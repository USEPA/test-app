package ToxPredictor.Application.Calculations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class sampleOperaOutput {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		JsonObject jo=new JsonObject();
		
		jo.addProperty("property", "logBCF");
		jo.addProperty("units", "log10[L/kg]");
		
		
		JsonArray ja=new JsonArray();
		
		jo.add("results",ja);		
		JsonObject jo1=new JsonObject();
		jo1.addProperty("experimental", 1.23);
		jo1.addProperty("predicted", 0.89);

		jo1.addProperty("AD", 1);
		jo1.addProperty("SMILES", "CCC");		
		ja.add(jo1);
		
		JsonObject jo2=new JsonObject();
		jo2.addProperty("experimental", 1.11);
		jo2.addProperty("predicted", 0.84);
		jo2.addProperty("AD", 1);
		jo2.addProperty("SMILES", "CCCC");		
		ja.add(jo2);
		
		
		jo.addProperty("success", true);
		jo.addProperty("time", "16.89 s");
		
    
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

        

        String json = gson.toJson(jo);
        System.out.println(json);
       
	}

}
