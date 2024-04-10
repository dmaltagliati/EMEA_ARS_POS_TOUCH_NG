package com.ncr.eft.data.knet.responses.all;


import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD )
//@XmlType(name = "")
@XmlRootElement(name = "Esp:Error",namespace = "http://www.mosaicsoftware.com/Postilion/eSocket.POS/")

public class EspErrorResponse extends EspBasicResponse {
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


}
