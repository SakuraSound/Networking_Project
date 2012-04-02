package comm.msg;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import server.job.Job;

import data.InstantMessage;

@XmlRootElement(name="im_message")
public final class IMMessage extends AbstractMessage {
	@XmlElement(name="uuid")
	private  String uuid;
	@XmlElement(name="im")
	private InstantMessage im;
	
	public InstantMessage get_im(){ return im; }
	public String get_uuid(){ return uuid; }
	
	public static IMMessage create_message(InstantMessage im, String uuid){
		return new IMMessage(im, uuid);
	}
	
	private IMMessage(InstantMessage im, String uuid){
		super(Job.SEND_MESSAGE, 4);
		this.im = im;
		this.uuid = uuid;
	}
	
	private IMMessage(){}
	
	
}
