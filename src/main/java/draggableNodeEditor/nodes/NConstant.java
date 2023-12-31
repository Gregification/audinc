package draggableNodeEditor.nodes;

import java.awt.Component;
import java.awt.GridBagLayout;
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
import draggableNodeEditor.DraggableNode;
import draggableNodeEditor.DraggableNodeEditor;
import draggableNodeEditor.NodeSupplier;
import draggableNodeEditor.suppliers.NspSpinner;
import presentables.Presentable;

public class NConstant extends DraggableNode<Void> {
	private static final long serialVersionUID = 1L;
	
	//GUI
	private Component constantsTableWrapper;
	private JTable constantsTable;	//could be better optimized 
	private static final Map<Class<?>, Function<Void, NodeSupplier<?>>> supportedConstants = Map.of(
				Number.class, v -> new NspSpinner<Number>(
							Number.class,
							"number spinner",
							new SpinnerNumberModel(0, null, null, 1),
							(s,e) -> (Number)s.getModel().getValue()
						)
			);
	
	//node
	protected NodeSupplier<?> supplier;
	
	public NConstant() {
		this(null);
	}
	public NConstant(NodeSupplier<?> supplier) {
		super(null);
		
		this.supplier = supplier;
		
		setLayout(new GridBagLayout());
		setPreferredSize(MainWin.stdtabIconSize);
		
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
				setSupplier(supportedConstants.get(key).apply(null));
				
				self.revalidate();
			}
		});
		
		constantsTable.getSelectionModel().setSelectionInterval(0, 0);
	}

	@Override public void initNode(DraggableNodeEditor editor) {
		setSupplier(supplier);
	}
	
	public NodeSupplier<?> getSupplier() {
		return supplier;
	}

	public void setSupplier(NodeSupplier<?> suppi) {
		boolean isnotnull = supplier != null;
		
		if(isnotnull)
			remove(supplier);
		
		supplier = suppi;
		
		if(isnotnull) {
			add(supplier, Presentable.createGbc(0, 0));
			setPreferredSize(supplier.getPreferredSize());
			genConnectionPoint(supplier);
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
