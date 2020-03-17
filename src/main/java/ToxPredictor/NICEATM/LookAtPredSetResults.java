package ToxPredictor.NICEATM;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;




public class LookAtPredSetResults {

	
	static DecimalFormat df=new DecimalFormat("0.0000");
	
	
	int findFieldNum(String [] hvals,String colName) {
		
		for (int i=0;i<hvals.length;i++) {
			
			if (hvals[i].equals(colName)) return i;
		}
		
		return -1;
		
	}
	
	Hashtable<String,String>getPreds(String filepath,String casName,String predColName) {
		
		Hashtable<String,String>ht=new Hashtable<String,String>();
		
		
		try {
			
			String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\final analysis";
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String header=br.readLine();
			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, casName);
			int colPred=this.findFieldNum(hvals, predColName);
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
//				System.out.println(Line);
				
//				String [] vals=Line.split("\t");
//				String CAS=vals[colCAS];
//				String pred=vals[colPred];
				
				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				String CAS=list.get(colCAS);
				String pred=list.get(colPred);
				
				
				ht.put(CAS, pred);
				
//				System.out.println(CAS+"\t"+pred);
				
			}
			
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		return ht;
		
	}
	
	public static String calcBinaryStats(ArrayList<String> exp, ArrayList<String> pred) {
		
		int countPos=0;
		int countNeg=0;
		
		
		double sens=0;
		double spec=0;
		
		for (int i=0;i<exp.size();i++) {
			
			if (exp.get(i).equals("TRUE")) {
				countPos++;
				if (exp.get(i).equals(pred.get(i))) {
					sens++;
				}
				
			}
			if (exp.get(i).equals("FALSE")) {
				countNeg++;
				
				if (exp.get(i).equals(pred.get(i))) {
					spec++;
				}

			}
			
			
		}
		
		sens/=(double)countPos;
		spec/=(double)countNeg;
		double ba=(sens+spec)/2.0;
		
		System.out.println(ba+"\t"+spec+"\t"+sens);
		
		return "";
		
	}
	
//	public static String calcCategoryStats(ArrayList<String> exp, ArrayList<String> pred) {
//		
//		int [] correct=new int[6];
//		int [] count=new int[6];
//		
//		for (int i=0;i<exp.size();i++) {
//			int expi=Integer.parseInt(exp.get(i));
//			int predi=Integer.parseInt(pred.get(i));
//			count[expi]++;
//			if (expi==predi) correct[expi]++;
//		}
//
//		double ba=0;
//		int numScores=0;
//		
//		for (int i=1;i<=5;i++) {
//			if (count[i]==0) continue;
//			numScores++;
//			double acc=(double)correct[i]/count[i];
//			System.out.println(i+"\t"+df.format(acc));
//			ba+=acc;
//		}
//		ba/=numScores;
//		
//		System.out.println(df.format(ba));
//		return df.format(ba);
//		
//	}
	
	public static String calcCategoryStats(ArrayList<Integer> exp, ArrayList<Integer> pred) {
		
		int [] correct=new int[6];
		int [] count=new int[6];
		
		for (int i=0;i<exp.size();i++) {
			int expi=exp.get(i);
			int predi=pred.get(i);
			count[expi]++;
			if (expi==predi) correct[expi]++;
		}

		double ba=0;
		int numScores=0;
		
		for (int i=1;i<=5;i++) {
			if (count[i]==0) continue;
			numScores++;
			double acc=(double)correct[i]/count[i];
//			System.out.println(i+"\t"+df.format(acc)+"\t"+correct[i]+"\t"+count[i]);
			ba+=acc;
		}
		ba/=numScores;
		
		System.out.println(df.format(ba));
		return df.format(ba);
		
	}
	public static double calcR2(double[] exp, double[] pred) {

//		for (int i=0;i<exp.length;i++) {
//			System.out.println(i+"\t"+exp[i]+"\t"+pred[i]+"\t"+bs.get(i));
//		}
		
		double[] X = exp;
		double[] Y = pred;

		double MeanX = 0;
		double MeanY = 0;

		for (int i = 0; i < X.length; i++) {
			
			MeanX += X[i];
			MeanY += Y[i];
			
		}
		// System.out.println("");

		MeanX /= X.length;
		MeanY /= X.length;

		// double Yexpbar=this.ccTraining.meanOrMode(this.classIndex);

		// System.out.println("Yexpbar = "+Yexpbar);

		double termXY = 0;
		double termXX = 0;
		double termYY = 0;

		double R2 = 0;

		for (int i = 0; i < X.length; i++) {
			termXY += (X[i] - MeanX) * (Y[i] - MeanY);
			termXX += (X[i] - MeanX) * (X[i] - MeanX);
			termYY += (Y[i] - MeanY) * (Y[i] - MeanY);
		}

		R2 = termXY * termXY / (termXX * termYY);

		return R2;

	}

	public static double calcR2_2(double[] exp, double[] pred) {

		double[] X = exp;
		double[] Y = pred;

		double MeanX = 0;

		for (int i = 0; i < X.length; i++) {
			MeanX += X[i];
		}

		MeanX /= X.length;

		double term1 = 0;
		double term2 = 0;
		for (int i = 0; i < X.length; i++) {
			term1 += Math.pow((X[i] - Y[i]),2);
			term2 += Math.pow((X[i] - MeanX),2);
		}
		double R2 = 1-term1/term2;
		return R2;

	}
	
void goThroughPredSetLogMg_Kg() {
		
		try {
			
			String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\final analysis";

			String fileNamePred="pred set.txt";
			String fileNameQSAR="hc.txt";
//			String fileNameQSAR="nn.txt";
			
			boolean useAD=true;
			
			String fieldCAS="CASRN";
			String fieldExp="LD50_mgkg";
			String fieldPred="LD50 mg/kg";
			String fieldAD="Inside AD?";
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"\\"+fileNamePred));
			
			Hashtable<String,String>htPreds=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldPred);
			Hashtable<String,String>htAD=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldAD);
			
			String header=br.readLine();
			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, fieldCAS);
			int colExp=this.findFieldNum(hvals, fieldExp);
			
			System.out.println(colExp);
			
			int count=0;
			
			ArrayList<Double>expVals=new ArrayList<Double>();
			ArrayList<Double>predVals=new ArrayList<Double>();
			
			
			int expCount=0;
			int predCount=0;

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				String [] vals=Line.split("\t");
				
				String CAS=vals[colCAS];
				String exp=vals[colExp];
				
				if (exp.equals("")) continue;

				if (htPreds.get(CAS)==null) continue;
					
				
				String pred=htPreds.get(CAS);
				String AD=htAD.get(CAS);


				expCount++;

				if (useAD) {
					if (AD.equals("No")) continue;
				}
				
				if (pred.equals("-9999")) continue;
				
				predCount++;

				double dexp=Double.parseDouble(exp);
				double dpred=Double.parseDouble(pred);
				
				expVals.add(dexp);
				predVals.add(dpred);
				
				count++;
				
				System.out.println(count+"\t"+CAS+"\t"+exp+"\t"+pred+"\t"+AD);
				
			}
			double [] dexpVals=new double [expVals.size()];
			double [] dpredVals=new double [expVals.size()];
			
			for (int i=0;i<expVals.size();i++) {
				dexpVals[i]=Math.log10(expVals.get(i));
				dpredVals[i]=Math.log10(predVals.get(i));
			}
			double r2=this.calcR2(dexpVals, dpredVals);
			double r2_2=this.calcR2_2(dexpVals, dpredVals);
			
			double coverage=(double)predCount/expCount;
			
			System.out.println(df.format(r2)+"\t"+df.format(r2_2)+"\t"+df.format(coverage));
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}

		
		
	}

void goThroughPredSetNegLogMol_Kg() {
	
	try {
		
		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\final analysis";

		String fileNamePred="pred set.txt";
		String fileNameQSAR="hc.txt";
//		String fileNameQSAR="nn.txt";
		
		boolean useAD=true;
		
		String fieldCAS="CASRN";
		String fieldExp="-LOG(LD50 mol/kg)";
		String fieldPred="-LOG(LD50 mol/kg)";
		String fieldAD="Inside AD?";
		
		BufferedReader br=new BufferedReader(new FileReader(folder+"\\"+fileNamePred));
		
		Hashtable<String,String>htPreds=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldPred);
		Hashtable<String,String>htAD=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldAD);
		
		String header=br.readLine();
		
		String [] hvals=header.split("\t");
		
		int colCAS=this.findFieldNum(hvals, fieldCAS);
		int colExp=this.findFieldNum(hvals, fieldExp);
		
//		System.out.println(colExp);
		
		int count=0;
		
		ArrayList<Double>expVals=new ArrayList<Double>();
		ArrayList<Double>predVals=new ArrayList<Double>();
		
		int expCount=0;
		int predCount=0;
		
		while (true) {
			String Line=br.readLine();
			if (Line==null) break;
			
			String [] vals=Line.split("\t");
			
			String CAS=vals[colCAS];
			String exp=vals[colExp];
			
			if (exp.equals("")) continue;

			if (htPreds.get(CAS)==null) continue;
				
			
			String pred=htPreds.get(CAS);
			String AD=htAD.get(CAS);

			
			expCount++;

			
			if (useAD) {
				if (AD.equals("No")) continue;
			}
			
			
			if (pred.equals("-9999")) continue;
			
			predCount++;
			
			double dexp=Double.parseDouble(exp);
			double dpred=Double.parseDouble(pred);
			
			expVals.add(dexp);
			predVals.add(dpred);
			
			count++;
			
			System.out.println(count+"\t"+CAS+"\t"+exp+"\t"+pred+"\t"+AD);
			
		}
		double [] dexpVals=new double [expVals.size()];
		double [] dpredVals=new double [expVals.size()];
		
		for (int i=0;i<expVals.size();i++) {
			dexpVals[i]=expVals.get(i);
			dpredVals[i]=predVals.get(i);
		}
		double r2=this.calcR2(dexpVals, dpredVals);
		double r2_2=this.calcR2_2(dexpVals, dpredVals);
		
		double coverage=(double)predCount/expCount;
		
		System.out.println(df.format(r2)+"\t"+df.format(r2_2)+"\t"+df.format(coverage));
		
	
	} catch (Exception ex) {
		ex.printStackTrace();
		
	}

	
	
}

	
	
	void goThroughPredSetBinary() {
		
		try {
			
			String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\final analysis";

			String fileNamePred="pred set.txt";
			
			String fileNameQSAR="hc.txt";
//			String fileNameQSAR="nn.txt";
			
//			String fieldExp="very_toxic";
			String fieldExp="non_toxic";
			
			String fieldPred=fieldExp;
			String fieldAD="Inside AD?";
						
			boolean useAD=true;
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"\\"+fileNamePred));
			
			Hashtable<String,String>htPreds=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldPred);
			Hashtable<String,String>htAD=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldAD);
			
			String header=br.readLine();
			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CASRN");
			int colExp=this.findFieldNum(hvals, fieldExp);
			
			int countPred=0;
			int countExp=0;
			
			ArrayList<String>expVals=new ArrayList<String>();
			ArrayList<String>predVals=new ArrayList<String>();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
//				System.out.println(Line);
				
				String [] vals=Line.split("\t");
				
				String CAS=vals[colCAS];
				String exp=vals[colExp];
				
				if (exp.equals("")) continue;

				if (htPreds.get(CAS)==null) continue;
				
				countExp++;
				
				String pred=htPreds.get(CAS);
				String AD=htAD.get(CAS);

//				System.out.println(pred);
				
//				System.out.println("CAS="+CAS);
				
				
				if (useAD) {
					if (AD.equals("No")) continue;
				}
				
				if (pred.equals("-9999")) continue;
				
				if (pred.equals("")) {
					System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+pred+"\t"+AD);
					continue;
				}
				
				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+pred+"\t"+AD);
				
				expVals.add(exp);
				predVals.add(pred);
				
				countPred++;
				
			}
			
			this.calcBinaryStats(expVals, predVals);
			
			System.out.println(countPred+"\t"+countExp);
			
			double coverage=(double)countPred/countExp;
			System.out.println(coverage);
		
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}

	}
	
	
	/**
	 * Use binary dependent variable to predict binary for external set
	 */

	void predVeryToxicBinary_input() {

//		String filepathBinary="z build models\\NICEATM\\very_toxic\\HC external set\\Hierarchical external set.txt";
		String filepathBinary="z build models\\NICEATM\\very_toxic\\NN external set\\NN external set-fitvalues.txt";
		
		
		try {
			
			BufferedReader brBinary=new BufferedReader(new FileReader(filepathBinary));
			
			String headerBinary=brBinary.readLine();

			Hashtable<String,String>htMW=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN", "MW");
			Hashtable<String,String>htExp=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","very_toxic");

			
			String [] hvals=headerBinary.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CAS");
			
			int colPred=this.findFieldNum(hvals, "predToxicValue");

			int countPred=0;
			int countExp=0;
			
			ArrayList<String>expVals=new ArrayList<String>();
			ArrayList<String>predValsBinary=new ArrayList<String>();

			while (true) {
				
				String lineBinary=brBinary.readLine();
				
				if (lineBinary==null) break;
				
				
				String [] valsBinary=lineBinary.split("\t");
				
				String CAS=valsBinary[colCAS];
				
				String predBinary=valsBinary[colPred];

				countExp++;
				
				double dpredBinary=Double.parseDouble(predBinary);
				
				if (dpredBinary==-9999) continue;
				
				countPred++;
				
				
				String exp=htExp.get(CAS);
				
				expVals.add(exp);
						
				String predBinaryAsBoolean=null;
				
				if (dpredBinary<0.5) predBinaryAsBoolean="FALSE";
				else predBinaryAsBoolean="TRUE";
				

				predValsBinary.add(predBinaryAsBoolean);
				
//				System.out.println(dpredContinuous+"\t"+MW);
//				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+predBinaryAsBoolean+"\t"+predBinary);
				
				
			}
			
			double coverage=(double)countPred/countExp;
			
			this.calcBinaryStats(expVals, predValsBinary);
			System.out.println(coverage);
//			System.out.println(countPred+"\t"+countExp);
			System.out.println("");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	
	void predGHS_Category_CatInput() {
		
//		String filepathBinary="z build models\\NICEATM\\GHS_Category\\NN external set\\NN external set-fitvalues.txt";
		String filepathBinary="z build models\\NICEATM\\GHS_Category\\HC external set\\HC external set-fitvalues.txt";
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepathBinary));
			
			String header=br.readLine();

			Hashtable<String,String>htExp=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","GHS_category");

			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CAS");
			
			int colPred=this.findFieldNum(hvals, "predToxicValue");

			int countPred=0;
			int countExp=0;
			
			ArrayList<Integer>expVals=new ArrayList<Integer>();
			ArrayList<Integer>predVals=new ArrayList<Integer>();

			while (true) {
				
				String line=br.readLine();
				
				if (line==null) break;
				
				
				String [] vals=line.split("\t");
				
				String CAS=vals[colCAS];
				String exp=htExp.get(CAS);
				String pred=vals[colPred];

				if (exp.equals("")) continue;
				countExp++;
				
				int ipred=(int)Double.parseDouble(pred);
				if (ipred==-9999) continue;
				countPred++;
				
				
//				System.out.println(CAS);
				expVals.add(Integer.parseInt(exp));
				predVals.add(new Integer(ipred));
				
//				System.out.println(dpredContinuous+"\t"+MW);
				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+ipred);
				
			}
			
			double coverage=(double)countPred/countExp;
			
			this.calcCategoryStats(expVals, predVals);
			System.out.println(coverage);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	void predGHS_Category_ChemicalsInCommon() {
		
//		String filepathBinary="z build models\\NICEATM\\GHS_Category\\NN external set\\NN external set-fitvalues.txt";
//		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\NN external set\\NN external set-fitvalues.txt";

		String filepathBinary="z build models\\NICEATM\\GHS_Category\\HC external set\\HC external set-fitvalues.txt";
		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\HC external set\\HC external set-fitvalues.txt";

		try {
			
			BufferedReader brBinary=new BufferedReader(new FileReader(filepathBinary));
			BufferedReader brContinuous=new BufferedReader(new FileReader(filepathContinuous));
			
			String headerBinary=brBinary.readLine();
			String headerContinuous=brContinuous.readLine();

			Hashtable<String,String>htExp=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","GHS_category");
			Hashtable<String,String>htMW=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","MW");

			
			String [] hvals=headerBinary.split("\t");
			int colCAS=this.findFieldNum(hvals, "CAS");
			int colPred=this.findFieldNum(hvals, "predToxicValue");

			int countPred=0;
			int countExp=0;
			
			ArrayList<Integer>expVals=new ArrayList<Integer>();
			ArrayList<Integer>predValsBinary=new ArrayList<Integer>();
			ArrayList<Integer>predValsContinuous=new ArrayList<Integer>();

			while (true) {
				
				String lineBinary=brBinary.readLine();
				
				if (lineBinary==null) break;
				
				String lineContinuous=brContinuous.readLine();

				String [] valsBinary=lineBinary.split("\t");
				String [] valsContinuous=lineContinuous.split("\t");
				
				String CAS=valsBinary[colCAS];
				String exp=htExp.get(CAS);
				String predBinary=valsBinary[colPred];
				String predContinuous=valsContinuous[colPred];

				if (exp.equals("")) continue;
				countExp++;
				
				int ipredBinary=(int)Double.parseDouble(predBinary);
				double dpredContinuous=Double.parseDouble(predContinuous);

				if (ipredBinary==-9999) continue;
				if (dpredContinuous==-9999) continue;
				
				countPred++;
				
				double MW=Double.parseDouble(htMW.get(CAS));
				double pred_mg_kg=Math.pow(10,-dpredContinuous)*MW*1000;
				int ipredContinuous=-1;
				if (pred_mg_kg<=5) {
					ipredContinuous=1;
				} else if (pred_mg_kg<=50) {
					ipredContinuous=2;
				} else if (pred_mg_kg<=300) {
					ipredContinuous=3;
				} else if (pred_mg_kg<=2000) {
					ipredContinuous=4;
				} else {
					ipredContinuous=5;
				}

				expVals.add(Integer.parseInt(exp));
				predValsBinary.add(new Integer(ipredBinary));
				predValsContinuous.add(new Integer(ipredContinuous));

				
//				System.out.println(dpredContinuous+"\t"+MW);
				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+ipredBinary+"\t"+ipredContinuous);
				
			}
			
			double coverage=(double)countPred/countExp;
			
			this.calcCategoryStats(expVals, predValsBinary);
			this.calcCategoryStats(expVals, predValsContinuous);
			System.out.println(coverage);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
void predGHS_Category_LD50_mg_kg_Input() {
		
		
		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\NN external set\\NN external set-fitvalues.txt";
//		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\HC external set\\Hierarchical external set.txt";
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepathContinuous));
			
			String header=br.readLine();

			Hashtable<String,String>htExp=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","GHS_category");
			Hashtable<String,String>htMW=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","MW");

			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CAS");
			
			int colPred=this.findFieldNum(hvals, "predToxicValue");

			int countPred=0;
			int countExp=0;
			
			ArrayList<Integer>expVals=new ArrayList<Integer>();
			ArrayList<Integer>predVals=new ArrayList<Integer>();

			while (true) {
				
				String line=br.readLine();
				
				if (line==null) break;
				
				
				String [] vals=line.split("\t");
				
				String CAS=vals[colCAS];
				String exp=htExp.get(CAS);
				String pred=vals[colPred];

				if (exp.equals("")) continue;
				countExp++;
				
				
				double dpred=Double.parseDouble(pred);
				
				if (dpred==-9999) continue;
				countPred++;
				
				double MW=Double.parseDouble(htMW.get(CAS));
				double pred_mg_kg=Math.pow(10,-dpred)*MW*1000;

				
//				System.out.println(CAS);
				expVals.add(Integer.parseInt(exp));
				
				int ipred=-1;
				
				if (pred_mg_kg<=5) {
					ipred=1;
				} else if (pred_mg_kg<=50) {
					ipred=2;
				} else if (pred_mg_kg<=300) {
					ipred=3;
				} else if (pred_mg_kg<=2000) {
					ipred=4;
				} else {
					ipred=5;
				}
					
				predVals.add(new Integer(ipred));
				
//				System.out.println(dpredContinuous+"\t"+MW);
				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+ipred);
				
			}
			
			double coverage=(double)countPred/countExp;
			
			this.calcCategoryStats(expVals, predVals);
			System.out.println(coverage);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}


	
	void compareExternalPredictionsLD50vsBinaryTraining() {
		
		
//		String filepathBinary="z build models\\NICEATM\\very_toxic\\NN external set\\NN external set-fitvalues.txt";
//		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\NN external set\\NN external set-fitvalues.txt";
		
		String filepathBinary="z build models\\NICEATM\\very_toxic\\HC external set\\Hierarchical external set.txt";
		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\HC external set\\Hierarchical external set.txt";
		
		try {
			
			BufferedReader brBinary=new BufferedReader(new FileReader(filepathBinary));
			BufferedReader brContinuous=new BufferedReader(new FileReader(filepathContinuous));
			
			String headerBinary=brBinary.readLine();
			String header2=brContinuous.readLine();

			
			Hashtable<String,String>htMW=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN", "MW");
			Hashtable<String,String>htExp=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","very_toxic");

			
			String [] hvals=headerBinary.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CAS");
			
			int colPred=this.findFieldNum(hvals, "predToxicValue");

			int countPred=0;
			int countExp=0;
			
			ArrayList<String>expVals=new ArrayList<String>();
			ArrayList<String>predValsBinary=new ArrayList<String>();
			ArrayList<String>predValsContinuous=new ArrayList<String>();

			while (true) {
				
				String lineBinary=brBinary.readLine();
				
				if (lineBinary==null) break;
				
				String lineContinuous=brContinuous.readLine();
				
				String [] valsBinary=lineBinary.split("\t");
				String [] valsContinuous=lineContinuous.split("\t");
				
				String CAS=valsBinary[colCAS];
				
				String predBinary=valsBinary[colPred];
				String predContinuous=valsContinuous[colPred];

				countExp++;
				
				double dpredBinary=Double.parseDouble(predBinary);
				double dpredContinuous=Double.parseDouble(predContinuous);
				
				
				if (dpredBinary==-9999) continue;
				if (dpredContinuous==-9999) continue;
				
				countPred++;
				
				double MW=Double.parseDouble(htMW.get(CAS));
				double pred_mg_kg=Math.pow(10,-dpredContinuous)*MW*1000;
				
				String exp=htExp.get(CAS);
				
				expVals.add(exp);
						
				String predBinaryFromContinous=null;
				String predBinaryAsBoolean=null;
				
				if (dpredBinary<0.5) predBinaryAsBoolean="FALSE";
				else predBinaryAsBoolean="TRUE";
				
				if (pred_mg_kg<50) predBinaryFromContinous="TRUE";
				else predBinaryFromContinous="FALSE";

				
				predValsBinary.add(predBinaryAsBoolean);
				predValsContinuous.add(predBinaryFromContinous);
				
//				System.out.println(dpredContinuous+"\t"+MW);
				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+predBinaryFromContinous+"\t"+predBinary+"\t"+predBinaryAsBoolean);
				
				
			}
			
			double coverage=(double)countPred/countExp;
			
			
			this.calcBinaryStats(expVals, predValsBinary);
			this.calcBinaryStats(expVals, predValsContinuous);

			System.out.println(coverage);
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	
	/**
	 * Use continuous dependent variable to predict binary for external set
	 */
	void predVeryToxic_LD50_input() {
		
		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\HC external set\\HC external set-fitvalues.txt";
//		String filepathContinuous="z build models\\NICEATM\\LD50_mgkg\\NN external set\\NN external set-fitvalues.txt";
		
		try {
			
			BufferedReader brContinuous=new BufferedReader(new FileReader(filepathContinuous));
			
			String header=brContinuous.readLine();

			
			Hashtable<String,String>htMW=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN", "MW");
			Hashtable<String,String>htExp=this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt", "CASRN","very_toxic");

			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CAS");
			
			int colPred=this.findFieldNum(hvals, "predToxicValue");

			int countPred=0;
			int countExp=0;
			
			ArrayList<String>expVals=new ArrayList<String>();
			ArrayList<String>predValsContinuous=new ArrayList<String>();

			while (true) {
				
				String lineContinuous=brContinuous.readLine();
				
				if (lineContinuous==null) break;
				
				
				String [] valsContinuous=lineContinuous.split("\t");
				
				String CAS=valsContinuous[colCAS];
				
				String predContinuous=valsContinuous[colPred];

				countExp++;
				
				double dpredContinuous=Double.parseDouble(predContinuous);
				
				
				if (dpredContinuous==-9999) continue;
				
				countPred++;
				
				double MW=Double.parseDouble(htMW.get(CAS));
				double pred_mg_kg=Math.pow(10,-dpredContinuous)*MW*1000;
				
				String exp=htExp.get(CAS);
				
				expVals.add(exp);
						
				String predBinaryFromContinous=null;
				
				if (pred_mg_kg<50) predBinaryFromContinous="TRUE";
				else predBinaryFromContinous="FALSE";

				
				predValsContinuous.add(predBinaryFromContinous);
				
//				System.out.println(dpredContinuous+"\t"+MW);
				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+predBinaryFromContinous);
				
				
			}
			
			double coverage=(double)countPred/countExp;
			
			this.calcBinaryStats(expVals, predValsContinuous);
			System.out.println(coverage);
			
			brContinuous.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	/**
	 * Uses spreadsheet derived results to get results for prediction set
	 */
void goThroughPredSetBinary2() {
		
		try {
			
			String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\final analysis";

			String fileNamePred="pred set.txt";
			
			String fileNameQSAR="hc very toxic.txt";
//			String fileNameQSAR="nn very toxic.txt";
//			String fileNameQSAR="nn.txt";
			
			String fieldExp="very_toxic";
//			String fieldExp="non_toxic";
			
			String fieldPred="predToxicValue";
			String fieldAD="Inside AD?";
						
			boolean useAD=true;
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"\\"+fileNamePred));
			
			Hashtable<String,String>htPreds=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldPred);
//			Hashtable<String,String>htAD=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldAD);
			
			String header=br.readLine();
			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CASRN");
			int colExp=this.findFieldNum(hvals, fieldExp);
			
			int countExp=0;
			int countPred=0;
			
			ArrayList<String>expVals=new ArrayList<String>();
			ArrayList<String>predVals=new ArrayList<String>();
			
			
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
//				System.out.println(Line);
				
				String [] vals=Line.split("\t");
				
				String CAS=vals[colCAS];
				String exp=vals[colExp];
				
				if (exp.equals("")) continue;

				if (htPreds.get(CAS)==null) continue;
				
				String pred=htPreds.get(CAS);
//				String AD=htAD.get(CAS);

				double dpred=Double.parseDouble(pred);
				
				countExp++;
				
				if (dpred==-9999) continue;
				
				String strPred="";
				
				if (dpred<0.5) strPred="FALSE";
				else strPred="TRUE";
				
				expVals.add(exp);
				predVals.add(strPred);
				countPred++;
				
//				System.out.println(countPred+"\t"+CAS+"\t"+exp+"\t"+strPred+"\t"+pred);

				
			}
			
			this.calcBinaryStats(expVals, predVals);
			
			double coverage=(double)countPred/countExp;
			
//			System.out.println(countPred+"\t"+countExp);
			
			System.out.println(coverage);
			System.out.println("");
		
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}

	}
	
	void goThroughPredSetCategory() {
		
		try {
			
			String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\final analysis";

			String fileNamePred="pred set.txt";
			
			String fileNameQSAR="hc.txt";
//			String fileNameQSAR="nn.txt";
			
			String fieldExp="GHS_category";
//			String fieldExp="EPA_category";
			
			String fieldPred=fieldExp;
			String fieldAD="Inside AD?";
						
			boolean useAD=true;
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"\\"+fileNamePred));
			
			Hashtable<String,String>htPreds=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldPred);
			Hashtable<String,String>htAD=this.getPreds(folder+"\\"+fileNameQSAR, "CAS", fieldAD);
			
			String header=br.readLine();
			
			String [] hvals=header.split("\t");
			
			int colCAS=this.findFieldNum(hvals, "CASRN");
			int colExp=this.findFieldNum(hvals, fieldExp);
			
			int count=0;
			
			ArrayList<Integer>expVals=new ArrayList<Integer>();
			ArrayList<Integer>predVals=new ArrayList<Integer>();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
//				System.out.println(Line);
				
				String [] vals=Line.split("\t");
				
				String CAS=vals[colCAS];
				String exp=vals[colExp];
				
				if (exp.equals("")) continue;

				if (htPreds.get(CAS)==null) continue;
				
				
				String pred=htPreds.get(CAS);
				String AD=htAD.get(CAS);

//				System.out.println(pred);
				
//				System.out.println("CAS="+CAS);
				
				
				if (useAD) {
					if (AD.equals("No")) continue;
				}
				
				if (pred.equals("-9999")) continue;
				
				int ipred=(int)Double.parseDouble(pred);
				
				expVals.add(Integer.parseInt(exp));
				predVals.add(new Integer(ipred));
				
				count++;
				
//				System.out.println(count+"\t"+CAS+"\t"+exp+"\t"+pred+"\t"+AD);
				
			}
			
			this.calcCategoryStats(expVals, predVals);
			
			
//			double [] dexpVals=new double [expVals.size()];
//			double [] dpredVals=new double [expVals.size()];
//			
//			for (int i=0;i<expVals.size();i++) {
//				dexpVals[i]=expVals.get(i);
//				dpredVals[i]=predVals.get(i);
//			}
//			double r2=this.calcR2_without_outliers(dexpVals, dpredVals);
//			System.out.println(r2+"\t"+dpredVals.length);
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}

	}

	
	void compileGHS_Category_HC_CategoryInput() {

		String folder = "L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM\\GHS_category\\Hierarchical external set";

		String outputFilepath="z build models\\NICEATM\\GHS_category\\HC external set\\HC external set-fitvalues.txt";

		try {
			
			FileWriter fw=new FileWriter(outputFilepath);
			
			fw.write("#\tCAS\texpToxicValue\tpredToxicValue\r\n");
			
			int countExp=0;
			
			for (int num=1;num<=10;num++) {


				BufferedReader[] brs = new BufferedReader[6];

				Hashtable<String, String> htExp = this.getPreds("z build models\\NICEATM\\final analysis\\pred set.txt",
						"CASRN", "GHS_category");


				for (int cat = 1; cat <= 5; cat++) {
					brs[cat] = new BufferedReader(
							new FileReader(folder + "/Hierarchical external set cat=" + cat + " num=" + num + ".txt"));

					brs[cat].readLine();
				}

				while (true) {

					double predMax = -9999;
					int predCat = -1;
					String CAS = "";

					boolean stop = false;

					for (int cat = 1; cat <= 5; cat++) {
						String Line = brs[cat].readLine();

						if (Line == null) {
							stop = true;
							break;
						}

						String[] vals = Line.split("\t");
						CAS = vals[1];
						String pred = vals[3];

						double dpred = Double.parseDouble(pred);

						// System.out.println(CAS+"\t"+dpred);

						if (dpred > predMax) {
							predMax = dpred;
							predCat = cat;
						}
					} //end loop over categories

					if (stop)
						break;

					if (predMax < 0.5) {
						predCat = -9999;
					}

					String exp = htExp.get(CAS);

					if (exp.equals("")) continue;
					
					countExp++;

					fw.write(countExp+"\t"+CAS + "\t" + exp + "\t" + predCat+"\r\n");

				}//end while loop over lines in file

			}

			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	public static void main(String [] args) {
		LookAtPredSetResults l=new LookAtPredSetResults();
		
//		l.goThroughPredSetNegLogMol_Kg();
		l.goThroughPredSetLogMg_Kg();
//		l.goThroughPredSetBinary();
//		l.goThroughPredSetCategory();

		
//		l.goThroughPredSetBinary2();
		
//		l.predVeryToxicBinary_input();
//		l.predVeryToxic_LD50_input();
		
//		l.compareExternalPredictionsLD50vsBinaryTraining();
		
//		l.compileGHS_Category_HC_CategoryInput();
		
//		l.predGHS_Category_CatInput();
//		l.predGHS_Category_LD50_mg_kg_Input();
//		l.predGHS_Category_ChemicalsInCommon();

		
		
	}
}

