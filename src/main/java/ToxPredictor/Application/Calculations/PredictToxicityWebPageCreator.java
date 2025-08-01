package ToxPredictor.Application.Calculations;


import QSAR.qsarOptimal.OptimalResults;
//import QSAR.validation2.Statistics;
import QSAR.validation2.TestChemical;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart.JLabelChart;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.Database.ChemistryDashboardRecord;
import ToxPredictor.Utilities.ReportUtils;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.ParseChemidplus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;

import wekalite.Instance;
import wekalite.Instances;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

//

/*
Outline of function calls for hierarchical, single model and group contribution:
-WriteHierarchicalResultsWebPages
	-WriteMainPage
		-WriteHeaderInfo
		-WriteMainResultsTable
			-WritePredictionResultsTable				
			-WriteClusterModelTable
			-WriteOverallModelStatistics
	-WriteClusterPage
		-CalculateClusterPredictedToxicities
		-WriteResultsWebPage
			-WriteHeaderInfo
			-WriteOverallModelTableForClusterPage
			-WritePlot
			-CreateStructureImages
			-WriteModelTable
			-WriteChemicalInfo
		-CreateQSARPlot
		
		
Outline of function calls for FDA method:
-WriteFDAResultsWebPages
	-WriteMainPage
		-WriteHeaderInfo
		-WriteMainResultsTable
			-WritePredictionResultsTable				
			-WriteClusterModelTable
			-WriteOverallModelStatistics
	-WriteResultsWebPage
		-WriteHeaderInfo
		-WriteOverallModelTableForClusterPage
		-WritePlot
		-CreateStructureImages
		-WriteModelTable
		-WriteChemicalInfo
		
*/

public class PredictToxicityWebPageCreator {

	private static final Logger logger = LogManager.getLogger(PredictToxicityWebPageCreator.class);

	static Hashtable<String,String> htVarDef;
	public static boolean isBinaryEndpoint = false;
	public static boolean isLogMolarEndpoint = true;
//	private Statistics statistics = new Statistics();

//	private PredictionResults predictionResults = new PredictionResults();
	
	// int maxSimilarCount =9999;//max similar chemicals displayed in results
	public static final int maxSimilarCount = 10;// max similar chemicals displayed in results

	// double SCmin=0.75;
	public static final double SCmin = 0.5;
	// double SCmin=0.0;

	public static int imgSize = 100;// img size displayed in web pages

	public static String messageMissingFragments = "A prediction could not be made " + "because the test chemical contains atoms which could not be assigned to "
			+ "<a href=\"../StructureData/AssignedFragments.html\">fragments</a>.";

	public static String messageNoStatisticallyValidModels = "<font color=darkred>(none of the closest clusters have statistically valid models)</font>\r\n";

	Lookup lookup = new Lookup();

	public static String webImagePathByCID = "https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";//image link
	public static String webImagePathBySID = "https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxsid/";//image link

//	public static String webPathDashboardPage = "https://comptox.epa.gov/dashboard/dsstoxdb/results?search=";//dashboard page for the chemical
	public static String webPathDashboardPage = "https://comptox.epa.gov/dashboard/chemical/details/";//dashboard page for the chemical
	
	
	
	static void WriteHeaderInfo(FileWriter fw, String CAS, String endpoint, String method) throws IOException {

		fw.write("<html>\n");
		fw.write("<head>\n");

		// fw.write("<title>Prediction results from the "+method+" method\n");
		fw.write("<title>Predicted " + endpoint + " for " + CAS + " from " + method + " method");
		fw.write("</title>\n");
		fw.write("</head>\n");

	}

	public PredictToxicityWebPageCreator() {
		this.LoadDefinitions();
	}

	private void LoadDefinitions() {

		try {
			//
			htVarDef = new Hashtable<>();

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
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	

	

	

	/**
	 * 
	 * @param method
	 * @param chemical
	 * @param OutputFolder
	 * @param CAS
	 * @param endpoint
	 * @param expToxVal
	 *            - experimental toxicity for database match for test chemical
	 * @param expCAS
	 *            - CAS number for database match for test chemical
	 * @param resultsVector
	 * @param MW
	 * @param inTrainingSet
	 */
	public void WriteHierarchicalResultsWebPages(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, Lookup.ExpRecord er, Vector<OptimalResults> resultsVector, Vector<OptimalResults> invalidResultsVector, double MW, String message, Hashtable<Double, Instance> htTestMatch,
			Hashtable<Double, Instance> htTrainMatch, long minFileAgeHours, ReportOptions options) {

		// System.out.println(message);

		try {
			this.isBinaryEndpoint = isBinaryEndpoint;
			this.isLogMolarEndpoint = isLogMolarEndpoint;

			for (int i = 0; i < resultsVector.size(); i++) {
				OptimalResults or = (OptimalResults) resultsVector.get(i);
				or.calculatePredictedValues();
				if (isBinaryEndpoint)
					or.CalculateCancerStats(0.5);// TODO add cutoff as passed
													// variable to this method
			}

			for (int i = 0; i < invalidResultsVector.size(); i++) {// vector of
																	// OptimalResults
																	// models
																	// that had
																	// their
																	// constraints
																	// violated
																	// or had
																	// invalid
																	// model
				OptimalResults or = (OptimalResults) invalidResultsVector.get(i);
				or.calculatePredictedValues();
				if (isBinaryEndpoint)
					or.CalculateCancerStats(0.5);// TODO add cutoff as passed
													// variable to this method
			}

			this.WriteMainPage(method, chemical, OutputFolder, CAS, endpoint, abbrev, er, resultsVector, invalidResultsVector, MW, message, htTestMatch, htTrainMatch, options);

			// if (expCAS != null) {
			// this.WriteExperimentalValuePage(OutputFolder, expCAS, endpoint);
			// }

			for (int i = 0; i < resultsVector.size(); i++) {
				OptimalResults or = (OptimalResults) resultsVector.get(i);
				this.WriteClusterPage(method, chemical, OutputFolder, CAS, endpoint, er.expCAS, or, minFileAgeHours, options);
			}

			// vector of OptimalResults models that had their constraints violated or had invalid model:
			for (int i = 0; i < invalidResultsVector.size(); i++) {
				OptimalResults or = (OptimalResults) invalidResultsVector.get(i);
				if (or.isValid())
					this.WriteClusterPage(method, chemical, OutputFolder, CAS, endpoint, er.expCAS, or, minFileAgeHours, options);
			}

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	public String WriteConsensusResultsWebPages(double predToxVal, double predToxUnc, String method, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, Lookup.ExpRecord er, double MW, String message, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, ArrayList <String>methods,
			ArrayList<Double> predictions, ArrayList<Double> uncertainties, boolean createDetailedConsensusReport, String dtxcid, Hashtable<String, ChemistryDashboardRecord> htChemistryDashboardInfo,
			ReportOptions options) {
		try {

			this.isBinaryEndpoint = isBinaryEndpoint;
			this.isLogMolarEndpoint = isLogMolarEndpoint;

			String outputfilename = "PredictionResults";

			outputfilename += method.replaceAll(" ", "");
			outputfilename += ".html";

			String htmlPath = Paths.get(OutputFolder, outputfilename).toFile().getAbsolutePath();
			FileWriter fw = new FileWriter(htmlPath);

			WriteHeaderInfo(fw, CAS, endpoint, method);

			fw.write("<h2>Predicted " + endpoint + " for <font color=\"blue\">" + CAS + "</font> from " + method + " method</h2>\n");

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

			// fw.write("<table><tr><td>\r\n");
			// delimited descriptor file

			if (isBinaryEndpoint) {
				this.WriteBinaryPredictionTable(fw, CAS, endpoint, method, er, predToxVal, MW, message);
			} else {
				// this.WriteMainConsensusTable(fw, CAS, predToxVal,predToxUnc,
				// endpoint, method, MW, er, message,false);
				this.WriteMainTable(fw, CAS, predToxVal, predToxUnc, endpoint, method, MW, er, message);
				// this.WritePredictionResultsTable2(method,fw, endpoint,
				// er,chemical, MW,message);
				// TODO: omit WriteMainConsensusTable and use
				// this.WritePredictionResultsTable2?
			}

			// fw.write("</td>\r\n");
			// fw.write("<td><img src=\"../structuredata/structure.png\"
			// height="+imgSize+" width="+imgSize+"></td>\r\n");
			// fw.write("</tr></table><br>\r\n");

			// if (predCount==1) {
			// fw.write("<br><font color=darkred>Note: the consensus prediction
			// for this chemical is considered unreliable since only one
			// prediction can only be made.</font>");
			// }

			fw.write("<BR><BR>\r\n");

			fw.write("\r\n<table border=1 cellpadding=10 cellspacing=0>\r\n");
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");
			this.WriteIndividualPredictionsForConsensus(fw, endpoint, methods, predictions, uncertainties, createDetailedConsensusReport);
			fw.write("</td>\r\n");

			if (dtxcid == null) {
				fw.write("<td><a href=\"../StructureData/structure.png\"><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + imgSize
						+ " border=0></a></td>\n");
			} else {// TODO- check if image exists online??? slow???
				fw.write("<td><a href=\"" + webImagePathByCID + dtxcid + "\"><img src=\"" + webImagePathByCID + dtxcid + "\" width=" + imgSize + " border=0></a></td>\n");
			}

			// fw.write("<td align=middle>Test chemical<br><img
			// src=\"../StructureData/structure.png\" width=" + imgSize
			// + "></td>\r\n");

			fw.write("</tr>\r\n");
			fw.write("</table>\r\n\r\n");

			if (createDetailedConsensusReport) {
				fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for " + "test chemical</a></p>\n");
			}
			fw.write("<br><hr>\n");

			this.WriteSimilarChemicals("test", htTestMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, htChemistryDashboardInfo, dtxcid, options);
			fw.write("<br><hr>\n");
			this.WriteSimilarChemicals("training", htTrainMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, htChemistryDashboardInfo, dtxcid, options);

			fw.close();

			return htmlPath;

		} catch (Exception ex) {
			logger.catching(ex);
		}

		return null;
	}

	public String WriteConsensusResultsWebPages(double predToxVal, double predToxUnc, String method, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, Lookup.ExpRecord er, double MW, String message, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, ArrayList methods,
			ArrayList predictions, ArrayList uncertainties, ReportOptions options) {

		return this.WriteConsensusResultsWebPages(predToxVal, predToxUnc, method, OutputFolder, CAS, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, MW, message, htTestMatch, htTrainMatch,
				methods, predictions, uncertainties, true, null, null, options);
	}

	// private void WriteMainConsensusTable(FileWriter fw,String CAS,double
	// predToxVal,double predToxUnc,String endpoint,String method,double
	// MW,Lookup.ExpRecord er,String message,boolean writePredictionInterval)
	// throws Exception {
	//
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.00");
	// java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00E00");
	//
	// double ExpToxValMass=-9999;
	// double PredToxValMass=-9999;
	//
	// if (predToxVal!=-9999) {
	// PredToxValMass=this.getToxValMass(endpoint,predToxVal,MW);
	// }
	//
	// if (er.expToxValue!=-9999) {
	// ExpToxValMass=this.getToxValMass(endpoint,er.expToxValue,MW);
	// }
	//
	//
	// String endpoint2=endpoint.replace("50","<sub>50</sub>");
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	//
	// fw.write("<caption>Prediction results</caption>\r\n");
	//
	// // ************************************************
	// // Header row
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	//
	// fw.write("<th>Endpoint</th>\n");
	//
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<th>Experimental value</th>\n");
	// } else {
	// fw.write("<th>Experimental value<br>CAS: "+er.expCAS);
	//
	// fw.write(this.getSource(endpoint));
	//
	// fw.write("</th>\n");
	// }
	//
	// if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
	//
	// fw.write("<th>Predicted value");
	// fw.write("<sup>a</sup>");
	//
	// if (!message.equals("OK")) {
	// fw.write("<sup>,b</sup>");
	// }
	//
	// fw.write("</th>\n");
	//
	// } else {
	// if (!message.equals("OK")) {
	// fw.write("<th>Predicted value<sup>b</sup></th>\n");
	// } else {
	// fw.write("<th>Predicted value</th>\n");
	// }
	// }
	//
	// if (writePredictionInterval) fw.write("<th>Prediction interval</th>\n");
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//// Molar units row
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>"+endpoint2+" "+getMolarLogUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");
	// } else {
	// fw.write("<td align=\"center\">"+d.format(er.expToxValue)+"</td>\n");
	// //molar
	// }
	//
	// if (predToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// fw.write("<td align=\"center\">"+d.format(predToxVal)+"</td>\n");//molar
	// }
	//
	// if (writePredictionInterval) {
	// if (predToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// double minval=predToxVal-predToxUnc; //molar value
	// double maxval=predToxVal+predToxUnc; //molar value
	// fw.write("<td align=\"center\">"+d.format(minval)+" &le; Tox &le;
	// "+d.format(maxval)+"</td>\n");//molar
	// }
	// }
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	// // mass units row:
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>"+endpoint2+" "+getMassUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");//mass
	// } else {
	// if (ExpToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(ExpToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(ExpToxValMass)+"</td>\n");
	// //mass
	// }
	// }
	//
	// if (predToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// } else {
	// if (PredToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(PredToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxValMass)+"</td>\n");
	// //mass
	// }
	// }
	//
	// if (writePredictionInterval) {
	// if (predToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// double minval=predToxVal+predToxUnc; //molar value
	// double maxval=predToxVal-predToxUnc; //molar value
	//
	// double minvalmass=this.getToxValMass(endpoint, minval, MW);
	// double maxvalmass=this.getToxValMass(endpoint, maxval, MW);
	//
	// if (maxvalmass<minvalmass) {//need for BCF
	// double temp=minvalmass;
	// minvalmass=maxvalmass;
	// maxvalmass=temp;
	// }
	//
	// if (maxvalmass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(minvalmass)+" &le; Tox &le;
	// "+d2.format(maxvalmass)+"</td>\n");//mass, need to reverse values since
	// less toxic = larger value
	// } else {
	// fw.write("<td align=\"center\">"+d.format(minvalmass)+" &le; Tox &le;
	// "+d.format(maxvalmass)+"</td>\n");//mass, need to reverse values since
	// less toxic = larger value
	// }
	//
	// }
	// }
	//
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//
	// fw.write("</table>");
	//
	// if (er.expSet.equals("Training")) {
	// fw.write("<sup>a</sup><font color=purple>Note: the test chemical was
	// present in the training set.");
	// fw.write(" The prediction does not represent an external prediction.");
	//
	// fw.write("</font>\r\n");
	//
	// } else if (er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup><font color=blue>Note: the test chemical was
	// present in the external test set.</font><br>\r\n");
	// }
	// if (!message.equals("OK")) {
	// fw.write("<sup>b</sup><font color=darkred>" +message+"</font>\r\n");
	// }
	// }

//	private void WriteSimilarChemicalsInTrainingSetOld(Hashtable<Double, Instance> ht, FileWriter fw, String endpoint, String abbrev, String CAS, int chemicalNameIndex, double expVal, double predVal,
//			String outputfolder, String method, ReportOptions options) {
//
//		Lookup lookup = new Lookup();
//
//		try {
//			if (ht == null)
//				return;
//
//			Vector v = new Vector(ht.keySet());
//			java.util.Collections.sort(v, new ToxPredictor.Utilities.MyComparator());
//
//			Enumeration e = v.elements();
//
//			int count = 0;
//			while (e.hasMoreElements()) {
//				double key = (Double) e.nextElement();
//				if (key < SCmin)
//					continue;
//				count++;
//			}
//
//			e = v.elements();
//
//			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
//
//			fw.write("<h2>Similar chemicals from the " + "<font color=blue>training set</font></h2>\n");
//
//			if (count == 0) {
//				fw.write("<i>Note: No chemicals in the training set exceed a minimum similarity coefficient of " + SCmin + " for comparison purposes</i>\r\n");
//				return;
//			}
//			fw.write("<h3>If there are very similar chemicals " + "in the training set (with similar experimental values),<br> " + "one has greater confidence " + "in the predicted value.</h3>\n");
//
//			fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
//
//			/////////////////////////////////////////////////////////////////
//			// Header
//			fw.write("\n<tr bgcolor=\"#D3D3D3\">\n");
//			fw.write("<th>CAS</th>\n");
//			fw.write("<th>Structure</th>\n");
//			fw.write("<th>Similarity<br>Coefficient</th>\n");
//
//			String units;
//			if (isLogMolarEndpoint) {
//				units = TESTConstants.getMolarLogUnits(endpoint);
//			} else {
//				units = TESTConstants.getMassUnits(endpoint);
//			}
//
//			fw.write("<th>Experimental value<br>" + units + "</th>\n");
//			fw.write("</tr>\n\n");
//
//			/////////////////////////////////////////////////////////////////
//			// Values for test chemical:
//			fw.write("<tr>\n");
//
//			// String CAS = chemical.stringValue(chemicalNameIndex);
//			fw.write("<td><font color=\"blue\">" + CAS + "<br>(test chemical)</font></td>\n");
//
//			fw.write("<td><a href=\"../StructureData/structure.png\"><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + imgSize
//					+ " border=0></a></td>\n");
//			fw.write("<td align=\"center\"><br></td>\n");
//
//			if (expVal == -9999.00)
//				fw.write("<td align=\"center\">N/A</td>\n");
//			else
//				fw.write("<td align=\"center\">" + df.format(expVal) + "</td>\n");
//
//			fw.write("</tr>\n\n");
//
//			/////////////////////////////////////////////////////////////////
//			// Values for similar compounds:
//
//			File of1 = new File(outputfolder);
//			String folder = of1.getParentFile().getParent();
//			String strImageFolder = folder + File.separator + "Images";
//
//			Vector<Double> exp = new Vector<Double>();
//			Vector<Double> pred = new Vector<Double>();
//
//			int counter = 0;
//
//			while (e.hasMoreElements()) {
//
//				double key = (Double) e.nextElement();
//
//				if (key < SCmin)
//					continue;
//
//				counter++;
//
//				Instance i = (Instance) ht.get(key);
//
//				String CASi = i.getName();
//
//				TaskCalculations.CreateStructureImage(CASi, strImageFolder);
//
//				String strKey = df.format(key);
//				String expVali = df.format(i.classValue());
//
//				// System.out.println(CAS+"\t"+strKey+"\t"+expVal+"\t"+predVal);
//
//				fw.write("<tr>\n");
//				fw.write("<td>" + CASi + "</td>\n");
//				fw.write("<td><a href=\"../../images/" + CASi + ".png\"><img src=\"" + ReportUtils.getImageSrc(options, "../../images", CASi + ".png") + "\" width=" + imgSize
//						+ " border=0></a></td>\n");
//				fw.write("<td align=\"center\">" + strKey + "</td>\n");
//				fw.write("<td align=\"center\">" + expVali + "</td>\n");
//
//				fw.write("</tr>\n\n");
//
//				if (counter == maxSimilarCount)
//					break;
//			} // end loop over elements
//
//			// System.out.println("");
//			fw.write("</table>\n");
//
//		} catch (Exception ex) {
//			logger.catching(ex);
//		}
//	}

	public static Color getColor(double SCi) {

		Color color = null;

		
		if (SCi ==1.0) {
			color = Color.LIGHT_GRAY;
		} else if (SCi >= 0.9) {
			color = Color.green;
		} else if (SCi < 0.9 && SCi >= 0.8) {
			// color=Color.blue;
			color = new Color(100, 100, 255);// light blue
		} else if (SCi < 0.8 && SCi >= 0.7) {
			color = Color.yellow;
		} else if (SCi < 0.7 && SCi >= 0.6) {
			color = Color.orange;
		} else if (SCi < 0.6) {
			// color=Color.red;//255,153,153
			color = new Color(255, 100, 100);// light red
		}

		if (color == null)
			System.out.println("null color for " + SCi);
		// System.out.println(SCi+"\t"+color.getRGB());
		return color;
	}

	public static String getColorString(double SC) {
		Color color = getColor(SC);
		String strColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		return strColor;
	}

	// public static String getColor(String SC) {
	// double SCi=Double.parseDouble(SC);
	// Color color=getColor(SCi);
	// String strColor = String.format("#%02x%02x%02x",
	// color.getRed(),color.getGreen(), color.getBlue());
	// return strColor;
	// }

	

	private void WriteSimilarChemicals(String set, Hashtable<Double, Instance> ht, FileWriter fw, String endpoint, String abbrev, String CAS, double expVal, double predVal, String outputfolder,
			String method, Hashtable<String, ChemistryDashboardRecord> htChemistryDashboardInfo, String gsid, ReportOptions options) {

		try {

			if (ht == null)
				return;

			Vector v = new Vector(ht.keySet());
			java.util.Collections.sort(v, new ToxPredictor.Utilities.MyComparator());

			Enumeration e = v.elements();

			int count = 0;
			while (e.hasMoreElements()) {
				double key = (Double) e.nextElement();
				if (key < SCmin)
					break;
				count++;
			}

			e = v.elements();

			String predfilename = abbrev + "/" + abbrev + " " + set + " set predictions.txt";

			if (set.equals("test")) {
				fw.write("<h2>Predictions for the test chemical and for the " + "most similar chemicals in the " + "<font color=blue>external test set</font></h2>\n");
			} else {
				fw.write("<h2>Predictions for the test chemical and for the " + "most similar chemicals in the " + "<font color=blue>" + set + " set</font></h2>\n");
			}

			if (count == 0) {
				fw.write("<i>Note: No chemicals in the " + set + " set exceed a minimum similarity coefficient of " + SCmin + " for comparison purposes</i>\r\n");
				return;
			}

			fw.write("<h3>If the predicted value matches the experimental values for similar chemicals " + "in the " + set + " set (and the similar chemicals were predicted well), "
					+ "one has greater confidence in the predicted value.</h3>\n");

			// System.out.println(outputfolder);
			File of1 = new File(outputfolder);
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

			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			int counter = 0;
			while (e.hasMoreElements()) {

				double key = (Double) e.nextElement();
				if (key < SCmin)
					continue;
				counter++;

				Instance i = (Instance) ht.get(key);

				String CASi = i.getName();

				// System.out.println("Here1"+method+"\t"+CASi);

				// TaskCalculations.CreateStructureImage(CASi, strImageFolder);

				String strKey = df.format(key);
				String expVali = df.format(i.classValue());
				String predVali = lookup.LookUpValueInJarFile(predfilename, CASi, "CAS", method, "\t");

				double dpredVali;

				if (predVali.equals("N/A")) {
					dpredVali = -9999;
				} else {
					dpredVali = Double.parseDouble(predVali);
					predVali = df.format(dpredVali);
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

				if (counter == maxSimilarCount)
					break;
			} // end loop over elements

			// ************************************************************
			// Calc stats:
			if (!isBinaryEndpoint && vecPred.size() > 0) {

				String set2 = set.substring(0, 1).toUpperCase() + set.substring(1);// capitalize
																					// first
																					// letter
				String chartname = "PredictionResults" + method + "-Similar" + set2 + "SetChemicals.png";
				this.writeExternalPredChart(endpoint, method, outputfolder, vecExp, vecPred, vecSC, fw, predfilename, chartname, options);
			} else if (isBinaryEndpoint) {
				String cancerstats = calcCancerStats(0.5, vecExp, vecPred);
				fw.write(cancerstats + "\r\n");
			}

			WriteSimilarChemicalsTable(fw, endpoint, CAS, expVal, predVal, htChemistryDashboardInfo, gsid, df, strImageFolder, vecCAS2, vecExp2, vecPred2, vecSC2, options);

		} catch (Exception ex) {
			logger.catching(ex);
		}

	}

	private void WriteSimilarChemicalsTable(FileWriter fw, String endpoint, String CAS, double expVal, double predVal, Hashtable<String, ChemistryDashboardRecord> htChemistryDashboardInfo,
			String gsid, java.text.DecimalFormat df, String strImageFolder, Vector<String> vecCAS2, Vector<String> vecExp2, Vector<String> vecPred2, Vector<Double> vecSC2, ReportOptions options)
			throws IOException {
		// ***********************************************************
		// Write out table of exp and pred values for nearest chemicals:
		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");

		fw.write("\n<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>CAS</th>\n");
		fw.write("<th>Structure</th>\n");
		fw.write("<th>Similarity<br>Coefficient</th>\n");

		String units;
		if (isLogMolarEndpoint) {
			units = TESTConstants.getMolarLogUnits(endpoint);
		} else {
			units = TESTConstants.getMassUnits(endpoint);
		}

		fw.write("<th>Experimental value<br>" + units + "</th>\n");
		fw.write("<th>Predicted value<br>" + units + "</th>\n");
		fw.write("</tr>\n\n");

		fw.write("<tr>\n");

		// String CAS = chemical.stringValue(chemicalNameIndex);
		fw.write("<td><font color=\"blue\">" + CAS + "<br>(test chemical)</font></td>\n");

		if (gsid == null) {
			fw.write("<td><a href=\"../StructureData/structure.png\"><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + imgSize
					+ " border=0></a></td>\n");
		} else {// TODO- check if image exists online??? slow???
			fw.write("<td><a href=\"" + webImagePathByCID + gsid + "\"><img src=\"" + webImagePathByCID + gsid + "\" width=" + imgSize + " border=0></a></td>\n");//TODO fix to use CID
		}

		fw.write("<td align=\"center\"><br></td>\n");

		if (expVal == -9999.00)
			fw.write("<td align=\"center\">N/A</td>\n");
		else
			fw.write("<td align=\"center\">" + df.format(expVal) + "</td>\n");

		if (predVal > -9999) {
			fw.write("<td align=\"center\">" + df.format(predVal) + "</td>\n");
		} else {
			fw.write("<td align=\"center\">N/A</td>\n");
		}

		fw.write("</tr>\n\n");

		for (int i = 0; i < vecExp2.size(); i++) {
			String CASi = vecCAS2.get(i);
			String predVali = vecPred2.get(i);

			String gsid_i = null;
			String DSSTOXSID = null;

			if (htChemistryDashboardInfo != null && htChemistryDashboardInfo.get(CASi) != null) {
				gsid_i = htChemistryDashboardInfo.get(CASi).gsid;
				DSSTOXSID = htChemistryDashboardInfo.get(CASi).dsstox_substance_id;
			}

			fw.write("<tr>\n");

			if (DSSTOXSID == null) {
				fw.write("<td>" + CASi + "</td>\n");
			} else {
				fw.write("<td><a href=\"" + webPathDashboardPage + DSSTOXSID + "\" target=\"_blank\">" + CASi + "</td>\n");// TODD
			}

			if (gsid_i == null) {// TODO- check if image exists online???
									// slow???
//				TaskCalculations.CreateStructureImage(CASi, strImageFolder);
				
				CreateImageFromTrainingPredictionSDFs c=new CreateImageFromTrainingPredictionSDFs();
				c.CreateStructureImage(CASi, strImageFolder,TESTConstants.getAbbrevEndpoint(endpoint));

				
				fw.write("<td><a href=\"../../images/" + CASi + ".png\"><img src=\"" + ReportUtils.getImageSrc(options, "../../images", CASi + ".png") + "\" width=" + imgSize
						+ " border=0></a></td>\n");
			} else {
				fw.write("<td><a href=\"" + webImagePathByCID + gsid_i + "\"><img src=\"" + webImagePathByCID + gsid_i + "\" width=" + imgSize + " border=0></a></td>\n");//TODO fix to use CID
			}

			String strColor = getColorString(vecSC2.get(i));
			fw.write("<td bgcolor=" + "\"" + strColor + "\"" + " align=\"center\">" + df.format(vecSC2.get(i)) + "</td>\n");

			fw.write("<td align=\"center\">" + vecExp2.get(i) + "</td>\n");
			fw.write("<td align=\"center\">" + vecPred2.get(i) + "</td>\n");
			fw.write("</tr>\n\n");

		} // end loop over elements

		fw.write("</table>\n");
	}

	

	void writeExternalPredChart(String endpoint, String method, String outputfolder, Vector<Double> vecExp, Vector<Double> vecPred, Vector<Double> vecSC, FileWriter fw, String predfilename,
			String chartname, ReportOptions options) throws Exception {

		String outputfolder2 = outputfolder + "/SimilarChemicals";

		File of = new File(outputfolder2);
		if (!of.exists()) {
			of.mkdir();
		}

		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

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

		String title;
		if (isLogMolarEndpoint) {
			title = endpoint + " " + TESTConstants.getMolarLogUnits(endpoint);
		} else {
			title = endpoint + " " + TESTConstants.getMassUnits(endpoint);
		}

		String xtitle = "Exp. " + title;
		String ytitle = "Pred. " + title;

		// String charttitle = "Prediction results (redder = more similar)";
		String charttitle = "Prediction results (colors defined in table below)";

		fraChart.JLabelChart fc = new fraChart.JLabelChart(x, y, SC, charttitle, xtitle, ytitle);
		fc.doDrawLegend = false;
		fc.doDrawStatsR2 = false;
		fc.doDrawStatsMAE = true;

		fc.WriteImageToFile(chartname, outputfolder2);

		double MAEEntireTestSet = lookup.CalculateMAE(predfilename, "expToxicValue", method, "\t");

		fw.write("<table><tr>\n");

		fw.write("<td><img src=\"" + ReportUtils.getImageSrc(options, "SimilarChemicals", chartname) + "\"></td>\n");

		fw.write("<td>\n");

		fw.write("\t<table border=1 cellpadding=10 cellspacing=0>\n");

		fw.write("\t<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("\t<th>Chemicals</th>\n");
		fw.write("\t<th>MAE*</th>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("\t<td>Entire set</td>\n");
		fw.write("\t<td>" + df.format(MAEEntireTestSet) + "</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t<tr>\n");
		fw.write("\t<td>Similarity coefficient &ge; " + SCmin + "</td>\n");

		if (MAE < MAEEntireTestSet) {
			fw.write("\t<td BGCOLOR=\"#90EE90\">" + df.format(MAE) + "</td>\n");
		} else {
			fw.write("\t<td BGCOLOR=LIGHTPINK>" + df.format(MAE) + "</td>\n");
		}
		// fw.write("\t<td>"+df.format(MAE)+"</td>\n");
		fw.write("\t</tr>\n");

		fw.write("\t</table>\n");

		String units = "";

		if (isLogMolarEndpoint) {
			units = TESTConstants.getMolarLogUnits(endpoint);
		} else {
			units = TESTConstants.getMassUnits(endpoint);
		}

		fw.write("*Mean absolute error in " + units + "\n");

		fw.write("</td>\n");

		fw.write("</tr></table>\n");

	}



	static String calcCancerStats(double cutoff, Vector<Double> vecExp, Vector<Double> vecPred) {

		int predCount = 0;
		int posPredcount = 0;
		int negPredcount = 0;

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
				posPredcount++;
			else if (strExp.equals("NC"))
				negPredcount++;

			if (strExp.equals(strPred)) {
				correctCount++;
				if (strExp.equals("C"))
					posCorrectCount++;
				else if (strExp.equals("NC"))
					negCorrectCount++;

			}
		}

		double concordance = correctCount / (double) predCount;
		double posConcordance = posCorrectCount / (double) posPredcount;
		double negConcordance = negCorrectCount / (double) negPredcount;

		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

		String stats = "<table border=1 cellpadding=3 cellspacing=0>\n";
		stats += "<caption>Prediction statistics for similar chemicals</caption>\r\n";

		stats += "\n<tr bgcolor=\"#D3D3D3\">\n";
		stats += "<th>Concordance</th>\r\n";
		stats += "<th>Sensitivity</th>\r\n";
		stats += "<th>Specificity</th>\r\n";
		stats += "</tr>\r\n";

		stats += "<tr>\r\n";

		if (predCount > 0) {
			stats += "<td align=\"center\">" + df.format(concordance) + "<br>(" + correctCount + " out of " + predCount + ")</td>\r\n";
		} else {
			stats += "<td align=\"center\">N/A</td>\r\n";
		}

		if (posPredcount > 0)
			stats += "<td align=\"center\" >" + df.format(posConcordance) + "<br>(" + posCorrectCount + " out of " + posPredcount + ")</td>\r\n";
		else
			stats += "<td align=\"center\">N/A</td>\r\n";

		if (negPredcount > 0)
			stats += "<td align=\"center\">" + df.format(negConcordance) + "<br>(" + negCorrectCount + " out of " + negPredcount + ")</td>\r\n";
		else
			stats += "<td align=\"center\">N/A</td>\r\n";

		stats += "</tr>\r\n";

		stats += "</table><br><br>\r\n";

		return stats;

	}

	private void WriteMainTable(FileWriter fw, String CAS, double predToxVal, double predToxUnc, String endpoint, String method, double MW, Lookup.ExpRecord er, String message) throws Exception {

		java.text.DecimalFormat d = new java.text.DecimalFormat("0.00");
		java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00E00");

		double ExpToxValMass = -9999;
		double PredToxValMass = -9999;

		boolean writePredictionInterval = true;

		if (method.equals(TESTConstants.ChoiceConsensus) || method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
			writePredictionInterval = false;
		}

		if (isLogMolarEndpoint) {
			if (predToxVal != -9999) {
				PredToxValMass = getToxValMass(endpoint, predToxVal, MW);
			}

			if (er.expToxValue != -9999) {
				ExpToxValMass = getToxValMass(endpoint, er.expToxValue, MW);
			}
		} else {
			PredToxValMass = predToxVal;
			ExpToxValMass = er.expToxValue;
		}

		String endpoint2 = endpoint.replace("50", "<sub>50</sub>");

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<caption>Prediction results</caption>\r\n");

		// ************************************************
		// Header row
		fw.write("<tr bgcolor=\"#D3D3D3\">\n");

		fw.write("<th>Endpoint</th>\n");

		if (er.expToxValue == -9999) {
			fw.write("<th>Experimental value</th>\n");
		} else {
			fw.write("<th align=left>Experimental value (CAS= " + er.expCAS + ")");

			fw.write(getSourceTag(endpoint));

			fw.write("</th>\n");
		}

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {

			fw.write("<th>Predicted value");
			fw.write("<sup>a</sup>");

			if (!message.equals("OK")) {
				fw.write("<sup>,b</sup>");
			}

			fw.write("</th>\n");

		} else {
			if (!message.equals("OK")) {
				fw.write("<th>Predicted value<sup>b</sup></th>\n");
			} else {
				fw.write("<th>Predicted value</th>\n");
			}
		}

		if (writePredictionInterval)
			fw.write("<th>Prediction interval</th>\n");

		fw.write("</tr>\n");

		// ************************************************************
		// Molar units row
		if (isLogMolarEndpoint) {
			fw.write("<tr>\n");

			fw.write("<td>" + endpoint2 + " " + TESTConstants.getMolarLogUnits(endpoint) + "</td>\n");

			if (er.expToxValue == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n");
			} else {
				fw.write("<td align=\"center\">" + d.format(er.expToxValue) + "</td>\n"); // molar
			}

			if (predToxVal == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n"); // molar units
			} else {
				fw.write("<td align=\"center\">" + d.format(predToxVal) + "</td>\n");// molar
			}

			if (writePredictionInterval) {
				if (predToxVal == -9999) {
					fw.write("<td align=\"center\">N/A</td>\n"); // molar units
				} else {
					double minval = predToxVal - predToxUnc; // molar value
					double maxval = predToxVal + predToxUnc; // molar value
					fw.write("<td align=\"center\">" + d.format(minval) + " &le; Tox &le; " + d.format(maxval) + "</td>\n");// molar
				}
			}

			fw.write("</tr>\n");
		}
		// ************************************************************
		// mass units row:

		fw.write("<tr>\n");

		fw.write("<td>" + endpoint2 + " " + TESTConstants.getMassUnits(endpoint) + "</td>\n");

		if (er.expToxValue == -9999) {
			fw.write("<td align=\"center\">N/A</td>\n");// mass
		} else {
			if (Math.abs(ExpToxValMass) < 0.1 && isLogMolarEndpoint) {
				fw.write("<td align=\"center\">" + d2.format(ExpToxValMass) + "</td>\n"); // mass
			} else {
				fw.write("<td align=\"center\">" + d.format(ExpToxValMass) + "</td>\n"); // mass
			}
		}

		if (predToxVal == -9999) {
			fw.write("<td align=\"center\">N/A</td>\n"); // mass units
		} else {
			if (Math.abs(PredToxValMass) < 0.1 && isLogMolarEndpoint) {
				fw.write("<td align=\"center\">" + d2.format(PredToxValMass) + "</td>\n"); // mass
			} else {
				fw.write("<td align=\"center\">" + d.format(PredToxValMass) + "</td>\n"); // mass
			}
		}

		if (writePredictionInterval) {
			if (predToxVal == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n"); // molar units
			} else {
				double minval = predToxVal + predToxUnc; // molar value
				double maxval = predToxVal - predToxUnc; // molar value

				double minvalmass, maxvalmass;

				if (isLogMolarEndpoint) {
					minvalmass = getToxValMass(endpoint, minval, MW);
					maxvalmass = getToxValMass(endpoint, maxval, MW);
				} else {
					minvalmass = minval;
					maxvalmass = maxval;
				}

				if (maxvalmass < minvalmass) {// need for BCF
					double temp = minvalmass;
					minvalmass = maxvalmass;
					maxvalmass = temp;
				}

				if (Math.abs(maxvalmass) < 0.1 && isLogMolarEndpoint) {
					fw.write("<td align=\"center\">" + d2.format(minvalmass) + " &le; Tox &le; " + d2.format(maxvalmass) + "</td>\n");// mass,
																																		// need
																																		// to
																																		// reverse
																																		// values
																																		// since
																																		// less
																																		// toxic
																																		// =
																																		// larger
																																		// value
				} else {
					fw.write("<td align=\"center\">" + d.format(minvalmass) + " &le; Tox &le; " + d.format(maxvalmass) + "</td>\n");// mass,
																																	// need
																																	// to
																																	// reverse
																																	// values
																																	// since
																																	// less
																																	// toxic
																																	// =
																																	// larger
																																	// value
				}

			}
		}

		fw.write("</tr>\n");

		// ************************************************************

		fw.write("</table>\n");

		if (er.expSet.equals("Training")) {
			fw.write("<sup>a</sup><font color=purple>Note: the test chemical was present in the training set.");

			if (predToxVal != -9999) {
				if (method.equals(TESTConstants.ChoiceFDAMethod) || method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
					fw.write("  However, the prediction <i>does</i> represent an external prediction.");
				} else {
					fw.write("  The prediction <i>does not</i> represent an external prediction.");
				}
			}

			fw.write("</font><br>\r\n");

		} else if (er.expSet.equals("Test")) {
			fw.write("<sup>a</sup><font color=blue>Note: the test chemical was present in the external test set.</font><br>\r\n");
		}

		// System.out.println(message);
		if (!message.equals("OK")) {
			fw.write("<sup>b</sup><font color=darkred>" + message + "</font>\r\n");
		}
	}

	public void WriteRandomForrestCaesarResultsWebPages(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, double[] calcToxCluster, Lookup.ExpRecord er, OptimalResults or, double MW, String message, Hashtable<Double, Instance> htTestMatch,
			Hashtable<Double, Instance> htTrainMatch, int chemicalNameIndex, ReportOptions options) throws Exception {

		this.isBinaryEndpoint = isBinaryEndpoint;
		this.isLogMolarEndpoint = isLogMolarEndpoint;

		String outputfilename = "PredictionResults";

		outputfilename += method.replaceAll(" ", "");
		outputfilename += ".html";

		FileWriter fw = new FileWriter(OutputFolder + File.separator + outputfilename);

		this.WriteHeaderInfo(fw, CAS, endpoint, method);

		fw.write("<h2>Predicted " + endpoint + " for <font color=\"blue\">" + CAS + "</font> from " + method + " method</h2>\n");

		java.text.DecimalFormat d = new java.text.DecimalFormat("0.00");
		java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00E00");

		double predToxVal = chemical.getPredictedValue();
		double predToxUncertainty = chemical.getPredictedUncertainty();

		this.WriteBinaryPredictionTable(fw, CAS, endpoint, method, er, predToxVal, MW, message);

		fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for test chemical</a></p>\n");

		fw.write("<br><hr>\n");

		// if (testSetMatchSetTable!=null)
		// for (int i=0;i<testSetMatchSetTable.size();i++)
		// fw.write(testSetMatchSetTable.get(i));

		this.WriteSimilarChemicals("test", htTestMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, null, null, options);// TODO
																																							// for
																																							// now
																																							// dont
																																							// send
																																							// gsid
																																							// lookup
		fw.write("<br><hr>\n");
		this.WriteSimilarChemicals("training", htTrainMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, null, null, options);// TODO
																																								// for
																																								// now
																																								// dont
																																								// send
																																								// gsid
																																								// lookup

		fw.write("</html>\n");

		fw.close();

	}

	public void WriteFDAResultsWebPages(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint, boolean isLogMolarEndpoint,
			Lookup.ExpRecord er, OptimalResults or, double MW, String message, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, int chemicalNameIndex,
			ReportOptions options) throws Exception {

		this.isBinaryEndpoint = isBinaryEndpoint;
		this.isLogMolarEndpoint = isLogMolarEndpoint;

		Vector resultsVector = new Vector();

		if (or != null) {
			double predToxVal = chemical.getPredictedValue();
			double predToxUnc = chemical.getPredictedUncertainty();
			chemical.getPredictions().add(predToxVal);
			chemical.getUncertainties().add(predToxUnc);
			resultsVector.add(or);
		}

		this.WriteMainPage(method, chemical, OutputFolder, CAS, endpoint, abbrev, er, resultsVector, null, MW, message, htTestMatch, htTrainMatch, options);

		// this.WriteMainPageFDA(method, chemical, OutputFolder, CAS,
		// endpoint,endpoint_abbrev, calcToxCluster, expToxVal, predToxVal,
		// predToxUncertainty, or);
		// if (expCAS != null) {
		// this.WriteExperimentalValuePage(OutputFolder, expCAS, endpoint);
		// }

		if (or != null) {
			String filename = "PredictionResultsFDACluster.html";

			String strClusterFolder = OutputFolder + "/ClusterFiles";
			File ClusterFolder = new File(strClusterFolder);
			if (!ClusterFolder.exists())
				ClusterFolder.mkdir();

			if (isBinaryEndpoint) {
				this.WriteResultsWebPageBinary(method, chemical, filename, strClusterFolder, CAS, endpoint, or, options);
			} else {
				this.WriteResultsWebPage(method, chemical, filename, strClusterFolder, CAS, endpoint, or, options);
				CreateQSARPlot(or, "FDA_QSAR_Plot.png", strClusterFolder, endpoint);
			}

			File of1 = new File(OutputFolder);
			String folder = of1.getParentFile().getParent();
			String strImageFolder = folder + "/Images";
			CreateStructureImages(or, strImageFolder);

			String strDescriptorsFolder = OutputFolder + "/ClusterFiles";
			File DescriptorsFolder = new File(strDescriptorsFolder);
			if (!DescriptorsFolder.exists())
				DescriptorsFolder.mkdir();

			String title = "Descriptors for " + CAS + " for FDA model";

			WriteTestChemicalDescriptorsForClusterModel(chemical, or, strDescriptorsFolder, "DescriptorsFDA.html", endpoint, title, options);
		}
	}

	private void WriteClusterPage(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String expCAS, OptimalResults or, long minFileAge, ReportOptions options) {

		String filename = "";
		String filename2 = "";

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
			filename = "GroupContributionCluster.html";
			filename2 = "QSAR_PlotGroupContributionCluster.png";
		} else {
			filename = or.getClusterNumber() + ".html";
			filename2 = "QSAR_Plot" + or.getClusterNumber() + ".png";
		}

		// String OutputFolder2=OutputFolder+"/HierarchicalFiles";
		// File OF=new File(OutputFolder2);
		// if(!OF.exists()) OF.mkdir();

		File of1 = new File(OutputFolder);
		String folder = of1.getParentFile().getParent();
		String strClusterFolder = folder + "/ClusterFiles";
		File ClusterFolder = new File(strClusterFolder);
		if (!ClusterFolder.exists())
			ClusterFolder.mkdir();

		String strClusterFolderEndpoint = strClusterFolder + "/" + endpoint;
		File ClusterFolderEndpoint = new File(strClusterFolderEndpoint);
		if (!ClusterFolderEndpoint.exists())
			ClusterFolderEndpoint.mkdir();

		// boolean writeModelFiles=true;
		//
		try {

			String filepath = strClusterFolderEndpoint + File.separator + filename;
			File file = new File(filepath);

			double diff_in_hours = -1;

			if (file.exists()) {
				// Path p = Paths.get(filepath);
				// BasicFileAttributes view = Files.getFileAttributeView(p,
				// BasicFileAttributeView.class).readAttributes();
				//
				// long fileModTime = view.lastModifiedTime().toMillis();

				long fileModTime = file.lastModified();

				long currentTime = System.currentTimeMillis();
				long diff = currentTime - fileModTime;
				diff_in_hours = diff / 1000.0 / 60.0 / 60.0;

				// if (diff_in_hours < 1)//only write model files if they are
				// more than 1 hr old
				// writeModelFiles = false;
				// System.out.println(diff_in_hours);
			}

			if (!file.exists() || diff_in_hours > minFileAge) {

				if (!isBinaryEndpoint) {
					WriteResultsWebPage(method, chemical, filename, strClusterFolderEndpoint, CAS, endpoint, or, options);

					CreateQSARPlot(or, filename2, strClusterFolderEndpoint, endpoint);
				} else {
					WriteResultsWebPageBinary(method, chemical, filename, strClusterFolderEndpoint, CAS, endpoint, or, options);

				}
				String strImageFolder = folder + "/Images";
				CreateStructureImages(or, strImageFolder);
			}

		} catch (Exception ex) {
			logger.catching(ex);
		}

		//////////////////////////////////////////////////////////////////
		String strDescriptorsFolder = OutputFolder + "/ClusterFiles";
		File DescriptorsFolder = new File(strDescriptorsFolder);
		if (!DescriptorsFolder.exists())
			DescriptorsFolder.mkdir();

		String descriptorsFilename = "";

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
			descriptorsFilename = "DescriptorsGroupContribution.html";
		} else {
			descriptorsFilename = "Descriptors" + or.getClusterNumber() + ".html";
		}

		String title = "";
		if (method.equals(TESTConstants.ChoiceFDAMethod)) {
			title = "Descriptors for " + CAS + " for FDA model";
		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
			title = "Descriptors for " + CAS + " for group contribution model";
		} else {
			title = "Descriptors for " + CAS + " for cluster model#" + or.getClusterNumber();
		}
		WriteTestChemicalDescriptorsForClusterModel(chemical, or, strDescriptorsFolder, descriptorsFilename, endpoint, title, options);

	}

	

	public static String getSourceTag(String endpoint) {
		String sourceTag = null;
				
		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50) || endpoint.equals(TESTConstants.ChoiceDM_LC50) || endpoint.equals(TESTConstants.ChoiceGA_EC50)) {
			//link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"http://cfpub.epa.gov/ecotox/\" target=\"_blank\">ECOTOX</a>");
		} else if (endpoint.equals(TESTConstants.ChoiceTP_IGC50)) {
			//link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"https://www.tandfonline.com/doi/abs/10.1080/105172397243079\" target=\"_blank\">TETRATOX</a>");
		} else if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
//			link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"https://chem.nlm.nih.gov/chemidplus/\" target=\"_blank\">ChemidPlus</a>");
		} else if (endpoint.equals(TESTConstants.ChoiceBCF)) {
//			links validated on 11/7/20:
			String ref = "<br>Sources: ";
			ref += "<a href=\"http://www.tandfonline.com/doi/abs/10.1080/10659360500474623\" target=\"_blank\">Dimetrov 2005</a>, ";
			ref += "<a href=\"http://www.nrcresearchpress.com/doi/abs/10.1139/a06-005\" target=\"_blank\">Arnot 2006</a>, and ";
			ref += "<a href=\"http://www.sciencedirect.com/science/article/pii/S0045653508011922\" target=\"_blank\">Zhao 2008</a>";
			sourceTag = (ref);
		} else if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
			//link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"http://www.caesar-project.eu/index.php?page=results&section=endpoint&ne=5\" target=\"_blank\">CAESAR</a>");
		} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
			//link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"http://doc.ml.tu-berlin.de/toxbenchmark/\" target=\"_blank\">Toxicity Benchmark</a>");
		} else if (endpoint.equals(TESTConstants.ChoiceDensity) || endpoint.equals(TESTConstants.ChoiceFlashPoint)) {
//			link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"http://www.lookchem.com/\" target=\"_blank\">Lookchem.com</a>");
		} else if (endpoint.equals(TESTConstants.ChoiceViscosity)) {
//			links validated on 11/7/20:
			String ref = "<br>Sources: ";
			ref += "<a href=\"http://www.worldcat.org/title/data-book-on-the-viscosity-of-liquids/oclc/18833753\" target=\"_blank\">Viswanath 1989</a>, ";
			ref += "<a href=\"https://www.worldcat.org/title/techniques-of-chemistry-2-organic-solvents-physical-properties-and-methods-of-purification-4ed/oclc/472811023\" target=\"_blank\">Riddick 1996</a>";
			sourceTag = (ref);
		} else if (endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {
//			links validated on 11/7/20:
			String ref = "<br>Sources: ";
			ref += "<a href=\"http://www.worldcat.org/title/liquid-thermal-conductivity-a-data-survey-to-1973/oclc/3090244\" target=\"_blank\">Jamieson 1975</a>, ";
			ref += "<a href=\"http://www.worldcat.org/title/handbook-of-thermal-conductivity-of-liquids-and-gases/oclc/28847166&referer=brief_results\" target=\"_blank\">Vargaftik 1994</a>";
			sourceTag = (ref);
		} else if (endpoint.equals(TESTConstants.ChoiceSurfaceTension)) {
//			link validated on 11/7/20:
			String ref = "<br>Source: ";
			ref += "<a href=\"https://doi.org/10.1063/1.3253106\" target=\"_blank\">Jaspar 1972</a>";
			sourceTag = ref;
		} else if (endpoint.equals(TESTConstants.ChoiceWaterSolubility) || endpoint.equals(TESTConstants.ChoiceBoilingPoint) || endpoint.equals(TESTConstants.ChoiceVaporPressure)
				|| endpoint.equals(TESTConstants.ChoiceMeltingPoint)) {
//			link validated on 11/7/20:
			sourceTag = ("<br>Source: <a href=\"" + "https://www.epa.gov/tsca-screening-tools/epi-suitetm-estimation-program-interface\" target=\"_blank\">" + "EPI Suite v 4.00</a> ");
		} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor) || endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
			String ref = "<br>Source: ";
			ref += "<a href=\"http://www.worldcat.org/title/predicting-chemical-toxicity-and-fate/oclc/54073110&referer=brief_results\">Cronin and Livingstone, 2004</a>";
			sourceTag = ref;
		} else {
			sourceTag = "<br>?";
		}
		return sourceTag;
	}
	
	

	// private void WritePredictionResultsTable1(String method,FileWriter
	// fw,String endpoint,Lookup.ExpRecord er,TestChemical chemical,double
	// MW,String message) throws Exception {
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.00");
	// java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00E00");
	//
	// double PredToxVal=chemical.getPredictedValue();
	// double PredToxUncertainty=chemical.getPredictedUncertainty();
	//
	// double ExpToxValMass=-9999;
	// double PredToxValMass=-9999;
	//
	//
	//// System.out.println("PredToxVal="+PredToxVal);
	//
	// if (PredToxVal!=-9999) {
	// PredToxValMass=this.getToxValMass(endpoint, PredToxVal, MW);
	// }
	//
	// if (er.expToxValue!=-9999) {
	// ExpToxValMass=this.getToxValMass(endpoint, er.expToxValue, MW);
	// }
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	// fw.write("<caption>Prediction results</caption>\r\n");
	//
	//
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	// fw.write("<th rowspan=\"2\">Parameter</th>\n");
	//
	//
	// String endpoint2=endpoint.replace("50","<sub>50</sub>");
	//
	// fw.write("<th colspan=\"2\">"+endpoint2+"</th>\n");
	// fw.write("</tr>\n");
	//
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	//
	// fw.write("<th>"+getMassUnits(endpoint)+"</th>\n");
	// fw.write("<th>"+getMolarLogUnits(endpoint)+"</th>\n");
	//
	// fw.write("</tr>\n");
	//
	// // *********************************************************
	// // in mass units:
	// fw.write("<tr>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td>Experimental value</td>\n");
	// } else {
	//// fw.write("<td><a href=\"experimentalvalue.html\">Experimental value
	// (CAS#="+expCAS+")</a></td>\n");
	//
	// fw.write("<td>Experimental value (CAS#="+er.expCAS+")");
	//
	// fw.write(this.getSource(endpoint));
	//
	// fw.write("</td>\n");
	//
	// }
	//
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");//mass
	// fw.write("<td align=\"center\">N/A</td>\n");//molar
	// } else {
	// if (ExpToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(ExpToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(ExpToxValMass)+"</td>\n");
	// //mass
	// }
	//
	//
	// fw.write("<td align=\"center\">"+d.format(er.expToxValue)+"</td>\n");
	// //molar
	// }
	//
	// fw.write("</tr>\n");
	//
	// // *********************************************************
	//
	// fw.write("<tr>\n");
	//
	// if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
	//
	// fw.write("<td>Predicted value");
	// fw.write("<sup>a</sup>");
	//
	// if (!message.equals("OK")) {
	// fw.write("<sup>,b</sup>");
	// }
	//
	// fw.write("</td>\n");
	//
	// } else {
	// if (!message.equals("OK")) {
	// fw.write("<td>Predicted value<sup>b</sup></td>\n");
	// } else {
	// fw.write("<td>Predicted value</td>\n");
	// }
	// }
	//
	//// if (inTrainingSet)
	//// fw.write("<td>Predicted value<sup>*</sup></td>\n");
	//// else
	//// fw.write("<td>Predicted value</td>\n");
	//
	//
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	//
	// if (PredToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(PredToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxValMass)+"</td>\n");
	// //mass
	// }
	//
	// fw.write("<td align=\"center\">"+d.format(PredToxVal)+"</td>\n");//molar
	// }
	//
	// fw.write("</tr>\n");
	//
	//
	// // *********************************************************
	//
	// fw.write("<tr>\n");
	// fw.write("<td>Prediction interval</td>\n");
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// double minval=PredToxVal+PredToxUncertainty; //molar value
	// double maxval=PredToxVal-PredToxUncertainty; //molar value
	//
	// double minvalmass=this.getToxValMass(endpoint, minval, MW);
	// double maxvalmass=this.getToxValMass(endpoint, maxval, MW);
	//
	// if (maxvalmass<minvalmass) {//need for BCF since doesnt use negative log
	// value
	// double temp=minvalmass;
	// minvalmass=maxvalmass;
	// maxvalmass=temp;
	// }
	//
	// if (maxvalmass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(minvalmass)+" &le; Tox &le;
	// "+d2.format(maxvalmass)+"</td>\n");
	// } else {
	// fw.write("<td align=\"center\">"+d.format(minvalmass)+" &le; Tox &le;
	// "+d.format(maxvalmass)+"</td>\n");
	// }
	//
	// fw.write("<td align=\"center\">"+d.format(minval)+" &le; Tox &le;
	// "+d.format(maxval)+"</td>\n");//molar
	// }
	//
	//
	// fw.write("</tr>\n");
	// // *********************************************************
	//
	//
	// fw.write("</table>\n");
	// if (er.expSet.equals("Training")) {
	// fw.write("<sup>a</sup><font color=purple>Note: the test chemical was
	// present in the training set.<br>");
	//
	// if (method.equals(TESTConstants.ChoiceHierarchicalMethod) ||
	// method.equals(TESTConstants.ChoiceGroupContributionMethod)||
	// method.equals(TESTConstants.ChoiceSingleModelMethod)) {
	// fw.write(" The prediction <br> " +
	// "does not represent an external prediction.");
	// } else {
	// //FDA method yields external prediction (but not in strictest sense)
	// }
	// fw.write("</font>\r\n");
	//
	// } else if (er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup><font color=blue>Note: the test chemical was
	// present in the external test set.</font><br>\r\n");
	// }
	// if (!message.equals("OK")) {
	// fw.write("<sup>b</sup><font color=darkred>" +message+"</font>\r\n");
	// }
	//
	//
	//
	// }

	// private void WritePredictionResultsTable2(String method,FileWriter
	// fw,String endpoint,Lookup.ExpRecord er,TestChemical chemical,double
	// MW,String message) throws Exception {
	//
	// if (isLogMolarEndpoint) {
	// WritePredictionResultsTable2a(method,fw,endpoint,er,chemical,MW,message);
	// } else {
	// WritePredictionResultsTable2b(method,fw,endpoint,er,chemical,MW,message);
	// }
	// }

	// private void WritePredictionResultsTable2a(String method,FileWriter
	// fw,String endpoint,Lookup.ExpRecord er,TestChemical chemical,double
	// MW,String message) throws Exception {
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.00");
	// java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00E00");
	//
	// double PredToxVal=chemical.getPredictedValue();
	// double PredToxUncertainty=chemical.getPredictedUncertainty();
	//
	//// System.out.println(PredToxUncertainty);
	//
	// double ExpToxValMass=-9999;
	// double PredToxValMass=-9999;
	//
	// if (PredToxVal!=-9999) {
	// PredToxValMass=this.getToxValMass(endpoint,PredToxVal,MW);
	// }
	//
	// if (er.expToxValue!=-9999) {
	// ExpToxValMass=this.getToxValMass(endpoint,er.expToxValue,MW);
	// }
	//
	//
	// String endpoint2=endpoint.replace("50","<sub>50</sub>");
	//
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	//
	// fw.write("<caption>Prediction results</caption>\r\n");
	//
	// // ************************************************
	// // Header row
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	//
	// fw.write("<th>Endpoint</th>\n");
	//
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<th>Experimental value</th>\n");
	// } else {
	// fw.write("<th>Experimental value<br>CAS: "+er.expCAS);
	//
	// fw.write(this.getSource(endpoint));
	//
	// fw.write("</th>\n");
	// }
	//
	// if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
	//
	// fw.write("<th>Predicted value");
	// fw.write("<sup>a</sup>");
	//
	// if (!message.equals("OK")) {
	// fw.write("<sup>,b</sup>");
	// }
	//
	// fw.write("</th>\n");
	//
	// } else {
	// if (!message.equals("OK")) {
	// fw.write("<th>Predicted value<sup>b</sup></th>\n");
	// } else {
	// fw.write("<th>Predicted value</th>\n");
	// }
	// }
	//
	// fw.write("<th>Prediction interval</th>\n");
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//// Molar units row
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>"+endpoint2+" "+getMolarLogUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");
	// } else {
	// fw.write("<td align=\"center\">"+d.format(er.expToxValue)+"</td>\n");
	// //molar
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxVal)+"</td>\n");//molar
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// double minval=PredToxVal-PredToxUncertainty; //molar value
	// double maxval=PredToxVal+PredToxUncertainty; //molar value
	// fw.write("<td align=\"center\">"+d.format(minval)+" &le; Tox &le;
	// "+d.format(maxval)+"</td>\n");//molar
	// }
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	// // mass units row:
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>"+endpoint2+" "+getMassUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");//mass
	// } else {
	// if (ExpToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(ExpToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(ExpToxValMass)+"</td>\n");
	// //mass
	// }
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// } else {
	// if (PredToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(PredToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxValMass)+"</td>\n");
	// //mass
	// }
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// double minval=PredToxVal+PredToxUncertainty; //molar value
	// double maxval=PredToxVal-PredToxUncertainty; //molar value
	//
	// double minvalmass=this.getToxValMass(endpoint, minval, MW);
	// double maxvalmass=this.getToxValMass(endpoint, maxval, MW);
	//
	// if (maxvalmass<minvalmass) {//need for BCF
	// double temp=minvalmass;
	// minvalmass=maxvalmass;
	// maxvalmass=temp;
	// }
	//// System.out.println("maxvalmass="+maxvalmass);
	//// System.out.println("minvalmass="+minvalmass);
	//
	// if (maxvalmass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(minvalmass)+" &le; Tox &le;
	// "+d2.format(maxvalmass)+"</td>\n");//mass, need to reverse values since
	// less toxic = larger value
	// } else {
	// fw.write("<td align=\"center\">"+d.format(minvalmass)+" &le; Tox &le;
	// "+d.format(maxvalmass)+"</td>\n");//mass, need to reverse values since
	// less toxic = larger value
	// }
	//
	// }
	//
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//
	// fw.write("</table>");
	//
	// if (er.expSet.equals("Training")) {
	// fw.write("<sup>a</sup><font color=purple>Note: the test chemical was
	// present in the training set.");
	//
	// if (method.equals(TESTConstants.ChoiceConsensus) ||
	// method.equals(TESTConstants.ChoiceHierarchicalMethod) ||
	// method.equals(TESTConstants.ChoiceGroupContributionMethod)||
	// method.equals(TESTConstants.ChoiceSingleModelMethod)) {
	// fw.write(" The prediction " +
	// "does not represent an external prediction.");
	// } else {
	// fw.write(" However, the prediction " +
	// "does represent an external prediction.");
	// }
	// fw.write("</font>\r\n");
	//
	// } else if (er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup><font color=blue>Note: the test chemical was
	// present in the external test set.</font><br>\r\n");
	// }
	// if (!message.equals("OK")) {
	// fw.write("<sup>b</sup><font color=darkred>" +message+"</font>\r\n");
	// }
	//
	// }
	//
	// private void WritePredictionResultsTable2b(String method,FileWriter
	// fw,String endpoint,Lookup.ExpRecord er,TestChemical chemical,double
	// MW,String message) throws Exception {
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.00");
	// java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00E00");
	//
	// double PredToxVal=chemical.getPredictedValue();
	// double PredToxUncertainty=chemical.getPredictedUncertainty();
	//
	// double ExpToxVal=er.expToxValue;
	//
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	//
	// fw.write("<caption>Prediction results</caption>\r\n");
	//
	// // ************************************************
	// // Header row
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	//
	// fw.write("<th>Endpoint</th>\n");
	//
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<th>Experimental value</th>\n");
	// } else {
	// fw.write("<th>Experimental value<br>CAS: "+er.expCAS);
	//
	// fw.write(this.getSource(endpoint));
	//
	// fw.write("</th>\n");
	// }
	//
	// if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
	//
	// fw.write("<th>Predicted value");
	// fw.write("<sup>a</sup>");
	//
	// if (!message.equals("OK")) {
	// fw.write("<sup>,b</sup>");
	// }
	//
	// fw.write("</th>\n");
	//
	// } else {
	// if (!message.equals("OK")) {
	// fw.write("<th>Predicted value<sup>b</sup></th>\n");
	// } else {
	// fw.write("<th>Predicted value</th>\n");
	// }
	// }
	//
	// fw.write("<th>Prediction interval</th>\n");
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//// Mass units row
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>"+endpoint+" "+getMassUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");
	// } else {
	// fw.write("<td align=\"center\">"+d.format(er.expToxValue)+"</td>\n");
	// //molar
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxVal)+"</td>\n");//molar
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// } else {
	// double minval=PredToxVal-PredToxUncertainty; //mass value
	// double maxval=PredToxVal+PredToxUncertainty; //mass value
	// fw.write("<td align=\"center\">"+d.format(minval)+" &le; Tox &le;
	// "+d.format(maxval)+"</td>\n");//molar
	// }
	//
	// fw.write("</tr>\n");
	//
	//
	//// ************************************************************
	//
	// fw.write("</table>");
	//
	// if (er.expSet.equals("Training")) {
	// fw.write("<sup>a</sup><font color=purple>Note: the test chemical was
	// present in the training set.");
	//
	// if (method.equals(TESTConstants.ChoiceConsensus) ||
	// method.equals(TESTConstants.ChoiceHierarchicalMethod) ||
	// method.equals(TESTConstants.ChoiceGroupContributionMethod)||
	// method.equals(TESTConstants.ChoiceSingleModelMethod)) {
	// fw.write(" The prediction " +
	// "does not represent an external prediction.");
	// } else {
	// fw.write(" However, the prediction " +
	// "does represent an external prediction.");
	// }
	// fw.write("</font>\r\n");
	//
	// } else if (er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup><font color=blue>Note: the test chemical was
	// present in the external test set.</font><br>\r\n");
	// }
	// if (!message.equals("OK")) {
	// fw.write("<sup>b</sup><font color=darkred>" +message+"</font>\r\n");
	// }
	//
	// }

	// private void WriteMainResultsTableNN2(FileWriter fw,String CAS,String
	// endpoint,String method,Lookup.ExpRecord er,double PredToxVal,double
	// MW,String msg,double ExpToxValMass,
	// double PredToxValMass) throws Exception {
	//
	// if (isBinaryEndpoint) {
	// this.WriteBinaryPredictionTable(fw, CAS, endpoint, method,er, PredToxVal,
	// MW, "OK");
	// return;
	// }
	//
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.00");
	// java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00E00");
	//
	// String endpoint2=endpoint.replace("50","<sub>50</sub>");
	//
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	//
	// fw.write("<caption>Prediction results</caption>\r\n");
	//
	// // ************************************************
	// // Header row
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	//
	// fw.write("<th>Endpoint</th>\n");
	//
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<th>Experimental value</th>\n");
	// } else {
	// fw.write("<th>Experimental value<br>CAS: "+er.expCAS);
	//
	// fw.write(this.getSource(endpoint));
	//
	//
	// fw.write("</th>\n");
	// }
	//
	// if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
	//
	// fw.write("<th>Predicted value");
	//
	// if (PredToxVal==-9999) {
	// fw.write("<sup>a,b</sup>");
	// } else {
	// fw.write("<sup>a</sup>");
	// }
	//
	// fw.write("</th>\n");
	//
	// } else {
	// fw.write("<th>Predicted value");
	// if (PredToxVal==-9999) {
	// fw.write("<sup>b</sup>");
	// } else {
	// }
	// fw.write("</th>\n");
	// }
	//
	//// fw.write("<th>Prediction interval</th>\n");
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//// Molar units row
	//
	// fw.write("<tr>\n");
	//
	//// fw.write("<td>"+endpoint2+" "+getMolarLogUnits(endpoint)+"</td>\n");
	//
	// fw.write("<td>"+endpoint2+" "+getMolarLogUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");
	// } else {
	// fw.write("<td align=\"center\">"+d.format(er.expToxValue)+"</td>\n");
	// //molar
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxVal)+"</td>\n");//molar
	// }
	//
	//// if (PredToxVal==-9999) {
	//// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	//// } else {
	//// double minval=PredToxVal-PredToxUncertainty; //molar value
	//// double maxval=PredToxVal+PredToxUncertainty; //molar value
	//// fw.write("<td align=\"center\">"+d.format(minval)+" &le; Tox &le;
	// "+d.format(maxval)+"</td>\n");//molar
	//// }
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	// // mass units row:
	//
	// fw.write("<tr>\n");
	//
	//// fw.write("<td>"+endpoint2+" "+getMassUnits(endpoint)+"</td>\n");
	//
	// fw.write("<td>"+endpoint2+" "+getMassUnits(endpoint)+"</td>\n");
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");//mass
	// } else {
	// if (ExpToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(ExpToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(ExpToxValMass)+"</td>\n");
	// //mass
	// }
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// } else {
	// if (PredToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(PredToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxValMass)+"</td>\n");
	// //mass
	// }
	// }
	//
	//
	// fw.write("</tr>\n");
	//
	//// ************************************************************
	//
	// fw.write("</table>");
	//
	// if (er.expSet.equals("Training")) {
	// fw.write("<sup>a</sup><font color=purple> Note: the test chemical was
	// present in the training set. However, the prediction " +
	// "does represent an external prediction.");
	//
	// fw.write("</font><br>\r\n");
	//
	// } else if (er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup><font color=blue> Note: the test chemical was
	// present in the external test set.</font><br>\r\n");
	// }
	//
	// if (PredToxVal==-9999) {
	// fw.write("<sup>b</sup><font color=darkred>A prediction could not be made
	// since there were insufficient chemicals in the training set that were
	// similar to the test compound.</font>\r\n");
	// }
	//
	// }

	private void WriteBinaryPredictionTable(FileWriter fw, String CAS, String endpoint, String method, Lookup.ExpRecord er, double PredToxVal, double MW, String message) throws Exception {

		java.text.DecimalFormat d = new java.text.DecimalFormat("0.00");

		// System.out.println(message);

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<caption>Prediction results</caption>\r\n");

		// ************************************************
		// Header row
		fw.write("<tr bgcolor=\"#D3D3D3\">\n");

		fw.write("<th>Endpoint</th>\n");

		if (er.expToxValue == -9999) {
			fw.write("<th>Experimental value</th>\n");
		} else {
			fw.write("<th align=left>Experimental value (CAS=" + er.expCAS + ")");
			fw.write(getSourceTag(endpoint));
			fw.write("</th>\n");
		}

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {

			fw.write("<th>Predicted value");
			fw.write("<sup>a</sup>");

			if (!message.equals("OK")) {
				fw.write("<sup>,b</sup>");
			}

			fw.write("</th>\n");

		} else {
			if (!message.equals("OK")) {
				fw.write("<th>Predicted value<sup>b</sup></th>\n");
			} else {
				fw.write("<th>Predicted value</th>\n");
			}
		}

		fw.write("</tr>\n");

		// ************************************************************
		// Value row

		fw.write("<tr>\n");
		fw.write("<td>" + endpoint + " value</td>\n");

		if (er.expToxValue == -9999) {
			fw.write("<td align=\"center\">N/A</td>\n");
		} else {
			fw.write("<td align=\"center\">" + d.format(er.expToxValue) + "</td>\n"); // molar
		}

		if (PredToxVal == -9999) {
			fw.write("<td align=\"center\">N/A</td>\n"); // molar units
		} else {
			fw.write("<td align=\"center\">" + d.format(PredToxVal) + "</td>\n");// molar
		}

		fw.write("</tr>\n");

		// ************************************************************
		// result row:

		fw.write("<tr>\n");

		fw.write("<td>" + endpoint + " result</td>\n");

		if (er.expToxValue == -9999) {
			fw.write("<td align=\"center\">N/A</td>\n");
		} else {
			if (er.expToxValue < 0.5) {
				// *add endpoint*
				if (endpoint.equals(TESTConstants.ChoiceReproTox))
					fw.write("<td align=\"center\">Developmental NON-toxicant</td>\n");
				else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
					fw.write("<td align=\"center\">Mutagenicity Negative</td>\n");
				else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
					fw.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); // mass

			} else {
				// *add endpoint*
				if (endpoint.equals(TESTConstants.ChoiceReproTox))
					fw.write("<td align=\"center\">Developmental toxicant</td>\n");
				else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
					fw.write("<td align=\"center\">Mutagenicity Positive</td>\n");
				else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
					fw.write("<td align=\"center\">Binds to estrogen receptor</td>\n");

			}
		}

		if (PredToxVal == -9999) {
			fw.write("<td align=\"center\">N/A</td>\n");
		} else {
			if (PredToxVal < 0.5) {
				if (endpoint.equals(TESTConstants.ChoiceReproTox))
					fw.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); // mass
				else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
					fw.write("<td align=\"center\">Mutagenicity Negative</td>\n"); // mass
				else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
					fw.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); // mass

			} else {
				if (endpoint.equals(TESTConstants.ChoiceReproTox))
					fw.write("<td align=\"center\">Developmental toxicant</td>\n"); // mass
				else if (endpoint.equals(TESTConstants.ChoiceMutagenicity))
					fw.write("<td align=\"center\">Mutagenicity Positive</td>\n"); // mass
				else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor))
					fw.write("<td align=\"center\">Binds to estrogen receptor</td>\n");

			}
		}

		fw.write("</tr>\n");

		// ************************************************************

		fw.write("</table>\n");

		if (er.expSet.equals("Training")) {
			fw.write("<sup>a</sup><font color=purple>Note: the test chemical was present in the training set.");

			if (method.equals(TESTConstants.ChoiceConsensus) || method.equals(TESTConstants.ChoiceHierarchicalMethod) || method.equals(TESTConstants.ChoiceGroupContributionMethod)
					|| method.equals(TESTConstants.ChoiceSingleModelMethod) || method.equals(TESTConstants.ChoiceRandomForrestCaesar)) {
				fw.write("  The prediction " + "does not represent an external prediction.<br>");
			} else {
				fw.write("  However, the prediction " + "does represent an external prediction.<br>");
			}
			fw.write("</font>\r\n");

		} else if (er.expSet.equals("Test")) {
			fw.write("<sup>a</sup><font color=blue>Note: the test chemical was present in the external test set.</font><br>\r\n");
		}
		if (!message.equals("OK")) {
			fw.write("<sup>b</sup><font color=darkred>" + message + "</font>\r\n");
		}

	}

	// private void WriteOverallModelStatistics(FileWriter fw,String
	// endpoint,TestChemical chemical,OptimalResults or,Vector resultsVector)
	// throws Exception {
	//// double PredToxVal=chemical.getPredictedValue();
	//
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.000");
	//
	// if (or==null) {
	//// fw.write("Note: model could not be built");
	// return;
	// }
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	// fw.write("<caption>Model statistics</caption>\r\n");
	//
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	// fw.write("<th>Parameter</th>\n");
	// fw.write("<th>Value</th>\n");
	// fw.write("</tr>\n");
	//
	//
	// //
	// ****************************************************************************
	//
	// if (!isBinaryEndpoint) {
	// // write r2 value:
	// fw.write("<tr>\n");
	// fw.write("<td>r<sup>2</sup></td>\n");
	// fw.write("<td align=\"center\">" + d.format(or.r2) + "</td>\n");
	// fw.write("</tr>\n\n");
	//
	// // write q2 value:
	// fw.write("<tr>\n");
	// fw.write("<td>q<sup>2</sup></td>\n");
	// fw.write("<td align=\"center\">" + d.format(or.q2) + "</td>\n");
	// fw.write("</tr>\n\n");
	// } else {
	// // write Concordance value:
	// fw.write("<tr>\n");
	// fw.write("<td>Concordance</td>\n");
	// fw.write("<td align=\"center\">" + d.format(or.Concordance) + "</td>\n");
	// fw.write("</tr>\n\n");
	//
	// // write Sensitivity value:
	// fw.write("<tr>\n");
	// fw.write("<td>Sensitivity</td>\n");
	// fw.write("<td align=\"center\">" + d.format(or.Sensitivity) + "</td>\n");
	// fw.write("</tr>\n\n");
	//
	// // write Specificity value:
	// fw.write("<tr>\n");
	// fw.write("<td>Specificity</td>\n");
	// fw.write("<td align=\"center\">" + d.format(or.Specificity) + "</td>\n");
	// fw.write("</tr>\n\n");
	//
	// }
	//
	//
	//// *******************************************************
	//// write number of chemicals:
	// fw.write("<tr>\n");
	// fw.write("<td>#chemicals</td>\n");
	//
	// fw.write("<td align=\"center\">" + or.numChemicals + "</td>\n");
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	//// write link to model page:
	//
	// fw.write("<tr>\n");
	// fw.write("<td>Model</td>\n");
	//
	// if (or.clusterNumber==-1) {//FDA
	// fw.write("<td align=\"center\"><a
	// href=\"ClusterFiles/PredictionResultsFDACluster.html\">FDA
	// model"+"</a></td>\n");
	// } else if (or.clusterNumber==0) {//GCM
	// fw.write("<td align=\"center\"><a
	// href=\"../../ClusterFiles/"+endpoint+"/GroupContributionCluster.html\">Group
	// contribution"+"</a></td>\n");
	// } else {
	// fw.write("<td align=\"center\"><a
	// href=\"../../ClusterFiles/"+endpoint+"/"+or.clusterNumber+".html\">"+or.clusterNumber+"</a></td>\n");
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	// fw.write("<td>Test chemical descriptor values</td>\n");
	//
	// if (or.clusterNumber == -1) {
	// fw.write("<td align=\"center\"><a
	// href=\"ClusterFiles/DescriptorsFDA.html"
	// + "\">Descriptors</a></td>\n");
	//
	// } else if (or.clusterNumber == 0) {// for group contribution
	// fw.write("<td align=\"center\"><a
	// href=\"ClusterFiles/DescriptorsGroupContribution.html"
	// + "\">Descriptors</a></td>\n");
	// } else {
	// fw.write("<td align=\"center\"><a href=\"ClusterFiles/Descriptors"
	// + or.clusterNumber
	// + ".html\">Descriptors</a></td>\n");
	// }
	//
	// fw.write("</tr>\n\n");
	//
	// fw.write("</table>\n");
	// }

	/**
	 * Writes a table of the overall results from each of the cluster models
	 * 
	 * @param fw
	 */
	private void WriteClusterModelTable(FileWriter fw, String endpoint, String method, Vector resultsVector, TestChemical chemical) throws Exception {

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Cluster model predictions and statistics</caption>\r\n");

		java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
		java.text.DecimalFormat d3 = new java.text.DecimalFormat("0.000");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Cluster model</th>\n");
		fw.write("<th>Test chemical <br>descriptor values</th>\n");

		if (isBinaryEndpoint) {
			fw.write("<th>Predicted value<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
			fw.write("<th>Concordance</th>\n");
			fw.write("<th>Sensitivity</th>\n");
			fw.write("<th>Specificity</th>\n");
		} else {

			String units;

			if (isLogMolarEndpoint) {
				units = TESTConstants.getMolarLogUnits(endpoint);
			} else {
				units = TESTConstants.getMassUnits(endpoint);
			}

			fw.write("<th>Prediction interval<br>" + units + "</th>\n");
			fw.write("<th>r<sup>2</sup></th>\n");
			fw.write("<th>q<sup>2</sup></th>\n");
		}

		fw.write("<th>#chemicals</th>\n");

		fw.write("</tr>\n");

		boolean HaveOmitted = false;

		for (int i = 0; i < resultsVector.size(); i++) {

			OptimalResults or = (OptimalResults) resultsVector.get(i);

			fw.write("<tr>\n");

			// fw.write("<td><a
			// href=\"../../ClusterFiles/"+endpoint+"/"+or.clusterNumber+".html\">"+or.clusterNumber+"</a></td>\n");

			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
				fw.write("<td align=\"center\"><a href=\"ClusterFiles/PredictionResultsFDACluster.html\">FDA model" + "</a></td>\n");
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
				fw.write("<td align=\"center\"><a href=\"../../ClusterFiles/" + endpoint + "/GroupContributionCluster.html\">Group contribution" + "</a></td>\n");
			} else {
				fw.write("<td align=\"center\"><a href=\"../../ClusterFiles/" + endpoint + "/" + or.getClusterNumber() + ".html\">" + or.getClusterNumber() + "</a></td>\n");
			}

			// fw.write("</tr>\n\n");

			// fw.write("<td><a
			// href=\"ClusterFiles/Descriptors"+or.clusterNumber+".html\">Descriptors</a></td>\n");

			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
				fw.write("<td align=\"center\"><a href=\"ClusterFiles/DescriptorsFDA.html" + "\">Descriptors</a></td>\n");

			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// GCM
				fw.write("<td align=\"center\"><a href=\"ClusterFiles/DescriptorsGroupContribution.html" + "\">Descriptors</a></td>\n");
			} else {
				fw.write("<td align=\"center\"><a href=\"ClusterFiles/Descriptors" + or.getClusterNumber() + ".html\">Descriptors</a></td>\n");
			}

			if (!isBinaryEndpoint) {
				fw.write("<td align=\"center\">" + d2.format(chemical.getPredictions().get(i)));
				fw.write(" &plusmn; " + d2.format(chemical.getUncertainties().get(i)));

				if ((Double) chemical.getUncertainties().get(i) == 0) {
					HaveOmitted = true;
					fw.write("*");
				}
				fw.write("</td>\n");

				fw.write("<td>" + d3.format(or.getR2()) + "</td>\n");
				if (or.getQ2() == -1)
					fw.write("<td>N/A</td>\n");
				else
					fw.write("<td>" + d3.format(or.getQ2()) + "</td>\n");

			} else {
				fw.write("<td align=\"center\">" + d2.format(chemical.getPredictions().get(i)));
				// if ((Double)chemical.getUncertainties().get(i)==0) {
				// HaveOmitted=true;
				// fw.write("*");
				// }
				fw.write("</td>\n");

				fw.write("<td align=\"center\">" + d3.format(or.getConcordance()) + "</td>\n");
				fw.write("<td align=\"center\">" + d3.format(or.getSensitivity()) + "</td>\n");
				fw.write("<td align=\"center\">" + d3.format(or.getSpecificity()) + "</td>\n");
			}

			fw.write("<td align=\"center\">" + or.getNumChemicals() + "</td>\n");

			fw.write("</tr>\n");

		}

		fw.write("</table>\n");
		if (HaveOmitted) {
			fw.write("*Value omitted from calculation of toxicity since prediction uncertainty was zero\n");
		}

	}

	/**
	 * Writes a table of the overall results from each of the QSAR models used
	 * in the consensus prediction
	 * 
	 * @param fw
	 */
	private void WriteIndividualPredictionsForConsensus(FileWriter fw, String endpoint, ArrayList<String> methods, ArrayList<Double> predictions, ArrayList<Double> uncertainties,
			boolean createDetailedConsensusReport) throws Exception {

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Individual Predictions</caption>\r\n");

		java.text.DecimalFormat d = new java.text.DecimalFormat("0.00");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Method</th>\n");

		if (isLogMolarEndpoint)
			fw.write("<th>Predicted value<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
		else
			fw.write("<th>Predicted value<br>" + TESTConstants.getMassUnits(endpoint) + "</th>\n");

		// fw.write("<th>Prediction
		// interval<br>"+getMolarLogUnits(endpoint)+"</th>\n");
		fw.write("</tr>\n");

		for (int i = 0; i < methods.size(); i++) {
			if (methods.get(i).equals(TESTConstants.ChoiceConsensus))
				continue;
			fw.write("<tr>\n");

			String filename = "PredictionResults";
			filename += methods.get(i).replaceAll(" ", "");
			filename += ".html";

			fw.write("<td>" + methods.get(i) + "</td>\n");

			// System.out.println(methods.get(i));

			double pred = predictions.get(i);

			fw.write("<td align=\"center\">");

			if (createDetailedConsensusReport)
				fw.write("<a href=\"" + filename + "\">");
			if (pred != -9999) {
				fw.write(d.format(predictions.get(i)));
				// fw.write(" &plusmn; "+d.format(uncertainties.get(i)));
			} else {
				fw.write("N/A");
			}
			if (createDetailedConsensusReport)
				fw.write("</a>");
			fw.write("</td>\n");

			//

			fw.write("</tr>\n");
		}
		fw.write("</table>\n");
	}

	private void WriteMainResultsTable(String CAS, String method, FileWriter fw, String endpoint, Lookup.ExpRecord er, TestChemical chemical, Vector resultsVector, double MW, String message,
			ReportOptions options) throws Exception {

		if (isBinaryEndpoint)
			this.WriteBinaryPredictionTable(fw, CAS, endpoint, method, er, chemical.getPredictedValue(), MW, message);
		else {
			// this.WritePredictionResultsTable2(method,fw, endpoint,
			// er,chemical, MW,message);
			this.WriteMainTable(fw, CAS, chemical.getPredictedValue(), chemical.getPredictedUncertainty(), endpoint, method, MW, er, message);
		}
		if (resultsVector == null)
			return;

//		OptimalResults or = null;
//
//		if (resultsVector.size() == 1)
//			or = (OptimalResults) resultsVector.get(0);

		// if (resultsVector.size()>=1 &&
		// !message.equals(this.messageMissingFragments)) {
		// fw.write("<br><br>\r\n");
		//
		// fw.write("<table border=1 cellpadding=10 cellspacing=0>\r\n");
		// fw.write("<tr>\r\n");
		//
		// fw.write("<td>\r\n");
		// this.WriteClusterModelTable(fw,endpoint,resultsVector, chemical);
		// fw.write("</td>\r\n");
		//
		// fw.write("<td align=middle>Test chemical<br><img
		// src=\"../StructureData/structure.png\" height="+imgSize+"
		// width="+imgSize+"></td>\r\n");
		//
		// fw.write("</tr>\r\n");
		// fw.write("</table>\r\n");
		//
		// } else if (message.equals(this.messageMissingFragments)) {
		// fw.write("<br><br><table border=1 cellpadding=10
		// cellspacing=0>\r\n");
		// fw.write("<tr>\r\n");
		// fw.write("<td align=middle>Test chemical<br><img
		// src=\"../StructureData/structure.png\" height="+imgSize+"
		// width="+imgSize+"></td>\r\n");
		// fw.write("</tr>\r\n");
		// fw.write("</table>\r\n");
		// }

		if (resultsVector.size() >= 1) {
			fw.write("<br><br>\r\n");

			fw.write("<table border=1 cellpadding=10 cellspacing=0>\r\n");
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");
			this.WriteClusterModelTable(fw, endpoint, method, resultsVector, chemical);
			fw.write("</td>\r\n");

			fw.write("<td align=middle>Test chemical<br><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + imgSize + "></td>\r\n");

			fw.write("</tr>\r\n");
			fw.write("</table>\r\n");

		}

	}

	private void WriteMainPage(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String abbrev, Lookup.ExpRecord er, Vector resultsVector,
			Vector invalidResultsVector, double MW, String message, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, ReportOptions options) throws Exception {
		String outputfilename = "PredictionResults";

		outputfilename += method.replaceAll(" ", "");
		outputfilename += ".html";

		FileWriter fw = new FileWriter(OutputFolder + File.separator + outputfilename);

		this.WriteHeaderInfo(fw, CAS, endpoint, method);

		fw.write("<h2>Predicted " + endpoint + " for <font color=\"blue\">" + CAS + "</font> from " + method + " method</h2>\n");

		this.WriteMainResultsTable(CAS, method, fw, endpoint, er, chemical, resultsVector, MW, message, options);

		if (!method.equals(TESTConstants.ChoiceFDAMethod)) {
			boolean HaveStatisticallyValidModel = false;
			for (int i = 0; i < invalidResultsVector.size(); i++) {
				OptimalResults or = (OptimalResults) invalidResultsVector.get(i);
				if (or.isValid()) {
					HaveStatisticallyValidModel = true;
					break;
				}
			}

			if (!HaveStatisticallyValidModel) {

				if (!message.equals(this.messageMissingFragments)) {
					if (chemical.getPredictedValue() == -9999) {
						fw.write(messageNoStatisticallyValidModels);
					}
				}
			} else {
				this.WriteInvalidClusters(fw, chemical, invalidResultsVector, endpoint, method);
			}

		}

		fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for test chemical</a></p>\n");

		fw.write("<br><hr>\n");

		double predToxVal = chemical.getPredictedValue();

		this.WriteSimilarChemicals("test", htTestMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, null, null, options);// TODO
																																							// send
																																							// gsid
		fw.write("<br><hr>\n");
		this.WriteSimilarChemicals("training", htTrainMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, null, null, options);// TODO
																																								// for
																																								// now
																																								// dont
																																								// send
																																								// gsid
																																								// lookup
		fw.write("</html>\n");
		fw.close();
	}

	

	

	private void WriteMainConsensus(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, Lookup.ExpRecord er, Vector resultsVector, Vector invalidResultsVector,
			double MW, String message, Vector<String> testSetMatchSetTable, Vector<String> trainSetMatchSetTable, ReportOptions options) throws Exception {
		String outputfilename = "PredictionResults";

		outputfilename += method.replaceAll(" ", "");
		outputfilename += ".html";

		FileWriter fw = new FileWriter(OutputFolder + File.separator + outputfilename);

		this.WriteHeaderInfo(fw, CAS, endpoint, method);

		fw.write("<h2>Predicted " + endpoint + " for <font color=\"blue\">" + CAS + "</font> from " + method + " method</h2>\n");

		this.WriteMainResultsTable(CAS, method, fw, endpoint, er, chemical, resultsVector, MW, message, options);

		if (!method.equals(TESTConstants.ChoiceFDAMethod))
			this.WriteInvalidClusters(fw, chemical, invalidResultsVector, endpoint, method);

		fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for test chemical</a></p>\n");

		fw.write("<br><hr>\n");

		for (int i = 0; i < testSetMatchSetTable.size(); i++)
			fw.write(testSetMatchSetTable.get(i));

		fw.write("</html>\n");

		fw.close();

	}

	private void WriteInvalidClusters(FileWriter fw, TestChemical chemical, Vector<OptimalResults> invalidClusters, String endpoint, String method) throws IOException {

		// if (chemical.getInvalidClusters()==null ||
		// chemical.getInvalidClusters().size()==0) return;

		boolean HaveStatisticallyValidModel = false;

		if (invalidClusters == null)
			return;

		fw.write("<br><br><table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption><font color=darkred>Cluster models with violated constraints</font</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Cluster Model</th>\n");
		fw.write("<th>Test chemical <br>descriptor values</th>\n");

		if (!isBinaryEndpoint) {
			String units;
			if (isLogMolarEndpoint) {
				units = TESTConstants.getMolarLogUnits(endpoint);
			} else {
				units = TESTConstants.getMassUnits(endpoint);
			}
			fw.write("<th>Prediction interval<br>" + units + "</th>\n");

			fw.write("<th>r<sup>2</sup></th>\n");
			fw.write("<th>q<sup>2</sup></th>\n");
		} else {
			fw.write("<th>Predicted value<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
			fw.write("<th>Concordance</th>\n");
			fw.write("<th>Sensitivity</th>\n");
			fw.write("<th>Specificity</th>\n");
		}

		fw.write("<th># chemicals</th>\n");
		fw.write("<th>Message</th>\n");
		fw.write("</tr>\n");

		java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
		java.text.DecimalFormat d3 = new java.text.DecimalFormat("0.000");

		for (int i = 0; i < chemical.getInvalidClusters().size(); i++) {

			int num = (Integer) chemical.getInvalidClusters().get(i);
			String msg = (String) chemical.getInvalidErrorMessages().get(i);

			OptimalResults or = this.GetResults(num, invalidClusters);
			// System.out.println(num);

			if (!or.isValid())
				continue;

			fw.write("<tr>\n");

			// System.out.println(i+"\t"+num+"\t"+msg+"\t"+or.numChemicals);

			if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) { // only									// happens
																				// for
																				// group
																				// contribution
																				// model:
				fw.write("<td align=\"center\"><a href=\"../../ClusterFiles/" + endpoint + "/GroupContributionCluster.html\">Group contribution" + "</a></td>\n");
				fw.write("<td align=\"center\"><a href=\"ClusterFiles/DescriptorsGroupContribution.html\">Descriptors</a></td>\n");
			} else {
				fw.write("<td align=\"center\"><a href=\"../../ClusterFiles/" + endpoint + "/" + num + ".html\">" + num + "</a></td>\n");
				fw.write("<td align=\"center\"><a href=\"ClusterFiles/Descriptors" + or.getClusterNumber() + ".html\">Descriptors</a></td>\n");
			}

			double predToxValue = -9999;
			double predUncertainty = -9999;
			try {
				predToxValue = chemical.calculateToxicValue(or);
				predUncertainty = chemical.calculateUncertainty(or);
			} catch (Exception ex) {
				logger.catching(ex);
			}

			if (isBinaryEndpoint) {
				fw.write("<td align=\"center\">" + d2.format(predToxValue));
				fw.write("</td>\n");

				fw.write("<td align=\"center\">" + d3.format(or.getConcordance()) + "</td>\r\n");
				fw.write("<td align=\"center\">" + d3.format(or.getSensitivity()) + "</td>\r\n");
				fw.write("<td align=\"center\">" + d3.format(or.getSpecificity()) + "</td>\r\n");
			} else {

				try {
					fw.write("<td align=\"center\">" + d2.format(predToxValue));
					fw.write(" &plusmn; " + d2.format(predUncertainty) + "</td>\n");
					fw.write("</td>\n");

				} catch (Exception e) {
					fw.write("<td>N/A (error)</td>\n");
				}

				fw.write("<td>" + d3.format(or.getR2()) + "</td>\r\n");
				fw.write("<td>" + d3.format(or.getQ2()) + "</td>\r\n");
			}
			fw.write("<td align=\"center\">" + or.getNumChemicals() + "</td>\r\n");

			// fw.write("<td>"+chemical.getInvalidClusters().get(i)+"</td>\n");
			fw.write("<td>" + msg + "</td>\n");

			fw.write("</tr>\n");
		}

		fw.write("</table>\n");

		// possible error messages:
		// "Fragment constraint not met"
		// "Model ellipsoid constraint not met"
		// "Rmax constraint not met"
		// "Model is not statistically valid"
		// TODO add description to web page for these error messages- either
		// alert or title mouseover or link to description page

	}

	private OptimalResults GetResults(int num, Vector vResults) {

		for (int i = 0; i < vResults.size(); i++) {
			OptimalResults results = (OptimalResults) vResults.get(i);

			if (results.getClusterNumber() == num) {
				return results;
			}
		}
		return null;

	}

	// private void WriteMainResultsTableFDA(FileWriter fw, TestChemical
	// chemical,String endpoint,
	// double ExpToxVal,double PredToxVal,double PredToxUnc,String expCAS,
	// OptimalResults or,
	// String clusterName, String clusterNameDisplay,boolean LinkModel) throws
	// Exception {
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	//
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.000");
	//
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	// fw.write("<th>Parameter</th>\n");
	// fw.write("<th>Value</th>\n");
	// fw.write("</tr>\n");
	//
	// fw.write("<tr>\n");
	// fw.write("<td>Endpoint</td>\n");
	// fw.write("<td>"+endpoint+"</td>\n");
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	// fw.write("<td>Toxicity units</td>\n");
	//
	//
	// if (endpoint.equals(TESTConstants.ChoiceFHM_LC50)) {
	// fw.write("<td align=\"center\">-Log (mol/L)</td>\n");
	// } else if (endpoint.equals(TESTConstants.ChoiceTP_IGC50)) {
	// fw.write("<td align=\"center\">-Log (mmol/L)</td>\n");
	// } if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
	// fw.write("<td align=\"center\">-Log (mol/kg)</td>\n");
	// }
	//
	//
	//// fw.write("<td>"+endpoint+"</td>\n");
	//
	// fw.write("</tr>\n\n");
	//
	//
	//
	//// *********************************************************************************
	// fw.write("<tr>\n");
	//
	// if (ExpToxVal ==-9999) {
	// fw.write("<td>Experimental value</td>\n");
	// } else {
	// fw.write("<td>Experimental value (CAS#="+expCAS+")</td>\n");
	// }
	//
	//
	//// if (endpoint_abbrev.equals(TESTConstants.ChoiceFHM_LC50)) {
	//// fw.write("<td>Experimental toxicity -Log (LC50 mol/L)</td>\n");
	//// } else if (endpoint_abbrev.equals(TESTConstants.ChoiceTP_IGC50)) {
	//// fw.write("<td>Experimental toxicity -Log (IGC50 mmol/L)</td>\n");
	//// } else if (endpoint_abbrev.equals(ChoiceRat_LD50)) {
	//// fw.write("<td>Experimental toxicity -Log (LD50 mol/kg)</td>\n");
	//// } else {
	//// fw.write("<td>Experimental toxicity</td>\n");
	//// }
	//
	//
	// if (ExpToxVal ==-9999)
	// fw.write("<td align=\"center\">N/A</td>\n");
	// else
	// fw.write("<td align=\"center\">"+d.format(ExpToxVal)+"</td>\n");
	//
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>Predicted toxicity</td>\n");
	//
	//// if (endpoint_abbrev.equals(TESTConstants.ChoiceFHM_LC50)) {
	//// fw.write("<td>Predicted toxicity -Log (LC50 mol/L)</td>\n");
	////
	//// } else if (endpoint_abbrev.equals(TESTConstants.ChoiceTP_IGC50)) {
	//// fw.write("<td>Predicted toxicity -Log (IGC50 mmol/L)</td>\n");
	////
	//// } else {
	//// fw.write("<td>Predicted toxicity</td>\n");
	//// }
	//
	//
	// if (PredToxVal==-9999)
	// fw.write("<td>Toxicity value could not be predicted</td>\n");
	// else {
	// fw.write("<td align=\"center\">"+d.format(PredToxVal)+"</td>\n");
	// // fw.write("<td align=\"center\">"+d.format(PredToxVal)+" &plusmn
	// "+d.format(PredToxUncertainty));
	//// double minval=PredToxVal-PredToxUncertainty;
	//// double maxval=PredToxVal+PredToxUncertainty;
	//// fw.write(d.format(minval)+" &le; toxicity &le;
	// "+d.format(maxval)+"</td>\n");
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>Predicted 90% confidence interval</td>\n");
	//
	// if (PredToxVal==-9999)
	// fw.write("<td align=\"center\">N/A</td>\n");
	// else {
	// double minval=PredToxVal-PredToxUnc; //molar value
	// double maxval=PredToxVal+PredToxUnc; //molar value
	//
	// fw.write("<td align=\"center\">"+d.format(minval)+" &le; Tox &le;
	// "+d.format(maxval)+"</td>\n");//molar
	//// fw.write("<td
	// align=\"center\">"+d.format(PredToxUncertainty)+"</td>\n");
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>r<sup>2</sup></td>\n");
	//
	// if (PredToxVal==-9999)
	// fw.write("<td align=\"center\">N/A</td>\n");
	// else {
	// fw.write("<td align=\"center\">" + d.format(or.r2) + "</td>\n");
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>q<sup>2</sup></td>\n");
	//
	// if (PredToxVal==-9999)
	// fw.write("<td align=\"center\">N/A</td>\n");
	// else {
	// if (or.q2>0) {
	// fw.write("<td align=\"center\">" + d.format(or.q2) + "</td>\n");
	// } else {
	// fw.write("<td align=\"center\">N/A</td>\n");
	// }
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	// fw.write("<tr>\n");
	//
	// fw.write("<td>#chemicals</td>\n");
	//
	// if (PredToxVal==-9999)
	// fw.write("<td align=\"center\">N/A</td>\n");
	// else {
	// fw.write("<td align=\"center\">" + or.numChemicals + "</td>\n");
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//// *********************************************************************************
	//
	// fw.write("<tr>\n");
	// fw.write("<td>Model</td>\n");
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");
	// } else {
	// if (LinkModel)
	// fw.write("<td align=\"center\"><a
	// href=\"PredictionResults"+clusterName+".html\">"+clusterNameDisplay+"</a></td>\n");
	// else
	// fw.write("<td align=\"center\">"+clusterNameDisplay+"</td>\n");
	//
	// }
	//
	// fw.write("</tr>\n\n");
	//
	//// if (or.q2 == -1)
	//// fw.write("<td>N/A</td>\n");
	//// else
	//// fw.write("<td>" + d.format(or.q2) + "</td>\n");
	////
	//// fw.write("<td>" + or.numChemicals + "</td>\n");
	//
	//
	//
	// fw.write("</table>\n");
	//
	//
	// }

	static void WriteOverallModelTableForClusterPage(FileWriter fw, Instance chemical, String endpoint, OptimalResults or, String clusterName, String clusterNameDisplay, boolean LinkModel)
			throws Exception {

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		java.text.DecimalFormat d = new java.text.DecimalFormat("0.000");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Parameter</th>\n");
		fw.write("<th>Value</th>\n");
		fw.write("</tr>\n");

		fw.write("<tr>\n");
		fw.write("<td>Endpoint</td>\n");

		String endpoint2 = endpoint.replace("50", "<sub>50</sub>");
		fw.write("<td>" + endpoint2 + "</td>\n");

		fw.write("</tr>\n\n");

		// *********************************************************************************

		fw.write("<tr>\n");

		fw.write("<td>r<sup>2</sup></td>\n");

		// if (PredToxVal==-9999)
		// fw.write("<td align=\"center\">N/A</td>\n");
		// else {
		fw.write("<td align=\"center\">" + d.format(or.getR2()) + "</td>\n");
		// }

		fw.write("</tr>\n\n");

		// *********************************************************************************

		fw.write("<tr>\n");

		fw.write("<td>q<sup>2</sup></td>\n");

		// if (PredToxVal==-9999)
		// fw.write("<td align=\"center\">N/A</td>\n");
		// else {
		if (or.getQ2() > 0) {
			fw.write("<td align=\"center\">" + d.format(or.getQ2()) + "</td>\n");
		} else {
			fw.write("<td align=\"center\">N/A</td>\n");
		}
		// }

		fw.write("</tr>\n\n");

		// *********************************************************************************
		fw.write("<tr>\n");

		fw.write("<td>#chemicals</td>\n");

		// if (PredToxVal==-9999)
		// fw.write("<td align=\"center\">N/A</td>\n");
		// else {
		fw.write("<td align=\"center\">" + or.getNumChemicals() + "</td>\n");
		// }

		fw.write("</tr>\n\n");

		// *********************************************************************************

		fw.write("<tr>\n");
		fw.write("<td>Model</td>\n");

		// if (PredToxVal==-9999) {
		// fw.write("<td align=\"center\">N/A</td>\n");
		// } else {
		if (LinkModel)
			fw.write("<td align=\"center\"><a href=\"PredictionResults" + clusterName + ".html\">" + clusterNameDisplay + "</a></td>\n");
		else
			fw.write("<td align=\"center\">" + clusterNameDisplay + "</td>\n");

		// }

		fw.write("</tr>\n\n");

		// if (or.q2 == -1)
		// fw.write("<td>N/A</td>\n");
		// else
		// fw.write("<td>" + d.format(or.q2) + "</td>\n");
		//
		// fw.write("<td>" + or.numChemicals + "</td>\n");

		fw.write("</table>\n");

	}

	// bob99
	static void WriteOverallModelTableForClusterPageBinary(FileWriter fw, String endpoint, OptimalResults or) throws Exception {

		java.text.DecimalFormat df = new java.text.DecimalFormat("0.000");

		// fw.write("<table border=\"1\" cellpadding=\"3\"
		// cellspacing=\"0\">\n");
		// fw.write("<caption>Prediction Statistics</caption>\r\n");
		// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		// fw.write("<th>Parameter</th>\n");
		// fw.write("<th>Value</th>\n");
		// fw.write("</tr>\n");
		//
		// fw.write("<tr>\n");
		// fw.write("<td>Endpoint</td>\n");
		//
		// String endpoint2=endpoint.replace("50","<sub>50</sub>");
		// fw.write("<td>"+endpoint2+"</td>\n");
		//
		// fw.write("</tr>\n\n");
		//
		//// *********************************************************************************
		//
		// fw.write("<tr>\n");
		// fw.write("<td>Concordance</td>\n");
		// fw.write("<td align=\"center\">" + df.format(or.Concordance) +
		// "</td>\n");
		// fw.write("</tr>\n\n");
		//
		//// *********************************************************************************
		//
		// fw.write("<tr>\n");
		// fw.write("<td>Sensitivity</td>\n");
		// fw.write("<td align=\"center\">" + df.format(or.Sensitivity) +
		// "</td>\n");
		// fw.write("</tr>\n\n");
		//
		//// *********************************************************************************
		// fw.write("<tr>\n");
		// fw.write("<td>Specificity</td>\n");
		// fw.write("<td align=\"center\">" + df.format(or.Specificity) +
		// "</td>\n");
		// fw.write("</tr>\n\n");
		//
		//// *********************************************************************************
		// fw.write("<tr>\n");
		// fw.write("<td>#chemicals</td>\n");
		// fw.write("<td align=\"center\">" + or.numChemicals + "</td>\n");
		// fw.write("</tr>\n\n");
		//
		//// *********************************************************************************
		//
		// fw.write("<tr>\n");
		// fw.write("<td>Model</td>\n");
		//
		// if (LinkModel)
		// fw.write("<td align=\"center\"><a
		// href=\"PredictionResults"+clusterName+".html\">"+clusterNameDisplay+"</a></td>\n");
		// else
		// fw.write("<td align=\"center\">"+clusterNameDisplay+"</td>\n");
		//
		// fw.write("</tr>\n\n");
		// fw.write("</table>\n");

		// ***********************************************************************************
		int predCount = 0;
		int posPredcount = 0;
		int negPredcount = 0;

		int correctCount = 0;
		int posCorrectCount = 0;
		int negCorrectCount = 0;

		double cutoff = 0.5;
		for (int i = 0; i < or.getObserved().length; i++) {
			double exp = or.getObserved()[i];
			double pred = or.getPredicted()[i];

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
				posPredcount++;
			else if (strExp.equals("NC"))
				negPredcount++;

			if (strExp.equals(strPred)) {
				correctCount++;
				if (strExp.equals("C"))
					posCorrectCount++;
				else if (strExp.equals("NC"))
					negCorrectCount++;

			}
		}

		double concordance = correctCount / (double) predCount;
		double posConcordance = posCorrectCount / (double) posPredcount;
		double negConcordance = negCorrectCount / (double) negPredcount;

		fw.write("<table border=1 cellpadding=3 cellspacing=0>\n");
		fw.write("<caption>Prediction statistics</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Endpoint</th>\r\n");
		fw.write("<th>Concordance</th>\r\n");
		fw.write("<th>Sensitivity</th>\r\n");
		fw.write("<th>Specificity</th>\r\n");
		fw.write("<th>#chemicals</th>\r\n");
		fw.write("</tr>\r\n");

		fw.write("<tr>\r\n");

		fw.write("<td>" + endpoint.replace("50", "<sub>50</sub>") + "</td>\n");

		if (predCount > 0) {
			fw.write("<td align=\"center\">" + df.format(concordance) + "<br>(" + correctCount + " out of " + predCount + ")</td>\r\n");
		} else {
			fw.write("<td align=\"center\">N/A</td>\r\n");
		}

		if (posPredcount > 0)
			fw.write("<td align=\"center\" >" + df.format(posConcordance) + "<br>(" + posCorrectCount + " out of " + posPredcount + ")</td>\r\n");
		else
			fw.write("<td align=\"center\">N/A</td>\r\n");

		if (negPredcount > 0)
			fw.write("<td align=\"center\">" + df.format(negConcordance) + "<br>(" + negCorrectCount + " out of " + negPredcount + ")</td>\r\n");
		else
			fw.write("<td align=\"center\">N/A</td>\r\n");

		fw.write("<td align=\"center\">" + or.getNumChemicals() + "</td>\r\n");

		fw.write("</tr>\r\n");

		fw.write("</table>\r\n");

	}

	private void WriteResultsWebPage(String method, Instance chemical, String filename, String ClusterFolder, String CAS, String endpoint, OptimalResults or, ReportOptions options) {

		try {

			File fol = new File(ClusterFolder);
			if (!fol.exists())
				fol.mkdir();

			FileWriter fw = new FileWriter(ClusterFolder + File.separator + filename);

			fw.write("<html>\n");
			fw.write("<head>\n");

			String clusterName = "";
			String clusterNameDisplay = "";

			if (method.equals(TESTConstants.ChoiceFDAMethod)) {
				clusterName = "FDACluster";
				clusterNameDisplay = "FDA Model";
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
				clusterName = or.getClusterNumber() + "";
				clusterNameDisplay = "Group contribution model";
			} else if (method.equals(TESTConstants.ChoiceHierarchicalMethod) || method.equals(TESTConstants.ChoiceSingleModelMethod)) {
				clusterName = or.getClusterNumber() + "";
				clusterNameDisplay = "Model # " + or.getClusterNumber();
			}

			fw.write("<title>" + clusterNameDisplay + "</title>");
			fw.write("</head>\n");

			fw.write("<h3>" + clusterNameDisplay + "</h3>");

			fw.write("\n<table>\n");
			fw.write("<tr>\n");

			fw.write("<td>\n");

			WriteOverallModelTableForClusterPage(fw, chemical, endpoint, or, clusterName, clusterNameDisplay, false);

			fw.write("</td>\n");

			fw.write("<td>\n");
			if (method.equals(TESTConstants.ChoiceFDAMethod)) {
				fw.write("<img src=\"" + ReportUtils.getImageSrc(options, "FDA_QSAR_Plot.png") + "\">");
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
				fw.write("<img src=\"" + ReportUtils.getImageSrc(options, "QSAR_PlotGroupContributionCluster.png") + "\">");
			} else if (method.equals(TESTConstants.ChoiceSingleModelMethod) || method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
				fw.write("<img src=\"" + ReportUtils.getImageSrc(options, "QSAR_Plot" + or.getClusterNumber() + ".png") + "\">");
			}
			fw.write("</td>\n");

			fw.write("</tr>\n");
			fw.write("</table>\n");

			WriteModelTable(endpoint, or, fw);

			String imagepath = "";

			if (method.equals(TESTConstants.ChoiceFDAMethod))
				imagepath = "../../../images";// FDA method
			else
				imagepath = "../../images";

			this.WriteModelChemicalInfo(CAS, chemical, endpoint, method, imagepath, ClusterFolder, or, fw, options);

			fw.write("</html>\n");

			fw.close();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	

	private void WriteResultsWebPageBinary(String method, Instance chemical, String filename, String ClusterFolder, String CAS, String endpoint, OptimalResults or, ReportOptions options) {

		try {

			File fol = new File(ClusterFolder);
			if (!fol.exists())
				fol.mkdir();

			FileWriter fw = new FileWriter(ClusterFolder + File.separator + filename);

			fw.write("<html>\n");
			fw.write("<head>\n");

			String clusterName = "";
			String clusterNameDisplay = "";

			if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
				clusterName = "FDACluster";
				clusterNameDisplay = "FDA Model";
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// group
																					// contribution
				clusterName = or.getClusterNumber() + "";
				clusterNameDisplay = "Group contribution model";
			} else {// hierarchical or single model
				clusterName = or.getClusterNumber() + "";
				clusterNameDisplay = "Model # " + or.getClusterNumber();
			}

			fw.write("<title>" + clusterNameDisplay + "</title>");

			fw.write("</head>\n");

			fw.write("<h3>" + clusterNameDisplay + "</h3>");

			WriteOverallModelTableForClusterPageBinary(fw, endpoint, or);

			fw.write("<br><br>\r\n");
			WriteModelTable(endpoint, or, fw);

			String imagepath = "";

			if (method.equals(TESTConstants.ChoiceFDAMethod))
				imagepath = "../../../images";// FDA method
			else
				imagepath = "../../images";

			this.WriteModelChemicalInfo(CAS, chemical, endpoint, method, imagepath, ClusterFolder, or, fw, options);

			fw.write("</html>\n");

			fw.close();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	

	private void WriteModelChemicalInfo(String CAS, Instance chemical, String endpoint, String method, String imagepath, String ClusterFolder, OptimalResults or, FileWriter fw,
			ReportOptions options) {
		String name2 = "";// clusterNameDisplay+" descriptors.txt";
		String name3 = "";// clusterNameDisplay+" descriptors.txt";

		if (method.equals(TESTConstants.ChoiceFDAMethod)) {// FDA
			name2 = "FDA model fit results by chemical";
			name3 = "FDA model training set descriptors";
		} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {// group
																				// contribution
			name2 = "Group contribution model fit results by chemical";
			name3 = "Group contribution model training set descriptors";
		} else {// hierarchical or single model
			name2 = "Model " + or.getClusterNumber() + " fit results by chemical";
			name3 = "Model " + or.getClusterNumber() + " training set descriptors";
		}

		try {
			String ext2 = ".html";
			String filepath2 = ClusterFolder + File.separator + name2 + ".html";
			WriteModelChemicalInfo2(endpoint, imagepath, chemical, or, CAS, filepath2, name2, options);

			String ext3 = ".txt";
			String filepath3 = ClusterFolder + File.separator + name3 + ext3;
			WriteModelChemicalInfo3(endpoint, filepath3, or, "|");

			// String ext3=".html";
			// String filepath3=ClusterFolder+File.separator+name3+ext3;
			// WriteModelChemicalInfo3html(endpoint,filepath3,or,name3);

			fw.write("<a href=\"" + name2 + ext2 + "\">" + name2 + "</a>&nbsp;&nbsp;\r\n");
			fw.write("<a href=\"" + name3 + ext3 + "\">" + name3 + "</a><br>\r\n");

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	

	static void WriteModelTable(String endpoint, OptimalResults or, FileWriter fw) throws IOException {
		fw.write("<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<tr>\n");

		fw.write("<td>\n");
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Model coefficients</caption>\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Coefficient</th>\n");
		fw.write("<th>Definition</th>\n");
		fw.write("<th>Value</th>\n");
		fw.write("<th>Uncertainty*</th>\n");

		fw.write("</tr>\n");

		DecimalFormat df = new DecimalFormat("0.0000");
		double[] bcoeff = or.getBcoeff();
		double[] bcoeffSE = or.getBcoeffSE();

		fw.write("<tr>\n");
		fw.write("<td>Intercept</td>\n");
		fw.write("<td>Model intercept</td>\n");
		fw.write("<td>" + df.format(bcoeff[bcoeff.length - 1]) + "</td>\n");
		fw.write("<td>" + df.format(bcoeffSE[bcoeff.length - 1]) + "</td>\n");

		fw.write("</tr>\n");

		for (int i = 0; i < or.getDescriptorNames().length; i++) {
			double MinVal = bcoeff[i] - bcoeffSE[i];
			double MaxVal = bcoeff[i] + bcoeffSE[i];

			fw.write("<tr>\n");
			fw.write("<td>" + or.getDescriptorNames()[i] + "</td>\n");
			fw.write("<td>" + htVarDef.get(or.getDescriptorNames()[i]) + "</td>\n");
			fw.write("<td>" + df.format(bcoeff[i]) + "</td>\n");
			fw.write("<td>" + df.format(bcoeffSE[i]) + "</td>\n");
			fw.write("</tr>\n");
		}

		fw.write("</table>\n");// close table for regression model details
		fw.write("* value for 90% confidence interval");
		fw.write("</td>\n");

		String model = GetModelEquation(or, endpoint);

		fw.write("<td>\n");
		fw.write("<i>Model equation: </i><br>" + model + "<td>\n");
		fw.write("</td>\n");

		fw.write("</tr>\n");
		fw.write("</table>\n");

		fw.write("<br>\n");

	}

	private static void WriteDescriptorTable(FileWriter fw, OptimalResults or, TestChemical chemical, String endpoint) {
		try {

			DecimalFormat df2 = new DecimalFormat("0.00");
			DecimalFormat df4 = new DecimalFormat("0.0000");

			fw.write("<table>\r\n"); // table to store descriptor table and
										// structure image
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");

			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
			fw.write("<caption>Descriptor Values</caption>\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("<th>Descriptor</th>\n");
			fw.write("<th>Value</th>\n");
			fw.write("<th>Coefficient</th>\n");
			fw.write("<th>Value &#215; Coefficient</th>\n");
			fw.write("</tr>\n");

			double[] coeff = or.getBcoeff();
			int[] descriptors = or.getDescriptors();

			for (int i = 0; i < or.getDescriptorNames().length; i++) {
				double descval = chemical.value(descriptors[i]);

				if (descval != 0) {
					fw.write("<tr style=\"background-color: lightgray\">\n");
					// if
					// (method.equals(TESTConstants.ChoiceGroupContributionMethod)
					// && chemical.value(or.getDescriptors()[i])>0) {
					// fw.write("<tr style=\"background-color: lightgray\">\n");
				} else {
					fw.write("<tr>\n");
				}

				fw.write("\t<td>" + or.getDescriptorNames()[i] + "</td>\n");

				// fw.write("<td align=\"center\">"
				// + df.format(chemical.value(or.getDescriptors()[i]))
				// + "</td>\n");

				fw.write("\t<td align=\"center\">" + df4.format(descval) + "</td>\n");
				fw.write("\t<td align=\"center\">" + df4.format(coeff[i]) + "</td>\n");

				if (descval == 0) {
					fw.write("\t<td align=\"center\">" + df2.format(0) + "</td>\n");
				} else {
					fw.write("\t<td align=\"center\">" + df2.format(descval * coeff[i]) + "</td>\n");
				}

				fw.write("</tr>\n");
			}

			// Intercept:
			fw.write("<tr style=\"background-color: lightgray\">\n");
			fw.write("\t<td>Model intercept</td>\n");
			fw.write("<td align=\"center\">1.0000</td>\n");
			fw.write("<td align=\"center\">" + df2.format(coeff[coeff.length - 1]) + "</td>\n");
			fw.write("<td align=\"center\">" + df2.format(coeff[coeff.length - 1]) + "</td>\n");
			fw.write("</tr>\n");

			// ************************************************************************************************
			// Predicted value:
			double predToxValue = chemical.calculateToxicValue(or);
			String units;
			if (isLogMolarEndpoint) {
				units = TESTConstants.getMolarLogUnits(endpoint);
			} else {
				units = TESTConstants.getMassUnits(endpoint);
			}
			fw.write("<tr>\n");
			fw.write("\t<td><font color=\"blue\">Predicted value " + units + "</font></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td><br></td>\n");
			fw.write("\t<td align=\"center\"><font color=\"blue\">" + df2.format(predToxValue) + "</font></td>\n");
			fw.write("</tr>\n");
			// ************************************************************************************************

			fw.write("</table>\n");
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	// This version doesnt add test chemical to table
	static void WriteModelChemicalInfo2(String endpoint, String imagepath, Instance chemical, OptimalResults or, String CAS, String filepath, String title, ReportOptions options) throws Exception {

		FileWriter fw = new FileWriter(filepath);

		double[] Yexp = or.getObserved();
		double[] Ycalc = or.getPredicted();

		fw.write("<html>\n");

		fw.write("<head><title>" + title + "</title></head>\n");

		fw.write("<br><br><table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>" + title + "</caption>\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Number</th>\n");
		fw.write("<th>Chemical</th>\n");
		fw.write("<th>Structure</th>\n");

		// fw.write("<th>Exp. Toxicity</th>\n");
		// fw.write("<th>Fit Toxicity</th>\n");

		String units;
		if (isLogMolarEndpoint) {
			units = TESTConstants.getMolarLogUnits(endpoint);
		} else {
			units = TESTConstants.getMassUnits(endpoint);
		}
		fw.write("<th>Exp. " + endpoint + "<br>" + units + "</th>\n");
		fw.write("<th>Fit " + endpoint + "<br>" + units + "</th>\n");

		// for (int i=0;i<or.descriptorNames.length;i++) {
		// fw.write("<th>"+or.descriptorNames[i]+"</th>\n");
		// }

		fw.write("</tr>\n");

		java.text.DecimalFormat df3 = new java.text.DecimalFormat("0.000");

		// fw.write("</tr>\n");

		for (int i = 0; i < or.getNumChemicals(); i++) {
			fw.write("<tr>\n");

			fw.write("<td>" + (i + 1) + "</td>\n");

			String CASi = or.getChemicalNames()[i];
			fw.write("<td>" + CASi + "</td>\n");

			fw.write("<td><a href=\"" + imagepath + "/" + CASi + ".png\"><img src=\"" + ReportUtils.getImageSrc(options, imagepath, CASi + ".png") + "\" width=" + imgSize + "></a></td>\n");

			fw.write("<td align=\"center\">" + df3.format(Yexp[i]) + "</td>\n");

			fw.write("<td align=\"center\">" + df3.format(Ycalc[i]) + "</td>\n");

			// for (int j=0;j<or.descriptorNames.length;j++) {
			// fw.write("<td
			// align=\"center\">"+df4.format(or.x[i][j])+"</td>\n");
			// }

			fw.write("</tr>\n");
		}
		fw.write("</table>\n");
		fw.write("</html>\n");
		// fw.write("*Not used in the cluster model\n");

		fw.close();
	}

	// Writes the descriptor values for chemicals in linear regression model
	static void WriteModelChemicalInfo3(String endpoint, String filepath, OptimalResults or, String del) throws Exception {

		FileWriter fw = new FileWriter(filepath);
		double[] Yexp = or.getObserved();
		double[] Ycalc = or.getPredicted();

		String units;
		if (isLogMolarEndpoint) {
			units = TESTConstants.getMolarLogUnits(endpoint);
		} else {
			units = TESTConstants.getMassUnits(endpoint);
		}

		if (!units.equals(""))
			units = " (" + units + ")";

		fw.write("Number" + del + "Chemical" + del + "Exp. " + endpoint + units + del + "Fit " + endpoint + units + del);

		for (int i = 0; i < or.getDescriptorNames().length; i++) {
			fw.write(or.getDescriptorNames()[i]);
			if (i < or.getDescriptorNames().length - 1)
				fw.write(del);
			else
				fw.write("\r\n");
		}

		java.text.DecimalFormat df3 = new java.text.DecimalFormat("0.000");
		java.text.DecimalFormat df4 = new java.text.DecimalFormat("0.0###");

		for (int i = 0; i < or.getNumChemicals(); i++) {
			String CASi = or.getChemicalNames()[i];
			fw.write((i + 1) + del + CASi + del + df3.format(Yexp[i]) + del + df3.format(Ycalc[i]) + del);
			for (int j = 0; j < or.getDescriptorNames().length; j++) {
				fw.write(df4.format(or.getX()[i][j]));
				if (j < or.getDescriptorNames().length - 1)
					fw.write(del);
				else
					fw.write("\r\n");
			}
		}
		fw.close();

	}

	// Writes the descriptor values for chemicals in linear regression model
	private void WriteModelChemicalInfo3html(String endpoint, String filepath, OptimalResults or, String title) throws Exception {

		FileWriter fw = new FileWriter(filepath);

		fw.write("<html>\n");

		fw.write("<head><title>" + title + "</title></head>\n");

		fw.write("<br><br><table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>" + title + "</caption>\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Number</th>\n");
		fw.write("<th>Chemical</th>\n");

		String units;
		if (isLogMolarEndpoint) {
			units = TESTConstants.getMolarLogUnits(endpoint);
		} else {
			units = TESTConstants.getMassUnits(endpoint);
		}
		if (!units.equals(""))
			units = " (" + units + ")";

		fw.write("<th>Exp. " + endpoint + "<br>" + units + "</th>\n");
		fw.write("<th>Fit " + endpoint + "<br>" + units + "</th>\n");

		for (int i = 0; i < or.getDescriptorNames().length; i++) {
			fw.write("<th>" + or.getDescriptorNames()[i] + "</th>\n");
		}
		fw.write("</tr>\n");

		double[] Yexp = or.getObserved();
		double[] Ycalc = or.getPredicted();

		java.text.DecimalFormat df3 = new java.text.DecimalFormat("0.000");
		java.text.DecimalFormat df4 = new java.text.DecimalFormat("0.0000");

		for (int i = 0; i < or.getNumChemicals(); i++) {
			fw.write("<tr>\n");

			fw.write("<td>" + (i + 1) + "</td>\n");

			String CASi = or.getChemicalNames()[i];
			fw.write("<td>" + CASi + "</td>\n");

			fw.write("<td align=\"center\">" + df3.format(Yexp[i]) + "</td>\n");
			fw.write("<td align=\"center\">" + df3.format(Ycalc[i]) + "</td>\n");

			for (int j = 0; j < or.getDescriptorNames().length; j++) {
				fw.write("<td align=\"center\">" + df4.format(or.getX()[i][j]) + "</td>\n");
			}

			fw.write("</tr>\n");

		}
		fw.write("</table>\n");
		fw.write("</html>\n");

		fw.close();

	}

	public static String GetModelEquation(OptimalResults or, String endpoint) {

		DecimalFormat df2 = new DecimalFormat("0.0000");

		// String model="Toxicity = ";
		String model = endpoint + " = ";

		for (int i = 0; i < or.getDescriptorNames().length; i++) {
			// System.out.print(i+"\t"+or.descriptorNames[i]+"\t");

			if (i == 0) {
				model += df2.format(or.getBcoeff()[i]) + "&#215;(" + or.getDescriptorNames()[i] + ")";

			} else {
				if (or.getBcoeff()[i] > 0) {
					model += " + " + df2.format(or.getBcoeff()[i]) + "&#215;(" + or.getDescriptorNames()[i] + ")";
				} else {
					model += " - " + df2.format(Math.abs(or.getBcoeff()[i])) + "&#215;(" + or.getDescriptorNames()[i] + ")";
				}
			}
		}

		if (or.getBcoeff()[or.getBcoeff().length - 1] > 0) {
			model += " + " + df2.format(or.getBcoeff()[or.getBcoeff().length - 1]);
		} else {
			model += " - " + df2.format(Math.abs(or.getBcoeff()[or.getBcoeff().length - 1]));
		}

		
		return model;

	}

	public void WriteExperimentalValuePage(String OutputFolder, String expCAS, String endpoint) throws IOException {

		FileWriter fw = new FileWriter(OutputFolder + File.separator + "experimentalvalue.html");

		fw.write("<html>\r\n");

		fw.write("<head>\n");
		fw.write("<title>Experimental data for " + expCAS + "\n");
		fw.write("</title>\n");
		fw.write("</head>\n");

		fw.write("<h3>Experimental data for " + expCAS + "</h3>\n");

		if (endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
			this.WriteExperimentalInfoLD50(fw, expCAS);
		}

		fw.write("</html>\r\n");
		fw.close();

	}

	private void WriteExperimentalInfoLD50(FileWriter fw, String CAS) throws IOException {

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Parameter</th>\n");
		fw.write("<th>Value</th>\n");
		fw.write("</tr>\n");

		fw.write("<tr>\n");
		fw.write("<td>Source</td>\n");
		fw.write("<td><a href=\"http://chem.sis.nlm.nih.gov/chemidplus/\">National Library of Medicine (ChemidPlus)</a>" + "<br>Search for Registry Number = " + CAS
				+ " and then click on the toxicity button</td>\n");
		fw.write("</tr>\n");

		fw.write("</table>\n");
	}

	/**
	 * This method writes out descriptors for test chemical for a given
	 * OptimalResults object (but doesnt write out cluster descriptor values)
	 * 
	 * @param chemical
	 * @param or
	 * @param fw
	 * @param Ycalc
	 * @param cas_number
	 * @param YexpTest
	 * @param YcalcTest
	 * @throws IOException
	 */
	static void WriteTestChemicalDescriptorsForClusterModel(TestChemical chemical, OptimalResults or, String folder, String filename, String endpoint, String title, ReportOptions options) {

		try {
			FileWriter fw = new FileWriter(folder + File.separator + filename);


			fw.write("<html>\n");

			fw.write("<head>\n");

			fw.write("<title>" + title + "</title>\r\n");

			fw.write("</head>\n");

			fw.write("<h3>" + title + "</h3>\r\n");

			fw.write("<table>\r\n");
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");
			WriteDescriptorTable(fw, or, chemical, endpoint);
			fw.write("</td>\r\n");

			fw.write("<td>\r\n");
			fw.write("<img src=\"" + ReportUtils.getImageSrc(options, "../../StructureData/structure.png") + "\">" + "\r\n");
			fw.write("</td>\r\n");

			fw.write("</tr>\r\n");
			fw.write("</table>\r\n");

			fw.write("</html>\n");
			fw.flush();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	static void CreateStructureImages(OptimalResults or, String DestFolder) {
		if (or == null)
			return;

		ToxPredictor.Utilities.GetStructureImagesFromJar g = new ToxPredictor.Utilities.GetStructureImagesFromJar();

		ParseChemidplus p = new ParseChemidplus();

		File imageFolder = new File(DestFolder);
		if (!imageFolder.exists())
			imageFolder.mkdir();

		// System.out.print("Creating images...");
		for (int i = 0; i < or.getChemicalNames().length; i++) {
			String CASi = or.getChemicalNames()[i];
			// System.out.println(CASi);
			IAtomContainer moleculei = null;

			File imageFile = new File(imageFolder.getAbsolutePath() + "/" + CASi + ".png");

			if (!imageFile.exists()) {

				int ret = g.GetImageFileFromJar(CASi, imageFolder.getAbsolutePath());
				// System.out.println(CASi+"\t"+ret);

				if (ret == -1) {
					moleculei = p.LoadChemicalFromMolFileInJar("ValidatedStructures2d/" + CASi + ".mol");
					if (moleculei != null) {
						// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei,CASi,ImageFolder.getAbsolutePath(),false,true,true,200);
						ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei, CASi, imageFolder.getAbsolutePath());
					} else {
						moleculei = p.GetBestMolecule(CASi);
						if (moleculei != null) {
							// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei,CASi,ImageFolder.getAbsolutePath(),false,true,true,200);
							ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei, CASi, imageFolder.getAbsolutePath());

						}
					}
				}

			}
		}
		// System.out.print("done\n");
	}

	static void CreateQSARPlot(OptimalResults or, String filename, String DestFolder, String endpoint) {

		double[] Yexp = or.getObserved();
		double[] Ycalc = or.getPredicted();

		// System.out.println(or.clusterNumber);
		// for (int i=0;i<or.observed.length;i++) {
		// System.out.println(i+"\t"+Yexp[i]+"\t"+Ycalc[i]);
		// }
		// System.out.println(filename);

		String title;

		if (isLogMolarEndpoint) {
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
		fc.WriteImageToFile(filename, DestFolder);
	}

	public static double getToxValMass(String endpoint, double toxValMolar, double MW) {

		// *add endpoint*
		if (endpoint.equals(TESTConstants.ChoiceBCF) || endpoint.equals(TESTConstants.ChoiceViscosity) || endpoint.equals(TESTConstants.ChoiceVaporPressure)
				|| endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
			return Math.pow(10, toxValMolar);
		} else {
			return Math.pow(10, -toxValMolar) * MW * 1000.0;
		}
	}

	public void WriteResultsWebPageNN(TestChemical chemical, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String method,
			Lookup.ExpRecord er, double predToxVal, double MW, String msg, Instances cc, Vector SimCoeffCluster, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch,
			ReportOptions options) {

		try {
			this.isBinaryEndpoint = isBinaryEndpoint;
			this.isLogMolarEndpoint = isLogMolarEndpoint;

			FileWriter fw = new FileWriter(OutputFolder + File.separator + "PredictionResultsNearestneighbor.html");

			this.WriteHeaderInfo(fw, CAS, endpoint, method);

			fw.write("<h2>Predicted " + endpoint + " for " + CAS + " for Nearest neighbor method</h2>\n");

			// if (expCAS != null) {
			// WriteExperimentalValuePage(OutputFolder, expCAS, endpoint);
			// }

			// double ExpToxValMass=-9999;
			// double PredToxValMass=-9999;
			//
			// if (predToxVal!=-9999) {
			// PredToxValMass=this.getToxValMass(endpoint,predToxVal,MW);
			// }
			//
			// if (er.expToxValue!=-9999) {
			// ExpToxValMass=this.getToxValMass(endpoint,er.expToxValue,MW);
			// }

			String message;
			if (cc == null)
				message = "Note: Insufficient nearest neighbors in the training set were available to make a prediction";
			else
				message = "OK";

			// fw.write("<table><tr><td>\r\n");
			if (isBinaryEndpoint) {
				this.WriteBinaryPredictionTable(fw, CAS, endpoint, method, er, predToxVal, MW, message);
			} else {
				this.WriteMainTable(fw, CAS, predToxVal, -9999, endpoint, method, MW, er, message);
			}
			// fw.write("</td>\r\n");
			// fw.write("<td><img src=\"../structuredata/structure.png\"
			// height="+imgSize+" width="+imgSize+"></td>\r\n");
			// fw.write("</tr></table>\r\n");

			// WriteMainTable(fw,CAS,predToxVal,-9999,endpoint,method,MW,er,"OK");

			if (cc != null) {
				String filepath = OutputFolder + File.separator + "NearestNeighborsFromTrainingSet.html";
				this.WriteClusterTableNN(fw, CAS, endpoint, er.expToxValue, cc, SimCoeffCluster, true, options);
				// fw.write("<br><br><a
				// href=\"NearestNeighborsFromTrainingSet.html\">Nearest
				// neighbors from training set used to make the
				// prediction</a><br>\n");
			}

			fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for test chemical</a></p>\n");
			fw.write("<br><hr>\n");

			this.WriteSimilarChemicals("test", htTestMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, null, null, options);// TODO
																																								// send
																																								// gsid
																																								// lookup
			fw.write("<br><hr>\n");
			this.WriteSimilarChemicals("training", htTrainMatch, fw, endpoint, abbrev, CAS, er.expToxValue, predToxVal, OutputFolder, method, null, null, options);// TODO
																																									// for
																																									// now
																																				// dont
																																									// send
																																									// gsid
																																									// lookup
			// for (int i=0;i<testSetMatchSetTable.size();i++)
			// fw.write(testSetMatchSetTable.get(i));

			fw.write("</html>\n");
			fw.close();
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	// private void WriteMainResultsTableNN(FileWriter fw,String CAS,String
	// endpoint,Lookup.ExpRecord er,double PredToxVal,double MW,String
	// msg,double ExpToxValMass,
	// double PredToxValMass) throws IOException {
	//
	//
	// java.text.DecimalFormat d=new java.text.DecimalFormat("0.00");
	// java.text.DecimalFormat d2=new java.text.DecimalFormat("0.00E00");
	//
	//
	// fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
	// fw.write("<caption>Prediction results</caption>\r\n");
	//
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	// fw.write("<th rowspan=\"2\">Parameter</th>\n");
	//
	// String endpoint2=endpoint.replace("50","<sub>50</sub>");
	// fw.write("<th colspan=\"2\">"+endpoint2+"</th>\n");
	// fw.write("</tr>\n");
	//
	// fw.write("<tr bgcolor=\"#D3D3D3\">\n");
	//
	// fw.write("<th>"+getMassUnits(endpoint)+"</th>\n");
	// fw.write("<th>"+getMassUnits(endpoint)+"</th>\n");
	//
	// fw.write("</tr>\n");
	//
	// // *********************************************************
	// // experimental value in mass units:
	// fw.write("<tr>\n");
	//
	//
	//// if (ExpToxVal ==-9999) {
	//// fw.write("<td>Experimental value</td>\n");
	//// } else {
	////// fw.write("<td>Experimental value (CAS#="+expCAS+")</td>\n");
	//// fw.write("<td><a href=\"experimentalvalue.html\">Experimental value
	// (CAS#="+expCAS+")</a></td>\n");
	//// }
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td>Experimental value</td>\n");
	// } else {
	//// fw.write("<td><a href=\"experimentalvalue.html\">Experimental value
	// (CAS#="+expCAS+")</a></td>\n");
	//
	// fw.write("<td>Experimental value (CAS#="+er.expCAS+")");
	//
	// fw.write(this.getSource(endpoint));
	// fw.write("</td>\n");
	// }
	//
	//
	// if (er.expToxValue ==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n");//mass
	// fw.write("<td align=\"center\">N/A</td>\n");//molar
	// } else {
	// if (ExpToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(ExpToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(ExpToxValMass)+"</td>\n");
	// //mass
	// }
	//
	//
	// fw.write("<td align=\"center\">"+d.format(er.expToxValue)+"</td>\n");
	// //molar
	// }
	//
	// fw.write("</tr>\n");
	//
	// // *********************************************************
	//
	// fw.write("<tr>\n");
	//
	// fw.write("<td>Predicted value");
	// if (!msg.equals("OK")) {
	// fw.write("*");
	// }
	// if (er.expSet.equals("Training") || er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup>");
	// }
	// fw.write("</td>\r\n");
	//
	//
	// if (PredToxVal==-9999) {
	// fw.write("<td align=\"center\">N/A</td>\n"); //mass units
	// fw.write("<td align=\"center\">N/A</td>\n"); //molar units
	// } else {
	//
	// if (PredToxValMass<0.1) {
	// fw.write("<td align=\"center\">"+d2.format(PredToxValMass)+"</td>\n");
	// //mass
	// } else {
	// fw.write("<td align=\"center\">"+d.format(PredToxValMass)+"</td>\n");
	// //mass
	// }
	//
	// fw.write("<td align=\"center\">"+d.format(PredToxVal)+"</td>\n");//molar
	// }
	//
	// fw.write("</tr>\n");
	//
	//
	// fw.write("</table>\n");
	//
	// if (!msg.equals("OK")) {
	// fw.write("*"+msg+"<br>\r\n");
	// }
	//
	// if (er.expSet.equals("Training")) {
	// fw.write("<sup>a</sup>The chemical was present in the training set (but
	// was not used to make the prediction).<br>");
	// } else if (er.expSet.equals("Test")) {
	// fw.write("<sup>a</sup>The chemical was present in the test set.<br>");
	// }
	// fw.write("<br><br>\r\n");
	// }

	private void WriteClusterTableNN(FileWriter fw, String CAS, String endpoint, double ExpToxVal, Instances cc, Vector SimCoeffCluster, boolean writeEvalChemical, ReportOptions options)
			throws IOException {

		// FileWriter fw=new FileWriter (filepath);

		if (cc == null)
			return;

		java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

		// fw.write("<html>\n");

		// fw.write("<h2>Nearest neighbors from the <font color=blue>training
		// set</font></h2>\n");
		// fw.write("<h3>Prediction is determined by averaging the experimental
		// values of the nearest neighbor chemicals in the training
		// set</h3>\n");

		fw.write("<br><br><table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");
		fw.write("<caption>Nearest neighbors from the <font color=blue>training set</font></caption>\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>CAS</th>\n");
		fw.write("<th>Structure</th>\n");

		// fw.write("<th>Toxicity</th>\n");

		fw.write("<th>Experimental value\n");

		if (isLogMolarEndpoint)
			fw.write("<br>" + TESTConstants.getMolarLogUnits(endpoint) + "\n");
		else
			fw.write("<br>" + TESTConstants.getMassUnits(endpoint) + "\n");

		fw.write("</th>\n");

		fw.write("<th>Similarity Coefficient</th>\n");
		fw.write("</tr>\n");

		// fw.write("<td><img src=\"images/structure.png\">"+"</td>\n");

		if (writeEvalChemical) {
			fw.write("<tr>\n");
			fw.write("<td><font color=\"blue\">" + CAS + "<br>(test chemical)</font></td>\n");

			fw.write("<td><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + imgSize + " border=0>" + "</td>\n");

			if (ExpToxVal < -999)
				fw.write("<td align=\"center\">N/A</td>\n");
			else
				fw.write("<td align=\"center\">" + df.format(ExpToxVal) + "</td>\n");

			fw.write("<td><br></td>\n");
			fw.write("</tr>\n");
		}

		for (int i = 0; i < cc.numInstances(); i++) {
			String CASi = cc.instance(i).getName();
			fw.write("<tr>\n");
			fw.write("<td>" + CASi + "</td>\n");
			fw.write("<td><a href=\"../../images/" + CASi + ".png\"><img src=\"" + ReportUtils.getImageSrc(options, "../../images", CASi + ".png") + "\" width=" + imgSize + " border=0></a></td>\n");
			fw.write("<td align=\"center\">" + df.format(cc.instance(i).classValue()) + "</td>\n");
			fw.write("<td align=\"center\">" + df.format(SimCoeffCluster.get(i)) + "</td>\n");
			fw.write("</tr>\n");

		}

		fw.write("</table>\n");
		// fw.write("</html>\n");
		// fw.close();
	}

	private void testSources() {

		try {
			FileWriter fw = new FileWriter("temp/sources.html");

			fw.write("<html>\r\n");

			TESTApplication f = new TESTApplication();
			f.includePhysicalPropertyEndpoints = true;
			f.setUpChoices();

			// System.out.println(f.EndPoints.length);

			for (int i = 0; i < f.endPointsToxicity.size(); i++) {
				String endpoint = f.endPointsToxicity.get(i);
				String src = this.getSourceTag(endpoint);
				fw.write(src + " " + endpoint + "\r\n");
			}

			for (int i = 0; i < f.endPointsPhysicalProperty.size(); i++) {
				String endpoint = f.endPointsPhysicalProperty.get(i);
				String src = this.getSourceTag(endpoint);
				fw.write(src + " " + endpoint + "\r\n");
			}

			fw.write("</html>\r\n");
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	public static void main(String[] args) {
		PredictToxicityWebPageCreator p = new PredictToxicityWebPageCreator();
		p.testSources();
	}
}
