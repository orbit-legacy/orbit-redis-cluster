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

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.orbit.actors.cluster.impl.RedisDB;
import cloud.orbit.actors.cluster.impl.RedisKeyGenerator;
import cloud.orbit.actors.cluster.impl.RedisMsg;
import cloud.orbit.actors.cluster.impl.RedisShardedMap;
import cloud.orbit.concurrent.Task;
import cloud.orbit.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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
    private RedisClusterConfig config;
    private RedisDB redisDB;

    private final ConcurrentMap<String, ConcurrentMap<?, ?>> cacheManager = new ConcurrentHashMap<>();


    public RedisClusterPeer(final RedisClusterConfig config)
    {
        this.config = config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> ConcurrentMap<K, V> getCache(final String name)
    {
        final String realName = RedisKeyGenerator.key("shardedMap", Pair.of("cluster", clusterName), Pair.of("mapName", name));
        ConcurrentMap<?, ?> result = cacheManager.get(realName);
        if (result == null)
        {
            ConcurrentMap<?, ?>  targetMap = new RedisShardedMap<K, V>(realName, redisDB.getActorDirectoryClients(), config.getShardingBuckets());
            result = cacheManager.putIfAbsent(realName, targetMap);
            if (result == null)
            {
                result = targetMap;
            }
        }
        return (ConcurrentMap<K, V>) result;
    }

    @Override
    public NodeAddress localAddress()
    {
        return localAddress;
    }

    @Override
    public Task<?> join(final String clusterName, final String nodeName)
    {
        logger.info("Joining Redis Cluster '{}' as node '{}' [{}]...", clusterName, nodeName, localAddress.asUUID().toString());

        this.clusterName = clusterName;
        redisDB = new RedisDB(config);


        // Subscribe to Pub Sub
        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, localAddress.toString());
        redisDB.subscribeToChannel(nodeKey, (chan, msg) ->
        {
            receiveMessage((RedisMsg) msg);
        });


        writeMyEntry();
        syncNodes();

        return Task.done();
    }

    private void writeMyEntry()
    {
        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, localAddress.toString());
        redisDB.getShardedNodeDirectoryClient(nodeKey).getBucket(nodeKey).set(localAddress.toString(), config.getNodeLifetimeSeconds(), TimeUnit.SECONDS);
    }

    private void syncNodes()
    {
        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, "*");

        List<String> keys = new ArrayList<>();
        List<RedissonClient> clients = redisDB.getNodeDirectoryClients();
        for(RedissonClient client : clients) {
            keys.addAll(client.getKeys().findKeysByPattern(nodeKey));
        }

        List<NodeAddress> nodeAddresses = new ArrayList<>();
        for (final String key : keys)
        {
            final String rawKey = (String) redisDB.getShardedNodeDirectoryClient(key).getBucket(key).get();
            nodeAddresses.add(new NodeAddressImpl(UUID.fromString(rawKey)));
        }

        viewListener.onViewChange(nodeAddresses);
    }

    @Override
    public void sendMessage(final NodeAddress toAddress, final byte[] message)
    {
        Task.runAsync(() ->
                {
                    final RedisMsg redisMsg = new RedisMsg();
                    redisMsg.setMessageContents(message);
                    redisMsg.setSenderAddress(localAddress.asUUID());
                    final String targetNodeKey = RedisKeyGenerator.nodeKey(clusterName, toAddress.toString());
                    redisDB.sendMessageToChannel(targetNodeKey, redisMsg);
                },
                config.getCoreExecutorService()
        );

    }

    public void receiveMessage(final RedisMsg rawMessage)
    {
        Task.runAsync(() ->
                {
                    final NodeAddress nodeAddr = new NodeAddressImpl(rawMessage.getSenderAddress());
                    messageListener.receive(nodeAddr, rawMessage.getMessageContents());
                },
                config.getCoreExecutorService()
        )
                .exceptionally((e) ->
                {
                    logger.error("Error receiving message", e);
                    return null;
                });
    }

    @Override
    public Task pulse()
    {
        writeMyEntry();
        syncNodes();
        return Task.done();
    }

    @Override
    public void leave()
    {
        final String nodeKey = RedisKeyGenerator.nodeKey(clusterName, localAddress.toString());
        redisDB.getShardedNodeDirectoryClient(nodeKey).getBucket(nodeKey).delete();

        redisDB.shutdownConnections();
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
