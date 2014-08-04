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
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;

import java.util.*;

/**
 * Registers and polls all donation checkers
 *
 * @author Dries007
 */
public class CheckerHandler
{
    public static final Comparator<Donation> RECENT_DONATION_COMPARATOR = new Comparator<Donation>()
    {
        @Override
        public int compare(Donation o1, Donation o2)
        {
            if (o1.time == o2.time) return 0;
            return o1.time > o2.time ? -1 : 1;
        }
    };
    public static final Comparator<Donation> AMOUNT_DONATION_COMPARATOR = new Comparator<Donation>()
    {
        @Override
        public int compare(Donation o1, Donation o2)
        {
            if (o1.amount == o2.amount) return 0;
            return o1.amount > o2.amount ? -1 : 1;
        }
    };

    private static HashMap<String, AbstractChecker> map = new HashMap<>();

    static
    {
        register(StreamtipChecker.INSTANCE);
        register(ChildsplayChecker.INSTANCE);
        register(TwitchChecker.INSTANCE);
        register(DonationTrackerChecker.INSTANCE);
    }

    public static Collection<AbstractChecker> getAbstractCheckers()
    {
        return map.values();
    }

    public static void register(AbstractChecker abstractChecker)
    {
        map.put(abstractChecker.getName(), abstractChecker);
    }

    public static void doConfig(Configuration configuration)
    {
        for (AbstractChecker abstractChecker : map.values())
        {
            abstractChecker.doConfig(configuration);
        }
    }

    public static void init()
    {
        for (AbstractChecker abstractChecker : map.values())
        {
            if (abstractChecker.enabled()) abstractChecker.init();
        }
    }

    public static void fakeDonation(double amount)
    {
        Donation donation = new Donation(UUID.randomUUID().toString(), amount, new Date().getTime());
        Helper.msg(EnumChatFormatting.GOLD + "[P2S] Faking donation of " + amount + ".");
        Pay2Spawn.getRewardsDB().process(donation, false);
    }
}
