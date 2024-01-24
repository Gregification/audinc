package draggableNodeEditor.connectionStyles;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

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
	
	public static Point getBezierCurveAtT(final float t, final LineAnchor[] anchors) {
		float x = 0, y = 0;
		
		long[] coeffs = getBezierCurveCoefficients(t, anchors.length);
		
		for(int i = 0; i < anchors.length; i++) {
			var anc = anchors[i];
			var coe = coeffs[i];
			
			x += anc.x() * coe;
			y += anc.y() * coe;
		}
		
		return new Point((int)x, (int)y);
	}
	
	/**
	 * returns a list of coefficients that when summed gives the point for 't' 
	 * 
	 * @param numPoints to expect
	 * @return
	 */
	//reference -> https://en.wikipedia.org/wiki/B%C3%A9zier_curve	
	public static long[] getBezierCurveCoefficients(final float t, final int numPoints) {
		assert numPoints > 1;
		
		//coefficients
		var c = new long[numPoints]; 
		Arrays.fill(c, 1); 
		
		try(var service = Executors.newVirtualThreadPerTaskExecutor()) {
			final BiConsumer<Integer, Double> applyChange = (index, mult) -> {
				synchronized(c) { 
					c[index] = Math.round(mult * c[index]);
				}
			};
			
			//(1 - t)^(n - i) , 1/(n - i)!
	        service.submit(() -> {
	        	double
	        		omt = 1-t,
	        		omtPow = 1,
	        		nmiFra = 1;
	        	
	        	for(int i = numPoints-2; i >= 0; i--) {
	        		omtPow *= omt;
	        		nmiFra *= i;
	        		
	        		applyChange.accept(i, omtPow / nmiFra);
				}
			});
	        
	        // 1 / i! , n! , t^i
	        service.submit(() -> {
	        	double
	        		nF = MathUtils.factorialDouble(numPoints),
	        		tP = 1,
	        		iF = 1;
	        	
				for(int i = 1; i < numPoints; i++) {
					tP *= t;
					iF *= i;
					
					applyChange.accept(i, nF * tP / iF);
				}
			});
		}
		
		return c;
	}
	
	public float[] getBezierBinomialComponent(int n) {
		var c = new float[n];
		
		final float nF = (float)MathUtils.factorialDouble(n);
		float 
			nMi = 1,
			iF = 1;
		
		c[0] = 1;
		
		for(int i = n-1; i > 0; i++) {
			
		}
				
		return c;
	}
}
