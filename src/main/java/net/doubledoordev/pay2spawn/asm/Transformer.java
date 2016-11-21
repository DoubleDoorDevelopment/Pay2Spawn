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

package net.doubledoordev.pay2spawn.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Dries007
 */
public class Transformer implements IClassTransformer
{
    private final static boolean DEOBF = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private static final String COMMANDBASE_NAME = "net.minecraft.command.CommandBase";
    private static final String ICOMMANDSENDER_TYPE = "net/minecraft/command/ICommandSender";
    private static final String ENTITYPLAYERMP_TYPE = "net/minecraft/entity/player/EntityPlayerMP";
    private static final String TARGET_DESC = "(L" + ICOMMANDSENDER_TYPE + ";)L" + ENTITYPLAYERMP_TYPE + ";";
    private static final String TARGET_NAME;
    private static final String GETCOMMANDSENDERENTITY_NAME;
    private static final String GETCOMMANDSENDERENTITY_DESC = "()Lnet/minecraft/entity/Entity;";

    static
    {
        if (DEOBF)
        {
            TARGET_NAME = "getCommandSenderAsPlayer";
            GETCOMMANDSENDERENTITY_NAME = "getCommandSenderEntity";
        }
        else
        {
            TARGET_NAME = "func_71521_c";
            GETCOMMANDSENDERENTITY_NAME = "func_174793_f";
        }

        Plugin.LOGGER.info("Looking for {} {}", TARGET_NAME, TARGET_DESC);
    }

    public Transformer()
    {
        Plugin.LOGGER.info("Loaded Black magic aka Transformer. Deobf: {}", DEOBF);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (transformedName.equals(COMMANDBASE_NAME)) return transformCommandBase(basicClass);
        return basicClass;
    }

    private byte[] transformCommandBase(byte[] basicClass)
    {
        Plugin.LOGGER.info("Found CommandBase");

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

        for (MethodNode method : classNode.methods)
        {
            if (method.name.equals(TARGET_NAME) && method.desc.equals(TARGET_DESC))
            {
                Plugin.LOGGER.info("Found getCommandSenderAsPlayer");

                InsnList inject = new InsnList();
                {
                    LabelNode ourLabel = new LabelNode();
                    inject.add(new VarInsnNode(ALOAD, 0));
                    inject.add(new MethodInsnNode(INVOKEINTERFACE, ICOMMANDSENDER_TYPE, GETCOMMANDSENDERENTITY_NAME, GETCOMMANDSENDERENTITY_DESC, true));
                    inject.add(new TypeInsnNode(INSTANCEOF, ENTITYPLAYERMP_TYPE));
                    inject.add(new JumpInsnNode(IFEQ, ourLabel));
                    inject.add(new VarInsnNode(ALOAD, 0));
                    inject.add(new MethodInsnNode(INVOKEINTERFACE, ICOMMANDSENDER_TYPE, GETCOMMANDSENDERENTITY_NAME, GETCOMMANDSENDERENTITY_DESC, true));
                    inject.add(new TypeInsnNode(CHECKCAST, ENTITYPLAYERMP_TYPE));
                    inject.add(new InsnNode(ARETURN));
                    inject.add(ourLabel);
                }
                InsnList list = method.instructions;

                AbstractInsnNode node = list.getFirst();
                while (node.getOpcode() != NEW && node != list.getLast()) node = node.getNext();

                if (node.getOpcode() != NEW) break;

                list.insertBefore(node, inject);
                Plugin.LOGGER.info("Done injecting!");

                final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(writer);
                return writer.toByteArray();
            }
        }

        throw new RuntimeException("Could not complete injection for CommandBase.");
    }
}
