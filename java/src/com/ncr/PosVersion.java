package com.ncr;

import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA. User: Administrator Date: 14/07/15 Time: 12.29 To change this template use File |
 * Settings | File Templates.
 */
public class PosVersion {
	private static final Logger logger = Logger.getLogger(PosVersion.class);

	private final static String NAME = "POS JAVA 3.01";
	private final static String MAJOR = "2";
	private final static String MINOR = "17";
	private final static String REVISION = "14";
	private final static String BUILD = "20240306";
	private final static String DATE = "06-03-2024 12:21";

	private static final String version = MAJOR + "." + MINOR + "." + REVISION;

	public static String fullVersion() {
		logger.fatal(NAME + " version " + getVersion() + " build " + getBuild() + " date " + getDate() + " jvm " + getJavaVersion());
		return NAME + " version " + getVersion() + " build " + getBuild() + " date " + getDate() + " jvm " + getJavaVersion();
	}

	public static String shortVersion() {
		logger.fatal(NAME + " version " + getVersion() + " build " + getBuild());
		return NAME + " version " + getVersion() + " build " + getBuild();
	}

	public static String getDate() {
		logger.debug("date: " + DATE);
		return DATE;
	}

	public static String getVersion() {
		logger.debug("version: " + version);
		return version;
	}

	public static String getName() {
		logger.debug("name: " + NAME);
		return NAME;
	}

	public static String getBuild() {
		logger.debug("build: " + BUILD);
		return BUILD;
	}

	public static String getJavaVersion() {
		logger.debug("JVM version: " + System.getProperty("java.version"));
		return System.getProperty("java.version");
	}
}
