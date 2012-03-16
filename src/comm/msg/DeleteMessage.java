package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

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
public class DeleteMessage implements Message {

    
    @XmlElement(name="target_db")
    private String target_db;
    @XmlAttribute(name="timestamp")
    private String timestamp;
    @XmlElement(name="delete_query")
    private SearchQuery delete_query;
    
    public String get_store(){ return target_db; }
    public SearchQuery get_query(){ return delete_query; }
    public String get_timestamp(){ return timestamp; }
    
    private DeleteMessage(){}
    
    private DeleteMessage(SearchQuery query){
        this(query, null);
    }
    
    
    private DeleteMessage(SearchQuery query, String target_db){
        this.delete_query = query;
        this.target_db = target_db;
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    public static DeleteMessage create_message(SearchQuery query){
        return new DeleteMessage(query);
    }
    
    public static DeleteMessage create_message(SearchQuery query, String target_db){
        return new DeleteMessage(query, target_db);
    }
    
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this,getClass());
    }

    
}
