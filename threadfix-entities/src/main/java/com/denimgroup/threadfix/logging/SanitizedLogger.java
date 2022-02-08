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

package com.denimgroup.threadfix.logging;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.SimpleMessage;

/**
 * This method provides a single point of access to the loggers to ease sanitization efforts.
 * Just use one of the constructors and use it like a normal logger.
 * @author mcollins
 *
 */
public class SanitizedLogger {
	
	private final Logger log;
	private static final String MY_CANONICAL_CLASS_NAME = SanitizedLogger.class.getCanonicalName();
	
	public SanitizedLogger(String className) {
		log = LogManager.getLogger(className);
	}
	
	public SanitizedLogger(Class<?> className) {
		log = LogManager.getLogger(className);
	}

	/**
	 * The longer form is used for the below methods so that the original line number is reported.
	 * @param message Debug message to log
	 */
	public void debug(String message) {
		if (log.isDebugEnabled()) {
			log.logMessage(Level.DEBUG, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), null);
		}
	}
	
	public void debug(String message, Throwable ex) {
		if (log.isDebugEnabled()) {
			log.logMessage(Level.DEBUG, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), ex);
		}
	}
	
	public void info(String message) {
		if (log.isInfoEnabled()) {
			log.logMessage(Level.INFO, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), null);
		}
	}
	
	public void info(String message, Throwable ex) {
		if (log.isInfoEnabled()) {
			log.logMessage(Level.INFO, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), ex);
		}
	}
	
	public void warn(String message) {
		if (log.isWarnEnabled()) {
			log.logMessage(Level.WARN, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), null);
		}
	}
	
	public void warn(String message, Throwable ex) {
		if (log.isWarnEnabled()) {
			log.logMessage(Level.WARN, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), ex);
		}
	}
	
	public void error(String message) {
		if (log.isErrorEnabled()) {
			log.logMessage(Level.ERROR, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), null);
		}
	}
	
	public void error(String message, Throwable ex) {
		if (log.isErrorEnabled()) {
			log.logMessage(Level.ERROR, null, MY_CANONICAL_CLASS_NAME, null, new SimpleMessage(sanitize(message)), ex);
		}
	}
	
	/**
	 * Blacklist. Should probably be a whitelist but I'm not
	 * sure what else needs to be sanitized.
	 * @param startString The String to be escaped
	 * @return The escaped string, or "&lt;NULL&gt;" if startString is null
	 */
	private String sanitize(String startString) {
		String retVal;
		
		if(startString == null) {
			retVal = "<NULL>";
		} else {
			//	This should handle ", \ and various CRLF characters as well as scary Unicode (non-ASCII) stuff
			// https://commons.apache.org/proper/commons-lang/javadocs/api-3.0/src-html/org/apache/commons/lang3/StringEscapeUtils.html
			retVal = StringEscapeUtils.escapeJava(startString);
		}
		
		return retVal;
	}
	
}
