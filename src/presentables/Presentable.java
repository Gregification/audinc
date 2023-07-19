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
		
//		jf.removeAll();
//		initGUIMenuBar(jf, mw);
		initGUI(jf);
		
		start();
	}
	
	public void quit(){};
	
	protected abstract void start();
	
	/*
	 * all gui that isnt the menu bar
	 */
	protected abstract void initGUI(JFrame jf);
	
//	/*
//	 * override for control over the menu bar
//	 */
//	protected void initGUIMenuBar(JFrame jf, MainWin mw) {
//		jf.setJMenuBar(mw.initGUIMenuBar());
//	}
}
