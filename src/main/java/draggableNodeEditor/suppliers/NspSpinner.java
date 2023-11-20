package draggableNodeEditor.suppliers;

import java.awt.GridBagLayout;
import java.util.function.BiFunction;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;

import draggableNodeEditor.NodeSupplier;
import presentables.Presentable;

/**
 * a simple wrapper class for node-components that use a JSpinner. 
 * @param <T>
 */
public class NspSpinner<T> extends NodeSupplier<T>{
	public JSpinner spinner;
	
	public NspSpinner(AbstractSpinnerModel spinnerModel, BiFunction<JSpinner, ChangeEvent,T> onSpinnerChange) {
		this("", spinnerModel, onSpinnerChange);
		this.name = value.getClass().toString();
	}
	@SuppressWarnings("unchecked")//death
	public NspSpinner(String name, AbstractSpinnerModel spinnerModel, BiFunction<JSpinner, ChangeEvent,T> onSpinnerChange) {
		super(name, (T)spinnerModel.getValue());
		
		spinner = new JSpinner(spinnerModel);
			spinner.addChangeListener(e -> {
		  			setValue(onSpinnerChange.apply(spinner, e));
		  		});
		
		this.setLayout(new GridBagLayout());
		this.add(spinner, Presentable.createGbc(0, 0));
	}

	@Override public T get() {
		return getValue();
	}
}
