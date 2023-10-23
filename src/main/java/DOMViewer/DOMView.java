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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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

import DOMViewer.nodeObjects.DFolderNodeObj;
import audinc.gui.MainWin;

/**
 */
/*
 *  displays relative parser content. in case of DOMViewFolder:no parser needed -> is built in.
 * - root node user-object is a Path.class instance
 */
public abstract class DOMView<
			popupOption extends Enum<popupOption> & PopupOptionable,	//big brain solution. crack house implementation
			popupFilter extends Enum<popupFilter> & PopupFilterable 
		>extends JPanel {
	
	public JPanel eleView;
	public final JTree domTree;
	public final DefaultMutableTreeNode domTree_root;
	public final DefaultTreeModel domTree_model;
	
	protected JScrollPane tv_sp, ev_sp;	//tree/element view scroll pane
	protected ExecutorService executor;	//for refreshing the tree
	
	protected JMenuItem[] popupMenuTopElements;
	protected Map<JMenuItem, popupOption> 		popupMenuMap; //given a limit, tells you what objects are allowed. value must be mutable list
	protected Map<JMenuItem, ArrayList<JMenuItem>>	popupChildren;
	
	private DefaultMutableTreeNode currentShownNode;
	
	public DOMView() {
		this(MainWin.settingsDir);
	}
	public DOMView(Path root){
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
	public static ArrayList<TreePath> filterForUniqueRoots(List<TreePath> rawnodes) {
		var nodes = rawnodes.stream().collect(Collectors.toCollection(ArrayList::new));//make mutable list
		
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
				itt.remove();
				continue;
			}

			latestParent = current;
		}
		
		return nodes;
	}
	
	protected abstract  void nodeOptionsPopupMenu_actionEvent(popupOption option, ActionEvent e);	//this is here instead of the relevant parser so its abstracted at least 1 level above the parsers since there could be multiple. ----------tldr: makes it so work done by the parsers can be better managed
	protected abstract void nodeOptions_refresh(); //hell :fire: :fire: :brim-stone:
	
	protected abstract Class<popupOption> getOptionEnum();
	protected abstract Class<popupFilter> getFilterEnum();
	protected abstract popupOption[] getOptionEnumOptions();
	protected abstract popupFilter getAllFilter();
	
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
	
	protected JPopupMenu getPopupMenu(TreePath[] treenodes){
		EnumSet<popupFilter> sharedLimits = EnumSet.allOf(getFilterEnum());
		
		for(var tn : treenodes) {
			var dmtn = (DefaultMutableTreeNode)tn.getLastPathComponent();
			
			filterPopupLimits(dmtn, sharedLimits);
			
			if(sharedLimits.size() == 1) break; //stop if the only limit left is [All]
		}
		
		sharedLimits.add(getAllFilter());//no static method for interfaces for enums :( i give up
		//at this point everything we want to show up is in sharedLimits
		
		//apply it to the popupMenu
		return getPopupMenu(sharedLimits);
	}
	
	protected JPopupMenu getPopupMenu(EnumSet<popupFilter> sharedLimits) {
		assert this.popupMenuMap != null : "did not initialize popup menu map";
//		System.out.println("shared limits of slection: " + sharedLimits);
		
		JPopupMenu menu = new JPopupMenu();
		
		for(var top : this.popupMenuTopElements) {
			popupMenu_addApplicableElements(top, null, sharedLimits, menu);
		}
	
		return menu;
	}
	
	protected void initNodeOptionsPopupMenu() {
		this.popupMenuMap 			= new HashMap<>();
		this.popupChildren			= new HashMap<>();
		
		var tmp_popupMenuTopElements	= new ArrayList<JMenuItem>();
		
		//create JMenu object for everything that should have one
		Map<popupOption, JMenu> jmenus = List.of(getOptionEnumOptions()).stream()
				.map(popupOption::getChildOf)
				.filter(e -> e != null)
				.distinct()
				.collect(Collectors.toMap(
						e -> (popupOption)e, 
						e -> new JMenu(e.toString())));
		
		//link JMenus & JMenuItems to corresponding JMenus. also puts to the GUI
		List.of(getOptionEnumOptions()).stream()
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
				
				item.setText(tno.getTitle());
				
				if(!tno.getTooltipText().isBlank())
					item.setToolTipText(tno.getTooltipText());
				
				//add to approate parent
				if(tno.getChildOf() == null)
					tmp_popupMenuTopElements.add(item);
				else {
					JMenuItem parent = jmenus.get(tno.getChildOf());
					parent.add(item);
					if(!this.popupChildren.containsKey(parent)) {
						popupChildren.put(parent, new ArrayList<JMenuItem>());
					}
					
					popupChildren.get(parent).add(item);
				}
				
				popupMenuMap.put(item, tno);
			});
		
		for(var v : this.popupChildren.values())
			v.trimToSize();
		
		popupMenuTopElements = tmp_popupMenuTopElements.toArray(new JMenuItem[] {});
	}
	
	protected void popupMenu_addApplicableElements(JMenuItem src, JMenu host,  EnumSet<? extends PopupFilterable> allowedFlags, JPopupMenu menu) { //most likely is doing a lot of pointless work
		PopupOptionable currentOption = popupMenuMap.get(src);
		EnumSet<? extends PopupFilterable> currentFlags = currentOption.getDisplayFlags();
		
		for(var af : allowedFlags) {
			if(currentFlags.contains(af)) {
				if(host == null)				
					menu.add(src);
				else
					host.add(src);
				
				if(src instanceof JMenu) {
					var children = this.popupChildren.get(src);
					for(var c : children) {
						popupMenu_addApplicableElements(c, (JMenu)src, allowedFlags, menu);
					}
				}
				
//				System.out.println("popup menu element:" + src.getText() + " \t host:" + (host == null ? "null" : host.getText()) + " \t node flags:" + currentFlags + "\t allowed");
				return;
			}
		}
		
//		System.out.println("popup menu element:" + src.getText() + " \t host:" + (host == null ? "null" : host.getText()) + " \t node flags:" + currentFlags + "\t not allowed");
		
		if(host != null)
			host.remove(src);
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

	protected abstract void filterPopupLimits(DefaultMutableTreeNode node, EnumSet<popupFilter> sharedLimits);
}