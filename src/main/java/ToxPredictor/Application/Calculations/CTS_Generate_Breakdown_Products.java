package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import ToxPredictor.Application.Calculations.ResultsCTS.Data.Tree.Child;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


public class CTS_Generate_Breakdown_Products {

	public static int numGenerations=4;
	public static String urlCTS="https://qed.epacdx.net/cts/rest/metabolizer/run";
	public static String strLibraryHydrolysis="hydrolysis";
	public static String strLibraryAbioticReduction="abiotic_reduction";
	public static String strLibraryHumanBioTransformation="human_biotransformation";
	
	
	
//	class ChildChemical {
//		String smiles;
//		String routes;
//		String generation;
//		String accumulation;
//		public String likelihood;
//	}
	
	void turnOffLogging() {
		Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
		for(String log:loggers) { 
			Logger logger = (Logger)LoggerFactory.getLogger(log);
			logger.setLevel(Level.INFO);
			logger.setAdditive(false);
		} //end
	}
	
	
//	/***
//	 * Determines unique smiles list of products but doesnt look at rest of metadata
//	 * 
//	 * @param jo
//	 * @param smilesChildren
//	 */
//	void goThroughChildren(JsonObject jo,ArrayList<String>smilesChildren) {
//		
//		JsonArray jaChildren=jo.get("children").getAsJsonArray();
//		
//		for (int i=0;i<jaChildren.size();i++) {
//			
//			JsonObject child=jaChildren.get(i).getAsJsonObject();
//			
//			JsonObject joData=child.get("data").getAsJsonObject();
//			String smiles=joData.get("smiles").getAsString();
//
//			if (!smilesChildren.contains(smiles)) smilesChildren.add(smiles);//TODO- only add if not present already? 
//			//Recursively go through children again:
//			goThroughChildren(child,smilesChildren);
//			
//		}
//		
//	}
	
//	/***
//	 * Gets all transformation products and metadata
//	 * @param jo
//	 * @param smilesChildren
//	 */
//	void goThroughChildren2(JsonObject jo,ArrayList<ChildChemical>smilesChildren) {
//		
//		JsonArray jaChildren=jo.get("children").getAsJsonArray();
//		
//		for (int i=0;i<jaChildren.size();i++) {
//			
//			JsonObject child=jaChildren.get(i).getAsJsonObject();
//			
//			ChildChemical childChemical=new ChildChemical();
//			
//			JsonObject joData=child.get("data").getAsJsonObject();
//			childChemical.smiles=joData.get("smiles").getAsString();
//			childChemical.routes=joData.get("routes").getAsString();
//			childChemical.generation=joData.get("generation").getAsString();
//			childChemical.accumulation=joData.get("accumulation").getAsString();
//			childChemical.likelihood=joData.get("likelihood").getAsString();
//			//TODO- only want to report accumulation >= 0.1
//			
//			smilesChildren.add(childChemical);//TODO- only add if not present already? Should we store info on routes and generation?
//			//Recursively go through children again:
//			goThroughChildren2(child,smilesChildren);
//			
//		}
//		
//	}
	
//	ArrayList<String> getChemicals(JsonObject jo) {
//		
//		JsonObject joData=jo.get("data").getAsJsonObject();
//		JsonObject joTree=joData.get("tree").getAsJsonObject();
//
//		JsonObject joDataParent=joTree.get("data").getAsJsonObject();
//		String smilesParent=joDataParent.get("smiles").getAsString();
//		
//		ArrayList<String>smilesChildren=new ArrayList<>();
//		
//		this.goThroughChildren(joTree,smilesChildren);
//		
//		return smilesChildren;
//		
////		GsonBuilder builder = new GsonBuilder();
////		builder.setPrettyPrinting();
////		Gson gson = builder.create();
//		
////		String strJSON=gson.toJson(joTree);//convert back to JSON string to see if we have implemented all the needed fields
////		System.out.println(strJSON);
////		System.out.println(smilesParent);
//		
//	}
	
//	private ArrayList<ChildChemical> getChildChemicals(JsonObject jo) {
//		
//		JsonObject joData=jo.get("data").getAsJsonObject();
//		JsonObject joTree=joData.get("tree").getAsJsonObject();
//
//		JsonObject joDataParent=joTree.get("data").getAsJsonObject();
//		String smilesParent=joDataParent.get("smiles").getAsString();
//		
//		ArrayList<ChildChemical>smilesChildren=new ArrayList<>();
//		
//		goThroughChildren2(joTree,smilesChildren);
//		
//		return smilesChildren;
//		
////		GsonBuilder builder = new GsonBuilder();
////		builder.setPrettyPrinting();
////		Gson gson = builder.create();
//		
////		String strJSON=gson.toJson(joTree);//convert back to JSON string to see if we have implemented all the needed fields
////		System.out.println(strJSON);
////		System.out.println(smilesParent);
//		
//	}
	
//	void bob() {
//		
//		String url="https://qed.epacdx.net/login";
//		HttpClient client = HttpClientBuilder.create().build();
//
////        String body="{\"structure\":\"C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3\",\"generationLimit\":2,\"transformationLibraries\":[\"hydrolysis\"]}";
//
//        
//        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
//                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)                
//                .addTextBody("Username", "qeduser").addTextBody("Password", "ecoDomain2019")
//        		.addTextBody("next","/hms/api_doc/swagger/")
//        	
////        		.addTextBody("body", body)
//        		;
//        		
//        
//        
//        		
//        HttpEntity entity = entityBuilder.build();
//        
//        HttpPost post = new HttpPost(url);
//        post.setEntity(entity);
//                          
//        try {
//			org.apache.http.HttpResponse response = client.execute(post);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

//	void runCTS() {
//
//		String smiles="C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3";
//
//		boolean useHydrolysisLibrary=true;
//		boolean useAbiotic_reductionLibrary=false;
//		boolean useHuman_biotransformation=false;
//
//		String outputFilePath="AADashboard calcs/outputCTS.json";
//
////		String url="https://qedinternal.epa.gov/cts/rest/metabolizer/run";
////		String url="https://134.67.114.1/cts/gentrans/run";
////		this.runCTS(url,smiles, generationLimit,true,false,false,outputFilePath);
////		this.runCTS(url,smiles, generationLimit,useHydrolysisLibrary,useAbiotic_reductionLibrary,useHuman_biotransformation,outputFilePath);
//		
//		int generationLimit=2;
//		
//		String url="https://qed.epacdx.net/cts/rest/metabolizer/run";
//		String userName="qeduser";
//		String password="ecoDomain2019";
//
//		
//		this.runCTS(url,userName,password,smiles, generationLimit,useHydrolysisLibrary,useAbiotic_reductionLibrary,useHuman_biotransformation,outputFilePath);
//		
//	}
	
	
	
	
//	void runCTS(String url, String smiles, int generationLimit,boolean useHydrolysisLibrary,boolean useAbiotic_reductionLibrary, boolean useHuman_biotransformation,String outputFilePath) {
//		
//		try {
//			
//			this.turnOffLogging();
//									
//			JsonObject jo=new JsonObject();
//			
//			jo.addProperty("structure", smiles);
//			jo.addProperty("generationLimit", generationLimit);
//			
//			JsonArray ja=new JsonArray();
//			if (useHydrolysisLibrary) ja.add(strLibraryHydrolysis);
//			if (useAbiotic_reductionLibrary) ja.add("abiotic_reduction");
//			if (useHuman_biotransformation) ja.add("human_biotransformation");
//			
//			jo.add("transformationLibraries", ja);
//			
//			System.out.println(jo.toString());
//			
////			String bob="{\"structure\": \"C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3\", \"generationLimit\": 2, \"transformationLibraries\": [\"hydrolysis\",\"abiotic_reduction\", \"human_biotransformation\"]}";
//			
//			HttpResponse<JsonNode> response = Unirest.post(url).header("content-type", "application/json")
//					.header("accept", "application/json").body(jo.toString()).asJson();
//
////			System.out.println(response.getBody().getObject());
//
//			// System.out.println(response.getBody().getObject().toString(2));
//			
//			GsonBuilder builder = new GsonBuilder();
//			builder.setPrettyPrinting();
//			Gson gson = builder.create();
//			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
//			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
//			System.out.println(strJSON);
//			
//			//Get children from result:
////			loadFromJsonObject(joResult);
//			
//			
//			if (outputFilePath!=null) {
//				FileWriter fw=new FileWriter(outputFilePath);
//				fw.write(strJSON);
//				fw.close();
//			}
//			
////			HttpResponse<JsonNode> response = Unirest.post(url)
////			  .header("accept", "application/json")
////			  .body(jo.toString())
////			  .asJson();
////			
////			System.out.println(response.getBody().getObject().toString(2));
////			
////			FileWriter fw=new FileWriter("TPP_"+library+".json");
////			
////			fw.write(response.getBody().getObject().toString(2));
////			fw.close();
//			
//
//		} catch (UnirestException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//		
//		
//	}
	
	
	String runCTS(String url, String smiles, int generationLimit,String library) {
		
		try {
			
			this.turnOffLogging();
									
			JsonObject jo=new JsonObject();
			
			jo.addProperty("structure", smiles);
			jo.addProperty("generationLimit", generationLimit);
			
			JsonArray ja=new JsonArray();

			//for now you dont specify any libraries to run human biotransformation. If you specify it, it accidentally runs an old version of the library according to NERL
			if (!library.contentEquals(strLibraryHumanBioTransformation)) {
				ja.add(library);
			}
						
			jo.add("transformationLibraries", ja);
			
//			System.out.println(jo.toString());
			
			Unirest.setTimeouts(1000*100,1000*100);
			
			HttpResponse<JsonNode> response = Unirest.post(url).header("content-type", "application/json")
					.header("accept", "application/json").body(jo.toString()).asJson();

			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
//			System.out.println(strJSON);
			return strJSON;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			String error="Error: "+e.getMessage();
			System.out.println(error);
			return error;
		}		
		
	}

	
//	void runCTS(String url, String username,String password,String smiles, int generationLimit,boolean useHydrolysisLibrary,boolean useAbiotic_reductionLibrary, boolean useHuman_biotransformation,String outputFilePath) {
//		
//		try {
//			
//			this.turnOffLogging();
//			
////			Unirest.setTimeouts(100000, 100000);
//			
//			
//			JsonObject jo=new JsonObject();
//			
//			jo.addProperty("structure", smiles);
//			jo.addProperty("generationLimit", generationLimit);
//			
//			JsonArray ja=new JsonArray();
//			if (useHydrolysisLibrary) ja.add("hydrolysis");
//			if (useAbiotic_reductionLibrary) ja.add("abiotic_reduction");
//			if (useHuman_biotransformation) ja.add("human_biotransformation");
//			
//			jo.add("transformationLibraries", ja);
//			
//			System.out.println(jo.toString());
//			
////			String bob="{\"structure\": \"C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3\", \"generationLimit\": 2, \"transformationLibraries\": [\"hydrolysis\",\"abiotic_reduction\", \"human_biotransformation\"]}";
//			
//			HttpResponse<JsonNode> response = Unirest.post(url)
//					.basicAuth(username, password)
//					.header("content-type","application/json")
//					.header("accept", "application/json").body(jo.toString()).asJson();
//
////			HttpResponse<String> response = Unirest.post(url)
////					.basicAuth(username, password)
////					.header("Content-Type","application/json")
////					.header("Accept", "application/json").body(jo.toString()).asString();
////			System.out.println(response.getBody());
//			
////			HttpResponse<JsonNode> response = Unirest.post("https://qed.epacdx.net/cts")
////					  .header("Content-Type", "application/json")
////					  .header("Accept", "application/json")
////					  .header("", "")
////					  .header("Authorization", "Basic cWVkdXNlcjplY29Eb21haW4yMDE5")
////					  .header("User-Agent", "PostmanRuntime/7.15.0")
////					  .header("Cache-Control", "no-cache")
////					  .header("Postman-Token", "2e1e50ac-a5b0-47da-999b-f16b88475c1c,c0f38504-5e61-47d2-9365-c6b2464fade9")
////					  .header("cookie", "csrftoken=jOqsYHToKn3xGMIXVdfIcvNx02XV11pwa59Xusu8BK7AoZh3qHptjeNDDBBvyYX0")
////					  .header("accept-encoding", "gzip, deflate")
////					  .header("referer", "https://qed.epacdx.net/cts/")
////					  .header("Connection", "keep-alive")
////					  .header("cache-control", "no-cache")
////					  .body("{\"structure\":\"C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3\",\"generationLimit\":2,\"transformationLibraries\":[\"hydrolysis\"]}")
////					  .asJson();
//			
////			System.out.println(response.getBody().getObject());
//
//			// System.out.println(response.getBody().getObject().toString(2));
//			
//			GsonBuilder builder = new GsonBuilder();
//			builder.setPrettyPrinting();
//			Gson gson = builder.create();
//			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
//			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
//			System.out.println(strJSON);
//			
//			//Get children from result:
////			loadFromJsonObject(joResult);
//			
//			
////			if (outputFilePath!=null) {
////				FileWriter fw=new FileWriter(outputFilePath);
////				fw.write(strJSON);
////				fw.close();
////			}
//			
////			HttpResponse<JsonNode> response = Unirest.post(url)
////			  .header("accept", "application/json")
////			  .body(jo.toString())
////			  .asJson();
////			
////			System.out.println(response.getBody().getObject().toString(2));
////			
////			FileWriter fw=new FileWriter("TPP_"+library+".json");
////			
////			fw.write(response.getBody().getObject().toString(2));
////			fw.close();
//			
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		
//	}

	
//	void loadJsonFromFile() {
//
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//		Gson gson = builder.create();
//
//		File jsonFile=new File("AA Dashboard/CTS/output.json");
//		try {
//			FileReader fr=new FileReader(jsonFile);
//			JsonObject jo = gson.fromJson(fr, JsonObject.class);
//			fr.close();
//			
////			System.out.println(gson.toJson(jo));
//			ArrayList<String>smilesChildren=this.getChemicals(jo);
//			
//			for(String smiles:smilesChildren) {
//				System.out.println(smiles);
//			}
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
//	void loadFromJsonObject(JsonObject jo) {
////		System.out.println(gson.toJson(jo));
//		ArrayList<String>smilesChildren=this.getChemicals(jo);
//		for(String smiles:smilesChildren) {
//			System.out.println(smiles);
//		}
//			
//	}
	
//	void loadFromJsonObject2(JsonObject jo) {
////		System.out.println(gson.toJson(jo));
//		ArrayList<ChildChemical>childChemicals=this.getChildChemicals(jo);
//		
//		for(ChildChemical cc:childChemicals) {
//			System.out.println(cc.generation+"\t"+cc.routes+"\t"+cc.smiles);
//		}
//	}


//	public ArrayList<ChildChemical> combineChemicals(ArrayList<ChildChemical>childChemicals) {
//		
//		Hashtable<String,ChildChemical>ht=new Hashtable<>();
//		
//		for (ChildChemical childChemical:childChemicals) {
//			
//			if (ht.get(childChemical.smiles)==null) {
//				ht.put(childChemical.smiles, childChemical);
//			} else {
//				ChildChemical childChemicalOld=ht.get(childChemical.smiles);
//				double accumOld=Double.parseDouble(childChemicalOld.accumulation);
//				double accumNew=Double.parseDouble(childChemical.accumulation);
//				double accumTotal=accumOld+accumNew;
//				childChemicalOld.accumulation=accumTotal+"";
//			}
//		}
//		
//		Set<String> keys = ht.keySet();
//		
//		ArrayList<ChildChemical>childChemicals2=new ArrayList<>();
//		
//		for(String key: keys){
//			childChemicals2.add(ht.get(key));	
////			System.out.println(ht.get(key).smiles+"\t"+ht.get(key).accumulation);
//        }
//		
//		return childChemicals2;
//		
//	}
	
//	public ArrayList<ChildChemical> getTransformationProducts(String strJSON) {
//
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//		Gson gson = builder.create();
//
//		try {
//			JsonObject jo = gson.fromJson(strJSON, JsonObject.class);
////			System.out.println(gson.toJson(jo));
//			ArrayList<ChildChemical>childChemicals=getChildChemicals(jo);
//			
//			
////			for(ChildChemical cc:childChemicals) {
//////				if (cc.likelihood.contentEquals("LIKELY"))
////					System.out.println(cc.generation+"\t"+cc.routes+"\t"+cc.smiles+"\t"+cc.generation+"\t"+cc.likelihood+"\t"+cc.accumulation);
////			}
//			
//			
//			return childChemicals;
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return null;
//		}
//	}
	
	
//	public ArrayList<ChildChemical> loadJsonFromFile2(String filePath) {
//
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//		Gson gson = builder.create();
//
//		File jsonFile=new File(filePath);
//		try {
//			FileReader fr=new FileReader(jsonFile);
//			JsonObject jo = gson.fromJson(fr, JsonObject.class);
//			fr.close();
//			
////			System.out.println(gson.toJson(jo));
//			ArrayList<ChildChemical>childChemicals=getChildChemicals(jo);
//			
//			
//			for(ChildChemical cc:childChemicals) {
////				if (cc.likelihood.contentEquals("LIKELY"))
//					System.out.println(cc.generation+"\t"+cc.routes+"\t"+cc.smiles+"\t"+cc.generation+"\t"+cc.likelihood+"\t"+cc.accumulation);
//			}
//			return childChemicals;
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			return null;
//		}
//	}
	
	void testRunCTS() {
		int numGens=4;
		String smilesTPP="O=P(OC1=CC=CC=C1)(OC2=CC=CC=C2)OC3=CC=CC=C3";
		String smilesHCE="C(Cl)(Cl)(Cl)C(Cl)(Cl)(Cl)";
		String smilesC8Cl="C(C(C(C(C(C(C(C(Cl)(Cl)Cl)(Cl)Cl)(Cl)Cl)(Cl)Cl)(Cl)Cl)(Cl)Cl)(Cl)Cl)(Cl)(Cl)Cl";
		
		String strJSON=runCTS(urlCTS,smilesTPP,numGens,strLibraryHydrolysis);

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		ResultsCTS rc = gson.fromJson(strJSON, ResultsCTS.class);
		
		ArrayList<Child>allChildren=ResultsCTS.getAllChildren(rc);		
		ArrayList<Child>uniqueChildren=ResultsCTS.combineChemicals(allChildren);
		
		double totalAccum=0;
		for(Child cc:uniqueChildren) {
				//	if (cc.likelihood.contentEquals("LIKELY"))
			System.out.println(cc.data.generation+"\t"+cc.data.routes+"\t"+cc.data.smiles+"\t"+cc.data.generation+"\t"+cc.data.likelihood+"\t"+cc.data.accumulation);
			
			totalAccum+=cc.data.accumulation;
		}
		System.out.println("totalAccum="+totalAccum);
		System.out.println("# generations="+numGens);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CTS_Generate_Breakdown_Products c=new CTS_Generate_Breakdown_Products();
		c.testRunCTS();
		
//		c.bob();
//		c.loadJsonFromFile();
//		c.loadJsonFromFile2("AADashboard calcs/outputCTS.json");
		
//		for (int i=1;i<=8;i++) {
//			String filepath="AADashboard calcs/hexachlorobenzene numGenerations="+i+".json";
//			System.out.println(filepath);
//			c.loadJsonFromFile2(filepath);
//			System.out.println("");
//		}
		
	}

}
