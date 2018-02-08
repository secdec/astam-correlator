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

package burp.dialog;

import burp.extention.BurpPropertiesManager;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class UrlDialog {

    public static String show(Component view) {
        BurpPropertiesManager burpPropertiesManager = BurpPropertiesManager.getBurpPropertiesManager();
        String targetHost = burpPropertiesManager.getTargetHost();
        String targetPort = burpPropertiesManager.getTargetPort();
        String targetPath = burpPropertiesManager.getTargetPath();
        String targetProto = new String();
        if(burpPropertiesManager.getUseHttps())
            targetProto = "https://";
        else
            targetProto = "http://";
        //String targetUrl = burpPropertiesManager.getTargetUrl();
        //if ((targetUrl != null) && !targetUrl.trim().isEmpty() &&(!targetUrl.trim().equalsIgnoreCase("http://") || !targetUrl.trim().equalsIgnoreCase("https://"))) {
            //return targetUrl;
        //}

        if(targetHost != null && !targetHost.trim().isEmpty() && targetPort != null && !targetPort.trim().isEmpty())
        {
            if(targetPath != null && !targetPath.trim().isEmpty())
                return targetProto+ targetHost  + ":" + targetPort + "/" + targetPath;
            else
                return targetProto+ targetHost  + ":" + targetPort;
        }


        JTextField hostField = new JTextField(40);
        JTextField portField = new JTextField(40);
        JTextField pathField = new JTextField(40);
        JCheckBox httpsField = new JCheckBox();

        PlainDocument portDoc = (PlainDocument)portField.getDocument();
        portDoc.setDocumentFilter(new PortFilter());


        GridBagLayout experimentLayout = new GridBagLayout();
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridwidth = 1;
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 0;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints textBoxConstraints = new GridBagConstraints();
        textBoxConstraints.gridwidth = 4;
        textBoxConstraints.gridx = 1;
        textBoxConstraints.gridy = 0;
        textBoxConstraints.fill = GridBagConstraints.HORIZONTAL;

        JPanel myPanel = new JPanel();
        myPanel.setLayout(experimentLayout);
        myPanel.add(new JLabel("Host"), labelConstraints);
        myPanel.add(hostField, textBoxConstraints);

        labelConstraints = new GridBagConstraints();
        labelConstraints.gridwidth = 1;
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 1;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        textBoxConstraints = new GridBagConstraints();
        textBoxConstraints.gridwidth = 4;
        textBoxConstraints.gridx = 1;
        textBoxConstraints.gridy = 1;
        textBoxConstraints.fill = GridBagConstraints.HORIZONTAL;

        myPanel.add(new JLabel("Port"), labelConstraints);
        myPanel.add(portField, textBoxConstraints);

        labelConstraints = new GridBagConstraints();
        labelConstraints.gridwidth = 1;
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 2;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        textBoxConstraints = new GridBagConstraints();
        textBoxConstraints.gridwidth = 4;
        textBoxConstraints.gridx = 1;
        textBoxConstraints.gridy = 2;
        textBoxConstraints.fill = GridBagConstraints.HORIZONTAL;

        myPanel.add(new JLabel("Path (optional)"), labelConstraints);
        myPanel.add(pathField, textBoxConstraints);

        labelConstraints = new GridBagConstraints();
        labelConstraints.gridwidth = 1;
        labelConstraints.gridx = 0;
        labelConstraints.gridy = 3;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        textBoxConstraints = new GridBagConstraints();
        textBoxConstraints.gridwidth = 4;
        textBoxConstraints.gridx = 1;
        textBoxConstraints.gridy = 3;
        textBoxConstraints.fill = GridBagConstraints.HORIZONTAL;

        myPanel.add(new JLabel("Use Https"), labelConstraints);
        myPanel.add(httpsField, textBoxConstraints);



        String attempt = UrlDialog.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/dg-icon.png";

        ImageIcon icon = new ImageIcon(attempt);

        int result = JOptionPane.showConfirmDialog(view,
                myPanel,
                "Please enter the target URL",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                icon);
        if (result == JOptionPane.OK_OPTION) {
            String host = hostField.getText();
            String port = portField.getText();
            String path = pathField.getText();
            String proto;
            boolean https = httpsField.isSelected();
            if(https)
                proto = "https://";
            else
                proto = "http://";

            String url = new String();
            if(host != null && !host.trim().isEmpty() && port != null && !port.trim().isEmpty())
            {
                if(path != null && !path.trim().isEmpty())
                    url = proto+ host  + ":" + port + "/" + path;
                else
                    url = proto+ host  + ":" + port;
            }
            if (url != null && !url.isEmpty())
            {
                burpPropertiesManager.setTargetUrl(url);
                burpPropertiesManager.setUseHttps(https);
                burpPropertiesManager.setTargetPort(port);
                burpPropertiesManager.setTargetHost(host);
                burpPropertiesManager.setTargetPath(path);
            }
            else
            {
                return null;
            }
            return url;
        } else {
            return null;
        }
    }


}
class PortFilter extends DocumentFilter {
    static final int maxLength = 5;
    @Override
    public void insertString(FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);
        int val = Integer.parseInt(sb.toString());

        if (test(sb.toString()) && sb.length() <= maxLength && val <= 65535) {
            super.insertString(fb, offset, string, attr);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean test(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);
        int val = Integer.parseInt(sb.toString());

        if (test(sb.toString()) && (sb.length() <= maxLength) && val <= 65535) {
            super.replace(fb, offset, length, text, attrs);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }

    }

    @Override
    public void remove(FilterBypass fb, int offset, int length)
            throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if ((test(sb.toString()) && (sb.length() <= maxLength)) || (sb.length() == 0)) {
            super.remove(fb, offset, length);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }

    }
}