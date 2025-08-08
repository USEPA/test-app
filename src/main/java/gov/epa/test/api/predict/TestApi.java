package gov.epa.test.api.predict;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

//import com.mashape.unirest.http.HttpResponse;

//import com.mashape.unirest.http.Unirest;
//import com.mashape.unirest.http.exceptions.UnirestException;

import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.MoleculeCreator;
import ToxPredictor.Application.model.PredictionResults;
import gov.epa.test.api.predict.PredictController.PostInput;




import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

/**
 * @author TMARTI02
 */
public class TestApi {

	public static Gson gsonPretty = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
			.serializeSpecialFloatingPointValues().create();

	String seek(BufferedReader br) throws Exception {

		while (true) {
			String Line = br.readLine();
			if (Line.contains(">  <") || Line.contains("><"))
				return Line;
			if (Line == null)
				return null;
		}
	}

	/**
	 * Need special method to read charlie's snapshot sdf
	 * 
	 * @param sdfFilePath
	 * @param count
	 * @return
	 */
	
	
	private static String callApi(String apiName, long delayMillis) {
		return "";
	}
	
	
	
	
	void testSpeedup() {
		
		
		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\snapshot-2025-07-30\\";
		String inputFilePath = folder + "50k_chunk_from_1.sdf";
		AtomContainerSet acs = RunFromSDF.readSDF(inputFilePath, 1,false);

//		System.out.println(gson.toJson(acs.getAtomContainer(0).getProperties()));
//		System.out.println(acs.getAtomContainerCount());

		IAtomContainer firstMolecule = acs.getAtomContainer(0);

		StringWriter sw = new StringWriter();

		SDFWriter sdfWriter = new SDFWriter(sw);
		sdfWriter.setAlwaysV3000(true);

		try {
			sdfWriter.write(firstMolecule);
			sdfWriter.close();
			String molFile = sw.toString();
			
			PostInput pi=new PostInput(molFile);
			Gson gson=new Gson();
			System.out.println(gson.toJson(pi));
			
			
			long t1=System.currentTimeMillis();
			for(int i=1;i<=100;i++) {
				int port=8081;
				
				System.out.println(i);
				
				String url="http://localhost:"+port+"/predictPost";
				HttpResponse<String> responsePost = Unirest.post(url)
						.header("Content-Type", "application/json")
						.body(gson.toJson(pi)).asString();
				
			}
			long t2=System.currentTimeMillis();
			
			System.out.println("Time to run 10x with 1 instance:"+(t2-t1)/1000+" seconds");
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static List<PredictionResults> runPrediction(IAtomContainer ac,String server, int port) {
		
		try {
			
			String molFile=null;
			
			try {
				molFile = MoleculeCreator.convertAtomContainerToMolFileStringV3000(ac);
			
			} catch (Exception ex) {
				String smiles=ac.getProperty("SMILES");
				String dtxsid=ac.getProperty("DTXSID");
				System.out.println("Failed to create mol file as string for "+smiles+"\t"+dtxsid);
				return null;
			}
			
			PostInput pi=new PostInput(molFile);
			Gson gson=new Gson();
//			System.out.println(gson.toJson(pi));
			
			String url=server+":"+port+"/predictPost";
//			System.out.println(url);
			
			HttpResponse<String> responsePost = Unirest.post(url)
					.header("Content-Type", "application/json")
					.body(gson.toJson(pi)).asString();
			
//			System.out.println(responsePost.getBody().toString());

			String json=responsePost.getBody().toString();
			
			List<PredictionResults>listResults=getResultsFromJson(json);
			
			return listResults;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		

	}
	
	

	public static List<PredictionResults> runPrediction(String molFile,String server, int port) {
		
		try {
			
			
//			System.out.println(molFile);
			
			IAtomContainer ac=MoleculeCreator.readFromMolFileString(molFile);
			
//			if(ac.getAtomCount()==0)
//				System.out.println(smiles+"\t"+sid+"\t"+ac.getAtomCount());
			
//			if(true)return null;
			
			PostInput pi=new PostInput(molFile);
			Gson gson=new Gson();
//			System.out.println(gson.toJson(pi));
			
			String url=server+":"+port+"/predictPost";
//			System.out.println(url);
			
			HttpResponse<String> responsePost = Unirest.post(url)
					.header("Content-Type", "application/json")
					.body(gson.toJson(pi)).asString();
			
//			System.out.println(responsePost.getBody().toString());

			String json=responsePost.getBody().toString();
			
			List<PredictionResults>listResults=getResultsFromJson(json);
			
			return listResults;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		

	}
	
	
	
	void run() {
		
		int port=8082;

		System.out.println("enter run:"+port);

//		List<String>endpoints=RunFromSmiles.allEndpoints;
//		List<String>endpoints= Arrays.asList(TESTConstants.ChoiceFHM_LC50);
//		String method = TESTConstants.ChoiceConsensus;

//		for (String endpoint:endpoints)
//			WebTEST4.loadTrainingData(endpoint,method);//TODO call this when the application launches
//		ResolverDb2.sqlitePath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST_2020_03_18_EPA_Github\\databases\\snapshot.db";

		String folder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dsstox\\snapshot-2025-07-30\\";
		String inputFilePath = folder + "50k_chunk_from_1.sdf";
		AtomContainerSet acs = RunFromSDF.readSDF(inputFilePath, -1,false);

		System.out.println(acs.getAtomContainerCount());
		
//		System.out.println(gson.toJson(acs.getAtomContainer(0).getProperties()));
//		System.out.println(acs.getAtomContainerCount());

		IAtomContainer firstMolecule = acs.getAtomContainer(0);


		try {

			String molFile = MoleculeCreator.convertAtomContainerToMolFileStringV3000(firstMolecule);
			
//			System.out.println("*"+molFile+"*");
			
//			IAtomContainer ac=RunFromSmiles.readFromMolFileString(molFile);
//			System.out.println(ac.getAtomCount());
//			System.out.println("CASRN="+ac.getProperty("CASRN"));
			

//			System.out.println(molFile);

			// Following should work but doesnt:
//			HttpResponse<List<PredictionResults>> response = Unirest.get("http://localhost:8081/predictGet")
//					  .header("Content-Type", "application/json")
//					  .queryString("molecule", molFile)
//					  .asObject(new GenericType<List<PredictionResults>>() {});
//			 List<PredictionResults> listResults = response.getBody();
//			 System.out.println(gson.toJson(listResults));

//			HttpResponse<String> response = Unirest.get("http://localhost:8081/predictGet")
//					.header("Content-Type", "application/json").queryString("molecule", molFile).asString();
//			System.out.println(response.getBody().toString());

			
			PostInput pi=new PostInput(molFile);
			Gson gson=new Gson();
//			System.out.println(gson.toJson(pi));
			
			String url="http://localhost:"+port+"/predictPost";
			System.out.println(url);
			
			HttpResponse<String> responsePost = Unirest.post(url)
					.header("Content-Type", "application/json")
					.body(gson.toJson(pi)).asString();
			
//			System.out.println(responsePost.getBody().toString());

			String json=responsePost.getBody().toString();
			
			List<PredictionResults>listResults=getResultsFromJson(json);
			
			String cas=listResults.get(0).getCAS();
			
			
			File of=new File("reports/"+cas);
			of.mkdirs();
			
			PredictToxicityWebPageCreatorFromJSON htmlCreator= new PredictToxicityWebPageCreatorFromJSON();
			
			for(PredictionResults pr:listResults) {
				htmlCreator.writeConsensusResultsWebPages(pr, of.getAbsolutePath()+File.separator+pr.getEndpoint()+".html");
			}
			
//			List<PredictionResults>listResults=RunFromSmiles.runEndpointsAsList(molFile, endpoints, method, true, false);
//			
//			for (PredictionResults pr:listResults) {
//				System.out.println(pr.getEndpoint()+"\t"+pr.getPredictionResultsPrimaryTable().getPredToxValue());
//			}

//			System.out.println(gson.toJson(listResults));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

	public static List<PredictionResults> getResultsFromJson(String json) {
		Type type = new TypeToken<List<PredictionResults>>(){}.getType();
		Gson gson = new Gson();
		return  gson.fromJson(json, type);

	}

	
	public static void main(String[] args) {
		TestApi r = new TestApi();
		r.run();
//		r.testSpeedup();
		
		//To run api in linux: java -jar WebTEST.jar --server.port=8081 &
	}

}
