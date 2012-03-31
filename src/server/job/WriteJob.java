package server.job;

import java.net.InetAddress;
import java.util.List;

import data.record.AbstractRecord;
import data.record.Record;

public final class WriteJob<T extends AbstractRecord> extends AbstractJob {

	private final T record;
	
	public final T get_record(){ return record; }
	
	private final Record from;
	
	private final List<Record> links;
	
	
	public WriteJob<T> priority_update() {
		return new WriteJob<T>(job_type, record, priority+1, inet, port_num);
	}
	
	public Record get_from(){ return from; }
	
	public List<Record> get_links(){ return links; }
	
	public static <S extends AbstractRecord> WriteJob<S> spawn(final Job type, final S record, final int priority, final InetAddress inet, final int port_num){
		return new WriteJob<S>(type, record, priority, inet, port_num);
	}
	
	private WriteJob(final Job type, final T record, final int priority, final InetAddress inet, final int port_num){
		super(type, priority, inet, port_num);
		this.record = record;
		from = null;
		links = null;
	}
	
	private WriteJob(final Job type, final T record, final int priority, final InetAddress inet, final int port_num, Record from, List<Record> links){
		super(type, priority, inet, port_num);
		this.record = record;
		from = null;
		links = null;
		this.from = from;
		this.links = links;
	}
	
}
