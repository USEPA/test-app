package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.smiles.SmilesGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.hpl.jena.util.FileUtils;


import AADashboard.Application.TableGeneratorExcel;
import AADashboard.Application.TableGeneratorHTML;
import AADashboard.Application.AADashboard;
import QSAR.validation2.InstanceUtilities;
import edu.stanford.ejalbert.BrowserLauncher;//new one
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
//import gov.epa.ghs_data_gathering.Parse.ToxVal.ParseToxValDB;
import ToxPredictor.Utilities.SaveStructureToFile;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.misc.Lookup;
import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.GUI.Miscellaneous.SwingWorker;
//import ToxPredictor.Application.Calculations.CTS_Generate_Breakdown_Products.ChildChemical;
import ToxPredictor.Application.Calculations.ResultsCTS.Data.Tree.Child;
import ToxPredictor.Application.GUI.MyBrowserLauncher;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;

/**
 * This class replaces DescriptorCalculationTask7 and
 * DescriptorCalculationTask7Batch- it was too difficult to maintain these two
 * classes since have to make changes in too many locations every time you make
 * a change to the calculations
 * 
 * @author TMARTI02
 *
 */
public class TaskCalculations2 {

		
	private static final Logger logger = LogManager.getLogger(TaskCalculations2.class);
	
	SwingWorker worker;

	// task variables:
	private int lengthOfTask;

	private int current = 0; // variable used to update length of progress bar
								// (100 = done)

	private boolean done = false;

	private String statMessage; // message that tells what has been done so far
								// or currently
	IAtomContainerSet moleculeSet;

//	boolean WriteResultsToFile;

//	boolean FindFragments;
//	boolean useFragmentsConstraint;

	long minFileAgeHours = 1000000000;

	File fileOutputFolder;

	DescriptorFactory df = new DescriptorFactory(false);

	InstanceUtilities iu = new InstanceUtilities();

	String batchFileName;

	CalculationParameters params;
	
	public static File commandLineOutputFile=null;
	
//	String endpoint;
//	boolean isBinaryEndpoint;
//	boolean isLogMolarEndpoint;
//	String method;
//	String abbrev;

//	boolean Use3D;
//	boolean is3Dpresent;
	Object gui = null;
//	String DescriptorSet;

	Lookup lookup = new Lookup();
	String del = "|";// delimiter for text file

	static double SCmin = 0.5;// min for consideration as similar in tables

	int taskType = -1;//single chemical or batch
	int runType = -1;//AA dashboard or endpoint calculation
	
//	String runNumber = "";

	boolean debug=false;
	
	AADashboard aad=null;

	private boolean runCTS=false;

	private String libraryCTS;
	
	
//	boolean createExcelReport=false;
//	String reportType;
	
	
	public static ArrayList<String> getMethods(String endpoint) {
		
		ArrayList<String> methods = new ArrayList<String>();
		methods.add(TESTConstants.ChoiceHierarchicalMethod);

		if (TESTConstants.haveSingleModelMethod(endpoint)) {
			methods.add(TESTConstants.ChoiceSingleModelMethod);
		}
		// Group contribution:
		if (TESTConstants.haveGroupContributionMethod(endpoint)) {
			methods.add(TESTConstants.ChoiceGroupContributionMethod);
		}
		// methods.add(TESTConstants.ChoiceFDAMethod);
		methods.add(TESTConstants.ChoiceNearestNeighborMethod);
		methods.add(TESTConstants.ChoiceConsensus);

		return methods;
	}

	

//	private void writeTextResultsBinary(AtomContainer ac, int index, String CAS, String query,
//			TESTPredictedValue tpv, String d, String endpoint, String method) {
//
//		java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
//		
//
//		try {	
//
//			
//			String smilesRan=ac.getProperty("SmilesRan");
//			
//			fwBatchTXT.write(index + d + CAS + d+query+d+smilesRan+d);
//			
//
//			if (tpv.expValLogMolar.isNaN()) {
//				fwBatchTXT.write("N/A" + d);
//			} else {
//				fwBatchTXT.write(d2.format(tpv.expValLogMolar) + d);
//			}
//
//			if (tpv.predValLogMolar.isNaN()) {
//				fwBatchTXT.write("N/A" + d);
//			} else {
//				fwBatchTXT.write(d2.format(tpv.predValLogMolar) + d);
//			}
//
//			// System.out.println(ExpToxVal+"\t"+PredToxVal);
//
//			if (tpv.expValLogMolar.isNaN()) {
//				fwBatchTXT.write("N/A" + d);
//			} else {
//				if (tpv.expValLogMolar < 0.5) {
//
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fwBatchTXT.write("Developmental NON-toxicant" + d); // mass
//
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						fwBatchTXT.write("Mutagenicity Negative" + d); // mass
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fwBatchTXT.write("Does NOT bind to estrogen receptor" + d); // mass
//					}
//				} else {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fwBatchTXT.write("Developmental toxicant" + d); // mass
//
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						fwBatchTXT.write("Mutagenicity Positive" + d); // mass
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fwBatchTXT.write("Binds to estrogen receptor" + d); // mass
//					}
//
//				}
//			}
//
//			if (tpv.predValLogMolar.isNaN()) {
//				fwBatchTXT.write("N/A"+d);
//			} else {
//
//				if (tpv.predValLogMolar < 0.5) {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fwBatchTXT.write("Developmental NON-toxicant"+d); // mass
//
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						fwBatchTXT.write("Mutagenicity Negative"+d); // mass
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fwBatchTXT.write("Does NOT bind to estrogen receptor" + d); // mass
//					}
//				} else {
//					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
//						fwBatchTXT.write("Developmental toxicant"+d); // mass
//
//					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
//						fwBatchTXT.write("Mutagenicity Positive"+d); // mass
//					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
//						fwBatchTXT.write("Binds to estrogen receptor" + d); // mass
//					}
//				}
//			}
//
//			fwBatchTXT.write(ac.getProperty("Error")+"");
//			fwBatchTXT.write("\r\n");
//			fwBatchTXT.flush();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}



	private int GetRunNumber(String ext,String endpoint,String method) {

		String path = "";

		int count = 1;
		File file = null;

		while (true) {
			path = fileOutputFolder.getAbsolutePath()+File.separator+"ToxRuns";
			path += File.separator + "batch_";

			if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				path += "Descriptors" + "_" + count + "." + ext;
			} else {
				path += endpoint.replace(" ", "_") + "_" + method + "_" + count + "." + ext;
			}

			file = new File(path);
			if (file.exists())
				count++;
			else
				break;
		}

		return count;

	}
	
	private String GetRunNumberAA() {

		String path = "";

		int count = 1;
		File file = null;

		while (true) {
			path = fileOutputFolder.getAbsolutePath()+File.separator+"HazardProfiles"+File.separator+"HazardProfiles"+count+".html";			
			file = new File(path);
			if (file.exists())
				count++;
			else
				break;
		}
		return count + "";

	}
	

	public void init(IAtomContainerSet moleculeSet,  
			boolean useFragmentsConstraint,boolean createReports,boolean createDetailedReports,boolean generateWebpages, 
			File fileOutputFolder, Object gui, 
//			boolean Use3D,boolean is3Dpresent, 
			String endpoint, 	
			String method, 
			int taskType,int runType,boolean runCTS,String libraryCTS) {

		this.moleculeSet = moleculeSet;
//		this.WriteResultsToFile = WriteResultsToFile;
		this.fileOutputFolder = fileOutputFolder;

		this.gui = gui;
//		this.Use3D = Use3D;
//		this.is3Dpresent = is3Dpresent;
//		this.endpoint = endpoint;
		
		this.params=new CalculationParameters();
		params.endpoints= new String[1];
		params.endpoints[0]=endpoint;			
		params.methods= new String[1];
		params.methods[0]=method;
		params.outputFile=fileOutputFolder.getAbsolutePath();
				
			
		this.taskType = taskType;
		this.runType = runType;
				
		WebTEST4.reportFolderName=fileOutputFolder.getAbsolutePath();		
		WebTEST4.useFragmentsConstraint=useFragmentsConstraint;
		
		WebTEST4.createReports=createReports;
		WebTEST4.createDetailedReports=createDetailedReports;
		WebTEST4.generateWebpages=generateWebpages;
				
		Set<WebReportType> wrt = WebReportType.getNone();
		wrt.add(WebReportType.HTML);
//		if (WebTEST4.createReports) wrt.add(WebReportType.JSON);
		
		params.reportTypes=wrt;
		
		this.runCTS=runCTS;
		this.libraryCTS=libraryCTS;

	}
	
	
	public void initForAA(IAtomContainerSet moleculeSet, 
			boolean useFragmentsConstraint, 
			File fileOutputFolder, Object gui,  
			int taskType,int runType,boolean runCTS) {

		this.moleculeSet = moleculeSet;
		this.fileOutputFolder = fileOutputFolder;
		this.gui = gui;

		this.taskType = taskType;
		this.runType = runType;
		this.runCTS=runCTS;

		WebTEST4.reportFolderName=fileOutputFolder.getAbsolutePath();		
		WebTEST4.createDetailedReports=false;//TODO set in init method
		WebTEST4.createReports=false;
		WebTEST4.useFragmentsConstraint=useFragmentsConstraint;
		WebTEST4.generateWebpages=true;
		
		this.taskType = taskType;
		this.runType = runType;
				
		
	}
	
	/**
	 * Called to start the task.
	 */
	public void go() {
		worker = new SwingWorker() {
			public Object construct() {
				current = 0;
				done = false;
				// canceled = false;
				statMessage = null;
				return new ActualTask();
			}
		};
		worker.start();
	}

	/**
	 * Called to find out how much work needs to be done.
	 * 
	 * @return Length of Task
	 */
	public int getLengthOfTask() {
		return lengthOfTask;
	}

	/**
	 * Called to find out how much has been done.
	 * 
	 * @return Current progress in %
	 */
	public int getCurrent() {
		return current;
	}

	public void stop() {
		done = true;
		this.df.done = true;

		statMessage = "canceled";

		if (gui instanceof TESTApplication) {
			((TESTApplication) gui).setCursor(Utilities.defaultCursor);
		} 
//		else if (gui instanceof ToxApplet7) {
//			((ToxApplet7) gui).setCursor(Utilities.defaultCursor);
//		}

		// gui.setCursor(Utilities.defaultCursor);

		System.out.println("\n****stop!****\n");

		// worker.interrupt();// this doesnt seem to want to work!

	}

	/**
	 * Called to find out if the task has completed.
	 * 
	 * @return Done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the most recent status message, or null if there is no current
	 * status message.
	 * 
	 * @return statMessage
	 */
	public String getMessage() {
		return statMessage;
	}

	public void setMessage(String message) {
		statMessage = message;
	}

	private AtomContainer calculate(String CAS, AtomContainer ac) {
				
		TESTApplication ta=(TESTApplication)gui;
		
		String endpoint=params.endpoints[0];
		String method=params.methods[0];
		
		String query=ac.getProperty("Query");
		
		// statMessage += "Molecule #" + Index;
		String error=(String) ac.getProperty("Error");
		List<TESTPredictedValue>listTPV=null;
		
		int index;
		
		if (taskType == TESTConstants.typeTaskBatch) {
			index = (Integer) ac.getProperty("Index");
		} else {
			index = 0;
		}

		
		DescriptorData dd=null;
		
		try {
			dd=WebTEST4.goDescriptors(ac);
			
			
//			System.out.println(dd.to_JSONVector_String(false));			
//			JsonArray ja=dd.toJSONVector();
//			JsonObject jo=new JsonObject();		
//			jo.addProperty("method", method);
//			jo.add("csv", ja);			
//			GsonBuilder builder = new GsonBuilder();
//			Gson gson = builder.create();
//			System.out.println(gson.toJson(jo));
			
			
			
			error=(String) ac.getProperty("Error");
			
			listTPV=WebTEST4.go2(ac,dd, params);

			
		} catch (Exception ex) {
			logger.error("Error running chemical{}",ex.getMessage());
			return ac;
		}
				
		if (taskType != TESTConstants.typeTaskBatch) return ac;
		
		
		String smiles="";
		
		try {
			SmilesGenerator sg =SmilesGenerator.unique();
			smiles = sg.create(ac);
			ac.setProperty("SmilesRan", smiles);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		
		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			TESTPredictedValue tpv=listTPV.get(0);
			
			tpv.smiles=smiles;
			tpv.index=new Integer(index);
			tpv.query=query;			
			
			ta.panelResults.addPrediction(tpv);
				
			if (method.contentEquals(TESTConstants.ChoiceConsensus)) {
				ta.panelResults.addPredictionAllMethods(tpv);
			} 
			
			
//			if (!TESTConstants.isBinary(endpoint)) {
//				WriteToxicityResultsForChemical(ac, fwBatchHTML, fwBatchTXT, index, CAS,query, tpv, del, error,endpoint,method);
//			} else {
//				WriteToxResultsForChemicalBinary(ac,fwBatchHTML, fwBatchTXT, index,CAS,query,tpv,del,error,endpoint,method);
//			}
//			
//			if (method.equals(TESTConstants.ChoiceConsensus)) {
//				WriteToxicityResultsForChemicalAllMethods(fwBatchConsensusTXT, index, CAS,query,listTPV, del, error,endpoint,ac);
//			}
		} else {// Descriptors
			
//			if(WebTEST4.createReports) WriteDescriptorResultsForChemicalToWebPage(fwBatchHTML, index, CAS, error);

			dd.Index=index;
			dd.Query=query;
			dd.SmilesRan=smiles;
											
			if (index==1) {
				ta.panelResults.initTableModelDescriptors();
				ta.panelResults.setVisible(true);
			}
			
			ta.panelResults.addPrediction(dd);
						
//			if (Strings.isEmpty(ac.getProperty("Error"))) {
//				df.WriteCSVLine(fwBatchTXT, dd, del);	
//			} else {
//				try {
//					fwBatchTXT.write(ac.getProperty("CAS")+del+"Error\r\n");
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
						


		}
		return ac;

	}

	void loadTrainingData(CalculationParameters params) {
		long t1 = System.currentTimeMillis();
		for (String endpoint : params.endpoints) {		
			setMessage("Loading training files for "+endpoint);			
			for (String method : params.methods) {
				WebTEST4.loadTrainingData(endpoint, method);
			}
		}
		long t2 = System.currentTimeMillis();

		logger.debug("Loading training data in {}s", (t2 - t1) / 1000.);
	}
	
	

	/**
	 * The actual long running task. This runs in a SwingWorker thread.
	 */

	public class ActualTask {

		ActualTask() {
			df.done = false;
			current = 0;
			
			WebTEST4.dashboardStructuresAvailable=WebTEST4.areDashboardStructuresAvailable();
			
			if (gui instanceof TESTApplication) {
				((TESTApplication) gui).setCursor(Utilities.waitCursor);
				
				if(((TESTApplication) gui).includeAA_Dashboard) {
					aad=new AADashboard();
				}
			} 
//			else if (gui instanceof ToxApplet7) {
//				((ToxApplet7) gui).setCursor(Utilities.waitCursor);
//			}

			if (runType==TESTConstants.typeRunEndpoint) {
				runEndpoint();
			} else if (runType==TESTConstants.typeRunAA) {
				runAA();
			} else {
				logger.error("Invalid run type");
				
			}
			

			if (gui instanceof TESTApplication) {
				((TESTApplication) gui).setCursor(Utilities.defaultCursor);
			} 
//			else if (gui instanceof ToxApplet7) {
//				((ToxApplet7) gui).setCursor(Utilities.defaultCursor);
//			}

		}
		
		private void runAA() {

			if (runCTS) {
				runCTS();
				if (moleculeSet.getAtomContainerCount()>1) {
					taskType=TESTConstants.typeTaskBatch;
				}
			}
			
			TableGeneratorHTML tgHTML = new TableGeneratorHTML();
			

			String outputHTMLFileName=null;
			String outputHTMLFilePath=null;
			
			String outputHTMLFolder=fileOutputFolder.getAbsolutePath()+File.separator+"HazardProfiles";

			String str1="HazardRecords"+File.separator+"HazardRecords_";

			
			
			if (taskType==TESTConstants.typeTaskBatch) {
				String runNumber = GetRunNumberAA();
				//e.g. MyToxicity/HazardProfiles/HazardProfiles1.html:
				outputHTMLFileName="HazardProfiles"+runNumber+".html";

			} else {
				AtomContainer ac=(AtomContainer)moleculeSet.getAtomContainer(0);
				String CAS=ac.getProperty("CAS");
				//e.g. MyToxicity/HazardProfiles/HazardProfile_CAS.html
				outputHTMLFileName="HazardProfile_"+CAS+".html";
			}
			
			outputHTMLFilePath=outputHTMLFolder+File.separator+outputHTMLFileName;
			
			String excelFileName=outputHTMLFileName.replace(".html", ".xlsx");
	        String excelFilePath=outputHTMLFolder+File.separator+excelFileName;


			try {
				Path directory = Paths.get(outputHTMLFolder);

				if (!Files.exists(directory)) {
					Files.createDirectories(directory);
				}

				//***************************************************************************************
				// Create writer for comparison webpage:
				FileWriter fw=new FileWriter(outputHTMLFilePath);
				
				//***************************************************************************************
				// Write header of comparison table webpage:
				tgHTML.writeHeaderForGUI(fw);
				//***************************************************************************************

				Chemicals chemicals=new Chemicals();
				
				String [] endpoints= {TESTConstants.ChoiceRat_LD50,
						TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50,
						TESTConstants.ChoiceMutagenicity,TESTConstants.ChoiceReproTox,
						TESTConstants.ChoiceBCF,
						TESTConstants.ChoiceEstrogenReceptor,
						TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
						TESTConstants.ChoiceWaterSolubility};

				String [] methods= {TESTConstants.ChoiceConsensus};
				
				Set<WebReportType> wrt = WebReportType.getNone();
				wrt.add(WebReportType.HTML);
				
				CalculationParameters cp=new CalculationParameters(null, null, endpoints, methods, wrt);
				loadTrainingData(cp);
								

				//			for (int i=0;i<1;i++) {
				for (int i=0;i<moleculeSet.getAtomContainerCount();i++) {
					AtomContainer ac=(AtomContainer)moleculeSet.getAtomContainer(i);
					String CAS=ac.getProperty(DSSToxRecord.strCAS);
					String name=ac.getProperty(DSSToxRecord.strName);
					
					String error=ac.getProperty("Error");
					error=error.replace("\n", "");
					error=error.replace("\r", "");
					error=error.trim();
					ac.setProperty("Error", error);

					

					statMessage = "Molecule ID = " + CAS + " (" + (i + 1) + " of " + moleculeSet.getAtomContainerCount() + ")";
					Chemical chemical=aad.runChemicalForGUI(CAS, name, ac,cp);

					//Get records from toxval database:
					

					String relativeRecordFolderPath=str1+CAS;
					String recordFolderPath=outputHTMLFolder+File.separator+relativeRecordFolderPath;
					
					//***************************************************************************************
					// Write separate webpage for records for each score
					for (Score score:chemical.getScores()) {
						tgHTML.writeRecordPage2(CAS, score,recordFolderPath);	
					}
					//***************************************************************************************
					// Write a row in comparison table webpage:
					tgHTML.WriteRowInComparisonTable(fw, ac,chemical, relativeRecordFolderPath);
					//***************************************************************************************

					SaveStructureToFile.CreateImageFile(ac, "structure_"+CAS,recordFolderPath);
					chemicals.add(chemical);

					//WebTEST4.printJSON(chemical);
					if (done)
						return;

				}
				//***************************************************************************************
				// Finish comparison webpage:
				tgHTML.WriteRestOfComparisonTable(fw);
				fw.close();
				//***************************************************************************************
				
				
				TableGeneratorExcel tgExcel = new TableGeneratorExcel();

				
		        XSSFWorkbook workbook = new XSSFWorkbook();
		        tgExcel.writeFinalScoresToWorkbook(chemicals, workbook);
		        tgExcel.writeScoreRecordsToWorkbook(chemicals,workbook);
		        
		        //TODO add option for json output
		        String jsonFilePath=outputHTMLFilePath.replace(".html", ".json");
		        chemicals.writeToFile(jsonFilePath);
			
		        
		        //TODO- add flag if we are generate excel or html...
		        
		        if (isExcelOpen(excelFilePath)) {
//		        	System.out.println("open");
		        	JOptionPane.showMessageDialog((TESTApplication)gui, "Please close "+excelFilePath);
		        	done = true;
		        	return;
		        			        	
		        } else {
		        	
//		        	System.out.println("closed");
					FileOutputStream out = new FileOutputStream(new File(excelFilePath));
		            workbook.write(out);
		            out.close();
		        }
		        
				
				
				if (done)
					return;

				done = true;

//				URL myURL;
				File htmlFile=new File(outputHTMLFilePath);
//				File htmlFile=new File(excelFilePath);
//				myURL=htmlFile.toURI().toURL();
//				String strURL=myURL.toString();

				if (taskType == TESTConstants.typeTaskBatch) {
					TESTApplication f=((TESTApplication) gui);
					f.as.addBatchFilePath(htmlFile.getAbsolutePath());
					f.as.saveSettingsToFile();
//					f.setupRecentBatchFiles();
				}
				
				
				//	Fix for files on EPA's network drive:
//				strURL=strURL.replace("file:////", "file://///");
//				BrowserLauncher launcher = new BrowserLauncher(null);
//				launcher.openURLinBrowser(strURL);//doesnt seem to work for applet!
				
				MyBrowserLauncher.launch(htmlFile.toURI());

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		private String runCTS() {

			CTS_Generate_Breakdown_Products c=new CTS_Generate_Breakdown_Products();

			AtomContainerSet ms2=new AtomContainerSet();

			for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
				AtomContainer ac=(AtomContainer)moleculeSet.getAtomContainer(i);
				ms2.addAtomContainer(ac);
				ac.setProperty("Index", ms2.getAtomContainerCount());
				
				statMessage="Running CTS for "+ac.getProperty("CAS");

				//For now use dummy results from CTS:
				//				ArrayList<ChildChemical>childChemicals=c.loadJsonFromFile2("AADashboard calcs/outputCTS.json");

				try {
					SmilesGenerator sg =SmilesGenerator.unique();
					String smiles = sg.create(ac);

					String strJSON=c.runCTS(c.urlCTS, smiles, c.numGenerations, libraryCTS);
					//TODO account for no children or it times out
					
//					System.out.println(strJSON);
					
					if (strJSON==null || strJSON.contains("Error")) {
						return strJSON;
					}
					
//					ArrayList<ChildChemical>childChemicals=c.getTransformationProducts(strJSON);
//					childChemicals=c.combineChemicals(childChemicals);

					GsonBuilder builder = new GsonBuilder();
					builder.setPrettyPrinting();
					Gson gson = builder.create();
					ResultsCTS rc = gson.fromJson(strJSON, ResultsCTS.class);
					
					ArrayList<Child>allChildren=ResultsCTS.getAllChildren(rc);		
					ArrayList<Child>uniqueChildren=ResultsCTS.combineChemicals(allChildren);

//					for (ChildChemical cc:childChemicals) {
					for (Child cc:uniqueChildren) {	
						try {

							//TODO should we add options to keep possible or unlikely products???							
							if (!cc.data.likelihood.contentEquals("LIKELY")) continue;
							
							AtomContainer m = WebTEST4.loadSMILES(cc.data.smiles);
							m = TaskStructureSearch.CleanUpMolecule(m);
							m.setProperty("Parent", ac.getProperty("CAS"));
							m.setProperty("Likelihood", cc.data.likelihood);
							m.setProperty("Accumulation", cc.data.accumulation);
							
							String Query="Product of " + m.getProperty("Parent") + ", "+
							"Accumulation = "+m.getProperty("Accumulation");

							m.setProperty("Query",Query);
							
							
							ms2.addAtomContainer(m);	
							m.setProperty("Index", ms2.getAtomContainerCount());
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
//						System.out.println(cc.smiles+"\t"+cc.likelihood);
					}				

				} catch (Exception ex) {
					continue;
				}
			}
			moleculeSet=ms2;
			return "OK";
			
//			System.out.println(ms2.getAtomContainerCount());
			
		}
		
		
		private void runEndpoint() {
									
//			File folder=new File(fileOutputFolder.getAbsolutePath()+File.separator+"ToxRuns");
//			folder.mkdirs();

			
			String endpoint=params.endpoints[0];
			String method=params.methods[0];
			
			if (runCTS) {

				String msg=runCTS();
				
//				System.out.println("msg="+msg);
				
				if (msg.contains("Error")) {
					JOptionPane.showMessageDialog((TESTApplication) gui,
							"CTS calculations were not completed successfully.\nThe CTS web service may not be currently available");
				} else {
					if (moleculeSet.getAtomContainerCount()>1) {
						taskType=TESTConstants.typeTaskBatch;
						
						TESTApplication f=(TESTApplication) gui;						
						
						if (!endpoint.contentEquals(TESTConstants.ChoiceDescriptors)) {
							f.panelResults.setTitle("Prediction results including environmental transformation products: "+endpoint);
							f.panelResults.initTableModel(endpoint, method);		
						} else {
							f.panelResults.setTitle("Descriptor values including environmental transformation products");
							f.panelResults.initTableModelDescriptors();
						}
						f.panelResults.setVisible(true);
						f.panelResults.jbSaveToHTML.setVisible(true);
					} else {
						JOptionPane.showMessageDialog((TESTApplication) gui, "No transformation products were generated by CTS");
					}
				}
			}
			
			// ************************************************
			// Figure out methods:
			ArrayList<String> methods = getMethods(endpoint);
			
			// *******************************************************

//			int runNumberHTML = GetRunNumber("html",endpoint,method);
//			int runNumberTEXT = GetRunNumber("txt",endpoint,method);			
//			int runNumber=Math.max(runNumberHTML, runNumberTEXT);
//			File htmlFileBatch=getBatchHTMLFile(runNumber,endpoint,method);

//			if (taskType == TESTConstants.typeTaskBatch) 
//				setUpFileWriters(methods,htmlFileBatch,runNumber,endpoint,method);
						
			loadTrainingData(params);

			for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {

				// if (i==10) break;//for debug
				if (done)
					return;
				AtomContainer m = (AtomContainer) moleculeSet.getAtomContainer(i);
				String CAS = (String) m.getProperty("CAS");
												
				statMessage = "Molecule ID = " + CAS + " (" + (i + 1) + " of " + moleculeSet.getAtomContainerCount() + ")";
				
				String error=m.getProperty("Error");
				error=error.replace("\n", "");
				error=error.replace("\r", "");
				error=error.trim();
				m.setProperty("Error", error);
				
				
				m=calculate(CAS, m);
				
				// System.out.println("Total
				// Memory"+Runtime.getRuntime().totalMemory());
				// System.out.println("Free
				// Memory"+Runtime.getRuntime().freeMemory());

			} // end loop over molecules

			if (done)
				return;

			if (taskType == TESTConstants.typeTaskBatch) {
				try {
					
//					if (WebTEST4.generateWebpages) {
//						fwBatchHTML.write("</table></body></html>\r\n");
//						fwBatchHTML.close();
//					}
//										
//					fwBatchTXT.close();
//
//					if (method.equals(TESTConstants.ChoiceConsensus)) {
//						fwBatchConsensusTXT.close();
//					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				IAtomContainer m = moleculeSet.getAtomContainer(0);
				
				String error=m.getProperty("Error");
				
//				System.out.println("error="+error);
				
				if (!Strings.isEmpty(error)) {
					TESTApplication f = (TESTApplication) gui;
					JOptionPane.showMessageDialog(f, "Calculation error: "+error);
					done = true;
					return;
				}
				
				String CAS = (String) m.getProperty("CAS");
				File fileToDisplay=getFilePathSingle(CAS,endpoint,method);
				
				URL myURL;
				try {
					myURL = fileToDisplay.toURI().toURL();
					String strURL = myURL.toString();
					MyBrowserLauncher.launch(fileToDisplay.toURI());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	

			}

			TESTApplication f=(TESTApplication)gui;
			
			
			if (commandLineOutputFile==null) {
				done =true;				
//				if (taskType==TESTConstants.typeTaskBatch)
//					JOptionPane.showMessageDialog(f, "If desired, save the results using the buttons below.", "Batch calculations complete", JOptionPane.PLAIN_MESSAGE);
				return;
			}
			

			//**************************************************************************************
			//Save the results if running from command line:
			try {
				
				String ext=FileUtils.getFilenameExt(commandLineOutputFile.getName()).toLowerCase();
								
				if (ext.contains("xls")) {
					f.panelResults.writeToExcelFile(commandLineOutputFile,endpoint,method);
				} else if (ext.contains("csv")) {
					f.panelResults.writeToTextFile(commandLineOutputFile,endpoint);
				} else if (ext.contains("htm")) {
					f.panelResults.writeToHTMLFile(commandLineOutputFile,endpoint);
				} else {
					System.out.println("else!");	
				}
				
				System.out.println("\n*** Completed saving to "+commandLineOutputFile.getAbsolutePath());
				System.exit(0);
				
			} catch (Exception ex) {
				System.out.println(ex);
			}
						
			done = true;
			

			//			try {
//
//				File fileToDisplay=null;
//
//				if (taskType == TESTConstants.typeTaskBatch) {					
//										
//					if (endpoint.contentEquals(TESTConstants.ChoiceDescriptors) && reportType.contentEquals(TESTConstants.strReportTypeSummary)) {						
//						String filename=htmlFileBatch.getName().replace("html", "txt");
//						fileToDisplay=new File(folder+File.separator+filename);						
//					} else if (reportType.contentEquals(TESTConstants.strReportTypeExcel )) {
//						String filename=htmlFileBatch.getName().replace("html", "xlsx");
//						fileToDisplay=new File(folder+File.separator+filename);	
//						
//						Vector<File>files=new Vector<>();
//						
//						files.add(txtFile);
//						
//						if (method.contentEquals(TESTConstants.ChoiceConsensus)) {
//							files.add(txtFileAll);
//						}
//																		
//						ExcelUtilities.createSpreadsheetFromTextFiles(files, fileToDisplay,del);
//						
//						
//					} else {
//						fileToDisplay=htmlFileBatch;	
//					}
//					
//					
//				} 	else fileToDisplay=htmlFileSingle;
//
//				URL myURL= fileToDisplay.toURI().toURL();	
//				String strURL = myURL.toString();
//
//				// Fix for files on EPA's network drive:
//				strURL = strURL.replace("file:////", "file://///");
//
//				if (gui instanceof TESTApplication) {
////					BrowserLauncher launcher = new BrowserLauncher(null);
////					launcher.openURLinBrowser(strURL);// doesnt seem to work for
//					
//					MyBrowserLauncher.launch(fileToDisplay.toURI());
//					
//					
////					System.out.println(strURL);
//					
//					if (taskType == TESTConstants.typeTaskBatch) { 
//						TESTApplication f=((TESTApplication) gui);
//						f.as.addBatchFilePath(fileToDisplay.getAbsolutePath());
//						f.as.saveSettingsToFile();
//						f.setupRecentBatchFiles();
//					}
//
//
//				} 
//				
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}

		private File getFilePathSingle(String CAS,String endpoint,String method) {
			// TODO Auto-generated method stub

			File htmlFileSingle=null;

			// TODO
			String strOutputFolder1 = fileOutputFolder.getAbsolutePath() + File.separator + "ToxRuns";
			String strOutputFolder2 = strOutputFolder1 + File.separator + "ToxRun_" + CAS;
			String strOutputFolder3 = strOutputFolder2 + File.separator + "StructureData";
			String strOutputFolder4 = strOutputFolder2 + File.separator + endpoint;

			String filename = "";
			if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				filename = "DescriptorData_"+CAS+".html";
				htmlFileSingle= new File(strOutputFolder3 + File.separator + filename);

			} else {

				//				filename = "PredictionResults";
				//				filename += method.replaceAll(" ", "");
				//				filename += ".html";
				String fileNameNoExtension=WebTEST4.getResultFileNameNoExtension(endpoint, method, CAS);
				filename=fileNameNoExtension+".html";

				htmlFileSingle= new File(strOutputFolder4 + File.separator + filename);
			}

			// here add file to recent file list:
			if (gui instanceof TESTApplication) {

				TESTApplication f = (TESTApplication) gui;

				if(WebTEST4.createDetailedReports) {
					String molFilePath = strOutputFolder3 + File.separator + CAS + ".mol";
					f.as.addFilePath(molFilePath);
					f.as.saveSettingsToFile();
				}

				//gui8.setupRecentFiles();
				// we are done:
				f.setCursor(Utilities.defaultCursor);
			} 

			return htmlFileSingle;
		}
		
	}// end ActualTaskConstructor

	
	
	
	
	
	File getBatchHTMLFile(int runNumber,String endpoint,String method) {
		String htmlFileNameMethod;
		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			htmlFileNameMethod = "batch_Descriptors" + "_" + runNumber + ".html";
		} else {
			htmlFileNameMethod = "Batch_" + endpoint.replace(" ", "_") + "_" + method + "_" + runNumber + ".html";
		}
		
		
		File htmlFile = new File(fileOutputFolder.getAbsolutePath()+File.separator+"ToxRuns" + File.separator + htmlFileNameMethod);
		return htmlFile;
	}
	

	
	public static boolean isExcelOpen(String filePath) {
		
		try  {
			FileWriter fw = new FileWriter(filePath);  
			fw.close();
			
			return false;
		} catch (IOException e) {
		    return true;
		}
	}
	
//	public static boolean go(CommandLine cmd) throws Exception {
////		CalculationParameters params = new CalculationParameters();
////		params.smilesColumn = cmd.getOptionValue("smiles-column");
//		
//		String [] endpoints=cmd.getOptionValues("endpoint");
//		String [] methods=cmd.getOptionValues("method");
//				
//		String endpoint=TESTConstants.getFullEndpoint(endpoints[0]);
//		String method=TESTConstants.getFullMethod(methods[0]);	
//				
//		String outputFilePath = cmd.getOptionValue("output");
//				
//		
//		// pass/fail - do as much check as possible in one pass
//		boolean pass = true;
//
//		// File format checks
//		// Input file format checks
//		String inputFilePath = cmd.getOptionValue("input");
////		String smilesColumn = params.smilesColumn;
//		int fmt = TESTConstants.getFormatByFileName(inputFilePath);
//		if (fmt == -1) {
//			logger.error("Unable to recognize input file type by extension");
//			pass = false;
//		}
//		if (fmt != TESTConstants.numFormatSDF && fmt != TESTConstants.numFormatSMI && fmt != TESTConstants.numFormatMOL
//				&& fmt != TESTConstants.numFormatCSV) {
//			logger.error("invalid input filetype - supported are SDF, SMI, MOL and CSV");
//			pass = false;
//		}
//		
////		if (fmt == TESTConstants.numFormatCSV && StringUtils.isEmpty(smilesColumn)) {
////			logger.error("SMILES column name is not specified for CSV input file");
////			pass = false;
////		}
////
////		// Output file format checks
////		String outputFilePath = cmd.getOptionValue("output");
////		if (StringUtils.isEmpty(outputFilePath))
////			outputFilePath = FilenameUtils.getBaseName(inputFilePath) + ".txt";
////		else {
////			fmt = TESTConstants.getFormatByFileName(outputFilePath);
////			if (fmt == -1) {
////				logger.error("Unable to recognize output file type by extension");
////				pass = false;
////			}
////			if (fmt != TESTConstants.numFormatSDF && fmt != TESTConstants.numFormatCSV) {
////				logger.error("invalid output file type - supported are SDF and CSV");
////				pass = false;
////			}
////		}
//
//				
////		if (ArrayUtils.contains(endpoints, "?")) {
////			logger.error("invalid endpoint(s)");
////			pass = false;
////		}
////
////		// Methods checks
////		
////		if (!ArrayUtils.contains(endpoints, TESTConstants.abbrevChoiceDescriptors)
////				&& ArrayUtils.contains(methods, "?")) {
////			logger.error("invalid method");
////			pass = false;
////		}
//
//		if (pass) {
//			
//			File inputFile=new File(inputFilePath);
//			if (!inputFile.exists())  {
//				logger.error("Input file does not exist:"+inputFilePath);
//				return false;
//			}
//						
//			int inputFormat=TESTConstants.getFormatByFileName(inputFilePath);
//
//			
//			AtomContainerSet acs=null;
//			
//			if (inputFormat==TESTConstants.numFormatSDF) {
//				acs=TaskStructureSearch.LoadFromSDF(inputFilePath);				
//			} else if (inputFormat==TESTConstants.numFormatSMI) {
//				acs=TaskStructureSearch.LoadFromSmilesList(inputFilePath);
//			}
//			
//			for (int i=0;i<acs.getAtomContainerCount();i++) {
//				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
//				ac.setProperty("Index", (i+1));
//			}
//			
//			TaskCalculations2 t=new TaskCalculations2();
//			
//			File fileOutputFolder=new File(outputFilePath);
//			Object gui=null;
//			
//			int taskType=TESTConstants.typeTaskBatch;
//			int runType=TESTConstants.typeRunEndpoint;
//			boolean runCTS=false;
//			String libraryCTS=null;
//			
//			
//			t.init(acs, true, TESTConstants.strReportTypeExcel,fileOutputFolder, gui, 
//					endpoint,method, taskType, runType, runCTS,libraryCTS);
//			
//			t.runFromCommandLine();
//			
//
//		}
//
//		return pass;
//	}
		
//	public void runFromCommandLine() {
//				
//		String endpoint=params.endpoints[0];
//		String method=params.methods[0];
//		
//		// ************************************************
//		// Figure out methods:
//		ArrayList<String> methods = getMethods(params.endpoints[0]);		
//		// *******************************************************
//
//
//		int runNumber = GetRunNumber("html",endpoint,method);
//
//		File htmlFileBatch=getBatchHTMLFile(runNumber,endpoint,method);
//
////		System.out.println(htmlFileBatch.getAbsolutePath());
//		
////		setUpFileWriters(methods,htmlFileBatch,runNumber,endpoint,method);
//
//		
//		Set<WebReportType> wrt = WebReportType.getNone();
//		wrt.add(WebReportType.HTML);
//		
//		if (WebTEST4.createDetailedReports)
//			wrt.add(WebReportType.JSON);
//		
//		params.reportTypes=wrt;
//
//		loadTrainingData(params);
//		
//		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
//			if (done)
//				return;
//			AtomContainer m = (AtomContainer) moleculeSet.getAtomContainer(i);
//			String CAS = (String) m.getProperty("CAS");			
//			statMessage = "Molecule ID = " + CAS + " (" + (i + 1) + " of " + moleculeSet.getAtomContainerCount() + ")";			
//			m=calculate(CAS, m);
//			
//		} // end loop over molecules
//
//		if (done)
//			return;
//
//		try {
////			fwBatchHTML.write("</table></body></html>\r\n");
////			fwBatchHTML.close();
////			fwBatchTXT.close();
//
////			if (method.equals(TESTConstants.ChoiceConsensus)) {
////				fwBatchConsensusTXT.close();
////			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		done = true;
//
//		try {
//
//			File htmlFile=htmlFileBatch;
//			
//			URL myURL= htmlFile.toURI().toURL();	
//			String strURL = myURL.toString();
//
//			// Fix for files on EPA's network drive:
//			strURL = strURL.replace("file:////", "file://///");
//
//			
//			BrowserLauncher launcher = new BrowserLauncher(null);
//			launcher.openURLinBrowser(strURL);// doesnt seem to work for
//			 
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	


	public static void main(String[] args) throws Exception {
//		Options options = WebTEST.createOptions();
//		try {
//			CommandLineParser parser = new DefaultParser();
//			CommandLine cmd = parser.parse(options, args);
//			logger.info("Running command-line WebTEST...");
//			go(cmd);
//		} catch (ParseException exp) {
//			HelpFormatter formatter = new HelpFormatter();
//			formatter.printHelp("WebTEST <options>", "", options, exp.getMessage());
//		}
	}
}
