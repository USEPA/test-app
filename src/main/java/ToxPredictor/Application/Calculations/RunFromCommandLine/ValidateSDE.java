package ToxPredictor.Application.Calculations.RunFromCommandLine;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles.ReportCreator;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;

/**
 * @author TMARTI02
 */
public class ValidateSDE {

	void rerunValerySample() {

		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

		String filepath = "data\\TEST1.0 reports\\valery sample reports.json";
		boolean createReports = true;// whether to store report
		boolean createDetailedReports = false;// detailed reports have lots more info and creates more html files

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		Gson gsonNotPretty = new Gson();

		String method = TESTConstants.ChoiceConsensus;// what QSAR method being used (default- runs all methods and

		for (String endpoint : RunFromSmiles.allEndpoints) {
			WebTEST4.loadTrainingData(endpoint, method);// Note: need to use webservice approach to make this data
														// persistent
		}

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));
			FileWriter fw = new FileWriter("data\\TEST1.0 reports\\todd sample reports.json");

			Map<String, PredictionResults> map = new LinkedHashMap<>();// preserves insertion order

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;
				PredictionResults prValery = gson.fromJson(line, PredictionResults.class);
				if (!map.containsKey(prValery.getDTXSID()))
					map.put(prValery.getDTXSID(), prValery);
//				System.out.println(gson.toJson(pr));
			}

			int counter = 0;

			for (String dtxsid : map.keySet()) {
				counter++;
//				System.out.println(counter+"\t"+map.get(dtxsid).getDTXSID());

				if (counter % 10 == 0)
					System.out.println(counter);

				PredictionResults prValery = map.get(dtxsid);

				AtomContainer ac;

				if (prValery.getSmiles() != null) {
					try {
						ac = (AtomContainer) sp.parseSmiles(prValery.getSmiles());
						// System.out.println(DTXCID+"\t"+smiles+"\t"+molecule2.getAtomCount());
					} catch (Exception ex) {
						ac = new AtomContainer();
					}

				} else {
					ac = new AtomContainer();
				}

				ac.setProperty(RunFromSmiles.strSID, prValery.getDTXSID());
				ac.setProperty(RunFromSmiles.strCID, prValery.getDTXCID());
				ac.setProperty(RunFromSmiles.strCAS, prValery.getCAS());
				ac.setProperty(RunFromSmiles.strSmiles, prValery.getSmiles());

				List<PredictionResults> results = RunFromSmiles.runEndpointsAsList(ac, RunFromSmiles.allEndpoints,
						method, createReports, createDetailedReports);

				for (PredictionResults pr : results) {
					fw.write(gsonNotPretty.toJson(pr) + "\r\n");
					fw.flush();
				}
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	void compareResults() {
		String filepath = "data\\TEST1.0 reports\\valery sample reports.json";
		String filepath2 = "data\\TEST1.0 reports\\todd sample reports.json";
		Gson gson = new Gson();

		try {

			BufferedReader br = new BufferedReader(new FileReader(filepath));
			BufferedReader br2 = new BufferedReader(new FileReader(filepath2));

			Map<String, List<PredictionResults>> mapValery = new TreeMap<>();// sort by key
			Map<String, List<PredictionResults>> mapTodd = new TreeMap<>();// sort by key

			while (true) {

				String line1 = br.readLine();
				String line2 = br2.readLine();
				if (line1 == null)
					break;

				PredictionResults prValery = gson.fromJson(line1, PredictionResults.class);
				PredictionResults prTodd = gson.fromJson(line2, PredictionResults.class);

				
				
				if (mapValery.containsKey(prValery.getDTXSID())) {
					List<PredictionResults> listValery = mapValery.get(prValery.getDTXSID());
					listValery.add(prValery);
				} else {
					List<PredictionResults> listValery = new ArrayList<>();
					listValery.add(prValery);
					mapValery.put(prValery.getDTXSID(), listValery);
				}

				if(prTodd!=null) {
					if (mapTodd.containsKey(prTodd.getDTXSID())) {
						List<PredictionResults> listTodd = mapTodd.get(prTodd.getDTXSID());
						listTodd.add(prTodd);
					} else {
						List<PredictionResults> listTodd = new ArrayList<>();
						listTodd.add(prTodd);
						mapTodd.put(prTodd.getDTXSID(), listTodd);
					}
					
				}

			}

			br.close();
			br2.close();

			String dtxsid = mapValery.keySet().iterator().next();

			PredictionResults prValery = mapValery.get(dtxsid).get(0);
			String htmlReportValery = ReportCreator.getReportAsHTMLString(prValery);

			List<PredictionResults> listTodd = mapTodd.get(dtxsid);
			
			System.out.println(dtxsid+"\t"+listTodd.get(0).getDTXSID());

			PredictionResults prTodd = null;

			for (PredictionResults pr : listTodd) {
				
//				System.out.println(prValery.getEndpoint()+"\t"+pr.getEndpoint());
				
				String abbrev=TESTConstants.getAbbrevEndpoint(pr.getEndpoint());
				
				if (abbrev.equals(prValery.getEndpoint())) {
					prTodd = pr;
					break;
				}
			}

			String htmlReportTodd = ReportCreator.getReportAsHTMLString(prTodd);

			// System.out.println(htmlReport);

			File fileValery = new File("data\\TEST1.0 reports\\valeryReport.html");
			FileWriter fw = new FileWriter(fileValery);
			fw.write(htmlReportValery);
			fw.flush();
			fw.close();

			Desktop desktop = Desktop.getDesktop();
			desktop.browse(fileValery.toURI());

			File fileTodd = new File("data\\TEST1.0 reports\\toddReport.html");

			fw = new FileWriter(fileTodd);
			fw.write(htmlReportTodd);
			fw.flush();
			fw.close();
			desktop.browse(fileTodd.toURI());

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) {
		ValidateSDE v = new ValidateSDE();
//		v.rerunValerySample();
		v.compareResults();

	}
}
