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

package net.doubledoordev.pay2spawn.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.common.registry.GameData;
import net.doubledoordev.pay2spawn.random.RandomRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.Map;

import static net.doubledoordev.pay2spawn.types.StructureType.BLOCKID_KEY;
import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * This is nearly full Json (gson) to NBT converter.
 * Not working:
 * NBT IntArrays and ByteArrays to Json.
 * Json null to NBT
 *
 * @author Dries007
 */
public class JsonNBTHelper
{
    /**
     * To avoid idiocy later we need to store all things as a string with the type in the string. :(
     * Please tell your users about this!
     *
     * @see net.doubledoordev.pay2spawn.util.JsonNBTHelper#parseJSON(com.google.gson.JsonPrimitive)
     */
    public static JsonElement parseNBT(NBTBase element)
    {
        switch (element.getId())
        {
            // 0 = END
            case BYTE:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagByte) element).func_150290_f());
            case SHORT:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagShort) element).func_150289_e());
            case INT:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagInt) element).func_150287_d());
            case LONG:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagLong) element).func_150291_c());
            case FLOAT:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagFloat) element).func_150288_h());
            case DOUBLE:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagDouble) element).func_150286_g());
            case BYTE_ARRAY:
                return parseNBT((NBTTagByteArray) element);
            case STRING:
                return new JsonPrimitive(NBTTypes[element.getId()] + ":" + ((NBTTagString) element).func_150285_a_());
            case LIST:
                return parseNBT((NBTTagList) element);
            case COMPOUND:
                return parseNBT((NBTTagCompound) element);
            case INT_ARRAY:
                return parseNBT((NBTTagIntArray) element);
            default:
                return null;
        }
    }

    public static JsonPrimitive parseNBT(NBTTagIntArray nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i : nbtArray.func_150302_c()) jsonArray.add(new JsonPrimitive(i));
        return new JsonPrimitive(NBTTypes[nbtArray.getId()] + ":" + jsonArray.toString());
    }

    public static JsonPrimitive parseNBT(NBTTagByteArray nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i : nbtArray.func_150292_c()) jsonArray.add(new JsonPrimitive(i));
        return new JsonPrimitive(NBTTypes[nbtArray.getId()] + jsonArray.toString());
    }

    public static JsonArray parseNBT(NBTTagList nbtArray)
    {
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < nbtArray.tagCount(); i++)
        {
            switch (nbtArray.func_150303_d())
            {
                case 5:
                    jsonArray.add(parseNBT(new NBTTagFloat(nbtArray.func_150308_e(i))));
                    break;
                case 6:
                    jsonArray.add(parseNBT(new NBTTagDouble(nbtArray.func_150309_d(i))));
                    break;
                case 8:
                    jsonArray.add(parseNBT(new NBTTagString(nbtArray.getStringTagAt(i))));
                    break;
                case 10:
                    jsonArray.add(parseNBT(nbtArray.getCompoundTagAt(i)));
                    break;
                case 11:
                    jsonArray.add(parseNBT(new NBTTagIntArray(nbtArray.func_150306_c(i))));
                    break;
            }

        }
        return jsonArray;
    }

    public static JsonObject parseNBT(NBTTagCompound compound)
    {
        boolean isItemStack = ItemStack.loadItemStackFromNBT(compound) != null;
        JsonObject jsonObject = new JsonObject();
        for (Object object : compound.func_150296_c())
        {
            if (object.equals("id") && compound.getTag(object.toString()).getId() == SHORT && isItemStack) // Itemstack?
            {
                int id = compound.getShort("id");
                Item item = GameData.getItemRegistry().getObjectById(id);
                jsonObject.addProperty(object.toString(), NBTTypes[SHORT] + ":" + (item == GameData.getItemRegistry().getDefaultValue() ? id : GameData.getItemRegistry().getNameForObject(item)));
            }
            else if (object.equals(BLOCKID_KEY) && compound.getTag(object.toString()).getId() == INT && isItemStack) // Itemstack?
            {
                int id = compound.getInteger(BLOCKID_KEY);
                Block block = GameData.getBlockRegistry().getObjectById(id);
                jsonObject.addProperty(object.toString(), NBTTypes[INT] + ":" + (block == GameData.getBlockRegistry().getDefaultValue() ? id : GameData.getBlockRegistry().getNameForObject(block)));
            }
            else
                jsonObject.add(object.toString(), parseNBT(compound.getTag(object.toString())));
        }
        return jsonObject;
    }

    public static NBTBase parseJSON(JsonElement element)
    {
        if (element.isJsonObject()) return parseJSON(element.getAsJsonObject());
        else if (element.isJsonArray()) return parseJSON(element.getAsJsonArray());
        else if (element.isJsonPrimitive()) return parseJSON(element.getAsJsonPrimitive());

        return null;
    }

    /**
     * There is no way to detect number types and NBT is picky about this. Lets hope the original type id is there, otherwise we are royally screwed.
     */
    public static NBTBase parseJSON(JsonPrimitive element)
    {
        String string = element.getAsString();
        if (string.contains(":"))
        {
            for (int id = 0; id < NBTTypes.length; id++)
            {
                if (string.startsWith(NBTTypes[id] + ":"))
                {
                    String value = string.replace(NBTTypes[id] + ":", "");
                    value = RandomRegistry.solveRandom(id, value);
                    switch (id)
                    {
                        // 0 = END
                        case BYTE:
                            //return new NBTTagByte(Byte.parseByte(value));
                        case SHORT:
                            //return new NBTTagShort(Short.parseShort(value));
                        case INT:
                            return new NBTTagInt(Integer.parseInt(value));
                        case LONG:
                            return new NBTTagLong(Long.parseLong(value));
                        case FLOAT:
                            return new NBTTagFloat(Float.parseFloat(value));
                        case DOUBLE:
                            return new NBTTagDouble(Double.parseDouble(value));
                        case BYTE_ARRAY:
                            return parseJSONByteArray(value);
                        case STRING:
                            return new NBTTagString(value);
                        // 9 = LIST != JsonPrimitive
                        // 10 = COMPOUND != JsonPrimitive
                        case INT_ARRAY:
                            return parseJSONIntArray(value);
                    }
                }
            }
        }

        // Now it becomes guesswork.
        if (element.isString()) return new NBTTagString(string);
        if (element.isBoolean()) return new NBTTagByte((byte) (element.getAsBoolean() ? 1 : 0));

        Number n = element.getAsNumber();
        if (n instanceof Byte) return new NBTTagByte(n.byteValue());
        if (n instanceof Short) return new NBTTagShort(n.shortValue());
        if (n instanceof Integer) return new NBTTagInt(n.intValue());
        if (n instanceof Long) return new NBTTagLong(n.longValue());
        if (n instanceof Float) return new NBTTagFloat(n.floatValue());
        if (n instanceof Double) return new NBTTagDouble(n.doubleValue());

        try
        {
            return new NBTTagInt(Integer.parseInt(element.toString()));
        }
        catch (NumberFormatException ignored)
        {

        }
        throw new NumberFormatException(element.getAsNumber() + " is was not able to be parsed.");
    }

    public static NBTTagByteArray parseJSONByteArray(String value)
    {
        JsonArray in = JSON_PARSER.parse(value).getAsJsonArray();
        byte[] out = new byte[in.size()];
        for (int i = 0; i < in.size(); i++) out[i] = in.get(i).getAsByte();
        return new NBTTagByteArray(out);
    }

    public static NBTTagIntArray parseJSONIntArray(String value)
    {
        JsonArray in = JSON_PARSER.parse(value).getAsJsonArray();
        int[] out = new int[in.size()];
        for (int i = 0; i < in.size(); i++) out[i] = in.get(i).getAsInt();
        return new NBTTagIntArray(out);
    }

    public static NBTTagCompound parseJSON(JsonObject data)
    {
        NBTTagCompound root = new NBTTagCompound();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) root.setTag(entry.getKey(), parseJSON(entry.getValue()));
        return root;
    }

    public static NBTTagList parseJSON(JsonArray data)
    {
        NBTTagList list = new NBTTagList();
        for (JsonElement element : data) list.appendTag(parseJSON(element));
        return list;
    }

    public static JsonElement cloneJSON(JsonElement toClone)
    {
        return JSON_PARSER.parse(toClone.toString());
    }

    public static JsonElement fixNulls(JsonElement element)
    {
        if (element.isJsonNull()) return new JsonPrimitive("");
        if (element.isJsonObject()) return fixNulls(element.getAsJsonObject());
        if (element.isJsonArray()) return fixNulls(element.getAsJsonArray());
        if (element.isJsonPrimitive()) return fixNulls(element.getAsJsonPrimitive());
        return null;
    }

    public static JsonPrimitive fixNulls(JsonPrimitive primitive)
    {
        if (primitive.isBoolean()) return new JsonPrimitive(primitive.getAsBoolean());
        if (primitive.isNumber()) return new JsonPrimitive(primitive.getAsNumber());
        if (primitive.isString()) return new JsonPrimitive(primitive.getAsString());
        return JSON_PARSER.parse(primitive.toString()).getAsJsonPrimitive();
    }

    public static JsonArray fixNulls(JsonArray array)
    {
        JsonArray newArray = new JsonArray();
        for (JsonElement element : array) newArray.add(fixNulls(element));
        return newArray;
    }

    public static JsonObject fixNulls(JsonObject object)
    {
        JsonObject newObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) newObject.add(entry.getKey(), fixNulls(entry.getValue()));
        return newObject;
    }
}
