package presentables.presents.draggableNodeEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import audinc.gui.MainWin;
import presentables.Presentable;
/**
 * GUI editor for draggable and linkable nodes. encouraged to inherit this class for more specialized uses
 * <p>
 * <b>to use :</b>  this class inherits JPanel and displays the main content (where nodes are dragged around)
 * there are 2 JPanels (get using {@code getEditorPanel()} and {@code getIndexPanel}) that are also part of the UI
 * @implNote Serializable
 */
public class DraggableNodeEditor extends JLayeredPane implements MouseListener, MouseMotionListener, Serializable {
	protected JScrollPane editorScrollPane;
	protected final int canvasLayer = 0;
	protected JPanel 
		indexPanel,		//options related to the entire content panel. add nodes, define nodes, ect.
		inspectorPanel;	//options related to a single node. view & edit details about a selected node
	protected List<DraggableNode> nodes = new ArrayList<>(5);
	
	//mouse events
	protected Point dragStartPoint;
	
	private static final long serialVersionUID = 1L;
	
	public DraggableNodeEditor(JPanel inspector, JPanel index) {
		this.indexPanel = index;
		this.inspectorPanel = inspector;
	}
	public DraggableNodeEditor() {
		this(new JPanel(), new JPanel());
		this.addMouseListener(this);
		
		editorScrollPane = new JScrollPane(this,	
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		genGUI();
	}
	
	public DraggableNode addNode(Point position, DraggableNode node) {
		this.add(node, 1, 0);
		
		if(position != null)
			node.setLocation(position);
		else 
			node.setLocation(
					(int)editorScrollPane.getVisibleRect().getCenterX(),
					(int)editorScrollPane.getVisibleRect().getCenterY()
				);
		
		this.revalidate();
		this.repaint();
		
		return node;
	}
	
	/**
	 * this just calls the 3 functions <code>genGUI inspector-index-editor</code> in that order 
	 */
	public void genGUI() {		
		genGUI_inspector(inspectorPanel);
		genGUI_index(indexPanel);
		genGUI_editor();
	}
	
////////////////////////////////
//mouse events
////////////////////////////////
	@Override public void mouseDragged(MouseEvent e) {
	// TODO Auto-generated method stub
	}
	
	@Override public void mouseMoved(MouseEvent e) {
	// TODO Auto-generated method stub
	}
	
	@Override public void mouseClicked(MouseEvent e) {
	// TODO Auto-generated method stub
	}
	
	@Override public void mousePressed(MouseEvent e) {	
	}
	
	@Override public void mouseReleased(MouseEvent e) {
	// TODO Auto-generated method stub
	}
	
	@Override public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override public void mouseExited(MouseEvent e) {
	// TODO Auto-generated method stub
	}
	
	public JPanel getIndexPanel() {
		return indexPanel;
	}
	public JPanel getInspectorPanel() {
		return inspectorPanel;
	}
	
////////////////////////////////
//gen gui
////////////////////////////////
	private void genGUI_editor() {
		this.setLayout(new FlowLayout());
	}
	private void genGUI_index(JPanel panel) {
		panel.setLayout(new GridBagLayout());
		int x = 0, y = 1;
		
		var newNodeBtn = new JButton("new node");
			newNodeBtn.addActionListener(e -> {
				var node = new DraggableNode();
					node.add(new JLabel("wee woooadasd"));
					node.setVisible(true);
				
				addNode(null, node);
			});
			
		panel.add(newNodeBtn, Presentable.createGbc(x, y++));
	}
	
	private void genGUI_inspector(JPanel panel) {
		
	}
	
}
