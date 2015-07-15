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
import net.doubledoordev.pay2spawn.types.guis.DeleteworldTypeGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;
import static net.doubledoordev.pay2spawn.util.Constants.STRING;

/**
 * This should be !FUN!
 *
 * @author Dries007
 */
public class DeleteworldType extends TypeBase
{
    public static final  String                  MESSAGE_KEY = "message";
    public static final  HashMap<String, String> typeMap     = new HashMap<>();
    private static final String                  NAME        = "deleteworld";

    static
    {
        typeMap.put(MESSAGE_KEY, NBTTypes[STRING]);
    }

    public static String DEFAULTMESSAGE = "A Pay2Spawn donation deleted the world.\\nGoodbye!";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        nbtTagCompound.setString(MESSAGE_KEY, DEFAULTMESSAGE);
        return nbtTagCompound;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        for (int i = 0; i < MinecraftServer.getServer().getConfigurationManager().playerEntityList.size(); ++i)
        {
            ((EntityPlayerMP) MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(i)).playerNetServerHandler.kickPlayerFromServer(dataFromClient.getString(MESSAGE_KEY).replace("\\n", "\n"));
        }
        MinecraftServer.getServer().deleteWorldAndStopServer();
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new DeleteworldTypeGui(rewardID, NAME, data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        return Collections.singletonList(new Node(NAME));
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME);
    }

    @Override
    public boolean isInDefaultConfig()
    {
        return false;
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        return id;
    }
}
