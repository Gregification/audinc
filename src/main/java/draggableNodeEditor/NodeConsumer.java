package draggableNodeEditor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.swing.JLabel;

public non-sealed class NodeConsumer<T> extends NodeComponent<T> implements Consumer<T> {
	private static final long serialVersionUID = 1L;
	
	protected JLabel nameLabel;
	
	protected NodeSupplier<T> supplier;
	
	public NodeConsumer(Class<T> type, String name, T value) {
		super(type, name, value);
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

	@Override public CompletableFuture<T> getValue() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override public void setValue(T value) {
		//TODO Auto-generated method stub
	}

	@Override public void accept(T value) { this.setValue(value); }

	@Override
	public void considerComponent(NodeComponent<T> comp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unconsiderComponent(NodeComponent<T> comp) {
		// TODO Auto-generated method stub
		
	}
}
