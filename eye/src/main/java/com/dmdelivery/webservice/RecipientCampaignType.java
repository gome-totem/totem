
package com.dmdelivery.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * This datatype is used as a return value when retrieving the overall campaigns a recipient is member of.
 * 
 * <p>Java class for RecipientCampaignType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecipientCampaignType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="campaign_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="group_ids" type="{http://dmdelivery.com/webservice/}ArrayOfIntType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecipientCampaignType", propOrder = {
    "campaignId",
    "groupIds"
})
public class RecipientCampaignType {

    @XmlElement(name = "campaign_id")
    protected int campaignId;
    @XmlElement(name = "group_ids")
    protected ArrayOfIntType groupIds;

    /**
     * Gets the value of the campaignId property.
     * 
     */
    public int getCampaignId() {
        return campaignId;
    }

    /**
     * Sets the value of the campaignId property.
     * 
     */
    public void setCampaignId(int value) {
        this.campaignId = value;
    }

    /**
     * Gets the value of the groupIds property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfIntType }
     *     
     */
    public ArrayOfIntType getGroupIds() {
        return groupIds;
    }

    /**
     * Sets the value of the groupIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfIntType }
     *     
     */
    public void setGroupIds(ArrayOfIntType value) {
        this.groupIds = value;
    }

}
