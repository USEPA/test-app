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

import org.apache.logging.log4j.util.Strings;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.TaskCalculations2;
import ToxPredictor.Application.GUI.Table.Renderer.MultiLineTableHeaderRenderer;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Utilities.TESTPredictedValue;

public class MyTableModelAllQSARMethods extends AbstractTableModel {

	protected int m_result = 0;

	protected int columnsCount = 1;
	
	JTable table;

	//TODO- maybe there's a way to use AtomContainerSet - messes up the Collections.sort- need to cast it somehow
	Vector<TESTPredictedValue> vecTPV;
	
	int sortCol;
	boolean isSortAsc=true;

	
	public void addPrediction(TESTPredictedValue tpv) {
		// TODO Auto-generated method stub
		vecTPV.add(tpv);		
	
		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		table.repaint();

	}

	public MyTableModelAllQSARMethods(String [] colNames) {
		vecTPV=new Vector<>();
		columnNames=colNames;		
	}
			
	
	/** 
	 * Convert vector to ACS for use by other classes
	 * @return
	 */
	public Vector<TESTPredictedValue> getPredictions() {
		return vecTPV;
	}
	

	private String[] columnNames = { "#", "ID", "Query","SmilesRan", "Error" };

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return vecTPV.size();
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
		
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
//		table.setRowSelectionAllowed(false);
		
//		table.addMouseListener(new mouseAdapter());
//		table.addKeyListener(ka);
	}

	
	public static String [] getColumnNames (String endpoint,String method) {

		Vector<String>colNames=new Vector<>();
		colNames.add("Index");
		colNames.add("ID");
		colNames.add("Query");
		colNames.add("SmilesRan");
		colNames.add("Error");		

		String units;
		
		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

		if (!TESTConstants.isBinary(endpoint)) {
			if (TESTConstants.isLogMolar(endpoint)) {
				units = molarlogunits;
			} else {
				units = massunits;
			}
		} else {
			units = "";
		}

		colNames.add("Exp"+"\n"+units);

//		if (!units.equals("")) {
//			System.out.println("Note: All values in " + units + "\r\n");//TODO add note below table
//		}
				
		ArrayList<String> methods = TaskCalculations2.getMethods(endpoint);	
		
		for (int i = 0; i < methods.size(); i++) {
			colNames.add("Pred_" + methods.get(i)+"\n"+units);
		}
				
		String[] array = colNames.toArray(new String[colNames.size()]);		
		
//		for (String colName:colNames) {
//			System.out.println(colName);
//		}
		
		return array;

	}

	
	public void updateRow(TESTPredictedValue tpv,int row) {
		
		vecTPV.set(row, tpv);
		fireTableDataChanged();

	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		TESTPredictedValue tpv=vecTPV.get(row);
		return getValue(tpv,col);
	}
	
	
	Object getValue(TESTPredictedValue tpv,int col) {
		Vector<String>values=getValues(tpv);
		return values.get(col);
	}

	Vector<String>getValues(TESTPredictedValue tpv) {
		
		if (!Strings.isEmpty(tpv.error)) {
			return getValuesError(tpv);
		}
						
		return getValuesNoError(tpv);
		
		
	}
	
	Vector<String>getValuesError(TESTPredictedValue tpv) {
		Vector<String>values=new Vector<String>();			
		values.add(tpv.index+"");
		values.add(tpv.id);
		values.add(tpv.query);
		values.add(tpv.smiles);
		values.add(tpv.error);
		
		for (int i=5;i<getColumnCount();i++) {
			values.add("");
		}
		
//		System.out.println(values.size());
		
		return values;
	}
	

	
	
	Vector<String>getValuesNoError(TESTPredictedValue tpv) {
		Vector<String>values=new Vector<String>();
				
		values.add(tpv.index+"");
		values.add(tpv.id);
		values.add(tpv.query);
		values.add(tpv.smiles);
		values.add(tpv.error);
				
		if (tpv.predictionResults!=null) {
			Vector<PredictionIndividualMethod>vecResults=tpv.predictionResults.getIndividualPredictionsForConsensus().getConsensusPredictions();
			PredictionResultsPrimaryTable pt=tpv.predictionResults.getPredictionResultsPrimaryTable();
			
			if (TESTConstants.isLogMolar(tpv.endpoint) || TESTConstants.isBinary(tpv.endpoint)) {
				values.add(pt.getExpToxValue());
			} else {
				values.add(pt.getExpToxValMass());
			}
		
			
			for (PredictionIndividualMethod pred:vecResults) {
				values.add(pred.getPrediction());
			}
			
			if (TESTConstants.isLogMolar(tpv.endpoint) || TESTConstants.isBinary(tpv.endpoint)) {
				values.add(pt.getPredToxValue());				
			} else {
				values.add(pt.getPredToxValMass());
			}

			
		} else {
			System.out.println("No pred results!");
			//TODO
		}
		
//		System.out.println(values.size());
////		
//		for (int i=0;i<values.size();i++) {
//			System.out.println(i+"\t"+values.get(i));
//		}
	
		return values;
		
	}
	
	
	public TESTPredictedValue getPrediction(int row) {
		return (TESTPredictedValue)vecTPV.get(row);
	}

	
	public void sortByCol() {						
		Collections.sort(vecTPV,new CustomComparator(sortCol));
		
//		System.out.println(sortCol);
		
		if (!isSortAsc) Collections.reverse(vecTPV);
		
		fireTableDataChanged();
	}
	
	public void  removeRow(int row) {	
		vecTPV.remove(row);
		fireTableDataChanged();
	}
	
	class CustomComparator implements Comparator<TESTPredictedValue>{
	    int col;
		
		CustomComparator(int sortCol) {
			this.col=sortCol;
		}
		
		public int compare(TESTPredictedValue ac1,TESTPredictedValue ac2) {	        
	    	String val1=(String)getValue(ac1,col);
	    	String val2=(String)getValue(ac2,col);
	    		    			    
	    	if (col==0) {//Index
	    		return MyTableModel.compareInt(val1,val2);
	    	} else if (col==1) {//CAS
	    		return MyTableModel.compareCAS_String(val1, val2);
	    	} else if (col>=5) {//CAS
	    		return MyTableModel.compareContinuous(val1, val2);
	    	} else {
	    		return MyTableModel.compareString(val1, val2);
	    	}
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
//		TESTPredictedValue ac=vecTPV.get(row);
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
		vecTPV.removeAllElements();
		fireTableDataChanged();
	}

}