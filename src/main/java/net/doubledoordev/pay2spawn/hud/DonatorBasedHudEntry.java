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

package net.doubledoordev.pay2spawn.hud;

import com.google.common.base.Strings;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.checkers.CheckerHandler;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.util.*;

/**
 * Base class for all on screen things that have to do with donation data directly
 *
 * @author Dries007
 */
public class DonatorBasedHudEntry implements IHudEntry
{
    final ArrayList<String> strings = new ArrayList<>();
    int position, amount, maxAmount, defaultPosition, defaultAmount;
    String header = "", format = "", configCat = "", defaultFormat = "", defaultHeader = "", filename = "";
    HashMap<String, Donator> donators   = new HashMap<>();
    boolean              writeToFile = true;
    private Comparator<? super Donator> comparator = new Comparator<Donator>()
    {
        @Override
        public int compare(Donator o1, Donator o2)
        {
            if (o1.amount == o2.amount) return 0;
            return o1.amount > o2.amount ? -1 : 1;
        }
    };

    public void clear()
    {
        donators.clear();
        update();
    }

    public static class Donator
    {
        public Donator(String name)
        {
            this.name = name;
        }

        final String name;
        double amount = 0;

        public static Donator readFrom(ByteBuf buf)
        {
            Donator donator = new Donator(ByteBufUtils.readUTF8String(buf));
            donator.amount = buf.readDouble();
            return donator;
        }

        public static void writeTo(Donator donator, ByteBuf buf)
        {
            ByteBufUtils.writeUTF8String(buf, donator.name);
            buf.writeDouble(donator.amount);
        }
    }

    public DonatorBasedHudEntry(String filename, String configCat, int maxAmount, int defaultPosition, int defaultAmount, String defaultFormat, String defaultHeader)
    {
        this.filename = filename;
        this.configCat = configCat;
        this.maxAmount = maxAmount;
        this.defaultPosition = defaultPosition;
        this.defaultAmount = defaultAmount;
        this.defaultFormat = defaultFormat;
        this.defaultHeader = defaultHeader;

        updateConfig();
    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public int getAmount()
    {
        return amount;
    }

    @Override
    public String getHeader()
    {
        return header;
    }

    @Override
    public String getFormat()
    {
        return format;
    }

    @Override
    public void addToList(ArrayList<String> list)
    {
        if (position != 0)
        {
            list.addAll(strings);
        }
    }

    @Override
    public void updateConfig()
    {
        Configuration config = Pay2Spawn.getConfig().configuration;
        position = config.get(configCat, "position", defaultPosition, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(defaultPosition);
        int amount1 = config.get(configCat, "amount", defaultAmount, "maximum: " + maxAmount).getInt(defaultAmount);
        if (maxAmount != -1 && amount1 > maxAmount) amount1 = maxAmount;
        amount = amount1;

        writeToFile = config.getBoolean("writeToFile", configCat, writeToFile, "Write to a file for external use.");

        format = Helper.formatColors(config.get(configCat, "format", defaultFormat).getString());
        header = Helper.formatColors(config.get(configCat, "header", defaultHeader, "Empty for no header. Use \\n for a blank line.").getString()).trim();
    }

    @Override
    public String getFilename()
    {
        return filename;
    }

    @Override
    public boolean writeToFile()
    {
        return writeToFile;
    }

    public void add(Donator donator)
    {
        donators.put(donator.name, donator);

        update();
    }

    public void add(Donation donation)
    {
        if (!donators.containsKey(donation.username)) donators.put(donation.username, new Donator(donation.username));
        donators.get(donation.username).amount += donation.amount;

        update();
    }

    private void update()
    {
        List<Donator> donators1List = new ArrayList<>(donators.values());
        Collections.sort(donators1List, comparator);

        ListIterator<Donator> i = donators1List.listIterator(donators1List.size());
        while (i.hasPrevious() && donators1List.size() > getAmount())
        {
            i.previous();
            i.remove();
        }

        strings.clear();
        if (!Strings.isNullOrEmpty(this.getHeader())) Helper.addWithEmptyLines(this.strings, this.getHeader());
        for (Donator donator : donators1List)
        {
            strings.add(getFormat().replace("$name", donator.name).replace("$amount", String.format("%.2f", donator.amount)));
        }
    }

    public Collection<Donator> getDonators()
    {
        return donators.values();
    }
}

