package server.store.task;

import java.net.SocketException;
import java.util.UUID;

import server.job.SearchJob;
import server.store.RecordStore;
import server.task.AbstractSearchTask;
import data.record.Record;

public class StoreReadTask extends AbstractSearchTask<Record>{
    
    
    public static final StoreReadTask spawn(final SearchJob job, final RecordStore store) throws SocketException{
        return new StoreReadTask(job, store);
    }
    
    private StoreReadTask(SearchJob job, RecordStore store) throws SocketException{
    	super(job, store);
        setName("Read Task: "+ UUID.randomUUID().toString());
    }

    
}