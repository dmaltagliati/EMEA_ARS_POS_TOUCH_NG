package com.ncr.zatca.greencore.data.status;

import lombok.Data;

@Data
public class ResponseBody {
    private String status;
    private String errorMessage;
    private String warningMessage;
}
