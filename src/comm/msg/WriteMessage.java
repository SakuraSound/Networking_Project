package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.job.Job;
import data.record.AbstractRecord;

/**
 * The message responsible for writing a new record to a data store
 * holds the record, target db, and the timestamp
 * @author Hatomi
 *
 */
@XmlRootElement(name="WriteMessage")
public class WriteMessage<T extends AbstractRecord> extends AbstractMessage {

    @XmlElement(name="record")
    private T record;
    @XmlElement(name="target_db")
    private String target_db;
    
    public T get_record(){ return record; }
    public String get_store(){ return target_db; }
    
    
    public static <S extends AbstractRecord> WriteMessage<S> create_message(Job job,S record, String target_db, int priority){
        return new WriteMessage<S>(job, record, target_db, priority);
    }
    
    private WriteMessage(){}
    
    
    private WriteMessage(Job job, T record, String target_db, int priority){
    	super(job, priority);
        this.record = record;
        this.target_db = target_db;
    }
}
