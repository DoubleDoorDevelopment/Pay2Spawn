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
import net.doubledoordev.pay2spawn.types.guis.shapes.CylinderGui;
import net.doubledoordev.pay2spawn.util.Helper;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.INT;
import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;

/**
 * Spawns a cylinder
 *
 * @author Dries007
 */
public class Cylinder extends AbstractShape
{
    public static final String RADIUS_KEY = "radius";
    public static final String HEIGHT_KEY = "height";

    static
    {
        typeMap.put(RADIUS_KEY, NBTTypes[INT]);
        typeMap.put(HEIGHT_KEY, NBTTypes[INT]);
    }

    int radius, height = 0;

    public Cylinder(int radius)
    {
        super();
        this.radius = radius;
    }

    public Cylinder(int radius, int height)
    {
        super();
        this.radius = radius;
        this.height = height;
    }

    public Cylinder(PointI center, int radius)
    {
        super(center);
        this.radius = radius;
    }

    public Cylinder(PointI center, int radius, int height)
    {
        super(center);
        this.radius = radius;
        this.height = height;
    }

    Cylinder()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger(RADIUS_KEY, radius);
        compound.setInteger(HEIGHT_KEY, height);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        this.radius = compound.getInteger(RADIUS_KEY);
        this.height = compound.getInteger(HEIGHT_KEY);
        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        HashSet<PointI> points = new HashSet<>();

        int d = (5 - radius * 4)/4;
        int x = 0;
        int z = radius;

        do
        {
            for (int y = -height; y <= height; y++)
            {
                if (hollow)
                {
                    points.add(new PointI(center.x + x, center.y + y, center.z + z));
                    points.add(new PointI(center.x + x, center.y + y, center.z - z));
                    points.add(new PointI(center.x - x, center.y + y, center.z + z));
                    points.add(new PointI(center.x - x, center.y + y, center.z - z));

                    points.add(new PointI(center.x + z, center.y + y, center.z + x));
                    points.add(new PointI(center.x + z, center.y + y, center.z - x));
                    points.add(new PointI(center.x - z, center.y + y, center.z + x));
                    points.add(new PointI(center.x - z, center.y + y, center.z - x));
                }
                else
                {
                    for (int z2 = -z; z2 <= z; z2++)
                    {
                        points.add(new PointI(center.x + x, center.y + y, center.z + z2));
                        points.add(new PointI(center.x - x, center.y + y, center.z + z2));
                    }

                    for (int x2 = -x; x2 <= x; x2++)
                    {
                        points.add(new PointI(center.x + z, center.y + y, center.z + x2));
                        points.add(new PointI(center.x - z, center.y + y, center.z + x2));
                    }
                }
            }

            if (d < 0)
            {
                d += 2 * x + 1;
            }
            else
            {
                d += 2 * (x - z) + 1;
                z--;
            }
            x++;
        }
        while (x <= z);

        return points;
    }

    @Override
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new CylinderGui(i, jsonObject, instance, typeMap);
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
        return new Cylinder(center, radius, height);
    }
}
