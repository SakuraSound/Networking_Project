package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

@XmlRootElement(name="BatchKillResponseMessage")
public class BatchKillResponseMessage implements Message{

    @XmlAttribute(name="timestamp")
    private String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    public static BatchKillResponseMessage create_response() {
        return new BatchKillResponseMessage();
    }
    
    private BatchKillResponseMessage(){
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, getClass());
    }

}
