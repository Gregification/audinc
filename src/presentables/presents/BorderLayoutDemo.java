package presentables.presents;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;

import presentables.Presentable;

public class BorderLayoutDemo extends Presentable {
	@Override
	public void init() {
		this.dispalyTitle = "java.swing.BorderLayout demo";
	}

	@Override
	protected void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initGUI(JFrame jf) {
		JLabel lineStartLabel = new JLabel("LINE_START", JLabel.CENTER);
		 lineStartLabel.setOpaque(true);
		 lineStartLabel.setBackground(Color.RED);
		 jf.add(lineStartLabel, BorderLayout.LINE_START); 
		 
		 JLabel centerLabel = new JLabel("CENTER", JLabel.CENTER);
		 centerLabel.setOpaque(true);
		 centerLabel.setBackground(Color.YELLOW);
		 jf.add(centerLabel, BorderLayout.CENTER);
		 
		 JLabel lineEndLabel = new JLabel("LINE_END", JLabel.CENTER);
		 lineEndLabel.setOpaque(true);
		 lineEndLabel.setBackground(Color.RED);
		 jf.add(lineEndLabel, BorderLayout.LINE_END);
		 
		 JLabel pageEndLabel = new JLabel("PAGE_END", JLabel.CENTER);
		 pageEndLabel.setOpaque(true);
		 pageEndLabel.setBackground(Color.GREEN);
		 jf.add(pageEndLabel, BorderLayout.PAGE_END);
		 
		 JLabel pageStartLabel = new JLabel("PAGE_START", JLabel.CENTER);
		 pageStartLabel.setOpaque(true);
		 pageStartLabel.setBackground(Color.GREEN);
		 jf.add(pageStartLabel, BorderLayout.PAGE_START);
	}
}
