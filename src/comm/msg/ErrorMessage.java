package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;
import comm.CommUtilities.ERROR;

/**
 * The Error message
 * Thrown when issues arise on the server or client.
 * @author Hatomi
 *
 */
@XmlRootElement(name="ErrorMessage")
public class ErrorMessage implements Message, Comparable<ErrorMessage>{

    @XmlElement(name="error_type")
    private ERROR error_value;
    @XmlElement(name="timestamp")
    private String timestamp;
    
    public ERROR get_error(){ return error_value; }
    public String get_timestamp(){ return timestamp; }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, this.getClass());
    }
    
    public static ErrorMessage create_message(ERROR error){
        return new ErrorMessage(error);
    }
    
    private ErrorMessage(ERROR error){
        this.error_value = error;
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    private ErrorMessage(ERROR error, String timestamp){
        this.error_value = error;
        this.timestamp = timestamp;
    }
    private ErrorMessage(String error_val){
        this.error_value = ERROR.from_string(error_val);
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    private ErrorMessage(){}
    
    public int compareTo(ErrorMessage msg){
        return timestamp.compareTo(msg.get_timestamp());
    }
    
    public boolean equals(ErrorMessage msg){
        return (this.error_value == msg.get_error()) &&
                (this.timestamp.equals(msg.get_timestamp()));
    }
    
}
