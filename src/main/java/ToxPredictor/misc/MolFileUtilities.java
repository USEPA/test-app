package ToxPredictor.misc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ToxPredictor.Application.Calculations.TaskStructureSearch;
//import ToxPredictor.Application.fraStructureDatabaseSearch7;
import ToxPredictor.MyDescriptors.AtomicProperties;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.ChemicalFinder;
import ToxPredictor.Utilities.Utilities;



public class MolFileUtilities {

	String NIHMolFolder = "N:/NRMRL-PRIV/CompTox/3Dcoordinates/database/coords";

	String DougMolFolder = "N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol";

	String ChemidplusMolFolder = "N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol-chemidplus-3d";


	
	
	public AtomContainer LoadChemicalFromMolFileInJar(String filePath) {
		
		try {

			InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filePath);
			
			MDLV2000Reader mr=new MDLV2000Reader(ins); 			
			AtomContainer ac=new AtomContainer();
			mr.read(ac);

			return ac;

		} catch (Exception e) {
//			e.printStackTrace();
			return null;
		}

	}
	
	public String loadChemicalFromMolFileInJarAsString(String filePath) {
		
		try {

			InputStream ins=this.getClass().getClassLoader().getResourceAsStream(filePath);
			InputStreamReader isr=new InputStreamReader(ins);
			
			
			BufferedReader br=new BufferedReader(isr);

			String ac="";
			
			while (true) {
				String line=br.readLine();
				
				if (line==null) break;
				else ac+=line+"\r\n";
			}
			return ac;

		} catch (Exception e) {
//			e.printStackTrace();
			return null;
		}

	}
	
	
	public void RemoveHydrogensFromMolFilesInFolder(String inputfolderpath,String outputfolderpath) {
		
		File folder=new File(inputfolderpath);
		
		String [] files=folder.list();
		
		for (int i=0;i<files.length;i++) {			
			
			if (i%100==0) System.out.println(i);
			if (files[i].indexOf(".mol")==-1) continue;
			String inputfilepath=inputfolderpath+"/"+files[i];
			String outputfilepath=outputfolderpath+"/"+files[i];
			this.RemoveHydrogensFromMolFile(inputfilepath, outputfilepath);			
		}
		 
	}
	
	
	public void RemoveHydrogensFromMolFile(String inputfilepath,String outputfilepath) {
		
		
		try {
			FileInputStream fis=new FileInputStream(inputfilepath);
			MDLV2000Reader mr = new MDLV2000Reader(fis);
				
			AtomContainer ac=new AtomContainer();
			
			mr.read(ac);
			fis.close();
			
			ac=(AtomContainer)AtomContainerManipulator.removeHydrogens(ac);
						
			FileWriter fw = new FileWriter(outputfilepath);
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			
			mw.setWriteAromaticBondTypes(false);
			mw.write(ac);
			fw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	public static String getCASField(IAtomContainer m) {
		
		Vector v=new Vector(m.getProperties().keySet());
		
		if (m.getProperty("CAS")!=null) return "CAS";
		
		for ( Enumeration e = v.elements(); e.hasMoreElements();) {
			// 	retrieve the object_key
			String prop = (String) e.nextElement();			
//			if (prop.indexOf("CAS")>-1) return prop;
			if (prop.toUpperCase().indexOf("CAS")>-1) return prop;//return first property with CAS in it

		}
		return "";
	}
	
	public static String getNameField(IAtomContainer m) {
		
		Vector v=new Vector(m.getProperties().keySet());
		
		if (m.getProperty("Name")!=null) return "Name";
		if (m.getProperty("name")!=null) return "name";
		if (m.getProperty("NAME")!=null) return "NAME";
		
		for ( Enumeration e = v.elements(); e.hasMoreElements();) {
			// 	retrieve the object_key
			String prop = (String) e.nextElement();			
//			if (prop.indexOf("CAS")>-1) return prop;
			if (prop.toUpperCase().indexOf("NAME")>-1) return prop;//return first property with CAS in it

		}
		return "";
	}
	
	
	public static AtomContainerSet LoadFromSDFNew(String SDFFilePath) {
		try {
			
			AtomContainerSet atomContainerSet=new AtomContainerSet();
			
			 IteratingSDFReader reader = new IteratingSDFReader(
			   new FileInputStream(SDFFilePath), DefaultChemObjectBuilder.getInstance());
			 while (reader.hasNext()) {
			   IAtomContainer molecule = (IAtomContainer)reader.next();
			   atomContainerSet.addAtomContainer(molecule);
			 }
			
			return atomContainerSet;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	
	
	
	public static AtomContainerSet LoadFromSDF(String SDFfilepath) {
    	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();
		AtomContainerSet atomContainerSet=new AtomContainerSet();

//		AtomContainerSet AtomContainerSet=new AtomContainerSet();
		
		try {

			int counter = -1;
			
			
			 IteratingSDFReader reader = new IteratingSDFReader(
			   new FileInputStream(SDFfilepath), DefaultChemObjectBuilder.getInstance());
			 
			 while (reader.hasNext()) {
			   IAtomContainer m = (IAtomContainer)reader.next();

				String CASfield=getCASField(m);
//				System.out.println(CASfield);
				
				String CAS=null;
				
				if (!CASfield.equals("")) {
					CAS=(String)m.getProperty(CASfield);
					if (CAS != null) {
						CAS=CAS.trim();
						CAS=CAS.replace("/", "_");
						m.setProperty("CAS", CAS);
					}
				}
				
//				System.out.println(CAS);
				
				
				if (CAS==null) {
					
					String name=(String)m.getProperty("Name");
					
					if (name==null) {
						name=(String)m.getProperty("name");
					}
					if (name==null) {
//						m.setProperty("Error", "<html>CAS and Name fields are both empty</html>");
						m.setProperty("CAS", "Unknown");											
					} else {
						m.setProperty("CAS", name);
					}
				}

				

				m.setProperty("Error", "");
				 
				 
				if (mfu.HaveBadElement(m)) {
					m.setProperty("Error",
							"AtomContainer contains unsupported element");
				} else if (m.getAtomCount() == 1) {
					m.setProperty("Error", "Only one nonhydrogen atom");
				} else if (m.getAtomCount() == 0) {
					m.setProperty("Error", "Number of atoms equals zero");
				}

				 AtomContainerSet  AtomContainerSet2 = (AtomContainerSet)ConnectivityChecker.partitionIntoMolecules(m);

				 
				 if (AtomContainerSet2.getAtomContainerCount() > 1) {

//					m.setProperty("Error","Multiple AtomContainers, largest fragment retained");
					m.setProperty("Error","Multiple AtomContainers");
					
				}

				 
			   atomContainerSet.addAtomContainer(m);
			   
			 } // end loop over molecules in SDF
			
			
			 
			 
			FixDuplicateCASNumbersInSDF(atomContainerSet);
			
		} catch (Exception e) {
			e.printStackTrace();

		}
		
		return atomContainerSet;

	}

	

	/** Works no matter if molecules are empty
	 * 
	 * @param SDFfilepath
	 * @return
	 */

	public static AtomContainerSet LoadFromSDF3(String SDFfilepath) {
		AtomContainerSet moleculeSet = new AtomContainerSet();

		try {
			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));

			IteratingSDFReader isr=new IteratingSDFReader(br, DefaultChemObjectBuilder.getInstance());

			
			int counter=0;
			
			//read file into String Vector:
			while (isr.hasNext()) {
				counter++;
				
				AtomContainer ac=(AtomContainer) isr.next();

				if (ac == null) {
					//dont add molecule
				} else {
					moleculeSet.addAtomContainer(ac);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return moleculeSet;

	}
	
	
	
	public AtomContainerSet LoadFromSDFInJar(String SDFfilepath) {
    	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();

		AtomContainerSet AtomContainerSet=new AtomContainerSet();
		
		try {

			java.io.InputStream ins = this.getClass().getClassLoader()
			.getResourceAsStream(SDFfilepath);


			IteratingSDFReader mr = new IteratingSDFReader(ins,DefaultChemObjectBuilder.getInstance());


			int counter = -1;

			
			while (mr.hasNext()) {
				
				counter++;
				
//				System.out.println(counter);
				
				IAtomContainer m=null;
				
				try {
					m = mr.next();
				
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				if (m==null || m.getAtomCount()==0) break;
				
//				System.out.println(counter+"\t"+m.getAtomCount());

				String CASfield=getCASField(m);
//				System.out.println(CASfield);
				
				String CAS=null;
				
				if (!CASfield.equals("")) {
					CAS=(String)m.getProperty(CASfield);
					if (CAS != null) {
						CAS=CAS.trim();
						CAS=CAS.replace("/", "_");
						m.setProperty("CAS", CAS);
					}
				}
								
				
//				Hashtable props=m.getProperties();//TODO make sure properties are preserved
				
				m.setProperty("Error", "");
				 
				 
				if (mfu.HaveBadElement(m)) {
					m.setProperty("Error",
							"AtomContainer contains unsupported element");
				} else if (m.getAtomCount() == 1) {
					m.setProperty("Error", "Only one nonhydrogen atom");
				} else if (m.getAtomCount() == 0) {
					m.setProperty("Error", "Number of atoms equals zero");
				}

				 AtomContainerSet  AtomContainerSet2 = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

				 
				 if (AtomContainerSet2.getAtomContainerCount() > 1) {

//					m.setProperty("Error","Multiple AtomContainers, largest fragment retained");
					m.setProperty("Error","Multiple AtomContainers");
				}
								
				 
				if (CAS==null) {
					
					String name=(String)m.getProperty("Name");
					
					if (name==null) {
						name=(String)m.getProperty("name");
					}
					if (name==null) {
//						m.setProperty("Error", "<html>CAS and Name fields are both empty</html>");
						m.setProperty("CAS", "Unknown");											
					} else {
						m.setProperty("CAS", name);
					}
				}

//				System.out.println(m.getAtomCount());
				AtomContainerSet.addAtomContainer(m);
				
				
//				System.out.println(CAS+"\t"+m.getAtomCount());
				
			}// end while true;

			ins.close();
			
			FixDuplicateCASNumbersInSDF(AtomContainerSet);
			
		} catch (Exception e) {
			e.printStackTrace();

		}
		
		return AtomContainerSet;

	}

	

	public static void FixDuplicateCASNumbersInSDF(IAtomContainerSet AtomContainerSet) {
		ArrayList<String> al=new ArrayList<String>();
		
		for (int i=0;i<AtomContainerSet.getAtomContainerCount();i++) {
			String CAS=(String)AtomContainerSet.getAtomContainer(i).getProperty("CAS");
//			String Log_BCF=(String)AtomContainerSet.getAtomContainer(i).getProperty("Log_BCF");			
			al.add(CAS+"\t"+i);			
//			System.out.println(CAS+"\t"+Log_BCF);
		}
		
		
		java.util.Collections.sort(al);
		
		for (int i=0;i<al.size()-1;i++) {
			
			String Line=al.get(i);
			String CAS=Line.substring(0,Line.indexOf("\t"));
			String Num=Line.substring(Line.indexOf("\t")+1,Line.length());
			
			ArrayList <String>duplist=new ArrayList<String>();
			while(Num.length()<6) Num="0"+Num;
			Line=CAS+"\t"+Num;
			
			duplist.add(Line);
			
			while (true) {
				if (i+duplist.size()==AtomContainerSet.getAtomContainerCount()) break;
				String Line2=al.get(i+duplist.size());
				String CAS2=Line2.substring(0,Line2.indexOf("\t"));
				
//				System.out.println(CAS+"\t"+CAS2);
				
				String StrNum2=Line2.substring(Line2.indexOf("\t")+1,Line2.length());
				
				while(StrNum2.length()<6) StrNum2="0"+StrNum2;
				
				Line2=CAS2+"\t"+StrNum2;
											
				if (CAS2.equals(CAS)) {
//					System.out.println(Line2);
					duplist.add(Line2);					
				} else {
					break;
				}
			}
			
			java.util.Collections.sort(duplist);
			
			if (duplist.size()>1) {

				for (int j=1;j<duplist.size();j++) {
					String dup=duplist.get(j);
					String dupCAS=dup.substring(0,dup.indexOf("\t"));
					String dupStrNum=dup.substring(dup.indexOf("\t")+1,dup.length());
//					System.out.println(j+"\t"+dupCAS+"\t"+dupStrNum);
					
					int dupNum=Integer.parseInt(dupStrNum);
					String dupCASNew=dupCAS+"_"+(j+1);
//					System.out.println(dupCASNew);
					IAtomContainer molDup=AtomContainerSet.getAtomContainer(dupNum);
					molDup.setProperty("CAS", dupCASNew);
				}
				
				i+=duplist.size();// skip ahead
			}
			
		}
		
//		//check how we did renaming:
//		ArrayList<String> al2=new ArrayList<String>();
//		for (int i=0;i<AtomContainerSet.getAtomContainerCount();i++) {
//			String CAS=(String)AtomContainerSet.getAtomContainer(i).getProperty("CAS");
//			al2.add(CAS);
//		}
//		java.util.Collections.sort(al2);
//		
//		for (int i=0;i<al2.size();i++) {
//			System.out.println(al2.get(i));
//		}
		
	}
	
	public static AtomContainerSet LoadFromSmilesList(String filepath) {
		
		
		AtomContainerSet AtomContainerSet=new AtomContainerSet();
		
		String delimiter="";
		
		SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
				
//		System.out.println("here");
		
    	try {
    		
    		BufferedReader br=new BufferedReader(new FileReader(filepath));
    		int counter=0;
    		while (true) {
    			String Line=br.readLine();
    			counter++;
    			
    			if (Line==null) break;
    			if (Line.trim().equals("")) break;
    			
//    			if (delimiter.equals("")) {
    				if (Line.indexOf("\t")>-1) delimiter="\t";
    				else if (Line.indexOf(",")>-1) delimiter=",";
    				else if(Line.indexOf(" ")>-1) delimiter=" ";
    				else return null;
//    			}
    			
    			ArrayList l=(ArrayList)ToxPredictor.Utilities.Utilities.Parse2(Line, delimiter);
    			if (l.size()!=2) return null;
    			
    			String Smiles=(String)l.get(0);
    			String ID=(String)l.get(1);
    			
    			//replace any characters that could mess up creation of file paths later:
    			ID=ID.replace("\"", "_");
    			ID=ID.replace("/", "_");
    			ID=ID.replace(":", "_");
    			ID=ID.replace("*", "_");
    			ID=ID.replace("?", "_");
    			ID=ID.replace("<", "_");
    			ID=ID.replace(">", "_");
    			ID=ID.replace("|", "_");
    			
    			    			
    			System.out.print(counter+": Loading "+ID+"...");
    			IAtomContainer m=null;
    			try {
    				
    				m=sp.parseSmiles(Smiles);
//    				System.out.println(Smiles);
    				
    				m.setProperty("Error", "");
    				
    				boolean aromaticOK=doesAromaticCountMatch(m, Smiles);

    				if (!aromaticOK) {
    					System.out.println("ID\tError perceiving aromaticity of smiles");
    					m.setProperty("Error", "Error perceiving aromaticity of smiles");
    				}
    				
    				
    			} catch (Exception e) {
    				m=new AtomContainer();
    				m.setProperty("Error", "Smiles parsing error: "+Smiles);
    			}
    			m.setProperty("CAS",ID);
    			AtomContainerSet.addAtomContainer(m);
    			
    			System.out.print("Done\n");

    		}
    		
    		br.close();
    		FixDuplicateCASNumbersInSDF(AtomContainerSet);//added TMM, 9/15/2010
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
		return AtomContainerSet;
	}
	
	
	
	
	public static boolean doesAromaticCountMatch(IAtomContainer AtomContainer,String Smiles) {
		
		//get count of aromatic atoms:
		int aromaticCount=0;
		for (int i=0;i<AtomContainer.getAtomCount();i++) {
			Atom atom=(Atom)AtomContainer.getAtom(i);
			if (atom.getFlag(CDKConstants.ISAROMATIC)) aromaticCount++;
		}

		// parse again but just to get a count of aromatic atoms in original smiles:
		
		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			AtomContainer AtomContainer2=(AtomContainer) sp.parseSmiles(Smiles);
			int aromaticCount2=0;

			for (int i=0;i<AtomContainer2.getAtomCount();i++) {
				Atom atom=(Atom)AtomContainer2.getAtom(i);
				IAtomType.Hybridization hybridization=atom.getHybridization();
				if (hybridization==IAtomType.Hybridization.SP2) aromaticCount2++;
			}
			
			if (aromaticCount2==0) return true; // smiles didnt have aromatic lower case atoms
			if (aromaticCount2==aromaticCount) return true;
			else return false;
		
		} catch (Exception e) {
			System.out.println("Error checking aromatic count");
			return false;
		}

	}
	
public static AtomContainerSet LoadFromCASList(String filepath,ChemicalFinder cf) {
    	
    	AtomContainerSet AtomContainerSet=new AtomContainerSet();
    
    	ToxPredictor.misc.MolFileUtilities mfu=new ToxPredictor.misc.MolFileUtilities();
    	
    	try {
    		
    		BufferedReader br=new BufferedReader(new FileReader(filepath));
    		
//    		String header="";
//    		if (HaveHeader)  {
//    			header=br.readLine();
//    		}
    		
    		String CAS="";
    		
    		while (true) {
    			String Line=br.readLine();
    			if (Line!=null) Line=Line.trim();
    			else break;
    			if (Line.equals("")) break;
    			
    			
    			CAS=Line;
    			CAS=TaskStructureSearch.parseSearchCAS(CAS);
//    			System.out.println("*"+CAS);

    			IAtomContainer m=cf.LoadChemicalFromCAS(CAS);
    			
    			if (m==null) {
    				m=new org.openscience.cdk.AtomContainer();
    				m.setProperty("Error", "CAS number not found");
    				m.setProperty("CAS", CAS);
    				AtomContainerSet.addAtomContainer(m);
    				continue;
    			}
    			
				AtomContainerSet.addAtomContainer(m);
				m.setProperty("Error", "");
				
				 IAtomContainerSet  AtomContainerSet2 = ConnectivityChecker.partitionIntoMolecules(m);
				 
				 if (AtomContainerSet2.getAtomContainerCount()>1) {					 
					 m.setProperty("Error", "Multiple AtomContainers");
					 continue;
				 }

				if (mfu.HaveBadElement(m)) {
					m.setProperty("Error", "AtomContainer contains unsupported element");
					continue;
				}
				
				if (m.getAtomCount()==1) {
					m.setProperty("Error", "Only one nonhydrogen atom");
					continue;
				} 

    		}
    		
    		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return AtomContainerSet;
    }

	
	
	public void FixHydrogensOfMolFilesInFolder(int Start,File f1,File f2) {
		
		if (!f1.exists() || !f1.isDirectory()) {
			System.out.println("bad directory");
			return;
		}
		
		File [] files=f1.listFiles();
		
		if (!f2.exists()) f2.mkdir();
		
		for (int i=Start;i<files.length;i++) {
			this.FixHydrogensOfMolFile(f1,f2,files[i].getName());
		}
		
		
	}
	
	
	public void CompareStructuresInTwoFolders() {
		
		String folderpath1="C:/Documents and Settings/tmarti02/My Documents/javax/cdk from jar-1.0/ToxPredictor/DescriptorTextTables/Cancer data files/mol files v5a with H";		
		String folderpath2="F:/javax/cdk from jar (backup on 8-13-07)/ToxPredictor/structures/DSSTOX_withH";
		
		String folderpath3="C:/Documents and Settings/tmarti02/My Documents/javax/cdk from jar-1.0/ToxPredictor/DescriptorTextTables/Cancer data files/differences between DSSTOX and v5a";
		
		File Folder1=new File(folderpath1);
		File Folder2=new File(folderpath2);
		
		File [] files1=Folder1.listFiles();
		
		for (int i=0;i<files1.length;i++) {
			File file1=files1[i];
			
			String name=file1.getName();
			String CAS=name.substring(0,name.indexOf("."));
			
			
			if (CAS.equals("NOCAS")) continue;
			
			File file2=new File(folderpath2+"/"+name);
			
			if (!file2.exists()) {
				System.out.println(CAS+"\tis missing in folder2");
				File DestFile1=new File(folderpath3+"/"+CAS+".mol");					
				ToxPredictor.Utilities.Utilities.CopyFile(file1, DestFile1);
				
			} else {//compare em:
				IAtomContainer m1=ParseChemidplus.LoadChemicalFromMolFile(CAS, folderpath1);
				IAtomContainer m2=ParseChemidplus.LoadChemicalFromMolFile(CAS, folderpath2);
				
				boolean isomorph=ToxPredictor.Utilities.chemicalcompare.isIsomorphFingerPrinter(m1, m2);
				
				if (!isomorph) {
					System.out.println(CAS+"\tStructures are different");
					
//					File DestFile1=new File(folderpath3+"/"+CAS+"_1.mol");					
//					ToxPredictor.Utilities.Utilities.CopyFile(file1, DestFile1);
//					
//					File DestFile2=new File(folderpath3+"/"+CAS+"_2.mol");
//					ToxPredictor.Utilities.Utilities.CopyFile(file2, DestFile2);
				}
			}
			
			
		}
		
		
		
	}
	
	/** finds MW field in sdf file without blank AtomContainers (usually mixture or unresolved)
	 * 
	 * @param ChemNums
	 */
	
	public void Bob(String ChemNums) {
		
		String folder="ToxPredictor/chemidplus/sdf_from_name/SDF Files Without Structures";
				

		String filename="Good_Data_Structures_"+ChemNums;
		
		File newSDFfile2=new File(folder+"/"+filename+"-3.sdf");
		
		File noteFile=new File(folder+"/notes/"+filename+"_notes.txt");
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(newSDFfile2));
			FileWriter fw=new FileWriter(noteFile);
			
			//read until EOF:
			
			boolean stop=false;
			
			while (true) {
				// read until $$$$:

				String CAS="";
				String MW="";

				while (true) {

					String Line = br.readLine();

					if (Line == null) {
						stop = true;
						break;
					}
					if (Line.indexOf("$$$$") > -1)
						break;

					if (Line.indexOf("<CAS>") > -1) {
						CAS = br.readLine();
					}
					
					if (Line.indexOf("<MW>") > -1) {
						MW = br.readLine();
						fw.write(CAS+"\t"+MW+"\r\n");
						fw.flush();
					}
					

				}
				if (stop)
					break;

			}
			
			
			fw.close();
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void CreateMolFilesFromSmilesList(int Start) {
			
//			String filename="ToxPredictor/chemidplus/parse/smiles for chemicals in chemidplus ld50 db.txt";
			String filename="ToxPredictor/chemidplus/parse/smiles w error-fixed.txt";
			
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			
						
			DescriptorFactory df=new DescriptorFactory(false);
//			MyHydrogenAdder myHA=new MyHydrogenAdder();
//			HydrogenAdder haCDK=new HydrogenAdder(new SmilesValencyChecker());
			
			
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			try {
				
				BufferedReader br=new BufferedReader(new FileReader(filename));
				
				int counter=0;
				
				while (true) {
					String Line=br.readLine();
					counter++;
					
					if (counter<Start) continue;
					
					if (Line==null) break;
					
					LinkedList l=Utilities.Parse(Line,"\t");
					String CAS=(String)l.get(0);
					String Smiles=(String)l.get(1);
					Smiles=CDKUtilities.FixSmiles(Smiles);
					
					System.out.println(CAS+"\t"+Smiles);
					if (CAS.equals("1330-38-7") || CAS.equals("2278-50-4")) continue;
					
					try {
						IAtomContainer AtomContainer = sp.parseSmiles(Smiles);
						df.Normalize(AtomContainer);
						
						
						File newmolfile=new File("ToxPredictor/chemidplus/structures/Smiles/"+CAS+"-Smiles.mol");
						
	//					System.out.println(newmolfile);
						
						IAtomContainer AtomContainer2=CDKUtilities.addHydrogens(AtomContainer);
	
						
						if (AtomContainer2 != null) {
							
							IAtomContainer AtomContainer3=(IAtomContainer)AtomContainer2.clone();
							
							sdg.setMolecule(AtomContainer3);
							sdg.generateCoordinates();
							
							
							if (AtomContainer3 != null) {
								FileWriter fw=new FileWriter(newmolfile);				
								MDLV2000Writer mw=new MDLV2000Writer(fw);
								mw.setWriteAromaticBondTypes(false);
								try {
									mw.write(AtomContainer3);
								} catch (Exception e) {
									System.out.println(CAS);
									e.printStackTrace();
								}
								mw.close();
								fw.close();
							} else {
								FileWriter fw=new FileWriter(newmolfile);				
								MDLV2000Writer mw=new MDLV2000Writer(fw);
								mw.setWriteAromaticBondTypes(false);
								mw.write(AtomContainer2);						
								mw.close();
								fw.close();
							}
							
						} 
						
						
					} catch (Exception e) {
						System.out.println("error:"+CAS+"\t"+Smiles);
	//					e.printStackTrace();
					}
				} // overall chemical while loop
				
				br.close();
				
			} catch (Exception e) {
	//			e.printStackTrace();
			}
		}

	void FindMissingMolFilesFromSmilesList() {
		
		String filename="ToxPredictor/chemidplus/parse/smiles for chemicals in chemidplus ld50 db.txt";
		

		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filename));
			FileWriter fw=new FileWriter("ToxPredictor/chemidplus/parse/smiles w error.txt");

			int counter=0;
			
			while (true) {
				String Line=br.readLine();
				counter++;
				
				if (Line==null) break;
				
				LinkedList l=Utilities.Parse(Line,"\t");
				String CAS=(String)l.get(0);
				String Smiles=(String)l.get(1);
				Smiles=CDKUtilities.FixSmiles(Smiles);
				
				if (Smiles.indexOf(".")>-1) continue; //omit ones with "."
				if (Smiles.indexOf("Ce")>-1) continue; //omit ones with "Ce"
				if (Smiles.indexOf("Na")>-1) continue; //omit ones with "Na"
				if (Smiles.indexOf("La")>-1) continue; //omit ones with "La"
				if (Smiles.indexOf("Sm")>-1) continue; //omit ones with "Sm"
				if (Smiles.indexOf("Pt")>-1) continue; //omit ones with "Pt"
				if (Smiles.indexOf(";")>-1) continue; //omit ones with ";"
				if (Smiles.indexOf("W")>-1) continue; //omit ones with "W"
				
//				System.out.println(CAS+"\t"+Smiles);
				try {

					File newmolfile=new File("ToxPredictor/chemidplus/structures/Smiles/"+CAS+"-Smiles.mol");
					if (!newmolfile.exists()) {
//						System.out.println(newmolfile.getName());
						fw.write(CAS+"\t"+Smiles+"\n");
					}
					
				} catch (Exception e) {
					System.out.println("error:"+CAS+"\t"+Smiles);
//					e.printStackTrace();
				}
			} // overall chemical while loop
			
			br.close();
			fw.close();
			
		} catch (Exception e) {
//			e.printStackTrace();
		}
	}
	
	
	public void CombineTextFiles(File folder,File outputfile,boolean HasHeader,String ext) {
		

		File[] files = folder.listFiles();

		try {

			FileWriter fw = new FileWriter(outputfile);

			for (int i = 0; i < files.length; i++) {

				if (files[i].getName().indexOf(ext) > -1
						&& files[i].getName().indexOf("Copy") == -1
						&& files[i].getName().indexOf("all.txt") == -1) {

					BufferedReader br = new BufferedReader(new FileReader(
							files[i]));
					
					if (HasHeader) {
						String Header = br.readLine();
						if (i == 0)
							fw.write(Header + "\r\n");
					}

					while (true) {
						String Line=br.readLine();
						if (Line==null) break;
						fw.write(Line+"\r\n");
					}
					
					
					br.close();

				}
			}

			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public void OmitMissingStructuresFromSDFFile(String filename,String folder) {
		
		
		filename=filename.substring(0,filename.indexOf("."));
		
		File SDFfile=new File(folder+"/Original SDF Files/"+filename+".sdf");
		File newSDFfile=new File(folder+"/SDF Files With Structures/"+filename+"-2.sdf");
		File newSDFfile2=new File(folder+"/SDF Files Without Structures/"+filename+"-3.sdf");
		
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(SDFfile));
			FileWriter fw=new FileWriter(newSDFfile);
			FileWriter fw2=new FileWriter(newSDFfile2);
						
			
			while (true) {
				String Line=br.readLine(); // first line
				if (Line==null) break;
				
				if (Line.indexOf(">")==-1) fw.write(Line+"\r\n");
				else fw2.write(Line+"\r\n");
				
				System.out.println(Line);
				//read until $$$$
				
				while (true) {
					String Line2=br.readLine();
					
					if (Line.indexOf(">")==-1) fw.write(Line2+"\r\n");
					else fw2.write(Line2+"\r\n");
					
					if (Line2==null) break;
					if (Line2.indexOf("$$$$")>-1) break;
					
				}
				
			}
			
			
			br.close();
			fw.close();
			fw2.close();			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	public void FindIncompleteFiles() {

		try {

			File mainfolder = new File(
					"C:/Documents and Settings/tmarti02/My Documents/comptox/ChemIDplus All");

			File[] folders = mainfolder.listFiles();

			for (int i = 0; i < folders.length; i++) {
				
				if (! folders[i].isDirectory()) continue;
				
				System.out.println(folders[i].getName());
				
				File[] files = folders[i].listFiles();

				for (int j = 0; j < files.length; j++) {
					
					if (files[j].isDirectory())	continue;

					BufferedReader br = new BufferedReader(new FileReader(
							files[j]));

					String Line;
					
					int CloseTagCount = 0;

					while (true) {
						Line = br.readLine();

						if (!(Line instanceof String))
							break;

						if (Line.indexOf("</html>") > -1)
							CloseTagCount++;

					}

					br.close();

					if (CloseTagCount<2) System.out.println(files[j].getName()+" is incomplete");
					
				}

			}

							

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

//	public void DownloadNISTMolFiles() {
//
//		try {
//
//			File mainfolder = new File(
//					"C:/Documents and Settings/tmarti02/My Documents/comptox/ChemIDplus All");
//
//			File[] folders = mainfolder.listFiles();
//			
//			for (int i = 0; i < folders.length; i++) {
//				
//				if (! folders[i].isDirectory()) continue;
//				
//				System.out.println(folders[i].getName());
//				
//				File[] files = folders[i].listFiles();
//
//				for (int j = 0; j < files.length; j++) {
//					
//					if (files[j].isDirectory())	continue;
//					String name=files[j].getName();
//					String CAS=name.substring(0,name.indexOf("."));
//					
//					System.out.print(CAS+"...");
//					if (downloadmolfiles.isNISTMolFileAvailable(CAS,2)) {
//						System.out.print("Got2d...");
//						downloadmolfiles.DownloadChemical(CAS,"ToxPredictor/chemidplus/structures/NIST2d",2);
//					}
//					
//					if (downloadmolfiles.isNISTMolFileAvailable(CAS,3)) {
//						System.out.print("Got3d...");
//						downloadmolfiles.DownloadChemical(CAS,"ToxPredictor/chemidplus/structures/NIST3d",3);
//					}
//					System.out.print("done\n");
//					
//				}
//
//			}
//
//							
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	
	

	/**
	 * Renames mol files with .txt extension to .mol
	 * 
	 */
	void RenameFiles() {
		File folder = new File(
				"N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol-chemidplus-3d");

		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			String ext = name.substring(name.indexOf(".") + 1, name.length());

			String name2 = name.substring(0, name.indexOf("."));

			// System.out.println(ext);
			if (ext.equals("txt")) {
				File newFile = new File(folder.getAbsolutePath() + "/" + name2
						+ ".mol");

				if (newFile.exists())
					files[i].delete();
				else
					files[i].renameTo(newFile);
			}

		}

	}

	
	public static void WriteMolFile(IAtomContainer m,String CAS,String outputFilePath,boolean Scale) {
		
//		String Name=(String)m.getProperty("Name");
		
		
		try {
			File molfile=new File(outputFilePath);
			FileWriter fw=new FileWriter(molfile);
			
//			System.out.println(molfile.getAbsolutePath());
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			
			mw.setWriteAromaticBondTypes(false);
			
			m.setProperty("CAS", CAS);
			
			IAtomContainer m2=(IAtomContainer)m.clone();
			
			if (Scale) ScaleAtomContainer(m2); 
			
			mw.write(m2);
			fw.write("> <CAS>\r\n"+CAS+"\r\n\r\n$$$$\r\n");			
			fw.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void ScaleAtomContainer(IAtomContainer m2) {
		double maxDim=-999999999;
		
		boolean HaveAll3D=true;
		
		for (int i=0;i<m2.getAtomCount();i++) {
			if (m2.getAtom(i).getPoint3d()==null) {
				HaveAll3D=false;
				break;
			}
		}
		
		
		for (int i=0;i<m2.getAtomCount();i++) {
			
			double x=0;
			double y=0;
			double z=0;
			
			if (HaveAll3D) {
				x=Math.abs(m2.getAtom(i).getPoint3d().x);
				y=Math.abs(m2.getAtom(i).getPoint3d().y);
				z=Math.abs(m2.getAtom(i).getPoint3d().z);
			} else if (m2.getAtom(i).getPoint2d()!=null) {
				x=Math.abs(m2.getAtom(i).getPoint2d().x);
				y=Math.abs(m2.getAtom(i).getPoint2d().y);
				z=0;
			}
			
			double maxCurrent=Math.max(x,y);
			maxCurrent=Math.max(maxCurrent, z);
			
			if (maxCurrent>maxDim) maxDim=maxCurrent;
			
		}
		
		if (maxDim>0) {
			for (int i=0;i<m2.getAtomCount();i++) {
				
				double x=0,y=0,z=0;
				
				if (HaveAll3D) {
					x=Math.abs(m2.getAtom(i).getPoint3d().x);						
					y=Math.abs(m2.getAtom(i).getPoint3d().y);
					z=Math.abs(m2.getAtom(i).getPoint3d().z);
				} else if (m2.getAtom(i).getPoint2d()!=null) {
					x=Math.abs(m2.getAtom(i).getPoint2d().x);
					y=Math.abs(m2.getAtom(i).getPoint2d().y);
					z=0;
				}
				
				x/=maxDim;
				y/=maxDim;
				z/=maxDim;
				
				if (HaveAll3D) {
					m2.getAtom(i).setPoint3d(new javax.vecmath.Point3d(x,y,z));
				} else {
					m2.getAtom(i).setPoint2d(new javax.vecmath.Point2d(x,y));
					m2.getAtom(i).setPoint3d(null);
				}
			}
		}
	}
	
	
	

	
	
	/**
	 * Removes extension with "_"  i.e. 50-00-0_DSSTOX2d.mol becomes 50-00-0.mol
	 *
	 */
	void RenameFiles2() {
		String f="ToxPredictor/chemidplus/structures";
//		String f2="DSSTox";
//		String f2="DSSTox_withH";
//		String f2="INDEXNET2d";
//		String f2="INDEXNET2d_withH";
//		String f2="Name";
//		String f2="Name_withH";
//		String f2="NIST2d";
//		String f2="NIST2d_withH";
//		String f2="NIST3d";
//		String f2="Smiles_Marvin_withH";
		String f2="Smiles_Marvin";
		
		String f3=f+"/"+f2;
		
//		f3="ToxPredictor/NIST_Download/NIST2d";
//		f3="ToxPredictor/NIST_Download/NIST2d_withH";
		f3="ToxPredictor/NIST_Download/NIST3d";
		
		
		
		File folder = new File(f3);
		if (!folder.exists()) {
			System.out.println("Folder:"+folder.getAbsolutePath()+" doesnt exist");
		}

		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			
			if (i%100==0) System.out.println(i);
			
			if (name.indexOf("_")==-1) continue;
			
			String CAS=name.substring(0,name.indexOf("_"));
			
			String newname=CAS+".mol";
			
			File newFile = new File(folder.getAbsolutePath() + "/" + newname);
					
			files[i].renameTo(newFile);
			
		}

	}

	
	
	/**
	 * Writes a mol file from a folder to the console
	 * 
	 * @param CAS
	 * @param folder
	 */

	public void DisplayMolFile(String CAS, String folder) {

		try {

			String filename = folder + File.separator + CAS + ".mol";
			File f = new File(filename);

			FileReader reader = new FileReader(f);

			BufferedReader br = new BufferedReader(reader);
			while (true) {
				String Line = br.readLine();
				if (Line == null)
					break;
				System.out.println(Line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Checks if AtomContainer has any hydrogens
	 * 
	 * @param AtomContainer
	 * @return
	 */
	public boolean HasHydrogens(IAtomContainer AtomContainer) {

		for (int i = 0; i < AtomContainer.getAtomCount(); i++) {
			if (AtomContainer.getAtom(i).getSymbol().equals("H"))
				return true;
		}

		return false;
	}

	/**
	 * Renames mol files from indexnet
	 * 
	 */
	public void BatchRename() {
		String strFolder = "ToxPredictor/chemidplus/structures/INDEXNET_Nov06_2d";
		File folder = new File(strFolder);

		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().indexOf(".mol") > -1) {
				String newname = files[i].getName();
				newname = newname.substring(0, newname.indexOf("."));
				newname += "_IndexNet2d.mol";
				File fnew = new File(strFolder + "/" + newname);
				files[i].renameTo(fnew);

			}
		}

	}

	public void DisplayMolFileInJar(String filename) {

		try {

			java.io.InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(filename);

			InputStreamReader isr = new InputStreamReader(ins);

			BufferedReader br = new BufferedReader(isr);

			while (true) {
				String Line = br.readLine();
				if (Line == null)
					break;
				System.out.println(Line);

			}

			isr.close();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	void FindMolFilesWithoutHydrogens() {

		String strFolder = "N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol-chemidplus-3d";

		File folder = new File(strFolder);

		File[] files = folder.listFiles();

		int count = 0;

		for (int i = 0; i < files.length; i++) {

			try {
				BufferedReader br = new BufferedReader(new FileReader(files[i]));
				MDLV2000Reader mr = new MDLV2000Reader(br);

				AtomContainer atomContainer =new AtomContainer();
				mr.read(atomContainer);

				if (!HasHydrogens(atomContainer)) {
					System.out.println(files[i].getName());
					count++;
				}

			} catch (Exception e) {
				System.out.println("Error for " + files[i].getName());
				e.printStackTrace();
			}
		}
		System.out.println("missingcount= " + count);

	}

	void MoveMolFilesWithNoAtoms() {

		String folder = "ToxPredictor/chemidplus/structures/INDEXNET_Nov06_2d";
		MDLV2000Reader mr = null;

		File f = new File(folder);

		File[] files = f.listFiles();
		try {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory())
					continue;

				BufferedReader br = new BufferedReader(new FileReader(files[i]));
				AtomContainer m = new AtomContainer();
				
				mr=new MDLV2000Reader(br);
				
				mr.read(m);
				
				br.close();

				File f2 = new File(folder + "/No Atoms/" + files[i].getName());

				if (m.getAtomCount() == 0) {
					Utilities.CopyFile(files[i], f2);
					files[i].delete();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	
	
	/**
	 * if a file exists in No Atoms folder and in parent folder, delete file in
	 * parent folder just in case the movemolfileswithnoatoms method failed in
	 * deleting them
	 */
	void DeleteMolFilesWithNoAtoms() {

		String folder = "ToxPredictor/chemidplus/structures/INDEXNET_Nov06_2d";
		// ToxPredictor.Utilities.MDLReader mr = new
		// ToxPredictor.Utilities.MDLReader();

		File f = new File(folder);
		File f2 = new File(folder + "/No Atoms");

		File[] files = f.listFiles();
		File[] files2 = f2.listFiles();
		try {
			for (int i = 0; i < files2.length; i++) {

				String name = files2[i].getName();
				File file = new File(folder + "/" + name);

				if (file.exists()) {
					System.out.println(name);
					file.delete();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	/**
	 * This method takes a file with CAS and source and makes an SDF
	 * If source is blank, the best source is determined and that is used
	 *
	 */
	void CreateSDFFileFromCASandSource3() {
		
		String CAS="",Source="";
		
		
		String folder="ToxPredictor/temp/doug";
    	String filename="cas list war data w revised source.txt";
    	String sdfFileName="wardata2008.sdf";
    	
    	
        String d="\t";
        String d2="\t";

        java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
        
		SmilesGenerator sg=new SmilesGenerator(); 
		
		ParseChemidplus p=new ParseChemidplus();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filename));
			FileWriter fw=new FileWriter(folder+"/"+sdfFileName);
		
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(false);
			
			
			String header=br.readLine();
			System.out.println(header);
			java.util.List headerlist=Utilities.Parse(header,d);			
			int Col_CAS=0;
			int Col_Source=1;
			
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.List list=Utilities.Parse(Line,d);
				CAS=(String)list.get(Col_CAS);
				
				FileData fd=new FileData();
				fd.CAS=CAS;
				
				if (list.size()>1)
					Source=(String)list.get(Col_Source);
				else
					Source="";
				
				
				IAtomContainer ac=null;
				
				if (Source.equals("")) {
					p.CheckForMolFiles2(fd);
					ac=p.GetBestMolecule(fd);
				} else {
					ac=p.getAtomContainerFromSource(Source,CAS);	
				}
			
				if (ac==null) {
					//try to load it from chemiplus folder for extra chemicals:
					String f="ToxPredictor/structures/chemidplus";
					ac=p.LoadChemicalFromMolFile(CAS,f );
				}
				
				
				if (ac==null) {
					System.out.println(CAS+"\tmissing");
					continue;
				}

				
//				System.out.println(CAS+"\t"+AtomContainer.getAtomCount());
				

				ac.setProperty("CAS", CAS);
				ac.setProperty("Source", Source);
				
				
//				AtomContainer.setProperty("CAS",CAS);
				
				mw.write(ac);
				fw.write("$$$$\n");
				
				
			}
			
			
			br.close();
			fw.close();
		} catch (Exception e) {
			System.out.println("Error for "+CAS+", Source="+Source);
			e.printStackTrace();
		}
		
	}
	
	public static void saveAtomContainerSetToSDFFile(String filePath,AtomContainerSet acs) {
		
		try {
			
			FileWriter fw=new FileWriter(filePath);
			
//			System.out.println(filePath);
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				
				mw.setWriteAromaticBondTypes(false);
				
				 if (acs.getAtomContainer(i)==null) {
					 System.out.println((String)ac.getProperty("CAS")+"\tnull structure");
					 continue;
				 }
				
				System.out.println((String)ac.getProperty("CAS"));
				mw.write(acs.getAtomContainer(i));
				
				fw.write("> <CAS>\r\n");
				fw.write(ac.getProperty("CAS")+"\r\n");
				
				if (ac.getProperty("name")!=null) {
					fw.write("> <name>\r\n");
					fw.write(ac.getProperty("name")+"\r\n");
				}
				
				fw.write("$$$$\r\n");
				
//				System.out.println(i);
			}
			
//			fw.close();
			mw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	void CreateSDFFileFromCASandSource() {
		
		String CAS="",Source="",Name="",SMILES="";
		
//		String folder="ToxPredictor/DescriptorTextTables/LD50 Data Files";
//		String filename="tox LD50 all.txt";
//		String sdffilename="LD50.sdf";

		
//		String folder="ToxPredictor/DescriptorTextTables/LC50 Data Files";
////		String filename="tox LC50 all.txt";
//		String filename="tox LC50 overall set.csv";
//		String sdffilename="LC50.sdf";
//		String delimiter=",";
		
		String folder="ToxPredictor/chemidplus/compare";
    	String filename="CAS list for union of lc50 and ld50 good chemicals.txt";
    	String sdfFileName="LC50 and LD50 good chemicals.sdf";
    	String manifestFileName="LC50 and LD50 good chemicals manifest.txt";
    	
        String d="\t";
        String d2="\t";

        java.text.DecimalFormat df=new java.text.DecimalFormat("0.00");
        
		org.openscience.cdk.smiles.SmilesGenerator sg=new org.openscience.cdk.smiles.SmilesGenerator(); 
		
		ParseChemidplus p=new ParseChemidplus();
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filename));
			FileWriter fw=new FileWriter(folder+"/"+sdfFileName);
			FileWriter fw2=new FileWriter(folder+"/"+manifestFileName);
			
			fw2.write("CAS Number"+d2+"SMILES"+d2+"Formula"+d2);
			fw2.write("Molecular Weight"+d2+"Name"+"\r\n");
			
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			mw.setWriteAromaticBondTypes(false);
			
			
			String header=br.readLine();
			System.out.println(header);
			java.util.List headerlist=Utilities.Parse(header,d);			
			int Col_CAS=0;
			int Col_Source=1;
			int Col_Name=2;
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.List list=Utilities.Parse(Line,d);
				CAS=(String)list.get(Col_CAS);
				Source=(String)list.get(Col_Source);
				Name="";
				
				if (Col_Name !=-1) {
					Name=(String)list.get(Col_Name);
				}
//				if (!CAS.equals("452-06-2")){
//					continue;
//				}
				
				AtomContainer atomContainer=p.getAtomContainerFromSource(Source,CAS);

				System.out.println(CAS+"\t"+atomContainer.getAtomCount());
				
				String Formula=CDKUtilities.calculateFormula(atomContainer);
				double MolecularWeight=CDKUtilities.calculateMolecularWeight(atomContainer);

				
				try {
					IAtomContainer m2=(IAtomContainer)atomContainer.clone();
//					ToxPredictor.Utilities.CDKUtilities.RemoveHydrogens(m2);
					m2=(IAtomContainer)AtomContainerManipulator.removeHydrogens(m2);
					SMILES=sg.create(m2);
				
				} catch (Exception e) {
					SMILES=sg.create(atomContainer);
				}
				
				atomContainer.setProperty("CAS", CAS);
				atomContainer.setProperty("molname", Name);
				atomContainer.setProperty("Formula", Formula);
				atomContainer.setProperty("SMILES", SMILES);
				
				
//				AtomContainer.setProperty("CAS",CAS);
				
				mw.write(atomContainer);
				fw.write("$$$$\n");
				
				fw2.write(CAS+d2+SMILES+d2+Formula+d2+df.format(MolecularWeight)+d2);
				fw2.write(Name+"\r\n");
				fw2.flush();
				
				
			}
			
			
			br.close();
			fw.close();
		} catch (Exception e) {
			System.out.println("Error for "+CAS+", Source="+Source);
			e.printStackTrace();
		}
		
		
	}
	
	
	void CreateCASListFromSDFFile(String SDFfilename, String outputfileloc,String fieldName) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
			
			FileWriter fw = new FileWriter(outputfileloc);

			MDLV2000Reader mr = new MDLV2000Reader(br);

			int counter=0;
			
			while (true) {
				counter++;
				try {
					
					AtomContainer m=new AtomContainer();
					
					mr.read(m);
					
					String CAS = (String)m.getProperty(fieldName);
					System.out.println(counter+"\t"+CAS);

					fw.write(CAS + "\r\n");
					fw.flush();

//					if (m.getAtomCount() == 0)
//						break;
				} catch (Exception e) {
					break;
				}



			}// end while true;

			br.close();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	
	void EvaluateResultsofAddHydrogens(File folder) {
		
		try {
			
			File[] files = folder.listFiles();
			
			FileWriter fw=new FileWriter(folder.getParentFile().getAbsolutePath()+"/"+folder.getName()+"_discrepancies.txt");
			
			fw.write("CAS\tNewHydrogenCount\tOldHydrogenCount\r\n");
			
			
			for (int i = 0; i < files.length; i++) {
				String filename=files[i].getName();
				
//				if (filename.indexOf(")")>-1) continue;
								
				BufferedReader br = new BufferedReader(new FileReader(files[i]));
				MDLV2000Reader mr = new MDLV2000Reader(br);
				
				AtomContainer m=new AtomContainer();
				mr.read(m);
				
				String NHC=(String)m.getProperty("NewHydrogenCount");
				String OHC=(String)m.getProperty("OldHydrogenCount");
				
				int NewHydrogenCount=Integer.parseInt(NHC);
				int OldHydrogenCount=Integer.parseInt(OHC);
				
				String CAS=filename.substring(0,filename.indexOf("_"));
				if (NewHydrogenCount!=OldHydrogenCount) {
					fw.write(CAS+"\t"+NHC+"\t"+OHC+"\r\n");
					fw.flush();
				}
				
				
				
			} 
			
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	/** more complex than FixHydrogensOfMolfilesInFolder
	 * 
	 * @param f1
	 * @param f2
	 * @param Start
	 */
	
	public void AddHydrogensToMolFilesInFolder(File f1, File f2,int Start) {


		if (!f1.exists() || !f1.isDirectory()) {
			System.out.println("bad directory");
			return;
		}
		
		if (!f2.exists()) f2.mkdir();

//		MyHydrogenAdder ha=new MyHydrogenAdder(); 
//		HydrogenAdder haCDK=new HydrogenAdder(new SmilesValencyChecker());
		
		String filename="";
		
		try {
			System.out.print("listing files...");			
			File[] files = f1.listFiles();
			System.out.print("done\r\n");
			
			for (int i = Start; i < files.length; i++) {
				filename=files[i].getName();
				
				
				
				if (i%100==0) 
					System.out.println(i);
				
				
//					System.out.println(files[i]);
				
				File oldmolfile=new File(f1.getAbsolutePath()+"/"+files[i].getName());
				File newmolfile=new File(f2.getAbsolutePath()+"/"+files[i].getName());								

//				System.out.println(oldmolfile.getName());
				
				BufferedReader br = new BufferedReader(new FileReader(
						oldmolfile));
				MDLV2000Reader mr = new MDLV2000Reader(br);
				AtomContainer originalAtomContainer=new AtomContainer();
				
				try {
					mr.read(originalAtomContainer);
				} catch (Exception e) {
					System.out.println("Error reading "+files[i].getName());
					continue;
				}

				if (originalAtomContainer==null) {
					continue;
				}

				int OriginalHydrogenCount=this.GetHydrogenCount(originalAtomContainer);
				int NewHydrogenCount=0;

				
				IAtomContainer AtomContainer2=null;
				
				IAtomContainer AtomContainer=(IAtomContainer)originalAtomContainer.clone();
								
				
				
				FileWriter fw=new FileWriter(newmolfile);				
				MDLV2000Writer mw=new MDLV2000Writer(fw);
				mw.setWriteAromaticBondTypes(false);
				
				
//				if (filename.equals("17702-41-9_IndexNet2d.mol") || filename.equals("190-31-8_IndexNet2d.mol") || filename.equals("190-55-6_IndexNet2d.mol")) {
//					haCDK.addExplicitHydrogensToSatisfyValency(AtomContainer);
//					if (AtomContainer != null) {	
//						mw.writeAtomContainer(AtomContainer);
//					}
//					continue;
//					
//				}
				
				try {
					AtomContainer2=CDKUtilities.addHydrogens(AtomContainer);
				} catch (Exception exc) {
					exc.printStackTrace();
				}

				if (AtomContainer2 != null) {
					NewHydrogenCount=this.GetHydrogenCount(AtomContainer2);
//					System.out.println(filename+"\t"+"MyAdder");
					AtomContainer2.setProperty("AtomContainerCount","1");
					AtomContainer2.setProperty("hydrogenadder", "MyHydrogenAdder");
					AtomContainer2.setProperty("OldHydrogenCount", OriginalHydrogenCount+"");
					AtomContainer2.setProperty("NewHydrogenCount", NewHydrogenCount+"");
					AtomContainer2.removeProperty("Title");
					AtomContainer2.removeProperty("Remark");
					
					mw.write(AtomContainer2);
				}
				
				mw.close();
				fw.close();				

				
				
			}

		} catch (Exception e) {
			System.out.println("error for "+filename);
			e.printStackTrace();
		}

	}

	
	
	public static boolean HaveCarbon(IAtomContainer mol) {
		
		try {
			
		for (int i=0; i<mol.getAtomCount();i++) {

			String var = mol.getAtom(i).getSymbol();

			// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

			if (var.equals("C")) {
				return true;
			}
		}
	
		return false;

	} catch (Exception e) {
		return true;
	}
		
	}
	
	public static boolean HaveBadElement(IAtomContainer mol) {
		
		try {
						
			for (int i=0; i<mol.getAtomCount();i++) {

				String var = mol.getAtom(i).getSymbol();

				// OK: C, H, O, N, F, Cl, Br, I, S, P, Si, As, Hg, Sn

				if (!var.equals("C") && !var.equals("H") && !var.equals("O")
						&& !var.equals("N") && !var.equals("F")
						&& !var.equals("Cl") && !var.equals("Br")
						&& !var.equals("I") && !var.equals("S")
						&& !var.equals("P") && !var.equals("Si")
						&& !var.equals("As") && !var.equals("Hg")
						&& !var.equals("Sn")) {

					return true;
			
			
				}
			}
		
			
			return false;

		} catch (Exception e) {
			return true;
		}
		
		
	}
	
	
	
	
	int GetHydrogenCount(IAtomContainer m) {
		int count=0;
		
		for (int i=0;i<m.getAtomCount();i++) {
			if (m.getAtom(i).getSymbol().equals("H")) count++;
		}
		
		return count;
	}

	boolean HaveElement(IAtomContainer m,String symbol) {
		
		
		for (int i=0;i<m.getAtomCount();i++) {
			if (m.getAtom(i).getSymbol().equals(symbol)) return true;
		}
		
		return false;
	}

	
	void FixHydrogensOfMolFile(File f1,File f2,String filename) {
		
		File oldmolfile=new File(f1.getAbsolutePath()+"/"+filename);
		File newmolfile=new File(f2.getAbsolutePath()+"/"+filename);
				
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(oldmolfile));
			MDLV2000Reader mr=new MDLV2000Reader(br);
			
			AtomContainer ac=new AtomContainer();
			mr.read(ac);
			IAtomContainer AtomContainerOld=null;
			

			int count1=this.GetHydrogenCount(ac);
			int count2=0;
			
			try {
				AtomContainerOld=(IAtomContainer)ac.clone();
				ac=CDKUtilities.addHydrogens(ac);	
				
			} catch (Exception e) {
				e.printStackTrace();
			}
						
			
			if (ac != null) {
				count2=this.GetHydrogenCount(ac);
				if (count1!=count2) {
					System.out.println("hydrogen count diff for "+filename);
				}
				
				FileWriter fw=new FileWriter(newmolfile);				
				MDLV2000Writer mw=new MDLV2000Writer(fw);
				mw.setWriteAromaticBondTypes(false);
				mw.write(ac);
				
				mw.close();
				fw.close();				
			} else {
				System.out.println("Could not fix hydrogens: "+filename+", outputting old version");
				
				FileWriter fw=new FileWriter(newmolfile);				
				MDLV2000Writer mw=new MDLV2000Writer(fw);
				mw.setWriteAromaticBondTypes(false);
				mw.write(AtomContainerOld);
				
				mw.close();
				fw.close();				

			}
			
			br.close();

			

		}catch (Exception e) {
			System.out.println("error: "+filename);
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/**
	 * Creates series of mol files from SDF file from INDEXNET
	 * 
	 * @param SDFfilename
	 * @param outputfileloc
	 */
	public void CreateMolFilesFromSDFFile(String SDFfilename, String outputfileloc,
			int Start,String CASFieldName) {
	
		
		
		try {
	
			BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
	
	
			IteratingSDFReader mr = new IteratingSDFReader(br,DefaultChemObjectBuilder.getInstance());
			MDLV2000Writer mw;
	
			IAtomContainer m;
	
			int NoCASCount = 0;
			int counter = 0;
			while (mr.hasNext()) {
	
				try {
	
					m = mr.next();
					counter++;
	
					if (counter % 1000 == 0)
						System.out.println(counter);
	
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
	
	
				String CAS = m.getProperty("CAS");
	
				if (CAS.equals("-9999")) {
					NoCASCount++;
					CAS = "NoCAS-" + NoCASCount;
				}
	
				if (counter < Start)
					continue;
	
				FileWriter fw = new FileWriter(outputfileloc + "/" + CAS
						+ ".mol");
				mw = new MDLV2000Writer(fw);
				mw.write(m);
	
				fw.flush();
				fw.close();
	
			}// end while true;
	
			br.close();
	
		} catch (Exception e) {
			e.printStackTrace();
	
		}
	
	}


	/**
	 * Creates series of mol files-doesnt use cdk's reader
	 * 
	 * @param SDFfilename
	 * @param outputfileloc
	 */
	public void CreateMolFilesFromSDFFile2(String SDFfilename, String outputfileloc,
			int Start,String CASFieldName,String desc) {

		File output=new File(outputfileloc);
		if (!output.exists()) output.mkdir();
		
		try {

			BufferedReader br = new BufferedReader(new FileReader(SDFfilename));

			// System.setProperty("cdk.debug.stdout", "true");
			// System.setProperty("cdk.debugging", "true");

			String[] strData = new String[200];

			IAtomContainer m;

			int NoCASCount = 0;
			int counter = 0;

			boolean Stop=false;
			while (true){
			
				ArrayList al = new ArrayList();

				while (true) {

					try {

						String Line = br.readLine();
						al.add(Line);

						if (Line==null) {
							Stop=true;
							break;
						}
						
						if (Line.indexOf("$$$$") > -1)
							break;

					} catch (Exception e) {
						e.printStackTrace();
						break;
					}


				}// end while loop for given chemical

				if (Stop) break;
				
				String CAS = "";
				for (int i = 0; i < al.size(); i++) {
					String Line = (String) al.get(i);
					
					String CASField=">  <"+CASFieldName+">";
					              
					String CASField2="> <"+CASFieldName+">";
					                
					if (Line.indexOf(CASField)>-1 || Line.indexOf(CASField2)>-1) {
						CAS = (String) al.get(i + 1);
//						System.out.println(CAS);
						break;
					}
				}

				if (CAS.equals("")) {
					NoCASCount++;
					CAS = "NoCAS-" + NoCASCount;
				}
				
//				System.out.println(CAS+"\t"+counter++);

				FileWriter fw = new FileWriter(outputfileloc + "/" + CAS + desc
						+ ".mol");

				for (int i = 0; i < al.size(); i++) {
					String Line=(String)al.get(i);
					
					if (Line.indexOf("> (")>-1) {
						Line=Line.substring(0,Line.indexOf("> (")+1);
					}
				
					
					fw.write(Line + "\r\n");
				}

				fw.close();
				
				
			}// end while loop over chemicals in sdf file

			
			
			br.close();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	
	/**
	 * Adds cas numbers to SDF file by using CAS numbers from a text file
	 * It is assumed that ID numbers in SDF are same as in CAS text file
	 * if not it stops and outputs error msg.
	 * 
	 * @param SDFfilename
	 * @param outputfileloc
	 */
	void AddCASNumbersToSDFFile(String SDFFilepath,String outputSDFPath,String casFilePath) {

		
		try {

			BufferedReader br = new BufferedReader(new FileReader(SDFFilepath));
			BufferedReader br2 = new BufferedReader(new FileReader(casFilePath));
			
			FileWriter fw = new FileWriter(outputSDFPath);

			br2.readLine();
			
			

			String[] strData = new String[200];

			IAtomContainer m;

			int NoCASCount = 0;
			int counter = 0;

			boolean Stop=false;
			while (true){
			
				ArrayList al = new ArrayList();

				while (true) {

					try {

						String Line = br.readLine();
						al.add(Line);

						if (Line==null) {
							Stop=true;
							break;
						}
						
						if (Line.indexOf("$$$$") > -1)
							break;

					} catch (Exception e) {
						e.printStackTrace();
						break;
					}


				}// end while loop for given chemical

				if (Stop) break;
				
				String ID = "";
				String IDFieldName="MOL_ID";
				for (int i = 0; i < al.size(); i++) {
					String Line = (String) al.get(i);
					String CASField=">  <"+IDFieldName+">";
					String CASField2="> <"+IDFieldName+">";
					if (Line.indexOf(CASField)>-1 || Line.indexOf(CASField2)>-1) {
						ID = (String) al.get(i + 1);
						break;
					}
					
					//Following fixes case where have blank line between M END and first field (which messes up Marvin)
					if (Line.indexOf("M  END")>-1) {
						String Line2=(String) al.get(i+1);
						if (Line2.trim().equals("")) al.remove(i+1);
					}
				}
				
				String Line=br2.readLine();
				String IDtxtFile=Line.substring(0,Line.indexOf("\t"));
				String CAS=Line.substring(Line.indexOf("\t")+1,Line.length());
				
				if (!ID.equals(IDtxtFile)) {
					System.out.println("Mismatch for "+ID+"\t"+IDtxtFile);
					break;
				}


				for (int i = 0; i < al.size()-1; i++) {
					String Linei=(String)al.get(i);
					if (Linei.indexOf("> (")>-1) {
						Linei=Linei.substring(0,Linei.indexOf("> (")+1);
					}
					fw.write(Linei + "\r\n");
				}

				fw.write(">  <CAS>\r\n");
				fw.write(CAS+"\r\n\r\n$$$$\r\n");

			}// end while loop over chemicals in sdf file

			
			fw.close();
			br.close();
			br2.close();
			

		} catch (Exception e) {
			e.printStackTrace();

		}

	}
	

	
	
	
	void CreateTrainingPredictionSets() {
			
			String folder="ToxPredictor/DescriptorTextTables/LD50 Data Files";
			String filename="tox LD50 all.txt";
			String fileTrainingSet="tox LD50 training set.txt";
			String filePredictionSet="tox LD50 prediction set.txt";
			
			ParseChemidplus p=new ParseChemidplus();
			
			try {
				
				BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filename));
				FileWriter fwTraining=new FileWriter(folder+"/"+fileTrainingSet);
				FileWriter fwPrediction=new FileWriter(folder+"/"+filePredictionSet);
				
				String header=br.readLine();
				System.out.println(header);
				java.util.List headerlist=Utilities.Parse(header,"\t");			

				fwTraining.write(header+"\r\n");
				fwPrediction.write(header+"\r\n");
				
				int counter=0;
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					counter++;
					
					
					
					if (counter%5==0) {
						fwPrediction.write(Line+"\r\n");
					} else {
						fwTraining.write(Line+"\r\n");
					}
					
					
				}
				
				
				br.close();
				fwTraining.close();
				fwPrediction.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}
	
	
	void RandomizeOrderOfPredictionSets() {
		
		String folder="ToxPredictor/DescriptorTextTables/LD50 Data Files";
				
		String filePredictionSet="LD50 prediction set - 2d.csv";
		String filePredictionSet2="LD50 prediction set - 2d-2.csv";
		
		
		try {
			
			BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filePredictionSet));
			
			FileWriter fwPrediction=new FileWriter(folder+"/"+filePredictionSet2);
			
			String header=br.readLine();
//			System.out.println(header);
		
			
			fwPrediction.write(header+"\r\n");
			

			Vector <String>dataVector=new Vector<String>();
						
			//read in all data
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				dataVector.add(Line);
			}
			
			//now pick rows at random and write to file:
			
			while (dataVector.size()>0) {
				int num=(int)Math.round(Math.random()*dataVector.size());
				
				num--;
				
				if (num==-1) num=0;
				
				String Line=dataVector.get(num);
				
				fwPrediction.write(Line+"\r\n");
				fwPrediction.flush();
				
				dataVector.remove(num);
			}
						
			
			br.close();
			fwPrediction.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}


	void CreateSDFFileFromCASandSource2() {//appends tox field to fields in sdf
			
			String CAS="",Source="",SMILES="",Tox="",Name="";
			

			String folder="ToxPredictor/DescriptorTextTables/LD50 Data Files";						                
			String filename="tox LD50 overall set-mg_kg.csv";
			String filename2="tox LD50 overall set.csv";
			String sdfFileName="LD50.sdf";
			String d=",";

			
//			String folder="ToxPredictor/DescriptorTextTables/LC50 Data Files";
//			String filename="tox LC50 overall set-mg_L.csv";
//			String filename2="tox LC50 overall set.csv";
//			String sdfFileName="LC50.sdf";
//			String d=",";
			
			
//			String folder="ToxPredictor/chemidplus/compare";
//	    	String filename="CAS list for union of lc50 and ld50 good chemicals.txt";
//	    	String sdfFileName="LC50 and LD50 good chemicals.sdf";
//	    	String manifestFileName="LC50 and LD50 good chemicals manifest.txt";
	    	
//	        String d="\t";
//	        String d2="\t";
	
	        java.text.DecimalFormat df=new java.text.DecimalFormat("0.000");
	        
			org.openscience.cdk.smiles.SmilesGenerator sg=new org.openscience.cdk.smiles.SmilesGenerator(); 
			
			ParseChemidplus p=new ParseChemidplus();
			
			try {
				
				BufferedReader br=new BufferedReader(new FileReader(folder+"/"+filename));
				FileWriter fw=new FileWriter(folder+"/"+sdfFileName);
				FileWriter fw2=new FileWriter(folder+"/"+filename2);
				
//				FileWriter fw2=new FileWriter(folder+"/"+manifestFileName);
				
//				fw2.write("CAS Number"+d2+"SMILES"+d2+"Formula"+d2);
//				fw2.write("Molecular Weight"+d2+"Name"+"\r\n");
				
				MDLV2000Writer mw=new MDLV2000Writer(fw);
				mw.setWriteAromaticBondTypes(false);
				
				
				String header=br.readLine();
				System.out.println(header);
				fw2.write("CAS,RevisedSource,NegLogLC50_mol_L,Name\r\n");
				
				java.util.List headerlist=Utilities.Parse(header,d);			
				int Col_CAS=0;
				int Col_Source=1;				
				int Col_Tox=2;
				int Col_Name=3;
				
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					
					java.util.List list=Utilities.Parse3(Line,d);
					CAS=(String)list.get(Col_CAS);
					
//					if (CAS.equals("50-76-0")) continue;//takes forever
					if (CAS.equals("2278-50-4")) continue;//runs out of memory
					
					Source=(String)list.get(Col_Source);
					
					Tox=(String)list.get(Col_Tox);
					Name=(String)list.get(Col_Name);
					
					AtomContainer ac=p.getAtomContainerFromSource(Source,CAS);
				
					
					
					double MolecularWeight=-1;
					
					
					try {
//						IAtomContainer m2=(IAtomContainer)AtomContainer.clone();
//						ToxPredictor.Utilities.CDKUtilities.RemoveHydrogens(m2);
//						m2=(IAtomContainer)AtomContainerManipulator.removeHydrogens(m2);
//						SMILES=sg.createSMILES(m2);
						
//						MolecularWeight=this.Calculate_mw(AtomContainer);
												
						MolecularWeight=CDKUtilities.calculateMolecularWeight(ac);
//						System.out.println(MolecularWeight);
						
						
					} catch (Exception e) {
//						SMILES=sg.createSMILES(AtomContainer);
					}
					
					//calculate tox from MW:
					
					double dTox=Double.parseDouble(Tox);
					double logTox=-Math.log10(dTox/1000.0/MolecularWeight);
					Tox=df.format(logTox);
					

					//remove hydrogens:
//					ToxPredictor.Utilities.CDKUtilities.RemoveHydrogens(AtomContainer);
					ac=(AtomContainer) AtomContainerManipulator.removeHydrogens(ac);

					HashMap map=new HashMap();
					
					ac.setProperty("CAS", CAS);
					ac.setProperty("Source", Source);
					ac.setProperty("Tox", Tox);
					ac.setProperty("Name", Name);
					
					
//					System.out.println(CAS+"\t"+Tox+"\t"+MolecularWeight);
					
					fw2.write(CAS+d+Source+d+Tox+d+"\""+Name+"\""+"\r\n");
					fw2.flush();
	//				AtomContainer.setProperty("CAS",CAS);
					
					mw.write(ac);
					fw.write("$$$$\n");
					
//					fw2.write(CAS+d2+SMILES+d2+Formula+d2+df.format(MolecularWeight)+d2);
//					fw2.write(Name+"\r\n");
//					fw2.flush();
					
					
				}
				
				
				br.close();
				fw.close();
				fw2.close();
			} catch (Exception e) {
				System.out.println("Error for "+CAS+", Source="+Source);
				e.printStackTrace();
			}
			
			
		}

	private double Calculate_mw(IAtomContainer m) {
		// tried to use CDK built in methods but they suck
		// alternative method would be to use m2 which includes the hydrogens
		
		
		AtomicProperties ap=null;
		try {
			ap=AtomicProperties.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double MW=0;
				
		for (int i=0;i<=m.getAtomCount()-1;i++) {			
			IAtom a=m.getAtom(i);
			MW+=ap.GetMass(a.getSymbol());			
			MW+=a.getImplicitHydrogenCount()*ap.GetMass("H");			
		}
		
		return MW;	
	}

	public static boolean ReadUntilNextAtomContainer(BufferedReader br) {
		
		try {
			while (true) {
				String Line=br.readLine();
				if (Line==null) return false;
				if (Line.indexOf("$$$")>-1) return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	void sortSDFByCAS(String SDFfilename,String SDFfolderpath,String sortID,String SDFoutputname) {
		
		String outputfolder=SDFfolderpath+"/mol files";
		File OF=new File(outputfolder);
		if (!OF.exists()) OF.mkdir();
		
		//Create mol files from SDF:
		this.CreateMolFilesFromSDFFile2(SDFfolderpath+"/"+SDFfilename, outputfolder, 0, sortID, "");
		
		File [] files=OF.listFiles();
		
		ArrayList CASNumbers=new ArrayList();
		
		for (int i=0;i<files.length;i++) {
			File file=files[i];
			
			String filename=file.getName();
			
			String CAS=filename.substring(0,filename.indexOf("."));
			
			//add zeros to cas for sorting purposes:
			while(CAS.length()<11) {
				CAS="0"+CAS;
			}
			CASNumbers.add(CAS);
			
		}
		
		//sort cas numbers:
		Collections.sort(CASNumbers);
		
		try {
			FileWriter fw=new FileWriter(SDFfolderpath+"/"+SDFoutputname);
			
			for (int i=0;i<CASNumbers.size();i++) {
				
				String CAS=(String)CASNumbers.get(i);
				
				//remove preceding zeros from mol file name:
				while (true) {
					if (CAS.indexOf("0")==0) CAS=CAS.substring(1,CAS.length());
					else break;
				}
				
				//add mol file to SDF:
				BufferedReader br=new BufferedReader(new FileReader(outputfolder+"/"+CAS+".mol"));
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					fw.write(Line+"\r\n");
				}
				fw.flush();
				br.close();
				
				
			}
			
			fw.close();
			
			for (int i=0;i<files.length;i++) {
				files[i].delete();
			}
			OF.delete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void sortSDFByCASusingMDLReaderWriter(String SDFfilename,String SDFfolderpath,String sortID,String SDFoutputname) {
		
		String outputfolder=SDFfolderpath+"/mol files";
		File OF=new File(outputfolder);
		if (!OF.exists()) OF.mkdir();
		
		//Create mol files from SDF:
		this.CreateMolFilesFromSDFFile2(SDFfolderpath+"/"+SDFfilename, outputfolder, 0, sortID, "");
		
		File [] files=OF.listFiles();
		
		ArrayList CASNumbers=new ArrayList();
		
		for (int i=0;i<files.length;i++) {
			File file=files[i];
			
			String filename=file.getName();
			
			String CAS=filename.substring(0,filename.indexOf("."));
			
			//add zeros to cas for sorting purposes:
			while(CAS.length()<11) {
				CAS="0"+CAS;
			}
			CASNumbers.add(CAS);
			
		}
		
		//sort cas numbers:
		Collections.sort(CASNumbers);
		
		try {
			FileWriter fw=new FileWriter(SDFfolderpath+"/"+SDFoutputname);
			MDLV2000Writer mw=new MDLV2000Writer(fw);
			
			for (int i=0;i<CASNumbers.size();i++) {
				
				String CAS=(String)CASNumbers.get(i);
				
				//remove preceding zeros from mol file name:
				while (true) {
					if (CAS.indexOf("0")==0) CAS=CAS.substring(1,CAS.length());
					else break;
				}
				
				

				AtomContainer m=new AtomContainer();
				MDLV2000Reader mr=new MDLV2000Reader(new FileInputStream(outputfolder+"/"+CAS+".mol"));
				mr.read(m);
				mr.close();
				
				
				
				boolean haveWeirdBO=false;
				
				for (int bond=0;bond<m.getBondCount();bond++) {
					
					
					if (m.getBond(bond).getOrder().numeric()>3) {
//						System.out.println(CAS+"\t"+m.getBond(bond).getOrder());
						haveWeirdBO=true;
						break;
					}
				}
				if (haveWeirdBO) System.out.println(CAS+"\tWeird Bond Order!");
				
				if (ToxPredictor.misc.ParseChemidplus.IsSalt(m)) {
					System.out.println(CAS+"\tSalt");
				}

				
				m.setProperty("CAS",CAS);
				mw.write(m);
				fw.write ("$$$$\n");
				
				fw.flush();
				
				
			}
			
			fw.close();
			
			for (int i=0;i<files.length;i++) {
				files[i].delete();
			}
			OF.delete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	void GetFieldFromMolFilesInFolder(String folder,String fieldname,String outputfilepath) {
		File Folder=new File(folder);
		File [] files=Folder.listFiles();

		try {
			
			FileWriter fw=new FileWriter(outputfilepath);
			fw.write("CAS\t" + fieldname+"\r\n");
			
			for (int i = 0; i < files.length; i++) {
				File file = files[i];

				

				AtomContainer m=new AtomContainer();
				MDLV2000Reader mr=new MDLV2000Reader(new FileInputStream(file));
				mr.read(m);


				String filename = file.getName();
				String CAS = filename.substring(0, filename.indexOf("."));
				String property = (String) m.getProperty(fieldname);
				fw.write(CAS + "\t" + property+"\r\n");
				fw.flush();

			}

			fw.close();
		} catch (Exception e) {
		e.printStackTrace();
	}

		
	}
	
	
	public static void main(String[] args) {

		MolFileUtilities m = new MolFileUtilities();
		
		m.testReadAsString();
		
		//******************************************************************************************************
//		m.createTDS_Files();
		
		
//		String folder="L:/Priv/Cin/NRMRL/CompTox/javax/cdk/ToxPredictor/DescriptorTextTablesMOA_2014_01_07_redo_sort/LC50_Broad data files";
//		String inputSDFpath=folder+"/overall set.sdf";
//		String outputSDFpath=folder+"/overall set uncharged.sdf";
//		m.makeSDF_Noncharged(inputSDFpath, outputSDFpath);
		

//		String folder="L:/Priv/Cin/NRMRL/CompTox/comptox/mildred/DIPPR Update 2015";
//		m.sortSDFByCAS("chemicals.sdf", folder, "CAS", "chemicals sort by cas.sdf");
		
		
		// ************************************************************************
		
////		String sdffilepath="C:/Documents and Settings/tmarti02/My Documents/comptox/benfenati/BCF discrepancies/sdf/neutralized_1039.sdf";
//		String sdffilepath="ToxPredictor/structures/ValidatedStructures2d.sdf";
//		MolFileUtilities.LoadFromSDF2(sdffilepath);
		
// ************************************************************************
//		String f="C:/Documents and Settings/tmarti02/My Documents/comptox/epi suite data sets";
//		String SDFfilename=f+"/EPI_Wskowwin_Data_SDF.sdf";
//		String outputfileloc=f+"/EPI_Wskowwin_Data_SDF.txt";		
//		m.CreateCASListFromSDFFile(SDFfilename, outputfileloc);
// ************************************************************************
		
		
		//mismatch
//		String f="C:/Documents and Settings/tmarti02/My Documents/comptox/mutagenicity-unc";
////		String set="MODELING";
//		String set="EXTERNAL";
//		String SDFFilePath=f+"/AMES_"+set+"_SET.SDF";
//		String outputSDFFilePath=f+"/AMES_"+set+"_SET w CAS.SDF";
//		String CASFilePath=f+"/CAS numbers for "+set+" set.txt";
		
//		m.AddCASNumbersToSDFFile(SDFFilePath,outputSDFFilePath,CASFilePath);

		
//		String SDFfolder="C:/Documents and Settings/tmarti02/My Documents/comptox/mutagenicity-unc";
//		String SDFname="AMES_OVERALL_SET w CAS dearomatize.SDF";
//		String SDFoutputname="AMES_OVERALL_SET w CAS dearomatize CAS sort.SDF";
//		m.sortSDFByCAS(SDFname,SDFfolder,"CAS",SDFoutputname );
		
//		String SDFfolder="C:/Documents and Settings/tmarti02/My Documents/comptox/cancer/CPDB v5d";
//		String SDFname="CPDBAS_v5d.sdf";
//		String SDFoutputname="CPDBAS_v5d CAS sort.SDF";
//		String CASFieldName="TestSubstance_CASRN";
//		m.sortSDFByCAS(SDFname,SDFfolder,CASFieldName,SDFoutputname );
		
//		String folder="C:/Documents and Settings/tmarti02/My Documents/javax/cdk/ToxPredictor/structures/chemidplus2d";
//		String fieldname="PUBCHEM_IUPAC_SYSTEMATIC_NAME";
//		String outputfilepath="C:/Documents and Settings/tmarti02/My Documents/javax/cdk/ToxPredictor/structures/IUPAC_Systematic_Names_chemidplus2d.txt";
//		m.GetFieldFromMolFilesInFolder(folder, fieldname,outputfilepath);
		
		// ****************************************************
//		m.CompareStructuresInTwoFolders();
		// ****************************************************
//		String folder="ToxPredictor/DescriptorTextTables/LD50_TopKat Data Files";
//		String set="prediction";
//		String SDFFileName="LD50_"+set+".sdf";
//		m.BreakupSDF(folder, SDFFileName, folder+"/"+set,true,1000);
//		
//		set="training";
//		SDFFileName="LD50_"+set+".sdf";
//		m.BreakupSDF(folder, SDFFileName, folder+"/"+set,true,1000);
//				
		// ****************************************************
		
//		m.RenameFiles2();
//		
		//****************************************************

//		String SDFfilename="ToxPredictor/structures/NameStructuresMutagensMaceBarron.sdf";
//		String outputfileloc="ToxPredictor/structures/Name";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");		

		
		
				

		
//		String SDFfilename="ToxPredictor/structures/get name structures/name structures igc50 schwobel.sdf";
//		String outputfileloc="ToxPredictor/structures/Name";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");		

//		String SDFfilename="ToxPredictor/chemidplus/parse/additional epiphys mp chemicals/name structures 3.sdf";
//		String outputfileloc="ToxPredictor/structures/NameNew";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");		

//		String SDFfilename="ToxPredictor/structures/name structures extra thermal conductivity.sdf";
//		String outputfileloc="ToxPredictor/structures/Name";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");		
		
//		String SDFfilename="ToxPredictor/structures/name structures extra bcf 701 chemical.sdf";
//		String outputfileloc="ToxPredictor/structures/NameNew";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");
		
//		String f="C:/Documents and Settings/tmarti02/My Documents/comptox/0 Physical Properties/ST";
////		String SDFfilename=f+"/name structures from Jaspar Name-2.sdf";
////		String outputfileloc=f+"/Jaspar Name Structures-2";
//		String SDFfilename=f+"/name structures from chemidplus best name-2.sdf";
//		String outputfileloc=f+"/Chemidplus Name Structures-2";
////		String SDFfilename=f+"/name structures from overall best name.sdf";
////		String outputfileloc=f+"/Overall Best Name Structures";
//
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");		

//		String SDFfilename="ToxPredictor/DescriptorTextTables/reproTox Data Files/Develop_tox_260907.sdf";
//		String outputfileloc="ToxPredictor/DescriptorTextTables/reproTox Data Files/mol files";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS Num", "");		

//		m.AddHydrogensToMolFilesInFolder(new File("ToxPredictor/structures/Name"),new File("ToxPredictor/structures/Name_withH"),0);
		
//		m.AddHydrogensToMolFilesInFolder(new File("C:/Documents and Settings/tmarti02/My Documents/comptox/0 Physical Properties/ST/Name structures"),new File("C:/Documents and Settings/tmarti02/My Documents/comptox/0 Physical Properties/ST/Name structures with H"),0);
		
		

//*******
//		String SDFfilename="ToxPredictor/structures/NameStructuresMutagens1.sdf";
//		String SDFfilename="ToxPredictor/structures/NameStructuresMutagens2.sdf";
//		String SDFfilename="ToxPredictor/structures/NameStructuresMutagens3.sdf";
//		String SDFfilename="ToxPredictor/structures/name structures additional viscosity chemicals.sdf";
//		String SDFfilename="ToxPredictor/structures/name structures extra fp chemicals.sdf";\
//		String SDFfilename="ToxPredictor/structures/name structures extra density chemicals-2.sdf";
//		String outputfileloc="ToxPredictor/structures/Name";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");
//		m.AddHydrogensToMolFilesInFolder(new File("ToxPredictor/structures/Name"),new File("ToxPredictor/structures/Name_withH"),0);
		

//*******		

//		String SDFfilename="ToxPredictor/structures/NIST_BP_Names.sdf";
//		String outputfileloc="ToxPredictor/structures/Name";
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, "CAS", "");		
	
//		m.AddHydrogensToMolFilesInFolder(new File("ToxPredictor/structures/Name"),new File("ToxPredictor/structures/Name_withH"),0);

//*******
//		String SDFfilename="ToxPredictor/benfenati/643 BCF set/BCf_Names.sdf";
//		String SDFfilename="C:/Documents and Settings/tmarti02/My Documents/comptox/structures for doug/benfenati reproTox need name structure_DY.sdf";
//		String SDFfilename="C:/Documents and Settings/tmarti02/My Documents/comptox/structures for doug/CPDB v5 need names.sdf";
//		String SDFfilename="ToxPredictor/structures/need StructureData2-9-30-09.sdf";
//		String SDFfilename="ToxPredictor/structures/Need name structures for chemicals in union of data sets.sdf";
//		String SDFfilename="ToxPredictor/structures/NameStructures Appendix A chemicals.sdf";
//		String SDFfilename="C:/Documents and Settings/tmarti02/My Documents/comptox/0 Physical Properties/WS/need name structure sdf files/need name structure.sdf";
//		
//		String outputfileloc="ToxPredictor/structures/NameNew";
/////		String CASFieldName="TestSubstance_CASRN";
//		String CASFieldName="CAS";
//////		
//		m.CreateMolFilesFromSDFFile2(SDFfilename, outputfileloc, 0, CASFieldName, "");		
////		
//////		m.AddHydrogensToMolFilesInFolder(new File("ToxPredictor/structures/Name"),new File("ToxPredictor/structures/Name_withH"),0);
//		m.AddHydrogensToMolFilesInFolder(new File("ToxPredictor/structures/NameNew"),new File("ToxPredictor/structures/Name_withH"),0);
//////		m.AddHydrogensToMolFilesInFolder(new File("ToxPredictor/structures/Other"),new File("ToxPredictor/structures/Other"),0);
		
		// ****************************************************
		//Extract mol files from 2d structures with no hydrogen sdf that was obtained from marvin:
//		String SDFFileName="ToxPredictor/structures/ValidatedStructures2d.sdf";
//		String outputfileloc="ToxPredictor/structures/ValidatedStructures2d";
//		m.CreateMolFilesFromSDFFile2(SDFFileName,outputfileloc,0,"CAS","");
		// ****************************************************
		
//		String inputfilepath="ToxPredictor/structures/57-06-7.mol";
//		String outputfilepath="ToxPredictor/structures/57-06-7_noH.mol";
//		m.RemoveHydrogensFromMolFile(inputfilepath, outputfilepath);
		
		//since Marvin for some reason doesn't delete all H's we can manually delete em:
//		String inputfolderpath="ToxPredictor/structures/ValidatedStructures2d";
//		String outputfolderpath=inputfolderpath;
//		m.RemoveHydrogensFromMolFilesInFolder(inputfolderpath, outputfolderpath);

//		m.CreateMolFilesFromSDFFile2("ToxPredictor/structures/Structures from Name-Rainbow trout.sdf", "ToxPredictor/structures/Name", 0, "CAS", "");
		
		// ****************************************************
		
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/DescriptorTextTables/IGC50 Data Files/IGC50.sdf", "ToxPredictor/DescriptorTextTables/IGC50 Data Files/mol files", 0, "CAS", "_IGC50Smiles");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/DescriptorTextTables/Cancer_MR Data Files/CPDBAS_v4a_1481_15Jun2007.sdf", "ToxPredictor/DescriptorTextTables/Cancer_MR Data Files/mol files", 0, "TestSubstance_CASRN", "");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/DescriptorTextTables/Cancer_MR_NTP Data Files/CPDBAS_v5a_1547_25Oct2007.sdf", "ToxPredictor/DescriptorTextTables/Cancer_MR_NTP Data Files/mol files", 0, "TestSubstance_CASRN", "");
		
//		m.CreateSDFFileFromCASandSource3();
		
//		if (true) return;
		
		
//		m.CreateSDFFileFromCASandSource2();
		

//		p.CreateMolFilesFromSmilesList(0);		
//		p.FindMissingMolFilesFromSmilesList();
		
//		m.RandomizeOrderOfPredictionSets();
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/DescriptorTextTables/LD50 Data Files/LD50.sdf", "ToxPredictor/chemidplus/structures/LD50Database", 0, "CAS", "_LD50Database");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/DescriptorTextTables/MISSING/missing chemicals.sdf", "ToxPredictor/chemidplus/structures/Missing", 0, "CAS", "_Missing");

//		m.CreateMolFilesFromSDFFile2("ToxPredictor/SDF Files/LD50 Marvin Smiles.sdf", "ToxPredictor/chemidplus/structures/Smiles_Marvin", 0, "CAS", "_Smiles_Marvin");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/SDF Files/smilecas from Marvin.sdf", "ToxPredictor/chemidplus/structures/Smiles_Marvin", 0, "CAS", "_Smiles_Marvin");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/DescriptorTextTables/LC50 Data Files/FHM_Structures_From_Name.sdf", "ToxPredictor/chemidplus/structures/Name", 0, "CAS", "_Name2d");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/chemidplus/compare/LC50 and LD50 good chemicals.sdf", "ToxPredictor/chemidplus/structures/ValidatedStructures", 0, "CAS", "");
		
//		m.CreateMolFilesFromSDFFile2("ToxPredictor/chemidplus/sdf_from_name/IGC50names.sdf", "ToxPredictor/chemidplus/structures/Name", 0, "CAS", "_Name2d");
		
//		ToxPredictor\DescriptorTextTables\MISSING
		
//		String sdffile=;
		
//		if (true) return;
		
		
//		File f1=new File("N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol");
//		File f2=new File("ToxPredictor/chemidplus/structures/DougDB3d");
//		p.FixAromaticityOfMolFilesInFolder(f1,f2);
//		p.FixAromaticityOfMolFile(f1,f2,"100-47-0.mol");

//***********************************************************************		
//		File f1=new File("ToxPredictor/structures/NIST2d");
//		File f2=new File("ToxPredictor/structures/NIST2d_withH");
//		m.AddHydrogensToMolFilesInFolder(f1,f2,0);

		
//		File f1=new File("ToxPredictor/NIST_Download/NIST2d");
//		File f2=new File("ToxPredictor/NIST_Download/NIST2d_withH");
//		m.AddHydrogensToMolFilesInFolder(f1,f2,0);

//		File f1=new File("ToxPredictor/DescriptorTextTables/IGC50 Data Files/mol files");
//		File f2=new File("ToxPredictor/DescriptorTextTables/IGC50 Data Files/mol files with hydrogens");
		
//		File f1=new File("ToxPredictor/NIST_Download/NIST2d");
//		File f2=new File("ToxPredictor/NIST_Download/NIST2d_withH");

//		File f1=new File("ToxPredictor/structures/Name");
//		File f2=new File("ToxPredictor/structures/Name_withH");
//
		
//		File f1=new File("ToxPredictor/DescriptorTextTables/Cancer data files/mol files v5a");
//		File f2=new File("ToxPredictor/DescriptorTextTables/Cancer data files/mol files v5a with H");
//		
//		m.AddHydrogensToMolFilesInFolder(f1,f2,0);
		

		
		//		if (true) return;
		
//		File f1=new File("ToxPredictor/chemidplus/structures/DSSTOX");
//		File f2=new File("ToxPredictor/chemidplus/structures/DSSTOX_withH");

//		File f1=new File("ToxPredictor/chemidplus/structures/Name");
//		File f2=new File("ToxPredictor/chemidplus/structures/Name_withH");


//		File f1=new File("ToxPredictor/chemidplus/structures/Smiles_Marvin");
//		File f2=new File("ToxPredictor/chemidplus/structures/Smiles_Marvin_withH");

//		File f1=new File("ToxPredictor/NIST_Download/NIST2d");
//		File f2=new File("ToxPredictor/NIST_Download/NIST2d_withH");

//		File f1=new File("ToxPredictor/chemidplus/structures/INDEXNET2d");
//		File f2=new File("ToxPredictor/chemidplus/structures/INDEXNET2d_withH");
		
//		System.out.println(f2.list().length);
//		System.out.println(f2.list().length);

//		File f1=new File("ToxPredictor/Structures/Name");
//		File f2=new File("ToxPredictor/Structures/Name_withH");
//		
//		m.FixHydrogensOfMolFilesInFolder(0,f1, f2);
//		m.AddHydrogensToMolFilesInFolder(f1, f2, 0);
//		m.AddHydrogensToMolFilesInFolderForSmilesMarvin(f1,f2,0);
//		m.EvaluateResultsofAddHydrogens(f2);
		
		
//		if (true) return;

		
//		*****************************************************************
		
		
//		p.CombineTextFiles(new File("ToxPredictor/chemidplus/sdf_from_name/SDF Files With Structures"),new File("ToxPredictor/chemidplus/sdf_from_name/Good_Data_Structures.sdf"),false,"sdf");
//		if (true) return;
		
		
		//*****************************************************************
//		String folder="ToxPredictor/chemidplus/sdf_from_name";
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_1_1000.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_1000_1998.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_1999_3000.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_3001_4000.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_4001_5000.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_5001_6000.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_6001_7000.sdf",folder);
//		p.OmitMissingStructuresFromSDFFile("Good_Data_Structures_7001_8116.sdf",folder);
//		if (true) return;
		
		
//		folder="ToxPredictor/SDF Files";
//		p.OmitMissingStructuresFromSDFFile("DSSToxMaster_v1a_8804_10Apr2006.sdf",folder);
					
		
		//********************************************************************
		
//		String sdffolder="ToxPredictor/SDF Files";		
//		String filename="DSSToxMaster_v1a_8804_10Apr2006.sdf";	
//		p.CreateMolFilesFromSDFFile2(sdffolder+"/"+filename,"ToxPredictor/chemidplus/structures/DSSTOX",0,"TestSubstance_CASRN","_DSSTOX2d");
		
//		String sdffolder="ToxPredictor/chemidplus/sdf_from_name";		
//		String filename="Good_Data_Structures.sdf";		
//		p.CreateMolFilesFromSDFFile2(sdffolder+"/"+filename,"ToxPredictor/chemidplus/structures/Name",0,"CAS","_Name2d");
		
		if (true) return;
		
		// ******************************************************************
		
//		p.Bob("1_1000");
//		p.Bob("1000_1998");
//		p.Bob("1999_3000");
//		p.Bob("3001_4000");
//		p.Bob("4001_5000");
//		p.Bob("5001_6000");
//		p.Bob("6001_7000");
//		p.Bob("7001_8116");
		
//		File folder=new File("ToxPredictor/chemidplus/sdf_from_name/SDF Files Without Structures/notes");
//		File combofile=new File("ToxPredictor/chemidplus/sdf_from_name/notefile.txt");
//		p.CombineTextFiles(folder ,combofile,false,"txt");
//		
//		if (true) return;
				
		
//		p.DisplayMolFile("100-01-6",p.DougMolFolder);
		
//
//		System.out.println("Mol file in jar:");
//		p.DisplayMolFileInJar("coords/288-13-1.mol");
//		System.out.println("Original mol file:");
//		p.DisplayMolFile("142-71-2", p.NIHMolFolder);
//		if (true)
//			return;
		
		
//		String NIHMolFolder="N:/NRMRL-PRIV/CompTox/3Dcoordinates/database/coords";
//		IAtomContainer AtomContainer = p.LoadChemicalFromMolFile("70-25-7",NIHMolFolder);		
//		AtomContainerViewer2D.display(AtomContainer,true,"mol");
//		
//		if (true) return;
		
		
//		 ***********************************************************
		
//		p.FindMolFilesWithoutHydrogens();
		
//		p.RenameFiles();

		
		
//		p.FindIncompleteFiles();

//		 ***********************************************************
		
//		p.DownloadNISTMolFiles();
		

		
		
		//p.CreateCASListFromSDFFile("ToxPredictor/SDF Files/fixed3d.sdf","ToxPredictor/chemidplus/DougCASList.txt");

//		p.CreateMolFilesFromSDFFile("ToxPredictor/SDF Files/INDEXNET_Nov06.sdf","ToxPredictor/chemidplus/structures/INDEXNET_Nov06_2d",28000);
//		p.MoveMolFilesWithNoAtoms();
		
//		System.out.println(p.HaveMolFileInJar("100207-68-9"));
//		IAtomContainer m=p.LoadChemicalFromMolFileInJar("71-43-2");
//		System.out.println(m.getAtomCount());


	}


	public void CreateSDFFromMolFilesInFolder(String folderPath,String SDFFilePath,boolean includeDollarSigns) {
		
		File Folder=new File(folderPath);
		
		File [] files=Folder.listFiles();
		
		
		try {
			
			FileWriter fw=new FileWriter(SDFFilePath);
			
			for (int i=0;i<files.length;i++) {
				File filei=files[i];
				System.out.println(filei.getName());
				
				BufferedReader br=new BufferedReader(new FileReader(files[i]));
				
				while (true) {
					String Line=br.readLine();
					if (Line==null) break;
					fw.write(Line+"\r\n");
				}
				br.close();
				if (includeDollarSigns) fw.write("$$$$\r\n");
				fw.flush();
				
			}
			
			
			
		} catch (Exception ex) { 
			ex.printStackTrace();
		}
		
		
	}

	public static AtomContainerSet LoadFromSdfString(String mol) {

		//https://stackoverflow.com/questions/5720524/how-does-one-create-an-inputstream-from-a-string

		try {
			
//			InputStream is = new ByteArrayInputStream( mol.getBytes() );
			
//			For multi-byte support use:
			InputStream is = new ByteArrayInputStream(Charset.forName("UTF-8").encode(mol).array());
			
			//TODO- make sure encoding is right- 8 or 16
			
			IteratingSDFReader isr=new IteratingSDFReader(is, DefaultChemObjectBuilder.getInstance());
			
			AtomContainerSet acs=new AtomContainerSet();
			
			while (isr.hasNext()) {
				acs.addAtomContainer(isr.next());
			}
			
			
			return acs;
			
		} catch (Exception ex) {
			return null;
		}

		
	}
	
	void testReadAsString () {
		
		
		try {
			
			String folder="L:/Priv/Cin/NRMRL/CompTox/javax/web-test/todd";
			String filename="55-63-0.mol";
			String filepath=folder+"/"+filename;
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			String data="";
			
			while (true) {
				
				String Line=br.readLine();
				
				if (Line!=null) {
					data+=Line+"\n";	
				} else {
					break;
				}
				
			}
			
			System.out.println(data);
			AtomContainerSet acs=this.LoadFromSdfString(data);
			AtomContainer ac=(AtomContainer) acs.getAtomContainer(0);
			System.out.println(ac.getAtomCount());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}
	
	public static String getAtomContainerStringFromDashboard(String CAS) {

		String strURL = "https://actorws.epa.gov/actorws/dsstox/v02/molfile.json?casrn=" + CAS;

		try {
			URL website = new URL(strURL);
			URLConnection connection = website.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			JsonParser jp = new JsonParser();
			JsonObject jo = (JsonObject) jp.parse(in);

			String strMolFile = jo.get("DataRow").getAsJsonObject().get("molfile").getAsString();

			return strMolFile;

		} catch (Exception ex) {
			return null;
		}

	}

	public static AtomContainerSet getAtomContainerSetFromDashboard(String CAS) {

		String strURL = "https://actorws.epa.gov/actorws/dsstox/v02/molfile.json?casrn=" + CAS;

		try {
			URL website = new URL(strURL);
			URLConnection connection = website.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			JsonParser jp = new JsonParser();
			JsonObject jo = (JsonObject) jp.parse(in);

			String strMolFile = jo.get("DataRow").getAsJsonObject().get("molfile").getAsString();

			AtomContainerSet acs = MolFileUtilities.LoadFromSdfString(strMolFile);

			acs.getAtomContainer(0).setProperty("CAS", CAS);

			// System.out.println(acs.getAtomContainer(0).getAtomCount());

			return acs;

		} catch (Exception ex) {
			return null;
		}

	}

}



