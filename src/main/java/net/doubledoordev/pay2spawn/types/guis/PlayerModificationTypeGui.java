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
import com.google.gson.JsonObject;
import net.doubledoordev.pay2spawn.configurator.Configurator;
import net.doubledoordev.pay2spawn.network.TestMessage;
import net.doubledoordev.pay2spawn.util.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.types.PlayerModificationType.*;
import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class PlayerModificationTypeGui extends HelperGuiBase
{
    public JScrollPane  scrollPane;
    public JTextPane    jsonPane;
    public JButton      parseFromJsonButton;
    public JButton      saveButton;
    public JButton      updateJsonButton;
    public JButton      testButton;
    public JPanel       pane1;
    public JRadioButton healthRadioButton;
    public JRadioButton hungerRadioButton;
    public JRadioButton saturationRadioButton;
    public JRadioButton XPRadioButton;
    public JRadioButton XPLevelsRadioButton;
    public JRadioButton flightRadioButton;
    public JRadioButton invulnerabilityRadioButton;
    public JRadioButton setRadioButton;
    public JRadioButton addRadioButton;
    public JRadioButton subtractRadioButton;
    public JRadioButton enableRadioButton;
    public JRadioButton disableRadioButton;
    public JTextField   amountTextField;
    public JTextField   HTMLTextField;

    public PlayerModificationTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        makeAndOpen();
    }

    @Override
    public void readJson()
    {
        String type = readValue(TYPE_KEY, data);
        if (Helper.isInt(type))
        {
            checkOperation(Integer.parseInt(type));
            switch (Integer.parseInt(type))
            {
                case 0:
                    healthRadioButton.setSelected(true);
                    break;
                case 1:
                    hungerRadioButton.setSelected(true);
                    break;
                case 2:
                    XPRadioButton.setSelected(true);
                    break;
                case 3:
                    XPLevelsRadioButton.setSelected(true);
                    break;
                case 4:
                    flightRadioButton.setSelected(true);
                    break;
                case 5:
                    invulnerabilityRadioButton.setSelected(true);
                    break;
            }
        }

        String op = readValue(OPERATION_KEY, data);
        if (Helper.isInt(op))
        {
            switch (Integer.parseInt(op))
            {
                case 0:
                    setRadioButton.setSelected(true);
                    break;
                case 1:
                    addRadioButton.setSelected(true);
                    break;
                case 2:
                    subtractRadioButton.setSelected(true);
                    break;
                case 3:
                    enableRadioButton.setSelected(true);
                    break;
                case 4:
                    disableRadioButton.setSelected(true);
                    break;
            }
        }

        HTMLTextField.setText(readValue(CUSTOMHTML, data));
        amountTextField.setText(readValue(AMOUNT_KEY, data));

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        if (healthRadioButton.isSelected()) storeValue(TYPE_KEY, data, FALSE_BYTE);
        if (hungerRadioButton.isSelected()) storeValue(TYPE_KEY, data, TRUE_BYTE);
        if (saturationRadioButton.isSelected()) storeValue(TYPE_KEY, data, "2");
        if (XPRadioButton.isSelected()) storeValue(TYPE_KEY, data, "3");
        if (XPLevelsRadioButton.isSelected()) storeValue(TYPE_KEY, data, "4");
        if (flightRadioButton.isSelected()) storeValue(TYPE_KEY, data, "5");
        if (invulnerabilityRadioButton.isSelected()) storeValue(TYPE_KEY, data, "6");

        if (setRadioButton.isSelected()) storeValue(OPERATION_KEY, data, SET + "");
        if (addRadioButton.isSelected()) storeValue(OPERATION_KEY, data, ADD + "");
        if (subtractRadioButton.isSelected()) storeValue(OPERATION_KEY, data, SUBTRACT + "");
        if (enableRadioButton.isSelected()) storeValue(OPERATION_KEY, data, ENABLE + "");
        if (disableRadioButton.isSelected()) storeValue(OPERATION_KEY, data, DISABLE + "");

        if (!Strings.isNullOrEmpty(HTMLTextField.getText())) storeValue(CUSTOMHTML, data, HTMLTextField.getText());

        storeValue(AMOUNT_KEY, data, amountTextField.getText());

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

        healthRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(0);
            }
        });
        hungerRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(1);
            }
        });
        saturationRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(2);
            }
        });
        XPRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(3);
            }
        });
        XPLevelsRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(4);
            }
        });
        flightRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(5);
            }
        });
        invulnerabilityRadioButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                checkOperation(6);
            }
        });
    }

    @Override
    public JPanel getPanel()
    {
        return pane1;
    }

    public void checkOperation(int i)
    {
        boolean timable = Type.values()[i].isTimable();

        setRadioButton.setEnabled(!timable && Type.values()[i] !=  Type.XP);
        addRadioButton.setEnabled(!timable);
        subtractRadioButton.setEnabled(!timable);
        enableRadioButton.setEnabled(timable);
        disableRadioButton.setEnabled(timable);
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
        pane1 = new JPanel();
        pane1.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pane1.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        panel2.setBorder(BorderFactory.createTitledBorder("Type"));
        healthRadioButton = new JRadioButton();
        healthRadioButton.setText("Health");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(healthRadioButton, gbc);
        hungerRadioButton = new JRadioButton();
        hungerRadioButton.setText("Hunger");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(hungerRadioButton, gbc);
        saturationRadioButton = new JRadioButton();
        saturationRadioButton.setText("Saturation");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(saturationRadioButton, gbc);
        XPRadioButton = new JRadioButton();
        XPRadioButton.setText("XP");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(XPRadioButton, gbc);
        XPLevelsRadioButton = new JRadioButton();
        XPLevelsRadioButton.setText("XP Levels");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(XPLevelsRadioButton, gbc);
        flightRadioButton = new JRadioButton();
        flightRadioButton.setText("Flight");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(flightRadioButton, gbc);
        invulnerabilityRadioButton = new JRadioButton();
        invulnerabilityRadioButton.setText("Invulnerability");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(invulnerabilityRadioButton, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder("Operation"));
        setRadioButton = new JRadioButton();
        setRadioButton.setText("Set");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(setRadioButton, gbc);
        addRadioButton = new JRadioButton();
        addRadioButton.setText("Add");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(addRadioButton, gbc);
        subtractRadioButton = new JRadioButton();
        subtractRadioButton.setText("Subtract");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(subtractRadioButton, gbc);
        enableRadioButton = new JRadioButton();
        enableRadioButton.setText("Enable");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(enableRadioButton, gbc);
        disableRadioButton = new JRadioButton();
        disableRadioButton.setText("Disable");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(disableRadioButton, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Amount:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        amountTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(amountTextField, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("FLOAT");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment(0);
        label3.setText("If enable or disable, amount is time in seconds. If empty, no time limit.");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(label4, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(HTMLTextField, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label5, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pane1.add(panel4, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(label6, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel4.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pane1.add(panel5, gbc);
        parseFromJsonButton = new JButton();
        parseFromJsonButton.setText("Parse from Json");
        parseFromJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(parseFromJsonButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(saveButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(updateJsonButton, gbc);
        testButton = new JButton();
        testButton.setText("Test");
        testButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel5.add(testButton, gbc);
        label1.setLabelFor(amountTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(healthRadioButton);
        buttonGroup.add(hungerRadioButton);
        buttonGroup.add(saturationRadioButton);
        buttonGroup.add(XPRadioButton);
        buttonGroup.add(XPLevelsRadioButton);
        buttonGroup.add(flightRadioButton);
        buttonGroup.add(invulnerabilityRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(setRadioButton);
        buttonGroup.add(addRadioButton);
        buttonGroup.add(subtractRadioButton);
        buttonGroup.add(enableRadioButton);
        buttonGroup.add(disableRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return pane1;
    }
}
