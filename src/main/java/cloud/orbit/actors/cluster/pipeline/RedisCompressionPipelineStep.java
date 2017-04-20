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

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4SafeDecompressor;

import java.nio.ByteBuffer;

/**
 * Created by joeh on 2017-04-20.
 */
public class RedisCompressionPipelineStep implements RedisPipelineStep
{
    private final LZ4Factory factory = LZ4Factory.fastestJavaInstance();

    @Override
    public byte[] read(final byte[] buf)
    {
        // Calculate initial lengths
        final Integer rawLength = buf.length;
        final Integer compressedLength = rawLength - 4;

        // Create a byte buffer we can read
        final ByteBuffer byteBuf = ByteBuffer.allocate(buf.length);
        byteBuf.put(buf);
        byteBuf.rewind();

        // Extract the decompressed length
        final Integer decompressedLength = byteBuf.getInt();
        final byte[] decompressedBytes = new byte[decompressedLength];

        // Get the compressed bytes
        final byte[] compressedBytes = new byte[compressedLength];
        byteBuf.get(compressedBytes);

        // Decompress
        final LZ4SafeDecompressor decompressor = factory.safeDecompressor();
        decompressor.decompress(compressedBytes, decompressedBytes);

        return decompressedBytes;
    }

    @Override
    public byte[] write(final byte[] uncompressedBytes)
    {
        final LZ4Compressor compressor = factory.highCompressor();
        final byte[] compressedBytes = compressor.compress(uncompressedBytes);
        final ByteBuffer buffer = ByteBuffer.allocate(compressedBytes.length + 4);
        buffer.putInt(uncompressedBytes.length);
        buffer.put(compressedBytes);

        return buffer.array();
    }
}
