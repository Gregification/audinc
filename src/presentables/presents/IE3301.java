package presentables.presents;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
		audioFormat_bigEndian = false,
		saveAudio = false;
	public AudioFormat.Encoding audioFormat_encoding = AudioFormat.Encoding.PCM_FLOAT;
	
	private boolean logginEnabled = false;
	private JLabel noticeDisplay = new JLabel("");
	
	@Override protected void start()	{
		
	}
	
	@Override protected void init(MainWin mw) 	{
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
	
	@Override public void quit() {}
	
	public void genUI_tab_part1(JTabbedPane host_tabb) {
		JPanel content = new JPanel(new BorderLayout());
		
		var emptyBorder = BorderFactory.createEmptyBorder();
		
		//tool bar
		JToolBar toolbar = new JToolBar();
			JButton tb_logbtn = new JButton(MainWin.getImageIcon("res/log.png", MainWin.stdtabIconSize));
				tb_logbtn.setBorder(emptyBorder);
				tb_logbtn.setToolTipText("log settings");
				tb_logbtn.addActionListener(e -> openSettingsDialoug());
			JButton tb_startbtn = new JButton(MainWin.getImageIcon("res/playbtn.png", MainWin.stdtabIconSize));
				tb_startbtn.setBorder(emptyBorder);
				tb_startbtn.setToolTipText("play/stop button");
				tb_startbtn.addActionListener(e -> onPlayBtnClick());
				
			toolbar.add(tb_logbtn);
			toolbar.add(tb_startbtn);
		content.add(toolbar, BorderLayout.PAGE_START);
		
		//log table
		this.part1DataTable = new JTable() {
					private static final long serialVersionUID = 1L;//eclipse complains
					public boolean isCellEditable(int row, int column) { return false; }; //disables user editing of table
				};
			var table_model = (DefaultTableModel)this.part1DataTable.getModel();
				for(var v : new String[] {"deciable", "time"}) table_model.addColumn(v);
				
			TableRowSorter<TableModel> table_sorter = new TableRowSorter<>(table_model);
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
		JCheckBox saveAudioFileToggler =	new JCheckBox("save audio", this.saveAudio);
			saveAudioFileToggler.addItemListener(il -> {
		    		if(il.getStateChange() == ItemEvent.SELECTED) {
		    			this.saveAudio = true;
		    		}else if(il.getStateChange() == ItemEvent.DESELECTED) {
		    			this.saveAudio = false;
		    		}
		    	});
			
		//package components
		JComponent[] objs = {
				saveFilePicker,
				audioEncoding,
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
				bigEndianToggler,
				saveAudioFileToggler
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
	
	public void setNoticeText(String text) {
		this.noticeDisplay.setText(text);
	}
	public void setNoticeText(String text, Color color) {
		this.noticeDisplay.setForeground(color);
		setNoticeText(text);
	}
	
	public void onPlayBtnClick() {
	 	try {
	 		AudioFormat audioFormat = new AudioFormat(
	 				this.audioFormat_encoding,
	 				this.sampleRate,
	 				this.sampleSize_bits, 
	 				this.channels,
	 				this.frameSize,
	 				this.frameRate,
	 				false);
	 		
	 		DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
	 		if(!AudioSystem.isLineSupported(dataInfo)) {
	 			String message = "the audio system does not support a dataline by the given specs";
	 			this.setNoticeText(message, Color.red);
		 		JOptionPane.showConfirmDialog(null, message,  "fatal", JOptionPane.PLAIN_MESSAGE);
	 		}
	 		
	 		TargetDataLine targetLine = (TargetDataLine)AudioSystem.getLine(dataInfo);
	 		targetLine.open();
	 		
	 		JOptionPane.showMessageDialog(null, "hit ok to start recording");
	 		targetLine.start();
	 		
	 	}catch (Exception e) {
	 		this.setNoticeText(e.toString(), Color.red);
	 		System.out.println(e);
	 	}
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
