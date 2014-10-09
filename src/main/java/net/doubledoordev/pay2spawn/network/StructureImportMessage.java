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

package net.doubledoordev.pay2spawn.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.types.guis.StructureTypeGui;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.doubledoordev.pay2spawn.util.shapes.PointI;
import net.doubledoordev.pay2spawn.util.shapes.Shapes;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.IOException;

import static net.doubledoordev.pay2spawn.types.StructureType.*;
import static net.doubledoordev.pay2spawn.util.Constants.COMPOUND;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * Reads all blockID, metadata and NBT from a list of points
 *
 * Uses NBT instead of a stringified JSON array because of network efficiency
 *
 * @author Dries007
 */
public class StructureImportMessage implements IMessage
{
    NBTTagCompound root;

    public StructureImportMessage()
    {
    }

    public StructureImportMessage(NBTTagCompound root)
    {
        this.root = root;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        root = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, root);
    }

    public static class Handler implements IMessageHandler<StructureImportMessage, IMessage>
    {
        @Override
        public IMessage onMessage(StructureImportMessage message, MessageContext ctx)
        {
            if (ctx.side.isServer())
            {
                int offsetx = message.root.getInteger("x"), offsety = message.root.getInteger("y"), offsetz = message.root.getInteger("z");
                NBTTagCompound newRoot = new NBTTagCompound();
                NBTTagList newList = new NBTTagList();

                NBTTagList list = message.root.getTagList("list", COMPOUND);
                for (int i = 0; i < list.tagCount(); i++)
                {
                    PointI point = new PointI(list.getCompoundTagAt(i));
                    World world = ctx.getServerHandler().playerEntity.worldObj;
                    int x = point.getX(), y = point.getY(), z = point.getZ();

                    // Set up the correct block data
                    NBTTagList blockDataNbt = new NBTTagList();
                    {
                        NBTTagCompound compound = new NBTTagCompound();

                        // BlockID
                        compound.setInteger(BLOCKID_KEY, Block.getIdFromBlock(world.getBlock(x, y, z)));

                        // metaData
                        int meta = world.getBlockMetadata(x, y, z);
                        if (meta != 0) compound.setInteger(META_KEY, meta);

                        // TileEntity
                        TileEntity te = world.getTileEntity(x, y, z);
                        if (te != null)
                        {
                            NBTTagCompound teNbt = new NBTTagCompound();
                            te.writeToNBT(teNbt);
                            teNbt.removeTag("x");
                            teNbt.removeTag("y");
                            teNbt.removeTag("z");
                            compound.setTag(TEDATA_KEY, teNbt);
                        }

                        blockDataNbt.appendTag(compound);
                    }
                    NBTTagCompound shapeNbt = point.move(offsetx, offsety, offsetz).toNBT();
                    shapeNbt.setTag(BLOCKDATA_KEY, blockDataNbt);
                    newList.appendTag(shapeNbt);
                }

                newRoot.setTag("list", newList);
                return new StructureImportMessage(newRoot);
            }
            else
            {
                StructureTypeGui.importCallback(message.root);
                return null;
            }
        }
    }
}
