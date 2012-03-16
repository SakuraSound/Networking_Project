package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

import data.Record;

@XmlRootElement(name="WriteResponseMessage")
public class WriteResponseMessage implements Message {

    @XmlAttribute(name="timestamp")
    private final String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, this.getClass());
    }

    //TODO: Implement this
    public static final WriteResponseMessage create_write_response(Record record){
        return new WriteResponseMessage();
    }
    
    private WriteResponseMessage(){
        this.timestamp = CommUtilities.get_timestamp();
    }
}
