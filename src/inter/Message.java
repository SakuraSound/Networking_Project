package inter;


//TODO: maybe convert this to an abstract class instead....
public interface Message extends XMLable{
    public abstract String get_timestamp();
    public abstract int get_priority();
}
