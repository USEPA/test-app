package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import QSAR.qsarOptimal.AllResults;
import QSAR.qsarOptimal.OptimalResults;
import QSAR.validation2.TestChemical;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart;
import ToxPredictor.Utilities.ReportUtils;
import ToxPredictor.Utilities.Utilities;
import ToxPredictor.misc.Lookup;
import wekalite.Instance;

public class PredictToxicityWebPageCreatorLDA {

	boolean isBinaryEndpoint;
	boolean isLogMolarEndpoint;
	Lookup lookup = new Lookup();
	
	private static final Logger logger = LogManager.getLogger(PredictToxicityWebPageCreatorLDA.class);
	
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
	public void WriteLDAResultsWebPages(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String abbrev, boolean isBinaryEndpoint, boolean isLogMolarEndpoint,
			Lookup.ExpRecord er, double MW, Hashtable<Double, Instance> htTestMatch, Hashtable<Double, Instance> htTrainMatch, int chemicalNameIndex, Vector<String> vecMOA, String[] predArrayMOA,
			String[] predArrayLC50, Hashtable<String, AllResults> htAllResultsMOA, Hashtable<String, AllResults> htAllResultsLC50, String bestMOA, double maxScore, ReportOptions options) {

		try {
			this.isBinaryEndpoint = isBinaryEndpoint;
			this.isLogMolarEndpoint = isLogMolarEndpoint;

			java.text.DecimalFormat d = new java.text.DecimalFormat("0.00");
			java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00E00");

			String outputfilename=endpoint.replace(" ", "_")+"_"+method.replace(" ", "_")+"_"+CAS+".html";
			
//			String outputfilename = "PredictionResults";
//			outputfilename += method.replaceAll(" ", "");
//			outputfilename += ".html";

			FileWriter fw = new FileWriter(Paths.get(OutputFolder, outputfilename).toString());

			PredictToxicityWebPageCreator.WriteHeaderInfo(fw, CAS, endpoint, method);

			// System.out.println(er.expCAS+"\t"+er.expMOA);

			writeLDAMainTable(method, endpoint, er, MW, d, d2, fw, chemical, CAS, bestMOA, maxScore);

			// System.out.println(message);
			// if (!message.equals("OK")) {
			// fw.write("<sup>b</sup><font color=darkred>"
			// +message+"</font>\r\n");
			// }

			// *********************************************************************

			fw.write("<br><br><table border=1 cellpadding=10 cellspacing=0>\r\n");
			fw.write("<tr>\r\n");

			fw.write("<td>\r\n");
			writeSortedMOATable(CAS, endpoint, vecMOA, fw, predArrayMOA, predArrayLC50, bestMOA, er);
			fw.write("</td>\r\n");

			fw.write("<td align=middle>Test chemical<br><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + PredictToxicityWebPageCreator.imgSize + "></td>\r\n");

			fw.write("</tr>\r\n");
			fw.write("</table>\r\n");

			// write pages for each MOA:
			for (int i = 0; i < vecMOA.size(); i++) {
				String MOA = vecMOA.get(i);

				AllResults arLC50 = htAllResultsLC50.get(MOA);
				OptimalResults orLC50 = arLC50.getResults().get(0);
				orLC50.calculatePredictedValues();
				this.WriteClusterPageLDA_LC50(method, chemical, OutputFolder, CAS, endpoint, er.expCAS, orLC50, MOA, options);

				AllResults arLDA = htAllResultsMOA.get(MOA);
				OptimalResults orLDA = arLDA.getResults().get(0);
				orLDA.calculatePredictedValues();
				this.WriteClusterPageLDA(method, chemical, OutputFolder, CAS, endpoint, er.expCAS, orLDA, MOA, options);
			}

			// *********************************************************************

			fw.write("<p><a href=\"../StructureData/descriptordata.html\">Descriptor values for test chemical</a></p>\n");

			fw.write("<br><hr>\n");

			String predfilename = "LC50 test set predictions.txt";
			this.WriteSimilarChemicalsInExternalSetLDA(htTestMatch, fw, endpoint, abbrev, CAS, chemicalNameIndex, er.expToxValue, chemical.getPredictedValue(), OutputFolder, method, predfilename,
					options);
			fw.write("<br><hr>\n");

			predfilename = "LC50 training set predictions.txt";
			this.WriteSimilarChemicalsInTrainingSetLDA(htTrainMatch, fw, endpoint, abbrev, CAS, chemicalNameIndex, er.expToxValue, chemical.getPredictedValue(), OutputFolder, method, predfilename,
					options);

			fw.write("</html>\n");

			// System.out.println("here!");

			fw.close();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	private void WriteSimilarChemicalsInTrainingSetLDA(Hashtable<Double, Instance> ht, FileWriter fw, String endpoint, String abbrev, String CAS, int chemicalNameIndex, double expVal, double predVal,
			String outputfolder, String method, String predfilename, ReportOptions options) {

		try {
			if (ht == null)
				return;

			Vector<Double> v = new Vector<>(ht.keySet());
			java.util.Collections.sort(v, new ToxPredictor.Utilities.MyComparator());

			Enumeration<Double> e = v.elements();

			int count = 0;
			while (e.hasMoreElements()) {
				double key = (Double) e.nextElement();
				if (key < PredictToxicityWebPageCreator.SCmin)
					continue;
				count++;
			}

			e = v.elements();

			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			fw.write("<h2>Predictions for the test chemical and for the " + "most similar chemicals in the " + "<font color=blue>training set</font></h2>\n");

			if (count == 0) {
				fw.write("<i>Note: No chemicals in the test set exceed a minimum similarity coefficient of " + PredictToxicityWebPageCreator.SCmin + " for comparison purposes</i>\r\n");
				return;
			}

			fw.write("<h3>If the predicted value matches the experimental values for similar chemicals " + "in the training set (and the similar chemicals were predicted well), "
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

			int counter = 0;
			while (e.hasMoreElements()) {

				double key = (Double) e.nextElement();
				if (key < PredictToxicityWebPageCreator.SCmin)
					continue;
				counter++;

				Instance i = (Instance) ht.get(key);

				String CASi = i.getName();

				// System.out.println("Here1"+method+"\t"+CASi);

				CreateImageFromTrainingPredictionSDFs c=new CreateImageFromTrainingPredictionSDFs();
				c.CreateStructureImageLDA(CASi, strImageFolder,TESTConstants.getAbbrevEndpoint(endpoint));


//				String strKey = df.format(key);
				String expVali = df.format(i.classValue());

				String predVali = lookup.LookUpValueInJarFile(predfilename, CASi, "ID", "Pred_Value:-Log10(mol/L)", "|");

				// System.out.println("Here2"+method+"\t"+CASi);

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
					vecSC.add(key);
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

			// System.out.println(vecExp2.size());

			// ************************************************************
			// Calc stats:
			if (!isBinaryEndpoint && vecPred.size() > 0) {

				String chartname = "PredictionResults" + method + "-SimilarTrainingSetChemicals.png";

				this.writeExternalPredChartLDA(endpoint, method, outputfolder, vecExp, vecPred, vecSC, fw, predfilename, chartname, options);
			} else if (isBinaryEndpoint) {
				String cancerstats = PredictToxicityWebPageCreator.calcCancerStats(0.5, vecExp, vecPred);
				fw.write(cancerstats + "\r\n");

			}

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

			fw.write("<td><a href=\"../StructureData/structure.png\"><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + PredictToxicityWebPageCreator.imgSize
					+ " border=0></a></td>\n");
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
//				String predVali = vecPred2.get(i);

				fw.write("<tr>\n");
				fw.write("<td>" + CASi + "</td>\n");
				fw.write("<td><a href=\"../../images/" + CASi + ".png\"><img src=\"" + ReportUtils.getImageSrc(options, "../../images", CASi + ".png") + "\" width=" + PredictToxicityWebPageCreator.imgSize
						+ " border=0></a></td>\n");
				// fw.write("<td align=\"center\">" + vecSC2.get(i) +
				// "</td>\n");

				String strColor = PredictToxicityWebPageCreator.getColorString(vecSC2.get(i));

				fw.write("<td bgcolor=" + "\"" + strColor + "\"" + " align=\"center\">" + df.format(vecSC2.get(i)) + "</td>\n");

				fw.write("<td align=\"center\">" + vecExp2.get(i) + "</td>\n");
				fw.write("<td align=\"center\">" + vecPred2.get(i) + "</td>\n");
				fw.write("</tr>\n\n");

			} // end loop over elements

			fw.write("</table>\n");

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	void writeExternalPredChartLDA(String endpoint, String method, String outputfolder, Vector<Double> vecExp, Vector<Double> vecPred, Vector<Double> vecSC, FileWriter fw, String predfilename,
			String chartname, ReportOptions options) throws Exception {

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

		fc.WriteImageToFile(chartname, outputfolder);

		double MAEEntireTestSet = lookup.CalculateMAE(predfilename, "Exp_Value:-Log10(mol/L)", "Pred_Value:-Log10(mol/L)", "|");

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
		fw.write("\t<td>Similarity coefficient &ge; " + PredictToxicityWebPageCreator.SCmin + "</td>\n");

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
	
	private void writeMOATable(String endpoint, Vector<String> vecMOA, FileWriter fw, String[] predArrayMOA, String[] predArrayLC50, String bestMOA) throws IOException {
		fw.write("<br><br><table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<caption>Results for each mode of action*</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Mode of action</th>\n");
		fw.write("<th>LDA model score</th>\n");
		fw.write("<th>LC50 prediction interval<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
		fw.write("</tr>\n");

		DecimalFormat df2 = new DecimalFormat("0.00");

		for (int i = 0; i < vecMOA.size(); i++) {

			if (vecMOA.get(i).equals(bestMOA)) {
				fw.write("<tr bgcolor=\"#00CC33\">\n");

			} else {
				fw.write("<tr>\n");
			}

			// fw.write("<td>"+vecMOA.get(i)+"</td>");//MOA

			fw.write("<td>" + vecMOA.get(i) + "<br>");

			fw.write("<a href=\"../../ClusterFiles/" + endpoint + "/" + vecMOA.get(i) + " LDA.html\">");
			fw.write("LDA Model");
			fw.write("</a>");
			fw.write("&nbsp;");

			fw.write("<a href=\"../../ClusterFiles/" + endpoint + "/" + vecMOA.get(i) + " LC50.html\">");
			fw.write("LC50 Model");
			fw.write("</a>");

			fw.write("</td>");

			java.util.LinkedList<String> l_MOA = ToxPredictor.Utilities.Utilities.Parse(predArrayMOA[i], "\t");
			java.util.LinkedList<String> l_LC50 = ToxPredictor.Utilities.Utilities.Parse(predArrayLC50[i], "\t");

			double MOAScore = Double.parseDouble(l_MOA.get(0));
//			double MOAUnc = Double.parseDouble(l_MOA.get(1));
			String MOAError = l_MOA.get(2);

			double LC50Score = Double.parseDouble(l_LC50.get(0));
			double LC50Unc = Double.parseDouble(l_LC50.get(1));
			String LC50Error = l_LC50.get(2);

			// TODO- write code to generate web page for each MOA and LC50 model
			// and link them!
			if (MOAScore == -9999) {
				fw.write("<td>" + MOAError + "</td>");// prediction error
			} else {
				// fw.write("<td>"+df2.format(MOAScore)+"</td>");//Score

				fw.write("<td>");

				// fw.write("<a
				// href=\"../../ClusterFiles/"+endpoint+"/"+vecMOA.get(i)+"
				// LDA.html\">");
				// fw.write(df2.format(MOAScore)+
				// " &plusmn; "+df2.format(MOAUnc));

				fw.write("<a href=\"ClusterFiles/Descriptors " + vecMOA.get(i) + " LDA.html\">");

				// fw.write(df2.format(MOAScore)+
				// " &plusmn; "+df2.format(MOAUnc));

				fw.write(df2.format(MOAScore));

				fw.write("</a></td>");// prediction interval

			}

			if (LC50Score == -9999) {
				fw.write("<td>" + LC50Error + "</td>");// prediction error
			} else {

				// fw.write("<td>"+df2.format(LC50Score)+
				// " &plusmn; "+df2.format(LC50Unc)
				// +"</td>");//prediction interval

				fw.write("<td>");
				fw.write("<a href=\"ClusterFiles/Descriptors " + vecMOA.get(i) + " LC50.html\">");
				fw.write(df2.format(LC50Score) + " &plusmn; " + df2.format(LC50Unc));
				fw.write("</a></td>");// prediction interval

			}

			fw.write("</tr>\n");
		}

		fw.write("</table>\n");
		fw.write("*Results for MOA with highest discriminant score highlighted in green");
	}
	
	private void WriteClusterPageLDA_LC50(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String expCAS, OptimalResults or, String MOA, ReportOptions options) {

		String filename = MOA + " LC50.html";
		String filename2 = "QSAR_Plot_" + MOA + ".png";

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

		boolean writeModelFiles = true;

		try {

			String filepath = strClusterFolderEndpoint + File.separator + filename;

			File file = new File(filepath);
			if (file.exists()) {
//				Path p = Paths.get(filepath);
				// BasicFileAttributes view= Files.getFileAttributeView(p,
				// BasicFileAttributeView.class)
				// .readAttributes();
				// long fileModTime=view.lastModifiedTime().toMillis();

				long fileModTime = file.lastModified();

				long currentTime = System.currentTimeMillis();
				long diff = currentTime - fileModTime;
				double diff_in_hours = diff / 1000.0 / 60.0 / 60.0;
				// System.out.println(diff_in_hours);

				if (diff_in_hours < 1)
					writeModelFiles = false;// dont bother files are less than
											// hour old
			}

		} catch (Exception ex) {
			logger.catching(ex);
		}

		if (writeModelFiles) {// only write model files if they more than 1 hour
								// old
			WriteResultsWebPageLDA(method, chemical, filename, strClusterFolderEndpoint, CAS, endpoint, or, MOA, options);

			PredictToxicityWebPageCreator.CreateQSARPlot(or, filename2, strClusterFolderEndpoint, endpoint);
			String strImageFolder = folder + "/Images";
			PredictToxicityWebPageCreator.CreateStructureImages(or, strImageFolder);
		}

		//////////////////////////////////////////////////////////////////
		String strDescriptorsFolder = OutputFolder + "/ClusterFiles";
		File DescriptorsFolder = new File(strDescriptorsFolder);
		if (!DescriptorsFolder.exists())
			DescriptorsFolder.mkdir();
		String descriptorsFileName = "Descriptors " + MOA + " LC50.html";
		// write descriptors for test chemical for given model:

		String title = "Descriptors for " + CAS + " for " + MOA + " model";
		PredictToxicityWebPageCreator.WriteTestChemicalDescriptorsForClusterModel(chemical, or, strDescriptorsFolder, descriptorsFileName, endpoint, title, options);
	}
	
	private void WriteResultsWebPageBinaryLDA(String method, Instance chemical, String filename, String ClusterFolder, String CAS, String endpoint, OptimalResults or, String MOA,
			ReportOptions options) {

		try {

			File fol = new File(ClusterFolder);
			if (!fol.exists())
				fol.mkdir();

			File file = new File(ClusterFolder + File.separator + filename);

			FileWriter fw = new FileWriter(file);

			// System.out.println(ClusterFolder+File.separator + filename);

			fw.write("<html>\n");
			fw.write("<head>\n");

//			String clusterName = MOA;
			String clusterNameDisplay = MOA + " LDA model";

			fw.write("<title>" + clusterNameDisplay + "</title>");

			fw.write("</head>\n");

			fw.write("<h3>" + clusterNameDisplay + "</h3>");
			fw.write("<p><i>This model determines the likelihood of the " + MOA + " mode of action in terms of a discriminant score.</i></p>");

			PredictToxicityWebPageCreator.WriteOverallModelTableForClusterPageBinary(fw, endpoint, or);

			fw.write("<br><br>\r\n");
			PredictToxicityWebPageCreator.WriteModelTable(MOA + " score", or, fw);

			String imagepath = "../../images";

			this.WriteModelChemicalInfoLDA(CAS, chemical, endpoint, imagepath, ClusterFolder, or, fw, MOA, "LDA", options);

			fw.write("</html>\n");

			fw.close();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	private void WriteModelChemicalInfoLDA(String CAS, Instance chemical, String endpoint, String imagepath, String ClusterFolder, OptimalResults or, FileWriter fw, String MOA, String desc,
			ReportOptions options) {
		String name2 = "";// clusterNameDisplay+" descriptors.txt";
		String name3 = "";// clusterNameDisplay+" descriptors.txt";

		name2 = MOA + " " + desc + " fit results by chemical";
		name3 = MOA + " " + desc + " training set descriptors";

		try {

			String ext2 = ".html";
			String filepath2 = ClusterFolder + File.separator + name2 + ".html";
			PredictToxicityWebPageCreator.WriteModelChemicalInfo2(MOA + " Score", imagepath, chemical, or, CAS, filepath2, name2, options);

			String ext3 = ".txt";
			String filepath3 = ClusterFolder + File.separator + name3 + ext3;
			PredictToxicityWebPageCreator.WriteModelChemicalInfo3(MOA + " Score", filepath3, or, "|");

			// String ext3=".html";
			// String filepath3=ClusterFolder+File.separator+name3+ext3;
			// WriteModelChemicalInfo3html(endpoint,filepath3,or,name3);

			fw.write("<a href=\"" + name2 + ext2 + "\">" + name2 + "</a>&nbsp;&nbsp;\r\n");
			fw.write("<a href=\"" + name3 + ext3 + "\">" + name3 + "</a><br>\r\n");

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	private void WriteResultsWebPageLDA(String method, Instance chemical, String filename, String ClusterFolder, String CAS, String endpoint, OptimalResults or, String MOA, ReportOptions options) {

		try {

			File fol = new File(ClusterFolder);
			if (!fol.exists())
				fol.mkdir();

			FileWriter fw = new FileWriter(ClusterFolder + File.separator + filename);

			fw.write("<html>\n");
			fw.write("<head>\n");

			String clusterName = "Cluster" + MOA;
			String clusterNameDisplay = MOA + " Model";

			fw.write("<title>" + clusterNameDisplay + "</title>");
			fw.write("</head>\n");

			fw.write("<h3>" + clusterNameDisplay + "</h3>");

			fw.write("\n<table>\n");
			fw.write("<tr>\n");

			fw.write("<td>\n");

			PredictToxicityWebPageCreator.WriteOverallModelTableForClusterPage(fw, chemical, endpoint, or, clusterName, clusterNameDisplay, false);

			fw.write("</td>\n");

			fw.write("<td>\n");
			fw.write("<img src=\"" + ReportUtils.getImageSrc(options, "QSAR_Plot_" + MOA + ".png") + "\">");
			fw.write("</td>\n");

			fw.write("</tr>\n");
			fw.write("</table>\n");

			// if (predToxVal == -9999) {
			// fw.write("</html>\n");
			// fw.close();
			// return;
			// }

			PredictToxicityWebPageCreator.WriteModelTable(endpoint, or, fw);

			String imagepath = "../../images";

			this.WriteModelChemicalInfoLDA(CAS, chemical, endpoint, imagepath, ClusterFolder, or, fw, MOA, "LC50", options);

			fw.write("</html>\n");

			fw.close();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	private void WriteClusterPageLDA(String method, TestChemical chemical, String OutputFolder, String CAS, String endpoint, String expCAS, OptimalResults or, String MOA, ReportOptions options) {

		String filename = "";

		filename = MOA + " LDA.html";

//		long t1 = System.currentTimeMillis();

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

		boolean writeModelFiles = true;

		try {

			String filepath = strClusterFolderEndpoint + File.separator + filename;
			File file = new File(filepath);

			if (file.exists()) {
				// Path p = Paths.get(filepath);
				// BasicFileAttributes view = Files.getFileAttributeView(p,
				// BasicFileAttributeView.class).readAttributes();
				//
				// long fileModTime = view.lastModifiedTime().toMillis();

				long fileModTime = file.lastModified();

				long currentTime = System.currentTimeMillis();
				long diff = currentTime - fileModTime;
				double diff_in_hours = diff / 1000.0 / 60.0 / 60.0;

				if (diff_in_hours < 1)
					writeModelFiles = false;
				// System.out.println(diff_in_hours);
			}

		} catch (Exception ex) {
			logger.catching(ex);
		}

		if (writeModelFiles) {
			WriteResultsWebPageBinaryLDA(method, chemical, filename, strClusterFolderEndpoint, expCAS, endpoint, or, MOA, options);
			String strImageFolder = folder + "/Images";
			PredictToxicityWebPageCreator.CreateStructureImages(or, strImageFolder);
		}

		//////////////////////////////////////////////////////////////////
		String strDescriptorsFolder = OutputFolder + "/ClusterFiles";
		File DescriptorsFolder = new File(strDescriptorsFolder);
		if (!DescriptorsFolder.exists())
			DescriptorsFolder.mkdir();

		String descriptorsFileName = "Descriptors " + MOA + " LDA.html";
		// write descriptors for test chemical for given model:

		String title = "Descriptors for " + CAS + " for " + MOA + " model";
		// WriteTestChemicalDescriptorsForClusterModel(chemical, or,
		// strDescriptorsFolder, CAS,MOA+" Score",descriptorsFileName,title);
		PredictToxicityWebPageCreator.WriteTestChemicalDescriptorsForClusterModel(chemical, or, strDescriptorsFolder, descriptorsFileName, endpoint, title, options);

		// System.out.println((t2-t1)/1000.0);
		// System.out.println((t3-t2)/1000.0);
	}
	
	
	private void WriteSimilarChemicalsInExternalSetLDA(Hashtable<Double, Instance> ht, FileWriter fw, String endpoint, String abbrev, String CAS, int chemicalNameIndex, double expVal, double predVal,
			String outputfolder, String method, String predfilename, ReportOptions options) {

		try {
			if (ht == null)
				return;

			Vector<Double> v = new Vector<>(ht.keySet());
			java.util.Collections.sort(v, new ToxPredictor.Utilities.MyComparator());

			Enumeration<Double> e = v.elements();

			int count = 0;
			while (e.hasMoreElements()) {
				double key = e.nextElement();
				if (key < PredictToxicityWebPageCreator.SCmin)
					continue;
				count++;
			}

			e = v.elements();

			// Hashtable htMatch = new Hashtable();

			java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

			fw.write("<h2>Predictions for the test chemical and for the " + "most similar chemicals in the " + "<font color=blue>external test set</font></h2>\n");

			if (count == 0) {
				fw.write("<i>Note: No chemicals in the test set exceed a minimum similarity coefficient of " + PredictToxicityWebPageCreator.SCmin + " for comparison purposes</i>\r\n");
				return;
			}

			fw.write("<h3>If <em>similar</em> test set chemicals " + "were predicted well relative to the entire test set, one has greater confidence in " + "the predicted value.</h3>\n");

			// System.out.println(outputfolder);
			File of1 = new File(outputfolder);
			String folder = of1.getParentFile().getParent();

			// System.out.println("parent="+of1.getParentFile().getAbsolutePath());
			// System.out.println("grandparent="+of1.getParentFile().getParent());
			// System.out.println("folder="+folder);

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

				CreateImageFromTrainingPredictionSDFs c=new CreateImageFromTrainingPredictionSDFs();
				c.CreateStructureImageLDA(CASi, strImageFolder,TESTConstants.getAbbrevEndpoint(endpoint));


//				String strKey = df.format(key);
				String expVali = df.format(i.classValue());

				String predVali = lookup.LookUpValueInJarFile(predfilename, CASi, "ID", "Pred_Value:-Log10(mol/L)", "|");

				// System.out.println("Here2"+method+"\t"+CASi);

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
					vecSC.add(key);
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

			// System.out.println(vecExp2.size());

			// ************************************************************
			// Calc stats:
			if (!isBinaryEndpoint && vecPred.size() > 0) {
				String chartname = "PredictionResults" + method + "-SimilarTestSetChemicals.png";

				this.writeExternalPredChartLDA(endpoint, method, outputfolder, vecExp, vecPred, vecSC, fw, predfilename, chartname, options);
			} else if (isBinaryEndpoint) {
				String cancerstats = PredictToxicityWebPageCreator.calcCancerStats(0.5, vecExp, vecPred);
				fw.write(cancerstats + "\r\n");

			}

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

			fw.write("<td><a href=\"../StructureData/structure.png\"><img src=\"" + ReportUtils.getImageSrc(options, "../StructureData/structure.png") + "\" width=" + PredictToxicityWebPageCreator.imgSize
					+ " border=0></a></td>\n");
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
//				String predVali = vecPred2.get(i);

				fw.write("<tr>\n");
				fw.write("<td>" + CASi + "</td>\n");
				fw.write("<td><a href=\"../../images/" + CASi + ".png\"><img src=\"" + ReportUtils.getImageSrc(options, "../../images", CASi + ".png") + "\" width=" + PredictToxicityWebPageCreator.imgSize
						+ " border=0></a></td>\n");
				// fw.write("<td align=\"center\">" + vecSC2.get(i) +
				// "</td>\n");

				String strColor = PredictToxicityWebPageCreator.getColorString(vecSC2.get(i));

				// System.out.println(CASi+"\t"+strColor);

				fw.write("<td bgcolor=" + "\"" + strColor + "\"" + " align=\"center\">" + df.format(vecSC2.get(i)) + "</td>\n");

				fw.write("<td align=\"center\">" + vecExp2.get(i) + "</td>\n");
				fw.write("<td align=\"center\">" + vecPred2.get(i) + "</td>\n");
				fw.write("</tr>\n\n");

			} // end loop over elements

			fw.write("</table>\n");

			// System.out.println("here!");

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}
	
	private void writeLDAMainTable(String method, String endpoint, Lookup.ExpRecord er, double MW, java.text.DecimalFormat d, java.text.DecimalFormat d2, FileWriter fw, TestChemical chemical,
			String CAS, String bestMOA, double maxScore) throws IOException {

		fw.write("<h2>Predicted " + endpoint + " for <font color=\"blue\">" + CAS + "</font> from " + method + " method</h2>\n");

		double ExpToxValMass = -9999;
		double PredToxValMass = -9999;

		double predToxVal = chemical.getPredictedValue();
		double predToxUnc = chemical.getPredictedUncertainty();

		boolean writePredictionInterval = true;

		if (isLogMolarEndpoint) {
			if (predToxVal != -9999) {
				PredToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, predToxVal, MW);
			}

			if (er.expToxValue != -9999) {
				ExpToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, er.expToxValue, MW);
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

			fw.write(PredictToxicityWebPageCreator.getSourceTag(endpoint));

			fw.write("</th>\n");
		}

		if (er.expSet.equals("Training") || er.expSet.equals("Test")) {

			fw.write("<th>Predicted value");
			fw.write("<sup>a</sup>");

			// if (!message.equals("OK")) {
			// fw.write("<sup>,b</sup>");
			// }

			fw.write("</th>\n");

		} else {

			fw.write("<th>Predicted value</th>\n");

			// if (!message.equals("OK")) {
			// fw.write("<th>Predicted value<sup>b</sup></th>\n");
			// } else {
			// fw.write("<th>Predicted value</th>\n");
			// }
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

				if (Math.abs(maxvalmass) < 0.1 && isLogMolarEndpoint) {
					// mass, need to reverse values	since less toxic = largervalue
					fw.write("<td align=\"center\">" + d2.format(minvalmass) + " &le; Tox &le; " + d2.format(maxvalmass) + "</td>\n");
				} else {					
					// mass, need to reverse values	since less toxic = largervalue					
					fw.write("<td align=\"center\">" + d.format(minvalmass) + " &le; Tox &le; " + d.format(maxvalmass) + "</td>\n");
				}

			}
		}

		fw.write("</tr>\n");

		// ************************************************************
		// MOA

		fw.write("<tr>\n");

		fw.write("<td>Mode of action</td>\n");

		if (er.expMOA.equals("")) {
			fw.write("<td align=\"center\">N/A</td>\n");
		} else {
			fw.write("<td align=\"center\">" + er.expMOA + "</td>\n");
		}

		// System.out.println("maxScore="+maxScore);
		// System.out.println("bestMOALC50Error="+bestMOA_LDAError);

		// if (bestMOA.equals("") || maxScore < 0.5 ||
		// !bestMOA_LDAError.equals("OK")) {
		if (bestMOA.equals("") || maxScore < 0.5) {
			fw.write("<td align=\"center\">N/A</td>\n");
		} else {
			fw.write("<td align=\"center\">" + bestMOA + "</td>\n");
		}

		fw.write("<td bgcolor=\"#D3D3D3\"></td>\n");

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
	}
	
	private void writeSortedMOATable(String CAS, String endpoint, Vector<String> vecMOA, FileWriter fw, String[] predArrayMOA, String[] predArrayLC50, String bestMOA, Lookup.ExpRecord er)
			throws IOException {

		Hashtable<Double, String> htMOA = new Hashtable<Double, String>();

		// System.out.println(vecMOA.size());

		for (int i = 0; i < vecMOA.size(); i++) {
			java.util.LinkedList<String> l_MOA = ToxPredictor.Utilities.Utilities.Parse(predArrayMOA[i], "\t");
//			java.util.LinkedList<String> l_LC50 = ToxPredictor.Utilities.Utilities.Parse(predArrayLC50[i], "\t");

			double MOAScore = Double.parseDouble(l_MOA.get(0));

			// System.out.println(vecMOA.get(i)+"\t"+MOAScore);

			htMOA.put(MOAScore, vecMOA.get(i));
		}

		Vector<Double> v = new Vector<>(htMOA.keySet());
		Collections.sort(v, new ToxPredictor.Utilities.MyComparator());

		Vector<String> vecMOA2 = new Vector<String>();
		Enumeration<Double> e = v.elements();
		while (e.hasMoreElements()) {
			double key = e.nextElement();
			vecMOA2.add(htMOA.get(key));
			// System.out.println(key+"\t"+htMOA.get(key));
		}

		// for (int i=0;i<vecMOA2.size();i++) {
		// String oldvalue=vecMOA2.get(i);
		// String newvalue=oldvalue.substring(oldvalue.indexOf("\t")+1);
		// String score=oldvalue.substring(0,oldvalue.indexOf("\t"));
		// vecMOA2.set(i, newvalue);
		// System.out.println(score+"\t"+vecMOA2.get(i));
		// }

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<caption>Results for each mode of action</caption>\r\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>Mode of action</th>\n");
		fw.write("<th>LDA model score</th>\n");
		fw.write("<th>LC50 predicted value<br>" + TESTConstants.getMolarLogUnits(endpoint) + "</th>\n");
		fw.write("</tr>\n");

		DecimalFormat df2 = new DecimalFormat("0.00");

		// System.out.println(vecMOA2.size());

		// System.out.println("*"+er.expMOA);

		for (int i = 0; i < vecMOA2.size(); i++) {
			String MOAi = vecMOA2.get(i);

			String predsMOA = predArrayMOA[vecMOA.indexOf(MOAi)];
			String predsLC50 = predArrayLC50[vecMOA.indexOf(MOAi)];

			LinkedList<String> l_MOA = Utilities.Parse(predsMOA, "\t");
			LinkedList<String> l_LC50 = Utilities.Parse(predsLC50, "\t");

			double MOAScore = Double.parseDouble(l_MOA.get(0));
//			double MOAUnc = Double.parseDouble(l_MOA.get(1));
			String MOAError = l_MOA.get(2);

			double LC50Score = Double.parseDouble(l_LC50.get(0));
//			double LC50Unc = Double.parseDouble(l_LC50.get(1));
			String LC50Error = l_LC50.get(2);

			// System.out.println(MOAi+"\t"+MOAScore+"\t"+MOAError+"\t"+LC50Score+"\t"+LC50Error);

			// if (MOAi.equals(bestMOA) && MOAScore>=0.5 ) {
			// fw.write("<tr bgcolor=\"#00CC33\">\n<td>"+MOAi);
			// fw.write(" <b>(Predicted MOA)</b>");
			// } else if (MOAi.equals(er.expMOA) && !er.expMOA.equals(bestMOA))
			// {
			// fw.write("<tr bgcolor=\"#FF9900\">\n<td>"+MOAi);
			// fw.write(" <b>(Experimental MOA)</b>");
			// } else {
			// fw.write("<tr>\n<td>"+MOAi);
			// }

			if (MOAi.equals(bestMOA) && MOAScore >= 0.5) {
				if (er.expMOA.equals(bestMOA)) {
					fw.write("<tr bgcolor=\"#00CC33\">\n<td>" + MOAi);// green
					fw.write(" <b>(Experimental & predicted MOA)</b>");
					// System.out.println(CAS+"\tCase1");
				} else if (er.expMOA.equals("N/A") || er.expMOA.equals("")) {// experimental
																				// MOA
																				// unavailable
					fw.write("<tr bgcolor=\"#00CC33\">\n<td>" + MOAi);// green
					fw.write(" <b>(Predicted MOA)</b>");
					// System.out.println(CAS+"\tCase2");
				} else {// predicted doesnt match experimental MOA
					fw.write("<tr bgcolor=\"#FF9900\">\n<td>" + MOAi);// orange
					fw.write(" <b>(Predicted MOA)</b>");
					// System.out.println(CAS+"\tCase3");
				}

			} else if (MOAi.equals(er.expMOA)) {
				fw.write("<tr bgcolor=\"#00CC33\">\n<td>" + MOAi);// green
				fw.write(" <b>(Experimental MOA)</b>");
				// System.out.println(CAS+"\tCase4");

			} else {
				fw.write("<tr>\n<td>" + MOAi);
				// System.out.println(CAS+"\tCase5");
			}

			fw.write("<br>");

			fw.write("<a href=\"../../ClusterFiles/" + endpoint + "/" + MOAi + " LDA.html\">");
			fw.write("LDA Model");
			fw.write("</a>");
			fw.write("&nbsp;");

			fw.write("<a href=\"../../ClusterFiles/" + endpoint + "/" + MOAi + " LC50.html\">");
			fw.write("LC50 Model");
			fw.write("</a>");

			fw.write("</td>");

			// TODO- write code to generate web page for each MOA and LC50 model
			// and link them!
			// if (MOAScore==-9999) {
			// fw.write("<td>"+MOAError+"</td>");//prediction error
			// } else {
			// fw.write("<td>");
			// fw.write("<a href=\"ClusterFiles/Descriptors "+MOAi+"
			// LDA.html\">");
			// if (MOAScore<0.5) {
			// fw.write("<font color=red>"+df2.format(MOAScore)+"</font>");
			// } else {
			// fw.write(df2.format(MOAScore));
			// }
			// fw.write("</a></td>");//prediction interval
			// }

			// ******************************************************************************************
			// MOA score
			fw.write("<td>");
			fw.write("<a href=\"ClusterFiles/Descriptors " + MOAi + " LDA.html\">");
			if (!MOAError.equals("OK")) {
				fw.write("<font color=red>" + df2.format(MOAScore) + " (" + MOAError + ")" + "</font>");
			} else {
				if (MOAScore < 0.5) {
					fw.write("<font color=red>" + df2.format(MOAScore) + " (score < 0.5)</font>");
				} else {
					fw.write(df2.format(MOAScore));
				}
			}

			fw.write("</a></td>");// prediction interval

			// ******************************************************************************************
			// LC50 value
			fw.write("<td>");
			fw.write("<a href=\"ClusterFiles/Descriptors " + MOAi + " LC50.html\">");
			if (!LC50Error.equals("OK")) {
				fw.write("<font color=red>" + df2.format(LC50Score) + " (" + LC50Error + ")" + "</font>");
			} else {
				fw.write(df2.format(LC50Score));
			}
			fw.write("</a></td>");// prediction interval

			fw.write("</tr>\n");
		}

		fw.write("</table>\n");
		// fw.write("*LDA scores less than 0.5 are not considered
		// significant.");
		// fw.write("*Results for MOA with highest discriminant score
		// highlighted in green.");
		// fw.write("<br>A prediction is not made if the maximum score is less
		// than 0.5");
	}
}
