package ToxPredictor.StructureDataDownload;

import java.lang.reflect.Field;

public class RecordChemIDplus{
	String InChI;
	String InChIKey;
	String Smiles;
	String Substance_Name;
//	String Name_of_Substance;
	String Systematic_Name;
	String CAS_Registry_Number;
	String Other_Registry_Numbers;
	String Synonyms;
	
	static String [] fieldNames= {"CAS_Registry_Number","Substance_Name","Systematic_Name","Synonyms","InChI","InChIKey","Smiles","Other_Registry_Numbers"};
	
	static String getHeader(String d) {
		// TODO Auto-generated method stub

		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
//			Line += "\""+fieldNames[i]+"\"";
			Line += fieldNames[i];
			if (i < fieldNames.length - 1) {
				Line += d;
			} 
		}

		return Line;
	}
	
	public String toString(String del) {
		
		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
			try {
			
				
				Field myField = this.getClass().getDeclaredField(fieldNames[i]);
				
				String val=null;
				
				if (myField.get(this)==null) {
//					val="\"\"";
					val="";
				} else {
//					val="\""+(String)myField.get(this)+"\"";
					val=(String)myField.get(this);
				} 
				
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");
				

				if (val.contains(del)) {
					System.out.println(CAS_Registry_Number+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
				}
				
				Line += val;
				if (i < fieldNames.length - 1) {
					Line += del;
				}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Line;

	}
	
}