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

import net.doubledoordev.pay2spawn.types.guis.StructureTypeGui;
import net.doubledoordev.pay2spawn.types.guis.shapes.SphereGui;
import net.doubledoordev.pay2spawn.util.Helper;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.INT;
import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;

/**
 * Spawns a shape
 *
 * @author Dries007
 */
public class Sphere extends AbstractShape
{
    public static final String RADIUS_KEY = "radius";

    static
    {
        typeMap.put(RADIUS_KEY, NBTTypes[INT]);
    }

    int radius;

    public Sphere(int radius)
    {
        super();
        this.radius = radius;
    }

    public Sphere(PointI center, int radius)
    {
        super(center);
        this.radius = radius;
    }

    Sphere()
    {
        super();
    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = super.toNBT();
        compound.setInteger(RADIUS_KEY, radius);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        super.fromNBT(compound);
        radius = compound.getInteger(RADIUS_KEY);
        return this;
    }

    @Override
    public Collection<PointI> getPoints()
    {
        HashSet<PointI> points = new HashSet<>();

        for (int x = -radius; x <= radius; x++)
        {
            for (int z = -radius; z <= radius; z++)
            {
                for (int y = -radius; y <= radius; y++)
                {
                    PointI p = new PointI(center.x + x, center.y + y, center.z + z);
                    if (center.distanceTo(p) < radius) points.add(p);
                }
            }
        }

        if (hollow) points.removeAll(new Sphere(center, radius - 1).getPoints());

        return points;
    }

    @Override
    public void openGui(int i, JsonObject jsonObject, StructureTypeGui instance)
    {
        new SphereGui(i, jsonObject, instance, typeMap);
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
        return new Sphere(center, radius);
    }
}
