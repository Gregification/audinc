package presentables.presents;

/*
 * this thing is NOT thread safe but its patched together just well enough it works
 * 
 * sauce http://www.labbookpages.co.uk/audio/javaWavFiles.html
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.fazecast.jSerialComm.SerialPort;

import audinc.gui.MainWin;
import presentables.Presentable;
import presentables.custom_function;

public class IE3301 extends Presentable{
	public JTable part1DataTable;
	private DefaultTableModel p1dtModle;
	public Path savePath;
	
	//audio
	public int 
		logInterval_mill 	= 5000,
		sampleSize_bits		= 16,
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
		
		this.savePath = Presentable.getRoot(this.getClass());
		initGUI(mw);
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
		this.stopRecording();
		
		//this should be already done but just in case
		this.targetLine.close();
	}
	
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
				
			toolbar.add(tb_logbtn);
			toolbar.add(tb_startbtn);
		content.add(toolbar, BorderLayout.PAGE_START);
		
		//log table
		this.part1DataTable = new JTable() {
					private static final long serialVersionUID = 1L;//eclipse complains
					public boolean isCellEditable(int row, int column) { return true; };
				};
			p1dtModle = (DefaultTableModel)this.part1DataTable.getModel();
				for(var v : new String[] {"deciable", "time"}) p1dtModle.addColumn(v);
				
			TableRowSorter<TableModel> table_sorter = new TableRowSorter<>(p1dtModle);
				table_sorter.setSortKeys(Arrays.asList(
						new RowSorter.SortKey(0, SortOrder.ASCENDING)
					));
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
		
		JCheckBox useCustomLineToggler =	new JCheckBox("custom format", this.saveAudio.get());
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
		JComponent[] objs = {
				saveFilePicker,
				saveAudioFileToggler,
				Presentable.genLabelInput("log interval (mil)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField((int)Math.ceil(Math.log10(24*60*60*1000)));
						o.setText(logInterval_mill+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								if(src.isEmpty()) {
									o.setText(logInterval_mill+"");
									setNoticeText("cannot set log interval to nothing", Color.yellow);
									return;
								}
								int val = Integer.parseInt(src);
								
								if(false) {} //something something reasonable
								
								logInterval_mill = val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {} // this dosen't trigger when inside a joptionpane. the same actions that trigger this also close the host panel
						});		
						return o;
					}}),
				audioEncoding,
				Presentable.genLabelInput((JComponent)useCustomLineToggler, new custom_function<JComponent>() {
					@Override public JComponent doTheThing(JComponent o) {
						var v = new JComboBox(detectedLines.toArray());
							v.setSelectedIndex(0);
							v.addActionListener(e -> {
								selectedLine = (Line.Info)v.getSelectedItem();
							});
						return v;
					}}),
				Presentable.genLabelInput("sample size 	(bits)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField(5);
						o.setText(sampleSize_bits+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								if(src.isEmpty()) {
									o.setText(sampleSize_bits+"");
									setNoticeText("cannot set sample size to nothing", Color.yellow);
									return;
								}
								int val = Integer.parseInt(src);
								
								sampleSize_bits = val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {}
						});		
						return o;
					}}),
				Presentable.genLabelInput("channels 	(int)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField(5);
						o.setText(channels+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								if(src.isEmpty() || Integer.parseInt(src) < 1) {
									o.setText(channels+"");
									setNoticeText("must be at least 1 channel", Color.yellow);
									return;
								}
								int val = Integer.parseInt(src);
								
								channels = val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {}
						});		
						return o;
					}}),
				Presentable.genLabelInput("frame size 	(int)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField(5);
						o.setText(frameSize+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								if(src.isEmpty() || Integer.parseInt(src) < 1) {
									o.setText(frameSize+"");
									setNoticeText("frame size must be at least 1", Color.yellow);
									return;
								}
								int val = Integer.parseInt(src);
								
								frameSize = val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {}
						});		
						return o;
					}}),
				Presentable.genLabelInput("frame rate	(int)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField(5);
						o.setText(frameRate+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								if(src.isEmpty() || Integer.parseInt(src) < 1) {
									o.setText(frameRate+"");
									setNoticeText("frame size must be at least 1", Color.yellow);
									return;
								}
								int val = Integer.parseInt(src);
								
								frameRate = val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {}
						});		
						return o;
					}}),
				Presentable.genLabelInput("sample rate	(float)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField(10);
						o.setText(sampleRate+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								float val;
								if(src.isEmpty() || (val = Float.parseFloat(src)) < 1) {
									o.setText(sampleRate+"");
									setNoticeText("sample rate must be at least 1", Color.yellow);
									return;
								}
								
								sampleRate= val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {}
						});		
						return o;
					}}),
				Presentable.genLabelInput("frame rate	(float)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField o = new JTextField(10);
						o.setText(frameRate+"");
						o.getDocument().addDocumentListener(new DocumentListener() {
							@Override public void insertUpdate(DocumentEvent e) {
								String src = o.getText();
								float val;
								if(src.isEmpty() || (val = Float.parseFloat(src)) < 1) {
									o.setText(frameRate+"");
									setNoticeText("sample rate must be at least 1", Color.yellow);
									return;
								}
								
								frameRate= val;
							}
							@Override public void removeUpdate(DocumentEvent e)	 {}
							@Override public void changedUpdate(DocumentEvent e) {}
						});		
						return o;
					}}),
				bigEndianToggler
		};
        JPanel wrapper = new JPanel();
        	wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        for(JComponent v : objs) {v.setAlignmentX(Component.LEFT_ALIGNMENT); wrapper.add(v);}
        JScrollPane wrapper_scroll = new JScrollPane(wrapper,
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        	wrapper_scroll.setBorder(BorderFactory.createEmptyBorder());
        	wrapper_scroll.setMaximumSize(new Dimension(MainWin.stdDimension.width * 2, MainWin.stdDimension.height * 2));
        
        this.setNoticeText("changes in settings will not go into effect untill restart");
        
        JOptionPane.showMessageDialog(cframe, wrapper_scroll, "logging", JOptionPane.PLAIN_MESSAGE);
	}
	
	public void onSaveClick() {
		
	}
	
	public void onImportClick() {
		
	}
	
	public synchronized void Part1DataTableModel(custom_function<DefaultTableModel> action) {
		action.doTheThing(this.p1dtModle);
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
		
		System.out.println("starting recording. "
				+ "\n\rsaveaudio:\t" + this.saveAudio + "");
		
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
	 		
	 		AudioInputStream recordingStream = new AudioInputStream(targetLine);
	 		
	 		if(this.saveAudio.get()) {
		 		audio_thread_recording = new Thread() {
		 			@Override public void run() {
		 				File outputFile;
		 				
		 				Path pth = savePath.resolve(savePath.getFileName() + ".wav");
		 				outputFile = pth.toFile();
		 				setNoticeText("saving audio to:" + pth.toAbsolutePath().toString(), Color.black);
		 				
		 				try {
		 					System.out.println("writing to recording stream");
							AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, outputFile);
						} catch (IOException e) {
							System.out.println(e);
						}
		 			}};
	 		}
	 		
	 		//this is live
	 		audio_thread_analysis = new Thread() {
	 			@Override public void run() {
	 				try {
	 					System.out.println("getting input stream");
						InputStream input = recordingStream;
						
						var dateFormat 	= new SimpleDateFormat("MMddyyyy_HHmmss");
						var calender 	= Calendar.getInstance();
						
						int len;
						byte[] buffer 	= new byte[1024];
						
						while((len = input.read(buffer)) > 0) {
							System.out.println((calender.getTime().getTime()) +"\t:\t"+ Arrays.toString(buffer));
						}
						
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			}};
	 			
	 			
 			if(this.saveAudio.get())
		 		audio_thread_recording.start(); 
 			
 			if(audio_thread_analysis != null)
 				audio_thread_analysis.start();
 			
	 	}catch (Exception e) {
	 		this.setNoticeText(e.toString(), Color.red);
	 		System.out.println(e);
	 	}
	}
	public void stopRecording() {
		System.out.println("stopping recording");
		try {
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
	}
	public boolean isRecording() {
		return targetLine != null && targetLine.isOpen();
	}
	private AudioFormat getAudioFormat() {
		AudioFormat audioFormat;
		if(this.audioFormat_custom) {
			audioFormat = new AudioFormat(
	 				this.audioFormat_encoding,
	 				this.sampleRate,
	 				this.sampleSize_bits, 
	 				this.channels,
	 				this.frameSize,
	 				this.frameRate,
	 				this.audioFormat_bigEndian);
		} else {
			Line.Info info = selectedLine;//i have no clue how to get data out if this thing. i thought it was a related to DataLine.Info but it's not :(
			
			audioFormat = new AudioFormat(
					this.sampleRate,
	 				this.sampleSize_bits,
	 				this.channels,
	 				this.audioFormat_encoding == AudioFormat.Encoding.PCM_SIGNED,
	 				this.audioFormat_bigEndian);
		}
		return audioFormat;
	}
	
///////////////////
//present statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "IE3301";	}
	public static ImageIcon getImgIcon() 	{	return MainWin.getImageIcon("res/aboutbg.png", MainWin.stbPresentIconSize); } //giving a invalid name will default to a blank icon
	public static String getDescription() 	{	return "<html>"
			+ "<body>a quick slap together to generate datasets for IE3301 course > project part 1<br>"
			+ "</body>"; }
}
