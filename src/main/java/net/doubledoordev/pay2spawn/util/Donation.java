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

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.ANONYMOUS;

/**
 * A donation in java object style
 *
 * @author Dries007
 */
public class Donation
{
    public String id;
    public double amount;
    public String username;
    public String note;
    public long   time;
    public String target;

    public Donation(String id, double amount, long time)
    {
        this.id = id;
        this.amount = amount;
        this.time = time;

        this.username = "Anonymous";
        this.note = "";
    }

    public Donation(String id, double amount, long time, String username)
    {
        this.id = id;
        this.amount = amount;
        this.time = time;

        for (Pattern p : Pay2Spawn.getConfig().blacklist_Name_p)
        {
            if (p.matcher(username).matches())
            {
                username = ANONYMOUS;
                break;
            }
        }
        this.username = username;
        this.note = "";
    }

    public Donation(String id, double amount, long time, String username, String note)
    {
        this.id = id;
        this.amount = amount;
        this.time = time;

        for (Pattern p : Pay2Spawn.getConfig().blacklist_Name_p)
        {
            if (p.matcher(username).matches())
            {
                username = ANONYMOUS;
                break;
            }
        }
        this.username = username;
        for (Pattern p : Pay2Spawn.getConfig().blacklist_Note_p)
        {
            if (p.matcher(note).matches())
            {
                note = "";
                break;
            }
        }
        this.note = note;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = id.hashCode();
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + username.hashCode();
        result = 31 * result + note.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Donation)) return false;

        Donation donation = (Donation) o;

        if (Double.compare(donation.amount, amount) != 0) return false;
        if (time != donation.time) return false;
        if (!id.equals(donation.id)) return false;
        if (!note.equals(donation.note)) return false;
        if (!username.equals(donation.username)) return false;
        if (!target.equals(donation.target)) return false;

        return true;
    }

    @Override
    public String toString()
    {
        return "Donation[" + id + ", " + amount + ", " + username + ", " + note + ", " + time + ", " + target + ']';
    }

    public static Donation readFrom(ByteBuf buf)
    {
        Donation donation = new Donation(ByteBufUtils.readUTF8String(buf), buf.readDouble(), buf.readLong(), ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf));
        donation.target = ByteBufUtils.readUTF8String(buf);
        return donation;
    }

    public static void writeTo(Donation donation, ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, donation.id);
        buf.writeDouble(donation.amount);
        buf.writeLong(donation.time);
        ByteBufUtils.writeUTF8String(buf, donation.username);
        ByteBufUtils.writeUTF8String(buf, donation.note);
        ByteBufUtils.writeUTF8String(buf, donation.target);
    }
}
