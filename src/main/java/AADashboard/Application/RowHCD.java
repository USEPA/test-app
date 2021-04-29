package AADashboard.Application;

import java.lang.reflect.Field;
import java.util.Vector;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;

public class RowHCD {
	String [] groups={"Identifiers","Human Health Effects","Ecotoxicity","Fate"};
	
	public String [] fieldNamesIdentifiers = {"CAS","name"};
	
	public String[] fieldNamesHumanHealthEffects = { Chemical.strAcute_Mammalian_Toxicity, Chemical.strCarcinogenicity,
			Chemical.strGenotoxicity_Mutagenicity, Chemical.strEndocrine_Disruption, Chemical.strReproductive,
			Chemical.strDevelopmental, Chemical.strNeurotoxicity, Chemical.strSystemic_Toxicity,
			Chemical.strSkin_Sensitization, Chemical.strSkin_Irritation, Chemical.strEye_Irritation };

	public String [] fieldNamesEcotoxicity= {Chemical.strAcute_Aquatic_Toxicity,Chemical.strChronic_Aquatic_Toxicity};
	public String [] fieldNamesFate= {Chemical.strPersistence,Chemical.strBioaccumulation};
	
	
	public String[] fieldNamesAcuteMammalianToxicity = { Chemical.strAcute_Mammalian_ToxicityOral,
			Chemical.strAcute_Mammalian_ToxicityInhalation, Chemical.strAcute_Mammalian_ToxicityDermal };
	public String[] fieldNamesNeurotoxicity = { Chemical.strNeurotoxicity_Repeat_Exposure,
			Chemical.strNeurotoxicity_Single_Exposure };
	public String[] fieldNamesSystemicToxicity = { Chemical.strSystemic_Toxicity_Repeat_Exposure,
			Chemical.strSystemic_Toxicity_Single_Exposure };

	static boolean debug=false;

	/**
	 * Gets values for a row in the final scores table
	 * 
	 * @param chemical
	 * @return
	 */
	static Vector<KeyValue>getValues(Chemical chemical) {
		
		Vector<KeyValue>values=new Vector<KeyValue>();
		RowHCD r=new RowHCD();
		
		for (String group:r.groups) {
			if (debug) System.out.println(group);
			try {
				Field myField = r.getClass().getField("fieldNames"+group.replace(" ",""));
				
				String[] fieldNames = (String[]) myField.get(r);
				
				for (String fieldName:fieldNames) {
					if (debug) System.out.println("\t"+fieldName);
					
					if (group.contentEquals("Identifiers")) {
						Field field = chemical.getClass().getField(fieldName);
						String value = (String)field.get(chemical);						
						values.add(new KeyValue(fieldName,value));
					} else {
						if (chemical.getScore(fieldName)!=null) {
							Score score=chemical.getScore(fieldName);
							values.add(new KeyValue(fieldName,score.final_score));
							if (debug) System.out.println("\t\t"+score.final_score);
						} else {
							Field myField2 = r.getClass().getField("fieldNames"+fieldName.replace(" ",""));
							
							String[] fieldNames2 = (String[]) myField2.get(r);
							
							for (String fieldName2:fieldNames2) {
								Score score=chemical.getScore(fieldName2);
								values.add(new KeyValue(fieldName2,score.final_score));
								if (debug) System.out.println("\t\t"+score.final_score);
							}
							
						}
						
					}
				}
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
		return values;
		
	}
	static class KeyValue {
		String key;
		String value;
		KeyValue(String key,String value) {
			this.key=key;
			this.value=value;
		}
		
	}

	public static void main(String[] args) {
		Chemicals chemicals=new Chemicals();
		Chemical chemical = TableGeneratorExcel.createSampleChemical();
		chemicals.add(chemical);
		
		Vector<KeyValue>rowValues=getValues(chemical);
		
		for (int i=0;i<rowValues.size();i++) {
			KeyValue kv=rowValues.get(i);
			System.out.println(i+"\t"+kv.key+"\t"+kv.value);
					
		}
		
	}
}






