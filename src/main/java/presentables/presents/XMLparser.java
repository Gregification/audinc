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

public class XMLparser extends Presentable{
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
public static String getDisplayTitle() 	{ 	return "XML parser";	}
public static ImageIcon getImgIcon() 	{	return MainWin.getImageIcon("res/presentIcons/xmlparser.png", new Dimension((int)(MainWin.stbPresentIconSize.width * 1.5),(int)(MainWin.stbPresentIconSize.height * 1.0))); }
public static String getDescription() 	{	return "<html>"
+ "<body> xml parser<br>"
+ "parse and view XML the way you never knew! (idk)"
+ "<br>- if using JAR, see the src file \"src/main/java/xmlparser/XMLElement.java\"<a href='https://github.com/Gregification/audinc/tree/main'>(github)</a> for methods"
+ "</body>"
+ "</html>"; }
}
