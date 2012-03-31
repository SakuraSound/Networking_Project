package server.store;
import static java.lang.System.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import server.job.AbstractJob;
import server.job.SearchJob;
import server.job.WriteJob;
import server.store.task.StoreDeleteTask;
import server.store.task.StoreReadTask;
import server.store.task.StoreWriteTask;
import utils.DataUtilities;
import data.record.InvalidRecordException;
import data.record.Record;

//TODO: Create timer  that calls searchmanager's maybeReopen call to swap indexsearchers every so often...

/**
 * Essentially a database. Can be in-memory, or on the filesystem.
 * @author Hatomi
 *
 */
public class RecordStore extends AbstractStore<Record>{
	
	private String name;
	private CopyOnWriteArrayList<Record> data;
	private File record_store;
	private final boolean persistent;
	
	
	public int get_record_size(){ return data.size(); }
	
	public Enumeration<Record> get_reader(){ return Collections.enumeration(data); }
	
	protected void close(){
	    is_open.set(false);
	    out.printf(" Starting close of %s at %s\n", name, DataUtilities.get_timestamp());
	}
	
	private void write_out() throws IOException{
		if(persistent)
	        write_records_to_store();
	}
	
	
	private final void add_response(final boolean value, Record record){
		 if(value)
			 out.printf("Added new record to %s: %s, %s, %d at %s\n", name, record.get_name(), record.get_ip(), record.get_port(), DataUtilities.get_timestamp());
		 else out.printf("Unable to add new record to %s: %s, %s, %d at %s\n", name, record.get_name(), record.get_ip(), record.get_port(), DataUtilities.get_timestamp());
	}
	
	public synchronized boolean add_record(Record record){
		boolean ret_value = data.contains(record);
		if(!ret_value){
			data.add(record);
		}
		add_response(ret_value, record);
	    return ret_value;
	}
	
	public synchronized boolean remove_record(final Record record){
	    boolean ret_value = data.remove(record);
	    out.printf("Removed record from %s: %s, %s, %d\n", name, record.get_name(), record.get_ip(), record.get_port());
	    return ret_value;
	}
	
	// *** EXECUTION OF RECORD STORE ***
	
	@SuppressWarnings("unchecked")
	protected final void run_task(AbstractJob job) throws IOException{
	    switch(job.get_job()){
            case READ:
                //TODO: Do I want to make a factory of this for all task types??
                StoreReadTask.spawn((SearchJob)job, this).start();
                break;
            case WRITE:
                // Check if can write...
                // If flag is set, then put back in write order..
                if(!write_mode.getAndSet(true)) {
                    // The write flag wasn't set, and now it is.. so lets do work
                    // Create a thread to run the job
                    // Make sure to check if record exists.. if so, then make sure to alert user to 
                    // Delete first before rewriting... (hope this gets changed...)
                    out.printf("Starting write task at %s\n", name);
                    StoreWriteTask.spawn((WriteJob<Record>)job, this).start();
                    write_mode.set(false);
                }else{
                    // The write flag was set already
                    // Let's put the job back (with new timestamp and priority + 1) and come back to it
                    job_queue.put(job.priority_update());
                }
                break;
            case DELETE:
                System.out.println("Delete task!!");
                if(!write_mode.getAndSet(true)){
                    //Write flag was not set, and now it is... lets do work...
                    // Create a thread to run the query...
                    out.printf("Starting delete task at %s\n", name);
                    StoreDeleteTask.spawn((SearchJob) job, this).start(); //TODO: Add cast if necessary
                    write_mode.set(false);
                }else{
                    // The write flag was set already
                    // Lets put the job back (with new timestamp and priority + 1) and come back to it
                    job_queue.put(job.priority_update());
                }
                break;
            case SHUT_DOWN:
                //THIS should be handled synchronously, to alert all future jobs stuck in queue
                close();
                write_out();
                break;
            default:
            	// Other types of jobs that this store doesn't recognize
            	break;
	    }
	}
	
	private void load_records_from_store() throws FileNotFoundException{
	    out.println("Loading data... from dat file");
	    Scanner sc = new Scanner(record_store).useDelimiter("\n");
	    String line;
	    while(sc.hasNextLine()){
	        line = sc.nextLine();
	        Scanner rec = new Scanner(line).useDelimiter("@_@");
	        String name = rec.next();
	        String ip = rec.next();
	        int port = rec.nextInt();
	        String timestamp = rec.next();
	        try{
	            Record record = Record.recreate_record(name, ip, port, timestamp);
	            data.add(record);
	        }catch(InvalidRecordException ire){
	            // print it out and continue
	            out.print("Invalid Record: ");
	            out.printf("name:(%80s) ip:(%12s) port:(%5d)\n", name, ip, port, timestamp);
	        }
	    }
	}
	
	private void write_records_to_store() throws IOException{
	    out.println("Writing records to dat file");
	    BufferedWriter buff = new BufferedWriter(new FileWriter(record_store.getAbsolutePath()));
	    ListIterator<Record> iter = data.listIterator();
	    try{
    	    while(iter.hasNext()){
    	        Record rec = iter.next();
    	        buff.write(rec.get_name()+"@_@"+rec.get_ip()+"@_@"+rec.get_port()+"@_@"+rec.get_timestamp()+"\n");
    	    }
	    }finally{ buff.close(); }
	}
	
	// *** STATIC FACTORY METHODS ***
	
	public static final RecordStore create_temp_database() throws IOException{
	    return new RecordStore(null, false);
	}
	
	public static final RecordStore create_new_database(String name) throws IOException{
	    return new RecordStore(name, true);
	}
	
	// *** PRIVATE CONSTRUCTORS ***
	
	// Can be set up for multiple temp dbs....
	
	private RecordStore(String index_location, boolean persistent) throws IOException{
		super(index_location);
	    data = new CopyOnWriteArrayList<Record>();
	    this.persistent = persistent;
	    record_store = (persistent)? new File("data/"+index_location+".dat"):null;
	    if(persistent){
    	    if (record_store.exists() && record_store.isFile()){
    	        load_records_from_store();
    	    }
	    }
	    setName("RecordStore: "+index_location);
	}
}
