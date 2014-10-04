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

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Thanks FE :p
 *
 * @author Dries007
 */
public class PointD
{
    private double x, y, z;

    public PointD(Entity entity)
    {
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
    }

    public PointD(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PointD()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public PointD makeNiceForBlock()
    {
        x = intX() + 0.5;
        y = intY();
        z = intZ() + 0.5;

        return this;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.z = z;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public ArrayList<PointD> getCircle(int rad)
    {
        ArrayList<PointD> pointDs = new ArrayList<>();
        for (int x = -rad; x < rad; x++)
        {
            for (int z = -rad; z < rad; z++)
            {
                PointD p = new PointD(this.x + x, this.y, this.z + z);
                if (distanceTo(p) < rad) pointDs.add(p);
            }
        }
        return pointDs;
    }

    public ArrayList<PointD> getCylinder(int rad, int height)
    {
        height = height / 2;
        ArrayList<PointD> pointDs = new ArrayList<>();
        for (PointD p : getCircle(rad))
        {
            for (int dy = -height; dy < height; dy++)
            {
                pointDs.add(new PointD(p.x, p.y + dy, p.z));
            }
        }
        return pointDs;
    }

    public ArrayList<PointD> getSphere(int rad)
    {
        ArrayList<PointD> pointDs = new ArrayList<>();
        for (PointD p : getCylinder(rad, rad)) if (distanceTo(p) < rad) pointDs.add(p);
        return pointDs;
    }

    public double distanceTo(PointD p)
    {
        return Math.sqrt((diffX(p) * diffX(p)) + (diffY(p) * diffY(p)) + (diffZ(p) * diffZ(p)));
    }

    public double diffX(PointD p)
    {
        return this.x - p.x;
    }

    public double diffY(PointD p)
    {
        return this.y - p.y;
    }

    public double diffZ(PointD p)
    {
        return this.z - p.z;
    }

    public boolean isValid()
    {
        return this.y >= 0;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointD pointD = (PointD) o;

        return Double.compare(pointD.x, x) == 0 && Double.compare(pointD.y, y) == 0 && Double.compare(pointD.z, z) == 0;
    }

    @Override
    public String toString()
    {
        return "[" + x + ";" + y + ";" + z + "]";
    }

    public int intX()
    {
        return (int) x;
    }

    public int intY()
    {
        return (int) y;
    }

    public int intZ()
    {
        return (int) z;
    }

    public void setPosition(Entity entity)
    {
        entity.setPosition(intX() + 0.5, intY() + 0.5, intZ() + 0.5);
    }

    public boolean canSpawn(Entity entity)
    {
        World world = entity.worldObj;
        for (int y = 0; y < Math.ceil(entity.height); y++) if (world.isBlockNormalCubeDefault(intX(), intY() + y, intZ(), false)) return false;
        return true;
    }
}
