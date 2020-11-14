package ToxPredictor.Application.GUI;

import java.awt.Color;
import java.awt.event.*;

import javax.vecmath.Point2d;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.PanelBatchChemicals.MyTableModel;
import ToxPredictor.Utilities.*;


//import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.*;
import org.openscience.cdk.interfaces.*;

public class PanelStructureDatabaseSearchBatch extends JPanel {


	public JTextArea jtfIdentifiers = new JTextArea();

	public ButtonGroup bg=new ButtonGroup();
	public JRadioButton jrbCAS=new JRadioButton();
	public JRadioButton jrbSmiles = new JRadioButton();
	public JRadioButton jrbName = new JRadioButton();
//	public JRadioButton jrbExtraOption=new JRadioButton();	
	JButton jbDrawChemical=new JButton();
	JButton jbDelete=new JButton();
	JButton jbClear=new JButton();
	
	
	JLabel jlBatchNote=new JLabel("Double click to edit batch list chemicals");
	JScrollPane scrollPane;
	JButton jbSearch=new JButton();
	
	actionAdapter aa =new actionAdapter();
	windowAdapter wa=new windowAdapter();
	
	Object gui;

	ArrayList<String> caslist;
	
	int inset=20;
	
	
	public PanelStructureDatabaseSearchBatch() {
		
	}
	
    public static void main(String[] args) {
    	PanelStructureDatabaseSearchBatch f=new PanelStructureDatabaseSearchBatch();
    	f.setVisible(true);
	}


	private void SetupSimpleControls() {
		
		int radialwidth=150;
		int textHeight=20;
		
//		int vspacing=15;
		int vspacing=(this.getHeight()-7*textHeight)/10;
		
		jtfIdentifiers.setName("jtfIdentifiers");
		scrollPane= new JScrollPane(jtfIdentifiers);
        scrollPane.setSize(this.getWidth()-3*inset-radialwidth,6*textHeight+6*vspacing);
        scrollPane.setLocation(this.getWidth()-inset-scrollPane.getWidth(),inset);

        
		jrbCAS.setText("CAS (e.g. 71-43-2):");
		jrbCAS.setSize(radialwidth, textHeight);
		jrbCAS.setLocation(20, 30);
	    jrbCAS.addActionListener(aa);
		jrbCAS.setActionCommand("jrbCAS");


		jrbSmiles.setText("Smiles (e.g. CCO):");
		jrbSmiles.setSize(radialwidth, textHeight);
		jrbSmiles.setLocation(20, (int)jrbCAS.getLocation().getY()+textHeight+vspacing);
	    jrbSmiles.addActionListener(aa);
		jrbSmiles.setActionCommand("jrbSmiles");

		
		jrbName.setText("Name (e.g benzene):");
		jrbName.setSize(radialwidth, textHeight);
		jrbName.setLocation(20, (int)jrbSmiles.getLocation().getY()+textHeight+vspacing);
	    jrbName.addActionListener(aa);
		jrbName.setActionCommand("jrbName");

//		jrbExtraOption.setSize(radialwidth, 20);		
//		jrbExtraOption.setText("Extra option");
//		jrbExtraOption.setLocation(20, (int)jrbName.getLocation().getY()+inset);
//	    jrbExtraOption.addActionListener(aa);
//		jrbExtraOption.setActionCommand("jrbExtraOption");
//		jrbExtraOption.setToolTipText("Add tooltip");
				
		jbSearch.setSize(120,textHeight);
		jbSearch.setLocation(getWidth()-inset-jbSearch.getWidth(),getHeight()-vspacing-textHeight);
		jbSearch.setText("Search");
        jbSearch.addActionListener(aa);
		jbSearch.setActionCommand("jbSearch");
		
//		jbCancel.setSize(textWidth,textHeight);
//		jbCancel.setLocation(jbOK.getX()-inset-textWidth,jbOK.getY());
//		jbCancel.setText("Cancel");
//		jbCancel.setActionCommand("jbCancel");

		

		jbDrawChemical.setActionCommand("jbDrawChemical");
		jbDrawChemical.addActionListener(aa);

		jbDrawChemical.setSize(radialwidth,20);
		jbDrawChemical.setLocation(inset,(int)jrbName.getLocation().getY()+vspacing+textHeight);
		jbDrawChemical.setText("Draw chemical");
		
		jbDelete.setSize(radialwidth,20);
		jbDelete.setLocation(inset,(int)jbDrawChemical.getLocation().getY()+vspacing+textHeight);
		jbDelete.setText("Delete selected");
		jbDelete.setActionCommand("jbDelete");
		jbDelete.addActionListener(aa);
		
		jbClear.setSize(radialwidth,20);
		jbClear.setLocation(inset,(int)jbDelete.getLocation().getY()+vspacing+textHeight);
		jbClear.setText("Clear table");
		jbClear.setActionCommand("jbClear");
		jbClear.addActionListener(aa);

		jlBatchNote.setSize(350,20);
		jlBatchNote.setLocation(20,this.getHeight()-jlBatchNote.getHeight()-vspacing);
		jlBatchNote.setForeground(Color.blue);
		
		add(this.jrbCAS);
		add(this.jrbSmiles);
		add(this.jrbName);
		this.add(jlBatchNote);
		
		this.add(jbDrawChemical);
		this.add(jbDelete);
		add(jbSearch);
		add(jbClear);


		
	}
	
//	public void SetupStructure(Image image) {
////		SaveStructureToFile.CreateScaledImageFile(molecule,"srchStructure","ToxPredictor/temp",false,true,true,30);		
////		ImageIcon ii=new ImageIcon("ToxPredictor/temp/"+"srchStructure"+".png");
////		jlDrawnStructure.setIcon(new ImageIcon(image));
//	}
	void init(int width,int height) {
		
//		try {
//			java.net.URL url=this.getClass().getClassLoader().getResource("epa_logo_icon.jpg");			
//			((java.awt.Frame)this.getOwner()).setIconImage(new ImageIcon(url).getImage());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		this.setSize(width,height);
//		this.setBorder(BorderFactory.createLineBorder(Color.black));
		
		TitledBorder border = new TitledBorder("Search the database by CAS, SMILES, or Name (one per line)");
	    border.setTitleJustification(TitledBorder.LEFT);
	    border.setTitlePosition(TitledBorder.TOP);
	    this.setBorder(border);

		
//		double SF=(double)DisplaySize/(double)ImageSize;
//		this.setResizable(false);		
		
		if (gui instanceof TESTApplication) {
			TESTApplication f=(TESTApplication)gui;
			this.setSize(width, height);	
		}
		
		Utilities.SetFonts(this);
//		this.getContentPane().setLayout(null);
		this.setLayout(null);
		
		this.SetupSimpleControls();
		
		
		bg.add(jrbCAS);
		bg.add(jrbSmiles);
		bg.add(jrbName);
//		bg.add(jrbExtraOption);
		
		add(this.scrollPane);
		this.jrbCAS.doClick();			
		this.setVisible(false);
		
	}

	public void SetParentFrame(Object gui) {
		this.gui=gui;
	}
			
	
    public void jbSearch_actionPerformed(ActionEvent e) {

    	if (jtfIdentifiers.getText().trim().isEmpty()) {
    		JOptionPane.showMessageDialog(this, "Please enter an identifier");
    		jtfIdentifiers.requestFocus();
    		return;
    	}

    	String identifiers=parseIdentifiers(jtfIdentifiers.getText().trim());
//    	System.out.println(identifiers);
//    	
    	
    	int type=-1;
    	
    	if (this.jrbSmiles.isSelected()) {
    		type=TaskStructureSearch.TypeSmiles;
    	} else if (this.jrbCAS.isSelected()) {
    		type=TaskStructureSearch.TypeCAS;
    	} else if (this.jrbName.isSelected()) {
    		type=TaskStructureSearch.TypeName;
    	}
    	
		if (gui instanceof TESTApplication) {
			TESTApplication fraMain8=(TESTApplication)gui;	
    		fraMain8.taskStructureFile.init(identifiers,type,TESTConstants.typeTaskBatch,fraMain8);
    		fraMain8.taskStructureFile.go();
    		fraMain8.timerBatchStructureFile.start();
		}

    	
    	this.jtfIdentifiers.setText("");
//    	this.setVisible(false);

    	
    }
    
    
    
    String parseIdentifiers(String text) {
    	
		ArrayList<String>identifiers=ToxPredictor.Utilities.Utilities.Parse3toArrayList(text, "\n");

		String ids="";
		
		for (int i=0;i<identifiers.size();i++) {
			String identifier=identifiers.get(i).trim();
				
			if (identifier.isEmpty()) continue;
			
			if (jrbCAS.isSelected()) {
				ids+=parseSearchCAS(identifier);
			} else {
				ids+=identifier;
			}
			ids+="\n";
		}
    	
    	
    	return ids;
    }

    /**
	 * Adds a blank molecule to the batch table
	 */
	private void jbAdd_actionPerformed() {

		TESTApplication f=(TESTApplication)gui;
		
			
		AtomContainer mol=new AtomContainer();
		mol.setProperty("CAS", "C_"+System.currentTimeMillis());
		mol.setProperty("Error", "Number of atoms equals zero");	
		mol.setProperty("Query", "Drawn structure");

		
		MyTableModel model=(MyTableModel)f.panelBatch.table.getModel();

		AtomContainerSet acs=new AtomContainerSet();
		acs.addAtomContainer(mol);

		f.panelBatch.addMoleculesToTable(acs);
		f.panelBatch.configureFraEditChemical();

		
		//	JScrollBar vbar = panelBatch.scrollPane.getVerticalScrollBar();
		//	vbar.setValue(vbar.getMaximum());
	}
	
	

	private void jrbClicked(String ac) {
		this.jtfIdentifiers.requestFocus();
	}
	
	
	public static String parseSearchCAS(String CAS) {
		String srchCAS = CAS.trim();
		
		if (srchCAS.indexOf("-")>-1) {	//kill off zeros in front:
			String part1, part2;
			part1 = (Integer.parseInt(srchCAS.substring(0, srchCAS.indexOf("-"))))
					+ "";
			part2 = srchCAS.substring(srchCAS.indexOf("-"), srchCAS.length());
			srchCAS = part1 + part2;
		
		} else { //missing dashes- try to convert it:
			String temp=srchCAS;
			String part1,part2,part3;
			
			if (temp.length()>=4) {
				part3=temp.substring(temp.length()-1,temp.length());
				temp=temp.substring(0,temp.length()-1);

				part2=temp.substring(temp.length()-2,temp.length());
				temp=temp.substring(0,temp.length()-2);

				part1=temp;
				srchCAS=part1+"-"+part2+"-"+part3;
			} else {
				return CAS;
			}
		}

		return srchCAS;
		
	}
	
	

	class windowAdapter extends java.awt.event.WindowAdapter {
    	  
    	  public void windowClosing(java.awt.event.WindowEvent e) {
    	     setVisible(false);
    	  }
   	}

	    
	
	class actionAdapter implements java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {

			String ac=e.getActionCommand();

			if (ac.equals("jbSearch")) {
				jbSearch_actionPerformed(e);
			} else if (e.getActionCommand().equals("jbDelete")) {
				if (gui instanceof TESTApplication) {
					TESTApplication f=(TESTApplication)gui;
					f.panelBatch.jbDelete_actionPerformed();
				}
				
			} else if (ac.equals("jrbCAS") || ac.equals("jrbSmiles") || ac.contentEquals("jrbName")) {
				jrbClicked(ac);					
			} else if (e.getActionCommand().equals("jbDrawChemical")) {
				jbAdd_actionPerformed();
			} else if (e.getActionCommand().contentEquals("jbClear")) {
				TESTApplication f=(TESTApplication)gui;
				
				if (f.panelBatch.table.getRowCount()==0) {
					JOptionPane.showMessageDialog(getParent(), "Table is already clear");
					return;
				}
				
				MyTableModel model=(MyTableModel)f.panelBatch.table.getModel();
				model.clear();
				
				
				
			}

		}
	}
    
	
}
