package ToxPredictor.NICEATM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import javax.sound.sampled.Line;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.xb.xsdschema.ListDocument.List;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;

import QSAR.validation2.NearestNeighborMethod;
import QSAR.validation2.Validation2;
import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;

import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.DownloadChemidplusData;

public class NICEATM {

	String mainFolder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM";

	
	/**
	 * Combine descriptor files from multiple runs
	 */
	void combineDescriptorFiles() {

		String part1="prediction set descriptors";//3
		String part2="-Descriptors.tsv";

		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\z build models\\NICEATM";

		String fileNameOutput="prediction set descriptors with errors.tsv";

		try {
			FileWriter fw=new FileWriter(mainFolder+"/"+fileNameOutput);

			String header="";
			
			Vector<String>overallLines=new Vector<String>();
			
			for (int i=1;i<=3;i++) {
				String filename="";

				if (i==1) {
					filename=part1+part2;
				} else {
					filename=part1+i+part2;
				}
				Vector<String>lines=this.readTextFile(folder+"\\"+filename);
				
				if (i==1) header=lines.get(0);
				
				for (int j=1;j<lines.size();j++) {
					String Line=lines.get(j);
					if (!overallLines.contains(Line)) {
						overallLines.add(Line);
					}	else {
						System.out.println("dup:"+Line);
					}
				}
				
			}
			
			Collections.sort(overallLines);
			overallLines.add(0, header);
			
			for (int i=0;i<overallLines.size();i++) {
				fw.write(overallLines.get(i)+"\r\n");
			}
			
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	void generateDescriptors() {
		
		WebTEST.reportFolderName=mainFolder;
		WebTEST.createDetailedConsensusReport=false;

		String method=TESTConstants.ChoiceNotApplicable;
		
		Set<WebReportType> wrt = WebReportType.getNone();
		
		try {
			
//			String sdfFileName="trainingset_171127_(1).sdf";
//			String descriptorFileName="training set descriptors.csv";

//			String sdfFileName="training set missing descriptors.sdf";
//			int fileNum=2;
//			String descriptorFileName="training set descriptors"+fileNum+".csv";

			
//			String sdfFileName="predset_qsarr_2d.sdf";
			String sdfFileName="prediction set missing descriptors.sdf";
			int fileNum=4;
			String descriptorFileName="prediction set descriptors"+fileNum+".csv";
			
			String inputFilePath=mainFolder+"/"+sdfFileName;
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);
			
//			String startCAS="";
//			
//			for (int i=0;i<acs.getAtomContainerCount();i++) {
//				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
//				String CAS=ac.getProperty("CAS");
//				
//				if (CAS.equals(startCAS)) {
//					break;
//				} else {
//					acs.removeAtomContainer(i--);
//				}
//			}
			
			
//			AtomContainerSet acs2=new AtomContainerSet();
//			acs2.addAtomContainer(acs.getAtomContainer(0));
			
//			for (int i=1;i<=3535;i++) {	
//				acs.removeAtomContainer(0);
//			}
			
			
			String outputFilePath=mainFolder+"/"+descriptorFileName;
			CalculationParameters cp=new CalculationParameters(outputFilePath, null, TESTConstants.ChoiceDescriptors, method, wrt);
			WebTEST.go(acs, cp);

//			CalculationParameters cp=new CalculationParameters(outputFilePath, null, TESTConstants.ChoiceDescriptors, method, wrt);
//			goDescriptors(acs, outputFilePath, wrt);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	void generateDescriptorsErrorMolecules() {
		
		WebTEST.reportFolderName=mainFolder;
		WebTEST.createDetailedConsensusReport=false;

		String method=TESTConstants.ChoiceNotApplicable;
		
		Set<WebReportType> wrt = WebReportType.getNone();
		
		try {
			
//			String inputFilePath=mainFolder+"/bad chemicals-chemidplus.sdf";
//			String outputFilePath=mainFolder+"/training set descriptors error molecules.csv";

			String inputFilePath=mainFolder+"/bad chemicals prediction-chemidplus.sdf";
			String outputFilePath=mainFolder+"/prediction set descriptors error molecules.csv";
			
			
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);

			CalculationParameters cp=new CalculationParameters(outputFilePath, null, TESTConstants.ChoiceDescriptors, method, wrt);
			WebTEST.go(acs, cp);

//			CalculationParameters cp=new CalculationParameters(outputFilePath, null, TESTConstants.ChoiceDescriptors, method, wrt);
//			goDescriptors(acs, outputFilePath, wrt);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}


	private void goDescriptors(AtomContainerSet acs, String outputFilePath,Set<WebReportType> wrt) throws IOException {
		
		DescriptorFactory df=new DescriptorFactory(false);
		
		FileWriter fw=new FileWriter(outputFilePath);
		df.WriteCSVHeader(fw, ",");
		
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(i);
			DescriptorData dd=WebTEST.goDescriptors(ac, wrt);
			
			if (dd.Error.equals("OK"))
				df.WriteCSVLine(fw, dd, ",");
			else {
				fw.write(ac.getProperty("CAS")+","+dd.Error+"\r\n");
			}
			
//			System.out.println(ac.getProperty("CAS")+"\t"+dd.error);
			
		}
		fw.close();
	}
	
	Vector<String>readTextFile(String filePath) {
		
		Vector<String>lines=new Vector<String>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filePath));
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				lines.add(Line);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		return lines;
		
		
	}
	
	
	void getListBadDescriptors() {
		String f2="bad chemicals from chemidplus";
		
		File folder=new File(mainFolder+"/"+f2);
		if(!folder.exists())folder.mkdir();
		
		
		String descriptorFileName="training set descriptors-Descriptors.tsv";
		
		DownloadChemidplusData dcd=new DownloadChemidplusData();
		
		try {
			
			String inputFilePath=mainFolder+"/trainingset_171127_(1).sdf";
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);

			
			Vector<String>lines=this.readTextFile(mainFolder+"/"+descriptorFileName);
			
			FileWriter fw=new FileWriter(mainFolder+"/bad chemicals.sdf");
			FileWriter fw2=new FileWriter(mainFolder+"/bad chemicals-chemidplus.sdf");
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(true);
			
			MDLV2000Writer mw2=new MDLV2000Writer(fw2);
			mw2.setWriteAromaticBondTypes(false);

			
			String header=lines.get(0);
			
			for (int i=1;i<lines.size();i++) {
				
				String Line=lines.get(i);
				
				if(Line==null) break;
				
				if (Line.indexOf("Error")>-1) {
					String CAS=Line.substring(0, Line.indexOf("\t"));
					
					if (CAS.indexOf("-")==-1) {
						Line=Line.substring(Line.indexOf("\t")+1,Line.length());
						CAS=Line.substring(0, Line.indexOf("\t"));
					}
					
					File molFile=new File(mainFolder+"/"+f2+"/"+CAS+".mol");
					
					if (!molFile.exists())
						dcd.GetMolFile(CAS, mainFolder+"/"+f2, "3d", dcd.typeSystematicName);
						
					if (!molFile.exists()) {
						System.out.println(CAS+"\tno mol file!");	
					} else {
						MDLV2000Reader mr=new MDLV2000Reader();
						FileInputStream is=new FileInputStream(molFile);
						mr.setReader(is);
						AtomContainer ac=new AtomContainer();
						mr.read(ac);
						System.out.println(ac.getAtomCount());
						mw2.writeMolecule(ac);
						fw2.write("> <CASRN>\r\n"+CAS+"\r\n");
						fw2.write("\r\n$$$$\r\n");
					}
					
					
					for (int j=0;j<acs.getAtomContainerCount();j++) {

						AtomContainer ac=(AtomContainer)acs.getAtomContainer(j);

						if (ac.getProperty("CASRN").equals(CAS)) {

							mw.writeMolecule(ac);

							fw.write("> <CASRN>\r\n"+CAS+"\r\n");

							fw.write("\r\n$$$$\r\n");
							break;
						}
					}

				}
					
			}
			
			
			fw.close();
			fw2.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	void getListBadDescriptorsPrediction() {
		String f2="bad chemicals from chemidplus prediction";
		
		File folder=new File(mainFolder+"/"+f2);
		if(!folder.exists())folder.mkdir();
		
		
		String descriptorFileName="prediction set descriptors with errors.tsv";
		
		DownloadChemidplusData dcd=new DownloadChemidplusData();
		
		try {
			
			String inputFilePath=mainFolder+"/predset_qsarr_2d.sdf";
			AtomContainerSet acs=WebTEST.LoadFromSDF(inputFilePath);

			
			Vector<String>lines=this.readTextFile(mainFolder+"/"+descriptorFileName);
			
			FileWriter fw=new FileWriter(mainFolder+"/bad chemicals prediction.sdf");
			FileWriter fw2=new FileWriter(mainFolder+"/bad chemicals prediction-chemidplus.sdf");
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(true);
			
			MDLV2000Writer mw2=new MDLV2000Writer(fw2);
			mw2.setWriteAromaticBondTypes(false);

			
			String header=lines.get(0);
			
			for (int i=1;i<lines.size();i++) {
				
				String Line=lines.get(i);
				
				if(Line==null) break;
				
				if (Line.indexOf("Error")>-1) {
					String CAS=Line.substring(0, Line.indexOf("\t"));
					
					if (CAS.indexOf("-")==-1 && CAS.indexOf("NOCAS_")==-1) {
						
//						System.out.println(Line);
						
						Line=Line.substring(Line.indexOf("\t")+1,Line.length());
						CAS=Line.substring(0, Line.indexOf("\t"));
					}
					
					File molFile=new File(mainFolder+"/"+f2+"/"+CAS+".mol");
					
					if (!molFile.exists())
						dcd.GetMolFile(CAS, mainFolder+"/"+f2, "3d", dcd.typeSystematicName);
						
					if (!molFile.exists()) {
						System.out.println(CAS+"\tno mol file!");	
					} else {
						MDLV2000Reader mr=new MDLV2000Reader();
						FileInputStream is=new FileInputStream(molFile);
						mr.setReader(is);
						AtomContainer ac=new AtomContainer();
						mr.read(ac);
//						System.out.println(ac.getAtomCount());
						mw2.writeMolecule(ac);
						fw2.write("> <CASRN>\r\n"+CAS+"\r\n");
						fw2.write("\r\n$$$$\r\n");
					}
					
					
					for (int j=0;j<acs.getAtomContainerCount();j++) {

						AtomContainer ac=(AtomContainer)acs.getAtomContainer(j);

						if (ac.getProperty("CASRN").equals(CAS)) {

							mw.writeMolecule(ac);

							fw.write("> <CASRN>\r\n"+CAS+"\r\n");

							fw.write("\r\n$$$$\r\n");
							break;
						}
					}

				}
					
			}
			
			
			fw.close();
			fw2.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	void mergeDescriptorFiles() {
		
		try {
//			String set="training";
			String set="prediction";
			
			String fileNameDescriptorsOriginal="";
			
			if (set.equals("training")) {
				fileNameDescriptorsOriginal=set+" set descriptors-Descriptors.tsv";
			} else if (set.equals("prediction")) {
				fileNameDescriptorsOriginal="prediction set descriptors with errors.tsv";
			}
			
			Vector<String>linesOriginal=this.readTextFile(mainFolder+"/"+fileNameDescriptorsOriginal);
			
			String fileNameDescriptorsErrorMolecules=set+" set descriptors error molecules-Descriptors.tsv";
			Vector<String>linesErrorMolecules=this.readTextFile(mainFolder+"/"+fileNameDescriptorsErrorMolecules);

			String fileNameDescriptorsNoErrors=set+ " set descriptors no errors.tsv";
			FileWriter fw=new FileWriter(mainFolder+"/"+fileNameDescriptorsNoErrors);

			String header=linesOriginal.get(0);
			fw.write(header+"\r\n");

			for (int i=1;i<linesOriginal.size();i++) {
				String Line=linesOriginal.get(i);
				
				if (Line.toLowerCase().indexOf("error")==-1) {
					fw.write(Line+"\r\n");
				} else {
					
					String CAS=Line.substring(0, Line.indexOf("\t"));
					
					if (CAS.indexOf("-")==-1 && CAS.indexOf("NOCAS_")==-1) {
						Line=Line.substring(Line.indexOf("\t")+1,Line.length());
						CAS=Line.substring(0, Line.indexOf("\t"));
					}

					String error=Line.substring(Line.indexOf("Error:")+"Error:".length(),Line.length()).trim();
//					System.out.println(CAS+"\t"+error);
				}
				
//				System.out.println(Line);
			}
			fw.flush();
			
			String header3=linesErrorMolecules.get(0);
			
			for (int i=1;i<linesErrorMolecules.size();i++) {
				String Line=linesErrorMolecules.get(i);
				
				
				if (Line.indexOf("?")>-1) {
					continue;//methane
				}
				
				if (Line.toLowerCase().indexOf("error")==-1) {
					fw.write(Line+"\r\n");
				} else {
					
					String CAS=Line.substring(0, Line.indexOf("\t"));
					
					if (CAS.indexOf("-")==-1 && CAS.indexOf("NOCAS_")==-1) {
						Line=Line.substring(Line.indexOf("\t")+1,Line.length());
						CAS=Line.substring(0, Line.indexOf("\t"));
					}

					
					String error=Line.substring(Line.indexOf("Error:")+"Error:".length(),Line.length()).trim();
					System.out.println(CAS+"\t"+error);
					
				}
				
//				System.out.println(Line);
			}
			fw.flush();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	public Hashtable<String,String> parseExcelFile(String excelFilePath,String fieldNameCAS,String fieldNameTox) {

		Hashtable<String,String>ht=new Hashtable<String,String>();
		
		
		try {

			
			File file = new File(excelFilePath);
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			DataFormatter formatter = new DataFormatter();
			
			Row headerRow=sheet.getRow(0);
			
			int colCAS=-1;
			int colTox=-1;
			
			for (int i=0;i<headerRow.getLastCellNum();i++) {
				Cell cell = headerRow.getCell(i);
				String name=formatter.formatCellValue(cell);
				
				if (name.equals(fieldNameCAS)) colCAS=i;
				if (name.equals(fieldNameTox)) colTox=i;
			}
			
			
			
			for (int row=1;row<8995;row++) {	
				Row currentRow = sheet.getRow(row);

				if (currentRow==null) {
					System.out.println(row+"\tnull row");
					break;
				}
				
//				if (row%500==0) System.out.println(row);
				
				
				Cell cellCAS = currentRow.getCell(colCAS);
				Cell cellTox = currentRow.getCell(colTox);

				String CAS=formatter.formatCellValue(cellCAS);
				String Tox=formatter.formatCellValue(cellTox);
				
				ht.put(CAS, Tox);
//				System.out.println(CAS+"\t"+Tox);

				
			}
			
			inputStream.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;
	}
	
	public Hashtable<String,String> parseTextFile(String textFilePath,String fieldNameCAS,String fieldNameTox,String del) {

		Hashtable<String,String>ht=new Hashtable<String,String>();
		
		
		try {

			BufferedReader br=new BufferedReader(new FileReader(textFilePath));
			
			String header=br.readLine();
			
//			System.out.println(header);
			
			LinkedList<String>hl=ToxPredictor.Utilities.Utilities.Parse3(header, del);
			
			int colCAS=-1;
			int colTox=-1;
			
			for (int i=0;i<hl.size();i++) {
				String name=hl.get(i);
				
				if (name.equals(fieldNameCAS)) colCAS=i;
				if (name.equals(fieldNameTox)) colTox=i;
			}
			
			Vector<String>uniqueVals=new Vector<String>();
			
			while (true) {	
				
				String Line=br.readLine();
				
				if (Line==null) {
					break;
				}
				
//				System.out.println(Line);
				
				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, del);
				
				String CAS=list.get(colCAS);
				String Tox=list.get(colTox);
				
				if(!uniqueVals.contains(Tox)) {
					uniqueVals.add(Tox);
//					System.out.println(Tox);
				}
				
				
				ht.put(CAS, Tox);
//				System.out.println(CAS+"\t"+Tox);
				
			}
			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;
	}
	
	void sortDescriptorFile() {

//		String set="training";
		String set="prediction";

		String filename=set+" set descriptors no errors.tsv";
		String filename2=set+" set descriptors no errors sorted.tsv";
		

		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(mainFolder+"/"+filename));
			
			String header=br.readLine();
			
			Vector<String>lines=new Vector<String>();
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				String CAS=Line.substring(0, Line.indexOf("\t"));
				
				String CAS2=CAS;
				
				while (CAS2.length()<15) CAS2="0"+CAS2;
				
				lines.add(CAS2+"\t"+Line);
				
			}
			
			Collections.sort(lines);
			
			FileWriter fw=new FileWriter(mainFolder+"/"+filename2);
			fw.write(header+"\r\n");
			
			for (int i=0;i<lines.size();i++) {
				String Line=lines.get(i);
				String Line2=Line.substring(Line.indexOf("\t")+1,Line.length());
				
				fw.write(Line2+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	/**
	 * Add Tox field and convert tsv to csv
	 * 
	 */
	void createTrainingFileWithTox() {

//		String fieldNameTox="LD50_mgkg";
//		String fieldNameTox="very_toxic";
//		String fieldNameTox="nontoxic";
//		String fieldNameTox="EPA_category";
//		String fieldNameTox="GHS_category";
		String fieldNameTox="";

		String textFileName="trainingset_171130.txt";
		String fileNameDescriptors="training set descriptors no errors sorted.tsv";
		
		String fileNameDescriptorsWithTox=null;
		
		Hashtable<String,String>htTox=null;
		
		if (!fieldNameTox.equals(""))  {
			fileNameDescriptorsWithTox="training set "+fieldNameTox+".csv";
			htTox=parseTextFile(mainFolder+"/"+textFileName, "CASRN",fieldNameTox,"\t");
			System.out.println("Tox text file parsed");
		} else {
			fileNameDescriptorsWithTox="training set.csv";
		}
		
		
		boolean convertTox=true;
		
		if (!fieldNameTox.equals("LD50_mgkg")) convertTox=false;
		
		
		DecimalFormat df=new DecimalFormat("0.000");
		
		
		if (!fieldNameTox.equals(""))  {
			File folder2=new File(mainFolder+"/"+fieldNameTox);
			if (!folder2.exists()) folder2.mkdir();
		}
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(mainFolder+"/"+fileNameDescriptors));
			
			FileWriter fw=null;
			
			if (!fieldNameTox.equals(""))  {
				fw=new FileWriter(mainFolder+"/"+fieldNameTox+"/"+fileNameDescriptorsWithTox);
			} else {
				fw=new FileWriter(mainFolder+"/"+fileNameDescriptorsWithTox);
			}
			String header=br.readLine();
			
			int colMW=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "MW", "\t");
			
			header=header.substring(header.indexOf("\t")+1,header.length());
			header="CAS\tTox\t"+header;
			
			String header2=this.createCSV_Header(header);
			
			
			fw.write(header2+"\r\n");
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) {
					break;
				}
//				System.out.println(Line);
				
				String CAS=Line.substring(0,Line.indexOf("\t"));
				
				
				if (fieldNameTox.equals(""))  {
					Line=Line.substring(Line.indexOf("\t")+1,Line.length());
					Line=CAS+"\t-9999\t"+Line;
					Line=Line.replace("\t", ",");
					fw.write(Line+"\r\n");
					continue;
				}
				
				
				String Tox=htTox.get(CAS);
				if (Tox==null) System.out.println(CAS);
				
				if (!Tox.equals("NA")) {
					
					if (convertTox) {
						String strMW=ToxPredictor.Utilities.Utilities.RetrieveField(Line, "\t", colMW);
						double MolecularWeight=Double.parseDouble(strMW);
						double dTox=Double.parseDouble(Tox);
						double logTox=-Math.log10(dTox/1000.0/MolecularWeight);
						Tox=df.format(logTox);
					}
					
					if (Tox.equals("TRUE")) {
						Tox="1";
					} else if (Tox.equals("FALSE")) {
						Tox="0";
					} 
//					else {
//						System.out.println(CAS+"\t"+Tox);
//						continue;
//					}
					
					Line=Line.substring(Line.indexOf("\t")+1,Line.length());
					Line=CAS+"\t"+Tox+"\t"+Line;
					Line=Line.replace("\t", ",");
					fw.write(Line+"\r\n");
					
//					System.out.println(Line);	
					
				}
				
			}
			
			fw.close();
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	/**
	 * Add Tox field and convert tsv to csv
	 * 
	 */
	void createOneAgainstRestTrainingFiles() {

//		String fieldNameTox="EPA_category";
		String fieldNameTox="GHS_category";

		String textFileName="trainingset_171130.txt";
		String fileNameDescriptors="training set descriptors no errors sorted.tsv";

		Hashtable<String,String>htTox=parseTextFile(mainFolder+"/"+textFileName, "CASRN",fieldNameTox,"\t");
		System.out.println("Tox text file parsed");

		String [] cats=null;
		
		if (fieldNameTox.equals("EPA_category")) {
			cats=new String [4];
		} else if (fieldNameTox.equals("GHS_category")) {
			cats=new String [5];
		}

		for(int i=0;i<cats.length;i++) {
			cats[i]=(i+1)+"";
			System.out.println(i+"\t"+cats[i]);
		}
		
//		if (true) return;
		
		File folder2=new File(mainFolder+"/"+fieldNameTox);
		if (!folder2.exists()) folder2.mkdir();
		
		
		try {
			
			Vector<String>descriptorLines=this.readTextFile(mainFolder+"/"+fileNameDescriptors);

			for (int i=0;i<cats.length;i++) {
				String fileNameDescriptorsWithTox="training set "+fieldNameTox+" cat = "+cats[i]+".csv";
				String outputFilePath=mainFolder+"/"+fieldNameTox+"/"+fileNameDescriptorsWithTox;
				writeOneAgainstRestFile(descriptorLines,cats[i], outputFilePath, htTox);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}


	private void writeOneAgainstRestFile(Vector<String>descriptorLines,String cat,String outputFilePath,
			Hashtable<String, String> htTox) throws IOException {
		
		FileWriter fw=new FileWriter(outputFilePath);
		
		String header=descriptorLines.get(0);
		
		int colMW=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "MW", "\t");
		
		header=header.substring(header.indexOf("\t")+1,header.length());
		header="CAS\tTox\t"+header;
		
		String header2=this.createCSV_Header(header);
		
		
		fw.write(header2+"\r\n");
		
		for (int i=1;i<descriptorLines.size();i++) {
			String Line=descriptorLines.get(i);
			
//				System.out.println(Line);
			
			String CAS=Line.substring(0,Line.indexOf("\t"));
			String Tox=htTox.get(CAS);
			
			if (Tox==null) System.out.println(CAS);
			
			if (!Tox.equals("NA")) {
				
				if (Tox.equals(cat)) Tox="1";
				else Tox="0";
				
				Line=Line.substring(Line.indexOf("\t")+1,Line.length());
				
				Line=CAS+"\t"+Tox+"\t"+Line;
				
				Line=Line.replace("\t", ",");
				
				fw.write(Line+"\r\n");
				
//					System.out.println(Line);	
				
			}
			
		}
		
		fw.close();
	}
	
	/**
	 * Add Tox field and convert tsv to csv
	 * 
	 */
	void createPredictionFileWithTox() {


//		String textFileName="trainingset_171130.txt";
//		String fileNameDescriptors="training set descriptors no errors sorted.tsv";
//		String fileNameDescriptorsWithTox="training set "+fieldNameTox+".csv";

		String textFileName="predset_qsarr_2d.txt";
		String fileNameDescriptors="prediction set descriptors no errors sorted.tsv";
		String fileNameDescriptorsWithTox="prediction set.csv";

		

//		String excelFileName="training set.xlsx";
//		Hashtable<String,String>htTox=parseExcelFile(mainFolder+"/"+excelFileName, "CASRN",fieldNameTox);
//		System.out.println("Spreadsheet parsed");

		
		boolean convertTox=true;
		
		DecimalFormat df=new DecimalFormat("0.000");
		
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(mainFolder+"/"+fileNameDescriptors));
			
			FileWriter fw=new FileWriter(mainFolder+"/"+fileNameDescriptorsWithTox);
			
			String header=br.readLine();
			
			header=header.substring(header.indexOf("\t")+1,header.length());
			header="CAS\tTox\t"+header;
			
			String header2=this.createCSV_Header(header);
			
			fw.write(header2+"\r\n");
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) {
					break;
				}
//				System.out.println(Line);
				
				String CAS=Line.substring(0,Line.indexOf("\t"));
				String Tox="-9999";

				Line=Line.substring(Line.indexOf("\t")+1,Line.length());//trim off CAS
				Line=CAS+"\t"+Tox+"\t"+Line; //create new line with tox
				Line=Line.replace("\t", ",");//convert to csv
				fw.write(Line+"\r\n");//write line
			}
			
			fw.close();
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}

	
	void convertTSV_to_CSV() {
	
		try {

			String fieldNameTox="LD50_mgkg";
			String fileNameDescriptors="training set "+fieldNameTox+".tsv";
			
			BufferedReader br=new BufferedReader(new FileReader(mainFolder+"/"+fileNameDescriptors));

			String fileNameDescriptorsCSV="training set "+fieldNameTox+".csv";
			FileWriter fw=new FileWriter(mainFolder+"/"+fileNameDescriptorsCSV);
			
			String header=br.readLine();
			
			String header2 = createCSV_Header(header);
			
			System.out.println(header2);
			fw.write(header2+"\r\n");
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				fw.write(Line.replace("\t", ",")+"\r\n");
			}
			
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

	}

	private String createCSV_Header(String header) {
		String header2="";

		LinkedList<String>hl=ToxPredictor.Utilities.Utilities.Parse3(header, "\t");

		for (int i=0;i<hl.size();i++) {
			String varname=hl.get(i);
			
			if (varname.indexOf(",")>-1) varname="\""+varname+"\"";
			
			header2+=varname;
			
			if (i<hl.size()-1) header2+=",";
		}
		return header2;
	}
	
	void findMissingCompounds() {

		try {

//			String fileNameList = "trainingset_171130.txt";
//			String descriptorFileName = "training set descriptors-Descriptors.tsv";
//			String sdfFileNameInput="trainingset_171127_(1).sdf";
//			String sdfFileNameOutput="training set missing descriptors.sdf";

			
			String fileNameList = "predset_qsarr_2d.txt";
			String descriptorFileName = "prediction set descriptors with errors.tsv";
			String sdfFileNameInput="predset_qsarr_2d.sdf";
			String sdfFileNameOutput="prediction set missing descriptors.sdf";

			Vector<String> list = this.readTextFile(mainFolder + "/" + fileNameList);
			Vector<String> list2 = this.readTextFile(mainFolder + "/" + descriptorFileName);

			String inputFilePath = mainFolder + "/"+sdfFileNameInput;
			AtomContainerSet acs = WebTEST.LoadFromSDF(inputFilePath);

			FileWriter fw2 = new FileWriter(mainFolder + "/"+sdfFileNameOutput);
			MDLV2000Writer mw2 = new MDLV2000Writer(fw2);
			mw2.setWriteAromaticBondTypes(true);
			
			
			Vector<String>caslist1=new Vector<String>();
			Vector<String>caslist2=new Vector<String>();
			
			for (int i = 1; i < list.size(); i++) {
				String Line = list.get(i);
				LinkedList<String> vals = ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				String CAS="";
				if (fileNameList.indexOf("training")>-1) {
					CAS = vals.get(0);
				} else if (fileNameList.indexOf("pred")>-1) {
					CAS = vals.get(1);
				}
				caslist1.add(CAS);
			}
				

			for (int j = 1; j < list2.size(); j++) {
				String Line2 = list2.get(j);

				String CAS2 = Line2.substring(0, Line2.indexOf("\t"));
				
				if (CAS2.indexOf("-")==-1 && CAS2.indexOf("NOCAS")==-1) {
					Line2=Line2.substring(Line2.indexOf("\t")+1,Line2.length());
					
					if (Line2.indexOf("\t")==-1) {
						System.out.println(Line2);
						continue;
					}
					CAS2 = Line2.substring(0, Line2.indexOf("\t"));
				}
				caslist2.add(CAS2);
			}

			
//			if (CAS2.equals(CAS)) {
//				haveCAS = true;
//				break;
//			}

			
			for (int i=0;i<caslist1.size();i++) {
			
				String CAS1=caslist1.get(i);
				
				if (i%500==0) System.out.println(i);
				
				if (!caslist2.contains(CAS1)) {
					
					System.out.println(CAS1);
					
					for (int j = 0; j < acs.getAtomContainerCount(); j++) {
						AtomContainer ac = (AtomContainer) acs.getAtomContainer(j);
						if (ac.getProperty("CAS").equals(CAS1)) {
							mw2.writeMolecule(ac);
							fw2.write("> <CASRN>\r\n" + CAS1 + "\r\n");
							fw2.write("\r\n$$$$\r\n");
							break;
						}
					}
				}
			}
			

			fw2.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	void dividePredictionSet() {

		int nfiles=10;
//		String set="prediction";
		String set="external";
		

		Vector<String>lines=readTextFile(mainFolder+"/"+set+" set.csv");

		int chemicalsPerFile=lines.size()/nfiles;

		String header=lines.remove(0);

		try {
			for (int j=1;j<=nfiles;j++) {

				FileWriter fw=new FileWriter(mainFolder+"/"+set+" set "+j+".csv");
				
				fw.write(header+"\r\n");

				if (j<nfiles) {
					for (int i=1;i<chemicalsPerFile;i++) {
						fw.write(lines.remove(0)+"\r\n");
					}
					fw.flush();
					fw.close();
				} else {
					//Write out remaining lines
					for (int i=0;i<lines.size();i++) {
						fw.write(lines.get(i)+"\r\n");
					}
					fw.flush();
					fw.close();
				}
			}
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}



	}
	
	
	void makePredictions() {
		try {
			
			String fieldNameTox="LD50_mgkg";
			
			Validation2 v2=new Validation2();

			v2.QSARMethod = "Hierarchical";

			v2.useFragments = true;
			v2.useModelEllipsoid=true;
			v2.UseRmaxCriterion = true;
			
			v2.NumClusters=1;
			v2.MinNumChemicals = -1;
			
			v2.PredictionsToUse = "WeightedAverage";			
			v2.useEqualWeighting = false;
			
			v2.DescriptorSet = "2d";
			v2.XMLFormat=2;
			//****************************************************************			

			String trainingFilePath = mainFolder + "/" + fieldNameTox+"/training set "+fieldNameTox+".csv";
			String testFilePath = mainFolder + "/prediction set.csv";
			String xmlFilePath = mainFolder + "/" + fieldNameTox+"/training set "+fieldNameTox+".xml";
			String weightFilePath = mainFolder + "/2d.txt";
			boolean useWeightingFile = false;

			String outputFolderPath =  mainFolder + "/" + fieldNameTox;
			String outputTextFileName = v2.QSARMethod+" prediction set.txt";

			v2.PerformExternalCalcs(v2.QSARMethod,trainingFilePath, testFilePath, xmlFilePath,
					outputFolderPath, outputTextFileName, useWeightingFile,
					weightFilePath);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void makePredictions2() {
		try {
			
			String fieldNameTox="LD50_mgkg";
			
			Validation2 v2=new Validation2();

			v2.QSARMethod = "Hierarchical";

			v2.useFragments = true;
			v2.useModelEllipsoid=true;
			v2.UseRmaxCriterion = true;
			
			v2.NumClusters=1;
			v2.MinNumChemicals = -1;
			
			v2.PredictionsToUse = "WeightedAverage";			
			v2.useEqualWeighting = false;
			
			v2.DescriptorSet = "2d";
			v2.XMLFormat=2;
			//****************************************************************			

			String trainingFilePath = mainFolder + "/" + fieldNameTox+"/training set "+fieldNameTox+".csv";
			String xmlFilePath = mainFolder + "/" + fieldNameTox+"/training set "+fieldNameTox+".xml";
			String weightFilePath = mainFolder + "/2d.txt";
			boolean useWeightingFile = false;
			String outputFolderPath =  mainFolder + "/" + fieldNameTox;

			int num=5;
			String testFilePath = mainFolder + "/prediction set "+num+".csv";
			String outputTextFileName = v2.QSARMethod+" prediction set "+num+".txt";
			
//			String testFilePath = mainFolder + "/prediction set test.csv";
//			String outputTextFileName = v2.QSARMethod+" prediction set test.txt";

			long t1=System.currentTimeMillis();
			v2.PerformExternalCalcs(v2.QSARMethod,trainingFilePath, testFilePath, xmlFilePath,
					outputFolderPath, outputTextFileName, useWeightingFile,
					weightFilePath);
			long t2=System.currentTimeMillis();
			long time=(t2-t1)/1000;
			System.out.println(time+" seconds");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	void createCSV_ChemicalsOutsideApplicabilityDomain() {
		
		
		String fieldNameTox = "LD50_mgkg";
		
//		int colPred=2;
//		int colCAS=0;
//		String predFileName="Hierarchical prediction set.txt";
//		String predFolder=mainFolder+"/"+fieldNameTox+"/hierarchical prediction set";
//		String predFilePath=predFolder+"/"+predFileName;
//		
//		String csvFolder=mainFolder;
//		String csvFileName="prediction set.csv";
//		String csvFilePath=csvFolder+"/"+csvFileName;
//		
//		String destFolder=mainFolder+"/"+fieldNameTox;
//		String destFileName="prediction hierarchical outside AD.csv";
//		String destFilePath=destFolder+"/"+destFileName;
		
		
		//*************************************************************************************************
//		int colPred=3;
//		int colCAS=1;
//		String predFileName="Hierarchical training set.txt";
//		String predFolder=mainFolder+"/"+fieldNameTox+"/hierarchical training set";
//		String predFilePath=predFolder+"/"+predFileName;
//		
//		String csvFolder=mainFolder+"/"+fieldNameTox;
//		String csvFileName="training set "+fieldNameTox+".csv";
//		String csvFilePath=csvFolder+"/"+csvFileName;
//		
//		String destFolder=csvFolder;
//		String destFileName="training hierarchical outside AD.csv";
//		String destFilePath=destFolder+"/"+destFileName;

		//*************************************************************************************************
//		int colPred=3;
//		int colCAS=1;
//		String predFileName="NN training set-fitvalues.txt";
//		String predFolder=mainFolder+"/"+fieldNameTox+"/NN training set";
//		String predFilePath=predFolder+"/"+predFileName;
//		
//		String csvFolder=mainFolder+"/"+fieldNameTox;
//		String csvFileName="training set "+fieldNameTox+".csv";
//		String csvFilePath=csvFolder+"/"+csvFileName;
//		
//		String destFolder=csvFolder;
//		String destFileName="training NN outside AD.csv";
//		String destFilePath=destFolder+"/"+destFileName;
		
		//*************************************************************************************************
		int colPred=3;
		int colCAS=1;
		String predFileName="nearestneighbor results.txt";
		String predFolder=mainFolder+"/"+fieldNameTox+"/NN prediction set";
		String predFilePath=predFolder+"/"+predFileName;
		
		String csvFolder=mainFolder;
		String csvFileName="prediction set.csv";
		String csvFilePath=csvFolder+"/"+csvFileName;
		
		String destFolder=mainFolder+"/"+fieldNameTox;
		String destFileName="prediction NN outside AD.csv";
		String destFilePath=destFolder+"/"+destFileName;
		
		Vector <String>csvLines=this.readTextFile(csvFilePath);
		Vector <String>predLines=this.readTextFile(predFilePath);
		
		try {
			FileWriter fw = new FileWriter(destFilePath);
			
			fw.write(csvLines.get(0)+"\r\n");
			

			for (int i = 1; i < predLines.size(); i++) {

				String predLine = predLines.get(i);

				LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse3(predLine, "\t");

				String CAS = list.get(colCAS);
				String pred = list.get(colPred);
				double dpred = Double.parseDouble(pred);

				if (dpred == -9999) {
					
					for (int j=1;j<csvLines.size();j++) {
						String csvLine=csvLines.get(j);
								
						String CASj=csvLine.substring(0, csvLine.indexOf(","));
						
						if (CAS.equals(CASj)) {
							fw.write(csvLine+"\r\n");
							break;
						}
						
					}
					
					
//					System.out.println(CAS);
				}

			}
			
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
 		
		
	}
	
	
	void combineResultsFiles() {

//		String fieldNameTox = "LD50_mgkg";
		String fieldNameTox = "very_toxic";

		try {

			FileWriter fw=new FileWriter(mainFolder + "/" + fieldNameTox + "/Hierarchical prediction set.txt");
			
			fw.write("CAS\texpToxicValue\tpredToxicValue\r\n");
			
			for (int i = 1; i <= 10; i++) {

				String filePath=mainFolder + "/" + fieldNameTox + "/Hierarchical prediction set " + i + ".txt";
				File filei = new File(filePath);

				if (!filei.exists()) continue;
				
				Vector<String>lines=this.readTextFile(filePath);
				
				for (int j=1;j<lines.size();j++) {
					String Line=lines.get(j);
					
					LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
					
					String CAS=list.get(1);
					String exp=list.get(2);
					String pred=list.get(3);
					
					fw.write(CAS+"\t"+exp+"\t"+pred+"\r\n");
//					System.out.println(Line);
				}
			}
			
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	void checkLD50Preds() {
		String fieldNameTox = "LD50_mgkg";
		Vector<String>predLines=this.readTextFile(mainFolder+"/"+fieldNameTox+"/Hierarchical prediction set .txt");

		Hashtable<String,String>htTox0=parseTextFile(mainFolder+"/trainingset_171130.txt", "CASRN","LD50_mgkg","\t");
		Hashtable<String,String>htTox=parseTextFile(mainFolder+"/"+fieldNameTox+"/tox LD50 overall set.csv", "CAS","Tox",",");

		for (int i=1;i<predLines.size();i++) {

			LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(predLines.get(i), "\t");
			String CAS=list.get(0);
			String pred=list.get(2);
			
			double dpred=Double.parseDouble(pred);

			String exp0=htTox0.get(CAS);
			
//			System.out.println(CAS+"\t"+exp0);
	
			//get chemicals that appear in our old LD50 set, have a predicted value and dont appear in training set:
			if (htTox.get(CAS)!=null && dpred>-9999 && exp0==null) {
				String exp=htTox.get(CAS);
				System.out.println(CAS+"\t"+exp+"\t"+pred);
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NICEATM na=new NICEATM();
//		na.generateDescriptors();
//		na.combineDescriptorFiles();
//		na.findMissingCompounds();
//		na.getListBadDescriptors();
//		na.getListBadDescriptorsPrediction();
//		na.generateDescriptorsErrorMolecules();
//		na.mergeDescriptorFiles();
//		na.sortDescriptorFile();
		
//		na.createTrainingFileWithTox();
//		na.createPredictionFileWithTox();
//		na.createOneAgainstRestTrainingFiles();
		
//		na.dividePredictionSet();
//		na.makePredictions();//runs out of memory- use code in TEST5 project (see QSAR.qsarOptimal.NICEATM_Calculations)
//		na.makePredictions2();
		
//		na.combineResultsFiles();
		
//		na.createCSV_ChemicalsOutsideApplicabilityDomain();
		
//		na.checkLD50Preds();
		
		
		}

}