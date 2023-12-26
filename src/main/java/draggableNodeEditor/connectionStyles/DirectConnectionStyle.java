package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Polygon;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;

import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;
import draggableNodeEditor.NodeConnectionDrawer.WeightedPoint;

/**
 * a straight line between each terminal.
 */
public class DirectConnectionStyle implements ConnectionStyle{
	public volatile boolean 			//TODO
		objsticalNavication = false,
		considerAnchors 	= false;

	@Override public CompletableFuture<PriorityBlockingQueue<WeightedPoint>> genConnections(
			PriorityBlockingQueue<WeightedPoint> output,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Polygon[] obstacles) {
		return CompletableFuture.supplyAsync(() -> {
			//ignore everything
			//draw line directly from terminal to terminal
			int i = 0;
			for(var t : terminals) {
				output.add(new WeightedPoint(i, new Point(t.x(), t.y())));
				i++;
			}
			
			return output;
		});
	}
}
