package utils;

import inter.Message;
import inter.XMLable;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

public class CommUtilities {

    
    public static final String get_timestamp(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return formatter.format(new Date());
    }
    
    /**
     * Converts a record object into xml representation
     * @param record the record we want to convert
     * @return xml representation of our record object
     * @throws JAXBException if corruption of object
     */
    public static final byte[] java_2_bytes(final XMLable xmlable, Class<?> clazz) throws JAXBException{
        StringWriter writer = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(clazz);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(xmlable, writer);
        return writer.toString().getBytes();
    }
    
    /**
     * Converts the received xml data to a Record object
     * Object must be of type XMLable
     * @param xml the sent schema (with information)
     * @return new Record object
     * @throws JAXBException if mangled xml data received.
     */
    public static final Message bytes_2_java(final byte[] bytes, Class<?> clazz) throws JAXBException{
        //Trim the trailing zeros from the byte array
        String xml = new String(bytes).trim();
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller u = context.createUnmarshaller();
        return (Message) clazz.cast(u.unmarshal(new ByteArrayInputStream(xml.getBytes())));
    }
    
    
    @XmlType(name="Error")
    @XmlEnum
    public static enum ERROR{
        @XmlEnumValue("invalid query")
        INVALID_QUERY("This query is invalid. Please format appropriately.", "invalid query"),
        @XmlEnumValue("invalid record")
        INVALID_RECORD("An invalid record was sent/detected. Rejecting.", "invalid record"),
        @XmlEnumValue("overwrite error")
        OVERWRITE_ERROR("Similar record exists. Remove that before inserting this one.", "overwrite error"),
        @XmlEnumValue("communication error")
        COMMUNICATION_ERROR("Having problems communicating with recordstore", "communication error"),
        @XmlEnumValue("server side error")
        SERVER_SIDE_ERROR("Issues with the recordstore server.", "server side error"),
        @XmlEnumValue("closed db")
        CLOSED_DB("The database you are accessing is closed", "closed db"),
        @XmlEnumValue("record non existant")
        RECORD_NOT_FOUND("The record you are trying to delete was not found.", "record non existant"),
        @XmlEnumValue("unknown request")
        UNKNOWN_REQUEST("Unknown request received.", "unknown request"),
        @XmlEnumValue("linkage error")
        LINKAGE_ERROR("Unable to perform linkage...", "linkage error"),
        @XmlEnumValue("duplicate link")
        DUPLICATE_LINK_ERROR("Link already exists", "duplicate link");
        
        
        private final String message;
        private final String value;
        
        public String get_message(){ return message; }
        public String get_value(){ return value; }
            
        
        ERROR(final String message, String value){
            this.message = message;
            this.value = value;
        }
        
        public static ERROR from_string(String value){
            for(ERROR e : ERROR.values()){
                if(e.get_value().equals(value)){
                    return e;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }
    
    
}
