import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uwm.cs351.Planet;
import edu.uwm.cs351.PlanetIndex;
import edu.uwm.cs351.PlanetIndex.Traversal;
import junit.framework.TestCase;


public class TestEfficiency extends TestCase {

	private PlanetIndex<Double> index;
	
	private Random random;
	
	private static final int POWER = 20; // 1/2 million entries
	private static final int TESTS = 100_000;
	
	protected static Planet n(int i) {
		return new Planet("Planet " + i, i);
	}
	
	private static final int MASS_INDEX = Planet.getFieldIndex("mass");
	private static final int NAME_INDEX = Planet.getFieldIndex("identifier");

	protected static PlanetIndex<Double> makeMassIndex() {
		return new PlanetIndex<>((p) -> (Double)p.getField(MASS_INDEX)); 
	}
	
	protected static PlanetIndex<String> makeNameIndex() {
		return new PlanetIndex<>((p) -> (String)p.getField(NAME_INDEX));		
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		random = new Random();
		try {
			assert index.size() == TESTS : "cannot run test with assertions enabled";
		} catch (NullPointerException ex) {
			throw new IllegalStateException("Cannot run test with assertions enabled");
		}
		index = makeMassIndex();
		int max = (1 << (POWER)); // 2^(POWER) = 2 million
		for (int power = POWER; power > 1; --power) {
			int incr = 1 << power;
			for (int i=1 << (power-1); i < max; i += incr) {
				index.add(n(i));
			}
		}
	}
		
	@Override
	protected void tearDown() throws Exception {
		index = null;
		super.tearDown();
	}
	
	<K extends Comparable<K>> 
	void testAll(PlanetIndex<K> pi, K lo, K hi, Traversal t, Planet... expected) {
		List<Planet> results = new ArrayList<>();
		pi.doAll((p) -> {
			int n = expected.length;
			int i = results.size();
			assertTrue("Too many planets returned: " + p, i < n);
			assertEquals(expected[i],p);
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


	public void testSize() {
		for (int i=0; i < TESTS; ++i) {
			assertEquals((1<<(POWER-1))-1,index.size());
		}
	}

	public void testRange() {
		for (int i=0; i < TESTS; ++i) {
			int r = random.nextInt(TESTS);
			int in = r*4+2;
			int out = r*2+1;
			testAll(index,in-0.5,in+0.5,n(r*4+2));
			testAll(index,out-0.5,out+0.5);
		}
	}
	
	public void testCopy1() {
		PlanetIndex<Double> index1 = makeMassIndex();
		index1.addAll(index);
		assertEquals((1<<(POWER-1))-1,index1.size());
	}
	
	public void testCopy2() {
		PlanetIndex<String> index1 = makeNameIndex();
		index1.addAll(index);
		assertEquals((1<<(POWER-1))-1,index1.size());
	}
}
