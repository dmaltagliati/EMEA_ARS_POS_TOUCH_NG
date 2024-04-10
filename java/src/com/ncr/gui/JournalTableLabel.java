package com.ncr.gui;

public class JournalTableLabel extends GdLabel {

	public JournalTableLabel(String text) {
		super(text);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5583533733827672608L;

	private int type = 0;

	public boolean isVoid() {
		return type > 1000;
	}

	public int getType() {
		return type % 1000;
	}

	public void setType(int type) {
		this.type = type;
	}

}
