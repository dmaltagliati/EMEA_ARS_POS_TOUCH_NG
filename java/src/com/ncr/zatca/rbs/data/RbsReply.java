package com.ncr.zatca.rbs.data;

import lombok.Data;

@Data
public class RbsReply {
    private boolean hasError;
    private String errorDesc;
    private String result;
}
