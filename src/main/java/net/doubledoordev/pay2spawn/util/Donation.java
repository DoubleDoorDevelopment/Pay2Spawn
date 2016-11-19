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

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.client.Pay2SpawnClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import java.util.UUID;
import java.util.regex.Matcher;

/**
 * @author Dries007
 */
public class Donation
{
    public final String name;
    public final double amount;
    public final long timestamp;
    public final String note;

    public Donation(String name, double amount, long timestamp, String note)
    {
        this.name = Strings.isNullOrEmpty(name) ? Helper.ANON : name;
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = Strings.nullToEmpty(note);
    }

    public Donation(String name, double amount, long timestamp)
    {
        this(name, amount, timestamp, null);
    }

    public Donation(double amount, long timestamp)
    {
        this(null, amount, timestamp, null);
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + note.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Donation donation = (Donation) o;

        if (Double.compare(donation.amount, amount) != 0) return false;
        if (timestamp != donation.timestamp) return false;
        if (!name.equals(donation.name)) return false;
        return note.equals(donation.note);

    }

    @Override
    public String toString()
    {
        return "Donation[" + name + ';' + amount + ';' + note + ';' + timestamp + ']';
    }

    public static Donation fromBytes(ByteBuf buf)
    {
        String name = ByteBufUtils.readUTF8String(buf);
        double amount = buf.readDouble();
        long timestamp = buf.readLong();
        String note = ByteBufUtils.readUTF8String(buf);
        return new Donation(name, amount, timestamp, note);
    }

    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeDouble(amount);
        buf.writeLong(timestamp);
        ByteBufUtils.writeUTF8String(buf, note);
    }
}
