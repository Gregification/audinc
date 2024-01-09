package draggableNodeEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import audinc.gui.AbsoluteLayout;
import audinc.gui.MainWin;
import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle;
import draggableNodeEditor.NodeConnectionDrawer.ConnectionStyle.ImageAndOffSet;
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
	public final Map<DraggableNodeGroup, Object> nodeGroups;
	
	public JToolBar editorToolBar;
	
	protected JScrollPane editorScrollPane;
	protected JPanel inspectorPanel;	//options related to a single node. view & edit details about a selected node
	protected JFrame editorDetailsView;
	
	private volatile boolean cacheDrawnLines = true;
	private final static long minRefreshWait_mill = 100;
	
	private ConcurrentLinkedQueue<WeakReference<NodeConnection>> ConnectionsToReimposeQueue = new ConcurrentLinkedQueue<>();
	private BufferedImage connectionImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private final PropertyChangeListener pcl_imageListener = new PropertyChangeListener() {
		
		private long lastRefresh = 0;
		@Override public void propertyChange(PropertyChangeEvent evt) {
//			System.out.println("draggable node editor > prop change listener; time@" + System.nanoTime()
//				+ "\n\t-observer property: " + evt.getPropertyName() 
//				+ "\n\t-source	: " + evt.getSource()
//				+ "\n\t-old val : " + evt.getOldValue()
//				+ "\n\t-new val : " + evt.getNewValue());
			if(!cacheDrawnLines) {
				long ref = System.currentTimeMillis();
				if(ref - lastRefresh > minRefreshWait_mill) {
					lastRefresh = ref;
					SwingUtilities.invokeLater(() -> repaint());
				}
				return;
			}
			
			switch(evt.getPropertyName()) {
				case ConnectionStyle.PropertyChange_ImageUpdate -> {
					var changedRect	= (Rectangle)evt.getOldValue();		//rectangle on image that needs redrawn
					var changedRastor 	= (Raster)evt.getNewValue();	//image inside the rectangle
						synchronized(connectionImage) {
							//clear the area, apply the change, re-apply the pre-existing connections
							var g = connectionImage.createGraphics();
							g.clearRect(changedRect.x, changedRect.y, changedRect.width, changedRect.height);
							g.dispose();
							
							connectionImage.setData(changedRastor);
						}
						
						reimposeArea(changedRect);
						
						repaint(changedRect);
					}
				case ConnectionStyle.PropertyChange_ImageReady -> {
						var oldCoverage = (Rectangle)evt.getOldValue();			//old coverage area
						var imgAndOffset= (ImageAndOffSet)evt.getNewValue();	//ConnectionStyle that finished
						
						if(oldCoverage != null)
							reimposeArea(oldCoverage);
						
						var img 	= imgAndOffset.image();						
						var offset 	= imgAndOffset.offset();
						synchronized(connectionImage) {
							var g = connectionImage.createGraphics();
							g.drawImage(img, offset.x, offset.y, null);
							g.dispose();
						}
						
						repaint(offset.x, offset.y, img.getWidth(), img.getHeight());
					}
				case ConnectionStyle.PropertyChange_ImageCanceled -> {}
				default -> throw new IllegalStateException("unknown property : " + evt.getPropertyName()); 
			}
	}};
	
	public void reimposeArea(Rectangle area) {
		synchronized(connectionImage) {
			var g = connectionImage.createGraphics();
			g.setBackground(new Color(0,0,0,0));
			g.clearRect(area.x, area.y, area.width, area.height);
			g.dispose();
		}
		
		getConnections()
		.filter(conn -> !conn.needsRedrawn())
		.filter(conn -> {
			var thisarea = conn.getConnectionImageCoverage();
			
			if(thisarea == null) return false;
			
			return thisarea.intersects(area);
		})
		.forEach(conn -> ConnectionsToReimposeQueue.add(new WeakReference<>(conn)));
		;
	}
	
	/**
	 * contains all the nodes on the nodeEditor including those that may not be a part of it anymore.
	 * use something like <code>this.isAncestor(element)</code> to see if its still in the editor
	 */
	protected ArrayList<DraggableNode<?>> draggableNodes = new ArrayList<>();
	
	//mouse events
	protected Point dragOffSet;
	
	//state events
	private EnumSet<EditorState> editorState = EnumSet.of(EditorState.GENERAL);
	final DraggableNodeEditor self = this;
	protected DraggableNode<?> selectedNode;		//the current node being dragged. "dragN":pronounced like drag-n-dez-...
	public int dragDropSnapRange = 4;
	/**
	 * draws the tempoary connection thats being controlled
	 */
	private NodeConnection	selectedConnection = null; 
	
	public DraggableNodeEditor(JPanel inspector, JToolBar index, Map<DraggableNodeGroup, Object> nodeGroups) {
		super();
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addComponentListener(this);
		
		this.editorToolBar = index;
		this.inspectorPanel = inspector;			
		
		this.nodeGroups = nodeGroups;
		
		genGUI();
	}
	public DraggableNodeEditor(Map<DraggableNodeGroup, Object> allowedNodeGroups) {
		this(new JPanel(), new JToolBar(), allowedNodeGroups);
	}
	public void quit() {
		if(editorDetailsView != null) {
			editorDetailsView.dispose();
			editorDetailsView = null;
		}
	}
	
	/** this just calls the 3 functions <code>genGUI inspector-index-editor</code> in that order 
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
			newLocation.x = Math.min(newLocation.x, this.getWidth()	- selectedNode.getWidth());		//east bound	
			newLocation.y = Math.min(newLocation.y, this.getHeight() - selectedNode.getHeight());	//south bound
			
			selectedNode.setLocation(newLocation);
		}
	}
	
	@Override public void mouseMoved(MouseEvent e) {
		if(isEditor(EditorState.DRAGGINGCONNECTION)) {
//			var pe = prepMouseEvent(e);
		}
	}
	@Override public void mouseClicked(MouseEvent e) {
		if(SwingUtilities.isMiddleMouseButton(e)) {
			var pe = prepMouseEvent(e);
			
			if(isEditor(EditorState.DRAGGINGCONNECTION)) {	
				if(pe.nodeAt instanceof AnchorPoint ap)
					selectedConnection.anchors.add(ap);	
				else
					plopConnectionIndicator(pe.compAt);
			}else {
				if(pe.compAt != null) {
					setEditor(EditorState.DRAGGINGCONNECTION);
					
					startConnectionIndicatorFrom(pe.compAt);
				}
			}
		}else if(SwingUtilities.isLeftMouseButton(e)) {
			
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
		
//		System.out.println("draggableNodeEditor > plop node");
	}

	public void startConnectionIndicatorFrom(NodeComponent<?> comp) {
//		System.out.println("draggableNodeEditor > startConnectionIndicatorFrom, comp : " + comp);
		setAllNodeComponentStatuses(comp.type);
		
		NodeConnection conn = new NodeConnection();
			conn.connectToComponent(comp);
			
		startConnectionIndicatorFrom(conn);
	}
	public void startConnectionIndicatorFrom(NodeConnection conn) {
//		System.out.println("draggableNodeEditor > startConnectionIndicatorFrom, conn : " + conn);
		setEditor(EditorState.DRAGGINGCONNECTION);
		
		selectedConnection = conn;
	}
	public void plopConnectionIndicator(NodeComponent<?> comp) {
//		var ret = new StringBuilder("draggableNodeEditor > plop conneciton indicator; attach to : " + comp);
		
		if(comp == null || comp.isCompStatus(NodeComponentStatus.NOT_AVAILABLE)) {
			selectedConnection = null;
			unsetEditor(EditorState.DRAGGINGCONNECTION);
			setAllNodeComponentStatuses(NodeComponentStatus.NETURAL);
//			ret.append("\n\t> connection is unchanged <- component called on is not valid.");
		}else {
			selectedConnection.connectToComponent(comp);
//			ret.append("\n\t> connection success. nodes connected : " + selectedConnection.getDirectleyConnectedComponents().size());
		}
		
		if(selectedConnection != null)
			if(selectedConnection.isPoinless()) {
//				ret.append("\n\t> deleting connection <- connection was pointless.");
				selectedConnection.deleteConnection();
				
				selectedConnection = null;
				unsetEditor(EditorState.DRAGGINGCONNECTION);
				setAllNodeComponentStatuses(NodeComponentStatus.NETURAL);
			}else {
				selectedConnection.removeAllPropertyChangeListener(pcl_imageListener);
				selectedConnection.addPropertyChangeListener(pcl_imageListener);
			}
		
		revalidateConnections();
		
//		System.out.println(ret.toString());
	}
	
	public void setAllNodeComponentStatuses(Class<?> type) {			
		for(var node : draggableNodes) {	
			var comps = node.getConnectableNodeComponents();
			for(var c : comps) {
				if(type == c.type)
					c.setCompStatus(NodeComponentStatus.SUGESTED);
				else if(c.type.isAssignableFrom(type))
					c.setCompStatus(NodeComponentStatus.AVAILABLE);
				else
					c.setCompStatus(NodeComponentStatus.NOT_AVAILABLE);
			}
			if(!comps.isEmpty()) node.repaint();
		}
	}
	public void setAllNodeComponentStatuses(NodeComponentStatus stat) {		
		for(var node : draggableNodes) {
			var comps = node.getConnectableNodeComponents();
			for(var comp : comps) {
				comp.setCompStatus(stat);
			}
			if(!comps.isEmpty()) node.repaint();
		}
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
	
	public void revalidateConnections() {	
//		var s = new StringBuilder("draggable node editor > recalidate connections. redrawing; ");
		
		Map<Boolean, List<NodeConnection>> cons = getConnections()
				.collect(Collectors.partitioningBy(conn -> conn.needsRedrawn())); 
		
		List<NodeConnection> 
			consToRedraw = cons.get(true),		//connections who's image has changed and should be removed 
			consToReimpose = cons.get(false); 	//connections who's image has not changed and should remain
		
		if(consToRedraw.isEmpty()) return;
		
		final Polygon[] obs = getLineObsticals();
		
		consToRedraw.stream().sequential()
			.forEach(conn -> {
				Rectangle redrawArea = conn.getConnectionImageCoverage();
				if(redrawArea != null) {
					synchronized(connectionImage) {
						var g = connectionImage.createGraphics();
						g.clearRect(redrawArea.x, redrawArea.y, redrawArea.width, redrawArea.height);
						g.dispose();
					}
					
					for(var con : consToReimpose) {
						var area = con.getConnectionImageCoverage();
						
						if(area == null) 
							consToReimpose.remove(con);	//not sure what would cause this but just in case, would mean its useless anyways
						else if(redrawArea.intersects(area)) {
							ConnectionsToReimposeQueue.add(new WeakReference<>(con));
							consToReimpose.remove(con); //no need to repeat check if its already in the redraw queue
						}
					}
				}
				
				conn.draw(obs, connectionImage, this);
//				s.append("\n\t> " + conn);
			})
			;
			
//		System.out.println(s);
	}
	
	private Stream<NodeConnection> getConnections(){
		return draggableNodes.parallelStream()
				.flatMap(node -> node.getConnectableNodeComponents().stream())
				.flatMap(comp -> comp.getDirectConnections().stream())
				.distinct();
	}
	
////////////////////////////////
//node & gui
////////////////////////////////
	public Polygon[] getLineObsticals() {
		final Component[] comps;
		synchronized(this.getTreeLock()) {
			comps = getComponents();
		}
		
		ArrayList<Polygon> polys = new ArrayList<>(comps.length);
		for(var c : comps)
			if(c instanceof DraggableNode node && node.isLineObstical)
				polys.add(node.getOutline());
		
		return polys.toArray(Polygon[]::new);
	}
	
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
		
		//position in center of view port
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
		
		selectedNode.onDelete(this);
		
		if(selectedNode != null && node == selectedNode) {
			inspectorPanel.removeAll();
			inspectorPanel.setBorder(null);
			inspectorPanel.revalidate();
		}
		
		if(node instanceof AnchorPoint ap)
			getConnections()
				.filter(conn -> conn.anchors.remove(node))
				.forEach(conn -> {
					while(conn.anchors.remove(node)) {}
					
					conn.setNeedsRedrawn(true);
				})
				;
		
		if(this.selectedConnection.getDirectleyConnectedComponents().size() <=  1)
			plopConnectionIndicator(null);
		
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
		editorScrollPane = new JScrollPane(this,	
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			editorScrollPane.setAutoscrolls(true);
		
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
		
		var viewConnectionsBtn = new JButton(MainWin.getImageIcon("res/info.png", MainWin.stdtabIconSize));
			viewConnectionsBtn.setToolTipText("see node editor details. eID:" + hashCode());
			viewConnectionsBtn.addActionListener(e -> onViewConnectionDetailsClick());
			viewConnectionsBtn.setMnemonic(KeyEvent.VK_C);

		var runGCBtn = new JButton(MainWin.getImageIcon("res/trashCan.png", MainWin.stdtabIconSize));
			runGCBtn.setToolTipText("clears unecessary data and runs Javas' GC");
			runGCBtn.addActionListener(e -> onRunGCClick());
		
		var smartLineToggler = new JToggleButton();
			smartLineToggler.setToolTipText("toggle line caching(is somewhat more efficent & faster if on; looks prettier if off)");
			smartLineToggler.setSelected(cacheDrawnLines);
			smartLineToggler.addActionListener(e -> onCacheDrawnLines(smartLineToggler.isSelected()));
			
		editorToolBar = new JToolBar("editor tool bar",JToolBar.VERTICAL);
			editorToolBar.setRollover(true);
			
		editorToolBar.add(newNodeBtn);
		editorToolBar.add(deleteNodeBtn);
		editorToolBar.add(Box.createVerticalGlue());
		editorToolBar.add(smartLineToggler);
		editorToolBar.add(runGCBtn);
		editorToolBar.add(viewConnectionsBtn);
	}
	
	private void genGUI_inspector(JPanel panel) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setPreferredSize(new Dimension(MainWin.stdDimension.width / 7, MainWin.stdDimension.height));
	}
	
	public void onDeletenodeButtonClick() {
		if(selectedNode == null) return;
		
		removeNode(selectedNode);
	}
	
	public void onCacheDrawnLines(boolean on) {
		cacheDrawnLines = on;
	}
	
	public void onRunGCClick() {
		if(editorDetailsView != null) {
			editorDetailsView.dispatchEvent(new WindowEvent(editorDetailsView, WindowEvent.WINDOW_CLOSING));
			editorDetailsView.dispose();
			editorDetailsView = null;
		}
		
		for(var n : draggableNodes)
			if(!this.isAncestorOf(n))
				draggableNodes.remove(n);
		
		draggableNodes.parallelStream()
			.flatMap(n -> n.getConnectableNodeComponents().stream())
			.flatMap(c -> c.getDirectConnections().stream())
			.distinct()
			.filter(c -> c.isPoinless())
			.forEach(c -> c.deleteConnection());
		
		System.gc();
	}
	
	public void onViewConnectionDetailsClick() {
		JFrame connF = viewConnectionDetails();
		connF.setVisible(!connF.isVisible());
	}
	
	public JFrame viewConnectionDetails() {
		if(editorDetailsView != null) return editorDetailsView;
		
		editorDetailsView = new JFrame("Node Editor info (eID:"+hashCode()+")");
		editorDetailsView.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		editorDetailsView.setLocationRelativeTo(this);
		editorDetailsView.setResizable(true);
		editorDetailsView.setMinimumSize(MainWin.stdDimension);
		editorDetailsView.setIconImage(MainWin.getImageIcon("res/clear.png").getImage());
		
		JPanel
			generalTab 		= new JPanel(),
			connectionTab	= new JPanel()
			;
		
		
		var content = new JTabbedPane();
			content.addTab("general", MainWin.getImageIcon("res/aboutbg.png", MainWin.stdtabIconSize),
					new JScrollPane(generalTab,
			        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			        		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
					"general overview");
			content.addTab("connection", MainWin.getImageIcon("res/link.png", MainWin.stdtabIconSize),
					connectionTab,
					"connection info");
		
		try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
			executor.execute(() -> genUI_editorDetails_general(generalTab));
			executor.execute(() -> genUI_editorDetails_connection(connectionTab));
		}
			
		editorDetailsView.add(content);
		return editorDetailsView;
	}
	public void genUI_editorDetails_general(JPanel content) {
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		ArrayList<Runnable> onReload = new ArrayList<>();
		
		JButton reloadBtn = new JButton(MainWin.getImageIcon("res/refresh.png", MainWin.stdtabIconSize));
			reloadBtn.setMnemonic(KeyEvent.VK_R);
			reloadBtn.setToolTipText("refresh this tabbs information");
			reloadBtn.addActionListener(e -> onReload.stream().forEach(r -> r.run()));
			
		JPanel allowedNodes = new JPanel();
			allowedNodes.setLayout(new GridBagLayout());
			allowedNodes.setBorder(new TitledBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED),
					"allowed nodes"
				));
		int y = 0;
		
		for(var gp : nodeGroups.keySet().stream().sorted().toList()) {
			JPanel group = new JPanel();
			group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
			group.setBorder(new TitledBorder(
					BorderFactory.createDashedBorder(Color.black),
					gp.toString()
				));
			
			JTextArea context = new JTextArea();
				onReload.add(() -> context.setText(nodeGroups.get(gp).toString()));
				context.setEditable(false);
				context.setLineWrap(true);
				context.setLayout(getLayout());
				
			int ty = 0;
			group.add(context, Presentable.createGbc(0, ty++));
			
			for(var v : gp.allowedNodes) {
				var l = new JLabel(v.toString());
					l.setAlignmentX(Component.LEFT_ALIGNMENT);
				group.add(l, Presentable.createGbc(0, ty++));
			}
			
			allowedNodes.add(group, Presentable.createGbc(0, y++));
		}
		
		JTable nodeTabel = new JTable(new DefaultTableModel(new Object[0][], new Object[] {"current draggable nodes"})){
				private static final long serialVersionUID = 2L;
				public boolean isCellEditable(int row, int column) { return false; }
			};
			nodeTabel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			nodeTabel.setCellEditor(null);
			
			onReload.add(() -> {
					var model = (DefaultTableModel)nodeTabel.getModel();
						model.setRowCount(0);
						model.setColumnCount(1);
					draggableNodes.stream().sequential()
							.map(n -> n.getTitle() + "    : " + n.toString())
							.sorted()
							.forEach(n -> model.addRow(new Object[] {n}));
				});
		
		for(var a : reloadBtn.getActionListeners()) a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		y = 0;
		content.add(reloadBtn);
		content.add(allowedNodes);
		content.add(new JScrollPane(nodeTabel,
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
					));
	}
	public void genUI_editorDetails_connection(JPanel content) {
		ArrayList<Runnable> onReload = new ArrayList<>();
		
		JButton reloadBtn = new JButton(MainWin.getImageIcon("res/refresh.png", MainWin.stdtabIconSize));
			reloadBtn.setMnemonic(KeyEvent.VK_R);
			reloadBtn.setToolTipText("refresh this tabbs information");
			reloadBtn.addActionListener(e -> onReload.stream().forEach(r -> r.run()));
		
		JPanel minimap = new JPanel() {
				private static final long serialVersionUID = 1L;
				final Color[] colors = new Color[] {Color.red, Color.green, Color.blue, Color.orange, Color.cyan, Color.pink, Color.magenta, Color.yellow}; 
				int ci = 0;
				private Color getAColor() {
					ci += 1;
					ci %= colors.length;
					return colors[ci];
				}

				double calculateArcAngle(int startX, int startY, int endX, int endY, double startAngle) {
					   // Calculate the angle between the start point and the end point
					   double deltaX = endX - startX;
					   double deltaY = endY - startY;
					   double angle = Math.atan2(deltaY, deltaX);	

					   // Convert the angle to degrees
					   angle = Math.toDegrees(angle);

					   // Normalize the angle to be between 0 and 360
					   if (angle < 0) {
					       angle += 360;
					   }

					   // Subtract the start angle from the calculated angle to get the arc angle
					   double arcAngle = angle - startAngle;

					   // Ensure the arc angle is positive
					   if (arcAngle < 0) {
					       arcAngle += 360;
					   }

					   return arcAngle;
					}
				
				@Override public void paint(Graphics g) {
					super.paint(g);
					
					int 
						iw = getWidth(),
						ih = getHeight(),
						compSpace = 10,
						compSpaceDiv2 = compSpace /2;
					float
						degPerConn = 15;
					
					HashMap<NodeComponent<?>, Point> compLocations = new HashMap<>();
					HashMap<NodeComponent<?>, Float> passedComps = new HashMap<>();
					draggableNodes.stream()
						.flatMap(n -> {
							var comps = n.getConnectableNodeComponents();
							int x = n.getX(), y = n.getY();
							for(var comp : comps) {
								y+= compSpace;
								g.setColor(Color.black);
								g.drawString("["+(
										(comp instanceof NodeSupplier) ? "O" 
											: (comp instanceof NodeConsumer) ? "I"
													: "") + "] " 
										+ comp.getName(), x+compSpace+2, y+compSpace);
								
								//draw rectangle
								g.setColor(getAColor());
								g.fillRect(x, y, compSpace, compSpace);
								
								//draw rectangle outline
								g.setColor(g.getColor().darker());
								g.drawRect(x, y, compSpace, compSpace);
								
								compLocations.put(comp, new Point(x+compSpaceDiv2, y+compSpaceDiv2));
								
								y+=compSpace;
							}
							
							g.setPaintMode();
							g.setColor(Color.black);
							g.drawString(n.getTitle(), n.getX(), n.getY());
							return comps.stream();
						})
						.toList()
						.stream()
						.forEach(comp -> {
							passedComps.clear();
							
							Point cp = compLocations.get(comp);
							
							comp.getDirectConnections().stream()
								.flatMap(c -> c.getDirectleyConnectedComponents().stream())
								.forEach(otherComp -> {
									Point ocp = compLocations.get(otherComp);
									
									g.setXORMode(getAColor());
									
									if(passedComps.containsKey(otherComp)) {
										float deg = passedComps.get(otherComp);
										
										g.drawArc(
												cp.x,
												cp.y,
												ocp.x,
												ocp.y,
												(int)deg,
												(int)calculateArcAngle(
														cp.x,
														cp.y,
														ocp.x,
														ocp.y,
														deg
													)
											);
										
										if(deg > 0)
											deg *= -1;
										else
											deg += Math.abs(deg) + degPerConn;
										
										passedComps.put(otherComp, deg);
									}else {
										g.drawLine(cp.x, cp.y, ocp.x, ocp.y);
										
										float deg = 0;//(float)Math.atan2(cp.y - ocp.y, cp.x - ocp.x);
										
										passedComps.put(otherComp, deg + degPerConn);
									}
								});
						})
						;
					
					g.setXORMode(Color.red);
					int //forgot there's no way to consider the layout's scale without some more variables and type checking. not worth it.
						w  	= self.getWidth(),
						h	= self.getHeight(), 
						th 	= (int)(h / w * iw),
						tw 	= (int)(w / h * ih);
					g.drawLine(0, 0, tw, th);
					
					g.setXORMode(Color.green);
					g.drawRect(0, 0, w, h);
					g.drawLine(0, 0, w, h);
					g.drawLine(0, h, w, 0);
				}
			};
			onReload.add(() -> minimap.repaint());
		
		JPanel minimapControlWrapper = new JPanel(new GridBagLayout());
		
		JPanel inspector = new JPanel(new GridBagLayout());
			JComboBox<String> connectionList = new JComboBox<>();
				onReload.add(() -> connectionList.setModel(new DefaultComboBoxModel<String>(draggableNodes.stream().map(n -> n.getTitle()).toArray(String[]::new))));
			JButton resetMinimapSizeBtn = new JButton("reset size");
				resetMinimapSizeBtn.setToolTipText(resetMinimapSizeBtn.getName());
				resetMinimapSizeBtn.addActionListener(e -> minimap.setPreferredSize(null));
		
		BiFunction<Integer, Integer, GridBagConstraints>	gc = (x,y) -> {var c = Presentable.createGbc(x, y); c.weighty = 0; return c;};
		{//miniMap
			minimapControlWrapper.setBorder(new TitledBorder(
					BorderFactory.createBevelBorder(BevelBorder.LOWERED),
					"mini map options"
				));
			
			Function<Integer, SpinnerNumberModel>	newModel = (num) 	-> new SpinnerNumberModel((int)num, 1, Integer.MAX_VALUE, 1);
			Function<JSpinner, Integer>				getValue = (s)	-> (int)s.getValue();
			JSpinner //size control for the minimap
				bx = new JSpinner(newModel.apply(MainWin.stdDimension.width)),		
				by = new JSpinner(newModel.apply(MainWin.stdDimension.height));
			
			onReload.add(() -> {
				bx.getModel().setValue(getWidth());
				by.getModel().setValue(getHeight());
			});
			
			Runnable setSize = () -> {
				var size = new Dimension(getValue.apply(bx), getValue.apply(by));
				minimap.setPreferredSize(size);
				minimap.setSize(size);
				minimap.revalidate();
			};
			onReload.add(setSize);
			
			bx.addChangeListener(e -> setSize.run());
			by.addChangeListener(e -> setSize.run());
			
			int x = 0, y = 0;
			minimapControlWrapper.add(new JLabel("x,y"), 	gc.apply(x++, y));
			minimapControlWrapper.add(bx, 	gc.apply(x++, y));
			minimapControlWrapper.add(by, 	gc.apply(x++, y));
			x = 0; y++;
			minimapControlWrapper.add(resetMinimapSizeBtn, gc.apply(x, y++));
		}
		{//inspector 
			
			inspector.setPreferredSize(new Dimension(MainWin.stdDimension.width / 4, inspector.getHeight()));
			int x = 0, y = 0;
			inspector.add(minimapControlWrapper,gc.apply(x, y++));
			inspector.add(connectionList,		gc.apply(x, y++));
		}
		
		for(var a : reloadBtn.getActionListeners()) a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		content.setLayout(new BorderLayout());
		content.add(new JSplitPane(SwingConstants.VERTICAL,
							new JScrollPane(inspector,	
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
							new JScrollPane(minimap,	
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
						),
						BorderLayout.CENTER
					);
		content.add(reloadBtn, BorderLayout.PAGE_START);
	}
/////////////////////
//node connection stuff
/////////////////////
	
	/*
	 * this should be triggered when the AbsoluteLayout decides to resize. this triggers when the gui is initialized
	 * - maintains the size of the nodeConnectionCanvas to that of the this(nodeEditor)
	 */
	@Override public void componentResized(ComponentEvent e) 	{ 
		synchronized(connectionImage) {
			//if panel resized, redraw all connections
			if((connectionImage == null || connectionImage.getWidth() != this.getWidth() || connectionImage.getHeight() != this.getHeight())) {
				connectionImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				
				var g = connectionImage.createGraphics();
				
				//redraw the existing connections
				draggableNodes.parallelStream()
						.flatMap(node -> node.getConnectableNodeComponents().stream())
						.flatMap(comp -> comp.getDirectConnections().stream())
						.distinct()
						.filter(conn -> !conn.needsRedrawn())
						.forEach(conn -> {		
								var style 	= conn.getConnectionStyle();
								
								//get image from the future immediately if possible
								BufferedImage connImg = style.getImageNow();
								
								if(connImg != null) {
									var offset	= style.getOffset();
									
									g.drawImage(connImg, offset.x, offset.y, null);
								}
							})
						;
				
				g.dispose();
			}
		}
	}
	@Override public void componentMoved(ComponentEvent e)  	{ }
	@Override public void componentShown(ComponentEvent e)  	{ }
	@Override public void componentHidden(ComponentEvent e) 	{ }
	
	
	volatile long 	nConnRescheduler_lastMovement 	= 0;
	long			nConnRescheduler_lastUpdate 	= 0;
	SoftReference<Polygon[]> nConnRescheduler_obsRef;
	/**
	 * triggers a line recalculation. editor to tell nodeConnection's should be recalculated/redrawn
	 * must only be attached to draggableNode's . intended for nodes that are a child of this nodeEditor.
	 */
	private ComponentListener nConnRescheduler = new ComponentListener() {
		/**
		 * gets the polygons that represents all the areas lines should try not to cross
		 * @return array of polygons
		 */
		synchronized Polygon[] getPoly() {
			Polygon[] ret;
			
			if(nConnRescheduler_lastUpdate < nConnRescheduler_lastMovement 
						|| nConnRescheduler_obsRef == null 
						|| (ret = nConnRescheduler_obsRef.get()) == null
					) {
				nConnRescheduler_lastUpdate = System.currentTimeMillis();
				ret = getLineObsticals();
				nConnRescheduler_obsRef = new SoftReference<>(ret);
			}
			
			return ret;
		}
		@Override public void componentResized(ComponentEvent e) 	{ }
		@Override public void componentMoved(ComponentEvent e)  	{
//			System.out.println("draggable node editor > nConnRescheduler , component moved : " + e.getSource());
			if(e.getSource() instanceof DraggableNode<?> node) { //this should always be true but just in case (i suspect something will break down the line and this might save it)
				
				node.getConnectableNodeComponents().stream()
					.flatMap(comp -> comp.getDirectConnections().stream())
					.forEach(conn -> {
						synchronized(connectionImage) {
							conn.draw(getPoly(), connectionImage, self);
						}
					})
					;
				
			}else {
				assert false : "componentEvent source comp :" + e.getComponent();
			}
		}
		@Override public void componentShown(ComponentEvent e)  	{ }
		@Override public void componentHidden(ComponentEvent e) 	{ }
	};

	@Override public void paint(Graphics g) {
		g.setColor(Color.orange);
		g.drawLine(0, getHeight(), getWidth(), 0);
		g.drawLine(0, 0 , getWidth(), getHeight());
		g.setPaintMode();
		
		super.paint(g);
		
		if(cacheDrawnLines) synchronized(connectionImage) {
			if(!ConnectionsToReimposeQueue.isEmpty()) {
				var cig = connectionImage.createGraphics();
				
				ConnectionsToReimposeQueue.stream()
					.map(ref -> ref.get())
					.filter(conn -> conn != null && !conn.isPoinless())
					.distinct()
					.map(conn -> conn.getConnectionStyle())
					.forEach(style-> {
						BufferedImage img = style.getImageNow();
						
						if(img == null) return;
						
						var offset = style.getOffset();
						
						cig.drawImage(img, offset.x, offset.y, null);
					});
				
				ConnectionsToReimposeQueue.clear();
				
				cig.dispose();
			}
			
		}
		else synchronized(connectionImage)
		{
			var cig = connectionImage.createGraphics();
			cig.setColor(new Color(0,0,0,0));
			cig.fillRect(0,  0, connectionImage.getWidth(), connectionImage.getHeight());
			
			ConnectionsToReimposeQueue.clear();
			
			getConnections()
				.map(conn -> conn.getConnectionStyle())
				.forEach(style -> {
					var offset = style.getOffset();
					var img = style.getImageNow();
					if(img == null || offset == null) return;
					
					g.drawImage(img, offset.x, offset.y, null);
				})
				;
			
			cig.dispose();
		}
		
		g.drawImage(connectionImage, 0, 0, null);
	}
	
	@Override public void revalidate() {
		super.revalidate();
		revalidateConnections();
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