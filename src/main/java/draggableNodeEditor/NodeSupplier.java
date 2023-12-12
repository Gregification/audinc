package draggableNodeEditor;

import java.util.ArrayList;
import java.util.function.Supplier;

public class NodeSupplier<T> extends NodeComponent<T> implements Supplier<T>{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<NodeConnection<T>> connections_or_something;
	
	public NodeSupplier(Class<T> type, String name, T value) {
		super(type, name, value);
	}
	
	@Override public NodeSupplier<T> getSupplier() {
		return this;
	}
	
	@Override public T get() {
		return getValue();
	}
}
