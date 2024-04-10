package com.ncr.eft.data.philobroker;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    @SerializedName("systemId")
    private String systemId;
   /* @SerializedName("info")
    private Info info;*/
    @SerializedName("payload")
    private Payload payload;
    /*@SerializedName("responseCode")
    private String responseCode;*/
    @SerializedName("timeStamp")
    private String timeStamp;
    @SerializedName("results")
    private List<Result> results;


}
