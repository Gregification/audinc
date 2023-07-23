package audinc.gui;

import javax.swing.JFrame;           // for main window
import javax.swing.JOptionPane;      // for standard dialogs
import javax.swing.JMenuBar;         // row of menu selections
import javax.swing.JMenu;            // menu selection that offers another menu
import javax.swing.JMenuItem;        // menu selection that does something
import javax.swing.JLabel;           // text or image holder
import javax.swing.ImageIcon;        // holds a custom icon
import javax.swing.SwingConstants;   // useful values for Swing method calls

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import presentables.Presentable;

public class MainWin extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String mainTitle = "Audinc";
	public static Path settingsDir = Paths.get("G:\\junk\\audincSettings"); //test with: "G:\\junk\\audincSettings" ; build with ".\\audincSettings"
	
	public static Set<Class<? extends Presentable>> Presents = Stream.of(
			presentables.presents.menu.class,
			presentables.presents.txtToSpeach.class,
			presentables.presents.BorderLayoutDemo.class,
			presentables.presents.AutoClicker.class
		).collect(Collectors.collectingAndThen(Collectors.toSet(),Collections::<Class<? extends Presentable>>unmodifiableSet));
	
	//standard values
	public static int stdTextSpace = 40,
			stdStructSpace = 15,
			stdPreferredNumThreads = 3;
	public static float stdDimensionScale = 1.75f;
	public static Dimension stdDimension = new Dimension((int)(480*stdDimensionScale),(int)(270*stdDimensionScale)),
			stdtabIconSize = new Dimension((int)(11*stdDimensionScale),(int)(11*stdDimensionScale));
	
	public Presentable currPresent = null;
	
//	private File settingsFile = new File("settings.audinc");
	
	public MainWin(String title) {
		super(title);
		
		//create save
		settingsDir = settingsDir.toAbsolutePath();
		if(!Files.exists(settingsDir))
			try { Files.createDirectory(settingsDir); } catch (IOException e) { e.printStackTrace(); }
		
		
		//GUI
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,200);
        this.setMinimumSize(stdDimension);
        
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
//        pack();
        setResizable(true);
        
        initGUIMenuBar();
		
		//starting present
		setPresent(presentables.presents.menu.class);
		
		setVisible(true);
	}
	
	private void initGUIMenuBar() {
		JMenuBar menubar = new JMenuBar();
		
		JMenu help 	= new JMenu("Help");
			JMenuItem mainMenu = new JMenuItem("main menu");
				mainMenu.addActionListener(event -> setPresent(presentables.presents.menu.class));
			JMenuItem about = new JMenuItem("About");
				about.addActionListener(event -> onAboutClick());
			JMenuItem quit = new JMenuItem("Quit");
				quit.addActionListener(event -> quit());
			JMenuItem restartPresent = new JMenuItem("restart present");
				restartPresent.addActionListener(event -> {if(currPresent == null) return; this.currPresent.quit(); this.setPresent(currPresent.getClass());});
		
		help.add(mainMenu);
		help.add(about);
		help.addSeparator();
		help.add(restartPresent);
		help.add(quit);
		
		JMenu presents = new JMenu("Presents");
		for(var k : Presents) {
			JMenuItem jmi = new JMenuItem((String)Presentable.tryForStatic(k, "getDisplayTitle"));
			jmi.addActionListener(event -> setPresent(k));
			presents.add(jmi);
		}
		
		menubar.add(help);
		menubar.add(presents);
		
		setJMenuBar(menubar);
	}

	public void setPresent(Class<? extends Presentable> cp) {
		if(currPresent != null) currPresent.quit();
		
		try {
			Presentable p = cp.getDeclaredConstructor().newInstance(); //throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException
			p.present(this);		
			setTitle(mainTitle + " : " + Presentable.tryForStatic(p.getClass(), "getDisplayTitle"));
			currPresent = p; 
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
	}
	
///////////////////
//button events
///////////////////
	protected void onAboutClick() {
		JLabel bg = null;
		try{
			BufferedImage bgImg = ImageIO.read(new File("res/aboutbg.png"));
            bg = new JLabel(new ImageIcon(bgImg));
        }catch(IOException e ){ e.printStackTrace(); }

        JLabel title = new JLabel("<html><body>"
        +"<h1 align='center'><font size=+1><u>Audinc</u></font></h1>"
        +"<p align='center'><br>George Boone</p>"
        +"</body></html>",
        SwingConstants.CENTER);

        JLabel artists = new JLabel("<html>"
        +"<br/><p>work inspired by Professor George Rice of UT-Arlington</p>"
        +"<br/><p>GDC 3.0 lisence 2023</p>"
        +"</html>",
        SwingConstants.CENTER);

        JOptionPane.showMessageDialog(this,
            new Object[]{bg, title, artists},
            "Audinc (audi, not the car)",
            JOptionPane.PLAIN_MESSAGE);
	}
	
	public void quit() {
		if(currPresent != null) {
			currPresent.quit();
		}
		dispose();
	}

///////////////////
//GUI utils
///////////////////
	public static ImageIcon getImageIcon(String src) {
		try {
			return new ImageIcon( ImageIO.read(new File(src)));
		} catch (IOException e) {
			return null;
		}
	}
	public static ImageIcon getImageIcon(String src, Dimension size) { return getImageIcon(src, size, Image.SCALE_FAST); }
	public static ImageIcon getImageIcon(String src, Dimension size, int scaleMethod) {
		try {
			Image img = ImageIO.read(new File(src));
			Image imgScaled = img.getScaledInstance(size.width, size.height, scaleMethod);
			return new ImageIcon(imgScaled);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}