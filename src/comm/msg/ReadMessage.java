package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.job.Job;
import data.SearchQuery;

/**
 * The read message
 * Holds the query responsible for record retrieval
 * @author Hatomi
 *
 */
@XmlRootElement(name="ReadMessage")
public class ReadMessage extends AbstractMessage {
	
    @XmlElement(name="store_name")
    private String store_name;
    @XmlElement(name="query")
    private SearchQuery query;
    
    public String get_store(){ return store_name; }
    public SearchQuery get_query(){ return query; }
    
    
    public static ReadMessage create_message(Job job, SearchQuery query, String store_name, int priority){
        return new ReadMessage(job, query, store_name, priority);
    }
    
    private ReadMessage(){}
    
    
    private ReadMessage(Job job, SearchQuery query, String store_name, int priority){
        super(job, priority);
    	this.query = query;
        this.store_name = store_name;
    }

}
