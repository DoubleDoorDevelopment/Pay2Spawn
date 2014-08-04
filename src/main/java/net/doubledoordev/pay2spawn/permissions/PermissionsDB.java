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

import net.doubledoordev.pay2spawn.Pay2Spawn;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.util.Constants.GSON;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Permission system stuff
 *
 * @author Dries007
 */
public class PermissionsDB
{
    private HashMap<String, Player> playerDB = new HashMap<>();
    private HashMap<String, Group>  groupDB  = new HashMap<>();

    public void save()
    {
        try
        {
            File file = getFile();
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            JsonObject rootObject = new JsonObject();

            JsonArray players = new JsonArray();
            for (Player player : playerDB.values()) players.add(player.toJson());
            rootObject.add("players", players);

            JsonArray groups = new JsonArray();
            for (Group group : groupDB.values()) groups.add(group.toJson());
            rootObject.add("groups", groups);

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(GSON.toJson(rootObject));
            bw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void load() throws IOException
    {
        File file = getFile();
        if (file.exists())
        {
            JsonObject rootObject = JSON_PARSER.parse(new FileReader(file)).getAsJsonObject();

            for (JsonElement element : rootObject.getAsJsonArray("players"))
            {
                Player player = new Player(element.getAsJsonObject());
                playerDB.put(player.getName(), player);
            }

            for (JsonElement element : rootObject.getAsJsonArray("groups"))
            {
                Group group = new Group(element.getAsJsonObject());
                groupDB.put(group.getName(), group);
            }
        }
        else
        {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            JsonObject rootObject = new JsonObject();
            rootObject.add("players", new JsonArray());
            rootObject.add("groups", new JsonArray());
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(GSON.toJson(rootObject));
            bw.close();
        }
    }

    private File getFile()
    {
        return new File(Pay2Spawn.getFolder(), "Permissions.json");
    }

    public boolean check(String entityName, Node node)
    {
        if (!playerDB.containsKey(entityName)) return false;
        Player player = playerDB.get(entityName);
        if (player.hasSpecificPermissionFor(node)) return true;
        for (String groupName : player.getGroups())
        {
            if (checkGroup(groupName, node)) return true;
        }
        return false;
    }

    public boolean checkGroup(String groupName, Node node)
    {
        return groupDB.containsKey(groupName) && groupDB.get(groupName).hasPermissionFor(node);
    }

    public void newGroup(String name, String parent)
    {
        groupDB.put(name, new Group(name, parent));
    }

    public void remove(String name)
    {
        groupDB.remove(name);
    }

    public Group getGroup(String name)
    {
        return groupDB.get(name);
    }

    public Player getPlayer(String name)
    {
        if (!playerDB.containsKey(name)) newPlayer(name);
        return playerDB.get(name);
    }

    public Iterable getGroups()
    {
        return groupDB.keySet();
    }

    public Iterable getPlayers()
    {
        return playerDB.keySet();
    }

    public void newPlayer(String name)
    {
        playerDB.put(name, new Player(name));
    }
}
