package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.NodeConnection;
import draggableNodeEditor.TerminalPoint;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	protected boolean objsticalNavication = false; //TODO
	
	@Override public <T> void genConnections(NodeConnection<T> nodeConnection, Rectangle[] obstacles) {
		
	}

	@Override public <T> void genConnection(NodeConnection<T> nodeConnection, Rectangle[] obstacles, Set<TerminalPoint> terminalsToReconnect) {
		
	}
}
