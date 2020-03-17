package QSAR.qsarOptimal;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Vector;

import QSAR.validation2.Statistics;
import weka.core.matrix.Matrix;

public class OptimalResults implements Serializable, Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 1052617133439245297L;
    private int clusterNumber = 0;
    private int parent1 = 0;
    private int parent2 = 0;
    private int numChemicals = 0;
    private int numDescriptors = 0;
    private int[] chemicalsUsed = null;
    private String[] chemicalNames = null;
    private String [] fragments=null;//list of fragments appearing in the cluster
    private double[] centroid = null;
    private boolean valid = false;
    private double q2 = 0;
	private double r2 = 0;
    private double Concordance = 0;
    private double Sensitivity = 0;
    private double Specificity = 0;
    private double sigma2 = 0;
    private double alpha = 0.1;//used to calculate prediction uncertainty
    private double[] observed = null;
    private double[] predicted = null;
    private int[] descriptors = null;
    private String[] descriptorNames = null;
    private double[] bcoeff = null;
    private double[] bcoeffSE = null;
    private double[][] x = null;
    private double[][] x2inv = null;
    private long startTime = 0;
    private long endTime = 0;
    private int myid = 0;
    private double rMax = 0;//note this changes if outliers are deleted!
	private double hMax = 0; 

    
    public int getNumDescriptors() {
		return numDescriptors;
	}

	public void setNumDescriptors(int numDescriptors) {
		this.numDescriptors = numDescriptors;
	}

	public double getSensitivity() {
		return Sensitivity;
	}

	public void setSensitivity(double sensitivity) {
		Sensitivity = sensitivity;
	}

	public double getSpecificity() {
		return Specificity;
	}

	public void setSpecificity(double specificity) {
		Specificity = specificity;
	}

	public double[] getPredicted() {
		return predicted;
	}

	public void setPredicted(double[] predicted) {
		this.predicted = predicted;
	}

	public double getrMax() {
		return rMax;
	}

	public void setrMax(double rMax) {
		this.rMax = rMax;
	}

	public double gethMax() {
		return hMax;
	}

	public void sethMax(double hMax) {
		this.hMax = hMax;
	}

    
    public OptimalResults() {

    }

    //calculates resubstitution binary stats (concordance, sensitivity, specificity)
	public void CalculateCancerStats(double cutoff) {
				
		double concordance=0;
		
		int posPredcount=0;
		int negPredcount=0;
		
		double posConcordance=0;
		double negConcordance=0;
		
		if (getObserved()==null) return;
		
		for (int i=0;i<getObserved().length;i++) {
			double exp=getObserved()[i];
			double pred=getPredicted()[i];
			
			String strExp="";
			
//			if (cutoff==0.5) {
//				if (exp>=cutoff) strExp="C";
//				else strExp="NC";
//			} else if (cutoff==30) {
//				if (exp==0) strExp="N/A";
//				else if (exp>=cutoff) strExp="C";
//				else strExp="NC";
//			}

			if (cutoff==30) {
				if (exp==0) strExp="N/A";
				else if (exp>=cutoff) strExp="C";
				else strExp="NC";
			} else {
				if (exp>=cutoff) strExp="C";
				else strExp="NC";
			}

			String strPred="";
			
			if (pred>=cutoff) strPred="C";
			else strPred="NC";
			
//			if (strExp.equals("C"))
//				System.out.println(exp+"\t"+pred+"\t"+strExp+"\t"+strPred);

			
			if (strExp.equals("C")) posPredcount++;
			else if (strExp.equals("NC")) negPredcount++;

			if (strExp.equals(strPred)) {
				concordance++;
				
				if (strExp.equals("C")) posConcordance++;
				else if (strExp.equals("NC")) negConcordance++;
				
			}
			
//			System.out.println(strExp+"\t"+pred);
		}
					
		concordance/=(double)getObserved().length;			
		posConcordance/=(double)posPredcount;
		negConcordance/=(double)negPredcount;
		
		
//		System.out.println("posConcordance="+posConcordance);
		this.setConcordance(concordance);
		this.setSensitivity(posConcordance);
		this.setSpecificity(negConcordance);
		
		if (posPredcount==0) setSensitivity(Double.NaN);
		if (negPredcount==0) setSpecificity(Double.NaN);

	
	}

    
    public void calculatePredictedValues() {

    	if (getBcoeff()==null) return;
	
    	setPredicted(new double [getObserved().length]);
	
//    	System.out.println(clusterNumber);
		for (int i=0;i<getNumChemicals();i++) {
			getPredicted()[i]=getBcoeff()[getBcoeff().length-1];
			for (int j=0;j<getBcoeff().length-1;j++) {
				getPredicted()[i]+=getBcoeff()[j]*getX()[i][j];					
			}
//			System.out.println(i+"\t"+observed[i]+"\t"+predicted[i]);
		}
		
    }

    
    public int[] findOutliers() throws Exception {
        
        if (!this.isValid()) return null;
        
        Vector <Integer> outliersV = new Vector<Integer>();
        Statistics s= new Statistics();
        
        //get the hatmatrix leverage 
        DecimalFormat df = new DecimalFormat("0.000"); 
        int clusterNum, csize,ndiscriptors ;   
        double sigma2 = ( this.getSigma2()); 
        clusterNum = this.getClusterNumber(); 
        //System.out.println("\n clusterNumber = "+clusterNum); 
//      findClusterNumberIndex(clusterNum); 
        
        // nparm = number of descriptors +1 = p 
        int nparm = this.getBcoeff().length; 
        csize = (this.getNumChemicals()) ; 
        double levarageMax = (2.0* nparm)/csize ;// critical leverage value for outlier test 
        double cooksMax = 4.0/(csize - 2.0);// Check this 
        double dffitsMax = 2.0* Math.sqrt(nparm *1.0/csize); 
        double covRatioMax,covRatioMin; 
        covRatioMax = 1.0 + (3.0*nparm) /csize; 
        covRatioMin = 1.0 - (3.0*nparm) /csize; 
        if (csize < 21)dffitsMax = 1.0; 
        //System.out.println("\n csize ="+csize +" nparm ="+nparm+" maximum leverage = "+ df.format(levarageMax) +" Cooks Critical Value "+ df.format(cooksMax)+ " dffits Critical Value "+ df.format(dffitsMax));         
        //System.out.println("\n Covariant Ratio Max = "+covRatioMax+" "+" Covariant Ratio Min = "+covRatioMin); 
        double[] hii = new double[csize]; 
        double[] sres = new double[csize];//Studentised deleted residual 
        double[] res = new double[csize]; 
        double[] sdres =new double[csize];//standardised residuals 
        double[] cooksd = new double[csize]; 
        double[] dffits = new double[csize]; 
        double[] covRatio = new double[csize]; 
        //System.out.println("\n For cluster Number " + clusterNum + " Cluster Size "+ csize + " model parameters "+nparm +" mse "+df.format(sigma2)); 
        
        hii = calculateHii(); 
        res = calculateResidual(); 
        
        //System.out.println("chemical Index \tsdres\tsres\thii\tcooksd\tdeffits\tcovRatio"); 
        
//      Calculate the studentized residual, the residual and the Cook's distance for all cluster elements                                 
        
        for(int i = 0; i< this.getNumChemicals();i++){ 
            
            if (Math.abs(res[i])> 0.000001) { 
                double mse =  sigma2; 
                double fac = 1.0 - hii[i]; 
                sdres[i] = res[i]/Math.sqrt(fac*mse); 
                fac = hii[i]/(fac *fac); 
                
                double s2h = (csize -nparm)*mse*(1-hii[i])-res[i]*res[i]; 
                s2h = s2h/(csize - nparm -1.0);
                sres[i]= res[i]/Math.sqrt(s2h); 
                cooksd[i] =  res[i]*res[i]*fac/(nparm *mse); 
                dffits[i] = sres[i]* Math.sqrt(hii[i]/(1.0 -hii[i])); 
                s2h = s2h / (mse *(1.0 -hii[i])); 
                covRatio[i] = Math.pow( s2h ,nparm ); 
                covRatio[i]= covRatio[i]/(1.0 -hii[i]); 
                //System.out.println("\n Chemical Index = "+ i + "    "+"  "+df.format(sdres[i])+"   "+df.format(sres[i])+"   "+df.format(hii[i])+ "   "+ df.format(cooksd[i])+"   "+ df.format(dffits[i])+"  "+df.format(covRatio[i])); 
            } 
        } 
//      Test for the outliers using residues and leverage if |residuals| >2.0 or leverage > 0.3                 
        //System.out.println("\n testing for outlers  "); 
        //System.out.println("chemical Index \tsdres\tsres\thii\tcooksd\tdeffits\tcovRatio"); 
//        double sresMax =2.5;
        double alp = 1.0 - 0.1/(2*csize);
        double chemnum = csize;
        double descriptnum = nparm;
        double sresMax = s.tstat(alp, chemnum - descriptnum - 1);
        
        for(int j = 0; j< this.getNumChemicals();j++){ 
            int influCnt =0; 
            if (Math.abs(dffits[j]) > dffitsMax){ 
                influCnt++; 
            } 
            if (s.CalcProb(cooksd[j],descriptnum,chemnum-descriptnum)>0.40){ 
                influCnt++; 
            } 
            if ( hii[j] > levarageMax){ 
                influCnt++; 
            } 
            if (Math.abs(covRatio[j]) < covRatioMin){ 
                influCnt++; 
            } 
            if ((influCnt >= 2)&& ( Math.abs(sres[j])> sresMax )){ 
                // flag for outlirs 
                outliersV.addElement(j); 
                //System.out.println("\n Outlier Chemical Index = "+ j + "    "+"  "+df.format(sdres[j])+"   "+df.format(sres[j])+"   "+df.format(hii[j])+ "   "+ df.format(cooksd[j])+"   "+ df.format(dffits[j])+"  "+df.format(covRatio[j])); 
            } 
        } 

        int[] outliers = new int[outliersV.size()]; 
        for (int i=0; i<outliers.length; i++) { 
            outliers[i] = outliersV.elementAt(i); 
        }
        
        return outliers; 
    } 
    
    private double[] calculateHii() throws Exception {
        DecimalFormat df = new DecimalFormat("0.000");
        int numChemicals = this.getNumChemicals();
 
        Matrix matX = new Matrix(this.getX());
        Matrix matX2Inv = ((matX.transpose()).times(matX)).inverse();
        
        Matrix matProduct = matX.times(matX2Inv.times(matX.transpose()));
        //System.out.println("\n Hatmatrix dimensions ="+ matProduct.getColumnDimension()+ "by "+ matProduct.getRowDimension());   
        if (matProduct.getColumnDimension() != numChemicals || matProduct.getRowDimension() != numChemicals) {
            throw new Exception("Something is wrong with the Hat Matrix Calculations in CalculateHii");
        }
        // Calculate the leverage values for the chemicals in the cluster
        double[] hii= new double [numChemicals ];
        for ( int ii= 0; ii < numChemicals ; ii++ ) {
            double htrace = matProduct.get(ii,ii);
            hii[ii]= htrace;
//          System.out.println("\n Hatmatrix trace "+ii+" trace element "+df.format(htrace));
        }
        return hii;
        
    }
    /**
     * @param results
     * @return
     */
    public void regenHMax() {
//        System.out.println("Calculating hMax");
    	
        regenX2inv();

    	Matrix matX = new Matrix(getX());
        Matrix matX2inv = new Matrix(getX2inv());

        // start with first element of the diagonal of the Hat Matrix
        Matrix matV = matX.getMatrix(0,0,0,matX.getColumnDimension()-1);
        double hmax = (matV.times(matX2inv.times(matV.transpose()))).get(0,0);
        
        // continue with the remaining elements of the diagonal of the Hat Matrix
        for (int i=1; i<matX.getRowDimension(); i++) {
            matV = matX.getMatrix(i,i,0,matX.getColumnDimension()-1);
            double hii = (matV.times(matX2inv.times(matV.transpose()))).get(0,0);
            if (hii > hmax) hmax = hii;
        }
        this.sethMax(hmax);
    }
    
    public void regenX2inv() {
    	if (getX2inv() == null) {
			try {
				Matrix matX = new Matrix(getX());
				setX2inv((((matX.transpose()).times(matX)).inverse())
						.getArray());
				
			} catch (Exception e) {
//				e.printStackTrace();
//				if (clusterNumber==11852) {
//					System.out.println(clusterNumber);
//					new Matrix(getX()).print(6,4);
//				}
				
				
			}
		}
    }
    
    
    private double[] calculateResidual() throws Exception {
        //DecimalFormat df = new DecimalFormat("0.000");
        int numChem =  this.getNumChemicals();
        int clusterNum = this.getClusterNumber();
//      ChemicalCluster cluster = (ChemicalCluster)allClusters.get(clusterNum -1);
//      int numChemicalFC = cluster.numInstances();
//      int clusterNumFC = cluster.getClusterNumber();
        double[] residu = null;
        double[] obs = null;
        double[] tox = null;
        
        if (numChem >= 1) {
            obs = this.getObserved();
            tox = new double[numChem];
            residu = new double[numChem];
            for (int i=0; i<numChem; i++){
                tox[i] = this.calculateToxicity(i);
                residu[i] = obs[i] - tox[i];
                //System.out.println("\n obs = "+df.format(obs[i])+"  tox = "+df.format(tox[i])+"  res="+df.format(residu[i]));   
            }
            //fraChart fc = new fraChart(obs,tox);
        }
        return residu;
    }
    
    private double calculateToxicity(int chemIndex) {
        
        double toxicValue = getBcoeff()[getBcoeff().length-1];  // b0 value is held as last coefficient (average over toxic values in cluster)
        for (int j=0; j<getBcoeff().length-1; j++) {
            toxicValue += getBcoeff()[j] * getX()[chemIndex][j];
        }
        
        return toxicValue;
        
    }
    
    /**
     * @return Returns the clusterNumber.
     */
    public int getClusterNumber() {
        return clusterNumber;
    }

    /**
     * @param clusterNumber The clusterNumber to set.
     */
    public void setClusterNumber(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    /**
     * @return Returns the bcoeff.
     */
    public double[] getBcoeff() {
        return bcoeff;
    }

    /**
     * @param bcoeff The bcoeff to set.
     */
    public void setBcoeff(double[] bcoeff) {
        this.bcoeff = bcoeff;
    }

    /**
     * @return Returns the bcoeff Standard Error.
     */
    public double[] getBcoeffSE() {
        return bcoeffSE;
    }

    /**
     * @param bcoeffSE The bcoeff Standard Error to set.
     */
    public void setBcoeffSE(double[] bcoeffSE) {
        this.bcoeffSE = bcoeffSE;
    }

    /**
     * @return Returns the centroid.
     */
    public double[] getCentroid() {
        return this.centroid;
    }

    /**
     * @param centroid The centroid to set.
     */
    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    /**
     * @return Returns the chemicalNames.
     */
    public String[] getChemicalNames() {
        return chemicalNames;
    }

    /**
     * @param chemicalNames The chemicalNames to set.
     */
    public void setChemicalNames(String[] chemicalNames) {
        this.chemicalNames = chemicalNames;
    }

    /**
     * @return Returns the descriptorNames.
     */
    public String[] getDescriptorNames() {
        return descriptorNames;
    }

    /**
     * @param descriptorNames The descriptorNames to set.
     */
    public void setDescriptorNames(String[] descriptorNames) {
        this.descriptorNames = descriptorNames;
    }

    /**
     * @return Returns the descriptors.
     */
    public int[] getDescriptors() {
        return descriptors;
    }

    /**
     * @param descriptors The descriptors to set.
     */
    public void setDescriptors(int[] descriptors) {
        this.descriptors = descriptors;
    }

    /**
     * @return Returns the endTime.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @param endTime The endTime to set.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Added by TMM to make old xml files work:
     * @param endTime The endTime to set.
     * @deprecated
     */
    public void setEndTime(double endTime) {
        this.endTime = (long)endTime;
    }

    /**
     * @return Returns the q2.
     */
    public double getQ2() {
        return q2;
    }

    /**
     * @param q2 The q2 to set.
     */
    public void setQ2(double q2) {
        this.q2 = q2;
    }

    /**
     * @return Returns the r2.
     */
    public double getR2() {
        return r2;
    }

    /**
     * @param r2 The r2 to set.
     */
    public void setR2(double r2) {
        this.r2 = r2;
    }

    /**
     * @return Returns the sigma2.
     */
    public double getSigma2() {
        return sigma2;
    }

    /**
     * @param sigma2 The sigma2 to set.
     */
    public void setSigma2(double sigma2) {
        this.sigma2 = sigma2;
    }

    /**
     * @return Returns the startTime.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Added by TMM to make old xml files work:
     * @param startTime The startTime to set.
     * @deprecated
     */
    public void setStartTime(double startTime) {
        this.startTime = (long)startTime;
    }

    /**
     * @return Returns the valid.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @param valid The valid to set.
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return Returns the x.
     */
    public double[][] getX() {
        return x;
    }

    /**
     * @param x The x to set.
     */
    public void setX(double[][] x) {
        this.x = x;
    }

    /**
     * @return Returns the x2inv.
     */
    public double[][] getX2inv() {
        return x2inv;
    }

    /**
     * @param x2inv The x2inv to set.
     */
    public void setX2inv(double[][] x2inv) {
        this.x2inv = x2inv;
    }

    /**
     * @return Returns the numChemicals.
     */
    public int getNumChemicals() {
        return numChemicals;
    }

    /**
     * @param numChemicals The numChemicals to set.
     */
    public void setNumChemicals(int numChemicals) {
        this.numChemicals = numChemicals;
    }

    /**
     * @return Returns the pClusterNum1.
     */
    public int getParent1() {
        return parent1;
    }

    /**
     * @param clusterNum1 The pClusterNum1 to set.
     */
    public void setParent1(int clusterNum1) {
        parent1 = clusterNum1;
    }

    /**
     * @return Returns the pClusterNum2.
     */
    public int getParent2() {
        return parent2;
    }

    /**
     * @param clusterNum2 The pClusterNum2 to set.
     */
    public void setParent2(int clusterNum2) {
        parent2 = clusterNum2;
    }

    /**
     * @return Returns the myID.
     */
    public int getMyid() {
        return myid;
    }

    /**
     * @param myID The myID to set.
     */
    public void setMyid(int myID) {
        this.myid = myID;
    }

    /**
     * @return Returns the alpha.
     */
    public double getAlpha() {
        return alpha;
    }
    
    /**
     * @param alpha The alpha to set.
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getConcordance() {
		return Concordance;
	}

	public void setConcordance(double concordance) {
		Concordance = concordance;
	}

    /**
     * @return Returns the observed.
     */
    public double[] getObserved() {
        return observed;
    }

    /**
     * @param observed The observed to set.
     */
    public void setObserved(double[] observed) {
        this.observed = observed;
    }

    /**
     * @return the rMax
     */
    public double getRMax() {
        return getrMax();
    }

    /**
     * @param max the rMax to set
     */
    public void setRMax(double max) {
        setrMax(max);
    }

    /**
     * @return the chemicalsUsed
     */
    public int[] getChemicalsUsed() {
        return chemicalsUsed;
    }

    /**
     * @param chemicalsUsed the chemicalsUsed to set
     */
    public void setChemicalsUsed(int[] chemicalsUsed) {
        this.chemicalsUsed = chemicalsUsed;
    }

	public String [] getFragments() {
		return fragments;
	}

	public void setFragments(String [] fragments) {
		this.fragments = fragments;
	}
    
}
