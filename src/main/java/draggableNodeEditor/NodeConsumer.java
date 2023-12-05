package draggableNodeEditor;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.swing.JLabel;

public class NodeConsumer<T> extends NodeComponent<T> implements Consumer<T> {
	private static final long serialVersionUID = 1L;
	
	protected JLabel nameLabel;
	
	/**
	 * BiFunction< old-value , new-value, final-value> 
	 * defaults to 
	 * <br><code>(old_v , new_v) -> new_v</code>
	 */
	protected BiFunction<T, T, T> onAccept;
	
	public NodeConsumer(Class<T> type, String name, T value) {
		this(type, name, value, null);
	}

	public NodeConsumer(Class<T> type, String name, T value, BiFunction<T, T, T> onAccept) {
		super(type, name, value);
		this.setOnAccept(onAccept);;
	}
	
	protected NodeSupplier<T> supplier;
	
	@Override public void accept(T t) {
		this.setValue(onAccept.apply(value, t));
	}
	
	@Override public NodeSupplier<T> getSupplier() {
		return this.supplier;
	}
	
	@Override public boolean hasConnection() {
		return this.supplier != null;
	}
	
	@Override public void setName(String newName) {
		super.setName(newName);
		
		if(name.isBlank()) {
			if(nameLabel != null)
				this.remove(nameLabel);
			return;
		}
		else if(nameLabel == null) {
			nameLabel = new JLabel();
			nameLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			this.add(nameLabel);
		}
		
		nameLabel.setText(name);
	}
	
	public void setOnAccept(BiFunction<T, T, T> onAccept){
		if(onAccept == null)
			this.onAccept = (o,n) -> n;		//default
		else
			this.onAccept = onAccept;
	}
}
