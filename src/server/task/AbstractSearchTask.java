package server.task;

import static java.lang.System.out;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBException;

import server.job.SearchJob;
import server.store.AbstractStore;
import utils.CommUtilities;

import comm.resp.GenericResponse;
import comm.resp.ReadListMessage;

import data.SearchQuery;
import data.record.AbstractRecord;

public abstract class AbstractSearchTask<T extends AbstractRecord> extends AbstractTask<T> {

	protected List<T> found_items;
	private Enumeration<T> searcher;
	
	
	protected void filter(T item, SearchQuery query){
        String nex = clean_name(query.get_name());
        if(item.get_name().matches(nex)){
            String regex = clean_ip(query.get_ip());
            if(item.get_ip().matches(regex)){
                found_items.add(item);
            }
        }
    }
	
	protected String clean_name(String raw){
		return raw.replaceAll("\\.", "\\\\.").replaceAll("\\*", "[\\\\w\\\\s]+");
	}
	
	protected String clean_ip(String raw){
		return raw.replaceAll("\\.", "\\\\.").replaceAll("\\*", "[\\\\d]");
	}
	
	
	protected boolean send() throws IOException, JAXBException{
		ReadListMessage<T> msg = ReadListMessage.create_message(found_items);
		socket.send(msg, job.get_inet(), job.get_port());
		byte[] bytes = socket.accept().getData();
        try{
            CommUtilities.bytes_2_java(bytes, GenericResponse.class);
            return true;
        }catch(JAXBException jaxbe){ return false; }
	}
	
	protected final void search(){
		SearchQuery query = ((SearchJob) job).get_query();
		while(searcher.hasMoreElements()){
			filter(searcher.nextElement(), query);
		}
	}
	
    public void run(){
        try{
            searcher = store.get_reader();
            search();
            out.println("Done running... now communicating");
            boolean success = send();
            out.println((success)?"Successful Read from ":"Unsuccessful Read from "+get_name());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }finally{
            socket.close();
            store.remove_self(this);
        }
    }
	
	protected AbstractSearchTask(final SearchJob job, final AbstractStore<T> store) throws SocketException{
		super(job, store);
		this.found_items = new ArrayList<T>();
	}

}
