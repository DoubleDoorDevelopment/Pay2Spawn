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

package net.doubledoordev.pay2spawn.permissions;

import net.doubledoordev.pay2spawn.Pay2Spawn;
import com.google.common.base.Strings;

import java.io.*;
import java.util.HashSet;

/**
 * Permission system stuff
 *
 * @author Dries007
 */
public class BanList
{
    public static final String[] BAD_CMD = {"stop", "op", "deop", "ban", "ban-ip", "pardon", "pardon-ip", "save-on", "save-off", "save-all"};
    HashSet<Node> nodes = new HashSet<>();

    public void save()
    {
        try
        {
            File file = getFile();
            if (!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

            PrintWriter pw = new PrintWriter(file);

            pw.println("## Any and all nodes in this list are globally banned.");
            pw.println("## 1 node per line.");
            pw.println("## Nodes can end in .* to indicate a wildcard.");

            for (Node node : nodes) pw.println(node.toString());

            pw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void load() throws IOException
    {
        nodes.clear();
        File file = getFile();
        if (file.exists())
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            for (String line; (line = br.readLine()) != null; )
            {
                line = line.trim();
                if (!Strings.isNullOrEmpty(line) && !line.startsWith("#")) nodes.add(new Node(line));
            }
        }
        else
        {
            try
            {
                file.createNewFile();

                PrintWriter pw = new PrintWriter(file);

                pw.println("## Any and all nodes in this list are globally banned.");
                pw.println("## 1 node per line.");
                pw.println("## Nodes can end in .* to indicate a wildcard.");

                for (String cmd : BAD_CMD) pw.println("command." + cmd);

                pw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public File getFile()
    {
        return new File(Pay2Spawn.getFolder(), "BanList.txt");
    }

    public boolean contains(Node node)
    {
        for (Node bannedNode : nodes)
            if (bannedNode.matches(node)) return true;
        return false;
    }
}
