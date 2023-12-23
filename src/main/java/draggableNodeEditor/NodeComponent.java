package draggableNodeEditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * a editable value for the
 * @param <T>
 */
public abstract class NodeComponent<T> extends JComponent {
	public final Class<T> type;
	
	private static final long serialVersionUID = 1L;
	
	//node stuff
	protected String name;
	protected volatile T value = null;
	public volatile ArrayList<NodeConnection<T>> 	connections = new ArrayList<>();
	
	/**
	 * center of the connectionPoint relative to the host DraggableNode
	 */
	public volatile Point 					connectionPoint 	= new Point(0,0);	//decided by the host node 
	
	/**
	 * preferred connection point icon values. these may not necessarily correlate to a circle
	 */
	public final static int 
		connectionPointRaduis = 12,
		connectionPointBorder = 3;
	protected NodeComponentImportance importance = NodeComponentImportance.SUGGESTED;

	public NodeComponent(Class<T> type, String name, T value) {
		this.type = type;
		this.setName(name);
		this.setValue(value);
		this.setLayout(new FlowLayout());
	}
	
	public abstract NodeSupplier<T> getSupplier();
	
	public NodeConnection<T> makeNewConnection(){
		return new NodeConnection<T>(type);
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
		g.setColor(importance.color);
		g.fillOval(p.x - r/2, p.y - r/2, r, r);
	}
	
	public boolean isInConnectionSeletionRegion(Point p) {
		return this.connectionPoint.distanceSq(p) <= Math.pow(connectionPointRaduis+connectionPointBorder, 2);
	}
	
	public Dimension getConnecitonPointDimensions() {
		int r = connectionPointRaduis+connectionPointBorder;
		return new Dimension(r,r);
	}
	
	public boolean hasConnection() {
		return connections.size() != 0;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String newName) {
		this.name = (newName == null) ? "" : newName;
		
		this.setToolTipText(name);
	}
	
	public NodeComponentImportance getImportance() {
		return importance;
	}

	public void setImportance(NodeComponentImportance importance) {
		if(importance != null)
			this.importance = importance;
	}
	
	public NodeConnection<T> getNewConnection(){
		NodeConnection<T> conn = new NodeConnection<T>(type);
		
		return conn;
	}
}