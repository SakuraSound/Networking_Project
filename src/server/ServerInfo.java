package server;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import data.record.AbstractRecord;
import data.record.InvalidRecordException;

@XmlRootElement(name="ServerInfo")
public final class ServerInfo extends AbstractRecord {
	
	@XmlElement(name="registered_to")
	private String server_name;
	
	@XmlElement(name="registered_ip")
	private String registered_ip;
	
	@XmlElement(name="registered_ip")
	private int registered_port;
	
	
	
	public String get_registered_server_name(){ return server_name; }
	public String get_registered_server_ip(){ return registered_ip; }
	public int get_registered_port(){ return registered_port; }
	public void set_registered_server_name(String name){ this.server_name = name; }
	
	public final String toString(){
		return "Handle: "+name+"\nAddress: "+ip_addr+"\nPort Num: "+port_num;
	}
	
	
	
	/**
	 * Take a HashMap containing a series of ServerINfo
	 * @param links
	 */
	public static final void file_from_links(final ConcurrentHashMap<String, ServerInfo> links, final String filename)
		throws IOException{
		try{
			write(links, filename);
		}catch(FileNotFoundException fnfe){ // Throws this if file given is directory instead of file...
			out.println("Directory given instead of file, writing tempfile");
			String tmpname = filename+"."+currentTimeMillis();
			write(links, filename);
			out.printf("Written data to %s\n", tmpname);
		}
		
	}
	
	/**
	 * Reads in a linkfile and parses out all the linked info and returns a map with logical server names and
	 * connection information. <br />
	 * The format of this file created is of the form : logical_name@?@string_ip@?@port_number
	 * @param filename the name of the file we want to extract information from
	 * @return a ConcurrentHashMap with a String key and ServerInfo value
	 */
	public static final ConcurrentHashMap<String, ServerInfo> links_from_file(String filename){
		ConcurrentHashMap<String, ServerInfo> related_servers = new ConcurrentHashMap<String, ServerInfo>();
		File linkfile = new File(filename);
		if(linkfile.exists() && linkfile.isFile()){
			try{
				Scanner scanner = new Scanner(linkfile);
				while(scanner.hasNextLine()){
					String[] info = scanner.nextLine().split("@?@");
					try{
						related_servers.put(info[0], wrap(info[0], info[1] , Integer.valueOf(info[2])));
					}catch(NumberFormatException nfe){
						out.println("Poorly formatted port number found, ignoring link");
					} catch (InvalidRecordException e) {
						out.println("Invalid tuple, ignoring");
					}
				}
			}catch(IOException ioe){
				out.println("Unable to read/parse the linkfile");
			}
		}
		return related_servers;
	}
	
	/**
	 * Factory Method for server info wrapper class
	 * @param string the server address
	 * @param port_no the connecting port number (entry port number)
	 * @return new ServerInfo wrapper object
	 * @throws InvalidRecordException 
	 */
	public static final ServerInfo wrap(final String handle, final String ip, final int port_no) throws InvalidRecordException{
		if(validate(handle, ip, port_no)){
			return new ServerInfo(handle, ip, port_no);
		}else throw new InvalidRecordException();
		
	}
	
	
	private ServerInfo(final String handle, final String ip, final int port_no){
		super(handle, ip, port_no);
	}
	
	
	private static void write(final ConcurrentHashMap<String, ServerInfo> links, final String filename) throws IOException{
		File tmpfile = new File(filename);
		PrintStream ps = new PrintStream(new FileOutputStream(tmpfile));
		for(Map.Entry<String, ServerInfo> entry: links.entrySet()){
			ps.printf("%s@?@%s@?@%d", entry.getKey(), entry.getValue().get_ip(),
													  entry.getValue().get_port());	
		}
		ps.close();
	}
	
	public boolean equals(Object record){
		if(!(record instanceof ServerInfo)) return false;
		else{
			ServerInfo rec = (ServerInfo) record;
			return name.equals(rec.get_name())
					&& ip_addr.equals(rec.get_ip());
		}
	}
		
	
	private ServerInfo(){}
	
	

}
