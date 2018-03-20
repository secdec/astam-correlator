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

package com.denimgroup.threadfix.plugin.zap.action;

import com.denimgroup.threadfix.data.entities.RouteParameter;
import com.denimgroup.threadfix.data.interfaces.Endpoint;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabase;
import com.denimgroup.threadfix.framework.engine.full.EndpointDatabaseFactory;
import com.denimgroup.threadfix.framework.util.EndpointUtil;
import com.denimgroup.threadfix.plugin.zap.dialog.OptionsDialog;
import org.parosproxy.paros.extension.ViewDelegate;
import org.zaproxy.zap.extension.threadfix.ZapPropertiesManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EndpointsButton extends JButton {

    private String name;
    public static final String GENERIC_INT_SEGMENT = "\\{id\\}";
    private AttackThread attackThread = null;
    List<String> nodes = new ArrayList<>();

    public EndpointsButton(final ViewDelegate view, String name) {
        this.name = name;
        setText(getMenuItemText());

        addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {


                boolean configured = OptionsDialog.Validate(view);
                boolean completed = false;

                if (configured) {
                    Endpoint.Info[] endpoints = getEndpoints();
                    fillEndpointsToTable(endpoints);
                    if ((endpoints == null) || (endpoints.length == 0)) {
                        view.showWarningDialog("Failed to retrieve endpoints from the source. Check your inputs.");
                    } else {

                        buildNodesFromEndpoints(endpoints);

                        String url = ZapPropertiesManager.INSTANCE.getTargetUrl();

                        if (url != null) { // cancel not pressed
                            completed = attackUrl(url);
                            if (!completed) {
                                view.showWarningDialog("Invalid URL.");
                            }
                        }
                    }
                }
                else if(ZapPropertiesManager.INSTANCE.getSourceFolder() != null &&!ZapPropertiesManager.INSTANCE.getSourceFolder().trim().isEmpty())
                {
                    Endpoint.Info[] endpoints = getEndpoints();
                    if ((endpoints == null) || (endpoints.length == 0)) {
                        view.showWarningDialog("Failed to retrieve endpoints from the source. Check your inputs.");
                    }
                    else
                    {
                        fillEndpointsToTable(endpoints);
                        view.showMessageDialog("The endpoints were successfully generated from source.");
                    }

                }

                if (completed) {
                    view.showMessageDialog("The endpoints were successfully generated from source.");
                }
            }
        });
    }

    public void buildNodesFromEndpoints(Endpoint.Info[] endpoints) {
        for (Endpoint.Info endpoint : endpoints) {
            if (endpoint != null) {

                String urlPath = endpoint.getUrlPath();

                if (urlPath.startsWith("/")) {
                    urlPath = urlPath.substring(1);
                }

                urlPath = urlPath.replaceAll(GENERIC_INT_SEGMENT, "1");

                nodes.add(urlPath);

                Map<String, RouteParameter> params = endpoint.getParameters();

                if (!params.isEmpty()) {
                    for(Map.Entry<String, RouteParameter> parameter : params.entrySet()){
                        nodes.add(urlPath + "?" + parameter.getKey() + "=" + parameter.getValue());
                    }
                }
            }
        }
    }

    public boolean attackUrl(String url) {
        try {
            if(!url.substring(url.length()-1).equals("/")){
                url = url+"/";
            }
            attack(new URL(url));
            return true;
        } catch (MalformedURLException e1) {
            return false;
        }
    }

    private void attack (URL url) {

        if (attackThread != null && attackThread.isAlive()) {
            return;
        }
        attackThread = new AttackThread();
        attackThread.setNodes(nodes);
        attackThread.setURL(url);
        attackThread.start();

    }

    public Endpoint.Info[] getEndpoints() {
        return getEndpoints(ZapPropertiesManager.INSTANCE.getSourceFolder());
    }

    public Endpoint.Info[] getEndpoints(String sourceFolder) {

        EndpointDatabase endpointDatabase = EndpointDatabaseFactory.getDatabase(sourceFolder);

        Endpoint.Info[] endpoints = null;
        if (endpointDatabase != null) {
            List<Endpoint> endpointList = endpointDatabase.generateEndpoints();
            endpointList = EndpointUtil.flattenWithVariants(endpointList);
            endpoints = new Endpoint.Info[endpointList.size()];
            int i = 0;
            for (Endpoint endpoint : endpointList) {
                endpoints[i++] = Endpoint.Info.fromEndpoint(endpoint);
            }
        }

        return endpoints;
    }

   public String getMenuItemText()
   {
       return name;
   }

    private void fillEndpointsToTable(Endpoint.Info[] endpoints)
    {
        int count = 0;
        JTable endpointTable = ZapPropertiesManager.INSTANCE.getEndpointsTable();
        DefaultTableModel dtm = (DefaultTableModel)endpointTable.getModel();
        while(dtm.getRowCount() > 0)
        {
            dtm.removeRow(0);
        }
        for (Endpoint.Info endpoint : endpoints)
        {
            boolean hasGet = false;
            boolean hasPost = false;
            String method = endpoint.getHttpMethod();
            if(method.toString().equalsIgnoreCase("post"))
                hasPost = true;
            else if (method.toString().equalsIgnoreCase("get"))
                hasGet = true;
            dtm.addRow(new Object[]
                    {
                            endpoint.getUrlPath(),
                            endpoint.getParameters().size(),
                            hasGet,
                            hasPost,
                            endpoint
                    });
            count++;
        }

    }

}
