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

import cloud.orbit.actors.cluster.impl.lettuce.FstSerializedObjectCodec;
import cloud.orbit.tuples.Pair;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LettuceOrbitClient
{
    private final RedisClient redisClient;
    private final StatefulRedisPubSubConnection<String, Object> redisPubSubConnection;
    private final RedisPubSubAsyncCommands<String, Object> redisPubSubAsyncCommands;
    private final List<Pair<String, RedisPubSubListener<String, Object>>> subscriptions;

    private volatile boolean isConnected = false;

    public LettuceOrbitClient(final String resolvedUri)
    {
        this.redisClient = RedisClient.create(resolvedUri);
        this.redisPubSubConnection = this.redisClient.connectPubSub(new FstSerializedObjectCodec());
        this.redisPubSubAsyncCommands = this.redisPubSubConnection.async();

        this.subscriptions = new ArrayList<>();

        this.isConnected = isConnected();
    }

    public CompletableFuture<Void> subscribe(final String channelId, final RedisPubSubListener<String, Object> messageListener)
    {
        this.redisPubSubConnection.addListener(messageListener);
        final RedisFuture<Void> subscribeResult = this.redisPubSubAsyncCommands.subscribe(channelId);
        return (CompletableFuture)subscribeResult;
    }

    public CompletableFuture<Long> publish(final String channelId, final Object redisMsg) {

        return (CompletableFuture)this.redisPubSubAsyncCommands.publish(channelId, redisMsg);
    }

    public boolean isConnected()
    {
        return this.redisPubSubConnection.isOpen();
    }

    public void shutdown()
    {
        this.redisPubSubConnection.close();
    }
}
