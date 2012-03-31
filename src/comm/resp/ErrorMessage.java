package comm.resp;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import utils.CommUtilities.ERROR;

/**
 * The Error message
 * Thrown when issues arise on the server or client.
 * @author Hatomi
 *
 */
@XmlRootElement(name="ErrorMessage")
public class ErrorMessage extends GenericResponse implements Comparable<ErrorMessage>{

    @XmlElement(name="error_type")
    private ERROR error_value;

    
    public ERROR get_error(){ return error_value; }
    
    public static ErrorMessage create_message(ERROR error){
        return new ErrorMessage(error);
    }
    
    private ErrorMessage(ERROR error){
    	super("Error Message");
        this.error_value = error;
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
