package ToxPredictor.Application.Calculations;

import java.io.File;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import wekalite.*;
import QSAR.validation2.*;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.misc.Lookup;
//import ToxPredictor.misc.ParseChemidplus;

public class PredictToxicityNearestNeighbor {

	private static final Logger logger = LogManager.getLogger(PredictToxicityNearestNeighbor.class);

	Double expToxVal = null;
	public Double predToxVal = null;
	public Double predToxUnc = null;
	public String msg;

	NearestNeighborMethod nn;

	InstanceUtilities iu = new InstanceUtilities();
	ToxPredictor.misc.Lookup lookup = new ToxPredictor.misc.Lookup();

	public PredictToxicityNearestNeighbor() {
		try {
			nn = new NearestNeighborMethod();
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	/**
	 * @param CAS                - CAS number of chemical being evaluated
	 * @param method             - QSAR method being used
	 * @param endpoint           - tox endpoint / property being calculated
	 * @param isBinaryEndpoint   - if endpoint is binary it is positive if value =1
	 *                           or negative if value =0
	 * @param isLogMolarEndpoint - if isLogMolarEndpoint, value represents the log
	 *                           of the molar value
	 * @param abbrev             - abbreviation for the endpoint
	 * @param OutputFolder       - string for the output folder for storing files
	 * @param DescriptorSet      - descriptor set used in the calculations
	 * @param evalInstances2d    - Instances object with all 2d descriptors for the
	 *                           chemical being evaluated
	 * @param trainingDataSet2d  - Instances object with all 2d descriptors for the
	 *                           training set
	 * @param testDataSet2d      - Instances object with all 2d descriptors for the
	 *                           external test set
	 * @param MW
	 * @param er
	 * @return
	 */
	public int CalculateToxicity(String CAS, String method, String endpoint, boolean isBinaryEndpoint,
			boolean isLogMolarEndpoint, String abbrev, String OutputFolder, String DescriptorSet,
			Instances evalInstances2d, Instances trainingDataSet2d, Instances testDataSet2d, double MW,
			Lookup.ExpRecord er, ReportOptions options) {
		try {

			TestData testData = new TestData(evalInstances2d);
			TestChemical chemical = (TestChemical) testData.firstInstance();

			this.SetNNOptions(DescriptorSet);

			nn.initialize(trainingDataSet2d);

			String msg = nn.predictToxicity(chemical, "");

			predToxVal = nn.predToxicValue;

			// predToxUnc=nn.predToxicUnc;
			predToxUnc = null;
			// TODO: add routine to calculate uncertainty

			if (OutputFolder == null)
				return 0;

			this.CreateStructureImages(endpoint,OutputFolder);

			// Vector<String>testMatchTable=DescriptorCalculationTask7.FindClosestTestSetChemicals(999,
			// chemical,testDataSet2d, 0.5,
			// true,false,true,OutputFolder,er.expToxValue,predToxVal,chemicalNameIndex,endpoint,method,abbrev,isLogMolarEndpoint);

			double[] Mean = trainingDataSet2d.getMeans();
			double[] StdDev = trainingDataSet2d.getStdDevs();

			Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(chemical, testDataSet2d,
					true, true, true, Mean, StdDev);

			Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(chemical,
					trainingDataSet2d, true, true, true, Mean, StdDev);

			PredictToxicityWebPageCreator p = new PredictToxicityWebPageCreator();

			p.WriteResultsWebPageNN(chemical, OutputFolder, CAS, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint,
					method, er, predToxVal, MW, msg, nn.cc, nn.SimCoeffCluster, htTestMatch, htTrainMatch, options);

			return 0;

		} catch (Exception ex) {
			logger.catching(ex);
		}
		return -1;

	}

	/**
	 * Simpler toxicity estimation with no results file creation
	 * 
	 * @param DescriptorSet
	 * @param evalInstances2d
	 * @param trainingDataSet2d
	 * @return
	 */
	public int CalculateToxicity2(String descriptorSet,Instances instancesTrain,Instances instancesEval) {
		
		try {
			TestData testData = new TestData(instancesEval);
			TestChemical chemical = (TestChemical) testData.firstInstance();
			this.SetNNOptions(descriptorSet);
			nn.initialize(instancesTrain);
			
			msg = nn.predictToxicity(chemical, "");
			
			predToxVal = nn.predToxicValue;
			predToxUnc = null;
			// TODO: add routine to calculate uncertainty
			return 0;

		} catch (Exception ex) {
			logger.catching(ex);
		}
		return -1;

	}

	private void SetNNOptions(String DescriptorSet) {

		nn.DescriptorSet = DescriptorSet;

		nn.minimumClusterSize = 3;
		nn.maximumClusterSize = 1001;
		nn.absoluteMinimumClusterSize = 3;

		nn.SimilarityMethod = "Cosine";
		nn.Standardize = false;
		nn.NormalizeDuringSCCalc = !nn.Standardize;
		// nn.UsePCATransform = false;
		// nn.pcaTolerance = 0.99;
		nn.UseWeights = false;
		nn.fragweight = 1.0;
		nn.HydrocarbonWeight = nn.fragweight;
		NearestNeighborMethod.MustExceedSCmin = true;

		if (DescriptorSet.equals("2d")) {
			nn.SCmin = 0.5;
		} else if (DescriptorSet.equals("FDA_Subset")) {
			nn.SCmin = 0.65;
		}

		// nn.MustHaveTestChemicalFragments = false;

		NearestNeighborMethod.ExcludeTestChemicalCASFromTrainingSet = true; // if the CAS matches
															// exclude chemical
															// from training
															// cluster
		NearestNeighborMethod.ExcludeTestChemical2dIsomerFromTrainingSet = false; // need to be
																// true because
																// TEST user
																// might run a
																// smiles string
																// w/o CAS

		// nn.RemoveAllDescriptorsNotInTestChemical=false;
		// nn.RemoveEStateDescriptorsNotInTestChemical=false;
		nn.PredictionMethod = "Average";
		nn.OmitCAS = "";
		// nn.MustMatchBondTypes=false;

	}

	private void CreateStructureImages(String endpoint,String OutputFolder) {

		// ParseChemidplus p=new ParseChemidplus();
//		ToxPredictor.Utilities.GetStructureImagesFromJar g = new ToxPredictor.Utilities.GetStructureImagesFromJar();

		if (nn.cc == null)
			return;

		File of1 = new File(OutputFolder);

		String folder = of1.getParentFile().getParent();
		// String folder=of1.getParentFile().getAbsolutePath();

		String strImageFolder=folder + "/images";
		File ImageFolder = new File(strImageFolder);
		if (!ImageFolder.exists())
			ImageFolder.mkdir();

		// System.out.println(ImageFolder.getAbsolutePath());

		for (int i = 0; i < nn.cc.numInstances(); i++) {
			String CASi = nn.cc.instance(i).getName();
//			TaskCalculations.CreateStructureImage(CASi, ImageFolder.getAbsolutePath());
			
			CreateImageFromTrainingPredictionSDFs c=new CreateImageFromTrainingPredictionSDFs();
			c.CreateStructureImage(CASi, strImageFolder,TESTConstants.getAbbrevEndpoint(endpoint));

			
		}
	}

}
