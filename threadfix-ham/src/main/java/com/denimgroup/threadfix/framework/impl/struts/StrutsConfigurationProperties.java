////////////////////////////////////////////////////////////////////////
//
//     Copyright (C) 2017 Applied Visions - http://securedecisions.com
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

package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StrutsConfigurationProperties {

    private static final SanitizedLogger log = new SanitizedLogger("StrutsConfigurationProperties");

    Map<String, String> configurationValues = new HashMap<String, String>();



    public boolean hasProperty(String key) {
        return configurationValues.containsKey(key);
    }

    public String get(String key) {
        if (!hasProperty(key)) {
            return null;
        } else {
            return configurationValues.get(key);
        }
    }

    public String get(String key, String defaultValue) {
        if (!hasProperty(key)) {
            return defaultValue;
        } else {
            return configurationValues.get(key);
        }
    }

    public Map<String, String> getAllProperties() {
        return new HashMap<String, String>(configurationValues);
    }




    public void addOrReplaceProperty(String name, String value) {
        if (configurationValues.containsKey(name)) {
            String currentValue = configurationValues.get(name);
            log.debug("Found a new value for " + name + " when it was already detected, replacing from " + currentValue + " to " + value);
        }

        configurationValues.put(name, value);
    }

    public void loadFromStrutsXml(File strutsXmlFile) {
        StrutsXmlParser parser = new StrutsConfigurationProperties.StrutsXmlParser(this);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = null;

        try {
            saxParser = factory.newSAXParser();
            saxParser.parse(strutsXmlFile, parser);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromStrutsProperties(File strutsPropertiesFile) {
        Properties properties = StrutsPropertiesParser.getStrutsProperties(strutsPropertiesFile);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            addOrReplaceProperty(entry.getKey().toString(), entry.getValue().toString());
        }
    }



    private class StrutsXmlParser extends DefaultHandler {

        StrutsConfigurationProperties target;

        public StrutsXmlParser(StrutsConfigurationProperties target) {
            this.target = target;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (qName.equalsIgnoreCase("constant")) {
                String name = attributes.getValue("name");
                String value = attributes.getValue("value");
                target.addOrReplaceProperty(name, value);
            }
        }
    }
}
