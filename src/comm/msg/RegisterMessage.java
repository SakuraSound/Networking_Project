package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.ServerInfo;
import server.job.Job;
import data.SearchQuery;

@XmlRootElement(name="register_message")
public class RegisterMessage extends AbstractMessage{
	@XmlElement(name="client")
	private ServerInfo client;
	
	@XmlElement(name="unreg_client")
	private SearchQuery query;

	public ServerInfo get_client(){ return client; }
	public SearchQuery get_query(){ return query; }
	
	public static  RegisterMessage create_message(Job job, ServerInfo client, int priority){
		return new RegisterMessage(job, client, priority);
	}
	
	public static  RegisterMessage create_message(Job job, SearchQuery query, int priority){
		return new RegisterMessage(job, query, priority);
	}
	
	private RegisterMessage(Job job, ServerInfo client, int priority){
		super(job, priority);
		this.client = client;
		this.query = null;
	}
	
	private RegisterMessage(Job job, SearchQuery query, int priority){
		super(job, priority);
		this.client = null;
		this.query = query;
	}
	
	private RegisterMessage(){}
	
}
