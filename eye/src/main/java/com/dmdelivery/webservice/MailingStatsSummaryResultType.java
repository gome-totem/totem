
package com.dmdelivery.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * This datatype represents a single Summary Statistics record.
 * 
 * <p>Java class for MailingStatsSummaryResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailingStatsSummaryResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="weblink">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="total_sent" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="total_accepted" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hardbounces" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="softbounces" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unsubscribers" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="suspends" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="spamcomplaints" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unique_opens" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="total_renders" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unique_renders" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="total_clickthroughs" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unique_clickthroughs" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unique_link_clickthroughs" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="total_conversion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="unique_conversion" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="click2open_rate" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailingStatsSummaryResultType", propOrder = {
    "weblink",
    "totalSent",
    "totalAccepted",
    "hardbounces",
    "softbounces",
    "unsubscribers",
    "suspends",
    "spamcomplaints",
    "uniqueOpens",
    "totalRenders",
    "uniqueRenders",
    "totalClickthroughs",
    "uniqueClickthroughs",
    "uniqueLinkClickthroughs",
    "totalConversion",
    "uniqueConversion",
    "click2OpenRate"
})
public class MailingStatsSummaryResultType {

    @XmlElement(required = true)
    protected String weblink;
    @XmlElement(name = "total_sent")
    protected int totalSent;
    @XmlElement(name = "total_accepted")
    protected int totalAccepted;
    protected int hardbounces;
    protected int softbounces;
    protected int unsubscribers;
    protected int suspends;
    protected int spamcomplaints;
    @XmlElement(name = "unique_opens")
    protected int uniqueOpens;
    @XmlElement(name = "total_renders")
    protected int totalRenders;
    @XmlElement(name = "unique_renders")
    protected int uniqueRenders;
    @XmlElement(name = "total_clickthroughs")
    protected int totalClickthroughs;
    @XmlElement(name = "unique_clickthroughs")
    protected int uniqueClickthroughs;
    @XmlElement(name = "unique_link_clickthroughs")
    protected int uniqueLinkClickthroughs;
    @XmlElement(name = "total_conversion")
    protected int totalConversion;
    @XmlElement(name = "unique_conversion")
    protected int uniqueConversion;
    @XmlElement(name = "click2open_rate")
    protected float click2OpenRate;

    /**
     * Gets the value of the weblink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWeblink() {
        return weblink;
    }

    /**
     * Sets the value of the weblink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWeblink(String value) {
        this.weblink = value;
    }

    /**
     * Gets the value of the totalSent property.
     * 
     */
    public int getTotalSent() {
        return totalSent;
    }

    /**
     * Sets the value of the totalSent property.
     * 
     */
    public void setTotalSent(int value) {
        this.totalSent = value;
    }

    /**
     * Gets the value of the totalAccepted property.
     * 
     */
    public int getTotalAccepted() {
        return totalAccepted;
    }

    /**
     * Sets the value of the totalAccepted property.
     * 
     */
    public void setTotalAccepted(int value) {
        this.totalAccepted = value;
    }

    /**
     * Gets the value of the hardbounces property.
     * 
     */
    public int getHardbounces() {
        return hardbounces;
    }

    /**
     * Sets the value of the hardbounces property.
     * 
     */
    public void setHardbounces(int value) {
        this.hardbounces = value;
    }

    /**
     * Gets the value of the softbounces property.
     * 
     */
    public int getSoftbounces() {
        return softbounces;
    }

    /**
     * Sets the value of the softbounces property.
     * 
     */
    public void setSoftbounces(int value) {
        this.softbounces = value;
    }

    /**
     * Gets the value of the unsubscribers property.
     * 
     */
    public int getUnsubscribers() {
        return unsubscribers;
    }

    /**
     * Sets the value of the unsubscribers property.
     * 
     */
    public void setUnsubscribers(int value) {
        this.unsubscribers = value;
    }

    /**
     * Gets the value of the suspends property.
     * 
     */
    public int getSuspends() {
        return suspends;
    }

    /**
     * Sets the value of the suspends property.
     * 
     */
    public void setSuspends(int value) {
        this.suspends = value;
    }

    /**
     * Gets the value of the spamcomplaints property.
     * 
     */
    public int getSpamcomplaints() {
        return spamcomplaints;
    }

    /**
     * Sets the value of the spamcomplaints property.
     * 
     */
    public void setSpamcomplaints(int value) {
        this.spamcomplaints = value;
    }

    /**
     * Gets the value of the uniqueOpens property.
     * 
     */
    public int getUniqueOpens() {
        return uniqueOpens;
    }

    /**
     * Sets the value of the uniqueOpens property.
     * 
     */
    public void setUniqueOpens(int value) {
        this.uniqueOpens = value;
    }

    /**
     * Gets the value of the totalRenders property.
     * 
     */
    public int getTotalRenders() {
        return totalRenders;
    }

    /**
     * Sets the value of the totalRenders property.
     * 
     */
    public void setTotalRenders(int value) {
        this.totalRenders = value;
    }

    /**
     * Gets the value of the uniqueRenders property.
     * 
     */
    public int getUniqueRenders() {
        return uniqueRenders;
    }

    /**
     * Sets the value of the uniqueRenders property.
     * 
     */
    public void setUniqueRenders(int value) {
        this.uniqueRenders = value;
    }

    /**
     * Gets the value of the totalClickthroughs property.
     * 
     */
    public int getTotalClickthroughs() {
        return totalClickthroughs;
    }

    /**
     * Sets the value of the totalClickthroughs property.
     * 
     */
    public void setTotalClickthroughs(int value) {
        this.totalClickthroughs = value;
    }

    /**
     * Gets the value of the uniqueClickthroughs property.
     * 
     */
    public int getUniqueClickthroughs() {
        return uniqueClickthroughs;
    }

    /**
     * Sets the value of the uniqueClickthroughs property.
     * 
     */
    public void setUniqueClickthroughs(int value) {
        this.uniqueClickthroughs = value;
    }

    /**
     * Gets the value of the uniqueLinkClickthroughs property.
     * 
     */
    public int getUniqueLinkClickthroughs() {
        return uniqueLinkClickthroughs;
    }

    /**
     * Sets the value of the uniqueLinkClickthroughs property.
     * 
     */
    public void setUniqueLinkClickthroughs(int value) {
        this.uniqueLinkClickthroughs = value;
    }

    /**
     * Gets the value of the totalConversion property.
     * 
     */
    public int getTotalConversion() {
        return totalConversion;
    }

    /**
     * Sets the value of the totalConversion property.
     * 
     */
    public void setTotalConversion(int value) {
        this.totalConversion = value;
    }

    /**
     * Gets the value of the uniqueConversion property.
     * 
     */
    public int getUniqueConversion() {
        return uniqueConversion;
    }

    /**
     * Sets the value of the uniqueConversion property.
     * 
     */
    public void setUniqueConversion(int value) {
        this.uniqueConversion = value;
    }

    /**
     * Gets the value of the click2OpenRate property.
     * 
     */
    public float getClick2OpenRate() {
        return click2OpenRate;
    }

    /**
     * Sets the value of the click2OpenRate property.
     * 
     */
    public void setClick2OpenRate(float value) {
        this.click2OpenRate = value;
    }

}
