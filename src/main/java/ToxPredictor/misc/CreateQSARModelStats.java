package ToxPredictor.misc;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.GUI.Miscellaneous.fraChart;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Vector;


/**
 * This class creates spreadsheets and plots for all endpoint for the predictions sets
 *  
 * It was needed since WebTEST doesnt use
 * FDA method so needed to recalculate statistics for the User's Guide.
 * 
 * @author Todd Martin
 *
 */

public class CreateQSARModelStats {

	int minPredCount=2;//min number of predictions for consensus method
	
	/**
	 * Create a spreadsheet of results for different methods, badCAS chemicals are omitted
	 * 
	 * @param endpoint
	 * @param resultsFolder
	 * @param badCAS
	 */
	void createRunSpreadsheet(String endpoint,String resultsFolder,Vector<String>badCAS) {

		System.out.println(endpoint);
		
		String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");

		Vector<String> methods=new Vector<String>();
		Vector<String> methods2=new Vector<String>();
		

		methods.add("Hierarchical clustering"); 
		methods.add("Single Model");
		methods.add("Group contribution");
		methods.add("Nearest neighbor");
//		methods.add("Consensus"); dont need???

		String outputFilePath=resultsFolder+"/"+endpoint+"/"+endpoint+".xls";

		File of=new File(outputFilePath);
		if (of.exists()) of.delete();

		Vector <BufferedReader> readers=new Vector<BufferedReader>();

		try {
			
			HSSFWorkbook wb = new HSSFWorkbook();

			for (int i=0;i<methods.size();i++) {

				String resultsFilePath=resultsFolder+"/"+endpoint+"/"+methods.get(i)+"/"+endpointAbbrev+"_prediction.txt";

				File rf=new File(resultsFilePath);

				if (rf.exists()) {			
					methods2.add(methods.get(i));
//					System.out.println(methods.get(i));
					readers.add(new BufferedReader(new FileReader(resultsFilePath)));	
				}
			}

			
			if (readers.size()==0) return;

			for (int i=0;i<readers.size();i++) {
				BufferedReader br=readers.get(i);
				
				if (TESTConstants.isBinary(endpoint)) {
					this.GetRunDataCancer(methods2,i,br,wb,badCAS);
				} else {
					this.GetRunData(methods2,i,br,wb,badCAS);	
				}
				
			}
									
			for (int i=0;i<readers.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}

			if (TESTConstants.isBinary(endpoint)) {
				this.CreateSummarySheet2Cancer(wb,methods2,endpoint);
			} else {
				this.CreateSummarySheet2(wb,methods2,endpoint);	
			}
			
			
						
			FileOutputStream fOut = new FileOutputStream(outputFilePath);
		// Write the XL sheet
			
			wb.setActiveSheet(wb.getNumberOfSheets()-1);
			wb.write(fOut);
			
			fOut.flush();
		// Done Deal..
			fOut.close();


		} catch (Exception e) {
			e.printStackTrace();
		}

		//		File file=new File(fileNameFrag);
		//		System.out.println(file.exists());


	}
	
	/**
	 * Loads results from text files for a given method and stores in spreadsheet (binary endpoint)
	 * 
	 * @param methods
	 * @param methodNumber
	 * @param br
	 * @param wb
	 * @param badCAS
	 */
	void GetRunDataCancer(Vector<String> methods,int methodNumber,BufferedReader br,HSSFWorkbook wb,Vector<String>badCAS) {
		try {
			
			String method=methods.get(methodNumber);
			
			HSSFSheet sheet = wb.createSheet(method);
			
			// ****************************************************
			HSSFSheet sheetConsensus=wb.getSheet("Consensus");
			
			if (sheetConsensus==null) {
				sheetConsensus = wb.createSheet("Consensus");
				wb.setSheetOrder("Consensus", 0);
				CreateConsensusTabHeader(sheetConsensus,methods);
			}

			// ****************************************************
			
			String header=(br.readLine());
			
			Vector<String>CAS=new Vector<String>();
			Vector<Double>exp=new Vector<Double>();
			Vector<Double>pred=new Vector<Double>();
			Vector<String>CAS2=new Vector<String>();// array of values if have no pred!
			Vector<Double>exp2=new Vector<Double>();// array of values if have no pred!

			//Consensus pred vectors
			Vector<String>conCAS=new Vector<String>();
			Vector<Double>conExp=new Vector<Double>();
			Vector<Double>conPred=new Vector<Double>();
			Vector<String>conCAS2=new Vector<String>();// array of values if have no pred!
			Vector<Double>conExp2=new Vector<Double>();// array of values if have no pred!

			
			HSSFRow row = sheetConsensus.getRow(0);
			if (row==null) row=sheetConsensus.createRow(0);
			
			HSSFCell cell = row.createCell(methodNumber+2);
			
			cell.setCellValue(method);
			
			int counter=1;
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				if (Line.indexOf("concordance")>-1) break;
				
				java.util.LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
				
				String currentCAS=list.get(1);
				String strExp=list.get(2);
				String strPred=list.get(3);
//				System.out.println(CAS+"\t"+strExp+"\t"+strPred);
				if (badCAS.contains(currentCAS)) continue;
				
				if (strPred.equals("N/A")) strPred="-9999";
				
				double dPred=Double.parseDouble(strPred);
				double dExp=Double.parseDouble(strExp);
				
				if (dPred!=-9999) {
					CAS.add(currentCAS);
					exp.add(dExp);
					pred.add(dPred);
				} else {
					CAS2.add(currentCAS);
					exp2.add(dExp);
				}
				
				WriteValToConsensusTab(sheetConsensus,counter,methodNumber,currentCAS,dExp,dPred);
				
				if (methodNumber==methods.size()-1) {
					row = sheetConsensus.getRow(counter);
					double predVal=this.CalculateConsensus(row,methods);

					cell=row.createCell(methods.size()+2);
					
					cell.setCellValue(predVal);
					
					if (predVal!=-9999) {
						conCAS.add(currentCAS);
						conExp.add(dExp);
						conPred.add(predVal);
					} else {
						conCAS2.add(currentCAS);
						conExp2.add(dExp);
					}
//					System.out.println(currentCAS+"\t"+predVal);
				}
				counter++;
				
			}// end while true
			
			double [] expArray=VectorToDoubleArray(exp);
			double [] predArray=VectorToDoubleArray(pred);
			
			double [] expArray2=VectorToDoubleArray(exp2);
			
			this.OutputPredictionsToTab(0,sheet, CAS, expArray, predArray, CAS2, expArray2);
			
//			System.out.println(method);
			this.outputMethodStatsCancer(expArray, predArray, sheet,exp2.size(),4);
			
			if (methodNumber==methods.size()-1) {
				double [] conExpArray=VectorToDoubleArray(conExp);
				double [] conPredArray=VectorToDoubleArray(conPred);
				double [] conExpArray2=VectorToDoubleArray(conExp2);
				
				this.outputMethodStatsCancer(conExpArray, conPredArray, sheetConsensus,conExp2.size(),methods.size()+8);
				this.OutputPredictionsToTab(methods.size()+4,sheetConsensus, conCAS, conExpArray, conPredArray, conCAS2, conExpArray2);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a summary of different methods for binary endpoints
	 * 
	 * @param wb
	 * @param methods
	 * @param endpoint
	 */
	void CreateSummarySheet2Cancer(HSSFWorkbook wb,Vector<String> methods,String endpoint) {
		try {
		HSSFSheet sheet = wb.createSheet("Summary");
		
		sheet.setColumnWidth(0, 18*256);
		
		for (int i=1;i<=6;i++) {
			sheet.setColumnWidth(i, 13*256);
		}

		short formatD3=wb.createDataFormat().getFormat("0.000");
		
		CellStyle cs=wb.createCellStyle();
		cs.setDataFormat(formatD3);
		cs.setAlignment(HorizontalAlignment.CENTER);
		
		CellStyle csHeader=wb.createCellStyle();
		csHeader.setBorderTop(BorderStyle.MEDIUM);
		csHeader.setBorderBottom(BorderStyle.MEDIUM);
		csHeader.setAlignment(HorizontalAlignment.CENTER);
		
		CellStyle csBottom=wb.createCellStyle();
		csBottom.setBorderBottom(BorderStyle.MEDIUM);
		csBottom.setAlignment(HorizontalAlignment.CENTER);
		csBottom.setDataFormat(formatD3);
		
		CellStyle csBottom2=wb.createCellStyle();
		csBottom2.setBorderBottom(BorderStyle.MEDIUM);
//		csBottom2.setAlignment(HorizontalAlignment.CENTER);

		
		
		//Creating header row:
		HSSFRow row = sheet.createRow(0);
		
		HSSFCell cell=row.createCell(0);
		
		cell.setCellValue("Method");
		cell.setCellStyle(csHeader);
		
		//

		cell=row.createCell(1);
		
		cell.setCellValue("Concordance");
		cell.setCellStyle(csHeader);

		cell=row.createCell(2);
		
		cell.setCellValue("Sensitivity");
		cell.setCellStyle(csHeader);

		cell=row.createCell(3);
		
		cell.setCellValue("Specificity");
		cell.setCellStyle(csHeader);
					
		cell=row.createCell(4);
		
		cell.setCellValue("Coverage");
		cell.setCellStyle(csHeader);
		
		
		
		for (int i=0;i<methods.size();i++) {
			HSSFSheet sheetMethod = wb.getSheet(methods.get(i));

			row = sheet.createRow(i+1);
			cell=row.createCell(0);
			
			cell.setCellValue(methods.get(i));

			HSSFRow rowMethod = sheetMethod.getRow(1);
			
			for (int j=1;j<=4;j++) {
				double val=rowMethod.getCell(j+3).getNumericCellValue();
				cell=row.createCell(j);
				
				cell.setCellValue(val);
				cell.setCellStyle(cs);
			} // end stat j loop
		}//end methods i loop
		
		
		
		HSSFSheet sheetConsensus = wb.getSheet("Consensus");
		HSSFRow rowConsensus = sheetConsensus.getRow(1);
		row = sheet.createRow(methods.size()+1);

		cell=row.createCell(0);
		
		cell.setCellValue("Consensus");
		cell.setCellStyle(csBottom2);
		
		for (int j=1;j<=4;j++) {
			double val=rowConsensus.getCell(j+methods.size()+7).getNumericCellValue();
			cell=row.createCell(j);
			
			cell.setCellValue(val);
			cell.setCellStyle(csBottom);
		} // end stat j loop
		
		// *****************************************************
		
		
		CellStyle csCenter=wb.createCellStyle();
		csCenter.setAlignment(HorizontalAlignment.CENTER);

		row = sheet.createRow(methods.size()+2);
		row = sheet.createRow(methods.size()+3);
		cell=row.createCell(0);
		
		cell.setCellValue("Endpoint");

		cell=row.createCell(1);
		
		cell.setCellValue(endpoint);
		cell.setCellStyle(csCenter);

//		row = sheet.createRow(methods.size()+4);
//		cell=row.createCell(0);
//		
//		cell.setCellValue("RndRun#");
//		
//		cell=row.createCell(1);
//		
//		cell.setCellValue(rndRun);
//		cell.setCellStyle(csCenter);

		} catch (Exception e) {
			e.printStackTrace();
		}
			
		
		
	}
	
	/**
	 * Create header row for consensus tab
	 * 
	 * @param sheetConsensus
	 * @param methods
	 */
	void CreateConsensusTabHeader(HSSFSheet sheetConsensus,Vector<String> methods) {
		HSSFRow row = sheetConsensus.createRow(0);
		HSSFCell cell = row.createCell(0);
		
		cell.setCellValue("CAS");
	
		cell = row.createCell(1);
		
		cell.setCellValue("exp");
		
		cell = row.createCell(methods.size()+2);
		
		cell.setCellValue("Consensus");				
	}
	
	/**
	 * Loads results from text files for a given method and stores in spreadsheet
	 * 
	 * @param methods
	 * @param methodNumber
	 * @param br
	 * @param wb
	 * @param badCAS
	 */
	void GetRunData(Vector<String> methods,int methodNumber,BufferedReader br,HSSFWorkbook wb,Vector<String>badCAS) {
		try {
			
			String method=methods.get(methodNumber);
			
			HSSFSheet sheet = wb.createSheet(method);
			
			// ****************************************************
			HSSFSheet sheetConsensus=wb.getSheet("Consensus");
			
			if (sheetConsensus==null) {
				sheetConsensus = wb.createSheet("Consensus");
				wb.setSheetOrder("Consensus", 0);
				CreateConsensusTabHeader(sheetConsensus,methods);
			}

			// ****************************************************
			
			String header=(br.readLine());
			
			Vector<String>CAS=new Vector<String>();
			Vector<Double>exp=new Vector<Double>();
			Vector<Double>pred=new Vector<Double>();
			Vector<String>CAS2=new Vector<String>();// array of values if have no pred!
			Vector<Double>exp2=new Vector<Double>();// array of values if have no pred!

			//Consensus pred vectors
			Vector<String>conCAS=new Vector<String>();
			Vector<Double>conExp=new Vector<Double>();
			Vector<Double>conPred=new Vector<Double>();
			Vector<String>conCAS2=new Vector<String>();// array of values if have no pred!
			Vector<Double>conExp2=new Vector<Double>();// array of values if have no pred!

			
			HSSFRow row = sheetConsensus.getRow(0);
			if (row==null) row=sheetConsensus.createRow(0);
			
			HSSFCell cell = row.createCell(methodNumber+2);
			
			cell.setCellValue(method);
			
			int counter=1;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				java.util.LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
				
				String currentCAS=list.get(1);
				String strExp=list.get(2);
				String strPred=list.get(3);
				
				if (badCAS.contains(currentCAS)) continue;
				
//				if (currentCAS.equals("76-29-9"))
//					System.out.println(currentCAS+"\t"+strExp+"\t"+strPred);
				
				if (strPred.equals("N/A")) strPred="-9999";
				
				double dPred=Double.parseDouble(strPred);
				double dExp=Double.parseDouble(strExp);
				
				if (dPred!=-9999) {
					CAS.add(currentCAS);
					exp.add(dExp);
					pred.add(dPred);
				} else {
					CAS2.add(currentCAS);
					exp2.add(dExp);
				}
				
				WriteValToConsensusTab(sheetConsensus,counter,methodNumber,currentCAS,dExp,dPred);
				
				if (methodNumber==methods.size()-1) {
					row = sheetConsensus.getRow(counter);

					double predVal=this.CalculateConsensus(row,methods);

					//TODO- do we need this since we have consensus output file? 

					cell=row.createCell(methods.size()+2);
					
					cell.setCellValue(predVal);
					
//					System.out.println(currentCAS+"\t"+predVal);
					
					if (predVal!=-9999) {
						conCAS.add(currentCAS);
						conExp.add(dExp);
						conPred.add(predVal);
					} else {
						conCAS2.add(currentCAS);
						conExp2.add(dExp);
					}
//					System.out.println(currentCAS+"\t"+predVal);
				}
				counter++;
				
			}// end while true
			
			double [] expArray=VectorToDoubleArray(exp);
			double [] predArray=VectorToDoubleArray(pred);
			
			double [] expArray2=VectorToDoubleArray(exp2);
			
			this.OutputPredictionsToTab(0,sheet, CAS, expArray, predArray, CAS2, expArray2);
						
			this.outputMethodStats(expArray, predArray, sheet,exp2.size(),4);
			
			
			
			if (methodNumber==methods.size()-1) {
				
				
				double [] conExpArray=VectorToDoubleArray(conExp);
				double [] conPredArray=VectorToDoubleArray(conPred);
				double [] conExpArray2=VectorToDoubleArray(conExp2);
				
				this.outputMethodStats(conExpArray, conPredArray, sheetConsensus,conExp2.size(),methods.size()+8);
				this.OutputPredictionsToTab(methods.size()+4,sheetConsensus, conCAS, conExpArray, conPredArray, conCAS2, conExpArray2);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Output stats for continuous endpoint to the spreadsheet
	 * 
	 * @param expArray
	 * @param predArray
	 * @param sheet
	 * @param countOutsideAD
	 * @param startCol
	 */
	void outputMethodStats(double []expArray,double []predArray,HSSFSheet sheet,int countOutsideAD,int startCol) {
		double []results = this.CalculateR2abs(expArray, predArray);			 
		double R2abs=results[0];
		double k=results[1];
		
		double R2=this.CalculateR2(expArray, predArray);
		double MAE=this.CalculateMAE(expArray, predArray);
		double RMSE=this.CalculateRMSE(expArray, predArray);
		double coverage=(double)expArray.length/((double)expArray.length+(double)countOutsideAD);
		double bob=(R2-R2abs)/R2;
		
//		System.out.println("Method = "+method);
//		System.out.println("R2 = "+R2);
//		System.out.println("(R2-R2abs)/R2 = "+bob);
//		System.out.println("k = "+k);
//		System.out.println("RMSE = "+RMSE);
//		System.out.println("MAE = "+MAE);
//		System.out.println("coverage = "+coverage);
//		System.out.println("");

		//**********************************************************
//		R2		k	RMSE	MAE	Coverage//			
		HSSFRow row=sheet.getRow(0);
					
		int col=startCol;
		HSSFCell cell=row.createCell(col++);
		
		cell.setCellValue("R2");

		cell=row.createCell(col++);
		
		cell.setCellValue("(R2-R2abs)/R2");

		cell=row.createCell(col++);
		
		cell.setCellValue("k");
					
		cell=row.createCell(col++);
		
		cell.setCellValue("RMSE");
		
		cell=row.createCell(col++);
		
		cell.setCellValue("MAE");

		cell=row.createCell(col++);
		
		cell.setCellValue("Coverage");

		// *************************************************
		row=sheet.getRow(1);
		
		col=startCol;
		
		cell=row.createCell(col++);
		
		cell.setCellValue(R2);

		cell=row.createCell(col++);
		
		cell.setCellValue(bob);

		cell=row.createCell(col++);
		
		cell.setCellValue(k);
					
		cell=row.createCell(col++);
		
		cell.setCellValue(RMSE);
		
		cell=row.createCell(col++);
		
		cell.setCellValue(MAE);

		cell=row.createCell(col++);
		
		cell.setCellValue(coverage);

	}
	/**
	 * Calculate consensus value. Possibly just use value from consensus text file in future
	 * 
	 * @param row
	 * @param methods
	 * @return
	 */
	double CalculateConsensus(HSSFRow row,Vector<String> methods) {
		double pred=0;
		
		try {
			
			int predCount=0;
			
			for (int i=0;i<methods.size();i++) {
				HSSFCell cell=row.getCell(i+2);
				
				double val=cell.getNumericCellValue();
				if (val>-9999) {
					predCount++;
					pred+=val;
				}
			}
			
			if (predCount>=minPredCount)
				pred/=(double)(predCount);
			else 
				pred=-9999;

			return pred;
			
		}catch (Exception e) {
//			e.printStackTrace();
			return -9999;
		}
	}
	
	
	/**
	 * Convert a Vector to a double array
	 * 
	 * @param v
	 * @return
	 */
	double [] VectorToDoubleArray(Vector<Double>v) {
		double []array=new double[v.size()];
		
		for (int i=0;i<v.size();i++) {
			array[i]=v.get(i);
		}
		return array;
	}
	
	/**
	 * Write an experimental and predicted value to consensus tab
	 * 
	 * @param sheetConsensus
	 * @param counter
	 * @param methodNumber
	 * @param currentCAS
	 * @param dExp
	 * @param dPred
	 */
	void WriteValToConsensusTab(HSSFSheet sheetConsensus,int counter,int methodNumber,String currentCAS,double dExp,double dPred) {
		HSSFRow row = sheetConsensus.getRow(counter);
		if (row==null) row=sheetConsensus.createRow(counter);
		
		HSSFCell cell = row.getCell(0);
		if (cell==null) {
			cell=row.createCell(0);
			
			cell.setCellValue(currentCAS);
		}
		
		cell = row.getCell(1);
		if (cell==null) {
			cell=row.createCell(1);
			
			cell.setCellValue(dExp);
		}
		
		cell = row.createCell(methodNumber+2);
		
		cell.setCellValue(dPred);

	}
	
	
	/**
	 * Calculate mean absolute error
	 * 
	 * @param exp
	 * @param pred
	 * @return
	 */
	public double CalculateMAE(double [] exp,double [] pred) {
		double MAE=0;
		for (int i=0;i<exp.length;i++) {
			MAE+=Math.abs(exp[i]-pred[i]);
		}
		MAE/=(double)exp.length;
		return MAE;
	}

	/**
	 * Calculate R squared value
	 * @param exp
	 * @param pred
	 * @return
	 */
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


	/**
	 * Calculate R2 where intercept is set to zero
	 * 
	 * @param exp
	 * @param pred
	 * @return
	 */
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
	 * Output exp vs predicted for a given method to the spreadsheet
	 * 
	 * @param startCol
	 * @param sheet
	 * @param CAS
	 * @param expArray
	 * @param predArray
	 * @param CAS2
	 * @param expArray2
	 */
	void OutputPredictionsToTab(int startCol,HSSFSheet sheet,Vector<String>CAS,double []expArray,double[]predArray,Vector<String>CAS2,double []expArray2) {
			
			HSSFRow row =this.rowMaker(sheet, 0);
	
			HSSFCell cell = row.createCell(startCol);
			
			cell.setCellValue("CAS");
			
			cell = row.createCell(startCol+1);
			
			cell.setCellValue("exp");
	
			cell = row.createCell(startCol+2);
			
			cell.setCellValue("pred");
	
	//		System.out.println(sheet.getSheetName()+"\t"+CAS.size()+"\t"+predArray.length);
			
			
			for (int i=0;i<CAS.size();i++) {
	
				
				row =this.rowMaker(sheet, i+1);
				
				
				cell = row.createCell(startCol);
				
				cell.setCellValue(CAS.get(i));
				
				cell = row.createCell(startCol+1);
				
				cell.setCellValue(expArray[i]);
	
				cell = row.createCell(startCol+2);
				
				cell.setCellValue(predArray[i]);
				
				
			}
			
			row =this.rowMaker(sheet, CAS.size()+1);
			row =this.rowMaker(sheet, CAS.size()+2);
			row =this.rowMaker(sheet, CAS.size()+3);
	
			cell = row.createCell(startCol);
			
			cell.setCellValue("Chemicals that are outside AD:");
	
			row =this.rowMaker(sheet, CAS.size()+4);
	
			cell = row.createCell(startCol);
			
			cell.setCellValue("CAS");
			
			cell = row.createCell(startCol+1);
			
			cell.setCellValue("exp");
	
			cell = row.createCell(startCol+2);
			
			cell.setCellValue("pred");
	
			for (int i=0;i<CAS2.size();i++) {	
				row =this.rowMaker(sheet, CAS.size()+5+i);
	
				cell = row.createCell(startCol);
				
				cell.setCellValue(CAS2.get(i));
				
				cell = row.createCell(startCol+1);
				
				cell.setCellValue(expArray2[i]);
	
				cell = row.createCell(startCol+2);
				
				cell.setCellValue(-9999);
			}
	
			
		}


	/**
	 * Calculate root mean squared error
	 * 
	 * @param exp
	 * @param pred
	 * @return
	 */
	private double CalculateRMSE(double [] exp,double [] pred) {
		double RMSE=0;
		for (int i=0;i<exp.length;i++) {
			RMSE+=Math.pow(exp[i]-pred[i],2);
		}
		RMSE=Math.sqrt(RMSE/(double)exp.length);
		return RMSE;
	}


	/**
	 * Create a row in a spreadsheet sheet
	 * 
	 * @param sheet
	 * @param r
	 * @return
	 */
	HSSFRow rowMaker(HSSFSheet sheet,int r) {
		HSSFRow row =sheet.getRow(r);
		if (row==null) row=sheet.createRow(r);
		return row;
	}


	
		/**
		 * Creates summary of different methods
		 * 
		 * @param wb
		 * @param methods
		 * @param endpoint
		 */
		void CreateSummarySheet2(HSSFWorkbook wb,Vector<String> methods,String endpoint) {
			try {
			HSSFSheet sheet = wb.createSheet("Summary");
	
			HSSFFont f = wb.createFont();
			f.setFontHeightInPoints((short) 12);
			f.setFontName("Times New Roman");
			
	
			sheet.setColumnWidth(0, 18*256);
			
			for (int i=1;i<=6;i++) {
				sheet.setColumnWidth(i, 13*256);
			}
	
			short formatD3=wb.createDataFormat().getFormat("0.000");
			
			CellStyle cs=wb.createCellStyle();
			cs.setDataFormat(formatD3);
			cs.setAlignment(HorizontalAlignment.CENTER);
			cs.setFont(f);
			
			short red=IndexedColors.ROSE.index;
			short green=IndexedColors.LIGHT_GREEN.index;
			short orange=IndexedColors.YELLOW.index;
			
			
//			CellStyle csRed=wb.createCellStyle();
//			csRed.setDataFormat(formatD3);
//			csRed.setAlignment(HorizontalAlignment.CENTER);
//			csRed.setFont(f);
//			csRed.setFillForegroundColor(red);
//			csRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			
//			CellStyle csGreen=wb.createCellStyle();
//			csGreen.setDataFormat(formatD3);
//			csGreen.setAlignment(HorizontalAlignment.CENTER);
//			csGreen.setFont(f);
//			csGreen.setFillForegroundColor(green);
//			csGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			
			CellStyle csOrange=wb.createCellStyle();
			csOrange.setDataFormat(formatD3);
			csOrange.setAlignment(HorizontalAlignment.CENTER);
			csOrange.setFont(f);
			csOrange.setFillForegroundColor(orange);
			csOrange.setFillPattern(FillPatternType.SOLID_FOREGROUND);


	
			CellStyle cs2=wb.createCellStyle();
			cs2.setFont(f);
	
			CellStyle csHeader=wb.createCellStyle();
			csHeader.setBorderTop(BorderStyle.MEDIUM);
			csHeader.setBorderBottom(BorderStyle.MEDIUM);
			csHeader.setAlignment(HorizontalAlignment.CENTER);
			csHeader.setFont(f);
			
			CellStyle csBottom=wb.createCellStyle();
			csBottom.setBorderBottom(BorderStyle.MEDIUM);
			csBottom.setAlignment(HorizontalAlignment.CENTER);
			csBottom.setDataFormat(formatD3);
			csBottom.setFont(f);
			
			CellStyle csBottom2=wb.createCellStyle();
			csBottom2.setBorderBottom(BorderStyle.MEDIUM);
			csBottom2.setFont(f);
	//		csBottom2.setAlignment(HorizontalAlignment.CENTER);
	
	
//			CellStyle csBottomRed=wb.createCellStyle();
//			csBottomRed.setBorderBottom(BorderStyle.MEDIUM);
//			csBottomRed.setAlignment(HorizontalAlignment.CENTER);
//			csBottomRed.setDataFormat(formatD3);
//			csBottomRed.setFont(f);
//			csBottomRed.setFillForegroundColor(red);
//			csBottomRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			
//			CellStyle csBottomGreen=wb.createCellStyle();
//			csBottomGreen.setBorderBottom(BorderStyle.MEDIUM);
//			csBottomGreen.setAlignment(HorizontalAlignment.CENTER);
//			csBottomGreen.setDataFormat(formatD3);
//			csBottomGreen.setFont(f);
//			csBottomGreen.setFillForegroundColor(green);
//			csBottomGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	
			CellStyle csBottomOrange=wb.createCellStyle();
			csBottomOrange.setBorderBottom(BorderStyle.MEDIUM);
			csBottomOrange.setAlignment(HorizontalAlignment.CENTER);
			csBottomOrange.setDataFormat(formatD3);
			csBottomOrange.setFont(f);
			csBottomOrange.setFillForegroundColor(orange);
			csBottomOrange.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			
			//Creating header row:
			HSSFRow row = sheet.createRow(0);
			
			HSSFCell cell=row.createCell(0);
			
			cell.setCellValue("Method");
			cell.setCellStyle(csHeader);
			
			
			cell=row.createCell(1);
			
			cell.setCellValue("R2");
			cell.setCellStyle(csHeader);
	
			cell=row.createCell(2);
			
			cell.setCellValue("(R2-R2abs)/R2");
			cell.setCellStyle(csHeader);
	
			cell=row.createCell(3);
			
			cell.setCellValue("k");
			cell.setCellStyle(csHeader);
						
			cell=row.createCell(4);
			
			cell.setCellValue("RMSE");
			cell.setCellStyle(csHeader);
			
			cell=row.createCell(5);
			
			cell.setCellValue("MAE");
			cell.setCellStyle(csHeader);
	
			cell=row.createCell(6);
			
			cell.setCellValue("Coverage");
			cell.setCellStyle(csHeader);
			
			
			
	//		List<String> l=HSSFDataFormat.getBuiltinFormats();
	//		for (int i=0;i<l.size();i++) {
	//			System.out.println(i+"\t"+l.get(i));
	//		}
			
			
			for (int i=0;i<methods.size();i++) {
				HSSFSheet sheetMethod = wb.getSheet(methods.get(i));
	
				row = sheet.createRow(i+1);
				cell=row.createCell(0);
				
				cell.setCellValue(methods.get(i));
				cell.setCellStyle(cs2);
	
				HSSFRow rowMethod = sheetMethod.getRow(1);
				
				for (int j=1;j<=6;j++) {
					double val=rowMethod.getCell(j+3).getNumericCellValue();
					cell=row.createCell(j);
					
					cell.setCellValue(val);
	
	//				System.out.println(methods.get(i)+"\t"+val);
					
					if (j==1) {//r2
						if (val>0.6) {//eqn 18 in TEST user guide: model is ok if R2 > 0.6
							cell.setCellStyle(cs);
						} else {
							cell.setCellStyle(csOrange);
						}
					} else if (j==2) {//(R2-R2abs)/R2
						if (val<0.1) {//eqn 19 in TEST user guide: model is ok if (R2-R2abs)/R2 < 0.1 
							cell.setCellStyle(cs);
						} else {
							cell.setCellStyle(csOrange);
						}
					} else if (j==3) {//k
						if (val>=0.85 && val <=1.15) {//eqn 19 in TEST user guide: model is ok if 0.85 ≤ k ≤ 1.15	 
							cell.setCellStyle(cs);
						} else {
							cell.setCellStyle(csOrange);
						}
						
					} else {
						cell.setCellStyle(cs);
					}
					
				} // end stat j loop
			}//end methods i loop
			
			HSSFSheet sheetConsensus = wb.getSheet("Consensus");
			
			HSSFRow rowConsensus = sheetConsensus.getRow(1);
			row = sheet.createRow(methods.size()+1);
	
			cell=row.createCell(0);
			
			cell.setCellValue("Consensus");
			cell.setCellStyle(csBottom2);
			
			for (int j=1;j<=6;j++) {
				double val=rowConsensus.getCell(j+methods.size()+7).getNumericCellValue();
				cell=row.createCell(j);
				
				cell.setCellValue(val);
	//			cell.setCellStyle(csBottom);
				
				if (j==1) {//r2
					if (val>0.6) {//eqn 18 in TEST user guide: model is ok if R2 > 0.6
						cell.setCellStyle(csBottom);
					} else {
						cell.setCellStyle(csBottomOrange);
					}
				} else if (j==2) {//(R2-R2abs)/R2
					if (val<0.1) {//eqn 19 in TEST user guide: model is ok if (R2-R2abs)/R2 < 0.1 
						cell.setCellStyle(csBottom);
					} else {
						cell.setCellStyle(csBottomOrange);
					}
				} else if (j==3) {//k
					if (val>=0.85 && val <=1.15) {//eqn 19 in TEST user guide: model is ok if 0.85 ≤ k ≤ 1.15	 
						cell.setCellStyle(csBottom);
					} else {
						cell.setCellStyle(csBottomOrange);
					}
					
				} else {
					cell.setCellStyle(csBottom);
				}
	
			} // end stat j loop
			
			// *****************************************************
			
			
			CellStyle csCenter=wb.createCellStyle();
			csCenter.setAlignment(HorizontalAlignment.CENTER);
	
			row = sheet.createRow(methods.size()+2);
			row = sheet.createRow(methods.size()+3);
			cell=row.createCell(0);
			
			cell.setCellValue("Endpoint");
	
			cell=row.createCell(1);
			
			cell.setCellValue(endpoint);
			cell.setCellStyle(csCenter);
//	
//			row = sheet.createRow(methods.size()+4);
//			cell=row.createCell(0);
//			
//			cell.setCellValue("RndRun#");
			
//			cell=row.createCell(1);
//			
//			cell.setCellValue(rndRun);
//			cell.setCellStyle(csCenter);
	
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			
			
		}

		
		
		
		/**
		 * Create plot of experimental versus predicted
		 * 
		 * @param endpoint
		 * @param resultsFilePath
		 * @param delimiter
		 * @param badCAS
		 * @param imageFilePath
		 */
		public void CreatePlot(String endpoint,String resultsFilePath, String delimiter,Vector<String>badCAS,String imageFilePath) {
			try {

				String expColumnName="expToxicValue";
				BufferedReader br = new BufferedReader(new FileReader(resultsFilePath));

				String header = br.readLine();

				int colCAS=1;
				int colExp=2;
				int colPred=3;

				Vector<Double>expVec=new Vector<Double>();
				Vector<Double>predVec=new Vector<Double>();
				
				while (true) {
					String Line = br.readLine();
					if (!(Line instanceof String))
						break;

					java.util.List<String> l = ToxPredictor.Utilities.Utilities
							.Parse(Line, delimiter);
					
					String CAS=l.get(colCAS);
					String strExp=l.get(colExp);
					String strPred=l.get(colPred);
					
//					System.out.println(CAS);
					
					if (badCAS.contains(CAS)) {
						System.out.println(CAS+"\tbadCAS");
					}
					
					if (strPred.equals("N/A"))continue;

					double exp=Double.parseDouble(strExp);
					double pred=Double.parseDouble(strPred);
					
					if (pred==-9999) continue;
					
					expVec.add(exp);
					predVec.add(pred);
					
//					System.out.println(exp+"\t"+pred);
					
				}
				
				double[] x = new double[expVec.size()];
				double[] y = new double[expVec.size()];

				for (int i = 0; i < expVec.size(); i++) {
					x[i] = expVec.get(i);
					y[i] = predVec.get(i);
				}

				
				String axistitle;
				
				if (TESTConstants.isLogMolar(endpoint)) {
					axistitle=endpoint+" "+ TESTConstants.getMolarLogUnits(endpoint);
				} else {
					axistitle=endpoint+" "+ TESTConstants.getMassUnits(endpoint);
				}

				String xtitle="Exp. "+axistitle;
				String ytitle="Pred. "+axistitle;
//				String title="Test set predictions for the "+methodColumnName+" method";
				String title="External prediction results";
				fraChart.JLabelChart fc = new fraChart.JLabelChart(x,y,title,xtitle,ytitle);

				fc.doDrawLegend=true;
				fc.doDrawStatsR2=false;
				fc.doDrawStatsMAE=false;
				fc.WriteImageToFile(imageFilePath);
				
				br.close();
				

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		

	public static void main(String[] args) {
		CreateQSARModelStats c=new CreateQSARModelStats();
		
		String [] endpoints=TESTConstants.getFullEndpoints(null);
		
		String resultsFolder="test-results";
		
		//Chemicals to not include in results:
		Vector<String>omitCAS=new Vector<String>();
		omitCAS.add("74-82-8");//methane
		omitCAS.add("74-90-8");//hydrogen cyanide
		omitCAS.add("409-21-2");//silicon carbide
		omitCAS.add("7440-44-0");//carbon
		omitCAS.add("7276-58-6");//Benzimidazolium, tris(1-dodecyl-3-methyl-2-phenyl-, ferricyanide
		omitCAS.add("25590-60-7");//1-Nitropropane nitronate (ion)
		omitCAS.add("85060-03-3");// 2-Nitro-1-butanol nitronate (ion)
		omitCAS.add("630-08-0");//carbon monoxide
		omitCAS.add("85060-01-1");// 2-Butanol, 3-nitro-, ion(1-)
		omitCAS.add("18137-96-7");//methanenitronate (ion)
		omitCAS.add("20846-00-8");//nitronate (ion)
		omitCAS.add("25590-58-3");//nitronate (ion)
		omitCAS.add("34430-24-5");//nitronate (ion)
		
		for (int i=0;i<endpoints.length;i++) {
			
			if (endpoints[i].equals("Estrogen Receptor RBA")) continue;

			c.createRunSpreadsheet(endpoints[i],resultsFolder,omitCAS);

			if (!TESTConstants.isBinary(endpoints[i])) {
				String method="consensus";
				String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoints[i]);
				String resultsFilePath=resultsFolder+"/"+endpoints[i]+"/"+method+"/"+endpointAbbrev+"_prediction.txt";
				String imageFilePath=resultsFolder+"/"+endpoints[i]+"/"+endpoints[i]+"_consensus.png";
				c.CreatePlot(endpoints[i], resultsFilePath, "\t", omitCAS, imageFilePath);
			}	
		}
		
		//Run one endpoint:
//		c.createRunSpreadsheet(TESTConstants.ChoiceBCF, "test-results",badCAS);

	}


	/**
	 * Output stats for binary endpoint to the spreadsheet
	 * 
	 * @param expArray
	 * @param predArray
	 * @param sheet
	 * @param countOutsideAD
	 * @param startCol
	 */
	void outputMethodStatsCancer(double []expArray,double []predArray,HSSFSheet sheet,int countOutsideAD,int startCol) {
			double [] results=this.CalculateCancerStats(0.5, expArray, predArray);
	
			double concordance=results[0];
			double sensitivity=results[1];
			double specificity=results[2];
			
			double RMSE=-1;
			double coverage=(double)expArray.length/((double)expArray.length+(double)countOutsideAD);
			
			double k=-1;
			
	//		System.out.println("R2 = "+R2);
	//		System.out.println("(R2-R2abs)/R2 = "+bob);
	//		System.out.println("k = "+k);
	//		System.out.println("RMSE = "+RMSE);
	//		System.out.println("MAE = "+MAE);
	//		System.out.println("coverage = "+coverage);
	//		System.out.println("");
	
			//**********************************************************
	//		R2		k	RMSE	MAE	Coverage//			
			HSSFRow row=sheet.getRow(0);
						
			int col=startCol;
			HSSFCell cell=row.createCell(col++);
			
			cell.setCellValue("Concordance");
	
			cell=row.createCell(col++);
			
			cell.setCellValue("Sensitivity");
	
			cell=row.createCell(col++);
			
			cell.setCellValue("Specificity");
						
			cell=row.createCell(col++);
			
			cell.setCellValue("Coverage");
	
			// *************************************************
			row=sheet.getRow(1);
			
			col=startCol;
			
			cell=row.createCell(col++);
			
			cell.setCellValue(concordance);
	
			cell=row.createCell(col++);
			
			cell.setCellValue(sensitivity);
	
			cell=row.createCell(col++);
			
			cell.setCellValue(specificity);
	
			cell=row.createCell(col++);
			
			cell.setCellValue(coverage);
	
		}


	
	/**
	 *
	 * calculates cancer stats (expArray and predArray have compounds outside the AD omitted)
	 * 
	 * @param cutoff
	 * @param expArray
	 * @param predArray
	 * @return
	 */
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
				
	//			if (strExp.equals("C"))
	//				System.out.println(exp+"\t"+pred+"\t"+strExp+"\t"+strPred);
	
				
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
	
	//		System.out.println("posConc="+posConcordance);
			
			results[0]=concordance;
			results[1]=posConcordance;
			results[2]=negConcordance;
			
			return results;
		}



}
