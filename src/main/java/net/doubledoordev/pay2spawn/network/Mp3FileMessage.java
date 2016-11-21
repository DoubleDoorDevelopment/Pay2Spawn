/*
 * Copyright (c) 2016 Dries007 & DoubleDoorDevelopment
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the
 * disclaimer below) provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of Pay2Spawn nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
 * GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.doubledoordev.pay2spawn.network;

import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.client.Pay2SpawnClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Dries007
 */
public class Mp3FileMessage implements IMessage
{
    public static final int MAX_SIZE = 32000;

    private String name;
    private int count;
    private int size;
    private int offset;
    private int end;
    private byte[] bytes;

    private static String lastName;
    private static int countGot;
    private static byte[] temp;

    @SuppressWarnings("unused")
    public Mp3FileMessage()
    {
    }

    public Mp3FileMessage(String name, int count, int size, byte[] bytes, int offset, int end)
    {
        this.name = name;
        this.count = count;
        this.size = size;
        this.bytes = bytes;
        this.offset = offset;
        this.end = end;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        count = buf.readInt();
        size = buf.readInt();
        offset = buf.readInt();
        end = buf.readInt();
        bytes = new byte[end - offset];
        buf.readBytes(bytes);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeInt(count);
        buf.writeInt(size);
        buf.writeInt(offset);
        buf.writeInt(end);
        buf.writeBytes(bytes, offset, end - offset);
    }

    public static void send(File file, EntityPlayerMP playerEntity)
    {
        try
        {
            final String name = file.getName();
            byte[] bytes = FileUtils.readFileToByteArray(file);
            int count = 1 + (bytes.length / MAX_SIZE);
            for (int offset = 0; offset < bytes.length; offset += MAX_SIZE)
            {
                int end = Math.min(offset + MAX_SIZE, bytes.length);
                Pay2Spawn.getSNW().sendTo(new Mp3FileMessage(name, count, bytes.length, bytes, offset, end), playerEntity);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void assemble(Mp3FileMessage part)
    {
        if (!part.name.equals(lastName))
        {
            countGot = 0;
            lastName = part.name;
            temp = new byte[part.size];
        }
        countGot++;
        System.arraycopy(part.bytes, 0, temp, part.offset, part.bytes.length);
        if (part.count == countGot) // last packet
        {
            File file = new File(new File(Pay2Spawn.getConfigDir(), "mp3"), part.name);
            try
            {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                FileUtils.writeByteArrayToFile(file, temp);
                Pay2SpawnClient.playMP3(file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            temp = null;
            lastName = null;
            countGot = 0;
        }
    }

    public static class Handler implements IMessageHandler<Mp3FileMessage, IMessage>
    {
        @Override
        public IMessage onMessage(Mp3FileMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient()) assemble(message);
            return null;
        }
    }
}
