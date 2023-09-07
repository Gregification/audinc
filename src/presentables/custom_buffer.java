package presentables;

import java.util.Arrays;

/*
 * a threadded buffer of objects
 * made as a way to handle rapid gui updates since i cant figure out how to use the java.nio.Buffer class with anyhting but primiitves
 */

public class custom_buffer<T> {
	private T[] buffer;
	private int 
		idx = 0;
	private final custom_function<T[]> flushEvent;
	
	public custom_buffer(int size, custom_function<T[]> flushEvent) {		
		@SuppressWarnings("unchecked")
		final T[] buffer = (T[])new Object[size];
		
		 this.flushEvent = flushEvent;
	}
	
	public void flush() {
		flushEvent.doTheThing(Arrays.copyOf(buffer, idx));
		
		while(idx > 0) {
			buffer[idx] = null;
			idx--;
		}
	}
	
	public void clear() {
		for(var v : buffer)
			v = null;
	}
	
	public void add(T o) {
		buffer[idx] = o;
			
		if(++idx == buffer.length)
			flush();
	}
}
