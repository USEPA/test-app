package ToxPredictor.Application.GUI;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Application.GUI.MyTableModel.ColumnListener;
//import ToxPredictor.Application.GUI.PanelResults.mouseAdapter;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Utilities.TESTPredictedValue;

public class MyTableModel extends AbstractTableModel {

	protected int m_result = 0;

	protected int columnsCount = 1;
	
	JTable table;

	//TODO- maybe there's a way to use AtomContainerSet - messes up the Collections.sort- need to cast it somehow
	Vector<TESTPredictedValue> vecTPV;
	
	int sortCol;
	boolean isSortAsc=true;

	MyTableModel(String [] colNames) {
		vecTPV=new Vector<>();
		columnNames=colNames;		
	}

	
	public void addPrediction(TESTPredictedValue tpv) {
		// TODO Auto-generated method stub
		vecTPV.add(tpv);
		
		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		table.repaint();
	}

			
	
	/** 
	 * Convert vector to ACS for use by other classes
	 * @return
	 */
	public Vector<TESTPredictedValue> getPredictions() {
		return vecTPV;
	}
	

	private String[] columnNames = { "#", "ID", "Query","SmilesRan", "Error" };

	
	public static String [] getColNames (String endpoint,String method) {

		Vector<String>colNames=new Vector<>();
		colNames.add("Index");
		colNames.add("ID");
		colNames.add("Query");
		colNames.add("SmilesRan");
		colNames.add("Error");
		
		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

		if (!TESTConstants.isBinary(endpoint)) {
			if (TESTConstants.isLogMolar(endpoint)) {
				colNames.add("Exp_Value:\n" + molarlogunits);
				colNames.add("Pred_Value:\n" + molarlogunits);
				
			}

			colNames.add("Exp_Value:\n" + massunits);
			colNames.add("Pred_Value:\n" + massunits);

			if (method.equals(TESTConstants.ChoiceLDA)) {
				colNames.add("Experimental MOA");
				colNames.add("Predicted MOA");
			}

		} else {
			colNames.add("Exp_Value");
			colNames.add("Pred_Value");
			colNames.add("Exp_Result");
			colNames.add("Pred_Result");
		}
		
		String[] array = colNames.toArray(new String[colNames.size()]);
		
		return array;

	}
	
	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return vecTPV.size();
	}
	
	public void updateRow(TESTPredictedValue tpv,int row) {
		
		TESTPredictedValue tpvOld=vecTPV.get(row);
		
		vecTPV.set(row, tpv);
		fireTableDataChanged();

	}
	
	void setupTable(JTable table) {
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
		
		String endpoint=tpv.predictionResults.getEndpoint();
		boolean binary=TESTConstants.isBinary(endpoint);
		
		if (binary) {
			return getValuesBinary(tpv);
		} else {
			return getValuesContinuous(tpv);
		}
		
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
		return values;
	}
	

	Vector<String>getValuesBinary(TESTPredictedValue tpv) {

		Vector<String>values=new Vector<String>();
		
		values.add(tpv.index+"");
		values.add(tpv.id);
		values.add(tpv.query);
		values.add(tpv.smiles);
		values.add(tpv.error);

		String endpoint=tpv.endpoint;
				
		if (tpv.predictionResults!=null) {
			PredictionResultsPrimaryTable pt=tpv.predictionResults.getPredictionResultsPrimaryTable();
			values.add(pt.getExpToxValue());
			values.add(pt.getPredToxValue());
			values.add(pt.getExpToxValueEndpoint());
			values.add(pt.getPredValueEndpoint());

		} else {//Does this happen?
			if (TESTConstants.isLogMolar(endpoint)) {
				values.add("N/A");//exp LDA score
				values.add("N/A");//pred LDA score
				values.add("N/A");//exp LDA binary
				values.add("N/A");//pred LDA binary
			}
		}
		
		return values;
	}
	
	
	Vector<String>getValuesContinuous(TESTPredictedValue tpv) {
		Vector<String>values=new Vector<String>();
		
		values.add(tpv.index+"");
		values.add(tpv.id);
		values.add(tpv.query);
		values.add(tpv.smiles);
		values.add(tpv.error);
		
		String endpoint=tpv.endpoint;
		String method=tpv.method;

		if (tpv.predictionResults!=null) {

			PredictionResultsPrimaryTable p=tpv.predictionResults.getPredictionResultsPrimaryTable();
	
			if (TESTConstants.isLogMolar(endpoint)) {
				values.add(p.getExpToxValue());
				values.add(p.getPredToxValue());
			}
		
			values.add(p.getExpToxValMass());
			values.add(p.getPredToxValMass());
			
			if (method.equals(TESTConstants.ChoiceLDA)) {
				values.add(p.getExpMOA());
				values.add(p.getPredMOA());
			}
		
		} else {//TODO does this ever happen?
			if (TESTConstants.isLogMolar(endpoint)) {
				if (tpv.expValLogMolar.isNaN()) {
					values.add("N/A");
				} else {
					values.add(tpv.expValLogMolar+"");
				}
				values.add("N/A");
			}
		
			String strExpValMass=PredictToxicityJSONCreator.getToxValMass(TESTConstants.isLogMolar(endpoint), tpv.expValMass);
			values.add(strExpValMass);
			values.add("N/A");//predValMass
			
			if (method.equals(TESTConstants.ChoiceLDA)) {
				values.add("N/A");
				values.add("N/A");
			}
		}
		
		return values;
		
	}
	
	
	public TESTPredictedValue getPrediction(int row) {
		return (TESTPredictedValue)vecTPV.get(row);
	}

	public static int compareInt(String val1,String val2) {
		int ival1=Integer.parseInt(val1);
		int ival2=Integer.parseInt(val2);
		return ival1-ival2;

	}
	
	public static int compareCAS_String(String val1,String val2) {
		return addZeros(val1,15).compareTo(addZeros(val2, 15));
	}
	
	public static int compareString(String val1,String val2) {
		return val1.compareTo(val2);	
	}

	public static int compareContinuous (String val1,String val2) {
		
		
		if (Strings.isEmpty(val1) || val1.contentEquals("N/A")) return 1;
		if (Strings.isEmpty(val2) || val2.contentEquals("N/A")) return -1;
		
		if (val1.contentEquals(val2)) return 0;
				
		double dval1=Double.parseDouble(val1);
		double dval2=Double.parseDouble(val2);
		
		if (dval1>dval2) return -1;
		else if (dval1<dval2) return 1;
		else return 0;
	}
	
	
	public static String addZeros(String val,int length) {
		while (val.length()<length) val="0"+val;
		return val;
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

	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	public void setValueAt(Object value, int row, int col) {

		TESTPredictedValue ac=vecTPV.get(row);

//		if (col==0) {
//			ac.setProperty("Index", value);
//		} else if (col==1) {
//			ac.setProperty("CAS", value);
//		} else if (col==2) {
//			ac.setProperty("Name", value);
//		} else if (col==3) {
//			ac.setProperty("Formula", value);
//		} else if (col==4) {
//			ac.setProperty("Error", value);
//		} else {
//			
//		}
		fireTableCellUpdated(row, col);

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

		}
	}

	public void addPredictions(Vector<TESTPredictedValue> preds) {
//		int newIndex=-1;
//
//		if (vecTPV.size()>0) {
//			//determine max index:
//			int maxIndex=-1;
//
//			Iterator<AtomContainer> iterator=vecTPV.iterator();
//			
//			while (iterator.hasNext()) {
//				AtomContainer ac=iterator.next();
//				int index=(Integer)ac.getProperty("Index");
//				if (index>maxIndex) maxIndex=index;
//			}
//			newIndex=maxIndex+1;
//		
//		} else 
//			newIndex=1;
			

		for (int i=0;i<preds.size();i++) {
			TESTPredictedValue ac=preds.get(i);
//			ac.setProperty("Index", newIndex);
			vecTPV.add(ac);
//			System.out.println(ac.index);
//			newIndex++;
		}			
		
//		System.out.println(htMols.size());
		fireTableDataChanged();
		table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
		int selectedRow=vecTPV.size()-1;
//		System.out.println(moleculeSet.getAtomContainerCount());
		if (selectedRow>-1)
			table.addRowSelectionInterval(selectedRow ,selectedRow);
		
	}

	/**
	 * Remove all rows from table
	 */
	public void clear() {
		vecTPV.removeAllElements();
		fireTableDataChanged();
	}

}