package ToxPredictor.Database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;

import ToxPredictor.Application.Calculations.NameToStructureOpsin;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Inchi;

import uk.ac.cam.ch.wwmm.opsin.OpsinResult;
import uk.ac.cam.ch.wwmm.opsin.OpsinWarning;

public class ResolverDb2 {

	private static final Logger logger = LogManager.getLogger(ResolverDb.class);

	private static final int BATCH_SIZE = 10000;
	private static final String TABLE_NAME = "substances";
	public static String sqlitePath = "databases"+File.separator+"snapshot.db";
//	public static String sqlitePath = "databases/snapshot-2025-07-30.db";
	
	NameToStructureOpsin nameToStructureOpsin=new NameToStructureOpsin(); 

	public static String getSqlitePath() {
		return sqlitePath;
	}

	public static void setSqlitePath(String sqlitePath) {
		ResolverDb2.sqlitePath = sqlitePath;
	}

	private static Connection conn = null;
	private static Statement stat = null;
	
	public static Statement getStat() {
		return stat;
	}

	private static Boolean isAvailable = true;

	public static Boolean isAvailable() {
		return isAvailable;
	}

	public static void assureDbIsOpen() {
		synchronized (ResolverDb2.class) {
			if (conn == null) {
				try {
					Class.forName("org.sqlite.JDBC");
					conn = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
					stat = conn.createStatement();
					isAvailable = true;
				} catch (Exception ex) {
					logger.catching(ex);
					isAvailable = false;
				}
			}
		}
	}

	private static ArrayList<DSSToxRecord> executeQuery(String query) {
		
		long t1=System.currentTimeMillis();
		assureDbIsOpen();
		long t2=System.currentTimeMillis();
//		System.out.println((t2-t1)+ " ms to open db");
//		System.out.println(query);

		ResultSet rs = null;
		try {
			ArrayList<DSSToxRecord> cdrs = new ArrayList<DSSToxRecord>();

			rs = stat.executeQuery(query);
			while (rs.next()) {
				DSSToxRecord cdr = new DSSToxRecord();
				cdr.cid = rs.getString("cid");
				cdr.sid = rs.getString("sid");
//				cdr.gsid = rs.getString("id");
				cdr.name = rs.getString("name");
				cdr.cas = rs.getString("casrn");
				cdr.mol = rs.getString("mol");
				cdr.inchi = rs.getString("inchi");
				cdr.inchiKey = rs.getString("inchiKey");
				cdr.inchiKey1 = rs.getString("inchiKey1");
				cdrs.add(cdr);
			}

			
			long t3=System.currentTimeMillis();
			
//			System.out.println((t3-t2)+ " ms to do query:");
//			System.out.println(query);
			
			return cdrs;
			
		

		} catch (Exception ex) {
			logger.catching(ex);
			return null;
		} finally {
			try {
				if (rs != null && !rs.isClosed())
					rs.close();
			} catch (SQLException ex) {

			}
		}
	}
	
	
	private static ArrayList<String> executeSynonymQuery(String query) {
		assureDbIsOpen();
//		System.out.println(query);

		ResultSet rs = null;
		try {
			ArrayList<String> gsids = new ArrayList<>();

			rs = stat.executeQuery(query);
			while (rs.next()) {				
				gsids.add(rs.getString("gsid"));
			}

			return gsids;

		} catch (Exception ex) {
			logger.catching(ex);
			return null;
		} finally {
			try {
				if (rs != null && !rs.isClosed())
					rs.close();
			} catch (SQLException ex) {

			}
		}
	}


	public static synchronized ArrayList<DSSToxRecord> lookupByCAS(String cas) {
		return executeQuery("select * from " + TABLE_NAME + " where casrn = '" + cas + "';");
	}

	public static synchronized ArrayList<DSSToxRecord> lookupByName(String name) {
//		name=name.replace("'", "''");
		return executeQuery("select * from " + TABLE_NAME + " where name = '" + name + "';");
	}
	
	
	public static synchronized ArrayList<DSSToxRecord> lookupBySynonym(String synonym) {
//		synonym=synonym.replace("'", "''");
		
		ArrayList<String>gsids=executeSynonymQuery("select * from " + "synonyms" + " where synonym = '" + synonym + "';");
		
		if (gsids.size()==0) return new ArrayList<DSSToxRecord>();
		
//		System.out.println(gsids.get(0));
		
		//Return record for first synonym hit:
		return executeQuery("select * from " + TABLE_NAME + " where id = '" + gsids.get(0) + "';");
		
	}

	
	
//	public static ArrayList<DSSToxRecord> lookupByNameToStructure(String Name) {				
//		String smiles=NameToStructureOpsin.nameToSmiles(Name);
//
////		System.out.println(smiles);
//		
//		if (smiles==null) return new ArrayList<DSSToxRecord>(); 
//		
//		return lookupBySMILES(smiles);
//	}


	public static synchronized ArrayList<DSSToxRecord> lookupByInChIKey(String inchiKey) {
		return executeQuery("select * from " + TABLE_NAME + " where inchiKey = '" + inchiKey + "';");
	}
	

	public static synchronized ArrayList<DSSToxRecord> lookupByInChIKey1(String inchiKey) {
		return lookupByInChIKey1(inchiKey, true);
	}

	public static synchronized ArrayList<DSSToxRecord> lookupByInChIKey1(String inchiKey, boolean preferred) {
		if (inchiKey.length() > 14)
			inchiKey = inchiKey.substring(0, 14);
		if (!preferred)
			return executeQuery("select * from " + TABLE_NAME + " where inchiKey1 = '" + inchiKey + "';");
		else
			return executeQuery("select * from " + TABLE_NAME + " where inchiKey1 = '" + inchiKey
					+ "' and inchi not like '%/i%';");
	}

	public static synchronized ArrayList<DSSToxRecord> lookupBySMILES(String smiles) {
			Inchi inchi = Inchi.generateInChiKeyIndigo(smiles);
			
			if (inchi==null) return new ArrayList<DSSToxRecord>();
			
			ArrayList<DSSToxRecord> res = lookupByInChIKey(inchi.inchiKey);
			if ( res.size() == 0 )
				res = lookupByInChIKey1(inchi.inchiKey1);
			return res;		
	}
	public static synchronized ArrayList<DSSToxRecord> lookupByInChis(Inchi inchi) {		
		if (inchi==null) return new ArrayList<DSSToxRecord>();		
		
		ArrayList<DSSToxRecord> recs = lookupByInChIKey(inchi.inchiKey);
		if ( recs.size() == 0 ) {
			recs = lookupByInChIKey1(inchi.inchiKey1);
//			System.out.println("Found by inchiKey1");
		}
		return recs;		
	}
	
	public static synchronized ArrayList<DSSToxRecord> lookupByInChiKey(Inchi inchi) {		
		if (inchi==null) return new ArrayList<DSSToxRecord>();		
		
		ArrayList<DSSToxRecord> res = lookupByInChIKey(inchi.inchiKey);
		return res;		
	}
	
	

//	/**
//	 * Look up in database by structure using inchi keys
//	 * 
//	 * Current database has a lot of nonstandard inchi keys- TODO- should they be nonstandard?
//	 * 
//	 * @param m
//	 * @return
//	 */
//	public static synchronized ArrayList<DSSToxRecord> lookupByAtomContainer(AtomContainer m) {
//		
//		try {
//
//			if (m.getAtomCount()==0) return null;
//			
//			boolean debug=false;
//			
//			String[] inchi = CDKUtilities.generateInChiKey(m);
//			String inchiKey=inchi[1];
//			
//
//			String [] inchi2 = IndigoUtilities.generateInChiKey(m);
//			String inchiKey2=inchi2[1];
//
//			//TODO is it ok just to change S (standard) to N (nonstandard) to get a hit in NCCT's db???
//			//TODO redo the database so that the NCCT records are all standard? Are they all standard?
//			
//			String inchiKeyN = getInchiN(inchiKey);
//			
////			System.out.println("inchiKey="+inchiKey);
////			System.out.println("inchiKeyN="+inchiKeyN);
//			
//			ArrayList<DSSToxRecord> res=null;
//
//			if (inchiKey!=null) {
//				
//				res=lookupByInChIKey(inchiKey);
//				
//				if (res.size()!=0) {
//					if (debug) System.out.println("Found by CDK inchikey");
//					return res;
//				}
//								
//				res=lookupByInChIKey(inchiKeyN);
//
//				if (res.size()!=0) {
//					if (debug) System.out.println("Found by CDK inchikey with S changed to N");
//					return res;
//				}
//			}
//			
//			
//			String inchiKey2N = getInchiN(inchiKey2);
//			
//			if(inchiKey2!=null) {
//				res=lookupByInChIKey(inchiKey2);
//				
//				if (res.size()!=0) {
//					if (debug) System.out.println("Found by CDK indigo inchikey");
//					return res;
//				}
//				
//				res=lookupByInChIKey(inchiKey2N);
//				
//				if (res.size()!=0) {
//					if (debug) System.out.println("Found by CDK indigo inchikey with S changed to N");
//					return res;
//				}
//			}
//			
//			return res;
//		
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//			return null;
//		}
//	}
//	
	
//	/**
//	 * Look up in database by structure using inchi keys
//	 * 
//	 * Current database has a lot of nonstandard inchi keys- TODO- should they be nonstandard?
//	 * 
//	 * @param m
//	 * @return
//	 */
//	public static synchronized ArrayList<DSSToxRecord> lookupByAtomContainer2dConnectivity(AtomContainer m) {
//		
//		try {
//
//			if (m.getAtomCount()==0) return null;
//			
//			boolean debug=false;
//			
//			String[] inchi = CDKUtilities.generateInChiKey(m);
//			String inchiKey=inchi[1];
//			
//
//			String [] inchi2 = IndigoUtilities.generateInChiKey(m);
//			String inchiKey2=inchi2[1];
//
//						
//			ArrayList<DSSToxRecord> res=null;
//
//			if (inchiKey!=null) {				
//				res=lookupByInChIKey1(inchiKey,false);				
//				if (res.size()!=0) {
//					if (debug) System.out.println("Found by CDK inchikey");
//					return res;
//				}								
//			}
//			
//								
//			if(inchiKey2!=null) {
//				res=lookupByInChIKey1(inchiKey2,false);				
//				if (res.size()!=0) {
//					if (debug) System.out.println("Found by CDK indigo inchikey");
//					return res;
//				}
//			}
//			
//			return res;
//		
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//			return null;
//		}
//	}

	private static String getInchiN(String inchiKey) {
		String inchiKeyN=inchiKey.substring(0,23)+"N"+inchiKey.substring(24,inchiKey.length());
		return inchiKeyN;
	}

	public static synchronized ArrayList<DSSToxRecord> lookup(String id) {
		return lookup(id, ChemIdType.AnyId);
	}

	public static void logit(String source,String id,DSSToxRecord rec) {
		logger.info("{}\t{}\t{}",id,source,rec.toString());		
	}
	
	
	public static void assignRecordByStructureViaInchis (IAtomContainer ac,String oldCAS) {
		
		Inchi inchi = Inchi.generateInChiKeyIndigo(ac);									
		if (inchi==null) return;
		
//		IAQRGUVFOMOMEM-UHFFFAOYSA-N//before
//		IAQRGUVFOMOMEM-ONEGZZNKSA-N//after load
		
//		System.out.println(inchi.inchiKey);
		
//		ArrayList<DSSToxRecord>recs = lookupByInChis(inchi);
				
		if (Strings.isBlank(oldCAS) || oldCAS.contains("C_")) {
			//First try by exact inchiKey match:
			ArrayList<DSSToxRecord>recs = lookupByInChIKey(inchi.inchiKey);

			if(recs.size()>0)			
				DSSToxRecord.assignFromDSSToxRecord(ac, recs.get(0));
			else {//Now try by inchiKey1
				recs = lookupByInChIKey1(inchi.inchiKey1);
				if(recs.size()>0)			
					DSSToxRecord.assignFromDSSToxRecord(ac, recs.get(0));
			}
		} else {
			ArrayList<DSSToxRecord>recs = lookupByInChIKey(inchi.inchiKey);//first try by full inchiKey
			if(recs.size()>0) {
				assignRecordMatchingCAS(ac, oldCAS, recs);	
			} else {
				recs = lookupByInChIKey1(inchi.inchiKey1);//try by inchiKey1
				assignRecordMatchingCAS(ac, oldCAS, recs);
			}
		}
		
		
//		if (recs.size()>0)	{
//			assignRecord(ac, recs,oldCAS);			
//		} else {
//			assignRecordByStructureNotInDB(ac);
//		}
	}

	private static void assignRecordMatchingCAS(IAtomContainer ac, String oldCAS, ArrayList<DSSToxRecord> recs) {
		boolean match=false;
		for (DSSToxRecord rec:recs) {
			if (rec.cas.contentEquals(oldCAS)) {
				DSSToxRecord.assignFromDSSToxRecord(ac, rec);
//							System.out.println("old CAS is ok!");
				match=true;
				break;
			}
		}
		if (!match) DSSToxRecord.assignDSSToxInfoFromFirstRecord(ac, recs);
	}
	
	public static void assignRecordByStructureViaInchiKey (AtomContainer ac,String oldCAS) {
		
		Inchi inchi = Inchi.generateInChiKeyIndigo(ac);									
		if (inchi==null) return;
		
//		IAQRGUVFOMOMEM-UHFFFAOYSA-N//before
//		IAQRGUVFOMOMEM-ONEGZZNKSA-N//after load
		
//		System.out.println(inchi.inchiKey);
		
		ArrayList<DSSToxRecord>recs = lookupByInChiKey(inchi);			
		
		if (recs.size()>0)	{
			assignRecord(ac, recs,oldCAS);			
		} else {
			assignRecordByStructureNotInDB(ac);
		}
	}
	
	
	public static void assignRecordByStructureNotInDB(IAtomContainer m) {
		Inchi inchi = Inchi.generateInChiKeyIndigo(m);
		
		DSSToxRecord rec=new DSSToxRecord();
				
		if (inchi==null) {
			rec.cas="C_"+System.currentTimeMillis();
		} else {
			rec.cas="C_"+inchi.inchiKey;
			rec.inchi=inchi.inchi;
			rec.inchiKey=inchi.inchiKey;
		}
		
		DSSToxRecord.assignFromDSSToxRecord(m, rec);
		
		
	}

	
	public static ArrayList<DSSToxRecord> lookupByAtomContainer (IAtomContainer ac) {		
		Inchi inchi = Inchi.generateInChiKeyIndigo(ac);									
		if (inchi==null) return new ArrayList<DSSToxRecord>();
		
		ArrayList<DSSToxRecord>recs = lookupByInChis(inchi);							
		return recs;					
	}

	
	private static void assignRecord(IAtomContainer m, ArrayList<DSSToxRecord> recs,String oldCAS) {
				

		if (Strings.isBlank(oldCAS) || oldCAS.contains("C_")) {
			DSSToxRecord.assignFromDSSToxRecord(m, recs.get(0));
		} else {
			assignRecordMatchingCAS(m, oldCAS, recs);
		}
	}


	static boolean isAllNumbers(String text) {		
		String text2=text.replace("-", "").replace("\n", "");
		if(text2.matches("[0-9]+")) return true;
		else return false;
	}
	
	public static boolean isCAS(String text) {
		if(!isAllNumbers(text)) return false;
		
		String text2=text.replace("-", "").replace("\n", "");
		if (text2.length()<5) return false;
		if (!isCASValid(text2)) return false;
		return true;
	}
	
	public static boolean isCASValid(String cas)  {

		try {
        cas = cas.replaceAll("-","");
        // Although the definition is usually expressed as a 
        // right-to-left fn, this works left-to-right.
        // Note the loop stops one character shy of the end.
        int sum = 0;
        for (int indx=0; indx < cas.length()-1; indx++) {
            sum += (cas.length()-indx-1)*Integer.parseInt(cas.substring(indx,indx+1));
        }
        // Check digit is the last char, compare to sum mod 10.
        return Integer.parseInt(cas.substring(cas.length()-1)) == (sum % 10);
		} catch (Exception ex) {
			return false;
		}
	}
	
	public static String parseSearchCAS(String srchCAS) {
		
		//kill off zeros in front:
		while (srchCAS.substring(0,1).contentEquals("0")) {
			srchCAS=srchCAS.substring(1,srchCAS.length());
			if (srchCAS.isEmpty()) return "";
		}
		
		if (srchCAS.indexOf("-")>-1) {	
			String part1, part2;
			part1 = (Integer.parseInt(srchCAS.substring(0, srchCAS.indexOf("-"))))
					+ "";
			part2 = srchCAS.substring(srchCAS.indexOf("-"), srchCAS.length());
			srchCAS = part1 + part2;
		
		} else { //missing dashes- try to convert it:
			String temp=srchCAS;
			String part1,part2,part3;
			
			if (temp.length()>=4) {
				part3=temp.substring(temp.length()-1,temp.length());
				temp=temp.substring(0,temp.length()-1);

				part2=temp.substring(temp.length()-2,temp.length());
				temp=temp.substring(0,temp.length()-2);

				part1=temp;
				srchCAS=part1+"-"+part2+"-"+part3;
			} else {
				return srchCAS;
			}
		}

		return srchCAS;
		
	}

	
	public static synchronized ArrayList<DSSToxRecord> lookup(String id, ChemIdType idType) {
		id=id.replace("'", "''");//to not mess sql query
		
		switch (idType) {
		case SMILES:
			return lookupBySMILES(id);
		case CAS:
			return lookupByCAS(id);
		case Name:
			return lookupByNameAdvanced(id);
		case Synonym:
			return lookupBySynonym(id);		
		case DTXSID:
			return lookupByDTXSID(id);
		case InChIKey:
			return lookupByInChIKey(id);
		case InChIKey_1:
			return lookupByInChIKey1(id);
		case AnyId: {
			ArrayList<DSSToxRecord> recs;
				

			
			//lookup by DTXSID:
			if (id.contains("DTXSID")) {
				recs = lookupByDTXSID(id);
				if (recs.size() > 0) {
					logit("DTXSID",id,recs.get(0));
					return recs;		
				}
			}
			
			//CAS:			
			if (isCAS(id)) {
				String CAS=parseSearchCAS(id.replace("\n", ""));								
				recs = lookupByCAS(CAS);							
				if (recs.size() > 0) {
//					System.out.println("Found by CAS\t"+CAS);
					logit("CAS",id,recs.get(0));						
					return recs;
				}				
			}
			
			if (id.contains("-")) {
				//Look up by inchiKey:
				recs = lookupByInChIKey(id);
				if (recs.size() > 0) {
					logit("InchiKey",id,recs.get(0));
					return recs;
				}
			}
			
			//Look up by inchiKey1:
			if (id.length() == 14) { // Only when explicitly provided with InChIKey1
				recs = lookupByInChIKey1(id);
				if (recs.size() > 0) {
					logit("InchiKey1",id,recs.get(0));		
					return recs;
				}
			}			

			//Next try name (name, synonym, name to structure):
			ArrayList<DSSToxRecord> resName=lookupByNameAdvanced(id);			
			if (resName.size()>0) return resName;
									
			//Next try smiles and inchi
			Inchi inchi = Inchi.generateInChiKeyIndigo(id);			
			recs = lookupByInChis(inchi);			
			
			String strField="Smiles";
			if (id.contains("InChI")) strField="Inchi";
					
			if (recs.size() > 0) {				
//				System.out.println(inchi.inchiKey+"\t"+recs.get(0).inchiKey);				
				logit(strField,id,recs.get(0));
//				recs.get(0).smiles=id;//store smiles since structures with unspecified isomer messes up cdk drawing for now
			} else if (inchi!=null) {
//				System.out.println("inchikey="+inchi.inchiKey);

				DSSToxRecord r=new DSSToxRecord();
				r.cas="C_"+inchi.inchiKey;				
				if (strField.contentEquals("Smiles")) r.smiles=id;
				recs.add(r);
								
				r.inchi=inchi.inchi;
				r.inchiKey=inchi.inchiKey;
				
				logit(strField+" but no db match",id,recs.get(0));
								
			} else {
//				logit("N/A",id,new DSSToxRecord());
			}
			
			return recs;
			
			
		}
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public static ArrayList<DSSToxRecord> lookupByDTXSID(String DTXSID) {
		return executeQuery("select * from " + TABLE_NAME + " where sid = '" + DTXSID + "';");
	}
	
	public static ArrayList<DSSToxRecord> lookupByDTXCID(String DTXCID) {
		return executeQuery("select * from " + TABLE_NAME + " where cid = '" + DTXCID + "';");
	}


	private static ArrayList<DSSToxRecord> lookupByInchi(String Inchi) {
		return executeQuery("select * from " + TABLE_NAME + " where inchi = '" + Inchi + "';");
	}
	public static synchronized ArrayList<DSSToxRecord> lookupByNameAdvanced(String id) {

		
		ArrayList<DSSToxRecord> res;

		res = lookupByName(id);
		if (res.size() > 0) {
			logit("Name",id,res.get(0));
			return res;
		}

		res=lookupBySynonym(id);
		if (res.size() > 0) {
			logit("Synonym",id,res.get(0));
			return res;
		}

		OpsinResult or=NameToStructureOpsin.nameToSmiles(id.replace("''", "'"));//need to convert '' back to '
		List<OpsinWarning>warnings=or.getWarnings();//TODO implement OpsinWarning									

		if (or.getSmiles()!=null) {				
			Inchi inchi = Inchi.generateInChiKeyIndigo(or.getSmiles());				
			res=lookupByInChis(inchi);				

			if (res.size() > 0) {
				logit("Name to structure",id,res.get(0));
				return res;
			} else {
				DSSToxRecord r=new DSSToxRecord();
				r.cas="C_"+inchi.inchiKey;
				r.inchi=inchi.inchi;
				r.inchiKey=inchi.inchiKey;
				r.name=id;
				r.smiles=or.getSmiles();
				res.add(r);
				logit("Name to structure no db match",id,res.get(0));
				return res;
			}
		}
		

		return res;			

	}

	public static synchronized ArrayList<DSSToxRecord> lookup(List<String> ids, ChemIdType idType) {
		ArrayList<DSSToxRecord> res = new ArrayList<DSSToxRecord>();
		for (String id : ids) {
			ArrayList<DSSToxRecord> r = lookup(id, idType);
			res.addAll(r);
		}
		return res;
	}

	public static synchronized ArrayList<DSSToxRecord> lookup(List<String> ids) {
		return lookup(ids, ChemIdType.AnyId);
	}

	public static synchronized ArrayList<DSSToxRecord> fetchAll() {
		return executeQuery("select * from " + TABLE_NAME);
	}

	private static CommandLine parseCli(String[] args) {
		// create the command line parser
		DefaultParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();
		options.addOption("i", "in", true, "Input (*.sdf) file or directory containing *.sdf files");
		options.addOption("o", "out", true, "Output (*.db) file");
		options.addOption("t", "toolkit", true, "Toolkit to use: CDK or Indigo (CDK is default)");
		options.addOption("e", "errors", false, "Create errors SDF file");

		try {
			return parser.parse(options, args);
		} catch (ParseException exp) {
			logger.catching(exp);
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ResolverDb", options);
		}

		return null;
	}

	public static void main(String[] args) throws Exception {
		
		
//		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup("71-43-2",ChemIdType.CAS);
//		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup("c1ccccc1",ChemIdType.SMILES);
//		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup("tnt",ChemIdType.Synonym);
//		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup("benzene",ChemIdType.Name);
		
//		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup("4,5,6-tribromobenzene",ChemIdType.AnyId);
		
		ArrayList<DSSToxRecord>recs=ResolverDb2.lookup("71-43-2",ChemIdType.AnyId);//CAS
		recs=ResolverDb2.lookup("71432",ChemIdType.AnyId);//CAS
		recs=ResolverDb2.lookup("0000071-43-2",ChemIdType.AnyId);//CAS
		recs=ResolverDb2.lookup("benzene",ChemIdType.AnyId);//good name
		recs=ResolverDb2.lookup("tnt",ChemIdType.AnyId);//common name
		recs=ResolverDb2.lookup("CC=CC",ChemIdType.AnyId);//smiles that works
		recs=ResolverDb2.lookup("4,5,6-tribromobenzene",ChemIdType.AnyId);//numbering is right but not IUPAC
		recs=ResolverDb2.lookup("1,2,3-dibromobenzene",ChemIdType.AnyId);//bad name
		recs=ResolverDb2.lookup("1-bromo,2-chloro,3-fluoro,4-iodo,5-methylbenzene",ChemIdType.AnyId);
		recs=ResolverDb2.lookup("COsa;ldk;ssk",ChemIdType.AnyId);//bad identifier
		recs=ResolverDb2.lookup("COCOCOCCCCCOCCCCCOCCCOCCCCOCC",ChemIdType.AnyId);//valid smiles but not in db 
		recs=ResolverDb2.lookup("xylenes",ChemIdType.AnyId);//valid smiles but not in db
		recs=ResolverDb2.lookup("pfos",ChemIdType.AnyId);//synonym
		recs=ResolverDb2.lookup("pfoa",ChemIdType.AnyId);//synonym

		System.out.println(recs.get(0).mol);
		
		
//		CommandLine cli = parseCli(args);
//		if (cli != null) {
//			String input = cli.getOptionValue('i');
//			String output = cli.getOptionValue('o');
//			ChemInfToolkit tk = cli.hasOption('t') ? ChemInfToolkit.valueOf(cli.getOptionValue('t'))
//					: ChemInfToolkit.CDK;
//			if (!new File(input).isDirectory())
//				updateNcctLookupDb(input, output, tk, cli.hasOption('e'));
//			else {
//				File dir = new File(input);
//				File[] files = dir.listFiles(new FilenameFilter() {
//					@Override
//					public boolean accept(File dir, String name) {
//						return name.toLowerCase().endsWith(".sdf");
//					}
//				});
//
//				for (File file : files) {
//					updateNcctLookupDb(file.getPath(), output, tk, cli.hasOption('e'));
//				}
//			}
//		}
	}
}

