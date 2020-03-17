import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Test;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import AADashboard.Application.MySQL_DB;
import QSAR.validation2.InstanceUtilities;
import ToxPredictor.Application.CalculationParameters;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.WebTEST2;
import ToxPredictor.Application.WebTESTDBs;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.misc.MolFileUtilities;
import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;

public class DescriptorsTest {

	
	@Test
	public void runBadChemicals() throws Exception {
		
		String inputFolder="data";
		String outputFolder="test-results/bad chemicals";
		
		int endpoint=TESTConstants.numChoiceDescriptors;
		int method=-1;
		
		// Run on validation set
		File inFilePath = new File(inputFolder+File.separator+ "bad smiles.smi"); 
		File outFilePath = new File(outputFolder+File.separator+ "bad chemicals.txt");
		
		String outDir = outFilePath.getParent();
		if ( !Files.isDirectory(Paths.get(outDir)) )
			Files.createDirectories(Paths.get(outDir));
		
		WebTEST.go(inFilePath.toString(), outFilePath.toString(), endpoint, method);
				
		//Compounds with errors: 
		//2278-22-0
		
		
	}
	
	
	/**
	 * Generate descriptors using WebTEST for validated structures file
	 * 
	 * @throws Exception
	 */
	@Test
	public void runDescriptors() throws Exception  {
		
		String inputFolder="data/descriptors";
		String outputFolder=inputFolder;
		
		String endpoint=TESTConstants.ChoiceDescriptors;
		String method=TESTConstants.ChoiceNotApplicable;
		
		// Run on validation set
		String sdfPath = inputFolder+File.separator+ "ValidatedStructures2d.sdf";
		
		Set<WebReportType> wrt = WebReportType.getNone();
		
		File outFilePath = new File(outputFolder+File.separator+ "new descriptors.txt");
		String outDir = outFilePath.getParent();
		if ( !Files.isDirectory(Paths.get(outDir)) )
			Files.createDirectories(Paths.get(outDir));
				
		AtomContainerSet acs = WebTEST.LoadFromSDF(sdfPath);
		
		CalculationParameters params=new CalculationParameters(null, null, endpoint, method, wrt);
		
		WebTEST2.compressDescriptorsInDB=false;
		WebTEST2.usePreviousDescriptors=true;
		WebTEST2.DB_Path_Descriptors="databases/descriptors_2018_10_19.db";
		WebTEST2.go(acs, params);
		
		//call compareDescriptorFiles to check results:
//		this.compareDescriptorFiles();
	}
	
	/**
	 * Run a single chemical for testing
	 */
	void runSingleChemical()  {
		
//		String CAS="2278-50-4";
		String CAS="71-43-2";
		
		String endpoint = TESTConstants.ChoiceDescriptors;
		String method = "N/A";
		
//		String endpoint = TESTConstants.ChoiceViscosity;
//		String method = TESTConstants.ChoiceNearestNeighborMethod;
		
		try {
			
			
			//Load chemical from ChemistryDashboard URL:
			String strAC=MolFileUtilities.getAtomContainerStringFromDashboard(CAS);
			
			AtomContainerSet acs=WebTEST.LoadFromSDF(strAC);
			
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(0);
			ac.setProperty("CAS", CAS);
			

			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;

			WebTEST2.go(acs, new CalculationParameters(null, null, 
			        endpoint, method, WebReportType.getNone()));
		
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Run a single chemical for testing
	 */
	void runTautomers()  {
		
//	 	Tautomers:
		String CAS1="142-08-5";
		String CAS2="72762-00-6";

		
		//For following isomers- the 2d structures dont have the hydrogens specified in validated structures file
		// Thus the inchikeys come out the same!!!
//		String CAS1="103-30-0";//	PJANXHGTPQOBST-VAWYXSNFNA-N
//		String CAS2="588-59-0";
		
		//For these isomers they come out different because the H's are specified:
//		String CAS1="590-18-1";//cis-2-butene
//		String CAS2="624-64-6";//trans-2-butene
		
		
		String endpoint = TESTConstants.ChoiceDescriptors;
		String method = "N/A";
		
		MolFileUtilities molFileUtilities=new MolFileUtilities();
		
//		String endpoint = TESTConstants.ChoiceViscosity;
//		String method = TESTConstants.ChoiceNearestNeighborMethod;
		
		try {
			AtomContainerSet acs=new AtomContainerSet();
			
			//Load chemical from ChemistryDashboard URL:					
//			AtomContainerSet acs1=MolFileUtilities.getAtomContainerSetFromDashboard(CAS1);			
//			AtomContainer ac1=WebTEST2.prepareMolecule(-1,(AtomContainer)acs1.getAtomContainer(0));
//			acs.addAtomContainer(ac1);

			String filePath1="ValidatedStructures2d/"+CAS1+".mol";
			AtomContainer ac1=(AtomContainer)molFileUtilities.LoadChemicalFromMolFileInJar(filePath1);
			ac1=WebTEST2.prepareMolecule(-1,ac1);
			acs.addAtomContainer(ac1);

			String filePath2="ValidatedStructures2d/"+CAS2+".mol";
			AtomContainer ac2=(AtomContainer)molFileUtilities.LoadChemicalFromMolFileInJar(filePath2);
			ac2=WebTEST2.prepareMolecule(-1,ac2);
			acs.addAtomContainer(ac2);
			
			String strAC1=molFileUtilities.loadChemicalFromMolFileInJarAsString(filePath1);
			String strAC2=molFileUtilities.loadChemicalFromMolFileInJarAsString(filePath2);
			
			System.out.println(strAC1);
			System.out.println(strAC2);

//			AtomContainerSet acs2=MolFileUtilities.getAtomContainerSetFromDashboard(CAS2);
//			acs.addAtomContainer(acs2.getAtomContainer(0));

			
			try {
				String SMILES1 = CDKUtilities.generateSmiles(ac1);
				String SMILES2 = CDKUtilities.generateSmiles(ac2);
				
				String [] results1=CDKUtilities.generateInChiKey(ac1);
				String [] results2=CDKUtilities.generateInChiKey(ac2);
				
				System.out.println(CAS1+"\t"+SMILES1+"\t"+results1[0]);
				System.out.println(CAS2+"\t"+SMILES2+"\t"+results2[0]);
				
			} catch (Exception e) {
				e.printStackTrace();
			}


			WebTEST2.usePreviousDescriptors=true;
			WebTEST2.compressDescriptorsInDB=false;
			WebTEST2.DB_Path_Descriptors="databases/descriptors_tautomers.db";
			

			WebTEST2.go(acs, new CalculationParameters(null, null, 
			        endpoint, method, WebReportType.getNone()));
		
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	
	/**
	 * Compare descriptors generated by TEST4.2 and from Web-test
	 */
	void compareDescriptorFiles() {
		
		String folder="test-results/descriptors";
		String folder2="data/descriptors";
		
		String filepathOriginal=folder2+"/original descriptors.txt";

		String filepathNew=folder+"/descriptors.txt";
		String filepathNewCopy=folder+"/descriptors-Copy.txt";				

		String destPath=folder+"/descriptor discrepancies.txt";
		
		//create a copy so can compare while calcs are running:
		ToxPredictor.Utilities.Utilities.CopyFile(new File(filepathNew), new File(filepathNewCopy));
		
		wekalite.CSVLoader c=new CSVLoader();
		
		
		try {
			FileWriter fw=new FileWriter(destPath);

			fw.write("CAS\tDescriptorNumber\tDescriptorName\tValue1\tValue2\tDiff\r\n");
			
			String del="\t";
			Instances i1=c.getDataSetFromFileNoTox(filepathNewCopy, del);
			Instances i2=c.getDataSetFromFileNoTox(filepathOriginal, del);
			System.out.println("sets loaded");
			
			for (int i=0;i<i1.numInstances();i++) {
				
				Instance instance1=i1.instance(i);
				
				if (i2.instance(instance1.getName())==null) continue;
				
				Instance instance2=i2.instance(instance1.getName());
				
//				System.out.println(instance1.getName()+"\t"+instance1.value("XLOGP")+"\t"+instance2.value("XLOGP"));
				
				
				for (int j=0;j<instance1.numAttributes();j++) {
					double val1=instance1.value(j);
					double val2=instance2.value(j);
					
					double err=Math.abs(val1-val2);
					
//					if (err>1e-4 && instance1.attribute(j).indexOf("XLOGP")==-1) {
//						System.out.println(instance1.getName()+"\t"+j+"\t"+instance1.attribute(j)+"\t"+val1+"\t"+val2+"\t"+err);
//						break;
//					}
					
					DecimalFormat df=new DecimalFormat("0.000");
					
					if (err>1e-4) {
						System.out.println(instance1.getName()+"\t"+j+"\t"+instance1.attribute(j)+"\t"+val1+"\t"+val2+"\t"+df.format(err));
						fw.write(instance1.getName()+"\t"+j+"\t"+instance1.attribute(j)+"\t"+val1+"\t"+val2+"\t"+df.format(err)+"\r\n");
						fw.flush();
						break;
					}

				}
			}

			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void compareDescriptorValuesForChemicalWithDifferentPredictedValue() {		
	
//		ISAOCJYIOMOJEB-UHFFFAOYSA-N
//		ZFXYFBGIUFBOJW-UHFFFAOYSA-N
		
//		String CAS1="579-44-2";		
//		String CAS2="119-53-9";//isomer that was stored in database based on inchikey
		
		
		//For some reason, the TEST5.01 GUI gives different descriptor values than WebTEST or the old descriptors
		String CAS1="58-55-9";		
		String CAS2="58-55-9";//isomer that was stored in database based on inchikey

		String inchiKeyGUI=null;
		
		Instances instancesOriginal=null;
		JsonObject jo=null;
		
		
		try {
			String molFilePath="E:\\MyToxicity\\ToxRuns\\ToxRun_"+CAS1+"\\StructureData\\"+CAS1+".mol";
			AtomContainerSet acs=WebTEST2.LoadFromSDF(molFilePath);
			AtomContainer ac=(AtomContainer)acs.getAtomContainer(0);
			String [] results=CDKUtilities.generateInChiKey(ac);
			inchiKeyGUI=results[1];
			System.out.println(results[1]);
			

			String folder="data/descriptors";		
			String filepathOriginal=folder+"/original descriptors.txt";
			CSVLoader c=new CSVLoader();
			instancesOriginal=c.getDataSetFromFileNoTox(filepathOriginal,"\t");

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String filepath1="E:/MyToxicity/ToxRuns/ToxRun_"+CAS1+"/StructureData/2d_descriptors.txt";
		ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(filepath1);
		Hashtable<String,Double>htGUI=new Hashtable<>();

		//Now lets get value from descriptors db:
		jo=this.getDescriptorsAsJSON("databases/descriptors.db", CAS2);
		
//		System.out.println(jo==null);
		JsonObject nonFragmentDescriptors=jo.get("non fragment descriptors").getAsJsonObject();
		JsonObject fragmentDescriptors=jo.get("fragment descriptors").getAsJsonObject();
		JsonObject metadata=jo.get("metadata").getAsJsonObject();
		
		String inchiKeyWebTEST=metadata.get("InChiKey").getAsString();
		boolean startFragments=false;
		
		Instance iOriginal=instancesOriginal.instance(CAS1);
		
		for (int i=1;i<lines.size();i++) {
			String line=lines.get(i);
			String attribute=line.substring(0, line.indexOf("|"));
			double valGUI=Double.parseDouble(line.substring(line.indexOf("|")+1,line.length()));
						
			if (attribute.equals("As [+5 valence, one double bond]")) startFragments=true;
			double valWebTEST=-9999;					
			if (startFragments) valWebTEST=fragmentDescriptors.get(attribute).getAsDouble();	
			else valWebTEST=nonFragmentDescriptors.get(attribute).getAsDouble();
			
			double valOriginal=iOriginal.value(attribute);
			
			if (Math.abs(valGUI-valWebTEST)>1e-4) 			
				System.out.println(attribute+"\t"+valGUI+"\t"+valWebTEST+"\t"+valOriginal);
			
		}
		
		System.out.println(inchiKeyGUI+"\t"+inchiKeyWebTEST);
		
		
	}
	
	
	/**
	 * Compare descriptors generated by TEST4.2 and from Web-test database
	 */
	void compareDescriptorValuesToDatabase() {
		
		String folder="data/descriptors";
		
		String filepathOriginal=folder+"/original descriptors.txt";
		String destPath=folder+"/descriptor discrepancies.txt";
		
		String dbPath="databases/descriptors_2018_10_19.db";
		String filepathNew="data/descriptors/descriptors_2018_10_19.csv";		
		boolean generateNewInstanceFile=true;
						
		CSVLoader c=new CSVLoader();
		
		WebTEST2.compressDescriptorsInDB=false;
		
		//TODO need to speed up by converting the descriptors database into a csv file for fast loading by CSVLoader
		
		try {
			
			FileWriter fw=new FileWriter(destPath);

			fw.write("CAS\tDescriptorNumber\tDescriptorName\tValue1\tValue2\tDiff\r\n");
			
			String del="\t";
			
			Instances instancesOriginal=c.getDataSetFromFileNoTox(filepathOriginal, del);
			System.out.println("Original descriptor file loaded");
			String[] varArray2d = instancesOriginal.getAttributes();
//			Hashtable<String, Instance> htInstances = createInstancesFromDatabase(varArray2d,dbPath);
//			Hashtable<String, JsonObject> htInstances = getHashtableFromDatabase(dbPath);

			
			Instances instancesNew=null;
			
			if(generateNewInstanceFile) {
				instancesNew = getInstancesFromDatabase(dbPath,varArray2d);
				System.out.println(instancesNew.numAttributes()+"\t"+instancesNew.numInstances());			
				writeInstancesToFile(filepathNew, instancesNew);
				System.out.println("Database file loaded and converted to instances");
			} else {
				instancesNew=c.getDataSetFromFileNoTox(filepathNew, del);
			}

			System.out.println("New descriptor file loaded");
			
			for (int i=0;i<instancesOriginal.numInstances();i++) {
				
				Instance iOriginal=instancesOriginal.instance(i);
				String CAS=iOriginal.getName();

//				if (!htInstances.contains(CAS)) continue;
				
				Instance iNew=instancesNew.instance(CAS);
				
				if (iNew==null) continue;
				
//				Instance iNew =htInstances.get(CAS);
				
//				JsonObject jo=htInstances.get(CAS);				
//				JsonObject nonFragmentDescriptors=jo.get("non fragment descriptors").getAsJsonObject();
//				JsonObject fragmentDescriptors=jo.get("fragment descriptors").getAsJsonObject();
				
//				System.out.println(instance1.getName()+"\t"+instance1.value("XLOGP")+"\t"+instance2.value("XLOGP"));

//				boolean startFragments=false;
				
				for (int j=0;j<iOriginal.numAttributes();j++) {

					double val1=iOriginal.value(j);
					double val2=iNew.value(j);
					
					String attribute=iOriginal.attribute(j);
					
//					if (attribute.equals("As [+5 valence, one double bond]")) startFragments=true;
//					double val2=-9999;					
//					if (startFragments) val2=fragmentDescriptors.get(attribute).getAsDouble();	
//					else val2=nonFragmentDescriptors.get(attribute).getAsDouble();
					
					
//					double val2=(Double)ht.get(iOriginal.attribute(j));
					
					double err=Math.abs(val1-val2);
					
//					if (err>1e-4 && instance1.attribute(j).indexOf("XLOGP")==-1) {
//						System.out.println(instance1.getName()+"\t"+j+"\t"+instance1.attribute(j)+"\t"+val1+"\t"+val2+"\t"+err);
//						break;
//					}
					
					DecimalFormat df=new DecimalFormat("0.000");
					
					if (err>1e-4) {
//					if (err>1e-4 && !attribute.contains("XLOGP")) {
						System.out.println(iOriginal.getName()+"\t"+j+"\t"+iOriginal.attribute(j)+"\t"+val1+"\t"+val2+"\t"+df.format(err));
						fw.write(iOriginal.getName()+"\t"+j+"\t"+iOriginal.attribute(j)+"\t"+val1+"\t"+val2+"\t"+df.format(err)+"\r\n");
						fw.flush();
						break;
					}

				}
			}

			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void writeInstancesToFile(String filepathNew, Instances instancesNew) throws IOException {
		FileWriter fwInstances=new FileWriter(filepathNew);
		fwInstances.write("CAS\t"+instancesNew.getDescriptorNames()+"\r\n");
		
		for (int i=0;i<instancesNew.numInstances();i++) {
			Instance instance=instancesNew.instance(i);
			fwInstances.write(instance.getName()+"\t"+instance.getDescriptorsValues()+"\r\n");
		}
		fwInstances.flush();
		fwInstances.close();
	}


/**
 * Go through descriptor database and store as hashtable with CAS as key and JsonObject as value
 * 
 * @param dbPath
 * @return Hashtable with CAS as key and JsonObject of descriptor values as values
 * 
 */
	private Hashtable<String, JsonObject> getHashtableFromDatabase(String dbPath) {
//		Hashtable<String,Instance>htInstances=new Hashtable<>();
		Hashtable<String,JsonObject>htInstances=new Hashtable<>();
		
		InstanceUtilities iu = new InstanceUtilities();

		Gson gson = new Gson();
		
		try {
			
			Connection connDescriptors=MySQL_DB.getConnection(dbPath);
			Statement stat=connDescriptors.createStatement();

			String query="select CAS, Descriptors from Descriptors;";
//				System.out.println(query);
			ResultSet rs = stat.executeQuery(query);
//				ResultSetMetaData rsmd = rs.getMetaData();
			
			//Create instances from descriptors SQLite DB:
			
			int count=0;
			
			while (rs.next()) {				
				count++;					
				if (count%1000==0) System.out.println(count);					
				String CAS=rs.getString(1);
				
				String Descriptors=rs.getString(2);
//				System.out.println(CAS+"\t"+Descriptors);
				
				if (Descriptors.equals("null")) continue;
				
//				System.out.println(CAS+"\t"+Descriptors.length());
				
				long t1=System.currentTimeMillis();
				
				//Converting to Instance takes 8 ms- keeping as Json takes 1 ms
//				DescriptorData dd=DescriptorData.loadFromJSON(Descriptors);
//				Hashtable<String,Object> ht = dd.CreateDataHashtable("Tox", true, true, false, false, false);
//				Instances instancesNew =iu.createInstances(ht, varArray2d);
//				Instance iNew =instancesNew.firstInstance();
				
				JsonObject jo = gson.fromJson(Descriptors,JsonObject.class);
				htInstances.put(CAS, jo);
				long t2=System.currentTimeMillis();
//				System.out.println(CAS+"\t"+(t2-t1)+" ms");
			
			}
			
			
//				this.printResultSet(rs);
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		return htInstances;
	}
	
	
	private JsonObject getDescriptorsAsJSON(String dbPath,String CAS) {
		Gson gson = new Gson();
		
		try {
			Connection connDescriptors=MySQL_DB.getConnection(dbPath);
			Statement stat=connDescriptors.createStatement();

			String query="select CAS, Descriptors from Descriptors where CAS=\""+CAS+"\";";
			
//			System.out.println(query);
						
			ResultSet rs = stat.executeQuery(query);
			
			while (rs.next()) {				
				
				String currentCAS=rs.getString(1);
				String Descriptors=rs.getString(2);
				
//				System.out.println(Descriptors);
				
				if (CAS.equals(currentCAS)) {
					return gson.fromJson(Descriptors,JsonObject.class);
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		return null;
	}
	
	/**
	 * Go through descriptor database and store as hashtable with CAS as key and JsonObject as value
	 * 
	 * @param dbPath
	 * @return Hashtable with CAS as key and JsonObject of descriptor values as values
	 * 
	 */
		private Instances getInstancesFromDatabase(String dbPath,String [] attributes) {
//			Hashtable<String,Instance>htInstances=new Hashtable<>();
			Instances instances=null;
			
			InstanceUtilities iu = new InstanceUtilities();

			Gson gson = new Gson();
			
			try {
				
				Connection connDescriptors=MySQL_DB.getConnection(dbPath);
				Statement stat=connDescriptors.createStatement();

				String query="select CAS, Descriptors from Descriptors;";
//					System.out.println(query);
				ResultSet rs = stat.executeQuery(query);
//					ResultSetMetaData rsmd = rs.getMetaData();
				
				//Create instances from descriptors SQLite DB:
				
				int count=0;
				
				while (rs.next()) {				
					count++;					
					
//					if (count==10) break;
					
					if (count%100==0) System.out.println(count);
					
					String CAS=rs.getString(1);
					
					String Descriptors=rs.getString(2);
//					System.out.println(CAS+"\t"+Descriptors);
					
					if (Descriptors.equals("null")) continue;
					
//					System.out.println(CAS+"\t"+Descriptors.length());
					
					long t1=System.currentTimeMillis();
					
					
//					System.out.println(CAS);
					
					//Converting to Instance takes 8 ms- keeping as Json takes 1 ms
					DescriptorData dd=DescriptorData.loadFromJSON(Descriptors);
					
					if (!dd.Error.equals("OK")) continue;
					
					Hashtable<String,Object> ht = dd.CreateDataHashtable("Tox", true, true, false, false, false);
					
					Instances instancesNew =iu.createInstances(ht, attributes);
					Instance iNew =instancesNew.firstInstance();
					
					if (instances==null) instances=new Instances(iNew);
					else instances.addInstance(iNew);
					
					long t2=System.currentTimeMillis();
//					System.out.println(CAS+"\t"+(t2-t1)+" ms");
				
				}
				
				
//					this.printResultSet(rs);
			} catch (Exception ex) {
				ex.printStackTrace();
				
			}
			return instances;
		}
	
	
	void lookAtDescriptorsinDB(String CAS,boolean compressed) {
		try {
			Connection connDescriptors=MySQL_DB.getConnection("databases/descriptors.db");
			Statement statDescriptors=connDescriptors.createStatement();
			DescriptorData dd=WebTESTDBs.getDescriptors(statDescriptors, "CAS","CAS",compressed);

			System.out.println(dd.to_JSON_String(true));
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	

	public static void main(String[] args) {
		DescriptorsTest dt=new DescriptorsTest();
//		
//		dt.runSingleChemical();
//		dt.compareDescriptorFiles();//		
//		dt.compareDescriptorValuesForChemicalWithDifferentPredictedValue();
		
		try {
//			dt.runBadChemicals();
			
//			dt.runDescriptors();
//			dt.compareDescriptorValuesToDatabase();
			
			dt.runTautomers();
			
//			dt.lookAtDescriptorsinDB("71-43-2");
//			dt.lookAtDescriptorsinDB("2278-50-4");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
}

