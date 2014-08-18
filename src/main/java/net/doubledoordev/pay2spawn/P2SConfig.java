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

package net.doubledoordev.pay2spawn;

import com.google.common.io.Files;
import net.doubledoordev.pay2spawn.checkers.CheckerHandler;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Uses subclasses to make file cleaner
 *
 * @author Dries007
 * @see net.doubledoordev.pay2spawn.Pay2Spawn#getConfig()
 */
public class P2SConfig
{
    public final static String HUD           = MODID + ".Hud";
    public static final String CONFIGVERSION = "2";
    public final boolean majorConfigVersionChange;
    public        Configuration configuration;
    public boolean forceServerconfig = true;
    public boolean forceP2S          = false;
    public double  min_donation      = 1;
    public Pattern[] blacklist_Name_p;
    public Pattern[] blacklist_Note_p;
    public Pattern[] whitelist_Name_p;
    public Pattern[] whitelist_Note_p;
    public  String   serverMessage  = "$streamer got $$amount from $name and $reward_name was triggered!";
    @SuppressWarnings("FieldCanBeLocal")
    private String[] blacklist_Name = {"fuck", "cunt", "dick", "shit"};
    @SuppressWarnings("FieldCanBeLocal")
    private String[] blacklist_Note = {"fuck", "cunt", "dick", "shit"};
    @SuppressWarnings("FieldCanBeLocal")
    private String[] whitelist_Name = {"\"[\\w-]*\""};
    @SuppressWarnings("FieldCanBeLocal")
    private String[] whitelist_Note = {};
    public  boolean  sillyness      = true;

    public P2SConfig(File file)
    {
        configuration = new Configuration(file);

        String cvk = "configversion";
        majorConfigVersionChange = !configuration.getCategory(MODID.toLowerCase()).keySet().contains(cvk) || !configuration.get(MODID, cvk, CONFIGVERSION).getString().equals(CONFIGVERSION);

        if (majorConfigVersionChange)
        {
            try
            {
                Files.copy(file, new File(file.getParentFile(), "Pay2SpawnBackup_" + new SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss's'").format(new Date()) + ".cfg"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            file.delete();

            configuration = new Configuration(file);
            configuration.get(MODID, cvk, CONFIGVERSION).set(CONFIGVERSION);
        }

        configuration.setCategoryLanguageKey(MODID, "d3.pay2spawn.config.general");
        configuration.setCategoryLanguageKey(SERVER_CAT, "d3.pay2spawn.config.server");
        configuration.setCategoryLanguageKey(FILTER_CAT, "d3.pay2spawn.config.filter");
        configuration.setCategoryLanguageKey(TYPES_CAT, "d3.pay2spawn.config.types");
        configuration.setCategoryLanguageKey(BASECAT_TRACKERS, "d3.pay2spawn.config.trackers");
    }

    public void syncConfig()
    {
        {
            configuration.addCustomCategoryComment(SERVER_CAT, "Anything here can override client side settings.\nAlso used for SSP");

            forceServerconfig = configuration.get(SERVER_CAT, "forceServerconfig", forceServerconfig, "If a client connects, force the config from the server to the client.").getBoolean(forceServerconfig);
            forceP2S = configuration.get(SERVER_CAT, "forceP2S", forceP2S, "If a client connects, kick it if there is no P2S. If there is, p2s will be locked in ON mode.").getBoolean(forceP2S);
            serverMessage = configuration.get(SERVER_CAT, "serverMessage", serverMessage, "Server config deferments the structure.\nMake empty to remove this message\nVars: $name, $amount, $note, $streamer, $reward_message, $reward_name, $reward_amount, $reward_countdown.").getString();
        }

        {
            configuration.addCustomCategoryComment(FILTER_CAT, "All filters use regex, very useful site: http://gskinner.com/RegExr/\nMatching happens case insensitive.\nUSE DOUBLE QUOTES (\") AROUND EACH LINE!");

            blacklist_Name = configuration.get(FILTER_CAT, "blacklist_Name", blacklist_Name, "If matches, name gets changed to Anonymous. Overrules whitelist.").getStringList();
            blacklist_Name_p = new Pattern[blacklist_Name.length];
            for (int i = 0; i < blacklist_Name.length; i++) blacklist_Name_p[i] = Pattern.compile(Helper.removeQuotes(blacklist_Name[i]), Pattern.CASE_INSENSITIVE);

            blacklist_Note = configuration.get(FILTER_CAT, "blacklist_Note", blacklist_Note, "If matches, the match gets removed. Overrules whitelist.").getStringList();
            blacklist_Note_p = new Pattern[blacklist_Note.length];
            for (int i = 0; i < blacklist_Note.length; i++) blacklist_Note_p[i] = Pattern.compile(Helper.removeQuotes(blacklist_Note[i]), Pattern.CASE_INSENSITIVE);

            whitelist_Name = configuration.get(FILTER_CAT, "whitelist_Name", whitelist_Name, "If NOT matches, name gets changed to Anonymous. Overruled by blacklist.").getStringList();
            whitelist_Name_p = new Pattern[whitelist_Name.length];
            for (int i = 0; i < whitelist_Name.length; i++) whitelist_Name_p[i] = Pattern.compile(Helper.removeQuotes(whitelist_Name[i]), Pattern.CASE_INSENSITIVE);

            whitelist_Note = configuration.get(FILTER_CAT, "whitelist_Note", whitelist_Note, "If NOT matches, note gets removed. Overruled by blacklist.").getStringList();
            whitelist_Note_p = new Pattern[whitelist_Note.length];
            for (int i = 0; i < whitelist_Note.length; i++) whitelist_Note_p[i] = Pattern.compile(Helper.removeQuotes(whitelist_Note[i]), Pattern.CASE_INSENSITIVE);
        }

        CheckerHandler.doConfig(configuration);
        TypeRegistry.doConfig(configuration);
        Hud.INSTANCE.doConfig();

        if (configuration.hasChanged()) configuration.save();
    }
}
