package draggableNodeEditor.NodeConnectionDrawer;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
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
	
	private CompletableFuture<BufferedImage> imageFuture = null;

	private Point offset = new Point(0,0);
	
	/**  property fired once when image has finished being drawn.
	 * new property value => raster of the changed data
	 * old property value => Point representing the offset
	 */
	public static final String PropertyChange_ImageReady = "PropertyChange_ImageReady";
	/**  property fired every time a region of the image changes
	 * new property value => raster of the changed data
	 * old property value => rectangle of the area the raster matches
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
    	if(imageFuture != null) {
    		if(imageFuture.cancel(true))
    			System.out.println("-----------------------cancel \t" + imageFuture.hashCode() + " : " + System.nanoTime());
    	}
    	
		imageFuture = CompletableFuture.supplyAsync(() -> {
					System.out.println("#################################START \t" + imageFuture.hashCode() + " : " + System.nanoTime());
					offset.x = offset.y = 0;
					
					var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					draw(
							img,
							anchors,
							terminals,
							obstacles,
							(rect, region) -> pcs.firePropertyChange(ConnectionStyle.PropertyChange_ImageUpdate, rect, region)
						);
					System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&END \t" + imageFuture.hashCode() + " : " + System.nanoTime());
					
					var cropRec = ConnectionStyle.cropOpaqueContent(img);
					offset.x = cropRec.x;
					offset.y = cropRec.y;
					
	//				img = img.getSubimage(cropRec.x, cropRec.y, cropRec.width, cropRec.height);
					return img;
				})
				.thenApply(img -> {
						//signal end of drawing
						pcs.firePropertyChange(ConnectionStyle.PropertyChange_ImageReady, offset, img);
						return img;
					})
				;
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
			BiConsumer<Rectangle, Raster> signalUpdate
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

	/**
	 * gets the corresponding rectangle from the given 2 coordinate points
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return rectangle that covers the enclosed area 
	 */
	public static Rectangle getRect(int x1, int y1, int x2, int y2) {
		int l, r, t, b; //left, right, top, bottom
		
		if(x1 < x2) {
			l = x1;
			r = x2;
		}else {
			l = x2;
			r = x1;
		}
		
		if(y1 < y2) {
			t = y1;
			b = y2;
		} else {
			t = y2;
			b = y1;
		}
		
		return new Rectangle(l, t, r - l, b - t);
	}
	
	@Override public String toString() {
		return getClass().getCanonicalName() + "[imageFuture:" + imageFuture + "]" ;
	}

	public Point getOffset() {
		return offset;
	}

	public void setOffset(int x, int y) {
		offset.x = x;
		offset.y = y;
	}
	
	public CompletableFuture<BufferedImage> getImageFuture() {
		return imageFuture;
	}
}
