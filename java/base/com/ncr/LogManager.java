package com.ncr;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LogManager {
    private static final Logger logger = Logger.getLogger(LogManager.class);
    private static LogManager instance = null;

    public static LogManager getInstance() {
        if (instance == null)
            instance = new LogManager();

        return instance;
    }

    private LogManager() {}

    public void init() {
        try {
            PropertyConfigurator.configure("conf/Log4j.properties");
            logger.fatal("---------- POS STARTS ----------");
            logger.fatal("Found configuration file");
            System.out.println("Found configuration file");
        } catch (Exception e) {
            logger.fatal("Error: ", e);
            System.err.println("Error loading configuration file");
        }
    }
}
