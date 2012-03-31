package data.record;

import inter.XMLable;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import server.ServerInfo;
import utils.DataUtilities;
import data.InstantMessage;

@XmlSeeAlso({ServerInfo.class, Record.class, InstantMessage.class})
public abstract class AbstractRecord implements XMLable{
	
	@XmlElement(name="name")
	protected String name;
	@XmlElement(name="ip_addr")
	protected String ip_addr;
	@XmlElement(name="port_num")
	protected int port_num;
	@XmlAttribute(name="timestamp")
	protected String timestamp;
	
	
	public String get_name(){ return name;}
	public String get_ip(){ return ip_addr;}
	public int get_port(){ return port_num;}
	public String get_timestamp(){ return timestamp; }
	
	protected static final boolean validate(final String name, final String ip, final int port_num){
		return DataUtilities.valid_ip(ip) &&
			   DataUtilities.valid_name(name) &&
			   DataUtilities.valid_port(port_num);
	}
	
	protected static Object from_bytes(byte[] bytes, Class<? extends AbstractRecord> clazz) throws JAXBException{
		return clazz.cast(DataUtilities.bytes_2_java(bytes, clazz));
	}
	
	public boolean equals(Object record){
		return (record.getClass() == this.getClass())? 
					this.name.equals(this.getClass().cast(record).get_name()) 
						&& this.ip_addr.equals(this.getClass().cast(record).get_ip()):
					false;
	}
	
	public byte[] to_bytes() throws JAXBException{
		return DataUtilities.java_2_bytes(this, this.getClass());
	}
	
	protected AbstractRecord(String name, String ip, int port_num){
		this.name = name;
		this.ip_addr = ip;
		this.port_num = port_num;
		this.timestamp = DataUtilities.get_timestamp();
	}
	
	protected AbstractRecord(String name, String ip, int port_num, String timestamp){
		this.name = name;
		this.ip_addr = ip;
		this.port_num = port_num;
		this.timestamp = timestamp;
	}
	
	protected AbstractRecord(){}
	

}
