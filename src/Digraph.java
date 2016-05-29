/*
 * CSC 301 - COMP 202
 * Koc University / DePaul University
 * Algorithms and Data Structures Course
 * Spring 2016
 * Project 5 - Phase 2
 * Group Project - Project Team : 10
 * Zafer Cavdar with Thomas Barry 
 */
import java.util.*;


public class Digraph {

	private int V;
	private int E;
	private LinkedList<Integer> adj[];

	// stack to keep edges that are part of cycle
	private Stack<Edge> cycle;
	
	private boolean[] isExplored_Cycle;
	private int[] backEdge;
	private boolean[] atStack;

	// boolean variables for DFS and BFS related methods
	private boolean[] isExplored_Distance;
	private boolean[] isExplored_Topo;
	
	// used to assign vertices
	private int topoNumber;


	// create an empty graph with V vertices
	// constructor
	public Digraph(int V) {
		this.V = V;
		this.E = 0;
		adj = (LinkedList<Integer> [])(new LinkedList[V]);
		for (int i = 0; i < V; i++) {
			adj[i] = new LinkedList<Integer>();
		}
	}

	// return the number of vertices
	public int getNumVertices() {
		return V;
	}

	// return the number of edges
	public int getNumEdges() {
		return E;
	}

	// add a new edge between vertices v and w
	public void addEdge(int v, int w) {
		if (adj[v].contains(w)) return;
		adj[v].add(w);
		E++;
	}

	// return the list of vertices which are adjacent to vertex v
	public Iterable<Integer> getNeighbors(int v) {
		return adj[v];
	}

	// remove a single edge that is part of a directed cycle, and return the edge that is removed
	// call DFS method and check whether we have cycle or not
	// if cycle is not null, find the lexicographically minimum edge and 
	// REMOVE it from the adjacency list of the corresponding vertex
	public Edge removeEdgeInCycle() {
		isExplored_Cycle =  new boolean[V];
		atStack = new boolean[V];
		backEdge = new int[V];
		for (int v = 0 ; v < V; v++){
			if (!isExplored_Cycle[v] && cycle == null)
				DFS(v);
		}

		Edge min = null;

		if (cycle != null){
			min = cycle.pop();
			int size = cycle.size();

			for (int x = 0 ; x < size; x++){
				int cmp = cycle.peek().compareTo(min);
				if (cmp < 0){
					min = cycle.pop();
				}
				else{
					cycle.pop();
				}
			}

			// remove part
			Object o = (Object) min.w;
			adj[min.v].remove(o);
		}

		return min;
	}

	// DFS method which detects a cycle and its trace
	// using back edges, it updates the cycle stack
	// This method is called by removeEdgeInCycle()
	private void DFS(int v){
		atStack[v] = true;
		isExplored_Cycle[v] = true;

		for (int w: adj[v]){
			if (cycle!= null){
				return;
			}

			else if (!isExplored_Cycle[w]){
				backEdge[w] = v;
				DFS(w);
			}

			else if (atStack[w]){
				cycle= new Stack<Edge>();
				for (int x = v; x != w; x = backEdge[x]){
					Edge push = new Edge(backEdge[x],x);
					cycle.push(push);
				}
				cycle.push(new Edge(v,w));
			}
		}
		atStack[v] = false;
	}


	// remove one edge per cycle until there are no more cycles, and return the list of edges in the order in which they are removed
	// This method calls removeEdgeInCycle() method while cycles exist in the graph
	// All removed edges are stored in a edge list and this list is returned.
	public List<Edge> removeAllCycles() {
		ArrayList<Edge> allRemoved = new ArrayList<Edge>();
		Edge edge = removeEdgeInCycle();
		if (edge != null)
			allRemoved.add(edge);

		while (edge != null){
			cycle = null;
			edge = removeEdgeInCycle();
			if (edge != null)
				allRemoved.add(edge);
		}

		return allRemoved;
	}

	// in-class private method
	// Specialized DFS method that assigns topological order numbers to each vertex
	// in the post order
	// label input is used by topologicalSort() method
	private void topologicalDFS(int[] label, int v){
		isExplored_Topo[v] = true;
		for (int w : adj[v]){
			if (!isExplored_Topo[w]){
				topologicalDFS(label,w);
			}
		}
		label[topoNumber]= v;
		topoNumber--;
	}

	// topological sort of vertices if there are no cycles, otherwise return null
	
	// first call cycle detector DFS method to determine whether cycles exist or not
	// if not exists, then call topologicalDFS to find the topological order of each vertex
	// return this order
	public List<Integer> topologicalSort() {

		isExplored_Cycle =  new boolean[V];
		atStack = new boolean[V];
		backEdge = new int[V];
		for (int v = 0 ; v < V; v++){
			if (!isExplored_Cycle[v] && cycle == null)
				DFS(v);
		}

		boolean cycleExists = (cycle != null);
		cycle = null;

		if (!cycleExists){
			int[] label =  new int[V];
			ArrayList<Integer> sorting = new ArrayList<Integer>();
			isExplored_Topo = new boolean[V];

			topoNumber = V-1;

			for (int v = 0; v < V; v++){
				if (!isExplored_Topo[v]){
					topologicalDFS(label, v);
				}
			}

			for (int i = 0; i < label.length; i++){
				sorting.add(label[i]);
			}
			return sorting;
		}
		else {
			return null;
		}
	}

	// check to see whether the given list of vertices are a topological sort
	// visit each edge and check for all v that it comes before w (just check the list indices)
	public boolean isTopological(List<Integer> sort) {
		for (int v= 0; v< V; v++){
			for (int w: adj[v]){
				int indexv = sort.indexOf(v);
				int indexw = sort.indexOf(w);
				if (indexv > indexw)
					return false;
			}
		}
		return true;
	}

	// generalized form of the verticesWithinDistance2
	// return vertices within distance d from vertex v
	
	// simply this is a BFS method
	// each level in the BFS corresponds to verticesWithin that distance
	// BFS continues until finding d'th List, then merge them
	public List<Integer> verticesWithinDistance(int v, int d) {

		ArrayList<Integer> result = new ArrayList<Integer>();

		isExplored_Distance = new boolean[V];
		ArrayList<Integer>[] L = new ArrayList[V];
		for (int i = 0; i < V; i++){
			L[i] = new ArrayList<Integer>();
		}
		int currentLevel = 0;

		L[currentLevel].add(v);
		isExplored_Distance[v] = true;
		while(!L[currentLevel].isEmpty()){
			for (int v1 : L[currentLevel]){
				for (int w: adj[v1]){
					if (!isExplored_Distance[w]){
						isExplored_Distance[w] = true;
						L[currentLevel+1].add(w);
					}
				}
			}
			currentLevel++;
			if (currentLevel > d)
				break;
		}

		for (int x = 0; x <= d; x++){
			result.addAll(L[x]);
		}

		return result;
	}
}