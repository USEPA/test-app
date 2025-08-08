package ToxPredictor.Utilities;

import java.io.StringWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Writer;
import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;

public class IndigoUtilities {

	private static final Logger logger = LogManager.getLogger(IndigoUtilities.class);

	private static Indigo indigo = null;
	
	private static IndigoInchi indigoInchi = null;
	
//	private static IndigoInchi indigoInchi;
	
	private static void init()
	{
		synchronized ( IndigoUtilities.class ) {
			if ( indigo == null ) {
				indigo = new Indigo();
				indigo.setOption("ignore-stereochemistry-errors", true);
				indigoInchi = new IndigoInchi(indigo);
			}
		}
	}
	
	private static IndigoObject toMol(IAtomContainer ac) {
		init();
		
		try {
			StringWriter sw = new StringWriter();
			MDLV2000Writer writer = new MDLV2000Writer(sw);
			writer.write((IAtomContainer)ac);
			writer.close();
			return indigo.loadMolecule(sw.toString());
		}
		catch ( Exception ex ) {
			return null;
		}
	}
	
	
	
	public static String generateSmiles(IAtomContainer ac) {
		IndigoObject mol = toMol(ac);
		if ( mol == null )
			return null;
		
		return mol.canonicalSmiles();
	}

	public static Inchi generateInChiKey(IAtomContainer ac) {
		
		init();
		
		IndigoObject mol = toMol(ac);
		if ( mol == null )
			return null;
		
			
		Inchi inchi=new Inchi();
		
		try {
			
//			IndigoInchi indigoInchi = new IndigoInchi(indigo);
		
			inchi.inchi = indigoInchi.getInchi(mol);
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;
			
			return inchi;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
				
	}
	
	public static Inchi toInchiIndigo(String mol) {
		try {
			init();
			
			IndigoObject m = indigo.loadMolecule(mol);

//			IndigoInchi indigoInchi = new IndigoInchi(indigo);

			Inchi inchi=new Inchi();
			inchi.inchi = indigoInchi.getInchi(m);
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;
			
			return inchi;
			
		} catch ( IndigoException ex ) {
//			log.error(ex.getMessage());
			return null;
		}
	}
	
	public static Inchi toInchiIndigo2(String mol) {
		try {
			Indigo indigo = new Indigo();
			indigo.setOption("ignore-stereochemistry-errors", true);

//			indigo.setOption("FixedH", true);
			indigo.setOption("inchi-options","/FixedH");
			

//			IndigoInchi indigoInchi = new IndigoInchi(indigo);

			// Example SMILES for a compound


			// Load the molecule from the SMILES string
			IndigoObject molecule = indigo.loadMolecule(mol);

			Inchi inchi=new Inchi();

			// Generate InChI with FixedH option
			inchi.inchi = indigoInchi.getInchi(molecule);

			//	            System.out.println("InChI with FixedH: " + inchi);

			// Generate InChIKey
			inchi.inchiKey = indigoInchi.getInchiKey(inchi.inchi);
			System.out.println("InChIKey: " + inchi.inchiKey);

			inchi.inchiKey1 = inchi.inchiKey != null ? inchi.inchiKey.substring(0, 14) : null;

			return inchi;


		} catch ( IndigoException ex ) {
			//			log.error(ex.getMessage());
			ex.printStackTrace();
		}

		return null;
	}


}
