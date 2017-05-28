import java.net.InetSocketAddress;


public class Message {

	InetSocketAddress from;
	String msg;
	
	Message(InetSocketAddress f, String m){
		
		from = f; 
		msg = m;
	}

}