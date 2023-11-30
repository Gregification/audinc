package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.NodeConnection;

/**
 * a straight line between each terminal node.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	protected boolean objsticalNavication = false; //TODO

	@Override public void genConnections(NodeConnection nodeConnection, List<Rectangle> obstacles) {
		List<List<Point>> nodes = nodeConnection.getPoints();
		
		for(int i = 0, numConnections = nodes.size() - 1; i < numConnections; i++) {
			List<Point> points = nodes.get(i);
			
			points.subList(1, points.size()).clear(); //remove all but the terminal node(the first one)
		}
	}

	@Override public void genConnection(NodeConnection nodeConnection, List<Rectangle> obstacles, int[] terminalsToReconnect) {
		List<List<Point>> nodes = nodeConnection.getPoints();
		
		for(var i : terminalsToReconnect) {
			var connection = nodes.get(i);
			connection.subList(1, connection.size()).clear();
		}
	}
}
