package ToxPredictor.MyDescriptors;
//my imports:
import ToxPredictor.Utilities.*;
import ToxPredictor.misc.Lookup;
import ToxPredictor.misc.MolFileUtilities;
import ToxPredictor.misc.ParseChemidplus;
//import ToxPredictor.My3Ddescriptors.*;

//java imports:
import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openscience.cdk.qsar.descriptors.molecular.MomentOfInertiaDescriptor;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
//import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import ToxPredictor.MyDescriptors.XLogPDescriptor;
import org.openscience.cdk.qsar.result.*;
import org.openscience.cdk.qsar.*;
import org.openscience.cdk.qsar.result.DoubleResult;

//import org.openscience.cdk.renderer.Renderer2D;
//import org.openscience.cdk.renderer.Renderer2DModel;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.ElectronDonation;
import org.openscience.cdk.config.Isotopes;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.formula.MolecularFormula;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.CycleFinder;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.io.MDLV2000Reader;

import org.openscience.cdk.normalize.Normalizer;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
//import org.openscience.cdk.tools.HydrogenAdder;
//import org.openscience.cdk.tools.Normalizer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.Ring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//TODO: add error propagation- i.e. make all methods throw exceptions 

public class DescriptorFactory {

	private static final Logger logger = LogManager.getLogger(DescriptorFactory.class);

	/**
	 * @param args
	 */

	// private AdaptMolecule m;
	// FileWriter fw;
	DecimalFormat myF = new DecimalFormat("0.000000");

	public static boolean debug = false;
	public boolean done;

	public String errorMsg = "";

	// words of caution- need to make sure when fortran changes a scalar value
	// in subroutine we
	// make the change in the place where the routine was called from

	// TMM changes:
	// 1. Changed how DV for certain atoms is calculated

	public boolean UnassignedAtoms; // flag for case where there is atoms with
									// an

	// undefined estate fragment

	// IAtomContainer m;

	DescriptorData dd;

	ChiDescriptor cd = new ChiDescriptor();

	EStateFragmentDescriptor esfd = new EStateFragmentDescriptor();

	public MoleculeFragmenter4 moleculefragmenter = new MoleculeFragmenter4();

	TwoDMolecularDescriptors twod = new TwoDMolecularDescriptors();

	TopologicalDescriptors td = new TopologicalDescriptors();

	InformationContentDescriptor icd = new InformationContentDescriptor();

	WalkandPathCountDescriptors wapcd = new WalkandPathCountDescriptors();

	GetawayDescriptors gd = new GetawayDescriptors();

	WHIMDescriptors wd = new WHIMDescriptors();

	TwoDAutoCorrelationDescriptors tdacd = new TwoDAutoCorrelationDescriptors();

	ConstitutionalDescriptors Con = new ConstitutionalDescriptors();

	RandicMolecularProfileDescriptors rmpd = new RandicMolecularProfileDescriptors();

	EState estate = new EState();

	PrincipalMomentOfInertiaDescriptors pmoid = new PrincipalMomentOfInertiaDescriptors();

	MolecularPropertyDescriptors mpd = new MolecularPropertyDescriptors();

	BurdenEigenValueDescriptors bevd = new BurdenEigenValueDescriptors();

	MomentOfInertiaDescriptor mid = new MomentOfInertiaDescriptor();

	public ALOGP alogp = new ALOGP();

	public MLOGP mlogp = new MLOGP();
	// KowWin2 kw=new KowWin2();

	KowDLL kd;

	XLogPDescriptor xlpd = new XLogPDescriptor();

	// needed intermediate values:
	// public IRingSet rs = null;
	// public IRingSet rs2 = null;//ringset with hydrogens in molecule

	IRingSet srs = null;

	double[] D = null;

	double[] DV = null;

	double[] DV2 = null;

	double[] S = null;

	double[] SV = null;

	double[] IS = null; // Intrinsic state

	int[][] Distance = null; // topological distance matrix

	int[] vdd = null; // vertex distance degrees - see pg 113 todeschini book

	double[] EState = null; // E-state for each atom

	double[] HEState = null; // H E-state for each atom

	public String[] Fragment = null; // E-state fragment assigned to each atom

	double[] KHE = null; // kier hall

	LinkedList[] paths; // path list - array of lists

	DecimalFormat df = new DecimalFormat("0.000");

	boolean isStandAlone = false;
	public boolean Calculate3DDescriptors = true;

	public IAtomContainer processedMolecule;

	public DescriptorFactory(boolean isStandAlone) {

		this.isStandAlone = isStandAlone;

		if (isStandAlone) {
			kd = new KowDLL();
		}

	}

	public int CalculateDescriptors(IAtomContainer m, DescriptorData dd, boolean FindFragments) {
		return this.CalculateDescriptors(m, dd, false, false, FindFragments, "");
	}

	private IAtomContainer Create_molecule_with_Hydrogens(IAtomContainer m) {
		//

		IAtomContainer m2 = null;

		try {
			m2 = (IAtomContainer) m.clone();

			Isotopes isf = Isotopes.getInstance();

			for (int i = 0; i < m.getAtomCount(); i++) {
				IIsotope isotope = isf.getMajorIsotope("H");
				IAtom atom = m2.getAtom(i);

				for (int j = 1; j <= m.getAtom(i).getImplicitHydrogenCount(); j++) {
					Atom hydrogen = new Atom("H");
					isf.configure(hydrogen, isotope);
					m2.addAtom(hydrogen);

					// System.out.println(m2.getAtomNumber(hydrogen));

					Bond newBond = new Bond(atom, hydrogen, IBond.Order.SINGLE);
					m2.addBond(newBond);
				}

			}

			// for (int i=0;i<m2.getAtomCount();i++) {
			// System.out.println(i+"\t"+m2.getAtom(i).getSymbol());
			// }

		} catch (Exception e) {
			logger.catching(e);
		}
		return m2;
	}

	/**
	 * 
	 * @param m
	 * @param dd
	 * @param WriteResultsToFile
	 * @param FindFragments
	 * @param OutputFolder
	 * @return
	 * 
	 * 		TODO: add different error codes for each method so can better
	 *         output error to output file
	 * 
	 */
	public int CalculateDescriptors(IAtomContainer m, DescriptorData dd, boolean WriteResultsToFile,
			boolean overwriteFiles, boolean FindFragments, String OutputFolder) {
		// this.m = m;
		this.dd = dd;
		this.errorMsg = "";
		this.done = false;

		IAtomContainer m3d = null;

		// System.out.println("enter calculate descriptors");

		// TODO: add code to catch errors when calculating descriptors
		// int IERRCODE = 0;

		// Normalize before structure image gen:
		this.Normalize(m);

		// System.out.println("here");

		double time1 = System.currentTimeMillis() / 1000.0;


		// clone it:
		try {
			m3d = m.clone();
		} catch (Exception e) {
			logger.catching(e);
			errorMsg = "clone";
		}

		// remove hydrogens from molecule:
		m = (IAtomContainer) AtomContainerManipulator.removeHydrogens(m);// remove
																			// all
																			// hydrogens
																			// first
																			// so
																			// they
																			// all
																			// get
																			// added
																			// to
																			// end

		// IAtomContainer m2=this.Create_molecule_with_Hydrogens(m);
		IAtomContainer m2 = CDKUtilities.addHydrogens(m);

		if (this.done || !errorMsg.equals(""))
			return -1;

		// Dimension all arrays:
		this.DimensionArrays(m);
		if (this.done || !errorMsg.equals(""))
			return -1;

		//TMM-1/10/2018
		for (int i=0;i<m.getAtomCount();i++) {
	        if (m.getAtom(i).getImplicitHydrogenCount()==null) {
	            throw new RuntimeException("Implicit hydrogen count is null");
	        }
		}
		
		// ----------------------------------------------------------------
		// Preliminary Calculations
		// find all the rings (well actually cycles)
		IRingSet rs = FindRings(m);
	
		
		if (this.done || !errorMsg.equals(""))
			return -1;

		IRingSet rs2 = FindRings(m2);

		if (this.done || !errorMsg.equals(""))
			return -1;

		// System.out.print("Finding paths...");

		double timeaa = System.currentTimeMillis() / 1000.0;
		// find all paths in the molecule
		FindPaths(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		double timebb = System.currentTimeMillis() / 1000.0;
		// System.out.print("done\t"+(timebb-timeaa)+"\r\n");

		// double timecc = System.currentTimeMillis() / 1000.0;
		// System.out.print("Calculating new chi descriptors...");
		// org.openscience.cdk.qsar.descriptors.molecular.ChiChainDescriptor
		// ccd=new
		// org.openscience.cdk.qsar.descriptors.molecular.ChiChainDescriptor();
		// ccd.calculate(m);
		// double timedd = System.currentTimeMillis() / 1000.0;
		// System.out.print("done\t"+(timedd-timecc)+"\r\n");

		// // calculate distance matrix
		PathFinder.CalculateDistanceMatrix(m, Distance);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate vertex distance degrees
		PathFinder.CalculateVertexDistanceDegrees(m, Distance, vdd);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // detect aromaticity of rings and atoms:
		DetectAromaticityOld(m, rs);
		DetectAromaticityOld(m2, rs2);

		// DetectAromaticity(m,rs);
		// DetectAromaticity(m2,rs2);

		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate Estates
		CalculateEStates(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		// // ----------------------------------------------------------------
		// // begin descriptor calcs
		//
		// // calculate kier hall chi descriptors:
		CalculateChiDescriptors(m, rs);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//

		// // assign estate fragments to each atom:
		CalculateEStateFragments(m, rs);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate Kappa and kappa alpha descriptors
		CalculateKappaDescriptors(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // determine our fragments:
		if (FindFragments)
			CalculateFragments(m, m2, rs, rs2);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate 2 dimensional descriptors:
		Calculate2DDescriptors(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate topological descriptors:
		CalculateTopologicalDescriptors(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate information content descriptors:
		CalculateInformationContentDescriptors(m, rs);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate constitutional descriptors:
		CalculateConstitutionalDescriptors(m, rs);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate walk and path count descriptors:
		CalculateWalkAndPathCountDescriptors(m, rs);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // ADAPT's Molecular Distance Edge Descriptors();
		CalculateMolecularDistanceEdgeDescriptors(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate 2d autocorrelation descriptors:
		Calculate2DAutocorrelationDescriptors(m);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		//
		// // calculate Burden eigenvalue descriptors:
		CalculateBurdenEigenvalueDescriptors(m2, rs2);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate ALOGP:
		alogp.Calculate(m, dd, Fragment, rs);
		if (this.done || !errorMsg.equals(""))
			return -1;

		// // calculate MLOGP:
		// mlogp.Calculate(m,dd,Fragment,rs);
		// if (this.done || !errorMsg.equals("")) return -1;
		//
		//
		// if (this.done) return -1;
		//
		// calculate molecular property descriptors:
		mpd.Calculate(m, dd, Fragment);
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		// // calculate kowwin:
		// this.CalculateKowWin();
		//
		// if (this.done) return -1;
		//
		if (isStandAlone) {
			this.CalculateKOWWIN_From_DLL(m);
		}
		if (this.done || !errorMsg.equals(""))
			return -1;
		//
		//
		this.CalculateXLogP(m2, rs2);// new code adds explicit hs

		// System.out.println(dd.XLOGP);

		// if (this.done || !errorMsg.equals("")) return -1;
		//
		//
		// if (this.Calculate3DDescriptors) {
		//
		// wd.Calculate(m3d, dd, EState);
		//
		// if (this.done) return -1;
		//
		// gd.Calculate(m3d, dd);
		//
		// if (this.done) return -1;
		//
		// rmpd.Calculate(m3d, dd);
		//
		// if (this.done) return -1;
		//
		// this.CalculateMomentOfInertia(m3d, dd);
		// }
		//
		//
		// // ----------------------------------------------------------------
		//
		// double time2 = System.currentTimeMillis() / 1000.0;
		//
		// if (debug)
		// System.out.println("Total computation time=" + (time2 - time1));
		//
		// if (this.done) return -1;
		//
		
		
		if (WriteResultsToFile) {
			
			try {
				Path pathStructureData = Paths.get(OutputFolder);
				Files.createDirectories(pathStructureData);
			} catch (Exception ex) {
				logger.error("Error creating "+OutputFolder);					
			}
						
			File fileStructure = new File(OutputFolder + File.separator + "structure" + ".png");
			
			if (overwriteFiles || !fileStructure.exists()) {
				ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m, "structure", OutputFolder);
				ToxPredictor.Utilities.SaveStructureToFile.CreateImageFileWithNumbers(m, "numberedstructure",
						OutputFolder);

				String outputFilePath = OutputFolder + File.separator + dd.ID + ".mol";
				MolFileUtilities.WriteMolFile(m, dd.ID, outputFilePath, true);

				dd.WriteToFileHTML(OutputFolder,"DescriptorData_"+dd.ID+".html");
				dd.WriteToFileTEXT(OutputFolder, "|");
				dd.WriteToFileJSON(OutputFolder, true);
				this.WriteEStates(OutputFolder, m, Fragment, S, SV);
				this.WriteAssignedFragments(OutputFolder, m, dd);
			}
			
		}

		return 0;

	}
	
	

	private boolean HasHydrogensInCa(IAtomContainer mol) {

		for (int i = 0; i < mol.getAtomCount(); i++) {
			List ca = mol.getConnectedAtomsList(mol.getAtom(i));
			for (int j = 0; j < ca.size(); j++) {
				IAtom caj = (IAtom) ca.get(j);
				if (caj.getSymbol().equals("H")) {
					System.out.println("--Found hydrogens in ca");
					return true;
				}
			}

		}

		return false;

	}

	public void Normalize(IAtomContainer m) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			String DataFile = "fixsmiles.xml";
			InputStream ins = this.getClass().getClassLoader().getResourceAsStream(DataFile);

			Document d = db.parse(ins);

			Normalizer.normalize(m, d);
		} catch (Exception e) {
			logger.catching(e);
			this.errorMsg = "normalize";
		}
	}

	private void CalculateMomentOfInertia(IAtomContainer m3d, DescriptorData dd) {
		try {

			DescriptorValue dv = mid.calculate(m3d); //

			DoubleArrayResult dar = (DoubleArrayResult) dv.getValue();

			for (int i = 0; i <= dar.length() - 1; i++) {
				Field myField = dd.getClass().getField("MOMI" + (i + 1));
				myField.setDouble(dd, dar.get(i));
				// System.out.println("MOMI"+(i+1)+"\t"+dar.get(i));
			}

			// mid.calculate(m3d, dd);
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void CalculateXLogP(IAtomContainer m2, IRingSet rs2) {
		try {
			// convert nitro groups to uncharged versions for XLOGP to work
			// right:
			// this.FixNitroGroups2(m2);//TODO- doesnt seem affect new xlogp in
			// CDK1.5
			xlpd.setCheckAromaticity(false);
			xlpd.setSalicylFlag(false);

			// DescriptorValue dv = this.xlpd.calculate(m2);
			DescriptorValue dv = this.xlpd.calculate(m2, rs2);
			DoubleResult dr = (DoubleResult) dv.getValue();
			dd.XLOGP = dr.doubleValue();
			dd.XLOGP2 = dd.XLOGP * dd.XLOGP;

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void CalculateConstitutionalDescriptors(IAtomContainer m, IRingSet rs) {
		if (debug)
			System.out.print("Calculating Constitutional Descriptors...");
		double time3 = System.currentTimeMillis() / 1000.0;

		Con.Calculate(m, dd, EState, rs, srs);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	private void CalculateBurdenEigenvalueDescriptors(IAtomContainer m2, IRingSet rs2) {
		if (debug)
			System.out.print("Calculating BurdenEigenvalueDescriptors...");
		double time3 = System.currentTimeMillis() / 1000.0;
		bevd.Calculate(m2, rs2, dd);
		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

		// System.out.println("done in DF = "+this.done);
	}

	private void CalculateKOWWIN_From_DLL(IAtomContainer m) {
		SmilesGenerator sg = new SmilesGenerator();
		Lookup lookup = new Lookup();

		if (debug)
			System.out.print("Calculating KOWWIN from DLL...");
		double time3 = System.currentTimeMillis() / 1000.0;

		String smiles = "";

		if (dd.ID instanceof String) {
			// first try to look up the smiles from the database:
			smiles = lookup.LookUpSmiles(dd.ID);

			// System.out.println(smiles);
		}

		// if dont have in the database generate the smiles using CDK:

		// smiles="missing";

		if (smiles.equals("missing")) {
			smiles = sg.createSMILES(m);
			// System.out.println("missing so created for "+dd.CAS);
		}
		// System.out.println(dd.CAS+"\t"+smiles);

		dd.KLOGP = kd.Calculate(smiles);
		dd.KLOGP2 = dd.KLOGP * dd.KLOGP;

		// System.out.println("dd.KLOGP="+dd.KLOGP);
		// System.out.println("LogPexp="+kd.KowExp);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

	}

	private void CalculateKOWWIN_From_DLL(String smiles) {

		if (debug)
			System.out.print("Calculating KOWWIN from DLL...");
		double time3 = System.currentTimeMillis() / 1000.0;

		dd.KLOGP = kd.Calculate(smiles);
		dd.KLOGP2 = dd.KLOGP * dd.KLOGP;

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

	}

	/**
	 * @param m
	 * @param dd
	 */
	private void Calculate2DAutocorrelationDescriptors(IAtomContainer m) {
		if (debug)
			System.out.print("Calculating 2DAutocorrelationDescriptors...");
		double time3 = System.currentTimeMillis() / 1000.0;
		tdacd.Calculate(m, dd, Distance);
		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

	}

	private void CalculateFragments(IAtomContainer m, IAtomContainer m2, IRingSet rs, IRingSet rs2) {

		if (debug)
			System.out.print("Calculating fragments...");

		double time3 = System.currentTimeMillis() / 1000.0;

		// determine molecular fragments:

		dd.FragmentList = moleculefragmenter.Calculate(m, m2, rs, rs2, dd, Fragment);
		// dd.FragmentList = moleculefragmenter.fragmentMolecule(m);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

		/*
		 * String fragnames=(String)dd.FragmentList.get(0); List
		 * namelist=Utilities.Parse(fragnames,"|");
		 * 
		 * for (int i=0;i<=namelist.size()-1;i++) { Integer
		 * fragval=(Integer)dd.FragmentList.get(i+1); int
		 * fragval2=fragval.intValue(); if (fragval2>0) {
		 * System.out.println(namelist.get(i)+"\t"+fragval2); } }
		 */

	}

	// private void CalculateKappa0() {
	// if (debug)
	// System.out.print("Calculating Kappa0...");
	// double time3 = System.currentTimeMillis() / 1000.0;
	//
	// // calculate Kappa0 using SV topological states of each atom:
	// Kappa0Descriptor2.Calculate(m, SV, dd);
	//
	// double time4 = System.currentTimeMillis() / 1000.0;
	//
	// if (debug)
	// System.out.println("done-" + df.format(time4 - time3) + " secs");
	// }

	/**
	 * @param m
	 * @param dd
	 */
	private void CalculateMolecularDistanceEdgeDescriptors(IAtomContainer m) {
		// -----------------------------------------------------------------
		// calculate Molecular distance edge descriptors:
		// MolecularDistanceEdgeDescriptors.Calculate(m,dd,Fragment,Distance);

		if (debug)
			System.out.print("Calculating MolecularDistanceEdgeDescriptors...");
		double time3 = System.currentTimeMillis() / 1000.0;

		MolecularDistanceEdgeDescriptors.Calculate2(m, dd, Distance);
		// MolecularDistanceEdgeDescriptors.Calculate(m, dd,Fragment,Distance);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	private void CalculateWalkAndPathCountDescriptors(IAtomContainer m, IRingSet rs) {
		if (debug)
			System.out.print("Calculating walk and path count descriptors...");
		double time3 = System.currentTimeMillis() / 1000.0;
		wapcd.Calculate(m, dd, paths, D, vdd, rs);
		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	private void CalculateInformationContentDescriptors(IAtomContainer m, IRingSet rs) {
		if (debug)
			System.out.print("Calculating information content descriptors...");

		double time3 = System.currentTimeMillis() / 1000.0;

		icd.Calculate(m, dd, Distance, rs, paths, D, DV, S, SV, vdd);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	private void CalculateTopologicalDescriptors(IAtomContainer m) {

		if (debug)
			System.out.print("Calculating topological descriptors...");
		double time3 = System.currentTimeMillis() / 1000.0;

		// calculate topological descriptors:
		td.Calculate(m, dd, D, DV, vdd, Distance, EState, IS);
		double time4 = System.currentTimeMillis() / 1000.0;
		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

	}

	private void Calculate2DDescriptors(IAtomContainer m) {
		if (debug)
			System.out.print("Calculating 2D descriptors...");

		double time3 = System.currentTimeMillis() / 1000.0;

		twod.calculate(m, dd, IS, EState, HEState, Fragment);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	private void CalculateKappaDescriptors(IAtomContainer m) {
		if (debug)
			System.out.print("Calculating kappa descriptors...");

		double time3 = System.currentTimeMillis() / 1000.0;

		// calculate Kappa descriptors:
		KappaDescriptor.Calculate(m, paths, dd);

		// calculate Kappa alpha descripors:
		KappaAlphaDescriptor.Calculate(m, paths, dd);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	/**
	 * @param m
	 * @param dd
	 */
	private void CalculateEStateFragments(IAtomContainer m, IRingSet rs) {

		if (debug)
			System.out.print("Calculating fragment descriptors...");

		double time3 = System.currentTimeMillis() / 1000.0;

		// calculates fragment estates and assigns fragments to each atom
		esfd.Calculate(m, dd, EState, HEState, rs, Fragment);

		for (int i = 0; i <= m.getAtomCount() - 1; i++) {
			if (!(Fragment[i] instanceof String)) {
				System.out.print(dd.ID + ":");
				System.out.print("Atom " + (i + 1) + " has no fragment assigned\n");
				UnassignedAtoms = true;
			}
		}

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	/**
	 * @param m
	 * @param dd
	 */
	private void CalculateChiDescriptors(IAtomContainer m, IRingSet rs) {
		if (debug)
			System.out.print("Calculating chi descriptors...");

		double time3 = System.currentTimeMillis() / 1000.0;

		// calculate chi descriptors:
		cd.Calculate(m, dd, D, DV, paths, rs);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	// private void CalculateKowWin() {
	// if (debug)
	// System.out.print("Calculating Kowwin...");
	//
	// double time3 = System.currentTimeMillis() / 1000.0;
	//
	// this.kw.Calculate(m,dd,Fragment,rs);
	//
	// double time4 = System.currentTimeMillis() / 1000.0;
	//
	// if (debug)
	// System.out.println("done-" + df.format(time4 - time3) + " secs");
	// }

	/**
	 * @param m
	 */
	private void CalculateEStates(IAtomContainer m) {

		UnassignedAtoms = false;

		if (debug)
			System.out.print("Calculating estates...");

		double time3 = System.currentTimeMillis() / 1000.0;

		// calculate D,DV,Distance matrix, Intrinsic states, Estates:
		estate.Calculate(m, D, DV, DV2, KHE, Distance, IS, EState, HEState);

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");
	}

	private void DimensionArrays(IAtomContainer m) {
		D = new double[m.getAtomCount()];
		DV = new double[m.getAtomCount()];
		DV2 = new double[m.getAtomCount()];
		S = new double[m.getAtomCount()];
		SV = new double[m.getAtomCount()];
		IS = new double[m.getAtomCount()];
		Distance = new int[m.getAtomCount()][m.getAtomCount()];
		vdd = new int[m.getAtomCount()];
		EState = new double[m.getAtomCount()];
		HEState = new double[m.getAtomCount()];
		Fragment = new String[m.getAtomCount()];
		KHE = new double[m.getAtomCount()];
		dd.FragmentList = null;
	}

	/**
	 * @param m
	 * @param rs
	 */
	public void DetectAromaticityOld(IAtomContainer m, IRingSet rs) {
		double time3 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.print("Finding aromaticity...");

		try {

			// for (int i=0;i<=m.getAtomCount()-1;i++) {
			// m.getAtom(i).setFlag(CDKConstants.ISAROMATIC,false);
			// }
			//
			// for (int i=0;i<=rs.getAtomContainerCount()-1;i++) {
			// Ring r=(Ring)rs.getAtomContainer(i);
			// r.setFlag(CDKConstants.ISAROMATIC,false);
			// }

			// do it multiple times to make sure it gets it right:
			for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {
				HueckelAromaticityDetector.detectAromaticity(m, rs, false);
			}

			// for (int i=0;i<=rs.getAtomContainerCount()-1;i++) {
			// Ring r=(Ring)rs.getAtomContainer(i);
			// System.out.println("Ring size = "+r.getAtomCount()+", aromaticity
			// = "+r.getFlag(CDKConstants.ISAROMATIC));
			// }

			// for (int i=0;i<=m.getAtomCount()-1;i++) {
			// System.out.println(m.getAtom(i).getFlag(CDKConstants.ISAROMATIC));
			// }

			// figure out which atoms are in aromatic rings:
			// HueckelAromaticityDetector.detectAromaticity(m,true);
			// figure out which rings are aromatic:
			// HueckelAromaticityDetector.detectAromaticity(m, rs,true);

		} catch (Exception e) {
			logger.catching(e);
			errorMsg = "DetectAromaticity";
		}

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

	}

	/**
	 * @param m
	 * @param rs
	 * 
	 */

	// just in case set all flags to false:
	// for (int i=0;i<m.getAtomCount();i++) {
	// m.getAtom(i).setFlag(CDKConstants.ISAROMATIC,false);
	// }

	// for (int i=0;i<rs.getAtomContainerCount();i++) {
	// Ring r=(Ring)rs.getAtomContainer(i);
	// r.setFlag(CDKConstants.ISAROMATIC,false);
	// }

	public void DetectAromaticity(IAtomContainer m, IRingSet rs) {

		/*
		 * http://cdk.github.io/cdk/2.0/docs/api/org/openscience/cdk/aromaticity
		 * /Aromaticity.html
		 * 
		 * A configurable model to perceive aromatic systems. Aromaticity is
		 * useful as both a chemical property indicating stronger stabilisation
		 * and as a way to treat different resonance forms as equivalent. Each
		 * has its own implications the first in physicochemical attributes and
		 * the second in similarity, depiction and storage. To address the
		 * resonance forms, several simplified (sometimes conflicting) models
		 * have arisen. Generally the models loosely follow Hückel's rule for
		 * determining aromaticity. A common omission being that planarity is
		 * not tested and chemical compounds which are non-planar can be
		 * perceived as aromatic. An example of one such compound is,
		 * cyclodeca-1,3,5,7,9-pentaene. Although there is not a single
		 * universally accepted model there are models which may better suited
		 * for a specific use (Cheminformatics Toolkits: A Personal Perspective,
		 * Roger Sayle). The different models are often ill-defined or
		 * unpublished but it is important to acknowledge that there are
		 * differences (see. Aromaticity Perception Differences, Blue Obelisk).
		 * Although models may get more complicated (e.g. considering tautomers)
		 * normally the reasons for differences are:
		 * 
		 * the atoms allowed and how many electrons each contributes the
		 * rings/cycles are tested
		 * 
		 * This implementation allows configuration of these via an
		 * ElectronDonation model and CycleFinder. To obtain an instance of the
		 * electron donation model use one of the factory methods,
		 * ElectronDonation.cdk(), ElectronDonation.cdkAllowingExocyclic(),
		 * ElectronDonation.daylight() or ElectronDonation.piBonds().
		 * 
		 * TODO- see if other ElectronDonation models work better (i.e. piBonds)
		 * 
		 * 
		 * // mimics the old CDKHuckelAromaticityDetector which uses the CDK
		 * atom types ElectronDonation model = ElectronDonation.cdk();
		 * CycleFinder cycles = Cycles.cdkAromaticSet(); Aromaticity aromaticity
		 * = new Aromaticity(model, cycles);
		 * 
		 * // apply our configured model to each molecule, the CDK model //
		 * requires that atom types are perceived for (IAtomContainer molecule :
		 * molecules) {
		 * AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(
		 * molecule); aromaticity.apply(molecule); }
		 * 
		 */

		double time3 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.print("Finding aromaticity...");

		try {

			// ********************************************************************************************

			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(m);

			ElectronDonation model = ElectronDonation.cdk();
			// ElectronDonation model = ElectronDonation.piBonds();

			CycleFinder cycles = Cycles.cdkAromaticSet();

			// Aromaticity aromaticity = new Aromaticity(model, cycles);
			// Aromaticity aromaticity=new Aromaticity(ElectronDonation.cdk(),
			// Cycles.or(Cycles.all(), Cycles.relevant()));

			// aromaticity.apply(m);

			// For now use the legacy code:
			Aromaticity.cdkLegacy().apply(m);// TODO compare legacy method to
												// new method

			// ********************************************************************************************
			// if all atoms are aromatic, set aromaticity of ring to true:
			for (int i = 0; i < rs.getAtomContainerCount(); i++) {
				Ring r = (Ring) rs.getAtomContainer(i);

				boolean allAromatic = true;

				for (int j = 0; j < r.getAtomCount(); j++) {
					Atom atomj = (Atom) r.getAtom(j);
					if (!atomj.isAromatic()) {
						allAromatic = false;
						break;
					}
				}
				r.setFlag(CDKConstants.ISAROMATIC, allAromatic);

				// aromaticity.apply(r);
				// Aromaticity.cdkLegacy().apply(r);
				// System.out.println("Ring size = "+r.getAtomCount()+",
				// aromaticity =
				// "+r.getFlag(CDKConstants.ISAROMATIC)+"\t"+allAromatic);
			}

			// ********************************************************************************************
			// set bonds to be aromatic if both atoms are aromatic and in same
			// ring:

			for (int i = 0; i < m.getBondCount(); i++) {
				IBond bondi = m.getBond(i);

				IAtom atom0 = bondi.getAtom(0);
				IAtom atom1 = bondi.getAtom(1);

				if (atom0.getFlag(CDKConstants.ISAROMATIC) && atom1.getFlag(CDKConstants.ISAROMATIC)) {
					// check if in same ring:
					boolean SameRing = EStateFragmentDescriptor.InSameAromaticRing(m, atom0, atom1, rs);

					if (SameRing) {
						// System.out.println(i+"\t"+bondi.getFlag(CDKConstants.ISAROMATIC));
						bondi.setFlag(CDKConstants.ISAROMATIC, true);
					}
				}
			}

			// int count=0;
			// for (int i=0;i<m.getAtomCount();i++) {
			// if (m.getAtom(i).getFlag(CDKConstants.ISAROMATIC) &&
			// m.getAtom(i).getSymbol().equals("C")) {
			// count++;
			// }
			// System.out.println(m.getAtom(i).getSymbol()+"\t"+m.getAtom(i).getFlag(CDKConstants.ISAROMATIC));
			// }
			// System.out.println("aromaticCarbonCount="+count);

		} catch (Exception e) {
			logger.catching(e);
			errorMsg = "DetectAromaticity";
		}

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

	}

	/**
	 * @param m
	 * @param rs
	 * @return
	 */
	private IRingSet FindRings(IAtomContainer m) {

		IRingSet ringSet = null;

		double time3 = System.currentTimeMillis() / 1000.0;
		if (debug)
			System.out.print("Finding rings...");

		try {
			AllRingsFinder arf = new AllRingsFinder();

			arf.usingThreshold(AllRingsFinder.Threshold.PubChem_994);

			// arf.setTimeout(100000);
			// TODO: set the threshold instead of timeout
			ringSet = arf.findAllRings(m);

			// ringSet = Cycles.sssr(m).toRingSet();//TODO how does compare to
			// method above???
		} catch (Exception e) {
			this.errorMsg = "Timeout while finding rings";
			logger.catching(e);
		}
		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.println("done-" + df.format(time4 - time3) + " secs");

		return ringSet;

	}

	private void FindPaths(IAtomContainer m) {
		// find all paths up to length 10:

		// TODO add code to figure out if it will run out of memory

		double time3 = System.currentTimeMillis() / 1000.0;

		if (debug)
			System.out.print("Finding paths...");

		// paths = PathFinder.FindPaths3(10, m);
		paths = PathFinder.FindPaths4(10, m);

		// System.out.println(paths.length);

		// if (paths==null) done=true;

		if (paths == null) {
			errorMsg = "FindPaths";
		}

		double time4 = System.currentTimeMillis() / 1000.0;

		if (debug) {
			System.out.println("done-" + df.format(time4 - time3) + " secs");
		}

	}

	private void WriteEStates(String OutputFolder, IAtomContainer m, String[] Fragment, double[] S, double[] SV) {

		// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(this.m,
		// "numberedstructure", "ToxPredictor/temp", true, true, false);
		// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m,
		// "structure", "ToxPredictor/temp", false, true, true);

		try {

			if (estate.D == null)
				return;

			DecimalFormat d4 = new DecimalFormat("0.0000");

			FileWriter fw = new FileWriter(OutputFolder + File.separator + "Estates.html");
			fw.write("<html>\n");

			fw.write("<table>\n");
			fw.write("<tr>\n");

			fw.write("<td>\n");
			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("\t<th>Atom</th>\n");
			fw.write("\t<th>Symbol</th>\n");
			fw.write("\t<th>Fragment</th>\n");
			fw.write("\t<th>Delta</th>\n");
			fw.write("\t<th>DeltaV</th>\n");
			fw.write("\t<th>Intrinsic State</th>\n");
			fw.write("\t<th>Estate</th>\n");
			fw.write("\t<th>HEstate</th>\n");
			fw.write("\t<th>S</th>\n");
			fw.write("\t<th>SV</th>\n");

			fw.write("</tr>\n");

			boolean HaveNonStandardFragment = false;

			for (int i = 0; i <= m.getAtomCount() - 1; i++) {
				fw.write("<tr>\n");
				fw.write("\t<td>" + (i + 1) + "</td>\n");
				fw.write("\t<td>" + m.getAtom(i).getSymbol() + "</td>\n");
				fw.write("\t<td>" + Fragment[i] + "</td>\n");

				if (Fragment[i].indexOf("*") > -1)
					HaveNonStandardFragment = true;

				fw.write("\t<td>" + d4.format(estate.D[i]) + "</td>\n");
				fw.write("\t<td>" + d4.format(estate.DV[i]) + "</td>\n");
				fw.write("\t<td>" + d4.format(estate.IS[i]) + "</td>\n");
				fw.write("\t<td>" + d4.format(estate.EState[i]) + "</td>\n");
				fw.write("\t<td>" + d4.format(estate.HEState[i]) + "</td>\n");
				fw.write("\t<td>" + d4.format(S[i]) + "</td>\n");
				fw.write("\t<td>" + d4.format(SV[i]) + "</td>\n");
				fw.write("<tr>\n");
			}

			fw.write("</table>\n");

			if (HaveNonStandardFragment) {
				fw.write("*Estate fragment which is not included in the set of descriptors");
			}

			fw.write("</td>\n");

			fw.write("<td>\n");
			fw.write("<img src=\"numberedstructure.png\">\n");
			fw.write("</td>\n");

			fw.write("</tr>\n");
			fw.write("</table>\n");

			fw.write("</html>\n");
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private void WriteAssignedFragments(String OutputFolder, IAtomContainer m, DescriptorData dd) {

		// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(this.m,
		// "numberedstructure", "ToxPredictor/temp", true, true, false);
		// ToxPredictor.Utilities.SaveStructureToFile.CreateImageFile(m,
		// "structure", "ToxPredictor/temp", false, true, true);

		try {

			if (moleculefragmenter.AssignedFragment == null)
				return;

			DecimalFormat d4 = new DecimalFormat("0.0000");

			FileWriter fw = new FileWriter(OutputFolder + File.separator + "AssignedFragments.html");
			fw.write("<html>\n");

			fw.write("<table>\n");
			fw.write("<tr>\n");

			fw.write("<td>\n");
			fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\n");

			fw.write("<tr bgcolor=\"#D3D3D3\">\n");
			fw.write("\t<th>Atom</th>\n");
			fw.write("\t<th>Symbol</th>\n");
			fw.write("\t<th>Assigned Fragment</th>\n");
			fw.write("</tr>\n");

			for (int i = 0; i <= m.getAtomCount() - 1; i++) {
				fw.write("<tr>\n");
				fw.write("\t<td>" + (i + 1) + "</td>\n");
				fw.write("\t<td>" + m.getAtom(i).getSymbol() + "</td>\n");

				String fragment = moleculefragmenter.AssignedFragment[i];

				if (fragment.equals("")) {
					fragment = "<font color=darkred>Unassigned</font>";
				}

				fw.write("\t<td>" + fragment + "</td>\n");
				fw.write("</tr>\n");
			}

			fw.write("</table>\n");
			fw.write("</td>\n");

			fw.write("<td>\n");
			fw.write("<img src=\"numberedstructure.png\">\n");
			fw.write("</td>\n");

			fw.write("</tr>\n");
			fw.write("</table>\n");

			fw.write("</html>\n");
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public static void main(String[] args) {

		DescriptorFactory DF = new DescriptorFactory(false);

		int mode = 1;

		String chemname = "";
		String delimiter = ",";
		DF.Calculate3DDescriptors = false;

		try {
			DF.debug = false;
			HueckelAromaticityDetector.debug=false;
			
			if (mode == 1) { // read from smiles string
				// SmilesParser sp = new SmilesParser();

				SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());

				String smiles = "";

				smiles = "CCOS(=O)(=O)OCC";

				// smiles="O=n1ccccc1";// pyridine-N-oxide, neutral
				// representation
				// smiles="[O-][n+]1ccccc1";//pyridine-N-oxide, charge-separated
				// representation"

				// smiles="O=N(=O)c(cccc1)c1"; // nitrobz
				// smiles="O=[N+]([O-])c1ccccc1";
				// smiles="OCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCO";
				// smiles="c1ccncc1";//pyridine
				// smiles="c1ncncc1";

//				smiles = "c1ccccc1";
				// smiles="CCCCCCCCCCCCc2cc5c1c(ccc3c1c2c1ccc4c2c1c3ccc2C(=O)c1ccccc41)c1ccccc1C5=O";
				// smiles="COc7ccc(N4C(=O)c5ccc6c1ccc3c2c1c(ccc2C(=O)N(c1ccc(OC)cc1)C3=O)c1ccc(C4=O)c5c61)cc7";

				AtomContainer m = (AtomContainer) sp.parseSmiles(smiles);

				// System.out.println(DescriptorFactory.calculateMolecularWeight(m));

				MolecularFormula mf = (MolecularFormula) MolecularFormulaManipulator.getMolecularFormula(m);
				String formula = MolecularFormulaManipulator.getString(mf);

				System.out.println(formula);

				DescriptorData dd = new DescriptorData();
				dd.molname = chemname;
				dd.ThreeD = false;

//				DF.Calculate3DDescriptors = false;
//				DF.CalculateDescriptors(m, dd, true, true, true, "ToxPredictor/temp");
//				DF.WriteOut2DDescriptors(m, dd, "Todd/temp", "|");
//				File myFile = new File("ToxPredictor/temp/DescriptorData.html");
//				BrowserLauncher.openURL(myFile.toURL().toString());
				
				
				DF.CalculateDescriptors(m, dd, true);
				DF.WriteJSON(dd, "Todd/temp/smiles.json",false);
				


				return;

			} else if (mode == 2) { // read from mol file

				DescriptorData dd = new DescriptorData();

				String folder = "ToxPredictor/temp";

				dd.ID = "67-68-5";
				// dd.CAS="bz_aromatic";
				// dd.CAS="613-91-2";
				// dd.CAS="613-92-3";
				// dd.CAS="2278-50-4";

				String filepath = folder + "/" + dd.ID + ".mol";

				// filepath="//Aa.ad.epa.gov/ord/CIN/Users/main/Q-Z/TMARTI02/Net
				// MyDocuments/55-63-0.mol";

				// System.out.println(filepath);

				AtomContainer m = new AtomContainer();
				MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(filepath));
				mr.read(m);
				mr.close();

				int result = DF.CalculateDescriptors(m, dd, true, true, true, folder);

				System.out.println(dd.ID + "\t" + result + "\t" + DF.errorMsg);

				// displaySubstructure ds=new displaySubstructure(m);

				// SaveStructureToFile.CreateImageFile(m, "afterfix",
				// "ToxPredictor/temp/struct/", true, false, false);

				// System.out.println(chemname);
				return;

				// System.out.println(m.getAtom(1).getX3d());
			} else if (mode == 3) {

				// String sdffile="ToxPredictor/SDF Files/fixed3d.sdf";
				// String folder="ToxPredictor/DescriptorTextTables/";

				// String folder="ToxPredictor/DescriptorTextTables/BP_final5
				// data files";
				// String sdffile=folder+"/BP_overall.sdf";

				String folder = "ToxPredictor/test descriptors";
				String sdffile = "ValidatedStructures2d.sdf";
				DF.LookAtDescriptorsInSDF(folder + "/" + sdffile, folder + "/ValidatedStructures.txt", 0, 999999);

				// DF.CreateDescriptorTableTextFileFromSDF(0, 99999,
				// sdffile,folder+"/data2D.txt","2D",delimiter,false,null);
				// DF.CreateDescriptorTableTextFileFromSDF(0, 3060,
				// sdffile,folder+"data3D.txt","3D",delimiter,false,null);
				// DF.CreateDescriptorTableTextFileFromSDF(0, 3060,
				// sdffile,folder+"dataFrag.txt","Frag",delimiter,true,"ToxPredictor/Check/Fragments.html");
				// DF.CreateKOWWINTableFromSDFFile(0,3060,"ToxPredictor/SDF
				// Files/fixed3d.sdf","ToxPredictor/DescriptorTextTables/dataKLOGP.txt",delimiter);
				//
				// CombineDataTextFiles c=new CombineDataTextFiles();
				// c.CreateDescriptorTable();
				// c.CheckFile(folder+"AllDescriptorValues.txt",",");

				// System.out.println(Lookup.LookUpSmiles("002173-57-1"));

				return;

			} else if (mode == 4) {
				String folder = "ToxPredictor/test descriptors";
				String sdffile = "ValidatedStructures2d.sdf";
				String SDFFilePath = folder + "/" + sdffile;
				// String seekCAS="50-37-3";
				String seekCAS = "91-20-3";
				// String seekCAS="54522-53-1";

				DescriptorData dd = new DescriptorData();
				dd.ID = seekCAS;

				MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFFilePath));

				while (true) {

					AtomContainer ac = new AtomContainer();
					mr.read(ac);

					String CAS = ac.getProperty("CAS");

					// System.out.println(CAS+"\t"+ac.getAtomCount());

					if (CAS.equals(seekCAS)) {

						System.out.println(CAS + "\t" + ac.getAtomCount());

						dd.ID = seekCAS;
						int result = DF.CalculateDescriptors(ac, dd, true, true, true, folder);

						dd.WriteToFileTEXT(folder, "|");

						File myFile = new File(folder + "/DescriptorData.html");
						BrowserLauncher.openURL(myFile.toURL().toString());

						// System.out.println(dd.XLOGP);
						System.out.println(dd.SdsCH);

						return;
					}
				}
			} else if (mode == 5) {
				DF.ReadDescriptorTextTable(delimiter, "ToxPredictor/temp/data2d.txt");
				return;
			}
		} catch (Exception ex) {
			logger.catching(ex);
		}
	}

	public void WriteCSVHeader(DescriptorData dd, FileWriter fw, String Dimension, String Delimiter) {
		try {
			// fw.write("CAS"+Delimiter+"NAME"+Delimiter);
			fw.write("CAS" + Delimiter);

			// // write out experimental data
			// for (java.util.Enumeration e = h.keys(); e.hasMoreElements();) {
			//
			// String strVar = (String) e.nextElement();
			// if (strVar.equals(depvar)) {
			// fw.write(strVar + Delimiter);
			// }
			// }

			if (Dimension.equals("Frag")) {
				// write out fragment names:
				this.WriteFragmentNames(Delimiter, fw, dd);
			} else if (Dimension.equals("2D")) {
				this.WriteDescriptorNames(dd, dd.varlist2d, fw, Delimiter);
			} else if (Dimension.equals("3D")) {
				this.WriteDescriptorNames(dd, dd.varlist3d, fw, Delimiter);
			} else if (Dimension.equals("3D-2")) {
				this.WriteDescriptorNames2(dd, dd.strQM, fw, Delimiter);
			}

			fw.write("\r\n");
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void WriteCSVHeader2(DescriptorData dd, FileWriter fw, String Dimension, String Delimiter) {
		try {
			// fw.write("CAS"+Delimiter+"NAME"+Delimiter);
			fw.write("CAS" + Delimiter);

			fw.write(this.GetDescriptorNames(dd, dd.varlist2d, Delimiter));

			fw.write(Delimiter);

			fw.write(this.GetFragmentNames(Delimiter, dd));

			fw.write("\r\n");
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private String GetFragmentNames(String Delimiter, DescriptorData dd) {
		String Line = "";

		try {
			int count = 0;

			for (int i = 0; i < dd.strFragments.length; i++) {

				String fragname = dd.strFragments[i];

				if (Delimiter.equals(",") && fragname.indexOf(",") > -1) {
					Line += "\"" + fragname + "\"";
				} else {
					Line += fragname;
				}

				if (i < dd.strFragments.length - 1)
					Line += Delimiter;

				count++;
			}
		} catch (Exception e) {
			logger.catching(e);
		}
		return Line;
	}

	void WriteFragmentNames(String Delimiter, FileWriter fw, DescriptorData dd) {
		try {
			int count = 0;

			for (int i = 0; i < dd.strFragments.length; i++) {

				String fragname = dd.strFragments[i];

				if (Delimiter.equals(",") && fragname.indexOf(",") > -1) {
					fw.write("\"" + fragname + "\"");
				} else {
					fw.write(fragname);
				}

				if (i < dd.strFragments.length - 1)
					fw.write(Delimiter);

				count++;
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	void WriteDescriptorNames(DescriptorData dd, String[] varlist, FileWriter fw, String Delimiter) {
		try {

			int count = 0;

			for (int i = 0; i <= varlist.length - 1; i++) {
				// System.out.println(dd.varlist2d[i]);

				Field myField = dd.getClass().getField(varlist[i]);
				String[] names = (String[]) myField.get(dd);

				for (int j = 0; j <= names.length - 1; j++) {
					count++;
					Field myField2 = dd.getClass().getField(names[j]);

					String sname = names[j];

					if (Delimiter.equals(",") && sname.indexOf(",") > -1) {
						sname = "\"" + sname + "\"";
					}

					// System.out.println(i+"\t"+j);
					if (i != varlist.length - 1 || j != names.length - 1) {
						fw.write(sname + Delimiter);
					} else {
						fw.write(sname + "");
					}
				}

			}

			// System.out.println(count);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private String GetDescriptorNames(DescriptorData dd, String[] varlist, String Delimiter) {

		String Line = "";
		try {

			int count = 0;

			for (int i = 0; i <= varlist.length - 1; i++) {
				// System.out.println(dd.varlist2d[i]);

				Field myField = dd.getClass().getField(varlist[i]);
				String[] names = (String[]) myField.get(dd);

				for (int j = 0; j <= names.length - 1; j++) {
					count++;
					Field myField2 = dd.getClass().getField(names[j]);

					String sname = names[j];

					if (Delimiter.equals(",") && sname.indexOf(",") > -1) {
						sname = "\"" + sname + "\"";
					}

					// System.out.println(i+"\t"+j);
					if (i != varlist.length - 1 || j != names.length - 1) {
						Line += sname + Delimiter;
					} else {
						Line += sname + "";
					}
				}

			}

			// System.out.println(count);

		} catch (Exception e) {
			logger.catching(e);
		}
		return Line;
	}

	void WriteDescriptorNames2(DescriptorData dd, String[] varlist, FileWriter fw, String Delimiter) {
		try {

			int count = 0;

			for (int j = 0; j <= varlist.length - 1; j++) {
				count++;
				Field myField2 = dd.getClass().getField(varlist[j]);

				String sname = varlist[j];

				if (Delimiter.equals(",") && sname.indexOf(",") > -1) {
					sname = "\"" + sname + "\"";
				}

				// System.out.println(i+"\t"+j);
				if (j != varlist.length - 1) {
					fw.write(sname + Delimiter);
				} else {
					fw.write(sname + "");
				}
			}

			// System.out.println(count);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	void WriteCSVLine(DescriptorData dd, FileWriter fw, String Dimension, String Delimiter) {

		try {

			double DepVal = -99;

			// fw.write(dd.CAS + Delimiter);

			if (Delimiter.equals(",")) {
				if (dd.ID.indexOf(",") > -1) {
					fw.write("\"" + dd.ID + "\"" + Delimiter);
				} else {
					fw.write(dd.ID + Delimiter);
				}

			} else {
				fw.write(dd.ID + Delimiter);
			}

			// write out descriptor values:

			if (Dimension.equals("Frag")) {
				this.WriteFragmentValues(Delimiter, fw, dd);
			} else if (Dimension.equals("2D")) {
				this.WriteDescriptorValues(dd, dd.varlist2d, fw, Delimiter);
			} else if (Dimension.equals("3D")) {
				this.WriteDescriptorValues(dd, dd.varlist3d, fw, Delimiter);
			} else if (Dimension.equals("3D-2")) {
				this.WriteDescriptorValues2(dd, dd.strQM, fw, Delimiter);
			}

			fw.write("\r\n");
			fw.flush();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private String GetFragmentValues(String Delimiter, DescriptorData dd) {
		String Line = "";

		DecimalFormat df = new DecimalFormat("0");

		try {
			int count = 0;
			for (java.util.Enumeration e = dd.FragmentList.keys(); e.hasMoreElements();) {
				String strVar = (String) e.nextElement();
				double Val = (Double) dd.FragmentList.get(strVar);

				Line += df.format(Val);

				if (e.hasMoreElements())
					Line += (Delimiter);

				count++;
			}
		} catch (Exception e) {
			logger.catching(e);
		}

		return Line;
	}

	void WriteFragmentValues(String Delimiter, FileWriter fw, DescriptorData dd) {
		try {
			// Old way: frags are in order defined by hashtable:
			// int count=0;
			// for (java.util.Enumeration e = dd.FragmentList.keys(); e
			// .hasMoreElements();) {
			// String strVar = (String) e.nextElement();
			// double Val = (Double) dd.FragmentList.get(strVar);
			//
			// System.out.println(count+"\t"+strVar+"\t"+dd.strFragments[count]);
			//
			// fw.write(Val+"");
			//
			// if (e.hasMoreElements()) fw.write(Delimiter);
			//
			// count++;
			// }
			// Use strFragments so that frags are in nice order:
			for (int i = 0; i < dd.strFragments.length; i++) {

				String strVar = dd.strFragments[i];

				double Val = (Double) dd.FragmentList.get(strVar);
				fw.write(Val + "");

				// System.out.println(i+"\t"+strVar);

				if (i < dd.strFragments.length - 1)
					fw.write(Delimiter);

			}

		} catch (Exception e) {
			logger.catching(e);
		}
	}

//	/**
//	 * Writes out descriptors to readable file for looking at all descriptors in
//	 * a single vertical table
//	 * 
//	 * @param m
//	 * @param dd
//	 */
//	public void WriteOut2DDescriptors(IAtomContainer m, DescriptorData dd, String folderpath, String Delimiter) {
//
//		try {
//			FileWriter fw = new FileWriter(folderpath + "/2d_descriptors.txt");
//
//			DecimalFormat myD8 = new DecimalFormat("0.########");
//
//			String[] varlist = dd.varlist2d;
//
//			fw.write("Descriptor" + Delimiter + "Value\r\n");
//			for (int i = 0; i <= varlist.length - 1; i++) {
//				// System.out.println(dd.varlist2d[i]);
//
//				Field myField = dd.getClass().getField(varlist[i]);
//				String[] names = (String[]) myField.get(dd);
//				for (int j = 0; j <= names.length - 1; j++) {
//					Field myField2 = dd.getClass().getField(names[j]);
//
//					String val = myD8.format(myField2.getDouble(dd));
//
//					fw.write(names[j] + Delimiter + val + "\r\n");
//				}
//			}
//
//			// for (java.util.Enumeration e = dd.FragmentList.keys(); e
//			// .hasMoreElements();) {
//			// String strVar = (String) e.nextElement();
//			// double Val = (Double) dd.FragmentList.get(strVar);
//			//
//			// fw.write(strVar+Delimiter+Val + "\r\n");
//			// }
//
//			for (int i = 0; i < dd.strFragments.length; i++) {
//				String strVar = dd.strFragments[i];
//				double Val = (Double) dd.FragmentList.get(strVar);
//				fw.write(strVar + Delimiter + Val + "\r\n");
//			}
//
//			fw.close();
//
//		} catch (Exception e) {
//			logger.catching(e);
//		}
//	}

	
	
	
	
	/**
	 * Writes out descriptors for csv file (2d+frag)
	 * 
	 * @param m2
	 * @param dd
	 */
	public void WriteCSVLine(Writer fw, DescriptorData dd, String Delimiter) {

		try {
			DecimalFormat myD8 = new DecimalFormat("0.########");

			String[] varlist = dd.varlist2d;

			fw.write(dd.ID + Delimiter);

			for (int i = 0; i < varlist.length; i++) {
				// System.out.println(dd.varlist2d[i]);

				Field myField = dd.getClass().getField(varlist[i]);
				String[] names = (String[]) myField.get(dd);
				for (int j = 0; j < names.length; j++) {
					Field myField2 = dd.getClass().getField(names[j]);
					String val = myD8.format(myField2.getDouble(dd));
					fw.write(val + Delimiter);
					// System.out.print(val+Delimiter);
				}

			}
			// System.out.print("\n");

			DecimalFormat myD = new DecimalFormat("0");
			// for (java.util.Enumeration e = dd.FragmentList.keys(); e
			// .hasMoreElements();) {
			// String strVar = (String) e.nextElement();
			// double val = (Double) dd.FragmentList.get(strVar);
			//
			// fw.write(myD.format(val));
			//
			// if (e.hasMoreElements()) fw.write(Delimiter);
			// else fw.write("\r\n");
			// }

			for (int i = 0; i < dd.strFragments.length; i++) {
				String strVar = dd.strFragments[i];
				double val = (Double) dd.FragmentList.get(strVar);

				fw.write(myD.format(val));

				if (i < dd.strFragments.length - 1)
					fw.write(Delimiter);
				else
					fw.write("\r\n");
			}

			fw.flush();

		} catch (Exception e) {
			logger.catching(e);
		}
	}
	
	/**
	 * Converts to JSON and outputs to file
	 * 
	 * @param m
	 * @param dd
	 */
	public void WriteJSON(DescriptorData dd, String outputFilePath,boolean writeMultiline) {

		try {
			JsonObject jo=dd.toJSON();
			
//			DescriptorData test=DescriptorData.toDescriptorData(record);//test convert back
			
			GsonBuilder builder = new GsonBuilder();
			
			if (writeMultiline) builder.setPrettyPrinting().serializeNulls();// makes it multiline and readable
	        
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(outputFilePath);
			fw.write(gson.toJson(jo));
            fw.flush();
            fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}
	

	/**
	 * Writes out header for csv file (2d+frag)
	 * 
	 * @param m2
	 * @param dd
	 */
	public void WriteCSVHeader(Writer fw, String Delimiter) {

		try {
			DescriptorData dd = new DescriptorData();

			String[] varlist = dd.varlist2d;

			fw.write("CAS" + Delimiter);

			for (int i = 0; i <= varlist.length - 1; i++) {
				// System.out.println(dd.varlist2d[i]);

				Field myField = dd.getClass().getField(varlist[i]);
				String[] names = (String[]) myField.get(dd);
				for (int j = 0; j < names.length; j++) {
					if (names[j].indexOf(",") > -1 && Delimiter.equals(",")) {
						fw.write("\"" + names[j] + "\"");
					} else {
						fw.write(names[j]);
					}

					fw.write(Delimiter);
				}
			}

			for (int i = 0; i < dd.strFragments.length; i++) {
				String strVar = dd.strFragments[i];

				if (strVar.indexOf(",") > -1 && Delimiter.equals(",")) {
					fw.write("\"" + strVar + "\"");
				} else {
					fw.write(strVar);
				}

				if (i < dd.strFragments.length - 1)
					fw.write(Delimiter);
			}
			fw.write("\r\n");

		} catch (Exception e) {
			logger.catching(e);
		}

	}

	void WriteDescriptorValues(DescriptorData dd, String[] varlist, FileWriter fw, String Delimiter) {

		DecimalFormat myD8 = new DecimalFormat("0.########");

		try {
			int countwritten = 0;

			for (int i = 0; i <= varlist.length - 1; i++) {
				// System.out.println(dd.varlist2d[i]);

				Field myField = dd.getClass().getField(varlist[i]);
				String[] names = (String[]) myField.get(dd);
				for (int j = 0; j <= names.length - 1; j++) {
					Field myField2 = dd.getClass().getField(names[j]);

					String val = myD8.format(myField2.getDouble(dd));

					countwritten++;
					// System.out.println(i+"\t"+j);
					if (i != varlist.length - 1 || j != names.length - 1) {
						fw.write(val + Delimiter);
					} else {
						fw.write(val + "");
					}
				}
			}
			// System.out.println(countwritten);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	private String GetDescriptorValues(DescriptorData dd, String[] varlist, String Delimiter) {

		String Line = "";
		DecimalFormat myD8 = new DecimalFormat("0.########");

		try {

			int countwritten = 0;

			for (int i = 0; i <= varlist.length - 1; i++) {
				// System.out.println(dd.varlist2d[i]);

				Field myField = dd.getClass().getField(varlist[i]);
				String[] names = (String[]) myField.get(dd);
				for (int j = 0; j <= names.length - 1; j++) {
					Field myField2 = dd.getClass().getField(names[j]);

					String val = myD8.format(myField2.getDouble(dd));

					countwritten++;
					// System.out.println(i+"\t"+j);
					if (i != varlist.length - 1 || j != names.length - 1) {
						Line += (val + Delimiter);
					} else {
						Line += (val + "");
					}
				}
			}
			// System.out.println(countwritten);

		} catch (Exception e) {
			logger.catching(e);
		}

		return Line;
	}

	void WriteDescriptorValues2(DescriptorData dd, String[] names, FileWriter fw, String Delimiter) {

		DecimalFormat myD8 = new DecimalFormat("0.########");

		try {
			int countwritten = 0;

			for (int j = 0; j <= names.length - 1; j++) {
				Field myField2 = dd.getClass().getField(names[j]);

				String val = myD8.format(myField2.getDouble(dd));

				countwritten++;
				// System.out.println(i+"\t"+j);
				if (j != names.length - 1) {
					fw.write(val + Delimiter);
				} else {
					fw.write(val + "");
				}

			}

			// System.out.println(countwritten);

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	// void StoreVariables(Molecule m, DescriptorData dd) {
	// Hashtable h = new Hashtable();
	//
	// try {
	// Field[] fields = DescriptorData.class.getFields();
	// for (int i = 0; i <= fields.length - 1; i++) {
	// // System.out.println(fields[i].getName()+"\t"+fields[i].getType());
	// // System.out.println("-"+fields[i].getType()+"-");
	//
	// if (fields[i].getType().getName().equals("int")) {
	// h.put(fields[i].getName(), fields[i].getInt(dd) + "");
	// } else if (fields[i].getType().getName().equals("double")) {
	// h.put(fields[i].getName(), fields[i].getDouble(dd) + "");
	// } else if (fields[i].getType().getName().equals(
	// "java.lang.String")) {
	// h.put(fields[i].getName(), fields[i].get(dd));
	// }
	//
	// }
	// // store fragment values:
	// for (java.util.Enumeration e = dd.FragmentList.keys(); e
	// .hasMoreElements();) {
	// String var = (String) e.nextElement();
	// String strval = (String) dd.FragmentList.get(var);
	// int val = Integer.parseInt(strval);
	// h.put(var, val + "");
	// }
	//
	// m.setProperties(h);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	public void ReadDescriptorTextTable(String del, String fileloc) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileloc));

			String strHeader = br.readLine();

			LinkedList header = Utilities.Parse3(strHeader, del);

			for (int i = 1; i <= 10; i++) {
				String Line1 = br.readLine();

				if (!(Line1 instanceof String))
					break;

				LinkedList data = Utilities.Parse(Line1, del);

				// System.out.println(i + "\t" + header.size() + "\t"
				// + data.size());
				// System.out.println(header.get(9) + "\t" + data.get(9));

				System.out.println(data.size() + "\t" + header.size() + "\t" + i);

				System.out.println("\n" + i);

				for (int j = 0; j <= header.size() - 1; j++) {
					System.out.println(j + "\t" + header.get(j) + "\t" + data.get(j));
				}

			}

			br.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void CreateKOWWINTableFromSDFFile(int startvalue, int stopvalue, String SDFfilename, String outputfileloc,
			String Delimiter) {
		try {

			DescriptorData md = new DescriptorData();
			DescriptorData d = new DescriptorData();

			BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
			FileWriter fw = new FileWriter(outputfileloc);

			MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFfilename));

			String[] strData = new String[200];

			int counter = -1;

			fw.write("CAS" + Delimiter + "KOWWINLOGP" + Delimiter + "KOWWINLOGP2" + "\n");

			dd = new DescriptorData();

			boolean stop = false;

			while (true) {

				counter++;

				IAtomContainer m = null;
				mr.read(m);

				if (m == null)
					break;

				String CASField = ToxPredictor.misc.MolFileUtilities.getCASField(m);
				String CAS = (String) m.getProperty(CASField);

				// String CAS=(String)m.getProperty("CAS");

				if (CAS == null)
					break;

				CAS = CAS.trim();

				dd.ID = CAS;

				// System.out.println(counter+"\t"+CAS);

				this.CalculateKOWWIN_From_DLL(m);

				if (counter < startvalue)
					continue;

				fw.write(CAS + Delimiter + dd.KLOGP + Delimiter + dd.KLOGP2 + "\r\n");
				fw.flush();

				if (counter == stopvalue)
					break;

			} // end while true;

			br.close();
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void CreateKOWWINTableFromSmilesFile(String SmilesFileName, String outputfileloc, String Delimiter) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(SmilesFileName));
			FileWriter fw = new FileWriter(outputfileloc);

			fw.write("CAS" + Delimiter + "KOWWINLOGP\n");

			dd = new DescriptorData();

			while (true) {

				String Line = br.readLine();
				// System.out.println(Line);
				if (Line == null)
					break;

				String CAS = "";
				String Smiles = "";

				Smiles = Line.substring(0, Line.indexOf("\t"));
				CAS = Line.substring(Line.indexOf("\t") + 1, Line.length());

				this.CalculateKOWWIN_From_DLL(Smiles);

				fw.write(CAS + Delimiter + dd.KLOGP + "\r\n");
				fw.flush();

			} // end while true;

			br.close();
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	// public void CreateDescriptorRaghu3DTableTextFileFromSDF(int startvalue,
	// int stopvalue, String SDFfilename,String outputfileloc,String Delimiter)
	// {
	//
	// try {
	//
	// descriptorfactory3D_2 df=new descriptorfactory3D_2();
	//
	// BufferedReader br = new BufferedReader(new FileReader(SDFfilename));
	// FileWriter fw = new FileWriter(outputfileloc);
	// FileWriter fw2=null;
	//
	// MDLReader mr = new MDLReader();
	//
	//
	// int counter = -1;
	//
	//// SmilesParser sp = new SmilesParser();
	//
	// while (true) {
	//
	// counter++;
	//
	//// if (counter % 50 == 0)
	//// System.out.println(counter);
	//
	// IAtomContainer m = mr.readMolecule(br);
	//
	// String CAS=(String)m.getProperty("CAS");
	// if (CAS==null) break;
	// CAS=CAS.trim();
	//
	// System.out.println(CAS);
	//
	//
	//// Molecule fragment = (Molecule)sp.parseSmiles("C(=O)N");
	//// List ll = UniversalIsomorphismTester.getSubgraphAtomsMaps(
	//// m, fragment);
	////
	//// if (ll.size()>0)
	//// System.out.println(CAS+"\t"+ll.size());
	//
	//// if (CAS.equals("50-76-0")) continue;//hangs
	// if (CAS.equals("51-03-6")) continue;//hangs
	// if (CAS.equals("59-05-2")) continue;//hangs
	// if (CAS.equals("64-77-7")) continue;//hangs
	// if (CAS.equals("67-98-1")) continue;//hangs
	// if (CAS.equals("75-05-8")) continue;//hangs -3 atoms in a row
	// if (CAS.equals("79-81-2")) continue;//hangs
	// if (CAS.equals("88-96-0")) continue;//hangs
	// if (CAS.equals("94-20-2")) continue;//hangs
	// if (CAS.equals("303-47-9")) continue;//hangs
	// if (CAS.equals("458-37-7")) continue;//hangs
	// if (CAS.equals("3546-10-9")) continue;//hangs
	// if (CAS.equals("6358-85-6")) continue;//hangs
	// if (CAS.equals("6471-49-4")) continue;//hangs
	// if (CAS.equals("7572-29-4")) continue;//hangs
	// if (CAS.equals("7585-39-9")) continue;//hangs
	// if (CAS.equals("7681-93-8")) continue;//hangs
	// if (CAS.equals("10473-70-8")) continue;//hangs
	// if (CAS.equals("13292-46-1")) continue;//hangs
	// if (CAS.equals("13743-07-2")) continue;//hangs
	// if (CAS.equals("15318-45-3")) continue;//hangs
	// if (CAS.equals("18883-66-4")) continue;//hangs
	// if (CAS.equals("22966-79-6")) continue;//hangs
	// if (CAS.equals("57817-89-7")) continue;//hangs
	// if (CAS.equals("59865-13-3")) continue;//hangs
	// if (CAS.equals("62488-57-7")) continue;//hangs
	// if (CAS.equals("63642-17-1")) continue;//hangs
	// if (CAS.equals("65089-17-0")) continue;//hangs
	// if (CAS.equals("116355-83-0")) continue;//hangs
	// if (CAS.equals("123524-52-7")) continue;//hangs
	// if (CAS.equals("145040-37-5")) continue;//hangs
	//
	// if (CAS.equals("126-13-6")) continue;//hangs
	//
	// if (CAS.equals("135-20-6")) continue;//error during descriptor
	// calcs-index error
	// if (CAS.equals("150-38-9")) continue;//error during descriptor
	// calcs-index error
	//
	//
	//
	//
	//// if (CAS.equals("101-25-7")) continue;//need to run v5
	//// if (CAS.equals("75411-83-5")) continue;//need to run v5
	//// if (CAS.equals("133920-06-6")) continue;//need to run v5
	//
	// dd=new DescriptorData();
	//
	// dd.CAS=CAS;
	//
	// if (counter == 0) {
	// this.WriteCSVHeader(dd, fw,"3D-2",Delimiter);
	// }
	//
	// if (counter < startvalue)
	// continue;
	//
	// if (CAS==null) break;
	//
	//
	// //** Calculate descriptors **
	// df.CalculateDescriptors(m, dd);
	//
	// // write out line of results
	// this.WriteCSVLine(dd, fw, "3D-2",Delimiter);
	//
	// if (counter == stopvalue)
	// break;
	//
	//
	// }// end while true;
	//
	// br.close();
	// fw.close();
	//
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	//
	// }
	//
	// }

	public void CreateDescriptorTableTextFileFromSDF(int startvalue, int stopvalue, String SDFfilename,
			String outputfileloc, String Dimension, String Delimiter, boolean CreateFragmentWebPage,
			String fragmentwebpage) {

		try {

			// this.WriteHeaderCheckFile(fw);

			FileWriter fw = new FileWriter(outputfileloc);
			FileWriter fw2 = null;

			if (CreateFragmentWebPage) {
				fw2 = new FileWriter(fragmentwebpage);
				fw2.write("<html><head>");
				fw2.write("<title>");
				fw2.write("Fragments for " + SDFfilename);
				fw2.write("</title></head>\n");
				fw2.flush();

			}

			// this.WriteCSVHeader(d,fw);

			MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFfilename));

			// String [] strMolFile=new String [200];
			String[] strData = new String[200];

			// String ChemicalName;
			// String CAS;

			int counter = -1;

			while (true) {

				counter++;

				// if (counter % 50 == 0)
				// System.out.println(counter);

				IAtomContainer m = null;
				mr.read(m);
				// System.out.println(m.getAtomCount());

				// int DataLineCount = 0;
				// while (true) {
				// String Line = br.readLine();
				// strData[DataLineCount] = Line;
				// DataLineCount++;
				// if (Line.indexOf("$$$$") > -1)
				// break;
				// }

				String CAS = (String) m.getProperty("CAS");
				if (CAS == null)
					break;
				CAS = CAS.trim();

				dd = new DescriptorData();

				dd.ID = CAS;
				// System.out.println(dd.CAS);

				// System.out.println(counter+"\t"+CAS);

				if (counter < startvalue)
					continue;

				// System.out.print("Processing "+ChemicalName+"...");
				if (CAS == null)
					break;
				// if (CAS.equals("2278-50-4")) continue;//runs out of memory!

				// if (!CAS.equals("1930729")) continue;

				if (Dimension.indexOf("Frag") > -1) {
					this.CalculateDescriptors(m, dd, false, false, true, "");

					if (CreateFragmentWebPage) {
						fw2.write("<p>CAS: " + CAS + "</p>\n");
						fw2.write("<table>\n");
						fw2.write("<tr>\n");
						fw2.write("<td>\n");
						ParseChemidplus.WriteFragmentTable(fw2, dd.FragmentList);
						fw2.write("</td>\n");

						File imgfile = new File("ToxPredictor/Check/structures/" + CAS + ".png");

						if (!(imgfile.exists())) {
							SaveStructureToFile.CreateImageFile(m, CAS, "ToxPredictor/Check/structures");
						}

						fw2.write("<td>\n");
						fw2.write("<img src=\"structures/" + CAS + ".png\">\n");
						fw2.write("</td>\n");

						fw2.write("</tr>\n");
						fw2.write("</table>\n");
						fw2.write("<br><hr>\n");
						fw2.flush();

					}

				} else {
					this.CalculateDescriptors(m, dd, false, false, true, "");
				}

				// Hashtable h = this.StoreSDData(strData, m, dd);

				if (counter == 0) {
					this.WriteCSVHeader(dd, fw, Dimension, Delimiter);
				}

				// write out line of results
				this.WriteCSVLine(dd, fw, Dimension, Delimiter);

				if (counter == stopvalue)
					break;

			} // end while true;

			fw.close();
			if (CreateFragmentWebPage) {
				fw2.write("<html>");
				fw2.close();
			}

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	public void LookAtDescriptorsInSDF(String SDFfilepath, String outputpath, int startvalue, int stopvalue) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));
			FileWriter fw = new FileWriter(outputpath);

			MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFfilepath));

			int counter = -1;

			ToxPredictor.misc.MolFileUtilities mfu = new ToxPredictor.misc.MolFileUtilities();

			while (true) {

				counter++;

				AtomContainer m = new AtomContainer();

				mr.read(m);

				// System.out.println(counter);

				// System.out.println(m.getAtomCount());

				if (m == null || m.getProperties() == null)
					break;

				String CASField = ToxPredictor.misc.MolFileUtilities.getCASField(m);

				String CAS = (String) m.getProperty(CASField);
				if (CAS == null)
					break;
				CAS = CAS.trim();

				if (CAS.equals("2278-50-4"))
					continue;

				// CAS="C"+CAS;

				dd = new DescriptorData();

				dd.ID = CAS;

				// System.out.println(mfu.HaveBadElement(m));

				if (mfu.HaveBadElement(m)) {
					System.out.println(counter + "\t" + CAS + "\tBadElement");
					continue;
				}

				IAtomContainerSet moleculeSet = (IAtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

				if (moleculeSet.getAtomContainerCount() > 1) {
					System.out.println(counter + "\t" + CAS + "\tMultiple fragments");
					continue;
				}

				// System.out.println(counter+"\t"+CAS);

				if (counter < startvalue) {
					continue;
				}

				if (CAS == null)
					break;

				if (m == null) {
					System.out.println("CAS=" + CAS + " has null molecule");
					continue;
				}
				if (m.getAtomCount() == 0) {
					System.out.println("CAS=" + CAS + " has no atoms");
					continue;
				}

				this.CalculateDescriptors(m, dd, false, false, true, "");

				double diff = Math.abs(dd.MW - dd.MW_Frag);

				DecimalFormat df = new DecimalFormat("0.00");

				System.out.println(counter + "\t" + CAS + "\t" + df.format(dd.MW) + "\t" + df.format(dd.MW_Frag) + "\t"
						+ df.format(diff));

				fw.write(counter + "\t" + CAS + "\t" + df.format(dd.MW) + "\t" + df.format(dd.MW_Frag) + "\t"
						+ df.format(diff) + "\r\n");
				fw.flush();

				if (counter == stopvalue)
					break;

			} // end while true;

			br.close();
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * Creates data2D.txt and dataFrag.txt simultaneously data2D.txt doesnt
	 * include frag descriptors
	 * 
	 * @param startvalue
	 * @param stopvalue
	 * @param SDFfilepath
	 * @param outputfilefolder
	 * @param Delimiter
	 */
	public void CreateDescriptorTableTextFileFromSDF_2d_and_frag(int startvalue, int stopvalue, String SDFfilepath,
			String outputfilefolder, String Delimiter) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));

			FileWriter fw2d = new FileWriter(outputfilefolder + "/data2D.csv");
			FileWriter fwFrag = new FileWriter(outputfilefolder + "/dataFrag.csv");

			// System.out.println(outputfilefolder);

			// this.WriteCSVHeader(d,fw);

			MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFfilepath));

			int counter = -1;

			ToxPredictor.misc.MolFileUtilities mfu = new ToxPredictor.misc.MolFileUtilities();

			while (true) {

				counter++;

				IAtomContainer m = null;

				try {

					mr.read(m);

				} catch (CDKException ex1) {
					// System.out.println(ex1.getMessage());
					break;
				}

				// System.out.println(m.getAtomCount());

				if (m == null || m.getProperties() == null)
					break;

				String CASField = ToxPredictor.misc.MolFileUtilities.getCASField(m);

				String CAS = (String) m.getProperty(CASField);
				if (CAS == null)
					break;
				CAS = CAS.trim();

				// CAS="C"+CAS;

				dd = new DescriptorData();

				dd.ID = CAS;

				if (mfu.HaveBadElement(m)) {
					System.out.println(counter + "\t" + CAS + "\tBadElement");
					continue;
				}

				IAtomContainerSet moleculeSet = (IAtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

				if (moleculeSet.getAtomContainerCount() > 1) {
					System.out.println(counter + "\t" + CAS + "\tMultiple fragments");
					continue;
				}

				// if (CAS.equals("409-21-2")) continue;
				// if (CAS.equals("1333-74-0")) continue;
				// if (CAS.equals("7782-39-0")) continue;
				// if (CAS.equals("14808-60-7")) continue;
				// if (CAS.equals("74-82-8")) continue;//methane

				// System.out.println(counter+"\t"+CAS);

				if (counter < startvalue) {
					continue;
				}

				if (CAS == null)
					break;

				if (m == null) {
					System.out.println("CAS=" + CAS + " has null molecule");
					continue;
				}
				if (m.getAtomCount() == 0) {
					System.out.println("CAS=" + CAS + " has no atoms");
					continue;
				}

				// http://wiki.jmol.org/index.php/Support_for_bond_orders
				boolean havePartialBondOrder = false;

				for (int bond = 0; bond < m.getBondCount(); bond++) {
					if (m.getBond(bond).getOrder().numeric() == 8) {
						havePartialBondOrder = true;
						System.out.println(CAS + "\tHave partial bond order!");
						break;
					}
				}

				if (havePartialBondOrder)
					continue;

				// System.out.println("here123");

				int result = this.CalculateDescriptors(m, dd, false, false, true, "");

				if (result < 0) {
					System.out.println(dd.ID + "\tError: " + this.errorMsg);
					fw2d.write(dd.ID + ",error\r\n");
					fwFrag.write(dd.ID + ",error\r\n");
					fw2d.flush();
					fwFrag.flush();
					continue;

				}

				double diff = Math.abs(dd.MW - dd.MW_Frag);
				if (diff > 0.1) {
					System.out.println(dd.ID + "\tPossible missing frag!");
				}

				if (counter == 0 || counter == startvalue) {
					this.WriteCSVHeader(dd, fw2d, "2D", Delimiter);
					this.WriteCSVHeader(dd, fwFrag, "Frag", Delimiter);
				}

				// write out line of results
				this.WriteCSVLine(dd, fw2d, "2D", Delimiter);
				this.WriteCSVLine(dd, fwFrag, "Frag", Delimiter);

				fw2d.flush();
				fwFrag.flush();

				if (counter == stopvalue)
					break;

			} // end while true;

			br.close();
			fw2d.close();
			fwFrag.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * Creates single descriptor file (which includes both 2d and frag
	 * descriptors - data2D_Frag.txt)
	 * 
	 * @param startvalue
	 * @param stopvalue
	 * @param SDFfilepath
	 * @param outputfilefolder
	 * @param Delimiter
	 */
	public void Create2D_Frag_DescriptorTable(int startvalue, int stopvalue, String SDFfilepath,
			String outputfilefolder, String Delimiter, String outputfilename) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));

			FileWriter fw = new FileWriter(outputfilefolder + "/" + outputfilename);

			// this.WriteCSVHeader(d,fw);

			MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFfilepath));

			int counter = -1;

			ToxPredictor.misc.MolFileUtilities mfu = new ToxPredictor.misc.MolFileUtilities();

			while (true) {

				counter++;

				IAtomContainer m = null;
				mr.read(m);

				String CASField = ToxPredictor.misc.MolFileUtilities.getCASField(m);

				String CAS = (String) m.getProperty(CASField);
				if (CAS == null)
					break;
				CAS = CAS.trim();

				dd = new DescriptorData();

				dd.ID = CAS;

				if (mfu.HaveBadElement(m)) {
					System.out.println(counter + "\t" + CAS + "\tBadElement");
					continue;
				}

				IAtomContainerSet moleculeSet = (IAtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

				if (moleculeSet.getAtomContainerCount() > 1) {
					System.out.println(counter + "\t" + CAS + "\tMultiple fragments");
					continue;
				}

				System.out.println(counter + "\t" + CAS);

				if (counter < startvalue) {
					continue;
				}

				if (CAS == null)
					break;

				this.CalculateDescriptors(m, dd, false, false, true, "");

				// ************************************************
				if (counter == 0 || counter == startvalue) {
					// write header:
					// fw.write("CAS"+Delimiter);
					// fw.write(this.GetDescriptorNames(dd, dd.varlist2d,
					// Delimiter));
					// fw.write(Delimiter);
					// fw.write(this.GetFragmentNames(Delimiter, dd));
					// fw.write("\r\n");

					this.WriteCSVHeader(fw, Delimiter);
				}

				if (m == null)
					System.out.println("CAS=" + CAS + " has null molecule");

				// ************************************************
				// write out line of results
				// fw.write(CAS+Delimiter);
				// fw.write(this.GetDescriptorValues(dd, dd.varlist2d,
				// Delimiter));
				// fw.write(Delimiter);
				// fw.write(this.GetFragmentValues(Delimiter, dd));
				// fw.write("\r\n");

				this.WriteCSVLine(fw, dd, Delimiter);

				// ************************************************

				if (counter == stopvalue)
					break;

			} // end while true;

			br.close();
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * Creates single file (which includes toxicity and both 2d and frag
	 * descriptors)
	 * 
	 * @param startvalue
	 * @param stopvalue
	 * @param SDFfilepath
	 * @param outputfilefolder
	 * @param Delimiter
	 */
	public void CreateOverallFile(int startvalue, int stopvalue, String SDFfilepath, String outputfilefolder,
			String Delimiter, String outputfilename, String toxFieldName) {

		try {
			BufferedReader br = new BufferedReader(new FileReader(SDFfilepath));

			FileWriter fw = new FileWriter(outputfilefolder + "/" + outputfilename);

			MDLV2000Reader mr = new MDLV2000Reader(new FileInputStream(SDFfilepath));

			int counter = -1;

			ToxPredictor.misc.MolFileUtilities mfu = new ToxPredictor.misc.MolFileUtilities();

			while (true) {

				counter++;

				IAtomContainer m = null;
				mr.read(m);

				String CASField = ToxPredictor.misc.MolFileUtilities.getCASField(m);

				String CAS = (String) m.getProperty(CASField);
				if (CAS == null)
					break;
				CAS = CAS.trim();

				String Tox = (String) m.getProperty(toxFieldName);

				dd = new DescriptorData();

				dd.ID = CAS;

				if (mfu.HaveBadElement(m)) {
					System.out.println(counter + "\t" + CAS + "\tBadElement");
					continue;
				}

				IAtomContainerSet moleculeSet = (IAtomContainerSet) ConnectivityChecker.partitionIntoMolecules(m);

				if (moleculeSet.getAtomContainerCount() > 1) {
					System.out.println(counter + "\t" + CAS + "\tMultiple fragments");
					continue;
				}

				System.out.println(counter + "\t" + CAS);

				if (counter < startvalue) {
					continue;
				}

				if (CAS == null)
					break;

				this.CalculateDescriptors(m, dd, false, false, true, "");

				if (counter == 0 || counter == startvalue) {
					// write header:
					fw.write("CAS" + Delimiter + "Tox" + Delimiter);
					fw.write(this.GetDescriptorNames(dd, dd.varlist2d, Delimiter));
					fw.write(Delimiter);
					fw.write(this.GetFragmentNames(Delimiter, dd));
					fw.write("\r\n");
				}

				if (m == null)
					System.out.println("CAS=" + CAS + " has null molecule");

				// write out line of results
				fw.write(CAS + Delimiter + Tox + Delimiter);
				fw.write(this.GetDescriptorValues(dd, dd.varlist2d, Delimiter));
				fw.write(Delimiter);
				fw.write(this.GetFragmentValues(Delimiter, dd));
				fw.write("\r\n");

				if (counter == stopvalue)
					break;

			} // end while true;

			br.close();
			fw.close();

		} catch (Exception e) {
			logger.catching(e);
		}
	}
}
