package ToxPredictor.StructureDataDownload;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class RecordSRS {
	
	private String subsKey;
	private String internalTrackingNumber;
	private String systematicName;
	private String epaIdentificationNumber = null;
	private String currentCasNumber;
	private String currentTaxonomicSerialNumber = null;
	private String epaName;
	private String substanceType;
	private String categoryClass;
	private String kingdomCode = null;
	private String iupacName;
	private String pubChemId;
	private String molecularWeight;
	private String molecularFormula;
	private String inchiNotation;
	private String smilesNotation;
	
	public static String []fieldNames= {"currentCasNumber","systematicName","iupacName","pubChemId","molecularFormula","inchiNotation","smilesNotation"};
	
	ArrayList < Object > classifications = new ArrayList < Object > ();
	ArrayList < Object > characteristics = new ArrayList < Object > ();
	ArrayList < Object > synonyms = new ArrayList < Object > ();
	ArrayList < Object > casNumbers = new ArrayList < Object > ();
	ArrayList < Object > taxonomicSerialNumbers = new ArrayList < Object > ();
	ArrayList < Object > relationships = new ArrayList < Object > ();

	// Getter Methods 
	public String getSubsKey() {
		return subsKey;
	}

	public String getInternalTrackingNumber() {
		return internalTrackingNumber;
	}

	public String getSystematicName() {
		return systematicName;
	}

	public String getEpaIdentificationNumber() {
		return epaIdentificationNumber;
	}

	public String getCurrentCasNumber() {
		return currentCasNumber;
	}

	public String getCurrentTaxonomicSerialNumber() {
		return currentTaxonomicSerialNumber;
	}

	public String getEpaName() {
		return epaName;
	}

	public String getSubstanceType() {
		return substanceType;
	}

	public String getCategoryClass() {
		return categoryClass;
	}

	public String getKingdomCode() {
		return kingdomCode;
	}

	public String getIupacName() {
		return iupacName;
	}

	public String getPubChemId() {
		return pubChemId;
	}

	public String getMolecularWeight() {
		return molecularWeight;
	}

	public String getMolecularFormula() {
		return molecularFormula;
	}

	public String getInchiNotation() {
		return inchiNotation;
	}

	public String getSmilesNotation() {
		return smilesNotation;
	}

	// Setter Methods 

	public void setSubsKey(String subsKey) {
		this.subsKey = subsKey;
	}

	public void setInternalTrackingNumber(String internalTrackingNumber) {
		this.internalTrackingNumber = internalTrackingNumber;
	}

	public void setSystematicName(String systematicName) {
		this.systematicName = systematicName;
	}

	public void setEpaIdentificationNumber(String epaIdentificationNumber) {
		this.epaIdentificationNumber = epaIdentificationNumber;
	}

	public void setCurrentCasNumber(String currentCasNumber) {
		this.currentCasNumber = currentCasNumber;
	}

	public void setCurrentTaxonomicSerialNumber(String currentTaxonomicSerialNumber) {
		this.currentTaxonomicSerialNumber = currentTaxonomicSerialNumber;
	}

	public void setEpaName(String epaName) {
		this.epaName = epaName;
	}

	public void setSubstanceType(String substanceType) {
		this.substanceType = substanceType;
	}

	public void setCategoryClass(String categoryClass) {
		this.categoryClass = categoryClass;
	}

	public void setKingdomCode(String kingdomCode) {
		this.kingdomCode = kingdomCode;
	}

	public void setIupacName(String iupacName) {
		this.iupacName = iupacName;
	}

	public void setPubChemId(String pubChemId) {
		this.pubChemId = pubChemId;
	}

	public void setMolecularWeight(String molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public void setInchiNotation(String inchiNotation) {
		this.inchiNotation = inchiNotation;
	}

	public void setSmilesNotation(String smilesNotation) {
		this.smilesNotation = smilesNotation;
	}
	

	public static String getHeader(String del) {
		// TODO Auto-generated method stub

		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
//			Line += "\""+fieldNames[i]+"\"";
			Line += fieldNames[i];
			if (i < fieldNames.length - 1) {
				Line += del;
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
					System.out.println(this.getCurrentCasNumber()+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
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