package audinc.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;

public class AbsoluteLayout implements LayoutManager {
	public volatile Point defaultPosition;
	public volatile int padding = 10;
	public volatile float positionScale = 1f;
	
	public AbsoluteLayout() {
		
	}
	
	public AbsoluteLayout(Point defaultPosition) {
		this.defaultPosition = defaultPosition;
		
	}
	
	@Override public void addLayoutComponent(String name, Component comp) {
		
	}

	@Override public void removeLayoutComponent(Component comp) {
		
	}

	/**
	 * find the size of the largest element, then +padding. if no child components are present, 
	 * the preferred size is just the padding.
	 * @param parent
	 * @return
	 */
	@Override public Dimension preferredLayoutSize(Container parent) {		
		var ret = this.minimumLayoutSize(parent);
			ret.width += padding;
			ret.height += padding;
			
		return ret;
			
//		return new Dimension(
//					Math.max(parent.getWidth(), ret.width),
//					Math.max(parent.getHeight(), ret.height)
//				);
	}

	/**
	 * gets the largest X and Y possible from any component, accounting for the positioning scale.
	 * @param parent
	 * @return
	 */
	@Override public Dimension minimumLayoutSize(Container parent) {
		int 
			maxX = 0, maxY = 0,
			maxXOffSet = 0, maxYOffSet = 0,
			t;
	
		for(Component c : parent.getComponents()) {
			if((t = c.getX()) >= maxX) {
				maxX = t;
				maxXOffSet = Math.max(maxXOffSet, c.getWidth());
			}
			if((t = c.getY()) >= maxY) {
				maxY = t;
				maxYOffSet = Math.max(maxYOffSet, c.getHeight());
			}
		}
		
		return new Dimension(
					(int)(maxX * positionScale) + maxXOffSet,
					(int)(maxY * positionScale) + maxYOffSet
				);
	}

	@Override public void layoutContainer(Container parent) {
		var parentInset = parent.getInsets();
		
		for(Component c : parent.getComponents()) {
			var pSize = c.getPreferredSize();
			c.setBounds(
						(int)(c.getX() * positionScale) + parentInset.left, 	//x
						(int)(c.getY() * positionScale) + parentInset.right, 	//y
						pSize.width,						//width
						pSize.height						//height
					);
		}
	}
}
