import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;
import ToxPredictor.Utilities.CDKUtilities;
import ToxPredictor.Utilities.Inchi;

public class ResolverDbTests {

	@Test
	public void byCAS() throws ClassNotFoundException, SQLException {
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookupByCAS("10190-99-5");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID7099", cdrs.get(0).cid);
	}

	@Test
	public void byInChIKey() throws ClassNotFoundException, SQLException {
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookupByInChIKey("NCKMMSIFQUPKCK-UHFFFAOYNA-N");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID30154", cdrs.get(0).cid);
	}
	
	@Test
	public void byInChIKey1() throws ClassNotFoundException, SQLException {
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookupByInChIKey1("NCKMMSIFQUPKCK", true);
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID30154", cdrs.get(0).cid);
	}
	
	@Test
	public void bySMILES() throws ClassNotFoundException, SQLException, InvalidSmilesException {
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		IAtomContainer m = sp.parseSmiles("CC(C)(CO)N1CC1C(O)=O");
		Inchi inchi = CDKUtilities.generateInChiKey(m);
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookupByInChIKey(inchi.inchiKey);
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID20336575", cdrs.get(0).cid);
	}
	
	@Test
	public void byName() throws ClassNotFoundException, SQLException, InvalidSmilesException {
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookupByName("AsPiRiN");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID50108", cdrs.get(0).cid);
	}
	
	@Test
	public void byAny() throws ClassNotFoundException, SQLException, InvalidSmilesException {
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookup("CCCCCCCNC1=C(O)C2=CC=CC=C2C(=O)C1=O");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID20250404", cdrs.get(0).cid);
		
		cdrs = ResolverDb.lookup("NSC129153");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID20250404", cdrs.get(0).cid);
		
		cdrs = ResolverDb.lookup("22158-43-6");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID20250404", cdrs.get(0).cid);
		
		cdrs = ResolverDb.lookup("VVTYDTWGDNCMHW-UHFFFAOYSA-N");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID20250404", cdrs.get(0).cid);
		
		cdrs = ResolverDb.lookup("VVTYDTWGDNCMHW");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID20250404", cdrs.get(0).cid);
	}
	
	@Test
	public void byAnyBatch() throws ClassNotFoundException, SQLException, InvalidSmilesException {
		String[] ids = { "CCCCCCCNC1=C(O)C2=CC=CC=C2C(=O)C1=O", "NSC129153", "22158-43-6", "VVTYDTWGDNCMHW-UHFFFAOYSA-N", "VVTYDTWGDNCMHW" };
		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookup(Arrays.asList(ids));
		assertEquals(5, cdrs.size());
		cdrs.stream().forEach(r -> assertEquals("DTXCID20250404", r.cid));
	}
}
