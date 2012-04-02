package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.job.Job;


@XmlRootElement(name="link_message")
public final class LinkMessage extends AbstractMessage {
	@XmlElement(name="server_name")
	private String server_name;
	
	public String get_server_name(){ return server_name; }

	public static LinkMessage create_message(Job type ,String server_name, int priority){
		return new LinkMessage(type, server_name, priority);
	}
	
	private LinkMessage(Job type, String server_name, int priority){
		super(type, priority);
		this.server_name = server_name;
	}
	
	private LinkMessage(){}
}
