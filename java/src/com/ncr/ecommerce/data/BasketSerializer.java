package com.ncr.ecommerce.data;

import com.google.gson.*;

import java.lang.reflect.Type;

public class BasketSerializer implements JsonSerializer<Basket> {
    @Override
    public JsonElement serialize(Basket basket, Type typeOfSrc, JsonSerializationContext jsonSerializationContext) {

        Double version = 1.0;

        if (basket.getVersion() != null && basket.getVersion() > 1.0) {
            version = basket.getVersion();
        }

        Gson gson = new GsonBuilder()
                .setVersion(version)
                .serializeNulls()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create();

        JsonObject jsonBasket = new JsonObject();

        try {
            jsonBasket = (JsonObject) gson.toJsonTree(basket);
            if (version == 1.0 && (basket.getTenders() != null && basket.getTenders().size() > 0)) {
                jsonBasket.addProperty("TenderType", basket.getTenders().get(0).getType());
            }
        } catch (IllegalArgumentException ie) {
        }

        return jsonBasket;
    }
}