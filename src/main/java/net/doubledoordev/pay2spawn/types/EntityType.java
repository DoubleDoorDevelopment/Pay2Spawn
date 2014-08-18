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
import net.doubledoordev.pay2spawn.types.guis.EntityTypeGui;
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
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * A simple entity spawner, can handle:
 * - agroing mobs
 * - custom name tags
 * - entities riding entities to infinity
 *
 * @author Dries007
 */
public class EntityType extends TypeBase
{
    public static final  String                  ENTITYNAME_KEY         = "name";
    public static final  String                  SPAWNRADIUS_KEY        = "SPAWNRADIUS";
    public static final  String                  AMOUNT_KEY             = "AMOUNT";
    public static final  String                  AGRO_KEY               = "agro";
    public static final  String                  CUSTOMNAME_KEY         = "CustomName";
    public static final  String                  RIDING_KEY             = "Riding";
    public static final  String                  RIDETHISMOB_KEY        = "RideThisMob";
    public static final  String                  RANDOM_KEY             = "random";
    public static final  String                  THROWTOWARDSPLAYER_KEY = "throwTowardsPlayer";
    public static final  HashSet<String>         NAMES                  = new HashSet<>();
    public static final  HashMap<String, String> typeMap                = new HashMap<>();
    private static final String                  NAME                   = "entity";
    public static final  String                  NODENAME               = NAME;

    private static int spawnLimit = 100;

    static
    {
        typeMap.put(ENTITYNAME_KEY, NBTTypes[STRING]);
        typeMap.put(SPAWNRADIUS_KEY, NBTTypes[INT]);
        typeMap.put(AMOUNT_KEY, NBTTypes[INT]);
        typeMap.put(AGRO_KEY, NBTTypes[BYTE]);
        typeMap.put(CUSTOMNAME_KEY, NBTTypes[STRING]);
        typeMap.put(RIDETHISMOB_KEY, NBTTypes[BYTE]);
        typeMap.put(RANDOM_KEY, NBTTypes[BYTE]);
        typeMap.put(THROWTOWARDSPLAYER_KEY, NBTTypes[BYTE]);
    }

    public static int getSpawnLimit()
    {
        return spawnLimit;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void doConfig(Configuration configuration)
    {
        configuration.addCustomCategoryComment(TYPES_CAT, "Reward config options");
        configuration.addCustomCategoryComment(TYPES_CAT + '.' + NAME, "Used for Entity and CustomEntity");
        spawnLimit = configuration.get(TYPES_CAT + '.' + NAME, "spawnLimit", spawnLimit, "A hard entity spawn limit. Only counts 1 reward's mobs. -1 for no limit.").getInt(spawnLimit);
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(ENTITYNAME_KEY, "$randomEntity");
        tag.setBoolean(AGRO_KEY, true);
        tag.setBoolean(RANDOM_KEY, true);
        tag.setString(CUSTOMNAME_KEY, "$name");

        NBTTagCompound tag2 = new NBTTagCompound();
        tag2.setString(ENTITYNAME_KEY, "$randomEntity");
        tag2.setBoolean(AGRO_KEY, true);
        tag2.setString(CUSTOMNAME_KEY, "$name");
        tag2.setBoolean(RIDETHISMOB_KEY, true);

        tag.setTag(RIDING_KEY, tag2);
        tag.setInteger(SPAWNRADIUS_KEY, 10);
        tag.setInteger(AMOUNT_KEY, 2);

        return tag;
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new EntityTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();
        for (String s : EntityType.NAMES) nodes.add(new Node(NODENAME, s));
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NODENAME, dataFromClient.getString(ENTITYNAME_KEY));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "entity":
                StringBuilder sb = new StringBuilder();
                sb.append(jsonObject.get(ENTITYNAME_KEY).getAsString().replace("STRING:", ""));
                while (jsonObject.has(RIDING_KEY))
                {
                    jsonObject = jsonObject.getAsJsonObject(RIDING_KEY);
                    sb.append(" riding a ").append(jsonObject.get(ENTITYNAME_KEY).getAsString().replace("STRING:", ""));
                }
                return sb.toString();
        }
        return id;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        if (!dataFromClient.hasKey(SPAWNRADIUS_KEY)) dataFromClient.setInteger(SPAWNRADIUS_KEY, 10);
        ArrayList<PointD> pointDs = new PointD(player).makeNiceForBlock().getCylinder(dataFromClient.getInteger(SPAWNRADIUS_KEY), 6);
        NBTTagCompound p2sTag = new NBTTagCompound();
        p2sTag.setString("Type", getName());
        if (rewardData.hasKey("name")) p2sTag.setString("Reward", rewardData.getString("name"));

        int count = 0;
        if (!dataFromClient.hasKey(AMOUNT_KEY)) dataFromClient.setInteger(AMOUNT_KEY, 1);
        for (int i = 0; i < dataFromClient.getInteger(AMOUNT_KEY); i++)
        {
            Entity entity = EntityList.createEntityByName(dataFromClient.getString(ENTITYNAME_KEY), player.getEntityWorld());

            if (entity != null)
            {
                count++;
                if (getSpawnLimit() != -1 && count > getSpawnLimit()) break;
                entity.setPosition(player.posX, player.posY, player.posZ);
                Helper.rndSpawnPoint(pointDs, entity);

                if (dataFromClient.getBoolean(AGRO_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setAttackTarget(player);
                if (dataFromClient.hasKey(CUSTOMNAME_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).setCustomNameTag(dataFromClient.getString(CUSTOMNAME_KEY));
                if (dataFromClient.getBoolean(RANDOM_KEY) && entity instanceof EntityLiving) ((EntityLiving) entity).onSpawnWithEgg(null);

                entity.getEntityData().setTag(Constants.NAME, p2sTag.copy());
                player.getEntityWorld().spawnEntityInWorld(entity);

                Entity entity1 = entity;
                for (NBTTagCompound tag = dataFromClient; tag.hasKey(RIDING_KEY); tag = tag.getCompoundTag(RIDING_KEY))
                {
                    Entity entity2 = EntityList.createEntityByName(tag.getCompoundTag(RIDING_KEY).getString(ENTITYNAME_KEY), player.getEntityWorld());

                    Node node = this.getPermissionNode(player, tag.getCompoundTag(EntityType.RIDING_KEY));
                    if (BanHelper.isBanned(node))
                    {
                        Helper.sendChatToPlayer(player, "This node (" + node + ") is banned.", EnumChatFormatting.RED);
                        Pay2Spawn.getLogger().warn(player.getCommandSenderName() + " tried using globally banned node " + node + ".");
                        continue;
                    }
                    if (PermissionsHandler.needPermCheck((EntityPlayerMP) player) && !PermissionsHandler.hasPermissionNode(player, node))
                    {
                        Pay2Spawn.getLogger().warn(player.getDisplayName() + " doesn't have perm node " + node.toString());
                        continue;
                    }

                    if (entity2 != null)
                    {
                        count++;
                        if (getSpawnLimit() != -1 && count > getSpawnLimit()) break;

                        if (tag.getCompoundTag(RIDING_KEY).getBoolean(AGRO_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setAttackTarget(player);
                        if (tag.getCompoundTag(RIDING_KEY).hasKey(CUSTOMNAME_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).setCustomNameTag(tag.getCompoundTag(RIDING_KEY).getString(CUSTOMNAME_KEY));
                        if (tag.getCompoundTag(RIDING_KEY).getBoolean(RANDOM_KEY) && entity2 instanceof EntityLiving) ((EntityLiving) entity2).onSpawnWithEgg(null);

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
                    new Vector3(entity, player).normalize().setAsVelocity(entity, 2);
                }
            }
        }
    }

    @Override
    public void printHelpList(File configFolder)
    {
        File file = new File(configFolder, "EntityList.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("This is a list of all the entities you can use in the json file.");

            ArrayList<String> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();

            ArrayList<Integer> list = new ArrayList<Integer>(EntityList.IDtoClassMapping.size());
            for (Object key : EntityList.IDtoClassMapping.keySet())
            {
                list.add((Integer) key);
            }
            Collections.sort(list);
            for (Object key : list)
            {
                Integer id = (Integer) key;
                String name = EntityList.getStringFromID(id);

                NAMES.add(name);
                ids.add(id + "");
                names.add(name);
            }
            pw.print(Helper.makeTable(new Helper.TableData("ID", ids), new Helper.TableData("name", names)));

            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
