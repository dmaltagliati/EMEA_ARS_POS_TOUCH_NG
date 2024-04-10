package com.ncr.giftcard.olgb.data;

import com.ncr.giftcard.psh.data.CustomTender;

public class CustomTenderOg  extends CustomTender {
	private String pattern;

	public CustomTenderOg(int tenderId, String accountType, boolean accountNumberRequired) {
		super(tenderId, accountType, accountNumberRequired);
	}

	public CustomTenderOg(int tenderId, String accountType, boolean accountNumberRequired, String pattern) {
		super(tenderId, accountType, accountNumberRequired);
		this.pattern = pattern;
	}
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
