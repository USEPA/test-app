package ToxPredictor.Application.GUI;

import java.awt.event.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.PanelBatchChemicals.MyTableModel;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.Utilities.*;

//import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.*;

public class PanelStructureDatabaseSearchBatch extends JPanel {

	public final static String strOptionAutomatic="Automatic";
	public final static String strOptionCAS="CAS (e.g. 71-43-2)";
	public final static String strOptionName="Name (e.g benzene)";
	public final static String strOptionSmiles="Smiles (e.g. CCO)";

	public JTextArea jtfIdentifiers = new JTextArea();

	public JComboBox jcbOptions=new JComboBox();
	
//	public ButtonGroup bg=new ButtonGroup();
//	public JRadioButton jrbAutomatic=new JRadioButton();
//	public JRadioButton jrbCAS=new JRadioButton();
//	public JRadioButton jrbSmiles = new JRadioButton();
//	public JRadioButton jrbName = new JRadioButton();
//	public JRadioButton jrbExtraOption=new JRadioButton();	
	JButton jbDrawChemical=new JButton();
	JButton jbDelete=new JButton();
	JButton jbClear=new JButton();
	
	
//	JLabel jlBatchNote=new JLabel("Double click to edit batch list chemicals");
	JScrollPane scrollPane;
	JButton jbSearch=new JButton();
	
	JButton jbLoad_CHC_List=new JButton();
	JButton jbLoad_CHC_Sample_List=new JButton();
	JButton jbLoad_CHC_HPV_List=new JButton();
	
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
		
		boolean forMDH=false;
		
		if (gui instanceof TESTApplication) {
			TESTApplication f=(TESTApplication)gui;
			forMDH=f.forMDH;
		}
		
		int radialwidth=150;
		int textHeight=20;
		
//		int vspacing=15;
		int vspacing=(this.getHeight()-5*textHeight)/10;
		
		if (forMDH) {
			vspacing=(this.getHeight()-7*textHeight)/10;
		}
		Vector<String>options=new Vector<>();
		options.add(strOptionAutomatic);
		options.add(strOptionCAS);
		options.add(strOptionName);
		options.add(strOptionSmiles);
		
		jcbOptions=new JComboBox(options);
		
	
		jtfIdentifiers.setName("jtfIdentifiers");
		scrollPane= new JScrollPane(jtfIdentifiers);
        scrollPane.setSize(this.getWidth()-3*inset-radialwidth,getHeight()-textHeight-3*inset);
        scrollPane.setLocation(this.getWidth()-inset-scrollPane.getWidth(),inset);
        
		jcbOptions.setSize(radialwidth, textHeight);
		jcbOptions.setLocation(20, 30);
		jcbOptions.addActionListener(aa);
		jcbOptions.setActionCommand("jrbAutomatic");

       
				
		jbSearch.setSize(120,textHeight);
		jbSearch.setLocation(getWidth()-inset-jbSearch.getWidth(),getHeight()-inset-textHeight);
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
		jbDrawChemical.setLocation(inset,(int)jcbOptions.getLocation().getY()+vspacing+textHeight);
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
		


//		jlBatchNote.setSize(350,20);
//		jlBatchNote.setLocation(20,this.getHeight()-jlBatchNote.getHeight()-vspacing);
//		jlBatchNote.setForeground(Color.blue);
		
		add(this.jcbOptions);
//		this.add(jlBatchNote);
		
		add(jbDrawChemical);
		add(jbDelete);
		add(jbSearch);
		add(jbClear);

			
		if (forMDH) {
			jbLoad_CHC_List.setSize(radialwidth,textHeight);
			jbLoad_CHC_List.setLocation(inset,(int)jbClear.getLocation().getY()+vspacing+textHeight);
			jbLoad_CHC_List.setText("Load CHC List");

			jbLoad_CHC_List.addActionListener(aa);
			jbLoad_CHC_List.setActionCommand("jbLoad_CHC_List");
			add(jbLoad_CHC_List);

			// **************************************************

			jbLoad_CHC_HPV_List.addActionListener(aa);
			jbLoad_CHC_HPV_List.setActionCommand("jbLoad_CHC_HPV_List");
			add(jbLoad_CHC_HPV_List);


			jbLoad_CHC_HPV_List.setSize(radialwidth,textHeight);
			jbLoad_CHC_HPV_List.setLocation(inset,(int)jbLoad_CHC_List.getLocation().getY()+vspacing+textHeight);
			jbLoad_CHC_HPV_List.setText("Load CHC HPV List");

			// **************************************************

			jbLoad_CHC_Sample_List.setSize(radialwidth,textHeight);
			jbLoad_CHC_Sample_List.setLocation(inset,(int)jbLoad_CHC_HPV_List.getLocation().getY()+vspacing+textHeight);
			jbLoad_CHC_Sample_List.setText("Load Sample List");

			jbLoad_CHC_Sample_List.addActionListener(aa);
			jbLoad_CHC_Sample_List.setActionCommand("jbLoad_CHC_Sample_List");
			add(jbLoad_CHC_Sample_List);

		}


		
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
		
		TitledBorder border = new TitledBorder("Search the database by CAS, SMILES, Name, InChi, InChiKey, or DTXSID (one per line)");
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
		
				
		add(this.scrollPane);
		this.jcbOptions.setSelectedIndex(0);			
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
    	
    	String option=(String)this.jcbOptions.getSelectedItem();
    	
    	if (option.contentEquals(strOptionAutomatic)) {
    		type=TaskStructureSearch.TypeAny;
    	} else if (option.contentEquals(strOptionSmiles)) {
    		type=TaskStructureSearch.TypeSmiles;
    	} else if (option.contentEquals(strOptionCAS)) {
    		type=TaskStructureSearch.TypeCAS;
    	} else if (option.contentEquals(strOptionName)) {
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
			
			String option=(String)jcbOptions.getSelectedItem();
			
			if (option.contentEquals(strOptionCAS) && ResolverDb2.isCAS(identifier)) 
				identifier=ResolverDb2.parseSearchCAS(identifier);
														
			ids+=identifier+"\n";			
			
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
				
			} else if (e.getActionCommand().equals("jbLoad_CHC_HPV_List")) {
				
				int type=TaskStructureSearch.TypeSDF_In_Jar;
				TESTApplication test=(TESTApplication)gui;	
							
	    		test.taskStructureFile.init("2022 CHC HPV.sdf",type,TESTConstants.typeTaskBatch,test);
	    		test.taskStructureFile.go();
	    		test.timerBatchStructureFile.start();
				
				
			} else if (e.getActionCommand().equals("jbLoad_CHC_List")) {
				
				int type=TaskStructureSearch.TypeSDF_In_Jar;
				TESTApplication test=(TESTApplication)gui;	
	    		test.taskStructureFile.init("WebVersion_2022_Chemicals_of_High_Concern_List_2023_02_07.sdf",type,TESTConstants.typeTaskBatch,test);
	    		test.taskStructureFile.go();
	    		test.timerBatchStructureFile.start();

//				try {
//					InputStream ins=this.getClass().getClassLoader().getResourceAsStream("MNDOHTOXFREE.smi");
//					Scanner scanner=new Scanner(ins);
//
//					String data="";
//					while (scanner.hasNext()) {
//						data+=scanner.nextLine()+"\r\n";
//					}
//					scanner.close();
//					jtfIdentifiers.setText(data);
//					jbSearch_actionPerformed(e);
//					
//					
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
	    		
	    		
			} else if (e.getActionCommand().equals("jbLoad_CHC_Sample_List")) {

				int type=TaskStructureSearch.TypeSDF_In_Jar;
				TESTApplication test=(TESTApplication)gui;	
	    		test.taskStructureFile.init("MDH sample chemicals.sdf",type,TESTConstants.typeTaskBatch,test);
	    		test.taskStructureFile.go();
	    		test.timerBatchStructureFile.start();
				
//				String data="CC(C)(C1=CC=C(O)C=C1)C1=CC=C(O)C=C1\tDTXSID7020182\r\n";//bisphenol A
//				data+="CC#N\tDTXSID7020009\r\n";//acetonitrile
//				data+="CC(C)(C)C1=CC(CCC(=O)OCC(COC(=O)CCC2=CC(=C(O)C(=C2)C(C)(C)C)C(C)(C)C)(COC(=O)CCC2=CC(=C(O)C(=C2)C(C)(C)C)C(C)(C)C)COC(=O)CCC2=CC(=C(O)C(=C2)C(C)(C)C)C(C)(C)C)=CC(=C1O)C(C)(C)C\tDTXSID1027633\r\n";//irganox 1010- cant predict descriptors in opera
//				data+="CC1CC(C)(C)CC(C1)(OOC(C)(C)C)OOC(C)(C)C\tDTXSID4020165\r\n";//AD=0
//				data+="CCCCOC(=O)C1=C(C=CC=C1)C(=O)OCC1=CC=CC=C1\tDTXSID3020205\r\n";
//				data+="CCCCOC(=O)C1=CC=CC=C1C(=O)OCCCC\tDTXSID2021781\r\n";
//				data+="C=CC1=CC=CC=C1\tDTXSID2021284\r\n";
//				data+="CC(C)(C)C1=CC=C(O)C=C1\tDTXSID1020221\r\n";
//				jtfIdentifiers.setText(data);
//				jbSearch_actionPerformed(e);

			}

		}
	}
    
	
}
