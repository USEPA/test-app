package QSAR.validation2;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import wekalite.Instances;
//import Jama.Matrix;
import QSAR.qsarOptimal.AllResults;
import QSAR.qsarOptimal.OptimalResults;

public class AllClusterInfo {
    private final static Comparator<OptimalResults> CLUSTER_NUMBER_COMPARATOR = 
            new Comparator<OptimalResults>() {
        @Override
        public int compare(OptimalResults o1, OptimalResults o2) {
            return Integer.compare(o1.getClusterNumber(), o2.getClusterNumber());
        }
    };
    
    private Vector <OptimalResults> vResults;
    private double[] scales;
    private double[] offsets;
    private Vector <ClusterInfo> clusterMap;
    
    private Map<Integer, Integer> clusterNumberMap;
    
    private int originalSize;
    int generationNumber;
    private Instances trainingDataset;
//    private int chemicalNameIndex;
    
    private TreeMap<Double, TreeSet<Integer>>[] minDistances;
    

    public AllClusterInfo(AllResults allResults) {
        this.vResults = allResults.getResults();
        this.scales = allResults.getScales();
        this.offsets = allResults.getOffsets();
    }
    
    public void Standardize() {
        
        for (int i=0; i<vResults.size(); i++) {
            OptimalResults results = vResults.get(i);
            if (results.isValid()) {
                standardizeResults(results);
            }
        }

    }

    /**
     * @param results
     */
    private void standardizeResults(OptimalResults results) {
        standardizeCoefficients(results);
        standardizeX(results);
        results.regenX2inv();
    }

    /**
     * @param results
     */
    private void standardizeCoefficients(OptimalResults results) {
        int n, j0;
        int[] descriptors = results.getDescriptors();
        n = descriptors.length;
        for (int j=0; j<n; j++) {
            j0 = descriptors[j];
            results.getBcoeff()[n] += results.getBcoeff()[j] * offsets[j0];
            results.getBcoeff()[j] *= scales[j0];
        }
    }
    
    /**
     * @param results
     */
    private void standardizeX(OptimalResults results) {
        int m, n, j0;
        int[] descriptors = results.getDescriptors();
        double scaleInv, offset;
        double[][] x = results.getX();
        n = x.length;
        m = x[0].length;
        for (int j=0; j<m-1; j++) {
            j0 = descriptors[j];
            offset = offsets[j0];
            if (scales[j0] == 0.0) {
                for (int i=0; i<n; i++) {
                    x[i][j] = (x[i][j] - offset);
                }
            } else {
                scaleInv = 1.0 / scales[j0];
                for (int i=0; i<n; i++) {
                    x[i][j] = (x[i][j] - offset) * scaleInv;
                }
            }

        }
    }
    
  
    
    public void sortResults() {
        Collections.sort(vResults, CLUSTER_NUMBER_COMPARATOR);
        
        /*
        OptimalResults results;
        int minClusterNumber,imin;
        
        // sort all results by clusterNumber
        for (int i=0; i<vResults.size(); i++) {
            results = vResults.get(i);
            imin = i;
            minClusterNumber = results.getClusterNumber();
            // find minimum clusterNumber (and position)
            for (int j=vResults.size()-1; j>i; j--) {
                results = vResults.get(j);
                if (results.getClusterNumber() < minClusterNumber) {
                    imin = j;
                    minClusterNumber = results.getClusterNumber();
                }
            }
            // reverse positions in vResults
            if (i != imin) {
                results = vResults.get(i);
                vResults.set(i,vResults.get(imin));
                vResults.set(imin, results);
            }
        }
        */
        
//        int i = 0;
//        int cn = 1;
//        while (i < vResults.size()) {
//            if (cn < vResults.get(i).clusterNumber) {
//                System.out.println("Cluster #"+cn+" not found");
//            } else {
//                i++;
//            }
//            cn++;
//        }
        
    }
    
    public void mapClusterResults() throws Exception {
        originalSize = (vResults.size()+1)/2;
        clusterMap = new Vector<ClusterInfo>(originalSize);
        clusterNumberMap = new HashMap<Integer, Integer>(originalSize);
        OptimalResults results;
        ClusterInfo clusterInfo;
        
        for (int i=0; i<originalSize; i++) {
            results = vResults.get(i);
            clusterInfo = new ClusterInfo(results);
            clusterInfo.setTrainingSet(trainingDataset);
            clusterInfo.setScales(scales);
//            clusterInfo.recalculateCentroidRMax();//not needed
            clusterMap.add(clusterInfo);
            clusterNumberMap.put(results.getClusterNumber(), clusterMap.size()-1);
        }
        generationNumber = 0;
    }
    
    public void initializeDistances(TestData testData) throws Exception {
        minDistances = new TreeMap[testData.numInstances()];
        
        for (int i = 0; i < testData.numInstances(); i++) {
            minDistances[i] = new TreeMap<>();
        }
        
        // calculate all distances between cluster centroids and test data.
        for (int i=0; i<clusterMap.size(); i++) {
            ClusterInfo clusterInfo = clusterMap.get(i);
            clusterInfo.calculateDistances(testData);
            updateMinDistances(i, clusterInfo.getDistances(), testData);
        }
    } 
    
    public void updateClusterMap(TestData testData) throws Exception {

        generationNumber++;
        OptimalResults results = vResults.get(originalSize+generationNumber-1);
        
        int p1 = results.getParent1();
        int p2 = results.getParent2();
        
        /*
        ClusterInfo clusterInfo = new ClusterInfo(results);
        clusterInfo.setTrainingSet(trainingDataset);
        clusterInfo.setScales(scales);
        // clusterInfo.recalculateCentroidRMax();
        clusterInfo.calculateDistances(testData);
        
        clusterMap.remove(findClusterNumberIndex(p1));
        clusterMap.remove(findClusterNumberIndex(p2));
        clusterMap.add(clusterInfo);
        */
        
        // code below replaces clusters p1 and p2 with a new cluster
        // with following nuances:
        // 1. avoid removing elements in the middle of the list 
        //    to avoid array copy operations
        // 2. track cluster number to cluster index mapping 
        //    to enable O(1) search in findClusterNumberIndex
        // 3. reuse existing ClusterInfo object 
        //    to avoid unnecessary object creation
        
        int p1Index = findClusterNumberIndex(p1);
        int p2Index = findClusterNumberIndex(p2);
        
        if (p1Index == clusterMap.size() - 1) {
            int tmp = p1Index;
            p1Index = p2Index;
            p2Index = tmp;
        }
        
        // remove distances from the heap
        for (int j = 0; j < testData.numInstances(); j++) {
            removeHeapNode(p1Index, j, clusterMap.get(p1Index).getDistances()[j]);
            removeHeapNode(p2Index, j, clusterMap.get(p2Index).getDistances()[j]);
        }

        // remove p2 cluster
        if (p2Index != clusterMap.size() - 1) {
            ClusterInfo lastCluster = clusterMap.get(clusterMap.size() - 1);
            for (int j = 0; j < testData.numInstances(); j++) {
                removeHeapNode(clusterMap.size() - 1, j, lastCluster.getDistances()[j]);
            }
            clusterMap.set(p2Index, lastCluster);
            clusterNumberMap.put(lastCluster.getResults().getClusterNumber(), 
                    p2Index);
            updateMinDistances(p2Index, lastCluster.getDistances(), testData);
        }
        
        clusterNumberMap.remove(p2);
        clusterMap.remove(clusterMap.size() - 1);
        
        
        // replace p1 cluster with a new one
        ClusterInfo p1Cluster = clusterMap.get(p1Index);
        p1Cluster.setResults(results);
        p1Cluster.calculateDistances(testData);
        updateMinDistances(p1Index, p1Cluster.getDistances(), testData);
        clusterNumberMap.remove(p1);
        clusterNumberMap.put(p1Cluster.getResults().getClusterNumber(), p1Index);
    }
    
    private void updateMinDistances(int clusterIndex, double[] distances, 
            TestData testData) {
        for (int j = 0; j < testData.numInstances(); j++) {
            if (minDistances[j].containsKey(distances[j])) {
                minDistances[j].get(distances[j]).add(clusterIndex);
            } else {
                TreeSet<Integer> set = new TreeSet<>();
                set.add(clusterIndex);
                minDistances[j].put(distances[j], set);
            }
        }
    }
    
    private void removeHeapNode(int clusterIndex, int instanceIndex, 
            double distance) {
        TreeSet<Integer> set = minDistances[instanceIndex].get(distance);
        if (set.size() > 1) {
            set.remove(clusterIndex);
        } else {
            minDistances[instanceIndex].remove(distance);
        }
    }
    
    /**
     * 
     * @param clusterIndex masterMap index
     * @param instanceIndex testData instance index
     * @return a mapping key for the heapMap
     */
    private long getHeapNodeKey(int clusterIndex, int instanceIndex) {
        return ((long) Integer.MAX_VALUE) * instanceIndex + clusterIndex;
    }
    
    public int findClusterNumberIndex(int clusterNumber) throws Exception {
        if (clusterNumberMap.containsKey(clusterNumber)) {
            return clusterNumberMap.get(clusterNumber);
        }

        throw new Exception("clusterNumber: "+clusterNumber+" not found");
    }

    
    /**
     * @return Returns the vResults.
     */
    public Vector<OptimalResults> getVResults() {
        return vResults;
    }

    /**
     * @return Returns the clusterMap.
     */
    public Vector<ClusterInfo> getClusterMap() {
        return clusterMap;
    }

    /**
     * @return Returns the scales.
     */
    public double[] getScales() {
        return scales;
    }

    /**
     * @return Returns the offsets.
     */
    public double[] getOffsets() {
        return offsets;
    }

    /**
     * @param trainingDataset the trainingDataset to set
     */
    public void setTrainingDataset(Instances trainingDataset) {
        this.trainingDataset = trainingDataset;
    }

    /**
     * @return the minDistances
     */
    public TreeMap<Double, TreeSet<Integer>>[] getMinDistances() {
        return minDistances;
    }
    
//    /**
//     * @param chemicalNameIndex the chemicalNameIndex to set
//     */
//    public void setChemicalNameIndex(int chemicalNameIndex) {
//        this.chemicalNameIndex = chemicalNameIndex;
//    }    

}
