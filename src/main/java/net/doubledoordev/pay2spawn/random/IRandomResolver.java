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

/**
 * Part of API.
 * Register your implementation!
 * <br>
 * Expected javadoc:
 * <br>
 * [Short explanation + example if needed]
 * Expected syntax: [$random for example]
 * Outcome: [outcome]
 * Works with: [All nbt types this will accept, enforce in #matches.]
 *
 * @author Dries007
 * @see net.minecraft.nbt.NBTBase#NBTTypes
 * @see net.doubledoordev.pay2spawn.random.RndBoolean RndBoolean for an example
 * @see RandomRegistry#addRandomResolver(IRandomResolver) RandomRegistry.addRandomResolver to register.
 */
public interface IRandomResolver
{

    /**
     * Gets called when #matches returns true
     *
     * @param type  NBT type
     * @param value The random tag
     * @return the randomised value
     */
    public String solverRandom(int type, String value);

    /**
     * Only return true when you can handle the type and the value matches your (and only your) pattern.
     *
     * @param type  NBT type
     * @param value The random tag
     * @return true to handle this string
     */
    public boolean matches(int type, String value);
}
