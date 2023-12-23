package draggableNodeEditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import draggableNodeEditor.connectionStyles.DirectConnectionStyle;

/**
 * connects one NodeSupplier to many NodeConsumers
 * 
 * honestly I'm not quite sure what keeps this thing alive
 * 
 * the source is not part of the <code>terminals</code> list 
 */
public class NodeConnection<T> {
	public static final int defaultLineWidth = 5;
	
	public final Class<T> type;
	
	public volatile boolean needsRedrawn = true;
	
	protected ArrayList<AnchorPoint> anchors = new ArrayList<>();
	
	protected int lineWidth = defaultLineWidth;
	
	private List<Point> linePoints = List.of();
	private ConnectionStyle connectionStyle;
	
	private CompletableFuture<Point[]> connectionFuture;

	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	public void genConnection(final Rectangle[] obstacles) {
		
	}
	
//////////////////////////
// getters & setters
//////////////////////////
	public final List<AnchorPoint> getAnchors(){
		return anchors.stream().toList();
	}
	
	public final List<Point> getLinePoints(){
		return this.linePoints;
	}
	
	public final CompletableFuture<Point[]> getPointFuture(){
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