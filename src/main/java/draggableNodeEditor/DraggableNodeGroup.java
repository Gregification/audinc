package draggableNodeEditor;

import java.util.Set;

import draggableNodeEditor.serialPoke.NepSPDataListener;

public enum DraggableNodeGroup {
		GENERAL(Set.of(
				NConstant.class
			)),
		SERIAL_PIKE(Set.of(
				NepSPDataListener.class
			))
	;
	
	public Set<Class<? extends DraggableNode>> allowedNodes;
	
	private DraggableNodeGroup(Set<Class<? extends DraggableNode>> allowedNodes) {
		this.allowedNodes = allowedNodes;
	}
}
