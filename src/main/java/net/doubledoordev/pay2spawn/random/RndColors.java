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

package net.doubledoordev.pay2spawn.random;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Produces a random color array
 * Expected syntax: $randomRGB(x)
 * Format: An int array with x amount of random colors
 * Works with: INT[], INT, STRING
 *
 * @author Dries007
 */
public class RndColors implements IRandomResolver
{
    private static final Pattern PATTERN = Pattern.compile("\\$randomRGB\\((\\w+)\\)");

    @Override
    public String getIdentifier()
    {
        return "$randomRGB";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher mRGB = PATTERN.matcher(value);
        if (type == INT_ARRAY)
        {
            JsonArray colors = new JsonArray();
            mRGB.find();
            for (int i = 0; i < Integer.parseInt(mRGB.group(1)); i++) colors.add(new JsonPrimitive((RANDOM.nextInt(200) << 16) + (RANDOM.nextInt(200) << 8) + RANDOM.nextInt(200)));
            return mRGB.replaceFirst(colors.toString());
        }
        else
        {
            return mRGB.replaceFirst("" + ((RANDOM.nextInt(200) << 16) + (RANDOM.nextInt(200) << 8) + RANDOM.nextInt(200)));
        }
    }

    @Override
    public boolean matches(int type, String value)
    {
        return (type == INT_ARRAY || type == INT || type == STRING) && PATTERN.matcher(value).find();
    }
}
