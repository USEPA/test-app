package ToxPredictor.Application.Calculations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.TESTPredictedValue;



public class RunFromSmiles {

	public static final String strSID="DTXSID";
	public static final String strCID="DTXCID";
	private static final String strCAS = "CASRN";

	static String [] allEndpoints= {TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50,
			TESTConstants.ChoiceTP_IGC50,TESTConstants.ChoiceRat_LD50,
			TESTConstants.ChoiceBCF,TESTConstants.ChoiceReproTox,
			TESTConstants.ChoiceMutagenicity,TESTConstants.ChoiceEstrogenReceptor,
			TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
			TESTConstants.ChoiceBoilingPoint,TESTConstants.ChoiceMeltingPoint,
			TESTConstants.ChoiceFlashPoint,TESTConstants.ChoiceVaporPressure,
			TESTConstants.ChoiceDensity,TESTConstants.ChoiceSurfaceTension,
			TESTConstants.ChoiceThermalConductivity,TESTConstants.ChoiceViscosity,
			TESTConstants.ChoiceWaterSolubility};

	//	private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	private static Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();


	public static AtomContainer createMolecule(String smiles,String DTXSID, String CAS) {
		return createMolecule(smiles,DTXSID,null, CAS);
	}

	public static AtomContainer createMolecule(String smiles,String DTXSID,String DTXCID, String CAS) {
		AtomContainer m = WebTEST4.prepareSmilesMolecule(smiles);
		m.setProperty(DSSToxRecord.strSID,DTXSID);//store sid so dont need to look up later
		m.setProperty(DSSToxRecord.strCID,DTXCID);//store sid so dont need to look up later
		m.setProperty(DSSToxRecord.strSmiles, smiles);//need original smiles NOT QSAR ready smiles
		if(CAS!=null) m.setProperty(DSSToxRecord.strCAS, CAS);//need CAS for doing nearest neighbor method (to exclude training chemical by CAS)
		return m;
	}


	public static void runSingle (String endpoint,String method, boolean createReports,
			boolean createDetailedReports,String reportBase,String smiles,String DTXSID,String CAS) {

		AtomContainerSet acs=new AtomContainerSet();		
		acs.addAtomContainer(RunFromSmiles.createMolecule(smiles,DTXSID,CAS));//valid simple molecule

		Hashtable<String,PredictionResults>htResults=RunFromSmiles.runEndpoint(acs, endpoint, method,createReports,createDetailedReports,DSSToxRecord.strSID);		

		//Get report for one of the above chemicals as Json string:
		PredictionResults predictionResults=htResults.get(DTXSID);//can also iterate over hashtable to get all the reports		
		String json=RunFromSmiles.getReportAsJsonString(predictionResults);
		System.out.println(json);

		//Get single report as HTML string:
		String htmlReport=RunFromSmiles.getReportAsHTMLString(predictionResults);
		System.out.println(htmlReport);

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

			String DTXSID=null;
			if (ac.getProperty(strSID)!=null) DTXSID=ac.getProperty(strSID);
			else if (ac.getProperty(DSSToxRecord.strSID)!=null) DTXSID=ac.getProperty(DSSToxRecord.strSID);
			ac.setProperty(DSSToxRecord.strSID, DTXSID);
			
			String DTXCID=null;
			if (ac.getProperty(strCID)!=null) DTXCID=ac.getProperty(strCID);
			else if (ac.getProperty(DSSToxRecord.strCID)!=null) DTXCID=ac.getProperty(DSSToxRecord.strCID);
			ac.setProperty(DSSToxRecord.strCID, DTXCID);
			
			String CAS=null;
			if (ac.getProperty(strCAS)!=null)	CAS=ac.getProperty(strCAS);
			else if (ac.getProperty(DSSToxRecord.strCAS)!=null)	CAS=ac.getProperty(DSSToxRecord.strCAS);
			ac.setProperty(DSSToxRecord.strCAS, CAS);
			

			String Smiles=ac.getProperty(DSSToxRecord.strSmiles);
			String error=(String) ac.getProperty("Error");

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


				pr.setCAS(CAS);	
				pr.setDTXSID(DTXSID);
				pr.setDTXCID(DTXCID);
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
	public static List<PredictionResults> runEndpointsAsList(AtomContainerSet moleculeSet, String [] endpoints,String method,boolean createReports,boolean createDetailedReports,String key) {				

		DescriptorFactory.debug=false;
		WebTEST4.createDetailedReports=createDetailedReports;
		WebTEST4.createReports=createReports;
		WebTEST4.generateWebpages=false;
		PredictToxicityJSONCreator.forGUI=true;

		List<PredictionResults>Results=new ArrayList<>();


		// *******************************************************

		for (String endpoint:endpoints)
			WebTEST4.loadTrainingData(endpoint,method);//Note: need to use webservice approach to make this data persistent

		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			AtomContainer ac = (AtomContainer) moleculeSet.getAtomContainer(i);

			String DTXSID=null;
			if (ac.getProperty(strSID)!=null) DTXSID=ac.getProperty(strSID);
			else if (ac.getProperty(DSSToxRecord.strSID)!=null) DTXSID=ac.getProperty(DSSToxRecord.strSID);
			ac.setProperty(DSSToxRecord.strSID, DTXSID);
			
			String DTXCID=null;
			if (ac.getProperty(strCID)!=null) DTXCID=ac.getProperty(strCID);
			else if (ac.getProperty(DSSToxRecord.strCID)!=null) DTXCID=ac.getProperty(DSSToxRecord.strCID);
			ac.setProperty(DSSToxRecord.strCID, DTXCID);
			
			System.out.println(ac.getProperty(DSSToxRecord.strSmiles)+"");
			
			ac=calculate(ac,endpoints,method);
			List<PredictionResults> resultsArray=getResultsArray(ac,method);

			
			String CAS=null;
			if (ac.getProperty(strCAS)!=null)	CAS=ac.getProperty(strCAS);
			else if (ac.getProperty(DSSToxRecord.strCAS)!=null)	CAS=ac.getProperty(DSSToxRecord.strCAS);
			ac.setProperty(DSSToxRecord.strCAS, CAS);
			

			String Smiles=ac.getProperty(DSSToxRecord.strSmiles);
			String error=(String) ac.getProperty("Error");

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

				pr.setVersion(TESTConstants.SoftwareVersion);
				pr.setCAS(CAS);	
				pr.setDTXSID(DTXSID);
				pr.setDTXCID(DTXCID);
				pr.setSmiles(Smiles);
				pr.setError(error);
				Results.add(pr);
			}

		} // end loop over molecules
		return Results;
	}
	private static void writeReportsAsHTML_Files(Hashtable<String,PredictionResults>htResults,String reportBase) {				
		Set<String> setOfKeys = htResults.keySet();
		for (String DTXSID : setOfKeys) {			
			PredictionResults predictionResults=htResults.get(DTXSID);			
			writeWebPage(predictionResults, reportBase, DTXSID);			
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


	public static String getReportAsJsonString(PredictionResults predictionResults) {				
		return gson.toJson(predictionResults);
	}

	public static String getReportAsHTMLString(PredictionResults predictionResults) {				
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



	private static void writeWebPage(PredictionResults predictionResults,String reportBase,String ID) {
		String fileNameNoExtension = WebTEST4.getResultFileNameNoExtension(predictionResults.getEndpoint(), predictionResults.getMethod(), ID);
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
			} else if (ac.getProperty(DSSToxRecord.strSID)!=null){
				dd.ID=ac.getProperty(DSSToxRecord.strSID);
			} else if (ac.getProperty(DSSToxRecord.strCID)!=null){
				dd.ID=ac.getProperty(DSSToxRecord.strCID);
			} else {
				System.out.println("Cant set ID for DescriptorData: no CAS,SID,or CID");
			}

			error=(String) ac.getProperty("Error");

			CalculationParameters params=new CalculationParameters();
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
		acs.addAtomContainer(createMolecule("O=P(OC1=CC=CC=C1)(OC1=CC=CC=C1)OC1=CC=CC=C1","DTXSID1021952","115-86-6"));//valid but more complicated molecule
		acs.addAtomContainer(createMolecule("Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1", "DTXSID20211176","62119-86-2"));//invalid molecule: salt
		acs.addAtomContainer(createMolecule("O=C6c4ccc5c1ccc3c2c1c(ccc2C(=O)N(c2ccc(\\N=N/c1ccccc1)cc2)C3=O)c1ccc(c4c51)C(=O)N6c2ccc(\\N=N/c1ccccc1)cc2", "DTXSID1051983","3049-71-6"));//very complicated aromatic molecule, takes longer to run
		acs.addAtomContainer(createMolecule("XXXX", "DTXSID2","123-45-6"));//bad smiles (cant convert to structure)
		acs.addAtomContainer(createMolecule("[S]", "DTXSID9034941","7704-34-9"));//has no carbon

		//Run all endpoints at once:
		//		String [] endpoints= {TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50};


		Hashtable<String,List<PredictionResults>>htResultsAll=
				runEndpoints(acs, allEndpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);

		//Get DTXSID of a chemical:
		//Get all results for a given chemical:
		//		List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);		

		//		for (PredictionResults pr:listPredictionResults) {
		//			String json=getReportAsJsonString(pr);
		//			System.out.println(json);
		//		}


		try {

			FileWriter fw= new FileWriter("reports/sample_predictions.json");
			fw.write(gson.toJson(htResultsAll));
			fw.flush();
			fw.close();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * Writing my own V3000 reader because CDK sucks and cant read SDFs for all the dashboard chemicals and get the properties too
	 * 
	 * @param sdfFilePath
	 * @return
	 */
	public static AtomContainerSet readSDFV3000(String sdfFilePath) {

		AtomContainerSet acs = new AtomContainerSet();

		MDLV3000Reader mr=new MDLV3000Reader();

		SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		
		try {

			FileInputStream fis=new FileInputStream(sdfFilePath);
			BufferedReader br=new BufferedReader (new InputStreamReader(fis, "UTF-8"));

			boolean stop=false;

			while (true) {

				String strStructure="";

				while (true) {
					String Line=br.readLine();

					if(Line==null) {
						stop=true;
						break;
					}

					//				System.out.println(Line);
					strStructure+=Line+"\r\n";
					if(Line.contains("M  END"))break;
				}

				if(stop)break;


				InputStream stream = new ByteArrayInputStream(strStructure.getBytes());
				mr.setReader(stream);

				IAtomContainer molecule=null;
				
				try {
					molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
				} catch (Exception ex) {
					molecule=new AtomContainer();
				}

				while (true) {
					String Line=br.readLine();
					//				System.out.println(Line);

					if(Line.contains(">  <")) {
						String field=Line.substring(Line.indexOf("<")+1,Line.length()-1);
						String value=br.readLine();
						molecule.setProperty(field, value);
						//					System.out.println(field);
					}

					if(Line.contains("$$$"))break;
				}

				if(molecule.getAtomCount()==0) {
				
					AtomContainer molecule2=null;
					
					String smiles=molecule.getProperty("smiles");
					
					if (smiles!=null) {
						try {
							molecule2 = (AtomContainer)sp.parseSmiles(smiles);
//						System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
						} catch (Exception ex) {
							molecule2=new AtomContainer();
						}
						
					}else {
						molecule2 = new AtomContainer();
					}							
						
					molecule2.setProperties(molecule.getProperties());
					acs.addAtomContainer(molecule2);
					
				} else {
					acs.addAtomContainer(molecule);
				}
						
			}
			
			br.close();
			mr.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return acs;
	}



	static void runSDF_all_endpoints(String SDFFilePath, String destJsonPath,boolean skipMissingSID,int maxCount) {

		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		AtomContainerSet acs=readSDFV3000(SDFFilePath);
		AtomContainerSet acs2 = filterAtomContainerSet(acs, skipMissingSID, maxCount);
		
		System.out.println(acs2.getAtomContainerCount());

//		Hashtable<String,List<PredictionResults>>htResultsAll=
//				runEndpoints(acs2, allEndpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);

		List<PredictionResults>results=runEndpointsAsList(acs2, allEndpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);
		
		saveJson(destJsonPath, results);
		
//		PredictionResults predictionResults=results.get(0);
//		String reportBase="reports";
//		writeWebPage(predictionResults, reportBase, predictionResults.getDTXCID());	
		
	}
	
	static void runSDF(String SDFFilePath, String endpoint,String destJsonPath,boolean skipMissingSID,int maxCount) {

		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		AtomContainerSet acs=readSDFV3000(SDFFilePath);
		
		
		AtomContainerSet acs2 = filterAtomContainerSet(acs, skipMissingSID, maxCount);
		
		acs=null;
		System.out.println(acs2.getAtomContainerCount());

//		Hashtable<String,List<PredictionResults>>htResultsAll=
//				runEndpoints(acs2, allEndpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);

		String [] endpoints= {endpoint};
		
		List<PredictionResults>results=runEndpointsAsList(acs2, endpoints, method,createReports,createDetailedReports,DSSToxRecord.strSID);
		
		saveJson(destJsonPath, results);

		//Write webpages to look at them
//		for (PredictionResults predictionResults:results) {
//			String reportBase="reports";
//			writeWebPage(predictionResults, reportBase, predictionResults.getDTXCID());	
//		}


	}

	private static void saveJson(String destJsonPath, Object obj) {
		try {
			
			File file=new File(destJsonPath);
			file.getParentFile().mkdirs();
			
			FileWriter fw= new FileWriter(destJsonPath);
			fw.write(gson.toJson(obj));
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static AtomContainerSet filterAtomContainerSet(AtomContainerSet acs, boolean skipMissingSID, int maxCount) {
		AtomContainerSet acs2=new AtomContainerSet();
		
		Iterator<IAtomContainer> iterator= acs.atomContainers().iterator();
		
		int count=0;
		
		while (iterator.hasNext()) {
			AtomContainer ac=(AtomContainer) iterator.next();
			String SID=ac.getProperty("DTXSID");
			if(skipMissingSID && SID==null) continue;
			acs2.addAtomContainer(ac);
			count++;
//			System.out.println(ac.getProperty("DTXSID")+"\t"+ac.getProperty("smiles"));
			
			WebTEST4.checkAtomContainer(ac);//theoretically the webservice has its own checking
			
			if(count==maxCount)break;
		}
		return acs2;
	}



	public static String runFromAPI(String smiles,String endpointAbbreviation) {

		String address="https://comptox.epa.gov/dashboard/web-test/";
		HttpResponse<String> response;
		try {
			response = Unirest.get(address+"/"+endpointAbbreviation)
					.queryString("smiles", smiles)					
					.asString();

			return response.getBody();

		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}

	}

	static class PredictionDashboard {
		String source;
		String property_name;
		String method;


		String dtxsid;
		String smiles;
		Double prediction_value;
		String prediction_units;
		String prediction_string;
		String prediction_error;

		static String [] fields= {"source","property_name","method","dtxsid","prediction_value","prediction_units","prediction_string","prediction_error","smiles"};

		//		static String getHeader(String del) {
		//			return "source"+del+"property_name"+del+"method"+del+"dtxsid"+del+"prediction_value"+del+"prediction_units"+del+"prediction_string"+del+"prediction_error"+del+"smiles";
		//		}

		//		String toString(String del) {
		//			return source+del+property_name+del+method+del+dtxsid+del+prediction_value+del+prediction_units+del+prediction_string+del+prediction_error+del+smiles;
		//		}


		public String toString(String del,String []fieldNames) {
			String result="";



			for (int i=0;i<fieldNames.length;i++) {
				String fieldName=fieldNames[i];
				try {
					Field myField = this.getClass().getDeclaredField(fieldName);
					String type=myField.getType().getName();
					//					System.out.println(fieldName);
					String strValue=null;

					switch (type) {

					case "java.lang.String":
						if (myField.get(this)==null) strValue="";	
						else strValue=myField.get(this)+"";						
						break;

					case "java.lang.Double":
						if (myField.get(this)==null) strValue="";	
						else {
							strValue=(Double)myField.get(this)+"";						
						}										
						break;

					case "java.lang.Integer":
					case "java.lang.Boolean": 							
						if (myField.get(this)==null) strValue="";	
						else strValue=myField.get(this)+"";						
						break;					
					case "boolean":
						strValue=myField.getBoolean(this)+"";
						break;
					case "int":
						strValue=myField.getInt(this)+"";
						break;
					case "double": 
						strValue=myField.getDouble(this)+"";

					}

					if (strValue!=null) {
						if (strValue.contains(del)) strValue="\""+strValue+"\"";					
					} else {
						strValue="";
					}

					result+=strValue;				

					if (i<fieldNames.length-1) result+=del;					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return result;

		}
		public static String getHeaderString(String del,String []fieldNames) {		
			String result="";		
			for (int i=0;i<fieldNames.length;i++) {
				if (fieldNames[i].contains(del)) fieldNames[i]="\""+fieldNames[i]+"\"";			
				result+=fieldNames[i];								
				if (i<fieldNames.length-1) result+=del;
			}
			return result;
		}


	}



	static PredictionDashboard convertResultsToPredictionFormat(PredictionResults pr) {

		PredictionDashboard pd=new PredictionDashboard();


		try {

			PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();

			try {

				pd.source="TEST v"+TESTConstants.SoftwareVersion;

				pd.property_name=pr.getEndpoint().replace(" at 25°C","");
				pd.method=pr.getMethod();


				pd.smiles=pr.getSmiles();
				pd.dtxsid=pr.getDTXSID();


				if (pr.getError()!=null && !pr.getError().isBlank()) {
					pd.prediction_error=pr.getError();
				} else {
					if (pr.isBinaryEndpoint()) {
						if (pt.getPredToxValue().equals("N/A")) {
							pd.prediction_error=pt.getMessage();
						} else {
							pd.prediction_value=Double.parseDouble(pt.getPredToxValue());
							pd.prediction_units="Binary";
							pd.prediction_string=pt.getPredValueEndpoint();
						}

					} else if (pr.isLogMolarEndpoint()) {

						if (pt.getPredToxValue().equals("N/A")) {
							pd.prediction_error=pt.getMessage();
						} else {
							if (pd.property_name.equals("Fathead minnow LC50 (96 hr)") || pd.property_name.equals("Daphnia magna LC50 (48 hr)") || pd.property_name.equals("T. pyriformis IGC50 (48 hr)") || pd.property_name.equals("Water solubility at 25°C")) {								
								pd.prediction_value=Math.pow(10.0,-Double.parseDouble(pt.getPredToxValue()));
								pd.prediction_units="M";
							} else if (pd.property_name.equals("Oral rat LD50")) {
								pd.prediction_value=Math.pow(10.0,-Double.parseDouble(pt.getPredToxValue()));
								pd.prediction_units="mol/kg";
							} else if (pd.property_name.equals("Bioconcentration factor")) {
								pd.prediction_value=Math.pow(10.0,Double.parseDouble(pt.getPredToxValue()));
								pd.prediction_units="L/kg";								
							} else if (pd.property_name.contains("Vapor pressure")) {
								pd.prediction_value=Math.pow(10.0,Double.parseDouble(pt.getPredToxValue()));
								pd.prediction_units="mmHg";								
							} else if (pd.property_name.contains("Viscosity")) {
								pd.prediction_value=Math.pow(10.0,Double.parseDouble(pt.getPredToxValue()));
								pd.prediction_units="cP";								
							} else if (pd.property_name.equals("Estrogen Receptor RBA")) {
								pd.prediction_value=Math.pow(10.0,Double.parseDouble(pt.getPredToxValue()));
								pd.prediction_units="Dimensionless";
							}
						}
					} else {

						if (pt.getPredToxValMass().equals("N/A")) {
							pd.prediction_error=pt.getMessage();
						} else {

							pd.prediction_value=Double.parseDouble(pt.getPredToxValMass());

							if (pt.getMassUnits().equals("g/cm³")) {
								pd.prediction_units="g/cm3";
							} else if (pt.getMassUnits().equals("°C")) {
								pd.prediction_units="C";
							} else {
								pd.prediction_units=pt.getMassUnits();	
							}
							//
						}
					}
				}

				if (pd.prediction_error!=null) {
					if (pd.prediction_error.equals("No prediction could be made")) {
						pd.prediction_error="No prediction could be made due to applicability domain violation";
					} else if (pd.prediction_error.contains("could not parse")) {
						pd.prediction_error="Could not parse smiles";	
					}
				}

				//					System.out.println(gson.toJson(pd));
			} catch (Exception ex) {
				//					System.out.println(gson.toJson(pr));
				ex.printStackTrace();
			}




		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return pd;
	}

	static void runFromSampleJsonFile() {

		Type listOfMyClassObject = new TypeToken<Hashtable<String,List<PredictionResults>>>() {}.getType();

		try {
			Hashtable<String,List<PredictionResults>>htResultsAll=gson.fromJson(new FileReader("reports/sample_predictions.json"), listOfMyClassObject);

			System.out.println(PredictionDashboard.getHeaderString("|",PredictionDashboard.fields));

			for (String DTXSID:htResultsAll.keySet()) {

				//				if (DTXSID.equals("DTXSID9034941")) continue;
				//				if (DTXSID.equals("DTXSID20211176")) continue;

				List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);

				for (PredictionResults pr:listPredictionResults) {
					PredictionDashboard pd=convertResultsToPredictionFormat(pr);
					System.out.println(pd.toString("|",PredictionDashboard.fields));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 

	}

	public static void main(String[] args) {
		//		runSingleEndpointExample();

		//		String json=runFromAPI("CCCO", TESTConstants.abbrevChoiceFHM_LC50);
		//		System.out.println(json);


		//		runAllEndpointsExample();
		//		runFromSampleJsonFile();
		
		
		String folderSrc="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\hibernate_qsar_model_building\\data\\dsstox\\sdf\\";
		String fileNameSDF="snapshot_compounds1.sdf";
		String filepathSDF=folderSrc+fileNameSDF;

		int maxCount=10;//set to -1 to run all in sdf
		boolean skipMissingSID=true;
		String destJsonPath="reports/TEST_results_all_endpoints_"+fileNameSDF.replace(".sdf", ".json");
		runSDF_all_endpoints(filepathSDF,destJsonPath,skipMissingSID,maxCount);

//		String endpoint=TESTConstants.ChoiceFHM_LC50;
//		String destJsonPath="reports/TEST_results_"+endpoint+"_"+fileNameSDF.replace(".sdf", ".json");
//		runSDF(filepathSDF,endpoint,destJsonPath,skipMissingSID,10);

		
	}

}
