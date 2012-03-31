package server.registrar;

import static java.lang.System.out;

import java.io.IOException;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

import server.ServerInfo;
import server.job.AbstractJob;
import server.job.SearchJob;
import server.job.UpdateJob;
import server.job.WriteJob;
import server.registrar.task.RegistrarDeleteTask;
import server.registrar.task.RegistrarReadTask;
import server.registrar.task.RegistrarUpdateTask;
import server.registrar.task.RegistrarWriteTask;
import server.store.AbstractStore;
import utils.DataUtilities;


/**
 * Responsible for maintaining a  list
 * of clients that are registered to a particular server (or store in our case)
 * @author Haruka
 *
 */
public class ClientRegistrar extends AbstractStore<ServerInfo>{
	
	private CopyOnWriteArrayList<ServerInfo> clients;
	
	public void close(){
		is_open.set(false);
		out.printf(" Closed client registrar at %s\n", DataUtilities.get_timestamp());
	}
	
	public Enumeration<ServerInfo> get_reader(){ return Collections.enumeration(clients); }
	
	
	/**
	 * Checks on the write flag for the ClientRegistrar and sets it atomically
	 * returns previous value, so if you get false, then you can perform a write 
	 * without being disturbed...
	 * @return true if flag was set, false otherwise
	 */
	public boolean test_and_set_write(){
		return write_mode.getAndSet(true);
	}
	
	public Enumeration<ServerInfo> get_client_list(){
		return Collections.enumeration(clients);
	}
	
	
	// ** REGISTRATION FUNCTIONS **
	
	private final void register_response(final boolean value, final String handle, final String ip_addr, final int port_num){
		 if(value)
			 out.printf("Registered %s@%s:%d to %s at %s.\n", handle, ip_addr, port_num, virtual_name, DataUtilities.get_timestamp());
		 else out.printf("Unable to register %s on %s at %s. (Duplication).\n", handle, virtual_name, DataUtilities.get_timestamp());
	}
	
	
	public final synchronized boolean add_record(ServerInfo info){
		boolean ret_value = clients.contains(info.get_name());
		if(!ret_value){
			clients.add(info);
		}
		register_response(ret_value, info.get_name(), info.get_ip(), info.get_port());
		return ret_value;
	}
	// ** UNREGISTRATION METHODS **
	
	private final void unregister_response(final boolean value, final String handle ){
		if(value)
			out.printf("Unregistered %s from %s at %s.\n", handle, virtual_name, DataUtilities.get_timestamp());
		else out.printf("Unable to unregister %s from %s at %s. (Handle doesn't exist)\n", handle, virtual_name, DataUtilities.get_timestamp());
	}
	
	public final synchronized boolean remove_record(final ServerInfo info){
		boolean ret_value = clients.contains(info.get_name());
		if(ret_value)
			clients.remove(info);
		unregister_response(ret_value, info.get_name());
		return ret_value;	
	}
	
	
	
	
	// ** ACTOR JOB : ROLE DEFINITION
	
	@SuppressWarnings("unchecked")
	public void run_task(AbstractJob job) throws IOException{
		switch(job.get_job()){
		
			case REGISTER:
				// Responsible for registering new clients to the attached store
				RegistrarWriteTask.spawn((WriteJob<ServerInfo>) job, this).start();
				break;
				
			case UNREGISTER:
				// Responsible for unregistering clients registered to this store
				RegistrarDeleteTask.spawn((SearchJob)job, this).start();
				break;
				
			case CLIENT_SEARCH:
				// Responsible for performing a search on clients registered to this store
				RegistrarReadTask.spawn((SearchJob)job, this).start();
				break;
				
			case UPDATE_REGISTER:
			case UPDATE_UNREGISTER:
				RegistrarUpdateTask.spawn((UpdateJob) job, this).start();
				break;
			default:
				//FIXME: Shoudln't get here...
				break;
		}
	}
	
	
	
	// ** STATIC FACTORY METHOD **
	
	public static final ClientRegistrar create_registrar(String virtual_name) throws SocketException{
		return new ClientRegistrar(virtual_name);
	}
	
	// ** PRIVATE CONSTRUCTORS **
	
	private ClientRegistrar(String virtual_name) throws SocketException{
		super(virtual_name);
		setName("Client Registrar: "+virtual_name);
		clients = new CopyOnWriteArrayList<ServerInfo>();
	}


	
}
