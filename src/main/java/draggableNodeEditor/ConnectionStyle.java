package draggableNodeEditor;

import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

public interface ConnectionStyle {
	/**
	 * generates the path, point by point, between all terminal nodes. 
	 * the final terminal node will not get a path made for it.
	 * 
	 * assume to be processing intensive, use sparsely
	 * @param <T>
	 * 
	 * @param nodeConnection : source of connection information
	 * @param obstacles : regions the lines are not to cross
	 */
	public abstract <T> void genConnections(NodeConnection<T> nodeConnection, Rectangle[] obstacles);
	
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
	public abstract <T> void genConnection(NodeConnection<T> nodeConnection, Rectangle[] obstacles, Set<TerminalPoint<T>> terminalsToReconnect);
}
