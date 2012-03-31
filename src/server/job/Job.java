package server.job;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum(String.class)
public enum Job {
	@XmlEnumValue("READ")  READ,
	@XmlEnumValue("WRITE")  WRITE,
	@XmlEnumValue("DELETE")  DELETE,
	@XmlEnumValue("REGISTER")  REGISTER,
	@XmlEnumValue("UNREGISTER")  UNREGISTER,
	@XmlEnumValue("CLIENT_SEARCH")  CLIENT_SEARCH,
	@XmlEnumValue("LINK")  LINK,
	@XmlEnumValue("UNLINK")  UNLINK,
	@XmlEnumValue("SHUT_DOWN")  SHUT_DOWN,
	@XmlEnumValue("SEND_MESSAGE")  SEND_MESSAGE,
	@XmlEnumValue("UPDATE_REGISTER") UPDATE_REGISTER,
	@XmlEnumValue("UPDATE_UNREGISTER") UPDATE_UNREGISTER;
}
