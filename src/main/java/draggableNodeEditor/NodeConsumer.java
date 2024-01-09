package draggableNodeEditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.function.Consumer;

import javax.swing.JLabel;

public non-sealed abstract class NodeConsumer<T> extends NodeComponent<T> implements Consumer<PropertyChangeEvent>, Serializable {
	private static final long serialVersionUID = 1L;
	
	protected JLabel nameLabel;
	
	protected PropertyChangeListener listener = pce -> {
			if(NodeSupplier.PC_ValueChanged.equals(pce.getPropertyName())) {
				accept(pce);
				
				try {
			        T val = type.cast(pce.getNewValue());
			        setValue(val);
			    } catch(ClassCastException e) {
			    	System.out.println("node consumer > listener triggered; failed cast to T:" + type + " from " + pce.getNewValue().getClass());
			    }
			}
		};
	
	public NodeConsumer(Class<T> type, String name, T value) {
		super(type, name, value);
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
}
