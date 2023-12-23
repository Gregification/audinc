package draggableNodeEditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.CompletableFuture;
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
	 * 
	 * @param output : the actual output of this function, Points will be pushed onto the queue as they're calculated 
	 * @param anchors : points that may effect where the line is drawn 
	 * @param terminals: points where the line is guaranteed to cross. the first element is the origin. 
	 * @param obstacles : regions the lines will try not to cross 
	 * 
	 * @return returns a completable future that returns the inked blocking queue to the output queue.
	 */
	public abstract CompletableFuture<LinkedBlockingQueue<Point>> genConnections(
			LinkedBlockingQueue<Point> output,
			final LineAnchor[] anchors,
			final LineAnchor[] terminals,
			final Rectangle[] obsticals
		);	
	
	//unused. too complicated and little reward. would require some sort of way to map what terminals effect what points, and some thread safe accessing of that data
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
	//public abstract <T> CompletableFuture<LinkedBlockingQueue<Point>> genConnection(LinkedBlockingQueue<Point> output, final List<TerminalPoint> terminals, final Rectangle[] obsticals, final Set<TerminalPoint> terminalsToReconnect);
}
