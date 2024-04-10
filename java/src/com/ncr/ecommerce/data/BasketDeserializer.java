package com.ncr.ecommerce.data;

import com.google.gson.*;
import com.ncr.common.data.AdditionalInfo;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class BasketDeserializer implements JsonDeserializer<Basket> {
    @Override
    public Basket deserialize(JsonElement paramJsonElement, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Double version = 2.0;

        if (paramJsonElement.getAsJsonObject().get("version") != null &&
                paramJsonElement.getAsJsonObject().get("version").getAsDouble() > 1.0) {
            version = paramJsonElement.getAsJsonObject().get("version").getAsDouble();
        }

        Gson gson = new GsonBuilder()
                .setVersion(version)
                .serializeNulls()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .create();

        Basket basket = gson.fromJson(paramJsonElement.getAsJsonObject(), Basket.class);

        try {
            if (basket.getSource() == null) {
                basket.setSource("Ecommerce");
            }
            if (basket.getVersion() == null) {
                basket.setVersion(version);
            }

            List<Tender> tenders = null;
            if (paramJsonElement.getAsJsonObject().get("TenderType") != null) {
                tenders = new ArrayList<Tender>();
                tenders.add(new Tender(paramJsonElement.getAsJsonObject().get("TenderType").getAsString(), new BigDecimal(0), new ArrayList<AdditionalInfo>()));
                basket.setTenders(tenders);
            }

        } catch (IllegalArgumentException ie) {
        }

        return basket;
    }
}
