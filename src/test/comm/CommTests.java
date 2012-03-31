package test.comm;


import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import utils.CommUtilities;
import utils.CommUtilities.ERROR;

import comm.resp.ErrorMessage;
import comm.resp.PrepareReadMessage;
import comm.resp.ReadListMessage;

import data.record.InvalidRecordException;
import data.record.Record;

public class CommTests {

    private TestThread from;
    private TestThread to;
    
    
    private class TestThread extends Thread{
        SpecialSocket socket;
        boolean sending;
        int answer;
        
        public int get_answer(){ return answer;}
        
        public void run(){
            try{
                if(sending){
                    PrepareReadMessage msg = PrepareReadMessage.create_message(15);
                    socket.send(msg, InetAddress.getLocalHost(), 5555);
                }else{
                    byte[] data = socket.accept().getData();
                    System.out.println(new String(data).trim().length());
                    PrepareReadMessage msg2 =  (PrepareReadMessage) CommUtilities.bytes_2_java(data,
                                                                                               PrepareReadMessage.class);
                    this.answer = msg2.get_next_bytes();
                    
                } 
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (JAXBException e) {
                fail(e.getMessage());
            }finally{
                socket.close();
                System.out.println("Answer "+((sending)?"Sent from":"Received by")+" me:"+this.answer);
            }
            return;
        }
        
        public TestThread(boolean sending, int portnum) throws SocketException{
            this.sending = sending;
            this.socket = SpecialSocket.create_socket(portnum);
        }
        
        public TestThread(boolean sending, int portnum, int answer) throws SocketException{
            this.sending = sending;
            this.socket = SpecialSocket.create_socket(portnum);
            this.answer = answer;
        }
    }
    
    @Before
    public void setUp() throws Exception {
       
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void test_error_msg() throws JAXBException{
        ErrorMessage msg = ErrorMessage.create_message(ERROR.INVALID_RECORD);
        String xml = new String(msg.to_bytes());
        System.out.println(xml);
        ErrorMessage fin = (ErrorMessage) CommUtilities.bytes_2_java(msg.to_bytes(), msg.getClass());
        System.out.println(new String(fin.to_bytes()));
        assertEquals(true, msg.equals(fin));
    }
    

    @SuppressWarnings("unchecked")
	@Test
    public void test_read_list_marshal() throws InvalidRecordException, JAXBException{
        List<Record> record_list = new ArrayList<Record>();
        record_list.add(Record.create_record("test record", "127.0.0.1", 5555));
        record_list.add(Record.create_record("test record 2", "127.0.0.1", 5556));
        ReadListMessage<Record> msg = ReadListMessage.create_message(record_list);
        System.out.println(new String(msg.to_bytes()));
        ReadListMessage<Record> test_msg = (ReadListMessage<Record>) CommUtilities.bytes_2_java(msg.to_bytes(), msg.getClass());
        for(int i=1; i < record_list.size(); i++){
            assertEquals(0, msg.get_records().get(i).compareTo(test_msg.get_records().get(i)));
        }
    }
    
    @Test
    public void test_msg_send() throws IOException, JAXBException, InterruptedException {
        from = new TestThread(true, 5554, 15);
        to = new TestThread(false, 5555);
        from.start();
        to.start();
        from.join();
        to.join();
        
        assertEquals(from.get_answer(), to.get_answer());
    }

}
