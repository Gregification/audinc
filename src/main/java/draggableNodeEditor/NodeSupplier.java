package draggableNodeEditor;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public non-sealed class NodeSupplier<T> extends NodeComponent<T> implements Supplier<CompletableFuture<T>>{
	private static final long serialVersionUID = 1L;
	private ArrayList<NodeConsumer<T>> consumers = new ArrayList<>();
	
	public NodeSupplier(Class<T> type, String name, T value, CompletableFuture<T> valueFuture) {
		super(type, name, value);
		this.valueFuture = valueFuture;
	}
	public NodeSupplier(Class<T> type, String name, T value) {
		super(type, name, value);
		valueFuture = CompletableFuture.completedFuture(value);
	}
	
	@Override public NodeSupplier<T> getSupplier() {
		return this;
	}
	
	@Override public CompletableFuture<T> get() {
		return getValue();
	}

	@Override public void considerComponent(NodeComponent<T> comp) {
		if(comp instanceof NodeConsumer<T> cosm) {
			if(!consumers.contains(cosm)) consumers.add(cosm);
		}
	}

	@Override public void unconsiderComponent(NodeComponent<T> comp) {
		if(comp instanceof NodeConsumer<T> cosm) {
			consumers.remove(cosm);
		}
	}

	@Override public CompletableFuture<T> getValue() {
		return this.valueFuture;
	}

	@Override public void setValue(T value) {
		if(valueFuture.isDone()) {
			valueFuture = CompletableFuture.completedFuture(value);
		} else 
			valueFuture.complete(value);
	}
}
