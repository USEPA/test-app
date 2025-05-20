package ToxPredictor.Application.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Handler;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.ws.rs.ForbiddenException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.ChemModel;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.jchempaint.JCPPropertyHandler;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.application.JChemPaint;
import org.openscience.jchempaint.io.JCPFileFilter;
import org.openscience.jchempaint.io.JCPFileView;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Miscellaneous.FileFilterStructure;
import ToxPredictor.Application.GUI.Miscellaneous.FileFilterText;
import ToxPredictor.Application.GUI.Miscellaneous.FileFilterText2;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.Application.Calculations.TaskCalculations2;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.HueckelAromaticityDetector;
//import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.Utilities.chemicalcompare;


public class TESTApplication extends JFrame{

	//	
	public static boolean defaultToSingleChemicalMode=true;
	public boolean includeLDA=true;
	public boolean includeAA_Dashboard=false;
	
	public static String versionToxVal="v96";
	
	public boolean includeRecentMolFileMenu=false;
	public boolean includeER=false;
	public boolean includeCTS=true;
	
	public static boolean forMDH=false;
	public static boolean includeExposure=true;

	String endpoint;
	String method;
	boolean isBinaryEndpoint;
	boolean isLogMolarEndpoint;
	String DescriptorSet="2d";
	//	String DescriptorSet="FDA_Subset";

	DescriptorFactory df = new DescriptorFactory(false);

	public static String compareMethod = chemicalcompare.methodHybrid;// fast and	// accurate

	boolean Use3D = false;
	boolean is3Dpresent = true;
	//	private boolean is3Dfilepresent = false;
	//	private boolean FillInTestValues=false;

	boolean MakeHydrogensImplicit=!Use3D;

	public ApplicationSettings as;

	String currentSmiles="";

	//	IAtomContainer myMolecule = null;

	//*********************************************************************************************************

	IChemModel model = JChemPaint.emptyModel();
	public JChemPaintPanel jChemPaintPanel;
	JPanel panelDraw=new JPanel();


	//*************************************************************************************
	// Vectors to store endpoints and choices
	public Vector<String> endPointsToxicity=null; //defined later in setUpChoices()
	public Vector<String> endPointsPhysicalProperty=null; //defined later in setUpChoices()
	//	public boolean doEndpointActionPerformed=true;

	public Vector<String>Methods=new Vector<String>();

	//Hashtable to retrieve abbreviation for endpoint choice:
	public Hashtable<String,String> htAbbrevChoice=new Hashtable<String,String>();

	//************************************************************************************
	//Decision variables
	public String defaultMethod=TESTConstants.ChoiceConsensus;
	public boolean includeToxicityEndpoints=true;
	public boolean includePhysicalPropertyEndpoints=true;
	public boolean includePhysicalPropertyDataSets=true;//if true it allows users to batch load data sets for physical properties- this might need to be set to false for copyright concerns
	public boolean includejcbMethod=true;
	public static boolean includeFDA=false;

	//*************************************************************************************
	//simple controls:
	//	private JLabel jl3DMessage = new JLabel();
	//	JLabel jlCAS_Info = new JLabel();
	//	JButton jbEnterStructure = new JButton();

	private JButton jbCalculate = new JButton();
	private JButton jbCalculateAA = new JButton();
	JButton jbStop = new JButton();
	//	JButton jbSaveSDF=new JButton();
	private JTextField jtfCalcStatus = new JTextField();
	//	JButton jbCloseBatch=new JButton();
	public JButton jbSwitchToSingle = new JButton();
	JButton jbSwitchToBatch = new JButton();


	//********************************************

	private String StructureFolder="ValidatedStructures2d";

	private String strFileSep="/";//TMM 11/20/08: for some reason using File.separator doesnt work now when specifiying path in jar files

	//	public ChemicalFinder cf = new ChemicalFinder(
	//			StructureFolder+strFileSep+"manifest.txt", StructureFolder);

	//********************************************
	//	public fraOptions myfraOptions=new fraOptions();
	AboutDialog aboutDialog = new AboutDialog();
	DialogAboutQSARMethods myfraAbout=new DialogAboutQSARMethods();

	public PanelStructureDatabaseSearchBatch panelBatchStructureDatabaseSearch=new PanelStructureDatabaseSearchBatch();
	public PanelStructureDatabaseSearchSingle panelSingleStructureDatabaseSearch=new PanelStructureDatabaseSearchSingle();
	public PanelCalculationOptions panelCalculationOptions=new PanelCalculationOptions();
	public PanelBatchChemicals panelBatch=new PanelBatchChemicals();
	public PanelResults panelResults=new PanelResults(this,"Prediction results",false);

//	public PanelCTSOptions panelCTSOptions=new PanelCTSOptions();
	
	//********************************************
	TaskStructureSearch taskStructureFile = new TaskStructureSearch();
	//	public TaskCalculations task = new TaskCalculations();//commented out by TMM, 2/2/18
	public TaskCalculations2 task = new TaskCalculations2();//commented out by TMM, 2/2/18

	//*********************************************************************
	JFileChooser chooser1 = new JFileChooser();
	JFileChooser chooserSDF = new JFileChooser(""); //
	JFileChooser chooserCAS = new JFileChooser(""); //
	JFileChooser chooserCSV = new JFileChooser(""); //
	JFileChooser chooserHTML = new JFileChooser(""); //
	JFileChooser chooserExcel = new JFileChooser(""); //
	JFileChooser chooserSmiles = new JFileChooser("");
	FileFilter currentFilter = null;

	//***************************
	JMenuItem jmiCleanup = null;
	JMenuItem jmiMakeImplicit = null;
	JMenu jmRecentMolFiles;
//	JMenu jmRecentBatchResultsFiles;
	//***************************

	TESTApplicationActionAdapter aa = new TESTApplicationActionAdapter(this);
	private windowAdapter wa = new windowAdapter();

	mouseListener ml=new mouseListener();

	javax.swing.Timer timerCalculations;
	javax.swing.Timer timerBatchStructureFile;

	JMenuBar menuBar = new JMenuBar();
	JMenu jmFileSingle = new JMenu("File");
	JMenu jmFileBatch = new JMenu("File");



	public TESTApplication() {
		init();

//		panelCalculationOptions.jcbEndpoint.setSelectedItem(TESTConstants.ChoiceFHM_LC50);


	}

	/**
	 * Used to make sure that sub forms are displayed when TEST has been
	 * switched to from another program
	 *
	 * @author TMARTI02
	 *
	 */
	class windowAdapter extends WindowAdapter {

		@Override
		public void windowActivated(WindowEvent e) {

			//			if (myfraSelectOutputFolder.isVisible()) {
			//				myfraSelectOutputFolder.requestFocus();
			if (panelBatch.myfraEditChemical.isVisible()) {
				panelBatch.myfraEditChemical.requestFocus();
				//			} else if (myfraSelectChemicalFromDB.isVisible()) {
				//				myfraSelectChemicalFromDB.requestFocus();
				//			} else if (myfraStructureDatabaseSearch.isVisible()) {
				//				myfraStructureDatabaseSearch.requestFocus();
			} else if (myfraAbout.isVisible()) {
				myfraAbout.requestFocus();
				//			} else if (myfraOptions.isVisible()) {
				//				myfraOptions.requestFocus();
			}

			super.windowActivated(e);
		}

	}// class ApplicationCloser



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
				model=JChemPaint.emptyModel();
				jChemPaintPanel.setChemModel(model);
				panelSingleStructureDatabaseSearch.jtfCAS.setText("");
				panelSingleStructureDatabaseSearch.jtfName.setText("");
				setTitle(TESTConstants.SoftwareTitle);
			} else if (arg0.getComponent().getName().equals("open")) {
				aa.jmiImportFromTextFile_actionPerformed(null);
				setTitle(TESTConstants.SoftwareTitle);
			}
		}

	}






	/**
	 * This method loads the application settings from the stored xml file in
	 * the user's application directory. The file is usually located at
	 * C:\Documents and Settings\USERNAME\.TEST\settings.xml for Windows 7
	 */
	private void loadApplicationSettings() {

		ApplicationSettings as=new ApplicationSettings();
		
		this.as=as.loadSettingsFromFile();
		

		//		System.out.println(as.getOutputFolderPath());

	}

	/**
	 * Initializes the class by setting up all java controls
	 */
	public void init() {

		//		WebTEST.createDetailedConsensusReport=true;
		int inset=20;	
		
		this.setupJChemPaintEditorPanel();
		this.loadApplicationSettings();
		this.setUpChoices();
		this.setupMenus();
		this.getContentPane().setLayout(null);


		if (this.getClass().toString().equals("class ToxPredictor.Application.ToxApplet7")) {
			this.setSize(650,520);
		} else {
			// Get the current screen size
			Dimension scrnsize = Toolkit.getDefaultToolkit().getScreenSize();

			int height=(int)(scrnsize.height*0.9);
			int width=(int)(scrnsize.width*0.95);
			//			int width=(int)(height);
			if (width<700) width=700;

			this.setSize(width,height);

			if (width < 800) {
				// maximize window
				this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				this.setVisible(true);
			}
		}
		ToxPredictor.Utilities.Utilities.CenterFrame(this); //applet-omit-04


		// controls state of maximize button, if resizable is false maximize button is grayed out - JH 08/15/2017
		this.setResizable(false);//applet-omit-01

		try { //applet-omit-07
			URL url=this.getClass().getClassLoader().getResource("epa_logo_icon.jpg");//applet-omit-07
			this.setIconImage(new ImageIcon(url).getImage()); //applet-omit-07
			//			this.setIconImage(new ImageIcon("fish_logo.jpg").getImage()); //applet-omit-07

		} catch (Exception e) {//applet-omit-07
			e.printStackTrace();//applet-omit-07
		}//applet-omit-07


		this.setupSimpleControls(inset);		

		//		this.SetupPanels();
		this.setupListeners();
		this.setupFileChoosers();

		//		Utilities.SetFonts(this.getContentPane());
		this.setUpTimers();

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//applet-omit-02
		this.setTitle(TESTConstants.SoftwareTitle);//applet-omit-03

		setUpPanels(inset);

		if (panelDraw.getWidth() < 700) removeExtraToolBars();
		
		repaint();
		
	}

	private void setUpPanels(int inset) {
		
		
		int heightRight=(int)(this.getHeight()-155);
		int widthRight=(int)(this.getWidth()*0.55);
		
//		if (widthRight<650) widthRight=650;
		
//		System.out.println(widthRight);

		int widthLeft=this.getWidth()-widthRight-3*inset;
		int heightUpperLeft=this.getHeight()/3;
		int heightLowerLeft=heightRight-heightUpperLeft;

		int xlocationRight=widthLeft+2*inset;
		int ylocationRight=inset;



		panelDraw.setSize(widthRight,heightRight);
		panelDraw.setLocation(xlocationRight, ylocationRight);
		panelDraw.setLayout(null);
		panelDraw.add(jChemPaintPanel);

		TitledBorder border = new TitledBorder("Draw Chemical");
		border.setTitleJustification(TitledBorder.LEFT);
		border.setTitlePosition(TitledBorder.TOP);
		panelDraw.setBorder(border);


		jChemPaintPanel.setSize(widthRight-2*inset,heightRight-3*inset);
		jChemPaintPanel.setLocation(inset, 2*inset);

		this.panelBatch.init((int)(widthRight), heightRight);
		this.panelBatch.setLocation(xlocationRight, ylocationRight);

		this.panelBatchStructureDatabaseSearch.SetParentFrame(this);
		this.panelSingleStructureDatabaseSearch.SetParentFrame(this);

		
		this.panelBatchStructureDatabaseSearch.setLocation(inset,inset);
		this.panelBatchStructureDatabaseSearch.init(widthLeft,heightUpperLeft);

		this.panelSingleStructureDatabaseSearch.setLocation(inset,inset);
		this.panelSingleStructureDatabaseSearch.init(widthLeft,heightUpperLeft);
		
		this.panelCalculationOptions.init(widthLeft,heightLowerLeft,this);
		this.panelCalculationOptions.setLocation(inset,panelSingleStructureDatabaseSearch.getHeight()+inset);

		//		this.myfraSelectOutputFolder.SetParentFrame(this);
		//		this.myfraOptions.SetParentFrame(this);

		this.add(panelDraw);
		this.add(panelBatch);
		this.add(panelBatchStructureDatabaseSearch);
		this.add(panelSingleStructureDatabaseSearch);
		this.add(panelCalculationOptions);
		


	}

	private void setupListeners() {





		jbCalculate.setActionCommand("jbCalculate");
		jbCalculate.addActionListener(aa);

		jbCalculateAA.setActionCommand("jbCalculateAA");
		jbCalculateAA.addActionListener(aa);

		jbStop.setActionCommand("jbStop");
		jbStop.addActionListener(aa);

		jbSwitchToBatch.setActionCommand("jbSwitchToBatch");
		jbSwitchToBatch.addActionListener(aa);

		jbSwitchToSingle.setActionCommand("jbSwitchToSingle");
		jbSwitchToSingle.addActionListener(aa);

		this.addWindowListener(wa);// applet-omit-08

		// applet-add-08
		// (http://forum.java.sun.com/thread.jspa?threadID=5231688&tstart=135)
		// Frame component = (Frame) SwingUtilities.getRoot(this);//trick to get
		// frame object from applet so can add window listener
		// component.addWindowListener(wa);

	}

	//	void loadChemicalFromMolFile() {
	//
	//		String folder = "//Aa.ad.epa.gov/ord/CIN/Users/main/Q-Z/TMARTI02/Net MyDocuments";
	//		String filename="55-63-0.mol";
	//
	//		File inFile=new File(folder+"/"+filename);
	//
	//		taskStructureFile.loadFromMolFile(inFile,false,this);
	//
	//	}


	/**
	 * Convenience method to load a chemical from the structure database to
	 * debug the program
	 */
	//	public void loadChemicalForDebug(String CAS) {
	//		this.jtfCAS.setText(CAS);
	//		this.jbEnterStructure.doClick();
	//	}

	private void setupFileChoosers() {
		// JCPFileFilter.addChoosableFileFilters(this.chooser1);

		chooser1.setFileView(new JCPFileView());
		chooser1.setAcceptAllFileFilterUsed(false);

		// for now only add the following:
		// chooser1.addChoosableFileFilter(new
		// JCPFileFilter(JCPFileFilter.smi));
		// chooser1.addChoosableFileFilter(new
		// JCPFileFilter(JCPFileFilter.sdf));
		chooser1.addChoosableFileFilter(new JCPFileFilter(JCPFileFilter.mol));

		chooserSDF.setFileFilter(new FileFilterStructure());

		chooserSDF.setDialogTitle("Select a SDF File");

		chooserCAS.setFileFilter(new FileFilterText(FileFilterText.txt));
		chooserCAS.setDialogTitle("Select a text file containing only the CAS number on each line");

		
		chooserCSV.setFileFilter(new FileFilterText(FileFilterText.csv));
		chooserCSV.setDialogTitle("Select a csv file");
		
		chooserExcel.setFileFilter(new FileFilterText(FileFilterText.xlsx));
		chooserExcel.setDialogTitle("Select a xlsx file");

		chooserHTML.setFileFilter(new FileFilterText(FileFilterText.html));
		chooserHTML.setDialogTitle("Select a html file");

		
		chooserSmiles.setFileFilter(new FileFilterText2());
		// TextFileFilter.addChoosableFileFilters(chooserSmiles);
		chooserSmiles.setDialogTitle("Select file containing only SMILES and identifier on each line");

		// set up chooser2:
		JPanel jpanelHelp = new JPanel();

		jpanelHelp.setBounds(0, 0, 100, 100);
		// jpanelHelp.setPreferredSize(new Dimension(100,100));
		// jpanelHelp.setLayout(null);

		JLabel jlHelp = new JLabel();
		// jlHelp.setSize(95,95);
		// jlHelp.setLocation(5,5);

		jlHelp.setText("<html>This dialog will select the folder<br>" + " for storing the results.<br><br>"
				+ "If the \"My Documents\" folder is selected,<br>" + "the results files will be stored in<br>"
				+ "\"My Documents/MyToxicity/ToxRun_CAS\"<br>" + "where CAS is the chemical abstract number<br>"
				+ "for the current chemical (i.e. 71-43-2) </html>");

		jpanelHelp.add(jlHelp);

		// chooser2.setAccessory(jpanelHelp);
		// chooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// chooser2.setDialogTitle("Select folder where all results files will
		// be located");

		// chooser1.setSelectedFile(new File("test4.mol"));

		// JPanel ap=new JPanel();
		//
		// ap.setLayout(null);
		//
		// JCheckBox jcbH=new JCheckBox("<html>Hydrogen depleted
		// molecule</html>");
		// jcbH.setSize(150,40);
		// jcbH.setLocation(20,20);
		// ap.add(jcbH);
		//
		// JCheckBox jcb2D=new JCheckBox("2D structure");
		// jcb2D.setSize(150,20);
		// jcb2D.setLocation(20,60);
		// ap.add(jcb2D);
		//
		// ap.setPreferredSize(new Dimension(150,150));
		// ap.setLocation(0,0);
		//
		// chooser1.setAccessory(ap);

	}



	/**
	 * Checks for aromatic bonds (and fixes them if found) and then makes the
	 * molecule registered with the JChemPaintEditorPanel
	 *
	 * @param molecule
	 * @return
	 */
	public IAtomContainer configureModel(IAtomContainer molecule) {


		TaskStructureSearch.fixNullBondStereo(molecule);
		
		ChemModel chemModel = new ChemModel();
		AtomContainerSet ms = new AtomContainerSet();
		ms.addAtomContainer(molecule);
		chemModel.setMoleculeSet(ms);

		this.model = chemModel;
		jChemPaintPanel.setChemModel(model);

		//		JChemPaintRendererModel m2=jChemPaintPanel.getRenderPanel().getRenderer().getRenderer2DModel();
		//		System.out.println(m2.getShowAromaticity());


		this.cleanUpStructure();

		// this.CheckFor3d();
		// this.GenerateSmiles(molecule);// not working properly- for some
		// reason if load benzene with aromatic bond orders =4 it doesnt work
		// right

		//		this.model.resetIsModified();

		return molecule;

	}



	public void getEndpointInfo() {
		endpoint = this.panelCalculationOptions.panelOptionsEndpointMethod.jcbEndpoint.getSelectedItem().toString();
		isBinaryEndpoint = TESTConstants.isBinary(endpoint);
		isLogMolarEndpoint =TESTConstants.isLogMolar(endpoint);
	}
	/**
	 * Triggers the cleanup event in jchempaint to fix the structure layout
	 */
	private void cleanUpStructure() {

		jmiCleanup.doClick();

		if (MakeHydrogensImplicit) {
			//			jmiMakeImplicit.doClick();	//need to make hydrogens implicit after cleaning up because this sometimes kills implicit hydrogens
		}

	}

	//	public void setNameCAS(IAtomContainer molecule) {
	//
	//
	//		String CASfield = ToxPredictor.misc.MolFileUtilities.getCASField(molecule);
	//
	//		String CAS = null;
	//
	//		if (!CASfield.equals("")) {
	//			CAS = (String) molecule.getProperty(CASfield);
	//			if (CAS != null) {
	//				CAS = CAS.trim();
	//				CAS = CAS.replace("/", "_");
	//			}
	//		}
	//
	//		if (CAS == null) {
	//
	//			String name = (String) molecule.getProperty("Name");
	//
	//			if (name == null) {
	//				name = (String) molecule.getProperty("name");
	//			}
	//			if (name == null) {
	//				// m.setProperty("Error", "<html>CAS and Name fields are both
	//				// empty</html>");
	//				CAS = "Unknown";
	//			} else {
	//				CAS = name;
	//			}
	//		}
	//
	//		if (CAS.equals("Unknown")) {
	////			String formula = CDKUtilities.GenerateFormula(molecule);
	//
	//			 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(molecule);
	//			 String formula=MolecularFormulaManipulator.getString(mf);
	//
	//			CAS = formula + "_" + System.currentTimeMillis();
	//		}
	//
	//		molecule.setProperty("CAS", CAS);
	//		
	//		panelSingleStructureDatabaseSearch.jtfCAS.setText(CAS);
	//		panelSingleStructureDatabaseSearch.jtfCAS.setCaretPosition(0);
	//
	//		// TODO decide how to deal with name- store in class variable?
	//
	//		// Hashtable htname=molecule.getProperties();
	//		// Iterator it=htname.keySet().iterator();
	//		//
	//		// String name="";
	//		//
	//		// while (it.hasNext()) {
	//		// String fieldname=(String)it.next();
	//		// String fieldname2=fieldname.toLowerCase();
	//		//
	//		// if (fieldname2.indexOf("name")>-1) {
	//		// name=(String)molecule.getProperty(fieldname);
	//		//// System.out.println(fieldname+"\t"+name);
	//		// break;
	//		// }
	//		//
	//		// }
	//
	//		// this.jtfChemicalName.setText(name);
	//		//
	//		// if (this.jtfChemicalName.getText().equals("")) {
	//		// this.jtfChemicalName.setText(filename.substring(0,filename.indexOf(".")));
	//		// }
	//
	//	}


	private void setupJChemPaintEditorPanel() {

		String title="";
		this.model.setID(title);

		ArrayList<String> blacklist = new ArrayList<String>();
		blacklist.add("file");
		//        blacklist.add("options");
		blacklist.add("menubar++");
		blacklist.add("rgroupMenu");
		blacklist.add("templates");
		blacklist.add("createInChI");

		//Need to set properties before creating the editor panel:
		JCPPropertyHandler.getInstance(true).getJCPProperties().setProperty("FitToScreen","false");
		JCPPropertyHandler.getInstance(true).getJCPProperties().setProperty("ShowAromaticity","false");
		JCPPropertyHandler.getInstance(true).getJCPProperties().setProperty("ShowExplicitHydrogens","false");
				
		
		//        blacklist.add("help");//for some reason this causes an error
		jChemPaintPanel = new JChemPaintPanel(model, JChemPaint.GUI_APPLICATION, false, null, blacklist);

		this.setUpToolBars();

//		jChemPaintPanel.getChemModel().setProperty("FitToScreen","false");
		
		//make it so it doesnt fit to screen:		
//		System.out.println("set fit to screen in test application");
		//		JCPPropertyHandler.getInstance(true).getJCPProperties().setProperty("ShowExplicitHydrogens","false");


		//		Enumeration props=JCPPropertyHandler.getInstance(true).getJCPProperties().propertyNames();
		//		while (props.hasMoreElements()) {
		//			System.out.println(props.nextElement());
		//		}


		//		System.out.println(this.jChemPaintPanel.getRenderPanel().getHub().getController2DModel().getAutoUpdateImplicitHydrogens());
		//showImplic
		//		String val=JCPPropertyHandler.getInstance(true).getJCPProperties().getProperty("FitToScreen");
		//		String arom=JCPPropertyHandler.getInstance(true).getJCPProperties().getProperty("ShowAromaticity");
		//		System.out.println(arom);
		//		System.out.println(this.jChemPaintPanel.getRenderPanel().getRenderer().getRenderer2DModel().getDrawNumbers());
		//		System.out.println(this.jChemPaintPanel.getRenderPanel().getRenderer().getRenderer2DModel().getShowImplicitHydrogens());

		// commented out by JH 08/15/2017 - replaced by code above to block out "View Menubar" and "Preferences..." menu items
		//        jChemPaintPanel = new JChemPaintPanel(model, JChemPaint.GUI_APPLICATION, false, null, new ArrayList<String>());
		//        jChemPaintPanel.updateStatusBar();

		//		jcpep.registerModel(model);
		//		jcpep.setJChemPaintModel(model,jcpep.getSize());
		//		model.getControllerModel().setAutoUpdateImplicitHydrogens(true);
		//		model.getRendererModel().setShowEndCarbons(true);

		//		jChemPaintPanel.setShowStatusBar(false);
		//		jChemPaintPanel.updateStatusBar();
		//jcpep.setShowInsertTextField(true);
		//		this.convertMenusToEnglish(jChemPaintPanel); //TODO- needed?
		//			jcpep.addChangeListener(cl);
		//			jcpep.setShowInsertTextField(false);
		//		jChemPaintPanel.getJMenuBar().setFloatable(false);

		//TODO- add Si, Sn, and As buttons

		//		JToolBar jtb=jChemPaintPanel.getJMenuBar();
		//		Object o=jtb.getComponent(jtb.getComponentCount()-1);//elementToolbar was added last
		//		JToolBar etb=(JToolBar)o;
		//
		//		//need a new box since would have too many elements in column
		//		javax.swing.Box box2= javax.swing.Box.createVerticalBox();
		//		etb.add(box2,3); //need to add it before the filler
		//
		//		JButton jb=createElementToolbarButton("Si", jChemPaintPanel, true);
		//		box2.add(jb);
		//
		//		jb=createElementToolbarButton("Sn", jChemPaintPanel, true);
		//		box2.add(jb);
		//
		//		jb=createElementToolbarButton("As", jChemPaintPanel, true);
		//		box2.add(jb);

	}

	public void setUpChoices() {

		//		int numEndpoints=1;
		//		if (includeToxicityEndpoints) numEndpoints+=7;
		//		if (includePhysicalPropertyEndpoints) numEndpoints+=9;

		endPointsToxicity=new Vector<String>();
		endPointsPhysicalProperty=new Vector<String>();

		Methods.add(TESTConstants.ChoiceConsensus);
		Methods.add(TESTConstants.ChoiceHierarchicalMethod);
		Methods.add(TESTConstants.ChoiceSingleModelMethod);
		Methods.add(TESTConstants.ChoiceGroupContributionMethod);
		if (includeFDA) Methods.add(TESTConstants.ChoiceFDAMethod);
		Methods.add(TESTConstants.ChoiceNearestNeighborMethod);


		if (includeToxicityEndpoints) {
			//*add endpoint* 03
			endPointsToxicity.add(TESTConstants.ChoiceFHM_LC50);
			endPointsToxicity.add(TESTConstants.ChoiceDM_LC50);
			endPointsToxicity.add(TESTConstants.ChoiceTP_IGC50);
			endPointsToxicity.add(TESTConstants.ChoiceRat_LD50);
			endPointsToxicity.add(TESTConstants.ChoiceBCF);
			endPointsToxicity.add(TESTConstants.ChoiceReproTox);
			endPointsToxicity.add(TESTConstants.ChoiceMutagenicity);
			
			if(includeER) {
				endPointsToxicity.add(TESTConstants.ChoiceEstrogenReceptor);
				endPointsToxicity.add(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity);
			}
			//*add endpoint* 04
			htAbbrevChoice.put(TESTConstants.ChoiceFHM_LC50, TESTConstants.abbrevChoiceFHM_LC50);
			htAbbrevChoice.put(TESTConstants.ChoiceDM_LC50, TESTConstants.abbrevChoiceDM_LC50);
			htAbbrevChoice.put(TESTConstants.ChoiceTP_IGC50,TESTConstants.abbrevChoiceTP_IGC50);
			htAbbrevChoice.put(TESTConstants.ChoiceRat_LD50,TESTConstants.abbrevChoiceRat_LD50);
			htAbbrevChoice.put(TESTConstants.ChoiceBCF,TESTConstants.abbrevChoiceBCF);
			htAbbrevChoice.put(TESTConstants.ChoiceReproTox,TESTConstants.abbrevChoiceReproTox);
			htAbbrevChoice.put(TESTConstants.ChoiceMutagenicity,TESTConstants.abbrevChoiceMutagenicity);
			
			if(includeER) {
				htAbbrevChoice.put(TESTConstants.ChoiceEstrogenReceptor,TESTConstants.abbrevChoiceER_Binary);
				htAbbrevChoice.put(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,TESTConstants.abbrevChoiceER_LogRBA);			
			}

		}

		if (includePhysicalPropertyEndpoints) {
			
			endPointsPhysicalProperty.add(TESTConstants.ChoiceBoilingPoint);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceVaporPressure);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceMeltingPoint);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceFlashPoint);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceDensity);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceSurfaceTension);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceThermalConductivity);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceViscosity);
			endPointsPhysicalProperty.add(TESTConstants.ChoiceWaterSolubility);

			htAbbrevChoice.put(TESTConstants.ChoiceBoilingPoint, TESTConstants.abbrevChoiceBoilingPoint);
			htAbbrevChoice.put(TESTConstants.ChoiceVaporPressure, TESTConstants.abbrevChoiceVaporPressure);
			htAbbrevChoice.put(TESTConstants.ChoiceMeltingPoint, TESTConstants.abbrevChoiceMeltingPoint);
			htAbbrevChoice.put(TESTConstants.ChoiceFlashPoint, TESTConstants.abbrevChoiceFlashPoint);
			htAbbrevChoice.put(TESTConstants.ChoiceDensity, TESTConstants.abbrevChoiceDensity);
			htAbbrevChoice.put(TESTConstants.ChoiceSurfaceTension, TESTConstants.abbrevChoiceSurfaceTension);
			htAbbrevChoice.put(TESTConstants.ChoiceThermalConductivity, TESTConstants.abbrevChoiceThermalConductivity);
			htAbbrevChoice.put(TESTConstants.ChoiceViscosity, TESTConstants.abbrevChoiceViscosity);
			htAbbrevChoice.put(TESTConstants.ChoiceWaterSolubility, TESTConstants.abbrevChoiceWaterSolubility);
		}

		//		EndPoints[i++]=ChoiceDescriptors;


	}

	private void setupSimpleControls(int inset) {

		int heightControls=20;
		int estMenuHeight=60;
		int loc2=this.getHeight()-inset-heightControls-estMenuHeight; // vertical location of second row of controls below JChemPaint panel

		jtfCalcStatus.setBounds(15, loc2, 490, 20);
		jtfCalcStatus.setVisible(false);

		jbCalculate.setSize(100, heightControls);
		jbCalculate.setLocation(getWidth()-jbCalculate.getWidth()-inset*2, loc2);
		jbCalculate.setText("Calculate!");
		jbCalculate.setBackground(Color.green);

		jbCalculateAA.setSize(150, heightControls);
		jbCalculateAA.setLocation(jbCalculate.getX()-jbCalculateAA.getWidth()-inset, loc2);
		jbCalculateAA.setText("Hazard Comparison");
		jbCalculateAA.setBackground(Color.green);

		jbSwitchToBatch.setSize(200, heightControls);
		if (includeAA_Dashboard) jbSwitchToBatch.setLocation(jbCalculateAA.getX()-jbSwitchToBatch.getWidth()-inset, loc2);
		else jbSwitchToBatch.setLocation(jbCalculate.getX()-jbSwitchToBatch.getWidth()-inset, loc2);
		jbSwitchToBatch.setText("Switch to Batch Mode");
		jbSwitchToBatch.setBackground(Color.cyan);

		jbSwitchToSingle.setSize(200, heightControls);
		jbSwitchToSingle.setLocation(jbSwitchToBatch.getLocation());
		jbSwitchToSingle.setText("Switch to Single Mode");
		jbSwitchToSingle.setBackground(Color.cyan);

		jbStop.setBounds(jbCalculate.getBounds());
		jbStop.setText("Stop");
		jbStop.setBackground(Color.red);
		jbStop.setVisible(false);

		this.add(this.jtfCalcStatus);
		if (includeAA_Dashboard) this.add(this.jbCalculateAA);
		this.add(this.jbCalculate);
		this.add(this.jbStop);
		add(jbSwitchToBatch);
		add(jbSwitchToSingle);

	}



	public void setupRecentFiles() {
	
		if (as==null) return;
		if (as.getRecentFilePaths()==null) return;
		if (as.getRecentFilePaths().size()==0) return;
	
		jmRecentMolFiles.removeAll();
	
		for (int i=0;i<as.getRecentFilePaths().size();i++) {
			String filepath=as.getRecentFilePaths().get(i);
			File file=new File(filepath);
	
	//		if (!file.exists()) {
	//			as.recentFilePaths.remove(i);
	//			continue;
	//		}
	
			String filename=file.getName();
			JMenuItem jmi = new JMenuItem(filename);
			jmi.setActionCommand("recentFile\t"+filepath);
			jmi.setToolTipText(filepath);
			jmi.addActionListener(aa);
			jmRecentMolFiles.add(jmi);
		}
	}

//	public void setupRecentBatchFiles() {
//
//
//
//		if (as==null) return;
//		if (as.getRecentBatchFilePaths()==null) return;
//		if (as.getRecentBatchFilePaths().size()==0) return;
//
//		jmRecentBatchResultsFiles.removeAll();
//
//		for (int i=0;i<as.getRecentBatchFilePaths().size();i++) {
//			String filepath=as.getRecentBatchFilePaths().get(i);
//			File file=new File(filepath);
//
//			//		if (!file.exists()) {
//			//			as.recentFilePaths.remove(i);
//			//			continue;
//			//		}
//
//			String filename=file.getName();
//			JMenuItem jmi = new JMenuItem(filename);
//			jmi.setActionCommand("recentBatchResultsFile\t"+filepath);
//			jmi.setToolTipText(filepath);
//			jmi.addActionListener(aa);
//			jmRecentBatchResultsFiles.add(jmi);
//
//		}
//
//	}

	static Vector<Component> getObjects(Component []comps,String name) {
		Vector<Component>components=new Vector<>();

		for (int i = 0; i < comps.length; i++) {
			if (comps[i].getClass().getName().equals(name)) {
				components.add(comps[i]);
			}
		}
		return components;

	}

	private void setUpToolBars() {
		Component[] comps = jChemPaintPanel.getComponents();
		int count=0;

		for (int i = 0; i < comps.length; i++) {
			if (comps[i].getClass().getName().equals("javax.swing.JPanel")) {


				JPanel mainContainer = (JPanel) comps[i];

				Component[] comps2 = mainContainer.getComponents();


				for (int j = 0; j < comps2.length; j++) {
					if (comps2[j].getClass().getName().equals("javax.swing.JToolBar")) {
						count++;
						//					System.out.println(i+"\t"+"Toolbar\t"+count);

						JToolBar jtb=(JToolBar)comps2[j];

						Component[]comps3=jtb.getComponents();

						for (int k=0;k<comps3.length;k++) {

							if (comps3[k].getClass().getName().equals("javax.swing.Box")) {
								Box box=(Box)comps3[k];

								Component[]comps4=box.getComponents();

								for (int l=0;l<comps4.length;l++) {

									if (comps4[l].getClass().getName().equals("org.openscience.jchempaint.JCPToolBar$1")) {

										String name=comps4[l].getName();

										//									System.out.println(name);

										if (name.equals("reactionArrow")) {
											comps4[l].setVisible(false);
										} else if (name.equals("new")) {										
											comps4[l].removeMouseListener(comps4[l].getMouseListeners()[0]);
											comps4[l].addMouseListener(ml);
											//										System.out.println(comps4[l]);
										} else if (name.equals("open")) {
											comps4[l].removeMouseListener(comps4[l].getMouseListeners()[0]);
											comps4[l].addMouseListener(ml);
											//										System.out.println(comps4[l]);
										}

									}

								}

							}

							//						JButton jbutton=(JButton)comps3[k];
							//						System.out.println(jbutton.getName());

						}
					}

				}

			}
		}
	}

	/**
	 * Removes extra Jchempaint toolbars for screen resolution
	 */
	private void removeExtraToolBars() {
		Component[] comps = jChemPaintPanel.getComponents();
		int count=0;

		
		
		for (int i = 0; i < comps.length; i++) {
			if (comps[i].getClass().getName().equals("javax.swing.JPanel")) {


				JPanel mainContainer = (JPanel) comps[i];

				Component[] comps2 = mainContainer.getComponents();


				for (int j = 0; j < comps2.length; j++) {
					if (comps2[j].getClass().getName().equals("javax.swing.JToolBar")) {
						count++;
						//					System.out.println(i+"\t"+"Toolbar\t"+count);

						JToolBar jtb=(JToolBar)comps2[j];

						Component[]comps3=jtb.getComponents();

						for (int k=0;k<comps3.length;k++) {

							if (comps3[k].getClass().getName().equals("javax.swing.Box")) {
								Box box=(Box)comps3[k];

								Component[]comps4=box.getComponents();

								for (int l=0;l<comps4.length;l++) {

									if (comps4[l].getClass().getName().equals("org.openscience.jchempaint.JCPToolBar$1")) {

										String name=comps4[l].getName();

										if (name.contentEquals("undo") || name.contentEquals("redo")
												|| name.contentEquals("rotate3d") || name.contentEquals("print")
												|| name.contentEquals("flipHorizontal") || name.contentEquals("flipVertical")) {
											box.remove(comps4[l]);
										}										
										
									}

								}

							}

						}
					}

				}

			}
		}
	}

	private void setupBatchImportSets(JMenu jm) {

		if (includeToxicityEndpoints) {
			JMenu jmBatchTrainingTestSets = new JMenu("Batch import of toxicity training/test sets");
			jm.add(jmBatchTrainingTestSets);

			for (int i = 0; i < endPointsToxicity.size(); i++) {
				String endpoint = endPointsToxicity.get(i);
				String abbrev = htAbbrevChoice.get(endpoint);

				JMenuItem jmiTrainingSet = new JMenuItem("Training set for " + endpoint);
				jmiTrainingSet.setActionCommand("loadSet\t" + abbrev + " training set");
				jmiTrainingSet.addActionListener(aa);
				jmBatchTrainingTestSets.add(jmiTrainingSet);

				JMenuItem jmiTestSet = new JMenuItem("Test set for " + endpoint);
				jmiTestSet.setActionCommand("loadSet\t" + abbrev + " test set");
				jmiTestSet.addActionListener(aa);
				jmBatchTrainingTestSets.add(jmiTestSet);

				if (i < endPointsToxicity.size() - 1)
					jmBatchTrainingTestSets.add(new JPopupMenu.Separator());

			}

			if (includeLDA) {

				String endpoint = TESTConstants.ChoiceFHM_LC50;
				String abbrev = htAbbrevChoice.get(endpoint);

				jmBatchTrainingTestSets.add(new JPopupMenu.Separator());

				JMenuItem jmiTrainingSet = new JMenuItem("Training set for " + endpoint + " (MOA based models)");
				jmiTrainingSet.setActionCommand("loadSet\t" + abbrev + " training set (MOA based models)");
				jmiTrainingSet.addActionListener(aa);
				jmBatchTrainingTestSets.add(jmiTrainingSet);

				JMenuItem jmiTestSet = new JMenuItem("Test set for " + endpoint + " (MOA based models)");
				jmiTestSet.setActionCommand("loadSet\t" + abbrev + " test set (MOA based models)");
				jmiTestSet.addActionListener(aa);
				jmBatchTrainingTestSets.add(jmiTestSet);

			}

		}

		if (includePhysicalPropertyEndpoints && includePhysicalPropertyDataSets) {
			JMenu jmBatchTrainingTestSets = new JMenu("Batch import of physical property training/test sets");
			jm.add(jmBatchTrainingTestSets);

			for (int i = 0; i < endPointsPhysicalProperty.size(); i++) {
				String endpoint = endPointsPhysicalProperty.get(i);
				String abbrev = htAbbrevChoice.get(endpoint);

				JMenuItem jmiTrainingSet = new JMenuItem("Training set for " + endpoint);
				jmiTrainingSet.setActionCommand("loadSet\t" + abbrev + " training set");
				jmiTrainingSet.addActionListener(aa);
				jmBatchTrainingTestSets.add(jmiTrainingSet);

				JMenuItem jmiTestSet = new JMenuItem("Test set for " + endpoint);
				jmiTestSet.setActionCommand("loadSet\t" + abbrev + " test set");
				jmiTestSet.addActionListener(aa);
				jmBatchTrainingTestSets.add(jmiTestSet);

				if (i < endPointsPhysicalProperty.size() - 1)
					jmBatchTrainingTestSets.add(new JPopupMenu.Separator());

			}

		}

//		jm.add(new JPopupMenu.Separator());

	}


	private void setUpTimers() {

		// this.jProgressBar1.setValue(0);
		// this.jProgressBar1.setStringPainted(true);

		timerCalculations = new javax.swing.Timer(1, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				setCursor(Utilities.waitCursor);
				panelResults.setCursor(Utilities.waitCursor);
				
				// TODO: add code to disable buttons here
				jbCalculate.setVisible(false);
				jbStop.setVisible(true);
				jtfCalcStatus.setVisible(true);

				jtfCalcStatus.setText(task.getMessage());

				String s = task.getMessage();

				int c = task.getCurrent();

				// TODO: visually update screen with progress
				// jProgressBar1.setValue(c);
				// jProgressBar1.setString(c+"%: "+s);

				// System.out.println("Free
				// Memory"+Runtime.getRuntime().freeMemory()/1e6);

				if (task.isDone()) {

					timerCalculations.stop();

					c = task.getCurrent();
					// jProgressBar1.setValue(c);

					// TODO: renable buttons/menus here

					jbCalculate.setVisible(true);
					jbStop.setVisible(false);
					jtfCalcStatus.setVisible(false);
					
					setCursor(Utilities.defaultCursor);
					panelResults.setCursor(Utilities.defaultCursor);
					panelResults.setDefaultCursorAllTables();
					
//					panelResults.tableHCD.setCursor(Utilities.defaultCursor);//Java bug makes this extra line necessary
//					System.out.println(panelResults.tableHCD.getCursor().toString());

				}

			}
		});


		timerBatchStructureFile = new javax.swing.Timer(1, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				setCursor(Utilities.waitCursor);
				
				// TODO: add code to disable buttons here
				jbCalculate.setVisible(false);
				jbStop.setVisible(true);
				jtfCalcStatus.setVisible(true);

				jtfCalcStatus.setText(taskStructureFile.getMessage());

				String s = taskStructureFile.getMessage();

				int c = taskStructureFile.getCurrent();

				// TODO: visually update screen with progress
				// jProgressBar1.setValue(c);
				// jProgressBar1.setString(c+"%: "+s);

				if (taskStructureFile.isDone()) {

					timerBatchStructureFile.stop();

					c = taskStructureFile.getCurrent();

					jbCalculate.setVisible(true);
					jbStop.setVisible(false);
					jtfCalcStatus.setVisible(false);

					setCursor(Utilities.defaultCursor);

				}

			}
		});

	}

	void testJChemPaint() {
		String title="Todd";
		JFrame f = new JFrame(title);
		IChemModel chemModel = JChemPaint.emptyModel();

		chemModel.setID(title);
		f.addWindowListener(new JChemPaintPanel.AppCloser());
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JChemPaintPanel p = new JChemPaintPanel(chemModel, JChemPaint.GUI_APPLICATION, false, null, new ArrayList<String>());


		//        p.updateStatusBar();
		f.setPreferredSize(new Dimension(800, 494));    //1.618
		f.setJMenuBar(p.getJMenuBar());
		f.add(p);
		f.pack();
		Point point = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getCenterPoint();
		int w2 = (f.getWidth() / 2);
		int h2 = (f.getHeight() / 2);
		f.setLocation(point.x - w2, point.y - h2);
		f.setVisible(true);
	}


	private void setupMenus() {
		//TODO- may need to hide view implicit hydrogens since it seems to make the carbons show up instead of hydrogens in JChemPaint 1.5!
		//TODO - hide atom menu and add charge +1, -1 toolbuttons like we used to have
		this.setJMenuBar(menuBar);
		this.jmiCleanup=getCleanup(jChemPaintPanel);
		setUpBatchMenu();
		setUpSingleMenu();
		setUpHelpMenu();

	}

	private void setUpSingleMenu() {
		this.menuBar.add(jmFileSingle);

		
		if (includeRecentMolFileMenu) {
			jmRecentMolFiles = new JMenu("Recent structures analyzed");
			jmFileSingle.add(jmRecentMolFiles);
			jmFileSingle.addSeparator();
			setupRecentFiles();
		}

		JMenuItem jmiBatchSearch = new JMenuItem("Switch to batch mode");
		jmiBatchSearch.setActionCommand("jmiBatchSearch");
		jmiBatchSearch.addActionListener(aa);
		jmFileSingle.add(jmiBatchSearch);

	}

	private void setUpHelpMenu() {
		JMenu jmHelp = new JMenu("Help");
		this.menuBar.add(jmHelp);

		JMenuItem jmiHelpUserGuide = new JMenuItem("User's Guide"); // applet-omit-06
		jmiHelpUserGuide.setActionCommand("jmiHelpUserGuide"); // applet-omit-06
		jmiHelpUserGuide.addActionListener(aa); // applet-omit-06
		jmHelp.add(jmiHelpUserGuide); // applet-omit-06

		JMenuItem jmiHelpDescriptorsGuide = new JMenuItem("Molecular Descriptors Guide"); // applet-omit-06
		jmiHelpDescriptorsGuide.setActionCommand("jmiHelpDescriptorsGuide"); // applet-omit-06
		jmiHelpDescriptorsGuide.addActionListener(aa); // applet-omit-06
		jmHelp.add(jmiHelpDescriptorsGuide); // applet-omit-06

		jmHelp.add(new JPopupMenu.Separator()); // applet-omit-06

		JMenuItem jmiHelpAboutToxPredictor = new JMenuItem("About T.E.S.T.");
		jmiHelpAboutToxPredictor.setActionCommand("jmiHelpAboutToxPredictor");
		jmiHelpAboutToxPredictor.addActionListener(aa);
		jmHelp.add(jmiHelpAboutToxPredictor);
	}

	private void setUpBatchMenu() {
		this.menuBar.add(jmFileBatch);
		
		JMenuItem jmiSingleChemicalMode = new JMenuItem("Switch to single chemical mode");
		jmiSingleChemicalMode.setActionCommand("jmiSingleChemicalMode");
		jmiSingleChemicalMode.addActionListener(aa);
		jmFileBatch.add(jmiSingleChemicalMode);

		jmFileBatch.add(new JPopupMenu.Separator());

		JMenuItem jmiBatchImportFromSDF = new JMenuItem("Batch import from MDL .mol/.sdf file");
		jmiBatchImportFromSDF.setActionCommand("jmiBatchImportFromSDF");
		jmiBatchImportFromSDF.addActionListener(aa);
		jmFileBatch.add(jmiBatchImportFromSDF);

		JMenuItem jmiBatchImportFromCAS = new JMenuItem("Batch import from text file containing CAS numbers");
		jmiBatchImportFromCAS.setActionCommand("jmiBatchImportFromCAS");
		jmiBatchImportFromCAS.addActionListener(aa);
		jmFileBatch.add(jmiBatchImportFromCAS);

		JMenuItem jmiBatchImportFromSmiles = new JMenuItem("Batch import from text file containing SMILES strings");
		jmiBatchImportFromSmiles.setActionCommand("jmiBatchImportFromSmiles");
		jmiBatchImportFromSmiles.addActionListener(aa);
		jmFileBatch.add(jmiBatchImportFromSmiles);
		setupBatchImportSets(jmFileBatch);


//		JMenuItem jmiSaveToSDF = new JMenuItem("Save batch list to .sdf file");
//		jmiSaveToSDF.setActionCommand("jmiSaveToSDF");
//		jmiSaveToSDF.addActionListener(aa);
//		jmFileBatch.add(jmiSaveToSDF);
//
//		JMenuItem jmiSaveToTXT = new JMenuItem("Save batch list to .csv file");
//		jmiSaveToTXT.setActionCommand("jmiSaveToTXT");
//		jmiSaveToTXT.addActionListener(aa);
//		jmFileBatch.add(jmiSaveToTXT);
//		
//		JMenuItem jmiSaveToExcel = new JMenuItem("Save batch list to .xlsx file");
//		jmiSaveToExcel.setActionCommand("jmiSaveToExcel");
//		jmiSaveToExcel.addActionListener(aa);
//		jmFileBatch.add(jmiSaveToExcel);


//		jmRecentBatchResultsFiles = new JMenu("Recent batch results files");
//		jmFileBatch.add(jmRecentBatchResultsFiles);

		//		Font myFontBold = new java.awt.Font("Arial", Font.BOLD, 11);
		//		jmiSingleChemicalMode.setFont(myFontBold);
//		setupRecentBatchFiles();

	}

	public static JMenuItem getCleanup(JChemPaintPanel jChemPaintPanel) {
		JMenu jmJChemPaintHelp;
		JMenu jmTools = null;
		JMenuBar jmb=jChemPaintPanel.getJMenuBar();

		for (int i=0;i<jmb.getMenuCount();i++) {
			if (jmb.getMenu(i)!=null) {

				JMenu jm=jmb.getMenu(i);
				String name=jm.getName();

				//				System.out.println(name);

				if (name.equals("help")) {
					//					jmb.remove(jm);
					jmJChemPaintHelp = jm;
					jm.setText("Drawing Help");//TODO- do we want to use any of these to help user draw molecules?
					//				} else if (name.equals("file") || name.equals("") || name.equals("")) {//use blacklist now
					//					jmb.remove(jm);//TODO- maybe preserve some of these?
				} else if (jmb.getMenu(i).getName().equals("tools")) {
					jmTools=jm;
				}
			}
		}


		for (int i=0;i<jmTools.getMenuComponentCount();i++) {
			if (jmTools.getMenuComponent(i)!=null) {
				JComponent jm=(JComponent) jmTools.getMenuComponent(i);
				if (jm.getName().equals("cleanup2")) {
					return (JMenuItem)jm;
				}
			}
		}
		
		return null;
	}

	void loadBatchForDebug() {

		//		String filePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2018 ecoTTC MOA paper\\ecoTTC.smi";
		//		String filePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\000 Papers\\2018 ecoTTC MOA paper\\rerun to get moa set cas number.smi";
		//		taskStructureFile.init(filePath,StructureFileImportTask7.TypeSmiles,this);

		//		String filePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\web-test_2019_05_03\\AADashboard calcs\\NCCT structures\\sample.sdf";
		//		taskStructureFile.init(filePath,StructureFileImportTask7.TypeSDF,this);

//		String filePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\web-test_2019_05_03\\data\\bad smiles no id.smi";
//		taskStructureFile.init(filePath,TaskStructureSearch.TypeSmiles,TESTConstants.typeTaskBatch,this);
		
		String filePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\AA Dashboard\\Output\\chemicals with most records\\cas list.txt";
		taskStructureFile.init(filePath,TaskStructureSearch.TypeCAS,TESTConstants.typeTaskBatch,this);


		taskStructureFile.go();
		timerBatchStructureFile.start();

	}

	

	public static void main(String[] args) {

		if (forMDH) {
			TESTConstants.SoftwareVersion+=" MDH";
		}
		
		DialogSplash fs = new DialogSplash();
		fs.setVisible(true);
		//
		TESTApplication f = new TESTApplication();
		fs.setVisible(false);
		fs.dispose();
		f.setVisible(true);
				
		ResolverDb2.assureDbIsOpen();
		
		File file=new File("webtest.log");
		
		try {
			file.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
//		System.out.println("forMDH="+forMDH);
		
		
		if (forMDH) {
			f.aa.switchToBatchMode();
//			System.out.println("here");
		} else {
			if (defaultToSingleChemicalMode) {
				f.aa.switchToSingleChemicalMode();
//				f.loadChemicalForDebug("COCOCOCOCOCOCOCOCC");
//				f.loadChemicalForDebug("DTXSID901297914");
				
//				f.taskStructureFile.runSingle("N1C2=NC(NC(N)=C2N=C2C=1c1ccccc1-c1ccccc12)=S",f);
//				f.taskStructureFile.runSingle("c1cc2C3=NC4=NC(NC(N)=C4N=C3c3c(-c2cc1)cccc3)=S",f);
//				f.taskStructureFile.runSingle("NC1NC(=S)N=C2N=C3c4ccccc4-c4ccccc4C3=NC=12",f);
//				f.taskStructureFile.runSingle("S=C1NC(N)=C2N=C3C=4C(C=5C(C3=NC2=N1)=CC=CC5)=CC=CC4",f);
				

				
//				f.loadChemicalForDebug("80-05-7");
//				f.loadChemicalForDebug("115-86-6");
//				f.loadChemicalForDebug("perchloroethane");
//				f.loadChemicalForDebug("61-94-9");
//				f.loadChemicalForDebug("N,N'-Dimethyl-N,N'-bis(3-(3',4',5'-trimethoxybenzoxy)propyl)ethylenediamine dihydrochloride");
//				f.loadChemicalForDebug("triphenyl phosphate");
//				f.panelCalculationOptions.panelCTSOptions.jcbRunCTS.setSelected(true);
				
			} else {
				f.aa.switchToBatchMode();

//				f.panelBatchStructureDatabaseSearch.jcbOptions.setSelectedItem(PanelStructureDatabaseSearchBatch.strOptionSmiles);
//				f.panelBatchStructureDatabaseSearch.jrbName.doClick();
//				f.panelBatchStructureDatabaseSearch.jtfIdentifiers.setText("c1ccccc1\n"
//						+ "CCCCOCC\nqwerty\nCCCOCCCCCCCCCCCOCC\nCCCOCCCCOCCCCCOCCOCOC");
				
//				f.panelBatchStructureDatabaseSearch.jtfIdentifiers.setText("xylenes");
//				f.panelBatchStructureDatabaseSearch.jtfIdentifiers.setText("COCOCOCCCCCOCCCCCOCCCOCCCCOCC");
				
//				f.panelBatchStructureDatabaseSearch.jbSearch.doClick();
				
				
//				f.loadBatchForDebug();
//				f.loadBatchForDebugFromString();
			}

		}
		
	
		
		HueckelAromaticityDetector.debug=false;
				
		f.repaint();
	}
	
	

	
	private void loadChemicalForDebug(String search) {
		panelSingleStructureDatabaseSearch.jtfIdentifier.setText(search);
		panelSingleStructureDatabaseSearch.jbSearch.doClick();
	}

	private void loadBatchForDebugFromString() {
		
		String list="71-43-2\n91-20-3\n129-00-0\n";
		taskStructureFile.init(list,TaskStructureSearch.TypeCAS,TESTConstants.typeTaskBatch,this);
		taskStructureFile.go();
		timerBatchStructureFile.start();

		
	}

}
