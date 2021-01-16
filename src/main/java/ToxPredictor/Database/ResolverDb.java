package ToxPredictor.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoInchi;

import ToxPredictor.Application.Calculations.NameToStructureOpsin;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.ChemInfToolkit;
import ToxPredictor.Utilities.FileUtils;
import ToxPredictor.Utilities.Inchi;
import ToxPredictor.Utilities.IndigoUtilities;

public class ResolverDb {

	private static final Logger logger = LogManager.getLogger(ResolverDb.class);

	private static final int BATCH_SIZE = 10000;
	private static final String TABLE_NAME = "ncct_lookup";
	private static String sqlitePath = "databases/ncct_lookup.db";
	
	NameToStructureOpsin nameToStructureOpsin=new NameToStructureOpsin(); 

	public static String getSqlitePath() {
		return sqlitePath;
	}

	public static void setSqlitePath(String sqlitePath) {
		ResolverDb.sqlitePath = sqlitePath;
	}

	public static synchronized int updateNcctLookupDb(String sdfPath, String dbPath)
			throws IOException, ClassNotFoundException, SQLException {
		return updateNcctLookupDb(sdfPath, dbPath, ChemInfToolkit.CDK, false);
	}

	public static synchronized int updateNcctLookupDb(String sdfPath, String dbPath, ChemInfToolkit tk,
			boolean writeSdfErrors) throws IOException, ClassNotFoundException, SQLException {

		int counter = 0;
		Connection conn = null;
		Statement stat = null;
		PreparedStatement prep = null;
		IteratingSDFReader rCdk = null;
		Indigo indigo = null;
		IndigoObject rIndigo = null;
		IndigoInchi indigoInchi = null;
		IAtomContainer cdkRec = null;
		IndigoObject indigoRec = null;
		String cid = null;
		String sid = null;
		String gsid = null;
		String name = null;
		String cas = null;
		IndigoObject sdfErrSaver = null;
		String sdfErrPath = null;

		try {
			logger.info("SDF file: {}, DB file: {}", sdfPath, dbPath);

			if (tk == ChemInfToolkit.CDK)
				rCdk = new IteratingSDFReader(new FileInputStream(sdfPath), DefaultChemObjectBuilder.getInstance(),
						true);
			else if (tk == ChemInfToolkit.Indigo) {
				indigo = new Indigo();
				indigo.setOption("ignore-stereochemistry-errors", true);
				indigoInchi = new IndigoInchi(indigo);
				rIndigo = indigo.iterateSDFile(sdfPath);
			}

			if (tk == ChemInfToolkit.Indigo && writeSdfErrors) {
				sdfErrPath = FileUtils.replaceExtension(sdfPath, "-err.sdf");
				Files.deleteIfExists(Paths.get(sdfErrPath));
				sdfErrSaver = indigo.writeFile(sdfErrPath);
			}

			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			stat = conn.createStatement();

			conn.setAutoCommit(true);

			stat.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME
					+ " (cid TEXT NOT NULL UNIQUE, sid TEXT NOT NULL UNIQUE, gsid INTEGER NOT NULL UNIQUE, "
					+ "name TEXT COLLATE NOCASE, cas TEXT UNIQUE, smiles TEXT, inchi_key TEXT, inchi_key_1 TEXT, inchi TEXT);");

			stat.executeUpdate("drop index if exists gsid_idx;");
			stat.executeUpdate("drop index if exists cas_idx;");
			stat.executeUpdate("drop index if exists inchi_key_idx;");
			stat.executeUpdate("drop index if exists inchi_key_1_idx;");
			stat.executeUpdate("drop index if exists name_idx;");

			conn.setAutoCommit(false);

			prep = conn.prepareStatement(
					"insert or replace into ncct_lookup (cid, sid, gsid, name, cas, smiles, inchi_key, inchi_key_1, inchi) values (?, ?, ?, ?, ?, ?, ?, ?, ?);");

			while (true) {
				if (tk == ChemInfToolkit.CDK) {
					if (!rCdk.hasNext())
						break;
					cdkRec = rCdk.next();
					cid = cdkRec.getProperty("DSSTox_Compound_id");
					sid = cdkRec.getProperty("DSSTox_Substance_id");
					gsid = sid.substring(8);
					name = cdkRec.getProperty("Preferred_name");
					cas = cdkRec.getProperty("CASRN");
				} else if (tk == ChemInfToolkit.Indigo) {
					if (!rIndigo.hasNext())
						break;
					indigoRec = rIndigo.next();
					cid = indigoRec.getProperty("DSSTox_Compound_id");
					sid = indigoRec.getProperty("DSSTox_Substance_id");
					gsid = sid.substring(8);
					name = indigoRec.getProperty("Preferred_name");
					cas = indigoRec.getProperty("CASRN");
				}

				prep.setString(1, cid);
				prep.setString(2, sid);
				prep.setString(3, gsid);
				prep.setString(4, name);
				prep.setString(5, cas);

				if (tk == ChemInfToolkit.CDK) {
					Inchi inchi = CDKUtilities.generateInChiKey(cdkRec);
					String smiles = CDKUtilities.generateSmiles(cdkRec, SmiFlavor.Absolute);

					prep.setString(6, smiles);
					prep.setString(7, inchi.inchiKey);
					prep.setString(8, inchi.inchiKey.substring(0, 14));
					prep.setString(9, inchi.inchi);
				} else if (tk == ChemInfToolkit.Indigo) {
					String inchi = null;
					String inchiKey = null;
					String smiles = null;
					boolean appended = false;
					try {
						inchi = indigoInchi.getInchi(indigoRec);
						inchiKey = indigoInchi.getInchiKey(inchi);
					} catch (IndigoException ex) {
						logger.catching(ex);
						indigoRec.setProperty("error", ex.getMessage());
						if (sdfErrSaver != null) {
							try {
								sdfErrSaver.sdfAppend(indigoRec);
							} catch (IndigoException ex2) {
							}
						}
						appended = true;
					}

					try {
						smiles = indigoRec.canonicalSmiles();
					} catch (IndigoException ex) {
						logger.catching(ex);
						if (!appended) {
							indigoRec.setProperty("error", ex.getMessage());
							if (sdfErrSaver != null) {
								try {
									sdfErrSaver.sdfAppend(indigoRec);
								} catch (IndigoException ex2) {
								}
							}
						}
					}

					prep.setString(6, smiles);
					prep.setString(7, inchiKey);
					prep.setString(8, inchiKey != null ? inchiKey.substring(0, 14) : null);
					prep.setString(9, inchi);
				}

				prep.addBatch();

				if (++counter % BATCH_SIZE == 0) {
					prep.executeBatch();
					logger.info("{} records", counter);
				}
			}

			prep.executeBatch();
			logger.info("{} records - done", counter);

			conn.commit();
			conn.setAutoCommit(true);

			logger.info("Creating indexes...");
			stat.executeUpdate("create unique index if not exists gsid_idx on ncct_lookup(gsid);");
			stat.executeUpdate("create unique index if not exists cas_idx on ncct_lookup(cas);");
			stat.executeUpdate("create index if not exists inchi_key_idx on ncct_lookup(inchi_key);");
			stat.executeUpdate("create index if not exists inchi_key_1_idx on ncct_lookup(inchi_key_1);");
			stat.executeUpdate("create index if not exists name_idx on ncct_lookup(name collate nocase);");
			logger.info("Done.");

			// Delete zero-sized error files
			if (sdfErrPath != null) {
				File f = new File(sdfErrPath);
				if (f.length() == 0)
					f.delete();
			}

			return counter;

		} finally {
			try {
				if (rCdk != null)
					rCdk.close();
				if (rIndigo != null)
					rIndigo.close();
			} catch (Exception ex) {
			}
			try {
				if (prep != null && !prep.isClosed())
					prep.close();
				if (stat != null && !stat.isClosed())
					stat.close();
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (Exception ex) {
			}
		}
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
		synchronized (ResolverDb.class) {
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
		assureDbIsOpen();
//		System.out.println(query);

		ResultSet rs = null;
		try {
			ArrayList<DSSToxRecord> cdrs = new ArrayList<DSSToxRecord>();

			rs = stat.executeQuery(query);
			while (rs.next()) {
				DSSToxRecord cdr = new DSSToxRecord();
				cdr.cid = rs.getString("cid");
				cdr.sid = rs.getString("sid");
				cdr.gsid = rs.getString("gsid");
				cdr.name = rs.getString("name");
				cdr.cas = rs.getString("cas");
				cdr.smiles = rs.getString("smiles");
				cdr.inchi = rs.getString("inchi");
				cdr.inchiKey = rs.getString("inchi_key");
				cdr.inchiKey1 = rs.getString("inchi_key_1");
				cdrs.add(cdr);
			}

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

	public static synchronized ArrayList<DSSToxRecord> lookupByCAS(String cas) {
		return executeQuery("select * from " + TABLE_NAME + " where cas = '" + cas + "';");
	}

	public static synchronized ArrayList<DSSToxRecord> lookupByName(String name) {
		name=name.replace("'", "''");
		return executeQuery("select * from " + TABLE_NAME + " where name = '" + name + "';");
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
		return executeQuery("select * from " + TABLE_NAME + " where inchi_key = '" + inchiKey + "';");
	}
	
	
	public static synchronized ArrayList<DSSToxRecord> lookupByInChIKey1(String inchiKey) {
		return lookupByInChIKey1(inchiKey, true);
	}

	public static synchronized ArrayList<DSSToxRecord> lookupByInChIKey1(String inchiKey, boolean preferred) {
		if (inchiKey.length() > 14)
			inchiKey = inchiKey.substring(0, 14);
		if (!preferred)
			return executeQuery("select * from " + TABLE_NAME + " where inchi_key_1 = '" + inchiKey + "';");
		else
			return executeQuery("select * from " + TABLE_NAME + " where inchi_key_1 = '" + inchiKey
					+ "' and inchi not like '%/i%';");
	}

	public static synchronized ArrayList<DSSToxRecord> lookupBySMILES(String smiles) {
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		try {
			IAtomContainer m = sp.parseSmiles(smiles);
			Inchi inchi = CDKUtilities.generateInChiKey(m);
			ArrayList<DSSToxRecord> res = lookupByInChIKey(inchi.inchiKey);
			if ( res.size() == 0 )
				res = lookupByInChIKey1(inchi.inchiKey);
			return res;
		} catch (InvalidSmilesException ex) {
			return null;
		}
	}
	
	

	/**
	 * Look up in database by structure using inchi keys
	 * 
	 * Current database has a lot of nonstandard inchi keys- TODO- should they be nonstandard?
	 * 
	 * @param m
	 * @return
	 */
	public static synchronized ArrayList<DSSToxRecord> lookupByAtomContainer(AtomContainer m) {
		
		try {

			if (m.getAtomCount()==0) return null;
			
			boolean debug=false;
			
			Inchi inchi = CDKUtilities.generateInChiKey(m);
			String inchiKey=inchi.inchiKey;
			

			Inchi inchi2 = IndigoUtilities.generateInChiKey(m);
			String inchiKey2=inchi2.inchiKey;

			//TODO is it ok just to change S (standard) to N (nonstandard) to get a hit in NCCT's db???
			//TODO redo the database so that the NCCT records are all standard? Are they all standard?
			
			String inchiKeyN = getInchiN(inchiKey);
			
//			System.out.println("inchiKey="+inchiKey);
//			System.out.println("inchiKeyN="+inchiKeyN);
			
			ArrayList<DSSToxRecord> res=null;

			if (inchiKey!=null) {
				
				res=lookupByInChIKey(inchiKey);
				
				if (res.size()!=0) {
					if (debug) System.out.println("Found by CDK inchikey");
					return res;
				}
								
				res=lookupByInChIKey(inchiKeyN);

				if (res.size()!=0) {
					if (debug) System.out.println("Found by CDK inchikey with S changed to N");
					return res;
				}
			}
			
			
			String inchiKey2N = getInchiN(inchiKey2);
			
			if(inchiKey2!=null) {
				res=lookupByInChIKey(inchiKey2);
				
				if (res.size()!=0) {
					if (debug) System.out.println("Found by CDK indigo inchikey");
					return res;
				}
				
				res=lookupByInChIKey(inchiKey2N);
				
				if (res.size()!=0) {
					if (debug) System.out.println("Found by CDK indigo inchikey with S changed to N");
					return res;
				}
			}
			
			return res;
		
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}
	
	
	/**
	 * Look up in database by structure using inchi keys
	 * 
	 * Current database has a lot of nonstandard inchi keys- TODO- should they be nonstandard?
	 * 
	 * @param m
	 * @return
	 */
	public static synchronized ArrayList<DSSToxRecord> lookupByAtomContainer2dConnectivity(AtomContainer m) {
		
		try {

			if (m.getAtomCount()==0) return null;
			
			boolean debug=false;
			
			Inchi inchi = CDKUtilities.generateInChiKey(m);
			String inchiKey=inchi.inchiKey;
			

			Inchi inchi2 = IndigoUtilities.generateInChiKey(m);
			String inchiKey2=inchi2.inchiKey;

						
			ArrayList<DSSToxRecord> res=null;

			if (inchiKey!=null) {				
				res=lookupByInChIKey1(inchiKey,false);				
				if (res.size()!=0) {
					if (debug) System.out.println("Found by CDK inchikey");
					return res;
				}								
			}
			
								
			if(inchiKey2!=null) {
				res=lookupByInChIKey1(inchiKey2,false);				
				if (res.size()!=0) {
					if (debug) System.out.println("Found by CDK indigo inchikey");
					return res;
				}
			}
			
			return res;
		
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return null;
		}
	}

	private static String getInchiN(String inchiKey) {
		String inchiKeyN=inchiKey.substring(0,23)+"N"+inchiKey.substring(24,inchiKey.length());
		return inchiKeyN;
	}

	public static synchronized ArrayList<DSSToxRecord> lookup(String id) {
		return lookup(id, ChemIdType.AnyId);
	}

	public static synchronized ArrayList<DSSToxRecord> lookup(String id, ChemIdType idType) {
		switch (idType) {
		case SMILES:
			return lookupBySMILES(id);
		case CAS:
			return lookupByCAS(id);
		case Name:
			return lookupByName(id);
		case InChIKey:
			return lookupByInChIKey(id);
		case InChIKey_1:
			return lookupByInChIKey1(id);
		case AnyId: {
			ArrayList<DSSToxRecord> res;
			res = lookupByInChIKey(id);
			if (res.size() > 0)
				return res;

			if (id.length() == 14) { // Only when explicitly provided with InChIKey1
				res = lookupByInChIKey1(id);
				if (res.size() > 0)
					return res;
			}

			if (id.matches("\\d+-\\d+-\\d+")) {
				res = lookupByCAS(id);
				if (res.size() > 0)
					return res;
			}

			res = lookupByName(id);
			if (res.size() > 0)
				return res;

			res = lookupBySMILES(id);
			return res;
		}
		default:
			throw new UnsupportedOperationException();
		}
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
		CommandLine cli = parseCli(args);
		if (cli != null) {
			String input = cli.getOptionValue('i');
			String output = cli.getOptionValue('o');
			ChemInfToolkit tk = cli.hasOption('t') ? ChemInfToolkit.valueOf(cli.getOptionValue('t'))
					: ChemInfToolkit.CDK;
			if (!new File(input).isDirectory())
				updateNcctLookupDb(input, output, tk, cli.hasOption('e'));
			else {
				File dir = new File(input);
				File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".sdf");
					}
				});

				for (File file : files) {
					updateNcctLookupDb(file.getPath(), output, tk, cli.hasOption('e'));
				}
			}
		}
	}
}
