package presentables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Collections;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipException;
//import java.util.zip.ZipFile;
//import java.util.zip.ZipInputStream;
//import java.util.zip.ZipOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import audinc.gui.MainWin;

/**
 * {@summary boarder layout}
 * {@value displayTitle parameter should be set to something unique.}
 */

public abstract class Presentable {
	public void quit()	{};
	public void present(MainWin mw) {
		mw.getContentPane().removeAll();
		init(mw);
		start();
		mw.validate();
	}
	
	protected abstract void init(MainWin mw);
	protected abstract void start();
	protected abstract void initGUI(MainWin mw);
	
	public static String getDescription() 	{ return "no description avaliable";	}
	public static String getDisplayTitle() 	{ return "default display title";		}
	public static Path getRoot(Class<? extends Presentable> clas)		{ return MainWin.settingsDir.resolve(clas.getName()); };
	public static ImageIcon getImgIcon() {
		try { return new ImageIcon( ImageIO.read(new File("res/presentIcons/default.png"))); } 
		catch (IOException e) { e.printStackTrace(); return null; }
	}
	/*
	 * attempts to get the relevant static method from the given Presentable child. ex: getDescription
	 * - made as a work-around for overriding static methods
	 */
	public static Object tryForStatic(Class<? extends Presentable> clas, String methodName) {
		try { 
//			System.out.println("presentable tryforstatic:\t" + methodName + "\n\t" + clas.toString());
			Method m = clas.getMethod(methodName);
			return m.invoke(null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}		
		return null;
	}
	public static ImageIcon getImageIcon(String prefereed) {
		try {
			ImageIcon ii = new ImageIcon( ImageIO.read(new File(prefereed)));
			if(ii != null) return ii;
		} catch (IOException e) {}
		
		try {
			return new ImageIcon( ImageIO.read(new File("res/presentIcons/default.png")));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
		
	/*
	 * protected static ZipOutputStream getZOStream(Class<? extends Presentable>
	 * clas) { ZipOutputStream out = null; try { out = new ZipOutputStream( new
	 * FileOutputStream( new File(settingsDir.getAbsolutePath() + "\\" +
	 * clas.toString()))); } catch (FileNotFoundException e) { e.printStackTrace();
	 * }
	 * 
	 * return out; }
	 * 
	 * protected static ZipEntry getZEntry(ZipFile zos, Class<? extends Presentable>
	 * cp, String name) { name = cp.getName() + (name == null ? "":("\\" + name));
	 * 
	 * for (ZipEntry e : Collections.list(zos.entries())) if
	 * (e.getName().endsWith(name)) return e;
	 * 
	 * ZipEntry r = new ZipEntry(name);
	 * 
	 * return r; }
	 */
	
///////////////////
//i know better but am too lazy to correct
///////////////////
	protected void writeToPath(Path path, custom_bufferedWriter lambda) {
		try(BufferedWriter br = Files.newBufferedWriter(path)){
			lambda.doTheThing(br);
		}catch (IOException e) { e.printStackTrace(); }
	}
	protected void readFromPath(Path path, custom_bufferedReader lambda) {
		if(Files.exists(path))
			try(BufferedReader br = Files.newBufferedReader(path)){
				lambda.doTheThing(br);
			} catch (IOException e) { e.printStackTrace(); }
	}
	protected int lazyPathRecursion(Path path, int maxDepth, custom_lasyDirRecursion lambda) {
		if(maxDepth < 0) return 0;
		if(Files.exists(path)) {
			if(Files.isDirectory(path)) {
					try { Files.list(path).forEach(p -> this.lazyPathRecursion(p, maxDepth - 1, lambda)); } catch (IOException e) { e.printStackTrace(); };
			}else if(Files.isRegularFile(path)){
				lambda.doTheThing(path);
				return 1;
			}
		}
		return 0;
	}
	protected <T> boolean nullCoalescing(T o, custom_doTheThingIfNotNull<T> lamda){
		if(o == null) return false;
		lamda.doTheThing(o);
		return true;
	}
}