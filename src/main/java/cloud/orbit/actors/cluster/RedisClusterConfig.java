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

import java.security.Key;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jhegarty on 2017-01-19.
 */
public class RedisClusterConfig
{
    private String actorDirectoryUri = "redis://localhost";
    private Boolean actorDirectoryClustered = false;
    private String nodeDirectoryUri = "redis://localhost";
    private Boolean nodeDirectoryClustered = false;
    private List<String> messagingUris = Arrays.asList("redis://localhost");
    private Integer nodeLifetimeSeconds = 20;
    private Integer maxRedisConnections = 10;
    private Integer actorDirectoryHashBuckets = 128;
    private Boolean actorDirectoryHashingEnabled = true;


    public String getActorDirectoryUri()
    {
        return actorDirectoryUri;
    }

    public void setActorDirectoryUri(final String actorDirectoryUri)
    {
        this.actorDirectoryUri = actorDirectoryUri;
    }

    public String getNodeDirectoryUri()
    {
        return nodeDirectoryUri;
    }

    public void setNodeDirectoryUri(final String nodeDirectoryUri)
    {
        this.nodeDirectoryUri = nodeDirectoryUri;
    }

    public List<String> getMessagingUris()
    {
        return messagingUris;
    }

    public void setMessagingUris(final List<String> messagingUris)
    {
        this.messagingUris = messagingUris;
    }


    public Boolean getActorDirectoryClustered()
    {
        return actorDirectoryClustered;
    }

    public void setActorDirectoryClustered(final Boolean actorDirectoryClustered)
    {
        this.actorDirectoryClustered = actorDirectoryClustered;
    }

    public Boolean getNodeDirectoryClustered()
    {
        return nodeDirectoryClustered;
    }

    public void setNodeDirectoryClustered(final Boolean nodeDirectoryClustered)
    {
        this.nodeDirectoryClustered = nodeDirectoryClustered;
    }

    public Integer getNodeLifetimeSeconds()
    {
        return nodeLifetimeSeconds;
    }

    public void setNodeLifetimeSeconds(final Integer nodeLifetimeSeconds)
    {
        this.nodeLifetimeSeconds = nodeLifetimeSeconds;
    }


    public Integer getMaxRedisConnections()
    {
        return maxRedisConnections;
    }

    public void setMaxRedisConnections(final Integer maxRedisConnections)
    {
        this.maxRedisConnections = maxRedisConnections;
    }


    public Integer getActorDirectoryHashBuckets()
    {
        return actorDirectoryHashBuckets;
    }

    public void setActorDirectoryHashBuckets(final Integer actorDirectoryHashBuckets)
    {
        this.actorDirectoryHashBuckets = actorDirectoryHashBuckets;
    }

    public Boolean getActorDirectoryHashingEnabled()
    {
        return actorDirectoryHashingEnabled;
    }

    public void setActorDirectoryHashingEnabled(final Boolean actorDirectoryHashingEnabled)
    {
        this.actorDirectoryHashingEnabled = actorDirectoryHashingEnabled;
    }
}
