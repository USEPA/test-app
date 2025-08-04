package AADashboard.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

//import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
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
import ToxPredictor.Application.GUI.TESTApplication;
import AADashboard.Application.RowHCD.HazardCategoryGeneral;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.RecordLink;
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
	
	public void writeLinksSheet(String[] headers, String sheetName, XSSFWorkbook wb,Vector<RecordLink>links) {
	
		CellStyle hlink_style = wb.createCellStyle();
		Font hlink_font = wb.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
		hlink_style.setFont(hlink_font);
		
		
		Sheet recSheet = wb.createSheet(sheetName);
//		Row recSubtotalRow = recSheet.createRow(0);
		Row recHeaderRow = recSheet.createRow(0);
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setBold(true);
		style.setFont(font);
		for (int i = 0; i < headers.length; i++) {
			Cell recCell = recHeaderRow.createCell(i);
			recCell.setCellValue(headers[i]);
			recCell.setCellStyle(style);
		}
		int recCurrentRow = 1;
		
		for (RecordLink rl:links) {
			
			
			Class rlClass = rl.getClass();
			Object value = null;
			try {
				Row row = recSheet.createRow(recCurrentRow);
				recCurrentRow++;
				 
				for (int i = 0; i < headers.length; i++) {
					
					Field field = rlClass.getDeclaredField(headers[i]);
					value = field.get(rl);
					
					if (value==null) continue;
					
					if (headers[i].contentEquals("URL")) {
						String strValue = (String) value;
						Cell cell = row.createCell(i);     						

						cell.setCellStyle(hlink_style);						
						if (strValue.length() > 32767) { strValue = strValue.substring(0,32767); }
						cell.setCellValue(strValue);

						Hyperlink href = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
						href.setAddress(strValue);
						cell.setHyperlink(href);
						
					} else if (value instanceof String) { 
						String strValue = (String) value;
						if (strValue.length() > 32767) { strValue = strValue.substring(0,32767); }
						row.createCell(i).setCellValue(strValue);
					} 
				}
			} catch (Exception ex) {
//				ex.printStackTrace();
				System.out.println("error store value in excel: "+value.toString());
			}
		}
		
		String lastCol = CellReference.convertNumToColString(headers.length-1);
		recSheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastCol+recCurrentRow));
		recSheet.createFreezePane(0, 1);
		
		for (int i = 0; i < headers.length; i++) {
			String col = CellReference.convertNumToColString(i);

//			String recSubtotal = "SUBTOTAL(3,"+col+"$3:"+col+"$"+(recCurrentRow+1)+")";
//			recSubtotalRow.createCell(i).setCellFormula(recSubtotal);
			
			recSheet.autoSizeColumn(i);
			if (recSheet.getColumnWidth(i)>50*256) {
				recSheet.setColumnWidth(i, 50*256);
			}

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
			
			CellStyle hlink_style = workbook.createCellStyle();
			Font hlink_font = workbook.createFont();
			hlink_font.setUnderline(Font.U_SINGLE);
			hlink_font.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
			hlink_style.setFont(hlink_font);

			
			String del="|";

			ArrayList<String>uniqueCAS=new ArrayList<>();

			//Create a blank sheet
			XSSFSheet sheet = workbook.createSheet("Hazard Records");

			int rowNum=0;
			XSSFRow row = sheet.createRow(rowNum);

			XSSFCellStyle styleBold=getStyleBold(workbook);

			String [] headers=ScoreRecord.allFieldNames;
			
			
			//Write header
			for (int i=0;i<headers.length;i++)	{
				XSSFCell cell=row.createCell(i);
				cell.setCellValue(headers[i]);
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

//					System.out.println(list.size());
					
					for (int i=0;i<list.size();i++) {
						XSSFCell cell=row.createCell(i);

						String value="";

						if (list.get(i).length()>32000) {
							value=list.get(i).substring(0,32000)+"...";
							cell.setCellValue(value);
						} else {
							value=list.get(i);
							
							if (headers[i].equals("valueMass") && !value.isEmpty()) {
								try {
									cell.setCellValue(Double.parseDouble(value));
								} catch (Exception ex) {							
								}

							} else if (headers[i].equals("toxvalID") && !value.isEmpty()) {
								try {
									cell.setCellValue(Integer.parseInt(value));
								} catch (Exception ex) {							
								}
							} else { 
								cell.setCellValue(value);	
							}
							
							if (headers[i].equals("url") && !value.contains(";")) {

								try {
									URL u = new URL(value); // this would check for the protocol
									u.toURI();
									Hyperlink href = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
									href.setAddress(value);
									cell.setHyperlink(href);
									cell.setCellStyle(hlink_style);						

								} catch (Exception ex) {
									if (value.length()>1) System.out.println("Invalid URL:"+value);
								}
							}
						}
					}
				}

				//				fw.write(chemical.to);
			}
						

			sheet.createFreezePane( 1, 1, 1, 1 );
			
			int lastRow=rowNum;
			Row rowHeader=sheet.getRow(0);
			int lastColumn=rowHeader.getLastCellNum()-1;
			CellAddress caLast=new CellAddress(lastRow,lastColumn);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+caLast.formatAsString()));
			
			
			for (int i=0;i<lastColumn;i++) {
//				if (i!=1 & i!=7 & i!=9 && i!=10 && i!=11 && i!=12) {
//					sheet.autoSizeColumn(i);
//				}
//				else
//					sheet.setColumnWidth(i, 50*256);
				sheet.autoSizeColumn(i);
				if (sheet.getColumnWidth(i)>50*256) {
					sheet.setColumnWidth(i, 50*256);
				}
			}


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
		Vector <Group>groups=null;		
		if (TESTApplication.forMDH) groups=RowHCD.createGroupsMDH();
		else groups=RowHCD.createGroups();
		
		writeFinalScoresToWorkbook(chemicals, workbook, groups);
	}
	
	public void writeFinalScoresToWorkbookSimple(Chemicals chemicals, XSSFWorkbook workbook) {
		Vector <Group>groups=null;		
		if (TESTApplication.forMDH) groups=RowHCD.createGroupsMDH_Simple();
		else groups=RowHCD.createGroupsSimple();
		
		writeFinalScoresToWorkbookSimple(chemicals, workbook, groups);
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
	
	public void writeFinalScoresToWorkbookSimple(Chemicals chemicals, XSSFWorkbook workbook, Vector <Group>groups) {
		//Create a blank sheet
		XSSFSheet sheet = workbook.createSheet("Hazard Profiles");

		writeHeaderFinalScoreTabSimple(workbook, sheet,groups);

		Hashtable<String, CellStyle> htStyles = createScoreStylesHashtable(workbook);

		Vector<HazardCategory>cats=new Vector<>();//make vector to flatten groups
		for (int i=0;i<groups.size();i++) {
			Group groupi=groups.get(i);
			for (HazardCategory cat:groupi.categories) cats.add(cat);
		}
		
		CellStyle csBorder=getStyleBorder(workbook);
		
		for (int chemicalNum=0;chemicalNum<chemicals.size();chemicalNum++) {
			XSSFRow row=sheet.createRow(chemicalNum+1);	 

			Chemical chemical=chemicals.get(chemicalNum);
			
			for (int categoryNum=0;categoryNum<cats.size();categoryNum++) {			
				HazardCategory cat=cats.get(categoryNum);

				XSSFCell cell=row.createCell(categoryNum);
				
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
			}
			
		}

		sheet.createFreezePane( 0, 1, 0, 1 );
		String lastCol = CellReference.convertNumToColString(cats.size()-1);
		sheet.setAutoFilter(CellRangeAddress.valueOf("A1:"+lastCol+chemicals.size()));


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
	
	private void writeHeaderFinalScoreTabSimple(XSSFWorkbook workbook, XSSFSheet sheet,Vector <Group>groups) {
		XSSFCellStyle styleBorderWithRotate=getStyleBorderWithRotate(workbook);
		//		XSSFCellStyle styleRotate=getStyleRotate(workbook);
		XSSFCellStyle styleBorder=getStyleBorder(workbook);

		
		XSSFRow row = sheet.createRow(0);
		row.setHeightInPoints(180);	  
		
		int count=0;
		
		for (Group group:groups) {
			for (HazardCategory cat:group.categories) {
				XSSFCell cell=row.createCell(count++);
				
				if (group.name.contentEquals("Identifiers"))
					createCell(cell, cat.name, workbook,styleBorder);
				else 
					createCell(cell, cat.name, workbook,styleBorderWithRotate);
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
