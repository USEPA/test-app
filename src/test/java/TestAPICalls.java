
import java.io.File;
import com.mashape.unirest.http.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;

public class TestAPICalls {

	private final String USER_AGENT = "Mozilla/5.0";

//	private boolean suppressLogging=false;
//
//	public TestAPICalls() {
//		if (suppressLogging) {
//			turnoffLogging();
//		}
//	}

//	public static void turnoffLogging () {
//		Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
//		for(String log:loggers) { 
//			Logger logger = (Logger)LoggerFactory.getLogger(log);
//			logger.setLevel(Level.INFO);
//			logger.setAdditive(false);
//		} //end
//	}
	
	
	public static void main(String[] args) throws Exception {

		TestAPICalls testAPICalls = new TestAPICalls();

//		testAPICalls.sendPost();
//		testAPICalls.sendPostJayPatel();
		
//		if (true) return;
		
		/* Suppress log messages*/
//		testAPICalls.suppressLogging=true;

		// System.out.println("Testing 1 - Send Http GET request");
		testAPICalls.sendGet();
		//
		// System.out.println("\nTesting 2 - Send Http POST request");
		
		testAPICalls.sendPost();
//		testAPICalls.sendPost2();
//		testAPICalls.testMultipleEndpointAndMethods();
//		testAPICalls.testMultipleEndpointAndMethodsBatch();
//		testAPICalls.testAADashboard_chemicals();
		 
//		testAPICalls.runAADashboardBatchTop20();

	}
	
	private void testAADashboard_chemicals() throws Exception {

		String url = "http://webtest.sciencedataexperts.com/AADashboard/chemicals/";
		JsonObject jo = new JsonObject();
		jo.addProperty("query", "CCCCCO");
		jo.addProperty("format", "SMILES");
		System.out.println(jo.toString());

		com.mashape.unirest.http.HttpResponse<com.mashape.unirest.http.JsonNode> response = com.mashape.unirest.http.Unirest
				.post(url).header("content-type", "application/json").header("accept", "application/json")
				.body(jo.toString()).asJson();


//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//
//		Gson gson = builder.create();
//
//		JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
//		String strJSON = gson.toJson(joResult);// convert back to JSON string to see if we have implemented all the
//												// needed fields
//		System.out.println(strJSON);

		
		//********************************************************************************************
		
//			HttpClient httpclient = HttpClients.createDefault();
//			HttpPost httppost = new HttpPost("http://webtest.sciencedataexperts.com/AADashboard/chemicals/");
//
//			// Request parameters and other properties.
//			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
//			params.add(new BasicNameValuePair("query", "CCCCCO"));
//			params.add(new BasicNameValuePair("format", "SMILES"));
//			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
//
//			//Execute and get the response.
//			HttpResponse response = (HttpResponse) httpclient.execute(httppost);
//			HttpEntity entity = response.getEntity();
//
//			if (entity != null) {
//			    try (InputStream is = entity.getContent()) {
//			    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			        while (true) {
//			        	String Line=br.readLine();
//			        	if (Line==null) break;
//			        	System.out.println(Line);
//			        }
//			    }
//			}

	}

	
	private void runAADashboardBatchTop20() {
		
		String strURL="http://webtest2.sciencedataexperts.com/AADashboard/batchChemicals/";
		
		JsonObject jo = new JsonObject();

		JsonArray ja=new JsonArray();
		
		ja.add("NC(=O)C=C");
		ja.add("ClC=C(Cl)Cl");
		ja.add("OC1=CC=CC=C1");
		ja.add("C=O");
		ja.add("O=CCCCC=O");
		ja.add("NN");
		ja.add("C1CO1");
		ja.add("O.NN");
		ja.add("NC1=CC=C(CC2=CC=C(N)C=C2)C=C1");
		ja.add("[Na+].[Na+].[O-][Cr](=O)(=O)O[Cr]([O-])(=O)=O");
		ja.add("C=CC#N");
		ja.add("C1COCCN1");
		ja.add("BrCCBr");
		ja.add("CO");
		ja.add("F");
		ja.add("OCC1CO1");
		ja.add("OC1=C(Cl)C(Cl)=C(Cl)C(Cl)=C1Cl");
		ja.add("NC1=CC=CC=C1");
		ja.add("ClCC1CO1");
		ja.add("[K+].[K+].[O-][Cr](=O)(=O)O[Cr]([O-])(=O)=O");

		jo.addProperty("format", "SMILES");
		jo.add("query", ja);
		
		try {
			com.mashape.unirest.http.HttpResponse<com.mashape.unirest.http.JsonNode> response = com.mashape.unirest.http.Unirest.post(strURL).
					header("content-type", "application/json")
					.header("accept", "application/json").
					body(jo.toString()).asJson();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
			System.out.println(strJSON);

			
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	// HTTP GET request
	private void sendGet() throws Exception {

		String endpoint="WS";
		
		
		String url = "https://comptox.epa.gov/dashboard/web-test/";
//		 String url = "http://webtest.sciencedataexperts.com/";
//		 String url = "http://webtest2.sciencedataexperts.com/";
		// String url="http://localhost:8100/";

		String smiles="CCO";

		
		url+=endpoint;
		
		HttpResponse<String> response = Unirest.get(url)
				.queryString("smiles", smiles)
                .header("Accept", "application/json")                
                .asString();
		
		
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//		ObjectMapper mapper = new ObjectMapper();
//		com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(rd);
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
		
//		System.out.println(response.getBody().toString());
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		JsonObject joResult = gson.fromJson(response.getBody().toString(), JsonObject.class);
		String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
		System.out.println("Get:\n"+strJSON);


	}

	

	// HTTP POST request
	private void sendPost() throws Exception {

		 String url="https://comptox.epa.gov/dashboard/web-test/";		             
//		String url = "http://webtest.sciencedataexperts.com/";
		// String url="http://localhost:8100/";

		String endpoint = "LC50";

		JsonObject jo = new JsonObject();
		jo.addProperty("query", "CCCCO\nCCCCCCCOCCCC\n");
//		jo.addProperty("query", "CCCCO");
//		jo.addProperty("query", "CCO\nHOH\n");
		// jo.addProperty("query", "CCO,HOH\n");
		jo.addProperty("format", "SMILES");
		jo.addProperty("method", "hc");

		System.out.println(jo.toString());

		HttpResponse<JsonNode> response = Unirest.post(url + endpoint).header("content-type", "application/json")
				.header("accept", "application/json").body(jo.toString()).asJson();

//		System.out.println(response.getBody().getObject());

		// System.out.println(response.getBody().getObject().toString(2));
		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
		String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
		System.out.println("\nPost:\n"+strJSON);


		// ObjectMapper mapper = new ObjectMapper();
		// System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody().getObject()));

		// FileWriter fw=new FileWriter("post.json");
		// fw.write(response.getBody().getObject().toString(2));
		// fw.close();

	}
	
	
	
	
	// HTTP POST request
		private void sendPost2() throws Exception {

//			 String url="https://comptox.epa.gov/dashboard/web-test/";
			String url = "http://webtest.sciencedataexperts.com/";
			// String url="http://localhost:8100/";

			String endpoint = "LC50";

			
			String filePath="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\cdk\\Sample_MDL_SDfile.sdf";
			
			
			String query=org.apache.commons.io.FileUtils.readFileToString(new File(filePath));
			
			JsonObject jo = new JsonObject();
			jo.addProperty("query", query);
			// jo.addProperty("query", "CCO,HOH\n");
			jo.addProperty("format", "SDF");
			jo.addProperty("method", "hc");

			System.out.println(jo.toString());


			com.mashape.unirest.http.HttpResponse<com.mashape.unirest.http.JsonNode> response = com.mashape.unirest.http.Unirest.post(url + endpoint).header("content-type", "application/json")
					.header("accept", "application/json").body(jo.toString()).asJson();

			
//			HttpResponse<JsonNode> response = Unirest.post(url + endpoint).header("content-type", "application/json")
//					.header("accept", "application/json").body(jo.toString()).asJson();

			// System.out.println(response.getBody().getObject().toString(2));
//			System.out.println(response.getBody().getObject());

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
			System.out.println(strJSON);

		}
	
	private void testMultipleEndpointAndMethods () throws Exception  {
		
//		 String url="https://comptox.epa.gov/dashboard/web-test/";
		String url = "http://webtest.sciencedataexperts.com/bulk/predict/";	
//		String url = "http://webtest2.sciencedataexperts.com/bulk/predict/";
		// 	String url="http://localhost:8100/";

			
			JsonObject jo = new JsonObject();

//*****************************************************************************************************
			jo.addProperty("query", "CCCCCO\nCCCOCC");
			
			//Not implemented yet, query as array:
//			JsonArray jaQuery=new JsonArray();
//			jaQuery.add("CCCCO");
//			jaQuery.add("CCCCCCCCCCO");
//			jo.add("query", jaQuery);//TODO query might change to an array in next API version
			//*****************************************************************************************************
			
			
			jo.addProperty("format", "SMI");
			
			JsonArray jaEndpoints=new JsonArray();
			jaEndpoints.add("WS");
			jaEndpoints.add("VP");
			jo.add("endpoints", jaEndpoints);
			
			JsonArray jaMethods=new JsonArray();
			jaMethods.add("hc");
			jaMethods.add("gc");
			jo.add("methods", jaMethods);

			System.out.println(jo.toString());

//			HttpResponse<JsonNode> response = Unirest.post(url).header("content-type", "application/json")
//					.header("accept", "application/json").body(jo.toString()).asJson();

			com.mashape.unirest.http.HttpResponse<com.mashape.unirest.http.JsonNode> response = com.mashape.unirest.http.Unirest.post(url).header("content-type", "application/json")
					.header("accept", "application/json").body(jo.toString()).asJson();
		
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
			System.out.println(strJSON);
			
			
	}
	
	private void testMultipleEndpointAndMethodsBatch () throws Exception  {
		
//		 String url="https://comptox.epa.gov/dashboard/web-test/";
//			String url = "http://webtest.sciencedataexperts.com/bulk/batchPredict/";
			String url = "http://webtest2.sciencedataexperts.com/bulk/batchPredict/";
//String url="http://localhost:8100/";

			
			JsonObject jo = new JsonObject();

//*****************************************************************************************************
			JsonArray jaQuery=new JsonArray();
			jaQuery.add("CCCCO");
			jaQuery.add("CCCCCCCCCCO");
			jo.add("query", jaQuery);//TODO query might change to an array in next API version
			//*****************************************************************************************************
			
			
			jo.addProperty("format", "SMILES");
			
			JsonArray jaEndpoints=new JsonArray();
			jaEndpoints.add("WS");
			jaEndpoints.add("VP");
			jo.add("endpoints", jaEndpoints);
			
			JsonArray jaMethods=new JsonArray();
			jaMethods.add("hc");
			jaMethods.add("gc");
			jo.add("methods", jaMethods);

			System.out.println(jo.toString());

//			HttpResponse<JsonNode> response = Unirest.post(url).header("content-type", "application/json")
//					.header("accept", "application/json").body(jo.toString()).asJson();
		
			com.mashape.unirest.http.HttpResponse<com.mashape.unirest.http.JsonNode> response = com.mashape.unirest.http.Unirest.post(url).header("content-type", "application/json")
					.header("accept", "application/json").body(jo.toString()).asJson();
			
			
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			JsonObject joResult = gson.fromJson(response.getBody().getObject().toString(2), JsonObject.class);
			String strJSON=gson.toJson(joResult);//convert back to JSON string to see if we have implemented all the needed fields
			System.out.println(strJSON);
			
			
	}

	// HTTP POST request
	private void sendPostJayPatel() throws Exception {

		 String url="https://comptox.epa.gov/dashboard/web-test/";
//		String url = "http://webtest.sciencedataexperts.com/";
		// String url="http://localhost:8100/";


		JsonObject jo = new JsonObject();
		String endpoint = "WS";
		jo.addProperty("query", "CCCCCO\n");
		jo.addProperty("format", "SMI");
		jo.addProperty("method", "hc");
		
		com.mashape.unirest.http.HttpResponse<com.mashape.unirest.http.JsonNode> response = com.mashape.unirest.http.Unirest.post(url + endpoint).header("content-type", "application/json")
				.header("accept", "application/json").body(jo.toString()).asJson();
		
//		HttpResponse<JsonNode> response = Unirest.post(url + endpoint).header("content-type", "application/json")
//				.header("accept", "application/json").body(jo.toString()).asJson();

//		JsonObject jo = new JsonObject();
//		jo.addProperty("query", "CCO\n");
//		jo.addProperty("format", "SMILES");
//		jo.addProperty("method", "hc");
//		HttpResponse<JsonNode> response = Unirest.post(url).header("content-type", "application/json")
//		.header("accept", "application/json").body(jo.toString()).asJson();
//https://qedinternal.epa.gov/cts/rest/#/ 

		System.out.println(jo.toString());


		// System.out.println(response.getBody().getObject().toString(2));
		System.out.println(response.getBody());

		// ObjectMapper mapper = new ObjectMapper();
		// System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response.getBody().getObject()));

		// FileWriter fw=new FileWriter("post.json");
		// fw.write(response.getBody().getObject().toString(2));
		// fw.close();

	}

}

