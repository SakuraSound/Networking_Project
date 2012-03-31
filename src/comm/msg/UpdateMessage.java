package comm.msg;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.ServerInfo;
import server.job.Job;
import data.record.Record;

@XmlRootElement(name="UpdateMessage")
public class UpdateMessage extends AbstractMessage {
	
	@XmlElement(name="update_id")
	private String uuid;
	@XmlElement(name="from_store")
	private Record from_store;
	@XmlElement(name="new_client")
	private ServerInfo new_client;
	
	public ServerInfo get_client(){ return new_client; }
	public Record get_origin(){ return from_store; }
	public String get_uuid(){ return uuid; }
	
	
	public static UpdateMessage create_message(Job job_type, ServerInfo client, Record from_store, String uuid){
		return new UpdateMessage(job_type, client, from_store, uuid);
	}
	
	
	private UpdateMessage(Job job_type, ServerInfo client, Record from_store, String uuid){
		super(job_type, 10);
		this.new_client = client;
		this.from_store = from_store;
		this.uuid = uuid == null ? UUID.randomUUID().toString() : uuid;
	}
	
}
