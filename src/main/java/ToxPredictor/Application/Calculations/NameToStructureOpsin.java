package ToxPredictor.Application.Calculations;


import uk.ac.cam.ch.wwmm.opsin.NameToStructure;
import uk.ac.cam.ch.wwmm.opsin.OpsinResult;

public class NameToStructureOpsin {
	
		
	public static OpsinResult nameToSmiles(String name) {

		NameToStructure nameToStructure=NameToStructure.getInstance();
		
		OpsinResult res = nameToStructure.parseChemicalName(name);
		
		
		return res;
				
	}
	
	
	public static void main(String[] args) {
		
		
		String name="1,2,3-trimethyl benzene";
		OpsinResult res=NameToStructureOpsin.nameToSmiles(name);
		System.out.println(res.getSmiles());

	}
}
