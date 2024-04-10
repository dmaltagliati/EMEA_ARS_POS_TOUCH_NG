package com.ncr.tablet;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class AsrTabletConfig {


    private static final Logger logger = Logger.getLogger(AsrTabletConfig.class);
    private static final String ASR_TABLET_PROPERTIES = "conf/asrTablet.properties";
    private Properties props = new Properties();

    private static AsrTabletConfig instance;

    public AsrTabletConfig(){
        load();
    }

    public static AsrTabletConfig getInstance(){
        if (instance == null){
            instance = new AsrTabletConfig();
        }

        return instance;
    }

    public void load(){
        try {
            props.load(new FileInputStream(ASR_TABLET_PROPERTIES));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    public boolean isEnable(){
        return Boolean.parseBoolean(props.getProperty("asrTablet.enabled", "false"));
    }

    public String getPath(){
        String path = props.getProperty("asrTablet.path", "c:/gd90/bmp/");
        checkDirectory(path);

        return path;
    }

    public boolean isDebug(){
        return Boolean.parseBoolean(props.getProperty("asrTablet.debugMode", "false"));
    }
    private boolean checkDirectory(String path){

        boolean success = false;
        File localFolder = new File(path);

        if (localFolder.isDirectory()) {
            success = true;
        }else{
            logger.info("creating folder" + path);
            success = localFolder.mkdir();

            if (success){
                logger.info("folder created.");
            }else{
                logger.info("creating folder error.");
            }
        }
        return success;
    }
}
