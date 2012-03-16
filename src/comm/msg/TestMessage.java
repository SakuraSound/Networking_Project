package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

/**
 * The test message for pinging a server
 * @author Hatomi
 *
 */
@XmlRootElement(name="TestMessage")
public class TestMessage implements Message {

    @XmlAttribute(name="timestamp")
    private String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    public static TestMessage create_message(){
        return new TestMessage();
    }
    
    
    private TestMessage(){
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, getClass());
    }

}
