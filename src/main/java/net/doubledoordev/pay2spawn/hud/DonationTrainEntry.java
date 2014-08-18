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

import net.doubledoordev.pay2spawn.P2SConfig;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;

/**
 * @author Dries007
 */
public class DonationTrainEntry implements IHudEntry
{
    public final ArrayList<String> strings = new ArrayList<>();
    int position;
    String format = "";
    private String timeoutMessage = "";
    private int timeout;
    int time = -1, amount = 0;
    private String line = "";

    public DonationTrainEntry()
    {

    }

    @Override
    public int getPosition()
    {
        return position;
    }

    @Override
    public int getAmount()
    {
        return 1;
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
        list.add(line);
    }

    @Override
    public void updateConfig()
    {
        Configuration config = Pay2Spawn.getConfig().configuration;
        String configCat = "DonationTrain";
        position = config.get(P2SConfig.HUD + "." + configCat, "position", 2, "0 = off, 1 = left top, 2 = right top, 3 = left bottom, 4 = right bottom.").getInt(2);
        format = Helper.formatColors(config.get(P2SConfig.HUD + "." + configCat, "format", "Donationtrain! $amount donations already! Expires in $time.").getString());
        line = timeoutMessage = config.get(P2SConfig.HUD + "." + configCat, "timeoutMessage", "No donation train going :(").getString();
        timeout = config.get(P2SConfig.HUD + "." + configCat, "timeout", 60 * 3).getInt();
    }

    public void resetTimeout()
    {
        amount ++;
        time = timeout;
    }

    public void tick()
    {
        if (time > 0) time --;
        else return;

        if (time == 0)
        {
            line = timeoutMessage;
            amount = 0;
        }
        else
        {
            line = format.replace("$amount", amount + "").replace("$time", time + "");
        }
    }
}
