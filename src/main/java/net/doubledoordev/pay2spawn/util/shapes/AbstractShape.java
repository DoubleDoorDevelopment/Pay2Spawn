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

import net.doubledoordev.pay2spawn.util.Helper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.util.Constants.BYTE;
import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;

/**
 * For structure spawning things
 *
 * @author Dries007
 */
public abstract class AbstractShape implements IShape
{
    public static final HashMap<String, String> typeMap             = new HashMap<>();
    public static final String                  CENTER_KEY          = "center";
    public static final String                  HOLLOW_KEY          = "hollow";
    public static final String                  REPLACEABLEONLY_KEY = "replaceableOnly";

    static
    {
        typeMap.put(HOLLOW_KEY, NBTTypes[BYTE]);
        typeMap.put(REPLACEABLEONLY_KEY, NBTTypes[BYTE]);
    }

    public static final long RENDERTIMEOUT = 1000;
    protected long tempPointsTime = 0L;
    protected Collection<PointI> temppoints;
    PointI center = new PointI();
    boolean hollow, replaceableOnly;

    public AbstractShape(PointI center)
    {
        this.center = center;
    }

    public AbstractShape()
    {

    }

    @Override
    public NBTTagCompound toNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(CENTER_KEY, center.toNBT());
        compound.setBoolean(HOLLOW_KEY, hollow);
        compound.setBoolean(REPLACEABLEONLY_KEY, replaceableOnly);
        return compound;
    }

    @Override
    public IShape fromNBT(NBTTagCompound compound)
    {
        center.fromNBT(compound.getCompoundTag(CENTER_KEY));
        hollow = compound.getBoolean(HOLLOW_KEY);
        replaceableOnly = compound.getBoolean(REPLACEABLEONLY_KEY);
        return this;
    }

    @Override
    public IShape move(int x, int y, int z)
    {
        center.move(x, y, z);
        return this;
    }

    @Override
    public PointI getCenter()
    {
        return center;
    }

    @Override
    public IShape setCenter(PointI pointI)
    {
        center = pointI;
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
    public void render(Tessellator tess)
    {
        if (temppoints == null || System.currentTimeMillis() - tempPointsTime > RENDERTIMEOUT)
        {
            temppoints = getPoints();
            tempPointsTime = System.currentTimeMillis();
        }
        for (PointI pointI : temppoints)
        {
            Helper.renderPoint(pointI, tess);
        }
    }

    @Override
    public IShape rotate(int baseRotation)
    {
        center.rotate(baseRotation);
        return this;
    }
}
