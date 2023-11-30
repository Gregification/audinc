package draggableNodeEditor;

import java.util.List;

import javax.swing.JComponent;

/**
 * abstraction to make connecting nodes simpler. also doubles as a UI element
 * each connection may have unlimited number of consumers, but only 1 source. The source can be another consumer.
 * the source is not part of the <code>terminals</code> list 
 */
public class NodeConnection<T> extends JComponent {	
	private static final long serialVersionUID = -8049483942756570774L;
	
	/**
	 * this node can be a consumer or a supplier.
	 * this node can NOT be part of the <code>terminals</code> list; for organizational purposes.
	 */
	protected volatile NodeComponent<T> source;
	
	/**
	 * a list of all terminals connected.
	 * does NOT include the source
	 */
	protected volatile List<NodeConsumer<T>> 
		terminals = List.of();
	
	public NodeConnection() {
		
	}
	
	public NodeConnection(NodeComponent<T> source, List<NodeConsumer<T>> terminals) {
		
	}
	
	public NodeComponent<T> getSource() {
		return source;
	}

	public List<NodeConsumer<T>> getTerminals() {
		return terminals;
	}

	/**
	 * overwrites current <code>terminals</code> with the new list. removes <code>source</code> if present.
	 * @param terminalComponents
	 */
	public void setTerminalComponents(NodeConsumer<T>... terminalComponents) {
		this.terminals = List.of(terminalComponents);
		if(source != null)
			terminals.remove(source);
	}

	public NodeSupplier<T> getSupplier() {
		if(this.source == null) return null;
		
		return source.getSupplier();
	}
	
	public int getNumberOfConnections() {
		return (terminals != null ? terminals.size() : 0) + (source != null ? 1 : 0);
	}
}
