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

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.util.Helper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Timer;
import java.util.TimerTask;

import static net.doubledoordev.pay2spawn.util.Constants.NAME;

/**
 * Oh god its event based now -_-
 *
 * @author Dries007
 */
public class ConnectionHandler
{
    public static final ConnectionHandler INSTANCE = new ConnectionHandler();

    private ConnectionHandler()
    {
        FMLCommonHandler.instance().bus().register(this);
    }

    public void init()
    {

    }

    @SubscribeEvent
    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP) // Cheap server detection
            StatusMessage.sendHandshakeToPlayer((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void connectionReceived(FMLNetworkEvent.ServerConnectionFromClientEvent event)
    {
        if (!event.isLocal && Pay2Spawn.getConfig().forceP2S)
        {
            final String username = ((NetHandlerPlayServer) event.handler).playerEntity.getCommandSenderName();
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (!StatusMessage.doesPlayerHaveValidConfig(username)) MinecraftServer.getServer().getConfigurationManager().func_152612_a(username).playerNetServerHandler.kickPlayerFromServer("Pay2Spawn is required on this server.\nIt needs to be configured properly.");
                }
            }, 5 * 1000);
        }
    }

    @SubscribeEvent
    public void disconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Pay2Spawn.reloadDB();
        StatusMessage.resetServerStatus();
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (!StatusMessage.doesServerHaveMod()) Helper.msg(EnumChatFormatting.RED + NAME + " isn't on the server. No rewards will spawn!");
            }
        }, 5 * 1000);
    }
}
