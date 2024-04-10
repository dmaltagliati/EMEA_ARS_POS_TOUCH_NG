package com.ncr.eft;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;


public class SequenceGenerator {
    private static final Logger logger = Logger.getLogger(SequenceGenerator.class);
    private final static String NUM_PROPERTIES = "conf/sequences.properties";

    private static SequenceGenerator instance = null;

    private SequenceGenerator() {
    }

    public static SequenceGenerator getInstance() {
        if (instance == null)
            instance = new SequenceGenerator();
        return instance;
    }

    public void init() {
        logger.debug("Initializing Number Generator");
    }

    public long getNext(String client, long min, long max) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(NUM_PROPERTIES));
            long sequence = Long.parseLong(props.getProperty(client + ".sequence", "1")) + 1;
            if (sequence < min || sequence > max) sequence = min;
            props.setProperty(client + ".sequence", String.valueOf(sequence));
            props.store(new FileOutputStream(NUM_PROPERTIES), "");
            return sequence;
        } catch (Exception e) {
            logger.error("Error loading " + NUM_PROPERTIES + ": ", e);
            return min;
        }
    }
}
