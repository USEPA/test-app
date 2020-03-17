package ToxPredictor.Application.GUI;


import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Comparator;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;

//import org.openscience.cdk.MoleculeSet;
//import org.openscience.cdk.interfaces.IMolecule;
//import org.openscience.cdk.interfaces.IMoleculeSet;
//import org.openscience.cdk.tools.HydrogenAdder;
//import org.openscience.cdk.tools.MFAnalyser;

import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.Utilities;
import gov.epa.api.Chemical;
//import ToxPredictor.misc.ParseChemidplus;
import gov.epa.api.ScoreRecord;
import uk.ac.ebi.beam.Element;

import javax.swing.table.JTableHeader;

public class PanelBatchChemicals extends JPanel {

	/**
	 * @param args
	 */

	public boolean Locked=false;
	
	DialogEditChemical myfraEditChemical=new DialogEditChemical(); 
	
	JTable table = new JTable();

	JScrollPane scrollPane;
	keyAdapter ka=new keyAdapter();

	int inset=20;
	
	int sortCol;
	boolean isSortAsc=true;
	

	public PanelBatchChemicals() {
	}
	
	class bob {
		IAtomContainer m;
		Object key;
	}
	
//	public void SortCol(int Col,boolean isSortAsc) {
//		
//		this.getParent().setCursor(Utilities.waitCursor);
//		
////		Comparator c=null;
////		
////		if (Col==1) {
////			c=new MyComparatorCAS(isSortAsc);
////		} else {
////			c=new MyComparator2(isSortAsc);	
////		}
//
//		Comparator c= new MyComparator2(isSortAsc);	
//		
//	    ArrayList l=new ArrayList();
//	    
//	    for (int i=0;i<moleculeSet.getAtomContainerCount();i++) {
//	    	Object key=table.getValueAt(i, Col);
//	    	IAtomContainer m=moleculeSet.getAtomContainer(i);
//	    	
//	    	bob newbob=new bob();
//	    	newbob.m=m;
//	    	newbob.key=key;
//	    	
//	    	l.add(newbob);
//	    }
//
//	    
//	    AtomContainerSet newMoleculeSet=new AtomContainerSet();
//	    
//	    Collections.sort(l,c);
//	    
//	    for (int i=0;i<l.size();i++) {
//	    	bob bobi=(bob)l.get(i);
//	    		    	
//	    	IAtomContainer mi=bobi.m;
//	    	
//	    	newMoleculeSet.addAtomContainer(mi);
//	    }
//	    
//	    this.SetupTable(newMoleculeSet);
//	    
//	    this.getParent().setCursor(Utilities.defaultCursor);
//		
//	}
	
	
	
	/**
	 * Bases size of scroll pane on the size on the fraMain screen
	 * @param width
	 * @param height
	 */
	public void init(int width,int height) {

		//		double SF=(double)DisplaySize/(double)ImageSize;
//		System.out.println(width+"\t"+height);
		
		
		this.setSize(width, height); // need space for controls at bottom

		//		Utilities.SetFonts(this.getContentPane());
		Utilities.SetFonts(this);

		//		this.getContentPane().setLayout(null);		
		this.setLayout(null);

//		setBorder(BorderFactory.createLineBorder(Color.black));
		
		TitledBorder border = new TitledBorder("Batch list of chemicals");
	    border.setTitleJustification(TitledBorder.LEFT);
	    border.setTitlePosition(TitledBorder.TOP);
	    this.setBorder(border);

		

		//		this.setModal(true);
		//		this.setResizable(false);

		//		jbOK.setSize(80,20);
		//		jbOK.setLocation(450,530);
		//		jbOK.setText("OK");
		//		jbOK.setActionCommand("jbOK");
		//		
		//		jbCancel.setSize(80,20);
		//		jbCancel.setLocation(350,530);
		//		jbCancel.setText("Cancel");
		//		jbCancel.setActionCommand("jbCancel");

		//Create the scroll pane and add the table to it.
		scrollPane = new JScrollPane(table);
		
//		scrollPane=new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		
		scrollPane.setLocation(inset, inset); //shift it over a bit
		scrollPane.setSize(width-2*inset, height-2*inset);
//		scrollPane.setVisible(false);

		//Add the scroll pane to this panel.
		add(scrollPane);
		//		add(jbOK);
		//		add(jbCancel);

		//		jbOK.addActionListener(aa);
		//		jbCancel.addActionListener(aa);
		//	    this.addWindowListener(wa);

		//		this.addWindowFocusListener(wl);
		//		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//		this.setTitle("Chemicals in batch file");
		//		
		//		ToxPredictor.Utilities.Utilities.CenterFrame(this);

		JTableHeader header = table.getTableHeader();
		header.setUpdateTableInRealTime(true);
		
		MyTableModel tableModel=new MyTableModel();
		header.addMouseListener(tableModel.new ColumnListener(table));

		table.setModel(tableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		

		table.addMouseListener(new mouseAdapter());
		table.addKeyListener(ka);

		
		int editWidth=(int)(0.9*width);
		int editHeight=(int)(0.9*height);
		
		if (editWidth<600) {
			Dimension scrnsize = Toolkit.getDefaultToolkit().getScreenSize();
			editHeight=(int)(scrnsize.height*0.75);
			editWidth=(int)(scrnsize.width*0.75);
		}
		
		myfraEditChemical.init(editWidth,editHeight);
		myfraEditChemical.setParentFrame(this);
		
//		SetupTable(new AtomContainerSet()); 
		
		
		
	}


	
	
	
	//	public void SetParentFrame(	Object gui) {
	//		this.gui=gui;
	//		
	//	}
	
	/**
	 * Convenience method so dont need to access model from other classes
	 * 
	 * @param moleculeSet
	 */
	public void addMoleculesToTable(AtomContainerSet moleculeSet) {
		MyTableModel tableModel=(MyTableModel)table.getModel();
		tableModel.addAtomContainers(moleculeSet);
	}

	
	/**
	 * Convenience method so dont need to access model from other classes
	 * 
	 * @param moleculeSet
	 */
	public AtomContainerSet getMolecules() {
		MyTableModel tableModel=(MyTableModel)table.getModel();
		return tableModel.getAtomContainerSet();
	}
//	Vector<DataRow> convertToVector(Hashtable <Integer,AtomContainer>acs) {
//		
//		Vector<DataRow> data=new Vector<>();
//		if (acs==null) return data;
//		
//		Set<Integer> keys = acs.keySet();
//        
//		int count=0;
//		for(Integer key: keys){
//			
//			AtomContainer molecule = acs.get(key);
//			
//			Integer Index=molecule.getProperty("Index");
//			if (Index==null) { 
//				Index=new Integer(count+1);
//				molecule.setProperty("Index", Index);
//			}
//			
//			DataRow row = createDataRow(molecule, Index);
//			data.add(row);
//			count++;
//
//		}
//		return data;
//	}


//	private void createDataRow(AtomContainer molecule, Integer Index) {
//		String formula = "N/A";
//		try {
//			MolecularFormula mf=(MolecularFormula) MolecularFormulaManipulator.getMolecularFormula(molecule);
//			formula=MolecularFormulaManipulator.getString(mf);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
////			if (formula == null)
////				formula = "N/A";
//
//		String error = (String) molecule.getProperty("Error");
//		String CAS = (String) molecule.getProperty("CAS");
//		
////			System.out.println(Index);
//		DataRow row=new DataRow(Index,CAS,formula,error);
//		return row;
//	}
	
//	private void SetupTable(AtomContainerSet acs) {
//		Hashtable<Integer,AtomContainer> htMols=new Hashtable<>();
//
//		for (int i=0;i<acs.getAtomContainerCount();i++) {
//			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
//			Integer key=ac.getProperty("Index");
//			htMols.put(key, ac);
//		}
//		SetupTable(htMols);
//	}
	
	
//	private void SetupTable() {
//		
//		try {
//			MyTableModel myTableModel=new MyTableModel();
//			table.setModel(myTableModel);
//			table.getColumnModel().getColumn(0).setPreferredWidth(50);
//			table.getColumnModel().getColumn(3).setPreferredWidth(200);
//			table.getTableHeader().setReorderingAllowed(false);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		if (htMols.size()>0) {
//			try {
//				table.scrollRectToVisible(table.getCellRect(table.getModel().getRowCount() - 1, 0, true));
//			} catch (Exception ex) {
//				System.out.println(ex.getMessage());
//			}
//		}
//
//	}
	
	public void showCell(int row, int column,JTable table) {
		Rectangle rect = table.getCellRect(row, column, true);
		scrollRectToVisible(rect);
	    table.clearSelection();
	    table.setRowSelectionInterval(row, row);
	    
	}

	//	 This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(java.awt.Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image
					.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image
					.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	// This method returns true if the specified image has transparent pixels
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}


	public void configureFraEditChemical () {
		
		if (Locked) {
			JOptionPane.showMessageDialog(this,"Click Stop first to view/edit a chemical");
			return;
		}
		
		MyTableModel model=(MyTableModel)table.getModel();
		IAtomContainer molecule=model.getAtomContainer(table.getSelectedRow());
		
//		System.out.println(molecule==null);
		
		String error=(String)molecule.getProperty("Error");
		if (error.equals("Molecules can only contain one fragment")) {
			JOptionPane.showMessageDialog(this, "Cannot display structure for multiple fragments");
			return;
		}

		this.getParent().setCursor(Utilities.waitCursor);
		
//		System.out.println("Sel row="+table.getSelectedRow());
		
		AtomContainer molClone;
		
		try {
			molClone=(AtomContainer)molecule.clone();
			
			myfraEditChemical.configureModel(molClone,table.getSelectedRow(),molecule.getProperty("Index"));
			myfraEditChemical.jtfCAS.setText(molecule.getProperty("CAS"));
			myfraEditChemical.cleanUpStructure();
			
			myfraEditChemical.setVisible(true);
			this.getParent().setCursor(Utilities.defaultCursor);

//			brute force way to fix things- otherwise sometimes changes to molecule will also mess up another molecule:
			//TODO: figure out a way to clear out model from jcpep so dont need to waste time
//			long time1=System.currentTimeMillis();
//			myfraEditChemical= new fraEditChemical7();
//			
//			long time2=System.currentTimeMillis();
//			
//			int width=(int)(0.9*this.getParent().getWidth());
//			int height=(int)(0.9*this.getParent().getHeight());
//			myfraEditChemical.init(width, height);
//
//			double timediff=(time2-time1)/1000.0;
////			System.out.println(timediff);
//			
//			myfraEditChemical.setVisible(true);
//			myfraEditChemical.setParentFrame(this);
//			
//			myfraEditChemical.configureModel(molClone,table.getSelectedRow(),molecule.getProperty("Index"));
//			myfraEditChemical.jtfCAS.setText(molecule.getProperty("CAS"));
			
//			myfraEditChemical.setNameCAS(molecule);
//			this.getParent().setCursor(Utilities.defaultCursor);

			
//			if (molClone.getAtomCount()==0) {
//				JOptionPane.showMessageDialog(this, "Structure contains no atoms, either add a structure or click Cancel");
//			}
		
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error in opening chemical");
		}
	}
	
	public static void main(String[] args) {

		String StructureFolder="ValidatedStructures2d";
		
		ChemicalFinder cf = new ChemicalFinder(
				StructureFolder+"/"+"manifest.txt", StructureFolder);

		
		PanelBatchChemicals f = new PanelBatchChemicals();

		//    	String folder="ToxPredictor/DescriptorTextTables/Cancer_MR_amines data files";
		//    	String filename="Cancer_MR_amines.sdf";

		//    	IMoleculeSet moleculeSet=f.LoadFromSDF(folder+"/"+filename);

		//    	IMoleculeSet moleculeSet=f.LoadFromSDF("test.sdf");
		AtomContainerSet moleculeSet = ToxPredictor.misc.MolFileUtilities.LoadFromCASList("testCAS.txt",cf);
		
		
		f.addMoleculesToTable(moleculeSet);

		JFrame jf = new JFrame();
		jf.getContentPane().setLayout(null);
		jf.setSize(600, 600);
		jf.add(f);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	
	
	public void jbOK_actionPerformed(ActionEvent e) {

		//    	String CAS=(String)caslist.get(table.getSelectedRow());
		//    	IMolecule molecule=cf.FindChemicalFromCAS(CAS);
		//    	
		//		if (molecule !=null) {			
		//			molecule.setProperty("CAS",CAS);
		//			
		//			if (gui instanceof fraMain5) {
		//				((fraMain5)gui).configureModel(molecule);;
		//				((fraMain5)gui).jtfCAS.setText(CAS);
		//			} else {
		//				((ToxApplet5)gui).configureModel(molecule);;
		//				((ToxApplet5)gui).jtfCAS.setText(CAS);
		//			}
		//
		//			
		//		} else {
		//			JOptionPane.showMessageDialog(this,"Failed to load molecule");
		//		}
		//    	
		//		if (gui instanceof fraMain5) {
		//			((fraMain5)gui).myfraStructureDatabaseSearch.setVisible(false);			
		//		} else {
		//			((ToxApplet5)gui).myfraStructureDatabaseSearch.setVisible(false);
		//		}
		//
		////		this.myfraMain5.myfraStructureDatabaseSearch.setVisible(false);//bob
		//    	this.setVisible(false);

	}

	//    double GetMW(IMolecule molecule){
	//    	try {
	//    		MFAnalyser mf=new MFAnalyser(molecule);
	//    		return mf.getCanonicalMass();
	//    		
	//    	} catch (Exception e) {
	//    		return -1;
	//    	}
	//    	
	//    }

	class windowAdapter extends java.awt.event.WindowAdapter {

		public void windowClosing(java.awt.event.WindowEvent e) {
			setVisible(false);
		}
	}

	
	/**
	 * Method that handles when jbDelete is clicked.
	 * This method deletes a chemical in the batch table
	 */
	void jbDelete_actionPerformed() {
		
		
		int [] selrows=table.getSelectedRows();

		if (selrows==null || selrows.length==0) {
			JOptionPane.showMessageDialog(getParent(), "Select one or more rows first");
			return;
		}

		MyTableModel model=(MyTableModel)table.getModel();
		
		
		if (model.vecAC.size()==0) {
			JOptionPane.showMessageDialog(getParent(), "No chemical to delete");
			return;
		}

		
		int deleteCount=0;
		for (int i=0;i<selrows.length;i++) {
			//		System.out.println(selrows[i]);
//			acs.removeAtomContainer(selrows[i]-deleteCount);
			model.removeRow(selrows[i]-deleteCount);
			deleteCount++;			
		}

//		if (f.panelBatch.moleculeSet.getAtomContainerCount()==0) {			
//			switchToSingleChemicalMode();
//		}

		
		if (table.getRowCount()==0) return;
		
		int selectedRow=selrows[0];
		if (selectedRow<table.getRowCount()) {
			table.addRowSelectionInterval(selectedRow, selectedRow);
		} else if (selectedRow==table.getRowCount()) {
			table.addRowSelectionInterval(selectedRow-1, selectedRow-1);
		}


	}
	
	/**
	 * Used to detect if the delete key is clicked (triggers delete event) or if
	 * enter key is pressed (searchs by CAS in molecule id text box)
	 */
	class keyAdapter extends java.awt.event.KeyAdapter {

		public void keyReleased(KeyEvent e) {
			Object o = e.getSource();

			if (e.getKeyCode() == e.VK_DELETE) {
				jbDelete_actionPerformed();
			}
			
		}
	}
	class mouseAdapter implements java.awt.event.MouseListener {

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount()==2) {
				configureFraEditChemical();
			}
			
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

	}

	
	class MyTableModel extends AbstractTableModel {

		protected int m_result = 0;

		protected int columnsCount = 1;

		//TODO- maybe there's a way to use AtomContainerSet - messes up the Collections.sort- need to cast it somehow
		Vector<AtomContainer> vecAC;

		MyTableModel() {
			vecAC=new Vector<>();
		}
		
		/** 
		 * Convert vector to ACS for use by other classes
		 * @return
		 */
		public AtomContainerSet getAtomContainerSet() {
			AtomContainerSet acs=new AtomContainerSet();
			Iterator<AtomContainer> iterator=vecAC.iterator();
			
			while (iterator.hasNext()) {
				AtomContainer ac=iterator.next();
				acs.addAtomContainer(ac);
			}
			return acs;
		}
		

		private String[] columnNames = { "#", "ID", "Name","Formula", "Error" };

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return vecAC.size();
		}
		
		public void updateRow(AtomContainer ac,int row) {
			
			AtomContainer acOld=vecAC.get(row);
			
			String casNew=ac.getProperty("CAS");
			String casOld=acOld.getProperty("CAS");

			if (!casNew.contentEquals(casOld)) {
				int returnVal=JOptionPane.showConfirmDialog(myfraEditChemical, "Do you wish to update the CAS to "+casNew+"?","Update record?",JOptionPane.YES_NO_OPTION);				
				if (returnVal==JOptionPane.NO_OPTION) {
					//Use old info:
					ac.setProperty(DSSToxRecord.strCAS, acOld.getProperty(DSSToxRecord.strCAS));
					ac.setProperty(DSSToxRecord.strName, acOld.getProperty(DSSToxRecord.strName));
				}
			}

			addFormula(ac);
			vecAC.set(row, ac);
			fireTableDataChanged();

		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			AtomContainer ac=vecAC.get(row);
			Object value=getValue(ac,col);
//			System.out.println(value);
			return value;
		}
		
		Object getValue(AtomContainer ac,int col) {
			if (col==0) {
				if (ac.getProperty("Index")==null) return -1;
				else return ac.getProperty("Index");
			} else if (col==1) {
				if (ac.getProperty(DSSToxRecord.strCAS)==null) return "";				
				else return ac.getProperty(DSSToxRecord.strCAS);
			} else if (col==2) {
				if (ac.getProperty(DSSToxRecord.strName)==null) return "";
				else return ac.getProperty(DSSToxRecord.strName);
			} else if (col==3) {
				if (ac.getProperty("Formula")==null) return "";
				else return ac.getProperty("Formula");
			} else if (col==4) {
				if (ac.getProperty("Error")==null) return "";
				else return ac.getProperty("Error");
			} else {
				return null;
			}
			
		}
		
		public AtomContainer getAtomContainer(int row) {
			return (AtomContainer)vecAC.get(row);
		}

		class CustomComparator implements Comparator<AtomContainer>{
		    int col;
			
			CustomComparator(int sortCol) {
				this.col=sortCol;
			}
			
			public int compare(AtomContainer ac1,AtomContainer ac2) {	        
		    	Object val1=getValue(ac1,col);
		    	Object val2=getValue(ac2,col);
		    			    	
		    	if (col==0) {//Index
		    		int ival1=(int)val1;
		    		int ival2=(int)val2;
		    		return ival1-ival2;
		    	} else if (col==1) {//CAS

		    		String strval1=(String)val1;
		    		String strval2=(String)val2;
		    		return addZeros(strval1,15).compareTo(addZeros(strval2, 15));

		    	} else {
		    		
		    		
		    		String strval1=(String)val1;
		    		String strval2=(String)val2;

		    		//TODO add code to sort formulas better (i.e. first by carbon, then H, etc)
		    		return strval1.compareTo(strval2);	
		    	}
		    	
//		    	System.out.println(val1+"\t"+val2);
		    		
		    }
			
			String addZeros(String val,int length) {
				while (val.length()<length) val="0"+val;
				return val;
			}
		}
		
		public void sortByCol() {						
			Collections.sort(vecAC,new CustomComparator(sortCol));
			
//			System.out.println(sortCol);
			
			if (!isSortAsc) Collections.reverse(vecAC);
			
			fireTableDataChanged();
		}
		
		public void  removeRow(int row) {	
			vecAC.remove(row);
			fireTableDataChanged();
		}
		
		public void addFormula(AtomContainer ac) {
			try {
				MolecularFormula mf=(MolecularFormula) MolecularFormulaManipulator.getMolecularFormula(ac);
				ac.setProperty("Formula",MolecularFormulaManipulator.getString(mf));
			} catch (Exception e) {
				e.printStackTrace();
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

		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		public void setValueAt(Object value, int row, int col) {

			AtomContainer ac=vecAC.get(row);

			if (col==0) {
				ac.setProperty("Index", value);
			} else if (col==1) {
				ac.setProperty("CAS", value);
			} else if (col==2) {
				ac.setProperty("Name", value);
			} else if (col==3) {
				ac.setProperty("Formula", value);
			} else if (col==4) {
				ac.setProperty("Error", value);
			} else {
				
			}
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

		public void addAtomContainers(AtomContainerSet moleculeSet) {
			int newIndex=-1;

			if (vecAC.size()>0) {
				//determine max index:
				int maxIndex=-1;

				Iterator<AtomContainer> iterator=vecAC.iterator();
				
				while (iterator.hasNext()) {
					AtomContainer ac=iterator.next();
					int index=(Integer)ac.getProperty("Index");
					if (index>maxIndex) maxIndex=index;
				}
				newIndex=maxIndex+1;
			
			} else 
				newIndex=1;
				


			for (int i=0;i<moleculeSet.getAtomContainerCount();i++) {
				AtomContainer ac=(AtomContainer)moleculeSet.getAtomContainer(i);
				addFormula(ac);
				ac.setProperty("Index", newIndex);
				vecAC.add(ac);
				newIndex++;
			}			
			
//			System.out.println(htMols.size());
			fireTableDataChanged();
			table.scrollRectToVisible(table.getCellRect(getRowCount() - 1, 0, true));
			int selectedRow=vecAC.size()-1;
//			System.out.println(moleculeSet.getAtomContainerCount());
			if (selectedRow>-1)
				table.addRowSelectionInterval(selectedRow ,selectedRow);
			
		}

		/**
		 * Remove all rows from table
		 */
		public void clear() {
			vecAC.removeAllElements();
			fireTableDataChanged();
		}

	}

} // end overall class


	
