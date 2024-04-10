package com.ncr.zatca.utils;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import java.util.Properties;

import static com.ncr.zatca.utils.Verb.GET;
import static com.ncr.zatca.utils.Verb.POST;

public class UnirestWrapper {
    private static final Logger logger = Logger.getLogger(UnirestWrapper.class);
    private static UnirestWrapper instance = null;

    private static final String TIMEOUT = "timeout";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private UnirestWrapper() {
    }

    public static UnirestWrapper getInstance() {
        if (instance == null)
            instance = new UnirestWrapper();

        return instance;
    }

    public JsonElement getRequest(String endpoint, Properties props) {
        return request(GET, endpoint, null, props);
    }

    public JsonElement postRequest(String endpoint, String serializedRequest, Properties props) {
        return request(POST, endpoint, serializedRequest, props);
    }
    public JsonElement postRequestSSL(String endpoint, String serializedRequest, Properties props) {
        return requestSSL(POST, endpoint, serializedRequest, props);
    }

    private JsonElement request(Verb verb, String endpoint, String serializedRequest, Properties props) {
        logger.debug("Enter. Verb: " + verb +  " Endpoint: " + endpoint + " Serialized request: " + serializedRequest);
        logger.debug("Using basic authentication: [" + props.getProperty(USERNAME, "") + "] [" + props.getProperty(PASSWORD, "") + "]");
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(props.getProperty(TIMEOUT, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(props.getProperty(TIMEOUT, "0")))
                    .setSocketTimeout(Integer.parseInt(props.getProperty(TIMEOUT, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
//                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
//                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(config)
                    .build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = null;
            if (verb == POST) {
                HttpRequestWithBody request = Unirest.post(endpoint)
                        .basicAuth(props.getProperty(USERNAME, ""), props.getProperty(PASSWORD, ""))
                        .header("Content-Type", "application/json")
                        .header("Accept", "*/*");
                response = request.body(serializedRequest).asString();
            } else {
                response = Unirest.get(endpoint)
                        .basicAuth(props.getProperty(USERNAME, ""), props.getProperty(PASSWORD, ""))
                        .header("Accept", "*/*")
                        .asString();
            }

            logger.debug("Status: " + response.getStatus());
            logger.debug("Body: " + response.getBody());
            if (response.getStatus() == HttpStatus.SC_OK) {
                return new JsonParser().parse(response.getBody());
            } else {
                logger.warn("Error from server: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            return null;
        }
    }

    private JsonElement requestSSL(Verb verb, String endpoint, String serializedRequest, Properties props) {
        logger.debug("Enter. Verb: " + verb +  " Endpoint: " + endpoint + " Serialized request: " + serializedRequest);
        logger.debug("Using basic authentication: [" + props.getProperty(USERNAME, "") + "] [" + props.getProperty(PASSWORD, "") + "]");
        try {
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(props.getProperty(TIMEOUT, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(props.getProperty(TIMEOUT, "0")))
                    .setSocketTimeout(Integer.parseInt(props.getProperty(TIMEOUT, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(config)
                    .build();
            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = null;
            if (verb == POST) {
                HttpRequestWithBody request = Unirest.post(endpoint)
                        .basicAuth(props.getProperty(USERNAME, ""), props.getProperty(PASSWORD, ""))
                        .header("Content-Type", "application/json")
                        .header("Accept", "*/*");
                response = request.body(serializedRequest).asString();
            } else {
                response = Unirest.get(endpoint)
                        .basicAuth(props.getProperty(USERNAME, ""), props.getProperty(PASSWORD, ""))
                        .header("Accept", "*/*")
                        .asString();
            }

            logger.debug("Status: " + response.getStatus());
            logger.debug("Body: " + response.getBody());
            if (response.getStatus() == HttpStatus.SC_OK) {
                return new JsonParser().parse(response.getBody());
            } else {
                logger.warn("Error from server: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            return null;
        }
    }

}
