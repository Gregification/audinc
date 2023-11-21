package draggableNodeEditor.suppliers;

import java.awt.GridBagLayout;
import java.util.function.BiFunction;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;

import audinc.gui.MainWin;
import draggableNodeEditor.NodeSupplier;
import presentables.Presentable;

/**
 * a simple wrapper class for node-components that use a JSpinner. 
 * @param <T>
 */
public class NspSpinner<T> extends NodeSupplier<T> {
	private static final long serialVersionUID = 1L;
	private static final int minWidth = MainWin.stdTextSpace * 3;
	
	public JSpinner spinner;
	
	public NspSpinner(AbstractSpinnerModel spinnerModel, BiFunction<JSpinner, ChangeEvent,T> onSpinnerChange) {
		this("", spinnerModel, onSpinnerChange);
		this.setName(value.getClass().toString());
	}
	@SuppressWarnings("unchecked")//death
	public NspSpinner(String name, AbstractSpinnerModel spinnerModel, BiFunction<JSpinner, ChangeEvent,T> onSpinnerChange) {
		super(name, (T)spinnerModel.getValue());
		
		spinner = new JSpinner(spinnerModel);
			spinner.addChangeListener(e -> {
		  			setValue(onSpinnerChange.apply(spinner, e));
		  		});
		
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(spinner.getSize());
		this.add(spinner, Presentable.createGbc(0, 0));
		
		var prefSize = spinner.getPreferredSize();
			prefSize.width = Math.max(prefSize.width, minWidth);
		spinner.setPreferredSize(prefSize);
			
		this.setPreferredSize(prefSize);
	}
}
