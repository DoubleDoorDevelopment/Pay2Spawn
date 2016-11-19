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

package net.doubledoordev.pay2spawn;

import com.google.common.io.Files;
import net.doubledoordev.pay2spawn.client.Pay2SpawnClient;
import net.doubledoordev.pay2spawn.network.Mp3FileMessage;
import net.doubledoordev.pay2spawn.network.RequestMp3Message;
import net.doubledoordev.pay2spawn.network.RewardMessage;
import net.doubledoordev.pay2spawn.trackers.Trackers;
import net.doubledoordev.pay2spawn.util.CommandPay2Spawn;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.RewardDB;
import net.doubledoordev.pay2spawn.util.ScriptHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@SuppressWarnings("WeakerAccess")
@Mod(modid = Helper.MOD_ID, name = Helper.MOD_NAME, acceptableRemoteVersions = "*")
public class Pay2Spawn
{
    @Mod.Instance(Helper.MOD_ID)
    public static Pay2Spawn instance;

    public static boolean allowTargeting = true;

    private Logger logger;
    private File configDir;
    private Configuration configuration;
    private SimpleNetworkWrapper snw;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        configDir = new File(event.getModConfigurationDirectory(), Helper.MOD_ID);
        File suggested = event.getSuggestedConfigurationFile();
        File configFile = new File(configDir, suggested.getName());
        try
        {
            if (suggested.exists()) Files.move(suggested, configFile);
        }
        catch (IOException e) // We tried :(
        {
            logger.info("Failed to move {} to {}.", suggested, configFile);
            logger.catching(e);
        }
        configuration = new Configuration(new File(configDir, event.getSuggestedConfigurationFile().getName()));
        doConfig();

        if (event.getSide().isClient()) Pay2SpawnClient.preInit(event);

        MinecraftForge.EVENT_BUS.register(this);

        ScriptHelper.init();

        int id = 1; // Start from 1, easier for debug
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(Helper.MOD_ID);

        snw.registerMessage(RewardMessage.Handler.class, RewardMessage.class, id++, Side.SERVER);
        snw.registerMessage(RequestMp3Message.Handler.class, RequestMp3Message.class, id++, Side.CLIENT);
        snw.registerMessage(RequestMp3Message.Handler.class, RequestMp3Message.class, id++, Side.SERVER);
        snw.registerMessage(Mp3FileMessage.Handler.class, Mp3FileMessage.class, id++, Side.CLIENT);
        snw.registerMessage(Mp3FileMessage.Handler.class, Mp3FileMessage.class, id++, Side.SERVER);

        //noinspection UnusedAssignment
        id++;
    }

    @EventHandler
    public void init(FMLInitializationEvent event) throws IOException
    {
        if (event.getSide().isClient()) Pay2SpawnClient.init(event);

        RewardDB.populate();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandPay2Spawn(Side.SERVER, "pay2spawn", "p2s"));
    }

    @SubscribeEvent
    public void configChangeEvent(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        doConfig();
    }

    private void doConfig()
    {
        configuration.addCustomCategoryComment(CATEGORY_GENERAL, "Make sure you read in README file!");

        allowTargeting = configuration.getBoolean("allowTargeting", CATEGORY_GENERAL, allowTargeting, "Allow '@<username>' in a note to target a specific player.");

        Trackers.config(configuration);

        if (FMLCommonHandler.instance().getSide().isClient()) Pay2SpawnClient.doConfig(configuration);
        if (configuration.hasChanged()) configuration.save();
    }

    public static File getConfigDir()
    {
        //noinspection ResultOfMethodCallIgnored
        instance.configDir.mkdirs();
        return instance.configDir;
    }

    public static Logger getLogger()
    {
        return instance.logger;
    }

    public static SimpleNetworkWrapper getSNW()
    {
        return instance.snw;
    }

    public static void reload() throws IOException
    {
        instance.doConfig();

        RewardDB.populate();
    }
}
