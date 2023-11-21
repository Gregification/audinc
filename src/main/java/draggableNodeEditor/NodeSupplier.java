package draggableNodeEditor;

import java.util.function.Supplier;

public abstract class NodeSupplier<T> extends NodeComponent<T> implements Supplier<T>{
	private static final long serialVersionUID = 1L;

	public NodeSupplier(String name, T value) {
		super(name, value);
	}
	
	@Override public NodeSupplier<T> getSupplier() {
		return this;
	}
	
	@Override public T get() {
		return getValue();
	}
}
