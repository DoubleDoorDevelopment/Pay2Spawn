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
import net.doubledoordev.pay2spawn.configurator.HTMLGenerator;
import net.doubledoordev.pay2spawn.permissions.Node;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for reward types
 *
 * @author Dries007
 */
public abstract class TypeBase
{
    public static final Pattern VAR = Pattern.compile("\\$\\{([\\w.]*?)\\}");

    /**
     * Used in JSON file
     *
     * @return the name in lover case only please.
     */
    public abstract String getName();

    /**
     * May or may not be random
     *
     * @return an example, NBT so it can be stored in the JSON
     */
    public abstract NBTTagCompound getExample();

    /**
     * Spawn the reward, only called server side.
     *
     * @param player         The player the reward comes from
     * @param dataFromClient the nbt from the JSON file, fully usable
     */
    public abstract void spawnServerSide(EntityPlayerMP player, NBTTagCompound dataFromClient, NBTTagCompound rewardData);

    /**
     * Extra method for custom configuration
     * Called pre-preInit
     *
     * @param configuration The configuration you should use
     */
    public void doConfig(Configuration configuration)
    {
    }

    /**
     * Use to print out useful files (aka entity and sound lists or help files)
     * Called post-preInit
     *
     * @param configFolder Make a file in here
     */
    public void printHelpList(File configFolder)
    {
    }

    public abstract void openNewGui(int rewardID, JsonObject data);

    public abstract Collection<Node> getPermissionNodes();

    public abstract Node getPermissionNode(EntityPlayer player, NBTTagCompound dataFromClient);

    public boolean isInDefaultConfig()
    {
        return true;
    }

    public void copyTemplateFile(File destinationFolder) throws IOException
    {
        File template = new File(destinationFolder, getName() + ".html");
        if (!template.exists())
        {
            InputStream link = (getClass().getResourceAsStream("/p2sTemplates/" + getName() + ".html"));
            Files.copy(link, template.getAbsoluteFile().toPath());
        }
    }

    public String getHTML(JsonObject data) throws IOException
    {
        String text = HTMLGenerator.readFile(getTermlateFile());
        while (true)
        {
            Matcher matcher = VAR.matcher(text);
            if (!matcher.find()) break;
            try
            {
                text = text.replace(matcher.group(), replaceInTemplate(matcher.group(1), data));
            }
            catch (Exception ignored)
            {

            }
        }
        return text;
    }

    public File getTermlateFile()
    {
        return new File(HTMLGenerator.templateFolder, getName() + ".html");
    }

    public abstract String replaceInTemplate(String id, JsonObject jsonObject);
}
