package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

import data.Record;

/**
 * The message responsible for writing a new record to a data store
 * holds the record, target db, and the timestamp
 * @author Hatomi
 *
 */
@XmlRootElement(name="WriteMessage")
public class WriteMessage implements Message {

    @XmlElement(name="record")
    private Record record;
    @XmlAttribute(name="timestamp")
    private String timestamp;
    @XmlElement(name="target_db")
    private String target_db;
    
    public Record get_record(){ return record; }
    public String get_timestamp(){ return timestamp; }
    public String get_store(){ return target_db; }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, this.getClass());
    }
    
    public static WriteMessage create_message(Record record, String target_db){
        return new WriteMessage(record, target_db);
    }
    
    public static WriteMessage create_message(Record record){
        return new WriteMessage(record);
    }
    
    private WriteMessage(){}
    
    private WriteMessage(Record record){
        this(record, null);
    }
    
    private WriteMessage(Record record, String target_db){
        this.record = record;
        this.target_db = target_db;
        this.timestamp = CommUtilities.get_timestamp();
    }

}
