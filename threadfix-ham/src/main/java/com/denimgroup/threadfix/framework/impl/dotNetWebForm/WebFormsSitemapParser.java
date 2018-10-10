////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2018 Applied Visions - http://securedecisions.com
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
//     This material is based on research sponsored by the Department of Homeland
//     Security (DHS) Science and Technology Directorate, Cyber Security Division
//     (DHS S&T/CSD) via contract number HHSP233201600058C.
//
//     Contributor(s):
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////

package com.denimgroup.threadfix.framework.impl.dotNetWebForm;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.denimgroup.threadfix.CollectionUtils.list;

public class WebFormsSitemapParser {

    List<String> parsedEndpoints = list();

    public WebFormsSitemapParser(File sitemapFile) throws ParserConfigurationException, SAXException, IOException {
        Handler parsingHandler = new Handler(sitemapFile);

        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(sitemapFile, parsingHandler);

        this.parsedEndpoints.addAll(parsingHandler.parsedEndpoints);

        for (File referencedSitemap : parsingHandler.referencedSitemaps) {
            WebFormsSitemapParser referencedParser = new WebFormsSitemapParser(referencedSitemap);
            this.parsedEndpoints.addAll(referencedParser.parsedEndpoints);
        }
    }

    public List<String> getParsedEndpoints() {
        return this.parsedEndpoints;
    }

    public int countEndpointsForFilename(String filename) {
        int cnt = 0;
        for (String endpoint : this.parsedEndpoints) {
            if (endpoint.endsWith("/" + filename)) {
                cnt++;
            }
        }
        return cnt;
    }

    public List<String> getEndpointsForFilename(String filename) {
        List<String> endpoints = list();
        for (String endpoint : this.parsedEndpoints) {
            if (endpoint.endsWith("/" + filename)) {
                endpoints.add(endpoint);
            }
        }
        return endpoints;
    }

    public String getEndpointForFilename(String filename) {
        for (String endpoint : this.parsedEndpoints) {
            if (endpoint.endsWith("/" + filename)) {
                return endpoint;
            }
        }
        return null;
    }


    private class Handler extends DefaultHandler {
        public List<String> parsedEndpoints = list();
        public List<File> referencedSitemaps = list();

        File sitemapFile;

        public Handler(File sitemapFile) {
            this.sitemapFile = sitemapFile.getAbsoluteFile();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (qName.equals("siteMapNode")) {
                String url = attributes.getValue("url");
                if (url != null) {
                    if (url.startsWith("~")) {
                        url = url.substring(1);
                    }
                    url = StringUtils.replaceChars(url, '\\', '/');
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }

                    this.parsedEndpoints.add(url);
                }

                String referencedSitemap = attributes.getValue("siteMapFile");
                if (referencedSitemap != null) {
                    referencedSitemaps.add(new File(this.sitemapFile.getParent(), referencedSitemap));
                }
            }
        }
    }
}
