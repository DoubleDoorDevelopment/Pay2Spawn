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

package net.doubledoordev.pay2spawn.cmd;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.ai.CustomAI;
import net.doubledoordev.pay2spawn.util.Constants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Arrays;
import java.util.List;

/**
 * The server side only command
 *
 * @author Dries007
 */
public class CommandP2SServer extends CommandBase
{
    static final String HELP = "OP only command, Server side.";

    @Override
    public String getCommandName()
    {
        return "pay2spawnserver";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return HELP;
    }

    @Override
    public void processCommand(final ICommandSender sender, String[] args)
    {
        // todo: DEBUG
        // CustomAI.INSTANCE.test(getCommandSenderAsPlayer(sender));

        if (args.length == 0)
        {
            sendChatToPlayer(sender, HELP, EnumChatFormatting.AQUA);
            sendChatToPlayer(sender, "Protip: Use tab completion!", EnumChatFormatting.AQUA);
            return;
        }
        switch (args[0])
        {
            case "butcher":
            {
                sendChatToPlayer(sender, "Removing all spawned entities...", EnumChatFormatting.YELLOW);
                int count = 0;
                for (WorldServer world : DimensionManager.getWorlds())
                {
                    for (Entity entity : (List<Entity>) world.loadedEntityList)
                    {
                        if (entity.getEntityData().hasKey(Constants.NAME))
                        {
                            count++;
                            entity.setDead();
                        }
                    }
                }
                sendChatToPlayer(sender, "Removed " + count + " entities.", EnumChatFormatting.GREEN);
                break;
            }
            case "reload":
                if (MinecraftServer.getServer().isDedicatedServer())
                {
                    try
                    {
                        Pay2Spawn.reloadDB_Server();
                    }
                    catch (Exception e)
                    {
                        sendChatToPlayer(sender, "RELOAD FAILED.", EnumChatFormatting.RED);
                        e.printStackTrace();
                    }
                }
                break;
            case "hasmod":
                if (args.length == 1) sendChatToPlayer(sender, "Use '/p2sserver hasmod <player>'.", EnumChatFormatting.RED);
                else sendChatToPlayer(sender, args[1] + (Pay2Spawn.doesPlayerHaveValidConfig(args[1]) ? " does " : " doesn't ") + "have P2S.", EnumChatFormatting.AQUA);
                break;
            default:
                sendChatToPlayer(sender, "Unknown command. Protip: Use tab completion!", EnumChatFormatting.RED);
                break;
        }
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2sserver");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return !(sender instanceof EntityPlayerMP) || MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayerMP) sender).getGameProfile());
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 1:
                return getListOfStringsMatchingLastWord(args, "reload", "hasmod", "butcher");
            case 2:
                switch (args[1])
                {
                    case "hasmod":
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                }
        }
        return null;
    }

    public void sendChatToPlayer(ICommandSender sender, String message, EnumChatFormatting chatFormatting)
    {
        sender.addChatMessage(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(chatFormatting)));
    }
}
