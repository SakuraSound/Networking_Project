package data;

import inter.XMLable;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Record implements Comparable<Record>, XMLable{
	
	@XmlElement(name="name")
	private String name;
	@XmlElement(name="ip_addr")
	private String ip_addr;
	@XmlElement(name="port_num")
	private int port_num;
	@XmlAttribute(name="timestamp")
	private String timestamp;
	
	
	public String get_name(){ return name;}
	
	public String get_ip(){ return ip_addr;}
	
	public int get_port(){ return port_num;}
	
	public String get_timestamp(){ return timestamp; }
	
	/**
	 * Factory method for creating Records
	 * @param name The name of the record being stored...
	 * @param ip   The ip address of the creator
	 * @param port_num The port number affiliated with this record
	 * @return a new record object
	 * @throws InvalidRecordException if any of the requirements of record is violated
	 */
	public static Record create_record(final String name, final String ip, final int port_num) throws InvalidRecordException{
		if(validate(name, ip, port_num))
			return new Record(name, ip, port_num);
		else throw new InvalidRecordException();
	}
	
	/**
	 * Factory method to create a record object representing an existing record
	 * @param name The name of the record
	 * @param ip The ip address of the creator
	 * @param port_num The port number of the creator
	 * @param timestamp Datetime representing the original creation of this record
	 * @return new Record object representing existing record in database
	 * @throws InvalidRecordException if invalid parameters...
	 */
	public static Record recreate_record(final String name, final String ip, final int port_num, final String timestamp)
	        throws InvalidRecordException{
	    if(validate(name, ip, port_num) && timestamp != null)
	        return new Record(name, ip, port_num, timestamp);
	    else throw new InvalidRecordException();
	}
	
	private static final boolean validate(final String name, final String ip, final int port_num){
		return DataUtilities.valid_ip(ip) &&
			   DataUtilities.valid_name(name ) &&
			   DataUtilities.valid_port(port_num);
	}
	
	private Record(String name, String ip, int port_num){
		this(name, ip, port_num, DataUtilities.get_timestamp());
	}
	
	private Record(String name, String ip, int port_num, String timestamp){
	    this.name = name;
        this.ip_addr = ip;
        this.port_num = port_num;
        this.timestamp = timestamp;
	}
	

	private Record(){}

	public int compareTo(Record record) {
		int compare_val = this.name.compareTo(record.get_name());
		
		if(compare_val != 0) 
			return compare_val;

		compare_val = this.ip_addr.compareTo(record.get_ip());
		return (compare_val != 0)? compare_val : 
		       (record.get_port() != this.get_port())? record.get_port() - this.port_num: 
		                                               this.timestamp.compareTo(record.get_timestamp());
	}
	
	public boolean is_equivalent(Record record){
	    return this.name.equals(record.get_name()) && this.ip_addr.equals(record.get_ip());
	}

    public byte[] to_bytes() throws JAXBException {
        return DataUtilities.java_2_bytes(this, Record.class);
    }
    
    public static Record from_bytes(byte[] bytes) throws JAXBException{
        return (Record) DataUtilities.bytes_2_java(bytes, Record.class);
    }
}
