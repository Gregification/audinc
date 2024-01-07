package draggableNodeEditor;

import java.awt.Component;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;

/**
 * connects NodeConsumers. line drawing is done by ConnectionStyle
 * 
 * this class is held together by a excessive use of the "volatile" key word. it seems to work though, just keep this in mind if your getting funky errors
 */
public class NodeConnection {	
	public volatile List<AnchorPoint> anchors = new ArrayList<>();
	
	/** set of directly connected components
	 */
	protected HashSet<NodeComponent<?>> directleyConnectedComponents 	= new HashSet<>();
	
	/**  true if this connection is a valid network node, otherwise networks will ignore this connection
	 */
	private volatile boolean networkable = false;

	/** a shared hashset between each group of connected NodeConnections
	 */
//	protected HashSet<NodeConnection> reachableConnections 			= new HashSet<>(List.of(this));
	
	//drawing stuff
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private	volatile boolean needsRedrawn = true;
	private volatile ConnectionStyle connectionStyle = ConnectionStyle.getDefaultConnectionsStyle();

	public NodeConnection() {
		super();
	}
	
	public boolean isPoinless() {
		return directleyConnectedComponents.size() < 2;
	}
	
	/**
	 * listener support -> https://docs.oracle.com/javase/8/docs/api/java/beans/PropertyChangeSupport.html
	 * listeners -> https://docs.oracle.com/javase/tutorial/uiswing///events/propertychangelistener.html
	 * SwingWorker (not used since its not a vitural thread) -> https://docs.oracle.com/javase/7/docs/api/javax/swing/SwingWorker.html
	 */
	
	/**
	 * draws the line points to the lineImage. done this way to somewhat force u to cache it
	 * @param obstacles : regions the lines will try to avoid (see ConnectionStyle doc. for more info).
	 */
	public void draw(final Polygon[] obstacles, BufferedImage outputTo, final Component hostComp) {
		var comps = this.directleyConnectedComponents.stream().toList();
		
		final LineAnchor[] 
				anchs  	= this.anchors.stream().map(LineAnchor::getFromAnchorPoint).toArray(LineAnchor[]::new),
				terms	= comps.stream().sequential()
							.map(comp -> LineAnchor.getFromNodeComponent(comp, hostComp))
							.toArray(LineAnchor[]::new);
		
//		var s = new StringBuilder("node connection > draw; terminals before and after point conversion");
		
		for(int i = 0; i < terms.length; i++) {
			var c	= comps.get(i);
			var cp 	= c.connectionPoint;
//			s.append("\n\t> (" + cp.x  + "," + cp.y + ")\t -> \t(" + terms[i].x() + "," + terms[i].y() + ")");
//			s.append("\t\t component:" + c.getName() + "\t\t hostNode:" + c.getHostNode());
		}
		
//		System.out.println(s);
		
		needsRedrawn = false;
		
		connectionStyle.drawConnection(
					outputTo.getWidth(),
					outputTo.getHeight(),
					anchs,
					terms,
					obstacles,
					pcs
				);		
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    
    public void removeAllPropertyChangeListener(PropertyChangeListener listener) {
    	for(var v : this.pcs.getPropertyChangeListeners())
    		if(v == listener) this.removePropertyChangeListener(listener);
    }
	
	/**
	 * adds the component as a directly connected one. rechecks all connections associated with the component
	 * @param comp : the new component
	 */
	public void connectToComponent(NodeComponent<?> comp) {	
		//if already connected
		if(directleyConnectedComponents.contains(comp)) 
			return;
		
//		System.out.println("node conneciton > connect to component, comp: " + comp);
		
		//if is a orphaned node -> no need to worry about a network
//		if(isNetworkable() && !comp.getDirectConnections().isEmpty()) {			
//			//check if the existing networks are valid (this should always be true but i don't trust my code)
//			boolean 
//				isOtherSus 	= isNodeNetworkSus(comp, true),
//				isThisSus	= this.getDirectleyConnectedComponents().stream().anyMatch(n -> isNodeNetworkSus(n, false));
//			
//			HashSet<NodeConnection> otherNet = comp.getDirectConnections().getFirst().reachableConnections;
//			
//			if(!isOtherSus || !isThisSus) {
//				//if the networks share a connection
//				if(otherNet.stream().anyMatch(reachableConnections::contains))
//					isOtherSus = isThisSus = true;
//			}
//			
//			//if-elses' for readability.
//			if(isOtherSus && isThisSus) { 	//trust nothing, except your self :3
//				remap(List.of(this));
//			}else if(isOtherSus) {			//trust this network.
//				remap(reachableConnections);
//			}else if(isThisSus) {			//trust the other network.
//				remap(otherNet);
//			}else {							//trust both networks.
//				//if networks are the same
//				if(reachableConnections == otherNet) return;
//				
//				//join
//				joinNetworks(reachableConnections, otherNet);
//			}
//		}
		
		//update self
		needsRedrawn = true;
		directleyConnectedComponents.add(comp);
		
		//update the node
		comp.joinConnection(this);
	}
	
	/**
	 * disconnects a node from the the connection, is not guaranteed to remove it from the network
	 * @param comp
	 */
	public void disconnectComponent(NodeComponent<?> comp) {
		//if is connected
		if(!directleyConnectedComponents.remove(comp))
			return;
		
		//update node
		comp.dropConnection(this);
		
		//update network
		//if linked to another connection
//		if(comp.getDirectConnections().stream().filter(conn -> conn != this).anyMatch(e -> true)) {
//			//lazy, just remap the network
//			remap(List.of(this));
//		}
	}
	
	/**
	 * checks if a node is part of a proper network. 
	 * @param node the node to check
	 * @return true if all direct NodeConnections share the same network, or if there are no connections
	 */
//	public boolean isNodeNetworkSus(NodeComponent<?> node, boolean ignoreself) {
//		ArrayList<NodeConnection> conns = new ArrayList<>(node.getDirectConnections());
//		
//		if(ignoreself) conns.remove(this);
//		
//		if(conns.isEmpty()) return true;
//		
//		var net = conns.getFirst().reachableConnections;
//		
//		return conns.stream()
//				.allMatch(conn -> conn.reachableConnections == net);
//	}
	
	/**
	 * ignores all existing information. remaps connections using the given knownConnections and sets this.reachableConnections to the updated map.
	 * this instance will now host the network map
	 */
//	public HashSet<NodeConnection> remap(Collection<NodeConnection> knownConnections) {
//		reachableConnections.clear();
//		
//		Set<NodeConnection> connections = ConcurrentHashMap.<NodeConnection>newKeySet(); //basically a concurrent HashSet
//			connections.addAll(knownConnections);
//			connections.add(this);
//			
//		try(ExecutorService exe = Executors.newVirtualThreadPerTaskExecutor()){
//			recursiveConnectionMapperFunc(this, connections, exe);
//		}
//		
//		//replace the existing network
//		//this is the new head
//		reachableConnections.addAll(connections);
//		
//		//idealy would only have to update the refrences in [connections] but just incase something went wrong elsewhere this may correct it
//		for(var conn : reachableConnections)
//			conn.reachableConnections = reachableConnections;
//		
//		return reachableConnections;
//	}
//	private final void recursiveConnectionMapperFunc(NodeConnection srcConn, Set<NodeConnection> knownConnections, ExecutorService exe) {
//		srcConn.getDirectleyConnectedComponents().stream()
//			.flatMap(comp -> comp.getDirectConnections().stream())
//			.filter(conn -> conn != srcConn) //preemptively removes some known duplicates
//			.forEach(conn -> {
//				if(knownConnections.add(conn))
//					exe.execute(() -> recursiveConnectionMapperFunc(conn, knownConnections, exe));
//			});
//	}
	
//	public static HashSet<NodeConnection> joinNetworks(HashSet<NodeConnection> net1, HashSet<NodeConnection> net2) {
//		//faster to add the smaller network to the larger one
//		HashSet<NodeConnection> small, big;
//				
//		if(net1.size() > net2.size()) {
//			small 	= net2;
//			big 	= net1;
//		}else {
//			small 	= net1;
//			big 	= net2;
//		}
//		
//		big.addAll(small);
//		
//		//update references
//		for(var conn : small)
//			conn.reachableConnections = big;
//		
//		return big;
//	}
	
	public void deleteConnection() {		
		//remap the neighboring connections
		
		//networks of the neighbors that have been remapped
//		HashSet<HashSet<NodeConnection>> knownGoodNets = new HashSet<>();
		for(var node : directleyConnectedComponents) {
			directleyConnectedComponents.remove(node);
			node.dropConnection(this);
			
//			var conns = node.getDirectConnections();
//			if(conns.isEmpty()) continue;
			
			//assume the network is good
			//if the network has not been remaped -> remap it
//			var conn = conns.getFirst();
//			if(knownGoodNets.add(conn.reachableConnections)) {
//				conn.remap(List.of(conn));
//			}
		}
		
		connectionStyle.dispose();
		
	}
	
//////////////////////////
// getters & setters
//////////////////////////
	/**
	 * if the optional flag to redraw the image has been set
	 * @return
	 */
	public boolean needsRedrawn() {
		return needsRedrawn;
	}
	
	/**
	 * sets the needsRedrawn option and returns what it previously was
	 * @param newVal
	 * @return true if the new value changed anything
	 */
	public boolean setNeedsRedrawn(boolean newVal) {
		var ret = needsRedrawn != newVal;
		needsRedrawn = newVal;
		return ret;
	}
	
	public List<NodeComponent<?>> getDirectleyConnectedComponents(){
		return directleyConnectedComponents.stream().toList();
	}
	
	public ConnectionStyle getConnectionStyle() {
		return connectionStyle;
	}
	
	public Rectangle getConnectionImageCoverage() {
		return connectionStyle.getConnectionImageCoverage();
	}
	
	/**
	 * sets the connection style, defaults to [DirectConnectionStyle] if NULL 
	 * @param connectionStyle, if NULL will use default
	 */
	public void setConnectionStyle(ConnectionStyle newConnStyle) {
		connectionStyle.dispose();
		
		if(newConnStyle == null)
			connectionStyle = ConnectionStyle.getDefaultConnectionsStyle();
		else
			connectionStyle = newConnStyle;
		
		needsRedrawn = true;
	}
//	public boolean isNetworkable() {
//		return networkable;
//	}
//
//	public void setNetworkable(boolean networkable) {
//		this.networkable = networkable;
//	}
	
	@Override public String toString() {
		return getClass().getCanonicalName() + "[" 
				+ "networkable:"+networkable
				+ ", needsRedrawn:"+needsRedrawn
				+ ", style:"+connectionStyle
				+ ", connectedComponents:"+directleyConnectedComponents
				+ "]";
	}
}