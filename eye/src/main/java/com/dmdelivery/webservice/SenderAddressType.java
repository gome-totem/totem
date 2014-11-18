
package com.dmdelivery.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * This datatype represents a sender address.
 * 
 * <p>Java class for SenderAddressType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SenderAddressType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="dmdelivery_address">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="forward_address">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="alias_address" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SenderAddressType", propOrder = {
    "id",
    "dmdeliveryAddress",
    "forwardAddress",
    "aliasAddress"
})
public class SenderAddressType {

    protected int id;
    @XmlElement(name = "dmdelivery_address", required = true)
    protected String dmdeliveryAddress;
    @XmlElement(name = "forward_address", required = true)
    protected String forwardAddress;
    @XmlElement(name = "alias_address")
    protected String aliasAddress;

    /**
     * Gets the value of the id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the dmdeliveryAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDmdeliveryAddress() {
        return dmdeliveryAddress;
    }

    /**
     * Sets the value of the dmdeliveryAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDmdeliveryAddress(String value) {
        this.dmdeliveryAddress = value;
    }

    /**
     * Gets the value of the forwardAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForwardAddress() {
        return forwardAddress;
    }

    /**
     * Sets the value of the forwardAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForwardAddress(String value) {
        this.forwardAddress = value;
    }

    /**
     * Gets the value of the aliasAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAliasAddress() {
        return aliasAddress;
    }

    /**
     * Sets the value of the aliasAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAliasAddress(String value) {
        this.aliasAddress = value;
    }

}
