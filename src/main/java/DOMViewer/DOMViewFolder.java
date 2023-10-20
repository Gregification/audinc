package DOMViewer;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import DOMViewer.nodeObjects.DFolderNodeObj;

/*
 * for displaying the file system
 */
/** 
 */
public class DOMViewFolder extends DOMView<DOMViewer.DOMViewFolder.popupOptions, DOMViewer.DOMViewFolder.popupLimit> {

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

	
	@Override protected void filterPopupLimits(DefaultMutableTreeNode node, EnumSet<popupLimit> sharedLimits) {
		sharedLimits.remove(node.getAllowsChildren() ? 		//if it allows children => its a folder, therefore it cannot be a file. visa versa
				popupLimit.ON_FILE : popupLimit.ON_FOLDER);
	}

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
			
//			System.out.println("flag set of " + title + " \t " + displayFlags);
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
		
		@Override public Enum<? extends PopupOptionable>[] getValues() {
			return popupOptions.values();
		}
		
	}
	
	enum popupLimit implements PopupFilterable {
		ON_FOLDER,
		ON_FILE,
		ON_ALL
		;
	}

	@Override
	protected Class<popupOptions> getOptionEnum() {
		return popupOptions.class;
	}

	@Override
	protected Class<popupLimit> getFilterEnum() {
		return popupLimit.class;
	}

	@Override
	protected popupOptions[] getOptionEnumOptions() {
		return  popupOptions.values();
	}

	@Override
	protected popupLimit getAllFilter() {
		return popupLimit.ON_ALL;
	}
}