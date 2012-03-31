package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.job.Job;
import data.SearchQuery;

/**
 * The delete message
 * contains the targetdb
 *          the timestamp
 *          and the delete query
 * @author Hatomi
 *
 */
@XmlRootElement(name="DeleteMessage")
public class DeleteMessage extends AbstractMessage {

    
    @XmlElement(name="target_db")
    private String target_db;
    @XmlElement(name="delete_query")
    private SearchQuery delete_query;
    
    public String get_store(){ return target_db; }
    public SearchQuery get_query(){ return delete_query; }
    
    private DeleteMessage(){}
    
    private DeleteMessage(Job job, SearchQuery query, String target_db, int priority){
    	super(job, priority);
    	this.delete_query = query;
    	this.target_db = target_db;
    }
    
    public static DeleteMessage create_message(Job job, SearchQuery query, String target_db, int priority){
        return new DeleteMessage(job, query, target_db, priority);
    }
    
}
