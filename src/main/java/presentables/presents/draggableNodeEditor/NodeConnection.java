package presentables.presents.draggableNodeEditor;

import java.util.List;
import java.util.function.Consumer;

public class NodeConnection<T> implements Consumer<T>{
	protected List<NodeComponent> terminals = List.of();
	
	public NodeConnection() {
		
	}
	
	public void addTerminal(NodeComponent nodeComp) {
		terminals.add(nodeComp);
	}

	@Override public void accept(T t) {
		
	}
}
