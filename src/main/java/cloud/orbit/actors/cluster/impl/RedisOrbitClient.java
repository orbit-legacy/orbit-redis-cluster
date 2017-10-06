/*
 Copyright (C) 2017 Electronic Arts Inc.  All rights reserved.

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

import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloud.orbit.tuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class RedisOrbitClient
{
    private static Logger logger = LoggerFactory.getLogger(RedisOrbitClient.class);

    private final RedissonClient redisClient;
    private volatile boolean isConnected = false;
    private final List<Pair<String, MessageListener<Object>>> subscriptions = new ArrayList<>();
    private final Timer connectionTimer;

    public RedisOrbitClient(final RedissonClient redisClient, final Integer messagingHealthcheckInterval)
    {
        this.redisClient = redisClient;
        this.isConnected = redisClient.getNodesGroup().pingAll();
        this.connectionTimer = new Timer();
        this.connectionTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                connectionTask();
            }
        }, 0, messagingHealthcheckInterval);

        connectionTask();
    }

    public void subscribe(final String channelId, final MessageListener<Object> messageListener)
    {
        subscriptions.add(Pair.of(channelId, messageListener));
        if(isConnected)
        {
            redisClient.getTopic(channelId).addListener(messageListener);
        }
    }

    private void connectionTask()
    {
        try
        {
            boolean nowConnected = false;

            try
            {
                nowConnected = redisClient.getNodesGroup().pingAll();
            }
            catch (Throwable e)
            {
                logger.error("Unexpected error during redis ping", e);
            }

            if (!nowConnected)
            {
                isConnected = false;
                logger.error("Redis is not connected: {}", redisClient.getNodesGroup().getNodes().stream()
                        .map((x) -> x.getAddr().toString()).collect(Collectors.joining(",")));
            }

            if (nowConnected && !isConnected)
            {
                boolean subscribedAll = true;
                for (Pair<String, MessageListener<Object>> subscription : subscriptions)
                {
                    try
                    {
                        redisClient.getTopic(subscription.getLeft()).removeAllListeners();
                        redisClient.getTopic(subscription.getLeft()).addListener(subscription.getRight());
                    }
                    catch (Exception e)
                    {
                        logger.warn("Could not resubscribe to '{}', this may be transient.\n{}", subscription.getLeft(), e);
                        subscribedAll = false;
                        break;
                    }
                }
                isConnected = subscribedAll;
            }
        }
        catch(Throwable e)
        {
            logger.error("Unexpected connection task error", e);
        }
    }

    public boolean isConnectied () {
        return isConnected;
    }

    public RedissonClient getRedissonClient() {
        return this.redisClient;
    }

    public void shutdown() {
        this.connectionTimer.cancel();
        this.redisClient.shutdown();
    }
}
