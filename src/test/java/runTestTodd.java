import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb;

public class runTestTodd {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ArrayList<DSSToxRecord> cdrs = ResolverDb.lookupByName("AsPiRiN");
		assertTrue(cdrs.size() == 1);
		assertEquals("DTXCID50108", cdrs.get(0).cid);
		
		
		
	}

}
