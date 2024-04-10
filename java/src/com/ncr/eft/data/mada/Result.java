package com.ncr.eft.data.mada;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Result")
public class Result {

    private String arabic = "";
    private String english = "";

    @XmlAttribute(name = "Arabic")
    public String getArabic() {
        return arabic;
    }

    public void setArabic(String arabic) {
        this.arabic = arabic;
    }

    @XmlAttribute(name = "English")
    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }
}
