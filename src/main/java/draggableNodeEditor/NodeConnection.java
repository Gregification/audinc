package draggableNodeEditor;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
	
	protected ArrayList<AnchorPoint> anchors = new ArrayList<>();
	
	//network stuff
	/**
	 * set of directly connected components
	 */
	protected HashSet<NodeComponent<T>> directleyConnectedComponents = new HashSet<>();
	
	//drawing stuff
	private boolean needsRedrawn = true;
	protected int lineWidth = defaultLineWidth;
	
	private List<Point> linePoints = List.of();
	private ConnectionStyle connectionStyle;
	
	/**
	 * the future thats doing the line calculations
	 */
	private CompletableFuture<Point[]> connectionFuture;

	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	public boolean isDirectlyConnected(NodeComponent<T> comp) {
		return directleyConnectedComponents.contains(comp);
	}
	
	public void connectToComponents(List<NodeComponent<T>> comps) {
		var newComps = comps.stream().parallel()
			.filter(directleyConnectedComponents::add)
			.peek(comp -> comp.joinConnection(this))
			.toList();
		
		if(newComps.size() == 0) return;
		
		var connPacket = new NodeConnectionUpdatePacket<T>(
				new HashSet<NodeConnection<T>>(),
				newComps,
				Collections.<NodeComponent<T>>emptyList()
			);
		
		receiveUpdatePacket(connPacket);
	}
	
	public void disconnectComponents(List<NodeComponent<T>> comps) {
		var newComps = comps.stream().parallel()
				.filter(directleyConnectedComponents::remove)
				.peek(comp -> comp.dropConnection(this))
				.toList();
			
		if(newComps.size() == 0) return;
		
		var connPacket = new NodeConnectionUpdatePacket<T>(
				new HashSet<NodeConnection<T>>(),
				Collections.<NodeComponent<T>>emptyList(),
				newComps
			);
		
		receiveUpdatePacket(connPacket);
	}
	
	public void redraw(final Polygon[] obsticals) {
		
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
	
//////////////////////////
// network
//////////////////////////
	/**
	 * apply the changes to itself
	 * @param changes
	 */
	void receiveUpdatePacket(NodeConnectionUpdatePacket<T> changes) { this.receiveUpdatePacket(changes, true); }
	void receiveUpdatePacket(NodeConnectionUpdatePacket<T> changes, boolean propagate) {
		if(changes.visitedConnections().add(this)) {
			
			//update packet with this nodes info
			
			
			//propagate to neighboring connections
			if(propagate) propagateUpdatePacket(changes);
		}
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
	
//////////////////////////
// getters & setters
//////////////////////////
	public List<NodeComponent<T>> getDirectleyConnectedComponents(){
		return directleyConnectedComponents.stream().toList();
	}
	
	public List<AnchorPoint> getAnchors(){
		return anchors.stream().toList();
	}
	
	public List<Point> getLinePoints(){
		return this.linePoints;
	}
	
	public CompletableFuture<Point[]> getPointFuture(){
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