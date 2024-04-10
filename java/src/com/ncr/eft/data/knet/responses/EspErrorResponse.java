//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.05.18 at 11:19:40 AM CEST 
//


package com.ncr.eft.data.knet.responses;

import com.ncr.eft.data.knet.responses.all.EspBasicResponse;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
/*@XmlType(name = "", propOrder = {
    "content"
}
)*/
@XmlRootElement(name = "Esp:Error")
public class EspErrorResponse  extends EspBasicResponse {

    @XmlAttribute(name = "TerminalId")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String terminalId;
   /* @XmlAttribute(name = "TransactionId")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String transactionId;*/
    @XmlAttribute(name = "ActionCode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String actionCode;
    @XmlAttribute(name = "ResponseCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String responseCode;
    @XmlAttribute(name = "MessageReasonCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String messageReasonCode;
    @XmlAttribute(name = "Description")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String description;
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

/*
    */
/**
     * Gets the value of the transactionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     *//*

    public String getTransactionId() {
        return transactionId;
    }

    */
/**
     * Sets the value of the transactionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     *//*

    public void setTransactionId(String value) {
        this.transactionId = value;
    }
*/

    /**
     * Gets the value of the actionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActionCode() {
        return actionCode;
    }

    /**
     * Sets the value of the actionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActionCode(String value) {
        this.actionCode = value;
    }

    /**
     * Gets the value of the responseCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Sets the value of the responseCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResponseCode(String value) {
        this.responseCode = value;
    }

    /**
     * Gets the value of the messageReasonCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageReasonCode() {
        return messageReasonCode;
    }

    /**
     * Sets the value of the messageReasonCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageReasonCode(String value) {
        this.messageReasonCode = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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
