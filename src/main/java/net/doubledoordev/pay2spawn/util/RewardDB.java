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

package net.doubledoordev.pay2spawn.util;

import com.google.common.base.Throwables;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.client.Pay2SpawnClient;
import net.doubledoordev.pay2spawn.network.RewardMessage;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Dries007
 */
public class RewardDB
{
    private static final TreeMap<Double, Reward> DONATION_MAP = new TreeMap<>();

    private RewardDB() {}

    public static void populate() throws IOException
    {
        DONATION_MAP.clear();

        File[] files = getRewardsFolder().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isFile();
            }
        });
        if (files == null) throw new IOException("Error listing files in " + getRewardsFolder().getAbsolutePath());
        if (files.length == 0) makeExample();
        for (File file : files) parse(file);
    }

    public static File getRewardsFolder()
    {
        File rewards = new File(Pay2Spawn.getConfigDir(), "rewards");
        //noinspection ResultOfMethodCallIgnored
        rewards.mkdirs();
        return rewards;
    }

    public static File getIncludesFolder()
    {
        File rewards = new File(Pay2Spawn.getConfigDir(), "includes");
        //noinspection ResultOfMethodCallIgnored
        rewards.mkdirs();
        return rewards;
    }


    private static void parse(File file) throws IOException
    {
        try
        {
            Reward reward = Reward.fromFile(file);
            DONATION_MAP.put(reward.amount, reward);
        }
        catch (IOException e)
        {
            Pay2Spawn.getLogger().fatal("Caught fatal error on trying to parse {}", file.getName());
            Pay2Spawn.getLogger().catching(e);
            Throwables.propagate(e);
        }
    }

    public static Reward select(Donation donation)
    {
        Map.Entry<Double, Reward> entry = DONATION_MAP.floorEntry(donation.amount);
        if (entry == null) return null;
        return entry.getValue();
    }

    public static void process(ICommandSender sender, final Donation donation)
    {
        final Reward reward = select(donation);
        Pay2Spawn.getLogger().info("Got new donation! {}", donation);
        Pay2Spawn.getLogger().info("Selected reward: {}", reward);

        if (reward == null) return;

        if (FMLCommonHandler.instance().getSide().isClient())
        {
            Pay2SpawnClient.runInClientThread(new Runnable() {
                @Override
                public void run()
                {
                    Pay2Spawn.getSNW().sendToServer(new RewardMessage(reward, donation));
                }
            });
        }
        else
        {
            if (sender == null) sender = FMLCommonHandler.instance().getMinecraftServerInstance();
            ScriptHelper.execute(sender, reward, donation);
        }
    }

    private static void makeExample() throws IOException
    {
        Pay2Spawn.getLogger().info("Creating example reward file, since the rewards folder is empty.");

        File example = new File(getRewardsFolder(), "example.js");
        File include = new File(getIncludesFolder(), "example_include.js");

        //todo: make better example
        //todo: write wiki

        FileUtils.write(include, "\"use strict\"; // only for IDE if you have it in the main script already.\n" +
                "\n" +
                "// This is an example include\n" +
                "p2s.log(\"Example include running for reward {} triggered by donation of ${} by {}.\",\n" +
                "    p2s.reward.name, p2s.donation.amount, p2s.donation.name);");

        FileUtils.write(example, "//NAME: Example reward\n" +
                "//AMOUNT: 5.0\n" +
                "//LANGUAGE: JavaScript\n" +
                "\n" +
                "/**\n" +
                " * This file, except the metadata above, will be processed as JavaScript.\n" +
                " * Its strongly recommended to put the metadata at the top, but not required.\n" +
                " * The name and languages can be guessed from the filename, amount is required.\n" +
                " * All are strongly recommended though.\n" +
                " *\n" +
                " * Legal prefix characters for the metadata: '//', '#', and '--'.\n" +
                " * This should cover a lot of languages, but if not, the lines will be removed\n" +
                " * anyway, so they won't cause syntax errors.\n" +
                " *\n" +
                " * For more info on what you can all do in this script environment:\n" +
                " * *******************************************************************\n" +
                " * ***** https://github.com/DoubleDoorDevelopment/Pay2Spawn/wiki *****\n" +
                " * *******************************************************************\n" +
                " */\n" +
                "\n" +
                "\"use strict\"; // STRONGLY recommended\n" +
                "\n" +
                "/**\n" +
                " * Feel free to contact us with any questions, but please read this, and the\n" +
                " * open issues first. If you are unfamiliar with JavaScript (which is totally\n" +
                " * unrelated to Java), please go and follow a couple of online tutorials.\n" +
                " * If you want a proper 'book' about JavaScript, try this:\n" +
                " *      http://eloquentjavascript.net/          (you only really need 1 -> 5)\n" +
                " *\n" +
                " * To get (partial) JSDoc completion, you can pull Pay2Spawn.js from the jar.\n" +
                " * If your IDE supports it (Intellij does :p), set your javascript library /\n" +
                " * limited static analysis of your scripts.\n" +
                " * environment to \"Nashorn\" and the Pay2Spawn.js file. This will allow some\n" +
                " * basic static analysis of your scripts.\n" +
                " */\n" +
                "\n" +
                "p2s.log(\"Example script running for reward {} triggered by donation of ${} by {}.\",\n" +
                "    p2s.reward.name, p2s.donation.amount, p2s.donation.name);\n" +
                "p2s.cmd('time add', Math.floor(10000 * Math.random()));\n" +
                "\n" +
                "//INCLUDE: example_include.js\n" +
                "\n" +
                "print('stdout print')\n" +
                "\n" +
                "p2s.cmd('tp ~ 256 ~');\n" +
                "\n" +
                "p2s.speak('I got a donation of $' + p2s.donation.amount, 'from', p2s.donation.name);\n" +
                "p2s.chat('A chat message only I see!');\n" +
                "\n" +
                "p2s.log({test_key: 'test_value'});\n" +
                "p2s.log('This function uses the Log4j2 message syntax. Example:\\n Donation: {}\\n Reward: {}', p2s.donation, p2s.reward);\n" +
                "p2s.log('The raw logger object is also available via p2s.logger: {}', p2s.logger);\n");

        parse(example);
    }
}
