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

package net.doubledoordev.pay2spawn.util;

import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.network.NbtRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;

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
}
