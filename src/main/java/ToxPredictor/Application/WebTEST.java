package ToxPredictor.Application;

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
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.CsvResultsWriter;
import ToxPredictor.Utilities.CsvWriter;
import ToxPredictor.Utilities.FileUtils;
import ToxPredictor.Utilities.FormatUtils;
import ToxPredictor.Utilities.HtmlUtils;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.IndigoUtilities;
import ToxPredictor.Utilities.JsonResultsWriter;
import ToxPredictor.Utilities.MemUtils;
import ToxPredictor.Utilities.ResultsWriter;
import ToxPredictor.Utilities.SdfResultsWriter;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.Utilities.XmlResultsWriter;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.MolFileUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.NotImplementedException;
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

import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class WebTEST {
	private static final Logger logger = LogManager.getLogger(WebTEST.class);

	public static final String ERROR_CODE_STRUCTURE_ERROR = "SE";
	private static final String ERROR_CODE_APPLICABILITY_DOMAIN_ERROR = "AD";
	private static final String ERROR_CODE_DESCRIPTOR_CALCULATION_ERROR = "DE";
	private static final String ERROR_CODE_APPLICATION_ERROR = "AE";

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
	public static boolean createDetailedConsensusReport = false;// create files for each method and detailed descriptor
																// files
	public static boolean deleteExtraFiles = true;
	public static boolean usePreviousDescriptors = false;// if true will use previously calculated descriptors json file
															// to speed up calculations

	final static long minFileAgeHours = 1000000000;// never overwrite

	final static int outputDatabaseFormat = 2;// 1 = tsv format, 2 = csv format

	final static boolean compressDescriptorsInDB = true;// should we zip the descriptors json string in the database

	///////////////////////////////////////////////////////

	static InstanceUtilities iu = new InstanceUtilities();
	static MolFileUtilities mfu = new MolFileUtilities();

//	private final String NCCT_ID_dataFile = "data/NCCT_ID_data.txt";
//	static Hashtable<String, ChemistryDashboardRecord> htChemistryDashboardInfo = null;

	public static final String DB_Path_NCCT_ID_Records = "databases/NCCT_ID.db";
//	public static Statement statNCCT_ID_Records = MySQL_DB.getStatement(DB_Path_NCCT_ID_Records);//commented out by TMM- use ResolverDB based on ncct_lookup.db instead

	// ****************************************************************************************************
	// Variables for progress update in fraMain8:
//	private static boolean done;//tells fraMain8 if it's done; 
//	private static boolean stop=false;
//	private static int current;//current % completed for fraMain8 status bar
//	private static String statMessage;//current message to display in fraMain8 progress bar

	public WebTEST() {
//		if (new File(NCCT_ID_dataFile).exists())
//			htChemistryDashboardInfo = GetChemistryDashboardIDs.getGSIDLookupTable4(NCCT_ID_dataFile);
//		else
//			logger.debug("NCCT IDs registry file ({}) does not exist - skip loading...", NCCT_ID_dataFile);

	}

	public static AtomContainerSet LoadFromSMI(String smi) {

		AtomContainerSet moleculeSet = new AtomContainerSet();
		String delimiter = "";

		try {
			BufferedReader br = new BufferedReader(
					smi.indexOf('\n') >= 0 ? new StringReader(smi) : new FileReader(smi));
			while (true) {
				String Line = br.readLine();

				if (Line == null)
					break;
				if (Line.trim().equals(""))
					break;

				// if (delimiter.equals("")) {
				if (Line.indexOf("\t") > -1)
					delimiter = "\t";
				else if (Line.indexOf(",") > -1)
					delimiter = ",";
				else if (Line.indexOf(" ") > -1)
					delimiter = " ";
				else {
					// we will look up CAS later or assign one based on
					// the current time and the row number
				}

				String ID = null;
				String Smiles = null;
				
				
				
				
				if (!delimiter.equals("")) {
					List<String> l = ToxPredictor.Utilities.Utilities.Parse3(Line, delimiter);

//					System.out.println(l.size());
					
					
					if (l.size() >= 2) {
						Smiles = (String) l.get(0);
						ID = (String) l.get(1);
					} else {
						return null;
					}
				} else {
					Smiles = Line;
				}
				
				Smiles=Smiles.trim();
				
//				System.out.println("delimiter=*"+delimiter+"*");
//				System.out.println("smiles=*"+Smiles+"*");
				
				AtomContainer m = loadSMILES(Smiles);
				
				
				if ( !StringUtils.isEmpty(ID) ) {
					ID = ID.replace("\"", "_");
					ID = ID.replace("/", "_");
					ID = ID.replace(":", "_");
					ID = ID.replace("*", "_");
					ID = ID.replace("?", "_");
					ID = ID.replace("<", "_");
					ID = ID.replace(">", "_");
					ID = ID.replace("|", "_");
					m.setProperty("CAS", ID);
				}
				
				moleculeSet.addAtomContainer(m);
			}

			br.close();
		} catch (Exception ex) {
			logger.catching(ex);
		}

		return moleculeSet;
	}

	public static AtomContainer prepareSmilesMolecule(String Smiles) {

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		AtomContainer m = null;
		try {
			// m=sp.parseSmiles(Smiles);
			m = (AtomContainer) sp.parseSmiles(Smiles);
			m.setProperty("Error", "");

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

			if (Smiles.indexOf(".") > -1) {
				m.setProperty("Error", "Molecules can only contain one fragment");
				m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
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

	public static AtomContainerSet LoadFromSMILES(String smiles) {
		AtomContainerSet moleculeSet = new AtomContainerSet();
		AtomContainer m = loadSMILES(smiles);
		moleculeSet.addAtomContainer(m);
		return moleculeSet;
	}

	public static AtomContainer loadSMILES(String smiles) {
		
		if (smiles.isEmpty()) {
			AtomContainer m = new AtomContainer();
			String error = "smiles blank";
			m.setProperty("Error", error);
			m.setProperty("ErrorCode", ERROR_CODE_STRUCTURE_ERROR);
			return m;
		}
		
		if (smiles.contains(":") || smiles.contains("*")) {
			AtomContainer m = new AtomContainer();
			String error = "not completely defined substance";
			m.setProperty("Error", error);
			m.setProperty("ErrorCode", ERROR_CODE_STRUCTURE_ERROR);
			return m;
		}


		
		String ID;
		ArrayList<DSSToxRecord> recs = ResolverDb.lookupBySMILES(smiles);
		if ( recs.size() == 0 ) {
			ID = "C_" + System.currentTimeMillis(); // generates unique ID so that output files can
									// be stored in unique folders (i.e. dont
									// get overwritten each time a new smiles
									// file is ran)
		}
		else {
			ID = recs.get(0).cas;
		}

		AtomContainer m = prepareSmilesMolecule(smiles);

		m.setProperty("CAS", ID);
		return m;
	}

	// TODO: Look into lazy loading...
	public static AtomContainerSet LoadFromSDF(String sdf) throws FileNotFoundException, IOException {
		AtomContainerSet moleculeSetFromFile = sdf.indexOf('\n') >= 0 ? MolFileUtilities.LoadFromSdfString(sdf)
				: MolFileUtilities.LoadFromSDF3(sdf);
		AtomContainerSet moleculeSetExport = new AtomContainerSet(); // create
																		// new
																		// molecule
																		// set
																		// since
																		// cant
																		// replace
																		// molecules
																		// in
																		// the
																		// vector
																		// (i.e.
																		// there's
																		// no
																		// setMolecule
																		// method)

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

	private static AtomContainerSet LoadFromMOL(String mol) throws IOException {
		AtomContainerSet moleculeSetFromFile = MolFileUtilities.LoadFromSdfString(mol);

		AtomContainer m = (AtomContainer) moleculeSetFromFile.getAtomContainer(0);
		m = prepareMolecule(0, m);

		AtomContainerSet moleculeSetExport = new AtomContainerSet(); // create
																		// new
																		// molecule
																		// set
																		// since
																		// cant
																		// replace
																		// molecules
																		// in
																		// the
																		// vector
																		// (i.e.
																		// there's
																		// no
																		// setMolecule
																		// method)
		moleculeSetExport.addAtomContainer(m);

		return moleculeSetExport;
	}

	private static AtomContainer prepareMolecule(int i, AtomContainer m) {
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

			AtomContainerSet moleculeSet2 = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

			if (moleculeSet2.getAtomContainerCount() > 1) {
				// m.setProperty("Error","Multiple molecules, largest fragment
				// retained");
				m.setProperty("Error", "Multiple molecules");
				m.setProperty("ErrorCode", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR);
			}
		} else {
			// otherwise preserve error stored in sdf file
		}

//		System.out.println(m.getProperty("CAS")+"\t"+m.getProperty("Error"));

		// df.Normalize(m);//needed?

		// m = (Molecule)MolFileUtilities.CheckForAromaticBonds(m);//not needed?
		return m;
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

		// if (method.equals(TESTConstants.ChoiceLDA)) {
		// LoadLDAFiles();
		// }

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod)
				|| method.equals(TESTConstants.ChoiceSingleModelMethod)
				|| method.equals(TESTConstants.ChoiceConsensus)) {
			LoadHierarchicalXMLFile(endpoint);
		}

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod)
				|| method.equals(TESTConstants.ChoiceConsensus)) {
			LoadFragmentXMLFile(endpoint);
		}

		// Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
		// Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);

		// trainingDataSet2d.setClassIndex(classIndex);
		// trainingDataSetFrag.setClassIndex(classIndex);
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

				logger.debug("Loading fragments XML...");

				ht_allResultsFrag.put(endpoint,
						readAllResultsFormat2_2(xmlFileName, ht_ccTrainingFrag.get(endpoint), true));
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	static boolean HaveFileInJar(String filepath) {
		try {
			java.io.InputStream is = new WebTEST().getClass().getClassLoader().getResourceAsStream(filepath);
			is.read();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void LoadHierarchicalXMLFile(String endpoint) {
		try {
			if (ht_allResults.get(endpoint) == null) {
				String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);
				String xmlFileName = abbrev + "/" + abbrev + "_training_set-2d.xml";
				if (!HaveFileInJar(xmlFileName))
					return;

				logger.debug("Loading cluster data file...");
				ht_allResults.put(endpoint, readAllResultsFormat2_2(xmlFileName, ht_ccTraining.get(endpoint), true));
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	static AllResults readAllResultsFormat2_2(String resultsXML, Instances trainingDataset,
			boolean isXMLFileInsideJar) {

		try {
			AllResultsXMLReader arxr = new AllResultsXMLReader();
			AllResults ar = arxr.readAllResults(resultsXML, trainingDataset, true);
			return ar;

		} catch (Exception e) {
			logger.catching(e);
		}
		return null;
	}

	private static void writeCsvHeader(CsvWriter csv, String endpoint, String method) {
		csv.printField("CAS");
		csv.printField("gsid");
		csv.printField("DSSTOXSID");
		csv.printField("DSSTOXCID");

		csv.printField("ExpToxValue");

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
			csv.printField("Hierarchical");
		} else if (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
			csv.printField("NearestNeighbor");
		} else if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
			csv.printField("SingleModel");
		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
			csv.printField("GroupContribution");
		} else if (method.equals(TESTConstants.ChoiceLDA)) {
			csv.printField("LDA");
		} else if (method.equals(TESTConstants.ChoiceConsensus)) {
			csv.printField("Hierarchical");
			if (TESTConstants.haveSingleModelMethod(endpoint))
				csv.printField("SingleModel");
			if (TESTConstants.haveGroupContributionMethod(endpoint))
				csv.printField("GroupContribution");
			csv.printField("NearestNeighbor");
			csv.printField("Consensus");
		}

		csv.newRecord();
	}

	/*
	 * private static TESTPredictedValue calculate(AtomContainer m, String endpoint,
	 * String method, Set<WebReportType> reportTypes, DescriptorFactory df) {
	 * 
	 * // PredictToxicityFDA ptFDA = new PredictToxicityFDA();
	 * PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
	 * PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
	 * // PredictToxicityRandomForrestCaesar ptRFC= new //
	 * PredictToxicityRandomForrestCaesar(); PredictToxicityLDA ptLDA = new
	 * PredictToxicityLDA();
	 * 
	 * TESTPredictedValue v = null; String pdfPath = null;
	 * 
	 * long start = System.currentTimeMillis();
	 * 
	 * // TODO: move following to variables that are passed to calculate method
	 * boolean isBinaryEndpoint = TESTConstants.isBinary(endpoint); boolean
	 * isLogMolarEndpoint = TESTConstants.isLogMolar(endpoint); String abbrev =
	 * TESTConstants.getAbbrevEndpoint(endpoint);
	 * 
	 * DescriptorData dd = new DescriptorData(); dd.CAS = (String)
	 * m.getProperty("CAS"); String CAS = dd.CAS; String gsid =
	 * m.getProperty("gsid"); String DSSTOXSID =
	 * m.getProperty("dsstox_compound_id"); String DSSTOXCID =
	 * m.getProperty("dsstox_substance_id");
	 * 
	 * // if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null) &&
	 * htChemistryDashboardInfo != null) { if ((gsid == null || DSSTOXSID == null ||
	 * DSSTOXCID == null)) { // try to look up based on CAS //
	 * ChemistryDashboardRecord rec = GetChemistryDashboardIDs.get(CAS);
	 * ChemistryDashboardRecord rec =
	 * ChemistryDashboardRecord.lookupDashboardRecord("casrn",
	 * CAS,statNCCT_ID_Records); if (rec == null) logger.debug("Cannot resolve {}",
	 * CAS); else { if (gsid == null) gsid = rec.gsid; if (DSSTOXSID == null)
	 * DSSTOXSID = rec.dsstox_substance_id; if (DSSTOXCID == null) DSSTOXCID =
	 * rec.dsstox_compound_id; } }
	 * 
	 * // ******************************************************************
	 * 
	 * String strOutputFolder = null; String strOutputFolderStructureData = null;
	 * 
	 * if (!reportTypes.isEmpty()) { File fileOutputFolder = new
	 * File(reportFolderName); String strOutputFolder1 =
	 * fileOutputFolder.getAbsolutePath() + File.separator + "ToxRuns"; File
	 * fileOutputFolder1 = new File(strOutputFolder1); if
	 * (!fileOutputFolder1.exists()) fileOutputFolder1.mkdirs();
	 * 
	 * String strOutputFolder2 = strOutputFolder1 + File.separator + "ToxRun_" +
	 * dd.CAS; File fileOutputFolder2 = new File(strOutputFolder2); if
	 * (!fileOutputFolder2.exists()) fileOutputFolder2.mkdirs();
	 * 
	 * String strOutputFolder3 = strOutputFolder2 + File.separator + endpoint; File
	 * fileOutputFolder3 = new File(strOutputFolder3);
	 * 
	 * strOutputFolder = strOutputFolder3;
	 * 
	 * if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) { if
	 * (!fileOutputFolder3.exists()) fileOutputFolder3.mkdirs(); }
	 * 
	 * strOutputFolderStructureData = strOutputFolder2 + File.separator +
	 * "StructureData"; File osdf = new File(strOutputFolderStructureData); if
	 * (!osdf.exists()) osdf.mkdirs();
	 * 
	 * }
	 * 
	 * // ******************************************************************
	 * 
	 * boolean Use3D = false; df.Calculate3DDescriptors = Use3D; dd.ThreeD = Use3D;
	 * 
	 * // calculate 2D and non quantum 3D descriptors: int descresult=-1;
	 * 
	 * if (usePreviousDescriptors) {
	 * 
	 * String filePathDescriptors=strOutputFolderStructureData+File.separator+
	 * "descriptorValues.json"; File descFile=new File(filePathDescriptors); //
	 * System.out.println(filePathDescriptors); //
	 * System.out.println(descFile.exists());
	 * 
	 * if (descFile.exists()) { dd=DescriptorData.loadFromJSON(descFile);
	 * dd.ThreeD=false; dd.WriteToFile(strOutputFolderStructureData); //
	 * System.out.println(dd.CAS+"\t"+dd.x0); if (dd!=null) descresult=0;
	 * 
	 * if(Math.abs(dd.MW-dd.MW_Frag)>1) { descresult=-1;// recalculate descriptors
	 * just in case }
	 * 
	 * } }
	 * 
	 * if (descresult==-1) { if (!reportTypes.isEmpty()) { descresult =
	 * df.CalculateDescriptors(m, dd, true, overwriteFiles, true,
	 * strOutputFolderStructureData); df.WriteJSON(dd,
	 * strOutputFolderStructureData+"/descriptorValues.json", true); } else {
	 * descresult = df.CalculateDescriptors(m, dd, true); } }
	 * 
	 * // just in case we are running a chemical without gsid (e.g. user drawn
	 * structure): if (!reportTypes.isEmpty() && gsid == null) {
	 * ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, "structure",
	 * strOutputFolderStructureData);// dont }
	 * 
	 * 
	 * if (descresult == -1) { // done=true; // commented out: dont kill rest of run
	 * for batch logger.error("Error calculating descriptors for {}", dd.CAS);
	 * 
	 * // TODO- create web report for this chemical that mentions this // error
	 * 
	 * return v; }
	 * 
	 * if (!reportTypes.isEmpty() && createDetailedConsensusReport)
	 * df.WriteOut2DDescriptors(m, dd, strOutputFolderStructureData, del);
	 * 
	 * // testchemical.png
	 * 
	 * // dont need testchemical.png since have structure.png: // if (writeFiles) //
	 * ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, //
	 * "testchemical",strOutputFolderStructureData);
	 * 
	 * if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
	 * 
	 * // array to store predictions for all methods for consensus method: ArrayList
	 * predictedToxicities = new ArrayList(); ArrayList predictedUncertainties = new
	 * ArrayList();
	 * 
	 * AllResults allResults = ht_allResults.get(endpoint);// shortcut to // results
	 * // object AllResults allResultsFrag = ht_allResultsFrag.get(endpoint); //
	 * shortcut // to // results // object
	 * 
	 * Instances trainingDataSet2d = ht_ccTraining.get(endpoint); Instances
	 * trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint); Instances
	 * testDataSet2d = ht_ccPrediction.get(endpoint);
	 * 
	 * // String // ToxFieldName=trainingDataSet2d.attribute(classIndex).name();
	 * 
	 * String ToxFieldName = "Tox";
	 * 
	 * java.util.Hashtable ht = dd.CreateDataHashtable(ToxFieldName, true, true,
	 * false, false, false);
	 * 
	 * // Create instances for test chemical (for GCM it will only contain //
	 * fragment descriptors): String[] varArrayFrag =
	 * TaskCalculations.CreateVarListFromTrainingSet(trainingDataSetFrag); Instances
	 * evalInstancesFrag = iu.createInstances(ht, varArrayFrag); String[] varArray2d
	 * = TaskCalculations.CreateVarListFromTrainingSet(trainingDataSet2d); Instances
	 * evalInstances2d = iu.createInstances(ht, varArray2d); Instance evalInstance2d
	 * = evalInstances2d.firstInstance();
	 * 
	 * // System.out.println(evalInstances2d.getDescriptorNames()); //
	 * System.out.println(evalInstance2d.toString());
	 * 
	 * Lookup.ExpRecord er = TaskCalculations.LookupExpVal(dd.CAS, evalInstance2d,
	 * trainingDataSet2d, testDataSet2d);
	 * 
	 * // **************************************************
	 * 
	 * int result = 0; String statMessage = "Calculating " + endpoint + "..."; //
	 * statMessage += "Molecule ID = " + dd.CAS;
	 * 
	 * // statMessage += "Molecule ID = " + dd.CAS+" (#"+(molNum+1)+")"; statMessage
	 * += "Molecule ID = " + dd.CAS; // statMessage += "Molecule #" + Index;
	 * 
	 * // System.out.println(statMessage);
	 * 
	 * // ******************************************************
	 * 
	 * double predToxVal = null; double predToxUnc = 1;// TODO: add code to
	 * calculate this
	 * 
	 * // if (method.equals(TESTConstants.ChoiceFDAMethod)) { // result =
	 * ptFDA.CalculateToxicity(CAS, method, // endpoint,isBinaryEndpoint,
	 * isLogMolarEndpoint,abbrev, // strOutputFolder, DescriptorSet,
	 * evalInstances2d, // trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag, //
	 * useFragmentsConstraint, this, er); // predToxVal = ptFDA.predToxVal; // }
	 * else if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) // {
	 * 
	 * long descriptorCalculationTime = System.currentTimeMillis() - start;
	 * totalDescriptorCalculationTime += descriptorCalculationTime; start =
	 * System.currentTimeMillis(); long reportGenerationTime = 0;
	 * 
	 * ReportOptions options = new ReportOptions(); options.reportBase =
	 * strOutputFolder; options.embedImages = true;
	 * 
	 * if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) { result =
	 * ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint,
	 * isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResults,
	 * evalInstances2d, evalInstance2d, trainingDataSet2d, trainingDataSet2d,
	 * testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er,
	 * minFileAgeHours, options); predToxVal = ptH.predToxVal; } else if
	 * (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) { result =
	 * ptNN.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint,
	 * isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, evalInstances2d,
	 * trainingDataSet2d, testDataSet2d, dd.MW, er, options); predToxVal =
	 * ptNN.predToxVal; } else if
	 * (method.equals(TESTConstants.ChoiceSingleModelMethod)) { result =
	 * ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint,
	 * isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResults,
	 * evalInstances2d, evalInstance2d, trainingDataSet2d, trainingDataSet2d,
	 * testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er,
	 * minFileAgeHours, options); predToxVal = ptH.predToxVal; } else if
	 * (method.equals(TESTConstants.ChoiceGroupContributionMethod)) { result =
	 * ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint,
	 * isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResultsFrag,
	 * evalInstancesFrag, evalInstance2d, trainingDataSetFrag, trainingDataSet2d,
	 * testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er,
	 * minFileAgeHours, options); predToxVal = ptH.predToxVal; // } else if //
	 * (method.equals(TESTConstants.ChoiceRandomForrestCaesar)) { //
	 * result=ptRFC.CalculateToxicity(CAS, method, // endpoint,isBinaryEndpoint,
	 * isLogMolarEndpoint,abbrev, // strOutputFolder, DescriptorSet,
	 * evalInstances2d, // trainingDataSet2d, testDataSet2d, dd.MW, // this, er,ht);
	 * // predToxVal = ptRFC.predToxVal;
	 * 
	 * } else if (method.equals(TESTConstants.ChoiceLDA)) { result =
	 * ptLDA.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint,
	 * isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, htAllResultsMOA,
	 * htTrainingSetsMOA, htAllResultsLC50, htTrainingSetsLC50, evalInstances2d,
	 * trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint,
	 * er, vecMOA, options);
	 * 
	 * predToxVal = ptLDA.predToxVal; } else if
	 * (method.equals(TESTConstants.ChoiceConsensus)) {
	 * 
	 * String strOutputFolder2 = null; if (createDetailedConsensusReport)
	 * strOutputFolder2 = strOutputFolder;
	 * 
	 * // TODO move following into a subroutine: // Hierarchical: result =
	 * ptH.CalculateToxicity(CAS, TESTConstants.ChoiceHierarchicalMethod, endpoint,
	 * isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2,
	 * DescriptorSet, allResults, evalInstances2d, evalInstance2d,
	 * trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
	 * useFragmentsConstraint, er, minFileAgeHours, options);
	 * predictedToxicities.add(ptH.predToxVal);
	 * 
	 * predictedUncertainties.add(ptH.predToxUnc);
	 * 
	 * // Single Model if (TESTConstants.haveSingleModelMethod(endpoint)) { //
	 * System.out.println("SM r2="+orSM.getR2()); result =
	 * ptH.CalculateToxicity(CAS, TESTConstants.ChoiceSingleModelMethod, endpoint,
	 * isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2,
	 * DescriptorSet, allResults, evalInstances2d, evalInstance2d,
	 * trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
	 * useFragmentsConstraint, er, minFileAgeHours, options);
	 * predictedToxicities.add(ptH.predToxVal);
	 * predictedUncertainties.add(ptH.predToxUnc); }
	 * 
	 * // Group contribution: if
	 * (TESTConstants.haveGroupContributionMethod(endpoint)) { result =
	 * ptH.CalculateToxicity(CAS, TESTConstants.ChoiceGroupContributionMethod,
	 * endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2,
	 * DescriptorSet, allResultsFrag, evalInstancesFrag, evalInstance2d,
	 * trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
	 * useFragmentsConstraint, er, minFileAgeHours, options);
	 * 
	 * predictedToxicities.add(ptH.predToxVal);
	 * predictedUncertainties.add(ptH.predToxUnc); }
	 * 
	 * // //FDA: // result = ptFDA.CalculateToxicity(CAS, //
	 * TESTConstants.ChoiceFDAMethod, endpoint,isBinaryEndpoint, //
	 * isLogMolarEndpoint,abbrev, // strOutputFolder, DescriptorSet,
	 * evalInstances2d, // trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag, //
	 * useFragmentsConstraint, this, er); //
	 * predictedToxicities.add(ptFDA.predToxVal); //
	 * predictedUncertainties.add(ptFDA.predToxUnc);
	 * 
	 * // Nearest neighbor: result = ptNN.CalculateToxicity(CAS,
	 * TESTConstants.ChoiceNearestNeighborMethod, endpoint, isBinaryEndpoint,
	 * isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, evalInstances2d,
	 * trainingDataSet2d, testDataSet2d, dd.MW, er, options);
	 * predictedToxicities.add(ptNN.predToxVal);
	 * predictedUncertainties.add(ptNN.predToxUnc);
	 * 
	 * // for (int i=0;i<methods.size();i++) { //
	 * System.out.println(methods.get(i)+"\t"+predictedToxicities.get(i)); // }
	 * 
	 * predToxVal = calculateConsensusToxicity(predictedToxicities); predToxUnc =
	 * 1;// TODO: add code to calculate this
	 * 
	 * predictedToxicities.add(predToxVal);
	 * 
	 * long time = System.currentTimeMillis();
	 * 
	 * if (!reportTypes.isEmpty()) {
	 * 
	 * double[] Mean = trainingDataSet2d.getMeans(); double[] StdDev =
	 * trainingDataSet2d.getStdDevs();
	 * 
	 * Hashtable<Double, Instance> htTestMatch =
	 * TaskCalculations.FindClosestChemicals(evalInstance2d, testDataSet2d, true,
	 * true, true, Mean, StdDev);
	 * 
	 * Hashtable<Double, Instance> htTrainMatch =
	 * TaskCalculations.FindClosestChemicals(evalInstance2d, trainingDataSet2d,
	 * true, true, true, Mean, StdDev);
	 * 
	 * PredictToxicityWebPageCreator ptwc = new PredictToxicityWebPageCreator();
	 * PredictToxicityJSONCreator jsonCreator = new PredictToxicityJSONCreator();
	 * PredictToxicityWebPageCreatorFromJSON htmlCreator = new
	 * PredictToxicityWebPageCreatorFromJSON();
	 * 
	 * ArrayList<String> methods = TaskCalculations.getMethods(endpoint);
	 * 
	 * PredictionResults predictionResults = null; if
	 * (reportTypes.contains(WebReportType.JSON) ||
	 * reportTypes.contains(WebReportType.HTML) ||
	 * reportTypes.contains(WebReportType.PDF)) { predictionResults =
	 * jsonCreator.writeConsensusResultsJSON(predToxVal, predToxUnc, method, CAS,
	 * endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, dd.MW, "OK",
	 * htTestMatch, htTrainMatch, methods, predictedToxicities,
	 * predictedUncertainties, createDetailedConsensusReport, gsid,
	 * options,statNCCT_ID_Records); }
	 * 
	 * if (reportTypes.contains(WebReportType.HTML) ||
	 * reportTypes.contains(WebReportType.PDF)) {
	 * 
	 * String outputfilename="PredictionResultsConsensus.html"; String htmlFilePath
	 * = Paths.get(options.reportBase, outputfilename).toFile().getAbsolutePath();
	 * htmlCreator.WriteConsensusResultsWebPages(predictionResults, htmlFilePath);
	 * if (reportTypes.contains(WebReportType.PDF) &&
	 * !Strings.isEmpty(htmlFilePath)) { try { pdfPath =
	 * HtmlUtils.HtmlToPdf(htmlFilePath, FileUtils.replaceExtension(htmlFilePath,
	 * ".pdf")); } catch (IOException e) { logger.catching(e); } } }
	 * 
	 * if (deleteExtraFiles) { deleteExtraFiles(options); } }
	 * 
	 * reportGenerationTime = System.currentTimeMillis() - time;
	 * 
	 * } // end choice consensus
	 * 
	 * if (result == -1) { return v; }
	 * 
	 * long predictionGenerationTime = System.currentTimeMillis() - start -
	 * reportGenerationTime; totalReportGenerationTime += reportGenerationTime;
	 * totalPredictionGenerationTime += predictionGenerationTime;
	 * 
	 * logger.info("{}\t{}\t{}\t\t{}\t{}\t{}", dd.CAS,
	 * FormatUtils.toD3(er.expToxValue), FormatUtils.toD3(predToxVal),
	 * descriptorCalculationTime, predictionGenerationTime, reportGenerationTime);
	 * 
	 * 
	 * if (!isBinaryEndpoint) { v = getTESTPredictedValue(endpoint, method, CAS,
	 * er.expToxValue, predToxVal, dd.MW, del, ""); } else { v =
	 * getTESTPredictedValueBinary(endpoint, method, CAS, er.expToxValue,
	 * predToxVal, dd.MW, del, ""); }
	 * 
	 * 
	 * if (v != null && pdfPath != null) v.reportPath = pdfPath;
	 * 
	 * // if (method.equals(TESTConstants.ChoiceConsensus)) { // //TODO //
	 * this.WriteToxicityResultsForChemicalAllMethods(fw3,index,dd.CAS,er.
	 * expToxValue,methods,predictedToxicities,dd.MW,del,""); // }
	 * 
	 * } else {// Descriptors // System.out.println(dd.CAS + "\t"); // TODO //
	 * WriteDescriptorResultsForChemicalToWebPage(fw,index,CAS,""); }
	 * 
	 * return v; }
	 */

	private static List<TESTPredictedValue> calculate(DescriptorData dd, String endpoint, String method,
			Set<WebReportType> reportTypes) {

		// error will be returned to client
		// final String invalidEndpointMethodCombination = checkParams(endpoint,
		// method);
		// if (invalidEndpointMethodCombination != null) {
		// logger.warn(invalidEndpointMethodCombination);
		// return null;
		// }

		List<TESTPredictedValue> res = new ArrayList<>();

		String CAS = dd.ID;

		// PredictToxicityFDA ptFDA = new PredictToxicityFDA();
		PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
		PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
		// PredictToxicityRandomForrestCaesar ptRFC= new
		// PredictToxicityRandomForrestCaesar();
		PredictToxicityLDA ptLDA = new PredictToxicityLDA();

		TESTPredictedValue v = null;
		String pdfPath = null;

		if (!dd.Error.equals("OK")) {
			return res;
		}

		long start = System.currentTimeMillis();

		// TODO: move following to variables that are passed to calculate method
		boolean isBinaryEndpoint = TESTConstants.isBinary(endpoint);
		boolean isLogMolarEndpoint = TESTConstants.isLogMolar(endpoint);
		String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);

		String strOutputFolder = null;

		if (!reportTypes.isEmpty()) {
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

		}

		// array to store predictions for all methods for consensus method:
		List<Double> predictedToxicities = new ArrayList<>();
		List<Double> predictedUncertainties = new ArrayList<>();

		AllResults allResults = ht_allResults.get(endpoint);// shortcut to
		// results
		// object
		AllResults allResultsFrag = ht_allResultsFrag.get(endpoint); // shortcut
		// to
		// results
		// object

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

		// System.out.println(evalInstances2d.getDescriptorNames());
		// System.out.println(evalInstance2d.toString());

		Lookup.ExpRecord er = TaskCalculations.LookupExpVal(dd.ID, evalInstance2d, trainingDataSet2d, testDataSet2d);

		// **************************************************

		int result = 0;
		String statMessage = "Calculating " + endpoint + "...";
		// statMessage += "Molecule ID = " + dd.CAS;

		// statMessage += "Molecule ID = " + dd.CAS+" (#"+(molNum+1)+")";
		statMessage += "Molecule ID = " + dd.ID;
		// statMessage += "Molecule #" + Index;

		// System.out.println(statMessage);

		// ******************************************************

		Double predToxVal = null;
		double predToxUnc = 1;// TODO: add code to calculate this

		// if (method.equals(TESTConstants.ChoiceFDAMethod)) {
		// result = ptFDA.CalculateToxicity(CAS, method,
		// endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
		// strOutputFolder, DescriptorSet, evalInstances2d,
		// trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
		// useFragmentsConstraint, this, er);
		// predToxVal = ptFDA.predToxVal;
		// } else if (method.equals(TESTConstants.ChoiceHierarchicalMethod))
		// {

		long descriptorCalculationTime = System.currentTimeMillis() - start;
		totalDescriptorCalculationTime += descriptorCalculationTime;
		start = System.currentTimeMillis();
		long reportGenerationTime = 0;

		ReportOptions options = new ReportOptions();
		options.reportBase = strOutputFolder;
		options.embedImages = true;

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
			result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
					strOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d, trainingDataSet2d,
					trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours,
					options);
			predToxVal = ptH.predToxVal;
		} else if (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
			result = ptNN.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
					strOutputFolder, DescriptorSet, evalInstances2d, trainingDataSet2d, testDataSet2d, dd.MW, er,
					options);
			predToxVal = ptNN.predToxVal;
		} else if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
			if (TESTConstants.haveSingleModelMethod(endpoint)) {
				res.add(getTESTPredictionError(endpoint, TESTConstants.ChoiceSingleModelMethod, CAS,
						"Single model method is unavailable for this endpoint", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
				return res;
			}
			result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
					strOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d, trainingDataSet2d,
					trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours,
					options);
			predToxVal = ptH.predToxVal;
		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
			if (TESTConstants.haveGroupContributionMethod(endpoint)) {
				res.add(getTESTPredictionError(endpoint, TESTConstants.ChoiceGroupContributionMethod, CAS,
						"Group contribution method is unavailable for this endpoint",
						ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
				return res;
			}
			result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
					strOutputFolder, DescriptorSet, allResultsFrag, evalInstancesFrag, evalInstance2d,
					trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint,
					er, minFileAgeHours, options);
			predToxVal = ptH.predToxVal;
			// } else if
			// (method.equals(TESTConstants.ChoiceRandomForrestCaesar)) {
			// result=ptRFC.CalculateToxicity(CAS, method,
			// endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
			// strOutputFolder, DescriptorSet, evalInstances2d,
			// trainingDataSet2d, testDataSet2d, dd.MW,
			// this, er,ht);
			// predToxVal = ptRFC.predToxVal;

		} else if (method.equals(TESTConstants.ChoiceLDA)) {
			result = ptLDA.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
					strOutputFolder, DescriptorSet, htAllResultsMOA, htTrainingSetsMOA, htAllResultsLC50,
					htTrainingSetsLC50, evalInstances2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
					useFragmentsConstraint, er, vecMOA, options);

			predToxVal = ptLDA.predToxVal;
		} else if (method.equals(TESTConstants.ChoiceConsensus)) {

			String strOutputFolder2 = null;
			if (createDetailedConsensusReport)
				strOutputFolder2 = strOutputFolder;

			// TODO move following into a subroutine:
			// Hierarchical:
			result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceHierarchicalMethod, endpoint, isBinaryEndpoint,
					isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, allResults, evalInstances2d,
					evalInstance2d, trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
					useFragmentsConstraint, er, minFileAgeHours, options);
			predictedToxicities.add(ptH.predToxVal);
			predictedUncertainties.add(ptH.predToxUnc);
			res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceHierarchicalMethod, CAS, er.expToxValue,
					ptH.predToxVal, dd.MW, "", isBinaryEndpoint));

			// Single Model
			if (TESTConstants.haveSingleModelMethod(endpoint)) {
				// System.out.println("SM r2="+orSM.getR2());
				result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceSingleModelMethod, endpoint, isBinaryEndpoint,
						isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, allResults, evalInstances2d,
						evalInstance2d, trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
						useFragmentsConstraint, er, minFileAgeHours, options);
				predictedToxicities.add(ptH.predToxVal);
				predictedUncertainties.add(ptH.predToxUnc);
				res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceSingleModelMethod, CAS, er.expToxValue,
						ptH.predToxVal, dd.MW, "", isBinaryEndpoint));
			} else {
				res.add(getTESTPredictionError(endpoint, TESTConstants.ChoiceSingleModelMethod, CAS,
						"Single model method is unavailable for this endpoint", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
			}

			// Group contribution:
			if (TESTConstants.haveGroupContributionMethod(endpoint)) {
				result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceGroupContributionMethod, endpoint,
						isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, allResultsFrag,
						evalInstancesFrag, evalInstance2d, trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW,
						dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);

				predictedToxicities.add(ptH.predToxVal);
				predictedUncertainties.add(ptH.predToxUnc);
				res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceGroupContributionMethod, CAS,
						er.expToxValue, ptH.predToxVal, dd.MW, "", isBinaryEndpoint));
			} else {
				res.add(getTESTPredictionError(endpoint, TESTConstants.ChoiceGroupContributionMethod, CAS,
						"Group contribution is unavailable for this endpoint", ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
			}

			// //FDA:
			// result = ptFDA.CalculateToxicity(CAS,
			// TESTConstants.ChoiceFDAMethod, endpoint,isBinaryEndpoint,
			// isLogMolarEndpoint,abbrev,
			// strOutputFolder, DescriptorSet, evalInstances2d,
			// trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
			// useFragmentsConstraint, this, er);
			// predictedToxicities.add(ptFDA.predToxVal);
			// predictedUncertainties.add(ptFDA.predToxUnc);

			// Nearest neighbor:
			result = ptNN.CalculateToxicity(CAS, TESTConstants.ChoiceNearestNeighborMethod, endpoint, isBinaryEndpoint,
					isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, evalInstances2d, trainingDataSet2d,
					testDataSet2d, dd.MW, er, options);
			predictedToxicities.add(ptNN.predToxVal);
			predictedUncertainties.add(ptNN.predToxUnc);
			res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceNearestNeighborMethod, CAS, er.expToxValue,
					ptNN.predToxVal, dd.MW, "", isBinaryEndpoint));

			// for (int i=0;i<methods.size();i++) {
			// System.out.println(methods.get(i)+"\t"+predictedToxicities.get(i));
			// }

			predToxVal = calculateConsensusToxicity(predictedToxicities);
			predToxUnc = 1;// TODO: add code to calculate this

			predictedToxicities.add(predToxVal);

			long time = System.currentTimeMillis();

			if (!reportTypes.isEmpty()) {

				double[] Mean = trainingDataSet2d.getMeans();
				double[] StdDev = trainingDataSet2d.getStdDevs();

				Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d,
						testDataSet2d, true, true, true, Mean, StdDev);

				Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d,
						trainingDataSet2d, true, true, true, Mean, StdDev);

//				PredictToxicityWebPageCreator ptwc = new PredictToxicityWebPageCreator();
				PredictToxicityJSONCreator jsonCreator = new PredictToxicityJSONCreator();
				PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();

				List<String> methods = TaskCalculations.getMethods(endpoint);

				PredictionResults predictionResults = null;
				if (reportTypes.contains(WebReportType.JSON) || reportTypes.contains(WebReportType.HTML)
						|| reportTypes.contains(WebReportType.PDF)) {
					

					predictionResults = jsonCreator.writeConsensusResultsJSON(predToxVal, predToxUnc, method, CAS,
							dd.dtxcid,dd.dtxsid,dd.SmilesRan,endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, dd.MW, "OK", htTestMatch,
							htTrainMatch, methods, predictedToxicities, predictedUncertainties,
							createDetailedConsensusReport,  options);
				}

				if (reportTypes.contains(WebReportType.HTML) || reportTypes.contains(WebReportType.PDF)) {
					String outputfilename = "PredictionResultsConsensus.html";
					String htmlFilePath = Paths.get(options.reportBase, outputfilename).toFile().getAbsolutePath();
					htmlCreator.writeConsensusResultsWebPages(predictionResults, htmlFilePath);
					if (reportTypes.contains(WebReportType.PDF) && !Strings.isEmpty(htmlFilePath)) {
						try {
							pdfPath = HtmlUtils.HtmlToPdf(htmlFilePath,
									FileUtils.replaceExtension(htmlFilePath, ".pdf"));
						} catch (IOException e) {
							logger.catching(e);
						}
					}
				}

				if (deleteExtraFiles) {
					deleteExtraFiles(options);
				}
			}

			reportGenerationTime = System.currentTimeMillis() - time;

		} // end choice consensus

		if (result == -1) {
			return res;
		}

		long predictionGenerationTime = System.currentTimeMillis() - start - reportGenerationTime;
		totalReportGenerationTime += reportGenerationTime;
		totalPredictionGenerationTime += predictionGenerationTime;

		logger.info("{}\t{}\t{}\t\t{}\t{}\t{}", dd.ID, FormatUtils.toD3(er.expToxValue), FormatUtils.toD3(predToxVal),
				descriptorCalculationTime, predictionGenerationTime, reportGenerationTime);

		v = getTESTPredictedValue(endpoint, method, CAS, er.expToxValue, predToxVal, dd.MW, "", isBinaryEndpoint);

		if (v != null && pdfPath != null)
			v.reportPath = pdfPath;

		// if (method.equals(TESTConstants.ChoiceConsensus)) {
		// //TODO
		// this.WriteToxicityResultsForChemicalAllMethods(fw3,index,dd.CAS,er.expToxValue,methods,predictedToxicities,dd.MW,del,"");
		// }

		res.add(v);

		return res;
	}

	private static List<TESTPredictedValue> calculate(int molNum, int molCount, AtomContainer m, Writer fw,
			String endpoint, String method, int index, Set<WebReportType> reportTypes, CsvWriter csv,
			DescriptorFactory df, DescriptorData dd) {

		List<TESTPredictedValue> res = new ArrayList<TESTPredictedValue>();

		// PredictToxicityFDA ptFDA = new PredictToxicityFDA();
		PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
		PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
		// PredictToxicityRandomForrestCaesar ptRFC= new
		// PredictToxicityRandomForrestCaesar();
		PredictToxicityLDA ptLDA = new PredictToxicityLDA();

		TESTPredictedValue v = null;
		String pdfPath = null;

		long start = System.currentTimeMillis();

		// TODO: move following to variables that are passed to calculate method
		boolean isBinaryEndpoint = TESTConstants.isBinary(endpoint);
		boolean isLogMolarEndpoint = TESTConstants.isLogMolar(endpoint);
		String abbrev = TESTConstants.getAbbrevEndpoint(endpoint);

		String CAS = dd.ID;
//		String gsid = m.getProperty("gsid");
		String DSSTOXSID = m.getProperty("dsstox_compound_id");
		String DSSTOXCID = m.getProperty("dsstox_substance_id");

//		if ((gsid == null || DSSTOXSID == null || DSSTOXCID == null) && htChemistryDashboardInfo != null) {
		if ((DSSTOXSID == null || DSSTOXCID == null)) {
			// try to look up based on CAS
//			ChemistryDashboardRecord rec = GetChemistryDashboardIDs.get(CAS);

			
			if (ResolverDb.isAvailable()) {			
				if (!Strings.isEmpty(CAS) && !CAS.matches("C\\d*_\\d{8,}")) {				
					ArrayList<DSSToxRecord> recs = ResolverDb.lookupByCAS(CAS);
					if (recs.size()==0) {
						logger.debug("Cannot resolve {}", CAS);
					} else {

						DSSToxRecord rec=recs.get(0);
						
						if (DSSTOXSID == null)
							DSSTOXSID = rec.sid;
						if (DSSTOXCID == null)
							DSSTOXCID = rec.cid;

					}					
				}
			}
						
			
//			ChemistryDashboardRecord rec = ChemistryDashboardRecord.lookupDashboardRecord("casrn", CAS,
//					statNCCT_ID_Records);
//			if (rec == null)
//				logger.debug("Cannot resolve {}", CAS);
//			else {
//				if (gsid == null)
//					gsid = rec.gsid;
//				if (DSSTOXSID == null)
//					DSSTOXSID = rec.dsstox_substance_id;
//				if (DSSTOXCID == null)
//					DSSTOXCID = rec.dsstox_compound_id;
//			}
		}

		if (csv != null) {
			csv.printField(CAS);
//			csv.printField(gsid);
			csv.printField(DSSTOXSID);
			csv.printField(DSSTOXCID);
		}

		// ******************************************************************

		String strOutputFolder = null;
		String strOutputFolderStructureData = null;

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

		// ******************************************************************
		/*
		 * use previously calcuated descriptors boolean Use3D = false;
		 * df.Calculate3DDescriptors = Use3D; dd.ThreeD = Use3D;
		 * 
		 * // calculate 2D and non quantum 3D descriptors: int descresult=-1;
		 * 
		 * if (usePreviousDescriptors) {
		 * 
		 * String filePathDescriptors=strOutputFolderStructureData+File.separator+
		 * "descriptorValues.json"; File descFile=new File(filePathDescriptors); //
		 * System.out.println(filePathDescriptors); //
		 * System.out.println(descFile.exists());
		 * 
		 * if (descFile.exists()) { dd=DescriptorData.loadFromJSON(descFile);
		 * dd.ThreeD=false; dd.WriteToFile(strOutputFolderStructureData); //
		 * System.out.println(dd.CAS+"\t"+dd.x0); if (dd!=null) descresult=0;
		 * 
		 * if(Math.abs(dd.MW-dd.MW_Frag)>1) { descresult=-1;// recalculate descriptors
		 * just in case }
		 * 
		 * } }
		 * 
		 * if (descresult==-1) { if (!reportTypes.isEmpty()) { descresult =
		 * df.CalculateDescriptors(m, dd, true, overwriteFiles, true,
		 * strOutputFolderStructureData); df.WriteJSON(dd,
		 * strOutputFolderStructureData+"/descriptorValues.json", true); } else {
		 * descresult = df.CalculateDescriptors(m, dd, true); } }
		 * 
		 * // just in case we are running a chemical without gsid (e.g. user drawn
		 * structure): if (!reportTypes.isEmpty() && gsid == null) {
		 * ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, "structure",
		 * strOutputFolderStructureData);// dont }
		 * 
		 * if (endpoint.equals(TESTConstants.ChoiceDescriptors)) { if (descresult != -1)
		 * { df.WriteCSVLine(fw, dd, del); } else { try { fw.write(dd.CAS + del +
		 * "error: " + df.errorMsg + "\r\n"); } catch (Exception ex) {
		 * logger.catching(ex); } } }
		 */
		if (!"OK".equals(dd.Error)) {
			// done=true; // commented out: dont kill rest of run for batch
			logger.error("Error calculating descriptors for {}", dd.ID);

			// TODO- create web report for this chemical that mentions this
			// error

			return res;
		}

		if (!reportTypes.isEmpty() && createDetailedConsensusReport)
			dd.WriteToFileTEXT(strOutputFolderStructureData, del);

		// testchemical.png

		// dont need testchemical.png since have structure.png:
		// if (writeFiles)
		// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m,
		// "testchemical",strOutputFolderStructureData);

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			// array to store predictions for all methods for consensus method:
			List<Double> predictedToxicities = new ArrayList<>();
			List<Double> predictedUncertainties = new ArrayList<>();

			AllResults allResults = ht_allResults.get(endpoint);// shortcut to
																// results
																// object
			AllResults allResultsFrag = ht_allResultsFrag.get(endpoint); // shortcut
																			// to
																			// results
																			// object

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

//			System.out.println(evalInstances2d.getDescriptorNames());
//			System.out.println(evalInstance2d.toString());

			Lookup.ExpRecord er = TaskCalculations.LookupExpVal(dd.ID, evalInstance2d, trainingDataSet2d,
					testDataSet2d);
			if (csv != null)
				csv.printField(FormatUtils.toD3(er.expToxValue));

			// **************************************************

			int result = 0;
			String statMessage = "Calculating " + endpoint + "...";
			// statMessage += "Molecule ID = " + dd.CAS;

			// statMessage += "Molecule ID = " + dd.CAS+" (#"+(molNum+1)+")";
			statMessage += "Molecule ID = " + dd.ID + " (" + (molNum + 1) + " of " + molCount + ")";
			// statMessage += "Molecule #" + Index;

			// System.out.println(statMessage);

			// ******************************************************

			Double predToxVal = null;
			double predToxUnc = 1;// TODO: add code to calculate this

			// if (method.equals(TESTConstants.ChoiceFDAMethod)) {
			// result = ptFDA.CalculateToxicity(CAS, method,
			// endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
			// strOutputFolder, DescriptorSet, evalInstances2d,
			// trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
			// useFragmentsConstraint, this, er);
			// predToxVal = ptFDA.predToxVal;
			// } else if (method.equals(TESTConstants.ChoiceHierarchicalMethod))
			// {

			long descriptorCalculationTime = System.currentTimeMillis() - start;
			totalDescriptorCalculationTime += descriptorCalculationTime;
			start = System.currentTimeMillis();
			long reportGenerationTime = 0;

			ReportOptions options = new ReportOptions();
			options.reportBase = strOutputFolder;
			options.embedImages = true;

			if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
						strOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d, trainingDataSet2d,
						trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er,
						minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
				if (csv != null)
					csv.printField(FormatUtils.toD3(predToxVal));
			} else if (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
				result = ptNN.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
						strOutputFolder, DescriptorSet, evalInstances2d, trainingDataSet2d, testDataSet2d, dd.MW, er,
						options);
				predToxVal = ptNN.predToxVal;
				if (csv != null)
					csv.printField(FormatUtils.toD3(predToxVal));
			} else if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
						strOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d, trainingDataSet2d,
						trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er,
						minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
				if (csv != null)
					csv.printField(FormatUtils.toD3(predToxVal));
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
						strOutputFolder, DescriptorSet, allResultsFrag, evalInstancesFrag, evalInstance2d,
						trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
						useFragmentsConstraint, er, minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
				if (csv != null)
					csv.printField(FormatUtils.toD3(predToxVal));
				// } else if
				// (method.equals(TESTConstants.ChoiceRandomForrestCaesar)) {
				// result=ptRFC.CalculateToxicity(CAS, method,
				// endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
				// strOutputFolder, DescriptorSet, evalInstances2d,
				// trainingDataSet2d, testDataSet2d, dd.MW,
				// this, er,ht);
				// predToxVal = ptRFC.predToxVal;

			} else if (method.equals(TESTConstants.ChoiceLDA)) {
				result = ptLDA.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev,
						strOutputFolder, DescriptorSet, htAllResultsMOA, htTrainingSetsMOA, htAllResultsLC50,
						htTrainingSetsLC50, evalInstances2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
						useFragmentsConstraint, er, vecMOA, options);

				predToxVal = ptLDA.predToxVal;
				if (csv != null)
					csv.printField(FormatUtils.toD3(predToxVal));
			} else if (method.equals(TESTConstants.ChoiceConsensus)) {

				String strOutputFolder2 = null;
				if (createDetailedConsensusReport)
					strOutputFolder2 = strOutputFolder;

				// TODO move following into a subroutine:
				// Hierarchical:
				result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceHierarchicalMethod, endpoint, isBinaryEndpoint,
						isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, allResults, evalInstances2d,
						evalInstance2d, trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag,
						useFragmentsConstraint, er, minFileAgeHours, options);
				predictedToxicities.add(ptH.predToxVal);
				res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceHierarchicalMethod, CAS, er.expToxValue,
						ptH.predToxVal, dd.MW, "", isBinaryEndpoint));

				if (csv != null)
					csv.printField(FormatUtils.toD3(ptH.predToxVal));
				predictedUncertainties.add(ptH.predToxUnc);

				// Single Model
				if (TESTConstants.haveSingleModelMethod(endpoint)) {
					// System.out.println("SM r2="+orSM.getR2());
					result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceSingleModelMethod, endpoint,
							isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, allResults,
							evalInstances2d, evalInstance2d, trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW,
							dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
					predictedToxicities.add(ptH.predToxVal);
					if (csv != null)
						csv.printField(FormatUtils.toD3(ptH.predToxVal));
					predictedUncertainties.add(ptH.predToxUnc);
					res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceSingleModelMethod, CAS, er.expToxValue,
							ptH.predToxVal, dd.MW, "", isBinaryEndpoint));
				} else {
					res.add(getTESTPredictionError(endpoint, TESTConstants.ChoiceSingleModelMethod, CAS,
							"Single model method is unavailable for this endpoint",
							ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
				}

				// Group contribution:
				if (TESTConstants.haveGroupContributionMethod(endpoint)) {
					result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceGroupContributionMethod, endpoint,
							isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet,
							allResultsFrag, evalInstancesFrag, evalInstance2d, trainingDataSetFrag, trainingDataSet2d,
							testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);

					predictedToxicities.add(ptH.predToxVal);
					if (csv != null)
						csv.printField(FormatUtils.toD3(ptH.predToxVal));
					predictedUncertainties.add(ptH.predToxUnc);
					res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceGroupContributionMethod, CAS,
							er.expToxValue, ptH.predToxVal, dd.MW, "", isBinaryEndpoint));
				} else {
					res.add(getTESTPredictionError(endpoint, TESTConstants.ChoiceGroupContributionMethod, CAS,
							"Group contribution method is unavailable for this endpoint",
							ERROR_CODE_APPLICABILITY_DOMAIN_ERROR));
				}

				// //FDA:
				// result = ptFDA.CalculateToxicity(CAS,
				// TESTConstants.ChoiceFDAMethod, endpoint,isBinaryEndpoint,
				// isLogMolarEndpoint,abbrev,
				// strOutputFolder, DescriptorSet, evalInstances2d,
				// trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
				// useFragmentsConstraint, this, er);
				// predictedToxicities.add(ptFDA.predToxVal);
				// predictedUncertainties.add(ptFDA.predToxUnc);

				// Nearest neighbor:
				result = ptNN.CalculateToxicity(CAS, TESTConstants.ChoiceNearestNeighborMethod, endpoint,
						isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder2, DescriptorSet, evalInstances2d,
						trainingDataSet2d, testDataSet2d, dd.MW, er, options);
				predictedToxicities.add(ptNN.predToxVal);
				if (csv != null)
					csv.printField(FormatUtils.toD3(ptNN.predToxVal));
				predictedUncertainties.add(ptNN.predToxUnc);
				res.add(getTESTPredictedValue(endpoint, TESTConstants.ChoiceNearestNeighborMethod, CAS, er.expToxValue,
						ptNN.predToxVal, dd.MW, "", isBinaryEndpoint));

				// for (int i=0;i<methods.size();i++) {
				// System.out.println(methods.get(i)+"\t"+predictedToxicities.get(i));
				// }

				predToxVal = calculateConsensusToxicity(predictedToxicities);
				if (csv != null)
					csv.printField(FormatUtils.toD3(predToxVal));
				predToxUnc = 1;// TODO: add code to calculate this

				predictedToxicities.add(predToxVal);

				long time = System.currentTimeMillis();

				if (!reportTypes.isEmpty()) {

					double[] Mean = trainingDataSet2d.getMeans();
					double[] StdDev = trainingDataSet2d.getStdDevs();

					Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d,
							testDataSet2d, true, true, true, Mean, StdDev);

					Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d,
							trainingDataSet2d, true, true, true, Mean, StdDev);

//					PredictToxicityWebPageCreator ptwc = new PredictToxicityWebPageCreator();
					PredictToxicityJSONCreator jsonCreator = new PredictToxicityJSONCreator();
					PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();

					List<String> methods = TaskCalculations.getMethods(endpoint);

					PredictionResults predictionResults = null;
					if (reportTypes.contains(WebReportType.JSON) || reportTypes.contains(WebReportType.HTML)
							|| reportTypes.contains(WebReportType.PDF)) {
						predictionResults = jsonCreator.writeConsensusResultsJSON(predToxVal, predToxUnc, method, CAS,DSSTOXCID, DSSTOXSID,dd.SmilesRan,
								endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, dd.MW, "OK", htTestMatch,
								htTrainMatch, methods, predictedToxicities, predictedUncertainties,
								createDetailedConsensusReport, options);
					}

					if (reportTypes.contains(WebReportType.HTML) || reportTypes.contains(WebReportType.PDF)) {
						String outputfilename = "PredictionResultsConsensus.html";
						String htmlFilePath = Paths.get(options.reportBase, outputfilename).toFile().getAbsolutePath();
						htmlCreator.writeConsensusResultsWebPages(predictionResults, htmlFilePath);
						if (reportTypes.contains(WebReportType.PDF) && !Strings.isEmpty(htmlFilePath)) {
							try {
								pdfPath = HtmlUtils.HtmlToPdf(htmlFilePath,
										FileUtils.replaceExtension(htmlFilePath, ".pdf"));
							} catch (IOException e) {
								logger.catching(e);
							}
						}
					}

					if (deleteExtraFiles) {
						deleteExtraFiles(options);
					}
				}

				reportGenerationTime = System.currentTimeMillis() - time;

			} // end choice consensus

			if (result == -1) {
				return res;
			}

			long predictionGenerationTime = System.currentTimeMillis() - start - reportGenerationTime;
			totalReportGenerationTime += reportGenerationTime;
			totalPredictionGenerationTime += predictionGenerationTime;

			logger.info("{}\t{}\t{}\t{}\t\t{}\t{}\t{}", index, dd.ID, FormatUtils.toD3(er.expToxValue),
					FormatUtils.toD3(predToxVal), descriptorCalculationTime, predictionGenerationTime,
					reportGenerationTime);

			if (!isBinaryEndpoint) {
				v = WriteToxicityResultsForChemical(fw, endpoint, method, index, dd.ID, er.expToxValue, predToxVal,
						dd.MW, del, "", null);
			} else {
				v = WriteBinaryToxResultsForChemical(fw, endpoint, method, index, dd.ID, er.expToxValue, predToxVal,
						dd.MW, del, "", null);
			}

			if (v != null && pdfPath != null)
				v.reportPath = pdfPath;

			// if (method.equals(TESTConstants.ChoiceConsensus)) {
			// //TODO
			// this.WriteToxicityResultsForChemicalAllMethods(fw3,index,dd.CAS,er.expToxValue,methods,predictedToxicities,dd.MW,del,"");
			// }

			res.add(0, v);

		} else {// Descriptors
			// System.out.println(dd.CAS + "\t");
			// TODO
			// WriteDescriptorResultsForChemicalToWebPage(fw,index,CAS,"");

			// Need to return something for descriptors or we get error when try to add
			// smiles to v object later:
			res.add(new TESTPredictedValue(CAS, endpoint, method));
			return res;

		}

		return res;
	}

	private static void deleteExtraFiles(ReportOptions options) {
		// TODO- delete extra files in StructureData folder? i.e. everything besides
		// descriptorValues.json

		// Don't need to delete similar chemicals folder since I figured out how to
		// write chart as encoded string to JSON file directly:
//		File similarChemicalsFolder=new File(options.reportBase+File.separator+"SimilarChemicals");
//		
//		File [] files=similarChemicalsFolder.listFiles();
//		
//		for (int i=0;i<files.length;i++) {
//			files[i].delete();
//			
//			if (files[i].exists())
//			
//			System.out.println(files[i].exists());
//			
//		}
//		similarChemicalsFolder.delete();

	}

	static Double calculateConsensusToxicity(List<Double> preds) {
		double pred = 0;
		int predcount = 0;

		for (int i = 0; i < preds.size(); i++) {
			if (preds.get(i) != null) {
				predcount++;
				pred += preds.get(i);
			}
		}

		if (predcount < minPredCount)
			return null;

		pred /= (double) predcount;
		// System.out.println(pred);
		return pred;
	}

	public static Lookup.ExpRecord LookupExpVal(String CAS, Instances trainingDataSet2d, Instances testDataSet2d) {
		Lookup lookup = new Lookup();
		// trainingDataSet2d.setClassIndex(classIndex);
		// testDataSet2d.setClassIndex(classIndex);

		Lookup.ExpRecord er;

		// lookup first in training set based on CAS
		er = lookup.LookupExpRecordByCAS(CAS, trainingDataSet2d,"Training");
		// System.out.println(er.expToxValue);
		if (er != null) return er;
		
		// ******************************************************
		// next lookup in external test set based on CAS:
		er = lookup.LookupExpRecordByCAS(CAS, testDataSet2d,"Test");
		// System.out.println(er.expToxValue);
		if (er != null) return er;

		// ******************************************************
		return null;

	}

	static void WriteOverallTextHeader(Writer fw, String d, String endpoint) throws Exception {

		fw.write("#" + d + "ID" + d);

		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

		if (!TESTConstants.isBinary(endpoint)) {
			if (TESTConstants.isLogMolar(endpoint)) {
				fw.write("Exp_Value:" + molarlogunits + d);
				fw.write("Pred_Value:" + molarlogunits + d);
			}

			fw.write("Exp_Value:" + massunits + d);
			fw.write("Pred_Value:" + massunits + d + "error");
		} else {
			fw.write("Exp_Value" + d);
			fw.write("Pred_Value" + d);

			fw.write("Exp_Result" + d);
			fw.write("Pred_Result" + d + "error");
		}

		fw.write("\r\n");
	}

	static TESTPredictedValue getTESTPredictionError(String endpoint, String method, String CAS, String error,
			String errorCode) {
		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
		v.error = error;
		v.errorCode = errorCode;

		return v;
	}

	public static TESTPredictedValue getTESTPredictedValue(String endpoint, String method, String CAS, Double ExpToxVal,
			Double PredToxVal, double MW, String error, boolean isBinary) {
		if (!isBinary) {
			return getTESTPredictedValue(endpoint, method, CAS, ExpToxVal, PredToxVal, MW, error);
		} else {
			return getTESTPredictedValueBinary(endpoint, method, CAS, ExpToxVal, PredToxVal, MW, error);
		}
	}

	static TESTPredictedValue getTESTPredictedValue(String endpoint, String method, String CAS, Double ExpToxVal,
			Double PredToxVal, double MW, String error) {

		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
		
		
		v.expValLogMolar = ExpToxVal;
		v.predValLogMolar = PredToxVal;

		try {
			Double ExpToxValMass = null;
			Double PredToxValMass = null;
			if (TESTConstants.isLogMolar(endpoint)) {
				if (PredToxVal != null) {
					PredToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, PredToxVal, MW);
					v.predValMass = PredToxValMass;
				}

				if (ExpToxVal != null) {
					ExpToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, ExpToxVal, MW);
					v.expValMass = ExpToxValMass;
				}
			} else {
				PredToxValMass = PredToxVal;
				v.predValMass = PredToxValMass;

				ExpToxValMass = ExpToxVal;
				v.expValMass = ExpToxValMass;
			}

			v.error = error;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return v;
	}

	static TESTPredictedValue WriteToxicityResultsForChemical(Writer fw2, String endpoint, String method, int index,
			String CAS, Double ExpToxVal, Double PredToxVal, double MW, String d, String error, String errorCode) {

		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);

		v.expValLogMolar = ExpToxVal;
		v.predValLogMolar = PredToxVal;

		try {
			Double ExpToxValMass = null;
			Double PredToxValMass = null;
			if (TESTConstants.isLogMolar(endpoint)) {
				if (PredToxVal != null) {
					PredToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, PredToxVal, MW);
					v.predValMass = PredToxValMass;
				}

				if (ExpToxVal != null) {
					ExpToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, ExpToxVal, MW);
					v.expValMass = ExpToxValMass;
				}
			} else {
				PredToxValMass = PredToxVal;
				v.predValMass = PredToxValMass;

				ExpToxValMass = ExpToxVal;
				v.expValMass = ExpToxValMass;
			}

			fw2.write(index + d + CAS + d);

			// ********************************************************
			// Molar values
			if (TESTConstants.isLogMolar(endpoint)) {
				if (ExpToxVal == null) {
					fw2.write("N/A" + d);
				} else {
					fw2.write(FormatUtils.toD2(ExpToxVal) + d);
				}
				if (PredToxVal == null) {
					fw2.write("N/A" + d);
				} else {
					fw2.write(FormatUtils.toD2(PredToxVal) + d);
				}
			}

			// ********************************************************
			// Mass values
			if (ExpToxVal == null) {
				fw2.write("N/A" + d);
			} else {
				if (Math.abs(ExpToxValMass) < 0.1) {
					fw2.write(FormatUtils.toDecimalString(ExpToxValMass, "0.00E00") + d); // mass
				} else {
					fw2.write(FormatUtils.toD2(ExpToxValMass) + d); // mass
				}
			}

			if (PredToxVal == null) {
				fw2.write("N/A");
			} else {
				if (Math.abs(PredToxValMass) < 0.1) {
					fw2.write(FormatUtils.toDecimalString(PredToxValMass, "0.00E00")); // mass
				} else {
					fw2.write(FormatUtils.toD2(PredToxValMass)); // mass
				}
			}

			// write error:
			fw2.write(d + error);
			v.error = error;
			v.errorCode = errorCode;

			fw2.write("\r\n");
			fw2.flush();

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return v;
	}

//	/**
//	 * Returns the most recent status message, or null if there is no current
//	 * status message.
//	 * 
//	 * @return statMessage
//	 */
//	public static String getMessage() {
//		return statMessage;
//	}
//	
//	
//	/**
//	 * Called to find out how much has been done.
//	 * 
//	 * @return Current progress in %
//	 */
//	public static int getCurrent() {
//		return current;
//	}
//
//	
//	/**
//	 * Called to find out if the task has completed.
//	 * @param done 
//	 * 
//	 * @return Done
//	 */
//	public static boolean isDone() {
//		return done;
//	}

	public static void addSmiles(AtomContainer m, TESTPredictedValue tpv) {

		try {
			tpv.smiles = CDKUtilities.generateSmiles(m);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public static List<TESTPredictedValue> doPredictions(DescriptorData dd, CalculationParameters params) {
		List<TESTPredictedValue> result = new ArrayList<>();

		for (String endpoint : params.endpoints) {
			for (String method : params.methods) {
				try {
					result.addAll(calculate(dd, endpoint, method, params.reportTypes));
				} catch (Exception ex) {// TODO above method doesnt throw an exception - will this get caught?
					final String CAS = dd.ID;
					logger.error("Error processing record with CAS {}: {} {}", CAS, ex.getClass(), ex.getMessage());
					logger.catching(ex);
				}
			}
		}

		return result;
	}

	/*
	 * not used public static TESTPredictedValue doPrediction(AtomContainer m,
	 * CalculationParameters params) {
	 * 
	 * 
	 * String endpoint = params.endpoint; String method = params.method; // String
	 * outputFilePath = params.outputFile;
	 * 
	 * DescriptorFactory df = new DescriptorFactory(false);
	 * 
	 * String smiles = null; try { smiles = CDKUtilities.generateSmiles(m); } catch
	 * (Exception e) { logger.catching(e); }
	 * 
	 * try {
	 * 
	 * String error = (String) m.getProperty("Error"); TESTPredictedValue v = null;
	 * 
	 * if (StringUtils.isEmpty(error)) { v = calculate(m, endpoint,
	 * method,params.reportTypes, df); } else { // something wrong with chemical
	 * dont do calculations // but write to file: String CAS = (String)
	 * m.getProperty("CAS");
	 * 
	 * if (!endpoint.equals(TESTConstants.abbrevChoiceDescriptors)) { Instances
	 * trainingDataSet2d = ht_ccTraining.get(endpoint); Instances testDataSet2d =
	 * ht_ccPrediction.get(endpoint); Lookup.ExpRecord er = LookupExpVal(CAS,
	 * trainingDataSet2d, testDataSet2d);
	 * 
	 * if (!TESTConstants.isBinary(endpoint)) { v = getTESTPredictedValue(endpoint,
	 * method, CAS, er.expToxValue, null, er.MW, del, error); } else { v =
	 * getTESTPredictedValueBinary(endpoint, method, CAS, er.expToxValue, null,
	 * er.MW, del, error); }
	 * 
	 * } else { // this.WriteDescriptorResultsForChemicalToWebPage(fw, //
	 * index,CAS,error); v = new TESTPredictedValue(CAS, endpoint, method); v.error
	 * = error; } }
	 * 
	 * // Add SMILES to the predicted result v.smiles = smiles;
	 * 
	 * return v;
	 * 
	 * } catch (Exception ex) { final String CAS = (String) m.getProperty("CAS");
	 * logger.error("Error processing record with CAS {}: {} {}", CAS,
	 * ex.getClass(), ex.getMessage()); logger.catching(ex); } finally { } return
	 * null;
	 * 
	 * }
	 */

	public static synchronized List<TESTPredictedValue> doPredictions(AtomContainerSet moleculeSet,
			CalculationParameters params) {
		// TODO recode this so that all predictions are done simultaneously
		// (speed up hierarchical by only looping through clusters once)

		String outputFilePath = params.outputFile;

		DescriptorFactory df = new DescriptorFactory(false);

		logger.info("Calculating '{}' using '{}' methods for {} molecule(s)...", params.endpoints,
				params.methods, moleculeSet.getAtomContainerCount());
		long start = System.currentTimeMillis();
		totalDescriptorCalculationTime = 0;
		totalPredictionGenerationTime = 0;
		totalReportGenerationTime = 0;

		List<TESTPredictedValue> result = new ArrayList<TESTPredictedValue>();
		try {
			Writer[] fws = new Writer[params.endpoints.size()];
			CsvWriter[] csv = new CsvWriter[params.endpoints.size()];
			for (int i = 0; i < fws.length; i++) {
				fws[i] = StringUtils.isEmpty(outputFilePath) ? new StringWriter()
						: new FileWriter(FileUtils.replaceExtension(outputFilePath, "") + "-"
								+ TESTConstants.getAbbrevEndpoint(params.endpoints.get(i)) + ".tsv");
				if (!params.endpoints.get(i).equals(TESTConstants.abbrevChoiceDescriptors)) {
					if (!Strings.isEmpty(outputFilePath))
						csv[i] = new CsvWriter(FileUtils.replaceExtension(outputFilePath, "") + "-"
								+ TESTConstants.getAbbrevEndpoint(params.endpoints.get(i)) + ".csv");
					if (csv[i] != null)
						writeCsvHeader(csv[i], params.endpoints.get(i), params.methods.get(0));
				}
				if (params.endpoints.get(i).equals(TESTConstants.abbrevChoiceDescriptors)) {
					df.WriteCSVHeader(fws[i], del);
				} else {
					WriteOverallTextHeader(fws[i], del, params.endpoints.get(i));
				}
			}

			// ArrayList
			// methods=DescriptorCalculationTask7.getMethods(endpoint);
			// //shortcut to results object

			for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
				AtomContainer m = (AtomContainer) moleculeSet.getAtomContainer(i);
				
				String CAS = (String) m.getProperty("CAS");

				DSSToxRecord rec = null;
				if (ResolverDb.isAvailable()) {
					if (!Strings.isEmpty(CAS) && !CAS.matches("C\\d*_\\d{8,}")) {
						ArrayList<DSSToxRecord> recs = ResolverDb.lookupByCAS(CAS);
						if (recs.size() > 0)
							rec = recs.get(0);
					} else {
						Inchi inchi = Inchi.generateInChiKeyCDK(m);
						
						if (!"N/A".equals(inchi.inchiKey) & !Strings.isEmpty(inchi.inchiKey)) {
							ArrayList<DSSToxRecord> recs = ResolverDb.lookupByInChIKey(inchi.inchiKey);
							if (recs.size() > 0)
								rec = recs.get(0);
							else {
								// Fallback to Indigo 
								inchi = IndigoUtilities.generateInChiKey(m);
								if (!Strings.isEmpty(inchi.inchiKey)) {
									recs = ResolverDb.lookupByInChIKey(inchi.inchiKey);
									if (recs.size() > 0)
										rec = recs.get(0);
								}
							}
							if (rec != null && !Strings.isEmpty(rec.cas)) {
								CAS = rec.cas;
								m.setProperty("CAS", rec.cas);
							}
						}
					}
				}

				DescriptorData dd = null;
				if (Strings.isEmpty((String) m.getProperty("Error"))) {
					dd = WebTEST.goDescriptors(m, params.reportTypes);
					if (!"OK".equals(dd.Error)) {
						m.setProperty("Error", dd.Error);
						m.setProperty("ErrorCode", ERROR_CODE_DESCRIPTOR_CALCULATION_ERROR);
					}
				}

				

				for (String method : params.methods) {
					for (int j = 0; j < params.endpoints.size(); j++) {
						String endpoint = params.endpoints.get(j);

						// if (checkParams(endpoint, method) != null) {
						// continue;
						// }

						List<TESTPredictedValue> res = null;
						
						TESTPredictedValue v = null;
						try {
							String smiles = null;
							try {
								smiles = CDKUtilities.generateSmiles(m);
							} catch (Exception e) {
								logger.catching(e);
							}

							int index = i + 1;
							String error = (String) m.getProperty("Error");
							
//							System.out.println(CAS+"\t"+endpoint+"\t"+method+"\t"+error);
							
							
							if (StringUtils.isEmpty(error)) {
								res = calculate(i, moleculeSet.getAtomContainerCount(), m, fws[j], endpoint, method,
										index, params.reportTypes, csv[j], df, dd);
							} else { // something wrong with chemical dont do calculations
										// but write to file:
								
								res = new ArrayList<>();
								
								String errorCode = (String) m.getProperty("ErrorCode");
								
								if (!endpoint.equals(TESTConstants.abbrevChoiceDescriptors)) {
									Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
									Instances testDataSet2d = ht_ccPrediction.get(endpoint);

									Lookup.ExpRecord er = LookupExpVal(CAS, trainingDataSet2d, testDataSet2d);

									if (!TESTConstants.isBinary(endpoint)) {
										v = WriteToxicityResultsForChemical(fws[j], endpoint, method, index, CAS,
												er.expToxValue, null, er.MW, del, error, errorCode);
									} else {
										v = WriteBinaryToxResultsForChemical(fws[j], endpoint, method, index, CAS,
												er.expToxValue, null, er.MW, del, error, errorCode);
									}

									// if (method.equals(TESTConstants.ChoiceConsensus)) {
									// this.WriteToxicityResultsForChemicalAllMethods(fw3,index,CAS,er.expToxValue,methods,null,er.MW,del,error);
									// }

								} else {
									// this.WriteDescriptorResultsForChemicalToWebPage(fw,
									// index,CAS,error);
									fws[j].write(CAS + del + "Error:" + error + "\r\n");
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
							if (!endpoint.equals(TESTConstants.abbrevChoiceDescriptors)) {
								fws[j].write(String.format("%d\t%s\tN/A\tN/A\tN/A\tN/A\tError: %s\r\n", i + 1, CAS,
										ex.getMessage()));

							}

							logger.error("Error processing record with CAS {}: {} {}", CAS, ex.getClass(),
									ex.getMessage());
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
							if (csv[j] != null)
								csv[j].newRecord();
						}
					}// end loop over endpoints
				} //end loop over methods
			} // end loop over molecules

			for (int i = 0; i < fws.length; i++) {
				fws[i].close();
				if (csv[i] != null) {
					csv[i].close();
				}
			}

		} catch (Exception ex) {
			logger.catching(ex);
		}

		logger.debug(
				"Time to generate output for {} using {} method: {}s " + "including time to calculate descriptors {}s, "
						+ "predictions {}s, reports {}s",
				params.endpoints, params.methods,
				(System.currentTimeMillis() - start) / 1000d, totalDescriptorCalculationTime / 1000d,
				totalPredictionGenerationTime / 1000d, totalReportGenerationTime / 1000d);

		return result;
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
//			r.gsid = rec.gsid;
			r.inChICode = rec.inchi;
			r.inChIKey = rec.inchiKey;
		}
	}

	static TESTPredictedValue getTESTPredictedValueBinary(String endpoint, String method, String CAS, Double ExpToxVal,
			Double PredToxVal, double MW, String error) {
		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
		try {

			if (ExpToxVal == null) {
			} else {
				v.expValLogMolar = ExpToxVal;
			}
			if (PredToxVal == null) {
			} else {
				v.predValLogMolar = PredToxVal;
			}

			if (ExpToxVal == null) {
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

			if (PredToxVal == null) {
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
			v.error = error;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return v;
	}

	static TESTPredictedValue WriteBinaryToxResultsForChemical(Writer fw2, String endpoint, String method, int index,
			String CAS, Double ExpToxVal, Double PredToxVal, double MW, String d, String error, String errorCode) {
		TESTPredictedValue v = new TESTPredictedValue(CAS, endpoint, method);
		try {
			fw2.write(index + d + CAS + d);

			if (ExpToxVal == null) {
				// fw.write("<td>N/A</td>\n");
				fw2.write("N/A" + d);
			} else {
				// fw.write("<td>" + d2.format(ExpToxVal) + "</td>\n");
				fw2.write(FormatUtils.toD2(ExpToxVal) + d);
				v.expValLogMolar = ExpToxVal;
			}
			if (PredToxVal == null) {
				// fw.write("<td>N/A</td>\n");
				fw2.write("N/A" + d);
			} else {
				// fw.write("<td>" + d2.format(PredToxVal) + "</td>\n");
				fw2.write(FormatUtils.toD2(PredToxVal) + d);
				v.predValLogMolar = PredToxVal;
			}

			// System.out.println(ExpToxVal+"\t"+PredToxVal);

			if (ExpToxVal == null) {
				// fw.write("<td align=\"center\">N/A</td>\n");//mass
				fw2.write("N/A" + d);
			} else {
				if (ExpToxVal < 0.5) {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						// fw.write("<td align=\"center\">Developmental
						// NON-toxicant</td>\n"); //mass
						fw2.write("Developmental NON-toxicant" + d); // mass
						v.message = "Developmental NON-toxicant";

					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						// fw.write("<td align=\"center\">Mutagenicity
						// Negative</td>\n"); //mass
						fw2.write("Mutagenicity Negative" + d); // mass
						v.message = "Mutagenicity Negative";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						// fw.write("<td align=\"center\">Does NOT bind to
						// estrogen receptor</td>\n"); //mass
						fw2.write("Does NOT bind to estrogen receptor" + d); // mass
						v.message = "Does NOT bind to estrogen receptor";
					}
					v.expActive = false;
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						// fw.write("<td align=\"center\">Developmental
						// toxicant</td>\n"); //mass
						fw2.write("Developmental toxicant" + d); // mass
						v.message = "Developmental toxicant";

					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						// fw.write("<td align=\"center\">Mutagenicity
						// Positive</td>\n"); //mass
						fw2.write("Mutagenicity Positive" + d); // mass
						v.message = "Mutagenicity Positive";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						// fw.write("<td align=\"center\">Binds to estrogen
						// receptor</td>\n"); //mass
						fw2.write("Binds to estrogen receptor" + d); // mass
						v.message = "Binds to estrogen receptor";
					}
					v.expActive = true;
				}
			}

			if (PredToxVal == null) {
				// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
				fw2.write("N/A");
			} else {
				if (PredToxVal < 0.5) {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						// fw.write("<td align=\"center\">Developmental
						// NON-toxicant</td>\n"); //mass
						fw2.write("Developmental NON-toxicant"); // mass
						v.message = "Developmental NON-toxicant";
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						// fw.write("<td align=\"center\">Mutagenicity
						// Negative</td>\n"); //mass
						fw2.write("Mutagenicity Negative"); // mass
						v.message = "Mutagenicity Negative";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						// fw.write("<td align=\"center\">Does NOT bind to
						// estrogen receptor</td>\n"); //mass
						fw2.write("Does NOT bind to estrogen receptor" + d); // mass
						v.message = "Does NOT bind to estrogen receptor";
					}
					v.predActive = false;
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						// fw.write("<td align=\"center\">Developmental
						// toxicant</td>\n"); //mass
						fw2.write("Developmental toxicant"); // mass
						v.message = "Developmental toxicant";
					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						// fw.write("<td align=\"center\">Mutagenicity
						// Positive</td>\n"); //mass
						fw2.write("Mutagenicity Positive"); // mass
						v.message = "Mutagenicity Positive";
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						// fw.write("<td align=\"center\">Binds to estrogen
						// receptor</td>\n"); //mass
						fw2.write("Binds to estrogen receptor" + d); // mass
						v.message = "Binds to estrogen receptor";
					}
					v.predActive = true;
				}
			}

			// write error:
			fw2.write(d + error);
			v.error = error;
			v.errorCode = errorCode;

			// if (!error.equals(""))
			// System.out.println(index + d + CAS + d + "N/A" + d + "N/A" + d +
			// error);

			// fw.write("</tr>\n");
			fw2.write("\r\n");

			// fw.flush();
			fw2.flush();

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return v;
	}

	/**
	 * This method allows to run the whole matrix of endpoint/method calculations
	 * and output/add results into SDF or CSV files
	 * 
	 * @param cmd - CommandLine parameters
	 * @throws Exception
	 */
	public static boolean go(CommandLine cmd) throws Exception {
		RunParams params = new RunParams();
		params.inputFilePath = cmd.getOptionValue("input");
		params.smilesColumn = cmd.getOptionValue("smiles-column");
		params.outputFilePath = cmd.getOptionValue("output");
		
		
		String endpoints=cmd.getOptionValue("endpoint");
		if (endpoints.contains(",")) {
			params.endpoints=Arrays.asList(endpoints.split(","));	
		} else {
			params.endpoints = Arrays.asList(cmd.getOptionValues("endpoint"));
		}
		
		for (String endpoint:params.endpoints ) {
			System.out.println(endpoint);
		}
		
				
		params.methods = Arrays.asList(cmd.getOptionValues("method"));
		params.reportTypes = new HashSet<>();
		
		if (cmd.getOptionValues("report")!=null) {
			for (String option : cmd.getOptionValues("report")) {
				params.reportTypes.add(WebReportType.valueOf(option));
			}
		} 
		return WebTEST.go(params);
	}

	public static boolean go(RunParams params) throws Exception {
		// pass/fail - do as much check as possible in one pass
		boolean pass = true;

		// File format checks
		// Input file format checks
		String inputFilePath = params.inputFilePath;
		String smilesColumn = params.smilesColumn;
		int fmt = TESTConstants.getFormatByFileName(inputFilePath);
		if (fmt == -1) {
			logger.error("Unable to recognize input file type by extension");
			pass = false;
		}
		if (fmt != TESTConstants.numFormatSDF && fmt != TESTConstants.numFormatSMI && fmt != TESTConstants.numFormatMOL
				&& fmt != TESTConstants.numFormatCSV) {
			logger.error("invalid input filetype - supported are SDF, SMI, MOL and CSV");
			pass = false;
		}
		if (fmt == TESTConstants.numFormatCSV && StringUtils.isEmpty(smilesColumn)) {
			logger.error("SMILES column name is not specified for CSV input file");
			pass = false;
		}

		// Output file format checks
		String outputFilePath = params.outputFilePath;
		if (StringUtils.isEmpty(outputFilePath))
			outputFilePath = FilenameUtils.getBaseName(inputFilePath) + ".txt";
		else {
			fmt = TESTConstants.getFormatByFileName(outputFilePath);
			if (fmt == -1) {
				logger.error("Unable to recognize output file type by extension");
				pass = false;
			}
			if (fmt != TESTConstants.numFormatSDF && fmt != TESTConstants.numFormatCSV) {
				logger.error("invalid output file type - supported are SDF and CSV");
				pass = false;
			}
		}

		// Endpoint checks
		List<String> endpoints = TESTConstants.getFullEndpoints(params.endpoints);
		if (endpoints.contains("?")) {
			logger.error("invalid endpoint(s)");
			pass = false;
		}

		// Methods checks
		List<String> methods = TESTConstants.getFullMethods(params.methods);
		
		if (!endpoints.contains(TESTConstants.abbrevChoiceDescriptors)
				&& methods.contains("?")) {
			logger.error("invalid method");
			pass = false;
		}

		if (pass) {
			CalculationParameters p = new CalculationParameters();
			p.outputFile = outputFilePath;
			p.endpoints = endpoints; // full names
			p.methods = methods; // full names
			p.reportTypes = params.reportTypes;
			p.discardResults = params.discardResults;
			WebTEST.runMatrix(params.inputFilePath, p);
		}

		return pass;
	}

	// This method is for running matrix calculations (endpoints by methods)
	public static void runMatrix(String inputFilePath, CalculationParameters params) throws Exception {

		logger.info("{}", params);

		AtomContainerSet ms = loadMolecules(inputFilePath, TESTConstants.getFormatByFileName(inputFilePath));

		// leveraging new multienpoint / multimethod api
		MemUtils.printMemoryUsage("Memory stats before calculations");
		go(ms, params);
		MemUtils.printMemoryUsage("Memory stats after calculations");
	}

	/**
	 * Loads molecules set from specified input
	 * 
	 * @param input       - file name or values string
	 * @param inputFormat - input string format
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static AtomContainerSet loadMolecules(String input, int inputFormat)
			throws FileNotFoundException, IOException {
		AtomContainerSet ms = null;
		switch (inputFormat) {
		case TESTConstants.numFormatSDF:
			ms = LoadFromSDF(input);
			break;
		case TESTConstants.numFormatMOL:
			ms = LoadFromMOL(input);
			break;
		case TESTConstants.numFormatSMI:
			ms = LoadFromSMI(input);
			break;
		case TESTConstants.numFormatSMILES:
			ms = LoadFromSMILES(input);
			break;
		case TESTConstants.numFormatCSV:
			throw new NotImplementedException("format is not supported yet");
		default:
			throw new IllegalArgumentException();
		}

		if (ms == null)
			logger.error("Unable to load molecule set");

		return ms;
	}

	public static AtomContainerSet loadMolecules(Set<String> structures, int inputFormat)
			throws FileNotFoundException, IOException {

		AtomContainerSet result = new AtomContainerSet();

		for (String str : structures) {
			AtomContainerSet ms = null;
			switch (inputFormat) {
			case TESTConstants.numFormatSDF:
				ms = LoadFromSDF(str);
				break;
			case TESTConstants.numFormatMOL:
				ms = LoadFromMOL(str);
				break;
			case TESTConstants.numFormatSMI:
				ms = LoadFromSMI(str);
				break;
			case TESTConstants.numFormatSMILES:
				ms = LoadFromSMILES(str);
				break;
			case TESTConstants.numFormatCSV:
				throw new NotImplementedException("format is not supported yet");
			default:
				throw new IllegalArgumentException();
			}

			if (ms == null)
				logger.error("Unable to load molecule set: {}", str);

			if (ms != null)
				result.add(ms);
		}

		return result;
	}

	private static ResultsWriter openResultsWriter(String outputFilePath) throws IOException {
		logger.debug("Opening ResultsWriter for {}", outputFilePath);

		switch (TESTConstants.getFormatByFileName(outputFilePath)) {
		case TESTConstants.numFormatCSV:
			return new CsvResultsWriter(outputFilePath);
		case TESTConstants.numFormatSDF:
			return new SdfResultsWriter(outputFilePath);
		case TESTConstants.numFormatJSON:
			return new JsonResultsWriter(outputFilePath);
		case TESTConstants.numFormatXML:
			return new XmlResultsWriter(outputFilePath);
		default:
			throw new UnsupportedOperationException();
		}
	}

	public static DescriptorData goDescriptors(IAtomContainer ac, Set<WebReportType> reportTypes) {

		String error = (String) ac.getProperty("Error");

		DescriptorFactory df = new DescriptorFactory(false);

		try {

			if (StringUtils.isEmpty(error)) {

				DescriptorData dd = new DescriptorData();
				dd.ID = (String) ac.getProperty("CAS");
				String CAS = dd.ID;
//				dd.gsid = getGSID(ac, CAS);

				int descresult = -1;

				String strOutputFolderStructureData = getStructureDataOutputFolder(reportTypes, CAS);

				// TODO store descriptors in SQLite database instead of json files

				if (usePreviousDescriptors) {
					String filePathDescriptors = strOutputFolderStructureData + File.separator
							+ "descriptorValues.json";
					File descFile = new File(filePathDescriptors);
//					System.out.println(filePathDescriptors);
//					System.out.println(descFile.exists());

					if (descFile.exists()) {

						long t1 = System.currentTimeMillis();
						dd = DescriptorData.loadFromJSON(descFile);
						dd.ThreeD = false;
						dd.WriteToFileHTML(strOutputFolderStructureData);
//						System.out.println(dd.CAS+"\t"+dd.x0);
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

					if (!reportTypes.isEmpty()) {
						descresult = df.CalculateDescriptors(ac, dd, true, overwriteFiles, true,
								strOutputFolderStructureData);
						df.WriteJSON(dd, strOutputFolderStructureData + "/descriptorValues.json", true);
					} else {
						descresult = df.CalculateDescriptors(ac, dd, true);
					}

					long t2 = System.currentTimeMillis();

					logger.debug("Calculated descriptors in {}s", (t2 - t1) / 1000.);
				}

				// just in case we are running a chemical without gsid (e.g. user drawn
				// structure):
				if (!reportTypes.isEmpty() && dd.dtxcid == null) {
					ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(ac, "structure",
							strOutputFolderStructureData);// dont
				}

				if (descresult == -1)
					dd.Error = df.errorMsg;
				else
					dd.Error = "OK";

				return dd;
			} else {
				DescriptorData dd = new DescriptorData();
				dd.Error = error;
				return dd;
			}

		} catch (Exception ex) {
			final String CAS = (String) ac.getProperty("CAS");
			logger.error("Error processing record with CAS {}: {} {}", CAS, ex.getClass(), ex.getMessage());
			logger.catching(ex);

			DescriptorData dd = new DescriptorData();
			dd.Error = "Error processing record with CAS " + CAS + ", error=" + ex.getMessage();
			return dd;
		}

	}

	static String getStructureDataOutputFolder(Set<WebReportType> reportTypes, String CAS) {
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

//	static String getGSID(IAtomContainer ac, String CAS) {
//		String gsid = ac.getProperty("gsid");
//
//		if (gsid == null) {
//						
//			if (ResolverDb.isAvailable()) {			
//				if (!Strings.isEmpty(CAS) && !CAS.matches("C\\d*_\\d{8,}")) {				
//					ArrayList<DSSToxRecord> recs = ResolverDb.lookupByCAS(CAS);
//					if (recs.size()==0) {
//						logger.debug("Cannot resolve {}", CAS);
//					} else {
//						DSSToxRecord rec=recs.get(0);
//						if (gsid == null)
//							gsid = rec.gsid;
//					}					
//				}
//			}
//			
//			
//			
////			// try to look up based on CAS
////			ChemistryDashboardRecord rec = ChemistryDashboardRecord.lookupDashboardRecord("casrn", CAS,
////					statNCCT_ID_Records);
////			if (rec == null)
////				logger.debug("Cannot resolve {}", CAS);
////			else
////				gsid = rec.gsid;
//		}
//		
//		return gsid;
//	}

	public static List<TESTPredictedValue> go(DescriptorData dd, CalculationParameters params) {
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
		return doPredictions(dd, params);
	}

	/**
	 * Check if endpoint/method combination is allowed
	 * 
	 * @param endpoint the endpoint
	 * @param method   the method
	 * @return warning message if an endpoint/method combination is not allowed,
	 *         null otherwise
	 */
	public static String checkParams(String endpoint, String method) {
		if (method.equalsIgnoreCase(TESTConstants.ChoiceSingleModelMethod)
				&& !TESTConstants.haveSingleModelMethod(endpoint)) {
			final String message = "Single model method is unavailable for this endpoint - ignoring";
			logger.warn(message);
			return message;
		}

		if (method.equalsIgnoreCase(TESTConstants.ChoiceGroupContributionMethod)
				&& !TESTConstants.haveGroupContributionMethod(endpoint)) {
			final String message = "Group contribution is unavailable for this endpoint - ignoring";
			logger.warn(message);
			return message;
		}

		return null;
	}

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

		logger.debug("Loading training data in {}s", (t2 - t1) / 1000.);

		///////////////////////////////////////////////////////////////
		// do predictions:

		return doPredictions(ms, params);

	}

//	public static List<TESTPredictedValue> goForGUI(AtomContainerSet ms, CalculationParameters params) throws Exception {
//
//		current = 0;
//		stop=false;
//		statMessage="";
//
//		
//		if (params.method.equalsIgnoreCase(TESTConstants.ChoiceSingleModelMethod) && !TESTConstants.haveSingleModelMethod(params.endpoint)) {
//			logger.warn("Single model method is unavailable for this method - ignoring");
//			return null;
//		}
//
//		if (params.method.equalsIgnoreCase(TESTConstants.ChoiceGroupContributionMethod) && !TESTConstants.haveGroupContributionMethod(params.endpoint)) {
//			logger.warn("Group contribution is unavailable for this method - ignoring");
//			return null;
//		}
//
//		///////////////////////////////////////////////////////////////
//		// load model data (if isn't already in memory):
//
//		long t1 = System.currentTimeMillis();
//		loadTrainingData(params.endpoint, params.method);
//		long t2 = System.currentTimeMillis();
//
//		logger.debug("Loading training data in {}s", (t2 - t1) / 1000.);
//
//		///////////////////////////////////////////////////////////////
//		// do predictions:
//
//		return doPredictionsForGUI(ms, params);
//		
//	}

	public static List<TESTPredictedValue> go(String inputFilePath, String outputFilePath, int iEndpoint, int iMethod,
			Set<WebReportType> reportTypes) throws Exception {

		String endpoint = TESTConstants.getEndpoint(iEndpoint);
		if (endpoint == "?") {
			logger.error("Invalid endpoint");
			return null;
		}

		String method = TESTConstants.getMethod(iMethod);
		if (method == "?") {
			logger.error("Invalid method");
			return null;
		}

		AtomContainerSet ms = loadMolecules(inputFilePath, TESTConstants.getFormatByFileName(inputFilePath));

		List<TESTPredictedValue> r = WebTEST.go(ms,
				new CalculationParameters(outputFilePath, null, endpoint, method, reportTypes));

		return r;
	}

	public static List<TESTPredictedValue> go(String inputFilePath, String outputFilePath, int iEndpoint, int iMethod)
			throws Exception {
		return go(inputFilePath, outputFilePath, iEndpoint, iMethod, null);
	}

	/**
	 * This is shortcut method to be called from web services
	 * 
	 * @param input      - input file or SMILES
	 * @param inputType  - numerical format specifier
	 * @param iEndpoint  - numerical endpoint specifier
	 * @param iMethod    - numerical method specifier
	 * @param reportType - report type @see {@link WebReportType}
	 * @return returns a list of predicted values
	 * @throws Exception
	 */
	public static List<TESTPredictedValue> go(String input, int inputType, int iEndpoint, int iMethod,
			Set<WebReportType> reportTypes) throws Exception {

		String endpoint = TESTConstants.getEndpoint(iEndpoint);
		if (endpoint == "?") {
			logger.error("Invalid endpoint");
			return null;
		}

		String method = TESTConstants.getMethod(iMethod);
		if (method == "?") {
			logger.error("Invalid method");
			return null;
		}

		AtomContainerSet ms = loadMolecules(input, inputType);

		return WebTEST.go(ms, new CalculationParameters(null, null, endpoint, method, reportTypes));
	}

	public static List<TESTPredictedValue> go(String input, int inputType, Set<String> endpoints, Set<String> methods,
			Set<WebReportType> reportTypes) throws Exception {
		AtomContainerSet ms = loadMolecules(input, inputType);
		return WebTEST.go(ms, new CalculationParameters(null, null, new ArrayList<>(endpoints), new ArrayList<>(methods), reportTypes));
	}

	public static List<TESTPredictedValue> go(Set<String> query, int inputType, Set<String> endpoints,
			Set<String> methods, Set<WebReportType> reportTypes) throws Exception {


		AtomContainerSet ms = loadMolecules(query, inputType);

		return WebTEST.go(ms, new CalculationParameters(null, null, new ArrayList<>(endpoints), new ArrayList<>(methods), reportTypes));
	}

	/**
	 * This is shortcut method to be called from web services
	 * 
	 * @param input     - input file or SMILES
	 * @param inputType - numerical format specifier
	 * @param iEndpoint - numerical endpoint specifier
	 * @param iMethod   - numerical method specifier
	 * @return returns a list of predicted values
	 * @throws Exception
	 */
	public static List<TESTPredictedValue> go(String input, int inputType, int iEndpoint, int iMethod)
			throws Exception {
		return go(input, inputType, iEndpoint, iMethod, WebReportType.getNone());
	}

	public static Options createOptions() {
		Options options = new Options();

		Option o = new Option("i", "input", true, "input file (MOL, SDF, SMI or CSV)");
		o.setRequired(true);
		options.addOption(o);

		options.addOption("o", "output", true, "output file (SDF or CSV)");

		options.addOption("c", "smiles-column", true, "in case of CSV file specifies SMILES column name");

		o = new Option("m", "method", true, "abbreviated QSAR method (hc, fda, sm, nn, gc, rf, lda, consensus)");
		// o.setArgs(8);
		options.addOption(o);

		o = new Option("e", "endpoint", true,
				"abbreviated endpoint (LC50, LC50DM, IGC50, LD50, EC50GA, BCF, DevTox, Mutagenicity, ER_Binary, ER_LogRBA, BP, VP, MP, Density, FP, ST, TC, Viscosity, WS)");
		
		options.addOption(o);
		
		o = new Option("r", "report", true,	"report options(json, html,pdf)");
		
		
		// o.setArgs(19);
		options.addOption(o);

		return options;
	}

	public static void main(String[] args) throws Exception {
		Options options = createOptions();
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			logger.info("Running command-line WebTEST...");
			go(cmd);
		} catch (ParseException exp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("WebTEST <options>", "", options, exp.getMessage());
		}
	}

	public static void preloadDatasets() {
		logger.info("Loading training datasets ...");
		loadTrainingData(TESTConstants.ChoiceFHM_LC50, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceDM_LC50, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceTP_IGC50, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceRat_LD50, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceBCF, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceReproTox, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceMutagenicity, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceEstrogenReceptor, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceBoilingPoint, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceVaporPressure, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceMeltingPoint, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceFlashPoint, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceDensity, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceSurfaceTension, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceThermalConductivity, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceViscosity, TESTConstants.ChoiceConsensus);
		loadTrainingData(TESTConstants.ChoiceWaterSolubility, TESTConstants.ChoiceConsensus);
	}

//	public static void stop() {
//		stop=true;
//	}
}
