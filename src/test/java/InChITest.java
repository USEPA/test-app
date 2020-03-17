import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.SmiFlavor;

import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.ResourceLoader;
import net.sf.jniinchi.INCHI_OPTION;
import net.sf.jniinchi.INCHI_RET;

public class InChITest extends ResourceLoader {

	@SuppressWarnings("resource")
	@Test
	public void inchiKeyCollisions() throws IOException {
		File sdfPath = new File(getResourcePath("collisions.sdf"));
		IteratingSDFReader r = new IteratingSDFReader(new FileInputStream(sdfPath), DefaultChemObjectBuilder.getInstance(), true);
		while (r.hasNext()) {
			IAtomContainer sdfRec = r.next();
			String smiles = CDKUtilities.generateSmiles(sdfRec, SmiFlavor.Generic);
			System.out.println("Generic:  " + smiles);
			smiles = CDKUtilities.generateSmiles(sdfRec, SmiFlavor.Unique);
			System.out.println("Unique:   " + smiles);
			smiles = CDKUtilities.generateSmiles(sdfRec, SmiFlavor.Isomeric);
			System.out.println("Isomeric: " + smiles);
			smiles = CDKUtilities.generateSmiles(sdfRec, SmiFlavor.Absolute);
			System.out.println("Absolute: " + smiles);
			
			String[] inchis = CDKUtilities.generateInChiKey(sdfRec);
			System.out.println("InChI: " + inchis[0]);
		}
		
		System.out.println("done");
	}
	
	public static String getInchiKeyFromInchi(String inchi) throws CDKException {
		List<INCHI_OPTION> options = new ArrayList<INCHI_OPTION>();
		options.add(INCHI_OPTION.FixedH);//makes sure  tautomers come out different! 

		IAtomContainer container = null;
		// Generate factory - throws CDKException if native code does not load
		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
		// Get InChIToStructure
		InChIToStructure intostruct = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance(), "FixedH");
		INCHI_RET ret = intostruct.getReturnStatus();
		if (ret != INCHI_RET.OKAY && ret != INCHI_RET.WARNING) {
			System.out.println("-- Structure generation failed: "
					+ ret.toString() + " [" + intostruct.getMessage() + "]");
			return null;
		}
		
		if (ret == INCHI_RET.WARNING) {
			System.out.println("-- InChI warning: " + intostruct.getMessage());
			
		}

		
		final InChIGenerator gen = factory.getInChIGenerator(intostruct.getAtomContainer(), options);
		ret = gen.getReturnStatus();
		
		if (ret != INCHI_RET.OKAY && ret != INCHI_RET.WARNING) {
			System.out.println("-- Inchi Key generation failed: "
					+ ret.toString() + " [" + intostruct.getMessage() + "]");
			return null;
		}

		if (ret == INCHI_RET.WARNING) {
			System.out.println("-- InChIKey warning: " + gen.getMessage());
		}
		
		return gen.getInchiKey();
	}

	public static void main(String[] args) throws IOException, CDKException {
		File file = new File("inchi/out.txt");
	       FileOutputStream fos = new FileOutputStream(file);
	       PrintStream ps = new PrintStream(fos);
	       System.setOut(ps);
	       
		List<String> lines = Files.readAllLines(Paths.get("inchi/in.txt"));
		String inchiKey = null;
		for (String s : lines) {
			String[] parts = s.split("\\t");
			if (parts.length == 1) {
				System.out.println("-- InChI is undefined for " + parts[0]);
				continue;
			}
			try {
				inchiKey = getInchiKeyFromInchi(parts[1]);
			} catch (Exception e) {
				System.out.println("-- Error generating inchi key for " + parts[1]);
			}
			if (inchiKey != null) {
				System.out.println(String.format("update NCCT_ID set InChI_Key_QSARr = '%s' where casrn = '%s';", inchiKey, parts[0]));
			}
		}
		
		fos.flush();
	}
}
