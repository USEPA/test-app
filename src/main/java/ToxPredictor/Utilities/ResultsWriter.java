package ToxPredictor.Utilities;

import java.io.IOException;

/**
 * Implementing classes provide ability to write results in respective file
 * formats
 * 
 * @author valt
 *
 */
public interface ResultsWriter {
	/**
	 * Writes single predicted value
	 * 
	 * @param v
	 */
	public void writeResultValue(TESTPredictedValue v) throws IOException;

	/**
	 * Closes the underlying stream
	 */
	public void close();
}
