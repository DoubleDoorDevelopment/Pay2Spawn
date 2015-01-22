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

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.hud.DonationsBasedHudEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Statistics;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

import java.util.HashSet;

/**
 * Base class for all donation checkers
 *
 * @author Dries007
 */
public abstract class AbstractChecker
{
    public    double            min_donation = 1;
    protected HashSet<String>   doneIDs      = new HashSet<>();
    protected HashSet<Donation> backlog      = new HashSet<>();

    protected AbstractChecker()
    {
    }

    public abstract String getName();

    public abstract void init();

    public abstract boolean enabled();

    public abstract void doConfig(Configuration configuration);

    public boolean addToTotal()
    {
        return true;
    }

    protected void doWait(int time)
    {
        try
        {
            synchronized (this)
            {
                this.wait(time * 1000);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    protected void process(Donation donation, boolean msg, AbstractChecker tracker)
    {
        if (Minecraft.getMinecraft().thePlayer == null)
        {
            if (!backlog.contains(donation)) backlog.add(donation);
            return;
        }

        if (!doneIDs.contains(donation.id))
        {
            doneIDs.add(donation.id);
            if (donation.amount > 0 && tracker.addToTotal()) // Only do these things for real donation amounts.
            {
                Statistics.addToDonationAmount(donation.amount);
                if (donation.amount < min_donation) return;
            }
            try
            {
                Hud.INSTANCE.topDonationsBasedHudEntry.add(donation);
                Hud.INSTANCE.recentDonationsBasedHudEntry.add(donation);
                Pay2Spawn.getRewardsDB().process(donation, msg);
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().warn("Error processing a donation with " + this.getName());
                e.printStackTrace();
            }
        }
    }
}
