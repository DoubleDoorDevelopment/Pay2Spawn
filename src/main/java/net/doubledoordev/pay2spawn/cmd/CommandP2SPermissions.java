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

import net.doubledoordev.pay2spawn.permissions.Group;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.doubledoordev.pay2spawn.permissions.Player;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * The main permission system command
 *
 * @author Dries007
 */
public class CommandP2SPermissions extends CommandBase
{
    static final String HELP = "Use client side command 'p2s' for non permissions stuff.";

    @Override
    public String getCommandName()
    {
        return "p2spermissions";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return HELP;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            Helper.sendChatToPlayer(sender, "Use '/p2sperm group|groups|player' for more info.", EnumChatFormatting.RED);
            return;
        }
        switch (args[0])
        {
            case "groups":
                if (args.length < 3)
                {
                    Helper.sendChatToPlayer(sender, "Use '/p2s perm groups add|remove <name> [parent group]' to add or remove a group.", EnumChatFormatting.RED);
                    break;
                }
                else
                {
                    String name = args[2];
                    switch (args[1])
                    {
                        case "add":
                            String parent = args.length == 4 ? args[3] : null;
                            PermissionsHandler.getDB().newGroup(name, parent);
                            Helper.sendChatToPlayer(sender, "Added new group named '" + name + (parent != null ? "' with parent group '" + parent : "") + "'.", EnumChatFormatting.GOLD);
                            break;
                        case "remove":
                            PermissionsHandler.getDB().remove(name);
                            Helper.sendChatToPlayer(sender, "Removed group named '" + name + "'", EnumChatFormatting.GOLD);
                            break;
                    }
                }
                break;
            case "group":
                if (args.length < 4)
                {
                    Helper.sendChatToPlayer(sender, "Use '/p2s perm group <name> add|remove <node>' OR '<name> parent set|clear [name]'", EnumChatFormatting.RED);
                    break;
                }
                else
                {
                    Group group = PermissionsHandler.getDB().getGroup(args[1]);
                    if (group == null)
                    {
                        Helper.sendChatToPlayer(sender, "The group doesn't exist.", EnumChatFormatting.RED);
                        break;
                    }
                    switch (args[2])
                    {
                        case "parent":
                            switch (args[3])
                            {
                                case "set":
                                    if (args.length != 5)
                                    {
                                        Helper.sendChatToPlayer(sender, "Use 'parent set <name>.", EnumChatFormatting.RED);
                                        return;
                                    }
                                    group.setParent(args[4]);
                                    Helper.sendChatToPlayer(sender, "Set parent to: " + args[4], EnumChatFormatting.GOLD);
                                    break;
                                case "clear":
                                    group.setParent(null);
                                    Helper.sendChatToPlayer(sender, "Cleared parent group.", EnumChatFormatting.GOLD);
                                    break;
                            }
                            break;
                        case "add":
                            group.addNode(args[3]);
                            Helper.sendChatToPlayer(sender, "Added node: " + args[3], EnumChatFormatting.GOLD);
                            break;
                        case "remove":
                            if (group.removeNode(args[3])) Helper.sendChatToPlayer(sender, "Removed node: " + args[3], EnumChatFormatting.GOLD);
                            else Helper.sendChatToPlayer(sender, "Node not removed, it wasn't there in the first place...", EnumChatFormatting.RED);
                            break;
                    }
                }
                break;
            case "player":
                if (args.length < 5)
                {
                    Helper.sendChatToPlayer(sender, "Use '/p2s perm player <name> group add|remove <group>' OR '<name> perm add|remove <node>'", EnumChatFormatting.RED);
                    break;
                }
                else
                {
                    Player playero = PermissionsHandler.getDB().getPlayer(args[1]);
                    if (playero == null)
                    {
                        Helper.sendChatToPlayer(sender, "That player doesn't exist.", EnumChatFormatting.RED);
                        break;
                    }
                    switch (args[2])
                    {
                        case "group":
                            switch (args[3])
                            {
                                case "add":
                                    playero.addGroup(args[4]);
                                    Helper.sendChatToPlayer(sender, "Added " + args[1] + " to " + args[4], EnumChatFormatting.GOLD);
                                    break;
                                case "remove":
                                    if (playero.removeGroup(args[4])) Helper.sendChatToPlayer(sender, "Removed group: " + args[4], EnumChatFormatting.GOLD);
                                    else Helper.sendChatToPlayer(sender, "Group not removed, it wasn't there in the first place...", EnumChatFormatting.RED);
                                    break;
                            }
                            break;
                        case "perm":
                            switch (args[3])
                            {
                                case "add":
                                    playero.addNode(new Node(args[4]));
                                    break;
                                case "remove":
                                    if (playero.removeNode(new Node(args[4]))) Helper.sendChatToPlayer(sender, "Added per node: " + args[4], EnumChatFormatting.GOLD);
                                    else Helper.sendChatToPlayer(sender, "Perm node not removed, it wasn't there in the first place...", EnumChatFormatting.RED);
                                    break;
                            }
                            break;
                    }
                }
                break;
        }
        PermissionsHandler.getDB().save();
    }

    @Override
    public List getCommandAliases()
    {
        return Arrays.asList("p2sperm");
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
                return getListOfStringsMatchingLastWord(args, "groups", "group", "player");
            case 2:
                switch (args[0])
                {
                    case "groups":
                        return getListOfStringsMatchingLastWord(args, "add", "remove");
                    case "group":
                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                    case "player":
                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayers());
                }
                break;
            case 3:
                switch (args[0])
                {
                    case "groups":
                        if (args[1].equals("remove")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                        break;
                    case "group":
                        return getListOfStringsMatchingLastWord(args, "parent", "add", "remove");
                    case "player":
                        return getListOfStringsMatchingLastWord(args, "group", "perm");
                }
                break;
            case 4:
                switch (args[0])
                {
                    case "groups":
                        if (args[1].equals("add")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                    case "group":
                        switch (args[2])
                        {
                            case "parent":
                                return getListOfStringsMatchingLastWord(args, "set", "clear");
                            case "add":
                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getAllPermNodes());
                            case "remove":
                                return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroup(args[2]).getNodes());
                        }
                        break;
                    case "player":
                        switch (args[2])
                        {
                            case "group":
                                return getListOfStringsMatchingLastWord(args, "add", "remove");
                            case "perm":
                                return getListOfStringsMatchingLastWord(args, "add", "remove");
                        }
                        break;
                }
                break;
            case 5:
                switch (args[0])
                {
                    case "group":
                        if (args[2].equals("parent") && args[3].equals("set")) return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                        break;
                    case "player":
                        switch (args[2])
                        {
                            case "group":
                                switch (args[3])
                                {
                                    case "add":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getGroups());
                                    case "remove":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayer(args[1]).getGroups());
                                }
                                break;
                            case "perm":
                                switch (args[3])
                                {
                                    case "add":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getAllPermNodes());
                                    case "remove":
                                        return getListOfStringsFromIterableMatchingLastWord(args, PermissionsHandler.getDB().getPlayer(args[1]).getNodes());
                                }
                                break;
                        }
                        break;
                }
                break;
        }
        return null;
    }
}
