package server.store.task;

import java.net.SocketException;
import java.util.UUID;

import server.job.SearchJob;
import server.store.RecordStore;
import server.task.AbstractDeleteTask;
import data.SearchQuery;
import data.record.AbstractRecord;
import data.record.Record;

/**
 * Task responsible for deleting data from a data store
 * @author Hatomi
 *
 */
public class StoreDeleteTask extends AbstractDeleteTask<Record> {

	//TODO: Abstract this out...
    
    
    public static StoreDeleteTask spawn(SearchJob job, RecordStore store) throws SocketException{
        return new StoreDeleteTask(job, store);
    }

    
    private StoreDeleteTask(final SearchJob job, final RecordStore store) throws SocketException{
    	super(job, store);
        setName("Delete Task: ("+store.get_name()+") "+UUID.randomUUID().toString());
    }
    
    protected boolean filter(final SearchQuery query, AbstractRecord record){
    	if(record.get_name().equals(query.get_name())){
            if(query.get_ip() == null && query.get_port() == 0){
                return true;
            }
            if(record.get_ip().equals(query.get_ip())){
                if(query.get_port() == 0 || record.get_port() == query.get_port()){
                    return true;
                }
            }
        }
    	return false;
    }
             
}
