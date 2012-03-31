package server.registrar.task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Enumeration;

import javax.xml.bind.JAXBException;

import server.ServerInfo;
import server.job.IMJob;
import server.registrar.ClientRegistrar;
import server.task.AbstractTask;

import comm.msg.IMMessage;

import data.InstantMessage;


//FIXME: This will need to be cleaned up... bad generics here...
public class RegistrarIMTask extends AbstractTask<InstantMessage> {
	
	private ClientRegistrar registrar;
	private ServerInfo to;
	private IMMessage msg;
	
	private void find(){
		InstantMessage im = msg.get_im();
		Enumeration<ServerInfo> searcher = registrar.get_reader();
		while(searcher.hasMoreElements()){
			to = searcher.nextElement();
			if(to.get_name().equals(im.get_handle())){
				break;
			}
			to = null;
		}
	}
	
	public void run(){
		try{
			msg = ((IMJob) job).get_message();
			find();
			if(to != null){
				socket.send(msg, InetAddress.getByName(to.get_ip()), to.get_port());
			}
		}catch(IOException ioe){ 
			ioe.printStackTrace();
		} catch (JAXBException jaxbe) {
			jaxbe.printStackTrace();
		}
	}
	
	public static RegistrarIMTask spawn(IMJob job, ClientRegistrar registrar) throws SocketException{
		return new RegistrarIMTask(job, registrar);
	}
	

	
	private RegistrarIMTask(IMJob job, ClientRegistrar registrar) throws SocketException{
		super(job, null);
		this.registrar = registrar;
	}
}
