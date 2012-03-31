package server.job;

import comm.msg.IMMessage;

public class IMJob extends AbstractJob {

	private final IMMessage message;
	
	public final IMMessage get_message(){ return message; }
	
	public IMJob priority_update(){
		return new IMJob(message, priority + 1);
	}
	
	public static IMJob spawn(final IMMessage message, final int priority){
		return new IMJob(message, priority);
	}
	
	private IMJob(final IMMessage message, final int priority){
		super(Job.SEND_MESSAGE, priority, null, 0);
		this.message = message;
	}
	
}
