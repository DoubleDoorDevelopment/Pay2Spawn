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

import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.types.guis.PotionEffectTypeGui;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Applies potion effect
 *
 * @author Dries007
 */
public class PotionEffectType extends TypeBase
{
    public static final String NODENAME      = "potioneffect";
    public static final String ID_KEY        = "Id";
    public static final String AMPLIFIER_KEY = "Amplifier";
    public static final String DURATION_KEY  = "Duration";

    public static final HashBiMap<String, Integer> POTIONS = HashBiMap.create();
    public static final HashMap<String, String>    typeMap = new HashMap<>();

    static
    {
        typeMap.put(ID_KEY, NBTTypes[BYTE]);
        typeMap.put(AMPLIFIER_KEY, NBTTypes[BYTE]);
        typeMap.put(DURATION_KEY, NBTTypes[INT]);
    }

    @Override
    public String getName()
    {
        return NODENAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        Potion potion = null;
        while (potion == null) potion = Potion.potionTypes[RANDOM.nextInt(Potion.potionTypes.length)];
        return new PotionEffect(potion.getId(), (int) (RANDOM.nextDouble() * 1000)).writeCustomPotionEffectToNBT(new NBTTagCompound());
    }

    @Override
    public void printHelpList(File configFolder)
    {
        File file = new File(configFolder, "Potion.txt");
        try
        {
            if (file.exists()) file.delete();
            file.createNewFile();
            PrintWriter pw = new PrintWriter(file);

            pw.println("Potion list file");

            ArrayList<String> ids = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();

            for (Potion potion : Potion.potionTypes)
            {
                if (potion != null)
                {
                    POTIONS.put(potion.getId() + ": " + potion.getName(), potion.getId());
                    ids.add(potion.getId() + "");
                    names.add(StatCollector.translateToLocal(potion.getName()));
                }
            }
            pw.print(Helper.makeTable(new Helper.TableData("ID", ids), new Helper.TableData("name", names)));

            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        player.addPotionEffect(PotionEffect.readCustomPotionEffectFromNBT(dataFromClient));
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new PotionEffectTypeGui(rewardID, getName(), data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        HashSet<Node> nodes = new HashSet<>();

        for (Potion potion : Potion.potionTypes)
        {
            if (potion != null)
            {
                String name = potion.getName();
                if (name.startsWith("potion.")) name = name.substring("potion.".length());
                nodes.add(new Node(NODENAME, name.replace(".", "_")));
            }
        }
        return nodes;
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(dataFromClient);
        String name = effect.getEffectName();
        if (name.startsWith("potion.")) name = name.substring("potion.".length());
        return new Node(NODENAME, name.replace(".", "_"));
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "effect":
                PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(JsonNBTHelper.parseJSON(jsonObject));
                return effect.getEffectName() + " " + (effect.getAmplifier() + 1);
            case "duration":
                return jsonObject.get(DURATION_KEY).getAsString().replace(typeMap.get(DURATION_KEY), "");
        }
        return id;
    }
}
