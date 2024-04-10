package com.ncr.eft.data.philobroker;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
	@SerializedName("sequenceId")
	private String sequenceId;

	@SerializedName("transactionType")
	private String transactionType;

	@SerializedName("errorCode")
	private String errorCode;

	@SerializedName("operationID")
	private String operationId;

	@SerializedName("status")
	private String status;

	@SerializedName("responseCode")
	private String responseCode;

	@SerializedName("transactionDetail")
	private TransactionDetail transactionDetail;

}
