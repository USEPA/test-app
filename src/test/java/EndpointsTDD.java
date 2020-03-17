import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.TESTPredictedValue;

public class EndpointsTDD {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_FHM_LC50_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceFHM_LC50, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_DM_LC50_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceDM_LC50, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_TP_IGC50_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceTP_IGC50, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_Rat_LD50_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceRat_LD50, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_GA_EC50_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceGA_EC50, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_BCF_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceBCF, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_ReproTox_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceReproTox, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_Mutagenicity_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceMutagenicity, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_ER_Binary_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceER_Binary, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_ER_LogRBA_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceER_LogRBA, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_BoilingPoint_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceBoilingPoint, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_VaporPressure_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceVaporPressure, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_MeltingPoint_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceMeltingPoint, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_FlashPoint_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceFlashPoint, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_Density_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceDensity, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_SurfaceTension_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceSurfaceTension, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_ThermalConductivity_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceThermalConductivity, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	public void test_Viscosity_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceViscosity, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
	
	@Test
	public void test_WaterSolubility_Consensus() throws Exception {
		String smiles = "CC12CCC3C(C1CC(C2O)O)CCC4=C3C=CC(=C4)O";
		List<TESTPredictedValue> result = WebTEST.go(smiles, TESTConstants.numFormatSMILES, TESTConstants.numChoiceWaterSolubility, TESTConstants.numChoiceConsensus, WebReportType.getAll());
		assertNotNull(result);
		assertEquals(1, result.size());
	}
}
