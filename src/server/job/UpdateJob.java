package server.job;

import java.net.InetAddress;
import java.util.List;

import server.ServerInfo;
import data.record.Record;

public class UpdateJob extends AbstractJob {

	private ServerInfo client;
	private List<Record> links;
	private String uuid;
	
	
	public ServerInfo get_client(){ return client; }
	public List<Record> get_links(){ return links; }
	public String get_uuid(){ return uuid; }
	
	
	public UpdateJob priority_update() {
		return new UpdateJob(job_type, client, links, uuid, 10, inet, port_num);
	}
	
	public static UpdateJob spawn(Job job_type, ServerInfo client, List<Record> links, String uuid, int priority){
		return new UpdateJob(job_type, client, links, uuid, priority, null, 0);
	}
	
	private UpdateJob(Job job_type, ServerInfo client, List<Record> links, String uuid, int priority, InetAddress inet, int port_num){
		super(job_type, priority, inet, port_num);
		this.links = links;
		this.uuid = uuid;
	}

}
