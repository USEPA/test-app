import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.TESTPredictedValue;

public class QuickTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_BCF_Groups() throws Exception {
		String inputFilePath = "data/BCF/BCF_prediction.sdf";
		WebTEST.go(inputFilePath, TestsBase.getOutFile(inputFilePath).toString(), TESTConstants.numChoiceBCF, TESTConstants.numChoiceGroupContributionMethod);
	}
	
	@Test
	public void test_BP_Groups() throws Exception {
		String inputFilePath = "data/BP/BP_prediction.sdf";
		WebTEST.go(inputFilePath, TestsBase.getOutFile(inputFilePath).toString(), TESTConstants.numChoiceBoilingPoint, TESTConstants.numChoiceGroupContributionMethod);
	}
	
	@Test
	public void test_Density_Groups() throws Exception {
		String inputFilePath = "data/Density/Density_prediction.sdf";
		WebTEST.go(inputFilePath, TestsBase.getOutFile(inputFilePath).toString(), TESTConstants.numChoiceDensity, TESTConstants.numChoiceGroupContributionMethod);
	}
	
	@Test
	public void test_Mutagenicity_Groups() throws Exception {
		String inputFilePath = "data/Mutagenicity/Mutagenicity_prediction.sdf";
		WebTEST.go(inputFilePath, TestsBase.getOutFile(inputFilePath).toString(), TESTConstants.numChoiceMutagenicity, TESTConstants.numChoiceGroupContributionMethod);
	}
	
	@Test
	public void test_VP_Groups() throws Exception {
		String inputFilePath = "data/VP/VP_prediction.sdf";
		WebTEST.go(inputFilePath, TestsBase.getOutFile(inputFilePath).toString(), TESTConstants.numChoiceVaporPressure, TESTConstants.numChoiceGroupContributionMethod);
	}
	
	@Test
	public void test_VP_Consensus() throws Exception {
		String inputFilePath = "data/VP/VP_prediction.sdf";
		WebTEST.go(inputFilePath, TestsBase.getOutFile(inputFilePath).toString(), TESTConstants.numChoiceVaporPressure, TESTConstants.numChoiceConsensus);
	}
	
	@Test
	public void test_tinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceBCF, TESTConstants.numChoiceGroupContributionMethod);
		assertNotNull(result);
		assertEquals(10, result.size());
		assertEquals("56-23-5", result.get(0).id);
		assertEquals(1.030, result.get(0).expValLogMolar.doubleValue(), 0.01);
		assertEquals(1.913, result.get(0).predValLogMolar.doubleValue(), 0.01);
	}
	
	@Test
	public void test_MOL() throws Exception {
		String mol = FileUtils.readFileToString(new File("data/1.mol"));
		List<TESTPredictedValue> result = WebTEST.go(mol, TESTConstants.numFormatMOL, TESTConstants.numChoiceBCF, TESTConstants.numChoiceGroupContributionMethod);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1.030, result.get(0).expValLogMolar.doubleValue(), 0.01);
		assertEquals(1.913, result.get(0).predValLogMolar.doubleValue(), 0.01);
	}
	
	@Test
	public void test_SMILES() throws Exception {
		String smiles = "ClC(Cl)(Cl)Cl";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceBCF, TESTConstants.numChoiceGroupContributionMethod);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1.030, result.get(0).expValLogMolar.doubleValue(), 0.01);
		assertEquals(1.913, result.get(0).predValLogMolar.doubleValue(), 0.01);
	}
}
