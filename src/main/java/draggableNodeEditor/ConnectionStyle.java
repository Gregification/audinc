package draggableNodeEditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

public interface ConnectionStyle {
	/**
	 * generates the path, point by point, between all terminal nodes. 
	 * the final terminal node will not get a path made for it.
	 * 
	 * assume to be processing intensive, use sparsely
	 * 
	 * @param nodeConnection : source of connection information
	 * @param obstacles : regions the lines are not to cross
	 */
	public abstract void genConnections(NodeConnection nodeConnection, List<Rectangle> obstacles);
	
	/**
	 * redraws the connections for only the given nodes.
	 * 
	 * a better option than redrawing all connections, however depending on the ConnectionStyle this may not be much better.
	 * 
	 * assume to be processing intensive, use carefully
	 *  
	 * @param nodeConnection : source of connection information
	 * @param obstacles : regions the lines are not to cross 
	 * @param terminalsToReconnect : specific terminals to recalculate
	 */
	public abstract void genConnection(NodeConnection nodeConnection, List<Rectangle> obstacles, int[] terminalsToReconnect);
}
