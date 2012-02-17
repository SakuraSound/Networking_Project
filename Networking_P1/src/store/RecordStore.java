package store;
import static java.lang.System.out;
import inter.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import comm.CommUtilities;
import comm.CommUtilities.ERROR;
import comm.msg.ErrorMessage;

import data.DataUtilities;
import data.InvalidRecordException;
import data.Record;

//TODO: Create timer  that calls searchmanager's maybeReopen call to swap indexsearchers every so often...

/**
 * Essentially a database. Can be in-memory, or on the filesystem.
 * @author Hatomi
 *
 */
public class RecordStore extends Thread{
	
	private String name;
	
	private PriorityBlockingQueue<IndexJob> job_queue;
	private ConcurrentHashMap<String, Task> current_tasks;
	
	private CopyOnWriteArrayList<Record> data;
	
	private File record_store;
	private AtomicBoolean write_flag;
	private AtomicBoolean is_open;
	
	private SpecialSocket socket;
	
	private final InetAddress inet;
	private final int port_num;
	private final boolean persistent;
	
	
	public String get_name(){ return name; }
	public boolean is_open(){ return is_open.get(); }
	public InetAddress get_inet(){ return inet; }
	public int get_port(){ return port_num; }
	public int get_record_size(){ return data.size(); }
	
	
	public void remove_self(Task finished_task){
	    current_tasks.remove(finished_task.get_name(), finished_task);
	}
	
	public void add_self(Task starting_task){
	    current_tasks.put(starting_task.get_name(), starting_task);
	}
	
	public void add_job(IndexJob job){
	    job_queue.add(job);
	}
	
	public ListIterator<Record> get_reader(){ return data.listIterator(); }
	
    private void send_error(CommUtilities.ERROR error, IndexJob job) throws IOException, JAXBException{
        ErrorMessage msg = ErrorMessage.create_message(error);
        socket.send(msg, job.get_inet(), job.get_port());
        // Do we want to wait for ack??
    }
	
	private void close_db() throws IOException{
	    is_open.set(false);
	    if(persistent)
	        write_records_to_store();
	    out.printf(" Starting close of %s at %s\n", name, DataUtilities.get_timestamp());
	}
	
	public synchronized void add_record(Record record){
	    data.add(record);
	    out.printf("Added new record to %s: %s, %s, %d\n", name, record.get_name(), record.get_ip(), record.get_port());
	}
	
	public synchronized void remove_data(int location){
	    Record record = data.remove(location);
	    out.printf("Removed record from %s: %s, %s, %d\n", name, record.get_name(), record.get_ip(), record.get_port());
	}
	
	// *** EXECUTION OF RECORD STORE ***
	
	private final void run_task(IndexJob job) throws IOException{
	    switch(job.get_job()){
            case READ:
                //TODO: Do I want to make a factory of this for all task types??
                ReadTask.spawn_read_task(job, this).start();
                break;
            case WRITE:
                // Check if can write...
                // If flag is set, then put back in write order..
                if(!write_flag.getAndSet(true)) {
                    // The write flag wasn't set, and now it is.. so lets do work
                    // Create a thread to run the job
                    // Make sure to check if record exists.. if so, then make sure to alert user to 
                    // Delete first before rewriting... (hope this gets changed...)
                    out.printf("Starting write task at %s\n", name);
                    WriteTask.spawn_write_task(job, this).start();
                    write_flag.set(false);
                }else{
                    // The write flag was set already
                    // Let's put the job back (with new timestamp and priority + 1) and come back to it
                    job_queue.put(IndexJob.get_refresh_job(job, false));
                }
                break;
            case DELETE:
                System.out.println("Delete task!!");
                if(!write_flag.getAndSet(true)){
                    //Write flag was not set, and now it is... lets do work...
                    // Create a thread to run the query...
                    out.printf("Starting delete task at %s\n", name);
                    DeleteTask.spawn_delete_task(job, this).start();
                    write_flag.set(false);
                }else{
                    // The write flag was set already
                    // Lets put the job back (with new timestamp and priority + 1) and come back to it
                    job_queue.put(IndexJob.get_refresh_job(job, true));
                }
                break;
            case SHUT_DOWN:
                //THIS should be handled synchronously, to alert all future jobs stuck in queue
                close_db();
                break;
	    }
	}
	
	public void run(){
		while(is_open.get() || !job_queue.isEmpty()){
			try {
				IndexJob job = job_queue.poll();
				if(job==null){
				    continue;
				}
				
				if(!is_open.get()){
				    out.println("Sending closed db error");
				    send_error(ERROR.CLOSED_DB, job);
				}else{
				    out.println("Acting on job");
				    out.println(job.get_job());
				    run_task(job);
				}
			}catch (SocketException e) {
			    // Log it and break...
                e.printStackTrace();
                break;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            } catch (JAXBException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
		}
		// Wait for tasks to complete
		out.println("Waiting for running tasks to complete");
		while(!current_tasks.isEmpty());
		// Then we commit if necessary
		out.println("Closed store"+getName());
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
	    socket = SpecialSocket.create_socket();
	    this.inet = socket.get_inet();
	    this.port_num = socket.get_port();
	    this.name = persistent? index_location: "TempDB";
	    is_open = new AtomicBoolean(true);
	    write_flag = new AtomicBoolean(false);
	    job_queue = new PriorityBlockingQueue<IndexJob>();
	    current_tasks = new ConcurrentHashMap<String, Task>();
	    data = new CopyOnWriteArrayList<Record>();
	    this.persistent = persistent;
	    record_store = (persistent)? new File("data/"+index_location+".dat"):null;
	    if(persistent){
    	    if (record_store.exists() && record_store.isFile()){
    	        load_records_from_store();
    	    }
	    }
	}
}
