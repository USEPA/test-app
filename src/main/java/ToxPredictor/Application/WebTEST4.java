package ToxPredictor.Application;

import QSAR.qsarOptimal.AllResults;
import QSAR.validation2.InstanceUtilities;
import ToxPredictor.Application.Calculations.DataForPredictionRun;
import ToxPredictor.Application.Calculations.PredictToxicityHierarchical;
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Application.Calculations.PredictToxicityLDA;
import ToxPredictor.Application.Calculations.PredictToxicityNearestNeighbor;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.TaskCalculations;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Application.GUI.Miscellaneous.DangerousPathChecker;
import ToxPredictor.Database.DSSToxRecord;
//import ToxPredictor.Database.ResolverDb;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.FormatUtils;
import ToxPredictor.Utilities.HueckelAromaticityDetector;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.MolFileUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

public class WebTEST4 {
	public static boolean dashboardStructuresAvailable = true;

	private static final Logger logger = LogManager.getLogger(WebTEST4.class);

	public static final String ERROR_CODE_STRUCTURE_ERROR = "SE";// TODO
	private static final String ERROR_CODE_APPLICABILITY_DOMAIN_ERROR = "AD";
	private static final String ERROR_CODE_DESCRIPTOR_CALCULATION_ERROR = "DE";
	private static final String ERROR_CODE_APPLICATION_ERROR = "AE";

	public static String reportFolderName = "web-reports";

//	private ChemicalFinder cf = new ChemicalFinder();

	private static Hashtable<String, Instances> ht_ccTraining = new Hashtable<String, Instances>();
	private static Hashtable<String, Instances> ht_ccTrainingFrag = new Hashtable<String, Instances>();
	private static Hashtable<String, Instances> ht_ccPrediction = new Hashtable<String, Instances>();
	private static Hashtable<String, AllResults> ht_allResults = new Hashtable<String, AllResults>();
	private static Hashtable<String, AllResults> ht_allResultsFrag = new Hashtable<String, AllResults>();

	///////////////////////////////////////////////////////
	private static java.util.Vector<String> vecMOA = new Vector<String>();
	private static Hashtable<String, AllResults> htAllResultsMOA = new Hashtable<String, AllResults>();
	private static Hashtable<String, Instances> htTrainingSetsMOA = new Hashtable<String, Instances>();

	private static Hashtable<String, AllResults> htAllResultsLC50 = new Hashtable<String, AllResults>();
	private static Hashtable<String, Instances> htTrainingSetsLC50 = new Hashtable<String, Instances>();

	private static Instances ccTrainingMOA;
	private static Instances ccPredictionMOA;
	private static Instances ccOverallMOA;

	///////////////////////////////////////////////////////

	private static long totalDescriptorCalculationTime = 0;
	private static long totalPredictionGenerationTime = 0;
	private static long totalReportGenerationTime = 0;

//	private int classIndex = 1;
//	private int chemicalNameIndex = 0;
	private static final String descriptorSet = "2d";
	public static int minPredCount = 2;// minimum number of predictions needed
										// for consensus pred
//	private static String del = "\t";
	public static boolean useFragmentsConstraint = true;

	private static boolean overwriteFiles = true;// dont overwrite any files so dont get conflict from parallel runs
	public static boolean createReports = true;// whether to reports at all for batch run
	public static boolean createDetailedReports = true;// create files for each method and detailed descriptor
//	public static boolean writeReportFiles = false;
	public static boolean generateWebpages = true;

	public static boolean deleteExtraFiles = true;
	public static boolean usePreviousDescriptors = false;// if true will use previously calculated descriptors json file
															// to speed up calculations

//	private final static long minFileAgeHours = 1000000000;// never overwrite
//	private final static int outputDatabaseFormat = 2;// 1 = tsv format, 2 = csv format
//	private final static boolean compressDescriptorsInDB = true;// should we zip the descriptors json string in the database

	///////////////////////////////////////////////////////

	private static InstanceUtilities iu = new InstanceUtilities();
//	private static MolFileUtilities mfu = new MolFileUtilities();

//	private final String NCCT_ID_dataFile = "data/NCCT_ID_data.txt";
//	static Hashtable<String, ChemistryDashboardRecord> htChemistryDashboardInfo = null;

//	public static final String DB_Path_NCCT_ID_Records = "databases/NCCT_ID.db";
//	public static Statement statNCCT_ID_Records = MySQL_DB.getStatement(DB_Path_NCCT_ID_Records);

	public static PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();
	private static PredictToxicityJSONCreator jsonCreator = new PredictToxicityJSONCreator();

	// PredictToxicityFDA ptFDA = new PredictToxicityFDA();
	private static PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
	private static PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
	// PredictToxicityRandomForrestCaesar ptRFC= new
	// PredictToxicityRandomForrestCaesar();
	private static PredictToxicityLDA ptLDA = new PredictToxicityLDA();

//	public static String statMessage="";

	// ****************************************************************************************************
	// Variables for progress update in fraMain8:
//	private static boolean done;//tells fraMain8 if it's done; TODO can we get rid of this???
//	private static boolean stop=false;
//	private static int current;//current % completed for fraMain8 status bar
//	private static String statMessage;//current message to display in fraMain8 progress bar

	public WebTEST4() {
	}

	
	public static DescriptorData runDescriptors(String identifier) {

		DescriptorData dd=null;

		try {

			AtomContainer ac = WebTEST4.getMoleculeFromIdentifier(identifier);
			
			if (ac==null) {
				dd=new DescriptorData();
				dd.Error="Invalid identifier:"+identifier;
				dd.ID=identifier;
				return dd;
			}
			
			WebTEST4.createReports = false;// whether to reports at all for batch run
			WebTEST4.createDetailedReports = false;
			HueckelAromaticityDetector.debug=false;

			dd=WebTEST4.goDescriptors(ac);//generate molecular descriptors
			if (dd.Error.contentEquals("OK")) dd.Error="";			

		} catch (Exception ex) {
			ex.printStackTrace();
			TESTPredictedValue tpv=new TESTPredictedValue();
			tpv.error=ex.getMessage();
		}

		return dd;//consensus is first prediction when running consensus, when running other methods will only have 1 prediction						

	}

	
	private static CalculationParameters createCalculationParameters(String method, String endpoint) {
		CalculationParameters params=new CalculationParameters();

		params.endpoints= new String[1];
		params.endpoints[0]=endpoint;			
		params.methods= new String[1];
		params.methods[0]=method;
		return params;
	}
	
	public static TESTPredictedValue run(String identifier,String endpointAbbrev,String methodAbbrev) {

		DescriptorData dd=null;

		try {

			AtomContainer ac = WebTEST4.getMoleculeFromIdentifier(identifier);//requires snapshot db
//			AtomContainer ac = WebTEST4.loadSMILES(identifier);

			if (ac==null) {
				TESTPredictedValue tpv=new TESTPredictedValue();
				tpv.error="Invalid identifier:"+identifier;
				return tpv;
			}
			
			
			String method=TESTConstants.getFullMethod(methodAbbrev);
			String endpoint=TESTConstants.getFullEndpoint(endpointAbbrev);
			
			CalculationParameters params = createCalculationParameters(method, endpoint);
			
			WebTEST4.createReports = false;// whether to reports at all for batch run
			WebTEST4.createDetailedReports = false;
			HueckelAromaticityDetector.debug=false;

			WebTEST4.loadTrainingData(endpoint, method);

			String error=(String)ac.getProperty("Error");
			

			dd=WebTEST4.goDescriptors(ac);//generate molecular descriptors
			List<TESTPredictedValue>listTPV=WebTEST4.go2(areDashboardStructuresAvailable(),ac,dd, params);
			
			TESTPredictedValue tpv=listTPV.get(0);
			tpv.error=error;
			
			return tpv;//consensus is first prediction when running consensus, when running other methods will only have 1 prediction						

		} catch (Exception ex) {
			ex.printStackTrace();
			TESTPredictedValue tpv=new TESTPredictedValue();
			tpv.error=ex.getMessage();
			return tpv;
		}


	}
	
	
	private static void LoadTrainingDataSet(String endpoint) {
		logger.debug("Loading training dataset for '{}'...", endpoint);

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

	public static void loadTrainingData(String endpoint, String method) {

		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			return;
		}

		LoadTrainingDataSet(endpoint);

		if (method.equals(TESTConstants.ChoiceLDA)) {
			LoadLDAFiles();
		}

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod)
				|| method.equals(TESTConstants.ChoiceSingleModelMethod)
				|| method.equals(TESTConstants.ChoiceConsensus)) {
			LoadHierarchicalXMLFile(endpoint);
		}

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod)
				|| method.equals(TESTConstants.ChoiceConsensus)) {
			LoadFragmentXMLFile(endpoint);
		}

	}

	private static void LoadLDAFiles() {
		if (vecMOA.size() > 0)
			return;

		// Load overall sets:

		try {
			CSVLoader atf = new CSVLoader();

			ccTrainingMOA = atf.getDataSetFromJarFile("LC50 training set.csv");
			ccPredictionMOA = atf.getDataSetFromJarFile("LC50 prediction set.csv");
			ccOverallMOA = atf.getDataSetFromJarFile("overall_set.csv");

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Specific MOAs:
		// vecMOA.add("AChE inhibition-Carbamate");
		// vecMOA.add("AChE inhibition-Organophosphate");
		// vecMOA.add("Narcosis-Ester");
		// vecMOA.add("Narcosis-Nonpolar");
		// vecMOA.add("Narcosis-Polar");
		// vecMOA.add("Neurotoxicity-Organochlorine");
		// vecMOA.add("Neurotoxicity-Pyrethroid");
		// vecMOA.add("Reactivity");
		// vecMOA.add("Uncoupling Oxidative Phosphorylation");

		// broad MOAs:
		vecMOA.add("AChE inhibition");
		// vecMOA.add("Anticoagulation");//no LC50 model
		// vecMOA.add("nAChR Agonism");//no LC50 model
		vecMOA.add("Narcosis");
		vecMOA.add("Neurotoxicity");
		vecMOA.add("Reactivity");
		// vecMOA.add("Uncoupling_Inhibiting Oxidative Phosphorylation- Electron
		// transport inhibitors");
		vecMOA.add("Uncoupler");

//		String endpointInFile = "mace_moa";
		CSVLoader atf = new CSVLoader();

		for (int i = 0; i < vecMOA.size(); i++) {

			try {

				String MOAi = vecMOA.get(i);

				String trainingFilePathMOA = "LDA/" + MOAi + ".csv";
				// Note need LDA folder in caps or it wont properly load from
				// jar file!

				Instances trainingDatasetMOA = atf.getDataSetFromJarFile(trainingFilePathMOA);
				htTrainingSetsMOA.put(MOAi, trainingDatasetMOA);

				String xmlFilePathMOA = "LDA/" + MOAi + ".xml";

				// System.out.println(xmlFilePathMOA);

				AllResults allResultsMOA = WebTEST.readAllResultsFormat2_2(xmlFilePathMOA, trainingDatasetMOA, true);

				htAllResultsMOA.put(MOAi, allResultsMOA);

				String trainingFilePathLC50 = "LC50/" + MOAi + ".csv";
				File tfpl = new File(trainingFilePathLC50);

				// System.out.println(trainingFilePathLC50);

				Instances trainingDatasetLC50 = atf.getDataSetFromJarFile(trainingFilePathLC50);
				htTrainingSetsLC50.put(MOAi, trainingDatasetLC50);

				String xmlFilePathLC50 = "LC50/" + MOAi + ".xml";
				AllResults allResultsLC50 = WebTEST.readAllResultsFormat2_2(xmlFilePathLC50, trainingDatasetLC50, true);
				htAllResultsLC50.put(MOAi, allResultsLC50);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} // end loop over MOAs

	}

	private static void LoadFragmentXMLFile(String endpoint) {
		try {
			if (ht_allResultsFrag.get(endpoint) == null) {
				String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
				String xmlFileName = abbrev + "/" + abbrev + "_training_set-frag.xml";
				if (!WebTEST.HaveFileInJar(xmlFileName)) {
					ht_allResultsFrag.put(endpoint, new AllResults());
					return;
				}

				logger.debug("Loading fragments XML...");

				ht_allResultsFrag.put(endpoint,
						WebTEST.readAllResultsFormat2_2(xmlFileName, ht_ccTrainingFrag.get(endpoint), true));
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static void LoadHierarchicalXMLFile(String endpoint) {
		try {
			if (ht_allResults.get(endpoint) == null) {
				String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
				String xmlFileName = abbrev + "/" + abbrev + "_training_set-2d.xml";
				if (!WebTEST.HaveFileInJar(xmlFileName))
					return;

				logger.debug("Loading cluster data file...");
				ht_allResults.put(endpoint,
						WebTEST.readAllResultsFormat2_2(xmlFileName, ht_ccTraining.get(endpoint), true));
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private static List<TESTPredictedValue> calculate(AtomContainer m, DescriptorData dd, DescriptorFactory df,
			String endpoint, String method, Set<WebReportType> reportTypes, ReportOptions reportOptions) {

		List<TESTPredictedValue> res = new ArrayList<TESTPredictedValue>();

		long start = System.currentTimeMillis();

		String CAS = dd.ID;
		String dtxcid = m.getProperty(DSSToxRecord.strCID);// already looked up in do predictions
		String dtxsid = m.getProperty(DSSToxRecord.strSID);// already looked up in do predictions

		// ******************************************************************

		if (!"OK".equals(dd.Error)) {
			// done=true; // commented out: dont kill rest of run for batch
			logger.error("Error calculating descriptors for {}", dd.ID);
			// TODO- create web report for this chemical that mentions this
			return res;

			// TODO need to make sure TaskCalculations handles this
		}

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			// array to store predictions for all methods for consensus method:

			AllResults allResults = ht_allResults.get(endpoint);// shortcut to results object
			AllResults allResultsFrag = ht_allResultsFrag.get(endpoint); // shortcut to results object

			Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
			Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);
			Instances testDataSet2d = ht_ccPrediction.get(endpoint);

			String ToxFieldName = "Tox";

			Hashtable<String, Object> ht = dd.CreateDataHashtable(ToxFieldName, true, true, false, false, false);

			// Create instances for test chemical (for GCM it will only contain
			// fragment descriptors):
			String[] varArrayFrag = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSetFrag);
			Instances evalInstancesFrag = iu.createInstances(ht, varArrayFrag);
			String[] varArray2d = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSet2d);
			Instances evalInstances2d = iu.createInstances(ht, varArray2d);
			Instance evalInstance2d = evalInstances2d.firstInstance();

//			System.out.println(evalInstances2d.getDescriptorNames());
//			System.out.println(evalInstance2d.toString());

//			Lookup.ExpRecord er = TaskCalculations.LookupExpVal(dd.CAS, evalInstance2d, trainingDataSet2d,
//					testDataSet2d);

			Lookup.ExpRecord er = null;

			if (method.equals(TESTConstants.ChoiceLDA)) {
				er = TaskCalculations.LookupExpVal_LDA(CAS, evalInstance2d, ccOverallMOA, ccTrainingMOA,
						ccPredictionMOA);
			} else {
				er = TaskCalculations.LookupExpVal(CAS, evalInstance2d, trainingDataSet2d, testDataSet2d);
			}

			// **************************************************

//			int result = 0;
//			String statMessage = "Calculating " + endpoint + " for "+dd.CAS;
			// System.out.println(statMessage);

			// ******************************************************

			long descriptorCalculationTime = System.currentTimeMillis() - start;
			totalDescriptorCalculationTime += descriptorCalculationTime;
			start = System.currentTimeMillis();
			long reportGenerationTime = 0;

			Hashtable<Double, Instance> htTestMatch = null;
			Hashtable<Double, Instance> htTrainMatch = null;

			// Currently we have a problem where the training and test sets for LDA for LC50
			// endpoint dont match that for the other methods...
			if (method.contentEquals(TESTConstants.ChoiceLDA)) {

				double[] Mean = ccTrainingMOA.getMeans();
				double[] StdDev = ccTrainingMOA.getStdDevs();

				htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, ccPredictionMOA, true, true, true,
						Mean, StdDev);

				htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, ccTrainingMOA, true, true, true,
						Mean, StdDev);

			} else {

				double[] Mean = trainingDataSet2d.getMeans();
				double[] StdDev = trainingDataSet2d.getStdDevs();

				htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, testDataSet2d, true, true, true,
						Mean, StdDev);

				htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, trainingDataSet2d, true, true,
						true, Mean, StdDev);

			}

			DataForPredictionRun d = new DataForPredictionRun(descriptorSet, endpoint,
					TESTConstants.getAbbrevEndpoint(endpoint), TESTConstants.isBinary(endpoint),
					TESTConstants.isLogMolar(endpoint), useFragmentsConstraint, CAS, dtxcid, dtxsid,dd.SmilesRan,er, dd.MW, dd.MW_Frag,
					htTestMatch, htTrainMatch, createDetailedReports, reportOptions, reportTypes);

			if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
				runHierarchical(TESTConstants.ChoiceHierarchicalMethod, d, res, trainingDataSet2d, evalInstances2d,
						allResults, reportOptions, createReports);
			} else if (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
				runNN(d, res, descriptorSet, trainingDataSet2d, evalInstances2d, reportOptions, createReports);
			} else if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
				runHierarchical(TESTConstants.ChoiceSingleModelMethod, d, res, trainingDataSet2d, evalInstances2d,
						allResults, reportOptions, createReports);
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
				runHierarchical(TESTConstants.ChoiceGroupContributionMethod, d, res, trainingDataSetFrag,
						evalInstancesFrag, allResultsFrag, reportOptions, createReports);
			} else if (method.equals(TESTConstants.ChoiceLDA)) {
				runLDA(method, d, res, reportOptions, CAS, evalInstances2d, createReports);
			} else if (method.equals(TESTConstants.ChoiceConsensus)) {
				runConsensus(d, res, evalInstances2d, trainingDataSet2d, allResults, evalInstancesFrag,
						trainingDataSetFrag, allResultsFrag, descriptorCalculationTime, reportOptions);
			} // end choice consensus

			long predictionGenerationTime = System.currentTimeMillis() - start - reportGenerationTime;
			totalReportGenerationTime += reportGenerationTime;
			totalPredictionGenerationTime += predictionGenerationTime;

		} else {// Descriptors
			res.add(new TESTPredictedValue(CAS, endpoint, method));
			return res;

		}

		return res;
	}

	private static void runLDA(String method, DataForPredictionRun d, List<TESTPredictedValue> res,
			ReportOptions reportOptions, String CAS, Instances evalInstances2d, boolean createReports) {

		ptLDA.CalculateToxicity2(d, CAS, htAllResultsMOA, htTrainingSetsMOA, htAllResultsLC50, htTrainingSetsLC50,
				evalInstances2d, vecMOA);

		TESTPredictedValue tpv = WebTEST.getTESTPredictedValue(d.endpoint, method, d.CAS, d.er.expToxValue,
				ptLDA.predToxVal, d.MW, "", d.isBinaryEndpoint);
		tpv.message = "OK";

		tpv.predictionResults = jsonCreator.generatePredictionResultsLDA(d, tpv, ptLDA.predToxVal, ptLDA.predToxUnc,
				ptLDA.bestMOA, ptLDA.maxScore, vecMOA, ptLDA.predArrayMOA, ptLDA.predArrayLC50, htAllResultsMOA,
				htAllResultsLC50, ptLDA.chemical, reportOptions, createReports);

		if (generateWebpages && createReports) {		
			writeResultsFiles(d,tpv,method);				
		}

		res.add(tpv);

		logger.info("{}\t{}\t{}\t{}", d.CAS, FormatUtils.toD3(d.er.expToxValue), FormatUtils.toD3(ptLDA.predToxVal),
				d.endpoint);

	}

	private static double runConsensus(DataForPredictionRun d, List<TESTPredictedValue> res, Instances evalInstances2d,
			Instances trainingDataSet2d, AllResults allResults, Instances evalInstancesFrag,
			Instances trainingDataSetFrag, AllResults allResultsFrag, long descriptorCalculationTime,
			ReportOptions options) {

		long timeStartPredictions = System.currentTimeMillis();

		ArrayList<Double> predictedToxicities = new ArrayList<>();
		ArrayList<Double> predictedUncertainties = new ArrayList<>();

		// Hierarchical:
		runHierarchical(TESTConstants.ChoiceHierarchicalMethod, d, res, trainingDataSet2d, evalInstances2d, allResults,
				options, createDetailedReports);
		predictedToxicities.add(ptH.predToxVal);
		predictedUncertainties.add(ptH.predToxUnc);

		// Single Model
		if (TESTConstants.haveSingleModelMethod(d.endpoint)) {
			runHierarchical(TESTConstants.ChoiceSingleModelMethod, d, res, trainingDataSet2d, evalInstances2d,
					allResults, options, createDetailedReports);
			predictedToxicities.add(ptH.predToxVal);
			predictedUncertainties.add(ptH.predToxUnc);
		} else {
			res.add(WebTEST.getTESTPredictionError(d.endpoint, TESTConstants.ChoiceSingleModelMethod, d.CAS,
					"Single model method is unavailable for this endpoint", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
		}

//		System.out.println(TESTConstants.haveGroupContributionMethod(d.endpoint));

		// Group contribution:
		if (TESTConstants.haveGroupContributionMethod(d.endpoint)) {
			runHierarchical(TESTConstants.ChoiceGroupContributionMethod, d, res, trainingDataSetFrag, evalInstancesFrag,
					allResultsFrag, options, createDetailedReports);
			predictedToxicities.add(ptH.predToxVal);
			predictedUncertainties.add(ptH.predToxUnc);
		} else {
			res.add(WebTEST.getTESTPredictionError(d.endpoint, TESTConstants.ChoiceGroupContributionMethod, d.CAS,
					"Group contribution method is unavailable for this endpoint",
					ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
		}
		// Nearest neighbor:
		runNN(d, res, d.DescriptorSet, trainingDataSet2d, evalInstances2d, options, createDetailedReports);
		predictedToxicities.add(ptNN.predToxVal);
		predictedUncertainties.add(ptNN.predToxUnc);

		double predToxVal = WebTEST.calculateConsensusToxicity(predictedToxicities);
		predictedToxicities.add(predToxVal);
//		double predToxUnc = 1;// TODO: add code to calculate this

		String method = TESTConstants.ChoiceConsensus;

		TESTPredictedValue tpv = WebTEST.getTESTPredictedValue(d.endpoint, method, d.CAS, d.er.expToxValue, predToxVal,
				d.MW, "", d.isBinaryEndpoint);

		res.add(0, tpv);

		tpv.message = "OK";
		int predCount = 0;
		for (int i = 0; i < predictedToxicities.size(); i++) {
			if ((Double) predictedToxicities.get(i) != -9999)
				predCount++;
		}

		if (predCount == 0) {
			tpv.message = "No prediction could be made";
		} else if (predCount < TaskCalculations.minPredCount) {
			tpv.message = "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made";
		}

		long timeFinishPredictions = System.currentTimeMillis();

		long t1 = System.currentTimeMillis();
		tpv.predictionResults = jsonCreator.generatePredictionResultsConsensus(d, tpv, predictedToxicities,
				predictedUncertainties, predToxVal, options, createReports);
		long t2 = System.currentTimeMillis();

//		System.out.println((t2-t1)+" millsecs");

		if (generateWebpages) writeResultsFiles(d, tpv, method);			

//		long reportGenerationTime = System.currentTimeMillis() - timeFinishPredictions;
		long predictionGenerationTime = timeFinishPredictions - timeStartPredictions;

		logger.info("{}\t{}\t{}\t\t{}\t{}\t{}", d.CAS, FormatUtils.toD3(d.er.expToxValue), FormatUtils.toD3(predToxVal),
				descriptorCalculationTime, predictionGenerationTime, d.endpoint);

		return predToxVal;
	}

	public static boolean areDashboardStructuresAvailable() {
		try {
			final URL url = new URL("https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/DTXCID20135");
			final URLConnection conn = url.openConnection();

			conn.setConnectTimeout(3 * 1000);
			conn.setReadTimeout(3 * 1000);

			conn.connect();
			conn.getInputStream().close();
			return true;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			return false;
		}
	}

	private static void runHierarchical(String method, DataForPredictionRun d, List<TESTPredictedValue> res,
			Instances instancesTrain, Instances instancesEval, AllResults allResults, ReportOptions options,
			boolean createReports) {

		int result = ptH.CalculateToxicity2(method, d.useFragmentsConstraint, d.isBinaryEndpoint, d.MW, d.MW_Frag,
				instancesEval, instancesTrain, allResults);

		TESTPredictedValue tpv = WebTEST.getTESTPredictedValue(d.endpoint, method, d.CAS, d.er.expToxValue,
				ptH.predToxVal, d.MW, "", d.isBinaryEndpoint);
		tpv.message = ptH.msg;

//		System.out.println(tpv.expValLogMolar+"\t"+tpv.predValLogMolar);

		// predictionResults object is needed needed for TEST prediction results tables
		// even if doesnt create webpage:
		// TODO- to do how to avoid create structure images for similar chemicals table
		// if similar chemicals table is never displayed

		tpv.predictionResults = jsonCreator.generatePredictionResultsHierarchicalClustering(ptH.chemical, d, tpv,
				ptH.predToxVal, ptH.predToxUnc, ptH.resultsVector, ptH.invalidResultsVector, ptH.predictions,
				ptH.uncertainties, ptH.predictionsOutsideAD, ptH.uncertaintiesOutsideAD, ptH.violationsAD, method,
				options, createReports);

		if (generateWebpages && createReports) {		
			writeResultsFiles(d,tpv,method);				
		}

		res.add(tpv);
	}

	private static void writeResultsFiles(DataForPredictionRun d, TESTPredictedValue tpv, String method) {

		String fileNameNoExtension = getResultFileNameNoExtension(d.endpoint, tpv.method, d.CAS);

		String reportBase=d.reportOptions.reportBase;

		
//		reportBase="\\\\MYSERVER\\MYFOLDER\\MYFOLDER\\MYPICTURE.JPG";
		
//		if (DangerousPathChecker.isDangerous(new File(reportBase))) {
//			reportBase="\\\\"+reportBase;
//			
//			try {
//		        // First try to resolve as URL (file:...)
//		        Path path = Paths.get(new URL(reportBase).toURI());		        
//		        reportBase=path.toFile().getAbsolutePath();
//		        
//		    } catch (Exception ex) {
//		        // If given file string isn't an URL, fall back to using a normal file 
//		        ex.printStackTrace();
//		    	return;
//		    }
			
//			System.out.println(reportBase);
//		}
		
		
		
		if (d.reportTypes.contains(WebReportType.HTML)) {
//			String outputFileName="PredictionResults"+method.replace(" ","")+".html";
			String outputFileName = fileNameNoExtension + ".html";
			String outputFilePath = reportBase + File.separator + outputFileName;
			htmlCreator.writeResultsWebPages(tpv.predictionResults, outputFilePath);
		}

//		System.out.println("have json="+d.reportTypes.contains(WebReportType.JSON));
		
		if (d.reportTypes.contains(WebReportType.JSON)) {
//			String outputFileName="PredictionResults"+method.replace(" ","")+".json";				
			String outputFileName = fileNameNoExtension + ".json";
			String outputFilePath = reportBase + File.separator + outputFileName;
			writeJSON(outputFilePath, tpv.predictionResults);
		}
	}

	public static String getResultFileNameNoExtension(String endpoint, String method, String CAS) {
		return endpoint.replace(" ", "_") + "_" + method.replace(" ", "_") + "_" + CAS;
	}

	private static void runNN(DataForPredictionRun d, List<TESTPredictedValue> res, String descriptorSet,
			Instances instancesTrain, Instances instancesEval, ReportOptions options, boolean createReports) {
		int result = ptNN.CalculateToxicity2(descriptorSet, instancesTrain, instancesEval);
		double predToxVal = ptNN.predToxVal;
//		double predToxUnc=ptNN.predToxUnc;//TODO		

		String method = TESTConstants.ChoiceNearestNeighborMethod;

		TESTPredictedValue v = WebTEST.getTESTPredictedValue(d.endpoint, method, d.CAS, d.er.expToxValue, predToxVal,
				d.MW, "", d.isBinaryEndpoint);

		v.message = ptNN.msg;

		v.predictionResults = jsonCreator.generatePredictionResultsNearestNeighbor(d, v, predToxVal, options,
				createReports);

		if (generateWebpages && createReports) {
			writeResultsFiles(d,v,method);
		}
		res.add(v);
	}

	public static void writeJSON(String filePath, Object object) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
//		builder.disableHtmlEscaping();
		Gson gson = builder.create();
		try {
			FileWriter fw = new FileWriter(filePath);
			fw.write(gson.toJson(object));
			fw.close();

		} catch (Exception ex) {
			logger.error("Error writing " + filePath);
		}
	}

	public static void printJSON(Object object) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
//		builder.disableHtmlEscaping();
		Gson gson = builder.create();
		System.out.println(gson.toJson(object));
	}

	public static synchronized List<TESTPredictedValue> doPredictions(AtomContainer m, DescriptorData dd,
			CalculationParameters params) {

		DescriptorFactory df = new DescriptorFactory(false);

		logger.info("Calculating '{}' using '{}' methods...", Arrays.toString(params.endpoints),
				Arrays.toString(params.methods));
		long start = System.currentTimeMillis();
		totalDescriptorCalculationTime = 0;
		totalPredictionGenerationTime = 0;
		totalReportGenerationTime = 0;

		List<TESTPredictedValue> result = new ArrayList<TESTPredictedValue>();

		String CAS = (String) m.getProperty("CAS");

		DSSToxRecord rec = null;

		try {
			rec = DSSToxRecord.createDSSToxRecord(m);
//			System.out.println("used value from molecule");

		} catch (Exception ex) {

//			System.out.println("try to look up");

			if (ResolverDb2.isAvailable()) {
				if (!Strings.isEmpty(CAS) && !CAS.matches("C\\d*_\\d{8,}")) {
					ArrayList<DSSToxRecord> recs = ResolverDb2.lookupByCAS(CAS);
					if (recs.size() > 0) {
						rec = recs.get(0);
					}
				} else {
					ArrayList<DSSToxRecord> recs = ResolverDb2.lookupByAtomContainer(m);
					if (recs.size() > 0) {
						rec = recs.get(0);
						CAS = rec.cas;
					}

				}
			}
		}

		for (String method : params.methods) {
			for (int j = 0; j < params.endpoints.length; j++) {
				String endpoint = params.endpoints[j];

				ReportOptions options = getReportOptions(params, CAS, endpoint);

				// if (checkParams(endpoint, method) != null) {
				// continue;
				// }

				List<TESTPredictedValue> res = null;

				TESTPredictedValue v = null;
				try {
					String smiles = null;
					try {
						smiles = CDKUtilities.generateSmiles(m);
						dd.SmilesRan=smiles;
					} catch (Exception e) {
						logger.catching(e);
					}

					String error = (String) m.getProperty("Error");

					if (StringUtils.isEmpty(error)) {
						res = calculate(m, dd, df, endpoint, method, params.reportTypes, options);
					} else { // something wrong with chemical dont do calculations
								// but write to file:

						res = new ArrayList<>();

						String errorCode = (String) m.getProperty("ErrorCode");

						if (!endpoint.equals(TESTConstants.abbrevChoiceDescriptors)) {
							Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
							Instances testDataSet2d = ht_ccPrediction.get(endpoint);

							Lookup.ExpRecord er = WebTEST.LookupExpVal(CAS, trainingDataSet2d, testDataSet2d);

							v = WebTEST.getTESTPredictedValue(endpoint, method, CAS, er.expToxValue, -9999, er.MW, "",
									TESTConstants.isBinary(endpoint));
							v.error = error;
							v.errorCode = errorCode;

						} else {
							v = new TESTPredictedValue(CAS, endpoint, method);
							v.error = error;
							v.errorCode = errorCode;
						}
						res.add(v);
					}

					// Add SMILES to the predicted result
					if (res != null) {
						for (TESTPredictedValue pv : res) {
							pv.smiles = smiles;
						}
					}

				} catch (Exception ex) {

					logger.error("Error processing record with CAS {}: {} {}", CAS, ex.getClass(), ex.getMessage());
					logger.catching(ex);
					v = new TESTPredictedValue(CAS, endpoint, method);
					v.error = ex.getMessage();
					v.errorCode = ERROR_CODE_APPLICATION_ERROR;
					res.add(v);
				} finally {
					if (!params.discardResults) {
						addProperties(rec, res);
						result.addAll(res);
					}
				}
			} // end loop over endpoints
		} // end loop over methods

		logger.debug(
				"Time to generate output for {} using {} method: {}s " + "including time to calculate descriptors {}s, "
						+ "predictions {}s, reports {}s",
				Arrays.toString(params.endpoints), Arrays.toString(params.methods),
				(System.currentTimeMillis() - start) / 1000d, totalDescriptorCalculationTime / 1000d,
				totalPredictionGenerationTime / 1000d, totalReportGenerationTime / 1000d);

		return result;
	}

	private static ReportOptions getReportOptions(CalculationParameters params, String CAS, String endpoint) {
		ReportOptions options = new ReportOptions();
		options.embedImages = true;

		if (!params.reportTypes.isEmpty()) {
//			System.out.println(reportFolderName);
			String folderOutputMain = reportFolderName;
			String folderToxRuns = folderOutputMain + File.separator + "ToxRuns";
			String folderToxRunCAS = folderToxRuns + File.separator + "ToxRun_" + CAS;
			try {
				String folderEndpointCAS = folderToxRunCAS + File.separator + endpoint;
				options.reportBase = folderEndpointCAS;

				if (!endpoint.equals(TESTConstants.ChoiceDescriptors) && createReports) {

					if (generateWebpages) {
						Path pathEndpointCAS = Paths.get(folderEndpointCAS);
						Files.createDirectories(pathEndpointCAS);
					}

//					File bob=new File("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Profile\\Documents\\MyToxicity\\ToxRuns");
//					System.out.println("XXX"+CAS+"\tToxRunExists="+bob.exists());

				}
			} catch (Exception ex) {
				logger.error("Error creating folders");
				return null;
			}

		}
		return options;
	}

	private static String getStructureDataFolder(String CAS) {
//		System.out.println(reportFolderName);
		String folderOutputMain = reportFolderName;
		String folderToxRuns = folderOutputMain + File.separator + "ToxRuns";
		String folderToxRunCAS = folderToxRuns + File.separator + "ToxRun_" + CAS;
		String folderStructureData = folderToxRunCAS + File.separator + "StructureData";

		return folderStructureData;
	}

	private static void addProperties(DSSToxRecord rec, List<TESTPredictedValue> res) {
		if (rec == null || res == null) {
			return;
		}

		for (TESTPredictedValue r : res) {
			r.casrn = rec.cas;
			r.dtxsid = rec.sid;
			r.dtxcid = rec.cid;
			r.preferredName = rec.name;
			r.inChICode = rec.inchi;
			r.inChIKey = rec.inchiKey;
		}
	}

	public static DescriptorData goDescriptors(IAtomContainer ac) {

		String strOutputFolderStructureData = getStructureDataFolder(ac.getProperty("CAS"));
		// TODO what happens if have no CAS? Can it happen?

		String error = (String) ac.getProperty("Error");

		DescriptorFactory df = new DescriptorFactory(false);
		df.Calculate3DDescriptors = false;

		if (Strings.isEmpty(error)) {
			if ((!createDetailedReports) && !dashboardStructuresAvailable
					|| ac.getProperty(DSSToxRecord.strCID) == null) {
				if (generateWebpages) {
					ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(ac, "structure",
							strOutputFolderStructureData);
				}
			}
		} else {
			DescriptorData dd = new DescriptorData();
			dd.Error = error;
			dd.ID = ac.getProperty("CAS");
			dd.dtxcid = ac.getProperty("gsid");
			return dd;
		}

		try {
			DescriptorData dd = new DescriptorData();
			dd.ID = (String) ac.getProperty("CAS");
			String CAS = dd.ID;

			dd.ThreeD = false;

			dd.dtxcid = ac.getProperty(DSSToxRecord.strCID);

			int descresult = -1;

			// TODO store descriptors in SQLite database instead of json files

			if (usePreviousDescriptors) {
				String filePathDescriptors = strOutputFolderStructureData + File.separator + "descriptorValues.json";
				File descFile = new File(filePathDescriptors);
//				System.out.println(filePathDescriptors);
//				System.out.println(descFile.exists());

				if (descFile.exists()) {

					long t1 = System.currentTimeMillis();
					dd = DescriptorData.loadFromJSON(descFile);
					dd.ThreeD = false;
					dd.WriteToFileHTML(strOutputFolderStructureData);
//					System.out.println(dd.CAS+"\t"+dd.x0);
					if (dd != null)
						descresult = 0;

					if (Math.abs(dd.MW - dd.MW_Frag) > 1) {
						descresult = -1;// recalculate descriptors just in case
					}
					long t2 = System.currentTimeMillis();

					logger.debug("Loaded previously calculated descriptors in {}s", (t2 - t1) / 1000.);

				}
			}

			if (descresult == -1) {

				long t1 = System.currentTimeMillis();

				if (createDetailedReports) {
					descresult = df.CalculateDescriptors(ac, dd, true, overwriteFiles, true,
							strOutputFolderStructureData);
				} else {
					descresult = df.CalculateDescriptors(ac, dd, true);

				}

				long t2 = System.currentTimeMillis();

				logger.debug("Calculated descriptors in {}s", (t2 - t1) / 1000.);
			}

			if (descresult == -1) {
				dd.Error = df.errorMsg;// store error message in dd
				// TODO should we have stored error in dd to begin with?

				logger.error("Error processing record with CAS {}: {}", CAS, dd.Error);
				ac.setProperty("Error", dd.Error);
				ac.setProperty("ErrorCode", ERROR_CODE_DESCRIPTOR_CALCULATION_ERROR);
			} else {
				dd.Error = "OK";
			}

//			logger.debug("dd.error={}", dd.error);

			return dd;

		} catch (Exception ex) {
			final String CAS = (String) ac.getProperty("CAS");
			logger.error("Error processing record with CAS {}: {} {}", CAS, ex.getClass(), ex.getMessage());
			logger.catching(ex);

			DescriptorData dd = new DescriptorData();
			dd.Error = "Error processing record with CAS " + CAS + ", error=" + ex.getMessage();

			ac.setProperty("Error", dd.Error);
			ac.setProperty("ErrorCode", ERROR_CODE_DESCRIPTOR_CALCULATION_ERROR);

			return dd;
		}

	}

	public static List<TESTPredictedValue> go(AtomContainer ac, CalculationParameters params) throws Exception {

		///////////////////////////////////////////////////////////////
		// load model data (if isn't already in memory):

		long t1 = System.currentTimeMillis();
		for (String endpoint : params.endpoints) {
			for (String method : params.methods) {
				loadTrainingData(endpoint, method);
			}
		}
		long t2 = System.currentTimeMillis();

		logger.debug("Loading training data in {}s", (t2 - t1) / 1000.);

		///////////////////////////////////////////////////////////////
		// do predictions:

		PredictToxicityJSONCreator.forGUI = true;

		if (areDashboardStructuresAvailable()) {
			ResolverDb2.assureDbIsOpen();
		}

		DescriptorData dd = goDescriptors(ac);
		return doPredictions(ac, dd, params);

	}

	public static void checkAtomContainer(AtomContainer m) {

		if (MolFileUtilities.HaveBadElement(m)) {
			m.setProperty("Error", "Molecule contains unsupported element");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		} else if (m.getAtomCount() == 1) {
			m.setProperty("Error", "Only one nonhydrogen atom");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		} else if (m.getAtomCount() == 0) {
			m.setProperty("Error", "Number of atoms equals zero");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		} else if (!MolFileUtilities.HaveCarbon(m)) {
			m.setProperty("Error", "Molecule does not contain carbon");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		}

		AtomContainerSet moleculeSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);
		if (moleculeSet.getAtomContainerCount() > 1) {
//			m.setProperty("Error","Multiple molecules, largest fragment retained");
			m.setProperty("Error", "Multiple molecules");
			m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
		}

		if (m.getProperty("Error") == null)
			m.setProperty("Error", "");

	}

	public static AtomContainer prepareSmilesMolecule(String Smiles) {

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		AtomContainer m = null;
		try {
			// m=sp.parseSmiles(Smiles);
			m = (AtomContainer) sp.parseSmiles(Smiles);
			

			m.setProperty("Error", "");

			if (Smiles.indexOf(".") > -1) {
				m.setProperty("Error", "Molecules can only contain one fragment");
				m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
			} else {
				checkAtomContainer(m);
			}

		} catch (org.openscience.cdk.exception.InvalidSmilesException e) {
			m = new AtomContainer();
			String error = e.getMessage() + ", SMILES=" + Smiles;
			m.setProperty("Error", error);
			m.setProperty("ErrorCode", ERROR_CODE_STRUCTURE_ERROR);
			logger.error(error);
		}
		return m;
	}

	public static AtomContainer getMoleculeFromIdentifier(String identifier) {
		//			AtomContainerSet acs=null;
		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup(identifier.trim());
		
		if (recs.size()>0)
			return TaskStructureSearch.getMoleculeFromDSSToxRecords(recs);
		else 
			return null;
	}
	
	public static AtomContainer loadSMILES(String smiles) {

		AtomContainer m = prepareSmilesMolecule(smiles);

		String ID;

		if (m.getProperty("ErrorCode") == WebTEST.ERROR_CODE_STRUCTURE_ERROR) {
			ID = "C_" + System.currentTimeMillis();
			m.setProperty("CAS", ID);
			return m;
		}

		ArrayList<DSSToxRecord> recs = ResolverDb2.lookupByAtomContainer(m);

		if (recs.size() == 0) {		
			ResolverDb2.assignRecordByStructureNotInDB(m);	
		} else {
			DSSToxRecord.assignDSSToxInfoFromFirstRecord(m, recs);
		}

		return m;
	}

	/**
	 * This version doesnt loadTrainingData (done previously so that GUI can display
	 * loading progress better)
	 * 
	 * @param ac
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<TESTPredictedValue> go2(boolean areDashboardStructuresAvailable, AtomContainer ac, DescriptorData dd, CalculationParameters params)
			throws Exception {
		Statement statRecords = null;
		PredictToxicityJSONCreator.forGUI = true;

//		dashboardStructuresAvailable=false;
		
		if (areDashboardStructuresAvailable) {
			ResolverDb2.assureDbIsOpen();
		}

		return doPredictions(ac, dd, params);
	}

}
