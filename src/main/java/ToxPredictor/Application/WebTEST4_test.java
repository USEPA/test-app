package ToxPredictor.Application;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;

import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.TESTPredictedValue;

public class WebTEST4_test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			WebTEST4.reportFolderName="D:\\MyToxicity4";			
			WebTEST4.createDetailedReports=false;
			
//			String endpoint=TESTConstants.ChoiceFHM_LC50;
			String endpoint=TESTConstants.ChoiceReproTox;
			
			String method=TESTConstants.ChoiceConsensus;
//			String method=TESTConstants.ChoiceHierarchicalMethod;
//			String method=TESTConstants.ChoiceNearestNeighborMethod;
			
			String CAS="71-43-2";
//			String CAS="80-52-4";
			
			String StructureFolder="ValidatedStructures2d";
			String strFileSep="/";
			ChemicalFinder cf = new ChemicalFinder(
					StructureFolder+strFileSep+"manifest.txt", StructureFolder);
			
			IAtomContainer m=cf.FindChemicalFromCAS(CAS);

			CalculationParameters params=new CalculationParameters();
			params.endpoints= Arrays.asList(endpoint);
			params.methods= Arrays.asList(method);
			
			
			Set<WebReportType> wrt = WebReportType.getNone();
			wrt.add(WebReportType.HTML);
			wrt.add(WebReportType.JSON);
			params.reportTypes=wrt;

			List<TESTPredictedValue>tpvArray=WebTEST4.go(m,params);
			TESTPredictedValue tpv=tpvArray.get(0);
			
//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//			System.out.println(gson.toJson(tpv.predictionResults));

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
