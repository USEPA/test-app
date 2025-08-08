package gov.epa.test.api.predict;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.PredictionResults;

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

	
	
    @GetMapping("/hello")
    public Map<String, String> hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        return response;
    }
    
    
    @GetMapping("/predictGet")
    public String predictGet(@RequestParam(value = "smiles", required = true) String smiles,
    		@RequestParam(value = "casrn", required = true) String casrn,
    		@RequestParam(value = "dtxsid", required = false) String dtxsid,
    		@RequestParam(value = "dtxcid", required = false) String dtxcid) {
//    	System.out.println("Received molecule="+molecule);

    	List<PredictionResults>listResults=RunFromSmiles.runEndpointsAsListFromSmiles(smiles,dtxsid,dtxcid,casrn, endpoints,method,true,false);
//    	System.out.println("In controller,listResults:"+gson.toJson(listResults));
		
		Gson gson=new Gson();//gives one line json
    	return gson.toJson(listResults);
    }


	
	public static class PostInput {
		public String molecule;//needs to be public or have getter method
		PostInput() {}//also need this constructor or it wont work
		PostInput(String molecule) {
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