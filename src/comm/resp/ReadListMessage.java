package comm.resp;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import data.record.AbstractRecord;

@XmlRootElement(name="ReadListMessage")
public class ReadListMessage<T extends AbstractRecord> extends GenericResponse {
    
    @XmlElement(name="records")
    private List<T> record_list;
    @XmlElement(name="num_recs")
    private int num_recs;
    
    
    public List<T> get_records(){ return record_list; }
    public int get_numrecs(){ return num_recs; }
    
    
    public static <T extends AbstractRecord> ReadListMessage<T> create_message(List<T> records){
        return new ReadListMessage<T>(records);
    }

    private ReadListMessage(List<T> records){
    	super("List of records received.");
        this.record_list = records;
        this.num_recs = records.size();
    }
    
    private ReadListMessage(){}
}
