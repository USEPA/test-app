package ToxPredictor.Database;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.inchi.INChIContentProcessorTool;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.WebTEST;

public class ChemistryDashboardRecord2 {
	public String casrn;
	public String name;// preferred_name;
	public String smiles;// Canonical_QSARr
	public String inchi;// InCHI_Code_QSARr

	private static final Logger logger = LogManager.getLogger(ChemistryDashboardRecord2.class);

	public String toString() {

		return casrn + "\t" + name + "\t" + smiles + "\t" + inchi;

	}

	public static ChemistryDashboardRecord2 lookupDashboardRecordbyCAS(String CAS) {
		return lookupDashboardRecord("casrn", CAS);
	}

	/**
	 * Look up Chemistry Dashboard record from sqlite database
	 * 
	 * Note: works fine as long as CAS is set up as the primary key in the database
	 * 
	 * @param statTEST_Results
	 * @param keyField
	 * @param keyValue
	 * @return
	 */
	public static ChemistryDashboardRecord2 lookupDashboardRecord(String keyField, String keyValue) {

		try {

			String tableName = "NCCT_ID";

//			String query="select * from "+tableName+" where "+keyField+" = \""+keyValue+"\";";
//			System.out.println(query);
//			ResultSet rs = stat.executeQuery(query);
//			ResultSetMetaData rsmd = rs.getMetaData();
//			
//			ChemistryDashboardRecord cdr=new ChemistryDashboardRecord();
//			
//			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//					
//				String colName=rsmd.getColumnName(i);
//				String colVal=rs.getString(i);
//										
//				if (colName.equals("casrn")) {
//					cdr.casrn=colVal;
//				} else if (colName.equals("gsid")) {
//					cdr.gsid=colVal;
//				} else if (colName.equals("dsstox_substance_id")) {
//					cdr.dsstox_substance_id=colVal;
//				} else if (colName.equals("dsstox_compound_id")) {
//					cdr.dsstox_compound_id=colVal;
//				} 
//				//TODO- add rest of fields to the ChemistryDataRecord class?
//			
//			}
			Statement stat = MySQL_DB.getStatement(WebTEST.DB_Path_NCCT_ID_Records);
			if (stat == null) {
				return null;
			}

			String name;//
			String query = "select casrn, preferred_name, Canonical_QSARr, inCHI_Code_QSARr  from " + tableName
					+ " where " + keyField + " = \"" + keyValue + "\";";
			ResultSet rs = stat.executeQuery(query);

			if (rs.isClosed())
				return null;

			ChemistryDashboardRecord2 cdr = new ChemistryDashboardRecord2();
			cdr.casrn = rs.getString(1);
			cdr.name = rs.getString(2);
			cdr.smiles = rs.getString(3);
			cdr.inchi = rs.getString(4);

			rs.close();
			stat.close();

			return cdr;

		} catch (Exception ex) {
			logger.debug(keyValue + " cant find");
			logger.catching(ex);
			return null;
		} finally {

		}
	}

	/**
	 * Parse inchi into AtomContainer
	 * 
	 * @param inchi
	 * @return
	 */
	public static AtomContainer getAtomContainer(String inchi, String CAS) {

		ChemFile cf = new ChemFile();
		INChIContentProcessorTool inchiTool = new INChIContentProcessorTool();

		// ok, we need to parse things like:
		// INChI=1.12Beta/C6H6/c1-2-4-6-5-3-1/h1-6H
		final String INChI = inchi.substring(6);
		StringTokenizer tokenizer = new StringTokenizer(INChI, "/");
		// ok, we expect 4 tokens
		tokenizer.nextToken(); // 1.12Beta not stored since never used
		final String formula = tokenizer.nextToken(); // C6H6
		String connections = null;
		if (tokenizer.hasMoreTokens()) {
			connections = tokenizer.nextToken().substring(1); // 1-2-4-6-5-3-1
		}
		// final String hydrogens = tokenizer.nextToken().substring(1); // 1-6H

		IAtomContainer parsedContent = inchiTool.processFormula(cf.getBuilder().newInstance(IAtomContainer.class),
				formula);
		if (connections != null)
			inchiTool.processConnections(connections, parsedContent, -1);

		parsedContent.setProperty("CAS", CAS);

		return (AtomContainer) parsedContent;

	}

	public static void main(String[] args) throws Exception {
		String CAS = "91-20-3";

		ChemistryDashboardRecord2 c = ChemistryDashboardRecord2.lookupDashboardRecordbyCAS(CAS);

//		InputStream in = IOUtils.toInputStream(c.inchi);
//		INChIPlainTextReader ir=new INChIPlainTextReader(in);
//		ChemFile cf=new ChemFile();
//		ir.read(cf);
//		System.out.println(cf.getChemSequenceCount());
		System.out.println(c.inchi);
		AtomContainer ac = c.getAtomContainer(c.inchi, CAS);
		System.out.println(ac.getAtomCount() + "\t" + ac.getBondCount());

	}

}
