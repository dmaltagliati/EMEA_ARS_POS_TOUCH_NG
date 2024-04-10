package com.ncr.zatca.greencore.data.status;

import lombok.Data;

@Data
public class Retention {
    private int processing;
    private int error;
    private int success;
}
