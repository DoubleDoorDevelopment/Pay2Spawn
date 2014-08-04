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

package net.doubledoordev.pay2spawn.util;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * A reward in java object style
 *
 * @author Dries007
 */
public class Reward
{
    private String    message;
    private String    name;
    private Double    amount;
    private JsonArray rewards;
    private Integer   countdown;

    public Reward(JsonObject json)
    {
        name = json.get("name").getAsString();
        amount = json.get("amount").getAsDouble();
        message = Helper.formatColors(json.get("message").getAsString());
        rewards = json.getAsJsonArray("rewards");
        try
        {
            countdown = json.get("countdown").getAsInt();
        }
        catch (Exception e)
        {
            countdown = 0;
        }
        /**
         * To try and catch errors in the config file ASAP
         */
        try
        {
            JsonNBTHelper.parseJSON(rewards);
        }
        catch (Exception e)
        {
            Pay2Spawn.getLogger().warn("ERROR TYPE 2: Error in reward " + name + "'s NBT data.");
            Pay2Spawn.getLogger().warn(rewards.toString());
            throw e;
        }
    }

    public Reward(String name, Double amount, JsonArray rewards)
    {
        this.name = name;
        this.amount = amount;
        this.rewards = rewards;
    }

    public String getName()
    {
        return name;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void addToCountdown(Donation donation, boolean addToHUD, Reward reward)
    {
        if (!Strings.isNullOrEmpty(message) && addToHUD) Helper.msg(RandomRegistry.solveRandom(STRING, Helper.formatText(message, donation, reward == null ? this : reward)));
        ClientTickHandler.INSTANCE.add(this, donation, addToHUD, reward);
    }

    public Integer getCountdown()
    {
        return countdown;
    }

    public String getMessage()
    {
        return message;
    }

    public String getTypes()
    {
        HashSet<String> types = new HashSet<>();
        for (JsonElement element : rewards) types.add(element.getAsJsonObject().get("type").getAsString());
        return JOINER_COMMA_SPACE.join(types);
    }

    public String getHTML() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (JsonElement element : rewards)
        {
            JsonObject object = element.getAsJsonObject();
            if (object.has(CUSTOMHTML) && !Strings.isNullOrEmpty(object.get(CUSTOMHTML).getAsString())) sb.append(object.get(CUSTOMHTML).getAsString());
            else sb.append(TypeRegistry.getByName(object.get("type").getAsString()).getHTML(object.getAsJsonObject("data")));
        }
        return sb.toString();
    }

    public JsonArray getRewards()
    {
        return rewards;
    }

    @Override
    public String toString()
    {
        return "Reward[" + name + ", " + hashCode() + "]";
    }
}
