package ToxPredictor.Utilities;

import java.util.Hashtable;

public class AdaptUtilities {

	
	public static void IXEROM2(int Array[][],int COLS,int value) {
		for (int i=0;i<=Array.length-1;i++) {
			for (int j=0;j<=COLS-1;j++) {
				Array[i][j]=value;
			}
		}
						
	}
	
	public static void XEROM(double [] Array, double value) {
		for (int i=0;i<=Array.length-1;i++) {
			Array[i]=value;
		}
	}
	
	
	public static double GetDoubleFromDataStringArray(String [] strData,String strVar) {
		
		String strValue=GetValueFromDataStringArray(strData,strVar);
		
		if (! (strValue instanceof String)) return -9999;
		double dValue=Double.parseDouble(strValue);
		return dValue;
		
		
	}
	
	
	public static String GetValueFromDataStringArray(String [] strData,String strVar) {
		String value="-9999";
		
		for (int i=0;i<=strData.length-1;i++) {
			
			if (!(strData[i] instanceof String) ) continue;
			
			String s=strData[i];
			
			if (s.indexOf("<")>-1) {				
				
				s=s.substring(1,s.length()); // trim off first char
//				System.out.println(s);
				
				if (s.indexOf("<")>-1 && s.indexOf(">")>-1) {
					String strVar2=s.substring(s.indexOf("<")+1,s.indexOf(">"));
					if (strVar2.equals(strVar)) {					
						value=strData[i+1].trim();
						return value;
					}
				}
			}
			
		}
		
		
		return value;
	}
	
	public static java.util.Hashtable GetAllValuesFromDataStringArray(String [] strData) {
		String value="-9999";
		
		Hashtable ht=new Hashtable();
		
		for (int i=0;i<=strData.length-1;i++) {
			
			if (!(strData[i] instanceof String) ) continue;
			
			String s=strData[i];
			
			if (s.indexOf("<")>-1) {				
				
				s=s.substring(1,s.length()); // trim off first char
				
				String strVar=s.substring(s.indexOf("<")+1,s.indexOf(">"));
									
				value=strData[i+1].trim();
				ht.put(strVar,value);				
			}
			
		}
		
		return ht;
		
	}
	
	
}
