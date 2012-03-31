package comm.msg;

import inter.Message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import server.job.Job;
import utils.CommUtilities;
import utils.DataUtilities;

@XmlSeeAlso({DeleteMessage.class, KillMessage.class, 
	         ReadMessage.class, TestMessage.class, 
	         WriteMessage.class, UpdateMessage.class,
	         LinkMessage.class, RegisterMessage.class,
	         IMMessage.class})
public abstract class AbstractMessage implements Message {

	@XmlAttribute(name="timestamp")
	private String timestamp;
	
	@XmlElement(name="priority")
	private int priority;
	
	@XmlElement(name="job_type")
    private Job job_type;
	
	public Job get_job(){ return job_type; }
	
	public byte[] to_bytes() throws JAXBException {
		return CommUtilities.java_2_bytes(this, this.getClass());
	}

	public String get_timestamp() {
		return timestamp;
	}

	public int get_priority() {
		return priority;
	}

	protected AbstractMessage(Job job, int priority){
		this.job_type = job;
		this.priority = priority;
		this.timestamp = DataUtilities.get_timestamp();
	}
	
	protected AbstractMessage(){}
	
}
