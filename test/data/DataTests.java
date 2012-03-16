package data;

import static data.DataUtilities.bytes_2_java;
import static data.DataUtilities.java_2_bytes;
import static data.DataUtilities.valid_ip;
import static data.DataUtilities.valid_name;
import static data.DataUtilities.valid_port;
import static data.DataUtilities.valid_wild_ip;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class DataTests extends TestCase{

	Record test_record;
	@Before
	public void setUp() throws Exception {
		test_record = Record.create_record("Test record num 1!", "125.55.212.16", 4033);
	}

	@Test
	public void test_good_ip() {
		String good_ip = "126.3.55.126";
		assertTrue(valid_ip(good_ip));
	}
	
	@Test
	public void test_bad_ip(){
		String bad_ip = "Lol look at me... i iz trying to b ip!!";
		assertFalse(valid_ip(bad_ip));
	}

	@Test
	public void test_wild_ip(){
	    String wild = "1**.*.*.1";
	    assertTrue(valid_wild_ip(wild));
	}
	
	@Test
	public void test_good_port(){
		assertTrue(valid_port(4033));
	}
	
	@Test
	public void test_bad_port(){
		assertFalse( valid_port(1023) || valid_port(65536) );
	}
	
	@Test 
	public void test_good_name(){
		assertTrue(valid_name("I believe in miracles."));
	}
	@Test public void test_bad_name(){
		StringBuilder buil = new StringBuilder("");
		for(int i = 1; i < 100; i++) buil.append("An");
		assertFalse(valid_name(buil.toString()));
	}
	
	@Test 
	public void test_marshall() throws JAXBException{
		String xml = new String(java_2_bytes(test_record, Record.class));
		Record result = (Record) bytes_2_java(xml.getBytes(), Record.class);
		assertEquals(0, result.compareTo(test_record));
	}
	
	
}
