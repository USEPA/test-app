package AADashboard.Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

//import AADashboard.Parse.Parse;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.DownloadChemidplusData;
import ToxPredictor.Utilities.IndigoUtilities;
import gov.epa.api.Chemical;
import gov.epa.api.FlatFileRecord;
import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;


/**
 * Class to store AADashboard database staticly
 * 
 * TODO- is there a way to avoid using a second class and still store in static fashion?
 * 
 * @author TMARTI02
 *
 */
public class CreateGHS_Database  {
    
	public static Chemical getChemicalFromRecords(Statement stat,String CAS) {
		
		Chemical chemical=new Chemical();
		
		ArrayList<FlatFileRecord>array=getRecords(stat,CAS,"HazardRecords");
		if (array.size()==0) return null;
		
		FlatFileRecord r0=array.get(0);
		
		chemical.CAS=r0.CAS;
		chemical.name=r0.name;
		
		for (FlatFileRecord f:array) {
			Score score=chemical.getScore(f.hazard_name);
			ScoreRecord sr=f.getScoreRecord();
			score.records.add(sr);
		}
		return chemical;
	}
	
	
	public static Chemical getChemicalFromRecordsUsingPrimaryKey(Statement stat,String CAS) {
		
		Chemical chemical=new Chemical();
		
		ArrayList<FlatFileRecord>array=getRecordsUsingPrimaryKey(stat,CAS,"HazardRecords");
		if (array.size()==0) return null;
		
		FlatFileRecord r0=array.get(0);
		
		chemical.CAS=r0.CAS;
		chemical.name=r0.name;
		
		for (FlatFileRecord f:array) {
			Score score=chemical.getScore(f.hazard_name);
			ScoreRecord sr=f.getScoreRecord();
			score.records.add(sr);
		}
		return chemical;
	}
	
//	public static Chemical getChemicalFromRecords(Statement stat,int CAS) {
//		
//		Chemical chemical=new Chemical();
//		
//		ArrayList<FlatFileRecord>array=getRecords(stat,CAS,"HazardRecords");
//		if (array.size()==0) return null;
//		
//		FlatFileRecord r0=array.get(0);
//		
//		chemical.CAS=r0.CAS;
//		chemical.name=r0.name;
//		
//		for (FlatFileRecord f:array) {
//			Score score=chemical.getScore(f.hazard_name);
//			ScoreRecord sr=f.getScoreRecord();
//			score.records.add(sr);
//		}
//		return chemical;
//	}

	public static ResultSet getRecords(Statement stat,String tableName,String keyField,String keyValue) {
		 ResultSet rs=MySQL_DB.getRecords(stat, tableName, keyField, keyValue);
		 return rs;
	}
	
	/***
	 * In this method, it is assumed all of the records are stored in one field and retrieve by primary key
	 * 
	 * @param stat
	 * @param CAS
	 * @param tableName
	 * @return
	 */
	public static ArrayList<FlatFileRecord> getRecordsUsingPrimaryKey(Statement stat,String CAS,String tableName) {

		ArrayList<FlatFileRecord>array=new ArrayList<>();

		long t1=System.currentTimeMillis();
		ResultSet rs=MySQL_DB.getRecords(stat, tableName, "CAS", CAS);
		long t2=System.currentTimeMillis();

		//    	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");

		try {
			if (rs.next()) {
				String lines=rs.getString(2);

				String [] records=lines.split("\r\n");
				
				for (String record:records) {
					FlatFileRecord f=FlatFileRecord.createFlatFileRecord(record);
					//				 System.out.println(f.toString());
					array.add(f);
				}
				
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return array;

	}
	
	
	public static ArrayList<FlatFileRecord> getRecords(Statement stat,String CAS,String tableName) {
		
		ArrayList<FlatFileRecord>array=new ArrayList<>();
		
		long t1=System.currentTimeMillis();
    	ResultSet rs=MySQL_DB.getRecords(stat, tableName, "CAS", CAS);
    	long t2=System.currentTimeMillis();
    	 
//    	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");
    	 
    	 
		 try {
			 while (rs.next()) {
				 FlatFileRecord f=createFlatFileRecord(rs);
				 if (f.note!=null) f.note=f.note.replace("<br><br><br>", "<br>");
				 if (f.note!=null) f.note=f.note.replace("<br><br>", "<br>");
				 
//				 System.out.println(f.toString());
				 array.add(f);
			 }
			 
		 } catch (Exception ex) {
			 ex.printStackTrace();
		 }
		 return array;
		 
	}
	
//public static ArrayList<FlatFileRecord> getRecords(Statement stat,int CAS,String tableName) {
//		
//		ArrayList<FlatFileRecord>array=new ArrayList<>();
//		
//		long t1=System.currentTimeMillis();
//    	ResultSet rs=MySQL_DB.getToxicityRecord(stat, tableName, "CAS", CAS);
//    	long t2=System.currentTimeMillis();
//    	 
////    	 System.out.println("time to pull AA records= "+(t2-t1)+" milliseconds");
//    	 
//    	 
//		 try {
//			 while (rs.next()) {
//				 FlatFileRecord f=createFlatFileRecord(rs);
////				 System.out.println(f.toString());
//				 array.add(f);
//			 }
//			 
//		 } catch (Exception ex) {
//			 ex.printStackTrace();
//		 }
//		 return array;
//		 
//	}
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
	public static void createDatabase(String textFilePath,String del,String tableName,String [] fieldNames) {

		try {
			System.out.println("Creating AA dashboard SQlite table");

			Connection conn= MySQL_DB.getConnection(AADashboard.DB_Path_AA_Dashboard_Records);
			Statement stat = MySQL_DB.getStatement(conn);
			
			conn.setAutoCommit(true);
			
			
			stat.executeUpdate("drop table if exists "+tableName+";");
			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
			
//			MySQL_DB.create_table(stat, tableName, fields);
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table_key_with_duplicates(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);

			BufferedReader br = new BufferedReader(new FileReader(textFilePath));

			String header = br.readLine();

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";


			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			

			while (true) {
				String Line = br.readLine();

				counter++;
				
//				if (counter==100) break;

				if (Line == null)
					break;

				if (!Line.isEmpty()) {

					LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse(Line, del);
					
					if (list.size()!=fieldNames.length) {
						System.out.println("*wrong number of values: "+Line);
					}

//					 System.out.println(Line);

					for (int i = 0; i < list.size(); i++) {
						prep.setString(i + 1, list.get(i));
//						 System.out.println((i+1)+"\t"+list.get(i));
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
						
			String sqlAddIndex="CREATE INDEX idx_CAS ON "+tableName+" (CAS)";
			stat.executeUpdate(sqlAddIndex);			

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
	 * @param filepath
	 * @return
	 */
	public static void createDatabaseWithPrimaryKey(String textFilePath,String dbPath,String del,String tableName,String [] fieldNames) {

		try {
			System.out.println("Creating AA dashboard SQlite table");

			Connection conn= MySQL_DB.getConnection(dbPath);
			Statement stat = MySQL_DB.getStatement(conn);
			
			conn.setAutoCommit(true);
			
			
			stat.executeUpdate("drop table if exists "+tableName+";");
			 
			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
			
//			MySQL_DB.create_table(stat, tableName, fields);
			
			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
			MySQL_DB.create_table(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!

			conn.setAutoCommit(false);
			
			ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(textFilePath);
			String header=lines.remove(0);
			Collections.sort(lines);

			String s = "insert into " + tableName + " values (";

			for (int i = 1; i <= fieldNames.length; i++) {
				s += "?";
				if (i < fieldNames.length)
					s += ",";
			}
			s += ");";


			int counter = 0;

			PreparedStatement prep = conn.prepareStatement(s);
			
			String CAS="";
			
			String records="";
			
			int count=0;
			
			for (String Line:lines) {
//				System.out.println(Line);
				
				String currentCAS=Line.substring(0,Line.indexOf(del));
				
				if (!CAS.equals(currentCAS)) {
					
					if (!CAS.isEmpty()) { 
						count++;
						prep.setString(1, CAS);
						prep.setString(2, records);
						prep.addBatch();

						if (counter % 1000 == 0) {
							// System.out.println(counter);
							prep.executeBatch();
						}
					}
					
					records=Line;
					CAS=currentCAS;
				} else {
					records+=Line+"\r\n";//separate records in Records field with a carriage return
				}
			}
			
			prep.setString(1, CAS);
			prep.setString(2, records);
			prep.addBatch();
			
			prep.executeBatch();// do what's left
			

			conn.setAutoCommit(true);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	
//	/**
//	 * Create sqlite database table with CAS as primary key (needs unique values for this to work)
//	 * 
//	 * Can search by any field in table but CAS is much faster since primary key
//	 * 
//	 * See http://sqlitebrowser.org/ for user friendly sqlite GUI to look at the database once it's created
//	 * 
//	 * @param filepath
//	 * @return
//	 */
//	public static void createDatabaseIntegerKey(String textFilePath,String del,String tableName,String [] fieldNames,String dbFilePath) {
//
//		try {
//			System.out.println("Creating AA dashboard SQlite table");
//
//			Connection conn= MySQL_DB.getConnection(dbFilePath);
//			Statement stat = MySQL_DB.getStatement(conn);
//			
//			conn.setAutoCommit(true);
//			
//			
//			stat.executeUpdate("drop table if exists "+tableName+";");
//			 
//			stat.executeUpdate("VACUUM;");//compress db now that have deleted the table
//			
////			MySQL_DB.create_table(stat, tableName, fields);
//			
//			//Need CAS as the primary key if we are doing lots of searches- otherwise searches will be like 1 second each!
//			MySQL_DB.create_table_key_with_duplicates_integer_key(stat, tableName, fieldNames,"CAS");//need unique values in the table for key field for this to work!
//
//			conn.setAutoCommit(false);
//
//			BufferedReader br = new BufferedReader(new FileReader(textFilePath));
//
//			String header = br.readLine();
//
//			String s = "insert into " + tableName + " values (";
//
//			for (int i = 1; i <= fieldNames.length; i++) {
//				s += "?";
//				if (i < fieldNames.length)
//					s += ",";
//			}
//			s += ");";
//
//
//			int counter = 0;
//
//			PreparedStatement prep = conn.prepareStatement(s);
//			
//
//			while (true) {
//				String Line = br.readLine();
//
//				counter++;
//				
////				if (counter==100) break;
//
//				if (Line == null)
//					break;
//
//				if (!Line.isEmpty()) {
//
//					LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse(Line, del);
//					
//					
//					if (list.size()!=fieldNames.length) {
//						System.out.println("*wrong number of values: "+Line);
//					}
//
//					
//					String CAS=list.getFirst();
//					String CAS2=CAS.replace("-", "");	
//						
//					int iCAS=-1;
//					
//					try {
//						iCAS=Integer.parseInt(CAS2);
//					} catch (Exception ex) {
//						continue;
//					}
//					
//					
////					 System.out.println(Line);
//
//					for (int i = 0; i < list.size(); i++) {
//						if (i==0) {
//							prep.setInt(i + 1, iCAS );
//						} else {
//							prep.setString(i + 1, list.get(i));	
//						}
//						
////						 System.out.println((i+1)+"\t"+list.get(i));
//					}
//
//					prep.addBatch();
//				}
//
//				if (counter % 1000 == 0) {
//					// System.out.println(counter);
//					prep.executeBatch();
//				}
//
//			}
//
//			int[] count = prep.executeBatch();// do what's left
//
//			conn.setAutoCommit(true);
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//	}
	
	
	private  static FlatFileRecord createFlatFileRecord(Vector<String>fieldNames,Vector<String>fieldValues) {
		FlatFileRecord f=new FlatFileRecord();
		
		 for (int i = 0; i < fieldNames.size(); i++) {
				try {
				
					Field myField = f.getClass().getDeclaredField(f.fieldNames[i]);
					
					if (fieldNames.get(i).equals("valueMass")) {
						double val=Double.parseDouble(fieldValues.get(i));
//						System.out.println("*"+val);
						
						if (val!=0) {
							f.valueMass=val;//no need to use reflection for one field
						}
					} else {
						String val=fieldValues.get(i);
						
						if (val!=null) {
							myField.set(f, val);
						} 
					}
				
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		 return f;
		 
	}
	
	
	private  static FlatFileRecord createFlatFileRecord(ResultSet rs) {
		FlatFileRecord f=new FlatFileRecord();

		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);

//				System.out.println(name);
				
				if (name.equals("valueMass")) {
					double val=rs.getDouble(i);
					//				System.out.println("*"+val);

					if (val!=0) {
						f.valueMass=val;//no need to use reflection for one field
					}
				} else {
					String val=rs.getString(i);

					if (val!=null) {
						Field myField = f.getClass().getDeclaredField(name);			
						myField.set(f, val);
					} 
				}
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
//		 for (int i = 0; i < f.fieldNames.length; i++) {
//				try {
//				
//					Field myField = f.getClass().getDeclaredField(f.fieldNames[i]);					
//
//				
//					if (f.fieldNames[i].equals("valueMass")) {
//						double val=rs.getDouble(i+1);
////						System.out.println("*"+val);
//						
//						if (val!=0) {
//							f.valueMass=val;//no need to use reflection for one field
//						}
//					} else if (f.fieldNames[i].equals("valueActive")) {
//						
//						Boolean val=rs.getBoolean(i+1);
////						System.out.println("*"+val);
//						
//						
//						f.valueActive=val;//no need to use reflection for one field
//						
//					
//					} else {
//						String val=rs.getString(i+1);
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
		 return f;
		 
	}
	
//	static void sortRecordsByCAS(String folder,String filename,String filenameSorted) {
//		
//		ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(folder+"/"+filename);
//		
//		String header=lines.remove(0);
//		
//		Collections.sort(lines);
//		
//		try {
//			
//			FileWriter fw=new FileWriter(folder+"/"+filenameSorted);
//			
//			
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//	}
//	
	
	static void getStructuresForUniqueChemicalsInGHSDatabase() {
		
		try {

			FileWriter fw=new FileWriter("databases/NCCT records for GHS chemicals.txt");
			FileWriter fwMissing=new FileWriter("databases/NCCT records for GHS chemicals-missing.txt");
			
			Connection conn= MySQL_DB.getConnection(AADashboard.DB_Path_AA_Dashboard_Records);
			Statement stat = MySQL_DB.getStatement(conn);

			String sql="SELECT DISTINCT CAS FROM HazardRecords";
			ResultSet rs=stat.executeQuery(sql);
			
//			Statement statNCCT_ID_Records=WebTEST.statNCCT_ID_Records;
			
			int count=0;
			
			fw.write("cas\tcid\tsid\tgsid\tname\tcas\tsmiles\tinchi_key\tinchi_key_1\tinchi\r\n");
			
			while (rs.next()) {
				count++;
				
				if (count%1000==0) System.out.println(count);
				
				String CAS=rs.getString(1);
				String sqlNCCT="SELECT * FROM ncct_lookup WHERE cas="+"\""+CAS+"\""+";";
				
				Statement statNCCT=MySQL_DB.getStatement("databases/ncct_lookup.db");
				
				ResultSet rsNCCT=statNCCT.executeQuery(sqlNCCT);

//				rsNCCT.next();
				
				if (rsNCCT.isClosed()) {
					fwMissing.write(CAS+"\tmissing\r\n");
					fwMissing.flush();
				} else {
					fw.write(CAS+"\t");					
					for (int i=1;i<=rsNCCT.getMetaData().getColumnCount();i++) {
//						System.out.println(CAS+"\t"+rsNCCT.getString(i));
						String value=rsNCCT.getString(i)+"";
								
						value=value.replace("\n", "").trim();
						
						fw.write(value+"");
						
						if (i<rsNCCT.getMetaData().getColumnCount()) {
							fw.write("\t");
						} else {
							fw.write("\n");
						}
					}
				}
				fw.flush();
				
			}
			fw.close();
			fwMissing.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
					

	}
	
	
	static Vector<String> downloadMolFileArrayFromChemidplus (String CAS) {
		
		Vector<String>lines=new Vector<>();
		try {
			
			String strURL="https://chem.sis.nlm.nih.gov/chemidplus/mol3d/"+CAS;
			java.net.URL myURL = new java.net.URL(strURL);

			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));

			int counter=0;
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				lines.add(Line);
				counter++;
			}

			br.close();
			
			
		} catch (FileNotFoundException ex1) {
			System.out.println(CAS+" not found");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return lines;
	}
	
	static String downloadMolFileAsStringFromChemidplus (String CAS) {
		
		String lines="";
		try {
			
			String strURL="https://chem.sis.nlm.nih.gov/chemidplus/mol3d/"+CAS;
			java.net.URL myURL = new java.net.URL(strURL);

			BufferedReader br
			= new BufferedReader(new InputStreamReader(myURL.openStream()));

			int counter=0;
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
//				System.out.println(Line);
				
				lines+=Line+"\r\n";
				counter++;
			}

			br.close();
			
			
		} catch (FileNotFoundException ex1) {
			return "N/A";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return lines;
	}
	
	static void downloadStructuresFromChemidplus(String textCASListPath,String dbFilePath) {
		
		try {
			Class.forName("org.sqlite.JDBC");
			

//			File db = new File(dbFilePath);
//			if (db.exists())
//				db.delete();

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String[] fieldNames = { "CAS", "molfile" };

			String tableName="ChemIDplus3d";
			MySQL_DB.create_table(stat, tableName, fieldNames, "CAS");
			
			BufferedReader br=new BufferedReader(new FileReader(textCASListPath));
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				String CAS=Line.substring(0,Line.indexOf("\t"));
				ResultSet rs = MySQL_DB.getRecords(stat,tableName, "CAS",CAS);
				
				if (rs.next()) {
					System.out.println(CAS+"\talready in db");
					continue;
				}
				
				String molfile=downloadMolFileAsStringFromChemidplus(CAS);
				
				String s = "insert into " + tableName + " values (";

				for (int i = 1; i <= fieldNames.length; i++) {
					s += "?";
					if (i < fieldNames.length)
						s += ",";
				}
				s += ");";
				
				PreparedStatement prep = conn.prepareStatement(s);
				prep.setString(1, CAS);
				prep.setString(2, molfile);
				prep.execute();

				System.out.println(CAS+"\tdone");
				Thread.sleep(3000);
			}
			
			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
	}
	
static void createSDFFromChemidplusStructureDatabase(String dbFilePath,String destFolder) {
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			int maxCount=2000;
			

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String[] fieldNames = { "CAS", "molfile" };

			String tableName="ChemIDplus3d";
			
			String sql="SELECT * From "+tableName;
			
			ResultSet rs = stat.executeQuery(sql);
			
			int fileNum=1;
			FileWriter fw=new FileWriter(destFolder+"/chemidplus3d"+fileNum+".sdf");
			
			int count=0;
			
			while (rs.next()) {
				count++;
				String CAS=rs.getString(1);
				String molfile=rs.getString(2);
				if (molfile.contentEquals("N/A")) {
					molfile="\r\n\r\n\r\n";
					molfile+="0  0  0  0  0  0            999 V2000\r\n";
					molfile+="M  END\r\n";
//					System.out.println(molfile);
				} else {
					
				}
				molfile+="> <CAS>\n"+CAS+"\r\n\r\n";
				fw.write(molfile+"$$$$\r\n");
				fw.flush();
				
				if (count%maxCount==0) {
					fw.close();
					fileNum++;
					fw=new FileWriter(destFolder+"/chemidplus3d"+fileNum+".sdf");
				}
				
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
	}
	
	static void createSDFFromNCCTRecords(String textCASListPath,String sdfPath) {
		
		try {
			Class.forName("org.sqlite.JDBC");
			String dbFilePath = "databases/structures.db";

//			File db = new File(dbFilePath);
//			if (db.exists())
//				db.delete();

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String[] fieldNames = { "CAS", "molfile" };

			String tableName="ChemIDplus3d";
			MySQL_DB.create_table(stat, tableName, fieldNames, "CAS");
			
			BufferedReader br=new BufferedReader(new FileReader(textCASListPath));

			FileWriter fw=new FileWriter(sdfPath);
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(false);

			String header=br.readLine();
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				LinkedList<String>vals=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
						
				String CAS=vals.get(0);
				String inchi=vals.get(vals.size()-1);
				String Name=vals.get(4);
				String SMILES=vals.get(6);
				
				
				InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
				InChIToStructure gen = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());
				IAtomContainer ac=gen.getAtomContainer();
				
//				System.out.println(CAS+"\t"+inchi+"\t"+ac.getAtomCount());
								
				mw.write(ac);
				fw.write("> <CAS>\n"+CAS+"\n\n");
				fw.write("> <molname>\n"+Name+"\n\n");
				fw.write("> <SMILES>\n"+SMILES+"\n\n");
				fw.write("$$$$\n");
			}
			
			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
	}
	
	static void createSDFFromNCCTRecords2(String textCASListPath,String outputFolder) {

		
		try {
			SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

			InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();

			Class.forName("org.sqlite.JDBC");
			String dbFilePath = "databases/structures.db";

//			File db = new File(dbFilePath);
//			if (db.exists())
//				db.delete();

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String[] fieldNames = { "CAS", "molfile" };

			String tableName="ChemIDplus3d";
			MySQL_DB.create_table(stat, tableName, fieldNames, "CAS");
			
			BufferedReader br=new BufferedReader(new FileReader(textCASListPath));

			int fileNum=1;
			System.out.println(fileNum);
			int maxCount=5000;
			
			FileWriter fw=new FileWriter(outputFolder+"/NCCT structures "+fileNum+".sdf");
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(false);

			String header=br.readLine();
			
			int count=0;
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				LinkedList<String>vals=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
						
				String CAS=vals.get(0);
				String inchi=vals.get(vals.size()-1);
				String Name=vals.get(4);
				String SMILES=vals.get(6);
				
				
//				Indigo indigo = new Indigo();
//				IndigoInchi ii = new IndigoInchi(indigo);
//				IndigoObject io=ii.loadMolecule(inchi);
//				
				
				InChIToStructure gen = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());
				IAtomContainer m=gen.getAtomContainer();				
				
//				sdg.setMolecule(m);
//				sdg.generateCoordinates();
//				m = (AtomContainer) sdg.getMolecule();

//				AtomContainer m = null;
//				try {
//					// m=sp.parseSmiles(Smiles);
//					
//					if (!SMILES.contentEquals("null")) {
//						m = (AtomContainer) sp.parseSmiles(SMILES);
//						m.setProperty("Error", "");
//					}
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
				
//				System.out.println(CAS+"\t"+inchi+"\t"+ac.getAtomCount());
								
				mw.write(m);
				fw.write("> <CAS>\n"+CAS+"\n\n");
				fw.write("> <molname>\n"+Name+"\n\n");
				fw.write("> <SMILES>\n"+SMILES+"\n\n");
				fw.write("$$$$\n");
				fw.flush();
				
				count++;
				if (count%maxCount==0) {			
					fw.close();
					fileNum++;
					System.out.println(fileNum);
					fw=new FileWriter(outputFolder+"/NCCT structures "+fileNum+".sdf");
					mw=new MDLV2000Writer(fw);
					mw.setWriteAromaticBondTypes(false);

				}
				
			}
			
			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
	}
	
	static void storeNCCT_Structures(String textCASListPath) {
		
		try {
			Class.forName("org.sqlite.JDBC");
			String dbFilePath = "databases/structures.db";

//			File db = new File(dbFilePath);
//			if (db.exists())
//				db.delete();

			// create the db:
			Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
			Statement stat = conn.createStatement();

			String[] fieldNames = { "CAS", "molfile" };

			String tableName="NCCT";
			MySQL_DB.create_table(stat, tableName, fieldNames, "CAS");
			
			BufferedReader br=new BufferedReader(new FileReader(textCASListPath));
			
			while (true) {
				String Line=br.readLine();
				
				if (Line==null) break;
				
				String CAS=Line.substring(0,Line.indexOf("\t"));
				ResultSet rs = MySQL_DB.getRecords(stat,tableName, "CAS",CAS);
				
				if (rs.next()) {
					System.out.println(CAS+"\talready in db");
					continue;
				}
				
				String molfile=downloadMolFileAsStringFromChemidplus(CAS);
				
				String s = "insert into " + tableName + " values (";

				for (int i = 1; i <= fieldNames.length; i++) {
					s += "?";
					if (i < fieldNames.length)
						s += ",";
				}
				s += ");";
				
				PreparedStatement prep = conn.prepareStatement(s);
				prep.setString(1, CAS);
				prep.setString(2, molfile);
				prep.execute();

				System.out.println(CAS+"\tdone");
				Thread.sleep(3000);
			}
			
			br.close();


		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		
	}
	
	
	
	
	public static void main(String[] args) {

		//Create files for all sources:
//		Parse.recreateFilesAllSources();
		
//		String folder=AADashboard.dataFolder+"\\dictionary\\text output";
//		String filename="flat file 2019-05-29.txt";
//		String textFilePath=folder+"\\"+filename;
//		FlatFileRecord.createFlatFileFromAllSources(textFilePath);

	
		String folder="AADashboard calcs";
		String filename="flat file 2019-07-18.txt";
		String textFilePath=folder+"/"+filename;
		FlatFileRecord.createFlatFileFromAllSourcesSortedByCASWithAuthority(textFilePath);


		//Create flat file for all data:
//		FlatFileRecord.createFlatFileFromAllSourcesSortedByCAS(textFilePath);
		
		//Get counts for each source:
//		FlatFileRecord.analyzeRecords(textFilePath,folder+"/counts.txt");
		
		String del="|";		
		//Create Sqlite database from flat file:
//		CreateGHS_Database.createDatabase(textFilePath,del,"HazardRecords",FlatFileRecord.fieldNames);
		CreateGHS_Database.createDatabase(textFilePath,del,"HazardRecords",FlatFileRecord.fieldNames);
		
//		CreateGHS_Database.createDatabaseIntegerKey(textFilePath,del,"HazardRecords",FlatFileRecord.fieldNames,"databases/db_integer_key.db");
		
		//**********************************************************************************************
		//Code to get structures for chemicals with GHS data:
		
		String  []fields= {"CAS","Records"};
//		CreateGHS_Database.createDatabaseWithPrimaryKey(textFilePath,"databases/AA dashboard_w_primary_key.db", del, "HazardRecords", fields);
//		getStructuresForUniqueChemicalsInGHSDatabase();

		//*******************************************************************************************
		String chemidplusfolder="AADashboard calcs\\chemidplus structures";
		String textCASListPath=chemidplusfolder+"\\NCCT records for GHS chemicals-missing.txt";
		String dbFilePath=chemidplusfolder+"/structures.db";
//		downloadStructuresFromChemidplus(textCASListPath,dbFilePath);
//		createSDFFromChemidplusStructureDatabase(dbFilePath,chemidplusfolder);
		
		
//		String textCASListPath="AADashboard calcs/NCCT records for GHS chemicals.txt";
//		String sdfPath="AA Dashboard calcs/NCCT structures.sdf";
//		createSDFFromNCCTRecords(textCASListPath,sdfPath);
//		createSDFFromNCCTRecords2(textCASListPath,"databases/NCCT structures");

		
	}
}
