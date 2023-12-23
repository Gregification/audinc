package presentables;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;

import audinc.gui.MainWin;
import presentables.presents.SerialPoke;

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
	public static Path makeRoot(Class<? extends Presentable> clas) {
		return Presentable.makeRoot(clas, Path.of(""));
	}
	public static Path makeRoot(Class<? extends Presentable> clas, Path extension) {
		Path path = Presentable.getRoot(clas);
		var file = path.toFile();
		if(!file.exists()) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
//		System.out.println("presentable>makeroot, dir path: \t\t" + path.toAbsolutePath().toString());
//		System.out.println("does dir exist? " + file.exists());
		
		var extPath = path.resolve(extension).toAbsolutePath();
		if(!extPath.toFile().exists()) {
			try {
//				System.out.println("presentable>makeroot, full path: \t\t" + extPath.toFile().toString());
				Files.createFile(extPath);
			} catch (IOException e) {
				System.out.println("error!");
				e.printStackTrace();
			}
		}
		
		return extPath;
	}
	
	public static ImageIcon getImgIcon() {
//		System.out.println("presentable>getImgIcon(),default path");
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
//		System.out.println("presentable>getImagIcon(),prefereed : " + prefereed);
		try {
			ImageIcon ii = new ImageIcon( ImageIO.read(new File(prefereed)));
			if(ii != null) return ii;
		} catch (IOException e) {}
		
		try {
			var v = new File("res/presentIcons/default.png");
			System.out.println("presentable>getImageIcon()> unable to find : "+prefereed+" : => using : " + v.getAbsolutePath());
			
			return new ImageIcon( ImageIO.read(new File("res/presentIcons/default.png")));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
		
	public static JPanel genFilePicker(
			Class<? extends Presentable> c,
			Path 		defaultPath,
			JCheckBox 	savetoggler,
			custom_function<JFileChooser> fileChooserEvent){
		
		JPanel logTranscript_panel = new JPanel();
			SpringLayout editorTab_portDescriptor_layout = new SpringLayout();
			logTranscript_panel.setLayout(editorTab_portDescriptor_layout);{
				SpringLayout layout = editorTab_portDescriptor_layout;
				
		    	JButton saveFilePicker = new JButton("choose file");
		    		saveFilePicker.setBackground(new Color(0f,0f,0f,0f));
		    		saveFilePicker.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		    		saveFilePicker.addActionListener(e -> {
			    			Path pPreferred = Presentable.getRoot(SerialPoke.class);
			    				
			    			JFileChooser fc = new JFileChooser(pPreferred.toAbsolutePath().toString());
			    			
			    			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			    			
			    			fileChooserEvent.doTheThing(fc);
			    			
		    			});
		    		
				JTextField input = new JTextField(defaultPath.toAbsolutePath().toString());
				
				logTranscript_panel.add(savetoggler);
				logTranscript_panel.add(input);
				logTranscript_panel.add(saveFilePicker);
		 
		        //toggler
		        layout.putConstraint(SpringLayout.WEST, savetoggler,
		        			5, SpringLayout.WEST, logTranscript_panel);
		        layout.putConstraint(SpringLayout.NORTH, savetoggler,
		        			5, SpringLayout.NORTH, logTranscript_panel);
		 
		        //file selector button
		        layout.putConstraint(SpringLayout.EAST, saveFilePicker,
		        		-5, SpringLayout.EAST, logTranscript_panel);
		        layout.putConstraint(SpringLayout.NORTH, saveFilePicker,
		        		3, SpringLayout.NORTH, logTranscript_panel);
		        layout.putConstraint(SpringLayout.SOUTH, saveFilePicker,
		        		-3, SpringLayout.NORTH, input);
		        
		        //text field .
		        layout.putConstraint(SpringLayout.NORTH, input,
		        			5, SpringLayout.SOUTH, savetoggler);
		        layout.putConstraint(SpringLayout.SOUTH, input,
	        			30, SpringLayout.SOUTH, savetoggler);
		        
		        //Adjust constraints for the content pane. text field assumed to be bottom most component
		        layout.putConstraint(SpringLayout.EAST, logTranscript_panel,
		        			1, SpringLayout.EAST, input);
		        layout.putConstraint(SpringLayout.SOUTH, logTranscript_panel,
		                    5, SpringLayout.SOUTH, input);
			}
			
		return logTranscript_panel;
	}
	
	public static void SpringLayout_EWJoin(JComponent leftC, int offset, JComponent rightC, SpringLayout sl) {
		sl.putConstraint(SpringLayout.EAST, leftC,
				offset, SpringLayout.WEST, rightC);
	}
	
	public static JPanel genLabelInput(JComponent label, JComponent input) {//orgionaly done with springlayout but that kept breaking
		JPanel container = new JPanel(new FlowLayout());
			container.add(label);
			container.add(input);
			
		return container;
	}
	public static JPanel genLabelInput(String title, custom_function<JTextField> cf) {
		return Presentable.genLabelInput(title, cf.doTheThing(null));
	}
	public static JPanel genLabelInput(JComponent title, custom_function<JComponent> cf) {
		return Presentable.genLabelInput(title, cf.doTheThing(null));
	}
	public static JPanel genLabelInput(JLabel label, custom_function<JTextField> cf) {
		return Presentable.genLabelInput(label, cf.doTheThing(null));
	}
	public static JPanel genLabelInput(String title, JTextField input) {
		return Presentable.genLabelInput(new JLabel(title), input);
	}
	public static void genLabledContent(JComponent host, String title, String tooltip, int x, int y, JComponent... displays) {
		var label = new JLabel(title+" :");
			
		if(!tooltip.isBlank())
			label.setToolTipText(tooltip);
			
		host.add(label, 	Presentable.createGbc(x, y));
		for(var v : displays)
			host.add(v,	Presentable.createGbc(++x, y));
	}
	public static GridBagConstraints createGbc(int x, int y) {	//scoured from -> https://stackoverflow.com/questions/9851688/how-to-align-left-or-right-inside-gridbaglayout-cell
	      GridBagConstraints gbc = new GridBagConstraints();
	      gbc.gridx = x;
	      gbc.gridy = y;
	      gbc.gridwidth = 1;
	      gbc.gridheight = 1;

	      gbc.anchor = (x == 0) ? GridBagConstraints.WEST : GridBagConstraints.EAST;
	      gbc.fill = (x == 0) ? GridBagConstraints.BOTH
	            : GridBagConstraints.HORIZONTAL;

	      gbc.weightx = (x == 0) ? 0.1 : 1.0;
	      gbc.weighty = 1.0;
	      return gbc;
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
	public static void readFromPath(Path path, custom_bufferedReader lambda) {
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
	
	//quick c&p
	
//	var table = new JTable(null, new String[] {"column1", "column2"}) {
//	private static final long serialVersionUID = 1L;	//eclipse complains
//	public boolean isCellEditable(int row, int column) { return false; }; //disables user editing of table
//};
//table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//table.setCellEditor(null);
//table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
//		@Override public void valueChanged(ListSelectionEvent e) {
//			onRowSelect(e);
//		}
//	});

//var sp = new JSplitPane(SwingConstants.VERTICAL, leftPane, rightPane);

//var scrollFrame = new JScrollPane(srcPanel,	
//		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
}