package com.ncr.eft.data.apex;

public class SaleDcc {
    private String indicator;
    private String chca;
    private String chcn;
    private String comm;
    private String markup;
    private String exch;

    public SaleDcc() {
    }

    public String getIndicator() {
        return indicator;
    }

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    public String getChca() {
        return chca;
    }

    public void setChca(String chca) {
        this.chca = chca;
    }

    public String getChcn() {
        return chcn;
    }

    public void setChcn(String chcn) {
        this.chcn = chcn;
    }

    public String getComm() {
        return comm;
    }

    public void setComm(String comm) {
        this.comm = comm;
    }

    public String getMarkup() {
        return markup;
    }

    public void setMarkup(String markup) {
        this.markup = markup;
    }

    public String getExch() {
        return exch;
    }

    public void setExch(String exch) {
        this.exch = exch;
    }

    @Override
    public String toString() {
        return "SaleDcc{" +
                "indicator='" + indicator + '\'' +
                ", chca='" + chca + '\'' +
                ", chcn='" + chcn + '\'' +
                ", comm='" + comm + '\'' +
                ", markup='" + markup + '\'' +
                ", exch='" + exch + '\'' +
                '}';
    }
}
