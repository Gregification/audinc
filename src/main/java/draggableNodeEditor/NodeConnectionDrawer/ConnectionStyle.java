package draggableNodeEditor.NodeConnectionDrawer;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import draggableNodeEditor.connectionStyles.DirectConnectionStyle;

/**Java Bean
 * handles line drawing for NodeConnection.
 * 
 * see here for generic overview -> https://nick-lab.gs.washington.edu/java/jdk1.5b/guide/2d/spec/j2d-image.html
 * 	- note 5.42
 * see here for graphic2d info -> https://docs.oracle.com/javase/8/docs/technotes/guides/2d/spec/j2d-awt.html
 */
public abstract class ConnectionStyle implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private CompletableFuture<Void> imageFuture = null;

	/**  property fired once when image has finished being drawn 
	 */
	public static final String PropertyChange_ImageReady = "PropertyChange_ImageReady";
	/**  property fired every time a region of the image changes  
	 */
	public static final String PropertyChange_ImageUpdate= "PropertyChange_ImageUpdate";
	
	public static ConnectionStyle getDefaultConnectionsStyle() {
		return new DirectConnectionStyle();
	}
    
    public void dispose() {
    	if(imageFuture != null && !imageFuture.isDone()) imageFuture.cancel(true);
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
    public void drawConnection(
    		int width,
    		int height,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Shape[] obstacles,
			PropertyChangeSupport pcs){
    	if(imageFuture != null && !imageFuture.isDone()) {
    		imageFuture.cancel(true);
    		System.out.println("-----------------------cancel \t" + imageFuture.hashCode() + " : " + System.nanoTime());
    	}
    	
		imageFuture = CompletableFuture.supplyAsync(() -> {
				System.out.println("#################################START \t" + imageFuture.hashCode() + " : " + System.nanoTime());
				var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				draw(
						img,
						anchors,
						terminals,
						obstacles,
						(region) -> pcs.firePropertyChange(ConnectionStyle.PropertyChange_ImageUpdate, null, region)
					);
				System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&END \t" + imageFuture.hashCode() + " : " + System.nanoTime());
				
//				var cropRec = ConnectionStyle.cropOpaqueContent(img);
//				img = img.getSubimage(cropRec.x, cropRec.y, cropRec.width, cropRec.height);
				//signal end of drawing
				pcs.firePropertyChange(ConnectionStyle.PropertyChange_ImageReady, null, img.getData());
				return null;
			});
    }
    
//    private void signalImageComplete(BufferedImage image) {
//    	pcs.firePropertyChange(ConnectionStyle.PropertyChange_ImageReady, null, image);	
//    }
    
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
	protected abstract void draw(
			BufferedImage bf,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Shape[] obstacles,
			Consumer<Raster> signalUpdate
		);
	
	public static Rectangle cropOpaqueContent(BufferedImage bf) {
		return new Rectangle(0,0,bf.getWidth(),bf.getHeight());
		/*
		int[] p = ((DataBufferInt)bf.getRaster().getDataBuffer()).getData();
		int
			ix = bf.getWidth(),
			iy = bf.getHeight(),
			minx = 0,
			miny = 0,
			maxx = 0,
			maxy = 0,
			alpha;
		*/
//		brute force
		/*
		for(int x = 0; x < ix; x++) {
			for(int y = 0; y < iy; y++) {
				alpha = ((p[minx * ix + y] >> 24) & 0xff);
				
				if(alpha != 0) {
					minx = Math.min(minx, x);
					miny = Math.min(miny, y);
					maxx = Math.max(maxx, x);
					maxy = Math.max(maxy, y);
				}
			}
		}
		*/
		
//		
//		//find minX
//		for(minx = 0; minx < ix; minx++) {
//			for(int y = 0; y < iy; y++) {
//				alpha = ((p[minx * ix + y] >> 24) & 0xff);
//				
//				if(alpha != 0) {
//					miny = maxy = y;
//					break;
//				}
//			}
//		}
//		
//		//find maxX
//		for(maxx = ix; maxx > minx; maxx--) {
//			for(int y = iy; y >= 0; y--) {
//				alpha = ((p[minx * ix + y] >> 24) & 0xff);
//				
//				if(alpha != 0) {
//					miny = Math.min(miny, y);
//					maxy = Math.max(maxy, y);
//					break;
//				}
//			}
//		}
//		
//		//confirm minY
//		for(int x = minx; x < maxx; x++) {
//			for(int y = 0; y < miny; y++) {
//				alpha = ((p[x * ix + y] >> 24) & 0xff);
//				
//				if(alpha != 0) {
//					miny = y;
//					break;
//				}
//			}
//		}
//		
//		//confirm maxY
//		for(int x = minx; x < maxx; x++) {
//			for(int y = iy; y > maxy; y--) {
//				alpha = ((p[x * ix + y] >> 24) & 0xff);
//				
//				if(alpha != 0) {
//					maxy = y;
//					break;
//				}
//			}
//		}
		
		//yippie
		
//		return new Rectangle(minx, miny, Math.max(1, maxx - minx), Math.max(1, maxy - miny));
	}
	
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

	@Override public String toString() {
		return getClass().getCanonicalName() + "[imageFuture:" + imageFuture + "]" ;
	}
}
