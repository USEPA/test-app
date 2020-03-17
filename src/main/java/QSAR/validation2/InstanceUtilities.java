package QSAR.validation2;

import wekalite.*;
import java.util.*;
import ToxPredictor.Utilities.Utilities;

public class InstanceUtilities {

	
	/**
	 * This method takes a hashtable of hashtables and converts into instances
	 */
	public Instances createInstances(Hashtable ht, String[] varnames) {

		
		Instances instances=new Instances();
		instances.setAttributes(varnames);

		String CAS=(String)ht.get("CAS");
		double Tox=(Double)ht.get("Tox");
		
		double []descriptors=new double [varnames.length];
		
		for (int i=0;i<descriptors.length;i++) {
			descriptors[i]=(Double)ht.get(varnames[i]);
		}
			
		Instance instance =new Instance();
		instance.setAttributes(varnames);
		instance.setDescriptors(descriptors);
		instance.setName(CAS);
		instance.setToxicity(Tox);
	
		instances.addInstance(instance);
		
		return instances;
		 
	}


}
