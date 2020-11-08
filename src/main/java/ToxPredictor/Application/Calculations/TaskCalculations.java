package ToxPredictor.Application.Calculations;

import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import QSAR.qsarOptimal.AllResults;
import QSAR.validation2.AllResultsXMLReader;
import QSAR.validation2.InstanceUtilities;
import wekalite.*;
//import ToxPredictor.My3Ddescriptors.descriptorfactory3D_2;
import edu.stanford.ejalbert.BrowserLauncher;//new one
import ToxPredictor.Utilities.TESTPredictedValue;
//import weka.core.Instance;
//import weka.core.Instances;
import ToxPredictor.Utilities.Utilities;
//import ToxPredictor.generate3Dcoordinates.*;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.Lookup.ExpRecord;
import ToxPredictor.misc.ParseChemidplus;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Miscellaneous.SwingWorker;
import ToxPredictor.Application.GUI.MyBrowserLauncher;
import ToxPredictor.Application.GUI.TESTApplication;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;

/**
 * This class replaces DescriptorCalculationTask7 and
 * DescriptorCalculationTask7Batch- it was too difficult to maintain these two
 * classes since have to make changes in too many locations every time you make
 * a change to the calculations
 * 
 * @author TMARTI02
 *
 */
public class TaskCalculations {

	SwingWorker worker;

	// task variables:
	private int lengthOfTask;

	private int current = 0; // variable used to update length of progress bar
								// (100 = done)

	private boolean done = false;

	private String statMessage; // message that tells what has been done so far
								// or currently

	IAtomContainerSet moleculeSet;

	boolean WriteResultsToFile;

	boolean FindFragments;
	boolean useFragmentsConstraint;

	long minFileAgeHours = 1000000000;

	File fileOutputFolder;

	DescriptorFactory df = new DescriptorFactory(false);
	// public descriptorfactory3D_2 df3D=new descriptorfactory3D_2();
	// coordinategenerator cg = new coordinategenerator();
	// PredictToxicityFDA ptFDA=new PredictToxicityFDA();
	PredictToxicityHierarchical ptH = new PredictToxicityHierarchical();
	PredictToxicityNearestNeighbor ptNN = new PredictToxicityNearestNeighbor();
	PredictToxicityRandomForrestCaesar ptRFC = new PredictToxicityRandomForrestCaesar();
	PredictToxicityLDA ptLDA = new PredictToxicityLDA();

	InstanceUtilities iu = new InstanceUtilities();

	String batchFileName;

	String endpoint;
	boolean isBinaryEndpoint;
	boolean isLogMolarEndpoint;
	String method;
	String abbrev;

	boolean Use3D;
	boolean is3Dpresent;
	Object gui = null;
	String DescriptorSet;

	Hashtable<String, Instances> ht_ccTraining = new Hashtable<String, Instances>();
	Hashtable<String, Instances> ht_ccTrainingFrag = new Hashtable<String, Instances>();
	Hashtable<String, Instances> ht_ccPrediction = new Hashtable<String, Instances>();
	Hashtable<String, AllResults> ht_allResults = new Hashtable<String, AllResults>();
	Hashtable<String, AllResults> ht_allResultsFrag = new Hashtable<String, AllResults>();

	///////////////////////////////////////////////////////
	java.util.Vector<String> vecMOA = new Vector<String>();
	Hashtable<String, AllResults> htAllResultsMOA = new Hashtable<String, AllResults>();
	Hashtable<String, Instances> htTrainingSetsMOA = new Hashtable<String, Instances>();

	Hashtable<String, AllResults> htAllResultsLC50 = new Hashtable<String, AllResults>();
	Hashtable<String, Instances> htTrainingSetsLC50 = new Hashtable<String, Instances>();

	Instances ccTrainingMOA;
	Instances ccPredictionMOA;
	Instances ccOverallMOA;

	///////////////////////////////////////////////////////

	int classIndex = 1;
	int chemicalNameIndex = 0;

	// Validation2 validation = new Validation2();
	AllResultsXMLReader arxr = new AllResultsXMLReader();

	Lookup lookup = new Lookup();

	String del = "|";// delimiter for text file

	public static int minPredCount = 2;// minimum number of predictions needed
										// for consensus pred

	static double SCmin = 0.5;// min for consideration as similar in tables

	int taskType = -1;
	String runNumber = "";

	public TaskCalculations() {
	}

	public static String[] CreateVarListFromTrainingSet(Instances trainingSet) {
		ArrayList varList = new ArrayList();

		for (int i = 0; i < trainingSet.numAttributes(); i++) {
			varList.add(trainingSet.attribute(i));
		}

		// Convert ArrayList to String array:
		String[] varArray = new String[varList.size()];
		for (int i = 0; i < varList.size(); i++)
			varArray[i] = (String) varList.get(i);
		return varArray;

	}

	public static ArrayList<String> getMethods(String endpoint) {
		ArrayList<String> methods = new ArrayList<String>();
		methods.add(TESTConstants.ChoiceHierarchicalMethod);

		if (TESTConstants.haveSingleModelMethod(endpoint)) {
			methods.add(TESTConstants.ChoiceSingleModelMethod);
		}
		// Group contribution:
		if (TESTConstants.haveGroupContributionMethod(endpoint)) {
			methods.add(TESTConstants.ChoiceGroupContributionMethod);
		}
		// methods.add(TESTConstants.ChoiceFDAMethod);
		methods.add(TESTConstants.ChoiceNearestNeighborMethod);

		methods.add(TESTConstants.ChoiceConsensus);

		return methods;
	}

	private void WriteBinaryToxResultsForChemical(FileWriter fw, FileWriter fw2, int index, String CAS, double ExpToxVal, double PredToxVal, double MW, String d, String error) {

		try {

			java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");

			String f = "ToxRuns/ToxRun_" + CAS;
			String f2 = f + "/" + endpoint;
			String f3 = f + "/StructureData";

			String filename = "PredictionResults";
			filename += method.replaceAll(" ", "");
			filename += ".html";

			fw.write("<tr>\n");
			fw.write("<td>" + index + "</td>\n");// #

			if (error.equals("")) {
				fw.write("<td><a href=\"" + f2 + "/" + filename + "\">" + CAS + "</a></td>\n");

				fw.write("<td><a href=\"" + f3 + "/structure.png" + "\"><img src=\"" + f3 + "/" + "structure.png" + "\" height=200 border=0></a></td>\n");
			} else {
				fw.write("<td>" + CAS + "</td>\n");
				fw.write("<td>Error: " + error + "</td>\n");
			}

			fw2.write(index + d + CAS + d);

			if (ExpToxVal == -9999) {
				fw.write("<td>N/A</td>\n");
				fw2.write("N/A" + d);
			} else {
				fw.write("<td>" + d2.format(ExpToxVal) + "</td>\n");
				fw2.write(d2.format(ExpToxVal) + d);
			}
			if (PredToxVal == -9999) {
				fw.write("<td>N/A</td>\n");
				fw2.write("N/A" + d);
			} else {
				fw.write("<td>" + d2.format(PredToxVal) + "</td>\n");
				fw2.write(d2.format(PredToxVal) + d);
			}

			// System.out.println(ExpToxVal+"\t"+PredToxVal);

			if (ExpToxVal == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n");// mass
				fw2.write("N/A" + d);
			} else {
				if (ExpToxVal < 0.5) {

					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						fw.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); // mass
						fw2.write("Developmental NON-toxicant" + d); // mass

					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						fw.write("<td align=\"center\">Mutagenicity Negative</td>\n"); // mass
						fw2.write("Mutagenicity Negative" + d); // mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						fw.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); // mass
						fw2.write("Does NOT bind to estrogen receptor" + d); // mass
					}
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						fw.write("<td align=\"center\">Developmental toxicant</td>\n"); // mass
						fw2.write("Developmental toxicant" + d); // mass

					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						fw.write("<td align=\"center\">Mutagenicity Positive</td>\n"); // mass
						fw2.write("Mutagenicity Positive" + d); // mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						fw.write("<td align=\"center\">Binds to estrogen receptor</td>\n"); // mass
						fw2.write("Binds to estrogen receptor" + d); // mass
					}

				}
			}

			if (PredToxVal == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n"); // mass units
				fw2.write("N/A");
			} else {

				if (PredToxVal < 0.5) {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						fw.write("<td align=\"center\">Developmental NON-toxicant</td>\n"); // mass
						fw2.write("Developmental NON-toxicant"); // mass

					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						fw.write("<td align=\"center\">Mutagenicity Negative</td>\n"); // mass
						fw2.write("Mutagenicity Negative"); // mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						fw.write("<td align=\"center\">Does NOT bind to estrogen receptor</td>\n"); // mass
						fw2.write("Does NOT bind to estrogen receptor" + d); // mass
					}
				} else {
					if (endpoint.equals(TESTConstants.ChoiceReproTox)) {
						fw.write("<td align=\"center\">Developmental toxicant</td>\n"); // mass
						fw2.write("Developmental toxicant"); // mass

					} else if (endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
						fw.write("<td align=\"center\">Mutagenicity Positive</td>\n"); // mass
						fw2.write("Mutagenicity Positive"); // mass
					} else if (endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
						fw.write("<td align=\"center\">Binds to estrogen receptor</td>\n"); // mass
						fw2.write("Binds to estrogen receptor" + d); // mass
					}
				}
			}

			fw.write("</tr>\n");
			fw2.write("\r\n");

			fw.flush();
			fw2.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void WriteDescriptorResultsForChemicalToWebPage(FileWriter fw, int index, String CAS, String error) {
		String filename = "DescriptorData.html";

		try {
			fw.write("<tr>\n");

			fw.write("<td>" + index + "</td>\n");// #

			if (error.equals("")) {
				String folder = "ToxRuns/ToxRun_" + CAS + "/StructureData";

				fw.write("<td><a href=\"" + folder + "/" + filename + "\">" + CAS + "</a></td>\n"); // ID
																									// col

				fw.write("<td><a href=\"" + folder + "/structure.png" + "\"><img src=\"" + folder + "/structure.png" + "\" height=200 border=0></a></td>\n");// structure
																																								// col

				// fw.write("<td><br></td>\n");//error col
			} else {
				fw.write("<td>" + CAS + "</td>\n"); // ID col
				fw.write("<td>" + "Error: " + error + "</td>\n");
				// fw.write("<td><br></td>\n");//error col
			}

			fw.write("</tr>\n");

			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void WriteOverallTextHeaderAllMethods(FileWriter fw, String d, ArrayList<String> methods) throws Exception {
		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

		String units;

		if (!isBinaryEndpoint) {
			if (isLogMolarEndpoint) {
				units = molarlogunits;
			} else {
				units = massunits;
			}
		} else {
			units = "";
		}

		if (!units.equals("")) {
			fw.write("Note: All values in " + units + "\r\n");
		}

		fw.write("#" + d + "ID" + d + "Exp" + d);

		for (int i = 0; i < methods.size(); i++) {
			fw.write("Pred_" + methods.get(i));
			if (i < methods.size() - 1) {
				fw.write(d);
			}
		}
		fw.write("\r\n");

	}

	private void WriteOverallHtmlHeader(FileWriter fw, String textFileNameMethod, String textFileNameAll) throws Exception {

		fw.write("<!DOCTYPE HTML><html><head><title>\r\n");

		// System.out.println(endpoint.equals(TESTConstants.ChoiceDescriptors));

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors))
			fw.write("Batch predictions for " + method + " method for " + endpoint + "\r\n");
		else {
			fw.write("Descriptors\r\n");
		}
		fw.write("</title></head><body>\r\n");

		String strDel = "\"" + del + "\"";
		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			fw.write("<h2>Batch predictions for " + method + " method for " + endpoint + "</h2>\r\n");
			fw.write("<h3><a href=\"" + textFileNameMethod + "\">" + "Text file (" + method + " method)" + "</a></h3>\r\n");

			if (method.equals(TESTConstants.ChoiceConsensus)) {
				fw.write("<h3><a href=\"" + textFileNameAll + "\">" + "Text file (All methods)" + "</a></h3>\r\n");
			}
		} else {
			fw.write("<h2>Descriptors (");

			// System.out.println("strDel="+strDel);

			if (del.equals("\t")) {
				fw.write("<a href=\"" + textFileNameMethod + "\">tab delimited descriptor file</a>)</h2>\r\n");
			} else {
				fw.write("<a href=\"" + textFileNameMethod + "\">" + strDel + " delimited descriptor file</a>)</h2>\r\n");
			}

		}

		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

		fw.write("<tr bgcolor=\"#D3D3D3\">\n");
		fw.write("<th>#</th>\n");
		fw.write("<th>ID</th>\n");
		fw.write("<th>Structure</th>\n");

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			String massunits = TESTConstants.getMassUnits(endpoint);
			String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

			if (!isBinaryEndpoint) {
				if (isLogMolarEndpoint) {
					fw.write("<th>Experimental Value<br>" + molarlogunits + "</th>\n");
					fw.write("<th>Predicted Value<br>" + molarlogunits + "</th>\n");
				}
				fw.write("<th>Experimental Value<br>" + massunits + "</th>\n");
				fw.write("<th>Predicted Value<br>" + massunits + "</th>\n");

				if (method.equals(TESTConstants.ChoiceLDA)) {
					fw.write("<th>Experimental MOA<br></th>\n");
					fw.write("<th>Predicted MOA<br></th>\n");
				}

			} else {
				fw.write("<th>Experimental Value</th>\n");
				fw.write("<th>Predicted Value</th>\n");
				fw.write("<th>Experimental Result</th>\n");
				fw.write("<th>Predicted Result</th>\n");

			}

		} else {
			// fw.write("<th>Error</th>\n");
		}

		// fw.write("<th>Experimental Value</th>\n");
		// fw.write("<th>Predicted Value</th>\n");
		fw.write("</tr>\n");

	}

	private void WriteOverallTextHeader(FileWriter fw, String d) throws Exception {

		fw.write("#" + d + "ID" + d);

		String massunits = TESTConstants.getMassUnits(endpoint);
		String molarlogunits = TESTConstants.getMolarLogUnits(endpoint);

		if (!isBinaryEndpoint) {
			if (isLogMolarEndpoint) {
				fw.write("Exp_Value:" + molarlogunits + d);
				fw.write("Pred_Value:" + molarlogunits + d);
			}

			fw.write("Exp_Value:" + massunits + d);
			fw.write("Pred_Value:" + massunits);

			if (method.equals(TESTConstants.ChoiceLDA)) {
				fw.write(d + "Experimental MOA" + d);
				fw.write("Predicted MOA");
			}

		} else {
			fw.write("Exp_Value" + d);
			fw.write("Pred_Value" + d);
			fw.write("Exp_Result" + d);
			fw.write("Pred_Result");
		}
		fw.write("\r\n");

	}

	private void WriteToxicityResultsForChemicalAllMethods(FileWriter fw3, int index, String CAS, double ExpToxVal, ArrayList<String> methods, ArrayList<Double> preds, double MW, String d,
			String error) {
		try {

			java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
			java.text.DecimalFormat d2exp = new java.text.DecimalFormat("0.00E00");

			double ExpToxValMass = -9999;

			ArrayList<Double> predsMass = new ArrayList<Double>();

			if (preds != null) {
				for (int i = 0; i < preds.size(); i++) {
					double PredToxVal = preds.get(i);

					if (PredToxVal == -9999) {
						predsMass.add(new Double(-9999));
					}

					if (isLogMolarEndpoint) {
						if (ExpToxVal != -9999) {
							ExpToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, ExpToxVal, MW);
						}
						if (PredToxVal != -9999) {
							predsMass.add(PredictToxicityWebPageCreator.getToxValMass(endpoint, PredToxVal, MW));
						}

					} else {
						if (PredToxVal != -9999) {
							predsMass.add(PredToxVal);
						}
						ExpToxValMass = ExpToxVal;
					}
				}
			}

			fw3.write(index + d + CAS + d);

			// ********************************************************
			// Molar values

			if (isLogMolarEndpoint) {
				if (ExpToxVal == -9999) {
					fw3.write("N/A" + d);
				} else {
					fw3.write(d2.format(ExpToxVal) + d);
				}
				if (preds != null) {
					for (int i = 0; i < preds.size(); i++) {
						double PredToxVal = preds.get(i);

						if (PredToxVal == -9999) {
							fw3.write("N/A");
						} else {
							fw3.write(d2.format(PredToxVal));
						}

						if (i < preds.size() - 1)
							fw3.write(d);

					}
				} else {

					for (int i = 0; i < methods.size(); i++) {
						fw3.write("N/A");

						if (i < methods.size() - 1)
							fw3.write(d);

					}
				}
			} else {// not molar endpoint:

				if (ExpToxVal == -9999) {
					fw3.write("N/A" + d);
				} else {
					if (Math.abs(ExpToxValMass) < 0.1) {
						fw3.write(d2exp.format(ExpToxValMass) + d); // mass
					} else {
						fw3.write(d2.format(ExpToxValMass) + d); // mass
					}
				}

				if (preds != null) {
					for (int i = 0; i < preds.size(); i++) {
						double PredToxValMass = predsMass.get(i);

						if (PredToxValMass == -9999) {
							fw3.write("N/A");
						} else {
							if (Math.abs(PredToxValMass) < 0.1) {
								fw3.write(d2exp.format(PredToxValMass)); // mass
							} else {
								fw3.write(d2.format(PredToxValMass)); // mass
							}
						}
						if (i < preds.size() - 1)
							fw3.write(d);

					}
				} else {
					for (int i = 0; i < methods.size(); i++) {
						fw3.write("N/A");
						if (i < methods.size() - 1)
							fw3.write(d);
					}
				}
			}

			// ********************************************************
			// Mass values

			fw3.write("\r\n");
			fw3.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void WriteToxicityResultsForChemical(FileWriter fw, FileWriter fw2, int index, String CAS, double ExpToxVal, double PredToxVal, double MW, String d, String error, String experimentalMOA,
			String bestMOA, double maxScore) {
		try {

			java.text.DecimalFormat d2 = new java.text.DecimalFormat("0.00");
			java.text.DecimalFormat d2exp = new java.text.DecimalFormat("0.00E00");

			double ExpToxValMass = -9999;
			double PredToxValMass = -9999;

			// if (PredToxVal!=-9999) {
			// PredToxValMass=PredictToxicityWebPageCreator.getToxValMass(endpoint,
			// PredToxVal, MW);
			// }
			// if (ExpToxVal!=-9999) {
			// ExpToxValMass=PredictToxicityWebPageCreator.getToxValMass(endpoint,
			// ExpToxVal, MW);
			// }

			if (isLogMolarEndpoint) {
				if (PredToxVal != -9999) {
					PredToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, PredToxVal, MW);
				}

				if (ExpToxVal != -9999) {
					ExpToxValMass = PredictToxicityWebPageCreator.getToxValMass(endpoint, ExpToxVal, MW);
				}
			} else {
				PredToxValMass = PredToxVal;
				ExpToxValMass = ExpToxVal;
			}

			String f = "ToxRuns/ToxRun_" + CAS;
			String f2 = f + "/" + endpoint;
			String f3 = f + "/StructureData";

			String filename = "PredictionResults";
			filename += method.replaceAll(" ", "");
			filename += ".html";

			fw.write("<tr>\n");
			fw.write("<td>" + index + "</td>\n");// #

			if (error.equals("")) {
				fw.write("<td><a href=\"" + f2 + "/" + filename + "\">" + CAS + "</a></td>\n");

				fw.write("<td><a href=\"" + f3 + "/structure.png" + "\"><img src=\"" + f3 + "/" + "structure.png" + "\" height=200 border=0></a></td>\n");
			} else {
				fw.write("<td>" + CAS + "</td>\n");
				fw.write("<td>Error: " + error + "</td>\n");
			}

			fw2.write(index + d + CAS + d);

			// ********************************************************
			// Molar values

			if (isLogMolarEndpoint) {
				if (ExpToxVal == -9999) {
					fw.write("<td>N/A</td>\n");
					fw2.write("N/A" + d);
				} else {
					fw.write("<td>" + d2.format(ExpToxVal) + "</td>\n");
					fw2.write(d2.format(ExpToxVal) + d);
				}
				if (PredToxVal == -9999) {
					fw.write("<td>N/A</td>\n");
					fw2.write("N/A" + d);
				} else {
					fw.write("<td>" + d2.format(PredToxVal) + "</td>\n");
					fw2.write(d2.format(PredToxVal) + d);
				}
			}

			// ********************************************************
			// Mass values
			if (ExpToxVal == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n");// mass
				fw2.write("N/A" + d);
			} else {
				if (Math.abs(ExpToxValMass) < 0.1) {
					fw.write("<td align=\"center\">" + d2exp.format(ExpToxValMass) + "</td>\n"); // mass
					fw2.write(d2exp.format(ExpToxValMass) + d); // mass
				} else {
					fw.write("<td align=\"center\">" + d2.format(ExpToxValMass) + "</td>\n"); // mass
					fw2.write(d2.format(ExpToxValMass) + d); // mass
				}
			}

			if (PredToxVal == -9999) {
				fw.write("<td align=\"center\">N/A</td>\n"); // mass units
				fw2.write("N/A");
			} else {
				if (Math.abs(PredToxValMass) < 0.1) {
					fw.write("<td align=\"center\">" + d2exp.format(PredToxValMass) + "</td>\n"); // mass
					fw2.write(d2exp.format(PredToxValMass)); // mass
				} else {
					fw.write("<td align=\"center\">" + d2.format(PredToxValMass) + "</td>\n"); // mass
					fw2.write(d2.format(PredToxValMass)); // mass
				}
			}

			if (method.equals(TESTConstants.ChoiceLDA)) {

				if (experimentalMOA.equals("")) {
					fw.write("<td align=\"center\">N/A</td>\n");
					fw2.write(d + "N/A" + d);
				} else {
					fw.write("<td align=\"center\">" + experimentalMOA + "</td>\n");
					fw2.write(d + experimentalMOA + d);
				}

				if (bestMOA.equals("") || maxScore < 0.5) {
					fw.write("<td align=\"center\">N/A</td>\n");
					fw2.write("N/A");
				} else {
					fw.write("<td align=\"center\">" + bestMOA + "</td>\n");
					fw2.write(bestMOA);
				}

			}

			fw.write("</tr>\n");
			fw2.write("\r\n");

			fw.flush();
			fw2.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private double calculateConsensusToxicity(ArrayList<Double> preds) {

		double pred = 0;

		int predcount = 0;

		for (int i = 0; i < preds.size(); i++) {
			if (preds.get(i) > -9999) {
				predcount++;
				pred += preds.get(i);
			}
		}

		if (predcount < minPredCount)
			return -9999;

		pred /= (double) predcount;
		// System.out.println(pred);
		return pred;
	}

	public static Lookup.ExpRecord LookupExpVal(String CAS, Instance chemical, Instances trainingDataSet2d, Instances testDataSet2d) {
		Lookup lookup = new Lookup();
		// trainingDataSet2d.setClassIndex(classIndex);
		// testDataSet2d.setClassIndex(classIndex);

		// TODO: use just structure search like with LDA method

		Lookup.ExpRecord er = lookup.new ExpRecord();

		// lookup in training set based on CAS
		er.expToxValue = lookup.LookupExpValByCAS(CAS, trainingDataSet2d);
		if (er.expToxValue != -9999) {
			er.expCAS = CAS;
			er.expSet = "Training";
			return er;
		}

		// ******************************************************
		// lookup in external test set based on CAS:
		er.expToxValue = lookup.LookupExpValByCAS(CAS, testDataSet2d);
		if (er.expToxValue != -9999) {
			er.expCAS = CAS;
			er.expSet = "Test";
			return er;
		}

		// ******************************************************
		// lookup in training set based on structure: 
		er = lookup.LookupExpValByStructure(chemical, trainingDataSet2d);
		if (er.expToxValue != -9999) {
			er.expSet = "Training";
			return er;
		}

		// ******************************************************
		// lookup in test set based on structure:
		er = lookup.LookupExpValByStructure(chemical, testDataSet2d);
		if (er.expToxValue != -9999) {
			er.expSet = "Test";
			return er;
		}

		// ******************************************************
		er = lookup.new ExpRecord();
		er.expToxValue = -9999;
		er.expSet = "";
		er.expCAS = "";

		return er;

	}

	public static Lookup.ExpRecord LookupExpVal(String CAS, Instances trainingDataSet2d, Instances testDataSet2d) {
		Lookup lookup = new Lookup();
		// trainingDataSet2d.setClassIndex(classIndex);
		// testDataSet2d.setClassIndex(classIndex);

		Lookup.ExpRecord er = lookup.new ExpRecord();

		// lookup in training set based on CAS
		er.expToxValue = lookup.LookupExpValByCAS(CAS, trainingDataSet2d);
		if (er.expToxValue != -9999) {
			er.expCAS = CAS;
			er.expSet = "Training";
			return er;
		}

		// ******************************************************
		// lookup in external test set based on CAS:
		er.expToxValue = lookup.LookupExpValByCAS(CAS, testDataSet2d);
		if (er.expToxValue != -9999) {
			er.expCAS = CAS;
			er.expSet = "Test";
			return er;
		}

		// ******************************************************
		er = lookup.new ExpRecord();
		er.expToxValue = -9999;
		er.expSet = "";
		er.expCAS = "";

		return er;

	}

	public static Lookup.ExpRecord LookupExpVal_LDA(String CAS, Instance chemical, Instances ccOverallMOA, Instances ccTrainingMOA, Instances ccPredictionMOA) {

		Lookup lookup = new Lookup();

		Lookup.ExpRecord er = lookup.new ExpRecord();
		er.expToxValue = -9999;
		er.expSet = "";
		er.expCAS = "";
		er.expMOA = "";

		String structureCAS = lookup.LookupCASByStructure(chemical, ccTrainingMOA);// first
																					// look
																					// in
																					// LC50
																					// training
																					// set

		if (structureCAS.equals("")) {
			structureCAS = lookup.LookupCASByStructure(chemical, ccPredictionMOA);// then
																					// look
																					// in
																					// LC50
																					// prediction
																					// set
			if (structureCAS.equals("")) {
				structureCAS = lookup.LookupCASByStructure(chemical, ccOverallMOA);// then
																					// look
																					// in
																					// overall
																					// set
																					// that
																					// also
																					// has
																					// chemicals
																					// with
																					// no
																					// LC50
																					// values
																					// (from
																					// MOA
																					// set)
			}
		}

		// System.out.println(structureCAS);

		if (!structureCAS.equals("")) {
			er.expCAS = structureCAS;
		} else {
			er.expCAS = CAS;
		}

		// **********************************************************************************
		// Lookup LC50 value from training and prediction sets:

		er.expToxValue = lookup.LookupExpValByCAS(er.expCAS, ccTrainingMOA);
		if (er.expToxValue != -9999) {
			er.expSet = "Training";
		}

		if (er.expToxValue == -9999) {
			er.expToxValue = lookup.LookupExpValByCAS(er.expCAS, ccPredictionMOA);

			if (er.expToxValue != -9999) {
				er.expSet = "Test";
			}
		}

		// **********************************************************************************
		// Lookup MOA:
		er.expMOA = lookup.LookUpValueInJarFile("overall set.csv", er.expCAS, "CAS", "MOA_Broad", ",");

		// System.out.println("***"+er.expMOA);

		er.expMOA = er.expMOA.replace("Uncoupling/Inhibiting Oxidative Phosphorylation- Electron transport inhibitors", "Uncoupler");

		// TODO: record Set_MOA

//		 System.out.println(CAS+"\t"+structureCAS+"\t"+er.expMOA+"\t"+er.expToxValue);

		return er;

	}
	
	public static Lookup.ExpRecord LookupExpVal_LDA(String CAS, Instances ccOverallMOA, Instances ccTrainingMOA, Instances ccPredictionMOA) {

		Lookup lookup = new Lookup();

		Lookup.ExpRecord er = lookup.new ExpRecord();
		er.expToxValue = -9999;
		er.expSet = "";
		er.expCAS = "";
		er.expMOA = "";

		
		// **********************************************************************************
		// Lookup LC50 value from training and prediction sets:

		er.expToxValue = lookup.LookupExpValByCAS(CAS, ccTrainingMOA);
		if (er.expToxValue != -9999) {
			er.expCAS=CAS;
			er.expSet = "Training";
		}

		if (er.expToxValue == -9999) {
			er.expToxValue = lookup.LookupExpValByCAS(CAS, ccPredictionMOA);

			if (er.expToxValue != -9999) {
				er.expCAS=CAS;
				er.expSet = "Test";
			}
		}

		// **********************************************************************************
		// Lookup MOA:
		er.expMOA = lookup.LookUpValueInJarFile("overall set.csv", CAS, "CAS", "MOA_Broad", ",");
		er.expMOA = er.expMOA.replace("Uncoupling/Inhibiting Oxidative Phosphorylation- Electron transport inhibitors", "Uncoupler");

		// TODO: record Set_MOA
		// System.out.println(CAS+"\t"+structureCAS+"\t"+er.expMOA+"\t"+er.expToxValue);

		return er;

	}

	private String GetRunNumber(String ext) {

		String path = "";

		int count = 1;
		File file = null;

		while (true) {
			path = fileOutputFolder.getAbsolutePath();
			path += File.separator + "batch_";

			if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				path += "Descriptors" + "_" + count + "." + ext;
			} else {
				path += endpoint.replace(" ", "_") + "_" + method + "_" + count + "." + ext;
			}

			file = new File(path);
			if (file.exists())
				count++;
			else
				break;
		}

		return count + "";

	}

	public void loadTrainingData() {
		if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			return;
		}

		statMessage = "Loading training set data...";

		if (method.equals(TESTConstants.ChoiceLDA)) {
			LoadLDAFiles();
			return;
		}

		this.LoadTrainingDataSet();

		if (method.equals(TESTConstants.ChoiceHierarchicalMethod) || method.equals(TESTConstants.ChoiceSingleModelMethod) || method.equals(TESTConstants.ChoiceConsensus)) {
			statMessage = "Loading cluster data file...";
			this.LoadHierarchicalXMLFile();
			// System.out.println("done loading hierarchical xml file");
		}
		if (done)
			return;

		if (method.equals(TESTConstants.ChoiceGroupContributionMethod) || method.equals(TESTConstants.ChoiceConsensus)) {
			statMessage = "Loading frag model...";
			// if (!endpoint.equals(TESTConstants.ChoiceRat_LD50) &&
			// !endpoint.equals(TESTConstants.ChoiceBCF) &&
			// !endpoint.equals(TESTConstants.ChoiceDM_LC50)) {
			this.LoadFragmentXMLFile();
			// }
		}

		if (done)
			return;

		Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
		Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);

	}

	public static Lookup.ExpRecord LookupExpValOld(String CAS, Instances trainingDataSet2d, Instances testDataSet2d) {
		Lookup lookup = new Lookup();

		Lookup.ExpRecord er;

		// lookup first in training set based on CAS
		er = lookup.LookupExpRecordByCAS(CAS, trainingDataSet2d);
		// System.out.println(er.expToxValue);
		if (er.expToxValue != -9999) {
			er.expSet = "Training";
			return er;
		}

		// ******************************************************
		// next lookup in external test set based on CAS:
		er = lookup.LookupExpRecordByCAS(CAS, testDataSet2d);
		// System.out.println(er.expToxValue);
		if (er.expToxValue != -9999) {
			er.expSet = "Test";
			return er;
		}

		// ******************************************************
		er = lookup.new ExpRecord();
		er.expToxValue = -9999;
		er.expSet = "";
		er.expCAS = "";

		return er;

	}

//	public static void CreateStructureImage(String CAS, String DestFolder) {
//
//		ToxPredictor.Utilities.GetStructureImagesFromJar g = new ToxPredictor.Utilities.GetStructureImagesFromJar();
//		ParseChemidplus p = new ParseChemidplus();
//		File ImageFolder = new File(DestFolder);
//		if (!ImageFolder.exists())
//			ImageFolder.mkdirs();
//
//		IAtomContainer moleculei = null;
//
//		File imageFile = new File(ImageFolder.getAbsolutePath() + "/" + CAS + ".png");
//
//		if (!imageFile.exists()) {
//			int ret = g.GetImageFileFromJar(CAS, ImageFolder.getAbsolutePath());
//
//			// System.out.println(CAS+"\t"+ret);
//			if (ret == -1) {
//				moleculei = p.LoadChemicalFromMolFileInJar("ValidatedStructures2d/" + CAS + ".mol");
//				if (moleculei != null) {
					// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(
					// moleculei, CAS, ImageFolder.getAbsolutePath(),
					// false, true, true, 200);
//
//					ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei, CAS, ImageFolder.getAbsolutePath());
//
//				} else {
//					moleculei = p.GetBestMolecule(CAS);
//					if (moleculei != null) {
//						// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei,
//						// CAS, ImageFolder.getAbsolutePath(), false, true,
//						// true,200);
//						ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(moleculei, CAS, ImageFolder.getAbsolutePath());
//					}
//				}
//			}
//
//		} else {
//			// System.out.println("Image for "+CAS+" exists");
//		}
//
//		// System.out.print("done\n");
//	}
	
	

		
	public void init(IAtomContainerSet moleculeSet, boolean WriteResultsToFile, boolean useFragmentsConstraint, boolean FindFragments, File fileOutputFolder, Object gui, boolean Use3D,
			boolean is3Dpresent, String endpoint, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String DescriptorSet, String method, int taskType) {

		this.moleculeSet = moleculeSet;

		this.WriteResultsToFile = WriteResultsToFile;
		this.useFragmentsConstraint = useFragmentsConstraint;
		this.FindFragments = FindFragments;
		this.fileOutputFolder = fileOutputFolder;

		// System.out.println("here1"+this.fileOutputFolder);

		this.gui = gui;

		this.WriteResultsToFile = WriteResultsToFile;
		this.useFragmentsConstraint = useFragmentsConstraint;
		this.FindFragments = FindFragments;

		this.gui = gui;

		this.Use3D = Use3D;
		this.is3Dpresent = is3Dpresent;
		this.endpoint = endpoint;
		this.isBinaryEndpoint = isBinaryEndpoint;
		this.isLogMolarEndpoint = isLogMolarEndpoint;

		this.DescriptorSet = DescriptorSet;
		this.method = method;

		if (gui instanceof TESTApplication) {
			abbrev = ((TESTApplication) gui).htAbbrevChoice.get(endpoint);
		} 
//		else if (gui instanceof ToxApplet7) {
//			abbrev = ((ToxApplet7) gui).htAbbrevChoice.get(endpoint);
//		}
		this.taskType = taskType;

	}

	public void init(IAtomContainerSet moleculeSet, boolean WriteResultsToFile, boolean useFragmentsConstraint, boolean FindFragments, File fileOutputFolder, Object gui, boolean Use3D,
			boolean is3Dpresent, String endpoint, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String DescriptorSet, String method, int taskType, String runNumber,
			TaskCalculations taskCalc) {

		this.moleculeSet = moleculeSet;

		this.WriteResultsToFile = WriteResultsToFile;
		this.useFragmentsConstraint = useFragmentsConstraint;
		this.FindFragments = FindFragments;
		this.fileOutputFolder = fileOutputFolder;

		// System.out.println("here1"+this.fileOutputFolder);

		this.gui = gui;

		this.WriteResultsToFile = WriteResultsToFile;
		this.useFragmentsConstraint = useFragmentsConstraint;
		this.FindFragments = FindFragments;

		this.gui = gui;

		this.Use3D = Use3D;
		this.is3Dpresent = is3Dpresent;
		this.endpoint = endpoint;
		this.isBinaryEndpoint = isBinaryEndpoint;
		this.isLogMolarEndpoint = isLogMolarEndpoint;

		this.DescriptorSet = DescriptorSet;
		this.method = method;

		if (gui instanceof TESTApplication) {
			abbrev = ((TESTApplication) gui).htAbbrevChoice.get(endpoint);
		} 
//		else if (gui instanceof ToxApplet7) {
//			abbrev = ((ToxApplet7) gui).htAbbrevChoice.get(endpoint);
//		}
		
		this.taskType = taskType;
		this.runNumber = runNumber;

		this.ht_ccTraining = taskCalc.ht_ccTraining;
		this.ht_ccTrainingFrag = taskCalc.ht_ccTrainingFrag;
		this.ht_ccPrediction = taskCalc.ht_ccPrediction;
		this.ht_allResults = taskCalc.ht_allResults;
		this.ht_allResultsFrag = taskCalc.ht_allResultsFrag;

		// System.out.println("***"+this.ht_ccTraining.get(endpoint).numInstances());

	}

	/**
	 * Called to start the task.
	 */
	public void go() {
		worker = new SwingWorker() {
			public Object construct() {
				current = 0;
				done = false;
				// canceled = false;
				statMessage = null;
				return new ActualTask();
			}
		};
		worker.start();
	}

	/**
	 * Called to find out how much work needs to be done.
	 * 
	 * @return Length of Task
	 */
	public int getLengthOfTask() {
		return lengthOfTask;
	}

	/**
	 * Called to find out how much has been done.
	 * 
	 * @return Current progress in %
	 */
	public int getCurrent() {
		return current;
	}

	public void stop() {
		done = true;
		this.df.done = true;
		// this.df3D.done=true;
		// this.cg.done=true;
		// this.ptFDA.fda.done=true;
		this.arxr.done = true;

		statMessage = "canceled";

		if (gui instanceof TESTApplication) {
			((TESTApplication) gui).setCursor(Utilities.defaultCursor);
		} 
//		else if (gui instanceof ToxApplet7) {
//			((ToxApplet7) gui).setCursor(Utilities.defaultCursor);
//		}

		// gui.setCursor(Utilities.defaultCursor);

		System.out.println("\n****stop!****\n");

		// worker.interrupt();// this doesnt seem to want to work!

	}

	/**
	 * Called to find out if the task has completed.
	 * 
	 * @return Done
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the most recent status message, or null if there is no current
	 * status message.
	 * 
	 * @return statMessage
	 */
	public String getMessage() {
		return statMessage;
	}

	public void setMessage(String message) {
		statMessage = message;
	}

	private void calculate(int molNum, int molCount, IAtomContainer m, FileWriter fw, FileWriter fw2, FileWriter fw3, int index, ArrayList<String> methods) {

		DescriptorData dd = new DescriptorData();
		dd.ID = (String) m.getProperty("CAS");
		String CAS = dd.ID;

		String strOutputFolder1 = fileOutputFolder.getAbsolutePath() + File.separator + "ToxRuns";
		File fileOutputFolder1 = new File(strOutputFolder1);
		if (!fileOutputFolder1.exists())
			fileOutputFolder1.mkdir();

		String strOutputFolder2 = strOutputFolder1 + File.separator + "ToxRun_" + dd.ID;
		File fileOutputFolder2 = new File(strOutputFolder2);
		if (!fileOutputFolder2.exists())
			fileOutputFolder2.mkdir();

		String strOutputFolder3 = strOutputFolder2 + File.separator + endpoint;
		File fileOutputFolder3 = new File(strOutputFolder3);

		String strOutputFolder = strOutputFolder3;

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {
			if (!fileOutputFolder3.exists())
				fileOutputFolder3.mkdir();
		}

		String strOutputFolder4 = strOutputFolder2 + File.separator + "StructureData";
		File osdf = new File(strOutputFolder4);
		if (!osdf.exists())
			osdf.mkdir();

		String outputFilePath = strOutputFolder4 + File.separator + dd.ID + ".mol";
		ToxPredictor.misc.MolFileUtilities.WriteMolFile(m, dd.ID, outputFilePath, true);

		if (done)
			return;

		// ***************************************************

		// if (Use3D) {
		// statMessage = "Calculating 3D Quantum descriptors...";
		// df3D.CalculateDescriptors(m, dd);
		// System.out.println("qpmax=" + dd.qpmax);
		// }

		if (Use3D) {
			statMessage = "Calculating 2D and additional 3D descriptors...";
		} else {
			statMessage = "Calculating 2D descriptors...";
		}

		Integer Index = (Integer) m.getProperty("Index");
		// statMessage += "Molecule #" + Index;
		statMessage += "Molecule ID = " + dd.ID + " (" + (molNum + 1) + " of " + molCount + ")";

		df.Calculate3DDescriptors = Use3D;
		dd.ThreeD = Use3D;

		// calculate 2D and non quantum 3D descriptors:

		// System.out.println(m.getAtomCount());

		// System.out.println(m.getProperty("CAS"));
		//
		// for (int i=0;i<m.getAtomCount();i++) {
		// System.out.println(m.getAtom(i).getSymbol());
		// }
		// System.out.println("");
		// for (int i=0;i<m.getBondCount();i++) {
		// System.out.println(m.getBond(i).getOrder());
		// }

		int descresult = df.CalculateDescriptors(m, dd, WriteResultsToFile, true, FindFragments, strOutputFolder4);

		// 11/1/16 - added better handling of what to do if have descriptor
		// calculation error
		if (descresult == -1) {
			// done=true; // commented out: dont kill rest of run for batch
			statMessage = "Error calculating descriptors";
			System.out.println("Error calculating descriptors for " + dd.ID);

			// 10/13/16: omit to speed up big sdf file calcs

			// if (gui instanceof fraMain7) {
			// JOptionPane.showMessageDialog((fraMain7) gui,
			// "Error calculating descriptors for " + dd.CAS);
			// } else if (gui instanceof ToxApplet7) {
			// JOptionPane.showMessageDialog((ToxApplet7) gui,
			// "Error calculating descriptors for " + dd.CAS);
			// }

			if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
				if (taskType == TESTConstants.typeTaskBatch) {
					WriteDescriptorResultsForChemicalToWebPage(fw, index, CAS, "Error calculating descriptors");

					try {
						fw2.write(CAS + del + "error\r\n");
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
				} else {
					// TODO add code to generate main page with error message in
					// it for single chemical mode
				}
			}

			// JOptionPane.showMessageDialog(gui, "Error calculating
			// descriptors");//bob
			return;
		} else {
			dd.WriteToFileTEXT(strOutputFolder4, del);

			if (endpoint.equals(TESTConstants.ChoiceDescriptors) && taskType == TESTConstants.typeTaskBatch) {
				df.WriteCSVLine(fw2, dd, del);
			}

		}

		ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, "testchemical", strOutputFolder4);

		if (done)
			return;

		if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

			// array to store predictions for all methods for consensus method:
			ArrayList predictedToxicities = new ArrayList();
			ArrayList predictedUncertainties = new ArrayList();

			AllResults allResults = ht_allResults.get(endpoint);// shortcut to
																// results
																// object
			AllResults allResultsFrag = ht_allResultsFrag.get(endpoint); // shortcut
																			// to
																			// results
																			// object

			Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
			Instances trainingDataSetFrag = ht_ccTrainingFrag.get(endpoint);
			Instances testDataSet2d = ht_ccPrediction.get(endpoint);

			if (method.equals(TESTConstants.ChoiceLDA)) {// this method uses
															// totally different
															// split!
				trainingDataSet2d = ccTrainingMOA;
				testDataSet2d = ccPredictionMOA;
			}

			String ToxFieldName = "Tox";

			java.util.Hashtable ht = dd.CreateDataHashtable(ToxFieldName, true, true, false, false, false);

			String[] varArrayFrag = null;
			Instances evalInstancesFrag = null;
			// Create instances for test chemical (for GCM it will only contain
			// fragment descriptors):
			if (!method.equals(TESTConstants.ChoiceLDA)) {
				varArrayFrag = CreateVarListFromTrainingSet(trainingDataSetFrag);
				evalInstancesFrag = iu.createInstances(ht, varArrayFrag);
			}

			String[] varArray2d = CreateVarListFromTrainingSet(trainingDataSet2d);
			Instances evalInstances2d = iu.createInstances(ht, varArray2d);
			Instance evalInstance2d = evalInstances2d.firstInstance();

			Lookup.ExpRecord er = null;

			if (method.equals(TESTConstants.ChoiceLDA)) {
				er = LookupExpVal_LDA(CAS, evalInstance2d, ccOverallMOA, ccTrainingMOA, ccPredictionMOA);
			} else {
				er = LookupExpVal(CAS, evalInstance2d, trainingDataSet2d, testDataSet2d);
			}

			if (done)
				return;
			// **************************************************

			int result = 0;

			statMessage = "Calculating " + endpoint + "...";
			// statMessage += "Molecule ID = " + dd.CAS;

			// statMessage += "Molecule ID = " + dd.CAS+" (#"+(molNum+1)+")";
			statMessage += "Molecule ID = " + dd.ID + " (" + (molNum + 1) + " of " + molCount + ")";
			// statMessage += "Molecule #" + Index;

			// ******************************************************

			double predToxVal = -9999;
			double predToxUnc = 1;// TODO: add code to calculate this

			ReportOptions options = new ReportOptions();
			options.reportBase = strOutputFolder;
			options.embedImages = true;

			// if (method.equals(TESTConstants.ChoiceFDAMethod)) {
			// result = ptFDA.CalculateToxicity(CAS, method,
			// endpoint,isBinaryEndpoint, isLogMolarEndpoint,abbrev,
			// strOutputFolder, DescriptorSet, evalInstances2d,
			// trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
			// useFragmentsConstraint, this, er);
			// predToxVal = ptFDA.predToxVal;
			if (method.equals(TESTConstants.ChoiceHierarchicalMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d,
						trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceNearestNeighborMethod)) {
				result = ptNN.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, evalInstances2d, trainingDataSet2d, testDataSet2d,
						dd.MW, er, options);
				predToxVal = ptNN.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceSingleModelMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResults, evalInstances2d, evalInstance2d,
						trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceGroupContributionMethod)) {
				result = ptH.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResultsFrag, evalInstancesFrag, evalInstance2d,
						trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
				predToxVal = ptH.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceRandomForrestCaesar)) {
				result = ptRFC.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, evalInstances2d, trainingDataSet2d, testDataSet2d,
						dd.MW, this, er, ht, options);
				predToxVal = ptRFC.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceLDA)) {
				result = ptLDA.CalculateToxicity(CAS, method, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, htAllResultsMOA, htTrainingSetsMOA,
						htAllResultsLC50, htTrainingSetsLC50, evalInstances2d, ccTrainingMOA, ccPredictionMOA, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, vecMOA, options);
				predToxVal = ptLDA.predToxVal;
			} else if (method.equals(TESTConstants.ChoiceConsensus)) {
				// TODO move following into a subroutine:
				// Hierarchical:
				result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceHierarchicalMethod, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResults,
						evalInstances2d, evalInstance2d, trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
				predictedToxicities.add(ptH.predToxVal);
				predictedUncertainties.add(ptH.predToxUnc);

				// Single Model
				if (TESTConstants.haveSingleModelMethod(endpoint)) {
					// System.out.println("SM r2="+orSM.getR2());
					result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceSingleModelMethod, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, allResults,
							evalInstances2d, evalInstance2d, trainingDataSet2d, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours, options);
					predictedToxicities.add(ptH.predToxVal);
					predictedUncertainties.add(ptH.predToxUnc);
				}

				// Group contribution:
				if (TESTConstants.haveGroupContributionMethod(endpoint)) {
					result = ptH.CalculateToxicity(CAS, TESTConstants.ChoiceGroupContributionMethod, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet,
							allResultsFrag, evalInstancesFrag, evalInstance2d, trainingDataSetFrag, trainingDataSet2d, testDataSet2d, dd.MW, dd.MW_Frag, useFragmentsConstraint, er, minFileAgeHours,
							options);

					predictedToxicities.add(ptH.predToxVal);
					predictedUncertainties.add(ptH.predToxUnc);
				}

				// FDA:
				// result = ptFDA.CalculateToxicity(CAS,
				// TESTConstants.ChoiceFDAMethod, endpoint,isBinaryEndpoint,
				// isLogMolarEndpoint,abbrev,
				// strOutputFolder, DescriptorSet, evalInstances2d,
				// trainingDataSet2d, testDataSet2d, dd.MW,dd.MW_Frag,
				// useFragmentsConstraint, this, er);
				// predictedToxicities.add(ptFDA.predToxVal);
				// predictedUncertainties.add(ptFDA.predToxUnc);

				// Nearest neighbor:
				result = ptNN.CalculateToxicity(CAS, TESTConstants.ChoiceNearestNeighborMethod, endpoint, isBinaryEndpoint, isLogMolarEndpoint, abbrev, strOutputFolder, DescriptorSet, evalInstances2d,
						trainingDataSet2d, testDataSet2d, dd.MW, er, options);
				predictedToxicities.add(ptNN.predToxVal);
				predictedUncertainties.add(ptNN.predToxUnc);

				// for (int i=0;i<methods.size();i++) {
				// System.out.println(methods.get(i)+"\t"+predictedToxicities.get(i));
				// }

				predToxVal = calculateConsensusToxicity(predictedToxicities);
				predToxUnc = 1;// TODO: add code to calculate this

				predictedToxicities.add(predToxVal);

				double[] Mean = trainingDataSet2d.getMeans();
				double[] StdDev = trainingDataSet2d.getStdDevs();

				Hashtable<Double, Instance> htTestMatch = this.FindClosestChemicals(evalInstance2d, testDataSet2d, true, false, true, Mean, StdDev);

				Hashtable<Double, Instance> htTrainMatch = FindClosestChemicals(evalInstance2d, trainingDataSet2d, true, false, true, Mean, StdDev);

				PredictToxicityWebPageCreator ptwc = new PredictToxicityWebPageCreator();

				ptwc.WriteConsensusResultsWebPages(predToxVal, predToxUnc, method, strOutputFolder, CAS, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, dd.MW, "OK", htTestMatch,
						htTrainMatch, methods, predictedToxicities, predictedUncertainties, options);

			} // end choice consensus

			if (result == -1) {
				done = true;
				return;
			}

			if (taskType == TESTConstants.typeTaskBatch) {
				if (!isBinaryEndpoint) {
					if (method.equals(TESTConstants.ChoiceLDA)) {

						double maxScore = ptLDA.maxScore;
						String expMOA = er.expMOA;
						String bestMOA = ptLDA.bestMOA;

						WriteToxicityResultsForChemical(fw, fw2, index, dd.ID, er.expToxValue, predToxVal, dd.MW, del, "", expMOA, bestMOA, maxScore);
					} else {
						WriteToxicityResultsForChemical(fw, fw2, index, dd.ID, er.expToxValue, predToxVal, dd.MW, del, "", "", "", -1);
					}

				} else {
					WriteBinaryToxResultsForChemical(fw, fw2, index, dd.ID, er.expToxValue, predToxVal, dd.MW, del, "");
				}

				if (method.equals(TESTConstants.ChoiceConsensus)) {
					WriteToxicityResultsForChemicalAllMethods(fw3, index, dd.ID, er.expToxValue, methods, predictedToxicities, dd.MW, del, "");
				}
			}
		} else {// Descriptors
			if (taskType == TESTConstants.typeTaskBatch)
				WriteDescriptorResultsForChemicalToWebPage(fw, index, CAS, "");
		}

	}

	// Finds matches in test set and compiles their predicted values
	public static Hashtable<Double, Instance> FindClosestChemicals(Instance evalInstance2d, Instances dataSet2d, boolean ExcludeTestChemicalCASFromTrainingSet,
			boolean ExcludeTestChemical2dIsomerFromTrainingSet, boolean MustExceedSCmin, double[] Mean, double[] StdDev) {

		Lookup lookup = new Lookup();
		// TODO make sure structure images for matches are there!

		// ChemicalCluster ccTest = new ChemicalCluster(dataSet2d);

		// double[] Mean = ccTest.CalculateMeans();
		// double[] StdDev = ccTest.CalculateStdDevs();

		Hashtable ht = new Hashtable();

		for (int i = 0; i < dataSet2d.numInstances(); i++) {
			Instance chemicali = dataSet2d.instance(i);
			String CAS = chemicali.getName();

			double SimCoeff = -1;
			SimCoeff = CalculateCosineCoefficient(evalInstance2d, chemicali, Mean, StdDev);

			if (ExcludeTestChemicalCASFromTrainingSet) {
				String TestCAS = evalInstance2d.getName();
				if (CAS.equals(TestCAS)) {
					continue;
				}
			}

			if (ExcludeTestChemical2dIsomerFromTrainingSet) {
				if (SimCoeff > 0.999) {
					continue;
				}
			}

			if (!MustExceedSCmin || SimCoeff > SCmin)
				ht.put(new Double(SimCoeff), chemicali);

		}

		return ht;

		// for (int i=0;i<tableV.size();i++) System.out.print(tableV.get(i));

	}

	public static double CalculateCosineCoefficient(Instance c1, Instance c2, double[] Mean, double[] StdDev) {

		double TC = 0;

		double SumXY = 0;
		double SumX2 = 0;
		double SumY2 = 0;

		for (int j = 2; j < c1.numValues(); j++) {
			double xj = c1.value(j);
			double yj = c2.value(j);

			if (StdDev[j] == 0) {
				xj = 0;
				yj = 0;
			} else {
				xj = (xj - Mean[j]) / StdDev[j];
				yj = (yj - Mean[j]) / StdDev[j];
			}

			SumXY += xj * yj;
			SumX2 += xj * xj;
			SumY2 += yj * yj;
		}

		TC = SumXY / Math.sqrt(SumX2 * SumY2);
		return TC;
	}

	private void LoadFragmentXMLFile() {

		String xmlFileName = "";

		xmlFileName = abbrev + "/" + abbrev + "_training_set-frag.xml";
		boolean HaveFile = this.HaveFileInJar(xmlFileName);

		if (!HaveFile) {
			return;
		}

		try {

			if (ht_allResultsFrag.get(endpoint) == null) {
				ht_allResultsFrag.put(endpoint, arxr.readAllResults(xmlFileName, ht_ccTrainingFrag.get(endpoint), true));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean HaveFileInJar(String filepath) {

		try {
			java.io.InputStream is = this.getClass().getClassLoader().getResourceAsStream(filepath);
			is.read();
			return true;

		} catch (Exception e) {
			// e.printStackTrace();
			// System.out.println("here");
			return false;
		}

	}

	// TODO- make it so that file loading can be aborted

	private void LoadTrainingDataSet() {
		String csvTraining_2d;
		String csvTrainingFrag;
		String csvPrediction_2d;

		String train2d = "_training_set-2d.csv";
		String trainfrag = "_training_set-frag.csv";
		String pred2d = "_prediction_set-2d.csv";

		csvTraining_2d = abbrev + "/" + abbrev + train2d;
		csvPrediction_2d = abbrev + "/" + abbrev + pred2d;
		csvTrainingFrag = abbrev + "/" + abbrev + trainfrag;

		try {

			CSVLoader atf = new CSVLoader();

			if (ht_ccTraining.get(endpoint) == null) {

				// System.out.println(csvTraining_2d);
				Instances train = atf.getDataSetFromJarFile(csvTraining_2d);
				ht_ccTraining.put(endpoint, train);

			}

			if (ht_ccPrediction.get(endpoint) == null) {// always load so that
														// can
				// find match in pred set
				Instances pred = atf.getDataSetFromJarFile(csvPrediction_2d);
				ht_ccPrediction.put(endpoint, pred);

			}

			if (ht_ccTrainingFrag.get(endpoint) == null) {
				try {
					Instances trainFrag = atf.getDataSetFromJarFile(csvTrainingFrag);
					// System.out.println(trainFrag.numInstances());
					ht_ccTrainingFrag.put(endpoint, trainFrag);
				} catch (Exception e) {
					// System.out.println(e);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void LoadHierarchicalXMLFile() {

		String xmlFileName = "";

		xmlFileName = abbrev + "/" + abbrev + "_training_set-2d.xml";
		boolean HaveFile = this.HaveFileInJar(xmlFileName);

		if (!HaveFile) {
			return;
		}

		try {

			if (ht_allResults.get(endpoint) == null) {
				ht_allResults.put(endpoint, arxr.readAllResults(xmlFileName, ht_ccTraining.get(endpoint), true, this));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The actual long running task. This runs in a SwingWorker thread.
	 */

	public class ActualTask {

		ActualTask() {
			
			FileWriter fw = null;
			FileWriter fw2 = null;
			FileWriter fw3 = null;

			File htmlFile = null;
			File txtFile = null;
			File txtFileAll = null;

			current = 0;

			if (gui instanceof TESTApplication) {
				((TESTApplication) gui).setCursor(Utilities.waitCursor);
			} 
//			else if (gui instanceof ToxApplet7) {
//				((ToxApplet7) gui).setCursor(Utilities.waitCursor);
//			}

			df.done = false;

			// ************************************************
			int result = -9999;

			loadTrainingData();
			if (done)
				return;

			// ************************************************
			// Figure out methods:
			ArrayList methods = getMethods(endpoint); // shortcut to results
														// object
			// *******************************************************

			if (taskType == TESTConstants.typeTaskBatch) {

				if (runNumber.equals("")) {
					runNumber = GetRunNumber("html");
				}

				String textFileNameMethod;
				String htmlFileNameMethod;
				String textFileNameAll;

				if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
					textFileNameMethod = "batch_Descriptors" + "_" + runNumber + ".txt";
					htmlFileNameMethod = "batch_Descriptors" + "_" + runNumber + ".html";
				} else {
					textFileNameMethod = "Batch_" + endpoint.replace(" ", "_") + "_" + method + "_" + runNumber + ".txt";
					htmlFileNameMethod = "Batch_" + endpoint.replace(" ", "_") + "_" + method + "_" + runNumber + ".html";
				}

				if (method.equals(TESTConstants.ChoiceConsensus)) {
					textFileNameAll = "Batch_" + endpoint.replace(" ", "_") + "_all_methods_" + runNumber + ".txt";
				} else {
					textFileNameAll = "N/A";
				}

				htmlFile = new File(fileOutputFolder.getAbsolutePath() + File.separator + htmlFileNameMethod);
				txtFile = new File(fileOutputFolder.getAbsolutePath() + File.separator + textFileNameMethod);
				txtFileAll = new File(fileOutputFolder.getAbsolutePath() + File.separator + textFileNameAll);

				try {
					fw = new FileWriter(htmlFile);
					fw2 = new FileWriter(txtFile);

					WriteOverallHtmlHeader(fw, textFileNameMethod, textFileNameAll);

					if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
						df.WriteCSVHeader(fw2, del);
					} else {
						WriteOverallTextHeader(fw2, del);

						if (method.equals(TESTConstants.ChoiceConsensus)) {
							fw3 = new FileWriter(txtFileAll);
							WriteOverallTextHeaderAllMethods(fw3, del, methods);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (int i = 0; i < moleculeSet.getAtomContainerCount(); i++) {
				
				// if (i==10) break;//for debug
				if (done)
					return;
				AtomContainer m = (AtomContainer) moleculeSet.getAtomContainer(i);
				
				
//				runWebtest(m);
				

				int index = -1;
				String error = null;

				if (taskType == TESTConstants.typeTaskBatch) {
					index = (Integer) m.getProperty("Index");
					error = (String) m.getProperty("Error");
				} else {
					index = 0;
					error = "";
				}

				if (!error.equals("")) {// We have an error in the structure of
										// the chemical- dont do normal calcs!

					String CAS = (String) m.getProperty("CAS");

					if (!endpoint.equals(TESTConstants.ChoiceDescriptors)) {

						Instances trainingDataSet2d = ht_ccTraining.get(endpoint);
						Instances testDataSet2d = ht_ccPrediction.get(endpoint);

//						Lookup.ExpRecord er = LookupExpVal(CAS, trainingDataSet2d, testDataSet2d);
						Lookup.ExpRecord er = null;
						
						if (method.equals(TESTConstants.ChoiceLDA)) {
							er=LookupExpVal_LDA(CAS, ccOverallMOA,ccTrainingMOA,ccPredictionMOA);
						} else {
							er = LookupExpVal(CAS, trainingDataSet2d, testDataSet2d);
						}

						if (taskType == TESTConstants.typeTaskBatch) {
							if (!isBinaryEndpoint) {
								WriteToxicityResultsForChemical(fw, fw2, index, CAS, er.expToxValue, -9999, er.MW, del, error, er.expMOA, "", -1);

							} else {
								WriteBinaryToxResultsForChemical(fw, fw2, index, CAS, er.expToxValue, -9999, er.MW, del, error);
							}

							if (method.equals(TESTConstants.ChoiceConsensus)) {
								WriteToxicityResultsForChemicalAllMethods(fw3, index, CAS, er.expToxValue, methods, null, er.MW, del, error);
							}
						}

					} else {// not descriptor run

						if (taskType == TESTConstants.typeTaskBatch) {
							WriteDescriptorResultsForChemicalToWebPage(fw, index, CAS, error);

							try {
								fw2.write(CAS + del + "Error:" + error + "\r\n");
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
					continue;
				}

				calculate(i, moleculeSet.getAtomContainerCount(), m, fw, fw2, fw3, index, methods);
				// System.out.println("Total
				// Memory"+Runtime.getRuntime().totalMemory());
				// System.out.println("Free
				// Memory"+Runtime.getRuntime().freeMemory());

			} // end loop over molecules

			if (done)
				return;

			if (taskType == TESTConstants.typeTaskBatch) {
				try {
					fw.write("</table></body></html>\r\n");
					fw.close();

					fw2.close();

					if (method.equals(TESTConstants.ChoiceConsensus)) {
						fw3.close();

					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				IAtomContainer m = moleculeSet.getAtomContainer(0);
				String CAS = (String) m.getProperty("CAS");
				// TODO
				String strOutputFolder1 = fileOutputFolder.getAbsolutePath() + File.separator + "ToxRuns";
				File fileOutputFolder1 = new File(strOutputFolder1);
				if (!fileOutputFolder1.exists())
					fileOutputFolder1.mkdir();

				String strOutputFolder2 = strOutputFolder1 + File.separator + "ToxRun_" + CAS;
				File fileOutputFolder2 = new File(strOutputFolder2);
				if (!fileOutputFolder2.exists())
					fileOutputFolder2.mkdir();

				String strOutputFolder3 = strOutputFolder2 + File.separator + "StructureData";
				File fileOutputFolder3 = new File(strOutputFolder3);
				if (!fileOutputFolder3.exists())
					fileOutputFolder3.mkdir();

				String strOutputFolder4 = strOutputFolder2 + File.separator + endpoint;
				File fileOutputFolder4 = new File(strOutputFolder4);
				if (!fileOutputFolder4.exists())
					fileOutputFolder4.mkdir();

				String filename = "";
				if (endpoint.equals(TESTConstants.ChoiceDescriptors)) {
					filename = "DescriptorData.html";
					htmlFile = new File(fileOutputFolder3.getAbsolutePath() + File.separator + filename);

				} else {
					filename = "PredictionResults";
					filename += method.replaceAll(" ", "");
					filename += ".html";
					htmlFile = new File(fileOutputFolder4.getAbsolutePath() + File.separator + filename);
				}

				// System.out.println(htmlFile);

				// here add file to recent file list:
				if (gui instanceof TESTApplication) {
					String molFilePath = strOutputFolder3 + File.separator + CAS + ".mol";

					TESTApplication gui8 = (TESTApplication) gui;

					gui8.as.addFilePath(molFilePath);
					gui8.as.saveSettingsToFile();
//					gui8.setupRecentFiles();
					// we are done:
					gui8.setCursor(Utilities.defaultCursor);
				} 
//				else if (gui instanceof ToxApplet7) {
//					((ToxApplet7) gui).setCursor(Utilities.defaultCursor);
//				}

			}

			done = true;

			try {

				// myURL=htmlFile.toURL();
				// BrowserLauncher.openURL(myURL.toString());

				URL myURL;
				myURL = htmlFile.toURI().toURL();

				String strURL = myURL.toString();

				// Fix for files on EPA's network drive:
				strURL = strURL.replace("file:////", "file://///");

				if (gui instanceof TESTApplication) {
//					BrowserLauncher launcher = new BrowserLauncher(null);
//					launcher.openURLinBrowser(strURL);// doesnt seem to work for
														// applet!

					MyBrowserLauncher.launch(htmlFile.toURI());
					
					
					// System.out.println(strURL);

					if (taskType == TESTConstants.typeTaskBatch) {
						((TESTApplication) gui).as.addBatchFilePath(htmlFile.getAbsolutePath());
						((TESTApplication) gui).as.saveSettingsToFile();
//						((TESTApplication) gui).setupRecentBatchFiles();
					}

				} 
//				else if (gui instanceof ToxApplet7) {
//					ToxPredictor.Utilities.BrowserLauncher.openURL(strURL);
//					// ((ToxApplet7)gui).getAppletContext().showDocument(myURL,"_blank");
//				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (gui instanceof TESTApplication) {
				((TESTApplication) gui).setCursor(Utilities.defaultCursor);
			} 
//			else if (gui instanceof ToxApplet7) {
//				((ToxApplet7) gui).setCursor(Utilities.defaultCursor);
//			}

		}

		
	}// end ActualTaskConstructor

	private void LoadLDAFiles() {
		if (vecMOA.size() > 0)
			return;

		// Load overall sets:

		try {
			CSVLoader atf = new CSVLoader();

			ccTrainingMOA = atf.getDataSetFromJarFile("LC50 training set.csv");
			ccPredictionMOA = atf.getDataSetFromJarFile("LC50 prediction set.csv");
			ccOverallMOA = atf.getDataSetFromJarFile("overall_set.csv");

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Specific MOAs:
		// vecMOA.add("AChE inhibition-Carbamate");
		// vecMOA.add("AChE inhibition-Organophosphate");
		// vecMOA.add("Narcosis-Ester");
		// vecMOA.add("Narcosis-Nonpolar");
		// vecMOA.add("Narcosis-Polar");
		// vecMOA.add("Neurotoxicity-Organochlorine");
		// vecMOA.add("Neurotoxicity-Pyrethroid");
		// vecMOA.add("Reactivity");
		// vecMOA.add("Uncoupling Oxidative Phosphorylation");

		// broad MOAs:
		vecMOA.add("AChE inhibition");
		// vecMOA.add("Anticoagulation");//no LC50 model
		// vecMOA.add("nAChR Agonism");//no LC50 model
		vecMOA.add("Narcosis");
		vecMOA.add("Neurotoxicity");
		vecMOA.add("Reactivity");
		// vecMOA.add("Uncoupling_Inhibiting Oxidative Phosphorylation- Electron
		// transport inhibitors");
		vecMOA.add("Uncoupler");

		String endpointInFile = "mace_moa";
		CSVLoader atf = new CSVLoader();

		for (int i = 0; i < vecMOA.size(); i++) {

			try {

				String MOAi = vecMOA.get(i);

				String trainingFilePathMOA = "LDA/" + MOAi + ".csv";
				// Note need LDA folder in caps or it wont properly load from
				// jar file!

				Instances trainingDatasetMOA = atf.getDataSetFromJarFile(trainingFilePathMOA);
				htTrainingSetsMOA.put(MOAi, trainingDatasetMOA);

				String xmlFilePathMOA = "LDA/" + MOAi + ".xml";

				// System.out.println(xmlFilePathMOA);

				AllResults allResultsMOA = arxr.readAllResults(xmlFilePathMOA, trainingDatasetMOA, true);

				htAllResultsMOA.put(MOAi, allResultsMOA);

				String trainingFilePathLC50 = "LC50/" + MOAi + ".csv";
				File tfpl = new File(trainingFilePathLC50);

				// System.out.println(trainingFilePathLC50);

				Instances trainingDatasetLC50 = atf.getDataSetFromJarFile(trainingFilePathLC50);
				htTrainingSetsLC50.put(MOAi, trainingDatasetLC50);

				String xmlFilePathLC50 = "LC50/" + MOAi + ".xml";
				AllResults allResultsLC50 = arxr.readAllResults(xmlFilePathLC50, trainingDatasetLC50, true);
				htAllResultsLC50.put(MOAi, allResultsLC50);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} // end loop over MOAs

	}

}
