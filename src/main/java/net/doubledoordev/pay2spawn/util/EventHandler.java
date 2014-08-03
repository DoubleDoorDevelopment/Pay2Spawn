/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
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

package net.doubledoordev.pay2spawn.util;

import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameData;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.network.NbtRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;
import static net.doubledoordev.pay2spawn.util.Constants.PERKS_URL;

/**
 * Handler for all forge events.
 *
 * @author Dries007
 */
public class EventHandler
{
    static boolean entityTracking = false, blockTracking = false;
    private JsonObject perks;

    public EventHandler()
    {
        try
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void addEntityTracking()
    {
        entityTracking = true;
    }

    public static void addBlockTracker()
    {
        blockTracking = true;
    }

    @SubscribeEvent
    public void event(PlayerInteractEvent e)
    {
        if (blockTracking)
        {
            blockTracking = false;

            NbtRequestMessage.requestBlock(e.x, e.y, e.z, e.world.provider.dimensionId);

            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void event(EntityInteractEvent event)
    {
        if (entityTracking)
        {
            entityTracking = false;
            NbtRequestMessage.requestByEntityID(event.target.getEntityId());
        }
    }

    @SubscribeEvent
    public void hudEvent(RenderGameOverlayEvent.Text event)
    {
        ArrayList<String> bottomLeft = new ArrayList<>();
        ArrayList<String> bottomRight = new ArrayList<>();

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        Hud.INSTANCE.render(event.left, event.right, bottomLeft, bottomRight);

        int baseHeight = event.resolution.getScaledHeight() - 25 - bottomLeft.size() * 10;
        if (!Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen())
        {
            for (int x = 0; x < bottomLeft.size(); x++)
            {
                String msg = bottomLeft.get(x);
                fontRenderer.drawStringWithShadow(msg, 2, baseHeight + 2 + x * 10, 0xFFFFFF);
            }
        }

        baseHeight = event.resolution.getScaledHeight() - 25 - bottomRight.size() * 10;
        if (!Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen())
        {
            for (int x = 0; x < bottomRight.size(); x++)
            {
                String msg = bottomRight.get(x);
                int w = fontRenderer.getStringWidth(msg);
                fontRenderer.drawStringWithShadow(msg, event.resolution.getScaledWidth() - w - 10, baseHeight + 2 + x * 10, 0xFFFFFF);
            }
        }
    }

    /**
     * Something other than capes for once
     */
    @SubscribeEvent
    public void nameFormatEvent(PlayerEvent.NameFormat event)
    {
        if (!Pay2Spawn.getConfig().sillyness) return;
        try
        {
            perks = JSON_PARSER.parse(IOUtils.toString(new URL(PERKS_URL), Charset.forName("UTF-8"))).getAsJsonObject();
            if (perks.has(event.username))
            {
                JsonObject perk = perks.getAsJsonObject(event.username);
                if (perk.has("displayname")) event.displayname = perk.get("displayname").getAsString();
                if (perk.has("hat") && (event.entityPlayer.inventory.armorInventory[3] == null || event.entityPlayer.inventory.armorInventory[3].stackSize == 0)) event.entityPlayer.inventory.armorInventory[3] = new ItemStack(GameData.getBlockRegistry().getObject(perk.get("hat").getAsString()), 0, perk.has("hat_meta") ? perk.get("hat_meta").getAsInt() : 0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
