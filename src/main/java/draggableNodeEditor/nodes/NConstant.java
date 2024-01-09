package draggableNodeEditor.nodes;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.DraggableNodeEditor;
import draggableNodeEditor.NodeSupplier;
import draggableNodeEditor.suppliers.NspColor;
import draggableNodeEditor.suppliers.NspSpinner;
import presentables.Presentable;

public class NConstant extends DraggableNode<Void> {
	private static final long serialVersionUID = 1L;
	
	//GUI
	private Component constantsTableWrapper;
	private JTable constantsTable;	//could be better optimized 
	private static final Map<Class<?>, Supplier<NodeSupplier<?>>> supportedConstants = Map.of(
				Number.class, () -> new NspSpinner<Number>(
							Number.class,
							"number spinner",
							new SpinnerNumberModel(0, null, null, 1),
							(s,e) -> (Number)s.getModel().getValue()
						),
				Color.class, () -> new NspColor(
							"color picker",
							Color.black
						)
			);
	private static final Supplier<NodeSupplier<?>> defaultSupplier;
	
	static {
		defaultSupplier = supportedConstants.values().stream().toList().getFirst();
	}
	
	//NConstant stuff
	protected NodeSupplier<?> supplier = null;
	protected DraggableNodeEditor nodeEditor;
	
	public NConstant() {
		this(defaultSupplier.get());
	}
	public NConstant(NodeSupplier<?> supplier) {
		super(null);
		
		setSupplier(supplier);
		
		setLayout(new GridBagLayout());
		
		initGUI();
	}
	
	@Override public void initGUI() {
		super.initGUI();
		this.setOpaque(false);
		
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
			constantsTable.setAlignmentX(JTable.LEFT_ALIGNMENT);;
			
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
				setSupplier(supportedConstants.get(key).get());
				
				self.revalidate();
			}
		});
		
		constantsTable.getSelectionModel().setSelectionInterval(0, 0);
	}

	@Override public void initNode(DraggableNodeEditor editor) {
		nodeEditor = editor;
		setSupplier(supplier);
	}
	
	public NodeSupplier<?> getSupplier() {
		return supplier;
	}

	public void setSupplier(NodeSupplier<?> suppi) {		
		if(this.supplier != null && this.supplier != suppi) {
			supplier.onDelete(nodeEditor);
			remove(supplier);
		}
		
		supplier = suppi;
		
		if(this.supplier != null) {
			supplier.hostNode = this;
			setPreferredSize(supplier.getPreferredSize());
			add(supplier, Presentable.createGbc(0, 0));
		}
		
	}

	@Override public String getTitle() {
		return "Constant (" + index + ")";
	}
	@Override public JComponent getInspector() {	
		var v = super.getInspector();
		v.add(constantsTableWrapper);
		return v;
	}
}
