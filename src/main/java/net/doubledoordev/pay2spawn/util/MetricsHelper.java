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

package net.doubledoordev.pay2spawn.util;

import com.google.common.base.Strings;
import cpw.mods.fml.common.FMLCommonHandler;
import net.doubledoordev.d3core.util.libs.org.mcstats.Metrics;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.checkers.AbstractChecker;
import net.doubledoordev.pay2spawn.checkers.CheckerHandler;

import java.io.IOException;

import static net.doubledoordev.pay2spawn.util.Constants.NAME;

/**
 * Collect all of the data!
 *
 * @author Dries007
 */
public class MetricsHelper
{
    public static double  totalMoney;
    public static Metrics metrics;

    public static void init()
    {
        if (metrics != null) return;
        try
        {
            metrics = new Metrics(NAME + "2", Pay2Spawn.getVersion());
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                metrics.createGraph("RewardCount").addPlotter(new Metrics.Plotter()
                {
                    @Override
                    public int getValue()
                    {
                        return Pay2Spawn.getRewardsDB().getRewards().size();
                    }
                });
                metrics.createGraph("MaxReward").addPlotter(new Metrics.Plotter()
                {
                    @Override
                    public int getValue()
                    {
                        return (int) (Helper.findMax(Pay2Spawn.getRewardsDB().getAmounts()));
                    }
                });
                String name = Pay2Spawn.getConfig().channel;
                if (Strings.isNullOrEmpty(name)) name = "Anonymous";
                metrics.createGraph("ChannelName").addPlotter(new Metrics.Plotter(name)
                {
                    @Override
                    public int getValue()
                    {
                        return 1;
                    }
                });
                Metrics.Graph graph = metrics.createGraph("Providers");
                for (final AbstractChecker abstractChecker : CheckerHandler.getAbstractCheckers())
                {
                    graph.addPlotter(new Metrics.Plotter(abstractChecker.getName())
                    {
                        @Override
                        public int getValue()
                        {
                            return abstractChecker.enabled() ? 1 : 0;
                        }
                    });
                }
            }
            metrics.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
