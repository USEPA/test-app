package ToxPredictor.Application.Calculations.CreateLookups;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.MyDescriptors.AtomicProperties;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.StatisticsCalculator.ModelPrediction;
import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;

/**
* @author TMARTI02
*/
public class GetTrainingTestSetPredictions {
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	private static final Logger logger = LogManager.getLogger(GetTrainingTestSetPredictions.class);
	
	
	@Deprecated
	public static Hashtable<String, Hashtable<String, Hashtable<String, String>>> getPredictionHashtableOld () {
		
		Hashtable<String,Hashtable<String,Hashtable<String,String>>> htDatasetPredictions=new Hashtable<>();
		
		for(String endpoint:RunFromSmiles.allEndpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
//			String jsonFilePath="gov"+File.separator+"epa"+File.separator+"webtest"+File.separator+abbrev+File.separator+abbrev+"_predictions.json";
			String jsonFilePath="gov/epa/webtest/"+abbrev+"/"+abbrev+"_predictions.json";

			Hashtable<String,Hashtable<String,String>>htByCAS=GetTrainingTestSetPredictions.getPredictionsLookupByCASOld(jsonFilePath, true);
			htDatasetPredictions.put(abbrev,htByCAS);
			logger.info(abbrev+"\t"+htByCAS.size());
			
		}
		
		return htDatasetPredictions;
	}

	
	public static Hashtable<String, List<ModelPrediction>> getPredictionHashtable () {
		
		Hashtable<String,List<ModelPrediction>> htDatasetPredictions=new Hashtable<>();
		
		for(String endpoint:RunFromSmiles.allEndpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
//			String jsonFilePath="gov"+File.separator+"epa"+File.separator+"webtest"+File.separator+abbrev+File.separator+abbrev+"_predictions.json";
			String jsonFilePath="gov/epa/webtest/"+abbrev+"/"+abbrev+"_predictions.json";

			List<ModelPrediction>mps=GetTrainingTestSetPredictions.getModelPredictions(jsonFilePath, true);
			
			HashSet<String>casrns=new HashSet<>();
			for(ModelPrediction mp:mps) {
				casrns.add(mp.id);
			}
			
			htDatasetPredictions.put(abbrev,mps);
			
//			logger.info("Loaded predictions for "+abbrev+"\t for "+casrns.size()+" casrns");
			
		}
		
		return htDatasetPredictions;
	}

	@Deprecated
	private static Hashtable<String,Hashtable<String,String>> getPredictionsLookupByCASOld(String jsonFilePath,boolean insideJar) {
		
//		String jsonFilePath = "/Datasets/training_and_test_set_predictions.json"; // Adjust the path based on your JAR structure
		
		if(insideJar) {
			
			try (InputStream inputStream = GetTrainingTestSetPredictions.class.getClassLoader().getResourceAsStream(jsonFilePath);
			        
	        		InputStreamReader reader = new InputStreamReader(inputStream)) {

	            // Define the type of the hashtable
	            Type hashtableType = new TypeToken<Hashtable<String,Hashtable<String,String>>>(){}.getType();

	            // Parse the JSON file into a Hashtable
	            Gson gson = new Gson();
	            Hashtable<String,Hashtable<String,String>> htByCAS = gson.fromJson(reader, hashtableType);
	            
	            //endpointAbbrev...cas...methodAbbrev => prediction as string
	            
	            return htByCAS;

	        } catch (Exception e) {
	        	System.out.println("Failed to load "+jsonFilePath);
	            e.printStackTrace();
	            return null;
	        }
			
		} else {
			try (InputStream inputStream = new FileInputStream(jsonFilePath);
			        
	        		InputStreamReader reader = new InputStreamReader(inputStream)) {

	            // Define the type of the hashtable
	            Type hashtableType = new TypeToken<Hashtable<String,Hashtable<String,String>>>(){}.getType();

	            // Parse the JSON file into a Hashtable
	            Gson gson = new Gson();
	            Hashtable<String,Hashtable<String,String>> htByCAS = gson.fromJson(reader, hashtableType);
	            
	            //endpointAbbrev...cas...methodAbbrev => prediction as string
	            
	            return htByCAS;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
			
		}
		
		
		
        // Read the JSON file from the JAR
        
    }
	
	
	public static List<ModelPrediction> getModelPredictions(String jsonFilePath,boolean insideJar) {
		
//		String jsonFilePath = "/Datasets/training_and_test_set_predictions.json"; // Adjust the path based on your JAR structure

        Type type = new TypeToken<List<ModelPrediction>>(){}.getType();

        
		
		if(insideJar) {
			
			try (InputStream inputStream = GetTrainingTestSetPredictions.class.getClassLoader().getResourceAsStream(jsonFilePath);
			        
	        		InputStreamReader reader = new InputStreamReader(inputStream)) {

	            // Define the type of the hashtable

	            // Parse the JSON file into a Hashtable
	            Gson gson = new Gson();
	            List<ModelPrediction> htByCAS = gson.fromJson(reader, type);
	            
	            //endpointAbbrev...cas...methodAbbrev => prediction as string
	            
	            return htByCAS;

	        } catch (Exception e) {
	        	System.out.println("Failed to load "+jsonFilePath);
	            e.printStackTrace();
	            return null;
	        }
			
		} else {
			try (InputStream inputStream = new FileInputStream(jsonFilePath);
			        
	        		InputStreamReader reader = new InputStreamReader(inputStream)) {

	            // Parse the JSON file into a Hashtable
	            Gson gson = new Gson();
	            List<ModelPrediction> htByCAS = gson.fromJson(reader, type);
	            
	            //endpointAbbrev...cas...methodAbbrev => prediction as string
	            
	            return htByCAS;

	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
			
		}
		
        // Read the JSON file from the JAR
        
    }
	
	void comparePredictionsInJarWithLatestPredictions() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		WebTEST4.printEachPrediction=false;
		
//		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceDM_LC50);
		List<String>endpoints=RunFromSmiles.allEndpoints;
		
//		RunFromSmiles.testReadFromJarStatic("ST/ST_training.sdf");
//		if(true)return;
		
		DecimalFormat df=new DecimalFormat("0.00");
		
		for(String endpoint:endpoints) {
			
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			
//			if(abbrev.equals("LC50DM")) continue;
			
//			String set="training";
			String set="prediction";
			System.out.println(abbrev+"\t"+set);
			
			String	sdfFilePath = abbrev+"/" + abbrev + "_"+set+".sdf";
			AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
			
//			for(int i=0;i<trainingACS.getAtomContainerCount();i++) {
//				IAtomContainer ac=trainingACS.getAtomContainer(i);
////				System.out.println((i+1)+"\t"+ac.getProperty("CAS")+"");
//			}

			String predfilename = abbrev + "/" + abbrev + " " + set + " set predictions.txt";
			if(set.equals("prediction")) predfilename=abbrev + "/" + abbrev + " test set predictions.txt";
			
			Hashtable<String, PredictionResults>htNewPredictions=RunFromSDF.runEndpoint(acs, endpoint, "Consensus", false, false, "CAS");
			Lookup lookup=new Lookup();
			
			for(String CAS:htNewPredictions.keySet()) {
				PredictionResults pr=htNewPredictions.get(CAS);
				Double pred=pr.getPredictionResultsPrimaryTable().getPredToxValue();
				Double predJar=lookup.LookUpValueConsensusValueOmitFDAInJarFile(predfilename, CAS, "CAS","\t");
				
				
				if(pred==null || predJar==null) {
					System.out.println("\t"+CAS+"\t"+pred+"\t"+predJar);
					continue;
				}
			
				
				
				if(Math.abs(pred-predJar)>0.05) 
					System.out.println("\t"+CAS+"\t"+df.format(pred)+"\t"+df.format(predJar));
				
				
//				System.out.println("\t"+CAS+"\t"+strPred+"\t"+strPredJar);

			}
			
//			System.out.println(gson.toJson(htTrain));
//			System.out.println(abbrev+"\ttraining\t"+trainingACS.getAtomContainerCount());
			String	sdfPred = abbrev+File.separator + abbrev + "_prediction.sdf";
		}
		
		
	}
	
	

	void createPredictionLookup() {
		
		boolean overWrite=true;
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		WebTEST4.printEachPrediction=false;
		
//		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceDM_LC50);
		
		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceSurfaceTension,TESTConstants.ChoiceViscosity);
		
//		List<String>endpoints=RunFromSmiles.allEndpoints;
		
//		RunFromSmiles.testReadFromJarStatic("ST/ST_training.sdf");
//		if(true)return;
		
//		Hashtable<String,Hashtable<String,Hashtable<String,String>>> htAll=new Hashtable<>();
		
		for(String endpoint:endpoints) {
			storePredictionsForSet(endpoint,overWrite);
//			if(true)break;
		}
		
//		try {
//			String folder="jar\\Datasets-1.1.1\\gov\\epa\\webtest\\";
//			FileWriter fw= new FileWriter (folder+"training_and_test_set_predictions.json");
//			fw.write(WebTEST4.gson.toJson(htAll));
//			fw.flush();
//			fw.close();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
//		for(String abbrev:htAll.keySet()) {
//			Hashtable<String,String>ht=htAll.get(abbrev);
//			System.out.println(ht.size());
//		}
		
	}
	
	

	void comparePredictionsWithChangeToAtomicProperties() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		WebTEST4.printEachPrediction=false;
		
//		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceFHM_LC50);
//		List<String>endpoints=RunFromSmiles.allEndpoints;
		
		
		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceFHM_LC50, 
				TESTConstants.ChoiceBCF,
				TESTConstants.ChoiceReproTox, 
				TESTConstants.ChoiceVaporPressure);

		
//		RunFromSmiles.testReadFromJarStatic("ST/ST_training.sdf");
//		if(true)return;
		
		Hashtable<String,Hashtable<String,Prediction>> htAll=new Hashtable<>();
		
		for(String endpoint:endpoints) {
			storePredictionsForSet2(htAll, endpoint, "training");
			storePredictionsForSet2(htAll, endpoint, "prediction");
		}
		
		
		Hashtable<String,Hashtable<String,Prediction>> htAll2=new Hashtable<>();
		AtomicProperties.reloadHashtables("SystemData/whim weights v3.0.txt");
		for(String endpoint:endpoints) {
			storePredictionsForSet2(htAll2, endpoint, "training");
			storePredictionsForSet2(htAll2, endpoint, "prediction");
		}
		
		
		for(String endpoint:endpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			
//			System.out.println("\n"+abbrev);

			Hashtable<String,Prediction>htPreds=htAll.get(abbrev);
			Hashtable<String,Prediction>htPreds2=htAll2.get(abbrev);
			
			
			double MAE=0;
			double MAE2=0;
			
			int count=0;
			int count2=0;
			
			for(String cas:htPreds.keySet()) {
				
				Prediction p=htPreds.get(cas);
				Prediction p2=htPreds2.get(cas);
				

				try {
					MAE+=Math.abs(p.exp-p.pred);
					count++;
				} catch (Exception ex) {
				}
				
				try {
					MAE2+=Math.abs(p2.exp-p2.pred);
					count2++;
				} catch (Exception ex) {
				}
			}
			
			MAE/=count;
			MAE2/=count2;
			
			System.out.println(abbrev+"\t"+MAE+"\t"+MAE2);
			
		}
		
	}

	
	class Prediction {
		String casrn;
		Double exp;
		Double pred;
	}
	
	

	private void storePredictionsForSet(String endpoint,boolean overWrite) {


//		Hashtable<String, Hashtable<String, ModelPrediction>>htPredictionsByCAS=new Hashtable<>();
		List<ModelPrediction>mps=new ArrayList<>();
		
		String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
		
		String filepathOut="jar\\add dependencies\\Datasets-1.1.1\\gov\\epa\\webtest\\"+abbrev+"\\"+abbrev+"_predictions.json";

		if(new File(filepathOut).exists() && !overWrite) {
			Hashtable<String,Hashtable<String,String>>htByCAS=getPredictionsLookupByCASOld(filepathOut,false);
			
			if(htByCAS.size()>0) {
				System.out.println(abbrev+"\t"+htByCAS.size());
				return;
			}
		}
		
		runPredictions(endpoint, mps, abbrev, "training");
		runPredictions(endpoint, mps, abbrev, "prediction");
		
		try {
			FileWriter fw= new FileWriter (filepathOut);
			fw.write(WebTEST4.gson.toJson(mps));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}


	private void runPredictions(String endpoint, List<ModelPrediction>mps, String abbrev, String set) {
		
		int split=0;
		if(set.equals("prediction"))split=1;
		
		
		System.out.println("\n"+abbrev+"\t"+set);
		
		String folder = WebTEST4.dataFolder+"/"+abbrev+"/";
		String sdfFilePath = folder+ abbrev + "_"+set+".sdf";//assumes using datasets jar version 1.1

		//Following assumes that the structures in the sdf in the jar file are correct, they need updating
		AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
		
		if(acs==null)return;
		
		Hashtable<String, PredictionResults>htNewPredictions=RunFromSDF.runEndpoint(acs, endpoint, "Consensus", false, false, "CAS");
		
			
		for(String CAS:htNewPredictions.keySet()) {
			PredictionResults pr=htNewPredictions.get(CAS);
			List<Double>preds=new ArrayList<>();
			Double exp=pr.getExpValueInModelUnits();
			addModelPredictions(mps, split, CAS, pr, preds, exp);
		}
	}


	private void addModelPredictions(List<ModelPrediction> mps, int split, String CAS, PredictionResults pr,
			List<Double> preds, Double exp) {
		
		for(PredictionIndividualMethod pim: pr.getIndividualPredictionsForConsensus().getConsensusPredictions()) {
			String methodAbbrev=TESTConstants.getAbbrevMethod(pim.getMethod());
			
		
			Double pred=pim.getPrediction();
			
			ModelPrediction mp=new ModelPrediction(CAS,exp,pred,split,methodAbbrev);
			
			mps.add(mp);
			
			if(pim.getPrediction()!=null) {
				preds.add(pim.getPrediction());
			}
		}

		Double predConsensus=null;
		if(preds.size()>=WebTEST.minPredCount) {
			predConsensus=0.0;
			for (int i = 0; i < preds.size(); i++) {
				predConsensus += preds.get(i);
			}
			predConsensus /= (double) preds.size();
		}
		
		ModelPrediction mpConsensus=new ModelPrediction(CAS,exp,predConsensus,split,TESTConstants.abbrevChoiceConsensus);
		
		mps.add(mpConsensus);
	}
	
	
	private void storePredictionsForSet2(Hashtable<String, Hashtable<String, Prediction>> htAll, String endpoint,
			 String set) {

		String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

		System.out.println(abbrev+"\t"+set);
		String	sdfFilePath = abbrev+"/" + abbrev + "_"+set+".sdf";

		//Following assumes that the structures in the sdf in the jar file are correct, they need updating
		AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
		
		Hashtable<String, PredictionResults>htNewPredictions=RunFromSDF.runEndpoint(acs, endpoint, "Consensus", false, false, "CAS");
		
		Hashtable<String, Prediction>htNewPredictions2=new Hashtable<>();
		
		for(String CAS:htNewPredictions.keySet()) {
			
			PredictionResults pr=htNewPredictions.get(CAS);
			
			String strPred=null;

			Prediction p=new Prediction();
			p.casrn=CAS;
			
			if(TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
				p.exp=pr.getPredictionResultsPrimaryTable().getExpToxValue();
				p.pred=pr.getPredictionResultsPrimaryTable().getPredToxValue();	
			} else {
				p.exp=pr.getPredictionResultsPrimaryTable().getExpToxValMass();
				p.pred=pr.getPredictionResultsPrimaryTable().getPredToxValMass();
			}
			
			htNewPredictions2.put(CAS,p);	
		}
		
		if(htAll.containsKey(abbrev)) {
			Hashtable<String,Prediction>htPreds=htAll.get(abbrev);
			for(String CAS:htNewPredictions2.keySet()) {
				htPreds.put(CAS, htNewPredictions2.get(CAS));
			}
		} else {
			htAll.put(abbrev, htNewPredictions2);
		}
		
		
		System.out.println(set+"\t"+htNewPredictions2.size()+"\t"+htAll.get(abbrev).size());
		
	}
	
	
	
	/**
	 * Chemicals in the csvs that arent in the SDFs:
	 *  
CAS	smiles	CSV
18137-96-7	null	Mutagenicity_training_set-2d.csv
20846-00-8	[O-][N+]([O-])=C(C)C	Mutagenicity_training_set-2d.csv
25590-58-3	O=[N+]([O-])C[CH2-]	Mutagenicity_training_set-2d.csv
34430-24-5	null	Mutagenicity_prediction_set-2d.csv
409-21-2	[C-]#[Si+]	Density_training_set-2d.csv
630-08-0	[C-]#[O+]	BP_training_set-2d.csv
85060-01-1	O=[N+]([O-])C(C)C([O-])C	Mutagenicity_training_set-2d.csv

	 * This method adds the missing chemicals to the SDFs
	 * 
	 */
	void compareSDF_to_csv() {
		
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		WebTEST4.printEachPrediction=false;
		
		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceFHM_LC50);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceMutagenicity);
//		List<String>endpoints=RunFromSmiles.allEndpoints;
		
		
		boolean writeSDF=true;
		boolean overwriteSDF=true;
		
		for(String endpoint:endpoints) {

			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			String folder=WebTEST4.dataFolder+"/"+abbrev+"/";
			
			compare("training", abbrev, folder,writeSDF, overwriteSDF);
			compare("prediction", abbrev, folder, writeSDF, overwriteSDF);
		}
		
		
	}

	
	private IAtomContainer readMolecule(String filePath) {

		// Create an MDLV2000Reader to read the MOL file
		try (FileInputStream fileInputStream = new FileInputStream(filePath);
				MDLV2000Reader reader = new MDLV2000Reader(fileInputStream)) {
			// Read the molecule into an IAtomContainer
			IAtomContainer molecule = reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IAtomContainer.class));
			return molecule;
		} catch (Exception e) {
			return null;
		}
	}
	 
	private void compare(String set, String abbrev, String folder,boolean writeSDF, boolean overwriteSDF) {
		
		
		CSVLoader csvLoader = new CSVLoader();
		
		String sdfFileName=abbrev + "_"+set+".sdf";
		String sdfFilePath = folder + sdfFileName; //assumes using datasets jar version 1.1
		
		String fileNameCSV=abbrev + "_"+set+"_set-2d.csv";
		String csvFilePath= folder + fileNameCSV;

		
		try {
			//Following assumes that the structures in the sdf in the jar file are correct, they need updating
			AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
			

			Instances instances= csvLoader.getDataSetFromJarFile(csvFilePath);
			
			HashSet<String>sdfCASRNs=new HashSet<>();
			HashSet<String>csvCASRNs=new HashSet<>();
			
			Iterator<IAtomContainer>iterator=acs.atomContainers().iterator();
			
			boolean haveDuplicate=false;
			
			
			while(iterator.hasNext()) {
				
				IAtomContainer ac=iterator.next();
				
				String cas=ac.getProperty("CAS");
				
				if(sdfCASRNs.contains(cas)) {
					System.out.println(cas+"\t"+abbrev+"\t"+set+"\tDuplicate in SDF, removing\n");
					haveDuplicate=true;
					iterator.remove();
					continue;
				}
				
				sdfCASRNs.add(cas);

				int instanceNumber=instances.getInstanceNumber(cas);
				
				if(instanceNumber==-1) {
					System.out.println(cas+"\t"+abbrev+"\t"+set+"\tMissing in csv\n");
				}
			}
			
			SmilesGenerator sg=new SmilesGenerator(SmiFlavor.Canonical);
			
			
			HashSet<String>deleteCASRNs=new HashSet<>();
			
			
			for(Instance instance:instances.getInstances()) {
				String cas=instance.getName();
				
				csvCASRNs.add(cas);

				if(!sdfCASRNs.contains(cas)) {
					
					List<DSSToxRecord>recs=ResolverDb2.lookupByCAS(cas);
					
					IAtomContainer molecule =  TaskStructureSearch.getMoleculeFromDSSToxRecords(recs);
					
//					String smiles=null;
//					if(molecule!=null) smiles=sg.create(molecule);
//					System.out.println(cas+"\t"+smiles+"\t"+fileNameCSV);
					
					//Delete from the instances:
				
					
					deleteCASRNs.add(cas);

					if(molecule!=null && molecule.getAtomCount()>0) {
						molecule.setProperties(new Hashtable<>());
						molecule.setProperty("Tox", instance.getToxicity());
						molecule.setProperty("Source", "DSSTox");
						molecule.setProperty("CAS", cas);
						acs.addAtomContainer(molecule);
					
					} else if(cas.equals("18137-96-7") || cas.equals("34430-24-5")) {
						molecule=readMolecule("jar/add dependencies/Datasets-1.1.1/"+cas+".mol");
						molecule.setProperties(new Hashtable<>());
						molecule.setProperty("Tox", instance.getToxicity());
						molecule.setProperty("CAS", cas);
						molecule.setProperty("Source", "Scifinder");
						acs.addAtomContainer(molecule);
					} else {
						System.out.println("Need to fix molecule for "+cas);
						continue;
					}
					
					
					String smiles=sg.create(molecule);
					System.out.println(smiles+"\t"+fileNameCSV+"\t"+molecule.getProperties());

						
				}
			}

//			System.out.println(abbrev+"\t"+set+"\t"+sdfCASRNs.size()+"\t"+csvCASRNs.size());
//			System.out.println(abbrev+"\t"+set+"\t"+acs.getAtomContainerCount()+"\t"+instances.numInstances()+"\n");
			
			if((haveDuplicate || deleteCASRNs.size()>0) && writeSDF) {
				String folderNew="jar/add dependencies/Datasets-1.1.1/gov/epa/webtest/"+abbrev+"/";
				
				String sdfFileName2=abbrev + "_"+set+"_2.sdf";
				if(overwriteSDF) sdfFileName2 = sdfFileName;
				
				String	sdfFilePath2 = folderNew +sdfFileName2;
				 try {
					 FileWriter fileWriter = new FileWriter(sdfFilePath2);
					 SDFWriter sdfWriter = new SDFWriter(fileWriter);
			         sdfWriter.setAlwaysV3000(false);

		            for (IAtomContainer atomContainer : acs.atomContainers()) {
		                // Write each AtomContainer to the SDF file
		                sdfWriter.write(atomContainer);
		            }
		            fileWriter.flush();
		            sdfWriter.close();

           		 } catch (IOException e) {
			            e.printStackTrace();
		        }
			}
			
			
//			if(deleteCASRNs.size()>0) {
//				String folderNew="jar/add dependencies/Datasets-1.1.1/gov/epa/webtest/"+abbrev+"/";
////				
//				String fileNameCSV2=abbrev + "_"+set+"_set-2d_2.csv";
//				if(overwriteCSV) fileNameCSV2=fileNameCSV;
//				
//				String csvFilePath2= folderNew + fileNameCSV2;
//				csvLoader.omitCASRNs(true, csvFilePath, csvFilePath2, deleteCASRNs);
//			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		GetTrainingTestSetPredictions g=new GetTrainingTestSetPredictions();

		PredictToxicityJSONCreator.useJsonLookups=false;
		
//		g.compareSDF_to_csv();
		g.createPredictionLookup();
		
//		g.comparePredictionsWithChangeToAtomicProperties();
		
//		g.comparePredictionsInJarWithLatestPredictions();

//		Hashtable<String, Hashtable<String,Hashtable<String,String>>>ht=getPredictionHashtable();
		
//		Hashtable<String, List<ModelPrediction>>ht=getPredictionHashtable();
		
//		System.out.println(ht);
		
	}
	

}
