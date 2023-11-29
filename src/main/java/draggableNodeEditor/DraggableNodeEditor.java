package draggableNodeEditor;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import audinc.gui.AbsoluteLayout;
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
	private static final long serialVersionUID = 1L;
	
	public static final int 
		LINE_LAYER 	= 2,
		NODE_LAYER 	= 3;
	
	public volatile Map<DraggableNodeGroup, Object> nodeGroups;
	
	public volatile JToolBar editorToolBar;
	
	protected volatile JScrollPane editorScrollPane;
	protected volatile JPanel inspectorPanel;	//options related to a single node. view & edit details about a selected node
	
	//mouse events
	protected Point dragOffSet;
	protected boolean draggingNode = false;
	protected DraggableNode dragN,		//the current node being dragged. "dragN":pronounced like drag-n-dez-...
		selectedNode;
	
	public DraggableNodeEditor(JPanel inspector, JToolBar index, Map<DraggableNodeGroup, Object> nodeGroups) {
		this.editorToolBar = index;
		this.inspectorPanel = inspector;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		editorScrollPane = new JScrollPane(this,	
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		this.nodeGroups = nodeGroups;
		
		
		genGUI();
	}
	public DraggableNodeEditor(Map<DraggableNodeGroup, Object> allowedNodeGroups) {
		this(new JPanel(), new JToolBar(), allowedNodeGroups);
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
		if(!draggingNode && dragN == null) return;
		
		assert dragOffSet != null 
				: "failed to be initialized";
		
		if(this.getBounds().contains(e.getPoint())) {
			Point newLocation = e.getPoint();
				newLocation.x -= dragOffSet.x;
				newLocation.y -= dragOffSet.y;
			newLocation.x = Math.max(newLocation.x, 0);	//lower	bound
			newLocation.y = Math.max(newLocation.y, 0);	//upper bound
			newLocation.x = Math.min(newLocation.x, this.getWidth()	- dragN.getWidth());	//right bound	
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
		Component compAt = getComponentAt(e.getPoint());
		if(compAt == null) return;
		
		if(compAt instanceof DraggableNode) {
			selectNode(this.dragN = (DraggableNode)compAt);
		
			//drag node event
			if(dragN.isDraggable) {
				if(e.getClickCount() == 2) {		//make connection event
					draggingNode = false;
					NodeComponent selectedComponent = dragN.getComponentForPoint(SwingUtilities.convertPoint(this, e.getPoint(), dragN));
					
					System.out.println("selected componetn:" + selectedComponent);
				}else {								//drag event
					draggingNode = true;
					this.dragOffSet = SwingUtilities.convertPoint(this, e.getPoint(), dragN);
				}
			}
		}
	}
	
	@Override public void mouseReleased(MouseEvent e) {
		draggingNode = false;
	}
	
	@Override public void mouseEntered(MouseEvent e) {
		
	}
	
	@Override public void mouseExited(MouseEvent e) {
		
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
	
	private JTable NewNodeDialogNodeTable = null;
	protected void openNewNodeDialog() {	
		if(NewNodeDialogNodeTable == null) {	//c++ strikes again
			var nodeClasses = nodeGroups.keySet().stream()
					.flatMap(e -> e.allowedNodes.stream())
					.map(e -> new Object[] {e})
					.sorted((a,b) -> a[0].toString().compareTo(b[0].toString()))
					.toArray(Object[][]::new);
			
			NewNodeDialogNodeTable = new JTable(new DefaultTableModel(nodeClasses, new Object[]{"nodes ("+ nodeClasses.length +")"})) {
					private static final long serialVersionUID = 2L;
		
					public boolean isCellEditable(int row, int column) {                
		                return false;               
					};
				};
				NewNodeDialogNodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				NewNodeDialogNodeTable.setCellEditor(null);
		}	
		
		var content = new JPanel(new GridBagLayout());
		content.add(
			new JScrollPane(NewNodeDialogNodeTable,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			Presentable.createGbc(0, 0));
		
		int result = JOptionPane.showConfirmDialog(null, content, 
	    		"Node selector", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			if(NewNodeDialogNodeTable.getSelectedRow() < 0)
				return;
			
			try {
				Object dragN = NewNodeDialogNodeTable.getValueAt(NewNodeDialogNodeTable.getSelectedRow(), 0);
				if(dragN != null) {
					//get context
					var dragClas = (Class<? extends DraggableNode>)dragN;
					
					for(DraggableNodeGroup dng : nodeGroups.keySet())
						if(dng.allowedNodes.contains(dragClas)) {
							//Instantiate
							DraggableNode node;
							
							if(dng.expectedContextType == Void.class) {
								node = dragClas
										.getConstructor()
										.newInstance();
							}else {
								//life saving if need to debug.
//								System.out.println("draggable node editor > open new node dialog"
//										+ "\n\tdraggable node class: \t" + dragClas
//										+ "\n\tdraggable node group: \t" + dng
//										+ "\n\texpected class: \t" + dng.expectedContextType
//										+ "\n\tobject class: \t" + nodeGroups.get(dng));
								node = dragClas
									.getConstructor(dng.expectedContextType)
									.newInstance(nodeGroups.get(dng));
							}
							
							addNode(node);
							return;
						}
					
					throw new RuntimeException("failed to find a matching class by the selected table value. expected type:Class<? extends DraggableNode>, got value:" + dragN==null?"null":dragN.toString());
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				//if your getting a error and traced your back to here it means something in the process of constructing a node has thrown a error. all this function does is call the constructor.
				if(!(e1 instanceof NoSuchMethodException)) {
					System.out.println("dragN:" + dragN);
					e1.printStackTrace();
				}
				e1.printStackTrace();
			}
	    }
	}
	
	public DraggableNode addNode(DraggableNode node) { return addNode(NODE_LAYER, null, node); }
	public DraggableNode addNode(int layer, Point position, DraggableNode node) {
		assert node != null
			: "cannot add a null node";
		
		if(position == null)
			position = new Point(
					(int)editorScrollPane.getVisibleRect().getCenterX(),		//with null layout this dosnet actually do anyhting
					(int)editorScrollPane.getVisibleRect().getCenterY()
				);
		
		this.add(node, layer);
		
		this.revalidate();
		
		node.initNode();
		
		this.repaint(node.getBounds());
		
		return node;
	}
	
	public void selectNode(DraggableNode node) {
		this.inspectorPanel.removeAll();
		
		if(selectedNode != null) {
			//remove visual indicator of node selection
		}
		
		if(node != null){
			this.moveToFront(node);
			
			if(node.getInspector() != null)
				this.inspectorPanel.add(node.getInspector(), Presentable.createGbc(0, 0));
			
			//remove visual indicator on former selected node
			//add visual indicator that the new node is selected
		}
		
		selectedNode = node;
		
		inspectorPanel.revalidate();
	}
	
////////////////////////////////
//gen gui
////////////////////////////////
	private void genGUI_editor() {
		this.setLayout(new AbsoluteLayout());
		
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
			newNodeBtn.setMnemonic('n');
		
		editorToolBar = new JToolBar("editor tool bar",JToolBar.VERTICAL);
			editorToolBar.setRollover(true);
			
		editorToolBar.add(newNodeBtn);
		editorToolBar.add(Box.createVerticalStrut(MainWin.stdStructSpace / 2));
	}
	
	private void genGUI_inspector(JPanel panel) {
		panel.setLayout(new GridBagLayout());
	}
	
}
