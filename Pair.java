
import java.util.Comparator;

public class Pair {
	
	//This class is auxiliary for Dijkstra algorithm
	//Pair is the name of a point with known distance to it
	String id;
	int dist;
    
	Pair(int dist1, String id1)
	{
		id = id1;
		dist = dist1;
	}

	//used to order the points to priority queue in the needed way
	static Comparator<Pair> comparator = new Comparator<Pair>() {
		@Override
		public int compare(Pair o1, Pair o2) {
			if( o1.dist < o2.dist ){
				return -1;
			}
			if( o1.dist > o2.dist ){
				return 1;
			}
			return 0;
		}
	};
	
}