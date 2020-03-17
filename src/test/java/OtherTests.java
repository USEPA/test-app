import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.AtomContainerSet;

import ToxPredictor.Application.Calculations.TaskStructureSearch;

public class OtherTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLoadFromSDF() throws Exception {
		TaskStructureSearch s = new TaskStructureSearch();
		AtomContainerSet ms = s.LoadFromSDF("data/BCF/BCF_prediction.sdf");
		assertNotNull(ms);
	}

}
