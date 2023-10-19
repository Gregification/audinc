package DOMViewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import DOMViewer.nodeObjects.DFolderNodeObj;

/*
 * for displaying the file system
 */
/** 
 */
public class DOMViewFolder extends DOMView {
	
	@Override protected void nodeOptions_refresh() {
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
				itt.remove();
				continue;
			}

			latestParent = current;
		}
		
		
		//re-traverse nodes. using executor to handle work loading.

		executor = Executors.newCachedThreadPool();//(we want jobs)
		
		nodes.stream()	//init jobs. a small initial investment(1 bajillion baboons)
			.map(e -> ((DefaultMutableTreeNode)e.getLastPathComponent()))
			.forEach(e -> {
					e.removeAllChildren();
					executor.execute(parse_recursive1(e, executor));	//42
				});
		
	}

	/*
	 * - self populates new jobs
	 * - prioritizes folders over files
	 * - no file filter
	 */
	protected Runnable parse_recursive1(DefaultMutableTreeNode treenode, ExecutorService executor) {
		return () -> {			
			assert treenode.getUserObject() instanceof DFolderNodeObj	//constructor & this.setRoot(Path) should guarantee this
				: "\tnode object is not instanceof DOMNodeObject, userobject -> "+(treenode.getUserObject() == null ? "IS NULL" : (treenode.getUserObject().getClass()));
			
			var node	= (DFolderNodeObj)treenode.getUserObject();
			File src	= node.getPath().toFile();
			
			if(src.exists()) {
				if(src.isDirectory()) {						
					List.of(src.listFiles()).stream()
						.sorted((o1, o2) -> Boolean.compare(o1.isFile(), o2.isFile()))	//priority = directories
						.parallel()
						.forEach(subFile -> {	//parse folder contents back onto the queue
							var subPath 	= subFile.toPath();
							var subObject 	= new DFolderNodeObj(subPath.getFileName().toString(), subPath);
							
							var subNode 	= new DefaultMutableTreeNode(subObject, subFile.isDirectory());
							treenode.add(subNode);
							
							//create new job
							executor.execute(parse_recursive1(subNode, executor));
						});
				}else {	
					//update to leaf, parse the file
					
				}
				
				System.out.println("DOMViewFolder: "  + treenode);
				updateTreeViewForNode(treenode);
			}
		};
	}

	
	@Override
	protected void displayNode(DefaultMutableTreeNode dmtn) {
		// TODO Auto-generated method stub
		System.out.println("displaying node: " + dmtn.toString());
	}
	
	@Override protected void nodeOptionsPopupMenu_actionEvent(PopupOptionable option, ActionEvent e) {
		assert option instanceof popupOptions : "whar??";
		
		var optionEnum = (popupOptions)option;
		switch(optionEnum) {
			default:
				System.out.println("i=umimplimented popup menu event : " + optionEnum);
		}
	}
	
	@Override protected void initNodeOptionsPopupMenu() {
		this.popupMenuMap 			= new HashMap<>();
		this.popupChildren			= new HashMap<>();
		
		var tmp_popupMenuTopElements	= new ArrayList<JMenuItem>();
		
		//create JMenu object for everything that should have one
		Map<popupOptions, JMenu> jmenus = List.of(popupOptions.values()).stream()
				.map(popupOptions::getChildOf)
				.filter(e -> e != null)
				.distinct()
				.collect(Collectors.toMap(
						e -> e, 
						e -> new JMenu(e.toString())));
		
		//link JMenus & JMenuItems to corresponding JMenus. also puts to the GUI
		List.of(popupOptions.values()).stream()
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
	
	@Override protected JPopupMenu getPopupMenu(TreePath[] treenodes){
		EnumSet<popupLimit> sharedLimits = EnumSet.allOf(popupLimit.class);
		
		for(var tn : treenodes) {
			var dmtn = (DefaultMutableTreeNode)tn.getLastPathComponent();
			
			filterPopupLimits(dmtn, sharedLimits);
			
			if(sharedLimits.size() == 1) break; //stop if the only limit left is [All]
		}
		
		popupLimit lim_all = popupLimit.values()[0].getFlagForAll();
		
		sharedLimits.add(lim_all);//no static method for interfaces for enums :( i give up
		//at this point everything we want to show up is in sharedLimits
		
		//apply it to the popupMenu
		return getPopupMenu(sharedLimits);
	}
	
	protected void filterPopupLimits(DefaultMutableTreeNode node, EnumSet<popupLimit> sharedLimits) {
		sharedLimits.remove(node.getAllowsChildren() ? 		//if it allows children => its a folder, therefore it cannot be a file. visa versa
				popupLimit.ON_FILE : popupLimit.ON_FOLDER);
	}
	
	protected JPopupMenu getPopupMenu(EnumSet<popupLimit> sharedLimits) {
		assert this.popupMenuMap != null : "did not initialize popup menu map";
		System.out.println("shared limits of slection: " + sharedLimits);
		
		JPopupMenu menu = new JPopupMenu();
		
		for(var top : this.popupMenuTopElements) {
			popupMenu_addApplicableElements(top, null, sharedLimits, menu);
		}
	
		return menu;
	}
	
	protected void popupMenu_addApplicableElements(JMenuItem src, JMenu host,  EnumSet<? extends PopupFilterable> allowedFlags, JPopupMenu menu) {
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
				
				System.out.println("popup menu element:" + src.getText() + " \t host:" + (host == null ? "null" : host.getText()) + " \t node flags:" + currentFlags + "\t allowed");
				return;
			}
		}
		
		System.out.println("popup menu element:" + src.getText() + " \t host:" + (host == null ? "null" : host.getText()) + " \t node flags:" + currentFlags + "\t not allowed");
		
		if(host != null)
			menu.remove(src);
	}
	

	private static final long serialVersionUID = -6509558498762564268L;


	enum popupOptions implements PopupOptionable {
		CLEAR			("clear",
						"remove view of this node, does effect the file system"),
		DELETE			("delete",
					"removes from file system (cannot be undone)"),
		REFRESH			("refresh",
					"ignore local changes, rebase from the file system"),
		PARSE_d			("parse"),
		PARSE_SELF		("this", 
						"parse this node (includes children)",
						PARSE_d),
		PARSE_CHILDREN	("children",
						"parse children only",
						PARSE_d,
						popupLimit.ON_FOLDER),
		PARSE_CUSTOM	("custom",
						"view parsing options",
						PARSE_d,
						popupLimit.ON_FILE),
		SAVE_d			("save"),
		SAVE_OVERWRITE	("overwrite",	
						"overwrite the selected file/folder(s)",
						SAVE_d),
		SAVE_AS			("save as ...",
						SAVE_d)
		;
		private String 
		title,
		tooltiptext;
		private EnumSet<popupLimit> displayFlags = EnumSet.noneOf(popupLimit.class);
		private popupOptions childOf;
		
		private popupOptions(String title) {
			this(title, "");
		}	
		private popupOptions(String title, String tooltiptext) {
			this(title, tooltiptext, popupLimit.ON_ALL);
		}
		private popupOptions(String title, String tooltiptext, popupLimit... flagSet) {
			this(title, tooltiptext, null, flagSet);
		}
		private popupOptions(String title, popupOptions parent) {
			this(title, "", parent);
		}	
		private popupOptions(String title, String tooltiptext, popupOptions parent, popupLimit... flagSet) {
			this.title = title;
			this.tooltiptext = tooltiptext;
			this.childOf = parent;
			this.displayFlags.addAll(Arrays.asList(flagSet));
			
			if(flagSet.length == 0) displayFlags.add(defaultFlag());
			
			System.out.println("flag set of " + title + " \t " + displayFlags);
		}
		
		public String getTitle() {
			return this.title;
		}
		
		public popupOptions getChildOf() {
			return this.childOf;
		}
		
		@Override public String getTooltipText() {
			return this.tooltiptext;
		}
		
		public EnumSet<popupLimit> getDisplayFlags(){
			return this.displayFlags;
		}
		
		private popupLimit defaultFlag() {
			return popupLimit.ON_ALL;
		}
	}
	
	enum popupLimit implements PopupFilterable {
		ON_FOLDER,
		ON_FILE,
		ON_ALL
		;
		
		@Override public popupLimit getFlagForAll() {
			return popupLimit.ON_ALL;
		}

		@Override
		public int getSizeWithoutAll() {
			return 0;
		}
	}

}