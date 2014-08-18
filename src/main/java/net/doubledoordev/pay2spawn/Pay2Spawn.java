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

package net.doubledoordev.pay2spawn;

import com.google.common.base.Strings;
import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.doubledoordev.d3core.util.ID3Mod;
import net.doubledoordev.pay2spawn.checkers.CheckerHandler;
import net.doubledoordev.pay2spawn.checkers.TwitchChecker;
import net.doubledoordev.pay2spawn.cmd.CommandP2S;
import net.doubledoordev.pay2spawn.cmd.CommandP2SPermissions;
import net.doubledoordev.pay2spawn.cmd.CommandP2SServer;
import net.doubledoordev.pay2spawn.configurator.ConfiguratorManager;
import net.doubledoordev.pay2spawn.configurator.HTMLGenerator;
import net.doubledoordev.pay2spawn.network.*;
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.doubledoordev.pay2spawn.types.TypeBase;
import net.doubledoordev.pay2spawn.types.TypeRegistry;
import net.doubledoordev.pay2spawn.util.*;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.ConfigElement;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * The main mod class
 *
 * @author Dries007
 */
@Mod(modid = MODID, name = NAME)
public class Pay2Spawn implements ID3Mod
{
    @Mod.Instance(MODID)
    public static Pay2Spawn instance;
    public static boolean enable  = true;
    public static boolean forceOn = false;

    @Mod.Metadata(MODID)
    private ModMetadata          metadata;
    private RewardsDB            rewardsDB;
    private P2SConfig            config;
    private File                 configFolder;
    private Logger               logger;
    private SimpleNetworkWrapper snw;
    private boolean              newConfig;

    public static String getVersion()
    {
        return instance.metadata.version;
    }

    public static RewardsDB getRewardsDB()
    {
        return instance.rewardsDB;
    }

    public static Logger getLogger() { return instance.logger; }

    public static P2SConfig getConfig() { return instance.config; }

    public static File getFolder()
    {
        return instance.configFolder;
    }

    public static SimpleNetworkWrapper getSnw()
    {
        return instance.snw;
    }

    public static File getRewardDBFile() { return new File(instance.configFolder, NAME + ".json"); }

    public static void reloadDB()
    {
        instance.rewardsDB = new RewardsDB(new File(instance.configFolder, NAME + ".json"));
        ConfiguratorManager.reload();
        try
        {
            PermissionsHandler.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void reloadDB_Server() throws Exception
    {
        StatusMessage.serverConfig = GSON_NOPP.toJson(JSON_PARSER.parse(new FileReader(new File(instance.configFolder, NAME + ".json"))));
        StatusMessage.sendConfigToAllPlayers();
    }

    public static void reloadDBFromServer(String input)
    {
        instance.rewardsDB = new RewardsDB(input);
        ConfiguratorManager.reload();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException
    {
        logger = event.getModLog();

        configFolder = new File(event.getModConfigurationDirectory(), NAME);
        //noinspection ResultOfMethodCallIgnored
        configFolder.mkdirs();

        File configFile = new File(configFolder, NAME + ".cfg");
        newConfig = !configFile.exists();
        config = new P2SConfig(configFile);
        MetricsHelper.init();

        int id = 0;
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        snw.registerMessage(MessageMessage.Handler.class, MessageMessage.class, id++, Side.SERVER);
        snw.registerMessage(MusicMessage.Handler.class, MusicMessage.class, id++, Side.CLIENT);
        snw.registerMessage(NbtRequestMessage.Handler.class, NbtRequestMessage.class, id++, Side.CLIENT);
        snw.registerMessage(NbtRequestMessage.Handler.class, NbtRequestMessage.class, id++, Side.SERVER);
        snw.registerMessage(RewardMessage.Handler.class, RewardMessage.class, id++, Side.SERVER);
        snw.registerMessage(StatusMessage.Handler.class, StatusMessage.class, id++, Side.SERVER);
        snw.registerMessage(StatusMessage.Handler.class, StatusMessage.class, id++, Side.CLIENT);
        snw.registerMessage(TestMessage.Handler.class, TestMessage.class, id++, Side.SERVER);
        snw.registerMessage(StructureImportMessage.Handler.class, StructureImportMessage.class, id++, Side.SERVER);
        snw.registerMessage(StructureImportMessage.Handler.class, StructureImportMessage.class, id++, Side.CLIENT);

        TypeRegistry.preInit();
        Statistics.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws MalformedURLException
    {
        ServerTickHandler.INSTANCE.init();
        config.syncConfig();

        rewardsDB = new RewardsDB(getRewardDBFile());

        if (event.getSide().isClient())
        {
            CheckerHandler.init();
            new EventHandler();
            ClientCommandHandler.instance.registerCommand(new CommandP2S());
        }

        ClientTickHandler.INSTANCE.init();
        ConnectionHandler.INSTANCE.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        for (TypeBase base : TypeRegistry.getAllTypes()) base.printHelpList(configFolder);

        TypeRegistry.registerPermissions();
        try
        {
            HTMLGenerator.init();
        }
        catch (IOException e)
        {
            logger.warn("Error initializing the HTMLGenerator.");
            e.printStackTrace();
        }

        if (newConfig)
        {
            JOptionPane.showMessageDialog(null, "Please configure Pay2Spawn properly BEFORE you try launching this instance again.\n" +
                            "You should provide AT LEAST your channel in the config. Pay2Spawn will crash otherwise.\n\n" +
                            "If you need help with the configuring of your rewards, contact us!", "Please configure Pay2Spawn!", JOptionPane.WARNING_MESSAGE
                                         );
            FMLCommonHandler.instance().handleExit(1);
        }

        if (Pay2Spawn.getConfig().majorConfigVersionChange)
        {
            JOptionPane.showMessageDialog(null, "Please reconfigure Pay2Spawn properly BEFORE you try launching this instance again.\n" +
                            "There have been major config changes.\n" +
                            "We made a backup for you, you should start fresh to avoid clutter.", "Please reconfigure Pay2Spawn!", JOptionPane.WARNING_MESSAGE
                                         );
            FMLCommonHandler.instance().handleExit(1);
        }

        boolean deobf = Launch.blackboard.containsKey("fml.deobfuscatedEnvironment") ? Boolean.valueOf(Launch.blackboard.get("fml.deobfuscatedEnvironment").toString()) : false;

        if (Strings.isNullOrEmpty(TwitchChecker.INSTANCE.getChannel()) && !MetricsHelper.metrics.isOptOut() && event.getSide().isClient() && !deobf)
        {
            JOptionPane.showMessageDialog(null, "You must provide your channel in the config for statistics.\n" +
                            "If you don't agree with this, opt out of the statistics program all together trough the 'PluginMetrics' config file.\n\n" +
                            "Important note: Don't send the PluginMetrics config to other users, that will screw up analytics.", "Please configure Pay2Spawn!", JOptionPane.WARNING_MESSAGE
                                         );
            FMLCommonHandler.instance().handleExit(1);
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) throws IOException
    {
        PermissionsHandler.init();
        try
        {
            StatusMessage.serverConfig = GSON_NOPP.toJson(JSON_PARSER.parse(new FileReader(new File(instance.configFolder, NAME + ".json"))));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        event.registerServerCommand(new CommandP2SPermissions());
        event.registerServerCommand(new CommandP2SServer());
    }

    @Override
    public void syncConfig()
    {
        config.syncConfig();
    }

    @Override
    public void addConfigElements(List<IConfigElement> configElements)
    {
        List<IConfigElement> listsList = new ArrayList<IConfigElement>();

        listsList.add(new ConfigElement(config.configuration.getCategory(Constants.MODID.toLowerCase())));
        listsList.add(new ConfigElement(config.configuration.getCategory(Constants.FILTER_CAT.toLowerCase())));
        listsList.add(new ConfigElement(config.configuration.getCategory(Constants.SERVER_CAT.toLowerCase())));
        listsList.add(new ConfigElement(config.configuration.getCategory(Constants.BASECAT_TRACKERS.toLowerCase())));

        configElements.add(new DummyConfigElement.DummyCategoryElement(MODID, "d3.pay2spawn.config.pay2spawn", listsList));
    }
}
