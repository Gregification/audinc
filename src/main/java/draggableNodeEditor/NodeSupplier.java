package draggableNodeEditor;

import java.util.function.Supplier;

public class NodeSupplier<T> extends NodeComponent<T> implements Supplier<T>{
	private static final long serialVersionUID = 1L;
	
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
