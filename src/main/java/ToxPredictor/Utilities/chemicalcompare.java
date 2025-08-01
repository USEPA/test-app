package ToxPredictor.Utilities;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.io.*;

import weka.core.*;
import weka.core.converters.CSVLoader;

import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.tools.manipulator.RingSetManipulator;

//import QSAR.validation.CSVLoader;

import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.misc.ParseChemidplus;
import java.util.Map.Entry;

public class chemicalcompare {

	public static String methodFingerPrinter="fingerprinter";
	public static String method2dDescriptors="2ddescriptors";
	public static String methodHybrid="hybrid";
	public static String methodUIT="UIT";

	void compareStructuresInMyToxicityFolders(String folder1,String folder2) {
		
		File Folder1=new File(folder1+"/ToxRuns");
		
		File [] Folders1=Folder1.listFiles();
		for (int i=0;i<Folders1.length;i++) {
			File Folder1i=Folders1[i];
			String filename=Folder1i.getName();
			if (filename.indexOf("ToxRun")==-1) continue;
			
			String CAS=filename.substring("ToxRun".length()+1,filename.length());
			CAS=CAS.trim();

			String strDescriptorFile1=Folder1i.getAbsolutePath()+File.separator+"StructureData"+File.separator+"2d_descriptors.txt";			
			String strDescriptorFile2=folder2+"/ToxRuns/ToxRun_"+CAS+File.separator+"StructureData"+File.separator+"2d_descriptors.txt";;
			
			File DescriptorFile1=new File(strDescriptorFile1);
			File DescriptorFile2=new File(strDescriptorFile2);
			
			boolean isEqual=this.CompareDescriptorTextFiles(CAS,DescriptorFile1, DescriptorFile2);
			
//			if (!isEqual)
//				System.out.println(CAS+"\t"+isEqual);
			
//			if (i%100==0) System.out.println(i);
			
		}
		
	}
	
	
	
	boolean CompareDescriptorTextFiles(String CAS,File f1,File f2) {
		
		try {
			BufferedReader br1=new BufferedReader(new FileReader(f1));
			BufferedReader br2=new BufferedReader(new FileReader(f2));
			
			br1.readLine();
			br2.readLine();
			
			double tol=0.0000000001;
			while (true) {
				String Line1=br1.readLine();
				if (Line1==null) break;
				String Line2=br2.readLine();
				
				String desc=Line1.substring(0,Line1.indexOf("|"));
				String strVal1=Line1.substring(Line1.indexOf("|")+1,Line1.length());
				String strVal2=Line2.substring(Line2.indexOf("|")+1,Line2.length());
				
				double dval1=Double.parseDouble(strVal1);
				double dval2=Double.parseDouble(strVal2);
				
				double diff=Math.abs(dval1-dval2);
				
				if (diff>tol) {
					System.out.println(CAS+"\t"+desc+"\t"+diff);
					br1.close();
					br2.close();
					return false;
				}
				
//				System.out.println(desc+);
			}
			
			
			br1.close();
			br2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	
	void lookatdescriptorsinMyToxicityFolder(String property,String folder) {
		
		File Folder2=new File(folder+"/ToxRuns");
		
		File [] folders=Folder2.listFiles();
		for (int i=0;i<folders.length;i++) {
			File folderi=folders[i];
			String filename=folderi.getName();
			if (filename.indexOf("ToxRun")==-1) continue;
			
			String CAS=filename.substring("ToxRun".length()+1,filename.length());
			CAS=CAS.trim();

			String descriptorFile=folderi.getAbsolutePath()+File.separator+"StructureData"+File.separator+"2d_descriptors.txt";
			
			String propVal=this.getPropertyFromDescriptorFile(descriptorFile, property);
			System.out.println(CAS+"\t"+propVal);
			
			
		}
		
	}
	
	String getPropertyFromDescriptorFile(String filepath,String property) {
		String val="";
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			br.readLine();//header
			
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				String currentProp=Line.substring(0,Line.indexOf("|"));
				if (currentProp.equals(property)) {
					val=Line.substring(Line.indexOf("|")+1,Line.length());					
					break;
				}
				
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return val;
		
	}
	
	public static boolean isIsomorphUIT(IAtomContainer mol1,IAtomContainer mol2) {
		
		
		org.openscience.cdk.isomorphism.UniversalIsomorphismTester uit=new org.openscience.cdk.isomorphism.UniversalIsomorphismTester();
		
		DescriptorFactory df = new DescriptorFactory(false);
		
		df.Normalize(mol1);
		df.Normalize(mol2);

		mol1=CDKUtilities.addHydrogens(mol1);
		mol2=CDKUtilities.addHydrogens(mol2);
		
		try {
			return uit.isIsomorph(mol1, mol2);
		
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	/**
	 * Checks isomorphism- need to send it ringsets
	 * Should add hydrogens before comparing
	 * @param mol1
	 * @param mol2
	 * @param rs1
	 * @param rs2
	 * @return
	 */
	public static boolean isIsomorphFingerPrinter(IAtomContainer mol1, IAtomContainer mol2,IRingSet rs1,IRingSet rs2) {

		Fingerprinter fp = new Fingerprinter();
		DescriptorFactory df = new DescriptorFactory(false);

		try {

			// MoleculeViewer2D.display(mol,true,"mol");
			// MoleculeViewer2D.display(mol1,true,"mol1");

			// make things like nitro groups into charged versions:
			df.Normalize(mol1);
			df.Normalize(mol2);

			boolean isSubset = false;
			boolean subset1 = false;

			BitSet superBS = fp.getFingerprint(mol1);
			BitSet subBS = fp.getFingerprint(mol2);
			isSubset =isSubset(superBS, subBS); // changed from FingerPrinterTool to local method by TMM
			subset1 = isSubset(subBS, superBS);

			if (!isSubset || !subset1)
				return false;


			if (rs1.getAtomContainerCount() != rs1.getAtomContainerCount())
				return false;

			RingSetManipulator.sort(rs1);
			RingSetManipulator.sort(rs2);

			for (int i = 0; i < rs1.getAtomContainerCount(); i++) {
				int size1 = rs1.getAtomContainer(i).getAtomCount();
				int size2 = rs2.getAtomContainer(i).getAtomCount();
				if (size1 != size2)
					return false;
			}

			// SSSRFinder s=new SSSRFinder(m);
			// rs=s.findEssentialRings();

			// SSSRFinder s = new SSSRFinder(m);
			// srs = s.findEssentialRings();

			return true;

		} catch (Exception e) {

			return false;
			
			
		}
	}
	//added local copy of this method
	public static boolean isSubset(BitSet bs1, BitSet bs2)
	{
		BitSet clone = (BitSet) bs1.clone();
		clone.and(bs2);
		if (clone.equals(bs2))
		{
			return true;
		}
		return false;
	}
	
	public static boolean isIsomorphFingerPrinter(IAtomContainer mol, IAtomContainer mol1) {

		Fingerprinter fp = new Fingerprinter();
//		HydrogenAdder ha = new HydrogenAdder();
		DescriptorFactory df = new DescriptorFactory(false);

		try {

			// MoleculeViewer2D.display(mol,true,"mol");
			// MoleculeViewer2D.display(mol1,true,"mol1");

			// make things like nitro groups into charged versions:
			df.Normalize(mol);
			df.Normalize(mol1);
			
			mol=CDKUtilities.addHydrogens(mol);
			mol1=CDKUtilities.addHydrogens(mol1);

//			ha.addExplicitHydrogensToSatisfyValency(mol);
//			ha.addImplicitHydrogensToSatisfyValency(mol);
//
//			ha.addExplicitHydrogensToSatisfyValency(mol1);
//			ha.addImplicitHydrogensToSatisfyValency(mol1);

			boolean isSubset = false;
			boolean subset1 = false;

			BitSet superBS = fp.getFingerprint(mol);
			BitSet subBS = fp.getFingerprint(mol1);
			isSubset = isSubset(superBS, subBS);
			subset1 = isSubset(subBS, superBS);

			if (!isSubset || !subset1)
				return false;

			AllRingsFinder arf = new AllRingsFinder();
			arf.setTimeout(60000);
			
			IRingSet rs = arf.findAllRings(mol);
			IRingSet rs1 = arf.findAllRings(mol1);

			if (rs.getAtomContainerCount() != rs1.getAtomContainerCount())
				return false;

			RingSetManipulator.sort(rs);
			RingSetManipulator.sort(rs1);

			for (int i = 0; i < rs.getAtomContainerCount(); i++) {
				int size = rs.getAtomContainer(i).getAtomCount();
				int size1 = rs1.getAtomContainer(i).getAtomCount();
				if (size != size1)
					return false;
			}

			// SSSRFinder s=new SSSRFinder(m);
			// rs=s.findEssentialRings();

			// SSSRFinder s = new SSSRFinder(m);
			// srs = s.findEssentialRings();

			return true;

		} catch (Exception e) {

//			javax.swing.JOptionPane.showMessageDialog(null, e);
			System.out.println(e);
			return false;
		}

	}
	
	//goes through CSV file with 2d descriptor values and reports 2d isomers:
	// it then outputs a new csvfile with the 2d isomers omitted and the avg 
	//toxicity used for the nonomitted compounds
	public static void FindDuplicates(String csvfilepath,String newcsvfilepath) {
		java.text.DecimalFormat df3=new java.text.DecimalFormat("0.000");
		try {
			CSVLoader atf = new CSVLoader();
			
			FileWriter fw=new FileWriter(newcsvfilepath);
			
			atf.setSource(new java.io.File(csvfilepath));
			Instances dataset=atf.getDataSet();
			
//			System.out.println("dataset loaded");

			for (int ii=0;ii<dataset.numAttributes();ii++) {
				if (dataset.attribute(ii).name().indexOf(",")>-1) {
					fw.write("\""+dataset.attribute(ii).name()+"\"");
				} else {
					fw.write(dataset.attribute(ii).name());
				}
				
				if (ii<dataset.numAttributes()-1) fw.write(",");				
			}
			fw.write("\r\n");
			fw.flush();
			
			for (int i=0;i<dataset.numInstances();i++) {
				Instance instancei=dataset.instance(i);

				String msg=instancei.stringValue(0);
				double avgVal=instancei.value(1);
				int avgCount=1;
				
				for (int j=i+1;j<dataset.numInstances();j++) {
					Instance instancej=dataset.instance(j);
					
					if (IsMatch2d(instancei,instancej)) {
//						System.out.println("Match:"+instancei.stringValue(0)+"\t"+instancej.stringValue(0)+"\t"+i+"\t"+j);

						msg+=","+instancej.stringValue(0);
						avgVal+=instancej.value(1);
						avgCount++;
						dataset.delete(j);
						j--;
					}
				}
				
//				if (avgCount>1) System.out.println("avgCount="+avgCount);
				
				fw.write(instancei.stringValue(0)+",");
				
				avgVal/=(double)avgCount;
				
				fw.write(df3.format(avgVal)+",");
				
				
				
				
				if (avgCount==2) System.out.println(msg+",,,"+df3.format(avgVal)); 
				else if (avgCount==3) System.out.println(msg+",,"+df3.format(avgVal));
				else if (avgCount>=4) System.out.println(msg+","+df3.format(avgVal));
				
				
				for (int ii=2;ii<instancei.numAttributes();ii++) {
					fw.write(instancei.value(ii)+"");
					if (ii<instancei.numAttributes()-1) fw.write(",");					
				}
				fw.write("\r\n");
				fw.flush();
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//goes through CSV file with 2d descriptor values and reports 2d isomers:
		// it then outputs a new csvfile with the 2d isomers omitted and the avg 
		//toxicity used for the nonomitted compounds
		public static void FindDuplicates(String csvfilepath) {
			java.text.DecimalFormat df3=new java.text.DecimalFormat("0.000");
			try {
				CSVLoader atf = new CSVLoader();
				
				atf.setSource(new java.io.File(csvfilepath));
				Instances dataset=atf.getDataSet();
				
				System.out.println("dataset loaded");
				
				for (int i=0;i<dataset.numInstances();i++) {
					Instance instancei=dataset.instance(i);

					String msg=instancei.stringValue(0);
					double avgVal=instancei.value(1);
					int avgCount=1;
					
					for (int j=i+1;j<dataset.numInstances();j++) {
						Instance instancej=dataset.instance(j);
						
						if (IsMatch2d(instancei,instancej)) {
//							System.out.println("Match:"+instancei.stringValue(0)+"\t"+instancej.stringValue(0)+"\t"+i+"\t"+j);

							msg+=","+instancej.stringValue(0);
							avgVal+=instancej.value(1);
							avgCount++;
							dataset.delete(j);
							j--;
						}
					}
					
					avgVal/=(double)avgCount;
					
					if (avgCount==2) System.out.println(msg+",,,"+df3.format(avgVal)); 
					else if (avgCount==3) System.out.println(msg+",,"+df3.format(avgVal));
					else if (avgCount>=4) System.out.println(msg+","+df3.format(avgVal));
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		public static void FindMatchesBetweenTwoFiles(String csvfilepath1,String csvfilepath2) {
			java.text.DecimalFormat df3=new java.text.DecimalFormat("0.000");
			try {
				CSVLoader atf = new CSVLoader();
				
//				FileWriter fw=new FileWriter(newcsvfilepath);
				
				atf.setSource(new java.io.File(csvfilepath1));
				Instances dataset1=atf.getDataSet();
				
				atf.setSource(new java.io.File(csvfilepath2));
				Instances dataset2=atf.getDataSet();
				
				System.out.println(dataset1.numInstances());
				System.out.println(dataset2.numInstances());
				
//				System.out.println("dataset loaded");

//				for (int ii=0;ii<dataset.numAttributes();ii++) {
//					if (dataset.attribute(ii).name().indexOf(",")>-1) {
//						fw.write("\""+dataset.attribute(ii).name()+"\"");
//					} else {
//						fw.write(dataset.attribute(ii).name());
//					}
//					
//					if (ii<dataset.numAttributes()-1) fw.write(",");				
//				}
//				fw.write("\r\n");
//				fw.flush();
				
				System.out.println("ID1\tID2\tTox1\tTox2");
				
				for (int i=0;i<dataset1.numInstances();i++) {
					Instance instancei=dataset1.instance(i);

					String msg=instancei.stringValue(0);
					double avgVal=instancei.value(1);
					int avgCount=1;
					
					for (int j=0;j<dataset2.numInstances();j++) {
						Instance instancej=dataset2.instance(j);
						
						if (IsMatch2d(instancei,instancej)) {
							System.out.println(instancei.stringValue(0)+"\t"+instancej.stringValue(0)+"\t"+instancei.value(1)+"\t"+instancej.value(1));

							msg+=","+instancej.stringValue(0);
							avgVal+=instancej.value(1);
							avgCount++;
//							dataset2.delete(j);
//							j--;
						}
					} //end loop over file2 instances
					
					
//					if (avgCount>1) System.out.println("avgCount="+avgCount);
					
//					fw.write(instancei.stringValue(0)+",");
					
//					avgVal/=(double)avgCount;
					
//					fw.write(df3.format(avgVal)+",");
					
//					if (avgCount==2) System.out.println(msg+",,,"+df3.format(avgVal)); 
//					else if (avgCount==3) System.out.println(msg+",,"+df3.format(avgVal));
//					else if (avgCount>=4) System.out.println(msg+","+df3.format(avgVal));
					
					
//					for (int ii=2;ii<instancei.numAttributes();ii++) {
//						fw.write(instancei.value(ii)+"");
//						if (ii<instancei.numAttributes()-1) fw.write(",");					
//					}
//					fw.write("\r\n");
//					fw.flush();
				}
//				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	public static boolean IsMatch2d(Instance i1,Instance i2) {
		
		double tol=1e-2;
		
		for (int i=2;i<i1.numAttributes();i++) {
			
			double val1=i1.value(i);
			double val2=i2.value(i);
			
			
			double err=-1;
			
			if (val1==0) {
				err=val1-val2;
			} else {
				err=Math.abs(val1-val2)/val1*100;
			}
			
//			System.out.println(err);
			
//			if (i1.stringValue(0).equals("923-26-2") && i2.stringValue(0).equals("27813-02-1"))
//				System.out.println(i1.stringValue(0)+"\t"+i2.stringValue(0)+"\t"+err);
			
			if (err > tol) {			
				return false;
			}

		}
//		System.out.println("true");
		return true;
	}
	
	/**
	 * Compares two chemicals based on differences in the values of the Information
	 * Content Indices
	 * @param mol1
	 * @param mol2
	 * @return boolean
	 */
	public static boolean isIsomorph2ddescriptors(IAtomContainer mol1, IAtomContainer mol2) {
		return isIsomorph2ddescriptors(mol1, mol2,true);
	}//public static boolean isIsomorph2ddescriptors
	
	
	/**
	 * Compares 2 molecules in a stepwise fashion
	 * 
	 * First checks formula, then fingerprint, and finally 2d descriptors
	 * 
	 * If fingerprint doesn't match there is no need to check descriptors- which
	 * can take a while for structures with lots of rings
	 * 
	 * @param mol1
	 * @param mol2
	 * @return boolean
	 */
	public static boolean isIsomorphHybrid(IAtomContainer mol1, IAtomContainer mol2,boolean CheckFormula,boolean CheckFingerPrinter,boolean Check2dDescriptors) throws Exception {
		long t1=System.currentTimeMillis()/1000;
		
		DescriptorFactory df = new DescriptorFactory(false);
		df.Normalize(mol1);
		df.Normalize(mol2);

		if (CheckFormula) {
//			HydrogenAdder ha = new HydrogenAdder();//cdk hydrogen adder
//			ha.addExplicitHydrogensToSatisfyValency(mol1);
//			ha.addImplicitHydrogensToSatisfyValency(mol1);
//
//			ha.addExplicitHydrogensToSatisfyValency(mol2);
//			ha.addImplicitHydrogensToSatisfyValency(mol2);

//			MFAnalyser mf1 = new MFAnalyser(mol1);
//			String formula1 = mf1.getMolecularFormula();
//			MFAnalyser mf2 = new MFAnalyser(mol2);
//			String formula2 = mf2.getMolecularFormula();
			 
			MolecularFormula mf1=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol1);
			String formula1=MolecularFormulaManipulator.getString(mf1);

			MolecularFormula mf2=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol2);
			String formula2=MolecularFormulaManipulator.getString(mf2);
			 
			if (!formula1.equals(formula2)) {
				return false;
			}//if
		}
//		System.out.println(mol2.getProperty("CAS")+" formula OK");
		
		if (CheckFingerPrinter) {
			//remove hydrogens for both just in case:
			mol1=AtomContainerManipulator.removeHydrogens(mol1);
			mol2=AtomContainerManipulator.removeHydrogens(mol2);

			Fingerprinter fp = new Fingerprinter();
			
			boolean isSubset = false;
			boolean subset1 = false;

			BitSet superBS = fp.getFingerprint(mol1);
			BitSet subBS = fp.getFingerprint(mol2);
			isSubset = isSubset(superBS, subBS);
			subset1 = isSubset(subBS, superBS);

//			System.out.println(isSubset+"\t"+subset1);
			if (!isSubset || !subset1)
				return false;

		}
//		System.out.println(mol2.getProperty("CAS")+" fingerprinter OK");
		
		if (!Check2dDescriptors) {//If dont check descriptors you need to check rings
			AllRingsFinder arf = new AllRingsFinder();
			arf.setTimeout(60000);
			
			IRingSet rs = arf.findAllRings(mol1);
			IRingSet rs1 = arf.findAllRings(mol2);

			if (rs.getAtomContainerCount() != rs1.getAtomContainerCount())
				return false;

			RingSetManipulator.sort(rs);
			RingSetManipulator.sort(rs1);

			for (int i = 0; i < rs.getAtomContainerCount(); i++) {
				int size = rs.getAtomContainer(i).getAtomCount();
				int size1 = rs1.getAtomContainer(i).getAtomCount();
				if (size != size1)
					return false;
			}

		}
		

		if (Check2dDescriptors) {
			HashMap<String, Double> hm1 = calculate2Ddescriptors(mol1);
			Set<Entry<String, Double>> set1 = hm1.entrySet();
			HashMap<String, Double> hm2 = calculate2Ddescriptors(mol2);
			Set<Entry<String,Double>> set2 = hm2.entrySet();
			
//			System.out.println(hm1.size());

//			System.out.println("here:"+mol2.getProperty("CAS"));

			
			for (Entry<String,Double> me: set1) {
				String key = me.getKey();
				double val1 = me.getValue();
				double val2 = hm2.get(key);
				
				double tol = Math.abs(0.0001 * val1);
				
				if (tol<1e-8) tol=1e-8;
				
//				System.out.println(key+"\t"+val1+"\t"+val2+"\t"+Math.abs(val1 - val2));
				
				if (Math.abs(val1 - val2) > tol) {
//					System.out.println(key+"\t"+val1+"\t"+val2+"\t"+Math.abs(val1 - val2));
					return false;
				}
			}//for
		}
		long t2=System.currentTimeMillis()/1000;
		
//		System.out.println(t2-t1);
		
		return true;
	}
	
	/**
	 * Compares two chemicals based on differences in the values of the Information
	 * Content Indices
	 * @param mol1
	 * @param mol2
	 * @return boolean
	 */
	public static boolean isIsomorph2ddescriptors(IAtomContainer mol1, IAtomContainer mol2,boolean CheckFormula) {
		
		
//		HydrogenAdder ha = new HydrogenAdder();
		
		try {
			//First, check the molecular formula
			
			if (CheckFormula) {
//				ha.addExplicitHydrogensToSatisfyValency(mol1);
//				ha.addImplicitHydrogensToSatisfyValency(mol1);
//
//				ha.addExplicitHydrogensToSatisfyValency(mol2);
//				ha.addImplicitHydrogensToSatisfyValency(mol2);

				
				 MolecularFormula mf1=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol1);
				 String formula1=MolecularFormulaManipulator.getString(mf1);

				 MolecularFormula mf2=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol2);
				 String formula2=MolecularFormulaManipulator.getString(mf2);

				 
//				MFAnalyser mf1 = new MFAnalyser(mol1);
//				String formula1 = mf1.getMolecularFormula();
//				MFAnalyser mf2 = new MFAnalyser(mol2);
//				String formula2 = mf2.getMolecularFormula();
				
				if (!formula1.equals(formula2)) {
					return false;
				}//if

			}
			
//			System.out.println(formula1+"\t"+formula2);
			
			//Next, check the 2D descriptors
			
			HashMap<String, Double> hm1 = calculate2Ddescriptors(mol1);
			Set<Entry<String, Double>> set1 = hm1.entrySet();
			HashMap<String, Double> hm2 = calculate2Ddescriptors(mol2);
			Set<Entry<String,Double>> set2 = hm2.entrySet();
			
//			System.out.println(hm1.size());
			
			for (Entry<String,Double> me: set1) {
				String key = me.getKey();
				double val1 = me.getValue();
				double val2 = hm2.get(key);
				
				double tol = Math.abs(0.001 * val1);
				
//					System.out.println(key+"\t"+val1+"\t"+val2+"\t"+Math.abs(val1 - val2));
				
				if (Math.abs(val1 - val2) > tol) {
//						System.out.println(key+"\t"+val1+"\t"+val2+"\t"+Math.abs(val1 - val2));
					return false;
				}
			}//for
			return true;
			
		}//try
		
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return false;
	}//public static boolean isIsomorph2ddescriptors

	
	/**
	 * 
	 * systematic name
	 * Calculates the Information content indices for a particular chemical
	 * @param mol
	 * @return HashMap
	 */
	public static HashMap calculate2Ddescriptors(IAtomContainer mol) {
		HashMap <String, Double> hm = new HashMap<String, Double>();
		DescriptorFactory df = new DescriptorFactory(false);
//		AromaticityFixer af = new AromaticityFixer();
		DescriptorData dd = new DescriptorData();
		df.Calculate3DDescriptors=false;
		try {
//			mol = af.FixAromaticity(mol);
			df.CalculateDescriptors(mol, dd, true);
			String [] varlist = dd.varlist2d;
			int num = varlist.length;
			
			for (int i=0; i<num; i++) {
				Field field1 = dd.getClass().getField(varlist[i]);
				String [] names = (String []) field1.get(dd);
//				System.out.println(names.length);
				
				for (int j=0; j<names.length; j++) {
					Field field2 = dd.getClass().getField(names[j]);
					double val = field2.getDouble(dd);
					hm.put(names[j], val);
				}//for int j=0
			}//for int i=0
		}//try
		catch (Exception e) {
			System.out.println(mol.getProperty("CAS")+"\tAromaticityFixer error");
			e.printStackTrace();
		}//catch
		return hm;
	}//public static HashMap calculate2Ddescriptors

	/**
	 * This method goes through an sdf file and compares the sdf structure
	 * to the smiles string structure (also contained in the sdf file 
	 * as a property)
	 * 
	 * @param SDFfilepath
	 */
	public void compareSDFStructureToSmilesStructure(String SDFfilepath) {
		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			String CASfield="CAS";
			String smilesField="SMILES (ACD/ChemSketch)";
			
//			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));


			IteratingSDFReader mr = new IteratingSDFReader(new FileReader(SDFfilepath),DefaultChemObjectBuilder.getInstance());


			int counter = -1;

			
			while (mr.hasNext()) {
				
				counter++;
				
//				System.out.println(counter);
				
				AtomContainer m1=null;
				
				try {
					m1 = (AtomContainer) mr.next();
				
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
				
				if (m1==null || m1.getAtomCount()==0) break;
				
				String CAS=(String)m1.getProperty(CASfield);
				CAS=CAS.trim();
				
				if (!CAS.equals("41814-78-2")) continue;
//				if (!CAS.equals("50-29-3")) continue;
				
				m1.setProperty("Error", "");
				 
				 AtomContainerSet  moleculeSet2 = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m1);

				 
				 if (moleculeSet2.getAtomContainerCount() > 1) {
					m1.setProperty("Error","Multiple molecules");
				}
								
				 
				String smiles=(String)m1.getProperty(smilesField);
				
				AtomContainer m2=null;
				try {
					m2 = (AtomContainer) sp.parseSmiles(smiles); 
				} catch (Exception ex) {
					System.out.println(CAS+"\t"+smiles+"\tSmiles parser error");
				}

				if (m2!=null) {
					boolean isIsomorph=chemicalcompare.isIsomorph2ddescriptors(m1, m2,false);
//					boolean isIsomorph=chemicalcompare.isIsomorph(m1, m2);
//					boolean isIsomorph=chemicalcompare.isIsomorphUIT(m1, m2);
					
//					if (!isIsomorph)
						System.out.println(CAS+"\t"+smiles+"\t"+isIsomorph);
				}
			}// end while true;

			
			
			
		} catch (Exception e) {
			e.printStackTrace();

		}

		
	}
	
	
	/**
	 * Loops through structures in folder1 and compares to folder2 if they are available
	 * @param folderpath1
	 * @param folderpath2
	 */
	public void CompareStructuresInTwoFolders(String folderpath1,String folderpath2,String outputfoldername) {
		

		File Folder1=new File(folderpath1);
		File Folder2=new File(folderpath2);
		
		File [] files1=Folder1.listFiles();
		File [] files2=Folder2.listFiles();
		
		AllRingsFinder arf = new AllRingsFinder();
		arf.setTimeout(60000);
		
		
		try {
			
			File of=new File(folderpath1+"/"+outputfoldername);
			if (!of.exists()) of.mkdir();
			
			FileWriter fw=new FileWriter(folderpath1+"/"+outputfoldername+"/mismatches.txt");

		for (int i=0;i<files1.length;i++) {
			File file1=files1[i];
			String filename1=file1.getName();
			
			if (filename1.indexOf(".mol")==-1) continue;
			
			String CAS=filename1.substring(0,filename1.indexOf("."));
			
//			if (!CAS.equals("613-87-6")) continue;
			
			
			File file2=new File(folderpath2+"/"+filename1);
			
			if (!file2.exists()) {
				System.out.println(CAS+"\tmissing for folder 2");
				fw.write(CAS+"\tMissing in folder2\r\n"); fw.flush();
				continue;
			}
			
			IAtomContainer molecule1=ParseChemidplus.LoadChemicalFromMolFile(CAS, folderpath1);
			IAtomContainer molecule2=ParseChemidplus.LoadChemicalFromMolFile(CAS, folderpath2);
			
			try {
//				IRingSet rs1 = arf.findAllRings(molecule1);
//				IRingSet rs2 = arf.findAllRings(molecule2);
//				boolean Isomorph=isIsomorph(molecule1,molecule2,rs1,rs2);

				boolean Isomorph=isIsomorph2ddescriptors(molecule1,molecule2);
//				System.out.println(molecule1.getAtomCount()+"\t"+molecule2.getAtomCount());
				
				if (!Isomorph) {
					
					File file1a=new File(folderpath1+"/"+outputfoldername+"/"+CAS+"_1.mol");
					File file2a=new File(folderpath1+"/"+outputfoldername+"/"+CAS+"_2.mol");
					
					ToxPredictor.Utilities.Utilities.CopyFile(file1, file1a);
					ToxPredictor.Utilities.Utilities.CopyFile(file2, file2a);
					fw.write(CAS+"\tNot isomorph\r\n"); fw.flush();
				}
				
//				if (i%100==0) System.out.println(i);
				if (!Isomorph) System.out.println(CAS+"\t"+Isomorph);
			
			} catch (Exception e) {
				System.out.println("Error finding rings for "+CAS);
			}
		}//end i for loop
		
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void CreateWebPageToLookAtDiscrepanciesForStructuresInTwoFolders() {

		try {

			String discrepancyfilename="mismatches.txt";
			
//			String name1="Chemidplus3d";
//			String name2="Chemidplus2d";
//			String folder="ToxPredictor/structures/"+name1+"/mismatch 2d";

//			String name1="Chemidplus3d";
//			String name2="Chemidplus3d new";
//			String folder="ToxPredictor/structures/"+name1+"/mismatch 3d new";

			String name1="Chemidplus3d new";
			String name2="Chemidplus2d";
			String folder="ToxPredictor/structures/Chemidplus3d new/mismatch 2d";
			
			BufferedReader br = new BufferedReader(new FileReader(folder + "/"
					+ discrepancyfilename));


			File imagefolder = new File(folder+"/images");
			if (!imagefolder.exists())
				imagefolder.mkdir();

			FileWriter fw = new FileWriter(folder+ "/"
					+ "compare.html");

			fw.write("<html>\r\n");

			fw.write("<table border=1>\r\n");

			fw.write("<tr>\r\n");
			fw.write("<th>#</th>\r\n");
			fw.write("<th>CAS</th>\r\n");
			fw.write("<th>"+name1+"</th>\r\n");
			fw.write("<th>"+name2+"</th>\r\n");
			fw.write("</tr>\r\n");

			int counter=0;
			
			while (true) {
				String CAS = br.readLine();

				if (CAS == null)
					break;
				
				counter++;

				System.out.println(CAS);

				File molfile1 = new File(folder + "/" + CAS+"_1.mol");
				File molfile2 = new File(folder + "/" + CAS+"_2.mol");
				// System.out.println(oldmolfile.getName());

				IAtomContainer molecule1=ParseChemidplus.LoadChemicalFromMolFile(CAS+"_1", folder);
				IAtomContainer molecule2=ParseChemidplus.LoadChemicalFromMolFile(CAS+"_2", folder);

				File ImageFile1 = null;
				File ImageFile2 = null;

				int imageSize=400;
				
				if (molecule1 != null) {
					ImageFile1 = new File(imagefolder + "/" + CAS + "_1.png");

				//	if (!ImageFile1.exists()) {
						SaveStructureToFile.CreateImageFile(molecule1,CAS+"_1",imagefolder.getAbsolutePath());
					//}
					
//					String format="png";
//					FileInputStream fis=new FileInputStream(folder + "/" + CAS+"_1.mol");
//					FileOutputStream fos=new FileOutputStream(folder+"/images/"+CAS+"_1.png");
//					chemaxon.formats.MolConverter mc=new chemaxon.formats.MolConverter(fis,fos,format,true);
//					mc.convert();
				}

				if (molecule2 != null) {
					ImageFile2 = new File(imagefolder + "/" + CAS + "_2.png");

//					if (!ImageFile2.exists()) {
						SaveStructureToFile.CreateImageFile(molecule2,CAS+"_2",imagefolder.getAbsolutePath());					
	//				}
					
//					String format="png";
//					FileInputStream fis=new FileInputStream(folder + "/" + CAS+"_2.mol");
//					FileOutputStream fos=new FileOutputStream(folder+"/images/"+CAS+"_2.png");
//					chemaxon.formats.MolConverter mc=new chemaxon.formats.MolConverter(fis,fos,format,true);
//					mc.convert();

					
				}

				fw.write("<tr>\r\n");

				fw.write("\t<td>" + counter + "</td>\r\n");
				fw.write("\t<td>" + CAS + "</td>\r\n");

				fw.write("\t<td><img src=\"images/" + ImageFile1.getName()
						+ "\"></td>\n");

				fw.write("\t<td><img src=\"images/" + ImageFile2.getName()
						+ "\"></td>\n");

				fw.write("</tr>\r\n");
				fw.flush();
				// fw.write(molecule1.getAtomCount()+"\t"+molecule2.getAtomCount()+"\r\n");

			}

			fw.write("</table>\r\n");
			fw.write("</html>\r\n");

			br.close();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	void CompareDougsNewDatabaseToAccessVersion() {
		
		String f="ToxPredictor/chemidplus/compare/ValidatedStructuresII_DY";
		
//		ToxPredictor.misc.MolFileUtilities m=new ToxPredictor.misc.MolFileUtilities();
//		m.CreateMolFilesFromSDFFile2(f+"/ValidatedStructuresII_DY.sdf", f+"/new doug SDF", 0, "CAS", "");
//		m.AddHydrogensToMolFilesInFolder(new File(f+"/new doug SDF"),new File(f+"/new doug SDF"),0);
		
//		ToxPredictor.misc.CreateFilesForQSARAnalysis c=new ToxPredictor.misc.CreateFilesForQSARAnalysis();
//		c.CreateValidatedStructureSDFile(f, "validatedstructures-12-08-09.txt", f+"/from access db", "validatedstructures-12-08-09.sdf", true);
		
		String folderpath2=f+"/from access db";
		String folderpath1=f+"/new doug SDF";
		String outputfoldername="mismatch";
		CompareStructuresInTwoFolders(folderpath1, folderpath2,outputfoldername);

	}
	
	public static void main(String[] args) {
		chemicalcompare cc=new chemicalcompare();
		
//		cc.CompareDougsNewDatabaseToAccessVersion();
		
//		String sdffilepath="C:/Documents and Settings/tmarti02/My Documents/comptox/benfenati/BCF discrepancies/neutralized_1039.sdf";
//		String sdffilepath="C:/Documents and Settings/tmarti02/My Documents/comptox/benfenati/BCF discrepancies/neutralized_1039_dearomatize.sdf";		
//		cc.compareSDFStructureToSmilesStructure(sdffilepath);
		
//		cc.lookatdescriptorsinMyToxicityFolder("ALOGP","C:/Documents and Settings/tmarti02/My Documents/MyToxicity_SDF");
//		cc.lookatdescriptorsinMyToxicityFolder("ALOGP","C:/Documents and Settings/tmarti02/My Documents/MyToxicity_SMI");
		
//		String f="C:/Documents and Settings/tmarti02/My Documents";
//		String folder1=f+"/MyToxicity_BenfenatiSDF";
//		String folder2=f+"/MyToxicity_BenfenatiSmiles";
//		cc.compareStructuresInMyToxicityFolders(folder1, folder2);
		
//		String f="C:/Documents and Settings/tmarti02/My Documents/comptox/0 Physical Properties/ST";
//		String folderpath1=f+"/Jaspar Name Structures-2";
//		String folderpath2=f+"/Chemidplus Name Structures-2";
//		
////		String folderpath1=f+"/Overall Best Name Structures";
////		String folderpath2=f+"/Jaspar Name Structures";
//		
//		cc.CompareStructuresInTwoFolders(folderpath1, folderpath2, "mismatches");
//
//		
//		if (true) return;
		
//		String folderpath1="ToxPredictor/structures/Chemidplus3d";
//		String folderpath2="ToxPredictor/structures/Chemidplus3d new";
//		String outputfoldername="mismatch 3d new";
//		cc.CompareStructuresInTwoFolders(folderpath1, folderpath2,outputfoldername);
		
//		String folderpath1="ToxPredictor/structures/Chemidplus3d";
//		String folderpath2="ToxPredictor/structures/Chemidplus2d";
//		String outputfoldername="mismatch 2d";
//		cc.CompareStructuresInTwoFolders(folderpath1, folderpath2,outputfoldername);
		
//		String folderpath1="ToxPredictor/structures/Chemidplus3d new";
//		String folderpath2="ToxPredictor/structures/Chemidplus2d";
//		String outputfoldername="mismatch 2d";
//		cc.CompareStructuresInTwoFolders(folderpath1, folderpath2,outputfoldername);

//		cc.CreateWebPageToLookAtDiscrepanciesForStructuresInTwoFolders();

		///////////////////////////////////////////////////////////////
		
////		String endpoint="LD50";
////		String desc="_Final1";
//
//		String endpoint="LC50";
//		String desc="_Final5";
//
//		String endpoint="viscosity";
//		String desc="_Final6";

//		String endpoint="BGVV";
//		String endpoint="LLNA";
//		String endpoint="Skin_Irritation";
//		String endpoint="Skin_Corrosion";
//		String endpoint="ParisIII_liquids2";
//		String endpoint="LogKow";
//		String endpoint="GPMT";
//		String desc="_WvsS";
//		String desc="_2";
//		String desc="";

		
//		String endpoint="BCF";
//		String desc="_Final8";
//		String f1="ToxPredictor/DescriptorTextTables";
//		String folder=f1+"/"+endpoint+desc+" Data Files/2d";
//		String filename=endpoint+"_overall_set-2d.csv";
//		String filename2=endpoint+"_overall_set-2d_no_2d_isomers.csv";
//		chemicalcompare.FindDuplicates(folder+"/"+filename,folder+"/"+filename2);
//		if (true) return;
		

		
//		String endpoint="ER_CERAPP_Binding_Class";
//		String desc="";
//		String f1="ToxPredictor/DescriptorTextTablesToxCast";
//		String folder=f1+"/"+endpoint+desc+" Data Files/2d";
//		String filename=endpoint+"_overall_set-2d.csv";
//		String csvfilepath1=folder+"/"+filename;
//		chemicalcompare.FindDuplicates(csvfilepath1);
		
//		String endpoint="ER_CERAPP_Binding_Class";
//		String desc="";
//		String f1="ToxPredictor/DescriptorTextTablesToxCast";
//		String folder=f1+"/"+endpoint+desc+" Data Files/2d";
//		String filename=endpoint+"_training_set-2d.csv";
//		String csvfilepath1=folder+"/"+filename;
//		chemicalcompare.FindDuplicates(csvfilepath1);


// *********************************************************************************************************
//		String endpoint="ER_CERAPP_Binding_Class_Eval_Exp";
//		String endpoint="ER_CERAPP_Agonist_Class_Eval_Exp";
//		String endpoint="ER_CERAPP_Antagonist_Class_Eval_Exp";
//		
//		String desc="";
//		String f1="ToxPredictor/DescriptorTextTablesToxCast";
//		String folder=f1+"/"+endpoint+desc+" Data Files/2d";
//		String filename=endpoint+"_prediction_set-2d.csv";
//		String csvfilepath1=folder+"/"+filename;
////		chemicalcompare.FindDuplicates(csvfilepath1);
//
//		
//		String endpoint2="InvivoMouse";
//		String folder2=f1+"/"+endpoint2+desc+" Data Files/2d";
////		String filename2=endpoint2+"_overall_set-2d.csv";
//		String filename2=endpoint2+"_overall_set-2d no duplicates.csv";
//		
//		String csvfilepath2=folder2+"/"+filename2;
////		chemicalcompare.FindDuplicates(csvfilepath2);
//		
//		chemicalcompare.FindMatchesBetweenTwoFiles(csvfilepath1, csvfilepath2);
// ********************************************************************************************************		
		String endpoint="InVivoMouse";
		String desc="_rnd";
		String f1="ToxPredictor/DescriptorTextTablesToxCast2";
		String folder=f1+"/"+endpoint+desc+" Data Files/2d";
		String filename=endpoint+"_overall_set-2d.csv";
		String csvfilepath1=folder+"/"+filename;
//		chemicalcompare.FindDuplicates(csvfilepath1);

		
		String endpoint2=endpoint;
		String folder2=f1+"/"+endpoint2+desc+" Data Files/2d";
//		String filename2=endpoint2+"_overall_set-2d.csv";
		String filename2=endpoint2+"_Prediction_browne_set-2d.csv";
		
		String csvfilepath2=folder2+"/"+filename2;
//		chemicalcompare.FindDuplicates(csvfilepath2);
		
		chemicalcompare.FindMatchesBetweenTwoFiles(csvfilepath1, csvfilepath2);
		// ********************************************************************************************************		

		
		
		
//		String folder1="ToxPredictor/DescriptorTextTablesToxCast/InvivoMouse_posFrac=0.25 data files/2d";
//		String csvfilepath1=folder1+"/InvivoMouse_training_set-2d.csv";
//		String csvfilepath2=folder1+"/InvivoMouse_prediction_set-2d.csv";

//		String folder1="ToxPredictor/DescriptorTextTablesToxCast/InvivoMouse_posFrac=0.25 data files/2d";
//		String csvfilepath1=folder1+"/InvivoMouse_prediction_set-2d.csv";
//		String folder2="ToxPredictor/DescriptorTextTablesToxCast/Binding Data Files/2d";
//		String csvfilepath2=folder2+"/Binding_training_set-2d.csv";
//		
//		chemicalcompare.FindMatchesBetweenTwoFiles(csvfilepath1, csvfilepath2);

		
		if (true) return;
		
		
		

//		 // *****************************************************
		
//		String endpoint="ER_CERAPP_Binding_Class";
//		String desc="";
//		String f1="ToxPredictor/DescriptorTextTablesToxCast";
//		String folder=f1+"/"+endpoint+desc+" Data Files/2d";
//		String filename=endpoint+"_overall_set-2d.csv";
//		String filename2=endpoint+"_overall_set-2d_no_2d_isomers.csv";
//		chemicalcompare.FindDuplicates(folder+"/"+filename,folder+"/"+filename2);
//		if (true) return;

//				
//		 // *****************************************************
//		
//		
//		
//		  SmilesParser sp = new SmilesParser();
//		 
//		  try {
//		 IMolecule m1 = sp.parseSmiles("CC(C)C"); // 
//		 IMolecule m2 = sp.parseSmiles("C1CC1"); //
//		 	System.out.println(chemicalcompare.isIsomorphUIT(m1, m2));
//		 	System.out.println(chemicalcompare.isIsomorph(m1, m2));
//		 	System.out.println(chemicalcompare.isIsomorph2ddescriptors(m1, m2));
//		  } catch (Exception e) {
//			  e.printStackTrace();
//		  }
//		 if (true) return;
//		 
//		 // *****************************************************
//		
//		
//		ParseChemidplus p=new ParseChemidplus();
//		String cas="71-43-2";
//		String f4="ToxPredictor/DescriptorTextTables/Cancer data files/mol files v4a";
//		String f5="ToxPredictor/DescriptorTextTables/Cancer data files/mol files v5a";
//		IMolecule mol4= p.LoadChemicalFromMolFile(cas,f4);
//		IMolecule mol5= p.LoadChemicalFromMolFile(cas,f5);
//		System.out.println(chemicalcompare.isIsomorph(mol4, mol5));
//		

		//*****************************************************************
		ParseChemidplus p=new ParseChemidplus();
		
		String cas="23404-73-1";
		
////		String cas="13366-73-9";//isomorphs (DSSTox and Other)
////		String cas="38588-65-7";//are isomorph (Chemidplus and Other)
////		String cas="14863-40-2"; //not isomorph (Chemidplus and Other)-tautomer
////		String cas="15383-20-7"; //not isomorph (Chemidplus and Other)
//		String cas="28057-48-9";
		
//		String f1="ToxPredictor/structures/IndexNet2d";
////		String f1="ToxPredictor/structures/Smiles_Marvin";
////		String f1="ToxPredictor/structures/Name";
//
//		
////		String f1="ToxPredictor/structures/Chemidplus3d";
//		String f2="ToxPredictor/structures/Other";
//		IMolecule mol1= p.LoadChemicalFromMolFile(cas,f1);
//		IMolecule mol2= p.LoadChemicalFromMolFile(cas,f2);
//		
//		ToxPredictor.Utilities.CDKUtilities.RemoveHydrogens(mol1);
//		ToxPredictor.Utilities.CDKUtilities.RemoveHydrogens(mol2);
//
//		System.out.println("mol1 atom count ="+mol1.getAtomCount());
//		System.out.println("mol2 atom count ="+mol2.getAtomCount());
		
		try {
//		System.out.println(chemicalcompare.isIsomorphUIT(mol1, mol2));
//		System.out.println(chemicalcompare.isIsomorph2ddescriptors(mol1, mol2));
//		System.out.println(chemicalcompare.isIsomorphHybrid(mol1, mol2, true,true,true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//*****************************************************************

		
//		
//		if (true) return;
//		String DougMolFolder="N:/NRMRL-PRIV/CompTox/3Dcoordinates/mol";
//		String NIHMolFolder="N:/NRMRL-PRIV/CompTox/3Dcoordinates/database/coords";
//		
//		String nistfolder="ToxPredictor/chemidplus/structures/INDEXNET2d_withH";
//		
//		
//		String CAS="132-16-1"+"_IndexNet2d";
////		String CAS="100-00-5"+"_IndexNet2d";
//		
//		
////		IMolecule mol =p.LoadChemicalFromMolFile(CAS,DougMolFolder);
//		
////		IMolecule mol1 = p.LoadChemicalFromMolFile(CAS,NIHMolFolder);
//		
//		
//		IMolecule mol1= p.LoadChemicalFromMolFile(CAS,nistfolder);
//		IMolecule mol2= p.LoadChemicalFromMolFile(CAS,nistfolder);
//		
////		IMolecule mol1=p.LoadChemicalFromMolFileInJar(CAS+"_IndexNet2d.mol");
////		IMolecule mol1=p.LoadChemicalFromMolFileInJar("NIST2d/"+CAS+"_NIST2d.mol");//this file has N(=O)(=O) version of nitro
//		
//		IRingSet rs1=null;
//		IRingSet rs2=null;
//		
//		
//		AllRingsFinder arf = new AllRingsFinder();
//		arf.setTimeout(60000);
//		
//		
//		try {
//			rs1 = arf.findAllRings(mol1);
//			rs2 = arf.findAllRings(mol2);
//		
//			boolean Match=chemicalcompare.isIsomorph(mol1,mol2,rs1,rs2);	
//			System.out.println(Match);	
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//
//		System.out.println("hi");
		
	} // main
} // public class chemicalcompare
