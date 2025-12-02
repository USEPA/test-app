package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;
import gov.epa.test.api.predict.TestApi;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

/**
* @author TMARTI02
*/
public class RunFromCompoundsJson {
	
	private static final Logger logger = LogManager.getLogger(RunFromCompoundsJson.class);
	
	
	boolean debug=true;
	
	public class DsstoxCompound {
	    private String dsstoxCompoundId;
	    private String jchemInchikey;
	    private String indigoInchikey;
	    private String molFile;
	    private Boolean molImagePNGAvailable;
	    private Double molWeight;
	    private String smiles;
	    private GenericSubstanceCompound genericSubstanceCompound;

	    public class GenericSubstanceCompound {
	        private GenericSubstance genericSubstance;
		    public class GenericSubstance {
		        private String dsstoxSubstanceId;
		        private String casrn;
		        private String preferredName;
		    }
	    }
	    
	    
	    @Override
		public String toString() {
			String strMolecule=molFile;
			strMolecule+="\n";
			
			Hashtable<String,Object> htProperties=new Hashtable<>();
			
			htProperties.put(DSSToxRecord.strCID,dsstoxCompoundId);
			htProperties.put(DSSToxRecord.strSID,this.genericSubstanceCompound.genericSubstance.dsstoxSubstanceId);
			htProperties.put(DSSToxRecord.strCAS,this.genericSubstanceCompound.genericSubstance.casrn);
			htProperties.put(DSSToxRecord.strName,this.genericSubstanceCompound.genericSubstance.preferredName);
			
			if(smiles!=null)
				htProperties.put(DSSToxRecord.strSmiles,smiles);
			
			if(molWeight!=null)
				htProperties.put(DSSToxRecord.strMolWeight,molWeight);
			
			if (indigoInchikey!=null)
				htProperties.put(DSSToxRecord.strInchiKey,indigoInchikey);
			
			for(String property:htProperties.keySet()) {
				strMolecule+="> <"+property+">\n";
				strMolecule+=htProperties.get(property)+"\n\n";
			}
			
			strMolecule+="$$$\n";
			return strMolecule;
		}
	    
	}
	
	
	
	class MyRunnableTask implements Runnable {
	    private int num;

	    public MyRunnableTask(int num) {
	        this.num = num;
	    }

	    @Override
	    public void run() {
	        System.out.println("num "+num + " is running in thread: " + Thread.currentThread().getName());
	        runFromJson(num);
	    }

	}
	

	private void runFromJson(int num) {

		boolean removeAlreadyRan = true;
		boolean useServer=true;
		
		int maxCount = -1;// set to -1 to run all in sdf
//		int maxCount = 100;// set to -1 to run all in sdf

		
		int port = 8081 + num - 1;	
		
		if(num>11) port-=11;
		
		String server="http://v2626umcth882.rtord.epa.gov";
//		String server="http://localhost";

		String snapshot = "snapshot-2025-07-30";

		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		// String folder = folderMain + "data\\dsstox\\snapshot-2024-11-12\\sdf\\";

		String folderSrc = folderMain + "data\\dsstox\\" + snapshot + "\\json filter\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";

		new File(folderDest).mkdirs();

		
//		int from = 1 + 50000 * (num - 1);
		
		String filenameSrcJson = "prod_compounds"+num+".json";

		String filenameDestJson = filenameSrcJson;

		
		boolean skipMissingSID = true;

		String srcJsonPath = folderSrc + filenameSrcJson;
		String destJsonPath = folderDest + filenameDestJson;

		System.out.println("num="+num+",fileName="+filenameSrcJson);
		
		if(useServer) {
			runJson_all_endpoints_write_continuously_use_api(srcJsonPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan,server,port);
		} else {
//			runJson_all_endpoints_write_continuously(sdfPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan);
		}
		
		
	}
	
	
	private void runFromJson() {

		String filenameSrcJson = "prod_compounds_no_test_prediction.json";
		String snapshot = "snapshot-2025-07-30";
		
		String folderMain = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\";
		String folderSrc = folderMain + "data\\dsstox\\" + snapshot + "\\json filter\\";
		String folderDest = folderMain + "data\\TEST5.1.3\\reports\\" + snapshot + "\\";

		
		boolean removeAlreadyRan = true;
		boolean useServer=true;
		
		int maxCount = -1;// set to -1 to run all in sdf
//		int maxCount = 100;// set to -1 to run all in sdf

		int port = 8081;	
		String server="http://v2626umcth882.rtord.epa.gov";
//		String server="http://localhost";
		new File(folderDest).mkdirs();

		String filenameDestJson = filenameSrcJson;
		boolean skipMissingSID = true;
		String srcJsonPath = folderSrc + filenameSrcJson;
		String destJsonPath = folderDest + filenameDestJson;
		System.out.println("fileName="+filenameSrcJson);
		
		if(useServer) {
			runJson_all_endpoints_write_continuously_use_api(srcJsonPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan,server,port);
		} else {
//			runJson_all_endpoints_write_continuously(sdfPath, destJsonPath, skipMissingSID, maxCount, removeAlreadyRan);
		}
		
		
	}
	
	
	private String getFieldFromJson(String line, String fieldName) {
		String value=line.substring(line.indexOf("\""+fieldName+"\":\""),line.length());
		value=value.substring(fieldName.length()+4,value.length());
		value=value.substring(0,value.indexOf("\""));
//		System.out.println(value);
		return value;
	}
	
	
	
	private int removeAlreadyRanChemicals(String destJsonPath, List<DsstoxCompound>compounds) {
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

		Iterator<DsstoxCompound> iterator = compounds.iterator();
		while (iterator.hasNext()) {
			DsstoxCompound dc = iterator.next();

			if (htCountByDTXCID.containsKey(dc.dsstoxCompoundId)) {
				if (htCountByDTXCID.get(dc.dsstoxCompoundId) == 16) {
					iterator.remove(); // Removes safely
					count++;
				}
			}
		}
		System.out.println("Removed " + count + " chemicals since already ran");
		return count;
	}


	private void runJson_all_endpoints_write_continuously_use_api(String srcJsonPath, String destJsonPath,
			boolean skipMissingSID, int maxCount, boolean removeAlreadyRan, String server, int port) {

		
//		List<String>skipDtxsids=Arrays.asList("DTXSID90332347","DTXSID101484125","DTXSID801336080");
		List<String>skipDtxsids=new ArrayList<>();		

		Gson gson=new Gson();
		long beforeUsedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();


		try {
			List<DsstoxCompound>compounds=gson.fromJson(new FileReader(srcJsonPath), listOfMyClassObject);

			if (debug)
				System.out.println("atom container count in json=" + compounds.size());


			File destFile = new File(destJsonPath);
			FileWriter fw;
			int countRan = 0;

			if (removeAlreadyRan) {
				if (destFile.exists()) {
					countRan = removeAlreadyRanChemicals(destJsonPath, compounds);
					if (debug)
						System.out.println(countRan + " removed since already ran");
				}
				fw = new FileWriter(destJsonPath, Charset.forName("UTF-8"),destFile.exists());
			} else {
				fw = new FileWriter(destJsonPath);
			}


			System.gc(); // Request garbage collection to get a more accurate 'after' reading


			if (debug)
				System.out.println("atom container count to run=" + compounds.size());

			// if(true)return;

			if (compounds.size() == 0) {
				if (debug)
					System.out.println("All chemicals ran");
				return;
			}

			if (debug)
				System.out.println("");



			if (debug)
				System.out.println("");


			int counter=0;

			while (compounds.size()>0) {

				//			for (APIMolecule molecule:molecules) {

				DsstoxCompound dc=compounds.remove(0);

				
				if(skipDtxsids.contains(dc.genericSubstanceCompound.genericSubstance.dsstoxSubstanceId)) {
					System.out.println("Skipping "+dc.genericSubstanceCompound.genericSubstance.dsstoxSubstanceId);
					continue;
				}
					
				
				// if (debug)
				// System.out.println((i+countRan)+"\t"+destFile.getName()+"\t"+ac.getProperty("SMILES")+"");

				if (debug)
					logger.info("{}\t{}\t{}\t{}", (++counter+countRan), destFile.getName(), dc.genericSubstanceCompound.genericSubstance.dsstoxSubstanceId,
							dc.smiles);

//				System.out.println(dc.toString());
//				if(true)return;
				
				List<PredictionResults>results=TestApi.runPredictionFromMolFileString(dc.toString(), server, port);

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


	void healthCheck() {
		
		for (int i = 1; i <= 11; i++) {
			int port = 8081 + i - 1;		
			
			try {
			
				HttpResponse<String> response = Unirest.get("http://v2626umcth882.rtord.epa.gov:"+port+"/hello?name=todddddd")
				      .asString();
				System.out.println(i+"\t"+port+"\t"+response.getBody());
			
			} catch (Exception ex) {
				System.out.println("Error for i="+i+", port"+port);
			}
				    
		}
	}
	


	void runWithThreads() {
		
//		for (int i = 1; i <= 11; i++) {
		for (int i = 12; i <= 22; i++) {
			MyRunnableTask task = this.new MyRunnableTask(i);
            Thread thread = new Thread(task, "Thread-" + i);
            thread.start(); // Starts the thread, which calls the run() method
        }
		
		
	}

	public static void main(String[] args) {

		RunFromCompoundsJson r=new RunFromCompoundsJson();
		r.runFromJson();
//		r.runFromJson(2);
//		r.runWithThreads();
//		r.healthCheck();

	}

}
