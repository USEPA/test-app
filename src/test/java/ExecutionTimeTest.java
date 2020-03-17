import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Stopwatch;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;

@Ignore
public class ExecutionTimeTest {
    
    private static final Logger logger = LogManager.getLogger(ExecutionTimeTest.class);

    private int[] allMethodsIds = new int[] { 
            TESTConstants.numChoiceHierarchicalMethod,
            TESTConstants.numChoiceFDAMethod, 
            TESTConstants.numChoiceSingleModelMethod,
            TESTConstants.numChoiceNearestNeighborMethod, 
            TESTConstants.numChoiceGroupContributionMethod,
            TESTConstants.numChoiceConsensus };

    /**
     * Method calculates prediction execution time for all endpoints using all methods. 
     * Then stores result into csv file.
     * @throws IOException
     */
    @Test
    public void logExecTime() {
        // exec times in miliseconds
        long[][] matrix = new long[allMethodsIds.length][TESTParams.endpoints.length];

        for (int i = 0; i < allMethodsIds.length; i++) {
            TESTParams[] params = TESTParams.createMatrix(allMethodsIds[i]);
            for (int j = 0; j < params.length; j++) {
                final Stopwatch sw = Stopwatch.createStarted();
                try {
                    runPredictionMethod(params[j]);
                    matrix[i][j] = sw.elapsed(TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    matrix[i][j] = -1;
                }
            }
        }
        
        try {
            saveExecTime(matrix);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        
        try {
            saveExecTimeMatrix(matrix);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void runPredictionMethod(TESTParams p) throws Exception {
        File inFile = new File(p.inputFilePath + "_prediction.sdf");
        File outFile = TestsBase.getOutFile(inFile, TESTConstants.getEndpoint(p.iEndpoint), TESTConstants.getMethod(p.iMethod));
        String outDir = outFile.getParent();
        if ( !Files.isDirectory(Paths.get(outDir)) )
            Files.createDirectories(Paths.get(outDir));
        
        logger.info("Processing file: {}, Endpoint: {}, Method: {} ...", 
                inFile.getCanonicalPath(), 
                TESTConstants.getEndpoint(p.iEndpoint), 
                TESTConstants.getMethod(p.iMethod));
        
        WebTEST.go(inFile.toString(), outFile.toString(), p.iEndpoint, p.iMethod);
    }

    private void saveExecTime(long matrix[][]) throws IOException {
        File outFile = TestsBase.getOutFile("exec-time-list");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                sb.append(String.format("%s,%s,%d\n", 
                        TESTConstants.getAbbrevMethod(TESTConstants.getMethod(allMethodsIds[i])), 
                        TESTConstants.getAbbrevEndpoint(TESTConstants.getEndpoint(TESTParams.endpoints[j])),
                        matrix[i][j]));
            }
        }
        
        FileUtils.writeStringToFile(outFile, sb.toString()); 
    }

    private void saveExecTimeMatrix(long matrix[][]) throws IOException {
        File outFile = TestsBase.getOutFile("exec-time-matrix");
        StringBuilder sb = new StringBuilder();
        sb.append("Method");
        for (int i = 0; i < matrix[0].length; i++) {
            sb.append(',');
            sb.append(TESTConstants.getAbbrevEndpoint(TESTConstants.getEndpoint(TESTParams.endpoints[i])));
        }
        sb.append("\n");
        
        
        for (int i = 0; i < matrix.length; i++) {
            sb.append(TESTConstants.getAbbrevMethod(TESTConstants.getMethod(allMethodsIds[i])));
            for (int j = 0; j < matrix[0].length; j++) {
                sb.append(',');
                sb.append(matrix[i][j]);
            }
            sb.append("\n");
        }

        FileUtils.writeStringToFile(outFile, sb.toString()); 
    }
    
    public static void main(String[] args) {
        new ExecutionTimeTest().logExecTime();
    }

}
