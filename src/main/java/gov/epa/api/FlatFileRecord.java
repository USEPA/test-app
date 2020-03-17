package gov.epa.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;

import org.apache.logging.log4j.util.Strings;

import AADashboard.Application.AADashboard;

public class FlatFileRecord {
	
	
	public String CAS;
	public String name;
	
	public String hazard_name;
	
	public String source;// where the record came from
	public String score;// i.e. L,M,H,VH

	public String category;// i.e. Category 1
	public String hazard_code;// code for hazard, i.e. "H301"
	public String hazard_statement;// text based description of what hazard they think it is
	public String rationale;// why classification was assigned
	public String route;// i.e. oral, dermal, inhalation- used mainly for acute mammalian toxicity for
						// now
	public String note;// extra clarification that doesn't fit into above fields
	public String note2;// extra clarification that doesn't fit into above fields
	
	
	// **************************************************************************************
	public Double valueMass;// quantitative value in mass units such as mg/L
	public String valueMassUnits;
	public String valueMassOperator;// "<",">", or ""

	public Boolean valueActive;// binary value
	
	public String authority;

//	public static String[] fieldNames = { "CAS","name","hazard_name","source", "score", "route", "category", "hazard_code",
//			"hazard_statement", "rationale", "note","note2","valueMassOperator","valueMass","valueMassUnits"};

	public static String[] fieldNames = { "CAS","name","hazard_name","source", "authority","score", "route", "category", "hazard_code",
			"hazard_statement", "rationale", "note","note2","valueMassOperator","valueMass","valueMassUnits","valueActive"};

	public static String[] displayFieldNames = { "CAS","Name","Hazard Name","Source", "Authority","Score", "Route", 
			"Category", "Hazard Code","Hazard Statement", "Rationale", "Note","Note2","Value Mass Operator","Value Mass","Value Mass Units","Value Active"};
	
	public static String getHeader() {
		return getHeader("\t");
	}
	
	public static FlatFileRecord createFlatFileRecord(String line) {
		FlatFileRecord f = new FlatFileRecord();
		LinkedList<String> list = ToxPredictor.Utilities.Utilities.Parse(line, "|");
//		if (list.getFirst().isEmpty()) System.out.println(line);
		
		for (int i = 0; i < fieldNames.length; i++) {

			try {
				String val = list.get(i);
				if (fieldNames[i].equals("valueMass")) {
					if (!val.isEmpty()) {
						f.valueMass = Double.parseDouble(val);// no need to use reflection for one field
					}
				} else {
					if (!val.isEmpty()) {
						Field myField = f.getClass().getDeclaredField(fieldNames[i]);
//						System.out.println(fieldNames[i]+"\t"+val);
						myField.set(f, val);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return f;

	}
	
	public static String getHeader(String d) {
		// TODO Auto-generated method stub

		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
//			Line += "\""+fieldNames[i]+"\"";
			Line += fieldNames[i];
			if (i < fieldNames.length - 1) {
				Line += d;
			} 
		}

		return Line;
	}
	
	public static String getHeader(String [] fieldNames,String d) {
		// TODO Auto-generated method stub

		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
//			Line += "\""+fieldNames[i]+"\"";
			Line += fieldNames[i];
			if (i < fieldNames.length - 1) {
				Line += d;
			} 
		}

		return Line;
	}
	
	public String toString2() {
		
		String str = CAS + "\t" + name + "\t" + hazard_name + "\t" + source + "\t" + score + "\t" + category
				+ "\t" + hazard_code + "\t" + hazard_statement + "\t" + rationale + "\t" + route + "\t" + note + "\t"
				+ note2 + "\t" + valueMassOperator+"\t"+valueMass+"\t"+valueMassUnits;

		return str;
	}
	
	public ScoreRecord getScoreRecord() {
		ScoreRecord sr=new ScoreRecord();
		//TODO- simplify with reflection
		
		if (name!=null) sr.name=name;
		sr.source=source;
		if(score!=null && !score.isEmpty()) sr.score=score;
		if (category!=null && !category.isEmpty()) sr.category=category;
		
//		System.out.println(hazard_code);
		
		if (!Strings.isEmpty(hazard_code)) sr.hazard_code=hazard_code;
		if (!Strings.isEmpty(hazard_statement)) sr.hazard_statement=hazard_statement;
		if (!Strings.isEmpty(rationale)) sr.rationale=rationale;
		if (!Strings.isEmpty(route)) sr.route=route;
		if (!Strings.isEmpty(note)) sr.note=note;
		if (!Strings.isEmpty(note2)) sr.note2=note2;
		if (!Strings.isEmpty(valueMassOperator)) sr.valueMassOperator=valueMassOperator;
		if (valueMass!=null)	sr.valueMass=valueMass;
		if (!Strings.isEmpty(valueMassUnits)) sr.valueMassUnits=valueMassUnits;
		if (valueActive!=null) sr.valueActive=valueActive;
		if (!Strings.isEmpty(authority)) sr.authority=authority;
		
		return sr;
	}
	
	public String toString() {
		return toString("\t");
	}
	
	//convert to string by reflection:
	public String toString(String d) {
		
		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
			try {
			
				
				Field myField = this.getClass().getDeclaredField(fieldNames[i]);
				
				String val=null;
				
				if (fieldNames[i].equals("valueMass")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=(Double)myField.get(this)+"";
					}
					
				} else if (fieldNames[i].equals("valueActive")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=(Boolean)myField.get(this)+"";
					}
				
				} else {
					if (myField.get(this)==null) {
//						val="\"\"";
						val="";
					} else {
//						val="\""+(String)myField.get(this)+"\"";
						val=(String)myField.get(this);
					} 
				}
				
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");
				
//				if (fieldNames[i].equals("note")) {
//					System.out.println(CAS+"\t"+source+"\t"+hazard_name+"\t"+val);
//				}

				if (val.contains(d)) {
					System.out.println(this.CAS+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
				}
				
				Line += val;
				if (i < fieldNames.length - 1) {
					Line += d;
				}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Line;

	}
	
	//convert to string by reflection:
	public String toString(String [] fieldNames,String d) {
		
		String Line = "";
		for (int i = 0; i < fieldNames.length; i++) {
			try {
			
				
				Field myField = this.getClass().getDeclaredField(fieldNames[i]);
				
				String val=null;
				
				if (fieldNames[i].equals("valueMass")) {
					if (myField.get(this)==null) {
						val="";	
					} else {
						val=(Double)myField.get(this)+"";
					}
					
				} else {
					if (myField.get(this)==null) {
//						val="\"\"";
						val="";
					} else {
//						val="\""+(String)myField.get(this)+"\"";
						val=(String)myField.get(this);
					} 
				}
				
				val=val.replace("\r\n","<br>");
				val=val.replace("\n","<br>");
				
//				if (fieldNames[i].equals("note")) {
//					System.out.println(CAS+"\t"+source+"\t"+hazard_name+"\t"+val);
//				}

				if (val.contains(d)) {
					System.out.println(this.CAS+"\t"+fieldNames[i]+"\t"+val+"\thas delimiter");
				}
				
				Line += val;
				if (i < fieldNames.length - 1) {
					Line += d;
				}
			
			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return Line;

	}

	
	
//	void createFlatFileFromAllSources() {
//		AADashboard a=new AADashboard();
//		String d="|";
//		
//		try {
//			
//			FileWriter fw=new FileWriter("AA Dashboard\\Data\\dictionary\\text output\\flat file 2018-05-10.txt");
//			
//			fw.write(FlatFileRecord.getHeader(d)+"\r\n");
//			
//			int NOCAScount=0;
//			
//			for (String jsonFolder:a.jsonFolders) {
//				
//				System.out.println(jsonFolder);
//				
//				File Folder = new File(jsonFolder);
//				
//				File[] files = Folder.listFiles();
//
//				int counter=0;
//				for (File file:files) {
//					
//					if (counter%1000==0) {
//						System.out.println(counter);
//					}
//					
//					Chemical chemical=Chemical.loadFromJSON(file);
//					
//					if (chemical.CAS==null || chemical.CAS.isEmpty()) {
//						NOCAScount++;
//						chemical.CAS="NOCAS"+NOCAScount;
//					}
//					
//					ArrayList<String>lines=chemical.toStringArray(d);
//					
//					for(String line:lines) {
//						fw.write(line+"\r\n");
//					}
//					fw.flush();
//					
//					counter++;
//				}
//			}
//			
//			fw.close();
//			
//		
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		
//	}
	
	
	public static void createFlatFileFromAllSources(String outputPath) {
		AADashboard a=new AADashboard();
		String d="|";
		
		try {
			
			FileWriter fw=new FileWriter(outputPath);
			fw.write(FlatFileRecord.getHeader(d)+"\r\n");
			
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			for (String source:a.sources) {
				
				String filePathFlatChemicalRecords = AADashboard.dataFolder+File.separator+source+File.separator+source +" Chemical Records.txt";

				File file=new File(filePathFlatChemicalRecords);
				
				if (!file.exists())  {
					System.out.println("*** "+source+" text file missing");
					continue;
				} else {
					System.out.println(source);
				}
				
				ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(filePathFlatChemicalRecords);
				
				String header=lines.remove(0);

				for (String line:lines) {
					
//					FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
					
					String CAS=line.substring(0,line.indexOf(d));
					line=line.substring(line.indexOf(d)+1,line.length());
					String name=line.substring(0,line.indexOf(d));
					line=line.substring(line.indexOf(d)+1,line.length());
					
					if (CAS.isEmpty()) {
						if (name.isEmpty()) {
							System.out.println("CAS and name =null: "+line);
							continue;
						}
						
						if (htCASName.get(name)==null) {
							String newCAS="NOCAS"+(htCASName.size()+1);
							htCASName.put(name,newCAS);
							CAS=newCAS;
						} else {
							CAS=htCASName.get(name);
						}
						
					}
					
					line=line.replace("<br><br><br>","<br><br>");
					fw.write(CAS+d+name+d+line+"\r\n");

				}
				fw.flush();
			}
			
			fw.close();
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * Get count of records per score for all chemicals
	 * 
	 * @param outputPath
	 */
	public static void analyzeRecords(String filePath,String outputFilePath) {

		ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(filePath);

		String header=lines.remove(0);

		Chemical chemical=new Chemical();

		FileWriter fw;
		try {
			fw = new FileWriter(outputFilePath);

			String d="|";
					

			fw.write("CAS"+d+"name"+d);
			for (Score score:chemical.scores) {
				fw.write(score.hazard_name+d);
			}
			fw.write("\n");

			for (String line:lines) {
				FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);

				//			System.out.println(line);

				if (chemical.CAS==null) {
					chemical.CAS=f.CAS;
					chemical.name=f.name;
//				} else if ( !f.CAS.equals(chemical.CAS) || !f.name.equals(chemical.name)) {
				} else if ( !f.CAS.equals(chemical.CAS)) {
					
					fw.write(chemical.CAS+d+chemical.name+d);
					for (Score score:chemical.scores) {
						fw.write(score.records.size()+d);
					}
					fw.write("\n");
					chemical=new Chemical();
				}

				Score score=chemical.getScore(f.hazard_name);
				ScoreRecord sr=f.getScoreRecord();
				score.records.add(sr);


			}
			fw.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	
	public static void createFlatFileFromAllSourcesSortedByCAS(String outputPath) {
		AADashboard a=new AADashboard();
		String d="|";
		
		try {
			
			
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			ArrayList<String>overallLines=new ArrayList<>();
			
			for (String source:a.sources) {
				
				String filePathFlatChemicalRecords = AADashboard.dataFolder+File.separator+source+File.separator+source +" Chemical Records.txt";

				File file=new File(filePathFlatChemicalRecords);
				
				if (!file.exists())  {
					System.out.println("*** "+source+" text file missing");
					continue;
				} else {
					System.out.println(source);
				}
				
				ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(filePathFlatChemicalRecords);
				
				String header=lines.remove(0);

				for (String line:lines) {
					
//					FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
					
					String CAS=line.substring(0,line.indexOf(d)).trim();
					line=line.substring(line.indexOf(d)+1,line.length());
					String name=line.substring(0,line.indexOf(d));
					line=line.substring(line.indexOf(d)+1,line.length());
					
					if (CAS.isEmpty()) {
						if (name.isEmpty()) {
							System.out.println("CAS and name =null: "+line);
							continue;
						}
						
						if (htCASName.get(name)==null) {
							String newCAS="NOCAS"+(htCASName.size()+1);
							htCASName.put(name,newCAS);
							CAS=newCAS;
						} else {
							CAS=htCASName.get(name);
						}
						
					}
					
					line=line.replace("<br><br><br>","<br><br>");
					overallLines.add(CAS+d+name+d+line);
				}
			}
			
			Collections.sort(overallLines);
			
			FileWriter fw=new FileWriter(outputPath);
			fw.write(FlatFileRecord.getHeader(d)+"\r\n");

			for (String Line:overallLines) {
				fw.write(Line+"\r\n");
			}
			fw.flush();
			fw.close();
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		
	}
	
	public static void createFlatFileFromAllSourcesSortedByCASWithAuthority(String outputPath) {
		AADashboard a=new AADashboard();
		String d="|";
		
		try {
			
			Hashtable<String,String>htCASName=new Hashtable<>();
			
			ArrayList<String>overallLines=new ArrayList<>();
			
			for (String source:a.sources) {
				
				String filePathFlatChemicalRecords = AADashboard.dataFolder+File.separator+source+File.separator+source +" Chemical Records.txt";

				File file=new File(filePathFlatChemicalRecords);
				
				if (!file.exists())  {
					System.out.println("*** "+source+" text file missing");
					continue;
				} else {
					System.out.println(source);
				}
				
				ArrayList<String>lines=ToxPredictor.Utilities.Utilities.readFileToArray(filePathFlatChemicalRecords);
				
				String header=lines.remove(0);

				for (String line:lines) {
					
//					FlatFileRecord f=FlatFileRecord.createFlatFileRecord(line);
					
					String CAS=line.substring(0,line.indexOf(d)).trim();
					line=line.substring(line.indexOf(d)+1,line.length());
					String name=line.substring(0,line.indexOf(d));
					line=line.substring(line.indexOf(d)+1,line.length());
					
					if (CAS.isEmpty()) {
						if (name.isEmpty()) {
							System.out.println("CAS and name =null: "+line);
							continue;
						}
						
						if (htCASName.get(name)==null) {
							String newCAS="NOCAS"+(htCASName.size()+1);
							htCASName.put(name,newCAS);
							CAS=newCAS;
						} else {
							CAS=htCASName.get(name);
						}
						
					}
					
					line=line.replace("<br><br><br>","<br><br>");
					overallLines.add(CAS+d+name+d+line);
				}
			}
			
			Collections.sort(overallLines);
			
			FileWriter fw=new FileWriter(outputPath);
			fw.write(FlatFileRecord.getHeader(FlatFileRecord.fieldNames,d)+d+"Authority"+"\r\n");

			for (String Line:overallLines) {				
				FlatFileRecord f=createFlatFileRecord(Line);
				f.authority=ScoreRecord.getListType(f.source);				
				fw.write(f.toString(fieldNames, d)+"\r\n");
			}
			fw.flush();
			fw.close();
			
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
			
	static void deleteExtraFiles() {
		
//		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Inhalation LC50 searches";
//		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Oral LD50 searches";
		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Skin LD50 searches";
		
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		for (File file:files) {
			if (!file.getName().contains("_files")) continue;
			
			file.deleteOnExit();
			
			File [] files2=file.listFiles();
			
			if (files2==null) continue;
		
			for (File file2:files2) {
				file2.deleteOnExit();
			}
			
			
			
			System.out.println(file.getName());
		}
		
		
	}

	static void deleteExtraFiles2() {
		
//		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Oral LD50 webpages";
		String folder="L:\\Priv\\Cin\\NRMRL\\CompTox\\javax\\web-test\\AA Dashboard\\Data\\Chemidplus\\Inhalation LC50 webpages";
		
		File Folder=new File(folder);
		
		File [] files=Folder.listFiles();
		
		for (File file:files) {
			file.deleteOnExit();
			System.out.println(file.getName());
		}
		
		
	}
	

	
	public static void main(String[] args) {
		
		
		// TODO Auto-generated method stub
		FlatFileRecord f=new FlatFileRecord();
		
		String folder="AA Dashboard/Data/dictionary/text output";
		String filename="flat file 2019-07-18.txt";
		
		
//		f.createFlatFileFromAllSources(folder+"/"+filename);
		f.createFlatFileFromAllSourcesSortedByCASWithAuthority(folder+"/"+filename);
		
		
//		Chemical chemical1=getChemicalFromRecords("100-37-8");
//		Chemical chemical2=getChemicalFromRecords("100-44-7");
//		
////		Chemical chemical1=getChemicalFromRecords("71-43-2");
////		Chemical chemical2=getChemicalFromRecords("bob");
//		
//		Chemicals chemicals=new Chemicals();
//		chemicals.add(chemical1);
//		chemicals.add(chemical2);
//		String strJSON=chemicals.toJSON();
////		
//		System.out.println(strJSON);
		
		
	}

}
