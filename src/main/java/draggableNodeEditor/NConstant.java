package draggableNodeEditor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import draggableNodeEditor.suppliers.NspSpinner;
import presentables.Presentable;

public class NConstant extends DraggableNode {
	private static final long serialVersionUID = 1L;
	
	//GUI
	private static final JScrollPane constantsTableWrapper;
	private static final JTable constantsTable;
	private static final Map<Class, Function<Void, NodeSupplier>> supportedConstants = Map.of(
				Integer.class, v -> new NspSpinner<Integer>(
							new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1),
							(s,e) -> (int)s.getModel().getValue()
						),
				Double.class, v -> new NspSpinner<Double>(
							new SpinnerNumberModel(0, Double.MIN_VALUE, Double.MAX_VALUE, .1),
							(s,e) -> (Double)s.getModel().getValue()
						),
				Float.class, v -> new NspSpinner<Float>(
							new SpinnerNumberModel(0, Float.MIN_VALUE, Float.MAX_VALUE, .1),
							(s,e) -> (Float)s.getModel().getValue()
						)
			);
	private int tableIndex = -1;
	
	//node
	protected NodeSupplier supplier;
	
	static {
		var constantsKeys = supportedConstants.keySet().stream()
				.map(e -> new Object[] {e})
				.toArray(Object[][]::new);
		constantsTable = new JTable(new DefaultTableModel(constantsKeys, new Object[]{"constants avaliable(" + constantsKeys.length + ")"})) {
				private static final long serialVersionUID = 7L;
				
				public boolean isCellEditable(int row, int column) {                
	                return false;               
				};
			};
			constantsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			constantsTable.setCellEditor(null);
		
		constantsTableWrapper = new JScrollPane(constantsTable,	
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}
	
	public NConstant() {
		this(null);
	}
	public NConstant(NodeSupplier supplier) {
		super();
		this.supplier = supplier;
		
		initGUI();
	}
	
	@Override public void initGUI() {
		this.removeAll();
		
		if(this.supplier != null)
			this.add(supplier);
	}

	@Override public void initNode() {
		
	}

	@Override public List<NodeComponent> getNodeComponents() {
		return null;
	}
	
	public NodeSupplier getSupplier() {
		return supplier;
	}

	public void setSupplier(NodeSupplier supplier) {
		if(this.supplier != null)
			this.remove(this.supplier);
		
		this.supplier = supplier;
		this.add(supplier, Presentable.createGbc(0, 0));
	}

	@Override public String getTitle() {
		return "Supplier of a constant";
	}
	@Override public JComponent getInspector() {
		var model = constantsTable.getSelectionModel();
		
		model.clearSelection();
				
		model.removeListSelectionListener(constantsTable);
		
		final var self = this;
		model.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {
				int selectedRow = constantsTable.getSelectedRow();
				
				if(selectedRow < 0) return;
				
				tableIndex = selectedRow;
				Object key = (constantsTable.getModel().getValueAt(selectedRow, 0));
				supplier = supportedConstants.get(key).apply(null);
				
				self.revalidate();
			}
		});
		
		return constantsTableWrapper;
	}
}
