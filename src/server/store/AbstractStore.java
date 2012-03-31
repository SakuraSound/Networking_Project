package server.store;

import static java.lang.System.out;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;
import server.job.AbstractJob;
import server.task.AbstractTask;
import utils.CommUtilities;
import utils.CommUtilities.ERROR;

import comm.resp.ErrorMessage;

import data.record.AbstractRecord;

public abstract class AbstractStore<T extends AbstractRecord> extends Thread {

	protected PriorityBlockingQueue<AbstractJob> job_queue;
	protected ConcurrentHashMap<String, AbstractTask<T>> current_tasks;
	protected AtomicBoolean write_mode;
	protected AtomicBoolean is_open;
	protected final String virtual_name;
	protected SpecialSocket socket;
	protected InetAddress inet;
	protected int port_num;
	
	
	
	public abstract Enumeration<T> get_reader();
	//Make sure to override add_record using synchronized modifier
	public abstract boolean add_record(final T record);
	//Make sure to override remove_record using synchronized modifier
	public abstract boolean remove_record(final T delete_record);
	
	protected abstract void close();
	protected abstract void run_task(final AbstractJob job) throws IOException;
	
	
	public final String get_name(){ return getName(); }
	public final String get_virtual_name(){ return virtual_name; }
	public void remove_self(AbstractTask<T> finished_task){ current_tasks.remove(finished_task.get_name()); }
	public void add_self(AbstractTask<T> starting_task){ current_tasks.put(starting_task.get_name(), starting_task); }
	public void add_job(AbstractJob job){ job_queue.add(job); }
	public boolean is_open(){ return is_open.get(); }
	public InetAddress get_inet(){ return inet; }
	public int get_port(){ return port_num; }
	
	
	
    protected void send_error(CommUtilities.ERROR error, AbstractJob job) throws IOException, JAXBException{
        ErrorMessage msg = ErrorMessage.create_message(error);
        socket.send(msg, job.get_inet(), job.get_port());
        // Do we want to wait for ack??
    }
    
    public void run(){
		while(is_open.get() || !job_queue.isEmpty()){
			try {
				AbstractJob job = job_queue.poll();
				if(job==null){
				    continue;
				}
				if(!is_open.get()){
				    out.println("Sending closed error");
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
    
    
    protected AbstractStore(String name) throws SocketException{
    	socket = SpecialSocket.create_socket();
    	this.inet = socket.get_inet();
    	this.port_num = socket.get_port();
    	this.virtual_name = name;
    	is_open = new AtomicBoolean(true);
    	write_mode = new AtomicBoolean(false);
    	job_queue = new PriorityBlockingQueue<AbstractJob>();
    	current_tasks = new ConcurrentHashMap<String, AbstractTask<T>>();
    }
    
	
}
