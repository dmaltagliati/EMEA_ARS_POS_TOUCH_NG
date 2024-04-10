package com.ncr;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.util.Date;

public class UtilLog4j {

	private final static String ASTERISK_LINE = "****************************************************************************************";

	private static boolean m_bolInitialized = false;

	private final static String thisClassName = UtilLog4j.class.getName();

	private static String m_strConfigurationFileName;

	public static boolean isInitialized() {
		return m_bolInitialized;
	}

	public static void initialize(Class theClass, String strProgramName, String strProgramTitle,
			String strProgramVersion, String strProgramDate) {

		boolean bolSuccess = initialize(strProgramName);

		if (bolSuccess) {

			logFatal(theClass, ASTERISK_LINE);

			logFatal(theClass,
					" " + strProgramTitle + " - Versione " + strProgramVersion + " (" + strProgramDate + ")");

			logFatal(theClass, ASTERISK_LINE);

		}
	}

	public static boolean initialize(String strApplicationName) {

		m_strConfigurationFileName = "conf/" + strApplicationName + "_Log4j.properties";

		if (m_bolInitialized) {
			return false;
		}

		// *************************************************************************
		// Se log4j F gia stato inizializzato da qualcun altro, esco con
		// successo
		// *************************************************************************
		Logger root = Logger.getRootLogger();
		boolean rootIsConfigured = root.getAllAppenders().hasMoreElements();

		if (rootIsConfigured) {
			logFatal(UtilLog4j.class, "root logger already configured by someone else. Skip configuring with file \""
					+ m_strConfigurationFileName + "\"");
			m_bolInitialized = true;
			return true;
		}

		File file = new File(m_strConfigurationFileName);

		if (!file.exists()) {
			return false;
		}
		try {

			PropertyConfigurator.configure(m_strConfigurationFileName);
			m_bolInitialized = true;

		} catch (Throwable e) {
			System.out.println(new Date() + " [" + System.currentTimeMillis() + "] " + thisClassName
					+ ".init - Exception opening file \"" + m_strConfigurationFileName + "\"");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void terminate(boolean bolSuccess) {

		String strResult = "SUCCESSO.";

		if (!bolSuccess) {
			strResult = "ERRORE !!!";

		}

		logInformation(UtilLog4j.class, ASTERISK_LINE);
		logInformation(UtilLog4j.class, " Programma terminato con " + strResult);
		logInformation(UtilLog4j.class, ASTERISK_LINE);

	}

	static public boolean logFatal(Class theClass, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).log(thisClassName, Level.FATAL, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean logFatal(Class theClass, String message, Throwable exception) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).fatal(message, exception);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logFatal(String strName, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(strName).log(thisClassName, Level.FATAL, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logError(Class theClass, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).log(thisClassName, Level.ERROR, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static boolean logError(Class theClass, String message, Throwable exception) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).error(message, exception);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logWarning(Class theClass, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).log(thisClassName, Level.WARN, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logWarning(Class theClass, String message, Exception exception) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).log(thisClassName, Level.WARN, message, exception);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logInformation(Class theClass, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).log(thisClassName, Level.INFO, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logDebug(Class theClass, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(theClass).log(thisClassName, Level.DEBUG, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logInformation(String strName, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(strName).log(thisClassName, Level.INFO, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logDebug(String strName, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(strName).log(thisClassName, Level.DEBUG, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logError(String strName, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(strName).log(thisClassName, Level.ERROR, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logWarning(String strName, String message) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(strName).log(thisClassName, Level.WARN, message, null);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	static public boolean logError(String strName, String message, Exception exception) {

		if (!m_bolInitialized) {
			return false;
		}

		try {
			Logger.getLogger(strName).log(thisClassName, Level.ERROR, message, exception);

		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static void logInformation(Class theClass, String message, int offset) {
		logInformation(theClass, message);
	}

}
