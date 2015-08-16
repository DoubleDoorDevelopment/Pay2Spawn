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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * ModID and P2S and so much more...
 *
 * @author Dries007
 */
public class Constants
{
    public static final String NAME  = "Pay2Spawn";
    public static final String MODID = "P2S";

    public static final String SERVER_CAT       = MODID + "_server";
    public static final String FILTER_CAT       = MODID + "_filter";
    public static final String TYPES_CAT        = MODID + "_types";
    public static final String BASECAT_TRACKERS = "P2S_trackers";
    public static final String MUSICURL         = "https://raw.github.com/CCM-Modding/Pay2Spawn/master/files/music.zip";

    public static final String ANONYMOUS  = "Anonymous";
    public static final String CUSTOMHTML = "customHTML";

    /**
     * Global helpers
     */
    public static final Random RANDOM = new Random();

    public static final Joiner JOINER_COMMA_SPACE = Joiner.on(", ").skipNulls();
    public static final Joiner JOINER_DOT         = Joiner.on(".").skipNulls();

    public static final JsonParser JSON_PARSER = new JsonParser();
    public static final Gson       GSON        = new GsonBuilder().registerTypeHierarchyAdapter(Donation.class, new DonationJson()).setPrettyPrinting().create();
    public static final Gson       GSON_NOPP   = new GsonBuilder().create();

    public static final NumberFormat NUMBER_FORMATTER   = new DecimalFormat("#.##");
    public static final NumberFormat CURRENCY_FORMATTER = new DecimalFormat("0.00");

    /**
     * NBT constants
     */
    public static final String[] NBTTypes   = new String[]{"END", "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BYTE[]", "STRING", "LIST", "COMPOUND", "INT[]"};
    public static final int      END        = 0;
    public static final int      BYTE       = 1;
    public static final int      SHORT      = 2;
    public static final int      INT        = 3;
    public static final int      LONG       = 4;
    public static final int      FLOAT      = 5;
    public static final int      DOUBLE     = 6;
    public static final int      BYTE_ARRAY = 7;
    public static final int      STRING     = 8;
    public static final int      LIST       = 9;
    public static final int      COMPOUND   = 10;
    public static final int      INT_ARRAY  = 11;

    private static final class DonationJson implements JsonDeserializer<Donation>
    {
        @Override
        public Donation deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (!element.isJsonObject()) throw new JsonParseException("a Donation needs to be an object");
            JsonObject json = element.getAsJsonObject();
            String id = getOrThrow(json, "id").getAsString();
            double amount = getOrThrow(json, "amount").getAsDouble();
            String name = getOrDefault(json, "Anonymous", "name", "username").getAsString();
            String note = getOrDefault(json, "", "note").getAsString();
            long time = ((Date) context.deserialize(getOrThrow(json, "time", "timestamp"), new TypeToken<Date>(){}.getType())).getTime();
            return new Donation(id, amount, time, name, note);
        }

        private static JsonElement getOrDefault(JsonObject json, String def, String... names)
        {
            for (String name : names) if (json.has(name)) return json.get(name);
            return new JsonPrimitive(def);
        }

        private static JsonElement getOrThrow(JsonObject json, String... names) throws JsonParseException
        {
            for (String name : names) if (json.has(name)) return json.get(name);
            throw new JsonParseException("Missing filed! You need one of: " + Arrays.toString(names));
        }
    }
}
