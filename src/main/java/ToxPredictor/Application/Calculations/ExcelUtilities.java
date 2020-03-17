package ToxPredictor.Application.Calculations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import AADashboard.Application.TableGenerator;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Utilities.Utilities;

public class ExcelUtilities {

	
	private static void writeTextFileToExcelSheet(XSSFSheet sheet, File file,String del) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));

			int rowNum=0;

			int maxNumCols=0;
			
			XSSFCellStyle styleBold=TableGenerator.getStyleBold(sheet.getWorkbook());
			boolean haveNote=false;
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;

				XSSFRow rowHeader = sheet.createRow(rowNum++);
				
				if (rowNum==1 && Line.contains("Note:")) haveNote=true;
				
				
				LinkedList<String>list=Utilities.Parse3(Line, del);
				
				if (list.size()>maxNumCols) maxNumCols=list.size();
				
//				String [] vals=Line.split(del);
				
		        for (int i=0;i<list.size();i++) {
		        	
		        	XSSFCell cell = rowHeader.createCell(i);
//					cell.setCellValue(vals[i]);
					cell.setCellValue(list.get(i));
					
					if ((haveNote && rowNum==2) || (!haveNote && rowNum==1)) {
						cell.setCellStyle(styleBold);
					}
					
					
		        }		        
			}
			
			if (!haveNote)
				sheet.createFreezePane(0,1);
			else 
				sheet.createFreezePane(0,2);
			
			
	        for (int i=0;i<maxNumCols;i++) {
	        	if ((i==0 && !haveNote) || i>0)
	        		sheet.autoSizeColumn(i);	
	        }
	        
			
			br.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void createSpreadsheetFromTextFiles(Vector<File>files,File outputFile,String del) {
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		for (File file:files) {
			String filename=file.getName().replace("_", " ");
			
			if (filename.contains(TESTConstants.ChoiceConsensus)) filename=TESTConstants.ChoiceConsensus;
			else if (filename.contains(TESTConstants.ChoiceHierarchicalMethod)) filename=TESTConstants.ChoiceHierarchicalMethod;
			else if (filename.contains(TESTConstants.ChoiceSingleModelMethod)) filename=TESTConstants.ChoiceSingleModelMethod;
			else if (filename.contains(TESTConstants.ChoiceGroupContributionMethod)) filename=TESTConstants.ChoiceGroupContributionMethod;
			else if (filename.contains(TESTConstants.ChoiceNearestNeighborMethod)) filename=TESTConstants.ChoiceNearestNeighborMethod;
			else if (filename.contains(TESTConstants.ChoiceLDA)) filename=TESTConstants.ChoiceLDA;
			else if (filename.contains("all methods"))  filename="All results";
			else if (filename.contains(TESTConstants.ChoiceDescriptors))  filename=TESTConstants.ChoiceDescriptors;
			
			
//			System.out.println(filename);
			
			XSSFSheet sheet = workbook.createSheet(filename);		
			writeTextFileToExcelSheet(sheet, file,del);
		}
		
		try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

		
	}
	
	public static void main(String[] args) throws Exception {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\danny chang\\MyToxicity6\\ToxRuns\\";
		
		File outputFile=new File(folder+"Batch_Fathead_minnow_LC50_(96_hr)_Consensus_3.xlsx");
		
		File file1=new File(folder+"Batch_Fathead_minnow_LC50_(96_hr)_Consensus_3.txt");
		File file2=new File(folder+"Batch_Fathead_minnow_LC50_(96_hr)_all_methods_3.txt");
		
		Vector<File>files=new Vector<>();
		files.add(file1);
		files.add(file2);
		
		createSpreadsheetFromTextFiles(files, outputFile, "|");
		
	}
}
