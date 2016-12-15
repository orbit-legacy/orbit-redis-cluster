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

import com.lambdaworks.redis.ScriptOutputType;

import cloud.orbit.exception.NotImplementedException;
import cloud.orbit.tuples.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by joeh@ea.com on 2016-12-13.
 */
public class RedisConcurrentMap<K, V> implements ConcurrentMap<K, V>
{
    private final RedisDB redisDb;
    private final String prefix;

    @SuppressWarnings("unchecked")
    public RedisConcurrentMap(final String name, final String clusterName, final RedisDB redisDb) {
        this.redisDb = redisDb;
        this.prefix = RedisKeyGenerator.key(name, Pair.of("clusterName", clusterName)) + "=";

    }

    @Override
    public V get(final Object key)
    {
        final String res = redisDb.getGenericConnection().get(prefix + key.toString());
        if(res == null) return null;
        return RedisSerializer.deserialize(res);
    }

    @Override
    public boolean containsKey(final Object key)
    {
        return redisDb.getGenericConnection().get(prefix + key.toString()) != null;
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue)
    {
        final V result = get(key);
        return result != null ? result : defaultValue;
    }

    @Override
    public V putIfAbsent(final K key, final V value)
    {
        if(redisDb.getGenericConnection().setnx(prefix + key.toString(), RedisSerializer.serialize(value)))
        {
            return null;
        } else {
            return get(key);
        }
    }

    @Override
    public V put(final K key, final V value)
    {
        final String ret = redisDb.getGenericConnection().getset(prefix + key.toString(), RedisSerializer.serialize(value));
        if(ret == null) return null;
        return RedisSerializer.deserialize(ret);
    }

    @Override
    public V remove(final Object key)
    {
        final V res = get(key);
        redisDb.getGenericConnection().del(prefix + key.toString());
        return res;
    }

    @Override
    public boolean remove(final Object key, final Object value)
    {
        final String script =
                "local currentVal = redis.call(\"GET\", KEYS[1])" +
                        "    if(ARGV[1] == currentVal) then" +
                        "        redis.call(\"DEL\", KEYS[1])" +
                        "        return true" +
                        "    end" +
                        "    return false";

        final String existingCheck = RedisSerializer.serialize(value);
        final String realKey = prefix + key.toString();
        final Boolean res =  redisDb.getGenericConnection().eval(script, ScriptOutputType.BOOLEAN, new String[] {realKey} , existingCheck);
        return res;
    }


    @Override
    public V replace(final K key, final V value)
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean replace(final K key, final V oldValue, final V newValue)
    {
        throw new NotImplementedException();
    }

    @Override
    public int hashCode()
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean equals(final Object o)
    {
        throw new NotImplementedException();
    }

    @Override
    public Collection<V> values()
    {
        throw new NotImplementedException();
    }

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        throw new NotImplementedException();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m)
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean containsValue(final Object value)
    {
        throw new NotImplementedException();
    }

    @Override
    public void clear()
    {
        throw new NotImplementedException();
    }

    @Override
    public int size()
    {
        throw new NotImplementedException();
    }

    @Override
    public boolean isEmpty()
    {
        throw new NotImplementedException();
    }

    @Override
    public Set<K> keySet()
    {
        throw new NotImplementedException();
    }
}
