package draggableNodeEditor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	protected ArrayList<SoftReference<DraggableNode<?>>> draggableNodes = new ArrayList<>();
	
	//mouse events
	protected Point dragOffSet;
	
	protected boolean draggingNode = false;
	protected DraggableNode<?> dragN;		//the current node being dragged. "dragN":pronounced like drag-n-dez-...
	
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
		if(!draggingNode || !dragN.isDraggable || !SwingUtilities.isLeftMouseButton(e) ) return;
		
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
			newLocation.x = Math.min(newLocation.x, this.getWidth()	- dragN.getWidth());	//east bound	
			newLocation.y = Math.min(newLocation.y, this.getHeight() - dragN.getHeight());	//south bound
			
			dragN.setLocation(newLocation);
		}
	}
	
	@Override public void mouseMoved(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isMiddleMouseButton(e)) {
			if(e.getSource() == this) {
				switch(this.getComponentAt(e.getPoint())) {
					case DraggableNode<?> node -> {
							NodeComponent<?> ncomp = node.getComponentForPoint(SwingUtilities.convertPoint(this, e.getPoint(), node));
							if(ncomp == null) return;
							
							System.out.println("new connection by : " + ncomp);
						} 
					default -> {System.out.println("not a node");}
				}
			}
		}
		
	}
	@Override public void mousePressed(MouseEvent e) {
		if(draggingNode){//is something is being dragged already
			this.mouseDragged(e);
			return;
		}
		
		DraggableNode<?> nodeAt = null;
		
		/* accounts for different JComponents triggering the mouse listener.
		 * 
		 * sets [compAt] to the component selected (can be null)
		 * sets [e](MouseEvent) to the perspective of [this](DraggableNodeEditor)
		 */
		switch(e.getSource()) {
			case DraggableNode<?> sourceNode -> {
				nodeAt = sourceNode;
				e = SwingUtilities.convertMouseEvent(sourceNode, e, this);
			}
			case DraggableNodeEditor editor when editor == this -> {
				Component compAt = getComponentAt(e.getPoint());
				
				if(compAt instanceof DraggableNode<?> catnode)
					nodeAt = catnode;
			}
			default -> {throw new UnsupportedOperationException("mouse event of the DraggableNodeEditor got triggered by a unknown Swing.Component . unable :( .");}
		};
		
		if(nodeAt == null){
			if(dragN != null)
				dragN.onOffClick(e, null, null);
			
			return;
		}
		
		if(dragN != null)
			dragN.onOffClick(e, nodeAt, dragN.getComponentForPoint(SwingUtilities.convertPoint(this, e.getPoint(), dragN)));
		
		if(SwingUtilities.isLeftMouseButton(e)) {
			selectNode(this.dragN = nodeAt); 
				
			if(dragN.isDraggable) { 		//drag event
				dragNode(nodeAt, SwingUtilities.convertPoint(this, e.getPoint(), dragN));
				return;
			}
		}else if(SwingUtilities.isMiddleMouseButton(e)) {//attempt creating a new connection 
			
		}
	}
	@Override public void mouseReleased(MouseEvent e) {
		if(draggingNode) {
			this.setLayer(dragN, NODE_LAYER);
			
			this.revalidate();//triggers layout manager (should be AbsoluteLayout) to recalculate the minimum size 
		}
		
		draggingNode = false;
	}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	
	public JToolBar geteditorToolBar() {
		return this.editorToolBar;
	}
	public JPanel getInspectorPanel() {
		return inspectorPanel;
	}
	
	public void dragNode(DraggableNode<?> node, Point offset) {
		if(!isAncestorOf(node)) return;
		
		if(draggingNode) {
			dragN.onOffClick(null, null, null);
		}
		
		if(node != dragN) dragN = node;
		
		if(offset == null) offset = new Point((int)(node.getWidth() / 2), (int)(node.getHeight() / 2));
		
		draggingNode = true;
		this.dragOffSet = offset;
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
				
				//find & supply context
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
						selectNode(this.dragN = node);
						dragNode(node, null);
						
						return;
					}
				
				throw new RuntimeException("failed to find a matching class by the selected table value. expected type:Class<? extends DraggableNode>, got value:" + dragN==null?"null":dragN.toString());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				//if your getting a error and traced your back to here it means something in the process of constructing a node has thrown a error. all this function does is call the constructor.
				if(!(e1 instanceof NoSuchMethodException)) {
					System.out.println("draggableNodeEditor > openNewNodeDialog, dragN:" + dragN);
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
		
		if(position == null)
			position = new Point(
					(int)editorScrollPane.getVisibleRect().getCenterX(),		//with null layout this dosen't actually do anything
					(int)editorScrollPane.getVisibleRect().getCenterY()
				);
		
		draggableNodes.add(new SoftReference<DraggableNode<?>>(node));
		this.add(node, layer);
		
		this.revalidate();
		
		node.initNode(this);
		
		node.addComponentListener(nConnRescheduler);
		
		this.repaint(node.getBounds());
		
		return node;
	}
	
	public void removeNode(DraggableNode<?> node) {
		dragN.onDelete();
		
		if(dragN != null && node == dragN) {
			inspectorPanel.removeAll();
			inspectorPanel.setBorder(null);
			inspectorPanel.revalidate();
		}
		
		remove(dragN);
		repaint(dragN.getBounds());
	}
	
	public void selectNode(DraggableNode<?> node) {
		if(node != null){
			this.moveToFront(node);
			
			if(node != null || node != dragN) {
				
				inspectorPanel.removeAll();
				inspectorPanel.setBorder(new TitledBorder(
						null,
						node.getTitle()
					));
				
				var ins = node.getInspector();
				if(ins != null)
					this.inspectorPanel.add(ins);
			}
			
		}
		
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
		if(dragN == null) return;
		
		removeNode(dragN);
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
				
			}
		}
		@Override public void componentShown(ComponentEvent e)  	{ }
		@Override public void componentHidden(ComponentEvent e) 	{ }
	};

	@Override public void paint(Graphics g) {
		super.paint(g);
		
		g.setXORMode(Color.red);
		g.drawLine(0, getHeight(), getWidth(), 0);
		
		draggableNodes.parallelStream()
			.map(sr -> sr.get())
			.filter(e 	-> e != null)
			.flatMap(e 	-> e.getConnectableNodeComponents().parallelStream())
			.flatMap(comp 	-> comp.connections.parallelStream())
			.filter(conn 	-> conn.needsRedrawn)
			.forEach(conn 	-> {
					
				});
	}
	
	
}
