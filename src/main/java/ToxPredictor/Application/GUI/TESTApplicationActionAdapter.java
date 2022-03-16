package ToxPredictor.Application.GUI;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.vecmath.Point2d;

import org.apache.logging.log4j.util.Strings;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import AADashboard.Application.TableGeneratorExcel;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.CTS_Generate_Breakdown_Products;
import ToxPredictor.Application.Calculations.TaskCalculations2;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.PanelBatchChemicals.MyTableModel;
import ToxPredictor.Database.DSSToxRecord;
//import ToxPredictor.Database.ResolverDb;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.misc.MolFileUtilities;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;



/**
 * Handles all action events by accessing the action command that triggered it
 */
public	class TESTApplicationActionAdapter implements java.awt.event.ActionListener {


	TESTApplication f;


	public TESTApplicationActionAdapter(TESTApplication f) {
		this.f=f;
	}

	public void actionPerformed(ActionEvent e) {

		if (f.jbStop.isVisible() && !e.getActionCommand().equals("jbStop") && e.getActionCommand().indexOf("jmiHelp")==-1) {
			JOptionPane.showMessageDialog(null,"Click Stop first");
			return;
		}

		if (e.getActionCommand().equals("jbCalculate")) {
			jbCalculate_actionPerformed();
		} else if (e.getActionCommand().equals("jbCalculateAA")) {
			jbCalculateAA_actionPerformed();
		} else if (e.getActionCommand().equals("jbStop")) {
			jbStop_actionPerformed(e);
		} else if (e.getActionCommand().equals("jmiImportFromTextFile")) {
			jmiImportFromTextFile_actionPerformed(e);
//		} else if (e.getActionCommand().equals("jbOptions")) {
//			f.myfraOptions.setVisible(true);
		} else if (e.getActionCommand().equals("jmiHelpUserGuide")) {
			jmiHelpUserGuide_actionPerformed(e);
		} else if (e.getActionCommand().equals("jmiHelpDescriptorsGuide")) {
			jmiHelpDescriptorsGuide_actionPerformed(e);
		} else if (e.getActionCommand().equals("jmiHelpAboutToxPredictor")) {
			f.aboutDialog.setVisible(true);
//		} else if (e.getActionCommand().equals("jmiCreateBatchList")) {
//			jmiCreateBatchList_actionPerformed();
		} else if (e.getActionCommand().equals("jmiSingleChemicalMode") || e.getActionCommand().equals("jbSwitchToSingle")) {
			switchToSingleChemicalMode();
		} else if (e.getActionCommand().equals("jmiBatchSearch") || e.getActionCommand().equals("jbSwitchToBatch")) {
			switchToBatchMode();
		} else if (e.getActionCommand().equals("jmiBatchImportFromSDF")) {
			jmiBatchImportFromSDF_actionPerformed();
		} else if (e.getActionCommand().equals("jmiBatchImportFromCAS")) {
			jmiBatchImportFromCAS_actionPerformed();
		} else if (e.getActionCommand().equals("jmiBatchImportFromSmiles")) {
			jmiBatchImportFromSmiles_actionPerformed();
		} else if (e.getActionCommand().equals("jmiSaveToSDF")) {
			jmiSaveToSDF_actionPerformed();
		} else if (e.getActionCommand().equals("jmiSaveToTXT")) {
			jmiSaveToTXT_actionPerformed();
		} else if (e.getActionCommand().equals("jmiSaveToExcel")) {
			jmiSaveToExcel_actionPerformed();

		} else if (e.getActionCommand().indexOf("loadSet\t")==0) {
			loadTrainingTestSet(e.getActionCommand());
		
		} else if (e.getActionCommand().indexOf("recentFile\t")==0) {
			
			String com=e.getActionCommand();
			
//			System.out.println(com);
			
			String str="recentFile\t";
			String filepath=com.substring(str.length(),com.length());
			File file=new File(filepath);
			AtomContainer molecule=f.taskStructureFile.loadFromMolFile(file, f);
			f.configureModel(molecule);
			f.taskStructureFile.setCAS(molecule);
			f.panelSingleStructureDatabaseSearch.jtfCAS.setText(molecule.getProperty("CAS"));

			
			
			
		} else if (e.getActionCommand().indexOf("recentBatchResultsFile\t")==0) {
			String com=e.getActionCommand();
			String str="recentBatchResultsFile\t";
			String filepath=com.substring(str.length(),com.length());
			File htmlFile=new File(filepath);
			f.as.addBatchFilePath(filepath);
			f.as.saveSettingsToFile();
//			f.setupRecentBatchFiles();

			try {
				URL myURL;
				myURL=htmlFile.toURI().toURL();

				String strURL=myURL.toString();

				//	Fix for files on EPA's network drive:
				strURL=strURL.replace("file:////", "file://///");

//				BrowserLauncher launcher = new BrowserLauncher(null);
//				launcher.openURLinBrowser(strURL);//doesnt seem to work for applet!
				
				MyBrowserLauncher.launch(htmlFile.toURI());

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}


	private void addSmilesRan(AtomContainerSet acs) {
		
		try {
			SmilesGenerator sg =SmilesGenerator.unique();
			
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				String smiles = sg.create(ac);
				ac.setProperty("SmilesRan", smiles);
			}
						
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
	}
 
	private void jmiSaveToExcel_actionPerformed() {
		AtomContainerSet acs=f.panelBatch.getMolecules();
		
		addSmilesRan(acs);
		
		if (acs.getAtomContainerCount()==0) {
			JOptionPane.showMessageDialog(f, "No molecules to save");
			return;
		}

		int returnVal = f.chooserExcel.showSaveDialog(f);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserExcel.getSelectedFile();

		String folder = inFile.getParentFile().getAbsolutePath();
		String name = inFile.getName();

		if (name.indexOf(".") == -1) {
			name += ".xlsx";
			inFile = new File(folder + File.separator + name);
		}

		if (inFile.exists()) {
			int retVal = JOptionPane.showConfirmDialog(f,
					"File exists, do you wish to overwrite?",
					"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
			if (retVal != JOptionPane.OK_OPTION) {
				return;
			}
		}
		
        if (TaskCalculations2.isExcelOpen(inFile.getAbsolutePath())) {
        	JOptionPane.showMessageDialog(f, "Please close "+inFile.getAbsolutePath());        	
        	return;
        }		        	


		try {

			Vector<String> newProps = getOutputProps(acs);
			XSSFWorkbook workbook = new XSSFWorkbook();
			
			TableGeneratorExcel tg=new TableGeneratorExcel();
			tg.writeBatchChemicalsToExcel(acs,newProps,workbook);

			FileOutputStream out = new FileOutputStream(new File(inFile.getAbsolutePath()));
            workbook.write(out);
            out.close();

        	launchFile(inFile);
			            

		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
					e.printStackTrace();
		}
		
	}

	private void launchFile(File inFile)
			throws MalformedURLException, BrowserLaunchingInitializingException, UnsupportedOperatingSystemException {
//		URL myURL= inFile.toURI().toURL();	
//		String strURL = myURL.toString();

		// Fix for files on EPA's network drive:
//		strURL = strURL.replace("file:////", "file://///");

		JOptionPane.showMessageDialog(f,
		"The file was saved successfully at\n"+ inFile.getAbsolutePath());

//		BrowserLauncher launcher = new BrowserLauncher(null);
//		launcher.openURLinBrowser(strURL);// doesnt seem to work for
		
		MyBrowserLauncher.launch(inFile.toURI());
		
	}

	private void jmiSaveToTXT_actionPerformed() {
		String del=",";
		
		// TODO Auto-generated method stub
		AtomContainerSet acs=f.panelBatch.getMolecules();
		
		addSmilesRan(acs);

		
		if (acs.getAtomContainerCount()==0) {
			JOptionPane.showMessageDialog(f, "No molecules to save");
			return;
		}

		int returnVal = f.chooserCSV.showSaveDialog(f);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserCSV.getSelectedFile();

		String folder = inFile.getParentFile().getAbsolutePath();
		String name = inFile.getName();

		if (name.indexOf(".") == -1) {
			name += ".csv";
			inFile = new File(folder + File.separator + name);
		}

		if (inFile.exists()) {
			int retVal = JOptionPane.showConfirmDialog(f,
					"File exists, do you wish to overwrite?",
					"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
			if (retVal != JOptionPane.OK_OPTION) {
				return;
			}
		}

		try {

			FileWriter fw=new FileWriter(inFile);
			
			Vector<String> newProps = getOutputProps(acs);
			
						
			for (int i=0;i<newProps.size();i++) {
				fw.write("\""+newProps.get(i)+"\"");
				if (i<newProps.size()-1) fw.write(del);
			}
			fw.write("\r\n");
			
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				
				for (int j=0;j<newProps.size();j++) {
					if (ac.getProperty(newProps.get(j))!=null) {
						fw.write("\""+ac.getProperty(newProps.get(j))+"\"");
					}
					if (j<newProps.size()-1) fw.write(del);
				}
				
				fw.write("\r\n");
			}
			
			fw.close();

//			JOptionPane.showMessageDialog(f,
//					"The file was saved successfully at\n"+ inFile.getAbsolutePath());
			
			launchFile(inFile);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
					e.printStackTrace();
		}

	}

	private Vector<String> getOutputProps(AtomContainerSet acs) {
		Vector<String>props=new Vector<>();
					
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			Set<Object>keys=ac.getProperties().keySet();
			
			for(Object key:keys) {
				String strKey=(String)key;
				
				if (strKey.contains("org.openscience.cdk")) continue;
				
				if (!props.contains(strKey)) props.add(strKey);
			}
		}
		
		Vector<String>newProps=new Vector<>();
		
		newProps.add("Index");
		newProps.add("CAS");
		newProps.add(DSSToxRecord.strName);
		newProps.add("Formula");
		newProps.add("Error");
		newProps.add("ErrorCode");
		

		for (int j=0;j<DSSToxRecord.fieldNames.length;j++) {
			if (!newProps.contains(DSSToxRecord.fieldNames[j])) newProps.add(DSSToxRecord.fieldNames[j]);
		}
		
		for (int i=0;i<props.size();i++) {
			if (!newProps.contains(props.get(i))) newProps.add(props.get(i));
		}
		
		if (newProps.contains("cdk:Title")) newProps.remove("cdk:Title");
		
		return newProps;
	}




	void saveApplicationSettings() {

		if (f.as==null) {
			f.as=new ApplicationSettings();
		}
		
		f.as.setEndpoint(f.panelCalculationOptions.panelOptionsEndpointMethod.jcbEndpoint.getSelectedItem()+"");
		f.as.setMethod(f.panelCalculationOptions.panelOptionsEndpointMethod.jcbMethod.getSelectedItem()+"");
		
//		f.as.setCreateReport(f.panelCalculationOptions.panelOutputOptions.jcbCreateReports.isSelected());
		f.as.setCreateDetailedReport(f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports.isSelected());
		f.as.setRelaxFragmentConstraint(f.panelCalculationOptions.panelOptionsEndpointMethod.jcbRelaxFragmentConstraint.isSelected());
		
		f.as.setOutputFolderPath(f.panelCalculationOptions.panelOutputOptions.jtfOutputFolder.getText());
	
		f.as.saveSettingsToFile();
	}

	/**
	 * Method which handles when the Calculate button is clicked.
	 * If the batch table is visible the batch calculation is performed.
	 * If the batch table is invisible the single chemical calculation is performed.
	 * 
	 */
	private void jbCalculate_actionPerformed() {

		if (Strings.isEmpty(f.as.getOutputFolderPath())) {
//			f.panelCalculationOptions.jbBrowse.doClick();
			JOptionPane.showMessageDialog(f, "Select output folder first");
			return;
		}

		saveApplicationSettings();

		if (f.panelBatch.isVisible()) {
			batchCalculate(TESTConstants.typeRunEndpoint);			
		} else {
			calculate(TESTConstants.typeRunEndpoint);			
		}

	}

	private void jbCalculateAA_actionPerformed() {


		if (Strings.isEmpty(f.as.getOutputFolderPath())) {
//			f.panelCalculationOptions.jbBrowse.doClick();
			JOptionPane.showMessageDialog(f, "Select output folder first");
			return;
		}

		saveApplicationSettings();

		f.panelResults.jbViewReport.setVisible(false);
		
		if (f.panelBatch.isVisible()) {
			batchCalculate(TESTConstants.typeRunAA);
		} else {
			calculate(TESTConstants.typeRunAA);
		}

	}


	/**
	 * Method that handles when jbCloseBatch button is clicked 
	 * This method switches to single chemical mode
	 */
	void switchToSingleChemicalMode() {
		
		
		f.panelDraw.setVisible(true);
		f.panelSingleStructureDatabaseSearch.setVisible(true);
		f.jmFileSingle.setVisible(true);
		f.jbSwitchToBatch.setVisible(true);
		
//		f.panelCalculationOptions.panelOutputOptions.jbViewPanelResults.setVisible(false);
//		f.panelCalculationOptions.panelOutputOptions.jcbCreateReports.setVisible(false);
		f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports.setVisible(true);
		f.panelCalculationOptions.panelOutputOptions.jbAboutCreateReports.setVisible(true);
		
//		f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports.setLocation(f.panelCalculationOptions.panelOutputOptions.jcbCreateReports.getLocation());
//		f.panelResults.jbSaveToHTML.setVisible(false);
		f.panelResults.jbViewReport.setVisible(true);

		f.panelBatch.setVisible(false);
		f.panelBatchStructureDatabaseSearch.setVisible(false);
		f.jmFileBatch.setVisible(false);
		f.jbSwitchToSingle.setVisible(false);
		
		if (f.includeCTS) {
			f.panelCalculationOptions.panelCTSOptions.setVisible(true);
		}
		f.panelSingleStructureDatabaseSearch.jtfIdentifier.requestFocus();
	}

	void switchToBatchMode() {
//		System.out.println("Enter switch to batch");
		f.panelDraw.setVisible(false);
		f.panelSingleStructureDatabaseSearch.setVisible(false);
		f.jmFileSingle.setVisible(false);
		f.jbSwitchToBatch.setVisible(false);
		
		
//		f.panelCalculationOptions.panelOutputOptions.jbViewPanelResults.setVisible(true);
		
//		f.panelCalculationOptions.panelOutputOptions.jcbCreateReports.setVisible(true);	
		f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports.setVisible(false);
		f.panelCalculationOptions.panelOutputOptions.jbAboutCreateReports.setVisible(false);
		
		f.panelBatch.setVisible(true);
		f.panelBatchStructureDatabaseSearch.setVisible(true);
		f.jmFileBatch.setVisible(true);
		f.jbSwitchToSingle.setVisible(true);
		
		if (f.includeCTS) {
			f.panelCalculationOptions.panelCTSOptions.setVisible(false);
		}
		f.panelBatchStructureDatabaseSearch.jtfIdentifiers.requestFocus();
//		f.panelResults.jbSaveToHTML.setVisible(true);
		f.panelResults.jbViewReport.setVisible(false);
		
	}


	

	/**
	 * This method handles when the user selects jmiBatchImportFromCAS 
	 * from the File menu.<br>
	 * This method loads a list of chemicals from a text file that 
	 * has a list of CAS numbers in it.<br>
	 * This is done via a task since it may take some time to load the molecules.
	 */
	private void jmiBatchImportFromCAS_actionPerformed() {

		int returnVal = f.chooserCAS.showOpenDialog(f);


		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserCAS.getSelectedFile();

		String name = inFile.getName();		
		String folder = inFile.getParentFile().getAbsolutePath();

		if (name.indexOf(".") == -1) {
			name += ".txt";
			inFile = new File(folder + File.separator + name);
		}

		if (!inFile.exists()) {
			JOptionPane.showMessageDialog(f, "File does not exist");
			return;
		}

		int type=TaskStructureSearch.TypeCAS;

		f.taskStructureFile.init(inFile.getAbsolutePath(),type,TESTConstants.typeTaskBatch,f);
		f.taskStructureFile.go();
		f.timerBatchStructureFile.start();

	}


	/**
	 * This method handles when the user selects jmiBatchImportFromSmiles from
	 * the File menu.  
	 * This method loads a list of chemicals from a smiles textfile.  
	 * Ideally each line of the text file has a smiles string, a delimiter such
	 * as a tab, and finally an identifier such as a CAS number.  
	 * This is done via a task since it may take some time to load the molecules.
	 */
	private void jmiBatchImportFromSmiles_actionPerformed() {


		int returnVal = f.chooserSmiles.showOpenDialog(f);		

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserSmiles.getSelectedFile();

		if (!inFile.exists()) {
			JOptionPane.showMessageDialog(f, "File does not exist");
			return;
		}

		int type=TaskStructureSearch.TypeSmiles;

		f.taskStructureFile.init(inFile.getAbsolutePath(),type,TESTConstants.typeTaskBatch,f);
		f.taskStructureFile.go();
		f.timerBatchStructureFile.start();


	}

//	/**
//	 * This method handles when the user selects jmiGenerateFromSmilesOnClipboard
//	 * from the File menu.<br>
//	 * This method uses the generateFromSmiles method to generate a molecule
//	 * from the smiles string on the clipboard.
//	 */
//	private void jmiGenerateFromSmilesOnClipboard_actionPerformed() {
//
//		String smiles="";
//		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//		Transferable contents = clipboard.getContents(null);
//		boolean hasTransferableText =
//				(contents != null) &&
//				contents.isDataFlavorSupported(DataFlavor.stringFlavor);
//		if ( hasTransferableText ) {
//			try {
//				smiles = (String)contents.getTransferData(DataFlavor.stringFlavor);
//				//        System.out.println(smiles);
//				//OCC(Cl)CCCO
//			}
//			catch (Exception ex) {	        
//				ex.printStackTrace();
//			}
//		} else {
//			JOptionPane.showMessageDialog(f, "Invalid object on clipboard");
//			return;
//		}
//
//		if (smiles==null || smiles.length() == 0) {
//			JOptionPane.showMessageDialog(f, "No Smiles entered");
//			return;
//		} 
//
//		generateFromSmiles(smiles);
//
//
//	}







	private void loadChemicalFromSmiles(String smiles, String name) {

		if (smiles == null) {
			return;
		}

		if (smiles.length() == 0) {
			JOptionPane.showMessageDialog(f, "No Smiles entered");
			return;
		}


		try {
			String Smiles = smiles.trim();
			Smiles = CDKUtilities.FixSmiles(Smiles);

			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			AtomContainer molecule = (AtomContainer)sp.parseSmiles(Smiles);

			if (!(molecule instanceof AtomContainer)) {
				JOptionPane.showMessageDialog(f, "Invalid smiles");
				return;
			}

			f.df.Normalize(molecule);

			f.configureModel(molecule);
			f.panelSingleStructureDatabaseSearch.jtfCAS.setText(name);

			boolean aromaticOK = ToxPredictor.misc.MolFileUtilities.doesAromaticCountMatch(molecule, Smiles);

			if (!aromaticOK) {
				JOptionPane.showMessageDialog(f, "Error perceiving aromaticity of smiles", "Error",
						JOptionPane.WARNING_MESSAGE);
			}

		} catch (InvalidSmilesException ise) {
			JOptionPane.showMessageDialog(f, "Invalid SMILES or parse error: " + ise.getMessage());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(f, ex);
		}
	}
	
//	/**
//	 * Generates a molecule from smiles string and then checks to see if the
//	 * molecule is in the structure database. If it is, the CAS is added to the
//	 * Molecule ID field.
//	 */
//	void generateFromSmiles(String smiles) {
//		try {
//
//			if (smiles.indexOf(".") > -1) {
//				JOptionPane.showMessageDialog(f,
//						"Please enter a single structure without a \".\" in the SMILES string");
//				return;
//			}
//
//			f.setCursor(Utilities.waitCursor);
//
//			String Smiles = smiles.trim();
//			//		Smiles = CDKUtilities.FixSmiles(Smiles);//TODO
//
//			AtomContainer molecule=null;
//
//			molecule=WebTEST4.loadSMILES(Smiles);
//
//			//		System.out.println(molecule==null);
//
//			if (molecule.getProperty("ErrorCode")==WebTEST.ERROR_CODE_STRUCTURE_ERROR) {
//				JOptionPane.showMessageDialog(f,
//						"Invalid SMILES = \"" + smiles + "\"");
//				f.setCursor(Utilities.defaultCursor);
//				return;
//			}
//
//
//
//			StructureDiagramGenerator sdg=new StructureDiagramGenerator();
//			sdg.setMolecule(molecule);
//			sdg.generateCoordinates();
//			molecule = (AtomContainer) sdg.getMolecule();
//
//			//		f.df.Normalize(molecule);
//
//			f.configureModel(molecule);
//
//			String CAS = molecule.getProperty("CAS");
//			f.panelSingleStructureDatabaseSearch.jtfCAS.setText(CAS);
//			//		JOptionPane.showMessageDialog(f, "Match found in database, Molecule ID: " + CAS);
//			f.setCursor(Utilities.defaultCursor);
//
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			JOptionPane.showMessageDialog(f, ex);
//		}
//
//	}


		/**
	 * This method handles when the user selects jmiSaveAs 
	 * from the File menu.<br>
	 * This method saves the current chemical to a MDL molfile
	 */
	private void jmiSaveSmilesToClipboard_actionPerformed() {
		IChemModel cm = null;
		IAtomContainerSet som = null;

		try {

			//		cm = model.getChemModel();
			som = f.model.getMoleculeSet();

			if (!(som instanceof AtomContainerSet)) {
				JOptionPane.showMessageDialog(f, "Enter a structure");
				return;
			}

			if (som.getAtomContainerCount() > 1) {
				JOptionPane.showMessageDialog(f,
						"Error: number of molecules = " + som.getAtomContainerCount()
						+ ",\nplease enter only one molecule");
				return;
			} else if (som.getAtomContainerCount() == 0) {
				JOptionPane.showMessageDialog(f, "Enter a structure first");
				return;
			}

			IAtomContainer m = som.getAtomContainer(0);


			//clone so dont mess up by removing hydrogens
			IAtomContainer m2=(IAtomContainer)m.clone();

			SmilesGenerator sg =SmilesGenerator.unique();
			String smiles = sg.create(m2);

			if (smiles!=null) {
				//copy to clipboard:
				StringSelection stringSelection = new StringSelection( smiles);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents( stringSelection, null);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	/**
	 * This method handles when the user selects jmiHelpDescriptorsGuide 
	 * from the Help menu. The method loads "TEST User's Guide.pdf"
	 * using the default reader.
	 * 
	 * @param e
	 */
	private void jmiHelpUserGuide_actionPerformed(ActionEvent e) {

		try {

			// fraMain5
			File file=new File("TEST User's Guide.pdf"); //located in main folder (not in a jar file)
			URL myURL=file.toURI().toURL();	

			String strURL=myURL.toString();

			//Fix for files on EPA's network drive:
			strURL=strURL.replace("file:////", "file://///");

			//		BrowserLauncher.openURL(myURL.toString()); 
//			BrowserLauncher launcher = new BrowserLauncher(null);
//			launcher.openURLinBrowser(strURL);
			
			MyBrowserLauncher.launch(myURL.toURI());
			//		ToxApplet5 - store pdf file in SystemData jar file
			//		URL myURL=this.getClass().getClassLoader().getResource("ToxPredictor User Guide.pdf");
			//		getAppletContext().showDocument(myURL,"_blank"); 


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

//	/**
//	 * This method handles when the user selects jmiImportFromDatabase 
//	 * from the File menu.<br>
//	 * This method loads myfraStructureDatabaseSearch so the user can search
//	 * the structure database.
//	 */
//	private void jmiImportFromDatabase_actionPerformed(ActionEvent e) {		
//
//		//	try {
//		//		this.myfraStructureDatabaseSearch.SetupStructure(jcpep.createImage(100,100));
//		//	} catch (Exception ex) {
//		//		System.out.println(ex);
//		//	}
//
//		f.myfraStructureDatabaseSearch.setVisible(true);
//
//
//		if (f.myfraStructureDatabaseSearch.jrbCAS.isSelected()) {
//			f.myfraStructureDatabaseSearch.jtfCAS.requestFocus();
//		} else if (f.myfraStructureDatabaseSearch.jrbMW.isSelected()) {
//			f.myfraStructureDatabaseSearch.jtfMW.requestFocus();
//		} else if (f.myfraStructureDatabaseSearch.jrbFormula.isSelected()) {
//			f.myfraStructureDatabaseSearch.jtfFormula.requestFocus();
//		}
//
//	}

	/**
	 * This method handles when the user selects jmiHelpDescriptorsGuide 
	 * from the Help menu. The method loads "MolecularDescriptorsGuide.pdf"
	 * using the default reader.
	 * 
	 * @param e
	 */
	private void jmiHelpDescriptorsGuide_actionPerformed(ActionEvent e) {

		try {

			// fraMain5
			File file=new File("MolecularDescriptorsGuide.pdf"); //located in main folder (not in a jar file)
			URL myURL=file.toURI().toURL();

			//		URL myURL=this.getClass().getClassLoader().getResource("ToxPredictor User Guide.pdf");
			//		BrowserLauncher.openURL(myURL.toString()); 

//			BrowserLauncher launcher = new BrowserLauncher(null);
//			launcher.openURLinBrowser(myURL.toString());
			
			MyBrowserLauncher.launch(myURL.toURI());

			//		ToxApplet5 - store pdf file in SystemData jar file
			//		URL myURL=this.getClass().getClassLoader().getResource("ToxPredictor User Guide.pdf");
			//		getAppletContext().showDocument(myURL,"_blank"); 


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}




	/**
	 * This method handles when the user selects jmiImportFromTextFile 
	 * from the File menu.<br>
	 * This method loads a chemical from a MDL molfile.
	 */
	void jmiImportFromTextFile_actionPerformed(ActionEvent e) {

		f.requestFocus();//get focus away from jtfCAS if it has it


		// TODO: Ask user if molecule one coordinate all zeros- ask if molecule
		// is 2d or 3d
		// need to determine if is planar molecule or if user entered 2d mol
		// file
		// if is planar- add code to put in 3d coordinates

		//	JChemPaintEditorPanel jcpPanel = this.jcpep;

		// following was commented out- moved to init routine so it doesnt keep
		// adding things to list of choices:
		// JCPFileFilter.addChoosableFileFilters(this.chooser1);

		f.chooser1.setCurrentDirectory(f.jChemPaintPanel.getCurrentWorkDirectory());

		if (f.jChemPaintPanel.getLastOpenedFile() != null) {
			f.chooser1.setSelectedFile(f.jChemPaintPanel.getLastOpenedFile());
		}

		// TODO: add code to allow user to decide which molecule in SDF file
		// to run or allow batch mode

		int returnVal = f.chooser1.showOpenDialog(f.jChemPaintPanel);
		String type = null;
		IChemObjectReader cor = null;

		f.currentFilter = f.chooser1.getFileFilter();

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		f.jChemPaintPanel.setCurrentWorkDirectory(f.chooser1.getCurrentDirectory());
		f.jChemPaintPanel.setCurrentOpenFileFilter(f.chooser1.getFileFilter());

		File inFile = f.chooser1.getSelectedFile();

		if (inFile.getName().indexOf(".")==-1) {
			inFile=new File(inFile.getAbsolutePath()+".mol");
		}

		f.jChemPaintPanel.setLastOpenedFile(inFile);

		f.setCursor(Utilities.waitCursor);
		
		AtomContainer ac=f.taskStructureFile.loadFromMolFile(inFile,f);
		
					
		
		if (ac!=null) {
			
			TaskStructureSearch.setCAS(ac);			
			ac=f.configureModel(ac);
			
			f.panelSingleStructureDatabaseSearch.jtfCAS.setText(ac.getProperty("CAS"));
			
			if (ac.getProperty(DSSToxRecord.strName)!=null) f.panelSingleStructureDatabaseSearch.jtfName.setText(ac.getProperty(DSSToxRecord.strName));
			else f.panelSingleStructureDatabaseSearch.jtfName.setText("");
			
		}
		
		f.setCursor(Utilities.defaultCursor);

	}


	/**
	 * This method handles when the user selects jmiSaveAs 
	 * from the File menu.<br>
	 * This method saves the current chemical to a MDL molfile
	 */
	private void jmiSaveAs_actionPerformed() {
		//	IChemModel cm = null;
		AtomContainerSet som = null;

		try {

			//		cm = model.getChemModel();
			som = (AtomContainerSet)f.model.getMoleculeSet();

			if (!(som instanceof AtomContainerSet)) {
				JOptionPane.showMessageDialog(f, "Enter a structure");
				return;
			}

			if (f.panelSingleStructureDatabaseSearch.jtfCAS.getText().equals("")) {
				JOptionPane
				.showMessageDialog(
						f,
						"Enter a CAS# (if one is not available enter a dummy value).\n\nThe CAS number is used to store the results for each chemical\nin its own folder inside the main output folder.",
						"CAS number missing",
						JOptionPane.WARNING_MESSAGE);
				f.panelSingleStructureDatabaseSearch.jtfCAS.requestFocus();
				return;
			}

			if (som.getAtomContainerCount() > 1) {
				JOptionPane.showMessageDialog(f,
						"Error: number of molecules = " + som.getAtomContainerCount()
						+ ",\nplease enter only one molecule");
				return;
			} else if (som.getAtomContainerCount() == 0) {
				JOptionPane.showMessageDialog(f, "Enter a structure first");
				return;
			}


			cleanUpStructure();

			f.requestFocus();// get focus away from jtfCAS if it has it

			File file=new File(f.panelSingleStructureDatabaseSearch.jtfCAS.getText()+".mol");
			f.chooser1.setSelectedFile(file);

			int returnVal = f.chooser1.showSaveDialog(f);

			if (returnVal != JFileChooser.APPROVE_OPTION)
				return;

			File inFile = f.chooser1.getSelectedFile();

			String folder = inFile.getParentFile().getAbsolutePath();
			String name = inFile.getName();

			if (name.indexOf(".") == -1) {
				name += ".mol";
				inFile = new File(folder + File.separator + name);
			}

			if (inFile.exists()) {
				int retVal = JOptionPane.showConfirmDialog(f,
						"File exists, do you wish to overwrite?",
						"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
				if (retVal != JOptionPane.OK_OPTION) {
					return;
				}
			}

			AtomContainer m = (AtomContainer)som.getAtomContainer(0);
			String CAS = f.panelSingleStructureDatabaseSearch.jtfCAS.getText();

			ToxPredictor.misc.MolFileUtilities.WriteMolFile(m, CAS, inFile
					.getAbsolutePath(), true);

			JOptionPane.showMessageDialog(f,
					"The MDL mol file was saved successfully at\n"
							+ inFile.getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	/**
	 * This method handles when the user selects jmiBatchImportFromSDF 
	 * from the File menu.<br>
	 * This method loads a list of chemicals from a MDL sdfile.<br>
	 * 	 * This is done via a task since it may take some time to load the molecules.
	 */
	private void jmiBatchImportFromSDF_actionPerformed() {


		int returnVal = f.chooserSDF.showOpenDialog(f);


		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserSDF.getSelectedFile();

		if (inFile.getName().indexOf(".")==-1) {
			inFile=new File(inFile.getAbsolutePath()+".sdf");
		}


		if (!inFile.exists()) {
			JOptionPane.showMessageDialog(f, "File does not exist");
			return;
		}

		int type=TaskStructureSearch.TypeSDF;

		f.taskStructureFile.init(inFile.getAbsolutePath(),type,TESTConstants.typeTaskBatch,f);
		f.taskStructureFile.go();
		f.timerBatchStructureFile.start();

	}

	/**
	 * This method handles when the user selects jmiGenerateFromSmiles 
	 * from the File menu.<br>
	 * This method prompts the user to enter a smiles string then calls
	 * generateFromSmiles to generate a molecule from the string
	 */
//	private void jmiGenerateFromSmiles_actionPerformed() {
//
//
//		String smiles=JOptionPane.showInputDialog(f,"Enter a SMILES string",f.currentSmiles);
//
//		if (smiles==null) {
//			return;
//		} 
//
//		f.currentSmiles=smiles;
//
//
//
//		if (smiles.length() == 0) {			 					
//			JOptionPane.showMessageDialog(f, "No Smiles entered");
//			return;
//		}
//
//		generateFromSmiles(smiles);
//
//
//	}

	/**
	 * This method handles when jbSaveSDF is clicked. It saves the chemicals in 
	 * the batch table in a MDL sd file
	 */
	private void jmiSaveToSDF_actionPerformed() {

		AtomContainerSet acs=f.panelBatch.getMolecules();
		
		addSmilesRan(acs);

		
		if (acs.getAtomContainerCount()==0) {
			JOptionPane.showMessageDialog(f, "No molecules to save");
			return;
		}

		int returnVal = f.chooserSDF.showSaveDialog(f);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File inFile = f.chooserSDF.getSelectedFile();

		String folder = inFile.getParentFile().getAbsolutePath();
		String name = inFile.getName();

		if (name.indexOf(".") == -1) {
			name += ".sdf";
			inFile = new File(folder + File.separator + name);
		}

		if (inFile.exists()) {
			int retVal = JOptionPane.showConfirmDialog(f,
					"File exists, do you wish to overwrite?",
					"File exists, overwrite?", JOptionPane.OK_CANCEL_OPTION);
			if (retVal != JOptionPane.OK_OPTION) {
				return;
			}
		}

		try {

			FileWriter fw=new FileWriter(inFile);

			MDLV2000Writer mw=new MDLV2000Writer(fw);

			mw.setWriteAromaticBondTypes(false);

			Vector<String> newProps = getOutputProps(acs);

			for (int i=0;i<acs.getAtomContainerCount();i++) {
				AtomContainer m=(AtomContainer)acs.getAtomContainer(i);
				mw.write(m);
								
				for (int j=0;j<newProps.size();j++) {
					writeProperty(fw, m,newProps.get(j));
				}
				
				fw.write("$$$$\n");
			}
			
			fw.close();
			
			launchFile(inFile);

//			JOptionPane.showMessageDialog(f,
//					"The SDF file was saved successfully at\n"
//							+ inFile.getAbsolutePath());


		} catch (Exception e) {
			JOptionPane.showMessageDialog(f, e);
			//		e.printStackTrace();
		}


	}



	private void writeProperty(FileWriter fw, AtomContainer m,String property) throws IOException {
		if (m.getProperty(property)!=null) {
			fw.write("> <"+property+">\n"+m.getProperty(property)+"\n\n");
		}
	}

	/**
	 * Stops all currently running tasks
	 * @param e
	 */
	private void jbStop_actionPerformed(ActionEvent e) {

		f.taskStructureFile.stop();
		//	WebTEST.stop();
		f.task.stop();
		//	this.taskBatch.stop();

	}


	boolean isMoleculeOK(AtomContainerSet som) {

		if (!(som instanceof IAtomContainerSet) || som.getAtomContainerCount()==0) {
			JOptionPane.showMessageDialog(f, "Enter a structure");
			return false;
		}

		//			 loop though Set of Molecules to delete molecules with no atoms:
		for (int i = 0; i <= som.getAtomContainerCount() - 1; i++) {
			IAtomContainer ac = som.getAtomContainer(i);
			if (ac.getAtomCount() == 0) {
				// delete molecules with no atoms:
				som.removeAtomContainer(i--);
			} 
		}
	
		
		if (!(som instanceof IAtomContainerSet) || som.getAtomContainerCount()==0) {
			JOptionPane.showMessageDialog(f, "Enter a structure");
			return false;
		}
		
		
		if (som.getAtomContainerCount()>1) {
			JOptionPane.showMessageDialog(f,
				"Error: structure contains more than one molecule");
			return false;
		}

		AtomContainer ac=(AtomContainer)som.getAtomContainer(0);
		
		if (ac==null) {
			JOptionPane.showMessageDialog(f, "Enter a structure first");
			return false;
		}
				
		WebTEST4.checkAtomContainer(ac);
		String error=ac.getProperty("Error");
		
		if (error.contentEquals("Multiple molecules")) {
			JOptionPane.showMessageDialog(f,
				"Error: structure contains more than one molecule");
			return false;

		} else if (error.contentEquals("Molecule contains unsupported element")) {
			JOptionPane.showMessageDialog(f,
					"Current molecule contains an element which cannot be predicted " +
							"using this software.\nValid elements include " +
							"carbon, hydrogen, oxygen, nitrogen, fluorine, chlorine, \n" +
							"bromine, iodine, sulfur, phosphorus, silicon, arsenic, " +
					"mercury, and tin.");
			return false;

		} else if (error.contentEquals("Molecule does not contain carbon")) {
			JOptionPane.showMessageDialog(f,
					"Error: cannot make predictions for molecules that do not contain carbon");
			return false;
		} else if (error.contentEquals("Only one nonhydrogen atom")) {
			JOptionPane.showMessageDialog(f,
					"Error: cannot make predictions for molecules\nwhich contain only one non hydrogen atom.");
			return false;
		} else if (error.contentEquals("Number of atoms equals zero") || som.getAtomContainerCount() == 0) {
			JOptionPane.showMessageDialog(f, "Enter a structure first");
			return false;
		}
		

		//TODO can following still happen?
		IAtomContainer myMolecule = som.getAtomContainer(0);
		ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities(); 
		if (!(myMolecule.getAtom(0).getPoint2d() instanceof Point2d)) {
			JOptionPane.showMessageDialog(f,
					"Please enter 2d coordinates for the molecule");
			return false;
		}
		
		myMolecule.setProperty("Error", "");

		return true;

	}


	
	/**
	 * First part of calculations, makes sure that we have a valid molecule
	 * to calculate and that the output path is set
	 */

	public void calculate(int type) {

		AtomContainerSet som =(AtomContainerSet)f.model.getMoleculeSet();
		try {
			som =(AtomContainerSet)f.model.getMoleculeSet().clone();//for some reason if I dont clone it, it causes error when trying to draw after checking this!
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		if (!isMoleculeOK(som)) return;

		AtomContainer myMolecule = (AtomContainer)som.getAtomContainer(0);
				
		//TODO ask user when we dont have a match and not blank
		
		ResolverDb2.assignRecordByStructureViaInchis(myMolecule, f.panelSingleStructureDatabaseSearch.jtfCAS.getText());
				
		f.panelSingleStructureDatabaseSearch.jtfCAS.setText(myMolecule.getProperty(DSSToxRecord.strCAS));
		
		if (myMolecule.getProperty(DSSToxRecord.strName)!=null) {
			f.panelSingleStructureDatabaseSearch.jtfName.setText(myMolecule.getProperty(DSSToxRecord.strName));
		} else {
			f.panelSingleStructureDatabaseSearch.jtfName.setText("");
		}

		

		AtomContainerSet ms=new AtomContainerSet();
		ms.addAtomContainer(myMolecule);

		File OutputFolder=new File(f.as.getOutputFolderPath());
		boolean useFragmentsConstraint=!f.panelCalculationOptions.panelOptionsEndpointMethod.jcbRelaxFragmentConstraint.isSelected();

		
		boolean runCTS=false;
		String libraryCTS=null;
				
		if (f.includeCTS) {
			runCTS=f.panelCalculationOptions.panelCTSOptions.jcbRunCTS.isSelected();
			
			if (runCTS) {				
				
				String route=(String)f.panelCalculationOptions.panelCTSOptions.jcbRoute.getSelectedItem();				
				if (route.contentEquals("Hydrolysis")) libraryCTS=CTS_Generate_Breakdown_Products.strLibraryHydrolysis;
				else if (route.contentEquals("Abiotic Reduction")) libraryCTS=CTS_Generate_Breakdown_Products.strLibraryAbioticReduction;
				else if (route.contentEquals("Human Metabolism")) libraryCTS=CTS_Generate_Breakdown_Products.strLibraryHumanBioTransformation;				
			}
		}
		
		
		if (type==TESTConstants.typeRunEndpoint) {
			f.method=f.panelCalculationOptions.panelOptionsEndpointMethod.jcbMethod.getSelectedItem().toString();
			//	method=this.ChoiceHierarchicalMethod;
			f.getEndpointInfo();
//			boolean writeResultsToFile=true;

			boolean createReports=true;//always create reports in single mode
			boolean generateWebpages=true;//always write webpages to hard drive in single mode
			boolean createDetailedReports=f.panelCalculationOptions.panelOutputOptions.jcbCreateDetailedReports.isSelected();
						
			if (f.endpoint.contentEquals(TESTConstants.abbrevChoiceDescriptors)) createDetailedReports=true;
			
			if (!f.endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
				f.panelResults.setTitle("Prediction results: "+f.endpoint);
				f.panelResults.initTableModel(f.endpoint, f.method,false);		
			} else {
				f.panelResults.setTitle("Descriptor values");
//				System.out.println("Init table model descriptors");
				f.panelResults.initTableModelDescriptors();
			}
			
//			if (!createReports) {
//				f.panelResults.jbSaveToHTML.setVisible(false);
//			} else {
//				f.panelResults.jbSaveToHTML.setVisible(true);
//			}
			
			f.panelCalculationOptions.panelOutputOptions.jbViewPanelResults.setEnabled(true);			
			f.panelResults.setVisible(true);
			
			
			f.task.init(ms, useFragmentsConstraint,createReports,createDetailedReports,generateWebpages, OutputFolder, f, f.endpoint, f.method, 
					TESTConstants.typeTaskSingle,TESTConstants.typeRunEndpoint, runCTS, libraryCTS);
						
			f.task.go();
			f.timerCalculations.start(); 
			
		} else {
			
			f.panelResults.setTitle("Hazard comparison");
			f.panelResults.initTableModelHCD();
//			f.panelResults.jbSaveToHTML.setVisible(false);

			f.panelCalculationOptions.panelOutputOptions.jbViewPanelResults.setEnabled(true);			
			f.panelResults.setVisible(true);				
			AtomContainerSet acs=new AtomContainerSet();
			acs.addAtomContainer(myMolecule);
			f.task.initForAA(acs, useFragmentsConstraint, OutputFolder, f, TESTConstants.typeTaskBatch, TESTConstants.typeRunAA,runCTS,libraryCTS);
			f.task.go();
			f.timerCalculations.start();
			
			
//			f.task.initForAA(ms, useFragmentsConstraint, OutputFolder, f, TESTConstants.typeTaskSingle, TESTConstants.typeRunAA,runCTS);
//			f.task.go();
//			f.timerCalculations.start();
		}
	}


	/**
	 * Triggers the cleanup event in jchempaint to fix the structure layout
	 */
	private void cleanUpStructure() {

		f.jmiCleanup.doClick();

		if (f.MakeHydrogensImplicit) {
			f.jmiMakeImplicit.doClick(); // need to make hydrogens implicit after
			// cleaning up because this sometimes
			// kills implicit hydrogens
		}

	}


	private void loadTrainingTestSet(String set) {
		//Convert name of action command to path of sdf file:
		String str="loadSet\t";

		//	System.out.println(set);

		String endpointabbrev="";

		if (set.equals("loadSet	LC50 training set (MOA based models)")) {
			endpointabbrev="LC50";
			set="LC50 training set.sdf";
		} else if (set.equals("loadSet	LC50 test set (MOA based models)")) {
			endpointabbrev="LC50";
			set="LC50 prediction set.sdf";
		} else {
			set=set.substring(str.length(),set.length());
			endpointabbrev=set.substring(0,set.indexOf(" "));
			set=set.replace(" set", "");
			set=set.replace("test", "prediction");//need to change test to prediction since that's how it is stored in jar file
			set=set.replace(" ", "_");
			set+=".sdf";
			set=endpointabbrev+"/"+set;
		}


		int type=TaskStructureSearch.TypeSDF_In_Jar;


		//the following sets the selected endpoint based on the data set that has been imported:
		for (java.util.Enumeration e = f.htAbbrevChoice.keys();e.hasMoreElements();) {
			String var = (String) e.nextElement(); //endpoint name
			String strval = (String) f.htAbbrevChoice.get(var); //endpoint abbreviation
			if (strval.equals(endpointabbrev)) {
				f.panelCalculationOptions.panelOptionsEndpointMethod.jcbEndpoint.setSelectedItem(var);
				break;
			}
		}

		//********************************************************************************
		//Import from SDF in jar file
		f.taskStructureFile.init(set,type,TESTConstants.typeTaskBatch,f);		
		f.taskStructureFile.go();
		f.timerBatchStructureFile.start();

		//********************************************************************************
		//Import for structures on the dashboard via CAS numbers in the sdf files in the jar file:		
//		System.out.println(set);		
//		f.taskStructureFile.init(set,type,TESTConstants.typeTaskBatch,f);
//		AtomContainerSet acs=f.taskStructureFile.LoadFromSDFInJar();
//		String casList="";
//		for (int i=0;i<acs.getAtomContainerCount();i++) {
//			casList+=acs.getAtomContainer(i).getProperty("CAS")+"\n";
//		}
//		System.out.println(casList);
//		
//		f.taskStructureFile.init(casList,TaskStructureSearch.TypeCAS,TESTConstants.typeTaskBatch,f);
//		f.taskStructureFile.go();
//		f.timerBatchStructureFile.start();
		
	}


	/**
	 * First part of batch calculations, makes sure that we have some molecules
	 * to calculate and that the output path is set
	 */
	public void batchCalculate(int type) {

		try {
			MyTableModel model=(MyTableModel)f.panelBatch.table.getModel();
			AtomContainerSet acs=model.getAtomContainerSet();

			if (acs ==null) {
				JOptionPane.showMessageDialog(f, "No structures were found");
				return;
			}

//			int goodcount = getGoodCount(acs);
//
//			if (goodcount==0) {
//				JOptionPane.showMessageDialog(f, "No valid structures found, calculations cannot proceed");
//				return;
//			}
			
			if (acs.getAtomContainerCount()==0) {
				JOptionPane.showMessageDialog(f, "No structures selected in batch list");
				return;
			}


			f.method=f.panelCalculationOptions.panelOptionsEndpointMethod.jcbMethod.getSelectedItem().toString();
			//	method=this.ChoiceHierarchicalMethod;

			f.getEndpointInfo();
			File OutputFolder=new File(f.as.getOutputFolderPath());

			boolean useFragmentsConstraint=!f.panelCalculationOptions.panelOptionsEndpointMethod.jcbRelaxFragmentConstraint.isSelected();

//			boolean runCTS=f.panelCTSOptions.jcbRunCTS.isSelected();
			boolean runCTS=false;//disable for batch mode

			if (type==TESTConstants.typeRunEndpoint) {
				
				boolean createReports=false;

				if (!f.endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
					f.panelResults.setTitle("Prediction results: "+f.endpoint);
					f.panelResults.initTableModel(f.endpoint, f.method,true);		
				} else {
					f.panelResults.setTitle("Descriptor values");
					f.panelResults.initTableModelDescriptors();
				}
				
//				if (!createReports) {
//					f.panelResults.jbSaveToHTML.setVisible(false);
//				} else {
//					f.panelResults.jbSaveToHTML.setVisible(true);
//				}
				
				f.panelCalculationOptions.panelOutputOptions.jbViewPanelResults.setEnabled(true);				
				f.panelResults.setVisible(true);
							
				boolean createDetailedReports=false;
				boolean generateWebpages=false;
								
				f.task.init(acs, useFragmentsConstraint, createReports,createDetailedReports,generateWebpages, 
						OutputFolder, f, f.endpoint, f.method, TESTConstants.typeTaskBatch,TESTConstants.typeRunEndpoint, runCTS, null);				
				
				f.task.go();
				f.timerCalculations.start();
			} else {
				
				f.panelResults.setTitle("Hazard comparison");
				f.panelResults.initTableModelHCD();
//				f.panelResults.jbSaveToHTML.setVisible(false);

				f.panelCalculationOptions.panelOutputOptions.jbViewPanelResults.setEnabled(true);
				f.panelResults.setVisible(true);	
				
				f.task.initForAA(acs, useFragmentsConstraint, OutputFolder, f, TESTConstants.typeTaskBatch, TESTConstants.typeRunAA,runCTS,null);
				f.task.go();
				f.timerCalculations.start();
			}
			
//			f.panelResults.jbSaveToHTML.setEnabled(f.panelCalculationOptions.panelOutputOptions.jcbCreateReports.isSelected());
			

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(f, ex);
			ex.printStackTrace();
			return;
		}
	}



	private int getGoodCount(AtomContainerSet acs) {
		int goodcount=0;

		for (int i=0;i<acs.getAtomContainerCount();i++) {
			IAtomContainer m=acs.getAtomContainer(i);
			String error=(String)m.getProperty("Error");
			
//			System.out.println("getGoodCount:"+error);
			
			if (error.equals("")) {
				goodcount++;
			} else {
				//				moleculeSet.removeAtomContainer(i);
				//				i--;
			}
		}
		return goodcount;
	}

	
}
