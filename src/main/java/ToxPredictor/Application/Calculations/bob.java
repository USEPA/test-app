package ToxPredictor.Application.Calculations;

import java.util.ArrayList;

import ToxPredictor.Database.DSSToxRecord;
import ToxPredictor.Database.ResolverDb2;

/**
* @author TMARTI02
*/
public class bob {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<DSSToxRecord>recs=ResolverDb2.lookupByDTXSID("DTXSID10938865");
		
		String smiles=recs.get(0).smiles;
		System.out.println(smiles);

	}

}
