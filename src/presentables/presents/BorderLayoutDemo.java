package presentables.presents;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import audinc.gui.MainWin;
import presentables.Presentable;

public class BorderLayoutDemo extends Presentable {
	@Override protected void init(MainWin mw) { initGUI(mw); }
	@Override protected void start() 		{}

	@Override protected void initGUI(MainWin mw) {
		JFrame jf = mw;
		jf.setLayout(new BorderLayout());
		
		JLabel lineStartLabel = new JLabel("LINE_START", JLabel.CENTER);
		 lineStartLabel.setOpaque(true);
		 lineStartLabel.setBackground(Color.RED);
		 jf.add(lineStartLabel, BorderLayout.LINE_START); 
		 
		 JLabel centerLabel = new JLabel("CENTER", JLabel.CENTER);
		 centerLabel.setOpaque(true);
		 centerLabel.setBackground(Color.YELLOW);
		 jf.add(centerLabel, BorderLayout.CENTER);
		 
		 JLabel lineEndLabel = new JLabel("LINE_END", JLabel.CENTER);
		 lineEndLabel.setOpaque(true);
		 lineEndLabel.setBackground(Color.RED);
		 jf.add(lineEndLabel, BorderLayout.LINE_END);
		 
		 JLabel pageEndLabel = new JLabel("PAGE_END", JLabel.CENTER);
		 pageEndLabel.setOpaque(true);
		 pageEndLabel.setBackground(Color.GREEN);
		 jf.add(pageEndLabel, BorderLayout.PAGE_END);
		 
		 JLabel pageStartLabel = new JLabel("PAGE_START", JLabel.CENTER);
		 pageStartLabel.setOpaque(true);
		 pageStartLabel.setBackground(Color.GREEN);
		 jf.add(pageStartLabel, BorderLayout.PAGE_START);
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{	return "BorderLayout demo";	}
	public static String getDescription() 	{	return "color coded visual of a java BorderLayout";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/BorderLayout.png"); }
}
