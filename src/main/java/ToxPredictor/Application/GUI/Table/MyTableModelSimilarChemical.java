package ToxPredictor.Application.GUI.Table;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.logging.log4j.util.Strings;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Table.Renderer.CellRendererHCD;
import ToxPredictor.Application.GUI.Table.Renderer.CellRendererSimilarChemical;
import ToxPredictor.Application.GUI.Table.Renderer.MultiLineTableHeaderRenderer;
import ToxPredictor.Application.GUI.Table.Renderer.VerticalTableHeaderCellRenderer;
import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.MyDescriptors.DescriptorData;


public class MyTableModelSimilarChemical extends AbstractTableModel {

//	protected int m_result = 0;
//	protected int columnsCount = 1;
	
	JTable table;

	//Store as vector of linkedhashmap so that values stay sorted and don't need to keep using complicated reflection to retrieve the values
	Vector<LinkedHashMap<String,String>> vecDD;
	
	
	int sortCol;
	boolean isSortAsc=true;
	public String [] columnNames;
	
	public void addPrediction(SimilarChemical sc) {
		// TODO Auto-generated method stub
		
//		System.out.println(sc.getImageUrl());
		vecDD.add(sc.convertToLinkedHashMap());
		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		table.repaint();
	}

	public MyTableModelSimilarChemical(String [] columnNames) {
		vecDD=new Vector<>();
		this.columnNames=columnNames;		
	}
			
	
	/** 
	 * Convert vector to ACS for use by other classes
	 * @return
	 */
	public Vector<LinkedHashMap<String,String>> getPredictions() {
		return vecDD;
	}
	

	public static String [] getColumnNames () {
		Vector<String>names=new Vector<>();
		
		names.add("CAS");
		names.add("Structure");
		names.add("Similarity");
		names.add("Experimental value");
		names.add("Predicted value");
		String[] array = names.toArray(new String[names.size()]);
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
		
		header.addMouseListener(this.new ColumnListener(table));

		table.setModel(this);
		
		MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration enumK = table.getColumnModel().getColumns();
        while (enumK.hasMoreElements())
        {
            ((TableColumn) enumK.nextElement()).setHeaderRenderer(renderer);
        }
		
//		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
//		table.setRowSelectionAllowed(false);
		
		
		CellRendererSimilarChemical crsc=new CellRendererSimilarChemical();
		table.getColumnModel().getColumn(2).setCellRenderer(crsc);
		
//		table.addMouseListener(new mouseAdapter());
//		table.addKeyListener(ka);
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		LinkedHashMap<String,String> dd=vecDD.get(row);
		String key=columnNames[col];
		if (dd.get(key)==null) return "";
		else {
			
			if (key.equals("Structure")) {
				try {
					ImageIcon imageIcon=new ImageIcon(new URL(dd.get(key)));
					Image image = imageIcon.getImage(); // transform it 
					Image newimg = image.getScaledInstance(100, 100,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
					imageIcon = new ImageIcon(newimg);  // transform it back
					return imageIcon;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					return "Image not available";
				}
			} else {
//				System.out.println(dd.get(key));
				return dd.get(key);	
			}
			
			
		}
	}
	
	
	public LinkedHashMap<String,String> getPrediction(int row) {
		return vecDD.get(row);
	}

	class CustomComparator implements Comparator<LinkedHashMap<String,String>>{
	    int col;
		
		CustomComparator(int sortCol) {
			this.col=sortCol;
		}
		
		public int compare(LinkedHashMap<String,String> ac1,LinkedHashMap<String,String> ac2) {	        
	    	
			String key=columnNames[col];
			String val1=(String)ac1.get(key);
	    	String val2=(String)ac2.get(key);
	    	
	    	
	    	if (col==0) {//Index
	    		return MyTableModel.compareString(val1, val2);
	    	} else if (col>=2) {//CAS
	    		return MyTableModel.compareContinuous(val1, val2);	    	
	    	} else {
	    		return 0;
	    	}
	    			    	
	    	
//	    	System.out.println(val1+"\t"+val2);
	    		
	    }
		
		String addZeros(String val,int length) {
			while (val.length()<length) val="0"+val;
			return val;
		}
	}
	
	public void sortByCol() {						
		Collections.sort(vecDD,new CustomComparator(sortCol));
		
//		System.out.println(sortCol);
		
		if (!isSortAsc) Collections.reverse(vecDD);
		
		fireTableDataChanged();
	}
	
	public void  removeRow(int row) {	
		vecDD.remove(row);
		fireTableDataChanged();
	}
	
	
	public void updateRowHeights()
	{
	    for (int row = 0; row < table.getRowCount(); row++)
	    {
	        int rowHeight = table.getRowHeight();

	        for (int column = 0; column < table.getColumnCount(); column++)
	        {
	            Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
	            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
	        }

	        table.setRowHeight(row, rowHeight);
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
			if (columnNames[c].equals("Structure")) {
				return ImageIcon.class; 
			} else {
				return String.class;
			}

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
			updateRowHeights();

		}
	}


	/**
	 * Remove all rows from table
	 */
	public void clear() {
		vecDD.removeAllElements();
		fireTableDataChanged();
	}

}