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

package cloud.orbit.actors.cluster.impl;

import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RedisMsgTest {
    private static final int TEST_BYTE_ARRAY_SIZE = 1;

    @Test
    public void testEqualsAndHashCode() {
        final UUID testSenderAddress = UUID.randomUUID();
        final byte[] testMessageContents = new byte[TEST_BYTE_ARRAY_SIZE];
        new Random().nextBytes(testMessageContents);

        final RedisMsg testRedisMsg = new RedisMsg(testSenderAddress, testMessageContents);
        final RedisMsg equivalentRedisMsg = new RedisMsg(testSenderAddress, testMessageContents);

        assertEquals(equivalentRedisMsg, testRedisMsg);
        assertEquals(equivalentRedisMsg.hashCode(), testRedisMsg.hashCode());
    }

    @Test
    public void testEqualsWithSelf() {
        final UUID testSenderAddress = UUID.randomUUID();
        final byte[] testMessageContents = new byte[TEST_BYTE_ARRAY_SIZE];
        new Random().nextBytes(testMessageContents);

        final RedisMsg testRedisMsg = new RedisMsg(testSenderAddress, testMessageContents);

        assertEquals(testRedisMsg, testRedisMsg);
    }

    @Test
    public void testNotEqualsDifferingSenderAddress() {
        final byte[] testMessageContents = new byte[TEST_BYTE_ARRAY_SIZE];
        new Random().nextBytes(testMessageContents);

        final RedisMsg testRedisMsg = new RedisMsg(UUID.randomUUID(), new byte[TEST_BYTE_ARRAY_SIZE]);
        final RedisMsg nonEquivalentRedisMsg = new RedisMsg(UUID.randomUUID(), new byte[TEST_BYTE_ARRAY_SIZE]);

        assertNotEquals(nonEquivalentRedisMsg, testRedisMsg);
        assertNotEquals(nonEquivalentRedisMsg.hashCode(), testRedisMsg.hashCode());
    }

    @Test
    public void testNotEqualsDifferingMessageContents() {
        final UUID testSenderAddress = UUID.randomUUID();
        final RedisMsg testRedisMsg = new RedisMsg(testSenderAddress, new byte[TEST_BYTE_ARRAY_SIZE]);
        final RedisMsg nonEquivalentRedisMsg = new RedisMsg(testSenderAddress, new byte[0]);

        assertNotEquals(nonEquivalentRedisMsg, testRedisMsg);
        assertNotEquals(nonEquivalentRedisMsg.hashCode(), testRedisMsg.hashCode());;
    }

    @Test
    public void testNotEqualsWithNull() {
        assertNotEquals(null, new RedisMsg(UUID.randomUUID(), new byte[TEST_BYTE_ARRAY_SIZE]));
    }
}
