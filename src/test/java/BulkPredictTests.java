import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import ToxPredictor.Utilities.ResourceLoader;
import gov.epa.api.BatchPredictionQuery;
import gov.epa.api.BulkPredictionQuery;
import gov.epa.resources.BulkPredictResource;

public class BulkPredictTests extends ResourceLoader {

	@Test
	public void testBulkPredict() throws Exception {
		BulkPredictResource res = new BulkPredictResource();
		BulkPredictionQuery bpq = new BulkPredictionQuery();
		bpq.setQuery("CC(C)(C1C=CC(O)=CC=1)C1C=CC(O)=CC=1");
		bpq.setFormat("SMILES");
		bpq.setEndpoints(new HashSet<String>(Arrays.asList("LC50")));
		bpq.setMethods(new HashSet<String>(Arrays.asList("consensus")));
		bpq.setReportTypes(new HashSet<String>(Arrays.asList("JSON")));
		Response r = res.bulkPredict(bpq);
		assertEquals(r.getStatus(), 200);
	}

	@Test
	public void testBatchPredict() throws Exception {
		BulkPredictResource res = new BulkPredictResource();
		BatchPredictionQuery bpq = new BatchPredictionQuery();
		File smiFile = new File(getResourcePath("smiles.smi"));
		List<String> smi = FileUtils.readLines(smiFile);
		bpq.setStructures(new HashSet<String>(smi));
		bpq.setFormat("SMILES");
		bpq.setEndpoints(new HashSet<String>(Arrays.asList("LC50")));
		bpq.setMethods(new HashSet<String>(Arrays.asList("consensus")));
		bpq.setReportTypes(new HashSet<String>(Arrays.asList("JSON")));
		Response r = res.batchPredict(bpq);
		assertEquals(r.getStatus(), 200);
	}
}
