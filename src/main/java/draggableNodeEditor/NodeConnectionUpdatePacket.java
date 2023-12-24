package draggableNodeEditor;

import java.util.HashSet;
import java.util.List;

/**
 * update packet for the node connection network.
 * 
 * can be used to ping for all connected nodes by... make new packet, clear visitedConnections, then override the consider method of a addedNode. all connected nodes will eventually ping it.  
 * 
 * <br>concept is not thread safe (HashSet is not thread safe. would need some way to keep track if this packet is out dated and what other packet should override this one). 
 * @param <T> 
 */
record NodeConnectionUpdatePacket<T> (
			HashSet<NodeConnection<T>> visitedConnections,
			List<NodeComponent<T>> addedNodes,
			List<NodeComponent<T>> removedNodes
		) {
	
	/**
	 * somewhat more efficient way to check if unvisited nodes exist.
	 * use .getUnvisitedEdges() to get a complete list of all unvisited neighboring connections
	 * @return true if a unvisited NodeConnection is found
	 */
	public boolean hasVisitedAll() {
		return visitedConnections.stream() 
				.allMatch(conn ->	//checks that all neighboring connections exist in the Set
							visitedConnections.containsAll(
									getNeighboringConnections(conn)
								)
						);
	}
	
	/**
	 * @return a complete list of all edge NodeConnections that have not registered with this packet.
	 */
	public List<NodeConnection<T>> getUnvisitedEdges(){
		return visitedConnections.stream()
				.flatMap(conn -> getNeighboringConnections(conn).stream())
				.filter(conn -> !visitedConnections.contains(conn))
				.toList();
	}
	
	/**
	 * gets a list of connections that are shared by the given connection's direct nodes.
	 * list may not be unique.
	 * @return
	 */
	private List<NodeConnection<T>> getNeighboringConnections(NodeConnection<T> srcConn){
		return srcConn.getDirectleyConnectedComponents().stream()
				.flatMap(comp -> comp.getDirectConnections().stream())
				.toList();
	}
}
