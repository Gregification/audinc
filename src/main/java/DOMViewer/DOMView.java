package DOMViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import DOMViewer.nodeObjects.DFolderNodeObj;
import audinc.gui.MainWin;

/**
 */
/*
 * - root node user-object is a Path.class instance
 */
public abstract class DOMView extends JPanel {	
	public JPanel eleView;
	public final JTree domTree;
	public final DefaultMutableTreeNode domTree_root;
	public final DefaultTreeModel domTree_model;
	
	protected JPopupMenu nodeOptionsPopupMenu;
	protected JScrollPane tv_sp, ev_sp;	//tree/element view scroll pane
	protected ExecutorService executor;	//mainly for refreshing the tree
	
	private DefaultMutableTreeNode currentShownNode;
	
	public DOMView() {
		this(MainWin.settingsDir);
	}
	public DOMView(Path root) {
		super(new GridBagLayout());
		
		domTree_root 	= new DefaultMutableTreeNode(null, true);
		domTree 		= new JTree(this.domTree_root);
		domTree_model 	= (DefaultTreeModel)domTree.getModel();
		
		initMouseListener();
		initGUI();
		
		setRoot(root);
		
		domTree_model.reload(domTree_root);
	}
	
	public void setRoot(Path root) {
		domTree_root.setUserObject(
				new DFolderNodeObj(
					root.getFileName().toString() + " | " +root.toAbsolutePath().toString(),
					root)
			);
		
		domTree.setSelectionPath(new TreePath(domTree_root.getPath()));
		nodeOptions_refresh();
	}
	
	public void close() {
		executor.shutdownNow();
	}
		
///////////////////
//gui
///////////////////	
	protected abstract  void nodeOptionsPopupMenu_actionEvent(nodeOptionEnum option, ActionEvent e);
	protected abstract void nodeOptions_refresh(); //hell :fire: :fire: :brim-stone:
	
	protected abstract void initNodeOptionsPopupMenu();
	protected JPopupMenu getPopupMenu(TreePath[] treenode) {
		return this.nodeOptionsPopupMenu;
	}
	
	protected void updateTreeViewForNode(DefaultMutableTreeNode treenode) {
		// see answer by Araon Digulla ->  https://stackoverflow.com/questions/2822695/java-jtree-how-to-check-if-node-is-displayed#:~:text=getViewport().,true%20%2C%20the%20node%20is%20visible.
		
		var path = new TreePath(treenode.getPath());
		if(		domTree.isVisible(path) &&		//is the tree allowing it to show? 
				tv_sp.getViewport().getViewRect().intersects(domTree.getPathBounds(path))) //is it inside the view port of the scrollable window? 
			{
			
			domTree_model.reload(treenode);
		}
	}
		
	protected void initGUI() {
		this.eleView 	= new JPanel();
		
		tv_sp = new JScrollPane(domTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tv_sp.setPreferredSize(new Dimension(MainWin.stdDimension.width/5, 30));
		
		ev_sp = new JScrollPane(eleView,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		var c = new GridBagConstraints();
		c.weightx = c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		
		this.setBackground(Color.red);
		this.add(new JSplitPane(SwingConstants.VERTICAL, tv_sp, ev_sp), c);
		
		initNodeOptionsPopupMenu();
	}

///////////////////
//MouseListener
///////////////////
	protected void initMouseListener() {
		 MouseListener ml = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		    	if (SwingUtilities.isRightMouseButton(e)) {
		    		
		    		//selection helper
		    		if(domTree.getSelectionCount() == 0) {
		    			int row = domTree.getClosestRowForLocation(e.getX(), e.getY());
			 	        domTree.setSelectionRow(row);
		    		}else {
		    			int row = domTree.getRowForLocation(e.getX(), e.getY());
		    			if(row >= 0) {
		    				boolean isPartOfSelection = false;
		    				for(int r : domTree.getSelectionRows())
		    					if(r == row) {
		    						isPartOfSelection = true;
		    						break;
		    					}
		    				
		    				if(!isPartOfSelection) {
		    					domTree.setSelectionRow(row);
		    				}
		    			}
		    		}
		    		
		    		//pop-up menus
		    		if(e.getClickCount() == 1) {
		    			getPopupMenu(domTree.getSelectionPaths()).show(e.getComponent(), e.getX(), e.getY());
		    		}
		    		
		 	    } else if(SwingUtilities.isLeftMouseButton(e)) {
		 	    	if(domTree.getSelectionCount() == 1) {
		 	    		var node = (DefaultMutableTreeNode)domTree.getLastSelectedPathComponent();
		 	    		
		 	    		if(currentShownNode == null || !currentShownNode.equals(node)) {	//stops display update if nothing new is being shown
		 	    			displayNode(node);
		 	    			currentShownNode = node;
		 	    		}
		 	    	}
		 	    }
		     }
		 };
		 domTree.addMouseListener(ml);
	}
	
	protected abstract void displayNode(DefaultMutableTreeNode dmtn);

///////////////////
//ignore
///////////////////
	protected void finalize() throws Throwable{
		close();
	}
	
	private static final long serialVersionUID = -418815011312738133L; //eclipse requirement
}