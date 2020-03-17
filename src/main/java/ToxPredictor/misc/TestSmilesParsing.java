package ToxPredictor.misc;


import java.io.BufferedReader;
import java.io.FileReader;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.silent.Bond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class TestSmilesParsing {

	IRingSet findRings(IAtomContainer m) {
		double time3 = System.currentTimeMillis() / 1000.0;
		// find all rings
	     try {
	    	 AllRingsFinder arf = new AllRingsFinder();
	    	 IRingSet rs = arf.findAllRings(m);
	    	 
	    	 double time4 = System.currentTimeMillis() / 1000.0;

//	    	 System.out.println((time4-time3));
	    	 
	    	 return rs;

	     } catch (CDKException e) {
	         // molecule was too complex, handle error
	    	 return null;
	     }
		
	}
	
	
	IAtomContainer parseSmiles(String smiles) {
		
		try {
		     
		     SmilesParser   sp  = new SmilesParser(SilentChemObjectBuilder.getInstance());
		     IAtomContainer m   = sp.parseSmiles(smiles);
		     return m;
		     
		 } catch (InvalidSmilesException e) {
		     System.err.println(e.getMessage());
		     return null;
		 }
		
		
	}
	
	
	void goThroughFile() {
		try {
			BufferedReader br=new BufferedReader(new FileReader("ToxPredictor/bad chemicals.smi"));
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				String smiles=Line.substring(0, Line.indexOf("\t"));
				
				IAtomContainer m=this.parseSmiles(smiles);
				
				IRingSet rs=this.findRings(m);
				
//				System.out.println(m.getAtomCount()+"\t"+rs.getAtomContainerCount());
			
				ALOGPDescriptor a=new ALOGPDescriptor();
				DescriptorValue dv=a.calculate(m);
				String []names=a.getDescriptorNames();
//				System.out.println(dv.getValue());
//				String []names=dv.getParameterNames();

				
				
//				for (int i=0;i<names.length;i++) {
//					System.out.println(names[i]);
//				}
				
			}
			
			br.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	
	public static void main(String[] args) {


		TestSmilesParsing tsp=new TestSmilesParsing();
		tsp.goThroughFile();
		
		

	}

}
