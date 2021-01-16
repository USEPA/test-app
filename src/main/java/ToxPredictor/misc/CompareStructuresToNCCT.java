package ToxPredictor.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import AADashboard.Application.MoleculeUtilities;
import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Inchi;

public class CompareStructuresToNCCT {

	void go() {
		
		try {
			
			FileWriter fw=new FileWriter("AADashboard calcs/comparison with ncct.txt");
			
			fw.write("CAS\tsmiles\tsmilesActor\tsmilesDatabase\tinchiKeyNRMRL\tinchiKeyActor\tinchiKeyDatabase\r\n");
			
			String filepath="jar/ValidatedStructures2d.jar";
			ZipFile zipfile = new ZipFile(filepath);
			
			FileInputStream fis=new FileInputStream(new File(filepath));
			
			ZipInputStream zip = new ZipInputStream(fis);
		    ZipEntry zipEntry = null;

		    boolean start=false;
		    
//		    String startCAS="2278-22-0";
		    
		    while( ( zipEntry = zip.getNextEntry() ) != null ) {
		        String entryName = zipEntry.getName();
		        
		        if (!entryName.contains(".mol")) continue;
		        
//		        if (entryName.contains(startCAS)) start=true;		        
//		        if (!start) continue;
		        
		        		        
		        InputStream is=zipfile.getInputStream(zipEntry);
		        
		        BufferedReader br = new BufferedReader(new InputStreamReader(is));
		        		      		       		        
//		        System.out.println(entryName);
		        
		        String molfile="";
		        while (true) {
		        	String Line=br.readLine();
		        	if (Line==null) break;
		        	molfile+=Line+"\r\n";
		        }
		        AtomContainerSet acs=MolFileUtilities.LoadFromSdfString(molfile);
		        AtomContainer ac=(AtomContainer)acs.getAtomContainer(0);
		        
		        String CAS=((String)ac.getProperty("CAS")).trim();
		        
		        String smiles="N/A";
		        
		        if (ac.getProperty("SMILES")!=null)
		        	smiles=((String)ac.getProperty("SMILES")).trim();
		        
		        
				Inchi inchi = CDKUtilities.generateInChiKey(ac);
				String inchiKeyNRMRL=inchi.inchiKey;
				
				DSSToxRecord dssToxRecordActor=MoleculeUtilities.getDSSToxRecordFromDashboard(CAS);				
				
				String inchiKeyActor="N/A";
				String smilesActor="N/A";
								
				if (dssToxRecordActor!=null) {
					inchiKeyActor=dssToxRecordActor.inchiKey;	
					smilesActor=dssToxRecordActor.smiles;
				} 
				
				ArrayList<DSSToxRecord>recs=ResolverDb.lookupByCAS(CAS);
				
				String inchiKeyDatabase="N/A";
				String smilesDatabase="N/A";
				
				if (recs.size()>0) {
					DSSToxRecord dssToxRecordDatabase=ResolverDb.lookupByCAS(CAS).get(0);
					smilesDatabase=dssToxRecordDatabase.smiles;
					inchiKeyDatabase=dssToxRecordDatabase.inchiKey;
				}
				
		        System.out.println(CAS+"\t"+smiles+"\t"+inchiKeyNRMRL+"\t"+inchiKeyActor+"\t"+inchiKeyDatabase);
		        fw.write(CAS+"\t"+smiles+"\t"+smilesActor+"\t"+smilesDatabase+"\t"+inchiKeyNRMRL+"\t"+inchiKeyActor+"\t"+inchiKeyDatabase+"\r\n");
		        fw.flush();
		        
//		        if (true) break;
		    }
		    fis.close();
		    fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		CompareStructuresToNCCT c=new CompareStructuresToNCCT();
		c.go();

	}

}
