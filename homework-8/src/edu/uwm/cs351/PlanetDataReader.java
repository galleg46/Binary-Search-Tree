package edu.uwm.cs351;
/**
 * reads planetary data from the OEC (open exoplanet catalogue)
	 * creates a PlanetIndex with ALL planets in the OEC
	 * https://github.com/OpenExoplanetCatalogue/open_exoplanet_catalogue
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to read exo-planet data in CSV
 * and create planet indices for different fields.
 */
public class PlanetDataReader{
	
	private final PlanetIndex<String> parentIndex;
	
	/**
	 * Read exoplanet data in CSV from the given source.
	 * @param r reader to use must not be null, must follow subset CSV format
	 */
	public PlanetDataReader(Reader r) {
		final int NAME_INDEX = Planet.getFieldIndex("identifier");
		parentIndex = new PlanetIndex<>(p -> (String)p.getField(NAME_INDEX));
		try(BufferedReader in = new BufferedReader(r)) {
			String str;
			int i = 0;
			while ((str = in.readLine()) != null) {
				if(!(str.charAt(0) == '#')) {
					++i;
					String[] stringData = breakCSV(str);
					Object[] data = new Object[25];
					int j = 0;
					for(String s: stringData) {
						if(j == 1) {//integer field
							data[j] = Integer.parseInt(s);
						}
						else if((j >= 13 && j <= 17)|| j == 0 || j == 24) {//string field        
							data[j] = s;
						}
						else { //double field
							if(s.equals("")) {
								data[j] = null;
							}
							else {
								data[j] = Double.parseDouble(s);
							}
						}
						++j;
					}
					Planet p = new Planet(data);
					parentIndex.add(p);
					if(i%200 == 0) {
						System.out.println("Read " + i + " planets...");
					}
				}
			}
			System.out.println("Read " + i + " planets in total");
		}
		catch (IOException e) {
			System.out.println("File Error: "+ e);
		}
	}
	
	/**
	 * Break up a CSV line into segments.
	 * It is slightly smarter than split because it can handle
	 * a field fully quoted with double quotes.
	 * @param s string to break apart
	 * @return array of strings from the CSV line
	 */
	public static String[] breakCSV(String s) {
		List<String> contents = new ArrayList<>();
		for (int i=0; i < s.length(); ) {
			int comma = s.indexOf(',',i);
			int quote = s.indexOf('"',i);
			if (comma > 0 && (comma < quote || quote == -1)) {
				contents.add(s.substring(i, comma));
				i = comma+1;
			} else if (quote > -1) {
				if (quote != i) throw new UnsupportedOperationException("cannot handle this line: " + s);
				quote = s.indexOf('"',quote+1);
				if (quote == -1) throw new UnsupportedOperationException("cannot handle this line: " + s);
				contents.add(s.substring(i+1,quote));
				i = quote+1;
				if (i < s.length() && s.charAt(i) == ',') ++i;
			} else {
				contents.add(s.substring(i));
				i = s.length();
			}
		}
		return contents.toArray(new String[contents.size()]);
	}
	
	/**
	 * Get planets indexed by an integer field.
	 * @param fieldIndex must be in range of [0,Planet.numFields())
	 * @return a (fresh) PlanetIndex<Integer> sorted by given field
	 * @throws IllegalArgumentException if the index is not an integer field.
	 */
	public PlanetIndex<Integer> createIntegerPlanetIndex(int fieldIndex) {
		if (fieldIndex < 0 || fieldIndex >= Planet.numFields() || 
				Integer.class != Planet.getFieldType(fieldIndex)) {
			throw new IllegalArgumentException("Field " + fieldIndex + " is not an integer field.");
		}
		PlanetIndex<Integer> ret = new PlanetIndex<>((p) -> (Integer)p.getField(fieldIndex));
		ret.addAll(parentIndex);
		return ret;
	}
	
	/** Get planets indexed by a double field.
	 * @param fieldIndex must be in range of [0,Planet.numFields())
	 * @return a PlanetIndex<Double> sorted by given field
	 * @throws IllegalArgumentException if the index is not a double field.
	 */
	public PlanetIndex<Double> createDoublePlanetIndex(int fieldIndex) {
		if (fieldIndex < 0 || fieldIndex >= Planet.numFields() || 
				Double.class != Planet.getFieldType(fieldIndex)) {
			throw new IllegalArgumentException("Field " + fieldIndex + " is not a double field.");
		}
		PlanetIndex<Double> ret = new PlanetIndex<>((p) -> (Double)p.getField(fieldIndex));
		ret.addAll(parentIndex);
		return ret;
	}
	
	/** Get planets indexed by a String field.
	 * @param fieldIndex must be in range of [0,Planet.numFields())
	 * @return a PlanetIndex<String> sorted by given field
	 * @throws IllegalArgumentException if the index is not a double field.
	 */
	public PlanetIndex<String> createStringPlanetIndex(int fieldIndex) {
		if (fieldIndex < 0 || fieldIndex >= Planet.numFields() || 
				String.class != Planet.getFieldType(fieldIndex)) {
			throw new IllegalArgumentException("Field " + fieldIndex + " is not a string field.");
		}
		PlanetIndex<String> ret = new PlanetIndex<>((p) -> (String)p.getField(fieldIndex));
		ret.addAll(parentIndex);
		return ret;
	}

}
