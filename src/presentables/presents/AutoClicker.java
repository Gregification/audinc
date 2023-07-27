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
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/mouse.png"); }
	public static String getDescription() 	{	return "<html>"
			+ "recreation of <a href=\"https://sourceforge.net/projects/orphamielautoclicker/\"><b>the autoclicker<b></a> created by <a href=\"https://sourceforge.net/u/mousetool/profile/\" title=\"https://sourceforge.net/u/mousetool/profile/\"><b>Mousetool<b></a>"
			+ "<br>automation of repetitive mouse actions"
			+ "<ul><li>click</li><li>click</li><li>click</li></ul></html>";	}
}
