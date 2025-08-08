package ToxPredictor.Application.Calculations;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wekalite.*;

import QSAR.validation2.*;

import QSAR.qsarOptimal.OptimalResults;
import QSAR.qsarOptimal.AllResults;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.misc.Lookup;

public class PredictToxicityHierarchical {

	private static final Logger logger = LogManager.getLogger(PredictToxicityHierarchical.class);

	/**
	 * @param args
	 */
	Validation2 v2;

	// double expToxVal=-9999;
	public double predToxVal = -9999;
	public double predToxUnc = -9999;
	public String msg;
	
	public Vector <OptimalResults> resultsVector=null;
	public Vector <OptimalResults> invalidResultsVector=null;
	
	public Vector<Double>predictions;
	public Vector<Double>uncertainties;
	
	public Vector<Double>predictionsOutsideAD;
	public Vector<Double>uncertaintiesOutsideAD;
	public Vector<String>violationsAD;
	
	public TestChemical chemical;


	public PredictToxicityHierarchical() {
		try {
			v2 = new Validation2();

		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	private void setOptions(String method, boolean useFragmentConstraint, boolean isBinaryEndpoint) {
		v2.MinNumChemicals = -1;
		v2.PredictionsToUse = "WeightedAverage";

		if (method.equals("Hierarchical clustering")) {
			v2.QSARMethod = "Hierarchical";
			// v2.QSARMethod="Rmax";
			v2.UseRmaxCriterion = true;
		} else if (method.equals("Single model")) {
			v2.QSARMethod = "One Step";
			v2.UseRmaxCriterion = true;
			v2.NumClusters = 1;
		} else if (method.equals("Group contribution")) {
			v2.QSARMethod = "Frag";
			v2.UseRmaxCriterion = false;// rmax criterion wasnt used for GCM in
										// paper
		}

		if (isBinaryEndpoint) {
			v2.useFragments = false;
			v2.useEqualWeighting = true;
		} else {
			v2.useFragments = useFragmentConstraint;
			v2.useEqualWeighting = false;
		}

	}

	/**
	 * 
	 * @param CAS
	 *            - CAS number of chemical being evaluated
	 * @param method
	 *            - QSAR method being used
	 * @param endpoint
	 *            - tox endpoint / property being calculated
	 * @param isBinaryEndpoint
	 *            - if endpoint is binary it is positive if value =1 or negative
	 *            if value =0
	 * @param isLogMolarEndpoint
	 *            - if isLogMolarEndpoint, value represents the log of the molar
	 *            value
	 * @param abbrev
	 *            - abbreviation for the endpoint
	 * @param OutputFolder
	 *            - string for the output folder for storing files
	 * @param DescriptorSet
	 *            - descriptor set used in the calculations
	 * @param allResults
	 *            - object which stores the qsar models
	 * @param evalInstances
	 *            - Instances object for the chemical being evaluated - may
	 *            include all 2d descriptors or just fragment descriptors-
	 *            depending on the method
	 * @param evalInstance2d
	 *            - Instance object with all 2d descriptors for the chemical
	 *            being evaluated
	 * @param trainingDataSet
	 *            - Instances object for the training set- may include all 2d
	 *            descriptors or just fragment descriptors- depending on the
	 *            method
	 * @param trainingDataSet2d
	 *            - Instances object with all 2d descriptors for the training
	 *            set
	 * @param testDataSet2d
	 *            - Instances object with all 2d descriptors for the external
	 *            test set
	 * @param MW
	 *            - molecular weight of the chemical being evaluated
	 * @param useFragmentConstraint
	 *            - if useFragmentConstraint, the fragment constraint is
	 *            included in the applicability domain calculation
	 * @param er
	 *            - ExpRecord used to store information about the experimental
	 *            data point for the chemical being evaluated
	 * @return
	 */
	public int CalculateToxicity(String CAS, String method, String endpoint, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String abbrev, String OutputFolder, String DescriptorSet,
			AllResults allResults, Instances evalInstances, Instance evalInstance2d, Instances trainingDataSet, Instances trainingDataSet2d, Instances testDataSet2d, double MW, double MW_Frag,
			boolean useFragmentConstraint, Lookup.ExpRecord er, long minFileAgeHours, ReportOptions options) {
		try {

			// System.out.println(OutputFolder);

			this.setOptions(method, useFragmentConstraint, isBinaryEndpoint);

			v2.initialize(evalInstances, allResults, trainingDataSet);

//			double time3 = System.currentTimeMillis() / 1000.0;

			TestChemical chemical = v2.runTestChemical();

			// System.out.println(MW+"\t"+MW_Frag);

			if (useFragmentConstraint) {
				double diff = Math.abs(MW - MW_Frag);
				if (diff > 0.1) {
					// System.out.println(MW+"\t"+MW_frag);
					v2.message = PredictToxicityWebPageCreator.messageMissingFragments;
					chemical.setPredictedValue(-9999);
					chemical.setPredictedUncertainty(-9999);
				}
			}

			this.predToxVal = chemical.getPredictedValue();
			this.predToxUnc = chemical.getPredictedUncertainty();

			Vector<OptimalResults> resultsVector = new Vector<>();
			Vector<Integer> cu = chemical.getClustersUsed();
			for (int i = 0; i < cu.size(); i++) {
				int num = (Integer) cu.get(i);
				resultsVector.add(v2.GetResults(num));
			}

			Vector<OptimalResults> invalidResultsVector = new Vector<>(); // vector of
														// OptimalResults for
														// clusters which cant
														// be used to make a
														// prediction
			Vector<Integer> cun = chemical.getInvalidClusters(); // clusters which
														// couldnt be used to
														// make a prediction
			for (int i = 0; i < cun.size(); i++) {
				int num = (Integer) cun.get(i);
				OptimalResults or = v2.GetResults(num);

				// System.out.println(i+"\t"+or.clusterNumber+"\t"+or.numChemicals);
				invalidResultsVector.add(or);
			}

//			double time4 = System.currentTimeMillis() / 1000.0;

			// *******************************************************************
			// Write web pages:

			if (OutputFolder == null)
				return 0;

			double[] Mean = trainingDataSet2d.getMeans();
			double[] StdDev = trainingDataSet2d.getStdDevs();

			Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, testDataSet2d, true, true, true, Mean, StdDev);

			Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(evalInstance2d, trainingDataSet2d, true, true, true, Mean, StdDev);

			// A prediction cannot be made
			PredictToxicityWebPageCreator p = new PredictToxicityWebPageCreator();
			p.WriteHierarchicalResultsWebPages(method, chemical, OutputFolder, CAS, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, er, resultsVector, invalidResultsVector, MW, v2.message,
					htTestMatch, htTrainMatch, minFileAgeHours, options);

			// method,chemical, OutputFolder, CAS,
			// endpoint,abbrev,isBinaryEndpoint,
			// isLogMolarEndpoint,er,resultsVector,invalidResultsVector,MW,v2.message,htTestMatch,htTrainMatch);

			return 0;

			// System.out.println("time to predict using paul's
			// method="+(time4-time3)+ " secs");

		} catch (Exception ex) {
			logger.catching(ex);
		}
		return -1;

	}
	
	public int CalculateToxicity2(String method,boolean useFragmentConstraint,boolean isBinaryEndpoint,
			double MW,double MW_Frag,
			Instances instancesEval,Instances instancesTrain,AllResults allResults) {
		
		try {

			// System.out.println(OutputFolder);

			this.setOptions(method, useFragmentConstraint, isBinaryEndpoint);
			v2.initialize(instancesEval, allResults, instancesTrain);

//			double time3 = System.currentTimeMillis() / 1000.0;

			chemical = v2.runTestChemical();

			// System.out.println(MW+"\t"+MW_Frag);

			if (useFragmentConstraint) {
				double diff = Math.abs(MW - MW_Frag);
				if (diff > 0.1) {
					// System.out.println(MW+"\t"+MW_frag);
					v2.message = PredictToxicityWebPageCreator.messageMissingFragments;
					chemical.setPredictedValue(-9999);
					chemical.setPredictedUncertainty(-9999);
				}
			}

			this.predToxVal = chemical.getPredictedValue();
			this.predToxUnc = chemical.getPredictedUncertainty();
			this.msg=v2.message;

			Vector<Integer>cu = chemical.getClustersUsed();
			Vector<Integer>cun = chemical.getInvalidClusters(); 

			// vector of OptimalResults for clusters used to make a prediction:
			resultsVector = new Vector<>();
			// vector of OptimalResults for clusters which cant be used to make a prediction:
			invalidResultsVector = new Vector<>(); 

			for (int i = 0; i < cu.size(); i++) {
				int num = (Integer) cu.get(i);
				resultsVector.add(v2.GetResults(num));
			}
			
			
			predictionsOutsideAD=new Vector<>();
			uncertaintiesOutsideAD=new Vector<>();
			violationsAD=new Vector<>();
			
			
			for (int i = 0; i < cun.size(); i++) {
				int num = (Integer) cun.get(i);
				OptimalResults or = v2.GetResults(num);				
				
				if (!or.isValid()) continue;				
				
				invalidResultsVector.add(or);				
				this.predictionsOutsideAD.add(chemical.calculateToxicValue(or));
				this.uncertaintiesOutsideAD.add(chemical.calculateUncertainty(or));
				this.violationsAD.add(chemical.getInvalidErrorMessages().get(i));
				
			}

			this.predictions=chemical.getPredictions();
			this.uncertainties=chemical.getUncertainties();
			
//			double time4 = System.currentTimeMillis() / 1000.0;
			
			return 0;


		} catch (Exception ex) {
			logger.catching(ex);
		}
		return -1;

	}
}
