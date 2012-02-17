import static java.lang.System.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import org.apache.commons.io.FileUtils;

import store.IndexJob;
import store.RecordStore;

import comm.CommUtilities;
import comm.msg.DeleteMessage;
import comm.msg.KillMessage;
import comm.msg.ReadMessage;
import comm.msg.TestMessage;
import comm.msg.WriteMessage;
import comm.resp.BatchKillResponseMessage;
import comm.resp.KillResponseMessage;


public  class Server {
	
    private static ConcurrentHashMap<String, RecordStore> open_record_stores;
    private static AtomicBoolean serving;
    private static SpecialSocket socket;
    private static HashMap<String, String> configuration;
    
    
    
    private static final void init() throws IOException{
        open_record_stores = new ConcurrentHashMap<String, RecordStore>();
        FileUtils.forceMkdir(new File("../data/"));
        serving = new AtomicBoolean(true);
        socket = SpecialSocket.create_socket(Integer.valueOf(configuration.get("port_num")));
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
    
    private static void test_and_open_store(String name, boolean is_temp) throws IOException{
     // check if store already open
        if(!open_record_stores.containsKey(name)){
            // Create the temp store and add it to open stores then add job
            RecordStore store = is_temp? RecordStore.create_temp_database(): RecordStore.create_new_database(name);
            store.start();
            open_record_stores.put(name, store);
        }
    }
    
    private static boolean is_read(DatagramPacket packet) throws IOException{
        try{
            ReadMessage msg = (ReadMessage) CommUtilities.bytes_2_java(packet.getData(), ReadMessage.class);
            // Check to see if accessing temp store
            String name = msg.get_store() == null? "TempDB": msg.get_store();
            test_and_open_store(name, msg.get_store() == null);
            open_record_stores.get(name).add_job(IndexJob.create_job(msg.get_query(), false, packet.getAddress(), packet.getPort()));
            return true;
        }catch(JAXBException jaxbe){return false;}
    }
    
    private static boolean is_write(DatagramPacket packet) throws IOException{
        try{
            WriteMessage msg = (WriteMessage) CommUtilities.bytes_2_java(packet.getData(), WriteMessage.class);
            String name = msg.get_store() == null? "TempDB": msg.get_store();
            test_and_open_store(name, msg.get_store() == null);
            open_record_stores.get(name).add_job(IndexJob.create_job(msg.get_record(), packet.getAddress(), packet.getPort()));
            return true;
        }catch(JAXBException jaxbe){ return false; }
    }
    
    private static boolean is_delete(DatagramPacket packet) throws IOException{
        try{
            DeleteMessage msg = (DeleteMessage) CommUtilities.bytes_2_java(packet.getData(), DeleteMessage.class);
            // Check to see if accessing temp store
            String name = msg.get_store() == null? "TempDB": msg.get_store();
            test_and_open_store(name, msg.get_store() == null);
            open_record_stores.get(name).add_job(IndexJob.create_job(msg.get_query(), true, packet.getAddress(), packet.getPort()));
            return true;
        }catch(JAXBException jaxbe){return false;}
    }
    
    private static boolean is_work(DatagramPacket packet) throws IOException{
        return is_read(packet) || is_write(packet) || is_delete(packet);
        
    }
    
    protected static boolean is_test(DatagramPacket packet) throws IOException{
        try{
            TestMessage msg = (TestMessage) CommUtilities.bytes_2_java(packet.getData(), TestMessage.class);
            socket.send(msg, packet.getAddress(), packet.getPort());
            return true;
        }catch(JAXBException jaxbe){ return false; }
    }
    
    protected static boolean is_kill(DatagramPacket packet) throws IOException{
        try{
            KillMessage msg = (KillMessage) CommUtilities.bytes_2_java(packet.getData(), KillMessage.class);
            if(msg.get_store() != null){
                open_record_stores.get(msg.get_store()).add_job(IndexJob.create_job(5, packet.getAddress(), packet.getPort()));
                socket.send(KillResponseMessage.create_response(), packet.getAddress(), packet.getPort());
                open_record_stores.remove(msg.get_store());
            }else{
                for(Map.Entry<String, RecordStore> key_val : open_record_stores.entrySet()){
                    key_val.getValue().add_job(IndexJob.create_job(5, packet.getAddress(), packet.getPort()));
                    open_record_stores.remove(key_val.getKey());
                }
                serving.set(false);
                socket.send(BatchKillResponseMessage.create_response(), packet.getAddress(), packet.getPort());
            }
            return true;
        }catch(JAXBException jaxbe){ return false; }
    }
    
	public static void main(String... args) throws IOException, ParseException{
	    if (args.length != 1){
	        out.println("Commands are Server <port_num>  or Server <config-file>");
	    }else{
	        parse_args(args);
	        init();
	        out.println(27 + " [31;1m" +"*** Server Startup ***" + 27 + " [0m");
	        while(serving.get()){
	            DatagramPacket packet;
	            if((packet = socket.non_blocking_accept()) != null){
	                Demultiplexor.get_demux(packet).start();
	            }
	        }
	        out.println(27 + " [31;1m"+ "*** Waiting for tasks to complete ***" + 27 +" [0m");
	        while(!open_record_stores.isEmpty());
	        out.println(27 + " [31;1m" +"*** Server Shutdown ***" + 27 + " [0m");
	        System.exit(0);
	    }
		
		
	}
	
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
                is_test(packet);
                is_work(packet);
                is_kill(packet);
            } catch (IOException e) { e.printStackTrace(); } 
	    }
	}
	
	
}
