package ToxPredictor.Application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CalculationParameters {

	public String outputFile;
	public String smilesColumn; // For CSV input files only
	public String[] endpoints;
	public String[] methods;
	// do not keep results in memory for command line tools
	public boolean discardResults;
	public Set<WebReportType> reportTypes = new HashSet<>();
	
    public CalculationParameters() {
        super();
    }

    public CalculationParameters(String outputFile, String smilesColumn, String[] endpoints,
            String[] methods, Set<WebReportType> reportTypes) {
        super();
        this.outputFile = outputFile;
        this.smilesColumn = smilesColumn;
        this.endpoints = endpoints;
        this.methods = methods;
        this.reportTypes = reportTypes;
    }

    public CalculationParameters(String outputFile, String smilesColumn, String endpoint,
            String method, Set<WebReportType> reportTypes) {
        super();
        this.outputFile = outputFile;
        this.smilesColumn = smilesColumn;
        this.endpoints = new String[] { endpoint };
        this.methods = new String[] { method };
        this.reportTypes = reportTypes;
    }

	public String toString() {
		return String.format("Output: %s; SMILES Column: %s; Report Types: %s; Endpoints: %s; Methods: %s", 
		        outputFile, smilesColumn, reportTypes.toString(), Arrays.toString(endpoints), Arrays.toString(methods));
	}

}
