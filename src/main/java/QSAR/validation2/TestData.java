/**
 * 
 */
package QSAR.validation2;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import QSAR.qsarOptimal.OptimalResults;
import Jama.Matrix;
import wekalite.*;

/**
 * @author PHARTEN
 *
 */
public class TestData extends Instances {
    
    /**
     * 
     */
    private int[] previousClosestCluster;
    private double alpha;
    private static final long serialVersionUID = 3033440524276774221L;
    private static Random rand = new Random(123456789L);

    public TestData(Instances data) {
		this.setAttributes(data.getAttributes().clone());

		this.setInstances(new ArrayList<Instance>());
		
		for (int i=0;i<data.numInstances();i++) {
			addInstance(new TestChemical(data.instance(i)));
		}
		
        previousClosestCluster = new int[data.numInstances()];
        for (int i=0; i<numInstances(); i++) {
            TestChemical chemical = (TestChemical)instance(i);
            chemical.findFragments();
            previousClosestCluster[i] = -1;
        }
       
    }
    
//    /**
//     * Converts an instance to a chemical and adds it to the 
//     * end of the set. Increases the size of the dataset if
//     * it is not large enough. Does not check if the instance
//     * is compatible with the dataset.
//     *
//     * @param instance the Instance to be added
//     */
//    public void add(/*@non_null@*/ Instance instance) {
//
//      TestChemical newChemical = new TestChemical(instance);
//
//      newChemical.setDataset(this);
//      newChemical.setSeed(rand.nextInt());
//      m_Instances.addElement(newChemical);
//
//    }
    
    private int findClosestClusterIndex(int chemicalNumber, Vector <ClusterInfo> clusterMap) {
        double minDistance, thisDistance;
        int minIndex;
        double[] distances;
        
        // find minimum distance for a chemical.
        distances = clusterMap.get(0).getDistances();
        minDistance = distances[chemicalNumber];
        minIndex = 0;
        
        for (int i=1; i<clusterMap.size(); i++) {
            distances = clusterMap.get(i).getDistances();
            thisDistance = distances[chemicalNumber];
            if (thisDistance < minDistance) {
                minDistance = thisDistance;
                minIndex = i;
            }
        }

        return minIndex;
    }
    
    private int findClosestClusterIndex(int chemicalNumber, 
            TreeMap<Double, TreeSet<Integer>>[] minDistances) {
        return minDistances[chemicalNumber].firstEntry().getValue().first();
    }
    
    //used by hierarchical method    
    public void makePredictions(AllClusterInfo allClusterInfo, boolean useFragments, boolean useRmax,double RmaxFactor,boolean useModelEllipsoid) throws Exception {
        Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
        ClusterInfo clusterInfo;
        OptimalResults results;
        TestChemical chemical;
        //int npred = 100;
        
//        System.out.println("gen#"+allClusterInfo.generationNumber);
        
        // make all predictions for test chemicals.
        for (int i=0; i<this.numInstances(); i++) {
        	
//        	System.out.println("\t"+i);
        	
            chemical = (TestChemical)this.instance(i);

            clusterInfo = clusterMap.get(findClosestClusterIndex(i, 
                    allClusterInfo.getMinDistances()));
            
            results = clusterInfo.getResults();
            
            if (results.getClusterNumber() != previousClosestCluster[i]) {
                previousClosestCluster[i] = results.getClusterNumber();

//                System.out.println(clusterMap.size()+"\t"+results.getNumChemicals());
                
                if (results.isValid()) {  
                	
//            		if (chemical.getName().equals("143-50-0")) {
//            			String bob=results.clusterNumber+"\t"+clusterInfo.calculateDistance(chemical)+"\t"+results.getRMax()*1.01*RmaxFactor+"\t";
//            			bob+=chemical.calculateH00(results)+"\t"+results.gethMax()+"\t";
//            			bob+=chemical.areChemicalFragmentsAreInPredictingCluster(clusterInfo);
//            			System.out.println(bob);
//            		}

                    if (!useRmax || chemical.isWithinRmax(clusterInfo, RmaxFactor)) {//mult by 1.01 to avoid roundoff error for cases where test chemical is in the cluster
//                        System.out.println("Distance="+clusterInfo.calculateDistance(chemical)+"\tRmax="+results.getRMax());
                    	if (! useModelEllipsoid || chemical.isWithinEllipsoid(clusterInfo)) {
                            // && chemicalIsWithinExtrapolationRange(clusterInfo, chemical)
                            if (!useFragments || chemical.areChemicalFragmentsAreInPredictingCluster(clusterInfo)) {
                            	chemical.getPredictions().add(chemical.calculateToxicValue(results));
                                chemical.getUncertainties().add(chemical.calculateUncertainty(results));
                                chemical.getStderr().add(chemical.calculateStdError(results));
                                chemical.getDistances().add(clusterInfo.getDistances()[i]);
                                chemical.getNumChemicals().add((Integer)results.getNumChemicals());
                                chemical.getClustersUsed().add((Integer)results.getClusterNumber());//TMM

                            } else {
                            
                            	Vector<String>vecMissing=chemical.missingFragmentsInPredictingCluster(clusterInfo);

//                            	String message="Fragment constraint not met.<br>" +
//                            			"The following fragments in the test chemical are not present:<br>";
                            	
                            	String message="The following fragments in the test chemical are not present:<br>";
                            	
                            	for (int j=0;j<vecMissing.size();j++) {
                            		message+=vecMissing.get(j);
                            		if (j<vecMissing.size()-1) message+=", ";
                            	}
                             	
                            	chemical.getInvalidErrorMessages().add(message);
                            	chemical.getInvalidClusters().add(results.getClusterNumber());
                            }
                        } else {//ellipsoid constraint failed
                        	 
                        	chemical.getInvalidErrorMessages().add("Model ellipsoid constraint not met");
                        	chemical.getInvalidClusters().add(results.getClusterNumber());
                        	
                        }
                    } else { //failed Rmax constraint                    	
                    	chemical.getInvalidErrorMessages().add("Rmax constraint not met");
                    	chemical.getInvalidClusters().add(results.getClusterNumber());
                    }
                } else { //invalid model
                	chemical.getInvalidErrorMessages().add("Model is not statistically valid");
                	chemical.getInvalidClusters().add(results.getClusterNumber());

                }
                
//                Vector msgs=chemical.getInvalidErrorMessages();
//                if (msgs.size()>0) {
//                	String msg=(String)msgs.get(msgs.size()-1);
//                	if (!msg.equals("Model is not statistically valid"))
//                		System.out.println(chemical.stringValue(0)+"\t"+msg);          
//                }
            }
        }
        
    }
    
    //used by hierarchical method    
    public void makePredictionsLDA(AllClusterInfo allClusterInfo, boolean useFragments, boolean useRmax,double RmaxFactor,boolean useModelEllipsoid) throws Exception {
        Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
        ClusterInfo clusterInfo;
        OptimalResults results;
        TestChemical chemical;
        //int npred = 100;
        
        // make all predictions for test chemicals.
        
        for (int i=0; i<this.numInstances(); i++) {
        	
            chemical = (TestChemical)this.instance(i);
            clusterInfo = clusterMap.get(0);//only have 1 model for LDA
            results = clusterInfo.getResults();
           
            
            if (results.isValid()) {  
            	
                if (!useRmax || chemical.isWithinRmax(clusterInfo, RmaxFactor)) {//mult by 1.01 to avoid roundoff error for cases where test chemical is in the cluster
//                        System.out.println("Distance="+clusterInfo.calculateDistance(chemical)+"\tRmax="+results.getRMax());
                	if (! useModelEllipsoid || chemical.isWithinEllipsoid(clusterInfo)) {
                        // && chemicalIsWithinExtrapolationRange(clusterInfo, chemical)
                        if (!useFragments || chemical.areChemicalFragmentsAreInPredictingCluster(clusterInfo)) {
//                                System.out.println(i+"\t"+results.numChemicals+"\t"+Utils.doubleToString(results.q2,6,4)+"\t"+Utils.doubleToString(results.r2,6,4));
                            //chemical.addManyPredictions(results, npred);
                        	
//                            	System.out.println(results.clusterNumber+"\t"+results.numChemicals+"\t"+chemical.calculateToxicValue(results)+"\t"+chemical.calculateUncertainty(results));
//                            	if (chemical.stringValue(0).equals("66-25-1")) {
//                            		System.out.println(results.clusterNumber+"\t"+chemical.calculateToxicValue(results));
//                            	}
                        	
                        	chemical.getPredictions().add(chemical.calculateToxicValue(results));
                            chemical.getUncertainties().add(chemical.calculateUncertainty(results));
                            chemical.getStderr().add(chemical.calculateStdError(results));
                            chemical.getDistances().add(clusterInfo.getDistances()[i]);
                            chemical.getNumChemicals().add((Integer)results.getNumChemicals());
                            chemical.getClustersUsed().add((Integer)results.getClusterNumber());//TMM

                        } else {
                        
                        	Vector<String>vecMissing=chemical.missingFragmentsInPredictingCluster(clusterInfo);

//                            	String message="Fragment constraint not met.<br>" +
//                            			"The following fragments in the test chemical are not present:<br>";
                        	
                        	String message="The following fragments in the test chemical are not present:<br>";
                        	
                        	for (int j=0;j<vecMissing.size();j++) {
                        		message+=vecMissing.get(j);
                        		if (j<vecMissing.size()-1) message+=", ";
                        	}
                         	
                        	chemical.getInvalidErrorMessages().add(message);
                        	chemical.getInvalidClusters().add(results.getClusterNumber());
                        }
                    } else {//ellipsoid constraint failed
                    	 
                    	chemical.getInvalidErrorMessages().add("Model ellipsoid constraint not met");
                    	chemical.getInvalidClusters().add(results.getClusterNumber());
                    	
                    }
                } else { //failed Rmax constraint                    	
                	chemical.getInvalidErrorMessages().add("Rmax constraint not met");
                	chemical.getInvalidClusters().add(results.getClusterNumber());
                }
            } else { //invalid model
            	chemical.getInvalidErrorMessages().add("Model is not statistically valid");
            	chemical.getInvalidClusters().add(results.getClusterNumber());

            }
            
//            Vector msgs=chemical.getInvalidErrorMessages();
//                if (msgs.size()>0) {
//                	String msg=(String)msgs.get(msgs.size()-1);
//                	if (!msg.equals("Model is not statistically valid"))
//                		System.out.println(chemical.stringValue(0)+"\t"+msg);          
//                }
            
            

        }//end loop over test instances
        
        
    }
    
    private boolean chemicalFragmentsAreInPredictingCluster(TestChemical chemical, ClusterInfo clusterInfo) {

        OptimalResults results = clusterInfo.getResults();
        Instances trainingDataset  = clusterInfo.getTrainingSet();
        int clusterNumber =results.getClusterNumber();

        Vector<Integer> fragIndex = chemical.getFragments();
        Instance  instance;

        for (int index:fragIndex){
            boolean haveFrag=false;
            
            for (int j=0; j<results.getNumChemicals(); j++){
                instance = trainingDataset.instance(results.getChemicalNames()[j]);
                if (instance.value(index)!=0) {
                    haveFrag=true;
                    break;
                }
            }
            if (!haveFrag) {
//                System.out.println("do not Have frag "+chemical.attribute(index).name()+" in cluster " +clusterNumber);
                return false;
            }

        }
        return true;    
    }
    
    /**
     * This method is modified version so that predictions and uncertainties are always stored even if a constraint is not met
     * @param allClusterInfo
     * @param useFragments
     * @param useRmax
     * @param RmaxFactor
     * @param useModelEllipsoid
     * @param sSCP
     * @param avgValues
     * @throws Exception
     */
	public void makePredictionsLDA2(AllClusterInfo allClusterInfo,
			boolean useFragments, boolean useRmax, double RmaxFactor,
			boolean useModelEllipsoid, Vector<Matrix> sSCP, double[][] avgValues) throws Exception {
		
		
		 Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
	        ClusterInfo clusterInfo;
	        OptimalResults results;
	        TestChemical chemical;
	        //int npred = 100;
	        
	        // make all predictions for test chemicals.
	        
	        for (int i=0; i<this.numInstances(); i++) {
	        	
	            chemical = (TestChemical)this.instance(i);
	            clusterInfo = clusterMap.get(0);//only have 1 model for LDA
	            results = clusterInfo.getResults();
	           
	            
	            //always store predicted value so can display in web page:
            	chemical.getPredictions().add(chemical.calculateToxicValue(results));
                chemical.getUncertainties().add(chemical.calculateUncertainty(results));
	            
                
                
	            if (results.isValid()) {  
	            	
	                if (!useRmax || clusterInfo.calculateDistance(chemical) <= results.getRMax()*1.01*RmaxFactor) {//mult by 1.01 to avoid roundoff error for cases where test chemical is in the cluster
//	                        System.out.println("Distance="+clusterInfo.calculateDistance(chemical)+"\tRmax="+results.getRMax());
	                	if (! useModelEllipsoid || chemical.isWithinEllipsoid(clusterInfo)) {
	                        // && chemicalIsWithinExtrapolationRange(clusterInfo, chemical)
	                        if (!useFragments || chemicalFragmentsAreInPredictingCluster(chemical,clusterInfo)) {
//	                                System.out.println(i+"\t"+results.numChemicals+"\t"+Utils.doubleToString(results.q2,6,4)+"\t"+Utils.doubleToString(results.r2,6,4));
	                            //chemical.addManyPredictions(results, npred);
	                        	
//	                            	System.out.println(results.clusterNumber+"\t"+results.numChemicals+"\t"+chemical.calculateToxicValue(results)+"\t"+chemical.calculateUncertainty(results));
//	                            	if (chemical.stringValue(0).equals("66-25-1")) {
//	                            		System.out.println(results.clusterNumber+"\t"+chemical.calculateToxicValue(results));
//	                            	}
	                        	
//	                        	chemical.calculateProbability(results, avgValues, SSCP);
	                        	
	                            chemical.getStderr().add(chemical.calculateStdError(results));
	                            chemical.getDistances().add(clusterInfo.getDistances()[i]);
	                            chemical.getNumChemicals().add((Integer)results.getNumChemicals());
	                            chemical.getClustersUsed().add((Integer)results.getClusterNumber());//TMM

	                        } else {
	                        
	                        	Vector<String>vecMissing=this.missingFragmentsInPredictingCluster(chemical, clusterInfo);

//	                            	String message="Fragment constraint not met.<br>" +
//	                            			"The following fragments in the test chemical are not present:<br>";
	                        	
	                        	String message="The following fragments in the test chemical are not present:<br>";
	                        	
	                        	for (int j=0;j<vecMissing.size();j++) {
	                        		message+=vecMissing.get(j);
	                        		if (j<vecMissing.size()-1) message+=", ";
	                        	}
	                         	
	                        	chemical.getInvalidErrorMessages().add(message);
	                        	chemical.getInvalidClusters().add(results.getClusterNumber());
	                        }
	                    } else {//ellipsoid constraint failed
	                    	 
	                    	chemical.getInvalidErrorMessages().add("Model ellipsoid constraint not met");
	                    	chemical.getInvalidClusters().add(results.getClusterNumber());
	                    	
	                    }
	                } else { //failed Rmax constraint                    	
	                	chemical.getInvalidErrorMessages().add("Rmax constraint not met");
	                	chemical.getInvalidClusters().add(results.getClusterNumber());
	                }
	            } else { //invalid model
	            	chemical.getInvalidErrorMessages().add("Model is not statistically valid");
	            	chemical.getInvalidClusters().add(results.getClusterNumber());

	            }
	            
//	            Vector msgs=chemical.getInvalidErrorMessages();
//	                if (msgs.size()>0) {
//	                	String msg=(String)msgs.get(msgs.size()-1);
//	                	if (!msg.equals("Model is not statistically valid"))
//	                		System.out.println(chemical.stringValue(0)+"\t"+msg);          
//	                }
	        }//end loop over test instances
		// TODO Auto-generated method stub
		
	}
    
    public void makePredictionsLDA(AllClusterInfo allClusterInfo, boolean useFragments, boolean useRmax,double RmaxFactor,boolean useModelEllipsoid,Vector<Matrix>SSCP,double [][]avgValues) throws Exception {
        Vector <ClusterInfo> clusterMap = allClusterInfo.getClusterMap();
        ClusterInfo clusterInfo;
        OptimalResults results;
        TestChemical chemical;
        //int npred = 100;
        
        // make all predictions for test chemicals.
        
        for (int i=0; i<this.numInstances(); i++) {
        	
            chemical = (TestChemical)this.instance(i);
            clusterInfo = clusterMap.get(0);//only have 1 model for LDA
            results = clusterInfo.getResults();
           
            
            if (results.isValid()) {  
            	
                if (!useRmax || clusterInfo.calculateDistance(chemical) <= results.getRMax()*1.01*RmaxFactor) {//mult by 1.01 to avoid roundoff error for cases where test chemical is in the cluster
//                        System.out.println("Distance="+clusterInfo.calculateDistance(chemical)+"\tRmax="+results.getRMax());
                	if (! useModelEllipsoid || chemical.isWithinEllipsoid(clusterInfo)) {
                        // && chemicalIsWithinExtrapolationRange(clusterInfo, chemical)
                        if (!useFragments || chemicalFragmentsAreInPredictingCluster(chemical,clusterInfo)) {
//                                System.out.println(i+"\t"+results.numChemicals+"\t"+Utils.doubleToString(results.q2,6,4)+"\t"+Utils.doubleToString(results.r2,6,4));
                            //chemical.addManyPredictions(results, npred);
                        	
//                            	System.out.println(results.clusterNumber+"\t"+results.numChemicals+"\t"+chemical.calculateToxicValue(results)+"\t"+chemical.calculateUncertainty(results));
//                            	if (chemical.stringValue(0).equals("66-25-1")) {
//                            		System.out.println(results.clusterNumber+"\t"+chemical.calculateToxicValue(results));
//                            	}
                        	
//                        	chemical.calculateProbability(results, avgValues, SSCP);
                        	
                        	
                        	chemical.getPredictions().add(chemical.calculateToxicValue(results));
                            chemical.getUncertainties().add(chemical.calculateUncertainty(results));
                            chemical.getStderr().add(chemical.calculateStdError(results));
                            chemical.getDistances().add(clusterInfo.getDistances()[i]);
                            chemical.getNumChemicals().add((Integer)results.getNumChemicals());
                            chemical.getClustersUsed().add((Integer)results.getClusterNumber());//TMM

                        } else {
                        
                        	Vector<String>vecMissing=this.missingFragmentsInPredictingCluster(chemical, clusterInfo);

//                            	String message="Fragment constraint not met.<br>" +
//                            			"The following fragments in the test chemical are not present:<br>";
                        	
                        	String message="The following fragments in the test chemical are not present:<br>";
                        	
                        	for (int j=0;j<vecMissing.size();j++) {
                        		message+=vecMissing.get(j);
                        		if (j<vecMissing.size()-1) message+=", ";
                        	}
                         	
                        	chemical.getInvalidErrorMessages().add(message);
                        	chemical.getInvalidClusters().add(results.getClusterNumber());
                        }
                    } else {//ellipsoid constraint failed
                    	 
                    	chemical.getInvalidErrorMessages().add("Model ellipsoid constraint not met");
                    	chemical.getInvalidClusters().add(results.getClusterNumber());
                    	
                    }
                } else { //failed Rmax constraint                    	
                	chemical.getInvalidErrorMessages().add("Rmax constraint not met");
                	chemical.getInvalidClusters().add(results.getClusterNumber());
                }
            } else { //invalid model
            	chemical.getInvalidErrorMessages().add("Model is not statistically valid");
            	chemical.getInvalidClusters().add(results.getClusterNumber());

            }
            
//            Vector msgs=chemical.getInvalidErrorMessages();
//                if (msgs.size()>0) {
//                	String msg=(String)msgs.get(msgs.size()-1);
//                	if (!msg.equals("Model is not statistically valid"))
//                		System.out.println(chemical.stringValue(0)+"\t"+msg);          
//                }
            
            

        }//end loop over test instances
        
        
    }
    
    private Vector <String> missingFragmentsInPredictingCluster(TestChemical chemical, ClusterInfo clusterInfo) {
    	Vector<String>vecMissing=new Vector<String>();
    	
        OptimalResults results = clusterInfo.getResults();
        Instances trainingDataset  = clusterInfo.getTrainingSet();
        int clusterNumber =results.getClusterNumber();

        Vector fragIndex = chemical.getFragments();
        Instance  instance=null;

        for (int i=0; i<fragIndex.size(); i++){
            boolean haveFrag=false;
            int index = (Integer)fragIndex.get(i);
            for (int j=0; j<results.getNumChemicals(); j++){
                instance = trainingDataset.instance(results.getChemicalNames()[j]);
                if (instance.value(index)!=0) {
                    haveFrag=true;
                    break;
                }
            }
            if (!haveFrag) {
//                System.out.println("do not Have frag "+chemical.attribute(index).name()+" in cluster " +clusterNumber);
                vecMissing.add(instance.attribute(index));
            }

        }
        return vecMissing;    
    }

    //used by Rmax method
    public void makePredictions(ClusterInfo clusterInfo, boolean useFragments,boolean useModelEllipsoid) throws Exception {
        TestChemical chemical;
        //int npred = 100;
        
        // make all predictions for test chemicals.
        for (int i=0; i<this.numInstances(); i++) {
            chemical = (TestChemical)this.instance(i);
            OptimalResults results = clusterInfo.getResults();
            double distance = clusterInfo.calculateDistance(chemical);
            
            if (distance <= results.getRMax()) {
                if (!useModelEllipsoid || chemical.isWithinEllipsoid(clusterInfo)) {
                    // && chemicalIsWithinExtrapolationRange(clusterInfo, chemical)
                    if (!useFragments || chemical.areChemicalFragmentsAreInPredictingCluster(clusterInfo)) {
//                        System.out.println(i+"\t"+results.numChemicals+"\t"+Utils.doubleToString(results.q2,6,4)+"\t"+Utils.doubleToString(results.r2,6,4));
                        //chemical.addManyPredictions(results, npred);
                        chemical.getPredictions().add(chemical.calculateToxicValue(results));
                        chemical.getUncertainties().add(chemical.calculateUncertainty(results));
                        chemical.getStderr().add(chemical.calculateStdError(results));
                        chemical.getDistances().add(distance);
                        chemical.getNumChemicals().add(results.getNumChemicals());
                        chemical.getClustersUsed().add(results.getClusterNumber()); //TMM
                    } else { // failed fragment constraint
                    	
                    	Vector<String>vecMissing=chemical.missingFragmentsInPredictingCluster(clusterInfo);
                    	String message="The following fragments in the test chemical are not present:<br>";
                    	
                    	for (int j=0;j<vecMissing.size();j++) {
                    		message+=vecMissing.get(j);
                    		if (j<vecMissing.size()-1) message+=", ";
                    	}
                    	
                    	chemical.getInvalidErrorMessages().add(message);
//                    	chemical.getInvalidErrorMessages().add("Fragment constraint not met");
                    	chemical.getInvalidClusters().add(results.getClusterNumber());
                    	
                    }
                } else {//failed ellipsoid constraint
                	chemical.getInvalidErrorMessages().add("Model ellipsoid constraint not met");
                	chemical.getInvalidClusters().add(results.getClusterNumber());
                }
            } else {//failed Rmax constraint            	            	 
            	chemical.getInvalidErrorMessages().add("Rmax constraint not met");
            	chemical.getInvalidClusters().add(results.getClusterNumber());            	
            }
        }
        
    }
    

    



//    private boolean chemicalIsWithinExtrapolationRange(ClusterInfo clusterInfo, TestChemical chemical) {
//        OptimalResults results = clusterInfo.getResults();
//
//        int csize = results.getNumChemicals();
//        int nparm = (results.descriptors).length;
//        double leverageMax = 2.0*nparm/csize ;
//        double hMax = results.gethMax();       
//        double h00 = 0;
//        try {
//            h00 = chemical.calculateH00(results);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        if (h00 <= leverageMax){ 
//            return true;
//        } else {
//            return false;
//        }
//
//    }
    
//    private boolean modelIsRobust(OptimalResults results) {
//        // TODO Auto-generated method stub
//        double qsq = results.q2;
//        double rsq = results.r2;
//        if ( Math.abs(qsq -rsq) > 0.10) return false;
//        else return true;
//    }
    
//    /**
//     * @return Returns the toxicValues.
//     */
//    public Vector getToxicValues() {
//        return toxicValues;
//    }
//
//    /**
//     * @return Returns the confidenceIntervals.
//     */
//    public Vector getConfidenceIntervals() {
//        return confidenceIntervals;
//    }

    /**
     * @param alpha The alpha to set.
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

}
