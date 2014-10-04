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
import net.doubledoordev.pay2spawn.types.guis.DropItemTypeGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.INT;
import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;

/**
 * @author Dries007
 */
public class DropItemType extends TypeBase
{
    public static final String TYPE_KEY = "type";
    public static final String NODENAME = "dropitems";

    public static final int HOLDING_1   = 0;
    public static final int HOLDING_ALL = 1;
    public static final int ALL         = 2;
    public static final int ARMOR       = 3;

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(TYPE_KEY, NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger(TYPE_KEY, ALL);
        return compound;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case HOLDING_1:
                player.dropOneItem(false);
                break;
            case HOLDING_ALL:
                player.dropOneItem(true);
                break;
            case ALL:
                player.inventory.dropAllItems();
                break;
            case ARMOR:
                for (int i = 0; i < player.inventory.armorInventory.length; ++i)
                {
                    if (player.inventory.armorInventory[i] != null)
                    {
                        player.dropPlayerItemWithRandomChoice(player.inventory.armorInventory[i], true);
                        player.inventory.armorInventory[i] = null;
                    }
                }
                break;
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new DropItemTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(new Node(NODENAME));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NODENAME);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "type":
                switch (Integer.parseInt(jsonObject.get(TYPE_KEY).getAsString().split(":", 2)[1]))
                {
                    case HOLDING_1:
                        return "one of the selected items";
                    case HOLDING_ALL:
                        return "all of the selected items";
                    case ALL:
                        return "all items";
                    case ARMOR:
                        return "all the armor worn";
                }
        }
        return id;
    }
}
