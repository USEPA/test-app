package gov.epa.api;



import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
* @author TMARTI02
*/
public class DashboardAPI {
	public static void main(String[] args) {
		Unirest.setTimeouts(0, 0);
		
		
		try {
			HttpResponse<String> response = Unirest.post("https://comptox.epa.gov/dashboard-api/ccdapp1/webtest/predict")
			  .header("Content-Type", "application/json")
			  .body("{\"query\":\"CCO\",\"queryType\":\"SMILES\",\"endpoints\":[{\"endpointCode\":\"LC50\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"LC50DM\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"IGC50\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"LD50\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"BCF\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"DevTox\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"Mutagenicity\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"ER_LogRBA\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"ER_Binary\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"BP\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"MP\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"FP\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"VP\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"Density\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"ST\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"TC\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"Viscosity\",\"providerCode\":\"TEST\"},{\"endpointCode\":\"WS\",\"providerCode\":\"TEST\"}]}")
			  .asString();
			
			System.out.println(response.getBody());
			
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
