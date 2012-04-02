package client;

import static java.lang.System.out;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;
import server.ServerInfo;
import utils.CommUtilities;

import comm.msg.IMMessage;

import data.InstantMessage;
import data.record.InvalidRecordException;

public abstract class AbstractMessageArbiter extends Thread{
	
	private SpecialSocket socket;
	private List<InstantMessage> messages;
	private AtomicBoolean working;
	private int port_num;
	

	private ServerInfo me;
	
	public String get_handle(){ return me.get_name(); }
	public ServerInfo get_profile(){ return me; }
	public int get_port(){ return port_num; }
	
	private void add_message(InstantMessage im){
		messages.add(im);
	}
	
	public void view_messages(String client){
		for(InstantMessage im : messages){
			if(im.get_handle().equals(client)){
				out.printf("%s (%s): %s\n", im.get_handle(), im.get_timestamp(), im.get_message());
			}
		}
	}
	
	public void view_messages(){
		for(InstantMessage im : messages){
			out.printf("%s (%s): %s\n", im.get_handle(), im.get_timestamp(), im.get_message());
		}
	}
	
	private void handle_message(DatagramPacket packet) throws JAXBException{
		IMMessage msg = (IMMessage) CommUtilities.bytes_2_java(packet.getData(), IMMessage.class);
		add_message(msg.get_im());
	}
	
	public void close(){
		working.set(false);
	}
	
	public void run(){
		DatagramPacket packet;
		while(working.get()){
			if((packet = socket.non_blocking_accept()) != null){
				try {
					handle_message(packet);
					out.println("New message received");
				} catch (JAXBException e) {
					// Drop message...
				}
			}
		}
		out.println("Done listening... closing");	
	}
	
	protected AbstractMessageArbiter(String handle, int port) throws SocketException, InvalidRecordException{
		this.port_num = port;
		socket = SpecialSocket.create_socket(port);
		me = ServerInfo.wrap(handle, socket.get_inet().getHostAddress(), socket.get_port());
		working = new AtomicBoolean(true);
		setDaemon(true);
	}

}
