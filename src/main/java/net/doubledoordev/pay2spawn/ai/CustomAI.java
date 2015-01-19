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

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.pay2spawn.util.Constants;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import static net.doubledoordev.pay2spawn.util.Constants.MODID;

/**
 * @author Dries007
 */
public class CustomAI
{
    public static final String CUSTOM_AI_TAG = MODID + "_CustomAI";
    public static final String OWNER_TAG = MODID + "_Owner";
    public static final CustomAI INSTANCE = new CustomAI();

    public CustomAI()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void respawnHandler(EntityJoinWorldEvent event)
    {
        if (event.entity instanceof EntityCreature && event.entity.getEntityData().hasKey(CUSTOM_AI_TAG, Constants.COMPOUND))
        {
            EntityCreature entity = (EntityCreature) event.entity;
            for (Object o : ImmutableList.copyOf(entity.tasks.taskEntries)) entity.tasks.removeTask(((EntityAITasks.EntityAITaskEntry) o).action);
            for (Object o : ImmutableList.copyOf(entity.targetTasks.taskEntries)) entity.targetTasks.removeTask(((EntityAITasks.EntityAITaskEntry) o).action);

            entity.tasks.addTask(0, new EntityAISwimming(entity));
            entity.tasks.addTask(1, new CustomAIAttackOnCollide(entity, 1.0d, false));
            entity.tasks.addTask(2, new CustomAIFollowOwner(entity, 1.5d, 6.0f, 10.0f));
            entity.tasks.addTask(3, new EntityAIWander(entity, 1.0D));
            entity.tasks.addTask(4, new EntityAIWatchClosest(entity, EntityPlayer.class, 8.0F));
            entity.tasks.addTask(5, new EntityAILookIdle(entity));
        }
    }

    public void test(final EntityPlayer player)
    {
        final EntityZombie zombie = new EntityZombie(player.getEntityWorld());
        zombie.setCanPickUpLoot(true);
        zombie.setPosition(player.posX, player.posY, player.posZ);

        setOwner(zombie, player.getCommandSenderName());
        zombie.setCustomNameTag("dries007");
/*
        zombie.targetTasks.addTask(1, new EntityAITarget (zombie, false)
        {
            EntityPlayer theDefendingTameable = player;
            EntityLivingBase theOwnerAttacker;
            private int field_142051_e;
            private static final String __OBFID = "CL_00001624";

            {
                this.setMutexBits(1);
            }

            public boolean shouldExecute()
            {
                EntityLivingBase entitylivingbase = this.theDefendingTameable;

                if (entitylivingbase == null)
                {
                    return false;
                }
                else
                {
                    this.theOwnerAttacker = entitylivingbase.getLastAttacker();
                    int i = entitylivingbase.func_142015_aE();
                    return i != this.field_142051_e && this.isSuitableTarget(this.theOwnerAttacker, false);
                }
            }

            public void startExecuting()
            {
                this.taskOwner.setAttackTarget(this.theOwnerAttacker);
                EntityLivingBase entitylivingbase = this.theDefendingTameable;

                if (entitylivingbase != null)
                {
                    this.field_142051_e = entitylivingbase.func_142015_aE();
                }

                super.startExecuting();
            }
        });
        //zombie.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(zombie));
        zombie.targetTasks.addTask(3, new EntityAIHurtByTarget(zombie, true));
        */

        player.getEntityWorld().spawnEntityInWorld(zombie);
    }

    public void init()
    {

    }

    public static Entity getOwner(EntityLiving mob)
    {
        return mob.worldObj.getPlayerEntityByName(mob.getEntityData().getCompoundTag(CUSTOM_AI_TAG).getString(OWNER_TAG));
    }

    public static void setOwner(EntityLiving mob, String owner)
    {
        NBTTagCompound compound = mob.getEntityData().getCompoundTag(CUSTOM_AI_TAG);
        compound.setString(OWNER_TAG, owner);
        mob.getEntityData().setTag(CUSTOM_AI_TAG, compound);
    }
}
