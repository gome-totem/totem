
package com.dmdelivery.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * This (return) datatype is used as a return-value when exporting a single mailing.
 * 
 * <p>Java class for MailingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="80"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="subject">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="kind">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="multipart"/>
 *               &lt;enumeration value="plaintext"/>
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
 *         &lt;element name="priority">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;minInclusive value="1"/>
 *               &lt;maxInclusive value="5"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="from_name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="255"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="sender_id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="plaintext_msg">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="template_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="html_msg">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="16777216"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="attachment">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="80"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="optin_confirm_candidate" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="history">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="65536"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="last_mod_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="last_mod_user">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;minLength value="1"/>
 *               &lt;maxLength value="80"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="last_def_sent_date" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="weblink">
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
@XmlType(name = "MailingType", propOrder = {
    "id",
    "name",
    "subject",
    "kind",
    "lang",
    "priority",
    "fromName",
    "senderId",
    "plaintextMsg",
    "templateId",
    "htmlMsg",
    "attachment",
    "optinConfirmCandidate",
    "history",
    "lastModDate",
    "lastModUser",
    "lastDefSentDate",
    "weblink"
})
public class MailingType {

    protected int id;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String subject;
    @XmlElement(required = true)
    protected String kind;
    @XmlElement(required = true)
    protected String lang;
    protected int priority;
    @XmlElement(name = "from_name", required = true)
    protected String fromName;
    @XmlElement(name = "sender_id")
    protected int senderId;
    @XmlElement(name = "plaintext_msg", required = true)
    protected String plaintextMsg;
    @XmlElement(name = "template_id")
    protected Integer templateId;
    @XmlElement(name = "html_msg", required = true)
    protected String htmlMsg;
    @XmlElement(required = true)
    protected String attachment;
    @XmlElement(name = "optin_confirm_candidate")
    protected boolean optinConfirmCandidate;
    @XmlElement(required = true)
    protected String history;
    @XmlElement(name = "last_mod_date", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastModDate;
    @XmlElement(name = "last_mod_user", required = true)
    protected String lastModUser;
    @XmlElement(name = "last_def_sent_date")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastDefSentDate;
    @XmlElement(required = true)
    protected String weblink;

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
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Gets the value of the kind property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKind() {
        return kind;
    }

    /**
     * Sets the value of the kind property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKind(String value) {
        this.kind = value;
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
     * Gets the value of the priority property.
     * 
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     */
    public void setPriority(int value) {
        this.priority = value;
    }

    /**
     * Gets the value of the fromName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * Sets the value of the fromName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromName(String value) {
        this.fromName = value;
    }

    /**
     * Gets the value of the senderId property.
     * 
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Sets the value of the senderId property.
     * 
     */
    public void setSenderId(int value) {
        this.senderId = value;
    }

    /**
     * Gets the value of the plaintextMsg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlaintextMsg() {
        return plaintextMsg;
    }

    /**
     * Sets the value of the plaintextMsg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlaintextMsg(String value) {
        this.plaintextMsg = value;
    }

    /**
     * Gets the value of the templateId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTemplateId() {
        return templateId;
    }

    /**
     * Sets the value of the templateId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTemplateId(Integer value) {
        this.templateId = value;
    }

    /**
     * Gets the value of the htmlMsg property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHtmlMsg() {
        return htmlMsg;
    }

    /**
     * Sets the value of the htmlMsg property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHtmlMsg(String value) {
        this.htmlMsg = value;
    }

    /**
     * Gets the value of the attachment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * Sets the value of the attachment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachment(String value) {
        this.attachment = value;
    }

    /**
     * Gets the value of the optinConfirmCandidate property.
     * 
     */
    public boolean isOptinConfirmCandidate() {
        return optinConfirmCandidate;
    }

    /**
     * Sets the value of the optinConfirmCandidate property.
     * 
     */
    public void setOptinConfirmCandidate(boolean value) {
        this.optinConfirmCandidate = value;
    }

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHistory() {
        return history;
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHistory(String value) {
        this.history = value;
    }

    /**
     * Gets the value of the lastModDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastModDate() {
        return lastModDate;
    }

    /**
     * Sets the value of the lastModDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastModDate(XMLGregorianCalendar value) {
        this.lastModDate = value;
    }

    /**
     * Gets the value of the lastModUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastModUser() {
        return lastModUser;
    }

    /**
     * Sets the value of the lastModUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastModUser(String value) {
        this.lastModUser = value;
    }

    /**
     * Gets the value of the lastDefSentDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastDefSentDate() {
        return lastDefSentDate;
    }

    /**
     * Sets the value of the lastDefSentDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastDefSentDate(XMLGregorianCalendar value) {
        this.lastDefSentDate = value;
    }

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

}
