package ToxPredictor.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SdfResultsWriter implements ResultsWriter {

	public SdfResultsWriter(String sdfFile) throws IOException {
		this(new FileWriter(sdfFile));
	}

	public SdfResultsWriter(Writer writer) throws IOException {
		
	}
	
	@Override
	public void writeResultValue(TESTPredictedValue v) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
