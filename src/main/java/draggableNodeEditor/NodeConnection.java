package draggableNodeEditor;

import java.util.List;

/**
 * abstraction to make connecting nodes simpler. also doubles as a UI element
 * each connection can have any number of terminals but only 1 source. A source dosen't have to be a supplier. 
 */
public class NodeConnection<T>{	
	protected volatile NodeComponent<T> source;
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

	public void setTerminalComponents(List<NodeConsumer<T>> terminalComponents) {
		this.terminals = terminalComponents;
	}

	public NodeSupplier<T> getSupplier() {
		if(this.source == null) return null;
		
		return source.getSupplier();
	}
}
