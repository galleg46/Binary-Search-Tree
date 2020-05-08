package edu.uwm.cs351;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import junit.framework.TestCase;

//Anthony Gallegos
/**
 * An index of planets using some comparable key.
 * We use a function that extracts a key from a planet.
 * The index is sorted using an (unbalanced) binary search tree.
 * @param <K> type of key
 */
public class PlanetIndex<K extends Comparable<K>> {
	public static enum Traversal { PRE_ORDER, IN_ORDER, POST_ORDER }

	private static class Node {
		Planet data;
		Node left, right;

		Node(Planet p) { data = p; }
	}

	private final Function<Planet,K> keyExtractor;
	private Node root;
	private int size;

	private static boolean doReport = true; // changed only by invariant tester

	private boolean report(String error) {
		if (doReport) System.out.println("Invariant error: " + error);
		// else System.out.println("Caught problem: " + error);
		return false;
	}

	/**
	 * Count the number of nodes in the subtree rooted here and
	 * return the total number, or -1 if we find ourselves deeper
	 * than the number of nodes supposedly in the tree. 
	 * @param r root of subtree
	 * @param d depth of this subtree (0 = root)
	 * @return number of nodes in the subtree, or -1
	 * if got deeper than claimed number of nodes in tree (size).
	 */
	private int countNodes(Node r, int d) {
		if (r == null) return 0;
		if (d >= size) return -1;
		int n1 = countNodes(r.left,d+1);
		int n2 = countNodes(r.right,d+1);
		if (n1 == -1 || n2 == -1) return -1;
		return 1 + n1 + n2;
	}

	/**
	 * Check a subtree to see if items are in the correct order.
	 * If not, an error message is printed and false is return.
	 * @param r subtree
	 * @param lo inclusive lower bound of keys, or null if no lower bound
	 * @param hi inclusive upper bound of keys, or null if no upper bound
	 * @param p1 exclusive lower bound for planets with the same key, or null if no bound
	 * @param p2 exclusive upper bound for planets with the same key, or null if no bound
	 * @return true if all keys in tree inside the range, and subtrees also good
	 */
	private boolean okTree(Node r, K lo, K hi, Planet p1, Planet p2) {
		//Root is null, thats ok
		if(r == null) return true;
		
		//data of the root cannot be null
		Planet p = r.data;
		if(p == null) return report("found null planet in tree");
		
		K key = keyExtractor.apply(r.data);
		if(key == null) return report("Key of " +p +"is null");
		
		//outside of lower bound(inclusive)
		if(lo != null && lo.compareTo(key) > 0) return report("Found " +p +" with " +key +" < " +lo);
		
		//outside of upper bound(inclusive)
		if(hi != null && hi.compareTo(key) < 0) return report("Found " +p +" with " +key +" > " +hi);  
		
		//outside of lowerbound(Exclusive)
		if(p1 != null && keyExtractor.apply(p1).equals(key) && p1.compareTo(p) >= 0)
		{
			return report("Found " +p +" <= " +p1);
		}
		
		//outside of the upperbound(Exclusive)
		if(p2 != null && keyExtractor.apply(p2).equals(key) && p2.compareTo(p) <= 0) 
		{
			return report("Found " +p +" >= " +p2);
		}
		
		//recursion for each side of the tree
		return okTree(r.left, lo, key, p1, p) && okTree(r.right, key, hi, p, p2);
	}

	public boolean wellFormed() {
		// 1. The tree is structured correctly
		if(okTree(root, null, null, null, null) == false) return report("tree is not structured correctly");
		
		// 2. The number of nodes is the same is the tree size
		if(countNodes(root, 0) != size) return report("the number of nodes does not equal the size");
		
		// Nothing is wrong
		return true;
	}

	/**
	 * Create an empty index of planets
	 * @param ex key extractor to use, must not be null
	 */
	public PlanetIndex(Function<Planet,K> ex) {
		if (ex == null) throw new NullPointerException("key extractor must not be null");
		keyExtractor = ex;
		assert wellFormed() : "tree badly formed at end of constructor";
	}

	/**
	 * Return number of planets in index
	 * @return size of index
	 */
	public int size() {
		assert wellFormed() : "tree badly formed at start of size()";
		return size;
	}

	/**
	 * Add a planet to the index, returning true unless it was already there
	 * or unless the planet's key is null.
	 * @param p planet to add, must not be null
	 * @return whether planet needed to be added.
	 */
	public boolean add(Planet p) {
		assert wellFormed() : "Tree badly formed at the start of add";
		
		if(p == null) throw new NullPointerException("cannot add null");
		
		K key = keyExtractor.apply(p);
		
		if(key == null) return false;
		
		int oldSize = size;
		
		root = doAdd(root, p, key);
		
		assert wellFormed() : "Tree badly formed at the end of add";
		
		return oldSize < size;
		
	}
	
	private Node doAdd(Node r, Planet p, K key)
	{
		if(r == null)
		{
			++size;
			return new Node(p);
		}
		
		K here = keyExtractor.apply(r.data);
		int c = key.compareTo(here);
		
		if(c == 0) c = p.compareTo(r.data);
		if(c == 0) return r;
		
		if(c < 0)
		{
			r.left = doAdd(r.left, p, key);
		}
		else
		{
			r.right = doAdd(r.right, p, key);
		}
		
		
		return r;
	}

	/**
	 * Add all planets from this collection.
	 * @param coll collection to add planets, must not be null.
	 */
	public void addAll(Collection<Planet> coll) {
		for(Planet p : coll)
		{
			add(p);
		}
		
	}

	/**
	 * Add all planets from the parent to this index.
	 * @param index existing index of planets, must not be null
	 */
	public void addAll(PlanetIndex<?> index) {
		index.doAll((p) -> add(p), null, null, Traversal.PRE_ORDER);
	}

	/**
	 * Find all the planets in the given range in the index.
	 * The consumer function is called on each in turn.
	 * If the traversal is {@link Traversal#IN_ORDER}, then the
	 * planets will be handled in order.  Otherwise, the order depends
	 * on internal data structure order, which can be unpredictable.
	 * @param f function to call on each planet in range, must not be null
	 * @param lo inclusive lower bound (or null, for no lower bound)
	 * @param hi inclusive upper bound (or null, for no upper bound)
	 * @param t traversal order, must not be null
	 */
	public void doAll(Consumer<Planet> f, K lo, K hi, Traversal t) {
		assert wellFormed() : "invariant failed in doAll";
		if (t == null || f == null) throw new NullPointerException("doAll requires non-null arguments");
		doAll(root,f,lo,hi,t);
	}

	private void doAll(Node r, Consumer<Planet> f, K lo, K hi, Traversal t) {
		if(r == null) return;
		
		K key = keyExtractor.apply(r.data);
		
		boolean inLeft = lo == null || lo.compareTo(key) <= 0;
		boolean inRight = hi == null || hi.compareTo(key) >= 0;
		boolean inBoth = inLeft && inRight;
		
		if(t == Traversal.PRE_ORDER && inBoth) f.accept(r.data);
		if(inLeft) doAll(r.left, f, lo, hi, t);
		
		if(t == Traversal.IN_ORDER && inBoth) f.accept(r.data);
		if(inRight) doAll(r.right, f, lo, hi, t);
		
		if(t == Traversal.POST_ORDER && inBoth) f.accept(r.data);
	}

	// Don't change this class:
	public static class TestInternals extends TestCase {
		protected PlanetIndex<Double> self;
		
		private Planet p0 = new Planet("TestA",0);
		private Planet p1 = new Planet("TestA",1);
		private Planet p2 = new Planet("TestA",2);
		private Planet p3 = new Planet("TestA",11); // later in mass but before in name
		private Planet p4 = new Planet("TestA",12);
		private Planet p5 = new Planet("TestA",101);
		private Planet d1 = new Planet("TestA",1);
		private Planet d2 = new Planet("TestA",2);
		private Planet d3 = new Planet("TestA",11);
		private Planet s1 = new Planet("TestB",1);
		private Planet s2 = new Planet("TestB",2);
		private Planet s3 = new Planet("TestB",11);
		private Planet t3 = new Planet("TestB",11);
		private Planet s5 = new Planet("TestB",101);
		private Planet t5 = new Planet("TestB",101);
		private Planet a1 = new Planet("TestC",1);
		private Planet a2 = new Planet("TestC",2);
		private Planet a3 = new Planet("TestC",11);
		private Planet a5 = new Planet("TestC",101);
		

		@Override
		protected void setUp() throws Exception {
			self = new PlanetIndex<Double>((p) -> (Double)p.getField(2));
			self.size = 3;
			doReport = false;
		}

		public void testC0() {
			assertEquals(0,self.countNodes(null,0));
			assertEquals(0,self.countNodes(null,1));
			assertEquals(0,self.countNodes(null,2));
			assertEquals(0,self.countNodes(null,3));
			assertEquals(0,self.countNodes(null,4));
		}

		public void testC1() {
			assertEquals(1,self.countNodes(new Node(p1), 0));
			assertEquals(1,self.countNodes(new Node(p1), 1));
			assertEquals(1,self.countNodes(new Node(p1), 2));
			assertEquals(-1,self.countNodes(new Node(p1), 3));
			assertEquals(-1,self.countNodes(new Node(p1), 4));
		}

		public void testC2() {
			Node n1 = new Node(p1);
			Node n2 = new Node(p2);
			n2.right = n1; // badly ordered, but countNodes should not care
			assertEquals(2,self.countNodes(n2, 0));
			assertEquals(2,self.countNodes(n2, 1));
			assertEquals(-1,self.countNodes(n2, 2));
			assertEquals(-1,self.countNodes(n2, 3));
			assertEquals(-1,self.countNodes(n2, 4));	
		}

		public void testC3() {
			Node n1 = new Node(p1);
			Node n2 = new Node(p2);
			Node n3 = new Node(p3);
			n2.left = n1;
			n2.right = n3;
			assertEquals(3,self.countNodes(n2, 0));
			assertEquals(3,self.countNodes(n2, 1));
			assertEquals(-1,self.countNodes(n2, 2));
			assertEquals(-1,self.countNodes(n2, 3));
			assertEquals(-1,self.countNodes(n2, 4));
		}

		public void testC4() {
			Node n1 = new Node(p1);
			Node n2 = new Node(p2);
			Node n3 = new Node(p3);
			Node n4 = new Node(p0);
			n1.left = n2;
			n1.right = n3;
			n3.right = n4;
			assertEquals(4,self.countNodes(n1, 0));
			assertEquals(-1,self.countNodes(n1, 1));
			assertEquals(-1,self.countNodes(n1, 2));
			assertEquals(-1,self.countNodes(n1, 3));
			assertEquals(-1,self.countNodes(n1, 4));
		}

		public void testC5() {
			Node n1 = new Node(p2);
			Node n2 = new Node(p1);
			n1.left = n2;
			n1.right = n1;
			assertEquals(-1,self.countNodes(n1,0));
			assertEquals(-1,self.countNodes(n1,1));
			assertEquals(-1,self.countNodes(n1,2));
			assertEquals(-1,self.countNodes(n1,3));
			assertEquals(-1,self.countNodes(n1,4));
		}

		public void testO0() {
			Node n = new Node(p2);
			doReport = true;
			assertTrue(self.okTree(n,null,null,null,null));
			assertTrue(self.okTree(n, null, 3.0, null, null));
			assertTrue(self.okTree(n, 1.0, null, null, null));
			assertTrue(self.okTree(n, 1.0, 3.0, null, null));
			assertTrue(self.okTree(n, 2.0, 3.0, null, null));
			assertTrue(self.okTree(n, 1.0, 2.0, null, null));
			doReport = false;
			assertFalse(self.okTree(n, 1.0, 1.9, null, null));
			assertFalse(self.okTree(n, 2.1, 3.0, null, null));
			assertFalse(self.okTree(n, null, 1.9, null, null));
			assertFalse(self.okTree(n, 2.1, null, null, null));
		}

		public void testO1() {
			Node n = new Node(s2);
			doReport = true;
			assertTrue(self.okTree(n, null, null, null, a2));
			assertTrue(self.okTree(n, null, null, p2, null));
			assertTrue(self.okTree(n, null, null, p2, a2));
			assertTrue(self.okTree(n, null, null, null, a1));
			assertTrue(self.okTree(n, null, null, a3, null));
			assertTrue(self.okTree(n, null, null, a3, a1));
			doReport = false;
			assertFalse(self.okTree(n, null, null, a2, null));
			assertFalse(self.okTree(n, null, null, null, p2));
		}

		public void testO2() {
			Node n = new Node(s2);
			doReport = true;
			assertTrue(self.okTree(n, 2.0, 2.0, p2, null));
			assertTrue(self.okTree(n, 1.0, 3.0, p2, a2));
			assertTrue(self.okTree(n, 1.0, 3.0, a3, a1));
			doReport = false;
			assertFalse(self.okTree(n, 0.0, 1.0, null, a1));
			assertFalse(self.okTree(n, 2.1, 3.0, a3, null));
			assertFalse(self.okTree(n, 1.0, 3.0, a2, null));
			assertFalse(self.okTree(n, 1.0, 3.0, null, p2));
		}

		public void testO3() {
			Node n = new Node(null);
			assertFalse(self.okTree(n,null,null,null,null));
		}

		public void testO4() {
			Node n = new Node(s2);
			Node n1 = new Node(a1);
			Node n2 = new Node(p3);
			n.left = n1;
			n.right = n2;
			doReport = true;
			assertTrue(self.okTree(n, null, null, null, null));
			assertTrue(self.okTree(n, 1.0, 11.0, s1, s3));
			doReport = false;
			assertFalse(self.okTree(n, 1.0, 11.0, a1, a3));
			assertFalse(self.okTree(n, 1.0, 11.0, p1, d3));
			assertFalse(self.okTree(n, 1.5, 11.0, p1, a3));
			assertFalse(self.okTree(n, 1.0, 10.0, p1, d3));
		}
		
		public void testO5() {
			Node n = new Node(s2);
			Node n1 = new Node(a1);
			Node n2 = new Node(p3);
			n.left = n2;
			assertFalse(self.okTree(n, null, null, null, null));
			assertFalse(self.okTree(n, 1.0, 11.0, s1, s3));
			n.right = n1;
			n.left = null;
			assertFalse(self.okTree(n, null, null, null, null));
			assertFalse(self.okTree(n, 1.0, 11.0, s1, s3));
		}
		
		public void testO6() {
			Node n = new Node(s3);
			Node n1 = new Node(p1);
			Node n1a = new Node(p2);
			Node n2 = new Node(p5);
			Node n2a = new Node(p4);
			n.left = n1;
			n.right = n2;
			n1.right = n1a;
			n2.left = n2a;
			doReport = true;
			assertTrue(self.okTree(n, null, null, null, null));
			assertTrue(self.okTree(n, 1.0, 101.0, p3, a3));
			doReport = false;
			assertFalse(self.okTree(n, 1.0, 101.0, d1, a3));
			assertFalse(self.okTree(n, 1.0, 101.0, d3, p5));
		}
		
		public void testO7() {
			Node n = new Node(s3);
			Node n1 = new Node(p1);
			Node n1a = new Node(p3);
			Node n2 = new Node(p5);
			Node n2a = new Node(a3);
			n.left = n1;
			n.right = n2;
			n1.right = n1a;
			n2.left = n2a;
			doReport = true;
			assertTrue(self.okTree(n, null, null, null, null));
			assertTrue(self.okTree(n, 1.0, 101.0, p0, s5));
		}
		
		public void testO8() {
			Node n = new Node(s3);
			Node n1 = new Node(p1);
			Node n1a = new Node(p4);
			Node n2 = new Node(p5);
			Node n2a = new Node(p2);
			n.left = n1;
			n.right = n2;
			n1.right = n1a;
			assertFalse(self.okTree(n, null, null, null, null));
			n1.right = null;
			n2.left = n2a;
			assertFalse(self.okTree(n, null, null, null, null));
			n2a.data = p3;
			assertFalse(self.okTree(n, null, null, null, null));
			n2.left = null;
			n1.right = n1a;
			n1a.data = a3;
			assertFalse(self.okTree(n, null, null, null, null));
		}
		
		public void testO9() {
			Node n = new Node(s3);
			Node n1 = new Node(p1);
			Node n1a = new Node(p3);
			Node n2 = new Node(s5);
			Node n2a = new Node(a3);
			Node n1ab = new Node(p2);
			Node n2ab = new Node(p4);
			n.left = n1;
			n.right = n2;
			n1.right = n1a;
			n2.left = n2a;
			n1a.left = n1ab;
			n2a.right = n2ab;
			doReport = true;
			assertTrue(self.okTree(n, null, null, null, null));
			assertTrue(self.okTree(n, 1.0, 101.0, p0, a5));
			n1ab.data = s1;
			n2ab.data = p5;
			assertTrue(self.okTree(n, null, null, null, null));
			assertTrue(self.okTree(n, 1.0, 101.0, p0, a5));
			n1ab.data = p1;
			doReport = false;
			assertFalse(self.okTree(n, null, null, null, null));
			assertFalse(self.okTree(n, 1.0, 101.0, p0, a5));
			n1ab.data = a1;
			n2ab.data = a5;
			assertFalse(self.okTree(n, null, null, null, null));
			assertFalse(self.okTree(n, 1.0, 101.0, p0, a5));			
		}
		
		public void testW0() {
			self.root = null;
			self.size = 0;
			doReport = true;
			assertTrue(self.wellFormed());
		}
		
		public void testW1() {
			self.root = new Node(null);
			self.size = 1;
			assertFalse(self.wellFormed());
		}
		
		public void testW2() {
			Node n = new Node(p3);
			self.root = new Node(p3);
			n.right = n;
			assertFalse(self.wellFormed());
			n.right = null;
			self.size = 1;
			doReport = true;
			assertTrue(self.wellFormed());
		}
		
		public void testW3() {
			Node n = new Node(s2);
			Node n1 = new Node(a1);
			Node n2 = new Node(p3);
			n.left = n1;
			n.right = n2;
			self.root = n;
			
			// try some bad things:
			self.size = 4;
			assertFalse(self.wellFormed());
			self.size = 3;
			n2.data = null;
			assertFalse(self.wellFormed());
			n2.data = p3;
			n.left = null;
			assertFalse(self.wellFormed());
			n.left = n1;
			
			// everything should be OK now:
			doReport = true;
			assertTrue(self.wellFormed());
		}
		
		public void testW4() {
			Node n = new Node(s3);
			Node n1 = new Node(p1);
			Node n1a = new Node(d2);
			Node n2 = new Node(s5);
			Node n2a = new Node(p4);
			n.left = n1;
			n.right = n2;
			n1.right = n1a;
			n2.left = n2a;
			self.root = n;
			self.size = 5;
			
			// try some errors:
			
			// first on n1a:
			n1a.data = s3;
			assertFalse(self.wellFormed());
			n1a.data = a3;
			assertFalse(self.wellFormed());
			n1a.data = d1;
			assertFalse(self.wellFormed());
			n1a.data = null;
			assertFalse(self.wellFormed());
			n1a.data = p2;
			
			// then on n2a
			n2a.data = t3;
			assertFalse(self.wellFormed());
			n2a.data = d3;
			assertFalse(self.wellFormed());
			n2a.data = t5;
			assertFalse(self.wellFormed());
			n2a.data = null;
			assertFalse(self.wellFormed());
			n2a.data = p4;
		}
		
		public void testW5() {
			Node n = new Node(s3);
			Node n1 = new Node(p1);
			Node n1a = new Node(p2);
			Node n2 = new Node(s5);
			Node n2a = new Node(p4);
			n.left = n1;
			n.right = n2;
			n1.right = n1a;
			n2.left = n2a;
			self.root = n;
			self.size = 5;
			
			doReport = true;
			assertTrue(self.wellFormed());
			// some good changes:
			n1a.data = p3;
			assertTrue(self.wellFormed());
			n1a.data = a1;
			assertTrue(self.wellFormed());		
			n2a.data = a3;
			assertTrue(self.wellFormed());
			n2a.data = p5;
			assertTrue(self.wellFormed());
		}
	}
}
