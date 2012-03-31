package server.job;

import java.net.InetAddress;

import data.SearchQuery;

/**
 * Can be used for either Searching or deleting entries from an AbstractStore
 * @author Haruka
 *
 */
public final class SearchJob extends AbstractJob{

	private final SearchQuery query;

	public final SearchQuery get_query(){ return query; }
	
	public SearchJob priority_update(){
		return new SearchJob(job_type, query, priority + 1, inet, port_num);
	}
	
	public static SearchJob spawn(final Job type, final SearchQuery query, final int priority, final InetAddress inet, final int port_num){
		return new SearchJob(type, query, priority, inet, port_num);
	}
	
	private SearchJob(final Job type, final SearchQuery query, final int priority, final InetAddress inet, final int port_num){
		super(type, priority, inet, port_num);
		this.query = query;
	}
}
