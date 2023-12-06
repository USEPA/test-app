package ToxPredictor.Application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalculationParameters {

	public String outputFile;
	public String smilesColumn; // For CSV input files only
	public List<String> endpoints;
	public List<String> methods;
	// do not keep results in memory for command line tools
	public boolean discardResults;
	public Set<WebReportType> reportTypes = new HashSet<>();
	
    public CalculationParameters() {
        super();
    }

    public CalculationParameters(String outputFile, String smilesColumn, List<String> endpoints,
            List<String> methods, Set<WebReportType> reportTypes) {
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
        this.endpoints = Arrays.asList(endpoint);
        this.methods = Arrays.asList(method);
        this.reportTypes = reportTypes;
    }

	public String toString() {
		return String.format("Output: %s; SMILES Column: %s; Report Types: %s; Endpoints: %s; Methods: %s", 
		        outputFile, smilesColumn, reportTypes.toString(), endpoints.toString(), methods.toString());
	}

}
