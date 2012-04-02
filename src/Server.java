import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import org.apache.commons.io.FileUtils;

import server.ServerInfo;
import server.job.AbstractJob;
import server.job.IMJob;
import server.job.Job;
import server.job.KillJob;
import server.job.SearchJob;
import server.job.UpdateJob;
import server.job.WriteJob;
import server.registrar.ClientRegistrar;
import server.store.RecordStore;
import utils.CommUtilities;
import utils.CommUtilities.ERROR;
import utils.ds.BloomFilter;

import comm.msg.DeleteMessage;
import comm.msg.IMMessage;
import comm.msg.KillMessage;
import comm.msg.LinkMessage;
import comm.msg.ReadMessage;
import comm.msg.RegisterMessage;
import comm.msg.TestMessage;
import comm.msg.UpdateMessage;
import comm.msg.WriteMessage;
import comm.resp.ErrorMessage;
import comm.resp.GenericResponse;
import comm.resp.ReadListMessage;

import data.record.InvalidRecordException;
import data.record.Record;


public  class Server {
	private static Record self;
	private static RecordStore store;
	private static String name;
    private static ConcurrentHashMap<String, RecordStore> open_record_stores;
    private static AtomicBoolean serving;
    private static SpecialSocket socket;
    private static HashMap<String, String> configuration;
    private static ConcurrentHashMap<Record, ClientRegistrar> client_lists;
    private static List<Record> links;
    private static BloomFilter update_filter;
    private static BloomFilter message_filter;
    
    
    /**
     * Responsible for initiating the server, creating folders, socket, and opening the record map
     * @throws IOException
     * @throws InvalidRecordException 
     */
    private static final void init() throws IOException, InvalidRecordException{
    	FileUtils.forceMkdir(new File("../data/"));
        name = configuration.get("port_num");
        open_record_stores = new ConcurrentHashMap<String, RecordStore>();
        client_lists = new ConcurrentHashMap<Record, ClientRegistrar>();
        update_filter = BloomFilter.create_filter(150000, 600000);
        message_filter = BloomFilter.create_filter(300000, 1000000);
        links = new ArrayList<Record>();
        serving = new AtomicBoolean(true);
        socket = SpecialSocket.create_socket(Integer.valueOf(configuration.get("port_num")));
        out.println(configuration.get("port_num"));
        self = Record.create_record(UUID.randomUUID().toString(), socket.get_inet().getHostAddress(), socket.get_port());
    }
    
    private static void parse_config_file(File file) throws FileNotFoundException, ParseException{
        Scanner scanner = new Scanner(file).useDelimiter("|");
        while(scanner.hasNextLine()){
            String key_val = scanner.nextLine();
            String[] pair = key_val.split(":");
            if(pair.length != 2) throw new ParseException("Problem getting key_value pair", 0);
            configuration.put(pair[0], pair[1]);
            
        }
    }
    
    private static void parse_args(String... args) throws FileNotFoundException, ParseException{
        try{
            configuration = new HashMap<String, String>();
            Integer.parseInt(args[0]);
            configuration.put("port_num", args[0]);
        }catch(NumberFormatException nfe){
            File file = new File(args[0]);
            if(file.exists() && file.isFile()){
                parse_config_file(file);
            }else{
                out.println("Argument was neither port number nor config file");
            }
        }
    }
    
    private static void test_and_open_store() throws IOException{
     // check if store already open
        if(!open_record_stores.containsKey(name)){
            // Create the temp store and add it to open stores then add job
            store =  RecordStore.create_new_database(name);
            store.start();
            open_record_stores.put(name, store);
        }
    }
    
    private static void test_and_open_list() throws IOException{
    	if(!client_lists.containsKey(self)){
    		ClientRegistrar reg = ClientRegistrar.create_registrar(self.get_name());
    		reg.start();
    		client_lists.put(self, reg);
    	}
    }
    
    private static void test_and_open_list(Record record) throws IOException{
    	if(!client_lists.containsKey(record)){
    		ClientRegistrar reg = ClientRegistrar.create_registrar(record.get_name());
    		reg.start();
    		client_lists.put(record, reg);
    	}
    }
    
    private static boolean is_read(DatagramPacket packet) throws IOException{
        try{
            ReadMessage msg = (ReadMessage) CommUtilities.bytes_2_java(packet.getData(), ReadMessage.class);
            // Check to see if accessing temp store
            test_and_open_store();
            open_record_stores.get(name).add_job(SearchJob.spawn(Job.READ, msg.get_query(), msg.get_priority(), packet.getAddress(), packet.getPort()));
            return true;
        }catch(JAXBException jaxbe){return false;}
         catch(ClassCastException cce){ return false; }
    }
    
    private static boolean is_write(DatagramPacket packet) throws IOException{
        try{
            WriteMessage<Record> msg = (WriteMessage<Record>) CommUtilities.bytes_2_java(packet.getData(), WriteMessage.class);
            test_and_open_store();
            open_record_stores.get(name).add_job(WriteJob.spawn(Job.WRITE, msg.get_record(), msg.get_priority(), packet.getAddress(), packet.getPort()));
            return true;
        }catch(JAXBException jaxbe){ return false; }
         catch(ClassCastException cce){ return false; }
    }
    
    private static boolean is_delete(DatagramPacket packet) throws IOException{
        try{
            DeleteMessage msg = (DeleteMessage) CommUtilities.bytes_2_java(packet.getData(), DeleteMessage.class);
            // Check to see if accessing temp store
            test_and_open_store();
            open_record_stores.get(name).add_job(SearchJob.spawn(Job.DELETE, msg.get_query(), msg.get_priority(), packet.getAddress(), packet.getPort()));
            return true;
        }catch(JAXBException jaxbe){return false;}
         catch(ClassCastException cce){ return false; }
    }
    
    private static boolean is_work(DatagramPacket packet) throws IOException{
        return is_read(packet) || is_write(packet) || is_delete(packet);
        
    }
    
    protected static boolean is_test(DatagramPacket packet) throws IOException{
        try{
        	out.println("Test Message received");
            TestMessage msg = (TestMessage) CommUtilities.bytes_2_java(packet.getData(), TestMessage.class);
            socket.send(GenericResponse.create_response("ping"), packet.getAddress(), packet.getPort());
            return true;
        }catch(JAXBException jaxbe){ return false; }
         catch(ClassCastException cce){ return false; }
    }
    
    protected static boolean is_kill(DatagramPacket packet) throws IOException{
        try{
            KillMessage msg = (KillMessage) CommUtilities.bytes_2_java(packet.getData(), KillMessage.class);
            open_record_stores.get(name).add_job(KillJob.spawn(5, packet.getAddress(), packet.getPort()));
            socket.send(GenericResponse.create_response("kill"), packet.getAddress(), packet.getPort());
            open_record_stores.remove(name);
            return true;
        }catch(JAXBException jaxbe){ return false; }
         catch(ClassCastException cce){ return false; }
    }
    
    public static boolean is_update(DatagramPacket packet) throws IOException{
    	try{
    		UpdateMessage msg = (UpdateMessage) CommUtilities.bytes_2_java(packet.getData(), UpdateMessage.class);
    		if(update_filter.not_planted(msg.get_uuid())){
				if(!client_lists.containsKey(msg.get_origin())){
					ClientRegistrar reg = ClientRegistrar.create_registrar(msg.get_origin().get_name());
					reg.start();
					client_lists.put(msg.get_origin(), reg);
				}
				UpdateJob ujob = UpdateJob.spawn(msg.get_job(), msg.get_client(), links, msg.get_uuid(), msg.get_priority());
				client_lists.get(msg.get_origin()).add_job(ujob);
				update_filter.grow(msg.get_uuid());
    		}
    		return true;
    		
    	}catch(JAXBException jaxbe){ return false; }
    	 catch(ClassCastException cce){ return false; }
    }
    
    public static boolean is_linkage(DatagramPacket packet) throws IOException{
    	try{
    		LinkMessage msg = (LinkMessage) CommUtilities.bytes_2_java(packet.getData(), LinkMessage.class);
    		if(open_record_stores.containsKey(name)){
    			Enumeration<Record> search = open_record_stores.get(name).get_reader();
    			while(search.hasMoreElements()){
    				Record rec = search.nextElement();
    				if(rec.get_name().equals(msg.get_server_name())){
    					if(links.contains(rec)){
    						socket.send(ErrorMessage.create_message(ERROR.DUPLICATE_LINK_ERROR), packet.getAddress(), packet.getPort());
    						return true;
    					}else{
    						test_and_open_list(self);
        					Enumeration<ServerInfo> clients = client_lists.get(self).get_reader();
        					while(clients.hasMoreElements()){
        						ServerInfo client = clients.nextElement();
        						String uuid = UUID.randomUUID().toString();
        						socket.send(UpdateMessage.create_message(msg.get_job(), client, self, uuid), InetAddress.getByName(rec.get_ip()), rec.get_port());
        					}
        					socket.send(GenericResponse.create_response("Link Successful"), packet.getAddress(), packet.getPort());
        					return true;
    					}
    				}
    			}
    		}
			ErrorMessage err = ErrorMessage.create_message(ERROR.LINKAGE_ERROR);
			socket.send(err, packet.getAddress(), packet.getPort());
    		return true;
    	}catch(JAXBException jaxbe){ return false; }
    	 catch(ClassCastException cce){ return false; }
    }
    
    public static boolean is_message(DatagramPacket packet) throws IOException, InvalidRecordException{
    	try{
    		IMMessage msg = (IMMessage) CommUtilities.bytes_2_java(packet.getData(), IMMessage.class);
    		if(message_filter.not_planted(msg.get_uuid())){
    			IMJob imjob = IMJob.spawn(msg, msg.get_priority());
    			Record reference = Record.recreate_record(msg.get_im().get_server(), msg.get_im().get_ip(), msg.get_im().get_port(), msg.get_im().get_timestamp());
    			test_and_open_list(reference);
    			client_lists.get(reference).add_job(imjob);
    			try{
    				socket.send(GenericResponse.create_response("Message Sent"), packet.getAddress(), packet.getPort());
    			}catch(JAXBException jaxbe) { 
    				jaxbe.printStackTrace(); 
    			}
    		}
    		return true;
    	}catch(JAXBException jaxbe){ return false; }
    	 catch(ClassCastException cce){ return false; }
    }

    
    public static boolean view_links(DatagramPacket packet) throws IOException{
    	
    	try{
    		LinkMessage msg = (LinkMessage) CommUtilities.bytes_2_java(packet.getData(), LinkMessage.class);
    		if(msg.get_job() != Job.VIEW_LINK) return false;
    		else{
    			ReadListMessage<Record> msg2 = ReadListMessage.create_message(links);
    			try {
					socket.send(msg2, packet.getAddress(), packet.getPort());
				} catch (JAXBException e) { 
					e.printStackTrace(); 
				}
    			return true;
    		}
    	}catch(ClassCastException cce){ return false; }
    	 catch(JAXBException jaxbe){ return false; }
    }
    
    public static boolean is_registrar(DatagramPacket packet) throws IOException{
    	try{
    		RegisterMessage msg = (RegisterMessage) CommUtilities.bytes_2_java(packet.getData(), RegisterMessage.class);
    		AbstractJob job = msg.get_job() == Job.REGISTER ?
    							WriteJob.spawn(msg.get_job(), msg.get_client(), msg.get_priority(), packet.getAddress(), packet.getPort(), self, links):
    							SearchJob.spawn(msg.get_job(), msg.get_query(), msg.get_priority(), packet.getAddress(), packet.getPort(), self,  links);
    		test_and_open_list();
    		client_lists.get(self).add_job(job);
    		return true;
    	}catch(JAXBException jaxbe){ return false; }
    	 catch(ClassCastException cce){ return false; }
    }
    

    
	public static boolean is_search(DatagramPacket packet) {
//		try{
//			ReadMessage msg = (ReadMessage) CommUtilities.bytes_2_java(packet.getData(), ReadMessage.class);
//			for(Entry<Record, ClientRegistrar> entry : client_lists.entrySet()){
//				if()
//			}
//		}catch(JAXBException jaxbe){ return false; }
//		return false;
		return false;
	}
	
	
    
	public static void main(String... args) throws IOException, ParseException, InvalidRecordException{
	    if (args.length != 1){
	        out.println("Commands are Server <port_num>  or Server <config-file>");
	    }else{
	        parse_args(args);
	        init();
	        out.println("*** Server Startup ***");
	        while(serving.get()){
	            DatagramPacket packet;
	            if((packet = socket.non_blocking_accept()) != null){
	                Demultiplexor.get_demux(packet).start();
	            }
	        }
	        out.println("*** Waiting for tasks to complete ***");
	        while(!open_record_stores.isEmpty());
	        out.println("*** Server Shutdown ***");
	        System.exit(0);
	    }
		
		
	}
	
	
	
	
	/**
	 *Responsible for passing received messages to the proper locations 
	 */
	private static final class Demultiplexor extends Thread{
	    private DatagramPacket packet;
	    
	    private Demultiplexor(DatagramPacket packet) throws SocketException{
	        this.packet = packet;
	    }
	    
	    public static Demultiplexor get_demux(DatagramPacket packet) throws SocketException{
	        return new Demultiplexor(packet);
	    }
	    
	    public void run(){
            try {
            	out.println("New Packet received");
                if(is_test(packet)) return; // Test if it is a test message
                if(is_work(packet)) return; // Test if it is a read/write/delete message
                if(is_kill(packet)) return; // Test if it is a kill message
                if(is_update(packet)) return; //Test if it is an update message
                if(is_registrar(packet)) return; // Test if it is a register/unregister message
                if(is_search(packet)) return; // Test to see if we are doing a list command
                if(is_linkage(packet)) return; // Test if it is a linkage message
                if(is_message(packet)) return; //Test if it is an instant message
            } catch (IOException e) { e.printStackTrace(); } 
              catch (InvalidRecordException e) { e.printStackTrace(); } 
	    }
	}

	
	
}
