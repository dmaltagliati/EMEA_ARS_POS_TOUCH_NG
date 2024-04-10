package com.ncr.eft.data.knet.responses.all;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Esp:Admin")
public class EspAdminResponse extends EspBasicResponse {
    @XmlAttribute(name = "TerminalId", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String terminalId;
    @XmlAttribute(name = "Action", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String action;
    @XmlAttribute(name = "ActionCode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String actionCode;
    @XmlAttribute(name = "MessageReasonCode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String messageReasonCode;

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
     * Gets the value of the action property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAction(String value) {
        this.action = value;
    }

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
}
