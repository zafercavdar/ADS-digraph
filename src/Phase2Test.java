import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class Phase2Test {
	String border = "*******************************************\n";
	String passed = "* Passed!                                 *\n";
	String failed = "* Failed!                                 *\n";
	String test;
	
	AssertionError ae;
	Exception e;
	
	int p, seed, prob;
	Digraph gr, lastgr, comp;
	boolean[] marked;
	int[] id;
	int count;
	MyStack<Integer> revPost;
	List<Integer> ret, topo;
	
	public Phase2Test () {
	}

	int V, lastV, w, lastw, d, lastd;
	LinkedList<LinkedList<Edge>> cycles, lastcyc;
	List<Edge> remEdges, mins;
	Edge rem;
	LinkedList<Edge> g, lastg;
	List<Integer> dlist, lastdlist;
	String str, laststr;
	
	public static int randomPerm(int i, int V, int v) {
		int[] mult = {1, 277, 281, 283};
		return (v * mult[i % 4] + i) % V;
	}
	
	private int r(int v) {
		return randomPerm(p, V, v);
	}

	// generates a random set of edges of the form (v, w), v0 <= v < v1, w0 <= w < w1 
	// where each edge is independently included in the set with prob/128 probability
	public LinkedList<Edge> randomEdges(int seed, int v0, int v1, int prob, boolean topo) {
		LinkedList<Edge> edges = new LinkedList<Edge>();
		Random rand = new Random(seed);
		
		for (int v = v0; v < v1; v++) {
			for (int w = (topo ? v + 1 : v0); w < v1; w++) {
				if (v == w) continue;
				int pick = rand.nextInt() & ((1 << 7) - 1);
				if (prob > pick) {
					edges.add(new Edge(r(v), r(w)));
				}
			}
		}
		
		return edges;
	}
	
	private String graphToString(int V, LinkedList<Edge> edges) {
		LinkedList<Edge> sorted = new LinkedList<Edge>(edges);
		sorted.sort(Edge.DEF);
		StringBuilder s = new StringBuilder();
		for (Edge e : sorted)
			if (e.valid(V)) s.append(e);
		return "V = " + V + ", edges = " + ((s.length() == 0) ? "-" : s.toString());
	}

	private String dlistToString(List<Integer> dlist) {
		if (dlist == null) return "null";
		StringBuilder s = new StringBuilder();
		for (int d : dlist) {
			s.append(d + "-");
		}
		return s.toString();
	}

	private String cyclesToString(List<LinkedList<Edge>> cycles) {
		if (cycles == null) return "null";
		StringBuilder s = new StringBuilder();
		for (List<Edge> c : cycles) {
			StringBuilder sc = new StringBuilder();
			for (Edge e : c) sc.append(e);
			s.append(sc.toString() + '\n');
		}
		return s.toString();
	}
	
	private void assertIsTopoDAG(Edge e) {
		if (e != null) g.add(e);   
		assertTrue(gr.isTopological(topo));
		if (e != null) {
			LinkedList<Integer> newtopo = new LinkedList<Integer>();
			for (int v : topo) {
				if (v == e.v) newtopo.add(e.w);
				else if (v == e.w) newtopo.add(e.v);
				else newtopo.add(v);
			}
			topo = newtopo;
			assertFalse(gr.isTopological(newtopo));
		}
	}
	
	private Digraph createGraph(int V, List<Edge> edges) {
		Digraph graph = new Digraph(V);
		for (Edge e : edges) {
			graph.addEdge(e.v, e.w);
		}
		return graph;
	}
	
	private void testIsTopo() {
		int i;
		for (V = 1; V <= 32; V++) {
			for (seed = 0; seed < 8; seed++) {
				for (p = 0; p < 4; p++) {
					for (prob = 1; prob <= 128; prob*=2) {
						topo = new LinkedList<Integer>();
						g = this.randomEdges(seed, 0, V, prob, true);
						gr = createGraph(V, g);
						for (i = 0; i < V; i++) topo.add(r(i));
						assertIsTopoDAG(g.poll());
						lastg = g;
						
						topo = new LinkedList<Integer>();
						g = this.randomEdges(seed, 0, V, prob, false);
						gr = createGraph(V, g);
						SCC();
						i = 0;
						for (int v : revPost) topo.add(v);
						if (count == V) {
							assertIsTopoDAG(g.poll());
						} else {
							assertFalse(gr.isTopological(topo));
						}
						lastg = g;
					}
				}
			}
		}
	}
	
	private void assertTopological(LinkedList<Edge> edges) {
		assertNotEquals(null, ret);
		marked = new boolean[V];
		for (int v : ret) {
			for (int w = 0; w < V; w++) {
				if (marked[w]) assertFalse(edges.contains(new Edge(v, w)));
			}
			marked[v] = true;
		}
		for (int v = 0; v < V; v++) {
			assertTrue(marked[v]);
		}
	}
	
	private void testTopo() {
		for (V = 1; V <= 32; V++) {
			for (seed = 0; seed < 8; seed++) {
				for (p = 0; p < 4; p++) {
					for (prob = 1; prob <= 128; prob*=2) {
						g = this.randomEdges(seed, 0, V, prob, true);
						gr = createGraph(V, g);
						ret = gr.topologicalSort();
						assertTopological(g);
						lastg = g;
						
						g = this.randomEdges(seed, 0, V, prob, false);
						gr = createGraph(V, g);
						ret = gr.topologicalSort();
						SCC();
						if (count == V) {
							assertTopological(g);
						} else {
							assertEquals(null, ret);
						}
						lastg = g;
					}
				}
			}
		}
	}

	private void testDistance() {
		for (V = 1; V <= 20; V++) {
			for (seed = 0; seed < 8; seed++) {
				for (prob = 21 - V; prob <= 128; prob*=2) {
					g = this.randomEdges(seed, 0, V, prob, false);
					Digraph gr = new Digraph(V);
					for (Edge e : g) {
						gr.addEdge(e.v, e.w);
					}
					for (w = 0; w < V; w++) {
						boolean dst[] = new boolean[V];
						dst[w] = true;
						for (Edge e : g) if (e.v == w) dst[e.w] = true;
						for (d = 2; d < V; d++) {
							boolean ndst[] = new boolean[V];
							for (Edge e : g) if (dst[e.v]) ndst[e.w] = true;
							for (int d2 = 0; d2 < V; d2++) dst[d2] = dst[d2] || ndst[d2];
							dlist = gr.verticesWithinDistance(w, d);
							assertNotEquals(null, dlist);
							boolean nd[] = new boolean[V];
							for (int d2 : dlist) { // check if all returned are within distance 2
								nd[d2] = true;
								if (!dst[d2]) fail("Vertex " + d2 + " is not within distance " + d + " of " + w);
							}
							for (int d2 = 0; d2 < V; d2++) { // check if others are within distance 2
								if (nd[d2]) continue;
								if (dst[d2]) fail("Vertex " + d2 + " is within distance " + d + " of " + w);
							}
							// save last successful test
							lastV = V;
							lastg = g;
							lastw = w;
							lastd = d;
							lastdlist = dlist;
						}
					}
				}
			}
		}
	}
	
	private void testAllCycles() {
		for (p = 1; p < 4; p++)
		for (int V = 1; V <= 24; V++) {
			for (seed = 0; seed < 8; seed++) {
				for (prob = 0; prob <= 128; prob+=16) {
					for (int numcyc = 0; numcyc < 4; numcyc++) {
						int l[] = new int[numcyc];
						for (int opt = 0; opt < (1 << (numcyc * 2)); opt++) {
							this.V = V;
							for (int n = 0; n < numcyc; n++) {
								l[n] = ((opt >> (n * 2)) & 3) + 1;
								this.V += l[n];
							}
							g = this.randomEdges(seed, 0, V, prob, true);
							cycles = new LinkedList<LinkedList<Edge>>();
							mins = new LinkedList<Edge>();
							int first = V;
							for (int n = 0; n < numcyc; first += l[n], n++) {
								LinkedList<Edge> cycle = new LinkedList<Edge>();
								int vf = first - n - 1;
								Edge min = new Edge(r(vf), r(first));
								cycle.add(min);
								g.add(min);
								for (int lv = 0; lv < l[n] - 1; lv++) {
									int v1 = r(first + lv);
									int v2 = r(first + lv + 1);
									Edge e = new Edge(v1, v2);
									if (e.compareTo(min) < 0) min = e;
									cycle.add(e);
									g.add(e);
								}
								Edge last = new Edge(r(first + l[n] - 1), r(vf));
								if (last.compareTo(min) < 0) min = last;
								cycle.add(last);
								g.add(last);
								cycles.add(cycle);
								mins.add(min);
							}
							gr = createGraph(this.V, g);
							remEdges = gr.removeAllCycles();
							assertNotEquals(null, remEdges);
							assertEquals(numcyc, remEdges.size());
							for (Edge e : remEdges) assertTrue(mins.contains(e));
							lastV = this.V;
							lastg = g;
							lastcyc = cycles;
						}
					}
				}
			}
		}
	}
	
	private void testOneCycle() {
		for (p = 1; p < 4; p++)
		for (int V = 1; V <= 32; V++) {
			for (seed = 0; seed < 8; seed++) {
				for (prob = 0; prob <= 128; prob+=16) {
					for (int l = 2; l < 7; l++) {
						this.V = V + l;
						g = this.randomEdges(seed, 0, V, prob, true);
						cycles = new LinkedList<LinkedList<Edge>>();
						LinkedList<Edge> cycle = new LinkedList<Edge>();
						Edge min = new Edge(r(V), r(V + 1));
						for (int lv = 0; lv < l; lv++) {
							int v1 = r(V + lv);
							int v2 = r(V + ((lv + 1) % l));
							Edge e = new Edge(v1, v2);
							if (e.compareTo(min) < 0) min = e;
							cycle.add(e);
							g.add(e);
						}
						cycles.add(cycle);
						gr = createGraph(this.V, g);
						rem = gr.removeEdgeInCycle();
						assertEquals(min, rem);
						lastV = this.V;
						lastg = g;
						lastcyc = cycles;
					}
				}
			}
		}
	}
	
	private void SCC() {
		MyStack<Integer> sccOrd = new MyStack<Integer>();
		int V = gr.getNumVertices();
		Digraph original = gr;
		Digraph rev = new Digraph(V);
		
		for (int v = 0; v < V; v++)
			for (int w : gr.getNeighbors(v))
				rev.addEdge(w, v);
		
		marked = new boolean[V];
		id = new int[V];
		count = 0;
		revPost = sccOrd;
		gr = rev;
		for (int v = 0; v < V; v++) {
			if (!marked[v]) DFS(v);
		}
		
		marked = new boolean[V];
		revPost = new MyStack<Integer>();
		gr = original;
		for (int v : sccOrd) {
			if (!marked[v]) {
				DFS(v);
				count++;
			}
		}
	}
	
	private void DFS(int v) {
		marked[v] = true;
		id[v] = count;
		for (int w : gr.getNeighbors(v)) {
			if (!marked[w]) DFS(w);
		}
		revPost.push(v);
	}
	
	private void testMethod(int method_id) throws Exception {
		try {
			System.out.print(border + test + border);
			switch (method_id) {
			case 0: testOneCycle(); break;
			case 1: testAllCycles(); break;
			case 2: testIsTopo(); break;
			case 3: testTopo(); break;
			case 4: testDistance(); break;
			}
		} catch(AssertionError aerr) {
			ae = aerr;
		} catch(Exception err) {
			e = err;
		}
		
		if (ae != null || e != null) {
			System.out.print("\n" + border + test + failed + border);
			System.out.println("failing case V = " + V + " seed = " + seed + " prob = " + prob + "/128 and permutation = " + p);
			System.out.println("the corresponding digraph is:");
			System.out.println(graphToString(V, g));
			
			String retStr = null;
			switch (method_id) {
			case 0: // testRemoveCycle (One cycle)
				System.out.println("the corresponding cycles in the graph are: ");
				System.out.print(cyclesToString(cycles));
				System.out.println("the returned edge is: " + (rem == null ? "null" : rem.toString()));
				break;
			case 1: // testRemoveCycle (One cycle)
				System.out.println("the corresponding cycles in the graph are: ");
				System.out.print(cyclesToString(cycles));
				System.out.println("the returned edges are: " + (remEdges == null ? "null" : remEdges.toString()));
				break;
			case 2: // testIsTopo
				System.out.print("failing vertex order is: ");
				for (int v : topo) System.out.print(v + " ");
				System.out.println();
				break;
			case 3: // testTopo
				retStr = "topological sort";
				break;
			case 4: // testCycle
				System.out.println("the corresponding vertex list(v=" + w + ", d=" + d + ") returned is:");
				System.out.println(dlistToString(dlist));
				break;
			}
			
			if (ret == null) {
				if (retStr != null) System.out.println("didn't return a " + retStr + "!");
			} else {
				System.out.print("returned an erroneous " + retStr + ": ");
				for (int v : ret) System.out.print(v + " ");
				System.out.println();
			}
			
			if (lastg != null) {
				System.out.println("the last successful graph is:");
				System.out.println(graphToString(lastV, lastg));
				if (method_id < 2) {
					System.out.println("the corresponding cycles in the graph are: ");
					System.out.print(cyclesToString(lastcyc));
				} else if (method_id == 4) {
					System.out.println("the last successful vertex list(v=" +lastw +", d=" + lastd + ") returned is:");
					System.out.println(dlistToString(lastdlist));
				}
			}
			
			if (ae != null) throw ae;
			if (e != null) throw e;
		} else {
			System.out.print(border + test + passed + border);
		}
	}
	
	@Test
	public void testGraphVerticesDistance() throws Exception {
		test = "* Testing vertices within distance d      *\n";
		testMethod(4);
	}
	
	@Test
	public void testTopological() throws Exception {
		test = "* Testing topological order               *\n";
		testMethod(3);
	}
	
	@Test
	public void testIsTopological() throws Exception {
		test = "* Testing isTopological                   *\n";
		testMethod(2);
	}
	
	@Test
	public void testAllCyclesRemoval() throws Exception {
		test = "* Testing removal of all cycles           *\n";
		testMethod(1);
	}
	
	@Test
	public void testOneCycleRemoval() throws Exception {
		test = "* Testing removal of one cycle            *\n";
		testMethod(0);
	}
}