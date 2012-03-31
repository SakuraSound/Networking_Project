package server.task;

import inter.Message;

import java.io.IOException;
import java.net.SocketException;
import java.util.Enumeration;

import javax.xml.bind.JAXBException;

import server.job.SearchJob;
import server.store.AbstractStore;
import utils.CommUtilities.ERROR;

import comm.resp.ErrorMessage;
import comm.resp.GenericResponse;

import data.SearchQuery;
import data.record.AbstractRecord;

public abstract class  AbstractDeleteTask<T extends AbstractRecord> extends AbstractTask<T> {

	
	protected int delete_position;
	protected T delete_record;
	
	protected abstract boolean filter(final SearchQuery query, AbstractRecord record);
	
	
	protected T find_document(){
		Enumeration<T> searcher = store.get_reader();
		SearchQuery query = ((SearchJob) job).get_query();
		
		while(searcher.hasMoreElements()){
			T record = searcher.nextElement();
			if(filter(query, record)){
				return record;
			}
        }   
        return null;
	}
	
	public void run(){
        store.add_self(this);
        System.out.println("Starting delete task");
        Message reply = null;
        try{
        	if(store.remove_record(delete_record)){
        		reply = GenericResponse.create_response("Deletion Successful");
        	}else{
                reply = ErrorMessage.create_message(ERROR.RECORD_NOT_FOUND);
            }
            socket.send(reply, job.get_inet(), job.get_port());
        }catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }finally{
            socket.close();
            store.remove_self(this);
        }
    }
	
	protected AbstractDeleteTask(final SearchJob job, final AbstractStore<T> store) throws SocketException{
		super(job, store);
		delete_position = 0;
		delete_record = null;
	}
}
