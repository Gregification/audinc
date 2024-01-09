package presentables;

import java.util.Arrays;
import java.util.function.Consumer;

/*
 * this was a stupid idea. i keep it here as a reminder. there're many better things in default java libraries
 * 
 * a threadded buffer of objects
 * made as a way to handle rapid gui updates since i cant figure out how to use the java.nio.Buffer class with anyhting but primiitves
 */

public class custom_buffer<T> {
	private T[] buffer;
	private int 
		idx = 0;
	private final Consumer<T[]> flushEvent;
	
	public custom_buffer(int size, Consumer<T[]> flushEvent) {		
		@SuppressWarnings("unchecked")
		final T[] buffer = (T[])new Object[size];
		
		 this.flushEvent = flushEvent;
	}
	
	public void flush() {
		flushEvent.accept(Arrays.copyOf(buffer, idx));
		
		while(idx > 0) {
			buffer[idx] = null;
			idx--;
		}
	}
	
	public void clear() {
		for(T v : buffer)
			v = null;
	}
	
	public void add(T o) {
		buffer[idx] = o;
			
		if(++idx == buffer.length)
			flush();
	}
}
