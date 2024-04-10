package com.ncr;

public class ReceiptMessage {
    private int area;
    private int sequence;
    private String text;

    public ReceiptMessage(int area, int sequence, String text) {
        this.area = area;
        this.sequence = sequence;
        this.text = text;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
