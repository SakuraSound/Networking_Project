package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

/**
 * The Simple Kill message
 * Kills a db store if name specified, otherwise kills everything
 * Contains target_db
 *          timestamp
 * @author Hatomi
 *
 */
@XmlRootElement(name="KillMessage")
public class KillMessage implements Message {
    @XmlElement(name="target_db")
    private String target_db;
    @XmlAttribute(name="timestamp")
    private String timestamp;
    
    public String get_store(){ return target_db; }
    public String get_timestamp(){ return timestamp; }

    
    public static KillMessage create_message(String target){
        return new KillMessage(target);
    }
    
    public static KillMessage create_message(){
        return new KillMessage();
    }
    
    private KillMessage(){
        this(null);
    }
    
    private KillMessage(String target){
        this.target_db = target;
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, this.getClass());
    }

}
