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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Picks a random number in between 2 given numbers, int or double
 * Expected syntax: $random(x,y)
 * Format: A random number between x and y
 * Works with: BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING
 *
 * @author Dries007
 */
public class RndNumberRange implements IRandomResolver
{
    private final static Pattern PATTERN = Pattern.compile("\\$random\\((-?\\w+), ?(-?\\w+)\\)");

    @Override
    public String getIdentifier()
    {
        return "$random(";
    }

    @Override
    public String solverRandom(int type, String value)
    {
        Matcher matcher = PATTERN.matcher(value);
        matcher.find();
        switch (type)
        {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case STRING:
            {
                int begin = Integer.parseInt(matcher.group(1));
                int end = Integer.parseInt(matcher.group(2));
                return matcher.replaceFirst("" + (begin + RANDOM.nextInt(end - begin)));
            }
            case FLOAT:
            case DOUBLE:
            {
                double begin = Double.parseDouble(matcher.group(1));
                double end = Double.parseDouble(matcher.group(2));
                return matcher.replaceFirst("" + (begin + (end - begin) * RANDOM.nextDouble()));
            }
        }
        return value;
    }

    @Override
    public boolean matches(int type, String value)
    {
        return type != BYTE_ARRAY && type != LIST && type != INT_ARRAY && PATTERN.matcher(value).find();
    }
}
