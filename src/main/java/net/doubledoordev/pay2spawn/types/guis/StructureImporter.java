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

import com.google.gson.JsonArray;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.pay2spawn.Pay2Spawn;
import net.doubledoordev.pay2spawn.network.StructureImportMessage;
import net.doubledoordev.pay2spawn.util.Helper;
import net.doubledoordev.pay2spawn.util.JsonNBTHelper;
import net.doubledoordev.pay2spawn.util.shapes.IShape;
import net.doubledoordev.pay2spawn.util.shapes.PointI;
import net.doubledoordev.pay2spawn.util.shapes.Shapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Items;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

/**
 * @author Dries007
 */
public class StructureImporter
{
    final StructureImporter instance  = this;
    final HashSet<PointI>   points    = new HashSet<>();
    final HashSet<IShape>   selection = new HashSet<>();
    private final StructureTypeGui callback;
    private final JDialog          dialog;
    public        JPanel           panel1;
    public        JList<String>    pointList;
    public        JLabel           helpText;
    public        JComboBox<Mode>  modeComboBox;
    public        JButton          addFromSelectionButton;
    public        JButton          removeFromSelectionButton;
    public        JCheckBox        renderSelectionOnlyCheckBox;
    public        JButton          clearSelectionButton;
    public        JButton          importButton;
    public        JCheckBox        disableAlreadyImportedShapesCheckBox;
    PointI[] tempPointsArray = points.toArray(new PointI[points.size()]);
    Mode     mode            = Mode.SINGLE;
    PointI p1; // For BOX mode
    PointI p2; // For BOX mode

    public StructureImporter(final StructureTypeGui callback)
    {
        this.callback = callback;

        modeComboBox.setModel(new DefaultComboBoxModel<>(Mode.values()));
        pointList.setModel(new AbstractListModel<String>()
        {
            @Override
            public int getSize()
            {
                tempPointsArray = points.toArray(new PointI[points.size()]);
                return tempPointsArray.length;
            }

            @Override
            public String getElementAt(int index)
            {
                tempPointsArray = points.toArray(new PointI[points.size()]);
                return tempPointsArray[index].toString();
            }
        });
        modeComboBox.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                mode = (Mode) modeComboBox.getSelectedItem();
                helpText.setText(mode.helpText);
            }
        });
        addFromSelectionButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                synchronized (points)
                {
                    synchronized (selection)
                    {
                        for (IShape shape : selection) points.addAll(shape.getPoints());
                        if (p1 != null && p2 != null)
                        {
                            int minX = Math.min(p1.getX(), p2.getX());
                            int minY = Math.min(p1.getY(), p2.getY());
                            int minZ = Math.min(p1.getZ(), p2.getZ());
                            int diffX = Math.max(p1.getX(), p2.getX()) - minX;
                            int diffY = Math.max(p1.getY(), p2.getY()) - minY;
                            int diffZ = Math.max(p1.getZ(), p2.getZ()) - minZ;

                            for (int x = 0; x <= diffX; x++)
                            {
                                for (int y = 0; y <= diffY; y++)
                                {
                                    for (int z = 0; z <= diffZ; z++)
                                    {
                                        PointI p = new PointI(minX + x, minY + y, minZ + z);
                                        points.add(p);
                                    }
                                }
                            }
                        }
                        p1 = null;
                        p2 = null;
                        selection.clear();
                    }
                }
                pointList.updateUI();
            }
        });
        removeFromSelectionButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                synchronized (points)
                {
                    synchronized (selection)
                    {
                        for (IShape shape : selection) points.removeAll(shape.getPoints());
                        selection.clear();
                    }
                }
                pointList.updateUI();
            }
        });
        clearSelectionButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                synchronized (selection)
                {
                    selection.clear();
                }
                pointList.updateUI();
                updateBtns();
            }
        });
        importButton.addActionListener(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int x = Helper.round(Minecraft.getMinecraft().thePlayer.posX), y = Helper.round(Minecraft.getMinecraft().thePlayer.posY), z = Helper.round(Minecraft.getMinecraft().thePlayer.posZ);

                JsonArray jsonArray = new JsonArray();
                synchronized (points)
                {
                    for (PointI point : points) jsonArray.add(JsonNBTHelper.parseNBT(Shapes.storeShape(point.move(-x, -y, -z))));
                }
                Pay2Spawn.getSnw().sendToServer(new StructureImportMessage(x, y, z, jsonArray));

                dialog.dispose();
            }
        });
        disableAlreadyImportedShapesCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                callback.disabled = disableAlreadyImportedShapesCheckBox.isSelected();
            }
        });

        dialog = new JDialog();
        dialog.setContentPane(panel1);
        dialog.setModal(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle("Structure importer");
        dialog.setPreferredSize(new Dimension(600, 750));
        dialog.setSize(400, 750);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        new ForgeEventbusDialogThing(dialog, this);
        helpText.setText(mode.helpText);
        dialog.pack();
        dialog.setVisible(true);

        updateBtns();
    }

    @SubscribeEvent
    public void renderEvent(RenderWorldLastEvent event)
    {
        if (selection.size() == 0 && points.size() == 0 && p1 == null && p2 == null) return;

        Tessellator tess = Tessellator.instance;
        Tessellator.renderingWorldRenderer = false;

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glTranslated(-RenderManager.renderPosX, -RenderManager.renderPosY, 1 - RenderManager.renderPosZ);
        GL11.glScalef(1.0F, 1.0F, 1.0F);

        if (!renderSelectionOnlyCheckBox.isSelected())
        {
            synchronized (points)
            {
                GL11.glLineWidth(1f);
                GL11.glColor3d(0, 1, 0);
                for (PointI point : points) point.render(tess);
            }
        }

        synchronized (selection)
        {
            GL11.glLineWidth(2f);
            GL11.glColor3d(1, 0, 0);
            for (IShape point : selection) point.render(tess);
        }

        if (pointList.getSelectedIndex() != -1 && tempPointsArray.length < pointList.getSelectedIndex())
        {
            GL11.glColor3d(0, 0, 1);
            tempPointsArray[pointList.getSelectedIndex()].render(tess);
        }

        if (mode == Mode.BOX && p1 != null)
        {
            Helper.renderPoint(p1, tess, 246.0 / 255.0, 59.0 / 255.0, 246.0 / 255.0);
        }

        if (mode == Mode.BOX && p2 != null)
        {
            Helper.renderPoint(p2, tess, 59.0 / 243.0, 243.0 / 255.0, 246.0 / 255.0);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        // tess.renderingWorldRenderer = true;
        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void clickEvent(PlayerInteractEvent e)
    {
        if (e.entityPlayer.getHeldItem() == null || e.entityPlayer.getHeldItem().getItem() != Items.stick) return;
        e.setCanceled(true);

        if (e.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) click(Click.LEFT, e.x, e.y, e.z);
        else if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) click(Click.RIGHT, e.x, e.y, e.z);
    }

    private void click(Click click, int x, int y, int z)
    {
        switch (mode)
        {
            case SINGLE:
                synchronized (selection)
                {
                    if (click == Click.LEFT) selection.remove(new PointI(x, y, z));
                    if (click == Click.RIGHT) selection.add(new PointI(x, y, z));
                }
                break;
            case BOX:
                synchronized (selection)
                {
                    if (click == Click.LEFT) p1 = new PointI(x, y, z);
                    if (click == Click.RIGHT) p2 = new PointI(x, y, z);
                }
                break;
        }
        updateBtns();
    }

    private void updateBtns()
    {
        addFromSelectionButton.setEnabled(selection.size() != 0 || (p1 != null && p2 != null));
        removeFromSelectionButton.setEnabled(selection.size() != 0 || (p1 != null && p2 != null));
        clearSelectionButton.setEnabled(selection.size() != 0 || (p1 != null && p2 != null));
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
        final JScrollPane scrollPane1 = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        pointList = new JList();
        scrollPane1.setViewportView(pointList);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        modeComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(modeComboBox, gbc);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("A stick is used as the \"wand\" for this!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        panel2.add(label1, gbc);
        helpText = new JLabel();
        helpText.setHorizontalAlignment(0);
        helpText.setHorizontalTextPosition(0);
        helpText.setText("HELP TEXT");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        panel1.add(helpText, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        addFromSelectionButton = new JButton();
        addFromSelectionButton.setEnabled(false);
        addFromSelectionButton.setText("Add from selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(addFromSelectionButton, gbc);
        removeFromSelectionButton = new JButton();
        removeFromSelectionButton.setEnabled(false);
        removeFromSelectionButton.setText("Remove from selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(removeFromSelectionButton, gbc);
        renderSelectionOnlyCheckBox = new JCheckBox();
        renderSelectionOnlyCheckBox.setText("Render selection only");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(renderSelectionOnlyCheckBox, gbc);
        clearSelectionButton = new JButton();
        clearSelectionButton.setEnabled(false);
        clearSelectionButton.setText("Clear selection");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(clearSelectionButton, gbc);
        disableAlreadyImportedShapesCheckBox = new JCheckBox();
        disableAlreadyImportedShapesCheckBox.setText("Disable already imported shapes ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(disableAlreadyImportedShapesCheckBox, gbc);
        importButton = new JButton();
        importButton.setText("Import relative to player!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(importButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panel1;
    }

    enum Mode
    {
        SINGLE("Single block mode", "Right click => add block, Left click => remove block"),
        BOX("Box mode", "Right click => Point 1, Left click => Point 2");

        public final String name;
        public final String helpText;

        Mode(String name, String helpText)
        {
            this.name = name;
            this.helpText = helpText;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    enum Click
    {
        LEFT, RIGHT
    }
}
