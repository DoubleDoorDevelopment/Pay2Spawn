/*
 * Copyright (c) 2014, DoubleDoorDevelopment
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.doubledoordev.pay2spawn.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.configurator.ConfiguratorManager;
import net.doubledoordev.pay2spawn.util.Constants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class JsonMessage implements IMessage
{
    public static final int MAX_SIZE = 32000;

    private static int lastID;
    private static int countGot;
    private static byte[] temp;

    private int id;
    private int count;
    private int size;
    private int offset;
    private int end;
    private byte[] bytes;

    public JsonMessage(int id, int count, int size, byte[] bytes, int offset, int end)
    {
        this.id = id;
        this.count = count;
        this.size = size;
        this.bytes = bytes;
        this.offset = offset;
        this.end = end;
    }

    public JsonMessage()
    {
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.id = buf.readInt();
        this.count = buf.readInt();
        this.size = buf.readInt();
        this.offset = buf.readInt();
        this.end = buf.readInt();
        bytes = new byte[end - offset];

        buf.readBytes(bytes);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeInt(count);
        buf.writeInt(size);
        buf.writeInt(offset);
        buf.writeInt(end);
        buf.writeBytes(bytes, offset, end - offset);
    }

    public static void sendToClient(EntityPlayerMP target) throws IOException
    {
        int id = RANDOM.nextInt();
        while (id == 0) id = RANDOM.nextInt();
        byte[] bytes = GSON_NOPP.toJson(JSON_PARSER.parse(FileUtils.readFileToString(Pay2Spawn.getRewardDBFile()))).getBytes(Charset.forName("utf-8"));
        int count = 1 + (bytes.length / MAX_SIZE);
        for (int offset = 0; offset < bytes.length; offset += MAX_SIZE)
        {
            int end = Math.min(offset + MAX_SIZE, bytes.length);
            Pay2Spawn.getSnw().sendTo(new JsonMessage(id, count, bytes.length, bytes, offset, end), target);
        }
    }

    public static void sendToServer(String data)
    {
        int id = RANDOM.nextInt();
        while (id == 0) id = RANDOM.nextInt();
        byte[] bytes = data.getBytes(Charset.forName("utf-8"));
        int count = 1 + (bytes.length / MAX_SIZE);
        for (int offset = 0; offset < bytes.length; offset += MAX_SIZE)
        {
            int end = Math.min(offset + MAX_SIZE, bytes.length);
            Pay2Spawn.getSnw().sendToServer(new JsonMessage(id, count, bytes.length, bytes, offset, end));
        }
    }

    public static String assemble(JsonMessage part)
    {
        String out = null;
        if (lastID != part.id)
        {
            countGot = 0;
            lastID = part.id;
            temp = new byte[part.size];
        }
        countGot ++;
        System.arraycopy(part.bytes, 0, temp, part.offset,part.bytes.length);
        if (part.count == countGot) // last packet
        {
            out = new String(temp, Charset.forName("utf-8"));
            temp = null;
            lastID = 0;
        }
        return out;
    }

    public static class Handler implements IMessageHandler<JsonMessage, IMessage>
    {
        @Override
        public IMessage onMessage(JsonMessage message, MessageContext ctx)
        {
            String data = assemble(message);
            if (data == null) return null;
            if (ctx.side.isServer())
            {
                try
                {
                    // Makes json file Pretty Print & prevents Json syntax errors.
                    FileUtils.writeStringToFile(Pay2Spawn.getRewardDBFile(), Constants.GSON.toJson(JSON_PARSER.parse(data)));
                    Pay2Spawn.reloadDB();
                    MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText("Pay2Spawn configuration updated by " + ctx.getServerHandler().playerEntity.getDisplayName()));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else if (ctx.side.isClient())
            {
                ConfiguratorManager.openCfg(data);
            }
            return null;
        }
    }
}
