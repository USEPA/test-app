package ToxPredictor.Application.GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Utilities.*;

//import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.*;

public class PanelStructureDatabaseSearchSingle extends JPanel {


	public JTextField jtfIdentifier = new JTextField();

	public JLabel jlCAS = new JLabel();
	public JTextField jtfCAS = new JTextField();
	
	public JLabel jlName = new JLabel();
	public JTextField jtfName = new JTextField();


//	JScrollPane scrollPane;
//	public JLabel jlDrawnStructure =new JLabel();
	
	
	JButton jbSearch=new JButton();
//	JButton jbCancel=new JButton();
	actionAdapter aa =new actionAdapter();	
	keyAdapter ka=new keyAdapter();
	
	Object gui;
	
	
	
//	public ChemicalFinder cf = new ChemicalFinder(
//			"ValidatedStructures.txt", "ValidatedStructures");

//	private ChemicalFinder cf = new ChemicalFinder();

	ArrayList<String> caslist;
	
    public static void main(String[] args) {
    	PanelStructureDatabaseSearchSingle f=new PanelStructureDatabaseSearchSingle();
    	f.setVisible(true);
	}


	private void SetupSimpleControls() {
		
		int heightControls=20;
		int inset=20;
		
		jbSearch.setSize(100,heightControls);
		jbSearch.setLocation(getWidth()-inset-jbSearch.getWidth(),2*inset);
		jbSearch.setText("Search");
		jbSearch.setActionCommand("jbSearch");

		jtfIdentifier.setName("jtfIdentifier");
		jtfIdentifier.addKeyListener(ka);
		jtfIdentifier.setSize(this.getWidth()-3*inset-jbSearch.getWidth(),heightControls);
		jtfIdentifier.setLocation(inset,2*inset);
		
		
		jlCAS.setLocation(inset, jtfIdentifier.getY()+heightControls+inset);
		jlCAS.setText("Molecule ID:");
		jlCAS.setSize(70, heightControls);
		jlCAS.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfCAS.setSize(jtfIdentifier.getWidth()-inset-jlCAS.getWidth(), heightControls);
		jtfCAS.setName("jtfCAS");
		jtfCAS.setLocation(2*inset+jlCAS.getWidth(), (int)jlCAS.getLocation().getY());
		jtfCAS.setEditable(false);
		
		jlName.setLocation(inset, jlCAS.getY()+heightControls+inset);
		jlName.setText("Name:");
		jlName.setSize(70, heightControls);
		jlName.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfName.setSize(jtfCAS.getWidth(), heightControls);
		jtfName.setName("jtfCAS");
		jtfName.setLocation(2*inset+jlName.getWidth(), (int)jlName.getLocation().getY());
		jtfName.setEditable(false);
		

//		jbCancel.setSize(textWidth,textHeight);
//		jbCancel.setLocation(jbOK.getX()-inset-textWidth,jbOK.getY());
//		jbCancel.setText("Cancel");
//		jbCancel.setActionCommand("jbCancel");
	}
	
	void init(int width,int height) {
		

		this.setSize(width,height);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
//		double SF=(double)DisplaySize/(double)ImageSize;
//		this.setResizable(false);		
		
		if (gui instanceof TESTApplication) {
			TESTApplication f=(TESTApplication)gui;
			this.setSize(width, height);	
		}
		
		Utilities.SetFonts(this);
//		this.getContentPane().setLayout(null);
		this.setLayout(null);
//		this.setModal(true);
		
		this.SetupSimpleControls();
		add(jbSearch);
		
		this.add(jlCAS);
		this.add(jtfCAS);

		this.add(jlName);
		this.add(jtfName);
		
		add(jtfIdentifier);
		jbSearch.addActionListener(aa);
		
		TitledBorder border = new TitledBorder("Enter a CAS, SMILES, or name and click Search");
	    border.setTitleJustification(TitledBorder.LEFT);
	    border.setTitlePosition(TitledBorder.TOP);
	    this.setBorder(border);
		
	}

	public void SetParentFrame(Object gui) {
		this.gui=gui;
	}
			
	
    public void jbSearch_actionPerformed(ActionEvent e) {

    	if (jtfIdentifier.getText().trim().isEmpty()) {
    		JOptionPane.showMessageDialog(this, "Please enter an identifier");
    		jtfIdentifier.requestFocus();
    		return;
    	}

    	String identifier=jtfIdentifier.getText().trim()+"\n";
//    	System.out.println(identifiers);
//    	
		if (gui instanceof TESTApplication) {
			TESTApplication fraMain8=(TESTApplication)gui;
			
			int type=-1;//it will autodetect it for single chemical mode
			
    		fraMain8.taskStructureFile.init(identifier,type,TESTConstants.typeTaskSingle,fraMain8);
    		fraMain8.taskStructureFile.go();
    		fraMain8.timerBatchStructureFile.start();
		}

    	
    	this.jtfIdentifier.setText("");
    	
    	
//    	this.setVisible(false);

    	
    }
    
    
	
	

	/**
	 * Used to detect if the delete key is clicked (triggers delete event) or if
	 * enter key is pressed (searchs by CAS in molecule id text box)
	 */
	class keyAdapter extends java.awt.event.KeyAdapter {

		public void keyReleased(KeyEvent e) {
			Object o = e.getSource();

			if (e.getKeyCode() != e.VK_ENTER)return;
			if (!(o instanceof JTextField)) return;

			String name = ((JTextField) o).getName();
			boolean HasFocus = ((JTextField) o).hasFocus();

//			System.out.println(e.getKeyCode()+"\t"+e.VK_ENTER+"\t"+name);

			
			if (name.equals("jtfIdentifier") && HasFocus) {
				// myfraStructureDatabaseSearch.ImportFromCAS(jtfCAS);
				jbSearch.doClick();
			}
				
			 

		}
	}    
	
	class actionAdapter implements java.awt.event.ActionListener {
		  public void actionPerformed(ActionEvent e) {
			  String ac=e.getActionCommand();
			  
				if (ac.equals("jbSearch")) {
					jbSearch_actionPerformed(e);
				} 
		  }
    }
    
	
}
