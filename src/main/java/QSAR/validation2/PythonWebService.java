package QSAR.validation2;


import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

//import edu.stanford.ejalbert.BrowserLauncher;


public class PythonWebService {

	
	static void testHelloWorld() {
				
		try {

			HttpResponse<JsonNode> jsonResponse 
		      = Unirest.get("http://www.mocky.io/v2/5a9ce37b3100004f00ab5154")
		      .header("accept", "application/json").queryString("apiKey", "123")
		      .asJson();
		    System.out.println(jsonResponse.getBody());
		    
		    
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	class PythonQSARPrediction {
		
		String ID;
		double predVal;
		String method;
		
	}
	
	
	static void startPythonWebServerUsingDesktop() {

		File batFile=new File("run webservice.bat");
				
		
		if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(batFile.toURI());
//                System.out.println("desktop worked!");
                return;
            } catch (Exception ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        } else {

        	try {
        		//TODO BrowserLauncher maybe?
     
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }
	}
	/** 
	 * So far using ProcessBuilder doesnt work...
	 */
	static void startPythonWebServerUsingProcessBuilder() {

		try {
			String filePathPython="C:/Users/Xerxes73/anaconda3/envs/src Python/python.exe";
			File filePython=new File(filePathPython);

			File fileWebService=new File("src Python"+File.separator+"QSAR_Prediction_Web_Service.py");

			ProcessBuilder pb = new ProcessBuilder(filePython.getAbsolutePath(),fileWebService.getAbsolutePath());
			Process p = pb.start();

//			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			
//			while (true) {
//				String Line=in.readLine();								
//				if (Line==null) break;
//				System.out.println(Line);
//			}
			
//			p.destroy();
			
			
		}catch(Exception e){
			System.out.println(e); 
		}
	}
	
	
	/**
	 * Start web-server using runtime- not reliable. 
	 */
	static void startPythonWebServerUsingRuntime() {
		
		
		 
		try{
		 
			String filePathPython="C:/Users/Xerxes73/anaconda3/envs/src Python/python.exe";
			File filePython=new File(filePathPython);
			
			File fileWebService=new File("src Python"+File.separator+"QSAR_Prediction_Web_Service.py");
			
			String command="\""+filePython.getAbsolutePath()+"\" \""+fileWebService.getAbsolutePath()+"\"";
			
			System.out.println(command);
			
			Process p = Runtime.getRuntime().exec(command);
		
		}catch(Exception e){
			e.printStackTrace();
		
		}
		
	}
	static void testAccessPython() {		

		String endpointAbbrev="LLNA";
		String methodAbbrev="SVM";
				
		//*****************************************************
		JsonObject jo = createExampleJSon(methodAbbrev);
//		System.out.println(jo.toString());
		//******************************************************************************
						
		String baseurl="http://127.0.0.1:8080/"+endpointAbbrev+"/";		
//		
		try {

			Unirest.setTimeouts(0, 0);
			HttpResponse<JsonNode>request = Unirest.get(baseurl+jo.toString()).asJson();			
			JSONObject myObj = request.getBody().getObject();
			System.out.println(myObj);
			
			
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();

			//Save to class in one line:
			PythonQSARPrediction qp=gson.fromJson(myObj.toString(), PythonQSARPrediction.class);
			System.out.println("ID="+qp.ID+", method="+qp.method+", predVal="+qp.predVal);
			
			String ID=myObj.getString("ID");
			double predVal=myObj.getDouble("predVal");
			String method=myObj.getString("method");			
			System.out.println("ID="+ID+", method="+method+", predVal="+predVal);
			
			
//			HttpResponse<String> response
			
//	        profile = gson.fromJson(response.getBody(), StandardProfile.class);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	}


	private static JsonObject createExampleJSon(String methodAbbrev) {
		String csvLine = "926-63-6,-9999,4.99156383156272,2.770055610029662,2.1825219847121646,0.8660254037844386,0.5773502691896257,0.0,0.0,0.0,0.0,0.0,0.0,0.408248290463863,0.0,0.28867513459481287,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.11957315586905015,4.861427157873052,2.417761738203301,1.8032759252836128,0.6708203932499369,0.4472135954999579,0.0,0.0,0.0,0.0,0.0,0.0,0.31622776601683794,0.0,0.22360679774997896,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.09262096826685898,6.0,3.2,5.333333333333333,5.961038961038961,3.162609641667233,5.298758259284573,3.14207321542264,6.354166666666666,0.0,2.4652777777777777,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,2.1805555555555554,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,3,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.0,2.1063055555555557,0.0,0.0,0.0,0.0,0.0,0.0,0.49166666666666664,2.1805555555555554,0.3319722222222222,1.2083333333333335,0.49166666666666664,0.0,1.0,0.0,0.0,2.1805555555555554,0.0,0.0,3.0743801652892553,2.818181818181818,1.8503213957759408,2.0,2.2516291673878226,0.0,13.509775004326936,15.509775004326936,0.8710490642551528,0.12895093574484717,2.298882115832523,30.6865864313812,0.8524051786494786,33.219280948873624,3.3219280948873626,1.4591479170272446,2.4464393446710155,24.273764861366715,0.9709505944546687,33.219280948873624,3.3219280948873626,28.62919048309069,1.9086126988727121,119.7353374935096,3.7417292966721747,1.9182958340544891,2.5537289082033148,14.854752972273346,1.4854752972273344,17.0,53.97709329692883,3.1751231351134614,0.0,0.0,0.0,0.0,0.0,19.70175057828924,23.27881749690966,0.944940787421155,2.94168275343288,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,3.451480206755227,2.974600156753219,2.3309429173233274,2.2120615363009555,1.4129967790886357,0.5997259111720111,0.08309658617818558,0.08309658617818516,1.8164191974401918,1.5591532031761688,1.2488463311451423,1.1169277692974,0.6454501820214048,0.0,0.0,0.0,3.398917762563123,2.9721249801101384,2.413915683556257,2.291386751734625,1.517532058247226,0.486007807923481,0.2978864142538988,0.2978864142538982,1.8001256297592685,1.4956109006358476,1.1170292693023618,0.9865527445239766,0.4832569230759889,0.0,0.0,0.0,3.596804699895185,3.2005168358696876,2.700472375216176,2.573403337908177,1.9321523936460774,1.0433298800901887,0.9408181818181829,0.9408181818181827,1.5609950373923775,1.2107646593248051,0.7606541933979963,0.6306846063134265,0.00649011710598196,0.0,0.0,0.0,3.4040050038709366,2.9848380476093563,2.4471497020438404,2.3235421278694077,1.5639632953686708,0.4956692102221334,0.37968181818181895,0.3796818181818184,1.7879405699282012,1.4660099171617154,1.0684678838620232,0.9382760770851616,0.422063848038152,0.0,0.0,0.0,20.0,36.0,18.0,26.0,2.6272148478988635,5.014346335622947,14.0,1.4591479170272446,1.4591479170272448,4.360925932006757,0.29166666666666663,0.18055555555555555,1.0694444444444444,32.0,2.1333333333333333,87.19000000000001,4.588947368421053,9.580400890868598,18.403636363636362,10.573863636363637,11.0,0.5042316258351893,0.9686124401913875,0.5565191387559809,1.8333333333333333,19,6,18,5,0,5.0,0.0,0,0,0,0,0,13,5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1.8717381256605767,1.8191359393935744,1.4270830495068183,1.0986122886681098,0.0,0.0,0.0,0.0,1.6262227587875413,1.7395670875489324,1.3069474024481902,1.0986122886681098,0.0,0.0,0.0,0.0,1.8687205103641833,1.8180767775454285,1.425515074273172,1.0986122886681098,0.0,0.0,0.0,0.0,1.5841201044498106,1.7272209480904839,1.2878542883066382,1.0986122886681098,0.0,0.0,0.0,0.0,-0.5199999999999991,-0.04000000000000044,-0.2,0.19999999999999893,0.0,0.0,0.0,0.0,-0.5200000000000001,-0.03999999999999991,-0.19999999999999998,0.20000000000000015,0.0,0.0,0.0,0.0,-0.5199999999999997,-0.040000000000000195,-0.2,0.19999999999999948,0.0,0.0,0.0,0.0,-0.52,-0.04,-0.2,0.2,0.0,0.0,0.0,0.0,1.7999999999999998,0.6,1.0,0.0,0.0,0.0,0.0,0.0,1.8,0.6000000000000001,1.0000000000000002,0.0,0.0,0.0,0.0,0.0,1.8000000000000003,0.6000000000000001,1.0000000000000002,0.0,0.0,0.0,0.0,0.0,1.8,0.6,1.0,0.0,0.0,0.0,0.0,0.0,5.0,3.044522437723423,3.6109179126442243,4.290459441148391,4.875197323201151,5.564520407322694,6.154858094016418,6.846943139585379,7.438971592395862,8.131824785007195,6.0,10.0,0.0,30.0,0.0,100.0,0.0,350.0,0.0,1250.0,5.0,5.0,3.0,2.0,0.0,0.0,0.0,0.0,0.0,0.0,1.791759469228055,1.791759469228055,1.3862943611198906,1.0986122886681098,0.0,0.0,0.0,0.0,0.0,0.0,12.06842558824411,60.95821513304473,12.06842558824411,10.679177494338822,1.779862915723137,6.5890746507011775,0.844,0.712336,1.1925999999999997,1.422294759999999,29.2603,-0.7673185176346681,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,3.0,0.0,2.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0";
		String [] vals=csvLine.split(",");		
		JsonArray ja=new JsonArray();		
		for (String val:vals) ja.add(val);	

		JsonObject jo=new JsonObject();		
		jo.addProperty("method", methodAbbrev);	
		jo.add("csv", ja);
		return jo;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		startPythonWebServerUsingRuntime();
//		startPythonWebServerUsingProcessBuilder();
		startPythonWebServerUsingDesktop();
		
		
		testAccessPython();
//		testHelloWorld();
	}

}
