package ToxPredictor.Application.Calculations;


import java.util.Vector;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST4;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.Utilities.TESTPredictedValue;
import ToxPredictor.Utilities.Utilities;

public class RunFromJava {

	static void runTestIdentifiers() {
		
//		Possible identifiers: CAS, SMILES, Name, InChi, InChiKey, or DTXSID
		
		
		String strBenzeneCAS_withDashes="71-43-2";
		String strBenzeneCAS_noDashes="71432";
		String strBenzeneCAS_noDasheswithZeros="000071432";
		String strBenzeneCAS_withDasheswithZeros="000071-43-2";
		String smilesButene="CC=CC";
		String validSmilesNotInDB="COCOCOCCCCCOCCCCCOCCCOCCCCOCC";
		String nameBenzene="benzene";
		String synonymTNT="tnt";
		String synonymPFOS="pfos";
		String nameToStructure="4,5,6-tribromobenzene";
		String nameToStructureBad="1,2,3-dibromobenzene";
		String nameToStructureNotInDB="1-bromo,2-chloro,3-fluoro,4-iodo,5-methylbenzene";
		String nameBad="sd;fls';fl';dsl";
		String nameAmbiguous="xylenes";
		String inchiKey="UICBCXONCUFSOI-UHFFFAOYSA-N";
		String inchiKey1="UICBCXONCUFSOI";
		String sid="DTXSID2020008";
		String nameWithQuotes="2,2',2\"-NITRILOTRETHANOL";
		String nameWithQuotesNotInDB="Azirino(2',3':3,4)pyrrolo(1,2-a)indole-4,7-dione, 6-amino-8-(((aminocarbonyl)oxy)methyl)-1,1a,2,8,8a,8b-hexahydro-8a-methoxy-5-methyl-, (1aS,8S,8aR,8bS)-";
		String inchi="InChI=1S/C7H4BrClFI/c1-3-2-4(8)5(9)6(10)7(3)11/h2H,1H3";
		String inchiNotInDB="InChI=1S/C2H2F4/c3-1-2(4,5)6/h1H2";
		
		String[] ids = { strBenzeneCAS_withDashes, strBenzeneCAS_noDashes, 
				strBenzeneCAS_noDasheswithZeros, strBenzeneCAS_withDasheswithZeros,
				smilesButene, validSmilesNotInDB,
				nameBenzene, synonymTNT, synonymPFOS, nameToStructure, nameToStructureBad,
				nameToStructureNotInDB, nameBad, nameAmbiguous,
				inchiKey, inchiKey1, sid, nameWithQuotes,
				nameWithQuotesNotInDB,
				inchi,inchiKey ,inchiNotInDB};


		String endpoint=TESTConstants.abbrevChoiceFHM_LC50;//or just use "LC50"; 		
		String method=TESTConstants.abbrevChoiceConsensus;//or just use "consensus";

		for (String id:ids) {
			TESTPredictedValue tpv=WebTEST4.run(id, endpoint, method);	

			if (tpv.error.contentEquals("")) {
				System.out.println(Utilities.toJson(tpv));//Print out all data
//				System.out.println(tpv.casrn+"\t"+tpv.predValLogMolar);
				
			} else 
				System.out.println(tpv.error);
		}
	}
	
	static void runIdentifier() {
		String id="benzene";
				
		String endpoint=TESTConstants.abbrevChoiceFHM_LC50;//or just use "LC50"; 		
		String method=TESTConstants.abbrevChoiceConsensus;//or just use "consensus";

		TESTPredictedValue tpv=WebTEST4.run(id, endpoint, method);	

		if (tpv.error.contentEquals("")) {
			System.out.println(Utilities.toJson(tpv));//Print out all data
//			System.out.println(tpv.casrn+"\t"+tpv.predValLogMolar);
			
		} else 
			System.out.println(tpv.error);

	}
		
	static void runDescriptors() {
		String id="benzene";
		DescriptorData dd=WebTEST4.runDescriptors(id);
		//System.out.println(Utilities.toJson(dd));//display descriptors as Json
		
		if (dd.Error.contentEquals("")) {
			Vector<String>vals=dd.toStringVector("\t");		
			for (String val:vals) System.out.println(val);//print as rows of name/value pairs			
		} else {
			System.out.println(dd.Error);
		}
		
	}
	
	public static void main(String[] args) {
		
		runTestIdentifiers();
//		runIdentifier();			
//		runDescriptors();
		
		
	}
}

