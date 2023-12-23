package audinc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class AbsoluteLayout implements LayoutManager {
	public volatile Point defaultPosition;
	
	private ArrayList<WeakReference<Component>> uninfluenceableComponents = new ArrayList<>();
	
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
	
	public ArrayList<Component> getUninfluenceableComponents(){
		ArrayList<Component> ret = new ArrayList<>();
		for(var itt = uninfluenceableComponents.listIterator(); itt.hasNext();) {
			WeakReference<Component> wr = itt.next();
			Component comp = wr.get();
			
			if(comp != null)
				ret.add(comp);
			else
				itt.remove();
		}
		return ret;
	}
	
	public void addUninfluenceableComponent(Component comp) {
		uninfluenceableComponents.add(new WeakReference<Component>(comp));
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
			tPos, tOff, diff = 0;
		
		ArrayList<Component> 
			south 	= new ArrayList<>(),
			east 	= new ArrayList<>();
		
		Component[] comps = parent.getComponents();
		if(comps.length == 1) {
			var c = comps[0];
			var bnd = c.getBounds();
			
			maxX = (int)bnd.getX();
			maxY = (int)bnd.getY();
			maxXOffSet = (int)bnd.getWidth();
			maxYOffSet = (int)bnd.getHeight();
		}
		
		for(Component c : comps) {
			if((tPos = c.getX()) >= maxX) {
				tOff = c.getWidth();
				
				diff = (tPos+tOff) - (maxX+maxXOffSet);
				if(diff < 0) //get the farthest out
					maxXOffSet = (maxX+maxXOffSet) - tPos;
				else
					maxXOffSet = tOff;
				
				maxX = tPos;
				
				if(diff != 0) east.clear();
				
				east.add(c);
			}
			if((tPos = c.getY()) >= maxY) {
				tOff = c.getHeight();
				
				diff = (tPos+tOff) - (maxY+maxYOffSet);
				if(diff < 0) //get the farthest out
					maxYOffSet = (maxY+maxYOffSet) - tPos;
				else
					maxYOffSet = tOff;
				
				maxY = tPos;
				
				if(diff == 0) south.clear();
				
				south.add(c);
			}
		}
		
		int padX, padY;
		
		List<Component> nonoList = getUninfluenceableComponents();
		if(nonoList.containsAll(east)) padX = 0;
		else padX = areaPaddingTop  + areaPaddingBottom;
		
		if(nonoList.containsAll(south)) padY = 0;
		else padY = areaPaddingLeft + areaPaddingRight;
		
//		System.out.println("absolute layout > @Override , min layout size , "
//				+ "\n\tsouth: \t\t" + south
//				+ "\n\teast: \t\t" + east
//				+ "\n\tnonolist: \t" + nonoList
//				+ "\n\tpadX: \t\t" + padX
//				+ "\n\tpadY: \t\t" + padY
//				+ "\n\tmax off set X: \t" + maxXOffSet
//				+ "\n\tmax off set Y: \t" + maxYOffSet
//				+ "\n\tdiff: \t\t" + diff);
		
		return new Dimension(
					(int)(maxX * positionScale) + maxXOffSet + padX,
					(int)(maxY * positionScale) + maxYOffSet + padY
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
