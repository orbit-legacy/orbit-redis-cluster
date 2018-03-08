/*
 Copyright (C) 2018 Electronic Arts Inc.  All rights reserved.

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

package cloud.orbit.actors.cluster.impl.lettuce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.util.concurrent.CompletableFuture;

public class LettuceOrbitClient
{
    private static Logger logger = LoggerFactory.getLogger(LettuceOrbitClient.class);

    private final RedisClient redisClient;;
    private final StatefulRedisPubSubConnection<String, Object> redisSubscribingConnection;
    private final RedisPubSubAsyncCommands<String, Object> redisSubscribingAsyncCommands;
    private final StatefulRedisPubSubConnection<String, Object> redisPublishingConnection;
    private final RedisPubSubAsyncCommands<String, Object> redisPublishingAsyncCommands;

    public LettuceOrbitClient(final String resolvedUri)
    {
        this.redisClient = RedisClient.create(resolvedUri);

        this.redisSubscribingConnection = this.redisClient.connectPubSub(new FstSerializedObjectCodec());
        this.redisSubscribingAsyncCommands = this.redisSubscribingConnection.async();

        this.redisPublishingConnection = this.redisClient.connectPubSub(new FstSerializedObjectCodec());
        this.redisPublishingAsyncCommands = this.redisPublishingConnection.async();
    }

    public CompletableFuture<Void> subscribe(final String channelId, final RedisPubSubListener<String, Object> messageListener)
    {
        if (this.redisSubscribingConnection.isOpen())
        {
            this.redisSubscribingConnection.addListener(messageListener);
            final RedisFuture<Void> subscribeResult = this.redisSubscribingAsyncCommands.subscribe(channelId);
            return (CompletableFuture)subscribeResult;
        }
        else
        {
            logger.error("Error subscribing to channel [{}]", channelId);
            return new CompletableFuture<>();
        }
    }

    public CompletableFuture<Long> publish(final String channelId, final Object redisMsg)
    {
        if (this.redisPublishingConnection.isOpen()) {
            return (CompletableFuture)this.redisPublishingAsyncCommands.publish(channelId, redisMsg);
        }
        else
        {
            logger.error("Error publishing message to channel [{}]", channelId);
            return new CompletableFuture<>();
        }
    }

    public boolean isConnected()
    {
        return this.redisSubscribingConnection.isOpen() && this.redisPublishingConnection.isOpen();
    }

    public void shutdown()
    {
        this.redisSubscribingConnection.close();
        this.redisPublishingConnection.close();
        this.redisClient.shutdown();
    }
}
