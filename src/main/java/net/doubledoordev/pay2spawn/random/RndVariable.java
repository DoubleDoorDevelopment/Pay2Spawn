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

package net.doubledoordev.pay2spawn.random;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * I'm insane for doing this but oh well.
 *
 * Use case: You want the same random value in 2 or more places.
 * You need to use the full expression everywhere.
 * The first time the solver comes across the tag with a new name, it will solve the random.
 * The second (or more) time if just fills in the value from memory.
 *
 * How to use: $var(name, somerandomthing)
 *
 * Example: $var(1, random(1, 10))
 *
 * @author Dries007
 */
public class RndVariable implements IRandomResolver
{
    private static final Pattern PATTERN = Pattern.compile("\\$var\\((.*?), ?([^$]*)\\)");
    private static final HashMap<String, String> VARMAP = new HashMap<>();

    @Override
    public String getIdentifier()
    {
        return "$var(";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher matcher = PATTERN.matcher(value);
        if (matcher.find())
        {
            String var = matcher.group(1);
            if (!VARMAP.containsKey(var)) VARMAP.put(var, RandomRegistry.solveRandom(type, "$" + matcher.group(2)));
            return matcher.replaceFirst(VARMAP.get(var));
        }

        return value;
    }

    @Override
    public boolean matches(int type, String value)
    {
        return PATTERN.matcher(value).find();
    }

    public static void reset()
    {
        VARMAP.clear();
    }
}
