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

package net.doubledoordev.pay2spawn.network;

import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.Reward;
import net.doubledoordev.pay2spawn.util.ScriptHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Dries007
 */
public class RewardMessage implements IMessage
{
    private Reward reward;
    private Donation donation;

    @SuppressWarnings("unused")
    public RewardMessage()
    {
    }

    public RewardMessage(Reward reward, Donation donation)
    {
        this.reward = reward;
        this.donation = donation;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        reward = Reward.fromBytes(buf);
        donation = Donation.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        reward.toBytes(buf);
        donation.toBytes(buf);
    }

    public static class Handler implements IMessageHandler<RewardMessage, IMessage>
    {
        @Override
        public IMessage onMessage(RewardMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient()) return null;
            if (message.reward == null)
            {
                Helper.chat(ctx.getServerHandler().playerEntity, "The server could not load the reward script.", TextFormatting.RED);
                Helper.chat(ctx.getServerHandler().playerEntity, "The scripting language is most likely not supported.", TextFormatting.RED);
            }
            else ScriptHelper.execute(ctx.getServerHandler().playerEntity, message.reward, message.donation);
            return null;
        }
    }
}
