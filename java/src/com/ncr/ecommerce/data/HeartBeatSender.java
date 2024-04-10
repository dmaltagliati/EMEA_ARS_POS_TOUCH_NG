package com.ncr.ecommerce.data;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ncr.ecommerce.ECommerceManager;
import com.ncr.restclient.HttpClientFactory;
import com.ncr.restclient.IHttpClient;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MultivaluedMap;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

public class HeartBeatSender
{
    private static final Logger logger = Logger.getLogger(HeartBeatSender.class);
    private static final String HEARTBEAT_PROPERTIES = "conf/heartbeat.properties";
    private Properties props = new Properties();
    private Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    private static HeartBeatSender instance = null;

    public static HeartBeatSender getInstance() {
        if (instance == null) {
            instance = new HeartBeatSender();
        }
        return instance;
    }

    private HeartBeatSender() {
        loadProperties();
    }

    private void loadProperties() {
        try {
            props.load(new FileInputStream(HEARTBEAT_PROPERTIES));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
    }

    public void send(String terminalId, Integer errorCode) {
        TerminalItem item = new TerminalItem(terminalId, errorCode);
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("authorize", "false");
        IHttpClient client = new HttpClientFactory().getClient(props);
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        String content = gson.toJson(item);
        client.post(params, content);
        logger.debug("SendHeartBeatMessage to WS: TerminalID: " + terminalId + "- ErrorCode: " + errorCode);
    }
}
