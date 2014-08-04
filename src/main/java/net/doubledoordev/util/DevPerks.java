/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 DoubleDoorDevelopment
 *
 * I can't demand this, but I ask for respect and gratitude for the time and effort
 * put into the project by all developers, testers, designers and documenters. ~~Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.doubledoordev.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.Charset;

/**
 * Something other than capes for once
 *
 * @author Dries007
 */
public class DevPerks
{
    private static final String PERKS_URL = "http://doubledoordev.net/perks.json";
    private JsonObject  perks = new JsonObject();
    private boolean debug;

    public DevPerks(boolean debug)
    {
        this.debug = debug;
        try
        {
            perks = new JsonParser().parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8"))).getAsJsonObject();
        }
        catch (Exception e)
        {
            if (debug) e.printStackTrace();
        }
    }

    /**
     * Something other than capes for once
     */
    @SubscribeEvent
    public void nameFormatEvent(PlayerEvent.NameFormat event)
    {
        try
        {
            if (debug) perks = new JsonParser().parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8"))).getAsJsonObject();
            if (perks.has(event.username))
            {
                JsonObject perk = perks.getAsJsonObject(event.username);
                if (perk.has("displayname")) event.displayname = perk.get("displayname").getAsString();
                if (perk.has("hat") && (event.entityPlayer.inventory.armorInventory[3] == null || event.entityPlayer.inventory.armorInventory[3].stackSize == 0))
                {
                    JsonObject hat = perk.getAsJsonObject("hat");
                    String name = hat.get("name").getAsString();
                    int meta = hat.has("meta") ? hat.get("meta").getAsInt() : 0;
                    event.entityPlayer.inventory.armorInventory[3] = new ItemStack(GameData.getItemRegistry().getObject(name), 0, meta);
                }
            }
        }
        catch (Exception e)
        {
            if (debug) e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void nameFormatEvent(PlayerEvent.Clone event)
    {
        try
        {
            if (debug) perks = new JsonParser().parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8"))).getAsJsonObject();
            if (perks.has(event.original.getCommandSenderName()))
            {
                JsonObject perk = perks.getAsJsonObject(event.original.getCommandSenderName());
                if (perk.has("hat") && (event.entityPlayer.inventory.armorInventory[3] == null || event.entityPlayer.inventory.armorInventory[3].stackSize == 0))
                {
                    JsonObject hat = perk.getAsJsonObject("hat");
                    String name = hat.get("name").getAsString();
                    int meta = hat.has("meta") ? hat.get("meta").getAsInt() : 0;
                    event.entityPlayer.inventory.armorInventory[3] = new ItemStack(GameData.getItemRegistry().getObject(name), 0, meta);
                }
            }
        }
        catch (Exception e)
        {
            if (debug) e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void deathEvent(PlayerDropsEvent event)
    {
        try
        {
            if (debug) perks = new JsonParser().parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8"))).getAsJsonObject();
            if (perks.has(event.entityPlayer.getCommandSenderName()))
            {
                JsonObject perk = perks.getAsJsonObject(event.entityPlayer.getCommandSenderName());
                if (perk.has("drop"))
                {
                    JsonObject drop = perk.getAsJsonObject("drop");
                    String name = drop.get("name").getAsString();
                    int meta = drop.has("meta") ? drop.get("meta").getAsInt() : 0;
                    int size = drop.has("size") ? drop.get("size").getAsInt() : 1;
                    event.drops.add(new EntityItem(event.entityPlayer.getEntityWorld(), event.entityPlayer.posX, event.entityPlayer.posY, event.entityPlayer.posZ, new ItemStack(GameData.getItemRegistry().getObject(name), size, meta)));
                }
            }
        }
        catch (Exception e)
        {
            if (debug) e.printStackTrace();
        }
    }
}
