package server.registrar.task;

import java.net.SocketException;
import java.util.UUID;

import server.ServerInfo;
import server.job.SearchJob;
import server.registrar.ClientRegistrar;
import server.store.AbstractStore;
import server.task.AbstractDeleteTask;
import data.SearchQuery;
import data.record.AbstractRecord;

public final class RegistrarDeleteTask extends AbstractDeleteTask<ServerInfo>{

	public static RegistrarDeleteTask spawn(SearchJob job, ClientRegistrar registrar) throws SocketException{
		return new RegistrarDeleteTask(job, registrar);
	}
	
	protected RegistrarDeleteTask(SearchJob job, AbstractStore<ServerInfo> store)
			throws SocketException {
		super(job, store);
		setName("Unregister Task: ("+store.get_name()+") "+UUID.randomUUID().toString());		
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
