////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////

package org.zaproxy.zap.extension.threadfix;

import com.denimgroup.threadfix.plugin.zap.ThreadFixApi;
import com.denimgroup.threadfix.plugin.zap.action.LocalEndpointsAction;
import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.zaproxy.zap.extension.api.API;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class ThreadFixExtension extends ExtensionAdaptor {

    private LocalEndpointsAction localEndpointsAction = null;
    private ResourceBundle messages = null;
    private AbstractPanel statusPanel;
    JTabbedPane tabbedPane;
    JCheckBox autoSpiderField;

    private static final Logger logger = Logger.getLogger(ThreadFixExtension.class);

    static {
       logger.info("Loading Class");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     *
     */
    public ThreadFixExtension() {
        super();
        logger.info("calling constructor");
        initialize();
        logger.info("No-arg Constructor");
        this.setEnabled(true);
    }

    /**
     * @param name
     */
    public ThreadFixExtension(String name) {
        super(name);
        logger.info("1-arg Constructor");
    }

    /**
     * This method initializes this
     *
     */
    private void initialize() {
       logger.info("Initialize");
        this.setName("CodePT");
        // Load extension specific language files - these are held in the extension jar
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
       logger.info("Hook");
        super.hook(extensionHook);

        if (getView() != null) {
            // Register our top menu item, as long as we're not running as a daemon
            // Use one of the other methods to add to a different menu list
            extensionHook.getHookMenu().addToolsMenuItem(getLocalEndpointsAction());
            extensionHook.getHookView().addStatusPanel(getStatusPanel());

        }

        API.getInstance().registerApiImplementor(new ThreadFixApi(this));
    }
    private AbstractPanel getStatusPanel() {
        if (statusPanel == null)
        {
            statusPanel = new AbstractPanel();
            statusPanel.setLayout(new GridBagLayout());
            Insets statusPanelInsets = new Insets(10, 10, 10, 10);
            GridBagConstraints statusPanelConstraints = new GridBagConstraints();
            statusPanelConstraints.gridx = 0;
            statusPanelConstraints.gridy = 0;
            statusPanelConstraints.ipadx = 5;
            statusPanelConstraints.ipady = 5;
            statusPanelConstraints.insets = statusPanelInsets;
            statusPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
            statusPanelConstraints.fill = GridBagConstraints.BOTH;
            statusPanelConstraints.weightx = 1.0;
            statusPanelConstraints.weighty = 1.0;


            statusPanel.setName("CodePT");
            tabbedPane = new JTabbedPane();
            JPanel optionsPanel = buildOptionsPanel();
            JScrollPane optionsScrollPane = new JScrollPane(optionsPanel);
            tabbedPane.addTab("Options", optionsScrollPane);
            statusPanel.add(tabbedPane, statusPanelConstraints);
        }

        return statusPanel;
    }

    private JPanel buildOptionsPanel()
    {
        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());
        Insets optionsPanelInsets = new Insets(10, 10, 10, 10);
        int yPosition = 0;

        JPanel autoOptionsPanel = buildAutoOptionsPanel();
        GridBagConstraints autoOptionsPanelConstraints = new GridBagConstraints();
        autoOptionsPanelConstraints.gridx = 0;
        autoOptionsPanelConstraints.gridy = yPosition++;
        autoOptionsPanelConstraints.ipadx = 5;
        autoOptionsPanelConstraints.ipady = 5;
        autoOptionsPanelConstraints.insets = optionsPanelInsets;
        autoOptionsPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
        autoOptionsPanelConstraints.fill = GridBagConstraints.BOTH;
        autoOptionsPanelConstraints.weightx = 1.0;
        autoOptionsPanelConstraints.weighty = 1.0;
        optionsPanel.add(autoOptionsPanel, autoOptionsPanelConstraints);

        return  optionsPanel;

    }

    private JPanel buildAutoOptionsPanel() {
        final JPanel autoOptionsPanel = new JPanel();
        autoOptionsPanel.setLayout(new GridBagLayout());
        int yPosition = 0;

        final JLabel autoOptionsPanelTitle = addPanelTitleToGridBagLayout("Code PT Plugin Behavior", autoOptionsPanel, yPosition++);
        ActionListener applicationCheckBoxSpiderActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               ZapPropertiesManager.INSTANCE.setAutoSpider(autoSpiderField.isSelected());

            }
        };
        autoSpiderField = addCheckBoxToGridBagLayout("Automatically start spider after importing endpoints: ", autoOptionsPanel, yPosition++, applicationCheckBoxSpiderActionListener);
        autoSpiderField.setSelected(ZapPropertiesManager.INSTANCE.getAutoSpider());
        return autoOptionsPanel;
    }
    public LocalEndpointsAction getLocalEndpointsAction() {
        logger.info("Getting menu");
        if (localEndpointsAction == null) {
            localEndpointsAction = new LocalEndpointsAction(getView(), getModel());
        }
        return localEndpointsAction;
    }

    public String getMessageString(String key) {
        return messages.getString(key);
    }
    @Override
    public String getAuthor() {
        logger.info("Getting Author");
        return "Secure Decisons";
    }

    @Override
    public String getDescription() {
        logger.info("Getting Description");
        return "Source Code Analysis";
    }

    @Override
    public URL getURL() {
        logger.info("Getting URL");
        try {
            return new URL("https://github.com/denimgroup/threadfix/wiki/Zap-Plugin");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private JTextField addTextFieldToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, String propertyKey) {
        return addTextFieldToGridBagLayout(labelText, gridBagContainer, yPosition, propertyKey, null, null);
    }

    private JTextField addTextFieldToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, String propertyKey, Runnable threadFixPropertyFieldListenerRunnable) {
        return addTextFieldToGridBagLayout(labelText, gridBagContainer, yPosition, propertyKey, threadFixPropertyFieldListenerRunnable, null);
    }

    private JTextField addTextFieldToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, String propertyKey, JButton button) {
        return addTextFieldToGridBagLayout(labelText, gridBagContainer, yPosition, propertyKey, null, button);
    }

    private JTextField addTextFieldToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, String propertyKey, Runnable threadFixPropertyFieldListenerRunnable, JButton button) {
        JLabel textFieldLabel = new JLabel(labelText);
        textFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagContainer.add(textFieldLabel, gridBagConstraints);

        JTextField textField = new JTextField(40);
        gridBagConstraints = new GridBagConstraints();
        if (button == null) {
            gridBagConstraints.gridwidth = 2;
        } else {
            gridBagConstraints.gridwidth = 1;
        }
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagContainer.add(textField, gridBagConstraints);

        if (button != null) {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = yPosition;
            gridBagConstraints.ipadx = 5;
            gridBagConstraints.ipady = 5;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
            gridBagContainer.add(button, gridBagConstraints);
        }

        return textField;
    }


    private JComboBox addComboBoxToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, ActionListener actionListener) {
        return addComboBoxToGridBagLayout(labelText, gridBagContainer, yPosition, actionListener, null);
    }

    private JComboBox addComboBoxToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, ActionListener actionListener, JButton button) {
        JLabel textFieldLabel = new JLabel(labelText);
        textFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagContainer.add(textFieldLabel, gridBagConstraints);

        JComboBox comboBox = new JComboBox();
        comboBox.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        if (button == null) {
            gridBagConstraints.gridwidth = 2;
        } else {
            gridBagConstraints.gridwidth = 1;
        }
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagContainer.add(comboBox, gridBagConstraints);

        if (button != null) {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = yPosition;
            gridBagConstraints.ipadx = 5;
            gridBagConstraints.ipady = 5;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
            gridBagContainer.add(button, gridBagConstraints);
        }

        comboBox.addActionListener(actionListener);

        return comboBox;
    }

    private JCheckBox addCheckBoxToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, ActionListener actionListener) {
        return addCheckBoxToGridBagLayout(labelText, gridBagContainer, yPosition, actionListener, null);
    }

    private JCheckBox addCheckBoxToGridBagLayout(JLabel label, Container gridBagContainer, int yPosition, ActionListener actionListener) {
        return addCheckBoxToGridBagLayout(label, gridBagContainer, yPosition, actionListener, null);
    }

    private JCheckBox addCheckBoxToGridBagLayout(String labelText, Container gridBagContainer, int yPosition, ActionListener actionListener, JButton button) {
        JLabel textFieldLabel = new JLabel(labelText);

        textFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagContainer.add(textFieldLabel, gridBagConstraints);

        JCheckBox checkBox = new JCheckBox();
        gridBagConstraints = new GridBagConstraints();
        if (button == null) {
            gridBagConstraints.gridwidth = 2;
        } else {
            gridBagConstraints.gridwidth = 1;
        }
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagContainer.add(checkBox, gridBagConstraints);

        if (button != null) {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = yPosition;
            gridBagConstraints.ipadx = 5;
            gridBagConstraints.ipady = 5;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
            gridBagContainer.add(button, gridBagConstraints);
        }

        checkBox.addActionListener(actionListener);

        return checkBox;
    }

    private JCheckBox addCheckBoxToGridBagLayout(JLabel textFieldLabel, Container gridBagContainer, int yPosition, ActionListener actionListener, JButton button) {

        textFieldLabel.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagContainer.add(textFieldLabel, gridBagConstraints);

        JCheckBox checkBox = new JCheckBox();
        gridBagConstraints = new GridBagConstraints();
        if (button == null) {
            gridBagConstraints.gridwidth = 2;
        } else {
            gridBagConstraints.gridwidth = 1;
        }
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagContainer.add(checkBox, gridBagConstraints);

        if (button != null) {
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = yPosition;
            gridBagConstraints.ipadx = 5;
            gridBagConstraints.ipady = 5;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
            gridBagContainer.add(button, gridBagConstraints);
        }

        checkBox.addActionListener(actionListener);

        return checkBox;
    }

    private JLabel addPanelTitleToGridBagLayout(String titleText, Container gridBagContainer, int yPosition) {
        final JLabel panelTitle = new JLabel(titleText, JLabel.LEFT);
        panelTitle.setForeground(new Color(236, 136, 0));
        Font font = panelTitle.getFont();
        panelTitle.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 4));
        panelTitle.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagContainer.add(panelTitle, gridBagConstraints);
        return panelTitle;
    }

    private JLabel addPanelLabelToGridBagLayout(String titleText, Container gridBagContainer, int yPosition) {
        final JLabel panelTitle = new JLabel(titleText);
        panelTitle.setForeground(new Color(236, 136, 0));
        Font font = panelTitle.getFont();
        panelTitle.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize()));
        panelTitle.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagContainer.add(panelTitle, gridBagConstraints);
        return panelTitle;
    }

    private JLabel addPanelDescriptionToGridBagLayout(String descriptionText, Container gridBagContainer, int yPosition) {
        final JLabel panelDescription = new JLabel(descriptionText);
        panelDescription.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = yPosition;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagContainer.add(panelDescription, gridBagConstraints);
        return panelDescription;
    }

}