package presentables.presents;

import java.awt.BorderLayout;
import java.io.BufferedReader;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import audinc.gui.MainWin;
import presentables.Presentable;

public class XMLpresent extends Presentable{
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
		
		JToolBar toolbar = new JToolBar("");
			JButton tb_startParse = new JButton(MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize));
				tb_startParse.addActionListener(e -> this.startParse());
				tb_startParse.setBorder(emptyBorder);
				tb_startParse.setToolTipText("start parse");
			
		toolbar.add(tb_startParse); 
				
		container.add(toolbar,BorderLayout.PAGE_START);
		
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
	public static String getDisplayTitle() 	{ 	return "xml parser";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/xmlparser.png"); } //giving a invalid name will default to a blank icon
	public static String getDescription() 	{	return "<html>"
			+ "<body>parses given xml into a document tree<br>"
			+ "</body>"; }
}
