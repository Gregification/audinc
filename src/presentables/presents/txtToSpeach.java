package presentables.presents;

import presentables.Presentable;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import audinc.gui.MainWin;
import audinc.gui.WrapLayout;

/* JSAPI documentation: https://docs.oracle.com/cd/E17802_01/products/products/java-media/speech/forDevelopers/jsapi-doc/index.html
 * FreeTTS: https://freetts.sourceforge.io/#freetts_api
 */

public class txtToSpeach extends Presentable {
	//GUI
	private JSplitPane runTS, buildTS;
	private JTextArea runTS_rP_txtEditor_input;
	private JLabel noticeDisplay;
	private JTable importT_srcsTable;
	private JPanel importT;
	private DefaultTableModel importT_srcsTable_model;
	private JButton importT_toolBar_rowTools_deleteBtn;
	private ListSelectionModel importT_srcsTable_selectionModel;
	
	//main variables
	private HashSet<Path> speachModelSources = new HashSet<>();
	private String[] speachModelFileExtensions = new String[] {"bz2"};
	private final Map<String, String> saveMap = Map.of(
			"runTS_rP_txtEditor_input", 	"textEditorContent.txt",
			"speachModelSources", 			"speachModelSources.txt"
		);
	private int importSearchDepth = 5;
	
	@Override protected void init(MainWin mw) { initGUI(mw); }
	@Override public void quit() 				{
		try { save(); } catch (IOException e) { e.printStackTrace(); }
	}
	@Override protected void start() 			{
		try { load(); } catch (IOException e) { e.printStackTrace(); }
	}
	@Override protected void initGUI(MainWin mw){
		mw.setLayout(new BorderLayout());
		
		noticeDisplay = new JLabel();
			noticeDisplay.setPreferredSize(new Dimension(50, MainWin.stdtabIconSize.height));
			noticeDisplay.setBorder(BorderFactory.createLineBorder(null));
			setNoticeText("...");
		mw.add(noticeDisplay, BorderLayout.PAGE_END);
		
		JTabbedPane mainTp = new JTabbedPane();
		Border emptyBorder = BorderFactory.createEmptyBorder();
		
		//run tab
		JPanel runT = new JPanel(); runT	.setLayout(new BorderLayout());
			//run tab \ screen left 
			JPanel runTS_l = new JPanel(); runTS_l	.setLayout(new BorderLayout());
				JPanel runTS_lP_runSettings = new JPanel(); runTS_lP_runSettings.setLayout(new WrapLayout());
					//run tab \ screen left \ settings UI
				JButton runTS_lP_runSettings_startBtn = new JButton("START");
						runTS_lP_runSettings_startBtn.addActionListener(event -> onRunPanelStartClick());
				runTS_lP_runSettings.add(runTS_lP_runSettings_startBtn);
				JScrollPane runTS_lP_txtEditor_ScrollFrame = new JScrollPane(runTS_lP_runSettings,	
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					runTS_lP_txtEditor_ScrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/3), (int)MainWin.stdDimension.getHeight()));
					runTS_lP_txtEditor_ScrollFrame.setPreferredSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/3), (int)MainWin.stdDimension.getHeight()));
					runTS_lP_txtEditor_ScrollFrame.setAutoscrolls(true);
					
			runTS_l.add(runTS_lP_txtEditor_ScrollFrame, BorderLayout.CENTER);
			
			
			//run tab \ screen right
			JPanel runTS_rP = new JPanel();	runTS_rP	.setLayout(new BorderLayout());
				//run tab \ screen right \ text editor (c)
				JPanel runTS_rP_txtEditor = new JPanel();	runTS_rP_txtEditor	.setLayout(new BorderLayout());
					runTS_rP_txtEditor_input = new JTextArea("text here will be read");
					runTS_rP_txtEditor_input.setLineWrap(true);
					JScrollPane runTS_rP_txtEditor_ScrollFrame = new JScrollPane(runTS_rP_txtEditor_input,	
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						runTS_rP_txtEditor_ScrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/5), (int)MainWin.stdDimension.getHeight()));
						runTS_rP_txtEditor_ScrollFrame.setAutoscrolls(true);
				runTS_rP_txtEditor.add(runTS_rP_txtEditor_ScrollFrame, BorderLayout.CENTER);
				
			runTS_rP.add(runTS_rP_txtEditor, BorderLayout.CENTER);
		
			//combine run tabs
			runTS = new JSplitPane(SwingConstants.VERTICAL, runTS_l, runTS_rP);
			runT.add(runTS, BorderLayout.CENTER);
		
		mainTp.addTab("synth", MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize), runT, "select and use voice models");
		
		//build tab
		JPanel buildT = new JPanel(); buildT	.setLayout(new BorderLayout());
			//build tab \ screen left
			JPanel buildTS_l = new JPanel(); buildTS_l	.setLayout(new BorderLayout());
				buildTS_l.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/5), (int)MainWin.stdDimension.getHeight()));
			
			//build tab \ screen right
			JPanel buildTS_r = new JPanel();	buildTS_r	.setLayout(new BorderLayout());
				buildTS_r.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/5), (int)MainWin.stdDimension.getHeight()));
			
			//combine build tabs
			buildTS = new JSplitPane(SwingConstants.VERTICAL, buildTS_l, buildTS_r);
			buildT.add(buildTS, BorderLayout.CENTER);
		
		mainTp.addTab("build", MainWin.getImageIcon("res/build.png", MainWin.stdtabIconSize), buildT, "build models");
		
		//import tab
		importT = new JPanel(); importT	.setLayout(new BorderLayout());
			JToolBar importT_toolBar = new JToolBar("Controls");
				JButton importT_toolBar_importbtn = new JButton(MainWin.getImageIcon("res/file.png", MainWin.stdtabIconSize));
					importT_toolBar_importbtn.addActionListener(event -> onImportModelSourceClick(mw));
					importT_toolBar_importbtn.setBorder(emptyBorder);
					importT_toolBar_importbtn.setToolTipText("Import files or folders(search depth:" + importSearchDepth + ")");
				importT_toolBar.add(importT_toolBar_importbtn);
				JButton importT_toolBar_rowTools_deleteBtn = new JButton(MainWin.getImageIcon("res/trashCan.png", MainWin.stdtabIconSize));
					importT_toolBar_rowTools_deleteBtn.addActionListener(event -> onImportTableDeleteClick());
					importT_toolBar_rowTools_deleteBtn.setBorder(emptyBorder);
					importT_toolBar_rowTools_deleteBtn.setToolTipText("Delete selected rows");
				importT_toolBar.add(Box.createHorizontalGlue());
				importT_toolBar.add(importT_toolBar_rowTools_deleteBtn);
				
			importT.add(importT_toolBar, BorderLayout.PAGE_START);
			importT_srcsTable = new JTable() {
						private static final long serialVersionUID = 1L;//eclipse complains
						public boolean isCellEditable(int row, int column) { return false; }; //disables user editing of table
					};
				importT_srcsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				importT_srcsTable.setCellEditor(null);
				importT_srcsTable_model = (DefaultTableModel) importT_srcsTable.getModel();
					importT_srcsTable_model.addColumn("Path");
				importT_srcsTable_selectionModel = importT_srcsTable.getSelectionModel();
					importT_srcsTable_selectionModel.addListSelectionListener(new ListSelectionListener() {
							@Override public void valueChanged(ListSelectionEvent e) {
								onImportTableRowSelect(true);
							}
						});
				TableRowSorter<TableModel> importT_srcsTable_sorter = new TableRowSorter<>(importT_srcsTable_model);
					importT_srcsTable_sorter.setSortKeys(Arrays.asList(
								new RowSorter.SortKey(0, SortOrder.ASCENDING)
							));
				importT_srcsTable.setRowSorter(importT_srcsTable_sorter);
				JScrollPane importT_srcsTable_scrollFrame = new JScrollPane(importT_srcsTable,	
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					importT_srcsTable_scrollFrame.setAutoscrolls(true);
		importT.add(importT_srcsTable_scrollFrame, BorderLayout.CENTER);
		
		mainTp.addTab("import", MainWin.getImageIcon("res/import.png", MainWin.stdtabIconSize), importT, "import model paths");
		
		//tab(nocturnal crab) activities
		mainTp.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent e) {
				if(mainTp.getSelectedComponent().equals(importT)) {
					load_speachModelSources(false);
				}
			}
			
		});
		
		mw.add(mainTp, BorderLayout.CENTER);
	}

///////////////////
// gui events
///////////////////
	protected void onRunPanelStartClick() {
		this.setNoticeText("something malicious is brewing...");
	}
	protected void onImportTableDeleteClick() {
		Path p = Paths.get("" + importT_srcsTable.getValueAt(importT_srcsTable.getSelectedRow(),0));
		setNoticeText("removed model:" + p);
		speachModelSources.remove(p);
		importT_srcsTable_model.removeRow(importT_srcsTable.getSelectedRow());
	}
	protected void onImportTableRowSelect(boolean selecting) {
		
	}
	
	protected void onImportModelSourceClick(MainWin mw) {
		Path pPreferred = speachModelSources.stream().findAny().orElse(
				getRoot(this.getClass()).resolve(saveMap.values().stream().findAny().orElse("")));
		
		JFileChooser fc = new JFileChooser(pPreferred.toAbsolutePath().toString());
		FileNameExtensionFilter allowedFiles = new FileNameExtensionFilter("Supported models", speachModelFileExtensions);
		fc.addChoosableFileFilter(allowedFiles);
		fc.setFileFilter(allowedFiles);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		
		switch(fc.showOpenDialog(mw)) {
			case JFileChooser.APPROVE_OPTION : 
				add_speachModelSources(fc.getSelectedFile().toPath());
				break;
			case JFileChooser.CANCEL_OPTION : 
				System.out.println("cancled file chooser");
				break;
		}
	}
		
	protected void setNoticeText(String text) {
		this.noticeDisplay.setText(text);
	}
	protected void setNoticeText(String text, Color color) {
		this.noticeDisplay.setForeground(color);
		setNoticeText(text);
	}

///////////////////
//freeTTS
///////////////////
	

///////////////////
//save & load. for everything
///////////////////
	protected Path save() throws IOException {
		Path root = getRoot(this.getClass());
			Files.createDirectories(root);
			Path path = root.resolve(saveMap.get("runTS_rP_txtEditor_input"));
				Files.writeString(path, runTS_rP_txtEditor_input.getText(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			path = root.resolve(saveMap.get("speachModelSources"));
				writeToPath(path, bw ->{
						for(var v : speachModelSources) try { bw.write(v.toAbsolutePath().toString()); bw.newLine(); } catch (IOException e) {} 
					});
		return root;	
	}
	
	protected Path load() throws IOException {
		Path root = getRoot(this.getClass());
		if(Files.notExists(root)) return null;
			
		this.load_speachModelSources(true);
		
		Path path = root.resolve(saveMap.get("runTS_rP_txtEditor_input"));
			readFromPath(path, br -> { runTS_rP_txtEditor_input.setText(""); String line;
					try { while((line = br.readLine()) != null) runTS_rP_txtEditor_input.append(line); } catch (IOException e) { e.printStackTrace(); }
				});
		
		return root;	
	}
	
	protected boolean load_speachModelSources(boolean force) {
		Path path = getRoot(this.getClass()).resolve(saveMap.get("speachModelSources"));
		if(Files.notExists(path)) return false;
		
		if(!force) { 
			try { BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				if(attr.lastAccessTime().compareTo(attr.lastModifiedTime()) == 1) return false;
			} catch (IOException e) { e.printStackTrace(); }
		}
		speachModelSources.clear();
		
		readFromPath(path, br -> {
				try { String line;
					while((line = br.readLine()) != null)
						add_speachModelSources(Paths.get(line));
				} catch (IOException e) { e.printStackTrace(); }
			});
		return true;
	}
	
///////////////////
//threads
///////////////////
	protected synchronized void add_speachModelSources(Path p) {
		if(Files.notExists(p)) {
			setNoticeText("model path DNE:" + p, Color.red);
			return;
		}
		if(this.speachModelSources.contains(p)) {
			setNoticeText("model path duplicate:" + p, Color.yellow);
			return;
		}
		
		importT.setEnabled(false);
		setNoticeText("Prepping to start imports...", Color.black);
		
		Set<Path> synset = Collections.synchronizedSet(speachModelSources);
		Thread t = new Thread(() -> {
				int ntotal = lazyPathRecursion(p, importSearchDepth, np -> { //every "np" is a file, not a directory
					if(synset.contains(np)) return;
					String comp = np.toAbsolutePath().toString();
					for(var v : this.speachModelFileExtensions) {
						if(comp.endsWith(v)) {
							setNoticeText("model now included:" + p, Color.green);
							synset.add(np);
							this.importT_srcsTable_model.addRow(new Object[]{np});
							return;
						}
					}
					setNoticeText("invalid model path:" + comp, Color.red);
				});
				setNoticeText("Imported " + ntotal + " path(s)" , Color.black);
			});
		t.start();
		
		try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
		
		importT.setEnabled(true);
	}
	
///////////////////
//static presentables
///////////////////
	public static String getDisplayTitle() 	{ 	return "txt -> speach";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/tts.png"); }
	public static String getDescription() 	{	return "<html><body>"
		+ "voice synthesis using FreeTTS (1.2.2) and recordings with VestFox and Festival." 
		+ "<ul><li>MBROLA files not supported.</li></ul>"
		+ "</body></html>";	}
}
