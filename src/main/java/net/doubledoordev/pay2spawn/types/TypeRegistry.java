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

import net.doubledoordev.pay2spawn.configurator.HTMLGenerator;
import net.doubledoordev.pay2spawn.permissions.PermissionsHandler;
import net.minecraftforge.common.config.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Stores all the type instances
 *
 * @author Dries007
 */
public class TypeRegistry
{
    private static HashMap<String, TypeBase> map = new HashMap<>();

    /**
     * Register your type here, in pre-preInit!
     *
     * @param typeBase the instance you want to register
     *
     * @throws IllegalArgumentException if the name has been taken
     */
    public static void register(TypeBase typeBase)
    {
        if (map.put(typeBase.getName().toLowerCase(), typeBase) != null) throw new IllegalArgumentException("Duplicate type!");
    }

    /**
     * Get a type by its name
     *
     * @param name the name
     *
     * @return null if no such type, otherwise the instance given to us
     */
    public static TypeBase getByName(String name)
    {
        return map.get(name.toLowerCase());
    }

    public static ArrayList<String> getNames()
    {
        return new ArrayList<>(map.keySet());
    }

    /**
     * Internal method
     *
     * @param configuration passed on to types
     */
    public static void doConfig(Configuration configuration)
    {
        for (TypeBase type : map.values())
        {
            type.doConfig(configuration);
        }
    }

    public static void registerPermissions()
    {
        for (TypeBase type : map.values())
        {
            PermissionsHandler.register(type.getPermissionNodes());
        }
    }

    public static void copyTemplates() throws IOException
    {
        for (TypeBase type : map.values())
        {
            type.copyTemplateFile(HTMLGenerator.templateFolder);
        }
    }

    /**
     * @return a Collection of all the registered types
     */
    public static Collection<TypeBase> getAllTypes()
    {
        return map.values();
    }

    /**
     * Used by metrics
     *
     * @return the amount of types
     */
    public static int getAmount()
    {
        return map.size();
    }

    /**
     * Called Pre-Init
     */
    public static void preInit()
    {
        TypeRegistry.register(new XPOrbsType());
        TypeRegistry.register(new EntityType());
        TypeRegistry.register(new ItemType());
        TypeRegistry.register(new PotionEffectType());
        TypeRegistry.register(new LightningType());
        //TypeRegistry.register(new SoundType());
        TypeRegistry.register(new FireworksType());
        TypeRegistry.register(new CustomEntityType());
        TypeRegistry.register(new RandomItemType());
        TypeRegistry.register(new DropItemType());
        TypeRegistry.register(new CommandType());
        TypeRegistry.register(new PlayerModificationType());
        TypeRegistry.register(new MusicType());
        TypeRegistry.register(new DeleteworldType());
        TypeRegistry.register(new StructureType());
    }
}
