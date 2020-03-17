package gov.epa.api;

import java.lang.reflect.Field;


public class FlatFileRecord2 extends FlatFileRecord{
	
	public String CAS;
	public String name;
	
	public String hazard_name;
	
	public String source;// where the record came from
	public String score;// i.e. L,M,H,VH

	public String category;// i.e. Category 1
	public String hazard_code;// code for hazard, i.e. "H301"
	public String hazard_statement;// text based description of what hazard they think it is
	public String rationale;// why classification was assigned
	public String route;// i.e. oral, dermal, inhalation- used mainly for acute mammalian toxicity for
						// now
	public String note;// extra clarification that doesn't fit into above fields
	public String note2;// extra clarification that doesn't fit into above fields
	
	//Extra fields for pubchem:
	public String concentration;
	public String referenceNumber;
	public String URL;
	
	// **************************************************************************************
	public Double valueMass;// quantitative value in mass units such as mg/L
	public String valueMassUnits;
	public String valueMassOperator;// "<",">", or ""

	public static String[] fieldNames = { "CAS","referenceNumber","source","URL","name","concentration","hazard_name", "category", "hazard_code",
			"hazard_statement", "note"};

	
	
	
	public static String getHeader() {
		return getHeader("\t");
	}
	

	
	public static String getHeader(String d) {
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
	
	
	
	
	public String toString() {
		return toString("\t");
	}
	
	//convert to string by reflection:
	public String toString(String d) {
		
		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
			try {
			
				
				Field myField = this.getClass().getDeclaredField(fieldNames[i]);
				
				String val=null;
				
				if (fieldNames[i].equals("valueMass")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=(Double)myField.get(this)+"";
					}
					
				} else {
					if (myField.get(this)==null) {
//						val="\"\"";
						val="";
					} else {
//						val="\""+(String)myField.get(this)+"\"";
						val=(String)myField.get(this);
					} 
				}
				
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");
				
//				if (fieldNames[i].equals("note")) {
//					System.out.println(CAS+"\t"+source+"\t"+hazard_name+"\t"+val);
//				}

				if (val.contains(d)) {
					System.out.println(this.CAS+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
				}
				
				Line += val;
				if (i < fieldNames.length - 1) {
					Line += d;
				}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Line;

	}
	
}
