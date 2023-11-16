package presentables.presents.draggableNodeEditor;

import java.util.function.Consumer;

public abstract class NodeConsumer<T> extends NodeComponent<T> implements Consumer<T> {
	protected NodeSupplier<T> supplier;
	
	@Override public void accept(T t) {
		this.value = t;
	}
	
	public NodeSupplier<T> getSupplier() {
		return this.supplier;
	}
	
	public void setSupplier(NodeSupplier<T> supplier) {
		dropSupplier();
		if(supplier == null) return;
		
		supplier.addConsumer(this);
	}
	
	public void dropSupplier() {
		if(supplier == null) return;
		
		supplier.removeConsumer(this);
		supplier = null;
	}
	
	@Override public boolean hasConnection() {
		return supplier != null;
	}
}
