package com.ncr;

/**
 * This class represent an UPB transaction. Transaction is stored in an arrayList for confirmation tu upb
 */

public class UPBTrans {
	private String ean = "";
	private long operationID = 0;
	private int transUPB = 0;
	private boolean isVoid = false;
	private boolean confirmed = false;

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public void setEan(String ean) {
		this.ean = ean;
	}

	public String getEan() {
		return ean;
	}

	public void setOperationID(long opID) {
		operationID = opID;
	}

	public long getOperationID() {
		return operationID;
	}

	public void setTransUPB(int traUpb) {
		transUPB = traUpb;
	}

	public int getTransUpb() {
		return transUPB;
	}

	public void setVoid(boolean flag) {
		isVoid = flag;
	}

	public boolean isVoid() {
		return isVoid;
	}
}
