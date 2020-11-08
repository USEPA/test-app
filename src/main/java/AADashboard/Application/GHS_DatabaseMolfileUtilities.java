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

import gov.epa.api.Score;
import gov.epa.api.ScoreRecord;


/**
 * Some methods to handle structures in AA Dashboard database- might not be needed
 *  
 * @author TMARTI02
 *
 */
public class GHS_DatabaseMolfileUtilities {
    

	

	
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

				
	}
}
