package draggableNodeEditor;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * supplies values to the network
 * see "PC_ ..." variables for bound property changes
 * @param <T>
 */
public non-sealed class NodeSupplier<T> extends NodeComponent<T> implements Supplier<T>, Serializable {
	private static final long serialVersionUID = 1L;
	
	/**  property fired when the value is changed 
	 * @new-property-value new value
	 * @old-property-value old value
	 */
	public static final String PC_ValueChanged = "PC_ValueChanged";
	 
	public NodeSupplier(Class<T> type, String name, T value) {
		super(type, name, value);
	}
	
	@Override public T get() {
		return getValue();
	}

	@Override public void setValue(T value) {
		firePropertyChange(PC_ValueChanged, this.value, value);
		super.setValue(value);
	}
}
