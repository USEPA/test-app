package AADashboard.Application;

import java.lang.reflect.Field;
import java.util.Vector;

import gov.epa.api.Chemical;
import gov.epa.api.Chemicals;
import gov.epa.api.Score;

public class RowHCD {
	
	public static final String [] groups={"Identifiers","Human Health Effects","Ecotoxicity","Fate"};
	
	public static final String [] fieldNamesIdentifiers = {"CAS","name"};
	
	public static final String[] fieldNamesHumanHealthEffects = { Chemical.strCarcinogenicity,
			Chemical.strGenotoxicity_Mutagenicity, Chemical.strEndocrine_Disruption, Chemical.strReproductive,
			Chemical.strDevelopmental, };

	public static final String [] fieldNamesEcotoxicity= {Chemical.strAcute_Aquatic_Toxicity,Chemical.strChronic_Aquatic_Toxicity};
	public static final String [] fieldNamesFate= {Chemical.strPersistence,Chemical.strBioaccumulation};
	
	
	public static final String[] fieldNamesAcuteMammalianToxicity = { Chemical.strAcute_Mammalian_ToxicityOral,
			Chemical.strAcute_Mammalian_ToxicityInhalation, Chemical.strAcute_Mammalian_ToxicityDermal, 
			Chemical.strSkin_Sensitization, Chemical.strSkin_Irritation, Chemical.strEye_Irritation
	};
	public static final String[] fieldNamesNeurotoxicity = { Chemical.strNeurotoxicity_Repeat_Exposure,
			Chemical.strNeurotoxicity_Single_Exposure };
	public static final String[] fieldNamesSystemicToxicity = { Chemical.strSystemic_Toxicity_Repeat_Exposure,
			Chemical.strSystemic_Toxicity_Single_Exposure };

	static boolean debug=false;
	
	
	public static int getColumnCount(Group group) {
		
		int count=0;
		
		for (HazardCategory cat:group.categories) {
			if (cat instanceof HazardCategorySpecific) count++;
			else if (cat instanceof HazardCategoryGeneral) {
				HazardCategoryGeneral hcg=(HazardCategoryGeneral) cat;
				count+=hcg.categories.size();
			}
			
		}
		return count;
		
	}
	
	public static int getColumnCount(HazardCategory cat) {
		if (cat instanceof HazardCategorySpecific) return 1;
		else {
			HazardCategoryGeneral hcg=(HazardCategoryGeneral) cat;
			return hcg.categories.size();
		}
	}

	
	
	public static Vector <Group>createGroups() {
		Vector <Group> groups=new Vector<>();
		
		Group group1=new Group("Identifiers");		
		for (String fields:fieldNamesIdentifiers) 
			group1.categories.add(new HazardCategorySpecific(fields));
					
		Group group2=new Group("Human Health Effects");		
		HazardCategoryGeneral hcgAMT=new HazardCategoryGeneral("Acute Mammalian Toxicity");
		group2.categories.add(hcgAMT);
		for (String fields:fieldNamesAcuteMammalianToxicity) 
			hcgAMT.categories.add(new HazardCategorySpecific(fields));
		for (String fields:fieldNamesHumanHealthEffects) {
			group2.categories.add(new HazardCategorySpecific(fields));
		}

		HazardCategoryGeneral hcgNT=new HazardCategoryGeneral("Neurotoxicity");
		group2.categories.add(hcgNT);
		for (String fields:fieldNamesNeurotoxicity) 
			hcgNT.categories.add(new HazardCategorySpecific(fields));
		
		HazardCategoryGeneral hcgST=new HazardCategoryGeneral("Systemic Toxicity");
		group2.categories.add(hcgST);
		for (String fields:fieldNamesSystemicToxicity) 
			hcgST.categories.add(new HazardCategorySpecific(fields));
		
		Group group3=new Group("Ecotoxicity");
		for (String fields:fieldNamesEcotoxicity) 
			group3.categories.add(new HazardCategorySpecific(fields));

		Group group4=new Group("Fate");
		for (String fields:fieldNamesFate) 
			group4.categories.add(new HazardCategorySpecific(fields));
				
		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
		groups.add(group4);
		
		return groups;
	}
	
	
	public static class Group {
		String name;
		
		Vector<HazardCategory>categories=new Vector<>();
		
		
		public Group(String name) {
			this.name=name;
		}
	}
	
	public static class HazardCategory {
		String name;
		
		public HazardCategory(String name) {
			this.name=name;
		}
	}
	
	public static class HazardCategoryGeneral extends HazardCategory{
		public HazardCategoryGeneral(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}

		Vector<HazardCategorySpecific>categories=new Vector<>();
	}
	
	public static class HazardCategorySpecific extends HazardCategory {

		public HazardCategorySpecific(String name) {
			super(name);
			// TODO Auto-generated constructor stub
		}
	}

	
	

	/**
	 * Gets values for a row in the final scores table
	 * 
	 * @param chemical
	 * @return
	 */
	static Vector<KeyValue>getValues(Chemical chemical) {
		
		Vector<KeyValue>values=new Vector<KeyValue>();
		RowHCD r=new RowHCD();
		
		for (String group:groups) {
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






