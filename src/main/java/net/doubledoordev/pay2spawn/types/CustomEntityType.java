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

package net.doubledoordev.pay2spawn.types;

import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.permissions.BanHelper;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.doubledoordev.pay2spawn.types.guis.CustomEntityTypeGui;
import net.doubledoordev.pay2spawn.util.Constants;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.PointD;
import net.doubledoordev.pay2spawn.util.Vector3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.types.EntityType.*;

/**
 * A reward for complex custom entities
 * (aka custom nbt based ones)
 *
 * @author Dries007
 */
public class CustomEntityType extends TypeBase
{
    private static final String NAME = "customeentity";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound tag = new NBTTagCompound();
        Entity entity = EntityList.createEntityByName("Wolf", null);
        entity.writeMountToNBT(tag);
        tag.setBoolean(AGRO_KEY, true);
        return tag;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        if (!dataFromClient.hasKey(SPAWNRADIUS_KEY)) dataFromClient.setInteger(SPAWNRADIUS_KEY, 10);
        ArrayList<PointD> pointDs = new PointD(player).getCylinder(dataFromClient.getInteger(SPAWNRADIUS_KEY), 6);
        NBTTagCompound p2sTag = new NBTTagCompound();
        p2sTag.setString("Type", getName());
        if (rewardData.hasKey("name")) p2sTag.setString("Reward", rewardData.getString("name"));

        int count = 0;
        if (!dataFromClient.hasKey(AMOUNT_KEY)) dataFromClient.setInteger(AMOUNT_KEY, 1);
        for (int i = 0; i < dataFromClient.getInteger(AMOUNT_KEY); i++)
        {
            Entity entity = EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld());

            if (entity != null)
            {
                count++;
                if (getSpawnLimit() != -1 && count > getSpawnLimit()) break;

                entity.setPosition(player.posX, player.posY, player.posZ);
                Helper.rndSpawnPoint(pointDs, entity);

                if (dataFromClient.getBoolean(AGRO_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setAttackTarget(player);

                entity.getEntityData().setTag(Constants.NAME, p2sTag.copy());
                player.worldObj.spawnEntityInWorld(entity);

                Entity entity1 = entity;
                for (NBTTagCompound tag = dataFromClient; tag.hasKey(RIDING_KEY); tag = tag.getCompoundTag(RIDING_KEY))
                {
                    Entity entity2 = EntityList.createEntityFromNBT(tag.getCompoundTag(RIDING_KEY), player.getEntityWorld());

                    Node node = this.getPermissionNode(player, tag.getCompoundTag(RIDING_KEY));
                    if (BanHelper.isBanned(node))
                    {
                        Helper.sendChatToPlayer(player, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                        Pay2Spawn.getLogger().warn(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
                        continue;
                    }
                    if (PermissionsHandler.needPermCheck(player) && !PermissionsHandler.hasPermissionNode(player, node))
                    {
                        Pay2Spawn.getLogger().warn(player.getDisplayName() + " doesn't have perm node " + node.toString());
                        continue;
                    }

                    if (entity2 != null)
                    {
                        count++;
                        if (getSpawnLimit() != -1 && count > getSpawnLimit()) break;

                        if (tag.getCompoundTag(RIDING_KEY).getBoolean(AGRO_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setAttackTarget(player);

                        entity2.setPosition(entity.posX, entity.posY, entity.posZ);
                        entity2.getEntityData().setTag(Constants.NAME, p2sTag.copy());
                        player.worldObj.spawnEntityInWorld(entity2);
                        entity1.mountEntity(entity2);
                        if (tag.getCompoundTag(RIDING_KEY).hasKey(RIDETHISMOB_KEY) && tag.getCompoundTag(RIDING_KEY).getBoolean(RIDETHISMOB_KEY)) player.mountEntity(entity2);
                    }

                    entity1 = entity2;
                }
                if (dataFromClient.hasKey(RIDETHISMOB_KEY) && dataFromClient.getBoolean(RIDETHISMOB_KEY)) player.mountEntity(entity);
                if (dataFromClient.hasKey(THROWTOWARDSPLAYER_KEY) && dataFromClient.getBoolean(THROWTOWARDSPLAYER_KEY))
                {
                    Vector3 v = new Vector3(entity, player).normalize();
                    entity.motionX = 2 * v.x;
                    entity.motionY = 2 * v.y;
                    entity.motionZ = 2 * v.z;
                }
            }
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new CustomEntityTypeGui(rewardID, getName(), data, EntityType.typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (String s : NAMES) nodes.add(new Node(NODENAME, s));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NODENAME, EntityList.getEntityString(EntityList.createEntityFromNBT(dataFromClient, player.getEntityWorld())));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "entity":
                StringBuilder sb = new StringBuilder();
                if (jsonObject.has("id")) sb.append(jsonObject.get("id").getAsString().replace("STRING:", ""));
                else sb.append("null");
                while (jsonObject.has(RIDING_KEY))
                {
                    jsonObject = jsonObject.getAsJsonObject(RIDING_KEY);
                    sb.append(" riding a ").append(jsonObject.get("id").getAsString().replace("STRING:", ""));
                }
                return sb.toString();
        }
        return id;
    }
}
