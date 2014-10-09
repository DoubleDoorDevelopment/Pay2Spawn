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

package net.doubledoordev.pay2spawn.util.shapes;

import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.types.guis.StructureTypeGui;
import net.doubledoordev.pay2spawn.types.guis.shapes.PointIGui;
import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Spawns a point
 *
 * @author Dries007
 */
public class PointI implements IShape
{
    public static final HashMap<String, String> typeMap             = new HashMap<>();
    public static final String                  HOLLOWCENTER_KEY    = "hollow";
    public static final String                  REPLACEABLEONLY_KEY = "replaceableOnly";
    public static final String                  X_KEY               = "x";
    public static final String                  Y_KEY               = "y";
    public static final String                  Z_KEY               = "z";

    static
    {
        typeMap.put(HOLLOWCENTER_KEY, NBTTypes[BYTE]);
        typeMap.put(REPLACEABLEONLY_KEY, NBTTypes[BYTE]);

        typeMap.put(X_KEY, NBTTypes[INT]);
        typeMap.put(Y_KEY, NBTTypes[INT]);
        typeMap.put(Z_KEY, NBTTypes[INT]);
    }

    int x, y, z;
    boolean hollow, replaceableOnly;

    public PointI(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PointI()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public PointI(NBTTagCompound compound)
    {
        fromNBT(compound);
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public double distanceTo(PointI p)
    {
        return Math.sqrt((diffX(p) * diffX(p)) + (diffY(p) * diffY(p)) + (diffZ(p) * diffZ(p)));
    }

    public double diffX(PointI p)
    {
        return this.x - p.x;
    }

    public double diffY(PointI p)
    {
        return this.y - p.y;
    }

    public double diffZ(PointI p)
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
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PointI)) return false;

        PointI pointI = (PointI) o;

        return x == pointI.x && y == pointI.y && z == pointI.z;
    }

    @Override
    public String toString()
    {
        return "[" + x + ";" + y + ";" + z + "]";
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("x", x);
        compound.setInteger("y", y);
        compound.setInteger("z", z);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        x = compound.getInteger("x");
        y = compound.getInteger("y");
        z = compound.getInteger("z");
        return this;
    }

    @Override
    public IShape move(int x, int y, int z)
    {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        return Arrays.asList(this);
    }

    @Override
    public PointI getCenter()
    {
        return this;
    }

    @Override
    public IShape setCenter(PointI pointI)
    {
        x = pointI.x;
        y = pointI.y;
        z = pointI.z;
        return this;
    }

    @Override
    public boolean getHollow()
    {
        return hollow;
    }

    @Override
    public IShape setHollow(boolean hollow)
    {
        this.hollow = hollow;
        return this;
    }

    @Override
    public boolean getReplaceableOnly()
    {
        return replaceableOnly;
    }

    @Override
    public IShape setReplaceableOnly(boolean replaceableOnly)
    {
        this.replaceableOnly = replaceableOnly;
        return this;
    }

    @Override
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new PointIGui(i, jsonObject, instance, typeMap);
    }

    @Override
    public void render(Tessellator tess)
    {
        Helper.renderPoint(this, tess);
    }

    @Override
    public IShape rotate(int baseRotation)
    {
        switch (baseRotation)
        {
            case 1:
                int tempx = x;
                x = -z;
                z = -tempx;
                break;
            case 2:
                x = -x;
                z = -z;
                break;
            case 3:
                int tempz = z;
                z = x;
                x = tempz;
                break;
        }
        return this;
    }

    public PointI addX(int x)
    {
        this.x += x;
        return this;
    }

    public PointI addY(int y)
    {
        this.y += y;
        return this;
    }

    public PointI addZ(int z)
    {
        this.z += z;
        return this;
    }

    public IShape copy()
    {
        return new PointI(x, y, z);
    }
}
