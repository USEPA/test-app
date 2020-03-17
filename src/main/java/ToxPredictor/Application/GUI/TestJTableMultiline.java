package ToxPredictor.Application.GUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class TestJTableMultiline extends JFrame {
	
	public TestJTableMultiline() {
		super("Multi-Line Cell Example");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		DefaultTableModel dm = new DefaultTableModel() {
			public Class<String> getColumnClass(int columnIndex) {
				return String.class;
			}
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		dm.setDataVector(
				new Object[][]{
					{"A0, Line1\nA0, Line2\nA0, Line3", 
						"B0, Line1\nB0, Line2", 
					"C0, Line1"}, 
					{"A1, Line1", 
						"B1, Line1\nB1, Line2", 
					"C1, Line1"},
					{"A2, Line1", 
						"B2, Line1", 
					"C2, Line1"}
				}, 
				new Object[] {"A", "B", "C"});

		JTable table = new JTable(dm);
		table.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());
		TableRowSorter<? extends TableModel> sort = new TableRowSorter<DefaultTableModel>(dm);
		table.setRowSorter(sort);
		JScrollPane scroll = new JScrollPane(table);
		getContentPane().add(scroll);
		setLocationByPlatform(true);
		setSize(400, 430);
		setVisible(true);
	}

	public static void main(String[] args) {
		TestJTableMultiline frame = new TestJTableMultiline();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}


