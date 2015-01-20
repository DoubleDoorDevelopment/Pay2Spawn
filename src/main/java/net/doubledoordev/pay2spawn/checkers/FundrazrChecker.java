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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.hud.DonationsBasedHudEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;

import static net.doubledoordev.pay2spawn.util.Constants.BASECAT_TRACKERS;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * @author Dries007
 */
public class FundrazrChecker extends AbstractChecker implements Runnable
{
    public static final FundrazrChecker INSTANCE = new FundrazrChecker();
    public static final String          NAME     = "Fundrazr";
    public static final String          CAT      = BASECAT_TRACKERS + '.' + NAME;
    public static final String          URL_     = "https://fundrazr.com/api/campaigns/%s/donations?max-results=10";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;
    String  id       = "";
    boolean enabled  = false;
    int     interval = 10;

    private FundrazrChecker()
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
        Hud.INSTANCE.set.add(topDonationsBasedHudEntry);
        Hud.INSTANCE.set.add(recentDonationsBasedHudEntry);

        new Thread(this, getName()).start();
    }

    @Override
    public boolean enabled()
    {
        return enabled && !Strings.isNullOrEmpty(id);
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for Fundrazr.");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();
        min_donation = configuration.get(CAT, "min_donation", min_donation, "Donations below this amount will only be added to statistics and will not spawn rewards").getDouble();
        id = configuration.get(CAT, "id", id, "Your campain ID").getString();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry("recent" + NAME + ".txt", CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
        topDonationsBasedHudEntry = new DonationsBasedHudEntry("top" + NAME + ".txt", CAT + ".topDonations", -1, 1, 5, "$name: $$amount", "-- Top donations --", CheckerHandler.AMOUNT_DONATION_COMPARATOR);
    }

    @Override
    public DonationsBasedHudEntry[] getDonationsBasedHudEntries()
    {
        return new DonationsBasedHudEntry[]{topDonationsBasedHudEntry, recentDonationsBasedHudEntry};
    }

    @Override
    public void run()
    {
        // Process any current donations from the API
        processDonationAPI(true);

        // Start the processing loop
        while (true)
        {
            // Pause the configured wait period
            doWait(interval);

            // Check for any new donations
            processDonationAPI(false);
        }
    }

    private void processDonationAPI(boolean firstRun)
    {
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(String.format(URL_, id)))).getAsJsonObject().getAsJsonObject("activities");
            if (!root.has("entries")) return;
            JsonElement element = root.get("entries");
            if (element.isJsonObject())
            {
                Donation donation = getDonation(element.getAsJsonObject());

                // Make sure we have a donation to work with and see if this is a first run
                if (donation != null && firstRun)
                {
                    // This is a first run so add to current list/done ids
                    topDonationsBasedHudEntry.add(donation);
                    doneIDs.add(donation.id);
                }
                else if (donation != null)
                {
                    // We have a donation and this is a loop check so process the donation
                    process(donation, true, this);
                }
            }
            else if (element.isJsonArray())
            {
                for (JsonElement jsonElement : element.getAsJsonArray())
                {
                    Donation donation = getDonation(jsonElement.getAsJsonObject());

                    // Make sure we have a donation to work with and see if this is a first run
                    if (donation != null && firstRun)
                    {
                        // This is a first run so add to current list/done ids
                        topDonationsBasedHudEntry.add(donation);
                        doneIDs.add(donation.id);
                    }
                    else if (donation != null)
                    {
                        // We have a donation and this is a loop check so process the donation
                        process(donation, true, this);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Donation getDonation(JsonObject jsonObject)
    {
        try
        {
            // unpack the root object
            jsonObject = jsonObject.getAsJsonObject("donation");
            String timestamp = jsonObject.get("created").getAsString();
            timestamp = timestamp.substring(0, timestamp.lastIndexOf(':')) + timestamp.substring(timestamp.lastIndexOf(':') + 1);
            Donation donation = new Donation(jsonObject.get("activityId").getAsString(), jsonObject.get("amount").getAsDouble(), sdf.parse(timestamp).getTime());

            donation.username = jsonObject.getAsJsonObject("owner").get("name").getAsString();

            if (jsonObject.has("message")) donation.note = jsonObject.get("message").getAsString();

            return donation;
        }
        catch (Exception e)
        {
            // We threw an error so just log it and move on
            e.printStackTrace();
        }

        // Something is wrong in the data so return null
        return null;
    }
}
