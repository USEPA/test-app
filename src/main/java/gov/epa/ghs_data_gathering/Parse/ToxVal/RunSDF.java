package gov.epa.ghs_data_gathering.Parse.ToxVal;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import AADashboard.Application.AADashboard;
import AADashboard.Application.TableGeneratorExcel;
import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Database.DSSToxRecord;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;


/**
* @author TMARTI02
*/
public class RunSDF {

	
	AtomContainerSet loadSDFFromJar(String filepath) {
		try {
			
//			System.out.println(filepath);
			
//			java.io.InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filepath);
			java.io.InputStream ins = new FileInputStream(filepath);

			
			
			IteratingSDFReader reader = new IteratingSDFReader(ins, DefaultChemObjectBuilder.getInstance());

			AtomContainerSet acs=new AtomContainerSet();
			
			while (reader.hasNext()) {
				IAtomContainer m = (IAtomContainer)reader.next();
				
//				Set set=m.getProperties().keySet();
//				Iterator iter = set.iterator();
//				while (iter.hasNext()) {
//				    System.out.println(iter.next());
//				}
				
				DSSToxRecord dr=new DSSToxRecord();
				m.setProperty("DSSToxRecord", dr);
				dr.sid=m.getProperty("DTXSID");
				dr.cas=m.getProperty("CASRN");
				dr.name=m.getProperty("PREFERRED_NAME");
				dr.cid=m.getProperty("DTXCID");
				dr.smiles=m.getProperty("SMILES");
				acs.addAtomContainer(m);
			}
			reader.close();
			
			return acs;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public void loadTrainingData(CalculationParameters params) {
		long t1 = System.currentTimeMillis();
		for (String endpoint : params.endpoints) {		
			for (String method : params.methods) {
				WebTEST4.loadTrainingData(endpoint, method);
			}
		}
		long t2 = System.currentTimeMillis();
	}

	
	public void runSDF() {
		AADashboard aad=new AADashboard();
		
//		String versionToxVal=ParseToxValDB.v8;
		String versionToxVal="v96";
		String filepath="reports/most hazard records.sdf";
		
		
		AtomContainerSet acs=loadSDFFromJar(filepath);
						
//		Connection conn=null;
//		Statement statToxVal=null;
//
//		try {
//			if (versionToxVal.equals(ParseToxValDB.v8)) {
//				conn=MySQL_DB.getConnection(ParseToxValDB.DB_Path_AA_Dashboard_Records_v8);
//			} else if (versionToxVal.equals(ParseToxValDB.v94)) {
//				conn=MySQL_DB.getConnection(ParseToxValDB.DB_Path_AA_Dashboard_Records_v94);
////				conn=SqlUtilities.getConnectionToxVal();//get connection to database server				
//			} else {
//				System.out.println("Invalid toxval version");
//				return;
//			}
//			statToxVal=conn.createStatement();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			System.out.println("Couldnt connect to toxval db");
//			return;
//		}
		
		try {

			Chemicals chemicals=new Chemicals();
			
			List<String> endpoints= Arrays.asList(TESTConstants.ChoiceRat_LD50,
					TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50,
					TESTConstants.ChoiceMutagenicity,TESTConstants.ChoiceReproTox,
					TESTConstants.ChoiceBCF,
					TESTConstants.ChoiceWaterSolubility);

			List<String> methods= Arrays.asList(TESTConstants.ChoiceConsensus);
			
			Set<WebReportType> wrt = WebReportType.getNone();
			wrt.add(WebReportType.HTML);
			
			CalculationParameters cp=new CalculationParameters(null, null, endpoints, methods, wrt);
			loadTrainingData(cp);
							

//			System.out.println("ac count="+moleculeSet.getAtomContainerCount());
			
			//			for (int i=0;i<1;i++) {
			
			int totalRecordCount=0;
			
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				
				DSSToxRecord dr=ac.getProperty("DSSToxRecord");
				ac.setProperty("CAS",dr.cas);//need to refactor so not needed
				
				WebTEST4.checkAtomContainer(ac);
				
				String error=ac.getProperty("Error");
				error=error.replace("\n", "");
				error=error.replace("\r", "");
				error=error.trim();
				ac.setProperty("Error", error);
				Chemical chemical=aad.runChemicalForGUI(ac,cp);
				chemicals.add(chemical);
				
				for (Score score:chemical.scores) {
					totalRecordCount+=score.records.size();
				}
				
				
			}
			
			TreeMap<String,List<ScoreRecord>>htRecords=new TreeMap<>();
			
			for (Chemical chemical:chemicals) {
				for (Score score:chemical.getScores()) {
					for (ScoreRecord scoreRecord:score.records) {
						if(htRecords.get(scoreRecord.source)==null) {
							List<ScoreRecord>records=new ArrayList<>();
							records.add(scoreRecord);
							htRecords.put(scoreRecord.source, records);
						} else {
							List<ScoreRecord>records=htRecords.get(scoreRecord.source);
							records.add(scoreRecord);
						}
					}
				}
				
			}
			
			System.out.println("totalRecordCount="+totalRecordCount+"\n");
			
			
			for (String source:htRecords.keySet()) {
				System.out.println(source+"\t"+htRecords.get(source).size());
			}
			
//			WebTEST4.printJSON(chemicals);

			XSSFWorkbook workbook = new XSSFWorkbook();
			TableGeneratorExcel tgExcel = new TableGeneratorExcel();
			tgExcel.writeFinalScoresToWorkbookSimple(chemicals, workbook);
	        tgExcel.writeScoreRecordsToWorkbook(chemicals,workbook);

			FileOutputStream out = new FileOutputStream(new File("reports/most chemicals toxval "+versionToxVal+".xlsx"));
			workbook.write(out);
			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(acs.getAtomContainerCount());
	}
	
	public static void main(String[] args) {
//		System.out.println("here");
		RunSDF ht=new RunSDF();
		ht.runSDF();
	}
}
