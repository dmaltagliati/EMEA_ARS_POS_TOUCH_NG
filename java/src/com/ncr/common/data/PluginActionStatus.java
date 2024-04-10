package com.ncr.common.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PluginActionStatus {
    private String description;
    private PluginActionStatusType status;
    private String plugin;
    private List<AdditionalInfo> additionalInfos = new ArrayList<AdditionalInfo>();
}
