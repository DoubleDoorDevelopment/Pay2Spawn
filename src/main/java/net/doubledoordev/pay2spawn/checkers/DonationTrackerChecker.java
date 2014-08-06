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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.BASECAT_TRACKERS;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * For donation-tracker.com
 *
 * @author Dries007
 */
public class DonationTrackerChecker extends AbstractChecker implements Runnable
{
    public static final DonationTrackerChecker INSTANCE     = new DonationTrackerChecker();
    public final static String                 NAME         = "donation-tracker";
    public final static String                 CAT          = BASECAT_TRACKERS + '.' + NAME;
    public final static Pattern                HTML_REGEX   = Pattern.compile("<td.*?>(.+?)<\\/td.*?>");
    public final static Pattern                AMOUNT_REGEX = Pattern.compile("(\\d+(?:\\.|,)\\d\\d)\\w?.?");

    DonationsBasedHudEntry topDonationsBasedHudEntry, recentDonationsBasedHudEntry;

    public String URL = "https://www.donation-tracker.com/customapi/?channel=%s&api_key=%s&custom=1";

    String Channel = "", APIKey = "";
    boolean          enabled  = false;
    int              interval = 20;
    SimpleDateFormat sdf      = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

    private DonationTrackerChecker()
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
        return enabled && !Channel.isEmpty() && !APIKey.isEmpty();
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(CAT, "This is the checker for donation-tracker.com");

        enabled = configuration.get(CAT, "enabled", enabled).getBoolean(enabled);
        Channel = configuration.get(CAT, "Channel", Channel).getString();
        APIKey = configuration.get(CAT, "APIKey", APIKey).getString();
        interval = configuration.get(CAT, "interval", interval, "The time in between polls (in seconds).").getInt();
        min_donation = configuration.get(CAT, "min_donation", min_donation, "Donations below this amount will only be added to statistics and will not spawn rewards").getDouble();
        URL = configuration.get(CAT, "url", URL, "Donation Tracker API end point string").getString();

        recentDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".recentDonations", -1, 2, 5, "$name: $$amount", "-- Recent donations --", CheckerHandler.RECENT_DONATION_COMPARATOR);
        topDonationsBasedHudEntry = new DonationsBasedHudEntry(configuration, CAT + ".topDonations", -1, 1, 5, "$name: $$amount", "-- Top donations --", CheckerHandler.AMOUNT_DONATION_COMPARATOR);
    }

    @Override
    public DonationsBasedHudEntry[] getDonationsBasedHudEntries()
    {
        return new DonationsBasedHudEntry[] {topDonationsBasedHudEntry, recentDonationsBasedHudEntry};
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

    /***
     * Connects to the API and attempt to process any donations
     *
     * @param firstRun <code>boolean</code> used to identify previous donations that should not be processed.
     */
    private void processDonationAPI(boolean firstRun)
    {
        try
        {
            JsonObject root = JSON_PARSER.parse(Helper.readUrl(new URL(String.format(URL, Channel, APIKey )))).getAsJsonObject();
            if (root.getAsJsonPrimitive("api_check").getAsInt() == 1)
            {
                JsonArray donations = root.getAsJsonArray("donation_list");
                for (JsonElement jsonElement : donations)
                {
                    Donation donation = getDonation(jsonElement.getAsString());

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
                        process(donation, true);
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Donation getDonation(String html)
    {
        ArrayList<String> htmlMatches = new ArrayList<>();
        Matcher htmlMatcher = HTML_REGEX.matcher(html);

        while (htmlMatcher.find())
        {
            htmlMatches.add(htmlMatcher.group(1));
        }
        String[] data = htmlMatches.toArray(new String[htmlMatches.size()]);

        // Make sure we have enough data to process
        if (htmlMatches.size() == 4)
        {
            Matcher amountMatcher = AMOUNT_REGEX.matcher(data[3]);

            // We only continue if we can match the regex amount
            if (amountMatcher.find())
            {
                double amount = Double.parseDouble(amountMatcher.group(1).replace(',', '.'));

                long time = new Date().getTime();
                try
                {
                    time = sdf.parse(data[2]).getTime();
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }

                // Currently Donation tracker doesn't send a proper ID value so we need to make one
                String id = Helper.MD5(String.format("%s|%s|%s|%s ",  data[0], data[1], data[2], data[3]));

                return new Donation(id, amount, time, data[0], data[1]);
            }
        }

        // Something is wrong in the data so return null
        return null;
    }
}
