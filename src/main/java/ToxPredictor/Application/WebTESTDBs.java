package ToxPredictor.Application;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.Utilities.FormatUtils;
import ToxPredictor.Utilities.StringCompression;
import ToxPredictor.Utilities.TESTPredictedValue;
import gov.epa.api.TESTRecord;

public class WebTESTDBs {
	
	static void createDescriptorsDB(Connection connDescriptors,String primaryKey) throws SQLException {
//		String [] fields= {"CAS","Descriptors"};//Add inchii key???
		Statement stat=connDescriptors.createStatement();
		String [] fields=getFieldsDescriptors();
		MySQL_DB.create_table(stat,"Descriptors",fields,primaryKey);
	}
	
	
	static void createDescriptorsTableMySQL(Connection connDescriptors,String primaryKey) throws SQLException {
//		String [] fields= {"CAS","Descriptors"};//Add inchii key???
		Statement stat=connDescriptors.createStatement();
		String [] fields=getFieldsDescriptors();
		int [] lengths=getFieldLengthsDescriptors();
		MySQL_DB.create_table_mysql(stat,"Descriptors",fields,lengths,primaryKey);
	}

	
	
	static void addRecordsToDescriptorDatabase(Connection conn,DescriptorData dd,boolean compressDescriptors) {
		try {

			String [] fields=getFieldsDescriptors();
			String table="Descriptors";
			
			String s = create_sql_insert(fields, table);			
			PreparedStatement prep= conn.prepareStatement(s);

			int i=1;
			prep.setString(i++, dd.ID);
//			prep.setString(i++, dd.InChi);
			prep.setString(i++, dd.InChiKey);
			
			//			prep.setString(i++, dd.to_JSON_String(dd,true));
//			long t1=System.currentTimeMillis();
			String strJSON=dd.to_JSON_String(true);
			
//			System.out.println(dd.CAS+"\t"+strJSON);
			
//			long t2=System.currentTimeMillis();
//			logger.info("Time to convert to json string="+(t2-t1));
			
			
			if (compressDescriptors) {
				byte [] bytesJSON=StringCompression.compress(strJSON,Charset.forName("UTF-8"));
				prep.setBytes(i++, bytesJSON);
			} else {				
				prep.setString(i++, strJSON);
			}
			
			prep.setString(i++, dd.Error);
			
//			long t3=System.currentTimeMillis();
//			logger.info("Time to compress="+(t3-t2));
			
//			prep.addBatch();
//			int [] count=prep.executeBatch();
			
			prep.executeUpdate();
//			
//			long t4=System.currentTimeMillis();
//			logger.info("Time to execute update="+(t4-t3));
			
			//			System.out.println(count.length);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());

			//			ex.printStackTrace();
		}
	}

	private static String create_sql_insert(String[] fields, String table) {
		String s = "insert into "+table+" values (";

		for (int i = 1; i <= fields.length; i++) {
			s += "?";
			if (i < fields.length)
				s += ",";
		}
		s += ");";
		return s;
	}
	
	public static DescriptorData getDescriptors(Statement stat,String searchField,String searchValue,boolean compressDescriptorsInDB) {
//		long t1=System.currentTimeMillis();

		try {
			
			ResultSet rs = MySQL_DB.getRecords(stat,"Descriptors", searchField,searchValue);
			
			if (!rs.next()) return null;
			
			return getDescriptors(rs,compressDescriptorsInDB);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
	
	public static DescriptorData getDescriptors(ResultSet rs,boolean compressDescriptorsInDB) {

		try {
			
			String strDescriptors="Descriptors";
			
			int colNum = getColNum(rs, strDescriptors);

			String strJSON=null;
			if (compressDescriptorsInDB) {
				byte [] bytes=rs.getBytes(colNum);
				strJSON=StringCompression.decompress(bytes, Charset.forName("UTF-8"));
			} else {
				strJSON=rs.getString(colNum);
			}

//			System.out.println(strJSON);
			
			
			if (strJSON==null || strJSON.isEmpty()) return null;
			
			DescriptorData dd=DescriptorData.loadFromJSON(strJSON);
			return dd;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}


	private static int getColNum(ResultSet rs, String fieldName) throws SQLException {
		int colNum=-1;
		for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
			if (rs.getMetaData().getColumnName(i).equals(fieldName)) {
				colNum=i;
				break;
			}
		}
		return colNum;
	}
	
	
	
	private static String [] getFieldsDescriptors() {
		
		ArrayList<String> fields=new ArrayList<String>();

		fields.add("CAS");
//		fields.add("InChi");
		fields.add("InChiKey");
		fields.add("Descriptors");
		fields.add("error");
				
		String[] strFields = new String[fields.size()];
		strFields = fields.toArray(strFields);

		return strFields;

	}
	
	
	private static int [] getFieldLengthsDescriptors() {
		
		ArrayList<Integer> fields=new ArrayList<Integer>();

		fields.add(15);//CAS
//		fields.add("InChi");
		fields.add(30);//InChiKey
		fields.add(25000);//Descriptors- should we zip it?
		fields.add(100);//error
				
		int[] intLength = new int[fields.size()];
		
		for (int i=0;i<fields.size();i++) {
			intLength[i]=fields.get(i);
		}
		
		return intLength;

	}
	
	private static int [] getFieldLengthsTESTPredictions(String endpoint) {
		
		ArrayList<Integer> fields=new ArrayList<Integer>();

		int sizeID=15;
		int sizeNumber=15;
		int sizeInChiKey=30;
		int sizeSmiles=200;

		fields.add(sizeID);//CAS
		fields.add(sizeID);//gsid
		fields.add(sizeID);//DSSTOXSID
		fields.add(sizeID);//DSSTOXCID
		fields.add(sizeNumber);//Molecular weight
//		fields.add(xxx);//InChi
		fields.add(sizeInChiKey);//InChiKey//		
		fields.add(sizeSmiles);//Smiles
		fields.add(sizeNumber);//ExpToxCAS
		fields.add(sizeNumber);//ExpToxValue
		fields.add(sizeNumber);//Hierarchical
		if (TESTConstants.haveSingleModelMethod(endpoint)) fields.add(sizeNumber);//SingleModel
		if (TESTConstants.haveGroupContributionMethod(endpoint)) fields.add(sizeNumber);//Group contribution
		fields.add(sizeNumber);//NearestNeighbor
		fields.add(sizeNumber);//Consensus
		fields.add(100);//error
		
		int[] intLength = new int[fields.size()];
		
		for (int i=0;i<fields.size();i++) {
			intLength[i]=fields.get(i);
		}
		
		return intLength;

	}

	
	private static String [] getFieldsPredictionsFormat1(String endpoint) {

		ArrayList<String> fields=new ArrayList<String>();

		fields.add("CAS");
		//		fields.add("endpoint");
		fields.add("method");

		if (TESTConstants.isBinary(endpoint)) {
			fields.add("expActive"); 
			fields.add("predActive");
			fields.add("expValLogMolar"); 
			fields.add("predValLogMolar");
		} else {
			fields.add("expValMass");
			fields.add("predValMass");

			if (TESTConstants.isLogMolar(endpoint)) {
				fields.add("expValLogMolar"); 
				fields.add("predValLogMolar");
			}
		}

		String[] strFields = new String[fields.size()];
		strFields = fields.toArray(strFields);
		return strFields;
	}


	private static String [] getFieldsPredictionsFormat2(String endpoint) {

		ArrayList<String> fields=new ArrayList<String>();

		fields.add("CAS");
		fields.add("gsid");
		fields.add("DSSTOXSID");
		fields.add("DSSTOXCID");
		fields.add("MolecularWeight");
//		fields.add("InChi");
		fields.add("InChiKey");
		fields.add("SMILES");
		
		fields.add("ExpToxCAS");
		fields.add("ExpToxValue");
		fields.add("Hierarchical");
		if (TESTConstants.haveSingleModelMethod(endpoint)) fields.add("SingleModel");
		if (TESTConstants.haveGroupContributionMethod(endpoint)) fields.add("GroupContribution");
		fields.add("NearestNeighbor");
		fields.add("Consensus");
		fields.add("error");
		
		String[] strFields = new String[fields.size()];
		strFields = fields.toArray(strFields);
		return strFields;
	}
	
	
	static Connection createPredictionsDB(String databasePath,int outputDatabaseFormat,CalculationParameters params,String primaryKey) throws SQLException {
		Connection conn;
		conn=MySQL_DB.getConnection(databasePath);
		Statement stat=conn.createStatement();

		for (String endpoint: params.endpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

			if (outputDatabaseFormat==1) {
				String [] fields=getFieldsPredictionsFormat1(endpoint);
				MySQL_DB.create_table_key_with_duplicates(stat,abbrev,fields,"CAS");//no primary key for format 1
			} else if (outputDatabaseFormat==2) {
				String [] fields=getFieldsPredictionsFormat2(endpoint);
				MySQL_DB.create_table(stat,abbrev,fields,primaryKey);
				//TODO should we set other DSSTOX fields as keys so can search by fields other than CAS quickly? 
			}
		}
		return conn;
	}
	
	static Connection createPredictionsTables(Connection conn,CalculationParameters params,String primaryKey) throws SQLException {
		
		Statement stat=conn.createStatement();

		for (String endpoint:params.endpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

			String [] fields=getFieldsPredictionsFormat2(endpoint);
			
			int [] lengths=getFieldLengthsTESTPredictions(endpoint);
			
			MySQL_DB.create_table_mysql(stat,abbrev,fields,lengths,primaryKey);
			
		}
		return conn;
	}
	
	static Connection createPredictionsTables(Connection conn,String [] endpoints,String primaryKey) throws SQLException {
		
		Statement stat=conn.createStatement();

		for (String endpoint:endpoints) {
			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);

			String [] fields=getFieldsPredictionsFormat2(endpoint);
			
			int [] lengths=getFieldLengthsTESTPredictions(endpoint);
			
			MySQL_DB.create_table_mysql(stat,abbrev,fields,lengths,primaryKey);
			
		}
		return conn;
	}
	
	static void addRecordsToDatabaseFormat1(Connection conn,List<TESTPredictedValue> results,String endpoint) {
		try {

			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			String [] fields=getFieldsPredictionsFormat1(endpoint);

			String s = create_sql_insert(fields, abbrev);
			PreparedStatement prep = conn.prepareStatement(s);


			for (TESTPredictedValue tpv:results) {

				//				System.out.println(tpv.id);

				int i=1;

				prep.setString(i++, tpv.id);
				prep.setString(i++, tpv.method);

				if (TESTConstants.isBinary(endpoint)) {

					if (tpv.expActive!=null) {
						prep.setString(i++, tpv.expActive+"");
					} else {
						prep.setString(i++, "N/A");
					}

					prep.setBoolean(i++, tpv.predActive);

					if (tpv.expValLogMolar!=null) {
						prep.setString(i++, FormatUtils.toD3(tpv.expValLogMolar));	
					} else {
						i++;
					}

					prep.setString(i++, FormatUtils.toD3(tpv.predValLogMolar));

				} else {
					prep.setString(i++, FormatUtils.toD3(tpv.expValMass));
					prep.setString(i++, FormatUtils.toD3(tpv.predValMass));

					if (TESTConstants.isLogMolar(endpoint)) {
						prep.setString(i++, FormatUtils.toD3(tpv.expValLogMolar));
						prep.setString(i++, FormatUtils.toD3(tpv.predValLogMolar));
					}
				}
				prep.addBatch();

			}

//			int [] count=prep.executeBatch();//do what'
			//System.out.println(count.length);
			
			prep.executeBatch();
			

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}
	
	
	static void addRecordsToDatabaseFormat2(Connection conn,TESTRecord tr,String endpoint) {
		try {

			String abbrev=TESTConstants.getAbbrevEndpoint(endpoint);
			String [] fields=getFieldsPredictionsFormat2(endpoint);

			String s = create_sql_insert(fields, abbrev);
			PreparedStatement prep = conn.prepareStatement(s);

			int i=1;
			
			prep.setString(i++, tr.CAS);
			prep.setString(i++, tr.gsid);
			prep.setString(i++, tr.DSSTOXSID);
			prep.setString(i++, tr.DSSTOXCID);
			prep.setString(i++, tr.MolecularWeight);
			
//			prep.setString(i++, tr.InChi);
			prep.setString(i++, tr.InChiKey);
			prep.setString(i++, tr.SMILES);

			prep.setString(i++, tr.ExpToxCAS);
			prep.setString(i++, tr.ExpToxValue);

			prep.setString(i++, tr.Hierarchical);
			if (TESTConstants.haveSingleModelMethod(endpoint)) prep.setString(i++, tr.SingleModel);
			if (TESTConstants.haveGroupContributionMethod(endpoint)) prep.setString(i++, tr.GroupContribution);
			prep.setString(i++, tr.NearestNeighbor);
			prep.setString(i++, tr.Consensus);
			
			prep.setString(i++, tr.error);

//			System.out.println("here tr.error="+tr.error);
			
//			long t1=System.currentTimeMillis();
			
//			prep.addBatch();			
//			int [] count=prep.executeBatch();
			
			prep.executeUpdate();
			
//			long t2=System.currentTimeMillis();
//			logger.info("time to execute update:"+(t2-t1));
			
			
			//			System.out.println(count.length);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());

			//			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

