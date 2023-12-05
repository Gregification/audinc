package draggableNodeEditor;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	protected static final ExecutorService connectionCalculatorExecutorService;
	public static final int defaultLineWidth = 5;
	
	public final Class<T> type;
	
	protected int lineWidth = defaultLineWidth; 
	protected ConnectionStyle connectionStyle;
	
	static {
		connectionCalculatorExecutorService = Executors.newCachedThreadPool();
	}
	
	public NodeConnection(Class<T> type) {
		super();
		
		this.type = type;
		setConnectionStyle(null);
	}
	
	public TerminalPoint<T> makeValidTerminal(){
		return new TerminalPoint<T>(type);
	}
	
	public boolean needsRepathed() {
		for(var v : this)
			if(v.needsRepathed) return true;
		
		return false;
	}
	
	public TerminalPoint<T> getRoot(){
		if(this.isEmpty()) return null;
		
		return this.getFirst();
	}
	
	public void genConnections(Rectangle[] obstacles) {
		connectionCalculatorExecutorService.execute(()->{
			connectionStyle.genConnections(this, obstacles);
		});
	}
	
	public void genConnection(Rectangle[] obstacles, Set<TerminalPoint<T>> terminalsToReconnect) {
		connectionCalculatorExecutorService.execute(()->{
			connectionStyle.genConnection(this, obstacles, terminalsToReconnect);
		});
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
	
	public void paint(Graphics g) {
		if(this.isEmpty()) return;
		
		for(var terminal : this) {
			int 
				x = terminal.pathToNext[0].x,
				y = terminal.pathToNext[0].y;
			
			for(var point : terminal.pathToNext) {
				g.drawLine(x, y, point.x, point.y);
				x = point.x;
				y = point.y;
			}
		}
	}
}