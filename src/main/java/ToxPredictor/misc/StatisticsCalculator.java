package ToxPredictor.misc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSDF;
import ToxPredictor.Database.DSSToxRecord;


/**
* @author TMARTI02
*/
public class StatisticsCalculator {			
	int minPredCount=2;
	Double BINARY_CUTOFF = 0.5;
	
	public static final String  MAE = "MAE";
	public static final String  RMSE = "RMSE";
	public static final String  BALANCED_ACCURACY = "BA";
	public static final String  SENSITIVITY = "SN";
	public static final String  SPECIFICITY = "SP";
	public static final String  CONCORDANCE = "Concordance";
	public static final String  POS_CONCORDANCE = "PosConcordance";
	public static final String  NEG_CONCORDANCE = "NegConcordance";
	public static final String  PEARSON_RSQ = "PearsonRSQ";
	public static final String  TAG_TEST = "_Test";
	public static final String  TAG_TRAINING = "_Training";

	public static final String  SOURCE_CHEMINFORMATICS_MODULES = "Cheminformatics Modules";

	public static final String  R2 = "R2";
	public static final String  Q2 = "Q2";
	public static final String  R2_TEST = R2+TAG_TEST;
	public static final String  Q2_TEST = Q2+TAG_TEST;
	
	public static final String  Q2_F3_TEST ="Q2_F3"+TAG_TEST;
	public static final String  MAE_TEST = MAE+TAG_TEST;
	public static final String  RMSE_TEST = RMSE+TAG_TEST;

	public static final String  TAG_CV = "_CV";

	public static final String  MAE_CV_TRAINING=MAE+TAG_CV+TAG_TRAINING;
	public static final String  PEARSON_RSQ_CV_TRAINING=PEARSON_RSQ+TAG_CV+TAG_TRAINING;
	public static final String  RMSE_CV_TRAINING=RMSE+TAG_CV+TAG_TRAINING;
	
	public static final String  PEARSON_RSQ_TRAINING=PEARSON_RSQ+TAG_TRAINING;
	public static final String  PEARSON_RSQ_TEST=PEARSON_RSQ+TAG_TEST;

	public static final String  R2_TRAINING = R2+TAG_TRAINING;
	public static final String  MAE_TRAINING = MAE+TAG_TRAINING;
	public static final String  RMSE_TRAINING = RMSE+TAG_TRAINING;

	public static final String  COVERAGE = "Coverage";
	public static final String  COVERAGE_TRAINING = "Coverage"+TAG_TRAINING;
	public static final String  COVERAGE_TEST = "Coverage"+TAG_TEST;
	
	public static final String  BA_TRAINING=BALANCED_ACCURACY+TAG_TRAINING;
	public static final String  SN_TRAINING=SENSITIVITY+TAG_TRAINING;
	public static final String  SP_TRAINING=SPECIFICITY+TAG_TRAINING;

	public static final String  BA_CV_TRAINING=BALANCED_ACCURACY+TAG_CV+TAG_TRAINING;
	public static final String  SN_CV_TRAINING=SENSITIVITY+TAG_CV+TAG_TRAINING;
	public static final String  SP_CV_TRAINING=SPECIFICITY+TAG_CV+TAG_TRAINING;

	public static final String  BA_TEST=BALANCED_ACCURACY+TAG_TEST;
	public static final String  SN_TEST=SENSITIVITY+TAG_TEST;
	public static final String  SP_TEST=SPECIFICITY+TAG_TEST;

	

	private Double calcMeanExpTraining(List<ModelPrediction>trainMP) {
		
		double meanExpTraining = 0.0;
		int count = 0;
		
		for (ModelPrediction mp:trainMP) {
			if (mp.exp!=null) {
				meanExpTraining += mp.exp;
				count++;
			}
		}
		meanExpTraining /= count;
		return meanExpTraining;
	}
	
	private Double calcAvgSumSqError(List<ModelPrediction>testMP) {
		Double ASSE = 0.0;
		int count=0;
		for (ModelPrediction mp:testMP) {	
			if (mp.exp!=null && mp.pred!=null) {
				ASSE+=Math.pow((mp.exp-mp.pred),2);
				count++;
			}
		}
		ASSE /= count;
		return ASSE;
	}

	private double calcAvgYminusYbar(List<ModelPrediction> trainMP,double Ybar_training) {				
		double AvgYminusYbar=0;
		int count=0;
		for (ModelPrediction mp:trainMP) {
			if (mp.exp!=null) {
				AvgYminusYbar+=Math.pow((mp.exp-Ybar_training),2);
				count++;
			} 
		}
		AvgYminusYbar/=count;
		return AvgYminusYbar;				
	}
	
	/**
	 * 
	 * Calculates Q2_F3 see eqn 2 of Consonni et al, 2019 (https://onlinelibrary.wiley.com/doi/full/10.1002/minf.201800029)
	 * 
	 * @param trainMP
	 * @param testMP
	 * @return
	 */
	private double calculateQ2_F3(List<ModelPrediction> trainMP, List<ModelPrediction> testMP) {		
		
		double YbarTrain=calcMeanExpTraining(trainMP);
		double numerator= calcAvgSumSqError(testMP);
		double denominator = calcAvgYminusYbar(trainMP,YbarTrain);		
		
		double q2=1.0-numerator/denominator;
		
//		System.out.println(numerator+"\t"+denominator+"\t"+q2);
		
		return q2;
	}
	


	private Hashtable<Integer, List<ModelPrediction>> createModelPredictionHashtable(List<ModelPrediction> mpsAll) {
		Hashtable<Integer,List<ModelPrediction>>htMP=new Hashtable<>();
		
		for (ModelPrediction mp:mpsAll) {
			
			if(htMP.get(mp.split)==null) {
				List<ModelPrediction>mps=new ArrayList<>();
				htMP.put(mp.split, mps);				
				mps.add(mp);
			} else {
				List<ModelPrediction>mps=htMP.get(mp.split);
				mps.add(mp);
			}
		}
		return htMP;
	}

	public class AllStats {
		public Map<String, Double> modelTestStatisticValues = null;
		public Map<String, Double> modelTrainingStatisticValues = null;

	}

	
	
	public HashMap<String, Double> calculateStatistics(List<ModelPrediction> trainingSetPredictions, List<ModelPrediction> testSetPredictions,
			String  endpoint) {
		
		double meanExpTraining= calcMeanExpTraining(trainingSetPredictions);
		HashMap<String, Double>mapStats=new HashMap<>();
		
		if (TESTConstants.isBinary(endpoint)) {
			calculateBinaryStatistics(testSetPredictions, BINARY_CUTOFF,TAG_TEST,mapStats);
			calculateBinaryStatistics(trainingSetPredictions,BINARY_CUTOFF,TAG_TRAINING,mapStats);
		} else {
			
			calculateContinuousStatistics(testSetPredictions,meanExpTraining,TAG_TEST,mapStats);
//			double Q2_TEST=modelTestStatisticValues.get(DevQsarConstants.Q2_TEST);
			Double valQ2_F3_TEST=calculateQ2_F3(trainingSetPredictions, testSetPredictions);
			mapStats.put(Q2_F3_TEST,valQ2_F3_TEST);
//			System.out.println("Q2_TEST="+Q2_TEST);
//			System.out.println("Q2_Consonni="+Q2_Consonni);
			calculateContinuousStatistics(trainingSetPredictions,meanExpTraining,TAG_TRAINING, mapStats);
		}
		
		return mapStats;
	}
	
	/**
	 * Calculates a basic set of binary model statistics
	 * @param modelPredictions	a list of tuples of experimental and predicted values
	 * @param cutoff			the cutoff to consider a non-binary value as positive or negative
	 * @return					a map of statistic names to calculated values
	 */
	private HashMap<String, Double> calculateBinaryStatistics(List<ModelPrediction> modelPredictions, double cutoff, String  tag,HashMap<String, Double>modelStatisticValues) {
		int countTotal = 0;
		int countPredicted = 0;
		int countPositive = 0;
		int countNegative = 0;
		double countTrue = 0.0;
		double countTruePositive = 0.0;
		double countTrueNegative = 0.0;
		for (ModelPrediction mp:modelPredictions) {
			if (mp.exp!=null) {
				countTotal++;
			} else {
				continue;
			}
			
			if (mp.pred!=null) {
				countPredicted++;
				int predBinary = mp.pred.doubleValue() >= cutoff ? 1 : 0;
				if (mp.exp==1) {
					countPositive++;
					if (predBinary==1) {
						countTrue++;
						countTruePositive++;
					}
				} else if (mp.exp==0) {
					countNegative++;
					if (predBinary==0) {
						countTrue++;
						countTrueNegative++;
					}
				}
			}
		}
		
		Double coverage = (double) countPredicted / (double) countTotal;
		Double concordance = countTrue /= (double) countPredicted;
		Double sensitivity = countTruePositive /= (double) countPositive;
		Double specificity  = countTrueNegative /= (double) countNegative;
		Double balancedAccuracy = (sensitivity + specificity) / 2.0;
		
		modelStatisticValues.put(COVERAGE + tag, coverage);
		modelStatisticValues.put(CONCORDANCE + tag, concordance);
		modelStatisticValues.put(SENSITIVITY + tag, sensitivity);
		modelStatisticValues.put(SPECIFICITY + tag, specificity);
		modelStatisticValues.put(BALANCED_ACCURACY + tag, balancedAccuracy);
		
		return modelStatisticValues;
	}
	
	
	 
	
	/**
	 * Calculates a basic set of continuous model statistics
	 * 
	 * @param modelPredictions	a list of tuples of experimental and predicted values
	 * @param meanExpTraining	the average experimental value in the training set
	 * @return					a map of statistic names to calculated values
	 */
	private void calculateContinuousStatistics(List<ModelPrediction> modelPredictions, Double meanExpTraining, 
			String  tag,HashMap<String, Double>modelStatisticValues) {
		// Loop once to get counts and means
		int countTotal = 0;
		int countPredicted = 0;
		Double meanExp = 0.0;
		Double meanPred = 0.0;
		for (ModelPrediction mp:modelPredictions) {
			
//			System.out.println(mp.id+"\t"+mp.exp+"\t"+mp.pred);
			
			if (mp.exp!=null) {
				countTotal++;
			} else {
				continue;
			}
			
			if (mp.pred!=null) {
				countPredicted++;
				meanExp += mp.exp;
				meanPred += mp.pred;
			}
		}
		
//		System.out.println("");
				
		meanExp /= (double) countPredicted;
		meanPred /= (double) countPredicted;
		
		// Loop again to calculate stats
		Double mae = 0.0;
		Double termXY = 0.0;
		Double termXX = 0.0;
		Double termYY = 0.0;
		Double ss = 0.0;
		Double ssTotal = 0.0;
		for (ModelPrediction mp:modelPredictions) {
			if (mp.exp==null || mp.pred==null) { 
				continue;
			}
			
			// Update MAE
			mae += Math.abs(mp.exp - mp.pred);
			
//			if (tag.equals(DevQsarConstants.TAG_TEST)) {
//				System.out.println(mp.id+"\t"+mp.exp+ "\t"+ mp.pred+"\t"+Math.abs(mp.exp - mp.pred));
//			}
			
			// Update terms for Pearson RSQ
			termXY += (mp.exp - meanExp) * (mp.pred - meanPred);
			termXX += (mp.exp - meanExp) * (mp.exp - meanExp);
			termYY += (mp.pred - meanPred) * (mp.pred - meanPred);
			
			// Update sums for coefficient of determination
			ss += Math.pow(mp.exp - mp.pred, 2.0);
			ssTotal += Math.pow(mp.exp - meanExpTraining, 2.0);
		}
		
		Double coverage = (double) countPredicted / (double) countTotal;
		mae /= (double) countPredicted;
		
//		System.out.println(countPredicted);
		
		Double pearsonRsq = termXY * termXY / (termXX * termYY);
		Double coeffDet = 1 - ss / ssTotal;
		Double rmse = Math.sqrt(ss / (double) countPredicted);
		
		modelStatisticValues.put(COVERAGE + tag, coverage);
		modelStatisticValues.put(MAE + tag, mae);
		modelStatisticValues.put(PEARSON_RSQ + tag, pearsonRsq);
		modelStatisticValues.put(RMSE + tag, rmse);
		
		if (tag.equals(TAG_TEST)) {
			modelStatisticValues.put(Q2_TEST, coeffDet);
		} else if (tag.equals(TAG_TRAINING)) {
			modelStatisticValues.put(R2_TRAINING, coeffDet);
		}
			
	}
	
	@Deprecated
	private List<ModelPrediction>getModelPredictionsConsensus(String  endpointAbbrev, String  set, int minPredCount) {
		
		List<ModelPrediction>mps=new ArrayList<>();
		
		String filepath=endpointAbbrev+"/"+endpointAbbrev+" "+set+" set predictions.txt";

		try {
			
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filepath);
			
			BufferedReader br=new BufferedReader(new InputStreamReader(ins));
			String header=br.readLine();
			List<String>headers=Arrays.asList(header.split("\t"));
			
			String endpoint=TESTConstants.getFullEndpoint(endpointAbbrev);
			List<String>consensusMethods=new ArrayList<>();
			consensusMethods.add("Hierarchical clustering");
			if(TESTConstants.haveSingleModelMethod(endpoint)) consensusMethods.add("Single model");
			if(TESTConstants.haveGroupContributionMethod(endpoint)) consensusMethods.add("Group contribution");
			consensusMethods.add("Nearest neighbor");
			
//			System.out.println(header);
			
			while (true) {
				
				String  Line=br.readLine();
				if(Line==null || Line.isBlank()) break;
				String  [] strVals=Line.split("\t");
				List<Double>consensusVals=new ArrayList<>();
				
				int splitNum=-1;
				if(set.equals("test")) splitNum=1;
				if(set.equals("training")) splitNum=0;
				
				String cas=strVals[headers.indexOf("CAS")];
				double dexpval=Double.parseDouble(strVals[headers.indexOf("expToxicValue")]);
				
				for (String method:consensusMethods) {
					double dpredval=Double.parseDouble(strVals[headers.indexOf(method)]);
					
					if(Math.abs(-9999-dpredval)<0.0001) continue;
//					if(dval==-9999) continue;
					
					consensusVals.add(dpredval);
				}
				
				Double dpredval=null;
				
				if(consensusVals.size()>=minPredCount) {//need 2 or more or unreliable (AD)
					dpredval=0.0;
					for(Double val:consensusVals) dpredval+=val;
					dpredval/=consensusVals.size();
				}
				ModelPrediction mp=new ModelPrediction(cas,dexpval,dpredval,splitNum);
				mps.add(mp);
//				System.out.println(strVals[0]+"\t"+consensusVals);
			}
			
//			System.out.println(Utilities.gson.toJson(mps));
			
		} catch (Exception ex) {
			System.out.println("Error getting predictions from "+filepath);
			ex.printStackTrace();
		}
		
		return mps;
		
	}
	

	private List<ModelPrediction>getModelPredictions(String  endpointAbbrev, String methodAbbrev, String  set, int minPredCount,Hashtable<String,Hashtable<String,String>>htPredByCAS) {
		
		List<ModelPrediction>mps=new ArrayList<>();
		
//		String filepath=endpointAbbrev+"/"+endpointAbbrev+" "+set+" set predictions.txt";

		try {
			
			
			String	sdfFilePath = WebTEST4.dataFolder+"/"+endpointAbbrev+"/" + endpointAbbrev + "_"+set+".sdf";

			
			//Following assumes that the structures in the sdf in the jar file are correct, they need updating
			AtomContainerSet acs=RunFromSDF.readSDF(sdfFilePath, -1,true);
			
			
			for (IAtomContainer ac:acs.atomContainers()) {
				
				String cas=ac.getProperty(DSSToxRecord.strCAS);
												
				int splitNum=-1;
				if(set.equals("test")) splitNum=1;
				if(set.equals("training")) splitNum=0;
								
				double dexpval=Double.parseDouble(ac.getProperty("Tox"));
				
				Hashtable<String,String>htPreds=htPredByCAS.get(cas);
								
				Double dpredval=null;
				
				if (!htPreds.containsKey(methodAbbrev)) {
					System.out.println(cas+"\t"+endpointAbbrev+"\t"+methodAbbrev+"\tmissing prediction");
					continue;
				}
				
				
				String strPred=htPreds.get(methodAbbrev);
				
				if(!strPred.equals("N/A")) {
					dpredval=Double.parseDouble(strPred);
				}
				
				ModelPrediction mp=new ModelPrediction(cas,dexpval,dpredval,splitNum);
				mps.add(mp);
//				System.out.println(strVals[0]+"\t"+consensusVals);
			}
			
//			System.out.println(Utilities.gson.toJson(mps));
			
		} catch (Exception ex) {
			System.out.println("Error getting predictions for "+endpointAbbrev);
			ex.printStackTrace();
		}
		
		return mps;
		
	}
	
	@Deprecated
	private List<ModelPrediction>getModelPredictions(String  endpointAbbrev, String method, String  set, int minPredCount) {
		
		List<ModelPrediction>mps=new ArrayList<>();
		
		String filepath=endpointAbbrev+"/"+endpointAbbrev+" "+set+" set predictions.txt";

		try {
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filepath);
			
			BufferedReader br=new BufferedReader(new InputStreamReader(ins));
			String header=br.readLine();
//			System.out.println(header);
			List<String>headers=Arrays.asList(header.split("\t"));
			
			while (true) {
				
				String  Line=br.readLine();
				if(Line==null || Line.isBlank()) break;
				String  [] strVals=Line.split("\t");
				
				int splitNum=-1;
				if(set.equals("test")) splitNum=1;
				if(set.equals("training")) splitNum=0;
				
				String cas=strVals[headers.indexOf("CAS")];
				double dexpval=Double.parseDouble(strVals[headers.indexOf("expToxicValue")]);

				double dpredval=Double.parseDouble(strVals[headers.indexOf(method)]);
				if(Math.abs(-9999-dpredval)<0.0001) continue;
				
//				System.out.println(dexpval+"\t"+dpredval);
				
				
				ModelPrediction mp=new ModelPrediction(cas,dexpval,dpredval,splitNum);
				mps.add(mp);
//				System.out.println(strVals[0]+"\t"+consensusVals);
			}
			
//			System.out.println(Utilities.gson.toJson(mps));
			
		} catch (Exception ex) {
			System.out.println("Error getting predictions from "+filepath);
			ex.printStackTrace();
		}
		
		return mps;
		
	}

	public static class ModelPrediction {
		public String  id;
		public Double exp;
		public Double pred;
		public Double weight;
		public Integer split;

		public ModelPrediction(String  id, Double exp, Double pred, Integer split) {
			this.id = id;
			this.exp=exp;
			this.pred = pred;
			this.split=split;
		}
	}
	
	public HashMap<String, Double> getStatistics(String endpoint,String method,Hashtable<String,Hashtable<String,String>>htPredByCAS) {

		String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);
		String methodAbbrev=TESTConstants.getAbbrevMethod(method);
		
		if(endpointAbbrev.equals("?")) {
			System.out.println("getStatistics(), Failed to get abbrev:\t"+endpoint);
			return null;
		}
		
//		if(method.equals("Consensus")) {
//			List<ModelPrediction>mpsTest=getModelPredictionsConsensus(endpointAbbrev, "test",minPredCount);
//			List<ModelPrediction>mpsTraining=getModelPredictionsConsensus(endpointAbbrev, "training",minPredCount);
//			return calculateStatistics(mpsTraining, mpsTest, endpointAbbrev);
//		} else {
//			List<ModelPrediction>mpsTest=getModelPredictions(endpointAbbrev,method, "test",minPredCount);
//			List<ModelPrediction>mpsTraining=getModelPredictions(endpointAbbrev, method, "training",minPredCount);
//			return calculateStatistics(mpsTraining, mpsTest, endpointAbbrev);
//		}
		

		List<ModelPrediction>mpsTest=getModelPredictions(endpointAbbrev,methodAbbrev, "prediction",minPredCount,htPredByCAS);
		List<ModelPrediction>mpsTraining=getModelPredictions(endpointAbbrev,methodAbbrev, "training",minPredCount,htPredByCAS);
		return calculateStatistics(mpsTraining, mpsTest, endpointAbbrev);

		
//		System.out.println(mpsTest.size()+"\t"+mpsTraining.size());
		
		
	}
	
	
//	public static void main(String[] args) {
//		StatisticsCalculator sc=new StatisticsCalculator();
//		HashMap<String, Double>mapStats=sc.getStatistics(TESTConstants.ChoiceFHM_LC50,"Consensus");
//		
//		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//		System.out.println(gson.toJson(mapStats));
//		
//	}


}
