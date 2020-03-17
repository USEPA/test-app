package ToxPredictor.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmilesParser;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Database.ChemistryDashboardRecord;
import ToxPredictor.Utilities.CDKUtilities;

public class GetChemistryDashboardIDs {

	private static final Logger logger = LogManager.getLogger(GetChemistryDashboardIDs.class);
	
	int loadFromSDF(File file,FileWriter fw) {

		
		int counter = 0;
		
		try {

			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(file),DefaultChemObjectBuilder.getInstance());

//			while (mr.hasNext()) {
			for (int i=0;i<50000;i++) {
					
				counter++;
				
				
//				System.out.println(counter);
				
				AtomContainer m=null;
				
				try {
					m = (AtomContainer)mr.next();

					if (m==null) break;
					
					
					
//					if (m==null || m.getAtomCount()==0) break;

					String gsid=m.getProperty("gsid");
					String dsstox_substance_id=m.getProperty("dsstox_substance_id");
					String casrn=m.getProperty("casrn");
					String preferred_name=m.getProperty("preferred_name");
					String cid=m.getProperty("cid");
					String dsstox_compound_id=m.getProperty("dsstox_compound_id");
					String Salt_Solvent=m.getProperty("Salt_Solvent");
					String Salt_Solvent_ID=m.getProperty("Salt_Solvent_ID");
					String Canonical_QSARr=m.getProperty("Canonical_QSARr");
					String InChI_Code_QSARr=m.getProperty("InChI_Code_QSARr");
					String InChI_Key_QSARr=m.getProperty("InChI_Key_QSARr");

					String newLine=casrn+"\t"+gsid+"\t"+dsstox_substance_id+"\t"+preferred_name+"\t"+cid+
							"\t"+dsstox_compound_id+"\t"+Salt_Solvent+"\t"+Salt_Solvent_ID+"\t"+
							Canonical_QSARr+"\t"+InChI_Code_QSARr+"\t"+InChI_Key_QSARr;
					
					
					System.out.println(counter+"\t"+casrn);
					
					fw.write(newLine+"\r\n");
					fw.flush();
					
					
				} catch (Exception e) {
					break;
				}
				
				
				
			}// end while true;
			
//			System.out.println(counter);

			mr.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
		

	}
	
	/**
	 * Need to go line by line because some chemicals have "?" for coordinates!!!
	 * @param file
	 * @param fw
	 * @return
	 */
	
int loadFromSDF2(File file,FileWriter fw) {

		
		int counter = 0;
		
		try {

			BufferedReader br=new BufferedReader(new FileReader(file));

//			while (mr.hasNext()) {
			for (int i=0;i<50000;i++) {

				if (i%1000 == 0) {
					System.out.println(i);
				}
				
				String Line="";

				while (Line.indexOf("> <gsid>")==-1) {
					Line=br.readLine();
					
					if (Line==null) break;
				}
				
				if (Line==null) break;

				Vector<String>lines=new Vector<String>();

				lines.add(Line);

				while (Line.indexOf("$$$$")==-1) {
					Line=br.readLine();
					lines.add(Line);
				}

				Hashtable <String,String>ht=new Hashtable<String,String>();

				while (lines.size()>0) {
					String field=lines.remove(0).replace("> <", "").replace(">", "");

					if (lines.size()==0) break;

					String value=lines.remove(0);
					
					ht.put(field, value);
					
					if (lines.size()==0) break;
					
					lines.remove(0);
					
					if (lines.size()==0) break;
					

//					System.out.println(field+"\t"+value);
				}

				String gsid=ht.get("gsid");
				String dsstox_substance_id=ht.get("dsstox_substance_id");
				String casrn=ht.get("casrn");
				String preferred_name=ht.get("preferred_name");
				String cid=ht.get("cid");
				String dsstox_compound_id=ht.get("dsstox_compound_id");
				String Salt_Solvent=ht.get("Salt_Solvent");
				String Salt_Solvent_ID=ht.get("Salt_Solvent_ID");
				String Canonical_QSARr=ht.get("Canonical_QSARr");
				String InChI_Code_QSARr=ht.get("InChI_Code_QSARr");
				String InChI_Key_QSARr=ht.get("InChI_Key_QSARr");


				String newLine=casrn+"\t"+gsid+"\t"+dsstox_substance_id+"\t"+preferred_name+"\t"+cid+
						"\t"+dsstox_compound_id+"\t"+Salt_Solvent+"\t"+Salt_Solvent_ID+"\t"+
						Canonical_QSARr+"\t"+InChI_Code_QSARr+"\t"+InChI_Key_QSARr;


//				System.out.println(counter+"\t"+casrn);

				fw.write(newLine+"\r\n");
				fw.flush();
				
				counter++;


			}// end while true;
			
//			System.out.println(counter);

			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return counter;
		

	}

	
	void createIDTextFile () {

		String folderPath="C:/Users/TMARTI02/OneDrive - Environmental Protection Agency (EPA)/CompTox2/NCCT QSAR ready";


		File Folder=new File(folderPath);

		File [] files=Folder.listFiles();


		try {

			FileWriter fw=new FileWriter("data/NCCT_ID_data.txt");
			
			String header="casrn\tgsid\tdsstox_substance_id\tpreferred_name\tcid\t"+
			"dsstox_compound_id\tSalt_Solvent\tSalt_Solvent_ID\tCanonical_QSARr\tInChI_Code_QSARr\t"+
					"InChI_Key_QSARr";
			fw.write(header+"\r\n");

			for (int i=0;i<files.length;i++) {

				File  filei=files[i];

				if (files[i].getName().indexOf("sdf")==-1) continue;

//				int count=this.loadFromSDF(filei,fw);
				int count=this.loadFromSDF2(filei,fw);

				System.out.println(files[i].getName()+"\t"+count);

			}

			fw.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	//uses class to store info, kinda slow- takes 15 seconds
	public static Hashtable<String,ChemistryDashboardRecord>getGSIDLookupTable2(String filepath) {
		Hashtable<String,ChemistryDashboardRecord>ht=new Hashtable<String,ChemistryDashboardRecord>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();
			
			LinkedList<String>hlist=ToxPredictor.Utilities.Utilities.Parse3(header, "\t");
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;

				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				
				ChemistryDashboardRecord cdr=new ChemistryDashboardRecord();
				
				for (int i=0;i<hlist.size();i++) {
					Field myField = cdr.getClass().getField(hlist.get(i));
					myField.set(cdr, list.get(i));
				}
				
				
				ht.put(cdr.casrn, cdr);
				counter++;
			}
			
//			System.out.println(counter);
			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;
		
	}
	
	/**
	 * Store hashtable with CAS as key and gsid and DSSTOX SID as tab delimited string
	 * @param filepath
	 * @return
	 */
	public static Hashtable<String,String []>getGSIDLookupTable3(String filepath) {
		Hashtable<String,String []>ht=new Hashtable<String,String []>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();
			
			LinkedList<String>hlist=ToxPredictor.Utilities.Utilities.Parse3(header, "\t");
			
			int colCAS=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "casrn");
			int colGSID=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "gsid");
			int col_dsstox_substance_id=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "dsstox_substance_id");
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;

				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				
				String CAS=list.get(colCAS);
				String gsid=list.get(colGSID);
				String dsstox_substance_id=list.get(col_dsstox_substance_id);
				
//				ChemistryDashboardRecord cdr=new ChemistryDashboardRecord();
//				cdr.casrn=CAS;
//				cdr.gsid=gsid;
//				cdr.dsstox_substance_id=dsstox_substance_id;
				
				String []results= {gsid,dsstox_substance_id};
				
//				ht.put(CAS, gsid+"\t"+dsstox_substance_id);
				ht.put(CAS, results);
				counter++;
			}
			
//			System.out.println(counter);
			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;
		
	}
	
	/**
	 * Store hashtable with CAS as key and ChemistryDashboardRecord as value to store gsid and DSSTOXSID
	 * 
	 * @param filepath
	 * @return
	 */
	public static Hashtable<String,ChemistryDashboardRecord>getGSIDLookupTable4(String filepath) {
		Hashtable<String,ChemistryDashboardRecord>ht=new Hashtable<String,ChemistryDashboardRecord>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();
			
			LinkedList<String>hlist=ToxPredictor.Utilities.Utilities.Parse3(header, "\t");
			
			int colCAS=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "casrn");
			int colGSID=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "gsid");
			int col_dsstox_substance_id=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "dsstox_substance_id");
			int col_dsstox_compound_id=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "dsstox_compound_id");
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;

				LinkedList<String>list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				
				ChemistryDashboardRecord cdr=new ChemistryDashboardRecord();
				
				String CAS=list.get(colCAS);
				cdr.gsid=list.get(colGSID);
				cdr.dsstox_substance_id=list.get(col_dsstox_substance_id);
				cdr.dsstox_compound_id=list.get(col_dsstox_compound_id);
				
				ht.put(CAS, cdr);
				counter++;
			}
			
//			System.out.println(counter);
			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;
		
	}
	
	
	
	/**
	 * Create sqlite database table with CAS as primary key (needs unique values for this to work)
	 * 
	 * Can search by any field in table but CAS is much faster since primary key
	 * 
	 * See http://sqlitebrowser.org/ for user friendly sqlite GUI to look at the database once it's created
	 * 
	 * @param filepath
	 * @return
	 */
	public static void createNCCTLookupTable(String textFilePath, String databasePath) {

		try {

			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
			Statement stat = conn.createStatement();

			conn.setAutoCommit(true);
			
			String tableName = "NCCT_ID";

			String[] fields = { "casrn", "gsid", "dsstox_substance_id", "preferred_name", "cid", "dsstox_compound_id",
					"Salt_Solvent", "Salt_Solvent_ID", "Canonical_QSARr", "InChI_Code_QSARr", "InChI_Key_QSARr" };


			stat.executeUpdate("drop table if exists "+tableName+";");
			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
			
			
//			MySQL_DB.create_table(stat, tableName, fields);
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table(stat, tableName, fields,"casrn");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);

			BufferedReader br = new BufferedReader(new FileReader(textFilePath));

			String header = br.readLine();

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fields.length; i++) {
				s += "?";
				if (i < fields.length)
					s += ",";
			}
			s += ");";


			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			

			while (true) {
				String Line = br.readLine();

				counter++;
//				if (counter==2000) break;

				if (Line == null)
					break;

				if (!Line.equals("")) {

					LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");

					// System.out.println(list.size()+"\t"+fields.length);

					for (int i = 0; i < list.size(); i++) {
						prep.setString(i + 1, list.get(i));
						// System.out.println((i+1)+"\t"+list.get(i));
					}

					prep.addBatch();
				}

				if (counter % 1000 == 0) {
					// System.out.println(counter);
					prep.executeBatch();
				}

			}

			int[] count = prep.executeBatch();// do what's left

			conn.setAutoCommit(true);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
		

	/**
	 * Create sqlite database table with CAS as primary key (needs unique values for this to work)
	 * 
	 * Can search by any field in table but CAS is much faster since primary key
	 * 
	 * See http://sqlitebrowser.org/ for user friendly sqlite GUI to look at the database once it's created
	 * 
	 * This version uses "qsar-ready_smiles_v2_2019_02_19.txt" as the source:
	 * dtxsid,casrn,dtxcid,qsar-ready_dtxcid,qsar-ready_smiles
	 * 
	 * 
	 * 
	 * @param filepath
	 * @return
	 */
	public static void createNCCTLookupTable_2019_02_19(String textFilePath, String databasePath) {

		try {
//			System.out.println("here");
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
			Statement stat = conn.createStatement();
			conn.setAutoCommit(true);
						
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			
			String tableName = "NCCT_ID";			
			String [] fields= {"dsstox_substance_id","casrn","dsstox_compound_id","qsar_ready_dtxcid","qsar_ready_smiles","InChI_Code", "InChI_Key"};

			
//			String[] fields = { "casrn", "gsid", "dsstox_substance_id", "preferred_name", "cid", "dsstox_compound_id",
//					"Salt_Solvent", "Salt_Solvent_ID", "Canonical_QSARr", "InChI_Code_QSARr", "InChI_Key_QSARr" };

			//Fields we are missing:
//			String[] fields = { "gsid", "preferred_name", "InChI_Code_QSARr", "InChI_Key_QSARr" };

			stat.executeUpdate("drop table if exists "+tableName+";");
			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
						
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table(stat, tableName, fields,"casrn");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);

			BufferedReader br = new BufferedReader(new FileReader(textFilePath));

			String header = br.readLine();

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fields.length; i++) {
				s += "?";
				if (i < fields.length)
					s += ",";
			}
			s += ");";


			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			

			while (true) {
				String Line = br.readLine();

				counter++;
//				if (counter==10000) break;

				if (Line == null)
					break;

				if (!Line.equals("")) {

					LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse3(Line, ",");
					
					String smiles=list.get(list.size()-1);
//					System.out.println(smiles);
					
					if (counter%1000==0) {
						System.out.println(counter);
					}
					

					try {
					
						AtomContainer molecule = (AtomContainer)sp.parseSmiles(smiles);					
						String [] result=CDKUtilities.generateInChiKey(molecule);
						String InChi=result[0];
						String InChiKey=result[1];
						list.add(InChi);
						list.add(InChiKey);
					
					} catch (InvalidSmilesException ex) {
						list.add("");
						list.add("");
					}

					
					// System.out.println(list.size()+"\t"+fields.length);

					for (int i = 0; i < list.size(); i++) {
						prep.setString(i + 1, list.get(i));
						// System.out.println((i+1)+"\t"+list.get(i));
					}

					prep.addBatch();
				}

				if (counter % 1000 == 0) {
					// System.out.println(counter);
					prep.executeBatch();
				}

			}
			

			int[] count = prep.executeBatch();// do what's left

			conn.setAutoCommit(true);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

			
	
	public static Hashtable<String,String>getGSIDLookupTable(String filepath) {
		Hashtable<String,String>ht=new Hashtable<String,String>();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String header=br.readLine();
			
			int colCAS=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "casrn");
			int colGSID=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "gsid");
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				String CAS=ToxPredictor.Utilities.Utilities.RetrieveField(Line, "\t", colCAS);
				String gsid=ToxPredictor.Utilities.Utilities.RetrieveField(Line, "\t", colGSID);
				
				ht.put(CAS, gsid);
				
				counter++;
						
			}
			
//			System.out.println(counter);
			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return ht;
		
	}
	
	void getCASListInMyDataSets() {
		
		File folder=new File("data");
		
		File [] folders=folder.listFiles();
		
		Vector<String>listCAS=new Vector<String>();
		
		for (int i=0;i<folders.length;i++) {
			
			File folderi=folders[i];
			
			if (!folderi.isDirectory()) {
				continue;
			}
			
//			System.out.println(folderi.getName());

			
			File [] folders2=folderi.listFiles();
			
			for (int j=0;j<folders2.length;j++) {
				File filej=folders2[j];
				
				if (filej.getName().indexOf("predictions.txt")==-1) continue;
				
				try {
					
					BufferedReader br=new BufferedReader(new FileReader(filej));
					
					br.readLine();//header
					
					while (true) {
						String Line=br.readLine();
						
						if (Line==null) break;
						
						String CAS=Line.substring(0, Line.indexOf("\t"));
						
						if (!listCAS.contains(CAS)) listCAS.add(CAS);
						
					}
					
					
					br.close();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
				
				
			}// end loop over files in endpoint folder
		}// end loop over endpoint folders
		
//		System.out.println(listCAS.size());
		
		Collections.sort(listCAS);
		
		
//		for (int i=0;i<listCAS.size();i++) {
//			System.out.println(listCAS.get(i));
//		}
		
		Hashtable<String,ChemistryDashboardRecord>ht=getGSIDLookupTable4("data/NCCT_ID_data.txt");
		int count=0;
		
		for (int i=0;i<listCAS.size();i++) {
			if (ht.get(listCAS.get(i))==null) {
//				System.out.println(++count+"\t"+listCAS.get(i)+"\tmissing");
				
				String CAS=listCAS.get((i));
				System.out.println(CAS);
				
				try {
				
					String URL="http://comptox.ag.epa.gov/dashboard/dsstoxdb/results?utf8=%E2%9C%93&search="+CAS;
				
					String strFolder="todd/Chemistry Dashboard Records";
					
					File destFile=new File(strFolder+"/"+CAS+".html");
					if (destFile.exists()) continue;
					
					System.out.println(URL);
					
					this.downloadFile(CAS, URL, strFolder);

//					Thread.sleep(3000);// wait so dont get locked out

				} catch (Exception ex) {
					ex.printStackTrace();
				}
//				
				count++;
			} 
		}
		System.out.println(count);
		
	}
	
	void downloadFile(String CAS,String URL,String destFolder) {
		
		try {
			
			java.net.URL myURL = new java.net.URL(URL);
			
			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));
			
			FileWriter fw=new FileWriter(destFolder+"/"+CAS+".html");
			
			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
//				System.out.println(counter+" " +Line);
				
				fw.write(Line+"\r\n");
				fw.flush();
				
				counter++;
			}
			
			br.close();
			fw.close();
		
		} catch (FileNotFoundException ex1) {
			System.out.println("file not found");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void getIDs() {

		File folder=new File("todd/Chemistry Dashboard Records");


		File [] files=folder.listFiles();


		try {

			FileWriter fw=new FileWriter("data/NCCTRecords for missing TEST compounds.txt");
			FileWriter fw2=new FileWriter("data/missing TEST compounds.txt");

			for (int i=0;i<files.length;i++) {
				
				if (i%100==0) {
					System.out.println(i);
				}

				BufferedReader br=new BufferedReader(new FileReader(files[i]));

				String Line="";

				while (Line.indexOf("data-dtxsid=\"")==-1) {
					Line=br.readLine();
					if (Line==null) {
						break;
					}
				}
				if (Line==null) {
					String name=files[i].getName();
					
					name=name.substring(0,name.indexOf("."));
					
					fw2.write(name+"\r\n");
					fw2.flush();
					continue;
				}

				while (Line.indexOf("|")==-1) {

					Line+=br.readLine();

					if (Line==null) {
						break;
					}
				}

				if (Line==null) {
					System.out.println("| not found!!!");
					continue;
				}


				String name=this.getVal(Line,"data-label");
				String DTXSID=this.getVal(Line, "data-dtxsid");

				//		        <small id="casrn-subtitle" data-cas="100-45-8" data-gsid="861706" data-cid="810592" data-comp_id="DTXCID70810592">


				String CAS=this.getVal(Line, "data-cas");
				String gsid=this.getVal(Line, "data-gsid");
				String cid=this.getVal(Line, "data-cid");
				String DTXCID=this.getVal(Line, "data-comp_id");


				fw.write(CAS+"\t"+gsid+"\t"+DTXSID+"\t"+name+"\t"+cid+"\t"+DTXCID+"\r\n");
				fw.flush();
				
				//  System.out.println(Line);
				//	casrn	gsid	dsstox_substance_id	preferred_name	cid	dsstox_compound_id
				//	System.out.println(DTXSID+"\t"+name);

				br.close();



			}

			fw.close();
			fw2.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	String getVal(String Line,String field) {
		String seek=field+"=\"";
		String val=Line.substring(Line.indexOf(seek)+seek.length(), Line.length());
		val=val.substring(0,val.indexOf("\""));
		
		return val;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		GetChemistryDashboardIDs g=new GetChemistryDashboardIDs();
		
		
		
//		g.createIDTextFile();
		
		
//		Hashtable<String,String>ht=g.getGSIDLookupTable("data/NCCT_ID_data.txt");
//		System.out.println(ht.get("71-43-2"));

		//********************************************************************************

//		long t1=System.currentTimeMillis();
////		
		String CAS="75-25-2";
////		
//		Hashtable<String,ChemistryDashboardRecord>ht=g.getGSIDLookupTable4("data/NCCT_ID_data.txt");
//		
//		for (int i=0;i<1000;i++) {
//			ChemistryDashboardRecord cdr=ht.get(CAS);
//		}
////		ChemistryDashboardRecord cdr=ht.get(CAS);
////		System.out.println(CAS+"\t"+cdr.gsid+"\t"+cdr.dsstox_substance_id);
////
//		long t2=System.currentTimeMillis();
//		System.out.println((double)(t2-t1)/1000.0+ " seconds");

		//********************************************************************************
		
//		Hashtable<String,String[]>ht=g.getGSIDLookupTable3("data/NCCT_ID_data.txt");
//		String [] results=ht.get(CAS);
//		System.out.println(results[0]);

		
//		g.getCASListInMyDataSets();
//		g.getIDs();
		
		
		//********************************************************************************
//		long t1=System.currentTimeMillis();
//		String databasePath="todd/temp.db";
//		String databasePath = "todd/results NCCT/TEST_Results_simple.db";
//		String databasePath = "data/NCCT_ID.db";
//		g.createNCCTLookupTable("data/z NCCT_ID/NCCT_ID_data_no_duplicates.txt",databasePath);
		
		
		String databasePath = "databases/NCCT_ID_2019_02_19.db";
		g.createNCCTLookupTable_2019_02_19("data/z NCCT_ID/qsar-ready_smiles_v2_2019_02_19.txt",databasePath);
		
		
//		Statement stat=MySQL_DB.getStatement(databasePath);
//		ChemistryDashboardRecord cdr=g.lookupDashboardRecord(stat, "casrn", CAS);
//		System.out.println(CAS+"\t"+cdr.gsid+"\t"+cdr.dsstox_substance_id);

		
//		long t1=System.currentTimeMillis();
//		for (int i=1;i<=100;i++) {
//			ChemistryDashboardRecord cdr=g.lookupDashboardRecord(stat, "casrn", CAS);
//		}
//		
//		long t2=System.currentTimeMillis();
//		
//		System.out.println((double)(t2-t1)/1000.0+ " seconds");
		
				

	}

}
