/**
 * 
 */
package QSAR.validation2;

import java.util.Random;
import java.util.Vector;

import QSAR.qsarOptimal.OptimalResults;
import QSAR.validation2.Statistics;
import ToxPredictor.MyDescriptors.DescriptorData;

import wekalite.*;
import Jama.Matrix;

/**
 * @author Paul Harten
 *
 */
public class TestChemical extends Instance {

    /**
     * 
     */
	
	private Vector <Integer>invalidClusters; //stores vectors of Integers for cluster models which couldnt be used to make a prediction
	private Vector <String>invalidErrorMessages;
	
    private Vector<Integer> clustersUsed;// TMM: vector of Integers for cluster numbers used in the prediction
	private Vector <Double>predictions;
	private Vector <Double>uncertainties;//TMM if predictions vector is used to store predictions from each cluster model, this variable stores the uncertainty for each prediction
    private Vector <Double>stderr;
    private Vector <Double>distances;
    private Vector <Integer>numChemicals;
    private Vector <Integer>fragments;
    private long seed = 123456789L;
    
    private Double predictedValue; // TMM: averaged predicted value
    private Double predictedUncertainty; // TMM: averaged prediction uncertainty
    private Statistics statistics = new Statistics();  
    
    private Random rand = new Random(seed);
    
    public TestChemical(Instance instance) {
        super(instance);
        predictions = new Vector<Double>();
        stderr = new Vector<Double>();
        uncertainties = new Vector<Double>();
        distances = new Vector<Double>();
        numChemicals = new Vector<Integer>();
        fragments = new Vector<Integer>();
        clustersUsed=new Vector<Integer>();
        invalidClusters=new Vector<Integer>();
        invalidErrorMessages=new Vector<String>();
    }
    
    public void findFragments(){
        //This needs to change if change order of fragments in csv file

        fragments = new Vector<Integer>();
        
//        String start="-OH [arsenic attach]";
//        String stop=">C=N[H] [2 Nitrogen attach]";
        
        String start= DescriptorData.strFragments[0]; //String start="As [+5 valence, one double bond]";
        String stop= DescriptorData.strFragments[DescriptorData.strFragments.length-1]; //String stop="-N=S=O";
        
        boolean flag=false;
        for (int i=0;i<this.numAttributes();i++) {
            if (attribute(i).equals(start)) {
                flag=true;
            }
            if (flag) {
                double val=this.value(i);
                val=Math.round(val);
                
                if (Math.abs(val)>0.01) {
                    fragments.add(i);
                }
            }
            if (attribute(i).equals(stop)) {
                break;
            }
        }   
        
//        for(int i=0;i<fragments.size();i++) {
//        	System.out.println(fragments.get(i));
//        }
         return;  
    }
    
    public Vector <String> missingFragmentsInPredictingCluster(ClusterInfo clusterInfo) {
    	Vector<String>vecMissing=new Vector<String>();
    	
        OptimalResults results = clusterInfo.getResults();
        Instances trainingDataset  = clusterInfo.getTrainingSet();

        Vector <Integer>fragIndex = getFragments();
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
    
    public boolean areChemicalFragmentsAreInPredictingCluster(ClusterInfo clusterInfo) {

        OptimalResults results = clusterInfo.getResults();
        Instances trainingSet  = clusterInfo.getTrainingSet();

        Vector <Integer>fragIndex = getFragments();
        Instance  instance;

        for (int i=0; i<fragIndex.size(); i++){
            boolean haveFrag=false;
            int index = (Integer)fragIndex.get(i);
            for (int j=0; j<results.getNumChemicals(); j++){
                instance =trainingSet.instance(results.getChemicalNames()[j]); 
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
    
    
    public boolean isWithinRmax(ClusterInfo clusterInfo,double RmaxFactor) {
    	
    	try {
    		double distance=clusterInfo.calculateDistance(this);
    		OptimalResults results=clusterInfo.getResults();
    		
//    		if (getName().equals("143-50-0")) {
//    			System.out.println(distance+"\t"+results.getrMax());
//    		}
    		
    		if (distance<=results.getRMax()*1.01*RmaxFactor) {
    			return true;
    		} else {
    			return false;
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }
    
    /**
     * @param int chemicalNumber
     * @return boolean 
     */
    public boolean isWithinEllipsoid(ClusterInfo clusterInfo) throws Exception {

        OptimalResults results = clusterInfo.getResults();
        
        double hMax = results.gethMax();       
        double h00 = calculateH00(results);

        
//		if (getName().equals("143-50-0")) {
//			System.out.println(h00+"\t"+hMax);
//		}
//    	System.out.println(getName()+"\t"+h00+"\t"+hMax);
        
//        System.out.println(clusterInfo.getResults().clusterNumber+"\t"+h00+"\t"+hMax);
//        System.out.println("\nhMax="+hMax);
//        System.out.println("h00="+h00);
        
        return h00 <= hMax;    
    }
    
    public void addManyPredictions(OptimalResults results, int npred) throws Exception {

        double mean = calculateToxicValue(results);
        
        double stderr = calculateStdError(results);
        
        for (int i=0; i<npred; i++) {
            predictions.add(rand.nextGaussian()*stderr + mean);
        }
        
    } 
    /**
     * @param results
     * @return double toxicValue 
     */
    public double calculateToxicValue(OptimalResults results) throws Exception {

        double[] coeff = results.getBcoeff();
        int[] descriptors = results.getDescriptors();

        double toxicValue = coeff[coeff.length-1];  // b0 value is held as last coefficient (average over toxic values in cluster)
        for (int j=0; j<descriptors.length; j++) {
            toxicValue += coeff[j] * value(descriptors[j]);
            
//            if (getName().equals("78-79-5")) {
//            	System.out.println(attribute(descriptors[j])+"\t"+coeff[j]+"\t"+value(descriptors[j]));
//            }

        }
        
        
//		if (getName().equals("143-50-0")) {
//			System.out.println(toxicValue);
//		}

//        System.out.println(results.clusterNumber+"\t"+toxicValue);
        
        return toxicValue;        
    } 
    
    /**
     * @param results
     * @return double stderr 
     */
    public double calculateStdError(OptimalResults results) throws Exception {

        double h00 = calculateH00(results);
        
        return Math.sqrt(results.getSigma2()*(1.0+h00));
        
    }
    
    /**
     * @param results
     * @return double uncertainty 
     */
    public double calculateUncertainty(OptimalResults results) throws Exception {

        double h00 = calculateH00(results);
        int dof = results.getNumChemicals() - results.getDescriptors().length - 1;
        double uncertainty = statistics.tstat(1.0-results.getAlpha()/2, dof) * Math.sqrt(results.getSigma2()*(1.0+h00));
        
        return uncertainty;
        
    }
    
    /**
     * @param results
     * @return h00
     * @throws Exception
     */
    public double calculateH00(OptimalResults results) throws Exception {
        int[] descriptors = results.getDescriptors();

        double[][] x0 = new double[descriptors.length+1][1];
        
        for (int i=0; i<descriptors.length; i++) {
            x0[i][0] = value(descriptors[i]);
        }
        x0[descriptors.length][0] = 1.0;
        
        Matrix matX2inv = new Matrix(results.getX2inv());
        Matrix matX0 = new Matrix(x0);
        
        Matrix matProduct = ((matX0.transpose()).times(matX2inv.times(matX0)));  // this should be a scalar
        
        if (matProduct.getColumnDimension() != 1 || matProduct.getRowDimension() != 1) {
            throw new Exception("Something is wrong with the Matrix Product");
        }
        
        double h00 = matProduct.get(0,0);
        
        return h00;     
    }
    
    public Vector <Double>getPredictions() {
        return predictions;
    }
    
    public Vector<Double> getUncertainties() {
        return uncertainties;
    }

    
    public Vector <Double>getStderr() {
        return stderr;
    }
    
    public Vector <Integer>getInvalidClusters() {
    	return this.invalidClusters;
    }
    
    public Vector <String>getInvalidErrorMessages() {
    	return this.invalidErrorMessages;
    }

    
    public Vector<Integer> getClustersUsed() {
    	return clustersUsed;
    }
    public Vector <Double>getDistances() {
        return distances;
    }

    /**
     * @return Returns the infoDensity.
     */
    public Vector <Integer>getNumChemicals() {
        return numChemicals;
    }

    /**
     * @return the fragments
     */
    public Vector<Integer> getFragments() {
        return fragments;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(long seed) {
        this.seed = seed;
        rand = new Random(seed);
    }
/**
 * Sets predicted value for the chemical
 * @param predVal
 */    
    public void setPredictedValue(Double predVal) {
    	this.predictedValue=predVal;
    }
    
    /**
     * Sets predicted uncertainty for the chemical
     * @param predUncertainty
     */
    public void setPredictedUncertainty(Double predUncertainty) {
    	this.predictedUncertainty=predUncertainty;
    }

    /**
     * Gets prediction value
     * @return
     */
    public Double getPredictedValue() {
    	return this.predictedValue;
    }
    
    /**
     * Gets prediction uncertainty
     * @return
     */
    public Double getPredictedUncertainty() {
    	return this.predictedUncertainty;
    }

}
