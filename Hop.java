import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Hop {

	static String name;
	static DatagramChannel mySocket;
	static InetSocketAddress local;
	static InetSocketAddress to_socket;
	static Map<String, InetSocketAddress> neighbours;
	static HashMap<String, HashMap<String, Integer>> g;
	static BlockingQueue<Message> to_send;
	
	Hop(String n) throws IOException{
		
		local = new InetSocketAddress(30009);
		mySocket = DatagramChannel.open();
		mySocket.bind(local);
		mySocket.socket().setBroadcast(true);
		to_socket = new InetSocketAddress("255.255.255.255",30009);
		name = n;
		neighbours = new HashMap<String, InetSocketAddress>();
		g = new HashMap<String, HashMap<String, Integer>>();
		to_send = new LinkedBlockingQueue<Message>();
	}
	
	 static void Beeper() throws InterruptedException, IOException{
		ByteBuffer bbuf = ByteBuffer.allocate(1200);
		while(true){
			Thread.sleep(100);
			bbuf.put(local.toString().getBytes("UTF-16be"));
			bbuf.flip();
			mySocket.send(bbuf.duplicate(), to_socket);
			bbuf.clear();
		}
	 }
		
		static void reciever() throws IOException{
			
			ByteBuffer bbuf = ByteBuffer.allocate(1200);
			
			String str;
		
			while(true){
				
				SocketAddress client_from = mySocket.receive(bbuf);
				bbuf.flip();
				str = StandardCharsets.UTF_16BE.decode(bbuf.duplicate()).toString();

				//System.out.println("Recieve this");
				//System.out.println(str);
				//reception.put(str);
				bbuf.clear();
				System.out.println(client_from.toString());
				
				msg_analiser(client_from, str);
			}
			
			
			
		}
		
		
	public static void sender() throws IOException, InterruptedException{
		
		while(true){
			
			while(to_send.isEmpty())
				Thread.sleep(10);
			Message msg = to_send.take();
			//msg.bbuf.flip();
			
			String str = StandardCharsets.UTF_16BE.decode(msg.bbuf.duplicate()).toString();
			System.out.println("send this");
			System.out.println(str);
			
			mySocket.send(msg.bbuf, msg.to);
			
		}
		
	}
	
	static void msg_analiser(SocketAddress add, String msg)
			throws InterruptedException, UnsupportedEncodingException {

		// String msg;

		// msg = reception.take();

		if (msg.length() > 2 && msg.charAt(0) == 'B') {
			String H = "H#";
			H = H + msg.substring(2);
			H = H + add.toString();
			ByteBuffer to_send = ByteBuffer.allocate(1500);
			to_send.put(H.getBytes("UTF-16BE"));
			to_send.limit(to_send.position());
			to_send.position(0);
			mySocket.send(to_send, add);

		}

		if (msg.length() > 0 && msg.charAt(0) == 'H') {

			String T = "T#";
			T = T + msg.substring(2);
			T = T + add.toString();
			ByteBuffer to_send = ByteBuffer.allocate(1500);
			to_send.put(T.getBytes("UTF-16BE"));
			to_send.limit(to_send.position());
			to_send.position(0);
			mySocket.send(to_send, "255.255.255.255");
		}

		if (msg.length() > 0 && msg.charAt(0) == 'T') {

			this.update_graph(msg.substring(2));
			ByteBuffer to_send = ByteBuffer.allocate(1500);
			to_send.put(msg.getBytes("UTF-16BE"));
			to_send.limit(to_send.position());
			to_send.position(0);
			mySocket.send(to_send, "255.255.255.255");
			// connection_from(msg.substring(2, i));
		}

		if (msg.length() > 0 && msg.charAt(0) == 'S') {
			String receiver = msg.substring(2).split("::")[0];
			if (this.name.equals(receiver)) {
				System.out.println(msg.substring(2));
			} else {
				this.sendto(receiver);
			}

		}

	}; 
	 
	 
	
	public static void main(String args[]) throws InterruptedException, IOException{
		
		Hop h = new Hop("ser");
		
		Thread t1 = new Thread(() -> {
			try {
				Beeper();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		t1.start();
		
		Thread t2 = new Thread(() -> {
			try {
				reciever();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		
	
		t2.start();
	}
}
	

