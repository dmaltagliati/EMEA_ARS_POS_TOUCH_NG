package com.ncr.common.utilities;

import lombok.Getter;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.util.Properties;

public class AutoCommandManager {
    private static final Logger logger = Logger.getLogger(AutoCommandManager.class);
    private static final String AUTO_LOGON_MANAGER = "conf/auto-cmd.properties";
    private static final String ENABLED = "enabled";
    private static final String OPERATOR = "operator";
    private static final String ENTER = "000D";
    private static AutoCommandManager instance = null;
    private final Properties props = new Properties();
    @Getter
    private boolean enabled = false;
    private Component component;
    private EventQueue queue;
    private boolean startUp = true;

    public static AutoCommandManager getInstance(){
        if (instance == null) {
            instance = new AutoCommandManager();
        }
        return instance;
    }

    private AutoCommandManager() {
        loadProperties();
    }

    public void initialize (Component component, EventQueue queue) {
        this.component = component;
        this.queue = queue;
    }

    private void loadProperties() {
        try {
            props.load(new FileInputStream(AUTO_LOGON_MANAGER));
            enabled = Boolean.parseBoolean(props.getProperty(ENABLED, "false"));
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    public void logon() {
        if (!enabled) return;
        if (!startUp) return;

        String operator = props.getProperty(OPERATOR, "001");
        logger.debug("Operator: " + operator);
        String cmd = "AUTO:" + operator + ":" + ENTER;
        postAction(cmd);
        startUp = false;
    }

    public boolean isOperator(int nbr) {
        return enabled && String.valueOf(nbr).equals(props.getProperty(OPERATOR, "001"));
    }

    public void postAction(String cmd) {
        ActionEvent e = new ActionEvent(component, ActionEvent.ACTION_PERFORMED, cmd);
        queue.postEvent(e);
        logger.info("postAction " + cmd);
    }
}