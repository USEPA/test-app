package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.TESTPredictedValue;

public class RunFromSmiles {

	
//	private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	
	public static AtomContainer createMolecule(String smiles,String DTXSID,String CAS) {
		AtomContainer m = WebTEST4.prepareSmilesMolecule(smiles);
		m.setProperty(DSSToxRecord.strSID,DTXSID);//store sid so dont need to look up later
		m.setProperty(DSSToxRecord.strSmiles, smiles);//need original smiles NOT QSAR ready smiles
		if(CAS!=null) m.setProperty(DSSToxRecord.strCAS, CAS);//need CAS for doing nearest neighbor method (to exclude training chemical by CAS)
		return m;
	}


	
	/**
	 * Run predictions in batch so don't have to reload model info each time 
	 * Results stored in hashtable by DTXSID
	 * 
	 * @param moleculeSet
	 * @param endpoint
	 * @param method
	 * @param createReports
	 * @param createDetailedReports
	 * @return hashtable of results with key as DTXSID and prediction results + report as TESTPredictedValue
	 * 
	 */
	public static Hashtable<String,PredictionResults> runEndpoint(AtomContainerSet moleculeSet, String endpoint,String method,boolean createReports,boolean createDetailedReports,String key) {				

		DescriptorFactory.debug=false;
		WebTEST4.createDetailedReports=createDetailedReports;
		WebTEST4.createReports=createReports;
		WebTEST4.generateWebpages=false;
		PredictToxicityJSONCreator.forGUI=true;
		
		Hashtable<String,PredictionResults>htResults=new Hashtable<>();
		
		// *******************************************************
		WebTEST4.loadTrainingData(endpoint,method);//Note: need to use webservice approach to make this data persistent

		String [] endpoints= {endpoint};
		
		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			AtomContainer ac = (AtomContainer) moleculeSet.getAtomContainer(i);
			ac=calculate(ac,endpoints,method);
			PredictionResults pr=getResults(ac,method); 

			String DTXSID=ac.getProperty(DSSToxRecord.strSID);
			String Smiles=ac.getProperty(DSSToxRecord.strSmiles);
			String error=(String) ac.getProperty("Error");
			String CAS=null;
			
//			System.out.println(DTXSID);
			
			
			if (pr==null) {
				pr=new PredictionResults();
				pr.setEndpoint(endpoint);
				pr.setMethod(method);
			}

			if (ac.getProperty(DSSToxRecord.strCAS)!=null) {				
				CAS=ac.getProperty(DSSToxRecord.strCAS);
				pr.setCAS(CAS);	
			}

			pr.setDTXSID(DTXSID);
			pr.setSmiles(Smiles);
			pr.setError(error);
			
			if (key.equals(DSSToxRecord.strCAS)) {
				htResults.put(CAS, pr);	
			} else if (key.equals(DSSToxRecord.strSID)) {
				htResults.put(DTXSID, pr);
			} else if (key.equals(DSSToxRecord.strSmiles)) {
				htResults.put(Smiles, pr);
			}
			
								
		} // end loop over molecules
		return htResults;
	}
	
	
	
	/**
	 * Run predictions in batch so don't have to reload model info each time 
	 * Results stored in hashtable by DTXSID
	 * 
	 * @param moleculeSet
	 * @param endpoint
	 * @param method
	 * @param createReports
	 * @param createDetailedReports
	 * @return hashtable of results with key as DTXSID and prediction results + report as TESTPredictedValue
	 * 
	 */
	public static Hashtable<String,List<PredictionResults>> runEndpoints(AtomContainerSet moleculeSet, String [] endpoints,String method,boolean createReports,boolean createDetailedReports,String key) {				

		DescriptorFactory.debug=false;
		WebTEST4.createDetailedReports=createDetailedReports;
		WebTEST4.createReports=createReports;
		WebTEST4.generateWebpages=false;
		PredictToxicityJSONCreator.forGUI=true;
		
		Hashtable<String,List<PredictionResults>>htResults=new Hashtable<>();
		

		// *******************************************************
		
		for (String endpoint:endpoints)
			WebTEST4.loadTrainingData(endpoint,method);//Note: need to use webservice approach to make this data persistent

		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			AtomContainer ac = (AtomContainer) moleculeSet.getAtomContainer(i);
			ac=calculate(ac,endpoints,method);
			List<PredictionResults> resultsArray=getResultsArray(ac,method);
			
			List<PredictionResults> resultsArray2=new ArrayList<>(); 

			String DTXSID=ac.getProperty(DSSToxRecord.strSID);
			String Smiles=ac.getProperty(DSSToxRecord.strSmiles);
			String error=(String) ac.getProperty("Error");
			String CAS=null;
			
//			System.out.println(DTXSID);
			
			for (int j=0;j<endpoints.length;j++) {
				
				PredictionResults pr=null;
				
				if (resultsArray.get(j)==null) {
					pr=new PredictionResults();
					pr.setEndpoint(endpoints[j]);
					pr.setMethod(method);	
				} else {
					pr=resultsArray.get(j);
				}
				
				if (ac.getProperty(DSSToxRecord.strCAS)!=null) {				
					CAS=ac.getProperty(DSSToxRecord.strCAS);
					pr.setCAS(CAS);	
				}
				
				pr.setDTXSID(DTXSID);
				pr.setSmiles(Smiles);
				pr.setError(error);
				resultsArray2.add(pr);
			}
			
			
			if (key.equals(DSSToxRecord.strCAS)) {
				htResults.put(CAS, resultsArray2);	
			} else if (key.equals(DSSToxRecord.strSID)) {
				htResults.put(DTXSID, resultsArray2);
			} else if (key.equals(DSSToxRecord.strSmiles)) {
				htResults.put(Smiles, resultsArray2);
			}
								
		} // end loop over molecules
		return htResults;
	}
	private static void writeReportsAsHTML_Files(Hashtable<String,PredictionResults>htResults,String endpoint,String method,String reportBase) {				
		Set<String> setOfKeys = htResults.keySet();
		for (String DTXSID : setOfKeys) {			
			PredictionResults predictionResults=htResults.get(DTXSID);			
			writeWebPage(endpoint, method, predictionResults, reportBase, DTXSID);			
		}
	}
	
	private static void writeReportsAsJsonFiles(Hashtable<String,PredictionResults>htResults,String endpoint,String method,String reportBase) {				

		Set<String> setOfKeys = htResults.keySet();
		for (String DTXSID : setOfKeys) {			
			PredictionResults predictionResults=htResults.get(DTXSID);			
			String json=getReportAsJsonString(predictionResults);//Json for report object
			writeReportJson(predictionResults,reportBase, json,DTXSID);			
			//TODO could also write to a database
		}
	}
	
	
	private static String getReportAsJsonString(PredictionResults predictionResults) {				
		return gson.toJson(predictionResults);
	}
	
	private static String getReportAsHTMLString(PredictionResults predictionResults) {				
		PredictToxicityWebPageCreatorFromJSON p=new PredictToxicityWebPageCreatorFromJSON();
		StringWriter sw=new StringWriter();	
		p.writeConsensusResultsWebPages(predictionResults, sw);		
		String htmlReport=sw.getBuffer().toString();
		return htmlReport;
	}
	

	private static PredictionResults getResults (AtomContainer ac,String method) {
		
		List<TESTPredictedValue>listTPV=ac.getProperty("listTPV");
		
		for (int j=0;j<listTPV.size();j++) {
			TESTPredictedValue tpv=listTPV.get(j);
			if (tpv.method.equals(method)) {					
				return tpv.predictionResults;
			}
		}
		return null;
		
	}
	
	
	private static List<PredictionResults> getResultsArray (AtomContainer ac,String method) {		
		List<TESTPredictedValue>listTPV=ac.getProperty("listTPV");		
		List<PredictionResults>listPR=new ArrayList<PredictionResults>();
		
		for (int j=0;j<listTPV.size();j++) {
			TESTPredictedValue tpv=listTPV.get(j);
			if (tpv.method.equals(method)) {					
				listPR.add(tpv.predictionResults);
			}
		}
		return listPR;
		
	}

	
	
	private static void writeWebPage(String endpoint, String method,PredictionResults predictionResults,String reportBase,String ID) {
		String fileNameNoExtension = WebTEST4.getResultFileNameNoExtension(endpoint, method, ID);
		String outputFileName = fileNameNoExtension + ".html";
		String outputFilePath = reportBase + File.separator + outputFileName;
		PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();
		htmlCreator.writeResultsWebPages(predictionResults, outputFilePath);		
	}
	
	private static void writeReportJson(PredictionResults pr,String reportBase,String json,String ID) {
		
		String endpoint=pr.getEndpoint();
		String method=pr.getMethod();
		
		String fileNameNoExtension = WebTEST4.getResultFileNameNoExtension(endpoint, method, ID);
		String outputFileName = fileNameNoExtension + ".json";
		String outputFilePath = reportBase + File.separator + outputFileName;

		try {			
			FileWriter fw=new FileWriter(outputFilePath);
			fw.write(json);
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
//	private static AtomContainer calculate(AtomContainer ac,String endpoint,String method) {
//		String error=(String) ac.getProperty("Error");
//		List<TESTPredictedValue>listTPV=null;
//
//		DescriptorData dd=null;
//
//		try {
//						
//			//Note: descriptor calculations for each chemical are the most time consuming. One could run calculations using the same descriptors and run all endpoints
//			dd=WebTEST4.goDescriptors(ac);
//
//			if (ac.getProperty(DSSToxRecord.strCAS)!=null)  {
//				dd.ID=ac.getProperty(DSSToxRecord.strCAS);//need to properly run nearest neighbor method to exclude test chemical by CAS	
//			} else {
//				dd.ID=ac.getProperty(DSSToxRecord.strSID);
//			}
//
//			error=(String) ac.getProperty("Error");
//
//			CalculationParameters params=new CalculationParameters();
//			params.endpoints= new String[1];
//			params.endpoints[0]=endpoint;			
//			params.methods= new String[1];
//			params.methods[0]=method;
//
//			listTPV=WebTEST4.doPredictions(ac,dd,params);
//
//		} catch (Exception ex) {
//			System.out.println("Error running "+ac.getProperty("smiles"));
//			return ac;
//		}
//
////		Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
////		System.out.println(gson.toJson(listTPV.get(0)));
//		ac.setProperty("listTPV", listTPV);
//		return ac;
//	}
	
	private static AtomContainer calculate(AtomContainer ac,String []endpoints,String method) {
		String error=(String) ac.getProperty("Error");
		List<TESTPredictedValue>listTPV=null;

		DescriptorData dd=null;

		try {
						
			//Note: descriptor calculations for each chemical are the most time consuming. One could run calculations using the same descriptors and run all endpoints
			dd=WebTEST4.goDescriptors(ac);

			if (ac.getProperty(DSSToxRecord.strCAS)!=null)  {
				dd.ID=ac.getProperty(DSSToxRecord.strCAS);//need to properly run nearest neighbor method to exclude test chemical by CAS	
			} else {
				dd.ID=ac.getProperty(DSSToxRecord.strSID);
			}

			error=(String) ac.getProperty("Error");

			CalculationParameters params=new CalculationParameters();
			params.endpoints= new String[1];
			params.endpoints=endpoints;			
			params.methods= new String[1];
			params.methods[0]=method;

			listTPV=WebTEST4.doPredictions(ac,dd,params);

		} catch (Exception ex) {
			System.out.println("Error running "+ac.getProperty("smiles"));
			return ac;
		}

//		Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
//		System.out.println(gson.toJson(listTPV.get(0)));
		ac.setProperty("listTPV", listTPV);
		return ac;
	}

	
	/**
	 * Example of how to run all endpoints at once
	 */
	private static void runSingleEndpointExample () {
		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		String reportBase="reports";//folder to store reports to harddrive

		AtomContainerSet acs=new AtomContainerSet();		
		acs.addAtomContainer(createMolecule("c1ccccc1", "DTXSID3039242","71-43-2"));//valid simple molecule
		acs.addAtomContainer(createMolecule("O=P(OC1=CC=CC=C1)(OC1=CC=CC=C1)OC1=CC=CC=C1","DTXSID1021952","115-86-6"));//valid but more complicated molecule
		acs.addAtomContainer(createMolecule("Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1", "DTXSID20211176","62119-86-2"));//invalid molecule: salt
//		acs.addAtomContainer(createMolecule("O=C6c4ccc5c1ccc3c2c1c(ccc2C(=O)N(c2ccc(\\N=N/c1ccccc1)cc2)C3=O)c1ccc(c4c51)C(=O)N6c2ccc(\\N=N/c1ccccc1)cc2", "DTXSID1051983","3049-71-6"));//very complicated aromatic molecule, takes longer to run
		acs.addAtomContainer(createMolecule("XXXX", "DTXSID2","123-45-6"));//bad smiles (cant convert to structure)
		acs.addAtomContainer(createMolecule("[S]", "DTXSID9034941","7704-34-9"));//has no carbon
				
		Hashtable<String,PredictionResults>htResults=runEndpoint(acs, endpoint, method,createReports,createDetailedReports,DSSToxRecord.strSID);		
				
		//Get DTXSID of a chemical:
		String DTXSID=acs.getAtomContainer(0).getProperty(DSSToxRecord.strSID);
		
		//Get report for one of the above chemicals as Json string:
		PredictionResults predictionResults=htResults.get(DTXSID);//can also iterate over hashtable to get all the reports		
		String json=getReportAsJsonString(predictionResults);
		System.out.println(json);
		
		//Get single report as HTML string:
		String htmlReport=getReportAsHTMLString(predictionResults);
		System.out.println(htmlReport);
		
//		//Write reports for all chemicals to harddrive:
//		writeReportsAsJsonFiles(htResults, endpoint, method, reportBase);
//		writeReportsAsHTML_Files(htResults, endpoint, method, reportBase);
		
	}
	
	/**
	 * Example of how to run all endpoints at once
	 */
	private static void runAllEndpointsExample () {

		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		
		AtomContainerSet acs=new AtomContainerSet();		
		acs.addAtomContainer(createMolecule("c1ccccc1", "DTXSID3039242","71-43-2"));//valid simple molecule
//		acs.addAtomContainer(createMolecule("O=P(OC1=CC=CC=C1)(OC1=CC=CC=C1)OC1=CC=CC=C1","DTXSID1021952","115-86-6"));//valid but more complicated molecule
//		acs.addAtomContainer(createMolecule("Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1", "DTXSID20211176","62119-86-2"));//invalid molecule: salt
//		acs.addAtomContainer(createMolecule("O=C6c4ccc5c1ccc3c2c1c(ccc2C(=O)N(c2ccc(\\N=N/c1ccccc1)cc2)C3=O)c1ccc(c4c51)C(=O)N6c2ccc(\\N=N/c1ccccc1)cc2", "DTXSID1051983","3049-71-6"));//very complicated aromatic molecule, takes longer to run
//		acs.addAtomContainer(createMolecule("XXXX", "DTXSID2","123-45-6"));//bad smiles (cant convert to structure)
//		acs.addAtomContainer(createMolecule("[S]", "DTXSID9034941","7704-34-9"));//has no carbon
		
		//Run all endpoints at once:
//		String [] endpoints= {TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50};
		String [] endpoints= {TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50,
				TESTConstants.ChoiceTP_IGC50,TESTConstants.ChoiceRat_LD50,
				TESTConstants.ChoiceBCF,TESTConstants.ChoiceReproTox,
				TESTConstants.ChoiceMutagenicity,TESTConstants.ChoiceEstrogenReceptor,
				TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
				TESTConstants.ChoiceBoilingPoint,TESTConstants.ChoiceMeltingPoint,
				TESTConstants.ChoiceFlashPoint,TESTConstants.ChoiceVaporPressure,
				TESTConstants.ChoiceDensity,TESTConstants.ChoiceSurfaceTension,
				TESTConstants.ChoiceThermalConductivity,TESTConstants.ChoiceViscosity,
				TESTConstants.ChoiceWaterSolubility};
		
		Hashtable<String,List<PredictionResults>>htResultsAll=
		runEndpoints(acs, endpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);

		//Get DTXSID of a chemical:
		String DTXSID=acs.getAtomContainer(0).getProperty(DSSToxRecord.strSID);
		
		//Get all results for a given chemical:
		List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);		
				
		for (PredictionResults pr:listPredictionResults) {
			String json=getReportAsJsonString(pr);
			System.out.println(json);
		}
	}
	public static void main(String[] args) {
		runSingleEndpointExample();
//		runAllEndpointsExample();
	}

}
