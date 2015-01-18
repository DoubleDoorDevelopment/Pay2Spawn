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

import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.util.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.types.FireworksType.*;

/**
 * @author Dries007
 */
public class ExplosionGui extends HelperGuiBase
{
    public  JButton           saveButton;
    public  JComboBox<String> typeComboBox;
    public  JRadioButton      flickerRadioButton;
    public  JRadioButton      noFlickerRadioButton;
    public  JRadioButton      randomFlickerRadioButton;
    public  JRadioButton      trailRadioButton;
    public  JRadioButton      noTrailRadioButton;
    public  JRadioButton      randomTrailRadioButton;
    public  JTextField        colorsTextField;
    public  JPanel            panel1;
    private JTextField        fadecolorsTextField;
    FireworksTypeGui callback;

    public ExplosionGui(final int index, final JsonObject jsonObject, final FireworksTypeGui callback, final HashMap<String, String> typemap)
    {
        super(index, "Baboom!", jsonObject, typemap);
        this.callback = callback;

        typeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"0: Small", "1: Large", "2: Star shaped", "3: Creeper", "4: Burst", "Random!"}));

        setupListeners();
        readJson();

        dialog = new JDialog();
        dialog.setContentPane(getPanel());
        dialog.setModal(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setTitle(name);
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    public void readJson()
    {
        typeComboBox.setSelectedItem(readValue(TYPE_KEY, data));

        colorsTextField.setText(readValue(COLORS_KEY, data));
        fadecolorsTextField.setText(readValue(FADECOLORS_KEY, data));

        String flicker = readValue(FLICKER_KEY, data);
        noFlickerRadioButton.setSelected(flicker.equals(FALSE_BYTE) || flicker.equals(""));
        flickerRadioButton.setSelected(flicker.equals(TRUE_BYTE));
        randomFlickerRadioButton.setSelected(flicker.startsWith(RANDOM_BOOLEAN));

        String trail = readValue(TRAIL_KEY, data);
        noTrailRadioButton.setSelected(trail.equals(FALSE_BYTE) || trail.equals(""));
        trailRadioButton.setSelected(trail.equals(TRUE_BYTE));
        randomTrailRadioButton.setSelected(trail.startsWith(RANDOM_BOOLEAN));
    }

    @Override
    public void updateJson()
    {
        String type = typeComboBox.getSelectedItem().toString();
        if (Helper.isDouble(type)) storeValue(TYPE_KEY, data, type);
        else if (type.contains(":")) storeValue(TYPE_KEY, data, type.substring(0, type.indexOf(":")));
        else storeValue(TYPE_KEY, data, "$random(0,5)");

        storeValue(COLORS_KEY, data, colorsTextField.getText());
        storeValue(FADECOLORS_KEY, data, fadecolorsTextField.getText());

        if (flickerRadioButton.isSelected()) storeValue(FLICKER_KEY, data, TRUE_BYTE);
        if (noFlickerRadioButton.isSelected()) storeValue(FLICKER_KEY, data, FALSE_BYTE);
        if (randomFlickerRadioButton.isSelected()) storeValue(FLICKER_KEY, data, RANDOM_BOOLEAN);

        if (trailRadioButton.isSelected()) storeValue(TRAIL_KEY, data, TRUE_BYTE);
        if (noTrailRadioButton.isSelected()) storeValue(TRAIL_KEY, data, FALSE_BYTE);
        if (randomTrailRadioButton.isSelected()) storeValue(TRAIL_KEY, data, RANDOM_BOOLEAN);
    }

    @Override
    public void setupListeners()
    {
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                callback.callback(rewardID, data);
                dialog.dispose();
            }
        });
    }

    @Override
    public JPanel getPanel()
    {
        return panel1;
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
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Type:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("BYTE");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label2, gbc);
        typeComboBox = new JComboBox();
        typeComboBox.setEditable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(typeComboBox, gbc);
        flickerRadioButton = new JRadioButton();
        flickerRadioButton.setText("Flicker");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(flickerRadioButton, gbc);
        noFlickerRadioButton = new JRadioButton();
        noFlickerRadioButton.setText("No flicker");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(noFlickerRadioButton, gbc);
        randomFlickerRadioButton = new JRadioButton();
        randomFlickerRadioButton.setText("Random flicker");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomFlickerRadioButton, gbc);
        trailRadioButton = new JRadioButton();
        trailRadioButton.setText("Trail");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(trailRadioButton, gbc);
        noTrailRadioButton = new JRadioButton();
        noTrailRadioButton.setText("No trail");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(noTrailRadioButton, gbc);
        randomTrailRadioButton = new JRadioButton();
        randomTrailRadioButton.setText("Random trail");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(randomTrailRadioButton, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Colors");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label3, gbc);
        colorsTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(colorsTextField, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("INT ARRAY");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setHorizontalAlignment(0);
        label5.setHorizontalTextPosition(0);
        label5.setText("Use comma seperated values, use java color codes. See help file.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("FadeColors");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label6, gbc);
        fadecolorsTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(fadecolorsTextField, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("INT ARRAY");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label7, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(saveButton, gbc);
        label1.setLabelFor(typeComboBox);
        label3.setLabelFor(colorsTextField);
        label6.setLabelFor(colorsTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(flickerRadioButton);
        buttonGroup.add(noFlickerRadioButton);
        buttonGroup.add(randomFlickerRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(trailRadioButton);
        buttonGroup.add(noTrailRadioButton);
        buttonGroup.add(randomTrailRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panel1;
    }
}
