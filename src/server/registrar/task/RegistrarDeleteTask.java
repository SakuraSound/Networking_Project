package server.registrar.task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import server.ServerInfo;
import server.job.SearchJob;
import server.registrar.ClientRegistrar;
import server.store.AbstractStore;
import server.task.AbstractDeleteTask;

import comm.msg.UpdateMessage;

import data.SearchQuery;
import data.record.AbstractRecord;
import data.record.Record;

public final class RegistrarDeleteTask extends AbstractDeleteTask<ServerInfo>{

	private List<Record> links;
	private Record from;
	
	public static RegistrarDeleteTask spawn(SearchJob job, ClientRegistrar registrar) throws SocketException{
		return new RegistrarDeleteTask(job, registrar);
	}
	
	protected RegistrarDeleteTask(SearchJob job, AbstractStore<ServerInfo> store)
			throws SocketException {
		super(job, store);
		this.links = job.get_links();
		this.from = job.get_self();
		setName("Unregister Task: ("+store.get_name()+") "+UUID.randomUUID().toString());		
	}

	protected void run_task(){
		try{
			for(Record link : links){
				String uuid = UUID.randomUUID().toString();
				UpdateMessage msg = UpdateMessage.create_message(job.get_job(), delete_record, from, uuid);
				socket.send(msg, InetAddress.getByName(link.get_ip()), link.get_port());
				//TODO: Do we want to ack here?
			}
		}catch(JAXBException jaxbe){jaxbe.printStackTrace();}
		 catch(IOException ioe){ioe.printStackTrace();}
	}
	
	protected boolean filter(SearchQuery query, AbstractRecord record) {
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
