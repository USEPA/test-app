package gov.epa.api;



public class TESTRecord {
	public String CAS;
	public String gsid;
	public String DSSTOXSID;
	public String DSSTOXCID;
	public String MolecularWeight;
//	public String InChi;
	public String InChiKey;
	public String SMILES;

	//TODO add inchii key for structure searching?
	
	public String ExpToxCAS;
	public String ExpToxValue;
	public String Hierarchical;
	public String SingleModel;
	public String GroupContribution;
	public String NearestNeighbor;
	public String Consensus;
	
	public String error;//error in molecule (salt, bad element)  or in descriptor calculation (descriptor calc timed out)

	String[] fields = { "CAS", "gsid", "DSSTOXSID", "DSSTOXCID", "ExpToxValue", "Hierarchical", "SingleModel",
			"GroupContribution", "NearestNeighbor", "Consensus" };
	

	public TESTRecord(String CAS, String gsid, String DSSTOXSID, String DSSTOXCID, String MolecularWeight,String InChiKey,String SMILES) {
		
		this.CAS=CAS;
		this.gsid=gsid;
		this.DSSTOXSID=DSSTOXSID;
		this.DSSTOXCID=DSSTOXCID;
		this.MolecularWeight=MolecularWeight;
//		this.InChi=InChi;
		this.InChiKey=InChiKey;
		this.SMILES=SMILES;
				
		// TODO Auto-generated constructor stub
	}



	public TESTRecord() {
		// TODO Auto-generated constructor stub
	}




	public String toString() {

		String str = CAS + "\t" + gsid + "\t" + DSSTOXSID + "\t" + DSSTOXCID + "\t" + ExpToxValue + "\t"
				+ Hierarchical + "\t" + SingleModel + "\t" + GroupContribution + "\t" + NearestNeighbor + "\t"
				+ Consensus;

		return str;

	}
}
