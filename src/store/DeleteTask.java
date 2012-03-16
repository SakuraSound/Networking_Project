package store;

import inter.Message;
import inter.Task;

import java.io.IOException;
import java.net.SocketException;
import java.util.ListIterator;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import comm.CommUtilities.ERROR;
import comm.msg.ErrorMessage;
import comm.resp.DeleteResponseMessage;

import data.Record;

/**
 * Task responsible for deleting data from a data store
 * @author Hatomi
 *
 */
public class DeleteTask extends Thread implements Task {

    private final IndexJob job;
    private final RecordStore store;
    private final SpecialSocket socket;
    
    public String get_name() {
        return getName();
    }
    
    public static DeleteTask spawn_delete_task(IndexJob job, RecordStore store) throws SocketException{
        return new DeleteTask(job, store);
    }

    
    private DeleteTask(final IndexJob job, final RecordStore store) throws SocketException{
        this.job = job;
        this.store = store;
        setName("Delete Task; ("+store.get_name()+") "+UUID.randomUUID().toString());
        this.socket = SpecialSocket.create_socket();
    }
    
    private int find_document(){
        //FIXME: This is ugly code... need to clean it up...
        ListIterator<Record> searcher = store.get_reader();
        int position=0;
        while(searcher.hasNext()){
            Record record = searcher.next();
            if(record.get_name().equals(job.get_query().get_name())){
                if(job.get_query().get_ip() == null && job.get_query().get_port() == 0){
                    return position;
                }
                if(record.get_ip().equals(job.get_query().get_ip())){
                    if(job.get_query().get_port() == 0 || record.get_port() == job.get_query().get_port()){
                        return position;
                    }
                }
            }
            position++;
        }
        return -1;
    }
    
    public void run(){
        store.add_self(this);
        System.out.println("Starting delete task");
        Message reply = null;
        try{
            int ndx = find_document();
            if(ndx != -1){
                store.remove_data(ndx);
                reply = DeleteResponseMessage.create_response();
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
}
