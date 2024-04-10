package com.ncr;

/**
 * This class is holder of data that is made from Itemdata. This object is used mainly used for the making input data for the TransDataView
 *
 * @author bs230003
 */

public class TransData {
	String text;
	Itemdata itm;
	char type;
	boolean isData;

	public TransData(Itemdata i, String t, char c) {
		itm = i;
		text = t;
		isData = (i == null);
		type = c;
	}

	public TransData(String t) {
		this(null, t, ' ');
	}

	public Itemdata getItemDataObject() {
		return this.itm;
	}

	public char getType() {
		return this.type;
	}

	public boolean isData() {
		return isData;
	}

	public String toString() {
		return new StringBuffer("[").append(text).append(";").append(itm).append("]").toString();
	}
}
