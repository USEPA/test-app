package QSAR.validation2;

//import java.beans.XMLDecoder;
//import java.beans.XMLEncoder;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import QSAR.qsarOptimal.AllResults;
import QSAR.qsarOptimal.OptimalResults;

import QSAR.validation2.Statistics;

import wekalite.*;

import QSAR.validation2.TestData;
import QSAR.validation2.AllClusterInfo;

public class Validation2 {
	
	public int XMLFormat=1;
	
	boolean WriteWebPages=false;
	
    public AllResults allResults;
    private AllClusterInfo allClusterInfo;
    public Instances trainingSet;
    public Instances testSet;
    public TestData testData;
    
    private double alpha = 0.10; // probability that predicted toxic endpoints are outside certainty intervals

    private Statistics statistics = new Statistics();
    
    int hierarchicalMethod=2;
    public String QSARMethod="Hierarchical";
    
    
  // *******************************************************************
    public String DescriptorSet="2d";
    public String endpoint="LC50";    
    public String info;
    
//    public String QSARMethod="Rmax";
//    public String QSARMethod="One Step";
//    String Method="Hierarchical";
//    String Method="Rmax-closest";

    public int NumClusters=1; // number of clusters in the step to use for One Step method
    
    
    public double minPosScore=0.5;// for maximum coverage- need to have program flag values less than 0.5
//  public boolean useRMax=true;
    public boolean useFragments=true;
    public boolean useModelEllipsoid=true;
    public boolean UseRmaxCriterion=true; // if true Rmax criterion is applied during the hierarchical method
	public double RmaxFactor=1.0; // decrease RmaxFactor to make the criterion more strict

    public boolean useEqualWeighting=false;
    
//    public String PredictionsToUse="ClosestCluster";
//    public String PredictionsToUse="SmallestUncertainty";
    public String PredictionsToUse="WeightedAverage";
    
	public int MinNumChemicals=-1;
	
	
	public boolean UseCancerConstraints=false;
	public double MinCancerConstraint=0.4;
	public double MaxCancerConstraint=0.6;
	
	public String message="";
	
    void runRndSets() {
		try {

//			String endpoint="BP";
//			String endpoint="Density";
//			String endpoint="FP";
//			String endpoint="IGC50";
			String endpoint="LC50";
//			String endpoint="LC50DM";
//			String endpoint="LD50";
//			String endpoint="LogKow";
//			String endpoint="MP";
//			String endpoint="Mutagenicity";
//			String endpoint="ST";
//			String endpoint="TC";
//			String endpoint="Viscosity";
//			String endpoint="VP";
//			String endpoint="WS_Final3";
			
			System.out.println(endpoint);
			
			
//****************************************************************
// General TEST five fold calcs
			String mainDataFolder = "ToxPredictor/DescriptorTextTables";
			String mainResultsFolder = "QSAR/resultsHierarchical";

//			String trial ="1";
//			QSARMethod = "Hierarchical";
//			
			String trial ="2";
			QSARMethod = "One Step";
			
//			String trial ="3";
//			QSARMethod = "Rmax";


			useFragments = true;
			useModelEllipsoid=true;
			UseRmaxCriterion = true;
			
			String desc="_Final"+QSAR.validation2.GetStats.FindFinalNumber(endpoint,mainDataFolder);

			this.NumClusters=1;
			MinNumChemicals = -1;
			
			PredictionsToUse = "WeightedAverage";			
			useEqualWeighting = false;
			
			DescriptorSet = "2d";
//			RmaxFactor=0.5;//** restrict coverage**
			XMLFormat=2;
			//****************************************************************			

			
			mainDataFolder += "/"+endpoint + desc + " Data Files/" + DescriptorSet;

			
			File F1 = new File(mainResultsFolder);
			if (!F1.exists())
				F1.mkdir();

			String f2 = mainResultsFolder + "/" + endpoint + desc;
			File F2 = new File(f2);
			if (!F2.exists())
				F2.mkdir();

			String f3 = f2 + "/trial" + trial;
			File F3 = new File(f3);
			if (!F3.exists())
				F3.mkdir();

			
			int numrnd=5;//number of rnd sets
			
			for (int i = 1; i <= numrnd; i++) {
//			for (int i = 1; i <= 1; i++) {
				String trainingFilePath = mainDataFolder + "/" + endpoint
						+ "_training_set-" + DescriptorSet +"-rnd"+i+".csv";

				String testFilePath = mainDataFolder + "/" + endpoint
						+ "_prediction_set-" + DescriptorSet  +"-rnd"+i+".csv";

				String xmlFilePath = mainDataFolder + "/" + endpoint + "_training_set-"
						+ DescriptorSet +"-rnd"+i+ ".xml";

				String weightFilePath = mainDataFolder + "/2d.txt";
				boolean useWeightingFile = false;

				String outputFolderPath = f3+"/run"+i;
				
				File outputFolder=new File(outputFolderPath);
				if(!outputFolder.exists()) outputFolder.mkdir();
				
				String outputTextFileName = "run" + i + "-fitvalues.txt";

				this.PerformExternalCalcs(QSARMethod,trainingFilePath, testFilePath, xmlFilePath,
						outputFolderPath, outputTextFileName, useWeightingFile,
						weightFilePath);
				

			}
			
			WriteTrialInfo(f3);
			NearestNeighborMethod.GetResultsFromRunFiles3(1, numrnd,f3);
			CompileNFoldPredictions(f3,endpoint,desc,trial);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void CompileNFoldPredictions(String trialFolderPath,String endpoint,String desc,String trial) {
			int NSets=5;
			
			DecimalFormat df=new DecimalFormat("000.00");
			
			try {
				
				FileWriter fw=new FileWriter(trialFolderPath+"/run1-"+NSets+"-fitvalues.txt");
				FileWriter fw2=new FileWriter(trialFolderPath+"/run1-"+NSets+"-fitvalues-sortbyerror.txt");
				
				fw.write("CAS\tExp\tPred\r\n");
				
				java.util.Vector<String>vec=new java.util.Vector<String>();
				
			for (int i=1;i<=NSets;i++) {
				File filei=new File(trialFolderPath+"/run"+i+"/run"+i+"-fitvalues.txt");
	//			System.out.println(filei.exists());
				
				BufferedReader br=new BufferedReader(new FileReader(filei));
				String header=br.readLine();
				
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					
					java.util.List<String> l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
					
					String CAS=l.get(1);
					String exp=l.get(2);
					String pred=l.get(3);
				
					double dexp=Double.parseDouble(exp);
					double dpred=Double.parseDouble(pred);
					
					double error=Math.abs(dexp-dpred);
					if (dpred==-9999) error=-9999;
					
					String newLine=CAS+"\t"+exp+"\t"+pred;
					
					
					vec.add(df.format(error)+"\t"+newLine);
					
					fw.write(newLine+"\r\n");
					fw.flush();
				}
				Collections.sort(vec,Collections.reverseOrder());
				br.close();
			}
			
			fw2.write("CAS\tExp\tPred\tError\r\n");
			for (int i=0;i<vec.size();i++) {
				String Line=vec.get(i);			
				String sortLine=(Line.substring(Line.indexOf("\t")+1,Line.length()));
				sortLine+="\t"+Line.substring(0,Line.indexOf("\t"));
				fw2.write(sortLine+"\r\n");
				fw2.flush();
			}
			
			fw.close();
			fw2.close();
			} catch (Exception  e) {
				e.printStackTrace();
			}
			
		}

	public void WriteTrialInfo(String destFolder) {
			
			try {
				FileWriter fw=new FileWriter(destFolder+"/runinfo.txt");
				
	//			System.out.println(destFolder);
				
				fw.write("QSARMethod="+QSARMethod+"\r\n");
				fw.write("PredictionsToUse="+PredictionsToUse+"\r\n");			
				fw.write("useEqualWeighting="+useEqualWeighting+"\r\n");
				fw.write("MinNumChemicals="+this.MinNumChemicals+"\r\n");
				fw.write("info="+this.info+"\r\n");
				fw.write("DescriptorSet="+this.DescriptorSet+"\r\n");
				fw.write("UseRmaxCriterion="+this.UseRmaxCriterion+"\r\n");
				fw.write("useModelEllipsoid="+this.useModelEllipsoid+"\r\n");
				fw.write("useFragments="+useFragments+"\r\n");
				
				if (QSARMethod.equals("One Step"))
					fw.write("NumClusters="+this.NumClusters+"\r\n");
				else 
					fw.write("NumClusters=N/A\r\n");
	
				
				fw.close();
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	
	    	
	    	
	    }

		void readDataSets(String trainingFilePath,String testFilePath) {
			CSVLoader atf = new CSVLoader();

			try {
				testSet = atf.getDataSetFromFile(testFilePath,",");
				trainingSet = atf.getDataSetFromFile(trainingFilePath,",");
				
			} catch (Exception e) {
				e.printStackTrace();
				
			}
		}
	
	
		
		
		
		
		
	public void PerformExternalCalcs(String method,String trainingFilePath, String testFilePath,
			String xmlFilePath, String outputFolderPath,
			String outputTextFileName, boolean UseWtFile, String weightFilePath) {

		try {

			readDataSets(trainingFilePath,testFilePath);

	        testData = new TestData(testSet);
	        testData.setAlpha(alpha); // set up the uncertainty factor for uncertainty calculations
			
			AllResultsXMLReader arxr=new AllResultsXMLReader();
			allResults=arxr.readAllResults(xmlFilePath, trainingSet, false);

//			double[] weights;
//			if (UseWtFile) {
//				// read in weights and change scales:
//				weights = readWeights(weightFilePath, trainingDataset,
//						allResults.getScales());
//			}

			System.out.println("XML File Loaded");

			this.runTestChemicals();

			double[] exp = new double[testData.numInstances()];
			double[] pred = new double[testData.numInstances()];
			String[] CAS = new String[testData.numInstances()];

			for (int j = 0; j < pred.length; j++) {
				TestChemical chemical = (TestChemical)testData.instance(j);
				pred[j] = chemical.getPredictedValue();
				exp[j] = chemical.getToxicity();
				CAS[j] = chemical.getName();
			}
			
			double Yexpbar = trainingSet.calculateAverageToxicity();

			String outputFilePath = outputFolderPath + "/" + outputTextFileName;

			this.WriteHierarchicalResults(CAS, exp, pred, Yexpbar,
					outputFilePath);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	/**
     * Called by PredictToxicityHierarchical
     * @param testDataset
     * @param allResults
     * @param trainingDataset
     * @throws Exception
     */
    public void initialize(Instances testDataset,AllResults allResults,Instances trainingDataset) throws Exception { 

    	testData = new TestData(testDataset);
        testData.setAlpha(alpha); // set up the uncertainty factor for uncertainty calculations
        this.allResults = allResults;         
        this.trainingSet = trainingDataset;
        
    }
	
	private void runRmax() throws Exception {
		Vector<OptimalResults> vResults = allResults.getResults();
		// loop through all clusters and make predictions with the clusters
		// that meet the constraints:
		for (int i = 0; i < vResults.size(); i++) {
			OptimalResults results = vResults.get(i);

			// System.out.println(results.getClusterNumber());

			ClusterInfo clusterInfo = new ClusterInfo(results);
			if (results.isValid()) {
//				clusterInfo.setChemicalNameIndex(chemicalNameIndex);
				clusterInfo.setTrainingSet(trainingSet);
				clusterInfo.setScales(allResults.getScales());
//				clusterInfo.recalculateCentroidRMax();//not needed
				testData.makePredictions(clusterInfo, useFragments,useModelEllipsoid);
			} else {
            	for (int j=0;j<testData.numInstances();j++) {
            		TestChemical chemical = (TestChemical)testData.instance(j);
                	chemical.getInvalidErrorMessages().add("Model is not statistically valid");
                	chemical.getInvalidClusters().add(results.getClusterNumber());
            	}
            }
		}
	}
	
	
	/**
	 * Simple version called by PredictToxicityHierarchical class. Calculations
	 * are designed for a single chemical since it returns results vector for
	 * clusters used in the prediction
     * @param hierarchicalMethod TODO
	 */
    public TestChemical runTestChemical() throws Exception {        
    	runTestChemicals();
		TestChemical chemical = (TestChemical) testData.firstInstance();
        return chemical;
    }
	
	private void runHierarchical() throws Exception {
		
		allClusterInfo = new AllClusterInfo(allResults);
		allClusterInfo.setTrainingDataset(trainingSet); 
//		allClusterInfo.setChemicalNameIndex(chemicalNameIndex); 

		// sort from smallest clusterNumber to largest
		allClusterInfo.sortResults(); 
		allClusterInfo.mapClusterResults();
		allClusterInfo.initializeDistances(testData); 

		// loop through all hierarchical maps of clusters and make
		// predictions
		// using the closest clusters from each step (provided they meet the
		// constraints)
		
		while (allClusterInfo.getClusterMap().size() > 1) {
		
			allClusterInfo.updateClusterMap(testData); // update clusterMap
														// and testData
														// distances
			testData.makePredictions(allClusterInfo, useFragments,
					UseRmaxCriterion,RmaxFactor,useModelEllipsoid);
		}

	}
	
	private void runOneStep() throws Exception {
		
		
		allClusterInfo = new AllClusterInfo(allResults);
		allClusterInfo.setTrainingDataset(trainingSet);
//		allClusterInfo.setChemicalNameIndex(chemicalNameIndex);
		// sort from smallest clusterNumber to largest
		allClusterInfo.sortResults();
		allClusterInfo.mapClusterResults();
		allClusterInfo.initializeDistances(testData);

		// loop through all hierarchical maps of clusters and make
		// predictions
		// using the closest clusters from each step (provided they meet the
		// constraints)
		while (allClusterInfo.getClusterMap().size() > 1) {
//			System.out.println(allClusterInfo.getClusterMap().size());
			allClusterInfo.updateClusterMap(testData); // update clusterMap
			// and testData
			// distances

			Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
	        
	        if (clusterMap.size()!=NumClusters) continue;
			
//	        boolean HaveUnpredictedCluster=false;
	        
	        for (int i=0; i<clusterMap.size(); i++) {
	        	ClusterInfo ci=clusterMap.get(i);
	        	
	        	int NumChemicals=ci.getResults().getNumChemicals();
	        }
	        
			testData.makePredictions(allClusterInfo, useFragments,
					UseRmaxCriterion,RmaxFactor,useModelEllipsoid);
		}
		
	}
	
	private void runFrag() throws Exception {
		allClusterInfo = new AllClusterInfo(allResults);
		allClusterInfo.setTrainingDataset(trainingSet);
		// sort from smallest clusterNumber to largest
//		allClusterInfo.sortResults();//not needed since done in validation class
		allClusterInfo.mapClusterResults();
		allClusterInfo.initializeDistances(testData);

		// ***************************************************
		// Need to redo some calcs because Paul's ClusterInfo constructor skips some
		// calcs if the results are invalid (due to error for coefficients)
		
		ClusterInfo ci=allClusterInfo.getClusterMap().get(0);
		
		OptimalResults or=ci.getResults();
		or.setValid(true); 
		
		// commented
		// It seems the following lines does not contribute to prediction result
		//  or.regenX2inv();
		//  ci.recalculateCentroidRMax();
		//  ci.recalculatehMax();
		// ***************************************************
		
		testData.makePredictions(allClusterInfo, useFragments,
				UseRmaxCriterion,RmaxFactor,useModelEllipsoid);
	}
	
	/**
	 * This version stores predictions in TestChemcal objects
	 * @param hierarchicalMethod TODO
	 * @return
	 * @throws Exception
	 */
	public void runTestChemicals() throws Exception {
		message="";
		if (QSARMethod.equals("Hierarchical")) {
			runHierarchical();
		} else if (QSARMethod.equals("Rmax")) {
			runRmax();
		} else if (QSARMethod.equals("One Step")) {
			runOneStep();
		} else if (QSARMethod.equals("Frag")) {
			runFrag();
		} else if (QSARMethod.equals("One Step LC50")) {
			runOneStepLC50();
		}
		
//		if (PredictionsToUse.equals("RandomSampling")) {
//			runRandomSampling();
//			return;
//		}
		
		for (int j = 0; j < testData.numInstances(); j++) {
			TestChemical chemical = (TestChemical) this.testData.instance(j);

//			System.out.println(j+"\n");
			// get rid of predictions that may have been stored:
			chemical.getPredictions().removeAllElements(); 
			chemical.getUncertainties().removeAllElements();

			if (PredictionsToUse.equals("ClosestCluster")) {
				runClosestCluster(chemical);
			} else if (PredictionsToUse.equals("SmallestUncertainty")) {
				runSmallestUncertainty(chemical);
			} else if (PredictionsToUse.equals("WeightedAverage")
					|| PredictionsToUse.equals("DistanceWeighted")) {
				runWeightedAverage(chemical);
			}				

		} // end loop over chemicals in Test set

	}
	
	void runSmallestUncertainty(TestChemical chemical) throws Exception {
		
		Vector<Integer> cu = chemical.getClustersUsed();
		Vector<Integer> nc = chemical.getNumChemicals();

		int smallestUncertaintyIndex = -1;
		double MinUncertainty = 99999;
		double PredictedValue = -9999;

		for (int i = 0; i < cu.size(); i++) {
			int num = cu.get(i);
			int numChem = nc.get(i);

			if (numChem >= MinNumChemicals || MinNumChemicals == -1) {
				OptimalResults results = this.GetResults(num);

				double predictedValue = chemical.calculateToxicValue(results);
				double predictedUncertainty = chemical.calculateUncertainty(results);

				if (predictedUncertainty < MinUncertainty) {
					MinUncertainty = predictedUncertainty;
					smallestUncertaintyIndex = num;
					PredictedValue=predictedValue;
				}
			}

		}

		if (smallestUncertaintyIndex > -1) {
			chemical.setPredictedValue(PredictedValue);
			chemical.setPredictedUncertainty(MinUncertainty);

			chemical.getPredictions().add(PredictedValue);
			chemical.getUncertainties().add(MinUncertainty);

			chemical.getClustersUsed().removeAllElements();
			chemical.getClustersUsed().add(smallestUncertaintyIndex);

		} else {
			chemical.setPredictedValue(null);
			chemical.setPredictedUncertainty(null);
		}

	}
	
	void runClosestCluster(TestChemical chemical) throws Exception {
		
		Vector<Double> distances = chemical.getDistances();
		Vector<Integer> cu = chemical.getClustersUsed();
		Vector<Integer> nc = chemical.getNumChemicals();

		int closestCluster = -1;
		double MinDist = 99999;

		for (int i = 0; i < distances.size(); i++) {
			double dist = (Double) distances.get(i);
			int numChem = (Integer) nc.get(i);

			if (dist < MinDist) {
				if (numChem >= MinNumChemicals || MinNumChemicals == -1) {
					MinDist = dist;
					closestCluster = (Integer) cu.get(i);
				}
			}
		}

		if (closestCluster > -1) {
			OptimalResults results = this.GetResults(closestCluster);

			double predValue = chemical.calculateToxicValue(results);
			
			double predUncertainty = chemical.calculateUncertainty(results);
					
			chemical.setPredictedValue(predValue);
			chemical.setPredictedUncertainty(predUncertainty);

			chemical.getPredictions().add(predValue);
			chemical.getUncertainties().add(predUncertainty);

			chemical.getClustersUsed().removeAllElements();
			chemical.getClustersUsed().add(closestCluster);

		} else {
			chemical.setPredictedValue(null);
			chemical.setPredictedUncertainty(null);
		}
	}
	
	

  
	
	private void runOneStepLC50() throws Exception {
		
		//just in case we have invalid models but still wanna run em:
		OptimalResults or=allResults.getResults().get(0);//TODO not used?
		
		allClusterInfo = new AllClusterInfo(allResults);
		allClusterInfo.setTrainingDataset(trainingSet);
		// sort from smallest clusterNumber to largest
//		allClusterInfo.sortResults();//not needed since done in validation class
		allClusterInfo.mapClusterResults();
		allClusterInfo.initializeDistances(testData);


		testData.makePredictions(allClusterInfo, useFragments,UseRmaxCriterion,RmaxFactor,useModelEllipsoid);
		
		
	}
	
	
	
	
	
		
    public OptimalResults GetResults(int num) {
    	Vector<OptimalResults> vResults = allResults.getResults();
    	
    	for (int i=0;i<vResults.size();i++) {
    		OptimalResults results = (OptimalResults)vResults.get(i);
    		
    		if (results.getClusterNumber()==num) {
    			return results;
    		}
    		
    	}    	
    	return null;
    }
	
	private void runWeightedAverage(TestChemical chemical) throws Exception {

		Vector<Integer> cu = chemical.getClustersUsed();
		Vector<Integer> nc = chemical.getNumChemicals();

		Vector<OptimalResults> rv = new Vector<>();
		// Vector iv=new Vector();

		Vector<Integer> cu2 = (Vector<Integer>) cu.clone();

		cu.removeAllElements();
		
//		System.out.println(cu2.get(0));

		for (int i = 0; i < cu2.size(); i++) {
			int num = (Integer) cu2.get(i);
			int numChem = (Integer) nc.get(i);

			if (numChem >= MinNumChemicals || MinNumChemicals == -1) {
				// System.out.println(chemical.stringValue(0)+"\t"+num);

				rv.add(this.GetResults(num));
				cu.add(num); // only add the numbers for
				// clusters exceeding min
				// number if minnumber >-1
				// iv.add(this.GetClusterInfo(num));
			}
		}
		for (int i = 0; i < rv.size(); i++) {
			OptimalResults results = (OptimalResults) rv.get(i);
			chemical.getPredictions().add(chemical.calculateToxicValue(results));
			chemical.getUncertainties().add(chemical.calculateUncertainty(results));
		}

		//		 System.out.println(chemical.stringValue(0)+"\t"+chemical.getPredictions().size()+"\t"+chemical.getClustersUsed().size());

		double avgtv = 0;
		double avgerr = 0;
		double weightTotal = 0;
		double weightSqTotal = 0;

		if (rv.size() == 1) {
			avgtv = (Double) chemical.getPredictions().get(0);
			avgerr = (Double) chemical.getUncertainties().get(0);
			weightTotal=1;
		} else if (rv.size()>1){

			for (int i = 0; i < rv.size(); i++) {
				double tv=-9999;
				double weight=-9999;
				double err=-9999;
							
				tv = (Double) chemical.getPredictions().get(i);

				if (hierarchicalMethod==1) {
					err = (Double) chemical.getUncertainties().get(i);
				} else 	if (hierarchicalMethod==2) {
					err = (Double) chemical.getStderr().get(i);
				}

				weight = 1.0 / (err * err);
				
			
				
				if (err==0) weight=0;//avoids infinite weight which occurs sometimes for binary end points such as mutagenicity
				if (Double.isNaN(err)) weight=0;

 
				if (this.UseCancerConstraints) {
					if (tv > this.MinCancerConstraint && tv < this.MaxCancerConstraint) {
						continue;
					}
				}

				if (PredictionsToUse.equals("DistanceWeighted")) {
					OptimalResults results = (OptimalResults) rv.get(i);
					double[] scales = this.allResults.getScales();
					double d = this.calculateDistance(results, chemical,
							scales);

					double K = 100;

					weight = Math.exp(-d * d / (2 * K * K));
					//				 System.out.println(chemical.stringValue(0)+"\t"+weight);
				}

			
				if (useEqualWeighting)
					weight = 1;

				avgtv += weight * tv;
				weightTotal += weight;
				weightSqTotal += weight * weight;

				if (hierarchicalMethod==1){				
					avgerr += weight * weight * err * err;// couldn't you just use avgerr+=1/(err*err); ?
				} else if (hierarchicalMethod==2) {
					//do nothing
				}
			} //end loop over resultsvector

			avgtv = avgtv / weightTotal;
			
			
			if (hierarchicalMethod==1) {
				avgerr = Math.sqrt(avgerr / weightSqTotal); //old method
			} else if (hierarchicalMethod==2) {
				// new method for avgerr (see TEST user guide)
				avgerr = Math.sqrt(1.0 / weightTotal);
				double tstat = statistics.tstat(1.0 - alpha / 2.0, rv.size() - 1);
				avgerr *= tstat;
			}
			
		
		}//end else rv.size<>1

//		System.out.println(avgtv+"\t"+avgerr);

		if (rv.size() == 0 || weightTotal==0) {
			chemical.setPredictedValue(null);
			chemical.setPredictedUncertainty(null);
			message = "A prediction cannot be made";
		} else {
			chemical.setPredictedValue(avgtv);
			chemical.setPredictedUncertainty(avgerr);
			message = "OK";
			// System.out.println("**"+chemical.getPredictedValue());
		}
	}
	
	public double calculateDistance(OptimalResults results,Instance chemical,double []scales) throws Exception {
        
        double[] centroid = null;
        double diff;
        
        // old results files have null centroids
        if (results.getCentroid()!=null) {
            centroid = results.getCentroid();
        } else {
            System.out.println("Centroid not found");
            return Double.MAX_VALUE;
        }
        
        double sum = 0;
        for (int i=0; i<centroid.length; i++) {
        	if (scales[i] == 0.0) {
        		diff = 0.0; // when the scaling is 0.0 in the training set,
        		// descriptor is not used.
        	} else {
        		// diff = (centroid[i]-chemical.value(i))/scales[i];
        		diff = (centroid[i]-chemical.value(i)) / scales[i];
        	}
        	sum += diff*diff;

        }
        
        return Math.sqrt(sum);
    }
	
    

	void WriteHierarchicalResults(String [] CAS,double []exp,double [] pred,double Yexpbar,String outputFilePath) {
		
		java.text.DecimalFormat df=new java.text.DecimalFormat("0.0000");
		
		try {
			
//			FileWriter fw=new FileWriter(f4+"/"+resultsfilename);
			FileWriter fw=new FileWriter(outputFilePath);
			
			fw.write("#\tCAS\texpToxicValue\tpredToxicValue\tR2\tQ2\t#Chemicals\tCurrentQ2ext\tCurrentR2abs\tCurrentR2\tCurrentMAE\tCoverage\r\n");
			
			for (int j=0;j<exp.length;j++) {
				
				fw.write((j+1)+"\t"); //#
				fw.write(CAS[j]+"\t"); //CAS
				
				fw.write(df.format(exp[j])+"\t"); //expToxicValue
				fw.write(df.format(pred[j])+"\t"); //predToxicValue
				
				// the following three stats are mainly for the FDA method but they are included here to make the table the same format as the files from the fda method for ease of parsing
				fw.write("N/A"+"\t"); // R2 - N/A since can have multiple cluster models
				fw.write("N/A"+"\t"); // Q2 - N/A since can have multiple cluster models
				fw.write("N/A"+"\t"); // #chemicals - N/A since can have multiple cluster models

				String stats=PredictionStats.getCurrentStats(j, exp, pred, Yexpbar, df);
				fw.write(stats);
				
				fw.write("\r\n");
				fw.flush();
				
			}
			
			System.out.println("Q2ext\tR2abs\tR2\tMAE\tCoverage");
			System.out.println(PredictionStats.GetStats(exp, pred, Yexpbar, df));
			fw.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
        try {
            Validation2 v = new Validation2();
            
//            v.tryDatabase();
            v.runRndSets();
            
//            select random run
            
        	
        } catch (Exception ex) {
            System.err.println("error: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

}


