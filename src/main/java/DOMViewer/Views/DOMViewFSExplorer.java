package DOMViewer.Views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import DOMViewer.DOMParser;
import DOMViewer.DOMView;
import DOMViewer.DOModel;
import DOMViewer.FileViewer;
import DOMViewer.PopupFilterable;
import DOMViewer.PopupOptionable;
import DOMViewer.nodeObjects.DFolderNodeObj;
import DOMViewer.parsers.parserVariation;
import audinc.gui.MainWin;
import presentables.Presentable;

/*
 * for displaying the file system
 */
/** 
 */
public class DOMViewFSExplorer extends DOMView<DOMViewer.Views.DOMViewFSExplorer.popupOptions, DOMViewer.Views.DOMViewFSExplorer.popupLimit> {
	protected FileViewer viewer;
	
	public DOMViewFSExplorer() { this(MainWin.settingsDir); }
	public DOMViewFSExplorer(Path root) {
		super(root);
		this.viewer = new FileViewer(null);
		
		this.eleView.setLayout(new GridBagLayout());
		var c = new GridBagConstraints();
		c.weightx = c.weighty = 1.0;	
		c.fill = GridBagConstraints.BOTH;
		this.eleView.add(viewer, c);
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
					var sb = new StringBuilder("----------------" + treenode + '\n');
					List.of(src.listFiles()).stream()
						.sorted((o1, o2) -> Boolean.compare(o1.isDirectory(), o2.isDirectory()))	//priority = files
						.parallel()
						.forEach(subFile -> {	//parse folder contents back onto the queue
							var subPath 	= subFile.toPath();
							var subObject 	= new DFolderNodeObj(subPath.getFileName().toString(), subPath);
							
							var subNode 	= new DefaultMutableTreeNode(subObject, subFile.isDirectory());
							treenode.add(subNode);
							
							//create new job
							executor.execute(parse_recursive1(subNode, executor));
							
							sb.append(subPath.toString());
							sb.append('\n');
						});
					System.out.println(sb.toString());
					sb.setLength(0);
				}else {	
					//update to leaf, parse the file
					
				}
				
//				System.out.println("DOMViewFolder: "  + treenode);
				updateTreeViewForNode(treenode);
			}
		};
	}

	@Override protected void displayNode(DefaultMutableTreeNode dmtn) {
		// TODO Auto-generated method stub
		System.out.println("displaying node: " + dmtn.toString());
		
		var node = (DFolderNodeObj)dmtn.getUserObject();
		
		this.viewer.setParser(node.getParser());
	}
	
	@Override protected void nodeOptions_refresh() {
		var nodes = filterForUniqueRoots(List.of(domTree.getSelectionPaths()));//make mutable list
		
		//re-traverse nodes. using executor to handle work loading.
		executor = Executors.newCachedThreadPool();//(we want jobs)
		
		nodes.stream()	//init jobs. a small initial investment(1 bajillion baboons)
			.map(e -> ((DefaultMutableTreeNode)e.getLastPathComponent()))
			.forEach(e -> {
					e.removeAllChildren();
					executor.execute(parse_recursive1(e, executor));	//42
				});
	}
	
	protected void nodeOptions_clear() {
		filterForUniqueRoots(List.of(domTree.getSelectionPaths())).stream()
			.map(e -> (DefaultMutableTreeNode)e.getLastPathComponent())
			.forEach(e -> domTree_model.removeNodeFromParent(e));
	}
	
	protected void nodeOptions_parseCustom() {
		//good luck
		filterForUniqueRoots(List.of(domTree.getSelectionPaths())).stream()
			.map(e -> (DefaultMutableTreeNode)e.getLastPathComponent())
			.map(e -> (DFolderNodeObj)e.getUserObject())
			.filter(e -> e.getParser() == null)
			.forEach(this::openCustomParseDialog);
	}
	protected void openCustomParseDialog(DFolderNodeObj dfnno) { //everything about how a parser is created is a trust exercise
		File file = dfnno.getPath().toFile();
		
		JPanel content = new JPanel(new GridBagLayout());
		var c = new GridBagConstraints();
		
		List<DOModel> models = List.of(DOModel.getApplicableModels(file).toArray(DOModel[]::new));
		String[][] modelRows = models.stream()
				.map(e -> new String[] {e.toString()})
				.toArray(String[][]::new);
		JTable modelTable = new JTable(modelRows, new String[] {"Avaliable Models (" + models.size() +")"}) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int row, int column) {                
	                return false;               
				};
			};
			modelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			modelTable.setCellEditor(null);
			
//		var variationType = DOMParser.class.getGenericInterfaces()[0].getClass();
		
		JTable variTable = new JTable() {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int row, int column) {                
	                return false;               
				};
			};
			var variModel = (DefaultTableModel)(variTable.getModel());
			variModel.addColumn("Model Variations");
			variTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			variTable.setCellEditor(null);
			
		modelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	DOModel selectedModel = models.get(modelTable.getSelectedRow());
	        	
	        	var variModel = (DefaultTableModel) variTable.getModel();
	        	variModel.setRowCount(0);					//clear table
	        	
	        	for(var v : selectedModel.getVariEnum()) {	//re-populate table
	        		variModel.addRow(new Object[] {v});
	        	}
	        	variTable.setRowSelectionInterval(0, 0);
	        }
	    });
		
		modelTable.setRowSelectionInterval(0, 0);
		
		content.add(new JScrollPane(modelTable));
		content.add(new JScrollPane(variTable));
		
		int result = JOptionPane.showConfirmDialog(null, content, 
	    		"Parser selector", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
//	    	this.viewer.setParser();
			DOModel model 	= models.get(modelTable.getSelectedRow());
			Object vari 	= variTable.getValueAt(variTable.getSelectedRow(), 0);
			
			System.out.println("mode:\t" + model + "\nvari:\t" + vari);
			
			try {
				var parser = model.getParser().getConstructor(File.class).newInstance(file);
				parser.setVariation(vari);
				
				dfnno = new DFolderNodeObj(dfnno.toString(), dfnno.getPath(), parser);
				
				this.viewer.setParser(parser);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    } 
		
	}
	protected void nodeOptions_parseChildren() {
		
	}
	
	@Override protected void nodeOptionsPopupMenu_actionEvent(popupOptions option, ActionEvent e) {
		assert option instanceof popupOptions : "whar??";
		
		var optionEnum = (popupOptions)option;
		switch(optionEnum) {
			case CLEAR : 			nodeOptions_clear(); 		break; 
			case REFRESH :			nodeOptions_refresh(); 		break;
			case PARSE_CUSTOM :		nodeOptions_parseCustom();	break;
			case PARSE_CHILDREN :	nodeOptions_parseChildren();break;
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