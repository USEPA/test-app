package ToxPredictor.Application.Calculations;


import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;

public class NameToStructureOpsin {
	
		
	public static String nameToSmiles(String name) {

		NameToStructure nameToStructure=NameToStructure.getInstance();
		
		OpsinResult res = nameToStructure.parseChemicalName(name);
		
		if ( res == null || res.getSmiles() == null ) {
			return null;
		}
		
		return res.getSmiles();
				
	}
	
	
	public static void main(String[] args) {
		
		
		String name="1,2,3-trimethyl benzene";
		String smiles=NameToStructureOpsin.nameToSmiles(name);
		System.out.println(smiles);

	}
}
