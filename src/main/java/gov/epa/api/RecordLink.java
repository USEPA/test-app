package gov.epa.api;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import AADashboard.Application.MySQL_DB;

public class RecordLink{
	public String CAS;
	public String Name;
	public String SourceName;
	public String LinkName;
	public String URL;
	
	public static String [] fieldNames= {"CAS","Name","SourceName","LinkName","URL"};
	public static String sourceName="MDH Links";
	
	
	public String toString(String del) {
		return CAS+del+Name+del+SourceName+del+LinkName+del+URL;
	}
	
	public Vector<String> toStringArray() {
		Vector<String>vals=new Vector<>();
		vals.add(CAS);
		vals.add(Name);
		vals.add(SourceName);
		vals.add(LinkName);
		vals.add(URL);
		return vals;
	}
	


	public static String getHeader(String del) {
		return "CAS"+del+"Name"+del+"SourceName"+del+"LinkName"+del+"URL";	
	}

	
	public  static RecordLink createRecord(ResultSet rs,RecordLink rl) {
		 for (int i = 0; i < fieldNames.length; i++) {
				try {
					Field myField = rl.getClass().getDeclaredField(fieldNames[i]);
					String val=rs.getString(i+1);
						
					if (val!=null) {
						myField.set(rl, val);
					} 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		 return rl;
	}
	
	public static void getLinks(Chemical chemical, Statement statAA_Dashboard_Records) {

		String sql="select * from Links where CAS=\""+chemical.CAS+"\" order by SourceName, LinkName";
		ResultSet rs=MySQL_DB.getRecords(statAA_Dashboard_Records, sql);
		
		try {
			while (rs.next()) {						 
				RecordLink rl=new RecordLink();
				createRecord(rs,rl);
				chemical.links.add(rl);
//				System.out.println(rl.toString("\t"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
