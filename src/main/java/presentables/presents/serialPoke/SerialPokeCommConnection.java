package presentables.presents.serialPoke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import DOMViewer.FileViewer;
import DOMViewer.parsers.SPCParser;
import audinc.gui.MainWin;
import audinc.gui.WrapLayout;
import draggableNodeEditor.DraggableNodeEditor;
import draggableNodeEditor.DraggableNodeGroup;
import presentables.Presentable;
import presentables.presents.SerialPoke;

/**
 * a GUI port manager for serial ports. extends JPanel
 * <p>
 * slow initialization is from file IO.
 */
//see top up voted answer for info on reading -> https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
public class SerialPokeCommConnection extends JPanel{		
	public String title;
	public SerialPort sp;
	public int logSettings 			= Integer.MAX_VALUE;//see line ~86 of https://github.com/Fazecast/jSerialComm/blob/master/src/main/java/com/fazecast/jSerialComm/SerialPort.java
	
	public FileViewer viewer		= new FileViewer(null);
	public SPCParser settingsParser;
	
	public DraggableNodeEditor inputEditor	= new DraggableNodeEditor(Map.of(
				DraggableNodeGroup.GENERAL, Void.class,//no context
				DraggableNodeGroup.SERIAL_PIKE, this
			));
	
	public JTabbedPane content_tabb = new JTabbedPane();
	
	//private
	private boolean
		loggingEnabled = true,
		saveLogTranscript = false;
	private Path logTranscriptPath;
	private BufferedWriter logger = null;
	private JLabel noticeDisplay;
	
	public SerialPokeCommConnection(SerialPort sp, String title) {
		super();
		this.title = title;
		this.sp = sp;
		
		setSettingsTo(getDefaultSettingsPath());
		
		try {
			this.logTranscriptPath = Presentable.getRoot(SerialPoke.class).toAbsolutePath().resolve(this.getDefaultSaveName());
		} catch (java.nio.file.InvalidPathException e) {
			JLabel jl = new JLabel("connection path is invalid. " + e);
			JScrollPane _scroll = new JScrollPane(jl,	
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				_scroll.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*1/8), (int)MainWin.stdDimension.getHeight()));
				_scroll.setAutoscrolls(true);
			
			JOptionPane.showMessageDialog(this, _scroll);
			return; 
		}
		
		genGUI();
		
		sp.addDataListener(this.getDataListener());
		
		setNoticeText("connection is not opened untill explicetely told to open", Color.black);
	}
	
	public void quit() {
		if(sp.isOpen()) {
			sp.closePort();
		}
		
		if(logger != null)
			try {
				logger.close();
			} catch (IOException e) { e.printStackTrace(); }
		
		inputEditor.quit();
	}

	protected SerialPortDataListener getDataListener() {
		return new SerialPortDataListener() {			
			@Override public int getListeningEvents() { return Integer.MAX_VALUE;}
				
			@Override public void serialEvent(SerialPortEvent spe) {
				System.out.println( this.hashCode() + "@" + System.currentTimeMillis() + " received:" + Arrays.toString(spe.getReceivedData()));
				
				//if event is to be logged
				if(loggingEnabled && ((logSettings & spe.getEventType()) != 0)) {
					long time_mil = (int)(System.nanoTime()/Math.pow(10,6)); 
					String 
						serialPortEventName_s 	= spe.toString(),
						receivedData_s			= Arrays.toString(spe.getReceivedData()); 
					StringBuilder sb = new StringBuilder();
						sb.append("<tr><td>");
						sb.append(time_mil);
						sb.append("</td><td>");
						sb.append(serialPortEventName_s);
						sb.append("</td><td>");
						sb.append(receivedData_s);
						sb.append("</td></tr>");
					
					if(saveLogTranscript) {
						String line = String.format("%-12d:%30s:%d\n",
								time_mil,
								serialPortEventName_s,
								receivedData_s);
						log(line);
						
					}
				}
				
				serialPortEvent(spe);
			}
		};
	}
	
	protected void serialPortEvent(SerialPortEvent spe) {
		switch(spe.getEventType()) {
			case SerialPort.LISTENING_EVENT_DATA_AVAILABLE :
				
				break;
			case SerialPort.LISTENING_EVENT_DATA_RECEIVED :
				break;
			case SerialPort.LISTENING_EVENT_DATA_WRITTEN :
				break;
			case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED :
				break;
			case SerialPort.LISTENING_EVENT_BREAK_INTERRUPT :
				break;
			/* clear to send */
			case SerialPort.LISTENING_EVENT_CTS :
				break;
			/* data set ready */
			case SerialPort.LISTENING_EVENT_DSR : 
				break;
			/* This is an input for DTE devices and an output for DCE devices. 
			 * This signals the DTE device that there is an incoming call.
			 * This signal is maintained "Off" at all times except when the 
			 * DCE receives a ringing signal. */
			case SerialPort.LISTENING_EVENT_RING_INDICATOR :
				break;
			/* you are opening the port while the remote device is in the middle of transmitting. */
			case SerialPort.LISTENING_EVENT_FRAMING_ERROR :
				break;
			/* device driver buffer overrun */
			case SerialPort.LISTENING_EVENT_FIRMWARE_OVERRUN_ERROR :
				break;
			/* application buffer overrun */
			case SerialPort.LISTENING_EVENT_SOFTWARE_OVERRUN_ERROR :
				break;
			/* parity error */
			case SerialPort.LISTENING_EVENT_PARITY_ERROR :
				break;
		}
	}
	
	public boolean setSettingsTo(Path src) {
		if(!src.toFile().exists())
			return false;
		
		if(settingsParser == null || src.toAbsolutePath().toFile().equals(settingsParser.srcFile)) {
			settingsParser = new SPCParser(src.toFile());
			settingsParser.settings.rebase(sp);
			viewer.setParser(settingsParser);
		}else{
			settingsParser.settings.rebase(sp);
			settingsParser.ParseFile();
		}
		settingsParser.updateGUI();
		
//		System.out.println("serial poke comm connection > setSettingsTo (" + settingsParser.settings.modifiedSettings.size() + " changed) -> src:" + src);
		
		return true;
	}
	
//////////////////////
//UI
//////////////////////
	private void genGUI() {
		setLayout(new BorderLayout());
			
		//brute force (easiest) way to generate a color that is brighter than so-and-so% of all possible colors 
		Color color;
		int maxTries = 5;
		do{
			color = MainWin.randColor();
			maxTries--;
//				System.out.println(Math.random() > .5 ? "lol" : "lmao");
		} while(maxTries > 0 && Math.pow(color.getRed(), 2) + Math.pow(color.getGreen(), 2) + Math.pow(color.getBlue(), 2) > 195075.0 * .6); //195075= (255^2) * 3
		setBackground(color);
			
		noticeDisplay = new JLabel("notices");
		add(noticeDisplay, BorderLayout.PAGE_END);
		
		
		JPanel 
			inputTabb 		= new JPanel(),
			liveInfoTabb 	= new JPanel(),
			generalTabb 	= new JPanel();
		
		//general tab
		content_tabb.addTab("general", MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize),
				generalTabb, "general manager");
		//settings tab
		content_tabb.addTab("settings", MainWin.getImageIcon("res/note.png", MainWin.stdtabIconSize), 
				viewer, "seral port settings and info");
		//live info tab
		content_tabb.addTab("live info", MainWin.getImageIcon("res/info1.png", MainWin.stdtabIconSize), 
				liveInfoTabb, "event driven info about the connection. read only");
		//input tab
		content_tabb.addTab("input editor", MainWin.getImageIcon("res/clear.png", MainWin.stdtabIconSize), 
				inputTabb, "let me be clear");
		
		try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()){
			executor.execute(() -> genUI_tab_general(generalTabb));
			executor.execute(() -> genUI_tab_liveInfo(liveInfoTabb));
			executor.execute(() -> genUI_tab_input(inputTabb));
		}
		
		content_tabb.addChangeListener(new ChangeListener() {
				@Override public void stateChanged(ChangeEvent e) {
					var selectedComponent = content_tabb.getSelectedComponent(); 
					
					if(selectedComponent.equals(viewer)) {
						viewer.updateMeta(viewer.getParser().getPath());
					}else if(selectedComponent.equals(settingsParser)) {
						settingsParser.updateGUI();
					}
				}
				
			});
		
		add(content_tabb, BorderLayout.CENTER);
	}
	public void onSaveSettingsClick(boolean forceSave) {
		if(forceSave || settingsParser.isModified()) {
			settingsParser.SaveToFile(settingsParser.srcFile);
		}
	}
	public void onSelectSettingFileClick() {
		JFileChooser fc = new JFileChooser(settingsParser.getPath().getParent().toAbsolutePath().toString());
		
		FileNameExtensionFilter allowedFiles = new FileNameExtensionFilter("spc settings", new String[] {SPCSettings.FileExtension_Settings});
			fc.addChoosableFileFilter(allowedFiles);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		switch(fc.showOpenDialog(null)) {
			case JFileChooser.APPROVE_OPTION : 
				setSettingsTo(fc.getSelectedFile().toPath());
				break;
			case JFileChooser.CANCEL_OPTION : 
				break;
		}
	}
	public void openLoggingDialoug() {		
		JFrame cframe = new JFrame();
        	cframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel logCatagories = new JPanel(new WrapLayout());
        	logCatagories.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Logging catagories", TitledBorder.LEFT, TitledBorder.TOP));
            logCatagories.setEnabled(this.loggingEnabled);
        	{
	        	Field[] fields = SerialPort.class.getDeclaredFields();
	        	String startsWith = "LISTENING_EVENT_";
	        	for(var v : fields) {
	        		String name = v.getName();
	        		if(name.startsWith(startsWith) && v.getType() == int.class) {
	        			if(name.endsWith("TIMED_OUT")) continue;
	        			
	        			int 	val = Integer.MIN_VALUE;
	        			
	        			try { val = v.getInt(null); } 
	        			catch (IllegalArgumentException e) 	{ e.printStackTrace(); } 
	        			catch (IllegalAccessException e) 	{ e.printStackTrace(); }
	        			
	        			if(val == Integer.MIN_VALUE) 		{ new IllegalArgumentException("retreived bad value form serialport. from:" + v.getName()); }
	        			
	        			final int val_f = val;
						JCheckBox jcb = new JCheckBox(name.substring(startsWith.length()), (this.logSettings | val)!=0);
						jcb.addItemListener(il -> {
							 	if(il.getStateChange() == ItemEvent.SELECTED) {
				        			logSettings =  logSettings | val_f;
				        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { //double check because there are other states that can be triggered
				        			int res = logSettings & val_f;
				        			if(res != 0) {
				        				logSettings = logSettings & (~val_f);
				        			}
				        		}
						 	});
						jcb.setSelected((logSettings & val_f) != 0 );
						logCatagories.add(jcb);
	        		}
	        	}
        	}
        
        	
        JCheckBox logToggler =	new JCheckBox("logging enabled", this.loggingEnabled);
        	logToggler.addItemListener(il -> {
        		if(il.getStateChange() == ItemEvent.SELECTED) {
        			this.setLogging(true);
        			logCatagories.setEnabled(true);
        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { //double check because there are other states that can be triggered
        			this.setLogging(false);
        			logCatagories.setEnabled(false);
        		}
        	});
    		
    	JPanel logTranscript_panel = new JPanel();
			SpringLayout editorTab_portDescriptor_layout = new SpringLayout();
			logTranscript_panel.setLayout(editorTab_portDescriptor_layout);{
				SpringLayout layout = editorTab_portDescriptor_layout;
				
				JCheckBox savetoggler =	new JCheckBox("save transcript", isLoggingToTranscript());
		    		savetoggler.addItemListener(il -> {
		        		if(il.getStateChange() == ItemEvent.SELECTED) {
		        			this.saveLogTranscript = true;
		        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { //double check because there are other states that can be triggered
		        			this.saveLogTranscript = false;
		        		}
		        	});
				
		    	JButton saveFilePicker = new JButton("choose file");
		    		saveFilePicker.setBackground(new Color(0f,0f,0f,0f));
		    		saveFilePicker.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		    		saveFilePicker.addActionListener(e -> {
			    			Path pPreferred = Presentable.getRoot(SerialPoke.class);
			    				
			    			JFileChooser fc = new JFileChooser(pPreferred.toAbsolutePath().toString());
			    			
			    			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			    			
			    			switch(fc.showOpenDialog(cframe)) {
			    				case JFileChooser.APPROVE_OPTION : 
				    					try {
				    						var file	= fc.getSelectedFile();
				    						
				    						if(!file.exists()) {
				    							setNoticeText("file cannot be found: " + file.toPath(), new Color(0,0,0));
				    						}
				    						
				    						if(logger != null) logger.close();
				    						
				    						if(file.isDirectory()) {
				    							Files.createDirectories(Paths.get(file.getAbsolutePath()));
				    							logTranscriptPath = logTranscriptPath.resolve(getDefaultSaveName());
				    						}
				    					} catch (IOException e1) { e1.printStackTrace(); }
			    					break;
			    				case JFileChooser.CANCEL_OPTION : 
			    					break;
			    			}
		    			});
		    		
				JTextField input = new JTextField(logTranscriptPath.toAbsolutePath().toString());
				
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
		        
		        //Adjust constraints for the content pane. text field assumed to be bottom most component
		        layout.putConstraint(SpringLayout.EAST, logTranscript_panel,
		        			1, SpringLayout.EAST, input);
		        layout.putConstraint(SpringLayout.SOUTH, logTranscript_panel,
		                    5, SpringLayout.SOUTH, input);
			}
    		
        
        JComponent[] objs = {
        		logToggler,
	        	logCatagories,
	        	logTranscript_panel
        	};
        JPanel wrapper = new JPanel();
        	wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        for(var v : objs) {v.setAlignmentX(JComponent.LEFT_ALIGNMENT); wrapper.add(v);}
        
        JScrollPane wrapper_scroll = new JScrollPane(wrapper,
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        	wrapper_scroll.setPreferredSize(new Dimension((int)(300*MainWin.stdDimensionScale), (int)(150*MainWin.stdDimensionScale)));
        	wrapper_scroll.setBorder(BorderFactory.createEmptyBorder());
        JOptionPane.showMessageDialog(cframe, wrapper_scroll, "logging", JOptionPane.PLAIN_MESSAGE);
        
	}
	public void openInfoDialoug() {
		JOptionPane.showMessageDialog(this, new Object[] {this.getInfoScrollFrame()}, "info: " +title, JOptionPane.PLAIN_MESSAGE);
	}	
	
	private JComponent getInfoScrollFrame() {
		JLabel spInfo = new JLabel();{
			 StringBuilder sb = new StringBuilder("<html>");
			 sb.append("<table>"
			 		+ "<tr>"
			 		+ "<th>field</th>"
			 		+ "<th>value</th>"
			 		+ "</tr>");
			 
			 for(String[] s : SerialPoke.getSPFullRowInfo(sp)) {
				 sb.append("<tr>"
				 		+ "<td>"+s[0]+"</td>"
				 		+ "<td>"+s[1]+"</td>"
				 		+ "</tr>");
			 }
			 
			 sb.append("</table>");
			 
			 spInfo.setText(sb.toString());
		}
		JScrollPane spInfo_scroll = new JScrollPane(spInfo,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return spInfo_scroll;
	}
	
	public void setNoticeText(String text) {
		this.noticeDisplay.setText(text);
	}
	public void setNoticeText(String text, Color color) {
		this.noticeDisplay.setForeground(color);
		setNoticeText(text);
	}
	public void appendNoticeText(String text, Color color) {
		setNoticeText(this.noticeDisplay.getText() + text);
	}
	private boolean isLogging() {
		return this.loggingEnabled;
	}
	private void setLogging(boolean enabled) {
		if(this.isLogging() == enabled) return;
		this.loggingEnabled = enabled;
	}
	public boolean isLoggingToTranscript() {
		return this.saveLogTranscript;
	}
	public void setLoggingTranscript(boolean enabled) {
		if(this.isLoggingToTranscript() == enabled) return;
	}
	public boolean setLoggingTranscript(boolean enabled, String setTo) {
		if(!this.setLoggingTranscript(setTo)) return false;
		
		this.setLogging(enabled);
		return true;
	}
	public boolean setLoggingTranscript(String setTo) {
		setLoggingTranscript(this.loggingEnabled, setTo);
		return false;
	}
	public String getDefaultSaveName() {
		return title + ".log";
	}
	private void log(String toLog) {
		if(!isLoggingToTranscript()) return;
		if(logger == null)
			try {
				logger = Files.newBufferedWriter(logTranscriptPath);
			} catch (IOException e) { e.printStackTrace(); }
		
		try {
			logger.write(toLog);
			logger.newLine();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	private void genUI_tab_liveInfo(JPanel tab_cont) {
		tab_cont.setLayout(new BorderLayout());
		
		var emptyBorder = BorderFactory.createEmptyBorder();
		
		//tool bar
		JToolBar toolbar = new JToolBar();
			JButton tb_infobtn = new JButton(MainWin.getImageIcon("res/info.png", MainWin.stdtabIconSize));
				tb_infobtn.setBorder(emptyBorder);
				tb_infobtn.setToolTipText("info");
				tb_infobtn.addActionListener(e -> openInfoDialoug());
			JButton tb_logbtn = new JButton(MainWin.getImageIcon("res/log.png", MainWin.stdtabIconSize));
				tb_logbtn.setBorder(emptyBorder);
				tb_logbtn.setToolTipText("log settings");
				tb_logbtn.addActionListener(e -> openLoggingDialoug());
				
		toolbar.add(tb_infobtn);
		toolbar.add(tb_logbtn);
		
		tab_cont.add(toolbar, BorderLayout.PAGE_START);
		
		//text area
		JTextPane logout = new JTextPane();
			logout.setContentType("text/html");
			logout.setEditable(false);
			logout.setText("<table>"
			 		+ "<tr>"
			 		+ "<th>time</th>"
			 		+ "<th>event</th>"
			 		+ "<th>data received</th>"
			 		+ "</tr>");
			SimpleAttributeSet logout_sas = new SimpleAttributeSet();
			JScrollPane logout_scroll = new JScrollPane(logout,	
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tab_cont.add(logout_scroll, BorderLayout.CENTER);
		
		sp.addDataListener(new SerialPortDataListener() {
			StyledDocument logout_doc = logout.getStyledDocument();
			int endLength = ("</html>").length();
			
			@Override public int getListeningEvents() { return 0; }
				
			@Override public void serialEvent(SerialPortEvent spe) {
				//if event is to be logged
				if(loggingEnabled && ((logSettings & spe.getEventType()) != 0)) {
					try {
						long time_mil = (int)(System.nanoTime()/Math.pow(10,6)); 
						String 
							serialPortEventName_s 	= spe.toString(),
							receivedData_s			= Arrays.toString(spe.getReceivedData()); 
						StringBuilder sb = new StringBuilder();
							sb.append("<tr><td>");
							sb.append(time_mil);
							sb.append("</td><td>");
							sb.append(serialPortEventName_s);
							sb.append("</td><td>");
							sb.append(receivedData_s);
							sb.append("</td></tr>");
						
						logout_doc.insertString(logout_doc.getLength() - endLength, sb.toString(), logout_sas);
						
						if(saveLogTranscript) {
							String line = String.format("%-12d:%30s:%d\n",
									time_mil,
									serialPortEventName_s,
									receivedData_s);
							log(line);
							
						}
					} catch (BadLocationException e) { e.printStackTrace(); }
				}
				
				serialPortEvent(spe);
			}
		});
	}
	
	private void genUI_tab_general(JPanel tab_cont) {
		tab_cont.setLayout(new WrapLayout());
		
		var applySettingButton = new JButton("apply settings");
		
		JCheckBox jcb_toggleport = new JCheckBox("port enabled", sp.isOpen());
			jcb_toggleport.setToolTipText("open/close port. ");
			jcb_toggleport.addItemListener(il -> {
					
				 	if(il.getStateChange() == ItemEvent.SELECTED) {
				 		int delay = 200;
	        			setNoticeText((sp.openPort(delay) ? "port is open" : "failed to open port" ), Color.black);
	        			jcb_toggleport.setSelected(sp.isOpen());	        			
	        			
	        		}else if(il.getStateChange() == ItemEvent.DESELECTED) { 
	        			if(sp.isOpen())
	        				setNoticeText((sp.closePort() ? "closed port " : "failed to close port" ), Color.black);
	        		}
			 	});
		
		var selectSettingButton = new JButton("choose settings file");
			selectSettingButton.addActionListener(e -> {
					onSelectSettingFileClick();
				});
		
		var refreshSettingButton = new JButton("rebase settings from port");
			selectSettingButton.addActionListener(e -> {
					settingsParser.settings.rebase(sp);
					viewer.updateAll();
				});
		
			applySettingButton.setToolTipText("port connection may be briefly interrupted when applying");
			applySettingButton.addActionListener(e -> {
//					if(jcb_toggleport.isSelected())	jcb_toggleport.setSelected(false);
					settingsParser.settings.applyAll(sp);
					if(sp.isOpen()) sp.openPort(); //resets port parameters
					setNoticeText("settings applied " + System.currentTimeMillis());
				});
		
		var saveSettingButton = new JButton("save settings to file");
			saveSettingButton.setToolTipText("everything not saved will be lost");
			saveSettingButton.addActionListener(e -> {
					onSaveSettingsClick(true);
				});
		
		var randBckGndColorBtn = new JButton("change color");
			randBckGndColorBtn.setToolTipText("revolutionary piece of technologeyeyeyey");
			randBckGndColorBtn.addActionListener(e -> setBackground(MainWin.randColor()));
			
		tab_cont.add(jcb_toggleport);
		tab_cont.add(saveSettingButton);
		tab_cont.add(selectSettingButton);
		tab_cont.add(refreshSettingButton);
		tab_cont.add(applySettingButton);
		tab_cont.add(randBckGndColorBtn);
	}
	
	private void genUI_tab_input(JPanel content) {
		//everything gets wrapped in a scroll frame
		
		var toolbarWrapper = new JPanel(new BorderLayout());
			toolbarWrapper.add(inputEditor.geteditorToolBar());
		var eWrapper =	new JScrollPane(inputEditor,	//left
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
		var editorWrapper = new JPanel(new BorderLayout());
			editorWrapper.add(toolbarWrapper, BorderLayout.LINE_START);
			editorWrapper.add(eWrapper, BorderLayout.CENTER);
			
			inputEditor.editorToolBar.setBackground(Color.LIGHT_GRAY);
			inputEditor.editorToolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, Color.black));
			
		var editorAndInspector = new JSplitPane(SwingConstants.VERTICAL,
				editorWrapper,
				new JScrollPane(inputEditor.getInspectorPanel(),	//right	
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
			);
			editorAndInspector.setResizeWeight(.8);
			
		content.setLayout(new GridBagLayout());
		content.add(editorAndInspector, Presentable.createGbc(0,0));
	}
	
	private Path getDefaultSettingsPath() {
		return Presentable.makeRoot(SerialPoke.class, Path.of(title + "." + SPCSettings.FileExtension_Settings));
	}
	
	private static final long serialVersionUID = -8147828337214712162L;
}
