package ToxPredictor.MyDescriptors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Hashtable;

import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.Calculations.TaskStructureSearch;

/**
* @author TMARTI02
*/
public class FragmenterTests {

	
	SmilesGenerator sg=new SmilesGenerator(SmiFlavor.Canonical);
	SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	DescriptorFactory df=new DescriptorFactory(false);
	double tol=1e-5;

	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	
//	@Test	
	public void runDoug1() {

		String smiles="CCO";
		Hashtable<String,Double>htDesired=new Hashtable<>();
		htDesired.put("-OH [aliphatic attach]",2.0);
		htDesired.put("-CH3 [aliphatic attach]",1.0);
		htDesired.put("-CH2- [aliphatic attach]",1.0);
		IAtomContainer ac=DescriptorTests.loadMolecule(smiles);


		doFragmentTest(htDesired, ac);
		
//		System.out.println(gson.toJson(dd));


	}
	
	
//	@Test	
	public void runDoug2() {

		String smiles="CCO";
		Hashtable<String,Double>htDesired=new Hashtable<>();
		htDesired.put("-OH [aliphatic attach]",1.0);
		htDesired.put("-CH3 [aliphatic attach]",1.0);
		htDesired.put("-CH2- [aliphatic attach]",1.0);
		IAtomContainer ac=DescriptorTests.loadMolecule(smiles);

		doFragmentTest(htDesired, ac);
		
//		System.out.println(gson.toJson(dd));


	}
	
	@Test	
	public void run2232_08_8() {

		IAtomContainer ac=TaskStructureSearch.lookupByCAS("2232-08-8");

		
		Hashtable<String,Double>htDesired=new Hashtable<>();
		htDesired.put("-OH [aliphatic attach]",1.0);
		htDesired.put("-CH3 [aliphatic attach]",1.0);
		htDesired.put("-CH2- [aliphatic attach]",1.0);
		

		doFragmentTest(htDesired, ac);
		
//		System.out.println(gson.toJson(dd));


	}




	private void doFragmentTest(Hashtable<String, Double> htDesired, IAtomContainer ac) {
		DescriptorData dd=new DescriptorData();
		
		int intResultTEST=df.CalculateDescriptors(ac, dd, true);
		
		Hashtable<String,Double>htActual=new Hashtable<>();
		
		for(String fragment:dd.FragmentList.keySet()) {
			double count=dd.FragmentList.get(fragment);
			if(count==0) continue;
			htActual.put(fragment, count);
//			System.out.println(fragment+"\t"+count);
		}
		
		assertEquals(htDesired.size(), htActual.size());
		
		for(String fragment:htDesired.keySet()) {
			
			if(!htActual.containsKey(fragment)) {
				fail(fragment+"\tnot present");
				break;
			}
			
			double countDesired=htDesired.get(fragment);
			double countActual=htActual.get(fragment);
			
			if(Math.abs(countDesired-countActual)>0) {
				fail(fragment+"\tCount diff:\t"+countDesired+"\t"+countActual);	
				break;
			}
			
		}
	}
}
