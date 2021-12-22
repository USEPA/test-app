package ToxPredictor.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.graph.ConnectivityChecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import AADashboard.Application.GenerateRecordsFromTEST;
import AADashboard.Application.MySQL_DB;
import QSAR.qsarOptimal.AllResults;
import QSAR.validation2.AllResultsXMLReader;
import QSAR.validation2.InstanceUtilities;
import ToxPredictor.Application.Calculations.PredictToxicityHierarchical;
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Application.Calculations.PredictToxicityLDA;
import ToxPredictor.Application.Calculations.PredictToxicityNearestNeighbor;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreator;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.TaskCalculations;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.ChemistryDashboardRecord;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.FileUtils;
import ToxPredictor.Utilities.FormatUtils;
import ToxPredictor.Utilities.HtmlUtils;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.StringCompression;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.MolFileUtilities;
import gov.epa.api.TESTRecord;
import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;

public class WebTEST2 {

	private static final Logger logger = LogManager.getLogger(WebTEST2.class);

	public static String reportFolderName = "web-reports";

	ChemicalFinder cf = new ChemicalFinder();

	static Hashtable<String, Instances> ht_ccTraining = new Hashtable<String, Instances>();
	static Hashtable<String, Instances> ht_ccTrainingFrag = new Hashtable<String, Instances>();
	static Hashtable<String, Instances> ht_ccPrediction = new Hashtable<String, Instances>();
	static Hashtable<String, AllResults> ht_allResults = new Hashtable<String, AllResults>();
	static Hashtable<String, AllResults> ht_allResultsFrag = new Hashtable<String, AllResults>();

	///////////////////////////////////////////////////////
	static java.util.Vector<String> vecMOA = new Vector<String>();
	static Hashtable<String, AllResults> htAllResultsMOA = new Hashtable<String, AllResults>();
	static Hashtable<String, Instances> htTrainingSetsMOA = new Hashtable<String, Instances>();

	static Hashtable<String, AllResults> htAllResultsLC50 = new Hashtable<String, AllResults>();
	static Hashtable<String, Instances> htTrainingSetsLC50 = new Hashtable<String, Instances>();
	///////////////////////////////////////////////////////

	private static long totalDescriptorCalculationTime = 0;
	private static long totalPredictionGenerationTime = 0;
	private static long totalReportGenerationTime = 0;

	int classIndex = 1;
	int chemicalNameIndex = 0;
	static final String DescriptorSet = "2d";
	public static int minPredCount = 2;// minimum number of predictions needed
	// for consensus pred
	static String del = "\t";
	static boolean useFragmentsConstraint = true;

	static boolean overwriteFiles = false;// dont overwrite any files so dont get conflict from parallel runs
	public static boolean createDetailedConsensusReport = false;// create files for each method and detailed descriptor files
	public static boolean deleteExtraFiles=true;
	public static boolean usePreviousDescriptors=false;//if true will use previously calculated descriptors json file to speed up calculations

	final static long minFileAgeHours = 1000000000;// never overwrite

	public static boolean displayLoadingMessage=false;
	
	///////////////////////////////////////////////////////

	static InstanceUtilities iu = new InstanceUtilities();
	static MolFileUtilities mfu = new MolFileUtilities();

	//****************************************************************************************************
	//Paths for databases:
	public static final String DB_Path_NCCT_ID_Records = "databases/NCCT_ID.db";
	public static Statement statNCCT_ID_Records = MySQL_DB.getStatement(DB_Path_NCCT_ID_Records);

	public static String DB_Path_TEST_Predictions="databases/TEST_Predictions.db";
	//Note: currently databases/test_results.db has TEST results for 700K chemicals but doesn't have inchikey, MW, and smiles
	static int outputDatabaseFormat=2;//1 = tsv format, 2 = csv format
	
	public static final String strInChi="InChi";
	public static final String strInChiKey="InChiKey";
	public static final String strCAS="CAS";
	
//	public static String searchKey=strInChi;
	public static String searchKey=strInChiKey;
	
	public static boolean compressDescriptorsInDB=false;//should we zip the descriptors json string in the database	
	public static String DB_Path_Descriptors="databases/descriptors.db";


	private static void addMethodToVecTPV(String endpoint, Vector<TESTPredictedValue> vecTPV, String CAS, TESTRecord tr, String method,
			String strPredToxVal) {
		String strExpToxVal=tr.ExpToxValue;
		String strMW=tr.MolecularWeight;


		double expToxVal,predToxVal,MW;

		
		if (strPredToxVal==null || strPredToxVal.equals("N/A")) predToxVal=-9999;
		else predToxVal=Double.parseDouble(strPredToxVal);
				
		if (strExpToxVal==null || strExpToxVal.equals("N/A")) expToxVal=-9999;
		else expToxVal=Double.parseDouble(strExpToxVal);
		

		if (strMW==null || strMW.equals("N/A")) MW=-9999;
		else MW=Double.parseDouble(strMW);

		TESTPredictedValue v=null;

		if (!TESTConstants.isBinary(endpoint)) {
			v = getTESTPredictedValue(endpoint, method, CAS, expToxVal, predToxVal, MW);
		} else {
			v = getTESTPredictedValueBinary(endpoint, method, CAS, expToxVal, predToxVal, MW);
		}

		vecTPV.add(v);
	}


	static TESTPredictedValue getTESTPredictedValueBinary(String endpoint, String method, String CAS, double ExpToxVal, double PredToxVal, double MW) {

		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
		try {

			if (ExpToxVal == -9999) {
			} else {
				v.expValLogMolar = ExpToxVal;
			}
			if (PredToxVal == -9999) {
			} else {
				v.predValLogMolar = PredToxVal;
			}


			if (ExpToxVal == -9999) {
			} else {
				if (ExpToxVal < 0.5) {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						v.message = "Developmental NON-toxicant";
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						v.message = "Mutagenicity Negative";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						v.message = "Does NOT bind to estrogen receptor";
					}
					v.expActive = false;
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						v.message = "Developmental toxicant";
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						v.message = "Mutagenicity Positive";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						v.message = "Binds to estrogen receptor";
					}
					v.expActive = true;
				}
			}

			if (PredToxVal == -9999) {
			} else {
				if (PredToxVal < 0.5) {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						v.message = "Developmental NON-toxicant";
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						v.message = "Mutagenicity Negative";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						v.message = "Does NOT bind to estrogen receptor";
					}
					v.predActive = false;
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						v.message = "Developmental toxicant";
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						v.message = "Mutagenicity Positive";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						v.message = "Binds to estrogen receptor";
					}
					v.predActive = true;
				}
			}

			// write error:
			//			v.error = error;


		} catch (Exception ex) {
			logger.catching(ex);
		}

		return v;
	}


	static TESTPredictedValue getTESTPredictedValue(String endpoint, String method, String CAS, double ExpToxVal, double PredToxVal, double MW) {

		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
		if (Math.abs(ExpToxVal - (-9999)) > 0.001)
			v.expValLogMolar = ExpToxVal;
		if (Math.abs(PredToxVal - (-9999)) > 0.001)
			v.predValLogMolar = PredToxVal;

		try {
			double ExpToxValMass = -9999;
			double PredToxValMass = -9999;
			if (TESTConstants.isLogMolar(endpoint)) {
				if (PredToxVal != -9999) {
					PredToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, PredToxVal, MW);
					v.predValMass = PredToxValMass;
				}

				if (ExpToxVal != -9999) {
					ExpToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, ExpToxVal, MW);
					v.expValMass = ExpToxValMass;
				}
			} else {
				PredToxValMass = PredToxVal;
				v.predValMass = PredToxValMass;

				ExpToxValMass = ExpToxVal;
				v.expValMass = ExpToxValMass;
			}

		
		} catch (Exception ex) {
			logger.catching(ex);
		}

		return v;
	}

	

	

	

	private static Vector<TESTPredictedValue> calculate(int molNum, int molCount, 
			AtomContainer m, String endpoint, Vector<String>vecMethods, 
			int index, Set<WebReportType> reportTypes, DescriptorFactory df, DescriptorData dd,Connection conn, long descriptorCalculationTime) {

		

//		System.out.println("****enter calculate");

		Vector<TESTPredictedValue>vecTPV=new Vector<>();

		// PredictToxicityFDA ptFDA = new PredictToxicityFDA();
		PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
		PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
		// PredictToxicityRandomForrestCaesar ptRFC= new
		// PredictToxicityRandomForrestCaesar();
		PredictToxicityLDA ptLDA = new PredictToxicityLDA();

		long start = System.currentTimeMillis();

		boolean isBinaryEndpoint = TESTConstants.isBinary(endpoint);
		boolean isLogMolarEndpoint = TESTConstants.isLogMolar(endpoint);
		String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
		String CAS = dd.ID;
		String dtxcid=dd.dtxcid;
		
		//*******************************************************************************************************
		//Look up record in database, if found, reconstruct objects and return that

		TESTRecord tr=null;

		if (conn!=null) {

//			long startDBCall=System.currentTimeMillis();

			Statement stat=null;
			try {
				stat=conn.createStatement();
			} catch (SQLException sqlerror) {
				logger.error(sqlerror.getMessage());
			}

			if (outputDatabaseFormat==1) {
				Vector<TESTPredictedValue>records=convertRecordsFormat1(stat, abbrev, "CAS", CAS);
				if (records.size()>0) return records;
			} else if (outputDatabaseFormat==2) {
				
				String searchString="";
				if (searchKey.equals(strInChi)) searchString=dd.InChi;
				else if (searchKey.equals(strInChiKey)) searchString=dd.InChiKey;
				else if (searchKey.equals(strCAS)) searchString=dd.ID;
				
				tr=GenerateRecordsFromTEST.getTESTRecord(stat, abbrev, searchKey, searchString);

				
//				long dbretrievetime=System.currentTimeMillis()-startDBCall;
				if (tr!=null) {
					Vector<TESTPredictedValue>records=convertRecordsFormat2(tr,endpoint);
					
//					System.out.println("****leave calculate");
					
//					logger.info("loaded TEST prediction records from database:\t"+abbrev+"\t"+index+"\t"+CAS);
					if (records.size()>0) return records;
				}
			}
		}
		


		//*******************************************************************************************************

		if (tr==null) {
			tr = createTESTRecord(m, dd, CAS);
			
		}

		// ******************************************************************

		String strOutputFolder = null;
		String strOutputFolderStructureData = null;
		//
		if (!reportTypes.isEmpty()) {
			//			System.out.println(reportFolderName);

			File fileOutputFolder = new File(reportFolderName);
			String strOutputFolder1 = fileOutputFolder.getAbsolutePath() + File.separator + "ToxRuns";
			File fileOutputFolder1 = new File(strOutputFolder1);
			if (!fileOutputFolder1.exists())
				fileOutputFolder1.mkdirs();

			String strOutputFolder2 = strOutputFolder1 + File.separator + "ToxRun_" + dd.ID;
			File fileOutputFolder2 = new File(strOutputFolder2);
			if (!fileOutputFolder2.exists())
				fileOutputFolder2.mkdirs();

			String strOutputFolder3 = strOutputFolder2 + File.separator + endpoint;
			File fileOutputFolder3 = new File(strOutputFolder3);

			strOutputFolder = strOutputFolder3;

			if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				if (!fileOutputFolder3.exists())
					fileOutputFolder3.mkdirs();
			}

			strOutputFolderStructureData = strOutputFolder2 + File.separator + "StructureData";
			File osdf = new File(strOutputFolderStructureData);
			if (!osdf.exists())
				osdf.mkdirs();

		}

		if (!"OK".equals(dd.Error) && !dd.Error.isEmpty()) {
			// done=true; // commented out: dont kill rest of run for batch
			logger.error("Error calculating descriptors for {}", dd.ID);

			// TODO- create web report for this chemical that mentions this error
			
			if (conn!=null) 				
				storeErrorRecord(conn, m, CAS, endpoint, dd.Error);
			
			
			return vecTPV;
		}

		//		if (!reportTypes.isEmpty() && createDetailedConsensusReport)
		//			df.WriteOut2DDescriptors(m, dd, strOutputFolderStructureData, del);

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			// array to store predictions for all methods for consensus method:
			ArrayList <Double>predictedToxicities = new ArrayList<>();
			ArrayList <Double>predictedUncertainties = new ArrayList<>();

			AllResults allResults = ht_allResults.get(endpoint);
			AllResults allResultsFrag = ht_allResultsFrag.get(endpoint); 

			Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
			Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);
			Instances testDataSet2d = ht_ccPrediction.get(endpoint);

			// String
			// ToxFieldName=trainingDataSet2d.attribute(classIndex).name();

			String ToxFieldName = "Tox";

			Hashtable<String,Object> ht = dd.CreateDataHashtable(ToxFieldName, true, true, false, false, false);

			// Create instances for test chemical (for GCM it will only contain
			// fragment descriptors):
			String[] varArrayFrag = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSetFrag);
			Instances evalInstancesFrag = iu.createInstances(ht, varArrayFrag);
			
			String[] varArray2d = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSet2d);
			Instances evalInstances2d = iu.createInstances(ht, varArray2d);
			Instance evalInstance2d = evalInstances2d.firstInstance();

			//TODO- in future we need to have a database of experimental values that lets you search by CAS or inchii key
			Lookup.ExpRecord er = TaskCalculations.LookupExpVal(dd.ID, evalInstance2d, trainingDataSet2d, testDataSet2d);

			tr.ExpToxValue=FormatUtils.toD3(er.expToxValue);		
			if (tr.ExpToxValue.equals("N/A")) tr.ExpToxCAS="N/A";
			else tr.ExpToxCAS=er.expCAS;

			// **************************************************

			int result = 0;
			
//			String statMessage = "Calculating " + endpoint + "...";
//			statMessage += "Molecule ID = " + dd.CAS + " (" + (molNum + 1) + " of " + molCount + ")";

			// ******************************************************

			start = System.currentTimeMillis();
			long reportGenerationTime = 0;

			ReportOptions options = new ReportOptions();
			options.reportBase = strOutputFolder;
			options.embedImages = true;

			//Folder to store web page for methods other than consensus
			//			String methodOutputFolder=strOutputFolder;
			String methodOutputFolder=null;//dont output report webpage for methods other than consensus

			if (vecMethods.contains(TESTConstants.ChoiceHierarchicalMethod) || vecMethods.contains(TESTConstants.ChoiceConsensus)) {

				String method=TESTConstants.ChoiceHierarchicalMethod;
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, methodOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d,
						trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
				double predToxVal = ptH.predToxVal;
				double predToxUnc=ptH.predToxUnc;

				updateArrays(endpoint, vecMethods, index, dd, vecTPV, isBinaryEndpoint, CAS, predictedToxicities,
						predictedUncertainties, er, result, method, predToxVal, predToxUnc);

				tr.Hierarchical=FormatUtils.toD3(predToxVal);
			} 


			if (TESTConstants.haveSingleModelMethod(endpoint) && (vecMethods.contains(TESTConstants.ChoiceSingleModelMethod) || vecMethods.contains(TESTConstants.ChoiceConsensus))) {
				String method=TESTConstants.ChoiceSingleModelMethod;
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, methodOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d,
						trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);

				double predToxVal = ptH.predToxVal;
				double predToxUnc=ptH.predToxUnc;

				updateArrays(endpoint, vecMethods, index, dd, vecTPV, isBinaryEndpoint, CAS, predictedToxicities,
						predictedUncertainties, er, result, method, predToxVal, predToxUnc);			

				tr.SingleModel=FormatUtils.toD3(predToxVal);				
			}

			if (TESTConstants.haveGroupContributionMethod(endpoint) && (vecMethods.contains(TESTConstants.ChoiceGroupContributionMethod) || vecMethods.contains(TESTConstants.ChoiceConsensus))) {
				String method=TESTConstants.ChoiceGroupContributionMethod;
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, methodOutputFolder, DescriptorSet, allResultsFrag, evalInstancesFrag, evalInstance2d,
						trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);

				double predToxVal = ptH.predToxVal;
				double predToxUnc=ptH.predToxUnc;

				updateArrays(endpoint, vecMethods, index, dd, vecTPV, isBinaryEndpoint, CAS, predictedToxicities,
						predictedUncertainties, er, result, method, predToxVal, predToxUnc);			

				tr.GroupContribution=FormatUtils.toD3(predToxVal);
			}

			if (vecMethods.contains(TESTConstants.ChoiceNearestNeighborMethod) || vecMethods.contains(TESTConstants.ChoiceConsensus)) {
				String method=TESTConstants.ChoiceNearestNeighborMethod;
				result = ptNN.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, methodOutputFolder, DescriptorSet, evalInstances2d, trainingDataSet2d, testDataSet2d,
						dd.MW, er, options);
				double predToxVal = ptNN.predToxVal;
				double predToxUnc = ptNN.predToxUnc;

				updateArrays(endpoint, vecMethods, index, dd, vecTPV, isBinaryEndpoint, CAS, predictedToxicities,
						predictedUncertainties, er, result, method, predToxVal, predToxUnc);			

				tr.NearestNeighbor=FormatUtils.toD3(predToxVal);
			}

			if (vecMethods.contains(TESTConstants.ChoiceLDA)) {

				String method=TESTConstants.ChoiceLDA;
				result = ptLDA.CalculateToxicity(CAS, TESTConstants.ChoiceLDA, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, methodOutputFolder, DescriptorSet, htAllResultsMOA, htTrainingSetsMOA,
						htAllResultsLC50, htTrainingSetsLC50, evalInstances2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, vecMOA, options);

				double predToxVal = ptLDA.predToxVal;

				TESTPredictedValue v;

				if (!isBinaryEndpoint) {
					v = getTESTPredictedValue(endpoint, method, CAS, er.expToxValue, predToxVal, dd.MW);
				} else {
					v = getTESTPredictedValueBinary(endpoint, method, CAS, er.expToxValue, predToxVal, dd.MW);
				}
				vecTPV.add(v);

				//				if ( csv != null )
				//					csv.printField(FormatUtils.toD3(predToxVal));
			} 

			if (vecMethods.contains(TESTConstants.ChoiceConsensus)) {
				String method=TESTConstants.ChoiceConsensus;
				double predToxVal = calculateConsensusToxicity(predictedToxicities);

				//				if ( csv != null )
				//					csv.printField(FormatUtils.toD3(predToxVal));
				double predToxUnc = 1;// TODO: add code to calculate this

				tr.Consensus=FormatUtils.toD3(predToxVal);

				TESTPredictedValue v=updateArrays(endpoint, vecMethods, index, dd, vecTPV, isBinaryEndpoint, CAS, predictedToxicities,
						predictedUncertainties, er, result, method, predToxVal, predToxUnc);			

				long time = System.currentTimeMillis();

				if (!reportTypes.isEmpty()) {
					createConsensusReport(endpoint, reportTypes, dd, v, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
							CAS, dtxcid, predictedToxicities, predictedUncertainties, trainingDataSet2d, testDataSet2d,
							evalInstance2d, er, options, predToxVal, predToxUnc);

				}

				reportGenerationTime = System.currentTimeMillis() - time;

			} // end choice consensus

			if (result == -1) {
				return vecTPV;
			}

			long predictionGenerationTime = System.currentTimeMillis() - start - reportGenerationTime;
			totalReportGenerationTime += reportGenerationTime;
			totalPredictionGenerationTime += predictionGenerationTime;

			String predLogger="N/A";//value to display on logger screen

			if (vecMethods.contains(TESTConstants.ChoiceConsensus)) {
				predLogger=tr.Consensus;
			} else if (vecMethods.size()==1) {//if just have one method, display that one
				if (vecMethods.get(0).equals(TESTConstants.ChoiceHierarchicalMethod)) {
					predLogger=tr.Hierarchical;
				} else if (vecMethods.get(0).equals(TESTConstants.ChoiceSingleModelMethod)) {
					predLogger=tr.SingleModel;
				} else if (vecMethods.get(0).equals(TESTConstants.ChoiceGroupContributionMethod)) {
					predLogger=tr.GroupContribution;
				} else if (vecMethods.get(0).equals(TESTConstants.ChoiceNearestNeighborMethod)) {
					predLogger=tr.NearestNeighbor;
				}
			} else {
				//dont know which prediction to display in logger
			}

			logger.info("{}\t{}\t{}\t{}\t{}\t\t{}\t{}\t{}", index,endpoint, dd.ID, FormatUtils.toD3(er.expToxValue), predLogger, 
					descriptorCalculationTime, predictionGenerationTime, reportGenerationTime);

			//			System.out.println("here1");


		} else {// Descriptors
			// System.out.println(dd.CAS + "\t");
			// TODO
			// WriteDescriptorResultsForChemicalToWebPage(fw,index,CAS,"");

			//Need to return something for descriptors or we get error when try to add smiles to v object later:
			//			return new TESTPredictedValue(CAS, endpoint, method);
			//TODO
		}


		if (conn!=null) {
			if (outputDatabaseFormat==1) {
				WebTESTDBs.addRecordsToDatabaseFormat1(conn, vecTPV,endpoint);
			} else if (outputDatabaseFormat==2) {
				WebTESTDBs.addRecordsToDatabaseFormat2(conn, tr,endpoint);
			}
		}

		return vecTPV;
	}


	private static double calculateConsensusToxicity(ArrayList<Double> preds) {
		double pred = 0;
		int predcount = 0;

		for (int i = 0; i < preds.size(); i++) {
			if (preds.get(i) > -9999) {
				predcount++;
				pred += preds.get(i);
			}
		}

		if (predcount < minPredCount)
			return -9999;

		pred /= (double) predcount;
		// System.out.println(pred);
		return pred;
	}

	private static Vector<TESTPredictedValue> convertRecordsFormat1(Statement stat,String tableName,String keyField,String keyValue) {
//		long t1=System.currentTimeMillis();

		Vector<TESTPredictedValue>vecTPV=new Vector<>();

		try {
			ResultSet rs = MySQL_DB.getRecords(stat,tableName, keyField, keyValue);

			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {

				TESTPredictedValue tpv = new TESTPredictedValue();

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String colName = rsmd.getColumnName(i);
					// System.out.println(i+"\t"+colName);

					String colVal = rs.getString(i);

					if (colName.equals("CAS")) {
						tpv.id = colVal;
					} else if (colName.equals("method")) {
						tpv.method = colVal;
					} else if (colName.equals("expValMass")) {
						tpv.expValMass = new Double(colVal);
					} else if (colName.equals("predValMass")) {
						tpv.predValMass = new Double(colVal);
					} else if (colName.equals("expValLogMolar")) {						
						if (!colVal.equals("N/A"))						
							tpv.expValLogMolar = new Double(colVal);
					} else if (colName.equals("predValLogMolar")) {
						tpv.predValLogMolar = new Double(colVal);
					} else if (colName.equals("expActive")) {
						if (!colVal.equals("N/A"))						
							tpv.expActive = new Boolean(colVal);
					} else if (colName.equals("predActive")) {
						tpv.predActive = new Boolean(colVal);
					}


				}

				vecTPV.add(tpv);
			}

//			long t2=System.currentTimeMillis();

			//			System.out.println(tableName+"\t"+(t2-t1)/1000.0);

			return vecTPV;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	private static Vector<TESTPredictedValue> convertRecordsFormat2(TESTRecord tr,String endpoint) {

		Vector<TESTPredictedValue> vecTPV=new Vector<>();

//		System.out.println("ConvertRecordsFormat2-"+CAS);
		
		String method=TESTConstants.ChoiceHierarchicalMethod;
		String strPredToxVal=tr.Hierarchical;
		addMethodToVecTPV(endpoint, vecTPV,tr.CAS, tr, method, strPredToxVal);

		if (TESTConstants.haveSingleModelMethod(endpoint)) {
			method=TESTConstants.ChoiceSingleModelMethod;
			strPredToxVal=tr.SingleModel;
			addMethodToVecTPV(endpoint, vecTPV, tr.CAS, tr, method, strPredToxVal);
		}

		if (TESTConstants.haveGroupContributionMethod(endpoint)) {
			method=TESTConstants.ChoiceGroupContributionMethod;
			strPredToxVal=tr.GroupContribution;
			addMethodToVecTPV(endpoint, vecTPV, tr.CAS, tr, method, strPredToxVal);
		}

		method=TESTConstants.ChoiceNearestNeighborMethod;
		strPredToxVal=tr.NearestNeighbor;
		addMethodToVecTPV(endpoint, vecTPV,  tr.CAS, tr, method, strPredToxVal);

		method=TESTConstants.ChoiceConsensus;
		strPredToxVal=tr.Consensus;
		addMethodToVecTPV(endpoint,  vecTPV, tr.CAS, tr, method, strPredToxVal);

		return vecTPV;

	}

	private static void createConsensusReport(String endpoint, Set<WebReportType> reportTypes, DescriptorData dd,
			TESTPredictedValue v, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String abbrev, String CAS,String dtxcid,
			 ArrayList<Double> predictedToxicities, ArrayList<Double> predictedUncertainties, Instances trainingDataSet2d,
			Instances testDataSet2d, Instance evalInstance2d, Lookup.ExpRecord er, ReportOptions options,
			double predToxVal, double predToxUnc) {
		double[] Mean = trainingDataSet2d.getMeans();
		double[] StdDev = trainingDataSet2d.getStdDevs();

		Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, testDataSet2d, true, true, true, Mean, StdDev);

		Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, trainingDataSet2d, true, true, true, Mean, StdDev);

//		PredictToxicityWebPageCreator ptwc = new PredictToxicityWebPageCreator();
		PredictToxicityJSONCreator jsonCreator = new PredictToxicityJSONCreator();
		PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();

		ArrayList<String> methods = TaskCalculations.getMethods(endpoint);

		PredictionResults predictionResults = null;

		//		logger.info("Predicted toxicities.size="+predictedToxicities.size());

		if (reportTypes.contains(WebReportType.JSON) || reportTypes.contains(WebReportType.HTML) || reportTypes.contains(WebReportType.PDF)) {
			predictionResults = jsonCreator.writeConsensusResultsJSON(predToxVal, predToxUnc,
					TESTConstants.ChoiceConsensus, CAS, dtxcid, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er,
					dd.MW, "OK", htTestMatch, htTrainMatch, methods, predictedToxicities,
					predictedUncertainties, createDetailedConsensusReport, options);
		}

		if (reportTypes.contains(WebReportType.HTML) || reportTypes.contains(WebReportType.PDF)) {
			String outputfilename="PredictionResultsConsensus.html";
			String htmlFilePath = Paths.get(options.reportBase, outputfilename).toFile().getAbsolutePath();
			htmlCreator.writeConsensusResultsWebPages(predictionResults, htmlFilePath);

			if (reportTypes.contains(WebReportType.PDF) && !htmlFilePath.isEmpty()) {
				try {
					v.reportPath = HtmlUtils.HtmlToPdf(htmlFilePath, FileUtils.replaceExtension(htmlFilePath, ".pdf"));
				} catch (IOException e) {
					logger.catching(e);
				}
			}
		}
	}

	

	

	private static TESTRecord createTESTRecord(AtomContainer m, DescriptorData dd, String CAS) {
		TESTRecord tr;
		String gsid = m.getProperty("gsid");
		String DSSTOXSID = m.getProperty("dsstox_compound_id");
		String DSSTOXCID = m.getProperty("dsstox_substance_id");

		//			if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null) && htChemistryDashboardInfo != null) {

		if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null)) {
			// try to look up based on CAS
			//				ChemistryDashboardRecord rec = GetChemistryDashboardIDs.get(CAS);

			ChemistryDashboardRecord rec = ChemistryDashboardRecord.lookupDashboardRecord("casrn", CAS,statNCCT_ID_Records);
			if (rec == null)
				logger.debug("Cannot resolve {}", CAS);
			else {
				if (gsid == null)
					gsid = rec.gsid;
				if (DSSTOXSID == null)
					DSSTOXSID = rec.dsstox_substance_id;
				if (DSSTOXCID == null)
					DSSTOXCID = rec.dsstox_compound_id;
			}
		}
		
						
//		System.out.println(result[0]+"\t"+result[1]+"\t"+result[2]);
		
		String MolecularWeight=FormatUtils.toD3(dd.MW);
		String InChi=dd.InChi;
		String InChiKey=dd.InChiKey;
		String SMILES=dd.SmilesRan;
		
//		tr=new TESTRecord(CAS,gsid,DSSTOXSID,DSSTOXCID,MolecularWeight,InChi,InChiKey,SMILES);
		tr=new TESTRecord(CAS,gsid,DSSTOXSID,DSSTOXCID,MolecularWeight,InChiKey,SMILES);
		return tr;
	}
	
	
	private static TESTRecord createTESTRecord_Error(AtomContainer m,String CAS,String error) {
		TESTRecord tr;
		String gsid = m.getProperty("gsid");
		String DSSTOXSID = m.getProperty("dsstox_compound_id");
		String DSSTOXCID = m.getProperty("dsstox_substance_id");

		//			if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null) && htChemistryDashboardInfo != null) {

		if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null)) {
			// try to look up based on CAS
			//				ChemistryDashboardRecord rec = GetChemistryDashboardIDs.get(CAS);

			ChemistryDashboardRecord rec = ChemistryDashboardRecord.lookupDashboardRecord("casrn", CAS,statNCCT_ID_Records);
			if (rec == null)
				logger.debug("Cannot resolve {}", CAS);
			else {
				if (gsid == null)
					gsid = rec.gsid;
				if (DSSTOXSID == null)
					DSSTOXSID = rec.dsstox_substance_id;
				if (DSSTOXCID == null)
					DSSTOXCID = rec.dsstox_compound_id;
			}
		}
		
		if (m.getAtomCount()==0) {
//			tr=new TESTRecord(CAS,gsid,DSSTOXSID,DSSTOXCID,null,null,null,null);
			tr=new TESTRecord(CAS,gsid,DSSTOXSID,DSSTOXCID,null,null,null);
		} else {
			Inchi result=CDKUtilities.generateInChiKey(m);
			String InChi=result.inchi;
			String InChiKey=result.inchiKey;
//			String warning=result[2];
			
			
			//TODO use warning message
			
			String SMILES=null;
			try {
				SMILES = CDKUtilities.generateSmiles(m);
			} catch (Exception e) {
				logger.catching(e);
			}
			
//			tr=new TESTRecord(CAS,gsid,DSSTOXSID,DSSTOXCID,null,InChi,InChiKey,SMILES);//TODO add MW somehow
			tr=new TESTRecord(CAS,gsid,DSSTOXSID,DSSTOXCID,null,InChiKey,SMILES);//TODO add MW somehow

		}
					
//		tr.Hierarchical="N/A";
//		tr.SingleModel="N/A";
//		tr.GroupContribution="N/A";
//		tr.NearestNeighbor="N/A";
//		tr.Consensus="N/A";
		
		tr.error=error;
		
		logger.error(CAS+"\t"+error);
		
		return tr;
	}

	private static List<TESTPredictedValue> doPredictions(AtomContainerSet moleculeSet, CalculationParameters params) {

		long start = System.currentTimeMillis();
		List<TESTPredictedValue> vecAllChemicalsAllMethods = new ArrayList<TESTPredictedValue>();

		try {

			Connection connDescriptors=null;
//			PreparedStatement prepDescriptors=null;

			//Create db and table to store descriptors:
			if (usePreviousDescriptors) {
				connDescriptors=MySQL_DB.getConnection(DB_Path_Descriptors);//TODO do we want to store the descriptors db path in CalculationParams?
				WebTESTDBs.createDescriptorsDB(connDescriptors,searchKey);
//				prepDescriptors=createDescriptorPreparedStatement(connDescriptors);
			}

			logger.info("Calculating '{}' using '{}' methods for {} molecule(s)...", Arrays.toString(params.endpoints), Arrays.toString(params.methods), moleculeSet.getAtomContainerCount());
			totalDescriptorCalculationTime = 0;
			totalPredictionGenerationTime = 0;
			totalReportGenerationTime = 0;

			DescriptorFactory df = new DescriptorFactory(false);

			Vector<String>vecMethods=new Vector<>();//create vector so can use contains method
			for (String method:params.methods) vecMethods.add(method);

			Connection conn=null;

			if (params.outputFile!=null) {
				conn = WebTESTDBs.createPredictionsDB(params.outputFile,outputDatabaseFormat,params,searchKey);
			}
						
//			if (params.endpoints[0].equals(TESTConstants.ChoiceDescriptors) && usePreviousDescriptors) {
//				removePreviousCalculatedChemicals(moleculeSet, connDescriptors);
//			}

//			int batchCount=0;
			
			for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
				AtomContainer m = (AtomContainer) moleculeSet.getAtomContainer(i);

				String CAS = (String) moleculeSet.getAtomContainer(i).getProperty("CAS");
				
//				System.out.println(CAS);

				long startDescriptors=System.currentTimeMillis();
				DescriptorData	dd=goDescriptors(m, params.reportTypes,connDescriptors);


				
//				if (usePreviousDescriptors) {
//					batchCount++;
//					if (batchCount==500) {
//						long t1=System.currentTimeMillis();
//						prepDescriptors.executeBatch();
//						long t2=System.currentTimeMillis();
//						System.out.println("Time to commit batch:"+(t2-t1));
//						batchCount=0;
//					}
//				}
				
				long descriptorCalculationTime = System.currentTimeMillis() - startDescriptors;

				if (params.endpoints[0].equals(TESTConstants.ChoiceDescriptors)) {
					logger.info("Descriptor Calcs: {}\t{}\t{}", i, dd.ID, descriptorCalculationTime);
					continue;
				}

				totalDescriptorCalculationTime += descriptorCalculationTime;

				for (int j = 0; j < params.endpoints.length; j++) {
					String endpoint = params.endpoints[j];


					//if (checkParams(endpoint, method) != null) {
					//    continue;
					//}

					Vector<TESTPredictedValue> vecTPV = null;
					try {

						int index = i + 1;
						String error = (String) m.getProperty("Error");

						if (error.isEmpty()) {
							
							vecTPV = calculate(i, moleculeSet.getAtomContainerCount(), m, endpoint, vecMethods, index, params.reportTypes, df, dd,conn,descriptorCalculationTime);
							
							addSmiles(dd.SmilesRan, vecTPV);
							//  logger.info("vecTPV.size="+vecTPV.size());

						} else { // something wrong with chemical dont do calculations
							// but write to file:

							if (!endpoint.equals(TESTConstants.abbrevChoiceDescriptors)) {
								if (conn!=null) {
									vecTPV=storeErrorRecord(conn, m, CAS, endpoint, error);
								}
							} 
						}
						//Store results for this chemical in the overall results vector:
						for (TESTPredictedValue tpv:vecTPV) {
							vecAllChemicalsAllMethods.add(tpv);
						}

					} catch (Exception ex) {
						if (!endpoint.equals(TESTConstants.abbrevChoiceDescriptors)) {
							//TODO store error? or is this reachable at all?
						}

						logger.error("Error processing record with CAS {}: {} {}",CAS, ex.getClass(), ex.getMessage());
						logger.catching(ex);

					} finally {
					}
				} // end loop over endpoints
			} // end loop over molecules


		} catch (Exception ex) {
			logger.catching(ex);
		}

		logger.debug("Time to generate output for {} using {} method: {}s "
				+ "including time to calculate descriptors {}s, " 
				+ "predictions {}s, reports {}s", Arrays.toString(params.endpoints), 
				Arrays.toString(params.methods), 
				(System.currentTimeMillis()-start) / 1000d, 
				totalDescriptorCalculationTime / 1000d,
				totalPredictionGenerationTime / 1000d,
				totalReportGenerationTime / 1000d);

		return vecAllChemicalsAllMethods;
	}

	private static Vector<TESTPredictedValue> storeErrorRecord(Connection conn, AtomContainer m, String CAS, String endpoint, String error)  {
		
		
//		Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
//		Instances testDataSet2d = ht_ccPrediction.get(endpoint);
		//   Lookup.ExpRecord er = LookupExpVal(CAS, trainingDataSet2d, testDataSet2d);
		//TODO store exp value if available?
				
		if (outputDatabaseFormat==1) {
			//TODO
		} else if (outputDatabaseFormat==2) {
			
			try {
				Statement stat=conn.createStatement(); 
				TESTRecord tr=GenerateRecordsFromTEST.getTESTRecord(stat, TESTConstants.getAbbrevEndpoint(endpoint), "CAS", CAS);
				
				if (tr==null) {
					tr=createTESTRecord_Error(m, CAS,error);
					WebTESTDBs.addRecordsToDatabaseFormat2(conn, tr,endpoint);
				}
				
				Vector<TESTPredictedValue>records=convertRecordsFormat2(tr,endpoint);
				return records;

			
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		
		return null;
			
	}

	private static void addSmiles(String smiles, Vector<TESTPredictedValue> vecTPV) {
		for (TESTPredictedValue tpv:vecTPV) {
			tpv.smiles=smiles;
		}
	}

	

//	private static void removePreviousCalculatedChemicals(AtomContainerSet moleculeSet, Connection connDescriptors)
//			throws SQLException {
//		ResultSet rs=MySQL_DB.getAllRecords(connDescriptors.createStatement(), "Descriptors");
//		
//		int count=0;
//		
//		while (rs.next()) {
//
//			String CAS=rs.getString(2);
//			
//			for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
//				String CASi = (String) moleculeSet.getAtomContainer(i).getProperty("CAS");
//				
//				if (CASi.equals(CAS)) {
//					moleculeSet.removeAtomContainer(i);
//					count++;
//					break;
//				}
//			}
//			
//		}
//		
//		logger.info(count+ " previously calculated chemicals removed");
//		
//	}

	
	
	
	

	

	/**
	 * This method allows one to run multiple methods simultaneously without having to rerun individual methods after running single methods
	 * It also stores directly to database using 2 different formats
	 * 
	 * @param ms
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<TESTPredictedValue> go(AtomContainerSet ms, CalculationParameters params) throws Exception {

		///////////////////////////////////////////////////////////////
		// load model data (if isn't already in memory):

		long t1 = System.currentTimeMillis();
		for (String endpoint : params.endpoints) {
			for (String method : params.methods) {
				loadTrainingData(endpoint, method);
			}
		}
		long t2 = System.currentTimeMillis();

		long diff=t2-t1;
		
		if (diff>10) logger.debug("Loading training data in {}s", (t2 - t1) / 1000.);

		///////////////////////////////////////////////////////////////
		// do predictions:


		List<TESTPredictedValue> results=doPredictions(ms, params);

		// For easy debug print out json string:		
//				GsonBuilder builder = new GsonBuilder();
//				builder.setPrettyPrinting();
//				builder.serializeSpecialFloatingPointValues();
//				Gson gson = builder.create();
//				System.out.println(gson.toJson(results));

		return results;

	}

	public static void loadTrainingData(String endpoint, String method) {

		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			return;
		}

		LoadTrainingDataSet(endpoint);

		// if (method.equals(TESTConstants.ChoiceLDA)) {
		// LoadLDAFiles();
		// }

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod) || method.equals(TESTConstants.ChoiceSingleModelMethod) || method.equals(TESTConstants.ChoiceConsensus)) {
			LoadHierarchicalXMLFile(endpoint);
		}

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod) || method.equals(TESTConstants.ChoiceConsensus)) {
			LoadFragmentXMLFile(endpoint);
		}

		// Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
		// Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);

		// trainingDataSet2d.setClassIndex(classIndex);
		// trainingDataSetFrag.setClassIndex(classIndex);
	}

	private static void LoadHierarchicalXMLFile(String endpoint) {
		try {
			if (ht_allResults.get(endpoint) == null) {
				String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
				String xmlFileName = abbrev + "/" + abbrev + "_training_set-2d.xml";
				if ( !HaveFileInJar(xmlFileName) )
					return;

				if (displayLoadingMessage) logger.debug("Loading cluster data file...");
				ht_allResults.put(endpoint, readAllResultsFormat2_2(xmlFileName, ht_ccTraining.get(endpoint), true));
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static AllResults readAllResultsFormat2_2(String resultsXML, Instances trainingDataset, boolean isXMLFileInsideJar) {

		try {
			AllResultsXMLReader arxr = new AllResultsXMLReader();
			AllResults ar = arxr.readAllResults(resultsXML, trainingDataset, true);
			return ar;

		} catch (Exception e) {
			logger.catching(e);
		}
		return null;
	}

	private static boolean HaveFileInJar(String filepath) {
		try {
			java.io.InputStream is = new WebTEST().getClass().getClassLoader().getResourceAsStream(filepath);
			is.read();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void LoadFragmentXMLFile(String endpoint) {
		try {
			if (ht_allResultsFrag.get(endpoint) == null) {
				String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
				String xmlFileName = abbrev + "/" + abbrev + "_training_set-frag.xml";
				if (!HaveFileInJar(xmlFileName)) {
					ht_allResultsFrag.put(endpoint, new AllResults());
					return;
				}

				if (displayLoadingMessage) logger.debug("Loading fragments XML...");

				ht_allResultsFrag.put(endpoint, readAllResultsFormat2_2(xmlFileName, ht_ccTrainingFrag.get(endpoint), true));
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static void LoadTrainingDataSet(String endpoint) {
		if (displayLoadingMessage) logger.debug("Loading training dataset for '{}'...", endpoint);

		String csvTraining_2d;
		String csvTrainingFrag;
		String csvPrediction_2d;

		String train2d = "_training_set-2d.csv";
		String trainfrag = "_training_set-frag.csv";
		String pred2d = "_prediction_set-2d.csv";

		String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
		csvTraining_2d = abbrev + "/" + abbrev + train2d;
		csvPrediction_2d = abbrev + "/" + abbrev + pred2d;
		csvTrainingFrag = abbrev + "/" + abbrev + trainfrag;

		try {
			CSVLoader atf = new CSVLoader();

			if (ht_ccTraining.get(endpoint) == null) {
				ht_ccTraining.put(endpoint, atf.getDataSetFromJarFile(csvTraining_2d));
			}

			if (ht_ccPrediction.get(endpoint) == null) {// always load so that
				// can
				ht_ccPrediction.put(endpoint, atf.getDataSetFromJarFile(csvPrediction_2d));
			}

			if (ht_ccTrainingFrag.get(endpoint) == null) {
				try {
					ht_ccTrainingFrag.put(endpoint, atf.getDataSetFromJarFile(csvTrainingFrag));
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}


	private static String getGSID(AtomContainer ac, String CAS) {
		String gsid = ac.getProperty("gsid");
		String DSSTOXSID = ac.getProperty("dsstox_compound_id");
		String DSSTOXCID = ac.getProperty("dsstox_substance_id");

		if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null)) {
			// try to look up based on CAS
			ChemistryDashboardRecord rec = ChemistryDashboardRecord.lookupDashboardRecord("casrn", CAS,statNCCT_ID_Records);
			if (rec == null)
				logger.debug("Cannot resolve {}", CAS);
			else {
				if (gsid == null)
					gsid = rec.gsid;
				//					if (DSSTOXSID == null)
				//						DSSTOXSID = rec.dsstox_substance_id;
				//					if (DSSTOXCID == null)
				//						DSSTOXCID = rec.dsstox_compound_id;
			}
		}
		return gsid;
	}



	private static String getStructureDataOutputFolder(Set<WebReportType>reportTypes, String CAS) {
		String strOutputFolderStructureData = null;

		if (!reportTypes.isEmpty()) {
			File fileOutputFolder = new File(reportFolderName);
			String strOutputFolder1 = fileOutputFolder.getAbsolutePath() + File.separator + "ToxRuns";
			File fileOutputFolder1 = new File(strOutputFolder1);
			if (!fileOutputFolder1.exists())
				fileOutputFolder1.mkdirs();

			String strOutputFolder2 = strOutputFolder1 + File.separator + "ToxRun_" + CAS;
			File fileOutputFolder2 = new File(strOutputFolder2);
			if (!fileOutputFolder2.exists())
				fileOutputFolder2.mkdirs();

			strOutputFolderStructureData = strOutputFolder2 + File.separator + "StructureData";
			File osdf = new File(strOutputFolderStructureData);
			if (!osdf.exists())
				osdf.mkdirs();

		}
		return strOutputFolderStructureData;
	}


	public static DescriptorData goDescriptors(AtomContainer ac, Set<WebReportType>reportTypes,Connection connDescriptors) {

		String error = (String) ac.getProperty("Error");
		String CAS= (String) ac.getProperty("CAS");

		DescriptorFactory df = new DescriptorFactory(false);
		
		try {
			
			String InChiKey=null;
			String InChi=null;
			String InChi_Warning=null;
			
			String smiles = null;
			
			if (ac.getAtomCount()>0) {
//				long t1=System.currentTimeMillis();
				Inchi result=CDKUtilities.generateInChiKey(ac);
				InChi=result.inchi;
				InChiKey=result.inchiKey;
				InChi_Warning=result.warning;
				
//				long t2=System.currentTimeMillis();
//				System.out.println("Inchi time="+(t2-t1));
								
				try {
					smiles = CDKUtilities.generateSmiles(ac);
				} catch (Exception e) {
					logger.catching(e);
				}
			}

			if (usePreviousDescriptors && InChiKey!=null) {
				long t1=System.currentTimeMillis();
				
				String searchString="";
				if (searchKey.equals(strInChi)) searchString=InChi;
				else if (searchKey.equals(strInChiKey)) searchString=InChiKey;
				else if (searchKey.equals(strCAS)) searchString=CAS;

				
				Statement statDescriptors=connDescriptors.createStatement();
				DescriptorData dd=WebTESTDBs.getDescriptors(statDescriptors, searchKey,searchString,compressDescriptorsInDB);

				long t2=System.currentTimeMillis();
				
				if (dd!=null) {
//					logger.info("Time to load descriptors from db:"+(t2-t1));
					return dd;
				}
			} 
			
			DescriptorData dd = new DescriptorData();
			
			String strOutputFolderStructureData = getStructureDataOutputFolder(reportTypes, CAS);
			
			if(error.isEmpty()) {
				if (!reportTypes.isEmpty()) {
					df.CalculateDescriptors(ac, dd, true, overwriteFiles, true, strOutputFolderStructureData);
					df.WriteJSON(dd, strOutputFolderStructureData+"/descriptorValues.json", true);
				} else {
					
					
					long t1=System.currentTimeMillis();
					df.CalculateDescriptors(ac, dd, true);
					long t2=System.currentTimeMillis();
//					logger.info("Time to calculate descriptors:"+(t2-t1));
					
				}
			} 

			
			dd.ID = CAS;
			dd.dtxcid = ac.getProperty(DSSToxRecord.strCID);
			
			if (ac.getAtomCount()>0) {
				dd.InChiKey=InChiKey;
				dd.InChi=InChi;
				dd.InChi_Warning=InChi_Warning;
				dd.SmilesRan=smiles;
			}
			
			
//			System.out.println(error+"\t"+df.errorMsg);
			
			if (error.isEmpty()) {
				if(df.errorMsg.isEmpty()) dd.Error="OK";
				else dd.Error=df.errorMsg;
			} else {
				dd.Error=error;
			}

			// just in case we are running a chemical without gsid (e.g. user drawn structure):
			if (!reportTypes.isEmpty() && dd.dtxcid == null) {
				ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(ac, "structure", strOutputFolderStructureData);// dont
			}
					
			//Store in database:
			if (usePreviousDescriptors) {
				long t1=System.currentTimeMillis();
				
				WebTESTDBs.addRecordsToDescriptorDatabase(connDescriptors, dd,compressDescriptorsInDB);
				long t2=System.currentTimeMillis();
//				logger.info("Time to store descriptors in database:"+(t2-t1));
			}

			return dd;


		} catch (Exception ex) {

			logger.error("Error processing record with CAS {}: {} {}", CAS, ex.getClass(), ex.getMessage());
			logger.catching(ex);

			DescriptorData dd=new DescriptorData();
			dd.Error="Error processing record with CAS "+CAS+", error="+ex.getMessage();
			return dd;
		} 

	}
	
	public static AtomContainer prepareMolecule(int i, AtomContainer m) {
		ToxPredictor.misc.MolFileUtilities mfu = new ToxPredictor.misc.MolFileUtilities();
		String CASfield = MolFileUtilities.getCASField(m);

		String CAS = null;

		if (!CASfield.equals("")) {
			CAS = (String) m.getProperty(CASfield);
			if (CAS != null) {
				CAS = CAS.trim();
				CAS = CAS.replace("/", "_");
				m.setProperty("CAS", CAS);
			}
		}

		if (CAS == null) {
			String nameField = MolFileUtilities.getNameField(m);
			String name = null;

			if (!nameField.equals("")) {
				name = (String) m.getProperty(nameField);
			}

			if (name == null) {
				// m.setProperty("Error",
				// "<html>CAS and Name fields are both empty</html>");
				// m.setProperty("CAS", "Unknown");

				long time = System.currentTimeMillis();
				m.setProperty("CAS", "C" + i + "_" + time);// generates
				// unique ID

			} else {
				m.setProperty("CAS", name);
			}
		}
		// System.out.println(CAS+"\t"+m.getProperty("NAME")+"\t"+m.getProperty("aritmentic
		// mean"));

		String error = (String) m.getProperty("Error");

		// System.out.println(m.getProperty("CAS")+"\t"+error);
		// System.out.println(m.getProperty("CAS")+"\t"+error==null);

		if (error == null || error.equals("")) {

			m.setProperty("Error", "");

			if (mfu.HaveBadElement(m)) {
				m.setProperty("Error", "Molecule contains unsupported element");
			} else if (m.getAtomCount() == 1) {
				m.setProperty("Error", "Only one nonhydrogen atom");
			} else if (m.getAtomCount() == 0) {
				m.setProperty("Error", "Number of atoms equals zero");
			} else if (!mfu.HaveCarbon(m)) {
				m.setProperty("Error", "Molecule does not contain carbon");
			}

			AtomContainerSet moleculeSet2 = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

			if (moleculeSet2.getAtomContainerCount() > 1) {
				// m.setProperty("Error","Multiple molecules, largest fragment
				// retained");
				m.setProperty("Error", "Multiple molecules");
			}
		} else {
			// otherwise preserve error stored in sdf file
		}

//		System.out.println(m.getProperty("CAS")+"\t"+m.getProperty("Error"));
		
		// df.Normalize(m);//needed?

		// m = (Molecule)MolFileUtilities.CheckForAromaticBonds(m);//not needed?
		return m;
	}
	
	// TODO: Look into lazy loading...
		public static AtomContainerSet LoadFromSDF(String sdf) throws FileNotFoundException, IOException {
			AtomContainerSet moleculeSetFromFile = sdf.indexOf('\n') >= 0 ? MolFileUtilities.LoadFromSdfString(sdf) : MolFileUtilities.LoadFromSDF3(sdf);
			AtomContainerSet moleculeSetExport = new AtomContainerSet(); 

			try {
				for (int i = 0; i < moleculeSetFromFile.getAtomContainerCount(); i++) {
					AtomContainer m = (AtomContainer) moleculeSetFromFile.getAtomContainer(i);
					m = prepareMolecule(i, m);
					moleculeSetExport.addAtomContainer(m);
				} // end loop over molecules

			} catch (Exception e) {
				logger.catching(e);
			}

			return moleculeSetExport;
		}

	private static TESTPredictedValue updateArrays(String endpoint, Vector<String> vecMethods, int index, DescriptorData dd,
			Vector<TESTPredictedValue> vecTPV, boolean isBinaryEndpoint, String CAS, ArrayList<Double> predictedToxicities,
			ArrayList<Double> predictedUncertainties, Lookup.ExpRecord er, int result, String method, double predToxVal,
			double predToxUnc) {

		if (result==0 && vecMethods.contains(TESTConstants.ChoiceConsensus)) {
			predictedToxicities.add(predToxVal);
			predictedUncertainties.add(predToxUnc);
		}

		TESTPredictedValue v=null;

		if (result==0) {

			if (!isBinaryEndpoint) {
				v = getTESTPredictedValue(endpoint, method, CAS, er.expToxValue, predToxVal, dd.MW);
			} else {
				v = getTESTPredictedValueBinary(endpoint, method, CAS, er.expToxValue, predToxVal, dd.MW);
			}

			//			if (!isBinaryEndpoint) {
			//				v = generateTESTPredictedValueContinuous(endpoint, method, index, CAS, er.expToxValue, predToxVal, dd.MW,"");
			//			} else {
			//				v = generateTESTPredictedValueBinary(endpoint, method, index, CAS, er.expToxValue, predToxVal, dd.MW, "");
			//			}
			v.error="OK";

			vecTPV.add(v);
		} 

		return v;

	}

}

