package draggableNodeEditor.connectionStyles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.function.Consumer;

import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle extends ConnectionStyle{
	private static final long serialVersionUID = -1155002155261577282L;

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
			Consumer<Raster> signalUpdate) {
		
		if(terminals.length < 2) return;
		
		var g = bf.createGraphics();
		g.setColor(Color.red);
		g.setStroke(new BasicStroke(strokeSize));
				
		LineAnchor f = terminals[0];
		for(var t : terminals) {
			g.drawLine(f.x(), f.y(), t.x(), t.y());
			
//			signalUpdate.accept(bf.getData(new Rectangle(f.x(), f.y(), t.x(), t.y())));
			
			f = t;
		}
		g.dispose();
	}
}
