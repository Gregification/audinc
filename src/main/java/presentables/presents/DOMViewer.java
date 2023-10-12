package presentables.presents;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import audinc.gui.MainWin;
import presentables.Presentable;

public class DOMViewer extends Presentable{
	private Thread parser_thread;
	
	@Override protected void start()	{
	}
	
	@Override protected void init(MainWin mw) 	{
		initGUI(mw);
	}	
	
	@Override public void quit() {}
	

	@Override protected void initGUI(MainWin mw){
		JPanel container = new JPanel(new BorderLayout());
		
		var emptyBorder = BorderFactory.createEmptyBorder();
		
		JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
			JButton tb_startParse = new JButton(MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize));
				tb_startParse.addActionListener(e -> this.startParse());
				tb_startParse.setBorder(emptyBorder);
				tb_startParse.setToolTipText("start parse");
			JButton tb_selectfile = new JButton(MainWin.getImageIcon("res/file.png", MainWin.stdtabIconSize));
				tb_selectfile.addActionListener(e -> this.startParse());
				tb_selectfile.setBorder(emptyBorder);
				tb_selectfile.setToolTipText("open folder/file");
			JButton tb_removeSelection = new JButton(MainWin.getImageIcon("res/trashCan.png", MainWin.stdtabIconSize));
			tb_removeSelection.addActionListener(e -> this.startParse());
				tb_removeSelection.setBorder(emptyBorder);
				tb_removeSelection.setToolTipText("open folder/file");
			
		toolbar.add(tb_selectfile);
		toolbar.add(Box.createVerticalStrut(MainWin.stdStructSpace / 2));
		toolbar.add(tb_startParse);
		toolbar.add(Box.createVerticalGlue());
		toolbar.add(tb_removeSelection);
				
		container.add(toolbar,BorderLayout.LINE_START);
		
		mw.add(container);
	}
	
	protected void openFileChooserDialog() {
		JPanel container = new JPanel();
		
	}
	
	public void startParse() {
		
	}
	
	public void parser_thread(String s) {
		
	}
	
	public void parser_thread(BufferedReader br) {
		
	}
///////////////////
//Presentable statics
///////////////////
///////////////////
//Presentable statics
///////////////////
public static String getDisplayTitle() 	{ 	return "DOM viewer";	}
public static ImageIcon getImgIcon() 	{	return MainWin.getImageIcon("res/presentIcons/domviewer.png", MainWin.stbPresentIconSize); }
public static String getDescription() 	{	return "<html>"
+ "<body> dom viewer<br>"
+ "parse and view dom's the way you never knew! (idk)"
+ "<ul>"
+ "<li>XML</li>"
+ "<li>json</li>"
+ "<li>files</li>"
+ "</ul>"
+ "</body>"
+ "</html>"; }
}
