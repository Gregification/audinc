package draggableNodeEditor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * update packet for the node connection network.
 * 
 * can be used to ping for all connected nodes by... make new packet, clear visitedConnections, then override the consider method of a addedNode. all connected nodes will eventually ping it.  
 * 
 * <br>concept is only thread safe because its self contained. changing NodeConneciton or NodeComponetn calls may invalidate this  
 * @param <T> 
 */
record NodeConnectionUpdatePacket<T> (
			HashSet<NodeConnection<T>> visitedConnections,
			LinkedList<NodeConnection<T>> knownConnectionsToCheck,
			HashSet<NodeComponent<T>> addedNodes,
			HashSet<NodeComponent<T>> removedNodes
		) {

	/**
	 * gets a empty packet.
	 */
	public static final <V> NodeConnectionUpdatePacket<V> getEmptyPacket() {
		return new NodeConnectionUpdatePacket<V>(
				new HashSet<NodeConnection<V>>(),
				new LinkedList<NodeConnection<V>>(),
				new HashSet<NodeComponent<V>>(),
				new HashSet<NodeComponent<V>>()
			);
	}
	
	/**
	 * updates packet information with the node. 
	 * override this method to get updates on what connection has been considered
	 * @param conn
	 */
	public void checkPacket(NodeConnection<T> conn) {
		if(this.visitedConnections.add(conn)){
			List<NodeComponent<T>> comps = conn.getDirectleyConnectedComponents();
			removedNodes.removeAll(comps); 	//if is still connected
			addedNodes.removeAll(comps);	//if was already connected
		}
	}
	
	/**
	 * the main function to update a connection. will check packet against all found nodes then puts the packet in effect.
	 * packets are automatically applied when finished.   
	 * @param srcConn
	 */
	public final void propagatePacket(NodeConnection<T> srcConn) {
		if(srcConn != null)
			knownConnectionsToCheck.add(srcConn);
		
		//keep looping anc chacking against each node
		
		//add all neighboring connections to knownConnectionsToCheck 
		srcConn.getDirectleyConnectedComponents().stream()
			.map(comp -> comp.getDirectConnections())
			.flatMap(conns -> conns.stream())
			.filter(conn -> conn != srcConn)
			.forEach(knownConnectionsToCheck::add);
	}
	
	/**
	 * applies changes to all visited nodes
	 */
	private final void applyPacket() {
		
	}
	
	public boolean isRelivent() {
		return addedNodes.size() > 0 || removedNodes.size() > 0;
	}
	
	/**
	 * empties knownConnectionsToCheck and repopulates according to "visitedConnections"
	 */
	public void resetKnownConnectionsToCheck() {
		knownConnectionsToCheck.clear();
		knownConnectionsToCheck.addAll(getUnvisitedEdges().stream().distinct().toList());
	}
	
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
	 * a complete list of all edge NodeConnections that have not registered with this packet.
	 * the list elements are not guaranteed to be distinct.
	 * @return list of edge NodeConnections. 
	 * 
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
