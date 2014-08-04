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

package net.doubledoordev.pay2spawn.configurator;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.util.Helper;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.util.EnumChatFormatting;

/**
 * Manages instances of the NBTgrabber and the configurator
 *
 * @author Dries007
 */
public class ConfiguratorManager
{
    public static void openNbt()
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            try
            {
                new NBTGrabber();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Pay2Spawn.getLogger().warn("WTF? Can't open the NBT Grabber on the server. How did this happen?");
        }
    }

    public static void openCfg()
    {
        if (!Pay2Spawn.getRewardsDB().editable) Helper.msg(EnumChatFormatting.GOLD + "[P2S] You can't edit a server side config.");
        else
        {
            if (FMLCommonHandler.instance().getEffectiveSide().isClient())
            {
                try
                {
                    Configurator.show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Pay2Spawn.getLogger().warn("WTF? Can't open the Configurator on the server. How did this happen?");
            }
        }
    }

    public static void reload()
    {
        if (Configurator.instance != null) Configurator.instance.update();
    }

    public static void exit()
    {
        if (Configurator.instance != null) Configurator.instance.close();
    }
}
