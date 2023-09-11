package presentables.presents;

/*
 * this thing is NOT thread safe but its patched together just well enough it works
 * 
 * sauce 
 * 		generic java uses 	: http://www.labbookpages.co.uk/audio/javaWavFiles.html
 * 		general info 		: https://en.wikipedia.org/wiki/WAV#:~:text=Waveform%20Audio%20File%20Format%20(WAVE,Windows%20systems%20for%20uncompressed%20audio.
 * 		wave format			: http://soundfile.sapp.org/doc/WaveFormat/
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JDialog;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.fazecast.jSerialComm.SerialPort;

import audinc.gui.MainWin;
import presentables.Presentable;
import presentables.custom_function;

public class IE3301 extends Presentable{
	public 	volatile JTable part1DataTable;
	private volatile DefaultTableModel p1dtModle;
	public 	volatile Path savePath;
	
	//audio
	public volatile int 
		logInterval_mill 	= 5000,
		logLength_mill 		= 1000,
		channelSize_byte	= 2,
		channels 		= 2,
		frameSize		= 4;
	public float
		sampleRate	= 44100,
		frameRate	= 44100;
	public boolean 
		audioFormat_bigEndian 	= false,
		audioFormat_custom		= false;
	public AudioFormat.Encoding audioFormat_encoding = AudioFormat.Encoding.PCM_SIGNED;
	private TargetDataLine targetLine;
	private Thread 
		audio_thread_recording 	= null,
		audio_thread_analysis 	= null;
	private final AtomicBoolean saveAudio = new AtomicBoolean(true);
	public ArrayList<Line.Info> detectedLines = new ArrayList<>();
	
	private Line.Info selectedLine = null;
	private boolean logginEnabled = true;
	private JLabel noticeDisplay = new JLabel("");
	
	@Override protected void start()	{
        
	}
	
	@Override protected void init(MainWin mw) 	{
		this.checkDataLines();
		
		this.savePath = Presentable.makeRoot(this.getClass());
		this.setLoggingTo(savePath);
		initGUI(mw);
		
		this.onDeleteClick();
	}	
	@Override protected void initGUI(MainWin mw){
		JPanel container = new JPanel(new BorderLayout()); // add notice later
			container.add(noticeDisplay, BorderLayout.PAGE_END);
			
			JTabbedPane mainTp = new JTabbedPane();
				
				genUI_tab_part1(mainTp);
				
			container.add(mainTp);
		mw.add(container);
	}
	
	@Override public void quit() {
		//stops & cleans up streams and threads
		this.stopRecording();
	}
	
	@SuppressWarnings("serial")
	public void genUI_tab_part1(JTabbedPane host_tabb) {
		JPanel content = new JPanel(new BorderLayout());
		
		var emptyBorder = BorderFactory.createEmptyBorder();
		
		//tool bar
		JToolBar toolbar = new JToolBar();
			JButton tb_logbtn = new JButton(MainWin.getImageIcon("res/log.png", MainWin.stdtabIconSize));
				tb_logbtn.setBorder(emptyBorder);
				tb_logbtn.setToolTipText("log settings");
				tb_logbtn.addActionListener(e -> openSettingsDialoug());
			JButton tb_startbtn = new JButton(MainWin.getImageIcon("res/dot_lime.png", MainWin.stdtabIconSize));
				tb_startbtn.setBorder(emptyBorder);
				tb_startbtn.setToolTipText("play/stop button");
				tb_startbtn.addActionListener(e -> {
						onPlayBtnClick();
						
						tb_startbtn.setIcon(MainWin.getImageIcon("res/"+ (isRecording() ? "dot_red": "playbtn")+".png", MainWin.stdtabIconSize));
					});
					tb_startbtn.setIcon(MainWin.getImageIcon("res/"+ (isRecording() ? "dot_red": "playbtn")+".png", MainWin.stdtabIconSize));
			JButton tb_deleteBtn = new JButton(MainWin.getImageIcon("res/trashCan.png", MainWin.stdtabIconSize));
				tb_deleteBtn.addActionListener(event -> onDeleteClick());
				tb_deleteBtn.setBorder(emptyBorder);
				tb_deleteBtn.setToolTipText("clear table contents. (non recoverable and will not save)");
			JButton tb_importAudioBtn = new JButton(MainWin.getImageIcon("res/import.png", MainWin.stdtabIconSize));
				tb_importAudioBtn.addActionListener(event -> this.onImportAudioClick());
				tb_importAudioBtn.setBorder(emptyBorder);
				tb_importAudioBtn.setToolTipText("import a wav file to analyze. (will first clear table)");
			JButton tb_saveLogBtn = new JButton(MainWin.getImageIcon("res/save.png", MainWin.stdtabIconSize));
				tb_saveLogBtn.addActionListener(event -> onSaveLogClick());
				tb_saveLogBtn.setBorder(emptyBorder);
				tb_saveLogBtn.setToolTipText("save the tables contents to the log path (overwrite)");
				
			toolbar.add(tb_logbtn);
			toolbar.add(tb_startbtn);
			toolbar.add(Box.createHorizontalGlue());
			toolbar.add(tb_importAudioBtn);
			toolbar.add(tb_saveLogBtn);
			toolbar.add(Box.createHorizontalStrut(MainWin.stdStructSpace));
			toolbar.add(tb_deleteBtn);
		content.add(toolbar, BorderLayout.PAGE_START);
		
		//log table
		p1dtModle = new DefaultTableModel(
				new Object[][] {null}, //data
				new String[] {"val", "time(mill-epoch)", "channel#"}) //columns 
				{
		            @SuppressWarnings({ "unchecked", "rawtypes" })
					@Override public Class getColumnClass(int column) {
		                switch (column) {
		                    case 0:
		                        return Long.class;
		                    case 1:
		                        return Long.class;
		                    case 2:
		                        return Integer.class;
		                    default:
		                        return String.class;
		                }
		            }
		        };
		part1DataTable = new JTable(p1dtModle) {
					private static final long serialVersionUID = 1L;//eclipse complains
					public boolean isCellEditable(int row, int column) { return true; };
				};
			part1DataTable.setAutoCreateRowSorter(true);
		JScrollPane table_scroll = new JScrollPane(this.part1DataTable,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		content.add(table_scroll, BorderLayout.CENTER);
	
		host_tabb.addTab("part 1", null, content, "data set1 generation");
	}
	
///////////////////
//gui events
///////////////////
	public void openSettingsDialoug() {	 
		
		JFrame cframe= new JFrame();
		
		//logging file picker
		JCheckBox logToggler =	new JCheckBox("logging enabled", this.isLoggingEnabled());
			logToggler.addItemListener(il -> {
		    		if(il.getStateChange() == ItemEvent.SELECTED) {
		    			this.setLoggingEnabled(true);
		    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
		    			this.setLoggingEnabled(false);
		    		}
		    	});
		
		//log to table toggler
		JCheckBox logToTableToggler =	new JCheckBox("save analysis", this.isLoggingEnabled());
			logToggler.addItemListener(il -> {
		    		if(il.getStateChange() == ItemEvent.SELECTED) {
		    			this.setLoggingEnabled(true);
		    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
		    			this.setLoggingEnabled(false);
		    		}
		    	});
			
			
		//big endian toggler
	 	JCheckBox bigEndianToggler =	new JCheckBox("big endian", this.audioFormat_bigEndian);
			bigEndianToggler.addItemListener(il -> {
		    		if(il.getStateChange() == ItemEvent.SELECTED) {
		    			this.audioFormat_bigEndian = true;
		    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
		    			this.audioFormat_bigEndian = false;
		    		}
		    	});
		
		//save audio file toggler
		JCheckBox saveAudioFileToggler =	new JCheckBox("save audio file", this.saveAudio.get());
			saveAudioFileToggler.addItemListener(il -> {
		    		if(il.getStateChange() == ItemEvent.SELECTED) {
		    			this.saveAudio.set(true);
		    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
		    			this.saveAudio.set(false);
		    		}
		    	});
			saveAudioFileToggler.setToolTipText(".wav . will save to the same location as logs even if logs are not enableds");
		
		//custom data line toggler
		JCheckBox useCustomLineToggler =	new JCheckBox("custom format", this.audioFormat_custom);
			useCustomLineToggler.addItemListener(il -> {
		    		if(il.getStateChange() == ItemEvent.SELECTED) {
		    			this.audioFormat_custom = true;
		    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
		    			this.audioFormat_custom = false;
		    		}
		    	});
			useCustomLineToggler.setToolTipText("selecting this additionaly applies these ontop of everything else ->"
					+ " frameSize"
					+ " & frameRate");
			
		JPanel saveFilePicker = Presentable.genFilePicker(
				this.getClass(),
				this.savePath,
				logToggler,
				new custom_function<JFileChooser>() {
					@Override public JFileChooser doTheThing(JFileChooser fc) {
						switch(fc.showOpenDialog(cframe)) {
							case JFileChooser.APPROVE_OPTION : 
		    					Path newpath = fc.getSelectedFile().toPath();
		    					setLoggingTo(newpath);
	    					break;
						}
						
						return null;
					}}
				);
			saveFilePicker.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "log to", TitledBorder.LEFT, TitledBorder.TOP));
			
		JLabel data_format_img = new JLabel(MainWin.getImageIcon("res/ie3301/wav_data_format.png"));
			
		//general settings
		JPanel audioEncoding = new JPanel();
			audioEncoding.setBorder(BorderFactory.createTitledBorder(BorderFactory.createDashedBorder(null), "audio encoding", TitledBorder.LEFT, TitledBorder.TOP));
			{
				AudioFormat.Encoding[] audioEncodings = new AudioFormat.Encoding[] {
						AudioFormat.Encoding.PCM_SIGNED,
						AudioFormat.Encoding.PCM_UNSIGNED,
						AudioFormat.Encoding.PCM_FLOAT,
						AudioFormat.Encoding.ULAW
					};
				
				ButtonGroup bg = new ButtonGroup();
				
				for(int i = 0; i <  audioEncodings.length; i++) {
					JRadioButton jrb = new JRadioButton(audioEncodings[i].toString());
						jrb.setActionCommand(i+"");
						jrb.addActionListener(e -> {
								this.audioFormat_encoding = audioEncodings[Integer.parseInt(e.getActionCommand())];
							});
						
					//no deep check since they should both be pointing to the same static object 
					if(audioEncodings[i] == this.audioFormat_encoding) jrb.setSelected(true);
					
					bg.add(jrb);
					audioEncoding.add(jrb);
				}
			}
			
		//package components
		ArrayList<DocumentListener> docListeners = new ArrayList<>(5);	
		
		JComponent[] objs = {
				saveFilePicker,
				saveAudioFileToggler,
				Presentable.genLabelInput("log interval (mil)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(5);
						tf.setText(logInterval_mill+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								int v;
								if(s.isBlank() || (v = Integer.parseInt(s)) <= 0 || v > 100000) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.green);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {								
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid log interval : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								logInterval_mill = Integer.parseInt(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				Presentable.genLabelInput("log length (mil)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(5);
						tf.setText(logLength_mill+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								int v;
								if(s.isBlank() || (v = Integer.parseInt(s)) <= 0 || v > logInterval_mill) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.BLUE);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {								
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid log length : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								logLength_mill = Integer.parseInt(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				audioEncoding,
				Presentable.genLabelInput((JComponent)useCustomLineToggler, new custom_function<JComponent>() {
					@Override public JComponent doTheThing(JComponent o) {
						var v = new JComboBox(detectedLines.toArray());
							v.setSelectedIndex(0);
							v.addActionListener(e -> {
								selectedLine = (Line.Info)v.getSelectedItem();
							});
							v.setToolTipText("honstley this selector dosnet do anything. the check box does tho");
						return v;
					}}),
				Presentable.genLabelInput("channel size 	(bytes)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(5);
						tf.setText(channelSize_byte+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								int v;
								if(s.isBlank() || (v = Integer.parseInt(s)) <=0 || v > 8) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.green);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {								
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid sample bit count (bytes): " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								channelSize_byte = Integer.parseInt(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				Presentable.genLabelInput("channels 	(int)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(5);
						tf.setText(channels+"");
						
						int maxChannels = 65535;
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								int v;
								if(s.isBlank() || (v = Integer.parseInt(s)) <= 0 || v > maxChannels) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.green);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {								
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid channel count (0-"+maxChannels+"): " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								channels = Integer.parseInt(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				Presentable.genLabelInput("frame size  (float)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(10);
						tf.setText(frameSize+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								
								if(s.isBlank() || Integer.parseInt(s) <= 0) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.green);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid frame size : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								frameSize = Integer.parseInt(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				Presentable.genLabelInput("sample rate (float-Hz)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(10);
						tf.setText(sampleRate+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								
								if(s.isBlank() || Float.parseFloat(s) <= 0) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.green);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid sample rate : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								sampleRate = Float.parseFloat(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				Presentable.genLabelInput("frame rate (float-Hz): ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(10);
						tf.setText(frameRate+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								
								if(s.isBlank() || Float.parseFloat(s) <= 0) {
									setNoticeText("invalid", Color.red);
									return false;
								}
								
								setNoticeText(":D", Color.green);
								return true;
							}};
						var v = new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) { isValid.doTheThing(true); } 
							@Override public void removeUpdate(DocumentEvent e)	{ isValid.doTheThing(true); }
							@Override public void changedUpdate(DocumentEvent e) {
								if(!isValid.doTheThing(false)) {
									JOptionPane.showInternalMessageDialog(
											null,
											"invalid frame rate : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								frameRate = Float.parseFloat(tf.getText());
							}
						};
						
						docListeners.add(v);
						tf.getDocument().addDocumentListener(v);		
						return tf;
					}}),
				data_format_img,
				bigEndianToggler
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
        
        this.setNoticeText("changes in settings will not go into effect untill restart");
        
        switch (JOptionPane.showConfirmDialog(cframe, wrapper_scroll, "logging", JOptionPane.OK_CANCEL_OPTION)) {
        	case JOptionPane.YES_OPTION:
        			setNoticeText("saving changes ...", Color.black);
        			
        			for(var dl : docListeners)
        				dl.changedUpdate(null);
        			
        			setNoticeText("changes saved!", Color.black);
        		break;
        		
        	case JOptionPane.NO_OPTION:
        	default:
        			setNoticeText("changes cancled.", Color.black);
        		break;
        }
	}
	
	public void onSaveLogClick() {
		var file = savePath.toFile();
		Path path = file.toPath();
		
		if(!file.exists())
			this.setLoggingTo(savePath);
		
		if(file.isFile()) {
			switch(JOptionPane.showConfirmDialog(null, "file already exists, do you want to override? " + path.toAbsolutePath().toString())) {
				case JFileChooser.APPROVE_OPTION : 
					break;
					
				default: return;
			}
		}
		
		if(file.isDirectory()) {
			path = savePath.resolve((int)(System.nanoTime()/1000000) + ".wav");
		}
		
		String message = "saving csv to:" + path.toAbsolutePath().toString();
		
		JLabel container = new JLabel(message);
			JProgressBar pb = new JProgressBar(SwingConstants.HORIZONTAL);
				pb.setSize(new Dimension((int)(MainWin.DimensionScale_window * 4/5), (int)(MainWin.stdStructSpace * 2)));
		container.add(pb);
		JOptionPane optionPane = new JOptionPane(container, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			
		JDialog dialog = new JDialog();
			dialog.setTitle("saving to csv ...");
			dialog.setModal(true);
			dialog.setContentPane(container);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.pack();
		
		if(path.toFile().isDirectory()) {
			path = path.resolve((System.nanoTime()/1000000) + ".csv");
		} else if(!path.endsWith(".csv")) {
			System.out.println("correcting to .csv file");
			String src = path.toAbsolutePath().toString();
			if(src.endsWith(".wav"))
				src = src.substring(0, src.length() - (".wav").length());
			
			path = Path.of(src + ".csv");
		}
		
		Path pas = path;
		System.out.println(pas.toAbsolutePath().toString());
		
		Thread loader = new Thread() {
 			@Override public void run() {
 				System.out.println("path:"+ pas.toString());
 				int 
 					rowCount 	= part1DataTable.getRowCount(),
 					columnCount = part1DataTable.getColumnCount();
 				
 				StringBuilder sb 	= new StringBuilder();
 				String newLine 		= System.getProperty("line.separator");
 				
 				writeToPath(pas, bw -> {
 						Object obj;
 						try {
	 						for(int iR = 0, iC = 0; iR < rowCount; iR++) {
	 							for(iC = 0; iC < columnCount; iC++) {
	 								obj = part1DataTable.getValueAt(iR, iC);
	 								
	 								sb.append(obj != null ? obj.toString() : ' ');
	 								
	 								sb.append(',');
	 							}
	 							
	 							//trim off trailing comma
	 							sb.setLength(sb.length()-1);
	 							
	 							sb.append(newLine);
	 							
								bw.write(sb.toString());
								
	 	 						sb.setLength(0);
	 	 						
	 	 						if(iR % 2 == 0)
	 	 							pb.setValue(iR / rowCount * 100);
	 						}
	 						bw.flush();
 						} catch (IOException e) { e.printStackTrace(); }
 					});
 				
 				dialog.dispose();
			}};
		
		setNoticeText("saving csv to:" + path.toAbsolutePath().toString(), Color.black);
		
		loader.start();
		dialog.setVisible(true);
	}
	
	public void onImportAudioClick() {
		
	}
	
	public void onDeleteClick() {
		var v = (DefaultTableModel)(part1DataTable.getModel());
		v.setRowCount(0);
	}
	
	public boolean isLoggingEnabled() {
		return this.logginEnabled;
	}
	public void setLoggingEnabled(boolean to) {
		if(to == this.isLoggingEnabled()) return;
		
		if(to) {
			setLoggingTo(this.savePath);
		}
		
		this.logginEnabled = to;
	}
	public void setLoggingTo(Path saveToPath) {
		savePath = Path.of(saveToPath.toAbsolutePath().toUri());//clone object
		
		File file = savePath.toAbsolutePath().toFile();
		
		if(!file.exists()) {
			try {
				Files.createDirectories(savePath);
				
				if(file.isDirectory()) {
					String timeStamp = new SimpleDateFormat("MMddyyyy_HHmmss").format(Calendar.getInstance().getTime());
					savePath = savePath.resolve(timeStamp + ".csv");
				}
				
				Files.createFile(savePath);
			} catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	public void checkDataLines() {
		detectedLines.clear();
		
		for (Mixer.Info i : AudioSystem.getMixerInfo()) {
            Line.Info[] tli = AudioSystem.getMixer(i).getTargetLineInfo();
            if (tli.length != 0) {
                for (int f = 0; f < tli.length; f += 1) {
                    detectedLines.add(tli[f]);
                }
            }
        }
		
		selectedLine = detectedLines.get(0);
	}
	
	public synchronized void setNoticeText(String text) {
		this.noticeDisplay.setText(text);
	}
	public synchronized void setNoticeText(String text, Color color) {
		this.noticeDisplay.setForeground(color);
		setNoticeText(text);
	}
	
	public void onPlayBtnClick() {
		if(isRecording())
			stopRecording();
		else
			startRecording();
	}
	
	public void startRecording() {
		if(isRecording()) {
			stopRecording();
		}
		
		System.out.println("starting recording");
//		System.out.println("starting recording. "
//				+ "\n\rsaveaudio:\t" + this.saveAudio + "");
		
		try {
			AudioFormat audioFormat = this.getAudioFormat();
	 		
	 		DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
	 		if(!AudioSystem.isLineSupported(dataInfo)) {
	 			String message = "the audio system does not support a dataline by the given specs";
	 			this.setNoticeText(message, Color.red);
		 		JOptionPane.showConfirmDialog(null, message,  "fatal", JOptionPane.PLAIN_MESSAGE);
	 		}
	 		

	 		targetLine = (TargetDataLine)AudioSystem.getLine(dataInfo);
	 		targetLine.open();
	 		
	 		setNoticeText("starting recording");
	 		targetLine.start();
	 		
	 		AudioInputStream stream = new AudioInputStream(targetLine);
	 		
	 		if(this.saveAudio.get()) {
		 		audio_thread_recording = new Thread() {
		 			@Override public void run() {
		 				File outputFile;
		 				
		 				AudioInputStream recordingStream = new AudioInputStream(targetLine);
		 				Path pth = savePath.resolve((int)(System.nanoTime()/1000000) + ".wav");
		 				outputFile = pth.toFile();
		 				setNoticeText("saving audio to:" + pth.toAbsolutePath().toString(), Color.black);
		 				
		 				try {
							AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, outputFile);
						} catch (IOException e) {
							System.out.println(e);
						}
		 			}};
	 		}
	 		
	 		if(this.isLoggingEnabled()) {	 			
		 		audio_thread_analysis = new Thread() {
					@Override public void run() {
						try {
							analizeWav(targetLine);
						} catch (IOException e) { e.printStackTrace(); }
						
//		 				try(AudioInputStream input = new AudioInputStream(targetLine);) {
//		 					analizeWav(input,getAudioFormat());
//		 				} catch (IOException e) { e.printStackTrace(); }
		 				
		 			}};
	 		}
	 		
	 		System.out.print("\t starting thread : ");
	 		
 			if(this.saveAudio.get()) {
 				System.out.print("\trecording_t");
		 		audio_thread_recording.start();
 			}
 			
 			if(this.isLoggingEnabled()) {
 				System.out.print("\tanalysis_t");
 				audio_thread_analysis.start();
 			}
 			
 			System.out.println();
 			
	 	}catch (Exception e) {
	 		this.setNoticeText(e.toString(), Color.red);
	 		System.out.println(e);
	 	}
	}
	public void stopRecording() {
		this.setNoticeText("stopping recording ... ", Color.black);
		
		try {
			if(targetLine != null)
			synchronized(targetLine) {
				targetLine.stop();
				targetLine.close();
				
				if(audio_thread_analysis != null) {
					setNoticeText("stopping analysis", Color.black);
					audio_thread_analysis.join();
					setNoticeText("analysis stopped");
				}
				
				if(audio_thread_recording != null) {
					setNoticeText("stopping recording", Color.black);
					audio_thread_recording.join();
					setNoticeText("recording stopped");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.setNoticeText("recording stopped", Color.black);
	}
	public boolean isRecording() {
		return targetLine != null && targetLine.isOpen();
	}
	private synchronized AudioFormat getAudioFormat() {
		AudioFormat audioFormat;
		
		int sampleSize_bits = this.channelSize_byte * 8;
		
		if(this.audioFormat_custom) {
			audioFormat = new AudioFormat(
	 				this.audioFormat_encoding,
	 				this.sampleRate,
	 				sampleSize_bits, 
	 				this.channels,
	 				this.frameSize,
	 				this.frameRate,
	 				this.audioFormat_bigEndian);
		} else {
			Line.Info info = selectedLine;//i have no clue how to get data out if this thing. i thought it was a related to DataLine.Info but it's not :(
			
			audioFormat = new AudioFormat(
					this.sampleRate,
	 				sampleSize_bits,
	 				this.channels,
	 				this.audioFormat_encoding == AudioFormat.Encoding.PCM_SIGNED,
	 				this.audioFormat_bigEndian);
		}
		return audioFormat;
	}
	
	/*
	 * works
	 */
	private void analizeWav(TargetDataLine dataLine) throws IOException{
		/*
		 * logging vars
		 * [#frames to analyze] = [frames per second] * [logging length (seconds)] + 1
		 * [frames per second]	= [samples per second] / [#samples in a frame]
		 * [#samples in a frame]= [#channels] 											//since each frame is made up of 1 of each channel & each channel is 1 sample
		 * 
		 * => [#frames to analyze] = [samples per second] * [logging length (mill)]  / ([#channels] 1000) + 1
		 */
		
		//general
		int 
			nCh 	= dataLine.getFormat().getChannels(),				//#channels
			sCh_byte= dataLine.getFormat().getSampleSizeInBits() / 8, 	//channel 	size(bytes). SHOULD ALWAYSE BE 2 until the the parser gets updated to handle different things
			sF_byte = nCh * sCh_byte,									
			nF		= (int)(logLength_mill* sampleRate / (1000 * nCh)) + 1;
		byte[] buffer = new byte[sF_byte * nF];
		
		//main loop
		long
			e6		= 1000000,
			start 	= 0,
			last	= System.nanoTime() /e6,
			elapsed = 0;
		
		//parsing
		int
			i		= 0,
			iF		= 0,
			iCh		= 0,
			iByte	= 0, //i hope not
			read	= 0; //red
		
		double[] chMeans = new double[nCh];
			Arrays.fill(chMeans, 0);
		
		ByteBuffer bb = ByteBuffer.allocate(sCh_byte);
			bb.order(dataLine.getFormat().isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
		
			
		System.out.println(""
				+ "numframes:\t" + nF
				+ "\nnum channels:\t" + nCh
				+ "\nframe   size(byte):\t" + sF_byte
				+ "\nchannel size(byte):\t" + sCh_byte
				+ "\nbuffer  size(byte):\t" + buffer.length);
		if(sCh_byte != 2)
			System.out.println("WARNING: CHANNEL SIZE IS NOT 2 BYTES, will continue as if it was 2");
			
			
		//main loop
		while((read = dataLine.read(buffer, 0, buffer.length)) != 0) {
			System.out.println(String.format(
					"frame:%2d\tread:%d",
					dataLine.getFramePosition(),
					read
				));
			
			//timing
			start = System.nanoTime();		//n
			elapsed = (start - last); 		//n
			last = start;					//n
			
			
			//parsing
			for(iF = 0, i = 0; iF < nF; iF++) {					//per frame
				for(iCh = 0; iCh < nCh; iCh++) {				//per channel
					for(iByte = 0; iByte < sCh_byte; iByte++) {		//get byte
						bb.put(buffer[i]);
						
						i++;
						if(i == read) break;
					}
					
					System.out.println("bb:" + Arrays.toString(bb.array()));
					short channelValue = bb.getShort(0);
						bb.rewind();
					
					chMeans[iCh] += channelValue;						
				}
			}
			
			for(i = 0; i < chMeans.length; i++) {
				chMeans[i] /= chMeans.length;
				
				var value 	= chMeans[i];
				var time	= start / e6;
				var channel = i;
				System.out.println(String.format(
						"\tchannel:%d\t\tmean:%3.1f",
						channel,
						value
					));
				p1dtModle.addRow(new Object[] {value, time, channel});
				
				chMeans[i] = 0;
			}
			
			//timing
			System.out.print("\n\nelapsed:" + elapsed + "\t< interval:"+logInterval_mill+"? ");
			if(elapsed < logInterval_mill) {
				System.out.println("true \t-> snoozing\n");
				try {
					Thread.sleep(logInterval_mill-elapsed);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}else {
				System.out.println("false \t-> losing :(\n");
			}
		}
		System.out.println("new thread thing quitting");
		dataLine.close();
	}
	
	/*
	 * dosen't work even if the buffer-underflow-exception is patched.
	 * - made going off the diagrams shown here http://soundfile.sapp.org/doc/WaveFormat/
	 * 
	 * : update - reason it dosent work is because i screwed up what a frame and sample represents
	 */
	private void analizeWav(InputStream input, AudioFormat format) throws IOException{		
		int
			nChans 			= format.getChannels(),
			nChanSize_byte	= channelSize_byte * 8 * channels,//sampleSize_bytes,
			nSamples 		= 1,//logSamples,
			nSpSize_byte	= nChans * nChanSize_byte,
			bufferSize_byte	= nSamples * nSpSize_byte;
		
		ByteOrder byteOrder	= format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
		
		byte[] 	
			buffer 		= new byte[bufferSize_byte],	//raw input
			spBuffer	= new byte[nChanSize_byte];		//bytes per float (or not... womp womp)
		
		float[][]
			chanBuffer	= new float[nChans][nSamples]; //the actual input data translated from bytes.
		
		//loop vars
		long
			e6		= 1000000,
			start 	= 0,
			last	= System.nanoTime() /e6,
			elapsed = 0;
		
		System.out.println("analize thread:"
				+ "\n\tinput buffer size: \t"	+ bufferSize_byte
				+ "\n\tnum channel: \t" + nChans
				+ "\n\t\tsize: \t"	+ nChanSize_byte
				+ "\n\tnum samples: \t"	+ nSamples 
				+ "\n\t\tsize: \t"	+ nSpSize_byte
				+ "");
		
		//main loop
		while(input.read(buffer) > 0) {
			start = System.nanoTime() /e6;	//u
			elapsed = (start - last); 		//u
			last = start;					//u
			
			System.out.println(start +"\t:\t"+ Arrays.toString(buffer));
			
			//cat input buffer -> channel data
			for(int iSp = 0, iChan = 0, iByte = 0, iTotal = 0; iSp < nSamples; iSp++) { 	//per sample
				
				System.out.println(String.format("sample %%%2.0f : %d\t\t%3d /%3d",(float)iTotal/(float)buffer.length*100,iSp, iTotal, buffer.length));
				
				for(iChan = 0; iChan < nChans; iChan++){							//per channel
					iTotal = iSp*nSpSize_byte + iChan*nChanSize_byte;
					
					for(iByte = 0; iByte < nChanSize_byte; iByte++) { 					//per byte
						spBuffer[iByte] = buffer[iTotal + iByte];
					}
					
					System.out.println("\tchannel:" + iChan + "\tvalue:"+Arrays.toString(spBuffer));
					
					p1dtModle.addRow(new Object[] {chanBuffer[iChan][iSp], start, iChan});
					
//					chanBuffer[iChan][iSp] = ByteBuffer.wrap(spBuffer).order(byteOrder).getInt(); //throws underFlowException
//					p1dtModle.addRow(new Object[] {chanBuffer[iChan][iSp], start, iChan});
				}
			}
			
			System.out.print("\n\nelapsed:" + elapsed + "\t< interval:"+logInterval_mill+"? ");
			if(elapsed < logInterval_mill) {
				System.out.println("true \t-> snoozing\n");
				try {
					Thread.sleep(logInterval_mill-elapsed);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}else {
				System.out.println("false \t-> losing :(\n");
			}
		}
	}

	
///////////////////
//present statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "IE3301";	}
	public static ImageIcon getImgIcon() 	{	return MainWin.getImageIcon("res/aboutbg.png", MainWin.stbPresentIconSize); } //giving a invalid name will default to a blank icon
	public static String getDescription() 	{	return "<html>"
			+ "<body>a quick slap together to generate datasets for IE3301 course > project part 1<br>"
			+ "<ul>"
			+ "<li><a href=\"http://soundfile.sapp.org/doc/WaveFormat/\">wav file refrence</a></li>"
			+ "</ul>"
			+ "</body>"; }
}

