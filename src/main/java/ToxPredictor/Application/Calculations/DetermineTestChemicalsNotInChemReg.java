package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;


public class DetermineTestChemicalsNotInChemReg {

	static void go() {
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\scidataexperts_webtest_from_master\\data";
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		Vector<String>casList=new Vector<>();
		
		Vector<String>nameList=new Vector<>();
		Vector<String>namecas=new Vector<>();
		
		for (int i=0;i<files.length;i++) {
			if (!files[i].isDirectory()) continue;
			if (files[i].getName().contains("descriptors")) continue;
			
			File [] files2=files[i].listFiles();
			
			System.out.println(files[i].getName());
			for (int j=0;j<files2.length;j++) {
				
				if (!files2[j].getName().contains(".sdf")) continue;
				System.out.println("\t"+files2[j].getName());
				
				AtomContainerSet acs=LoadFromSDF(files2[j].getAbsolutePath());
				
				for (int k=0;k<acs.getAtomContainerCount();k++) {
					String CAS=acs.getAtomContainer(k).getProperty("CAS");
					String Name=acs.getAtomContainer(k).getProperty("Name");
					String NameCAS=Name+"\t"+CAS;		
					
					if (!casList.contains(CAS)) {						
						casList.add(CAS);
						nameList.add(Name);
					}
					
					if (!namecas.contains(NameCAS)) namecas.add(NameCAS);
				}							
			}
			
		}
		
//		int count=0;
//		
//		for (int i=0;i<casList.size();i++) {
//			String CAS=casList.get(i);
//			String Name=nameList.get(i);
//			
//			ArrayList<DSSToxRecord>recs=ResolverDb2.lookupByCAS(CAS);
//			
//			if (recs.size()==0) {
//				count++;
////				System.out.println(count+"\t"+CAS+"\t"+Name);
//			} else {
////				System.out.println(count+"\t"+CAS+"\t"+Name+"\tMatch");
//			}
//			
//		}
		
		System.out.println("\n");
		
		for (int i=0;i<casList.size();i++) {
			System.out.println(casList.get(i)+"\t"+nameList.get(i));
		}

//		for (int i=0;i<namecas.size();i++) {
//			System.out.println(namecas.get(i));
//		}

		System.out.println(casList.size()+"\t"+nameList.size());
		
	}
	
	public static AtomContainerSet LoadFromSDF(String filepath) {

		AtomContainerSet acs=new AtomContainerSet();

		try {
			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepath),DefaultChemObjectBuilder.getInstance());								
			
			
			while (mr.hasNext()) {
				
				
				AtomContainer m=null;
				
				try {
					m = (AtomContainer)mr.next();
					acs.addAtomContainer(m);

//					System.out.println(m.getAtomCount());
				
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				if (m==null || m.getAtomCount()==0) break;
				
				
			}// end while true;
			
			mr.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;			
		}
		return acs;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		go();
	}

}
