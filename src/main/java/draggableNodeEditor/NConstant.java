package draggableNodeEditor;

import java.awt.GridBagLayout;
import java.awt.Rectangle;
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

import audinc.gui.MainWin;
import draggableNodeEditor.suppliers.NspSpinner;
import presentables.Presentable;

public class NConstant extends DraggableNode {
	private static final long serialVersionUID = 1L;
	
	//GUI
	private JScrollPane constantsTableWrapper;
	private JTable constantsTable;
	private static final Map<Class, Function<Void, NodeSupplier>> supportedConstants = Map.of(
				Integer.class, v -> new NspSpinner<Integer>(
							new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1),
							(s,e) -> (int)s.getModel().getValue()
						),
				Double.class, v -> new NspSpinner<Double>(
							new SpinnerNumberModel(0.0, Double.MIN_VALUE, Double.MAX_VALUE, .1),
							(s,e) -> (Double)s.getModel().getValue()
						),
				Float.class, v -> new NspSpinner<Float>(
							new SpinnerNumberModel(0f, Float.MIN_VALUE, Float.MAX_VALUE, .1),
							(s,e) -> (Float)s.getModel().getValue()
						)
			);
	
	//node
	protected NodeSupplier supplier;
	
	public NConstant() {
		this(null);
	}
	public NConstant(NodeSupplier supplier) {
		super();
		this.supplier = supplier;
		this.setLayout(new GridBagLayout());
		this.setPreferredSize(MainWin.stdtabIconSize);
		
		initGUI();
	}
	
	@Override public void initGUI() {
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
		
		var model = constantsTable.getSelectionModel();
		
		final var self = this;
		model.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {
				int selectedRow = constantsTable.getSelectedRow();
				
				if(selectedRow < 0) return;
				
				Object key = (constantsTable.getModel().getValueAt(selectedRow, 0));
				setSupplier(supportedConstants.get(key).apply(null));
				
				self.revalidate();
			}
		});
		
		
		
		this.setSupplier(supplier);
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
		boolean isnotnull = this.supplier != null;
		
		if(isnotnull)
			this.remove(this.supplier);
		
		this.supplier = supplier;
		
		if(isnotnull) {
			this.add(supplier, Presentable.createGbc(0, 0));
			this.setPreferredSize(supplier.getPreferredSize());
		}
		
//		System.out.println(""
//				+ "NConstant prefered/actual size:" + this.getPreferredSize() + "\t / \t" + this.getSize()
//				+ "\nsupplier prefered/actual  size:" + (supplier==null?"null": (supplier.getPreferredSize() + "\t / \t" + supplier.getSize())));
		
	}

	@Override public String getTitle() {
		return "Constant";
	}
	@Override public JComponent getInspector() {		
		return constantsTableWrapper;
	}
}
