package ToxPredictor.Application.Calculations;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.Calculations.RunFromCommandLine.RunFromSmiles;
import ToxPredictor.Application.model.PredictionResults;
import ToxPredictor.Database.DSSToxRecord;

public class RunFromSmilesTEST {
	
	private Hashtable<String,Double> getTEST5Results(String filename) {
		
		try {
			Hashtable<String,Double>htPreds=new Hashtable<>();
			
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);	         
	        Workbook workbook = new XSSFWorkbook(inputStream);
	        Sheet sheet = workbook.getSheetAt(0);
	        
	        for (int i=1;i<sheet.getLastRowNum();i++) {
	        	Row row=sheet.getRow(i);
	        	
	        	String CAS=row.getCell(1).getStringCellValue();
	        	String Pred=row.getCell(6).getStringCellValue();
	        	
	        	Double dPred=null;
	        	
	        	if (!Pred.equals("N/A")) dPred=Double.parseDouble(Pred);
	        	else dPred=Double.NaN;
	        	
	        	htPreds.put(CAS, dPred);
//	        	System.out.println(CAS+"\t"+Pred);
//	        	System.out.println(DTXSID+"\t"+CAS+"\t"+Smiles);
	        	
	        }
	        
	        workbook.close();	        
	        return htPreds;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	@Test 
	public void runBenzene() {
		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		AtomContainerSet acs=new AtomContainerSet();
		AtomContainer ac=RunFromSmiles.createMolecule("c1ccccc1", "DTXSID3039242","71-43-2");
		acs.addAtomContainer(ac);//valid simple molecule
		Hashtable<String,PredictionResults>htResults=RunFromSmiles.runEndpoint(acs, endpoint, method,createReports,createDetailedReports,DSSToxRecord.strSID);		
		String pred=htResults.get(ac.getProperty(DSSToxRecord.strSID)).getPredictionResultsPrimaryTable().getPredToxValue();
		assertTrue(pred.equals("3.28"));
	}
	
	@Test 
	public void runSulfur() {
		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		AtomContainerSet acs=new AtomContainerSet();
		AtomContainer ac=RunFromSmiles.createMolecule("[S]", "DTXSID9034941","7704-34-9");//has no carbon
		acs.addAtomContainer(ac);//valid simple molecule
		Hashtable<String,PredictionResults>htResults=RunFromSmiles.runEndpoint(acs, endpoint, method,createReports,createDetailedReports,DSSToxRecord.strSID);		
		String error=htResults.get(ac.getProperty(DSSToxRecord.strSID)).getError();
		assertTrue(error.equals("Only one nonhydrogen atom"));		
//		System.out.println(error);
		
//		assertTrue(pred.equals("3.28"));
	}
	
	
	@Test 
	public void runBadSmiles() {
		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		AtomContainerSet acs=new AtomContainerSet();
		AtomContainer ac=RunFromSmiles.createMolecule("XXXX", "DTXSID2","123-45-6");
		acs.addAtomContainer(ac);//valid simple molecule
		Hashtable<String,PredictionResults>htResults=RunFromSmiles.runEndpoint(acs, endpoint, method,createReports,createDetailedReports,DSSToxRecord.strSID);		
		String error=htResults.get(ac.getProperty(DSSToxRecord.strSID)).getError();

//		System.out.println("*"+error+"*");
		assertTrue(error.contains("could not parse"));		
	}
	
	
	@Test 
	public void runSalt() {
		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)
		boolean createReports=true;//whether to store report
		boolean createDetailedReports=false;//detailed reports have lots more info and creates more html files
		AtomContainerSet acs=new AtomContainerSet();
		AtomContainer ac=RunFromSmiles.createMolecule("Cl.CCC(=O)OC1(CCN(CC#CC2=CC=CC=C2)CC1)C1=CC=CC=C1", "DTXSID20211176","62119-86-2");

		acs.addAtomContainer(ac);//valid simple molecule
		Hashtable<String,PredictionResults>htResults=RunFromSmiles.runEndpoint(acs, endpoint, method,createReports,createDetailedReports,DSSToxRecord.strSID);		
		String error=htResults.get(ac.getProperty(DSSToxRecord.strSID)).getError();

//		System.out.println("*"+error+"*");
		assertTrue(error.contains("Molecules can only contain one fragment"));		
	}
	

	
	@Test	
	/**
	 * Compares TEST5.1 predictions to new RunFromSmiles class using excel files 
	 * 
	 */
	public void runLC50_FHM_Test_set() {

		String endpoint =TESTConstants.ChoiceFHM_LC50;//what property or toxicity endpoint being predicted
		String method =TESTConstants.ChoiceConsensus;//what QSAR method being used (default- runs all methods and takes average)

		String excelFileName="CCD-Batch-Search_FHM_LC50_Prediction_Set.xlsx";
		String testPredFile="TEST5.1_FHM_LC50_Prediction_Set_Results.xlsx";
		
		try {

			//Get old results from TEST5.1 prediction excel file:
			Hashtable<String,Double>htPreds=getTEST5Results(testPredFile);

			//get chemicals to run from batch search excel file:
			AtomContainerSet acs = getDSSToxRecords(excelFileName); 
	        
			//Generate new predictions using RunFromSmiles class:
			Hashtable<String,PredictionResults>htResults=RunFromSmiles.runEndpoint(acs, endpoint, method,false,false,DSSToxRecord.strCAS);		

	        Enumeration<String> e = htResults.keys();
	        
	        double tol=0.01;
	        
	        while (e.hasMoreElements()) {
	        	String CAS=e.nextElement();
	        	
	        	Double oldTESTprediction=htPreds.get(CAS);
	        	String strNewPrediction=htResults.get(CAS).getPredictionResultsPrimaryTable().getPredToxValue();

	        	Double newPrediction=null;
	        		        	
	        	if (!strNewPrediction.equals("N/A")) 
	        		newPrediction=Double.parseDouble(strNewPrediction);
	        	else
	        		newPrediction=Double.NaN;

	        	double diff=Math.abs(oldTESTprediction-newPrediction);
	        	
	        	System.out.println(CAS+"\t"+oldTESTprediction+"\t"+newPrediction);
	        	
	        	if (oldTESTprediction.equals(Double.NaN)) {
	        		Assert.assertTrue(newPrediction.equals(Double.NaN));
	        	} else {
//	    	        if (diff>tol) System.out.println(CAS+"\t"+oldTESTprediction+"\t"+newPrediction);	        		
	        		Assert.assertTrue(diff<tol);
	        	}
	        }
	        
//	        System.out.println(htResults.get("3206-31-3").getPredictionResultsPrimaryTable().getPredToxValue());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/**
	 * 
	 * @param excelFileName - file name for excel file exported from dashboard batch search
	 * @return
	 * @throws IOException
	 */
	private AtomContainerSet getDSSToxRecords(String excelFileName) throws IOException {
		AtomContainerSet acs=new AtomContainerSet();		

		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(excelFileName);	         
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet sheet = workbook.getSheetAt(1);//assumes results are in second tab from file exported from batch search
		Row row0=sheet.getRow(0);
		
		Hashtable<String,Integer>htCols=new Hashtable<>();
		
		for (int i=0;i<row0.getLastCellNum();i++) {
			Cell cell=row0.getCell(i);
			htCols.put(cell.getStringCellValue(), i);//Store the column number associated with the column name:
		}
		
		for (int i=1;i<sheet.getLastRowNum();i++) {
			Row row=sheet.getRow(i);
			String DTXSID=row.getCell(htCols.get("DTXSID")).getStringCellValue();
			String CAS=row.getCell(htCols.get("CASRN")).getStringCellValue();
			String Smiles=row.getCell(htCols.get("SMILES")).getStringCellValue();
			acs.addAtomContainer(RunFromSmiles.createMolecule(Smiles,DTXSID,CAS));
//	        	System.out.println(DTXSID+"\t"+CAS+"\t"+Smiles);
		}		
		workbook.close();		
		return acs;
	}
	
}
