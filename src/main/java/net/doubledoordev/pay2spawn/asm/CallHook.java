/*
 * Copyright (c) 2016 Dries007 & DoubleDoorDevelopment
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the
 * disclaimer below) provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of Pay2Spawn nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
 * GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.doubledoordev.pay2spawn.asm;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.commons.io.FileUtils;

import javax.script.ScriptEngineManager;
import java.io.File;
import java.net.URL;
import java.util.Map;

/**
 * @author Dries007
 */
public class CallHook implements IFMLCallHook
{
    private ClassLoader cl;
    private File mcLocation;

    @Override
    public void injectData(Map<String, Object> data)
    {
        cl = ((ClassLoader) data.get("classLoader"));
        mcLocation = ((File) data.get("mcLocation"));

        Plugin.LOGGER.info("injectData: {}", data);
    }

    @Override
    public Void call() throws Exception
    {
        Plugin.LOGGER.info("CallHook doing ScriptEngine test");
        if (new ScriptEngineManager().getEngineByName("javascript") == null)
        {
            File mods = new File(mcLocation, "mods");
            mods.mkdirs();
            File target = new File(mods, "Pay2Spawn-Library-Nashorn.jar");

            if (!target.isFile())
            {
                Plugin.LOGGER.info("\n\nMissing JavaScript engine. Downloading now...\n");
                try
                {
                    FileUtils.copyURLToFile(new URL("http://doubledoordev.net/p2s/nashorn-openjdk8.jar"), target);
                }
                catch (Exception e)
                {
                    Plugin.LOGGER.fatal("\n\nDownload failed! Download and add to classpath or mods folder manually.\n\n");
                    Plugin.LOGGER.catching(e);
                    throw e;
                }
            }
        }
        return null;
    }
}
