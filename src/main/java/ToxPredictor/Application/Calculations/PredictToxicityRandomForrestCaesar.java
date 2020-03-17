package ToxPredictor.Application.Calculations;

import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import weka.core.Attribute;
//import weka.core.FastVector;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils;

import wekalite.*;
import QSAR.validation2.TestChemical;
import ToxPredictor.Application.ReportOptions;
import ToxPredictor.misc.Lookup;

//import ToxPredictor.misc.ParseChemidplus;
import caesar.model.DevTox.CaesarModelDevTox;

public class PredictToxicityRandomForrestCaesar {

	private static final Logger logger = LogManager.getLogger(PredictToxicityRandomForrestCaesar.class);

	/**
	 * @param args
	 */
	private String relationName = "testchemical";

	int chemicalNameIndex = 0;

	double expToxVal = -9999;
	public double predToxVal = -9999;
	double predToxUnc = -9999;

	ToxPredictor.misc.Lookup lookup = new ToxPredictor.misc.Lookup();

	CaesarModelDevTox cmdt;

	public PredictToxicityRandomForrestCaesar() {
		try {
			cmdt = new CaesarModelDevTox();
		} catch (Exception ex) {
			logger.catching(ex);
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
	 * @param evalInstances2d
	 *            - Instances object with all 2d descriptors for the chemical
	 *            being evaluated
	 * @param trainingDataSet2d
	 *            - Instances object with all 2d descriptors for the training
	 *            set
	 * @param testDataSet2d
	 *            - Instances object with all 2d descriptors for the external
	 *            test set
	 * @param MW
	 *            - molecular weight of the chemical being evaluated
	 * @param task
	 *            - task being run (i.e. DescriptorCalculationTask7) * @param er
	 * @param htDescriptors
	 *            - hashtable of descriptors needed for random forest method
	 * @return
	 */
	public int CalculateToxicity(String CAS, String method, String endpoint, boolean isBinaryEndpoint, boolean isLogMolarEndpoint, String abbrev, String OutputFolder, String DescriptorSet,
			Instances evalInstances2d, Instances trainingDataSet2d, Instances testDataSet2d, double MW, Object task, Lookup.ExpRecord er, Hashtable htDescriptors, ReportOptions options) {
		try {

			PredictToxicityWebPageCreator p = new PredictToxicityWebPageCreator();

			double Tox = -1;

			cmdt.calculateDevTox(htDescriptors);

			if (!cmdt.Warning.equals("OK")) {// dont make a prediction if
												// descriptors are outside the
												// range of the training set
												// descriptors
				cmdt.Tox = -9999;
			}

			// System.out.println(cmdt.Tox);
			// System.out.println(cmdt.Warning);

			Instance chemical = evalInstances2d.firstInstance();

			TestChemical tc = new TestChemical(chemical);
			tc.setPredictedValue(cmdt.Tox);

			predToxVal = cmdt.Tox;

			if (OutputFolder == null)
				return 0;

			double[] Mean = trainingDataSet2d.getMeans();
			double[] StdDev = trainingDataSet2d.getStdDevs();

			Hashtable<Double, Instance> htTestMatch = TaskCalculations.FindClosestChemicals(chemical, testDataSet2d, true, true, true, Mean, StdDev);

			Hashtable<Double, Instance> htTrainMatch = TaskCalculations.FindClosestChemicals(chemical, trainingDataSet2d, true, true, true, Mean, StdDev);

			p.WriteRandomForrestCaesarResultsWebPages(method, tc, OutputFolder, CAS, endpoint, abbrev, isBinaryEndpoint, isLogMolarEndpoint, null, er, null, MW, cmdt.Warning, htTestMatch,
					htTrainMatch, chemicalNameIndex, options);

			return 0;

		} catch (Exception ex) {
			logger.catching(ex);
			return -1;
		}

	}

}
