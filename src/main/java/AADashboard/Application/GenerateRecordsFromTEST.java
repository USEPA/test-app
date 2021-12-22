package AADashboard.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesParser;

import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST2;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.PredictToxicityWebPageCreator;
import ToxPredictor.Database.ChemistryDashboardRecord2;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.FormatUtils;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.misc.MolFileUtilities;
import gov.epa.api.Chemical;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;
import gov.epa.api.TESTRecord;

/**
 * Class to create score records using T.E.S.T. predictions
 * 
 * @author Todd Martin
 *
 */
public class GenerateRecordsFromTEST {

//	String databasePath = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0000 WebTEST\\results NCCT\\TEST_Results.db";
//	static String databasePath="R:\\NCCT Results 11_16_17\\test_results.db";
	

	MolFileUtilities mfu=new MolFileUtilities();
	
/**
 * This version uses WebTEST4 so that it runs single chemical and doesnt create any extra files
 * @param chemical
 * @param ac
 */
	public void createRecordsForGUI(Chemical chemical,AtomContainer ac,CalculationParameters cp)  {
		
		try {
			
			//**************************************************************
//			WebTEST2.usePreviousDescriptors=true;
//			List<TESTPredictedValue>vecTPV=WebTEST2.go(acs, cp);

			//Utilize old WebTEST class until we can create 800K TEST prediction database:
			
			DescriptorData dd=WebTEST4.goDescriptors(ac);			
						
			List<TESTPredictedValue>vecTPV=WebTEST4.go2(true,ac, dd,cp);
		
			
			long t1=System.currentTimeMillis();
			createRecords_From_TEST(chemical, vecTPV);
					
			
			long t2=System.currentTimeMillis();			
//			System.out.println("Time to get records from TEST models for "+chemical.CAS+"\t"+(t2-t1));
			
//			createRecord_From_TEST_Prediction_Database(chemical,statTEST);
//			createRecordFromNewCalculation(chemical,wt);
			//**************************************************************
			
			
			//TODO fix based on quantitive vals in ScoreRecord...
			adjustAquaticToxicityTESTUsingWaterSolubility(chemical);
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	public void createRecords(Chemical chemical,String dbPathTEST_Predictions)  {
		
		AtomContainerSet acs=new AtomContainerSet();
		acs.addAtomContainer(chemical.atomContainer);
				
		String [] endpoints= {TESTConstants.ChoiceWaterSolubility,TESTConstants.ChoiceRat_LD50,
				TESTConstants.ChoiceFHM_LC50,TESTConstants.ChoiceDM_LC50,
				TESTConstants.ChoiceMutagenicity,TESTConstants.ChoiceReproTox,
				TESTConstants.ChoiceBCF,
				TESTConstants.ChoiceEstrogenReceptor,
				TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity};

		String [] methods= {TESTConstants.ChoiceConsensus};
		
		Set<WebReportType> wrt = WebReportType.getNone();
		
		CalculationParameters cp=new CalculationParameters(dbPathTEST_Predictions, null, endpoints, methods, wrt);
		

		try {
			
			//**************************************************************
//			WebTEST2.usePreviousDescriptors=true;
//			List<TESTPredictedValue>vecTPV=WebTEST2.go(acs, cp);

			//Utilize old WebTEST class until we can create 800K TEST prediction database:
			List<TESTPredictedValue>vecTPV=WebTEST.go(acs, cp);
			
			long t1=System.currentTimeMillis();
			createRecords_From_TEST(chemical, vecTPV);
					
			
			long t2=System.currentTimeMillis();			
//			System.out.println("Time to get records from TEST models for "+chemical.CAS+"\t"+(t2-t1));
			
//			createRecord_From_TEST_Prediction_Database(chemical,statTEST);
//			createRecordFromNewCalculation(chemical,wt);
			//**************************************************************
			
			
			//TODO fix based on quantitive vals in ScoreRecord...
			adjustAquaticToxicityTESTUsingWaterSolubility(chemical);
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	
	private void adjustAquaticToxicityTESTUsingWaterSolubility(Chemical chemical) {
		 
		double WS_TEST=-1;
		String WS_TEST_Source="N/A";
		
		ArrayList<ScoreRecord> recordsWS=chemical.scoreWaterSolubility.records;
				
		for (int i=0;i<recordsWS.size();i++) {
			ScoreRecord sr=recordsWS.get(i);
			
//			System.out.println(sr.valueMass+"\t"+sr.source);

			if (sr.valueMass==null) continue;
			
			WS_TEST=sr.valueMass;
			WS_TEST_Source=sr.source;

			if (sr.source.equals(ScoreRecord.sourceTEST_Experimental)) {
				break;
			}
						
		}
		
//		System.out.println("WS_TEST="+WS_TEST+"\tsource="+WS_TEST_Source);
		

		if (WS_TEST==-1) {
//			System.out.println("No water sol");
			return;
		}
		
//		System.out.println(chemical.CAS+"\t"+WS_TEST+"\t"+WS_TEST_Source);
		
		ArrayList<ScoreRecord> recordsAAT=chemical.scoreAcute_Aquatic_Toxicity.records;
		
//		WS_TEST=1; for debug
		
		DecimalFormat df=new DecimalFormat("0.00E00");
		
		for (int i=0;i<recordsAAT.size();i++) {
			ScoreRecord sr=recordsAAT.get(i);
			
			if (sr.source.equals(ScoreRecord.sourceTEST_Predicted) && sr.valueMass!=null) {
				
				if (sr.valueMass>WS_TEST) {
//					System.out.println(chemical.CAS+"\t"+sr.valueMass+"\t"+WS_TEST);
					
					sr.score=ScoreRecord.scoreL;//set score to L since tox value will never be reached
					
					sr.rationale="Predicted 96 hour fathead minnow LC50 of "+df.format(sr.valueMass)+ " mg/L is greater than the ";
					
					if (WS_TEST_Source.equals(ScoreRecord.sourceTEST_Predicted)) {
						sr.rationale+=" TEST predicted water solubility of ";
					} else if (WS_TEST_Source.equals(ScoreRecord.sourceTEST_Experimental)) {
						sr.rationale+=" TEST experimental water solubility of ";//+WS_TEST+" mg/L";
					}
					sr.rationale+=df.format(WS_TEST)+" mg/L";
				} else {
//					System.out.println(chemical.CAS+"\t"+sr.valueMass+"\t"+WS_TEST);
				}
			}
		}//end loop over aquatic toxicity records
		
	}

	
	String checkStructure(String CAS, IAtomContainer ac) {
		
		String error="";
		
		if (ac==null) {
			error= "Structure is missing";
		} else if (ac.getAtomCount()==0) {
			error ="No atoms";
		} else if (ac.getAtomCount() == 1) {
			error= "Only one nonhydrogen atom";
		} else if (mfu.HaveBadElement(ac)) {
			error= "Contains unsupported element";
		} else if (!mfu.HaveCarbon(ac)) {
			error= "Molecule does not contain carbon";
		} else {
			AtomContainerSet moleculeSet2 = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(ac);
			if (moleculeSet2.getAtomContainerCount() > 1) {
				error="multiple molecules";
			}
				
		}
		if (error.equals("")) return "OK";
		else return "error: "+error+", CAS="+CAS;
		
	}
	

	


	private TESTRecord createTestRecord(Chemical chemical, TESTPredictedValue v) {
		TESTRecord tr = new TESTRecord();
		tr.CAS = chemical.CAS;

		if (v.expValMass.equals(Double.NaN)) {
			tr.ExpToxValue = "N/A";
		} else {
			tr.ExpToxValue = v.expValMass + "";
		}

		if (v.predValMass.equals(Double.NaN)) {
			tr.Consensus = "N/A";
		} else {
			tr.Consensus = v.predValMass + "";
		}

		return tr;
	}

	private TESTRecord createTestRecordLog(Chemical chemical, TESTPredictedValue v) {
		TESTRecord tr = new TESTRecord();
		tr.CAS = chemical.CAS;

		if (v.expValLogMolar.equals(Double.NaN)) {
			tr.ExpToxValue = "N/A";
		} else {
			tr.ExpToxValue = v.expValLogMolar + "";
		}

		if (v.predValLogMolar.equals(Double.NaN)) {
			tr.Consensus = "N/A";
		} else {
			tr.Consensus = v.predValLogMolar + "";
		}

		return tr;
	}

	private TESTPredictedValue runTESTCalculation(Chemical chemical, AtomContainerSet acs, String method,
			String endpoint)  {

		try {
			List<TESTPredictedValue> list = WebTEST.go(acs,
					new CalculationParameters(null, null, endpoint, method, WebReportType.getNone()));
			TESTPredictedValue v = list.get(0);
			
//			System.out.println(chemical.getCAS() + "\t" + v.expValMass + "\t" + v.predValMass);
			return v;
			

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	

	private static TESTPredictedValue runTESTCalculation(DescriptorData dd, String method,String endpoint)  {
		try {
			List<TESTPredictedValue> v = WebTEST.go(dd, new CalculationParameters(null, null, endpoint, method, WebReportType.getNone()));
//			System.out.println(chemical.getCAS() + "\t" + v.expValMass + "\t" + v.predValMass);
			return v.get(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	private static DescriptorData runTESTDescriptors(IAtomContainer ac)  {

		try {
			
	        Set<WebReportType> wrt = WebReportType.getNone();
			DescriptorData dd = WebTEST.goDescriptors(ac, wrt);
//			System.out.println(chemical.getCAS() + "\t" + v.expValMass + "\t" + v.predValMass);
			return dd;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	static boolean haveTESTPrediction(Score score) {
		
		for (int j=0;j<score.records.size();j++) {
			ScoreRecord sr=score.records.get(j);
			if (sr.source.equals(ScoreRecord.sourceTEST_Predicted)) {
				return true;
			}
		}
		return false;
	}

	
	private void setAcuteMammalianToxicityScore(double LD50_mg_kg, ScoreRecord sr) {
		// System.out.println(value);

		DecimalFormat df = new DecimalFormat("0");

		if (LD50_mg_kg <= 50) {
			sr.score = "VH";
			sr.rationale = "Oral rat LD50 (" + df.format(LD50_mg_kg) + " mg/kg) < 50 mg/kg";

		} else if (LD50_mg_kg > 50 && LD50_mg_kg <= 300) {
			sr.score = "H";
			sr.rationale = "50 mg/kg < Oral rat LD50 (" + df.format(LD50_mg_kg) + " mg/kg) <=300 mg/kg";
		} else if (LD50_mg_kg > 300 && LD50_mg_kg <= 2000) {
			sr.score = "M";
			sr.rationale = "300 mg/kg < Oral rat LD50 (" + df.format(LD50_mg_kg) + " mg/kg) <=2000 mg/kg";
		} else {// >2000
			sr.score = "L";
			sr.rationale = "Oral rat LD50 (" + df.format(LD50_mg_kg) + " mg/kg) > 2000 mg/kg";
		}
	}

	
	
	
	
	private void setAcuteAquaticToxicityScore(double LC50_mg_L, ScoreRecord sr,String endpoint) {

		// i.e. if concentration is greater than the water solubility, then score is low

//		DecimalFormat df = new DecimalFormat("0.00E0");
		
		String strVal=FormatUtils.asString(LC50_mg_L);
		
		String endpoint2="";
		
		if (endpoint.equals(TESTConstants.ChoiceFHM_LC50)) {
			endpoint2="96 hr Fathead minnow LC50";
		} else if (endpoint.equals(TESTConstants.ChoiceDM_LC50)) {
			endpoint2="48 hr Daphnia magna LC50";
		}
		
		if (LC50_mg_L < 1) {
			sr.score = "VH";
			sr.rationale = endpoint2+" (" + strVal + " mg/kg) < 1 mg/L";
		} else if (LC50_mg_L <= 10) {
			sr.score = "H";
			sr.rationale = "1 mg/L <= "+endpoint2+" (" + strVal + " mg/L) <= 10 mg/L";
		} else if (LC50_mg_L <= 100) {
			sr.score = "M";
			sr.rationale = "10 mg/L <= "+endpoint2+" (" + strVal + " mg/L) <= 100 mg/L";
		} else {//=100
			sr.score = "L";
			sr.rationale = endpoint2 +" (" + strVal + " mg/kg) > 100 mg/L";
		}
		

	}

	Hashtable<String, TESTRecord> getTESTRecords(Statement stat, String tableName, String keyField,
			Vector<String> vec) {

		Hashtable<String, TESTRecord> ht = new Hashtable<String, TESTRecord>();

		try {
			ResultSet rs = MySQL_DB.getRecords(stat, tableName, keyField, vec);

			ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {

				TESTRecord tr = new TESTRecord();

				for (int i = 1; i <= rsmd.getColumnCount(); i++) {

					String colName = rsmd.getColumnName(i);
					String colVal = rs.getString(i);

					if (colName.equals("CAS")) {
						tr.CAS = colVal;
					} else if (colName.equals("gsid")) {
						tr.gsid = colVal;
					} else if (colName.equals("DSSTOXSID")) {
						tr.DSSTOXSID = colVal;
					} else if (colName.equals("DSSTOXCID")) {
						tr.DSSTOXCID = colVal;
					} else if (colName.equals("ExpToxValue")) {
						tr.ExpToxValue = colVal;
					} else if (colName.equals("Hierarchical")) {
						tr.Hierarchical = colVal;
					} else if (colName.equals("SingleModel")) {
						tr.SingleModel = colVal;
					} else if (colName.equals("GroupContribution")) {
						tr.GroupContribution = colVal;
					} else if (colName.equals("NearestNeighbor")) {
						tr.NearestNeighbor = colVal;
					} else if (colName.equals("Consensus")) {
						tr.Consensus = colVal;
					}
				}

				ht.put(tr.CAS, tr);

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return ht;

	}

public static TESTRecord getTESTRecord(Statement stat,String tableName,String keyField,String keyValue) {
		long t1=System.currentTimeMillis();
		
		try {
			ResultSet rs = MySQL_DB.getRecords(stat,tableName, keyField, keyValue);

			ResultSetMetaData rsmd = rs.getMetaData();

			TESTRecord tr=new TESTRecord();
			
			if (!rs.next()) return null;
			
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				
				String colName=rsmd.getColumnName(i);
				
//				System.out.println(i+"\t"+colName);
				
				String colVal=rs.getString(i);
									
				if (colName.equals("CAS")) {
					tr.CAS=colVal;
				} else if (colName.equals("gsid")) {
					tr.gsid=colVal;
				} else if (colName.equals("DSSTOXSID")) {
					tr.DSSTOXSID=colVal;
				} else if (colName.equals("DSSTOXCID")) {
					tr.DSSTOXCID=colVal;
				} else if (colName.equals("MolecularWeight")) {
					tr.MolecularWeight=colVal;
				} else if (colName.equals("InChiKey")) {
					tr.InChiKey=colVal;
				} else if (colName.equals("SMILES")) {
					tr.SMILES=colVal;
				} else if (colName.equals("ExpToxCAS")) {//exp record might have come from structure match so need to specify what CAS we found
					tr.ExpToxCAS=colVal;
				} else if (colName.equals("ExpToxValue")) {
					tr.ExpToxValue=colVal;
				} else if (colName.equals("Hierarchical")) {
					tr.Hierarchical=colVal;
				} else if (colName.equals("SingleModel")) {
					tr.SingleModel=colVal;
				} else if (colName.equals("GroupContribution")) {
					tr.GroupContribution=colVal;
				} else if (colName.equals("NearestNeighbor")) {
					tr.NearestNeighbor=colVal;
				} else if (colName.equals("Consensus")) {
					tr.Consensus=colVal;
				} else if (colName.equals("error")) {
					tr.error=colVal;					
				}
			}
			
			long t2=System.currentTimeMillis();
			
//			System.out.println(tableName+"\t"+(t2-t1)/1000.0);
			
			return tr;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}


//	private void old_search_multiple_records_at_once() {
//		
		// long t1=System.currentTimeMillis();
				//
				// Hashtable<String,TESTRecord>htAcute_Mammalian_Toxicity=this.getTESTRecords(stat,TESTConstants.abbrevChoiceRat_LD50,
				// "CAS", vec);
				// Hashtable<String,TESTRecord>htAcute_Aquatic_Toxicity=this.getTESTRecords(stat,TESTConstants.abbrevChoiceFHM_LC50,
				// "CAS", vec);
				//
				// for (int i=0;i<chemicals.size();i++) {
				// Chemical chemicali=chemicals.get(i);
				// String CAS=chemicali.CAS;
				//
				// if (htAcute_Mammalian_Toxicity.get(CAS)!=null) {
				// TESTRecord tr=htAcute_Mammalian_Toxicity.get(CAS);
				// this.createAcute_Mammalian_ToxicityRecord(chemicali,tr);
				// }
				//
				// if (htAcute_Aquatic_Toxicity.get(CAS)!=null) {
				// TESTRecord tr=htAcute_Aquatic_Toxicity.get(CAS);
				// this.createAcute_Aquatic_ToxicityRecord(chemicali,tr);
				// }
				//
				// }
				//
				// long t2=System.currentTimeMillis();
				// System.out.println((t2-t1));
//	}



		
	private void createRecords_From_TEST(Chemical chemical,List<TESTPredictedValue>vecTPV) {

		long t1 = System.currentTimeMillis();
		
		String CAS = chemical.CAS;
								
		for (TESTPredictedValue tpv:vecTPV) {
			
//			System.out.println(CAS+"\t"+tpv.endpoint+"\t"+tpv.method);
			
			if (!tpv.method.equals(TESTConstants.ChoiceConsensus)) continue;
						
			if (tpv.endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
				Score score=chemical.scoreAcute_Mammalian_ToxicityOral;
				createRecordContinuousLogMolar(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceFHM_LC50)) {
				Score score=chemical.scoreAcute_Aquatic_Toxicity;
				createRecordContinuousLogMolar(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceDM_LC50)) {
				Score score=chemical.scoreAcute_Aquatic_Toxicity;
				createRecordContinuousLogMolar(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceReproTox)) {
				Score score=chemical.scoreDevelopmental;
				createRecordBinary(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceMutagenicity)) {
				Score score=chemical.scoreGenotoxicity_Mutagenicity;
				createRecordBinary(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceBCF)) {
				Score score=chemical.scoreBioaccumulation;
				createRecordContinuousLogMolar(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceWaterSolubility)) {
				Score score=chemical.scoreWaterSolubility;
				createRecordContinuousLogMolar(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceEstrogenReceptor)) {
				Score score=chemical.scoreEndocrine_Disruption;
				createRecordBinary(chemical, tpv, score);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
				Score score=chemical.scoreEndocrine_Disruption;
				createRecordContinuousLogMolar(chemical, tpv, score);
			}
			
		}

//		System.out.println(chemical.toJSONString());

		
//		TESTRecord trER = getTESTRecord(stat, TESTConstants.abbrevChoiceER_Binary, "CAS", CAS);
//		if (trER != null)
//			createEndocrine_Disruption(chemical, trER);
//		
//		TESTRecord trER_RBA = getTESTRecord(stat, TESTConstants.abbrevChoiceER_LogRBA, "CAS", CAS);
//		if (trER_RBA != null)
//			createEndocrine_Disruption_RBA(chemical, trER_RBA);


		long t2 = System.currentTimeMillis();
//		System.out.println((t2 - t1));


	}

	


	private void setEstrogenReceptorRBAScore(double val,ScoreRecord sr) {
		
//		System.out.println("er val="+val);
		
		if (val<-3) {///TODO we need a good cutoff for this- this is 3 orders of magnitude
			sr.score="L";
			sr.rationale="Does NOT bind strongly to the estrogen receptor (log10 RBA = "+FormatUtils.toD2(val)+")";
		} else {
			sr.score="H";
			sr.rationale="Does bind strongly to the estrogen receptor (log10 RBA = "+FormatUtils.toD2(val)+")";
		}
	}


	

	private void setBioconcentrationScore(double logBCF, ScoreRecord sr) {
		
		DecimalFormat df = new DecimalFormat("0.00");
		
		if (logBCF>3.7)  {// >3.7
			sr.score = "VH";
			sr.rationale = "logBCF (" + df.format(logBCF) + ") > 3.7";
		} else if (logBCF>=3) {
			sr.score = "H";
			sr.rationale = "3 <= logBCF (" + df.format(logBCF) + ") <= 3.7";
		}else if (logBCF>=2) {
			sr.score = "M";
			sr.rationale = "2 <= logBCF (" + df.format(logBCF) + ") < 3";
		} else {
			sr.score = "L";
			sr.rationale = "logBCF (" + df.format(logBCF) + ") < 2";
		}
		
	}
	
	

	private void createRecordContinuousLogMolar(Chemical chemical, TESTPredictedValue tpv,Score score) {
		
//		System.out.println(tpv.endpoint+"\t"+tpv.expValMass+"\t"+tpv.expValLogMolar+"\t"+tpv.predValLogMolar);
		
		String units=TESTConstants.getMassUnits(tpv.endpoint);
		
//		System.out.println(chemical.name+"\t"+chemical.CAS+"\t"+tpv.endpoint);
		
		
		if (!tpv.expValMass.equals(Double.NaN)) {
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.source = ScoreRecord.sourceTEST_Experimental;
			sr.name=chemical.name;

//			sr.classification = "";
			sr.hazardStatement = "";
			sr.route = "";
			
			sr.valueMass=tpv.expValMass;
			sr.valueMassUnits=units;
			
			// Set score and rationale:
			
			if (tpv.endpoint.equals(TESTConstants.ChoiceFHM_LC50) || tpv.endpoint.equals(TESTConstants.ChoiceDM_LC50)) {
				setAcuteAquaticToxicityScore(tpv.expValMass, sr,tpv.endpoint);	
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
				setAcuteMammalianToxicityScore(tpv.expValMass, sr);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceBCF)) {			
				setBioconcentrationScore(tpv.expValLogMolar, sr);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
				setEstrogenReceptorRBAScore(tpv.expValLogMolar, sr);
			}
			//Note: dont need to add score for water solubility- just need to store mass unit value

			score.records.add(sr);

		}
		

		if (!tpv.predValMass.equals(Double.NaN)) {
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.name=chemical.name;
			sr.source = ScoreRecord.sourceTEST_Predicted;
			
//			sr.classification = "";
			sr.hazardStatement = "";
			sr.route = "";
			
			sr.valueMass=tpv.predValMass;
			sr.valueMassUnits=units;
			sr.listType=ScoreRecord.typePredicted;
			
			long t2=System.currentTimeMillis();
//			System.out.println(chemical.CAS+"\t"+expValMass+"\t"+(t2-t1));
						
			if (tpv.endpoint.equals(TESTConstants.ChoiceFHM_LC50) || tpv.endpoint.equals(TESTConstants.ChoiceDM_LC50)) {
				setAcuteAquaticToxicityScore(tpv.predValMass, sr,tpv.endpoint);	
			}  else if (tpv.endpoint.equals(TESTConstants.ChoiceRat_LD50)) {
				setAcuteMammalianToxicityScore(tpv.predValMass, sr);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceBCF)) {			
				setBioconcentrationScore(tpv.predValLogMolar, sr);
			} else if (tpv.endpoint.equals(TESTConstants.ChoiceEstrogenReceptorRelativeBindingAffinity)) {
				setEstrogenReceptorRBAScore(tpv.predValLogMolar, sr);
			}

			//Note: dont need to add score for water solubility- just need to store mass unit value
			
			score.records.add(sr);

		} else {
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.name=chemical.name;
			sr.source = ScoreRecord.sourceTEST_Predicted;
//			sr.classification = "";
			sr.hazardStatement = "";
			sr.route = "";
			sr.score = "N/A";
			sr.listType=ScoreRecord.typePredicted;
			sr.rationale = tpv.endpoint+" could not be predicted using T.E.S.T.";
			score.records.add(sr);
		}
		
	}
	
	
	private void createRecordBinary(Chemical chemical, TESTPredictedValue tpv, Score score) {
//		System.out.println(tpv.endpoint+"\t"+tpv.expValLogMolar+"\t"+tpv.predValLogMolar);
		
		if (!tpv.expValLogMolar.equals(Double.NaN)) {

			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.source = ScoreRecord.sourceTEST_Experimental;
			sr.name=chemical.name;
			
//			sr.classification = "";
			sr.hazardStatement = "";
			sr.route = "";
			
			sr.valueActive=tpv.expActive;

			if (!tpv.expActive) {
				sr.score="L";
				sr.rationale=tpv.endpoint+" Negative";
				sr.rationale="Negative for "+tpv.endpoint;
			} else {
				sr.score="H";
				sr.rationale="Positive for "+tpv.endpoint;
			}

			score.records.add(sr);

		}
		

		if (!tpv.predValLogMolar.equals(Double.NaN)) {
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.source = ScoreRecord.sourceTEST_Predicted;
			sr.name=chemical.name;
			
//			sr.classification = "";
			sr.hazardStatement = "";
			sr.route = "";

			sr.valueActive=tpv.predActive;
			
			if (!tpv.predActive) {
				sr.score="L";
				sr.rationale="Negative for "+tpv.endpoint;
			} else {
				sr.score="H";
				sr.rationale="Positive for "+tpv.endpoint;
			}
			
//			System.out.println(sr.toJSON());
			sr.listType=ScoreRecord.typePredicted;
			score.records.add(sr);

		} else {
			ScoreRecord sr = new ScoreRecord(score.hazard_name,chemical.CAS,chemical.name);
			sr.name=chemical.name;
			sr.source = ScoreRecord.sourceTEST_Predicted;
//			sr.classification = "";
			sr.hazardStatement = "";
			sr.route = "";
			sr.score = "N/A";
			sr.rationale = tpv.endpoint+" could not be predicted using T.E.S.T.";
			sr.listType=ScoreRecord.typePredicted;
			score.records.add(sr);
		}
	}

	

	static TESTPredictedValue runTESTCalculation (AtomContainer ac,String endpoint) {
		DescriptorData dd=runTESTDescriptors(ac);
		
//		System.out.println("number of aromatic bonds="+dd.nAB);
		
		String method=TESTConstants.ChoiceConsensus; //method for TEST calc
		TESTPredictedValue v = runTESTCalculation(dd, method, endpoint);
		return v;
	}
	
	
	static AtomContainer parseSmiles(String smiles,String CAS) {
		
		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			AtomContainer molecule = (AtomContainer)sp.parseSmiles(smiles);
			
			molecule.setProperty("CAS", CAS);
			
			DescriptorFactory df=new DescriptorFactory(false);
			
			df.Normalize(molecule);
			
			return molecule;
			
		} catch (org.openscience.cdk.exception.InvalidSmilesException e) {
			return null;
		}

		
	}

	
	void combineSDFFiles() {

		String folderPath="AA Dashboard\\Data\\Chemidplus\\StructureFilesforDensity";

		File Folder=new File(folderPath);

		File [] files=Folder.listFiles();

		try {

			FileWriter fw=new FileWriter("AA Dashboard\\Data\\Chemidplus\\calc density from scifinder structures.sdf");

			for (int i=0;i<files.length;i++) {
				File filei=files[i];

				if (filei.getName().indexOf(".sdf")==-1) continue;

				ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(filei.getAbsolutePath());

				for (int j=0;j<lines.size();j++) {
					fw.write(lines.get(j)+"\r\n");
				}

			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	

	void calculateDensities() {
		
		String sdfFilePath="AA Dashboard\\Data\\Chemidplus\\calc density from scifinder structures.sdf";
		ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();
		DescriptorFactory df=new DescriptorFactory(false);
		DecimalFormat df3=new DecimalFormat("0.000");
		
		try {

			
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(sdfFilePath),DefaultChemObjectBuilder.getInstance());
			int counter = -1;
			
			FileWriter fw=new FileWriter("AA Dashboard\\Data\\Chemidplus\\density from scifinder structures.txt");
			
			while (mr.hasNext()) {
				
				counter++;
				
				
				AtomContainer m=null;
				
				try {
					m = (AtomContainer)mr.next();
				} catch (Exception e) {
					break;
				}
				
				if (m==null || m.getAtomCount()==0) break;
				
//				System.out.println(counter+"\t"+m.getAtomCount());

				String CASfield=MolFileUtilities.getCASField(m);
//				System.out.println(CASfield);
				
				String CAS=null;
				
				if (!CASfield.equals("")) {
					CAS=(String)m.getProperty(CASfield);
					if (CAS != null) {
						CAS=CAS.trim();
						CAS=CAS.replace("/", "_");
						m.setProperty("CAS", CAS);
					}
					CAS=CAS.replace("CAS-", "");
					m.setProperty("CAS", CAS);
				}
				
				m.setProperty("Error", "");
				 
				if (mfu.HaveBadElement(m)) {
					m.setProperty("Error",
							"Molecule contains unsupported element");
				} else if (m.getAtomCount() == 1) {
					m.setProperty("Error", "Only one nonhydrogen atom");
				} else if (m.getAtomCount() == 0) {
					m.setProperty("Error", "Number of atoms equals zero");
				}

				 AtomContainerSet  moleculeSet2 = (AtomContainerSet)ConnectivityChecker.partitionIntoMolecules(m);
				 if (moleculeSet2.getAtomContainerCount() > 1) {
//					m.setProperty("Error","Multiple molecules, largest fragment retained");
					m.setProperty("Error","Multiple molecules");
				}
								

				 if (!m.getProperty("Error").equals("")) {
					 System.out.println(CAS+"\t"+m.getProperty("Error"));
					 continue;
				 }
				 
				df.Normalize(m);

				TESTPredictedValue tpv=runTESTCalculation(m,TESTConstants.ChoiceDensity);
				double densityFinal=-9999;
				String densityFinalSource;
				
				if (tpv.expValMass.doubleValue()!=-9999)  {
					densityFinalSource="Exp";
					densityFinal=tpv.expValMass.doubleValue();
				} else if (tpv.predValMass.doubleValue()!=-9999){
					densityFinal=tpv.predValMass.doubleValue();
					densityFinalSource="TEST_Consensus";
				} else {
					System.out.println(CAS+"\tcant predict density");
					continue;
				}

				fw.write(CAS+"\t"+df3.format(densityFinal)+"\t"+densityFinalSource+"\r\n");		
				fw.flush();
			}// end while true;

			mr.close();
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	void getDensities() {

		ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray("AA Dashboard/Data/Chemidplus/need density.txt");

		try {
			
			FileWriter fw=new FileWriter("AA Dashboard/Data/Chemidplus/density from dashboard calcs.txt");
			FileWriter fw2=new FileWriter("AA Dashboard/Data/Chemidplus/need density not in dashboard.txt");


			Connection conn=MySQL_DB.getConnection("databases/test_results.db");
			Statement stat=conn.createStatement();

			
			for (int i=0;i<lines.size();i++) {

				String CAS=lines.get(i);

				//			System.out.println(CAS);

				
				

				TESTRecord trDensity = getTESTRecord(stat, TESTConstants.abbrevChoiceDensity, "CAS", CAS);

				double densityFinal=-9999;
				String densityFinalSource="";

				if (trDensity!=null) {

					if (!trDensity.ExpToxValue.equals("N/A")) {
						densityFinal=Double.parseDouble(trDensity.ExpToxValue);
						densityFinalSource="Exp";
					} else if (!trDensity.Consensus.equals("N/A")) {
						densityFinal=Double.parseDouble(trDensity.Consensus);
						densityFinalSource="TEST_Consensus";
					}
				}

				if (densityFinal!=-9999) {
					fw.write(CAS+"\t"+densityFinal+"\t"+densityFinalSource+"\r\n");
				} else {

					ChemistryDashboardRecord2 c = ChemistryDashboardRecord2.lookupDashboardRecordbyCAS(CAS);
					if (c==null) {
						fw2.write(CAS+"\tnot in dashboard\r\n");
						continue;
					}
					if (c.smiles.equals("")) {
						fw2.write(CAS+"\tsmiles missing\r\n");
						continue;
					}

					//				System.out.println(CAS+"\t"+c.smiles);
					AtomContainer ac2=parseSmiles(c.smiles,CAS);
					//				System.out.println(c.smiles);
					TESTPredictedValue tpvDensity2=runTESTCalculation(ac2,TESTConstants.ChoiceDensity);
					if (tpvDensity2.predValMass==-9999) {
						fw2.write(CAS+"\tcant predict\r\n");
					}
				}

				//			System.out.println(trDensity.ExpToxValue+"\t"+trDensity.Consensus+"\t"+tpvDensity2.predValMass);
			}
			
			fw.flush();
			fw.close();
			
			fw2.flush();
			fw2.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
	
	public static void main(String[] args) {

		String CAS="71-43-2";
//		String CAS="110-82-7";
		
//		ChemistryDashboardRecord2 c = ChemistryDashboardRecord2.lookupDashboardRecordbyCAS(CAS);

//		System.out.println(c.inchi);
//		AtomContainer ac=ChemistryDashboardRecord2.getAtomContainer(c.inchi,CAS);
//		System.out.println(ac.getAtomCount()+"\t"+ac.getBondCount());
//		System.out.println(ac.getBond(0).isAromatic());
//		TESTPredictedValue tpvDensity=runTESTCalculation(ac,TESTConstants.ChoiceDensity);
//		System.out.println(trDensity.ExpToxValue+"\t"+trDensity.Consensus+"\t"+tpvDensity.predValMass);
		
//		AtomContainer ac2=parseSmiles(c.smiles,CAS);
//		System.out.println(c.smiles);
//		TESTRecord trDensity = getTESTRecord(stat, TESTConstants.abbrevChoiceDensity, "CAS", CAS);
//		TESTPredictedValue tpvDensity2=runTESTCalculation(ac2,TESTConstants.ChoiceDensity);
//		System.out.println(trDensity.ExpToxValue+"\t"+trDensity.Consensus+"\t"+tpvDensity2.predValMass);
		
		GenerateRecordsFromTEST g=new GenerateRecordsFromTEST();
//		g.getDensities();
//		g.combineSDFFiles();
//		g.calculateDensities();
		
		Chemical chemical=new Chemical();
//		chemical.CAS="3194-55-6";
//		chemical.CAS="208-96-8";
		chemical.CAS="1163-19-5";
		
		try {
			
			Connection conn=MySQL_DB.getConnection("databases/test_results.db");
			Statement stat=conn.createStatement();
			
//			Old way based on TESTRecord
//			g.createRecord_From_TEST_Prediction_Database(chemical,stat);
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println(chemical.toJSONString());
	}


}
