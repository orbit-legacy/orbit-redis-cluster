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

package cloud.orbit.actors.cluster.pipeline;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RedisCompressionPipelineStepTest
{
    private static final int TEST_BYTE_ARRAY_SIZE = 50;

    private final RedisCompressionPipelineStep redisCompressionPipelineStep = new RedisCompressionPipelineStep();

    @Test
    public void testWriteRead() {
        final byte[] testByteArray = new byte[TEST_BYTE_ARRAY_SIZE];
        new Random().nextBytes(testByteArray);
        ByteBuf testByteBuf = Unpooled.wrappedBuffer(testByteArray);

        final ByteBuf compressedByteBuf = this.redisCompressionPipelineStep.write(testByteBuf);
        assertEquals(testByteBuf.refCnt(), 0);

        final ByteBuf decompressedByteBuf = this.redisCompressionPipelineStep.read(compressedByteBuf);
        assertEquals(compressedByteBuf.refCnt(), 0);

        final byte[] resultantByteArray = new byte[decompressedByteBuf.readableBytes()];
        decompressedByteBuf.getBytes(decompressedByteBuf.readerIndex(),resultantByteArray);
        assertArrayEquals(testByteArray, resultantByteArray);
    }
}
