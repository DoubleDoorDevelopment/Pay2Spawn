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

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.doubledoordev.pay2spawn.types.guis.ItemsTypeGui;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.doubledoordev.pay2spawn.util.Reward;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Upgraded version of the Item reward
 *
 * @author Dries007
 */
public class ItemsType extends TypeBase
{
    public static final String NAME = "items";

    public static final String                  SLOT_KEY = "SLOT";
    public static final String                  WEIGHT_KEY = "WEIGHT";
    public static final String                  ITEMS_KEY = "ITEMS";
    public static final String                  MODE_KEY = "MODE";

    public static final byte                    MODE_ALL = 0; // default mode
    public static final byte                    MODE_PICK_ONE = 1;

    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(SLOT_KEY, NBTTypes[INT]);
        typeMap.put(WEIGHT_KEY, NBTTypes[INT]);
        typeMap.put(MODE_KEY, NBTTypes[BYTE]);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList items = new NBTTagList();
        {
            ItemStack itemStack = new ItemStack(Items.golden_apple);
            itemStack.setStackDisplayName("$name");
            NBTTagCompound itemNbt = itemStack.writeToNBT(new NBTTagCompound());
            itemNbt.setInteger(WEIGHT_KEY, 3);
            items.appendTag(itemNbt);
        }
        {
            ItemStack itemStack = new ItemStack(Items.record_13);
            itemStack.setStackDisplayName("$name");
            NBTTagCompound itemNbt = itemStack.writeToNBT(new NBTTagCompound());
            items.appendTag(itemNbt);
        }
        {
            ItemStack itemStack = new ItemStack(Items.golden_carrot);
            itemStack.setStackDisplayName("$name");
            NBTTagCompound itemNbt = itemStack.writeToNBT(new NBTTagCompound());
            items.appendTag(itemNbt);
        }
        root.setTag(ITEMS_KEY, items);
        root.setByte(MODE_KEY, MODE_PICK_ONE);
        return root;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        if (dataFromClient.getByte(MODE_KEY) == MODE_ALL)
        {
            NBTTagList tagList = dataFromClient.getTagList(ITEMS_KEY, COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i ++)
            {
                spawnItemStackOnPlayer(player, tagList.getCompoundTagAt(i));
            }
        }
        else if (dataFromClient.getByte(MODE_KEY) == MODE_PICK_ONE)
        {
            ArrayList<NBTTagCompound> stacks = new ArrayList<>();
            NBTTagList tagList = dataFromClient.getTagList(ITEMS_KEY, COMPOUND);
            for (int i = 0; i < tagList.tagCount(); i ++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                if (!tag.hasKey(WEIGHT_KEY)) stacks.add(tag);
                else for (int j = 0; j < tag.getInteger(WEIGHT_KEY); j++) stacks.add(tag);
            }
            spawnItemStackOnPlayer(player, RandomRegistry.getRandomFromSet(stacks));
        }
    }

    @Override
    public void addConfigTags(NBTTagCompound rewardNtb, Donation donation, Reward reward)
    {
        NBTTagList tagList = rewardNtb.getTagList(ITEMS_KEY, COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i ++) setConfigTags(tagList.getCompoundTagAt(i), donation, reward);
    }

    public static void setConfigTags(NBTTagCompound tagCompound, Donation donation, Reward reward)
    {
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(tagCompound);
        if (!itemStack.hasDisplayName() && !Strings.isNullOrEmpty(Pay2Spawn.getConfig().allItemName)) itemStack.setStackDisplayName(Helper.formatText(Pay2Spawn.getConfig().allItemName, donation, reward));
        if (Pay2Spawn.getConfig().allItemLore.length != 0)
        {
            NBTTagCompound root = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
            itemStack.setTagCompound(root);
            NBTTagCompound display = root.getCompoundTag("display");
            root.setTag("display", display);
            if (!display.hasKey("Lore"))
            {
                NBTTagList lore = new NBTTagList();
                for (String line : Pay2Spawn.getConfig().allItemLore) lore.appendTag(new NBTTagString(Helper.formatText(line, donation, reward)));
                display.setTag("Lore", lore);
            }
        }
        itemStack.writeToNBT(tagCompound);
    }

    public static void spawnItemStackOnPlayer(EntityPlayerMP player, NBTTagCompound dataFromClient)
    {
        try
        {
            ItemStack itemStack = ItemStack.loadItemStackFromNBT(dataFromClient);
            itemStack.stackSize = ((NBTBase.NBTPrimitive) dataFromClient.getTag("Count")).func_150287_d();
            while (itemStack.stackSize != 0)
            {
                ItemStack itemStack1 = itemStack.splitStack(Math.min(itemStack.getMaxStackSize(), itemStack.stackSize));
                int id = dataFromClient.hasKey(SLOT_KEY) ? dataFromClient.getInteger(SLOT_KEY) : -1;
                if (id != -1 && player.inventory.getStackInSlot(id) == null)
                {
                    player.inventory.setInventorySlotContents(id, itemStack1);
                }
                else
                {
                    EntityItem entityitem = player.dropPlayerItemWithRandomChoice(itemStack1, false);
                    entityitem.delayBeforeCanPickup = 0;
                    entityitem.func_145797_a(player.getCommandSenderName());
                }
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
        new ItemsTypeGui(rewardID, NAME, data, typeMap);
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
        return new Node(NAME);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "mode":
                return jsonObject.has(MODE_KEY) && jsonObject.get(MODE_KEY).getAsString().replace("BYTE:", "").equals(String.valueOf(MODE_ALL)) ? "all" : "one";
            case "items":
                JsonArray array = jsonObject.getAsJsonArray(ITEMS_KEY);
                StringBuilder sb = new StringBuilder(array.size() * 20);
                for (int i = 0; i < array.size(); i++)
                {
                    sb.append(ItemStack.loadItemStackFromNBT(JsonNBTHelper.parseJSON(array.get(i).getAsJsonObject())));
                }
                return sb.toString();
        }
        return id;
    }
}
