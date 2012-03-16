package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;


@XmlRootElement(name="PrepareReadMessage")
public class PrepareReadMessage implements Message {

    @XmlElement(name="bytes")
    private int bytes;
    
    private String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    public int get_next_bytes(){ return bytes; }
    
    
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, this.getClass());
    }

    public static PrepareReadMessage create_message(int req_bytes){
        return new PrepareReadMessage(req_bytes);
    }
    
    
    private PrepareReadMessage(int req_bytes){
        this.bytes = req_bytes;
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    private PrepareReadMessage(){}
}
