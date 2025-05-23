package ToxPredictor.Application.GUI.Table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ToxPredictor.Application.GUI.Table.Renderer.CellRendererHCD;
import ToxPredictor.Application.GUI.Table.Renderer.CellRendererHCD_ScoreRecords;
import gov.epa.api.ScoreRecord;



public class MyTableModelHCD_ScoreRecords extends AbstractTableModel {

	protected int m_result = 0;

	protected int columnsCount = 1;
	
	private static final String [] fieldNames=ScoreRecord.allFieldNames;
	
	JTable table;

	//TODO- maybe there's a way to use AtomContainerSet - messes up the Collections.sort- need to cast it somehow
	Vector<ScoreRecord> records;
	
	int sortCol;
	boolean isSortAsc=true;

	
	public void addScoreRecord(ScoreRecord scoreRecord) {
		// TODO Auto-generated method stub
		records.add(scoreRecord);		
		
//		System.out.println(scoreRecord.CAS+"\t"+scoreRecord.score);
		
	
		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		table.repaint();

	}

	public MyTableModelHCD_ScoreRecords() {
		records=new Vector<ScoreRecord>();	
	}
			
	
	/** 
	 * Convert vector to ACS for use by other classes
	 * @return
	 */
	public  Vector<ScoreRecord> getRecords() {
		return records;
	}
	

	public int getColumnCount() {
//		System.out.println(columnNames.length);
		return fieldNames.length;
	}

	public int getRowCount() {
		return records.size();
	}
	
	
	public void setupTable(JTable table) {
		this.table=table;
		
		JTableHeader header = table.getTableHeader();

		header.setUpdateTableInRealTime(true);
		
		header.addMouseListener(this.new ColumnListener(table));

		table.setModel(this);
		
//		MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
//        Enumeration enumK = table.getColumnModel().getColumns();
//        while (enumK.hasMoreElements())
//        {
//            ((TableColumn) enumK.nextElement()).setHeaderRenderer(renderer);
//        }
//		
//		table.getColumnModel().getColumn(0).setPreferredWidth(15);
//		
//		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
//		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
//		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		
//		System.out.println(table.getColumnModel().getColumnCount());
		
		CellRendererHCD_ScoreRecords c=new CellRendererHCD_ScoreRecords();
		c.setHorizontalAlignment(JLabel.CENTER);
		table.getColumn("score").setCellRenderer(c);
		
//		table.setRowSelectionAllowed(false);
		
//		table.addMouseListener(new mouseAdapter());
//		table.addKeyListener(ka);
	}

	
	public static String [] getColumnNames () {
		return fieldNames;
	}

	
	public void updateRow(ScoreRecord scoreRecord,int row) {
		
		records.set(row, scoreRecord);
		fireTableDataChanged();

	}
	public String getLink(int row) {
//		System.out.println("records.size="+records.size());
		return records.get(row).url;
	}
	
	public String getColumnName(int col) {
//		System.out.println(col+"\t"+columnNames[col]);
		return fieldNames[col];
	}

	public Object getValueAt(int row, int col) {
		ScoreRecord scoreRecord=getScoreRecord(row);
		return getValue(scoreRecord, col);
	}
	
	
	Object getValue(ScoreRecord scoreRecord,int col) {
		return scoreRecord.getValue(fieldNames[col]);		
	}

	public ScoreRecord getScoreRecord(int row) {
		return records.get(row);
	}

	
	public void sortByCol() {						
		Collections.sort(records,new CustomComparator(sortCol));
		
//		System.out.println(sortCol);
		
		if (!isSortAsc) Collections.reverse(records);
		
		fireTableDataChanged();
	}
	
	public void  removeRow(int row) {	
		records.remove(row);
		fireTableDataChanged();
	}
	
	class CustomComparator implements Comparator<ScoreRecord>{
	    int col;
		
		CustomComparator(int sortCol) {
			this.col=sortCol;
		}
		
		public int compare(ScoreRecord ac1, ScoreRecord ac2) {	        
	    	String val1=(String)getValue(ac1,col);
	    	String val2=(String)getValue(ac2,col);
	    		    			    
	    	if (fieldNames[col].equals("CAS")) {//CAS
	    		return MyTableModel.compareCAS_String(val1, val2);
	    	} else {
	    		return MyTableModel.compareString(val1, val2);
	    	}

//	    	if (col==0) {//Index
//	    		return MyTableModel.compareInt(val1,val2);
//	    	} else if (col==1) {//CAS
//	    		return MyTableModel.compareCAS_String(val1, val2);
//	    	} else if (col>=5) {//CAS
//	    		return MyTableModel.compareContinuous(val1, val2);
//	    	} else {
//	    		return MyTableModel.compareString(val1, val2);
//	    	}
	    }
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

//	/*
//	 * Don't need to implement this method unless your table's
//	 * data can change.
//	 */
//	public void setValueAt(Object value, int row, int col) {
//
//		TESTPredictedValue ac=vecchemical.get(row);
//
////		if (col==0) {
////			ac.setProperty("Index", value);
////		} else if (col==1) {
////			ac.setProperty("CAS", value);
////		} else if (col==2) {
////			ac.setProperty("Name", value);
////		} else if (col==3) {
////			ac.setProperty("Formula", value);
////		} else if (col==4) {
////			ac.setProperty("Error", value);
////		} else {
////			
////		}
//		fireTableCellUpdated(row, col);
//
//	}

	class ColumnListener extends MouseAdapter {
		protected JTable table;


		public ColumnListener(JTable t) {
			table = t;
		}

		public void mouseClicked(MouseEvent e) {

			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());

			int modelIndex = colModel.getColumn(columnModelIndex)
					.getModelIndex();

			if (modelIndex < 0)
				return;

			sortCol=modelIndex;
			isSortAsc = !isSortAsc;
			//System.out.println(isSortAsc);
			
			if (columnModelIndex!=modelIndex) {
				System.out.println("mismatch!");
			}
			sortByCol();

		}
	}

	

	/**
	 * Remove all rows from table
	 */
	public void clear() {
		records.clear();
		fireTableDataChanged();
	}

}