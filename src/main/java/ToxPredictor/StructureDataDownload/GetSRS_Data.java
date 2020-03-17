package ToxPredictor.StructureDataDownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;



public class GetSRS_Data {

	RecordSRS getChemical(String filepath) {
		
		try {
			Gson gson = new Gson();

			FileReader fr=new FileReader(filepath);

			RecordSRS[] chemicals = gson.fromJson(fr, RecordSRS[].class);
			
			RecordSRS chemical=chemicals[0];
//			System.out.println(chemical.toString("\t"));

			// test it to see if it outputs back out correctly:
			// System.out.println(c.toJSON());
			return chemical;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	void downloadWebPagesFromCASList(String textFilePath,String destFolderPath) {
		int waitSeconds=3;//time to wait in seconds
				
		try {								
			BufferedReader br=new BufferedReader(new FileReader(textFilePath));			
			Vector<String>casList=new Vector<String>();
			
			while (true) {
				String Line=br.readLine();
				if (Line==null) break;
				Line=Line.trim();
				if (!casList.contains(Line)) casList.add(Line);
			}
			br.close();
			
			for (String CAS:casList) {
				System.out.println(CAS);
				String strURL="https://cdxnodengn.epa.gov/cdx-srs-rest/substance/cas/"+CAS;
				String outputFilePath=destFolderPath+File.separator+CAS+".json";
				FileUtilities.downloadFileByBytes(strURL, outputFilePath);
				TimeUnit.SECONDS.sleep(waitSeconds);
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	void goThroughFolder(String folderPath) {
		File Folder=new File(folderPath);
		File [] files=Folder.listFiles();
		
		System.out.println(RecordChemIDplus.getHeader("\t"));
		for (File file:files) {
			RecordSRS cr=getChemical(file.getAbsolutePath());
			
			System.out.println(cr.toString("\t"));
		}
		
	}
	
	public static void main(String[] args) {
		GetSRS_Data g=new GetSRS_Data();

		String folderStructureData="data\\StructureData\\";
		String textFilePath=folderStructureData+"unique cas list no hit LLNA_echemportal.txt";

		String source="SRS";
		String folderSource=folderStructureData+source+"\\";
		String destFolderPath=folderSource+"json files";
		File DF=new File(destFolderPath);
		DF.mkdirs();
//		g.downloadWebPagesFromCASList(textFilePath, destFolderPath);
		g.goThroughFolder(destFolderPath);
//		g.getChemical(destFolderPath+"\\71-43-2.json");
//		g.getChemical(destFolderPath+"\\91-20-3.json");

	}

}
