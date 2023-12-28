package draggableNodeEditor.NodeConnectionDrawer;

import java.awt.Shape;
import java.awt.image.BufferedImage;
import draggableNodeEditor.connectionStyles.DirectConnectionStyle;

/**
 * line drawing interface
 * see here for generic overview -> https://nick-lab.gs.washington.edu/java/jdk1.5b/guide/2d/spec/j2d-image.html
 * 	- note 5.42
 * see here for graphic2d info -> https://docs.oracle.com/javase/8/docs/technotes/guides/2d/spec/j2d-awt.html
 */
public interface ConnectionStyle {	
	public static ConnectionStyle getDefaultConnectionsStyle() {
		return new DirectConnectionStyle();
	}
	
	/**
	 * generates the path, point by point, between all terminal nodes. 
	 * the final terminal node will not get a path made for it.
	 * 
	 * it is suggested that, the parameter, onNewPoints be used to provide live updates.  
	 * 
	 * assume to be processing intensive.
	 * 
	 * @param bf : the image to draw the line on.
	 * @param output : the actual output of this function, Points will be pushed onto the queue as they're calculated 
	 * @param anchors : points that may effect where the line is drawn 
	 * @param terminals: points where the line is guaranteed to cross. the first element is the origin. 
	 * @param obstacles : regions the lines will try not to cross   
	 */
	public abstract void draw(
			BufferedImage bf,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Shape[] obstacles
		);
	
//	static void a(BufferedImage bf, DraggableNodeEditor dne, Canvas can) {
//		var raster = bf.getRaster();
//		
//		var v = ((DataBufferInt)raster.getDataBuffer()).getData();
//	}
	
	//unused. too complicated and little reward. would require some sort of way to map what terminals effect what points, and some thread safe accessing of that data
	/**
	 * redraws the connections for only the given nodes.
	 * 
	 * a better option than redrawing all connections, however depending on the ConnectionStyle this may not be much better.
	 * 
	 * assume to be processing intensive
	 *  
	 * @param nodeConnection : source of connection information
	 * @param obstacles : regions the lines are not to cross 
	 * @param terminalsToReconnect : specific terminals to recalculate
	 */
	//public abstract <T> CompletableFuture<LinkedBlockingQueue<Point>> genConnection(LinkedBlockingQueue<Point> output, final List<TerminalPoint> terminals, final Rectangle[] obsticals, final Set<TerminalPoint> terminalsToReconnect);
}
