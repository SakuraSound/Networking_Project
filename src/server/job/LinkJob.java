package server.job;

import java.net.InetAddress;

public class LinkJob extends AbstractJob {

	private String server_name;
	
	public String get_server_name(){ return server_name; }
	
	public static LinkJob spawn(Job job_type, String server_name, int priority, InetAddress inet, int port_num){
		return new LinkJob(job_type, server_name, priority, inet, port_num);
	}
	
	public LinkJob priority_update() {
		return new LinkJob(job_type, server_name, priority + 2, inet, port_num);
	}
	
	private LinkJob(Job job_type, final String server_name, final int priority, final InetAddress inet, int port_num){
		super(job_type, priority, inet, port_num);
		this.server_name = server_name;
	}

}
