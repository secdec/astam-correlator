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
//              Denim Group, Ltd.
//              Secure Decisions, a division of Applied Visions, Inc
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.impl.struts;

import com.denimgroup.threadfix.framework.impl.struts.model.StrutsAction;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsPackage;
import com.denimgroup.threadfix.framework.impl.struts.model.StrutsResult;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;

/**
 * Created by sgerick on 11/12/2014.
 */
public class StrutsXmlParser {
	private static final SanitizedLogger log = new SanitizedLogger("FrameworkCalculator");

     Collection<File> configFiles;

	public StrutsXmlParser(){}

	public StrutsXmlParser(Collection<File> files) {
	    this();
	    this.configFiles = files;
    }

	public  List<StrutsPackage> parse(File f) {
		XmlParser handler = new StrutsXmlParser.XmlParser();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = null;
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(f,handler);
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException parsing struts.xml", e);
		} catch (SAXException e) {
			log.error("SAXException parsing struts.xml", e);
		} catch (IOException e) {
			log.error("IOException parsing struts.xml", e);
		} catch (Exception e) {
			log.error("General exception parsing struts.xml", e);
		}

		List<StrutsPackage> packages = handler.strutsPackages;
		for (StrutsPackage strutsPackage : packages) {
			if (strutsPackage.getNamespace() == null) {
				strutsPackage.setNamespace("/");
			}
			for (StrutsAction action : strutsPackage.getActions()) {
				if (action.getMethod() == null) {
					action.setMethod("execute");
				}
			}
		}

		return packages;
	}


	private class XmlParser extends DefaultHandler {
		List<StrutsPackage> strutsPackages = list();
		boolean bPackage = false;
		boolean bAction = false;
		boolean bResult = false;
		boolean bParam = false;
		boolean bInclude = false;
		StrutsPackage strutsPackage;
		StrutsAction strutsAction;
		StrutsResult strutsResult;
		String paramName;
		String paramValue;

		public final void startElement(String uri, String localName, String qName,
		                         Attributes attributes) throws SAXException {


			if (qName.equalsIgnoreCase("package")) {
				bPackage = true;
				strutsPackage = new StrutsPackage();
				strutsPackage.setName( attributes.getValue("name") );
				strutsPackage.setNamespace(attributes.getValue("namespace"));
				strutsPackage.setPkgExtends( attributes.getValue("extends") );
			}

			if (qName.equalsIgnoreCase("action") && (bPackage)) {
				bAction = true;
				strutsAction = new StrutsAction();
				paramName = null;
				paramValue = null;
				strutsAction.setName( attributes.getValue("name") );
				strutsAction.setMethod( attributes.getValue("method") );
				strutsAction.setActClass( attributes.getValue("class") );
			}

			if (qName.equalsIgnoreCase("result") && (bAction)) {
				bResult = true;
				strutsResult = new StrutsResult();
				paramName = null;
				paramValue = null;
				strutsResult.setName(attributes.getValue("name"));
				strutsResult.setType(attributes.getValue("type"));
			}

			if (qName.equalsIgnoreCase("param")	&& (bAction||bResult)) {
				bParam = true;
				paramName = attributes.getValue("name");

			}

			if(qName.equalsIgnoreCase("include")){
			    bInclude = true;
			    String path = attributes.getValue("file");
			    log.info("Found path to another config file: " + path );
			    strutsPackages.addAll(parse(path));
            }

		}

		public final void endElement(String uri, String localName,
		                       String qName) throws SAXException {

			if (qName.equalsIgnoreCase("package")) {
				bPackage = false;
				strutsPackages.add( strutsPackage );
			}

			if (qName.equalsIgnoreCase("action")) {
				bAction = false;
				if (bPackage)
					strutsPackage.addAction(strutsAction);
			}

			if (qName.equalsIgnoreCase("result")) {
				bResult = false;
				if (bAction)
					strutsAction.addResult(strutsResult);
			}

			if (qName.equalsIgnoreCase("param")) {
				bParam = false;
				if (bResult)
					strutsResult.addParam(paramName, paramValue);
				else if (bAction)
					strutsAction.addParam(paramName, paramValue);
			}

			if(qName.equalsIgnoreCase("include")){
			    bInclude = false;
            }

		}

		public final void characters(char ch[], int start, int length) throws SAXException {

			if (bPackage) {
				if (bAction) {  // action in Package
					if (bParam) {   // param in Action
						paramValue = new String(ch, start, length).trim();
					} else
						if (bResult) {  // result in Action
							if (bParam) {   // param in Result
								paramValue = new String(ch, start, length).trim();
							} else {
								String s = new String(ch, start, length).trim();
								if (s.length() == 0)
									s = null;
								strutsResult.setValue(s);
							}
						}
				}
			}

		}

		private File getConfigFile(String fName, Collection<File> files){
            for(Iterator iterator = files.iterator(); iterator.hasNext();){
		        File file = (File) iterator.next();
                if(file.getName().equalsIgnoreCase(fName)){
                    return file;
                }
            }
            return null;
        }

        private  String extractFileName(String filePath){
            return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        }

        private List<StrutsPackage> parse(String path){
            String fileName = extractFileName(path);
            File configFile = getConfigFile(fileName, configFiles);
            if (configFile != null) {
				StrutsXmlParser strutsXmlParser = new StrutsXmlParser();
				return strutsXmlParser.parse(configFile);
			} else {
            	return null;
			}
        }

	}
}
