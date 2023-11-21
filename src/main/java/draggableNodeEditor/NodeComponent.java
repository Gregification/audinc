package draggableNodeEditor;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * a editable value for the
 * @param <T>
 */
public abstract class NodeComponent<T extends Object> extends JComponent {
	private static final long serialVersionUID = 1L;
	
	//node stuff
	private String name;
//	private JLabel nameLabel;
	protected volatile T value = null;
	protected volatile List<NodeConnection<T>> connections = List.of();
	
	//meta-ish stuff
	protected volatile boolean 
		needsRedrawn	= true,		
		needsNewValue 	= true;
	public NodeComponentImportance importance;//UI stuff

	public NodeComponent(String name, T value) {
		this.setName(name);
		this.value = value;

		this.setLayout(new FlowLayout());
		this.setToolTipText(value.getClass() + "");
	}
	
	public abstract NodeSupplier<T> getSupplier();
	
	public boolean hasConnection() {
		return connections.size() != 0;
	}
	
	public Class getType() {
		return value.getClass();
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public List<NodeConnection<T>> getConnections() {
		return connections;
	}

	public void setConnections(List<NodeConnection<T>> connections) {
		this.connections = connections;
	}

	public void addConnections(List<NodeConnection<T>> connections) {
		this.connections.addAll(connections);
	}
	
	public boolean NeedsRedrawn() {
		return needsRedrawn;
	}

	public boolean NeedsNewValue() {
		return needsNewValue;
	}
	
	public String getName() {
		return this.name;
	}

	//the layout manager needs to be fixed before anything more can be done here
	public void setName(String newName) {
		this.name = (newName == null) ? "" : newName;
		
		this.setToolTipText(name);
		
//		if(name.isBlank()) {
//			if(nameLabel != null) {
//				this.remove(nameLabel);
//				nameLabel = null;
//			}
//		}
//		else {
//			if(nameLabel == null) {
//				nameLabel = new JLabel();
//				this.add(nameLabel, 0);
//			}
//			
//			nameLabel.setText(name);
//		}
	}
	
}