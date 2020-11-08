package ToxPredictor.Database;

import org.openscience.cdk.AtomContainer;

import ToxPredictor.Application.Calculations.TaskStructureSearch;

public class DSSToxRecord {
	public String cas = "";
	public String name = "";
	public String cid = "";
	public String sid = "";
	public String gsid = "";
	public String smiles = "";
	public String inchi = "";
	public String inchiKey = "";
	public String inchiKey1 = "";

	public static String strCAS = "CAS";//Need it to be capitalized for things like the batch table
	public static String strName = "name";
	public static String strCID="cid";
	public static String strSID = "sid";
	public static String strGSID = "gsid";
	public static  String strSmiles = "smiles";
	public static String strInchi = "inchi";
	public static String strInchiKey = "inchiKey";
	public static String strInchiKey1 = "inchiKey1";

	public static String[] fieldNames = { strCAS, strName, strCID, strSID, strGSID, strSmiles, strInchi, strInchiKey,
			strInchiKey1 };
	
	public static DSSToxRecord createDSSToxRecord(AtomContainer m) throws Exception {
		DSSToxRecord rec;
		rec=new DSSToxRecord();
		rec.cid=m.getProperty(strCID);
		rec.sid=m.getProperty(strSID);
		rec.gsid=m.getProperty(strGSID);
		rec.name=m.getProperty(strName);
		rec.cas=m.getProperty(strCAS);
		rec.smiles=m.getProperty(strSmiles);
		rec.inchi=m.getProperty(strInchi);
		rec.inchiKey=m.getProperty(strInchiKey);
		rec.inchiKey1=m.getProperty(strInchiKey1);
		return rec;
	}
	
	
	
	public static void assignFromDSSToxRecord(AtomContainer m, DSSToxRecord rec) {
	
		m.setProperty(strCAS, rec.cas);
		m.setProperty(strName, rec.name);
		m.setProperty(strCID,rec.cid);//store gsid so dont need to look up later
		m.setProperty(strSID,rec.sid);//store gsid so dont need to look up later
		m.setProperty(strGSID,rec.gsid);//store gsid so dont need to look up later
		m.setProperty(strInchi, rec.inchi);
		m.setProperty(strInchiKey, rec.inchiKey);
		m.setProperty(strInchiKey1, rec.inchiKey1);
		m.setProperty(strSmiles, rec.smiles);
	}

	public static void clearProperties(AtomContainer m) {
//		m.setProperty(strCAS, "C_"+System.currentTimeMillis());
		TaskStructureSearch.assignIDFromStructure(m);
		
		m.setProperty(strName, null);
		m.setProperty(strCID,null);//store gsid so dont need to look up later
		m.setProperty(strSID,null);//store gsid so dont need to look up later
		m.setProperty(strGSID,null);//store gsid so dont need to look up later
		m.setProperty(strInchi, null);
		m.setProperty(strInchiKey, null);
		m.setProperty(strInchiKey1, null);
		m.setProperty(strSmiles, null);
	}
	
	public String toString() {
		return cas + "\t" + gsid + "\t" + name;
	}

}
