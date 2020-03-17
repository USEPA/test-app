import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MatrixTests extends MethodsTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAllMethods() throws Exception {
		testHierarchicalMethod();
//		testFDAMethod();//not needed in new version
		testSingleModelMethod();
		testNearestNeighborMethod();
		testGroupContributionMethod();
		testConsensusMethod();
	}
}
