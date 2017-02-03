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
public class RedisClusterBuilder
{
    private RedisClusterConfig redisClusterConfig;

    public RedisClusterBuilder() {
        redisClusterConfig = new RedisClusterConfig();
    }

    public RedisClusterBuilder nodeDirectoryUri(final String nodeDirectoryUri) {
        return this.nodeDirectoryUri(nodeDirectoryUri, false);
    }

    public RedisClusterBuilder nodeDirectoryUri(final String nodeDirectoryUri, final Boolean clustered) {
        redisClusterConfig.setNodeDirectoryUri(nodeDirectoryUri);
        redisClusterConfig.setNodeDirectoryClustered(clustered);
        return this;
    }

    public RedisClusterBuilder actorDirectoryUri(final String actorDirectoryUri) {
        return this.actorDirectoryUri(actorDirectoryUri, false);
    }

    public RedisClusterBuilder actorDirectoryUri(final String actorDirectoryUri, final Boolean clustered) {
        redisClusterConfig.setActorDirectoryUri(actorDirectoryUri);
        redisClusterConfig.setActorDirectoryClustered(clustered);
        return this;
    }

    public RedisClusterBuilder messagingUris(final List<String> messagingUris) {
        redisClusterConfig.setMessagingUris(messagingUris);
        return this;
    }


    public RedisClusterBuilder nodeLifetimeSecs(final Integer nodeLifetimeSecs) {
        redisClusterConfig.setNodeLifetimeSeconds(nodeLifetimeSecs);
        return this;
    }

    public RedisClusterBuilder maxRedisConnections(final Integer maxRedisConnections) {
        redisClusterConfig.setMaxRedisConnections(maxRedisConnections);
        return this;
    }

    public RedisClusterBuilder enableActorDirectoryHashing(final Integer clusteredActoryDirectoryBuckets) {
        redisClusterConfig.setActorDirectoryHashingEnabled(true);
        redisClusterConfig.setActorDirectoryHashBuckets(clusteredActoryDirectoryBuckets);
        return this;
    }

    public RedisClusterBuilder disableClusteredActorDirectory() {
        redisClusterConfig.setActorDirectoryHashingEnabled(false);
        redisClusterConfig.setActorDirectoryHashBuckets(0);
        return this;
    }

    public RedisClusterBuilder enableCompression() {
        redisClusterConfig.setUseCompression(true);
        return this;
    }

    public RedisClusterBuilder disableCompression() {
        redisClusterConfig.setUseCompression(false);
        return this;
    }

    public RedisClusterBuilder enableEncryption(final String encryptionKey) {
        redisClusterConfig.setUseEncryption(true);
        redisClusterConfig.setEncryptionKey(encryptionKey);
        return this;
    }

    public RedisClusterBuilder disableEncryption() {
        redisClusterConfig.setUseEncryption(false);
        return this;
    }

    public RedisClusterPeer build() {
        return new RedisClusterPeer(redisClusterConfig);
    }
}
