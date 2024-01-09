package draggableNodeEditor.suppliers;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.function.Function;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import audinc.gui.MainWin;
import draggableNodeEditor.NodeSupplier;

public class NspColor extends NodeSupplier<Color> {
	private static final long serialVersionUID = -997271649559342764L;

	public NspColor(String name, Color value) {
		this(Color.class, name, value);
	}
	private NspColor(Class<Color> type, String name, Color value) {
		super(type, name, value);
		initGUI();
	}
	
	private void initGUI() {
		  JSpinner 
		  		spinR = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1)),
		  		spinG = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1)),
		  		spinB = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1)),
		  		spinA = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1))
		  	;
		  
		  spinR.setToolTipText("red");
		  spinG.setToolTipText("green");
		  spinB.setToolTipText("blue");
		  spinA.setToolTipText("alpha");
		  
		  Function<JSpinner, Integer> getVal = s -> (int)(s.getValue());
		  ChangeListener listener = e -> {
			  int 
			  	r = getVal.apply(spinR),
			  	g = getVal.apply(spinG),
			  	b = getVal.apply(spinB),
			  	a = getVal.apply(spinA);
			  setValue(new Color(r,g,b,a));
		  };
		  
		  setLayout(new FlowLayout());
		  
		  for(var spin : new JSpinner[]{spinR, spinG, spinB, spinA}) {
			  spin.addChangeListener(listener);
			  
			  var 	prefSize = spin.getPreferredSize();
			  		prefSize.width = Math.min(prefSize.width, MainWin.stdTextSpace);
			  spin.setPreferredSize(prefSize);
			  
			  add(spin);
		  }
	}
}
