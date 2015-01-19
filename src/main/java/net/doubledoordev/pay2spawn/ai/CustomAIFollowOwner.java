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

package net.doubledoordev.pay2spawn.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Largely 'borrowed' from EntityAIFollowOwner
 * @author Dries007
 */
public class CustomAIFollowOwner extends EntityAIBase
{
    EntityLiving mob;
    World theWorld;
    double speed;
    PathNavigate mobPathfinder;
    int updateTimer;
    float maxDist;
    float minDist;
    boolean avoidsWater;
    Entity owner;

    public CustomAIFollowOwner(EntityLiving mob, double speed, float minDist, float maxDist)
    {
        this.mob = mob;
        this.theWorld = mob.worldObj;
        this.speed = speed;
        this.mobPathfinder = mob.getNavigator();
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        owner = CustomAI.getOwner(mob);
        return owner != null && this.mob.getDistanceSqToEntity(owner) >= (double) (this.minDist * this.minDist);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return !this.mobPathfinder.noPath() && this.mob.getDistanceSqToEntity(this.owner) > (double) (this.maxDist * this.maxDist);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.updateTimer = 0;
        this.avoidsWater = this.mob.getNavigator().getAvoidsWater();
        this.mob.getNavigator().setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.mobPathfinder.clearPathEntity();
        this.mob.getNavigator().setAvoidsWater(this.avoidsWater);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.mob.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.mob.getVerticalFaceSpeed());

        if (--this.updateTimer <= 0)
        {
            this.updateTimer = 10;

            if (!this.mobPathfinder.tryMoveToEntityLiving(this.owner, this.speed))
            {
                if (!this.mob.getLeashed())
                {
                    if (this.mob.getDistanceSqToEntity(this.owner) >= 144.0D)
                    {
                        int i = MathHelper.floor_double(this.owner.posX) - 2;
                        int j = MathHelper.floor_double(this.owner.posZ) - 2;
                        int k = MathHelper.floor_double(this.owner.boundingBox.minY);

                        for (int l = 0; l <= 4; ++l)
                        {
                            for (int i1 = 0; i1 <= 4; ++i1)
                            {
                                if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.doesBlockHaveSolidTopSurface(this.theWorld, i + l, k - 1, j + i1) && !this.theWorld.getBlock(i + l, k, j + i1).isNormalCube() && !this.theWorld.getBlock(i + l, k + 1, j + i1).isNormalCube())
                                {
                                    this.mob.setLocationAndAngles((double) ((float) (i + l) + 0.5F), (double) k, (double) ((float) (j + i1) + 0.5F), this.mob.rotationYaw, this.mob.rotationPitch);
                                    this.mobPathfinder.clearPathEntity();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
