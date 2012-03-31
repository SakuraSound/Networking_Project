package comm.resp;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PrepareReadMessage")
public class PrepareReadMessage extends GenericResponse {
    @XmlElement(name="bytes")
    private int bytes;
    
    public int get_next_bytes(){ return bytes; }
    
    public static PrepareReadMessage create_message(int req_bytes){
        return new PrepareReadMessage(req_bytes);
    }
    
    
    private PrepareReadMessage(int req_bytes){
    	super("Preparing for large data");
        this.bytes = req_bytes;
    }
    
    private PrepareReadMessage(){}
}
