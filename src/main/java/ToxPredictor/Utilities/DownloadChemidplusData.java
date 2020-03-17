package ToxPredictor.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class DownloadChemidplusData {

	
	public  String typeSystematicName="SystematicName";
	public  String typeSynonyms="Synonyms";

	 String getSID(String CAS) {
//		String s1="http://www.ncbi.nlm.nih.gov/sites/entrez?term=";
//		String s2="~[synonym]&cmd=search&db=pcsubstance";
		
		String s1="https://www.ncbi.nlm.nih.gov/pcsubstance?term=";
		String s2="%7E%5Bsynonym%5D&cmd=search";
		
		
//		https://www.ncbi.nlm.nih.gov/pcsubstance?term=5894-60-0%7E%5Bsynonym%5D&amp;cmd=search
		
		String srch=s1+CAS+s2;
//		System.out.println(srch);
		
		try {
			java.net.URL myURL = new java.net.URL(srch);
			
			BufferedReader br
			   = new BufferedReader(new InputStreamReader(myURL.openStream()));

			
			String Line="";
			
			String CAS2=CAS.replace("-", "");
			
			boolean PreviousMismatch=false;
			String SID="";
			
			String bob="2D SDF:";
			boolean haveBob=false;
			
			while (true) {
				Line=br.readLine();
//				System.out.println(Line);
				if (Line==null) {
					break;
				}
				if (Line.indexOf("ChemIDplus")>-1) {
					break;
				}
				
				if (Line.indexOf(bob)>-1) {
					haveBob=true;
					break;
				}
				
			}
			
			br.close();
			
//			if (haveBob) {
//				bob="2D SDF: Display</a></li><li><a href=\"summary.cgi?sid=";
//				Line=Line.substring(Line.indexOf(bob)+bob.length(),Line.length());
//				String sid=Line.substring(0,Line.indexOf("&"));
////				System.out.println("*"+Line);
//				return sid;
//			} else {
//				if (Line!=null) return getSIDFromPubChemSearchResults(Line, CAS2);
//			}

//			System.out.println(Line);
			
			if (Line!=null) return getSIDFromPubChemSearchResults(Line, CAS2);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}
	
 String getSIDFromPubChemSearchResults(String Line,String CAS) {
		
		
//	System.out.println(Line);
	
		if (Line.indexOf("ChemIDplus</a>")==-1) {
			return "error";
		}
		
		Line=Line.substring(Line.indexOf("ChemIDplus</a>")+"ChemIDplus</a>".length(),Line.length());
		
		Line=Line.substring(Line.indexOf("SID: </dt><dd>")+"SID: </dt><dd>".length(),Line.length());
		
		String SID=Line.substring(0,Line.indexOf("<"));
//		System.out.println(SID);
		
		return SID;
		
		
		
	}
	
public  void GetMolFile(String CAS,String destFolder,String type,String nameType2d) {
		
//		String s1="http://www.ncbi.nlm.nih.gov/sites/entrez?term=";
//		String s2="~[synonym]&cmd=search&db=pcsubstance";
//		
//		String srch=s1+CAS+s2;
//		System.out.println(srch);
		
//		File destMolFile=new File(destFolder+"/"+CAS+".mol");
		
//		if (destMolFile.exists()) return;
		
//		System.out.println(destMolFile.exists()+"\t"+destMolFile.getAbsolutePath());
		
		try {
				
				if (type.equals("2d")) {
					String s3="https://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=";
//					
					
					String s4=null;
					if (nameType2d.equals(typeSystematicName)) {
						s4="&viewopt=PubChem&disopt=SaveSDF";//gives systematic names and hydrogens attached
					} else if (nameType2d.equals(typeSynonyms)) {
						s4="&disopt=SaveSDF";//gives synonyms but NO hydrogens attached to molecule
					}
					
					String SID=getSID(CAS);
					
//					System.out.println(SID);
					
					if (!SID.equals("error")) {
						String molfileURL=s3+SID+s4;
//						System.out.println(molfileURL);
						downloadStructure(CAS,molfileURL,destFolder);
					}
					
				} else if (type.equals("3d")) {
					
					
//					String cid=this.getCID(CAS);
////					System.out.println(CAS+"\t"+cid);
//					
//					String s3="http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=";
//					String s4="&disopt=3DSaveSDF";
//
//					if (!cid.equals("error")) {
//						String molfileURL=s3+cid+s4;	
////						System.out.println(molfileURL);
//						this.downloadStructure(CAS,molfileURL,destFolder);
//					}
					String molfileURL="https://chem.sis.nlm.nih.gov/chemidplus/mol3d/"+CAS;
					downloadMolFile(CAS,molfileURL,destFolder);
					
				}

		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}


 public static void downloadMolFile(String CAS,String molfileURL,String destFolder) {
	

	try {
		String strURL="https://chem.sis.nlm.nih.gov/chemidplus/mol3d/"+CAS;
		java.net.URL myURL = new java.net.URL(strURL);
		
		BufferedReader br
		= new BufferedReader(new InputStreamReader(myURL.openStream()));
		
		FileWriter fw=new FileWriter(destFolder+"/"+CAS+".mol");
		
		int counter=0;
		while (true) {
			String Line=br.readLine();
			
			if (Line==null) break;
			
//			System.out.println(counter+" " +Line);
			
			fw.write(Line+"\r\n");
			fw.flush();
			
			counter++;
		}
		
		br.close();
		fw.close();
	} catch (FileNotFoundException ex1) {
		System.out.println(CAS+" not found");
	} catch (Exception ex) {
		ex.printStackTrace();
	}
}
 void downloadStructure(String CAS,String url,String destFolder) {
	
	try {
		java.net.URL myURL = new java.net.URL(url);
//		System.out.println(url);
		
		File DestMolFile=new File(destFolder+"/"+CAS+".mol");
		BufferedReader br
		   = new BufferedReader(new InputStreamReader(myURL.openStream()));

		FileWriter fw=new FileWriter(DestMolFile);
		String Line="";
		while (true) {
			Line=br.readLine();
			if (Line==null) break;
//			System.out.println(Line);
			fw.write(Line+"\r\n");
		}
		fw.close();
		br.close();

		br= new BufferedReader(new FileReader(DestMolFile));
		br.readLine();
		Line=br.readLine();
		
		if (Line.indexOf("<head><title>PubChem Error Report</title>")>-1) {
			br.close();
			System.out.println(CAS+"\tnot found in chemidplus");
			
			DestMolFile.delete();
		} else {
			br.close();	
		}
//		<head><title>PubChem Error Report</title>
		
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}
}
