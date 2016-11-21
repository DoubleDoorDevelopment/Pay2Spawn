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

package net.doubledoordev.pay2spawn.trackers;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ICrashCallable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Dries007
 */
public class Trackers
{
    private Trackers() {}

    private static final Map<String, Tracker> TRACKERS = new HashMap<>();
    private static final Map<Tracker, Thread> TRACKER_THREADS = new HashMap<>();

    static
    {
        register(StreamLabsTracker.INSTANCE);

        FMLCommonHandler.instance().registerCrashCallable(new ICrashCallable()
        {
            @Override
            public String getLabel()
            {
                return Helper.MOD_NAME + "-Trackers";
            }

            @Override
            public String call() throws Exception
            {
                return Helper.SEMICOLON_JOINER.join(TRACKERS.values().stream().map((e) ->
                        e.getName() + " is " + (e.isEnabled() ? "enabled" : "disabled") + " and " +
                                (TRACKER_THREADS.containsKey(e) ? (TRACKER_THREADS.get(e).isAlive() ? "alive" : "dead") : "unused")
                ).collect(Collectors.toList()));
            }
        });
    }

    private static void register(Tracker tracker)
    {
        if (TRACKERS.containsKey(tracker.getName())) throw new IllegalArgumentException("Name already registered");
        TRACKERS.put(tracker.getName(), tracker);
    }

    public static void config(Configuration cfg)
    {
        for (final Tracker tracker : TRACKERS.values())
        {
            tracker.config(cfg);
            if (tracker.isEnabled())
            {
                Thread thread = TRACKER_THREADS.get(tracker);
                if (thread == null)
                {
                    thread = new Thread(tracker, Helper.MOD_NAME + "-Tracker-" + tracker.getName());
                    TRACKER_THREADS.put(tracker, thread);
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t, e) ->
                    {
                        Pay2Spawn.getLogger().error("Tracker thread ended with error {}", tracker.getName());
                        Pay2Spawn.getLogger().catching(e);
                    });
                }
                if (!thread.isAlive())
                {
                    thread.start();
                    Pay2Spawn.getLogger().info("Started tracker: {}", tracker.getName());
                }
            }
        }
    }
}
