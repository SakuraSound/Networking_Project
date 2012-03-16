package comm.resp;

import inter.Message;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

import data.Record;

@XmlRootElement(name="ReadListMessage")
public class ReadListMessage implements Message {
    
    @XmlElement(name="records")
    private List<Record> record_list;
    @XmlAttribute(name="timestamp")
    private String timestamp;
    @XmlElement(name="num_recs")
    private int num_recs;
    
    
    public List<Record> get_records(){ return record_list; }
    public String get_timestamp(){ return timestamp; }
    public int get_pgnum(){ return num_recs; }
    
    
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, this.getClass());
    }
    
    public static ReadListMessage create_message(List<Record> records, int num_recs){
        return new ReadListMessage(records, num_recs);
    }

    private ReadListMessage(List<Record> records, int num_recs){
        this.record_list = records;
        this.num_recs = num_recs;
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    private ReadListMessage(){}
}
