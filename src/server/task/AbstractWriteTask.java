package server.task;

import inter.Message;

import java.io.IOException;
import java.net.SocketException;
import java.util.Enumeration;

import javax.xml.bind.JAXBException;

import server.job.WriteJob;
import server.store.AbstractStore;
import utils.CommUtilities.ERROR;

import comm.resp.ErrorMessage;
import comm.resp.GenericResponse;

import data.record.AbstractRecord;

public abstract class AbstractWriteTask<T extends AbstractRecord> extends AbstractTask<T> {
	
	protected Enumeration<T> searcher;
	
	protected abstract boolean found_duplicate();
	protected abstract void run_task(T record);
	
	protected AbstractWriteTask(final WriteJob<T> job, final AbstractStore<T> store) throws SocketException{
		super(job, store);
	}
	
	
	
    @SuppressWarnings("unchecked")
	public void run(){
        store.add_self(this);
        WriteJob<T> tjob = (WriteJob<T>) job;
        try {
            Message reply = null; // need to initialize properly...
            if(found_duplicate()){
                reply = ErrorMessage.create_message(ERROR.OVERWRITE_ERROR);
            }else{
                run_task(tjob.get_record());
                reply = GenericResponse.create_response("Addition Successful");
            }
            socket.send(reply, job.get_inet(), job.get_port());
        } catch (IOException e) {
                e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }finally{
            socket.close();
            store.remove_self(this);
        }
    }
	
}
