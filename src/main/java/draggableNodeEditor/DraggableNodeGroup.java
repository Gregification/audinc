package draggableNodeEditor;

import java.util.Set;

import draggableNodeEditor.nodes.NConstant;
import draggableNodeEditor.serialPoke.NNoticeUpdater;
import draggableNodeEditor.serialPoke.NepSPDataListener;
import presentables.presents.serialPoke.SerialPokeCommConnection;

public enum DraggableNodeGroup {
	
	//do not duplicate classes. searches through here only expect 1 instance of each
	
		GENERAL(
				Void.class,
				Set.of(
					NConstant.class,
					AnchorPoint.class
				)
			),
		SERIAL_PIKE(
				SerialPokeCommConnection.class,
				Set.of(
					NNoticeUpdater.class,
					NepSPDataListener.class
				)
			)
	;
	
	public volatile Class<?> expectedContextType;
	public volatile Set<Class<? extends DraggableNode<?>>> allowedNodes;
	
	private DraggableNodeGroup(Class<?> expectedContext, Set<Class<? extends DraggableNode<?>>> allowedNodes) {
		this.allowedNodes = allowedNodes;
		this.expectedContextType = expectedContext;
	}
}
