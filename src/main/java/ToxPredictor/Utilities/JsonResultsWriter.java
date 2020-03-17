package ToxPredictor.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class JsonResultsWriter implements ResultsWriter {

	public JsonResultsWriter(String sdfFile) throws IOException {
		this(new FileWriter(sdfFile));
	}

	public JsonResultsWriter(Writer writer) throws IOException {
		
	}
	
	@Override
	public void writeResultValue(TESTPredictedValue v) throws IOException {
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
}
