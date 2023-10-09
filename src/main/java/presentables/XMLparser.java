package presentables;

import javax.swing.ImageIcon;

import audinc.gui.MainWin;

public class XMLparser extends Presentable{
	
	@Override protected void start()	{
	}
	
	@Override protected void init(MainWin mw) 	{
		initGUI(mw);
	}	
	@Override protected void initGUI(MainWin mw){
		
	}
	
	@Override public void quit() {}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "XML parser";	}
	public static ImageIcon getImgIcon() 	{	return MainWin.getImageIcon("res/presentIcons/XMLicon.png", MainWin.stbPresentIconSize); } //giving a invalid name will default to a blank icon
	public static String getDescription() 	{	return "<html>"
			+ "<body> xml parser<br>"
			+ "if using JAR, see the src file \"src/main/java/xmlparser/XMLElement.java\"<a href='https://github.com/Gregification/audinc/tree/main'>(github)</a> for methods"
			+ "</body>"
			+ "</html>"; }
}
