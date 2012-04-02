package server.job;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import data.SearchQuery;
import data.record.Record;

/**
 * Can be used for either Searching or deleting entries from an AbstractStore
 * @author Haruka
 *
 */
public final class SearchJob extends AbstractJob{

	private final SearchQuery query;
	
	private final List<Record> links;
	private final Record self;

	
	public final List<Record> get_links(){ return links; }
	public final Record get_self(){ return self; }
	public final SearchQuery get_query(){ return query; }
	
	public SearchJob priority_update(){
		return new SearchJob(job_type, query, priority + 1, inet, port_num);
	}
	
	public static SearchJob spawn(final Job type, final SearchQuery query, final int priority, final InetAddress inet, final int port_num){
		return new SearchJob(type, query, priority, inet, port_num);
	}
	
	public static SearchJob spawn(final Job type, final SearchQuery query, final int priority, final InetAddress inet, final int port_num, final Record self, final List<Record> links){
		return new SearchJob(type, query, priority, inet, port_num, self, links);
	}
	
	private SearchJob(final Job type, final SearchQuery query, final int priority, final InetAddress inet, final int port_num){
		super(type, priority, inet, port_num);
		this.query = query;
		this.links = new ArrayList<Record>();
		this.self = null;
	}
	
	private SearchJob(final Job type, final SearchQuery query, final int priority, final InetAddress inet, final int port_num, final Record self, final List<Record> links){
		super(type, priority, inet, port_num);
		this.query = query;
		this.links = links;
		this.self = self;
	}
}
