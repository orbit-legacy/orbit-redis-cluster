package cloud.orbit.actors.cluster.impl;

import cloud.orbit.actors.cluster.RedisClusterPeer;

/**
 * Created by joeh@ea.com on 2016-12-15.
 */
public class RedisPubSubListener implements com.lambdaworks.redis.pubsub.RedisPubSubListener<String, String> {
    private final RedisClusterPeer clusterPeer;

    public RedisPubSubListener(final RedisClusterPeer clusterPeer)
    {
        this.clusterPeer = clusterPeer;
    }

    @Override
    public void message(final String s, final String s2)
    {
        clusterPeer.receiveMessage(s2);
    }

    @Override
    public void message(final String s, final String k1, final String s2)
    {

    }

    @Override
    public void subscribed(final String s, final long l)
    {

    }

    @Override
    public void psubscribed(final String s, final long l)
    {

    }

    @Override
    public void unsubscribed(final String s, final long l)
    {

    }

    @Override
    public void punsubscribed(final String s, final long l)
    {

    }
}
