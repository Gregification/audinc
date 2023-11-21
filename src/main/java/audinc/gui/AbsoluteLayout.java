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
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import draggableNodeEditor.DraggableNode;

public class AbsoluteLayout implements LayoutManager {
	public volatile Point defaultPosition;
	
	/**
	 * padding for nodes, useful for making sure every node has a suitable amount of drag space
	 */
	public volatile int 
		minPaddingTop = 5,
		minPaddingBottom= 3,
		minPaddingLeft	= 3,
		minPaddingRight	= 3;
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
			
//		return new Dimension(
//					Math.max(parent.getWidth(), ret.width),
//					Math.max(parent.getHeight(), ret.height)
//				);
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
					(int)(maxX * positionScale) + maxXOffSet,
					(int)(maxY * positionScale) + maxYOffSet
				);
	}

	@Override public void layoutContainer(Container parent) {
		var parentInset = parent.getInsets();
		
		int
			minOffsetX =  minPaddingLeft + minPaddingRight,
			minOffsetY =  minPaddingTop + minPaddingBottom;
		
		for(Component c : parent.getComponents()) {	
			int 
				offsetX = 0,
				offsetY = 0;
			
			if(c instanceof JComponent) {
				var jc = (JComponent)c;
				var border = jc.getBorder();
				
				if(border == null || !(border instanceof CompoundBorder || border instanceof TitledBorder)) {
					if(jc instanceof DraggableNode)
						border = new TitledBorder(
										jc.getBorder(),
										((DraggableNode)jc).getTitle()
									);
					else
						border = BorderFactory.createCompoundBorder(
										new EmptyBorder(
												minPaddingTop,
												minPaddingLeft,
												minPaddingBottom,
												minPaddingRight),
										jc.getBorder()
									);
					
					jc.setBorder(border);	
				}
				
				var borderInset = border.getBorderInsets(c);
				offsetX += borderInset.left + borderInset.right;
				offsetY += borderInset.top + borderInset.bottom;
			}
			
			var pSize = c.getPreferredSize();
			c.setBounds(
						(int)(c.getX() * positionScale) + parentInset.left, 	//x
						(int)(c.getY() * positionScale) + parentInset.right, 	//y
						pSize.width + 	Math.max(minOffsetX, offsetX),							//width
						pSize.height + 	Math.max(minOffsetY, offsetY)							//height
					);
		}
	}
}
