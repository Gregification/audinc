package presentables;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import audinc.gui.MainWin;

/**
 * {@summary boarder layout}
 * {@value displayTitle parameter should be set to something unique.}
 */

public abstract class Presentable {
	public String dispalyTitle = "default display title";
	
	/*
	 * set the display title here and what ever else need to run before the GUI
	 */
	public abstract void init();
	
	public void present(MainWin mw) {
		JFrame jf = mw;
		
		initGUI(jf);
//		jf.update(null);
		
		start();
	}
	
	public void quit(){};
	
	protected abstract void start();
	
	protected abstract void initGUI(JFrame jf);
	
}
