package ToxPredictor.Application.Calculations.CreateLookups;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.misc.Lookup;

/**
* @author TMARTI02
*/
public class GetTrainingTestSetPredictions {
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	public static Hashtable<String,Hashtable<String,String>> getPredictionsLookupByCAS() {
		
		String jsonFilePath = "/Datasets/training_and_test_set_predictions.json"; // Adjust the path based on your JAR structure

        // Read the JSON file from the JAR
        try (InputStream inputStream = GetTrainingTestSetPredictions.class.getResourceAsStream(jsonFilePath);
             InputStreamReader reader = new InputStreamReader(inputStream)) {

            // Define the type of the hashtable
            Type hashtableType = new TypeToken<Hashtable<String,Hashtable<String,String>>>(){}.getType();

            // Parse the JSON file into a Hashtable
            Gson gson = new Gson();
            Hashtable<String,Hashtable<String,String>> htAll = gson.fromJson(reader, hashtableType);

            // Print the hashtable contents
//            for(String abbrev:htAll.keySet()) {
//            	System.out.println(abbrev);
//            	Hashtable<String,String> htEndpoint=htAll.get(abbrev);
//            	for(String CAS:htEndpoint.keySet()) {
//            		System.out.println("\t"+CAS+"\t"+htEndpoint.get(CAS));	
//            	}
//            }
            
            return htAll;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
		
//		List<String>endpoints=TESTConstants.getFullEndpoints(null);
//		List<String>endpoints=Arrays.asList(TESTConstants.ChoiceDM_LC50);
		List<String>endpoints=RunFromSmiles.allEndpoints;
		
//		RunFromSmiles.testReadFromJarStatic("ST/ST_training.sdf");
//		if(true)return;
		
		Hashtable<String,Hashtable<String,String>> htAll=new Hashtable<>();
		
		for(String endpoint:endpoints) {
			
//			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
//			if(!abbrev.equals("BP")) continue;
			
			String set="training";
			storePredictionsForSet(htAll, endpoint, set);
			set="prediction";
			storePredictionsForSet(htAll, endpoint, set);
//			if(true)break;
		}
		
		
		try {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\src\\main\\resources\\Datasets\\";
			FileWriter fw= new FileWriter (folder+"training_and_test_set_predictions.json");
			fw.write(WebTEST4.gson.toJson(htAll));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
//		for(String abbrev:htAll.keySet()) {
//			Hashtable<String,String>ht=htAll.get(abbrev);
//			System.out.println(ht.size());
//		}
		
	}

	
	

	private void storePredictionsForSet(Hashtable<String, Hashtable<String, String>> htAll, String endpoint,
			 String set) {

		String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

		System.out.println(abbrev+"\t"+set);
		String	sdfFilePath = abbrev+"/" + abbrev + "_"+set+".sdf";

		//Following assumes that the structures in the sdf in the jar file are correct, they need updating
		AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
		
		Hashtable<String, PredictionResults>htNewPredictions=RunFromSDF.runEndpoint(acs, endpoint, "Consensus", false, false, "CAS");
		
		Hashtable<String, String>htNewPredictions2=new Hashtable<>();
		for(String CAS:htNewPredictions.keySet()) {
			
			PredictionResults pr=htNewPredictions.get(CAS);
			
			String strPred=null;
			
			if(TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
				strPred=pr.getPredictionResultsPrimaryTable().getPredToxValue();	
			} else {
				strPred=pr.getPredictionResultsPrimaryTable().getPredToxValMass();
			}
			
//			if(CAS==null || strPred==null) {
//				System.out.println(gson.toJson(pr));
//				continue;
//			}
			
			htNewPredictions2.put(CAS,strPred);	
		}
		
		if(htAll.containsKey(abbrev)) {
			Hashtable<String,String>htPreds=htAll.get(abbrev);
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
		
		g.createPredictionLookup();
		
//		g.comparePredictionsInJarWithLatestPredictions();

//		Hashtable<String,Hashtable<String,String>>ht=getPredictionsLookupByCAS();
		
//		System.out.println(ht);
		
	}
	

}
