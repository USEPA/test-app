package gov.epa.test.api.predict;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreatorFromJSON;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
* @author TMARTI02
*/

@RestController
public class PredictController {

//	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	List<String>endpoints=RunFromSmiles.allEndpoints;
	String method = TESTConstants.ChoiceConsensus;

	PredictToxicityWebPageCreatorFromJSON webpageCreator=new PredictToxicityWebPageCreatorFromJSON ();

	
    @GetMapping("/hello")
    public Map<String, String> hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        return response;
    }
    
    /**
     * 
     * Currently this doesnt look up compound from database, relies on user to supply smiles, casrn,dtxsid etc
     * 
     * 
     * @param smiles
     * @param casrn
     * @param dtxsid
     * @param dtxcid
     * @param format
     * @return
     */
    @GetMapping("/predictGet")
    public String predictGet(@RequestParam(value = "smiles", required = true) String smiles,
    		@RequestParam(value = "casrn", required = true) String casrn,
    		@RequestParam(value = "dtxsid", required = false) String dtxsid,
    		@RequestParam(value = "dtxcid", required = false) String dtxcid,
    		@RequestParam(value = "format", required = false, defaultValue = "json") String format) {
//    	System.out.println("Received molecule="+molecule);

    	
    	List<PredictionResults>listResults=RunFromSmiles.runEndpointsAsListFromSmiles(smiles,dtxsid,dtxcid,casrn, endpoints,method,true,false);
//    	
//    	System.out.println("In controller,listResults:"+gson.toJson(listResults));

    	if(format.equals("json")) {
    		Gson gson=new Gson();//gives one line json
        	return gson.toJson(listResults);    		
    	} else if (format.equals("html")) {
    		

    		PredictToxicityWebPageCreatorFromJSON p=new PredictToxicityWebPageCreatorFromJSON ();
    		
    		StringWriter sw=new StringWriter();
    		
    		
    		PredictionResults pr=listResults.get(0);
    		p.writeConsensusResultsWebPages(pr,sw);
    		
    		Document doc = Jsoup.parse(sw.toString());
    		doc.outputSettings().indentAmount(2);
    		
    		//TODO make tabbed dialog with all reports as was done in predictGetDB()

    		
    		return doc.toString();
    	} else {
    		return format+" not implemented";
    	}

		

    }

    private String getFooter() {
		String footer="<script>\r\n"
				+ "    // JavaScript to handle tab switching\r\n"
				+ "    const tabs = document.querySelectorAll('.tab');\r\n"
				+ "    const tabContents = document.querySelectorAll('.tab-content');\r\n"
				+ "\r\n"
				+ "    tabs.forEach(tab => {\r\n"
				+ "        tab.addEventListener('click', () => {\r\n"
				+ "            // Remove active class from all tabs and contents\r\n"
				+ "            tabs.forEach(t => t.classList.remove('active'));\r\n"
				+ "            tabContents.forEach(tc => tc.classList.remove('active'));\r\n"
				+ "\r\n"
				+ "            // Add active class to the clicked tab and corresponding content\r\n"
				+ "            tab.classList.add('active');\r\n"
				+ "            document.getElementById(tab.dataset.tab).classList.add('active');\r\n"
				+ "        });\r\n"
				+ "    });\r\n"
				+ "</script>\r\n"
				+ "\r\n"
				+ "</body>\r\n"
				+ "</html>";
		
		return footer;
	}

    
    private String getHeader() {
		String header="html\r\n"
				+ "Copy code\r\n"
				+ "\r\n"
				+ "<!DOCTYPE html>\r\n"
				+ "<html lang=\"en\">\r\n"
				+ "<head>\r\n"
				+ "    <meta charset=\"UTF-8\">\r\n"
				+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
				+ "    <title>TEST Predictions</title>\r\n"
				+ "    <style>\r\n"
				+ "        /* Basic styling for tabs */\r\n"
				+ "        .tab-container {\r\n"
				+ "            display: flex;\r\n"
				+ "            position: fixed;\r\n"
				+ "            top: 0;\r\n"
				+ "            width: 100%;\r\n"
				+ "            background-color: #f1f1f1;\r\n"
				+ "            border-bottom: 1px solid #ccc;\r\n"
				+ "            z-index: 1000; /* Ensure tabs are above other content */\r\n"
				+ "        }\r\n"
				+ "        .tab {\r\n"
				+ "            padding: 10px 20px;\r\n"
				+ "            cursor: pointer;\r\n"
				+ "            border: 1px solid #ccc;\r\n"
				+ "            border-bottom: none;\r\n"
				+ "            background-color: #f1f1f1;\r\n"
				+ "        }\r\n"
				+ "        .tab.active {\r\n"
				+ "            background-color: #fff;\r\n"
				+ "            border-bottom: 1px solid #fff;\r\n"
				+ "        }\r\n"
				+ "        body {\r\n"
				+ "            margin: 0;\r\n"
				+ "            padding-top: 50px; /* Adjust based on tab height */\r\n"
				+ "        }\r\n"
				+ "        .tab-content {\r\n"
				+ "            display: none;\r\n"
				+ "            padding: 20px;\r\n"
				+ "            border: 1px solid #ccc;\r\n"
				+ "        }\r\n"
				+ "        .tab-content.active {\r\n"
				+ "            display: block;\r\n"
				+ "        }\r\n"
				+ "        table {\r\n"
				+ "            width: auto; /* Fit to content */\r\n"
				+ "            border-collapse: collapse;\r\n"
				+ "            margin-bottom: 10px;\r\n"
				+ "        }\r\n"
				+ "        th, td {\r\n"
				+ "            border: 1px solid #ccc;\r\n"
				+ "            padding: 8px;\r\n"
				+ "            text-align: left;\r\n"
				+ "        }\r\n"
				+ "    </style>\r\n"
				+ "</head>\r\n"
				+ "<body>";
				
		return header;
	}
    
    @GetMapping("/predictDB")
    public String predictGetDB(@RequestParam(value = "identifier", required = true) String identifier,
    					@RequestParam(value = "endpointAbbrev", required = false) String endpointAbbrev,
    		    		@RequestParam(value = "format", required = false, defaultValue = "json") String format) {
//    	System.out.println("Received molecule="+molecule);

    	ArrayList<DSSToxRecord>recs=ResolverDb2.lookup(identifier);
    	
    	if(recs.size()==0) {
    		return identifier+" not found in database";
    	}
    	
    	IAtomContainer ac=TaskStructureSearch.getMoleculeFromDSSToxRecords(recs);
    	
    	List<PredictionResults>listResults=null;
    	
    	if(endpointAbbrev!=null) {
    		List<String>endpoints=Arrays.asList(TESTConstants.getFullEndpoint(endpointAbbrev));
    		listResults=RunFromSmiles.runEndpointsAsList(ac, endpoints, method, true, false);
    	} else {
        	listResults=RunFromSmiles.runEndpointsAsList(ac, this.endpoints, method, true, false);
    	}
//    	System.out.println("In controller,listResults:"+gson.toJson(listResults));

    	if(format.equals("json")) {
    		 Gson gson = new GsonBuilder()
    	                .serializeSpecialFloatingPointValues()
    	                .create();
    		
        	return gson.toJson(listResults);    		
    	
    	} else if (format.equals("html")) {
    		if(listResults.size()==1) {
    			PredictionResults pr=listResults.get(0);
        		return getEndpointHtml(pr);
    		} if(listResults.size()>1) {
    			return getMultiEndpointHtml(listResults);
    		} else {
    			return "No results";
    		}
    	} else if(format.equals("html2")){
    		return "TODO implement Dashboard format";
    	} else {    		
    		return format+" not implemented";
    	}
    }

	private String getEndpointHtml(PredictionResults pr) {
		StringWriter sw=new StringWriter();
		webpageCreator.writeConsensusResultsWebPages(pr,sw);
		Document doc = Jsoup.parse(sw.toString());
		doc.outputSettings().indentAmount(2);
		return doc.toString();
	}

	

	
	

	
	public String getMultiEndpointHtml(List<PredictionResults> listResults) {
		StringBuffer sb=new StringBuffer();
		
		sb.append(getHeader());
		
		
		sb.append("<div class=\"tab-container\">\r\n");
		
		sb.append("<div class=\"tab active\" data-tab=\"Summary\">Summary</div>\r\n");

		for(PredictionResults pr:listResults) {
			String property=pr.getEndpoint();
			String property2=TESTConstants.getAbbrevEndpoint(pr.getEndpoint());//fix HLC
			sb.append("<div class=\"tab\" data-tab=\""+property2+"\">"+property+"</div>\r\n");
		}
		sb.append("</div>\r\n");


		String strSummary=webpageCreator.writeEndpointSummaryTable(listResults);
		
		sb.append("<div id=\"Summary\" class=\"tab-content active\">"+strSummary+"</div>\n");
		
		for(PredictionResults pr:listResults) {
			
			String property2=TESTConstants.getAbbrevEndpoint(pr.getEndpoint());
			
			StringWriter sw=new StringWriter();
			webpageCreator.writeConsensusResultsWebPages(pr,sw);
			Document doc = Jsoup.parse(sw.toString());
			doc.outputSettings().indentAmount(2);
//            		System.out.println(doc.body().toString());

			sb.append("<div id=\""+property2+"\" class=\"tab-content\">\n");
			sb.append(doc.body().html());
			sb.append("</div>\n");
		}
		
		sb.append(getFooter());
		Document doc = Jsoup.parse(sb.toString());
		doc.outputSettings().indentAmount(2);
//        		System.out.println(doc.toString());
		
		return doc.toString();
	}


    
	public static class PostInput {
		public String molecule;//needs to be public or have getter method
		PostInput() {}//also need this constructor or it wont work
		PostInput(String molecule) {//molecule is in V3000 format with property block (DTXSID, CASRN, etc)
			this.molecule=molecule;
		}
	}
	
    
    @PostMapping("/predictPost")
    //TODO fix, needs to use body instead of param
    public String predictPost(@RequestBody PostInput body) {
//    	System.out.println("Received molecule="+body.molecule);

    	List<PredictionResults>listResults=RunFromSmiles.runEndpointsAsList(body.molecule, endpoints, method, true, false);
//    	System.out.println("In controller,listResults:"+gson.toJson(listResults));
		
		Gson gson=new Gson();//gives one line json
    	return gson.toJson(listResults);
        
    }

    
}