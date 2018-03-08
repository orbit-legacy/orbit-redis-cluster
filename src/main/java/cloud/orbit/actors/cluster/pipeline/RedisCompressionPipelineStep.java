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

package cloud.orbit.actors.cluster.pipeline;

import cloud.orbit.actors.cluster.impl.redisson.RedisPipelineCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;

/**
 * <p>
 * Used with {@link RedisPipelineCodec}
 * </p>
 *
 * @see RedisPipelineCodec
 *
 * <p>
 * An alternative to using this pipeline step is to create a {@link org.redisson.client.RedisClient} in
 * {@link cloud.orbit.actors.cluster.impl.RedisConnectionManager} that is configured to use an instance of
 * {@link org.redisson.codec.LZ4Codec} with an inner codec that converts the objects being persisted to a binary stream.
 * Note that the {@link org.redisson.codec.LZ4Codec} uses the {@link org.redisson.codec.FstCodec} as its inner codec by
 * default
 * </p>
 *
 * @see org.redisson.codec.LZ4Codec
 * @see org.redisson.codec.FstCodec
 *
 */
public class RedisCompressionPipelineStep implements RedisPipelineStep
{
    private final LZ4Factory factory = LZ4Factory.fastestJavaInstance();

    @Override
    public ByteBuf read(final ByteBuf compressedByteBuf)
    {
        // Calculate initial lengths
        final int rawLength = compressedByteBuf.readableBytes();
        final int compressedLength = rawLength - 4;

        // Extract the decompressed length
        final int decompressedLength = compressedByteBuf.getInt(0);
        final byte[] decompressedBytes = new byte[decompressedLength];

        // Get the compressed bytes
        final byte[] compressedBytes = new byte[compressedLength];
        compressedByteBuf.getBytes(4, compressedBytes);

        // Decompress
        final LZ4SafeDecompressor decompressor = factory.safeDecompressor();
        decompressor.decompress(compressedBytes, decompressedBytes);

        return Unpooled.wrappedBuffer(decompressedBytes);
    }

    @Override
    public ByteBuf write(final ByteBuf uncompressedByteBuf)
    {
        // Calculate decompressed length and get uncompressed bytes
        final int uncompressedLength = uncompressedByteBuf.readableBytes();
        final byte[] uncompressedBytes = new byte[uncompressedLength];
        uncompressedByteBuf.getBytes(uncompressedByteBuf.readerIndex(), uncompressedBytes);

        // Compress the bytes
        final LZ4Compressor compressor = factory.fastCompressor();
        final byte[] compressedBytes = compressor.compress(uncompressedBytes);

        // Create final byte buffer to be returned
        final ByteBuffer byteBuffer = ByteBuffer.allocate(compressedBytes.length + 4);
        byteBuffer.putInt(uncompressedLength);
        byteBuffer.put(compressedBytes);
        byteBuffer.flip();

        return Unpooled.wrappedBuffer(byteBuffer);
    }
}
