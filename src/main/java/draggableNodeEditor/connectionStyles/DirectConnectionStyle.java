package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import draggableNodeEditor.ConnectionStyle;
import draggableNodeEditor.NodeConnection;
import draggableNodeEditor.TerminalPoint;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	protected boolean objsticalNavication = false; //TODO

	@Override
	public <T> Future<Boolean> genConnections(NodeConnection<T> nodeConnection, LinkedBlockingQueue<Point> output,
			Rectangle[] obsticals) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Future<Boolean> genConnection(NodeConnection<T> nodeConnection, LinkedBlockingQueue<Point> output,
			Rectangle[] obsticals, Set<TerminalPoint> terminalsToReconnect) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
