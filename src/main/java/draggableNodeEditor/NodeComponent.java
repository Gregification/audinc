package draggableNodeEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.JComponent;

/**
 * a editable value for the
 * @param <T>
 */
public abstract sealed class NodeComponent<T> extends JComponent permits NodeConsumer, NodeSupplier {
	public final Class<T> type;
	private static final long serialVersionUID = 1L;
	
	//node stuff
	protected String name;
	protected HashSet<NodeConnection> 	directConnections = new HashSet<>();
	protected CompletableFuture<T> valueFuture = CompletableFuture.supplyAsync(() -> null);
	protected DraggableNode<?> hostNode = null;
	
	/**
	 * center of the connectionPoint relative to the host DraggableNode
	 */
	public volatile Point 	connectionPoint 	= new Point(0,0);	//decided by the host node 
	
	/**
	 * preferred connection point icon values. these may not necessarily correlate to a circle
	 */
	public final static int 
		connectionPointRaduis = 12,
		connectionPointBorder = 3;
	protected NodeComponentStatus compStatus = NodeComponentStatus.NETURAL;

	public NodeComponent(Class<T> type, String name, T value) {
		this.type = type;
		this.setName(name);
		this.setValue(value);
		this.setLayout(new FlowLayout());
	}
	
	public abstract CompletableFuture<T> getValue();
	public abstract void setValue(T value);
	
	/**
	 * gets called when a new node joins the connection. 
	 * @param comp : the new node
	 */
	public abstract void considerComponent(NodeComponent<T> comp);
	
	/**
	 * gets called when a node is removed from the connection. 
	 * it is likely, especially in networks with many shared nodes, that the same NodeComponent 
	 *  will be called by unconsiderComponent will be called shortly before being rediscovered by
	 *  considerComponent.
	 * @param comp : the removed node
	 */
	public abstract void unconsiderComponent(NodeComponent<T> comp);
	
	public abstract NodeSupplier<T> getSupplier();
	
	public List<NodeConnection> getDirectConnections(){
		return directConnections.stream().toList();
	}
	
	public DraggableNode<?> getHostNode(){
		return this.hostNode;
	}
	
	public void joinConnection(NodeConnection conn) {
		//if is not connected
		if(directConnections.add(conn)) {
			//update the connection
			conn.connectToComponent(this);
		}
	}
	
	public void dropConnection(NodeConnection conn) {
		//if is connected
		if(directConnections.remove(conn)) {
			//update the connection
			conn.disconnectComponent(this);
		}
			
	}
	
	/**
	 * draws the connection ports indicator, used by NodeComp's .paintComponent()
	 * @param g
	 * @param p, point that is the connection point. note that this will be THE RCENTER OF THE ICON, not the top-left
	 */
	public void drawConnectionPoint(Graphics g, Point p) {
		assert g != null && p != null : "(g is null?"+(g==null)+"),(p is null?"+(p==null)+")";
		
		//makes a circle with the color of [importance.color] with a black border
		int 
			R = connectionPointRaduis+connectionPointBorder,
			r =	connectionPointRaduis;
		g.setColor(Color.black);
		g.fillOval(p.x - R/2, p.y - R/2, R, R);
		g.setColor(compStatus.color);
		g.fillOval(p.x - r/2, p.y - r/2, r, r);
	}
	
	public boolean isInConnectionSeletionRegion(Point p) {
		return this.connectionPoint.distanceSq(p) <= Math.pow(connectionPointRaduis+connectionPointBorder, 2);
	}
	
	public Dimension getConnecitonPointDimensions() {
		int r = connectionPointRaduis+connectionPointBorder;
		return new Dimension(r,r);
	}
	
	/**
	 * hash any sort of connection. 
	 * @return true if a direct connection exists
	 */
	public boolean hasConnection() {
		return directConnections.size() != 0;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String newName) {
		this.name = (newName == null) ? "" : newName;
		
		this.setToolTipText(name);
	}
	
	public NodeComponentStatus getCompStatus() {
		return compStatus;
	}
	
	public boolean isCompStatus(NodeComponentStatus... statius) {
		var stats = List.of(statius);
		return stats.contains(compStatus);
	}

	public void setCompStatus(NodeComponentStatus stat) {
		compStatus = stat;
	}
	
	public void setImportance(NodeComponentStatus importance) {
		if(importance != null)
			this.compStatus = importance;
	}
	
	public NodeConnection getNewConnection(){
		NodeConnection conn = new NodeConnection();
		
		return conn;
	}
}