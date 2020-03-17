package QSAR.validation2;

import java.util.Hashtable;
import java.util.Vector;

import Jama.Matrix;
import wekalite.Instances;
import QSAR.qsarOptimal.AllResults;
import QSAR.qsarOptimal.OptimalResults;

public class LDAMethod {
	
    public AllResults allResults;
    private AllClusterInfo allClusterInfo;
    public Instances trainingSet;
    public Instances testSet;
    public TestData testData;
    
    public boolean useFragments=false;
    public double minPosScore=0.5;// for maximum coverage- need to have program flag values less than 0.5
	boolean useModelEllipsoid = true;
	boolean UseRmaxCriterion = true;
	public double RmaxFactor=1.0; // decrease RmaxFactor to make the criterion more strict

    private double alpha = 0.10; // needed?

    
    String QSARMethod;
    
    
    String message;
    
  //goes through class variable and compiles list of scores- i.e. 0,1 
  	private static Vector<Double> getScoreList(Instances instances) {
  		
  		Vector <Double>scores=new Vector<Double>();
  		
  		for (int i=0;i<instances.numInstances();i++) {
  			
  			Double score=instances.instance(i).classValue();
  			
  			if (!scores.contains(score)) {
  				scores.add(score);
  			}
  		}
  		java.util.Collections.sort(scores);
  		
  		return scores;
  	}
  	

    private void runDiscriminantModel() throws Exception {
//		System.out.println("runDiscModel");
		
		allClusterInfo = new AllClusterInfo(allResults);
		allClusterInfo.setTrainingDataset(trainingSet);
		allClusterInfo.mapClusterResults();
		allClusterInfo.initializeDistances(testData);

		Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
		ClusterInfo clusterInfo = clusterMap.get(0);//only have 1 model for LDA
        OptimalResults results = clusterInfo.getResults();
		int[] descriptorNums = results.getDescriptors();
		
		double [][]descriptors=this.getTrainingSetDescriptorMatrix(descriptorNums);
		double[] exp = getExpArray();
		
		Vector <Double>scores=getScoreList(trainingSet);
		int []scoreCounts=this.getScoreCounts(exp, scores);
		double [][]avgValues=this.calculateAvgValues(exp, descriptors, scores,scoreCounts);
		Vector<Matrix>SSCP=this.calculateSmatrix(exp,descriptors,avgValues,scores,scoreCounts);

//		SSCP.get(0).print(6, 4);
		
		testData.makePredictionsLDA(allClusterInfo, useFragments,
				UseRmaxCriterion,RmaxFactor,useModelEllipsoid,SSCP,avgValues);
		

	}
    
    private double[][] calculateAvgValues(double [] exp,double [][]descriptors,Vector<Double>scores,int []scoreCounts) {
		
		int numVars=descriptors[0].length;
		int numScores=scores.size();
		int numInstances=descriptors.length;

		double [][]avgValues=new double [numScores][numVars];
		
		
		for (int i=0;i<numInstances;i++) {
			Double score=exp[i];
			int scoreIndex=scores.indexOf(score);
			scoreCounts[scoreIndex]++;

			for (int j=0;j<numVars;j++) {
				double valuej=descriptors[i][j];
				avgValues[scoreIndex][j]+=valuej;
			}
		}
		
		for (int i=0;i<scores.size();i++) {
			for (int j=0;j<numVars;j++) {
				avgValues[i][j]/=scoreCounts[i];
			}
		}
		return avgValues;
		
	}
	
	
	


	
    
	/**
	 * This version stores predictions in TestChemcal objects
	 * @param hierarchicalMethod TODO
	 * @return
	 * @throws Exception
	 */
	public void runTestChemicalsLDA() throws Exception {
		message="";


		runDiscriminantModel2();
		
		for (int j = 0; j < testData.numInstances(); j++) {
			TestChemical chemical = (TestChemical) this.testData.instance(j);
			chemical.setPredictedValue((Double)chemical.getPredictions().get(0));
			chemical.setPredictedUncertainty((Double)chemical.getUncertainties().get(0));
			chemical.getPredictions().removeAllElements(); 
			chemical.getUncertainties().removeAllElements();
		}
		
		//Don't need to runWeightedAverage- since only have 1 model for each MOA!
		
//		for (int j = 0; j < testData.numInstances(); j++) {
//			TestChemcal chemical = (TestChemcal) this.testData.instance(j);
//			chemical.getPredictions().removeAllElements(); 
//			chemical.getUncertainties().removeAllElements();
//			runWeightedAverage(chemical,2);
//
//		} // end loop over chemicals in Test set

	}
	
	private void runDiscriminantModel2() throws Exception {
//		System.out.println("runDiscModel");
		
		allClusterInfo = new AllClusterInfo(allResults);
		allClusterInfo.setTrainingDataset(trainingSet);

		allClusterInfo.mapClusterResults();
		allClusterInfo.initializeDistances(testData);

		Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
		ClusterInfo clusterInfo = clusterMap.get(0);//only have 1 model for LDA
        OptimalResults results = clusterInfo.getResults();
		int[] descriptorNums = results.getDescriptors();
		
		double [][]descriptors=this.getTrainingSetDescriptorMatrix(descriptorNums);
		double[] exp = getExpArray();
		
		Vector <Double>scores=getScoreList(trainingSet);
		int []scoreCounts=this.getScoreCounts(exp, scores);
		double [][]avgValues=this.calculateAvgValues(exp, descriptors, scores,scoreCounts);
		Vector<Matrix>SSCP=this.calculateSmatrix(exp,descriptors,avgValues,scores,scoreCounts);

//		SSCP.get(0).print(6, 4);
		
		testData.makePredictionsLDA2(allClusterInfo, useFragments,
				UseRmaxCriterion,RmaxFactor,useModelEllipsoid,SSCP,avgValues);

	}
	
	private double[] getExpArray() {
		double [] exp=new double [trainingSet.numInstances()];
		for (int i=0;i<trainingSet.numInstances();i++) {
			exp[i]=trainingSet.instance(i).getToxicity();
		}
		return exp;
	}
	
	private double [][]getTrainingSetDescriptorMatrix(int [] descriptorNums) {
		double [][]descriptors =new double [trainingSet.numInstances()][descriptorNums.length];

		//store descriptors as 2d array:
		for (int i=0;i<trainingSet.numInstances();i++) {
			for (int j=0;j<descriptorNums.length;j++) {
				descriptors[i][j]=trainingSet.instance(i).value(descriptorNums[j]);
			}
		}
		return descriptors;

	}
	
	
	
	public String [][] calculateLC50_for_each_MOA2(Vector<String> vecMOA,Instances testDataset,Hashtable<String,Instances>htTrainingSets,Hashtable<String,AllResults>htAllResults) {
		try {
			this.QSARMethod="One Step LC50";
			
			String [][] results=new String [testDataset.numInstances()][vecMOA.size()];
			
			//load all xml files into memory:
			for (int i = 0; i < vecMOA.size(); i++) {
				String MOA = vecMOA.get(i);

				this.allResults=htAllResults.get(MOA);
				this.trainingSet=htTrainingSets.get(MOA);
				
				testData = new TestData(testDataset);
				testData.setAlpha(alpha); //

				TestChemical chem=(TestChemical)testData.firstInstance();
				
//				if (chem.getName().equals("78-79-5")) {
//					System.out.println("\r\n"+MOA);
//				}
				
				this.runTestChemicalsLDA();
				
//				TestChemcal chem0=(TestChemcal)testData.instance(0);
//				System.out.println(MOA+"\t"+chem0.getPredictedValue());
				
				
				for (int j=0;j<testData.numInstances();j++) {
					TestChemical chemical=(TestChemical)testData.instance(j);
					double pred=chemical.getPredictedValue();
					double unc=chemical.getPredictedUncertainty();
					
					String error="";
					
					if (chemical.getInvalidErrorMessages().size()>0) {
						error=(String)chemical.getInvalidErrorMessages().get(0);
					} else {
						error="OK";
					}

					
//					System.out.println(MOA+"\t"+pred+"\t"+error);
					
					results[j][i]=pred+"\t"+unc+"\t"+error;
				}
				
				
				
			}// end loop over MOAs

			return results;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Run linear discriminant analysis for MOA model, run from PredictToxicityLDA (TEST)
		public String [][] calculateLDA_Scores2(Vector<String> vecMOA,Instances testDataset,Hashtable<String,Instances>htTrainingSets,Hashtable<String,AllResults>htAllResults) {
			try {
				this.QSARMethod="LDA";
				
//				System.out.println("run calculateLDA_Scores");

				String[][] predArray = new String[testDataset.numInstances()][vecMOA
						.size()];

				for (int j = 0; j < vecMOA.size(); j++) {
					
					testData = new TestData(testDataset);//put inside loop since need to reset vectors for each chemical object
					testData.setAlpha(alpha); // set up the uncertainty factor for

					String MOA = vecMOA.get(j);

					if (MOA.indexOf("Non-ache")>-1) continue;

					this.trainingSet = htTrainingSets.get(MOA);
					this.allResults = htAllResults.get(MOA);

//					System.out.println(MOA);
					this.runTestChemicalsLDA();

					for (int k = 0; k < testData.numInstances(); k++) {
						TestChemical chemical = (TestChemical) this.testData
								.instance(k);
						
//						predArray[k][j] = chemical.getPredictedValue();

						predArray[k][j]=chemical.getPredictedValue()+"\t"+chemical.getPredictedUncertainty()+"\t";

						if (chemical.getInvalidErrorMessages().size()>0) {
							predArray[k][j]+=chemical.getInvalidErrorMessages().get(0);
						} else {
							predArray[k][j]+="OK";
						}
					}
//					System.out.println(MOA+"\t"+predArray[0][j]);

				}// end loop over MOAs
				
				return predArray;


			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
		private Vector<Matrix>calculateSmatrix(double [] exp,double [][]descriptors,double [][]avgValues,Vector<Double>scores,int []scoreCounts) {
			
	    	//TODO- if inverse of S matrix  for a given score is singular 
	        //we can't calculate the probability of the score!!!

			int numVars=descriptors[0].length;
			int numInstances=descriptors.length;
			
			Vector<Matrix>SSCP=new Vector<Matrix>();
			for (int i=0;i<scores.size();i++) {
				SSCP.add(new Matrix(numVars,numVars));
			}

			
//			Matrix bob=new Matrix(avgValues);
//			bob.print(6,4);
			
			for (int i=0;i<numInstances;i++) {
				
				Double score=exp[i];
				
				int scoreIndex=scores.indexOf(score);
				
				Matrix SSCPindex=SSCP.get(scoreIndex);
				
				for (int j=0;j<numVars;j++) {
					
					double valuej=descriptors[i][j];
					
					for (int k=0;k<numVars;k++) {
						
						double valuek=descriptors[i][k];
						
						double oldValue=SSCPindex.get(j, k);
						double newValue=(valuej-avgValues[scoreIndex][j])*(valuek-avgValues[scoreIndex][k]);
						
						SSCPindex.set(j,k,oldValue+newValue);
						
					}
				}
				
			}

			for (int i=0;i<scores.size();i++) {
				Matrix SSCPindex=SSCP.get(i);
				SSCP.set(i,SSCPindex.times(1.0/(scoreCounts[i]-1)));
			}

			return SSCP;
			
		}
		
		
		
		
		private int[] getScoreCounts(double []exp,Vector<Double>scores) {
			int numScores=scores.size();

			int [] scoreCounts=new int [numScores];//counts of each score
			
			for (int i=0;i<exp.length;i++) {
				Double score=exp[i];
				int scoreIndex=scores.indexOf(score);
				scoreCounts[scoreIndex]++;
			}
			
			return scoreCounts;
			
		}
		
}
