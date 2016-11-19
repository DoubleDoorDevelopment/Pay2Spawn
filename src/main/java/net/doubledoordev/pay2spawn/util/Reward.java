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
import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Dries007
 */
public class Reward
{
    public final String name;
    public final double amount;
    public final String script;
    public final String language;

    public Reward(String name, double amount, String language, String script)
    {
        if (Strings.isNullOrEmpty(name)) throw new IllegalArgumentException("Name cannot be null or empty.");
        if (Strings.isNullOrEmpty(language)) throw new IllegalArgumentException("Language cannot be null or empty.");
        if (Strings.isNullOrEmpty(script)) throw new IllegalArgumentException("Script cannot be null or empty.");
        this.name = name;
        this.amount = amount;
        this.language = language;
        this.script = script;

        if (ScriptHelper.get(language) == null)
        {
            Pay2Spawn.getLogger().fatal("Scripting Language {} (for reward {}) not available.", language, name);
            ScriptHelper.dump();
            throw new IllegalArgumentException("Scripting language not available. See long for a list of available options.");
        }

        Pay2Spawn.getLogger().info("Reward {} (for ${}, in {}) full script: \n{}", name, amount, language, script);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reward reward = (Reward) o;

        if (Double.compare(reward.amount, amount) != 0) return false;
        if (!name.equals(reward.name)) return false;
        if (!script.equals(reward.script)) return false;
        return language.equals(reward.language);

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + script.hashCode();
        result = 31 * result + language.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "Reward[" + name + ';' + amount + ';' + language + ']';
    }

    public static Reward fromFile(File file) throws IOException
    {
        String name = null;
        double amount = Double.NaN;
        String language = null;
        ArrayList<String> lines = new ArrayList<>();

        LineIterator li = FileUtils.lineIterator(file);
        while (li.hasNext())
        {
            final String line = li.nextLine();

            // Metadata or comments
            {
                Matcher matcher = Helper.NAME_PATTERN.matcher(line);
                if (matcher.matches())
                {
                    if (name != null) throw new IOException("Name specified more then once.");
                    name = matcher.group(1);
                    continue;
                }
                matcher = Helper.AMOUNT_PATTERN.matcher(line);
                if (matcher.matches())
                {
                    if (!Double.isNaN(amount)) throw new IOException("");
                    amount = Double.parseDouble(matcher.group(1));
                    continue;
                }
                matcher = Helper.LANGUAGE_PATTERN.matcher(line);
                if (matcher.matches())
                {
                    if (language != null) throw new IOException("Language specified more then once.");
                    language = matcher.group(1);
                    continue;
                }
                matcher = Helper.INCLUDE_PATTERN.matcher(line);
                if (matcher.matches())
                {
                    for (String inc : matcher.group(1).trim().split("[\\s,]+")) parseIncludeFile(lines, inc);
                    continue;
                }
            }

            lines.add(line);
        }

        if (name == null)
        {
            Pay2Spawn.getLogger().info("{} specifies no reward name. Assuming filename.", file.getName());
            name = FilenameUtils.getBaseName(file.getName());
        }
        if (language == null)
        {
            Pay2Spawn.getLogger().info("{} specifies no script language. Guessing based on extension.", file.getName());
            language = ScriptHelper.fromExtension(FilenameUtils.getExtension(file.getName()));
            if (language == null) throw new IOException("Language undetectable from extension.");
        }
        if (Double.isNaN(amount)) throw new IOException("You MUST specify a reward amount.");

        return new Reward(name, amount, language, Joiner.on('\n').join(lines));
    }

    private static void parseIncludeFile(List<String> lines, String filename) throws IOException
    {
        File file = new File(RewardDB.getIncludesFolder(), filename);

        LineIterator li = FileUtils.lineIterator(file);
        while (li.hasNext())
        {
            final String line = li.nextLine();
            Matcher matcher = Helper.INCLUDE_PATTERN.matcher(line);
            if (matcher.matches())
            {
                for (String inc : matcher.group(1).split("[\\s,]+")) parseIncludeFile(lines, inc);
                continue;
            }

            lines.add(line);
        }
    }

    public static Reward fromBytes(ByteBuf buf)
    {
        String name = ByteBufUtils.readUTF8String(buf);
        double amount = buf.readDouble();
        String language = ByteBufUtils.readUTF8String(buf);


        byte script[] = new byte[buf.readInt()];
        buf.readBytes(script);
        return new Reward(name, amount, language, new String(script, Helper.UTF8));
    }

    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeDouble(amount);
        ByteBufUtils.writeUTF8String(buf, language);
        byte[] bytes = script.getBytes(Helper.UTF8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
