package com.ncr.zatca.greencore.data.b2b;

import lombok.Data;

@Data
public class PostalAddress {
    private String streetName;
    private String buildingNumber;
    private String citySubdivisionName;
    private String cityName;
    private String postalZone;
    private String countrySubEntity;
    private Country country;
}
