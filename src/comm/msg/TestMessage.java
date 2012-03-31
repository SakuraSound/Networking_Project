package comm.msg;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The test message for pinging a server
 * @author Hatomi
 *
 */
@XmlRootElement(name="TestMessage")
public class TestMessage extends AbstractMessage {
    
    public static TestMessage create_message(){
        return new TestMessage();
    }
    
    private TestMessage(){
    	super(null, 1);
    }
}
