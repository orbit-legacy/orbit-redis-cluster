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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by jhegarty on 2017-01-25.
 */
public class RedisMsg implements Serializable
{
    private UUID senderAddress;
    private byte[] messageContents;

    public RedisMsg(final UUID senderAddress, final byte[] messageContents) {
        this.senderAddress = senderAddress;
        this.messageContents = messageContents;
    }

    public UUID getSenderAddress()
    {
        return senderAddress;
    }

    public void setSenderAddress(UUID senderAddress)
    {
        this.senderAddress = senderAddress;
    }

    public byte[] getMessageContents()
    {
        return messageContents;
    }

    public void setMessageContents(byte[] messageContents)
    {
        this.messageContents = messageContents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RedisMsg redisMsg = (RedisMsg) o;
        return Objects.equals(senderAddress, redisMsg.senderAddress) &&
                Arrays.equals(messageContents, redisMsg.messageContents);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(senderAddress);
        result = 31 * result + Arrays.hashCode(messageContents);
        return result;
    }
}