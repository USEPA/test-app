import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Task_TEST_Prediction;
import ToxPredictor.Application.Task_TEST_Prediction_All_Endpoints;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST2;
import ToxPredictor.Application.WebTEST3;
import ToxPredictor.Application.WebTEST5;
import ToxPredictor.Application.WebTESTDBs;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.HueckelAromaticityDetector;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.misc.MolFileUtilities;
import wekalite.CSVLoader;
import wekalite.Instances;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
//import ToxPredictor.misc.ParseChemidplus;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.smiles.SmilesParser;
//import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QSARTest {

	
	/**
	 * Convenience method to just rerun the methods for a single endpoint
	 */
	void runSingleEndpoint() {
		
//		String endpoint=TESTConstants.ChoiceDM_LC50;
		String endpoint=TESTConstants.ChoiceReproTox;
		
		String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
		
		Vector<String> methods=new Vector<String>();
		methods.add(TESTConstants.ChoiceHierarchicalMethod);
		methods.add(TESTConstants.ChoiceSingleModelMethod);
		methods.add(TESTConstants.ChoiceGroupContributionMethod);
		methods.add(TESTConstants.ChoiceNearestNeighborMethod);
		methods.add(TESTConstants.ChoiceConsensus);
		
		try {
			for ( int i=0;i<methods.size();i++) {

				String method=methods.get(i);

				File outputFolder1=new File("test-results/"+endpoint);
				if (!outputFolder1.exists()) outputFolder1.mkdir();
				
				File outputFolder=new File("test-results/"+endpoint+"/"+method);
				if (!outputFolder.exists()) outputFolder.mkdir();
				
				// Run on validation set
				File inFilePath = new File("data/"+endpointAbbrev+"/"+endpointAbbrev+"_prediction.sdf"); 
				File outFilePath = new File(outputFolder.getAbsolutePath()+"/"+inFilePath.getName().replace(".sdf",".txt"));

				System.out.println("\r\n"+inFilePath.getName()+"\t"+method);//so we know where we are at in tests
				WebTEST.go(inFilePath.getAbsolutePath(), outFilePath.getAbsolutePath(), TESTConstants.getEndpoint(TESTConstants.getAbbrevEndpoint(endpoint)), TESTConstants.getMethod(TESTConstants.getAbbrevMethod(method)));

				// Run on training set
				inFilePath = new File("data/"+endpointAbbrev+"/"+endpointAbbrev+"_training.sdf");
				outFilePath = new File("test-results/"+endpoint+"/"+method+"/"+inFilePath.getName().replace(".sdf",".txt"));
				
				System.out.println("\r\n"+inFilePath.getName()+"\t"+method);//so we know where we are at in tests
				WebTEST.go(inFilePath.getAbsolutePath(), outFilePath.getAbsolutePath(), TESTConstants.getEndpoint(TESTConstants.getAbbrevEndpoint(endpoint)), TESTConstants.getMethod(TESTConstants.getAbbrevMethod(method)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Test generating web pages for a sdf file
	 */
	void testBatchReportGen() {
		try {
			String endpoint = TESTConstants.ChoiceFHM_LC50;

			String input="data/LC50/LC50_training.sdf";
			String outputFilePath="L:/Priv/Cin/NRMRL/CompTox/javax/web-test/web-reports/"+endpoint+".txt";
			
			String method = TESTConstants.ChoiceConsensus;
			boolean writeFiles=true;

			WebTEST.go(input, outputFilePath, TESTConstants.getEndpoint(
					TESTConstants.getAbbrevEndpoint(endpoint)), 
					TESTConstants.getMethod(TESTConstants.getAbbrevMethod(method)), 
					WebReportType.getAll());
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}

	void runSDFallEndpoints() {
		String newStrFolder="todd/web-reports_rerun";
		WebTEST.reportFolderName=newStrFolder;
		WebTEST.createDetailedConsensusReport=false;

		List<String>endpoints=TESTConstants.getFullEndpoints(null);
		String method=TESTConstants.ChoiceConsensus;
		
		Set<WebReportType> wrt = WebReportType.getNone();
		wrt.add(WebReportType.HTML);
		wrt.add(WebReportType.JSON);
		
		try {
			
			String inputFilePath=newStrFolder+"/test.sdf";
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);

			for (int j = 0; j < endpoints.size(); j++) {
				String outputFilePath=newStrFolder+"/"+endpoints.get(j)+".txt";
				WebTEST.go(acs,new CalculationParameters(outputFilePath, null, endpoints.get(j), method, wrt));
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void createDescriptorsForSDF() {
		
		try {
			String folder="C:/Users/TMARTI02/OneDrive - Environmental Protection Agency (EPA)/0 java/TEST5/ToxPredictor/Sheffield";
			String inputFilePath=folder+"/CDK_formatted.sdf";
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);
			Set<WebReportType> wrt = WebReportType.getNone();
			Logger l=(Logger) LogManager.getLogger(WebTEST.class);
			l.setLevel(Level.ERROR);
			
			String outputFilePath=folder+"/descriptors using webtest from java code.txt";
//			WebTEST.go(acs,new CalculationParameters(outputFilePath, null, TESTConstants.ChoiceDescriptors, TESTConstants.ChoiceNotApplicable, wrt));
			
			CSVLoader c=new CSVLoader();			
			String outputFilePathTSV=folder+"/descriptors using webtest from java code-Descriptors.tsv";

			Instances instancesWebTEST=c.getDataSetFromFileNoTox(outputFilePathTSV, "\t");
			Instances instancesGUI=c.getDataSetFromFileNoTox(folder+"/descriptors using framain8.txt", "|");

//			System.out.println(instancesWebTEST.numAttributes()+"\t"+instancesWebTEST.numInstances());
//			System.out.println(instancesGUI.numAttributes()+"\t"+instancesGUI.numInstances());
			
			Instances.compare(instancesWebTEST, instancesGUI);
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	
	void runBadSmiles() {
//		String newStrFolder="web-reports-smi";
//		String newStrFolder="todd\\web-reports-bad-smiles-sdf-aromatic-bonds";
		String newStrFolder="todd\\web-reports-bad-smiles-sdf-dearomaticized";
		WebTEST.reportFolderName=newStrFolder;
		WebTEST.createDetailedConsensusReport=false;
		
		try {
			
//			String inputFilePath="data/bad smiles.smi";
			String inputFilePath="data/bad smiles.sdf";
//			String inputFilePath="data/bad smiles dearomaticized.sdf";
			
			String outputFilePath=newStrFolder+"/bad_smiles.txt";
			
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);
//			WebTEST.go(acs,endpoint,method,outputFilePath,writeFiles);

	        Set<WebReportType> wrt = WebReportType.getNone();
	        wrt.add(WebReportType.HTML);
	        wrt.add(WebReportType.JSON);

//			WebTEST.go(inputFilePath,outputFilePath,TESTConstants.numChoiceFHM_LC50,TESTConstants.numChoiceConsensus,true);

			ToxPredictor.Application.CalculationParameters params=new CalculationParameters(outputFilePath, null, TESTConstants.ChoiceFHM_LC50, TESTConstants.ChoiceConsensus, wrt);
			WebTEST.go(acs, params);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void getSDFFromCASListFromFolder() {
		
		String strFolder="todd\\web-reports (1)\\web-reports\\ToxRuns";
		
		String newStrFolder="todd/web-reports_rerun";
		
		File Folder2=new File(newStrFolder);
		if (!Folder2.exists()) Folder2.mkdir();
		
		File Folder=new File(strFolder);
		
		File [] files=Folder.listFiles();
		
		AtomContainerSet acsAll=new AtomContainerSet();
		for (int i = 0; i < files.length; i++) {

			File filei = files[i];

			if (filei.getName().indexOf("ToxRun") == -1)
				continue;

			String CAS = filei.getName().substring(filei.getName().indexOf("_") + 1, filei.getName().length());
//			System.out.println(CAS);
			//Load chemical from ChemistryDashboard URL:
			AtomContainerSet acs=MolFileUtilities.getAtomContainerSetFromDashboard(CAS);
			acsAll.addAtomContainer(acs.getAtomContainer(0));
		}
		
		
		try {
			SDFWriter sw=new SDFWriter(new FileWriter(newStrFolder+"/"+"test.sdf"));
			
			sw.write(acsAll);
			sw.close();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		
	}
	
	
	/**
	 * This method uses WebTEST.go(AtomContainer ac,CalculationParameters params) which doesnt output 
	 * batch text files and preserves DescriptorData
	 * 
	 * 
	 * 
	 */
	void runSingleChemical2() {
		
//		String CAS="91-20-3";
		String CAS="2278-50-4";
//		String CAS="57-27-9";//strychnine
//		String CAS="1287268-40-9";
//		String CAS="460347-61-9";
//		String CAS="349669-77-8";//wont get descriptors?
		
//		String endpoint = TESTConstants.ChoiceBCF;//log endpoint
		String endpoint = TESTConstants.ChoiceTP_IGC50;
		String method = TESTConstants.ChoiceConsensus;

		try {
			
			//Load chemical from ChemistryDashboard URL:
			String strAC=MolFileUtilities.getAtomContainerStringFromDashboard(CAS);
			
			AtomContainerSet acs=WebTEST.LoadFromSDF(strAC);
			
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(0);
			ac.setProperty("CAS", CAS);

			
			//use to check what happens if chemical is not in dashboard:
//			ac.setProperty("CAS", "bob");
			
//			ParseChemidplus p=new ParseChemidplus();
//			AtomContainer ac=p.LoadChemicalFromMolFileInJar("ValidatedStructures2d/"+CAS+".mol");

			
	        Set<WebReportType> wrt = WebReportType.getNone();
//	        wrt.add(WebReportType.HTML);
//	        wrt.add(WebReportType.JSON);
			
	        
//	        String dbpath="databases/"+CAS+".db";
	        			        
			WebTEST2.createDetailedConsensusReport=false;
			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;
	        
	        long t1=System.currentTimeMillis();

	        List<TESTPredictedValue>listTPV= WebTEST2.go(acs, new CalculationParameters(null, null, endpoint, method, wrt));
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println("Calc time="+(t2-t1));
	        
	        TESTPredictedValue tpv=listTPV.get(0);
	        
	        
	        System.out.println(tpv.predValLogMolar);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * This method uses WebTEST.go(AtomContainer ac,CalculationParameters params) which doesnt output 
	 * batch text files and preserves DescriptorData
	 * 
	 * 
	 * 
	 */
	void runSingleChemical3() {
		
		String CAS="91-20-3";
//		String CAS="2278-50-4";
//		String CAS="57-27-9";//strychnine
//		String CAS="1287268-40-9";
//		String CAS="460347-61-9";
//		String CAS="349669-77-8";//wont get descriptors?
		
//		String endpoint = TESTConstants.ChoiceBCF;//log endpoint
		String endpoint = TESTConstants.ChoiceTP_IGC50;
		String method = TESTConstants.ChoiceConsensus;

		try {
			
			//Load chemical from ChemistryDashboard URL:
			String strAC=MolFileUtilities.getAtomContainerStringFromDashboard(CAS);
			
			AtomContainerSet acs=WebTEST.LoadFromSDF(strAC);
			
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(0);
			ac.setProperty("CAS", CAS);

			
			//use to check what happens if chemical is not in dashboard:
//			ac.setProperty("CAS", "bob");
			
//			ParseChemidplus p=new ParseChemidplus();
//			AtomContainer ac=p.LoadChemicalFromMolFileInJar("ValidatedStructures2d/"+CAS+".mol");

			
	        Set<WebReportType> wrt = WebReportType.getNone();
//	        wrt.add(WebReportType.HTML);
//	        wrt.add(WebReportType.JSON);
			
	        
//	        String dbpath="databases/"+CAS+".db";
	        			        
			WebTEST3.createDetailedConsensusReport=false;
			
			WebTEST3.compressDescriptorsInDB=false;
	        
	        long t1=System.currentTimeMillis();

	        List<TESTPredictedValue>listTPV= WebTEST3.go(acs, new CalculationParameters(null, null, endpoint, method, wrt));
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println("Calc time="+(t2-t1));
	        
	        TESTPredictedValue tpv=listTPV.get(0);
	        
	        System.out.println("here runSingleChemical3, tpv.predValLogMolar="+tpv.predValLogMolar);

//	        for (int i=1;i<=100;i++) WebTEST3.go(acs, new CalculationParameters(null, null, endpoint, method, wrt));
//	        System.out.println("here2 runSingleChemical3, tpv.predValLogMolar="+tpv.predValLogMolar);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void runSmiles() {
		
//		String smiles="NC(=O)Nc8cc(Nc7nc(Nc6ccc(c5ccc(Nc4nc(Nc3ccc(\\N=N\\c1cc2c(cc1S(O)(=O)=O)cc(S(O)(=O)=O)cc2S(O)(=O)=O)c(NC(N)=O)c3)nc([Cl])n4)cc5S(O)(=O)=O)c(S(O)(=O)=O)c6)nc([Cl])n7)ccc8\\N=N\\c1cc2c(cc1S(O)(=O)=O)cc(S(O)(=O)=O)cc2S(O)(=O)=O";
//		String CAS="68133404";
		
//		String smiles="NC(N)NC([Cl])\\N=C\\Nc7ccc(N4C(=O)c5ccc6c1ccc3c2c1c(ccc2C(=O)N(c2ccc(Nc1nc(N)nc([Cl])n1)c(S(O)(=O)=O)c2)C3=O)c1ccc(C4=O)c5c61)cc7S(O)(=O)=O";
//		String CAS="106424719";
		
		String smiles="O=C1C2=C3C=4C(N5N=C6C=7C5=C(C4C=C2)C=CC7C(C=8C6=CC=CC8)=O)=CC=C3C9=C1C%10=C(NC%11=C%10C=CC%12=C%11C(=O)C=%13C(C%12=O)=CC=CC%13)C=C9";
		String CAS="2278-50-4";
		
		try {
			
			AtomContainerSet acs=WebTEST.LoadFromSMILES(smiles);
			
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(0);
			ac.setProperty("CAS", CAS);

	        Set<WebReportType> wrt = WebReportType.getNone();
//	        wrt.add(WebReportType.HTML);
//	        wrt.add(WebReportType.JSON);
			
			WebTEST2.createDetailedConsensusReport=false;
			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;
			
			DescriptorFactory.debug=true;
			
			
	        
	        long t1=System.currentTimeMillis();
	        
	        String endpoint=TESTConstants.ChoiceDescriptors;
	        String method=TESTConstants.ChoiceNotApplicable;

	        List<TESTPredictedValue>listTPV= WebTEST2.go(acs, new CalculationParameters(null, null, endpoint, method, wrt));
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println("Calc time="+(t2-t1));
	        
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
				
	}
	
	
	void runBadSmilesList() {
		
		try {
			
			AtomContainerSet acs=WebTEST.LoadFromSMI("data/bad smiles.smi");
			
	        Set<WebReportType> wrt = WebReportType.getNone();
//	        wrt.add(WebReportType.HTML);
//	        wrt.add(WebReportType.JSON);
			
			WebTEST2.createDetailedConsensusReport=false;
			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;
			DescriptorFactory.debug=true;
			
	        long t1=System.currentTimeMillis();
	        
	        String endpoint=TESTConstants.ChoiceDescriptors;
	        String method=TESTConstants.ChoiceNotApplicable;

	        List<TESTPredictedValue>listTPV= WebTEST2.go(acs, new CalculationParameters(null, null, endpoint, method, wrt));
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println("Calc time="+(t2-t1));
	        
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
				
	}
	
	
	void runBadSmilesListTextFile() {
		
		try {
			
			AtomContainerSet acs=WebTEST.LoadFromSMI("data/bad smiles.smi");
			
	        Set<WebReportType> wrt = WebReportType.getNone();
//	        wrt.add(WebReportType.HTML);
//	        wrt.add(WebReportType.JSON);
			
			WebTEST2.createDetailedConsensusReport=false;
			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;
			DescriptorFactory.debug=true;
			
	        long t1=System.currentTimeMillis();
	        
	        String endpoint=TESTConstants.ChoiceFHM_LC50;
	        String method=TESTConstants.ChoiceConsensus;

	        String outputFilePath="data/bad smiles.txt";
	        List<TESTPredictedValue>listTPV= WebTEST.go(acs, new CalculationParameters(outputFilePath, null, endpoint, method, wrt));
	        
	        long t2=System.currentTimeMillis();
	        
	        System.out.println("Calc time="+(t2-t1));
	        
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
				
	}
	
	
	void runMultipleMethods() {

//		String CAS = "71-43-2";
//		String CAS2 = "71-43-2";


		String CAS="590-18-1";//cis-2-butene
		String CAS2="624-64-6";//trans-2-butene
//		String CAS3="107-01-7";//2-butene
		
		String CAS3="2278-50-4";//causes descriptor error?
		String CAS4="7647-01-0";//only 1 non hydrogen atom
		
				
		ArrayList<String>casList=new ArrayList<>();
		casList.add(CAS);
		casList.add(CAS2);
//		casList.add(CAS3);
		casList.add(CAS4);;
		
		try {

			// Load chemical from ChemistryDashboard URL:
			AtomContainerSet acs = getAtomContainerSetFromDashboard(casList);

			WebTEST2.createDetailedConsensusReport = false;
			WebTEST2.usePreviousDescriptors = true;
			WebTEST2.compressDescriptorsInDB=false;
//			DescriptorFactory.debug=true;
			
			Set<WebReportType> wrt = WebReportType.getNone();
			wrt.add(WebReportType.HTML);
			wrt.add(WebReportType.JSON);

			List<String> endpoints=Arrays.asList(TESTConstants.ChoiceTP_IGC50);
			List<String> methods=Arrays.asList(TESTConstants.ChoiceConsensus,TESTConstants.ChoiceGroupContributionMethod);
			
			CalculationParameters params=new CalculationParameters(null, null, endpoints, methods, wrt);
			
			List<TESTPredictedValue> listTPV = WebTEST2.go(acs,params);


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private AtomContainerSet getAtomContainerSetFromDashboard(ArrayList<String>casList) throws FileNotFoundException, IOException {

		AtomContainerSet acs = new AtomContainerSet();

		for (String CAS:casList) {
			String strAC = MolFileUtilities.getAtomContainerStringFromDashboard(CAS);

			if (strAC==null) {
				System.out.println(CAS+" not available");
				continue;
			}
			AtomContainerSet acsCAS = WebTEST.LoadFromSDF(strAC);
			
//			System.out.println(strAC);
			
			AtomContainer ac = (AtomContainer) acsCAS.getAtomContainer(0);
			ac.setProperty("CAS", CAS);

			acs.addAtomContainer(ac);

		}
		return acs;
	}
	
	
	/**
	 * Run a single chemical for testing
	 */
	void runSingleChemical()  {

//				String CAS="2278-50-4";
//		String CAS="583-59-5";
		String CAS="91-20-3";
//		String CAS="71-43-2";
//		String CAS="57-24-9";//strychnine
//		String CAS="403483-19-2";//polycyclic- crashes ring finder!

//		String CAS="59-83-6";//salt
//		String CAS="60-09-3";//plot failed
		
//		String CAS="25-97-8";
		
		
//		String endpoint = TESTConstants.ChoiceFHM_LC50;
		String endpoint = TESTConstants.ChoiceBCF;//log endpoint
//		
//		String endpoint = TESTConstants.ChoiceMutagenicity;//binary endpoint
//		String endpoint = TESTConstants.ChoiceDensity;//not log endpoint
//		String endpoint=TESTConstants.ChoiceFlashPoint;
//		String endpoint=TESTConstants.ChoiceReproTox;
		
		
		//		String endpoint = TESTConstants.ChoiceViscosity;
//				String method = TESTConstants.ChoiceHierarchicalMethod;
		//		String method = TESTConstants.ChoiceSingleModelMethod;
		//		String method = TESTConstants.ChoiceFDAMethod;
		//		String method = TESTConstants.ChoiceGroupContributionMethod;
		//		String method = TESTConstants.ChoiceNearestNeighborMethod;
		String method = TESTConstants.ChoiceConsensus;
		//		String method=TESTConstants.ChoiceLDA;

		String outputFilePath="todd/test-results/"+endpoint.replaceAll(" ", "_")+"_"+method+"_"+CAS+".txt";

		try {
			
			//Load chemical from ChemistryDashboard URL:
			AtomContainerSet acs=MolFileUtilities.getAtomContainerSetFromDashboard(CAS);
			
			//use to check what happens if chemical is not in dashboard:
//			acs.getAtomContainer(0).setProperty("CAS", "bob");
			
//			ParseChemidplus p=new ParseChemidplus();
//			AtomContainer ac=p.LoadChemicalFromMolFileInJar("ValidatedStructures2d/"+CAS+".mol");
//			AtomContainerSet acs=new AtomContainerSet();
//			acs.addAtomContainer(ac);	

			//do multiple calcs to find file conflict
			//	for (int i=0;i<=10;i++) {
			//		AtomContainer cloneAC=(AtomContainer) ac.clone();
			//		acs.addAtomContainer(cloneAC);	
			//	}

//			WebTEST.reportFolderName="todd\\web-reports (1)\\web-reports\\";
//			WebTEST.reportFolderName="todd\\web-reports_rerun";
			
			WebTEST.createDetailedConsensusReport=false;
			
	        Set<WebReportType> wrt = WebReportType.getNone();
	        wrt.add(WebReportType.HTML);
	        wrt.add(WebReportType.JSON);
			
	        WebTEST.usePreviousDescriptors=true;
	        
			WebTEST.go(acs, new CalculationParameters(outputFilePath, null, endpoint, method, wrt));
			
//			String html ="web-reports/ToxRuns/ToxRun_"+CAS+"/"+endpoint+"/PredictionResultsConsensus.html";
//			String pdf = "web-reports/ToxRuns/ToxRun_"+CAS+"/"+endpoint+"/PredictionResultsConsensus.pdf";
//			HtmlUtils.HtmlToPdf(html, pdf);


		} catch (Exception ex){
			ex.printStackTrace();
		}

	}

	/**
	 * Run a single chemical for testing
	 */
	void runSingleChemicalAllEndpoints()  {

//				String CAS="2278-50-4";
//		String CAS="583-59-5";
		String CAS="91-20-3";
//		String CAS="71-43-2";
//		String CAS="57-24-9";//strychnine
//		String CAS="403483-19-2";//polycyclic- crashes ring finder!

//		String CAS="59-83-6";//salt
//		String CAS="60-09-3";//plot failed
		
//		String CAS="25-97-8";

		
//		WebTEST.reportFolderName="todd\\web-reports (1)\\web-reports\\";
//		WebTEST.reportFolderName="todd\\web-reports_rerun";
		WebTEST.createDetailedConsensusReport=false;
		
        Set<WebReportType> wrt = WebReportType.getNone();
        wrt.add(WebReportType.HTML);
        wrt.add(WebReportType.JSON);


		List<String>endpoints=TESTConstants.getFullEndpoints(null);
		String method = TESTConstants.ChoiceConsensus;

		try {
			
			//Load chemical from ChemistryDashboard URL:
			AtomContainerSet acs=MolFileUtilities.getAtomContainerSetFromDashboard(CAS);
			
			String of="todd/test-results";
			File OF=new File(of);
			OF.mkdirs();
			
			
			
			for (int i=0;i<endpoints.size();i++) {
				
				String outputFilePath=of+"/"+endpoints.get(i).replaceAll(" ", "_")+"_"+method+"_"+CAS+".txt";
				

				//	Do calcs:
//				WebTEST.loadTrainingData(endpoints[i], method);
//				WebTEST.doPredictions(acs, endpoints[i], method,outputFilePath, true);
				
				ToxPredictor.Application.CalculationParameters params=new CalculationParameters(outputFilePath, null, endpoints.get(i), method, wrt);
				WebTEST.go(acs, params);
			
			}
			

		} catch (Exception ex){
			ex.printStackTrace();
		}

	}
	
	
	/**
	 * Only calculates descriptors once
	 */
	void runSingleChemicalAllEndpoints2()  {

//				String CAS="2278-50-4";
//		String CAS="583-59-5";
//		String CAS="91-20-3";
//		String CAS="71-43-2";
//		String CAS="57-24-9";//strychnine
//		String CAS="403483-19-2";//polycyclic- crashes ring finder!
		String CAS="2278-50-4";
		
		
//		String CAS="59-83-6";//salt
//		String CAS="60-09-3";//plot failed
		
//		String CAS="25-97-8";

		
//		WebTEST.reportFolderName="todd\\web-reports (1)\\web-reports\\";
//		WebTEST.reportFolderName="todd\\web-reports_rerun";
		WebTEST.createDetailedConsensusReport=false;
		WebTEST.usePreviousDescriptors=true;
		
        Set<WebReportType> wrt = WebReportType.getNone();
//        wrt.add(WebReportType.HTML);
//        wrt.add(WebReportType.JSON);


		List<String> endpoints=TESTConstants.getFullEndpoints(null);
		String method = TESTConstants.ChoiceConsensus;

		try {
			
			String strAC=MolFileUtilities.getAtomContainerStringFromDashboard(CAS);
			AtomContainerSet acs=WebTEST.LoadFromSDF(strAC);
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(0);
			ac.setProperty("CAS", CAS);
			
//	        String dbpath="databases/"+CAS+".db";
	        			       
			WebTEST2.createDetailedConsensusReport=false;
			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;

			CalculationParameters cp=new CalculationParameters(null, null, endpoints, Arrays.asList(method), wrt);
			
			long t1=System.currentTimeMillis();			
			List<TESTPredictedValue> predictions=WebTEST2.go(acs,cp );			
			long t2=System.currentTimeMillis();
			
			System.out.println("calc time ="+(t2-t1));

		} catch (Exception ex){
			ex.printStackTrace();
		}

	}
	
	void runTESTSets() {
		
		List<String> endpoints = TESTConstants.getFullEndpoints(null); 

		Set<WebReportType> wrt = WebReportType.getNone();

		WebTEST2.usePreviousDescriptors=true;
		WebTEST2.compressDescriptorsInDB=false;
		WebTEST2.displayLoadingMessage=true;
		
		for (int i=0;i<endpoints.size();i++) {
			
//			if (!endpoints[i].equals(TESTConstants.ChoiceFlashPoint)) continue;
			
//		for (int i=12;i<endpoints.length;i++) {
			String method=TESTConstants.ChoiceConsensus;			
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoints.get(i));

			try {
//				String sdfPath="data/"+abbrev+"/"+abbrev+"_prediction.sdf";
				String sdfPath="data/"+abbrev+"/"+abbrev+"_training.sdf";
				
				AtomContainerSet acs = WebTEST2.LoadFromSDF(sdfPath);			
//				String dbPath="databases/TEST_sets_2018_10_19.db";
//				String dbPath=null;				
				List<TESTPredictedValue> listTPV = WebTEST2.go(acs,
					new CalculationParameters(null, null, endpoints.get(i), method, wrt));
			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	void runTESTSetsWebTEST3_multicore() {
		
		List<String> endpoints = TESTConstants.getFullEndpoints(null); 

		Set<WebReportType> wrt = WebReportType.getNone();

		int nproc=20;
		
    	ExecutorService threadExecutor = Executors.newFixedThreadPool(nproc);

    	long t1=System.currentTimeMillis();
    	
		for (int i=0;i<endpoints.size();i++) {
			
//			if (!endpoints[i].equals(TESTConstants.ChoiceFlashPoint)) continue;

			String method=TESTConstants.ChoiceConsensus;			
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoints.get(i));

			try {
//				String sdfPath="data/"+abbrev+"/"+abbrev+"_prediction.sdf";
				String sdfPath="data/"+abbrev+"/"+abbrev+"_training.sdf";
				
				AtomContainerSet acs = WebTEST3.LoadFromSDF(sdfPath);			
//				String dbPath="databases/TEST_sets_2018_10_19.db";
//				String dbPath=null;				
				
				
//				AtomContainerSet acs2 = new AtomContainerSet();
//				int n=10;
//				for (int ii=0;ii<n;ii++) {
//					acs2.addAtomContainer(acs.getAtomContainer(ii));
//				}
				
       			Task_TEST_Prediction task=new Task_TEST_Prediction();
       			task.init(acs,endpoints.get(i),method,wrt);
       			threadExecutor.execute(task ); // start task
        	
			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		long t2=System.currentTimeMillis();
		System.out.println((t2-t1)/1000+" seconds for all sets for nproc="+nproc);
		
	}
	
	
	void run_DataSet_WebTEST3_multicore() {
		
		Set<WebReportType> wrt = WebReportType.getNone();

		int nproc=10;
		
    	ExecutorService threadExecutor = Executors.newFixedThreadPool(nproc);

    	long t1=System.currentTimeMillis();
    	

			String method=TESTConstants.ChoiceConsensus;			
			String endpoint=TESTConstants.ChoiceRat_LD50;			
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

			try {
//				String sdfPath="data/"+abbrev+"/"+abbrev+"_prediction.sdf";
				String sdfPath="data/"+abbrev+"/"+abbrev+"_training.sdf";
				
				AtomContainerSet acs = WebTEST3.LoadFromSDF(sdfPath);	
				
				
				for (int ii=0;ii<acs.getAtomContainerCount();ii++) {
					AtomContainerSet acs2=new AtomContainerSet();
					acs2.addAtomContainer(acs.getAtomContainer(ii));
	       			Task_TEST_Prediction task=new Task_TEST_Prediction();
	       			task.init(acs2,endpoint,method,wrt);
	       			threadExecutor.execute(task ); // start task
				}
			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		
		
		long t2=System.currentTimeMillis();
		System.out.println((t2-t1)/1000+" seconds to set up tasks for nproc="+nproc);
		
	}

//	static void turnOffLogging() {
//		Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
//		for(String log:loggers) { 
//			Logger logger = (Logger)LoggerFactory.getLogger(log);
//			logger.setLevel(Level.INFO);
//			logger.setAdditive(false);
//		} //end
//	}
	
	/**
	 * Runs all chemicals in NCCT database for single endpoint with multicores
	 * 
	 * @param textFilePath
	 */
	public static void run800K_NCCT_Set_Multicore(String textFilePath) {

		//Maybe just run each endpoint in it's own core?
		
		try {
//			System.out.println("here");
						
			Set<WebReportType> wrt = WebReportType.getNone();
			String method=TESTConstants.ChoiceConsensus;			
//			String endpoint=TESTConstants.ChoiceRat_LD50;			
			String endpoint=TESTConstants.ChoiceFHM_LC50;
//			String endpoint=TESTConstants.ChoiceDM_LC50;

			int nproc=30;
	    	ExecutorService threadExecutor = Executors.newFixedThreadPool(nproc);

			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			BufferedReader br = new BufferedReader(new FileReader(textFilePath));
			String header = br.readLine();
			int counter = 0;
		
//			turnOffLogging();

			while (true) {
				String Line = br.readLine();

				counter++;
				//				if (counter==10000) break;

				if (Line == null)
					break;

				if (Line.equals("")) break;


				LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse3(Line, ",");

				if (counter%1000==0) {
					System.out.println(counter);
				}

				String smiles=null;
				
				try {

					//dtxsid	casrn	dtxcid	qsar-ready_dtxcid	qsar-ready_smiles
					//0			1		2		3					4

					String dsstox_substance_id=list.get(0);
					String cas=list.get(1);
					String dtxcid=list.get(2);					
					String qsar_ready_dtxcid=list.get(3);					
					smiles=list.get(4);
					
					AtomContainer molecule = (AtomContainer)sp.parseSmiles(smiles);
					
					molecule = WebTEST3.prepareMolecule(counter, molecule);
					
					molecule.setProperty("CAS",cas);
					molecule.setProperty("smiles",smiles);//use QSAR ready one?
					molecule.setProperty("dsstox_substance_id",dsstox_substance_id);
					molecule.setProperty("dsstox_compound_id",qsar_ready_dtxcid);//use QSAR ready one?
					
					AtomContainerSet acs=new AtomContainerSet();
					acs.addAtomContainer(molecule);

	       			Task_TEST_Prediction task=new Task_TEST_Prediction();
	       			task.init(acs,endpoint,method,wrt);
	       			threadExecutor.execute(task ); // start task

				} catch (InvalidSmilesException ex) {
					System.out.println("Can't parse "+smiles);
//					ex.printStackTrace();
				}

			}//end loop over lines in text file
				


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * Runs all chemicals in NCCT database for single endpoint with multicores
	 * 
	 * @param textFilePath
	 */
	public static void run800K_NCCT_Set_Multicore_AllEndpoints(String textFilePath) {

		//Maybe just run each endpoint in it's own core?

		try {
			int nproc=30;
			ExecutorService threadExecutor = Executors.newFixedThreadPool(nproc);

			int start=8558;//row to start running calcs
			int stop=50000;//row to stop at

//			String [] endpoints= {TESTConstants.ChoiceBCF};
			List<String> endpoints=TESTConstants.getFullEndpoints(null);
			List<String> methods= Arrays.asList(TESTConstants.ChoiceConsensus);

			WebTEST5.writePredictionsToDatabase=true;
			WebTEST5.addDescriptorsToDatabase=false;
			WebTEST5.createDetailedReport=false;
			WebTEST5.reportFolderName=null;
			
			HueckelAromaticityDetector.debug=false;
			Set<WebReportType> wrt = WebReportType.getNone();//dont generate reports						

			//*********************************************************************
			//Load training sets
			WebTEST5.LoadTrainingData(endpoints, methods);
			
//			WebTEST5.deleteDatabase();
			
			//Create db tables if needed
			Connection conn=WebTEST5.setupDatabase(endpoints);
			//*********************************************************************

			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			BufferedReader br = new BufferedReader(new FileReader(textFilePath));
			String header = br.readLine();
			int counter = 0;

			//			turnOffLogging();


			while (true) {
				String Line = br.readLine();

				counter++;

				if (counter<start) continue;
				if (counter==stop) break;

				if (Line == null)
					break;

				if (Line.equals("")) break;

				//				if (counter==1000) break;

				LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse3(Line, ",");

				if (counter%1000==0) {
					System.out.println(counter);
				}

				String smiles=null;

				try {

					//dtxsid	casrn	dtxcid	qsar-ready_dtxcid	qsar-ready_smiles
					//0			1		2		3					4

					String dsstox_substance_id=list.get(0);
					String cas=list.get(1);
					String dtxcid=list.get(2);					
					String qsar_ready_dtxcid=list.get(3);					
					smiles=list.get(4);

					//					System.out.println(cas+"\t"+smiles);

					AtomContainer molecule = (AtomContainer)sp.parseSmiles(smiles);

					molecule = WebTEST3.prepareMolecule(counter, molecule);

					molecule.setProperty(DSSToxRecord.strCAS,cas);
					molecule.setProperty(DSSToxRecord.strSmiles,smiles);//use QSAR ready one?
					molecule.setProperty(DSSToxRecord.strSID,dsstox_substance_id);
					molecule.setProperty(DSSToxRecord.strCID,qsar_ready_dtxcid);//use QSAR ready one?					
					molecule.setProperty(DSSToxRecord.strGSID,dsstox_substance_id.substring(dsstox_substance_id.length()-5));

					AtomContainerSet acs=new AtomContainerSet();
					acs.addAtomContainer(molecule);

					Task_TEST_Prediction_All_Endpoints task=new Task_TEST_Prediction_All_Endpoints();	       			

					task.init(molecule,endpoints,methods,wrt,conn);
					threadExecutor.execute(task ); // start task
					//	       			System.out.println(threadExecutor.isTerminated());

					//	       			if (cas.contentEquals("2113-61-3")) break;

				} catch (InvalidSmilesException ex) {
					System.out.println("Can't parse "+smiles);
					//					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}//end loop over lines in text file



		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	
	/**
	 * This method equally divides up data and minimizes connections to the database
	 */
	void run_DataSet_WebTEST3_multicore2() {


		Set<WebReportType> wrt = WebReportType.getNone();

		int nproc=20;

		ExecutorService threadExecutor = Executors.newFixedThreadPool(nproc);

		long t1=System.currentTimeMillis();

		String method=TESTConstants.ChoiceConsensus;			
		String endpoint=TESTConstants.ChoiceRat_LD50;			
		String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

		try {
			//				String sdfPath="data/"+abbrev+"/"+abbrev+"_prediction.sdf";
			String sdfPath="data/"+abbrev+"/"+abbrev+"_training.sdf";

			AtomContainerSet acs = WebTEST3.LoadFromSDF(sdfPath);			

			int nchems=acs.getAtomContainerCount()/nproc;
			System.out.println(nchems);

			for (int ii=1;ii<=nproc;ii++) {

				AtomContainerSet acs2=new AtomContainerSet();

				for (int jj=0;jj<nchems;jj++) {
					acs2.addAtomContainer(acs.getAtomContainer(0));
					acs.removeAtomContainer(0);
				}
				
				if (ii==nproc) {//add rest
					for (int jj=0;jj<acs.getAtomContainerCount();jj++) {
						acs2.addAtomContainer(acs.getAtomContainer(jj));
					}
				}

				this.writeAtomContainerSet(acs2, "data/"+abbrev+"/"+abbrev+"_"+ii+"_training.sdf");
				
				Task_TEST_Prediction task=new Task_TEST_Prediction();
				task.init(acs2,endpoint,method,wrt);
				threadExecutor.execute(task ); // start task
			}

			
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}


		long t2=System.currentTimeMillis();
		System.out.println((t2-t1)/1000+" seconds to set up tasks for nproc="+nproc);

	}
	
	

	
	void writeAtomContainerSet(AtomContainerSet acs,String filepath) {
		
		try {
			
			FileWriter fw=new FileWriter(filepath);
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(false);
			

			for (int i=0;i<acs.getAtomContainerCount();i++) {
				
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				System.out.println(ac.getProperty("CAS")+"");
				
				mw.writeMolecule(acs.getAtomContainer(i));
				fw.write("> <CAS>\r\n"); 
				fw.write(ac.getProperty("CAS")+"\r\n\r\n");
				
				fw.write("$$$$\n");
			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * Check to see if any compound in the TEST sets has a different structure in diff file
	 */
	void compileInChiKeysForTESTSets() {
		
		List<String> endpoints = TESTConstants.getFullEndpoints(null); 

		Set<WebReportType> wrt = WebReportType.getNone();

		Hashtable <String,String>ht=new Hashtable<>();
		
		String outputFolder="data/0 compare TEST set predictions/inchikeys";
		
		int count=0;
		
		for (int i=0;i<endpoints.size();i++) {
//		for (int i=12;i<endpoints.length;i++) {			
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoints.get(i));
			
			String set="prediction";
			String sdfPath="data/"+abbrev+"/"+abbrev+"_"+set+".sdf";
			String outputPath=outputFolder+"/"+abbrev+"_"+set+".txt";
			goThroughSDF(sdfPath,outputPath);
			
			set="training";
			sdfPath="data/"+abbrev+"/"+abbrev+"_"+set+".sdf";
			outputPath=outputFolder+"/"+abbrev+"_"+set+".txt";
			goThroughSDF(sdfPath,outputPath);
		}
		
	}

	private void goThroughSDF(String sdfPath,String outputPath) {
		try {
			
			System.out.println(sdfPath);
			AtomContainerSet acs = WebTEST2.LoadFromSDF(sdfPath);
			
			FileWriter fw=new FileWriter(outputPath);
			
			for (int j=0;j<acs.getAtomContainerCount();j++) {
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(j);				
				String CAS=ac.getProperty("CAS");
				Inchi results=CDKUtilities.generateInChiKey(ac);
				String InChiKey=results.inchiKey;				
				fw.write(CAS+"\t"+InChiKey+"\r\n");				
			}
			fw.close();
		
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	void runTSCA17K() {
		
		try {

			String sdfPath="AA Dashboard/Output/TSCA 17K/list_chemicals-2018-09-13-07-18-17_v2000.sdf";
			AtomContainerSet acs = WebTEST2.LoadFromSDF(sdfPath);
		
			
			
			WebTEST2.usePreviousDescriptors=false;//they have diff structures
			
//			String endpoint=TESTConstants.ChoiceFHM_LC50;
//			String endpoint=TESTConstants.ChoiceDM_LC50;
//			String endpoint=TESTConstants.ChoiceMutagenicity;
			String endpoint=TESTConstants.ChoiceRat_LD50;
			
//			String dbPath="databases/TSCA17K_"+endpoint+".db";
			
			String method=TESTConstants.ChoiceConsensus;
			Set<WebReportType> wrt = WebReportType.getNone();

			List<TESTPredictedValue> listTPV = WebTEST2.go(acs,
				new CalculationParameters(null, null, endpoint, method, wrt));
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QSARTest q=new QSARTest();
		
		//Run all TEST prediction sets:
//		q.runTESTSets();
//		q.compileInChiKeysForTESTSets();
		
//		q.runSmiles();
//		q.runBadSmilesList();
//		q.runBadSmilesListTextFile();
		
//		q.runTSCA17K();
		
//		q.runMultipleMethods();
		
//		q.runSingleChemical();

		
		
//		q.createDescriptorsForSDF();
		
//		q.runSingleChemicalAllEndpoints();
		
//		q.runSingleChemical2();
//		q.runSingleChemical3();
		
//		q.runTESTSetsWebTEST3_multicore();
//		q.run_DataSet_WebTEST3_multicore();
//		q.run_DataSet_WebTEST3_multicore2();
//		q.run800K_NCCT_Set_Multicore("data\\z NCCT_ID\\qsar-ready_smiles_v2_2019_02_19.txt");
//		q.run800K_NCCT_Set_Multicore_AllEndpoints("data\\0 NCCT structures\\qsar-ready_smiles_v2.txt");
		
//		q.runSingleChemicalAllEndpoints2();
		
//		q.getSDFFromCASListFromFolder();
//		q.runSDFallEndpoints();
		
//		q.testBatchReportGen();
//		q.runSingleEndpoint();
//		q.runBadSmiles();
//		
	}
}

