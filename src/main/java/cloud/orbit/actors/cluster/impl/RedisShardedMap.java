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

import com.github.ssedano.hash.JumpConsistentHash;

import cloud.orbit.actors.cluster.impl.redisson.RedissonOrbitClient;
import cloud.orbit.exception.NotImplementedException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by joeh on 2017-01-26.
 */
public class RedisShardedMap<K, V> implements ConcurrentMap<K, V>
{
    private final Integer bucketCount;
    private final List<RedissonOrbitClient> redissonClients;
    private final String mapName;
    private final ConcurrentMap<String, ConcurrentMap<K, V>> cacheManager = new ConcurrentHashMap<>();

    public RedisShardedMap(final String mapName, final List<RedissonOrbitClient> redissonClients, final Integer bucketCount) {
        this.mapName = mapName;
        this.redissonClients = redissonClients;
        this.bucketCount = bucketCount;
    }

    private ConcurrentMap<K, V> getBucketMap(final Integer bucket) {
        final String realName = mapName + "#" + bucket;
        ConcurrentMap<K, V> result = cacheManager.get(realName);
        if (result == null)
        {
            final Integer clientId = JumpConsistentHash.jumpConsistentHash(realName, redissonClients.size());
            ConcurrentMap<K, V> temp = redissonClients.get(clientId).getRedissonClient().getMap(realName);
            result = cacheManager.putIfAbsent(realName, temp);
            if (result == null)
            {
                result = temp;
            }
        }
        return result;
    }

    private ConcurrentMap<K, V> getRealMap(final Object key) {
        final Integer bucket = JumpConsistentHash.jumpConsistentHash(key, bucketCount);
        return getBucketMap(bucket);
    }

    @Override
    public int size() {
        int result = 0;
        for(int i = 0; i < bucketCount; ++i) {
            result += getBucketMap(i).size();
        }
        return result;
    }

    @Override
    public boolean isEmpty()
    {
        for(int i = 0; i < bucketCount; ++i) {
            if(!getBucketMap(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public boolean containsKey(Object key)
    {
        for(int i = 0; i < bucketCount; ++i) {
            if(getBucketMap(i).containsKey(key)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value)
    {
        for(int i = 0; i < bucketCount; ++i) {
            if(getBucketMap(i).containsValue(value)) return true;
        }
        return false;
    }

    @Override
    public V get(Object key)
    {
        return getRealMap(key).get(key);
    }

    @Override
    public V put(K key, V value)
    {
        return getRealMap(key).put(key, value);
    }

    @Override
    public V remove(Object key)
    {
        return getRealMap(key).remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((key, val) -> {
            getRealMap(key).put(key, val);
        });
    }

    @Override
    public void clear() {
        for(int i = 0; i < bucketCount; ++i) {
            getBucketMap(i).clear();
        }
    }

    @Override
    public V putIfAbsent(final K key, final V value)
    {
        return getRealMap(key).putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return getRealMap(key).remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue)
    {
        return getRealMap(key).replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value)
    {
        return getRealMap(key).replace(key, value);
    }

    @Override
    public Set<K> keySet() {
        throw new NotImplementedException("Can not use sets on sharded map");
    }
    @Override
    public Collection<V> values() {
        throw new NotImplementedException("Can not use sets on sharded map");
    }
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new NotImplementedException("Can not use sets on sharded map");
    }


}
