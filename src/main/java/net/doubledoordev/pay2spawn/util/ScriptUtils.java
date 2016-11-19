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

import com.google.common.util.concurrent.ListenableFuture;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.network.RequestMp3Message;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static net.minecraftforge.common.ForgeHooks.newChatWithLinks;

/**
 * This is all accessible in the scripting environment.
 * Please keep the resource Pay2Spawn.js up to date.
 *
 * @author Dries007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ScriptUtils
{
    public final ICommandSender runner;
    public final Donation donation;
    public final Reward reward;
    public final Logger logger = Pay2Spawn.getLogger();
    public final EntityPlayerMP target;
    public final MinecraftServer server;
    public final ICommandSender targetCommandSender;

    public ScriptUtils(EntityPlayerMP target, ICommandSender runner, Donation donation, Reward reward)
    {
        this.runner = runner;
        this.donation = donation;
        this.reward = reward;
        this.target = target;
        this.server = target.getServer();
        this.targetCommandSender = new TargetCommandSender(target);
    }

    /**
     * @param parts will be joined by a space (' ')
     */
    public void speak(final String... parts) throws ExecutionException, InterruptedException
    {
        server.addScheduledTask(new Runnable() {
            @Override
            public void run()
            {
                String s = Helper.SPACE_JOINER.join(parts);
                server.getPlayerList().sendChatMsgImpl(new TextComponentTranslation("chat.type.text", target.getDisplayName(), newChatWithLinks(s)), false);
            }
        });
    }

    /**
     * @param parts will be joined by a space (' ')
     */
    public void chat(final String... parts) throws ExecutionException, InterruptedException
    {
        server.addScheduledTask(new Runnable() {
            @Override
            public void run()
            {
                Helper.chat(target, Helper.SPACE_JOINER.join(parts));
            }
        });
    }

    /**
     * @param parts will be joined by a space (' ')
     */
    public int cmd(final String... parts) throws ExecutionException, InterruptedException
    {
        return run(new Callable<Integer>()
        {
            @Override
            public Integer call() throws Exception
            {
                return server.getCommandManager().executeCommand(targetCommandSender, Helper.SPACE_JOINER.join(parts));
            }
        });
    }

    public void mp3(final String name)
    {
        server.addScheduledTask(new Runnable() {
            @Override
            public void run()
            {
                Pay2Spawn.getSNW().sendTo(new RequestMp3Message(name), target);
            }
        });
    }

    /**
     * Must be used to manipulate the server!
     */
    public <T> T run(Callable<T> func) throws ExecutionException, InterruptedException
    {
        return async(func).get();
    }

    public <T> ListenableFuture<T> async(Callable<T> func)
    {
        return server.callFromMainThread(func);
    }

    /**
     * Convenience method
     */
    public void log(Object anything)
    {
        logger.info(anything);
    }

    /**
     * Convenience method
     */
    public void log(String message, Object... params)
    {
        logger.info(message, params);
    }

    public WorldServer getWorld()
    {
        return target.getServerWorld();
    }
}
