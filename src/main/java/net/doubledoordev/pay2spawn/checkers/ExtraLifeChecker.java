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

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.hud.DonationsBasedHudEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.minecraftforge.common.config.Configuration;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * For extra-life.org
 *
 * @author Dries007
 */
public class ExtraLifeChecker extends AbstractChecker implements Runnable
{
    public final static ExtraLifeChecker INSTANCE           = new ExtraLifeChecker();
    public final static String            NAME               = "extra-life";
    public final static String            CAT                = BASECAT_TRACKERS + '.' + NAME;
    public final static SimpleDateFormat  SIMPLE_DATE_FORMAT = new SimpleDateFormat("MMMMM, F y H:m:s", Locale.US);

    static
    {
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    DonationsBasedHudEntry recentDonationsBasedHudEntry;

    String           url      = "";
    boolean          enabled  = false;
    int              interval = 20;

    private ExtraLifeChecker()
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
        return enabled && !url.isEmpty();
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for Extra Life\nYou need to get your API key from them.");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        url = configuration.get(CAT, "url", url, "Your pages url. Must be donorDrive.participantDonations").getString();
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();
        min_donation = configuration.get(CAT, "min_donation", min_donation, "Donations below this amount will only be added to statistics and will not spawn rewards").getDouble();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry("recent" + NAME + ".txt", CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
    }

    @Override
    public void run()
    {
        try
        {
            for (JsonElement jsonElement : get())
            {
                Donation donation = getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject()));
                recentDonationsBasedHudEntry.add(donation);
                doneIDs.add(donation.id);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        while (true)
        {
            doWait(interval);
            try
            {
                for (JsonElement jsonElement : get())
                {
                    Donation donation = getDonation(JsonNBTHelper.fixNulls(jsonElement.getAsJsonObject()));
                    recentDonationsBasedHudEntry.add(donation);
                    doneIDs.add(donation.id);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private Donation getDonation(JsonObject jsonObject)
    {
        long time = 0L;
        try
        {
            time = SIMPLE_DATE_FORMAT.parse(jsonObject.get("createdOn").getAsString()).getTime();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        String name = jsonObject.get("donorName").getAsString();
        if (Strings.isNullOrEmpty(name)) name = ANONYMOUS;
        String id = jsonObject.get("createdOn").getAsString().replace(" ", "") + jsonObject.get("donationAmount").getAsDouble() + "" + name;
        return new Donation(id, jsonObject.get("donationAmount").getAsDouble(), time, name, jsonObject.get("message").getAsString());
    }

    private JsonArray get() throws Exception
    {
        return JSON_PARSER.parse(Helper.readUrl(new URL(url))).getAsJsonArray();
    }
}
