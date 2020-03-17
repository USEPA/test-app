import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ToxPredictor.Application.TESTConstants;

public class MethodsTests extends TestsBase {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHierarchicalMethod() throws Exception {
		testMatrix(TESTParams.createMatrix(TESTConstants.numChoiceHierarchicalMethod), TESTConstants.ChoiceHierarchicalMethod);
	}

//	@Test
//	public void testFDAMethod() throws Exception {
//		testMatrix(TESTParams.matrixFDAMethod, "FDAMethod");
//	}
	
	@Test
	public void testSingleModelMethod() throws Exception {
		testMatrix(TESTParams.createMatrix(TESTConstants.numChoiceSingleModelMethod), TESTConstants.ChoiceSingleModelMethod);
	}
	
	@Test
	public void testNearestNeighborMethod() throws Exception {
		testMatrix(TESTParams.createMatrix(TESTConstants.numChoiceNearestNeighborMethod), TESTConstants.ChoiceNearestNeighborMethod);
	}
	
	@Test
	public void testGroupContributionMethod() throws Exception {
		testMatrix(TESTParams.createMatrix(TESTConstants.numChoiceGroupContributionMethod), TESTConstants.ChoiceGroupContributionMethod);
	}
	
	@Test
	public void testConsensusMethod() throws Exception {
		testMatrix(TESTParams.createMatrix(TESTConstants.numChoiceConsensus), TESTConstants.ChoiceConsensus);
	}
}
