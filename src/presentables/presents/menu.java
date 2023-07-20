package presentables.presents;

import presentables.Presentable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import audinc.gui.MainWin;
import audinc.gui.WrapLayout;

public class menu extends Presentable{
	private JPanel descriptionArea, displayArea;
	private JButton d_start_btn;
	private JLabel d_title, d_description;
	private Class<? extends Presentable> selectedPresent = null;
	private Thread thread_loadApp = null;
	
	
	@Override public void init() 			{}
	@Override protected void start() 		{
		setAppDetails(MainWin.Presents.entrySet().iterator().next().getKey()); 
	}
	
	@Override protected void initGUI(MainWin mw) {
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
		
		JPanel screenLeft = new JPanel();
			screenLeft.setLayout(new BorderLayout());
			JTextField searchBarInput = new JTextField();
					searchBarInput.setBorder(BorderFactory.createTitledBorder(etchedLowered, "Search", TitledBorder.CENTER, TitledBorder.TOP));
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
				
		JPanel screenRight = new JPanel();
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
	
	protected void setAppDetails(String presentName) {
		setAppDetails(MainWin.Presents.get(presentName));
	}
	protected void setAppDetails(Class<? extends Presentable> clas) {
		selectedPresent = clas;
		d_start_btn.setEnabled(true);
		d_description.setText((String)Presentable.tryForStatic(clas, "getDescription"));
		d_title.setText((String)Presentable.tryForStatic(clas, "getDisplayTitle"));
		
	}

///////////////////
//button events
///////////////////
	protected void onStartClick(MainWin mw) {
		if(selectedPresent != null) {
			try {
				mw.setPresent(selectedPresent);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else d_start_btn.setEnabled(false);
	}

///////////////////
//Presentable load apps
///////////////////
	private void loadApps() {
		if(thread_loadApp.isAlive()) {
			thread_loadApp.interrupt();
		}
		thread_loadApp = new Thread(() -> loadApps_threadFunciton());
	}
	private void loadApps_threadFunciton() { //there should only ever be 1 thread running this at anytimes.
		Iterator<Class<? extends Presentable>> presI  = MainWin.Presents.values().iterator();
		for(Class<? extends Presentable> p; presI.hasNext(); p = presI.next()) {
			
		}
	}
	
///////////////////
//Presentable statics
///////////////////
	public static String getDisplayTitle() 	{ 	return "MENU";	}
	public static ImageIcon getImgIcon() 	{	try { return new ImageIcon( ImageIO.read(new File("res/presentIcons/default.png"))); } catch (IOException e) {	e.printStackTrace(); return null; }}
	public static String getDescription() 	{	return "<html><body>"
			+ "the menu lists all avaliable presents along side their icons." 
			+ "<br>Clicking on one will show its description & details"
			+ "</body></html>";	}
}
