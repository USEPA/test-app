/**
 * 
 */
package QSAR.wardsMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import weka.core.Instance;
import weka.core.Instances;
/**
 * @author Paul Harten
 *
 */
public class ClusterCollection extends Vector {
    
    private double[] offsets;
    private double[] scales;
    
    public ClusterCollection() {
    }
   
    public ClusterCollection Clone() {
        ChemicalCluster cluster;
        ClusterCollection clusters =  new ClusterCollection();

        for (int i=0; i<this.elementCount; i++) {
            cluster = (ChemicalCluster)this.elementAt(i);
            clusters.add(cluster.Clone());    
        }
        
        clusters.setOffsets(this.offsets);
        clusters.setScales(this.scales);
        
        return clusters;
    }
 
    void Initialize() {
        
    }
    
    //  distribute the chemicals in the input cluster into separate clusters  
    public void Distribute(ChemicalCluster cluster)  {
        ChemicalCluster cluster1;

        for (int i=0; i<cluster.numInstances(); i++) {
            cluster1 = new ChemicalCluster(cluster,i,1);
            cluster1.setVariance(0);   // Wards's method variance is 0 for single element cluster;
            cluster1.setClusterNumber(i); // Assign a cluster number
            this.addElement(cluster1);
        }
            
    }

    public void StandardizeCentroids() {
        ChemicalCluster cluster;
        
        //  standardize the properties by subtacting the mean and then dividing by the standard deviation
        for (int k = 0; k < this.elementCount; k++) {
            cluster = (ChemicalCluster)this.elementAt(k);
            cluster.StandardizeCentroid(offsets,scales);
        }

    }
    
    public void Standardize(double[] offsets, double[] scales) {
        ChemicalCluster cluster;
        
        this.offsets = offsets;
        this.scales = scales;
        
        //  standardize the properties by subtacting the mean and then dividing by the standard deviation
        for (int k = 0; k < this.elementCount; k++) {
            cluster = (ChemicalCluster)this.elementAt(k);
            cluster.Standardize(offsets,scales);
        }

    }
    
    public void UnStandardize() {
        ChemicalCluster cluster;
        
        if (offsets == null || scales == null) return;
        
        //  unstandardize the properties by multiplying by standard deviation and then adding the means
        for (int k = 0; k < this.elementCount; k++) {
            cluster = (ChemicalCluster)this.elementAt(k);
            cluster.UnStandardize(offsets,scales);
        }

    }
    
    void CalculateToxicAverages() {
        ChemicalCluster cluster;

        for (int k = 0; k < this.elementCount; k++) {
            cluster = (ChemicalCluster)this.elementAt(k);
            cluster.CalculateAverageToxicity();
        }
    }
    
    void CalculateToxicUncertainties() {
        ChemicalCluster cluster;

        for (int k = 0; k < this.elementCount; k++) {
            cluster = (ChemicalCluster)this.elementAt(k);
            cluster.CalculateToxicUncertainty();
        }
    }

    void Print() {
        ChemicalCluster cluster;
        System.out.println("number of clusters = "+this.elementCount);

        for (int k = 0; k < this.elementCount; k++) {
            cluster = (ChemicalCluster)this.elementAt(k);
            cluster.Print();
        }
    }
    
    /**
     * @return Returns the m_Means.
     */
    public double[] getOffsets() {
        return offsets;
    }

    /**
     * @param means The m_Means to set.
     */
    public void setOffsets(double[] offsets) {
        this.offsets = offsets;
    }

    /**
     * @return Returns the m_Stddev.
     */
    public double[] getScales() {
        return scales;
    }

    /**
     * @param stddev The m_Stddev to set.
     */
    public void setScales(double[] scales) {
        this.scales = scales;
    }

    /*
    void PrintSampleToxicities()
    {
        ChemicalCluster cluster;
        Iterator iter;

        iter = this.iterator();
        while (iter.hasNext())
        {
            cluster = (ChemicalCluster)iter.next();
            cluster.PrintSampleToxicities();
        }
    }
    */

}
