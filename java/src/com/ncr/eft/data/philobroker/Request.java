package com.ncr.eft.data.philobroker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String systemId;
    private Info info;
    //private String operationType;
    private String transactionType;
    private Payload payload;
}
