package ToxPredictor.Application.Calculations;

import ToxPredictor.Application.ReportOptions;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart.JLabelChart;
import ToxPredictor.Application.model.CancerStats;
import ToxPredictor.Application.model.ClusterModel;
import ToxPredictor.Application.model.ClusterTable;
import ToxPredictor.Application.model.Descriptor;

import ToxPredictor.Application.model.EndpointSource;
import ToxPredictor.Application.model.ExternalPredChart;
import ToxPredictor.Application.model.IndividualPredictionsForConsensus;
import ToxPredictor.Application.model.MOAPrediction;
import ToxPredictor.Application.model.MOATable;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Application.model.PredictionResultsPrimaryTable;
import ToxPredictor.Application.model.SimilarChemical;
import ToxPredictor.Application.model.SimilarChemicals;
import ToxPredictor.Database.DSSToxRecord;
//import ToxPredictor.Database.ChemistryDashboardRecord;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.Utilities.ReportUtils;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.misc.Lookup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ToxPredictor.Application.Calculations.CreateImageFromTrainingPredictionSDFs;

import QSAR.qsarOptimal.AllResults;
import QSAR.qsarOptimal.OptimalResults;
import QSAR.validation2.TestChemical;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import wekalite.Instance;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

public class PredictToxicityJSONCreator {

	private static final Logger logger = LogManager.getLogger(PredictToxicityJSONCreator.class);

	//	public static boolean isBinaryEndpoint = false;
	//	public static boolean isLogMolarEndpoint = true;

	private Lookup lookup = new Lookup();


	static DecimalFormat d2 = new DecimalFormat("0.00");
	static DecimalFormat d3 = new DecimalFormat("0.000");
	static DecimalFormat d2exp = new DecimalFormat("0.00E00");

	public static boolean forGUI=false;


	//	private PredictionResults predictionResults = new PredictionResults();

	Hashtable<String,String>htVarDefs=null;

	public PredictToxicityJSONCreator() {
		htVarDefs=LoadDefinitions();
	}


	public PredictionResults writeConsensusResultsJSON(double predToxVal, double predToxUnc, String method, String CAS,String dtxcid, String dtxsid,String endpoint, String abbrev, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, Lookup.ExpRecord er, double MW, String message, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, ArrayList methods,
			ArrayList predictions, ArrayList uncertainties, boolean createDetailedConsensusReport, 
			ReportOptions options) {
		try {

			PredictionResults pr=new PredictionResults();

			IndividualPredictionsForConsensus individualPredictionsForConsensus = pr.getIndividualPredictionsForConsensus();
			pr.setCreateDetailedReport(createDetailedConsensusReport);
			PredictionResultsPrimaryTable predictionResultsPrimaryTable = pr.getPredictionResultsPrimaryTable();

			String outputfilename = "PredictionResults";

			outputfilename += method.replaceAll(" ", "");
			outputfilename += ".json";

			String jsonFilePath = Paths.get(options.reportBase, outputfilename).toFile().getAbsolutePath();
			FileWriter fw = new FileWriter(jsonFilePath);


			pr.setCAS(CAS);

			pr.setEndpoint(endpoint);
			pr.setBinaryEndpoint(isBinaryEndpoint);
			pr.setLogMolarEndpoint(isLogMolarEndpoint);

			pr.setMethod(method);

			pr.setSCmin(PredictToxicityWebPageCreator.SCmin);

			int predCount = 0;
			for (int i = 0; i < predictions.size(); i++) {
				if ((Double) predictions.get(i) != -9999)
					predCount++;
			}

			if (predCount == 0) {
				message = "No prediction could be made";
			} else if (predCount < TaskCalculations.minPredCount) {
				message = "The consensus prediction for this chemical is considered unreliable since only one prediction can only be made";
			}


			this.writeIndividualPredictionsForConsensus(pr,methods, predictions, uncertainties, createDetailedConsensusReport);

			predictionResultsPrimaryTable.setDtxcid(dtxcid);
			pr.setImgSize(PredictToxicityWebPageCreator.imgSize);
			pr.setWebPath(PredictToxicityWebPageCreator.webPath);
			pr.setWebPath2(PredictToxicityWebPageCreator.webPath2);


			if (dtxcid == null) {
				//Following code doesnt seem to work:		
				//				individualPredictionsForConsensus.setImageUrl(ReportUtils.convertImageToBase64(ReportUtils.getImageSrc(options, "../StructureData/structure.png")));
				//				logger.debug("valeryURL="+ReportUtils.getImageSrc(options, "../StructureData/structure.png"));

				//Using brute force to make sure path is right:
				File outputFolder=new File(options.reportBase);
				File imageFile=new File(outputFolder.getParentFile().getAbsolutePath()+File.separator+"StructureData/structure.png");
				String strURL = imageFile.toURI().toURL().toString();
				pr.setImageURL(ReportUtils.convertImageToBase64(strURL));

			} else {
				//use dashboard image if available:
				pr.setImageURL(PredictToxicityWebPageCreator.webPath + dtxcid);
				//				individualPredictionsForConsensus.setImageUrl(ReportUtils.convertImageToBase64(PredictToxicityWebPageCreator.webPath + gsid));
			}

			//			if (createDetailedConsensusReport) {
			//				fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for " + "test chemical</a></p>\n");
			//			}

			this.writeSimilarChemicals(pr,"test", htTestMatch, abbrev, er.expToxValue, predToxVal,  CAS, dtxcid, dtxsid,options);

			this.writeSimilarChemicals(pr,"training", htTrainMatch, abbrev, er.expToxValue, predToxVal, CAS, dtxcid, dtxsid, options);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(pr, fw);

			fw.close();

			return pr;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return null;
	}

	private void setCommonValues(String method,DataForPredictionRun d,TESTPredictedValue tpv,PredictionResults pr,double predToxVal,double predToxUnc,boolean createReports) throws Exception {

		pr.setCreateDetailedReport(d.createDetailedReport);
		pr.setCAS(d.CAS);
		pr.setEndpoint(d.endpoint);
		pr.setBinaryEndpoint(d.isBinaryEndpoint);
		pr.setLogMolarEndpoint(d.isLogMolarEndpoint);
		pr.setMethod(method);
		
//		System.out.println(pr.getMethod());
		
		pr.setSCmin(PredictToxicityWebPageCreator.SCmin);

		pr.setImgSize(PredictToxicityWebPageCreator.imgSize);
		pr.setWebPath(PredictToxicityWebPageCreator.webPath);
		pr.setWebPath2(PredictToxicityWebPageCreator.webPath2);

		if (createReports) {
			if (d.dtxcid == null || !WebTEST4.dashboardStructuresAvailable) {
				//Using brute force to make sure path is right:
				File outputFolder=new File(d.reportOptions.reportBase);
				File imageFile=new File(outputFolder.getParentFile().getAbsolutePath()+File.separator+"StructureData/structure.png");

				if (forGUI) {
					pr.setImageURL("../StructureData/structure.png");
				} else {
					String strURL = imageFile.toURI().toURL().toString();
					pr.setImageURL(ReportUtils.convertImageToBase64(strURL));

				}
			} else {
				//use dashboard image if available:
				pr.setImageURL(PredictToxicityWebPageCreator.webPath + d.dtxcid);
				//individualPredictionsForConsensus.setImageUrl(ReportUtils.convertImageToBase64(PredictToxicityWebPageCreator.webPath + gsid));
			}
		}

		//		double predToxVal=getPredToxVal(tpv, d);

		long t1=System.currentTimeMillis();
		
		if (createReports) writeSimilarChemicals(pr,"test", d.htTestMatch, d.abbrev, d.er.expToxValue, predToxVal, d.CAS, d.dtxcid, d.dtxsid,d.reportOptions);
		if (createReports) writeSimilarChemicals(pr,"training", d.htTrainMatch, d.abbrev, d.er.expToxValue, predToxVal, d.CAS, d.dtxcid,d.dtxsid, d.reportOptions);

		long t2=System.currentTimeMillis();
		
//		if (createReports)
//			System.out.println((t2-t1)+" millsecs to find similar chemicals for "+pr.getCAS()+" for "+pr.getMethod());
		
		if (d.isBinaryEndpoint) {
			this.WriteBinaryPredictionTable(pr, d.dtxcid,d.er, predToxVal, tpv.message);
		} else {
			this.writeMainTable(pr, d.dtxcid, tpv, predToxVal,predToxUnc, d.MW, d.er, tpv.message);
		}
	}

	//	private double getPredToxVal(TESTPredictedValue tpv,DataForPredictionRun d) {		
	//		if (d.isBinaryEndpoint || d.isLogMolarEndpoint) {
	//			return tpv.predValLogMolar;
	//		} else {
	//			return tpv.predValMass;
	//		}
	//	}

	public PredictionResults generatePredictionResultsConsensus(DataForPredictionRun d,TESTPredictedValue tpv,ArrayList<Double>predictedToxicities,ArrayList<Double>predictedUncertainties,double predToxVal,ReportOptions options,boolean createReports) {
		try {
			PredictionResults pr=new PredictionResults();
			pr.setReportBase(options.reportBase);
			setCommonValues(TESTConstants.ChoiceConsensus,d, tpv,pr,predToxVal,-9999,createReports);

			ArrayList<String> methods = TaskCalculations.getMethods(d.endpoint);
			this.writeIndividualPredictionsForConsensus(pr,methods, predictedToxicities, predictedUncertainties, d.createDetailedReport);			
			
//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//			String json=gson.toJson(pr);
//			System.out.println(json);

			return pr;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return null;
	}


	public PredictionResults generatePredictionResultsNearestNeighbor(DataForPredictionRun d,TESTPredictedValue tpv,double predToxVal,ReportOptions options,boolean createReports) {

		try {
			PredictionResults pr=new PredictionResults();
			pr.setReportBase(options.reportBase);
			setCommonValues(TESTConstants.ChoiceNearestNeighborMethod, d, tpv, pr, predToxVal,-9999,createReports);
			return pr;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return null;
	}


	//	public PredictionResults generatePredictionResultsError(DataForPredictionRun d,TESTPredictedValue tpv,String method) {		
	//		try {
	//			PredictionResults pr=new PredictionResults();
	//			setCommonValues(method, d, tpv, pr, -9999,-9999);
	//			return pr;
	//
	//		} catch (Exception ex) {
	//			logger.catching(ex);
	//		}
	//		return null;
	//	}


	//	private void WriteClusterTableNN(String CAS, String endpoint, double ExpToxVal, Instances cc, Vector<Double> SimCoeffCluster,  ReportOptions options,PredictionResults pr)
	//			throws IOException {
	//
	//		// FileWriter fw=new FileWriter (filepath);
	//		if (cc == null)
	//			return;
	//
	//		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
	//
	//		SimilarChemicals similarChemicals=new SimilarChemicals();
	//		
	//		if (TESTConstants.isLogMolar(endpoint))
	//			similarChemicals.setUnits(TESTConstants.getMolarLogUnits(endpoint));
	//		else
	//			similarChemicals.setUnits(TESTConstants.getMassUnits(endpoint));
	//
	//		similarChemicals.setExpVal(df.format(ExpToxVal));
	//		similarChemicals.setImageUrl(ReportUtils.getImageSrc(options, "../StructureData/structure.png"));
	//
	//		for (int i = 0; i < cc.numInstances(); i++) {
	//			SimilarChemical similarChemical = new SimilarChemical();
	//			String CASi=cc.instance(i).getName();
	//			similarChemical.setCAS(CASi);
	//			similarChemical.setImageUrl(ReportUtils.getImageSrc(options, "../../images", CASi + ".png"));
	//			similarChemical.setExpVal(df.format(cc.instance(i).classValue()));
	//			similarChemical.setSimilarityCoefficient(df.format(SimCoeffCluster.get(i))+"");
	//			
	//			String strColor = PredictToxicityWebPageCreator.getColorString(SimCoeffCluster.get(i));
	//			similarChemical.setBackgroundColor(strColor);
	//			
	//			similarChemicals.getSimilarChemicalsList().add(similarChemical);
	//		}
	//		pr.setSimilarChemicalsForNN(similarChemicals);
	//		
	//	}


	public static Vector<EndpointSource> getSourceVector(String endpoint) {
		//TODO needed?

		Vector<EndpointSource> endpointSources = new Vector<>();

		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50) || endpoint.equals(TESTConstants.ChoiceGA_EC50)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("ECOTOX");
			endpointSource.setSourceURL("http://cfpub.epa.gov/ecotox");
			endpointSources.add(endpointSource);
		} else if (endpoint.equals(TESTConstants.ChoiceTP_IGC50)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("TETRATOX");
			endpointSource.setSourceURL("http://www.vet.utk.edu/TETRATOX/index.php");
			endpointSources.add(endpointSource);
		} else if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("ChemidPlus");
			endpointSource.setSourceURL("http://chem.sis.nlm.nih.gov/chemidplus");
			endpointSources.add(endpointSource);
		} else if (endpoint.equals(TESTConstants.ChoiceBCF)) {
			// String ref="<br>Source: <br><ul{list-style:none}>";

			// ref+="<li>SAR QSAR Environ Res, 16, p. 531-554
			// (2005)</li>";//http://www.tandfonline.com/doi/abs/10.1080/10659360500474623
			// ref+="<li>Environ. Rev., 14:257-297
			// (2006)</li>";//http://www.nrcresearchpress.com/doi/abs/10.1139/a06-005
			// ref+="<li>Chemosphere, 73:1701-1707
			// (2008)</li>";//http://www.sciencedirect.com/science/article/pii/S0045653508011922
			// ref+="</ul>";

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Dimetrov 2005");
			endpointSource.setSourceURL("http://www.tandfonline.com/doi/abs/10.1080/10659360500474623");
			endpointSources.add(endpointSource);

			endpointSource = new EndpointSource();
			endpointSource.setSourceName("Arnot 2006");
			endpointSource.setSourceURL("http://www.nrcresearchpress.com/doi/abs/10.1139/a06-005");
			endpointSources.add(endpointSource);

			endpointSource = new EndpointSource();
			endpointSource.setSourceName("Zhao 2008");
			endpointSource.setSourceURL("http://www.sciencedirect.com/science/article/pii/S0045653508011922");
			endpointSources.add(endpointSource);

		} else if (endpoint.equals(TESTConstants.ChoiceReproTox)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("CAESAR");
			endpointSource.setSourceURL("http://www.caesar-project.eu/index.php?page=results&section=endpoint&ne=5");
			endpointSources.add(endpointSource);
		} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Toxicity Benchmark");
			endpointSource.setSourceURL("http://doc.ml.tu-berlin.de/toxbenchmark");
			endpointSources.add(endpointSource);

		} else if (endpoint.equals(TESTConstants.ChoiceDensity) || endpoint.equals(TESTConstants.ChoiceFlashPoint)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Lookchem.com");
			endpointSource.setSourceURL("http://www.lookchem.com");
			endpointSources.add(endpointSource);

		} else if (endpoint.equals(TESTConstants.ChoiceViscosity)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Viswanath 1989");
			endpointSource.setSourceURL("http://www.worldcat.org/title/data-book-on-the-viscosity-of-liquids/oclc/18833753");
			endpointSources.add(endpointSource);

			endpointSource = new EndpointSource();
			endpointSource.setSourceName("Riddick 1996");
			endpointSource.setSourceURL("http://www.wiley.com/WileyCDA/WileyTitle/productCd-0471084670.html");
			endpointSources.add(endpointSource);

		} else if (endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {

			// String ref="<br>Sources: <br><ul{list-style:none}>";
			// ref+="<li>Jamieson, D.T.; Irving, J.B; Tudhope, J.S. <br>" +
			// "\"Liquid Thermal Conductivity. A Data Survey to 1973,\"<br>" +
			// "H. M. Stationary Office, Edinburgh, 1975</li>";
			// ref+="<li>Vargaftik, N. B., Filippov, L. P., Tarzimanov, A. A.,
			// <br>" +
			// "and Totskii, E. E. 1994. Handbook of thermal conductivity<br>" +
			// " of liquids and gases. Boca Raton: CRC Press.</li>";
			// ref+="</ul>";

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Jamieson 1975");
			endpointSource.setSourceURL("http://www.worldcat.org/title/liquid-thermal-conductivity-a-data-survey-to-1973/oclc/3090244");
			endpointSources.add(endpointSource);

			endpointSource = new EndpointSource();
			endpointSource.setSourceName("Vargaftik 1994");
			endpointSource.setSourceURL("http://www.worldcat.org/title/handbook-of-thermal-conductivity-of-liquids-and-gases/oclc/28847166&referer=brief_results");
			endpointSources.add(endpointSource);
		} else if (endpoint.equals(TESTConstants.ChoiceSurfaceTension)) {
			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Jaspar 1972");
			endpointSource.setSourceURL("http://jpcrd.aip.org/resource/1/jpcrbu/v1/i4/p841_s1?isAuthorized=no");
			endpointSources.add(endpointSource);

		} else if (endpoint.equals(TESTConstants.ChoiceWaterSolubility) || endpoint.equals(TESTConstants.ChoiceBoilingPoint) || endpoint.equals(TESTConstants.ChoiceVaporPressure)
				|| endpoint.equals(TESTConstants.ChoiceMeltingPoint)) {

			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("EPI Suite v 4.00");
			endpointSource.setSourceURL("http://www.epa.gov/opptintr/exposure/pubs/episuite.htm");
			endpointSources.add(endpointSource);

		} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
			EndpointSource endpointSource = new EndpointSource();
			endpointSource.setSourceName("Cronin and Livingstone, 2004");
			endpointSource.setSourceURL("http://www.worldcat.org/title/predicting-chemical-toxicity-and-fate/oclc/54073110&referer=brief_results");
			endpointSources.add(endpointSource);
		} else {
		}

		return endpointSources;
	}

	public static void writeMainTable(PredictionResults pr,String dtxcid,TESTPredictedValue tpv,double predToxVal,double predToxUnc, double MW, Lookup.ExpRecord er, String message) throws Exception {

		String method=pr.getMethod();
		String endpoint=pr.getEndpoint();

		double ExpToxValMass = tpv.expValMass;
		double PredToxValMass = tpv.predValMass;

		boolean writePredictionInterval = true;

		PredictionResultsPrimaryTable predictionResultsPrimaryTable = pr.getPredictionResultsPrimaryTable();
		predictionResultsPrimaryTable.setDtxcid(dtxcid);


		if (method.equals(TESTConstants.ChoiceConsensus) || method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
			writePredictionInterval = false;
		}

		predictionResultsPrimaryTable.setWritePredictionInterval(writePredictionInterval);

		String endpoint2 = endpoint.replace("50", "<sub>50</sub>");
		predictionResultsPrimaryTable.setEndpointSubscripted(endpoint2);

		// ************************************************
		// Header row

		if (er.expToxValue != -9999) {
			predictionResultsPrimaryTable.setExpCAS(er.expCAS);
			predictionResultsPrimaryTable.setSource(PredictToxicityWebPageCreator.getSourceTag(endpoint));
		}

		predictionResultsPrimaryTable.setExpSet(er.expSet);

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
			String predictedValue = "a";

			if (!message.equals("OK")) {
				predictedValue += ",b";
			}
			predictionResultsPrimaryTable.setPredictedValueSuperscript(predictedValue);
		} else {
			if (!message.equals("OK")) {
				predictionResultsPrimaryTable.setPredictedValueSuperscript("b");
			}
		}

		// ************************************************************
		// Molar units row
		if (pr.isLogMolarEndpoint()) {
			predictionResultsPrimaryTable.setMolarLogUnits(TESTConstants.getMolarLogUnits(endpoint));

			if (er.expToxValue == -9999) {
				predictionResultsPrimaryTable.setExpToxValue("N/A");
			} else {
				predictionResultsPrimaryTable.setExpToxValue(d2.format(er.expToxValue));
			}

			if (predToxVal == -9999) {
				predictionResultsPrimaryTable.setPredToxValue("N/A");
			} else {
				predictionResultsPrimaryTable.setPredToxValue(d2.format(predToxVal));
			}

			if (writePredictionInterval) {
				if (predToxVal == -9999) {
					predictionResultsPrimaryTable.setPredMinMaxVal("N/A");
				} else {
					double minval = predToxVal - predToxUnc; // molar value
					double maxval = predToxVal + predToxUnc; // molar value
					predictionResultsPrimaryTable.setPredMinMaxVal(d2.format(minval) + " &le; Tox &le; " + d2.format(maxval));
				}
			}

		}
		// ************************************************************
		// mass units row:

		predictionResultsPrimaryTable.setMassUnits(TESTConstants.getMassUnits(endpoint));
		predictionResultsPrimaryTable.setExpToxValMass(getToxValMass(pr.isLogMolarEndpoint(),ExpToxValMass));		
		predictionResultsPrimaryTable.setPredToxValMass(getToxValMass(pr.isLogMolarEndpoint(),PredToxValMass));


		if (writePredictionInterval) {
			if (predToxVal == -9999) {
				predictionResultsPrimaryTable.setPredMinMaxValMass("N/A");
			} else {
				double minval = predToxVal + predToxUnc; // molar value
				double maxval = predToxVal - predToxUnc; // molar value

				double minvalmass, maxvalmass;

				if (pr.isLogMolarEndpoint()) {
					minvalmass = PredictToxicityWebPageCreator.getToxValMass(endpoint, minval, MW);
					maxvalmass = PredictToxicityWebPageCreator.getToxValMass(endpoint, maxval, MW);
				} else {
					minvalmass = minval;
					maxvalmass = maxval;
				}

				if (maxvalmass < minvalmass) {// need for BCF
					double temp = minvalmass;
					minvalmass = maxvalmass;
					maxvalmass = temp;
				}

				if (Math.abs(maxvalmass) < 0.1 && pr.isLogMolarEndpoint()) {
					predictionResultsPrimaryTable.setPredMinMaxValMass(d2exp.format(minvalmass) + " &le; Tox &le; " + d2exp.format(maxvalmass));
				} else {
					predictionResultsPrimaryTable.setPredMinMaxValMass(d2.format(minvalmass) + " &le; Tox &le; " + d2.format(maxvalmass));
				}

			}
		}

		// ************************************************************

		if (er.expSet.equals("Training")) {
			String trainingExpSetText = "Note: the test chemical was present in the training set.";

			if (predToxVal != -9999) {
				if (method.equals(TESTConstants.ChoiceFDAMethod) || method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
					trainingExpSetText += "  However, the prediction <i>does</i> represent an external prediction.";
				} else {
					trainingExpSetText += "  The prediction <i>does not</i> represent an external prediction.";
				}
			}

			predictionResultsPrimaryTable.setPredictedValueNote(trainingExpSetText);
		} else if (er.expSet.equals("Test")) {
			predictionResultsPrimaryTable.setPredictedValueNote("Note: the test chemical was present in the external test set.");
		}

		// System.out.println(message);
		if (!message.equals("OK")) {
			predictionResultsPrimaryTable.setMessage(message);
		}
	}

	public static String getToxValMass(boolean isLogMolarEndpoint, Double val) {

		String strVal="";

		if (val.isNaN() || Math.abs(-9999 - val)<0.0001) {
			strVal="N/A";			
		} else {
			if (Math.abs(val) < 0.1 && isLogMolarEndpoint) {
				strVal=d2exp.format(val);
			} else {
				strVal=d2.format(val);
			}
		}
		return strVal;
	}


	/**
	 * 
	 * Writes a table of the overall results from each of the QSAR models used
	 * in the consensus prediction
	 *
	 * @param endpoint
	 * @param methods
	 * @param predictions
	 * @param uncertainties
	 * @param createDetailedConsensusReport
	 * @throws Exception
	 */
	private void writeIndividualPredictionsForConsensus(PredictionResults pr, ArrayList<String> methods,
			ArrayList<Double> predictions, ArrayList<Double> uncertainties, boolean createDetailedConsensusReport)
					throws Exception {

		IndividualPredictionsForConsensus individualPredictionsForConsensus = new IndividualPredictionsForConsensus();
		pr.setIndividualPredictionsForConsensus(individualPredictionsForConsensus);

		if (pr.isLogMolarEndpoint())
			individualPredictionsForConsensus.setUnits(TESTConstants.getMolarLogUnits(pr.getEndpoint()));
		else
			individualPredictionsForConsensus.setUnits(TESTConstants.getMassUnits(pr.getEndpoint()));

		for (int i = 0; i < methods.size(); i++) {
			if (methods.get(i).equals(TESTConstants.ChoiceConsensus))
				continue;


			String fileNameNoExtension=WebTEST4.getResultFileNameNoExtension(pr.getEndpoint(), methods.get(i), pr.getCAS());			
			String filename = fileNameNoExtension+".html";

			IndividualPredictionsForConsensus.PredictionIndividualMethod predIndMethod = new IndividualPredictionsForConsensus().new PredictionIndividualMethod();

			predIndMethod.setMethod(methods.get(i));

			double pred = predictions.get(i);

			if (createDetailedConsensusReport) {
				predIndMethod.setFileName(filename);
			}
			if (pred != -9999) {
				predIndMethod.setPrediction(d2.format(pred));
			} else {
				predIndMethod.setPrediction("N/A");
			}

			individualPredictionsForConsensus.getConsensusPredictions().add(predIndMethod);
		}

	}

	private void writeSimilarChemicals(PredictionResults pr, String set, 
			Hashtable<Double, Instance> ht, String abbrev,
			double expVal, double predVal, String CAS, String dtxcid, String dtxsid,
			ReportOptions options) throws Exception
	{

		if (ht == null)
			return;

		SimilarChemicals similarChemicals = new SimilarChemicals();

		Vector<Double> v = new Vector<Double>(ht.keySet());
		Collections.sort(v, new ToxPredictor.Utilities.MyComparator());

		Enumeration<Double> e = v.elements();

		int count = 0;
		while (e.hasMoreElements()) {
			double key = (Double) e.nextElement();
			if (key < PredictToxicityWebPageCreator.SCmin)
				break;
			count++;
		}

		e = v.elements();

		String predfilename=null;

		if (pr.getMethod().contentEquals(TESTConstants.ChoiceLDA))
			predfilename = abbrev + " " + set + " set predictions.txt";
		else
			predfilename = abbrev + "/" + abbrev + " " + set + " set predictions.txt";

		similarChemicals.setSimilarChemicalsCount(count);
		similarChemicals.setSimilarChemicalsSet(set);

		pr.getSimilarChemicals().add(similarChemicals);

		if (count == 0) {
			return;
		}

		// System.out.println(outputfolder);
		File of1 = new File(options.reportBase);
		String folder = of1.getParentFile().getParent();
		String strImageFolder = folder + File.separator + "Images";

		// Vectors to store results for calculations:
		Vector<Double> vecExp = new Vector<Double>();
		Vector<Double> vecPred = new Vector<Double>();
		Vector<Double> vecSC = new Vector<Double>();

		// Vectors to store results for display table:
		Vector<String> vecCAS2 = new Vector<String>();
		Vector<String> vecExp2 = new Vector<String>();// includes non
		// predicted ones
		Vector<String> vecPred2 = new Vector<String>();// includes non
		// predicted ones
		Vector<Double> vecSC2 = new Vector<Double>();// includes non
		// predicted ones

		int counter = 0;
		while (e.hasMoreElements()) {

			double key = (Double) e.nextElement();
			if (key < PredictToxicityWebPageCreator.SCmin)
				continue;
			counter++;

			Instance i = (Instance) ht.get(key);

			String CASi = i.getName();

			// System.out.println("Here1"+method+"\t"+CASi);

			// TaskCalculations.CreateStructureImage(CASi, strImageFolder);

			String strKey = d2.format(key);
			String expVali = d2.format(i.classValue());

			String predVali=null;

			if (pr.getMethod().contentEquals(TESTConstants.ChoiceLDA))
				predVali = lookup.LookUpValueInJarFile(predfilename, CASi, "ID", "Pred_Value:-Log10(mol/L)", "|");
			else {
				//Need to fix it so that consensus value in text file does not include FDA method in TEST5.1+:
				if (pr.getMethod().contentEquals(TESTConstants.ChoiceConsensus)) {
					predVali=lookup.LookUpValueConsensusValueOmitFDAInJarFile(predfilename, CASi, "CAS","\t");
				} else {
					predVali = lookup.LookUpValueInJarFile(predfilename, CASi, "CAS", pr.getMethod(), "\t");	
				}				
			}

			//				System.out.println(CASi+"\t"+predVali);


			double dpredVali;

			if (predVali.equals("N/A")) {
				dpredVali = -9999;
			} else {
				dpredVali = Double.parseDouble(predVali);
				predVali = d2.format(dpredVali);
			}

			if (dpredVali != -9999) {
				vecExp.add(i.classValue());
				vecPred.add(dpredVali);
				vecSC.add(new Double(key));
			} else {
				predVali = "N/A";
			}

			vecExp2.add(expVali);
			vecPred2.add(predVali);
			vecCAS2.add(CASi);
			vecSC2.add(key);

			if (counter == PredictToxicityWebPageCreator.maxSimilarCount)
				break;
		} // end loop over elements

		// ************************************************************
		// Calc stats:
		if (!pr.isBinaryEndpoint() && vecPred.size() > 0) {

			String set2 = set.substring(0, 1).toUpperCase() + set.substring(1);// capitalize
			// first
			// letter
			String chartname = "PredictionResults" + pr.getMethod() + "-Similar" + set2 + "SetChemicals.png";
			this.writeExternalPredChart(pr, vecExp, vecPred, vecSC,  predfilename, chartname, options, similarChemicals);
		} else if (pr.isBinaryEndpoint()) {
			this.calcCancerStats(0.5, vecExp, vecPred, similarChemicals);
		}

		//			logger.debug(expVal+"\t"+predVal);

		writeSimilarChemicalsTable(pr, expVal, predVal, CAS, dtxcid, dtxsid, d2, strImageFolder, vecCAS2, vecExp2, vecPred2, vecSC2, options, similarChemicals);



	}

	private String calcCancerStats(double cutoff, Vector<Double> vecExp, Vector<Double> vecPred, SimilarChemicals similarChemicals) {

		CancerStats cancerStats = new CancerStats();
		int predCount = 0;
		int posPredCount = 0;
		int negPredCount = 0;

		int correctCount = 0;
		int posCorrectCount = 0;
		int negCorrectCount = 0;

		for (int i = 0; i < vecExp.size(); i++) {
			double exp = vecExp.get(i);
			double pred = vecPred.get(i);

			String strExp = "";

			if (cutoff == 0.5) {
				if (exp >= cutoff)
					strExp = "C";
				else
					strExp = "NC";
			} else if (cutoff == 30) {
				if (exp == 0)
					strExp = "N/A";
				else if (exp >= cutoff)
					strExp = "C";
				else
					strExp = "NC";
			}

			String strPred = "";

			if (pred >= cutoff)
				strPred = "C";
			else
				strPred = "NC";

			// if (strExp.equals("C"))
			// System.out.println(exp+"\t"+pred+"\t"+strExp+"\t"+strPred);

			predCount++;
			if (strExp.equals("C"))
				posPredCount++;
			else if (strExp.equals("NC"))
				negPredCount++;

			if (strExp.equals(strPred)) {
				correctCount++;
				if (strExp.equals("C"))
					posCorrectCount++;
				else if (strExp.equals("NC"))
					negCorrectCount++;

			}
		}

		double concordance = correctCount / (double) predCount;
		double posConcordance = posCorrectCount / (double) posPredCount;
		double negConcordance = negCorrectCount / (double) negPredCount;


		if (predCount > 0) {
			cancerStats.setConcordance(d2.format(concordance));
			cancerStats.setCorrectCount(correctCount);
			cancerStats.setPredCount(predCount);
		} else {
			cancerStats.setConcordance("N/A");
		}

		if (posPredCount > 0) {
			cancerStats.setPosConcordance(d2.format(posConcordance));
			cancerStats.setPosCorrectCount(posCorrectCount);
			cancerStats.setPosPredCount(posPredCount);
		}
		else
			cancerStats.setPosConcordance("N/A");

		if (negPredCount > 0) {
			cancerStats.setNegConcordance(d2.format(negConcordance));
			cancerStats.setNegCorrectCount(negCorrectCount);
			cancerStats.setNegPredCount(negPredCount);
		}
		else
			cancerStats.setNegConcordance("N/A");

		similarChemicals.setCancerStats(cancerStats);

		return null;
	}

	private void writeSimilarChemicalsTable(PredictionResults pr, double expVal, double predVal, 
			String CAS, String dtxcid,String dtxsid, java.text.DecimalFormat df, String strImageFolder, Vector<String> vecCAS2,
			Vector<String> vecExp2, Vector<String> vecPred2, Vector<Double> vecSC2, ReportOptions options,
			SimilarChemicals similarChemicals) throws Exception   {



		// ***********************************************************
		// Write out table of exp and pred values for nearest chemicals:

		//		SimilarChemicals similarChemicals = predictionResults.getSimilarChemicals();

		String units;
		if (pr.isLogMolarEndpoint()) {
			units = TESTConstants.getMolarLogUnits(pr.getEndpoint());
		} else {
			units = TESTConstants.getMassUnits(pr.getEndpoint());
		}

		similarChemicals.setUnits(units);

		//use same URL as the one previously for test chemical:
		//		similarChemicals.setImageUrl(pr.getImageURL());

		//		if (gsid == null) {
		//			File outputFolder=new File(options.reportBase);
		//			File imageFile=new File(outputFolder.getParentFile().getAbsolutePath()+File.separator+"StructureData/structure.png");
		//			String strURL = imageFile.toURI().toURL().toString();
		//			similarChemicals.setImageUrl(ReportUtils.convertImageToBase64(strURL));
		//			
		////			similarChemicals.setImageUrl(ReportUtils.convertImageToBase64(ReportUtils.getImageSrc(options, "../StructureData/structure.png")));
		//		} else {// TODO- check if image exists online??? slow???
		//			//use Chemistry Dashboard image if available:
		//			similarChemicals.setImageUrl(PredictToxicityWebPageCreator.webPath + gsid);
		////			similarChemicals.setImageUrl(ReportUtils.convertImageToBase64(PredictToxicityWebPageCreator.webPath + gsid));
		//		}

		if (expVal == -9999.00)
			similarChemicals.setExpVal("N/A");
		else
			similarChemicals.setExpVal(df.format(expVal));

		if (predVal > -9999) {
			similarChemicals.setPredVal(df.format(predVal));
		} else {
			similarChemicals.setPredVal("N/A");
		}

		for (int i = 0; i < vecExp2.size(); i++) {
			SimilarChemical similarChemical = new SimilarChemical();
			String CASi = vecCAS2.get(i);
			String predVali = vecPred2.get(i);

			String cid_i = null;
			String DSSTOXSIDi = null;

			//			if (htChemistryDashboardInfo != null) {
			//				gsid_i = htChemistryDashboardInfo.get(CASi).gsid;
			//				DSSTOXSID = htChemistryDashboardInfo.get(CASi).dsstox_substance_id;
			//			}

			ArrayList<DSSToxRecord> records=ResolverDb2.lookupByCAS(CASi);

			if(records.size()>0) {
				DSSToxRecord record=records.get(0);
				cid_i = record.cid;
				DSSTOXSIDi = record.sid;
			}

			//			ChemistryDashboardRecord cdr=ChemistryDashboardRecord.lookupDashboardRecord("casrn", CASi,statNCCT_ID);
			//			if (cdr!=null) {
			//				gsid_i = cdr.gsid;
			//				DSSTOXSIDi = cdr.dsstox_substance_id;
			//			}
//msg=

			similarChemical.setDSSTOXCID(cid_i);
			similarChemical.setDSSTOXSID(DSSTOXSIDi);
			similarChemical.setCAS(CASi);

			if (cid_i == null || !WebTEST4.dashboardStructuresAvailable){
				// slow???
				
				CreateImageFromTrainingPredictionSDFs c=new CreateImageFromTrainingPredictionSDFs();
//				c.CreateStructureImage(CASi, strImageFolder,TESTConstants.getAbbrevEndpoint(pr.getEndpoint()));
				String url=c.CreateStructureImage2(CASi, strImageFolder,TESTConstants.getAbbrevEndpoint(pr.getEndpoint()));
				if (forGUI) {
//					similarChemical.setImageUrl("../../Images/"+CASi + ".png");
					similarChemical.setImageUrl(url);
				} else {
					File imageFile=new File(strImageFolder+File.separator+CASi + ".png");
					String strURL = imageFile.toURI().toURL().toString();
					similarChemical.setImageUrl(ReportUtils.convertImageToBase64(strURL));
				}

				//				similarChemical.setImageUrl(ReportUtils.convertImageToBase64(ReportUtils.getImageSrc(options, strImageFolder, CASi + ".png")));
				//				logger.debug("imgSrc old="+ReportUtils.getImageSrc(options, strImageFolder, CASi + ".png"));
				//				logger.debug("imgSrc todd="+strURL);

			} else {
				//use Chemistry Dashboard image if available:
				similarChemical.setImageUrl(PredictToxicityWebPageCreator.webPath + cid_i);
				//				similarChemical.setImageUrl(ReportUtils.convertImageToBase64(PredictToxicityWebPageCreator.webPath + gsid_i));
			}

			String strColor = PredictToxicityWebPageCreator.getColorString(vecSC2.get(i));
			similarChemical.setBackgroundColor(strColor);
			similarChemical.setSimilarityCoefficient(df.format(vecSC2.get(i)));
			similarChemical.setExpVal(vecExp2.get(i));
			similarChemical.setPredVal(vecPred2.get(i));

			similarChemicals.getSimilarChemicalsList().add(similarChemical);

		} // end loop over elements
		
		SimilarChemical testChemical=new SimilarChemical();
		similarChemicals.getSimilarChemicalsList().add(0,testChemical);
		Color color = Color.LIGHT_GRAY;
		
		String strColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		testChemical.setBackgroundColor(strColor);

		if (expVal > -9999)
			testChemical.setExpVal(d2.format(expVal));
		else 
			testChemical.setExpVal("N/A");
				
		if (predVal > -9999)
			testChemical.setPredVal(d2.format(predVal));
		else 
			testChemical.setPredVal("N/A");
		
		
		testChemical.setImageUrl(PredictToxicityWebPageCreator.webPath + dtxcid);
		testChemical.setSimilarityCoefficient(df.format(1.00));
		testChemical.setCAS(CAS+" (test chemical)");
		testChemical.setDSSTOXCID(dtxcid);
		testChemical.setDSSTOXSID(dtxsid);
		
//		System.out.println(testChemical.getImageUrl());
				

		//		predictionResults.setSimilarChemicals(similarChemicals);
	}

	
	
	private void writeExternalPredChart(PredictionResults pr,Vector<Double> vecExp, Vector<Double> vecPred, Vector<Double> vecSC, String predfilename,
			String chartname, ReportOptions options, SimilarChemicals similarChemicals) throws Exception {

		ExternalPredChart externalPredChart = new ExternalPredChart();

		// **************************************************************************************
		// since we can write encoded string to JSON we dont need to write image to file first:
		// String outputfolder2 = options.reportBase +
		// File.separator+"SimilarChemicals";
		//
		// File of = new File(outputfolder2);
		// if (!of.exists()) {
		// 		of.mkdir();
		// }
		//**************************************************************************************


		double[] x = new double[vecExp.size()];
		double[] y = new double[vecExp.size()];
		double[] SC = new double[vecExp.size()];

		double MAE = 0;

		for (int i = 0; i < vecExp.size(); i++) {
			x[i] = vecExp.get(i);
			y[i] = vecPred.get(i);
			SC[i] = vecSC.get(i);
			MAE += Math.abs(x[i] - y[i]);
		}
		MAE /= (double) x.length;

		externalPredChart.setMAE(MAE);

		double MAEEntireTestSet=-9999;

		if (pr.getMethod().contentEquals(TESTConstants.ChoiceLDA))
			MAEEntireTestSet = lookup.CalculateMAE(predfilename, "Exp_Value:-Log10(mol/L)", "Pred_Value:-Log10(mol/L)", "|");
		else
			MAEEntireTestSet = lookup.CalculateMAE(predfilename, "expToxicValue", pr.getMethod(), "\t");

		externalPredChart.setMAEEntireTestSet(MAEEntireTestSet);

		String title;
		if (pr.isLogMolarEndpoint()) {
			title = pr.getEndpoint() + " " + TESTConstants.getMolarLogUnits(pr.getEndpoint());
		} else {
			title = pr.getEndpoint() + " " + TESTConstants.getMassUnits(pr.getEndpoint());
		}

		String xtitle = "Exp. " + title;
		String ytitle = "Pred. " + title;

		// String charttitle = "Prediction results (redder = more similar)";
//		String charttitle = "Prediction results (colors defined in table below)";
		String charttitle = "Prediction results";

		JLabelChart fc = new fraChart.JLabelChart(x, y, SC, charttitle, xtitle, ytitle);
		fc.doDrawLegend = false;
		fc.doDrawStatsR2 = false;
		fc.doDrawStatsMAE = true;

		//		fc.WriteImageToFile(chartname, outputfolder2);
		//		URL imageFileURL = new File(ReportUtils.getImageSrc(options, "SimilarChemicals", chartname)).toURI().toURL();
		//		externalPredChart.setExternalPredChartImageSrc(ReportUtils.convertImageToBase64(imageFileURL.toString()));

		//Don't need write to file- save directly to the Json object:
		externalPredChart.setExternalPredChartImageSrc(fc.createImgURL());


		similarChemicals.setExternalPredChart(externalPredChart);
	}


	static String CreateQSARPlot(OptimalResults or, String endpoint) {

		double[] Yexp = or.getObserved();
		double[] Ycalc = or.getPredicted();

		// System.out.println(or.clusterNumber);
		// for (int i=0;i<or.observed.length;i++) {
		// System.out.println(i+"\t"+Yexp[i]+"\t"+Ycalc[i]);
		// }
		// System.out.println(filename);

		String title;

		if (TESTConstants.isLogMolar(endpoint)) {
			title = endpoint + " " + TESTConstants.getMolarLogUnits(endpoint);
		} else {
			title = endpoint + " " + TESTConstants.getMassUnits(endpoint);
		}

		String xtitle = "Exp. " + title;
		String ytitle = "Pred. " + title;
		fraChart.JLabelChart fc = new fraChart.JLabelChart(Yexp, Ycalc, xtitle, ytitle);

		fc.doDrawLegend = true;
		fc.doDrawStatsR2 = false;
		fc.doDrawStatsMAE = false;

		// fraChart fc=new fraChart(Yexp,Ycalc);
		return fc.createImgURL();

	}

	public static void WriteBinaryPredictionTable(PredictionResults pr, String dtxcid,Lookup.ExpRecord er, double predToxVal, String message) throws Exception {

		PredictionResultsPrimaryTable predictionResultsPrimaryTable = pr.getPredictionResultsPrimaryTable();
		predictionResultsPrimaryTable.setDtxcid(dtxcid);


		String endpoint=pr.getEndpoint();
		String method=pr.getMethod();


		double bound1 = 0.5;//cutoff for positive result

		// System.out.println(message);


		// ************************************************
		// Header row

		if (er.expToxValue != -9999) {
			predictionResultsPrimaryTable.setExpCAS(er.expCAS);
			predictionResultsPrimaryTable.setSource(PredictToxicityWebPageCreator.getSourceTag(endpoint));
		}

		predictionResultsPrimaryTable.setExpSet(er.expSet);

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
			String predictedValueSuperscript = "a";

			if (!message.equals("OK")) {
				predictedValueSuperscript += ",b";
			}
			predictionResultsPrimaryTable.setPredictedValueSuperscript(predictedValueSuperscript);
		} else {
			if (!message.equals("OK")) {
				predictionResultsPrimaryTable.setPredictedValueSuperscript("b");
			}
		}

		// ************************************************************
		// Value row

		if (er.expToxValue == -9999) {
			predictionResultsPrimaryTable.setExpToxValue("N/A");
		} else {
			predictionResultsPrimaryTable.setExpToxValue(d2.format(er.expToxValue));
		}

		if (predToxVal == -9999) {
			predictionResultsPrimaryTable.setPredToxValue("N/A");
		} else {
			predictionResultsPrimaryTable.setPredToxValue(d2.format(predToxVal));
		}


		// ************************************************************
		// result row:


		if (er.expToxValue == -9999) {
			predictionResultsPrimaryTable.setExpToxValueEndpoint("N/A");
		} else if (er.expToxValue < bound1) {
			// *add endpoint*
			if (endpoint.equals(TESTConstants.ChoiceReproTox))
				predictionResultsPrimaryTable.setExpToxValueEndpoint("Developmental NON-toxicant");
			else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
				predictionResultsPrimaryTable.setExpToxValueEndpoint("Mutagenicity Negative");
			else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
				predictionResultsPrimaryTable.setExpToxValueEndpoint("Does NOT bind to estrogen receptor");
		} else {
			// *add endpoint*
			if (endpoint.equals(TESTConstants.ChoiceReproTox))
				predictionResultsPrimaryTable.setExpToxValueEndpoint("Developmental toxicant");
			else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
				predictionResultsPrimaryTable.setExpToxValueEndpoint("Mutagenicity Positive");
			else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
				predictionResultsPrimaryTable.setExpToxValueEndpoint("Binds to estrogen receptor");
		}

		if (predToxVal == -9999) {
			predictionResultsPrimaryTable.setPredValueEndpoint("N/A");
		} else if (predToxVal < 0.5) {
			if (endpoint.equals(TESTConstants.ChoiceReproTox))
				predictionResultsPrimaryTable.setPredValueEndpoint("Developmental NON-toxicant");
			else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
				predictionResultsPrimaryTable.setPredValueEndpoint("Mutagenicity Negative");
			else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
				predictionResultsPrimaryTable.setPredValueEndpoint("Does NOT bind to estrogen receptor");

		} else {
			if (endpoint.equals(TESTConstants.ChoiceReproTox))
				predictionResultsPrimaryTable.setPredValueEndpoint("Developmental toxicant");
			else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
				predictionResultsPrimaryTable.setPredValueEndpoint("Mutagenicity Positive");
			else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
				predictionResultsPrimaryTable.setPredValueEndpoint("Binds to estrogen receptor");

		}

		// ************************************************************

		if (er.expSet.equals("Training")) {
			String trainingExpSetText = "Note: the test chemical was present in the training set.";

			if (method.equals(TESTConstants.ChoiceConsensus) || method.equals(TESTConstants.ChoiceHierarchicalMethod) || method.equals(TESTConstants.ChoiceGroupContributionMethod)
					|| method.equals(TESTConstants.ChoiceSingleModelMethod) || method.equals(TESTConstants.ChoiceRandomForrestCaesar)) {
				trainingExpSetText += "  The prediction " + "does not represent an external prediction.";
			} else {
				trainingExpSetText += "  However, the prediction does represent an external prediction.";
			}

			predictionResultsPrimaryTable.setPredictedValueNote(trainingExpSetText);
		} else if (er.expSet.equals("Test")) {
			predictionResultsPrimaryTable.setPredictedValueNote("Note: the test chemical was present in the external test set.");
		}

		// System.out.println(message);
		if (!message.equals("OK")) {
			predictionResultsPrimaryTable.setMessage(message);
		}

		pr.setPredictionResultsPrimaryTable(predictionResultsPrimaryTable);

	}


	private Hashtable<String,String> LoadDefinitions() {

		try {
			//
			Hashtable<String,String>htVarDef = new Hashtable<>();

			String file = "variable definitions.txt"; // need to go up and then
			// down into data folder
			InputStream ins = this.getClass().getClassLoader().getResourceAsStream(file);

			BufferedReader br = new BufferedReader(new InputStreamReader(ins));

			String Line = "12345";
			while (true) {
				Line = br.readLine();
				if (!(Line instanceof String))
					break;

				LinkedList<String> ll = Utilities.Parse(Line, "\t");

				String strvar = (String) ll.get(0);
				String strdef = (String) ll.get(1);

				htVarDef.put(strvar, strdef);

			}

			br.close();
			ins.close();

			return htVarDef;

		} catch (Exception ex) {
			logger.catching(ex);
			return null;
		}
	}


	private  void createDescriptorTable(TestChemical chemical,OptimalResults or, double predVal, String endpoint,ClusterModel clusterModel) {
		try {


			DecimalFormat df4 = new DecimalFormat("0.0000");

			Vector<Descriptor>descriptors=new Vector<>();
			clusterModel.setDescriptors(descriptors);



			double[] coeff = or.getBcoeff();
			double[] coeffSE = or.getBcoeffSE();

			int[] descriptorNumbers = or.getDescriptors();

			for (int i = 0; i < or.getDescriptorNames().length; i++) {

				Descriptor descriptor=new Descriptor();
				descriptors.add(descriptor);

				descriptor.setName(or.getDescriptorNames()[i]);
				descriptor.setDefinition(this.htVarDefs.get(descriptor.getName()));

				double descval = chemical.value(descriptorNumbers[i]);
				descriptor.setValue (df4.format(descval));
				descriptor.setCoefficient(df4.format(coeff[i]));
				descriptor.setCoefficientUncertainty(df4.format(coeffSE[i]));

				if (descval == 0) {
					descriptor.setValue_x_coefficient(d2.format(0));
				} else {
					descriptor.setValue_x_coefficient(d2.format(descval * coeff[i]));
				}
			}

			Descriptor descriptor=new Descriptor();
			descriptors.add(descriptor);
			descriptor.setName("Model intercept");
			descriptor.setValue ("1.0000");
			descriptor.setCoefficient(df4.format(coeff[coeff.length - 1]));
			descriptor.setCoefficientUncertainty(df4.format(coeffSE[coeff.length - 1]));
			descriptor.setValue_x_coefficient(df4.format(coeff[coeff.length - 1]));
			descriptor.setDefinition("Intercept of multilinear regression model");

			// ************************************************************************************************
			// Predicted value:

			String units;
			if (TESTConstants.isLogMolar(endpoint)) {
				units = TESTConstants.getMolarLogUnits(endpoint);
			} else {
				units = TESTConstants.getMassUnits(endpoint);
			}
			clusterModel.setPredictedValue(d2.format(predVal));
			clusterModel.setPredictedValueLabel("Predicted value " + units);

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	
	private ClusterModel createClusterModelLDA(PredictionResults pr,TestChemical chemical,String method,OptimalResults or,double predVal,double predUnc,
			String message,String MOA,String type) {
		ClusterModel clusterModel=new ClusterModel();
		
		if (pr.isCreateDetailedReport()) {
			clusterModel.setClusterID(MOA + " "+type);
			clusterModel.setUrl("../../ClusterFiles/" + MOA + " "+type+".html");
			clusterModel.setUrlDescriptors("ClusterFiles/Descriptors " + MOA + " "+type+".html");
		} 

		if (type.contentEquals("LC50")) {
			clusterModel.setBinary(false);
			clusterModel.setMinMaxValue(d2.format(predVal)+" &plusmn; " + d2.format(predUnc));
			if (predUnc == 0) {
				clusterModel.setOmitted(true);
			}
			clusterModel.setR2(d3.format(or.getR2()));
			clusterModel.setQ2(d3.format(or.getQ2()));

			if (pr.isCreateDetailedReport()) clusterModel.setPlotImage(CreateQSARPlot(or, pr.getEndpoint()));

		} else {
			clusterModel.setBinary(true);
			clusterModel.setMinMaxValue(d2.format(predVal));
			clusterModel.setConcordance(d3.format(or.getConcordance()));			
			clusterModel.setSensitivity(d3.format(or.getSensitivity()));
			clusterModel.setSpecificity(d3.format(or.getSpecificity()));
		}
		clusterModel.setNumChemicals(or.getNumChemicals());
		clusterModel.setMessageAD(message);

		String dependentVariable="";
		
		if (type.contentEquals("LC50")) dependentVariable=pr.getEndpoint();
		else dependentVariable="LDA Score "+MOA;
		
		clusterModel.setModelEquation(PredictToxicityWebPageCreator.GetModelEquation(or,dependentVariable));

		//		System.out.println(clusterModel.getModelEquation());

		if (pr.isCreateDetailedReport()) 
			createDescriptorTable(chemical,or, predVal, pr.getEndpoint(), clusterModel);


		return clusterModel;
	}

	private ClusterModel createClusterModel(PredictionResults pr,TestChemical chemical,String method,OptimalResults or,double predVal,double predUnc,String message) {
		ClusterModel clusterModel=new ClusterModel();


		if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
			clusterModel.setClusterID("FDA model");
		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
			clusterModel.setClusterID("Group Contribution");
		} else {
			clusterModel.setClusterID(or.getClusterNumber()+"");
		}

		if (pr.isCreateDetailedReport()) {
			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
				clusterModel.setUrl("ClusterFiles/PredictionResultsFDACluster.html");
				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsFDA.html");
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
				clusterModel.setUrl("../../ClusterFiles/" + pr.getEndpoint() + "/GroupContribution.html");
				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsGroupContribution.html");
			} else if (method.contentEquals(TESTConstants.ChoiceLDA)) {
				clusterModel.setUrl("../../ClusterFiles/" + pr.getEndpoint() + "/GroupContribution.html");
				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsGroupContribution.html");
				
			} else {
				clusterModel.setUrl("../../ClusterFiles/" + pr.getEndpoint() + "/" + or.getClusterNumber() + ".html");
				clusterModel.setUrlDescriptors("ClusterFiles/Descriptors" + or.getClusterNumber()+ ".html");
			} 
		} 

		if (!pr.isBinaryEndpoint()) {
			clusterModel.setBinary(false);
			clusterModel.setMinMaxValue(d2.format(predVal)+" &plusmn; " + d2.format(predUnc));
			if (predUnc == 0) {
				clusterModel.setOmitted(true);
			}
			clusterModel.setR2(d3.format(or.getR2()));
			clusterModel.setQ2(d3.format(or.getQ2()));

			if (pr.isCreateDetailedReport()) clusterModel.setPlotImage(CreateQSARPlot(or, pr.getEndpoint()));

		} else {
			clusterModel.setBinary(true);
			clusterModel.setMinMaxValue(d2.format(predVal));
			clusterModel.setConcordance(d3.format(or.getConcordance()));
			clusterModel.setSensitivity(d3.format(or.getSensitivity()));
			clusterModel.setSpecificity(d3.format(or.getSpecificity()));
		}
		clusterModel.setNumChemicals(or.getNumChemicals());
		clusterModel.setMessageAD(message);

		clusterModel.setModelEquation(PredictToxicityWebPageCreator.GetModelEquation(or,pr.getEndpoint()));

		//		System.out.println(clusterModel.getModelEquation());

		if (pr.isCreateDetailedReport()) 
			createDescriptorTable(chemical,or, predVal, pr.getEndpoint(), clusterModel);


		return clusterModel;
	}

	//	private ClusterModel createClusterModel(String method,DataForPredictionRun d,OptimalResults or) {
	//		ClusterModel clusterModel=new ClusterModel();
	//
	//		if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
	//			clusterModel.setClusterID("FDA model");
	//		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
	//			clusterModel.setClusterID("Group Contribution");
	//		} else {
	//			clusterModel.setClusterID(or.getClusterNumber()+"");
	//		}
	//
	//		if (d.createDetailedReport) {
	//			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
	//				clusterModel.setUrl("ClusterFiles/PredictionResultsFDACluster.html");
	//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsFDA.html");
	//			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
	//				clusterModel.setUrl("../../ClusterFiles/" + d.endpoint + "/GroupContributionCluster.html");
	//				clusterModel.setUrlDescriptors("ClusterFiles/DescriptorsGroupContribution.html");
	//			} else {
	//				clusterModel.setUrl("../../ClusterFiles/" + d.endpoint + "/" + or.getClusterNumber() + ".html");
	//				clusterModel.setUrlDescriptors("ClusterFiles/Descriptors" + or.getClusterNumber()+ ".html");
	//			}
	//		} 
	//				
	//		if (!d.isBinaryEndpoint) {		
	//			clusterModel.setR2(d3.format(or.getR2()));
	//			clusterModel.setQ2(d3.format(or.getQ2()));		
	//		} else {
	//			clusterModel.setConcordance(d3.format(or.getConcordance()));
	//			clusterModel.setSensitivity(d3.format(or.getSensitivity()));
	//			clusterModel.setSpecificity(d3.format(or.getSpecificity()));
	//		}
	//		clusterModel.setNumChemicals(or.getNumChemicals());
	//
	//		return clusterModel;
	//	}


	public PredictionResults generatePredictionResultsHierarchicalClustering(TestChemical chemical,DataForPredictionRun d,
			TESTPredictedValue tpv,double predToxVal, double predToxUnc,
			Vector<OptimalResults>resultsVector,Vector<OptimalResults>invalidResultsVector,Vector<Double>predictions,Vector<Double>uncertainties,
			Vector<Double>predictionsOutsideAD,Vector<Double>uncertaintiesOutsideAD,Vector<String>violationsAD,
			String method,ReportOptions options,boolean createReports) {

		try {

			PredictionResults pr=new PredictionResults();
			pr.setReportBase(options.reportBase);
			setCommonValues(method, d, tpv, pr, predToxVal, predToxUnc,createReports);

			if (createReports) {
				if (resultsVector.size() > 0) {
					pr.setClusterTable(createClusterTable(pr,chemical,resultsVector, predictions, uncertainties, null,method,"Cluster model predictions and statistics"));
				}

				if (invalidResultsVector.size() > 0) {
					pr.setInvalidClusterTable(createClusterTable(pr,chemical,invalidResultsVector, predictionsOutsideAD, uncertaintiesOutsideAD,violationsAD, method,"Cluster models with <font color=\"red\">applicability domain violation</font>"));
				}
			}

			return pr;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return null;
	}

	private ClusterTable createClusterTable(PredictionResults pr,TestChemical chemical, Vector<OptimalResults> resultsVector,
			Vector<Double> predictions, Vector<Double> uncertainties, Vector<String> violationAD,String method,String caption) {

		ClusterTable clusterTable=new ClusterTable();

		clusterTable.setCaption(caption);

		if (pr.isBinaryEndpoint() || pr.isLogMolarEndpoint()) {
			clusterTable.setUnits(TESTConstants.getMolarLogUnits(pr.getEndpoint()));
		} else {
			clusterTable.setUnits(TESTConstants.getMassUnits(pr.getEndpoint()));
		}

		Vector<ClusterModel>clusterModels=new Vector<>();
		clusterTable.setClusterModels(clusterModels);

		boolean HaveOmitted = false;


		
		for (int i = 0; i < resultsVector.size(); i++) {
			OptimalResults or = (OptimalResults) resultsVector.get(i);

			or.calculatePredictedValues();
			

			
			if (pr.isBinaryEndpoint()) or.CalculateCancerStats(0.5);

			//			if (d.isBinaryEndpoint)
			//				or.CalculateCancerStats(0.5);// TODO add cutoff as passed

			double predVal=predictions.get(i);
			double predUnc=uncertainties.get(i);

			String message;						
			if (violationAD==null) message="OK";
			else message=violationAD.get(i);

			ClusterModel clusterModel=createClusterModel(pr,chemical,method, or,predVal,predUnc,message);
			clusterModels.add(clusterModel);

		}

		if (HaveOmitted) 
			clusterTable.setMessage("*Value omitted from calculation of toxicity since prediction uncertainty was zero");

		if (clusterModels.size()==0) return null;

		return clusterTable;
	}


	public PredictionResults generatePredictionResultsLDA(DataForPredictionRun d,
			TESTPredictedValue tpv,double predToxVal, double predToxUnc,String predMOA,double maxScore,
			Vector<String>vecMOA,String[] predArrayMOA, String[] predArrayLC50,
			Hashtable<String, AllResults> htAllResultsMOA,Hashtable<String, AllResults> htAllResultsLC50,TestChemical chemical,ReportOptions options,boolean createReports) {
		// TODO Auto-generated method stub


		try {

			PredictionResults pr=new PredictionResults();

			PredictionResultsPrimaryTable prpt=pr.getPredictionResultsPrimaryTable();
			prpt.setExpMOA(d.er.expMOA);

			if (Strings.isBlank(predMOA) || maxScore<0.5)
				predMOA="N/A";
			
			prpt.setPredMOA(predMOA);
			prpt.setMaxScoreMOA(d2.format(maxScore));
						
			setCommonValues(TESTConstants.ChoiceLDA, d, tpv, pr, predToxVal, predToxUnc,createReports);
			
			pr.setReportBase(options.reportBase);
			
			
			
			writeSortedMOATable(pr, vecMOA, predArrayMOA, predArrayLC50, 
					predMOA, d.er,chemical,htAllResultsMOA,htAllResultsLC50);


			return pr;

		} catch (Exception ex) {
			logger.catching(ex);
			return null;
		}
	}

	private void writeSortedMOATable(PredictionResults pr,			
			Vector<String> vecMOA, String[] predArrayMOA, String[] predArrayLC50, 
			String bestMOA, Lookup.ExpRecord er,TestChemical chemical,
			Hashtable<String, AllResults> htAllResultsMOA,Hashtable<String, AllResults> htAllResultsLC50)
			throws IOException {


		MOATable mt=new MOATable();
		pr.setMoaTable(mt);

		Vector<MOAPrediction> MOAPredictions=new Vector<>();		
		mt.setMOAPredictions(MOAPredictions);


		Hashtable<Double, String> htMOA = new Hashtable<Double, String>();

		// System.out.println(vecMOA.size());

		for (int i = 0; i < vecMOA.size(); i++) {
			java.util.LinkedList<String> l_MOA = ToxPredictor.Utilities.Utilities.Parse(predArrayMOA[i], "\t");
			java.util.LinkedList<String> l_LC50 = ToxPredictor.Utilities.Utilities.Parse(predArrayLC50[i], "\t");

			double MOAScore = Double.parseDouble(l_MOA.get(0));

			// System.out.println(vecMOA.get(i)+"\t"+MOAScore);

			htMOA.put(MOAScore, vecMOA.get(i));
		}

		Vector v = new Vector(htMOA.keySet());
		Collections.sort(v, new ToxPredictor.Utilities.MyComparator());

		Vector<String> vecMOA2 = new Vector<String>();
		Enumeration e = v.elements();
		while (e.hasMoreElements()) {
			double key = (Double) e.nextElement();
			vecMOA2.add(htMOA.get(key));
			// System.out.println(key+"\t"+htMOA.get(key));
		}


		//		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		//
		//		fw.write("<caption>Results for each mode of action</caption>\r\n");
		//
		//		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		//		fw.write("<th>Mode of action</th>\n");
		//		fw.write("<th>LDA model score</th>\n");
		//		fw.write("<th>LC50 predicted value<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
		//		fw.write("</tr>\n");



		// System.out.println(vecMOA2.size());

		// System.out.println("*"+er.expMOA);

		for (int i = 0; i < vecMOA2.size(); i++) {
			String MOAi = vecMOA2.get(i);

			MOAPrediction mp=new MOAPrediction();
			MOAPredictions.add(mp);

			mp.setMOA(MOAi);


			String predsMOA = predArrayMOA[vecMOA.indexOf(MOAi)];
			String predsLC50 = predArrayLC50[vecMOA.indexOf(MOAi)];

			LinkedList<String> l_MOA = Utilities.Parse(predsMOA, "\t");
			LinkedList<String> l_LC50 = Utilities.Parse(predsLC50, "\t");

			double MOAScore = Double.parseDouble(l_MOA.get(0));
			double MOAUnc = Double.parseDouble(l_MOA.get(1));
			String MOAError = l_MOA.get(2);

			double LC50Score = Double.parseDouble(l_LC50.get(0));
			double LC50Unc = Double.parseDouble(l_LC50.get(1));
			String LC50Error = l_LC50.get(2);

			
			if (MOAi.equals(bestMOA) && MOAScore >= 0.5) {
				if (er.expMOA.equals(bestMOA)) {
					mp.setColor("#00CC33");//green
					mp.setTag("Experimental & predicted MOA");
					
				} else if (er.expMOA.equals("N/A") || er.expMOA.equals("")) {// experimental
					mp.setColor("#00CC33");//green
					mp.setTag("Predicted MOA");
				} else {// predicted doesnt match experimental MOA
					mp.setTag("Predicted MOA");
					mp.setColor("#FF9900");//orange
				}

			} else if (MOAi.equals(er.expMOA)) {
				mp.setColor("#00CC33");//green
				mp.setTag("Experimental MOA");
			} 
			
			mp.setMOAScore(d2.format(MOAScore));
			mp.setLC50Score(d2.format(LC50Score));
						
			if (!MOAError.equals("OK")) {
				mp.setMOAScoreMsg(MOAError);
			} else {
				if (MOAScore < 0.5) {
					mp.setMOAScoreMsg("score < 0.5");
				}
			}

			if (pr.isCreateDetailedReport()) {

				AllResults arLDA = htAllResultsMOA.get(MOAi);
				OptimalResults orLDA = arLDA.getResults().get(0);
				orLDA.calculatePredictedValues();
				ClusterModel clusterModelMOA=createClusterModelLDA(pr,chemical,pr.getMethod(), orLDA,MOAScore,MOAUnc,MOAError,MOAi,"LDA");
				mp.setClusterModelMOA(clusterModelMOA);
			
				AllResults arLC50 = htAllResultsLC50.get(MOAi);
				OptimalResults orLC50 = arLC50.getResults().get(0);
				orLC50.calculatePredictedValues();
				ClusterModel clusterModelLC50=createClusterModelLDA(pr,chemical,pr.getMethod(), orLC50,LC50Score,LC50Unc,LC50Error,MOAi,"LC50");
				mp.setClusterModelLC50(clusterModelLC50);

			}
			
			//fw.write("<a href=\"ClusterFiles/Descriptors " + MOAi + " LDA.html\">");
			//fw.write("<a href=\"ClusterFiles/Descriptors " + MOAi + " LC50.html\">");

			if (!LC50Error.equals("OK")) {
				mp.setLC50ScoreMsg(LC50Error);
			}
		}
	}


}



