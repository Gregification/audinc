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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
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
	
	private JPopupMenu nodeOptionsPopupMenu;
	
	public DOMView() {
		super(new GridBagLayout());
		
		domTree_root = new DefaultMutableTreeNode(MainWin.settingsDir, true);
		
		domTree = new JTree(this.domTree_root);
		initMouseListener();
		
		initGUI();
	}
	
	public DOMView(Path root) {
		super(new GridBagLayout());
		
		domTree_root = new DefaultMutableTreeNode(
				new DOMNodeObject(
						root.getFileName().toString(),
						root)
				, true);
		
		domTree = new JTree(this.domTree_root);
		initMouseListener();
		
		initGUI();
	}
	
	public void parse() {
		
	}
	
	public void setRoot(Object newObj) {
		domTree_root.setUserObject(newObj);
		
		System.out.println("validating");
		
		var v = (DefaultTreeModel)domTree.getModel();
		v.nodeChanged(domTree_root);
	}
	
	
	
///////////////////
//gui
///////////////////	
	protected void nodeOptionsPopupMenu_actionEvent(treeNodeOption option) {
		switch(option) {
			case DELETE :
				nodeOptions_deleteSelected();
				break;
			case PARSE_SELF : 
				nodeOptions_parseSelf();
				break;
			case PARSE_CHILDREN : 
				nodeOptions_parseChildren();
				break;
			case REFRESH :
				nodeOptions_refresh();
				break;
				
			default :
				System.out.println("DOMViewer/DOMView.java/nodeOptionsPopupMenu_actionEvent(treeNodeOption) : forgot to impliment a parsing option");
				break;
		}
	}
	private void nodeOptions_deleteSelected() {
		domTree.removeSelectionPaths(domTree.getSelectionPaths());
	}
	private void nodeOptions_refresh() {
		var nodes = List.of(domTree.getSelectionPaths());
		 
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
				System.out.println("removing: \t" + current);
				itt.remove();
				continue;
			}
			
			System.out.println("keeping: \t" + current);
			latestParent = current;
		}
		System.out.println("final:");
		nodes.forEach(System.out::println);
		
		
		//re-traverse nodes. using executor to handle work loading.
		
//		 steveJobs = new PriorityBlockingQueue<DefaultMutableTreeNode>(	//(he holds all the jobs)
//				nodes.size() + 5, 							//initial size 
//				(o1, o2) -> Boolean.compare(o1.getAllowsChildren(), o2.getAllowsChildren())	//files get higher priority than folders
//			);
		
		ExecutorService executor = Executors.newCachedThreadPool();//(we want jobs)
		
		nodes.stream()	//init jobs. a small initial investment(1 bajillion baboons)
			.map(e -> ((DefaultMutableTreeNode)e.getLastPathComponent()))
			.forEach(e -> parse_recursive1(e, executor));
		nodes.clear();
		
	}
	private void nodeOptions_parseSelf() {}
	private void nodeOptions_parseChildren() {}
	
	//parse and add to GUI
	protected void ParseFile(File file, DefaultMutableTreeNode relRoot) {
		
	}
	
	/*
	 * - self populates new jobs
	 * - prioritizes folders over files
	 * - no file filter
	 */
	protected Runnable parse_recursive1(DefaultMutableTreeNode treenode, ExecutorService executor) {
		return () -> {
			var node	= (DOMNodeObject)treenode.getUserObject();
			File src	= ((Path)node.value()).toFile();
			
			if(src.exists()) {
				if(src.isDirectory()) {	
					List.of(src.listFiles()).stream()
						.sorted((o1, o2) -> Boolean.compare(o1.isDirectory(), o2.isDirectory()))	//priority = directories
						.forEach(subFile -> {	//parse folder contents back onto the queue
							var subPath 	= src.toPath();
							var subObject 	= new DOMNodeObject(subPath.getFileName().toString(), subPath);
							
							var subNode 	= new DefaultMutableTreeNode(subObject, true);	//hard coded [true] because its a folder => will always be able to have children
							
							treenode.add(subNode);
							
							//create new job
							executor.execute(parse_recursive1(subNode, executor));
						});
				}else {	
					//parse the file
					ParseFile(src, treenode);
				}
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
		
		//link JMenus & JMenuItems to corresponding JMenus
		List.of(treeNodeOption.values()).stream()
			.forEach(e ->{
				JMenuItem item = null;
				
				if(jmenus.containsKey(e))
					item = jmenus.get(e);
				else
					item = new JMenuItem(e.toString());
				
				if(e.getChildOf() == null)
					this.nodeOptionsPopupMenu.add(item);
				else
					jmenus.get(e.getChildOf()).add(item);
			});
	}
	
	protected void initGUI() {
		this.eleView 	= new JPanel();
		
		JScrollPane tv_sp = new JScrollPane(domTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tv_sp.setPreferredSize(new Dimension(MainWin.stdDimension.width/5, 30));
		
		JScrollPane ev_sp = new JScrollPane(eleView,
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
		    		int row = domTree.getClosestRowForLocation(e.getX(), e.getY());
		 	        domTree.setSelectionRow(row);
		 	        var r = domTree.getPathForRow(row);
		    		if(row != -1) {
			             if(e.getClickCount() == 1) {
			            	 nodeOptionsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			             }
			         }
		 	    }
		     }
		 };
		 domTree.addMouseListener(ml);
	}
}