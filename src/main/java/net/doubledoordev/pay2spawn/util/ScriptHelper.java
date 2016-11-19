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

package net.doubledoordev.pay2spawn.util;

import com.google.common.base.Joiner;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.script.*;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Dries007
 */
public class ScriptHelper
{
    private static ScriptEngineManager mgr;

    private ScriptHelper() {}

    public static void init()
    {
        mgr = new ScriptEngineManager();
        dump();
    }

    public static ScriptEngine get(String language)
    {
        return mgr.getEngineByName(language);
    }

    public static String fromExtension(String extension)
    {
        ScriptEngine engine = mgr.getEngineByExtension(extension);
        if (engine == null) return null;
        return engine.getFactory().getLanguageName();
    }

    public static void dump()
    {
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();

        Pay2Spawn.getLogger().info("******************** Available scripting languages ********************");
        for (ScriptEngineFactory factory : factories)
        {
            Pay2Spawn.getLogger().info("Script Engine: {}({})", factory.getEngineName(), factory.getEngineVersion());
            Pay2Spawn.getLogger().info("\tLanguage: {}({})", factory.getLanguageName(), factory.getLanguageVersion());
            Pay2Spawn.getLogger().info("\tAliases: {}", Joiner.on(", ").join(factory.getNames()));
            Pay2Spawn.getLogger().info("\tExtensions: {}", Joiner.on(", ").join(factory.getExtensions()));
            Pay2Spawn.getLogger().info("");
        }
    }

    public static void execute(final ICommandSender runner, final Reward reward, final Donation donation)
    {
        try
        {
            EntityPlayerMP target = null;
            if (runner instanceof EntityPlayerMP) target = (EntityPlayerMP) runner;
            else if (runner.getCommandSenderEntity() instanceof EntityPlayerMP) target = (EntityPlayerMP) runner.getCommandSenderEntity();

            if (Pay2Spawn.allowTargeting)
            {
                Matcher matcher = Helper.AT_PATTERN.matcher(donation.note);
                MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

                if (matcher.find()) target = server.getPlayerList().getPlayerByUsername(matcher.group(1));
                if (target == null) target = Helper.pickRandom(server.getPlayerList().getPlayers());
            }

            if (target == null) throw new RuntimeException("No target available.");

            final ScriptContext context = new SimpleScriptContext();

            SimpleBindings bindings = new SimpleBindings();
            bindings.put("p2s", new ScriptUtils(target, runner, donation, reward));
            context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);

            context.setWriter(new Writer()
            {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException
                {
                    String out = new String(cbuf, off, len).trim();
                    if (!out.isEmpty()) Pay2Spawn.getLogger().info(out);
                }
                @Override public void flush() throws IOException {}
                @Override public void close() throws IOException {}
            });

            context.setErrorWriter(new Writer()
            {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException
                {
                    String out = new String(cbuf, off, len).trim();
                    if (!out.isEmpty()) Pay2Spawn.getLogger().warn(out);
                }
                @Override public void flush() throws IOException {}
                @Override public void close() throws IOException {}
            });

            new Thread(new CatchingRunnable(reward, donation, runner)
            {
                @Override
                public void runCatching() throws Exception
                {
                    ScriptHelper.get(reward.language).eval(reward.script, context);
                }
            }, String.format("%s-%s-SCRIPT", runner.getName(), reward.name)).start();
        }
        catch (Exception e)
        {
            Helper.chat(runner, "Error while starting reward script. See log for more info.", TextFormatting.RED);
            Pay2Spawn.getLogger().error("Caught error while starting reward {} with donation {}", reward, donation);
            Pay2Spawn.getLogger().catching(e);
            e.printStackTrace();
        }
    }

    private abstract static class CatchingRunnable implements Runnable
    {
        protected final Reward reward;
        protected final Donation donation;
        protected final ICommandSender runner;

        public CatchingRunnable(Reward reward, Donation donation, ICommandSender runner)
        {
            this.reward = reward;
            this.donation = donation;
            this.runner = runner;
        }

        public abstract void runCatching() throws Exception;

        @Override
        public void run()
        {
            try
            {
                this.runCatching();
            }
            catch (Exception e)
            {
                Pay2Spawn.getLogger().error("Caught error while running reward {} with donation {}", reward, donation);
                Pay2Spawn.getLogger().catching(e);
                e.printStackTrace();

                try
                {
                    Helper.chat(runner, "Error while running reward script. See log for more info.", TextFormatting.RED);
                }
                catch (Exception ignored)
                {
                    // We tried :(
                }
            }
        }
    }

}
