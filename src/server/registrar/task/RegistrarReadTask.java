package server.registrar.task;

import java.net.SocketException;
import java.util.UUID;

import server.ServerInfo;
import server.job.SearchJob;
import server.registrar.ClientRegistrar;
import server.task.AbstractSearchTask;

public final class RegistrarReadTask extends AbstractSearchTask<ServerInfo>{
	
	
	public static RegistrarReadTask spawn(final SearchJob job, final ClientRegistrar registrar) throws SocketException{
		return new RegistrarReadTask(job, registrar);
	}
	
	private RegistrarReadTask(final SearchJob job, final ClientRegistrar registrar) throws SocketException{
		super(job, registrar);
		this.setName("ClientRegistrar Filter Task: "+ UUID.randomUUID().toString());
	}
	
}