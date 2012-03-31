package server.registrar.task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBException;

import server.ServerInfo;
import server.job.UpdateJob;
import server.job.WriteJob;
import server.registrar.ClientRegistrar;
import server.task.AbstractTask;

import comm.msg.UpdateMessage;

import data.record.InvalidRecordException;
import data.record.Record;

public class RegistrarUpdateTask extends AbstractTask<ServerInfo>{
	
	private List<Record> links;
	private ServerInfo client;
	private String uuid;
	
	
	@SuppressWarnings("unchecked")
	public boolean found_duplicate(){
		WriteJob<ServerInfo> sjob = (WriteJob<ServerInfo>) job;
		Enumeration<ServerInfo> searcher = store.get_reader();
		while(searcher.hasMoreElements()){
			ServerInfo info = searcher.nextElement();
			if(info.equals(sjob.get_record())){
				return true;
			}
		}
		return false;
	}
	
	
	private boolean run_update(){
		switch(job.get_job()){
			case UPDATE_REGISTER:
				if(!found_duplicate()){
					store.add_record(client);
				}
				break;
			case UPDATE_UNREGISTER:
				store.remove_record(client);
				break;
			default:
				return false;
		}
		return true;
	}
	
	public void run(){
		try{
			if(run_update()){
				Record rec = Record.create_record(store.get_name(), store.get_inet().getHostAddress(), store.get_port());
				for(Record link : links){
					UpdateMessage msg = UpdateMessage.create_message(job.get_job(), client, rec, uuid);	
					socket.send(msg, InetAddress.getByName(link.get_ip()), link.get_port());
					//TODO: Do we want to ack here?
				}
			}
			
		}catch(InvalidRecordException ire){
			ire.printStackTrace();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		} catch (JAXBException jaxbe) {
			// TODO Auto-generated catch block
			jaxbe.printStackTrace();
		}
		
	}
	
	public static RegistrarUpdateTask spawn(UpdateJob job, ClientRegistrar registrar) throws SocketException{
		return new RegistrarUpdateTask(job, registrar);
	}
	
	private RegistrarUpdateTask(UpdateJob job, ClientRegistrar registrar) throws SocketException{
		super(job, registrar);
	}
}
