package ToxPredictor.Application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunParams {
	public String inputFilePath;
	public String smilesColumn;
	public String outputFilePath;
	public List<String> endpoints;
	public List<String> methods;
	public boolean discardResults;
	public Set<WebReportType> reportTypes = new HashSet<>();
}
