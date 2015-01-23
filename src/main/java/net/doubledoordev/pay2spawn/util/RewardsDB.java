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
import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.network.DonationMessage;
import net.doubledoordev.pay2spawn.network.SaleMessage;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.doubledoordev.pay2spawn.types.TypeBase;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;

import static net.doubledoordev.pay2spawn.util.Constants.GSON;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * The rewards database
 *
 * @author Dries007
 * @see net.doubledoordev.pay2spawn.Pay2Spawn#getRewardsDB()
 */
public class RewardsDB
{
    public final  LinkedList<Sale>             saleList = new LinkedList<>();
    private final HashMultimap<Double, Reward> map      = HashMultimap.create();
    public boolean editable;

    public RewardsDB(String input)
    {
        editable = false;
        JsonArray rootArray = JSON_PARSER.parse(input).getAsJsonArray();

        for (JsonElement element : rootArray)
        {
            Reward reward = new Reward(element.getAsJsonObject());
            map.put(reward.getAmount(), reward);
        }
    }

    public RewardsDB(File file)
    {
        editable = true;
        try
        {
            if (file.exists())
            {
                try
                {
                    JsonArray rootArray = JSON_PARSER.parse(new FileReader(file)).getAsJsonArray();

                    for (JsonElement element : rootArray)
                    {
                        Reward reward = new Reward(element.getAsJsonObject());
                        map.put(reward.getAmount(), reward);
                    }
                }
                catch (Exception e)
                {
                    Pay2Spawn.getLogger().warn("ERROR TYPE 2: There is an error in your config file.");
                    e.printStackTrace();
                }
            }
            else
            {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
                JsonArray rootArray = new JsonArray();

                JsonObject group = new JsonObject();
                group.addProperty("name", "EXAMPLE");
                group.addProperty("amount", 2);
                group.addProperty("countdown", 10);
                group.addProperty("message", "&a[$name donated $$amount]");
                JsonArray rewards = new JsonArray();
                for (TypeBase type : TypeRegistry.getAllTypes())
                {
                    if (type.isInDefaultConfig())
                    {
                        JsonObject element = new JsonObject();
                        element.addProperty("type", type.getName());
                        //noinspection unchecked
                        element.add("data", JsonNBTHelper.parseNBT(type.getExample()));
                        rewards.add(element);
                    }
                }
                group.add("rewards", rewards);
                rootArray.add(group);

                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(GSON.toJson(rootArray));
                bw.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void process(Donation donation, boolean msg)
    {
        if (FMLCommonHandler.instance().getSide().isClient())
        {
            donation.target = FMLClientHandler.instance().getClientPlayerEntity().getCommandSenderName();
        }
        else
        {
            Matcher matcher = Constants.USERNAME_FROM_NOTE.matcher(donation.note);
            if (matcher.find())
            {
                String target = matcher.group(1);
                for (String username : MinecraftServer.getServer().getAllUsernames())
                {
                    if (target.equalsIgnoreCase(username))
                    {
                        donation.target = username;
                    }
                }
            }
            if (donation.target == null)
            {
                donation.target = RandomRegistry.getRandomFromSet(Arrays.asList(MinecraftServer.getServer().getAllUsernames()));
            }
        }

        Hud.INSTANCE.topDonationsBasedHudEntry.add(donation);
        Hud.INSTANCE.recentDonationsBasedHudEntry.add(donation);

        double amount = donation.amount; // Keep original value for stats and display purposes.

        Sale sale = getLastSale();
        if (sale != null) amount /= 1.0 - (sale.amount / 100.0);

        Pay2Spawn.getLogger().info("Donation + sale = " + amount);

        double highestmatch = 0d;
        Reward reward = null;
        if (map.containsKey(amount)) reward = RandomRegistry.getRandomFromSet(map.get(amount));
        else
        {
            for (double key : map.keySet()) if (key < amount && highestmatch < key) highestmatch = key;

            if (map.containsKey(highestmatch)) reward = RandomRegistry.getRandomFromSet(map.get(highestmatch));
        }

        boolean messageHasBeenSend = false;
        Pay2Spawn.getSnw().sendToAll(new DonationMessage(donation));
        if (reward != null)
        {
            Statistics.handleSpawn(reward.getName());
            reward.addToCountdown(donation, true, null);
            messageHasBeenSend = reward.sendMessage(donation);
        }

        /**
         * -1 will always spawn
         */
        if (map.containsKey(-1D))
        {
            RandomRegistry.getRandomFromSet(map.get(-1D)).addToCountdown(donation, false, reward);
        }

        String format = Helper.formatColors(Pay2Spawn.getConfig().serverMessage);
        if (!messageHasBeenSend && Strings.isNullOrEmpty(format))
        {
            format = format.replace("$name", donation.username);
            format = format.replace("$amount", String.format("%.2f", donation.amount));
            format = format.replace("$note", donation.note);
            format = format.replace("$streamer", donation.target);
            if (reward != null)
            {
                format = format.replace("$reward_message", reward.getMessage());
                format = format.replace("$reward_name", reward.getName());
                format = format.replace("$reward_amount", String.format("%.2f", reward.getAmount()));
                format = format.replace("$reward_countdown", String.valueOf(reward.getCountdown()));
            }
            else
            {
                format = format.replace("$reward_message", "??");
                format = format.replace("$reward_name", "??");
                format = format.replace("$reward_amount", "??");
                format = format.replace("$reward_countdown", "??");
            }
            MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(format));
        }
    }

    public Set<Double> getAmounts()
    {
        return map.keySet();
    }

    public Collection<Reward> getRewards()
    {
        return map.values();
    }

    public void addSale(int time, int amount)
    {
        if (FMLCommonHandler.instance().getSide().isServer()) Pay2Spawn.getSnw().sendToAll(new SaleMessage(time, amount));
        saleList.add(new Sale(time, amount));
    }

    public Sale getLastSale()
    {
        Sale sale = null;
        synchronized (saleList)
        {
            while (!saleList.isEmpty() && sale == null)
            {
                if (!saleList.getFirst().isExpired()) sale = saleList.getFirst();
                else saleList.remove();
            }
        }
        return sale;
    }

    public static class Sale
    {
        public final int  amount;
        public       long time;
        private boolean activated = false;

        public Sale(int time, int amount)
        {
            this.time = time * 1000 * 60;
            this.amount = amount;
        }

        public boolean isExpired()
        {
            if (!activated)
            {
                this.activated = true;
                this.time += System.currentTimeMillis();
            }
            return time - System.currentTimeMillis() < 0;
        }
    }
}
