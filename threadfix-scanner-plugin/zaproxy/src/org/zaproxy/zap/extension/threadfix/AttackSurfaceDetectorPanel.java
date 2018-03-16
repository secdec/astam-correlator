/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.threadfix;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.*;

import com.denimgroup.threadfix.plugin.zap.dialog.OptionsDialog;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.extension.history.HistoryFilter;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;

/**
 * This class creates the Spider AJAX Panel where the found URLs are displayed
 * It has a button to stop the crawler and another one to open the options.
 *
 */
public class AttackSurfaceDetectorPanel extends AbstractPanel{
    private static final long serialVersionUID = 1L;

    private javax.swing.JScrollPane scrollLog = null;
    private javax.swing.JPanel attackSurfaceDetectorPanel = null;
    private javax.swing.JToolBar panelToolbar = null;
    private JLabel filterStatus = null;
    private JButton stopScanButton;
    private JButton startScanButton;
    private JButton optionsButton = null;
    private ViewDelegate view = null;


    /**
     * This is the default constructor
     */
    public AttackSurfaceDetectorPanel(ViewDelegate view) {
        super();
        super.setName("Attack Surface Detector");
        this.view = view;
        initialize();
    }

    /**
     * This method initializes this class and its attributes
     *
     */
    private  void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(600, 200);

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new java.awt.GridBagLayout());
        basePanel.setName("Attack Surface Detector");


        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weightx = 1.0D;
        gridBagConstraints1.insets = new java.awt.Insets(4,4,4,4);
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;



        basePanel.add(buildToolBar(),gridBagConstraints1);

        JScrollPane scrollPane = new JScrollPane(new JTextArea("SCROLLLLL"));


        //maybe add table to a panel and then add panel to scrollPane?


        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        gridBagConstraints2.insets = new java.awt.Insets(4,4,4,4);
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        basePanel.add(scrollPane,gridBagConstraints2);


        this.add(basePanel, java.awt.BorderLayout.CENTER);



    }




    private javax.swing.JToolBar buildToolBar()
    {

        panelToolbar = new javax.swing.JToolBar();
        panelToolbar.setLayout(new java.awt.GridBagLayout());
        panelToolbar.setEnabled(true);
        panelToolbar.setFloatable(false);
        panelToolbar.setRollover(true);
        panelToolbar.setPreferredSize(new java.awt.Dimension(1000,30));
        panelToolbar.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
        panelToolbar.setName("Attack Surface Detector");

        JButton importButton = new JButton("Import endpoints from source");
        JButton selectedButton = new JButton("View Selected");
        selectedButton.setEnabled(false);
        JButton optionsButton = new JButton("Options");

        optionsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean shouldContinue = OptionsDialog.show(view);
            }
        });


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(4,4,4,4);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;

        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty= 1.0;

        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints1.anchor = GridBagConstraints.WEST;

        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 3;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.insets = new java.awt.Insets(4,4,4,4);
        gridBagConstraints4.anchor = GridBagConstraints.WEST;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.weighty= 1.0;

        buttonPanel.add(importButton, gridBagConstraints1);
        buttonPanel.add(selectedButton, gridBagConstraints2);
        buttonPanel.add(optionsButton, gridBagConstraints3);

        GridBagConstraints toolConstraints1 = new GridBagConstraints();
        toolConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        toolConstraints1.insets = new java.awt.Insets(4,4,4,4);
        toolConstraints1.anchor = GridBagConstraints.WEST;
        toolConstraints1.weightx = 1.0;
        toolConstraints1.weighty= 1.0;

        panelToolbar.add(buttonPanel, toolConstraints1);

        return panelToolbar;
    }

}
