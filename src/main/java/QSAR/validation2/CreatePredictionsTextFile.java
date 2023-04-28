package QSAR.validation2;


import java.text.DecimalFormat;
import java.util.*;
//import java.awt.Font;
import java.io.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import ToxPredictor.Application.TESTConstants;

import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFColor;


/**
 * This class goes through all prediction files for the different qsar 
 * methods for a given endpoint and generates a text file
 * 
 * 
 * @author TMARTI02
 *
 */


public class CreatePredictionsTextFile {
//	String mainFolder="C:/Documents and Settings/tmarti02/My Documents/comptox/TEST/T.E.S.T. deployment 4.1";
	String mainOutputFolder;
	
	String overallSetsFolder;
	String validationCalcsFolder;
	String destSDFFolderPath;
	
	String mainDataFolder;
	String descriptorTextTablesFolder;
	String qsarFolder;
	
	boolean includeHierarchical=true;
	boolean includeSingleModel=false;
	boolean includeFDA=false;
	boolean includeFrag=true;
	boolean includeNN=true;
	boolean includekNN=true;
	

	int minPredCount=2;//min number of predictions for consensus method
	
	
	void setFolders(String mainOutputFolder,String mainDataFolder) {
		this.mainOutputFolder=mainOutputFolder;
		this.mainDataFolder=mainDataFolder;
		
		overallSetsFolder=mainOutputFolder+"/Overall Sets";
		validationCalcsFolder=mainOutputFolder+"/validation calcs";
		destSDFFolderPath=mainOutputFolder+"/web page/DataSets";
		
		descriptorTextTablesFolder=mainDataFolder+"\\ToxPredictor\\DescriptorTextTables";
		qsarFolder=mainDataFolder+"\\QSAR";


	}
	
	void CopyFiles (String endpoint, String desc,int run) {
		
		String destFolderPath=this.overallSetsFolder+"/"+endpoint;
		File DestFolder=new File(destFolderPath);
		if (!DestFolder.exists()) DestFolder.mkdir();
		
		File DestSDFFolder=new File(this.destSDFFolderPath);
		if (!DestSDFFolder.exists()) DestSDFFolder.mkdir();
		
		
		String f1=descriptorTextTablesFolder+"/"+endpoint+desc+" Data Files";
		
		String filePath1=f1+"/2d/"+endpoint+"_training_set-2d-rnd"+run+".csv";
		File File1=new File(filePath1);
		String newFilePath1=destFolderPath+"/"+endpoint+"_training_set-2d.csv";
		File newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing: "+filePath1);
		

		filePath1=f1+"/2d/"+endpoint+"_prediction_set-2d-rnd"+run+".csv";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_prediction_set-2d.csv";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing: "+filePath1);
		
		filePath1=f1+"/2d/"+endpoint+"_training_set-2d-rnd"+run+".xml";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_training_set-2d.xml";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing: "+filePath1);
		
		filePath1=f1+"/frag/"+endpoint+"_training_set-frag-rnd"+run+".csv";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_training_set-frag.csv";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing: "+filePath1);
		
		filePath1=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/run"+run+"/results.xml";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_training_set-frag.xml";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing: "+filePath1);
		
		filePath1=f1+"/sdf/"+endpoint+"_training_set-2d-rnd"+run+".sdf";
		File1=new File(filePath1);		
		newFilePath1=destSDFFolderPath+"/"+endpoint+"_training.sdf";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		File newFile2=new File(destFolderPath+"/"+endpoint+"_training.sdf");
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile2);
		else System.out.println("missing: "+filePath1);
		
		filePath1=f1+"/sdf/"+endpoint+"_prediction_set-2d-rnd"+run+".sdf";
		File1=new File(filePath1);
		newFilePath1=destSDFFolderPath+"/"+endpoint+"_prediction.sdf";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		newFile2=new File(destFolderPath+"/"+endpoint+"_prediction.sdf");
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile2);
		else System.out.println("missing: "+filePath1);
	}
	
	void CopyFilesNoRnd (String endpoint,String desc) {
		

		String destFolderPath=this.overallSetsFolder+"/"+endpoint;
		File DestFolder=new File(destFolderPath);
		if (!DestFolder.exists()) DestFolder.mkdir();
		
		File DestSDFFolder=new File(this.destSDFFolderPath);
		if (!DestSDFFolder.exists()) DestSDFFolder.mkdir();

		
		String f1=this.descriptorTextTablesFolder+"/"+endpoint+desc+" Data Files";
		
		String filePath1=f1+"/2d/"+endpoint+"_training_set-2d.csv";
		File File1=new File(filePath1);
		String newFilePath1=destFolderPath+"/"+endpoint+"_training_set-2d.csv";
		File newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing:"+File1.getAbsolutePath());

		filePath1=f1+"/2d/"+endpoint+"_prediction_set-2d.csv";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_prediction_set-2d.csv";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing:"+File1.getAbsolutePath());
		
		filePath1=f1+"/2d/"+endpoint+"_training_set-2d.xml";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_training_set-2d.xml";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing:"+File1.getAbsolutePath());
		
		filePath1=f1+"/frag/"+endpoint+"_training_set-frag.csv";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_training_set-frag.csv";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing:"+File1.getAbsolutePath());

		filePath1=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/results.xml";
		File1=new File(filePath1);
		newFilePath1=destFolderPath+"/"+endpoint+"_training_set-frag.xml";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);

		filePath1=f1+"/sdf/"+endpoint+"_training_set-2d.sdf";
		File1=new File(filePath1);		
		newFilePath1=destSDFFolderPath+"/"+endpoint+"_training.sdf";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing:"+File1.getAbsolutePath());
		
		File newFile2=new File(destFolderPath+"/"+endpoint+"_training.sdf");
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile2);
		
		
		filePath1=f1+"/sdf/"+endpoint+"_prediction_set-2d.sdf";
		File1=new File(filePath1);
		newFilePath1=destSDFFolderPath+"/"+endpoint+"_prediction.sdf";
		newFile1=new File(newFilePath1);
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile1);
		else System.out.println("missing:"+File1.getAbsolutePath());
		
		newFile2=new File(destFolderPath+"/"+endpoint+"_prediction.sdf");
		if (File1.exists()) ToxPredictor.Utilities.Utilities.CopyFile(File1, newFile2);
		

	}

	void createRunSpreadsheet(String endpoint,String desc,int run,Vector<String>badCAS) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
		Vector<String> filePaths=new Vector<String>();
		Vector<String> methods=new Vector<String>();

		String f=this.validationCalcsFolder+"/run summary";
		File F=new File(f);
		if (!F.exists()) F.mkdir();
		
		String outputFilePath=f+"/"+endpoint+desc+".xls";
		
		File of=new File(outputFilePath);
		if (of.exists()) of.delete();
		
		String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/run"+run+"/prediction.txt";
		String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/run"+run+"/run"+run+"-fitvalues.txt";
		String fileNamekNN=qsarFolder+"/resultskNN/"+endpoint+desc+"/trialuseOverallAD=false q2min=0.5 useSquareDistancesInWeights=true removeOutliers=false Z=2.0/run"+run+"/run"+run+"-fitvalues.txt";

		if(includeHierarchical) {
			if (!new File(fileNameHierarchical).exists()) System.out.println("missing:"+fileNameHierarchical);
			else {
				filePaths.add(fileNameHierarchical);
				methods.add("Hierarchical");
			}
		}
		if(includeSingleModel) {
			if (!new File(fileNameSingle).exists()) System.out.println("missing:"+fileNameSingle);
			else {
				filePaths.add(fileNameSingle);
				methods.add("SingleModel");
			}
		}
		if(includeFDA) {
			if (!new File(fileNameFDA).exists()) System.out.println("missing:"+fileNameFDA);
			else {
				filePaths.add(fileNameFDA);
				methods.add("FDA");
			}
		}
		if(includeFrag) {
			if (!new File(fileNameFrag).exists()) System.out.println("missing:"+fileNameFrag);
			else {
				filePaths.add(fileNameFrag);
				methods.add("GC");
			}
		}
		if(includeNN) {
			if (!new File(fileNameNN).exists()) System.out.println("missing:"+fileNameNN);
			else {
				filePaths.add(fileNameNN);
				methods.add("NN");
			}
			
		}
		if(includekNN) {
			if (!new File(fileNamekNN).exists()) System.out.println("missing:"+fileNamekNN);
			else {
				filePaths.add(fileNamekNN);
				methods.add("kNN");
			}
		}
		
		
		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			HSSFWorkbook wb = new HSSFWorkbook();
			
			Vector<String> methods2=new Vector<String>();

			
			for (int i=0;i<filePaths.size();i++) {
				File file=new File (filePaths.get(i));
				if (file.exists()) {
					methods2.add(methods.get(i));
//					System.out.println(methods.get(i));
					readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
				} else {
//					System.out.println("missing:"+filePaths.get(i));
//					readers.add(null);
				}
				
			}
			
			
			for (int i=0;i<readers.size();i++) {
				BufferedReader br=readers.get(i);
				this.GetRunData(methods2,i,br,wb,badCAS);
			}
						
			for (int i=0;i<readers.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}
			

			
			this.CreateSummarySheet2(wb,methods2,run,endpoint,desc);
			
			FileOutputStream fOut = new FileOutputStream(outputFilePath);
			// Write the XL sheet
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
	
	void createRunSpreadsheetHierarchical(String endpoint,String desc,int run) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
		
		Vector<String> methods=new Vector<String>();

		String f=this.validationCalcsFolder+"/run summary";
		File F=new File(f);
		if (!F.exists()) F.mkdir();
		
		String outputFilePath=f+"/"+endpoint+desc+".xls";
		
		File of=new File(outputFilePath);
		if (of.exists()) of.delete();
		
		String filePath=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/run"+run+"/run"+run+"-fitvalues.txt";

		methods.add("Hierarchical"); 
		
		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			HSSFWorkbook wb = new HSSFWorkbook();
			
			Vector<String> methods2=new Vector<String>();

			BufferedReader br=new BufferedReader(new FileReader(filePath));

			this.GetRunDataHierarchical(br,wb,run,desc);
			
			br.close();
						
			FileOutputStream fOut = new FileOutputStream(outputFilePath);
			// Write the XL sheet
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

	
	void createRunSpreadsheetCancer(String endpoint,String desc,int run,Vector<String>badCAS) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
		Vector<String> filePaths=new Vector<String>();
		Vector<String> methods=new Vector<String>();

		String f=this.validationCalcsFolder+"/run summary";
		String outputFilePath=f+"/"+endpoint+desc+".xls";
		
		File of=new File(outputFilePath);
		if (of.exists()) of.delete();
		
		String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/run"+run+"/cancerstats.txt";
		String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/run"+run+"/cancerstats.txt";
		String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/run"+run+"/cancerstats.txt";
		String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/run"+run+"/cancerstats.txt";
		String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/run"+run+"/cancerstats.txt";

		filePaths.add(fileNameHierarchical);
		filePaths.add(fileNameSingle);
		filePaths.add(fileNameFDA);
		filePaths.add(fileNameFrag);
		filePaths.add(fileNameNN);
		
		methods.add("Hierarchical"); 
		methods.add("Single Model");
		methods.add("FDA");
		methods.add("Group contribution");
		methods.add("Nearest neighbor");
		
		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			HSSFWorkbook wb = new HSSFWorkbook();
			
			Vector<String> methods2=new Vector<String>();

			
			for (int i=0;i<filePaths.size();i++) {
				File file=new File (filePaths.get(i));
				if (file.exists()) {
					methods2.add(methods.get(i));
//					System.out.println(methods.get(i));
					readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
				} else {
//					System.out.println("missing:"+filePaths.get(i));
//					readers.add(null);
				}
				
			}
			
			
			for (int i=0;i<readers.size();i++) {
				BufferedReader br=readers.get(i);
				this.GetRunDataCancer(methods2,i,br,wb,badCAS);
			}
						
			for (int i=0;i<readers.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}
			

			
			this.CreateSummarySheet2Cancer(wb,methods2,run,endpoint,desc);
			
			FileOutputStream fOut = new FileOutputStream(outputFilePath);
			// Write the XL sheet
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

	
	
	
	//used to create spreadsheet which helps choose which rnd run to use
	
	void createRndSummarySpreadsheet(String endpoint,String desc,int numRndSets) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
		Vector<String> filePaths=new Vector<String>();
		Vector<String> methods=new Vector<String>();

		String f=this.validationCalcsFolder+"/select random run";
		File Folder=new File(f);
		if (!Folder.exists()) Folder.mkdir();
		
		String outputFilePath=f+"/"+endpoint+desc+" random calcs.xls";
		
		File of=new File(outputFilePath);
		if (of.exists()) of.delete();
		
		String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/runinfo-1-"+numRndSets+".txt";
		String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/runinfo-1-"+numRndSets+".txt";
		String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/runinfo-1-"+numRndSets+".txt";
		String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/runinfo-1-"+numRndSets+".txt";
		String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/runinfo-1-"+numRndSets+".txt";
		String fileNamekNN=qsarFolder+"/resultskNN/"+endpoint+desc+"/trialuseOverallAD=false q2min=0.5 useSquareDistancesInWeights=true removeOutliers=false Z=2.0/runinfo-1-"+numRndSets+".txt";

		
		if(includeHierarchical) {
			if (!new File(fileNameHierarchical).exists()) System.out.println("missing:"+fileNameHierarchical);
			else {
				filePaths.add(fileNameHierarchical);
				methods.add("Hierarchical");
			}
		}
		if(includeSingleModel) {
			if (!new File(fileNameSingle).exists()) System.out.println("missing:"+fileNameSingle);
			else {
				filePaths.add(fileNameSingle);
				methods.add("SingleModel");
			}
		}
		if(includeFDA) {
			if (!new File(fileNameFDA).exists()) System.out.println("missing:"+fileNameFDA);
			else {
				filePaths.add(fileNameFDA);
				methods.add("FDA");
			}
		}
		if(includeFrag) {
			if (!new File(fileNameFrag).exists()) System.out.println("missing:"+fileNameFrag);
			else {
				filePaths.add(fileNameFrag);
				methods.add("GC");
			}
		}
		if(includeNN) {
			if (!new File(fileNameNN).exists()) System.out.println("missing:"+fileNameNN);
			else {
				filePaths.add(fileNameNN);
				methods.add("NN");
			}
			
		}
		if(includekNN) {
			if (!new File(fileNamekNN).exists()) System.out.println("missing:"+fileNamekNN);
			else {
				filePaths.add(fileNamekNN);
				methods.add("kNN");
			}
		}

		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			HSSFWorkbook wb = new HSSFWorkbook();
			
			
			for (int i=0;i<filePaths.size();i++) {
				File file=new File (filePaths.get(i));
				if (file.exists()) {
					readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
				} else {
					System.out.println("missing:"+filePaths.get(i));
					readers.add(null);
				}
				
			}
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);				
				String method=methods.get(i);
//				System.out.println(method);
				if (br!=null) {
					this.GetRandomRunData(method,br, wb,numRndSets);
				}
			}
			
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}
			
			this.CreateSummarySheet(wb,methods,numRndSets);
			
			FileOutputStream fOut = new FileOutputStream(outputFilePath);
			// Write the XL sheet
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
	
	//used to create spreadsheet which helps choose which rnd run to use
	
	void createRndSummarySpreadsheetCancer(String endpoint,String desc,int numRndSets) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
		Vector<String> filePaths=new Vector<String>();
		Vector<String> methods=new Vector<String>();

		String f=this.validationCalcsFolder+"/select random run";
		String outputFilePath=f+"/"+endpoint+desc+" random calcs.xls";
		
		File of=new File(outputFilePath);
		if (of.exists()) of.delete();
		
		String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/stats.txt";
		String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/stats.txt";
		String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/stats.txt";
		String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/stats.txt";
		String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/stats.txt";

		filePaths.add(fileNameHierarchical);
		filePaths.add(fileNameSingle);
		filePaths.add(fileNameFDA);
		filePaths.add(fileNameFrag);
		filePaths.add(fileNameNN);
		
		methods.add("Hierarchical");
		methods.add("SingleModel");
		methods.add("FDA");
		methods.add("GC");
		methods.add("NN");
		
		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			HSSFWorkbook wb = new HSSFWorkbook();
			
			
			for (int i=0;i<filePaths.size();i++) {
				File file=new File (filePaths.get(i));
				if (file.exists()) {
					readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
				} else {
					System.out.println("missing:"+filePaths.get(i));
					readers.add(null);
				}
				
			}
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				String method=methods.get(i);
				if (br!=null) {
					this.GetRandomRunDataCancer(method,br, wb);
				}
			}
			
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}
			
			this.CreateSummarySheetCancer(wb,methods,numRndSets);
			
			FileOutputStream fOut = new FileOutputStream(outputFilePath);
			// Write the XL sheet
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


	
	//creates summary sheet for different methods for a given rnd run
	
	void CreateSummarySheet2(HSSFWorkbook wb,Vector<String> methods,int rndRun,String endpoint,String desc) {
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
		

		CellStyle csRed=wb.createCellStyle();
		csRed.setDataFormat(formatD3);
		csRed.setAlignment(HorizontalAlignment.CENTER);
		csRed.setFont(f);
		csRed.setFillForegroundColor(red);
		csRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		
		CellStyle csGreen=wb.createCellStyle();
		csGreen.setDataFormat(formatD3);
		csGreen.setAlignment(HorizontalAlignment.CENTER);
		csGreen.setFont(f);
		csGreen.setFillForegroundColor(green);
		csGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

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


		CellStyle csBottomRed=wb.createCellStyle();
		csBottomRed.setBorderBottom(BorderStyle.MEDIUM);
		csBottomRed.setAlignment(HorizontalAlignment.CENTER);
		csBottomRed.setDataFormat(formatD3);
		csBottomRed.setFont(f);
		csBottomRed.setFillForegroundColor(red);
		csBottomRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);


		CellStyle csBottomGreen=wb.createCellStyle();
		csBottomGreen.setBorderBottom(BorderStyle.MEDIUM);
		csBottomGreen.setAlignment(HorizontalAlignment.CENTER);
		csBottomGreen.setDataFormat(formatD3);
		csBottomGreen.setFont(f);
		csBottomGreen.setFillForegroundColor(green);
		csBottomGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		
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
						cell.setCellStyle(csGreen);
					} else {
						cell.setCellStyle(csRed);
					}
				} else if (j==2) {//(R2-R2abs)/R2
					if (val<0.1) {//eqn 19 in TEST user guide: model is ok if (R2-R2abs)/R2 < 0.1 
						cell.setCellStyle(csGreen);
					} else {
						cell.setCellStyle(csRed);
					}
				} else if (j==3) {//k
					if (val>=0.85 && val <=1.15) {//eqn 19 in TEST user guide: model is ok if 0.85 ≤ k ≤ 1.15	 
						cell.setCellStyle(csGreen);
					} else {
						cell.setCellStyle(csRed);
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
					cell.setCellStyle(csBottomGreen);
				} else {
					cell.setCellStyle(csBottomRed);
				}
			} else if (j==2) {//(R2-R2abs)/R2
				if (val<0.1) {//eqn 19 in TEST user guide: model is ok if (R2-R2abs)/R2 < 0.1 
					cell.setCellStyle(csBottomGreen);
				} else {
					cell.setCellStyle(csBottomRed);
				}
			} else if (j==3) {//k
				if (val>=0.85 && val <=1.15) {//eqn 19 in TEST user guide: model is ok if 0.85 ≤ k ≤ 1.15	 
					cell.setCellStyle(csBottomGreen);
				} else {
					cell.setCellStyle(csBottomRed);
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
		
		cell.setCellValue("RunFolder");

		cell=row.createCell(1);
		
		cell.setCellValue(endpoint+desc);
		cell.setCellStyle(csCenter);

		row = sheet.createRow(methods.size()+4);
		cell=row.createCell(0);
		
		cell.setCellValue("RndRun#");
		
		cell=row.createCell(1);
		
		cell.setCellValue(rndRun);
		cell.setCellStyle(csCenter);

		} catch (Exception e) {
			e.printStackTrace();
		}
			
		
		
	}
	
	//creates summary sheet for different methods for a given rnd run
	
	void CreateSummarySheet2Cancer(HSSFWorkbook wb,Vector<String> methods,int rndRun,String endpoint,String desc) {
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
		
		cell.setCellValue("RunFolder");

		cell=row.createCell(1);
		
		cell.setCellValue(endpoint+desc);
		cell.setCellStyle(csCenter);

		row = sheet.createRow(methods.size()+4);
		cell=row.createCell(0);
		
		cell.setCellValue("RndRun#");
		
		cell=row.createCell(1);
		
		cell.setCellValue(rndRun);
		cell.setCellStyle(csCenter);

		} catch (Exception e) {
			e.printStackTrace();
		}
			
		
		
	}

	
	

	
	void CreateSummarySheet(HSSFWorkbook wb,Vector<String> methods,int numRndSets) {
		
		
		HSSFSheet sheet = wb.createSheet("Summary");

		//Creating header row:
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
		
		cell.setCellValue("");

		cell = row.createCell(1);
		
		cell.setCellValue("MAE");
		
		row = sheet.createRow(1);
		
		cell = row.createCell(0);
		
		cell.setCellValue("Run");
		
		for (int i=1;i<=methods.size();i++) {
			cell = row.createCell(i);
			
			cell.setCellValue(methods.get(i-1));
		}

		cell = row.createCell(methods.size()+1);
		
		cell.setCellValue("Avg");
		
		cell = row.createCell(methods.size()+2);
		
		cell.setCellValue("Diff from global Avg");

		
		//***********************************************************************
		double minAvgMAE=9999;
		int minAvgRun=-1;

		for (int i=1;i<=numRndSets;i++) {
			row = sheet.createRow(i+1);

			cell = row.createCell(0);
			
			cell.setCellValue(i);
			
			for (int j=0;j<methods.size();j++) {
				cell = row.createCell(j+1);
				
				if (wb.getSheet(methods.get(j))!=null)
					cell.setCellFormula(methods.get(j)+"!E"+(i+1));
				else 
					cell.setCellValue("N/A");
			}

			cell = row.createCell(methods.size()+1);
			
			cell.setCellFormula("AVERAGE(B"+(i+2)+":F"+(i+2)+")");
			
			cell = row.createCell(methods.size()+2);
			
			cell.setCellFormula("ABS(G"+(i+2)+"-$G$"+(numRndSets+3)+")");//TODO add code to select column based on # of methods!
		}
		
		row = sheet.createRow(numRndSets+2);
		cell = row.createCell(0);
		
		cell.setCellValue("Avg");
		
		for (int j=0;j<methods.size();j++) {
			cell = row.createCell(j+1);
			
			if (wb.getSheet(methods.get(j))!=null) {
				
				cell.setCellFormula(methods.get(j)+"!E"+(numRndSets+2));
			} else {
				
			}
		}
		
		cell = row.createCell(methods.size()+1);
		
		cell.setCellFormula("AVERAGE(B"+(numRndSets+3)+":F"+(numRndSets+3)+")");//TODO add code if F isnt last column

		row = sheet.createRow(numRndSets+3);
		row = sheet.createRow(numRndSets+4);
		
		for (int i=0;i<methods.size();i++) {
									
			row = sheet.createRow(numRndSets+5+i);
			cell = row.createCell(0);
//			
			cell.setCellValue(methods.get(i));
			
			cell = row.createCell(1);
			
			if (wb.getSheet(methods.get(i))==null) {
//				
				cell.setCellValue("N/A");
			} else {
//				
				cell.setCellFormula(methods.get(i)+"!B"+(numRndSets+4));	
			}
			
		}
		
		
		row = sheet.createRow(numRndSets+methods.size()+5);
		cell = row.createCell(0);
//		
		cell.setCellValue("Overall avg");

		cell = row.createCell(1);
//		
		cell.setCellFormula("MATCH(MIN(H3:H7),$H$3:H7,0)");//TODO: make based on number of sets and methods
		
		
	}
	void CreateSummarySheetCancer(HSSFWorkbook wb,Vector<String> methods,int numRndSets) {

		
		HSSFSheet sheet = wb.createSheet("Summary");

		//Creating header row:
		HSSFRow row = sheet.createRow(0);
		HSSFCell cell = row.createCell(0);
//		
		cell.setCellValue("");

		cell = row.createCell(1);
//		
		cell.setCellValue("Concordance");
		
		row = sheet.createRow(1);
		
		cell = row.createCell(0);
//		
		cell.setCellValue("Run");
		
		for (int i=1;i<=methods.size();i++) {
			cell = row.createCell(i);
			//
			cell.setCellValue(methods.get(i-1));
		}

		cell = row.createCell(methods.size()+1);
		//
		cell.setCellValue("Avg");
		
		cell = row.createCell(methods.size()+2);
		//
		cell.setCellValue("Diff from global Avg");

		
		//***********************************************************************
		double minAvgMAE=9999;
		int minAvgRun=-1;

		for (int i=1;i<=numRndSets;i++) {
			row = sheet.createRow(i+1);

			cell = row.createCell(0);
			
			cell.setCellValue(i);
			
			for (int j=0;j<methods.size();j++) {
				cell = row.createCell(j+1);
				
				if (wb.getSheet(methods.get(j))!=null)
					cell.setCellFormula(methods.get(j)+"!B"+(i+1)); //average concordance column
				else 
					cell.setCellValue("N/A");
			}

			cell = row.createCell(methods.size()+1);
			
			cell.setCellFormula("AVERAGE(B"+(i+2)+":F"+(i+2)+")");
			
			cell = row.createCell(methods.size()+2);
			
			cell.setCellFormula("ABS(G"+(i+2)+"-$G$"+(numRndSets+3)+")");//TODO add code to select column based on # of methods!
		}
		
		row = sheet.createRow(numRndSets+2);
		cell = row.createCell(0);
		//
		cell.setCellValue("Avg");
		
		for (int j=0;j<methods.size();j++) {
			cell = row.createCell(j+1);
			
			if (wb.getSheet(methods.get(j))!=null) {
				
				cell.setCellFormula(methods.get(j)+"!B"+(numRndSets+2));
			} else {
				
			}
		}
		
		cell = row.createCell(methods.size()+1);
		
		cell.setCellFormula("AVERAGE(B"+(numRndSets+3)+":F"+(numRndSets+3)+")");//TODO add code if F isnt last column

		row = sheet.createRow(numRndSets+3);
		row = sheet.createRow(numRndSets+4);
		
		for (int i=0;i<methods.size();i++) {
									
			row = sheet.createRow(numRndSets+5+i);
			cell = row.createCell(0);
			
			cell.setCellValue(methods.get(i));
			
			cell = row.createCell(1);
			
			if (wb.getSheet(methods.get(i))==null) {
				
				cell.setCellValue("N/A");
			} else {
				
				cell.setCellFormula(methods.get(i)+"!B"+(numRndSets+4));	
			}
			
		}
		
		
		row = sheet.createRow(numRndSets+methods.size()+5);
		cell = row.createCell(0);
		
		cell.setCellValue("Overall avg");

		cell = row.createCell(1);
		
		cell.setCellFormula("MATCH(MIN(H3:H7),$H$3:H7,0)");//TODO: make based on number of sets and methods
		
		
	}
	void CreateConsensusTabHeader(HSSFSheet sheetConsensus,Vector<String> methods) {
		HSSFRow row = sheetConsensus.createRow(0);
		HSSFCell cell = row.createCell(0);
		
		cell.setCellValue("CAS");
	
		cell = row.createCell(1);
		
		cell.setCellValue("exp");
		
		cell = row.createCell(methods.size()+2);
		
		cell.setCellValue("Consensus");				
	}

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
	
	HSSFRow rowMaker(HSSFSheet sheet,int r) {
		HSSFRow row =sheet.getRow(r);
		if (row==null) row=sheet.createRow(r);
		return row;
	}
	
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
	
	void GetRunDataHierarchical(BufferedReader br,HSSFWorkbook wb,int run,String desc) {
		try {
			
			String method="Hierarchical";
			HSSFSheet sheet = wb.createSheet(method);
			String header=(br.readLine());
			
			Vector<String>CAS=new Vector<String>();
			Vector<Double>exp=new Vector<Double>();
			Vector<Double>pred=new Vector<Double>();
			Vector<String>CAS2=new Vector<String>();// array of values if have no pred!
			Vector<Double>exp2=new Vector<Double>();// array of values if have no pred!

			int counter=1;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				java.util.LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
				
				String currentCAS=list.get(1);
				String strExp=list.get(2);
				String strPred=list.get(3);
				
				
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
				
				counter++;
				
			}// end while true
			
			double [] expArray=VectorToDoubleArray(exp);
			double [] predArray=VectorToDoubleArray(pred);
			
			double [] expArray2=VectorToDoubleArray(exp2);
			
			this.OutputPredictionsToTab(0,sheet, CAS, expArray, predArray, CAS2, expArray2);
			
			Row row=sheet.getRow(0);
			Cell cell=row.createCell(10);
			
			cell.setCellValue("Run #");
			
			cell=row.createCell(11);
			
			cell.setCellValue("Desc");

			row=sheet.getRow(1);
			cell=row.createCell(10);
			
			cell.setCellValue(run);
			
			cell=row.createCell(11);
			
			cell.setCellValue(desc);

						
			this.outputMethodStats(expArray, predArray, sheet,exp2.size(),4);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
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
				
				String currentCAS=list.get(0);
				String strExp=list.get(1);
				String strPred=list.get(2);
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
	
	double [] VectorToDoubleArray(Vector<Double>v) {
		double []array=new double[v.size()];
		
		for (int i=0;i<v.size();i++) {
			array[i]=v.get(i);
		}
		return array;
	}
	
	void GetRandomRunData(String method,BufferedReader br,HSSFWorkbook wb,int numRndSets) {
		
		
		double [][]data=new double [6+1][7];
		
		try {
		// 	Make a worksheet in the XL document created
			HSSFSheet sheet = wb.createSheet(method);
//			System.out.println(method);
		
			String header=br.readLine();
			
//			System.out.println(method+"\t"+header);
			
			LinkedList<String> hl=ToxPredictor.Utilities.Utilities.Parse(header, "\t");
			int counter=0;
			HSSFRow row = sheet.createRow((short) counter);
			
			for (int i=0;i<hl.size();i++) {					
				HSSFCell cell = row.createCell(i);
				
				cell.setCellValue(hl.get(i));
			}
			
			for (int i=1;i<hl.size()-1;i++) {					
				HSSFCell cell = row.createCell(i+hl.size()-1);
				
				cell.setCellValue(hl.get(i));
			}

			int avgrow=-1;
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
//				System.out.println(Line);
				counter++;
				
				
				LinkedList<String> l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
								
				if (!l.get(0).equals("Avg")) {
					
					int bob=Integer.parseInt(l.get(0));	
					
					while (counter!=bob)  { 
//						System.out.println(bob+"\t"+counter);
						row = sheet.createRow((short) counter);
						HSSFCell cell = row.createCell(0);
						
						cell.setCellValue(counter);
						
						for (int j=0;j<hl.size();j++) {
							cell = row.createCell(j+1);
														
						}
						
						
						counter++;
					}
				}
				
				row = sheet.createRow((short) counter);
				
				for (int i=0;i<l.size();i++) {					
					HSSFCell cell = row.createCell(i);
					
					// Type some content
					
					String val=l.get(i);
					
					if (i>0) {
						data[counter][i]=Double.parseDouble(val);
					}
					
					if (val.equals("Avg")) {
						avgrow=counter;
						
						cell.setCellValue(val);
					} else {
						
						cell.setCellValue(Double.parseDouble(val));
					}
				}
			}
//			System.out.println(avgrow);
			
			double minMAEDiff=9999;
			int minMAERun=-1;
			int colMAE=4;
			
			for (int j=1;j<=numRndSets;j++) {
				row=sheet.getRow(j);
				for (int i=1;i<hl.size()-1;i++) {		
					
					
					HSSFCell cell = row.createCell(i+hl.size()-1);
					
				
					double dval=(data[j][i]-data[numRndSets+1][i])/data[numRndSets+1][i]*100;
					
					if (i==colMAE && Math.abs(dval)<minMAEDiff) {
						minMAEDiff=Math.abs(dval);
						minMAERun=j;
					}
					
					cell.setCellValue(dval);
				}
			}

//			System.out.println(method+"\t"+minMAERun);

			row = sheet.createRow(++counter);
			row = sheet.createRow(++counter);
			
			HSSFCell cell = row.createCell(0);
			
			cell.setCellValue("Run with MAE closest to avg");
			
			cell = row.createCell(1);
			
			cell.setCellValue(minMAERun);
			
//			weka.core.matrix.Matrix m=new weka.core.matrix.Matrix(data);
//			m.print(6,4);
			
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	void GetRandomRunDataCancer(String method,BufferedReader br,HSSFWorkbook wb) {
		int n=5; //number of random runs
		
			
		try {
		// 	Make a worksheet in the XL document created
			HSSFSheet sheet = wb.createSheet(method);
//			System.out.println(method);
		
			String header=br.readLine();
			
			LinkedList<String> hl=ToxPredictor.Utilities.Utilities.Parse(header, "\t");
			int counter=0;
			HSSFRow row = sheet.createRow((short) counter);
			
			for (int i=0;i<hl.size();i++) {					
				HSSFCell cell = row.createCell(i);
				
				cell.setCellValue(hl.get(i));
			}
			
			for (int i=1;i<hl.size()-1;i++) {					
				HSSFCell cell = row.createCell(i+hl.size()-1);
				
				cell.setCellValue(hl.get(i));
			}

			int avgrow=-1;
			
//			if (true) return;
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
//				System.out.println(Line);
				counter++;
				
				if (Line.indexOf("run")==-1) break;
				
				LinkedList<String> l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
				
				l.set(0,l.get(0).replace("run", ""));

				int bob=Integer.parseInt(l.get(0));	
				
				while (counter!=bob)  { 
//					System.out.println(bob+"\t"+counter);
					row = sheet.createRow((short) counter);
					HSSFCell cell = row.createCell(0);
					
					cell.setCellValue(counter);
					
					for (int j=0;j<hl.size();j++) {
						cell = row.createCell(j+1);
													
					}
					counter++;
				}

				
				row = sheet.createRow((short) counter);
				HSSFCell cell=row.createCell(0);
				
				cell.setCellValue("Avg");
				
				for (int i=0;i<l.size();i++) {					
					cell = row.createCell(i);
					
					// Type some content
					
					String val=l.get(i);

					
					cell.setCellValue(Double.parseDouble(val));
					
				}
			}
			
			int nstats=3;
			row = sheet.createRow(counter);
			HSSFCell cell=row.createCell(0);
			
			cell.setCellValue("Avg");

			
			//TODO: add code if a given run has no results!
			for (int i=1;i<=nstats;i++) {	
				double Avg=0;
				for (int j=1;j<=n;j++) {
					HSSFRow rowj=sheet.getRow(j);
					cell=rowj.getCell(i);
					
					double val=cell.getNumericCellValue();
					
					Avg+=val;
				}
				Avg/=(double)n;
				
				cell=row.createCell(i);
				
				cell.setCellValue(Avg);

//				System.out.println(i+"\t"+Avg);
			}

			
//			System.out.println(avgrow);
			
			double minMAEDiff=9999;
			int minMAERun=-1;
			int colMAE=1;
			
			HSSFRow rowAvg=sheet.getRow(counter);
			for (int j=1;j<=n;j++) {
				row=sheet.getRow(j);
				for (int i=1;i<hl.size()-1;i++) {					
					cell = row.createCell(i+hl.size()-1);
					
				
					double val1=row.getCell(i).getNumericCellValue();
					double valavg=rowAvg.getCell(i).getNumericCellValue();
					
//					System.out.println(i+"\t"+val1+"\t"+valavg);
					
					double dval=(val1-valavg)/valavg;
					
					if (i==colMAE && Math.abs(dval)<minMAEDiff) {
						minMAEDiff=Math.abs(dval);
						minMAERun=j;
					}
					
					cell.setCellValue(dval);
				}
			}

//			System.out.println(method+"\t"+minMAERun);

			row = sheet.createRow(counter+2);
//			row = sheet.createRow(counter+3);
			
			cell = row.createCell(0);
			cell.setCellValue("Run with concordance closest to avg");
			cell = row.createCell(1);
			cell.setCellValue(minMAERun);
			
//			weka.core.matrix.Matrix m=new weka.core.matrix.Matrix(data);
//			m.print(6,4);
			
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	double CalcAllStatsForRndRun(String endpoint,int run,String folder,String filename) {
		
		double AvgVal=-1;
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filename));
			
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
//			Vector<Double>pred=new Vector<Double>();
//			Vector<String>CAS2=new Vector<String>();// array of values if have no pred!
//			Vector<Double>exp2=new Vector<Double>();// array of values if have no pred!

//			System.out.println(filename);
			
			
			LinkedList<String> hl=ToxPredictor.Utilities.Utilities.Parse(header, "\t");
			int nmethods=hl.size()-2;
			
			double [][] preds=new double [Data.size()][nmethods];

			
			for (int i=0;i<Data.size();i++) {
				LinkedList<String> l=ToxPredictor.Utilities.Utilities.Parse(Data.get(i), "\t");
				
				String CASi=l.get(0);
				CAS.add(CASi);
				
				double expi=Double.parseDouble(l.get(1));
				exp.add(expi);
				
				for (int j=1;j<=nmethods;j++) {
					double predij=Double.parseDouble(l.get(j+1));
					preds[i][j-1]=predij;
				}
			}

			
			FileWriter fw=new FileWriter(folder+"/stats-rnd"+run+".txt");
//			System.out.println("****"+folder+"/stats-rnd"+run+".txt");
			
			fw.write("Method	R2	(R2-R2abs)/R2	k	RMSE	MAE	Coverage\r\n");
			
			for (int j=1;j<=nmethods;j++) {
				
				Vector<Double>conExp=new Vector<Double>();
				Vector<Double>conPred=new Vector<Double>();
				Vector<Double>conExp2=new Vector<Double>();// array of values if have no pred!

				for (int i=0;i<Data.size();i++) {
					double predij=preds[i][j-1];
					
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
				java.text.DecimalFormat df=new java.text.DecimalFormat("0.00000");
				
				
				fw.write(hl.get(j+1)+"\t");
				fw.write(df.format(R2)+"\t");
				fw.write(df.format(bob)+"\t");
				fw.write(df.format(k)+"\t");
				fw.write(df.format(RMSE)+"\t");
				fw.write(df.format(MAE)+"\t");
				fw.write(df.format(coverage)+"\t");
				fw.write("\r\n");			
				fw.flush();
				
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
	
double CalcAllStatsForRndRunCancer(String endpoint,int run,String folder,String filename) {
		
		double AvgVal=-1;
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filename));
			
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

			double [][] preds=new double [Data.size()][6];
			
			LinkedList<String> hl=ToxPredictor.Utilities.Utilities.Parse(header, "\t");
			int nmethods=hl.size()-2;
			
			for (int i=0;i<Data.size();i++) {
				LinkedList<String> l=ToxPredictor.Utilities.Utilities.Parse(Data.get(i), "\t");
				
				String CASi=l.get(0);
				CAS.add(CASi);
				
				double expi=Double.parseDouble(l.get(1));
				exp.add(expi);
				
				for (int j=1;j<=nmethods;j++) {
					double predij=Double.parseDouble(l.get(j+1));
					preds[i][j-1]=predij;
				}
			}

			
			FileWriter fw=new FileWriter(folder+"/stats-rnd"+run+".txt");

			fw.write("Method	concordance	sensitivity	specificity	Coverage\r\n");
			
			for (int j=1;j<=nmethods;j++) {
				
				Vector<Double>conExp=new Vector<Double>();
				Vector<Double>conPred=new Vector<Double>();
				Vector<Double>conExp2=new Vector<Double>();// array of values if have no pred!

				for (int i=0;i<Data.size();i++) {
					double predij=preds[i][j-1];
					
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
				
				double [] results=this.CalculateCancerStats(0.5, expArray, predArray);
				double concordance=results[0];
				double sensitivity=results[1];
				double specificity=results[2];
				double coverage=(double)expArray.length/((double)expArray.length+(double)conExp2.size());
				
				java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");
				
				fw.write(hl.get(j+1)+"\t");
				fw.write(df.format(concordance)+"\t");
				fw.write(df.format(sensitivity)+"\t");
				fw.write(df.format(specificity)+"\t");
				fw.write(df.format(coverage)+"\t");
				fw.write("\r\n");			
				fw.flush();
				
				if (j==nmethods) {
					AvgVal=concordance;
//					System.out.println(run+"\t"+AvgVal);
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


	void createHierarchicalPredictionsTextFile(String endpoint, String desc,
			int run, String outputFolder, String outputFilename) {

		java.text.DecimalFormat df = new java.text.DecimalFormat("0.000");
		Vector<String> filePaths = new Vector<String>();

		String fileNameHierarchical = qsarFolder+"/resultsHierarchical/" + endpoint
				+ desc + "/trial1/" + "run" + run + "/run" + run
				+ "-fitvalues.txt";

		String newHeader = "CAS	expToxicValue	Hierarchical clustering";

		try {

			FileWriter fw = new FileWriter(outputFolder + "/" + outputFilename);

			fw.write(newHeader + "\r\n");

			BufferedReader br = new BufferedReader(new FileReader(
					fileNameHierarchical));

			String header = br.readLine();

			while (true) {
				String Line = br.readLine();
				if (Line == null) {
					break;
				}

				LinkedList<String> l = ToxPredictor.Utilities.Utilities.Parse(
						Line, "\t");

				String CAS = l.get(1);
				String exp = l.get(2);
				String pred = l.get(3);
				if (pred.equals("N/A"))
					pred = "-9999";

				double dpred = Double.parseDouble(pred);
				pred = df.format(dpred);

				fw.write(CAS + "\t" + exp + "\t" + pred + "\r\n");
			}

			br.close();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	void createPredictionsTextFile(String endpoint,String desc,int run,String outputFolder,String outputFilename,Vector<String>badCAS) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");
		Vector<String> filePaths=new Vector<String>();
		
		String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/"+"run"+run+"/prediction.txt";
		String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNamekNN=qsarFolder+"/resultskNN/"+endpoint+desc+"/trialuseOverallAD=false q2min=0.5 useSquareDistancesInWeights=true removeOutliers=false Z=2.0/"+"run"+run+"/run"+run+"-fitvalues.txt";

		Vector <String>vecMethodNames=new Vector<String>();
		
		if (includeHierarchical) {
			filePaths.add(fileNameHierarchical);
			vecMethodNames.add("HC");
		}
		if (includeSingleModel) {
			filePaths.add(fileNameSingle);
			vecMethodNames.add("SM");
		}
		if (includeFDA) {
			filePaths.add(fileNameFDA);
			vecMethodNames.add("FDA");
		}
		if (includeFrag) {
			filePaths.add(fileNameFrag);
			vecMethodNames.add("GC");
		}
		if (includeNN) {
			filePaths.add(fileNameNN);
			vecMethodNames.add("NN");
		}
		if (includekNN) {
			filePaths.add(fileNamekNN);
			vecMethodNames.add("kNN");
		}
		
		vecMethodNames.add("Consensus");
		
		String newHeader="CAS	expToxicValue	";
		
		for (int i=0;i<vecMethodNames.size();i++) {
			newHeader+=vecMethodNames.get(i);
			
			if (i<vecMethodNames.size()-1) {
				newHeader+="\t";
			}
		}
		
		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			FileWriter fw=new FileWriter(outputFolder+"/"+outputFilename);
			
			fw.write(newHeader+"\r\n");
			
			for (int i=0;i<filePaths.size();i++) {
				File file=new File (filePaths.get(i));
				if (file.exists()) {
					readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
				} else {
//					System.out.println("missing:"+filePaths.get(i));
					readers.add(null);
				}
				
			}
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				String header=br.readLine();
//				System.out.println(filePaths.get(i)+"\t"+header);
			}
			
			while (true) {
				boolean stop=false;
				
				int predcount=0;
				double consensus=0;

				boolean skip=false;
				
				for (int i=0;i<filePaths.size();i++) {
					
					BufferedReader br=readers.get(i);
					
//					System.out.println(run+"\t"+i);	
					if (br==null) {				
						if (!skip) {
							fw.write("-9999.00");
							if (i<filePaths.size()-1) {
								fw.write("\t");
							} else {
								fw.write("\n");
							}
						}
						continue;
					}
					
					String Line=br.readLine();
					if (Line==null) {
						stop=true;
						break;
					}
							
					LinkedList<String>l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
					
					String CAS=l.get(1);
					String exp=l.get(2);
					String pred=l.get(3);
					
					if (badCAS.contains(CAS)) {
						skip=true;
						continue;
					}
					
					if (pred.equals("N/A")) pred="-9999";
					
					
					double dpred=Double.parseDouble(pred);
					
					if (dpred!=-9999) {
						predcount++;
						consensus+=dpred;
					}
					
					pred=df.format(dpred);
					
					if (i==0) {
						fw.write(CAS+"\t"+exp+"\t");
					}
					
					fw.write(pred+"\t");
					
					if (i==filePaths.size()-1) {

						if (predcount<minPredCount) {
							fw.write("-9999");
						} else {
							consensus/=(double)predcount;
							fw.write(df.format(consensus));
						}
						
						fw.write("\r\n");
					}
					
				}
				if (stop) break;
			}

			
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		File file=new File(fileNameFrag);
//		System.out.println(file.exists());
		
		
	}
	
	
	void createPredictionsTextFile2(String endpoint,String desc,int run,String outputFolder,String outputFilename,Vector<String>badCAS) {

		java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");
		Vector<String> filePaths=new Vector<String>();
		
		String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/trial1/"+"run"+run+"/prediction.txt";
		String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
		String fileNamekNN=qsarFolder+"/resultskNN/"+endpoint+desc+"/trialuseOverallAD=false q2min=0.5 useSquareDistancesInWeights=true removeOutliers=false Z=2.0/"+"run"+run+"/run"+run+"-fitvalues.txt";

		Vector <String>vecMethodNames=new Vector<String>();

		filePaths.add(fileNameHierarchical);
		vecMethodNames.add("HC");

		if (TESTConstants.haveSingleModelMethod(endpoint)) {
			filePaths.add(fileNameSingle);
			vecMethodNames.add("SM");
		}
		if (TESTConstants.haveGroupContributionMethod(endpoint)) {
			filePaths.add(fileNameFrag);
			vecMethodNames.add("GC");
		}

		filePaths.add(fileNameNN);
		vecMethodNames.add("NN");

		vecMethodNames.add("Consensus");
		
		String newHeader="CAS	expToxicValue	";
		
		for (int i=0;i<vecMethodNames.size();i++) {
			newHeader+=vecMethodNames.get(i);
			
			if (i<vecMethodNames.size()-1) {
				newHeader+="\t";
			}
		}
		
		Vector <BufferedReader> readers=new Vector<BufferedReader>();
		
		try {
			
			FileWriter fw=new FileWriter(outputFolder+"/"+outputFilename);
			
			fw.write(newHeader+"\r\n");
			
			for (int i=0;i<filePaths.size();i++) {
				File file=new File (filePaths.get(i));
				if (file.exists()) {
					readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
				} else {
//					System.out.println("missing:"+filePaths.get(i));
					readers.add(null);
				}
				
			}
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				String header=br.readLine();
//				System.out.println(filePaths.get(i)+"\t"+header);
			}
			
			while (true) {
				boolean stop=false;
				
				int predcount=0;
				double consensus=0;

				boolean skip=false;
				
				for (int i=0;i<filePaths.size();i++) {
					
					BufferedReader br=readers.get(i);
					
//					System.out.println(run+"\t"+i);	
					if (br==null) {				
						if (!skip) {
							fw.write("-9999.00");
							if (i<filePaths.size()-1) {
								fw.write("\t");
							} else {
								fw.write("\n");
							}
						}
						continue;
					}
					
					String Line=br.readLine();
					if (Line==null) {
						stop=true;
						break;
					}
							
					LinkedList<String>l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
					
					String CAS=l.get(1);
					String exp=l.get(2);
					String pred=l.get(3);
					
					if (badCAS.contains(CAS)) {
						skip=true;
						continue;
					}
					
					if (pred.equals("N/A")) pred="-9999";
					
					
					double dpred=Double.parseDouble(pred);
					
					if (dpred!=-9999) {
						predcount++;
						consensus+=dpred;
					}
					
					pred=df.format(dpred);
					
					if (i==0) {
						fw.write(CAS+"\t"+exp+"\t");
					}
					
					fw.write(pred+"\t");
					
					if (i==filePaths.size()-1) {

						if (predcount<minPredCount) {
							fw.write("-9999");
						} else {
							consensus/=(double)predcount;
							fw.write(df.format(consensus));
						}
						
						fw.write("\r\n");
					}
					
				}
				if (stop) break;
			}

			
			
			for (int i=0;i<filePaths.size();i++) {
				BufferedReader br=readers.get(i);
				if (br==null) continue;
				br.close();
			}
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		File file=new File(fileNameFrag);
//		System.out.println(file.exists());
		
		
	}
	
	void createPredictionsTextFileNoRnd(String endpoint,String desc,String outputFolder,String outputFileName) {
	
			java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");
			Vector<String> filePaths=new Vector<String>();
			
	
			int run=1;
	
			//TODO: make different QSAR methods have consistent output folders- shouldn't have a mix of some with trial folders and some with run folders
			String fileNameHierarchical=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/run1/trial1-fitvalues.txt";
			String fileNameSingle=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial2/run1/trial2-fitvalues.txt";
			String fileNameFDA=qsarFolder+"/results/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
			String fileNameFrag=qsarFolder+"/resultsFrag/"+endpoint+desc+"/"+"run"+run+"/prediction.txt";
			String fileNameNN=qsarFolder+"/resultsNN/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";
	
			filePaths.add(fileNameHierarchical);
			filePaths.add(fileNameSingle);
			filePaths.add(fileNameFDA);
			filePaths.add(fileNameFrag);
			filePaths.add(fileNameNN);
			
			String fileNameOutput=outputFolder+"/"+outputFileName;
			
			String newHeader="CAS	expToxicValue	Hierarchical clustering	Single model	FDA	Group contribution	Nearest neighbor	Consensus";
			
			Vector <BufferedReader> readers=new Vector<BufferedReader>();
			
			try {
				
				FileWriter fw=new FileWriter(fileNameOutput);
				fw.write(newHeader+"\r\n");
				
				for (int i=0;i<filePaths.size();i++) {
					File file=new File (filePaths.get(i));
					if (file.exists()) {
						readers.add(new BufferedReader(new FileReader(filePaths.get(i))));	
					} else {
						System.out.println("missing:"+filePaths.get(i));
						readers.add(null);
					}
					
				}
				
				for (int i=0;i<filePaths.size();i++) {
					BufferedReader br=readers.get(i);
					if (br==null) continue;
					String header=br.readLine();
	//				System.out.println(filePaths.get(i)+"\t"+header);
				}
				
				while (true) {
					boolean stop=false;
					
					int predcount=0;
					double consensus=0;
					
					for (int i=0;i<filePaths.size();i++) {
						BufferedReader br=readers.get(i);
						
						if (br==null) {
							fw.write("-9999.00");
	
							if (i<filePaths.size()-1) {
								fw.write("\t");
							} else {
								fw.write("\n");
							}
							continue;
						}
						
						String Line=br.readLine();
						if (Line==null) {
							stop=true;
							break;
						}
								
						LinkedList<String>l=ToxPredictor.Utilities.Utilities.Parse(Line, "\t");
						
						String CAS=l.get(1);
						String exp=l.get(2);
						String pred=l.get(3);
						if (pred.equals("N/A")) pred="-9999";
						
						double dpred=Double.parseDouble(pred);
						
						if (dpred!=-9999) {
							predcount++;
							consensus+=dpred;
						}
						
						pred=df.format(dpred);
						
						
						
	//					System.out.println(CAS+"\t"+exp+"\t"+pred);
						
						if (i==0) {
							fw.write(CAS+"\t"+exp+"\t");
						}
						
						fw.write(pred+"\t");
						
						if (i==filePaths.size()-1) {
							if (predcount>0) {
								consensus/=(double)predcount;
								fw.write(df.format(consensus));
							} else {
								fw.write("-9999");
							}
							fw.write("\r\n");
						}
						
					}
					if (stop) break;
				}
	
				
				
				for (int i=0;i<filePaths.size();i++) {
					BufferedReader br=readers.get(i);
					if (br==null) continue;
					br.close();
				}
				fw.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	//		File file=new File(fileNameFrag);
	//		System.out.println(file.exists());
			
			
		}
	
	//creates training and prediction SDFs from overall SDF and tox file for training and prediction set
	void CreateTrainingPredictionSDFs(String endpoint,String desc,String CASField,String delimiter) {
		
		
//		String folder=this.descriptorTextTablesFolder+"/"+endpoint+desc+" Data Files";
//		
//		String sdfFilePath=folder+"/"+endpoint+"_overall.sdf";
////		File sdfFile=new File(sdfFilePath);
//		
//		String trainingToxFilePath=folder+"/"+"tox "+endpoint+" training set.csv";
//		String predictionToxFilePath=folder+"/"+"tox "+endpoint+" prediction set.csv";
//		
//		String outputFolder=folder+"/sdf";
//		File OF=new File(outputFolder);
//		if (!OF.exists()) OF.mkdir();
//		
//		String trainingSDFFileName=endpoint+"_training_set-2d.sdf";
//		String trainingSDFFilePath=outputFolder+"/"+trainingSDFFileName;
//		
//		String predictionSDFFileName=endpoint+"_prediction_set-2d.sdf";
//		String predictionSDFFilePath=outputFolder+"/"+predictionSDFFileName;
//		
//		try {
//			BufferedReader br=new BufferedReader(new FileReader(sdfFilePath));
////			System.out.println(sdfFilePath);
//			
//			FileWriter fwTrain=new FileWriter(trainingSDFFilePath);
//			FileWriter fwPred=new FileWriter(predictionSDFFilePath);
//			
//			ToxPredictor.Utilities.MDLReader mr=new ToxPredictor.Utilities.MDLReader(); 
//			ToxPredictor.Utilities.MDLWriter mwTrain=new ToxPredictor.Utilities.MDLWriter(fwTrain);
//			ToxPredictor.Utilities.MDLWriter mwPred=new ToxPredictor.Utilities.MDLWriter(fwPred);
//			ToxPredictor.Utilities.MDLWriter mw=null;
//			FileWriter fw=null;
//			while (true) {
//				Molecule mol=(Molecule)mr.readMolecule(br);
//				
//				if (mol==null || mol.getAtomCount()==0) break;
////				System.out.println(mol.getAtomCount());
//
//			
//				String CAS=(String)mol.getProperty(CASField);
//				String set="";
//				double Tox=this.GetTox(CAS, trainingToxFilePath, delimiter);
//
//				if (Tox!=-1) {
//					set="T";
//				} else {
//					Tox=this.GetTox(CAS, predictionToxFilePath, delimiter);
//					if (Tox!=-1) {
//						set="P";
//					} else {
//						set="?";
//					}
//				}
//				
//				mol.setProperty("CAS", CAS);
//				mol.setProperty("Tox", Tox+"");
//				
//				mol.removeProperty("CAS Num");
//				mol.removeProperty("Class");
//				mol.removeProperty("Mol_ID");
//				mol.removeProperty("Title");
//				mol.removeProperty("MolfileName");
//				mol.removeProperty("Remark");
//				
//				if (set.equals("T")) {
//					mw=mwTrain;
//					fw=fwTrain;
//				} else if (set.equals("P")) {
//					mw=mwPred;
//					fw=fwPred;
//				}
//				
//				mw.setSdFields(mol.getProperties());
//				mw.writeMolecule(mol);
//				fw.write("$$$$\n");
//				
////				System.out.println(CAS+"\t"+Tox+"\t"+set);
//			}
//			
//			fwTrain.close();
//			fwPred.close();
//			
////			MolFileUtilities m = new MolFileUtilities();
////			m.sortSDFByCAS(trainingSDFFileName, outputFolder, "CAS", trainingSDFFileName);
////			m.sortSDFByCAS(predictionSDFFileName, outputFolder, "CAS", predictionSDFFileName);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
	}
	
	double GetTox(String CAS,String toxFilePath,String delimiter) {
		try {
			BufferedReader br=new BufferedReader(new FileReader(toxFilePath));
			
			br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.List l=ToxPredictor.Utilities.Utilities.Parse3(Line, delimiter);
				String currentCAS=(String)l.get(0);
				
//				System.out.println(CAS+"\t"+currentCAS);
				if (CAS.equals(currentCAS)) {
					String strTox=(String)l.get(2);
					double Tox=Double.parseDouble(strTox);
					
					
					br.close();
					return Tox;
				}
				
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return -1;
	}

	/**
	 * Creates a sdf for each random splitting for training and prediction sets
	 * Nsets = 5
	 */
	void CreateRndTrainingPredictionSDFs(String endpoint,String desc,int numRndRuns,Vector<String>badCAS) {
		String sdfFileName=endpoint+"_overall.sdf";
		CreateRandomTrainingPredictionSDFs2(endpoint,desc,sdfFileName,numRndRuns,badCAS);
	}
	
	/**
	 * This version writes out overall mol files to a folder then loops through each 2d descriptor file to create sdfs
	 * @param endpoint
	 * @param desc
	 * @param overallSDFFileName
	 * @param NSets
	 */
	void CreateRandomTrainingPredictionSDFs2(String endpoint,String desc,String overallSDFFileName,int NSets,Vector<String>badCAS) {
		
		try {
	
			String f1=this.descriptorTextTablesFolder+"/"+endpoint+desc+" data files";
			BufferedReader br=new BufferedReader(new FileReader(f1+"/"+overallSDFFileName));
			IteratingSDFReader reader = new IteratingSDFReader(br,DefaultChemObjectBuilder.getInstance());
			
			String of=f1+"/sdf";
			
			File outputFolder=new File(of);
			if (!outputFolder.exists()) outputFolder.mkdir();
			
			Hashtable<String,AtomContainer>htMol=new Hashtable<>();
			
			while(reader.hasNext()) {
				AtomContainer ac=(AtomContainer)reader.next();
				String CAS=ac.getProperty("CAS");
				htMol.put(CAS, ac);
			}			
							
			for (int i=1;i<=NSets;i++) {
				String filepathSDF=of+"/"+endpoint+"_training_set-2d-rnd"+i+".sdf";
				String filepathDesc=f1+"/2d/"+endpoint+"_training_set-2d-rnd"+i+".csv";
				this.createSDFFile(htMol, filepathDesc,filepathSDF,badCAS);
				
				filepathSDF=of+"/"+endpoint+"_prediction_set-2d-rnd"+i+".sdf";
				filepathDesc=f1+"/2d/"+endpoint+"_prediction_set-2d-rnd"+i+".csv";
				this.createSDFFile(htMol, filepathDesc,filepathSDF,badCAS);
			}

		
		} catch (Exception  e) {
			e.printStackTrace();
		}
		
	}
	void createSDFFile(Hashtable<String,AtomContainer>htMol,String descFilePath,String sdfFilePath,Vector<String>badCAS) {
		try {
			
			FileWriter fw=new FileWriter(sdfFilePath);
			MDLV2000Writer mw=new MDLV2000Writer(fw);		
			mw.setWriteAromaticBondTypes(false);
			
			BufferedReader br=new BufferedReader(new FileReader(descFilePath));
			br.readLine();
			
			
//			System.out.println(descFilePath);
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				String CAS=Line.substring(0,Line.indexOf(","));
//				System.out.println(CAS);
				
				if (badCAS.contains(CAS)) continue;
				
				AtomContainer ac=htMol.get(CAS);				
				mw.writeMolecule(ac);
				
				Vector<String>props=new Vector<>();
				
				
				Set<Object>keys=ac.getProperties().keySet();
					
				for(Object key:keys) {
					String property=(String)key;
					if (property.contains("org.openscience.cdk")) continue;
					fw.write("> <"+property+">\n"+ac.getProperty(property)+"\n\n");
				}
												
				fw.write("$$$$\n");
				fw.flush();

			}
			fw.close();
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void goAllQSARMethods (String endpoint,String desc,boolean doCreateTrainingPredictionSDFs,boolean doCopyFiles,boolean isCancerEndpoint,int numRndRuns,Vector<String>badCAS) {
			
		File OSF=new File(overallSetsFolder);
		if (!OSF.exists()) OSF.mkdir();
		
		File VCF=new File(this.validationCalcsFolder);
		if (!VCF.exists()) VCF.mkdir();

		File WPF=new File(mainOutputFolder+"/web page");
		if (!WPF.exists()) WPF.mkdir();
		
//		System.out.println(endpoint+"\t"+desc+"\t"+run);
		
		String outputFolder=this.overallSetsFolder+"/"+endpoint;
		
		File OF=new File(outputFolder);
		if (!OF.exists()) OF.mkdir();

		//Create SDF for each training/test set for each rnd run:
		if (doCreateTrainingPredictionSDFs) CreateRndTrainingPredictionSDFs(endpoint,desc,numRndRuns,badCAS);
		
		
		// *****************************************************************
//		//create prediction text file for each rnd run:
		String rndfolder=this.validationCalcsFolder+"/random_results";
		File Folder=new File(rndfolder);
		if (!Folder.exists()) Folder.mkdir();
		
		String rndfolder2=rndfolder+"/"+endpoint;
		File Folder2=new File(rndfolder2);
		if (!Folder2.exists()) Folder2.mkdir();
		
		Vector<Double>MAEVector=new Vector<Double>();
		double AvgMAE=0;
		for (int i=1;i<=numRndRuns;i++) {
			String rndfilename=endpoint + " test set predictions-rnd"+i+".txt";;
			createPredictionsTextFile2(endpoint,desc,i,rndfolder2,rndfilename,badCAS);
			double MAEConsensus=-1;
			
			if (!isCancerEndpoint) MAEConsensus=this.CalcAllStatsForRndRun(endpoint,i,rndfolder2,rndfilename);
			else MAEConsensus=this.CalcAllStatsForRndRunCancer(endpoint,i,rndfolder2,rndfilename);

//			System.out.println(i+"\t"+MAEConsensus);
			
			MAEVector.add(MAEConsensus);
			AvgMAE+=MAEConsensus;
		}
		AvgMAE/=(double)numRndRuns;
		
		int runConsensusAvg=-1;
		double minDiff=99999;
		for (int i=1;i<=numRndRuns;i++) {
			double diff=Math.abs(AvgMAE-MAEVector.get(i-1));
			System.out.println(i+"\t"+MAEVector.get(i-1));
			
			if (diff<minDiff) {
				runConsensusAvg=i;				
				minDiff=diff;
			}
		}

		
		int run=runConsensusAvg;
		
//		run=4;
		System.out.println("runConsensusAvg="+run);
		
		if (true) return;
		
		
//		// Create SS which helps choose rnd run:
		if (!isCancerEndpoint) createRndSummarySpreadsheet(endpoint, desc,numRndRuns);
		else createRndSummarySpreadsheetCancer(endpoint, desc,numRndRuns);
		

//		if (true) return;
		
		//Create text file with predictions from all methods (including consensus):
		String outputFilename=endpoint + " test set predictions.txt";;
		createPredictionsTextFile(endpoint,desc,run,outputFolder,outputFilename,badCAS);
		
		
//		//copies all files to outputFolder
		if (doCopyFiles) CopyFiles(endpoint,desc,run);
		
		//Create spreadsheet with prediction statistics for chosen rnd run: 
		if (!isCancerEndpoint) {
			createRunSpreadsheet(endpoint, desc, run,badCAS);
			CreatePlot(endpoint, desc, "Consensus", "\t",badCAS);
		} else {
			createRunSpreadsheetCancer(endpoint, desc, run,badCAS);
		}

		
//		int runOverallAvg=FindRunClosestToAvg(endpoint);		
//		System.out.println(endpoint+desc+"\t"+runOverallAvg+"\t"+runConsensusAvg);
		//Note almost always runOverallAvg=runConsensusAvg
		
		// ************************************************************
		
		System.exit(0);

//		
	}
	
	void goHierarchicalMethod (String endpoint,String desc,boolean doCreateTrainingPredictionSDFs,boolean doCopyFiles,boolean isCancerEndpoint,int numRndRuns,Vector<String>badCAS) {
		
		File OSF=new File(this.overallSetsFolder);
		if (!OSF.exists()) OSF.mkdir();
		
		String outputFolder=this.overallSetsFolder+"/"+endpoint;
		File OF=new File(outputFolder);
		if (!OF.exists()) OF.mkdir();

		//Create SDF for each training/test set for each rnd run:
		if (doCreateTrainingPredictionSDFs) CreateRndTrainingPredictionSDFs(endpoint,desc,numRndRuns,badCAS);

		String runInfoFilePath=qsarFolder+"/resultsHierarchical/"+endpoint+desc+"/trial1/runinfo-1-"+numRndRuns+".txt";
		
		int run=this.getRunWithMAEClosestToAvg(runInfoFilePath,numRndRuns);
		System.out.println("run with MAE closest to avg = "+run);
		
//		//copies all files to outputFolder
		if (doCopyFiles) CopyFiles(endpoint,desc,run);
		
		//Create spreadsheet with prediction statistics for chosen rnd run: 
		if (!isCancerEndpoint) createRunSpreadsheetHierarchical(endpoint, desc, run);
//		else createRunSpreadsheetCancer(endpoint, desc, run);

		String outputFilename=endpoint + " test set predictions.txt";;
		createHierarchicalPredictionsTextFile(endpoint,desc,run,outputFolder,outputFilename);
		
//		
	}

	int getRunWithMAEClosestToAvg(String runInfoFilePath,int numRndRuns) {
		try {			
			BufferedReader br= new BufferedReader(new FileReader(runInfoFilePath));
			
			String header=br.readLine();
			
			int colMAE=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "MAE","\t");
			
			double [] MAEs=new double [numRndRuns+1];
			
			for (int i=1;i<=numRndRuns;i++) {
				String Line=br.readLine();
				
				String strMAE=ToxPredictor.Utilities.Utilities.RetrieveField(Line, "\t", colMAE);
				MAEs[i]=Double.parseDouble(strMAE);
//				System.out.println(strMAE);	
			}
			
			String Line=br.readLine();
			String strMAEAvg=ToxPredictor.Utilities.Utilities.RetrieveField(Line, "\t", colMAE);
			double MAEAvg=Double.parseDouble(strMAEAvg);
			
			int bestRunNum=-1;
			double minDiff=99999;
			for (int i=1;i<=numRndRuns;i++) {
				double diff=Math.abs(MAEs[i]-MAEAvg);
				if (diff<minDiff) {
					minDiff=diff;
					bestRunNum=i;
				}
			}
						
			br.close();
			return bestRunNum;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
		
	}
	CellValue getCellValue (HSSFWorkbook wb,HSSFSheet sheet,String reference) {
		
		CellValue cv=null;
		
		CellReference cellrefer = new CellReference(reference);
		
		HSSFRow row=sheet.getRow(cellrefer.getRow());
		HSSFCell cell=row.getCell((int)cellrefer.getCol());
		
		HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);

		cv = evaluator.evaluate(cell);
		
		return cv;
	}
	
	int FindRunClosestToAvg(String endpoint) {
		String f=this.validationCalcsFolder+"/select random run";
		
		try {
		
			String filename = f+"/"+endpoint+" random calcs.xls";

			HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(filename));
			HSSFSheet sheet=wb.getSheet("Summary");
			
			String ref="B16";// run with avg MAE closest to average over all methods
//			String ref="B11";// run with MAE closest to average for hierarchical
			
			CellValue cellValue = this.getCellValue(wb, sheet, ref);
			double dval=cellValue.getNumberValue();
			int run=(int)dval;
			
			return run;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
		
	}
	
	void runDevTox(Vector<String>badCAS) {
		
		// No random i.e. training/prediction for reproTox:
		String endpoint="DevTox";
		String desc="_Final3";
		
		String rndfolder=validationCalcsFolder+"/random_results";
		String rndfolder2=rndfolder+"/"+endpoint;
		File Folder2=new File(rndfolder2);
		if (!Folder2.exists()) Folder2.mkdir();

		String outputFileName=endpoint + " test set predictions.txt";;
		String outputFolder=this.overallSetsFolder+"/"+endpoint;
		createPredictionsTextFileNoRnd(endpoint,desc,outputFolder,outputFileName);//for reproTox
		
		//add prediction from CAESAR RF method:
		addCAESAR_RF(endpoint,desc,outputFolder, outputFileName);		

		File file1=new File(outputFolder+"/"+outputFileName);
		File file2=new File(rndfolder2+"/"+outputFileName);
		ToxPredictor.Utilities.Utilities.CopyFile(file1, file2);
//		createPredictionsTextFileNoRnd(endpoint,desc,rndfolder2,outputFileName);//for reproTox

		CreateTrainingPredictionSDFs(endpoint, desc,"CAS",",");
		CopyFilesNoRnd(endpoint,desc);//for reproTox
		createRunSpreadsheetCancer(endpoint,desc,1,badCAS);

	}
	
	
	
	void addCAESAR_RF(String endpoint, String desc,String outputFolder,String outputFileName) {
		try {
			BufferedReader br=new BufferedReader(new FileReader(outputFolder+"/"+outputFileName));
			
			java.util.Vector vec=new java.util.Vector();
			String header=br.readLine();
			header+="\tCAESAR Random Forest";
			vec.add(header);
			
			int run=1;
			String filePathRF=qsarFolder+"/resultsRF/"+endpoint+desc+"/trial1/"+"run"+run+"/run"+run+"-fitvalues.txt";			
			BufferedReader brRF=new BufferedReader(new FileReader(filePathRF));
			brRF.readLine();//header discarded
			
			while (true) {
				String Line=br.readLine();
				
				String LineRF=brRF.readLine();
				
				if (Line==null || LineRF==null) break;
				
				LinkedList<String>l=ToxPredictor.Utilities.Utilities.Parse(LineRF, "\t");
				
				String CAS=l.get(1);
				String exp=l.get(2);
				String pred=l.get(3);
				if (pred.equals("N/A")) pred="-9999";

				Line+="\t"+pred;
				
//				System.out.println(Line);
				
				vec.add(Line);
			}
			
			br.close();
			brRF.close();
			
			FileWriter fw=new FileWriter(outputFolder+"/"+outputFileName);
			
			for (int i=0;i<vec.size();i++) {
				fw.write(vec.get(i)+"\r\n");
				fw.flush();
			}
			
			fw.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void findMissingChemicals() {
		String folder=this.descriptorTextTablesFolder;
		File Folder=new File(folder);

//		String endpoint="MP";
//		String desc="_final3";
		
		String endpoint="density";
		String desc="_final6";

		String overallToxFilePath=folder+"/"+endpoint+desc+" data files"+"/"+"tox "+endpoint+" overall set.csv";
		String overallFilePath=folder+"/"+endpoint+desc+" data files"+"/2d/"+endpoint+"_overall_set-2d.csv";
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(overallToxFilePath));
			br.readLine();
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				counter++;
				
				if (Line==null || Line.length()<5) break;
				
				String CAS=Line.substring(0,Line.indexOf(","));
				
				System.out.println(counter+"\t"+CAS);
				
				if (!haveChemical(CAS,overallFilePath)) {
					System.out.println(Line);
				}
				
			}
			
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	boolean haveChemical(String CAS,String filepath) {
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			br.readLine();
			
			int counter=0;
			while (true) {
				String Line=br.readLine();
				counter++;
				
				if (counter>20000) break;
				
				if (Line==null || Line.length()<5) break;
				
				String currentCAS=Line.substring(0,Line.indexOf(","));
				
				if (CAS.equals(currentCAS)) {
					br.close();
					return true;
				}
			}
			
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	
	void getSizeOfOverallSets() {
		String folder=this.descriptorTextTablesFolder;
		File Folder=new File(folder);
		
		for (int i=17;i<Folder.listFiles().length;i++) {
			File Folderi=Folder.listFiles()[i];
			if (!Folderi.isDirectory()) continue;
			String name=Folderi.getName();
			if (name.toLowerCase().indexOf("data files")==-1) continue;
//			System.out.println(name);
			
			String endpoint="";
			if (name.indexOf("_")>-1)
				endpoint=name.substring(0,name.indexOf("_"));
			else 
				endpoint=name.substring(0,name.indexOf(" "));
			
			String overallToxFilePath=folder+"/"+name+"/"+"tox "+endpoint+" overall set.csv";
			String overallFilePath=folder+"/"+name+"/2d/"+endpoint+"_overall_set-2d.csv";

//			File ofp=new File(overallFilePath);
//			if (!ofp.exists()) {
//				System.out.println(overallFilePath+" doesnt exist");
//				continue;
//			}
			System.out.println(name+"\t"+numChemicalsInCSVFile(overallToxFilePath)+"\t"+numChemicalsInCSVFile(overallFilePath));
			
		}
	}
	
	int numChemicalsInCSVFile(String filepath) {
		try {
			
			File file=new File(filepath);
			if (!file.exists()) return -1;
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			int count=0;
			
			br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null || Line.length()<10) break;
				count++;
			}
			
			br.close();
			return count;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	/**
	 * Creates series of mol files-doesnt use cdk's reader
	 * 
	 * @param SDFfilename
	 * @param outputfileloc
	 */
	public void GoThroughSDF(String SDFfilepath, 
			String CASFieldName,String descriptorFilePath) {
		
//		try {
//
//			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));
//
//			// System.setProperty("cdk.debug.stdout", "true");
//			// System.setProperty("cdk.debugging", "true");
//
//			ToxPredictor.Utilities.MDLReader mr = new ToxPredictor.Utilities.MDLReader();
//
//			String[] strData = new String[200];
//
//			IMolecule m;
//
//			
//			int counter = 0;
//
//			boolean Stop=false;
//			while (true){
//			
//				ArrayList al = new ArrayList();
//
//				while (true) {
//
//					try {
//
//						String Line = br.readLine();
//						al.add(Line);
//
//						if (Line==null) {
//							Stop=true;
//							break;
//						}
//						
//						if (Line.indexOf("$$$$") > -1)
//							break;
//
//					} catch (Exception e) {
//						e.printStackTrace();
//						break;
//					}
//
//
//				}// end while loop for given chemical
//
//				counter++;
//				
//				if (counter%100==0) System.out.println(counter);
//				
//				if (Stop) break;
//				
//				String CAS = "";
//				for (int i = 0; i < al.size(); i++) {
//					String Line = (String) al.get(i);
//					
//					String CASField=">  <"+CASFieldName+">";
//					              
//					String CASField2="> <"+CASFieldName+">";
//					                
//					if (Line.indexOf(CASField)>-1 || Line.indexOf(CASField2)>-1) {
//						CAS = (String) al.get(i + 1);
//						boolean HaveCAS=this.haveChemical(CAS, descriptorFilePath);
//						if (!HaveCAS) System.out.println("missing:"+CAS);
//						break;
//					}
//				}
//
////				System.out.println(CAS+"\t"+counter++);
//				
//			}// end while loop over chemicals in sdf file
//
//			
//			
//			br.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		}

	}
	
	/**
	 * Creates series of mol files-doesnt use cdk's reader
	 * 
	 * @param SDFfilename
	 * @param outputfileloc
	 */
	public void GoThroughSDF2(String SDFfilepath, 
			String CASFieldName,String descriptorFilePath) {
		
//		try {
//
//			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));
//			BufferedReader br2 = new BufferedReader(new FileReader(descriptorFilePath));
//			// System.setProperty("cdk.debug.stdout", "true");
//			// System.setProperty("cdk.debugging", "true");
//
//			ToxPredictor.Utilities.MDLReader mr = new ToxPredictor.Utilities.MDLReader();
//
//			String[] strData = new String[200];
//
//			IMolecule m;
//			br2.readLine();
//			
//			int counter = 0;
//
//			boolean Stop=false;
//			while (true){
//			
//				ArrayList al = new ArrayList();
//
//				while (true) {
//
//					try {
//
//						String Line = br.readLine();
//						al.add(Line);
//
//						if (Line==null) {
//							Stop=true;
//							break;
//						}
//						
//						if (Line.indexOf("$$$$") > -1)
//							break;
//
//					} catch (Exception e) {
//						e.printStackTrace();
//						break;
//					}
//
//
//				}// end while loop for given chemical
//
//				counter++;
//				
////				if (counter%200==0) System.out.println(counter);
//				
//				if (Stop) break;
//				
//				String CAS = "";
//				for (int i = 0; i < al.size(); i++) {
//					String Line = (String) al.get(i);
//					
//					String CASField=">  <"+CASFieldName+">";
//					              
//					String CASField2="> <"+CASFieldName+">";
//					                
//					if (Line.indexOf(CASField)>-1 || Line.indexOf(CASField2)>-1) {
//						CAS = (String) al.get(i + 1);
//						
//						break;
//					}
//				}
//				
//				String descLine=br2.readLine();
//				String CAS_descriptor=descLine.substring(0,descLine.indexOf(","));
////				System.out.println(CAS+"\t"+CAS_descriptor);
//				if (!CAS.equals(CAS_descriptor)) {
//					System.out.println("mismatch:"+CAS+"\t"+CAS_descriptor);
//					break;
//				}
//
////				System.out.println(CAS+"\t"+counter++);
//				
//			}// end while loop over chemicals in sdf file
//
//			
//			
//			br.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		}

	}
	
	/**
	 * Checks sdf files to see if have a chemical not present in descriptor files
	 */
	void checkSDFFiles(String endpoint,String desc) {
		String f1=this.descriptorTextTablesFolder+"/"+endpoint+desc+" data files";
		String of=f1+"/sdf";
		
		System.out.println(endpoint+desc);
		for (int i=1;i<=5;i++) {
//			System.out.println("i="+i);
			
			System.out.println("training"+i);
			String filepathSDF=of+"/"+endpoint+"_training_set-2d-rnd"+i+".sdf";
			String descriptorFilePath=f1+"/2d/"+endpoint+"_training_set-2d-rnd"+i+".csv";
			GoThroughSDF2(filepathSDF,"CAS",descriptorFilePath);

			System.out.println("prediction"+i);
			filepathSDF=of+"/"+endpoint+"_prediction_set-2d-rnd"+i+".sdf";
			descriptorFilePath=f1+"/2d/"+endpoint+"_prediction_set-2d-rnd"+i+".csv";
			GoThroughSDF2(filepathSDF,"CAS",descriptorFilePath);
		}
	}
	
	void lookForChemicalsInFolders() {
		
		Vector<String>badCAS=new Vector<String>();

		badCAS.add("74-82-8");
		badCAS.add("74-90-8");
		badCAS.add("304-91-6");
		badCAS.add("409-21-2");
		badCAS.add("536-80-1");
		badCAS.add("630-08-0");
		badCAS.add("696-33-3");
		badCAS.add("2533-82-6");
		badCAS.add("7276-58-6");
		badCAS.add("7440-44-0");
		badCAS.add("18137-96-7");
		badCAS.add("20846-00-8");
		badCAS.add("25590-58-3");
		badCAS.add("25590-60-7");
		badCAS.add("34430-24-5");
		badCAS.add("41206-16-0");
		badCAS.add("51528-03-1");
		badCAS.add("85060-01-1");
		badCAS.add("85060-03-3");
		badCAS.add("113359-04-9");

		
//		badCAS.add("");
		
				String folder=this.descriptorTextTablesFolder;
		File Folder=new File(folder);
		
		for (int j = 0; j < badCAS.size(); j++) {
			
			String badCASj=badCAS.get(j);
			
			for (int i = 0; i < Folder.listFiles().length; i++) {
				File Folderi = Folder.listFiles()[i];

				String filenamei = Folderi.getName();

				if (filenamei.toLowerCase().indexOf("data files") == -1)
					continue;
				if (filenamei.toLowerCase().indexOf("logp") > -1)
					continue;
				if (filenamei.indexOf("_")==-1) continue;

//				System.out.println(filenamei);
				String endpoint=filenamei.substring(0,filenamei.indexOf("_"));
				
				String filepath=Folderi.getAbsolutePath()+"/tox "+endpoint+" overall set.csv";
				File toxFile=new File(filepath);
//				System.out.println(filepath+"\t"+toxFile.exists());
				
				boolean haveCAS=this.haveChemical(badCASj, filepath);
				
				if (haveCAS) {
					System.out.println(badCASj+"\t"+endpoint);
				}
			}
		}		
		
	}
	

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
				
//				System.out.println(num);
				int inum=Integer.parseInt(num);
				if (inum>max)max=inum;
			}
			
		}
		
		
		return max;
	}

	
	
	public static void main(String[] args) { 
		
		CreatePredictionsTextFile c=new CreatePredictionsTextFile();

		String mainOutputFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\web-test_2019_05_03\\TEST deployment 5.1";				
		String mainDataFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\TEST5";
		c.setFolders(mainOutputFolder, mainDataFolder);
				
		String endpoint=TESTConstants.ChoiceRat_LD50;
		String endpointAbbrev=TESTConstants.getAbbrevEndpoint(endpoint);

		System.out.println("Endpoint="+endpoint);
		
		int numRndRuns=5;
		boolean isCancerEndpoint=TESTConstants.isBinary(endpoint);
//		boolean isCancerEndpoint=true;
		
		String desc="";
		int finalNum=FindFinalNumber(endpointAbbrev,c.descriptorTextTablesFolder);
		if (finalNum>0)
			desc="_Final"+finalNum;
		else 
			desc="";
		
		boolean doCreateTrainingPredictionSDFs=false;
		boolean doCopyFiles=true;
				
		Vector<String>badCAS=new Vector<String>();
		badCAS.add("74-82-8");//methane
		badCAS.add("74-90-8");//hydrogen cyanide
		badCAS.add("409-21-2");//silicon carbide
		badCAS.add("7440-44-0");//carbon
		badCAS.add("7276-58-6");//Benzimidazolium, tris(1-dodecyl-3-methyl-2-phenyl-, ferricyanide
		badCAS.add("25590-60-7");//1-Nitropropane nitronate (ion)
		badCAS.add("85060-03-3");// 2-Nitro-1-butanol nitronate (ion)
		badCAS.add("630-08-0");//carbon monoxide
		badCAS.add("85060-01-1");// 2-Butanol, 3-nitro-, ion(1-)
		badCAS.add("18137-96-7");//methanenitronate (ion)
		badCAS.add("20846-00-8");//nitronate (ion)
		badCAS.add("25590-58-3");//nitronate (ion)
		badCAS.add("34430-24-5");//nitronate (ion)

		
		c.goAllQSARMethods(endpointAbbrev,desc,doCreateTrainingPredictionSDFs,doCopyFiles,isCancerEndpoint,numRndRuns,badCAS);
		
		//instead of trying to determine which rnd run to use, just run the prediction set through the software and calc stats off that!!
		
		
		
//		c.goThroughConsensusPermutations(endpoint);
		
//		String [] abbrevs={"LC50","LC50DM","IGC50","LD50","BCF","BP","Density","WS","FP","ST","TC","Viscosity","VP","MP"};
//		for (int i=0;i<abbrevs.length;i++) {
//			String abbrev=abbrevs[i];
//			finalNum=QSAR.validation.NearestNeighborMethod.FindFinalNumber(abbrev);
//			if (finalNum>0) {
//				desc="_Final"+finalNum;
//			}
//			System.out.println(abbrev+desc);
//			c.CreatePlot(abbrev, desc, "Consensus", "\t", badCAS);
//		}

//		c.goHierarchicalMethod(endpoint, desc, doCreateTrainingPredictionSDFs, doCopyFiles, isCancerEndpoint,numRndRuns);
		
//		c.CreateTrainingPredictionSDFs("LC50", "_RationalDesignVsRnd", "CAS", ",");
//		c.runDevTox(badCAS);
//		c.getSizeOfOverallSets();
//		c.findMissingChemicals();
		

//		c.checkSDFFiles(endpoint, desc);
		
//		int run=1;
//		CreateFilesForQSARAnalysis cffqa=new CreateFilesForQSARAnalysis();
//		String sdfFileName=endpoint+"_overall.sdf";
//		cffqa.CreateRandomTrainingPredictionSDFs2(endpoint,desc,sdfFileName,numRndRuns,true);
//		c.CopyFiles(endpoint,desc,run);
//		
//		c.checkSDFFiles(endpoint, desc);
//		c.lookForChemicalsInFolders();
		
	}// end main
	
	//analyze consensus models to see what gives best results
	// including FDA might not improve results so it might be better to omit it to speed things along
	void goThroughConsensusPermutations(String endpoint) {
		
		String predFile=overallSetsFolder+"/"+endpoint+"/"+endpoint+" test set predictions.txt";
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(predFile));
			
			String header=br.readLine();
			
//			System.out.println(header);
			
			int colCAS=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "CAS","\t");
			int colExp=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "expToxicValue","\t");
			
			int colHierarchical=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Hierarchical clustering","\t");
			int colSingleModel=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Single model","\t");
			int colFDA=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "FDA","\t");
			int colGroupContribution=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Group contribution","\t");
			int colNearestNeighbor=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "Nearest neighbor","\t");
			
			
			Vector<String>data=new Vector<String>();
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				data.add(Line);
				
			}
			
			br.close();
			
			double [] exp=new double [data.size()];
			double [][] preds=new double [data.size()][5];
			
			for (int i=0;i<data.size();i++) {
				String Line=data.get(i);
				String[] vals=Line.split("\t");
				
				exp[i]=Double.parseDouble(vals[colExp]);
				preds[i][0]=Double.parseDouble(vals[colHierarchical]);
				preds[i][1]=Double.parseDouble(vals[colSingleModel]);
				preds[i][2]=Double.parseDouble(vals[colFDA]);
				preds[i][3]=Double.parseDouble(vals[colGroupContribution]);
				preds[i][4]=Double.parseDouble(vals[colNearestNeighbor]);
				
//				System.out.println(exp[i]+"\t"+preds[i][0]);
				
			}
			
			Vector<String>models=new Vector<String>();
			
			//1 model:
			for (int i=0;i<5;i++) {
				models.add(i+"");	
			}

			//2 models:
			for (int i=0;i<5;i++) {
				for (int j=i+1;j<5;j++){
					models.add(i+"\t"+j);	
				}
			}

			//3 models:
			models.add("0\t1\t2");
			models.add("0\t1\t3");
			models.add("0\t1\t4");
			models.add("0\t2\t3");
			models.add("0\t2\t4");
			models.add("0\t3\t4");
			models.add("1\t2\t3");
			models.add("1\t2\t4");
			models.add("1\t3\t4");
			models.add("2\t3\t4");

			//4 models:
			models.add("0\t1\t2\t3");
			models.add("0\t1\t2\t4");
			models.add("0\t1\t3\t4");
			models.add("0\t2\t3\t4");
			models.add("1\t2\t3\t4");
			
			//5 models:
			models.add("0\t1\t2\t3\t4");
			
			System.out.println("models	r2	cov	prod");
			for (int i=0;i<models.size();i++) {
				String stats=getStats(models.get(i),exp,preds);
				System.out.println(models.get(i).replace("\t","|")+"\t"+stats);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	String getStats(String modelNums,double [] exp,double [][]preds) {
		
		String []modelNumStringArray=modelNums.split("\t");
		
		int [] modelNumIntArray=new int[modelNumStringArray.length];
		for (int i=0;i<modelNumIntArray.length;i++) {
			modelNumIntArray[i]=Integer.parseInt(modelNumStringArray[i]);
//			System.out.println(modelNumIntArray[i]);
		}
		
		int predCount=0;
		
		Vector<Double>expArray=new Vector<Double>();
		Vector<Double>predArray=new Vector<Double>();
		
		for (int i=0;i<exp.length;i++) {
			double sum=0;
			int count=0;
			
			double pred;
			
			for (int j=0;j<modelNumIntArray.length;j++) {
				double predj=preds[i][modelNumIntArray[j]];
				if (predj>-9999) {
					sum+=predj;
					count++;
				}
			}
			
			if (count>0) {
				predCount++;
				pred=sum/(double)count;
				
				expArray.add(exp[i]);
				predArray.add(pred);
			} else {
				pred=-9999;
			}
		}
		double coverage=(double)predCount/(double)exp.length;
		
		if (predCount==0) return "";
		
		double []exp2=new double [predCount];
		double []pred2=new double [predCount];
		
		for (int i=0;i<predCount;i++) {
			exp2[i]=expArray.get(i);
			pred2[i]=predArray.get(i);
		}
		
		double R2=GetStats.CalculateCurrentR2_2(exp2,pred2);

		DecimalFormat df=new DecimalFormat("0.000");
		return df.format(R2)+"\t"+df.format(coverage)+"\t"+df.format(R2*coverage);
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
	public void CreatePlot(String abbrev,String desc, String methodColumnName, String delimiter,Vector<String>badCAS) {
//		try {
//
//			String expColumnName="expToxicValue";
//			
//			
//			String folder=overallSetsFolder + "/" + abbrev;
//			String filename = folder+ "/"+ abbrev + " test set predictions.txt";
//
//			
//			BufferedReader br = new BufferedReader(new FileReader(filename));
//
//			String header = br.readLine();
//
//			int colExp = ToxPredictor.Utilities.Utilities
//			.FindFieldNumber(header, expColumnName, delimiter);
//
//			int colPred= ToxPredictor.Utilities.Utilities
//					.FindFieldNumber(header, methodColumnName, delimiter);
//
//			Vector<Double>expVec=new Vector<Double>();
//			Vector<Double>predVec=new Vector<Double>();
//			
//			while (true) {
//				String Line = br.readLine();
//				if (!(Line instanceof String))
//					break;
//
//				java.util.List<String> l = ToxPredictor.Utilities.Utilities
//						.Parse(Line, delimiter);
//				
//				String CAS=l.get(0);
//				String strExp=l.get(colExp);
//				String strPred=l.get(colPred);
//				
////				System.out.println(CAS);
//				
//				if (badCAS.contains(CAS)) {
//					System.out.println(CAS+"\tbadCAS");
//				}
//				
//				if (strPred.equals("N/A"))continue;
//
//				double exp=Double.parseDouble(strExp);
//				double pred=Double.parseDouble(strPred);
//				
//				if (pred==-9999) continue;
//				
//				expVec.add(exp);
//				predVec.add(pred);
//				
////				System.out.println(exp+"\t"+pred);
//				
//			}
//			
//			double[] x = new double[expVec.size()];
//			double[] y = new double[expVec.size()];
//
//			for (int i = 0; i < expVec.size(); i++) {
//				x[i] = expVec.get(i);
//				y[i] = predVec.get(i);
//			}
//
//			
//			String endpoint=fraMain7.getEndpointFromAbbrev(abbrev);
//			
//			String axistitle;
//			if (endpoint.equals(fraMain7.ChoiceBoilingPoint) || 
//					endpoint.equals(fraMain7.ChoiceDensity) ||
//					endpoint.equals(fraMain7.ChoiceFlashPoint) ||
//					endpoint.equals(fraMain7.ChoiceMeltingPoint) ||
//					endpoint.equals(fraMain7.ChoiceSurfaceTension) ||
//					endpoint.equals(fraMain7.ChoiceThermalConductivity)) {
//				
//				axistitle=endpoint+" "+PredictToxicityWebPageCreator.getMassUnits(endpoint);
//				
//			} else {
//				axistitle=endpoint+" "+PredictToxicityWebPageCreator.getMolarLogUnits(endpoint);
//			}
//
//			
//			String xtitle="Exp. "+axistitle;
//			String ytitle="Pred. "+axistitle;
////			String title="Test set predictions for the "+methodColumnName+" method";
//			String title="External prediction results";
//			fraChart fc = new fraChart(x,y,title,xtitle,ytitle);
//
//			String destFileName=abbrev+desc+"_"+methodColumnName+".png";
//			String destFolder=validationCalcsFolder+"/run summary";
//			fc.doDrawLegend=true;
//			fc.doDrawStatsR2=false;
//			fc.doDrawStatsMAE=false;
//			fc.WriteImageToFile(destFileName,destFolder);
//
//			
//			br.close();
//			
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

	}
	
	
}
