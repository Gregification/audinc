package draggableNodeEditor;

import java.awt.Rectangle;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	
	protected int lineWidth = defaultLineWidth;
	
	private ConnectionStyle connectionStyle;
	
	
	
	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	public void genConnections(Rectangle[] obstacles) {
		
		
	}
	
	public void genConnection(Rectangle[] obstacles, Set<TerminalPoint> terminalsToReconnect) {
		
		
	}
	
//////////////////////////
// getters & setters
//////////////////////////	
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