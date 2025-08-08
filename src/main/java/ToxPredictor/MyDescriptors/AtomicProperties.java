package ToxPredictor.MyDescriptors;

import java.util.Hashtable;
import java.io.*;
import ToxPredictor.Utilities.Utilities;
import java.util.LinkedList;


public class AtomicProperties {

	private static AtomicProperties ap=null;
	
	private Hashtable<String,Double> htMass=new Hashtable<>();
	private Hashtable<String,Double> htVdWVolume=new Hashtable<>();
	private Hashtable<String,Double> htElectronegativity=new Hashtable<>();
	private Hashtable<String,Double> htPolarizability=new Hashtable<>();
	
	
	private AtomicProperties() throws IOException {
		
			//TEST3
		String DataFile="whim weights.txt";
//		String DataFile="SystemData/whim weights v3.0.txt";//use file in src/main/resources instead of jar file
				
		InputStream ins=this.getClass().getClassLoader().getResourceAsStream(DataFile);
		InputStreamReader isr=new InputStreamReader(ins);
		BufferedReader br=new BufferedReader(isr);
		
		String Header=br.readLine(); // header
		
		String Line="";
		while (true) {
			Line=br.readLine();
			
//			System.out.println(Line);
			
			if (!(Line instanceof String)) {
				break;
			}
			
			LinkedList<String> l=Utilities.Parse(Line,"\t");
			
			String symbol=(String)l.get(0);
			htMass.put(symbol,Double.parseDouble(l.get(1)));
			htVdWVolume.put(symbol,Double.parseDouble(l.get(2)));
			htElectronegativity.put(symbol,Double.parseDouble(l.get(3)));
			htPolarizability.put(symbol,Double.parseDouble(l.get(4)));
			
		}
						
		br.close();
		
	}

	public Double GetVdWVolume(String symbol) {
		if(htVdWVolume.containsKey(symbol)) return htVdWVolume.get(symbol);
		else return null;
	}
	
	public Double GetNormalizedVdWVolume(String symbol) {
		if(htVdWVolume.containsKey(symbol)) {
			return GetVdWVolume(symbol)/GetVdWVolume("C");
		} else return null;
	}
	
	public Double GetElectronegativity(String symbol) {
		if(htElectronegativity.containsKey(symbol)) return htElectronegativity.get(symbol);
		else return null;
	}
	
	public Double GetNormalizedElectronegativity(String symbol) {
		if(htElectronegativity.containsKey(symbol)) 
			return GetElectronegativity(symbol)/GetElectronegativity("C");
		else return null;
	}
	public Double GetPolarizability(String symbol) {
		if(htPolarizability.containsKey(symbol)) return htPolarizability.get(symbol);
		else return null;
	}
	
	public Double GetNormalizedPolarizability(String symbol) {
		if(htPolarizability.containsKey(symbol)) 
			return GetPolarizability(symbol)/GetPolarizability("C");
		else return null;
	}
	
	public Double GetMass(String symbol) {
		if(htMass.containsKey(symbol)) return htMass.get(symbol);
		else return null;
	}
	
	public Double GetNormalizedMass(String symbol) {
		
		if(htMass.containsKey(symbol)) 
			return GetMass(symbol)/this.GetMass("C");
		else return null;		
	}
	
	public static AtomicProperties getInstance() throws IOException
	{
		if (ap == null) {
			ap = new AtomicProperties();
		}
		return ap;
	}
}
