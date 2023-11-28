package draggableNodeEditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.List;
import java.util.function.Function;

import javax.swing.JComponent;

/**
 * a editable value for the
 * @param <T>
 */
public abstract class NodeComponent<T extends Object> extends JComponent {
	private static final long serialVersionUID = 1L;
	
	//node stuff
	protected String name;
	protected volatile T value = null;
	protected volatile List<NodeConnection<T>> 	connections 		= List.of();
	
	/**
	 * center of the connectionPoint relative to the host DraggableNode
	 */
	public volatile Point 					connectionPoint 	= new Point(0,0);	//decided by the host node 
	
	//meta-ish stuff
	protected volatile boolean 
		needsRedrawn	= true,		
		needsNewValue 	= true;
	
	//UI stuff
	public final static int connectionPointRaduis = 15;
	protected NodeComponentImportance importance = NodeComponentImportance.SUGGESTED;

	public NodeComponent(String name, T value) {
		this.setName(name);
		this.setValue(value);

		this.setLayout(new FlowLayout());
	}
	
	public abstract NodeSupplier<T> getSupplier();
	
	public void drawConnectionPoint(Graphics g, Point p) {
		assert g != null && p != null : "(g is null?"+(g==null)+"),(p is null?"+(p==null)+")";
		
			
		g.setColor(importance.color);
		g.fillOval(p.x, p.y, connectionPointRaduis, connectionPointRaduis);
		
	}
	
	public boolean isInConnectionSeletionRegion(Point p) {
		return this.connectionPoint.distance(p) <= 5;
	}
	
	public boolean hasConnection() {
		return connections.size() != 0;
	}
	
	public Class getType() {
		return value.getClass();
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public List<NodeConnection<T>> getConnections() {
		return connections;
	}

	public void setConnections(List<NodeConnection<T>> connections) {
		this.connections = connections;
	}

	public void addConnections(List<NodeConnection<T>> connections) {
		this.connections.addAll(connections);
	}
	
	public boolean NeedsRedrawn() {
		return needsRedrawn;
	}

	public boolean NeedsNewValue() {
		return needsNewValue;
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
}