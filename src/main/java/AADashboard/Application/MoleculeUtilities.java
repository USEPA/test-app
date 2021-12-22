package AADashboard.Application;

import java.io.File;
import java.io.FileWriter;
import java.sql.Statement;
import java.util.ArrayList;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.io.MDLV2000Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import AADashboard.Utilities.FileUtilities;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Database.ChemistryDashboardRecord;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.misc.MolFileUtilities;
import gov.epa.api.Chemical;

public class MoleculeUtilities {

public static AtomContainerSet createAtomContainerSetFromCASList(ArrayList<String> casList) {
		
		AtomContainerSet acs=new AtomContainerSet();
		
		for (int i=0;i<casList.size();i++) {
			String CAS=casList.get(i);
			String molFileString=getMolFileFromDashboard(CAS);
			
//			System.out.println(CAS+"\t"+molFileString);
			
			
			if (molFileString==null) {
				System.out.println(CAS+"\tstructure N/A");
				continue;
			}
			
			AtomContainerSet acsCurrent=MolFileUtilities.LoadFromSdfString(molFileString);
//			System.out.println(chemicali.CAS+"\t"+acs.getAtomContainer(0).getAtomCount());
			
			AtomContainer m=(AtomContainer)acsCurrent.getAtomContainer(0);
			m.setProperty("CAS", CAS);
			
			if (m==null || m.getAtomCount()==0) {
				System.out.println(CAS+"\tstructure N/A");
				continue;
			} else {
				System.out.println(CAS+"\t"+m.getAtomCount());
			}
			
//			System.out.println(m.getAtomCount()+"\t"+m.getBondCount());
			
			acs.addAtomContainer(m);
//			try {
//				Thread.sleep(3000);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}

		}
		return acs;
		
	}
	

	
	

	public Chemical createChemicalFromCAS(String CAS) {
		
		Chemical chemical=new Chemical();
		chemical.CAS=CAS;
		chemical.molFileV3000=this.getMolFileFromDashboard(CAS);
		
		AtomContainerSet acs=MolFileUtilities.LoadFromSdfString(chemical.molFileV3000);
//		System.out.println(chemicali.CAS+"\t"+acs.getAtomContainer(0).getAtomCount());
		acs.getAtomContainer(0).setProperty("CAS", chemical.CAS);

		AtomContainer m=(AtomContainer)acs.getAtomContainer(0);
		
		chemical.molecularWeight=CDKUtilities.calculateMolecularWeight(m);
		
		return chemical;
		
	}
	
	
	void createMolFilesFrom_CAS_List(String caslistpath,String molFileFolder) {
		
		ArrayList<String>casList=ToxPredictor.Utilities.Utilities.readFileToArray(caslistpath);
		
		for(String CAS:casList) {
			
			File molFile=new File(molFileFolder+"/"+CAS+".mol");
			if (molFile.exists()) continue;
			
			
			String molFileString=getMolFileFromDashboard(CAS);
			AtomContainerSet acsCurrent=MolFileUtilities.LoadFromSdfString(molFileString);
//			System.out.println(chemicali.CAS+"\t"+acs.getAtomContainer(0).getAtomCount());
			
			if (acsCurrent==null || acsCurrent.getAtomContainer(0)==null) {
				System.out.println(CAS+"\tstructure N/A");
				continue;
			} 
			
			AtomContainer m=(AtomContainer)acsCurrent.getAtomContainer(0);
			m.setProperty("CAS", CAS);
			
			System.out.println(CAS+"\t"+m.getAtomCount());
			
			try {
				
						
				
				FileWriter fw=new FileWriter(molFile);
				MDLV2000Writer mw=new MDLV2000Writer(fw);
				mw.write(m);
				
				fw.write("> <CAS>\r\n");
				fw.write(m.getProperty("CAS")+"\r\n");
				
				if (m.getProperty("name")!=null) {
					fw.write("> <name>\r\n");
					fw.write(m.getProperty("name")+"\r\n");
				}

				mw.close();
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			
		}
		
	}
	
	void createSDF_From_CAS_List(String caslistpath,String sdfFilePath) {
		
		ArrayList<String>casList=ToxPredictor.Utilities.Utilities.readFileToArray(caslistpath);
		AtomContainerSet acs=createAtomContainerSetFromCASList(casList);
		System.out.println(acs.getAtomContainerCount());
		MolFileUtilities.saveAtomContainerSetToSDFFile(sdfFilePath, acs);
	}

	

	
	void createSDFfromCAS_Name_List(String sdfFilePath) {
		
		ArrayList<String>casList=new ArrayList<String>();
		
		casList.add("208-96-8");
		casList.add("191-24-2");
		casList.add("101-55-3");
		casList.add("86-74-8");
		casList.add("7005-72-3");
		casList.add("541-73-1");
		casList.add("10061-01-5");
		casList.add("10061-02-6");
		casList.add("131-11-3");
		casList.add("108-87-2");
		casList.add("99-09-2");
		casList.add("88-75-5");
		casList.add("100-02-7");
		casList.add("85-01-8");
//		casList.add("179601-23-1");
//		casList.add("1330-20-7");
		casList.add("108-38-3");
		casList.add("106-42-3");
		
		ArrayList<String>nameList=new ArrayList<String>();
		nameList.add("Acenaphthylene");
		nameList.add("Benzo(g,h,i)perylene");
		nameList.add("Bromophenyl-phenyl ether, 4-");
		nameList.add("Carbazole");
		nameList.add("Chlorophenyl-phenyl ether, 4-");
		nameList.add("Dichlorobenzene, 1,3-");
		nameList.add("Dichloropropene, cis-1,3-");
		nameList.add("Dichloropropene, trans-1,3-");
		nameList.add("Dimethylphthalate");
		nameList.add("Methylcyclohexane");
		nameList.add("Nitroaniline, 3-");
		nameList.add("Nitrophenol, 2-");
		nameList.add("Nitrophenol, 4-");
		nameList.add("Phenanthrene");
		nameList.add("Xylene, m-");
		nameList.add("Xylene, p-");
		
		AtomContainerSet acs=createAtomContainerSetCASNameList(casList,nameList);
		
		System.out.println(acs.getAtomContainerCount());
		
		
		MolFileUtilities.saveAtomContainerSetToSDFFile(sdfFilePath, acs);
		
		
	}
	
	void createSDF_From_CAS_List(String sdfFilePath) {
		
		ArrayList<String>casList=new ArrayList<String>();
		
		casList.add("208-96-8");
		casList.add("191-24-2");
		casList.add("101-55-3");
		casList.add("86-74-8");
		casList.add("7005-72-3");
		casList.add("541-73-1");
		casList.add("10061-01-5");
		casList.add("10061-02-6");
		casList.add("131-11-3");
		casList.add("108-87-2");
		casList.add("99-09-2");
		casList.add("88-75-5");
		casList.add("100-02-7");
		casList.add("85-01-8");
		casList.add("179601-23-1");
		casList.add("1330-20-7");
		casList.add("108-38-3");
		casList.add("106-42-3");
		AtomContainerSet acs=createAtomContainerSetFromCASList(casList);
		System.out.println(acs.getAtomContainerCount());
		MolFileUtilities.saveAtomContainerSetToSDFFile(sdfFilePath, acs);
		
	}
	
	static void createSDF_FromCASListInTextFileUsingSQLiteDB(String folder,String textFileName,String sdfFileName) {
		
		String DB_Path_NCCT_ID_Records = "databases/NCCT_ID.db";
		Statement statNCCT_ID_Records = MySQL_DB.getStatement(DB_Path_NCCT_ID_Records);

		
		ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(folder+"/"+textFileName);

		ArrayList<String>casList=new ArrayList<String>();
		ArrayList<String>nameList=new ArrayList<String>();
		
		AtomContainerSet acs=new AtomContainerSet();
		
		for (String CAS:lines) {
			casList.add(CAS);
			ChemistryDashboardRecord rec = ChemistryDashboardRecord.lookupDashboardRecordAll("casrn", CAS,statNCCT_ID_Records);
			
			if (rec==null) {
				System.out.println(CAS+"\tnot in database");	
				continue;
			}
			
//			System.out.println(CAS);
			nameList.add(rec.preferred_name);
			
			
			
			if (rec.Canonical_QSARr==null) {
				System.out.println(CAS+"\tstructure N/A");
				continue;
			} 
			
			
			System.out.println(CAS+"\t"+rec.Canonical_QSARr);
			
			AtomContainer ac=WebTEST.prepareSmilesMolecule(rec.Canonical_QSARr);
			ac.setProperty("CAS", CAS);
			ac.setProperty("name", rec.preferred_name);
			acs.addAtomContainer(ac);
		}
		
		
		System.out.println(acs.getAtomContainerCount());
		MolFileUtilities.saveAtomContainerSetToSDFFile(folder+"/"+sdfFileName, acs);

	}
	
	
static void createSDF_from_name_cas_in_text_file(String folder,String textFileName,String sdfFileName) {

	ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(folder+"/"+textFileName);

		ArrayList<String>casList=new ArrayList<String>();
		ArrayList<String>nameList=new ArrayList<String>();
		
		for (String Line :lines) {
			String CAS=Line.substring(0,Line.indexOf("\t"));
			String name=Line.substring(Line.indexOf("\t")+1,Line.length());
			casList.add(CAS);
			nameList.add(name);
		}
		
		AtomContainerSet acs=createAtomContainerSetCASNameList(casList,nameList);
//		System.out.println(acs.getAtomContainerCount());
		MolFileUtilities.saveAtomContainerSetToSDFFile(folder+"/"+sdfFileName, acs);
	}
	
	
	static void createTSCA_TopTen_SDF(String sdfFilePath) {
		
		ArrayList<String>casList=new ArrayList<String>();
		
		casList.add("79-01-6");
		casList.add("127-18-4");
		casList.add("81-33-4");
		casList.add("872-50-4");
		casList.add("123-91-1");
		casList.add("106-94-5");
		casList.add("56-23-5");
		casList.add("1332-21-4");
		casList.add("75-09-2");
		casList.add("25637-99-4");
		casList.add("3194-55-6");
		casList.add("3194-57-8");

		ArrayList<String>nameList=new ArrayList<String>();
		
		nameList.add("Trichloroethylene");
		nameList.add("Perchloroethylene");
		nameList.add("Pigment Violet 29");
		nameList.add("N – methylpyrrolidone");
		nameList.add("1,4 - dioxane");
		nameList.add("1 - Bromopropane");
		nameList.add("Carbon tetrachloride");
		nameList.add("Asbestos");
		nameList.add("Methylene chloride");	
		nameList.add("Hexabromocyclododecane");
		nameList.add("1,2,5,6,9,10-Hexabromocyclododecane");
		nameList.add("1,2,5,6-Tetrabromocyclooctane");
		
		
		AtomContainerSet acs=createAtomContainerSetCASNameList(casList,nameList);
		
//		System.out.println(acs.getAtomContainerCount());
		
		MolFileUtilities.saveAtomContainerSetToSDFFile(sdfFilePath, acs);
		
	}
	
void createFlameRetardantsSDF(String sdfFilePath) {
		
		ArrayList<String>casList=new ArrayList<String>();
		ArrayList<String>nameList=new ArrayList<String>();
		
		
		casList.add("1163-19-5");
		nameList.add("decaBDE");

		casList.add("115-86-6");
		nameList.add("TPP");
		
		casList.add("57583-54-7");
		nameList.add("RDP");

		casList.add("5945-33-5");
		nameList.add("BPADP");

		casList.add("71-43-2");
		nameList.add("benzene");

		casList.add("79-06-1");
		nameList.add("acrylamide");

		casList.add("60-51-5");
		nameList.add("dimethoate");
		
		casList.add("117-84-0");
		nameList.add("Di-n-octyl phthalate");

		AtomContainerSet acs=createAtomContainerSetCASNameList(casList,nameList);
		

		//Add benzene but change the cas so we have a chemical that isnt present in the database by cas so that new TEST calcs run
		String molFileV3000 = "71-43-2	\r\n" + "  Mrv1533009301517202D          \r\n" + "\r\n"
				+ "  0  0  0     0  0            999 V3000\r\n" + "M  V30 BEGIN CTAB\r\n"
				+ "M  V30 COUNTS 6 6 0 0 0\r\n" + "M  V30 BEGIN ATOM\r\n" + "M  V30 1 C 2.6674 -2.31 0 0\r\n"
				+ "M  V30 2 C 1.3337 -3.08 0 0\r\n" + "M  V30 3 C 0 -2.31 0 0\r\n" + "M  V30 4 C 0 -0.77 0 0\r\n"
				+ "M  V30 5 C 1.3337 0 0 0\r\n" + "M  V30 6 C 2.6674 -0.77 0 0\r\n" + "M  V30 END ATOM\r\n"
				+ "M  V30 BEGIN BOND\r\n" + "M  V30 1 2 1 2\r\n" + "M  V30 2 1 1 6\r\n" + "M  V30 3 1 2 3\r\n"
				+ "M  V30 4 2 3 4\r\n" + "M  V30 5 1 4 5\r\n" + "M  V30 6 2 5 6\r\n" + "M  V30 END BOND\r\n"
				+ "M  V30 END CTAB\r\n" + "M  END";
		
		AtomContainerSet acs2=MolFileUtilities.LoadFromSdfString(molFileV3000);
//		System.out.println(chemicali.CAS+"\t"+acs.getAtomContainer(0).getAtomCount());
		acs2.getAtomContainer(0).setProperty("CAS", "NotBenzeneLOL");
		AtomContainer atomContainer=(AtomContainer)acs.getAtomContainer(0);

		acs.addAtomContainer(atomContainer);
		
		System.out.println(acs.getAtomContainerCount());
		
		
		MolFileUtilities.saveAtomContainerSetToSDFFile(sdfFilePath, acs);
		
		
	}
	
	public static String getMolFileFromDashboard(String CAS) {
		String strURL = "https://actorws.epa.gov/actorws/dsstox/v02/molfile.json?casrn=" + CAS;

		String jsonStr = FileUtilities.getText(strURL);

		 
		// DataRow jo2=gson.fromJson(jsonStr,DataRow.class);
		// System.out.println(jo2.molfile);

		// {"DataRow":{"molfile":"\n Mrv1533009301517252D \n\n 0 0 0 0 0 999 V3000\nM
		// V30 BEGIN CTAB\nM V30 COUNTS 49 54 0 0 0\nM V30 BEGIN ATOM\nM V30 1 C 22.2274
		// -6.4735 0 0\nM V30 2 C 19.4711 -10.393 0 0\nM V30 3 C 11.2527 -6.2712 0 0\nM
		// V30 4 C 12.7699 -3.6413 0 0\nM V30 5 C 10.6965 -4.1977 0 0\nM V30 6 C 12.0113
		// -4.9563 0 0\nM V30 7 C 15.9814 -7.2575 0 0\nM V30 8 P 18.6619 -7.2575 0 0\nM
		// V30 9 O 17.878 -5.9172 0 0\nM V30 10 O 19.4205 -8.5977 0 0\nM V30 11 O
		// 17.3217 -8.0161 0 0\nM V30 12 O 19.9515 -6.4735 0 0\nM V30 13 C 14.6665
		// -4.9563 0 0\nM V30 14 C 13.3263 -5.7149 0 0\nM V30 15 C 15.9814 -5.7149 0
		// 0\nM V30 16 C 13.3263 -7.2322 0 0\nM V30 17 C 14.6665 -8.0161 0 0\nM V30 18 C
		// 4.046 -4.9563 0 0\nM V30 19 O 4.046 -3.4138 0 0\nM V30 20 C 3.0597 -1.3149 0
		// 0\nM V30 21 O 4.6023 -1.3402 0 0\nM V30 22 O 6.1448 -3.9954 0 0\nM V30 23 P
		// 5.3862 -2.6552 0 0\nM V30 24 O 6.7011 -1.8966 0 0\nM V30 25 C 8.0161 -2.6552
		// 0 0\nM V30 26 C 8.0161 -4.1977 0 0\nM V30 27 C 9.3563 -1.8966 0 0\nM V30 28 C
		// 9.3563 -4.9563 0 0\nM V30 29 C 10.6965 -2.6552 0 0\nM V30 30 C 0.7839 0 0
		// 0\nM V30 31 C 0 -1.3149 0 0\nM V30 32 C 2.3011 -0.0253 0 0\nM V30 33 C 0.7839
		// -2.6552 0 0\nM V30 34 C 2.3011 -2.6552 0 0\nM V30 35 C 4.046 -8.0161 0 0\nM
		// V30 36 C 2.7057 -7.2322 0 0\nM V30 37 C 5.3862 -7.2322 0 0\nM V30 38 C 2.7057
		// -5.7149 0 0\nM V30 39 C 5.3862 -5.7149 0 0\nM V30 40 C 20.786 -11.1516 0 0\nM
		// V30 41 C 20.786 -12.6941 0 0\nM V30 42 C 19.4711 -13.4528 0 0\nM V30 43 C
		// 18.1308 -11.1516 0 0\nM V30 44 C 18.1308 -12.6941 0 0\nM V30 45 C 22.986
		// -5.1585 0 0\nM V30 46 C 24.5286 -5.1585 0 0\nM V30 47 C 25.2872 -6.4735 0
		// 0\nM V30 48 C 22.986 -7.8137 0 0\nM V30 49 C 24.5033 -7.8137 0 0\nM V30 END
		// ATOM\nM V30 BEGIN BOND\nM V30 1 1 1 12\nM V30 2 2 1 45\nM V30 3 1 1 48\nM V30
		// 4 1 2 10\nM V30 5 2 2 40\nM V30 6 1 2 43\nM V30 7 1 3 6\nM V30 8 1 4 6\nM V30
		// 9 1 5 6\nM V30 10 1 5 28\nM V30 11 2 5 29\nM V30 12 1 6 14\nM V30 13 1 7
		// 11\nM V30 14 1 7 15\nM V30 15 2 7 17\nM V30 16 2 8 9\nM V30 17 1 8 10\nM V30
		// 18 1 8 11\nM V30 19 1 8 12\nM V30 20 1 13 14\nM V30 21 2 13 15\nM V30 22 2 14
		// 16\nM V30 23 1 16 17\nM V30 24 1 18 19\nM V30 25 1 18 38\nM V30 26 2 18 39\nM
		// V30 27 1 19 23\nM V30 28 1 20 21\nM V30 29 1 20 32\nM V30 30 2 20 34\nM V30
		// 31 1 21 23\nM V30 32 2 22 23\nM V30 33 1 23 24\nM V30 34 1 24 25\nM V30 35 1
		// 25 26\nM V30 36 2 25 27\nM V30 37 2 26 28\nM V30 38 1 27 29\nM V30 39 1 30
		// 31\nM V30 40 2 30 32\nM V30 41 2 31 33\nM V30 42 1 33 34\nM V30 43 1 35 36\nM
		// V30 44 2 35 37\nM V30 45 2 36 38\nM V30 46 1 37 39\nM V30 47 1 40 41\nM V30
		// 48 2 41 42\nM V30 49 1 42 44\nM V30 50 2 43 44\nM V30 51 1 45 46\nM V30 52 2
		// 46 47\nM V30 53 1 47 49\nM V30 54 2 48 49\nM V30 END BOND\nM V30 END CTAB\nM
		// END\n"}}

		JsonParser jp = new JsonParser();
		JsonObject jo = (JsonObject) jp.parse(jsonStr);

		
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		
//		System.out.println(gson.toJson(jo));


		if (jo.isJsonNull()) return null;
		if (jo.get("DataRow").getAsJsonObject().get("molfile").isJsonNull()) return null;

		return jo.get("DataRow").getAsJsonObject().get("molfile").getAsString();

		// System.out.println(chemical.CAS+"\t"+chemical.molFileV3000);
	}
	
	/**
	 * Gets record from actor
	 * 
	 * @param identifier
	 * @return
	 */
	public static DSSToxRecord getDSSToxRecordFromDashboard(String identifier) {

		identifier=identifier.replace(" ", "%20");
		String strURL = "https://actorws.epa.gov/actorws/chemIdentifier/v01/resolve.json?identifier=" + identifier;
					
		String jsonStr = FileUtilities.getText(strURL);
		
//		System.out.println(jsonStr);
		
		if (jsonStr==null) return null;

		JsonParser jp = new JsonParser();
		JsonObject jo = (JsonObject) jp.parse(jsonStr);
		
		if (jo.isJsonNull()) return null;

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		
		JsonObject datarow=jo.get("DataRow").getAsJsonObject();

		if (datarow.get("casrn").isJsonNull()) return null;

		DSSToxRecord d=new DSSToxRecord();
		d.cas=datarow.get("casrn").getAsString();
		if (!datarow.get("dtxcid").isJsonNull()) d.cid=datarow.get("dtxcid").getAsString();
//		if (!datarow.get("synGsid").isJsonNull()) d.gsid=datarow.get("synGsid").getAsString();
		
		if (!datarow.get("jChemInChIKey").isJsonNull()) {//are they null if it's not in there? or blank or error?
			d.inchiKey=datarow.get("jChemInChIKey").getAsString();	
		} else if (!datarow.get("indigoInChIKey").isJsonNull()) {
			d.inchiKey=datarow.get("indigoInChIKey").getAsString();	
		}
		if (!datarow.get("preferredName").isJsonNull()) d.name=datarow.get("preferredName").getAsString();
		if (!datarow.get("dtxsid").isJsonNull()) d.sid=datarow.get("dtxsid").getAsString();
		
		if (!datarow.get("smiles").isJsonNull()) d.smiles=datarow.get("smiles").getAsString();
				
//		System.out.println(gson.toJson(datarow));
//		System.out.println(d.toString());
		
		return d;
	}
	/**
	 * Search using V2 DSSTOX for qsar ready structure and parse list
	 * Not done!!!TODO
	 * 
	 * @param identifier
	 * @return
	 */
	public static DSSToxRecord getDSSToxRecordFromDashboardQSARReady(String identifier) {

		identifier=identifier.replace(" ", "%20");
		String strURL = "https://actorws.epa.gov/actorws/dsstox/v02/qsar.json?identifier=" + identifier;
		
		/**
		 * {"DataList":{"list":[{"dsstoxCompoundId":"DTXCID20135","smiles":"C1=CC=CC=C1",
		 * "imageURL":"http://actorws.epa.gov/actorws/chemical/image?dtxcid=DTXCID20135&w=150&h=150&fmt=svg",
		 * "qsarImageURL":"http://actorws.epa.gov/actorws/chemical/image?dtxcid=DTXCID20135&w=150&h=150&fmt=svg",
		 * "qsarDtxcid":"DTXCID20135","qsarSmiles":"C1=CC=CC=C1","qcNotes":"","source":"Public",
		 * "qcName":"DSSTox_Low","qcLabel":"DSSTox_Low","qcDescription":"Level
		 * 2: Expert curated, unique chemical identifiers confirmed using multiple
		 * public sources"}],"count":1}}
		 */
		//TODO finish code below...
		
//		String jsonStr = FileUtilities.getText(strURL);
//
//		JsonParser jp = new JsonParser();
//		JsonObject jo = (JsonObject) jp.parse(jsonStr);
//		
//		if (jo.isJsonNull()) return null;
//
//		GsonBuilder builder = new GsonBuilder();
//		builder.setPrettyPrinting();
//		Gson gson = builder.create();
//		
//		JsonObject datarow=jo.get("DataRow").getAsJsonObject();
//				
//		DSSToxRecord d=new DSSToxRecord();
//		
//		d.cas=datarow.get("casrn").getAsString();
//		d.cid=datarow.get("dtxcid").getAsString();
//		d.gsid=datarow.get("synGsid").getAsString();
////		d.inchi
//		
//		if (datarow.get("jChemInChIKey")!=null) {//are they null if it's not in there? or blank or error?
//			d.inchiKey=datarow.get("jChemInChIKey").getAsString();	
//		} else if (datarow.get("indigoInChIKey")!=null) {
//			d.inchiKey=datarow.get("indigoInChIKey").getAsString();	
//		}
//		d.name=datarow.get("preferredName").getAsString();
//		d.sid=datarow.get("dtxsid").getAsString();
//		d.smiles=datarow.get("smiles").getAsString();
//				
////		System.out.println(gson.toJson(datarow));
////		System.out.println(d.toString());
		
		return null;
	}
	
	public static AtomContainerSet createAtomContainerSetCASNameList(ArrayList<String> casList,ArrayList<String> nameList) {
		
		AtomContainerSet acs=new AtomContainerSet();
		
		for (int i=0;i<casList.size();i++) {
			String CAS=casList.get(i);
			String name=nameList.get(i);
					
			String molFileString=getMolFileFromDashboard(CAS);

			AtomContainer m=new AtomContainer();
			
			if (molFileString==null) {
				System.out.println(CAS+"\tstructure N/A");
			} else {
				AtomContainerSet acsCurrent=MolFileUtilities.LoadFromSdfString(molFileString);
//				System.out.println(chemicali.CAS+"\t"+acs.getAtomContainer(0).getAtomCount());
				m=(AtomContainer)acsCurrent.getAtomContainer(0);
			}
			
			m.setProperty("CAS", CAS);
			m.setProperty("name", name);
			
//			System.out.println(m.getAtomCount()+"\t"+m.getBondCount());
			
			acs.addAtomContainer(m);

		}
		return acs;
		
	}
	
	void actor() {
		/*
		 * The collection of public services are at … http://actorws.epa.gov/actorws/
		 * 
		 * We have another set that is currently only internal, including our initial
		 * QSAR models. The catalog is at …
		 * http://134.67.216.45:9191/actorws/indexInternal.jsp
		 * 
		 * Here an example of how to run the logP calculation …
		 * http://134.67.216.45:9191/actorws/knime01/toxin/LogP/C=O
		 * 
		 * where the last item is the smiles None of this is well enough documented yet,
		 * but this should provide you an idea of what’s available. All are using the
		 * RESTful approach
		 * 
		 * 
		 */	
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MoleculeUtilities m=new MoleculeUtilities();

//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\aude";
//		String caslistpath=folder+"\\cas list need structure.txt";
//		String molFolder=folder+"\\mol files";
//		m.createMolFilesFrom_CAS_List(caslistpath, molFolder);
		
//		MoleculeUtilities.getDSSToxRecordFromDashboard("benzene");
		MoleculeUtilities.getDSSToxRecordFromDashboardQSARReady("benzene");

	}





	public static void createPhosphates(String sdfFilePath) {
		
		ArrayList<String>casList=new ArrayList<String>();
		ArrayList<String>nameList=new ArrayList<String>();
		
		casList.add("121-45-9");
		nameList.add("Trimethyl phosphite");
		
		casList.add("993-13-5");
		nameList.add("methyl phosphonic acid");

		casList.add("676-97-1");
		nameList.add("methyl phosphonic dichloride");
		
		casList.add("2857-97-8");
		nameList.add("Bromotrimethylsilane");
		
		casList.add("74-88-4");
		nameList.add("Methyl iodide");
		
		casList.add("67-56-1");
		nameList.add("Methanol");
		
		

		AtomContainerSet acs=createAtomContainerSetCASNameList(casList,nameList);
		System.out.println(acs.getAtomContainerCount());
		
		MolFileUtilities.saveAtomContainerSetToSDFFile(sdfFilePath, acs);
		
	}

}
