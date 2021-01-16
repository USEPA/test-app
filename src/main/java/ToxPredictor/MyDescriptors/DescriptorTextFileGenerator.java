package ToxPredictor.MyDescriptors;





import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.AtomContainerSet;

import ToxPredictor.Application.WebTEST4;
import ToxPredictor.Application.Calculations.TaskStructureSearch;
import ToxPredictor.Database.ResolverDb2;
import ToxPredictor.MyDescriptors.DescriptorData;
import ToxPredictor.Utilities.HueckelAromaticityDetector;
import org.apache.commons.io.FileUtils;


public class DescriptorTextFileGenerator {

	void generateDescriptors(String folder,String inputFileName,String outputFileName) {
		
		try {
			//			

			File iFile=new File(folder+File.separator+inputFileName);

			System.out.println("input file="+iFile.getAbsolutePath()+"\t"+iFile.exists());

			AtomContainerSet acs=TaskStructureSearch.LoadFromList(iFile.getAbsolutePath(),TaskStructureSearch.TypeSmiles);


//			HueckelAromaticityDetector.debug=false;

			FileWriter fw=new FileWriter (folder+File.separator+outputFileName);
			for (int i=0;i<acs.getAtomContainerCount();i++) {
				AtomContainer ac=(AtomContainer)acs.getAtomContainer(i);
				DescriptorData dd=WebTEST4.goDescriptors(ac);//TODO fix errors involved with it trying to write structure images

				if (i==0) {				
					String header=getDescriptorDataHeader(dd);
					fw.write(header+"\r\n");					

				}

				String Line=convertDescriptorDataToLine(dd);

				fw.write(Line+"\r\n");
				fw.flush();
				//				System.out.println("Line="+Line);					


				//				System.out.println(dd.molname+"\t"+dd.x0);
			}

			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	String getDescriptorDataHeader(DescriptorData dd) {		
		LinkedHashMap<String, String>ddMap=dd.convertToLinkedHashMap();				
		String Line="";						
		for( String key : ddMap.keySet() ){
			if (key.contentEquals("Index")) continue;
			if (key.contentEquals("Query")) continue;
			if (key.contentEquals("SmilesRan")) continue;
			if (key.contentEquals("Error")) continue;					
			Line+=key+"\t";		  
			//	  System.out.println(key+"\t"+value);
		}
		Line=Line.trim();				
		return Line;		
	}

	String convertDescriptorDataToLine(DescriptorData dd) {

		LinkedHashMap<String, String>ddMap=dd.convertToLinkedHashMap();

		String Error=dd.Error;

		if (!Error.contentEquals("OK")) {
			return dd.ID+"\t"+Error;			
		}

		String Line="";


		for( String key : ddMap.keySet() ){

			if (key.contentEquals("Index")) continue;
			if (key.contentEquals("Query")) continue;
			if (key.contentEquals("SmilesRan")) continue;
			if (key.contentEquals("Error")) continue;

			String value = ddMap.get(key);
			Line+=value+"\t";		  
			//		System.out.println(key+"\t"+value);
		}
		Line=Line.trim();


		return Line;

	}
	
	
	public static void main(String[] args) {
		DescriptorTextFileGenerator d=new DescriptorTextFileGenerator();		
		
		HueckelAromaticityDetector.debug=false;		
		WebTEST4.createReports = false;// whether to reports at all for batch run
		WebTEST4.createDetailedReports = false;// create files for each method and detailed descriptor
		WebTEST4.generateWebpages = false;
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\RawData\\SkinSensitization\\merged with salts";
		d.generateDescriptors(folder, "SkinSensitization descriptors input.txt","SkinSensitization descriptors output.txt");
		
	}

}
