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

package net.doubledoordev.pay2spawn.types.guis.shapes;

import net.doubledoordev.pay2spawn.types.guis.HelperGuiBase;
import net.doubledoordev.pay2spawn.types.guis.StructureTypeGui;
import net.doubledoordev.pay2spawn.util.shapes.IShape;
import net.doubledoordev.pay2spawn.util.shapes.Shapes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.types.StructureType.*;

/**
 * Base class for shape GUIs
 *
 * @author Dries007
 */
public abstract class ShapeGuiBase extends HelperGuiBase
{
    public ShapeGuiBase instance = this;
    public JsonArray blockData;
    StructureTypeGui callback;

    public ShapeGuiBase(int index, JsonObject jsonObject, Class<? extends IShape> shape, StructureTypeGui callback, HashMap<String, String> typeMap)
    {
        super(index, shape.getSimpleName(), jsonObject, typeMap);
        this.callback = callback;
        if (!data.has(BLOCKDATA_KEY)) data.add(BLOCKDATA_KEY, new JsonArray());
        if (!data.has(Shapes.SHAPE_KEY)) data.addProperty(Shapes.SHAPE_KEY, shape.getSimpleName());
    }

    protected void init()
    {
        setupListeners();
        readJson();

        dialog = new JDialog();
        dialog.setContentPane(getPanel());
        dialog.setModal(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle(name);
        dialog.setPreferredSize(new Dimension(400, 500));
        dialog.setSize(400, 500);
        dialog.pack();
        dialog.setVisible(true);
    }

    protected ListModel<String> getBlockDataModel()
    {
        return new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                return data.getAsJsonArray(BLOCKDATA_KEY).size();
            }

            @Override
            public String getElementAt(int index)
            {
                JsonObject object = data.getAsJsonArray(BLOCKDATA_KEY).get(index).getAsJsonObject();
                String s = readValue(BLOCKID_KEY, object);
                if (object.has(META_KEY)) s += ":" + readValue(META_KEY, object);
                if (object.has(WEIGHT_KEY)) s += " x " + readValue(WEIGHT_KEY, object);
                if (object.has(TEDATA_KEY)) s += " TE: " + object.get(TEDATA_KEY).toString();
                return s;
            }
        };
    }

    protected void callback(int id, JsonObject data)
    {
        if (id == -1)
        {
            blockData.add(data);
        }
        else
        {
            JsonArray newBlockData = new JsonArray();
            for (int i = 0; i < blockData.size(); i++)
            {
                if (i != id) newBlockData.add(blockData.get(i));
            }
            newBlockData.add(data);
        }
        readJson();
    }
}
