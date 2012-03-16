package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

@XmlRootElement(name="TestResponseMessage")
public class TestResponseMessage implements Message {

    @XmlAttribute(name="timestamp")
    private String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    private TestResponseMessage(){
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    public static TestResponseMessage create_response(){
        return new TestResponseMessage();
    }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, getClass());
    }

}
