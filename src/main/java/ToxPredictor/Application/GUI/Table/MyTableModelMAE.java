package ToxPredictor.Application.GUI.Table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.util.Strings;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Table.Renderer.MultiLineTableHeaderRenderer;
import ToxPredictor.MyDescriptors.DescriptorData;


public class MyTableModelMAE extends AbstractTableModel {

//	protected int m_result = 0;
//	protected int columnsCount = 1;
	
	JTable table;

	//Store as vector of linkedhashmap so that values stay sorted and don't need to keep using complicated reflection to retrieve the values
	Vector<LinkedHashMap<String,String>> vecDD;
	
	Vector<String>descriptorNames=DescriptorData.getDescriptorNames();
	
	int sortCol;
	boolean isSortAsc=true;

	public void addResult(String set,double MAE) {
		// TODO Auto-generated method stub
		
		LinkedHashMap<String, String>map=new LinkedHashMap<>();
		
		map.put("Chemicals", set);
		DecimalFormat df=new DecimalFormat("0.000");
		map.put("Mean absolute error", df.format(MAE));
		vecDD.add(map);

		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		table.repaint();
	}

	public MyTableModelMAE(String [] colNames) {
		vecDD=new Vector<>();
		columnNames=colNames;		
	}
			
	
	/** 
	 * Convert vector to ACS for use by other classes
	 * @return
	 */
	public Vector<LinkedHashMap<String,String>> getPredictions() {
		return vecDD;
	}
	

	private String[] columnNames;

	
	public static String [] getColumnNames () {
		Vector<String>colNames=new Vector<>();
		
		colNames.add("Chemicals");
		colNames.add("Mean absolute error");
		
		String[] array = colNames.toArray(new String[colNames.size()]);
		return array;

	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return vecDD.size();
	}
	
	public void updateRow(LinkedHashMap<String,String>dd,int row) {
		
		vecDD.set(row, dd);
		fireTableDataChanged();

	}
	
	public void setupTable(JTable table) {
		this.table=table;
		
		JTableHeader header = table.getTableHeader();

		header.setUpdateTableInRealTime(true);
		
//		header.addMouseListener(this.new ColumnListener(table));

		table.setModel(this);
		
		MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration enumK = table.getColumnModel().getColumns();
        while (enumK.hasMoreElements())
        {
            ((TableColumn) enumK.nextElement()).setHeaderRenderer(renderer);
        }
		
//		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		DefaultTableCellRenderer enderer = new DefaultTableCellRenderer();
		enderer.setHorizontalAlignment(JLabel.LEFT);
		table.getColumnModel().getColumn(0).setCellRenderer(enderer);
//		table.setRowSelectionAllowed(false);
		table.setRowHeight(25);
		
//		table.addMouseListener(new mouseAdapter());
//		table.addKeyListener(ka);
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		LinkedHashMap<String,String> dd=vecDD.get(row);
		String key=this.columnNames[col];
		if (dd.get(key)==null) return "";
		else return dd.get(key);
	}
	
	
	
	
	public LinkedHashMap<String,String> getPrediction(int row) {
		return vecDD.get(row);
	}

//	class CustomComparator implements Comparator<LinkedHashMap<String,String>>{
//	    int col;
//		
//		CustomComparator(int sortCol) {
//			this.col=sortCol;
//		}
//		
//		public int compare(LinkedHashMap<String,String> ac1,LinkedHashMap<String,String> ac2) {	        
//	    	
//			String key=descriptorNames.get(col);
//			String val1=(String)ac1.get(key);
//	    	String val2=(String)ac2.get(key);
//	    	
//	    	
//	    	if (col==0) {//Index
//	    		return MyTableModel.compareInt(val1,val2);
//	    	} else if (col==1) {//CAS
//	    		return MyTableModel.compareCAS_String(val1, val2);
//	    	} else if (col>=5) {
//	    		return MyTableModel.compareContinuous(val1, val2);
//	    	} else {
//	    		return MyTableModel.compareString(val1, val2);
//	    	}
//	    			    	
//	    	
////	    	System.out.println(val1+"\t"+val2);
//	    		
//	    }
//		
//		String addZeros(String val,int length) {
//			while (val.length()<length) val="0"+val;
//			return val;
//		}
//	}
	
//	public void sortByCol() {						
//		Collections.sort(vecDD,new CustomComparator(sortCol));
//		
////		System.out.println(sortCol);
//		
//		if (!isSortAsc) Collections.reverse(vecDD);
//		
//		fireTableDataChanged();
//	}
	
	public void  removeRow(int row) {	
		vecDD.remove(row);
		fireTableDataChanged();
	}
	
	

	
	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
	public Class getColumnClass(int c) {
		try {
			return String.class;
		} catch (Exception e) {
			return null;
		}
	}

	/*
	 * Don't need to implement this method unless your table's
	 * editable.
	 */
	public boolean isCellEditable(int row, int col) {
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		//			if (col < 2) {
		//				return false;
		//			} else {
		//				return true;
		//			}

		return false;
	}


//	class ColumnListener extends MouseAdapter {
//		protected JTable table;
//
//
//		public ColumnListener(JTable t) {
//			table = t;
//		}
//
//		public void mouseClicked(MouseEvent e) {
//
//			TableColumnModel colModel = table.getColumnModel();
//			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
//
//			int modelIndex = colModel.getColumn(columnModelIndex)
//					.getModelIndex();
//
//			if (modelIndex < 0)
//				return;
//
//			sortCol=modelIndex;
//			isSortAsc = !isSortAsc;
//			//System.out.println(isSortAsc);
//			
//			if (columnModelIndex!=modelIndex) {
//				System.out.println("mismatch!");
//			}
//			sortByCol();
//
//		}
//	}


	/**
	 * Remove all rows from table
	 */
	public void clear() {
		vecDD.removeAllElements();
		fireTableDataChanged();
	}

}