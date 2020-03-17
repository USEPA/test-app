package QSAR.validation2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

import ToxPredictor.Utilities.Utilities;

public class GetStats {
	
	public static double CalculateCurrentMAE(double[] exp, double[] pred) {
		double MAE = 0;
		for (int i = 0; i < exp.length; i++) {
			MAE += Math.abs(exp[i] - pred[i]);
		}
		MAE /= (double) exp.length;
		return MAE;
	}
	
	

	public static double CalculateCurrentR2_2(double[] exp, double[] pred) {

		double[] X = exp;
		double[] Y = pred;

		double MeanX = 0;
		double MeanY = 0;

		for (int i = 0; i < X.length; i++) {
			MeanX += X[i];
			MeanY += Y[i];
		}
		// System.out.println("");

		MeanX /= (double) X.length;
		MeanY /= (double) X.length;

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
	
	public static double getR2_omit_no_predict(double [] exp,double [] pred) {
		try {
			
			double [] exp2;
			double [] pred2;
			
			int count=0;
			
			for (int i=0;i<exp.length;i++) {
				if (pred[i]>-999) count++;				
			}
			
		
			exp2=new double [count];
			pred2=new double [count];

			count=0;
			for (int i=0;i<exp.length;i++) {
				if (pred[i]>-999) {
					exp2[count]=exp[i];
					pred2[count]=pred[i];
					count++;				
				}
			}
			double r2=GetStats.CalculateCurrentR2_2(exp2,pred2);
			return r2;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
		
	}
	
	public static double getCoverage(double [] exp,double [] pred) {
		int count=0;
		for (int i=0;i<exp.length;i++) {
			if (pred[i]>-999) count++;				
		}
		double coverage=(double)count/(double)exp.length;
		return coverage;
	}
	
	public static double CalculateCurrentQ2ext(double[] exp, double[] pred,
			double Yexpbar) {

		// // first remove zero values in arrays:
		// int nonzerocount = 0;
		// for (int i = 0; i < exp.length; i++) {
		// if (exp[i] != 0) {
		// nonzerocount++;
		// }
		// }
		//
		// double[] exp2 = new double[nonzerocount];
		// double[] pred2 = new double[nonzerocount];
		//
		// int count = 0;
		//
		// for (int i = 0; i < exp.length; i++) {
		// if (exp[i] != 0) {
		// exp2[count] = exp[i];
		// pred2[count] = pred[i];
		// count++;
		// }
		// }
		//
		// double[] X = exp2;
		// double[] Y = pred2;

		double[] X = exp;
		double[] Y = pred;

		// double MeanExpVal=0;
		// for (int i=0;i<X.length;i++) MeanExpVal+=X[i];
		// MeanExpVal/=(double)X.length;

		// double Yexpbar=this.ccTraining.meanOrMode(this.classIndex);

		// System.out.println("Yexpbar = "+Yexpbar);

		double SSreg = 0;
		double SStot = 0;
		double R2 = 0;

		for (int i = 0; i < X.length; i++) {
			SSreg += Math.pow(X[i] - Y[i], 2.0);
			SStot += Math.pow(X[i] - Yexpbar, 2.0);
		}

		double RMSE = Math.sqrt(SSreg / (double) X.length);

		R2 = 1 - SSreg / SStot;

		return R2;

	}
	public static double CalculateCurrentR2abs(double[] exp, double[] pred) {

		// first remove zero values in arrays:
		// int nonzerocount = 0;
		// for (int i = 0; i < exp.length; i++) {
		// if (exp[i] != 0) {
		// nonzerocount++;
		// }
		// }
		//
		// double[] exp2 = new double[nonzerocount];
		// double[] pred2 = new double[nonzerocount];
		//
		// int count = 0;
		//
		// for (int i = 0; i < exp.length; i++) {
		// if (exp[i] != 0) {
		// exp2[count] = exp[i];
		// pred2[count] = pred[i];
		// count++;
		// }
		// }
		//
		// double[] X = exp2;
		// double[] Y = pred2;

		double[] X = exp;
		double[] Y = pred;

		double slope = 0;

		double sum1 = 0, sum2 = 0;
		for (int i = 0; i < X.length; i++) {
			sum1 += X[i] * Y[i];
			sum2 += X[i] * X[i];
		}
		slope = sum1 / sum2;

		double[] Xnew = Y;
		double[] Ynew = new double[X.length];

		for (int i = 0; i < X.length; i++) {
			Ynew[i] = slope * X[i];
		}

		double Yexpbar = 0;
		for (int i = 0; i < X.length; i++) {
			Yexpbar += Xnew[i];
		}
		Yexpbar /= (double) X.length;

		double SSreg = 0;
		double SStot = 0;
		double R2 = 0;

		for (int i = 0; i < X.length; i++) {
			SSreg += Math.pow(Xnew[i] - Ynew[i], 2.0);
			SStot += Math.pow(Xnew[i] - Yexpbar, 2.0);
		}

		double RMSE = Math.sqrt(SSreg / (double) X.length);

		R2 = 1 - SSreg / SStot;

		return R2;

	}
	
	
	public static String CompileNFoldPredictions(String trialFolderPath,int NSets) {
//		int NSets=5;
		
		DecimalFormat df=new DecimalFormat("000.00");
		
		try {
			
			FileWriter fw=new FileWriter(trialFolderPath+"/run1-"+NSets+"-fitvalues.txt");
			FileWriter fw2=new FileWriter(trialFolderPath+"/run1-"+NSets+"-fitvalues-sortbyerror.txt");
			
			fw.write("CAS\tExp\tPred\r\n");
			
			java.util.Vector<String>vec=new java.util.Vector<String>();
			
			java.util.Vector<String>vecCAS=new java.util.Vector<String>();
			java.util.Vector<Double>vecExp=new java.util.Vector<Double>();
			java.util.Vector<Double>vecPred=new java.util.Vector<Double>();
			
		for (int i=1;i<=NSets;i++) {
			File filei=new File(trialFolderPath+"/run"+i+"/run"+i+"-fitvalues.txt");
//			System.out.println(filei.exists());
			
			BufferedReader br=new BufferedReader(new FileReader(filei));
			String header=br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.List<String> l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
				
				String CAS=l.get(1);
				String exp=l.get(2);
				String pred=l.get(3);
				
				vecCAS.add(CAS);
				vecExp.add(Double.parseDouble(exp));
				vecPred.add(Double.parseDouble(pred));
				
			
				double dexp=Double.parseDouble(exp);
				double dpred=Double.parseDouble(pred);
				
				double error=Math.abs(dexp-dpred);
				if (dpred==-9999) error=-9999;
				
				String newLine=CAS+"\t"+exp+"\t"+pred;
				
				
				vec.add(df.format(error)+"\t"+newLine);
				
				fw.write(newLine+"\r\n");
				fw.flush();
			}//end while loop over lines in file
			
		}//end loop over sets
		
		double []exp=new double[vecExp.size()];
		double []pred=new double[vecExp.size()];
		
		int predCount=0;
		for (int i=0;i<vecExp.size();i++) {
			exp[i]=vecExp.get(i);
			pred[i]=vecPred.get(i);
			if (pred[i]>-999) predCount++;
		}
		
		double coverage=(double)predCount/(double)vecExp.size();
		double r2=GetStats.getR2_omit_no_predict(exp,pred);
		double product=coverage*r2;
		
		String result=r2+"\t"+coverage+"\t"+product;
		
		Collections.sort(vec,Collections.reverseOrder());
		
		
		fw2.write("CAS\tExp\tPred\tError\r\n");
		for (int i=0;i<vec.size();i++) {
			String Line=vec.get(i);			
			String sortLine=(Line.substring(Line.indexOf("\t")+1,Line.length()));
			sortLine+="\t"+Line.substring(0,Line.indexOf("\t"));
			fw2.write(sortLine+"\r\n");
			fw2.flush();
		}
		
		fw.close();
		fw2.close();
		
		return result;
		
		} catch (Exception  e) {
			
			e.printStackTrace();
			return "err";
		}
		
	}
	
	public static void GetResultsFromRunFiles(int Start, int Stop, String outputfolder) {
		try {

			FileWriter fw = new FileWriter(outputfolder + "/runinfo-" + Start
					+ "-" + Stop + ".txt");

			double AvgQ2=0;
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
				
				runcount++;

				BufferedReader br = new BufferedReader(new FileReader(filename));

				String header=br.readLine();
				java.util.LinkedList hl=Utilities.Parse(header, "\t");
								
								
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

				LinkedList ll = Utilities.Parse(LastLine, "\t");

				double Q2=Double.parseDouble((String)ll.get(ll.size() - 1));
				AvgQ2+=Q2;
				AvgCount+=count;
				fw.write(i + "\t" + ll.get(ll.size() - 1) + "\t"
						+ count+"\r\n");
				fw.flush();
				

				br.close();

			}
			
			AvgQ2/=runcount;
			AvgCount/=runcount;
			
			DecimalFormat df=new DecimalFormat("0.000");
			
			fw.write("Avg\t"+df.format(AvgQ2)+"\t"+AvgCount+"\r\n");
			
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
	
	public static void GetResultsFromRunFiles2(int Start, int Stop, String outputfolder) {
		try {

			FileWriter fw = new FileWriter(outputfolder + "/runinfo-" + Start
					+ "-" + Stop + ".txt");

			fw.write("Run#\tQ2ext\tR2abs\tR2\tCoverage\tcount\r\n");

			double AvgQ2ext=0;
			double AvgR2abs=0;
			double AvgR2=0;
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
				
				runcount++;

				BufferedReader br = new BufferedReader(new FileReader(filename));

				String header=br.readLine();
				java.util.LinkedList hl=Utilities.Parse(header, "\t");
				
				int colQ2ext=Utilities.GetColumnNumber("CurrentQ2ext", hl);
				int colR2abs=Utilities.GetColumnNumber("CurrentR2abs", hl);
				int colR2=Utilities.GetColumnNumber("CurrentR2", hl);
				int colCoverage=Utilities.GetColumnNumber("Coverage", hl);
								
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

				LinkedList ll = Utilities.Parse(LastLine, "\t");

				double Q2ext=Double.parseDouble((String)ll.get(colQ2ext));
				double R2abs=Double.parseDouble((String)ll.get(colR2abs));
				double R2=Double.parseDouble((String)ll.get(colR2));
				double Coverage=Double.parseDouble((String)ll.get(colCoverage));
				
				
				AvgQ2ext+=Q2ext;
				AvgR2abs+=R2abs;
				AvgR2+=R2;
				AvgCoverage+=Coverage;
				
				
				AvgCount+=count;
				
				fw.write(i + "\t" + Q2ext+"\t"+R2abs+"\t"+R2+"\t"+Coverage + "\t"
						+ count+"\r\n");
				fw.flush();
				

				br.close();

			}
			
			AvgQ2ext/=runcount;
			AvgR2abs/=runcount;
			AvgR2/=runcount;
			AvgCoverage/=runcount;
			
			AvgCount/=runcount;
			
			DecimalFormat df=new DecimalFormat("0.000");
			
			fw.write("Avg\t"+df.format(AvgQ2ext)+"\t"+df.format(AvgR2abs)+"\t"+df.format(AvgR2)+"\t"+df.format(AvgCoverage)+"\t"+AvgCount+"\r\n");
			
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
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
				java.util.LinkedList hl=Utilities.Parse(header, "\t");
				
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

				LinkedList ll = Utilities.Parse(LastLine, "\t");

				double Q2ext=Double.parseDouble((String)ll.get(colQ2ext));
				double R2abs=Double.parseDouble((String)ll.get(colR2abs));
				double R2=Double.parseDouble((String)ll.get(colR2));
				double MAE=Double.parseDouble((String)ll.get(colMAE));
				double Coverage=Double.parseDouble((String)ll.get(colCoverage));
				
				
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
	
	/**
	 * This version also extracts MAE
	 * @param Start
	 * @param Stop
	 * @param outputfolder
	 */
	public static void GetResultsFromRunFiles4(int Start, int Stop, String outputfolder,String outputfilename) {
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
						+ outputfilename;
				// System.out.println(filename);
				File file = new File(filename);
				if (!file.exists())
					continue;
				
				
				BufferedReader br = new BufferedReader(new FileReader(filename));
	
				String header=br.readLine();
				java.util.LinkedList hl=Utilities.Parse(header, "\t");
				
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
	
				LinkedList ll = Utilities.Parse(LastLine, "\t");
	
				double Q2ext=Double.parseDouble((String)ll.get(colQ2ext));
				double R2abs=Double.parseDouble((String)ll.get(colR2abs));
				double R2=Double.parseDouble((String)ll.get(colR2));
				double MAE=Double.parseDouble((String)ll.get(colMAE));
				double Coverage=Double.parseDouble((String)ll.get(colCoverage));
				
				
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
			
			DecimalFormat df=new DecimalFormat("0.000");
			
			fw.write("Avg\t"+df.format(AvgQ2ext)+"\t"+df.format(AvgR2abs)+"\t"+df.format(AvgR2)+"\t"+df.format(AvgMAE)+"\t"+df.format(AvgCoverage)+"\t"+AvgCount+"\r\n");
			
			fw.close();
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	//determines folder which has maximum final folder number
		public static int FindFinalNumber(String endpoint,String datafolder) {
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
					
//					System.out.println(num);
					int inum=Integer.parseInt(num);
					if (inum>max)max=inum;
				}
				
			}
			
			
			return max;
		}
		
		/**
		 * This version is used by CreateCSVFilesForStructureValidationStudies2
		 * @param Start
		 * @param Stop
		 * @param outputfolder
		 */
		public static void GetResultsFromRunFiles5(int Start, int Stop, String outputfolder,String outputfilename,String MolSrc,String destinationFolder) {
			try {
		

				FileWriter fw = new FileWriter(destinationFolder + "/"+outputfilename);
		
				fw.write("Run#\tQ2ext\tR2abs\tR2\tMAE\tCoverage\tcount\r\n");
		
				double AvgQ2ext=0;
				double AvgR2abs=0;
				double AvgR2=0;
				double AvgMAE=0;
				double AvgCoverage=0;
				
				double AvgCount=0;
				double runcount=0;
				
				double TotalChemicalsCalculated=0;
				
				for (int i = Start; i <= Stop; i++) {
					String runfolder = MolSrc+"-" + i;
					String filename = outputfolder + "/" + runfolder + "/"+runfolder+"-fitvalues.txt";
					 
					File file = new File(filename);
					if (!file.exists())
						continue;
					
					
					BufferedReader br = new BufferedReader(new FileReader(filename));
		
					String header=br.readLine();
					java.util.LinkedList hl=Utilities.Parse(header, "\t");
					
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
		
					LinkedList ll = Utilities.Parse(LastLine, "\t");
		
					double Q2ext=Double.parseDouble((String)ll.get(colQ2ext));
					double R2abs=Double.parseDouble((String)ll.get(colR2abs));
					double R2=Double.parseDouble((String)ll.get(colR2));
					double MAE=Double.parseDouble((String)ll.get(colMAE));
					double Coverage=Double.parseDouble((String)ll.get(colCoverage));
					
					double ChemicalsCalculated=(double)count*Coverage;
					
					TotalChemicalsCalculated+=ChemicalsCalculated;
					
					
					AvgQ2ext+=Q2ext*ChemicalsCalculated;
					AvgR2abs+=R2abs*ChemicalsCalculated;
					AvgR2+=R2*ChemicalsCalculated;
					AvgMAE+=MAE*ChemicalsCalculated;
					AvgCoverage+=Coverage*ChemicalsCalculated;
					
					
					AvgCount+=count;
					
					fw.write(i + "\t" + Q2ext+"\t"+R2abs+"\t"+R2+"\t"+MAE+"\t"+Coverage + "\t"
							+ count+"\r\n");
					fw.flush();
					
		
					br.close();
		
				}
				
				AvgQ2ext/=TotalChemicalsCalculated;
				AvgR2abs/=TotalChemicalsCalculated;
				AvgR2/=TotalChemicalsCalculated;
				AvgMAE/=TotalChemicalsCalculated;
				AvgCoverage/=TotalChemicalsCalculated;
				
				AvgCount/=runcount;
				
				DecimalFormat df=new DecimalFormat("0.000");
				
				fw.write("Avg\t"+df.format(AvgQ2ext)+"\t"+df.format(AvgR2abs)+"\t"+df.format(AvgR2)+"\t"+df.format(AvgMAE)+"\t"+df.format(AvgCoverage)+"\t"+AvgCount+"\r\n");
				
				fw.close();
		
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
}
