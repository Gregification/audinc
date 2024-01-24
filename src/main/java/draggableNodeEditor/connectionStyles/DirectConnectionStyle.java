package draggableNodeEditor.connectionStyles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle extends ConnectionStyle{
	private static final long serialVersionUID = -1155002155261577282L;
	private static final byte nodesBetweenUpdate = 3;
	
	public volatile boolean 			//TODO
		objsticalNavication = false,
		considerAnchors 	= false;
	
	public static volatile int
		strokeSize = 5; 
	
	@Override public void draw(
			BufferedImage bf,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Shape[] obstacles,
			BiConsumer<Rectangle, Raster> signalUpdate) {
		
		if(terminals.length < 2) return;
		
		//graphics
		Graphics2D g;
		Supplier<Graphics2D> newG = () -> {
			var gphic = bf.createGraphics();
			gphic.setColor(Color.red);
			gphic.setStroke(new BasicStroke(strokeSize));
			return gphic;
		};
		g = newG.get();
		
		//edited area tracker
		var rect = new Rectangle(0,0,0,0);
		
		LineAnchor f = terminals[0];
		for(int i = 0; i < terminals.length; i++) {
			if(Thread.currentThread().isInterrupted()) return;
			
			var t = terminals[i];
			
			//actually drawing the line
			g.drawLine(f.x(), f.y(), t.x(), t.y());
			
			//determining what to update
			//update area thats been changed
			rect.add(ConnectionStyle.getRect(f.x(), f.y(), t.x(), t.y()));
			
			//signal for update if wanted
			if(i % nodesBetweenUpdate == 0) {
				
				//synchronize graphics
				g.dispose();
				
				var finalRect = rect.intersection(bf.getData().getBounds()); //corrects for overlap(can be caused by misuse of Rectangle.add, bad line calculation, etc )
				
				if(!(finalRect.width == 0 || finalRect.height == 0))
					signalUpdate.accept(rect, bf.getData(finalRect));
				
				rect = new Rectangle(0,0,0,0);
				
				g = newG.get();
			}
			f = t;
		}
	}
}
