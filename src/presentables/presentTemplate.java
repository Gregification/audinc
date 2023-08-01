package presentables;

import javax.swing.ImageIcon;

import audinc.gui.MainWin;
import presentables.Presentable;

public class presentTemplate extends Presentable{
	/*
	 * when the present is selected to run, these methods will be called in order.
	 * 	1. init(MainWin mw)
	 *  2. start()
	 * note - there is no real difference between these methods. they are simply for organization.
	 *  	see the presentables.Presentable classes present() method for exact differences.
	 */
	
	@Override protected void start()	{
		//this runs after the init() method
	}
	
	@Override protected void init(MainWin mw) 	{
		//having you call initGUI() gives a bit more control
		initGUI(mw);
	}	
	@Override protected void initGUI(MainWin mw){
		//the MainWin class is a JFrame. add your GUI components to it; however, DO NOT change the JMenuBar
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "the name displayed on the menu";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/info.png"); } //giving a invalid name will default to a blank icon
	public static String getDescription() 	{	return "<html>"
			+ "<body> descripiton of this present.<br>"
			+ "<a href=\"cow.com\" alt=\"cow.com link\">links can be displayed</a>"
			+ "</body>"; }
}
