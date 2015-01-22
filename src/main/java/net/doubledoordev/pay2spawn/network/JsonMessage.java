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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;

import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * @author Dries007
 */
public class JsonMessage implements IMessage
{
    private String data;

    public JsonMessage(String data)
    {
        this.data = data;
    }

    public JsonMessage()
    {

    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        byte[] data = new byte[buf.readInt()];
        buf.readBytes(data);
        this.data = new String(data, Charset.forName("utf-8"));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(data.length());
        buf.writeBytes(data.getBytes(Charset.forName("utf-8")));
    }

    public static class Handler implements IMessageHandler<JsonMessage, IMessage>
    {
        @Override
        public IMessage onMessage(JsonMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                try
                {
                    // Makes json file Pretty Print & prevents Json syntax errors.
                    FileUtils.writeStringToFile(Pay2Spawn.getRewardDBFile(), Constants.GSON.toJson(JSON_PARSER.parse(message.data)));
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
                System.out.println(message.data);
                ConfiguratorManager.openCfg(message.data);
            }
            return null;
        }
    }
}
