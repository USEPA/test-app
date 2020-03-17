package ToxPredictor.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.codahale.metrics.MetricRegistryListener.Base;

public class CsvResultsWriter implements ResultsWriter {
	private CSVFormat csvFileFormat = CSVFormat.DEFAULT;
	private CSVPrinter csvFilePrinter = null;
	private static final Object[] FILE_HEADER = { "ID", "ExtId", "Endpoint", "Method", "SMILES", "ExpVal",
			"ExpActive", "PredVal", "PredActive", "Error" };
	private Writer writer;

	public CsvResultsWriter(String csvFile) throws IOException {
		this(new FileWriter(csvFile));
	}

	public CsvResultsWriter(Writer writer) throws IOException {
		csvFilePrinter = new CSVPrinter(writer, csvFileFormat);
		csvFilePrinter.printRecord(FILE_HEADER);
	}

	@Override
	public void writeResultValue(TESTPredictedValue v) throws IOException {
		List<String> rec = new ArrayList<String>();
		rec.add(v.id);
		rec.add(v.extId);
		rec.add(v.endpoint);
		rec.add(v.method);
		rec.add(v.smiles);
		rec.add(String.valueOf(v.expValMass));
		rec.add(String.valueOf(v.expActive));
		rec.add(String.valueOf(v.predValMass));
		rec.add(String.valueOf(v.predActive));
		rec.add(v.error);
		csvFilePrinter.printRecord(rec);
	}

	@Override
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
