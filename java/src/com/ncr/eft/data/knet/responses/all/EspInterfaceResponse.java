
package com.ncr.eft.data.knet.responses.all;


import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "")
@XmlRootElement(name = "Interface", namespace = "http://www.mosaicsoftware.com/Postilion/eSocket.POS/")
public class EspInterfaceResponse {
    @XmlAttribute(name = "Version", required = true)
    //@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/" ,name = "xmlns:Esp")
    protected String xmlnsEsp;
    //@XmlElement(name = "Error")
    //@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlElements({
            @XmlElement(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/",name = "Admin", required = true, type = EspAdminResponse.class),
            @XmlElement(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/",name = "Error", required = true, type = EspErrorResponse.class),
            @XmlElement(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/",name = "Transaction", required = true, type = EspTransactionResponse.class)//,
            //@XmlElement(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/",name = "Event", required = true, type = EspEventResponse.class)
    })
    protected EspBasicResponse espElement;

    //protected EspErrorResponse error;


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
    //@XmlAttribute(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/" ,name = "xmlns:Esp")

    public void setXmlnsEsp(String value) {
        this.xmlnsEsp = value;
    }

   /* public com.ncr.eft.data.knet.data.responses.all.EspErrorResponse getEspErrorResponse() {
        return error;
    }
    @XmlElement(namespace="http://www.mosaicsoftware.com/Postilion/eSocket.POS/", name="Error")

    public void setEspErrorResponse(com.ncr.eft.data.knet.data.responses.all.EspErrorResponse espErrorResponse) {
        error = espErrorResponse;
    }*/

    public EspBasicResponse getEspElement() {
        return espElement;
    }

    public void setEspElement(EspBasicResponse espElement) {
        this.espElement = espElement;
    }
}
