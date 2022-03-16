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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Application.GUI.Table.Renderer.CellRendererHCD;
import ToxPredictor.Application.GUI.Table.Renderer.VerticalTableHeaderCellRenderer;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;

public class MyTableModelHCD extends AbstractTableModel {

	protected int m_result = 0;

	protected int columnsCount = 1;
	
	JTable table;

	Chemicals chemicals;
	
	int sortCol;
	boolean isSortAsc=true;

	public void addChemical(Chemical chemical) {
		// TODO Auto-generated method stub
		chemicals.add(chemical);		
	
		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		table.repaint();

	}

	public MyTableModelHCD() {
		chemicals=new Chemicals();
		columnNames=getColumnNames();		
	}
			
	
	/** 
	 * Convert vector to ACS for use by other classes
	 * @return
	 */
	public Chemicals getChemicals() {
		return chemicals;
	}
	

	private String[] columnNames;

	public int getColumnCount() {
//		System.out.println(columnNames.length);
		return columnNames.length;
	}

	public int getRowCount() {
		return chemicals.size();
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
		CellRendererHCD c=new CellRendererHCD();
		c.setHorizontalAlignment(JLabel.CENTER);
		
		TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();
		
		for (int i=2;i<table.getColumnModel().getColumnCount();i++) {
			table.getColumn(getColumnName(i)).setCellRenderer(c);
			table.getColumn(getColumnName(i)).setHeaderRenderer(headerRenderer);
		}
		
		table.getTableHeader().setResizingAllowed(true);
//		table.setRowSelectionAllowed(false);
		
//		table.addMouseListener(new mouseAdapter());
//		table.addKeyListener(ka);
	}

	
	public static String [] getColumnNames () {
		Vector<String>colNames=new Vector<>();
		colNames.add("CAS");
		colNames.add("Name");
		colNames.add(Chemical.strAcute_Mammalian_ToxicityOral);
		colNames.add(Chemical.strAcute_Mammalian_ToxicityInhalation);
		colNames.add(Chemical.strAcute_Mammalian_ToxicityDermal);
		
		if (!TESTApplication.forMDH) {
			colNames.add(Chemical.strSkin_Sensitization);
			colNames.add(Chemical.strSkin_Irritation);
			colNames.add(Chemical.strEye_Irritation);			
		}
		colNames.add(Chemical.strCarcinogenicity);
		colNames.add(Chemical.strGenotoxicity_Mutagenicity);
		colNames.add(Chemical.strEndocrine_Disruption);		
		colNames.add(Chemical.strReproductive);
		colNames.add(Chemical.strDevelopmental);
		colNames.add(Chemical.strNeurotoxicity_Repeat_Exposure);
		colNames.add(Chemical.strNeurotoxicity_Single_Exposure);
		colNames.add(Chemical.strSystemic_Toxicity_Repeat_Exposure);
		colNames.add(Chemical.strSystemic_Toxicity_Single_Exposure);
		
		if (!TESTApplication.forMDH) {
			colNames.add(Chemical.strAcute_Aquatic_Toxicity);
			colNames.add(Chemical.strChronic_Aquatic_Toxicity);
			colNames.add(Chemical.strPersistence);
			colNames.add(Chemical.strBioaccumulation);
		}
		
		if (TESTApplication.forMDH) {
			colNames.add(Chemical.strExposureIndividual);
			colNames.add(Chemical.strExposurePopulation);
			colNames.add(Chemical.strExposureChildOrConsumerProducts);
		} else {
			if (TESTApplication.includeExposure) {
				colNames.add(Chemical.strExposure);
			}
		}
		
		
		for(int i=2;i<colNames.size();i++) {
			colNames.set(i, "  "+colNames.get(i));
		}
		
		String[] array = colNames.toArray(new String[colNames.size()]);		
		return array;
	}

	
	public void updateRow(Chemical chemical,int row) {
		
		chemicals.set(row, chemical);
		fireTableDataChanged();

	}

	
	
	public String getColumnName(int col) {
//		System.out.println(col+"\t"+columnNames[col]);
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		Chemical chemical=chemicals.get(row);
		return getValue(chemical,col);
	}
	
	
	Object getValue(Chemical chemical,int col) {
		Vector<String>values=getValues(chemical);
		return values.get(col);
	}

	Vector<String>getValues(Chemical chemical) {
		
//		if (!Strings.isEmpty(chemical.error)) {
//			return getValuesError(chemical);
//		}
						
		return getValuesNoError(chemical);
		
		
	}
	
	Vector<String>getValuesError(Chemical chemical) {
		Vector<String>values=new Vector<String>();			
		values.add(chemical.CAS);
		values.add(chemical.name);
		
		for (int i=2;i<getColumnCount();i++) {
			values.add("");
		}
		
//		System.out.println(values.size());
		
		return values;
	}
	

	
	/**
	 * Get values for a row in table
	 * 
	 * @param chemical
	 * @return
	 */
	Vector<String>getValuesNoError(Chemical chemical) {
		
		Vector<String>values=new Vector<String>();			
		values.add(chemical.CAS);
		values.add(chemical.name);
		
		
		for (int i=2;i<columnNames.length;i++) {	
			Score score=chemical.getScore(columnNames[i].trim());
			
//			System.out.println(columnNames[i]);
			
//			if (score.final_score==null) System.out.println(score.hazard_name);
			
			String final_score=score.final_score;
			String hazard_name=score.hazard_name;			
			
			if (score.records.size()>0) {
				if (final_score.contentEquals("N/A")) final_score="I";			
//				fw.write("\t\t<td bgcolor="+getColor(final_score)+" align=center width="+width+"px><a href=\""+relativePathRecordFolder+"/"+hazard_name+"_"+chemical.CAS+".html\">"+final_score+"</a></td>\r\n");
				values.add(final_score);
			} else {
//				fw.write("\t\t<td bgcolor=white align=center width="+width+"px><br></td>\r\n");
				if (final_score.contentEquals("N/A")) values.add("");
				else values.add(final_score);
			}
		}
		
		return values;
		
	}
	
	
	public Chemical getChemical(int row) {
		if (row>=chemicals.size()) return null;
		return chemicals.get(row);
	}

	
	public void sortByCol() {						
		Collections.sort(chemicals,new CustomComparator(sortCol));
		
//		System.out.println(sortCol);
		
		if (!isSortAsc) Collections.reverse(chemicals);
		
		fireTableDataChanged();
	}
	
	public void  removeRow(int row) {	
		chemicals.remove(row);
		fireTableDataChanged();
	}
	
	class CustomComparator implements Comparator<Chemical>{
	    int col;
		
		CustomComparator(int sortCol) {
			this.col=sortCol;
		}
		
		public int compare(Chemical ac1,Chemical ac2) {	        
	    	String val1=(String)getValue(ac1,col);
	    	String val2=(String)getValue(ac2,col);
	    		    			    
	    	if (columnNames[col].equals("CAS")) {//CAS
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
		chemicals.clear();
		fireTableDataChanged();
	}

}