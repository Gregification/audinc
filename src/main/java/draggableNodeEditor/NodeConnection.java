package draggableNodeEditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import draggableNodeEditor.connectionStyles.DirectConnectionStyle;


/**
 * abstraction to make connecting nodes simpler. also doubles as a UI element
 * each connection may have unlimited number of consumers, but only 1 source. The source can be another consumer.
 * the source is not part of the <code>terminals</code> list 
 */
public class NodeConnection<T> {
	public static final int defaultLineWidth = 5;
	
	protected volatile int lineWidth = defaultLineWidth; 
	
	/**
	 * for each sub array; the first index of each is the terminal,
	 *  everything else are the steps to reach the next terminal(the next sub array).
	 * 
	 * the last terminal does not have steps.
	 * no points need to be unique. 
	 */
	protected volatile List<List<Point>> points = List.of();
	
	/**
	 * maps a terminal(by index), to the node its connected to
	 */
	protected volatile List<NodeComponent<T>> connectedNodes = List.of();
	
	protected volatile ConnectionStyle connectionStyle;
	
	/**
	 * this is a flag that can be triggered by this class (<class>NodeConnection</class>)
	 * this class does not ever set this flag to false.
	 * 
	 * you must set this flag to false by your own means.
	 * preferably after the connection style has recalculated the connections.
	 */
	private volatile Set<Integer> terminalsToRepath = Set.of();
	
	public NodeConnection(ConnectionStyle connectionStyle) {
		this.connectionStyle = connectionStyle;
	}
	public NodeConnection(Point... terminals) {
		for(var v : terminals)
			points.add(List.of(v));
	}
	public NodeConnection(List<List<Point>> points) {
		this.points = points;
	}
	
	public List<NodeComponent<T>> snapTerminalsToConnections(List<NodeComponent<T>> nodeComponents){
		List<NodeComponent<T>> ret = List.of();
		
		//this is usually how java Swing's code is written so maybe its better than a iterator loop. not sure
		for(int iTerm = 0, numTerminals = points.size(), numComponents = nodeComponents.size(), iComp = 0
			; iTerm < numTerminals; iTerm++) {
			
			Point terminal = points.get(iTerm).get(0);
			
			for(iComp = 0; iComp < numComponents; iComp++) {
				NodeComponent<T> comp = nodeComponents.get(iComp);
				
				if(comp.isInConnectionSeletionRegion(terminal)) {
					ret.add(comp);
					
					this.connectTerminalTo(iTerm, comp);
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param idx
	 * @param comp
	 * @return true if a new connection was made, false if a connection already existed 
	 */
	public boolean connectTerminalTo(int idx, NodeComponent<T> comp) {
		if(idx >= points.size()) throw new IndexOutOfBoundsException("trying to access a terminal that dosent exist. # of terminals:" + points.size() + ", accedded index:" + idx);
		
		if(this.connectedNodes.contains(comp)) return false; // connection already exists
		
		//detach from existing connection
		this.detachTerminal(idx);
	
		synchronized(this.connectedNodes) {
			//register connection
			this.connectedNodes.set(idx, comp);
		}	
	
		//set point
		Point terminal = this.points.get(idx).get(0);
			terminal.x = comp.connectionPoint.x;
			terminal.y = comp.connectionPoint.y;
		
		//register that a terminal has been changed
		this.terminalsToRepath.add(idx);
		
		comp.addConnection(this);
		return true;
	}
	
	/**
	 * makes a new terminal that connects to comp
	 * @param comp
	 * @return the index of the connected terminal
	 */
	public int addTerminalTo(NodeComponent<T> comp) {
		if(this.connectedNodes.contains(comp)) // connection already exists
			return this.connectedNodes.indexOf(comp);
		
		//make new terminal and connect
		int newTerminal = this.addTerminal(0, 0);
		this.connectTerminalTo(newTerminal, comp);
		
		return newTerminal;
	}
	
	public NodeComponent<T> detachTerminal(int idx){
		if(this.connectedNodes.size() < idx) {
			NodeComponent<T> connectedNode;
			
			connectedNode = connectedNodes.get(idx);
			connectedNodes.set(idx, null);
			
			connectedNode.dropConnection(this);
			
			return connectedNode;
		} else return null;
	}
	
	public boolean dropComponent(NodeComponent<T> comp) {
		synchronized(this.connectedNodes) {
			return this.connectedNodes.remove(comp);
		}
	}
	
	/**
	 * makes a new terminal using the point.
	 * @return terminal id
	 */
	public int addTerminal(int x, int y) {
		Point newTerminal = new Point(x, y);
		
		int id = points.size();
		points.add(List.of(newTerminal));
		
		terminalsToRepath.add(id);
		
		return id;
	}
	
	public NodeComponent<T> getTerminalTarget(int idx){
		return connectedNodes.get(idx);
	}
	
	public void genConnections(List<Rectangle> obstacles) {
		terminalsToRepath.clear();
		
		this.connectionStyle.genConnections(this, obstacles);
	}
	
	public void genConnection(List<Rectangle> obstacles, int[] terminalsToReconnect) {
		this.terminalsToRepath.removeAll(Arrays.stream(terminalsToReconnect).boxed().toList());
		
		this.connectionStyle.genConnection(this, obstacles, terminalsToReconnect);
	}
	
	public void genMinimalConnections(List<Rectangle> obstacles) {
		final int numTerminals = points.size();
		int[] terminalsToReconnect = terminalsToRepath.stream()
					.filter(i -> i <= numTerminals)
					.mapToInt(Integer::intValue)
					.toArray();
		
		this.terminalsToRepath.clear();
		
		this.genConnection(obstacles, terminalsToReconnect);
	}
	
//////////////////////////
// getters & setters
//////////////////////////	
	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	public List<List<Point>> getPoints() {
		return points;
	}
	
	public void setPoints(List<List<Point>> points) {
		this.points = points;
	}
	
	public ConnectionStyle getConnectionStyle() {
		return connectionStyle;
	}
	
	public void setConnectionStyle(ConnectionStyle connectionStyle) {
		if(connectionStyle == null)
			this.connectionStyle = new DirectConnectionStyle();
		
		this.connectionStyle = connectionStyle;
	}
}