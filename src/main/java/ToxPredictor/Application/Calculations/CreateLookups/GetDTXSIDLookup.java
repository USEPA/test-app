package ToxPredictor.Application.Calculations.CreateLookups;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityJSONCreator;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.misc.Lookup;

/**
* @author TMARTI02
*/
public class GetDTXSIDLookup {
	

	/**
	 * Following method is to get CAS to dtxsid lookup. For GUI, use getDsstoxRecordLookupByCAS() instead
	 * 
	 * @return
	 */
	public static Hashtable<String, String> getDtxsidLookupByCAS() {
		
		String jsonFilePath = "gov/epa/webtest/dtxsid_lookup_from_cas.json"; // Adjust the path based on your JAR structure

        // Read the JSON file from the JAR
        try (InputStream inputStream = GetDTXSIDLookup.class.getClassLoader().getResourceAsStream(jsonFilePath);
             InputStreamReader reader = new InputStreamReader(inputStream)) {

            // Define the type of the hashtable
            Type hashtableType = new TypeToken<Hashtable<String, String>>(){}.getType();

            // Parse the JSON file into a Hashtable
            Gson gson = new Gson();
            Hashtable<String, String> hashtable = gson.fromJson(reader, hashtableType);

            // Print the hashtable contents
//            hashtable.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));

            return hashtable;
            
            
        } catch (Exception e) {
        	System.out.println("Failed to load "+jsonFilePath);
            e.printStackTrace();
            return null;
        }
    }
	
	


	public static Hashtable<String, DSSToxRecord> getDsstoxRecordLookupByCAS() {
		
		String jsonFilePath = "gov/epa/webtest/DsstoxRecord_lookup_from_cas.json"; // Adjust the path based on your JAR structure

        // Read the JSON file from the JAR
        try (InputStream inputStream = GetDTXSIDLookup.class.getClassLoader().getResourceAsStream(jsonFilePath);
             InputStreamReader reader = new InputStreamReader(inputStream)) {

            // Define the type of the hashtable
            Type hashtableType = new TypeToken<Hashtable<String, DSSToxRecord>>(){}.getType();

            // Parse the JSON file into a Hashtable
            Gson gson = new Gson();
            Hashtable<String, DSSToxRecord> hashtable = gson.fromJson(reader, hashtableType);

            // Print the hashtable contents
//            hashtable.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));

            return hashtable;
            
            
        } catch (Exception e) {
        	System.out.println("Failed to load "+jsonFilePath);
            e.printStackTrace();
            return null;
        }
    }
	
	
	
	private static Hashtable<String, String> queryDatabaseGenericSubstances(Connection connection, List<String> casNumbers)  {
        Hashtable<String, String> casToDtxsid = new Hashtable<>();

        Iterator<String> it=casNumbers.iterator();
		String strCASRNs="";
		while (it.hasNext()) {
			strCASRNs+="'"+it.next()+"'";
			if(it.hasNext()) strCASRNs+=",";
		}

		String sql="select casrn,dsstox_substance_id from generic_substances\n"
				+"where casrn in ("+strCASRNs+");";
        
        try {
        	
            ResultSet resultSet = MySQL_DB.getStatement(connection).executeQuery(sql);
            		
            while (resultSet.next()) {
                // Add the CASRN and dsstox_substance_id to the HashMap
                String casNumber = resultSet.getString("casrn");
                String dsstoxId = resultSet.getString("dsstox_substance_id");
                casToDtxsid.put(casNumber, dsstoxId);
            }
        	
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        
        return casToDtxsid;
    }
	
	private static Hashtable<String, String> queryDatabaseOtherCAS(Connection connection, List<String> casNumbers)  {
        Hashtable<String, String> casToDtxsid = new Hashtable<>();

        Iterator<String> it=casNumbers.iterator();
		String strCASRNs="";
		while (it.hasNext()) {
			strCASRNs+="'"+it.next()+"'";
			if(it.hasNext()) strCASRNs+=",";
		}

		String sql="select oc.casrn,gs.dsstox_substance_id from other_casrns oc\n"
				+"join generic_substances gs on oc.fk_generic_substance_id = gs.id\n"
				+"where oc.casrn in ("+strCASRNs+");";
        
        try {
        	
            ResultSet resultSet = MySQL_DB.getStatement(connection).executeQuery(sql);
            		
            while (resultSet.next()) {
                // Add the CASRN and dsstox_substance_id to the HashMap
                String casNumber = resultSet.getString(1);
                String dsstoxId = resultSet.getString(2);
                casToDtxsid.put(casNumber, dsstoxId);
            }
        	
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        
        return casToDtxsid;
    }

	

	
	public void createDTXSIDLookupFromCAS() {
		
		List<String>endpoints=TESTConstants.getFullEndpoints(null);
		
		Lookup lookup=new Lookup();
		
		HashSet<String>casrns=new HashSet<>();
		
		
		PredictToxicityJSONCreator.useJsonLookups=false;
		
		for(String endpoint:endpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			
			String folder=WebTEST4.dataFolder+"/"+abbrev+"/";
			
			
			String	fileTrain = folder + abbrev + "_training.sdf";
			HashSet<String>casrnsT=lookup.lookUpValsInSDF(fileTrain, "CAS");
			casrns.addAll(casrnsT);
			String	filePred = folder + abbrev+ "_prediction.sdf";
			HashSet<String>casrnsP=lookup.lookUpValsInSDF(filePred, "CAS");
			casrns.addAll(casrnsP);
		}
		
		Hashtable<String,String>htCAS_to_SID=new Hashtable<>();
		
//		long t1=System.currentTimeMillis();
		
		Connection conn=MySQL_DB.getConnectionDSSTOX();
		
		List<String>casBatch=new ArrayList<>();
		
		for (String casrn:casrns) {
			casBatch.add(casrn);
			if(casBatch.size()==100) {
				Hashtable<String,String>htCAS_to_sid=queryDatabaseGenericSubstances(conn, casBatch);
				htCAS_to_SID.putAll(htCAS_to_sid);
				casBatch.clear();
			}
		}
		
		//Do what's left:
		Hashtable<String,String>htCAS_to_sid=queryDatabaseGenericSubstances(conn, casBatch);
		htCAS_to_SID.putAll(htCAS_to_sid);
		casBatch.clear();
		
		
		for (String casrn:casrns) {
			if(!htCAS_to_SID.containsKey(casrn)) {
				casBatch.add(casrn);
			}
		}
		
//		System.out.println(casBatch.size());

		htCAS_to_sid=queryDatabaseOtherCAS(conn, casBatch);
		htCAS_to_SID.putAll(htCAS_to_sid);

		for (String casrn:casrns) {
			if(!htCAS_to_SID.containsKey(casrn)) {
				System.out.println(casrn);
			}
		}

//		long t2=System.currentTimeMillis();
		 
		try {
			String folder="jar\\add dependencies\\Datasets-1.1.1\\gov\\epa\\webtest\\";
			FileWriter fw= new FileWriter (folder+"dtxsid_lookup_from_cas.json");
			fw.write(WebTEST4.gson.toJson(htCAS_to_SID));
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println(casrns.size()+"\t"+htCAS_to_SID.size());
	}
	
	
	public static void main(String[] args) throws Exception {
		
		GetDTXSIDLookup g=new GetDTXSIDLookup();
		g.createDTXSIDLookupFromCAS();

//		Hashtable<String, String>ht=GetDTXSIDLookup.getDtxsidLookupByCAS();
//		System.out.println(ht);
		
	}
	

}
