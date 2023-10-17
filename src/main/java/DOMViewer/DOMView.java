package DOMViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

import audinc.gui.MainWin;

/*
 * made for file trees
 */
public class DOMView extends JPanel {
	public JPanel eleView;
	public final JTree domTree;
	public final DefaultMutableTreeNode domTree_root;
	public final DefaultTreeModel domTree_model;
	
	private JPopupMenu nodeOptionsPopupMenu;
	private JScrollPane tv_sp, ev_sp;	//tree/element view scroll pane
	private ExecutorService executor;	//mainly for refreshing the tree
	
	
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
				new NFSystem(
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
	protected void nodeOptionsPopupMenu_actionEvent(treeNodeOption option, ActionEvent e) {
		System.out.println("node option selected: " + option.toString());
		switch(option) {
			case CLEAR :
				nodeOptions_clearSelected();
				break;
			case DELETE :
				nodeOptions_deleteSelected();
				break;
			case PARSE_d :
			case PARSE_SELF : 
				nodeOptions_parseSelf();
				break;
			case PARSE_CHILDREN : 
				nodeOptions_parseChildren();
				break;
			
			case REFRESH :
				nodeOptions_refresh();
				break;
				
			case SAVE_d :
			case SAVE_OVERWRITE :
				System.out.println("not implimented : SAVE_OVERWRITE");
				break;
			case SAVE_AS :
				System.out.println("not implimented : SAVE_AS");
				break;
				
			default :
				System.out.println("DOMViewer/DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption) : forgot to impliment a parsing option");
				break;
		}
	}
	
	private void nodeOptions_deleteSelected() {
		//TODO
	}
	private void nodeOptions_clearSelected() {
		domTree.removeSelectionPaths(domTree.getSelectionPaths());
	}
	private void nodeOptions_refresh() {
		var nodes = List.of(domTree.getSelectionPaths()).stream().collect(Collectors.toCollection(ArrayList::new));//make mutable list
		 
		/*
		 * filter for relevant nodes
		 * details:
		 * only allow BRANCHES that are NOT DECENDENTS of other selected branches.
		 * see diagram for details: https://media.discordapp.net/attachments/1162543720613302293/1162558001211768892/image.png?ex=653c5f82&is=6529ea82&hm=616f0aee041238ed943ab11f215dae8909324701d1815e17129f953892f36dbd&= 
		 * 	- circled 	=> selected paths
		 *  	- red 		=> ignored & filtered out
		 *  	- blue 		=> keep
		 */
		TreePath current, latestParent = nodes.get(0);
		var itt = nodes.iterator(); 
			if(itt.hasNext()) itt.next(); //skip first element since its the first possible parent
			
		while(itt.hasNext()) {
			current = itt.next();
			if(current.isDescendant(latestParent) ||		//if is a decendent of another node
				!((DefaultMutableTreeNode)current.getLastPathComponent()).getAllowsChildren()	//if is not a branch
				){
//				System.out.println("removing: \t" + current);
				itt.remove();
				continue;
			}
			
//			System.out.println("keeping: \t" + current);
			latestParent = current;
		}
//		System.out.println("final root set, size("+nodes.size()+"):");
		
		
		//re-traverse nodes. using executor to handle work loading.

		executor = Executors.newCachedThreadPool();//(we want jobs)
		
//		System.out.println("init: executor, (root node of tree: " + domTree_root + ")");
		nodes.stream()	//init jobs. a small initial investment(1 bajillion baboons)
			.map(e -> ((DefaultMutableTreeNode)e.getLastPathComponent()))
			.forEach(e -> {
//					System.out.println("init: loading ... " + e);
					e.removeAllChildren();
					executor.execute(parse_recursive1(e, executor));	//42
				});
//		System.out.println("init: executor, finished");
		
	}
	private void nodeOptions_parseSelf() {}
	private void nodeOptions_parseChildren() {}
	
	private void updateTreeViewForNode(DefaultMutableTreeNode treenode) {
		// see answer by Araon Digulla ->  https://stackoverflow.com/questions/2822695/java-jtree-how-to-check-if-node-is-displayed#:~:text=getViewport().,true%20%2C%20the%20node%20is%20visible.
		
		var path = new TreePath(treenode.getPath());
		if(		domTree.isVisible(path) &&		//is the tree allowing it to show? 
				tv_sp.getViewport().getViewRect().intersects(domTree.getPathBounds(path))) //is it inside the view port of the scrollable window? 
			{
			
			domTree_model.reload(treenode);
		}
	}

	/*
	 * - self populates new jobs
	 * - prioritizes folders over files
	 * - no file filter
	 */
	protected Runnable parse_recursive1(DefaultMutableTreeNode treenode, ExecutorService executor) {
		return () -> {
//			System.out.println("parse_recursive1 | \t" + treenode.toString() +  "\t | "+ Thread.currentThread().getName());
			
			assert treenode.getUserObject() instanceof NFSystem	//constructor & this.setRoot(Path) should guarantee this
				: "\tnode object is not instanceof DOMNodeObject, userobject -> "+(treenode.getUserObject() == null ? "IS NULL" : (treenode.getUserObject().getClass()));
			
			var node	= (NFSystem)treenode.getUserObject();
			File src	= node.path.toFile();
			
			if(src.exists()) {
				if(src.isDirectory()) {						
					List.of(src.listFiles()).stream()
						.sorted((o1, o2) -> Boolean.compare(o1.isFile(), o2.isFile()))	//priority = directories
						.forEach(subFile -> {	//parse folder contents back onto the queue
							var subPath 	= subFile.toPath();
							var subObject 	= new NFSystem(subPath.getFileName().toString(), subPath);
							
							var subNode 	= new DefaultMutableTreeNode(subObject, true);	//hard coded [true] because its a item => will always be able to have children
							
//							System.out.println("\t" + treenode + " > " + subNode + " = " + subFile);
							treenode.add(subNode);
							
							//create new job
							executor.execute(parse_recursive1(subNode, executor));
						});
				}else {	
					//parse the file
					DOMParser.Parse(src, treenode);
				}
				System.out.println(treenode + " \t[is leaf]=" + (node));
				updateTreeViewForNode(treenode);
			}
		};
	}
	
	private void initNodeOptionsPopupMenu() {
		this.nodeOptionsPopupMenu = new JPopupMenu("popup menu");
		
		//create JMenu object for everything that should have one
		Map<treeNodeOption, JMenu> jmenus = List.of(treeNodeOption.values()).stream()
				.map(treeNodeOption::getChildOf)
				.filter(e -> e != null)
				.distinct()
				.collect(Collectors.toMap(
						e -> e, 
						e -> new JMenu(e.toString())));
		
		//link JMenus & JMenuItems to corresponding JMenus. also puts to the GUI
		List.of(treeNodeOption.values()).stream()
			.forEach(tno ->{
				JMenuItem item = null;
				
				if(jmenus.containsKey(tno))
					item = jmenus.get(tno);
				else
					item = new JMenuItem(tno.toString());
				
				item.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							nodeOptionsPopupMenu_actionEvent(tno, e);
						}
					});
				
				if(!tno.getTooltipText().isBlank())
					item.setToolTipText(tno.getTooltipText());
				
				
				if(tno.getChildOf() == null)
					this.nodeOptionsPopupMenu.add(item);
				else
					jmenus.get(tno.getChildOf()).add(item);
			});
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
	private void initMouseListener() {
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
		    			
		    			
		    			
		    			nodeOptionsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
		    		}
		 	    }
		     }
		 };
		 domTree.addMouseListener(ml);
	}

///////////////////
//private \ protected
///////////////////
	protected void finalize() throws Throwable{
		close();
	}
}