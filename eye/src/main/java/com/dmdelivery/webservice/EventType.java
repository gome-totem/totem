
package com.dmdelivery.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * This (return) datatype is used as a return-value when returning Event data.
 * 
 * <p>Java class for EventType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="campaign_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="template_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="80"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="title">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="80"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="lang">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;length value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="close_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="nr_seats" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="show_fields">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="extras" type="{http://dmdelivery.com/webservice/}EventExtraType" maxOccurs="unbounded"/>
 *         &lt;element name="show_decline" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="max_attendees" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="accept_mailing_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="accept_sms_mailing_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="text_open">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="text_full">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="text_after">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="text_accept">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="text_decline">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="DMDgid_accept" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="DMDgid_decline" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventType", propOrder = {
    "id",
    "campaignId",
    "templateId",
    "name",
    "title",
    "lang",
    "closeDate",
    "nrSeats",
    "showFields",
    "extras",
    "showDecline",
    "maxAttendees",
    "acceptMailingId",
    "acceptSmsMailingId",
    "textOpen",
    "textFull",
    "textAfter",
    "textAccept",
    "textDecline",
    "dmDgidAccept",
    "dmDgidDecline"
})
public class EventType {

    protected int id;
    @XmlElement(name = "campaign_id")
    protected int campaignId;
    @XmlElement(name = "template_id")
    protected int templateId;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    protected String lang;
    @XmlElement(name = "close_date", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar closeDate;
    @XmlElement(name = "nr_seats")
    protected int nrSeats;
    @XmlElement(name = "show_fields", required = true)
    protected String showFields;
    @XmlElement(required = true)
    protected List<EventExtraType> extras;
    @XmlElement(name = "show_decline")
    protected int showDecline;
    @XmlElement(name = "max_attendees")
    protected int maxAttendees;
    @XmlElement(name = "accept_mailing_id")
    protected Integer acceptMailingId;
    @XmlElement(name = "accept_sms_mailing_id")
    protected Integer acceptSmsMailingId;
    @XmlElement(name = "text_open", required = true)
    protected String textOpen;
    @XmlElement(name = "text_full", required = true)
    protected String textFull;
    @XmlElement(name = "text_after", required = true)
    protected String textAfter;
    @XmlElement(name = "text_accept", required = true)
    protected String textAccept;
    @XmlElement(name = "text_decline", required = true)
    protected String textDecline;
    @XmlElement(name = "DMDgid_accept")
    protected Integer dmDgidAccept;
    @XmlElement(name = "DMDgid_decline")
    protected Integer dmDgidDecline;

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
     * Gets the value of the templateId property.
     * 
     */
    public int getTemplateId() {
        return templateId;
    }

    /**
     * Sets the value of the templateId property.
     * 
     */
    public void setTemplateId(int value) {
        this.templateId = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLang() {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLang(String value) {
        this.lang = value;
    }

    /**
     * Gets the value of the closeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCloseDate() {
        return closeDate;
    }

    /**
     * Sets the value of the closeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCloseDate(XMLGregorianCalendar value) {
        this.closeDate = value;
    }

    /**
     * Gets the value of the nrSeats property.
     * 
     */
    public int getNrSeats() {
        return nrSeats;
    }

    /**
     * Sets the value of the nrSeats property.
     * 
     */
    public void setNrSeats(int value) {
        this.nrSeats = value;
    }

    /**
     * Gets the value of the showFields property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShowFields() {
        return showFields;
    }

    /**
     * Sets the value of the showFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShowFields(String value) {
        this.showFields = value;
    }

    /**
     * Gets the value of the extras property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extras property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtras().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EventExtraType }
     * 
     * 
     */
    public List<EventExtraType> getExtras() {
        if (extras == null) {
            extras = new ArrayList<EventExtraType>();
        }
        return this.extras;
    }

    /**
     * Gets the value of the showDecline property.
     * 
     */
    public int getShowDecline() {
        return showDecline;
    }

    /**
     * Sets the value of the showDecline property.
     * 
     */
    public void setShowDecline(int value) {
        this.showDecline = value;
    }

    /**
     * Gets the value of the maxAttendees property.
     * 
     */
    public int getMaxAttendees() {
        return maxAttendees;
    }

    /**
     * Sets the value of the maxAttendees property.
     * 
     */
    public void setMaxAttendees(int value) {
        this.maxAttendees = value;
    }

    /**
     * Gets the value of the acceptMailingId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAcceptMailingId() {
        return acceptMailingId;
    }

    /**
     * Sets the value of the acceptMailingId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAcceptMailingId(Integer value) {
        this.acceptMailingId = value;
    }

    /**
     * Gets the value of the acceptSmsMailingId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAcceptSmsMailingId() {
        return acceptSmsMailingId;
    }

    /**
     * Sets the value of the acceptSmsMailingId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAcceptSmsMailingId(Integer value) {
        this.acceptSmsMailingId = value;
    }

    /**
     * Gets the value of the textOpen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextOpen() {
        return textOpen;
    }

    /**
     * Sets the value of the textOpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextOpen(String value) {
        this.textOpen = value;
    }

    /**
     * Gets the value of the textFull property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextFull() {
        return textFull;
    }

    /**
     * Sets the value of the textFull property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextFull(String value) {
        this.textFull = value;
    }

    /**
     * Gets the value of the textAfter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextAfter() {
        return textAfter;
    }

    /**
     * Sets the value of the textAfter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextAfter(String value) {
        this.textAfter = value;
    }

    /**
     * Gets the value of the textAccept property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextAccept() {
        return textAccept;
    }

    /**
     * Sets the value of the textAccept property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextAccept(String value) {
        this.textAccept = value;
    }

    /**
     * Gets the value of the textDecline property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTextDecline() {
        return textDecline;
    }

    /**
     * Sets the value of the textDecline property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTextDecline(String value) {
        this.textDecline = value;
    }

    /**
     * Gets the value of the dmDgidAccept property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDMDgidAccept() {
        return dmDgidAccept;
    }

    /**
     * Sets the value of the dmDgidAccept property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDMDgidAccept(Integer value) {
        this.dmDgidAccept = value;
    }

    /**
     * Gets the value of the dmDgidDecline property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDMDgidDecline() {
        return dmDgidDecline;
    }

    /**
     * Sets the value of the dmDgidDecline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDMDgidDecline(Integer value) {
        this.dmDgidDecline = value;
    }

}
