package AADashboard.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import AADashboard.Application.RowHCD.Group;
import AADashboard.Application.RowHCD.HazardCategory;
import AADashboard.Application.RowHCD.HazardCategorySpecific;
import AADashboard.Application.RowHCD.HazardCategoryGeneral;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;

public class TableGeneratorExcel {

	
	public void toFlatFileXLS(Chemicals chemicals,String filepath) {

		try {

			//Blank workbook
			XSSFWorkbook workbook = new XSSFWorkbook();
			writeScoreRecordsToWorkbook(chemicals,workbook);

			FileOutputStream out = new FileOutputStream(new File(filepath));
			workbook.write(out);
			out.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void writeBatchChemicalsToExcel(AtomContainerSet acs, Vector<String> newProps,XSSFWorkbook workbook) {

		XSSFSheet sheet = workbook.createSheet("Batch chemicals");

		int rowNum=0;
		XSSFRow row0 = sheet.createRow(rowNum);

		XSSFCellStyle styleBold=getStyleBold(workbook);

		//Write header
		for (int i=0;i<newProps.size();i++) {
			XSSFCell cell=row0.createCell(i);
			cell.setCellValue(newProps.get(i));
			cell.setCellStyle(styleBold);
		}

		for (int i=0;i<acs.getAtomContainerCount();i++) {
			
			XSSFRow row = sheet.createRow(i+1);
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);

			for (int j=0;j<newProps.size();j++) {
				XSSFCell cell=row.createCell(j);
				
				if (ac.getProperty(newProps.get(j))!=null) {
					cell.setCellValue(ac.getProperty(newProps.get(j))+"");					
				} else {
					cell.setCellValue("");
				}
			}			
		}
		
		sheet.createFreezePane( 0, 1, 0, 1 );
		
	}

	
	public void writeScoreRecordsToWorkbook(Chemicals chemicals,XSSFWorkbook workbook) {
		try {
			String del="|";

			ArrayList<String>uniqueCAS=new ArrayList<>();

			//Create a blank sheet
			XSSFSheet sheet = workbook.createSheet("Hazard Records");

			int rowNum=0;
			XSSFRow row = sheet.createRow(rowNum);

			XSSFCellStyle styleBold=getStyleBold(workbook);

			//Write header
			for (int i=0;i<ScoreRecord.allFieldNames.length;i++)	{
				XSSFCell cell=row.createCell(i);
				cell.setCellValue(ScoreRecord.allFieldNames[i]);
				cell.setCellStyle(styleBold);
			}


			for (Chemical chemical:chemicals) {

				ArrayList<String>lines=chemical.toStringArray(del,ScoreRecord.allFieldNames);

				if (!uniqueCAS.contains(chemical.CAS)) uniqueCAS.add(chemical.CAS);


				for (String line:lines) {
					rowNum++;
					row = sheet.createRow(rowNum);

					line=line.replace("–", "-").replace("’", "'").trim();//TODO use StringEscapeUtils?


					LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(line, del);

					for (int i=0;i<list.size();i++) {
						XSSFCell cell=row.createCell(i);

						String value="";

						if (list.get(i).length()>32000) {
							value=list.get(i).substring(0,32000)+"...";	

						} else {
							value=list.get(i);
						}
						cell.setCellValue(value);

					}
				}

				//				fw.write(chemical.to);
			}

			for (int i=0;i<17;i++) {
//				if (i!=1 & i!=7 & i!=9 && i!=10 && i!=11 && i!=12) {
//					sheet.autoSizeColumn(i);
//				}
//				
//				else
//					sheet.setColumnWidth(i, 50*256);
				
				sheet.autoSizeColumn(i);
				
				if (sheet.getColumnWidth(i)>50*256) {
					sheet.setColumnWidth(i, 50*256);
				}
				
				
			}

			sheet.createFreezePane( 1, 1, 1, 1 );

			//			for (String CAS:uniqueCAS) {
			//				System.out.println(CAS);
			//			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	/**
	 * This version has Acute Mammalian Toxicity broken out by route
	 * 
	 * @param jsonFilePath
	 * @param outputFolder
	 * @param outputFileName
	 */
	void generateComparisonTableFromJSONFileAsExcel(Chemicals chemicals,String outputFolder,String outputFileName) {

		try {
			XSSFWorkbook workbook = new XSSFWorkbook();
			writeFinalScoresToWorkbook(chemicals, workbook);
			FileOutputStream out = new FileOutputStream(new File(outputFolder+File.separator+outputFileName));
			workbook.write(out);
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	XSSFCellStyle getStyleBorder(XSSFWorkbook wb) {
		XSSFCellStyle style=wb.createCellStyle();
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);    	
		return style;
	}

	public static XSSFCellStyle getStyleBold(XSSFWorkbook wb) {
		XSSFCellStyle style=wb.createCellStyle();
		XSSFFont font= wb.createFont();
		font.setBold(true);
		style.setFont(font);    	
		return style;
	}

	
	public static XSSFCellStyle getStyleWrap(XSSFWorkbook wb) {
		XSSFCellStyle style=wb.createCellStyle();
		style.setWrapText(true);
		return style;
	}
	
	public static XSSFCellStyle getStyleBoldWrap(XSSFWorkbook wb) {
		XSSFCellStyle style=wb.createCellStyle();
		XSSFFont font= wb.createFont();
		font.setBold(true);
		style.setFont(font);
		style.setWrapText(true);
		return style;
	}

	
	XSSFCellStyle getStyleBorderWithRotate(XSSFWorkbook wb) {
		XSSFCellStyle style=wb.createCellStyle();
		style.setBorderBottom(BorderStyle.MEDIUM);
		style.setBorderTop(BorderStyle.MEDIUM);
		style.setBorderRight(BorderStyle.MEDIUM);
		style.setBorderLeft(BorderStyle.MEDIUM);
		style.setRotation((short)90);
		return style;
	}

	XSSFCellStyle getStyleRotate(XSSFWorkbook wb) {
		XSSFCellStyle style=wb.createCellStyle();
		style.setRotation((short)90);
		return style;
	}
	
	public void writeFinalScoresToWorkbook(Chemicals chemicals, XSSFWorkbook workbook) {
		Vector <Group>groups=RowHCD.createGroups();
		writeFinalScoresToWorkbook(chemicals, workbook, groups);
	}
	
	public void writeFinalScoresToWorkbook(Chemicals chemicals, XSSFWorkbook workbook, Vector <Group>groups) {
		//Create a blank sheet
		XSSFSheet sheet = workbook.createSheet("Hazard Profiles");

		for (int i=1;i<=3;i++)	{
			XSSFRow row = sheet.createRow(i-1);	    
			for (int j=1;j<=20;j++)	{
				row.createCell(j-1);
			}
		}

		sheet.getRow(2).setHeightInPoints(120);	  

		

		writeHeaderFinalScoreTab(workbook, sheet,groups);

		Hashtable<String, CellStyle> htStyles = createScoreStylesHashtable(workbook);

		Vector<HazardCategory>cats=new Vector<>();
		for (int i=0;i<groups.size();i++) {
			Group groupi=groups.get(i);
			for (HazardCategory cat:groupi.categories) cats.add(cat);
		}
		
		
		for (int chemicalNum=0;chemicalNum<chemicals.size();chemicalNum++) {
			XSSFRow row=sheet.createRow(chemicalNum+3);	 

			Chemical chemical=chemicals.get(chemicalNum);
			
			for (int categoryNum=0;categoryNum<cats.size();categoryNum++) {			
				HazardCategory cat=cats.get(categoryNum);

				int prevCount=0;
				for (int j=0;j<categoryNum;j++) {
					prevCount+=RowHCD.getColumnCount(cats.get(j));
				}
				
				CellStyle csBorder=getStyleBorder(workbook);
				
				if (cat instanceof HazardCategorySpecific) {
					XSSFCell cell=row.createCell(prevCount);					
					
					if (cat.name.equals("CAS"))	{
						cell.setCellValue(chemical.CAS);
						cell.setCellStyle(csBorder);
					} else if (cat.name.equals("name")) {
						cell.setCellValue(chemical.name);
						cell.setCellStyle(csBorder);
					} else {
						Score score=chemical.getScore(cat.name);
						String final_score=score.final_score;
						if (score.records.size()>0) {
							if (final_score.contentEquals("N/A")) final_score="I";
							cell.setCellValue(final_score);
							cell.setCellStyle(htStyles.get(final_score));
		
						} else {
							cell.setCellValue("");
							cell.setCellStyle(htStyles.get(final_score));
						}
					}
					
				}  else if (cat instanceof HazardCategoryGeneral) {

					HazardCategoryGeneral catGen=(HazardCategoryGeneral)cat;
					for (int j=0;j<catGen.categories.size();j++) {
						XSSFCell cell=row.createCell(prevCount+j);					
						Score score=chemical.getScore(catGen.categories.get(j).name);
						String final_score=score.final_score;
						if (score.records.size()>0) {
							if (final_score.contentEquals("N/A")) final_score="I";
							cell.setCellValue(final_score);
							cell.setCellStyle(htStyles.get(final_score));
		
						} else {
							cell.setCellValue("");
							cell.setCellStyle(htStyles.get(final_score));
						}
					}
					
				}
			}
			
		}

		sheet.createFreezePane( 0, 3, 0, 3 );

	}

	private Hashtable<String, CellStyle> createScoreStylesHashtable(XSSFWorkbook workbook) {
		Hashtable<String,CellStyle>htStyles=new Hashtable<>();

		String []finalScores= {"VH","H","M","L","N/A","I"};

		for (String score:finalScores) {
			CellStyle cs=workbook.createCellStyle();
			cs.setVerticalAlignment(VerticalAlignment.CENTER);
			cs.setAlignment(HorizontalAlignment.CENTER);
			cs.setBorderBottom(BorderStyle.MEDIUM);
			cs.setBorderTop(BorderStyle.MEDIUM);
			cs.setBorderRight(BorderStyle.MEDIUM);
			cs.setBorderLeft(BorderStyle.MEDIUM);
			cs.setFillForegroundColor(getColorShort(score));
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			htStyles.put(score, cs);
		}
		return htStyles;
	}

	
	
	
	private void writeHeaderFinalScoreTab(XSSFWorkbook workbook, XSSFSheet sheet,Vector <Group>groups) {
		XSSFCellStyle styleBorderWithRotate=getStyleBorderWithRotate(workbook);
		//		XSSFCellStyle styleRotate=getStyleRotate(workbook);
		XSSFCellStyle styleBorder=getStyleBorder(workbook);

		int startColumn=0;
		int stopColumn=0;	
		
		Vector<HazardCategory>cats=new Vector<>();

		for (int i=0;i<groups.size();i++) {
			
			Group groupi=groups.get(i);
			
			for (HazardCategory cat:groupi.categories) cats.add(cat);
			int colCount=RowHCD.getColumnCount(groupi);
			
			if (i==0) {
				stopColumn=colCount-1;
			} else {
				int prevCount=0;
				for (int j=0;j<i;j++) {
					prevCount+=RowHCD.getColumnCount(groups.get(j));
				}
				startColumn=prevCount;
				stopColumn=startColumn+colCount-1;
			}
						
			CellAddress caStart=new CellAddress(0,startColumn);	
			CellAddress caStop=new CellAddress(0,stopColumn);
			String merge=caStart.formatAsString()+":"+caStop.formatAsString();
//			System.out.println(groupi.name+"\t"+merge);
			createMergedRegion(sheet,merge , groupi.name,styleBorder);
			
		}
		
		for (int i=0;i<cats.size();i++) {			
			HazardCategory cat=cats.get(i);

			int colCount=RowHCD.getColumnCount(cat);

			int prevCount=0;
			for (int j=0;j<i;j++) {
				prevCount+=RowHCD.getColumnCount(cats.get(j));
			}

			if (cat instanceof HazardCategorySpecific) {
				startColumn=prevCount;
				stopColumn=startColumn;
				CellAddress caStart=new CellAddress(1,startColumn);	
				CellAddress caStop=new CellAddress(2,stopColumn);
				String merge=caStart.formatAsString()+":"+caStop.formatAsString();
//				System.out.println(cat.name+"\t"+merge);				
				
				if (cat.name.equals("name") || cat.name.equals("CAS")) {
					createMergedRegion(sheet,merge , cat.name,styleBorder);
				} else {
					createMergedRegion(sheet,merge , cat.name,styleBorderWithRotate);
				}
				
			}  else if (cat instanceof HazardCategoryGeneral) {
				HazardCategoryGeneral catGen=(HazardCategoryGeneral)cat;
				
				startColumn=prevCount;
				stopColumn=startColumn+colCount-1;
				CellAddress caStart=new CellAddress(1,startColumn);	
				CellAddress caStop=new CellAddress(1,stopColumn);
				String merge=caStart.formatAsString()+":"+caStop.formatAsString();

				createMergedRegion(sheet,merge , cat.name,styleBorder);
				
				for (int j=0;j<catGen.categories.size();j++) {
					startColumn=prevCount+j;
					stopColumn=startColumn;
					String value=catGen.categories.get(j).name.replace("Acute Mammalian Toxicity", "");
					value=value.replace("Systemic Toxicity", "").replace("Neurotoxicity", "");
//					System.out.println(value+"\t"+startColumn);					
					createCell(sheet, 2, startColumn, value, true, workbook, styleBorderWithRotate);
				}
			}
		}
		
	}
	
	public static short getColorShort(String val) {
		if (val.equals("L")) {
			return IndexedColors.LIGHT_GREEN.getIndex();
		} else if (val.equals("M")) {
			return IndexedColors.LIGHT_YELLOW.getIndex();
		} else if (val.equals("H")) {
			return IndexedColors.LIGHT_ORANGE.getIndex();
		} else if (val.equals("VH")) {
			return IndexedColors.RED.getIndex();
		} else if (val.equals("I")) {
			return IndexedColors.GREY_25_PERCENT.getIndex();
		} else {
			return IndexedColors.WHITE.getIndex();
		}

	}

	
	
	void createMergedRegion(XSSFSheet sheet,String range,String value,XSSFCellStyle styleNoBorderWithRotate) {
		XSSFWorkbook wb = sheet.getWorkbook();
		createCell(sheet,range,value,wb,styleNoBorderWithRotate);

		CellRangeAddress region=org.apache.poi.ss.util.CellRangeAddress.valueOf(range);		
		sheet.addMergedRegion(region);
		RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
		RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);

	}

	void createCell(XSSFSheet sheet,int row,int col,String value,boolean addBorders,XSSFWorkbook wb,XSSFCellStyle style) {
		XSSFCell cell = sheet.getRow(row).getCell(col);
		sheet.getRow(row).createCell(col);
		createCell(cell, value, wb,style);
	}

	void createCell(XSSFSheet sheet,String range,String value,XSSFWorkbook wb,XSSFCellStyle style) {
		CellRangeAddress cra=org.apache.poi.ss.util.CellRangeAddress.valueOf(range);
		sheet.getRow(cra.getFirstRow()).createCell(cra.getFirstColumn());		
		XSSFCell cell = sheet.getRow(cra.getFirstRow()).getCell(cra.getFirstColumn());
		createCell(cell,value,wb,style);
	}


	void createCell(XSSFCell cell,String value,XSSFWorkbook wb,XSSFCellStyle style) {
//		System.out.println("Cell==null"+(cell==null));
//		System.out.println("value==null"+(value==null));
		cell.setCellValue(value);
		cell.setCellStyle(style);
		CellUtil.setVerticalAlignment(cell,VerticalAlignment.CENTER);
		CellUtil.setAlignment(cell,HorizontalAlignment.CENTER);
	}

	//	void createCell(XSSFCell cell,String value,boolean addBorders,short color) {
	//        
	//        cell.setCellValue(value);
	//        CellUtil.setVerticalAlignment(cell,VerticalAlignment.CENTER);
	//        CellUtil.setAlignment(cell,HorizontalAlignment.CENTER);
	//
	//        if (addBorders) {
	//        	XSSFCellStyle style=(XSSFCellStyle) cell.getCellStyle();
	//        	style.setBorderBottom(BorderStyle.MEDIUM);
	//        	style.setBorderTop(BorderStyle.MEDIUM);
	//        	style.setBorderRight(BorderStyle.MEDIUM);
	//        	style.setBorderLeft(BorderStyle.MEDIUM);
	//        	style.setFillForegroundColor(color);
	//        	style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	//        }
	//        
	//	}

	void createCell(XSSFCell cell,String value,CellStyle cs,short color) {
		cs.setFillForegroundColor(color);
		cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cell.setCellStyle(cs);
		cell.setCellValue(value);
	}
	
	public static void writeExcelFile(File inFile,XSSFWorkbook workbook) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(new File(inFile.getAbsolutePath()));
		workbook.write(out);
		out.close();
	}
	
	public static void main(String[] args) {
		TableGeneratorExcel tgExcel = new TableGeneratorExcel();
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		Chemicals chemicals=new Chemicals();
		Chemical chemical = createSampleChemical();
		chemicals.add(chemical);
		
		tgExcel.writeFinalScoresToWorkbook(chemicals, workbook);
        tgExcel.writeScoreRecordsToWorkbook(chemicals,workbook);
        
        File inFile=new File("test.xlsx");
        
        try {
			writeExcelFile(inFile, workbook);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}

	public static Chemical createSampleChemical() {
		Chemical chemical=new Chemical();
		chemical.CAS="71-43-2";
		chemical.name="Benzene";
		//TODO store SID so can retrieve dashboard URL later
				
		chemical.scores.get(0).final_score="M";
		chemical.scores.get(1).final_score="L";
		chemical.scores.get(2).final_score="VH";
		chemical.scores.get(3).final_score="VH";
		chemical.scores.get(4).final_score="VH";
		chemical.scores.get(5).final_score="H";
		chemical.scores.get(6).final_score="H";
		chemical.scores.get(7).final_score="H";
		chemical.scores.get(8).final_score="H";
		chemical.scores.get(9).final_score="N/A";
		chemical.scores.get(10).final_score="H";
		chemical.scores.get(11).final_score="H";
		chemical.scores.get(12).final_score="I";
		chemical.scores.get(13).final_score="H";
		chemical.scores.get(14).final_score="H";
		chemical.scores.get(15).final_score="H";
		chemical.scores.get(16).final_score="M";
		chemical.scores.get(17).final_score="H";
		chemical.scores.get(18).final_score="H";
		
		for (int i=0;i<chemical.scores.size();i++) {
			Score score=chemical.scores.get(i);
			ScoreRecord sr=new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);//dummy score record
			score.records.add(sr);
		}
		return chemical;
	}
}
