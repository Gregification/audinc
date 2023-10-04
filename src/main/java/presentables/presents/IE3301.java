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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
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
	public volatile float
		sampleRate	= 44100,
		frameRate	= 44100,
		ampRef	= 1.228f;
	public boolean 
		audioFormat_bigEndian 	= false,
		audioFormat_custom		= false;
	public volatile boolean
		thread_stopTableRefresh = false;
	public AudioFormat.Encoding audioFormat_encoding = AudioFormat.Encoding.PCM_SIGNED;
	private TargetDataLine targetLine;
	private Thread 
		audio_thread_recording 	= null,
		audio_thread_analysis 	= null,
		thread_tableRefresh		= null;
	private final AtomicBoolean saveAudio = new AtomicBoolean(true);
	public ArrayList<Line.Info> detectedLines = new ArrayList<>();
	public volatile ArrayList<ArrayList<AudioRecord>> rawSamplePoints = new ArrayList<>();
	
	public final int
		PART1_TABLE_COLUMN_VALUE 	= 0,
		PART1_TABLE_COLUMN_dB		= 1,
		PART1_TABLE_COLUMN_TIME		= 2,
		PART1_TABLE_COLUMN_CHANNEL	= 3;	
	public Class[] rowClasses = new Class[4]; //effectively final
	
	private Line.Info selectedLine = null;
	private boolean logginEnabled = true;
	private JLabel noticeDisplay = new JLabel("");
	
	@Override protected void start()	{
        
	}
	
	public Object[] arrangeIntoRow(AudioRecord ar, int channel) {
		var arr = new Object[4];
			arr[PART1_TABLE_COLUMN_VALUE] 	= ar.value; 
			arr[PART1_TABLE_COLUMN_dB] 		= AudioRecord.evaluatedB(ar.value, ampRef);		
			arr[PART1_TABLE_COLUMN_TIME] 	= ar.time;		
			arr[PART1_TABLE_COLUMN_CHANNEL] = channel;	
		return arr;
	}
	
	@Override protected void init(MainWin mw) 	{
		rowClasses[PART1_TABLE_COLUMN_VALUE] 	= Float.class; 
		rowClasses[PART1_TABLE_COLUMN_dB] 		= Float.class;		
		rowClasses[PART1_TABLE_COLUMN_TIME] 	= Long.class;		
		rowClasses[PART1_TABLE_COLUMN_CHANNEL] 	= Integer.class;
		
		this.checkDataLines();
		
		this.savePath = Presentable.makeRoot(this.getClass());
		this.setLoggingTo(savePath);
		initGUI(mw);
		
		this.setChannelCount(channels);
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
			JButton tb_refreshbtn = new JButton(MainWin.getImageIcon("res/refresh.png", MainWin.stdtabIconSize));
				tb_refreshbtn.setBorder(emptyBorder);
				tb_refreshbtn.setToolTipText("refresh table to local data points");
				tb_refreshbtn.addActionListener(e -> refreshTable());
			JButton tb_deleteBtn = new JButton(MainWin.getImageIcon("res/trashCan.png", MainWin.stdtabIconSize));
				tb_deleteBtn.addActionListener(event -> onDeleteClick());
				tb_deleteBtn.setBorder(emptyBorder);
				tb_deleteBtn.setToolTipText("delete selected rows of the table. (non recoverable)");
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
			toolbar.add(tb_refreshbtn);
			toolbar.add(Box.createHorizontalGlue());
			toolbar.add(tb_importAudioBtn);
			toolbar.add(tb_saveLogBtn);
			toolbar.add(Box.createHorizontalStrut(MainWin.stdStructSpace));
			toolbar.add(tb_deleteBtn);
		content.add(toolbar, BorderLayout.PAGE_START);
		
		//log table
		String[] columns = new String[4];
				columns[PART1_TABLE_COLUMN_VALUE] 	= "val";
				columns[PART1_TABLE_COLUMN_dB] 		= "dB";
				columns[PART1_TABLE_COLUMN_TIME] 	= "time(mill-epoch)";
				columns[PART1_TABLE_COLUMN_CHANNEL] = "channel";
		p1dtModle = new DefaultTableModel(
				new Object[][] {null}, //data
				columns) 
				{
		            @SuppressWarnings({ "unchecked", "rawtypes" })
					@Override public Class getColumnClass(int column) {
		                switch (column) {
		                    case PART1_TABLE_COLUMN_VALUE:
		                        return rowClasses[column];
		                    case PART1_TABLE_COLUMN_dB:
		                        return rowClasses[column];
		                    case PART1_TABLE_COLUMN_TIME:
		                        return rowClasses[column];
		                    case PART1_TABLE_COLUMN_CHANNEL:
		                        return rowClasses[column];
		                    default:
		                    	System.out.println("ie3301 / genUI_tab_part1 / missing a columns somewhow");
		                        return String.class;
		                }
		            }
		        };
		part1DataTable = new JTable(p1dtModle) {
					private static final long serialVersionUID = 1L;//eclipse complains
					public boolean isCellEditable(int row, int column) { return true; };
				};
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
			rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		part1DataTable.setDefaultRenderer(Object.class, rightRenderer);
		
		part1DataTable.setAutoCreateRowSorter(true);
		
		JScrollPane table_scroll = new JScrollPane(this.part1DataTable,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		content.add(table_scroll, BorderLayout.CENTER);
	
		host_tabb.addTab("part 1", null, content, "data set1 generation");
	}

	public void setChannelCount(int n) {
		synchronized(this.rawSamplePoints) {
			int l = rawSamplePoints.size();
			
			if(n > l) {
				for(int i = l; i < n; i++)
					this.rawSamplePoints.add(new ArrayList<AudioRecord>());
			}else {
				for(int i = n; i+1 < l; i++)
					this.rawSamplePoints.remove(l);
			}
			
			this.rawSamplePoints.trimToSize();
//			System.out.println("sample array count:" + this.rawSamplePoints.size() + " / " + n);
		}
	}
	
	public boolean isValidChannelCount(int n) {
		return !(n <= 0 || n > 65535);
	}
	
	
	public void refreshTable() {		
		setNoticeText("refreshing table ...", Color.yellow);
		
		p1dtModle.setRowCount(0);
		
		thread_stopTableRefresh = false;
		if(thread_tableRefresh != null && thread_tableRefresh.isAlive()) {
			thread_stopTableRefresh = true;
			try {
				thread_tableRefresh.join();
			} catch (InterruptedException e1) { e1.printStackTrace(); }
		}
		
		thread_tableRefresh = new Thread(() -> {
//				System.out.println("refresh table > start");
				synchronized(this.rawSamplePoints) {
					for(int channel = 0, totalChannels = this.rawSamplePoints.size(); channel < totalChannels; channel++) {
						for(AudioRecord ar : this.rawSamplePoints.get(channel)) {
							if(thread_stopTableRefresh) {
//								System.out.println("refresh table > stopped early");
								return;
							}
							
//							System.out.println("refresh table > add row");
							
							p1dtModle.addRow(arrangeIntoRow(ar, channel));
						}
					}
//					System.out.println("refresh table > thread ended");
				}
			});
		thread_tableRefresh.start();
		
		setNoticeText("table has been refreshed", Color.black);
	}
	
	public void addSamplePoint(int channel, AudioRecord ar) {
		synchronized(this.rawSamplePoints) {			
			this.rawSamplePoints.get(channel).add(ar);
				
			if(this.isLoggingEnabled())
				p1dtModle.addRow(arrangeIntoRow(ar, channel));
		}
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
				Presentable.genLabelInput("refrence amp (float): ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(5);
						tf.setText(ampRef+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();try {
								if(s.isBlank() || Float.parseFloat(s) == 0) {
									setNoticeText("invalid", Color.red);
									return false;
								}}catch(NumberFormatException e ) {
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
											"invalid refrence amp : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								ampRef = Float.parseFloat(tf.getText());
								
								refreshTable();
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
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								
								if(s.isBlank() || !isValidChannelCount(Integer.parseInt(s))) {
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
											"invalid channel count : " + tf.getText(),
											"unable to set value",
											JOptionPane.OK_OPTION);
									return;
								}
								
								setChannelCount(Integer.parseInt(tf.getText()));
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
		
		
		//file validatation
		if(!file.exists())
			this.setLoggingTo(savePath);
		
		if(file.isFile()) {
			switch(JOptionPane.showConfirmDialog(null, "file already exists, do you want to override? " + path.toAbsolutePath().toString())) {
				case JFileChooser.APPROVE_OPTION : 
					break;
					
				default: return;
			}
		} else if(file.isDirectory()) {
			path = path.resolve((System.nanoTime()/1000000) + ".csv");
		} else if(!path.endsWith(".csv")) {
			String src = path.toAbsolutePath().toString();
			if(src.endsWith(".wav"))
				src = src.substring(0, src.length() - (".wav").length());
			
			path = Path.of(src + ".csv");
		}
		
		String message = "saving csv to:" + path.toAbsolutePath().toString();
		
		
		//user selects desired columns
		int[] columnsToLog;
		{
			ArrayList<Integer> ctl = new ArrayList<>(rowClasses.length);
			JPanel columnsContainer = new JPanel();
			
			for(int i = 0, l = this.p1dtModle.getColumnCount(); i < l; i++) {
				String name = this.p1dtModle.getColumnName(i);
				int idx = i;
				
				JCheckBox toggler =	new JCheckBox(name, this.isLoggingEnabled());
					toggler.addItemListener(il -> {
				    		if(il.getStateChange() == ItemEvent.SELECTED) {
				    			ctl.add(idx);
				    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
				    			ctl.remove(Integer.valueOf(idx));
				    		}
				    	});
					
				columnsContainer.add(toggler);
			}
			
			switch(JOptionPane.showConfirmDialog(null, columnsContainer, "logging", JOptionPane.OK_CANCEL_OPTION)) {
				case JOptionPane.CANCEL_OPTION:
					setNoticeText("cancled saving table");
					return;
			}
			
			columnsToLog = new int[ctl.size()];
			for(int i = 0, l = ctl.size(); i < l; i++)
				columnsToLog[i] = ctl.get(i);
		}
		
		
		
		//loading UI
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
		
		
		Path pas = path;
		Thread loader = new Thread() {
 			@Override public void run() {
 				int 
 					rowCount 	= part1DataTable.getRowCount(),
 					columnCount = part1DataTable.getColumnCount();
 				
 				StringBuilder sb 	= new StringBuilder();
 				String newLine 		= System.getProperty("line.separator");
 				
 				writeToPath(pas, bw -> {
 						Object obj;
 						try {
	 						for(int iR = 0, iC = 0; iR < rowCount; iR++) {
	 							for(iC = 0; iC < columnsToLog.length; iC++) {
	 								obj = part1DataTable.getValueAt(iR, columnsToLog[iC]);
	 								
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
//		System.out.println("on delete click");
		var model = (DefaultTableModel)(this.p1dtModle);
		int[] rows = part1DataTable.getSelectedRows();
		
		if(rows.length == 0 || rows.length == part1DataTable.getRowCount()) {
			rawSamplePoints.clear();
			setChannelCount(channels);
			model.setRowCount(0);
		}else {
			for(int r : rows){
				int channel = (int) model.getValueAt(r, 3);
				float errorRate = .01f;
				
				Predicate<? super AudioRecord> filter;
					switch(part1DataTable.getSelectedColumn()) {
						case PART1_TABLE_COLUMN_CHANNEL:
	                    	rawSamplePoints.get(channel).clear();
	                    	return;
	                    	
						case PART1_TABLE_COLUMN_VALUE:{
	                    	float value = (float)part1DataTable.getValueAt(r, PART1_TABLE_COLUMN_dB);
	                    	filter = ar -> ar.value == value;
	                    	}break;
	                    	
	                    case PART1_TABLE_COLUMN_dB:{
	                    	float db = (float)part1DataTable.getValueAt(r, PART1_TABLE_COLUMN_dB);
	                    	filter = ar -> Math.abs(AudioRecord.evaluatedB(ar.value, ampRef) - db) < errorRate;
	                    	}break;
	                    	
	                    case PART1_TABLE_COLUMN_TIME:{
	                    	long time = (long)part1DataTable.getValueAt(r, PART1_TABLE_COLUMN_TIME);
	                    	filter = ar -> ar.time == time;
	                    	}break;
	                    	
	                    default:
	                    	filter = ar -> true;
	                    	break;
					}
					
				rawSamplePoints.get(channel).remove(filter);
			}
		}
		
		this.refreshTable();
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
//		System.out.println("start recording");
		if(isRecording()) {
			stopRecording();
		}
		
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
							System.out.println("ie3301 / start recording / recording thread setup failed");
						}
		 			}};
	 		}
	 		
	 		if(this.isLoggingEnabled()) {	 			
		 		audio_thread_analysis = new Thread() {
					@Override public void run() {
						try {
							analizeLiveWav(targetLine);
						} catch (IOException e) { e.printStackTrace(); }		 				
		 			}};
	 		}
	 		
 			if(this.saveAudio.get()) {
		 		audio_thread_recording.start();
 			}
 			
 			
 			audio_thread_analysis.start();
 			
// 			System.out.println();
 			
	 	}catch (Exception e) {
	 		System.out.print("ie3301 / startRecording errored");
	 		this.setNoticeText(e.toString(), Color.red);
	 	}
		
//		System.out.println("start recording ended");
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
	private void analizeLiveWav(TargetDataLine dataLine) throws IOException{
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
		
		if(this.rawSamplePoints.size() != nCh)
			this.setChannelCount(nCh);
		
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
		
		
			
		//main loop
		long
			e6		= 1000000,
			start 	= 0,
			last	= System.nanoTime() /e6,
			elapsed = 0,
			sleepIntervals 	= 2,
			sleepStep 		= 0;
		
		while((read = dataLine.read(buffer, 0, buffer.length)) != 0) {
			//parsing
			for(iF = 0, i = 0; iF < nF; iF++) {	//per frame
				for(iCh = 0; iCh < nCh; iCh++) {				//per channel
					for(iByte = 0; iByte < sCh_byte; iByte++) {		//get byte
						bb.put(buffer[i]);
						
						i++;
						if(i == read) break;
					}
					
					short channelValue = bb.getShort(0);
						bb.rewind();
					
					chMeans[iCh] += channelValue;						
				}
			}
			
			
			//analysis			
			for(i = 0; i < chMeans.length; i++) {
				chMeans[i] /= chMeans.length;
				
				addSamplePoint(
						i,
						new AudioRecord((float)chMeans[i],last)
					);
				
				chMeans[i] = 0;
			}
			
			//timing
			start = System.nanoTime() / e6;		//n
			elapsed = (start - last); 		//n
			last = start;					//n
			if(elapsed < logInterval_mill) {
				try {
					sleepStep = (long)((logInterval_mill-elapsed) / sleepIntervals) + 1;
					
					for(i = 0; i < sleepIntervals; i++) {
						Thread.sleep(sleepStep);
						if(!dataLine.isActive()) break;
					}

					last = System.nanoTime() / e6;
				} catch (InterruptedException e) { System.out.println("analize wav (data line). timing"); e.printStackTrace(); }
			}
		}
		
		dataLine.close();
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

class AudioRecord{
	public final float 	value;
	public final long 	time;
	
	public AudioRecord(float value, long time) {
		this.value 	= value;
		this.time	= time;
	}
	
	public static float evaluatedB(float value, float refrenceAmp) {
		float ratio = value / refrenceAmp;
		return  20 * (float)(Math.log10(Math.abs(ratio)) * Math.signum(ratio));
	}
}