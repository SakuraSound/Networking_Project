package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

@XmlRootElement(name="DeleteResponseMessage")
public class DeleteResponseMessage implements Message {

    @XmlAttribute(name="timestamp")
    private final String timestamp;
    
    public String get_timestamp(){ return timestamp; }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, DeleteResponseMessage.class);
    }

    public static DeleteResponseMessage create_response() {
        return new DeleteResponseMessage();
    }
    private DeleteResponseMessage(){
        this.timestamp = CommUtilities.get_timestamp();
    }

}
