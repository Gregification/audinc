package draggableNodeEditor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import draggableNodeEditor.connectionStyles.DirectConnectionStyle;


/**
 * abstraction to make connecting nodes simpler. also doubles as a UI element
 * each connection may have unlimited number of consumers, but only 1 source. The source can be another consumer..
 * 
 * this class is potentially a hot mess, for ensured safety, please only call these functions in the Swing EDT
 * 
 * honestly I'm not quite sure what keeps this thing alive
 *
 * points are relative to the host node
 * 
 * the source is not part of the <code>terminals</code> list 
 */
public class NodeConnection<T> {
	public static final int defaultLineWidth = 5;
	
	public final Class<T> type;
	
	public volatile boolean needsRedrawn = true;
	
	protected ArrayList<TerminalPoint> terminals = new ArrayList<>();
	protected int lineWidth = defaultLineWidth;
	
	private List<Point> linePoints = List.of();
	private ConnectionStyle connectionStyle;
	
	private LinkedBlockingQueue<Point> points;
	private CompletableFuture<Point[]> connectionFuture;

	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	public void genConnection(Rectangle[] obstacles) {
		
	}
	
	public void genConnection(Rectangle[] obstacles, Set<TerminalPoint> terminalsToReconnect) {
				
	}
	
//////////////////////////
// getters & setters
//////////////////////////
	public final List<TerminalPoint> getTerminals(){
		return terminals.stream().toList();
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