package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Performance")
public class Performance {


    private String startDateTime;

    private String endDateTime;

    @XmlAttribute(name = "StartDateTime")
    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    @XmlAttribute(name = "EndDateTime")
    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
}
