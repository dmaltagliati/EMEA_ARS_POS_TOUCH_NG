package com.ncr.aymcoupon;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.ncr.loyalty.aym.AymLoyaltyService;
import com.ncr.loyalty.aym.GsonDateDeSerializer;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import java.util.Date;


public class AymCouponService {

    private static final Logger logger = Logger.getLogger(AymCouponSettings.class);

    private Gson gsonGeneric = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .registerTypeAdapter(Date.class, new GsonDateDeSerializer())
            .create();

    private static AymCouponService instance;

    public static AymCouponService getInstance() {
        if (instance == null) {
            instance = new AymCouponService();
        }

        return instance;
    }

    public HttpResponse<String> get(String URL, int timeout, String token, String... queries){
        logger.info("ENTER get. URL: " + URL);


        try {
            String fullUrl = URL;

            if (queries.length > 0){
                fullUrl += "?" + resolveQueries(queries);
            }

            logger.info("fullUrl : " + fullUrl);

            RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = Unirest.get(fullUrl)
                    .header("Accept", "*/*")
                    .header("Authorization", "bearer " + token).asString();

            logger.info("Status: " + response.getStatus());
            logger.info("response.body: " + response.getBody());
            logger.info("EXIT get");

            return response;
        } catch (Exception e) {
            logger.error("Error: ", e);
        }

        logger.info("EXIT get with null");

        return null;
    }

    public HttpResponse<String> post(String URL, int timeout, String token, String body, String... queries){
        logger.info("ENTER post. URL: " + URL);
        logger.info("body : " + body);

        try {
            String fullUrl = URL;

            if (queries.length > 0){
                fullUrl += "?" + resolveQueries(queries);
            }

            logger.info("fullUrl : " + fullUrl);

            RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

            Unirest.setHttpClient(httpClient);
            HttpResponse<String> response = Unirest.post(fullUrl)
                    .header("Accept", "*/*")
                    .header("Authorization", "bearer " + token)
                    .body(body).asString();

            logger.info("Status: " + response.getStatus());
            logger.info("response.body: " + response.getBody());
            logger.info("EXIT post");
            return response;
        } catch (Exception e) {
            logger.error("Error: ", e);
        }

        logger.info("EXIT post with null");
        return null;

    }

    public HttpResponse<String> put(String URL, int timeout, String... queries){
        return null;
    }

    private String resolveQueries(String... queries){
        logger.info("ENTER resolveQueries");
        dumpQueries(queries);

        String fullQuery = "";

        for (int index = 0; index<queries.length; index++){
            fullQuery += queries[index].trim() + (index + 1 == queries.length ? "" :  "&");
        }

        logger.info("EXIT resolveQueries : " + fullQuery);
        return  fullQuery;
    }

    private void dumpQueries(String... queries){
        for(String query : queries){
            logger.info(query);
        }
    }

    public Gson getGsonGeneric() {
        return gsonGeneric;
    }
}
