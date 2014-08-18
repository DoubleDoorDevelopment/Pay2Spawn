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

package net.doubledoordev.pay2spawn.types;

import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.types.guis.CommandTypeGui;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.config.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class CommandType extends TypeBase
{
    public static final  String                  COMMAND_KEY = "command";
    public static final  HashMap<String, String> typeMap     = new HashMap<>();
    public static final  HashSet<String>         commands    = new HashSet<>();
    private static final String                  NAME        = "command";

    static
    {
        typeMap.put(COMMAND_KEY, NBTTypes[STRING]);
    }

    public boolean feedback = true;

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(COMMAND_KEY, "weather clear");
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        MinecraftServer.getServer().getCommandManager().executeCommand(new cmdSender((EntityPlayerMP) player), dataFromClient.getString(COMMAND_KEY));
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new CommandTypeGui(rewardID, NAME, data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null)
        {
            for (Object o : server.getCommandManager().getCommands().values())
            {
                ICommand command = (ICommand) o;
                commands.add(command.getCommandName());
                nodes.add(new Node(NAME, command.getCommandName()));
            }
        }
        else
        {
            nodes.add(new Node(NAME));
        }

        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME, dataFromClient.getString(COMMAND_KEY).split(" ")[0]);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "cmd":
                return jsonObject.get(COMMAND_KEY).getAsString().replace(typeMap.get(COMMAND_KEY) + ":", "");
        }
        return id;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(TYPES_CAT, "Reward config options");
        configuration.addCustomCategoryComment(TYPES_CAT + '.' + NAME, "Used for commands");
        feedback = configuration.get(TYPES_CAT + '.' + NAME, "feedback", feedback, "Disable command feedback. (server overrides client)").getBoolean(feedback);
    }

    public class cmdSender extends EntityPlayerMP
    {
        public cmdSender(EntityPlayerMP player)
        {
            super(player.mcServer, player.getServerForPlayer(), player.getGameProfile(), player.theItemInWorldManager);
            this.theItemInWorldManager.thisPlayerMP = player;
            this.playerNetServerHandler = player.playerNetServerHandler;
        }

        @Override
        public boolean canCommandSenderUseCommand(int par1, String cmd)
        {
            return true;
        }

        @Override
        public void addChatComponentMessage(IChatComponent p_146105_1_)
        {
            if (feedback) super.addChatComponentMessage(p_146105_1_);
        }
    }
}
