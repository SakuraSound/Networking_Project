package server.store.task;

import java.net.SocketException;
import java.util.UUID;

import server.job.WriteJob;
import server.store.RecordStore;
import server.task.AbstractWriteTask;
import data.record.Record;

public class StoreWriteTask extends AbstractWriteTask<Record>{

    
    public static StoreWriteTask spawn(final WriteJob<Record> job, final RecordStore store) throws SocketException{
        return new StoreWriteTask(job, store);
    }
    
    protected void run_task(Record record) { store.add_record(record); }
    
    private StoreWriteTask(final WriteJob<Record> job, final RecordStore store) throws SocketException{
        super(job, store);
        setName("Write Task: ("+ store.get_name() +") "+UUID.randomUUID().toString());
    }
    
    @SuppressWarnings("unchecked")
	public boolean found_duplicate(){
    	WriteJob<Record> wjob = (WriteJob<Record>) job;
        searcher = store.get_reader();
        while(searcher.hasMoreElements()){
            Record record = searcher.nextElement();
            if(wjob.get_record().equals(record))
                return true;
        }
        return false;
    }

	
    
    
}
