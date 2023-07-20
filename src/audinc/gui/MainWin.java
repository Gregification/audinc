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
import java.util.Map;

import presentables.Presentable;
import presentables.presents.*;

public class MainWin extends JFrame {
	public static String mainTitle = "Audinc";
	public static int standardTextSpace = 40;
	public static Dimension stdDimension = new Dimension(480,270);
	public static Map<String, Class<? extends Presentable>> Presents = Map.of(
			"Menu", 		presentables.presents.menu.class,
			"txt -> voice", presentables.presents.txtToVoice.class,
			"Borderlayout visualization",	presentables.presents.BorderLayoutDemo.class
		);
	
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
		setPresent("Menu");
		
		setVisible(true);
	}
	
	private void initGUIMenuBar() {
		JMenuBar menubar = new JMenuBar();
		
		JMenu help 	= new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(event -> onAboutClick());
		JMenuItem quit = new JMenuItem("Quit");
		quit.addActionListener(event -> quit());
		
		help.add(about);
		help.add(quit);
		
		JMenu presents = new JMenu("Presents");
		for(var k : Presents.keySet()) {
			JMenuItem jmi = new JMenuItem(k);
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
	public void setPresent(String key){
		try {
			setPresent(MainWin.Presents.get(key));
		}catch(Exception E) {
			throw new java.lang.Error(E);
			/* expected errors to catch
			 * - invalid key
			 * - key is valid but the class dosn't have a matching constructor
			 */
		}
	}
	public void setPresent(Class<? extends Presentable> cp) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if(currPresent != null) currPresent.quit();
				
		Presentable p = cp.getDeclaredConstructor().newInstance(); //throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException
		p.init();
		p.present(this);		
		
		setTitle(mainTitle + " : " + Presentable.tryForStatic(p.getClass(), "getDisplayTitle"));
	}
	
///////////////////
//button events
///////////////////
	protected void onAboutClick() {
		JLabel bg = null;
		try{
			BufferedImage bgImg = ImageIO.read(new File("res/aboutbg.png"));
            bg = new JLabel(new ImageIcon(bgImg));
        }catch(IOException e ){
        }

        JLabel title = new JLabel("<html><body>"
        +"<h1 align='center'><font size=+1><u>Audinc</u></font></h1>"
        +"<p align='center'><br>v</p>"
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