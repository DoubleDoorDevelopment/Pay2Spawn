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

package net.doubledoordev.pay2spawn.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.hud.DonatorBasedHudEntry;
import net.doubledoordev.pay2spawn.hud.Hud;
import net.doubledoordev.pay2spawn.util.Donation;
import net.doubledoordev.pay2spawn.util.Statistics;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Dries007
 */
public class UpdateMessage implements IMessage
{
    double amount;
    Collection<Donation> recent, topDonations;
    Collection<DonatorBasedHudEntry.Donator> topDonators;

    public UpdateMessage(double amount, Collection<Donation> recent, Collection<Donation> topDonations, Collection<DonatorBasedHudEntry.Donator> topDonators)
    {
        this.amount = amount;
        this.recent = recent;
        this.topDonations = topDonations;
        this.topDonators = topDonators;
    }

    public UpdateMessage()
    {
    }

    public UpdateMessage(double amount)
    {
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        amount = buf.readDouble();
        int size = buf.readInt();
        recent = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            recent.add(Donation.readFrom(buf));
        }
        size = buf.readInt();
        topDonations = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            topDonations.add(Donation.readFrom(buf));
        }
        size = buf.readInt();
        topDonators = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            topDonators.add(DonatorBasedHudEntry.Donator.readFrom(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeDouble(amount);
        if (recent == null) buf.writeInt(0);
        else
        {
            buf.writeInt(recent.size());
            for (Donation donation : recent)
            {
                Donation.writeTo(donation, buf);
            }
        }
        if (topDonations == null) buf.writeInt(0);
        else
        {
            buf.writeInt(topDonations.size());
            for (Donation donation : topDonations)
            {
                Donation.writeTo(donation, buf);
            }
        }
        if (topDonators == null) buf.writeInt(0);
        else
        {
            buf.writeInt(topDonators.size());
            for (DonatorBasedHudEntry.Donator donation : topDonators)
            {
                DonatorBasedHudEntry.Donator.writeTo(donation, buf);
            }
        }
    }

    public static class Handler implements IMessageHandler<UpdateMessage, IMessage>
    {
        @Override
        public IMessage onMessage(UpdateMessage message, MessageContext ctx)
        {
            if (ctx.side.isClient())
            {
                Statistics.setDonationAmount(message.amount);
                Hud.INSTANCE.recentDonationsBasedHudEntry.clear();
                Hud.INSTANCE.topDonationsBasedHudEntry.clear();
                Hud.INSTANCE.topDonatorsHudEntry.clear();

                for (Donation donation : message.recent) Hud.INSTANCE.recentDonationsBasedHudEntry.add(donation);
                for (Donation donation : message.topDonations) Hud.INSTANCE.topDonationsBasedHudEntry.add(donation);
                for (DonatorBasedHudEntry.Donator donator : message.topDonators) Hud.INSTANCE.topDonatorsHudEntry.add(donator);
            }
            return null;
        }
    }
}
