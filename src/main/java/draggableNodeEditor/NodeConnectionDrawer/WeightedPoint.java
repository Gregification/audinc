package draggableNodeEditor.NodeConnectionDrawer;

import java.awt.Point;

public record WeightedPoint(double weight, Point point) implements Comparable<WeightedPoint>{
	@Override public int compareTo(WeightedPoint other) {
		return Double.compare(weight, other.weight);
	}
	
}
