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
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

/**
 * Used for status things
 * - Handshake
 * - Forced config
 * - Enable or disable
 * - Status check
 *
 * @author Dries007
 */
public class StatusMessage implements IMessage
{
    public static String serverConfig;
    private Type     type;
    private String[] extraData;

    public StatusMessage(Type type, String... extraData)
    {
        this.type = type;
        this.extraData = extraData;
    }

    public StatusMessage()
    {

    }

    public static void sendHandshakeToPlayer(EntityPlayerMP player)
    {
        Pay2Spawn.getSnw().sendTo(new StatusMessage(Type.HANDSHAKE), player);
    }

    private static void sendForceToPlayer(EntityPlayerMP player)
    {
        Pay2Spawn.getSnw().sendTo(new StatusMessage(Type.FORCE), player);
    }

    private static void sendConfigToPlayer(EntityPlayerMP player)
    {
        Pay2Spawn.getSnw().sendTo(new StatusMessage(Type.CONFIGSYNC, serverConfig), player);
    }

    public static void sendConfigToAllPlayers()
    {
        Pay2Spawn.getSnw().sendToAll(new StatusMessage(Type.CONFIGSYNC, serverConfig));
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = Type.values()[buf.readInt()];
        extraData = new String[buf.readInt()];
        for (int i = 0; i < extraData.length; i++) extraData[i] = Helper.readLongStringToByteBuf(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(type.ordinal());
        buf.writeInt(extraData.length);
        for (String s : extraData) Helper.writeLongStringToByteBuf(buf, s);
    }

    public static enum Type
    {
        HANDSHAKE,
        CONFIGSYNC,
        FORCE,
        STATUS,
        SALE
    }

    public static class Handler implements IMessageHandler<StatusMessage, IMessage>
    {
        @Override
        public IMessage onMessage(StatusMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                switch (message.type)
                {
                    case HANDSHAKE:
                        return new StatusMessage(Type.HANDSHAKE);
                    case CONFIGSYNC:
                        Pay2Spawn.reloadDBFromServer(message.extraData[0]);
                        ConfiguratorManager.exit();
                        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Using config specified by the server.");
                        break;
                    case FORCE:
                        Pay2Spawn.forceOn = true;
                        break;
                    case STATUS:
                        return new StatusMessage(Type.STATUS, message.extraData[0], Boolean.toString(Pay2Spawn.enable));
                    case SALE:
                        Pay2Spawn.getRewardsDB().addSale(Integer.parseInt(message.extraData[0]), Integer.parseInt(message.extraData[1]));
                        break;
                }
            }
            else
            {
                switch (message.type)
                {
                    case HANDSHAKE:
                        PermissionsHandler.getDB().newPlayer(ctx.getServerHandler().playerEntity.getCommandSenderName());
                        Pay2Spawn.playersWithValidConfig.add(ctx.getServerHandler().playerEntity.getCommandSenderName());
                        // Can't use return statement here cause you can't return multiple packets
                        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceServerconfig) sendConfigToPlayer(ctx.getServerHandler().playerEntity);
                        if (MinecraftServer.getServer().isDedicatedServer() && Pay2Spawn.getConfig().forceP2S) sendForceToPlayer(ctx.getServerHandler().playerEntity);
                        break;
                    case STATUS:
                        EntityPlayer sender = MinecraftServer.getServer().getConfigurationManager().func_152612_a(message.extraData[0]);
                        Helper.sendChatToPlayer(sender, ctx.getServerHandler().playerEntity.getCommandSenderName() + " has Pay2Spawn " + (Boolean.parseBoolean(message.extraData[1]) ? "enabled." : "disabled."), EnumChatFormatting.AQUA);
                        break;
                }
            }
            return null;
        }
    }
}
