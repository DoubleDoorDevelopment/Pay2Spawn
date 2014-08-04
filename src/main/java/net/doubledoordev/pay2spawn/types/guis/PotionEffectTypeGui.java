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

import net.doubledoordev.pay2spawn.configurator.Configurator;
import net.doubledoordev.pay2spawn.network.TestMessage;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.types.PotionEffectType.*;
import static net.doubledoordev.pay2spawn.util.Constants.CUSTOMHTML;
import static net.doubledoordev.pay2spawn.util.Constants.GSON;
import static net.doubledoordev.pay2spawn.util.Constants.JSON_PARSER;

/**
 * @author Dries007
 */
public class PotionEffectTypeGui extends HelperGuiBase
{
    public JTextField        amplifierTextField;
    public JScrollPane       scrollPane;
    public JTextPane         jsonPane;
    public JButton           parseFromJsonButton;
    public JButton           saveButton;
    public JButton           updateJsonButton;
    public JButton           testButton;
    public JPanel            panel1;
    public JTextField        durationTextField;
    public JComboBox<String> potionEffectComboBox;
    private JTextField HTMLTextField;

    public PotionEffectTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        ArrayList<String> list = new ArrayList<>();
        list.addAll(POTIONS.keySet());
        Collections.sort(list);
        potionEffectComboBox.setModel(new DefaultComboBoxModel<>(list.toArray(new String[list.size()])));

        makeAndOpen();
    }

    @Override
    public void readJson()
    {
        String id = readValue(ID_KEY, data);
        try
        {
            int i = Integer.parseInt(id);
            if (POTIONS.inverse().containsKey(i)) id = POTIONS.inverse().get(i);
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
        potionEffectComboBox.setSelectedItem(id);
        amplifierTextField.setText(readValue(AMPLIFIER_KEY, data));
        durationTextField.setText(readValue(DURATION_KEY, data));
        HTMLTextField.setText(readValue(CUSTOMHTML, data));

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        String id = potionEffectComboBox.getSelectedItem().toString();
        if (POTIONS.containsKey(id)) id = "" + POTIONS.get(id);

        storeValue(ID_KEY, data, id);
        storeValue(AMPLIFIER_KEY, data, amplifierTextField.getText());
        storeValue(DURATION_KEY, data, durationTextField.getText());
        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

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
        label3.setText("Amplifier:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("BYTE");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        panel2.add(label4, gbc);
        amplifierTextField = new JTextField();
        amplifierTextField.setToolTipText("0 means lvl 1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(amplifierTextField, gbc);
        durationTextField = new JTextField();
        durationTextField.setToolTipText("x 20 for seconds!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(durationTextField, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Duration (ticks):");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label5, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        panel2.add(label6, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Potion effect:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel2.add(label7, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("BYTE");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        panel2.add(label8, gbc);
        potionEffectComboBox = new JComboBox();
        potionEffectComboBox.setEditable(true);
        potionEffectComboBox.setToolTipText("Look, a build in list!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(potionEffectComboBox, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label9, gbc);
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
        gbc.gridy = 2;
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
        label3.setLabelFor(amplifierTextField);
        label5.setLabelFor(durationTextField);
        label7.setLabelFor(potionEffectComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
