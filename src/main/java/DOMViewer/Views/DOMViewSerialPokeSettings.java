package DOMViewer.Views;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;

import javax.swing.tree.DefaultMutableTreeNode;

import DOMViewer.DOMView;
import DOMViewer.PopupFilterable;
import DOMViewer.PopupOptionable;

public class DOMViewSerialPokeSettings extends DOMView<DOMViewer.Views.DOMViewSerialPokeSettings.popupOptions, DOMViewer.Views.DOMViewSerialPokeSettings.popupLimit>{
	
	public DOMViewSerialPokeSettings(Path root) {
		super(root);
	}

	@Override protected void nodeOptionsPopupMenu_actionEvent(popupOptions option, ActionEvent e) {
		switch(option) {
			case RELOAD:
				this.displayNode((DefaultMutableTreeNode)domTree.getLastSelectedPathComponent());
				break;
			case APPLY_CHANGES:
				break;
		}
		
	}

	@Override
	protected void nodeOptions_refresh() {
		
	}

	@Override
	protected void displayNode(DefaultMutableTreeNode dmtn) {
		// TODO Auto-generated method stub
		System.out.println("DOMViewSerialPokeSettings>displaying node: " + dmtn.toString());
	}

	@Override
	protected void filterPopupLimits(DefaultMutableTreeNode node, EnumSet<popupLimit> sharedLimits) {
		
	}
	
	
//////////////////////
//ENUMS
//////////////////////
	
	enum popupOptions implements PopupOptionable {
		RELOAD			("reload",
						"resets changes"),
		APPLY_CHANGES	("apply changes",
						"update settings to what ever is shown",
						popupLimit.ON_SETTING),
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
		ON_ALL,
		ON_SETTING
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