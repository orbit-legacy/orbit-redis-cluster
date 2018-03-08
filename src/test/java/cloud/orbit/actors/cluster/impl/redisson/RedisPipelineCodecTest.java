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

package cloud.orbit.actors.cluster.impl.redisson;

import org.junit.Test;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.handler.State;

import cloud.orbit.actors.cluster.impl.redisson.RedisPipelineCodec;
import cloud.orbit.actors.cluster.pipeline.RedisCompressionPipelineStep;
import cloud.orbit.actors.cluster.pipeline.RedisNoOpPipelineStep;
import cloud.orbit.actors.cluster.pipeline.RedisPipelineStep;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RedisPipelineCodecTest {

    private static final Integer TEST_INT = 7;
    private static final Codec INNER_CODEC = new IntegerCodec();

    @Test
    public void testValueEncodeDecode_EmptyPipelineSteps() throws Exception {
        final List<RedisPipelineStep> pipelineSteps = new ArrayList<>();
        final RedisPipelineCodec testRedisPipelineCodec = new RedisPipelineCodec(pipelineSteps, INNER_CODEC);

        final ByteBuf encodedTestIntAsByteBuf = testRedisPipelineCodec.getValueEncoder().encode(TEST_INT);
        final Integer decodedTestInt = (Integer)testRedisPipelineCodec.getValueDecoder().decode(encodedTestIntAsByteBuf, new State(false));

        assertEquals(decodedTestInt, TEST_INT);
    }


    @Test
    public void testValueEncodeDecode_WithNoOpPipelineStep() throws Exception {
        final List<RedisPipelineStep> pipelineSteps = new ArrayList<>();
        final RedisPipelineCodec testRedisPipelineCodec = new RedisPipelineCodec(pipelineSteps, INNER_CODEC);

        final ByteBuf encodedTestIntAsByteBuf = testRedisPipelineCodec.getValueEncoder().encode(TEST_INT);
        final Integer decodedTestInt = (Integer)testRedisPipelineCodec.getValueDecoder().decode(encodedTestIntAsByteBuf, new State(false));

        assertEquals(decodedTestInt, TEST_INT);
    }

    @Test
    public void testValueEncodeDecode_WithCompressionPipelineStep() throws Exception {
        final List<RedisPipelineStep> pipelineSteps = Arrays.asList(new RedisCompressionPipelineStep());
        final RedisPipelineCodec testRedisPipelineCodec = new RedisPipelineCodec(pipelineSteps, INNER_CODEC);

        final ByteBuf encodedTestIntAsByteBuf = testRedisPipelineCodec.getValueEncoder().encode(TEST_INT);
        final Integer decodedTestInt = (Integer)testRedisPipelineCodec.getValueDecoder().decode(encodedTestIntAsByteBuf, new State(false));

        assertEquals(decodedTestInt, TEST_INT);
    }

    @Test
    public void testValueEncodeDecode_WithMultiplePipelineSteps() throws Exception {
        final List<RedisPipelineStep> pipelineSteps = Arrays.asList(new RedisNoOpPipelineStep(), new RedisCompressionPipelineStep());
        final RedisPipelineCodec testRedisPipelineCodec = new RedisPipelineCodec(pipelineSteps, INNER_CODEC);

        final ByteBuf encodedTestIntAsByteBuf = testRedisPipelineCodec.getValueEncoder().encode(TEST_INT);
        final Integer decodedTestInt = (Integer)testRedisPipelineCodec.getValueDecoder().decode(encodedTestIntAsByteBuf, new State(false));

        assertEquals(decodedTestInt, TEST_INT);
    }
}
