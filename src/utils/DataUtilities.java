package utils;
import inter.XMLable;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class DataUtilities {
	
	private static final String IP_PATTERN = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	   private static final String WILD_IP_PATTERN = 
	            "^([01*]?[\\d*]{1,2}?|2[0-4*][\\d*]| 25[0-5*])\\." +
	            "([01*]?[\\d*]{1,2}?|2[0-4*][\\d*]| 25[0-5*])\\." +
	            "([01*]?[\\d*]{1,2}?|2[0-4*][\\d*]| 25[0-5*])\\." +
	            "([01*]?[\\d*]{1,2}?|2[0-4*][\\d*]| 25[0-5*])$";
	
	/**
	 * Validates an ip address
	 * @param ip the address to be validated
	 * @return true if valid, false otherwise
	 */
	public static final boolean valid_ip(final String ip){
		Pattern pattern = Pattern.compile(IP_PATTERN);
	    Matcher matcher = pattern.matcher(ip);
	    return matcher.matches(); 
	}
	
	public static final boolean valid_wild_ip(final String wild_ip){
	    Pattern pattern = Pattern.compile(WILD_IP_PATTERN);
        Matcher matcher = pattern.matcher(wild_ip);
        return matcher.matches(); 
	}
	
	/**
	 * Validates the port number received. valid port numbers are greater than 1023 and less than 65536
	 * @param port_num the port number we are validating
	 * @return true if valid, false otherwise
	 */
	public static final boolean valid_port(final int port_num){
		return port_num > 1023 && port_num < 65536;
	}
	
	/**
	 * Validates the name of the record. A valid record is between 1 and 80 chracters long
	 * @param name the name we want to validate
	 * @return true if valid, false otherwise
	 */
	public static final boolean valid_name(final String name){
		return name.length() > 0 && name.length() < 81;
	}
	
	/**
	 * Creates a timestamp with the current datetime
	 * @return String representation of datetime as YYYY-MM-DD HH:mm:ss
	 */
	public static final String get_timestamp(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(new Date());
	}
	
	/**
	 * Converts a searchquery text into lucene readable text
	 * @param query the searchquery to translate
	 * @return lucene readable query
	 */
    public static String search_2_lucene(String query){
        return query.replaceAll("\\*", "?");
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
	public static final XMLable bytes_2_java(final byte[] bytes, Class<?> clazz) throws JAXBException{
		try{
    		String xml = new String(bytes).trim();
    		JAXBContext context = JAXBContext.newInstance(clazz);
    		Unmarshaller u = context.createUnmarshaller();
    		return (XMLable) clazz.cast(u.unmarshal(new ByteArrayInputStream(xml.getBytes())));
		}catch(JAXBException jaxbe){
			jaxbe.printStackTrace();
			throw jaxbe;
		}
	}
	
}
