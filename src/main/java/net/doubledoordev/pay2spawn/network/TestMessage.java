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

import com.google.gson.JsonObject;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.permissions.BanHelper;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.doubledoordev.pay2spawn.random.RndVariable;
import net.doubledoordev.pay2spawn.types.TypeBase;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Allows testing of rewards
 *
 * @author Dries007
 */
public class TestMessage implements IMessage
{
    private String     name;
    private JsonObject data;

    public TestMessage()
    {

    }

    public TestMessage(String name, JsonObject data)
    {
        this.name = name;
        this.data = data;
    }

    public static void sendToServer(String name, JsonObject data)
    {
        if (Minecraft.getMinecraft().isGamePaused()) Helper.msg(EnumChatFormatting.RED + "Some tests don't work while paused! Use your chat key to lose focus.");
        Pay2Spawn.getSnw().sendToServer(new TestMessage(name, data));
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        name = ByteBufUtils.readUTF8String(buf);
        data = JSON_PARSER.parse(ByteBufUtils.readUTF8String(buf)).getAsJsonObject();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        ByteBufUtils.writeUTF8String(buf, data.toString());
    }

    public static class Handler implements IMessageHandler<TestMessage, IMessage>
    {
        @Override
        public IMessage onMessage(TestMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                RndVariable.reset();

                NBTTagCompound rewardData = new NBTTagCompound();
                Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "Testing reward " + message.name + ".");
                Pay2Spawn.getLogger().info("Test by " + ctx.getServerHandler().playerEntity.getCommandSenderName() + " Type: " + message.name + " Data: " + message.data);
                TypeBase type = TypeRegistry.getByName(message.name);
                NBTTagCompound nbt = JsonNBTHelper.parseJSON(message.data);

                Node node = type.getPermissionNode(ctx.getServerHandler().playerEntity, nbt);
                if (BanHelper.isBanned(node))
                {
                    Helper.sendChatToPlayer(ctx.getServerHandler().playerEntity, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                    Pay2Spawn.getLogger().warn(ctx.getServerHandler().playerEntity.getCommandSenderName() + " tried using globally banned node " + node + ".");
                    return null;
                }
                if (PermissionsHandler.needPermCheck(ctx.getServerHandler().playerEntity) && !PermissionsHandler.hasPermissionNode(ctx.getServerHandler().playerEntity, node))
                {
                    Pay2Spawn.getLogger().warn(ctx.getServerHandler().playerEntity.getDisplayName() + " doesn't have perm node " + node.toString());
                    return null;
                }
                type.spawnServerSide(ctx.getServerHandler().playerEntity, nbt, rewardData);
            }
            return null;
        }
    }
}
