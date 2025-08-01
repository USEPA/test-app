/*
 * ChemicalFinder.java: imports chemicals from the NCI database
 * 
 * Started - 12/11/2006
 * Modified - 12/19/2006 to include a tolerance variable
 */

package ToxPredictor.Utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import ToxPredictor.MyDescriptors.DescriptorFactory;
import ToxPredictor.misc.MolFileUtilities;
import ToxPredictor.misc.ParseChemidplus;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.DefaultChemObjectBuilder;

import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.smiles.SmilesParser;

import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;


//import ToxPredictor.Utilities.MDLReader;

/**
 * Finds a chemical from the database given the CAS, SMILES, Formula, Molecular Weight, 
 * 2D coordinates, 3D coordinates
 */

public class ChemicalFinder {

	/**
	 * Tolerance value; difference between user entered MW and MW of chemicals in 
	 * the NCI database
	 */
	private double tol = 0.2;
	private String file1="ncidb.Manifest";
	public String coordfolder="coords";

	int ColCAS=0;
	int ColSMILES=1;
	int ColFormula=2;
	int ColMW=3;
	int ColName=4;

	public ChemicalFinder () {}
	
	public ChemicalFinder (String manifestFilename,String coordinatesFolder) {
		this.file1=manifestFilename;
		this.coordfolder=coordinatesFolder;		
	}
	
	

	/**
	 * Method to retrieve molecule from database specified in coordfolder based on the CAS
	 * @param cas (CAS number with hyphens)
	 * @return IMolecule mol
	 */
	public IAtomContainer FindChemicalFromCAS(String CAS) {
		
		ParseChemidplus parseChemidplus=new ParseChemidplus();
		
		String filepath = coordfolder+"/" +CAS + ".mol";
//		javax.swing.JOptionPane.showMessageDialog(null,filepath);
		
		IAtomContainer mol = null;
		
		try {
		boolean HaveCAS=parseChemidplus.HaveMolFileInJar(filepath);
		
//		System.out.println(filepath);
//		System.out.println(coordfolder);
//		System.out.println(CAS+"\t"+HaveCAS);
		
		if (HaveCAS) {
			mol=parseChemidplus.LoadChemicalFromMolFileInJar(filepath);
			mol.setProperty("CAS", CAS);			
			return mol;
		} else {
			return null;
		}
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return null;
		
	}// public IMolecule FindChemicalFromCAS
	
	/**
	 * Method to retrieve molecule from smiles string in text listing 
	 * of validated structures (manifest file) 
	 * @param cas (CAS number with hyphens)
	 * @return IMolecule mol
	 */
	public AtomContainer FindChemicalFromCAS2(String CAS) {
		
		try {
			//Check for the presence of the formula in the jar file Manifest
			
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			
			
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header=br.readLine();
			
			int CASCol=ToxPredictor.Utilities.Utilities.FindFieldNumber(header, "CAS Number");
			

			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.LinkedList list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				
				String currentCAS=(String)list.get(ColCAS);
					
				if (CAS.equals(currentCAS)) {
					String SMILES=(String)list.get(ColSMILES);
					String Name=(String)list.get(ColName);	
					
					AtomContainer molecule=(AtomContainer) sp.parseSmiles(SMILES);
					
					if (molecule!=null) {
						molecule.setProperty("Source","ValidatedStructures");
						molecule.setProperty("Name",Name);
						molecule.setProperty("CAS",CAS);
					}
					
					return molecule;
				}
				
			}//while line is not empty
			br.close();
			isr.close();
			ins.close();
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch

		return null;
		
	}// public IMolecule FindChemicalFromCAS


	
	
	
	
	/**
	 * Method to search for the chemical structure given the CAS number
	 * @param cas (CAS number with hyphens)
	 * @return IMolecule mol
	 */
	public AtomContainer LoadChemicalFromCAS(String cas) {
		AtomContainer mol = null;

		try {
			
			
			String file = coordfolder+"/" +cas + ".mol";
			
				mol=new AtomContainer();
				InputStream ins = this.getClass().getClassLoader().getResourceAsStream(file);
						
				MDLV2000Reader mr=new MDLV2000Reader(ins);
				
				mr.read(mol);
				ins.close();

		}//try
		catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Error loading "+cas);
		}//catch
		return mol;
	}// public IMolecule LoadChemicalFromfromCAS
	
	/**
	 * Method to search for chemical structures in the database given an IMolecule
	 * from either SMILES, 2D or 3D
	 * @param mol
	 * @return List of IMolecules
	 */
	public AtomContainerSet FindChemicalFromMolecule(AtomContainer mol) {
		String form;
		AtomContainerSet mols = new AtomContainerSet();
//		ArrayList<String> casnums = new ArrayList<String>();

		//First, get the molecular formula of the chemical
		 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol);
		 String strFormula=MolecularFormulaManipulator.getString(mf);

		

		//Get the list of IMolecules that have the same formula
		AtomContainerSet ims = FindChemicalsFromFormula(strFormula);

		//Compare the IMolecule with molecules from the list
		for (int i = 0; i < ims.getAtomContainerCount(); i++) {
			AtomContainer mol1 = (AtomContainer) ims.getAtomContainer(i);
			boolean present = chemicalcompare.isIsomorphFingerPrinter(mol, mol1);
			if (present)
				mols.addAtomContainer(mol1);
		}//for int i=0
		return mols;
	}//public void FindChemicalFromMolecule

	/**
	 * Method to search for chemical structures in the database given the CAS numbers
	 * from either SMILES, 2D or 3D
	 * 
	 * @param mol
	 * @return List of CAS numbers in the NCI database that match the chemical
	 */
	public List FindCASFromMolecule(IAtomContainer mol,String compareMethod) {
		
//		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		ArrayList<String> casnums = new ArrayList<String>();

		try {
			
			IAtomContainer molClone=CDKUtilities.addHydrogens(mol);
//		
//			MyHydrogenAdder myHA = new MyHydrogenAdder();//better but slow since descriptor calculation and ring finding needed
//			molClone = myHA.AddExplicitHydrogens(molClone);
			
			// First, get the molecular formula of the chemical
			MolecularFormula formula=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol);
			String form=MolecularFormulaManipulator.getString(formula);

//			System.out.println(form);
			
			// Get the list of CAS numbers of IMolecules that have the same
			// formula
			List ims = FindCASFromFormula(form);
			
//			System.out.println(ims.size());

			// Compare the IMolecule with molecules from the list
			for (int i = 0; i < ims.size(); i++) {
				String cas = (String) ims.get(i);
				String file = coordfolder+"/" + cas + ".mol";
//				 System.out.println(file);
				InputStream ins = this.getClass().getClassLoader()
						.getResourceAsStream(file);
				
				
				AtomContainer moli=new AtomContainer();
				
				MDLV2000Reader mr=new MDLV2000Reader(ins);
				mr.read(moli);
				mr.close();

				
//				InputStreamReader isr = new InputStreamReader(ins);
//				BufferedReader br = new BufferedReader(isr);
//				MDLReader reader = new MDLReader();
/*				ChemFile content = (ChemFile) reader
						.read((ChemObject) new ChemFile());
				List containers = ChemFileManipulator
						.getAllAtomContainers(content);
*/
//				IMolecule moli = reader.readMolecule(br);
//				br.close();
//				isr.close();
//				ins.close();
				
				boolean present=false;
				
				if (compareMethod.equals(chemicalcompare.method2dDescriptors)) {
					present = chemicalcompare.isIsomorph2ddescriptors(molClone, moli);
				} else if (compareMethod.equals(chemicalcompare.methodFingerPrinter)) {
					present = chemicalcompare.isIsomorphFingerPrinter(molClone, moli);
				} else if (compareMethod.equals(chemicalcompare.methodHybrid)) {
					present=chemicalcompare.isIsomorphHybrid(molClone, moli, false, true,true);//dont need to check formula again since we already did that above
				}
				
//				boolean present = chemicalcompare.isIsomorph(mol, mol1);
				
//				System.out.println(mol.getAtomCount()+"\t"+mol1.getAtomCount());
//				boolean present = chemicalcompare.isIsomorph2ddescriptors(mol, mol1);
				if (present)
					casnums.add(cas);
			}// for int i=0
		}// try
		catch (Exception e) {
			e.printStackTrace();
		}// catch
		return casnums;
	}// public void FindCASFromMolecule
	
	
	/**
	 * Method to search for chemical structures in the database given the CAS numbers
	 * from either SMILES, 2D or 3D
	 * 
	 * @param mol
	 * @return List of CAS numbers in the NCI database that match the chemical
	 */
	public List FindCASFromMolecule(AtomContainer mol,String compareMethod,String structureFolder,String manifestFileName) {
		
//		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		ArrayList<String> casnums = new ArrayList<String>();

		try {
			
			AtomContainer mol_w_hydrogens = (AtomContainer) CDKUtilities.addHydrogens(mol);
			
			// First, get the molecular formula of the chemical
			 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol);
			 String strFormula=MolecularFormulaManipulator.getString(mf);

			// Get the list of CAS numbers of IMolecules that have the same
			// formula
			List ims = FindCASFromFormula(strFormula,structureFolder+"/"+manifestFileName);

//			for (int i=0;i<ims.size();i++) {
//				System.out.println(i+"\t"+ims.get(i));	
//			}
			
			
			
			// Compare the IMolecule with molecules from the list
			for (int i = 0; i < ims.size(); i++) {
				
				
				String cas = (String) ims.get(i);
				String file = structureFolder+"/" + cas + ".mol";
				
				
				AtomContainer mol1=new AtomContainer();
				MDLV2000Reader mr=new MDLV2000Reader(new FileInputStream(file));
				mr.read(mol1);

				boolean present=false;
				
				AtomContainerSet molSet = (AtomContainerSet)ConnectivityChecker.partitionIntoMolecules(mol1);
				if (molSet.getAtomContainerCount()>1) {
					continue;
				}

				
				if (compareMethod.equals(chemicalcompare.method2dDescriptors)) {
					present = chemicalcompare.isIsomorph2ddescriptors(mol_w_hydrogens, mol1);
				} else if (compareMethod.equals(chemicalcompare.methodFingerPrinter)) {
					present = chemicalcompare.isIsomorphFingerPrinter(mol, mol1);
				} else if (compareMethod.equals(chemicalcompare.methodHybrid)) {
					present=chemicalcompare.isIsomorphHybrid(mol, mol1, false,true,true);
				}
				
//				boolean present = chemicalcompare.isIsomorph(mol, mol1);
				
//				System.out.println(mol.getAtomCount()+"\t"+mol1.getAtomCount());
//				boolean present = chemicalcompare.isIsomorph2ddescriptors(mol, mol1);
				if (present)
					casnums.add(cas);
			}// for int i=0
		}// try
		catch (Exception e) {
			e.printStackTrace();
		}// catch
		return casnums;
	}// public void FindCASFromMolecule

	
	/**
	 * Method to search for structure using all of our structure databases
	 * from either SMILES, 2D or 3D
	 * 
	 * @param mol
	 * @return List of CAS numbers in the NCI database that match the chemical
	 */
	public String FindCASFromMoleculeUsingFromAllDatabases(AtomContainer mol,String compareMethod) {
		String form;
//		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		ArrayList<String> casnums = new ArrayList<String>();

		

		try {
			

			String manifestFileName="manifest.txt";

			ParseChemidplus p=new ParseChemidplus();

			java.util.ArrayList<String> structureFolderList=new java.util.ArrayList<String>();

			structureFolderList.add(p.Chemidplus3dMolFolder);
			structureFolderList.add(p.DougMolFolder);
			structureFolderList.add(p.IndexNet2DFolder);
			structureFolderList.add(p.DSSToxFolder);
			structureFolderList.add(p.NameFolder);
			structureFolderList.add(p.NIST2DFolder);
			structureFolderList.add(p.NIST3DFolder);
			structureFolderList.add(p.SmilesFolder);
			
			ArrayList<String> overallList=new ArrayList<String>();
			java.util.Hashtable ht=new java.util.Hashtable();
			
			int maxCount=0;
			String maxCAS="";
			
			for (int i=0;i<structureFolderList.size();i++) {
				List list=this.FindCASFromMolecule(mol, compareMethod,structureFolderList.get(i), manifestFileName);

//				System.out.println(structureFolderList.get(i));
				for (int j=0;j<list.size();j++) {
//					System.out.print(list.get(j)+"\t");
					String casij=(String)list.get(j);
					
					int count=0;
					if (ht.get(casij)!=null) {
						count=(Integer)ht.get(casij);
						ht.put(casij,count+1);
					} else {
						count=1;
						ht.put(casij,1);
					}
					if (count>maxCount) {
						maxCount=count;
						maxCAS=casij;
					}
					
					overallList.add((String)list.get(j));
				}
//				System.out.print("\r\n");
			}
		
			Collections.sort(overallList);
			
//			System.out.println(maxCAS+"\t"+maxCount);
			if (maxCAS.equals("")) return "Not found";
			
			return maxCAS;
			
		}// try
		catch (Exception e) {
			e.printStackTrace();
			return "Not found";
		}// catch
//		return casnums;
		
	}// public void FindCASFromMolecule

	
	/**
	 * Method to search for chemical structures in the database given the CAS
	 * numbers from either SMILES, 2D or 3D
	 * 
	 * @param mol
	 * @return List of data rows (each row is a tab delimited string with CAS,
	 *         SMILES, Formula, MW, and Name
	 */
	public List FindCASFromMolecule2(AtomContainer mol) {
		
//		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		ArrayList<String> datalist = new ArrayList<String>();

		try {
			// First, get the molecular formula of the chemical
			
			 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(mol);
			 String formula=MolecularFormulaManipulator.getString(mf);

			// Get the list of CAS numbers of IMolecules that have the same
			// formula
			
			java.util.List<String> datarows=this.FindCASFromFormula2(formula);

			// Compare the IMolecule with molecules from the list
			for (int i = 0; i < datarows.size(); i++) {
				String datarow=datarows.get(i);
				
				java.util.List<String> datacols=ToxPredictor.Utilities.Utilities.Parse3(datarow,"\t");
				String CAS=datacols.get(ColCAS);
				
				AtomContainer mol1=this.FindChemicalFromCAS2(CAS);
				boolean match = chemicalcompare.isIsomorphFingerPrinter(mol, mol1);
				
				if (match) { //there might be more than 1 CAS with same structure
					datalist.add(datarow);
				}
				
			}// for int i=0
		}// try
		catch (Exception e) {
			e.printStackTrace();
		}// catch
		return datalist;
	}// public void FindCASFromMolecule

	
	/**
	 * Method to search for CAS numbers given the Molecular Formula
	 * of a chemical. The Molecular Formula can be entered in any manner; the
	 * formula will be rewritten to follow the Hill System Order (carbons followed
	 * by hydrogens followed by the chemical elements in alphabetical order).
	 * @param form
	 * @return List of CAS numbers
	 */
	public List FindCASFromFormula(String formula) {
		
		ArrayList<String> casnums = new ArrayList<String>();

		//Change the user-entered formula string to the standard form
		String form = CorrectFormula(formula);//TODO- needed???

//		System.out.println(form);
		
		try {
			//Check for the presence of the formula in the jar file Manifest
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
//			System.out.println(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);
			String line;
			String junk;
			StringTokenizer token;
			String cas = null;
			line = in.readLine(); //read the first line in the file
			String formline;

			while (line != null) {
				token = new StringTokenizer(line, "\t,;|");
				cas = token.nextToken(); //first token is the CAS number
				junk = token.nextToken(); //second token is the SMILES
				formline = token.nextToken(); //third token is the Formula
				if (formline.equals(form))
					casnums.add(cas);
				line = in.readLine();
			}//while line is not empty
			in.close();
			isr.close();
			ins.close();
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		
		sortCASList(casnums);
		
		return casnums;
	}//public List FindCASFromFormula
	
	/**
	 * Method to search for CAS numbers given the Molecular Formula
	 * of a chemical. The Molecular Formula can be entered in any manner; the
	 * formula will be rewritten to follow the Hill System Order (carbons followed
	 * by hydrogens followed by the chemical elements in alphabetical order).
	 * 
	 * The manifest is assumed to have the form: CAS	Formula on each line
	 * 
	 * @param form
	 * @return List of CAS numbers
	 */
	public List FindCASFromFormula(String formula,String manifestFilePath) {
		String form = formula;
		ArrayList<String> casnums = new ArrayList<String>();

		//Change the user-entered formula string to the standard form
		form = CorrectFormula(formula);

		try {
			//Check for the presence of the formula in the jar file Manifest
			
			BufferedReader in = new BufferedReader(new FileReader(manifestFilePath));
			String line;
			String junk;
			StringTokenizer token;
			String cas = null;
			line = in.readLine(); //read the first line in the file
			String formline="";

			while (line != null) {
				token = new StringTokenizer(line, "\t,;|");
				cas = token.nextToken(); //first token is the CAS number
				
				if (token.hasMoreTokens()) {
					formline = token.nextToken(); //second token is the Formula

					if (formline.equals(form))
						casnums.add(cas);
				}
				line = in.readLine();
			}//while line is not empty
			in.close();
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return casnums;
	}//public List FindCASFromFormula
	
	/**
	 * Method to search for CAS numbers given the Molecular Formula
	 * of a chemical. The Molecular Formula can be entered in any manner; the
	 * formula will be rewritten to follow the Hill System Order (carbons followed
	 * by hydrogens followed by the chemical elements in alphabetical order).
	 * @param form
	 * @return List of data rows (each row is a tab delimited string with CAS,
	 *         SMILES, Formula, MW, and Name
	 */
	public List FindCASFromFormula2(String formula) {
		String form = formula;
		ArrayList<String> datarows = new ArrayList<String>();

		//Change the user-entered formula string to the standard form
		form = CorrectFormula(formula);

		try {
			//Check for the presence of the formula in the jar file Manifest
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header=br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.LinkedList list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				String currentFormula=(String)list.get(ColFormula);
				
				if (formula.equals(currentFormula)) {
//					System.out.println(Line);
					datarows.add(Line);
				}
				
			}//while line is not empty
			br.close();
			isr.close();
			ins.close();
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return datarows;
	}//public List FindCASFromFormula
	
	
	public static void sortCASList(ArrayList<String> caslist) {

		
//		for (int i=0;i<caslist.size();i++) {
//			System.out.println(i+"\t"+caslist.get(i));
//		}

		for (int i=0;i<caslist.size();i++) {
			String CASi=caslist.get(i);
			
			while (CASi.length()<12) CASi="0"+CASi;
			caslist.set(i, CASi);
		}
		Collections.sort(caslist);
		
		for (int i=0;i<caslist.size();i++) {
			String CASi=caslist.get(i);
			
			while (CASi.indexOf("0")==0) {
				CASi=CASi.substring(1,CASi.length());
				if (CASi.indexOf("-")==1) break;
			}
			caslist.set(i, CASi);
		}
//		for (int i=0;i<caslist.size();i++) {
//			System.out.println(i+"\t"+caslist.get(i));
//		}
		
	}
	
	/**
	 * Method to search for CAS numbers given the Molecular Formula
	 * of a chemical. The Molecular Formula can be entered in any manner; the
	 * formula will be rewritten to follow the Hill System Order (carbons followed
	 * by hydrogens followed by the chemical elements in alphabetical order).
	 * @param form
	 * @return List of data rows (each row is a tab delimited string with CAS,
	 *         SMILES, Formula, MW, and Name
	 */
	public List FindCASFromMolecularWeight2(double MW) {
		
		ArrayList<String> datarows = new ArrayList<String>();


		try {
			//Check for the presence of the formula in the jar file Manifest
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader br = new BufferedReader(isr);

			String header=br.readLine();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				java.util.LinkedList list=ToxPredictor.Utilities.Utilities.Parse3(Line, "\t");
				String strweight=(String)list.get(ColMW);
				
				double currentMW = Double.parseDouble(strweight); //fourth token
				if (Math.abs(MW - currentMW) < tol) {
					datarows.add(Line);
				}
				
			}//while line is not empty
			br.close();
			isr.close();
			ins.close();
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return datarows;
	}//public List FindCASFromFormula


	/**
	 * Method to put the Molecular Formula in the standard format (according to
	 * Hill's criteria)
	 * @param form
	 * @return String Molecular Formula
	 */
	public static String CorrectFormula(String form) {
		String formula = null;
		try {
			
			 MolecularFormula mf=(MolecularFormula)MolecularFormulaManipulator.getMolecularFormula(form,DefaultChemObjectBuilder.getInstance());
			 formula=MolecularFormulaManipulator.getString(mf);
			 
			
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}
		return formula;
	}//private String CorrectFormula

	/**
	 * Method to search for chemical structures given the Molecular Formula
	 * of a chemical. The Molecular Formula can be entered in any manner; the
	 * formula will be rewritten to follow the Hill System Order (carbons followed
	 * by hydrogens followed by the chemical elements in alphabetical order).
	 * @param form
	 * @return List of IMolecules
	 */
	public AtomContainerSet FindChemicalsFromFormula(String formula) {
		
		
//		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		ArrayList<String> casnums = new ArrayList<String>();
		AtomContainerSet molecules = new AtomContainerSet();

		//Change the user-entered formula string to the standard form
		
		String form = CorrectFormula(formula);

		try {
			//Check for the presence of the formula in the jar file Manifest
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);
			String line;
			String junk;
			StringTokenizer token;
			String cas = null;
			line = in.readLine(); //read the first line in the file
			String formline;

			while (line != null) {
				token = new StringTokenizer(line, "\t,;|");
				cas = token.nextToken(); //first token is the CAS
				junk = token.nextToken();//second token is SMILES
				formline = token.nextToken(); //third token is formula
				if (formline.equals(form))
					casnums.add(cas);
				line = in.readLine();
			}//while line is not empty
			in.close();
			isr.close();
			ins.close();

			//If the mol file is present, read the file, and return the IMolecule
			if (casnums.size() > 0) {
				for (int i = 0; i < casnums.size(); i++) {
					cas = (String) casnums.get(i);
					String file = coordfolder+"/" +cas + ".mol";
					ins = this.getClass().getClassLoader().getResourceAsStream(
							file);
					
					
					
					AtomContainer mol=new AtomContainer();
					
					MDLV2000Reader mr=new MDLV2000Reader(ins);
					
					mr.read(mol);
					mol.setProperty("CAS", cas);
					molecules.addAtomContainer(mol);
					
					ins.close();
				}//for int i=0
			}//if the number of cas numbers in the list is greater than zero

		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return molecules;
	}//public List FindChemicalsFromFormula

	/**
	 * Method to search for CAS numbers given the Molecular Weight
	 * of a chemical. All CAS numbers within a weight of 1 from the specified
	 * MW will be returned.
	 * @param form
	 * @return List of CAS numbers
	 */
	public List FindCASFromMolecularWeight(double mw) {
		ArrayList<String> casnums = new ArrayList<String>();

		try {
			//Check for the presence of the Molecular Weight in the jar file Manifest
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);
			String line;
			String junk;
			double weight = 0;
			StringTokenizer token;
			String cas = null;
			line = in.readLine(); //read the first line in the file
			line = in.readLine(); //read the second line in the file

			while (line != null) {
				token = new StringTokenizer(line, "\t,;|");
				cas = token.nextToken();//CAS is the first token
				junk = token.nextToken();//SMILES
				junk = token.nextToken();//Formula
				
				String strweight=token.nextToken();
				
				try {
					weight = Double.parseDouble(strweight); //fourth token
					if (Math.abs(mw - weight) < tol)
						casnums.add(cas);
					
				} catch (Exception e) {
//					System.out.println(strweight);
					//continue if doesnt have weight
				}
				
				line = in.readLine();
			}//while line is not empty
			in.close();
			isr.close();
			ins.close();
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return casnums;
	}//public List FindCASFromFormula

	/**
	 * Method to search for chemical structures given the Molecular weight
	 * of a chemical. All Molecules within a weight of 1 from the specified MW
	 * will be returned.
	 * @param form
	 * @return List of IMolecules
	 */
	public AtomContainerSet FindChemicalsFromMolecularWeight(double mw) {
		
//		ArrayList<Molecule> mols = new ArrayList<Molecule>();
		ArrayList<String> casnums = new ArrayList<String>();
		AtomContainerSet molecules = new AtomContainerSet();


		try {
			//Check for the presence of the Molecular Weight in the jar file Manifest
			
			InputStream ins = this.getClass().getClassLoader()
					.getResourceAsStream(file1);
			InputStreamReader isr = new InputStreamReader(ins);
			BufferedReader in = new BufferedReader(isr);
			String line;
			String junk;
			double weight = 0;
			StringTokenizer token;
			String cas = null;
			line = in.readLine(); //read the first line in the file
			line = in.readLine(); //read the second line in the file

			while (line != null) {
				token = new StringTokenizer(line, "\t,;|");
				cas = token.nextToken();//CAS
				junk = token.nextToken();//SMILES
				junk = token.nextToken();//Formula
//				System.out.println(junk);
				weight = (new Double(token.nextToken()).doubleValue()); //MW
				if (Math.abs(mw - weight) < tol)//if value is less than the tolerance value
					casnums.add(cas);
				line = in.readLine();
			}//while line is not empty
			in.close();
			isr.close();
			ins.close();

//			System.out.println(casnums.size());
//			for (int i=0; i<casnums.size(); i++)
//				System.out.println(i +": " +casnums.get(i));
			
			//If the mol file is present, read the file, and return the List of IMolecules
			if (casnums.size() > 0) {
				for (int i = 0; i < casnums.size(); i++) {
					cas = (String) casnums.get(i);
					String file = coordfolder+"/" +cas + ".mol";
					ins = this.getClass().getClassLoader().getResourceAsStream(
							file);
					
					
					AtomContainer mol=new AtomContainer();
					MDLV2000Reader mr=new MDLV2000Reader(ins);
					
					mr.read(mol);
					mol.setProperty("CAS", cas);
//					String s = (String)mol.getProperty("cas");
//					System.out.println(s);
					molecules.addAtomContainer(mol);
//					mols.add(new Molecule(mol));//Adds the IMolecule to the List
					ins.close();
				}//for int i=0
			}//if the number of cas numbers in the list is greater than zero
		}//try
		catch (Exception e) {
			e.printStackTrace();
		}//catch
		return molecules;
	}//public List FindChemicalsFromFormula


	void FindStructuresForSurfaceTensionChemicals() {
		String StructureFolder="ValidatedStructures2d";
		String strFileSep="/";//TMM 11/20/08: for some reason using File.separator doesnt work now when specifiying path in jar files
		
		this.file1=StructureFolder+strFileSep+"manifest.txt";
		this.coordfolder=StructureFolder;

//		String SDFfilepath="C:/Documents and Settings/tmarti02/My Documents/javax/cdk/ToxPredictor/DescriptorTextTables/ST_Final3 data files/ST_overall.sdf";
//		String SDFfilepath="ToxPredictor/structures/get name structures/riddick viscosity chemicals.sdf";
//		String SDFfilepath="C:/Documents and Settings/tmarti02/My Documents/comptox/0 Physical Properties/ST/final2 sdf files/bob.sdf";
//		String SDFfilepath="C:/Documents and Settings/tmarti02/My Documents/comptox/benfenati/BCF/need cas.sdf";
//		String SDFfilepath="C:/Documents and Settings/tmarti02/My Documents/comptox/Mace Barron/articles/aptula/name structures.sdf";
//		String SDFfilepath="L:/Priv/Cin/NRMRL/CompTox/comptox/0 Physical Properties/AIT/chen name structures.sdf";
//		String SDFfilepath="L:/Priv/Cin/NRMRL/CompTox/comptox/0 Physical Properties/LFL/name structures.sdf";
		String SDFfilepath="L:/Priv/Cin/NRMRL/CompTox/comptox/0 Physical Properties/UFL/name structures.sdf";
		
		AtomContainerSet AtomContainerSet=MolFileUtilities.LoadFromSDF(SDFfilepath);
		
		String compareMethod=chemicalcompare.methodHybrid;
//		String compareMethod=chemicalcompare.method2dDescriptors;
//		String compareMethod=chemicalcompare.methodFingerPrinter;
		
		ParseChemidplus p=new ParseChemidplus();
//		String structureFolder=p.Chemidplus3dMolFolder;
		
//		for (int i=2;i<=2;i++) {
		for (int i=0;i<AtomContainerSet.getAtomContainerCount();i++) {
			
			AtomContainer mol=(AtomContainer)AtomContainerSet.getAtomContainer(i);
			
			String CAS=(String)mol.getProperty("CAS");
			
			
//			MyHydrogenAdder myHA = new MyHydrogenAdder();
//			IAtomContainer mol2 = myHA.AddExplicitHydrogens(mol);

//			if (!CAS.equals("T371")) continue;

			
			System.out.print((i+1)+"\t"+CAS+"\t");
			
			
			AtomContainerSet molSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(mol);

			if (molSet.getAtomContainerCount() > 1) {
				System.out.println("error: multiple molecules");
				continue;
			}
			if (mol==null || mol.getAtomCount()==0 ) {
				System.out.println("error: no atoms");
				continue;
			}
			
			// *******************************************************************			
//			List list=FindCASFromMolecule(mol,compareMethod);
			// *******************************************************************
			
//			List list=FindCASFromMolecule(mol,compareMethod,structureFolder,"manifest.txt");
//			if (list.size()>0) {
//				for (int j=0;j<list.size();j++) {
//					System.out.print(list.get(j)+"\t");
//				}
//				System.out.print("\r\n");
//			} else {
//				System.out.print("Not found\r\n");
//			}
			
			// *******************************************************************
			String CASmatch=this.FindCASFromMoleculeUsingFromAllDatabases(mol, compareMethod);
			System.out.println(CASmatch);
			
		}

	}
	
	void FindStructuresForRiddickViscosityChemicals() {
		String StructureFolder="ValidatedStructures2d";
		String strFileSep="/";//TMM 11/20/08: for some reason using File.separator doesnt work now when specifiying path in jar files
		
		this.file1=StructureFolder+strFileSep+"manifest.txt";
		this.coordfolder=StructureFolder;

		String SDFfilepath="ToxPredictor/structures/get name structures/riddick viscosity chemicals.sdf";
		
		
		AtomContainerSet AtomContainerSet=MolFileUtilities.LoadFromSDF(SDFfilepath);
		
		String compareMethod=chemicalcompare.method2dDescriptors;
//		String compareMethod=chemicalcompare.methodFingerPrinter;
		
		ParseChemidplus p=new ParseChemidplus();
//		String structureFolder=p.Chemidplus3dMolFolder;
		
		
		for (int i=0;i<AtomContainerSet.getAtomContainerCount();i++) {
			
			AtomContainer mol=(AtomContainer)AtomContainerSet.getAtomContainer(i);
			
			String Number=(String)mol.getProperty("Number");
			
			
//			MyHydrogenAdder myHA = new MyHydrogenAdder();
//			IAtomContainer mol2 = myHA.AddExplicitHydrogens(mol);
			
			System.out.print(Number+"\t");
			
			AtomContainerSet molSet = (AtomContainerSet) ConnectivityChecker.partitionIntoMolecules(mol);

			if (molSet.getAtomContainerCount() > 1) {
				System.out.println("error: multiple molecules");
				continue;
			}
			if (mol==null || mol.getAtomCount()==0 ) {
				System.out.println("error: no atoms");
				continue;
			}
			
			// *******************************************************************			
//			List list=FindCASFromMolecule(mol,compareMethod);
			// *******************************************************************
			
//			List list=FindCASFromMolecule(mol,compareMethod,structureFolder,"manifest.txt");
//			if (list.size()>0) {
//				for (int j=0;j<list.size();j++) {
//					System.out.print(list.get(j)+"\t");
//				}
//				System.out.print("\r\n");
//			} else {
//				System.out.print("Not found\r\n");
//			}
			
			// *******************************************************************
			String CASmatch=this.FindCASFromMoleculeUsingFromAllDatabases(mol, compareMethod);
			System.out.println(CASmatch);
			
		}

	}
	
	public static void main(String[] args) {
		ChemicalFinder cf=new ChemicalFinder();
		
//		String sep=File.separator;//for some reason this no longer works!
//		String sep="/";
//		
//		cf.file1="ValidatedStructures2d"+sep+"manifest.txt";
//		AtomContainerSet ms=cf.FindChemicalsFromFormula("C6H6");
//		System.out.println(ms.getAtomContainerCount());
		
		// **************************************************************
		
//		cf.FindStructuresForRiddickViscosityChemicals();
//		cf.FindStructuresForSurfaceTensionChemicals();
		
		System.out.println(cf.CorrectFormula("H6C6"));
		

//		try {
//			BufferedReader br=new BufferedReader(new FileReader("C:/Documents and Settings/tmarti02/My Documents/mol files/71-43-2.mol"));
//			ToxPredictor.Utilities.MDLReader mr=new ToxPredictor.Utilities.MDLReader();
//			IMolecule mol=mr.readMolecule(br);
//			br.close();
//			String CAS=cf.FindCASFromMoleculeUsingFromAllDatabases(mol, chemicalcompare.method2dDescriptors);
//			System.out.println(CAS);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		ParseChemidplus p=new ParseChemidplus();
//		List l=cf.FindCASFromFormula("C7H8O", p.Chemidplus3dMolFolder+"/manifest.txt");
//		for(int i=0;i<l.size();i++) System.out.println(l.get(i));
		
	}
	
}//public class FindChemical
