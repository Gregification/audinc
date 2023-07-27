package presentables.presents;

import presentables.Presentable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import audinc.gui.MainWin;
import audinc.gui.WrapLayout;

public class menu extends Presentable{
	private JPanel descriptionArea, displayArea, screenLeft, screenRight;
	private JButton d_start_btn;
	private JLabel d_title, d_description;
	private JTextField searchBarInput;
	private Class<? extends Presentable> selectedPresent = null;
	private Thread thread_loadApps = null;
	private MainWin mw;
	
	@Override protected void start() 		{
		selectApp(MainWin.Presents.stream().findAny().orElse(presentables.presents.menu.class));
		loadApps(searchBarInput.getText());
	}
	
	@Override protected void init(MainWin mw) { initGUI(mw); }
	@Override protected void initGUI(MainWin mw) {
		this.mw = mw;
		JFrame jf = mw;//just for readability
		
		jf.setLayout(new BorderLayout());
		
		Border etchedLowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		
		d_start_btn = new JButton("SELECT"); d_start_btn.setBorder(etchedLowered);
			d_start_btn.setEnabled(false);
			d_start_btn.addActionListener(event -> onStartClick(mw));
		d_title = new JLabel();				d_title		.setBorder(BorderFactory.createTitledBorder(etchedLowered, "Name", TitledBorder.LEFT, TitledBorder.TOP));
			d_title.setVerticalAlignment(JLabel.TOP);
		d_description = new JLabel(); 	d_description	.setBorder(BorderFactory.createTitledBorder(etchedLowered, "Description", TitledBorder.LEFT, TitledBorder.TOP));
			d_description.setVerticalAlignment(JLabel.TOP);
		
		screenLeft = new JPanel();
			screenLeft.setLayout(new BorderLayout());
			searchBarInput = new JTextField();
				searchBarInput.setBorder(BorderFactory.createTitledBorder(etchedLowered, "Search", TitledBorder.CENTER, TitledBorder.TOP));
				searchBarInput.addActionListener(new ActionListener() {
				      public void actionPerformed(ActionEvent e) { loadApps(searchBarInput.getText());}
				      });
			screenLeft.add(searchBarInput, BorderLayout.PAGE_END);
			displayArea = new JPanel(); 		//app display
	//			displayArea.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/5), (int)MainWin.stdDimension.getHeight()));
				displayArea.setLayout(new WrapLayout());
				JScrollPane DAscrollFrame = new JScrollPane(displayArea,	//Display Apps
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					DAscrollFrame.setPreferredSize(new Dimension(100, 100));
					DAscrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/5), (int)MainWin.stdDimension.getHeight()));
					DAscrollFrame.setLayout(new ScrollPaneLayout());
					DAscrollFrame.setAutoscrolls(true);
			screenLeft.add(DAscrollFrame);
				
		screenRight = new JPanel();
			screenRight.setLayout(new BorderLayout());
			screenRight.add(d_start_btn, 	BorderLayout.PAGE_END);
			descriptionArea = new JPanel();
	//			descriptionArea.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()/3), (int)MainWin.stdDimension.getHeight()));
				descriptionArea.setLayout(new BorderLayout());
				descriptionArea.add(d_description,	BorderLayout.CENTER);
				descriptionArea.add(d_title,		BorderLayout.PAGE_START);
				JScrollPane DesscrollFrame = new JScrollPane(descriptionArea,	//Description
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					DesscrollFrame.setPreferredSize(new Dimension(100, 100));
					DesscrollFrame.setAutoscrolls(true);
					DesscrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()/3), (int)MainWin.stdDimension.getHeight()));
			screenRight.add(DesscrollFrame);
		
		JSplitPane sl = new JSplitPane(SwingConstants.VERTICAL, screenLeft, screenRight);
		jf.add(sl);
	}
	protected JToolBar setupGUIToolBar(MainWin mw) {
		JToolBar toolbar = new JToolBar();
		
		
		
		return toolbar;
	}
	
	
	protected void selectApp(Class<? extends Presentable> clas) {
		if(selectedPresent != null && selectedPresent == clas) {
			onStartClick(mw);
			return;
		}
		selectedPresent = clas;
		d_start_btn.setEnabled(true);
		d_description.setText((String)Presentable.tryForStatic(clas, "getDescription"));
		d_title.setText((String)Presentable.tryForStatic(clas, "getDisplayTitle"));	
	}

///////////////////
//button events
///////////////////
	protected void onStartClick(MainWin mw) {
		if(selectedPresent != null) 
			mw.setPresent(selectedPresent);
		else 
			d_start_btn.setEnabled(false);
	}

///////////////////
//Presentable load apps
///////////////////
	private void loadApps(String pattern) {
		if(thread_loadApps != null && thread_loadApps.isAlive()) thread_loadApps.interrupt();
		
		displayArea.removeAll();
		
		thread_loadApps = new Thread(() -> loadApps_threadFunciton(new String(pattern), MainWin.Presents));
		thread_loadApps.start();
	}
	private void loadApps_threadFunciton(String pattern, Collection<Class<? extends Presentable>> presents) {
		int tick = 0, tickperiod = 5; 
		for(Class<? extends Presentable> p : presents) {
			String tTitle = Presentable.tryForStatic(p, "getDisplayTitle").toString(); 
			if(tTitle.toLowerCase().contains(pattern.toLowerCase())) {
				displayArea.add(genDisplayCard(p));
				if(tick <= 0) {
					screenLeft.validate();
					tick = tickperiod;
				}else tick--;
			}
		}
		
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { e.printStackTrace(); }
		
		screenLeft.validate();
	}
	private JComponent genDisplayCard(Class<? extends Presentable> present) {
		JButton jp = new JButton();
		jp.setMinimumSize(new Dimension(100,100));
		jp.addActionListener(event -> {selectApp(present);});
		jp.setLayout(new BorderLayout());
		jp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		//set image
		jp.add(new JLabel((ImageIcon)Presentable.tryForStatic(present, "getImgIcon")) ,BorderLayout.CENTER);
		
		//set title
		JLabel cTitle = new JLabel((String)Presentable.tryForStatic(present, "getDisplayTitle"));
		cTitle.setHorizontalAlignment(SwingConstants.CENTER);
		jp.add(cTitle, BorderLayout.PAGE_END);
		
		return jp;
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "Menu";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/presentIcons/menu.png"); }
	public static String getDescription() 	{	return "<html><body>"
			+ "the menu lists all avaliable presents along side their icons." 
			+ "<br>Clicking on one will show its description & details"
			+ "<br>- note: the swing component used to display the presents dosent refresh very well."
			+ "<br>&emsp;try resizing any of the windows elements to have it correct itself."
			+ "</body></html>";	}
}
