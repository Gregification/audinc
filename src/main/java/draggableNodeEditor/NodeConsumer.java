package draggableNodeEditor;

import java.util.function.Consumer;

import javax.swing.JLabel;

public class NodeConsumer<T> extends NodeComponent<T> implements Consumer<T> {
	public NodeConsumer(String name, T value) {
		super(name, value);
		this.add(new JLabel(name));
	}

	protected NodeSupplier<T> supplier;
	
	@Override public void accept(T t) {
		this.value = t;
	}
	
	@Override public NodeSupplier<T> getSupplier() {
		return this.supplier;
	}
	
	@Override public boolean hasConnection() {
		return supplier != null;
	}
}
