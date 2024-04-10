package com.ncr.common.data.nextgen;

import com.ncr.common.data.AdditionalInfo;
import com.ncr.common.data.TerminalInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Date startTimestamp;
    private Date endTimestamp;
    private TerminalInfo terminal;
    private List<Item> items;
    private List<Vat> vats;
    private List<AdditionalInfo> additionalInfos;
}
