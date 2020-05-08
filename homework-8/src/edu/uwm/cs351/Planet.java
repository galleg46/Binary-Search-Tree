package edu.uwm.cs351;

import java.util.Arrays;
/**
 *
 */
public class Planet implements Comparable<Planet> {
	//  0: Primary identifier of planet
	//  1: Binary flag [0=no known stellar binary companion; 1=P-type binary (circumbinary); 2=S-type binary; 3=orphan planet (no star)]
	//  2: Planetary mass [Jupiter masses]
	//  3: Radius [Jupiter radii]
	//  4: Period [days]
	//  5: Semi-major axis [Astronomical Units]
	//  6: Eccentricity
	//  7: Periastron [degree]
	//  8: Longitude [degree]
	//  9: Ascending node [degree]
	// 10: Inclination [degree]
	// 11: Surface or equilibrium temperature [K]
	// 12: Age [Gyr]
	// 13: Discovery method
	// 14: Discovery year [yyyy]
	// 15: Last updated [yy/mm/dd]
	// 16: Right ascension [hh mm ss]
	// 17: Declination [+/-dd mm ss]
	// 18: Distance from Sun [parsec]
	// 19: Host star mass [Solar masses]
	// 20: Host star radius [Solar radii]
	// 21: Host star metallicity [log relative to solar]
	// 22: Host star temperature [K]
	// 23: Host star age [Gyr]
	// 24: A list of lists the planet is on
	private Object[] fields;

	private static final Class<?>[] TYPES = {//holds the Class values of each field, for type checking
			String.class, Integer.class, Double.class, Double.class, Double.class,
			Double.class, Double.class, Double.class, Double.class, Double.class,
			Double.class, Double.class, Double.class, String.class, String.class,
			String.class, String.class, String.class, Double.class, Double.class,
			Double.class, Double.class, Double.class, Double.class, String.class
	};
	private static final String[] TYPE_NAMES = {//holds the names of each field, for convenience
			"identifier", "binary_flag", "mass", "radius", "period", 
			"semi_major_axis", "eccentricity", "periastron", "longitude", "ascending_node",
			"inclination", "temperature", "age", "discovery_method", "discovery_year", 
			"year_updated", "right_ascension", "declination", "distance", "host_star_mass", 
			"host_star_radius", "metallicity", "host_star_temperature", "host_star_age", "planetary_lists", 
	};

	/**
	 * Constructor that sets the fields given a list of input fields
	 * throws IllegalArgumentException if an input field is not of correct type
	 * @param input - array of planet values, values may be null
	 */
	public Planet(Object[] input) {
		fields = new Object[25];
		for(int i=0; i<fields.length; ++i) {
			if(input[i] == null || TYPES[i].isInstance(input[i])) //checking null first will short circuit
				fields[i] = input[i];	
			else 
				throw new IllegalArgumentException("input field " + input[i] + " (a " + input[i].getClass() + " ) is not of the same class as fields[i] - " + TYPES[i]);
		}
	}

	/**
	 * A constructor that sets the ONLY mass and name
	 * This is used by tests that need planet objects to test data structures.
	 * @param mass mass of planet in earth-masses.
	 */
	public Planet(String name, double mass) {
		Object[] data = new Object[25];
		final int MASS_INDEX = getFieldIndex("mass");
		final int NAME_INDEX = getFieldIndex("identifier");
		Arrays.setAll( (Object[]) data, index -> {
			if(index == NAME_INDEX)   {++index; return name;}
			else if(index == MASS_INDEX) {++index; return mass;}
			else {++index; return null;}
		});
		this.fields = data;
	}

	@Override
	public int compareTo(Planet o) {
		// This should return 0 only for two planets that are equal
		return ( this.dataString().compareTo(o.dataString()));
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Planet)) return false;
		return compareTo((Planet)o) == 0;
	}

	@Override
	public int hashCode() {
		// not used in this assignment, but still need a legal definition
		return fields[0].hashCode();
	}

	@Override
	public String toString() {
		return "Planet(" + fields[0] + ":" + fields[2] + ")";
	}

	/**
	 * Creates and returns a string representation of 
	 * every data field of this planet, along with the name
	 * of each data field
	 * @return String representation of all data
	 */
	public String dataString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<fields.length; ++i) {
			if(!(fields[i] == null)) {
				sb.append(TYPE_NAMES[i] + " = " + fields[i] + " ");
			}
		}
		return sb.toString();
	}

	/** 
	 * @param index
	 * @return the field at given index
	 */
	public Object getField(int index) {
		return fields[index];
	}

	/**
	 * Return the number of fields for planets.
	 * @return number of information fields for a planet.
	 */
	public static int numFields() {
		return TYPE_NAMES.length;
	}
	
	/** 
	 * @param index
	 * @return the name of the field at the given index
	 */
	public static Object getFieldName(int index) {
		return TYPE_NAMES[index];
	}
	
	/** 
	 * @param index
	 * @return the name of the field at the given index
	 */
	public static Class<?> getFieldType(int index) {
		return TYPES[index];
	}

	/** 
	 * @param String name
	 * @return the index of the field with the given name, or -1 if it doesn't exist
	 */
	public static int getFieldIndex(String field_name) {
		for(int i=0; i< TYPES.length; ++i) {
			if(TYPE_NAMES[i].equals(field_name)) {
				return i;
			}
		}
		return -1;
	}

}
