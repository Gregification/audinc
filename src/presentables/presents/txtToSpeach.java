package presentables.presents;

import presentables.Presentable;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import audinc.gui.MainWin;

public class txtToSpeach extends Presentable {
	@Override public void init() 				{}
	@Override public void quit() 				{}
	@Override protected void start() 			{}
	@Override protected void initGUI(MainWin mw){}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "txt -> speach";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/tts.png"); }
	public static String getDescription() 	{	return "<html><body>"
	+ "Text to vioce generation with the Kalid project." 
	+ "<br>work in progress..."
	+ "</body></html>";	}
}
