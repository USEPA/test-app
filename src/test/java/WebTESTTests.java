import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import org.junit.Test;
import org.openscience.cdk.AtomContainerSet;
import org.openscience.cdk.exception.InvalidSmilesException;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.ResourceLoader;

public class WebTESTTests extends ResourceLoader {

	@Test
	public void loadSMILES() throws ClassNotFoundException, SQLException, InvalidSmilesException {
		AtomContainerSet s = WebTEST.LoadFromSMILES("CC(C)(CO)N1CC1C(O)=O");
		
		assertTrue(s.getAtomContainerCount() == 1);
		assertEquals("331416-38-7", s.getAtomContainer(0).getProperty("CAS"));
	}

	@Test
	public void loadSMI() throws ClassNotFoundException, SQLException, InvalidSmilesException, IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(getResourcePath("smiles.smi")));
		String smi = new String(encoded, Charset.defaultCharset());
		AtomContainerSet s = WebTEST.LoadFromSMI(smi);
		
		assertEquals(4, s.getAtomContainerCount());
		assertEquals("71-43-2", s.getAtomContainer(0).getProperty("CAS"));
		assertEquals("91-20-3", s.getAtomContainer(2).getProperty("CAS"));
		assertEquals("57-88-5", s.getAtomContainer(3).getProperty("CAS"));
	}
}
