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

package net.doubledoordev.pay2spawn.types.guis;

import com.google.common.base.Strings;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Base class for type GUIs
 *
 * @author Dries007
 */
public abstract class HelperGuiBase
{
    public static final String RANDOM_BOOLEAN = "$random()";
    public static final String TRUE_BYTE      = "1";
    public static final String FALSE_BYTE     = "0";

    public final String                  name;
    public final int                     rewardID;
    public final HashMap<String, String> typeMap;
    public       JsonObject              data;
    public       JDialog                 dialog;

    public HelperGuiBase(final int rewardID, final String name, final JsonObject inputData, final HashMap<String, String> typeMap)
    {
        this.rewardID = rewardID;
        this.name = name;
        this.typeMap = typeMap;
        this.data = inputData;
    }

    public void makeAndOpen()
    {
        setupListeners();
        readJson();

        dialog = new JDialog();
        dialog.setContentPane(getPanel());
        dialog.setModal(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle("Editing: " + name);
        dialog.setPreferredSize(new Dimension(600, 750));
        dialog.setSize(400, 750);
        setupDialog();
        dialog.pack();
        dialog.setVisible(true);
    }

    public void setupDialog()
    {

    }

    public abstract void readJson();

    public abstract void updateJson();

    public abstract void setupListeners();

    public String readValue(String key, JsonObject jsonObject)
    {
        if (jsonObject == null || !jsonObject.has(key)) return "";
        String string = jsonObject.get(key).getAsString();
        return string.substring(string.indexOf(":") + 1);
    }

    public void storeValue(String key, JsonObject jsonObject, Object value)
    {
        if (key == null || jsonObject == null) return;
        if (value == null)
        {
            jsonObject.add(key, JsonNull.INSTANCE);
            return;
        }
        if (Strings.isNullOrEmpty(value.toString())) jsonObject.remove(key);
        else jsonObject.addProperty(key, typeMap != null && typeMap.containsKey(key) ? typeMap.get(key) + ":" + value.toString() : value.toString());
    }

    public void close()
    {
        dialog.dispose();
    }

    public abstract JPanel getPanel();
}
