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

package net.doubledoordev.pay2spawn.configurator;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.checkers.TwitchChecker;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import net.doubledoordev.pay2spawn.util.Reward;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.CURRENCY_FORMATTER;

/**
 * Makes nice looking HTML page!
 *
 * @author Dries007
 */
public class HTMLGenerator
{
    static final String  LOOP_START = "<!-- BEGIN REWARDS -->";
    static final String  LOOP_END   = "<!-- END REWARDS -->";
    static final Pattern VAR        = Pattern.compile("\\$\\{([\\w.]*?)\\}");
    public static  File htmlFolder;
    public static  File templateFolder;
    private static File templateIndex;

    public static void init() throws IOException
    {
        htmlFolder = new File(Pay2Spawn.getFolder(), "html");
        templateFolder = new File(htmlFolder, "templates");
        //noinspection ResultOfMethodCallIgnored
        templateFolder.mkdirs();

        templateIndex = new File(templateFolder, "index.html");
        if (!templateIndex.exists())
        {
            InputStream link = (HTMLGenerator.class.getResourceAsStream("/p2sTemplates/index.html"));
            Files.copy(link, templateIndex.getAbsoluteFile().toPath());
        }
        TypeRegistry.copyTemplates();
    }

    public static void generate() throws IOException
    {
        ArrayList<Reward> sortedRewards = new ArrayList<>();
        sortedRewards.addAll(Pay2Spawn.getRewardsDB().getRewards());
        Collections.sort(sortedRewards, new Comparator<Reward>()
        {
            @Override
            public int compare(Reward o1, Reward o2)
            {
                return (int) (o1.getAmount() * 100 - o2.getAmount() * 100);
            }
        });

        File output = new File(htmlFolder, "index.html");
        String text = readFile(templateIndex);
        int begin = text.indexOf(LOOP_START);
        int end = text.indexOf(LOOP_END);

        FileUtils.writeStringToFile(output, replace(text.substring(0, begin)), false);

        String loop = text.substring(begin + LOOP_START.length(), end);
        for (Reward reward : sortedRewards)
        {
            Pay2Spawn.getLogger().info("Adding " + reward + " to html file.");
            FileUtils.writeStringToFile(output, replace(loop, reward), true);
        }

        FileUtils.writeStringToFile(output, text.substring(end + LOOP_END.length(), text.length()), true);
    }

    private static String replace(String text) throws IOException
    {
        return replace(text, null);
    }

    private static String replace(String text, Reward reward) throws IOException
    {
        while (true)
        {
            Matcher matcher = VAR.matcher(text);
            if (!matcher.find()) break;
            text = text.replace(matcher.group(), get(matcher.group(1), reward));
        }
        return text;
    }

    private static String get(String group, Reward reward) throws IOException
    {
        String[] parts = group.split("\\.");
        switch (parts[0])
        {
            case "channel":
                return TwitchChecker.INSTANCE.getChannel();
            case "reward":
                switch (parts[1])
                {
                    case "name":
                        return reward.getName();
                    case "amount":
                        return CURRENCY_FORMATTER.format(reward.getAmount());
                    case "countdown":
                        return reward.getCountdown().toString();
                    case "message":
                        return reward.getMessage();
                    case "types":
                        return reward.getTypes();
                    case "uid":
                        return Integer.toHexString(reward.hashCode());
                    case "rewards":
                        return reward.getHTML();
                }
        }
        return group;
    }

    public static String readFile(File file) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
        return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
    }
}
