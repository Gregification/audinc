package presentables.presents;

import presentables.Presentable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import audinc.gui.MainWin;

public class menu extends Presentable{
	JPanel descriptionArea, displayArea;
	private JButton d_start_btn;
	private JLabel d_title, d_description;
	
	public static String getDisplayTitle() 	{ 	return "MENU";	}
	public static String getDescription() 	{	return "the menu lists all avaliable presents along side their icons. clicking on one will show its description & details";	}
	public static ImageIcon getImgIcon() 	{	try {
			return new ImageIcon( ImageIO.read(new File("res/presentIcons/default.png")));
		} catch (IOException e) {	e.printStackTrace(); return null; }
	}
	@Override public void init() 			{}
	@Override protected void start() 		{	setAppDetails("Menu"); }
	
	@Override protected void initGUI(JFrame jf) {
		jf.setLayout(new BorderLayout());
		
		Border etchedLowered = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		
		d_start_btn = new JButton("SELECT");			d_start_btn	 .setBorder(etchedLowered);
//		d_icon = new JLabel("icon");				d_icon		 .setBorder(etchedLowered);
		d_title = new JLabel(); 	d_title.setBorder(
				BorderFactory.createTitledBorder(etchedLowered, "Name", TitledBorder.LEFT, TitledBorder.TOP));
			d_title.setVerticalAlignment(JLabel.TOP);
		d_description = new JLabel(); 	d_description.setBorder(
				BorderFactory.createTitledBorder(etchedLowered, "Description", TitledBorder.LEFT, TitledBorder.TOP));
			d_description.setVerticalAlignment(JLabel.TOP);
		
		displayArea = new JPanel();
//			displayArea.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/5), (int)MainWin.stdDimension.getHeight()));
			JScrollPane DAscrollFrame = new JScrollPane(displayArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				DAscrollFrame.setPreferredSize(new Dimension(100, 100));
				DAscrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()*2/5), (int)MainWin.stdDimension.getHeight()));
				DAscrollFrame.setLayout(new ScrollPaneLayout());
				DAscrollFrame.setAutoscrolls(true);
		
		descriptionArea = new JPanel();
//			descriptionArea.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()/3), (int)MainWin.stdDimension.getHeight()));
			descriptionArea.setLayout(new BorderLayout());
			descriptionArea.add(d_start_btn, 	BorderLayout.PAGE_END);
			descriptionArea.add(d_description,	BorderLayout.CENTER);
			descriptionArea.add(d_title,		BorderLayout.PAGE_START);
			JScrollPane DesscrollFrame = new JScrollPane(descriptionArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				DesscrollFrame.setPreferredSize(new Dimension(100, 100));
				DesscrollFrame.setAutoscrolls(true);
				DesscrollFrame.setMinimumSize(new Dimension((int)(MainWin.stdDimension.getWidth()/3), (int)MainWin.stdDimension.getHeight()));
		
		JSplitPane sl = new JSplitPane(SwingConstants.VERTICAL, DAscrollFrame, DesscrollFrame);
		jf.add(sl);
		
	}
	
	protected void setAppDetails(String presentName) {
		setAppDetails(MainWin.Presents.get(presentName));
	}
	
	protected void setAppDetails(Class<? extends Presentable> clas) {
		d_description.setText((String)Presentable.tryForStatic(clas, "getDescription"));
		d_title.setText((String)Presentable.tryForStatic(clas, "getDisplayTitle"));
		
	}
}
