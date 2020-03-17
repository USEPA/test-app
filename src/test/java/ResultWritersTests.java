import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ToxPredictor.Application.RunParams;
import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.CsvResultsWriter;
import ToxPredictor.Utilities.FileUtils;
import ToxPredictor.Utilities.TESTPredictedValue;

public class ResultWritersTests {

	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCsvResultsWriter() throws IOException {
		String csvFile = Paths.get(testFolder.newFolder("ResultWritersTests").getAbsolutePath(), "results.csv").toString();
		CsvResultsWriter csv = new CsvResultsWriter(csvFile);
		
		TESTPredictedValue v = new TESTPredictedValue("000-00-0", "endpoint", "method");
		v.extId = "DSSTox123";
		csv.writeResultValue(v);
		
		v.endpoint = TESTConstants.ChoiceBCF;
		v.method = TESTConstants.ChoiceConsensus;
		csv.writeResultValue(v);
		
		v.expActive = true;
		v.expValLogMolar = 1.0;
		
		v.predActive = false;
		v.predValLogMolar = 2.0;
		
		csv.writeResultValue(v);
		
		csv.close();
		
		assertTrue(new File(csvFile).length() > 0);
	}
	
	@Test
	public void testCsvOutputBP() throws Exception {
		String inputFilePath = "data/VP_prediction_small.sdf";
		String csvFile = FileUtils.replaceExtension(TestsBase.getOutFile(inputFilePath).toString(), ".csv");
		WebTEST.go(inputFilePath, csvFile, TESTConstants.numChoiceBoilingPoint, TESTConstants.numChoiceConsensus);
	}
	
	@Test
	public void testCsvOutputAll() throws Exception {
		String inputFilePath = "data/tiny.sdf";
		String csvFile = FileUtils.replaceExtension(TestsBase.getOutFile(inputFilePath).toString(), ".csv");
		WebTEST.go(inputFilePath, csvFile, TESTConstants.numChoiceBoilingPoint, TESTConstants.numChoiceConsensus);
		
		RunParams p = new RunParams();
		p.inputFilePath = inputFilePath;
		p.outputFilePath = csvFile;
		p.endpoints = TESTConstants.getFullEndpoints(null);
		p.methods = TESTConstants.getFullMethods(null);
		WebTEST.go(p);
	}
}
