package ToxPredictor.MyDescriptors;
import java.io.*;
import java.lang.reflect.Field;
import ToxPredictor.Utilities.Utilities;

public class CreateSubsetDescriptorTables {

	
	
//	String mainfolder="C:/Documents and Settings/tmarti02/My Documents/javax/QSAR/QSAR/DescriptorTextTables";
	String mainfolder="ToxPredictor/DescriptorTextTables";
	
	
//	String paper="Eldred";
//	String paper="LC50";
	String paper="IGC50";
//	String paper="LD50";
		
	String Parent="2d";
	String Subset="FDA_Subset";

	String folder=mainfolder+"/"+paper+" Data Files/2d";
	String newfolder=mainfolder+"/"+paper+" Data Files/"+Subset;

//	String folder=mainfolder+"/"+paper+" Data Files";
//	String newfolder=mainfolder+"/"+paper+" Data Files";
	
	DescriptorData dd=new DescriptorData();
	
	
	String[] varlistSubset = { "strChi", "strKappa", "strES", "strES_acnt","strHES",
			"strESMaxMin", "str2D","strCon", "strMP" }; // array of list names

//	omitted = strIC, strMDEC, strBurden, strTopo, str2DAuto, strWalk, our fragments 

	
	/**
	 * Creates subset file for overall set
	 */
	void run2() {
		String folder=mainfolder+"/"+paper+" Data Files";

		File OldFile=new File(folder+"/"+paper+" overall set - 2d.csv");
		File NewFile=new File(folder+"/"+paper+" overall set - FDA_Subset.csv");
		
//		File OldFile=new File(folder+"/"+paper+" overall set - 2d-omitspecial.csv");
//		File NewFile=new File(folder+"/"+paper+" overall set - FDA_Subset-omitspecial.csv");

		
		this.CreateSubsetFile(OldFile, NewFile);
		

	}
	
	void run() {
		System.out.println(folder);
		java.io.File fileFolder=new java.io.File(folder);
		java.io.File newfileFolder=new java.io.File(newfolder);
		if (!newfileFolder.exists()) newfileFolder.mkdir(); 
		
		
		File [] files=fileFolder.listFiles();
		
		for (int i=0;i<files.length;i++) {
			String name=files[i].getName();
			System.out.println("old file = "+name);
			
//			if (name.indexOf("rnd")==-1) continue;
			if (name.indexOf("xml")>-1) continue;
			
			String currentPaper=name.substring(0,name.indexOf(" "));
			
			name=name.substring(name.indexOf(" ")+1,name.length());
			
			String set=name.substring(0,name.indexOf(" "));
			
			name=name.substring(name.indexOf(" ")+1,name.length());
			name=name.substring(name.indexOf(" ")+1,name.length());
			name=name.substring(name.indexOf(" ")+1,name.length());
			
			String currentParent="";
			
			String newFileName="";
			
			if (name.indexOf("-")>-1) {
				currentParent=name.substring(0,name.indexOf("-"));
				
				if (!currentParent.equals(Parent)) continue;
				
				name=name.substring(name.indexOf("-")+1,name.length());
								
				String currentDescription=name.substring(0,name.indexOf("."));
				
				newFileName=currentPaper+" "+set+" set - "+Subset+"-"+currentDescription+".csv";

				
			} else { //case where dont have "desc" like "rnd1"
				currentParent=name.substring(0,name.indexOf("."));

				if (!currentParent.equals(Parent)) continue;
				
				newFileName=currentPaper+" "+set+" set - "+Subset+".csv";				
			}
//			System.out.println("currentParent="+currentParent);
			
			File NewFile=new File(newfolder+"/"+newFileName);
			System.out.println("new file = "+newFileName);
			
			this.CreateSubsetFile(files[i], NewFile);
			
			
		}
		
		
		
		
		
		
	}
	
	void Bob() {
		
		int count=0;
		
		for (int i=0;i<varlistSubset.length;i++) {
			System.out.println(varlistSubset[i]);
			
			try {
			Field myField = dd.getClass().getField(varlistSubset[i]);
			String[] names = (String[]) myField.get(dd);
			
			count+=names.length;

			for (int j=0;j<names.length;j++) {
				System.out.print(names[j]+"\t");
			}
			System.out.print("\n");
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Count = "+count);
		
	}
	
	
	
	public void CreateSubsetFile(File OldFile,File NewFile) {
		
		try {
			String d=",";
			
			BufferedReader br=new BufferedReader(new FileReader(OldFile));
			FileWriter fw=new FileWriter(NewFile);
			
			String header=br.readLine();
			
			header=header.replaceAll(",SssSnH,", ",SsssSnH,"); //mistake in original file
			header=header.replaceAll(",SssSnH_acnt,", ",SsssSnH_acnt,"); //mistake in original file
			
//			System.out.println(header.substring(717,760));
			
			java.util.LinkedList<String>hl=Utilities.Parse3(header, d);

//			System.out.println(hl.get(0));
			
			fw.write(hl.get(0)+d+hl.get(1)+d);
			
			int count=0; //count of descriptors
			
			for (int i=0;i<varlistSubset.length;i++) {
//				System.out.println(varlistSubset[i]);
				
				
				Field myField = dd.getClass().getField(varlistSubset[i]);
				String[] names = (String[]) myField.get(dd);
				
				count+=names.length;

				for (int j=0;j<names.length;j++) {
					fw.write(names[j]);										
					if (i<varlistSubset.length-1 || j<names.length-1) fw.write(d);
				}
				
				
			}
			
			fw.write("\r\n");
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				
				
				java.util.LinkedList<String>l=ToxPredictor.Utilities.Utilities.Parse3(Line, d);
				
				fw.write(l.get(0)+d+l.get(1)+d);
				
				for (int i=0;i<varlistSubset.length;i++) {
//					System.out.println(varlistSubset[i]);
					
					
					Field myField = dd.getClass().getField(varlistSubset[i]);
					String[] names = (String[]) myField.get(dd);
					
					count+=names.length;

					for (int j=0;j<names.length;j++) {
//						System.out.println(names[j]);
						int fieldnum=this.FindFieldNumber(hl, names[j]);
						String val=l.get(fieldnum);
						fw.write(val);
						
						if (i<varlistSubset.length-1 || j<names.length-1) fw.write(d);
					}
					
					
				}
				fw.write("\r\n");
				
			}
			
			
			fw.close();
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static int FindFieldNumber(java.util.LinkedList<String> myList, String field) {
	    
	    for (int i = 0; i <= myList.size() - 1; i++) {
	      	    	
	    	if (field.equals(myList.get(i))) {
	        return i;
	      }
	    }

	    System.out.println("Field = "+field+" is missing");
	    
	    return -1;

	  }
	
	public static void main(String[] args) {
		CreateSubsetDescriptorTables c=new CreateSubsetDescriptorTables();
		c.run();
//		c.run2();
//		c.Bob();
	}
		
	
	
}
