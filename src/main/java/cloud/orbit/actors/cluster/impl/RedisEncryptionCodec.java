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

import cloud.orbit.exception.UncheckedException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by joeh on 2017-02-02.
 */
public class RedisEncryptionCodec implements Codec
{
    private final Codec innerCodec;
    private final SecretKey secretKey;

    private static final String CRYPTO_ALGO = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGO = "AES";


    public RedisEncryptionCodec(final String rawSecretKey, final Codec innerCodec) {
        this.secretKey = createKey(rawSecretKey);
        this.innerCodec = innerCodec;
    }

    private static SecretKey createKey(final String rawSecretKey) {
        try
        {
            byte[] key = rawSecretKey.getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            final byte[] finalKey = Arrays.copyOf(key, 16);
            return new SecretKeySpec(finalKey, KEY_ALGO);
        } catch(Exception e) {
            throw new UncheckedException(e);
        }
    }

    private static byte[] doCipher(final int mode, final SecretKey key, final byte[] rawBytes) {
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, key);
            return cipher.doFinal(rawBytes);
        } catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException
        {
            final int rawLength = buf.readableBytes();
            final byte[] rawBytes = new byte[rawLength];
            buf.readBytes(rawBytes);

            final byte[] clearBytes = doCipher(Cipher.DECRYPT_MODE, secretKey, rawBytes);

            final ByteBuf bf = Unpooled.wrappedBuffer(clearBytes);

            return innerCodec.getValueDecoder().decode(bf, state);
        }
    };

    private final Encoder encoder = new Encoder() {
        @Override
        public byte[] encode(Object in) throws IOException {
            final byte[] innerCodecBytes = innerCodec.getValueEncoder().encode(in);
            final byte[] cipherBytes = doCipher(Cipher.ENCRYPT_MODE, secretKey, innerCodecBytes);
            return cipherBytes;
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
