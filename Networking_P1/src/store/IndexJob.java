package store;

import static java.lang.System.currentTimeMillis;
import static java.lang.Math.signum;

import java.net.InetAddress;

import store.StoreUtilities.Job;
import data.Record;
import data.SearchQuery;

public class IndexJob implements Comparable<IndexJob>{
    // Parameters specific to actual job
	private Job job_type;
	private Record record;
	private SearchQuery query; 
	private final int priority;
	private final long timestamp;
	
	
	// Parameters specific to origin of job
	private final InetAddress inet;
	private final int port_num;
	
	
	// *** GETTERS ****
	public Job get_job(){ return job_type; }
	public Record get_record(){ return record; }	
	public SearchQuery get_query(){ return query; }
	public int get_priority(){ return priority; }
	public long get_timestamp(){ return timestamp; }
	
	public InetAddress get_inet(){ return inet; }
	public int get_port(){ return port_num; }
	
	// *** STATIC FACTORY METHODS ***
	
	/**
	 * Creates either a search job, or a delete job, based on message information. Assigns default
	 * priority value (1)
	 * @param query The query to run against the index
	 * @param is_delete true if it is a deletion job, false if search job
	 * @return new index job object to wait in the RecordStore instance
	 */
	public static IndexJob create_job(SearchQuery query, boolean is_delete, InetAddress inet, int port_num){
		return create_job(query, is_delete, 1, inet, port_num);
	}
	
	/**
	 * Creates either a search job, or a delete job, based on message information and priority
	 * @param query The query to run against the index
	 * @param is_delete true if it is a deletion job, false otherwise
	 * @param priority how important this operation is 
	 * @return new index job to wait in the RecordStore 
	 */
	public static IndexJob create_job(SearchQuery query, boolean is_delete, int priority, InetAddress inet, int port_num){
		return new IndexJob(query, is_delete, priority, inet, port_num);
	}
	
	
	
	/**
	 * Creates a write job, to add a new record
	 * @param record the record that we want to add to our RecordStore
	 * @return new index job to wait in the RecordStore 
	 */
	public static IndexJob create_job(Record record, InetAddress inet, int port_num){
		return create_job(record, 1, inet, port_num);
	}
	
	/**
	 * Creates a write job, to add a new record, with priority information
	 * @param record the record that we want to add to our RecordStore
	 * @param priority how important this operation is
	 * @return new index job to wait in the RecordStore 
	 */
	public static IndexJob create_job(Record record, int priority, InetAddress inet, int port_num){
		return new IndexJob(record, priority, inet, port_num);
	}
	/**
	 * Create a shutdown job
	 * @param priority
	 * @param inet
	 * @param port_num
	 * @return
	 */
	public static IndexJob create_job(int priority, InetAddress inet, int port_num){
	    return new IndexJob(priority, inet, port_num);
	}
	
	// *** PRIVATE CONSTRUCTORS *** 
	
	private IndexJob(int priority, InetAddress inet, int port_num){
	    this.job_type = Job.SHUT_DOWN;
	    this.priority = priority;
	    this.inet = inet;
	    this.port_num = port_num;
	    this.timestamp = currentTimeMillis();
	}
	
	private IndexJob(SearchQuery query, boolean is_delete, int priority, InetAddress inet, int port_num){
		this.query = query;
		this.job_type = (is_delete)? Job.DELETE : Job.READ;
		this.priority = (priority > 10)? 10:
		                (priority < 1)? 1: priority;
		this.timestamp = currentTimeMillis();
		this.inet = inet;
		this.port_num = port_num;
	}
	
	private IndexJob(Record record, int priority, InetAddress inet, int port_num){
		this.record = record;
		this.job_type = Job.WRITE;
		this.priority = (priority > 10)? 10:
                        (priority < 1)? 1: priority;
		this.timestamp = currentTimeMillis();
		this.inet = inet;
		this.port_num = port_num;
	}

	public int compareTo(IndexJob job) {
		/* answer = sign(time_diff - 150*priority_diff)
		 * If job is younger, than we already start with a negative number
		 * then we subtract the priority difference * 150ms and take the sign
		 */
		int time_diff = (int) ( this.timestamp- job.get_timestamp());
		int priority_diff = this.priority - job.get_priority();
		return (int) signum(time_diff - 50*priority_diff);
	}
	
	public static IndexJob get_refresh_job(IndexJob job, boolean is_delete){
	    
	    return is_delete? new IndexJob(job.get_query(), true, job.get_priority() + 1, job.get_inet(), job.get_port()):
	                      new IndexJob(job.get_record(), job.get_priority() + 1, job.get_inet(), job.get_port());
	}
	
}
