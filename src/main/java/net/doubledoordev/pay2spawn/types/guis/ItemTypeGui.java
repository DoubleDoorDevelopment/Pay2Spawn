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
import net.doubledoordev.pay2spawn.network.NbtRequestMessage;
import net.doubledoordev.pay2spawn.network.TestMessage;
import net.doubledoordev.pay2spawn.util.IIHasCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import static net.doubledoordev.pay2spawn.types.ItemType.SLOT_KEY;
import static net.doubledoordev.pay2spawn.util.Constants.*;

/**
 * @author Dries007
 */
public class ItemTypeGui extends HelperGuiBase implements IIHasCallback
{
    public  JScrollPane scrollPane;
    public  JTextPane   jsonPane;
    public  JButton     parseFromJsonButton;
    public  JButton     saveButton;
    public  JButton     updateJsonButton;
    public  JButton     testButton;
    public  JButton     importItemYouAreButton;
    public  JPanel      pane1;
    public  JTextField  HTMLTextField;
    private JTextField  slotTextField;
    private ItemTypeGui instance = this;

    public ItemTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        makeAndOpen();
    }

    @Override
    public void readJson()
    {
        if (data.has(SLOT_KEY)) slotTextField.setText(readValue(SLOT_KEY, data));

        HTMLTextField.setText(readValue(CUSTOMHTML, data));

        jsonPane.setText(GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        storeValue(SLOT_KEY, data, slotTextField.getText());

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
        importItemYouAreButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NbtRequestMessage.requestItem(instance);
            }
        });
    }

    @Override
    public JPanel getPanel()
    {
        return pane1;
    }

    @Override
    public void callback(Object... data)
    {
        this.data = JSON_PARSER.parse((String) data[0]).getAsJsonObject();
        updateJson();
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
        importItemYouAreButton = new JButton();
        importItemYouAreButton.setText("Import item you are holding ingame!");
        importItemYouAreButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(importItemYouAreButton, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Custom HTML:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(label1, gbc);
        HTMLTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(HTMLTextField, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("STRING");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        slotTextField = new JTextField();
        slotTextField.setText("-1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(slotTextField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Slot");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label3, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("INT");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label4, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pane1.add(panel2, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label5, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        pane1.add(panel3, gbc);
        parseFromJsonButton = new JButton();
        parseFromJsonButton.setText("Parse from Json");
        parseFromJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(parseFromJsonButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(saveButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(updateJsonButton, gbc);
        testButton = new JButton();
        testButton.setText("Test");
        testButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(testButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return pane1;
    }
}
