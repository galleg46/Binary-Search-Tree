import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.uwm.cs.junit.LockedTestCase;
import edu.uwm.cs351.Planet;
import edu.uwm.cs351.PlanetIndex;
import edu.uwm.cs351.PlanetIndex.Traversal;

public class TestPlanetIndex extends LockedTestCase {

	protected void assertException(Class<? extends Throwable> c, Runnable r) {
		try {
			r.run();
			assertFalse("Exception should have been thrown",true);
		} catch (RuntimeException ex) {
			assertTrue("should throw exception of " + c + ", not of " + ex.getClass(), c.isInstance(ex));
		}
	}
	
	protected String asString(Supplier<?> r) {
		try {
			Object o = r.get();
			if (o == null) return "null";
			return o.toString();
		} catch (RuntimeException ex) {
			return ex.getClass().getSimpleName();
		}
	}

	private PlanetIndex<Double> pi;
	private PlanetIndex<String> pis;
	
	private Planet p0 = new Planet("TestA",0);
	private Planet p1 = new Planet("TestA",1);
	private Planet p2 = new Planet("TestA",2);
	private Planet p3 = new Planet("TestA",3);
	private Planet p4 = new Planet("TestA",4);
	private Planet p5 = new Planet("TestA",5);
	private Planet p6 = new Planet("TestA",6);
	private Planet p7 = new Planet("TestA",7);

	// duplicates:
	private Planet d0 = new Planet("TestA",0);
	private Planet d1 = new Planet("TestA",1);
	private Planet d2 = new Planet("TestA",2);
	private Planet d3 = new Planet("TestA",3);
	private Planet d4 = new Planet("TestA",4);
	private Planet d5 = new Planet("TestA",5);
	private Planet d6 = new Planet("TestA",6);
	private Planet d7 = new Planet("TestA",7);

	// similar
	private Planet s0 = new Planet("TestB",0);
	private Planet s1 = new Planet("TestB",1);
	private Planet s2 = new Planet("TestB",2);
	private Planet s3 = new Planet("TestB",3);
	private Planet s4 = new Planet("TestB",4);
	private Planet s5 = new Planet("TestB",5);
	private Planet s6 = new Planet("TestB",6);
	private Planet s7 = new Planet("TestB",7);

	// alternates
	private Planet a0 = new Planet("TestC",0);
	private Planet a1 = new Planet("TestC",1);
	private Planet a2 = new Planet("TestC",2);
	private Planet a3 = new Planet("TestC",3);
	private Planet a4 = new Planet("TestC",4);
	private Planet a5 = new Planet("TestC",5);
	private Planet a6 = new Planet("TestC",6);
	private Planet a7 = new Planet("TestC",7);
	
	private static final int MASS_INDEX = Planet.getFieldIndex("mass");
	private static final int NAME_INDEX = Planet.getFieldIndex("identifier");

	private Object[] makeNamedObjectArray(String name) {
		Object[] result = new Object[Planet.numFields()];
		result[NAME_INDEX] = name;
		return result;
	}
	
	/**
	 * Return the mass truncated to an integer (used for testing only)
	 * @param p planet to get mass for, must not be null
	 * @return mass of planet truncated to an integer
	 */
	protected int getMass(Planet p) {
		return ((Double)p.getField(MASS_INDEX)).intValue();
	}
	
	private Planet missingName = new Planet(null,3);
	private Planet missingMass = new Planet(makeNamedObjectArray("TestB"));

	protected static PlanetIndex<Double> makeMassIndex() {
		return new PlanetIndex<>((p) -> (Double)p.getField(MASS_INDEX));
	}
	
	protected static PlanetIndex<String> makeNameIndex() {
		return new PlanetIndex<>((p) -> (String)p.getField(NAME_INDEX));
	}
	
	protected void setUp() throws Exception {
		try {
			assert (int)(p0.getField(MASS_INDEX)) == 0;
			System.err.println("Assertions must be enabled to use this test suite.");
			System.err.println("In Eclipse: add -ea in the VM Arguments box under Run>Run Configurations>Arguments");
			assertFalse("Assertions must be -ea enabled in the Run Configuration>Arguments>VM Arguments",true);			
		} catch (ClassCastException ex) {
			assertTrue("Assertions enabled!", true);
		}
		pi = makeMassIndex();
		pis = makeNameIndex();
	}

	protected final Consumer<Planet> doNotCall = (p) -> {
		assertFalse("This consumer should not have been called.",true);
	};

	<K extends Comparable<K>> 
	void testAll(PlanetIndex<K> pi, K lo, K hi, Traversal t, Planet... expected) {
		List<Planet> results = new ArrayList<>();
		pi.doAll((p) -> {
			int n = expected.length;
			int i = results.size();
			assertTrue("Too many planets returned: " + p, i < n);
			assertSame(expected[i],p);
			results.add(p);
		}, lo, hi, t);
		assertEquals(expected.length,results.size());
	}

	<K extends Comparable<K>> 
	void testAll(PlanetIndex<K> pi, K lo, K hi,  Planet... expected) {
		testAll(pi,lo,hi,Traversal.IN_ORDER,expected);
	}

	void testAll(PlanetIndex<?> pi, Traversal t, Planet... expected) {
		testAll(pi,null,null,t,expected);
	}

	void testAll(PlanetIndex<?> pi, Planet... expected) {
		testAll(pi,null,null,Traversal.IN_ORDER,expected);
	}


	/// Locked tests
	
	public void test() {
		// pi is a planet index indexed by mass (numeric)
		assertEquals(0,pi.size());
		// asString gives the result (as a string) or the name of the exception
		assertEquals(Ts(575222589),asString(() -> pi.add(null)));
		// p0 is a planet with name "TestA" and mass = 0
		assertEquals(Ts(1413690320),asString(() -> pi.add(p0)));
		// d0 is a planet that is equal to p0 (but not identical to it)
		assertEquals(Ts(1189682951),asString(() -> pi.add(d0)));
		// s0 is a planet with the same mass as p0 but a different name
		assertEquals(Ts(351187346),asString(() -> pi.add(s0)));
		// missingMass is a planet without mass defined
		assertEquals(Ts(1483968380),asString(() -> pi.add(missingMass)));
		// missingName is a planet without a name, but has mass
		assertEquals(Ts(1383801626),asString(() -> pi.add(missingName)));
		assertEquals(Ti(563344228),pi.size()); // what is size now?
		pi = makeMassIndex();
		testDoAll(false);
	}
	
	private void testDoAll(boolean ignored) {
		// pi starts off empty
		pi.add(p4); // has mass of 4.0
		pi.add(p2); // has mass of 2.0
		pi.add(p1); // ...
		pi.add(p3);
		pi.add(p6);
		// we have a tree:
		//         p4
		//     p2      p6
		//   p1  p3
		List<Integer> results = new ArrayList<>();
		// first in-order
		pi.doAll((p) -> results.add(getMass(p)), null, null, Traversal.IN_ORDER);
		assertEquals(Ti(254510363),results.get(0).intValue()); // what comes first
		assertEquals(Ti(918516802),results.get(1).intValue()); // what comes next
		results.clear();
		// next pre-order
		pi.doAll((p) -> results.add(getMass(p)), null, null, Traversal.PRE_ORDER);
		assertEquals(Ti(164581155),results.get(0).intValue()); // what comes first
		assertEquals(Ti(974787389),results.get(1).intValue()); // what comes next
		results.clear();
		// finally: post-order
		pi.doAll((p) -> results.add(getMass(p)), null, null, Traversal.POST_ORDER);
		assertEquals(Ti(549042176),results.get(0).intValue()); // what comes first
		assertEquals(Ti(1441250241),results.get(1).intValue()); // what comes next
	}
	

	// test0X: basic tests on empty indices

	public void test00() {
		assertEquals(0,pi.size());
	}

	public void test01() {
		assertException(NullPointerException.class, () -> pi.add(null));
	}

	public void test02() {
		assertException(NullPointerException.class, () -> new PlanetIndex<Integer>(null));
	}

	public void test03() {
		assertException(NullPointerException.class, () -> pi.doAll(null, null, null, Traversal.IN_ORDER));
	}

	public void test04() {
		assertException(NullPointerException.class, () -> pi.doAll(doNotCall, null, null, null));
	}

	public void test05() {
		pi.doAll(doNotCall, null, null, Traversal.IN_ORDER);
	}
	
	public void test06() {
		assertFalse(pi.add(missingMass));
		assertEquals(0,pi.size());
	}
	
	public void test07() {
		assertFalse(pis.add(missingName));
		assertEquals(0,pis.size());
	}


	// test1X: very small tests of add without traversal

	public void test10() {
		assertTrue(pi.add(p0));
	}

	public void test11() {
		pi.add(p1);
		assertEquals(1,pi.size());
	}

	public void test12() {
		pi.add(p2);
		assertFalse(pi.add(d2));
	}

	public void test13() {
		pi.add(p3);
		pi.add(d3);
		assertEquals(1,pi.size());
	}

	public void test14() {
		pi.add(p4);
		assertTrue(pi.add(p1));
	}

	public void test15() {
		pi.add(p1);
		pi.add(p5);
		assertEquals(2,pi.size());
	}

	public void test16() {
		pi.add(p6);
		assertTrue(pi.add(s6));
	}

	public void test17() {
		pi.add(p7);
		pi.add(s7);
		assertEquals(2,pi.size());
	}

	public void test18() {
		pi.add(p1);
		pi.add(s1);
		assertFalse(pi.add(d1));
		assertEquals(2,pi.size());
	}

	public void test19() {
		pi.add(s0);
		pi.add(p0);
		assertFalse(pi.add(d0));
		assertEquals(2,pi.size());
	}
	
	public void test1A() {
		PlanetIndex<Planet> pip = new PlanetIndex<Planet>((p) -> p);
		assertTrue(pip.add(s3));
		assertTrue(pip.add(p3));
		assertEquals(2,pip.size());
	}


	// test2X: larger tests of add, again only using size

	public void test20() {
		pi.add(s0);
		pi.add(a0);
		assertTrue(pi.add(p0));
		assertFalse(pi.add(d0));
		assertEquals(3,pi.size());
	}

	public void test21() {
		pi.add(a1);
		pi.add(p1);
		pi.add(s1);
		assertFalse(pi.add(d1));
		assertFalse(pi.add(s1));
		assertFalse(pi.add(a1));
		assertEquals(3,pi.size());
	}

	public void test22() {
		pi.add(p2);
		pi.add(a2);
		pi.add(s2);
		assertFalse(pi.add(d2));
		assertFalse(pi.add(s2));
		assertFalse(pi.add(a2));
		assertEquals(3,pi.size());		
	}

	public void test23() {
		pi.add(p2);
		assertTrue(pi.add(p3));
		assertTrue(pi.add(s2));
		assertTrue(pi.add(a3));
		assertEquals(4,pi.size());
		assertFalse(pi.add(d2));
		assertFalse(pi.add(s2));
		assertFalse(pi.add(d3));
		assertFalse(pi.add(a3));
	}

	public void test24() {
		pi.add(a2);
		assertTrue(pi.add(s4));
		assertTrue(pi.add(p2));
		assertTrue(pi.add(p4));
		assertTrue(pi.add(s2));
		assertEquals(5,pi.size());
		assertFalse(pi.add(d2));
	}

	public void test25() {
		pi.add(p4);
		pi.add(p2);
		pi.add(p6);
		pi.add(p1);
		pi.add(p3);
		pi.add(p5);
		pi.add(p7);
		assertEquals(7,pi.size());
		assertFalse(pi.add(d1));
		assertFalse(pi.add(d2));
		assertFalse(pi.add(d3));
		assertFalse(pi.add(d4));
		assertFalse(pi.add(d5));
		assertFalse(pi.add(d6));
		assertFalse(pi.add(d7));
	}

	public void test26() {
		pi.add(s4);
		pi.add(s2);
		pi.add(s6);
		pi.add(s1);
		pi.add(s3);
		pi.add(s5);
		pi.add(s7);
		assertEquals(7,pi.size());
		assertTrue(pi.add(p1));
		assertTrue(pi.add(p2));
		assertTrue(pi.add(p3));
		assertTrue(pi.add(p4));
		assertTrue(pi.add(p5));
		assertTrue(pi.add(p6));
		assertTrue(pi.add(p7));
		assertEquals(14,pi.size());
		assertFalse(pi.add(d1));
		assertFalse(pi.add(d2));
		assertFalse(pi.add(d3));
		assertFalse(pi.add(d4));
		assertFalse(pi.add(d5));
		assertFalse(pi.add(d6));
		assertFalse(pi.add(d7));
		assertEquals(14,pi.size());		
	}

	public void test27() {
		pi.add(a4);
		pi.add(a2);
		pi.add(a6);
		pi.add(a1);
		pi.add(a3);
		pi.add(a5);
		pi.add(a7);
		assertEquals(7,pi.size());
		assertTrue(pi.add(s1));
		assertTrue(pi.add(s2));
		assertTrue(pi.add(s3));
		assertTrue(pi.add(s4));
		assertTrue(pi.add(s5));
		assertTrue(pi.add(s6));
		assertTrue(pi.add(s7));
		assertEquals(14,pi.size());
		assertTrue(pi.add(p1));
		assertTrue(pi.add(p2));
		assertTrue(pi.add(p3));
		assertTrue(pi.add(p4));
		assertTrue(pi.add(p5));
		assertTrue(pi.add(p6));
		assertTrue(pi.add(p7));
		assertEquals(21,pi.size());
		assertFalse(pi.add(d1));
		assertFalse(pi.add(d2));
		assertFalse(pi.add(d3));
		assertFalse(pi.add(d4));
		assertFalse(pi.add(d5));
		assertFalse(pi.add(d6));
		assertFalse(pi.add(d7));
	}

	public void test28() {
		pi.add(p0); pi.add(s0); pi.add(a0);
		pi.add(p1); pi.add(s1); pi.add(a1);
		pi.add(p2); pi.add(s2); pi.add(a2);
		pi.add(p3); pi.add(s3); pi.add(a3);
		pi.add(p4); pi.add(s4); pi.add(a4);
		pi.add(p5); pi.add(s5); pi.add(a5);
		pi.add(p6); pi.add(s6); pi.add(a6);
		pi.add(p7); pi.add(s7); pi.add(a7);
		assertEquals(24,pi.size());		
		assertFalse(pi.add(d0));
		assertFalse(pi.add(d1));
		assertFalse(pi.add(d2));
		assertFalse(pi.add(d3));
		assertFalse(pi.add(d4));
		assertFalse(pi.add(d5));
		assertFalse(pi.add(d6));
		assertFalse(pi.add(d7));
	}

	public void test29() {
		pi.add(a7); pi.add(s7); pi.add(p7);
		pi.add(a6); pi.add(s6); pi.add(p6);
		pi.add(a5); pi.add(s5); pi.add(p5);
		pi.add(a4); pi.add(s4); pi.add(p4);
		pi.add(a3); pi.add(s3); pi.add(p3);
		pi.add(a2); pi.add(s2); pi.add(p2);
		pi.add(a1); pi.add(s1); pi.add(p1);
		pi.add(a0); pi.add(s0); pi.add(p0);
		assertEquals(24,pi.size());		
		assertFalse(pi.add(d0));
		assertFalse(pi.add(d1));
		assertFalse(pi.add(d2));
		assertFalse(pi.add(d3));
		assertFalse(pi.add(d4));
		assertFalse(pi.add(d5));
		assertFalse(pi.add(d6));
		assertFalse(pi.add(d7));
	}






	/// test 3X: test addAll with collection

	@SafeVarargs
	private static <E> Collection<E> asCollection(E... elements) {
		return Arrays.asList(elements);
	}

	public void test30() {
		pi.addAll(asCollection());
		assertEquals(0,pi.size());
	}

	public void test31() {
		pi.addAll(asCollection(p0));
		assertEquals(1,pi.size());
	}

	public void test32() {
		pi.add(p0);
		pi.addAll(asCollection(d0));
		assertEquals(1,pi.size());
	}

	public void test33() {
		pi.add(p1);
		pi.addAll(asCollection(s2));
		assertEquals(2,pi.size());
	}

	public void test34() {
		pi.add(p2);
		pi.add(p4);
		pi.addAll(asCollection(d4,d2));
		assertEquals(2,pi.size());
	}

	public void test35() {
		pi.add(p1);
		pi.add(p3);
		pi.addAll(asCollection(p0,p2,p4));
		assertEquals(5,pi.size());
	}

	public void test36() {
		pi.add(p1);
		pi.add(p2);
		pi.add(p3);
		pi.addAll(asCollection(p0,a1,d1,d0,s2,d3));
		assertEquals(6,pi.size());
	}

	public void test37() {
		pi.add(p3);
		pi.add(p1);
		pi.add(p5);
		pi.add(p2);
		pi.add(p4);
		pi.addAll(asCollection(p0,d1,a1,s2,d2,a3,p3,d3,d0,d4,a4,s5,d5));
		assertEquals(11,pi.size());
	}
	
	public void test38() {
		pis.add(s2);
		pis.add(p4);
		pis.add(a0);
		pis.addAll(asCollection(d4,p2,a0,p0));
		assertEquals(5,pis.size());
	}
	
	
	/// test4X: tests of doAll INORDER without bounds
	
	public void test40() {
		testAll(pi);
	}
	
	public void test41() {
		pi.add(s2);
		testAll(pi,s2);
	}
	
	public void test42() {
		pi.add(a3);
		pi.add(p1);
		testAll(pi,p1,a3);
	}
	
	public void test43() {
		pi.add(p3);
		pi.add(a1);
		testAll(pi,a1,p3);
	}
	
	public void test44() {
		pis.add(p3);
		pis.add(a1);
		testAll(pis,p3,a1);
	}
	
	public void test45() {
		pi.add(a3);
		pi.add(s4);
		pi.add(p5);
		testAll(pi,a3,s4,p5);
	}
	
	public void test46() {
		pis.add(a3);
		pis.add(s4);
		pis.add(p5);
		testAll(pis,p5,s4,a3);
	}
	
	public void test47() {
		pi.add(p3);
		pi.add(p1);
		pi.add(p2);
		pi.add(p5);
		pi.add(p4);
		testAll(pi,p1,p2,p3,p4,p5);
	}
	
	public void test48() {
		for (Planet p : asCollection(
				p0,p1,p2,p3,p4,p5,p6,p7,
				s0,s1,s2,s3,s4,s5,s6,s7,
				a0,a1,a2,a3,a4,a5,a6,a7)) {
			pi.add(p);
		}
		testAll(pi,
				p0,s0,a0,
				p1,s1,a1,
				p2,s2,a2,
				p3,s3,a3,
				p4,s4,a4,
				p5,s5,a5,
				p6,s6,a6,
				p7,s7,a7);
	}
	
	public void test49() {
		for (Planet p : asCollection(
				p0,s0,a0,
				p1,s1,a1,
				p2,s2,a2,
				p3,s3,a3,
				p4,s4,a4,
				p5,s5,a5,
				p6,s6,a6,
				p7,s7,a7)) {
			pis.add(p);
		}
		testAll(pis,
				p0,p1,p2,p3,p4,p5,p6,p7,
				s0,s1,s2,s3,s4,s5,s6,s7,
				a0,a1,a2,a3,a4,a5,a6,a7);
	}
	
	
	/// test5X: PRE/POST order tests
	
	public void test50() {
		testAll(pi,Traversal.PRE_ORDER);
		testAll(pi,Traversal.POST_ORDER);
	}
	
	public void test51() {
		pi.add(s2);
		testAll(pi,Traversal.PRE_ORDER,s2);
		testAll(pi,Traversal.POST_ORDER,s2);
	}
	
	public void test52() {
		pi.add(a3);
		pi.add(p1);
		testAll(pi,Traversal.PRE_ORDER,a3,p1);
		testAll(pi,Traversal.POST_ORDER,p1,a3);
	}
	
	public void test53() {
		pi.add(p3);
		pi.add(a1);
		testAll(pi,Traversal.PRE_ORDER,p3,a1);
		testAll(pi,Traversal.POST_ORDER,a1,p3);
	}
	
	public void test54() {
		pis.add(p3);
		pis.add(a1);
		testAll(pis,Traversal.PRE_ORDER,p3,a1);
		testAll(pis,Traversal.POST_ORDER,a1,p3);
	}
	
	public void test55() {
		pi.add(a3);
		pi.add(s4);
		pi.add(p5);
		testAll(pi,Traversal.PRE_ORDER,a3,s4,p5);
		testAll(pi,Traversal.POST_ORDER,p5,s4,a3);
	}
	
	public void test56() {
		pis.add(a3);
		pis.add(s4);
		pis.add(p5);
		testAll(pis,Traversal.PRE_ORDER,a3,s4,p5);
		testAll(pis,Traversal.POST_ORDER,p5,s4,a3);
	}
	
	public void test57() {
		pi.add(p3);
		pi.add(p5);
		pi.add(p4);
		pi.add(p1);
		pi.add(p2);
		testAll(pi,Traversal.PRE_ORDER,p3,p1,p2,p5,p4);
		testAll(pi,Traversal.POST_ORDER,p2,p1,p4,p5,p3);
	}
	
	public void test58() {
		for (Planet p : asCollection(
				p0,p1,p2,p3,p4,p5,p6,p7,
				s0,s1,s2,s3,s4,s5,s6,s7,
				a0,a1,a2,a3,a4,a5,a6,a7)) {
			pi.add(p);
		}
		testAll(pi,Traversal.PRE_ORDER,
				p0,
				p1, s0, a0,
				p2, s1, a1,
				p3, s2, a2,
				p4, s3, a3,
				p5, s4, a4,
				p6, s5, a5,
				p7, s6, a6,
				    s7, a7);
		testAll(pi,Traversal.POST_ORDER,
				a0, s0,
				a1, s1,
				a2, s2,
				a3, s3,
				a4, s4,
				a5, s5,
				a6, s6,
				a7, s7, p7, p6, p5, p4, p3, p2, p1, p0);
	}
	
	public void test59() {
		for (Planet p : asCollection(
				p0,s0,a0,
				p1,s1,a1,
				p2,s2,a2,
				p3,s3,a3,
				p4,s4,a4,
				p5,s5,a5,
				p6,s6,a6,
				p7,s7,a7)) {
			pis.add(p);
		}
		testAll(pis, Traversal.PRE_ORDER,
				p0,
				s0, p1, p2, p3, p4, p5, p6, p7,
				a0, s1, s2, s3, s4, s5, s6, s7,
				    a1, a2, a3, a4, a5, a6, a7);
		testAll(pis, Traversal.POST_ORDER,
				p7, p6, p5, p4, p3, p2, p1,
				s7, s6, s5, s4, s3, s2, s1,
				a7, a6, a5, a4, a3, a2, a1, a0, s0, p0);
	}

	
	/// test6X: ranges
	
	public void test60() {
		testAll(pi,0.0,1.0);
	}
	
	public void test61() {
		pi.add(p3);
		testAll(pi,1.0,2.0);
		testAll(pi,2.0,3.0,p3);
		testAll(pi,3.0,3.0,p3);
		testAll(pi,2.0,4.0,p3);
		testAll(pi,3.0,4.0,p3);
		testAll(pi,4.0,4.0);
		testAll(pi,4.0,5.0);
		testAll(pi,null,2.0);
		testAll(pi,null,3.0,p3);
		testAll(pi,null,4.0,p3);
		testAll(pi,2.0,null,p3);
		testAll(pi,3.0,null,p3);
		testAll(pi,4.0,null);
		
		// some empty ones
		testAll(pi,4.0,2.0);
		testAll(pi,3.0,2.0);
		testAll(pi,4.0,3.0);
	}
	
	public void test62() {
		pis.add(s3);
		testAll(pis,"Test","TestA");
		testAll(pis,"TestA","TestB",s3);
		testAll(pis,"TestA","TestC",s3);
		testAll(pis,"TestB","TestC",s3);
		testAll(pis,"TestC","TestD");
		testAll(pis,null,"TestA");
		testAll(pis,null,"TestB",s3);
		testAll(pis,null,"TestC",s3);
		testAll(pis,"TestA",null,s3);
		testAll(pis,"TestB",null,s3);
		testAll(pis,"TestC",null);
	}
	
	public void test63() {
		pi.add(s3);
		pi.add(a2);
		pi.add(p4);
		testAll(pi,0.0,1.0);
		testAll(pi,1.0,2.0,a2);
		testAll(pi,2.0,3.0,a2,s3);
		testAll(pi,1.0,4.0,a2,s3,p4);
		testAll(pi,2.0,4.0,a2,s3,p4);
		testAll(pi,2.0,5.0,a2,s3,p4);
		testAll(pi,3.0,4.0,s3,p4);
		testAll(pi,4.0,5.0,p4);
		testAll(pi,5.0,6.0);
		testAll(pi,null,1.0);
		testAll(pi,null,2.0,a2);
		testAll(pi,null,3.0,a2,s3);
		testAll(pi,null,4.0,a2,s3,p4);
		testAll(pi,null,5.0,a2,s3,p4);
		testAll(pi,1.0,null,a2,s3,p4);
		testAll(pi,2.0,null,a2,s3,p4);
		testAll(pi,3.0,null,s3,p4);
		testAll(pi,4.0,null,p4);
		testAll(pi,5.0,null);
	}
	
	public void test64() {
		pis.add(s3);
		pis.add(a2);
		pis.add(p4);
		testAll(pis,"T","Test");
		testAll(pis,"Test","TestA",p4);
		testAll(pis,"TestA","TestB",p4,s3);
		testAll(pis,"Test","TestC",p4,s3,a2);
		testAll(pis,"TestA","TestC",p4,s3,a2);
		testAll(pis,"TestA","TestD",p4,s3,a2);
		testAll(pis,"TestB","TestD",s3,a2);
		testAll(pis,"TestC","TestD",a2);
		testAll(pis,"TestD","TestE");
		testAll(pis,null,"Test");
		testAll(pis,null,"TestA",p4);
		testAll(pis,null,"TestB",p4,s3);
		testAll(pis,null,"TestC",p4,s3,a2);
		testAll(pis,null,"TestD",p4,s3,a2);
		testAll(pis,"Test",null,p4,s3,a2);
		testAll(pis,"TestA",null,p4,s3,a2);
		testAll(pis,"TestB",null,s3,a2);
		testAll(pis,"TestC",null,a2);
		testAll(pis,"TestD",null);
	}
	
	public void test65() {
		pi.add(p3);
		pi.add(p5);
		pi.add(p4);
		pi.add(p1);
		pi.add(p2);
		testAll(pi,0.0,0.5);
		testAll(pi,0.0,1.0,p1);
		testAll(pi,1.0,2.0,p1,p2);
		testAll(pi,2.0,3.0,p2,p3);
		testAll(pi,1.0,3.0,p1,p2,p3);
		testAll(pi,1.0,4.0,p1,p2,p3,p4);
		testAll(pi,2.0,4.0,p2,p3,p4);
		testAll(pi,2.0,5.0,p2,p3,p4,p5);
		testAll(pi,3.0,4.0,p3,p4);
		testAll(pi,4.0,5.0,p4,p5);
		testAll(pi,5.0,6.0,p5);
		testAll(pi,6.0,7.0);
		testAll(pi,null,0.0);
		testAll(pi,null,1.0,p1);
		testAll(pi,null,2.0,p1,p2);
		testAll(pi,null,3.0,p1,p2,p3);
		testAll(pi,null,4.0,p1,p2,p3,p4);
		testAll(pi,null,5.0,p1,p2,p3,p4,p5);
		testAll(pi,0.0,null,p1,p2,p3,p4,p5);
		testAll(pi,1.0,null,p1,p2,p3,p4,p5);
		testAll(pi,2.0,null,p2,p3,p4,p5);
		testAll(pi,3.0,null,p3,p4,p5);
		testAll(pi,4.0,null,p4,p5);
		testAll(pi,5.0,null,p5);
		testAll(pi,6.0,null);
	}

	public void test69() {
		for (Planet p : asCollection(
				p0,s0,a0,
				p1,s1,a1,
				p2,s2,a2,
				p3,s3,a3,
				p4,s4,a4,
				p5,s5,a5,
				p6,s6,a6,
				p7,s7,a7)) {
			pis.add(p);
		}
		testAll(pis,"Test","Test");
		testAll(pis,"Test","TestA",p0,p1,p2,p3,p4,p5,p6,p7);
		testAll(pis,"Test","TestB",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7);
		testAll(pis,"Test","TestC",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"Test","TestD",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestA","Test");
		testAll(pis,"TestA","TestA",p0,p1,p2,p3,p4,p5,p6,p7);
		testAll(pis,"TestA","TestB",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7);
		testAll(pis,"TestA","TestC",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestA","TestD",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestB","Test");
		testAll(pis,"TestB","TestA");
		testAll(pis,"TestB","TestB",s0,s1,s2,s3,s4,s5,s6,s7);
		testAll(pis,"TestB","TestC",s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestB","TestD",s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestC","Test");
		testAll(pis,"TestC","TestA");
		testAll(pis,"TestC","TestB");
		testAll(pis,"TestC","TestC",a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestC","TestD",a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestD","Test");
		testAll(pis,"TestD","TestA");
		testAll(pis,"TestD","TestB");
		testAll(pis,"TestD","TestC");
		testAll(pis,"TestD","TestD");
		
		testAll(pis,null,"Test");
		testAll(pis,null,"TestA",p0,p1,p2,p3,p4,p5,p6,p7);
		testAll(pis,null,"TestB",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7);
		testAll(pis,null,"TestC",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,null,"TestD",p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		
		testAll(pis,"Test",null,p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestA",null,p0,p1,p2,p3,p4,p5,p6,p7,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestB",null,s0,s1,s2,s3,s4,s5,s6,s7,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestC",null,a0,a1,a2,a3,a4,a5,a6,a7);
		testAll(pis,"TestD",null);
	}

}
