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

import com.google.common.base.Throwables;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.checkers.CheckerHandler;
import net.doubledoordev.pay2spawn.checkers.TwitchChecker;
import net.doubledoordev.pay2spawn.configurator.ConfiguratorManager;
import net.doubledoordev.pay2spawn.configurator.HTMLGenerator;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.Statistics;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Useful command when dealing with setting up the JSON file
 * Can get an entities/items JSONified NBT
 * Can reload the JSON file
 *
 * @author Dries007
 */
public class CommandP2S extends CommandBase
{
    static final String HELP = "Use command to control P2S Client side.";
    static Timer timer;

    @Override
    public String getCommandName()
    {
        return "pay2spawn";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return HELP;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            Helper.msg(EnumChatFormatting.AQUA + HELP);
            Helper.msg(EnumChatFormatting.AQUA + "Protip: Use tab completion!");
            return;
        }
        switch (args[0])
        {
            case "reload":
                if (Pay2Spawn.getRewardsDB().editable)
                {
                    Pay2Spawn.reloadDB();
                    Helper.msg(EnumChatFormatting.GREEN + "Reload done!");
                }
                else Helper.msg(EnumChatFormatting.RED + "[P2S] If you are OP, use the server side command for this.");
                break;
            case "configure":
                if (Pay2Spawn.getRewardsDB().editable) ConfiguratorManager.openCfg();
                else Helper.msg(EnumChatFormatting.RED + "[P2S] You can't do that with a server side config.");
                break;
            case "getnbt":
                ConfiguratorManager.openNbt();
                break;
            case "makehtml":
                try
                {
                    HTMLGenerator.generate();
                }
                catch (IOException e)
                {
                    Throwables.propagate(e);
                }
                break;
            case "off":
                if (Pay2Spawn.forceOn) Helper.msg(EnumChatFormatting.RED + "Forced on by server.");
                else
                {
                    Pay2Spawn.enable = false;
                    Helper.msg(EnumChatFormatting.GOLD + "[P2S] Disabled on the client.");
                }
                break;
            case "on":
                if (Pay2Spawn.forceOn) Helper.msg(EnumChatFormatting.RED + "Forced on by server.");
                else
                {
                    Pay2Spawn.enable = true;
                    Helper.msg(EnumChatFormatting.GOLD + "[P2S] Enabled on the client.");
                }
                break;
            case "donate":
                if (args.length == 1) Helper.msg(EnumChatFormatting.RED + "Use '/p2s donate <amount> [name]'.");
                else
                {
                    String name = "Anonymous";
                    if (args.length > 2) name = args[2];
                    double amount = CommandBase.parseDouble(sender, args[1]);
                    CheckerHandler.fakeDonation(amount, name);
                }
                break;
            case "adjusttotal":
                if (args.length == 1) Helper.msg(EnumChatFormatting.RED + "Use '/p2s adjusttotal <amount>'. You can use + and -");
                else
                {
                    double amount = CommandBase.parseDouble(sender, args[1]);
                    Statistics.addToDonationAmount(amount);
                }
                break;
            case "resetsubs":
                TwitchChecker.INSTANCE.reset();
                Helper.msg(EnumChatFormatting.GOLD + "[P2S] Subs have been resetted!");
                break;
            case "test":
                if (args.length == 1) Helper.msg(EnumChatFormatting.RED + "Use '/p2s test <amount> <repeat delay in sec> [name]' use '/p2s test end' to stop the testing.");
                else
                {
                    if (args[1].equalsIgnoreCase("end") && timer != null)
                    {
                        timer.cancel();
                    }
                    else if (args.length > 2)
                    {
                        final String name;
                        final Double amount = CommandBase.parseDouble(sender, args[1]);
                        final Integer delay = CommandBase.parseInt(sender, args[2]) * 1000;
                        if (args.length > 3) name = args[3];
                        else name = "Anonymous";
                        timer = new Timer();
                        timer.scheduleAtFixedRate(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                CheckerHandler.fakeDonation(amount, name);
                            }
                        }, 0, delay);
                    }
                    else Helper.msg(EnumChatFormatting.RED + "Use '/p2s test <amount> <repeat delay in sec> [name]' use '/p2s test end' to stop the testing.");
                }
                break;
            default:
                Helper.msg(EnumChatFormatting.RED + "Unknown command. Protip: Use tab completion!");
                break;
        }
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2s");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "reload", "configure", "getnbt", "makehtml", "off", "on", "donate", "permissions", "adjusttotal", "test");
        return null;
    }
}
