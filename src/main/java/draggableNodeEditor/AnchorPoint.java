package draggableNodeEditor;
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import presentables.Presentable;

/**
 * a cheesy way to make anchor points
 * 
 * TODO: some Swing shenangains. the inspector panel vertically centers the options. make it such that the content starts at the top right corner instead of the center. 
 */
public class AnchorPoint extends DraggableNode<Void> implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -1554483802522425955L;
	
	public final static int 
		pointDiameter 			= 12,
		pointBorderThickness 	= 3,
		biasWidth				= 5;
	
	public static Color 
		defaultPointColor 		= Color.blue,
		defaultPointBorderColor	= Color.black; 
	 
	public Color 
		pointColor 		 = AnchorPoint.defaultPointColor,
		pointBorderColor = AnchorPoint.defaultPointBorderColor;
	
	/**
	 * a arbitrary weight value assigned to this node, for lines to use. 
	 * does not have to be normalized.
	 * default is 1.
	 */
	public float weight = 1;
	protected Point2D 
		incommingBias 	= new Point2D.Float(0,0),
		outgoingBias	= new Point2D.Float(0,0);
	
	protected DraggableNodeEditor nodeEditor;
	
	public AnchorPoint() {
		super(null);
		
		this.setBorder(null);
		
		int diameter = pointDiameter + pointBorderThickness;
		var dim = new Dimension(diameter+2, diameter+2);
		this.setPreferredSize(dim);
		
		this.setBackground(new Color(0,0,0,0));
		this.setOpaque(true);
	}
	
	public Point getPoint() {
		var bounds = this.getBounds();
		return new Point((int)bounds.getCenterX(), (int)bounds.getCenterY());
	}

	@Override public String getTitle() {
		return "anchor point (" + this.index + ")";
	}

	@Override public void initNode(DraggableNodeEditor editor) {
		nodeEditor = editor;
	}

	@Override public JComponent getInspector() {
		JPanel 
			content = new JPanel(),
			wrapper_bias = new JPanel(new GridBagLayout());
		
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		wrapper_bias.setBorder(new TitledBorder(
				null,
				"line bias (x, y)"
			));
		
		Function<Double, SpinnerNumberModel>	newModel = (num) 	-> new SpinnerNumberModel((double)num,Integer.MIN_VALUE, Integer.MAX_VALUE, .2f);
		Function<JSpinner, Double>			getValue = (s)	-> (Double)s.getValue();
		JSpinner //bias for X and Y of i/o
			ibx = new JSpinner(newModel.apply(incommingBias.getX())),		
			iby = new JSpinner(newModel.apply(incommingBias.getY())),		
			obx = new JSpinner(newModel.apply(outgoingBias.getX())),		
			oby = new JSpinner(newModel.apply(outgoingBias.getY()));
		ibx.addChangeListener(e -> incommingBias.setLocation(getValue.apply(ibx) ,	incommingBias.getY()));
		iby.addChangeListener(e -> incommingBias.setLocation(incommingBias.getX(), 	getValue.apply(iby)));
		obx.addChangeListener(e -> outgoingBias.setLocation(getValue.apply(obx)  ,	outgoingBias.getY()));
		oby.addChangeListener(e -> outgoingBias.setLocation(outgoingBias.getX()	 ,	getValue.apply(oby)));
		
		JCheckBox mouseBiasSelectionToggler = new JCheckBox("use mouse to select", List.of(getMouseListeners()).contains(this));//is mouse listener already registered?
			mouseBiasSelectionToggler.addItemListener(l -> enableMouseBiasSelection(l.getStateChange() == ItemEvent.SELECTED));
			mouseBiasSelectionToggler.setToolTipText("work in progress (aka does not work right now)");
		
		BiFunction<Integer, Integer, GridBagConstraints>	gc = (x,y) -> {var c = Presentable.createGbc(x, y); c.weighty = 0; return c;};
		
		int x = 0, y = 0;
		wrapper_bias.add(mouseBiasSelectionToggler,	gc.apply(x,   y++));		
		wrapper_bias.add(new JLabel("incomming"), 	gc.apply(x++, y));
		wrapper_bias.add(ibx, 						gc.apply(x++, y));
		wrapper_bias.add(iby, 						gc.apply(x++, y++));
		x=0;
		wrapper_bias.add(new JLabel("outgoing"), 	gc.apply(x++, y));
		wrapper_bias.add(obx, 						gc.apply(x++, y));
		wrapper_bias.add(oby, 						gc.apply(x,   y++));
		
		x=y=0;
		content.add(wrapper_bias);
		content.setMaximumSize(content.getMinimumSize());
		return content;
	}
	
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int 
			R =	pointDiameter + 2 * pointBorderThickness;
		
		g.setColor(pointBorderColor); 
		g.fillOval(0, 0, R, R);
		g.setColor(pointColor);
		g.fillOval(pointBorderThickness,pointBorderThickness, pointDiameter, pointDiameter);
	}
	
	@Override public void mouseClicked(MouseEvent e) 	{}
	@Override public void mousePressed(MouseEvent e) 	{
		if(SwingUtilities.isRightMouseButton(e)) {
			this.isDraggable = false;
			//... bias selection with mouse. logic would go here
		}
	}
	@Override public void mouseReleased(MouseEvent e)	{
		this.isDraggable = true;
	}
	@Override public void mouseEntered(MouseEvent e) 	{}
	@Override public void mouseExited(MouseEvent e)	 	{}
	@Override public void mouseDragged(MouseEvent e) 	{}
	@Override public void mouseMoved(MouseEvent e) 		{}
	
	@Override public boolean onOffClick(MouseEvent me, DraggableNode<?> oNode, NodeComponent<?> oC) {
		if(oNode != null && oNode != this) {	//on deselect
			enableMouseBiasSelection(false);
		}
		
		return true;
	}
	
	public float getIncommingBiasX() { return (float) incommingBias.getX(); }
	public float getIncommingBiasY() { return (float) incommingBias.getY(); }
	public float getOutgoingBiasX()  { return (float) outgoingBias.getX();	}
	public float getOutgoingBiasY()  { return (float) outgoingBias.getY();	}
	
	public void setIncommingBias(Point2D incommingBias) {
		this.incommingBias = incommingBias;
	}

	public void setOutgoingBias(Point2D outgoingBias) {
		this.outgoingBias = outgoingBias;
	}
	
	//cheese hack to make the mouseListeners work
	private void enableMouseBiasSelection(boolean on) {		
		if(on) {
			addMouseListener(this);
			addMouseMotionListener(this);
			
			addMouseListener(nodeEditor);
			addMouseMotionListener(nodeEditor);
		}else {
			removeMouseMotionListener(this);
			removeMouseListener(this);
			
			removeMouseMotionListener(nodeEditor);
			removeMouseListener(nodeEditor);
		}
	}
}
