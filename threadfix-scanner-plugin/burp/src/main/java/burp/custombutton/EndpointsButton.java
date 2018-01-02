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
//     Contributor(s):
//             Denim Group, Ltd.
//             Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package burp.custombutton;

import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.dialog.ConfigurationDialogs;
import burp.dialog.UrlDialog;
import burp.extention.BurpPropertiesManager;
import com.denimgroup.threadfix.data.enums.ParameterDataType;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stran
 * Date: 12/30/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EndpointsButton extends JButton {

    public static final String GENERIC_INT_SEGMENT = "\\{id\\}";

    public EndpointsButton(final Component view, final IBurpExtenderCallbacks callbacks) {

        setText(getButtonText());

        addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean configured = ConfigurationDialogs.show(view, getDialogMode());
                boolean completed = false;
                java.util.List<String> nodes = new ArrayList<>();

                if (configured) {
                    if (BurpPropertiesManager.getBurpPropertiesManager().getConfigFile() != null ) {
                        callbacks.loadConfigFromJson(getBurpConfigAsString());
                    }

                    Endpoint.Info[] endpoints = getEndpoints();

                    if (endpoints.length == 0) {
                        JOptionPane.showMessageDialog(view, getNoEndpointsMessage(), "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        for (Endpoint.Info endpoint : endpoints) {
                            if (endpoint != null) {
                                String endpointPath = endpoint.getUrlPath();
                                if (endpointPath.startsWith("/")) {
                                    endpointPath = endpointPath.substring(1);
                                }
                                endpointPath = endpointPath.replaceAll(GENERIC_INT_SEGMENT, "1");
                                nodes.add(endpointPath);

                                for(Map.Entry<String, ParameterDataType> parameter : endpoint.getParameters().entrySet()) {
                                    nodes.add(endpointPath + "?" + parameter.getKey() + "=" + parameter.getValue());
                                }
                            }
                        }

                        String url = UrlDialog.show(view);

                        if (url != null) { // cancel not pressed
                            try {
                                if (!url.substring(url.length() - 1).equals("/")) {
                                    url = url+"/";
                                }
                                for (String node: nodes) {
                                    URL nodeUrl = new URL(url + node);
                                    callbacks.includeInScope(nodeUrl);
                                    if(BurpPropertiesManager.getBurpPropertiesManager().getAutoSpider())
                                        callbacks.sendToSpider(nodeUrl);

                                }
                                completed = true;
                            } catch (MalformedURLException e1) {
                                JOptionPane.showMessageDialog(view, "Invalid URL.",
                                        "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }

                if (completed) {
                    JOptionPane.showMessageDialog(view, getCompletedMessage());
                }
                if(BurpPropertiesManager.getBurpPropertiesManager().getAutoScan())
                    sendToScanner(callbacks);
            }
        });
    }

    private void sendToScanner(IBurpExtenderCallbacks callbacks) {
        IHttpRequestResponse[] responses = callbacks.getSiteMap(BurpPropertiesManager.getBurpPropertiesManager().getTargetUrl());
        for (IHttpRequestResponse response : responses) {
            IHttpService service = response.getHttpService();
            boolean useHttps = service.getProtocol().equalsIgnoreCase("https");
            callbacks.doActiveScan(service.getHost(), service.getPort(), useHttps, response.getRequest());
        }
    }

    private String getBurpConfigAsString() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(BurpPropertiesManager.getBurpPropertiesManager().getConfigFile()));

            return jsonObject.toJSONString();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    protected abstract String getButtonText();

    protected abstract String getNoEndpointsMessage();

    protected abstract String getCompletedMessage();

    protected abstract ConfigurationDialogs.DialogMode getDialogMode();

    protected abstract Endpoint.Info[] getEndpoints();
}
