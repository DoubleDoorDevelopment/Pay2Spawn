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
import net.doubledoordev.pay2spawn.P2SConfig;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.RewardsDB;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Dries007
 */
public class SaleEntry implements IHudEntry
{
    int position;
    String format = "";
    int amount = 0;
    private String noSaleMessage = "";
    private boolean writeToFile = true;

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
        return "";
    }

    @Override
    public String getFormat()
    {
        return format;
    }

    @Override
    public void addToList(ArrayList<String> list)
    {
        RewardsDB.Sale sale = Pay2Spawn.getRewardsDB().getLastSale();

        if (sale == null)
        {
            if (!Strings.isNullOrEmpty(noSaleMessage)) list.add(noSaleMessage);
        }
        else
        {
            list.add(format.replace("$amount", String.valueOf(sale.amount)).replace("$time", Helper.getTimeString((int) (sale.time - System.currentTimeMillis()))));
        }
    }

    @Override
    public void updateConfig()
    {
        Configuration config = Pay2Spawn.getConfig().configuration;
        String configCat = "Sales";
        position = config.get(P2SConfig.HUD + "." + configCat, "position", 1, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(2);
        format = Helper.formatColors(config.get(P2SConfig.HUD + "." + configCat, "format", "SALE! $amount% off for $time").getString());
        noSaleMessage = config.get(P2SConfig.HUD + "." + configCat, "noSaleMessage", "No sale right now...").getString();
        writeToFile = config.getBoolean("writeToFile", configCat, writeToFile, "Write to a file for external use.");
    }

    @Override
    public String getFilename()
    {
        return "sale.txt";
    }

    @Override
    public boolean writeToFile()
    {
        return writeToFile;
    }
}
