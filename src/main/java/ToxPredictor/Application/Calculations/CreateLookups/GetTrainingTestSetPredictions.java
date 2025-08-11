package ToxPredictor.Application.Calculations.CreateLookups;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.openscience.cdk.AtomContainerSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.MyDescriptors.AtomicProperties;
import ToxPredictor.misc.Lookup;

/**
* @author TMARTI02
*/
public class GetTrainingTestSetPredictions {
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	public static Hashtable<String, Hashtable<String, Hashtable<String, String>>> getPredictionHashtable () {
		
		Hashtable<String,Hashtable<String,Hashtable<String,String>>> htDatasetPredictions=new Hashtable<>();
		
		for(String endpoint:RunFromSmiles.allEndpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
//			String jsonFilePath="gov"+File.separator+"epa"+File.separator+"webtest"+File.separator+abbrev+File.separator+abbrev+"_predictions.json";
			String jsonFilePath="gov/epa/webtest/"+abbrev+"/"+abbrev+"_predictions.json";

			Hashtable<String,Hashtable<String,String>>htByCAS=GetTrainingTestSetPredictions.getPredictionsLookupByCAS(jsonFilePath, true);
			htDatasetPredictions.put(abbrev,htByCAS);
		}
		
		return htDatasetPredictions;
	}

	private static Hashtable<String,Hashtable<String,String>> getPredictionsLookupByCAS(String jsonFilePath,boolean insideJar) {
		
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
				String strPred=pr.getPredictionResultsPrimaryTable().getPredToxValue();
				String strPredJar=lookup.LookUpValueConsensusValueOmitFDAInJarFile(predfilename, CAS, "CAS","\t");
				
				if(strPred.equals("N/A")) strPred="-9999";
				
				if(strPred.equals("N/A") || strPredJar.equals("N/A")) {
					System.out.println("\t"+CAS+"\t"+strPred+"\t"+strPredJar);
					continue;
				}
			
				Double dpred=Double.parseDouble(strPred);
				Double dpredjar=Double.parseDouble(strPredJar);
				
				if(Math.abs(dpred-dpredjar)>0.05) 
					System.out.println("\t"+CAS+"\t"+df.format(dpred)+"\t"+df.format(dpredjar));
				
				
//				System.out.println("\t"+CAS+"\t"+strPred+"\t"+strPredJar);

			}
			
//			System.out.println(gson.toJson(htTrain));
//			System.out.println(abbrev+"\ttraining\t"+trainingACS.getAtomContainerCount());
			String	sdfPred = abbrev+File.separator + abbrev + "_prediction.sdf";
		}
		
		
	}
	
	

	void createPredictionLookup() {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
		WebTEST4.printEachPrediction=false;
		
		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceDM_LC50);
//		List<String>endpoints=RunFromSmiles.allEndpoints;
		
//		RunFromSmiles.testReadFromJarStatic("ST/ST_training.sdf");
//		if(true)return;
		
		Hashtable<String,Hashtable<String,Hashtable<String,String>>> htAll=new Hashtable<>();
		
		for(String endpoint:endpoints) {
			
//			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
//			if(!abbrev.equals("BP")) continue;
			storePredictionsForSet(endpoint);
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
					double pred=Double.parseDouble(p.pred);
					double exp=Double.parseDouble(p.exp);
					MAE+=Math.abs(exp-pred);
					count++;
				} catch (Exception ex) {
				}
				
				try {
					double pred=Double.parseDouble(p2.pred);
					double exp=Double.parseDouble(p2.exp);
					MAE2+=Math.abs(exp-pred);
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
		String exp;
		String pred;
	}
	
	

	private void storePredictionsForSet(String endpoint) {


		Hashtable<String, Hashtable<String,String>>htPredictionsByCAS=new Hashtable<>();
		String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
		
		String filepathOut="jar\\add dependencies\\Datasets-1.1.1\\gov\\epa\\webtest\\"+abbrev+"\\"+abbrev+"_predictions.json";

		if(new File(filepathOut).exists()) {
			Hashtable<String,Hashtable<String,String>>htByCAS=getPredictionsLookupByCAS(filepathOut,false);
			System.out.println(abbrev+"\t"+htByCAS.size());
			return;
		}
		
		
		runPredictions(endpoint, htPredictionsByCAS, abbrev, "training");
		runPredictions(endpoint, htPredictionsByCAS, abbrev, "prediction");
		
		try {
			FileWriter fw= new FileWriter (filepathOut);
			fw.write(WebTEST4.gson.toJson(htPredictionsByCAS));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}


	private void runPredictions(String endpoint, Hashtable<String, Hashtable<String, String>> htPredictionsByCAS,
			String abbrev, String set) {
		
		
		System.out.println("\n"+abbrev+"\t"+set);
		
		String	sdfFilePath = abbrev+"/" + abbrev + "_"+set+".sdf";

		//Following assumes that the structures in the sdf in the jar file are correct, they need updating
		AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
		
		Hashtable<String, PredictionResults>htNewPredictions=RunFromSDF.runEndpoint(acs, endpoint, "Consensus", false, false, "CAS");
		
			
		for(String CAS:htNewPredictions.keySet()) {
			PredictionResults pr=htNewPredictions.get(CAS);
			Hashtable<String,String>htPredictionsByMethod=new Hashtable<>();
			
			
			List<Double>preds=new ArrayList<>();
			
			for(PredictionIndividualMethod pim: pr.getIndividualPredictionsForConsensus().getConsensusPredictions()) {
				String methodAbbrev=TESTConstants.getAbbrevMethod(pim.getMethod());
				htPredictionsByMethod.put(methodAbbrev, pim.getPrediction());
				
				if(!pim.getPrediction().equals("N/A")) {
					preds.add(Double.parseDouble(pim.getPrediction()));
				}
			}

			DecimalFormat df=new DecimalFormat("0.00");
			
			if(preds.size()<WebTEST.minPredCount) {
				htPredictionsByMethod.put(TESTConstants.abbrevChoiceConsensus, "N/A");
			} else {
				double predConsensus=0.0;
				for (int i = 0; i < preds.size(); i++) {
					predConsensus += preds.get(i);
				}
				predConsensus /= (double) preds.size();
				htPredictionsByMethod.put(TESTConstants.abbrevChoiceConsensus, df.format(predConsensus));
			}
			
			htPredictionsByCAS.put(CAS,htPredictionsByMethod);
		}
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
	
	public static void main(String[] args) throws Exception {
		GetTrainingTestSetPredictions g=new GetTrainingTestSetPredictions();
		
//		g.createPredictionLookup();
		
//		g.comparePredictionsWithChangeToAtomicProperties();
		
//		g.comparePredictionsInJarWithLatestPredictions();

		Hashtable<String, Hashtable<String,Hashtable<String,String>>>ht=getPredictionHashtable();
		
		System.out.println(ht);
		
	}
	

}
