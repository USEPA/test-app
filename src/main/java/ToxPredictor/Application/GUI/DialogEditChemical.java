package ToxPredictor.Application.GUI;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.vecmath.Vector2d;

import org.apache.logging.log4j.util.Strings;
//import org.jmol.api.JmolViewer;
import org.openscience.cdk.*;
//import org.openscience.cdk.applications.jchempaint.JCPLocalizationHandler;
//import org.openscience.cdk.applications.jchempaint.JChemPaintEditorPanel;
//import org.openscience.cdk.applications.jchempaint.JChemPaintModel;
//import org.openscience.cdk.applications.jchempaint.io.JCPFileFilter;
//import org.openscience.cdk.applications.jchempaint.io.JCPFileView;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.jchempaint.JCPPropertyHandler;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.application.JChemPaint;
import org.openscience.jchempaint.io.JCPFileFilter;
import org.openscience.jchempaint.io.JCPFileView;

import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.PanelBatchChemicals.MyTableModel;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;
import ToxPredictor.MyDescriptors.*;
import ToxPredictor.Utilities.*;
//import ToxPredictor.descriptors.DescriptorGenerator;


public class DialogEditChemical extends JDialog {

	/**
	 * @param arguments
	 */
	boolean Debug=false;

	public JFileChooser chooser1 = new JFileChooser(); // file chooser for dialog to select mol file- store here so can load from same place


	PanelBatchChemicals pbc=null;
	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();

	boolean MakeHydrogensImplicit=true;

//	String StructureFolder="ValidatedStructures2d";
//	public ChemicalFinder cf = new ChemicalFinder(
//			StructureFolder+"/"+"manifest.txt", StructureFolder);
	
	mouseListener ml=new mouseListener();


//	to get the hierarchy of files to work with the applet, I needed to put both frames here
//	public fraStructureDatabaseSearch7 myfraStructureDatabaseSearch = new fraStructureDatabaseSearch7(cf);
//	public fraSelectChemicalFromDB7 myfraSelectChemicalFromDB = new fraSelectChemicalFromDB7(cf);


	//	 data classes
//	private JChemPaintModel model = new JChemPaintModel();
	private IChemModel model = JChemPaint.emptyModel();

	private JPanel jpTop = new JPanel();
	private TitledBorder titledBorder1 = new TitledBorder("");
	private TitledBorder titledBorder2 = new TitledBorder("");
	private JLabel jlCAS = new JLabel();
	public JTextField jtfCAS = new JTextField();

	private JButton jbOK = new JButton();
	private JButton jbCancel = new JButton();

//	public JChemPaintEditorPanel jcpep;
	public JChemPaintPanel jcpep;


	// Jar needs to be signed for the following line to not crash the applet:

	private FileFilter currentFilter = null;


	private actionAdapter aa = new actionAdapter();

	private keyAdapter ka = new keyAdapter();

	private windowAdapter wa=new windowAdapter();

	AboutDialog aboutDialog = new AboutDialog();

	java.awt.datatransfer.Clipboard clipboard = new java.awt.datatransfer.Clipboard(
	"myClipBoard");

	JMenuItem jmiCleanup=null;
	JMenuItem jmiMakeImplicit=null;

//	Hashtable htProps=null;

	private String currentSmiles="";

	private DescriptorFactory df = new DescriptorFactory(false);

	int selectedRow=-1;

	private Integer Index;


	public void setParentFrame(PanelBatchChemicals pbc) {
		this.pbc=pbc;
	}

	private void setupFileChooser() {
		//			JCPFileFilter.addChoosableFileFilters(this.chooser1);

		chooser1.setFileView(new JCPFileView());
		chooser1.setAcceptAllFileFilterUsed(false);

		// for now only add the following:
		chooser1.addChoosableFileFilter(new JCPFileFilter(JCPFileFilter.mol));

		//set up chooser2:
		JPanel jpanelHelp=new JPanel();

		jpanelHelp.setBounds(0, 0, 100, 100);
//		jpanelHelp.setPreferredSize(new Dimension(100,100));
//		jpanelHelp.setLayout(null);

		JLabel jlHelp = new JLabel();
//		jlHelp.setSize(95,95);
//		jlHelp.setLocation(5,5);


		jlHelp.setText("<html>This dialog will select the folder<br>" +
				             " for storing the results.<br><br>" +
				             "If the \"My Documents\" folder is selected,<br>" +
				             "the results files will be stored in<br>" +
							 "\"My Documents/MyToxicity/ToxRun_CAS\"<br>" +
							 "where CAS is the chemical abstract number<br>" +
							 "for the current chemical (i.e. 71-43-2) </html>");

		jpanelHelp.add(jlHelp);

	}

	
	public void init(int width,int height) {

		this.getContentPane().setLayout(null);
		this.setSize(width, height);

		// javax.swing.event.EventListenerList e=model.getChangeListeners();

//		this.setIconImage(new ImageIcon("ToxPredictor/system/skullicon.jpg").getImage());
		this.setResizable(false);//applet-omit

		this.setupSimpleControls();
		this.setupPanels();
		this.setupListeners();

		this.setupJChemPaintEditorPanel();
		jmiCleanup=TESTApplication.getCleanup(jcpep);
		this.setUpToolBars();

		Utilities.SetFonts(this.getContentPane());

//		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Edit chemical");

//		this.myfraStructureDatabaseSearch.SetParentFrame(this);
//		this.myfraSelectChemicalFromDB.SetParentFrame(this);

		this.setupFileChooser();

		ToxPredictor.Utilities.Utilities.CenterFrame(this); //applet-omit
//		setModalityType(JDialog.ModalityType.);
		repaint();
}


	public void setupJChemPaintEditorPanel() {

		int height=(int)(this.getHeight()-115);
		int width=(int)(this.getWidth()-40);

        ArrayList<String> blacklist = new ArrayList<String>();
        blacklist.add("file");
        blacklist.add("options");
        blacklist.add("menubar++");
        blacklist.add("rgroupMenu");
        blacklist.add("templates");
//        blacklist.add("new");
        blacklist.add("createInChI");
        
        jcpep = new JChemPaintPanel(model, JChemPaint.GUI_APPLICATION, false, null, blacklist);
        
//        System.out.println("set fit to screen in editor panel");
		JCPPropertyHandler.getInstance(true).getJCPProperties().setProperty("FitToScreen","false");

        
        // commented out by JH 08/15/2017 - replaced by code above to block out "View Menubar" and "Preferences..." menu items
//		jcpep = new JChemPaintPanel(model, JChemPaint.GUI_APPLICATION, false, null, new ArrayList<String>());
//        jcpep.updateStatusBar();
		jcpep.setSize(width,height);
		jcpep.setLocation(20, 20);
//		jcpep.updateStatusBar();

		//			jcpep.setShowInsertTextField(true);

//		this.setupMenus();//TODO


//		fraMain7.convertMenusToEnglish(jcpep);

//		jcpep.getToolBar().setFloatable(false);
//
//		Component[] comps = jcpep.getComponents();
//
//		JToolBar jtb=jcpep.getToolBar();
//		Object o=jtb.getComponent(jtb.getComponentCount()-1);//elementToolbar was added last
//		JToolBar etb=(JToolBar)o;
//
//		//need a new box since would have too many elements in column
//		javax.swing.Box box2= javax.swing.Box.createVerticalBox();
//		etb.add(box2,3); //need to add it before the filler
//
//		JButton jb=fraMain7.createElementToolbarButton("Si", jcpep, true);
//		box2.add(jb);
//
//		jb=fraMain7.createElementToolbarButton("Sn", jcpep, true);
//		box2.add(jb);
//
//		jb=fraMain7.createElementToolbarButton("As", jcpep, true);
//		box2.add(jb);

		this.add(jcpep);

	}

	public AtomContainer configureModel(AtomContainer molecule,int selectedRow,Integer Index) {

		this.selectedRow=selectedRow;
		this.Index=Index;
		
		AtomContainerSet ms = new AtomContainerSet();
		ms.addAtomContainer(molecule);
		model.setMoleculeSet(ms);
		jcpep.setChemModel(model);
		

		
//		try {
//			molecule = CDKUtilities.Generate2dCoords(molecule);
//			if (molecule==null) {
//				molecule.setProperty("Error", "Error during coordinate generation");
//				JOptionPane.showMessageDialog(this, "Error during coordinate generation");
//				return molecule;
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			JOptionPane.showMessageDialog(this, "Error during coordinate generation");
//			molecule.setProperty("Error", "Error during coordinate generation");
//			return molecule;
//		}
//
//		try {
//			//check for aromatic bonds (order=1.5) and change to 1 and 2's
//			molecule = CDKUtilities.CheckForAromaticBonds(molecule);
//			CDKUtilities.DetectAromaticity(molecule);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			JOptionPane.showMessageDialog(this, "Error during fixing aromatic bond orders");
//			molecule.setProperty("Error", "Error during fixing aromatic bond orders");
//		}


//		this.htProps=(Hashtable)molecule.getProperties().clone();

//		ChemModel chemModel = new ChemModel();
		

//		this.model = new JChemPaintModel(chemModel);


//		this.model.setMoleculeSet(ms);

//		jcpep.registerModel(model);
//		jcpep.setJChemPaintModel(model,jcpep.getSize());


//		this.model.resetIsModified();

		return molecule;

	}





//	private void setupMenus() {
//
//
//		Component[] c = jcpep.getMenu().getComponents();
//
//		JMenu jmEdit = null;
//		JMenuItem jmiRenderingOptions = null;
//
//		JMenu jmJChemPaintHelp = null;
//
////		System.out.println(Locale.getDefault().getCountry());
//
//		JCPLocalizationHandler jlh=JCPLocalizationHandler.getInstance();
//		String strInsert=jlh.getString("insert");
//		String strReport=jlh.getString("report");
//		String strView=jlh.getString("view");
//		String strRenderingOptions=jlh.getString("renderOptions");
//		String strEdit=jlh.getString("edit");
//		String strHydrogen=jlh.getString("hydrogen");
//		String strMakeHydrogenImplicit=jlh.getString("makeHydrogenImplicit");
//		String strCleanUp=jlh.getString("cleanup");
//		String strNew=jlh.getString("new");
//		String strFlip=jlh.getString("flip");
//		String strAdjustBondOrders=jlh.getString("adjustBondOrders");
//		String strResetBondOrders=jlh.getString("resetBondOrders");
//		String strModelProps=jlh.getString("modelProps");
//		String strCut=jlh.getString("cut");
//		String strCopy=jlh.getString("copy");
//		String strPaste=jlh.getString("paste");
//		String strHelp=jlh.getString("help");
//
//		for (int i = 0; i <= c.length - 1; i++) {
//			// System.out.println(c[i]);
//
//			if (!(c[i] instanceof JMenu))
//				continue;
//
//			JMenu jm = (JMenu) c[i];
//
//
//			if (jm.getText().equals(strInsert) || jm.getText().equals(strReport)) {
//				jm.setVisible(false);
//			} else if (jm.getText().equals(strView)) {
//				Component[] c2 = jm.getMenuComponents();
//
//				for (int i2 = 0; i2 <= c2.length - 1; i2++) {
//
//					// kill separators:
//					// if (c2[i2] instanceof JPopupMenu.Separator)
//					// c2[i2].setVisible(false);
//
//					if (!(c2[i2] instanceof JMenuItem))
//						continue;
//					JMenuItem jmi = (JMenuItem) c2[i2];
//
//
//					if ((jmi.getText().equals(strRenderingOptions))) {
//						jmiRenderingOptions = jmi;
//						jm.remove(jmiRenderingOptions);
//					}
//				}
//
//				jm.setVisible(false);
//
//			} else if (jm.getText().equals(strEdit)) {
//
//				jmEdit = jm;
//				Component[] c2 = jm.getMenuComponents();
//
//				for (int i2 = 0; i2 <= c2.length - 1; i2++) {
//
//					// kill separators:
//					if (c2[i2] instanceof JPopupMenu.Separator)
//						c2[i2].setVisible(false);
//
//					if (!(c2[i2] instanceof JMenuItem))
//						continue;
//
//					JMenuItem jmi = (JMenuItem) c2[i2];
//
//					if (jmi.getText().equals(strHydrogen)) {
//
//						jmi.setVisible(false);
//
//						JMenu j = (JMenu) jmi;
//						Component[] c3 = j.getMenuComponents();
//
//						for (int i3 = 0; i3 <= c3.length - 1; i3++) {
//							JMenuItem jmi2 = (JMenuItem) c3[i3];
//
////							if (jmi2.getText().equals("Make Existing Implicit Hydrogens Explicit")) {
////								jmi2.addActionListener(aa);
////								jmi2.setActionCommand("MakeHydrogensExplicit");
////							}
//							if (jmi2.getText().equals(strMakeHydrogenImplicit))  {
//								this.jmiMakeImplicit=jmi2;
//							}
//
//						}
//					} else if (jmi.getText().equals(strCleanUp)) {
//						jmiCleanup=jmi;
//					}
//
//					if (jmi.getText().equals(strNew)
//							|| jmi.getText().equals(strFlip)
//							|| jmi.getText().equals(strAdjustBondOrders)
//							|| jmi.getText().equals(strResetBondOrders)
//							|| jmi.getText().equals(strModelProps)) {
//						jmi.setVisible(false);
//					}
//
//					// cant access clipboard in applet:
////					if (this.getClass().getName().indexOf("Applet") > -1) {
//
//						if (jmi.getText().equals(strCut)
//								|| jmi.getText().equals(strCopy)
//								|| jmi.getText().equals(strPaste)) {
//							// jmi.setVisible(false);
//
//							ActionListener[] listeners = jmi
//									.getActionListeners();
//							for (int l = 0; l < listeners.length; l++) {
//								jmi.removeActionListener(listeners[l]);
//							}
//
//							if (jmi.getText().equals(strCut)) {
//								jmi.addActionListener(new EditAction(clipboard,
//										jcpep, "cutSelected"));
//							} else if (jmi.getText().equals(strCopy)) {
//								jmi.addActionListener(new CopyPasteAction(
//										clipboard, jcpep, "copy"));
//							} else if (jmi.getText().equals(strPaste)) {
//								jmi.addActionListener(new CopyPasteAction(
//										clipboard, jcpep, "paste"));
//							}
//						}
////					}
//
//				} // end for loop over components
//			} else if (jm.getText().equals(strHelp)) {
//				jcpep.getMenu().remove(jm);
//				jmJChemPaintHelp = jm;
//				jm.setText("JChemPaint Help");
//			}
//
//		}
//
//		jmEdit.add(jmiRenderingOptions); // add rendering options to edit menu (used to be in View menu)
//
//		// **********************************************************
//		// set up help menu:
////		JMenu jmHelp = new JMenu("Help");
////
////
////		JMenuItem jmiHelpUserGuide=new JMenuItem("User Guide"); //applet omit
////		jmiHelpUserGuide.setActionCommand("jmiHelpUserGuide");
////		jmiHelpUserGuide.addActionListener(aa);
////
////		JMenuItem jmiHelpAboutToxPredictor=new JMenuItem("About ToxPredictor");
////		jmiHelpAboutToxPredictor.setActionCommand("jmiHelpAboutToxPredictor");
////		jmiHelpAboutToxPredictor.addActionListener(aa);
////
////		jmHelp.add(jmiHelpUserGuide); //applet-omit
////		jmHelp.add(new JPopupMenu.Separator()); //applet-omit
//////		jmHelp.add(jmJChemPaintHelp);
//////		jmHelp.add(new JPopupMenu.Separator());
////		jmHelp.add(jmiHelpAboutToxPredictor);
////
////
////		jcpep.getMenu().add(jmHelp);
//
//
//		// **********************************************************
//		//		    JMenu jmFile=new JMenu("File");
//		//		    jcpep.getMenu().add(jmFile,0);
//		//
//		//		    JMenuItem jmiSaveMolFile=new JMenuItem("Save Molecule");
//		//		    jmiSaveMolFile.setActionCommand("jmiSaveMolFile");
//		//		    jmiSaveMolFile.addActionListener(aa);
//		//		    jmFile.add(jmiSaveMolFile);
//
//		JMenu jm = new JMenu("Import Chemical");
//		jcpep.getMenu().add(jm, 0);
//
//		JMenuItem jmiImportFromTextFile = new JMenuItem(
//				"Import from MDL mol file");
//		jmiImportFromTextFile.setActionCommand("jmiImportFromTextFile");
//		jmiImportFromTextFile.addActionListener(aa);
//		jm.add(jmiImportFromTextFile);
//
//
//		JMenuItem jmiGenerateFromSmiles = new JMenuItem(
//		"Generate from Smiles string");
//		jmiGenerateFromSmiles.setActionCommand("jmiGenerateFromSmiles");
//		jmiGenerateFromSmiles.addActionListener(aa);
//		jm.add(jmiGenerateFromSmiles);
//
//		JMenuItem jmiImportFromDatabase = new JMenuItem(
//		"Import from structure database");
//		jmiImportFromDatabase.setActionCommand("jmiImportFromDatabase");
//		jmiImportFromDatabase.addActionListener(aa);
//		jm.add(jmiImportFromDatabase);
//
//
//	}

	private void setupListeners() {

		jbOK.setActionCommand("jbOK");
		jbOK.addActionListener(aa);


		jbCancel.setActionCommand("jbCancel");
		jbCancel.addActionListener(aa);

		jtfCAS.addKeyListener(ka);
//		jtfFormula.addKeyListener(ka);
//		jtfSmiles.addKeyListener(ka);
//		jtfMW.addKeyListener(ka);

		this.addWindowListener(wa);//applet-omit

//		applet-add (http://forum.java.sun.com/thread.jspa?threadID=5231688&tstart=135)
//		Frame component = (Frame) SwingUtilities.getRoot(this);//trick to get frame object from applet so can add window listener
//		component.addWindowListener(wa);

	}

	private void setupPanels() {

		//		    jpStructureEntry.setBorder(titledBorder1);
		//		    jpStructureEntry.setBounds(new Rectangle(12, 11, 523, 200));
		//		    jpStructureEntry.setLayout(null);

		//		    jpChemical.setBorder(titledBorder2);
		//		    jpChemical.setBounds(new Rectangle(13, 13, 522, 500));
		//		    jpChemical.setLayout(null);

		jpTop.setPreferredSize(new Dimension(360, 115));
		jpTop.setSize(360, 150);
		jpTop.setLayout(null);
		jpTop.setLocation(0, 0);


//		jpTop.add(jlFormula);
//		jpTop.add(jtfFormula);

//		jpTop.add(jlMW);
//		jpTop.add(jtfMW);

//		jpTop.add(jlSmiles);
//		//		    jpTop.add(jspSmiles);
//		jpTop.add(jtfSmiles);

//		jpTop.add(jtfChemicalName);
//		jpTop.add(jspChemicalName);
//		jpTop.add(jlChemicalName);

	}
	
	
	private void setUpToolBars() {
		Component[] comps = this.jcpep.getComponents();
		int count=0;
		
		for (int i = 0; i < comps.length; i++) {
			if (comps[i].getClass().getName().equals("javax.swing.JPanel")) {


				JPanel mainContainer = (JPanel) comps[i];

				Component[] comps2 = mainContainer.getComponents();


				for (int j = 0; j < comps2.length; j++) {
					if (comps2[j].getClass().getName().equals("javax.swing.JToolBar")) {
						count++;
//						System.out.println(i+"\t"+"Toolbar\t"+count);

						JToolBar jtb=(JToolBar)comps2[j];

						Component[]comps3=jtb.getComponents();

						for (int k=0;k<comps3.length;k++) {

							if (comps3[k].getClass().getName().equals("javax.swing.Box")) {
								Box box=(Box)comps3[k];

								Component[]comps4=box.getComponents();

								for (int l=0;l<comps4.length;l++) {

									if (comps4[l].getClass().getName().equals("org.openscience.jchempaint.JCPToolBar$1")) {

										String name=comps4[l].getName();

//										System.out.println(name);

										if (name.equals("reactionArrow")) {
											comps4[l].setVisible(false);
										} else if (name.equals("new")) {										
											comps4[l].removeMouseListener(comps4[l].getMouseListeners()[0]);
											comps4[l].addMouseListener(ml);
//											System.out.println(comps4[l]);
										} else if (name.equals("open")) {
											comps4[l].removeMouseListener(comps4[l].getMouseListeners()[0]);
											comps4[l].addMouseListener(ml);
//											System.out.println(comps4[l]);
										}

									}

								}

							}

//							JButton jbutton=(JButton)comps3[k];
//							System.out.println(jbutton.getName());

						}
					}

				}

			}
		}
	}
	
	class mouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getComponent().getName().equals("new")) {
				
				AtomContainer mol=new AtomContainer();
				mol.setProperty("CAS", "C_"+System.currentTimeMillis());
				mol.setProperty("Error", "Number of atoms equals zero");	
				
				AtomContainerSet ms = new AtomContainerSet();
				ms.addAtomContainer(mol);
				model.setMoleculeSet(ms);
				jcpep.setChemModel(model);
//				this.cleanUpStructure();
				jtfCAS.setText(mol.getProperty("CAS"));
				
			} else if (arg0.getComponent().getName().equals("open")) {
				jmiImportFromTextFile_actionPerformed();		
				
				
			}
		}

	}
	
	private void setupSimpleControls() {
		int s1=20;

		titledBorder1
				.setTitle("Enter structure from one of the following choices:");
		titledBorder1.setBorder(BorderFactory.createEtchedBorder());

		titledBorder2.setTitle("Chemical");
		titledBorder2.setBorder(BorderFactory.createEtchedBorder());

//		int loc1=410; // vertical location of first row of controls below JChemPaint panel
		int loc1=this.getHeight()-70; // vertical location of first row of controls below JChemPaint panel

		jlCAS.setText("CAS # (e.g. 71-43-2):");
		jlCAS.setSize(192, 20);
		jlCAS.setLocation(15, loc1);


		jtfCAS.setSize(120, 20);
		jtfCAS.setLocation(130, loc1);
		jtfCAS.setName("jtfCAS");
		jtfCAS.setEditable(false);

		jbOK.setSize(100,20);
		jbOK.setLocation(getWidth()-jbOK.getWidth()-s1, loc1);
		jbOK.setText("OK");

		jbCancel.setSize(100, 20);
		jbCancel.setLocation(jbOK.getX()-s1-jbCancel.getWidth(),loc1);
		jbCancel.setText("Cancel");
		jbCancel.setVisible(true);

		// jbMolFileBrowse.setBounds(new Rectangle(417, 146, 96, 23));
		// jbMolFileBrowse.setText("Browse...");


		this.add(this.jbOK);
		this.add(this.jbCancel);

		this.add(jlCAS);
		this.add(jtfCAS);


	}

//	private void jmiImportFromDatabase_actionPerformed(ActionEvent e) {
//
////		try {
////			this.myfraStructureDatabaseSearch.SetupStructure(jcpep.createImage(100,100));
////		} catch (Exception ex) {
////			System.out.println(ex);
////		}
//
//		this.myfraStructureDatabaseSearch.setVisible(true);
//
//		if (this.myfraStructureDatabaseSearch.jrbCAS.isSelected()) {
//			this.myfraStructureDatabaseSearch.jtfCAS.requestFocus();
//		} else if (this.myfraStructureDatabaseSearch.jrbMW.isSelected()) {
//			this.myfraStructureDatabaseSearch.jtfMW.requestFocus();
//		} else if (this.myfraStructureDatabaseSearch.jrbFormula.isSelected()) {
//			this.myfraStructureDatabaseSearch.jtfFormula.requestFocus();
//		}
//
//	}

	private void jmiImportFromTextFile_actionPerformed() {

		// TODO: add code to allow user to decide which molecule in SDF file
		// to run or allow batch mode

		int returnVal = chooser1.showOpenDialog(jcpep);
		String type = null;
		IChemObjectReader cor = null;

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;


		File inFile = chooser1.getSelectedFile();

		if (inFile.getName().indexOf(".")==-1) {
			inFile=new File(inFile.getAbsolutePath()+".mol");
		}

		jcpep.setLastOpenedFile(inFile);

		this.setCursor(Utilities.waitCursor);
		
		AtomContainer ac=TaskStructureSearch.loadFromMolFile(inFile,this);
		
		if (ac!=null) {
			configureModel(ac,selectedRow,Index);
			jtfCAS.setText(ac.getProperty("CAS"));
		}
		
		this.setCursor(Utilities.defaultCursor);

	}


//	/**
//	 * Currently this method loads a molecule via a MDL molfile
//	 * @param inFile file for MDL molfile
//	 */
//	AtomContainer loadFromMolFile(File inFile) {
//
//		if (!inFile.exists()) {
//			JOptionPane.showMessageDialog(getParent(), "\""+inFile.getAbsolutePath()+"\" cannot be found.");
//			return null;
//		}
//
//		System.out.println("*"+inFile.getAbsolutePath()+"*");
//		
//		AtomContainerSet acs=TaskStructureSearch.LoadFromSDF(inFile.getAbsolutePath());
//		
//		if (acs.getAtomContainerCount()==0) {
//			JOptionPane.showMessageDialog(getParent(),
//					"Molecule not loaded, please select another mol file");
//			return null;
//		}
//
//		
//		AtomContainer molecule=(AtomContainer)acs.getAtomContainer(0);
//
//		String error=molecule.getProperty("Error");
//		if (error.contentEquals("Multiple molecules")) {
//			JOptionPane.showMessageDialog(getParent(),"Molecule not connected, please select another mol file");
//			return null;
//		}
//
//		configureModel(molecule, selectedRow, Index);
//		
//		jtfCAS.setText(molecule.getProperty("CAS"));
//		
//		return molecule;
//		
////		f.setNameCAS(molecule);
//
//	}



	


	//	private IMolecule Calculate3DCoordinates(IMolecule molecule) {
	//
	//		coordinategenerator cg = new coordinategenerator();
	//
	//		IMolecule molecule2=null;
	//
	//		try {
	//			molecule2=(IMolecule)molecule.clone();
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//
	//		}
	//
	//		//Get the CAS number for the molecule
	//		String cast = (String)molecule2.getProperty("CAS");
	//		String cas = cast.trim();
	//
	//		//compare molecules stored in the directory and molecule2
	//		File file4 = new File(foldername +"coords/" +cas +".mol");
	//
	//		//State locations of MOPAC and TINKER output files
	//		File file = new File(foldername + "mopac/" +cas +".mno"); //Contains MOPAC output file
	//		File file1 = new File(foldername +"surface/" +cas +"-vdw.out"); //Contains surface and volume calculations based on van der Waals surface area
	//		File file2 = new File(foldername +"surface/" +cas +"-sasa.out"); //Contains surface and volume calculations based on Solvent Access Surface
	//		File file3 = new File(foldername +"surface/" +cas +"-cra.out"); //Contains surface and voume calculations based on Contact Re-entrant area
	//
	//		if (!file.exists() || !file1.exists() || !file2.exists() || !file3.exists())
	//			molecule2 = cg.generate3Dcoordinates(molecule2);
	//
	//		if (file.exists()) {
	//			mopacreader mr = new mopacreader();
	//			mr.readxyz(molecule2,file);
	//		}
	//
	//		return molecule2;
	//
	//
	//	}


	public IChemModel getModel() {
		return this.model;
	}




	//	  private IChemObjectReader GetReader(File inFile) {
	//		  IChemObjectReader cor=null;
	//
	//		  String type = null;
	//
	//			// try to determine from user's guess
	//			try {
	//				FileInputStream reader = new FileInputStream(inFile);
	//				javax.swing.filechooser.FileFilter ff = chooser1.getFileFilter();
	//
	//				if (ff instanceof JCPFileFilter) {
	//					type = ((JCPFileFilter) ff).getType();
	//				}
	//				else {
	//					type = "unknown";
	//				}
	//
	//				if (type.equals(JCPFileFilter.cml) || type.equals(JCPFileFilter.xml)) {
	//					cor = new CMLReader(reader);
	//				}
	//				else if (type.equals(JCPFileFilter.sdf)) {
	//					cor = new MDLReader(reader);
	//				}
	//				else if (type.equals(JCPFileFilter.mol)) {
	//					cor = new MDLReader(reader);
	//				}
	//				else if (type.equals(JCPFileFilter.inchi)) {
	//					cor = new INChIReader(reader);
	//				}
	//
	//
	//			} catch (Exception exception) {
	//				JOptionPane.showMessageDialog(this,exception);
	//			}
	//
	//			return cor;
	//
	//	  }



//	private void generateFromSmiles(String smiles) {
////		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
////		try {
////			String Smiles = smiles.trim();
////			Smiles = CDKUtilities.FixSmiles(Smiles);
////
////			org.openscience.cdk.smiles.SmilesParser sp = new org.openscience.cdk.smiles.SmilesParser();
////			//ToxPredictor.Utilities.SmilesParser sp=new ToxPredictor.Utilities.SmilesParser();
////
////			IMolecule molecule = sp.parseSmiles(Smiles);
////
////			if (!(molecule instanceof IMolecule)) {
////				JOptionPane.showMessageDialog(this, "Invalid smiles");
////				return;
////			}
////
////			DescriptorFactory df=new DescriptorFactory(false);
////			df.Normalize(molecule);
////
////			this.configureModel(molecule);
////
////
////
////		} catch (InvalidSmilesException ise) {
////			JOptionPane.showMessageDialog(this,
////					"Invalid SMILES or parse error: " + ise.getMessage());
////		} catch (Exception ex) {
////			JOptionPane.showMessageDialog(this, ex);
////		}
//	try {
//		this.setCursor(Utilities.waitCursor);
//
//		String Smiles = smiles.trim();
////		Smiles = CDKUtilities.FixSmiles(Smiles);//TODO
//
////		org.openscience.cdk.smiles.SmilesParser sp = new org.openscience.cdk.smiles.SmilesParser();
//
//
//
//
//		AtomContainer molecule=null;
////
////		if (!(molecule instanceof IMolecule)) {
////			JOptionPane.showMessageDialog(this, "Invalid smiles");
////			return;
////		}
//
//		try {
//
//			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
//
//			molecule = (AtomContainer)sp.parseSmiles(smiles);
//
//
//		} catch (org.openscience.cdk.exception.InvalidSmilesException e) {
//
//			JOptionPane.showMessageDialog(this,
//					"Invalid SMILES or parse error: " + e.getMessage()+"\nSMILES = \""+smiles+"\"");
//			this.setCursor(Utilities.defaultCursor);
//			return;
//
//		}
//
//		df.Normalize(molecule);
//		this.configureModel(molecule);
//
//		java.util.ArrayList<String> l = (java.util.ArrayList<String>) cf
//		.FindCASFromMolecule(molecule,fraMain8.compareMethod);
//
//		this.setCursor(Utilities.defaultCursor);
//
//		if (l.size() >1) {
//			myfraSelectChemicalFromDB.SetupTable(l);
//			myfraSelectChemicalFromDB.setVisible(true);;
//
//		}else if (l.size() ==1) {
//			//just add CAS to ID:
//			String CAS=l.get(0);
//			this.jtfCAS.setText(CAS);
//			JOptionPane.showMessageDialog(this, "Match found in database, Molecule ID: "+CAS);
//
//		} else if (l.size()==0) {
////			String formula=CDKUtilities.GenerateFormula(molecule);
//
////			IAtomContainer molecule2=DescriptorFactory.addHydrogens(molecule);//TODO- is this needed?
//			String formula=MolecularFormulaManipulator.getMolecularFormula(molecule).toString();
//
//			this.jtfCAS.setText(formula+"_"+System.currentTimeMillis());
//			this.jtfCAS.setCaretPosition(0);
//		}
//
//	} catch (Exception ex) {
//		JOptionPane.showMessageDialog(this, ex);
//	}
//
//
//	}

//	private void jmiGenerateFromSmiles_actionPerformed() {
//
//		String smiles=JOptionPane.showInputDialog(this,"Enter a SMILES string",currentSmiles);
//
//
//		if (smiles==null) {
//			return;
//		}
//		currentSmiles=smiles;
//
//		if (smiles.indexOf(".")>-1) {
//			JOptionPane.showMessageDialog(this, "Please enter a single structure without a \".\" in the SMILES string");
//			return;
//		}
//
//		if (smiles.length() == 0) {
//			JOptionPane.showMessageDialog(this, "No Smiles entered");
//			return;
//		}
//		this.generateFromSmiles(smiles);
//
//	}

//	@SuppressWarnings("unused")
//	private void generateModel(IMolecule molecule) {
//		if (molecule == null)
//			return;
//
//		// ok, get relevent bits from active model
//		JChemPaintModel jcpModel = jcpep.getJChemPaintModel();
//		Renderer2DModel renderModel = jcpModel.getRendererModel();
//		org.openscience.cdk.interfaces.IChemModel chemModel = jcpModel
//				.getChemModel();
//		org.openscience.cdk.interfaces.IMoleculeSet moleculeSet = chemModel
//				.getMoleculeSet();
//		if (moleculeSet == null) {
//			moleculeSet = new MoleculeSet();
//		}
//
//		// ok, now generate 2D coordinates
//		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
//		sdg.setTemplateHandler(new TemplateHandler(moleculeSet.getBuilder()));
//		try {
//			sdg.setMolecule(molecule);
//			sdg.generateCoordinates(new Vector2d(0, 1));
//			molecule = sdg.getMolecule();
//			double bondLength = renderModel.getBondLength();
//			HashMap rc = renderModel.getRenderingCoordinates();
//
//			double scaleFactor = GeometryTools.getScaleFactor(molecule,
//					bondLength, rc);
//			GeometryTools.scaleMolecule(molecule, scaleFactor, renderModel
//					.getRenderingCoordinates());
//			//if there are no atoms in the actual chemModel all 2D-coordinates would be set to NaN
////			if (ChemModelManipulator.getAllInOneContainer(chemModel)
////					.getAtomCount() != 0) {
////				Point2d center = GeometryTools.get2DCenter(ChemModelManipulator
////						.getAllInOneContainer(chemModel), rc);
////
////				//	            	GeometryTools.translate2DCenterTo(molecule,center,rc);
////			}
//			GeometryTools.translate2D(molecule, 5 * bondLength, 0, renderModel
//					.getRenderingCoordinates()); // in pixels
//		} catch (Exception exc) {
//			exc.printStackTrace();
//		}
//
//		moleculeSet.addMolecule(molecule);
//		//	        renderModel.setSelectedPart(m);
//		jcpep.getChemModel().setMoleculeSet(moleculeSet);
//		jcpep.scaleAndCenterMolecule(jcpep.getChemModel());
//		jcpModel.fireChange(jcpep.getChemModel());
//	}

	public void cleanUpStructure() {

		jmiCleanup.doClick();//TODO

		if (MakeHydrogensImplicit) {
//			jmiMakeImplicit.doClick();//TODO	//need to make hydrogens implicit after cleaning up because this sometimes kills implicit hydrogens
		}


	}

	private void jbCancel_actionPerformed(ActionEvent e) {

		if (Debug) System.exit(0);

		this.setVisible(false);

	}

//	//weird things happen if have just 1 atom (2 duplicate molecules are in som)
//	private void fix(IAtomContainerSet som) {
//
//		boolean AllHave1Atom=true;
//
//		for (int i = 0; i <= som.getAtomContainerCount() - 1; i++) {
//			IAtomContainer m = som.getAtomContainer(i);
//			int atomCount = m.getAtomCount();
//
//			if (atomCount!=1) {
//				AllHave1Atom=false;
//			}
//		}
////		System.out.println(som.getMoleculeCount());
//		//			 loop though Set of Molecules to delete molecules with no atoms:
//		for (int i = 0; i <= som.getAtomContainerCount() - 1; i++) {
//			IAtomContainer m = som.getAtomContainer(i);
//			int atomCount = m.getAtomCount();
//
//			if (AllHave1Atom) {//weird things happen if have just 1 atom (2 duplicate molecules are in som)
//
////				if (atomCount==1) {
////					IAtom atom=m.getAtom(0);
////					IMolecule mnew=new Molecule();
////					mnew.addAtom(atom);
////					m=mnew;
////				}
//
////				if (mfu.HaveBadElement(m)) m.setProperty("Error", "Molecule contains unsupported element");
////				else if (atomCount==1) m.setProperty("Error", "Molecule contains only 1 atom");
//
////				m.setProperties(this.htProps);//preserve original properties if loaded from SDF
//
//				if (atomCount==1) m.setProperty("Error", "Only one nonhydrogen atom");
//
//				m.setProperty("CAS", jtfCAS.getText().trim());
//				m.setProperty("Index", molNum+1);
//
//				AtomContainerSet newMolSet=new AtomContainerSet();//create a new molecule set
//				AtomContainerSet oldMolSet=pbc.moleculeSet;
//
//				for (int j=0;j<pbc.moleculeSet.getAtomContainerCount();j++) {
//					if (j!=molNum) newMolSet.addAtomContainer(oldMolSet.getAtomContainer(j));
//					else newMolSet.addAtomContainer(m);
////					if (j!=molNum) newMolSet.addMolecule((IMolecule)oldMolSet.getMolecule(j).clone());
//
//				}
//
//				this.pbc.SetupTable(newMolSet);
//				this.pbc.table.setRowSelectionInterval(molNum,molNum);
//				this.setVisible(false);
//				return;
//			}
//
////			System.out.println(i+"\t"+atomCount);
//
//			if (atomCount == 0) {
//				// delete molecules with no atoms:
//				som.removeAtomContainer(i--);
//			}
//		}//end loop over molecules in som
//
//	}
	

	private void jbOK_actionPerformed(ActionEvent e) {
		if (Debug) System.exit(0);

		IChemModel cm = null;
		IAtomContainerSet som = null;

		if (this.jtfCAS.getText().equals("")) {
			JOptionPane.showMessageDialog(this,"Enter a CAS# (if one is not available enter a dummy value).\n\nThe CAS number is used to store the results for each chemical\nin its own folder inside the main output folder.","CAS number missing",JOptionPane.WARNING_MESSAGE);
			this.jtfCAS.requestFocus();
			return;
		}

		
//		System.out.println("Sel row2="+molNum);

		try {

//			cm = model.getChemModel();
//			som = (IMoleculeSet)cm.getMoleculeSet();

			som=(AtomContainerSet)model.getMoleculeSet().clone();

			//TODO- do we need fix???
			
			this.fix(som);//weird things happen if have just 1 atom (2 duplicate molecules are in som)

			if (!(som instanceof IAtomContainerSet) || som.getAtomContainerCount() == 0) {
				JOptionPane.showMessageDialog(this, "Enter a structure or click Cancel");
				return;
			}

			if (som.getAtomContainerCount() > 1) {
				JOptionPane.showMessageDialog(this, "Enter only 1 structure");
				return;
			}

			AtomContainer m  = (AtomContainer)som.getAtomContainer(0);
			
			if (m.getAtomCount() == 0) {
				JOptionPane.showMessageDialog(this, "Enter a structure or click Cancel");
				return;
			}

			
			ArrayList<DSSToxRecord> recs = ResolverDb.lookupByAtomContainer(m);

//			if ( recs.size() > 0 ) {
//				
//				if (!recs.get(0).cas.contentEquals(jtfCAS.getText().trim())) {
//					
//					if (jtfCAS.getText().contains("C_")){
//						ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);		
//					} else {
//						String message="The ID ("+jtfCAS.getText().trim()+ ") does not match the CAS ("+recs.get(0).cas+") for the structure in the database, do you wish to update it?";
//						int Result = JOptionPane.showConfirmDialog(this, message, "Update CAS", JOptionPane.OK_CANCEL_OPTION);
//						if (Result == JOptionPane.OK_OPTION) {
//							ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);		
//						} else if (Result==JOptionPane.CANCEL_OPTION) {
//							m.setProperty("CAS", jtfCAS.getText().trim());
//						}
//					}
//					
//				} else if (jtfCAS.getText().contains("C_")){
//					ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);
//				}
//					
//			} else {
//				m.setProperty("CAS", jtfCAS.getText().trim());	
//			}
			
			if (recs.size()>0)	{
				assignRecord(m, recs);
			}else  {				
				
				ArrayList<DSSToxRecord>recs2=ResolverDb.lookupByAtomContainer2dConnectivity(m);
				
				if (recs2.size()>0)	{
					assignRecord(m, recs2);
				} else {
					DSSToxRecord.clearProperties(m);	
				}
			}
			
								
			m.setProperty("Error", "");
			WebTEST4.checkAtomContainer(m);

			m.setProperty("Index", Index);
			
			MyTableModel tableModel=(MyTableModel)pbc.table.getModel();
			tableModel.updateRow(m,selectedRow);

			this.setVisible(false);
						
			pbc.table.addRowSelectionInterval(selectedRow ,selectedRow);


		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex);
			ex.printStackTrace();
		}


	}

	private void assignRecord(AtomContainer m, ArrayList<DSSToxRecord> recs) {
		String oldCAS=jtfCAS.getText().trim();

		if (Strings.isBlank(oldCAS) || oldCAS.contains("C_")) {
			ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);
		} else {
			boolean match=false;
			for (DSSToxRecord rec:recs) {
				if (rec.cas.contentEquals(oldCAS)) {
					DSSToxRecord.assignFromDSSToxRecord(m, rec);
//							System.out.println("old CAS is ok!");
					match=true;
					break;
				}
			}
			if (!match) ResolverDb.assignDSSToxInfoFromFirstRecord(m, recs);
		}
	}
	
	
	


private void fix(IAtomContainerSet som) {
	// TODO Auto-generated method stub
	
//	for (int i=0;i<som.getAtomContainerCount();i++ ) {
//		
//		AtomContainer ac=(AtomContainer)som.getAtomContainer(i);
//		System.out.println(i+"\t"+ac.getAtomCount()+"\t"+ac.getProperty("Index"));		
//	}
	
	
}

//	private boolean GetOutputFolder() {
//
//
//
//		int returnVal = chooser2.showOpenDialog(this);
//
//		if (returnVal == JFileChooser.APPROVE_OPTION) {
//
//			File inFile = chooser2.getSelectedFile();
//
//			if (!inFile.exists()) {
//				int Result = JOptionPane.showConfirmDialog(this, "The folder, "
//						+ inFile.getName()
//						+ ", does not exist, do you wish to create it?",
//						"Create folder?", JOptionPane.OK_CANCEL_OPTION);
//				//
//				if (Result == JOptionPane.OK_OPTION) {
//					inFile.mkdir();
//				} else {
//					return false;
//				}
//			}
//
//			// JOptionPane.showMessageDialog(this,inFile.exists()+"\t"+inFile.isDirectory());
//
//			if (inFile.exists()) {
//				this.OutputFolder = inFile;
//			} else {
//				return false;
//			}
//
//		} else {
//			return false;
//		}
//
//		return true;
//
//	}

//	private void loadChemicalForDebug(String CAS) {
//
//		this.jtfCAS.setText(CAS);
//		myfraStructureDatabaseSearch.ImportFromCAS(jtfCAS);
//
//	}




//	public static void main(String[] args) { //applet-omit
//		// TODO Auto-generated method stub
//		fraEditChemical7 f = new fraEditChemical7();
//		f.Debug=true;
//
//		f.init(650,500);
////		f.jcbMethod.setSelectedItem(f.HierarchicalMethodChoice);
////		f.jcbMethod.setSelectedItem(f.FDAMethodChoice);
////		f.jcbMethod.setSelectedItem(f.GroupContributionMethodChoice);
////		f.jcbEndPoint.setSelectedItem(f.FHM_LC50Choice);
//
//
//
//		f.setVisible(true);
//		f.loadChemicalForDebug("71-43-2");
//	}




	class keyAdapter extends java.awt.event.KeyAdapter {

		public void keyReleased(KeyEvent e) {
			Object o = e.getSource();
			JTextField jtf;
			String name = "";

			if (e.getKeyCode() != e.VK_ENTER)
				return;


			if (o instanceof JTextField) {
				name = ((JTextField) o).getName();
				boolean HasFocus = ((JTextField) o).hasFocus();
			} else if (o instanceof JTextArea) {
				name = ((JTextArea) o).getName();
			}

//			if (name.equals("jtfCAS")) {
//				myfraStructureDatabaseSearch.ImportFromCAS(jtfCAS);
//			}

		}
	}




	class actionAdapter implements java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("jbOK")) {
				jbOK_actionPerformed(e);
			} else if (e.getActionCommand().equals("jbCancel")) {
				jbCancel_actionPerformed(e);
			}
		}

	}

	class windowAdapter extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub

			if (Debug) System.exit(0);

			jbCancel.doClick();
			super.windowClosing(arg0);
		}



	}//class ApplicationCloser

} //end class

