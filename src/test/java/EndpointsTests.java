import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.TESTPredictedValue;

public class EndpointsTests {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_FHM_LC50_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceFHM_LC50, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_DM_LC50_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceDM_LC50, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_TP_IGC50_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceTP_IGC50, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_Rat_LD50_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceRat_LD50, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_GA_EC50_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceGA_EC50, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_BCF_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceBCF, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_ReproTox_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceReproTox, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_Mutagenicity_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceMutagenicity, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_ER_Binary_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceER_Binary, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_ER_LogRBA_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceER_LogRBA, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_BoilingPoint_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceBoilingPoint, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_VaporPressure_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceVaporPressure, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_MeltingPoint_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceMeltingPoint, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_FlashPoint_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceFlashPoint, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_Density_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceDensity, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_SurfaceTension_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceSurfaceTension, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_ThermalConductivity_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceThermalConductivity, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}

	@Test
	public void test_Viscosity_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceViscosity, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	
	@Test
	public void test_WaterSolubility_Consensus_TinySdf() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		List<TESTPredictedValue> result = WebTEST.go(inputFilePath, null, TESTConstants.numChoiceWaterSolubility, TESTConstants.numChoiceConsensus);
		assertNotNull(result);
		assertEquals(10, result.size());
	}
}
