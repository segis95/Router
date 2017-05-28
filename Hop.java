import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;


public class Hop {

	static DatagramChannel mySocket;
	static InetSocketAddress local;
	static InetSocketAddress to_socket;
	static Map<String, InetSocketAddress> neighbours;
	static HashMap<String, HashMap<String, Integer>> g;
	static HashSet<String> my_names;
	static ReentrantLock lock;
	static BlockingQueue<Message> reception;
	
	
	Hop() throws IOException{
		
		local = new InetSocketAddress(30009);
		mySocket = DatagramChannel.open();
		mySocket.bind(local);
		mySocket.socket().setBroadcast(true);
		to_socket = new InetSocketAddress("255.255.255.255",30009);
		neighbours = new HashMap<String, InetSocketAddress>();
		g = new HashMap<String, HashMap<String, Integer>>();
		my_names = new HashSet<String>();
		lock = new ReentrantLock();
		reception = new LinkedBlockingQueue<Message>();
	}
	
	 static void Beeper() throws InterruptedException, IOException{
		ByteBuffer bbuf = ByteBuffer.allocate(1200);
		while(true){
			Thread.sleep(100);
			bbuf.put("B#".getBytes("UTF-16be"));
			bbuf.flip();
			mySocket.send(bbuf.duplicate(), to_socket);
			bbuf.clear();
		}
	 }
		
		static void reciever() throws IOException, InterruptedException{
			
			ByteBuffer bbuf = ByteBuffer.allocate(1200);
			
			String str;
		
			while(true){
				
				InetSocketAddress client_from = (InetSocketAddress) mySocket.receive(bbuf);
				bbuf.flip();
				str = StandardCharsets.UTF_16BE.decode(bbuf.duplicate()).toString();

				//System.out.println("Recieve this");
				//System.out.println(str);
				//reception.put(str);
				bbuf.clear();
				System.out.println(client_from.toString());
				
				reception.put(new Message(client_from, str));
				//msg_analiser(client_from, str);
			}
			
			
			
		}
		
		
//	public static void sender() throws IOException, InterruptedException{
//		
//		while(true){
//			
//			while(to_send.isEmpty())
//				Thread.sleep(10);
//			Message msg = to_send.take();
//			//msg.bbuf.flip();
//			
//			String str = StandardCharsets.UTF_16BE.decode(msg.bbuf.duplicate()).toString();
//			System.out.println("send this");
//			System.out.println(str);
//			
//			mySocket.send(msg.bbuf, msg.to);
//			
//		}
//		
//	}
	
	static void msg_analiser()
			throws InterruptedException, IOException {

		// String msg;

		// msg = reception.take();
		while(true){
			
			while(reception.isEmpty())
			{
				Thread.sleep(10);
			}
			Message m = reception.take();
			SocketAddress add = m.from; 
			String msg = m.msg;
			
			if (msg.length() > 2 && msg.charAt(0) == 'B') {
				String H = "H#";
				//H = H + msg.substring(2);
				String tmp = add.toString();
				int p;
				p = tmp.indexOf('/');
				tmp = tmp.substring(0, p);
				
				H = H + tmp + "#";
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(H.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				mySocket.send(to_send, add);
	
			}
	
			if (msg.length() > 0 && msg.charAt(0) == 'H') {
	
				int p, q;
				p = msg.indexOf('#');
				q = msg.indexOf(p + 1, '#');
				String my_add = msg.substring(p + 1, q);
				
				my_names.add(my_add);
				
				if (!g.containsKey("zero")){
					g.put("zero", new HashMap<String, Integer>());
				}
				g.get("zero").put(my_add, 0);
				
				if (!g.containsKey(my_add)){
					g.put(my_add, new HashMap<String, Integer>());
				}
				g.get(my_add).put("zero", 0);
				
				String tmp = add.toString();
				p = tmp.indexOf('/');
				tmp = tmp.substring(0, p);
				
				g.get(my_add).put(tmp, 0);
				if (!g.containsKey(tmp)){
					g.put(tmp, new HashMap<String, Integer>());
				}
				g.get(tmp).put("zero", 1);
				
						
						
				String T = "T#" ;
				T = T + my_add + "#" + tmp + '#';
	
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(T.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				mySocket.send(to_send, to_socket);
			}
	
			if (msg.length() > 0 && msg.charAt(0) == 'T') {
	
				int p, q, r;
				p = msg.indexOf('#');
				q = msg.indexOf(p + 1, '#');
				r = msg.indexOf(q + 1, '#');
				String from = msg.substring(p + 1, q);
				String to = msg.substring(q + 1, r);
				
				
				if (my_names.contains(from)||my_names.contains(to))
					break;
				
				if (g.containsKey(from) && g.get(from).containsKey(to))
					break;
				
				if (!g.containsKey(from)){
					g.put(from, new HashMap<String, Integer>());
				}
				g.get(from).put(to, 1);
				
				if (!g.containsKey(to)){
					g.put(to, new HashMap<String, Integer>());
				}
				g.get(to).put(from, 1);		
				
				
				//this.update_graph(msg.substring(2));
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(msg.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				mySocket.send(to_send, to_socket);
				// connection_from(msg.substring(2, i));
			}
	
			if (msg.length() > 0 && msg.charAt(0) == 'S') {
				
				while(lock.isLocked())
					Thread.sleep(10);
				
				
				int p, q, r;
				p = msg.indexOf('#');
				q = msg.indexOf(p + 1, '#');
				r = msg.indexOf(q + 1, '#');
				
				String text_msg = msg.substring(q + 1, r);
				String to_add = msg.substring(p + 1, q);
				
				if (my_names.contains(to_add)){
					System.out.println("Recieved a message:" + text_msg);
					break;
				}
				
				String next_hop = next(to_add);
				
				InetSocketAddress to_sock = new InetSocketAddress(next_hop, 30009);
				
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(msg.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				mySocket.send(to_send, to_sock);
				

	
			}
		}

	} 
	 
	//****************************************************************
	//****************************************************************
	//*******************************************************************
	
//Infinity in terms of Dijkstra
	final static int INF = 200000;
	
	//represents the shortest distances to all points
	static Map<String, Integer> d = new HashMap<String, Integer>();
	
	//represents the previous point on the way from the source to a given point 
	static Map<String, String> p = new HashMap<String, String>();
	
	
	//priority queue needed for Dijkstra with the need comparator
	static Queue<Pair> q = new PriorityQueue<Pair>(Pair.comparator);
	
	//Dijkstra algorithm implementation for source s
	static void Dijkstra(String s)
	{
		//System.out.println("Dijkstra : calculating shortest paths...*** may take up to 40 seconds");
		//we put shortest known distance to all points INF
		for (String x:g.keySet())
		{
			d.put(x, INF);
		}
		//auxiliary Map
		HashMap<String, Integer> list_incid;
		
		//We put distance to source 0
		d.put(s,0);
		
		//adding pair to queue
		q.add(new Pair(d.get(s), s));
		
		//auxiliary variables
		int len;
		int curr;
		String v;
		
		//main loop
		while (!q.isEmpty()) {
			
			//we take a point from the top of our queue
			v = q.peek().id;
			curr = q.peek().dist;
			q.remove();
			
			//if this point is already not up to date(we know a shorter path)
			//We just omit it
			if (curr > d.get(v))
				continue;
			
			//points incident with v
			list_incid = g.get(v);
			
			try{
				for (String to : list_incid.keySet()) //we check all incident points with v(call them to) 
				{
					//shortest known distance to to
					len = list_incid.get(to);
					
					//if we can make a relaxation we do it
					if (d.get(v) + len < d.get(to)) {
							d.put(to, d.get(v) + len);
							p.put(to, v);
							q.offer(new Pair(d.get(to), to));
					}
				}
			}catch(Throwable t){
				//catches cases when list_incid is empty or null
			}
		}
	}
		
		
	public static String next(String to){
		
		//System.out.println("Points on the way from s to TO");
		
		if ((long)d.get(to) == INF)
		{
			System.out.println("This point is not reachable from s");
			return "";
		}
		
		String curr = to;
		String next = "";
		while(curr != "zero")
		{	
			//System.out.println(curr);
			next = curr;
			curr = p.get(curr);
			
		}
		//System.out.println(s);
		return next;
		
	}
		//***********************************************************************
		//************************************************************************
	 
	public static void graph_control() throws InterruptedException{
		
		
		Thread.sleep(3000);
		lock.lock();
		g.clear();
		p.clear();
		q.clear();
		d.clear();
		Thread.sleep(200);
		Dijkstra("zero");
		lock.unlock();
		System.out.println(g.keySet().toString());

	}
	
	public static void main(String args[]) throws InterruptedException, IOException{
		
		Hop h = new Hop();
		
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
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		t2.start();
		
		Thread t3 = new Thread(() -> {
			try {
				msg_analiser();
			} catch (InterruptedException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		t3.start();
		
		Thread t4 = new Thread(() -> {
			try {
				graph_control();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		t4.start();

	}
}