package presentables.presents;

import javax.swing.ImageIcon;

import audinc.gui.MainWin;
import presentables.Presentable;

public class AutoClicker extends Presentable{
	public static String getDisplayTitle() 	{	return "auto clicker";	}
	public static String getDescription() 	{	return "automation of repetitive mouse actions<ul><li><li></ul>";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon(""); }
	@Override public void init() 			{}
	@Override protected void start() 		{}

	@Override
	protected void initGUI(MainWin mw) {}
}
