package util;

/**
 * A helper class designed to hold a pair of data values.
 * 
 * @author Adam Lechovský
 *
 * @param <T> The type of the first value
 * @param <U> The type of the second value
 */
public class Pair<T, U> {
	public T first;
	public U second;
	
	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}	
	
}
