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

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import cloud.orbit.actors.cluster.pipeline.RedisPipelineStep;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by joeh on 2017-04-20.
 */
public class RedisPipelineCodec implements Codec
{
    private final Codec innerCodec;
    private final List<RedisPipelineStep> pipelineSteps;

    public RedisPipelineCodec(final List<RedisPipelineStep> pipelineSteps, final Codec innerCodec) {
        this.innerCodec = innerCodec;
        this.pipelineSteps = pipelineSteps;
    }

    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException
        {
            final int rawLength = buf.readableBytes();
            final byte[] rawBytes = new byte[rawLength];
            buf.readBytes(rawBytes);

            final ListIterator li = pipelineSteps.listIterator(pipelineSteps.size());
            
            byte[] conversionBytes = rawBytes;

            while (li.hasPrevious()) {
                final RedisPipelineStep pipelineStep = (RedisPipelineStep) li.previous();
                conversionBytes = pipelineStep.read(conversionBytes);
            }

            final ByteBuf bf = Unpooled.wrappedBuffer(conversionBytes);

            return innerCodec.getValueDecoder().decode(bf, state);
        }
    };

    private final Encoder encoder = new Encoder() {
        @Override
        public byte[] encode(Object in) throws IOException {
            byte[] conversionBytes = innerCodec.getValueEncoder().encode(in);

            for (final RedisPipelineStep pipelineStep : pipelineSteps)
            {
                conversionBytes = pipelineStep.write(conversionBytes);
            }

            return conversionBytes;
        }
    };

    @Override
    public Decoder<Object> getMapValueDecoder() {
        return getValueDecoder();
    }

    @Override
    public Encoder getMapValueEncoder() {
        return getValueEncoder();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return getValueDecoder();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return getValueEncoder();
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
}
