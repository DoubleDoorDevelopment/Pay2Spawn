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

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.hud.CountDownHudEntry;
import net.doubledoordev.pay2spawn.hud.DonationTrainEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.hud.SaleEntry;
import net.doubledoordev.pay2spawn.network.CountdownMessage;
import net.doubledoordev.pay2spawn.permissions.BanHelper;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.doubledoordev.pay2spawn.types.TypeBase;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Client side tick things
 * - countdown exists here
 *
 * @author Dries007
 */
public class CountdownTickHandler
{
    public static final CountdownTickHandler INSTANCE = new CountdownTickHandler();
    public DonationTrainEntry donationTrainEntry;
    public SaleEntry          saleEntry;
    final Set<QueEntry> entries = new HashSet<QueEntry>();
    private CountDownHudEntry countDownHudEntry;
    private int i = 0;

    private CountdownTickHandler()
    {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void tickEvent(TickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.type != TickEvent.Type.CLIENT && event.type != TickEvent.Type.SERVER) return;
        if (i++ != 20) return;
        i = 0;

        if (event.type == TickEvent.Type.CLIENT)
        {
            donationTrainEntry.tick();
            countDownHudEntry.lines.clear();
        }

        synchronized (entries)
        {
            Iterator<QueEntry> rewardIterator = entries.iterator();
            while (rewardIterator.hasNext())
            {
                QueEntry queEntry = rewardIterator.next();
                if (queEntry.remaining == 0)
                {
                    queEntry.end();
                    rewardIterator.remove();
                }
                else
                {
                    if (event.type == TickEvent.Type.CLIENT && countDownHudEntry.getPosition() != 0 && queEntry.addToHUD)
                        countDownHudEntry.lines.add(countDownHudEntry.getFormat().replace("$name", queEntry.name).replace("$time", String.valueOf(queEntry.remaining)));
                    queEntry.remaining--;
                }
            }
        }
        if (event.type == TickEvent.Type.CLIENT && countDownHudEntry.getPosition() != 0 && !countDownHudEntry.lines.isEmpty())
        {
            if (!Strings.isNullOrEmpty(countDownHudEntry.getHeader())) Helper.addWithEmptyLines(countDownHudEntry.lines, countDownHudEntry.getHeader());
        }
    }

    public void add(QueEntry entry)
    {
        if (FMLCommonHandler.instance().getSide().isServer()) Pay2Spawn.getSnw().sendToAll(new CountdownMessage(entry));
        synchronized (entries)
        {
            entries.add(entry);
        }
    }

    public void init()
    {
        if (FMLCommonHandler.instance().getSide().isClient())
        {
            countDownHudEntry = new CountDownHudEntry("countdown", 1, "$name incoming in $time sec.", "-- Countdown --");
            Hud.INSTANCE.set.add(countDownHudEntry);

            donationTrainEntry = new DonationTrainEntry();
            Hud.INSTANCE.set.add(donationTrainEntry);

            saleEntry = new SaleEntry();
            Hud.INSTANCE.set.add(saleEntry);
        }
    }

    public static class QueEntry
    {
        public final String   name;
        public final boolean  addToHUD;
        public int      remaining;

        public QueEntry(String name, int remaining, boolean addToHUD)
        {
            this.name = name;
            this.remaining = remaining;
            this.addToHUD = addToHUD;
        }

        public void end()
        {

        }
    }

    public static class ServerQueEntry extends QueEntry
    {
        Donation donation;
        Reward   reward;
        Reward   actualReward;

        public ServerQueEntry(Reward reward, Donation donation, boolean addToHUD, Reward actualReward)
        {
            super(reward.getName(), reward.getCountdown(), addToHUD);
            this.donation = donation;
            this.reward = reward;
            this.actualReward = actualReward;
        }

        public void end()
        {
            EntityPlayerMP playerMP = MinecraftServer.getServer().getConfigurationManager().func_152612_a(donation.target);
            if (playerMP == null)
            {
                Pay2Spawn.getLogger().warn("ERROR TYPE 3: Error spawning a reward on the server.");
                Pay2Spawn.getLogger().warn("Target was not a valid EntityPlayerMP: " + donation.target);
                return;
            }
            NBTTagCompound rewardData = new NBTTagCompound();
            rewardData.setString("name", reward.getName());
            rewardData.setDouble("amount", reward.getAmount());
            // replace all the dummy data with the correct data by converting the JsonArry to a string and then parsing that string to a new JsonArray
            JsonArray rewards = JSON_PARSER.parse(Helper.formatText(reward.getRewards(), donation, actualReward == null ? reward : actualReward).toString()).getAsJsonArray();
            for (JsonElement reward : rewards)
            {
                NBTTagCompound rewardNtb = JsonNBTHelper.parseJSON(reward.getAsJsonObject());
                TypeBase type = TypeRegistry.getByName(rewardNtb.getString("type").toLowerCase());
                try
                {
                    NBTTagCompound nbt = rewardNtb.getCompoundTag("data");
                    type.addConfigTags(nbt, donation, actualReward == null ? this.reward : actualReward);

                    type.spawnServerSide(playerMP, nbt, rewardData);
                }
                catch (Exception e)
                {
                    Pay2Spawn.getLogger().warn("ERROR TYPE 3: Error spawning a reward on the server.");
                    Pay2Spawn.getLogger().warn("Type: " + rewardNtb.getString("type").toLowerCase());
                    Pay2Spawn.getLogger().warn("Data: " + rewardNtb.getCompoundTag("data"));
                    e.printStackTrace();
                }
            }
        }
    }
}
