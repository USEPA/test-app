import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ToxPredictor.Utilities.HtmlUtils;
import ToxPredictor.Utilities.ReflectUtils;
import ToxPredictor.Utilities.ResourceLoader;

public class HtmlToPdfTests extends ResourceLoader {
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHtmlToXhtml() throws IOException {
		String html = getResourcePath("hello.html");
		String xhtml = Paths.get(testFolder.newFolder(ReflectUtils.getMethodName()).getAbsolutePath(), "hello.xhtml").toString();
		HtmlUtils.HtmlToXhtml(html, xhtml);
		assertTrue(new File(xhtml).length() > 0);
	}
	
	@Test
	public void testHtmlToPdf() throws IOException {
		String html = getResourcePath("hello.html");
		String pdf = Paths.get(testFolder.newFolder(ReflectUtils.getMethodName()).getAbsolutePath(), "hello.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
	}
	
	@Test
	public void testMainReportToPdf() throws IOException {
		String html = getResourcePath("reports/Batch_Fathead_minnow_LC50_(96_hr)_Consensus_1.html");
		String pdf = Paths.get(testFolder.newFolder(ReflectUtils.getMethodName()).getAbsolutePath(), "Batch_Fathead_minnow_LC50_(96_hr)_Consensus_1.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
	}

	@Test
	public void testReportWithImageToPdf() throws IOException {
		String html = getResourcePath("reports/ToxRuns/ToxRun_RC12/StructureData/AssignedFragments.html");
		String pdf = Paths.get(testFolder.newFolder(ReflectUtils.getMethodName()).getAbsolutePath(), "AssignedFragments.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
	}
	
	@Test
	public void testReportsToPdfs() throws IOException {
		String testFolderPath = testFolder.newFolder(ReflectUtils.getMethodName()).getAbsolutePath();
		
		// Descriptors1276.html
		String html = getResourcePath("reports/ToxRuns/ToxRun_RC12/Fathead minnow LC50 (96 hr)/ClusterFiles/Descriptors1276.html");
		String pdf = Paths.get(testFolderPath, "Descriptors1276.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
		
		// DescriptorsGroupContribution.html
		html = getResourcePath("reports/ToxRuns/ToxRun_RC12/Fathead minnow LC50 (96 hr)/ClusterFiles/DescriptorsGroupContribution.html");
		pdf = Paths.get(testFolderPath, "DescriptorsGroupContribution.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
		
		// PredictionResultsConsensus.html
		html = getResourcePath("reports/ToxRuns/ToxRun_RC12/Fathead minnow LC50 (96 hr)/PredictionResultsConsensus.html");
		pdf = Paths.get(testFolderPath, "PredictionResultsConsensus.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
		
		// AssignedFragments.html
		html = getResourcePath("reports/ToxRuns/ToxRun_RC12/StructureData/AssignedFragments.html");
		pdf = Paths.get(testFolderPath, "AssignedFragments.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
		
		// DescriptorData.html
		html = getResourcePath("reports/ToxRuns/ToxRun_RC12/StructureData/DescriptorData.html");
		pdf = Paths.get(testFolderPath, "DescriptorData.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
		
		// Estates.html
		html = getResourcePath("reports/ToxRuns/ToxRun_RC12/StructureData/Estates.html");
		pdf = Paths.get(testFolderPath, "Estates.pdf").toString();
		HtmlUtils.HtmlToPdf(html, pdf);
		assertTrue(new File(pdf).length() > 0);
	}
}
