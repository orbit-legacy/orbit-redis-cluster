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
import java.util.List;


/**
 * Created by joeh@ea.com on 2016-12-13.
 */
public class RedisDB
{
    private RedisClusterConfig redisClusterConfig = null;
    private RedissonClient nodeDirectoryClient = null;
    private RedissonClient actorDirectoryClient = null;
    private List<RedissonClient> messagingClients = new ArrayList<>();
    private static Logger logger = LoggerFactory.getLogger(RedisDB.class);


    public RedisDB(final RedisClusterConfig redisClusterConfig)
    {
        this.redisClusterConfig = redisClusterConfig;

        logger.info("Connecting to Redis node directory at '{}'...", redisClusterConfig.getNodeDirectoryUri());
        nodeDirectoryClient = createClient(
                redisClusterConfig.getNodeDirectoryUri(),
                redisClusterConfig.getNodeDirectoryClustered(),
                true
        );

        logger.info("Connecting to Redis actor directory at '{}'...", redisClusterConfig.getActorDirectoryUri());
        actorDirectoryClient = createClient(
                redisClusterConfig.getActorDirectoryUri(),
                redisClusterConfig.getActorDirectoryClustered(),
                true
        );


        List<String> masters = redisClusterConfig.getMessagingUris();
        for (final String uri : masters)
        {
            logger.info("Connecting to Redis messaging node at '{}'...", uri);
            messagingClients.add(createClient(uri, false, false));

        }
    }

    public RedissonClient getNodeDirectoryClient()
    {
        return nodeDirectoryClient;
    }

    public RedissonClient getActorDirectoryClient()
    {
        return actorDirectoryClient;
    }


    public RedissonClient getMessagingClient(final String channel)
    {
        final int jumpConsistentHash = JumpConsistentHash.jumpConsistentHash(channel, messagingClients.size());
        return messagingClients.get(jumpConsistentHash);
    }

    public void shutdownConnections() {
        nodeDirectoryClient.shutdown();
        actorDirectoryClient.shutdown();
        for (final RedissonClient messagingClient : messagingClients)
        {
            messagingClient.shutdown();
        }

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

        // Compression?
        if(redisClusterConfig.getUseCompression()) {
            currentCodec = new RedisCompressionCodec(currentCodec);
        }

        // Encryption?
        if(redisClusterConfig.getUseEncryption()) {
            currentCodec = new RedisEncryptionCodec(redisClusterConfig.getEncryptionKey(), currentCodec);
        }

        redissonConfig.setCodec(currentCodec);

        // Clustered or not
        if (clustered)
        {
            redissonConfig.useClusterServers()
                    .addNodeAddress(resolvedUri)
                    .setMasterConnectionPoolSize(redisClusterConfig.getMaxRedisConnections())
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
