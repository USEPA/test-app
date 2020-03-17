package QSAR.validation2;


import java.text.DecimalFormat;

import QSAR.validation2.GetStats;

public class PredictionStats {
	
	public static double [] ResizeArray (double [] array,int count) {
		double [] newarray=new double [count];
		
		for (int i=0;i<count;i++) {
			newarray[i]=array[i];
		}
		
		return newarray;
	}
	
	
	/**
	 * Writes out the stats for the chemicals that have valid predictions so far (changes as more chemicals are predicted)
	 * @param currentNum
	 * @param fw
	 * @param exp
	 * @param pred
	 * @param Yexpbar
	 */
		public static String getCurrentStats(int currentNum,double [] exp,double [] pred,double Yexpbar,DecimalFormat df) {
			
					
			try {
				
				double [] exp2;
				double [] pred2;
				
				int count=0;
				
				for (int i=0;i<=currentNum;i++) {
					if (pred[i]>-999) count++;				
				}
				
			
				exp2=new double [count];
				pred2=new double [count];
	
				count=0;
				for (int i=0;i<=currentNum;i++) {
					if (pred[i]>-999) {
						exp2[count]=exp[i];
						pred2[count]=pred[i];
						count++;				
					}
				}
	
				double CurrentQ2ext=GetStats.CalculateCurrentQ2ext(exp2,pred2,Yexpbar);
				double CurrentR2abs=GetStats.CalculateCurrentR2abs(exp2,pred2);
				double CurrentR2=GetStats.CalculateCurrentR2_2(exp2,pred2);
				double CurrentMAE=GetStats.CalculateCurrentMAE(exp2,pred2);
				double CurrentCoverage=(double)count/(double)(currentNum+1)*100.0;
							
				String Line=df.format(CurrentQ2ext)+"\t";
				Line+=(df.format(CurrentR2abs)+"\t");
				Line+=(df.format(CurrentR2)+"\t");
				Line+=(df.format(CurrentMAE)+"\t");
				Line+=(df.format(CurrentCoverage));
				return Line;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "err";
		}
		
	public static String GetStats(double [] exp,double [] pred,double Yexpbar,DecimalFormat df) {
		
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
			//0.4550	0.1549	0.4765	0.7196	94.7090	

			double CurrentQ2ext=CalculateCurrentQ2ext(exp2,pred2,Yexpbar);
			double CurrentR2abs=CalculateCurrentR2abs(exp2,pred2);
			double CurrentR2=CalculateCurrentR2_2(exp2,pred2);
			double CurrentMAE=CalculateCurrentMAE(exp2,pred2);
			double CurrentCoverage=(double)count/(double)(exp.length)*100.0;
						
			String Line=df.format(CurrentQ2ext)+"\t";
			Line+=(df.format(CurrentR2abs)+"\t");
			Line+=(df.format(CurrentR2)+"\t");
			Line+=(df.format(CurrentMAE)+"\t");
			Line+=(df.format(CurrentCoverage));
			return Line;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "err";
	}
	public static double CalculateCurrentQ2ext(double[] exp, double[] pred,
			double Yexpbar) {


		double[] X = exp;
		double[] Y = pred;

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
	
	public static double CalculateCurrentR2_2(double[] exp, double[] pred) {

		double[] X = exp;
		double[] Y = pred;

		double MeanX = 0;
		double MeanY = 0;

		for (int i = 0; i < X.length; i++) {
			MeanX += X[i];
			MeanY += Y[i];
		}

		MeanX /= (double) X.length;
		MeanY /= (double) X.length;

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

	private double CalculateCurrentR2(double[] exp, double[] pred) {

		double[] X = exp;
		double[] Y = pred;

		double MeanExpVal = 0;
		for (int i = 0; i < X.length; i++) {
			MeanExpVal += X[i];
			// System.out.println(exp[i]+"\t"+pred[i]);
		}
		// System.out.println("");

		MeanExpVal /= (double) X.length;

		double SSreg = 0;
		double SStot = 0;
		double R2 = 0;

		for (int i = 0; i < X.length; i++) {
			SSreg += Math.pow(X[i] - Y[i], 2.0);
			SStot += Math.pow(X[i] - MeanExpVal, 2.0);
		}

		double RMSE = Math.sqrt(SSreg / (double) X.length);

		R2 = 1 - SSreg / SStot;

		return R2;

	}

	public static double CalculateCurrentMAE(double[] exp, double[] pred) {
		double MAE = 0;
		for (int i = 0; i < exp.length; i++) {
			MAE += Math.abs(exp[i] - pred[i]);
		}
		MAE /= (double) exp.length;
		return MAE;
	}


	public static double CalculateCurrentR2abs(double[] exp, double[] pred) {
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


}
