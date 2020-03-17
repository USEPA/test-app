package ToxPredictor.Application.Calculations;

import wekalite.*;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import QSAR.validation2.*;
import QSAR.qsarOptimal.AllResults;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.misc.Lookup;

public class PredictToxicityLDA {

	private static final Logger logger = LogManager.getLogger(PredictToxicityLDA.class);

	int classIndex = 1;
	int chemicalNameIndex = 0;

	// Validation2 v2;
	LDAMethod lm = new LDAMethod();

	// double expToxVal=-9999;
	public double predToxVal = -9999;
	public double predToxUnc=-9999;

	public String bestMOA;

	public double maxScore;
	public String[] predArrayMOA;
	public String[] predArrayLC50;
	public TestChemical chemical=null;
	

	private void setOptions() {
		lm.useFragments = false;// never use fragment constraint since doesnt
								// help
		lm.minPosScore = 0.5;// for maximum coverage- need to have program flag
								// values less than 0.5
	}

	/**
	 * 
	 * @param CAS
	 * @param method
	 * @param endpoint
	 * @param isBinaryEndpoint
	 * @param isLogMolarEndpoint
	 * @param abbrev
	 * @param OutputFolder
	 * @param DescriptorSet
	 * @param htAllResultsMOA
	 * @param htTrainingSetsMOA
	 * @param htAllResultsLC50
	 * @param htTrainingSetsLC50
	 * @param evalInstances
	 * @param evalInstance2d
	 * @param trainingDataSet
	 * @param trainingDataSet2d
	 * @param testDataSet2d
	 * @param MW
	 * @param MW_Frag
	 * @param useFragmentConstraint
	 * @param er
	 * @param vecMOA
	 * @return
	 */
	public int CalculateToxicity(String CAS, String method, String endpoint, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String abbrev, String OutputFolder, String DescriptorSet,
			Hashtable<String, AllResults> htAllResultsMOA, Hashtable<String, Instances> htTrainingSetsMOA, Hashtable<String, AllResults> htAllResultsLC50,
			Hashtable<String, Instances> htTrainingSetsLC50, Instances evalInstances2d, Instances trainingDataSet2d, Instances testDataSet2d, double MW, double MW_Frag, boolean useFragmentConstraint,
			Lookup.ExpRecord er, Vector<String> vecMOA, ReportOptions options) {
		try {

			// System.out.println(OutputFolder);

			this.setOptions();

			// First estimate scores for all MOAs:

			String[][] predArray = lm.calculateLDA_Scores2(vecMOA, evalInstances2d, htTrainingSetsMOA, htAllResultsMOA);

			// Second estimate LC50 from all models

			String[][] results = lm.calculateLC50_for_each_MOA2(vecMOA, evalInstances2d, htTrainingSetsLC50, htAllResultsLC50);

			// for (int i=0;i<evalInstances2d.numInstances();i++) {
			// for (int j=0;j<vecMOA.size();j++) {
			// System.out.println(vecMOA.get(j)+"\t"+predArray[i][j]+"\t"+results[i][j]);
			// }
			// }

			// store results for first chemical:
			String[] predArrayMOA = new String[vecMOA.size()];
			String[] predArrayLC50 = new String[vecMOA.size()];

			for (int i = 0; i < vecMOA.size(); i++) {
				predArrayMOA[i] = predArray[0][i];
				// System.out.println(vecMOA.get(i)+"\t"+predArrayMOA[i]);
				predArrayLC50[i] = results[0][i];
			}

			maxScore = -9999;
			bestMOA = "";
			String bestMOA_LC50_Error = "";
			// String bestMOA_LDA_Error="";

			// double predValue=-9999;

			Instance evalInstance2d = evalInstances2d.instance(0);

			// System.out.println(vecMOA.size());

			for (int j = 0; j < vecMOA.size(); j++) {

				java.util.LinkedList<String> l_MOA = ToxPredictor.Utilities.Utilities.Parse(predArrayMOA[j], "\t");
				java.util.LinkedList<String> l_LC50 = ToxPredictor.Utilities.Utilities.Parse(predArrayLC50[j], "\t");

				double score = Double.parseDouble(l_MOA.get(0));
				String LC50error = (String) (l_LC50.get(2));
				String MOAerror = (String) (l_MOA.get(2));

				// System.out.println(vecMOA.get(j)+"\t"+MOAerror);

				// System.out.println(vecMOA.get(j)+"\t"+l_MOA.get(0));
				if (score > maxScore && MOAerror.equals("OK")) {
					maxScore = score;
					bestMOA = vecMOA.get(j);
					bestMOA_LC50_Error = (String) (l_LC50.get(2));
					// bestMOA_LDA_Error=(String)(l_MOA.get(2));
					// System.out.println(bestMOALC50Error);
					// it's possible that second best MOA could have score > 0.5
					// if LC50 constraint is violated for best MOA
				}
			}

			// TODO- store error?

			TestChemical chemical = (TestChemical) lm.testData.instance(0);

			// if (bestMOA.equals("") || maxScore < lm.minPosScore ||
			// !bestMOA_LC50_Error.equals("OK") ||
			// !bestMOA_LDA_Error.equals("OK")) {
			if (bestMOA.equals("") || maxScore < lm.minPosScore || !bestMOA_LC50_Error.equals("OK")) {
				this.predToxVal = -9999;
			} else {
				String resultPredLC50 = predArrayLC50[vecMOA.indexOf(bestMOA)];
				java.util.LinkedList<String> l = ToxPredictor.Utilities.Utilities.Parse(resultPredLC50, "\t");
				this.predToxVal = Double.parseDouble(l.get(0));
				chemical.setPredictedUncertainty(Double.parseDouble(l.get(1)));
			}
			chemical.setPredictedValue(predToxVal);

			if (OutputFolder == null)
				return 0;

			double[] Mean = trainingDataSet2d.getMeans();
			double[] StdDev = trainingDataSet2d.getStdDevs();

			Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, testDataSet2d, true, true, true, Mean, StdDev);
			//

			Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, trainingDataSet2d, true, true, true, Mean, StdDev);

			PredictToxicityWebPageCreatorLDA p = new PredictToxicityWebPageCreatorLDA();

			p.WriteLDAResultsWebPages(method, chemical, OutputFolder, CAS, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, MW, htTestMatch, htTrainMatch, chemicalNameIndex, vecMOA,
					predArrayMOA, predArrayLC50, htAllResultsMOA, htAllResultsLC50, bestMOA, maxScore, options);

			return 0;

		} catch (Exception ex) {
			logger.catching(ex);
		}
		return -1;
	}
	
	/**
	 * Calculate toxicity using LDA method for TaskCalculations2
	 * 
	 * @param d
	 * @param CAS
	 * @param htAllResultsMOA
	 * @param htTrainingSetsMOA
	 * @param htAllResultsLC50
	 * @param htTrainingSetsLC50
	 * @param evalInstances2d
	 * @param vecMOA
	 * @return
	 */
	public int CalculateToxicity2(DataForPredictionRun d, String CAS, 
			Hashtable<String, AllResults> htAllResultsMOA,
			Hashtable<String, Instances> htTrainingSetsMOA, 
			Hashtable<String, AllResults> htAllResultsLC50,
			Hashtable<String, Instances> htTrainingSetsLC50, 
			Instances evalInstances2d, 			
			Vector<String> vecMOA) {
		
		try {

			this.setOptions();

			// First estimate scores for all MOAs:
			String[][] predArray = lm.calculateLDA_Scores2(vecMOA, evalInstances2d, htTrainingSetsMOA, htAllResultsMOA);

			// Second estimate LC50 from all models
			String[][] results = lm.calculateLC50_for_each_MOA2(vecMOA, evalInstances2d, htTrainingSetsLC50, htAllResultsLC50);

			// store results for first chemical:
			predArrayMOA = new String[vecMOA.size()];
			predArrayLC50 = new String[vecMOA.size()];

			for (int i = 0; i < vecMOA.size(); i++) {
				predArrayMOA[i] = predArray[0][i];
				// System.out.println(vecMOA.get(i)+"\t"+predArrayMOA[i]);
				predArrayLC50[i] = results[0][i];
			}

			maxScore = -9999;
			bestMOA = "";
			String bestMOA_LC50_Error = "";
			// String bestMOA_LDA_Error="";


			for (int j = 0; j < vecMOA.size(); j++) {

				java.util.LinkedList<String> l_MOA = ToxPredictor.Utilities.Utilities.Parse(predArrayMOA[j], "\t");
				java.util.LinkedList<String> l_LC50 = ToxPredictor.Utilities.Utilities.Parse(predArrayLC50[j], "\t");

				double score = Double.parseDouble(l_MOA.get(0));
				String LC50error = (String) (l_LC50.get(2));
				String MOAerror = (String) (l_MOA.get(2));

				if (score > maxScore && MOAerror.equals("OK")) {
					maxScore = score;
					bestMOA = vecMOA.get(j);
					bestMOA_LC50_Error = (String) (l_LC50.get(2));
				}
			}

			// TODO- store error?

			chemical = (TestChemical) lm.testData.instance(0);

			if (bestMOA.equals("") || maxScore < lm.minPosScore || !bestMOA_LC50_Error.equals("OK")) {
				this.predToxVal = -9999;
			} else {
				String resultPredLC50 = predArrayLC50[vecMOA.indexOf(bestMOA)];
				java.util.LinkedList<String> l = ToxPredictor.Utilities.Utilities.Parse(resultPredLC50, "\t");
				this.predToxVal = Double.parseDouble(l.get(0));
				this.predToxUnc=Double.parseDouble(l.get(1));
			}
			
			chemical.setPredictedValue(predToxVal);
			chemical.setPredictedUncertainty(predToxUnc);

			
			
			return 0;

		} catch (Exception ex) {
			logger.catching(ex);
		}
		return -1;
	}


	public static void main(String[] args) {
		PredictToxicityLDA p = new PredictToxicityLDA();

	}
}
