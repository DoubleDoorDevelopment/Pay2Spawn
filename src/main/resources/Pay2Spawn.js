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

//todo: write more docs
//todo: make wiki page, see example.js

/**
 * Only a partial document. Feel free to contribute.
 *
 * Script context is always the server.
 *
 * @author Dries007
 */

/**
 * This helper provided by Pay2Spawn
 * @name p2s
 * @type {ScriptUtils}
 * @global
 * @readonly
 */

// ########## ScriptUtils

/**
 * @typedef {object} ScriptUtils
 * @readonly
 * @property {ICommandSender} runner The person/thing that caused the script to be run
 * @property {Donation} donation The donation that is associated with the
 * @property {Reward} reward
 * @property {Logger} logger
 * @property {EntityPlayer} target
 * @property {MinecraftServer} server
 */

/**
 * @callback ScriptUtils.speak
 * @param {...string}
 */

/**
 * @callback ScriptUtils.chat
 * @param {...string}
 */

/**
 * @callback ScriptUtils.cmd
 * @param {...string}
 */

/**
 * @callback ScriptUtils.log
 * @param {(string|object)}
 * @param {...*}
 */

/**
 * @callback ScriptUtils.getWorld
 * @return {WorldServer}
 */

/**
 * @callback ScriptUtils.run
 * @return {function}
 */

// ########## Donation

/**
 * @typedef {object} Donation
 * @readonly
 * @property {string} name
 * @property {number} amount
 * @property {number} timestamp
 * @property {string} note
 */

// ########## Reward

/**
 * @typedef {object} Reward
 * @readonly
 * @property {string} name
 * @property {double} amount
 * @property {string} script
 * @property {string} language
 */

// ########## ICommandSender

/**
 * @typedef {object} ICommandSender
 */

/**
 * @callback ICommandSender.getName
 * @return {string}
 */

/**
 * @callback ICommandSender.getPosition
 * @return {BlockPos}
 */

/**
 * @callback ICommandSender.getPositionVector
 * @return {Vec3d}
 */

// ########## BlockPos

/**
 * All integers (Use for world manupulation)
 * @typedef {object} BlockPos
 * @readonly
 * @property {number} x
 * @property {number} y
 * @property {number} z
 */

// ########## Vec3d

/**
 * All doubles (Use for entity manupulation)
 * @typedef {object} Vec3d
 * @readonly
 * @property {number} x
 * @property {number} y
 * @property {number} z
 */

