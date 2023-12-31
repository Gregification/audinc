package presentables.presents;

import java.awt.BorderLayout;
import java.nio.file.Path;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import DOMViewer.DOMView;
import DOMViewer.DOModel;
import DOMViewer.Views.DOMViewFSExplorer;
import audinc.gui.MainWin;
import presentables.Presentable;

public class DOMViewer extends Presentable{
	public DOMView domView 	= new DOMViewFSExplorer();
	public Path source 		= MainWin.settingsDir; 
	
	@Override protected void start()	{
		
	}
	
	@Override protected void init(MainWin mw) 	{	
		initGUI(mw);
	}	
	
	@Override public void quit() {
		domView.close();
	}
	
	public void openPath(Path path) {
		this.source = path;
		this.domView.setRoot(path);
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "DOM viewer";	}
	public static ImageIcon getImgIcon() 	{	return MainWin.getImageIcon("res/presentIcons/domviewer.png", MainWin.stbPresentIconSize); }
	public static String getDescription() 	{	return "<html>"
	+ "<body> dom viewer for file trees"
	+ "parse and view dom's the way you never knew! (idk)"
	+ "<ul>"
	+ "<li>XML</li>"
	+ "<li>json</li>"
	+ "<li>files</li>"
	+ "</ul>"
	+ "metadata support is only tested for windows, i havent bothered to debug/impliment the details for other file systems"
	+ "<br> - if you would like to see those features impliment them yourself (shouldnt be too hard, use the 'base attributes' as refrence)"
	+ "</body>"
	+ "</html>"; }

///////////////////
//gui
///////////////////
	private void onStartParseClick() {
		
	}
	
	private void onSelectFileClick() {
		JFileChooser fc = new JFileChooser(source.getParent().toAbsolutePath().toString());//this.preferredPath.toString());
		
		for(var v : DOModel.values()) {
			FileNameExtensionFilter allowedFiles = new FileNameExtensionFilter(v.name(), v.extensions().toArray(new String[] {}));
			fc.addChoosableFileFilter(allowedFiles);
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		
		switch(fc.showOpenDialog(null)) {
			case JFileChooser.APPROVE_OPTION : 
				this.source = fc.getSelectedFile().toPath();
				System.out.println(source);
				openPath(source);
				break;
			case JFileChooser.CANCEL_OPTION : 
				break;
		}
	}
	
	private void onClearSelectionClick() {
		
	}

///////////////////
//gui display
///////////////////
	@Override protected void initGUI(MainWin mw){
		mw.add(this.init_guiToolbar(), 	BorderLayout.LINE_START);
		mw.add(this.domView, 			BorderLayout.CENTER);
	}

	private JPanel init_guiToolbar() {
		JPanel container = new JPanel(new BorderLayout());
		
		var emptyBorder = BorderFactory.createEmptyBorder();
		
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
			JButton tb_startParse = new JButton(MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize));
				tb_startParse.addActionListener(e -> this.onStartParseClick());
				tb_startParse.setBorder(emptyBorder);
				tb_startParse.setToolTipText("start parse");
			JButton tb_selectfile = new JButton(MainWin.getImageIcon("res/file.png", MainWin.stdtabIconSize));
				tb_selectfile.addActionListener(e -> this.onSelectFileClick());
				tb_selectfile.setBorder(emptyBorder);
				tb_selectfile.setToolTipText("open folder/file");
			JButton tb_clearSelection = new JButton(MainWin.getImageIcon("res/clear.png", MainWin.stdtabIconSize));
				tb_clearSelection.addActionListener(e -> this.onClearSelectionClick());
				tb_clearSelection.setBorder(emptyBorder);
				tb_clearSelection.setToolTipText("clear display");
			JButton tb_viewInfo = new JButton(MainWin.getImageIcon("res/info.png", MainWin.stdtabIconSize));
				tb_viewInfo.addActionListener(e -> this.ShowPresentInfo());
				tb_viewInfo.setBorder(emptyBorder);
				tb_viewInfo.setToolTipText("present info");
			
		toolbar.add(tb_selectfile);
		toolbar.add(Box.createVerticalStrut(MainWin.stdStructSpace / 2));
		//toolbar.add(tb_startParse);
		toolbar.add(Box.createVerticalGlue());
		toolbar.add(tb_viewInfo);
//		toolbar.add(Box.createVerticalStrut(MainWin.stdStructSpace / 3));
//		toolbar.add(tb_clearSelection);
		
		container.add(toolbar);
		
		return container;
	}

	public void ShowPresentInfo() {
		JPanel container = new JPanel();
		
	}
}
