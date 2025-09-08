package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
//import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.GUI.MyBrowserLauncher;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.TESTPredictedValue;

public class RunFromSmiles {

	public static boolean debug = true;

	// public static String status;
	public static final String strSID = "DTXSID";
	public static final String strCID = "DTXCID";
	public static final String strCAS = "CASRN";
	public static final String strSmiles = "SMILES";// ?
	public static final String strName = "NAME";// ?

	private static final Logger logger = LogManager.getLogger(RunFromSmiles.class);

	public static List<String> allEndpoints = Arrays.asList(TESTConstants.ChoiceFHM_LC50, TESTConstants.ChoiceDM_LC50,
			TESTConstants.ChoiceTP_IGC50, TESTConstants.ChoiceRat_LD50, TESTConstants.ChoiceBCF,
			TESTConstants.ChoiceReproTox, TESTConstants.ChoiceMutagenicity,
//			TESTConstants.ChoiceEstrogenReceptor,
//			TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
			TESTConstants.ChoiceBoilingPoint, TESTConstants.ChoiceVaporPressure, TESTConstants.ChoiceMeltingPoint,
			TESTConstants.ChoiceDensity, TESTConstants.ChoiceFlashPoint, TESTConstants.ChoiceSurfaceTension,
			TESTConstants.ChoiceThermalConductivity, TESTConstants.ChoiceViscosity,
			TESTConstants.ChoiceWaterSolubility);

	public static String[] twoEndpoints = { TESTConstants.ChoiceFHM_LC50, TESTConstants.ChoiceMutagenicity };

	// private static Gson gson = new
	// GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	public static Gson gsonNotPretty = new GsonBuilder().serializeSpecialFloatingPointValues().disableHtmlEscaping()
			.create();
	
	
//	private static SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());

	
	public class ReportCreator {


		public static void createWebPagesForDTXSID(String filePath, String targetDTXSID) {
			
			
			File file=new File(filePath);
			
			String reportBase=file.getParentFile().getAbsolutePath()+File.separator+targetDTXSID;
			
			
			int count=0;
			
			try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
	            String line;
	            Gson gson = new Gson();

	            while ((line = reader.readLine()) != null) {
	                // Parse the JSON line into a PredictionResults object
	                PredictionResults predictionResults = gson.fromJson(line, PredictionResults.class);

	                // Check if the DTXSID matches the target value
	                if (predictionResults.getDTXSID().equals(targetDTXSID)) {
	                    System.out.println(predictionResults.getDTXSID()+"\t"+predictionResults.getEndpoint());
	                    
	                    File htmlFile=writeWebPage(predictionResults, reportBase, targetDTXSID);
	                    
	                    MyBrowserLauncher.launch(htmlFile.toURI());
	                    count++;
	                }
	                
	                if(count==16)break;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
		
		
		public static void lookAtPrettyJson(String filepathJson) {
		
				try {
					Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
					BufferedReader br = new BufferedReader(new FileReader(filepathJson));
					int count = 0;
		
		//			for (String line:lines) {
					while (true) {
						String line = br.readLine();
						if (line == null)
							break;
						count++;
						PredictionResults pr = gson.fromJson(line, PredictionResults.class);
		
						if (!pr.getEndpoint().equals(TESTConstants.ChoiceBoilingPoint))
							continue;
		
						System.out.println(gson.toJson(pr));
		
					}
		
					br.close();
		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
			}

		static void convertJsonReportsToHtml() {
		
			String dtxsid = "DTXSID40177523";
			File folder = new File(
					"C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\reports\\"
							+ dtxsid);
		
			for (File file : folder.listFiles()) {
		
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
		
					String json = br.readLine();
		
					PredictionResults predictionResults = gson.fromJson(json, PredictionResults.class);
		
					String htmlReport = ReportCreator.getReportAsHTMLString(predictionResults);
		
					FileWriter fw = new FileWriter(file.getAbsolutePath().replace(".json", ".html"));
		
					fw.write(htmlReport);
					fw.flush();
					fw.close();
		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}

		public static void saveJson(String destJsonPath, Object obj) {
			try {
		
				File file = new File(destJsonPath);
				file.getParentFile().mkdirs();
		
				FileWriter fw = new FileWriter(destJsonPath);
				fw.write(gson.toJson(obj));
				fw.flush();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static void writeReportsAsJsonFiles(Hashtable<String,PredictionResults>htResults,String endpoint,String method,String reportBase) {				
		
			Set<String> setOfKeys = htResults.keySet();
			for (String DTXSID : setOfKeys) {			
				PredictionResults predictionResults=htResults.get(DTXSID);			
				String json=ReportCreator.getReportAsJsonString(predictionResults);//Json for report object
				ReportCreator.writeReportJson(predictionResults,reportBase, json,DTXSID);			
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

		private static void saveResultsAsTsv(String destJsonPath, List<PredictionResults> results) {
			try {
		
				File file = new File(destJsonPath);
				file.getParentFile().mkdirs();
		
				FileWriter fw = new FileWriter(destJsonPath);
		
				fw.write("DTXSID\tDTXCID\tEndpoint\tReport\r\n");
		
				for (PredictionResults pr : results) {
					fw.write(pr.getDTXSID() + "\t" + pr.getDTXCID() + "\t" + pr.getEndpoint() + "\t"
							+ gsonNotPretty.toJson(pr) + "\r\n");
		
				}
		
				fw.flush();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private static void writeReportsAsHTML_Files(Hashtable<String,PredictionResults>htResults,String reportBase) {				
			Set<String> setOfKeys = htResults.keySet();
			for (String DTXSID : setOfKeys) {			
				PredictionResults predictionResults=htResults.get(DTXSID);			
				writeWebPage(predictionResults, reportBase, DTXSID);			
			}
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
		
	}
	
	public class MoleculeCreator {

		public static IAtomContainer createMolecule(DSSToxRecord dr) {
			IAtomContainer m = WebTEST4.prepareMolFileMolecule(dr.mol);
			m.setProperty(DSSToxRecord.strSID, dr.sid);// store sid so dont need to look up later
			m.setProperty(DSSToxRecord.strCID, dr.cid);// store sid so dont need to look up later
			m.setProperty(DSSToxRecord.strSmiles, dr.smiles);// need original smiles NOT QSAR ready smiles
			if (dr.cas != null)
				m.setProperty(DSSToxRecord.strCAS, dr.cas);// need CAS for doing nearest neighbor method (to exclude
															// training chemical by CAS)
			return m;
		}

		public static IAtomContainer createMolecule(String smiles) {
			IAtomContainer m = WebTEST4.prepareSmilesMolecule(smiles);
			m.setProperty(DSSToxRecord.strSmiles, smiles);// need orig
			ResolverDb2.assignRecordByStructureViaInchis(m, "");
			// Gson gson=new Gson();
			// System.out.println(gson.toJson(m.getProperties()));
			return m;
		}

		public static IAtomContainer createMolecule(String smiles, String DTXSID, String CAS) {
			return createMolecule(smiles, DTXSID, null, CAS);
		}

		public static IAtomContainer createMolecule(String smiles, String DTXSID, String DTXCID, String CAS) {
			IAtomContainer m = WebTEST4.prepareSmilesMolecule(smiles);
			m.setProperty(DSSToxRecord.strSID, DTXSID);// store sid so dont need to look up later
			m.setProperty(DSSToxRecord.strCID, DTXCID);// store sid so dont need to look up later
			m.setProperty(DSSToxRecord.strSmiles, smiles);// need original smiles NOT QSAR ready smiles
			if (CAS != null)
				m.setProperty(DSSToxRecord.strCAS, CAS);// need CAS for doing nearest neighbor method (to exclude training
														// chemical by CAS)
			return m;
		}

		public static IAtomContainer createMoleculeFromResolverBySID(String sid) {
		
				ArrayList<DSSToxRecord> recs = ResolverDb2.lookupByDTXSID(sid);
		
				if (recs.size() == 0) {
					System.out.println(sid + "\tnot in snapshot");
					return null;
				}
		
				DSSToxRecord dr = recs.get(0);
				MDLV3000Reader mr = new MDLV3000Reader();
				InputStream stream = new ByteArrayInputStream(dr.mol.getBytes());
		
				try {
					mr.setReader(stream);
					IAtomContainer m = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
					m.setProperty("Error", "");
					WebTEST4.checkAtomContainer(m);
					m.setProperty("DSSToxRecord", dr);
					mr.close();
		
					if (dr.smiles == null) {
						try {
							dr.smiles = CDKUtilities.generateSmiles(m, SmiFlavor.Canonical);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
		
					return m;
				} catch (Exception e) {
					System.out.println("couldnt read mol for " + sid);
		
		//			System.out.println(dr.mol);
					AtomContainer ac = new AtomContainer();
					ac.setProperty("DSSToxRecord", dr);
					WebTEST4.checkAtomContainer(ac);
					return ac;
				}
			}

		/**
		 * Sometimes a molecule in an SDF can have a smiles but the V3000 has no atoms
		 * 
		 * @param sp
		 * @param molecule
		 * @return
		 */
		public static AtomContainer createMoleculeFromSmilesProperty(SmilesParser sp, IAtomContainer molecule) {
			AtomContainer molecule2 = null;
			String smiles = null;
			if (molecule.getProperty("smiles") != null) {
				smiles = molecule.getProperty("smiles");
			} else if (molecule.getProperty("SMILES") != null) {
				smiles = molecule.getProperty("SMILES");
			}
		
			if (smiles != null) {
				try {
					molecule2 = (AtomContainer) sp.parseSmiles(smiles);
					// System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
				} catch (Exception ex) {
					molecule2 = new AtomContainer();
				}
		
			} else {
				molecule2 = new AtomContainer();
			}
		
			molecule2.setProperties(molecule.getProperties());
			return molecule2;
		}

		private static IAtomContainer getMoleculeFromSmiles(SmilesParser sp, IAtomContainer molecule) {
			AtomContainer molecule2 = null;
		
			String smiles = null;
		
			if (molecule.getProperty("smiles") != null) {
				smiles = molecule.getProperty("smiles");
			} else if (molecule.getProperty("SMILES") != null) {
				smiles = molecule.getProperty("SMILES");
			}
		
			if (smiles != null) {
				try {
					molecule2 = (AtomContainer) sp.parseSmiles(smiles);
					// System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
				} catch (Exception ex) {
					molecule2 = new AtomContainer();
				}
		
			} else {
				molecule2 = new AtomContainer();
			}
		
			molecule2.setProperties(molecule.getProperties());
			return molecule2;
		}

		public static String convertAtomContainerToMolFileStringV3000(IAtomContainer firstMolecule)
				throws CDKException, IOException {
			StringWriter sw = new StringWriter();
			SDFWriter sdfWriter = new SDFWriter(sw);
			sdfWriter.setAlwaysV3000(true);
		
			sdfWriter.write(firstMolecule);
			sdfWriter.close();
			String molFile = sw.toString();
			return molFile;
		}

		public static String getStringStructure(BufferedReader br) {


			try {
				String type="V2000";

				String strStructure = "";

				while (true) {
					String Line = br.readLine();

					if (Line == null) return null;
					//			System.out.println(Line);
					if(Line.contains("V30 BEGIN CTAB")) type="V3000";

					// System.out.println(Line);
					strStructure += Line + "\r\n";
					if (Line.contains("M  END"))
						break;
				}

				return strStructure;

			} catch (Exception ex) {
				//				ex.printStackTrace();
				return null;
			}
		}
		
		private static IAtomContainer getMoleculeNoProperties(BufferedReader br)
					throws IOException, CDKException {
				
				
				String strStructure=getStringStructure(br);
				
				if(strStructure==null) return null;
				
		
				InputStream stream = new ByteArrayInputStream(strStructure.getBytes());
		
				IAtomContainer molecule = null;
		
				if (strStructure.contains("V30 BEGIN CTAB")) {
					MDLV3000Reader mr = new MDLV3000Reader(stream);
					try {
						molecule = mr.readMolecule(DefaultChemObjectBuilder.getInstance());
					} catch (Exception ex) {
		//				System.out.println("Error parsing:\n"+strStructure);
						molecule = new AtomContainer();
					}
					mr.close();
				} else {
					MDLV2000Reader mr = new MDLV2000Reader(stream);
						molecule = mr.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
						try {
					} catch (Exception ex) {
						molecule = new AtomContainer();
					}
					mr.close();
				}
				return molecule;
			}

		public static IAtomContainer getMoleculeWithProperties(BufferedReader br)
				throws IOException, CDKException {
		
			IAtomContainer molecule=getMoleculeNoProperties(br);		
			if(molecule==null) return null;
			
			getProperties(br, molecule);
			return molecule;
		}

		/**
		 * Use this method to make sure that properties get retrieved
		 * 
		 * @param molFile
		 * @return
		 */
		public static IAtomContainer readFromMolFileString(String molFile) {
		
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		
			try {
		
				StringReader stringReader = new StringReader(molFile);
				BufferedReader br = new BufferedReader(stringReader);
		
				IAtomContainer molecule = MoleculeCreator.getMoleculeNoProperties(br);
				getProperties(br, molecule);
		
				br.close();
		
				if (molecule.getAtomCount() == 0) {
					molecule=MoleculeCreator.getMoleculeFromSmiles(sp, molecule);
//					System.out.println("From smiles\t"+molecule.getProperty("SMILES")+"\t"+molecule.getAtomCount());
				}
				return molecule;

		
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		/**
			 * Accounts for the fact that the molecule properties can be multiline
			 * 
			 * @param br
			 * @param molecule
			 * @throws IOException
			 */
			private static void getProperties(BufferedReader br, IAtomContainer molecule) throws IOException {
				String value = null;
				String field = null;
		
				while (true) {
		
					String Line = br.readLine();
					
		//			System.out.println(Line);
		
					if (Line.contains(">  <") || Line.contains("><") || Line.contains("> <")) {
						if (field != null && value != null) {
							molecule.setProperty(field, value);
		//					System.out.println(field+"\t"+value+"\n");
						}
						field = Line.substring(Line.indexOf("<") + 1, Line.length() - 1);
						value = null;
						// System.out.println(field);
					} else if (Line.contains("$$$")) {
						molecule.setProperty(field, value);
						break;
					} else if (Line.trim().length() > 0) {
		
						if (value == null)
							value = Line;
						else
							value += "\n" + Line;
					}
					
				}
				
		//		System.out.println(gson.toJson(molecule.getProperties()));
			}
			
			
			public static Hashtable<String,Object> getPropertiesHashtable(BufferedReader br) throws IOException {
				String value = null;
				String field = null;
		
				Hashtable<String,Object>htProperties=new Hashtable<>();
				
				while (true) {
		
					String Line = br.readLine();
					
					if(Line==null)return null;
					
//					System.out.println(Line);
		
					if (Line.contains(">  <") || Line.contains("><") || Line.contains("> <")) {
						if (field != null && value != null) {
							htProperties.put(field, value);
		//					System.out.println(field+"\t"+value+"\n");
						}
						field = Line.substring(Line.indexOf("<") + 1, Line.length() - 1);
						value = null;
						// System.out.println(field);
					} else if (Line.contains("$$$")) {
						htProperties.put(field, value);
						break;
					} else if (Line.trim().length() > 0) {
		
						if (value == null)
							value = Line;
						else
							value += "\n" + Line;
					}
					
				}
				//		System.out.println(gson.toJson(molecule.getProperties()));

				return htProperties;
			}
		
	}
	

	public static void runSingle(String endpoint, String method, boolean createReports, boolean createDetailedReports,
			String reportBase, String smiles, String DTXSID, String CAS) {

		
		IAtomContainer ac=MoleculeCreator.createMolecule(smiles, DTXSID, CAS);// valid simple molecule

		PredictionResults predictionResults = RunFromSmiles.runEndpoint(ac, endpoint, method, createReports,
				createDetailedReports);

		String json = ReportCreator.getReportAsJsonString(predictionResults);
		System.out.println(json);

		// Get single report as HTML string:
		String htmlReport = ReportCreator.getReportAsHTMLString(predictionResults);
		System.out.println(htmlReport);

	}

	/**Results stored in hashtable by key
	 * 
	 * @param moleculeSet
	 * @param endpoint
	 * @param method
	 * @param createReports
	 * @param createDetailedReports
	 * @return hashtable of results with key as DTXSID and prediction results +
	 *         report as TESTPredictedValue
	 * 
	 */
	public static PredictionResults runEndpoint(IAtomContainer ac, String endpoint, String method,
			boolean createReports, boolean createDetailedReports) {

		DescriptorFactory.debug = false;
		WebTEST4.createDetailedReports = createDetailedReports;
		WebTEST4.createReports = createReports;
		WebTEST4.generateWebpages = false;
		PredictToxicityJSONCreator.forGUI = true;

		// *******************************************************
		WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
													// persistent

		List<String> endpoints = Arrays.asList(endpoint);

		ac = calculate(ac, endpoints, method);
		PredictionResults pr = getResults(ac, method);

		String DTXSID = ac.getProperty(DSSToxRecord.strSID);
		String Smiles = ac.getProperty(DSSToxRecord.strSmiles);
		String error = (String) ac.getProperty("Error");
		String CAS = null;

		if (pr == null) {
			pr = new PredictionResults();
			pr.setEndpoint(endpoint);
			pr.setMethod(method);
		}

		if (ac.getProperty(DSSToxRecord.strCAS) != null) {
			CAS = ac.getProperty(DSSToxRecord.strCAS);
			pr.setCAS(CAS);
		}

		pr.setDTXSID(DTXSID);
		pr.setSmiles(Smiles);
		pr.setError(error);

		return pr;

	}

	public static List<PredictionResults> runEndpointsAsList(String strMolFile, List<String> endpoints,String method,boolean createReports,boolean createDetailedReports) {

		try {
			
			IAtomContainer ac=MoleculeCreator.readFromMolFileString(strMolFile);
			WebTEST4.checkAtomContainer(ac);
//			System.out.println(ac.getProperties());
            
			return runEndpointsAsList(ac, endpoints,method,createReports,createDetailedReports); 
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}

	/**
	 * 
	 * @param ac
	 * @param endpoints
	 * @param method
	 * @param createReports
	 * @param createDetailedReports
	 * @return
	 */
	public static List<PredictionResults> runEndpointsAsList(IAtomContainer ac, List<String> endpoints,String method,boolean createReports,boolean createDetailedReports) {				

		DescriptorFactory.debug=false;
		WebTEST4.createDetailedReports=createDetailedReports;
		WebTEST4.createReports=createReports;
		WebTEST4.generateWebpages=false;
		PredictToxicityJSONCreator.forGUI=true;

		List<PredictionResults>Results=new ArrayList<>();
		
		SmilesGenerator sg=new SmilesGenerator(SmiFlavor.Canonical);

		// *******************************************************

		String DTXSID=null;
		if (ac.getProperty(strSID)!=null) DTXSID=ac.getProperty(strSID);
		else if (ac.getProperty(DSSToxRecord.strSID)!=null) DTXSID=ac.getProperty(DSSToxRecord.strSID);
		ac.setProperty(DSSToxRecord.strSID, DTXSID);

		String DTXCID=null;
		if (ac.getProperty(strCID)!=null) DTXCID=ac.getProperty(strCID);
		else if (ac.getProperty(DSSToxRecord.strCID)!=null) DTXCID=ac.getProperty(DSSToxRecord.strCID);
		ac.setProperty(DSSToxRecord.strCID, DTXCID);

		String CASRN=null;
		if (ac.getProperty(strCAS)!=null)	CASRN=ac.getProperty(strCAS);
		else if (ac.getProperty(DSSToxRecord.strCAS)!=null)	CASRN=ac.getProperty(DSSToxRecord.strCAS);
		ac.setProperty(DSSToxRecord.strCAS, CASRN);
		
//        System.out.println(CAS+"\t"+ac.getAtomCount());
		
		String SMILES=null;
		if (ac.getProperty(strSmiles)!=null)	SMILES=ac.getProperty(strSmiles);
		else if (ac.getProperty(DSSToxRecord.strSmiles)!=null)	SMILES=ac.getProperty(DSSToxRecord.strSmiles);

		if(SMILES==null) {
			try {
				SMILES=sg.create(ac);
			} catch (CDKException e) {
			}
		}
		ac.setProperty(DSSToxRecord.strSmiles, SMILES);
		
		String NAME=null;
		if (ac.getProperty(strName)!=null) NAME=ac.getProperty(strName);
		else if (ac.getProperty(DSSToxRecord.strName)!=null) NAME=ac.getProperty(DSSToxRecord.strName);
		ac.setProperty(DSSToxRecord.strCID, DTXCID);
		
		
		if (ac.getProperty("DSSToxRecord")!=null) {
			DSSToxRecord rec = ac.getProperty("DSSToxRecord");
			if (DTXSID == null && rec != null )	DTXSID = rec.sid;
			if (DTXCID == null && rec != null )	DTXCID = rec.cid;
			if (CASRN == null && rec != null )CASRN=rec.cas;
			if (SMILES== null && rec != null)SMILES=rec.smiles;
			if (NAME== null && rec != null)NAME=rec.name;
		}
			

		
		//			status=ac.getProperty(DSSToxRecord.strSmiles)+"";
		//			System.out.println((i+1)+"\t"+ac.getProperty(DSSToxRecord.strSmiles)+"");

		
		ac=calculate(ac,endpoints,method);
		
		List<PredictionResults> resultsArray=getResultsArray(ac,method);

		
		String error=(String) ac.getProperty("Error");
		
		
		logger.info("{}\t{}\t{}", SMILES,CASRN,DTXSID);



		for (int j=0;j<endpoints.size();j++) {

			PredictionResults pr=null;

			if (resultsArray.get(j)==null) {
				pr=new PredictionResults();
				pr.setEndpoint(endpoints.get(j));
				pr.setMethod(method);	
			} else {
				pr=resultsArray.get(j);
			}

			pr.setVersion(TESTConstants.SoftwareVersion);
			pr.setCAS(CASRN);	
			pr.setDTXSID(DTXSID);
			pr.setDTXCID(DTXCID);
			pr.setSmiles(SMILES);
			pr.setName(NAME);
			
//			System.out.println("pr.getName()="+pr.getName());
			
			if(pr.getError()==null)
				pr.setError(error);			
			
			Results.add(pr);

			//				Vector<SimilarChemical>similarChemicals0=pr.getSimilarChemicals().get(0).getSimilarChemicalsList();
			//				SimilarChemical sc0_0=similarChemicals0.get(0);
			//				System.out.println("here123:"+sc0_0.getCAS());

		}


		return Results;
	}
	
	
	
	

	public static List<PredictionResults> runEndpointsAsListFromSmiles(String Smiles,String DTXSID,String DTXCID,String CAS, List<String> endpoints,String method,boolean createReports,boolean createDetailedReports) {				
		IAtomContainer ac = WebTEST4.prepareSmilesMolecule(Smiles);
		ac.setProperty(DSSToxRecord.strSID, DTXSID);
		ac.setProperty(DSSToxRecord.strCID, DTXCID);
		ac.setProperty(DSSToxRecord.strCAS, CAS);
		ac.setProperty(DSSToxRecord.strSmiles, Smiles);
		return runEndpointsAsList(ac, endpoints, method, createReports, createDetailedReports);
	}

	public static PredictionResults getResults (IAtomContainer ac,String method) {

		List<TESTPredictedValue>listTPV=ac.getProperty("listTPV");

		for (int j=0;j<listTPV.size();j++) {
			TESTPredictedValue tpv=listTPV.get(j);
			if (tpv.method.equals(method)) {					
				return tpv.predictionResults;
			}
		}
		return null;

	}

	public static List<PredictionResults> getResultsArray (IAtomContainer ac,String method) {		
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

	private static File writeWebPage(PredictionResults predictionResults,String reportBase,String ID) {
		String fileNameNoExtension = WebTEST4.getResultFileNameNoExtension(predictionResults.getEndpoint(), predictionResults.getMethod(), ID);
		String outputFileName = fileNameNoExtension + ".html";
		String outputFilePath = reportBase + File.separator + outputFileName;
		PredictToxicityWebPageCreatorFromJSON htmlCreator = new PredictToxicityWebPageCreatorFromJSON();
		htmlCreator.writeResultsWebPages(predictionResults, outputFilePath);	
		return new File(outputFilePath);
	}

	

	// private static AtomContainer calculate(AtomContainer ac,String
	// endpoint,String method) {
	// String error=(String) ac.getProperty("Error");
	// List<TESTPredictedValue>listTPV=null;
	//
	// DescriptorData dd=null;
	//
	// try {
	//
	// //Note: descriptor calculations for each chemical are the most time
	// consuming. One could run calculations using the same descriptors and run all
	// endpoints
	// dd=WebTEST4.goDescriptors(ac);
	//
	// if (ac.getProperty(DSSToxRecord.strCAS)!=null) {
	// dd.ID=ac.getProperty(DSSToxRecord.strCAS);//need to properly run nearest
	// neighbor method to exclude test chemical by CAS
	// } else {
	// dd.ID=ac.getProperty(DSSToxRecord.strSID);
	// }
	//
	// error=(String) ac.getProperty("Error");
	//
	// CalculationParameters params=new CalculationParameters();
	// params.endpoints= new String[1];
	// params.endpoints[0]=endpoint;
	// params.methods= new String[1];
	// params.methods[0]=method;
	//
	// listTPV=WebTEST4.doPredictions(ac,dd,params);
	//
	// } catch (Exception ex) {
	// System.out.println("Error running "+ac.getProperty("smiles"));
	// return ac;
	// }
	//
	//// Gson gson = new
	// GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
	//// System.out.println(gson.toJson(listTPV.get(0)));
	// ac.setProperty("listTPV", listTPV);
	// return ac;
	// }

	public static IAtomContainer calculate(IAtomContainer ac,List<String>endpoints,String method) {
		List<TESTPredictedValue>listTPV=null;

		DescriptorData dd=null;

		try {

//			System.out.println("Enter RunFromSmiles calculate");
			
			//Note: descriptor calculations for each chemical are the most time consuming. One could run calculations using the same descriptors and run all endpoints
			dd=WebTEST4.goDescriptors(ac);
			
//			System.out.println(gson.toJson(dd));
			

			//Make sure dd.ID is not null
			if (ac.getProperty(DSSToxRecord.strCAS)!=null)  {
				dd.ID=ac.getProperty(DSSToxRecord.strCAS);//need to properly run nearest neighbor method to exclude test chemical by CAS	
			} else if (ac.getProperty(DSSToxRecord.strSID)!=null){
				dd.ID=ac.getProperty(DSSToxRecord.strSID);
			} else if (ac.getProperty(DSSToxRecord.strCID)!=null){
				dd.ID=ac.getProperty(DSSToxRecord.strCID);
			} else if (ac.getProperty("DSSToxRecord")!=null) {
				DSSToxRecord dr=ac.getProperty("DSSToxRecord");
				dd.ID=dr.cas;
			} 
			
			if (dd.ID==null)
				System.out.println("Cant set ID for DescriptorData: no CAS,SID,or CID");
			

//			String error=(String) ac.getProperty("Error");

			CalculationParameters params=new CalculationParameters();
			params.endpoints=endpoints;			
			params.methods= Arrays.asList(method);

			listTPV=WebTEST4.doPredictions(ac,dd,params);
			
//			System.out.println("Here 999, listTPV="+gson.toJson(listTPV));

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
//		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
//		String endpoint=TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity;
		String endpoint=TESTConstants.ChoiceRat_LD50;
		
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		String reportBase="reports";//folder to store reports to harddrive

//		AtomContainerSet acs=new AtomContainerSet();		

		IAtomContainer ac=MoleculeCreator.createMolecule("c1ccccc1", "DTXSID3039242","71-43-2");//valid simple molecule
		
//		IAtomContainer ac=createMolecule("c1ccccc1", "DTXSID3039242",null);//valid simple molecule
//		IAtomContainer ac=createMolecule("c1ccccc1", null,null);//valid simple molecule
//		IAtomContainer ac=createMolecule("O=P(OC1=CC=CC=C1)(OC1=CC=CC=C1)OC1=CC=CC=C1","DTXSID1021952","115-86-6");//valid but more complicated molecule
//		IAtomContainer ac=createMolecule("Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1", "DTXSID20211176","62119-86-2");//invalid molecule: salt
//		IAtomContainer ac=createMolecule("NC1=C(C=CC(=C1)NC(=O)C)OCC", "DTXSID7020053","17026-81-2");
//		IAtomContainer ac=createMolecule("O=C6c4ccc5c1ccc3c2c1c(ccc2C(=O)N(c2ccc(\\N=N/c1ccccc1)cc2)C3=O)c1ccc(c4c51)C(=O)N6c2ccc(\\N=N/c1ccccc1)cc2", "DTXSID1051983","3049-71-6");//very complicated aromatic molecule, takes longer to run
//		IAtomContainer ac=createMolecule("XXXX", "DTXSID2","123-45-6");//bad smiles (cant convert to structure)
//		IAtomContainer ac=createMolecule("[S]", "DTXSID9034941","7704-34-9");//has no carbon

		List<String> endpoints= Arrays.asList(endpoint);

		List<PredictionResults>listPR=runEndpointsAsList(ac, endpoints, method,createReports,createDetailedReports);		

		//Get report for one of the above chemicals as Json string:
		PredictionResults predictionResults=listPR.get(0);		
		String json=ReportCreator.getReportAsJsonString(predictionResults);
		System.out.println(json);

		//Get single report as HTML string:
		String htmlReport=ReportCreator.getReportAsHTMLString(predictionResults);
		//		System.out.println(htmlReport);

		try {
			File file=new File(reportBase+"/results.html");
			FileWriter fw=new FileWriter(file);
			fw.write(htmlReport);
			fw.flush();
			fw.close();

			Desktop desktop = Desktop.getDesktop();
			desktop.browse(file.toURI());


		} catch (Exception ex) {
			ex.printStackTrace();
		}


		//		//Write reports for all chemicals to harddrive:
		//		writeReportsAsJsonFiles(htResults, endpoint, method, reportBase);
		//		writeReportsAsHTML_Files(htResults, endpoint, method, reportBase);

	}

	public static void runDTXSIDAllEndpoints () {
		
		ResolverDb2.sqlitePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\snapshot.db";
		
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files

//		String dtxsid="DTXSID3039242";
		String dtxsid="DTXSID40177962";
		
		ArrayList<DSSToxRecord>recs=ResolverDb2.lookupByDTXSID(dtxsid);

		IAtomContainer molecule = TaskStructureSearch.getMoleculeFromDSSToxRecords(recs);
		
//		List<String>endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);
		List<String>endpoints= allEndpoints;

		List<PredictionResults>listPR=runEndpointsAsList(molecule, endpoints, method,createReports,createDetailedReports);		

		try {
			
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\reports sample\\";
			new File(folder).mkdirs();
			File file=new File(folder+"/predictionResults_"+dtxsid+".json");
			FileWriter fw=new FileWriter(file);

			Gson gson=new Gson();
			
			Gson gsonPretty = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

			for (PredictionResults pr:listPR) {
				System.out.println(gsonPretty.toJson(pr));
				fw.write(gson.toJson(pr)+"\r\n");
			}

			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Example of how to run all single chemical for single endpoint
	 */
	private static void runSingleChemicalFromSmiles () {

		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)

		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files

		String reportBase="reports";//folder to store reports to harddrive

		String smiles="c1ccccc1";
		//		String smiles="O=P(OC1=CC=CC=C1)(OC1=CC=CC=C1)OC1=CC=CC=C1";//valid but more complicated molecule
		//		String smiles="Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1";//salt
		//		String smiles="O=C6c4ccc5c1ccc3c2c1c(ccc2C(=O)N(c2ccc(\\N=N/c1ccccc1)cc2)C3=O)c1ccc(c4c51)C(=O)N6c2ccc(\\N=N/c1ccccc1)cc2";//very complicated aromatic molecule, takes longer to run
		//		String smiles="XXXX";//bad smiles (cant convert to structure)
		//		String smiles="[S]";//has no carbon
		//		String smiles="COCOCCCOCOCOCOCOOCCCCOCOC";// valid compound not in database


		IAtomContainer ac=MoleculeCreator.createMolecule(smiles);//valid simple molecule

		PredictionResults predictionResults=runEndpoint(ac, endpoint, method,createReports,
				createDetailedReports);		

		//Get report for one of the above chemicals as Json string:
		String json=ReportCreator.getReportAsJsonString(predictionResults);
		System.out.println(json);

		//Get single report as HTML string:
		String htmlReport=ReportCreator.getReportAsHTMLString(predictionResults);

		System.out.println(htmlReport);

		try {
			FileWriter fw=new FileWriter(reportBase+"/results.html");
			fw.write(htmlReport);
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Example of how to run all endpoints at once
	 */
	private static void runAllEndpointsExample () {

		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files

//		AtomContainerSet acs=new AtomContainerSet();		
		IAtomContainer ac=MoleculeCreator.createMolecule("c1ccccc1", "DTXSID3039242","71-43-2");//valid simple molecule

		//		acs.addAtomContainer(createMolecule("O=P(OC1=CC=CC=C1)(OC1=CC=CC=C1)OC1=CC=CC=C1","DTXSID1021952","115-86-6"));//valid but more complicated molecule
		//		acs.addAtomContainer(createMolecule("Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1", "DTXSID20211176","62119-86-2"));//invalid molecule: salt
		//		acs.addAtomContainer(createMolecule("O=C6c4ccc5c1ccc3c2c1c(ccc2C(=O)N(c2ccc(\\N=N/c1ccccc1)cc2)C3=O)c1ccc(c4c51)C(=O)N6c2ccc(\\N=N/c1ccccc1)cc2", "DTXSID1051983","3049-71-6"));//very complicated aromatic molecule, takes longer to run
		//		acs.addAtomContainer(createMolecule("XXXX", "DTXSID2","123-45-6"));//bad smiles (cant convert to structure)
		//		acs.addAtomContainer(createMolecule("[S]", "DTXSID9034941","7704-34-9"));//has no carbon

		//Run all endpoints at once:
		//		String [] endpoints= {TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50};


		List<PredictionResults>resultsAll=
				runEndpointsAsList(ac, allEndpoints, method,createReports,createDetailedReports);

		//Get DTXSID of a chemical:
		//Get all results for a given chemical:
		//		List<PredictionResults>listPredictionResults=htResultsAll.get(DTXSID);		

		//		for (PredictionResults pr:listPredictionResults) {
		//			String json=getReportAsJsonString(pr);
		//			System.out.println(json);
		//		}


		try {

			FileWriter fw= new FileWriter("reports/sample_predictions.json");
			fw.write(gson.toJson(resultsAll));
			fw.flush();
			fw.close();


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public void testReadFromJar() {
		
		String sdfFilePath="ST/ST_training.sdf";
		
		try {
		
			java.io.InputStream ins = this.getClass().getClassLoader().getResourceAsStream(sdfFilePath);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);
			
			for(int i=1;i<=5;i++)
				System.out.println(br.readLine());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	public static void testReadFromJarStatic(String sdfFilePath) {
		
		try {
			
			
			//example of sdfFilePath in the datasets jar file: "ST/ST_training.sdf"
			java.io.InputStream ins = RunFromSmiles.class.getClassLoader().getResourceAsStream(sdfFilePath);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);
			
			for(int i=1;i<=5;i++)
				System.out.println(br.readLine());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static String runFromAPI(String smiles, String endpointAbbreviation) {

		String address = "https://comptox.epa.gov/dashboard/web-test/";
		HttpResponse<String> response;
		try {
			response = Unirest.get(address + "/" + endpointAbbreviation).queryString("smiles", smiles).asString();

			return response.getBody();

		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}

	}

	
	public static class PredictionDashboard {
		public String source;
		public String property_name;
		public String method;

		public String dtxsid;
		public String smiles;
		public Double prediction_value;
		public String prediction_units;
		public String prediction_string;
		public String prediction_error;

		public static String[] fields = { "dtxsid", "property_name", "method", "source", "prediction_value",
				"prediction_units", "prediction_string", "prediction_error", "smiles" };

		// static String getHeader(String del) {
		// return
		// "source"+del+"property_name"+del+"method"+del+"dtxsid"+del+"prediction_value"+del+"prediction_units"+del+"prediction_string"+del+"prediction_error"+del+"smiles";
		// }

		// String toString(String del) {
		// return
		// source+del+property_name+del+method+del+dtxsid+del+prediction_value+del+prediction_units+del+prediction_string+del+prediction_error+del+smiles;
		// }
		
		
		


		public String toString(String del, String[] fieldNames) {
			String result = "";

			for (int i = 0; i < fieldNames.length; i++) {
				String fieldName = fieldNames[i];
				try {
					Field myField = this.getClass().getDeclaredField(fieldName);
					String type = myField.getType().getName();
					// System.out.println(fieldName);
					String strValue = null;

					switch (type) {

					case "java.lang.String":
						if (myField.get(this) == null)
							strValue = "";
						else
							strValue = myField.get(this) + "";
						break;

					case "java.lang.Double":
						if (myField.get(this) == null)
							strValue = "";
						else {
							strValue = (Double) myField.get(this) + "";
						}
						break;

					case "java.lang.Integer":
					case "java.lang.Boolean":
						if (myField.get(this) == null)
							strValue = "";
						else
							strValue = myField.get(this) + "";
						break;
					case "boolean":
						strValue = myField.getBoolean(this) + "";
						break;
					case "int":
						strValue = myField.getInt(this) + "";
						break;
					case "double":
						strValue = myField.getDouble(this) + "";

					}

					if (strValue != null) {
						if (strValue.contains(del))
							strValue = "\"" + strValue + "\"";
					} else {
						strValue = "";
					}

					result += strValue;

					if (i < fieldNames.length - 1)
						result += del;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return result;

		}

		private static void convertLogMolarUnits(PredictionDashboard pd, PredictionResultsPrimaryTable pt) {
		
			if (pd.property_name.contentEquals(TESTConstants.ChoiceFHM_LC50)
					|| pd.property_name.contentEquals(TESTConstants.ChoiceDM_LC50)
					|| pd.property_name.contentEquals(TESTConstants.ChoiceTP_IGC50)
					|| pd.property_name.contains("Water solubility")) {
				pd.prediction_value = Math.pow(10.0, -pt.getPredToxValue());
				pd.prediction_units = "M";
			} else if (pd.property_name.contentEquals("Oral rat LD50")) {
				pd.prediction_value = Math.pow(10.0, -pt.getPredToxValue());
				pd.prediction_units = "mol/kg";
			} else if (pd.property_name.contentEquals(TESTConstants.ChoiceBCF)) {
				pd.prediction_value = Math.pow(10.0, pt.getPredToxValue());
				pd.prediction_units = "L/kg";
			} else if (pd.property_name.contains("Vapor pressure")) {
				pd.prediction_value = Math.pow(10.0, pt.getPredToxValue());
				pd.prediction_units = "mmHg";
			} else if (pd.property_name.contains("Viscosity")) {
				pd.prediction_value = Math.pow(10.0, pt.getPredToxValue());
				pd.prediction_units = "cP";
			} else if (pd.property_name.contentEquals("Estrogen Receptor RBA")) {
				pd.prediction_value = Math.pow(10.0, pt.getPredToxValue());
				pd.prediction_units = "Dimensionless";
			} else {
				System.out.println("convertLogMolarUnits, Not handled:\t" + pd.property_name);
			}
		}

		static void loadReportsJson() {
			String filepath = "reports/TEST_results_all_endpoints_snapshot_compounds1.json";
		
			Type typeListPredictionResults = new TypeToken<List<PredictionResults>>() {
			}.getType();
		
			try {
				List<PredictionResults> results = gson.fromJson(new FileReader(filepath), typeListPredictionResults);
		
				System.out.println(PredictionDashboard.getHeaderString("\t", PredictionDashboard.fields));
		
				for (PredictionResults pr : results) {
		
					PredictionDashboard pd = convertResultsToPredictionFormat(pr, false);
		
					// System.out.println(gsonNotPretty.toJson(pr)+"\r\n");
					// System.out.println(gsonNotPretty.toJson(pd)+"\r\n");
					System.out.println(pd.toString("\t", PredictionDashboard.fields));
				}
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}

		static void runFromSampleJsonFile() {
		
			Type listOfMyClassObject = new TypeToken<Hashtable<String, List<PredictionResults>>>() {
			}.getType();
		
			try {
				Hashtable<String, List<PredictionResults>> htResultsAll = gson
						.fromJson(new FileReader("reports/sample_predictions.json"), listOfMyClassObject);
		
				System.out.println(PredictionDashboard.getHeaderString("|", PredictionDashboard.fields));
		
				for (String DTXSID : htResultsAll.keySet()) {
		
					// if (DTXSID.equals("DTXSID9034941")) continue;
					// if (DTXSID.equals("DTXSID20211176")) continue;
		
					List<PredictionResults> listPredictionResults = htResultsAll.get(DTXSID);
		
					for (PredictionResults pr : listPredictionResults) {
						PredictionDashboard pd = convertResultsToPredictionFormat(pr, true);
						System.out.println(pd.toString("|", PredictionDashboard.fields));
					}
				}
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}

		public static PredictionDashboard convertResultsToPredictionFormat(PredictionResults pr,
				boolean convertPredictionMolarUnits) {
		
			PredictionDashboard pd = new PredictionDashboard();
		
			try {
		
				PredictionResultsPrimaryTable pt = pr.getPredictionResultsPrimaryTable();
		
				try {
		
					pd.source = "TEST v" + TESTConstants.SoftwareVersion;
		
					pd.property_name = pr.getEndpoint().replace(" at 25°C", "");
					pd.method = pr.getMethod();
		
					pd.smiles = pr.getSmiles();
					pd.dtxsid = pr.getDTXSID();
		
					if (pr.getError() != null && !pr.getError().isEmpty()) {
						pd.prediction_error = pr.getError();
					} else {
						if (pr.isBinaryEndpoint()) {
							if (pt.getPredToxValue().equals("N/A")) {
								pd.prediction_error = pt.getMessage();
							} else {
								pd.prediction_value = pt.getPredToxValue();
								pd.prediction_units = "Binary";
								pd.prediction_string = pt.getPredValueConclusion();
							}
		
						} else if (pr.isLogMolarEndpoint()) {
		
							if (pt.getPredToxValue().equals("N/A")) {
								pd.prediction_error = pt.getMessage();
							} else {
								if (convertPredictionMolarUnits)
									convertLogMolarUnits(pd, pt);
								else {
									pd.prediction_value = pt.getPredToxValue();
									pd.prediction_units = pt.getMolarLogUnits();
								}
		
							}
						} else {
		
							if (pt.getPredToxValMass().equals("N/A")) {
								pd.prediction_error = pt.getMessage();
							} else {
		
								pd.prediction_value = pt.getPredToxValMass();
		
								if (pt.getMassUnits().equals("g/cm³")) {
									pd.prediction_units = "g/cm3";
								} else if (pt.getMassUnits().equals("°C")) {
									pd.prediction_units = "C";
								} else {
									pd.prediction_units = pt.getMassUnits();
								}
								//
							}
						}
					}
		
					if (pd.prediction_error != null) {
						if (pd.prediction_error.equals("No prediction could be made")) {
							pd.prediction_error = "No prediction could be made due to applicability domain violation";
						} else if (pd.prediction_error.contains("could not parse")) {
							pd.prediction_error = "Could not parse smiles";
						}
					}
		
					// System.out.println(gson.toJson(pd));
				} catch (Exception ex) {
					// System.out.println(gson.toJson(pr));
					ex.printStackTrace();
				}
		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			return pd;
		}

		public static String getHeaderString(String del, String[] fieldNames) {
			String result = "";
			for (int i = 0; i < fieldNames.length; i++) {
				if (fieldNames[i].contains(del))
					fieldNames[i] = "\"" + fieldNames[i] + "\"";
				result += fieldNames[i];
				if (i < fieldNames.length - 1)
					result += del;
			}
			return result;
		}

	}

	void parseTsv() {
		String filepath = "reports/TEST_results_all_endpoints_snapshot_compounds1.tsv";
		Path path = Paths.get(filepath);

		try {
			List<String> lines = Files.readAllLines(path);

			for (int i = 1; i < lines.size(); i++) {
				String line = lines.get(i);

				String[] values = line.split("\t");

				String DTXSID = values[0];
				String report = values[3];

				// System.out.println(report);

				JsonObject joReport = gson.fromJson(report, JsonObject.class);
				System.out.println(DTXSID);
				System.out.println(gson.toJson(joReport));
			}

			// System.out.println(strTsv);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void runSIDList() {

		String method = TESTConstants.ChoiceConsensus;

//		List<String>endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);
		List<String> endpoints = allEndpoints;

		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

//		String filepath="reports/DTXSIDs for Todd.txt";
		String filepath = "reports/INTERPRET_NTA_confirmed_IDs.txt";

		String destJsonPath = filepath.replace("txt", "json");

		if (debug)
			System.out.println("");
		for (String endpoint : endpoints) {
			if (debug)
				System.out.println("Loading " + endpoint);
			WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
														// persistent
		}

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));
			FileWriter fw = new FileWriter(destJsonPath);

			while (true) {
				String sid = br.readLine();
				if (sid == null)
					break;

				IAtomContainer ac = MoleculeCreator.createMoleculeFromResolverBySID(sid);// mismatch with standalone

				if (ac == null) {
					System.out.println("cant load " + sid);
					continue;
				}

				DSSToxRecord dr = ac.getProperty("DSSToxRecord");

				System.out.println(sid + "\t" + dr.name + "\t" + dr.cas);

				List<PredictionResults> results = runEndpointsAsList(ac, endpoints, method, createReports,
						createDetailedReports);
				for (PredictionResults pr : results) {
					fw.write(gsonNotPretty.toJson(pr) + "\r\n");
					fw.flush();
				}
			}

			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	static void runSIDListCalcDescriptors() {

		String filepath = "reports/DTXSIDs for Todd-mismatch.txt";

		String destJsonPath = filepath.replace("txt", "tsv");

		WebTEST4 webtest = new WebTEST4();
		WebTEST4.createDetailedReports = false;
		WebTEST4.createReports = false;

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));
			FileWriter fw = new FileWriter(destJsonPath);

			while (true) {

				String sid = br.readLine();

//				System.out.println(sid);

				if (sid == null)
					break;

				IAtomContainer ac = MoleculeCreator.createMoleculeFromResolverBySID(sid);// mismatch with standalone

				if (ac == null) {
					System.out.println("cant load " + sid);
					continue;
				}

				DescriptorData dd = webtest.goDescriptors(ac);
				fw.write(sid + "\t" + dd.XLOGP + "\n");
				System.out.println(sid + "\t" + dd.XLOGP);
			}

			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) {
		RunFromSmiles r = new RunFromSmiles();

//	runSIDList();
//		r.lookForExpValue();
		runSingleEndpointExample();
//		runSingleChemicalFromSmiles();
//		runDTXSIDAllEndpoints();
//		runSIDList();
//		runSIDListCalcDescriptors();
	}

	private void lookForExpValue() {
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\PredictionResults reports\\";

		String filenameJson = "prod_compounds_updated_lt_2024-11-12_1.json";
		String destJsonPath = folderDest + filenameJson;

		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			BufferedReader br = new BufferedReader(new FileReader(destJsonPath));

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				PredictionResults pr = gson.fromJson(line, PredictionResults.class);

				if (pr.getPredictionResultsPrimaryTable() == null)
					continue;

				if (pr.getPredictionResultsPrimaryTable().getExpCAS() != null) {
					if (pr.getPredictionResultsPrimaryTable().getExpSet().toLowerCase().contains("train")) {
						System.out.println(gson.toJson(pr));
						break;
					}

				}

			}

			br.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

