package ToxPredictor.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.WebTEST;

public class ChemistryDashboardRecord {

	public String casrn = "";
	public String gsid = "";
	public String dsstox_substance_id = "";
	public String dsstox_compound_id = "";
	public String preferred_name = "";
	public String cid = "";
	public String Salt_Solvent = "";
	public String Salt_Solvent_ID = "";
	public String Canonical_QSARr = "";
	public String InChI_Code_QSARr = "";
	public String InChI_Key_QSARr = "";

	private static final Logger logger = LogManager.getLogger(ChemistryDashboardRecord.class);

	public String toString() {

		return casrn + "\t" + gsid + "\t" + preferred_name;

	}

	// some reason it's faster if dont have these fields:

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
	public static synchronized ChemistryDashboardRecord lookupDashboardRecord(String keyField, String keyValue,
			Statement stat) {

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
			if (stat == null) {
				return null;
			}

			String query = "select casrn, gsid, dsstox_substance_id, dsstox_compound_id from " + tableName + " where "
					+ keyField + " = \"" + keyValue + "\";";
			ResultSet rs = stat.executeQuery(query);

			if (rs.isClosed())
				return null;

			ChemistryDashboardRecord cdr = new ChemistryDashboardRecord();
			cdr.casrn = rs.getString(1);
			cdr.gsid = rs.getString(2);
			cdr.dsstox_substance_id = rs.getString(3);
			cdr.dsstox_compound_id = rs.getString(4);

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
	 * Look up Chemistry Dashboard record from sqlite database
	 * 
	 * Note: works fine as long as CAS is set up as the primary key in the database
	 * 
	 * @param statTEST_Results
	 * @param keyField
	 * @param keyValue
	 * @return
	 */
	public static synchronized ChemistryDashboardRecord lookupDashboardRecordAll(String keyField, String keyValue,
			Statement stat) {

		try {

			String tableName = "NCCT_ID";

			String query = "select * from " + tableName + " where " + keyField + " = \"" + keyValue + "\";";
//			System.out.println(query);
			ResultSet rs = stat.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();

			if (rs.isClosed())
				return null;

			ChemistryDashboardRecord cdr = new ChemistryDashboardRecord();

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {

				String colName = rsmd.getColumnName(i);
				String colVal = rs.getString(i);

				if (colName.equals("casrn")) {
					cdr.casrn = colVal;
				} else if (colName.equals("gsid")) {
					cdr.gsid = colVal;
				} else if (colName.equals("dsstox_substance_id")) {
					cdr.dsstox_substance_id = colVal;
				} else if (colName.equals("dsstox_compound_id")) {
					cdr.dsstox_compound_id = colVal;
				} else if (colName.equals("preferred_name")) {
					cdr.preferred_name = colVal;
				} else if (colName.equals("cid")) {
					cdr.cid = colVal;
				} else if (colName.equals("Salt_Solvent")) {
					cdr.Salt_Solvent = colVal;
				} else if (colName.equals("Salt_Solvent_ID")) {
					cdr.Salt_Solvent_ID = colVal;
				} else if (colName.equals("Canonical_QSARr")) {
					cdr.Canonical_QSARr = colVal;
				} else if (colName.equals("InChI_Code_QSARr")) {
					cdr.InChI_Code_QSARr = colVal;
				} else if (colName.equals("InChI_Key_QSARr")) {
					cdr.InChI_Key_QSARr = colVal;
				} else {
					System.out.println(colName);
				}
				// TODO- add rest of fields to the ChemistryDataRecord class?

			}

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

	public static void main(String[] args) throws Exception {

		Connection conn = MySQL_DB.getConnection(WebTEST.DB_Path_NCCT_ID_Records);
		Statement stat = MySQL_DB.getStatement(conn);

//		ChemistryDashboardRecord c = ChemistryDashboardRecord.lookupDashboardRecord("casrn", "71-43-2",stat);
		ChemistryDashboardRecord c = ChemistryDashboardRecord.lookupDashboardRecordAll("casrn", "71-43-2", stat);
		System.out.println(c);
	}
}
