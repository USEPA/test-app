package AADashboard.Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//import AADashboard.Parse.*;
//import AADashboard.ParseNew.ParseOSPAR;
import AADashboard.Utilities.FileUtilities;
import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.IndigoUtilities;
import ToxPredictor.misc.MolFileUtilities;
import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;




/**
 * This class generates AA reports from a list of chemicals
 * 
 * To add new sources:
 * 
 * 1. Add to jsonFolders string array 2. Add to recreateJSONFilesAllSources 3.
 * Add source string to SourceRecord 4. Add weight in SourceRecord (getWeight)
 * 
 * @author Todd Martin
 *
 */
public class AADashboard {

	WebTEST wt;
	GenerateRecordsFromTEST generateRecordsFromTEST = new GenerateRecordsFromTEST();
	
	private static final Logger logger = LogManager.getLogger(AADashboard.class);
	
	boolean debug=true;

	public static final String dataFolder = "C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\AA Dashboard\\Data";
	public static final String dictionaryFolder = dataFolder+File.separator+"dictionary";


	//If you use CAS primary key (no duplicate CAS) and store all records for each chemical in one field separated by end of line characters
	//you get 100x faster search:
	//    public static final String DB_Path_AA_Dashboard_Records = "databases/AA dashboard_w_primary_key.db";

	public static final String DB_Path_AA_Dashboard_Records="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\databases\\RevisedHazardDB\\HazardRecordsComplete.db";	
//	public static final String DB_Path_AA_Dashboard_Records = "databases/AA dashboard.db";//fast if you add index for CAS: "CREATE INDEX idx_CAS ON "+tableName+" (CAS)"

	public static Statement statAA_Dashboard_Records = MySQL_DB.getStatement(DB_Path_AA_Dashboard_Records);


//	private static final String DB_Path_OPERA_Records = "databases/OPERA ER AR hazard records.db";

//	public static Statement statOPERA_Records = MySQL_DB.getStatement(DB_Path_OPERA_Records);
	
	//	public static final String DB_Path_TEST_Results="databases/test_results.db";//Link stored in WebTEST2
	//Note: 700K NCCT chemical info database located at "WebTEST.DB_Path_NCCT_ID_Records"//Link stored in WebTEST2

	final static String finalScoreSchemeTrumping ="Trumping";
	final static String finalScoreSchemeWeightedAverage ="Weighted Average";

	public static String finalScoreScheme=finalScoreSchemeTrumping; 
	//	public static String finalScoreScheme=finalScoreSchemeWeightedAverage; 

	public static boolean filterRecords=true;

	public static ArrayList<String> sources = new ArrayList<String>();

	public static boolean weightedAverageRoundUp=true;

	public AADashboard() {
		if (sources.size()==0) addSources();
	}

	private void addSources() {
		sources.add(ScoreRecord.sourceAustralia);
		sources.add(ScoreRecord.sourceCanada);
		sources.add(ScoreRecord.sourceChemIDplus);
		sources.add(ScoreRecord.sourceDenmark);
		sources.add(ScoreRecord.sourceDSL);
		sources.add(ScoreRecord.sourceECHA_CLP);
		sources.add(ScoreRecord.sourceEPAMidAtlanticHumanHealth);
		sources.add(ScoreRecord.sourceGermany);
		sources.add(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Carcinogenicity);
		sources.add(ScoreRecord.sourceHealth_Canada_Priority_Substance_Lists_Reproductive);
		sources.add(ScoreRecord.sourceIARC);
		sources.add(ScoreRecord.sourceIRIS);
		sources.add(ScoreRecord.sourceJapan);
		//		sources.add(ScoreRecord.sourceKorea);//omit Korea for now since cant update it since they locked down the webpages
		sources.add(ScoreRecord.sourceMalaysia);
		sources.add(ScoreRecord.sourceNewZealand);
		sources.add(ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens);
		// sources.add(ParseOSPAR.sourceName);
		sources.add(ScoreRecord.sourceProp65);
		sources.add(ScoreRecord.sourceReachVeryHighConcernList);
		sources.add(ScoreRecord.sourceReportOnCarcinogens);
		sources.add(ScoreRecord.sourceSIN);
		sources.add(ScoreRecord.sourceTEDX);
		sources.add(ScoreRecord.sourceTSCA_Work_Plan);
		sources.add(ScoreRecord.sourceUMD);

	}

	// class DataRow {
	// public String molfile;
	// }



	/**
	 *
	 * get reader for json file within jar
	 * 
	 * @param jsonFilePath
	 * @return
	 */
	public BufferedReader getReaderFromJar(String jsonFilePath) {
		try {
			java.io.InputStream ins = this.getClass().getClassLoader().getResourceAsStream(jsonFilePath);
			if (ins == null)
				return null;

			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);
			return br;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}



	private void createRecordsJarFile() {

		try {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			JarOutputStream target = new JarOutputStream(new FileOutputStream("jar/ScoreRecords.jar"), manifest);

			System.out.println("creating jar file...");
			for (String source:sources) {
				System.out.println(source);
				FileUtilities.addToJarFile(new File(this.dataFolder+File.separator+source), target);
			}

			target.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void addRecords(Chemical chemical, String source, boolean useJarFiles) {
		String CAS = chemical.CAS;
		String jsonFilePath = this.dataFolder+File.separator+source+File.separator+"json files"+File.separator+CAS +".json";

		//TODO- should we in the future have file names with multiple cas numbers in the name and look for the cas number by looping through the files in folder? kinda slow!

		// System.out.println(jsonFolder);

		//		System.out.println(useJarFiles);


		if (useJarFiles) {

			//			System.out.println("here1");

			BufferedReader br = this.getReaderFromJar(jsonFilePath);
			if (br != null) {
				Chemical chemical2 = Chemical.loadFromJSON(br);
				//				 System.out.println(chemical2.CAS);

				if (chemical2.scores.size() < chemical.scores.size()) {
					System.out.println(source + "\tscore array size mismatch!");
					return;
				}
				//				System.out.println("here");
				chemical.addRecords(chemical2);
			} else {
				System.out.println(jsonFilePath+"\tdoesnt exist");
			}
		} else {
			File jsonFile = new File(jsonFilePath);
			if (jsonFile == null || !jsonFile.exists())
				return;
			Chemical chemical2 = Chemical.loadFromJSON(jsonFile);

			if (chemical2.scores.size() < chemical.scores.size()) {
				System.out.println(source + "\tscore array size mismatch!\t"+chemical.CAS);
				return;
			}

			chemical.addRecords(chemical2);
		}
	}







	//Sort by authority (descending), score (descending), and source name (ascending)
	void sortRecordsAuthorityScoreSourceName(ArrayList<ScoreRecord> records) {

		Collections.sort(records, new Comparator<ScoreRecord>() {

			public int compare(ScoreRecord sr1, ScoreRecord sr2) {

				int Authority1 = sr1.getAuthorityWeight();
				int Authority2 = sr2.getAuthorityWeight();
				int sComp = Authority1-Authority2;

				if (sComp != 0)
					return -sComp;//sort in descending order in terms of authority weight
				else {
					int score1 = sr1.scoreToInt();
					int score2 = sr2.scoreToInt();
					int sComp2 = score1-score2;

					if (sComp2!=0) {
						return -sComp2;//sort in ascending order in terms of source name	
					} else {
						String source1=sr1.source;
						String source2=sr2.source;
						int sComp3=source1.compareTo(source2);
						return sComp3;
					}
				}

			}
		});

	}

	//Sort by authority (descending), score (descending), and source name (ascending)
	void sortRecordsSourceName(ArrayList<ScoreRecord> records) {

		Collections.sort(records, new Comparator<ScoreRecord>() {

			public int compare(ScoreRecord sr1, ScoreRecord sr2) {
				String source1=sr1.source;
				String source2=sr2.source;
				int sComp3=source1.compareTo(source2);
				return sComp3;
			}
		});

	}


	// void testFlameRetardantsNITE() {
	//
	// Chemicals chemicals=this.createFlameRetardants();
	//
	// for (int i=0;i<chemicals.size();i++) {
	// Chemical chemicali=chemicals.get(i);
	// this.generateRecordsFromNITE.createRecords(chemicali);
	// }
	//
	// //Output to file so can look at it:
	// chemicals.toJSON_File("todd/AA dashboard/Records from NITE.json");
	// //AA dashboard json testing file.json
	// }

	/**
	 * Assign final score based on weighted average from multiple sources
	 * 
	 * @param chemical
	 */
	private void AssignFinalScoresWeightedAverage(Chemical chemical) {



		for (int i = 0; i < chemical.scores.size(); i++) {//loop over tox endpoints

			Score score = chemical.scores.get(i);

			if (score.records.size() == 0) {
				score.final_score = ScoreRecord.scoreNA;
				continue;
			}

			sortRecordsSourceName(score.records);

			// First create ArrayList of scores with final scores:
			ArrayList<ScoreRecord> recordsWithScore = new ArrayList<ScoreRecord>();

			for (int j = 0; j < score.records.size(); j++) {
				ScoreRecord scoreRecord = score.records.get(j);

				if (!scoreRecord.score.equals("N/A")) {
					// System.out.println(chemical.CAS+"\t"+scoreRecord.score);
					recordsWithScore.add(scoreRecord);
				}
			}

			if (recordsWithScore.size() == 0) {
				score.final_score = ScoreRecord.scoreNA;
				// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\tNA");
				continue;
			}

			float totalWeights = 0;
			float weightedScore = 0;

			for (int j = 0; j < recordsWithScore.size(); j++) {
				ScoreRecord scoreRecord = recordsWithScore.get(j);

				int iScore = scoreRecord.scoreToInt();

				float weight = scoreRecord.getWeight();

				weightedScore += weight * iScore;
				totalWeights += weight;

				// if (chemical.CAS.equals("115-86-6"))
				// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+scoreRecord.source+"\t"+weight);

			}

			weightedScore /= totalWeights;

			int iScore=-1;

			if (weightedAverageRoundUp) {
				iScore = (int) Math.ceil(weightedScore);// round up	
			} else {
				iScore = (int) Math.round(weightedScore);// round to nearest integer	
			}


			// Convert integer back to L,M,H,VH score:
			score.final_score = this.getScoreFromTranslatedData(iScore);

			// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+weightedScore+"\t"+totalWeights+"\t"+iScore+"\t"+score.final_score);

			// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+weightedScore+"\t"+totalWeights+"\t"+final_fscore+"\t"+score.final_score);

		}

	}


	/**
	 * Assign final score based on trumping rules
	 * 
	 * @param chemical
	 */
	private void AssignFinalScoresGreenScreenListTranslator(Chemical chemical) {

		for (int i = 0; i < chemical.scores.size(); i++) {//loop over tox endpoints

			Score score = chemical.scores.get(i);

			if (score.records.size() == 0) {
				score.final_score = ScoreRecord.scoreNA;
				continue;
			}

			// First create ArrayList of scores with final scores:
			ArrayList<ScoreRecord> recordsWithScore = new ArrayList<ScoreRecord>();

			for (int j = 0; j < score.records.size(); j++) {
				ScoreRecord scoreRecord = score.records.get(j);

				if (!scoreRecord.score.equals(ScoreRecord.scoreNA)) {
					// System.out.println(chemical.CAS+"\t"+scoreRecord.score);
					recordsWithScore.add(scoreRecord);
				}
			}

			if (recordsWithScore.size() == 0) {
				score.final_score = ScoreRecord.scoreNA;
				// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\tNA");
				continue;
			}


			int iScoreMax=-1;
			String typeMax="";

			for (int j = 0; j < recordsWithScore.size(); j++) {
				ScoreRecord scoreRecord = recordsWithScore.get(j);
				int iScore = scoreRecord.scoreToInt();
				String type=scoreRecord.getListType();

				if (type.equals("")) {
					System.out.println("missing type for "+scoreRecord.source);
					continue;
				}

				if (j==0) {
					iScoreMax=iScore;
					typeMax=type;
					continue;
				}

				if (type.equals(ScoreRecord.typeAuthoritative)) {
					if (typeMax.equals(ScoreRecord.typeAuthoritative)) {
						if (iScore>=iScoreMax) {//Use more conservative score
							iScoreMax=iScore;
						} else {
							//do nothing
						}
					} else if (typeMax.equals(ScoreRecord.typeScreening)) {
						iScoreMax=iScore;
						typeMax=type;
					}
				} else if (type.equals(ScoreRecord.typeScreening)) {
					if (typeMax.equals(ScoreRecord.typeAuthoritative)) {
						//do nothing
					} else if (typeMax.equals(ScoreRecord.typeScreening)) {
						if (iScore>=iScoreMax) {//Use more conservative score
							iScoreMax=iScore;
						} else {
							//do nothing
						}
					}
				}

				if (chemical.CAS.equals("60-51-5")) {
					if (score.hazard_name.equals("Acute Mammalian Toxicity Oral")) {
						System.out.println(type+"\t"+typeMax+"\t"+iScore+"\t"+iScoreMax);
					}
				}


				/*//***************************************************************************************************
					//Detailed system with A and B subtypes
//					if (type1.equals(ScoreRecord.typeAuthoritativeA) || type1.equals(ScoreRecord.typeAuthoritativeB)) {
//						if (type2.equals(ScoreRecord.typeAuthoritativeA) || type2.equals(ScoreRecord.typeAuthoritativeB)) {
//							if (iScore1>=iScore2) {//Use more conservative score
//								finalIntegerScore=iScore1;
//							} else {
//								finalIntegerScore=iScore2;
//							}
//						} else if (type2.equals(ScoreRecord.typeScreeningA) || type2.equals(ScoreRecord.typeScreeningB)) {
//							finalIntegerScore=iScore1;
//						}
//					} else if (type1.equals(ScoreRecord.typeScreeningA) || type1.equals(ScoreRecord.typeScreeningB)) {
//						if (type2.equals(ScoreRecord.typeAuthoritativeA) || type2.equals(ScoreRecord.typeAuthoritativeB)) {
//							finalIntegerScore=iScore2;
//						} else if (type2.equals(ScoreRecord.typeScreeningA) || type2.equals(ScoreRecord.typeScreeningB)) {
//							if (iScore1>=iScore2) {//Use more conservative score
//								finalIntegerScore=iScore1;
//							} else {
//								finalIntegerScore=iScore2;
//							}
//						}
//					}

					//***************************************************************************************************
					//Simplified version with no subtypes because the trumping system is unaffected by subtypes anyway:
				 */					

				// if (chemical.CAS.equals("115-86-6"))
				// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+scoreRecord.source+"\t"+weight);

			}// end of the loop over the ScoreRecords with actual scores (i.e. != NA)


			// Convert integer back to L,M,H,VH score:
			score.final_score = this.getScoreFromTranslatedData(iScoreMax);

			// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+weightedScore+"\t"+totalWeights+"\t"+iScore+"\t"+score.final_score);

			// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+weightedScore+"\t"+totalWeights+"\t"+final_fscore+"\t"+score.final_score);

		}

	}

	void assignFinalScoreAndRemoveFinalScoreRecordsMDHExposure(Score score) {
		for (int j = 0; j < score.records.size(); j++) {
			ScoreRecord sr=score.records.get(j);
			
			if (sr.note.equals("final score")) {
				if (sr.score!=null && !sr.score.isEmpty()) {
					score.final_score=sr.score;
				} else {
					score.final_score=sr.scoreNA;
				}
				score.records.remove(j--);
			}
		}
	}
	
	/**
	 * Assign final score based on trumping rules and use authority weights to make more general
	 * 
	 * @param chemical
	 */
	private void AssignFinalScoresGreenScreenListTranslator2(Chemical chemical) {

		for (int i = 0; i < chemical.scores.size(); i++) {//loop over tox endpoints

			Score score = chemical.scores.get(i);

			if (score.records.size() == 0) {
				score.final_score = ScoreRecord.scoreNA;
				continue;
			}

			this.sortRecordsAuthorityScoreSourceName(score.records);

			if (score.hazard_name.equals(Chemical.strExposureIndividual) ||
					score.hazard_name.equals(Chemical.strExposurePopulation)||
					score.hazard_name.equals(Chemical.strExposureChildOrConsumerProducts)) {
				assignFinalScoreAndRemoveFinalScoreRecordsMDHExposure(score);
				continue;
			}
			

			int iScoreMax=-1;
			int iAuthorityMax=-1;
			String sourceMax="";

			for (int j = 0; j < score.records.size(); j++) {
				ScoreRecord scoreRecord = score.records.get(j);

				if (scoreRecord.score.equals(ScoreRecord.scoreNA)) continue;

				int iScore = scoreRecord.scoreToInt();
				int iAuthority=scoreRecord.getAuthorityWeight();

				if (iAuthority==0) {
					System.out.println("missing type for "+scoreRecord.source);
					continue;
				}

				if (iScoreMax==-1) {
					iScoreMax=iScore;
					iAuthorityMax=iAuthority;
					sourceMax=scoreRecord.source;
					continue;
				}

				if (iAuthority>iAuthorityMax) {
					iScoreMax=iScore;
					iAuthorityMax=iAuthority;
					sourceMax=scoreRecord.source;

				} else if (iAuthority==iAuthorityMax) {
					if (iScore>iScoreMax) {//Use more conservative score
						iScoreMax=iScore;
						sourceMax=scoreRecord.source;
					} else {
						//do nothing with score
					}
				} else if (iAuthority<iAuthorityMax) {
					//do nothing with score
				}


			}// end of the loop over the ScoreRecords with actual scores (i.e. != NA)


			// Convert integer back to L,M,H,VH score:
			score.final_score = this.getScoreFromTranslatedData(iScoreMax);
			score.final_score_source=sourceMax;

			//					 System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+score.final_score);

			// System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+weightedScore+"\t"+totalWeights+"\t"+final_fscore+"\t"+score.final_score);

		}

	}

	public static String getScoreFromTranslatedData(int value) {

		// TODO- do we need to change this depending on which toxicity value it is? i.e.
		// do I trust wehage's translation patterns? i.e. to match DfE like I did in
		// ChemHAT.java?

		if (value == 5) {
			return "VH";
		} else if (value == 4) {
			return "H";
		} else if (value == 3) {
			return "M";
		} else if (value == 2) {
			return "L";
		} else {
			return "N/A";
		}
	}







	public static void deleteFiles(String folder) {

		File Folder = new File(folder);

		if (!Folder.exists())
			return;

		for (int j = 1; j <= 3; j++) {
			File[] files = Folder.listFiles();

			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
	}




	//	public void run(Chemicals chemicals,String outputFolder,String outputJsonFileName,String outputHTMLFileName,boolean useJarFiles) {
	//		
	//		String jsonFilePath=outputFolder+"/"+outputJsonFileName;
	//		
	//		// Create the records from all sources
	//		createRecords(chemicals, jsonFilePath, useJarFiles);
	//
	//		long t2 = System.currentTimeMillis();
	//
	//
	//		// Create web pages:
	//		TableGenerator tg = new TableGenerator();
	//		
	//		tg.generateComparisonTableFromJSONFile2(jsonFilePath, outputFolder, outputHTMLFileName);
	//
	//	}

	public void runUsingSQLiteDB(AtomContainerSet acs,String outputFolder,String outputJsonFileName,String outputHTMLFileName) {
		runUsingSQLiteDB(acs,outputFolder,outputJsonFileName,outputHTMLFileName,true,true,"name");
	}


	public void runUsingSQLiteDB(AtomContainerSet acs,String outputFolder,String outputJsonFileName,String outputHTMLFileName,boolean generateComparisonTable,boolean writeRecordPages,String nameField) {

		File of=new File(outputFolder);
		if(!of.exists()) of.mkdirs();


		Chemicals chemicals=new Chemicals();

		//		for (int i=0;i<1;i++) {
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			String CAS=ac.getProperty("CAS");
			String name=ac.getProperty(nameField);

			//			System.out.println("\n"+CAS+"\t"+name+"\t"+(i+1)+" of "+acs.getAtomContainerCount());

			Chemical chemical=this.runChemicalUsingSQLiteDB(CAS, name, ac);
			chemicals.add(chemical);
		}

		String jsonFilePath=outputFolder+"/"+outputJsonFileName;
		chemicals.writeToFile(jsonFilePath);//Necessary to write to json file? or just send chemicals class to html generator?

		// Create web pages:
		TableGeneratorHTML tg = new TableGeneratorHTML();

		//		tg.generateComparisonTableFromJSONFile2(jsonFilePath, outputFolder, outputHTMLFileName);
		if (generateComparisonTable) tg.generateComparisonTableFromJSONFile2(chemicals, outputFolder, outputHTMLFileName,"Hazard Record Pages",writeRecordPages);

	}

	public void runUsingSQLiteDB(AtomContainerSet acs,String outputFolder,String outputJsonFileName,String outputHTMLFileName,boolean generateComparisonTable,boolean writeRecordPages,String nameField,String recordPageFolderName) {

		File of=new File(outputFolder);
		if(!of.exists()) of.mkdirs();


		Chemicals chemicals=new Chemicals();

		//		for (int i=0;i<1;i++) {
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			String CAS=ac.getProperty("CAS");
			String name=ac.getProperty(nameField);

			//			System.out.println("\n"+CAS+"\t"+name+"\t"+(i+1)+" of "+acs.getAtomContainerCount());

			Chemical chemical=this.runChemicalUsingSQLiteDB(CAS, name, ac);
			chemicals.add(chemical);
		}

		String jsonFilePath=outputFolder+"/"+outputJsonFileName;
		chemicals.writeToFile(jsonFilePath);//Necessary to write to json file? or just send chemicals class to html generator?

		// Create web pages:
		TableGeneratorHTML tg = new TableGeneratorHTML();

		//		tg.generateComparisonTableFromJSONFile2(jsonFilePath, outputFolder, outputHTMLFileName);
		if (generateComparisonTable) tg.generateComparisonTableFromJSONFile2(chemicals, outputFolder, outputHTMLFileName,recordPageFolderName,writeRecordPages);

	}

	//	void mergeAADashboardResults() {
	//		String outputFolder="AADashboard calcs";
	//		
	////		String jsonFilePath=folder+"/sample.json";		
	////		Chemicals chemicals=Chemicals.loadFromJSON(jsonFilePath);
	////		System.out.println(chemicals.size());
	////		Chemicals chemicalsOverall=new Chemicals();
	////		chemicalsOverall.addAll(chemicals);
	//		
	//		
	//		Chemicals chemicalsOverall=new Chemicals();
	//		for (int i=1;i<=13;i++) {
	//			String jsonFilePath="AADashboard calcs\\NCCT structures\\NCCT structures "+i+".json";
	//			Chemicals chemicals=Chemicals.loadFromJSON(jsonFilePath);
	//			chemicalsOverall.addAll(chemicals);
	//			System.out.println(i+"\t"+chemicals.size());
	//		}
	//		
	//		for (int i=1;i<=12;i++) {
	//			
	//			String jsonFilePath="AADashboard calcs\\chemidplus structures\\chemidplus3d"+i+".json";
	//			File file=new File(jsonFilePath);
	//			if (!file.exists()) continue;
	//			
	//			Chemicals chemicals=Chemicals.loadFromJSON(jsonFilePath);
	//			chemicalsOverall.addAll(chemicals);
	//			System.out.println(i+"\t"+chemicals.size());
	//		}
	//
	//		
	//		chemicalsOverall.sortByCAS();
	//		
	////		for (Chemical chemical:chemicalsOverall) {
	////			System.out.println(chemical.CAS);
	////		}
	//		
	//		TableGenerator tg = new TableGenerator();
	//		
	////		chemicalsOverall.writeToFile(outputFolder+"/overallChemicals.json");
	////		chemicalsOverall.toFlatFile(outputFolder+"/overallChemicals.txt");
	//		tg.toFlatFileXLS(chemicalsOverall,outputFolder+"/overallChemicals.xlsx");
	//				
	//		
	//		String htmlFileName="NCCT structures.html";
	////		tg.generateComparisonTableFromJSONFile2(chemicalsOverall, folder, htmlFileName,null,false);
	//		tg.generateComparisonTableFromJSONFileAsExcel(chemicalsOverall, outputFolder, "overallChemicalsFinalScores.xlsx");
	//		
	//		
	//	}


	public Chemicals generateDashboard(IAtomContainerSet acs, String nameField) {
		Chemicals chemicals = new Chemicals();

		for (int i = 0; i < acs.getAtomContainerCount(); i++) {

			IAtomContainer m = (IAtomContainer) acs.getAtomContainer(i);

			// Ignore atom containers with errors  
			if (!StringUtils.isEmpty(m.getProperty("Error"))) {
				continue;
			}

			String CAS = m.getProperty("CAS");
			String name = m.getProperty(nameField);

			DSSToxRecord rec = null;

			if ( ResolverDb.isAvailable() ) {
				if (!StringUtils.isEmpty(CAS) && !CAS.matches("C\\d*_\\d{8,}")) {
					ArrayList<DSSToxRecord> recs = ResolverDb.lookupByCAS(CAS);
					if (recs.size() > 0)
						rec = recs.get(0);
				} else {
					Inchi inchi = CDKUtilities.generateInChiKey(m);
					if (!"N/A".equals(inchi.inchiKey) & !StringUtils.isEmpty(inchi.inchiKey)) {
						ArrayList<DSSToxRecord> recs = ResolverDb.lookupByInChIKey(inchi.inchiKey);
						if (recs.size() > 0)
							rec = recs.get(0);
						else {
							// Fallback to Indigo 
							inchi = IndigoUtilities.generateInChiKey(m);
							if ( inchi != null && !StringUtils.isEmpty(inchi.inchiKey)) {
								recs = ResolverDb.lookupByInChIKey(inchi.inchiKey);
								if (recs.size() > 0)
									rec = recs.get(0);
							}
						}
						if (rec != null && !StringUtils.isEmpty(rec.cas)) {
							CAS = rec.cas;
							m.setProperty("CAS", rec.cas);
						}
					}
				}
			}

			if (StringUtils.isEmpty(name) && rec != null && StringUtils.isEmpty(rec.name)) {
				name = rec.name;
				m.setProperty(nameField, rec.name);
			}

			Chemical chemical = this.runChemicalUsingSQLiteDB(CAS, name, m);
			chemicals.add(chemical);
		}

		return chemicals;
	}

	public Chemicals generateDashboard(String data, int format) throws FileNotFoundException, IOException {
		AtomContainerSet acs = WebTEST.loadMolecules(data, format);
		return generateDashboard(acs, "preferred_name");
	}

	public Chemicals generateDashboard(Set<String> data, int format) throws FileNotFoundException, IOException {
		AtomContainerSet acs = WebTEST.loadMolecules(data, format);
		return generateDashboard(acs, "preferred_name");
	}

	public static void filterRecords(Chemical chemical) {
		for (int i=0;i<chemical.scores.size();i++) {

			Score score=chemical.scores.get(i);

			for (int j=0;j<score.records.size();j++) {
				ScoreRecord sr=score.records.get(j);

				//Remove records with diluent in name (since it's technically a mixture- only happens for New Zealand):
				if (sr.name!=null && sr.name.toLowerCase().contains("diluent")) {
					score.records.remove(j);
					j--;
				}
			}

		}
	}




	public static Chemical getChemicalFromRecords(Statement stat,String CAS) {

		Chemical chemical=new Chemical();

		ArrayList<ScoreRecord>array=getRecords(stat,CAS,"HazardRecords");
		if (array.size()==0) return chemical;

		ScoreRecord r0=array.get(0);

		chemical.CAS=r0.CAS;
		chemical.name=r0.name;

		for (ScoreRecord sr:array) {
			if (sr.hazardName==null || sr.hazardName.trim().isEmpty()) continue;
			Score score=chemical.getScore(sr.hazardName);	
			if (score==null) continue;
			score.records.add(sr);
		}
		return chemical;
	}

	public static ArrayList<ScoreRecord> getRecords(Statement stat,String CAS,String tableName) {

		ArrayList<ScoreRecord>array=new ArrayList<>();

		long t1=System.currentTimeMillis();
		ResultSet rs=MySQL_DB.getRecords(stat, tableName, "CAS", CAS);
		long t2=System.currentTimeMillis();


		//   	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");

		try {
			ResultSetMetaData rsmd = rs.getMetaData();

			//			System.out.println(rsmd.getColumnCount()+"\t"+rsmd.getColumnName(1));

			while (rs.next()) {

				ScoreRecord sr=new ScoreRecord();			
				createRecord(rs,sr);

				//				System.out.println(sr.toString("|", ScoreRecord.allFieldNames));

				fixNote(sr);
				array.add(sr);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return array;

	}

	/**
	 * Gets rid of too many breaks for compact display
	 * @param sr
	 */
	private static void fixNote(ScoreRecord sr) {
		if (sr.note==null) return;

		while (sr.note.contains("<br><br>")) {
			sr.note=sr.note.replace("<br><br>", "<br>");
		}

	}

	/**
	 * Gets scorerecord from AA Dashboard- it assumes columns are in the same order as allFieldNames
	 * 
	 * @param rs
	 * @param rsmd 
	 * @return
	 */
//	public  static ScoreRecord createScoreRecord(ResultSet rs, ResultSetMetaData rsmd) {
//		ScoreRecord f=new ScoreRecord(null,null,null);
//
//		try {
//			for (int i = 1; i<= rsmd.getColumnCount(); i++) {
//				try {
//
//					//						System.out.println(i+"\t"+rsmd.getColumnLabel(i));
//
//					Field myField = f.getClass().getDeclaredField(rsmd.getColumnLabel(i));
//
//					if (myField.getType().getName().contains("Double")) {
//						double val=rs.getDouble(i);
//						//						System.out.println("*"+val);
//
//						if (val!=0)	myField.set(f, val);
//
//					} else {
//						String val=rs.getString(i);
//
//						if (val!=null) {
//							myField.set(f, val);
//						} 
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return f;
//
//	}
//	
	
	public static void createRecord(ResultSet rs, Object r) {
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnLabel(i);
				
				if(name.equals("class")) name="class_";
												
				String val=rs.getString(i);
				
				if(val==null || val.equals("-") || val.isBlank()) continue;

				//				System.out.println(name+"\t"+val);

				if (val!=null) {
					Field myField = r.getClass().getDeclaredField(name);	
					
					String type=myField.getType().getName();
					
					if (type.contentEquals("boolean")) {
						myField.setBoolean(r, rs.getBoolean(i));
					} else if (type.contentEquals("double")) {
						myField.setDouble(r, rs.getDouble(i));
					} else if (type.contentEquals("int")) {
						myField.setInt(r, rs.getInt(i));
					} else if (type.contentEquals("long")) {
						myField.setLong(r, rs.getLong(i));

//					} else if (type.contentEquals("java.lang.Long") || type.contentEquals("java.lang.Double") || type.contentEquals("java.lang.Integer")) {
//						myField.set(r, rs.getObject(i));

					} else if (type.contentEquals("java.lang.Double")) {
						//Following parses from string- so if need to change data types, this will let you do it:
						try {
							Double dval=Double.parseDouble(val);						
							myField.set(r, dval);
						} catch (Exception ex) {
							System.out.println("Error parsing "+val+" for field "+name+" to Double for "+rs.getString(1));
						}
					} else if (type.contentEquals("java.lang.Integer")) {
						
//						Integer ival=Integer.parseInt(val);
						myField.set(r,rs.getObject(i));
						
					} else if (type.contentEquals("[B")) {		
						myField.set(r,rs.getBytes(i));
					} else if (type.contentEquals("java.lang.String")) {
						myField.set(r, val);
					} else if (type.contentEquals("java.util.Set")) {
//						System.out.println(name+"\t"+val);
						val=val.replace("[", "").replace("]", "");
						
						String  [] values = val.split(", ");
						Set<String>list=new HashSet<>();
						for (String value:values) {
							list.add(value.trim());
						}
						myField.set(r,list);

					} else if (type.contentEquals("java.util.List")) {
						
						//TODO use getArray instead and then convert array to list?
						
//						System.out.println(name+"\t"+val);
						val=val.replace("[", "").replace("]", "");
						
						String  [] values = val.split(",");
						ArrayList<String>list=new ArrayList<>();
						for (String value:values) {
							list.add(value.trim());
						}
						myField.set(r,list);
					} else {
						System.out.println("Need to implement: "+type);
					}					
										
				}

			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private Chemical runChemicalUsingSQLiteDB(String CAS,String name, IAtomContainer ac) {

		long t1_AA=System.currentTimeMillis();
		if (debug) System.out.print("Getting AA dashboard records from GHS database..."); 		

		//		int iCAS=Integer.parseInt(CAS.replace("-", ""));

		//100x faster if use AA dashboard with primary key (only 1 db record per CAS):
		//		Chemical chemical=CreateGHS_Database.getChemicalFromRecordsUsingPrimaryKey(statAA_Dashboard_Records,CAS);

		Chemical chemical=getChemicalFromRecords(statAA_Dashboard_Records,CAS);

		long t2_AA=System.currentTimeMillis();
		if (debug) System.out.println("done in "+(t2_AA-t1_AA)+ " milliseconds");

		if (chemical==null) chemical=new Chemical();

		chemical.CAS=CAS;
		chemical.name=name;
		chemical.molecularWeight=CDKUtilities.calculateMolecularWeight(ac);
		chemical.atomContainer=ac;

		// Get from TEST:
		if (debug) System.out.println("Start getting records from TEST..."); 
		long t1=System.currentTimeMillis();
		//		generateRecordsFromTEST.createRecords(chemical, WebTEST2.DB_Path_TEST_Predictions);
		generateRecordsFromTEST.createRecords(chemical, null);


		long t2=System.currentTimeMillis();
		if (debug) System.out.println("Done getting records from TEST in "+(t2-t1)+" milliseconds");

		if (filterRecords) {
			filterRecords(chemical);
		}

		//Use trumping algorithm to assign final scores:
		if (finalScoreScheme.equals(finalScoreSchemeTrumping)) {
			this.AssignFinalScoresGreenScreenListTranslator2(chemical);	
		} else if (finalScoreScheme.equals(finalScoreSchemeWeightedAverage)) {
			this.AssignFinalScoresWeightedAverage(chemical);
		} else {
			System.out.println("Invalid final score scheme");
		}

		return chemical;
	}

//	public Chemical runChemicalForGUI(String CAS,String name, String dtxsid, AtomContainer ac,CalculationParameters cp,String versionToxVal,Statement statToxVal) {
	public Chemical runChemicalForGUI(String CAS,String name, String dtxsid, AtomContainer ac,CalculationParameters cp) {

		long t1_AA=System.currentTimeMillis();
		if (debug) System.out.print("Getting AA dashboard records from GHS database..."); 		

		//**********************************************************************

		//Get everything from one database (including OPERA, toxval, ECOTOX, etc):
		Chemical chemical=getChemicalFromRecords(statAA_Dashboard_Records,CAS);//TODO update this db to have everything
		
//		Chemical chemical2=getChemicalFromRecords(statOPERA_Records,CAS);		
//		chemical.addRecords(chemical2);
		
		long t2_AA=System.currentTimeMillis();
		if (debug) System.out.println("done in "+(t2_AA-t1_AA)+ " milliseconds");

			
		chemical.CAS=CAS;
		chemical.name=name;
		chemical.dtxsid=dtxsid;		
		chemical.molecularWeight=CDKUtilities.calculateMolecularWeight(ac);
		chemical.atomContainer=ac;

		//**********************************************************************
		// Get from TEST:
		if (debug) System.out.println("Start getting records from TEST..."); 
		long t1=System.currentTimeMillis();
		//		generateRecordsFromTEST.createRecords(chemical, WebTEST2.DB_Path_TEST_Predictions);
		generateRecordsFromTEST.createRecordsForGUI(chemical, ac,cp);

		
		//**********************************************************************

		long t2=System.currentTimeMillis();
		if (debug) System.out.println("Done getting records from TEST in "+(t2-t1)+" milliseconds");
		
		
//		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
//		System.out.println(gson.toJson(chemical));

		
		if (filterRecords) {
			filterRecords(chemical);
		}
		
//		System.out.println("\nAfter filter:\n"+gson.toJson(chemical));
	
		//**********************************************************************
		//Get records from ToxVal:
//		ParseToxValDB p=new ParseToxValDB();
//		p.getDataFromToxValDB(chemical,versionToxVal,statToxVal);
		//**********************************************************************
		
		removeDuplicateRecords(chemical);
		deleteRecordsFromRetiredSources(chemical);
		//[TMM] Change for OCSPP/OPP made on 11/23/22
		removeDSL_TSCA_Persistence(chemical);

		//Use trumping algorithm to assign final scores:
		if (finalScoreScheme.equals(finalScoreSchemeTrumping)) {
			this.AssignFinalScoresGreenScreenListTranslator2(chemical);	
		} else if (finalScoreScheme.equals(finalScoreSchemeWeightedAverage)) {
			this.AssignFinalScoresWeightedAverage(chemical);
		} else {
			System.out.println("Invalid final score scheme");
		}
		

		return chemical;
	}
	
	/**
	 * Deleting records from toxval sources that should be retired according to Richard Judson
	 * @param chemical
	 */
	void deleteRecordsFromRetiredSources(Chemical chemical) {
		deleteRecordsFromSource(chemical,"ATSDR MRLs 2020");
		deleteRecordsFromSource(chemical,"ATSDR PFAS 2021");
		deleteRecordsFromSource(chemical,"PPRTV (ORNL)");
	}
	

	private void deleteRecordsFromSource(Chemical chemical, String sourceName) {
		for (Score score:chemical.getScores()) {
			for (int i=0;i<score.records.size();i++) {
				ScoreRecord sr=score.records.get(i);
				if(sr.source.equals(sourceName)) {
					System.out.println("Deleting:"+sourceName+"\t"+score.hazard_name);
					score.records.remove(i--);
				}
			}
		}
	}

	
	public Chemical runChemicalForGUI(AtomContainer ac,CalculationParameters cp) {
		DSSToxRecord dr=ac.getProperty("DSSToxRecord");
		return runChemicalForGUI(dr.cas,dr.name,dr.sid, ac,cp);
	}

	/**
	 * OCSPP/OPPT does not want these records for persistence scoring:
	 * 
	 * "The DSL persistence calls were often based on estimated half-life in air
	 * which is not generally a persistence driver under TSCA. The “TSCA Workplan”
	 * scores, as far as I’ve been able to determine, come from a table of early
	 * screening level results. In some cases those ratings were revised as
	 * additional information was collected."
	 * 
	 * @param chemical
	 */
	void removeDSL_TSCA_Persistence(Chemical chemical) {
		Score score=chemical.getScore(Chemical.strPersistence);
//		Gson gson=new Gson();
		for (int i=0;i<score.records.size();i++) {			
			ScoreRecord sr=score.records.get(i);			
			if (sr.source.equals(ScoreRecord.sourceDSL) || sr.source.equals(ScoreRecord.sourceTSCA_Work_Plan)) {
//				System.out.println(gson.toJson(sr));
				score.records.remove(i);
				i--;
			}
		}
	}
	

	void deleteDuplicateScoreRecord(Score score,String source,String sourceOriginal) {

		for (int i=0;i<score.records.size();i++) {

			ScoreRecord sr=score.records.get(i);
			boolean bSource=sr.source.contentEquals(source);
			boolean bSourceOriginal=false;

			if (sourceOriginal==null && (sr.sourceOriginal==null || sr.sourceOriginal.isEmpty())) {
				bSourceOriginal=true;
			} else if (sourceOriginal!=null && sr.sourceOriginal!=null) {
				if (sourceOriginal.contentEquals(sr.sourceOriginal)) bSourceOriginal=true;
			}

			//			if (score.getHazardName().contentEquals(HazardConstants.strCarcinogenicity))
			//				System.out.println(sr.source+"\t"+bSource+"\t"+bSourceOriginal+"\t"+sourceOriginal+"\t"+sr.sourceOriginal);

			//			boolean bTestOrganism=test_organism==null  || test_organism.contentEquals(sr.test_organism.toLowerCase());

			if (bSource && bSourceOriginal) {
				//				System.out.println("Deleted duplicate score record: "+sr.toString("|", ScoreRecord.actualFieldNames));
				score.records.remove(i--);	//it is possible for more than duplicate to exist so can't just exit method when find one			
			}			
		}


	}


	void removeDuplicateRecords(Chemical chemical) {
		//Delete TEST experimental value since covered by chemidplus record for acute mammalian tox
		deleteDuplicateScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral, ScoreRecord.sourceTEST_Experimental,null);//Delete TEST experimental value since covered by chemidplus record for acute mammalian tox
		deleteDuplicateScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral, ScoreRecord.sourceToxVal,"TEST");//ToxVal has duplicate of TEST experimental value
		
		deleteDuplicateScoreRecord(chemical.scoreAcute_Mammalian_ToxicityOral, ScoreRecord.sourceToxVal,ScoreRecord.sourceChemIDplus);//ToxVal has duplicate of chemidplus
		deleteDuplicateScoreRecord(chemical.scoreAcute_Mammalian_ToxicityInhalation, ScoreRecord.sourceToxVal,ScoreRecord.sourceChemIDplus);//ToxVal has duplicate of chemidplus
		deleteDuplicateScoreRecord(chemical.scoreAcute_Mammalian_ToxicityDermal, ScoreRecord.sourceToxVal,ScoreRecord.sourceChemIDplus);//ToxVal has duplicate of chemidplus
		
		deleteDuplicateScoreRecord(chemical.scoreCarcinogenicity, ScoreRecord.sourceROC,null);
		deleteDuplicateScoreRecord(chemical.scoreCarcinogenicity, ScoreRecord.sourceNIOSH_Potential_Occupational_Carcinogens,null);
		deleteDuplicateScoreRecord(chemical.scoreCarcinogenicity, ScoreRecord.sourceProp65,null);
		deleteDuplicateScoreRecord(chemical.scoreCarcinogenicity, ScoreRecord.sourceIARC,null);
		deleteDuplicateScoreRecord(chemical.scoreCarcinogenicity, ScoreRecord.sourceIRIS,null);//we have more info in Note field, but CHA database is using 2008 spreadsheet for IRIS

		deleteDuplicateScoreRecord(chemical.scoreAcute_Aquatic_Toxicity, ScoreRecord.sourceTEST_Experimental,null);//Delete TEST experimental values since covered by ECOTOX values

	}




	private void runNERL() {
		String outputFolder="AA Dashboard\\Output\\NERL";
		String sdfFilePath=outputFolder+"\\NERL.sdf";
		//		 r.createSDFfromCAS_Name_List(sdfFilePath);

		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(sdfFilePath);

		String outputJsonFileName="NERL.json";
		String outputHTMLFileName="NERL.html";
		boolean useJarFiles=false;
		//		run(acs, outputFolder, outputJsonFileName, outputHTMLFileName,useJarFiles);

		//Run using database:
		runUsingSQLiteDB(acs, outputFolder, outputJsonFileName, outputHTMLFileName);

	}


	private void runPhosphates() {

		String outputFolder="AA Dashboard\\Output\\Phosphates";

		String sdfFilePath=outputFolder+"\\phosphates.sdf";
		File OF=new File(outputFolder);
		OF.mkdirs();


		//		MoleculeUtilities.createPhosphates(sdfFilePath);

		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(sdfFilePath);



		String outputJsonFileName="phosphates.json";
		String outputHTMLFileName="phosphates.html";

		long t1=System.currentTimeMillis();
		runUsingSQLiteDB(acs, outputFolder, outputJsonFileName, outputHTMLFileName);
		long t2=System.currentTimeMillis();

		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");

	}

	private void runSdfFile(String sdfFile) {

		String outputFolder="tmp";

		AtomContainerSet acs = MolFileUtilities.LoadFromSDF(sdfFile);

		String outputJsonFileName="most records.json";
		String outputHTMLFileName="most records.html";
		long t1=System.currentTimeMillis();
		String outputFolder2=outputFolder+"/trumping";
		finalScoreScheme=finalScoreSchemeTrumping;
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName);
		long t2=System.currentTimeMillis();

		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");

		t1=System.currentTimeMillis();
		outputFolder2=outputFolder+"/weighted average";
		this.weightedAverageRoundUp=false;
		finalScoreScheme=finalScoreSchemeWeightedAverage;
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName);
		t2=System.currentTimeMillis();
		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");

		t1=System.currentTimeMillis();
		outputFolder2=outputFolder+"/weighted average round up";
		this.weightedAverageRoundUp=true;
		finalScoreScheme=finalScoreSchemeWeightedAverage;
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName);
		t2=System.currentTimeMillis();
		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");
	}

	private void runChemicalsWithGHSRecords() {
		finalScoreScheme=finalScoreSchemeTrumping;

		boolean generateComparisonTable=true;
		boolean writeRecordPages=false;
		String nameField="Name";
		String recordPageFolderName="Hazard Record Pages";

		//************************************************************************************************

		//		String sdfFileName="sample.sdf";
		//		String outputJSONFileName="sample.json";
		//		String outputHTMLFileName="sample.html";

		//************************************************************************************************
		String inputFolder="AADashboard calcs\\chemidplus structures";		
		String outputFolder=inputFolder;

		int index=2;
		String sdfFileName="chemidplus3d"+index+".sdf";
		String outputJSONFileName="chemidplus3d"+index+".json";
		String outputHTMLFileName="chemidplus3d"+index+".html";

		//************************************************************************************************
		//		String inputFolder="AADashboard calcs/NCCT structures";		
		//		String outputFolder=inputFolder;

		//		int index=13;
		//		String sdfFileName="NCCT structures "+index+" marvin.sdf";
		//		String outputJSONFileName="NCCT structures "+index+".json";
		//		String outputHTMLFileName="NCCT structures "+index+".html";

		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(inputFolder+"/"+sdfFileName);

		//		System.out.println(acs.getAtomContainerCount());


		//		Logger logger = LogManager.getLogger(HueckelAromaticityDetector.class);

		long t1=System.currentTimeMillis();				
		runUsingSQLiteDB(acs, outputFolder, outputJSONFileName, outputHTMLFileName, generateComparisonTable,writeRecordPages, nameField,recordPageFolderName);
		long t2=System.currentTimeMillis();		

	}


	private void runMostRecords() {
		// TODO Auto-generated method stub

		String folderName="chemicals with most records";

		//		String inputFolder="AA Dashboard\\Output\\"+folderName;		
		String inputFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\AA Dashboard\\Output\\"+folderName;

		//		String outputFolder="L:\\Priv\\Cin\\NRMRL\\CompTox\\Leora Vegosen\\"+folderName;	
		String outputFolder=inputFolder;


		String sdfFileName="most records.sdf";
		//
		//		String textFileName="cas name list.txt";
		//		MoleculeUtilities.createSDF_from_name_cas_in_text_file(folder,textFileName, sdfFileName);
		//		
		//		String textFileName="cas list.txt";
		//		MoleculeUtilities.createSDF_FromCASListInTextFileUsingSQLiteDB(folder, textFileName, sdfFileName);
		//		
		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(inputFolder+"/"+sdfFileName);

		//		AtomContainerSet acs2=new AtomContainerSet();
		//		acs2.addAtomContainer(acs.getAtomContainer(acs.getAtomContainerCount()-1));

		//		WebTEST2.searchKey="CAS";
		//		WebTEST2.DB_Path_TEST_Predictions="databases/test_results.db";

		String outputJsonFileName="most records.json";
		String outputHTMLFileName="most records.html";
		long t1=System.currentTimeMillis();
		String outputFolder2=outputFolder+"/trumping";
		finalScoreScheme=finalScoreSchemeTrumping;
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName);
		long t2=System.currentTimeMillis();

		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");

		if(true) return;

		t1=System.currentTimeMillis();
		outputFolder2=outputFolder+"/weighted average";
		this.weightedAverageRoundUp=false;
		finalScoreScheme=finalScoreSchemeWeightedAverage;
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName);
		t2=System.currentTimeMillis();
		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");

		t1=System.currentTimeMillis();
		outputFolder2=outputFolder+"/weighted average round up";
		this.weightedAverageRoundUp=true;
		finalScoreScheme=finalScoreSchemeWeightedAverage;
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName);
		t2=System.currentTimeMillis();
		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");
		//
		//		
		//		Chemicals chemicalsTrumping=Chemicals.loadFromJSON(outputFolder+"/trumping/"+outputJsonFileName);
		//		Chemicals chemicalsWeightedAverage1=Chemicals.loadFromJSON(outputFolder+"/weighted average/most records.json");
		//		Chemicals chemicalsWeightedAverage2=Chemicals.loadFromJSON(outputFolder+"/weighted average round up/"+outputJsonFileName);
		////		compareSchemes(chemicalsTrumping, chemicalsWeightedAverage2);
		//		compareSchemes(chemicalsWeightedAverage1, chemicalsWeightedAverage2);
	}


	private void runBadChemical() {
		// TODO Auto-generated method stub

		String folderName="TSCA 17K";

		String inputFolder="AA Dashboard\\Output\\"+folderName;
		String outputFolder=inputFolder;

		//		String sdfFileName="list_chemicals-2018-09-13-07-18-17.sdf";

		//		String CAS="1051371-21-1";
		String CAS="4465-47-8";

		String sdfFileName=CAS+".sdf";
		//		
		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(inputFolder+"/"+sdfFileName);

		System.out.println(acs.getAtomContainerCount());

		String outputJsonFileName=CAS+".json";
		String outputHTMLFileName="N/A";
		long t1=System.currentTimeMillis();
		String outputFolder2=outputFolder+"/trumping";
		finalScoreScheme=finalScoreSchemeTrumping;

		String nameField="PREFERRED_NAME";
		runUsingSQLiteDB(acs, outputFolder2, outputJsonFileName, outputHTMLFileName,false,false,nameField);
		long t2=System.currentTimeMillis();



	}

	private void compileSDF_Files() {
		String folderName="TSCA 17K";

		String inputFolder="AA Dashboard\\Output\\"+folderName;
		String outputFolder=inputFolder;


		String sdfFileName="ChemistryDashboard-Batch-Search_2018-09-20_18_06_58.sdf";


		try {
			int counter = -1;
			IteratingSDFReader reader = new IteratingSDFReader(
					new FileInputStream(inputFolder+"\\"+sdfFileName), DefaultChemObjectBuilder.getInstance());

			org.openscience.cdk.io.MDLV3000Reader mr=new org.openscience.cdk.io.MDLV3000Reader();

			FileInputStream fis=new FileInputStream(inputFolder+"\\"+sdfFileName);

			mr.setReader(fis);


			IAtomContainer ac=mr.readMolecule(DefaultChemObjectBuilder.getInstance());

			System.out.println(ac.getProperties().size());

			//				 while (reader.hasNext()) {
			//				   IAtomContainer m = (IAtomContainer)reader.next();
			//				   
			//				    
			//				   counter++;
			//				   String input=(String)m.getProperty("INPUT");
			//				   
			//				   System.out.println(input+"\t"+m.getAtomCount());
			//				   
			//				   
			//				 } // end loop over molecules in SDF
			System.out.println(counter);

		} catch (Exception e) {
			e.printStackTrace();

		}
	}



	private void runTSCA_17K() {
		// TODO Auto-generated method stub

		String folderName="TSCA 17K";

		String inputFolder="AA Dashboard\\Output\\"+folderName;
		String outputFolder=inputFolder;

		//		String sdfFileName="list_chemicals-2018-09-13-07-18-17.sdf";
		String sdfFileName="list_chemicals-2018-09-13-07-18-17_v2000.sdf";
		//		
		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(inputFolder+"/"+sdfFileName);

		System.out.println(acs.getAtomContainerCount());

		int numSplit=10;
		int currentSplit=1;

		int count=(int)Math.ceil(acs.getAtomContainerCount()/numSplit);

		int start=(currentSplit-1)*count;
		int stop=start+count-1;

		if (stop>acs.getAtomContainerCount()) stop=acs.getAtomContainerCount()-1;

		AtomContainerSet acs2=new AtomContainerSet();
		for (int i=start;i<=stop;i++) {
			acs2.addAtomContainer(acs.getAtomContainer(i));
		}

		String outputJsonFileName="most records "+currentSplit+" of "+numSplit+".json";
		String outputHTMLFileName="N/A";
		long t1=System.currentTimeMillis();
		String outputFolder2=outputFolder+"/trumping";
		finalScoreScheme=finalScoreSchemeTrumping;

		String nameField="PREFERRED_NAME";

		runUsingSQLiteDB(acs2, outputFolder2, outputJsonFileName, outputHTMLFileName,false,false,nameField);
		long t2=System.currentTimeMillis();


	}

	private void lookAtCoverage() {
		int numSplit=11;

		String folder="AA Dashboard\\Output\\TSCA 17K\\trumping";


		AtomContainerSet acs=MolFileUtilities.LoadFromSDF("AA Dashboard\\Output\\TSCA 17K\\list_chemicals-2018-09-13-07-18-17_v2000.sdf");
		for (int i=0;i<acs.getAtomContainerCount();i++) {
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
			String CAS=(String)ac.getProperty("CAS");
			System.out.println(i+"|"+CAS+"|");
		}


		float [] counts=new float[19];
		String [] hazardNames=new String [19];

		int numChemicals=0;

		ScoreRecord sr=new ScoreRecord(null,null,null);

		for (int i=1;i<=11;i++) {
			//		for (int i=1;i<=2;i++) {	


			String filename="TSCA 17K "+i+" of "+numSplit+".json";

			Chemicals chemicals=Chemicals.loadFromJSON(folder+"\\"+filename);

			for (int j=0;j<chemicals.size();j++) {
				Chemical chemical=chemicals.get(j);

				//				System.out.println(chemical.CAS);

				numChemicals++;

				//				System.out.println(numChemicals+"\t"+chemical.CAS+"\t"+chemical.name);

				for (int k=0;k<19;k++) {

					Score score=chemical.scores.get(k);

					if (hazardNames[k]==null) hazardNames[k]=score.hazard_name;


					String authority="";

					if (score.final_score_source!=null) authority=sr.getListType(score.final_score_source);

					if (!authority.isEmpty() && !authority.equals("QSAR Model")) {
						//						System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+score.final_score+"\t"+score.final_score_source+"\t"+authority);
						counts[k]++;
					}
				}
			}
		}
		System.out.println("NumChemicals="+numChemicals);

		for (int k=0;k<19;k++) {
			counts[k]/=(float)numChemicals;

			System.out.println((k+1)+"\t"+hazardNames[k]+"\t"+counts[k]);
		}



	}


	//	private void TEST_Comparison() {
	//		
	//		
	//		
	//		int numSplit=11;
	//		
	//		String folder="AA Dashboard\\Output\\TSCA 17K\\trumping";
	//		
	////		String hazard=Chemical.strAcute_Mammalian_ToxicityOral;
	//		String hazard=Chemical.strGenotoxicity_Mutagenicity;
	//		
	//		
	//		int numChemicals=0;
	//
	//		for (int i=1;i<=11;i++) {
	////		for (int i=1;i<=2;i++) {	
	//			
	//			
	//			String filename="TSCA 17K "+i+" of "+numSplit+".json";
	//			
	//			Chemicals chemicals=Chemicals.loadFromJSON(folder+"\\"+filename);
	//			
	//			for (int j=0;j<chemicals.size();j++) {
	//				Chemical chemical=chemicals.get(j);
	//			
	////				System.out.println(chemical.CAS);
	//				
	////				System.out.println(numChemicals+"\t"+chemical.CAS+"\t"+chemical.name);
	//				
	//				for (int k=0;k<19;k++) {
	//					
	//					Score score=chemical.scores.get(k);
	//					
	//					if (!score.hazard_name.equals(hazard)) continue;
	//					
	//					
	//					String authority="";
	//							
	//					if (score.final_score_source!=null) authority=ScoreRecord.getListType(score.final_score_source);
	//
	//					if (authority.isEmpty() || authority.equals("QSAR Model")) continue;
	//						
	//					for (int l=0;l<score.records.size();l++) {
	//						ScoreRecord sr=score.records.get(l);
	//						String currentauthority=sr.getListType();
	//						
	//						
	////						System.out.println(chemical.CAS+"\t"+sr.source+"\t"+sr.score);
	//						
	//						
	//						if (sr.source.equals(ScoreRecord.sourceTEST_Predicted) && !sr.score.equals("N/A")) {
	//							numChemicals++;
	//							System.out.println(chemical.CAS+"\t"+score.final_score_source+"\t"+score.final_score+"\t"+sr.score);
	////							break;
	//						} else {
	////							System.out.println(chemical.CAS+"\t"+sr.source+"\t"+sr.score);
	//						}
	//					}
	//						
	////						System.out.println(chemical.CAS+"\t"+score.hazard_name+"\t"+score.final_score+"\t"+score.final_score_source+"\t"+authority);
	//					
	//				}
	//			}
	//		}
	//		System.out.println("NumChemicals="+numChemicals);
	//		
	//		
	//	}



	private void compareSchemes(Chemicals chemicalsTrumping, Chemicals chemicalsWeightedAverage) {
		int countTotal=0;
		int countDiff=0;

		for (int i=0;i<chemicalsTrumping.size();i++) {
			Chemical chemicalTrumping=chemicalsTrumping.get(i);
			Chemical chemicalWeightedAverage=chemicalsWeightedAverage.get(i);

			for (int j=0;j<chemicalTrumping.scores.size();j++) {
				Score scoreTrumping=chemicalTrumping.scores.get(j);
				Score scoreWeightedAverage=chemicalWeightedAverage.scores.get(j);

				if (scoreTrumping.final_score.equals(ScoreRecord.scoreNA)) continue;

				if (scoreTrumping.records.size()<2) continue;

				countTotal++;

				if (!scoreTrumping.final_score.equals(scoreWeightedAverage.final_score)) {
					countDiff++;
					System.out.println(chemicalTrumping.CAS+"\t"+scoreTrumping.hazard_name+"\t"+scoreTrumping.final_score+"\t"+scoreWeightedAverage.final_score);
				}
			}
		}

		System.out.println(countDiff+"\t"+countTotal);
	}


	private void runTSCA_chemicals() {

		String folder="AA Dashboard\\Output\\TSCA";

		String sdfFilePath=folder+"\\TSCA top ten.sdf";

		//		MoleculeUtilities.createTSCA_TopTen_SDF(sdfFilePath);
		//
		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(sdfFilePath);
		//		
		String outputJsonFileName="TSCA.json";
		String outputHTMLFileName="TSCA.html";
		//		boolean useJarFiles=true;
		//		
		long t1=System.currentTimeMillis();
		String outputFolder=folder+"/trumping";
		finalScoreScheme=finalScoreSchemeTrumping;
		runUsingSQLiteDB(acs, outputFolder, outputJsonFileName, outputHTMLFileName);
		long t2=System.currentTimeMillis();
		//		
		//		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");
		//		
		//		t1=System.currentTimeMillis();
		//		outputFolder=folder+"/weighted average";
		//		finalScoreScheme=finalScoreSchemeWeightedAverage;
		//		runUsingSQLiteDB(acs, outputFolder, outputJsonFileName, outputHTMLFileName);
		//		t2=System.currentTimeMillis();
		//		System.out.println("overall run time:"+(t2-t1)/1000.0+ " seconds");

		//		Chemicals chemicalsTrumping=Chemicals.loadFromJSON(folder+"/trumping/"+outputJsonFileName);
		//		Chemicals chemicalsWeightedAverage=Chemicals.loadFromJSON(folder+"/weighted average/"+outputJsonFileName);
		//		
		//		compareSchemes(chemicalsTrumping, chemicalsWeightedAverage);

	}


	private void runFlameRetardants() {

		String sdfFilePath="AA Dashboard\\Output\\flame retardants\\flame retardants.sdf";
		//		MoleculeUtilities mu=new MoleculeUtilities();
		//		mu.createFlameRetardantsSDF(sdfFilePath);

		AtomContainerSet acs=MolFileUtilities.LoadFromSDF(sdfFilePath);

		// a.testFlameRetardantsTEST();
		long t1 = System.currentTimeMillis();

		String outputJsonFileName="flame retardants.json";
		String outputHTMLFileName="flame retardants.html";

		String outputFolder="AA Dashboard\\Output\\TSCA_SQLITE";
		runUsingSQLiteDB(acs, outputFolder, outputJsonFileName, outputHTMLFileName);

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// long t1=System.currentTimeMillis();
		// WebTEST wt=new WebTEST();
		// long t2=System.currentTimeMillis();

		// System.out.println((t2-t1));

		AADashboard a = new AADashboard();
		//NCCT_ID.db
				a.runSdfFile(args[0]);
		//		a.runMostRecords();
		//		a.runChemicalsWithGHSRecords();
		//		a.mergeAADashboardResults();
		//		a.runTSCA_17K();
		//		a.runBadChemical();
		//		a.lookAtCoverage();
		//		a.TEST_Comparison();
		//		a.compileSDF_Files();

		//************************************************************************************************************		
		//		a.runFlameRetardants();
		//************************************************************************************************************		
		//		a.runTSCA_chemicals();



		//************************************************************************************************************
		//		a.runPhosphates();
		//************************************************************************************************************

		//		a.runNERL();
		//************************************************************************************************************		

		//		String CAS="106-44-5";
		//		Chemical chemical=a.runChemical(CAS, "name", getMolFileFromDashboard(CAS), false);
		//		System.out.println(chemical.toJSONString());

		//*******************************************************************************************************************
		//Create jar file with all the records from all the sources:
		//		a.createRecordsJarFile(); 
	}


}
