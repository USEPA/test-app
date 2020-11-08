
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;


public class AADashboardTest {
	static Hashtable<String,Chemical>htWS=null;
	static Hashtable<String,Chemical>htRef=null;
	
	static ArrayList<String>vecCAS_WS=new ArrayList<>();
	static ArrayList<String>vecCAS_Ref=new ArrayList<>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		
		String filePathTodd="data/most records.json";
		Chemicals chemicalsReference=loadFromJSON(filePathTodd);
		
				
		htRef=new Hashtable<>();
//		System.out.println(chemicalsReference.size());
		
		for (Chemical chemical:chemicalsReference) {
//			System.out.println(chemical.CAS);
			
			htRef.put(chemical.CAS, chemical);
			vecCAS_Ref.add(chemical.CAS);
//			System.out.println(chemical.CAS);
		}
		
		String filePathWebService="data/most records from web service.json";
		Chemicals chemicalsWebService=loadFromJSON(filePathWebService);
		
		System.out.println(chemicalsWebService.size());
		
//		runAADashboard();//TODO get webservice call running from Java and not just from Postman so can check easier

		htWS=new Hashtable<>();
		for (Chemical chemical:chemicalsWebService) {
			htWS.put(chemical.CAS, chemical);
			vecCAS_WS.add(chemical.CAS);
		}
		
	}

	@Before
	public void setUp() throws Exception {
				
	
	}
	
	
	

	private static Chemicals loadFromJSON(String jsonFilePath) {
		
		Chemicals chemicals=Chemicals.loadFromJSON(jsonFilePath);
		
		return chemicals;
		
		
	}
	
	/**
	 * Run AADashboard webservice
	 * 
	 * @throws Exception
	 */
	Chemicals runAADashboard() throws Exception {
		String url = "http://webtest2.sciencedataexperts.com/AADashboard/batchChemicals/";

		//Smiles for top 20 chemicals:
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

		JsonObject jo=new JsonObject();
		jo.add("query", ja);
		jo.addProperty("format", "SMILES");
		System.out.println(jo.toString());		
		Chemicals chemicals=null;

		Future<HttpResponse<JsonNode>> future = Unirest.post("http://webtest2.sciencedataexperts.com/AADashboard/batchChemicals/")
				.header("Content-Type", "application/json").header("Accept", "application/json")
				.body(jo.toString())
				.asJsonAsync(new Callback<JsonNode>() {

					
					@Override
					public void failed(UnirestException e) {
						System.out.println("The request has failed");
					}

					@Override
					public void completed(HttpResponse<JsonNode> response) {
						
						System.out.println("Completed");
						
						int code = response.getStatus();
						Map headers = response.getHeaders();
						JsonNode body = response.getBody();

						
//						InputStream inputStream = response.getRawBody();
//						BufferedInputStream bis = new BufferedInputStream(inputStream);
//						ByteArrayOutputStream buf = new ByteArrayOutputStream();
//						
//						try {
//							int result = bis.read();
//							while(result != -1) {
//								buf.write((byte) result);
//								result = bis.read();
//							}
//
//							String rawJson = buf.toString("UTF-8");
//							System.out.println("rawJson="+rawJson);
//						} catch (Exception ex) {
//							ex.printStackTrace();
//						}


						//TODO Need to return Chemicals object from the JSON- but so far it only prints the Json to the logger outout
//						JSONObject json = response.getBody().getObject();
//						System.out.println("received json: " + json.toString());
						
//						Gson gson = new Gson();
//						chemicals = gson.fromJson(json, Chemicals.class);
						
					}
					
					@Override
					public void cancelled() {
						System.out.println("The request has been cancelled");
					}

				});
		
		System.out.println(future.get().getStatus());
		return chemicals;
		
		
	}
	
	private boolean compareChemicals(Chemical c1,Chemical c2) {

//		ArrayList<String>array1=c1.toStringArray("|");
//		ArrayList<String>array2=c2.toStringArray("|");
//				
//		for (String r1:array1) {
//			if (!array2.contains(r1)) {
//				System.out.println("missing record in chemical1:"+r1);
//			}
//		}
//		
//		for (String r2:array2) {
//			if (!array1.contains(r2)) {
//				System.out.println("missing record in chemical2:"+r2);
//			}
//		}
			
		for (int i=0;i<c1.scores.size();i++) {
			Score score1=c1.scores.get(i);
			Score score2=c2.scores.get(i);
			
//			System.out.println(score1.hazard_name);
			
			Vector<String>strRecords1=getStringRecords(c1, score1);
			Vector<String>strRecords2=getStringRecords(c2, score2);
			
			if (score1.records.size()!=score2.records.size()) {
				System.out.println("Web service records:");
				System.out.println("size mismatch:"+score1.hazard_name+"\t"+score2.hazard_name);
				
				for (int j=0;j<score1.records.size();j++) {
					ScoreRecord ffr=score1.records.get(j);
					System.out.println(ffr);
				}

				System.out.println("\n");
				System.out.println("Todd's Java records:");
				for (int j=0;j<score2.records.size();j++) {
					ScoreRecord ffr2=score2.records.get(j);
					System.out.println(ffr2);
				}
				return false;
			} else {
				//look at records in detail:
				
				for (int j=0;j<score1.records.size();j++) {
					boolean recordMatch=this.compareScoreRecords(c1.CAS, score1.records.get(j), score2.records.get(j));
					if (!recordMatch) return false;
				}
				
				
				
			}
		}
		return true;
	}

	private Vector<String> getStringRecords(Chemical c, Score score) {
		Vector<String>strRecords=new Vector<>();
		for (ScoreRecord sr:score.records) {			
			String rec=sr.toString("|");
			strRecords.add(rec);
		}
		return strRecords;
	}
	
	private boolean compareScoreRecords(String CAS,ScoreRecord sr1,ScoreRecord sr2) {
		
		
		
		for (int i = 0; i < ScoreRecord.actualFieldNames.length; i++) {
			try {
			
				String fieldName=ScoreRecord.actualFieldNames[i];
				
				String val1=getFieldValue(sr1, fieldName);
				String val2=getFieldValue(sr1, fieldName);
				
				if (!val1.contentEquals(val2)) {
					System.out.println("mismatch for "+CAS+" for "+fieldName);
					System.out.println("val1=" +val1);
					System.out.println("val2=" +val1);
					return false;
				} else {
//					if (fieldName.contentEquals("name")) 
//						System.out.println(fieldName+"\t"+val1+"\t"+val2);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return true;
		
	}

	private String getFieldValue(ScoreRecord sr, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field myField = sr.getClass().getDeclaredField(fieldName);
		
		String val=null;
		
		if (fieldName.equals("valueMass")) {
			if (myField.get(this)==null) {
				val="";	
			} else {
				val=(Double)myField.get(sr)+"";
			}
			
		} else {
			if (myField.get(sr)==null) {
				val="";
			} else {
				val=(String)myField.get(sr);
			} 
		}
		return val;
	}
	

	/**
	 * Compares records for acrolein
	 * 
	 * @throws Exception
	 */

	@Test
	public void testCompareAcrolein() throws Exception {
		
		Chemical acroleinRef=htRef.get("79-06-1");
		Chemical acroleinWS=htWS.get("79-06-1");
		
//		printFlatChemical(acroleinRef);
//		printFlatChemical(acroleinWS);

//		printChemicals(chemicalsReference);
//		printChemicals(chemicalsWebService);
		
		assertTrue(compareChemicals(acroleinWS, acroleinRef));
	}

	
	@Test
	public void testCompareAll() throws Exception {
		//TODO
		assertTrue(true);
		
	}
	
	@Test
	/**
	 * Checks to see if have the same list of CAS numbers
	 * @throws Exception
	 */
	public void testCompareListOfChemicals() throws Exception {

//		Collections.sort(vecCAS_Ref);
//		Collections.sort(vecCAS_WS);
		
		boolean haveAll=true;
		
		for (String CAS_Ref:vecCAS_Ref) {
			if (!vecCAS_WS.contains(CAS_Ref)) {
				haveAll=false;
				break;
			}
		}
		
		if (!haveAll) {
			System.out.println("reference list:");
			for (String CAS:vecCAS_Ref) {
				System.out.println(CAS);
			}
			
			System.out.println("\nweb service chemicals retrieved:");
			for (String CAS:vecCAS_WS) {
				System.out.println(CAS);
			}

		}
		
		assertTrue(haveAll);
	}

	
	private void printFlatChemical(Chemical chemical) {
		ArrayList<String>list=chemical.toStringArray("|");
		for (String line:list) {
			System.out.println(line);
		}
	}

//	FlatFileRecord getFFR(Chemical chemical,Score score,ScoreRecord sr) {
//		FlatFileRecord f=new FlatFileRecord();
//		f.CAS=chemical.CAS;
//		f.name=chemical.name;
//
//		f.hazard_name=score.hazard_name;
//		
//		f.source=sr.source;
//		f.score=sr.score;
//		f.category=sr.category;
//		f.hazard_code=sr.hazard_code;
//		f.hazard_statement=sr.hazard_statement;
//		f.rationale=sr.rationale;
//		f.route=sr.route;
//		f.note=sr.note;
//		f.note2=sr.note2;
//		f.valueMassOperator=sr.valueMassOperator;
//		
//		if (sr.valueMass!=null)	f.valueMass=sr.valueMass;
//		
//		f.valueMassUnits=sr.valueMassUnits;
//		
//		return f;
//	}
	
	
	private void printChemicals(Chemicals chemicals) {
		for (Chemical chemical:chemicals) {
			printFlatChemical(chemical);
			
		}
	}

}
