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

package net.doubledoordev.pay2spawn.types;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.network.MusicMessage;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.doubledoordev.pay2spawn.types.guis.MusicTypeGui;
import net.doubledoordev.pay2spawn.util.Constants;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static net.doubledoordev.pay2spawn.util.Constants.NBTTypes;
import static net.doubledoordev.pay2spawn.util.Constants.STRING;

/**
 * @author Dries007
 */
public class MusicType extends TypeBase
{
    public static final  String                  SOUND_KEY = "song";
    public static final  HashMap<String, String> typeMap   = new HashMap<>();
    private static final String                  NAME      = "music";
    public static File musicFolder;

    static
    {
        typeMap.put(SOUND_KEY, NBTTypes[STRING]);
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public NBTTagCompound getExample()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(SOUND_KEY, "Rickroll.mp3");
        return nbt;
    }

    @Override
    public void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData)
    {
        Pay2Spawn.getSnw().sendTo(new MusicMessage(dataFromClient.getString(SOUND_KEY)), (EntityPlayerMP) player);
    }

    @Override
    public void openNewGui(int rewardID, JsonObject data)
    {
        new MusicTypeGui(rewardID, NAME, data, typeMap);
    }

    @Override
    public Collection<Node> getPermissionNodes()
    {
        return new HashSet<>();
    }

    @Override
    public Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient)
    {
        return new Node(NAME, dataFromClient.getString(SOUND_KEY).split(" ")[0]);
    }

    @Override
    public String replaceInTemplate(String id, JsonObject jsonObject)
    {
        switch (id)
        {
            case "song":
                return jsonObject.get(SOUND_KEY).getAsString().replace(typeMap.get(SOUND_KEY) + ":", "");
        }
        return id;
    }

    @Override
    public void printHelpList(File configFolder)
    {
        musicFolder = new File(configFolder, "music");
        if (musicFolder.mkdirs())
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        File zip = new File(musicFolder, "music.zip");
                        FileUtils.copyURLToFile(new URL(Constants.MUSICURL), zip);
                        ZipFile zipFile = new ZipFile(zip);
                        Enumeration<? extends ZipEntry> entries = zipFile.entries();
                        while (entries.hasMoreElements())
                        {
                            ZipEntry entry = entries.nextElement();
                            File entryDestination = new File(musicFolder, entry.getName());
                            entryDestination.getParentFile().mkdirs();
                            InputStream in = zipFile.getInputStream(entry);
                            OutputStream out = new FileOutputStream(entryDestination);
                            IOUtils.copy(in, out);
                            IOUtils.closeQuietly(in);
                            IOUtils.closeQuietly(out);
                        }
                        zipFile.close();
                        zip.delete();
                    }
                    catch (IOException e)
                    {
                        Pay2Spawn.getLogger().warn("Error downloading music file. Get from github and unpack yourself please.");
                        e.printStackTrace();
                    }
                }
            }, "Pay2Spawn music download and unzip").start();
        }
    }
}
