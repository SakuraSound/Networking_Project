package server.job;

import static java.lang.Math.signum;
import static java.lang.System.currentTimeMillis;
import inter.Prioritizable;

import java.net.InetAddress;

public abstract class AbstractJob implements Comparable<AbstractJob>, Prioritizable{
	
	protected final Job job_type;
	protected final int priority;
	protected final long timestamp;
	
	protected final InetAddress inet;
	protected final int port_num;
	
	
	public abstract AbstractJob priority_update();
	
	//** GETTERS **
	public Job get_job(){ return job_type; }
	public final int get_priority(){ return priority; }
	public final long get_timestamp(){ return timestamp; }
	public InetAddress get_inet(){ return inet; }
	public int get_port(){ return port_num; }
	
	public int compareTo(AbstractJob job) {
		/* answer = sign(time_diff - 150*priority_diff)
		 * If job is younger, than we already start with a negative number
		 * then we subtract the priority difference * 150ms and take the sign
		 */
		int time_diff = (int) ( this.timestamp- job.get_timestamp());
		int priority_diff = this.priority - job.get_priority();
		return (int) signum(time_diff - 50*priority_diff);
	}
	
	public static AbstractJob get_refresh_job(AbstractJob job){
		return (AbstractJob) job.priority_update();
	}
	
	protected AbstractJob(final Job type, final int priority, final InetAddress inet, final int port_num){
		this.job_type = type;
		this.priority = (priority > 10)? 10:
            			(priority < 1)? 1: priority;
		this.inet = inet;
		this.port_num = port_num;
		this.timestamp = currentTimeMillis();
	}
}
