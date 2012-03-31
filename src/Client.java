import static java.lang.System.out;
import inter.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;
import server.ServerInfo;
import server.job.Job;
import utils.CommUtilities;
import utils.DataUtilities;
import client.AbstractMessageArbiter;

import comm.msg.DeleteMessage;
import comm.msg.KillMessage;
import comm.msg.LinkMessage;
import comm.msg.ReadMessage;
import comm.msg.RegisterMessage;
import comm.msg.TestMessage;
import comm.msg.WriteMessage;
import comm.resp.ErrorMessage;
import comm.resp.GenericResponse;
import comm.resp.ReadListMessage;

import data.SearchQuery;
import data.record.InvalidRecordException;
import data.record.Record;


public class Client {
    
    private static boolean running;
    private static SpecialSocket socket;
    private static MessageArbiter arbiter;
    private static int port_num;
    private static String ip_address;
    private static String target_db;
    
    private static List<Record> disconnected_servers;
    
    
    private static void disconnected() throws InvalidRecordException{
    	disconnected_servers.add(Record.create_record(target_db, ip_address, port_num));
    	port_num = 0;
    	ip_address = null;
    	target_db = null;
    }

    
    private static void print_separator(String point){
        for(int i=0;i<110;i++) out.print(point.charAt(0));
        out.println();
    }
    
    private static void display_clients(List<ServerInfo> clients, String timestamp){
        //TODO: When paging, we can change this accordingly
        out.printf("%65s\n", "RETRIEVED Clients");
        print_separator("_");
        String format = "%5s %80s %80s\n";
        int counter = 1;
        out.printf("%5s %80s %80s\n", "NUM.", "Client", "Server");
        print_separator("_");
        if(clients == null)
            clients = new ArrayList<ServerInfo>();
        for(ServerInfo info : clients){
            out.printf(format, counter++, info.get_name(), info.get_registered_server_name());
        }
        out.println();
        print_separator("_");
        out.printf("Retrieved %d clients from %s:%d/%s at %s\n", clients.size(), ip_address, port_num, target_db, timestamp);
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void display_records(List<Record> records, String timestamp){
        //TODO: When paging, we can change this accordingly
        out.printf("%65s\n", "RETRIEVED RECORDS");
        print_separator("_");
        String format = "%5s %80s %16s %05d\n";
        int counter = 1;
        out.printf("%5s %80s %16s %5s\n", "NUM.", "Record name", "IP Address", "Port");
        print_separator("_");
        if(records == null)
            records = new ArrayList<Record>();
        for(Record record : records){
            out.printf(format, counter++, record.get_name(), record.get_ip(), record.get_port());
        }
        out.println();
        print_separator("_");
        out.printf("Retrieved %d records from %s:%d/%s at %s\n", records.size(), ip_address, port_num, target_db, timestamp);
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    //TODO: Add register and unregister commands to the system
    private static void menu(){
        out.println();
        out.println("___________________________________________");
        out.println("List of commands.");
        out.println("___________________________________________");
        out.println(" 0. Server <ip_address> <port_num> [dbname]");
        out.println(" 1. Show    (currently unavailable)        ");
        out.println(" 2. Insert <name> <ip_address> <port_num>  ");
        out.println(" 3. Delete <name> [ip_address] [port_num]  ");
        out.println(" 4. Find   <wild_name> <wild_ip>           ");
        out.println(" 5. Test   <ip_address> <port_num>         ");
        out.println(" 6. Switch [dbname] (currently unavailable)");
        out.println(" 7. Kill   [dbname]                        ");
        out.println(" 8. Help                                   ");
        out.println(" 9. Close                                  ");
        out.println("___________________________________________");
        out.print("Enter Choice (0-9): ");
    }
    
    
    private static String get_valid_name(boolean can_be_wild){
        Scanner scan = new Scanner(System.in);
        boolean valid_name = false;
        String name;
        out.print("Record Name (max 80)"+(can_be_wild? "with wildcard *":"")+": ");
        do{
            name = scan.nextLine().trim();
            if(! (valid_name = DataUtilities.valid_name(name))){
                out.println("Invalid record name. Name should be between 1 and 80 characters in length.");
            }
        }while(!valid_name);
        return name;
    }
    
    private static String get_valid_ip(boolean can_be_wild, boolean can_be_null){
        Scanner scan = new Scanner(System.in);
        boolean valid_ip = false;
        String ip_addr;
        out.print("Record IP (X.X.X.X with X [0-255]"+(can_be_wild? " and *":"")+(can_be_null?" or enter to skip":"") +"): ");
        do{
            ip_addr = scan.nextLine().trim();
            if(can_be_wild){
                if(! (valid_ip = DataUtilities.valid_wild_ip(ip_addr))){
                    out.println("Invalid IP address. Address in form of X.X.X.X, where X is number from 0-255 with wildcard * per digit.");
                }
            }else{
                if(! (valid_ip = DataUtilities.valid_ip(ip_addr))){
                    if(can_be_null){
                        if(ip_addr.length() == 0){
                            break;
                        }else
                            out.println("Invalid IP address. Address in form of X.X.X.X, where X is number from 0-255 (or hit enter to leave null).");
                    }else{
                        out.println("Invalid IP address. Address in form of X.X.X.X, where X is number from 0-255.");
                    }
            }
            }
        }while(!valid_ip);
        return ip_addr;
    }
    
    private static String get_target_db(){
        Scanner scan = new Scanner(System.in);
        String target = null;
        out.print("Name of database (just press enter for temp store): ");
        if(scan.hasNextLine()){
            target = scan.nextLine();
        }
        return target.length() == 0? null : target;
    }
    
    private static int get_valid_port(boolean can_be_0){
        Scanner scan = new Scanner(System.in);
        boolean valid_port = false;
        int port = 0;
        out.print("Record Port (1024-65535)"+(can_be_0?" or enter to skip":"")+": ");
        do{
            try{
                port = Integer.valueOf(scan.nextLine().trim());
                if(! (valid_port = DataUtilities.valid_port(port))){
                    out.println("Invalid port. Port is integer betwee 1024-65535.");
                }
            }catch(NumberFormatException nfe){
                if(can_be_0) break;
            }
        }while(!valid_port);
        return port;
    }
    
    private static boolean handle_response(byte[] data, Class<?> expected) throws JAXBException{
        Message msg;
        try{
            msg = CommUtilities.bytes_2_java(data, expected);
            return true;
        }catch(JAXBException jaxbe){
            try{
                msg = CommUtilities.bytes_2_java(data, ErrorMessage.class);
                out.printf("%s at %s", ((ErrorMessage) msg).get_error().get_message(), msg.get_timestamp());
                return false;
            }catch(JAXBException jxb){ return false; }
        }
    }
    
    private static void add_record() throws UnknownHostException, IOException, JAXBException, InvalidRecordException{
        if(port_num != 0 && ip_address != null){
            Record record = Record.create_record(get_valid_name(false), get_valid_ip(false, false), get_valid_port(false));
            WriteMessage<Record> msg = WriteMessage.create_message(Job.WRITE, record, target_db, 1);
            socket.send(msg, InetAddress.getByName(ip_address), port_num);
            if(handle_response(socket.accept().getData(), GenericResponse.class))
                out.printf("Successfully added record to %s:%d/%s at %s\n", ip_address, port_num, target_db,msg.get_timestamp());
        }else out.println("You must provide a server address to run this command.");
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }

    private static int valid_input(String value){
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException nfe){ return -1; }
        
    }
    
    
    private static void init() throws SocketException{
        running = true;
        socket = SpecialSocket.create_socket();
    }
    
    private static void link() throws UnknownHostException, IOException, JAXBException{
    	String server_name = get_valid_name(false);
    	LinkMessage msg = LinkMessage.create_message(Job.LINK, server_name, 4);
    	socket.send(msg, InetAddress.getByName(ip_address), port_num);
    	if(handle_response(socket.accept().getData(), GenericResponse.class))
    		out.printf("Successfully linked %s to %s.\n", target_db, server_name);
    	else out.println("Unable to form the link... does the server know of it?");
    	out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void unlink() throws UnknownHostException ,JAXBException, IOException{
    	String server_name = get_valid_name(false);
    	LinkMessage msg = LinkMessage.create_message(Job.UNLINK, server_name, 4);
    	socket.send(msg, InetAddress.getByName(ip_address), port_num);
    	if(handle_response(socket.accept().getData(), GenericResponse.class))
    		out.printf("Successfully unlinked %s from %s.\n", target_db, server_name);
    	else out.printf("Unable to disconnect the link... did the link exist?\n");
    	out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void register() throws InvalidRecordException, UnknownHostException, IOException, JAXBException{
    	String client_name = get_valid_name(false);
    	int port = get_valid_port(false);
    	if(arbiter == null){
    		arbiter = new MessageArbiter(client_name, port);
    		arbiter.start();
    		RegisterMessage msg = RegisterMessage.create_message(Job.REGISTER, arbiter.get_profile(), 5);
        	socket.send(msg, InetAddress.getByName(ip_address), port_num);
        	byte[] data = socket.accept().getData();
        	if(handle_response(data, GenericResponse.class)){
        		out.printf("Successfully registered %s to server.\n", client_name);
        	}else if(handle_response(data, ErrorMessage.class)){
        		ErrorMessage msg2 = (ErrorMessage) CommUtilities.bytes_2_java(data, ErrorMessage.class);
        		out.println(msg2.get_error().get_message());
        	}else{
        		out.println("General Error with request");
        	}
    	}else{
    		out.printf("Already registered as %s:%d.\n", arbiter.get_profile().get_name(), arbiter.get_profile().get_port());
    	}
    	
    }
    
    private static void unregister() throws IOException, JAXBException{
    	if(arbiter != null){
    		SearchQuery delete_query = SearchQuery.make_delete_query(arbiter.get_profile().get_name(), 
    																	arbiter.get_profile().get_ip(), 
    																		arbiter.get_profile().get_port());
    		RegisterMessage msg = RegisterMessage.create_message(Job.UNREGISTER, delete_query , 5);
    		socket.send(msg, InetAddress.getByName(ip_address), port_num);
        	byte[] data = socket.accept().getData();
        	if(handle_response(data, GenericResponse.class)){
        		out.println("Successfully unregistered handle from server");
        		arbiter.close();
        		arbiter = null;
        		
        	}else{
        		ErrorMessage msg2 = (ErrorMessage) CommUtilities.bytes_2_java(data, ErrorMessage.class);
        		out.println(msg2.get_error().get_message());
        	}
    	}else out.println("Unable to unregister... It seems like you havent registered yet....");
    }
    
    private static void client_list() throws UnknownHostException, IOException, JAXBException{
    	String server_name = get_valid_name(true);
    	String client_name = get_valid_name(true);
    	SearchQuery query = SearchQuery.make_retrieve_query(client_name, "*");
    	ReadMessage msg = ReadMessage.create_message(Job.CLIENT_SEARCH, query, server_name, 2);
    	socket.send(msg, InetAddress.getByName(ip_address), port_num);
    	DatagramPacket pkt = socket.accept();
    	byte[] data = pkt.getData();
    	if(handle_response(data, ReadListMessage.class)){
    		ReadListMessage<ServerInfo> msg2 = (ReadListMessage<ServerInfo>) CommUtilities.bytes_2_java(data, ReadListMessage.class);
    		display_clients(msg2.get_records(), msg2.get_timestamp());
    		socket.send(GenericResponse.create_response("successful"), pkt.getAddress(),pkt.getPort());
    	}else out.println("Unable to obtain list...");
    	
    }
    
    private static void send_message() throws SocketException{
    	
    }
    
    private static void test(){
        String ip = get_valid_ip(false, false);
        int port = get_valid_port(false);
        TestMessage msg = TestMessage.create_message();
        try{
            socket.send(msg, InetAddress.getByName(ip), port);
            DatagramPacket pkt = socket.accept(1024, 5000);
            TestMessage tm = (TestMessage) CommUtilities.bytes_2_java(pkt.getData(), TestMessage.class);
            
            out.printf("Host %s:%d reached at %s\n", pkt.getAddress().getHostAddress(), 
                       pkt.getPort(), tm.get_timestamp());
        }catch(UnknownHostException uhe){
            out.println("Unknown host...");
        } catch (IOException e) {
            out.println("Unable to reach the host... timed out");
        }catch(JAXBException jaxbe){
            out.println("Unable to reach the host... error at host");
        }
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void show(){
        out.println("Currently Unavailable...");
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void delete() throws JAXBException, IOException{
        if(port_num != 0 && ip_address != null){
            String name = get_valid_name(false);
            String ip = get_valid_ip(false, true);
            ip = (ip.length() > 0) ? ip : null;
            int port = get_valid_port(true);
            SearchQuery query = SearchQuery.make_delete_query(name, ip, port);
            DeleteMessage msg = DeleteMessage.create_message(Job.DELETE, query, target_db, 1);
            socket.send(msg, InetAddress.getByName(ip_address), port_num);
            if (handle_response(socket.accept().getData(), GenericResponse.class))
                out.printf("Successfully deleted record from %s:%d at %s\n", ip_address, port_num, msg.get_timestamp());
        }else out.println("You must provide a server address to run this command.");
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void setup(){
        ip_address = get_valid_ip(false, false);
        port_num = get_valid_port(false);
        target_db = get_target_db();
        out.println("Server information set... I hope you tested these first.");
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    @SuppressWarnings("unchecked")
	private static void find() throws UnknownHostException, IOException, JAXBException{
        if(port_num != 0 && ip_address != null){
            String name = get_valid_name(true);
            String ip = get_valid_ip(true, false);
            ip = (ip.length() > 0) ? ip : null;
            SearchQuery query = SearchQuery.make_retrieve_query(name, ip);
            ReadMessage msg = ReadMessage.create_message(Job.READ, query, target_db, 1);
            socket.send(msg, InetAddress.getByName(ip_address), port_num);
            DatagramPacket pkt = socket.accept();
            if(handle_response(pkt.getData(), ReadListMessage.class)){
                ReadListMessage<Record> rlm = (ReadListMessage<Record>) (CommUtilities.bytes_2_java(pkt.getData(), 
                                                                                    ReadListMessage.class));
                display_records(rlm.get_records(), rlm.get_timestamp());
                socket.send(GenericResponse.create_response("Received data"), pkt.getAddress(), port_num);
            }
        }else out.println("You must provide a server address to run this command.");
    }
    
    
    
    private static void kill() throws UnknownHostException, IOException, JAXBException, InvalidRecordException{
        if(port_num != 0 && ip_address != null){
            Class<?> expected = GenericResponse.class;
            
            String db = get_target_db();
            
            KillMessage msg = KillMessage.create_message(db, 1);
            socket.send(msg, InetAddress.getByName(ip_address), port_num);
            DatagramPacket pkt = socket.accept();
            if(handle_response(pkt.getData(), expected)){
                GenericResponse resp = (GenericResponse) CommUtilities.bytes_2_java(pkt.getData(), expected);
                if(!resp.get_response().equals("kill")){
                	disconnected();
                }
            }else out.println("Error in shutting down..."); //Dont think this will happen...
            
        }else out.println("You must provide a server address to run this command.");
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void switch_db(){
        String db = target_db;
        target_db = get_target_db();
        out.printf("Switched db from %s to %s at %s\n", db, target_db, CommUtilities.get_timestamp());
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    private static void help(){
        out.println("Server: User provides server connect information");
        out.println("Show:   Displays to user available db connections on server");
        out.println("Insert: Insert a new record into a db. If db isn't there,");
        out.println("        a new db is created on server.");
        out.println("Delete: Delete a record from a database.");
        out.println("Find:   Locate record(s) in a db on the server.");
        out.println("Test:   Test whether a server exists at a particular location.");
        out.println("Switch: Allows user to switch to another db_store");
        out.println("        To access temp_store again, just hit enter ");
        out.println("        on command.");
        out.println("Kill:   Shuts down a single db (with db_name given) or shuts ");
        out.println("        down the entire server. If store was given name, then");
        out.println("        the store is saved in the data/ folder.");
        out.println("Help:   Yo dawg! I heard you needed help with help, so i put a");
        out.println("        Help description in your help description so you could");
        out.println("        ask a stupid question while asking a somewhat reasonable");
        out.println("        question.");
        out.println("Close:  Closes the client application...");
        out.print("Press enter to continue");
        new Scanner(System.in).nextLine();
    }
    
    
	public static void main(String[] args) throws InvalidRecordException, UnknownHostException, IOException, JAXBException {
	    init();
	    Scanner scanner = new Scanner(System.in);
	    while(running){
	        int choice;
	        menu();
	        if ((choice = valid_input(scanner.nextLine())) != -1){
	            switch(choice){
	                case 0:
	                    setup();
	                    break;
	                case 1:
	                    show();
	                    break;
	                case 2:
	                    add_record();
	                    break;
	                case 3:
	                    delete();
	                    break;
	                case 4:
	                    find();
	                    break;
	                case 5: 
	                    test();
	                    break;
	                case 6:
	                    switch_db();
	                    break;
	                case 7:
	                    kill();
	                    break;
	                case 8:
	                    help();
	                    break;
	                case 9:
	                default:
	                    running = false;
	            }
	        }else continue;
	    }
	    
	    out.println("Closing client...");
	}

	static class MessageArbiter extends AbstractMessageArbiter{

		protected MessageArbiter(String client_name, int port) throws SocketException, InvalidRecordException {
			super(client_name, port);
		}
		
	}
	
}
