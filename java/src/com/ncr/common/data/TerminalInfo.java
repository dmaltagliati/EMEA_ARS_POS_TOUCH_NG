package com.ncr.common.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalInfo {
    private String storeId;
    private String registerId;
    private String cashierId;
    private String transactionNumber;
}
