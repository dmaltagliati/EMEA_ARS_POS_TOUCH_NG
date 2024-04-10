package com.ncr.eft.data.philobroker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Info {
    private String terminalId;
    private String storeId;
    private String merchant;
    private List<PluginInfo> pluginInfo = new ArrayList<PluginInfo>();
    private String version;
    private String user;
    private String password;
}
