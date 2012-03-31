package comm.resp;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import utils.CommUtilities;

@XmlRootElement(name="GenericResponse")
public class GenericResponse implements Message {

	@XmlElement(name="response")
	protected String response;
	
    @XmlElement(name="timestamp")
    protected String timestamp;
	
	public int get_priority() { return 0; }
    public String get_response(){ return response; }
    public String get_timestamp(){ return timestamp; }
    
	public byte[] to_bytes() throws JAXBException {
		return CommUtilities.java_2_bytes(this, getClass());
	}

	public static GenericResponse create_response(String response){
		return new GenericResponse(response);
	}
	
	protected GenericResponse(String response){
		this.response = response;
		this.timestamp = CommUtilities.get_timestamp();
	}
	
	protected GenericResponse(){}

}
