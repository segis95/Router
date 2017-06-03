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
	static Map<String, String> rout_table;
	
	
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
		rout_table = new HashMap<String, String>();
	}
	
	 static void Beeper() throws InterruptedException, IOException{
		ByteBuffer bbuf = ByteBuffer.allocate(1200);
		while(true){
			Thread.sleep(100);
			bbuf.put("B#".getBytes("UTF-16be"));
			bbuf.flip();
			//System.out.println(StandardCharsets.UTF_16BE.decode(bbuf.duplicate()).toString());
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
				//System.out.println(str);
				//System.out.println("Recieve this");
				//System.out.println(str);
				//reception.put(str);
				bbuf.clear();
				//System.out.println(client_from.toString());
				
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
			
			if (msg.length() > 0 && msg.charAt(0) == 'B') {
				
				//System.out.println("B");
				String H = "H#";
				//H = H + msg.substring(2);
				String tmp = add.toString();
				
				int p;
				p = tmp.indexOf(":");
				tmp = tmp.substring(1, p);
				//System.out.println(tmp);
				H = H + tmp + "#";
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(H.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				//System.out.println(StandardCharsets.UTF_16BE.decode(to_send.duplicate()).toString());
				mySocket.send(to_send, add);
				
	
			}
	
			if (msg.length() > 0 && msg.charAt(0) == 'H') {
				
				//System.out.println("H");
				//System.out.println(msg);
				int p, q;
				p = msg.indexOf('#');
				//System.out.println(p);
				q = msg.indexOf('#', p + 1);
				
				
				String my_add = msg.substring(p + 1, q);
				//System.out.println(my_add);
				my_names.add(my_add);
				
				if (!g.containsKey("zero")){
					g.put("zero", new HashMap<String, Integer>());
				}
				g.get("zero").put(my_add, 0);
				
				//System.out.println(g.keySet().toString());
				if (!g.containsKey(my_add)){
					g.put(my_add, new HashMap<String, Integer>());
				}
				g.get(my_add).put("zero", 0);
				
				String tmp = add.toString();
				p = tmp.indexOf(":");
				tmp = tmp.substring(1, p);
				
				//System.out.println(tmp);

				
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
	
				//System.out.println("T");
				int p, q, r;
				p = msg.indexOf('#');
				q = msg.indexOf('#', p + 1);
				r = msg.indexOf('#', q + 1);
				String from = msg.substring(p + 1, q);
				String to = msg.substring(q + 1, r);
				
				//System.out.println(from);
				//System.out.println(to);
				if (my_names.contains(from)||my_names.contains(to))
					continue;
				
				if (g.containsKey(from) && g.get(from).containsKey(to))
					continue;
				

				
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
				//System.out.println("S");
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
					continue;
				}
				
				String next_hop = rout_table.get(to_add);//= next(to_add);
				
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
		
		String nextnext = ""; 
		String curr = to;
		String next = "";
		while(curr != "zero")
		{	
			//System.out.println(curr);
			nextnext = next;
			next = curr;
			curr = p.get(curr);
			
		}
		//System.out.println(s);
		return next;
		
	}
	
	public static void roots_build(){
		String st;
		for (String s: g.keySet()){
			 
			if(!my_names.contains(s) && !s.equals("zero")){
				st = next(s);
				rout_table.put(s, st);
			}
			
		}
		
		
	}
		//***********************************************************************
		//************************************************************************
	 
	public static void graph_control() throws InterruptedException{
		
		while(true){
			Thread.sleep(3000);
			lock.lock();
			g.clear();
			p.clear();
			q.clear();
			d.clear();
			rout_table.clear();
			Thread.sleep(200);
			Dijkstra("zero");
			roots_build();
			lock.unlock();
			System.out.println(rout_table.toString());
		}

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