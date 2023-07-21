package audinc.gui;

import javax.swing.JFrame;           // for main window
import javax.swing.JOptionPane;      // for standard dialogs
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JMenuBar;         // row of menu selections
import javax.swing.JMenu;            // menu selection that offers another menu
import javax.swing.JMenuItem;        // menu selection that does something
import javax.swing.JToolBar;         // row of buttons under the menu
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;    // 2-state button
import javax.swing.BorderFactory;    // manufacturers Border objects around buttons
import javax.swing.Box;              // to create toolbar spacer
import javax.swing.UIManager;        // to access default icons
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JLabel;           // text or image holder
import javax.swing.ImageIcon;        // holds a custom icon
import javax.swing.SwingConstants;   // useful values for Swing method calls
import javax.swing.SpinnerModel;

import java.net.URL;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import presentables.Presentable;
import presentables.presents.*;

public class MainWin extends JFrame {
	public static String mainTitle = "Audinc";
	public static int standardTextSpace = 40;
	public static Dimension stdDimension = new Dimension(480,270);
	public static Set<Class<? extends Presentable>> Presents = Stream.of(
			presentables.presents.menu.class,
			presentables.presents.txtToSpeach.class,
			presentables.presents.BorderLayoutDemo.class,
			presentables.presents.AutoClicker.class
		).collect(Collectors.collectingAndThen(Collectors.toSet(),Collections::<Class<? extends Presentable>>unmodifiableSet));
	
	public Presentable currPresent = null;
	
//	private File settingsFile = new File("settings.audinc");
	
	public MainWin(String title) {
		super(title);
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
		
		help.add(mainMenu);
		help.add(about);
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
	
///////////////////
//events
///////////////////
	public void setPresent(Class<? extends Presentable> cp) {
		if(currPresent != null) currPresent.quit();
		
		try {
			Presentable p = cp.getDeclaredConstructor().newInstance(); //throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException
			p.init();
			p.present(this);		
			setTitle(mainTitle + " : " + Presentable.tryForStatic(p.getClass(), "getDisplayTitle"));
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
		if(currPresent != null) currPresent.quit();
		dispose();
	}
}