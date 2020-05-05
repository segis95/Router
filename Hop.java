import java.io.IOException;
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
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;

public class Hop {
    final static int bpm = 10;
    final static int time_of_execution = 20000;
	static Map<String, InetSocketAddress> neighbours;
	static HashMap<String, HashMap<String, Integer>> g;
	static HashSet<String> my_names;
	static ReentrantLock lock;
    static boolean flag;
	static BlockingQueue<Message> reception;
	static Map<String, String> rout_table;
	static HashMap<DatagramChannel, InetSocketAddress> to_addresses;
	static Set<DatagramChannel> interfaces;// normal
	static HashMap<DatagramChannel, DatagramChannel> recievers;

	Hop() throws IOException {

        flag = true;
		recievers = new HashMap<DatagramChannel, DatagramChannel>();
		to_addresses = Netint.broadcasts();
		interfaces = to_addresses.keySet();
		for (DatagramChannel ch : interfaces) {
			InetSocketAddress brd = to_addresses.get(ch);
			DatagramChannel rec = DatagramChannel.open();
			rec.socket().setBroadcast(true);
			rec.socket().setSoTimeout(10);
			rec.bind(brd);
			recievers.put(rec, ch);
		}

		neighbours = new HashMap<String, InetSocketAddress>();
		g = new HashMap<String, HashMap<String, Integer>>();
		my_names = new HashSet<String>();
		lock = new ReentrantLock();
		reception = new LinkedBlockingQueue<Message>(100000);
		rout_table = new HashMap<String, String>();

		for (DatagramChannel c : interfaces) {
			String s = c.socket().getLocalAddress().toString().substring(1);
			my_names.add(s);
		}

	}
    // SOwn names transmitter
	static void Beep_my_names() throws IOException, InterruptedException {
		while (flag) {
			Thread.sleep(bpm);
			for (String s1 : my_names)
				for (String s2 : my_names)
					if (!s1.equals(s2)) {
						for (DatagramChannel c : recievers.keySet()) {
							ByteBuffer to_send = ByteBuffer.allocate(1500);
							String newmsg = "T#" + s1 + "#" + s2 + "#";
							to_send.put(newmsg.getBytes("UTF-16BE"));
							to_send.limit(to_send.position());
							to_send.position(0);
							to_send.clear();
							recievers.get(c).send(to_send, new InetSocketAddress(c.socket().getLocalAddress(), 30009));
						}
					}
		}
        
        System.out.println("Names Beeper Stopped Running....");

	}
    // Beeper
	static void Beeper() throws InterruptedException, IOException {
		ByteBuffer bbuf = ByteBuffer.allocate(1200);
		while (flag) {
			Thread.sleep(bpm);
			for (DatagramChannel ch : interfaces) {
				bbuf.put("B#".getBytes("UTF-16be"));
				bbuf.flip();
				ch.send(bbuf.duplicate(), to_addresses.get(ch));
				bbuf.clear();
			}
		}
        System.out.println("Beeper Stopped Running....");
	}
    
    // Recieves and stocks messages in a queue
	static void reciever() throws IOException, InterruptedException {

		ByteBuffer bbuf = ByteBuffer.allocate(1200);
		String str;
		while (flag) {
			for (DatagramChannel ch : recievers.keySet()) {
				InetSocketAddress client_from = (InetSocketAddress) ch.receive(bbuf);
				bbuf.flip();
				str = StandardCharsets.UTF_16BE.decode(bbuf.duplicate()).toString();
				bbuf.clear();
				reception.put(new Message(client_from, str, ch));
			}
		}
        System.out.println("Reciever Stopped Running....");
	}
    
    // Parses and analyses messages; creates appropriate responses
	static void msg_analiser() throws InterruptedException, IOException {

		while (flag) {
			while (reception.isEmpty()) {
				Thread.sleep(1);
			}
			Message m = reception.take();

			SocketAddress address = m.from;
			String msg = m.msg;
			int i = address.toString().indexOf(':');
			String add = address.toString().substring(1, i);

			if (msg.length() > 0 && msg.charAt(0) == 'B') {

				if (my_names.contains(add))
					continue;
				String H = "H#";
				H = H + add + "#";
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(H.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				recievers.get(m.ch).send(to_send, new InetSocketAddress(m.ch.socket().getLocalAddress(), 30009));
			}

			if (msg.length() > 0 && msg.charAt(0) == 'H') {
				int p, q;
				p = msg.indexOf('#');
				q = msg.indexOf('#', p + 1);
				String my_add = msg.substring(p + 1, q);
				if (!g.containsKey("zero")) {
					g.put("zero", new HashMap<String, Integer>());
				}

				if (!my_names.contains(my_add))
					continue;

				g.get("zero").put(my_add, 0);

				if (!g.containsKey(my_add)) {
					g.put(my_add, new HashMap<String, Integer>());
				}
				g.get(my_add).put("zero", 0);

				if (add.equals(my_add))
					continue;
				g.get(my_add).put(add, 1);
				if (!g.containsKey(add)) {
					g.put(add, new HashMap<String, Integer>());
				}

				g.get(add).put(my_add, 1);
				String T = "T#";
				T = T + my_add + "#" + add + '#';
                
				for (DatagramChannel c : recievers.keySet()) {
					ByteBuffer to_send = ByteBuffer.allocate(1500);
					to_send.put(T.getBytes("UTF-16BE"));
					to_send.limit(to_send.position());
					to_send.position(0);
					to_send.clear();
					recievers.get(c).send(to_send, new InetSocketAddress(c.socket().getLocalAddress(), 30009));
				}
			}

			if (msg.length() > 0 && (msg.charAt(0) == 'T')) {

				int p, q, r;
				p = msg.indexOf('#');
				q = msg.indexOf('#', p + 1);
				r = msg.indexOf('#', q + 1);
				String from = msg.substring(p + 1, q);
				String to = msg.substring(q + 1, r);

				if (my_names.contains(add))
					continue;

				if (my_names.contains(from) || my_names.contains(to))
					continue;

				if (!g.containsKey(from)) {
					g.put(from, new HashMap<String, Integer>());
				}

				g.get(from).put(to, 1);

				if (!g.containsKey(to)) {
					g.put(to, new HashMap<String, Integer>());
				}
				g.get(to).put(from, 1);

				for (DatagramChannel c : recievers.keySet()) {
					ByteBuffer to_send = ByteBuffer.allocate(1500);
					to_send.put(msg.getBytes("UTF-16BE"));
					to_send.limit(to_send.position());
					to_send.position(0);
					to_send.clear();
					recievers.get(c).send(to_send, new InetSocketAddress(c.socket().getLocalAddress(), 30009));
				}
			}

			if (msg.length() > 0 && msg.charAt(0) == 'S') {
				while (lock.isLocked())
					Thread.sleep(10);

				int p, q, r;
				p = msg.indexOf('#');
				q = msg.indexOf(p + 1, '#');
				r = msg.indexOf(q + 1, '#');

				String text_msg = msg.substring(q + 1, r);
				String to_add = msg.substring(p + 1, q);

				if (my_names.contains(to_add)) {
					System.out.println("Recieved a message:" + text_msg);
					continue;
				}

				String next_hop = rout_table.get(to_add);
				ByteBuffer to_send = ByteBuffer.allocate(1500);
				to_send.put(msg.getBytes("UTF-16BE"));
				to_send.limit(to_send.position());
				to_send.position(0);
				m.ch.send(to_send, to_addresses.get(m.ch));
			}
		}
        
        System.out.println("Message Analyser Stopped Running....");

	}
	final static int INF = 200000;
	static Map<String, Integer> d = new HashMap<String, Integer>();
	static Map<String, String> p = new HashMap<String, String>();
	static Queue<Pair> q = new PriorityQueue<Pair>(Pair.comparator);

	static void Dijkstra(String s) {
		for (String x : g.keySet()) {
			d.put(x, INF);
		}
		HashMap<String, Integer> list_incid;
		d.put(s, 0);
		q.add(new Pair(d.get(s), s));
        
		int len;
		int curr;
		String v;

		while (!q.isEmpty()) {
			v = q.peek().id;
			curr = q.peek().dist;
			q.remove();

			if (curr > d.get(v))
				continue;

			list_incid = g.get(v);

			try {
				for (String to : list_incid.keySet()){
					len = list_incid.get(to);
					if (d.get(v) + len < d.get(to)) {
						d.put(to, d.get(v) + len);
						p.put(to, v);
						q.offer(new Pair(d.get(to), to));
					}
				}
			} catch (Throwable t) {
			}
		}
	}

	public static String next(String to) {

        if ((long) d.get(to) == INF) {
			System.out.println(to + "This point is not reachable from s");
			return "";
		}

		String nextnext = "";
		String curr = to;
		String next = "";
		while (curr != "zero") {
			nextnext = next;
			next = curr;
			curr = p.get(curr);

		}
		return nextnext;

	}

	public static void roots_build() {
		String st;
		for (String s : g.keySet()) {
			if (!my_names.contains(s) && !s.equals("zero")) {
				st = next(s);
				rout_table.put(s, st);
			}
		}
	}
    // builds routes from "zero" to all nodes
	public static void graph_control() throws InterruptedException, IOException {

			Thread.sleep(time_of_execution);
			Dijkstra("zero");
			roots_build();
			Runtime rt = Runtime.getRuntime();
			Process pr;// = rt.exec("ip route flush table main");
            //pr = rt.exec("ip route flush cache");
			for (String from : rout_table.keySet()) {
				pr = rt.exec("ip route add " + from + " via " + Hop.rout_table.get(from));//.substring(0, from.lastIndexOf('.') + 1) 0/24
			}
			System.out.println(rout_table.toString());
            flag = false;
        System.out.println("Graph Control Stopped Running....");

	}

	public static void main(String args[]) throws InterruptedException, IOException {
        try{
            Hop h = new Hop();

            Thread t1 = new Thread(() -> {
                try {
                    Beeper();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {}
            });
            t1.start();

            Thread t2 = new Thread(() -> {
                try {
                    reciever();
                } catch (IOException | InterruptedException e) {}
            });

            t2.start();

            Thread t3 = new Thread(() -> {
                try {
                    msg_analiser();
                } catch (InterruptedException | IOException e) {}
            });

            t3.start();

            Thread t4 = new Thread(() -> {
                try {
                    graph_control();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {}
            });

            t4.start();

            Thread t5 = new Thread(() -> {
                try {
                    Beep_my_names();
                } catch (IOException | InterruptedException e) {}
            });

            t5.start();
        }catch(Exception e){
            flag = false;
            System.out.println(e);
        }
	}
}
