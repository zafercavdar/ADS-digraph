import java.util.Comparator;

public class Edge implements Comparable {
	public static final Comparator<Edge> DEF = Comparator.naturalOrder();
	public final int v, w; // Vertices of this undirected edge
	
	// Constructor for an edge (no self-loops allowed)
	public Edge(int v, int w) {
		if (v < 0 || w < 0 || v == w) {
			throw new IllegalArgumentException("Vertices of an edge must be different nonnegative integers, v = " + v + ", w = " + w);
		} else {
			this.v = v;
			this.w = w;
		}
	}
	
	@Override
	// Returns true iff the given object describes this edge
	public boolean equals(Object o) {
		if (!(o instanceof Edge)) return false;
		Edge e = (Edge) o;
		return v == e.v && w == e.w;
	}

	@Override
	// Compares the given object against this object according to lexicographic order
	public int compareTo(Object o) {
		if (!(o instanceof Edge)) return 1;
		Edge e = (Edge) o;
		return (e.v == v) ? ((Integer)w).compareTo(e.w) : ((Integer)v).compareTo(e.v);
	}
	
	// Returns true iff this edge can be an edge in a graph with V vertices
	public boolean valid(int V) {
		return (v < V && w < V);
	}
	
	// Returns a string representing this edge
	public String toString() {
		return "(" + v + "," + w + ")";
	}
}