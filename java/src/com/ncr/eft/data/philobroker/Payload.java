package com.ncr.eft.data.philobroker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payload {
    private String cashierId;
    private String currencyCode;
    private int length;
    private String sessionId;
    private AdditionalInfo additionalInfo;
}
