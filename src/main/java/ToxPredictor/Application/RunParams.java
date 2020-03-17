package ToxPredictor.Application;

import java.util.HashSet;
import java.util.Set;

public class RunParams {
	public String inputFilePath;
	public String smilesColumn;
	public String outputFilePath;
	public String[] endpoints;
	public String[] methods;
	public boolean discardResults;
	public Set<WebReportType> reportTypes = new HashSet<>();
}
