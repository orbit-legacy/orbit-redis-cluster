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

package cloud.orbit.actors.cluster;

import cloud.orbit.actors.cluster.pipeline.RedisBasicPipeline;
import cloud.orbit.actors.cluster.pipeline.RedisPipelineStep;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by jhegarty on 2017-01-19.
 */
public class RedisClusterConfig
{
    private List<String> actorDirectoryUris = Arrays.asList("redis://localhost");
    private List<String> nodeDirectoryUris = Arrays.asList("redis://localhost");
    private List<String> messagingUris = Arrays.asList("redis://localhost");
    private Integer nodeLifetimeSeconds = 20;
    private Integer minRedisConnections = 10;
    private Integer maxRedisConnections = 64;
    private Integer connectionTimeout = 5000;
    private Integer generalTimeout = 3000;
    private Integer idleTimeout = 10000;
    private Integer reconnectionTimeout = 1000;
    private Integer pingTimeout = 1000;
    private Integer retryAttempts = 5;
    private Integer retryInterval = 1000;
    private Integer failedAttempts = Integer.MAX_VALUE;
    private Integer messageSendAttempts = Integer.MAX_VALUE;
    private Boolean dnsMonitoring = true;
    private Integer dnsMonitoringInverval = 10000;
    private Integer messagingHealthcheckInterval = 10000;
    private Integer shardingBuckets = 256;
    private Integer redissonThreads = Runtime.getRuntime().availableProcessors() * 2;
    private Integer nettyThreads = Runtime.getRuntime().availableProcessors() * 2;
    private ExecutorService redissonExecutorService = null;
    private Boolean shareEventLoop = false;
    private List<RedisPipelineStep> pipelineSteps = RedisBasicPipeline.defaultPipeline();
    private ExecutorService coreExecutorService = ForkJoinPool.commonPool();


    public List<String> getActorDirectoryUris()
    {
        return actorDirectoryUris;
    }

    public void setActorDirectoryUris(final List<String> actorDirectoryUris)
    {
        this.actorDirectoryUris = Collections.unmodifiableList(actorDirectoryUris);
    }

    public List<String> getNodeDirectoryUris()
    {
        return nodeDirectoryUris;
    }

    public void setNodeDirectoryUris(final List<String> nodeDirectoryUris)
    {
        this.nodeDirectoryUris = Collections.unmodifiableList(nodeDirectoryUris);
    }

    public List<String> getMessagingUris()
    {
        return messagingUris;
    }

    public void setMessagingUris(final List<String> messagingUris)
    {
        this.messagingUris = Collections.unmodifiableList(messagingUris);
    }

    public Integer getNodeLifetimeSeconds()
    {
        return nodeLifetimeSeconds;
    }

    public void setNodeLifetimeSeconds(final Integer nodeLifetimeSeconds)
    {
        this.nodeLifetimeSeconds = nodeLifetimeSeconds;
    }

    public Integer getMinRedisConnections()
    {
        return minRedisConnections;
    }

    public void setMinRedisConnections(Integer minRedisConnections)
    {
        this.minRedisConnections = minRedisConnections;
    }


    public Integer getMaxRedisConnections()
    {
        return maxRedisConnections;
    }

    public void setMaxRedisConnections(final Integer maxRedisConnections)
    {
        this.maxRedisConnections = maxRedisConnections;
    }


    public ExecutorService getCoreExecutorService()
    {
        return coreExecutorService;
    }

    public void setCoreExecutorService(final ExecutorService coreExecutorService)
    {
        this.coreExecutorService = coreExecutorService;
    }


    public Integer getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout(final Integer connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getGeneralTimeout()
    {
        return generalTimeout;
    }

    public void setGeneralTimeout(final Integer generalTimeout)
    {
        this.generalTimeout = generalTimeout;
    }

    public Integer getIdleTimeout()
    {
        return idleTimeout;
    }

    public void setIdleTimeout(final Integer idleTimeout)
    {
        this.idleTimeout = idleTimeout;
    }

    public Integer getReconnectionTimeout()
    {
        return reconnectionTimeout;
    }

    public void setReconnectionTimeout(final Integer reconnectionTimeout)
    {
        this.reconnectionTimeout = reconnectionTimeout;
    }

    public Integer getPingTimeout()
    {
        return pingTimeout;
    }

    public void setPingTimeout(final Integer pingTimeout)
    {
        this.pingTimeout = pingTimeout;
    }

    public Integer getRetryAttempts()
    {
        return retryAttempts;
    }

    public void setRetryAttempts(final Integer retryAttempts)
    {
        this.retryAttempts = retryAttempts;
    }

    public Integer getRetryInterval()
    {
        return retryInterval;
    }

    public void setRetryInterval(final Integer retryInterval)
    {
        this.retryInterval = retryInterval;
    }

    public Integer getShardingBuckets()
    {
        return shardingBuckets;
    }

    public void setShardingBuckets(final Integer shardingBuckets)
    {
        this.shardingBuckets = shardingBuckets;
    }

    public List<RedisPipelineStep> getPipelineSteps()
    {
        return pipelineSteps;
    }

    public void setPipelineSteps(List<RedisPipelineStep> pipelineSteps)
    {
        this.pipelineSteps = Collections.unmodifiableList(pipelineSteps);
    }


    public Integer getRedissonThreads()
    {
        return redissonThreads;
    }

    public void setRedissonThreads(Integer redissonThreads)
    {
        this.redissonThreads = redissonThreads;
    }

    public Integer getNettyThreads()
    {
        return nettyThreads;
    }

    public void setNettyThreads(Integer nettyThreads)
    {
        this.nettyThreads = nettyThreads;
    }

    public Boolean getShareEventLoop()
    {
        return shareEventLoop;
    }

    public void setShareEventLoop(Boolean shareEventLoop)
    {
        this.shareEventLoop = shareEventLoop;
    }

    public ExecutorService getRedissonExecutorService()
    {
        return redissonExecutorService;
    }

    public void setRedissonExecutorService(ExecutorService redissonExecutorService)
    {
        this.redissonExecutorService = redissonExecutorService;
    }

    public Boolean getDnsMonitoring()
    {
        return dnsMonitoring;
    }

    public void setDnsMonitoring(Boolean dnsMonitoring)
    {
        this.dnsMonitoring = dnsMonitoring;
    }

    public Integer getDnsMonitoringInverval()
    {
        return dnsMonitoringInverval;
    }

    public void setDnsMonitoringInverval(Integer dnsMonitoringInverval)
    {
        this.dnsMonitoringInverval = dnsMonitoringInverval;
    }

    public Integer getFailedAttempts()
    {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts)
    {
        this.failedAttempts = failedAttempts;
    }

    public Integer getMessagingHealthcheckInterval()
    {
        return messagingHealthcheckInterval;
    }

    public void setMessagingHealthcheckInterval(Integer messagingHealthcheckInterval)
    {
        this.messagingHealthcheckInterval = messagingHealthcheckInterval;
    }

    public Integer getMessageSendAttempts()
    {
        return messageSendAttempts;
    }

    public void setMessageSendAttempts(Integer messageSendAttempts)
    {
        this.messageSendAttempts = messageSendAttempts;
    }
}
