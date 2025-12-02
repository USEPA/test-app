package ToxPredictor.Application.Calculations.RunFromCommandLine;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.MoleculeCreator;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.ReportCreator;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.Utilities;
import gov.epa.test.api.predict.PredictController;
import gov.epa.test.api.predict.PredictController.PostInput;
import gov.epa.test.api.predict.TestApi;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

/**
 * @author TMARTI02
 */
public class RunFromSDF {

	public static boolean debug = true;

	public static List<String> allEndpoints = Arrays.asList(TESTConstants.ChoiceFHM_LC50, TESTConstants.ChoiceDM_LC50,
			TESTConstants.ChoiceTP_IGC50, TESTConstants.ChoiceRat_LD50, TESTConstants.ChoiceBCF,
			TESTConstants.ChoiceReproTox, TESTConstants.ChoiceMutagenicity,
//			TESTConstants.ChoiceEstrogenReceptor,
//			TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity,
			TESTConstants.ChoiceBoilingPoint, TESTConstants.ChoiceVaporPressure, TESTConstants.ChoiceMeltingPoint,
			TESTConstants.ChoiceDensity, TESTConstants.ChoiceFlashPoint, TESTConstants.ChoiceSurfaceTension,
			TESTConstants.ChoiceThermalConductivity, TESTConstants.ChoiceViscosity,
			TESTConstants.ChoiceWaterSolubility);

	private static final Logger logger = LogManager.getLogger(RunFromSDF.class);
//	private static Gson gsonNotPretty = new Gson();
	
	 private static Gson gsonNotPretty = new GsonBuilder()
             .disableHtmlEscaping()
             .create();


	public static void runSDF(String SDFFilePath, String endpoint, String destJsonPath, boolean skipMissingSID,
			int maxCount) {

		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		AtomContainerSet acs = readSDFV3000(SDFFilePath);

		AtomContainerSet acs2 = filterAtomContainerSet(acs, skipMissingSID, maxCount);

		acs = null;
		System.out.println(acs2.getAtomContainerCount());

		// Hashtable<String,List<PredictionResults>>htResultsAll=
		// runEndpoints(acs2, allEndpoints,
		// method,createReports,createDetailedReports,DSSToxRecord.strSID);

		List<String> endpoints = Arrays.asList(endpoint);

		List<PredictionResults> results = runEndpointsAsList(acs2, endpoints, method, createReports,
				createDetailedReports);

		ReportCreator.saveJson(destJsonPath, results);

		// Write webpages to look at them
		// for (PredictionResults predictionResults:results) {
		// String reportBase="reports";
		// writeWebPage(predictionResults, reportBase, predictionResults.getDTXCID());
		// }

	}

	public static void runSDF_all_endpoints(String SDFFilePath, String destJsonPath, boolean skipMissingSID,
			int maxCount) {

		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		AtomContainerSet acs = readSDFV3000(SDFFilePath);

		System.out.println("atom container count=" + acs.getAtomContainerCount());

		AtomContainerSet acs2 = filterAtomContainerSet(acs, skipMissingSID, maxCount);

		System.out.println("atom container count=" + acs2.getAtomContainerCount());


		List<PredictionResults> results = runEndpointsAsList(acs2, allEndpoints, method, createReports,
				createDetailedReports);
		
		ReportCreator.saveJson(destJsonPath, results);
		
		// RunFromSmiles.saveResultsAsTsv(destJsonPath.replace(".json", ".tsv"), results);

		// PredictionResults predictionResults=results.get(0);
		// String reportBase="reports";
		// RunFromSmiles.writeWebPage(predictionResults, reportBase, predictionResults.getDTXCID());

	}

	public static void runSDF_all_endpoints_write_continuously(String SDFFilePath, String destJsonPath,
			boolean skipMissingSID, int maxCount, boolean removeAlreadyRan) {

		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		// AtomContainerSet acs=readSDFV3000(SDFFilePath);

		AtomContainerSet acs = readSDF(SDFFilePath, maxCount, false);
		if (debug)
			System.out.println("atom container count in sdf=" + acs.getAtomContainerCount());
		AtomContainerSet acs2 = filterAtomContainerSet(acs, skipMissingSID, maxCount);
		if (debug)
			System.out.println("atom container count filtered=" + acs2.getAtomContainerCount());

		// if(true)return;

		try {

			File destFile = new File(destJsonPath);
			FileWriter fw;
			int countRan = 0;

			if (removeAlreadyRan) {
				if (destFile.exists()) {
					countRan = removeAlreadyRanChemicals(destJsonPath, acs2);
					if (debug)
						System.out.println(countRan + " removed since already ran");
				}
				fw = new FileWriter(destJsonPath, destFile.exists());
			} else {
				fw = new FileWriter(destJsonPath);
			}

			if (debug)
				System.out.println("atom container count to run=" + acs2.getAtomContainerCount());

			// if(true)return;

			if (acs2.getAtomContainerCount() == 0) {
				if (debug)
					System.out.println("All chemicals ran");
				return;
			}

			if (debug)
				System.out.println("");
			for (String endpoint : allEndpoints) {
				if (debug)
					System.out.println("Loading " + endpoint);
				WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
															// persistent
			}
			if (debug)
				System.out.println("");

			for (int i = 0; i < acs2.getAtomContainerCount(); i++) {

				AtomContainer ac = (AtomContainer) acs2.getAtomContainer(i);

				// if (debug)
				// System.out.println((i+countRan)+"\t"+destFile.getName()+"\t"+ac.getProperty("SMILES")+"");

				if (debug)
					logger.info("{}\t{}\t{}\t{}", (i + countRan + 1), destFile.getName(), ac.getProperty("DTXSID"),
							ac.getProperty("SMILES"));

				List<PredictionResults> results = RunFromSmiles.runEndpointsAsList(ac, allEndpoints, method, createReports,
						createDetailedReports);

				for (PredictionResults pr : results) {

					fw.write(gsonNotPretty.toJson(pr) + "\r\n");

					// if(pr.getPredictionResultsPrimaryTable()!=null ) {
					// if(pr.getPredictionResultsPrimaryTable().getExpToxValue()!=null) {
					// if(!pr.getPredictionResultsPrimaryTable().getExpToxValue().equals("N/A")) {
					// System.out.println(pr.getDTXSID()+"\t"+pr.getEndpoint());
					// }
					// }
					// }

					fw.flush();
				}
			}
			fw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void runSDF_all_endpoints_write_continuously_use_api(String SDFFilePath, String destJsonPath,
			boolean skipMissingSID, int maxCount, boolean removeAlreadyRan, String server, int port) {

		Gson gson=new Gson();
		
	    long beforeUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		
		//TODO make the api implement these as variables in the api input
//		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and
//		boolean createReports = true;// whether to store report
//		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		// AtomContainerSet acs=readSDFV3000(SDFFilePath);

		List<APIMolecule>molecules = readSDF_to_API_Molecules(SDFFilePath, maxCount, false);
		
		
		if (debug)
			System.out.println("atom container count in sdf=" + molecules.size());
		
		filterAtomContainerSet(molecules, skipMissingSID, maxCount);
		
		
		if (debug)
			System.out.println("atom container count filtered=" + molecules.size());

		// if(true)return;

		try {

			File destFile = new File(destJsonPath);
			FileWriter fw;
			int countRan = 0;

			if (removeAlreadyRan) {
				if (destFile.exists()) {
					countRan = removeAlreadyRanChemicals(destJsonPath, molecules);
					if (debug)
						System.out.println(countRan + " removed since already ran");
				}
				fw = new FileWriter(destJsonPath, Charset.forName("UTF-8"),destFile.exists());
			} else {
				fw = new FileWriter(destJsonPath);
			}

			
			System.gc(); // Request garbage collection to get a more accurate 'after' reading
		    long afterUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		    long memoryUsedByObject = afterUsedMemory - beforeUsedMemory;
		    
		    
            double memoryInMB =memoryUsedByObject / (1024 * 1024);
            
            System.out.println("Memory usage:"+memoryInMB+" MB");

			
//			if(true)return;
			
			
			if (debug)
				System.out.println("atom container count to run=" + molecules.size());

			// if(true)return;

			if (molecules.size() == 0) {
				if (debug)
					System.out.println("All chemicals ran");
				return;
			}

			if (debug)
				System.out.println("");
			
			
			
		if (debug)
				System.out.println("");

		
			int counter=0;
		
			while (molecules.size()>0) {
			
//			for (APIMolecule molecule:molecules) {

				APIMolecule molecule=molecules.remove(0);
				
				
				// if (debug)
				// System.out.println((i+countRan)+"\t"+destFile.getName()+"\t"+ac.getProperty("SMILES")+"");

				if (debug)
					logger.info("{}\t{}\t{}\t{}", (++counter+countRan), destFile.getName(), molecule.htProperties.get("DTXSID"),
							molecule.htProperties.get("SMILES"));


				List<PredictionResults>results=TestApi.runPredictionFromMolFileString(molecule.toString(), server, port);
				
				if(results==null)continue;
				
				for (PredictionResults pr : results) {

					fw.write(gson.toJson(pr) + "\r\n");

					// if(pr.getPredictionResultsPrimaryTable()!=null ) {
					// if(pr.getPredictionResultsPrimaryTable().getExpToxValue()!=null) {
					// if(!pr.getPredictionResultsPrimaryTable().getExpToxValue().equals("N/A")) {
					// System.out.println(pr.getDTXSID()+"\t"+pr.getEndpoint());
					// }
					// }
					// }

					fw.flush();
				}
			}
			fw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int removeAlreadyRanChemicals(String destJsonPath, List<APIMolecule> molecules) {
		System.out.println(destJsonPath);

		Hashtable<String, Integer> htCountByDTXCID = new Hashtable<>();

		try {

			File file=new File(destJsonPath);
			
			BufferedReader br = new BufferedReader(new FileReader(destJsonPath));

			
			int counter=0;
			
			while (true) {
				String line = br.readLine();

				if (line == null)
					break;

				counter++;
				
				try {
				
//					PredictionResults pr = gsonNotPretty.fromJson(line, PredictionResults.class);
//
//					if (pr.getDTXCID() == null)
//						continue;
					
					if(!line.contains("DTXCID")) continue;
					
					
					String dtxcid = getFieldFromJson(line, "DTXCID");
					String error = getFieldFromJson(line, "error");
					
					if(!line.contains("Q2_Test") && !line.contains("SP_Test") && error.equals("")) {
						//if doesnt have the stats then it didnt finish writing the line
						System.out.println(destJsonPath+"\t"+dtxcid+"\t"+line);
						continue;
					}

					if(counter%100000==0) System.out.println("\t"+file.getName()+"\t"+counter);
					
					// System.out.println(pr.getDTXCID());

//					if (htCountByDTXCID.containsKey(pr.getDTXCID())) {
//						htCountByDTXCID.put(pr.getDTXCID(), htCountByDTXCID.get(pr.getDTXCID()) + 1);
//					} else {
//						htCountByDTXCID.put(pr.getDTXCID(), 1);
//					}
					
					if (htCountByDTXCID.containsKey(dtxcid)) {
						htCountByDTXCID.put(dtxcid, htCountByDTXCID.get(dtxcid) + 1);
					} else {
						htCountByDTXCID.put(dtxcid, 1);
					}

				} catch (Exception ex) {
//					JsonObject jo = gsonNotPretty.fromJson(line, JsonObject.class);
//					System.out.println(Utilities.toJson(jo));
				}

			}

			br.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// for (String line:lines) {
		// System.out.println(gson.toJson(htCountByDTXCID));
		int count = 0;

		Iterator<APIMolecule> iterator = molecules.iterator();
		while (iterator.hasNext()) {
			APIMolecule molecule = iterator.next();

			String DTXCID = (String) molecule.htProperties.get("DTXCID");

			if (htCountByDTXCID.containsKey(DTXCID)) {
				if (htCountByDTXCID.get(DTXCID) == 16) {
					iterator.remove(); // Removes safely
					count++;
				}
			}
		}
		System.out.println("Removed " + count + " chemicals since already ran");
		return count;
	}

	private static void filterAtomContainerSet(List<APIMolecule> molecules, boolean skipMissingSID,
			int maxCount) {

		int count=0;
		
		Iterator<APIMolecule> iterator = molecules.iterator();
		
		while (iterator.hasNext()) {
			
			APIMolecule molecule=iterator.next();
			String SID = (String)molecule.htProperties.get("DTXSID");
			
			if (skipMissingSID && SID == null) {
				// System.out.println("Skipping");
				iterator.remove();
			}
			count++;

			if (count == maxCount)
				break;

		}
	
	}

	public static int removeAlreadyRanChemicals(String destJsonPath, AtomContainerSet acs2) throws IOException {

		System.out.println(destJsonPath);

		BufferedReader br = new BufferedReader(new FileReader(destJsonPath));

		// for (String line:lines) {

		HashSet<String> dtxsids = new HashSet<>();

		Hashtable<String, Integer> htCountByDTXCID = new Hashtable<>();

		
		
		while (true) {
			String line = br.readLine();

			if (line == null)
				break;

			PredictionResults pr = gsonNotPretty.fromJson(line, PredictionResults.class);

			if (pr.getDTXCID() == null)
				continue;

			// System.out.println(pr.getDTXCID());

			if (htCountByDTXCID.containsKey(pr.getDTXCID())) {
				htCountByDTXCID.put(pr.getDTXCID(), htCountByDTXCID.get(pr.getDTXCID()) + 1);
			} else {
				htCountByDTXCID.put(pr.getDTXCID(), 1);
			}
		}

		// System.out.println(gson.toJson(htCountByDTXCID));

		br.close();

		int count = 0;

		for (int i = 0; i < acs2.getAtomContainerCount(); i++) {
			AtomContainer ac = (AtomContainer) acs2.getAtomContainer(i);
			String DTXCID = ac.getProperty("DTXCID");
			// System.out.println(DTXCID+"\t"+pr.getDTXCID());

			if (htCountByDTXCID.containsKey(DTXCID)) {
				if (htCountByDTXCID.get(DTXCID) == 16) {
					acs2.removeAtomContainer(i--);
					count++;
				}
			}

		}
		System.out.println("Removed " + count + " chemicals since already ran");
		return count;
	}

	public static int removeAlreadyRanChemicalsOld(String destJsonPath, AtomContainerSet acs2) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(destJsonPath));
		String lastLine = "";
		// for (String line:lines) {
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			lastLine = line;
		}
		br.close();

		PredictionResults pr = gsonNotPretty.fromJson(lastLine, PredictionResults.class);

		int count = 0;

		for (int i = 0; i < acs2.getAtomContainerCount(); i++) {
			AtomContainer ac = (AtomContainer) acs2.getAtomContainer(i);
			String DTXCID = ac.getProperty("DTXCID");
			// System.out.println(DTXCID+"\t"+pr.getDTXCID());

			acs2.removeAtomContainer(i--);

			count++;

			if (DTXCID.equals(pr.getDTXCID()))
				break;
		}

		// System.out.println("Removed "+count+" chemicals since already ran");
		return count;
	}
	

	/**
	 * Instead of converting molecule in SDF back and forth, just store structure as string and convert to IAtomContainer in the api
	 */
	public static class APIMolecule {
		APIMolecule(String strStructure,Hashtable<String,Object>htProperties) {
			this.strStructure=strStructure;
			this.htProperties=htProperties;
		}
		public String strStructure;
		public Hashtable<String,Object>htProperties;
		
		@Override
		public String toString() {
			String strMolecule=strStructure;
			strMolecule+="\n";
			for(String property:htProperties.keySet()) {
				strMolecule+="> <"+property+">\n";
				strMolecule+=htProperties.get(property)+"\n\n";
			}
			
			strMolecule+="$$$\n";
			return strMolecule;
		}
	}
	
	
	
	public static List<APIMolecule> readSDF_to_API_Molecules(String sdfFilePath, int count, boolean insideJar) {
		return readSDF_to_API_Molecules(sdfFilePath, count, insideJar,null);
	}
	
	/**
	 * Make it so that it doesnt convert to IAtomContainer object, just parse sdf into a better formatted mol string
	 * 
	 * @param sdfFilePath
	 * @param count
	 * @param insideJar
	 * @param encoding "UTF-8", "Windows-1252", etc

	 * 
	 * @return
	 */
	public static List<APIMolecule> readSDF_to_API_Molecules(String sdfFilePath, int count, boolean insideJar,String encoding) {
		
		List<APIMolecule>molecules=new ArrayList<>();
		
		try {

			
			BufferedReader br = null;

			if (insideJar) {
				// System.out.println(sdfFilePath);
				java.io.InputStream ins = RunFromSmiles.class.getClassLoader().getResourceAsStream(sdfFilePath);
				InputStreamReader isr = new InputStreamReader(ins);
				br = new BufferedReader(isr);
			} else {
				FileInputStream fis = new FileInputStream(sdfFilePath);
				
				if(encoding!=null) {
					br = new BufferedReader(new InputStreamReader(fis, encoding));
				} else {
					br = new BufferedReader(new InputStreamReader(fis));	
				}
//				
			}

			while (true) {
				String strStructure = MoleculeCreator.getStringStructure(br);
				Hashtable<String,Object>htProperties=MoleculeCreator.getPropertiesHashtable(br);
				if(strStructure==null || htProperties==null) break;
				molecules.add(new APIMolecule(strStructure,htProperties));
				
				if(molecules.size()==count)break;
				
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return molecules;
	}
	

	public static AtomContainerSet readSDF(String sdfFilePath, int count, boolean insideJar) {

		AtomContainerSet acs = new AtomContainerSet();
		MDLV3000Reader mr = new MDLV3000Reader();
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		try {

			BufferedReader br = null;

			if (insideJar) {
				// System.out.println(sdfFilePath);
				java.io.InputStream ins = RunFromSmiles.class.getClassLoader().getResourceAsStream(sdfFilePath);
				
				if(ins==null) {
					System.out.println(sdfFilePath+" is missing");
					return null;
				}
				InputStreamReader isr = new InputStreamReader(ins);
				br = new BufferedReader(isr);
			} else {
				FileInputStream fis = new FileInputStream(sdfFilePath);
				br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			}

			int counter = 0;

			while (true) {

				IAtomContainer molecule = MoleculeCreator.getMoleculeWithProperties(br);
				counter++;

				// if(counter%1000==0) System.out.println(counter);

				if (molecule == null)
					break;

				// String SID=molecule.getProperty("DTXSID");
				// String CID=molecule.getProperty("DTXCID");
				//
				// if(SID==null) {
				// System.out.println("SID missing for "+CID);
				// }

				if (molecule.getAtomCount() == 0) {
					molecule = MoleculeCreator.createMoleculeFromSmilesProperty(sp, molecule);
					acs.addAtomContainer(molecule);
				} else {
					acs.addAtomContainer(molecule);
				}

				if (acs.getAtomContainerCount() == count)
					break;

			}

			br.close();
			mr.close();
		} catch (Exception ex) {
			System.out.println(sdfFilePath+" missing");
			ex.printStackTrace();
		}
		return acs;
	}

	/**
	 * Writing my own V3000 reader because CDK sucks and cant read SDFs for all the
	 * dashboard chemicals and get the properties too
	 * 
	 * @param sdfFilePath
	 * @return
	 */
	public static AtomContainerSet readSDFV3000(String sdfFilePath) {

		AtomContainerSet acs = new AtomContainerSet();

		MDLV3000Reader mr = new MDLV3000Reader();

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		try {

			FileInputStream fis = new FileInputStream(sdfFilePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

			boolean stop = false;

			while (true) {

				String strStructure = "";

				while (true) {
					String Line = br.readLine();

					if (Line == null) {
						stop = true;
						break;
					}

					// System.out.println(Line);
					strStructure += Line + "\r\n";
					if (Line.contains("M  END"))
						break;
				}

				if (stop)
					break;

				InputStream stream = new ByteArrayInputStream(strStructure.getBytes());
				mr.setReader(stream);

				IAtomContainer molecule = null;

				try {
					molecule = (IAtomContainer) mr.readMolecule(DefaultChemObjectBuilder.getInstance());
				} catch (Exception ex) {
					molecule = new AtomContainer();
				}

				while (true) {
					String Line = br.readLine();
					// System.out.println(Line);

					if (Line.contains(">  <")) {
						String field = Line.substring(Line.indexOf("<") + 1, Line.length() - 1);
						String value = br.readLine();
						molecule.setProperty(field, value);
						// System.out.println(field);
					}

					if (Line.contains("$$$"))
						break;
				}

				if (molecule.getAtomCount() == 0) {

					AtomContainer molecule2 = null;

					String smiles = molecule.getProperty("smiles");

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

	static void runSampleSDF() {
		boolean removeAlreadyRan = false;
		// String filepathSDF="reports/sample.sdf";
		String filepathSDF = "reports/snapshot_compounds35.sdf";

		int maxCount = 10000;// set to -1 to run all in sdf
		boolean skipMissingSID = true;

		String destJsonPath = filepathSDF.replace("sdf", "json");
		runSDF_all_endpoints_write_continuously(filepathSDF, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan);

	}

	static void runSDFSample() {

		String folderSrc = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\RunTestCalculationsFromJar\\sdf\\";

		String fileNameSDF = "sample.sdf";
		String filepathSDF = folderSrc + fileNameSDF;

		System.out.println(fileNameSDF);

		int maxCount = -1;// set to -1 to run all in sdf
		boolean skipMissingSID = true;
		String destJsonPath = folderSrc + fileNameSDF.replace(".sdf", "_2.json");

		System.out.println(new File(destJsonPath).getAbsolutePath());

		boolean debug = true;
		runSDF_all_endpoints_write_continuously(filepathSDF, destJsonPath, skipMissingSID, maxCount, debug);

		ReportCreator.lookAtPrettyJson(destJsonPath);

	}

	/**
	 * 
	 * 
	 * 
	 */
	public void runSDF(int num) {
		
		debug=true;
		
		boolean removeAlreadyRan = true;
		boolean useServer=true;
		
		int maxCount = -1;// set to -1 to run all in sdf
//		int maxCount = 100;// set to -1 to run all in sdf

		
		int port = 8081 + num - 1;		
		
		String server="http://v2626umcth882.rtord.epa.gov";
//		String server="http://localhost";

		String snapshot = "snapshot-2025-07-30";

		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		// String folder = folderMain + "data\\dsstox\\snapshot-2024-11-12\\sdf\\";

		String folderSrc = folderMain + "data\\dsstox\\" + snapshot + "\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";

		new File(folderDest).mkdirs();

		
		int from = 1 + 50000 * (num - 1);
		String filenameSDF = "50k_chunk_from_" + from + ".sdf";

		String filenameJson = filenameSDF.replace(".sdf", ".json");

		
		boolean skipMissingSID = true;

		String sdfPath = folderSrc + filenameSDF;
		String destJsonPath = folderDest + filenameJson;

		System.out.println("num="+num+",fileName="+filenameSDF);
		
		if(useServer) {
			runSDF_all_endpoints_write_continuously_use_api(sdfPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan,server,port);
		} else {
			runSDF_all_endpoints_write_continuously(sdfPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan);
		}
		

	}
	
	
	

	/**
	 * Run predictions in batch so don't have to reload model info each time Results
	 * stored in hashtable by DTXSID
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
	public static Hashtable<String, PredictionResults> runEndpoint(AtomContainerSet moleculeSet, String endpoint,
			String method, boolean createReports, boolean createDetailedReports, String key) {
	
		DescriptorFactory.debug = false;
		WebTEST4.createDetailedReports = createDetailedReports;
		WebTEST4.createReports = createReports;
		WebTEST4.generateWebpages = false;
		PredictToxicityJSONCreator.forGUI = true;
	
		Hashtable<String, PredictionResults> htResults = new Hashtable<>();
	
		// *******************************************************
		WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
													// persistent
	
		List<String> endpoints = Arrays.asList(endpoint);
	
		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			
			if((i+1)%100==0) System.out.println("\t"+(i+1));
			
			IAtomContainer ac = moleculeSet.getAtomContainer(i);
			ac = RunFromSmiles.calculate(ac, endpoints, method);
			PredictionResults pr = RunFromSmiles.getResults(ac, method);
	
			String DTXSID = ac.getProperty(DSSToxRecord.strSID);
			String Smiles = ac.getProperty(DSSToxRecord.strSmiles);
			String error = (String) ac.getProperty("Error");
			String CAS = null;
	
			// System.out.println(DTXSID);
	
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
	 * Run predictions in batch so don't have to reload model info each time Results
	 * stored in hashtable by DTXSID
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
	public static Hashtable<String, List<PredictionResults>> runEndpoints(AtomContainerSet moleculeSet,
			List<String> endpoints, String method, boolean createReports, boolean createDetailedReports, String key) {
	
		DescriptorFactory.debug = false;
		WebTEST4.createDetailedReports = createDetailedReports;
		WebTEST4.createReports = createReports;
		WebTEST4.generateWebpages = false;
		PredictToxicityJSONCreator.forGUI = true;
	
		Hashtable<String, List<PredictionResults>> htResults = new Hashtable<>();
	
		// *******************************************************
	
		for (String endpoint : endpoints)
			WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
														// persistent
	
		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			IAtomContainer ac = (AtomContainer) moleculeSet.getAtomContainer(i);
			ac = RunFromSmiles.calculate(ac, endpoints, method);
			List<PredictionResults> resultsArray = RunFromSmiles.getResultsArray(ac, method);
	
			List<PredictionResults> resultsArray2 = new ArrayList<>();
	
			String DTXSID = null;
			if (ac.getProperty(RunFromSmiles.strSID) != null)
				DTXSID = ac.getProperty(RunFromSmiles.strSID);
			else if (ac.getProperty(DSSToxRecord.strSID) != null)
				DTXSID = ac.getProperty(DSSToxRecord.strSID);
			ac.setProperty(DSSToxRecord.strSID, DTXSID);
	
			String DTXCID = null;
			if (ac.getProperty(RunFromSmiles.strCID) != null)
				DTXCID = ac.getProperty(RunFromSmiles.strCID);
			else if (ac.getProperty(DSSToxRecord.strCID) != null)
				DTXCID = ac.getProperty(DSSToxRecord.strCID);
			ac.setProperty(DSSToxRecord.strCID, DTXCID);
	
			String CAS = null;
			if (ac.getProperty(RunFromSmiles.strCAS) != null)
				CAS = ac.getProperty(RunFromSmiles.strCAS);
			else if (ac.getProperty(DSSToxRecord.strCAS) != null)
				CAS = ac.getProperty(DSSToxRecord.strCAS);
			ac.setProperty(DSSToxRecord.strCAS, CAS);
	
			String Smiles = ac.getProperty(DSSToxRecord.strSmiles);
			String error = (String) ac.getProperty("Error");
	
			// System.out.println(DTXSID);
	
			for (int j = 0; j < endpoints.size(); j++) {
	
				PredictionResults pr = null;
	
				if (resultsArray.get(j) == null) {
					pr = new PredictionResults();
					pr.setEndpoint(endpoints.get(j));
					pr.setMethod(method);
				} else {
					pr = resultsArray.get(j);
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

	public static AtomContainerSet filterAtomContainerSet(AtomContainerSet acs, boolean skipMissingSID, int maxCount) {
		AtomContainerSet acs2 = new AtomContainerSet();

		Iterator<IAtomContainer> iterator = acs.atomContainers().iterator();

		int count = 0;

		while (iterator.hasNext()) {
			IAtomContainer ac = iterator.next();
			String SID = ac.getProperty("DTXSID");
			if (skipMissingSID && SID == null) {
				// System.out.println("Skipping");
				continue;
			}
			acs2.addAtomContainer(ac);
			count++;
			// System.out.println(ac.getProperty("DTXSID")+"\t"+ac.getProperty("smiles"));

			WebTEST4.checkAtomContainer(ac);// theoretically the webservice has its own checking

			if (count == maxCount)
				break;
		}
		return acs2;
	}

	/**
	 * Run predictions in batch so don't have to reload model info each time Results
	 * stored in hashtable by DTXSID
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
	public static List<PredictionResults> runEndpointsAsList(AtomContainerSet moleculeSet, List<String> endpoints,
			String method, boolean createReports, boolean createDetailedReports) {

		DescriptorFactory.debug = false;
		WebTEST4.createDetailedReports = createDetailedReports;
		WebTEST4.createReports = createReports;
		WebTEST4.generateWebpages = false;
		PredictToxicityJSONCreator.forGUI = true;

		List<PredictionResults> Results = new ArrayList<>();

		// *******************************************************

		if (debug)
			System.out.print("Loading models/datasets ...");

		for (String endpoint : endpoints)
			WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
														// persistent

		if (debug)
			System.out.println("done");

		for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
			IAtomContainer ac = moleculeSet.getAtomContainer(i);

			String DTXSID = null;
			if (ac.getProperty(RunFromSmiles.strSID) != null)
				DTXSID = ac.getProperty(RunFromSmiles.strSID);
			else if (ac.getProperty(DSSToxRecord.strSID) != null)
				DTXSID = ac.getProperty(DSSToxRecord.strSID);
			ac.setProperty(DSSToxRecord.strSID, DTXSID);

			String DTXCID = null;
			if (ac.getProperty(RunFromSmiles.strCID) != null)
				DTXCID = ac.getProperty(RunFromSmiles.strCID);
			else if (ac.getProperty(DSSToxRecord.strCID) != null)
				DTXCID = ac.getProperty(DSSToxRecord.strCID);
			ac.setProperty(DSSToxRecord.strCID, DTXCID);

			String CAS = null;
			if (ac.getProperty(RunFromSmiles.strCAS) != null)
				CAS = ac.getProperty(RunFromSmiles.strCAS);
			else if (ac.getProperty(DSSToxRecord.strCAS) != null)
				CAS = ac.getProperty(DSSToxRecord.strCAS);
			ac.setProperty(DSSToxRecord.strCAS, CAS);

			// status=ac.getProperty(DSSToxRecord.strSmiles)+"";
			// System.out.println((i+1)+"\t"+ac.getProperty(DSSToxRecord.strSmiles)+"");

			ac = RunFromSmiles.calculate(ac, endpoints, method);
			List<PredictionResults> resultsArray = RunFromSmiles.getResultsArray(ac, method);

			// System.out.println(resultsArray.size());

			String Smiles = ac.getProperty(DSSToxRecord.strSmiles);
			String error = (String) ac.getProperty("Error");

			for (int j = 0; j < endpoints.size(); j++) {

				PredictionResults pr = null;

				if (resultsArray.get(j) == null) {

					pr = new PredictionResults();
					pr.setEndpoint(endpoints.get(j));
					pr.setMethod(method);
				} else {
					pr = resultsArray.get(j);

				}

				pr.setVersion(TESTConstants.SoftwareVersion);
				pr.setCAS(CAS);
				pr.setDTXSID(DTXSID);
				pr.setDTXCID(DTXCID);
				pr.setSmiles(Smiles);
				pr.setError(error);
				Results.add(pr);

				// Vector<SimilarChemical>similarChemicals0=pr.getSimilarChemicals().get(0).getSimilarChemicalsList();
				// SimilarChemical sc0_0=similarChemicals0.get(0);
				// System.out.println("here123:"+sc0_0.getCAS());

			}

		} // end loop over molecules
		return Results;
	}

	void displayWebpages(String dtxsid) {
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String snapshot = "snapshot-2025-07-30";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";
		String filePathJson=folderDest+"50k_chunk_from_1.json";
		ReportCreator.createWebPagesForDTXSID(filePathJson, dtxsid);
	}
 	
	
	
	
	String createSampleMol() {
		return 	"\r\n"
				+ "  Mrv1805 04221910482D          \r\n"
				+ "\r\n"
				+ "  0  0  0     0  0            999 V3000\r\n"
				+ "M  V30 BEGIN CTAB\r\n"
				+ "M  V30 COUNTS 12 16 0 0 0\r\n"
				+ "M  V30 BEGIN ATOM\r\n"
				+ "M  V30 1 C 1.7781 -1.54 0 0\r\n"
				+ "M  V30 2 C 2.6671 0 0 0\r\n"
				+ "M  V30 3 C 4.004 -0.77 0 0\r\n"
				+ "M  V30 4 C 4.004 -2.31 0 0\r\n"
				+ "M  V30 5 C 2.6671 -3.0801 0 0\r\n"
				+ "M  V30 6 C 1.3371 -2.31 0 0\r\n"
				+ "M  V30 7 C 1.3371 -0.77 0 0\r\n"
				+ "M  V30 8 O 0 -1.54 0 0\r\n"
				+ "M  V30 9 C 5.4671 -2.7861 0 0\r\n"
				+ "M  V30 10 C 6.3701 -1.54 0 0\r\n"
				+ "M  V30 11 C 5.4671 -0.294 0 0\r\n"
				+ "M  V30 12 O 7.0001 -0.133 0 0\r\n"
				+ "M  V30 END ATOM\r\n"
				+ "M  V30 BEGIN BOND\r\n"
				+ "M  V30 1 1 1 2\r\n"
				+ "M  V30 2 1 1 5\r\n"
				+ "M  V30 3 1 2 3\r\n"
				+ "M  V30 4 1 2 7\r\n"
				+ "M  V30 5 1 3 4\r\n"
				+ "M  V30 6 1 3 11\r\n"
				+ "M  V30 7 1 4 5\r\n"
				+ "M  V30 8 1 4 9\r\n"
				+ "M  V30 9 1 5 6\r\n"
				+ "M  V30 10 1 6 7\r\n"
				+ "M  V30 11 1 6 8\r\n"
				+ "M  V30 12 1 7 8\r\n"
				+ "M  V30 13 1 9 10\r\n"
				+ "M  V30 14 1 10 11\r\n"
				+ "M  V30 15 1 10 12\r\n"
				+ "M  V30 16 1 11 12\r\n"
				+ "M  V30 END BOND\r\n"
				+ "M  V30 END CTAB\r\n"
				+ "M  END\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "><DTXSID>\r\n"
				+ "DTXSID4020452\r\n"
				+ "\r\n"
				+ "><DTXCID>\r\n"
				+ "DTXCID00452\r\n"
				+ "\r\n"
				+ "><PREFERRED_NAME>\r\n"
				+ "Dicyclopentadiene dioxide\r\n"
				+ "\r\n"
				+ "><CASRN>\r\n"
				+ "81-21-0\r\n"
				+ "\r\n"
				+ "><INCHIKEY>\r\n"
				+ "BQQUFAMSJAKLNB-UHFFFAOYNA-N\r\n"
				+ "\r\n"
				+ "><INCHI_STRING>\r\n"
				+ "InChI=1/C10H12O2/c1-4-3-2-6-10(11-6)7(3)5(1)9-8(4)12-9/h3-10H,1-2H2\r\n"
				+ "AuxInfo=1/0/N:1,9,4,5,2,10,3,6,7,11,12,8/rA:12CCCCCCCOCCCO/rB:s1;s2;s3;s1s4;s5;s2s6;s6s7;s4;s9;s3s10;s10s11;/rC:1.7781,-1.54,0;2.6671,0,0;4.004,-.77,0;4.004,-2.31,0;2.6671,-3.0801,0;1.3371,-2.31,0;1.3371,-.77,0;0,-1.54,0;5.4671,-2.7861,0;6.3701,-1.54,0;5.4671,-.294,0;7.0001,-.133,0;\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "><SMILES>\r\n"
				+ "C1C2OC2C2C3CC(C4OC34)C12\r\n"
				+ "\r\n"
				+ "><IUPAC_NAME>\r\n"
				+ "Octahydro-1aH-2,4-methanoindeno[1,2-b:5,6-b']bisoxirene\r\n"
				+ "\r\n"
				+ "><MOLECULAR_FORMULA>\r\n"
				+ "C10H12O2\r\n"
				+ "\r\n"
				+ "><AVERAGE_MASS>\r\n"
				+ "164.204\r\n"
				+ "\r\n"
				+ "><MONOISOTOPIC_MASS>\r\n"
				+ "164.083729626\r\n"
				+ "\r\n"
				+ "$$$$";
				
	}
	
	void convertPredictionResultsToWebPages() {

		int num=1;
//		String casrn="75-07-0";
		String dtxsid="DTXSID5039224";
		Charset charset=Charset.forName("UTF-8");
		
		String snapshot = "snapshot-2025-07-30";
		
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";
		int from = 1 + 50000 * (num - 1);
		String filenameSDF = "50k_chunk_from_" + from + ".sdf";
		String filenameJson = filenameSDF.replace(".sdf", ".json");
		String destJsonPath = folderDest + filenameJson;
		String filenameHTML = dtxsid+".html";
		String destHtmlPath=folderDest+filenameHTML;
		
		Gson gson=new Gson();
		
		List<PredictionResults>prList=new ArrayList<>();
		
		try (BufferedReader br=new BufferedReader(new FileReader(destJsonPath,charset))) {
			
			while (true) {
				String line=br.readLine();
				if(line==null) break;
				PredictionResults pr = gson.fromJson(line, PredictionResults.class);
				if(pr.getDTXSID().equals(dtxsid)) prList.add(pr);
			}
			
			PredictController pc=new PredictController();
			String html=pc.getMultiEndpointHtml(prList);
			
			
			Utilities.toFile(html, destHtmlPath,charset);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	void getCountsPerJson() {
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\TEST5.1.3\\reports\\snapshot-2025-07-30";

		int total=0;
		
		for (File file:new File(folder).listFiles()) {
			if(!file.getName().contains(".json")) continue;
			
			try {

				BufferedReader br = new BufferedReader(new FileReader(file));

				HashSet<String>dtxcids=new HashSet<>();
				
//				System.out.println(br.readLine());
				int counter=0;
				
				while (true) {
					
					String line = br.readLine();
					
					if (line == null)
						break;

//					System.out.println(line);
					
					counter++;
					
					if(counter%100000==0) System.out.println("\t"+file.getName()+"\t"+counter);
					
					if(!line.contains("\"DTXCID\"")){
						continue;
					}
					
//					if(!line.contains("DTXCID")) continue;
					
					String dtxcid = getFieldFromJson(line, "DTXCID");
					String error = getFieldFromJson(line, "error");
					if(!line.contains("Q2_Test") && !line.contains("SP_Test") && error.equals("")) {
						System.out.println(file.getName()+"\t"+dtxcid+"\t"+line);
						continue;
					}
					dtxcids.add(dtxcid);
					
//					try {
//						PredictionResults pr = gsonNotPretty.fromJson(line, PredictionResults.class);
//						dtxcids.add(pr.getDTXCID());	
//					} catch (Exception ex) {
//						String dtxcid = getFieldFromJson(line, "DTXCID");
//						System.out.println(file.getName()+"\t"+dtxcid+"\t"+line);
//					}
					
//					System.out.println(dtxcid);
//					"DTXCID":"DTXCID101","Smiles"
					
					

				}
				
				br.close();
				
				total+=dtxcids.size();
				
				System.out.println(file.getName()+"\t"+dtxcids.size());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("total\t"+total);
		
	}

	private String getFieldFromJson(String line, String fieldName) {
		String value=line.substring(line.indexOf("\""+fieldName+"\":\""),line.length());
		value=value.substring(fieldName.length()+4,value.length());
		value=value.substring(0,value.indexOf("\""));
		
//		System.out.println(value);
//		
//		value=value.substring(0,value.indexOf("\""));
		return value;
	}
	
	
	class MyRunnableTask implements Runnable {
	    private int num;

	    public MyRunnableTask(int num) {
	        this.num = num;
	    }

	    @Override
	    public void run() {
	        System.out.println("num "+num + " is running in thread: " + Thread.currentThread().getName());
	        runSDF(num);
	    }
	}
	
	
	void runWithThreads() {
		
		for (int i = 1; i <= 9; i++) {
            MyRunnableTask task = this.new MyRunnableTask(i);
            Thread thread = new Thread(task, "Thread-" + i);
            thread.start(); // Starts the thread, which calls the run() method
        }
		
		
	}
	
	/*
	 * TODO:
	 * - Check if descriptors match previous release of TEST- they match for FHM LC50 training set for installed version of TEST 5.1.3
	 * - Check if predictions match previous release check if html reports look good
	 * - Why does Neighbors display CAS instead of DTXSID in NN report?
	 * - Update the snapshot db using charlies snapshot- take from materialized view when complete
	 * - Recalculate statistics using latest jsons
	 * - See if csvs have chemicals that sdfs dont
	 */
	
	public static void main(String[] args) {
		
		RunFromSDF r=new RunFromSDF();
		
//		r.runSDF(1);
//		r.runWithThreads();
//		r.getCountsPerJson();
		
		String path="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\EpisuiteISIS\\EPI_SDF_Data\\EPI_PCKOC_Data_SDF.sdf";
		List<APIMolecule>mols=RunFromSDF.readSDF_to_API_Molecules(path, -1, false);
		
		
		HashSet<String>srcs=new HashSet<>();
		for(APIMolecule mol:mols) {
			for(String key:mol.htProperties.keySet()) {
				if(key.contains("KocRef")) {
					srcs.add(mol.htProperties.get(key)+"");
				}
			}
		}
		
		for(String src:srcs) {
			System.out.println(src);	
		}
		
		
		
//		r.displayWebpages("DTXSID5039224");
		

//		int num = 1;
//		int port = 8081 + num - 1;		
//		String server="http://v2626umcth882.rtord.epa.gov";
//		List<PredictionResults>results=TestApi.runPredictionFromMolFileString(r.createSampleMol(), server, port);
//		System.out.println(Utilities.toJson(results));
//		Utilities.toJsonFile(results, "bob.json");
		
//		r.convertPredictionResultsToWebPages();
		
		
	}

}
