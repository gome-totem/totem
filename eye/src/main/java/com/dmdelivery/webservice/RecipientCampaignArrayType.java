
package com.dmdelivery.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * This datatype represents a sequence/array of RecipientCampaignType's.
 * 
 * <p>Java class for RecipientCampaignArrayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecipientCampaignArrayType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="recipientCampaign" type="{http://dmdelivery.com/webservice/}RecipientCampaignType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecipientCampaignArrayType", propOrder = {
    "recipientCampaign"
})
public class RecipientCampaignArrayType {

    protected List<RecipientCampaignType> recipientCampaign;

    /**
     * Gets the value of the recipientCampaign property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recipientCampaign property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecipientCampaign().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RecipientCampaignType }
     * 
     * 
     */
    public List<RecipientCampaignType> getRecipientCampaign() {
        if (recipientCampaign == null) {
            recipientCampaign = new ArrayList<RecipientCampaignType>();
        }
        return this.recipientCampaign;
    }

}
