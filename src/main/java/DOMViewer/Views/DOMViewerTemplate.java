package DOMViewer.Views;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;

import javax.swing.tree.DefaultMutableTreeNode;

import DOMViewer.DOMView;
import DOMViewer.PopupFilterable;
import DOMViewer.PopupOptionable;

/*
 * for displaying a single files contents.
 * acts as a mapper to find the appropriate parser for each file
 * 
 * the generics supplied to [DOMView] are local [enum]s.
 */
public class DOMViewerTemplate extends DOMView<DOMViewer.Views.DOMViewerTemplate.popupOptions, DOMViewer.Views.DOMViewerTemplate.popupLimit> {
	
	public DOMViewerTemplate(Path root) {
		super(root);
	}
	
	/*
	 * when a node option is selected it gets sent through here
	 */
	@Override protected void nodeOptionsPopupMenu_actionEvent(popupOptions option, ActionEvent e) {
		var optionEnum = (popupOptions)option;
		switch(optionEnum) {
			case A_OPTION:
					nodeOptions_refresh();
				break;
			case ANOTHER_OPTION:	break;
			case PET_CAT:			break;
			case A_FOLDER_FOR_CATS: break; //this will never run because [A_FOLDER_FOR_CATS] is used as a folder;
			default:
				System.out.println("i=umimplimented popup menu event : " + optionEnum);
		}
		
	}
	
	/*
	 * this creates the tree from a given node. the given nodes in this case are the selected nodes of the tree
	 * 			- selected nodes are of the [TreePath] object
	 * 			- get selected nodes with ->  domTree.getSelectionPaths();
	 * 	- although it is not required. would suggest making a "refresh" option for the popupMenu, and linking it to this function
	 * 	- recommend using this line of code to get all the unique [TreePath]s' selected by the user
	 * 		ArrayList<TreePath> nodes = filterForUniqueRoots(List.of(domTree.getSelectionPaths()));
	 */
	@Override protected void nodeOptions_refresh() {
		// TODO Auto-generated method stub
		System.out.println("DOMView templete> rebuilding the tree");
	}
	
	/*
	 * gets called when the user selects a node to display. the view port is called of the [JScrollPane] class and called [DOMView.ev_sp]
	 */
	@Override protected void displayNode(DefaultMutableTreeNode dmtn) {
		// TODO Auto-generated method stub
		System.out.println("DOMView templete> displaying node:" + dmtn);
	}
	
	/*
	 * this is where the logic around the popupLimits limitations is implemented.
	 * Given a [EnumSet<popupLimits>] called [sharedLimits], and a [DMTN] called [node] to match against 
	 * 	 the function removes all invalid flags according to the node.
	 */
	@Override protected void filterPopupLimits(DefaultMutableTreeNode node, EnumSet<popupLimit> sharedLimits) {
		// TODO Auto-generated method stub
	}
	
//////////////////
// popupMenu settings
//////////////////
	
	enum popupOptions implements PopupOptionable {
		A_OPTION			("dispaly name"	, "description"),
		ANOTHER_OPTION		("dispaly name"	, "description"),
		A_FOLDER_FOR_CATS	("cat options"	, 
							"this is menu tab, it itself cannot be clicked but will dispaly its contents when hovered over"),
			PET_CAT			(" uuooohh cat"	, "a sub option of [A_FOLDER_FOR_CATS]",	
								A_FOLDER_FOR_CATS),
			SMACK_CAT			("no cat!"	, "a sub option of [A_FOLDER_FOR_CATS]. only appears if [popupLimit.BAD_CAT] evalues true for the node",	
								A_FOLDER_FOR_CATS,
								popupLimit.BAD_CAT),
		MULTICAT			("THERE IS AS YET INSUFFICIENT GREENIES FOR A MEANINGFUL ANSWER", "the constructor also lets u do this",	
							popupLimit.GOOD_CAT),
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
		SOME_OTHER_FLAG,
		BAD_CAT,
		GOOD_CAT
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
