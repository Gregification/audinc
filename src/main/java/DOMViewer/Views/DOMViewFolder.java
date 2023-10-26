package DOMViewer.Views;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import DOMViewer.DOMView;
import DOMViewer.PopupFilterable;
import DOMViewer.PopupOptionable;
import DOMViewer.nodeObjects.DFolderNodeObj;

/*
 * for displaying a single folder (ignores internal contents)
 */
public class DOMViewFolder extends DOMView<DOMViewer.Views.DOMViewFolder.popupOptions, DOMViewer.Views.DOMViewFolder.popupLimit> {
	
	public DOMViewFolder(Path root) {
		super(root);
	}
	
	@Override public void setRoot(Path root) {
		domTree_root.setUserObject(
				new DFolderNodeObj(
					root.getFileName().toString() + " | " +root.toAbsolutePath().toString(),
					root)
			);
		
		domTree.setSelectionPath(new TreePath(domTree_root.getPath()));
		nodeOptions_refresh();
	}
	
	@Override protected void nodeOptionsPopupMenu_actionEvent(popupOptions option, ActionEvent e) {
		var optionEnum = (popupOptions)option;
		switch(optionEnum) {
			default:
				nodeOptions_refresh();
				System.out.println("domview file > i=umimplimented popup menu event : " + optionEnum);
		}
		
	}

	@Override protected void nodeOptions_refresh() {
		// TODO Auto-generated method stub
		System.out.println("DOMView templete> rebuilding the tree");
	}
	
	@Override protected void displayNode(DefaultMutableTreeNode dmtn) {
		// TODO Auto-generated method stub
		System.out.println("DOMView templete> displaying node:" + dmtn);
	}
	
	@Override protected void filterPopupLimits(DefaultMutableTreeNode node, EnumSet<popupLimit> sharedLimits) {
		if(!node.equals(domTree_root))
			sharedLimits.remove(popupLimit.ROOT_ONLY);
	}
	
//////////////////
// popupMenu settings
//////////////////
	
	enum popupOptions implements PopupOptionable {
		REFRESH				("refresh"	, "description"),
		REPARSE				("reparse"	, "description"),
		SAVE				("save"),
			SAVE_AS			("as", "pick a location",
								SAVE, popupLimit.ROOT_ONLY),
			OVERWRITE		("overwrite", "overwrites orgional folder, internal contents not altered",
								SAVE, popupLimit.ROOT_ONLY)
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
		ON_ALL,	//this (a universal flag) is required. everything with this will always be shown. it is also the default flag is nothing is listed
		ROOT_ONLY
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

	@Override
	protected void onLeftClick(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}
}
