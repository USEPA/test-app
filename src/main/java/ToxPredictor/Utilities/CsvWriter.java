package ToxPredictor.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.util.Strings;

public class CsvWriter {
	private CSVFormat csvFileFormat = CSVFormat.DEFAULT;
	private CSVPrinter csvFilePrinter = null;
	private Writer writer;

	public CsvWriter(String csvFile) throws IOException {
		this(new FileWriter(csvFile));
	}

	public CsvWriter(Writer writer) throws IOException {
		csvFilePrinter = new CSVPrinter(writer, csvFileFormat);
	}

	public void printField(String field) {
		try {
			csvFilePrinter.print(Strings.isEmpty(field) ? "" : field);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void newRecord() {
		try {
			csvFilePrinter.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (csvFilePrinter != null)
				csvFilePrinter.close();
		} catch (IOException e) {
			// Ignore
		}

		try {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			// Ignore
		}
	}
}
