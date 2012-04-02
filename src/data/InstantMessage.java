package data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import utils.CommUtilities;

import data.record.AbstractRecord;

@XmlRootElement(name="instant_message")
public final class InstantMessage extends AbstractRecord {
	@XmlElement(name="to")
	private String handle;
	@XmlElement(name="message")
	private String message;
	@XmlAttribute(name="timestamp")
	private String timestamp;
	@XmlAttribute(name="server")
	private String server;
	
	
	public String get_server(){ return server; }
	public String get_handle(){ return handle; }
	public String get_message(){ return message; }
	public String get_timestamp(){ return timestamp; }
	
	public static InstantMessage compose(String name, String server, String ip_addr, int port_num, String message){
		return new InstantMessage(name, server, ip_addr, port_num, message);
	}
	
	private InstantMessage(){}
	
	private InstantMessage(String name, String server, String ip_addr, int port_num, String message){
		super(name, ip_addr, port_num);
		this.message = message;
		this.server = server;
		this.timestamp = CommUtilities.get_timestamp();
	}
}
