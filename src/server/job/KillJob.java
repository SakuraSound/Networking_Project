package server.job;

import java.net.InetAddress;

public final class KillJob extends AbstractJob {

	
	public AbstractJob priority_update() {
		return new KillJob(priority+1, inet, port_num);
	}
	
	public static KillJob spawn(final int priority, final InetAddress inet, final int port_num){
		return new KillJob(priority, inet, port_num);
	}
	
	private KillJob(final int priority, final InetAddress inet, final int port_num){
		super(Job.SHUT_DOWN, priority, inet, port_num);
	}

}
