package ToxPredictor.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class XmlResultsWriter implements ResultsWriter {

	public XmlResultsWriter(String sdfFile) throws IOException {
		this(new FileWriter(sdfFile));
	}

	public XmlResultsWriter(Writer writer) throws IOException {
		
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
