import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class Message {

	InetSocketAddress to;
	ByteBuffer bbuf;
	
	Message(InetSocketAddress t, ByteBuffer b){
		
		to = t; 
		bbuf = b;
	}

}
