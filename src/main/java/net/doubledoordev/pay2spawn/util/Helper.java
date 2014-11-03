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

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import net.doubledoordev.pay2spawn.util.shapes.PointI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * Static helper functions with no other home
 *
 * @author Dries007
 */
public class Helper
{
    public static final String  FORMAT_WITH_DELIMITER = "((?<=\u00a7[0123456789AaBbCcDdEeFfKkLlMmNnOoRr])|(?=\u00a7[0123456789AaBbCcDdEeFfKkLlMmNnOoRr]))";
    public static final Pattern DOUBLE_QUOTES         = Pattern.compile("\"(.*)\"");

    /**
     * Convert & into § if the next char is a chat formatter char
     *
     * @param message the message to be converted
     * @return the converted message
     */
    public static String formatColors(String message)
    {
        char[] b = message.toCharArray();
        for (int i = 0; i < b.length - 1; i++)
        {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1)
            {
                b[i] = '\u00a7';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    /**
     * Print a message client side
     *
     * @param message the message to be displayed
     */
    public static void msg(String message)
    {
        System.out.println("P2S client message: " + message);
        if (Minecraft.getMinecraft().thePlayer != null) Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(message));
    }

    /**
     * Fill in variables from a donation
     *
     * @param format   text that needs var replacing
     * @param donation the donation data
     * @return the fully var-replaced string
     */
    public static String formatText(String format, Donation donation, Reward reward)
    {
        format = format.replace("$name", donation.username);
        format = format.replace("$uuid", getGameProfileFromName(donation.username).getId().toString());
        format = format.replace("$amount", donation.amount + "");
        format = format.replace("$note", donation.note);
        if (Minecraft.getMinecraft().thePlayer != null)
        {
            format = format.replace("$streamer", Minecraft.getMinecraft().thePlayer.getCommandSenderName());
            format = format.replace("$streameruuid", Minecraft.getMinecraft().thePlayer.getGameProfile().getId().toString());
        }

        if (reward != null)
        {
            format = format.replace("$reward_message", reward.getMessage());
            format = format.replace("$reward_name", reward.getName());
            format = format.replace("$reward_amount", reward.getAmount() + "");
            format = format.replace("$reward_countdown", reward.getCountdown() + "");
        }

        return format;
    }

    private static GameProfileRepository profileRepo;
    private static HashMap<String, GameProfile> nameToProfileMap = new HashMap<>();

    /**
     * Modified method from iChunUtil's EntityHelperBase class
     * @author iChun
     */
    public static GameProfile getGameProfileFromName(String name)
    {
        if (name == null)
        {
            UUID uuid = EntityPlayer.func_146094_a(new GameProfile(null, ANONYMOUS));
            return new GameProfile(uuid, ANONYMOUS);
        }
        if (nameToProfileMap.containsKey(name)) return nameToProfileMap.get(name);

        final GameProfile[] agameprofile = new GameProfile[1];
        ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback()
        {
            public void onProfileLookupSucceeded(GameProfile p_onProfileLookupSucceeded_1_)
            {
                agameprofile[0] = p_onProfileLookupSucceeded_1_;
            }

            public void onProfileLookupFailed(GameProfile p_onProfileLookupFailed_1_, Exception p_onProfileLookupFailed_2_)
            {
                agameprofile[0] = null;
            }
        };
        if (profileRepo == null)
        {
            profileRepo = ((new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), String.format("%s_%s", NAME, UUID.randomUUID())))).createProfileRepository();
        }
        profileRepo.findProfilesByNames(new String[]{name}, Agent.MINECRAFT, profilelookupcallback);

        if (agameprofile[0] == null)
        {
            UUID uuid = EntityPlayer.func_146094_a(new GameProfile(null, name));
            GameProfile gameprofile = new GameProfile(uuid, name);
            profilelookupcallback.onProfileLookupSucceeded(gameprofile);
        }
        else
        {
            nameToProfileMap.put(agameprofile[0].getName(), agameprofile[0]);
        }

        return agameprofile[0];
    }

    /**
     * Fill in variables from a donation
     *
     * @param dataToFormat data to be formatted
     * @param donation     the donation data
     * @return the fully var-replaced JsonElement
     */
    public static JsonElement formatText(JsonElement dataToFormat, Donation donation, Reward reward)
    {
        if (dataToFormat.isJsonPrimitive() && dataToFormat.getAsJsonPrimitive().isString())
        {
            return new JsonPrimitive(Helper.formatText(dataToFormat.getAsString(), donation, reward));
        }
        if (dataToFormat.isJsonArray())
        {
            JsonArray out = new JsonArray();
            for (JsonElement element : dataToFormat.getAsJsonArray())
            {
                out.add(formatText(element, donation, reward));
            }
            return out;
        }
        if (dataToFormat.isJsonObject())
        {
            JsonObject out = new JsonObject();
            for (Map.Entry<String, JsonElement> entity : dataToFormat.getAsJsonObject().entrySet())
            {
                out.add(entity.getKey(), Helper.formatText(entity.getValue(), donation, reward));
            }
            return out;
        }
        return dataToFormat;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isDouble(String text)
    {
        try
        {
            Double.parseDouble(text);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static void addWithEmptyLines(ArrayList<String> list, String header)
    {
        String[] lines = header.split("\\\\n");
        int i = 0;
        for (String s : lines)
            list.add(i++, s);
    }

    public static String removeQuotes(String s)
    {
        Matcher m = DOUBLE_QUOTES.matcher(s);
        if (m.matches()) return m.group(1);
        else return s;
    }

    public static boolean rndSpawnPoint(ArrayList<PointD> pointDs, Entity entity)
    {
        Collections.shuffle(pointDs, RANDOM);
        for (PointD p : pointDs)
        {
            Collections.shuffle(pointDs, RANDOM);
            if (p.canSpawn(entity))
            {
                p.setPosition(entity);
                return true;
            }
        }
        return false;
    }

    public static String readUrl(URL url) throws IOException
    {
        BufferedReader reader = null;
        StringBuilder buffer = new StringBuilder();

        try
        {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) buffer.append(chars, 0, read);
        }
        finally
        {
            if (reader != null) reader.close();
        }
        return buffer.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isInt(String text)
    {
        try
        {
            Integer.parseInt(text);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static double findMax(Collection<Double> vals)
    {
        double max = Double.MIN_VALUE;

        for (double d : vals)
        {
            if (d > max) max = d;
        }

        return max;
    }

    public static void sendChatToPlayer(ICommandSender player, String message, EnumChatFormatting chatFormatting)
    {
        player.addChatMessage(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(chatFormatting)));
    }

    public static void sendChatToPlayer(EntityPlayer player, String message)
    {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static int round(double d)
    {
        return MathHelper.floor_double(d);

    }

    public static void renderPoint(PointI p, Tessellator tess, double r, double g, double b)
    {
        GL11.glColor3d(r, g, b);
        renderPoint(p, tess);
    }

    public static void renderPoint(PointI p, Tessellator tess)
    {
        renderPoint(tess, p.getX(), p.getY(), p.getZ());
    }

    public static void renderPoint(Tessellator tess, int x, int y, int z)
    {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glScalef(1.01f, 1.01f, 1.01f);
        tess.startDrawing(GL11.GL_LINES);

        // FRONT
        tess.addVertex(0, 0, 0);
        tess.addVertex(0, 1, 0);

        tess.addVertex(0, 1, 0);
        tess.addVertex(1, 1, 0);

        tess.addVertex(1, 1, 0);
        tess.addVertex(1, 0, 0);

        tess.addVertex(1, 0, 0);
        tess.addVertex(0, 0, 0);

        // BACK
        tess.addVertex(0, 0, -1);
        tess.addVertex(0, 1, -1);
        tess.addVertex(0, 0, -1);
        tess.addVertex(1, 0, -1);
        tess.addVertex(1, 0, -1);
        tess.addVertex(1, 1, -1);
        tess.addVertex(0, 1, -1);
        tess.addVertex(1, 1, -1);

        // betweens.
        tess.addVertex(0, 0, 0);
        tess.addVertex(0, 0, -1);

        tess.addVertex(0, 1, 0);
        tess.addVertex(0, 1, -1);

        tess.addVertex(1, 0, 0);
        tess.addVertex(1, 0, -1);

        tess.addVertex(1, 1, 0);
        tess.addVertex(1, 1, -1);

        tess.draw();
        GL11.glPopMatrix();
    }

    public static String makeTable(TableData... datas)
    {
        int size = 0;
        for (TableData data : datas) size += data.width * data.strings.size();
        StringBuilder stringBuilder = new StringBuilder(size);

        for (TableData data : datas) stringBuilder.append('|').append(' ').append(data.header).append(new String(new char[data.width - data.header.length() + 1]).replace('\0', ' '));
        stringBuilder.append('|').append('\n');
        for (TableData data : datas) stringBuilder.append('+').append(new String(new char[data.width + 2]).replace('\0', '-'));
        stringBuilder.append('+').append('\n');
        int i = 0;
        while (i < datas[0].strings.size())
        {
            for (TableData data : datas) stringBuilder.append('|').append(' ').append(data.strings.get(i)).append(new String(new char[data.width - data.strings.get(i).length() + 1]).replace('\0', ' '));
            stringBuilder.append('|').append('\n');
            i++;
        }

        return stringBuilder.toString();
    }

    public static int getHeading(EntityPlayer player)
    {
        return MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
    }

    /**
     * Returns a <code>true</code> if the supplied username is an OP in the server list
     *
     * @param username <code>String</code> containing the username to check OP status
     * @return <code>boolean</code>
     */
    public static boolean isPlayerOpped(String username)
    {
        MinecraftServer server = MinecraftServer.getServer();

        return server.getConfigurationManager().func_152596_g(server.func_152358_ax().func_152655_a(username));
    }

    /**
     * Used to get the MD5 hash of a given string.
     *
     * @param inputString
     * @return <code>String</code> containing MD5 hash or null if error caught
     */
    public static String MD5(String inputString)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(inputString.getBytes());

            StringBuffer stringBuilder = new StringBuffer();

            for (int i = 0; i < array.length; ++i)
            {
                stringBuilder.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return stringBuilder.toString();
        }
        catch (java.security.NoSuchAlgorithmException e)
        {
        }

        return null;
    }

    public static void writeLongStringToByteBuf(ByteBuf buf, String s)
    {
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readLongStringToByteBuf(ByteBuf buf)
    {
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        return new String(bytes, Charsets.UTF_8);
    }

    public static String getTimeString(int ms)
    {
        StringBuilder sb = new StringBuilder();
        if (ms > 60 * 60 * 1000)
        {
            sb.append(ms / (60 * 60 * 1000)).append("h ");
            ms %= 60 * 60 * 1000;
        }
        if (ms > 60 * 1000)
        {
            sb.append(ms / (60 * 1000)).append("m ");
            ms %= 60 * 1000;
        }
        sb.append((double) ms / 1000).append("s");
        return sb.toString();
    }

    /**
     * a > 32k check
     *
     * @return true if too big
     */
    public static boolean checkTooBigForNetwork(NBTTagCompound root)
    {
        try
        {
            if (CompressedStreamTools.compress(root).length > 32000)
            {
                JOptionPane.showMessageDialog(null, "Your reward is too big. (>32 kb)\nYou will CRASH if you spawn this in.\nYou can fix this by separating one large reward into 2 or more smaller rewards.", "Reward too big", JOptionPane.ERROR_MESSAGE);
                return true;
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * a > 32k check
     *
     * @return true if too big
     */
    public static boolean checkTooBigForNetwork(JsonObject object)
    {
        return checkTooBigForNetwork(JsonNBTHelper.parseJSON(object));
    }

    public static final class TableData
    {
        public  String            header;
        public  ArrayList<String> strings;
        private int               width;

        public TableData(String header, ArrayList<String> data)
        {
            this.header = header;
            this.strings = data;
            width = header.length();

            updateWidth();
        }

        private void updateWidth()
        {
            for (String string : strings) if (width < string.length()) width = string.length();
        }
    }
}
