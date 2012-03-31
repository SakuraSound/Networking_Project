package server.registrar.task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import server.ServerInfo;
import server.job.WriteJob;
import server.registrar.ClientRegistrar;
import server.task.AbstractWriteTask;

import comm.msg.UpdateMessage;

import data.record.Record;

public final class RegistrarWriteTask extends AbstractWriteTask<ServerInfo> {
	
	private List<Record> links;
	private ServerInfo client;
	private Record from;
	
	public static RegistrarWriteTask spawn(final WriteJob<ServerInfo> job, final ClientRegistrar registrar) throws SocketException{
		return new RegistrarWriteTask(job, registrar);
	}
	
	protected void run_task(ServerInfo info) {
		store.add_record(info); 
		try{
			for(Record link : links){
				String uuid = UUID.randomUUID().toString();
				UpdateMessage msg = UpdateMessage.create_message(job.get_job(), client, from, uuid);	
				socket.send(msg, InetAddress.getByName(link.get_ip()), link.get_port());
				//TODO: Do we want to ack here?
			}
		}catch(JAXBException jaxbe){jaxbe.printStackTrace();}
		 catch(IOException ioe){ioe.printStackTrace();}
		
	}
	
	private RegistrarWriteTask(final WriteJob<ServerInfo> job, final ClientRegistrar registrar) throws SocketException{
		super(job, registrar);
		this.links = job.get_links();
		this.from = job.get_from();
		setName("Register Task: ("+ store.get_name()+") "+UUID.randomUUID().toString());
	}
	
	@SuppressWarnings("unchecked")
	public boolean found_duplicate(){
		WriteJob<ServerInfo> sjob = (WriteJob<ServerInfo>) job;
		searcher = store.get_reader();
		while(searcher.hasMoreElements()){
			ServerInfo info = searcher.nextElement();
			if(info.equals(sjob.get_record())){
				return true;
			}
		}
		return false;
	}

}
