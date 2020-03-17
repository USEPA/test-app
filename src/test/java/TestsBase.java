import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;

public class TestsBase {
	public static File getOutFile(String inputFilePath)
	{
		return getOutFile(new File(inputFilePath), null, null);
	}
	
	public static File getOutFile(File inputFilePath, String dir, String endp)
	{
		return Paths.get("test-results", StringUtils.isEmpty(dir) ? "" : dir, StringUtils.isEmpty(endp) ? "" : endp, FilenameUtils.removeExtension(inputFilePath.getName()) + ".txt").toFile();
	}
	
	public static void testMatrix(TESTParams[] matrix, String outDir) throws Exception {
		for ( TESTParams p: matrix ) {
			// Run on validation set
			File inFilePath = new File(p.inputFilePath + "_prediction.sdf"); 
			File outFilePath = TestsBase.getOutFile(inFilePath, TESTConstants.getEndpoint(p.iEndpoint), TESTConstants.getMethod(p.iMethod));
			outDir = outFilePath.getParent();
			if ( !Files.isDirectory(Paths.get(outDir)) )
				Files.createDirectories(Paths.get(outDir));
			
			
			System.out.println("\r\n"+inFilePath.getName()+"\t"+TESTConstants.getMethod(p.iMethod));//so we know where we are at in tests
			WebTEST.go(inFilePath.toString(), outFilePath.toString(), p.iEndpoint, p.iMethod);
			
			// Run on training set
			inFilePath = new File(p.inputFilePath + "_training.sdf");
			outFilePath = TestsBase.getOutFile(inFilePath, TESTConstants.getEndpoint(p.iEndpoint), TESTConstants.getMethod(p.iMethod));
			
			System.out.println("\r\n"+inFilePath.getName()+"\t"+TESTConstants.getMethod(p.iMethod));//so we know where we are at in tests
			WebTEST.go(inFilePath.toString(), outFilePath.toString(), p.iEndpoint, p.iMethod);
		}
	}
}
