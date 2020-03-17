/**
 * 
 */
package QSAR.qsarOptimal;

import java.util.Vector;

/**
 * @author PHARTEN
 *
 */
public class AllResults {
    
    private double[] offsets = null;
    private double[] scales = null;
    
    private Vector <OptimalResults> vResults = null;
    
    public AllResults() {
        vResults = new Vector<OptimalResults>();
    }

    /**
     * @return Returns the offset.
     */
    public double[] getOffsets() {
        return offsets;
    }

    /**
     * @return Returns the results.
     */
    public Vector<OptimalResults> getResults() {
        return vResults;
    }

    /**
     * @return Returns the scale.
     */
    public double[] getScales() {
        return scales;
    }
    
    /**
     * @param offset The offset to set.
     */
    public void setOffsets(double[] offsets) {
        this.offsets = offsets;
    }
    
    /**
     * @param scale The scale to set.
     */
    public void setScales(double[] scales) {
        this.scales = scales;
    }

    /**
     * @param results The results to set.
     */
    public void setResults(Vector<OptimalResults> vResults) {
        this.vResults = vResults;
    }

}
