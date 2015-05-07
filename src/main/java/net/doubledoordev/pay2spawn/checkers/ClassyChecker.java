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
import net.minecraftforge.common.config.Configuration;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static net.doubledoordev.pay2spawn.util.Constants.BASECAT_TRACKERS;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * For classy.org, or any charity who uses them
 *
 * @author Dries007
 */
public class ClassyChecker extends AbstractChecker implements Runnable
{
    public final static ClassyChecker INSTANCE = new ClassyChecker();
    public final static String        NAME     = "classy";
    public final static String        CAT      = BASECAT_TRACKERS + '.' + NAME;
    public final static String BASEURL = "https://www.classy.org/api1/donations";
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale.US);

    DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;
    String url = "", apiToken = "", charityId = "", eid = "", fcid = "", ftid = "";
    boolean enabled  = false;
    int     interval = 5;

    private ClassyChecker()
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
        return enabled;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for anything that uses the Classy.org API.\nA list can be found here: https://www.classy.org/customers\nYou will have to contact the charity for an API key and other required information.");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        interval = configuration.get(CAT, "interval", interval, "The time in between polls minimum 5 (in seconds).").getInt();
        min_donation = configuration.get(CAT, "min_donation", min_donation, "Donations below this amount will only be added to statistics and will not spawn rewards").getDouble();

        apiToken = configuration.get(CAT, "apiToken", apiToken, "The charity api token.").getString();
        charityId = configuration.get(CAT, "charityId", charityId, "The charity id.").getString();
        eid = configuration.get(CAT, "eid", eid, "The unique ID of the campaign. (Optional filter)").getString();
        fcid = configuration.get(CAT, "fcid", fcid, "Fundraising page ID to filter results by. (Optional filter)").getString();
        ftid = configuration.get(CAT, "ftid", ftid, "Fundraising team page ID to filter results by. (Optional filter)").getString();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry("recent" + NAME + ".txt", CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
        topDonationsBasedHudEntry = new DonationsBasedHudEntry("top" + NAME + ".txt", CAT + ".topDonations", -1, 1, 5, "$name: $$amount", "-- Top donations --", CheckerHandler.AMOUNT_DONATION_COMPARATOR);

        // Donation tracker doesn't allow a poll interval faster than 5 seconds
        // They will IP ban anyone using a time below 5 so force the value to be safe
        if (interval < 5)
        {
            interval = 5;
            // Now force the config setting to 5
            configuration.get(CAT, "interval", "The time in between polls minimum 5 (in seconds).").set(interval);
        }

        StringBuilder s = new StringBuilder(BASEURL);
        s.append("?token=").append(apiToken).append("&cid=").append(charityId);
        if (!Strings.isNullOrEmpty(eid)) s.append("&eid=").append(eid);
        if (!Strings.isNullOrEmpty(fcid)) s.append("&fcid=").append(fcid);
        if (!Strings.isNullOrEmpty(ftid)) s.append("&ftid=").append(ftid);
        url = s.toString();
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
        // processDonationAPI(true);

        // Start the processing loop
        while (true)
        {
            // Pause the configured wait period
            doWait(interval);

            // Check for any new donations
            processDonationAPI(false);
        }
    }

    /**
     * Connects to the API and attempt to process any donations
     *
     * @param firstRun <code>boolean</code> used to identify previous donations that should not be processed.
     */
    private void processDonationAPI(boolean firstRun)
    {
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(url))).getAsJsonObject();
            if (root.getAsJsonPrimitive("status_code").getAsString().equals("SUCCESS"))
            {
                JsonArray donations = root.getAsJsonArray("donations");
                for (JsonElement jsonElement : donations)
                {
                    Donation donation = getDonation(jsonElement.getAsJsonObject());

                    // Make sure we have a donation to work with and see if this is a first run
                    if (donation != null && firstRun == true)
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
            // Attempt to parse the data we need for the donation
            String username = jsonObject.get("billing_name").getAsString();
            String note = jsonObject.get("comment").getAsString();
            long timestamp = SIMPLE_DATE_FORMAT.parse(jsonObject.get("donation_date").getAsString()).getTime();
            double amount = jsonObject.get("donate_amount").getAsDouble();
            String id = jsonObject.get("order_id").getAsString();

            // We have all the data we need to return the Donation object
            return new Donation(id, amount, timestamp, username, note);
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
