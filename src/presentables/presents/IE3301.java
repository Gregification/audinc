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
	public volatile int 
		logInterval_mill 	= 5000,
		logSamples			= 5,
		sampleSize_bytes	= 2,
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
		//stops & cleans up streams and threads
		this.stopRecording();
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
				for(var v : new String[] {"val", "time(mill-epoch)", "channel#"}) p1dtModle.addColumn(v);
				
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
				Presentable.genLabelInput("sample size 	(bytes)	: ", new custom_function<JTextField>() {
					@Override public JTextField doTheThing(JTextField thisisnull) {
						JTextField tf = new JTextField(5);
						tf.setText(sampleSize_bytes+"");
						
						custom_function<Boolean> isValid = new custom_function<>() {
							@Override public Boolean doTheThing(Boolean o) {
								String s = tf.getText();
								int v;
								if(s.isBlank() || (v = Integer.parseInt(s)) <=0 || v > 100) {
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
								
								sampleSize_bytes = Integer.parseInt(tf.getText());
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
		
	}
	
	public void onImportAudioClick() {
		
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
		 				Path pth = savePath.resolve(savePath.getFileName().toString()+(int)(System.nanoTime()/1000000) + ".wav");
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
		 				try(AudioInputStream input = new AudioInputStream(targetLine);) {
		 					analizeWav(input,getAudioFormat());
		 				} catch (IOException e) {
							e.printStackTrace();
						}
		 				
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
	}
	public boolean isRecording() {
		return targetLine != null && targetLine.isOpen();
	}
	private synchronized AudioFormat getAudioFormat() {
		AudioFormat audioFormat;
		
		int sampleSize_bits = this.sampleSize_bytes * 8;
		
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
	
	private void analizeWav(InputStream input, AudioFormat format) throws IOException{		
		int
			nChans 			= format.getChannels(),
			nChanSize_byte	= sampleSize_bytes,
			nSamples 		= logSamples,
			nSpSize_byte	= nChans * nChanSize_byte,
			bufferSize_byte	= nSamples * nSpSize_byte;
		
		ByteOrder byteOrder	= format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
		
		byte[] 	
			buffer 		= new byte[bufferSize_byte],	//raw input
			spBuffer	= new byte[nChanSize_byte];		//bytes per float 
		
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
			
			//cat input buffer -> channel data
			for(int iSp = 0, iChan, iByte, iTotal; iSp < nSamples; iSp++) { 	//per sample
				
				for(iChan = 0; iChan < nChans; iChan++){							//per channel
					iTotal = iSp*nSpSize_byte + iChan*nChanSize_byte;
					
					for(iByte = 0; iByte < nChanSize_byte; iByte++) { 					//per byte
						spBuffer[iByte] = buffer[iTotal + iByte];
					}
					
					chanBuffer[iChan][iSp] = ByteBuffer.wrap(spBuffer).order(byteOrder).getInt();
					p1dtModle.addRow(new Object[] {chanBuffer[iChan][iSp], (start / e6), iChan});
				}
			}
			
			System.out.println(start +"\t:\t"+ Arrays.toString(buffer));
			
			System.out.print("\telapsed:" + elapsed + "\t < interval? ");
			if(elapsed < logInterval_mill) {
				System.out.println("true");
				try {
					Thread.sleep(logInterval_mill-elapsed);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}else {
				System.out.println("false");
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

