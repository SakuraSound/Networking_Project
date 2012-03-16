package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import comm.CommUtilities;

import data.SearchQuery;

/**
 * The read message
 * Holds the query responsible for record retrieval
 * @author Hatomi
 *
 */
@XmlRootElement(name="ReadMessage")
public class ReadMessage implements Message {
    @XmlElement(name="store_name")
    private String store_name;
    @XmlElement(name="query")
    private SearchQuery query;
    @XmlAttribute(name="timestamp")
    private String timestamp;
    
    public String get_store(){ return store_name; }
    public SearchQuery get_query(){ return query; }
    public String get_timestamp(){ return timestamp; }
    
    public static ReadMessage create_message(SearchQuery query){
        return create_message(query, null);
    }
    
    public static ReadMessage create_message(SearchQuery query, String store_name){
        return new ReadMessage(query, store_name);
    }
    
    private ReadMessage(){}
    
    private ReadMessage(SearchQuery query){
        this(query, null);
    }
    
    private ReadMessage(SearchQuery query, String store_name){
        this.query = query;
        this.store_name = store_name;
        this.timestamp = CommUtilities.get_timestamp();
    }
    
    @Override
    public byte[] to_bytes() throws JAXBException {
        return CommUtilities.java_2_bytes(this, ReadMessage.class);
    }

}
