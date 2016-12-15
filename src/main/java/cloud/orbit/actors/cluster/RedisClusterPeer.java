/*
 Copyright (C) 2016 Electronic Arts Inc.  All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1.  Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
 2.  Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
 3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
     its contributors may be used to endorse or promote products derived
     from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cloud.orbit.actors.cluster;

import cloud.orbit.actors.cluster.impl.RedisConcurrentMap;
import cloud.orbit.actors.cluster.impl.RedisDB;
import cloud.orbit.actors.cluster.impl.RedisKeyGenerator;
import cloud.orbit.actors.cluster.impl.RedisPubSubListener;
import cloud.orbit.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by joeh@ea.com on 2016-12-13.
 */
public class RedisClusterPeer implements ClusterPeer
{
    private static Logger logger = LoggerFactory.getLogger(RedisClusterPeer.class);
    private ViewListener viewListener;
    private MessageListener messageListener;
    private NodeAddress localAddress = new NodeAddressImpl(UUID.randomUUID());
    private String clusterName;
    private final RedisDB redisDb = new RedisDB();
    private final String redisUri;
    private final Integer nodeLifetimeSecs;

    private final ConcurrentMap<String, RedisConcurrentMap<?, ?>> cacheManager = new ConcurrentHashMap<>();

    private RedisPubSubListener pubSubListener = new RedisPubSubListener(this);

    public RedisClusterPeer(final String redisUri) {
        this(redisUri, 20);
    }

    public RedisClusterPeer() {
        this("redis://localhost", 20);
    }

    public RedisClusterPeer(final String redisUri, final Integer nodeLifetimeSecs) {
        this.redisUri = redisUri;
        this.nodeLifetimeSecs = nodeLifetimeSecs;

    }


    @Override
    @SuppressWarnings("unchecked")
    public <K, V> ConcurrentMap<K, V> getCache(final String name)
    {
        RedisConcurrentMap<?, ?> result = cacheManager.get(name);
        if(result == null) {
            RedisConcurrentMap<?, ?> temp = new RedisConcurrentMap<K, V>(name, clusterName, redisDb);
            result = cacheManager.putIfAbsent(name, temp);
            if(result == null)
            {
                result = temp;
            }
        }
        return ( ConcurrentMap<K, V>) result;
    }

    @Override
    public NodeAddress localAddress()
    {
        return localAddress;
    }

    @Override
    public Task<?> join(final String clusterName, final String nodeName)
    {
        // TODO: Make this a task
        logger.info("Joining Redis Cluster...");

        this.clusterName = clusterName;

        redisDb.init(redisUri);

        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, localAddress.toString());

        // Subscribe to Pub Sub
        redisDb.getPubSubConnection().addListener(pubSubListener);
        redisDb.getPubSubConnection().subscribe(nodeKey);

        writeMyEntry();
        syncNodes();

        return Task.done();
    }

    private void writeMyEntry() {
        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, localAddress.toString());
        redisDb.getGenericConnection().setex(nodeKey, nodeLifetimeSecs, localAddress.toString());
    }

    private void syncNodes() {
        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, "*");

        List<NodeAddress> nodeAddresses = new ArrayList<>();
        List<String> keys = redisDb.getGenericConnection().keys(nodeKey);
        for(final String key: keys) {
            final String rawKey = redisDb.getGenericConnection().get(key);
            nodeAddresses.add(new NodeAddressImpl(UUID.fromString(rawKey)));
        }

        viewListener.onViewChange(nodeAddresses);
    }

    @Override
    public void sendMessage(final NodeAddress toAddress, final byte[] message)
    {
        // TODO: Base64 is not efficient, choose something else
        final String targetNodeKey = RedisKeyGenerator.nodeKey(clusterName, toAddress.toString());
        final String rawMessage = localAddress.toString() + "//" + Base64.getEncoder().encodeToString(message);
        redisDb.getGenericConnection().publish(targetNodeKey, rawMessage);
    }

    public void receiveMessage(final String rawMessage) {
        // TODO:  Base64 is not efficient, choose something else
        final Integer splitPoint = rawMessage.indexOf("//");
        if(splitPoint != -1){
            final String rawNodeAddr = rawMessage.substring(0, splitPoint);
            final String rawContents = rawMessage.substring(splitPoint + 2);
            final NodeAddress nodeAddr = new NodeAddressImpl(UUID.fromString(rawNodeAddr));
            final byte[] contents = Base64.getDecoder().decode(rawContents);
            messageListener.receive(nodeAddr, contents);
        }
    }

    @Override
    public Task pulse() {
        writeMyEntry();
        syncNodes();
        return Task.done();
    }

    @Override
    public void leave()
    {

    }

    @Override
    public void registerMessageReceiver(final MessageListener messageListener)
    {
        this.messageListener = messageListener;
    }

    @Override
    public void registerViewListener(final ViewListener viewListener)
    {
        this.viewListener = viewListener;
    }
}
