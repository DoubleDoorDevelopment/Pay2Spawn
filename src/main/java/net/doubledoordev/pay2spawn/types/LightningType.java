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
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.doubledoordev.pay2spawn.types.guis.LightningTypeGui;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Strikes the world within 1 block of the player
 * (randomness to avoid always striking a full 6 heart hit)
 * <p/>
 * No extra data
 *
 * @author Dries007
 */
public class LightningType extends TypeBase
{
    public static final String NODENAME   = "lightning";
    public static final String SPREAD_KEY = "spread";
    public static final String TYPE_KEY   = "type";

    public static final int PLAYER_ENTITY  = 0;
    public static final int NEAREST_ENTITY = 1;
    public static final int RND_ENTITY     = 2;
    public static final int RND_SPOT       = 3;


    public static final HashMap<String, String> typeMap = new HashMap<>();

    static
    {
        typeMap.put(SPREAD_KEY, NBTTypes[INT]);
        typeMap.put(TYPE_KEY, NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(SPREAD_KEY, 10);
        nbt.setInteger(TYPE_KEY, RND_ENTITY);
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        if (!dataFromClient.hasKey(SPREAD_KEY)) dataFromClient.setInteger(SPREAD_KEY, 10);
        double spread = dataFromClient.getInteger(SPREAD_KEY);
        double X = player.posX, Y = player.posY - 1, Z = player.posZ;
        if (!dataFromClient.hasKey(TYPE_KEY)) dataFromClient.setInteger(TYPE_KEY, RND_SPOT);

        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case PLAYER_ENTITY:
            {
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case NEAREST_ENTITY:
            {
                AxisAlignedBB AABB = AxisAlignedBB.getBoundingBox(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                Entity entity = player.getEntityWorld().findNearestEntityWithinAABB(EntityLiving.class, AABB, player);
                if (entity != null) player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), entity.posX, entity.posY, entity.posZ));
                else player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case RND_SPOT:
            {
                X += (spread - (RANDOM.nextDouble() * spread * 2));
                Z += (spread - (RANDOM.nextDouble() * spread * 2));
                Y += (3 - RANDOM.nextDouble() * 6);
                player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
                break;
            }
            case RND_ENTITY:
            {
                IEntitySelector iEntitySelector = new IEntitySelector()
                {
                    @Override
                    public boolean isEntityApplicable(Entity entity)
                    {
                        return entity instanceof EntityLiving;
                    }
                };
                AxisAlignedBB AABB = AxisAlignedBB.getBoundingBox(X - spread, Y - spread, Z - spread, X + spread, Y + spread, Z + spread);
                //noinspection unchecked
                List<EntityLiving> entity = player.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player, AABB, iEntitySelector);
                EntityLiving entityLiving = RandomRegistry.getRandomFromSet(entity);
                if (entityLiving != null) player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), entityLiving.posX, entityLiving.posY, entityLiving.posZ));
                else player.getEntityWorld().addWeatherEffect(new EntityLightningBolt(player.getEntityWorld(), X, Y, Z));
            }
        }
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new LightningTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(new Node(NODENAME, "player"));
        nodes.add(new Node(NODENAME, "nearest"));
        nodes.add(new Node(NODENAME, "rnd_entity"));
        nodes.add(new Node(NODENAME, "rnd_spot"));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        if (!dataFromClient.hasKey(TYPE_KEY)) dataFromClient.setInteger(TYPE_KEY, RND_SPOT);
        switch (dataFromClient.getInteger(TYPE_KEY))
        {
            case PLAYER_ENTITY:
                return new Node(NODENAME, "player");
            case NEAREST_ENTITY:
                return new Node(NODENAME, "nearest");
            case RND_SPOT:
                return new Node(NODENAME, "rnd_entity");
            case RND_ENTITY:
                return new Node(NODENAME, "rnd_spot");
            default:
                return new Node(NODENAME, "player");
        }
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "target":
                switch (Integer.parseInt(jsonObject.get(TYPE_KEY).getAsString().split(":", 2)[1]))
                {
                    case PLAYER_ENTITY:
                        return "the streamer";
                    case NEAREST_ENTITY:
                        return "the nearest entity";
                    case RND_SPOT:
                        return "a random near spot";
                    case RND_ENTITY:
                        return "a random near entity";
                }
        }
        return id;
    }
}
