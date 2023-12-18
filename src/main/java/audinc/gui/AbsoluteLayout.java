package audinc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class AbsoluteLayout implements LayoutManager {
	public volatile Point defaultPosition;
	
	/**
	 * padding for nodes, useful for making sure every node has a suitable amount of drag space
	 */
	public volatile int 
		minNodePaddingTop 	= 5,
		minNodePaddingBottom= 1,
		minNodePaddingLeft	= 1,
		minNodePaddingRight	= 1;
	
	/**
	 * padding for the area this layout is applied to. padding is applied to each side(top,left,bottom,right).
	 * this does not effect node placement.
	 */
	public volatile int
		areaPaddingTop 		= 30,
		areaPaddingBottom 	= 30,
		areaPaddingLeft		= 30,
		areaPaddingRight 	= 30;
	
	
	public volatile Color
		paddingHighlightColor 	= Color.DARK_GRAY,
		paddingShadowColor		= null;
	
	public volatile Dimension minSize = new Dimension(10,10);
	
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
			ret.width 	= Math.max(ret.width,	minSize.width);
			ret.height 	= Math.max(ret.height,	minSize.height);
			
		return ret;
	}

	/**
	 * gets the largest X and Y (independently) possible of the children components, accounting for the positioning scale.
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
					(int)(maxX * positionScale) + maxXOffSet + areaPaddingTop  + areaPaddingBottom,
					(int)(maxY * positionScale) + maxYOffSet + areaPaddingLeft + areaPaddingRight
				);
	}

	@Override public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			var parentInset = parent.getInsets();
			
			int
				minOffsetX =  minNodePaddingLeft + minNodePaddingRight,
				minOffsetY =  minNodePaddingTop + minNodePaddingBottom;
			
			for(int i = 0, nmembers = parent.getComponentCount(); i < nmembers; i++) {
				Component c = parent.getComponent(i);
				
				if(c.isVisible()) {
					int 
						offsetX = 0,
						offsetY = 0;
					
					if(c instanceof JComponent jc) {
						var border = jc.getBorder();				
						
						if(border != null) {
							var borderInset = border.getBorderInsets(c);
							
							offsetX += borderInset.left + borderInset.right;
							offsetY += borderInset.top + borderInset.bottom;
						}
					}
					
					var pSize = c.getPreferredSize();
					
					int 
						x = (int)(c.getX() * positionScale) + parentInset.left,
						y = (int)(c.getY() * positionScale) + parentInset.right,
						w = pSize.width	 +	Math.max(minOffsetX, offsetX),
						h = pSize.height + 	Math.max(minOffsetY, offsetY);
					
					c.setBounds(x, y, w, h);
				}
			}
		}
	}
}
