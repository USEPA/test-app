package wekalite;

//import java.util.Hashtable;
//import java.util.ListIterator;
import java.util.ArrayList;

public class Instance {
	
	

	// private Hashtable <String,Object>htDescriptors;
	private double [] descriptors;// store all as Strings- faster
												// than storing name as String
												// and rest as Double
	private String [] attributes;// list of attributes in order

	
	private String name;
	private double tox;
	
	public Instance() {
	}
	public Instance(Instance instance) {
		this.attributes=instance.attributes.clone();
		this.descriptors=instance.descriptors.clone();
		this.name=instance.name;
		this.tox=instance.tox;
	}

	public String[] getAttributes() {
		return attributes;
	}

	public int getAttributeNumber(String descriptorName)  {
		
		for (int i=0;i<numAttributes();i++) {
			if (attribute(i).equals(descriptorName)) {
				return i;
			}
		}
		return -1;
	}
	
	public void setAttributes(String [] attributes) {
		this.attributes = attributes;
	}

	@Override
	public Object clone()  {
		Instance instance = new Instance();
		
		instance.name=name;
		instance.tox=tox;
		instance.descriptors = (double []) this.descriptors.clone();
		instance.attributes = (String []) this.attributes.clone();
		return instance;
	}

	public double [] getDescriptors() {
		return this.descriptors;
	}

	public void setDescriptors(double [] llDescriptors) {
		this.descriptors = llDescriptors;
	}

	public String getInstanceValues() {
		return getName()+"\t"+getToxicity()+"\t"+getDescriptorsValues();
	}
	
	/**
	 * Reports first descriptor where they dont match
	 * 
	 * @param instance1
	 * @param instance2
	 */
	public static void Compare(Instance instance1,Instance instance2) {
		
		if (instance1.descriptors.length!=instance2.descriptors.length) {
			System.out.println("Mismatch in descriptor length:\t"+instance1.descriptors.length+"\t"+instance2.descriptors.length);
			return;
		}
		
		
		double tol=1e-3; //0.001 %
		
		for (int i=0;i<instance1.descriptors.length;i++) {
			
			double d1=instance1.descriptors[i];
			double d2=instance2.descriptors[i];
			
			if (d1==0) {
				if (Math.abs(d1-d2)*100>tol) {
					System.out.println(instance1.name+instance1.attribute(i)+"\t"+d1+"\t"+d2);
					break;
				}
				
			} else {
				
//				System.out.println(instance1.name+"\t"+d1+"\t"+d2);
				
				if (Math.abs(d1-d2)/d1*100>tol) {
					System.out.println(instance1.name+instance1.attribute(i)+"\t"+d1+"\t"+d2);
					break;
				}
			}
		}
		
	}
	
	
	/**
	 * Note doesnt include CAS and Tox
	 * @return
	 */
	public String getDescriptorsValues() {
		String str="";
//		ListIterator<Double> it = getDescriptorsIterator();
//		while (it.hasNext()) {
//			str+=it.next();
//			if (it.hasNext())
//				str+="\t";
//		}
				
		for (int i=0;i<numAttributes();i++) {
			str+=value(i);
			if (i<numAttributes()-1)
				str+="\t";
		}
		return str;
	}
	
	@Override
	public String toString() {
		return getDescriptorsValues();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String Name) {
		this.name=Name;
	}


	public double getToxicity() {
		return tox;
	}

	public void setToxicity(double tox) {
		this.tox=tox;
	}

	public void Standardize(double[] offsets, double[] scales) {
		// offset the properties by the mean and scale by standard deviations of
		// all chemicals
		

        for (int i=0;i<numAttributes();i++) {
        	double val=value(i);
        	
			if (scales[i] > 0) {
				val = (val - offsets[i]) / scales[i];
			} else {
				val = (val - offsets[i]);
			}
			this.setValue(i, val);
        }

		
	}

	public void UnStandardize(double[] offsets, double[] scales) {

	    for (int i=0;i<numAttributes();i++) {
        	double val=value(i);
        	
			if (scales[i] > 0) {
				val = val * scales[i] + offsets[i];
			} else {
				val = val + offsets[i];
			}
			this.setValue(i, val);
        }

	}


	public void setValue(int i,double value) {
		descriptors[i]=value;
	}

	public double value(int i) {
		return descriptors[i];
	}

	public double value(String descriptorName)  {
		for (int i=0;i<numAttributes();i++) {
			String attname=attribute(i);
			if (attname.equals(descriptorName)) {
				return value(i);
			}
 		}
		return -9999;
	}

	/**
	 * Returns CAS + Tox + descriptor names
	 */
	public String getAttributeNames() {
		String names="";
		for (int i=0;i<numAttributes();i++) {
			names+=attribute(i);
			if (i<numAttributes()-1) names+="\t";
		}
		return names;
	}


	public String attribute(int i) {
		return attributes[i];
	}

	public int numValues() {
		return attributes.length;
	}

	public double classValue() {
		return getToxicity();
	}

	public int numAttributes() {
		return attributes.length;
	}


}
