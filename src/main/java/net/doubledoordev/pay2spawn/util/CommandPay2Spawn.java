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

package net.doubledoordev.pay2spawn.util;

import com.google.common.collect.ImmutableList;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.trackers.Trackers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Dries007
 */
public class CommandPay2Spawn extends CommandBase
{
    private final Side side;
    private final String name;
    private final ImmutableList<String> aliases;

    public CommandPay2Spawn(Side side, String name, String... aliases)
    {
        this.side = side;
        this.name = name;
        this.aliases = ImmutableList.copyOf(aliases);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Control pay2spawn on the " + side.name().toLowerCase();
    }

    @Override
    public List<String> getAliases()
    {
        return aliases;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return side.isClient() || super.checkPermission(server, sender);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "help", "reload", "test");
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0) help(sender);
        else if (args[0].equalsIgnoreCase("help")) help(sender);
        else if (args[0].equalsIgnoreCase("reload"))
        {
            try
            {
                Pay2Spawn.reload();
                Helper.chat(sender, "Reloaded", TextFormatting.GREEN);
            }
            catch (IOException e)
            {
                Helper.chat(sender, "Reload failed!", TextFormatting.RED);
                e.printStackTrace();
                throw new CommandException(e.getMessage());
            }
        }
        else if (args[0].equalsIgnoreCase("test"))
        {
            if (args.length < 2) throw new CommandException("Missing amount argument");
            double amount = parseDouble(args[1], 0);
            String name = args.length > 2 ? args[2] : Helper.ANON;
            String note = args.length > 3 ? buildString(args, 3) : "";
            RewardDB.process(sender, new Donation(name, amount, System.currentTimeMillis(), note));
        }
        else help(sender);
//
//        //fixme: remove example!
//
//        String name = Helper.randomString(5 + Helper.RANDOM.nextInt(10));
//        double amount = Math.round(Helper.RANDOM.nextDouble() * 10000.0) / 100.0;
//        String note = Helper.randomString(25 + Helper.RANDOM.nextInt(25));
//        RewardDB.process(new Donation(name, amount, System.currentTimeMillis(), note));
    }

    private void help(ICommandSender sender)
    {
        Helper.chat(sender, "Pay2Spawn sub-commands help (" + side + ")", TextFormatting.GREEN);
        Helper.chat(sender, "- help -> Display this help");
        Helper.chat(sender, "- reload -> Reload config and rewards");
        Helper.chat(sender, "- test <amount> [name] [note] -> Run a test donation");
    }
}
