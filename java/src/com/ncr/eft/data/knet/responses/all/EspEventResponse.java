//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.05.18 at 11:19:40 AM CEST 
//


package com.ncr.eft.data.knet.responses.all;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = ""/*, propOrder = {
    "content"
}*/)
@XmlRootElement(name = "Esp:Event")
public class EspEventResponse extends EspBasicResponse{

    @XmlAttribute(name = "TerminalId", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String terminalId;
    @XmlAttribute(name = "EventId", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String eventId;
    @XmlAttribute(name = "EventData")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String eventData;
    @XmlMixed
    @XmlAnyElement
    protected List<Object> content;

    /**
     * Gets the value of the terminalId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminalId() {
        return terminalId;
    }

    /**
     * Sets the value of the terminalId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminalId(String value) {
        this.terminalId = value;
    }

    /**
     * Gets the value of the eventId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the value of the eventId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventId(String value) {
        this.eventId = value;
    }

    /**
     * Gets the value of the eventData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventData() {
        return eventData;
    }

    /**
     * Sets the value of the eventData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventData(String value) {
        this.eventData = value;
    }

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link String }
     * 
     * 
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
    }

}
