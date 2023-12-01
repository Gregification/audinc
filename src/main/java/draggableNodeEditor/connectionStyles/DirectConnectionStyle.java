package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.NodeComponent;
import draggableNodeEditor.NodeConnection;
import draggableNodeEditor.TerminalPoint;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	protected boolean objsticalNavication = false; //TODO
	
	@SuppressWarnings("unchecked")
	@Override public <T> void genConnections(NodeConnection<T> nodeConnection, Rectangle[] obstacles) {
		var arr = nodeConnection.toArray(TerminalPoint[]::new);
		
		for(int i = 0; i < arr.length - 1; i++) {
			arr[i].pathToNext = new Point[] {
					arr[i+1].getPoint()
				};
		}
		
		arr[arr.length-1].pathToNext = new Point[0];
	}

	@Override public <T> void genConnection(NodeConnection<T> nodeConnection, Rectangle[] obstacles, Set<TerminalPoint<T>> terminalsToReconnect) {
		var arr = nodeConnection.toArray(TerminalPoint[]::new);//apparently includign a generic in the array type dosent fly
	}
}
