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

package cloud.orbit.actors.cluster.impl;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.SerializationCodec;
import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ssedano.hash.JumpConsistentHash;

import cloud.orbit.actors.cluster.RedisClusterConfig;
import cloud.orbit.exception.UncheckedException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by joeh@ea.com on 2016-12-13.
 */
public class RedisDB
{
    private RedisClusterConfig redisClusterConfig = null;
    private List<RedissonClient> nodeDirectoryClients = new ArrayList<>();
    private List<RedissonClient> actorDirectoryClients = new ArrayList<>();
    private List<RedissonClient> messagingClients = new ArrayList<>();
    private EventLoopGroup eventLoopGroup = null;
    private static Logger logger = LoggerFactory.getLogger(RedisDB.class);


    public RedisDB(final RedisClusterConfig redisClusterConfig)
    {
        this.redisClusterConfig = redisClusterConfig;

        // Create shared event loop group if required
        if(redisClusterConfig.getShareEventLoop())
        {
            eventLoopGroup = new NioEventLoopGroup();
        }

        final List<String> nodeDirectoryMasters = redisClusterConfig.getNodeDirectoryUris();
        for (final String uri : nodeDirectoryMasters)
        {
            logger.info("Connecting to Redis Node Directory node at '{}'...", uri);
            nodeDirectoryClients.add(createClient(uri, redisClusterConfig.getNodeDirectoryClustered(), true));

        }

        final List<String> actorDirectoryMasters = redisClusterConfig.getActorDirectoryUris();
        for (final String uri : actorDirectoryMasters)
        {
            logger.info("Connecting to Redis Actor Directory node at '{}'...", uri);
            actorDirectoryClients.add(createClient(uri, redisClusterConfig.getActorDirectoryClustered(), true));

        }


        final List<String> messagingMasters = redisClusterConfig.getMessagingUris();
        for (final String uri : messagingMasters)
        {
            logger.info("Connecting to Redis messaging node at '{}'...", uri);
            messagingClients.add(createClient(uri, redisClusterConfig.getMessagingClustered(), false));

        }
    }

    public List<RedissonClient> getNodeDirectoryClients()
    {
        return Collections.unmodifiableList(nodeDirectoryClients);
    }

    public List<RedissonClient> getActorDirectoryClients()
    {
        return Collections.unmodifiableList(actorDirectoryClients);
    }

    public List<RedissonClient> getMessagingClients()
    {
        return Collections.unmodifiableList(messagingClients);
    }

    public RedissonClient getShardedNodeDirectoryClient(final String shardId)
    {
        final int jumpConsistentHash = JumpConsistentHash.jumpConsistentHash(shardId, nodeDirectoryClients.size());
        return nodeDirectoryClients.get(jumpConsistentHash);
    }

    public RedissonClient getShardedActorDirectoryClient(final String shardId)
    {
        final int jumpConsistentHash = JumpConsistentHash.jumpConsistentHash(shardId, actorDirectoryClients.size());
        return actorDirectoryClients.get(jumpConsistentHash);
    }

    public void subscribeToChannel(final String channelId, final MessageListener<Object> statusListener)
    {
        for (final RedissonClient messagingClient : messagingClients)
        {
            messagingClient.getTopic(channelId).addListener(statusListener);
        }
    }

    public void sendMessageToChannel(final String channelId, final Object msg)
    {
            final List<RedissonClient> localMessagingClients = messagingClients;
            final int activeClientCount = localMessagingClients.size();
            if(activeClientCount > 0)
            {
                final int randomId = ThreadLocalRandom.current().nextInt(activeClientCount);
                final RedissonClient client = localMessagingClients.get(randomId);

                client.getTopic(channelId).publishAsync(msg).exceptionally((e) ->
                {
                    logger.error("Error sending message", e);
                    return 0L;
                });
            }
            else
            {
                throw new UncheckedException("No Redis messaging instances available.");
            }
    }

    public void shutdownConnections() {
        nodeDirectoryClients.forEach(RedissonClient::shutdown);
        actorDirectoryClients.forEach(RedissonClient::shutdown);
        messagingClients.forEach(RedissonClient::shutdown);
    }

    private RedissonClient createClient(final String uri, final Boolean clustered, final Boolean useJavaSerializer)
    {
        // Resolve URI
        final URI realUri = URI.create(uri);
        if (!realUri.getScheme().equalsIgnoreCase("redis"))
        {
            throw new UncheckedException("Invalid Redis URI.");
        }
        String host = realUri.getHost();
        if (host == null) host = "localhost";
        Integer port = realUri.getPort();
        if (port == -1) port = 6379;
        final String resolvedUri = host + ":" + port;

        final Config redissonConfig = new Config();

        Codec currentCodec;

        // Low level serializer
        if (useJavaSerializer) {
            currentCodec = new SerializationCodec();
        }
        else {
            currentCodec = new JsonJacksonCodec();
        }

        // Pipeline
        currentCodec = new RedisPipelineCodec(redisClusterConfig.getPipelineSteps(), currentCodec);

        // Configure codec
        redissonConfig.setCodec(currentCodec);

        // Event loop group
        if(eventLoopGroup != null) {
            redissonConfig.setEventLoopGroup(eventLoopGroup);
        }

        // Threading
        redissonConfig.setThreads(redisClusterConfig.getRedissonThreads());
        redissonConfig.setNettyThreads(redisClusterConfig.getNettyThreads());
        if(redisClusterConfig.getRedissonExecutorService() != null) {
            redissonConfig.setExecutor(redisClusterConfig.getRedissonExecutorService());
        }

        // Clustered or not
        if (clustered)
        {
            redissonConfig.useClusterServers()
                    .addNodeAddress(resolvedUri)
                    .setMasterConnectionMinimumIdleSize(redisClusterConfig.getMinRedisConnections())
                    .setMasterConnectionPoolSize(redisClusterConfig.getMaxRedisConnections())
                    .setSlaveConnectionMinimumIdleSize(redisClusterConfig.getMinRedisConnections())
                    .setSlaveConnectionPoolSize(redisClusterConfig.getMaxRedisConnections())
                    .setConnectTimeout(redisClusterConfig.getConnectionTimeout())
                    .setTimeout(redisClusterConfig.getGeneralTimeout())
                    .setIdleConnectionTimeout(redisClusterConfig.getIdleTimeout())
                    .setReconnectionTimeout(redisClusterConfig.getReconnectionTimeout())
                    .setPingTimeout(redisClusterConfig.getPingTimeout())
                    .setRetryAttempts(redisClusterConfig.getRetryAttempts())
                    .setRetryInterval(redisClusterConfig.getRetryInterval())
                    .setReadMode(ReadMode.MASTER);
        }
        else
        {
            redissonConfig.useSingleServer()
                    .setAddress(resolvedUri)
                    .setConnectionMinimumIdleSize(redisClusterConfig.getMinRedisConnections())
                    .setConnectionPoolSize(redisClusterConfig.getMaxRedisConnections())
                    .setConnectTimeout(redisClusterConfig.getConnectionTimeout())
                    .setTimeout(redisClusterConfig.getGeneralTimeout())
                    .setIdleConnectionTimeout(redisClusterConfig.getIdleTimeout())
                    .setReconnectionTimeout(redisClusterConfig.getReconnectionTimeout())
                    .setPingTimeout(redisClusterConfig.getPingTimeout())
                    .setRetryAttempts(redisClusterConfig.getRetryAttempts())
                    .setRetryInterval(redisClusterConfig.getRetryInterval());
        }

        return Redisson.create(redissonConfig);
    }
}
