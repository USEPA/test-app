package gov.epa.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import AADashboard.Application.TableGenerator;

/**
 * Class to store chemicals
 * 
 * @author Todd Martin
 *
 */
public class Chemicals extends ArrayList<Chemical> {

	public class CustomComparator implements Comparator<Chemical>{
	    public int compare(Chemical c1,Chemical c2) {	        
	    	try {
		    	int CAS1=Integer.parseInt(c1.CAS.replace("-", ""));
		    	int CAS2=Integer.parseInt(c2.CAS.replace("-", ""));
		    	return CAS1-CAS2;	
	    	} catch (Exception ex) {
	    		return c1.CAS.compareTo(c2.CAS);
	    	}
	    	
	    }
	}
	
	public void sortByCAS() {
		Collections.sort(this,new CustomComparator());
	}
	
	public JsonElement toJsonElement() {
		String strJSON=this.toJSON();
		Gson gson = new Gson();
		JsonElement json = gson.fromJson(strJSON, JsonElement.class);
		
		
		return json;
	}
	
	public Chemical getChemical(String CAS) {
		
		for (Chemical chemical:this) {
			if (chemical.CAS.equals(CAS)) return chemical;
		}
		return null;
	}
	
	/**
	 * Looks for duplicate CAS numbers and merges changes from later versions into earlier ones
	 */
	public void mergeRevisions() {
		try {

			ArrayList<String> CASList=new ArrayList<>();

			//Get list of unique cas numbers that dont have an underscore:
			for (Chemical chemical:this) {
				if (!CASList.contains(chemical.CAS) && !chemical.CAS.contains("_"))  {
					CASList.add(chemical.CAS);
				}
			}


			for (String CAS : CASList) {
				//				if (!CAS.equals("100-44-7")) continue;
				Chemicals chemicals=new Chemicals();

				//Create array of chemicals that have the cas number (including ones with underscore):
				for (int i=1;i<=3;i++) {
					String casSeek="";

					if (i==1 ) casSeek=CAS;
					else casSeek=CAS+"_"+i;

					for (Chemical chemical:this) {
						if (chemical.CAS.equals(casSeek)) chemicals.add(chemical);
					}
				}

//				if (chemicals.isEmpty()) System.out.println(CAS);
				
				
				if (chemicals.size()==1) continue;
				
//				System.out.println(CAS+"\t"+chemicals.size());
				

				Chemical chemical0=chemicals.get(0);
				//				System.out.println(gson.toJson(chemical0));
				for (int i=1;i<chemicals.size();i++) {
					Chemical chemicali=chemicals.get(i);
					
//					if (chemicali.CAS.contains("107-02-8")) {
//						System.out.println(gson.toJson(chemicali));
//					}
					
					Chemical.merge(chemical0, chemicali);//merge changes from chemicali into chemical0
				}

				chemicals.remove(0);

				//Remove the chemicals with underscore from overall list of chemicals:
				for (int i=0;i<chemicals.size();i++) {					
					Chemical chemicali=chemicals.get(i);

					for (int j=0;j<this.size();j++) {
						Chemical chemicalj=this.get(j);

						if (chemicali.CAS.equals(chemicalj.CAS)) {
							this.remove(j);
							break;
						}
					}
				}


				//				System.out.println(gson.toJson(chemical0));
				//				System.out.println("");

				//				System.out.println(CAS);

			}//end loop over CAS numbers

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeToFile(String filePath) {

		try {

//			removeEmptyFields();//save space
			
//			this.CAS = CAS;

			File file = new File(filePath);
			file.getParentFile().mkdirs();

			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();

			FileWriter fw = new FileWriter(file);
			fw.write(gson.toJson(this));
			fw.flush();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public void toFlatFile(String filepath) {
		
		try {
			
			String del="|";
			
			FileWriter fw=new FileWriter(filepath);
			
			fw.write(FlatFileRecord.getHeader(del)+"\r\n");
			
			ArrayList<String>uniqueCAS=new ArrayList<>();
			
			
			for (Chemical chemical:this) {
				
				ArrayList<String>lines=chemical.toStringArray(del);
				
				if (!uniqueCAS.contains(chemical.CAS)) uniqueCAS.add(chemical.CAS);
				
				
				for (String line:lines) {
					line=line.replace("–", "-").replace("’", "'");//TODO use StringEscapeUtils?
					fw.write(line+"\r\n");
				}
				
//				fw.write(chemical.to);
			}
			fw.flush();
			fw.close();
			
//			for (String CAS:uniqueCAS) {
//				System.out.println(CAS);
//			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();// makes it multiline and readable
		Gson gson = builder.create();
		

		return gson.toJson(this);//all in one line!
	}
	
	public void toJSON_File(String filepath) {

		try {
			String result=this.toJSON();
			
			FileWriter fw=new FileWriter(filepath);
			fw.write(result);
			fw.flush();
			fw.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Chemicals loadFromJSON(String jsonFilePath) {

		try {
			Gson gson = new Gson();

			File file = new File(jsonFilePath);

			if (!file.exists())
				return null;

			Chemicals chemicals = gson.fromJson(new FileReader(jsonFilePath), Chemicals.class);

			
			//FieldNamingPolicy
			// System.out.println(chemicals.size());

			// test it to see if it outputs back out correctly:
			// System.out.println(c.toJSON());

			return chemicals;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	void testWriteToExcel() {
		String folder="D:\\MyToxicity5\\HazardProfiles\\";
		Chemicals chemicals = loadFromJSON(folder +"HazardProfiles7.json");
		toExcelFile(chemicals, folder+"HazardProfilesEdit.xlsx");
	}
	
	
	static void toExcelFile(Chemicals chemicals,String excelFilePath) {
		XSSFWorkbook workbook = new XSSFWorkbook();		
		TableGenerator tg=new TableGenerator();
		
		tg.writeFinalScoresToWorkbook(chemicals, workbook);
		tg.writeScoreRecordsToWorkbook(chemicals,workbook);
		
		try {
			FileOutputStream out=new FileOutputStream(new File(excelFilePath));
	        workbook.write(out);
	        out.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
//		Chemicals chemicals=new Chemicals();
//		chemicals.testWriteToExcel();
		
		String folder="AADashboard calcs\\";
		Chemicals chemicalsApplication = loadFromJSON(folder +"output AADashboard top 20-TESTApplication.json");
		chemicalsApplication.toFlatFile(folder+"output AADashboard top 20-TESTApplication.txt");
		Chemicals.toExcelFile(chemicalsApplication,folder+"output AADashboard top 20-TESTApplication.xlsx");
		
		Chemicals chemicalsWebTEST = loadFromJSON(folder +"output AADashboard top 20-webtest2.json");
		chemicalsWebTEST.toFlatFile(folder+"output AADashboard top 20-webtest2.txt");
		Chemicals.toExcelFile(chemicalsWebTEST, folder+"output AADashboard top 20-webtest2.xlsx");
		
	}

	

}
