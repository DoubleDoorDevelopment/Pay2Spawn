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

import com.google.common.base.Joiner;
import com.google.gson.JsonParser;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author Dries007
 */
@SuppressWarnings("WeakerAccess")
public class Helper
{
    public static final String MOD_ID = "pay2spawn";
    public static final String MOD_NAME = "Pay2Spawn";
    public static final String ANON = "Anonymous";

    public static final Pattern NAME_PATTERN = Pattern.compile("^(?:#|//|--)[ \\t]*NAME(?:[ \\t]*:?[ \\t]*)(.*)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern AMOUNT_PATTERN = Pattern.compile("^(?:#|//|--)[ \\t]*AMOUNT(?:[ \\t]*:?[ \\t]*)([-+]?[0-9]*\\.?[0-9]+)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern LANGUAGE_PATTERN = Pattern.compile("^(?:#|//|--)[ \\t]*LANGUAGE(?:[ \\t]*:?[ \\t]*)(.+)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern INCLUDE_PATTERN = Pattern.compile("^(?:#|//|--)[ \\t]*INCLUDE(?:[ \\t]*:?[ \\t]*)(.+)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern AT_PATTERN = Pattern.compile("@(\\w+)\\b");

    public static final JsonParser JSON_PARSER = new JsonParser();
    public static final Joiner SPACE_JOINER = Joiner.on(' ');
    public static final DecimalFormat FORMAT_2_POINT = new DecimalFormat("0.00");
    public static final Charset UTF8 = Charset.forName("UTF-8");
    public static final Random RANDOM = new Random();

    private static final char[] RND_LETTERS;

    static
    {
        FORMAT_2_POINT.setDecimalSeparatorAlwaysShown(true);

        StringBuilder tmp = new StringBuilder(" .,!?@");
        for (char ch = '0'; ch <= '9'; ++ch) tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch) tmp.append(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch) tmp.append(ch);
        RND_LETTERS = tmp.toString().toCharArray();
    }

    public static <T> T pickRandom(List<T> list)
    {
        if (list.size() == 0) return null;
        return list.get(RANDOM.nextInt(list.size()));
    }

    public static String randomString(int n)
    {
        StringBuilder builder = new StringBuilder(n);
        for (int i = 0; i < n; i++)
        {
            builder.append(RND_LETTERS[RANDOM.nextInt(RND_LETTERS.length)]);
        }
        return builder.toString();
    }

    private Helper() {}

    public static void chat(ICommandSender runner, String text, TextFormatting color)
    {
        runner.sendMessage(new TextComponentString(text).setStyle(new Style().setColor(color)));
    }

    public static void chat(ICommandSender runner, String text)
    {
        runner.sendMessage(new TextComponentString(text));
    }

    public static String readUrl(String url, String[]... headers) throws IOException
    {
        URLConnection connection = new URL(url).openConnection();
        for (String[] header : headers) connection.addRequestProperty(header[0], header[1]);
        return IOUtils.toString(connection.getInputStream());
    }
}
