package ToxPredictor.Application.Calculations;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ToxPredictor.Application.Calculations.ResultsCTS.Data.Tree.Child;

public class ResultsCTS {

	MetaInfo metaInfo;
	Data data;
	
	class MetaInfo {
		String modelVersion;
		String description;
	    String model;
	    String collection;
	    URL url;
	    String status;
	    String timestamp;
	    
	    class URL {
	    	String href;
	    	String type;
	    }
	}
	
	class Data {
		Tree tree;
		
		class Tree {
			Data2 data;			
			Child [] children;

			class Child {
				Data2 data;
				String name;
				String id;
				
				Child [] children;
			}
			
			class Data2 {
				String generation;
				String routes;
				String likelihood;
				String smiles;
				double accumulation;
				double production;
				double globalAccumulation;
			}
		}
	}
	
	public static ArrayList<Child> getAllChildren (ResultsCTS rc) {
		Data.Tree.Child [] children=rc.data.tree.children;
		ArrayList<Child>allChildren=new ArrayList<>();
		goThroughChildren(children,allChildren);
		
		return allChildren;
	}
	
	void testJsonToClass() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		
//		File jsonFile=new File("C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\ghs-data-gathering\\AA Dashboard\\CTS\\output.json");
		File jsonFile=new File("CTS/TPP human metabolism.json");
		
		try {
			FileReader fr=new FileReader(jsonFile);
			ResultsCTS rc = gson.fromJson(fr, ResultsCTS.class);			
//			System.out.println(gson.toJson(rc));//see if get back same data after storing in our java class		
			
			fr.close();
			
			ArrayList<Child>allChildren=getAllChildren(rc);

			double totalAccum=0;
			for (Child cc:allChildren) {
				System.out.println(cc.data.generation+"\t"+cc.data.routes+"\t"+cc.data.smiles+"\t"+cc.data.generation+"\t"+cc.data.likelihood+"\t"+cc.data.accumulation);
				totalAccum+=cc.data.accumulation;
			}
			System.out.println("totalAccum="+totalAccum);
			
			System.out.println("\nUnique, likely chemicals:");
			
			ArrayList<Child>uniqueChildren=combineChemicals(allChildren);
			
			for(Child cc:uniqueChildren) {
				if (!cc.data.likelihood.contentEquals("LIKELY")) continue;
				System.out.println(cc.data.generation+"\t"+cc.data.routes+"\t"+cc.data.smiles+"\t"+cc.data.generation+"\t"+cc.data.likelihood+"\t"+cc.data.accumulation);				
			}
			

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
		
	public static ArrayList<Child> combineChemicals(ArrayList<Child>allChildren) {
		
		Hashtable<String,Child>ht=new Hashtable<>();
		
		for (Child cc:allChildren) {
			
			if (ht.get(cc.data.smiles)==null) {
				ht.put(cc.data.smiles, cc);
			} else {
				Child childChemicalOld=ht.get(cc.data.smiles);
				double accumOld=childChemicalOld.data.accumulation;
				double accumNew=cc.data.accumulation;
				double accumTotal=accumOld+accumNew;
				childChemicalOld.data.accumulation=accumTotal;
			}
		}
		
		Set<String> keys = ht.keySet();
		
		ArrayList<Child>childChemicals2=new ArrayList<>();
		
		for(String key: keys){
			childChemicals2.add(ht.get(key));	
//			System.out.println(ht.get(key).smiles+"\t"+ht.get(key).accumulation);
        }
		
		return childChemicals2;
		
	}
	
	/***
	 * Gets all transformation products and metadata
	 * @param jo
	 * @param smilesChildren
	 */
	static void goThroughChildren(Child [] children,ArrayList<Child> allChildren) {
		for (int i=0;i<children.length;i++) {			
			Child child=children[i];			
			allChildren.add(child);
			//Recursively go through children again:
			goThroughChildren(child.children,allChildren);			
		}		
	}
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ResultsCTS r=new ResultsCTS();
		r.testJsonToClass();
	}

}
