package audinc.gui;

import javax.swing.JFrame;           // for main window
import javax.swing.JOptionPane;      // for standard dialogs
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JMenuBar;         // row of menu selections
import javax.swing.JMenu;            // menu selection that offers another menu
import javax.swing.JMenuItem;        // menu selection that does something
import javax.swing.JLabel;           // text or image holder
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;        // holds a custom icon
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;   // useful values for Swing method calls
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Random;

import presentables.Presentable;
import presentables.custom_function;

public class MainWin extends JFrame {
	/**
	 * 
	 */
	
	public static Path settingsDir = Paths.get(".\\audincSettings"); // build with ".\\audincSettings"
	
	//accessible presents go here. order does not matter
	public static Set<Class<? extends Presentable>> Presents = Stream.of(
			presentables.presents.menu.class,
			presentables.presents.txtToSpeach.class,
			presentables.presents.BorderLayoutDemo.class,
			presentables.presents.AutoClicker.class,
			presentables.presents.SerialPoke.class,
			presentables.presents.IE3301.class,
			presentables.presents.Awake.class,
			presentables.presents.XMLpresent.class
		).collect(Collectors.collectingAndThen(Collectors.toSet(),Collections::<Class<? extends Presentable>>unmodifiableSet));
	
	//standard values
	public static final float stdDimensionScale_default = 1.75f;
	public static float 
		stdDimensionScale 		= stdDimensionScale_default,
		DimensionScale_window	= 1,
		DimensionScale_icon		= 1,
		DimensionScale_present	= 1;
	public static int 
			stdTextSpace 			= 40,	//standard spacing unit between gui components 
			stdStructSpace 			= 15,
			stdPreferredNumThreads 	= 2;
	public static Dimension 
			stdDimension 		= new Dimension((int)(480*stdDimensionScale),	(int)(270*stdDimensionScale)),
			stdtabIconSize 		= new Dimension((int)(11*stdDimensionScale),	(int)(11*stdDimensionScale)),
			stbPresentIconSize 	= new Dimension((int)(32*stdDimensionScale), 	(int)(30*stdDimensionScale));
	
	public void setPresent(Class<? extends Presentable> cp) {
		if(currPresent != null) currPresent.quit();
		
		try {
			Presentable p = cp.getDeclaredConstructor().newInstance(); //throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException
			p.present(this);		
			setTitle(mainTitle + " : " + Presentable.tryForStatic(p.getClass(), "getDisplayTitle"));
			currPresent = p; 
		} catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
	}
	
	public static void setUIScale(float scale) {
		MainWin.stdDimensionScale = scale;
			MainWin.setUIWindowScale(DimensionScale_window);
			MainWin.setUIIconScale(DimensionScale_icon);
			MainWin.setUIPresentScale(DimensionScale_present);
	}
	public static void setUIWindowScale(float scale) {
		DimensionScale_window = scale; 
		scale = stdDimensionScale * DimensionScale_window;
			stdDimension 		= new Dimension((int)(480*scale),	(int)(270*scale));
	}
	public static void setUIIconScale(float scale) {
		DimensionScale_icon = scale;
		scale = stdDimensionScale * DimensionScale_icon;
			stdtabIconSize 		= new Dimension((int)(11*scale),	(int)(11*scale));
	}
	public static void setUIPresentScale(float scale) {
		DimensionScale_present = scale;
		scale = stdDimensionScale * DimensionScale_present;
			stbPresentIconSize 	= new Dimension((int)(32*scale), 	(int)(30*scale));
	}
	
	public MainWin(String title) {
		super(title);
		
		MainWin.setUIScale(MainWin.stdDimensionScale);
		
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
        setResizable(true);
        setIconImage((new ImageIcon("res/JSAPIRecognition1.png")).getImage());
        
        
        initGUIMenuBar();
        this.setBackground(randColor());
        
        
		//starting present
		setPresent(presentables.presents.menu.class);
		
		setVisible(true);
	}
	
///////////////////
//GUI utils
///////////////////
	//make a icon from a file
	public static ImageIcon getImageIcon(String src) {
			try {
			return new ImageIcon( ImageIO.read(new File(src)));
			} catch (IOException e) {
			return null;
			}
	}
	
	//make a icon of specified size from a file
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
	
	public static Color randColor() {
		Random rand = new Random();
		return new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat());
	}
	public static Color randColor(Color theme, float range) {
		Random rand = new Random();
		return new Color(
				theme.getRed() 		* rand.nextFloat() * range,
				theme.getGreen() 	* rand.nextFloat() * range,
				theme.getBlue() 	* rand.nextFloat() * range
			);
	}

///////////////////
//button events
///////////////////
	protected void onAboutClick() {
		JLabel bg = new JLabel(MainWin.getImageIcon("res/aboutbg.png"));

        JLabel title = new JLabel("<html><body>"
        +"<h1 align='center'><font size=+1><u>Audinc</u></font></h1>"
        +"<p align='center'><br>joe cat</p>"
        +"</body></html>",
        SwingConstants.CENTER);

        JLabel artists = new JLabel("<html>"
        +"<br/><p>GDC 3.0 lisence 2023</p>"
        +"</html>",
        SwingConstants.CENTER);

        JOptionPane.showMessageDialog(this,
            new Object[]{bg, title, artists},
            "Audinc (audi, not the car)",
            JOptionPane.PLAIN_MESSAGE);
	}
	
	public void onRestartPresentClick() {
		if(currPresent == null) 
			return; 
		
		currPresent.quit();
		setPresent(currPresent.getClass());
	}
	
	public void quit() {
		if(currPresent != null) {
			currPresent.quit();
		}
		
		dispose();
		System.exit(0);
	}
	
	public static void openScaleUIDialoug(MainWin host) {
			JFrame cframe= new JFrame();
			
			//package components
			ArrayList<DocumentListener> docListeners = new ArrayList<>(5);	
			
			JComponent[] objs = {
					Presentable.genLabelInput("general scale (float): ", new custom_function<JTextField>() {
						@Override public JTextField doTheThing(JTextField thisisnull) {
							JTextField tf = new JTextField(5);
							tf.setText(MainWin.stdDimensionScale+"");
							
							custom_function<Boolean> isValid = new custom_function<>() {
								@Override public Boolean doTheThing(Boolean o) {
									String s = tf.getText();
									return !(s.isBlank() || Float.parseFloat(s) <= 0);
								}};
								
							var v = new DocumentListener() {
								@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
								@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
								@Override public void changedUpdate(DocumentEvent e) {
									if(!isValid.doTheThing(false)) {
										JOptionPane.showInternalMessageDialog(
												null,
												"invalid general scale : " + tf.getText(),
												"unable to set value",
												JOptionPane.OK_OPTION);
										return;
									}
									
									MainWin.setUIScale(Float.parseFloat(tf.getText()));
								}
							};
							
							docListeners.add(v);
							tf.getDocument().addDocumentListener(v);		
							return tf;
						}}),
					Presentable.genLabelInput("window scale (float): ", new custom_function<JTextField>() {
						@Override public JTextField doTheThing(JTextField thisisnull) {
							JTextField tf = new JTextField(5);
							tf.setText(MainWin.DimensionScale_window+"");
							
							custom_function<Boolean> isValid = new custom_function<>() {
								@Override public Boolean doTheThing(Boolean o) {
									String s = tf.getText();
									return !(s.isBlank() || Float.parseFloat(s) <= 0);
								}};
								
							var v = new DocumentListener() {
								@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
								@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
								@Override public void changedUpdate(DocumentEvent e) {
									if(!isValid.doTheThing(false)) {
										JOptionPane.showInternalMessageDialog(
												null,
												"invalid window scale : " + tf.getText(),
												"unable to set value",
												JOptionPane.OK_OPTION);
										return;
									}
									
									MainWin.setUIWindowScale(Float.parseFloat(tf.getText()));
								}
							};
							
							docListeners.add(v);
							tf.getDocument().addDocumentListener(v);		
							return tf;
						}}),
					Presentable.genLabelInput("icon scale (float): ", new custom_function<JTextField>() {
						@Override public JTextField doTheThing(JTextField thisisnull) {
							JTextField tf = new JTextField(5);
							tf.setText(MainWin.DimensionScale_icon+"");
							
							custom_function<Boolean> isValid = new custom_function<>() {
								@Override public Boolean doTheThing(Boolean o) {
									String s = tf.getText();
									return !(s.isBlank() || Float.parseFloat(s) <= 0);
								}};
								
							var v = new DocumentListener() {
								@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
								@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
								@Override public void changedUpdate(DocumentEvent e) {
									if(!isValid.doTheThing(false)) {
										JOptionPane.showInternalMessageDialog(
												null,
												"invalid icon scale : " + tf.getText(),
												"unable to set value",
												JOptionPane.OK_OPTION);
										return;
									}
									
									MainWin.setUIIconScale(Float.parseFloat(tf.getText()));
								}
							};
							
							docListeners.add(v);
							tf.getDocument().addDocumentListener(v);		
							return tf;
						}}),
					Presentable.genLabelInput("present scale (float): ", new custom_function<JTextField>() {
						@Override public JTextField doTheThing(JTextField thisisnull) {
							JTextField tf = new JTextField(5);
							tf.setText(MainWin.DimensionScale_present+"");
							
							custom_function<Boolean> isValid = new custom_function<>() {
								@Override public Boolean doTheThing(Boolean o) {
									String s = tf.getText();
									return !(s.isBlank() || Float.parseFloat(s) <= 0);
								}};
								
							var v = new DocumentListener() {
								@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
								@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
								@Override public void changedUpdate(DocumentEvent e) {
									if(!isValid.doTheThing(false)) {
										JOptionPane.showInternalMessageDialog(
												null,
												"invalid presnet scale : " + tf.getText(),
												"unable to set value",
												JOptionPane.OK_OPTION);
										return;
									}
									
									MainWin.setUIPresentScale(Float.parseFloat(tf.getText()));
								}
							};
							
							docListeners.add(v);
							tf.getDocument().addDocumentListener(v);		
							return tf;
						}})
			};
			docListeners.trimToSize();
			
	        JPanel wrapper = new JPanel();
	        	wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
	        for(JComponent v : objs) {v.setAlignmentX(Component.LEFT_ALIGNMENT); wrapper.add(v);}
	        JScrollPane wrapper_scroll = new JScrollPane(wrapper,
	        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	        	wrapper_scroll.setBorder(BorderFactory.createEmptyBorder());
	        	wrapper_scroll.setMaximumSize(new Dimension(MainWin.stdDimension.width * 2, MainWin.stdDimension.height * 2));
	        
	        switch (JOptionPane.showConfirmDialog(cframe, wrapper_scroll, "options", JOptionPane.OK_CANCEL_OPTION)) {
	        	case JOptionPane.YES_OPTION:
	        			for(var dl : docListeners)
	        				dl.changedUpdate(null);
	        			
	        			if(host != null)
	        				host.onRestartPresentClick();
	        		break;
	        }
	}
//////////////////
// private
/////////////////
	private static final long serialVersionUID = 1L;
	private static String mainTitle = "Audinc";
	private Presentable currPresent = null;
	
	private void initGUIMenuBar() {
		JMenuBar menubar = new JMenuBar();
		
		JMenu help 	= new JMenu("Help");
			help.setMnemonic('H');
			JMenuItem mainMenu = new JMenuItem("main menu");
				mainMenu.addActionListener(event -> setPresent(presentables.presents.menu.class));
				mainMenu.setMnemonic('M');
			JMenuItem about = new JMenuItem("About");
				about.setMnemonic('A');
				about.addActionListener(event -> onAboutClick());
			JMenuItem quit = new JMenuItem("Quit");
				quit.setMnemonic('Q');
				quit.addActionListener(event -> quit());
			JMenuItem restartPresent = new JMenuItem("restart present");
				restartPresent.setMnemonic('R');
				restartPresent.addActionListener(event -> onRestartPresentClick());
			JMenuItem scaleoptions = new JMenuItem("options");
				scaleoptions.setMnemonic('S');
				scaleoptions.addActionListener(event -> MainWin.openScaleUIDialoug(this));
				
		help.add(mainMenu);
		help.add(scaleoptions);
		help.add(about);
		help.addSeparator();
		help.add(restartPresent);
		help.add(quit);
		
		//menu bar present options
		JMenu presents = new JMenu("Presents");
			presents.setMnemonic('P');
			ArrayList<String> present_names = new ArrayList<>(Presents.size());
			for(var v : Presents) { present_names.add((String)Presentable.tryForStatic(v, "getDisplayTitle")); };
		for(var k : Presents) {
			JMenuItem jmi = new JMenuItem((String)Presentable.tryForStatic(k, "getDisplayTitle"));
			jmi.addActionListener(event -> setPresent(k));
			presents.add(jmi);
		}
		
		menubar.add(help);
		menubar.add(presents);
		
		setJMenuBar(menubar);
	}
}