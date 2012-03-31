package server.task;

import java.net.SocketException;

import net.SpecialSocket;
import server.job.AbstractJob;
import server.store.AbstractStore;
import data.record.AbstractRecord;

public abstract class AbstractTask<T extends AbstractRecord> extends Thread {

	protected  AbstractJob job;
	protected  SpecialSocket socket;
	protected  AbstractStore<T> store;
	
	
	public String get_name(){ return getName(); }
	
	protected AbstractTask(final AbstractJob job, final AbstractStore<T> store) throws SocketException{
		this.job = job;
		this.store = store;
		this.socket = SpecialSocket.create_socket();
	}

	public AbstractTask() {
		// TODO Auto-generated constructor stub
	}
}
