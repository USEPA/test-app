import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Utilities.ResourceLoader;

public class MolLoadTests extends ResourceLoader {

	static WebTEST webTest = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		webTest = new WebTEST();
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

	@Test
    public void testLoadMolecules() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, IOException {
 
        Method method = WebTEST.class.getDeclaredMethod("loadMolecules", String.class, int.class);
        method.setAccessible(true);

        File smiFile = new File(getResourcePath("smiles.smi"));
        String smi = FileUtils.readFileToString(smiFile);
        AtomContainerSet ms = (AtomContainerSet) method.invoke(null, smi, TESTConstants.numFormatSMI);
        assertThat(ms, notNullValue());
        assertEquals(ms.getAtomContainerCount(), 4);
        
		for (int i = 0; i < ms.getAtomContainerCount(); i++) {
			AtomContainer m = (AtomContainer) ms.getAtomContainer(i);
			assertThat(m, notNullValue());
			System.out.println(m.toString());
		}
    }
	
	@Test
    public void testLoadMoleculesSet() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException, IOException {
 
        Method method = WebTEST.class.getDeclaredMethod("loadMolecules", Set.class, int.class);
        method.setAccessible(true);

        List<String> smi = FileUtils.readLines(new File(getResourcePath("smiles.smi")));
        
        AtomContainerSet ms = (AtomContainerSet) method.invoke(null, new HashSet<String>(smi), TESTConstants.numFormatSMILES);
        assertThat(ms, notNullValue());
        assertEquals(ms.getAtomContainerCount(), 4);
        
		for (int i = 0; i < ms.getAtomContainerCount(); i++) {
			AtomContainer m = (AtomContainer) ms.getAtomContainer(i);
			assertThat(m, notNullValue());
			System.out.println(m.toString());
		}
    }
}
