package presentables.presents;

import presentables.Presentable;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import audinc.gui.MainWin;
import audinc.gui.WrapLayout;

/* JSAPI documentation: https://docs.oracle.com/cd/E17802_01/products/products/java-media/speech/forDevelopers/jsapi-doc/index.html
 * FreeTTS: https://freetts.sourceforge.io/#freetts_api
 */

public class txtToSpeach extends Presentable {
	private JSplitPane runTS, buildTS;
	private JTextArea runTS_rP_txtEditor_input;
	private JButton runTS_lP_runSettings_startBtn;
	
	private ArrayList<String> speachModelSources = new ArrayList<>();
	private final Map<String, String> saveMap = Map.of(
			"runTS_rP_txtEditor_input", 	"textEditorContent.txt",
			"speachModelSources", 			"speachModelSources.txt"
		);
	
//	private runT_textInput;
	
	@Override protected void init(MainWin mw) { initGUI(mw); }
	@Override public void quit() 				{
		try { save(); } catch (IOException e) { e.printStackTrace(); }
	}
	@Override protected void start() 			{
		try { load(); } catch (IOException e) { e.printStackTrace(); }
	}
	@Override protected void initGUI(MainWin mw){
		mw.setLayout(new BorderLayout());
		
		JTabbedPane mainTp = new JTabbedPane();
		Border emptyBorder = BorderFactory.createEmptyBorder();
		
		//run tab
		JPanel runT = new JPanel(); runT	.setLayout(new BorderLayout());
			//run tab \ screen left 
			JPanel runTS_l = new JPanel(); runTS_l	.setLayout(new BorderLayout());
				JPanel runTS_lP_runSettings = new JPanel(); runTS_lP_runSettings.setLayout(new WrapLayout());
					//run tab \ screen left \ settings UI
					runTS_lP_runSettings_startBtn = new JButton("START");
						runTS_lP_runSettings_startBtn.addActionListener(event -> onRunPanelStartClick());
				runTS_lP_runSettings.add(runTS_lP_runSettings_startBtn);
				JScrollPane runTS_lP_txtEditor_ScrollFrame = new JScrollPane(runTS_lP_runSettings,	
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					runTS_lP_txtEditor_ScrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/3), (int)MainWin.stdDimension.getHeight()));
					runTS_lP_txtEditor_ScrollFrame.setPreferredSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/3), (int)MainWin.stdDimension.getHeight()));
					runTS_lP_txtEditor_ScrollFrame.setAutoscrolls(true);
					
			runTS_l.add(runTS_lP_txtEditor_ScrollFrame, BorderLayout.CENTER);
			
			
			//run tab \ screen right
			JPanel runTS_rP = new JPanel();	runTS_rP	.setLayout(new BorderLayout());
				//run tab \ screen right \ text editor (c)
				JPanel runTS_rP_txtEditor = new JPanel();	runTS_rP_txtEditor	.setLayout(new BorderLayout());
					runTS_rP_txtEditor_input = new JTextArea("text here will be read");
					runTS_rP_txtEditor_input.setLineWrap(true);
					JScrollPane runTS_rP_txtEditor_ScrollFrame = new JScrollPane(runTS_rP_txtEditor_input,	
							JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						runTS_rP_txtEditor_ScrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/5), (int)MainWin.stdDimension.getHeight()));
						runTS_rP_txtEditor_ScrollFrame.setAutoscrolls(true);
				runTS_rP_txtEditor.add(runTS_rP_txtEditor_ScrollFrame, BorderLayout.CENTER);
				
			runTS_rP.add(runTS_rP_txtEditor, BorderLayout.CENTER);
		
			//combine run tabs
			runTS = new JSplitPane(SwingConstants.VERTICAL, runTS_l, runTS_rP);
			runT.add(runTS, BorderLayout.CENTER);
		
		mainTp.addTab("synth", MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize), runT, "select and use voice models");
		
		//build tab
		JPanel buildT = new JPanel(); buildT	.setLayout(new BorderLayout());
			//build tab \ screen left
			JPanel buildTS_l = new JPanel(); buildTS_l	.setLayout(new BorderLayout());
				buildTS_l.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/5), (int)MainWin.stdDimension.getHeight()));
			
			//build tab \ screen right
			JPanel buildTS_r = new JPanel();	buildTS_r	.setLayout(new BorderLayout());
				buildTS_r.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/5), (int)MainWin.stdDimension.getHeight()));
			
			//combine build tabs
			buildTS = new JSplitPane(SwingConstants.VERTICAL, buildTS_l, buildTS_r);
			buildT.add(buildTS, BorderLayout.CENTER);
		
		mainTp.addTab("build", MainWin.getImageIcon("res/build.png", MainWin.stdtabIconSize), buildT, "build models");
		
		//import tab
		JPanel importT = new JPanel(); importT	.setLayout(new BorderLayout());
			JToolBar importT_toolBar = new JToolBar("Controls");
				JButton importT_toolBar_importbtn = new JButton(MainWin.getImageIcon("res/file.png", MainWin.stdtabIconSize));
					importT_toolBar_importbtn.setBorder(emptyBorder);
				importT_toolBar.add(importT_toolBar_importbtn);
			importT.add(importT_toolBar, BorderLayout.PAGE_START);
		
		mainTp.addTab("import", MainWin.getImageIcon("res/import.png", MainWin.stdtabIconSize), importT, "import models");
		
		//tab(nocturnal crab) activities
		mainTp.addChangeListener(new ChangeListener() {
			@Override public void stateChanged(ChangeEvent e) {
				if(mainTp.getSelectedComponent().equals(importT)) {
					Path root = MainWin.settingsDir.resolve(this.getClass().getName());
						if(Files.notExists(root)) return;
					load_speachModelSources(root, false);
				}
			}
			
		});
		
		mw.add(mainTp);
	}

///////////////////
// gui events
///////////////////
	protected void onRunPanelStartClick() {
		
	}

///////////////////
//freeTTS
///////////////////
	

///////////////////
//save & load. for everything
///////////////////
	protected Path save() throws IOException {
		Path root = MainWin.settingsDir.resolve(this.getClass().getName());
			Files.createDirectories(root);
			Path path = root.resolve(saveMap.get("runTS_rP_txtEditor_input"));
				Files.writeString(path, runTS_rP_txtEditor_input.getText(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			path = root.resolve(saveMap.get("speachModelSources"));
				writeToPath(path, bw ->{
						for(var v : speachModelSources) try { bw.write(v); bw.newLine(); } catch (IOException e) {} 
					});
		return root;	
	}
	
	protected Path load() throws IOException {
		Path root = MainWin.settingsDir.resolve(this.getClass().getName());
		if(Files.notExists(root)) return null;
			
//		this.load_speachModelSources(root, true);
		
		Path path = root.resolve(saveMap.get("runTS_rP_txtEditor_input"));
			readFromPath(path, br -> { runTS_rP_txtEditor_input.setText(""); String line;
					try { while((line = br.readLine()) != null) runTS_rP_txtEditor_input.append(line); } catch (IOException e) { e.printStackTrace(); }
				});
		
		return root;	
	}
	
	protected void load_speachModelSources(Path root, boolean force) {
		Path path = root.resolve(saveMap.get("speachModelSources"));
		if(Files.notExists(path)) return;
		
		if(!force) { 
			try { BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				if(attr.lastAccessTime().compareTo(attr.lastModifiedTime()) == 1) return;
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		readFromPath(path, br -> {
				speachModelSources.clear();
				String line;
				try { while((line = br.readLine()) != null)
					lazyPathRecursion(Paths.get(br.readLine()), 3, p -> { //every "p" is a file, not a directory
							this.speachModelSources.add(p.toString());
						});
				} catch (IOException e) { e.printStackTrace(); }
			});
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "txt -> speach";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/tts.png"); }
	public static String getDescription() 	{	return "<html><body>"
		+ "voice synthesis using FreeTTS (1.2.2) and recordings with VestFox and Festival." 
		+ "<ul><li>MBROLA files not supported.</li></ul>"
		+ "</body></html>";	}
	
}
