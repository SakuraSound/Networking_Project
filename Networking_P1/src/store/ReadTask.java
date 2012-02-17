package store;

import static java.lang.System.out;
import inter.Task;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import comm.CommUtilities;
import comm.resp.ReadListMessage;
import comm.resp.ReadResponseMessage;

import data.Record;
import data.SearchQuery;

public class ReadTask extends Thread implements Task{
    private final IndexJob job;
    private final RecordStore store;
    private ArrayList<Record> found_recs;
    private ListIterator<Record> searcher;
    private SpecialSocket socket;
    
    
    public String get_name() {
        return getName();
    }
    
    private void run_search(){
        SearchQuery query = job.get_query();
        searcher = store.get_reader();
        while(searcher.hasNext()){
            Record rec = searcher.next();
            String nex = query.get_name().replaceAll("\\.", "\\\\.").replaceAll("\\*", "[\\\\w\\\\s]+");
            if(rec.get_name().matches(nex)){
                String regex = query.get_ip().replaceAll("\\.", "\\\\.").replaceAll("\\*", "[\\\\d]");
                if(rec.get_ip().matches(regex)){
                    found_recs.add(rec);
                }
            }
        }
    }
    
    private boolean communicate_records() throws IOException, JAXBException{
        //Send a page for the user to look through...
        ReadListMessage msg = ReadListMessage.create_message(found_recs, found_recs.size());
        socket.send(msg, job.get_inet(), job.get_port());
        byte[] bytes = socket.accept().getData();
        try{
            CommUtilities.bytes_2_java(bytes, ReadResponseMessage.class);
            return true;
        }catch(JAXBException jaxbe){ return false; }
    }
    
    
    public void run(){
        try{
            searcher = store.get_reader();
            run_search();
            out.println("Done running... now communicating");
            boolean success = communicate_records();
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
    
    public static final ReadTask spawn_read_task(final IndexJob job, final RecordStore store) throws SocketException{
        return new ReadTask(job, store);
    }
    
    private ReadTask(IndexJob job, RecordStore store) throws SocketException{
        this.job = job;
        this.store = store;
        this.found_recs = new ArrayList<Record>();
        setName("Read Task: "+ UUID.randomUUID().toString());
        this.socket = SpecialSocket.create_socket();
    }

    

}