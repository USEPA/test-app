package ToxPredictor.Application.Calculations;


import java.util.List;
import org.openscience.cdk.AtomContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.Utilities.TESTPredictedValue;

public class RunFromJava {


	static TESTPredictedValue run(String SMILES,String endpointAbbrev,String methodAbbrev) {

		DescriptorData dd=null;

		try {

			AtomContainer ac = WebTEST4.loadSMILES(SMILES);

			String method=TESTConstants.getFullMethod(methodAbbrev);
			String endpoint=TESTConstants.getFullEndpoint(endpointAbbrev);
			
			CalculationParameters params = createCalculationParameters(method, endpoint);
			
			WebTEST4.createReports = false;// whether to reports at all for batch run
			WebTEST4.createDetailedReports = false;

			WebTEST4.loadTrainingData(endpoint, method);

			String error=(String)ac.getProperty("Error");

			if (!error.contentEquals("")) {
				TESTPredictedValue tpv=new TESTPredictedValue();
				tpv.error=error;
				return tpv;
			}

			dd=WebTEST4.goDescriptors(ac);//generate molecular descriptors
			List<TESTPredictedValue>listTPV=WebTEST4.go2(ac,dd, params);			
			return listTPV.get(0);//consensus is first prediction when running consensus, when running other methods will only have 1 prediction						

		} catch (Exception ex) {
			ex.printStackTrace();
			TESTPredictedValue tpv=new TESTPredictedValue();
			tpv.error=ex.getMessage();
			return tpv;
		}


	}


	private static CalculationParameters createCalculationParameters(String method, String endpoint) {
		CalculationParameters params=new CalculationParameters();

		params.endpoints= new String[1];
		params.endpoints[0]=endpoint;			
		params.methods= new String[1];
		params.methods[0]=method;
		return params;
	}


	public static void main(String[] args) {

		String SMILES="c1ccccc1";//benzene
//		String SMILES="CCCCO[Xe]";//should just give error
		//smiles that takes a long time to run:
//		String SMILES="CCC(C)C3C(=O)NC(C)C(=O)NC(=C)C(=O)NC(C)C(=O)NC24CC\\C(c1nc(C(=O)NC(=C)C(=O)NC(=C)C(N)=O)cs1)=N/C2c5csc(C(C(C)OC(=O)c2cc(C(C)O)c1\\C=C/C(C(O)c1n2)N3)NC(=O)c3csc(C(C(C)(O)C(C)O)NC(=O)C/2CS\\C(C(=C\\C)\\NC(=O)C(C(C)O)NC(=O)c1csc4n1)=N2)n3)n5";
//		String SMILES="CCCCOCOCOCOCCCCCOOOCCCCCCCCCCCCCCCCCCCCCCCCC";
		
		
		
		String endpoint=TESTConstants.abbrevChoiceFHM_LC50;//or just use "LC50"; 
		String method=TESTConstants.abbrevChoiceConsensus;//or just use "consensus";

		TESTPredictedValue tpv=RunFromJava.run(SMILES, endpoint, method);	

		if (tpv.error.contentEquals("")) {
			//Use json to display all the values inside tpv object:
			GsonBuilder builder = new GsonBuilder();
			builder.serializeSpecialFloatingPointValues();//allow NaN values for chemicals that have no prediction
			builder.setPrettyPrinting();			
			Gson gson = builder.create();
			String json=gson.toJson(tpv);			
			System.out.println(json);
			//			System.out.println(tpv.toString());
		} else 
			System.out.println(tpv.error);
	}
}

