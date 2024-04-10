package com.ncr.restclient;

import java.util.HashMap;
import java.util.Properties;

public class HttpClientFactory {
    private static String HTTP = "http";
    private static String HTTPS = "https";
    private static String URL = "url";
    private static String TIMEOUT = "timeout";
    private static String PROTOCOL = "protocol";

    public IHttpClient getClient(Properties props) {
        if (HTTPS.equals(props.getProperty(PROTOCOL, HTTP))) {
            return new HttpsClient(props.getProperty(URL), Integer.parseInt(props.getProperty(TIMEOUT, "10")), new HashMap<String, String>());
        } else {
            return new HttpClient(props.getProperty(URL), Integer.parseInt(props.getProperty(TIMEOUT, "10")), new HashMap<String, String>());
        }
    }
    public IHttpClient getClient(Properties props,String url) {
        if (HTTPS.equals(props.getProperty(PROTOCOL, HTTP))) {
            return new HttpsClient(url, Integer.parseInt(props.getProperty(TIMEOUT, "10")), new HashMap<String, String>());
        } else {
            return new HttpClient(url, Integer.parseInt(props.getProperty(TIMEOUT, "10")), new HashMap<String, String>());
        }
    }
    public IHttpClient getClient(String url,int timeout ) {
            return new HttpClient(url, timeout, new HashMap<String, String>());

    }
    public IHttpClient getClient(Properties props,String url,String username, String password) {
        if (HTTPS.equals(props.getProperty(PROTOCOL, HTTP))) {
            return new HttpsClient(url, Integer.parseInt(props.getProperty(TIMEOUT, "10")), new HashMap<String, String>(),username,password);
        } else {
            return new HttpClient(url, Integer.parseInt(props.getProperty(TIMEOUT, "10")), new HashMap<String, String>());
        }
    }
}
