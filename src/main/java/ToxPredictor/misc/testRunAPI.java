package ToxPredictor.misc;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class testRunAPI {
	//	http://unirest.io/java.html
	
	
	void runCTS() {
		
		try {
			
//			Unirest.setTimeouts(100000, 100000);
			
			String url="https://qedinternal.epa.gov/cts/rest/metabolizer/run";
//			String url="https://134.67.114.1/cts/gentrans/run";
			String structure="C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3";
			int generationLimit=2;
			
			String library="hydrolysis";
//			String library="abiotic_reduction";
//			String library="human_biotransformation";
			
			
			JSONObject jo=new JSONObject();
			jo.put("structure", structure);
			jo.put("generationLimit", generationLimit);
			JSONArray ja=new JSONArray();
			ja.put(library);
			jo.put("transformationLibraries", ja);
			
			System.out.println(jo.toString());
			
//			String bob="{\"structure\": \"C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3\", \"generationLimit\": 2, \"transformationLibraries\": [\"hydrolysis\",\"abiotic_reduction\", \"human_biotransformation\"]}";
			
			HttpResponse<JsonNode> response = Unirest.post(url)
			  .header("accept", "application/json")
			  .body(jo.toString())
			  .asJson();
			
			System.out.println(response.getBody().getObject().toString(2));
			
			FileWriter fw=new FileWriter("TPP_"+library+".json");
			
			fw.write(response.getBody().getObject().toString(2));
			fw.close();
			
//			System.out.println(bob);
			
			
//			HttpResponse<JsonNode> jsonResponse = Unirest.post("https://qedinternal.epa.gov/cts/rest/metabolizer/run")
//					.header("accept", "application/json")				
//					.queryString("structure", "C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3")
//					.field("generationLimit", 2)
//					.field("transformationLibraries", "[\"hydrolysis\"]")					
//					.asJson();			
			
			
//			System.out.println(jsonResponse.getBody().toString());
			
			
			
//			System.out.println(jsonResponse.getBody().getObject().toString(2));
			
//			curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{ \ 
//			   "structure": "C1=CC=C(C=C1)OP(=O)(OC2=CC=CC=C2)OC3=CC=CC=C3", \ 
//			   "generationLimit": 2, \ 
//			   "transformationLibraries": [ \ 
//			     "human biotransformation"  ] \ 
//			 }' 'https://qedinternal.epa.gov/cts/rest/metabolizer/run'
				 
				 
			
//			
			
			
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	void runExample() {
		
		try {
			HttpResponse<JsonNode> jsonResponse = Unirest.post("http://httpbin.org/post")
					  .header("accept", "application/json")
					  .queryString("apiKey", "123")
					  .field("parameter", "value")
					  .field("foo", "bar")
					  .asJson();
			
			System.out.println(jsonResponse.getBody());
			
			
//			 HttpResponse<JsonNode> response = Unirest.get("https://api.stackexchange.com/2.2/questions").
//	        header("accept",  "application/json").
//	        queryString("order","desc").
//	        queryString("sort", "creation").
//	        queryString("filter", "default").
//	        queryString("site", "stackoverflow").
//	        asJson();
//	    System.out.println(response.getBody().getObject().toString(2));


			//example using json body instead of separate fields:
//			HttpResponse<JsonNode> response = Unirest.post("https://api.stackexchange.com/2.2/questions")
//			  .header("accept", "application/json")
//			  .body("{\"order\":\"desc\", \"sort\":\"creation\", \"filter\":\"default\",\"site\":\"stackoverflow\"}")
//			  .asJson();
//	System.out.println(response.getBody().getObject().toString(2));

			
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		testRunAPI t=new testRunAPI();
		t.runCTS();
	}

}
