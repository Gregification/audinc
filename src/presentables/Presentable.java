package presentables;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

import audinc.gui.MainWin;

/**
 * {@summary boarder layout}
 * {@value displayTitle parameter should be set to something unique.}
 */

public abstract class Presentable {	
	/*
	 * set the display title here and what ever else need to run before the GUI
	 */
	public abstract void init();
	
	public void present(MainWin mw) {
		JFrame jf = mw;
		
		jf.getContentPane().removeAll();
		init();
		initGUI(jf);
		jf.validate();
		
		start();
	}
	
	public void quit(){};
	
	protected abstract void start();
	
	protected abstract void initGUI(JFrame jf);
	
	public static String getDescription() {
		return "no description avaliable";
	}
	public static String getDisplayTitle() {
		return "default display title";
	}
	
	public static ImageIcon getImgIcon() {
		try {
			return new ImageIcon( ImageIO.read(new File("res/presentIcons/default.png")));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * attempts to get the relevant static method from the given Presentable child. ex: getDescription
	 * - made as a work-around for overriding static methods
	 */
	public static Object tryForStatic(Class<? extends Presentable> clas, String methodName) {
		try { 
			Method m = clas.getMethod(methodName);
			return m.invoke(null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}		
		return null;
	}
}
