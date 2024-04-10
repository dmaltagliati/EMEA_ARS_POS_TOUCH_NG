package com.ncr.common.data.nextgen;

import com.ncr.common.data.AdditionalInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BasicInfo {
    private String text;
    private String code;
    private List<AdditionalInfo> additionalInfos = new ArrayList<AdditionalInfo>();
}
