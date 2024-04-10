package com.ncr.zatca.greencore.data.b2b.sap;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class B2bCustomer extends B2bResponse {
    @SerializedName("CUSTOMER")
    private String customer;
    @SerializedName("NAME_EN")
    private String englishName;
    @SerializedName("NAME_AR")
    private String arabicName;
    @SerializedName("VAT_NO")
    private String vatRegistrationNumber;
    @SerializedName("STREET")
    private String addressStreet;
    @SerializedName("CITY")
    private String addressCity;
    @SerializedName("POSTAL_CODE")
    private String addressZip;
    @SerializedName("COUNTRY_CODE")
    private String addressCountryCode;
    @SerializedName("COUNTRY_TEXT")
    private String addressCountry;
}
