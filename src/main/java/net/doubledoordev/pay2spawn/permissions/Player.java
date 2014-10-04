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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashSet;

/**
 * Permission system stuff
 *
 * @author Dries007
 */
public class Player
{
    private HashSet<String> groups        = new HashSet<>();
    private HashSet<Node>   overrideNodes = new HashSet<>();
    private String name;

    public Player(JsonObject jsonObject)
    {
        name = jsonObject.get("name").getAsString();
        if (jsonObject.has("groups")) for (JsonElement groupName : jsonObject.getAsJsonArray("groups")) groups.add(groupName.getAsString());
        if (jsonObject.has("overrideNodes")) for (JsonElement node : jsonObject.getAsJsonArray("overrideNodes")) overrideNodes.add(new Node(node.getAsString()));
    }

    public Player(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public JsonElement toJson()
    {
        JsonObject root = new JsonObject();
        root.addProperty("name", getName());
        JsonArray groups = new JsonArray();
        for (String group : this.getGroups()) groups.add(new JsonPrimitive(group));
        root.add("groups", groups);

        JsonArray nodes = new JsonArray();
        for (Node node : this.overrideNodes) nodes.add(new JsonPrimitive(node.toString()));
        root.add("overrides", nodes);

        return root;
    }

    public boolean hasSpecificPermissionFor(Node requestNode)
    {
        for (Node hadNode : overrideNodes) if (hadNode.matches(requestNode)) return true;
        return false;
    }

    public Iterable<? extends String> getGroups()
    {
        return groups;
    }

    public boolean removeGroup(String group)
    {
        return groups.remove(group);
    }

    public void addGroup(String groupName)
    {
        groups.add(groupName);
    }

    @Override
    public int hashCode()
    {
        int result = groups.hashCode();
        result = 31 * result + overrideNodes.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return groups.equals(player.groups) && name.equals(player.name) && overrideNodes.equals(player.overrideNodes);

    }

    public void addNode(Node node)
    {
        overrideNodes.add(node);
    }

    public boolean removeNode(Node node)
    {
        return overrideNodes.remove(node);
    }

    public HashSet<String> getNodes()
    {
        HashSet<String> strings = new HashSet<>();
        for (Node node : overrideNodes) strings.add(node.toString());
        return strings;
    }
}
