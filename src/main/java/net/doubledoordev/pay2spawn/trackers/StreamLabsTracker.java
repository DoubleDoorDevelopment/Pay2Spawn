/*
 * Copyright (c) 2016 Dries007 & DoubleDoorDevelopment
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the
 * disclaimer below) provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of Pay2Spawn nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
 * GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.doubledoordev.pay2spawn.trackers;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;

import static net.doubledoordev.pay2spawn.util.Helper.JSON_PARSER;

/**
 * @author Dries007
 */
public class StreamLabsTracker extends Tracker
{
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final String NAME = "StreamLabs";
    public static final String BASE_URL = "http://www.twitchalerts.com/api/donations?access_token=%s&limit=%d&limit=%s";
    public static final StreamLabsTracker INSTANCE = new StreamLabsTracker();

    private boolean enabled = false;
    private String token = "";
    private int interval = 5;
    private int limit = 10;
    private String url;
    private String currency = "USD";

    private StreamLabsTracker() {}

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void config(Configuration cfg)
    {
        cfg.addCustomCategoryComment(NAME, "Still uses the old TwitchAlerts API.");
        enabled = cfg.getBoolean("enabled", NAME, enabled, "");
        token = cfg.getString("token", NAME, token, "Your *legacy* API Token.");
        currency = cfg.getString("currency", NAME, currency, "Desired currency code. https://twitchalerts.readme.io/v1.0/docs/currency-codes");
        interval = cfg.getInt("interval", NAME, interval, 1, Integer.MAX_VALUE, "The polling interval in seconds. Don't make it too fast, of you might get blocked!");
        limit = cfg.getInt("limit", NAME, limit, 1, 100, "The amount of donation polled per request.");

        url = String.format(BASE_URL, token, limit, currency);
    }

    @Override
    public boolean isEnabled()
    {
        return enabled && !Strings.isNullOrEmpty(token);
    }

    @Override
    public void run()
    {
        processDonationAPI(true);
        while (enabled)
        {
            doWait(interval);
            processDonationAPI(false);
        }
    }

    private void processDonationAPI(boolean firstRun)
    {
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(url, new String[]{"User-Agent", "Pay2Spawn/3"})).getAsJsonObject();
            JsonArray donations = root.getAsJsonArray("donations");
            for (JsonElement jsonElement : donations)
            {
                Donation donation = getDonation(jsonElement.getAsJsonObject());
                if (donation == null) continue;

                if (firstRun) done.add(donation);
                else process(donation);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Donation getDonation(JsonObject object)
    {
        try
        {
            String username = object.get("donator").getAsJsonObject().get("name").getAsString();
            double amount = object.get("amount").getAsDouble();
            long timestamp = SIMPLE_DATE_FORMAT.parse(object.get("created_at").getAsString()).getTime();
            String note = object.has("message") ? object.get("message").getAsString() : "";
            return new Donation(username, amount, timestamp, note);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
