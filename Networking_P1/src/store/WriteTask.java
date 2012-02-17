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
import comm.resp.WriteResponseMessage;

import data.Record;

public class WriteTask extends Thread implements Task{
    private final IndexJob job;
    private final RecordStore store;
    private final SpecialSocket socket;
    
    public static WriteTask spawn_write_task(final IndexJob job, final RecordStore store) throws SocketException{
        return new WriteTask(job, store);
    }
    
    private WriteTask(final IndexJob job, final RecordStore store) throws SocketException{
        this.job = job;
        this.store = store;
        setName("Write Task: ("+ store.get_name() +") "+UUID.randomUUID().toString());
        this.socket = SpecialSocket.create_socket();
    }

    public String get_name() {
        return getName();
    }
    
    public boolean found_duplicate(){
        ListIterator<Record> searcher = store.get_reader();
        while(searcher.hasNext()){
            Record record = searcher.next();
            if(job.get_record().is_equivalent(record))
                return true;
        }
        return false;
    }
    
    
    public void run(){
        store.add_self(this);
        try {
            Message reply = null; // need to initialize properly...
            if(found_duplicate()){
                reply = ErrorMessage.create_message(ERROR.OVERWRITE_ERROR);
            }else{
                store.add_record(job.get_record());
                reply = WriteResponseMessage.create_write_response(job.get_record());
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
