package draggableNodeEditor;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;
import draggableNodeEditor.NodeConnectionDrawer.WeightedPoint;
import draggableNodeEditor.connectionStyles.DirectConnectionStyle;

/**
 * connects NodeConsumers
 * 
 * @unrelated
 * > be me
 * <br>> morbius(2018)
 * <br>> fight sequence in tourist gimie store, specifically funko pop isle
 * <br>>"stand b-back, im - i'm -- i'm goinngg tooo c-cconsuumme!"
 * <br>> what did he mean by this 
 */
public class NodeConnection<T> {
	public static final int defaultLineWidth = 5;
	
	public final Class<T> type;
	
	public ArrayList<AnchorPoint> anchors = new ArrayList<>();
	public HashSet<LineAnchor> knownAnchors = new HashSet<>();
	
	//network stuff
	/**
	 * set of directly connected components
	 */
	protected HashSet<NodeComponent<T>> directleyConnectedComponents 	= new HashSet<>();
	
	/**
	 * a shared hashset between each group of connected NodeConnections
	 */
	protected HashSet<NodeConnection<T>> reachableConnections 			= new HashSet<>(List.of(this));
	
	//drawing stuff
	private boolean needsRedrawn = true;
	protected int lineWidth = defaultLineWidth;
	
	private PriorityBlockingQueue<WeightedPoint> linePoints =  new PriorityBlockingQueue<>();
	private ConnectionStyle connectionStyle;
	
	/**
	 * the future thats doing the line calculations
	 */
	private CompletableFuture<PriorityBlockingQueue<WeightedPoint>> connectionFuture;

	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	/**
	 * adds the component as a directly connected one. rechecks all connections associated with the component
	 * @param comp : the new component
	 */
	public void connectToComponent(NodeComponent<T> comp) {		
		//if already connected
		if(directleyConnectedComponents.contains(comp)) 
			return;
		
		//if is a orphaned node -> no need to worry about a network
		if(!comp.getDirectConnections().isEmpty()) {			
			//check if the existing networks are valid (this should always be true but i don't trust my code)
			boolean 
				isOtherSus 	= isNodeNetworkSus(comp, true),
				isThisSus	= this.getDirectleyConnectedComponents().stream().anyMatch(n -> isNodeNetworkSus(n, false));
			
			HashSet<NodeConnection<T>> otherNet = comp.getDirectConnections().getFirst().reachableConnections;
			
			if(!isOtherSus || !isThisSus) {
				//if the networks share a connection
				if(otherNet.stream().anyMatch(reachableConnections::contains))
					isOtherSus = isThisSus = true;
			}
			
			//if-elses' for readability.
			if(isOtherSus && isThisSus) { 	//trust nothing, except your self :3
				remap(List.of(this));
			}else if(isOtherSus) {			//trust this network.
				remap(reachableConnections);
			}else if(isThisSus) {			//trust the other network.
				remap(otherNet);
			}else {							//trust both networks.
				//if networks are the same
				if(reachableConnections == otherNet) return;
				
				//join
				joinNetworks(reachableConnections, otherNet);
			}
		}
		
		//update self
		directleyConnectedComponents.add(comp);
		
		//update the node
		comp.joinConnection(this);
	}
	
	/**
	 * disconnects a node from the the connection, is not guaranteed to remove it from the network
	 * @param comp
	 */
	public void disconnectComponent(NodeComponent<T> comp) {
		//if is connected
		if(!directleyConnectedComponents.remove(comp))
			return;
		
		//update node
		comp.dropConnection(this);
		
		//update network
		//if linked to another connection
		if(comp.getDirectConnections().stream().filter(conn -> conn != this).anyMatch(e -> true)) {
			//lazy, just remap the network
			remap(List.of(this));
		}
	}
	
	/**
	 * checks if a node is part of a proper network. 
	 * @param node the node to check
	 * @return true if all direct NodeConnections share the same network, or if there are no connections
	 */
	public boolean isNodeNetworkSus(NodeComponent<T> node, boolean ignoreself) {
		List<NodeConnection<T>> conns = node.getDirectConnections();
		
		if(ignoreself) conns.remove(this);
		
		if(conns.isEmpty()) return true;
		
		var net = conns.getFirst().reachableConnections;
		
		return conns.stream()
				.allMatch(conn -> conn.reachableConnections == net);
	}
	
	/**
	 * ignores all existing information. remaps connections using the given knownConnections and sets this.reachableConnections to the updated map.
	 * this instance will now host the network map
	 */
	public HashSet<NodeConnection<T>> remap(Collection<NodeConnection<T>> knownConnections) {
		reachableConnections.clear();
		
		Set<NodeConnection<T>> connections = ConcurrentHashMap.<NodeConnection<T>>newKeySet(); //basically a concurrent HashSet
			connections.addAll(knownConnections);
			connections.add(this);
			
		try(ExecutorService exe = Executors.newVirtualThreadPerTaskExecutor()){
			recursiveConnectionMapperFunc(this, connections, exe);
		}
		
		//replace the existing network
		//this is the new head
		reachableConnections.addAll(connections);
		
		//idealy would only have to update the refrences in [connections] but just incase something went wrong elsewhere this may correct it
		for(var conn : reachableConnections)
			conn.reachableConnections = reachableConnections;
		
		return reachableConnections;
	}
	private final void recursiveConnectionMapperFunc(NodeConnection<T> srcConn, Set<NodeConnection<T>> knownConnections, ExecutorService exe) {
		srcConn.getDirectleyConnectedComponents().stream()
			.flatMap(comp -> comp.getDirectConnections().stream())
			.filter(conn -> conn != srcConn) //preemptively removes some known duplicates
			.forEach(conn -> {
				if(knownConnections.add(conn))
					exe.execute(() -> recursiveConnectionMapperFunc(conn, knownConnections, exe));
			});
	}
	
	public static <V> HashSet<NodeConnection<V>> joinNetworks(HashSet<NodeConnection<V>> net1, HashSet<NodeConnection<V>> net2) {
		//faster to add the smaller network to the larger one
		HashSet<NodeConnection<V>> small, big;
				
		if(net1.size() > net2.size()) {
			small 	= net2;
			big 	= net1;
		}else {
			small 	= net1;
			big 	= net2;
		}
		
		big.addAll(small);
		
		//update references
		for(var conn : small)
			conn.reachableConnections = big;
		
		return big;
	}
	
	/**
	 * recalculates the lines points.
	 * @param obstacles : regions the lines will try to avoid (see ConnectionStyle doc. for more info).
	 */
	public void redraw(final Polygon[] obstacles) {
		stopRedrawing();
		LineAnchor[] 
				anchs  	= this.anchors.stream().map(LineAnchor::getFromAnchorPoint).toArray(LineAnchor[]::new),
				terms	= Stream.concat(
								directleyConnectedComponents.stream().map(LineAnchor::getFromNodeComponent),
							  	knownAnchors.stream())
							.toArray(LineAnchor[]::new);
		
		linePoints.clear();
		
		connectionFuture = connectionStyle.genConnections(
					linePoints,
					anchs,
					terms,
					obstacles
				);
	}
	
	public void setNeedsRedrawing(boolean needsRedrawn) { this.needsRedrawn = needsRedrawn; }
	public boolean needsRedrawing() { return this.needsRedrawn; }
	
	public boolean isRedrawing() { return !connectionFuture.isDone(); }
	
	/**
	 * stops redrawing if it is drawing. does nothing otherwise
	 * @return true : if redrawing was stopped . false : if redrawing was already stopped
	 */
	public boolean stopRedrawing() {
		if(!isRedrawing()) return false;
		
		connectionFuture.cancel(true);
		
		return true;
	}
	
	/**
	 * send the changes to neighboring connections.
	 * @param changes
	 */
	void propagateUpdatePacket(NodeConnectionUpdatePacket<T> changes) {
		if(changes.visitedConnections().add(this)) {
			
			//stop dead end propagations
			if(changes.addedNodes().size() == 0 && changes.removedNodes().size() == 0) return;
			
			//send to neighbors
			for(var comp : directleyConnectedComponents) {
				comp.getDirectConnections().stream()
					.filter(conn -> conn != this)		//could be better
					;
			}
		}
	}
	
	public void deleteConnection() {
		stopRedrawing();
		
		//remap the neighboring connections
		
		//networks of the neighbors that have been remapped
		HashSet<HashSet<NodeConnection<T>>> knownGoodNets = new HashSet<>();
		for(var node : directleyConnectedComponents) {
			directleyConnectedComponents.remove(node);
			node.dropConnection(this);
			
			var conns = node.getDirectConnections();
			if(conns.isEmpty()) continue;
			
			//assume the network is good
			//if the network has not been remaped -> remap it
			var conn = conns.getFirst();
			if(knownGoodNets.add(conn.reachableConnections)) {
				conn.remap(List.of(conn));
			}
		}
		
		directleyConnectedComponents.clear();
	}
	
//////////////////////////
// getters & setters
//////////////////////////	
	public List<NodeComponent<T>> getDirectleyConnectedComponents(){
		return directleyConnectedComponents.stream().toList();
	}
	
	public Queue<WeightedPoint> getLinePoints(){
		return linePoints;
	}
	
	public CompletableFuture<PriorityBlockingQueue<WeightedPoint>> getPointFuture(){
		return this.connectionFuture;
	}
	
	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = Math.max(lineWidth, 1);
	}
	
	public ConnectionStyle getConnectionStyle() {
		return connectionStyle;
	}
	
	/**
	 * sets the connection style, defaults to [DirectConnectionStyle] if NULL 
	 * @param connectionStyle, if NULL will use default
	 */
	public void setConnectionStyle(ConnectionStyle connectionStyle) {
		if(connectionStyle == null)
			this.connectionStyle = new DirectConnectionStyle();
		
		this.connectionStyle = connectionStyle;
	}
}