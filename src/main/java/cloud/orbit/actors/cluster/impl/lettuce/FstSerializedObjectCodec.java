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

package cloud.orbit.actors.cluster.impl.lettuce;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import io.lettuce.core.codec.RedisCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class FstSerializedObjectCodec implements RedisCodec<String, Object>
{
    private static final Charset charset = Charset.forName("UTF-8");
    private static final ThreadLocal<FSTConfiguration> fstConfig =
            ThreadLocal.withInitial(FSTConfiguration::createDefaultConfiguration);

    @Override
    public String decodeKey(ByteBuffer bytes)
    {
        return charset.decode(bytes).toString();
    }

    @Override
    public Object decodeValue(ByteBuffer bytes)
    {
        try
        {
            byte[] array = new byte[bytes.remaining()];
            bytes.get(array);
            FSTObjectInput in = fstConfig.get().getObjectInput(new ByteArrayInputStream(array));
            return in.readObject();
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key)
    {
        return charset.encode(key);
    }

    @Override
    public ByteBuffer encodeValue(Object value)
    {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream())
        {
            FSTObjectOutput out = fstConfig.get().getObjectOutput(bytes);
            out.writeObject(value);
            out.flush();
            return ByteBuffer.wrap(bytes.toByteArray());
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
