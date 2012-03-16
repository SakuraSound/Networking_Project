package inter;

import javax.xml.bind.JAXBException;

/**
 * Make sure to implement public static ? implements XMLable from_bytes
 * unless i change my mind to make it an abstract class... (considering)
 * @author Hatomi
 *
 */
public interface XMLable {
    public abstract byte[] to_bytes() throws JAXBException;
}
