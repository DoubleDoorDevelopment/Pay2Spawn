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

package net.doubledoordev.pay2spawn.permissions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Permission system stuff
 *
 * @author Dries007
 */
public class PermissionsHandler
{
    private static PermissionsDB   permissionsDB = new PermissionsDB();
    private static HashSet<String> nodes         = new HashSet<>();

    public static boolean hasPermissionNode(EntityPlayer player, Node node)
    {
        return permissionsDB.check(player.getCommandSenderName(), node);
    }

    public static void init() throws IOException
    {
        permissionsDB.load();
        BanHelper.init();
    }

    public static void register(Collection<Node> nodesToAdd)
    {
        for (Node node : nodesToAdd)
            nodes.add(node.toString());
    }

    public static boolean needPermCheck(EntityPlayerMP player)
    {
        MinecraftServer mcs = MinecraftServer.getServer();
        return !(mcs.isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()));
    }

    public static PermissionsDB getDB()
    {
        return permissionsDB;
    }

    public static Iterable<String> getAllPermNodes()
    {
        return nodes;
    }
}
