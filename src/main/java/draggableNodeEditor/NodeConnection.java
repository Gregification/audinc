package draggableNodeEditor;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import draggableNodeEditor.connectionStyles.DirectConnectionStyle;


/**
 * abstraction to make connecting nodes simpler. also doubles as a UI element
 * each connection may have unlimited number of consumers, but only 1 source. The source can be another consumer..
 * 
 * this class is potentially a hot mess, for ensured safety, please only call these functions in the Swing EDT
 * 
 * honestly I'm not quite sure what keeps this thing alive, but it'll sometimes gives thread related errors if the "volatile" decelerations are removed.
 * 
 * the TerminalPoints are not updated when their added to this
 * 
 * the source is not part of the <code>terminals</code> list 
 */
public class NodeConnection<T> extends LinkedBlockingDeque<TerminalPoint<T>>{
	private static final long serialVersionUID = -2495316406627258166L;
	
	public static final int defaultLineWidth = 5;
	
	public final Class<T> type;
	protected int lineWidth = defaultLineWidth; 
	
	/**
	 * the type of line between the terminals
	 */
	protected volatile ConnectionStyle connectionStyle;
	
//	private final ReentrantLock connectionLock = new ReentrantLock(true);
	
	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	public TerminalPoint<T> makeValidTerminal(){
		return new TerminalPoint<T>(type);
	}
	
	public TerminalPoint<T> getRoot(){
		if(this.isEmpty()) return null;
		
		return this.getFirst();
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