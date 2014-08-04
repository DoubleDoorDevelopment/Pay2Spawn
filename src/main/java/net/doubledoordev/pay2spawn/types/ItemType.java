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
 *  Neither the name of DoubleDoorDevelopment nor the names of its
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

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.types.guis.ItemTypeGui;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.INT;
import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;

/**
 * Spawn an itemstack
 * Can handle all custom NBT data
 *
 * @author Dries007
 */
public class ItemType extends TypeBase
{
    public static final String NAME = "item";

    public static final String                  SLOT_KEY = "SLOT";
    public static final HashMap<String, String> typeMap  = new HashMap<>();

    static
    {
        typeMap.put(SLOT_KEY, NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        ItemStack is = new ItemStack((Item) Item.itemRegistry.getObject("minecraft:golden_apple"));
        is.setStackDisplayName("$name");
        return is.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        try
        {
            int id = dataFromClient.hasKey(SLOT_KEY) ? dataFromClient.getInteger(SLOT_KEY) : -1;
            if (id != -1 && player.inventory.getStackInSlot(id) == null)
            {
                player.inventory.setInventorySlotContents(id, ItemStack.loadItemStackFromNBT(dataFromClient));
            }
            else
            {
                EntityItem entityitem = player.dropPlayerItemWithRandomChoice(ItemStack.loadItemStackFromNBT(dataFromClient), false);
                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(player.getCommandSenderName());
            }
        }
        catch (Exception e)
        {
            Pay2Spawn.getLogger().warn("ItemStack could not be spawned. Does the item exists? JSON: " + JsonNBTHelper.parseNBT(dataFromClient));
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new ItemTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (Object itemName : Item.itemRegistry.getKeys())
        {
            nodes.add(new Node(ItemType.NAME, itemName.toString().replace(".", "_")));
        }
        for (Object itemName : Block.blockRegistry.getKeys())
        {
            nodes.add(new Node(ItemType.NAME, itemName.toString().replace(".", "_")));
        }
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME, ItemStack.loadItemStackFromNBT(dataFromClient).getUnlocalizedName().replace(".", "_"));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "stacksize":
                return jsonObject.get("Count").getAsString().replace("BYTE:", "");
            case "itemname":
                ItemStack is = ItemStack.loadItemStackFromNBT(JsonNBTHelper.parseJSON(jsonObject));
                return is.getItem().getItemStackDisplayName(is);
        }
        return id;
    }

    @Override
    public void printHelpList(File configFolder)
    {
        File file = new File(configFolder, "Enchantment.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("Enchantment list file");

            ArrayList<String> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> minlvl = new ArrayList<>();
            ArrayList<String> maxlvl = new ArrayList<>();
            for (Enchantment enchantment : Enchantment.enchantmentsList)
            {
                if (enchantment != null)
                {
                    ids.add(enchantment.effectId + "");
                    names.add(enchantment.getTranslatedName(enchantment.getMinLevel()));
                    minlvl.add(enchantment.getMinLevel() + "");
                    maxlvl.add(enchantment.getMaxLevel() + "");
                }
            }
            pw.print(Helper.makeTable(new Helper.TableData("ID", ids), new Helper.TableData("name", names), new Helper.TableData("minLvl", minlvl), new Helper.TableData("maxLvl", maxlvl)));

            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
