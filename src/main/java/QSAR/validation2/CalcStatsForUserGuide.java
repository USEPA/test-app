package QSAR.validation2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JLabel;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart;

public class CalcStatsForUserGuide {

	
	public double[] CalculateR2abs(double[] exp, double[] pred) {

		double[] X = exp;
		double[] Y = pred;

		double slope=0;

		double sum1=0, sum2=0;
		for (int i=0;i<X.length;i++) {
			sum1+=X[i]*Y[i];
			sum2+=X[i]*X[i];
		}
		slope=sum1/sum2;
		
		
		double[] Xnew = Y;
		double[] Ynew = new double [X.length];

		for (int i=0;i<X.length;i++) {
			Ynew[i]=slope*X[i];
		}
		
		
		double Yexpbar=0;
		for (int i=0;i<X.length;i++) {
			Yexpbar+=Xnew[i];		
		}
		Yexpbar/=(double)X.length;
		
		double SSreg = 0;
		double SStot = 0;
		double R2 = 0;

		for (int i = 0; i < X.length; i++) {
			SSreg += Math.pow(Xnew[i] - Ynew[i], 2.0);
			SStot += Math.pow(Xnew[i] - Yexpbar, 2.0);
		}

		double RMSE = Math.sqrt(SSreg / (double) X.length);

		R2 = 1 - SSreg / SStot;
		

		double [] results=new double [2];
		results[0]=R2;
		results[1]=slope;
		return results;
	}
	
	
	/**
	 * Searches a file (in a jar) for the record specified by the keyValue
	 * and then returns the value for valueColumnName 
	 * 
	 * @param filename
	 * @param keyValue
	 * @param keyColumnName
	 * @param valueColumnName
	 * @param delimiter
	 * @return
	 */
	public void CreatePlot(String endpoint,String destFilePath,double []x,double []y) {
		try {
			
			String axistitle;
			if (endpoint.equals(TESTConstants.ChoiceBoilingPoint) || 
					endpoint.equals(TESTConstants.ChoiceDensity) ||
					endpoint.equals(TESTConstants.ChoiceFlashPoint) ||
					endpoint.equals(TESTConstants.ChoiceMeltingPoint) ||
					endpoint.equals(TESTConstants.ChoiceSurfaceTension) ||
					endpoint.equals(TESTConstants.ChoiceThermalConductivity)) {
				
				axistitle=endpoint+" "+TESTConstants.getMassUnits(endpoint);
				
			} else {
				axistitle=endpoint+" "+TESTConstants.getMolarLogUnits(endpoint);
			}
			
			String xtitle="Exp. "+axistitle;
			String ytitle="Pred. "+axistitle;
//			String title="Test set predictions for the "+methodColumnName+" method";
			String title="External prediction results";
			fraChart fc = new fraChart(x,y,title,xtitle,ytitle);
			
			fc.jlChart.doDrawLegend=true;
			
			fc.WriteImageToFile(destFilePath);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	double CalcAllStatsForRndRun(String endpoint,String filepath,String del,String outputfilepath,String outputChartPath) {

		double AvgVal=-1;

		try {
			
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			br.readLine();
			
			String header=br.readLine();

			Vector <String>Data=new Vector<String>();

			while (true) {
				String Line=br.readLine();				
				if (Line==null) break;

				Data.add(Line);
			}
			br.close();

			Vector<String>CAS=new Vector<String>();
			Vector<Double>exp=new Vector<Double>();

//			System.out.println(header);
			
			LinkedList<String> hl=ToxPredictor.Utilities.Utilities.Parse(header, del);
			
			int nmethods=3;		
			if (TESTConstants.haveSingleModelMethod(endpoint)) nmethods++;
			if (TESTConstants.haveGroupContributionMethod(endpoint)) nmethods++;

			double [][] preds=new double [Data.size()][nmethods];


			for (int i=0;i<Data.size();i++) {
				LinkedList<String> l=ToxPredictor.Utilities.Utilities.Parse(Data.get(i), del);

				String CASi=l.get(1);
				CAS.add(CASi);

				double expi=Double.parseDouble(l.get(2));
				exp.add(expi);

				for (int j=0;j<nmethods;j++) {
					
					String strpred=l.get(j+3);
					if (strpred.contentEquals("N/A")) strpred="-9999";					
					double predij=Double.parseDouble(strpred);
					preds[i][j]=predij;
				}
			}


			FileWriter fw=new FileWriter(outputfilepath);

			fw.write("Method	R2	(R2-R2abs)/R2	k	RMSE	MAE	Coverage\r\n");

			for (int j=0;j<nmethods;j++) {
				
				String method=hl.get(j+3).replace("Pred_", "");
				
				Vector<Double>conExp=new Vector<Double>();
				Vector<Double>conPred=new Vector<Double>();
				Vector<Double>conExp2=new Vector<Double>();// array of values if have no pred!

				for (int i=0;i<Data.size();i++) {
					double predij=preds[i][j];

					if (predij!=-9999) {
						conExp.add(exp.get(i));
						conPred.add(predij);
					} else {
						conExp2.add(exp.get(i));
					}
				}
				double [] expArray=VectorToDoubleArray(conExp);
				double [] predArray=VectorToDoubleArray(conPred);
				double [] expArray2=VectorToDoubleArray(conExp2);

				double []results = this.CalculateR2abs(expArray, predArray);	
				
				double R2abs=results[0];
				double k=results[1];
				double R2=this.CalculateR2(expArray, predArray);
				double MAE=this.CalculateMAE(expArray, predArray);
				double RMSE=this.CalculateRMSE(expArray, predArray);
				double coverage=(double)expArray.length/((double)expArray.length+(double)conExp2.size());
				double bob=(R2-R2abs)/R2;

				//				R2	(R2-R2abs)/R2	k	RMSE	MAE	Coverage
				java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");


				fw.write(method+"\t");
				fw.write(df.format(R2)+"\t");
				fw.write(df.format(bob)+"\t");
				fw.write(df.format(k)+"\t");
				fw.write(df.format(RMSE)+"\t");
				fw.write(df.format(MAE)+"\t");
				fw.write(df.format(coverage)+"\t");
				fw.write("\r\n");			
				fw.flush();
				
				if (method.contentEquals("Consensus")) {
					CreatePlot(endpoint, outputChartPath, expArray, predArray);
				}
				

				if (j==nmethods) {
					AvgVal=MAE;
					//					System.out.println(run+"\t"+MAE);
				}
				
				
				
			}

			fw.close();

			return AvgVal;
			//			System.out.println(Data.size());


		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;

	}
	
	double CalcAllStatsForRndRunBinary(String endpoint,String filepath,String del,String outputfilepath) {

		double AvgVal=-1;

		try {
			
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
//			br.readLine();
			
			String header=br.readLine();

			Vector <String>Data=new Vector<String>();

			while (true) {
				String Line=br.readLine();				
				if (Line==null) break;

				Data.add(Line);
			}
			br.close();

			Vector<String>CAS=new Vector<String>();
			Vector<Double>exp=new Vector<Double>();

//			System.out.println(header);
			
			LinkedList<String> hl=ToxPredictor.Utilities.Utilities.Parse(header, del);
			
			int nmethods=3;		
			if (TESTConstants.haveSingleModelMethod(endpoint)) nmethods++;
			if (TESTConstants.haveGroupContributionMethod(endpoint)) nmethods++;

			double [][] preds=new double [Data.size()][nmethods];


			for (int i=0;i<Data.size();i++) {
				LinkedList<String> l=ToxPredictor.Utilities.Utilities.Parse(Data.get(i), del);

				String CASi=l.get(1);
				CAS.add(CASi);

				double expi=Double.parseDouble(l.get(2));
				exp.add(expi);

				for (int j=0;j<nmethods;j++) {
					
					String strpred=l.get(j+3);
					if (strpred.contentEquals("N/A")) strpred="-9999";					
					double predij=Double.parseDouble(strpred);
					preds[i][j]=predij;
				}
			}


			FileWriter fw=new FileWriter(outputfilepath);

			fw.write("Method	Concordance	Sensitivity	Specificity	Coverage\r\n");

			for (int j=0;j<nmethods;j++) {
				
				String method=hl.get(j+3).replace("Pred_", "");
				
				System.out.println(method);
				
				Vector<Double>conExp=new Vector<Double>();
				Vector<Double>conPred=new Vector<Double>();
				Vector<Double>conExp2=new Vector<Double>();// array of values if have no pred!

				for (int i=0;i<Data.size();i++) {
					double predij=preds[i][j];

					if (predij!=-9999) {
						conExp.add(exp.get(i));
						conPred.add(predij);
					} else {
						conExp2.add(exp.get(i));
					}
				}
				double [] expArray=VectorToDoubleArray(conExp);
				double [] predArray=VectorToDoubleArray(conPred);
				double [] expArray2=VectorToDoubleArray(conExp2);

				double []results = this.CalculateR2abs(expArray, predArray);	
				
				double coverage=(double)expArray.length/((double)expArray.length+(double)conExp2.size());
				
				double [] stats=CalculateCancerStats(0.5, expArray, predArray);

				double conc=stats[0];
				double sens=stats[1];
				double spec=stats[2];

				//				R2	(R2-R2abs)/R2	k	RMSE	MAE	Coverage
				java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");


				fw.write(method+"\t");
				fw.write(df.format(conc)+"\t");
				fw.write(df.format(sens)+"\t");
				fw.write(df.format(spec)+"\t");
				fw.write(df.format(coverage)+"\t");
				fw.write("\r\n");			
				fw.flush();
				
				if (j==nmethods) {
					AvgVal=(sens+spec)/2.0;
					//					System.out.println(run+"\t"+MAE);
				}
				
				
				
			}

			fw.close();

			return AvgVal;
			//			System.out.println(Data.size());


		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;

	}
	
	public double CalculateMAE(double [] exp,double [] pred) {
		double MAE=0;
		for (int i=0;i<exp.length;i++) {
			MAE+=Math.abs(exp[i]-pred[i]);
		}
		MAE/=(double)exp.length;
		return MAE;
	}
	
	private double CalculateRMSE(double [] exp,double [] pred) {
		double RMSE=0;
		for (int i=0;i<exp.length;i++) {
			RMSE+=Math.pow(exp[i]-pred[i],2);
		}
		RMSE=Math.sqrt(RMSE/(double)exp.length);
		return RMSE;
	}
	

	double [] VectorToDoubleArray(Vector<Double>v) {
		double []array=new double[v.size()];

		for (int i=0;i<v.size();i++) {
			array[i]=v.get(i);
		}
		return array;
	}


	public double CalculateR2(double [] exp,double [] pred) {

		double [] X=exp;
		double [] Y=pred;


		double MeanX=0;
		double MeanY=0;

		for (int i=0;i<X.length;i++) {
			MeanX+=X[i];
			MeanY+=Y[i];			
		}
		//		System.out.println("");

		MeanX/=(double)X.length;				
		MeanY/=(double)X.length;


		// 	  double Yexpbar=this.ccTraining.meanOrMode(this.classIndex);

		// 	  System.out.println("Yexpbar = "+Yexpbar);

		double termXY=0;
		double termXX=0;
		double termYY=0;


		double R2=0;

		for (int i=0;i<X.length;i++) {
			termXY+=(X[i]-MeanX)*(Y[i]-MeanY);
			termXX+=(X[i]-MeanX)*(X[i]-MeanX);
			termYY+=(Y[i]-MeanY)*(Y[i]-MeanY);
		}

		R2=termXY*termXY/(termXX*termYY);

		return R2;

	}	

	
	//calculates cancer stats (expArray and predArray have compounds outside the AD omitted)
		public double [] CalculateCancerStats(double cutoff,double []expArray,double []predArray) {
			double [] results=new double [3];
			
			double concordance=0;
			
			int posPredcount=0;
			int negPredcount=0;
			
			double posConcordance=0;
			double negConcordance=0;
			
			for (int i=0;i<expArray.length;i++) {
				double exp=expArray[i];
				double pred=predArray[i];
				
				String strExp="";
				
				if (cutoff==0.5) {
					if (exp>=cutoff) strExp="C";
					else strExp="NC";
				} else if (cutoff==30) {
					if (exp==0) strExp="N/A";
					else if (exp>=cutoff) strExp="C";
					else strExp="NC";
				}
				

				String strPred="";
				
				if (pred>=cutoff) strPred="C";
				else strPred="NC";
				
//				if (strExp.equals("C"))
//					System.out.println(exp+"\t"+pred+"\t"+strExp+"\t"+strPred);

				
				if (strExp.equals("C")) posPredcount++;
				else if (strExp.equals("NC")) negPredcount++;

				if (strExp.equals(strPred)) {
					concordance++;
					
					if (strExp.equals("C")) posConcordance++;
					else if (strExp.equals("NC")) negConcordance++;
					
				}
				
				
			}
						
			concordance/=(double)expArray.length;			
			posConcordance/=(double)posPredcount;
			negConcordance/=(double)negPredcount;

//			System.out.println("posConc="+posConcordance);
			
			results[0]=concordance;
			results[1]=posConcordance;
			results[2]=negConcordance;
			
			return results;
		}
		
		
	public static void main(String[] args) {
		CalcStatsForUserGuide c=new CalcStatsForUserGuide();
		String folder="D:\\MyToxicityUserGuide\\ToxRuns";
		
//		String endpoint=TESTConstants.ChoiceFHM_LC50;//OK
		String endpoint=TESTConstants.ChoiceDM_LC50;//Need to figure out why diff
//		String endpoint=TESTConstants.ChoiceTP_IGC50;//OK
//		String endpoint=TESTConstants.ChoiceRat_LD50;//OK
//		String endpoint=TESTConstants.ChoiceBCF;//OK
		//TODO devtox
		
//		String endpoint=TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity;//OK
//		String endpoint=TESTConstants.ChoiceEstrogenReceptor;//OK
//		String endpoint=TESTConstants.ChoiceMutagenicity;//OK
//		String endpoint=TESTConstants.ChoiceReproTox;//OK

//		String endpoint=TESTConstants.ChoiceBoilingPoint;//OK
//		String endpoint=TESTConstants.ChoiceVaporPressure;//OK
//		String endpoint=TESTConstants.ChoiceMeltingPoint;//OK
//		String endpoint=TESTConstants.ChoiceFlashPoint;//OK
//		String endpoint=TESTConstants.ChoiceDensity;//OK
//		String endpoint=TESTConstants.ChoiceSurfaceTension;//OK
//		String endpoint=TESTConstants.ChoiceThermalConductivity;//OK
//		String endpoint=TESTConstants.ChoiceViscosity;//OK
//		String endpoint=TESTConstants.ChoiceWaterSolubility;//OK

		
		String filepath=folder+"\\Batch_"+endpoint.replace(" ", "_")+"_all_methods_1.txt";
		String outputfilepath=folder+"\\Batch_"+endpoint.replace(" ", "_")+"_stats.txt";
		String outputChartPath=folder+"\\Batch_"+endpoint.replace(" ", "_")+"_consensus.png";
		
		if (TESTConstants.isBinary(endpoint)) {
			c.CalcAllStatsForRndRunBinary(endpoint,filepath, "|", outputfilepath);	
		} else {
			c.CalcAllStatsForRndRun(endpoint,filepath, "|", outputfilepath,outputChartPath);	
		}
		
		
//		String [] endpoints=TESTConstants.getFullEndpoints(null);
//		
//		for (String endpoint:endpoints) {
//			String filepath=folder+"\\Batch_"+endpoint.replace(" ", "_")+"_all_methods_1.txt";
//			String outputfilepath=folder+"\\Batch_"+endpoint.replace(" ", "_")+"_stats.txt";
//			String outputChartPath=folder+"\\Batch_"+endpoint.replace(" ", "_")+"_consensus.png";
//			c.CalcAllStatsForRndRun(endpoint,filepath, "|", outputfilepath,outputChartPath);
//			
//		}
		
		
	}

}


