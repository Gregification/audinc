package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import org.apache.commons.math.util.MathUtils;

import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;

/**
 * non interpolated
 */
public class BezierSplineConnectionStyle extends ConnectionStyle{
	private static final long serialVersionUID = -3403380589063068913L;
	
	@Override
	protected void draw(
			BufferedImage bf,
			LineAnchor[] anchors,
			LineAnchor[] terminals,
			Shape[] obstacles,
			BiConsumer<Rectangle, Raster> signalUpdater) {
		
		//from terminals[i] to [i + 1] considering anchors as
		for(int i = 0; i < terminals.length - 1; i++) {
			LineAnchor tsrc = terminals[i], tend = terminals[i+1];
			
			//divide and conquer
			try(var service = Executors.newVirtualThreadPerTaskExecutor()) {
				
			}
		}
		
	}	
	
	@Override
	public void dispose() {
		super.dispose();
		
		//stop futures
	}	
	
	public static Point getBezierCurveAtT(float t, final LineAnchor[] anchors) {
		float[] coeffs = getBezierCurveCoefficients(t, anchors.length);
		return getBezierCurveAtT(t, anchors, coeffs);
	}
	public static Point getBezierCurveAtT(float t, final LineAnchor[] anchors, final float[] coeffs) {
		float x = 0, y = 0;
		
		for(int i = 0; i < coeffs.length; i++) {
			var anc = anchors[i];
			var coe = coeffs[i];
			
			x += anc.x() * coe;
			y += anc.y() * coe;
		}
		
		return new Point((int)x, (int)y);
	}
	
	/**
	 * returns a list of coefficients that when summed gives the point for 't' 
	 * brute forces it
	 * @param numPoints to expect
	 * @return
	 */
	//reference -> https://en.wikipedia.org/wiki/B%C3%A9zier_curve	
	public static float[] getBezierCurveCoefficients(float t, int numPoints) {
		assert numPoints > 1;
		
		//coefficients
		float[] c = new float[numPoints];
		
		IntStream.range(0,  c.length).parallel()
			.forEach(i -> {
				double val = MathUtils.binomialCoefficientDouble(numPoints, i) * Math.pow(t, i) * Math.pow(1-t, numPoints - i);
				
				c[i] = (float)val;
			});
		
		return c;
	}
}
