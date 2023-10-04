package presentables.presents;

import java.awt.GridLayout;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import audinc.gui.MainWin;
import presentables.Presentable;

public class Awake extends Presentable{
	@Override protected void init(MainWin mw) { initGUI(mw); }
	@Override protected void start() 		{}
	
	private int aHours, aMins, cycles;
	private JLabel awake;

	@Override
	protected void initGUI(MainWin mw) {
		JPanel mainWrap = new JPanel(new GridLayout(3, 1));
		  JPanel control = new JPanel(new FlowLayout());
			
		  	awake = new JLabel("Awake", JLabel.CENTER);
			mainWrap.add(awake);
			
			JPanel hour = new JPanel(new FlowLayout());
			  JLabel hourLab = new JLabel("Hour", JLabel.CENTER);
			  SpinnerModel hourSM = new SpinnerNumberModel(0, 0, 12, 1);
			  JSpinner hourSpin = new JSpinner(hourSM);
			  hourSpin.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent e) {
					  aHours = (int)(((JSpinner)e.getSource()).getValue());
					  setAwake();
				  }
			  });
			  hour.add(hourSpin);
			  hour.add(hourLab);
			control.add(hour);
			
			JPanel mins = new JPanel(new FlowLayout());
			  JLabel minsLab = new JLabel("Minutes", JLabel.CENTER);
			  SpinnerModel minsSM = new SpinnerNumberModel(0, 0, 59, 1);
			  JSpinner minsSpin = new JSpinner(minsSM);
			  minsSpin.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent e) {
					  aMins = (int)(((JSpinner)e.getSource()).getValue());
					  setAwake();
				  }
			  });
			  mins.add(minsSpin);
			  mins.add(minsLab);
			control.add(mins);
			
			JPanel cycle = new JPanel(new FlowLayout());
			  JLabel cycleLab = new JLabel("Cycles", JLabel.CENTER);
			  SpinnerModel cycleSM = new SpinnerNumberModel(0, 0, 12, 1);
			  JSpinner cycleSpin = new JSpinner(cycleSM);
			  cycleSpin.addChangeListener(new ChangeListener() {
				  public void stateChanged(ChangeEvent e) {
					  cycles = (int)(((JSpinner)e.getSource()).getValue());
					  setAwake();
				  }
			  });
			  cycle.add(cycleSpin);
			  cycle.add(cycleLab);
			control.add(cycle);

		  mainWrap.add(control);
		mw.add(mainWrap);
	}
	
	private void setAwake() {
		int aTime = aHours*60 + aMins + cycles*90;
		
		int nH = (aTime / 60) % 12;
		int nM = aTime % 60;
		awake.setText("Awake: " + nH + ":" + String.format("%02d", nM));
	}
	
	public static String getDisplayTitle() 	{	return "Awake";	}
	public static ImageIcon getImgIcon() 	{	return getImageIcon("res/mouse.png"); }
	public static String getDescription() 	{	return 
			"Tells you the time that you should wake up based on sleep cycles. "
			+ "So that you don't interupt your REM sleep."; }
}
