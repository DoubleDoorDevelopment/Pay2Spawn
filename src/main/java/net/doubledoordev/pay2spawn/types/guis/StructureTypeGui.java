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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.pay2spawn.configurator.Configurator;
import net.doubledoordev.pay2spawn.network.TestMessage;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.doubledoordev.pay2spawn.util.shapes.IShape;
import net.doubledoordev.pay2spawn.util.shapes.PointI;
import net.doubledoordev.pay2spawn.util.shapes.Shapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static net.doubledoordev.pay2spawn.types.StructureType.*;
import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class StructureTypeGui extends HelperGuiBase
{
    private static StructureTypeGui instance;
    public final ArrayList<IShape> ishapes = new ArrayList<>();
    public JPanel        panel1;
    public JTextField    HTMLTextField;
    public JScrollPane   scrollPane;
    public JTextPane     jsonPane;
    public JButton       parseFromJsonButton;
    public JButton       saveButton;
    public JButton       updateJsonButton;
    public JButton       testButton;
    public JButton       addShapeButton;
    public JList<String> shapeList;
    public JButton       removeShapeButton;
    public JButton       importButton;
    public JCheckBox     renderShapesIngameCheckBox;
    public JCheckBox     renderSelectedShapeInCheckBox;
    public JsonArray shapes   = new JsonArray();
    public boolean   disabled = false;
    private JCheckBox  rotateBasedOnPlayerCheckBox;
    private JTextField baseRotation;

    public StructureTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);
        instance = this;

        setupModels();
        makeAndOpen();
    }

    public static void importCallback(NBTTagCompound root)
    {
        NBTTagList list = root.getTagList("list", COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            instance.shapes.add(JsonNBTHelper.parseNBT(Shapes.addShapeType(list.getCompoundTagAt(i), PointI.class)));
        }
        instance.updateJson();
        instance.shapeList.clearSelection();
    }

    @Override
    public void setupDialog()
    {
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        new ForgeEventbusDialogThing(dialog, this);

        synchronized (ishapes)
        {
            ishapes.clear();
            if (shapes == null) shapes = new JsonArray();

            for (JsonElement element : shapes) ishapes.add(Shapes.loadShape(JsonNBTHelper.parseJSON(element.getAsJsonObject())));
        }
    }

    @Override
    public void readJson()
    {
        HTMLTextField.setText(readValue(CUSTOMHTML, data));

        shapes = data.getAsJsonArray(SHAPES_KEY);
        shapeList.updateUI();

        rotateBasedOnPlayerCheckBox.setSelected(readValue(ROTATE_KEY, data).equals(TRUE_BYTE));
        baseRotation.setText(readValue(BASEROTATION_KEY, data));

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

        data.add(SHAPES_KEY, shapes);
        storeValue(ROTATE_KEY, data, rotateBasedOnPlayerCheckBox.isSelected() ? TRUE_BYTE : FALSE_BYTE);
        storeValue(BASEROTATION_KEY, data, baseRotation.getText());

        synchronized (ishapes)
        {
            ishapes.clear();
            for (JsonElement element : shapes) ishapes.add(Shapes.loadShape(JsonNBTHelper.parseJSON(element.getAsJsonObject())));
        }

        shapeList.updateUI();

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void setupListeners()
    {
        testButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                TestMessage.sendToServer(name, data);
            }
        });
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                Configurator.instance.callback(rewardID, name, data);

                dialog.dispose();
            }
        });
        parseFromJsonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    data = JSON_PARSER.parse(jsonPane.getText()).getAsJsonObject();
                    readJson();
                    jsonPane.setForeground(Color.black);
                    shapeList.clearSelection();
                }
                catch (Exception e1)
                {
                    jsonPane.setForeground(Color.red);
                    e1.printStackTrace();
                }
            }
        });
        updateJsonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
            }
        });
        addShapeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Shapes.MAP.get(JOptionPane.showInputDialog(instance.panel1, "Please pick a new shape to add.", "Pick a shape", JOptionPane.QUESTION_MESSAGE, null, Shapes.LIST.toArray(), Shapes.LIST.get(0))).openGui(-1, new JsonObject(), instance);
                shapeList.clearSelection();
            }
        });
        shapeList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    JsonObject object = shapes.get(shapeList.getSelectedIndex()).getAsJsonObject();
                    Shapes.MAP.get(readValue(Shapes.SHAPE_KEY, object)).openGui(shapeList.getSelectedIndex(), object, instance);
                    shapeList.clearSelection();
                }
            }
        });
        removeShapeButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                JsonArray newShapes = new JsonArray();
                int[] ints = shapeList.getSelectedIndices();
                HashSet<Integer> selection = new HashSet<>(ints.length);
                for (int i : ints) selection.add(i);

                for (int i = 0; i < shapes.size(); i++)
                {
                    if (!selection.contains(i)) newShapes.add(shapes.get(i));
                }
                shapes = newShapes;
                updateJson();
                removeShapeButton.setEnabled(!shapeList.isSelectionEmpty());
                shapeList.clearSelection();
            }
        });
        importButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                shapeList.clearSelection();
                new StructureImporter(instance);
            }
        });
        rotateBasedOnPlayerCheckBox.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                baseRotation.setEnabled(rotateBasedOnPlayerCheckBox.isSelected());
            }
        });
    }

    @Override
    public JPanel getPanel()
    {
        return panel1;
    }

    public void callback(int id, JsonObject data)
    {
        if (id == -1) shapes.add(data);
        else
        {
            JsonArray newShape = new JsonArray();
            for (int i = 0; i < shapes.size(); i++) if (i != id) newShape.add(shapes.get(i));
            newShape.add(data);
            shapes = newShape;
        }
        updateJson();
        shapeList.clearSelection();
    }

    private void setupModels()
    {
        shapeList.setModel(new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                return shapes.size();
            }

            @Override
            public String getElementAt(int index)
            {
                JsonObject object = shapes.get(index).getAsJsonObject();
                return instance.readValue(Shapes.SHAPE_KEY, object) + ": " + object.toString();
            }
        });
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event)
    {
        if (disabled || !renderShapesIngameCheckBox.isSelected()) return;
        if (ishapes.size() == 0) return;

        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, 1 - RenderManager.renderPosZ);
        GL11.glTranslated(Helper.round(Minecraft.getMinecraft().thePlayer.posX), Helper.round(Minecraft.getMinecraft().thePlayer.posY), Helper.round(Minecraft.getMinecraft().thePlayer.posZ));
        GL11.glScalef(1.0F, 1.0F, 1.0F);

        if (rotateBasedOnPlayerCheckBox.isSelected())
        {
            try
            {
                int i = Integer.parseInt(baseRotation.getText());
                if (i != -1)
                {
                    GL11.glRotated(90 * i, 0, -1, 0);

                    switch (i)
                    {
                        case 1:
                            GL11.glTranslated(-1, 0, 0);
                            break;
                        case 2:
                            GL11.glTranslated(-1, 0, 1);
                            break;
                        case 3:
                            GL11.glTranslated(0, 0, 1);
                            break;
                    }
                }
            }
            catch (Exception ignored)
            {
            }

            int rot = Helper.getHeading(Minecraft.getMinecraft().thePlayer);
            GL11.glRotated(90 * rot, 0, -1, 0);

            switch (rot)
            {
                case 1:
                    GL11.glTranslated(-1, 0, 0);
                    break;
                case 2:
                    GL11.glTranslated(-1, 0, 1);
                    break;
                case 3:
                    GL11.glTranslated(0, 0, 1);
                    break;
            }
        }

        synchronized (ishapes)
        {
            GL11.glLineWidth(1f);
            GL11.glColor3d(1, 1, 1);
            for (IShape ishape : ishapes)
            {
                ishape.render(tess);
            }

            if (renderSelectedShapeInCheckBox.isSelected())
            {
                GL11.glLineWidth(2f);
                GL11.glColor3d(0, 0, 1);
                for (int i : shapeList.getSelectedIndices())
                {
                    // Fuck event based bullshit that causes IndexOutOfBoundsExceptions & NullPointerExceptions out of nowhere.
                    if (i < ishapes.size())
                    {
                        IShape shape = ishapes.get(i);
                        if (shape != null) shape.render(tess);
                    }
                }
            }
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        // tess.renderingWorldRenderer = true;
        GL11.glPopMatrix();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Visual editor:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Type:");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel2.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label3, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(HTMLTextField, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label4, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label5, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel4, gbc);
        parseFromJsonButton = new JButton();
        parseFromJsonButton.setText("Parse from Json");
        parseFromJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(parseFromJsonButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(saveButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(updateJsonButton, gbc);
        testButton = new JButton();
        testButton.setText("Test");
        testButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(testButton, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel5, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(panel6, gbc);
        addShapeButton = new JButton();
        addShapeButton.setText("Add shape");
        addShapeButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(addShapeButton, gbc);
        removeShapeButton = new JButton();
        removeShapeButton.setText("Remove shape");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(removeShapeButton, gbc);
        importButton = new JButton();
        importButton.setText("Import!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel6.add(importButton, gbc);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel6.add(panel7, gbc);
        rotateBasedOnPlayerCheckBox = new JCheckBox();
        rotateBasedOnPlayerCheckBox.setText("Rotate based on player");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(rotateBasedOnPlayerCheckBox, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Base rotaion (0: South 1: West 2: North 3: East):");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 15, 0, 15);
        panel7.add(label6, gbc);
        baseRotation = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel7.add(baseRotation, gbc);
        final JLabel label7 = new JLabel();
        label7.setHorizontalAlignment(0);
        label7.setHorizontalTextPosition(0);
        label7.setText(">>> When flying the wireframe might be off by 1 block on the Y axis. <<<");
        label7.setToolTipText("This is because the client has a slightly different player position then the server. Can't do anything about that.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panel6.add(label7, gbc);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel6.add(panel8, gbc);
        renderShapesIngameCheckBox = new JCheckBox();
        renderShapesIngameCheckBox.setSelected(true);
        renderShapesIngameCheckBox.setText("Render shapes ingame");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel8.add(renderShapesIngameCheckBox, gbc);
        renderSelectedShapeInCheckBox = new JCheckBox();
        renderSelectedShapeInCheckBox.setSelected(true);
        renderSelectedShapeInCheckBox.setText("Render selected shape in color");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel8.add(renderSelectedShapeInCheckBox, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(scrollPane1, gbc);
        shapeList = new JList();
        shapeList.setToolTipText("Double click to edit!");
        scrollPane1.setViewportView(shapeList);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panel1;
    }
}
