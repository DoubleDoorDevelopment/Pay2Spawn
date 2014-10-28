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
import net.doubledoordev.pay2spawn.network.RewardMessage;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashSet;
import java.util.Iterator;

import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Client side tick things
 * - countdown exists here
 *
 * @author Dries007
 */
public class ClientTickHandler
{
    public static final ClientTickHandler INSTANCE = new ClientTickHandler();
    public DonationTrainEntry donationTrainEntry;
    public SaleEntry          saleEntry;
    HashSet<QueEntry> entries = new HashSet<>();
    private CountDownHudEntry countDownHudEntry;
    private int i = 0;

    private ClientTickHandler()
    {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        if (i++ != 20) return;
        i = 0;

        donationTrainEntry.tick();
        countDownHudEntry.lines.clear();

        Iterator<QueEntry> rewardIterator = entries.iterator();
        while (rewardIterator.hasNext())
        {
            QueEntry queEntry = rewardIterator.next();
            if (queEntry.remaining == 0)
            {
                queEntry.send();
                rewardIterator.remove();
            }
            else
            {
                if (countDownHudEntry.getPosition() != 0 && queEntry.addToHUD) countDownHudEntry.lines.add(countDownHudEntry.getFormat().replace("$name", queEntry.reward.getName()).replace("$time", queEntry.remaining + ""));
                queEntry.remaining--;
            }
        }
        if (countDownHudEntry.getPosition() != 0 && !countDownHudEntry.lines.isEmpty())
        {
            if (!Strings.isNullOrEmpty(countDownHudEntry.getHeader())) Helper.addWithEmptyLines(countDownHudEntry.lines, countDownHudEntry.getHeader());
        }
    }

    public void add(Reward reward, Donation donation, boolean addToHUD, Reward actualReward)
    {
        entries.add(new QueEntry(reward, donation, addToHUD, actualReward));
    }

    public void init()
    {
        countDownHudEntry = new CountDownHudEntry("countdown", 1, "$name incoming in $time sec.", "-- Countdown --");
        Hud.INSTANCE.set.add(countDownHudEntry);

        donationTrainEntry = new DonationTrainEntry();
        Hud.INSTANCE.set.add(donationTrainEntry);

        saleEntry = new SaleEntry();
        Hud.INSTANCE.set.add(saleEntry);
    }

    public class QueEntry
    {
        int      remaining;
        Donation donation;
        Reward   reward;
        Reward   actualReward;
        boolean  addToHUD;

        public QueEntry(Reward reward, Donation donation, boolean addToHUD, Reward actualReward)
        {
            this.remaining = reward.getCountdown();
            this.donation = donation;
            this.reward = reward;
            this.addToHUD = addToHUD;
            this.actualReward = actualReward;
        }

        public void send()
        {
            NBTTagCompound rewardData = new NBTTagCompound();
            rewardData.setString("name", reward.getName());
            rewardData.setDouble("amount", reward.getAmount());
            // replace all the dummy data with the correct data by converting the JsonArry to a string and then parsing that string to a new JsonArray
            JsonArray rewards = JSON_PARSER.parse(Helper.formatText(reward.getRewards(), donation, actualReward == null ? reward : actualReward).toString()).getAsJsonArray();
            for (JsonElement reward : rewards)
            {
                Pay2Spawn.getSnw().sendToServer(new RewardMessage(JsonNBTHelper.parseJSON(reward.getAsJsonObject()), rewardData));
            }
        }
    }
}
