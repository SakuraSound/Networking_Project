package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.job.Job;

/**
 * The Simple Kill message
 * Kills a db store if name specified, otherwise kills everything
 * Contains target_db
 *          timestamp
 * @author Hatomi
 *
 */
@XmlRootElement(name="KillMessage")
public class KillMessage extends AbstractMessage {
    
	@XmlElement(name="target_db")
    private String target_db;
    
    public String get_store(){ return target_db; }

    
    public static KillMessage create_message(String target, int priority){
        return new KillMessage(target, priority);
    }
    
    public static KillMessage create_message(int priority){
        return new KillMessage(null, priority);
    }
    
    private KillMessage(){}
    
    private KillMessage(String target, int priority){
    	super(Job.SHUT_DOWN, priority);
        this.target_db = target;
    }

}
