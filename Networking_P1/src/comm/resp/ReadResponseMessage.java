package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

@XmlRootElement(name="ReadResponseMessage")
public class ReadResponseMessage implements Message {
    @XmlAttribute(name="timestamap")
    private String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, getClass());
    }
    
    public static ReadResponseMessage create_response(){
        return new ReadResponseMessage();
    }
    
    private ReadResponseMessage(){
        this.timestamp = CommUtilities.get_timestamp();
    }

}
