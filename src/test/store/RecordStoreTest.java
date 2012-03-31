package test.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import inter.Message;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import javax.xml.bind.JAXBException;

import net.SpecialSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import server.job.Job;
import server.job.KillJob;
import server.job.SearchJob;
import server.job.WriteJob;
import server.store.RecordStore;
import utils.CommUtilities;

import comm.resp.GenericResponse;
import comm.resp.ReadListMessage;

import data.SearchQuery;
import data.record.InvalidRecordException;
import data.record.Record;

public class RecordStoreTest {

    RecordStore temp_store;
    TestThread thread;
    
    @Before
    public void setUp() throws Exception {
       temp_store =  RecordStore.create_temp_database();
       thread = new TestThread(GenericResponse.class);
       temp_store.start();
    }

    @After
    public void tearDown() throws Exception {
        

    }

    @Test
    public void test_write() throws InvalidRecordException, JAXBException, InterruptedException {
        thread.start();
        System.out.println(thread.getName());
        Record record = Record.create_record("Test Document one", "127.0.0.1", 5777);
        System.out.println(thread.get_port());
        System.out.println(thread.get_inet());
        temp_store.add_job(WriteJob.spawn(Job.WRITE, record, 2, thread.get_inet(), thread.get_port()));
        thread.join();
        System.out.println(thread.get_class());
        System.out.println(new String(thread.get_msg().to_bytes()));
        assertTrue(thread.get_class() == GenericResponse.class);
        temp_store.add_job(KillJob.spawn(10, thread.get_inet(), thread.get_port()));
        temp_store.join();
    }
    
    @Test
    public void test_read() throws InvalidRecordException, SocketException, InterruptedException, JAXBException{
        thread.start();
        Record record = Record.create_record("Test Document one", "127.0.0.1", 5777);
        temp_store.add_job(WriteJob.spawn(Job.WRITE, record, 2, thread.get_inet(), thread.get_port()));
        thread.join();
        TestThread thread2 = new TestThread(ReadListMessage.class);
        thread2.start();
        SearchQuery query = SearchQuery.make_retrieve_query("Test Docu*", "12*.*.0.*");
        temp_store.add_job(SearchJob.spawn(Job.READ, query, 6, thread2.get_inet(), thread2.get_port()));
        thread2.join();
        ReadListMessage<Record> msg = (ReadListMessage<Record>) thread2.get_msg();
        System.out.println(new String(msg.to_bytes()));
        assertTrue(thread2.success() && msg.get_records().size() == 1);
        temp_store.add_job(KillJob.spawn(10, thread2.get_inet(), thread2.get_port()));
    }
    
//    @Test
//    public void test_delete() throws InvalidRecordException, SocketException, InterruptedException, JAXBException{
//        thread.start();
//        Record record = Record.create_record("Test Document one.", "127.0.0.1", 5777);
//        temp_store.add_job(WriteJob.spawn(Job.WRITE, record, 2, thread.get_inet(), thread.get_port()));
//        thread.join();
//        TestThread thread2 = new TestThread(GenericResponse.class);
//        thread2.start();
//        SearchQuery query = SearchQuery.make_delete_query(record.get_name(), record.get_ip(), record.get_port());
//        temp_store.add_job(SearchJob.spawn(Job.DELETE, query, 6, thread2.get_inet(), thread2.get_port()));
//        thread2.join();
//        GenericResponse msg = (GenericResponse) thread2.get_msg();
//        System.out.println(new String(msg.to_bytes()));
//        assertTrue(thread2.success());
//        temp_store.add_job(KillJob.spawn(10, thread2.get_inet(), thread2.get_port()));
//    }
    
    @Test
    public void test_persistence() throws IOException, InvalidRecordException, InterruptedException{
        thread.start();
        temp_store.add_job(KillJob.spawn(10, thread.get_inet(), thread.get_port()));
        RecordStore persist_store = RecordStore.create_new_database("real_db");
        persist_store.start();
        Record n_rec = Record.create_record("I want to live!!!", "100.65.3.255", thread.get_port());
        persist_store.add_job(WriteJob.spawn(Job.WRITE, n_rec, 5, thread.get_inet(), thread.get_port()));
        thread.join();
        persist_store.add_job(KillJob.spawn(10, thread.get_inet(), thread.get_port()));
        persist_store.join();
        persist_store = RecordStore.create_new_database("real_db");
        assertEquals(1, persist_store.get_record_size());
        File file = new File("data/real_db.dat");
        file.delete();
    }
    
    private class TestThread extends Thread{
        protected SpecialSocket socket;
        protected Class<?> expected;
        protected Class<?> clazz;
        protected Message msg;
        
        public InetAddress get_inet(){ return socket.get_inet(); }
        public int get_port(){ return socket.get_port(); }
        
        public Class<?> get_class(){ return clazz; }
        public Message get_msg(){ return msg; }
        
        public boolean success(){
            return expected == clazz;
        }
        
        public void run(){
            
            try {
                msg = (Message) expected.cast(CommUtilities.bytes_2_java(socket.accept().getData(), expected));
                clazz = msg.getClass();
            } catch (JAXBException e) {
                fail(e.getStackTrace().toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally{
                socket.close();
            }
            
        }
        
        public TestThread(Class<?> clazz) throws SocketException{
            this.expected = clazz;
            socket = SpecialSocket.create_socket(5777);
            System.out.println(socket.get_inet());
        }
    }

}
