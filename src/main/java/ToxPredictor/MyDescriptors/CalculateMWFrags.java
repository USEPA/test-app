package ToxPredictor.MyDescriptors;


import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
//import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.smiles.SmilesParser;

import wekalite.CSVLoader;
import wekalite.Instance;
import wekalite.Instances;


/**
 * 
 * This class calculates MW of frags
 * 
 * 
 */

public class CalculateMWFrags {

	
	TreeMap<String,Double> htFragMW=new TreeMap<String,Double>();//store MW of each fragment
	
	AtomicProperties ap;
	boolean Debug=true;
	
	UniversalIsomorphismTester uit=new UniversalIsomorphismTester();

	public CalculateMWFrags() {
		try {
			ap=AtomicProperties.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static double calculateMW_Frag(DescriptorData dd) {
		
		TreeMap<String,Double>map=getMap();
		
	    Vector v=new Vector(dd.FragmentList.keySet());
	    Collections.sort(v);
	    
	    double MW_Frag=0;

	    for ( Enumeration e = v.elements(); e.hasMoreElements();) {
			// 	retrieve the object_key
			String fragName = (String) e.nextElement();
			// 	retrieve the object associated with the key
			double fragCount = (Double) dd.FragmentList.get ( fragName );
			
			if (map.get(fragName)==null) {
				System.out.println(fragName+"\tmissing");
				continue;
			}

//			if(fragCount>0) System.out.println(fragName+"\t"+fragCount+"\t"+map.get(fragName));
			
			//Hack to fix halogens:
			
			
			if (fragName.equals("Halogen [Nitrogen attach]") && fragCount>0) {
				
				double sum=dd.nF+dd.nCL+dd.nBR+dd.nI;
				
				if (fragCount==sum) {
					MW_Frag+=dd.nF*map.get("-F [aliphatic attach]");
					MW_Frag+=dd.nCL*map.get("-Cl [aliphatic attach]");
					MW_Frag+=dd.nBR*map.get("-Br [aliphatic attach]");
					MW_Frag+=dd.nI*map.get("-I [aliphatic attach]");
				} else {
					int count=0;
					if (dd.nF>0) count++;
					if (dd.nCL>0) count++;
					if (dd.nBR>0) count++;
					if (dd.nI>0) count++;

					if (count>1) return 0;//no way to assign MW of this frag just from instance info
					
					if (dd.nF>0) {
						MW_Frag+=fragCount*map.get("-F [aliphatic attach]");
					} else if (dd.nCL>0) {
						MW_Frag+=fragCount*map.get("-Cl [aliphatic attach]");
					} else if (dd.nBR>0) {
						MW_Frag+=fragCount*map.get("-Br [aliphatic attach]");
					} else if (dd.nI>0) {
						MW_Frag+=fragCount*map.get("-I [aliphatic attach]");
					} 
				}
			} else {
				MW_Frag+=fragCount*map.get(fragName);	
			}
			
			
	    }
		return MW_Frag;
	}

	public static TreeMap<String,Double>getMap() {
		
		TreeMap<String,Double>map=new TreeMap<String,Double>();
		
		map.put("#C [aliphatic attach]", 12.01);
		map.put("#CH [aliphatic attach]", 13.02);
		map.put("->S<-", 32.07);
		map.put("-Br [aliphatic attach]", 79.90);
		map.put("-Br [aromatic attach]", 79.90);
		map.put("-Br [olefinic attach]", 79.90);
		map.put("-C# [aromatic attach]", 12.01);
		map.put("-C#N [aliphatic attach]", 26.02);
		map.put("-C#N [aliphatic nitrogen attach]", 26.02);
		map.put("-C#N [aliphatic sulfur attach]", 26.02);
		map.put("-C#N [aromatic attach]", 26.02);
		map.put("-C#N [olefinic attach]", 26.02);
		map.put("-C(=O)- [2 aromatic attach]", 28.01);
		map.put("-C(=O)- [2 nitrogen attach]", 28.01);
		map.put("-C(=O)- [aromatic attach]", 28.01);
		map.put("-C(=O)- [halogen attach]", 28.01);
		map.put("-C(=O)- [nitrogen, aliphatic attach]", 28.01);
		map.put("-C(=O)- [nitrogen, aromatic attach]", 28.01);
		map.put("-C(=O)- [olefinic attach]", 28.01);
		map.put("-C(=O)- [phosphorus attach]", 28.01);
		map.put("-C(=O)O- [aliphatic attach]", 44.01);
		map.put("-C(=O)O- [aromatic attach]", 44.01);
		map.put("-C(=O)O- [cyclic]", 44.01);
		map.put("-C(=O)O- [nitrogen attach]", 44.01);
		map.put("-C(=O)O- [olefinic attach]", 44.01);
		map.put("-C(=O)OC(=O)-", 72.02);
		map.put("-C(=O)S- [aliphatic attach]", 60.08);
		map.put("-C(=O)S- [aromatic attach]", 60.08);
		map.put("-C(=O)S- [nitrogen attach]", 60.08);
		map.put("-C(=O)SH [aliphatic attach]", 61.09);
		map.put("-C(=O)SH [aromatic attach]", 61.09);
		map.put("-C(=O)SH [nitrogen attach]", 61.09);
		map.put("-C(=O)[O-]", 44.01);
		map.put("-C(=S)- [2 nitrogen attach]", 44.08);
		map.put("-C(=S)- [aliphatic attach]", 44.08);
		map.put("-C(=S)- [aromatic attach]", 44.08);
		map.put("-C(=S)- [nitrogen, aliphatic attach]", 44.08);
		map.put("-C(=S)- [nitrogen, aromatic attach]", 44.08);
		map.put("-C(=S)O- [aliphatic attach]", 60.08);
		map.put("-C(=S)O- [aromatic attach]", 60.08);
		map.put("-C(=S)O- [nitrogen attach]", 60.08);
		map.put("-C(=S)OH [aliphatic attach]", 61.09);
		map.put("-C(=S)OH [aromatic attach]", 61.09);
		map.put("-C(=S)S- [aliphatic attach]", 76.15);
		map.put("-C(=S)S- [aromatic attach]", 76.15);
		map.put("-C(=S)S- [nitrogen attach]", 76.15);
		map.put("-C(=S)SH [aliphatic attach]", 77.16);
		map.put("-C(=S)SH [aromatic attach]", 77.16);
		map.put("-C(=S)SH [nitrogen attach]", 77.16);
		map.put("-C([H])=N [Nitrogen attach]", 27.03);
		map.put("-C([H])=N[H] [Nitrogen attach]", 28.04);
		map.put("-CCl3 [aliphatic attach]", 118.36);
		map.put("-CCl3 [aromatic attach]", 118.36);
		map.put("-CF3 [aliphatic attach]", 69.01);
		map.put("-CF3 [aromatic attach]", 69.01);
		map.put("-CH2- [aliphatic attach]", 14.03);
		map.put("-CH2- [aromatic attach]", 14.03);
		map.put("-CH3 [aliphatic attach]", 15.04);
		map.put("-CH3 [aromatic attach]", 15.04);
		map.put("-CH< [aliphatic attach]", 13.02);
		map.put("-CH< [aromatic attach]", 13.02);
		map.put("-CH= [aromatic attach]", 13.02);
		map.put("-CH=N", 27.03);
		map.put("-CH=NH", 28.04);
		map.put("-CH=NO", 43.03);
		map.put("-CH=NOH", 44.04);
		map.put("-CHO [aliphatic attach]", 29.02);
		map.put("-CHO [aromatic attach]", 29.02);
		map.put("-CHO [nitrogen attach]", 29.02);
		map.put("-COOH [aliphatic attach]", 45.02);
		map.put("-COOH [aromatic attach]", 45.02);
		map.put("-Cl [aliphatic attach]", 35.45);
		map.put("-Cl [aromatic attach]", 35.45);
		map.put("-Cl [olefinic attach]", 35.45);
		map.put("-F [aliphatic attach]", 19.00);
		map.put("-F [aromatic attach]", 19.00);
		map.put("-F [olefinic attach]", 19.00);
		map.put("-Hg", 200.59);
		map.put("-Hg+", 200.59);
		map.put("-Hg-", 200.59);
		map.put("-I [aliphatic attach]", 126.90);
		map.put("-I [aromatic attach]", 126.90);
		map.put("-I [olefinic attach]", 126.90);
		map.put("-N(=O)", 30.01);
		map.put("-N< [3 membered ring]", 14.01);
		map.put("-N< [aliphatic attach]", 14.01);
		map.put("-N< [aromatic attach]", 14.01);
		map.put("-N< [attached to P]", 14.01);
		map.put("-N< [nitrogen attach]", 14.01);
		map.put("-N=C=O [aliphatic attach]", 42.02);
		map.put("-N=C=O [aromatic attach]", 42.02);
		map.put("-N=C=S", 58.09);
		map.put("-N=N-", 28.02);
		map.put("-N=NH", 29.03);
		map.put("-N=O [aromatic attach]", 30.01);
		map.put("-N=S=O", 62.08);
		map.put("-N=[N+]=[N-]", 42.03);
		map.put("-N=[N+][O-]", 44.02);
		map.put("-NH- [3 membered ring]", 15.02);
		map.put("-NH- [aliphatic attach]", 15.02);
		map.put("-NH- [aromatic attach]", 15.02);
		map.put("-NH- [attached to P]", 15.02);
		map.put("-NH- [nitrogen attach]", 15.02);
		map.put("-NH2 [aliphatic attach]", 16.03);
		map.put("-NH2 [aromatic attach]", 16.03);
		map.put("-NH2 [attached to P]", 16.03);
		map.put("-NH2 [nitrogen attach]", 16.03);
		map.put("-NHN=O", 45.03);
		map.put("-NO2 [aliphatic attach]", 46.01);
		map.put("-NO2 [aromatic attach]", 46.01);
		map.put("-NO2 [nitrogen attach]", 46.01);
		map.put("-NO2 [olefinic attach]", 46.01);
		map.put("-O- (epoxide)", 16.00);
		map.put("-O- [2 aromatic attach]", 16.00);
		map.put("-O- [2 phosphorus attach]", 16.00);
		map.put("-O- [aliphatic attach]", 16.00);
		map.put("-O- [aromatic attach]", 16.00);
		map.put("-O- [arsenic attach]", 16.00);
		map.put("-O- [nitrogen attach]", 16.00);
		map.put("-O- [oxygen attach]", 16.00);
		map.put("-O- [phosphorus attach]", 16.00);
		map.put("-O- [phosphorus, aromatic attach]", 16.00);
		map.put("-O- [sulfur attach]", 16.00);
		map.put("-OC(=O)O-", 60.01);
		map.put("-OC(=S)O-", 76.08);
		map.put("-OH [aliphatic attach]", 17.01);
		map.put("-OH [aromatic attach]", 17.01);
		map.put("-OH [arsenic attach]", 17.01);
		map.put("-OH [nitrogen attach]", 17.01);
		map.put("-OH [oxygen attach]", 17.01);
		map.put("-OH [phosphorus attach]", 17.01);
		map.put("-OH [sulfur attach]", 17.01);
		map.put("-ONO2", 62.01);
		map.put("-S(=O)(=O)- [2 nitrogen attach]", 64.07);
		map.put("-S(=O)(=O)- [aliphatic attach]", 64.07);
		map.put("-S(=O)(=O)- [aromatic attach]", 64.07);
		map.put("-S(=O)(=O)- [nitrogen, aliphatic attach]", 64.07);
		map.put("-S(=O)(=O)- [nitrogen, aromatic attach]", 64.07);
		map.put("-S(=O)(=O)- [olefinic attach]", 64.07);
		map.put("-S(=O)- [2 nitrogen attach]", 48.07);
		map.put("-S(=O)- [aliphatic attach]", 48.07);
		map.put("-S(=O)- [aromatic attach]", 48.07);
		map.put("-S(=O)- [nitrogen, aliphatic attach]", 48.07);
		map.put("-S(=O)- [nitrogen, aromatic attach]", 48.07);
		map.put("-S(=O)- olefinic attach]", 48.07);
		map.put("-S- [2 aromatic attach]", 32.07);
		map.put("-S- [aliphatic attach]", 32.07);
		map.put("-S- [aromatic attach]", 32.07);
		map.put("-S- [arsenic attach]", 32.07);
		map.put("-S- [nitrogen attach]", 32.07);
		map.put("-S- [phosphorus attach]", 32.07);
		map.put("-S- [sulfur attach]", 32.07);
		map.put("-S- [three membered ring]", 32.07);
		map.put("-SC(=O)O-", 76.08);
		map.put("-SC(=O)S-", 92.15);
		map.put("-SC(=S)O-", 92.15);
		map.put("-SC(=S)S-", 108.22);
		map.put("-SH [aliphatic attach]", 33.08);
		map.put("-SH [aromatic attach]", 33.08);
		map.put("-[N+]#N", 28.02);
		map.put("-[O-]", 16.00);
		map.put("=C [aliphatic attach]", 12.01);
		map.put("=C=", 12.01);
		map.put("=CH [aliphatic attach]", 13.02);
		map.put("=CH2 [aliphatic attach]", 14.03);
		map.put("=O [other]", 16.00);
		map.put("=S [other]", 32.07);
		map.put(">C(=N) [2 Nitrogen attach]", 26.02);
		map.put(">C(=N) [Nitrogen attach]", 26.02);
		map.put(">C< [aliphatic attach]", 12.01);
		map.put(">C< [aromatic attach]", 12.01);
		map.put(">C= [aromatic attach]", 12.01);
		map.put(">C=N", 26.02);
		map.put(">C=NH", 27.03);
		map.put(">C=NO", 42.02);
		map.put(">C=NOH", 43.03);
		map.put(">C=N[H] [2 Nitrogen attach]", 27.03);
		map.put(">C=N[H] [Nitrogen attach]", 27.03);
		map.put(">NN=O", 44.02);
		map.put("AC", 12.01);
		map.put("ACAC", 24.02);
		map.put("ACH", 13.02);
		map.put("AN", 14.01);
		map.put("AN [attached to AN in same ring]", 14.01);
		map.put("AN+", 14.01);
		map.put("ANH", 15.02);
		map.put("ANH [attached to AN in same ring]", 15.02);
		map.put("AO", 16.00);
		map.put("AS", 32.07);
		map.put("A[N+][O-]", 30.01);
		map.put("As", 74.92);
		map.put("As [+3 valence, all single bonds]", 74.92);
		map.put("As [+3 valence, one double bond]", 74.92);
		map.put("As [+5 valence, all single bonds]", 74.92);
		map.put("As [+5 valence, one double bond]", 74.92);
		map.put("As [+5 valence, two double bonds]", 74.92);
		map.put("As(=O)", 90.92);
		map.put("B", 10.81);
		map.put("C=C=O", 40.02);
		map.put("C=O(ketone, aliphatic attach)", 28.01);
		map.put("C=O(non-ketone, aliphatic attach)", 28.01);
		map.put("C=[N+]=[N-]", 40.03);
		map.put("C=[N+][O-]", 42.02);
		map.put("CH2=C(CH3)C(=O)O-", 85.09);
		map.put("CH2=CHC(=O)O-", 71.06);
		map.put("CH2=N", 28.04);
		map.put("Fused aromatic carbon", 12.01);
		map.put("Fused aromatic nitrogen", 14.01);
		map.put("H [carbon attach]", 1.01);
		map.put("H [nitrogen attach]", 1.01);
		map.put("H [other]", 1.01);
		map.put("H [phosphorus attach]", 1.01);
		map.put("H [silicon attach]", 1.01);
		map.put("HC(=O)O-", 45.02);
		map.put("HN=C=N", 41.04);
		map.put("Halogen [Nitrogen attach]", 126.90);
		map.put("Hg", 200.59);
		map.put("N+ [four single bonds]", 14.01);
		map.put("N=C=N", 40.03);
		map.put("P", 30.97);
		map.put("P [+3 valence, all single bonds]", 30.97);
		map.put("P [+5 valence, all single bonds]", 30.97);
		map.put("P [+5 valence, one double bond]", 30.97);
		map.put("P [+5 valence, two double bonds]", 30.97);
		map.put("P(=O)", 46.97);
		map.put("P=N", 44.98);
		map.put("P=NH", 45.99);
		map.put("P=S", 63.04);
		map.put("Pb", 207.20);
		map.put("S=C=S", 76.15);
		map.put("Si", 28.09);
		map.put("Si [aromatic attach]", 28.09);
		map.put("Si [oxygen attach]", 28.09);
		map.put("Si [oxygen, aromatic attach]", 28.09);
		map.put("Sn", 118.71);
		map.put("Sn [aromatic attach]", 118.71);
		map.put("Sn [oxygen attach]", 118.71);
		map.put("Sn=O", 134.71);
		map.put("[C-]#[N+]", 26.02);


		
		return map;
		
	}
	
	public void testDescriptorFile() {
		
		String endpoint="LD50";
//		String endpoint="Mutagenicity";
		String filePath="L:/Priv/Cin/NRMRL/CompTox/javax/web-test/data/"+endpoint+"/"+endpoint+"_training_set-2d.csv";
		
		TreeMap<String,Double>map=getMap();
		
		try {
			CSVLoader c=new CSVLoader();

			Instances instances=c.getDataSetFromFile(filePath);
			
			
			for (int i=0;i<instances.numInstances();i++) {
				Instance instance=instances.instance(i);
			
				double MW_Frag = calculateMW(map, instance);
		
				double MW=instance.value("MW");
				double err=Math.abs(MW-MW_Frag);
				
				if (err>0.1)
					System.out.println(instance.getName()+"\t"+MW+"\t"+MW_Frag+"\t"+err);
				
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

	private double calculateMW(TreeMap<String, Double> map, Instance instance) {
		double MW_Frag=0;

		String startDescriptor="As [+5 valence, one double bond]";
		int colStart=instance.getAttributeNumber(startDescriptor);

		for (int j=colStart;j<instance.numAttributes();j++) {

			double fragCount=instance.value(j);
			String fragName=instance.attribute(j);

			//Hack to fix MW halogen frag:
			if (fragName.equals("Halogen [Nitrogen attach]") && fragCount>0) {

				double sum=instance.value("nF")+instance.value("nCL")+instance.value("nBR")+instance.value("nI");

				if (fragCount==sum) {
					MW_Frag+=instance.value("nF")*map.get("-F [aliphatic attach]");
					MW_Frag+=instance.value("nCL")*map.get("-Cl [aliphatic attach]");
					MW_Frag+=instance.value("nBR")*map.get("-Br [aliphatic attach]");
					MW_Frag+=instance.value("nI")*map.get("-I [aliphatic attach]");
				} else {
					int count=0;
					if (instance.value("nF")>0) count++;
					if (instance.value("nCL")>0) count++;
					if (instance.value("nBR")>0) count++;
					if (instance.value("nI")>0) count++;

					if (count>1) return 0;//no way to assign MW of this frag just from instance info

					if (instance.value("nF")>0) {
						MW_Frag+=fragCount*map.get("-F [aliphatic attach]");
					} else if (instance.value("nCL")>0) {
						MW_Frag+=fragCount*map.get("-Cl [aliphatic attach]");
					} else if (instance.value("nBR")>0) {
						MW_Frag+=fragCount*map.get("-Br [aliphatic attach]");
					} else if (instance.value("nI")>0) {
						MW_Frag+=fragCount*map.get("-I [aliphatic attach]");
					} 
				}

			} else {
				MW_Frag+=fragCount*map.get(fragName);

				//				if (instance.getName().equals("51528-03-1")) {
				//					if (fragCount>0)
				//						System.out.println(fragName+"\t"+fragCount+"\t"+map.get(fragName));
			}

		}

		return MW_Frag;
	}
	
	
	private double  Calculate_mw(AtomContainer m) {
		// tried to use CDK built in methods but they suck
		// alternative method would be to use m2 which includes the hydrogens
		
		try {
			ap=AtomicProperties.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double MW=0;
				
		for (int i=0;i<=m.getAtomCount()-1;i++) {			
			IAtom a=m.getAtom(i);
			MW+=ap.GetMass(a.getSymbol());
		}
		return MW;
			
	}
	
	private void FindFrags() {
		
		this.getMW("ACAC", "CC");
		this.getMW("AN [attached to AN in same ring]", "N");
		this.getMW("ANH [attached to AN in same ring]", "N[H]");

		this.getMW("->S<-","S");
		this.getMW("[C-]#[N+]","[C-]#[N+]");
		
		this.getMW("S=C=S","S=C=S");
		this.getMW("HN=C=N","[H]N=C=N");
		this.getMW("N=C=N","N=C=N");


		this.getMW("C=C=O","C=C=O");
		this.getMW("C=[N+][O-]","C=[N+][O-]");
		this.getMW("C=[N+]=[N-]","C=[N+]=[N-]");


		this.getMW("-N=S=O","N=S=O");
		this.getMW("-N=[N+][O-]","N=[N+][O-]");

		this.getMW("-N=[N+]=[N-]","N=[N+]=[N-]");
		this.getMW("-[N+]#N","[N+]#N");
		this.getMW("A[N+][O-]","[N+][O-]");
		this.getMW("N+ [four single bonds]","[N+]");

		this.getMW("CH2=C(CH3)C(=O)O-","C([H])([H])=C(C([H])([H])([H]))C(=O)O");
		this.getMW("CH2=CHC(=O)O-","C([H])([H])=C([H])C(=O)O");
		this.getMW("-N=C=S","N=C=S");

		this.getMW("-SC(=S)S-","SC(=S)S");
		this.getMW("-SC(=S)O-","SC(=S)O");
		this.getMW("-SC(=O)S-","SC(=O)S");
		this.getMW("-SC(=O)O-","SC(=O)O");
		this.getMW("-OC(=S)O-","OC(=S)O");

		this.getMW("-C(=O)OC(=O)-","C(=O)OC(=O)");

		this.getMW("-NH- [3 membered ring]","N([H])");
		this.getMW("-N< [3 membered ring]","N");

		this.getMW("-NH2 [attached to P]","N([H])([H])");
		this.getMW("-NH- [attached to P]","N([H])");
		this.getMW("-N< [attached to P]","N");

		this.getMW("Sn=O","[Sn]=O");
		this.getMW("Sn [oxygen attach]","[Sn]");
		this.getMW("Sn [aromatic attach]","[Sn]");
		this.getMW("Sn","[Sn]");

		this.getMW("Si [aromatic attach]","[Si]");
		this.getMW("Si [oxygen, aromatic attach]","[Si]");
		this.getMW("Si [oxygen attach]","[Si]");
		this.getMW("Si","[Si]");

		this.getMW("-S(=O)(=O)- [2 nitrogen attach]","S(=O)(=O)");
		this.getMW("-S(=O)(=O)- [nitrogen, aromatic attach]","S(=O)(=O)");
		this.getMW("-S(=O)(=O)- [aromatic attach]","S(=O)(=O)");
		this.getMW("-S(=O)(=O)- [olefinic attach]","S(=O)(=O)");
		this.getMW("-S(=O)(=O)- [nitrogen, aliphatic attach]","S(=O)(=O)");
		this.getMW("-S(=O)(=O)- [aliphatic attach]","S(=O)(=O)");

		this.getMW("-S(=O)- [2 nitrogen attach]","S(=O)");
		this.getMW("-S(=O)- [nitrogen, aromatic attach]","S(=O)");
		this.getMW("-S(=O)- [aromatic attach]","S(=O)");
		this.getMW("-S(=O)- olefinic attach]","S(=O)");
		this.getMW("-S(=O)- [nitrogen, aliphatic attach]","S(=O)");
		this.getMW("-S(=O)- [aliphatic attach]","S(=O)");

		this.getMW("-OH [aromatic attach]","O([H])");
		this.getMW("-SH [aromatic attach]","S([H])");
		this.getMW("-N=O [aromatic attach]","N=O");
		this.getMW("-ONO2","O[N+](=O)[O-]");
		this.getMW("-NO2 [aromatic attach]","[N+](=O)[O-]");
		this.getMW("-NO2 [olefinic attach]","[N+](=O)[O-]");
		this.getMW("-NO2 [nitrogen attach]","[N+](=O)[O-]");
		this.getMW("-NO2 [aliphatic attach]","[N+](=O)[O-]");
		this.getMW("-N=C=O [aromatic attach]","N=C=O");
		this.getMW("-N=C=O [aliphatic attach]","N=C=O");
		this.getMW("-COOH [aromatic attach]","C(=O)O([H])");
		this.getMW("-OC(=O)O-","OC(=O)O");


		this.getMW("-C(=O)[O-]","C(=O)[O-]");

		this.getMW("-C(=O)O- [aromatic attach]","C(=O)O");
		this.getMW("-C(=O)SH [aromatic attach]","C(=O)S[H]");
		this.getMW("-C(=O)S- [aromatic attach]","C(=O)S");
		this.getMW("-C(=S)SH [aromatic attach]","C(=S)S[H]");
		this.getMW("-C(=S)S- [aromatic attach]","C(=S)S");
		this.getMW("-C(=S)OH [aromatic attach]","C(=S)O[H]");
		this.getMW("-C(=S)O- [aromatic attach]","C(=S)O");
		this.getMW("-C(=S)- [nitrogen, aromatic attach]","C(=S)");
		this.getMW("-C(=S)- [aromatic attach]","C(=S)");
		this.getMW("-CHO [aromatic attach]","C(=O)([H])");

		this.getMW("-COOH [aliphatic attach]","C(=O)O([H])");
		this.getMW("HC(=O)O-","[H]C(=O)O");
		this.getMW("-C(=O)O- [nitrogen attach]","C(=O)O");
		this.getMW("-C(=O)O- [cyclic]","C(=O)O");
		this.getMW("-C(=O)O- [olefinic attach]","C(=O)O");
		this.getMW("-C(=O)O- [aliphatic attach]","C(=O)O");
		this.getMW("-C(=O)SH [nitrogen attach]","C(=O)S[H]");
		this.getMW("-C(=O)SH [aliphatic attach]","C(=O)S[H]");
		this.getMW("-C(=O)S- [nitrogen attach]","C(=O)S");
		this.getMW("-C(=O)S- [aliphatic attach]","C(=O)S");
		this.getMW("-C(=S)SH [nitrogen attach]","C(=S)S[H]");
		this.getMW("-C(=S)SH [aliphatic attach]","C(=S)S[H]");
		this.getMW("-C(=S)OH [aliphatic attach]","C(=S)O[H]");
		this.getMW("-C(=S)O- [nitrogen attach]","C(=S)O");
		this.getMW("-C(=S)O- [aliphatic attach]","C(=S)O");
		this.getMW("-C(=S)S- [nitrogen attach]","C(=S)S");
		this.getMW("-C(=S)S- [aliphatic attach]","C(=S)S");
		this.getMW("-C(=S)- [2 nitrogen attach]","C(=S)");
		this.getMW("-C(=S)- [nitrogen, aliphatic attach]","C(=S)");
		this.getMW("-C(=S)- [aliphatic attach]","C(=S)");

		this.getMW("-CF3 [aromatic attach]","C(F)(F)F");
		this.getMW("-CCl3 [aromatic attach]","C(Cl)(Cl)Cl");
		this.getMW("-C#N [aromatic attach]","C#N");

		this.getMW("-CH=NOH","C([H])=NO([H])");
		this.getMW("-CH=NO","C([H])=NO");

		this.getMW(">C=NOH","C=NO([H])");
		this.getMW(">C=NO","C=NO");

		this.getMW("-C([H])=N[H] [Nitrogen attach]","C([H])=N[H]");
		this.getMW("-C([H])=N [Nitrogen attach]","C([H])=N");
		this.getMW(">C=N[H] [2 Nitrogen attach]","C=N[H]");
		this.getMW(">C=N[H] [Nitrogen attach]","C=N[H]");
		this.getMW(">C(=N) [2 Nitrogen attach]","C=N");
		this.getMW(">C(=N) [Nitrogen attach]","C=N");

		this.getMW("-CH=NH","C([H])=N([H])");
		this.getMW(">C=NH","C=N([H])");
		this.getMW("CH2=N","C([H])([H])=N");
		this.getMW("-CH=N","C([H])=N");
		this.getMW(">C=N","C=N");

		this.getMW("-NHN=O","N([H])N=O");
		this.getMW(">NN=O","NN=O");
		this.getMW("-N(=O)","N(=O)");
		this.getMW("-N=NH","N=N([H])");
		this.getMW("-N=N-","N=N");

		this.getMW("-NH2 [nitrogen attach]","N([H])([H])");
		this.getMW("-NH- [nitrogen attach]","N([H])");
		this.getMW("-N< [nitrogen attach]","N");

		this.getMW("-CHO [nitrogen attach]","C(=O)([H])");
		this.getMW("-CHO [aliphatic attach]","C(=O)([H])");

		this.getMW("-C(=O)- [halogen attach]","C(=O)");
		this.getMW("-C(=O)- [halogen attach]","C(=O)");
		this.getMW("-C(=O)- [halogen attach]","C(=O)");
		this.getMW("-C(=O)- [halogen attach]","C(=O)");
		this.getMW("-C(=O)- [2 aromatic attach]","C(=O)");
		this.getMW("-C(=O)- [nitrogen, aromatic attach]","C(=O)");
		this.getMW("-C(=O)- [aromatic attach]","C(=O)");
		this.getMW("-C(=O)- [2 nitrogen attach]","C(=O)");
		this.getMW("-C(=O)- [nitrogen, aliphatic attach]","C(=O)");
		this.getMW("-C(=O)- [phosphorus attach]","C(=O)");
		this.getMW("-C(=O)- [olefinic attach]","C(=O)");
		this.getMW("C=O(ketone, aliphatic attach)","C(=O)");
		this.getMW("C=O(non-ketone, aliphatic attach)","C(=O)");

		this.getMW("-CCl3 [aliphatic attach]","C(Cl)(Cl)(Cl)");
		this.getMW("-CF3 [aliphatic attach]","C(F)(F)F");

		this.getMW("-C#N [aliphatic nitrogen attach]","C#N");
		this.getMW("-C#N [aliphatic sulfur attach]","C#N");
		this.getMW("-C#N [olefinic attach]","C#N");
		this.getMW("-C#N [aliphatic attach]","C#N");

		this.getMW("P=NH","P=N[H]");
		this.getMW("P=N","P=N");
		this.getMW("P=S","P=S");
		this.getMW("P(=O)","P(=O)");

		this.getMW("As(=O)","[As](=O)");

		this.getMW("-SH [aliphatic attach]","S([H])");
		this.getMW("-S- [three membered ring]","S");
		this.getMW("-S- [sulfur attach]","S");
		this.getMW("-S- [arsenic attach]","S");
		this.getMW("-S- [phosphorus attach]","S");
		this.getMW("-S- [nitrogen attach]","S");
		this.getMW("-S- [2 aromatic attach]","S");
		this.getMW("-S- [aromatic attach]","S");

		this.getMW("-OH [sulfur attach]","O[H]");
		this.getMW("-OH [oxygen attach]","O[H]");
		this.getMW("-OH [arsenic attach]","O[H]");
		this.getMW("-OH [phosphorus attach]","O[H]");
		this.getMW("-OH [nitrogen attach]","O[H]");
		this.getMW("-OH [aliphatic attach]","O([H])");
		this.getMW("-O- (epoxide)","O");
		this.getMW("-O- [2 aromatic attach]","O");
		this.getMW("-O- [phosphorus, aromatic attach]","O");
		this.getMW("-O- [2 phosphorus attach]","O");
		this.getMW("-O- [phosphorus attach]","O");
		this.getMW("-O- [arsenic attach]","O");
		this.getMW("-O- [nitrogen attach]","O");
		this.getMW("-O- [sulfur attach]","O");
		this.getMW("-O- [oxygen attach]","O");
		this.getMW("-O- [aromatic attach]","O");

		this.getMW("-CH= [aromatic attach]","C([H])");
		this.getMW(">C= [aromatic attach]","C");
		this.getMW("-C# [aromatic attach]","C");
		this.getMW("-CH3 [aromatic attach]","C([H])([H])([H])");
		this.getMW("-CH2- [aromatic attach]","C([H])([H])");
		this.getMW("-CH< [aromatic attach]","C([H])");
		this.getMW(">C< [aromatic attach]","C");

		this.getMW("-CH3 [aliphatic attach]","C([H])([H])([H])");
		this.getMW("-CH2- [aliphatic attach]","C([H])[H]");
		this.getMW("-CH< [aliphatic attach]","C([H])");
		this.getMW(">C< [aliphatic attach]","C");
		this.getMW("=C=","C");
		this.getMW("=CH2 [aliphatic attach]","C([H])([H])");
		this.getMW("=CH [aliphatic attach]","C([H])");
		this.getMW("=C [aliphatic attach]","C");
		this.getMW("#CH [aliphatic attach]","C([H])");
		this.getMW("#C [aliphatic attach]","C");

		this.getMW("-NH2 [aromatic attach]","N([H])([H])");
		this.getMW("-NH- [aromatic attach]","N([H])");
		this.getMW("-N< [aromatic attach]","N");
		this.getMW("-NH2 [aliphatic attach]","N([H])([H])");
		this.getMW("-NH- [aliphatic attach]","N([H])");
		this.getMW("-N< [aliphatic attach]","N");

		this.getMW("Fused aromatic carbon","C");
		this.getMW("Fused aromatic nitrogen","N");
		this.getMW("ACH","C[H]");
		this.getMW("ANH","N([H])");
		this.getMW("AC","C");
		this.getMW("AN","N");
		this.getMW("AN","N");
		this.getMW("AN+","N");
		this.getMW("AN+","N");
		this.getMW("AO","O");
		this.getMW("AS","S");

		this.getMW("=O [other]","O");
		this.getMW("=S [other]","S");
		this.getMW("-[O-]","[O-]");
		this.getMW("-O- [aliphatic attach]","O");
		this.getMW("-S- [aliphatic attach]","S");

		this.getMW("-F [aromatic attach]","F");
		this.getMW("-Br [aromatic attach]","Br");
		this.getMW("-Cl [aromatic attach]","Cl");
		this.getMW("-I [aromatic attach]","I");

		this.getMW("-Br [olefinic attach]","Br");
		this.getMW("-Cl [olefinic attach]","Cl");
		this.getMW("-F [olefinic attach]","F");
		this.getMW("-I [olefinic attach]","I");

		this.getMW("Halogen [Nitrogen attach]","Br");
		this.getMW("Halogen [Nitrogen attach]","Cl");
		this.getMW("Halogen [Nitrogen attach]","F");
		this.getMW("Halogen [Nitrogen attach]","I");

		this.getMW("-Br [aliphatic attach]","Br");
		this.getMW("-Cl [aliphatic attach]","Cl");
		this.getMW("-F [aliphatic attach]","F");
		this.getMW("-I [aliphatic attach]","I");

		this.getMW("H [phosphorus attach]","[H]");
		this.getMW("H [nitrogen attach]","[H]");
		this.getMW("H [silicon attach]","[H]");
		this.getMW("H [carbon attach]","[H]");
		this.getMW("H [other]","[H]");

		this.getMW("-Hg","[Hg]");
		this.getMW("-Hg-","[Hg]");
		this.getMW("-Hg+","[Hg]");

		this.getMW("Hg","[Hg]");
		this.getMW("Pb","[Pb]");
		this.getMW("B","[B]");

		this.getMW("P [+3 valence, all single bonds]","P");
		this.getMW("P [+3 valence, all single bonds]","P");
		this.getMW("P [+3 valence, all single bonds]","P");
		this.getMW("P [+5 valence, one double bond]","P");
		this.getMW("P [+5 valence, two double bonds]","P");
		this.getMW("P [+5 valence, all single bonds]","P");
		this.getMW("P","P");

		this.getMW("As [+3 valence, one double bond]","[As]");
		this.getMW("As [+3 valence, all single bonds]","[As]");
		this.getMW("As [+3 valence, all single bonds]","[As]");
		this.getMW("As [+3 valence, all single bonds]","[As]");
		this.getMW("As [+5 valence, one double bond]","[As]");
		this.getMW("As [+5 valence, two double bonds]","[As]");
		this.getMW("As [+5 valence, all single bonds]","[As]");
		this.getMW("As","[As]");

		
}
	
	

	private void getMW(String fragdesc, String smiles) {

		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			AtomContainer fragment = (AtomContainer)sp.parseSmiles(smiles);
			
			double MW=this.Calculate_mw(fragment);
			this.htFragMW.put(fragdesc, MW);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	
	public void Calculate() {
		this.FindFrags();

		DecimalFormat df=new DecimalFormat("0.00");
		
		for (Map.Entry<String, Double> entry : htFragMW.entrySet()) {
		     System.out.println(entry.getKey() +"\t"+df.format(entry.getValue()));
		}
		

	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CalculateMWFrags c=new CalculateMWFrags();
//		c.Calculate();
		c.testDescriptorFile();

	}

}
