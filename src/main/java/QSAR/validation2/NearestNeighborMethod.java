package QSAR.validation2;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
//import java.util.ListIterator;
import java.util.Vector;


import wekalite.*;
import ToxPredictor.Utilities.Utilities;


/**
 * Simplified NearestNeighborMethod
 * 
 * This class doesn't depend on WEKA!
 *  
 * @author TMARTI02
 *
 */
public class NearestNeighborMethod{
	
	static String datafolder="ToxPredictor/DescriptorTextTables";
//    public String outputfolder="QSAR/resultsNN";

	public String DescriptorSet="2d";
	String note="";
	
	public String predictionset="prediction";
			
//	boolean writeClusterDataXMLFile=false;
	
//	**********************************************************************
	
	public int minimumClusterSize=3;
	public int maximumClusterSize=1001;
    public int absoluteMinimumClusterSize=3;
    
	public String SimilarityMethod="Cosine";
//  String SimilarityMethod="Tanimoto";
//  String SimilarityMethod="Euclidean";
            
	public boolean Standardize=false;//false is faster and doesnt change Instances
	public boolean NormalizeDuringSCCalc=!Standardize;
   
	public boolean UseWeights=false;
	public double fragweight=1;	
	public double HydrocarbonWeight=fragweight;
	
   String weightfolder="ToxPredictor/DescriptorTextTables/Weighting Files";
   String weightfilename=DescriptorSet+"-"+"100"+".txt";
   
   public static boolean MustExceedSCmin=true;
//   public double SCmin=0.65;
   public double SCmin=0.5;
   
//   public boolean MustHaveTestChemicalFragments=false;
//   public boolean MustNotHaveExtraChemicalFragments=false;
   
	public static boolean ExcludeTestChemicalCASFromTrainingSet = true; //if the CAS matches exclude chemical from training cluster
	public static boolean ExcludeTestChemical2dIsomerFromTrainingSet = false; //if the similarity coefficient >= 0.999 exclude chemical from training cluster

//   public boolean RemoveEStateDescriptorsNotInTestChemical=false;
//   public boolean RemoveAllDescriptorsNotInTestChemical=false;
   
//   public boolean MustMatchBondTypes=false;   
//   public boolean MustHaveSameElements=false;
   public boolean UseWeightedNeighborValues=false;
   public double weightingAlpha=100;
   
//   public String PredictionMethod="QSAR";
//   public String PredictionMethod="Median";
   public String PredictionMethod="Average";
   public String OmitCAS="";
   public String OmitCAS2="";
   
    //**********************************************************************
    
    Instances testSet=null;
    Instances trainingSet=null;    
    
    double [] weights;
    
   private double expToxicValue;
   public Double predToxicValue;
   public double SCAvg;
   
   //**********************************************************************
   
//   public OptimalResults optimalresults; // need to be able to retrieve external if predicting from fraMain
   public Instances cc; // need to be able to retrieve external if predicting from fraMain
   
   public Vector<Double> SimCoeffCluster=new Vector<>();
   
   
 //determines folder which has maximum final folder number
	public static int FindFinalNumber(String endpoint) {
		File folder=new File (datafolder);
		
		File [] files=folder.listFiles();
		
		int max=-1;
		for (int i=0;i<files.length;i++) {
			File file=files[i];
			
			String filename=file.getName().toLowerCase();
			
			if (!file.isDirectory()) continue;
			if (filename.indexOf("data files")==-1) continue;
			if (filename.indexOf("_final")==-1) continue;
			
			String bob=filename.substring(0,filename.indexOf("data files")).trim();
			String currentEndpoint=filename.substring(0,filename.indexOf("_final"));
			
			if (currentEndpoint.equals(endpoint.toLowerCase())) {
				String num=bob.substring(bob.indexOf("_final")+"_final".length(),bob.length());
				
				if (num.indexOf("_")>-1) continue;
				
//				System.out.println(num);
				int inum=Integer.parseInt(num);
				if (inum>max)max=inum;
			}
			
		}
		
		
		return max;
	}

	public void mkdir(String folderPath) {
		File folder=new File(folderPath);
		if (!folder.exists()) folder.mkdir();
	}
	
	/**
	 * This version runs rnd sets for LC50 but doesnt create new instance of main class for each run
	 *
	 */
	void RunRndSets() {
		try {
			
		    int trial=1;
		    
		    this.minimumClusterSize=3;
		    this.absoluteMinimumClusterSize=3;

//		    this.Standardize=true;
		    this.Standardize=false;
		    this.NormalizeDuringSCCalc=!Standardize;
		    
		    String endpoint="LC50";		    
		    String desc="_Final"+NearestNeighborMethod.FindFinalNumber(endpoint);
		    
		    String f1Path="QSAR2";
		    mkdir(f1Path);
		    
		    String f2Path=f1Path+"/resultsNN";
		    mkdir(f2Path);
		    
		    String f3Path=f2Path+"/"+endpoint+desc;
		    mkdir(f3Path);
		    
	    	String outputFolderPath=f3Path+"/trial"+trial;
	    	mkdir(outputFolderPath);
	    	
	    	int NSets=5;
	    	
	    	//Write runinfo file to trial folder:
			this.WriteRunInfoFile(outputFolderPath,endpoint);
	    	
			long t1=System.currentTimeMillis();
			
		    for (int i=1;i<=NSets;i++) {
		    	
		    	DescriptorSet="2d";
		    	
				if (DescriptorSet.equals("2d")) {
					SCmin=0.5;
				} else if (DescriptorSet.equals("FDA_Subset")) {
					SCmin=0.65;	
				}
				
				String testFilePath= datafolder+"/"+endpoint+desc+ " Data Files/"+DescriptorSet+"/"+endpoint+"_prediction_set-"+DescriptorSet+"-rnd"+i+".csv";
				String trainFilePath=datafolder+"/"+endpoint+desc+ " Data Files/"+DescriptorSet+"/"+endpoint+"_training_set-"+DescriptorSet+"-rnd"+i+".csv";
			    
		    	initialize(trainFilePath,testFilePath);
		    	
		    	String runfolder="run"+i;

		    	run(outputFolderPath,runfolder);
		    }
		    
		    GetResultsFromRunFiles3(1,NSets,outputFolderPath);

		    long t2=System.currentTimeMillis();
		    System.out.println("Total runtime="+(t2-t1)/1000+" seconds");
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	}
	
	private void ReadWeights() {
		try {

			File wtFile = new File(this.weightfolder + "/"
					+ this.weightfilename);
			
//			System.out.println(wtFile.exists());
			
			BufferedReader br = new BufferedReader(new FileReader(wtFile));

			weights = new double[trainingSet.numAttributes()];

			int counter = 0;
			while (true) {
				String Line = br.readLine();
				if (Line == null)
					break;

				LinkedList<String> l = Utilities.Parse3(Line, ",");
//				String var = (String) l.get(0);
				String val = l.get(1);
				double weight = Double.parseDouble(val);

				weights[counter + 2] = weight;

				counter++;
			}

			br.close();

			// System.out.println(trainingDataSet.numInstances());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void initialize(String trainFilePath, String testFilePath) {
				
		CSVLoader atf = new CSVLoader();

		try {
			testSet = atf.getDataSetFromFile(testFilePath,",");
			trainingSet = atf.getDataSetFromFile(trainFilePath,",");

			trainingSet.calculateMeans();
			trainingSet.calculateStdDevs();
			
//			for (int i=0;i<trainingSet.numAttributes();i++) {
//				System.out.println(i+"\t"+trainingSet.getMeans()[i]+"\t"+trainingSet.getStdDevs()[i]);
//			}
			
			if (this.UseWeights)
				this.ReadWeights();

			System.out.println("initialize complete");
			
//			trainingSet.printInstances();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initialize(Instances trainingSet) {
		this.trainingSet=trainingSet;
		
		if (Standardize) {
			trainingSet.Standardize();
		}

	}


	/**
	 * This version also extracts MAE
	 * @param Start
	 * @param Stop
	 * @param outputfolder
	 */
	public static void GetResultsFromRunFiles3(int Start, int Stop, String outputfolder) {
		try {

			FileWriter fw = new FileWriter(outputfolder + "/runinfo-" + Start
					+ "-" + Stop + ".txt");

			fw.write("Run#\tQ2ext\tR2abs\tR2\tMAE\tCoverage\tcount\r\n");

			double AvgQ2ext=0;
			double AvgR2abs=0;
			double AvgR2=0;
			double AvgMAE=0;
			double AvgCoverage=0;
			
			double AvgCount=0;
			double runcount=0;
			
			for (int i = Start; i <= Stop; i++) {
				String runfolder = "run" + i;
				String filename = outputfolder + "/" + runfolder + "/"
						+ runfolder + "-fitvalues.txt";
				// System.out.println(filename);
				File file = new File(filename);
				if (!file.exists())
					continue;
				
				
				BufferedReader br = new BufferedReader(new FileReader(filename));

				String header=br.readLine();
				java.util.LinkedList<String> hl=Utilities.Parse(header, "\t");
				
				int colQ2ext=Utilities.GetColumnNumber("CurrentQ2ext", hl);
				int colR2abs=Utilities.GetColumnNumber("CurrentR2abs", hl);
				int colR2=Utilities.GetColumnNumber("CurrentR2", hl);
				int colMAE=Utilities.GetColumnNumber("CurrentMAE", hl);
				int colCoverage=Utilities.GetColumnNumber("Coverage", hl);
								
				if (colQ2ext ==-1 || colR2abs ==-1 || colR2==-1 || colMAE==-1 || colCoverage==-1) continue;
								
				runcount++;
				
				String LastLine = "";

				int count = 0;
				while (true) {
					String Line = br.readLine();
					if (Line == null)
						break;
					count++;
					LastLine = Line;

				}
				// System.out.println(LastLine);

				LinkedList<String> ll = Utilities.Parse(LastLine, "\t");

				double Q2ext=Double.parseDouble(ll.get(colQ2ext));
				double R2abs=Double.parseDouble(ll.get(colR2abs));
				double R2=Double.parseDouble(ll.get(colR2));
				double MAE=Double.parseDouble(ll.get(colMAE));
				double Coverage=Double.parseDouble(ll.get(colCoverage));
				
				
				AvgQ2ext+=Q2ext;
				AvgR2abs+=R2abs;
				AvgR2+=R2;
				AvgMAE+=MAE;
				AvgCoverage+=Coverage;
				
				
				AvgCount+=count;
				
				fw.write(i + "\t" + Q2ext+"\t"+R2abs+"\t"+R2+"\t"+MAE+"\t"+Coverage + "\t"
						+ count+"\r\n");
				fw.flush();
				

				br.close();

			}
			
			AvgQ2ext/=runcount;
			AvgR2abs/=runcount;
			AvgR2/=runcount;
			AvgMAE/=runcount;
			AvgCoverage/=runcount;
			
			AvgCount/=runcount;
			
			DecimalFormat df=new DecimalFormat("0.0000");
			
			fw.write("Avg\t"+df.format(AvgQ2ext)+"\t"+df.format(AvgR2abs)+"\t"+df.format(AvgR2)+"\t"+df.format(AvgMAE)+"\t"+df.format(AvgCoverage)+"\t"+AvgCount+"\r\n");
			
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void WriteRunInfoFile(String outputfolder,String paper) {
		
		try {
			FileWriter fw=new FileWriter(outputfolder+"/runinfo.txt");
			
			fw.write("paper="+paper+"\n");
			fw.write("prediction set="+predictionset+"\n");
			fw.write("DescriptorSet="+DescriptorSet+"\n");
//			fw.write("desc="+desc+"\n");
			fw.write("fragweight="+fragweight+"\n");
			
			fw.write("minimumClusterSize="+minimumClusterSize+"\n");
			fw.write("maximumClusterSize="+maximumClusterSize+"\n");
			fw.write("absoluteMinimumClusterSize="+absoluteMinimumClusterSize+"\n");
			
			fw.write("SimilarityMethod="+SimilarityMethod+"\n");			
			fw.write("HydrocarbonWeight="+this.HydrocarbonWeight+"\n");
			
			fw.write("MustExceedSCmin="+MustExceedSCmin+"\n");
			fw.write("SCmin="+SCmin+"\n");
//			fw.write("MustBeWithinEllipse="+MustBeWithinEllipse+"\n");
			
			fw.write("Standardize="+Standardize+"\n");
			fw.write("NormalizeDuringSCCalc="+this.NormalizeDuringSCCalc+"\n");
//			fw.write("UsePCATransform="+this.UsePCATransform+"\n");
//			fw.write("pcaTolerance="+this.pcaTolerance+"\n");			
//			fw.write("MustHavePredToxInClusterRange="+this.MustHavePredToxInClusterRange+"\n");
//			fw.write("MustHaveTestChemicalFragments="+this.MustHaveTestChemicalFragments+"\n");
//			fw.write("RemoveEStateDescriptorsNotInTestChemical="+this.RemoveEStateDescriptorsNotInTestChemical+"\n");						
//			fw.write("RemoveAllDescriptorsNotInTestChemical="+this.RemoveAllDescriptorsNotInTestChemical+"\n");
//			fw.write("MustMatchBondTypes="+this.MustMatchBondTypes+"\n");
//			fw.write("MustHaveSameElements="+this.MustHaveSameElements+"\n");
			
			fw.write("weightingAlpha="+this.weightingAlpha+"\n");
			fw.write("UseWeightedNeighborValues="+this.UseWeightedNeighborValues+"\n");
			
			
			fw.write("PredictionMethod="+this.PredictionMethod+"\n");

			fw.write("ExcludeTestChemicalCASFromTrainingSet="
					+ ExcludeTestChemicalCASFromTrainingSet + "\n");
			fw.write("ExcludeTestChemical2dIsomerFromTrainingSet="
					+ ExcludeTestChemical2dIsomerFromTrainingSet + "\n");
			
			fw.write("note="+note+"\n");
			
			fw.close();
			
			
			
		} catch (Exception e) {			
			e.printStackTrace();
		}
		 
	}
	
	
	
	
	private void run(String outputFolderPath,String runFolderName) {
		// TODO Auto-generated method stub
		File f=new File(outputFolderPath);
		if (!f.exists()) f.mkdir();
		
		File RunFolder=new File(outputFolderPath+"/"+runFolderName);
		if(!RunFolder.exists()) RunFolder.mkdir();

//		double time1 = System.currentTimeMillis() / 1000.0;	
		double Yexpbar=trainingSet.calculateAverageToxicity();
		
		System.out.println("runfolder="+runFolderName);
		
		
       try {
    	   FileWriter fw=new FileWriter(outputFolderPath+"/"+runFolderName+"/"+runFolderName+"-fitvalues.txt");
    	   FileWriter fw2=new FileWriter(outputFolderPath+"/"+runFolderName+"/"+runFolderName+"-nearestneighbors.txt");
    	   
    	   fw2.write("CAS\t\t");
    	       	   
    	   for (int i=0;i<this.minimumClusterSize;i++ ) {
				fw2.write("CAS"+(i+1));
				fw2.write("\t\t");
    	   }
    	   fw2.write("pred\n");
    	   
    	   fw.write("#\tCAS\texpToxicValue\tpredToxicValue\t#Chemicals\tSCavg\tAbsErr\tCurrentQ2ext\tCurrentR2abs\tCurrentR2\tCurrentMAE\tCoverage\r\n");
    	   fw.flush();
    			   
    	   double [] exp=new double [testSet.numInstances()];
    	   double [] pred=new double [testSet.numInstances()];
    	   
    	   int goodcount=0;
    	   
    	   if (this.Standardize) {				
				trainingSet.Standardize();								
			}
    	   
//    	   for (int i=0;i<trainingSet.numInstances();i++) {
//    		   System.out.println(i+"\t"+trainingSet.getMeans()[i]+"\t"+trainingSet.getStdDevs()[i]);
//    	   }
    	   
    	   java.text.DecimalFormat d=new java.text.DecimalFormat("0.00"); 
    	   
//    	   double CurrentR2=-1;
    	   
//    	   ListIterator<Instance>li=testSet.getInstancesIterator();
    	   
//    	   int ii=0;
    	   
//    	   while (li.hasNext()) {
//				Instance chemical = li.next(); // test chemical
				
    	   for (int i=0;i<testSet.numInstances();i++) {
    		   Instance chemical=testSet.instance(i);
    		   
    		   
    		   
    		   String CAS= chemical.getName();

    		   if (CAS.equals(OmitCAS)) continue;

    		   String msg=this.predictToxicity(chemical, outputFolderPath);

    		   if (this.predToxicValue<-999) cc=null;

    		   fw2.write(CAS+"\t"+d.format(chemical.classValue())+"\t");

    		   if (cc!=null) {
    			   for (int j=0;j<cc.numInstances();j++ ) {
    				   fw2.write(cc.instance(j).getName()+"\t");
    				   fw2.write(d.format(cc.instance(j).classValue()));
    				   fw2.write("\t");
    			   }
    		   } else {

    		   }
    		   fw2.write(d.format(this.predToxicValue)+"\n");

    		   fw2.flush();

    		   if (this.predToxicValue>-999) {
    			   exp[goodcount]=this.expToxicValue;
    			   pred[goodcount]=this.predToxicValue;				
    			   goodcount++;
    		   }

    		   double [] exp2=PredictionStats.ResizeArray(exp, goodcount);
    		   double [] pred2=PredictionStats.ResizeArray(pred, goodcount);				

    		   double Coverage=(double)goodcount/(double)(i+1)*100.0;
    		   double CurrentQ2ext=PredictionStats.CalculateCurrentQ2ext(exp2,pred2,Yexpbar);
    		   double CurrentR2abs=PredictionStats.CalculateCurrentR2abs(exp2,pred2);
    		   double CurrentR2=PredictionStats.CalculateCurrentR2_2(exp2,pred2);
    		   double CurrentMAE=PredictionStats.CalculateCurrentMAE(exp2,pred2);

    		   //				System.out.print("\n"+(ii+1)+"\t"+CAS+"\t"+expToxicValue+"\t"+predToxicValue+"\t"+cc.numInstances()+"\t"+CurrentR2+"\n");
    		   DecimalFormat df=new DecimalFormat("0.000");
    		   fw.write((i+1)+"\t"+CAS+"\t");
    		   fw.write(df.format(expToxicValue)+"\t");											
    		   fw.write(df.format(predToxicValue)+"\t");

    		   if (cc!=null) fw.write(cc.numInstances()+"\t");
    		   else fw.write(0+"\t");

    		   fw.write(df.format(this.SCAvg)+"\t");
    		   fw.write(df.format(Math.abs(expToxicValue-predToxicValue))+"\t");

    		   fw.write(df.format(CurrentQ2ext)+"\t"+df.format(CurrentR2abs)+"\t"+df.format(CurrentR2)+"\t"+df.format(CurrentMAE)+"\t"+df.format(Coverage)+"\r\n");

    		   fw.flush();

    		   // System.out.println(chemical.value(descriptorNums[0]));
//    		   double time2 = System.currentTimeMillis() / 1000.0;
    		   //				System.out.println("current run time = "+(time2-time1)+" secs");

//    		   ii++;
    	   }//end loop over testDataSet

    	   fw.close();
    	   fw2.close();
    	   
//    	   double time2 = System.currentTimeMillis() / 1000.0;
//    	   System.out.println("overall run time = "+(time2-time1)/60.0+" mins");
    	   
    	   
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public String predictToxicity(Instance chemical, String outputfolder) {

		double [] means=trainingSet.getMeans();
		double [] stddevs=trainingSet.getStdDevs();
		
		if (this.UseWeights)
			this.ReadWeights();

		
		 if (this.Standardize) {
			 chemical.Standardize(means,stddevs);
		 }

		this.SimCoeffCluster = new Vector<>();
		this.cc = null;

		this.expToxicValue = chemical.getToxicity();

		int CurrentMaxChemicals = this.minimumClusterSize;

//		boolean ValidPrediction = true;

//		int MaxOverSCmin = this.GetCountExceedingSCmin(chemical);

		cc = this.BuildClusterFromTrainingSet(CurrentMaxChemicals, chemical,
				SCmin);
		
		if (cc==null || cc.numInstances() < this.absoluteMinimumClusterSize) {
			String msg = "Insufficient chemicals exceeding the minimum similarity coefficient were found";
			this.predToxicValue = null;
			return msg;
		}


//		if (NearestNeighborMethod.MustExceedSCmin) {
//			if (cc == null || cc.numInstances() < this.absoluteMinimumClusterSize) {
//				String msg = "Insufficient chemicals exceeding the minimum similarity coefficient were found";
//				this.predToxicValue = null;
//				return msg;
//			}
//		}

		if (this.Standardize) {
			// unstandardize w.r.t. to overall trainingset:
			chemical.UnStandardize(means, stddevs);
			
			trainingSet.UnStandardize();

		}

//		if (!ValidPrediction) {
//			this.predToxicValue = -9999;
//			return "invalid  prediction (minimum cluster size exceeds specified maximum cluster size)";
//		}

		if (this.PredictionMethod.equals("Median")) {
			predToxicValue = this.calculateMedianValue(cc);
		} else if (this.PredictionMethod.equals("Average")) {
			predToxicValue = this.calculateAverageValue(cc);
		}

		return "OK";
	}
	
private Instances BuildClusterFromTrainingSet(int MaxCount,Instance chemical,double SCmin) {
	
	Instances cc = null;

	Hashtable<Double,Instance> ht = new Hashtable<Double,Instance>();//Store instances by similarity
	
	
	
//	System.out.println("here b\t"+chemical.toString());
//	System.out.println("here b\t"+Arrays.asList(trainingSet.getMeans()));
	
//    Gson gson = new GsonBuilder().setPrettyPrinting().create();
//	System.out.println("here b\t"+gson.toJson(Arrays.asList(trainingSet.getStdDevs())));

	
	for (int i = 0; i < trainingSet.numInstances(); i++) {
		Instance chemicali = trainingSet.instance(i);
		String CAS = chemicali.getName();
					
		double SimCoeff=-1;
		
		SimCoeff = this.CalculateCosineCoefficient(chemical,chemicali);	
		
//		SimCoeff = TaskCalculations.CalculateCosineCoefficient(chemical,chemicali,trainingSet.getMeans(),trainingSet.getStdDevs());	
		

//		if(CAS.equals("6850-57-3") || CAS.equals("2859-78-1")) {
//			System.out.println("here b\t"+CAS+"\t"+SimCoeff);			
//		}
//		if(CAS.equals("6850-57-3")) {
//			System.out.println("here b\t"+SimCoeff);			
//		}


		if (NearestNeighborMethod.ExcludeTestChemicalCASFromTrainingSet) {
			String TestCAS=chemical.getName();
			if (CAS.equals(TestCAS)) {
//				if (debug) {
//					System.out.println("Same chemical detected & was excluded from training cluster");
//				}
				continue;
			}
		}

		if (NearestNeighborMethod.ExcludeTestChemical2dIsomerFromTrainingSet) {
			if (SimCoeff > 0.999) {
//				if (debug) {
//					System.out.println("Same chemical detected & was excluded from training cluster");
//				}
				continue;
			}
		}

		if (NearestNeighborMethod.MustExceedSCmin) {
			if (SimCoeff >= SCmin) {
				ht.put(SimCoeff, chemicali);
			}
		} else {
			ht.put(SimCoeff, chemicali);
		}
	}

	Vector<Double> v = new Vector<>(ht.keySet());

	//Sort in descending order (highest similarity first)
	java.util.Collections.sort(v, Collections.reverseOrder());
	
	Enumeration <Double>e = v.elements();

	int counter = 0;
	this.SCAvg=0;
	while (e.hasMoreElements()) {
		counter++;
		double key = e.nextElement();
		Instance instance = (Instance) ht.get(key);
		
		this.SCAvg+=key;

		if (counter == 1) {
			cc = trainingSet.createInstancesFromInstance(instance);
		} else
			cc.addInstance(instance);
		
		this.SimCoeffCluster.add(key);

		if (counter == MaxCount)
			break;
	}
	
	this.SCAvg/=(double)counter;
	
	return cc;

}

	private double calculateAverageValue(Instances cc) {
		if (this.UseWeightedNeighborValues) {
			
			double denominator=0;
			double pred=0;
			for (int i=0;i<cc.numInstances();i++) {				
				double SCi=(Double)this.SimCoeffCluster.get(i);
				denominator+=Math.exp(SCi*this.weightingAlpha);				
			}
			
			for (int i=0;i<cc.numInstances();i++) {				
				
				double SCi=(Double)this.SimCoeffCluster.get(i);
				double numerator=Math.exp(SCi*this.weightingAlpha);
				double weighti=numerator/denominator;
				pred+=weighti*cc.instance(i).getToxicity();
//				System.out.println(weighti);
			}
			
			return pred;
		} else {
			return cc.calculateAverageToxicity();	
		}
	}

	
	private double calculateMedianValue(Instances cc) {
		
		int num=cc.numInstances();
		
//		System.out.println("num ="+num);
		
		
		if (num==1) {
			return cc.instance(0).classValue();
		} else if (num==2) {
			double val1=cc.instance(0).classValue();
			double val2=cc.instance(1).classValue();
			return (val1+val2)/2.0;
		} else if (num>2 && num%2!=0) {
			int middlenum=(int)(num/2.0)+1;
//			System.out.println("middlenum="+middlenum);
			double val1=cc.instance(middlenum).classValue();
			return val1;
			
		} else {
			int middlenum=num/2;
//			System.out.println("middlenum="+middlenum);
			double val1=cc.instance(middlenum).classValue();
			double val2=cc.instance(middlenum+1).classValue();
			return (val1+val2)/2.0;			
		}
		
		
	}

	private double CalculateCosineCoefficient(Instance c1,Instance c2) {
		
		double TC=0;
		
		double SumXY=0;
		double SumX2=0;
		double SumY2=0;
		
//		ListIterator<Double> li1=c1.getDescriptorsIterator();
//		ListIterator<Double> li2=c2.getDescriptorsIterator();

//		long t1=System.currentTimeMillis();
		
		double [] Mean=trainingSet.getMeans();
		double [] StdDev=trainingSet.getStdDevs();

//      int i=0;
//        while (li1.hasNext()) {
		
//			double val1=li1.next();
//			double val2=li2.next();

        for (int i=0;i<c1.numAttributes();i++) {
        	
			double val1=c1.value(i);
			double val2=c2.value(i);
        	
//        	System.out.println(val1+"\t"+val2);
        	
			if (NormalizeDuringSCCalc) {
				if (StdDev[i]>0) {
					val1=(val1-Mean[i])/StdDev[i];
					val2=(val2-Mean[i])/StdDev[i];
				} else {
					val1=val1-Mean[i];
					val2=val2-Mean[i];
				}
			}
			
			if (this.UseWeights)  {
				double weight=weights[i];
				val1*=weight;
				val2*=weight;
			}
			
			SumXY+=val1*val2;
			SumX2+=val1*val1;
			SumY2+=val2*val2;
        	
        }//end loop over descriptors
        
		
		TC=SumXY/Math.sqrt(SumX2*SumY2);
		
//    	long t2=System.currentTimeMillis();
//		System.out.println("here, "+(t2-t1));
		
		return TC;
		
	}
	
	public static void main(String[] args) {
        
		try {
			NearestNeighborMethod n=new NearestNeighborMethod();
			n.RunRndSets();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	    
	}


}

