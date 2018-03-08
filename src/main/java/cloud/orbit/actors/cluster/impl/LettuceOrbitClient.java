package cloud.orbit.actors.cluster.impl;

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
