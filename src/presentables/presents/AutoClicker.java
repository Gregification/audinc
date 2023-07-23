package presentables.presents;

import javax.swing.ImageIcon;

import audinc.gui.MainWin;
import presentables.Presentable;

public class AutoClicker extends Presentable{
	@Override protected void init(MainWin mw) { initGUI(mw); }
	@Override protected void start() 		{}

	@Override
	protected void initGUI(MainWin mw) {}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{	return "auto clicker";	}
	public static String getDescription() 	{	return "<html>automation of repetitive mouse actions"
			+ "<ul><li>click</li><li>click</li><li>click</li></ul></html>";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon(""); }
}
