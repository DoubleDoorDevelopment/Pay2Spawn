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
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.checkers.CheckerHandler;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for all on screen things that have to do with donation data directly
 *
 * @author Dries007
 */
public class DonationsBasedHudEntry implements IHudEntry
{
    final ArrayList<String> strings = new ArrayList<>();
    int position, amount, maxAmount, defaultPosition, defaultAmount;
    String header = "", format = "", configCat = "", defaultFormat = "", defaultHeader = "";
    Comparator<Donation> comparator = CheckerHandler.AMOUNT_DONATION_COMPARATOR;
    List<Donation> donations = new ArrayList<>();

    public DonationsBasedHudEntry(String configCat, int maxAmount, int defaultPosition, int defaultAmount, String defaultFormat, String defaultHeader, Comparator<Donation> comparator)
    {
        this.configCat = configCat;
        this.maxAmount = maxAmount;
        this.defaultPosition = defaultPosition;
        this.defaultAmount = defaultAmount;
        this.defaultFormat = defaultFormat;
        this.defaultHeader = defaultHeader;

        this.comparator = comparator;
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

        format = Helper.formatColors(config.get(configCat, "format", defaultFormat).getString());
        header = Helper.formatColors(config.get(configCat, "header", defaultHeader, "Empty for no header. Use \\n for a blank line.").getString()).trim();
    }

    public void add(Donation donation)
    {
        donations.add(donation);
        Collections.sort(donations, comparator);
        update();
    }

    private void update()
    {
        while (donations.size() > getAmount())
        {
            donations.remove(donations.size() - 1);
        }

        strings.clear();
        if (!Strings.isNullOrEmpty(this.getHeader())) Helper.addWithEmptyLines(this.strings, this.getHeader());
        for (Donation donation : donations)
        {
            strings.add(Helper.formatText(this.getFormat(), donation, null));
        }
    }
}

