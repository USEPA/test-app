package ToxPredictor.MyDescriptors;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.junit.Test;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.AbstractMolecularDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.ChiClusterDescriptor;
import org.openscience.cdk.qsar.result.IDescriptorResult;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import com.google.gson.Gson;

import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Database.DSSToxRecord;


/**
* @author TMARTI02
*/
public class DescriptorTests {

	static SmilesGenerator sg=new SmilesGenerator(SmiFlavor.Canonical);
	static SmilesParser sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	DescriptorFactory df=new DescriptorFactory(false);
	double tol=1e-5;
	
	@Test
	/**
	 * Should have non zero MATS1v descriptor for compound with chlorine and carbon
	 * 
	 * Fix implemented:
	 * <dependency>​
	 * 		<groupId>gov.epa.webtest</groupId>​
	 * 		<artifactId>SystemData</artifactId>​
	 * 		<!--<version>1.2</version>-->​
	 * 		<version>1.3</version>​
	 * 	</dependency>​
	 * 
	 */
	public void testWhimWeightsForChlorineCompound() {

		try {

			IAtomContainer ac=TaskStructureSearch.lookupByCAS("50-29-3");
			String smiles=sg.create(ac);
			DescriptorData dd=new DescriptorData();
			int intResultTEST=df.CalculateDescriptors(ac, dd, true);
			
			for (int i=1;i<=8;i++) {
				String nameTEST="MATS"+i+"v";
				double valueTEST = dd.getValue(nameTEST);
				boolean match=valueTEST !=1.0;
				
				System.out.println(smiles+"\t"+nameTEST+"\t"+valueTEST+"\t"+match);
				assertEquals(true,match);
			}
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Test
	/**
	 * 
	 */
	public void testWe() {

		try {

			IAtomContainer ac=TaskStructureSearch.lookupByCAS("50-06-6");
			String smiles=sg.create(ac);
			DescriptorData dd=new DescriptorData();
			int intResultTEST=df.CalculateDescriptors(ac, dd, true);
						
			String nameTEST="We";
			double valueTEST = dd.getValue("We");
			double valueExpected=322;

			boolean match=Math.abs(valueTEST-valueExpected)<tol;
			System.out.println(smiles+"\t"+nameTEST+"\t"+valueExpected+"\t"+valueTEST+"\t"+match);
			assertEquals(true,match);
			
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	public static IAtomContainer loadMolecule(String smiles) {
		IAtomContainer molecule=null;
		try {
			molecule = sp.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return molecule;
	}
	
	
//	@Test
	public void testChiClusterDescriptors() {

		String smiles="[O-][N+](=O)OCC(CO[N+]([O-])=O)O[N+]([O-])=O";
		IAtomContainer molecule=loadMolecule(smiles);

		ChiClusterDescriptor ccd = new ChiClusterDescriptor();
		
		Hashtable<String,String>nameDict=new Hashtable<>();
		nameDict.put("SC-3", "xc3");
		nameDict.put("SC-4", "xc4");
		nameDict.put("VC-3", "xvc3");
		nameDict.put("VC-4", "xvc4");
		
		compareDescriptorsToCDK(smiles, molecule, ccd, nameDict);
	}


	public void compareDescriptorsToCDK(String smiles, IAtomContainer molecule, AbstractMolecularDescriptor cdkDescriptorCalculator,Hashtable<String, String> nameDict) {
		
		DescriptorData dd=new DescriptorData();
		int intResultTEST=df.CalculateDescriptors(molecule, dd, true);
		
		if(intResultTEST==-1) {
			System.out.println("descriptorCalculation failed to run");
			return;
		}
		
		IDescriptorResult idr = cdkDescriptorCalculator.calculate(molecule).getValue();
		String strCDKValuesString=idr.toString();
		String [] valuesCDK=strCDKValuesString.split(",");
		String [] names=cdkDescriptorCalculator.getDescriptorNames();
		
		
		System.out.println("smiles\tDescriptor\tCDK\tTEST\tMatches");
		
		for (int i=0;i<names.length;i++) {

			if(!nameDict.containsKey(names[i])) continue;
			
			try {
				
				String nameTEST=nameDict.get(names[i]);				
				double valueTEST = dd.getValue(nameTEST);
				
				double valueCDK=Double.parseDouble(valuesCDK[i]);
				double diff=Math.abs(valueTEST-valueCDK);
				boolean valueMatches=diff<tol;
				
				System.out.println(smiles+"\t"+nameTEST+"\t"+valuesCDK[i]+"\t"+valueTEST+"\t"+valueMatches);
				assertEquals(true,valueMatches);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
}
