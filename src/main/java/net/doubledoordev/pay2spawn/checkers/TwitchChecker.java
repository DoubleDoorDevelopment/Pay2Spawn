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

package net.doubledoordev.pay2spawn.checkers;

import net.doubledoordev.pay2spawn.hud.DonationsBasedHudEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.common.config.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.util.Constants.BASECAT_TRACKERS;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * For tracking twitch subs
 *
 * @author Dries007
 */
public class TwitchChecker extends AbstractChecker implements Runnable
{
    public static final TwitchChecker INSTANCE = new TwitchChecker();
    public final static String        NAME     = "twitchsubs";
    public final static String        CAT      = BASECAT_TRACKERS + '.' + NAME;
    HashMap<String, String> subs = new HashMap<>();

    DonationsBasedHudEntry recentDonationsBasedHudEntry;

    String  APIKey   = "";
    boolean enabled  = false;
    int     interval = 20;
    double  amount   = 5;
    String  channel  = "";
    URL url;
    boolean firstrun = true;
    String  note     = "I subscribed on Twitch.tv!";

    private TwitchChecker()
    {
        super();
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void init()
    {
        Hud.INSTANCE.set.add(recentDonationsBasedHudEntry);

        new Thread(this, getName()).start();
    }

    @Override
    public boolean enabled()
    {
        return enabled && !APIKey.isEmpty() && !channel.isEmpty();
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for twitch subs");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        APIKey = configuration.get(CAT, "APIKey", APIKey, "Get this from http://dries007.net/ccm/p2s/").getString();
        channel = configuration.get(CAT, "channel", channel, "Your exact channel name").getString();
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();
        amount = configuration.get(CAT, "amount", amount, "The amount of currency a sub counts for.").getDouble(amount);
        note = configuration.get(CAT, "note", note, "The note attached to the 'fake' donation.").getString();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry(CAT + ".recentSubs", -1, 2, 5, "$name", "-- Recent subs --", CheckerHandler.RECENT_DONATION_COMPARATOR);

        try
        {
            url = new URL("https://api.twitch.tv/kraken/channels/" + channel + "/subscriptions?limit=100&oauth_token=" + APIKey);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public DonationsBasedHudEntry[] getDonationsBasedHudEntries()
    {
        return new DonationsBasedHudEntry[] {recentDonationsBasedHudEntry};
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                HashMap<String, String> newSubs = new HashMap<>();
                JsonObject root = JSON_PARSER.parse(Helper.readUrl(url)).getAsJsonObject();
                parseSubs(newSubs, root);
                int total = root.getAsJsonPrimitive("_total").getAsInt();
                for (int offset = 100; offset < total; offset += 100)
                {
                    root = JSON_PARSER.parse(Helper.readUrl(new URL(url.toString() + "&offset=" + offset))).getAsJsonObject();
                    parseSubs(newSubs, root);
                }

                for (String sub : newSubs.keySet())
                {
                    if (!subs.containsKey(sub) && !firstrun)
                    {
                        process(new Donation(sub, amount, new Date().getTime(), newSubs.get(sub)), true);
                    }
                }
                subs = newSubs;
                firstrun = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            doWait(interval);
        }
    }

    private void parseSubs(HashMap<String, String> subs, JsonObject object)
    {
        for (JsonElement sub : object.getAsJsonArray("subscriptions"))
        {
            subs.put(sub.getAsJsonObject().getAsJsonObject("user").get("_id").getAsString(), sub.getAsJsonObject().getAsJsonObject("user").get("display_name").getAsString());
        }
    }

    public String getChannel()
    {
        return channel;
    }
}
