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

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.network.MessageMessage;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.doubledoordev.pay2spawn.types.TypeBase;
import net.doubledoordev.pay2spawn.types.TypeRegistry;

import java.io.*;
import java.util.Collection;
import java.util.Set;

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
    private final HashMultimap<Double, Reward> map = HashMultimap.create();
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
        double highestmatch = 0d;

        Reward reward = null;
        if (map.containsKey(donation.amount)) reward = RandomRegistry.getRandomFromSet(map.get(donation.amount));
        else
        {
            for (double key : map.keySet())
                if (key < donation.amount && highestmatch < key) highestmatch = key;

            if (map.containsKey(highestmatch)) reward = RandomRegistry.getRandomFromSet(map.get(highestmatch));
        }

        if (reward != null)
        {
            Statistics.handleSpawn(reward.getName());
            ClientTickHandler.INSTANCE.donationTrainEntry.resetTimeout();
            reward.addToCountdown(donation, true, null);
        }

        /**
         * -1 will always spawn
         */
        if (map.containsKey(-1D))
        {
            RandomRegistry.getRandomFromSet(map.get(-1D)).addToCountdown(donation, false, reward);
        }

        Pay2Spawn.getSnw().sendToServer(new MessageMessage(reward, donation));
    }

    public Set<Double> getAmounts()
    {
        return map.keySet();
    }

    public Collection<Reward> getRewards()
    {
        return map.values();
    }
}
