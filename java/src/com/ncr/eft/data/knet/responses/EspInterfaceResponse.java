//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.05.18 at 11:19:40 AM CEST 
//


package com.ncr.eft.data.knet.responses;


import com.ncr.eft.data.knet.responses.all.EspEventResponse;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
/*@XmlType(name = "", propOrder = {
    "espAdminOrEspErrorOrEspTransactionOrEspEvent"
}
)*/
@XmlRootElement(name = "Esp:Interface", namespace = "http://www.mosaicsoftware.com/Postilion/eSocket.POS/")
public class EspInterfaceResponse {

    @XmlAttribute(name = "Version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "xmlns:Esp")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlnsEsp;

   /* @XmlElements({
        @XmlElement(name = "Esp:Admin", required = true, type = EspAdminResponse.class),
        @XmlElement(name = "Esp:Error", required = true, type = EspErrorResponse.class),
        @XmlElement(name = "Esp:Transaction", required = true, type = EspTransactionResponse.class),
        @XmlElement(name = "Esp:Event", required = true, type = EspEventResponse.class)
    })
    protected List<EspBasicResponse> espErrors;*/


    @XmlElement(name = "Esp:Error", required = true, type = EspErrorResponse.class)
    protected List<EspErrorResponse> espErrors;
    @XmlElement(name = "Esp:Admin", required = true, type = EspAdminResponse.class)
    protected List<EspAdminResponse> espAdmins;


    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the xmlnsEsp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlnsEsp() {
        if (xmlnsEsp == null) {
            return "http://www.mosaicsoftware.com/Postilion/eSocket.POS/";
        } else {
            return xmlnsEsp;
        }
    }

    /**
     * Sets the value of the xmlnsEsp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlnsEsp(String value) {
        this.xmlnsEsp = value;
    }

    /**
     * Gets the value of the espAdminOrEspErrorOrEspTransactionOrEspInquiryOrEspCheckOrEspMerchandiseOrEspCallbackOrEspEventOrEspReconciliationOrEspNetwork property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the espAdminOrEspErrorOrEspTransactionOrEspInquiryOrEspCheckOrEspMerchandiseOrEspCallbackOrEspEventOrEspReconciliationOrEspNetwork property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEspAdminOrEspErrorOrEspTransactionOrEspInquiryOrEspCheckOrEspMerchandiseOrEspCallbackOrEspEventOrEspReconciliationOrEspNetwork().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EspAdminResponse }
     * {@link EspErrorResponse }
     * {@link EspTransactionResponse }
     * {@link EspEventResponse }
     * 
     * 
     */
    /*public List<EspBasicResponse> getEspRegisters() {
        if (espRegisters == null) {
            espRegisters = new ArrayList<EspBasicResponse>();
        }
        return this.espRegisters;
    }*/

    public List<EspErrorResponse> getEspErrors() {
        return espErrors;
    }

    public void setEspErrors(List<EspErrorResponse> espErrors) {
        this.espErrors = espErrors;
    }

    public List<EspAdminResponse> getEspAdmins() {
        return espAdmins;
    }

    public void setEspAdmins(List<EspAdminResponse> espAdmins) {
        this.espAdmins = espAdmins;
    }
}
