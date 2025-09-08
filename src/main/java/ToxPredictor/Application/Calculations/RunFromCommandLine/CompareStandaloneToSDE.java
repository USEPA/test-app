package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;


import java.util.*;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.ReportCreator;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus.PredictionIndividualMethod;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.Application.model.SimilarChemicals;


/**
* @author TMARTI02
*/
public class CompareStandaloneToSDE {

	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
	String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\TEST1.0\\";

	PredictionResults getPredictionResultsFromSDEresults(int index) {

//		String filepath=folder+"predictionResults.json.gz";
		String filepath=folder+"predictResults-head.json.gz";
		
		try {
			
			InputStream fileStream = new FileInputStream(filepath);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream,  StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(decoder);
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				
				if(Line==null) {
					br.close();
					return null;
				}
				counter++;
				
				if(counter==index) {
					PredictionResults pr=gson.fromJson(Line, PredictionResults.class);
					System.out.println(gson.toJson(pr));
					br.close();
					return pr;
				}
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	void getPredictionResultsFromSDEresultsSalt() {

		String filepath=folder+"predictionResults.json.gz";
		
		try {
			
			InputStream fileStream = new FileInputStream(filepath);
			InputStream gzipStream = new GZIPInputStream(fileStream);
			Reader decoder = new InputStreamReader(gzipStream,  StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(decoder);
			
			int counter=0;
			
			while (true) {
			
				String Line=br.readLine();
				if(Line==null) break;
				counter++;
				
				if(counter%1000==0) System.out.println(counter);
				PredictionResults pr=gson.fromJson(Line, PredictionResults.class);

				if(pr.getSmiles().contains(".")) {
					System.out.println(gson.toJson(pr));
					break;
				}
				

			}
			
			
			
			br.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	void writeResultsHtml(String htmlReport,String filename) {
		try {
			File file=new File(folder+filename);
			FileWriter fw=new FileWriter(file);
			fw.write(htmlReport);
			fw.flush();
			fw.close();
			
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(file.toURI());


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void fixPredictionResultsSDE(PredictionResults pr) {
		
		pr.setWebPathDashboardPage("https://comptox.epa.gov/dashboard/chemical/details/");

		pr.setEndpoint(TESTConstants.getFullEndpoint(pr.getEndpoint()));
		pr.setLogMolarEndpoint(TESTConstants.isLogMolar(pr.getEndpoint()));
		pr.setBinaryEndpoint(TESTConstants.isBinary(pr.getEndpoint()));
		
		if(pr.getIndividualPredictionsForConsensus()!=null) {
			Vector<PredictionIndividualMethod>vec=pr.getIndividualPredictionsForConsensus().getConsensusPredictions();
			for (PredictionIndividualMethod pim:vec) {
				pim.setMethod(TESTConstants.getFullMethod(pim.getMethod()));
//				System.out.println(pim.getMethod());
			}
		}
		
		SimilarChemicals scTest=pr.getSimilarChemicals().get(0);
		SimilarChemicals scTraining=pr.getSimilarChemicals().get(1);
		
		addTestChemicalToSimilarChemicals(scTest, pr);
		addTestChemicalToSimilarChemicals(scTraining, pr);
		
		PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();
		pt.setMessage(pr.getError());
	}
	
	void addTestChemicalToSimilarChemicals(SimilarChemicals sc,PredictionResults pr) {
		
		Vector<SimilarChemical>scs=sc.getSimilarChemicalsList();
		
		SimilarChemical sc0=new SimilarChemical();
		
		sc0.setImageUrl(pr.getImageURL());
		sc0.setDSSTOXSID(pr.getDTXSID());
		sc0.setDSSTOXCID(pr.getDTXCID());
		sc0.setSimilarityCoefficient("1.00");
		
		PredictionResultsPrimaryTable pt=pr.getPredictionResultsPrimaryTable();
				
		if (pt.getExpToxValue()!=null) sc0.setExpVal(pt.getExpToxValue());
		else if(pt.getExpToxValMass()!=null) sc0.setExpVal(pt.getExpToxValMass());

		if (pt.getPredToxValue()!=null)  sc0.setPredVal(pt.getPredToxValue());
		else if(pt.getPredToxValMass()!=null) sc0.setPredVal(pt.getPredToxValMass());
		
		sc0.setBackgroundColor("lightgray");
		scs.add(0,sc0);
		
		
	}
	
	public static void main(String[] args) {

		CompareStandaloneToSDE c=new CompareStandaloneToSDE();

		PredictionResults pr=c.getPredictionResultsFromSDEresults(2);		
//		PredictionResults pr=c.getPredictionResultsFromSDEresults(1383);
//		PredictionResults pr=c.getPredictionResultsFromSDEresults(1384);
		c.fixPredictionResultsSDE(pr);
		String htmlReport=ReportCreator.getReportAsHTMLString(pr);
		c.writeResultsHtml(htmlReport, "resultsHTML_SDE.html");
		
//		c.getPredictionResultsFromSDEresultsSalt();
		
		
		

	}

}

