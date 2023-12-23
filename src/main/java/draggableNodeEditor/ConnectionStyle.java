package draggableNodeEditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * handles the drawing of a line 
 */
public interface ConnectionStyle {
	/**
	 * generates the path, point by point, between all terminal nodes. 
	 * the final terminal node will not get a path made for it.
	 * 
	 * assume to be processing intensive
	 * @param <T>
	 * 
	 * @param nodeConnection : source of connection information
	 * @param obstacles : regions the lines are not to cross
	 */
	public abstract <T> Future<Boolean> genConnections(LinkedBlockingQueue<Point> output, final List<TerminalPoint> terminals, final Rectangle[] obsticals);
	
	/**
	 * redraws the connections for only the given nodes.
	 * 
	 * a better option than redrawing all connections, however depending on the ConnectionStyle this may not be much better.
	 * 
	 * assume to be processing intensive
	 *  
	 * @param nodeConnection : source of connection information
	 * @param obstacles : regions the lines are not to cross 
	 * @param terminalsToReconnect : specific terminals to recalculate
	 */
	public abstract <T> Future<Boolean> genConnection(LinkedBlockingQueue<Point> output, final List<TerminalPoint> terminals, final Rectangle[] obsticals, final Set<TerminalPoint> terminalsToReconnect);
}
