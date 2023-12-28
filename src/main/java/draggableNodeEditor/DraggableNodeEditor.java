package draggableNodeEditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import audinc.gui.AbsoluteLayout;
import audinc.gui.MainWin;
import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.LineAnchor;
import draggableNodeEditor.connectionStyles.DirectConnectionStyle;
import presentables.Presentable;
/**
 * GUI editor for draggable and linkable nodes. encouraged to inherit this class for more specialized uses
 * <p>
 * <b>to use :</b>  this class inherits JPanel and displays the main content (where nodes are dragged around)
 * there are 2 JPanels (get using {@code getEditorPanel()} and {@code getIndexPanel}) that are also part of the UI
 * @implNote Serializable
 */
public class DraggableNodeEditor extends JLayeredPane implements MouseListener, MouseMotionListener, ComponentListener {
	private static final long serialVersionUID = 1L;
	
	/**
	 * see her for layer guidelines -> https://docs.oracle.com/javase/tutorial/uiswing/components/rootpane.html
	 */
	public static final int 
		NODE_LAYER 	= JLayeredPane.DEFAULT_LAYER;
	
	/**
	 * for node use.
	 * A table of all node groups and the "context" associated with them.
	 */
	public volatile Map<DraggableNodeGroup, Object> nodeGroups;
	
	public JToolBar editorToolBar;
	
	protected JScrollPane editorScrollPane;
	protected JPanel inspectorPanel;	//options related to a single node. view & edit details about a selected node
	
	/**
	 * contains all the nodes on the nodeEditor including those that may not be a part of it anymore.
	 * use something like <code>this.isAncestor(element)</code> to see if its still in the editor
	 */
	protected ArrayList<DraggableNode<?>> draggableNodes = new ArrayList<>();
	
	//mouse events
	protected Point dragOffSet;
	
	//state events
	private EnumSet<EditorState> editorState = EnumSet.of(EditorState.GENERAL);
	
	protected DraggableNode<?> selectedNode;		//the current node being dragged. "dragN":pronounced like drag-n-dez-...
	public int dragDropSnapRange = 4;
	/**
	 * draws the tempoary connection thats being controlled
	 */
	private NodeConnection<?> 	connectionIndicator = null;
	private ConnectionStyle		connectionIndicatorStyle = new DirectConnectionStyle();
	
	public DraggableNodeEditor(JPanel inspector, JToolBar index, Map<DraggableNodeGroup, Object> nodeGroups) {
		super();
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
		
		this.editorToolBar = index;
		this.inspectorPanel = inspector;			
		
		editorScrollPane = new JScrollPane(this,	
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			editorScrollPane.setAutoscrolls(true);
		
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
		if(!isEditor(EditorState.DRAGGINGNODE) || !selectedNode.isDraggable || !SwingUtilities.isLeftMouseButton(e) ) return;
		
		assert dragOffSet != null 
				: "failed to be initialized";
		
		/* accounts for different JComponents triggering the mouse listener.
		 * 
		 * sets [e](MouseEvent) to the perspective of [this](DraggableNodeEditor)
		 */
		switch(e.getSource()) {
			case DraggableNode<?> sourceNode -> { e = SwingUtilities.convertMouseEvent(sourceNode, e, this); }
			case DraggableNodeEditor editor when editor == this -> {}
			default -> {throw new UnsupportedOperationException("mouse event of the DraggableNodeEditor got triggered by a unknown Swing.Component . unable :( .");}
		};
		
		if(this.getBounds().contains(e.getPoint())) {
			Point newLocation = e.getPoint();
				newLocation.x -= dragOffSet.x;
				newLocation.y -= dragOffSet.y;
				
			newLocation.x = Math.max(newLocation.x, 0);	//west	bound
			newLocation.y = Math.max(newLocation.y, 0);	//north bound
			newLocation.x = Math.min(newLocation.x, this.getWidth()	- selectedNode.getWidth());	//east bound	
			newLocation.y = Math.min(newLocation.y, this.getHeight() - selectedNode.getHeight());	//south bound
			
			selectedNode.setLocation(newLocation);
		}
	}
	
	@Override public void mouseMoved(MouseEvent e) {
		if(isEditor(EditorState.DRAGGINGCONNECTION)) {
			var pe = prepMouseEvent(e);
			
			connectionIndicator.knownAnchors.clear();
			connectionIndicator.knownAnchors.set(1, new LineAnchor(
					pe.mouseEvent.getPoint().x,
					pe.mouseEvent.getPoint().y,
					0f,0f,0f,0f,0f
				));
		}
	}
	@Override public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isMiddleMouseButton(e)) {
			var pe = prepMouseEvent(e);
			
			if(setEditor(EditorState.DRAGGINGCONNECTION)) {
				if(pe.compAt != null) {
					System.out.println("new connection, compAt : " + pe.compAt);
					startConnectionIndicatorFrom(pe.nodeAt, pe.compAt);
				}
			}else {
				System.out.println("setting connection, compAt : " + pe.compAt);
				
				if(pe.compAt != null)
					plopConnectionIndicator(pe.compAt);
			}
		}
		
	}
	
	@Override public void mousePressed(MouseEvent e) {
		if(isEditor(EditorState.DRAGGINGNODE)){//is something is being dragged already
			dragOffSet = switch(e.getSource()) {
				case DraggableNode<?> node when node == selectedNode -> e.getPoint();
				case DraggableNodeEditor editor -> SwingUtilities.convertPoint(editor, e.getPoint(), selectedNode);
				default -> new Point(0,0);
			};
			
			mouseDragged(e);
			return;
		}
		
		var pe = prepMouseEvent(e);
				
		if(selectedNode != null) 	selectedNode.onOffClick(e, pe.nodeAt, pe.compAt);
		
		//event logic
		if(SwingUtilities.isLeftMouseButton(e)) {
			selectNode(pe.nodeAt);
			
			if(pe.nodeAt != null) {
				//drag event
				if(pe.nodeAt.isDraggable) {
					dragNode(pe.nodeAt, pe.pointAt);
				}
			}
		}
	}
	@Override public void mouseReleased(MouseEvent e) {
		if(isEditor(EditorState.DRAGGINGNODE)) {
			setLayer(selectedNode, NODE_LAYER);
			
			plopNode(selectedNode);
			unsetEditor(EditorState.DRAGGINGNODE);
			
			revalidate();//triggers layout manager (should be AbsoluteLayout) to recalculate the minimum size
		}
	}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	private editorMouseEvent prepMouseEvent(MouseEvent e) {
		DraggableNode<?> nodeAt = null;	//node at clicked point. nodeAt == null => compAt == null
		NodeComponent<?> compAt = null;	//component at clicked point.
		Point 			pointAt = null;	//clicked point relative to nodeAt
		
		// accounts for different JComponents triggering the mouse listener.
		switch(e.getSource()) {
			case null -> {} 
			case DraggableNode<?> sourceNode -> {
				nodeAt = sourceNode;
				pointAt = e.getPoint();
				e = SwingUtilities.convertMouseEvent(sourceNode, e, this);
			}
			case DraggableNodeEditor editor when editor == this -> {
				if(getComponentAt(e.getPoint()) instanceof DraggableNode<?> cat) {
					nodeAt = cat;
					pointAt = SwingUtilities.convertPoint(this, e.getPoint(), nodeAt);
				}
			}
			default -> {throw new UnsupportedOperationException("mouse event of the DraggableNodeEditor got triggered by a unknown Swing.Component . unable :( .");}
		};
		if(nodeAt != null)	compAt = nodeAt.getComponentForPoint(pointAt);
		
		return new editorMouseEvent(
				e,
				nodeAt,
				compAt,
				pointAt
			);
	}
	
	public JToolBar geteditorToolBar() {
		return this.editorToolBar;
	}
	public JPanel getInspectorPanel() {
		return inspectorPanel;
	}
	
	public void dragNode(DraggableNode<?> node, Point offset) {
		assert isAncestorOf(node) : "refrenced a non existing node";
		
		if(isEditor(EditorState.DRAGGINGNODE)) {
			selectedNode.onOffClick(null, null, null);
		}
		
		if(node != selectedNode) selectedNode = node;
		
		if(offset == null) offset = new Point((int)(node.getWidth() / 2), (int)(node.getHeight() / 2));
		
		setEditor(EditorState.DRAGGINGNODE);
		this.dragOffSet = offset;
	}
	
	/**
	 * puts the given node to the given location.
	 * the node must already be part of the editor.
	 * @param node
	 * @param location
	 */
	public void plopNode(DraggableNode<?> node) {
		assert isAncestorOf(node) : "refrenced a non existing node";
		
		System.out.println("plop node");
	}
	
	public void startConnectionIndicatorFrom(DraggableNode<?> hostNode, NodeComponent<?> comp) {
		setEditor(EditorState.DRAGGINGCONNECTION);
		
		connectionIndicator = comp.getNewConnection();
		Point connPoint = SwingUtilities.convertPoint(hostNode, comp.connectionPoint, this);
		connectionIndicator.knownAnchors.add(new LineAnchor(
				connPoint.x,
				connPoint.y,
				0f,0f,0f,0f,0f
			));
		for(var node : draggableNodes) {
			for(var c : node.getConnectableNodeComponents()) {
				if(comp.type == c.type)
					c.setCompStatus(NodeComponentStatus.AVAILABLE);
				else if(c.type.isAssignableFrom(comp.type))
					c.setCompStatus(NodeComponentStatus.NETURAL);
				else
					c.setCompStatus(NodeComponentStatus.NOT_AVAILABLE);
			}	
		}
	}
	
	public void setAllNodeComponentStatuses(NodeComponentStatus stat) {
		for(var node : draggableNodes) {
			for(var c : node.getConnectableNodeComponents()) {
				c.setCompStatus(stat);
			}	
		}
	}
	
	public void plopConnectionIndicator(NodeComponent<?> comp) {
		if(!unsetEditor(EditorState.DRAGGINGCONNECTION)) return;
		
		System.out.println("plop conneciton indicator");
	}
	
	/**
	 * for readability reasons
	 * @param eds : the editor states to check
	 * @return true if the states are set
	 */
	protected boolean isEditor(EditorState... eds) {
		return editorState.containsAll(List.of(eds));
	}
	
	/**
	 * for readability reasons
	 * @param eds : the editor states to unset
	 * @return true if all the states got changed
	 */
	protected boolean unsetEditor(EditorState... eds) {
		return editorState.removeAll(List.of(eds));
	}
	
	/**
	 * for readability reasons
	 * @param eds : the editor states to set
	 * @return true if all the states got changed
	 */
	protected boolean setEditor(EditorState... eds) {
		return editorState.addAll(List.of(eds));
	}
	
////////////////////////////////
//node & gui
////////////////////////////////
	
	private JTable NewNodeDialogNodeTable = null;
	private List<Class<? extends DraggableNode<?>>> nodeClasses; 
	protected void openNewNodeDialog() {	
		if(NewNodeDialogNodeTable == null) {	//this if statement is intended to work exactly like the c++ local-static concept
			nodeClasses = nodeGroups.keySet().stream()
					.flatMap(e -> e.allowedNodes.stream())
					.sorted((a,b) -> a.toString().compareTo(b.toString()))
					.toList();
			Object[][] nodeClassNames = nodeClasses.stream().sequential()
					.map(e -> {
							String es = e.toString();
							return new Object[] {es.substring(es.indexOf('.') + 1)};
						})
					.toArray(Object[][]::new);
			
			NewNodeDialogNodeTable = new JTable(new DefaultTableModel(nodeClassNames, new Object[]{"nodes ("+ nodeClassNames.length +")"})) {
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
				Class<? extends DraggableNode<?>> dragClas = nodeClasses.get(NewNodeDialogNodeTable.getSelectedRow());
				
				//find & supply contexts
				for(DraggableNodeGroup dng : nodeGroups.keySet())
					if(dng.allowedNodes.contains(dragClas)) {
						//Instantiate
						DraggableNode<?> node;
						
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
						
						//let user drag the new node
						selectNode(node);
						dragNode(node, null);
						
						return;
					}
				
				throw new RuntimeException("failed to find a matching class by the selected table value. expected type:Class<? extends DraggableNode>, got value:" + selectedNode==null?"null":selectedNode.toString());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				//if your getting a error and traced your back to here it means something in the process of constructing a node has thrown a error. all this function does is call the constructor.
				if(!(e1 instanceof NoSuchMethodException)) {
					System.out.println("draggableNodeEditor > openNewNodeDialog, dragN:" + selectedNode);
					e1.printStackTrace();
				}
				e1.printStackTrace();
			}
	    }
	}
	
	public DraggableNode<?> addNode(DraggableNode<?> node) { return addNode(NODE_LAYER, null, node); }
	public DraggableNode<?> addNode(int layer, Point position, DraggableNode<?> node) {
		assert node != null
			: "cannot add a null node";
		
		draggableNodes.add(node);
		
		this.add(node, layer);
		
		this.revalidate();
		
		node.initNode(this);
		
		node.addComponentListener(nConnRescheduler);
		
		SwingUtilities.invokeLater(() -> {
			Point location = position;
			if(location == null) {
				var visr = this.getVisibleRect();
				location = new Point(
						(int)visr.getCenterX() - node.getWidth()/2,
						(int)visr.getCenterY() - node.getHeight()/2
					);
			}
			
			node.setLocation(location);
		});
		
		return node;
	}
	
	public void removeNode(DraggableNode<?> node) {		
		if(!draggableNodes.remove(node)) return;
		
		selectedNode.onDelete();
		
		if(selectedNode != null && node == selectedNode) {
			inspectorPanel.removeAll();
			inspectorPanel.setBorder(null);
			inspectorPanel.revalidate();
		}
		
		remove(selectedNode);
		repaint(selectedNode.getBounds());
	}
	
	public void selectNode(DraggableNode<?> node) {
		if(node != null){
			moveToFront(node);
			
			if(node != null || node != selectedNode) {
				
				inspectorPanel.removeAll();
				inspectorPanel.setBorder(new TitledBorder(
						null,
						node.getTitle()
					));
				
				var ins = node.getInspector();
				if(ins != null)
					inspectorPanel.add(ins);
			}
			
		}
		
		selectedNode = node;
		inspectorPanel.revalidate();
	}
	
////////////////////////////////
//gui events
////////////////////////////////
	private void genGUI_editor() {
		var absLayout = new AbsoluteLayout();
		this.setLayout(absLayout);
				
		System.out.println();
		var newNodeBtn = new JButton("+");
			newNodeBtn.setToolTipText("new node");
			newNodeBtn.addActionListener(e -> openNewNodeDialog());
			newNodeBtn.setMnemonic('n');
			
		var deleteNodeBtn = new JButton("-");//MainWin.getImageIcon("res/trashCan.png", MainWin.stdtabIconSize));
			deleteNodeBtn.setToolTipText("delete the selected node");
			deleteNodeBtn.addActionListener(e -> onDeletenodeButtonClick());
			deleteNodeBtn.setMnemonic(KeyEvent.VK_DELETE);
		
		editorToolBar = new JToolBar("editor tool bar",JToolBar.VERTICAL);
			editorToolBar.setRollover(true);
			
		editorToolBar.add(newNodeBtn);
		editorToolBar.add(deleteNodeBtn);
		editorToolBar.add(Box.createVerticalStrut(MainWin.stdStructSpace / 2));
	}
	
	private void genGUI_inspector(JPanel panel) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	}
	
	public void onDeletenodeButtonClick() {
		if(selectedNode == null) return;
		
		removeNode(selectedNode);
	}
	
/////////////////////
//node connection stuff
/////////////////////
	
	/*
	 * this should be triggered when the AbsoluteLayout decides to resize. this triggers when the gui is initialized
	 * - maintains the size of the nodeConnectionCanvas to that of the this(nodeEditor)
	 */
	@Override public void componentResized(ComponentEvent e) 	{ 
//		System.out.println("draggable node editor > component resized, editor resized");
	}
	@Override public void componentMoved(ComponentEvent e)  	{ }
	@Override public void componentShown(ComponentEvent e)  	{ }
	@Override public void componentHidden(ComponentEvent e) 	{ }
	
	/**
	 * triggers a line recalculation. editor to tell nodeConnection's should be recalculated/redrawn
	 * must only be attached to draggableNode's . intended for nodes that are a child of this nodeEditor.
	 */
	private ComponentListener nConnRescheduler = new ComponentListener() {
		@Override public void componentResized(ComponentEvent e) 	{ }
		@Override public void componentMoved(ComponentEvent e)  	{
//			System.out.println("draggable node editor > nConnRescheduler , component moved : " + e.getSource());
			if(e.getSource() instanceof DraggableNode node) {
				var arr = node.getConnectableNodeComponents();
				
//				for(NodeComponent<?> comp : node.getConnectableNodeComponents()){
//					
//				}
			}
		}
		@Override public void componentShown(ComponentEvent e)  	{ }
		@Override public void componentHidden(ComponentEvent e) 	{ }
	};

	@Override public void paint(Graphics g) {
		super.paint(g);
		
		g.setXORMode(Color.orange);
		g.drawLine(0, getHeight(), getWidth(), 0);
		g.drawLine(0, 0 , getWidth(), getHeight());
		g.setPaintMode();
		
//		draggableNodes.stream().sequential()
//			.flatMap(node 	-> node.getConnectableNodeComponents().stream())
//			.flatMap(comp 	-> comp.getDirectConnections().stream())
//			.distinct()
//			.filter(conn 	-> {
//					if(conn.needsRedrawing()) {
//						conn.setImage(new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB));
//						conn.draw(new Polygon[0]);
//						return false;
//					}
//					return true;
//				})
//			.forEach(conn -> {
//				Point offset = conn.getImageOffset();
//				var img = conn.getImage();
//				g.drawImage(img, offset.x, offset.y, img.getWidth(), img.getHeight(), null);
//			});
	}
	
	enum EditorState{
		GENERAL,
		DRAGGINGNODE,
		DRAGGINGCONNECTION,
		SELECTINGREGION
		;
	}
	
	record editorMouseEvent(
			MouseEvent mouseEvent,
			DraggableNode<?> 	nodeAt,
			NodeComponent<?> 	compAt,
			Point				pointAt) {
	}
}