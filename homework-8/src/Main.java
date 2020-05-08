import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import edu.uwm.cs351.Planet;
import edu.uwm.cs351.PlanetDataReader;
import edu.uwm.cs351.PlanetIndex;
import edu.uwm.cs351.PlanetIndex.Traversal;
/**
 * A demo of the use of PlanetIndex
 * Feel free to modify this class and do what you'd like with the data from the OEC
 * https://github.com/OpenExoplanetCatalogue/open_exoplanet_catalogue
 */
public class Main {	
	
	private static final String planetURL = "https://raw.githubusercontent.com/OpenExoplanetCatalogue/oec_tables/master/comma_separated/open_exoplanet_catalogue.txt";

	public static void main(String[] args) throws IOException {
		Reader r = new InputStreamReader(new URL(planetURL).openStream());
		PlanetDataReader reader = new PlanetDataReader(r);
		PlanetIndex<Integer> binary_flag = (PlanetIndex<Integer>) reader.createIntegerPlanetIndex(1);
		PlanetIndex<Double> orbital_radius = (PlanetIndex<Double>) reader.createDoublePlanetIndex(5);
		PlanetIndex<Double> mass = (PlanetIndex<Double>) reader.createDoublePlanetIndex(2);
		PlanetIndex<Double> temperature_and_radius;

		System.out.println();
		System.out.println("These planets are all orphan planets, meaning they have no star");
		System.out.println("There are few in the catalog because they can only be found through imaging,  ");
		System.out.println("which is more difficult and less reliable than other methods:");
		System.out.println("-----------------------------------------------------------------------------------");
		printAll(binary_flag, 3,3,Traversal.PRE_ORDER);

		System.out.println();
		System.out.println("The earth is approximately .00315 Jupiter masses.");
		System.out.println("This planet directly below has some remarkable simillarities to Earth!");
		System.out.println("It's sun is even only 1.04 solar masses (ours happens to be 1.0 solar masses)");
		System.out.println("However, with a radius 1/10th that of the earth, it's a bit too close to its star.");
		System.out.println("-----------------------------------------------------------------------------------");
		printAll(mass, 0.003, 0.004 ,Traversal.PRE_ORDER);

		System.out.println();
		System.out.println("Now let's try something more interesting with the PlanetIndex:");
		System.out.println("Creating an empty PlanetIndex, then filling it with values");
		System.out.println("in range of habitable zone characteristics and finally printing");
		System.out.println("only those in a certain temperature range can");
		System.out.println("give a list of planets that have some decent chances to be habitable");
		System.out.println("-----------------------------------------------------------------------------------");
		final int TEMP_INDEX = Planet.getFieldIndex("host_star_temperature");
		temperature_and_radius = new PlanetIndex<Double>((p) -> (Double)p.getField(TEMP_INDEX));
		//fill temp_and_radius with only 'habitable' orbital radii
		orbital_radius.doAll((p) -> {
			temperature_and_radius.add(p);
		}, 0.7, 1.2, Traversal.PRE_ORDER);
		printAll(temperature_and_radius, 5500.0, 6500.0 ,Traversal.PRE_ORDER);
		System.out.println("(There are MANY more variables that go into more realistically determining a 'habitable zone' if you are interested, this is an interesting read on the factors at play)");
		System.out.println("https://www.astro.umd.edu/~miller/teaching/astr380f09/lecture14.pdf");

		System.out.println();
		System.out.println("Finally, a list of some of the biggest exoplanets (keep in mind these are jupiter masses!).");
		System.out.println("And more are being found every week");
		System.out.println("https://exoplanetarchive.ipac.caltech.edu/docs/exonews_archive.html");
		System.out.println("-----------------------------------------------------------------------------------");
		printAll(mass, 20.0 , null ,Traversal.IN_ORDER);
	}

	static <K extends Comparable<K>> 
	void printAll(PlanetIndex<K> pi, K lo, K hi, Traversal t) {
		pi.doAll((p) -> {
			System.out.println(p.dataString());
		}, lo, hi, t);
	}

}
