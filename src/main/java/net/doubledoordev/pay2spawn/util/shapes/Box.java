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
 *  Neither the name of DoubleDoorDevelopment nor the names of its
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

import net.doubledoordev.pay2spawn.types.guis.StructureTypeGui;
import net.doubledoordev.pay2spawn.types.guis.shapes.BoxGui;
import net.doubledoordev.pay2spawn.util.Helper;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.INT;
import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;

/**
 * Spawns a box
 *
 * @author Dries007
 */
public class Box extends AbstractShape
{
    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String Z_KEY = "z";

    static
    {
        typeMap.put(X_KEY, NBTTypes[INT]);
        typeMap.put(Y_KEY, NBTTypes[INT]);
        typeMap.put(Z_KEY, NBTTypes[INT]);
    }

    int x, y, z;

    public Box(int x, int y, int z)
    {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Box(PointI center, int x, int y, int z)
    {
        super(center);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Box()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger(X_KEY, x);
        compound.setInteger(Y_KEY, y);
        compound.setInteger(Z_KEY, z);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        x = compound.getInteger(X_KEY);
        y = compound.getInteger(Y_KEY);
        z = compound.getInteger(Z_KEY);
        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        HashSet<PointI> points = new HashSet<>();

        for (int x = -this.x; x <= this.x; x++)
        {
            for (int z = -this.z; z <= this.z; z++)
            {
                for (int y = -this.y; y <= this.y; y++)
                {
                    points.add(new PointI(center.x + x, center.y + y, center.z + z));
                }
            }
        }

        if (hollow) points.removeAll(new Box(center, x - 1, y - 1, z - 1).getPoints());

        return points;
    }

    @Override
    public void openGui(int index, JsonObject jsonObject, StructureTypeGui instance)
    {
        new BoxGui(index, jsonObject, instance, typeMap);
    }

    private Collection<PointI> temppoints;
    @Override
    public void render(Tessellator tess)
    {
        if (temppoints == null) temppoints = getPoints();
        for (PointI pointI : temppoints)
        {
            Helper.renderPoint(pointI, tess);
        }
    }

    @Override
    public IShape cloneShape()
    {
        return new Box(center, x, y, z);
    }

    @Override
    public IShape rotate(int baseRotation)
    {
        super.rotate(baseRotation);

        switch (baseRotation)
        {
            case 1:
                int tempx = x;
                x = -z;
                z = -tempx;
                break;
            case 2:
                x *= -1;
                z *= -1;
                break;
            case 3:
                int tempz = z;
                z = x;
                x = tempz;
                break;
        }

        return this;
    }
}
