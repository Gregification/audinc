package presentables.presents.draggableNodeEditor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class NodeSupplier<T> extends NodeComponent<T> implements Supplier<T>{
	protected List<NodeConsumer<T>> terminals = List.of();
	protected List<Consumer<T>> polls = List.of();
	
	public void addConsumer(NodeConsumer<T> consumer) {
		terminals.add(consumer);
		consumer.supplier = this;
	}
	
	public void removeConsumer(NodeConsumer<T> consumer) {
		if(terminals.remove(consumer))
			consumer.supplier = null;
	}
	
	public void supplyValue(T t) {		
		for(var v : polls)
			v.accept(t);
		polls.clear();
	}
	
	/** 
	 * Gets the value of the node if it is available, otherwise will call the consumer when it next becomes available.
	 * @param consumer : the consumer to be passed the value if it is not immediately available. putting NULL here is accepted. 
	 * @return returns the value if it is available. otherwise returns NULL
	 */
	public T pollValue(Consumer<T> consumer) {
		if(this.needsNewValue) {
			if(consumer != null)
				polls.add(consumer);
			
			return null;
		}
		
		return value;
	}
	
	@Override public boolean hasConnection() {
		return terminals.size() != 0;
	}
	
}
