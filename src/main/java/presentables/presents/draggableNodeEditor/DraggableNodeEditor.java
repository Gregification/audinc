package presentables.presents.draggableNodeEditor;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.io.Serializable;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import audinc.gui.MainWin;
/**
 * GUI editor for draggable and linkable nodes. encouraged to inherit this class for more specialized uses
 * <p>
 * <b>to use :</b>  this class inherits JPanel and displays the main content (where nodes are dragged around)
 * there are 2 JPanels (get using {@code getEditorPanel()} and {@code getIndexPanel}) that are also part of the UI
 * @implNote Serializable
 */
public class DraggableNodeEditor extends JLayeredPane implements MouseListener, MouseMotionListener, Serializable {
	public static final int 
		topLayer = 5,
		defaultLayer = 2;
	public JToolBar editorToolBar;
	
	protected JScrollPane editorScrollPane;
	protected final int canvasLayer = 0;
	protected JPanel inspectorPanel;	//options related to a single node. view & edit details about a selected node
	
	//mouse events
	protected Point dragOffSet;
	protected DraggableNode dragN;		//the current node being dragged. "dragN":pronounced like drag-N-dez-... 
	
	private static final long serialVersionUID = 1L;
	
	public DraggableNodeEditor(JPanel inspector, JToolBar index) {
		this.editorToolBar = index;
		this.inspectorPanel = inspector;
	}
	public DraggableNodeEditor() {
		this(new JPanel(), new JToolBar());
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		editorScrollPane = new JScrollPane(this,	
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		genGUI();
	}
	
	/**
	 * this just calls the 3 functions <code>genGUI inspector-index-editor</code> in that order 
	 */
	public void genGUI() {		
		genGUI_inspector(inspectorPanel);
		genGUI_editor();
	}
	
////////////////////////////////
//mouse events
////////////////////////////////
	@Override public void mouseDragged(MouseEvent e) {
		if(dragN == null) return;
		
		assert dragOffSet != null 
				: "failed to be initialized";
		
		if(this.getBounds().contains(e.getPoint())) {
			Point newLocation = e.getPoint();
				newLocation.x -= dragOffSet.x;
				newLocation.y -= dragOffSet.y;
			newLocation.x = Math.max(newLocation.x, 0);	//lower	bound
			newLocation.y = Math.max(newLocation.y, 0);	//upper bound
			newLocation.x = Math.min(newLocation.x, this.getWidth() - dragN.getWidth());	//right bound	
			newLocation.y = Math.min(newLocation.y, this.getHeight() - dragN.getHeight());	//bottom bound
			
			dragN.setLocation(newLocation);
		}
	}
	
	@Override public void mouseMoved(MouseEvent e) {
	// 	TODO Auto-generated method stub
	}
	
	@Override public void mouseClicked(MouseEvent e) {
		
	}
	
	@Override public void mousePressed(MouseEvent e) {
		this.dragN = getNodeAt(e);
		this.dragOffSet = SwingUtilities.convertPoint(this, e.getPoint(), dragN);
	}
	
	@Override public void mouseReleased(MouseEvent e) {
		
	}
	
	@Override public void mouseEntered(MouseEvent e) {
		
	}
	
	@Override public void mouseExited(MouseEvent e) {
		
	}
	
	protected DraggableNode getNodeAt(MouseEvent e) {
		Component comp = getComponentAt(e.getPoint());
		
//		System.out.println(comp.hashCode() + " : is "+ ((comp instanceof DraggableNode) ? "" : "NOT ") + "a draggable node");
		
		if(comp instanceof DraggableNode) 	return (DraggableNode) comp;
		else 								return null;
	}
	
	public JToolBar geteditorToolBar() {
		return this.editorToolBar;
	}
	public JPanel getInspectorPanel() {
		return inspectorPanel;
	}
	
////////////////////////////////
//gui
////////////////////////////////
	protected void openNewNodeDialog() {
		var node = new DraggableNode();
		node.add(new JLabel("wee woooadasd"));
		node.setVisible(true);
	
		addNode(null, node);
	}
	
	public DraggableNode addNode(Point position, DraggableNode node) { return addNode(defaultLayer, position, node); }
	public DraggableNode addNode(int layer, Point position, DraggableNode node) {
		if(position == null)
			position = new Point(
					(int)editorScrollPane.getVisibleRect().getCenterX(),		//with null layout this dosnet actually do anyhting
					(int)editorScrollPane.getVisibleRect().getCenterY()
				);
		
		this.add(node, layer, 0);
		var insets = this.getInsets();
		var size = node.getPreferredSize();
		node.setBounds(
				position.x + insets.left,
				position.y + insets.top,
		        size.width,
		        size.height);
		
		this.revalidate();
		this.repaint(node.getBounds());
		
		return node;
	}
	
////////////////////////////////
//gen gui
////////////////////////////////
	private void genGUI_editor() {
		this.setLayout(null);
		
		//dynamic resizing
//		final DraggableNodeEditor self = this;
//		this.addComponentListener(new ComponentAdapter() {
//            public void componentResized(ComponentEvent e) {
//            	Dimension 
//            		spd = editorScrollPane.getSize(),
//            		ed	= new Dimension();
//                
//            	System.out.println("ed : " + self.getSize());
//            	System.out.println("sp : " + spd);
//            	
//            	ed.width = Math.max(spd.width, self.getWidth());
//            	ed.height= Math.max(spd.height, self.getHeight());
//            	
//            	if(self.getWidth() < spd.getWidth() || self.getHeight() < spd.getHeight()) {
//            		System.out.println("------current size:" + self.getSize());
//            		System.out.println("------new size:" + ed);
//            		
//            	}
//            }
//        });
		
		System.out.println();
		var newNodeBtn = new JButton("+");
			newNodeBtn.addActionListener(e -> openNewNodeDialog());
		
		editorToolBar = new JToolBar("editor tool bar",JToolBar.VERTICAL);
			editorToolBar.setRollover(true);
			
		editorToolBar.add(newNodeBtn);
		editorToolBar.add(Box.createVerticalStrut(MainWin.stdStructSpace / 2));
	}
	
	private void genGUI_inspector(JPanel panel) {
		
	}
	
}
