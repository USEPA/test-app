package QSAR.validation2;


import QSAR.qsarOptimal.OptimalResults;


//import weka.core.matrix.Matrix;
import Jama.Matrix;
import wekalite.*;

public class ClusterInfo {
    
    private OptimalResults results;
    private double[] distances = null;
//    private double hMax;
    private Instances trainingSet;
    private double[] scales;
//    private int chemicalNameIndex;
//    ChemicalCluster cluster = null;
    private boolean debug=false;
	private double hMax;
    

    public ClusterInfo(OptimalResults results) throws Exception {
        this.results = results;
        
        if (this.results.isValid()) this.results.setValid(modelRegressionCoefficientsAreStable());
        
        // if results are still valid
        if (this.results.isValid()) {
            if (results.gethMax()==0) {
            	results.regenHMax();
            }
        }
    }

    
//    /**
//		Not needed because centroid is calculated during loading of xml file
//      and Rmax is stored in the xml file
//     * @param results
//     */
//    public void recalculateCentroidRMax() {
//        if (!results.valid) return;
//        Instances data = new Instances(trainingDataset,0);
//        for (int i=0; i<results.numChemicals; i++) {
//            try {
//            	data.add(findInstance(results.chemicalNames[i]));
//            } catch (Exception e) {
//            	if (debug)System.out.println(results.chemicalNames[i]);
//            }
//        }
//        cluster = new ChemicalCluster(data);
//        cluster.CalculateCentroid();
//        cluster.CalculateRMax(scales);
//        results.centroid = cluster.getCentroid();
//        results.rMax = cluster.getRMax();
//    }
    
    public boolean modelRegressionCoefficientsAreStable(){

        boolean stable = true;
        for (int i=0;i<results.getBcoeff().length; i++){            
            if (Math.abs(results.getBcoeff()[i]) <= results.getBcoeffSE()[i]){
              stable = false;
              break;
            }
        }
        if (!stable) {
            if (debug) System.out.println("For cluster number: "+results.getClusterNumber());
            for (int i=0;i<results.getBcoeff().length; i++){
                if (i==results.getBcoeff().length-1) {
                	if (debug) System.out.println("bcoeff["+i+"] = "+results.getBcoeff()[i]+" +/- "+results.getBcoeffSE()[i]);
                } else {
                	if (debug) System.out.println("bcoeff["+i+"] = "+results.getBcoeff()[i]+" +/- "+results.getBcoeffSE()[i]+", descriptor "+results.getDescriptors()[i]);
                }
            }
        }
        return stable;
        
    }


    public void calculateDistances(TestData testData) throws Exception {
        Instance chemical;
        distances = new double[testData.numInstances()];
        for (int i=0; i<distances.length; i++) {
            chemical = testData.instance(i);
            distances[i] = calculateDistance(chemical);
//            if (i%100==0) System.out.println("\t"+i);
        }
    }
    
    public double calculateDistance(Instance chemical) throws Exception {
         
        double[] centroid = null;
        double diff;
//        int classIndex = chemical.classIndex();
        
        // old results files have null centroids
        if (results.getCentroid()!=null) {
            centroid = results.getCentroid();
        } else {
            if (debug) System.out.println("Centroid not found");
            return Double.MAX_VALUE;
        }
        
        double sum = 0;
        for (int i=0; i<centroid.length; i++) {
        	if (scales[i] == 0.0) {
        		diff = 0.0; // when the scaling is 0.0 in the training set, descriptor is not used.
        	} else {
        		//diff = (centroid[i]-chemical.value(i))/scales[i];
        		diff = (centroid[i]-chemical.value(i)) / scales[i];
        	}
        	sum += diff*diff;
        }
        
        return Math.sqrt(sum);
    }
    
    /**
     * @param results
     */
    public void recalculateCentroidRMax() {
        if (!results.isValid()) return;
        Instances data = null;
        
        for (int i=0; i<results.getNumChemicals(); i++) {
        	String name=results.getChemicalNames()[i];
        	Instance instance=trainingSet.instance(name);
        	
        	if (i==0) {
        		data=new Instances(instance);
        	} else {
        		data.addInstance(instance);	
        	}
        }
        
        data.calculateMeans();
        data.calculateStdDevs();
        
        results.setRMax(data.CalculateRMax());
        results.setCentroid(data.getMeans());
        
    }
    
    public void recalculatehMax() {
    	Matrix matX = new Matrix(results.getX());
        Matrix matX2inv = new Matrix(results.getX2inv());

        // start with first element of the diagonal of the Hat Matrix
        Matrix matV = matX.getMatrix(0,0,0,matX.getColumnDimension()-1);
        double hmax = (matV.times(matX2inv.times(matV.transpose()))).get(0,0);
        
        // continue with the remaining elements of the diagonal of the Hat Matrix
        for (int i=1; i<matX.getRowDimension(); i++) {
            matV = matX.getMatrix(i,i,0,matX.getColumnDimension()-1);
            double hii = (matV.times(matX2inv.times(matV.transpose()))).get(0,0);
            if (hii > hmax) hmax = hii;
        }
        this.hMax=hmax;
    	
    	
    }
    
    /**
     * @return Returns the results.
     */
    public OptimalResults getResults() {
        return results;
    }

    /**
     * @return Returns the distances.
     */
    public double[] getDistances() {
        return distances;
    }

    /**
     * @return Returns the hMax.
     */
//    public double getHMax() {
//        return hMax;
//    }

    /**
     * @return the trainingDataset
     */
    public Instances getTrainingSet() {
        return trainingSet;
    }

    /**
     * @param trainingDataset the trainingDataset to set
     */
    public void setTrainingSet(Instances trainingDataset) {
        this.trainingSet = trainingDataset;
    }

    /**
     * @param scales the scales to set
     */
    public void setScales(double[] scales) {
        this.scales = scales;
    }


    /**
     * @param results the results to set
     */
    public void setResults(OptimalResults results) {
        this.results = results;
        
        if (this.results.isValid()) this.results.setValid(modelRegressionCoefficientsAreStable());
        
        // if results are still valid
        if (this.results.isValid()) {
            if (results.gethMax()==0) {
                results.regenHMax();
            }
        }
    }


    
}
